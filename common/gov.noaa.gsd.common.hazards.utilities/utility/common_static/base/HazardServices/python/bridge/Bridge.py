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
 
''' 

import ast, types, time, traceback, os
import DatabaseStorage
import json

from HazardServicesConfig import HazardServicesConfig
import HazardMetaDataAccessor
from HazardConstants import *
from LocalizationInterface import LocalizationInterface
from HazardServicesPythonOverrider import importModule
import collections
# TODO: remove the traceback import when the deprecated function is removed
import traceback

# TODO: several of the putData functions supported filters. However,
# nothing in the current system was using the filters. A per data type
# filter capability can be implemented when it is needed.

# TODO: remove this function and all uses of it after the bridge.py
# re-factor is complete
def deprecated(func):
    """This is a decorator which can be used to mark functions
    as deprecated. It will result in a warning being emmitted
    when the function is used."""
    def newFunc(*args, **kwargs):        
        print 'Call to deprecated function %s.' % func.__name__
        traceback.print_stack()
        return func(*args, **kwargs)
    newFunc.__name__ = func.__name__
    newFunc.__doc__ = func.__doc__
    newFunc.__dict__.update(func.__dict__)
    return newFunc

try:
    import JUtil 
    from jep import *  
    import logging, UFStatusHandler
except:
    tbData = traceback.format_exc()
    print tbData

class Bridge:
    #
    # Localization path to HazardTypes.py
    hazardTypesPath = 'HazardServices/hazardTypes/HazardTypes.py'
    geoSpatialPath = 'HazardServices/python/events/productgen/geoSpatial'
    textUtilitiesPath = 'HazardServices/python/textUtilities'

    unusedArgument = "unusedArgument"

    def __init__(self):                       
        self.DatabaseStorage = DatabaseStorage.DatabaseStorage()
        
        self.logger = logging.getLogger('Bridge')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'Bridge', level=logging.INFO))
        self.logger.setLevel(logging.INFO)         
        
        self.textUtilityRoot = TEXT_UTILITY_ROOT
        self.siteCfgRoot = SITE_CFG_ROOT
        self.caveEdexRepo = {}

###################################################################################
    ### External Interface methods
        
    # Data interfaces -- Access to data in the NationalDatabase, AWIPS II, or external source
    #@deprecated
    def putData(self, criteria):
        '''
        Persist data in an external source.
        @param criteria: dataType, plus data
        '''
        info = json.loads(criteria, 'UTF-8')
        dataType = info.get(DATA_TYPE_KEY)
        if dataType in [SETTINGS_DATA, HAZARD_TYPES_DATA, HAZARD_CATEGORIES_DATA, PRODUCT_DATA,
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
    
    def getCityLocation(self, filter=None):
        cityLocation = self.caveEdexRepo.get('CityLocation')
        if cityLocation:
            return cityLocation
        
        cityLocation = self._getLocalizationData('CityLocation.py', self.textUtilityRoot, 'CAVE_STATIC')
        if cityLocation is None:
            msg = 'No data at all for type: CityLocation'
            self.logger.error(msg)
            return None
         
        self.caveEdexRepo['CityLocation'] = cityLocation.CityLocation    
        return cityLocation.CityLocation
    
    def getAreaDictionary(self, filter=None):
        areaDictionary = self.caveEdexRepo.get('AreaDictionary')
        if areaDictionary:
            return areaDictionary
        
        areaDictionary = self._getLocalizationData('AreaDictionary.py', self.textUtilityRoot, 'CAVE_STATIC')
        if areaDictionary is None:
            msg = 'No data at all for type: AreaDictionary'
            self.logger.error(msg)
            return None
            
        self.caveEdexRepo['AreaDictionary'] = areaDictionary.AreaDictionary
        return areaDictionary.AreaDictionary
    
    def getSiteInfo(self, filter=None):

        natl = {
         'region': 'CR',
         'fullStationID': 'KOAX',
         'wfoCityState': 'OMAHA/VALLEY NE',
         'wfoCity': 'OMAHA/VALLEY',
         'state': 'NEBRASKA',
         }
        
        siteInfo = self.caveEdexRepo.get('SiteInfo')
        if siteInfo:

            if NATIONAL not in siteInfo:
               siteInfo[NATIONAL] =  natl

            return siteInfo
        
        siteInfo = self._getLocalizationData('SiteCFG.py', self.siteCfgRoot, 'COMMON_STATIC')
           
        if siteInfo is None:
            msg = 'No data at all for type: SiteInfo'
            self.logger.error(msg)
            return None        

        if NATIONAL not in siteInfo.SiteInfo:
           siteInfo.SiteInfo[NATIONAL] =  natl
           
        self.caveEdexRepo['SiteInfo'] = siteInfo.SiteInfo
        return siteInfo.SiteInfo

    def getProductGeneratorTable(self, filter=None):
        hazardServicesConfig = HazardServicesConfig('productGeneratorTable')
        rawOut = hazardServicesConfig.getConfigData({}) or {}
        return rawOut

    def _getLocalizationData(self, fileName, directoryPath, locType):
        fullLocalizationPath = os.path.join(directoryPath, fileName)
        
        return importModule(fullLocalizationPath, locType)    

    def as_str(self, obj):
        if isinstance(obj, dict):
            return {self.as_str(key):self.as_str(value) for key,value in obj.items()}
        elif isinstance(obj, list):  
            return [self.as_str(value) for value in obj]
        elif isinstance(obj, set):  
            return {self.as_str(value) for value in obj}
        elif isinstance(obj, unicode):
            return obj.encode('utf-8')
        else:
            return obj

    #@deprecated
    def getData(self, criteria):
        '''
        Retrieve data from an external source.
        @param criteria: Defines the filter (and perhaps routing information)
                         for retrieving the data.
        @return: The requested data.
        '''
        info = json.loads(criteria, object_pairs_hook=collections.OrderedDict)
        info = self.as_str(info)
        dataType = info.get(DATA_TYPE_KEY)
        if dataType in [SETTINGS_DATA, HAZARD_TYPES_DATA, HAZARD_CATEGORIES_DATA, \
                        HAZARD_METADATA, PRODUCT_DATA, \
                        STARTUP_CONFIG_DATA]:
            hazardServicesConfig = HazardServicesConfig(dataType)
            rawOut = hazardServicesConfig.getConfigData(criteria) or {}
            return json.dumps(rawOut)
        elif dataType in [HAZARD_METADATA_FILTER]:
        
            filter = info.get(FILTER_KEY) or {}
            phenomena, sig, subType, site = \
               filter.get(PHENOMENON), filter.get(SIGNIFICANCE), filter.get(SUBTYPE), filter.get(SITE)
            return HazardMetaDataAccessor.getHazardMetaData( \
                      HAZARD_METADATA, phenomena, sig, subType=subType, site=site)
        elif dataType in [METADATA]:
            fileName = info.get(FILENAME_KEY)
            site = info.get(SITE, None)
            return HazardMetaDataAccessor.getMetaData(fileName, site=site)
        
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
        elif dataType == GFE_DATA:
            selectedTimeMS = info.get(SELECTED_TIME_MS)
            gridParm = info.get(GRID_PARAM_KEY)
            gfeAccessor = GFEAccessor(str(gridParm))
            gridDataArray = \
                   gfeAccessor.getGridDataForSelectedTime(selectedTimeMS)
            return gridDataArray 
    
###################################################################################
    ###  Helper methods
    
    def addCriterium(self, settings, key, filters):
        if settings.has_key(key):
            filters[key] = settings[key]

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

    def getDuplicateUGCs(self):
        name='DuplicateUGCs'
        criteria = {'filter':{'name':name}}
        hazardServicesConfig = HazardServicesConfig(configType=self.unusedArgument, configDir=self.geoSpatialPath)
        rawOut = hazardServicesConfig.getConfigData(criteria) or {}
        return rawOut


    def getSpatialQueries(self, filter=None):
        name = 'SpatialQueryConfigs'
        criteria = {'filter':{'name':name}}
        hazardServicesConfig = HazardServicesConfig(configType=self.unusedArgument, configDir=self.geoSpatialPath)
        rawOut = hazardServicesConfig.getConfigData(criteria) or {}
        return rawOut

    def getPathCastConfig(self, filter=None):
        name = 'PathcastConfig'
        criteria = {'filter':{'name':name}}
        hazardServicesConfig = HazardServicesConfig(configType=self.unusedArgument, configDir=self.geoSpatialPath)
        rawOut = hazardServicesConfig.getConfigData(criteria) or {}
        return rawOut

    def getDamMetaData(self, filter=None):
        name = 'DamMetaData'
        criteria = {'filter':{'name':name}}
        hazardServicesConfig = HazardServicesConfig(configType=self.unusedArgument, configDir=self.textUtilitiesPath)
        rawOut = hazardServicesConfig.getConfigData(criteria) or {}
        return rawOut

    def getBurnScarMetaData(self, filter=None):
        name = 'BurnScarMetaData'
        criteria = {'filter':{'name':name}}
        hazardServicesConfig = HazardServicesConfig(configType=self.unusedArgument, configDir=self.textUtilitiesPath)
        rawOut = hazardServicesConfig.getConfigData(criteria) or {}
        return rawOut

    def getHazardTypes(self, hazardType=None):
        '''
        Returns hazard type meta information.
        @param hazardType: The hazard type to retrieve meta information for.
                           If this is not provided, then the entire Hazard Types
                           table is returned. 
        @return: The dictionary of hazard types or a specific hazard type entry 
        '''
        hazardServicesConfig = HazardServicesConfig('hazardTypes')
        rawOut = hazardServicesConfig.getConfigData({}) or {}
        if hazardType:
            return rawOut.get(hazardType)
        return rawOut

    def getProductParts(self, productPart=None):
        '''
        Returns product part configuration information.
        @param productPart: The product part to retrieve config information for.
                            If this is not provided, then the entire Product Parts
                            configuration dictionary is returned. 
        @return: The dictionary of Product Parts or a specific Product Part entry 
        '''
        criteria = {'filter':{'name': 'ProductParts'}}
        hazardServicesConfig = HazardServicesConfig(configType=self.unusedArgument, configDir=self.textUtilitiesPath)
        rawOut = hazardServicesConfig.getConfigData(criteria) or {}
        return rawOut

    def getConfigFile(self, criteria):
        '''
        Retrieve data from an external source.
        @param criteria: Defines the filter (and perhaps routing information)
                         for retrieving the data.
        @return: The requested data.
        '''

        info = json.loads(criteria, object_pairs_hook=collections.OrderedDict)
        info = self.as_str(info)
        dataType = info.get(DATA_TYPE_KEY)
        configDir = info.get('configDir')
        hazardServicesConfig = HazardServicesConfig(dataType, configDir=configDir)
        rawOut = hazardServicesConfig.getConfigData(info) or {}
        return rawOut

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()
