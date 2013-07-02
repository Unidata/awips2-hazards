'''
    Description: Base Class for Product Generators holding common logic and process.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation
    July  1, 2013  648       Tracy.L.Hansen      Added CAP fields to dictionary
    July  8, 2013  784,1290  Tracy.L.Hansen      Added ProductParts and changes for ESF product generator
    
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
'''

try:
    import JUtil
except:
    pass

from Bridge import Bridge
from LocalizationInterface import *

from HazardServicesGenericHazards import HazardServicesGenericHazards
from TextProductCommon import TextProductCommon
from TextProductCommon import CallToActions
from ProductPart import ProductPart

import logging, UFStatusHandler
from MapInfo import MapInfo
from VTECEngineWrapper import VTECEngineWrapper

import collections, datetime
from pytz import timezone
import os, types, copy, sys, json
from QueryAfosToAwips import QueryAfosToAwips

try:
    from java.util import ArrayList
except:
    pass


class Product(object):
 
    def __init__(self):  
        '''
        General Note:  All times are in milliseconds unless designated 
        otherwise e.g. creationTime_secs.
        
        '''  
        self.initialize()
    
    def initialize(self):
        self._gh = HazardServicesGenericHazards()
        self._mapInfo = MapInfo()
    
        self.bridge = Bridge()
        
        self._vtecEngine = None
        self._productCategory = ''
        
        criteria = {'dataType' : 'AreaDictionary'}
        self._areaDictionary = json.loads(self.bridge.getData(json.dumps(criteria)))

        criteria = {'dataType' : 'CityLocation'}
        self._cityLocation = json.loads(self.bridge.getData(json.dumps(criteria)))
        criteria = {'dataType' : 'SiteInfo'}

        self._siteInfo = json.loads(self.bridge.getData(json.dumps(criteria)))
        self._cta = CallToActions()
        
        self._tpc = TextProductCommon()
        self._tpc.setUp(self._areaDictionary)
        
        # List of vtecEngineWrappers generated for these products
        #  Used at end to save vtec records if issueFlag is on
        self._wrappers = []
        
        self.logger = logging.getLogger('ProductGeneratorTemplate')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'com.raytheon.uf.common.hazards.productgen', 'ProductGeneratorTemplate', level=logging.INFO))
        self.logger.setLevel(logging.INFO)  
        
        # Default is True -- Products which are not VTEC can override and set to False
        self._vtecProduct = True       

    def getScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD developers'
        metadata['description'] = 'Base Class for Product Generators'
        metadata['version'] = '1.0'
        return metadata
       
    def defineDialog(self):
        '''
        @return: dialog definition to solicit user input before running tool
        '''  
        return {}
    
    def execute(self, hazardEventSet):          
        '''
        Must be overridden by the Product Generator
        '''
        pass
    
    def _productParts(self, productID):
        '''
        List of product parts in the order they appear in the product
        Order and defines the Product Parts for the given productID
        This is a generic product format, however, it can be overridden by the Product Generator.
        '''
        segmentParts = [
            ProductPart('ugcHeader'),
            ProductPart('vtecRecords'),
            ProductPart('areaString'),
            ProductPart('cityString'),
            ProductPart('issuanceTimeDate'),
            ProductPart('description'),
            ProductPart('callsToAction'),
            ProductPart('polygonText'),
            ProductPart('end'),                                    
            ]
        return [
            ProductPart('wmoHeader'),
            ProductPart('easMessage'),
            ProductPart('productHeader'),
            ProductPart('overview'),
            ProductPart('segments', productParts=segmentParts),
            ]
        
    def _getVariables(self, hazardEventSet): 
        '''
         Set up class variables
         
         Must convert Java object hazardEventSet to Python eventDicts
           NOTE: The Framework will be fixed to send a Python Hazard Event Set
           and then we will unpack the eventDicts and meta information
        '''  
        
        # Translate Hazard Event Set from Java to Python                
        iterator = hazardEventSet.iterator()
        eventList = ArrayList()
        while iterator.hasNext():
            event = iterator.next()
            eventList.add(event)    
        self._eventDicts = self.bridge.handleRecommenderResult('ProductGenerator', eventList, enclosed=False)                             
        attributes = hazardEventSet.getAttributes()
        metaDict = JUtil.javaMapToPyDict(attributes)                        

        self._issueFlag = metaDict.get('issueFlag')
        self._formats = metaDict.get('formats')        
        self._creationTime = int(metaDict.get('currentTime'))
        self._creationTime_secs = self._creationTime/1000
        self._siteID = metaDict.get('siteID')
        self._tpc.setSiteID(self._siteID)
        self._backupSiteID = metaDict.get('backupSiteID')
        inputFields = metaDict.get('inputFields')
        if not inputFields:
            inputFields = {}
        self._overviewHeadline = inputFields.get('overviewHeadline', '') 
                 
        self._sessionDict = metaDict.get('sessionDict')
        if not self._sessionDict :
            self._sessionDict = {}
        self._lineLength = 69
        self._upperCase = True
         
        # Set up issue time strings       
        self._ddhhmmTime = self._tpc.getFormattedTime(
              self._creationTime_secs, '%d%H%M', shiftToLocal=0, stripLeading=0).upper()
        self._timeLabel = self._tpc.getFormattedTime(
              self._creationTime_secs, '%I%M %p %Z %a %b %e %Y',
              shiftToLocal=1, stripLeading=1).upper()
              
        # These come from SiteInfo
        # Primary Site
        siteEntry = self._siteInfo.get(self._siteID)
        self._fullStationID = siteEntry.get('fullStationID')
        self._region = siteEntry.get('region')
        self._wfoCity = siteEntry.get('wfoCity')
        self._wfoCityState = siteEntry.get('wfoCityState')
        self._areaName = siteEntry.get('state')  #  'GEORGIA' 

        # Backup Site
        siteEntry = self._siteInfo.get(self._backupSiteID)        
        self._backupWfoCityState = siteEntry.get('wfoCityState')
        
    def _makeProducts_FromHazardEvents(self, eventDicts): 
        '''
        Make the products
        @param eventDicts: hazard events
        
        @return productDicts -- one dictionary for each product generated
            As input, Product Generators take a set of hazard events and produce 1 
            or more products.  For example, the FFA_ProductGenerator may take in 
            an areal FA.A and point FL.A which by policy must go into two separate 
            FFA products.  
            For each product, one dictionary is generated.  That dictionary serves 
            as input to multiple formatters to produce, for example, legacy ascii text 
            (Legacy formatter) and parter XML (XML formatter) formats for each product.

        '''
        methodName = 'ProductGeneratorTemplate:makeProducts_FromHazardEvents'
        
        # Determine the list of segments given the hazard events        
        segments = self._getSegments(eventDicts)
        self.logger.info(methodName + ' len(segments)='+str(len(segments)))
         
        # Determine the list of products and associated segments given the segments
        productSegmentGroups = self._groupSegments(segments)
        
        # Create each product and add to the list of productDicts
        productDicts = []                
        for productSegmentGroup in productSegmentGroups:
            
            productDict = collections.OrderedDict()
            # Contains information for all formats e.g.
            #   partner XML, CAP, and legacy text            
            self._productID =  productSegmentGroup.get('productID', 'NNN') 
            self._getProductInfo(self._siteID, self._productID)
            self._productName = productSegmentGroup.get('productName', 'Product Name')
            productDict = self._initializeProductDict()
            self._productTimeZones = []
                                            
            segments = productSegmentGroup.get('segments')
            
            # Product header
            self._preProcessProduct(productSegmentGroup, segments)
            
            # Create segments
            
            # Each entry in segmentEntries will represent one segment and
            #  be added to the productDict for formatting by the Legacy and XML
            #  formatters
            segmentEntries = []
            
            for segment in segments:  
                self._preProcessSegment(productSegmentGroup, segment)
                self._makeSegment(productSegmentGroup, segment)
                self._postProcessSegment(productSegmentGroup, segment)
                segmentEntries.append(self._createSegment(segment))
            segmentDict = collections.OrderedDict()
            segmentDict['segment'] = segmentEntries
            productDict['segments'] = segmentDict
            productDict = self._createSentTimes(productDict)
            
            # Product wrap-up
            self._postProcessProduct(productSegmentGroup, segments)  
            productDicts.append(productDict)
                        
        # If issuing, save the VTEC records for legacy products       
        self._saveVTEC() 
        #self.logger.info(self._productCategory + '::_makeProducts_FromHazardEvents returning: '+\
        #                       json.dumps(productDicts, indent=4))
        return productDicts
                                      
    def _getProductInfo(self, siteID, productID): 
        '''
         Get Product Info given siteID and product ID
         @param siteID: The site identifier, e.g. OAX
         @param productID: The product identifier, e.g. FFA
        '''
        #
        # Retrieve the record from the afos_to_awips table
        # for which the nnn and xxx portions of the afosid
        # correspond to the productID and siteID, respectively.
        a2a = QueryAfosToAwips(productID, siteID)        
        self._wmoID = a2a.getWMOprod() # e.g. WUUS53
        self._CCC = a2a.getCCC()  #e.g. OMA 
        
        # Product PIL, e.g. SVRTOP
        self._pil = productID + siteID

        # Product ID for transmitting to AWIPS WAN, e.g. KTOPSVRTOP  
        self._awipsWANPil = a2a.getAwipsWANpil()      
      
        # Product ID for storing to AWIPS text database, e.g. TOPSVRTOP  
        self._textdbPil = a2a.getTextDBpil() 
            
    def _getSegments(self, eventDicts):
        '''
        Determine the segments for the product
        @param eventDicts: list of Hazard Events
        @return a list of segments for the hazard events
        '''
        self._eventDicts = self.computeUGCsForEventDicts(eventDicts)
        self.getVtecEngine(self._eventDicts)        
        segments = self._vtecEngine.getSegments()
        return segments

    def _getSegments_ForPointsAndAreas(self, eventDicts): 
        '''
        Gets the segments for point hazards and areal hazards separately
        
        Sets variables 
            self._pointEvents, self._pointSegments
            self._areaEvents,  self._areaSegments
            
        @param eventDicts: list of Hazard Events
        @return a list of segments for the hazard events
                
        Separate the point events from the area event and create a separate VTEC engine for each.  
        This means we will put the area and point segments into different FFA products for now.
        However, if policy determines that they could be in one product, we can change that.
        
        For area events, segments look like this:
          [(frozenset(['FLZ051', 'FLZ048', 'FLZ049']), frozenset([1, 6])), 
           (frozenset(['FLZ030', 'FLZ031', 'FLZ032']), frozenset([2, 3,])]
         which indicates 2 segments:
        1 zones FLZ051, FLZ048, FLZ049,  comprised of eventIDs of 1 and 6
        2 zones FLZ030, FLZ031, FLZ032, comprised of eventIDs of 2 and 3        
        Zones are never repeated, but eventIDs may be repeated.
        
        For the point events, segments look like this:
        [(frozenset(['FLC049']), frozenset([26])), 
          (frozenset(['FLC049']), frozenset([25]))]           
        '''    
        self._pointEvents = []
        self._areaEvents = []
        self._pointSegments = []
        self._areaSegments  = []
        for eventDict in eventDicts:
            if eventDict.get('geoType') == 'point':
                self._pointEvents.append(eventDict)
            else:
                self._areaEvents.append(eventDict)
        for geoType in ('point', 'area'):
            if geoType == 'point' :     events = self._pointEvents
            else:                     events = self._areaEvents
            if not events: continue
            events = self.computeUGCsForEventDicts(events)
            self.getVtecEngine(events)
            segments = self._vtecEngine.getSegments()
            if geoType == 'point':  
                self._pointSegments = segments
                self._pointVtecEngine = self._vtecEngine
            else:                     
                self._areaSegments = segments
                self._areaVtecEngine = self._vtecEngine   
               
        return self._pointSegments + self._areaSegments
            
    def _groupSegments(self, segments):
        '''
        Group the segments into the products
        
        Since the rules for grouping segments into products differs across product
        types, this method must be implemented by the Product Generator class
        @param segments
        @return: productSegmentGroups
           where productSegmentGroup contains specific information for generating the product 
                 including a list of ordered segments included in the product
       '''
        return []

    def _preProcessProduct(self, productSegmentGroup, segments):
        '''
        Create the product header information 
        
        ***********
        Example segmented product:
        
           WGUS63 KBOU 080400
           FFABOU

           URGENT - IMMEDIATE BROADCAST REQUESTED

           FLOOD WATCH
           NATIONAL WEATHER SERVICE DENVER CO
           400 AM GMT TUE FEB 8 2011

           Overview Headline
           Overview

        ***********
        Example non-segmented product:
           WGUS63 KBOU 080400
           FFWBOU

        @productSegmentGroup: holds meta information about the product
        @segments: list of segments for this product
        '''
        methodName = 'ProductGeneratorTemplate:preProcessProduct'
        textStr = ''        
        # Unpack the productSegmentGroup
        self._productID = productSegmentGroup.get('productID')
        self._productName = productSegmentGroup.get('productName')
        self._geoType = productSegmentGroup.get('geoType')
        self._vtecEngine = productSegmentGroup.get('vtecEngine')
        self._mapType = productSegmentGroup.get('mapType')
        self._segmented = productSegmentGroup.get('segmented')
        self._formatPolygon = productSegmentGroup.get('formatPolygon')

        # Determine if EAS message should be generated
        useEAS = 0
        for segment in segments:
            vtecRecords = self.getVtecRecords(segment)
            for vtecRecord in vtecRecords:
                if self._useEAS(vtecRecord):
                    useEAS = 1
                    break

        # Gather pieces of the header
        self._textdbPil = self._CCC + self._productID + self._siteID
        self._awipsWANPil = self._fullStationID + self._productID + self._siteID
        self._pil = self._productID + self._siteID

        if self._areaName != '':
            self._areaName = ' FOR ' + self._areaName + '\n'
        if useEAS == 1:
            self._easActivationRequested= 'true'
            self._easPhrase = 'URGENT - IMMEDIATE BROADCAST REQUESTED\n'
        else:
            self._easActivationRequested = 'false'
            self._easPhrase = ''

        if self._backupSiteID != self._siteID:
            self._issuedByString = self.getIssuedByString()
        else :
            self._issuedByString = ''

        textStr += self._wmoID + ' ' + self._fullStationID + ' ' + \
               self._ddhhmmTime + '\n' + self._pil + '\n\n' 

        if self._segmented :
            self._productName = self.checkTestMode(self._sessionDict, self._productName + self._areaName)
            textStr += '\n' + self._easPhrase + '\n' + self._productName + '\n' + \
                   'NATIONAL WEATHER SERVICE ' + self._wfoCityState + \
                   '\n' + self._issuedByString + self._timeLabel + '\n\n'

        textStr = textStr + '|* DEFAULT OVERVIEW SECTION *|\n\n'
        # PV2: Overview information will be added in PV2
        # if self._overviewHeadline != '':
        #     textStr += self._overviewHeadline + '\n'
        # if self._overView != '':
        #     textStr += self._overview + '\n'
        return textStr
            
    def _preProcessSegment(self, productSegmentGroup, segment):
        '''
        Create the segment header information  
        
        *********
        Example segmented product:
                 
        COC003-080400-
        /0.NEW.KBOU.FL.A.0001.110208T0400Z-110208T0430Z/
        /00000.2.SM.110208T0400Z.110208T0755Z.110208T0755Z.NR/
        DOUGLAS CO-
        400 AM GMT TUE FEB 8 2011 
        
        **********
        Example non-segmented product:
        
        COC005-035-039-070730-
        /O.NEW.KBOU.FF.W.0004.120607T0425Z-120607T0730Z/
        /00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/

        BULLETIN - EAS ACTIVATION REQUESTED
        FLASH FLOOD WARNING
        NATIONAL WEATHER SERVICE DENVER CO
        1025 PM MDT WED JUN 6 2012

        '''       
        methodName = 'ProductGeneratorTemplate:preProcessSegment'
        textStr = ''
        self.metaDataList = self.getHazardMetaData(segment)
        # PV2: REFACTORING: There may be multiple (metaData, eventDict) pairs
        #   This will need to be accounted for in a loop such that all metadata for a segment is reported
        #   An example would be if we had a Frost Advisory and a Wind Advisory in one segment
        #   For short-fused, however, this should not happen
        try:
            self.metaData, self.segmentEventDict = self.metaDataList[0]
        except:
            self.logger.info(methodName + ' WARNING -- no Meta Data ' + str(self.metaDataList))        
            self.metaData = {}
            self.segmentEventDict = self._eventDicts[0]

        if self._geoType == 'area':
            self._ugcs = list(segment[0])
        else: # 'point'
            self._ugcs = self.segmentEventDict.get('ugcs', [])
            
        self._ugcs.sort()
        
        self._segmentVtecRecords = self.getVtecRecords(segment)
        self._headlines = []
        for vtecRecord in self._segmentVtecRecords:
            if vtecRecord['hdln'] not in self._headlines:
                self._headlines.append(vtecRecord['hdln'])
            
        self._timeZones = self._tpc.hazardTimeZones(self._ugcs)
        for tz in self._timeZones:
            if tz not in self._productTimeZones:
                self._productTimeZones.append(tz)
        self._expireTime = self._tpc.getExpireTime(
                    self._creationTime, self._purgeHours, 
                    self._segmentVtecRecords)
        
        self._ugcHeader = self._tpc.formatUGCs(self._ugcs, self._expireTime)
        textStr += self._ugcHeader + '\n'                
        # VTEC and HVTEC
        for vtecString in self._vtecEngine.getVTECString(segment):
            textStr += vtecString + '\n' 
        
        self._areaString = ''
        self._cityString = ''
        if self._segmented :
            if self._includeAreaNames:
                self._areaString = self._tpc.formatUGC_names(self._ugcs)
                textStr += self._areaString + '\n'
            if self._includeCityNames:
                self._cityString = 'INCLUDING THE CITIES OF ' + self._tpc.formatUGC_cities(self._ugcs)
                textStr += self._cityString + '\n'            
        else:
            textStr += '\n' + self._easPhrase + '\n' + \
                    self._productName + '\n' + \
                   'NATIONAL WEATHER SERVICE ' + self._wfoCityState + \
                   '\n' + self._issuedByString           
        # IssueTime 
        textStr += self._timeLabel
        textStr += '\n\n'             
        return textStr        

    def _makeSegment(self, productSegmentGroup, segment):
        '''
        Create the segment body -- Example:
                 
        ...FLOOD WATCH IN EFFECT LATE MONDAY NIGHT...
        THE NATIONAL WEATHER SERVICE IN DENVER HAS ISSUED A

        * FLOOD WATCH FOR A PORTION OF SOUTH CENTRAL COLORADO...INCLUDING 
          THE FOLLOWING COUNTY...ALAMOSA.

        * LATE MONDAY NIGHT

        * |* BASIS FOR THE WATCH *|

        * |* (OPTIONAL) POTENTIAL IMPACTS OF FLOODING *|

        '''       
        methodName = 'ProductGeneratorTemplate:makeSegment'
        textStr = ''        
                                      
        # Headlines for the segment 
        '''
        ...FLOOD WATCH IN EFFECT LATE MONDAY NIGHT...
        THE NATIONAL WEATHER SERVICE IN DENVER HAS ISSUED A
        '''
        
        textStr = textStr + self._tpc.getHeadlines(self._segmentVtecRecords, self._productID, self._creationTime_secs)
                
        replacedBy = self.segmentEventDict.get('replacedBy')
        replaces = self.segmentEventDict.get('replaces')
        if replacedBy:
            textStr = textStr + '...REPLACED BY ' + replacedBy + '...\n\n'
        elif replaces:
            textStr = textStr + '...REPLACES ' + replaces + '...\n\n'
        else:
            textStr = textStr + '\n'        
        #
        # This section generates the attribution statements and calls-to-action
        #
        attrPhrase = ''
        #
        # Check for special case where a CAN/EXP is paired with a
        # NEW/EXA/EXB/EXT
        #
        includeText, includeFrameCodes, skipCTAs, forceCTAList = \
          self._gh.useCaptureText(self._segmentVtecRecords)

        # sort the sections within the segment
        self._segmentVtecRecords.sort(self._tpc.sortSection)

        # find any 'CAN' with non-CAN for reasons of text capture
        canVtecRecord = None
        for vtecRecord in self._segmentVtecRecords:
            if vtecRecord['act'] in ['CAN','EXP','UPG']:
                canVtecRecord = vtecRecord
                break  #take the first one

        # Make Area Phrase
        areas = self._tpc.getGeneralAreaList(self._ugcs, areaDict = self._areaDictionary)
        self._areas = self._tpc.simplifyAreas(areas)

        areaPhrase = self._tpc.makeAreaPhrase(self._areas, self._geoType)
        areaPhraseShort = self._tpc.makeAreaPhrase(self._areas, self._geoType, True)
        
        # Process each part of the section
        testMode = self._sessionDict.get('testMode', 0)
        self._callsToAction = []
        for vtecRecord in self._segmentVtecRecords:
            if vtecRecord['act'] in ['CAN','EXP','UPG']:
                aPhrase = areaPhraseShort
                canVtecRecord = None
            else:
                aPhrase = areaPhrase
            eventID = vtecRecord.get('eventID')

            try:
                eventDict = self.getEventDicts(self._eventDicts, [eventID])[0]
            except:
                continue
            phrase = self.makeSection(vtecRecord, canVtecRecord, areaPhrase, self._geoType, eventDict, self.metaDataList,
                                           self._creationTime_secs, testMode, self._wfoCity)
            textStr = textStr + phrase

        self._description = textStr
       
        ctas = self.getMetadataItemForEvent(self.segmentEventDict, self.metaData,  'cta')
        if ctas is not None:
            textStr += '\nPRECAUTIONARY/PREPAREDNESS ACTIONS...\n'
            for cta in ctas :
                textStr += '\n'+(cta)

        self._polygonText = ''
        if self._formatPolygon:
            formattedPolygon = self.formatPolygonForEvent(self.segmentEventDict)
            textStr += formattedPolygon
            self._polygonText = formattedPolygon
            timeMotionLocationStr = self.formatTimeMotionLocationForEvent(self.segmentEventDict)
            if timeMotionLocationStr != None :
                textStr += timeMotionLocationStr
                
        return textStr

    def _postProcessSegment(self, productSegmentGroup, segment):                            
        return '\n&&\n'        

    def _postProcessProduct(self, productSegmentGroup, segments):   
        return '$$'  
              
    
    #### Utility methods
    def getVtecEngine(self, eventDicts) :
        '''
        Instantiates a VTEC Engine for the given eventDicts
        Note that more than one VTEC Engine may be instantiated for
        a product generator. For example, point and area eventDicts
        must have separate VTEC Engines.
        @param eventDicts -- list of hazard events
        '''
        testMode = self._sessionDict.get('testMode', 0)
        self._vtecEngineWrapper = VTECEngineWrapper(
               self.bridge, self._productCategory, self._fullStationID, 
               eventDicts, vtecMode='O', creationTime=self._creationTime_secs, 
               testHarnessMode=testMode, vtecProduct=self._vtecProduct)
        try :
            pass
        except :
            msg = 'Constructor for VTECEngineWrapper failed.'
            self.logger.info(msg)
        self._vtecEngine = self._vtecEngineWrapper.engine()
        self._wrappers.append(self._vtecEngineWrapper)
        
    def getVtecRecords(self, segment, vtecEngine=None):
        if not vtecEngine:
            vtecEngine = self._vtecEngine
        return vtecEngine.getVtecRecords(segment)
        vtecRecords = copy.deepcopy(vtecEngine.getVtecRecords(segment))
        # Change times to milliseconds from seconds
        for vtecRecord in vtecRecords:
            for key in ['startTime', 'endTime', 'riseAbove', 'crest', 'fallBelow', 'issueTime']:
                value = vtecRecord.get(key)
                if value:
                    vtecRecord[key] = value * 1000
        return vtecRecords

    def getIssuedByString(self, words = 'ISSUED BY NATIONAL WEATHER SERVICE '):
        '''
        If there is a backup site, then add an 'issued by' string
        '''
        issuedByString = words + self._backupWfoCityState + '\n'
        return issuedByString
    
    def getCityInfo(self, ugcs, returnType='string'):
        '''
        @param ugcs -- list of ugc codes
        @param returnType -- can be list or string
        @return list or string of cities within the ugc areas
        '''
        cities = ''
        cityList = []
        for ugc in ugcs:
            ugcCities = self._cityLocation.get(ugc)
            if not ugcCities: return ''
            for city in ugcCities:
                cities += city + '...'
                cityList.append((city, ugcCities.get(city)))
        if returnType == 'string':
            return cities
        else:
            return cityList            

    def computeUGCsForEventDicts(self, eventDicts):
        '''
        For each hazard event, determine the set of ugcs
        For area events, the polygon will be used to map to ugcs
        For point events, the lat/lon of the point will be used
        @param eventDicts -- list of hazard events
        @return newEventDicts -- list of augmented hazard events with 
            entries added for the ugcs
            NOTE that these augmentations are local to the product 
            generator and do not propagate back to Hazard Services
        '''
        newEventDicts = []
        for eDict in eventDicts:
            # eDict['geoType'] = 'area' or 'point'
            eDict['siteID'] = self._fullStationID

            if eDict.get('geoType') == 'area':
                polygons = self._extractPolygons(eDict)
                ugcs = self._mapInfo.getUGCsMatchPolygons(self._areaUgcType, 
                                polygons, siteID=self._siteID)
                newUgcs = []
                for ugc in ugcs:
                    if ugc in newUgcs: continue
                    newUgcs.append(ugc)
                    
                eDict['ugcs'] = newUgcs
                eDict['shapeType'] = 'polygon'
            else:
                ugcs = []
                #  For points, convert a lat/lon to a UGC
                #  forecastPoint': {'id': 'DCTN1', 'name': 'Decatur', 
                #   'point': ['-96.2413888888889', '42.0072222222222']}
                forecastPoint = eDict.get('forecastPoint')
                lon, lat = forecastPoint.get('point')
                ugcs = self._mapInfo.getUGCsMatchPolygons(self._pointUgcType, 
                            [[(float(lon),float(lat))]], siteID=self._siteID)
                if not ugcs:
                    # TODO Preventing a crash, but need to handle better 
                    ugcs = ['COC003']
                eDict['ugcs'] = ugcs           
        
            #  Handle 'previewState' 
            #  IF we are previewing an 'ended' state, set the state as 'ended'
            #   so that the VTEC processing will be done correctly
            if eDict.get('previewState') == 'ended':
                eDict['state'] = 'ended'
            newEventDicts.append(eDict)
   
        return newEventDicts
    
    def _useEAS(self, vtecRecord):
        '''
        Return True if we should use an EAS phrase
        @param vtec record
        @return boolean 
        '''
        if (vtecRecord['act'] in ['NEW', 'EXA', 'EXB', 'EXT'] and
            (vtecRecord['phen'] == 'FF' or vtecRecord['phen'] == 'FA') or vtecRecord['phen'] == 'FL'):
            return True
        else:
            return False

    def getHazardMetaData(self, segment) :
        '''
        @param: eventInfo
        ''' 
        # Get meta data for this segment
        #  May need to get multiple eventDicts and meta data
        metaDataList = []
        self._segmentEventDicts = self.getSegmentEventDicts(self._eventDicts, [segment])
        for eventDict in self._segmentEventDicts:
            phen = eventDict.get('phen', '')   
            sig = eventDict.get('sig', '')   
            subType = eventDict.get('subType', '')   
            criteria = {'dataType':'hazardMetaData_filter', 
                    'filter':{'phen':phen, 'sig':sig,'subType':subType}
                    }
            metaData = self.bridge.getData(json.dumps(criteria))
            metaDataList.append((metaData, eventDict))

        return metaDataList
        
    def _saveVTEC(self):
        '''
        if issuing: 
            For each VTEC Engine generated in the product, save the vtec records 
        '''        
        if str(self._issueFlag) == 'True':
            for wrapper in self._wrappers:
                wrapper.mergeResults() 
            self.logger.info(self._productCategory +' Saving VTEC')
                    
    def checkTestMode(self, sessionDict, str):
        # testMode is set, then we are in product test mode.
        # modify the str to have beginning and ending TEST indication.
        if sessionDict.get('testMode', 0):
            return 'TEST...'+str+'...TEST'
        elif sessionDict.get('experimentalMode', 0):
            return 'EXPERIMENTAL...' + str
        else:
            return str               
                            
    def getSegmentEventDicts(self, inputEventDicts, segments):
        # Return the eventDicts for the segments -
        #  Each segment lists eventIDs, so collect those and they use
        #  getEventDicts to get the eventDicts.
        eventIDs = []
        for segment in segments:
            ugcs, ids = segment
            eventIDs += ids
        return self.getEventDicts(inputEventDicts, eventIDs)
    
    def getEventDicts(self, inputEventDicts, eventIDs):
        '''
        @param inputEventDicts: Set of eventDicts 
        @param eventIDs: The ids of the eventDicts to retrieve from the input set
        @return: Return a list of eventDicts
        '''
        eventDicts = []
        for eventDict in inputEventDicts:
            if eventDict.get('eventID') in eventIDs:
                eventDicts.append(eventDict)
        return eventDicts

    def getMetadataItemForEvent(self, eventDict, metaData, fieldName) :
        methodName = 'ProductGeneratorTemplate:getMetadataItemForEvent'
        
        # Find the field entry in the eventDict
        # Giving us the Hazard Information Dialog entry given by the user
        entry = eventDict.get(fieldName)
        if entry is None :
            self.logger.info('Item '+fieldName+' not in event dict')
            return None
        if not isinstance(metaData, list) :
            self.logger.info('No metadata available')
            return None
        
        # Find the choices in the meta data associated with this fieldName
        metaChoices = None
        for metaEntry in metaData :
            if metaEntry.get('fieldName') == fieldName :
                metaChoices = metaEntry.get('choices')
                if metaChoices != None :
                    break
        if metaChoices is None :
            self.logger.info('Item '+fieldName+' not in event metadata')
            return None
        
        # Handle case of single valued entry e.g. radio button
        if not isinstance(entry, list) :
            for choice in metaChoices :
                if choice.get('displayString')==entry :
                    return choice.get('productString')
            return None
        # Handle case of list e.g. check list
        productStrings = []
        for choice in metaChoices :
            if not choice.get('displayString') in entry:
                continue
            metaStr = choice.get('productString')
            if not metaStr or len(metaStr)==0 :
                # Use displayString if productString not specified
                metaStr = choice.get('displayString')
            productStrings.append(metaStr)
        if len(productStrings)==0 :
            return None
        return productStrings
    
    
    #### 
    ## Make Section 

    def makeSection(self, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69):
        '''
         Creates a section of the product.  
        @param vtecRecord: The vtec record is passed in.  
        @param canVtecRecord: any associated CAN/EXP/UPG vtecRecord, 
        @param areaPhrase: area description for the segment.
        @param eventType: 'area' or 'point'
        @param eventDict:  Hazard Event information
        @param metaDataList: List of meta data for the section
        @param creationTime: in MS
        @param testMode: 0 or 1
        @param wfoCity
        @param lineLength
        
        @return ascii text version of section
        '''        
        attribution, headPhrase, attributionPhrase = self.getAttributionPhrase(vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69)
        hazardTimePhrases = self.getHazardTimePhrases(vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69)
        if eventType == 'point':
            pointPhrase = self.getPointPhrase(vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69)
        else:
            pointPhrase = ''
        metaDataPhrase = self.getMetaDataPhrase(attribution, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69)
                
        if headPhrase:
            attrPhrase = attributionPhrase + hazardTimePhrases + pointPhrase + metaDataPhrase
        #no headPhrase (EXP or CAN alone)
        else:
            attrPhrase = attribution

        return attrPhrase  
        
    def getAttributionPhrase(self, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69):
        '''
        THE NATIONAL WEATHER SERVICE IN DENVER HAS ISSUED A

        * AREAL FLOOD WATCH FOR A PORTION OF SOUTH CENTRAL COLORADO...
          INCLUDING THE FOLLOWING COUNTY...ALAMOSA.
        '''
        nwsPhrase = 'THE NATIONAL WEATHER SERVICE IN ' + wfoCity + ' HAS '

        #
        # Attribution and 1st bullet (headPhrase)
        #
        headPhrase = None
        attribution = ''

        hazName = self._tpc.hazardName(vtecRecord['hdln'], testMode, False)
        
        if len(vtecRecord['hdln']):
            if vtecRecord['act'] == 'NEW':
                attribution = nwsPhrase + 'ISSUED A'
                headPhrase =  '* ' + hazName + ' FOR ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'CON':
                attribution = 'THE ' + hazName + ' CONTINUES FOR'
                headPhrase =  '* ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'EXA':
                attribution = nwsPhrase + 'EXPANDED THE'
                headPhrase =  '* ' + hazName + ' TO INCLUDE ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'EXT':
                attribution = 'THE ' + hazName + ' IS NOW IN EFFECT FOR' 
                headPhrase = '* ' + areaPhrase + '.'
                    
            elif vtecRecord['act'] == 'EXB':
                attribution = nwsPhrase + 'EXPANDED THE'
                headPhrase =  '* ' + hazName + ' TO INCLUDE ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'CAN':
                attribution = 'THE ' + hazName + \
                   ' FOR ' + areaPhrase + ' HAS BEEN CANCELLED. ' + \
                   '|* BRIEF POST-SYNOPSIS/SUMMARY OF HYDROMET ACTIVITY *|\n\n'
    
            elif vtecRecord['act'] == 'EXP':
                expTimeCurrent = creationTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'THE ' + hazName + \
                      ' FOR ' + areaPhrase + ' HAS EXPIRED. ' + \
                      '|* BRIEF POST-SYNOPSIS/SUMMARY OF HYDROMET ACTIVITY *|'
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, expTimeCurrent)
                   attribution = 'THE ' + hazName + \
                      ' FOR ' + areaPhrase + ' WILL EXPIRE ' + timeWords + \
                      '. ' + \
                      '|* BRIEF POST-SYNOPSIS/SUMMARY OF HYDROMET ACTIVITY *|'

        if headPhrase is not None:
            headPhrase = self._tpc.indentText(headPhrase, indentFirstString = '',
              indentNextString = '  ', maxWidth=lineLength,
              breakStrings=[' ', '-', '...']) +'\n'
        else:
            headPhrase = ''

        return attribution, headPhrase, attribution + '\n\n' + headPhrase
    
    def getHazardTimePhrases(self, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69 ):
        '''
        LATE MONDAY NIGHT
        '''
        endTimePhrase = self._tpc.hazardTimePhrases(vtecRecord, creationTime, prefixSpace=False)
        endTimePhrase = self._tpc.substituteBulletedText(endTimePhrase,
                'TIME IS MISSING', 'DefaultOnly', lineLength)
        return endTimePhrase
    
    def getPointPhrase(self, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69 ):
        # Add in the point information                
        '''
        * AT 4:00 AM TUESDAY THE STAGE WAS 32.2 FEET
        * MODERATE FLOODING IS POSSIBLE. 
        * FLOOD STAGE IS 35.0FEET 
        * FORECAST...FLOOD STAGE MAY BE REACHED BY TUESDAY AM
        '''                
        stageTime = eventDict.get('startTime') # Use start time for now -- '8:45 AM Monday'
        timeOfStage =  self._tpc.getFormattedTime(stageTime/1000, '%I:%M %p %A', shiftToLocal=1, stripLeading=1).upper() 
        currentStage = eventDict.get('currentStage')
        if currentStage is not None:
            stageHeight = `currentStage` + ' feet'
            stagePhrase = '* At '+timeOfStage+' the stage was '+stageHeight + '\n'
        else:
            stagePhrase = ''
                
        metaData = metaDataList[0]
        severity = self.getMetaDataValue(metaData, 'floodSeverity', eventDict.get('floodSeverity'))
        if severity is not None:
            if severity != '':
                severity = severity+' '
            severityPhrase = '* '+severity + 'Flooding is possible. \n'
        else:
            severityPhrase = ''
                    
        floodStage = eventDict.get('floodStage')
        if floodStage is not None:
            floodStage = `floodStage`
            floodStagePhrase = '* Flood stage is '+floodStage + 'feet \n'
        else:
            floodStagePhrase = ''
                
        crest = eventDict.get('crest')
        if crest is not None:
            crestTime = self._tpc.getFormattedTime(crest/1000, '%A %p', shiftToLocal=1, stripLeading=1).upper() #'Monday Morning'
            crestPhrase = '* Forecast...Flood stage may be reached by '+crestTime+ '\n'
        else:
            crestPhrase = ''                               
        pointPhrase = stagePhrase + severityPhrase + floodStagePhrase + crestPhrase + '\n'
        return pointPhrase
    
    def getMetaDataPhrase(self, attribution, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69 ):
        
        basisPhrase, multRecords, impact, remainder = self.getBasisPhrase(vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69)
        if vtecRecord['act'] not in ['CAN', 'EXP']:
            impactsPhrase = self.getImpactsPhrase(multRecords, impact, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69)
            ctaBodyPhrase = self.getCTAsPhrase(attribution, remainder, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69)
        else:
            impactsPhrase = ''
            ctaBodyPhrase = ''
        
        return basisPhrase + '\n' + impactsPhrase + '\n'
        
    def getBasisPhrase(self, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69 ):
        # Basis bullet
        if vtecRecord['act'] == 'NEW' and canVtecRecord:
            capText = canVtecRecord.get('prevText', None)
        else:
            capText = vtecRecord.get('prevText', None)
        (haz, timeB, basis, impact, remainder, multRecords) = \
            self._tpc.decodeBulletedText(capText)

        defaultBasis = {
            'NEW': ('BASIS FOR THE WATCH', 'Always'),
            'CON': ('DESCRIBE CURRENT SITUATION', 'DefaultOnly'),
            'EXT': ('BASIS FOR EXTENDING THE WATCH', 'DefaultOnly'),
            'EXB': ('BASIS FOR EXPANSION OF THE WATCH', 'DefaultOnly'),
            'EXA': ('BASIS FOR EXPANSION OF THE WATCH', 'DefaultOnly'),
            'CAN': ('BASIS FOR CANCELLATION OF THE WATCH', 'DefaultOnly'),
            }
        b = defaultBasis[vtecRecord['act']]
        if multRecords == 0:
            basisPhrase = self._tpc.substituteBulletedText(basis, b[0], b[1], lineLength)
        else:
            basisPhrase = self._tpc.substituteBulletedText(basis, b[0], 'Always', lineLength)
        return basisPhrase, multRecords, impact, remainder
    
    def getImpactsPhrase(self, multRecords, impact, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69):       
        # Impacts bullet
        if (vtecRecord['act'] == 'NEW' and canVtecRecord) or multRecords:
            framing = 'Always'
        else:
            framing = 'DefaultOnly'
        impactsPhrase = self._tpc.substituteBulletedText(impact,
            '(OPTIONAL) POTENTIAL IMPACTS OF FLOODING', framing, lineLength)
        
        return impactsPhrase
    
    def getCTAsPhrase(self, attribution, remainder, vtecRecord, canVtecRecord, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69):

        # CTA's -- remainder of text
        general = ''
        addCTA = False
        ctaBodyPhrase = ''
        if remainder is not None and \
            (canVtecRecord or vtecRecord['act'] != 'NEW'):
            if canVtecRecord is None:
                general = remainder  #use all
            else:
                #frame the text, without the ctas
                addCTA = True
                paras = self._tpc.convertSingleParas(remainder)
                for p in paras:
                    found = self.ctasFound(p)
                    if len(found) == 0:
                        general = general + p + '\n\n'
                if len(general):
                    general = '|* ' + general[:-2]  + ' *|\n\n'    
        else:
            addCTA = True

        # add in call to actions
        
        # Find Calls to Action in Meta Data
        choice = []
        for metaData, eventDict in metaDataList:
            for entry in metaData:
                if entry.get('fieldName') == 'cta':
                    choices = entry.get('choices')
                    break
        if addCTA:
            ctas = eventDict.get('cta')
            if ctas is not None:
                for cta in ctas:
                    for choice in choices:
                        if choice.get('displayString') == cta or choice.get('identifier') == cta:
                            productString = choice.get('productString', choice.get('displayString'))
                            self._callsToAction.append(productString+'\n')
        else:
            cta = ''

        if len(self._callsToAction) > 1:
            ctaBodyPhrase ='\n\nPRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n' + \
            cta + '\n\n&&\n\n'
        else:
            ctaBodyPhrase = ''
        
        if ctaBodyPhrase.find('PRECAUTIONARY/PREPAREDNESS ACTIONS...') != -1 and  \
            attribution.find('&&') != -1:
                attribution = attribution.replace('&&','')
                ctaBodyPhrase = ctaBodyPhrase.replace('PRECAUTIONARY/PREPAREDNESS ACTIONS...','')
                
        return general + ctaBodyPhrase

    def getMetaDataValue(self, metaData, fieldName, value):
        '''
        Translates the entries from the Hazard Information Dialog into product strings.
        @param  meta data dictionary specifying information typically entered through the Hazard Information Dialog
        @param  a key field in the dictionary
        @param a user entered value for the given field
        @return the associated productString.  If a productString is not given, return the displayString.
        
        For example, In the Hazard Information Dialog, there is a field for specifying the Flood Severity.  
        This is specified in the Meta Data for certain flood type hazards.  The key field would be 
        'immediateCause', the user-entered value might be 'ER (Excessive Rainfall)' and the productString 
        returned would then be 'ER'.
        '''
        metaData = metaData[0]
        for widget in metaData:
            if widget.get('fieldName') == fieldName:
                for choice in widget.get('choices'):
                    if choice.get('identifier') == value or choice.get('displayString') == value:
                        returnVal = choice.get('productString')
                        if returnVal is None:
                            returnVal = choice.get('displayString')
                        return returnVal
 



    def getCountyInfoForEvent(self, eventDict) :
        ''' 
        Returns list of tuples:        
        From AreaDictionary:
        ( ugcName, portion, PARISH/COUNTY, fullStateName, stateAbbr, partOfState)
        '''
        countyList = []
        for ugc in self._ugcs:
            ugcEntry = self._areaDictionary.get(ugc)
            if ugcEntry is None:
                continue
            ugcName = ugcEntry.get('ugcName', '')
            portion = ''
            if ugc[:2] == 'LA':
                equiv = 'PARISH'
            else:
                equiv = 'COUNTY'
            fullState = ugcEntry.get('fullStateName')
            stateAbbrev = ugcEntry.get('stateAbbr')
            partOfState = ugcEntry.get('partOfState')
            countyList.append((ugcName, portion, equiv, fullState, stateAbbrev, partOfState))
        return countyList
            

    #-----------------------------------------------------
    #-----------------------------------------------------
    def processTimeForGetFormattedTime(self, timeArg):
        '''
        If timeArg is in milliseconds, return seconds
        '''
        if timeArg==None :
            return None
        if isinstance(timeArg, str) or isinstance(timeArg, unicode) :
            timeArg = long(timeArg)
        if timeArg > 100000000000:
            timeArg = timeArg / 1000
        return timeArg 

    def processTime(self, timeArg):
        '''
        If timeArg is in seconds, return milliseconds
        '''
        if timeArg==None :
            return None
        if isinstance(timeArg, str) or isinstance(timeArg, unicode) :
            timeArg = long(timeArg)
        if timeArg < 100000000000:
            timeArg = timeArg * 1000
        return timeArg 
            

    ######################################
    ### Product Dictionary        
    ######################################

    def _initializeProductDict(self):
        productDict = collections.OrderedDict()
        productDict['disclaimer'] = 'This XML wrapped text product should be considered COMPLETELY EXPERIMENTAL. The National Weather Service currently makes NO GUARANTEE WHATSOEVER that this product will continue to be supplied without interruption. The format of this product MAY CHANGE AT ANY TIME without notice.'
        productDict['senderName'] = 'NATIONAL WEATHER SERVICE ' + self._wfoCityState
        productDict['productName'] = self._productName
        productDict['productID'] = self._productID
        productDict['productParts'] = self._productParts(self._productID)

        # wmoHeader
        headerDict = collections.OrderedDict()
        headerDict['TTAAii'] = self._wmoID
        headerDict['originatingOffice'] = self._fullStationID
        headerDict['productID'] = self._productID
        headerDict['siteID'] = self._siteID
        headerDict['wmoHeaderLine'] = self._wmoID + ' ' + self._fullStationID + ' ' + self._ddhhmmTime
        headerDict['awipsIdentifierLine'] = self._productID + self._siteID
        productDict['wmoHeader'] = headerDict
        # These fields will be solicited from the user in PV2
        productDict['overview'] = ''
        productDict['synopsis'] = ''
        return productDict

    def _createSentTimes(self, productDict):    
        productDict['easActivationRequested'] = self._easActivationRequested  # Set in preProcessProduct
        productDict['sentTimeZ'] = self._convertToISO(self._creationTime)
        productDict['sentTimeZ_datetime'] = self._convertToDatetime(self._creationTime)
        productDict['sentTimeLocal'] = self._convertToISO(self._creationTime, local=True)
        productDict['timeZones'] = self._productTimeZones
        self._addToProductDict(productDict)
        return productDict
    
        
    def _createSegment(self, segment):
        segmentEntry = collections.OrderedDict()
        segmentEntry['description:editable'] = self._description  # Set in makeSegment
        segmentEntry['ugcCodes'] = self._formatUGC_entries(segment)
        segmentEntry['ugcHeader'] = self._ugcHeader
        segmentEntry['areaString'] = self._areaString
        segmentEntry['cityString'] = self._cityString
        segmentEntry['areaType'] = self._geoType
        segmentEntry['expireTime'] = self._convertToISO(self._expireTime)
        segmentEntry['expireTime_datetime'] = self._convertToDatetime(self._expireTime)
        segmentEntry['vtecRecords'] = self._vtecRecordEntries(segment)
        segmentEntry['headlines'] = self._headlines
        segmentEntry['polygons'] = self._createPolygonEntries()
        segmentEntry['timeMotionLocation'] = self._createTimeMotionLocationEntry()
        segmentEntry['impactedLocations'] = self._createImpactedLocationEntries(segment)
        segmentEntry['observations'] = collections.OrderedDict()   # We do not have this sort of information yet
        callsToAction = collections.OrderedDict()
        callsToAction['callToAction'] = self._callsToAction # Set in makeSegment, makeSection
        segmentEntry['callsToAction'] = callsToAction
        segmentEntry['polygonText'] = self._polygonText
        segmentEntry['timeZones'] = self._timeZones   
        
        # CAP Specific Fields -- must skip for Partner XML        
        segmentEntry['status']= 'Actual' 
        segmentEntry['CAP_areaString'] = self._tpc.formatUGC_namesWithState(self._ugcs, separator='; ') 
        infoDict = collections.OrderedDict()
        segmentEntry['info'] = [infoDict]
        infoDict['category'] = 'Met'
        infoDict['responseType']=self.segmentEventDict.get('responseType', '') # 'Avoid'
        infoDict['urgency']=self.segmentEventDict.get('urgency', '') # 'Immediate'
        infoDict['severity']=self.segmentEventDict.get('severity', '') #'Severe' 
        infoDict['certainty']=self.segmentEventDict.get('certainty', '') # 'Observed'
        infoDict['onset_datetime']=self._convertToDatetime(self.segmentEventDict.get('startTime', '')) 
        infoDict['WEA_text']=self.segmentEventDict.get('WEA_Text', '') # 'Observed'
        infoDict['pil'] = self._pil
        infoDict['sentBy'] = self._wfoCity
        infoDict['event'] = self.segmentEventDict.get('headline')
        endTime = self.segmentEventDict.get('endTime') 
        if endTime: 
            infoDict['eventEndingTime_datetime'] = self._convertToDatetime(endTime)  
        self._addToSegmentEntry(segmentEntry)      
        return segmentEntry
       
    def _addToProductDict(self, productDict):
        '''
        This method can be overridden by the Product Generators to add specific product information to the productDict
        '''
        pass
    
    def _addToSegmentEntry(self, segmentEntry):
        '''
        This method can be overridden by the Product Generators to add specific product information to the segmentEntry
        '''
        pass
    
    def _formatUGC_entries(self, segment):
        ugcDict = collections.OrderedDict()
        ugcCodeList = []
        for ugc in self._ugcs:
            areaDictEntry = self._areaDictionary.get(ugc)
            if areaDictEntry is None:
                # We are not localized correctly for the hazard
                # So get the first dictionary entry
                self.logger.info('Not Localized for the hazard area -- ugc' + ugc)
                keys = self._areaDictionary.keys()
                areaDictEntry = self._areaDictionary.get(keys[0])
            ugcEntry = collections.OrderedDict()
            ugcEntry['state'] = areaDictEntry.get('stateAbbr')
            ugcEntry['type'] = self._getUgcInfo(ugc, 'type')
            ugcEntry['number'] = self._getUgcInfo(ugc, 'number')
            ugcEntry['text'] = ugc
            ugcEntry['subArea'] = ''
            ugcDict[ugc] = ugcEntry
            ugcCodeList.append(ugcEntry)
        ugcCodes = collections.OrderedDict()
        ugcCodes['ugcCode'] = ugcCodeList
        return ugcCodes
    
    def _vtecRecordEntries(self, segment):
        '''
        @param segment -- current segment
        @return list of Ordered Dictionaries each representing a vtec or hvtec string
                for the segment
        '''
        vtecRecords = []        
        for vtecString in self._vtecEngine.getVTECString(segment):
            vtecString = vtecString.strip('/')
            parts = vtecString.split('.')
            vtecString = '/'+vtecString+'/'
            vtecDict = collections.OrderedDict()
            if len(parts[0]) > 1:  
                vtecDict['vtecRecordType'] = 'hvtecRecordType'
                vtecDict['name']= 'hvtecRecord'
                nwsli, floodSeverity, immediateCause, floodBeginTimeVTEC, floodCrestTimeVTEC, floodEndTimeVTEC, floodRecordStatus = parts
                vtecDict['nwsli'] = nwsli
                vtecDict['floodSeverity'] = floodSeverity
                vtecDict['immediateCause'] = immediateCause
                vtecDict['floodBeginTimeVTEC'] = floodBeginTimeVTEC
                vtecDict['floodCrestTimeVTEC'] = floodCrestTimeVTEC
                vtecDict['floodEndTimeVTEC'] = floodEndTimeVTEC
                vtecDict['floodBeginTime'] = self._getIsoTime(floodBeginTimeVTEC)
                vtecDict['floodCrestTime'] = self._getIsoTime(floodCrestTimeVTEC)
                vtecDict['floodEndTime'] = self._getIsoTime(floodEndTimeVTEC)
                vtecDict['floodRecordStatus'] = floodRecordStatus
                vtecDict['vtecString'] = vtecString
            else:
                vtecDict = collections.OrderedDict()
                vtecDict['vtecRecordType'] = 'pvtecRecord'
                vtecDict['name']= 'pvtecRecord'
                productClass, action, site, phenomenon, significance, eventTrackingNumber, timeVTEC = parts
                startTimeVTEC, endTimeVTEC = timeVTEC.split('-')
                vtecDict['productClass'] = productClass
                vtecDict['action'] = action
                vtecDict['site'] = site
                vtecDict['phenomenon'] = phenomenon
                vtecDict['significance'] = significance
                vtecDict['eventTrackingNumber'] = eventTrackingNumber
                vtecDict['startTimeVTEC'] = startTimeVTEC
                vtecDict['startTime'] = self._getIsoTime(startTimeVTEC)
                vtecDict['endTimeVTEC'] = endTimeVTEC
                vtecDict['endTime'] = self._getIsoTime(endTimeVTEC)
                vtecDict['vtecString'] = vtecString
            
            vtecRecords.append(vtecDict)            
        return vtecRecords
                
    def _getUgcInfo(self, ugc, part='type'):
        if part == 'type':
            if ugc[2] == 'C': 
                return 'County'
            else: 
                return 'Zone'
        if part == 'number':
            return ugc[3:]
        
    def _convertToISO(self, time_ms, local=None):
        dt = datetime.datetime.fromtimestamp(time_ms / 1000)
        if local:
            timeZone = self._timeZones[0]
        else:
            timeZone = None
        return self._tpc.formatDatetime(dt, timeZone=timeZone)
    
    def _convertToDatetime(self, time_ms):
        return datetime.datetime.fromtimestamp(time_ms / 1000)    
        
    def _getIsoTime(self, timeStr): 
        '''
        @param timeStr in format: yymmddThhssZ
        @return ISO format string
        '''
        if timeStr == '000000T0000Z':
            return '000-00-00T00:00Z'
        year =  2000 + int(timeStr[:2])
        month = int(timeStr[2:4])
        day = int(timeStr[4:6])
        hour = int(timeStr[7:9])
        secs = int(timeStr[9:11])
        dt = datetime.datetime(year, month, day, hour, secs)
        return dt.isoformat()
                
    def _createPolygonEntries(self):
        polygonEntries = []
        for eventDict in self._segmentEventDicts:
            polygons = self._extractPolygons(eventDict)
            for polygon in polygons:
                polyDict = collections.OrderedDict()
                pointList = []
                for lon, lat in polygon:
                    pointDict = collections.OrderedDict()
                    pointDict['latitude'] = str(lat)
                    pointDict['longitude'] = str(lon)
                    pointList.append(pointDict)
                pointDict = collections.OrderedDict()
                pointDict['point'] = pointList
            polygonEntries.append(pointDict)       
        polyDict = collections.OrderedDict()
        polyDict['polygon'] = polygonEntries
        return polyDict
    
    def _extractPolygons(self, eventDict):
        '''
        Return the lat/lon polygon points from the eventDict
        There could be multiple polygons
        '''
        polygons = []
        shapes = eventDict.get('shapes', [])
        for shape in shapes:
            if shape.get('shapeType') == 'polygon':
                points = shape.get('points', [])
                polygons.append(points)
        return polygons
    
    def _createTimeMotionLocationEntry(self):
        '''
        To be implemented in PV2
        '''
        return collections.OrderedDict()
    
    def _createImpactedLocationEntries(self, segment):
        '''
        @param segment
        @return list of Ordered Dictionaries representing locations covered by the segment
        '''
        # Must use self._ugcs to handle point hazards
        # In the case of point hazards, the segment 'ugcs' field is really the pointID e.g. DCTN1
        #  rather than the UGC that corresponds to that point.
        # The UGC is set for the segment (see preProcessSegment) in self._ugcs
        #ugcs, ids = segment
        locations = []
        for state, portion, areas in  self._areas:
            for area in areas:
                areaName, areaType = area
                locations.append({'locationName': areaName})
        cities = self.getCityInfo(self._ugcs, returnType='list') 
        for cityName, latLon in cities:
            lat, lon = latLon
            pointDict = collections.OrderedDict()
            pointDict['latitude'] = str(lat)
            pointDict['longitude'] = str(lon)
            locations.append({'point': pointDict})
            locations.append({'locationName': cityName})
        impactedLocations = collections.OrderedDict()
        impactedLocations['location'] = locations
        return impactedLocations
    

    #############################################################################
    #  Formatting for Short-fused products
    #  Except for the first method, these rely on Storm Track Recommender information
    #       which will be provided in PV2
    #  These methods will be re-written in PV2
     #############################################################################
   
    def formatPolygonForEvent(self, eventDict):
        for shape in eventDict.get('shapes'):
            if shape.get('shapeType') == 'polygon':
                polyStr = 'LAT...LON'
                points = shape.get('points')
                # 3 points per line
                pointsOnLine = 0
                for point in points:              
                    if pointsOnLine == 3:
                        polyStr += '\n         '
                    lon, lat = point
                    # For end of Aleutians
                    if lat>50 and lon>0 : 
                        lon = 360 - lon
                    elif lon<0 :
                        lon = -lon
                    lon = (int)(100*lon + 0.5)
                    if lat<0 :
                        lat = -lat
                    lat = (int)(100*lat + 0.5)
                    polyStr += ' '+str(lat)+' '+str(lon)
                    pointsOnLine += 1
                return polyStr+'\n'
        return ''
    
    def formatTimeMotionLocationForEvent(self, eventDict) :
        modNam = 'ProductGeneratorTemplate:formatTimeMotionLocationForEvent'
        # Time Motion Location
        clientid = eventDict.get('clientid')
        if clientid == None :
            return None
        # Need to get the storm motion for PV2
        # Stubbed for PV1
        return None
    
        try :
            tmpEventTime = 0
            for shape1 in eventDict['shapes'] :
                et1 = self.processTime(shape1.get('pointID'))
                if et1 != None :
                    if et1 > tmpEventTime :
                        tmpEventTime = et1
            if tmpEventTime == 0 :
                tmpEventTime = self.processTime(eventDict['startTime'])
            inJson = "{ 'action' : 'state', ' + \
                       ''times' : ['+str(tmpEventTime)+'], ' + \
                       ''id' : ''+clientid+'/latest' }"
            outData = json.loads(myJT.transaction(inJson))
            frame = outData['frameList'][0]
            speed = frame['speed']
            bearing = frame['bearing']
            shape = frame['shape']
            timeMotionLocationStr = 'TIME...MOT...LOC '
            timeMotionLocationStr += self._tpc.getFormattedTime( \
               self.processTimeForGetFormattedTime(tmpEventTime), \
                   '%H%MZ ', shiftToLocal=0, stripLeading=0).upper()
            timeMotionLocationStr += str(int(bearing+0.5))+'DEG '+str(int(speed+0.5))+'KT'
            a = 2
            for onept in shape :
                a = a-1
                if a<0 :
                    a = 3
                    timeMotionLocationStr += '\n           '
                lat = onept[1]
                lon = onept[0]
                if lat>50 and lon>0 : # For end of Aleutians
                    lon = 360 - lon
                elif lon<0 :
                    lon = -lon
                lon = (int)(100*lon + 0.5)
                if lat<0 :
                    lat = -lat
                lat = (int)(100*lat + 0.5)
                timeMotionLocationStr += ' '+str(lat)+' '+str(lon)
            return timeMotionLocationStr
        except :
            return None
        return None

    def descMotionForEvent(self, eventDict, useMph=True, \
                           still='STATIONARY', slow='NEARLY STATIONARY',
                           lead='MOVING', trail='',
                           minSpd=2.5, round=5.0) :
        modNam = 'ProductGeneratorTemplate:descMotionForEvent'
        clientid = eventDict.get('clientid')
        if clientid == None :
            return None
        return None
        # Need to get the storm motion for PV2
        # Stubbed for PV1
        try :
            tmpEventTime = 0
            for shape1 in eventDict['shapes'] :
                et1 = self.processTime(shape1.get('pointID'))
                if et1 != None :
                    if et1 > tmpEventTime :
                        tmpEventTime = et1
            if tmpEventTime == 0 :
                tmpEventTime = self.processTime(eventDict['startTime'])
            inJson = "{ 'action' : 'state', ' + \
                       ''times' : ['+str(tmpEventTime)+'], ' + \
                       ''id' : ''+clientid+'/latest' }"
            outData = json.loads(myJT.transaction(inJson))
            frame = outData['frameList'][0]
            speed = frame['speed']
            if speed<0 :
                return None
            if speed==0 :
                return still
            if useMph :
                speed *= 1.16
            if speed<minSpd :
                return slow
            bearing = 45*(int)((frame['bearing']+ 22.5)/45)
            if bearing==45 :
                bearing = 'SOUTHWEST '
            elif bearing==90 :
                bearing = 'WEST '
            elif bearing==135 :
                bearing = 'NORTHWEST '
            elif bearing==180 :
                bearing = 'NORTH '
            elif bearing==225 :
                bearing = 'NORTHEAST '
            elif bearing==270 :
                bearing = 'EAST '
            elif bearing==315 :
                bearing = 'SOUTHEAST '
            else :
                bearing = 'SOUTH '
            speed = round*int((speed+round/2)/round)
            movStr = bearing+'AT '+str(int(speed))
            if useMph :
                movStr += ' MPH'
            else :
                movStr += ' KNOTS'
            if len(lead)>0 :
                movStr = lead+' '+movStr
            if len(trail)>0 :
                movStr += ' '+trail
            return movStr
        except :
            return None
        return None

    def descWxLocForEvent(self, eventDict,
             noevent='FROM HEAVY RAIN. THIS RAIN WAS LOCATED', \
             point='FROM A THUNDERSTORM. THIS STORM WAS LOCATED', \
             line='FROM A LINE OF THUNDERSTORMS. THESE STORMS WERE LOCATED', \
             lead='', \
             trail='over the warned area') :
        modNam = 'ProductGeneratorTemplate:descWxLocForEvent'
        clientid = eventDict.get('clientid')
        if clientid == None :
            if lead=='-' :
                return noevent
            wxLoc = ''
            if len(lead)>0 :
                wxLoc = lead+' '
            wxLoc += noevent
            if len(trail)>0 :
                wxLoc += ' '+trail
            return wxLoc
        # Need to get the storm motion for PV2
        # Stubbed for PV1
        return None
        try :
            tmpEventTime = 0
            for shape1 in eventDict['shapes'] :
                et1 = self.processTime(shape1.get('pointID'))
                if et1 != None :
                    if et1 > tmpEventTime :
                        tmpEventTime = et1
            if tmpEventTime == 0 :
                tmpEventTime = self.processTime(eventDict['startTime'])
            inJson = "{ 'action' : 'state', ' + \
                       ''times' : ['+str(tmpEventTime)+'], ' + \
                       ''id' : ''+clientid+'/latest' }"
            outData = json.loads(myJT.transaction(inJson))
            frame = outData['frameList'][0]
            shape = frame['shape']
            if lead=='-' :
                if len(shape)<=1 :
                    return point
                else :
                    return line
            wxLoc = ''
            if len(lead)>0 :
                wxLoc = lead+' '
            if len(shape)<=1 :
                wxLoc += point
            else :
                wxLoc += line
            if len(trail)>0 :
                wxLoc += ' '+trail
            return wxLoc
        except :
            pass
        wxLoc = ''
        if len(lead)>0 :
            wxLoc = lead+' '
        wxLoc += noevent
        if len(trail)>0 :
            wxLoc += ' '+trail
        return wxLoc

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

