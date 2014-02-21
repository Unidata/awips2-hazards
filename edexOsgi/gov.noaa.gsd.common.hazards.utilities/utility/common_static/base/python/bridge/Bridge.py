'''
 The bridge provides a common interface to the recommender 
 and product generation frameworks as well as to the hazard 
 services database and localization files. 
 
 Using the bridge, the Hazard Services HMI, recommenders and 
 product generators do not need to know the specific details
 about accessing these frameworks and data sources, in effect 
 decoupling them from implementation details.
 
 Python is used for this module to work efficiently with 
 non-homogeneous data structures. 
 
 @since: February 2012
 @author: GSD Hazard Services Team
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 Apr 19, 2013            blawrenc     Made fixes for code review
                                      Replaced most string literals
                                      with constants from
                                      HazardConstants.py
 Jul 15, 2013     585    Chris.Golden Added passing of event bus
                                      to job listeners, since event
                                      bus is no longer a singleton.
 August 14, 2013  1360   hansen       Modified to return hazard event
                                      set from product generators
 August 20 2013   1360   blawrenc     Removed eventDictsToHazardEvents().
                                      This is not used.
 January 29 2013  2882   bkowal       Eliminated Python Script Adapter
'''


import ast, types, time, traceback
import DatabaseStorage
import json

from HazardServicesConfig import HazardServicesConfig
from HazardMetaData import HazardMetaData
from HazardConstants import *
from LocalizationInterface import LocalizationInterface
from PythonOverrider import importModule
import collections

try:
    import HazardEventPythonAdapter as Adapter
    from HazardEventPythonAdapter import HazardEventPythonAdapter
    import JUtil 
    from jep import *  
    import logging, UFStatusHandler
except:
    tbData = traceback.format_exc()
    print tbData

class Bridge:
    #
    # Localization path to HazardTypes.py
    hazardTypesPath = 'hazardServices/hazardTypes/HazardTypes.py'
    
    def __init__(self):                       
        self.DatabaseStorage = DatabaseStorage.DatabaseStorage()
        
        self.logger = logging.getLogger('Bridge')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'Bridge', level=logging.INFO))
        self.logger.setLevel(logging.INFO)         
        
        # The try/catch block is needed because when this module is used from python-only
        # unit tests, we have no access to JAVA stuff which is embedded in HazardEventPythonAdapter
        try:
            self.hazardEventPythonAdapter = HazardEventPythonAdapter()
        except:
            tbData = traceback.format_exc()
            self.logger.error(tbData)
        
        self.textUtilityRoot = TEXT_UTILITY_ROOT
        self.caveEdexRepo = {}

###################################################################################
    ### External Interface methods
        
    def setHazardEventManager(self, hazardEventManager):
        '''
        Sets the object which manages the storage and 
        retrieval of hazard event information. The implementation
        of the hazard event manager determines how the
        events are actually stored (in memory, database, etc.)
        
        @param hazardEventManager:  The hazard event manager object.
                                    Implements IHazardEventManager interface. 
        '''
        self.hazardEventPythonAdapter.setHazardEventManager(hazardEventManager)
                
    # Data interfaces -- Access to data in the NationalDatabase, AWIPS II, or external source
    def putData(self, criteria):
        '''
        Persist data in an external source.
        @param criteria: dataType, plus data
        '''
        info = json.loads(criteria, 'UTF-8')
        dataType = info.get(DATA_TYPE_KEY)
        if dataType == EVENTS_DATA:
            hazardDicts = info.get(EVENTDICTS_KEY)
            productCategory = info.get(PRODUCT_CATEGORY_KEY)
            siteID = info.get(SITE_ID)
            criteria = self.putHazards(hazardDicts, productCategory, siteID)
            eventDicts = json.loads(criteria).get(EVENTDICTS_KEY)
            self.writeEventDB(eventDicts)
        elif dataType in [SETTINGS_DATA, HAZARD_TYPES_DATA, HAZARD_CATEGORIES_DATA, PRODUCT_DATA,
                        STARTUP_CONFIG_DATA]:
            hazardServicesConfig = HazardServicesConfig(dataType)
            name, configData, level = info.get('name'), info.get('configData'), info.get(LOCALIZATION_LEVEL)
            forSite = True if level == 'Site' else False
            hazardServicesConfig.writeConfigData(name, configData, forSite)
        else:         
            self.DatabaseStorage.putData(criteria)
        

    # This is a general routine for restricting the set of data returned
    # based on the criteria.  If the object is a dict at the top level,
    # selecting individual members to keep can be done with a 'keys' entry,
    # which can be a single key or a list of keys to return.  If the object
    # is either a list or dict at the top level, but the members are dict
    # objects, then which members to keep can be done using a 'filter' entry,
    # which must be a dict object.  To be included the member objects must
    # have the same value for each entry in the 'filter'.  Also, if the members
    # are dict objects, then the 'fields' entry is a key or list of keys
    # to retain entries for in the members.
    def returnFilteredData(self, data, criteria) :

        # Make sure our criteria is a dict
        if isinstance(criteria, str) or isinstance(criteria, unicode) :
            criteria = json.loads(criteria, \
                          object_pairs_hook=collections.OrderedDict)
        if not isinstance(criteria, dict) :
            return data

        # Get list of fields and our filter, if there.
        fields = criteria.get(FIELDS_KEY)
        if isinstance(fields, str) or isinstance(fields, unicode) :
            fields = [ str(fields) ]
        elif not isinstance(fields, list) :
            fields = None
        filter = criteria.get(FILTER_KEY)
        if not isinstance(filter, dict) :
            filter = None

        # Case of data being a dict object at the top level.
        if isinstance(data, dict) :
            keylist = criteria.get('keys')
            if isinstance(keylist, str) or isinstance(keylist, unicode) :
                keylist = [ str(keylist) ]
            elif not isinstance(fields, list) :
                keylist = None
            if keylist == None :
                if filter == None and fields == None :
                    return data
                keylist = data.keys()
            retData = collections.OrderedDict({ })

            # We only have a list of keys to select on.
            if fields == None and filter == None :
                for onekey in keylist :
                    if onekey in data :
                        retData[onekey] = data[onekey]
                return retData

            # We also have a list of fields or a filter.
            for onekey in keylist :
                entry = data.get(onekey)
                if not isinstance(entry, dict) :
                    continue
                if filter != None :
                    ok = True
                    for filterkey in filter.keys() :
                        ok = entry.get(filterkey)==filter[filterkey]
                        if not ok :
                            break
                    if not ok :
                        continue
                if fields == None :
                    retData[onekey] = entry
                    continue
                newentry = collections.OrderedDict({ })
                for onefield in fields :
                    value = entry.get(onefield)
                    if value != None :
                        newentry[onefield] = entry[onefield]
                if len(newentry) > 0 :
                    retData[onekey] = newentry
            return retData

        # Case of data being a list object at the top level.
        if not isinstance(data, list) :
            return data
        if fields == None and filter == None :
            return data
        retData = [ ]
        for entry in data :
            if not isinstance(entry, dict) :
                continue
            if filter != None :
                ok = True
                for filterkey in filter.keys() :
                    ok = entry.get(filterkey)==filter[filterkey]
                    if not ok :
                        break
                if not ok :
                    continue
            if fields == None :
                retData.append(entry)
                continue
            newentry = collections.OrderedDict({ })
            for onefield in fields :
                value = entry.get(onefield)
                if value != None :
                    newentry[onefield] = entry[onefield]
            if len(newentry) > 0 :
                retData.append(newentry)
        return retData
            

    def getData(self, criteria):
        '''
        Retrieve data from an external source.
        @param criteria: Defines the filter (and perhaps routing information)
                         for retrieving the data.
        @return: The requested data.
        '''
        info = json.loads(criteria, object_pairs_hook=collections.OrderedDict)
        dataType = info.get(DATA_TYPE_KEY)
        if dataType in [SETTINGS_DATA, HAZARD_TYPES_DATA, HAZARD_CATEGORIES_DATA, \
                        HAZARD_METADATA, PRODUCT_DATA, \
                        STARTUP_CONFIG_DATA]:
            hazardServicesConfig = HazardServicesConfig(dataType)
            rawOut = hazardServicesConfig.getConfigData(criteria) or {}
            return json.dumps(rawOut)
        elif dataType in [HAZARD_METADATA_FILTER]:
        
            filter = info.get(FILTER_KEY) or {}
            phenomena, sig, subType = \
               filter.get(PHENOMENON), filter.get(SIGNIFICANCE), filter.get(SUBTYPE)
            return HazardMetaData.getMetaData( \
                      HAZARD_METADATA, phenomena, sig, subType)
        
        elif dataType in [CONFIG_DATA, VTEC_TABLE_DATA, VTEC_RECORDS_DATA, ALERTS_DATA, \
                          TEST_VTEC_RECORDS_DATA, VIEW_DEF_CONFIG_DATA, VIEW_CONFIG_DATA, \
                          VIEW_DEFAULT_DATA, HAZARD_INSTANCE_ALERT_CONFIG_DATA, \
                          ALERT_CONFIG_DATA]:   
            return self.DatabaseStorage.getData(criteria)

        elif dataType in [AREA_DICTIONARY_DATA, CITY_LOCATION_DATA, SITE_INFO_DATA] :
            repoEntry = self.caveEdexRepo.get(dataType)
            if repoEntry == None :
                oneLI = LocalizationInterface('')
                if dataType == SITE_INFO_DATA :
                    repoEntry = oneLI.getLocData( \
                        'textproducts/library/SiteInfo.py', 'EDEX', '', '')
                else :
                    repoEntry = oneLI.getLocData(
                        self.textUtilityRoot+str(dataType)+'.py', 'CAVE', '', '')
                if repoEntry == None :
                    msg = 'No data at all for type '+dataType
                    self.logger.error(msg)
                    repoEntry = {}
                self.caveEdexRepo[dataType] = repoEntry
            return json.dumps(self.returnFilteredData(repoEntry, info))
        elif dataType in [CALLS_TO_ACTIONS_DATA]:
            pass        
        elif dataType == EVENTS_DATA:
            settings = info.get(SETTINGS_DATA)
            if settings is not None:
                return self.getHazards(settings)
            # DSS.  None of the unit/functional tests ever hit this branch.
            # Does the GUI?
            else:
                return self.DatabaseStorage.getData(criteria)
        elif dataType == GFE_DATA:
            selectedTimeMS = info.get(SELECTED_TIME_MS)
            gridParm = info.get(GRID_PARAM_KEY)
            gfeAccessor = GFEAccessor(str(gridParm))
            gridDataArray = \
                   gfeAccessor.getGridDataForSelectedTime(selectedTimeMS)
            return gridDataArray 
    
###################################################################################
    ###  Helper methods
    
 
    def getHazards(self, settingsObject):
        '''
        Directly calls the DatabaseStorage to get hazards. 
        All of the hazardCategory and returnType==kml section 
        can likely be removed, as we no longer use kml files 
        for hazards
        @param settingsObject: The currently selected Hazard Services Settings
        @return: A list of events for the given settings. 
        '''

        hazardFilter = {}
        if type(settingsObject) in [types.DictType, collections.OrderedDict]:
            currentSettings = settingsObject
        else:
            # Get the Settings
            settingsID = str(settingsObject)
            criteria = {DATA_TYPE_KEY: SETTINGS_DATA, FILTER_KEY:{SETTINGS_ID:settingsID}}
            currentSettings = self.getData(json.dumps(criteria))
            currentSettings = json.loads(currentSettings) 
        self.addCriterium(currentSettings, VISIBLE_TYPES, hazardFilter)
        self.addCriterium(currentSettings, VISIBLE_STATES, hazardFilter)
        self.addCriterium(currentSettings, VISIBLE_SITES, hazardFilter)
        criteria = {DATA_TYPE_KEY:EVENTS_DATA, RETURN_TYPE_KEY:'json'}
        criteria[FILTER_KEY] = hazardFilter
        return self.getEvents(criteria)
    
    def getEvents(self, info):
        '''
        Retrieves events based on the filter passed in.
        @param info: The criteria to use to filter events.
        @return: The events  
        '''
    
        filter = info.get(FILTER_KEY)

        eventFilter = {}

        if filter is not None:
            eventFilter[PHENSIG] = filter.get(VISIBLE_TYPES)
            eventFilter[SITE_ID] = filter.get(VISIBLE_SITES)
            eventFilter[STATE] = filter.get(VISIBLE_STATES) 
        events = self.readEventsFromDB(eventFilter)    
        
        return json.dumps(events) 

    def readAllEventsFromDB(self):
        filter = {}
        events = self.readEventsFromDB(filter)
        return events
        
    def readEventsFromDB(self, filter):
        events = self.hazardEventPythonAdapter.getEventsByFilter(filter)
        return events
    
    def writeEventDB(self, eventDicts):
        self.hazardEventPythonAdapter.storeEvents(eventDicts)  
    
    def addCriterium(self, settings, key, filters):
        if settings.has_key(key):
            filters[key] = settings[key]

    def putHazards(self, hazardDicts, productCategory, site4id,
      currentTime=None, mergedRecords='separate', limitRecordTypeTo=None,
      format='xml', path=''):
        '''
        Stores hazard database information.  HazardDicts is a list of dictionaries
        describing current hazards.  ProductCategory is the product type, e.g.,
        WSW, and site4id is the 4-letter site id, such as KSLC.  
        '''
        #self._formatHeaders(format)
      
        # convert strings back into objects
        if type(hazardDicts) is types.StringType:
            exec('hazardDicts=' + hazardDicts)
        if currentTime is not None:
            currentTime = float(currentTime)
        if limitRecordTypeTo == 'None':
            limitRecordTypeTo = None

        # store all of the hazardDicts into the hazardDatabase. This will
        # overwrite ones with the same hazardSequence number (which is what
        # we want).
        
        # 'store' functionality copied from HazardMergerPurger
        if type(hazardDicts) is list:
            hazardDicts_dict = self._convertToDict(hazardDicts)
            
        #Get Current Hazards
        records = self.readAllEventsFromDB()

        # update the dictionary with new dictionary entries
        keys = hazardDicts_dict.keys()
        for key in keys:
            records[key] = hazardDicts_dict[key]

        # purge old records during our store operation
        records = self._purgeOldRecords(records)

        # write out the information back to disk
        return json.dumps({DATA_TYPE_KEY:EVENTS_DATA, EVENTDICTS_KEY: records, 'unlock': 'True'})
    
    def _convertToDict(self, listRecords):
        ''' 
        Converts the list into a dictionary of records.  This assumes that the
        the eventID becomes the 'key' and it also exists in the 
        value member of the input dictionary.  If the 'key' is not present,
        the error is silently ignored.
        '''
        retDict = {}
        for rec in listRecords:
            key = rec.get(EVENT_ID, None)
            if key is not None:
                retDict[key] = rec 
        return retDict

    def _purgeOldRecords(self, records, purgeTimeWindow=3*3600):
        '''
        The purgeOldRecords function deletes records from the dictionary that
        are 'old' and 'obsolete'.  Old is defined as records where the
        current time is past the ending time of a hazard (plus a buffer
        window defined as purgeTimeWindow in seconds).  Returns the 
        filtered set of records.  
        Note: since records is a dictionary, it may get changed within this 
        routine.  Records without the 'endTime' will remain in the dictionary.
        '''
        currentTime = time.time()
        keys = records.keys()
        for key in keys:
            endT = records[key].get(END_TIME, None)
            if endT is not None and (endT + purgeTimeWindow) < currentTime:
                del records[key]   #delete the record
        return records

    def getHazardTypes(self, hazardType=None):
        '''
        Returns hazard type meta information.
        @param hazardType: The hazard type to retrieve meta information for.
                           If this is not provided, then the entire Hazard Types
                           table is returned. 
        @return: The dictionary of hazard types or a specific hazard type entry 
        '''
        
        try:
           result = importModule(self.hazardTypesPath) 
           hazardTypes = result.HazardTypes
        except:
           traceback.print_exc()
           return
        
        if hazardType is not None:           
            return hazardTypes.get(hazardType)
        else:
            return hazardTypes
        
    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()
    
    
           
