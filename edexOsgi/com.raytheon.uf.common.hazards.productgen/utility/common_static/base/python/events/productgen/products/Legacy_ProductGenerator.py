'''
    Description: Base Class for Legacy Product Generators holding common logic and process.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation
    July  1, 2013  648       Tracy.L.Hansen      Added CAP fields to dictionary
    July  8, 2013  784,1290  Tracy.L.Hansen      Added ProductParts and changes for ESF product generator
    Aug  14, 2013  784,1360  Tracy.L.Hansen      Organized more according to Product Parts and Dictionary,
                                                 Added handling of sections within segments,
                                                 Returning updated eventSet with Product Dictionaries
                                                 Issues 784, 1369     
    Sept 9,  2013  1298       Tracy.L.Hansen     Setting hazard event to ended, setting product information
                                                 on ended hazard events, correctly reporting currentStage, 
                                                 floodStage
    Dec      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents
    Jan  7, 2014   2367      jsanchez            Replaced ProductParts with a native python objects.
    Apr 11, 2014   3422      bkowal              Use getHazardTypes in bridge.py
    Apr 18, 2014   696       dgilling            Add support for selectable VTEC mode.
    
    @author Tracy.L.Hansen@noaa.gov
'''

import ProductTemplate

from Bridge import Bridge
from LocalizationInterface import *

from HazardServicesGenericHazards import HazardServicesGenericHazards
from TextProductCommon import TextProductCommon
from ProductParts import ProductParts

# NOTE:  Temporary try / except around the RiverForecastPoints 
#   module until we figure out how to import it into the Product Generator
try:
    from RiverForecastPoints import RiverForecastPoints
except:
    pass

import logging, UFStatusHandler
from MapInfo import MapInfo
from VTECEngineWrapper import VTECEngineWrapper

import collections, datetime, time
from pytz import timezone
import os, types, copy, sys, json
from QueryAfosToAwips import QueryAfosToAwips
import HazardDataAccess
import HazardConstants

from HazardEvent import HazardEvent
from shapely import geometry
from KeyInfo import KeyInfo
import ProductTextUtil

class Product(ProductTemplate.Product):
 
    def __init__(self):  
        '''
        General Note:  All times are in milliseconds unless designated 
        otherwise e.g. issueTime_secs.
        
        '''  
        #self.initialize()
    
    def initialize(self):      
        self._gh = HazardServicesGenericHazards()
        self._mapInfo = MapInfo()    
        
        self.bridge = Bridge()        
        self._hazardTypes = self.bridge.getHazardTypes()         
        self._areaDictionary = self.bridge.getAreaDictionary()
        self._cityLocation = self.bridge.getCityLocation()
        self._siteInfo = self.bridge.getSiteInfo()
        
        self._tpc = TextProductCommon()
        self._tpc.setUp(self._areaDictionary)        
        self._pp = ProductParts()
        try:
            self._rfp = RiverForecastPoints()
        except:
            pass


        self.logger = logging.getLogger('Legacy_ProductGenerator')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'com.raytheon.uf.common.hazards.productgen', 'Legacy_ProductGenerator', level=logging.INFO))
        self.logger.setLevel(logging.INFO)  
                
        # Default is True -- Products which are not VTEC can override and set to False
        self._vtecProduct = True       
        self._vtecEngine = None
        self._productCategory = ''

    def defineScriptMetadata(self):
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
    
    def execute(self, eventSet, dialogInputMap):          
        '''
        Must be overridden by the Product Generator
        '''
        pass
    def _getVariables(self, eventSet): 
        '''
         Set up class variables
        ''' 
        self._inputHazardEvents = eventSet.getEvents()
        metaDict = eventSet.getAttributes()
                
        # List of vtecEngineWrappers generated for these products
        #  Used at end to save vtec records if issueFlag is on
        self._wrappers = []        
        
        self._issueFlag = metaDict.get('issueFlag')
        
        if self._issueFlag == 'False':
            self._issueFlag = False
        else:
            self._issueFlag = True

        self._formats = metaDict.get('formats')        
        self._issueTime = int(metaDict.get('currentTime'))
        self._issueTime_secs = self._issueTime / 1000
        self._siteID = metaDict.get('siteID')
        self._tpc.setSiteID(self._siteID)
        self._backupSiteID = metaDict.get('backupSiteID', self._siteID)
        inputFields = metaDict.get('inputFields')
        if not inputFields:
            inputFields = {}
        self._overviewHeadline_value = inputFields.get('overviewHeadline', '') 
                 
        self._sessionDict = metaDict.get('sessionDict')
        if not self._sessionDict :
            self._sessionDict = {}
        self._lineLength = 69
        self._upperCase = True
         
        # Set up issue time strings       
        self._ddhhmmTime = self._tpc.getFormattedTime(
              self._issueTime_secs, '%d%H%M', shiftToLocal=True, stripLeading=False).upper()
        self._timeLabel = self._tpc.getFormattedTime(
              self._issueTime_secs, '%I%M %p %Z %a %b %e %Y',
              shiftToLocal=True, stripLeading=True).upper()
              
        # These come from SiteInfo
        # Primary Site
        siteEntry = self._siteInfo.get(self._siteID)
        self._fullStationID = siteEntry.get('fullStationID')  # KBOU
        self._region = siteEntry.get('region')
        self._wfoCity = siteEntry.get('wfoCity')
        self._wfoCityState = siteEntry.get('wfoCityState')
        self._areaName = ''  # siteEntry.get('state')  #  'GEORGIA' 

        # Backup Site
        siteEntry = self._siteInfo.get(self._backupSiteID)        
        self._backupWfoCityState = siteEntry.get('wfoCityState')
        self._backupFullStationID = siteEntry.get('fullStationID')
        
        vtecMode = metaDict.get('vtecMode')
        if vtecMode:
            vtecMode = str(vtecMode)
        self._vtecMode = vtecMode
        self._vtecTestMode = bool(metaDict.get('vtecTestMode'))
                
    def _makeProducts_FromHazardEvents(self, hazardEvents): 
        '''        
        Make the products
        @param hazardEvents: hazard events
        
        @return productDicts -- one dictionary for each product generated
        @return generatedHazardEvents -- hazard events are modified with product 
                       information (e.g. PIL, VTEC Codes) and returned for update
        
            As input, Product Generators take a set of hazard events and produce 1 
            or more products.  For example, the FFA_ProductGenerator may take in 
            an areal FA.A and point FL.A which by policy must go into two separate 
            FFA products.  
            
            For each product, one dictionary is generated.  That dictionary serves 
            as input to multiple formatters to produce, for example, legacy ASCII text 
            (Legacy formatter), parter XML (XML formatter), and CAP (CAP formatter) 
            formats for each product
            
            A product dictionary is organized according to the product parts. 
            The dictionary for a typical VTEC product is as follows:
                Product level information (e.g. wmoHeader, overview)
                    Could be multiple hazard events
                Segment level information (e.g. ugcHeader, vtec strings)
                    Determined by VTEC rules
                    Could be multiple VTEC lines, multiple hazard events
                Section level information (e.g. bulleted text, calls to action)
                    Typically one hazard event is described
                    Or in the case of long-fused watches (e.g.FA.A), multiple
                    duplicate hazard events (1 ETN) are described.                    
        '''        
        # Determine the list of segments given the hazard events 
        segments = self._getSegments(hazardEvents)
        self.logger.info('Product Generator --  Number of segments=' + str(len(segments)))
         
        # Determine the list of products and associated segments given the segments
        productSegmentGroups = self._groupSegments(segments)
        
        # Create each product dictionary and add to the list of productDicts
        productDicts = []                
        for productSegmentGroup in productSegmentGroups:
            productDict = self._initializeProductDict(productSegmentGroup)  
            productParts = productSegmentGroup.get('productParts') 
            productDict['productParts'] = productParts                                         
            self._pp._processProductParts(self, productDict, productSegmentGroup, productParts)
            self._wrapUpProductDict(productDict)
            productDicts.append(productDict)
            
        # If issuing, save the VTEC records for legacy products       
        self._saveVTEC(self._generatedHazardEvents) 
        # Note: these print statements are left here for debugging.
#         print '\nPGT Output dictionaries'
#         for productDict in productDicts:
#             self._printDict(productDict)            
#         print 'PGT Output hazardEvents'
#         for hazardEvent in self._generatedHazardEvents:
#             print '  ', str(hazardEvent)
#         self.flush()
        #return [], self._generatedHazardEvents
        return productDicts, self._generatedHazardEvents

    def _printDict(self, dictionary, indent=''):
        for key in dictionary:
            value = dictionary.get(key)
            if type(value) is collections.OrderedDict or type(value) is types.DictType:
                print key, ': {'
                self._printDict(value, indent+'  ')
                print '}'
            elif type(value) is types.ListType:
                print key, ': ['
                for val in value:
                    if type(val) is collections.OrderedDict or type(val) is types.DictType:
                        print ' {'
                        self._printDict(val, indent+'  ')
                        print '}'
                    else:
                        print val
                print ']'
            else:
                print key, ':', value
              

    ######################################################
    #  Product Segment determination         
    ######################################################
    def _getSegments(self, hazardEvents):
        '''
        Determine the segments for the product
        @param hazardEvents: list of Hazard Events
        @return a list of segments for the hazard events
        '''
        self._generatedHazardEvents = self.determineShapeTypesForHazardEvents(hazardEvents)
        self.getVtecEngine(self._generatedHazardEvents)        
        segments = self._vtecEngine.getSegments()
        return segments

    def _getSegments_ForPointsAndAreas(self, hazardEvents):
        '''
        Gets the segments for point hazards and areal hazards separately
        
        Sets variables 
            self._pointEvents, self._point_segment_vtecRecords_tuples
            self._areaEvents,  self._area_segment_vtecRecords_tuples
            
        @param hazardEvents: list of Hazard Events
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
        self._point_segment_vtecRecords_tuples = []
        self._area_segment_vtecRecords_tuples = []
        self._generatedHazardEvents = []
        for hazardEvent in hazardEvents:
            if hazardEvent.get('geoType') == 'point':
                self._pointEvents.append(hazardEvent)
            else:
                self._areaEvents.append(hazardEvent)
        for geoType in ('point', 'area'):
            if geoType == 'point' :   events = self._pointEvents
            else:                     events = self._areaEvents
            if not events: continue
            events = self.determineShapeTypesForHazardEvents(events)
            self._generatedHazardEvents += events
            self.getVtecEngine(events)
            segments = self._vtecEngine.getSegments()
            segment_vtecRecords_tuples = []
            for segment in segments:
                vtecRecords = self.getVtecRecords(segment)
                segment_vtecRecords_tuples.append((segment, vtecRecords))
            if geoType == 'point':  
                self._point_segment_vtecRecords_tuples = segment_vtecRecords_tuples
                self._pointVtecEngine = self._vtecEngine
            else:                     
                self._area_segment_vtecRecords_tuples = segment_vtecRecords_tuples
                self._areaVtecEngine = self._vtecEngine                  
        return self._point_segment_vtecRecords_tuples + self._area_segment_vtecRecords_tuples
            
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
    
    ######################################################
    #  Product Dictionary -- General product information        
    ######################################################

    def _initializeProductDict(self, productSegmentGroup):
        '''
        Set up the Product Dictionary for the given Product consisting of a 
        group of segments.
        
        Fill in the dictionary information for the product header.
        
        @param productSegmentGroup: holds meta information about the product
        @return initialized product dictionary
      
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
        
        '''        
        self._productID = productSegmentGroup.get('productID', 'NNN') 
        self._getProductInfo(self._siteID, self._productID)
        if self._areaName != '':
            self._areaName = ' FOR ' + self._areaName + '\n'
        self._geoType = productSegmentGroup.get('geoType')
        self._vtecEngine = productSegmentGroup.get('vtecEngine')
        self._mapType = productSegmentGroup.get('mapType')
        self._segmented = productSegmentGroup.get('segmented')
        self._formatPolygon = productSegmentGroup.get('formatPolygon')
        self._productTimeZones = []
        
        # Fill in product dictionary information
        productDict = collections.OrderedDict()
        productDict['productID'] = self._productID
        return productDict

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
        self._wmoID = a2a.getWMOprod()  # e.g. WUUS53
        self._CCC = a2a.getCCC()  # e.g. OMA         
        # Product PIL, e.g. SVRTOP
        self._pil = productID + siteID
        # Product ID for transmitting to AWIPS WAN, e.g. KTOPSVRTOP  
        self._awipsWANPil = a2a.getAwipsWANpil()            
        # Product ID for storing to AWIPS text database, e.g. TOPSVRTOP  
        self._textdbPil = a2a.getTextDBpil() 

    def _wrapUpProductDict(self, productDict):    
        productDict['sentTimeZ'] = self._convertToISO(self._issueTime)
        productDict['sentTimeZ_datetime'] = self._convertToDatetime(self._issueTime)
        productDict['sentTimeLocal'] = self._convertToISO(self._issueTime, local=True)
        productDict['timeZones'] = self._productTimeZones
        self._addToProductDict(productDict)
        return productDict
    
    
    ######################################################
    #  Product Part Methods 
    # 
    #    def _methodName(self, productDict, productSegmentGroup, arguments=None):
    #        
    ######################################################
        

    ################# Product Level
          
    def _wmoHeader(self, productDict, productSegmentGroup, arguments=None):
        headerDict = collections.OrderedDict()
        headerDict['TTAAii'] = self._wmoID
        headerDict['originatingOffice'] = self._backupFullStationID  # Will be siteID if not in backup mode
        headerDict['productID'] = self._productID
        headerDict['siteID'] = self._siteID
        headerDict['wmoHeaderLine'] = self._wmoID + ' ' + self._fullStationID + ' ' + self._ddhhmmTime
        headerDict['awipsIdentifierLine'] = self._productID + self._siteID
        productDict['wmoHeader'] = headerDict

    def _wmoHeader_noCR(self, productDict, productSegmentGroup, arguments=None):
        self._wmoHeader(productDict, productSegmentGroup, arguments=None)

    def _easMessage(self, productDict, productSegmentGroup, arguments=None):
        productDict['easActivationRequested'] = 'true' 
        
    def _productHeader(self, productDict, productSegmentGroup, arguments=None):
        productDict['disclaimer'] = 'This XML wrapped text product should be considered COMPLETELY EXPERIMENTAL. The National Weather Service currently makes NO GUARANTEE WHATSOEVER that this product will continue to be supplied without interruption. The format of this product MAY CHANGE AT ANY TIME without notice.'
        productDict['senderName'] = 'NATIONAL WEATHER SERVICE ' + self._wfoCityState
        self._productName = self.checkTestMode(
                self._sessionDict, productSegmentGroup.get('productName') + self._areaName)
        productDict['productName'] = self._productName
        productDict['issuedByString'] = self.getIssuedByString()

    def _overviewHeadline_area(self, productDict, productSegmentGroup, segment_vtecRecords_tuples):
        productDict['overviewHeadline'] = '|* Overview Headline *|'
        
    def _overviewHeadline_point(self, productDict, productSegmentGroup, segment_vtecRecords_tuples):
        '''
        The Overview headline for Point-based FFA, FLW, FLS would look like this:

            ...THE NATIONAL WEATHER SERVICE IN <WFO location> HAS ISSUED A
            FLOOD WATCH [(optional:) UNTIL <time/day phrase>4] FOR THE FOLLOWING
            <LOCATION(S) or RIVER(S)> <IN or ON> <geographic name or phrase>...
            
            - or -
            
            ...THE FLOOD WATCH CONTINUES [(optional:) UNTIL <time/day phrase>] FOR
            THE FOLLOWING <LOCATION(S) or RIVER(S)> <IN or ON> <geographic name
            or phrase>...
            
            <river/stream> <proximity term> <location> [(optional:) AFFECTING 
            <county #1>...<county #2> AND <county #n> <COUNTY or COUNTIES>].
            
            AFFECTING THE FOLLOWING COUNTIES IN <state>....<county #1>...
            <county #2> AND <county #n>. (optional)
            
            
            and/ or 
            
            ...THE FLOOD WATCH <IS CANCELLED or HAS EXPIRED or WILL EXPIRE> FOR THE
            FOLLOWING <LOCATION(S) or RIVER(S)> <IN or ON> <geographic name or
            phrase>...
            
            
            Note (4): <time/day phrase> stands for time/day phrases used in long duration watches (see NWSI 10-1701) -
            i.e., specific times within 12 hours of issuance, general phrases beyond 12 hours (e.g., TUESDAY AFTERNOON).

            
            '''
        # Group the segments according to VTEC code
        #  The order of appearance of segments (CAN, EXP, NEW, EXT, CON)
        #  differs from the order of appearance in the overviewHeadline (NEW, EXT, CON, CAN, EXP).
        #  Also, each segment lists one point whereas the overviewHeadline will
        #  list locations for all similar points together e.g. all NEW together...
        new_ext_segment_vtecRecords_tuples = []
        con_segment_vtecRecords_tuples = []
        can_exp_segment_vtecRecords_tuples = [] 
        for segment, vtecRecords in segment_vtecRecords_tuples:
            for vtecRecord in vtecRecords:
                action = vtecRecord.get('act')
                if action in ('NEW','EXT'):
                    new_ext_segment_vtecRecords_tuples.append((segment, vtecRecords))
                elif action == 'CON':
                    con_segment_vtecRecords_tuples.append((segment, vtecRecords))
                else:  # CAN, EXP
                    can_exp_segment_vtecRecords_tuples.append((segment, vtecRecords)) 
        
        overviewHeadline = ''
        for tupleGroup in  [
                        new_ext_segment_vtecRecords_tuples,
                        con_segment_vtecRecords_tuples,
                        can_exp_segment_vtecRecords_tuples]: 
            if tupleGroup:
                overviewHeadline += self._getOverviewHeadline_point(tupleGroup) 
        productDict['overviewHeadline_point'] = overviewHeadline
        
    def _getOverviewHeadline_point(self, segment_vtecRecords_tuples):
        '''
        ...FLOOD WATCH IN EFFECT FOR THE FOLLOWING LOCATIONS ON THE MILWAUKEE
            RIVER IN SOUTHEAST WISCONSIN...
                MILWAUKEE RIVER AT WAUBEKA AFFECTING OZAUKEE COUNTY.
                MILWAUKEE RIVER AT CEDARBURG AFFECTING OZAUKEE COUNTY.
                MILWAUKEE RIVER AT MILWAUKEE AFFECTING MILWAUKEE COUNTY.
        Report the geographic areas / rivers for the combination of segments in the tuples.
        Report the time IF the times are the same for all vtecRecords in the tuples.
        '''
        
        segmentAreas = set()
        for segment, vtecRecords in segment_vtecRecords_tuples:
            segAreas, eventIDs = segment
            segmentAreas.update(segAreas)
        partOfState = None
        riverNames = []
        
        # NOTE:  Temporary try / except until we figure out how to import RiverForecastPoints
        try:
         for segmentArea in segmentAreas:
             riverNames.append(self._rfp.getGroupName(segmentArea))
        except:
            pass
        metaDataList, segmentHazardEvents = self.getHazardMetaData(segment)
        summaryHeadlines_value, headlines, sections = self._tpc.getHeadlinesAndSections(
            vtecRecords, metaDataList, self._productID, self._issueTime_secs) 
        overview = summaryHeadlines_value
        try:
         for segmentArea in segmentAreas:
             overview += self._rfp.getGroupForecastPointList(segmentArea) + '\n'
        except:
            pass
        return overview        
                                                
    def _overviewSynopsis(self, productDict, productSegmentGroup, arguments=None):
        '''
        FFA_point:
        <General synopsis. Note for cancellation or expiration products: if a
        flood situation never developed, provide a brief explanation of why this
        was the case; if flood situation developed or is developing, mention that a
        flood product (advisory, warning) will be or has been issued>.
        If product is not a cancellation or expiration, include the following:
        THE SEGMENTS IN THIS PRODUCT ARE RIVER FORECASTS FOR SELECTED
        LOCATIONS IN THE WATCH AREA [(optional:) BASED ON CURRENTLY AVAILABLE
                                     RAINFALL FORECASTS RANGING FROM <QPF lower range> TO <QPF upper range>
                                     INCHES OVER THE <river/basin name(s)>]...
        '''
        productDict['overviewSynopsis'] = ''
        
    def _rainFallStatement(self, productDict, productSegmentGroup, arguments=None):
        '''
        If product is not a cancellation or expiration, include the following: 
        THE SEGMENTS IN THIS PRODUCT ARE RIVER FORECASTS FOR SELECTED 
        LOCATIONS IN THE WATCH AREA [(optional:) BASED ON CURRENTLY AVAILABLE 
         
         RAINFALL FORECASTS RANGING FROM <QPF lower range> TO <QPF upper range> 
        INCHES OVER THE <river/basin name(s)>]... 
        '''
        productDict['rainFallStatement'] = ''
        
    def _callsToAction_productLevel(self, productDict, productSegmentGroup, arguments=None):
        '''
        We pass here because the Calls To Action will be gathered as we go through the segments.
        The 'wrapup_product' method will then add the cta's to the product dictionary
        '''
        pass
   
    def _additionalInfoStatement(self, productDict, productSegmentGroup, arguments=None):
        productDict['additionalInfoStatement'] = 'ADDITIONAL INFORMATION IS AVAILABLE AT <Web site URL>.'
        
    def _nextIssuanceStatement(self, productDict, productSegmentGroup, arguments=None):
        productDict['nextIssuanceStatement'] = 'THE NEXT STATEMENT WILL BE ISSUED <time/day phrase>.' 
        
    def _floodPointTable(self, productDict, productSegmentGroup, arguments=None):
        # TODO Could be called at product level (no arguments or segment level with _segment_vtecRecords_tuple argument
        pass
        
    def _wrapUp_product(self, productDict, productSegmentGroup, arguments=None):
        productDict['ctas'] = self._ctas
        
    ################# Segment Level

        '''
        Create the dictionary information for the segment  
        
        *********
        Example segmented product segment header:
                 
        COC003-080400-
        /0.NEW.KBOU.FL.A.0001.110208T0400Z-110208T0430Z/
        /00000.2.SM.110208T0400Z.110208T0755Z.110208T0755Z.NR/
        DOUGLAS CO-
        400 AM GMT TUE FEB 8 2011 
        
        ...FLOOD WATCH IN EFFECT LATE MONDAY NIGHT...

        
        **********
        Example non-segmented product segment header:
        
        COC005-035-039-070730-
        /O.NEW.KBOU.FF.W.0004.120607T0425Z-120607T0730Z/
        /00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/

        BULLETIN - EAS ACTIVATION REQUESTED
        FLASH FLOOD WARNING
        NATIONAL WEATHER SERVICE DENVER CO
        1025 PM MDT WED JUN 6 2012
        
        ...FLOOD WARNING IN EFFECT LATE MONDAY NIGHT...
        
        '''
    def _setup_segment(self, segmentDict, productSegmentGroup, productSegment):   
        segment, vtecRecords = productSegment
        self._metaDataList, self._segmentHazardEvents = self.getHazardMetaData(segment)
        # NOTE -- using getVtecRecords to change to milliseconds
        self._segmentVtecRecords = self.getVtecRecords(segment)
       
        # There may be multiple (metaData, hazardEvent) pairs in a segment 
        #   An example would be for a NPW product which had a Frost Advisory and a Wind Advisory in one segment
        # There will be a section for each 
        # 'segment' is (frozenset([list of ugcs or points]), frozenset([list of eventIDs])        
            
        # UGCs and Expire Time
        # Assume that the geoType is the same for all hazard events in the segment i.e. area or point
        hazardEvent = self._segmentHazardEvents[0]
        if hazardEvent.get('geoType') == 'area':
            self._ugcs = list(segment[0])
        else:
            self._ugcs = hazardEvent.get('ugcs', [])    
        self._ugcs.sort()  
        self._timeZones = self._tpc.hazardTimeZones(self._ugcs)
        segmentDict['timeZones'] = self._timeZones  

        for tz in self._timeZones:
            if tz not in self._productTimeZones:
                self._productTimeZones.append(tz)
        self._expireTime = self._tpc.getExpireTime(
                    self._issueTime, self._purgeHours, self._segmentVtecRecords) 
        self._ugcHeader_value = self._tpc.formatUGCs(self._ugcs, self._expireTime)
                                                        
        #
        # Generate the sections i.e. attribution bullets, calls-to-action, polygonText
        #
        segmentDict['areaType'] = self._geoType
        segmentDict['expireTime'] = self._convertToISO(self._expireTime)
        segmentDict['expireTime_datetime'] = self._convertToDatetime(self._expireTime) 
        # CAP Specific Fields        
        segmentDict['status'] = 'Actual' 
        segmentDict['CAP_areaString'] = self._tpc.formatUGC_namesWithState(self._ugcs, separator='; ') 

        # Summary Headlines for the segment  -- this orders them  -- Used by Legacy after ugc header...
        #   Create and order the sections for the segment:
        #       (vtecRecord, sectionMetaData, sectionHazardEvent)      
        self._summaryHeadlines_value, self._headlines, self._sections = self._tpc.getHeadlinesAndSections(
                    self._segmentVtecRecords, self._metaDataList, self._productID, self._issueTime_secs) 

        # Check for special case where a CAN/EXP is paired with a
        # NEW/EXA/EXB/EXT
        #
        includeText, includeFrameCodes, skipCTAs, forceCTAList = \
          self._gh.useCaptureText(self._segmentVtecRecords)
        # find any 'CAN' with non-CAN for reasons of text capture
        self._canVtecRecord = None
        for vtecRecord in self._segmentVtecRecords:
            if vtecRecord['act'] in ['CAN', 'EXP', 'UPG']:
                self._canVtecRecord = vtecRecord
                break  # take the first one

        # Make Area Phrase
        areas = self._tpc.getGeneralAreaList(self._ugcs, areaDict=self._areaDictionary)
        self._areas = self._tpc.simplifyAreas(areas)
        self._areaPhrase = self._tpc.makeAreaPhrase(self._areas, self._geoType)
        self._areaPhraseShort = self._tpc.makeAreaPhrase(self._areas, self._geoType, True)

        # Calls to Action and polygonText are gathered and reported for all sections together
        self._ctas = []
        self._polygonText_value = ''
        segmentDict['polygons'] = self._createPolygonEntries()
       
        if not self._sections:
            self._setProductInformation(self._segmentVtecRecords[0], self._segmentHazardEvents[0])

    def _ugcHeader(self, segmentDict, productSegmentGroup, productSegment):
        segment, vtecRecords = productSegment
        segmentDict['ugcCodes'] = self._formatUGC_entries(segment)
        self._ugcHeader_value = self._tpc.formatUGCs(self._ugcs, self._expireTime)
        segmentDict['ugcHeader'] = self._ugcHeader_value
        segmentDict['segmentID'] = self._ugcHeader_value
       
    def _vtecRecords(self, segmentDict, productSegmentGroup, productSegment):
       segment, vtecRecords = productSegment
       segmentDict['vtecRecords'] = self._vtecRecordEntries(segment)

    def _areaList(self, segmentDict, productSegmentGroup, productSegment):
         # Area String        
        segmentDict['areaList'] = self._tpc.formatUGC_names(self._ugcs)
           
    def _cityList(self, segmentDict, productSegmentGroup, productSegment):
        segment, vtecRecords = productSegment
        ids, eventIDs = segment
        cityList = []
        for city, ugcCity in self.getCityInfo(self._ugcs, returnType='list'):
            cityList.append(city)        
        self._setEditedField(segmentDict, 'cityList', self._productCategory, self._productID, list(eventIDs), segment, cityList, "Included Cities")

    def _summaryHeadlines(self, segmentDict, productSegmentGroup, productSegment):
        '''
         Summary Headlines for the segment  -- this orders them  -- 
           Used by Legacy after ugc    header...
           Create and order the sections for the segment:
               (vtecRecord, sectionMetaData, sectionHazardEvent) 
        '''     
        segmentDict['summaryHeadlines'] = self._summaryHeadlines_value
        segmentDict['headlines'] = self._headlines
            
    def _meaningOfStatement(self, segmentDict, productSegmentGroup, productSegment):
        # TODO: Check for FA.A or FF.A  Flood Watch or Flash Flood Watch
        segmentDict['meaningOfStatement'] = 'A flood watch means that flooding is possible but not imminent in the watch area.\n'

    def _emergencyStatement(self, segmentDict, productSegmentGroup, productSegment):
        '''
        Example:
        ...A FLASH FLOOD EMERGENCY FOR <geographic area>...
        '''
        segmentDict['emergencyStatement'] = '...A FLASH FLOOD EMERGENCY FOR <geographic area>...'
        
    def _basisAndImpactsStatement_segmentLevel(self, segmentDict, productSegmentGroup, productSegment):
        segmentDict['basisAndImpactsStatement_segmentLevel'] = '|* current hydrometeorological situation and expected impacts *|\n'
        
    def _callsToAction(self, segmentDict, productSegmentGroup, productSegment):
        segmentDict['callsToAction'] = self._ctas

    def _polygonText(self, segmentDict, productSegmentGroup, productSegment):
        segmentDict['polygonText'] = self._polygonText_value

    def _timeMotionLocation(self, segmentDict, productSegmentGroup, productSegment):
        segmentDict['timeMotionLocation'] = self._createTimeMotionLocationEntry()

    def _impactedLocations(self, segmentDict, productSegmentGroup, productSegment):
        segment, vtecRecords = productSegment
        segmentDict['impactedLocations'] = self._createImpactedLocationEntries(segment)

    def _observations(self, segmentDict, productSegmentGroup, productSegment):
        segmentDict['observations'] = collections.OrderedDict()  # We do not have this sort of information yet

    ###################### Section Level

        '''
        Create the dictionary information for the section body -- Example:
        
         ...FLOOD WATCH IN EFFECT THROUGH WEDNESDAY MORNING...

        THE NATIONAL WEATHER SERVICE IN BLACKSBURG HAS ISSUED A
                
        * FLOOD WATCH FOR A PORTION OF SOUTH CENTRAL COLORADO...INCLUDING 
          THE FOLLOWING COUNTY...ALAMOSA.

        * UNTIL hhmm am/pm <day of week> time_zone   

        * AT hhmm am/pm time_zone <warning basis and expected impacts>.

        * (OPTIONAL) POTENTIAL IMPACTS OF FLOODING e.g. <forecast path of flood and/or locations to be affected>.

        ''' 
    def _setup_section(self, sectionDict, productSegmentGroup, arguments):
        self._productSegment, vtecRecord = arguments
        self._segment, self._vtecRecords_value = self._productSegment
        
        if not self._sections:
            return

        # Find the section information for this section
        for section in self._sections:
            sectionVtecRecord, sectionMetaData, sectionHazardEvent = section
            if sectionVtecRecord == vtecRecord:
                self._section = section
                self._sectionVtecRecord = sectionVtecRecord
                self._sectionMetaData = sectionMetaData
                self._sectionHazardEvent = sectionHazardEvent
           
        sectionDict['description'] = ''
        self._setProductInformation(self._sectionVtecRecord, self._sectionHazardEvent)
        
        # Process each part of the section
        self._testMode = self._sessionDict.get('testMode', 0)
#       TODO 
#       Need to rectify this code taken from existing A2 to make 
#       sure we retain the functionality as it pertains to 
#       user edited text (Issue 1321)
#         if vtecRecord['act'] in ['CAN','EXP','UPG']:
#             aPhrase = areaPhraseShort
#             canVtecRecord = None
#         else:
#             aPhrase = areaPhrase
#         phrase = self.makeSection(vtecRecord, canVtecRecord, self._areaPhrase, self._geoType, sectionHazardEvent, self._metaDataList,
#                                         self._issueTime_secs, self._testMode, self._wfoCity)

        ctas = self.getCTAsPhrase(sectionVtecRecord, self._canVtecRecord, sectionHazardEvent, sectionMetaData)
        if ctas:
            self._ctas += ctas

        if self._formatPolygon:
             self._polygonText_value += self.formatPolygonForEvent(self._sectionHazardEvent) + '\n'

       # CAP Specific Fields        
        infoDict = collections.OrderedDict()
        sectionDict['info'] = [infoDict]
        infoDict['category'] = 'Met'
        infoDict['responseType'] = self._sectionHazardEvent.get('responseType', '')  # 'Avoid'
        infoDict['urgency'] = self._sectionHazardEvent.get('urgency', '')  # 'Immediate'
        infoDict['severity'] = self._sectionHazardEvent.get('severity', '')  # 'Severe' 
        infoDict['certainty'] = self._sectionHazardEvent.get('certainty', '')  # 'Observed'
        infoDict['onset_datetime'] = self._sectionHazardEvent.getStartTime() 
        infoDict['WEA_text'] = self._sectionHazardEvent.get('WEA_Text', '')  # 'Observed'
        infoDict['pil'] = self._pil
        infoDict['sentBy'] = self._wfoCity
        infoDict['event'] = self._hazardTypes[self._sectionHazardEvent.getHazardType()]['headline']
        endTime = self._sectionHazardEvent.getEndTime() 
        if endTime: 
            infoDict['eventEndingTime_datetime'] = endTime

    def _attribution(self, sectionDict, productSegmentGroup, arguments):
        # Attribution and First Bullet
        attribution, firstBullet = self.getAttributionPhrase(
                   self._sectionVtecRecord, self._areaPhrase, self._issueTime, self._testMode, self._wfoCity)        
        sectionDict['attribution'] = attribution +'\n'
        sectionDict['description'] += attribution + '\n'

    def _firstBullet(self, sectionDict, productSegmentGroup, arguments):
        # TODO -- if attribution, then this is duplicated  -- Separate out first bullet from attribution
        attribution, firstBullet = self.getAttributionPhrase(
                   self._sectionVtecRecord, self._areaPhrase, self._issueTime, self._testMode, self._wfoCity)     
        sectionDict['firstBullet'] = firstBullet
        sectionDict['description'] += firstBullet + '\n'

    def _timeBullet(self, sectionDict, productSegmentGroup, arguments):        
        timeBullet = self.getHazardTimePhrases(self._sectionVtecRecord, self._issueTime)
        sectionDict['timeBullet'] = timeBullet
        sectionDict['description'] += timeBullet + '\n'

    def _basisBullet(self, sectionDict, productSegmentGroup, arguments):
        basisBullet = self.getBasisPhrase(self._sectionVtecRecord, self._canVtecRecord, self._sectionHazardEvent, self._sectionMetaData)
        self._setEditedField(sectionDict, 'basisBullet',  self._productCategory, self._productID, [self._sectionHazardEvent.getEventID()], 
                     self._segment, basisBullet + '\n', 'Basis')
        sectionDict['description'] += basisBullet + '\n'
        
    def _impactsBullet(self, sectionDict, productSegmentGroup, arguments):
        impactsBullet = self.getImpactsPhrase(self._sectionVtecRecord, self._canVtecRecord, self._sectionHazardEvent, self._sectionMetaData)
        self._setEditedField(sectionDict, 'impactsBullet',  self._productCategory, self._productID, [self._sectionHazardEvent.getEventID()], 
                     self._segment, impactsBullet, 'Impacts')
        sectionDict['description'] += impactsBullet + '\n'

    def _basisAndImpactsStatement(self, sectionDict, productSegmentGroup, arguments):
        '''
            AT 759 PM EDT...LOCAL LAW ENFORCEMENT OFFICIALS REPORTED INTERSTATE 131
            WAS CLOSED DUE TO OVER A FOOT OF WATER RUSHING OVER THE INTERSTATE NEAR
            ROCKFORD. KENT COUNTY ROAD COMMISSION REPORTED NUMEROUS ROAD CLOSURES
            DUE TO WATER OVER ROADS AND ROAD WASH OUTS. CHILDSDALE AVE SOUTH OF
            ROCKFORD WAS WASHED OUT AND WAS IMPASSABLE. ALTHOUGH RAIN HAS MOVED OUT
            OF THE AREA AND ADDITIONAL RAINFALL IS NOT EXPECTED OVERNIGHT...MANY LOW
            LYING AREAS ARE STILL FLOODED AND DRIVERS NEED TO BE ESPECIALLY CAUTIOUS
            AT NIGHT.
        '''
        basisAndImpactsStatement = '|* current hydrometeorological situation and expected impacts *|\n'
        self._setEditedField(sectionDict, 'basisAndImpactsStatement',  self._productCategory, self._productID, [self._sectionHazardEvent.getEventID()], 
                     self._segment, basisAndImpactsStatement, 'Basis And Impacts')
        sectionDict['description'] += basisAndImpactsStatement + '\n'
        
    def _locationsAffected(self, sectionDict, productSegmentGroup, arguments):
        ''' LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO SPARTA AND ROCKFORD.
        '''
        locationsAffected = '* |* Forecast path of flood and/or locations to be affected *|' + '\n'
        self._setEditedField(sectionDict, 'locationsAffected',  self._productCategory, self._productID, [self._sectionHazardEvent.getEventID()], 
                     self._segment, locationsAffected, 'Locations Affected')
        sectionDict['description'] += locationsAffected + '\n'
                
    def _floodPointHeadline(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['floodPointHeadline'] = 'Flood point headline'
    
    def _floodPointHeader(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['floodPointHeader'] = 'Flood point header'
    
    def _floodPointTimeBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['floodPointTimeBullet'] = '* Flood Point Time Bullet'

    def _floodStageBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['floodStageBullet'] = '* Flood point headline'
    
    def _otherStageBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['otherStageBullet'] = '* Other Stage Bullet'
    
    def _floodCategoryBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['floodCategoryBullet'] = '* Flood Category Bullet'
    
    def _recentActivityBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['recentActivityBullet'] = '* Recent Activity bullet'
    
    def _forecastStageBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['forecastStageBullet'] = '* Forecast Stage bullet'
    
    def _pointImpactsBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['pointImpactsBullet'] = '* Point impacts bullet'
    
    def _floodHistoryBullet(self, sectionDict, productSegmentGroup, arguments):
        sectionDict['floodHistoryBullet'] = '* Flood history bullet'
           
    def _endingSynopsis(self, sectionDict, productSegmentGroup, arguments):
        # TODO Add to Product Staging -- FFA, FLS area
        sectionDict['endingSynopsis'] = '\n|* Brief post-synopsis of hydrometeorological activity *|\n'
            
    ######################################
    #  Code       
    ######################################           
                                        
    def _setProductInformation(self, vtecRecord, hazardEvent):
        if self._issueFlag:
            # Update hazardEvent
            expTime = hazardEvent.get('expirationTime')
            # Take the earliest expiration time
            if (expTime and expTime > self._expireTime) or not expTime:
                hazardEvent.set('expirationTime', self._expireTime)
            hazardEvent.set('issueTime', self._issueTime)
            hazardEvent.addToList('etns', vtecRecord['etn'])
            hazardEvent.addToList('vtecCodes', vtecRecord['act'])
            hazardEvent.addToList('pils', vtecRecord['pil'])
        else:
            # Reset state if previewing ended
            if hazardEvent.get('previewState') == 'ended':
                hazardEvent.setState('ISSUED')

    def _addToProductDict(self, productDict):
        '''
        This method can be overridden by the Product Generators to add specific product information to the productDict
        '''
        pass
    
    def _addToSegmentEntry(self, segment, segmentEntry):
        '''
        This method can be overridden by the Product Generators to add specific product information to the segmentEntry
        '''
        pass

    def _addToSectionEntry(self, section, sectionEntry):
        '''
        This method can be overridden by the Product Generators to add specific product information to the sectionEntry
        '''
        pass
    
    def _setEditedField(self, prodDict, key, productCategory, productID, eventIDs, segment, default, label=None):      
        '''
        Retrieve user edited text using the given identifying information:
                key, self._productCategory, productID, segment, eventIDs 
        If not found, use the default value provided         
        '''   
        
        # Temporarily bypassing editable functionality to allow for further
        # testing and debugging of Product Editor Dialog
        prodDict[key] = default
        return
     
        # Converts a list of string integers into a list of integers
        tmp = []
        for eventID in eventIDs:
            tmp.append(int(eventID))
        eventIDs = tmp

        if None in [key, productCategory, productID, eventIDs, segment]:
            userEditedKey = key
            value = default
        else:
            # create KeyInfo object
            segmentList = list(segment[0])
            segmentList.sort()
            segment = ''
            for seg in segmentList:
                if len(segment) > 0:
                    segment += ', '
                segment += seg
            segment = str(segment)
            userEditedKey = KeyInfo(key, productCategory, productID, eventIDs, segment, True, label=label)
                    
            # Try and retrieve previously edited text
            productTextList = ProductTextUtil.retrieveProductText(key, productCategory, productID, segment, eventIDs)
            
            # If not there, use defaultValue
            if len(productTextList) > 0:
                value = productTextList[0].getValue()
            else:
                value = default
            
        prodDict[userEditedKey] = value
    
    def _formatUGC_entries(self, segment):
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
            ugcCodeList.append(ugcEntry)
        return ugcCodeList
    
    def _vtecRecordEntries(self, segment):
        '''
        @param segment -- current segment
        @return list of Ordered Dictionaries each representing a vtec or hvtec string
                for the segment
        '''
        vtecRecords = []        
        for vtecString in self._vtecEngine.getVTECString(segment):
            if not vtecString:
                continue
            
            vtecString = vtecString.strip('/')
            parts = vtecString.split('.')
            vtecString = '/' + vtecString + '/'
            vtecDict = collections.OrderedDict()
            if len(parts[0]) > 1:  
                vtecDict['vtecRecordType'] = 'hvtecRecordType'
                vtecDict['name'] = 'hvtecRecord'
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
                vtecDict['name'] = 'pvtecRecord'
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
        year = 2000 + int(timeStr[:2])
        month = int(timeStr[2:4])
        day = int(timeStr[4:6])
        hour = int(timeStr[7:9])
        secs = int(timeStr[9:11])
        dt = datetime.datetime(year, month, day, hour, secs)
        return dt.isoformat()
                
    def _createPolygonEntries(self):
        polygonEntries = []
        for hazardEvent in self._segmentHazardEvents:
            polygons = self._extractPolygons(hazardEvent)
            for polygon in polygons:
                pointList = []
                for lon, lat in polygon:
                    pointDict = collections.OrderedDict()
                    pointDict['latitude'] = str(lat)
                    pointDict['longitude'] = str(lon)
                    pointList.append(pointDict)
                pointDict = collections.OrderedDict()
                pointDict['point'] = pointList
            polygonEntries.append(pointDict)       
        return polygonEntries

    def _extractPolygons(self, hazardEvent):

        polygonPointLists = []            
        for geometry in hazardEvent.getGeometry():
            
            if geometry.geom_type == HazardConstants.SHAPELY_POLYGON:
                polygonPointLists.append(list(geometry.exterior.coords))
            elif geometry.geom_type == HazardConstants.SHAPELY_POINT or geometry.geom_type == HazardConstants.SHAPELY_LINE:
                polygonPointLists.append(list(geometry.coords))
            else:
                for geo in geometry:
                    polygonPointLists.append(list(geo.exterior.coords))
        return polygonPointLists
        
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
        
    #### Utility methods
    def getVtecEngine(self, hazardEvents) :
        '''
        Instantiates a VTEC Engine for the given hazardEvents
        Note that more than one VTEC Engine may be instantiated for
        a product generator. For example, point and area hazardEvents
        must have separate VTEC Engines.
        @param hazardEvents -- list of hazard events
        '''
        opMode = not self._sessionDict.get('testMode', 0)
        self._vtecEngineWrapper = VTECEngineWrapper(
               self.bridge, self._productCategory, self._fullStationID,
               hazardEvents, vtecMode=self._vtecMode, issueTime=self._issueTime_secs,
               operationalMode=opMode, testHarnessMode=False, vtecProduct=self._vtecProduct)
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
        vtecRecords = copy.deepcopy(vtecEngine.getVtecRecords(segment))
        # Change times to milliseconds from seconds
        for vtecRecord in vtecRecords:
            for key in ['startTime', 'endTime', 'issueTime', 'riseAbove', 'crest', 'fallBelow']:
                value = vtecRecord.get(key)
                if value:
                    vtecRecord[key] = value * 1000
        return vtecRecords

    def getIssuedByString(self, words='ISSUED BY NATIONAL WEATHER SERVICE '):
        '''
        If there is a backup site, then add an 'issued by' string
        '''
        if self._backupSiteID != self._siteID:
            issuedByString = words + self._backupWfoCityState + '\n'
        else:
            issuedByString = ''
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

    def determineShapeTypesForHazardEvents(self, hazardEvents):
        '''
        For each hazard event, determine the shapeType
        to associate with its collection of geometries.
        @param hazardEvents -- list of hazard events
        @return newHazardEvents -- list of augmented hazard events with 
            entries added for the shape type and site ID
        '''
        #
        # TODO - This logic needs to be improved or replaced 
        # to reflect the fact that multiple geometries of different shape types may be
        # associated with a single hazard event
        newHazardEvents = []
        for hazardEvent in hazardEvents:
            # VTEC processing expects siteID4 e.g. KOAX instead of OAX
            hazardEvent.set('siteID4', str(self._fullStationID))

            geometryCollection = hazardEvent.getGeometry()
            
            for geometry in geometryCollection:
            
                geometryType = geometry.geom_type
            
                if geometryType in [HazardConstants.SHAPELY_POLYGON, HazardConstants.SHAPELY_MULTIPOLYGON]:
                    hazardEvent.set('shapeType', 'polygon')
                elif geometryType == HazardConstants.SHAPELY_LINE:
                    hazardEvent.set('shapeType', 'line')
                else:
                    hazardEvent.set('shapeType', 'point')

                    # Ensure the event has a pointID
                    if hazardEvent.get('pointID') is None:
                        hazardEvent.set('pointID', 'XXXXX')
                        
            #  Handle 'previewState' 
            #  IF we are previewing an 'ended' state, temporarily set the state as 'ended'
            #   so that the VTEC processing will be done correctly
            if hazardEvent.get('previewState') == 'ended':
                hazardEvent.setState('ENDED')
            newHazardEvents.append(hazardEvent)
 
        return newHazardEvents
    
    def getHazardMetaData(self, segment) :
        '''
        @param: eventInfo
        ''' 
        # Get meta data for this segment
        #  May need to get multiple hazardEvents and meta data
        metaDataList = []
        segmentEvents = self.getSegmentHazardEvents([segment])
        for hazardEvent in segmentEvents:
            phen = hazardEvent.getPhenomenon()   
            sig = hazardEvent.getSignificance()  
            subType = hazardEvent.getSubtype()  
            criteria = {'dataType':'hazardMetaData_filter',
                    'filter':{'phen':phen, 'sig':sig, 'subType':subType}
                    }
            metaData = self.bridge.getData(json.dumps(criteria))
            
            if type(metaData) is not types.ListType:
                metaData = metaData.execute(hazardEvent, {})
            metaDataList.append((metaData, hazardEvent))
            
        return metaDataList, segmentEvents
        
    def _saveVTEC(self, hazardEvents):
        '''
        if issuing: 
            For each VTEC Engine generated in the product, save the vtec records 
        '''        
        if str(self._issueFlag) == 'True':
            for wrapper in self._wrappers:
                wrapper.mergeResults() 
            self.logger.info(self._productCategory + ' Saving VTEC')
            # Handle Ended eventIDs 
            # Set the state to 'ended' for events that are completely canceled or expired.
            # Note that for some long-fused hazards e.g. FA.A, one eventID could be
            # associated with both a CAN and a NEW and we do not want to change the 
            # state to 'ended'.
            for hazardEvent in hazardEvents:
                vtecCodes = hazardEvent.get('vtecCodes', [])
                if ('CAN' in vtecCodes or 'EXP' in vtecCodes) and not ['NEW', 'CON', 'EXA', 'EXT', 'EXB', 'UPG', 'ROU'] in vtecCodes:
                    hazardEvent.setState('ENDED')
                    
    def checkTestMode(self, sessionDict, str):
        # testMode is set, then we are in product test mode.
        # modify the str to have beginning and ending TEST indication.
        if sessionDict.get('testMode', 0):
            return 'TEST...' + str + '...TEST'
        elif sessionDict.get('experimentalMode', 0):
            return 'EXPERIMENTAL...' + str
        else:
            return str               
                            
    def getSegmentHazardEvents(self, segments, hazardEventList=None):
        '''
        @param segments: List of segments
        @param inputHazardEvents: Set of hazardEvents 
        @param eventIDs: The ids of the hazardEvents to retrieve from the input set
        @return: Return a list of hazardEvents
        '''
        #  Each segment lists eventIDs, so collect those and then use
        #  getHazardEvents to get the hazardEvents.
        if not hazardEventList:
            hazardEventList = self._generatedHazardEvents
        
        eventIDs = []
        for segment in segments:
            ugcs, ids = segment
            eventIDs += ids
        hazardEvents = []
        for eventID in eventIDs:
            found = False
            for hazardEvent in hazardEventList:
                if hazardEvent.getEventID() == eventID:
                    hazardEvents.append(hazardEvent)
                    found = True
            if not found:
                # Must retrieve this hazard event for automatic cancellation
                mode = self._sessionDict.get('hazardMode','PRACTICE').upper()
                hazardEvent = HazardDataAccess.getHazardEvent(str(eventID), mode)
                # Initialize the product-specific information
                hazardEvent.removeHazardAttribute('expirationTime');
                hazardEvent.removeHazardAttribute('vtecCodes');
                hazardEvent.removeHazardAttribute('etns');
                hazardEvent.removeHazardAttribute('pils');                
                hazardEvents.append(hazardEvent)  
                hazardEventList.append(hazardEvent)              
        return hazardEvents

    def getMetadataItemForEvent(self, hazardEvent, metaData, fieldName):
        '''
        Translates the entries from the Hazard Information Dialog into product strings.
        @param hazardEvent: hazard event with user choices
        @param metaData:  dictionary specifying information to be entered through the Hazard Information Dialog
        @param fieldName: key field in the dictionaries, e.g. 'cta'

        @return the associated productString.  If a productString is not given, return the displayString.
            If no value is specified in the hazardEvent, return empty string.
        
        For example, In the Hazard Information Dialog, there is a field for specifying the Flood Severity.  
        This is specified in the Meta Data for certain flood type hazards.  The key field would be 
        'immediateCause', the user-entered value might be 'ER (Excessive Rainfall)' and the productString 
        returned would then be 'ER'.
        '''   
        
        value = hazardEvent.get(fieldName) 
        if not value:
            return '' 
        if type(value) is types.ListType:
            returnList = []
            for val in value:
                metaStr = self.getMetaDataValue(metaData, fieldName, val) 
                if metaStr: 
                    returnList.append(metaStr)
            return returnList
        else:
            return self.getMetaDataValue(metaData, fieldName, value)

    def getMetaDataValue(self, metaData, fieldName, value):                     
        '''
        Given a value, return the corresponding productString (or displayString) from the metaData. 
        @param metaData:  dictionary specifying information to be entered through the Hazard Information Dialog
        @param fieldName: key field in the dictionaries, e.g. 'cta'
        @param value: chosen value for the key field

        @return the associated productString.  If a productString is not given, return the displayString.
            If no value is specified in the hazardEvent, return empty string.
        '''    
        for widget in metaData:
            if widget.get('fieldName') == fieldName:
                for choice in widget.get('choices'):
                    if choice.get('identifier') == value or choice.get('displayString') == value:
                        returnVal = choice.get('productString')
                        if returnVal is None:
                            returnVal = choice.get('displayString')
                        returnVal = returnVal.replace('  ', '')
                        returnVal = returnVal.replace('\n', ' ')
                        returnVal = returnVal.replace('</br>', '\n')
                        return returnVal
        return ''
                 
    def getAttributionPhrase(self, vtecRecord, areaPhrase, issueTime, testMode, wfoCity, lineLength=69):
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
                headPhrase = '* ' + hazName + ' FOR ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'CON':
                attribution = 'THE ' + hazName + ' CONTINUES FOR'
                headPhrase = '* ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'EXA':
                attribution = nwsPhrase + 'EXPANDED THE'
                headPhrase = '* ' + hazName + ' TO INCLUDE ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'EXT':
                attribution = 'THE ' + hazName + ' IS NOW IN EFFECT FOR' 
                headPhrase = '* ' + areaPhrase + '.'
                    
            elif vtecRecord['act'] == 'EXB':
                attribution = nwsPhrase + 'EXPANDED THE'
                headPhrase = '* ' + hazName + ' TO INCLUDE ' + areaPhrase + '.'
    
            elif vtecRecord['act'] == 'CAN':
                attribution = 'THE ' + hazName + \
                   ' FOR ' + areaPhrase + ' HAS BEEN CANCELLED. ' 
    
            elif vtecRecord['act'] == 'EXP':
                expTimeCurrent = issueTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'THE ' + hazName + \
                      ' FOR ' + areaPhrase + ' HAS EXPIRED. '
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, expTimeCurrent)
                   attribution = 'THE ' + hazName + \
                      ' FOR ' + areaPhrase + ' WILL EXPIRE ' + timeWords + \
                      '. '

        if headPhrase is not None:
            headPhrase = self._tpc.indentText(headPhrase, indentFirstString='',
              indentNextString='  ', maxWidth=lineLength,
              breakStrings=[' ', '-', '...'])
        else:
            headPhrase = ''

        return attribution, headPhrase
    
    def getHazardTimePhrases(self, vtecRecord, issueTime, lineLength=69):
        '''
        LATE MONDAY NIGHT
        '''
        endTimePhrase = self._tpc.hazardTimePhrases(vtecRecord, issueTime, prefixSpace=False)
        endTimePhrase = self._tpc.substituteBulletedText(endTimePhrase, 'TIME IS MISSING', 'DefaultOnly', lineLength)
        return endTimePhrase
    
    def getPointPhrase(self, hazardEvent, metaData, lineLength=69):
        # Add in the point information                
        '''
        * AT 4:00 AM TUESDAY THE STAGE WAS 32.2 FEET
        * MODERATE FLOODING IS POSSIBLE. 
        * FLOOD STAGE IS 35.0FEET 
        * FORECAST...FLOOD STAGE MAY BE REACHED BY TUESDAY AM
        '''                
        stageTime = hazardEvent.getStartTime()  # Use start time for now -- '8:45 AM Monday'
        timeOfStage = self._tpc.getFormattedTime(time.mktime(stageTime.timetuple()), '%I:%M %p %A', shiftToLocal=True, stripLeading=True).upper() 
        currentStage = hazardEvent.get('currentStage')
        if currentStage is not None:
            stageHeight = `int(float(currentStage))` + ' feet'
            stagePhrase = '* At ' + timeOfStage + ' the stage was ' + stageHeight + '\n'
        else:
            stagePhrase = ''
                
        severity = self.getMetadataItemForEvent(hazardEvent, metaData, 'floodSeverity')
        if severity is not None:
            if severity != '':
                severity = severity + ' '
            severityPhrase = '* ' + severity + 'Flooding is possible. \n'
        else:
            severityPhrase = ''
                    
        floodStage = hazardEvent.get('floodStage')
        if floodStage is not None:
            floodStage = `int(float(floodStage))`
            floodStagePhrase = '* Flood stage is ' + floodStage + ' feet \n'
        else:
            floodStagePhrase = ''
                
        crest = hazardEvent.get('crest')
        if crest is not None:
            try:
                crestTime = self._tpc.getFormattedTime(int(crest) / 1000, '%A %p', shiftToLocal=True, stripLeading=True).upper()  # 'Monday Morning'
                crestPhrase = '* Forecast...Flood stage may be reached by ' + crestTime + '\n'
            except:
                crestPhrase = ''
        else:
            crestPhrase = ''                               
        pointPhrase = stagePhrase + severityPhrase + floodStagePhrase + crestPhrase + '\n'
        return pointPhrase
    
        
    def getBasisPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69):
        # Basis bullet
        
        # Logic that will contribute to user edited text retrieval
#         if vtecRecord['act'] == 'NEW' and canVtecRecord:
#             capText = canVtecRecord.get('prevText', None)
#         else:
#             capText = vtecRecord.get('prevText', None)
        defaultBasis = {
            'NEW': ('BASIS FOR THE WATCH', 'Always'),
            'CON': ('DESCRIBE CURRENT SITUATION', 'DefaultOnly'),
            'EXT': ('BASIS FOR EXTENDING THE WATCH', 'DefaultOnly'),
            'EXB': ('BASIS FOR EXPANSION OF THE WATCH', 'DefaultOnly'),
            'EXA': ('BASIS FOR EXPANSION OF THE WATCH', 'DefaultOnly'),
            'CAN': ('BASIS FOR CANCELLATION OF THE WATCH', 'DefaultOnly'),
            }
        basis = self.getMetadataItemForEvent(hazardEvent, metaData, 'basis')
        basisLocation = self.getMetadataItemForEvent(hazardEvent, metaData, 'basisLocation')
        basis = basis.replace('!** LOCATION **!', basisLocation)
        default, framing = defaultBasis[vtecRecord['act']] 
        basisPhrase = self._tpc.substituteBulletedText(basis, default, framing, lineLength)            
        return basisPhrase
    
    def getImpactsPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69):       
        # Impacts bullet
        if (vtecRecord['act'] == 'NEW' and canVtecRecord):  # or multRecords:
            framing = 'Always'
        else:
            framing = 'DefaultOnly'
        impacts = self.getMetadataItemForEvent(hazardEvent, metaData, 'impacts')
        impactsPhrase = self._tpc.substituteBulletedText(impacts,
            '(OPTIONAL) POTENTIAL IMPACTS OF FLOODING', framing, lineLength)        
        return impactsPhrase
    
    def getCTAsPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69):
        ctas = self.getMetadataItemForEvent(hazardEvent, metaData, 'cta')                
        return ctas
 
    def getCountyInfoForEvent(self, hazardEvent) :
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
        if timeArg == None :
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
        if timeArg == None :
            return None
        if isinstance(timeArg, str) or isinstance(timeArg, unicode) :
            timeArg = long(timeArg)
        if timeArg < 100000000000:
            timeArg = timeArg * 1000
        return timeArg 
            
    #############################################################################
    #  Formatting for Short-fused products
    #  Except for the first method, these rely on Storm Track Recommender information
    #       which will be provided in PV2
    #  These methods will be re-written in PV2
     #############################################################################
   
    def formatPolygonForEvent(self, hazardEvent):
        for polygon in self._extractPolygons(hazardEvent):
            polyStr = 'LAT...LON'
            # 3 points per line
            pointsOnLine = 0
            for lon,lat in polygon:              
                if pointsOnLine == 3:
                    polyStr += '\n         '
                # For end of Aleutians
                if lat > 50 and lon > 0 : 
                    lon = 360 - lon
                elif lon < 0 :
                    lon = -lon
                lon = (int)(100 * lon + 0.5)
                if lat < 0 :
                    lat = -lat
                lat = (int)(100 * lat + 0.5)
                polyStr += ' ' + str(lat) + ' ' + str(lon)
                pointsOnLine += 1
            return polyStr + '\n' 
        return ''
#         for shape in hazardEvent.get('shapes'):
#             if shape.get('shapeType') == 'polygon':
#                 polyStr = 'LAT...LON'
#                 points = shape.get('points')
#                 # 3 points per line
#                 pointsOnLine = 0
#                 for point in points:              
#                     if pointsOnLine == 3:
#                         polyStr += '\n         '
#                     lon, lat = point
#                     # For end of Aleutians
#                     if lat > 50 and lon > 0 : 
#                         lon = 360 - lon
#                     elif lon < 0 :
#                         lon = -lon
#                     lon = (int)(100 * lon + 0.5)
#                     if lat < 0 :
#                         lat = -lat
#                     lat = (int)(100 * lat + 0.5)
#                     polyStr += ' ' + str(lat) + ' ' + str(lon)
#                     pointsOnLine += 1
#                 return polyStr + '\n'
#         return ''
    
    def formatTimeMotionLocationForEvent(self, hazardEvent) :
        # Time Motion Location
        clientid = hazardEvent.get('clientid')
        if clientid == None :
            return None
        # Need to get the storm motion for PV3
        # Stubbed for PV2
        return None
    
#         try :
#             tmpEventTime = 0
#             for shape1 in hazardEvent['shapes'] :
#                 et1 = self.processTime(shape1.get('pointID'))
#                 if et1 != None :
#                     if et1 > tmpEventTime :
#                         tmpEventTime = et1
#             if tmpEventTime == 0 :
#                 tmpEventTime = self.processTime(hazardEvent['startTime'])
#             inJson = '{ 'action' : 'state', ' + \
#                        ''times' : ['+str(tmpEventTime)+'], ' + \
#                        ''id' : ''+clientid+'/latest' }'
#             outData = json.loads(myJT.transaction(inJson))
#             frame = outData['frameList'][0]
#             speed = frame['speed']
#             bearing = frame['bearing']
#             shape = frame['shape']
#             timeMotionLocationStr = 'TIME...MOT...LOC '
#             timeMotionLocationStr += self._tpc.getFormattedTime(\
#                self.processTimeForGetFormattedTime(tmpEventTime), \
#                    '%H%MZ ', shiftToLocal=0, stripLeading=0).upper()
#             timeMotionLocationStr += str(int(bearing + 0.5)) + 'DEG ' + str(int(speed + 0.5)) + 'KT'
#             a = 2
#             for onept in shape :
#                 a = a - 1
#                 if a < 0 :
#                     a = 3
#                     timeMotionLocationStr += '\n           '
#                 lat = onept[1]
#                 lon = onept[0]
#                 if lat > 50 and lon > 0 :  # For end of Aleutians
#                     lon = 360 - lon
#                 elif lon < 0 :
#                     lon = -lon
#                 lon = (int)(100 * lon + 0.5)
#                 if lat < 0 :
#                     lat = -lat
#                 lat = (int)(100 * lat + 0.5)
#                 timeMotionLocationStr += ' ' + str(lat) + ' ' + str(lon)
#             return timeMotionLocationStr
#         except :
#             return None
#         return None

    def descMotionForEvent(self, hazardEvent, useMph=True, \
                           still='STATIONARY', slow='NEARLY STATIONARY',
                           lead='MOVING', trail='',
                           minSpd=2.5, round=5.0) :
        clientid = hazardEvent.get('clientid')
        if clientid == None :
            return None
        return None
        # Need to get the storm motion for PV3
        # Stubbed for PV2
#         try :
#             tmpEventTime = 0
#             for shape1 in hazardEvent['shapes'] :
#                 et1 = self.processTime(shape1.get('pointID'))
#                 if et1 != None :
#                     if et1 > tmpEventTime :
#                         tmpEventTime = et1
#             if tmpEventTime == 0 :
#                 tmpEventTime = self.processTime(hazardEvent['startTime'])
#             inJson = '{ 'action' : 'state', ' + \
#                        ''times' : ['+str(tmpEventTime)+'], ' + \
#                        ''id' : ''+clientid+'/latest' }'
#             outData = json.loads(myJT.transaction(inJson))
#             frame = outData['frameList'][0]
#             speed = frame['speed']
#             if speed < 0 :
#                 return None
#             if speed == 0 :
#                 return still
#             if useMph :
#                 speed *= 1.16
#             if speed < minSpd :
#                 return slow
#             bearing = 45 * (int)((frame['bearing'] + 22.5) / 45)
#             if bearing == 45 :
#                 bearing = 'SOUTHWEST '
#             elif bearing == 90 :
#                 bearing = 'WEST '
#             elif bearing == 135 :
#                 bearing = 'NORTHWEST '
#             elif bearing == 180 :
#                 bearing = 'NORTH '
#             elif bearing == 225 :
#                 bearing = 'NORTHEAST '
#             elif bearing == 270 :
#                 bearing = 'EAST '
#             elif bearing == 315 :
#                 bearing = 'SOUTHEAST '
#             else :
#                 bearing = 'SOUTH '
#             speed = round * int((speed + round / 2) / round)
#             movStr = bearing + 'AT ' + str(int(speed))
#             if useMph :
#                 movStr += ' MPH'
#             else :
#                 movStr += ' KNOTS'
#             if len(lead) > 0 :
#                 movStr = lead + ' ' + movStr
#             if len(trail) > 0 :
#                 movStr += ' ' + trail
#             return movStr
#         except :
#             return None
#         return None
 
    def descWxLocForEvent(self, hazardEvent,
             noevent='FROM HEAVY RAIN. THIS RAIN WAS LOCATED', \
             point='FROM A THUNDERSTORM. THIS STORM WAS LOCATED', \
             line='FROM A LINE OF THUNDERSTORMS. THESE STORMS WERE LOCATED', \
             lead='', \
             trail='over the warned area') :
        clientid = hazardEvent.get('clientid')
        if clientid == None :
            if lead == '-' :
                return noevent
            wxLoc = ''
            if len(lead) > 0 :
                wxLoc = lead + ' '
            wxLoc += noevent
            if len(trail) > 0 :
                wxLoc += ' ' + trail
            return wxLoc
        # Need to get the storm motion for PV3
        # Stubbed for PV2
        return None
#         try :
#             tmpEventTime = 0
#             for shape1 in hazardEvent['shapes'] :
#                 et1 = self.processTime(shape1.get('pointID'))
#                 if et1 != None :
#                     if et1 > tmpEventTime :
#                         tmpEventTime = et1
#             if tmpEventTime == 0 :
#                 tmpEventTime = self.processTime(hazardEvent['startTime'])
#             inJson = '{ 'action' : 'state', ' + \
#                        ''times' : ['+str(tmpEventTime)+'], ' + \
#                        ''id' : ''+clientid+'/latest' }'
#             outData = json.loads(myJT.transaction(inJson))
#             frame = outData['frameList'][0]
#             shape = frame['shape']
#             if lead == '-' :
#                 if len(shape) <= 1 :
#                     return point
#                 else :
#                     return line
#             wxLoc = ''
#             if len(lead) > 0 :
#                 wxLoc = lead + ' '
#             if len(shape) <= 1 :
#                 wxLoc += point
#             else :
#                 wxLoc += line
#             if len(trail) > 0 :
#                 wxLoc += ' ' + trail
#             return wxLoc
#         except :
#             pass
#         wxLoc = ''
#         if len(lead) > 0 :
#             wxLoc = lead + ' '
#         wxLoc += noevent
#         if len(trail) > 0 :
#             wxLoc += ' ' + trail
#         return wxLoc

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

