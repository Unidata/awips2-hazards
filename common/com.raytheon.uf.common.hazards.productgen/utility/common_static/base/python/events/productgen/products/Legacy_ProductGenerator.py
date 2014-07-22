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
    Apr 20, 2014   2925      Chris.Golden        Changed to work with new hazard event metadata.
    May 06, 2014   1328      jramer              Remove reference to deprecated MapInfo class.
    @author Tracy.L.Hansen@noaa.gov
'''

import ProductTemplate

from Bridge import Bridge
from LocalizationInterface import *

from HazardServicesGenericHazards import HazardServicesGenericHazards
from TextProductCommon import  TextProductCommon
from ProductParts import ProductParts
from RiverForecastPoints import RiverForecastPoints

import logging, UFStatusHandler
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
from com.raytheon.uf.common.time import SimulatedTime
   
class Prod:
    pass

class ProdSegment:
    def __init__(self, segment, vtecRecords):
        self.segment = segment
        self.vtecRecords = vtecRecords
        self.vtecRecords_ms = vtecRecords
        
class ProdSection:
    pass

class ProductSegmentGroup:
    def __init__(self, productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn=None, formatPolygon=None, actions=[]):
        self.productID = productID
        self.productName = productName
        self.geoType = geoType
        self.vtecEngine = vtecEngine
        self.mapType = mapType
        self.segmented = segmented
        self.productSegments = productSegments
        self.etn = etn
        self.formatPolygon = formatPolygon
        self.actions = actions
        
    def addProductSegment(self, productSegment):
        self.productSegments.append(productSegment)
        
    def setProductParts(self, productParts):
        self.productParts = productParts

class Product(ProductTemplate.Product):
 
    def __init__(self):  
        '''
        General Note:  All times are in milliseconds unless designated 
        otherwise e.g. issueTime_secs.
        
        '''  
        #self.initialize()
        
    def createProduct(self):
        return Prod()
    def createProductSegment(self, segment, vtecRecords):
        return ProdSegment(segment, vtecRecords)
    def createProductSection(self):
        return ProdSection()
    def createProductSegmentGroup(self, productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn=None, formatPolygon=None):
        return ProductSegmentGroup(productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn, formatPolygon)        
                   
    def initialize(self):      
        self._gh = HazardServicesGenericHazards()
        
        self.bridge = Bridge()        
        self._hazardTypes = self.bridge.getHazardTypes()         
        self._areaDictionary = self.bridge.getAreaDictionary()
        self._cityLocation = self.bridge.getCityLocation()
        self._siteInfo = self.bridge.getSiteInfo()
        
        self._tpc = TextProductCommon()
        self._tpc.setUp(self._areaDictionary)        
        self._pp = ProductParts()
        self._rfp = RiverForecastPoints()

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
        self._testMode = self._sessionDict.get('testMode', 0)

        self._lineLength = 69
        self._upperCase = True
         
        # Set up issue time strings       
        self._ddhhmmTime = self._tpc.getFormattedTime(
              self._issueTime_secs, '%d%H%M', shiftToLocal=True, stripLeading=False)
        self._timeLabel = self._tpc.getFormattedTime(
              self._issueTime_secs, '%I%M %p %Z %a %b %e %Y',
              shiftToLocal=True, stripLeading=True)
              
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
        
        self._preProcessHazardEvents(self._inputHazardEvents)
        
    def _preProcessHazardEvents(self, hazardEvents):
        '''
        Can be overridden to preprocess the hazard events
        For example, the Immediate Cause is derived from the Hydrologic Cause for
        an FF.W.NonConvective and needs to be set prior to VTEC processing
        
        @param hazardEvents: hazard events
        '''
        pass

                
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
            productParts = productSegmentGroup.productParts 
            productDict['productParts'] = productParts                                         
            self._pp._processProductParts(self, productDict, productSegmentGroup, productParts)
            self._wrapUpProductDict(productDict)
            productDicts.append(productDict)
            
        # If issuing, save the VTEC records for legacy products       
        self._saveVTEC(self._generatedHazardEvents) 
        # Note: these print statements are left here for debugging
        # They will be useful for Focal Points as they are overriding product generators.
#         print '\nPGT Output dictionaries'
#         for productDict in productDicts:
#             self._printDict(productDict)
#         self.flush()            
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
            self._pointEvents, self._point_productSegments
            self._areaEvents,  self._area_productSegments
            
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
        self._point_productSegments = []
        self._area_productSegments = []
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
            productSegments = []
            for segment in segments:
                vtecRecords = self.getVtecRecords(segment)
                productSegments.append(self.createProductSegment(segment, vtecRecords))
            if geoType == 'point':  
                self._point_productSegments = productSegments
                self._pointVtecEngine = self._vtecEngine
            else:                     
                self._area_productSegments = productSegments
                self._areaVtecEngine = self._vtecEngine                  
        return self._point_productSegments + self._area_productSegments
            
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
        self._product = self.createProduct()
              
        self._product.productID = productSegmentGroup.productID 
        self._getProductInfo(self._product, self._siteID)
        if self._areaName != '':
            self._areaName = ' FOR ' + self._areaName + '\n'
        self._product.geoType = productSegmentGroup.geoType
        self._product.vtecEngine = productSegmentGroup.vtecEngine
        self._product.mapType = productSegmentGroup.mapType
        self._product.segmented = productSegmentGroup.segmented
        self._product.formatPolygon = productSegmentGroup.formatPolygon
        self._product.timeZones = []
        
        # Fill in product dictionary information
        productDict = collections.OrderedDict()
        productDict['productID'] = self._product.productID
        return productDict

    def _getProductInfo(self, product, siteID): 
        '''
         Get Product Info given siteID and product ID
         @param siteID: The site identifier, e.g. OAX
         @param productID: The product identifier, e.g. FFA
        '''
        #
        # Retrieve the record from the afos_to_awips table
        # for which the nnn and xxx portions of the afosid
        # correspond to the productID and siteID, respectively.
        a2a = QueryAfosToAwips(product.productID, siteID)        
        product.wmoID = a2a.getWMOprod()  # e.g. WUUS53
        product.CCC = a2a.getCCC()  # e.g. OMA         
        # Product PIL, e.g. SVRTOP
        product.pil = product.productID + siteID
        # Product ID for transmitting to AWIPS WAN, e.g. KTOPSVRTOP  
        product.awipsWANPil = a2a.getAwipsWANpil()            
        # Product ID for storing to AWIPS text database, e.g. TOPSVRTOP  
        product.textdbPil = a2a.getTextDBpil() 

    def _wrapUpProductDict(self, productDict):    
        productDict['sentTimeZ'] = self._convertToISO(self._issueTime)
        productDict['sentTimeZ_datetime'] = self._convertToDatetime(self._issueTime)
        productDict['sentTimeLocal'] = self._convertToISO(self._issueTime, local=True)
        productDict['timeZones'] = self._product.timeZones
        self._addToProductDict(productDict)
        return productDict
    
    
    ######################################################
    #  Product Part Methods 
    # 
    #    def _methodName(self, productDict, productSegmentGroup, arguments=None):
    #        
    ######################################################
        

    ################# Product Level
    
    def _setUp_product(self, productDict, productSegmentGroup, arguments=None):
        pass
              
    def _wmoHeader(self, productDict, productSegmentGroup, arguments=None):
        headerDict = collections.OrderedDict()
        headerDict['TTAAii'] = self._product.wmoID
        headerDict['originatingOffice'] = self._backupFullStationID  # Will be siteID if not in backup mode
        headerDict['productID'] = self._product.productID
        headerDict['siteID'] = self._siteID
        headerDict['wmoHeaderLine'] = self._product.wmoID + ' ' + self._fullStationID + ' ' + self._ddhhmmTime
        headerDict['awipsIdentifierLine'] = self._product.productID + self._siteID
        productDict['wmoHeader'] = headerDict

    def _wmoHeader_noCR(self, productDict, productSegmentGroup, arguments=None):
        self._wmoHeader(productDict, productSegmentGroup, arguments=None)

    def _easMessage(self, productDict, productSegmentGroup, arguments=None):
        productDict['easActivationRequested'] = 'true' 
        
    def _productHeader(self, productDict, productSegmentGroup, arguments=None):
        productDict['disclaimer'] = 'This XML wrapped text product should be considered COMPLETELY EXPERIMENTAL. The National Weather Service currently makes NO GUARANTEE WHATSOEVER that this product will continue to be supplied without interruption. The format of this product MAY CHANGE AT ANY TIME without notice.'
        productDict['senderName'] = 'NATIONAL WEATHER SERVICE ' + self._wfoCityState
        self._productName = self.checkTestMode(
                self._sessionDict, productSegmentGroup.productName + self._areaName)
        productDict['productName'] = self._productName
        productDict['issuedByString'] = self.getIssuedByString()

    def _overviewHeadline_area(self, productDict, productSegmentGroup, productSegments):
        productDict['overviewHeadline'] = 'Overview Headline'
        
    def _overviewHeadline_point(self, productDict, productSegmentGroup, productSegments):
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
        new_ext_productSegments = []
        con_productSegments = []
        can_exp_productSegments = [] 
        for productSegment_tuple in productSegments:
            segment, vtecRecords = productSegment_tuple             
            for vtecRecord in vtecRecords:
                action = vtecRecord.get('act')
                if action in ('NEW','EXT'):
                    new_ext_productSegments.append(self.createProductSegment(segment, vtecRecords))
                elif action == 'CON':
                    con_productSegments.append(self.createProductSegment(segment, vtecRecords))
                else:  # CAN, EXP
                    can_exp_productSegments.append(self.createProductSegment(segment, vtecRecords)) 
        
        overviewHeadline = ''
        for productSegments in  [
                        new_ext_productSegments,
                        con_productSegments,
                        can_exp_productSegments]: 
            if productSegments:
                overviewHeadline += self._getOverviewHeadline_point(productSegments) 
        productDict['overviewHeadline_point'] = overviewHeadline
        
    def _getOverviewHeadline_point(self, productSegments):
        '''
        ...The National Weather Service in Newport has issued a Flood Warning
        until 400 pm Wednesday for the following rivers in North Carolina...
        
            NEUSE RIVER AT KINSTON AFFECTING CRAVEN AND LENOIR COUNTIES.
            TAR RIVER AT GREENVILLE AFFECTING PITT COUNTY.
            ROANOKE RIVER NEAR WILLIAMSTON AFFECTING MARTIN COUNTY.
                
        '''
        # TODO -- IF not CAN, EXP and all vtecRecords have the same timing
        #         Add timing [(optional:) UNTIL <time/day phrase>4]
        #
        #( <Action> SEQ "NEW" )
        #bulletstr: <EventTime>.
        #condition: ( ( <Action> SEQ "NEW" ) AND ( <EventEndTime> GT <EventBeginTime> ) )
        #bulletstr: <EventTime>...OR UNTIL THE WARNING IS CANCELLED.
        #condition: ( ( <Action> SEQ "NEW" ) AND ( <EventEndTime> LE <EventBeginTime> ) )
        #bulletstr: <EventTime>.
        #
        locationPhrases = []  
        areaGroups = []
        # There could be multiple points sharing a VTEC code e.g. NEW     
        for productSegment in productSegments:
            segment = productSegment.segment
            vtecRecords = productSegment.vtecRecords      
            #  <River> <Proximity> <IdName> AFFECTING <LocCntyList> 
            
            points, eventIDs = segment
            # There is only one point per segment
            pointID = str(list(points)[0])
            eventID = str(list(eventIDs)[0])
            hazardEvent = self.getSegmentHazardEvents([segment])[0]
            ugcs = hazardEvent.get('ugcs', [])
            
            pointAreaGroups = self._tpc.getGeneralAreaList(ugcs, areaDict=self._areaDictionary)
            areaGroups += pointAreaGroups

            nameDescription, nameTypePhrase = self._tpc.getNameDescription(pointAreaGroups)
            affected = nameDescription + ' '+ nameTypePhrase
            riverName = self._rfp.getGroupName(pointID)
            proximity = self._rfp.getRiverPointProximity(pointID) 
            riverPointName = self._rfp.getRiverPointName(pointID)
            locationPhrases.append('       ' +riverName + ' ' + proximity + ' ' + riverPointName + ' affecting ' + affected + '.')  
                  
        locationPhrase = '\n'.join(locationPhrases)   
        areaGroups = self._tpc.simplifyAreas(areaGroups)
        states = self._tpc.getStateDescription(areaGroups)
        riverPhrase = 'the following rivers in ' + states
        # Use the last segment_vtecRecord_tuple (segment, vtecRecords)
        # since that information will apply to all points in the product
        vtecRecord = vtecRecords[0]       
        attribution, headPhrase = self.getAttributionPhrase(
                    vtecRecord, hazardEvent, riverPhrase, self._issueTime, self._testMode, self._wfoCity, endString = '...')                
        overview = '...'+attribution + headPhrase + '\n\n' + locationPhrase + '\n'
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
        productDict['rainFallStatement'] = 'The segments in this product are river forecasts for selected locations in the watch area.'
               
    def _callsToAction_productLevel(self, productDict, productSegmentGroup, arguments=None):
        '''
        We pass here because the Calls To Action will be gathered as we go through the segments.
        The 'wrapUp_product' method will then add the cta's to the product dictionary
        '''
        pass
   
    def _additionalInfoStatement(self, productDict, productSegmentGroup, arguments=None):
        productDict['additionalInfoStatement'] = 'ADDITIONAL INFORMATION IS AVAILABLE AT <Web site URL>.'
        
    def _nextIssuanceStatement(self, productDict, productSegmentGroup, arguments=None):
        productDict['nextIssuanceStatement'] = 'THE NEXT STATEMENT WILL BE ISSUED <time/day phrase>.' 
        
    def _floodPointTable(self, productDict, productSegmentGroup, arguments=None):
        # TODO Could be called at product level (no arguments or segment level with _productSegment argument
        pass
        
    def _wrapUp_product(self, productDict, productSegmentGroup, arguments=None):
        #productDict['ctas'] = productSegment.ctas
        pass
        
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
    def _setUp_segment(self, segmentDict, productSegmentGroup, productSegment_tuple):  
        segment, vtecRecords = productSegment_tuple 
        productSegment = self.createProductSegment(segment, vtecRecords)
        productSegment.metaDataList, productSegment.hazardEvents = self.getHazardMetaData(segment)
               
        # There may be multiple (metaData, hazardEvent) pairs in a segment 
        #   An example would be for a NPW product which had a Frost Advisory and a Wind Advisory in one segment
        # There will be a section for each 
        # 'segment' is (frozenset([list of ugcs or points]), frozenset([list of eventIDs])        
            
        # UGCs and Expire Time
        # Assume that the geoType is the same for all hazard events in the segment i.e. area or point
        hazardEvent = productSegment.hazardEvents[0]
        if hazardEvent.get('geoType') == 'area':
            productSegment.ugcs = list(segment[0])
        else:
            productSegment.ugcs = hazardEvent.get('ugcs', [])
            points, eventIDs = segment
            productSegment.pointID = str(list(points)[0])
        productSegment.ugcs.sort()  
        productSegment.cityInfo = self.getCityInfo(productSegment.ugcs, returnType='list')       
        productSegment.cityString = self._tpc.getTextListStr(productSegment.cityInfo)        
        productSegment.timeZones = self._tpc.hazardTimeZones(productSegment.ugcs)
        segmentDict['timeZones'] = productSegment.timeZones  

        for tz in productSegment.timeZones:
            if tz not in self._product.timeZones:
                self._product.timeZones.append(tz)
        productSegment.expireTime = self._tpc.getExpireTime(
                    self._issueTime, self._purgeHours, productSegment.vtecRecords_ms) 
        productSegment.ugcHeader_value = self._tpc.formatUGCs(productSegment.ugcs, productSegment.expireTime)
                                                        
        #
        # Generate the sections i.e. attribution bullets, calls-to-action, polygonText
        #
        segmentDict['areaType'] = self._product.geoType
        segmentDict['expireTime'] = self._convertToISO(productSegment.expireTime)
        segmentDict['expireTime_datetime'] = self._convertToDatetime(productSegment.expireTime) 
        # CAP Specific Fields        
        segmentDict['status'] = 'Actual' 
        segmentDict['CAP_areaString'] = self._tpc.formatUGC_namesWithState(productSegment.ugcs, separator='; ') 

        # Summary Headlines for the segment  -- this orders them  -- Used by Legacy after ugc header...
        #   Create and order the sections for the segment:
        #       (vtecRecord, sectionMetaData, sectionHazardEvent)      
        productSegment.summaryHeadlines_value, productSegment.headlines, productSegment.sections = self._tpc.getHeadlinesAndSections(
                    productSegment.vtecRecords_ms, productSegment.metaDataList, self._product.productID, self._issueTime_secs) 

        # Check for special case where a CAN/EXP is paired with a
        # NEW/EXA/EXB/EXT
        #
        includeText, includeFrameCodes, skipCTAs, forceCTAList = \
          self._gh.useCaptureText(productSegment.vtecRecords_ms)
        # find any 'CAN' with non-CAN for reasons of text capture
        canVtecRecord = None
        for vtecRecord in productSegment.vtecRecords_ms:
            if vtecRecord['act'] in ['CAN', 'EXP', 'UPG']:
                canVtecRecord = vtecRecord
                break  # take the first one
        productSegment.canVtecRecord = canVtecRecord

        # Calls to Action and polygonText are gathered and reported for all sections together
        productSegment.ctas = []
        productSegment.polygonText_value = ''
        segmentDict['polygons'] = self._createPolygonEntries(productSegment)
       
        if not productSegment.sections:
            self._setProductInformation(productSegment.vtecRecords_ms[0], productSegment.hazardEvents[0], productSegment)
        self._productSegment = productSegment

    def _ugcHeader(self, segmentDict, productSegmentGroup, arguments):
        segment = self._productSegment.segment
        vtecRecords = self._productSegment.vtecRecords
        segmentDict['ugcCodes'] = self._formatUGC_entries(self._productSegment)
        self._ugcHeader_value = self._tpc.formatUGCs(self._productSegment.ugcs, self._productSegment.expireTime)
        segmentDict['ugcHeader'] = self._ugcHeader_value
        self._tpc.setVal(segmentDict, 'displayUgcHeader', self._ugcHeader_value, displayable=True,
                         productCategory=self._productCategory, productID=self._product.productID) 
       
    def _vtecRecords(self, segmentDict, productSegmentGroup, arguments):
        segment = self._productSegment.segment
        vtecRecords = self._productSegment.vtecRecords
        segmentDict['vtecRecords'] = self._vtecRecordEntries(segment)
        self._tpc.setVal(segmentDict, 'displayVtecRecords', self._vtecRecordEntries(segment), displayable=True,
                         productCategory=self._productCategory, productID=self._product.productID) 

    def _areaList(self, segmentDict, productSegmentGroup, arguments):
         # Area String        
        self._tpc.setVal(segmentDict, 'areaList', self._tpc.formatUGC_names(self._productSegment.ugcs), displayable=True,
                         productCategory=self._productCategory, productID=self._product.productID) 
           
    def _cityList(self, segmentDict, productSegmentGroup, arguments):
        segment = self._productSegment.segment
        ids, eventIDs = segment
        cityList = []
        for city, ugcCity in self._productSegment.cityInfo:
            cityList.append(city)        
        self._tpc.setVal(segmentDict, 'cityList', cityList, editable=True, label='Included Cities', eventIDs=list(eventIDs), segment=segment,
                         productCategory=self._productCategory, productID=self._product.productID) 
 
    def _summaryHeadlines(self, segmentDict, productSegmentGroup, arguments):
        '''
         Summary Headlines for the segment  -- this orders them  -- 
           Used by Legacy after ugc    header...
           Create and order the sections for the segment:
               (vtecRecord, sectionMetaData, sectionHazardEvent) 
        '''     
        segmentDict['summaryHeadlines'] = self._productSegment.summaryHeadlines_value
        segmentDict['headlines'] = self._productSegment.headlines
            
    def _meaningOfStatement(self, segmentDict, productSegmentGroup, arguments):
        # TODO: Check for FA.A or FF.A  Flood Watch or Flash Flood Watch
        segmentDict['meaningOfStatement'] = 'A flood watch means that flooding is possible but not imminent in the watch area.\n'

    def _emergencyStatement(self, segmentDict, productSegmentGroup, arguments):
        '''
        Example:
        ...A FLASH FLOOD EMERGENCY FOR <geographic area>...
        '''
        segmentDict['emergencyStatement'] = '...A FLASH FLOOD EMERGENCY FOR <geographic area>...'
        
    def _basisAndImpactsStatement_segmentLevel(self, segmentDict, productSegmentGroup, arguments):
        segmentDict['basisAndImpactsStatement_segmentLevel'] = 'Current hydrometeorological situation and expected impacts \n'
        
    def _callsToAction(self, segmentDict, productSegmentGroup, arguments):
        segmentDict['callsToAction'] = self._productSegment.ctas

    def _polygonText(self, segmentDict, productSegmentGroup, arguments):
        segmentDict['polygonText'] = self._productSegment.polygonText_value

    def _timeMotionLocation(self, segmentDict, productSegmentGroup, arguments):
        segmentDict['timeMotionLocation'] = self._createTimeMotionLocationEntry()

    def _impactedLocations(self, segmentDict, productSegmentGroup, arguments):
        segment = self._productSegment.segment
        vtecRecords = self._productSegment.vtecRecords
        segmentDict['impactedLocations'] = self._createImpactedLocationEntries(segment)

    def _observations(self, segmentDict, productSegmentGroup, arguments):
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
    def _setUp_section(self, sectionDict, productSegmentGroup, arguments):
        productSegment_tuple, vtecRecord, formatArgs = arguments
        
        if not self._productSegment.sections:
            return

        # Find the section information for this section
        for section in self._productSegment.sections:
            sectionVtecRecord, sectionMetaData, sectionHazardEvent = section
            self._section = self.createProductSection()
            self._section.vtecRecord = sectionVtecRecord
            self._section.metaData = sectionMetaData
            self._section.hazardEvent = sectionHazardEvent
            self._section.action = sectionVtecRecord.get('act')
                                
        additionalInfo = self._section.hazardEvent.get('additionalInfo')
        self._section.floodMoving = False
        self._section.listOfCities = False 
        self._section.listOfDrainages = False
        if additionalInfo:
            if 'listOfCities' in additionalInfo:
                self._section.listOfCities = True 
            if 'listOfDrainages' in additionalInfo:
                self._section.listOfDrainages = True 
            if 'floodMoving' in additionalInfo:
                self._section.floodMoving = self._tpc.getProductStrings(
                            self._section.hazardEvent, self._section.metaData, 'additionalInfo', choiceIdentifier='floodMoving')
        self._section.areaPhrase = self.getAreaPhrase(self._productSegment, sectionMetaData, sectionHazardEvent)
        self._section.attribution, self._section.firstBullet = self.getAttributionPhrase(
                   self._section.vtecRecord, self._section.hazardEvent, self._section.areaPhrase, self._issueTime, self._testMode, self._wfoCity)
    
        # Format
        self._section.formatArgs = formatArgs
        if 'bulletFormat' in formatArgs:
            self._section.bulletFormat = formatArgs.get('bulletFormat')
        else:
            self._section.bulletFormat = 'bulletFormat_CR'
           
        sectionDict['description'] = ''
        self._setProductInformation(self._section.vtecRecord, self._section.hazardEvent, self._productSegment)

        # Process each part of the section
#       TODO 
#       Need to rectify this code taken from existing A2 to make 
#       sure we retain the functionality as it pertains to 
#       user edited text (Issue 1321)
#         if vtecRecord['act'] in ['CAN','EXP','UPG']:
#             aPhrase = areaPhraseShort
#             canVtecRecord = None
#         else:
#             aPhrase = areaPhrase
#         phrase = self.makeSection(vtecRecord, canVtecRecord, self._section.areaPhrase, self._product.geoType, sectionHazardEvent, self._metaDataList,
#                                         self._issueTime_secs, self._testMode, self._wfoCity)

        ctas = self.getCTAsPhrase(self._section.vtecRecord, self._section.hazardEvent, self._section.metaData)
        if ctas:
            self._productSegment.ctas += ctas

        if self._product.formatPolygon:
             self._productSegment.polygonText_value += self.formatPolygonForEvent(self._section.hazardEvent) + '\n'

       # CAP Specific Fields        
        infoDict = collections.OrderedDict()
        sectionDict['info'] = [infoDict]
        infoDict['category'] = 'Met'
        infoDict['responseType'] = self._section.hazardEvent.get('responseType', '')  # 'Avoid'
        infoDict['urgency'] = self._section.hazardEvent.get('urgency', '')  # 'Immediate'
        infoDict['severity'] = self._section.hazardEvent.get('severity', '')  # 'Severe' 
        infoDict['certainty'] = self._section.hazardEvent.get('certainty', '')  # 'Observed'
        infoDict['onset_datetime'] = self._section.hazardEvent.getStartTime() 
        infoDict['WEA_text'] = self._section.hazardEvent.get('WEA_Text', '')  # 'Observed'
        infoDict['pil'] = self._product.pil
        infoDict['sentBy'] = self._wfoCity
        infoDict['event'] = self._hazardTypes[self._section.hazardEvent.getHazardType()]['headline']
        endTime = self._section.hazardEvent.getEndTime() 
        if endTime: 
            infoDict['eventEndingTime_datetime'] = endTime

    def _emergencyHeadline(self, sectionDict, productSegmentGroup, arguments):
        # Check to see if emergencyHeadine is to be included
        hazardEvent = self._section.hazardEvent
        includeChoices = hazardEvent.get('include')
        print "includeChoices", includeChoices
        self.flush()
        if includeChoices and 'ffwEmergency' in includeChoices:
            location = hazardEvent.get('includeEmergencyLocation')
            if location is None:
                location = self._tpc.frame('Enter location')
            print "location", location
            self.flush()
            sectionDict['emergencyHeadline'] = '...Flash Flood Emergency for '+ location + '...\n'

    def _attribution(self, sectionDict, productSegmentGroup, arguments):
        attribution = self._section.attribution
        if attribution:
            if self._section.bulletFormat == 'bulletFormat_CR':
                attribution = self._section.attribution+'\n'
            self._tpc.setVal(sectionDict, 'attribution', attribution, 
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] += attribution + '\n'

    def _firstBullet(self, sectionDict, productSegmentGroup, arguments):
        if self._section.firstBullet:
            self._tpc.setVal(sectionDict, 'firstBullet', self._section.firstBullet, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] += self._section.firstBullet + '\n'

    def _timeBullet(self, sectionDict, productSegmentGroup, arguments):        
        timeBullet = self.getHazardTimePhrases(self._section.vtecRecord, self._section.hazardEvent, self._issueTime)
        self._tpc.setVal(sectionDict, 'timeBullet', timeBullet, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
        sectionDict['description'] += timeBullet + '\n'

    def _basisBullet(self, sectionDict, productSegmentGroup, arguments):
        basisBullet = self.getBasisPhrase(self._section.vtecRecord, self._section.hazardEvent, self._section.metaData)
        if basisBullet:
            self._tpc.setVal(sectionDict, 'basisBullet', basisBullet, editable=True, label='Basis', eventIDs=[self._section.hazardEvent.getEventID()],
                       segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] += basisBullet + '\n'
        
    def _impactsBullet(self, sectionDict, productSegmentGroup, arguments):
        impactsBullet = self.getImpactsPhrase(self._section.vtecRecord, self._section.hazardEvent, self._section.metaData)
        self._tpc.setVal(sectionDict, 'impactsBullet', impactsBullet, editable=True, label='Impacts', eventIDs=[self._section.hazardEvent.getEventID()],
                       segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
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
        basisAndImpactsStatement = 'Current hydrometeorological situation and expected impacts\n'
        self._tpc.setVal(sectionDict, 'basisAndImpactsStatement', basisAndImpactsStatement, editable=True, label='Basis and Impacts', eventIDs=[self._section.hazardEvent.getEventID()],
                       segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
        sectionDict['description'] += basisAndImpactsStatement + '\n'
        
    def _locationsAffected(self, sectionDict, productSegmentGroup, arguments):
        ''' 
            LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO SPARTA AND ROCKFORD.
        
            From nonConvectiveFlashFloodWarning.vm
            ##########################################################################
            ## Optional 4th bullet...comment out if not needed.
            ##########################################################################
            ## This first if loop will override the locations impacted statement
            ## with the site specific information in the 4th bullet.
            ##########################################################################
            #if(${sitespecSelected} == "YES")
            * ##
            ${addInfo} 
            
            ${scenario}
            
            ${ruleofthumb}
            
            ##########################################################################
            ## Continue with the regular 4th bullet information
            ##########################################################################
            #elseif(${list.contains(${bullets}, "pathcast")})
            * ##
            #if(${productClass}=="T")
            THIS IS A TEST MESSAGE. ##
            #end
            #pathCast("THE FLOOD WILL BE NEAR..." "THIS FLOODING" ${pathCast} ${otherPoints} ${areas} ${dateUtil} ${timeFormat} 0)
            
            #elseif(${list.contains(${bullets}, "listofcities")})
            * ##
            #if(${productClass}=="T")
            THIS IS A TEST MESSAGE. ##
            #end
            #### THE THIRD ARGUMENT IS A NUMBER SPECIFYING THE NUMBER OF COLUMNS TO OUTPUT THE CITIES LIST IN
            #### 0 IS A ... SEPARATED LIST, 1 IS ONE PER LINE, >1 IS A COLUMN FORMAT
            #### IF YOU USE SOMETHING OTHER THAN "LOCATIONS IMPACTED INCLUDE" LEAD IN BELOW, MAKE SURE THE
            #### ACCOMPANYING XML FILE PARSE STRING IS CHANGED TO MATCH!
            #locationsList("LOCATIONS IMPACTED INCLUDE..." "THIS FLOODING" 0 ${cityList} ${otherPoints} ${areas} ${dateUtil} ${timeFormat} 0)
            
            #end
            ############################ End of Optional 4th Bullet ###########################
            #if(${list.contains(${bullets}, "drainages")})
            #drainages(${riverdrainages})
            
            #end
            
            ## parse file command here is to pull in mile marker info
            ## #parse("mileMarkers.vm")
            
            #if(${list.contains(${bullets}, "floodMoving")})
            FLOOD WATERS ARE MOVING DOWN !**name of channel**! FROM !**location**! TO !**location**!. 
            THE FLOOD CREST IS EXPECTED TO REACH !**location(s)**! BY !**time(s)**!.
            
            #end
        '''
        
        locationsAffected = ''
        damOrLeveeName = self._section.hazardEvent.get('damName')
        if damOrLeveeName:
            damInfo = self._damInfo().get(damOrLeveeName)
            if damInfo:
                # Scenario
                scenario = self._section.hazardEvent.get('scenario')
                if scenario:
                    scenarios = damInfo.get('scenarios')
                    if scenarios:
                        scenarioText = scenarios.get(scenario)
                        if scenarioText:
                            locationsAffected += scenarioText + '\n'
                # Rule of Thumb
                ruleOfThumb = self._section.hazardEvent.get('ruleOfThumb')
                if ruleOfThumb:
                    locationsAffected += ruleOfThumb + '\n'
        # Locations impacted
        if self._section.listOfCities:
            locationsAffected += 'Locations impacted include...' + self._productSegment.cityString + '. '
        # Flood moving
        if self._section.floodMoving:
            locationsAffected += self._section.floodMoving + '. '
            
        if not locationsAffected:
             locationsAffected = 'Forecast path of flood and/or locations to be affected' + '\n'
        self._tpc.setVal(sectionDict, 'locationsAffected', locationsAffected, editable=True, label='Locations Affected', 
                         eventIDs=[self._section.hazardEvent.getEventID()],
                         segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
        sectionDict['description'] += locationsAffected + '\n'
        
        
    #  Point based
    
    def _attribution_point(self, sectionDict, productSegmentGroup, arguments):
        attribution = self._section.attribution
        if attribution:
            if self._section.bulletFormat == 'bulletFormat_CR':
                attribution = self._section.attribution+'\n'
            self._tpc.setVal(sectionDict, 'attribution_point', attribution, 
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] += attribution + '\n'

    def _firstBullet_point(self, sectionDict, productSegmentGroup, arguments):
        if self._section.firstBullet:
            self._tpc.setVal(sectionDict, 'firstBullet_point', self._section.firstBullet, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] +=  self._section.firstBullet + '\n'
                
    def _floodPointHeadline(self, sectionDict, productSegmentGroup, arguments):
        self._tpc.setVal(sectionDict, 'floodPointHeadline', 'Flood point headline', formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
    
    def _floodPointHeader(self, sectionDict, productSegmentGroup, arguments):
        self._tpc.setVal(sectionDict, 'floodPointHeader', 'Flood point header', formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
    
    def _observedStageBullet(self, sectionDict, productSegmentGroup, arguments):
        observedCategory = self._rfp.getObservedCategory(self._productSegment.pointID)
        if observedCategory < 0:
            bulletContent = 'There is no current observed data.'
        else:
            #AT <ObsTime> THE <StgFlowName> WAS <ObsStg> <StgFlowUnits>.
            #  ObsTime --> hhmm am/pm <day> time_zone
            observedTime_ms = self._rfp.getObservedTime(self._productSegment.pointID)
            stageFlowName = self._rfp.getStageFlowName(self._productSegment.pointID)
            observedStage, shefQualityCode = self._rfp.getObservedStage(self._productSegment.pointID)
            self._stageFlowUnits = self._rfp.getStageFlowUnits(self._productSegment.pointID)
            # 900 AM EDT FRIDAY 
            observedTime = self._getFormattedTime(observedTime_ms)
            bulletContent = 'At '+observedTime+ ' the '+stageFlowName+' was '+`observedStage`+' '+self._stageFlowUnits+'.'
        self._tpc.setVal(sectionDict, 'observedStageBullet', bulletContent, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 

    def _floodStageBullet(self, sectionDict, productSegmentGroup, arguments):
        floodStage = self._rfp.getFloodStage(self._productSegment.pointID)
        if floodStage != self._rfp.MISSING_VALUE:
            #FLOOD STAGE IS <FldStg> <StgFlowUnits>.
            bulletContent = 'Flood stage is '+`floodStage`+' '+self._stageFlowUnits+'.'
        else:
            bulletContent = ''
        self._tpc.setVal(sectionDict, 'floodStageBullet', bulletContent, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
    
    def _otherStageBullet(self, sectionDict, productSegmentGroup, arguments):
        # TODO
        sectionDict['otherStageBullet'] = ''
    
    def _floodCategoryBullet(self, sectionDict, productSegmentGroup, arguments):
        '''        
        condition: ( ( <ObsCat> EQ 0 ) AND ( <MaxFcstCat> GT 0 ) )
        bulletstr: <MaxFcstCatName> FLOODING IS FORECAST. 
        condition: ( ( <ObsCat> LT 0 ) AND ( <MaxFcstCat> GT 0 ) )
        bulletstr: <MaxFcstCatName> FLOODING IS FORECAST.
        
        condition: ( ( <ObsCat> GT 0 ) AND ( <MaxFcstCat> GT 0 ) )
        bulletstr: <ObsCatName> FLOODING IS OCCURRING &
        AND <MaxFcstCatName> FLOODING IS FORECAST.
        
        condition: ( ( <Action> SEQ "ROU" ) OR ( ( <ObsCat> EQ 0 ) AND ( <MaxFcstCat> LE 0 ) ) )
        bulletstr: NO FLOODING IS CURRENTLY FORECAST.
        '''
        observedCategory = self._rfp.getObservedCategory(self._productSegment.pointID)
        maxFcstCategory = self._rfp.getMaximumForecastCategory(self._productSegment.pointID)
        maxFcstCategoryName = self._rfp.getMaximumForecastCatName(self._productSegment.pointID)
        if observedCategory <= 0 and maxFcstCategory > 0:
            bulletContent = maxFcstCategoryName + ' flooding is forecast.'
        elif observedCategory > 0 and maxFcstCategory > 0:
            observedCategoryName = self._rfp.getObservedCategoryName(self._productSegment.pointID)
            bulletContent = observedCategoryName + ' flooding is occurring and '+maxFcstCategoryName+' flooding is forecast.'
        else:
            productSegment_tuple, vtecRecord, formatArgs = arguments
            action = vtecRecord.get('act')
            if action == 'ROU' or (observedCategory == 0 and maxFcstCategory < 0):
                bulletContent = 'No flooding is currently forecast.'
            else:
                bulletContent = '' 
        self._maxForecastCategory = maxFcstCategory
        self._tpc.setVal(sectionDict, 'floodCategoryBullet', bulletContent, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
    
    def _recentActivityBullet(self, sectionDict, productSegmentGroup, arguments):
        '''
        condition: ( <ObsCat> GT 0 )
        bulletstr: RECENT ACTIVITY...THE MAXIMUM RIVER STAGE IN THE 24 HOURS ENDING AT <ObsTime> WAS <MaxObsStg24> FEET.
        '''
        observedCategory = self._rfp.getObservedCategory(self._productSegment.pointID)
        bulletContent = ''
        if observedCategory > 0:
            observedTime_ms = self._rfp.getObservedTime(self._productSegment.pointID)
            observedTime = self._getFormattedTime(observedTime_ms)
            maxStage, shefQualityCode = self._rfp.getMaximum24HourObservedStage(self._productSegment.pointID)
            bulletContent = 'Recent Activity...The maximum river stage in the 24 hours ending at '+observedTime+' was '+`maxStage`+' feet. '            
        self._tpc.setVal(sectionDict, 'recentActivityBullet', bulletContent, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
    
    def _forecastStageBullet(self, sectionDict, productSegmentGroup, arguments):
        '''                        
        '''
        # From Mark Armstrong -- national baseline templates
        #    roundups2011.0331  -- 
        # TODO -- this is for FLW -- is this the same for FFA, FLS?
        bulletContent = ''
        
        # Gather information
        observedStage, shefCode = self._rfp.getObservedStage(self._productSegment.pointID)
        floodStage = self._rfp.getFloodStage(self._productSegment.pointID)
        maximumForecastStage, shefCode = self._rfp.getMaximumForecastStage(self._productSegment.pointID)
        maximumForecastTime_ms = self._rfp.getMaximumForecastTime(self._productSegment.pointID)
        if maximumForecastTime_ms != self._rfp.MISSING_VALUE:
            maximumForecastTime_str = self._getFormattedTime(maximumForecastTime_ms)
        else:
            maximumForecastTime_str = ' at time unknown' 
        forecastRiseAboveFloodStageTime_ms = self._rfp.getForecastRiseAboveFloodStageTime(self._productSegment.pointID)
        forecastRiseAboveFloodStageTime_str = self._getFormattedTime(maximumForecastTime_ms)
            
        #TODO  <HG,0,FF,X,NEXT>   ???
        #physicalElement = self._rfp.getPhysicalElement(self._productSegment.pointID, 'HG', '0', 'FF', 'X', 'NEXT')
        #physicalElementStage = physicalElement.getStage()
        #physicalElementTime = physicalElement.getTime()
        physicalElementStage = floodStage 
        physicalElementTime_str = ' stage time'
        
        forecastFallBelowFloodStageTime_ms = self._rfp.getForecastFallBelowFloodStageTime(self._productSegment.pointID)
        forecastFallBelowFloodStageTime_str = self._getFormattedTime(forecastFallBelowFloodStageTime_ms)
        stageFlowUnits = self._rfp.getStageFlowUnits(self._productSegment.pointID)
        
        forecastCrestStage = self._rfp.getForecastCrestStage(self._productSegment.pointID)
        stageTrend = self._rfp.getStageTrend(self._productSegment.pointID)

        # Create bullet content
        if self._section.action in ['NEW','CON','EXT']:
                    
            if observedStage == self._rfp.MISSING_VALUE:
                # FORECAST INFORMATION FOR NO OBS SITUATION    
                #  change made 3/17/2009 Mark Armstrong HSD
                #
                # condition: ( ( <ObsStg> EQ MISSING ) AND ( <MaxFcstStg> GE <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS FORECAST TO HAVE A MAXIMUM VALUE OF <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
                if maximumForecastStage >= floodStage:
                    bulletContent = 'The river is forecast to have a maximum value of '+`maximumForecastStage`+' '+stageFlowUnits+\
                      ' by '+maximumForecastTime_str+'. '
                #
                # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
                #  change made 3/17/2009 Mark Armstrong HSD
                #
                # condition: ( ( <ObsStg> EQ MISSING ) AND ( <MaxFcstStg> LT <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS FORECAST BELOW FLOOD STAGE WITH A MAXIMUM VALUE OF <MaxFcstStg> &
                # <StgFlowUnits> <MaxFcstTime>.
                elif maximumForecastStage < floodStage:
                    bulletContent = 'The river is forecast below flood stage with a maximum value of '+`maximumForecastStage`+' '+stageFlowUnits+\
                      ' by '+maximumForecastTime_str+'. '

            elif observedStage < floodStage:
                # Observed below flood stage/forecast to rise just to flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> EQ <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS EXPECTED TO RISE TO NEAR FLOOD STAGE <MaxFcstTime>.
                #
                if maximumForecastStage == floodStage:
                    bulletContent = 'The river is expected to rise to near flood stage by '+ maximumForecastTime_str
                    
                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/not falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND &
                # ( <HG,0,FF,X,NEXT> GT <FldStg> ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>.        
                #            
                elif physicalElementStage > floodStage and forecastFallBelowFloodStageTime == self._rfp.MISSING:
                    bulletContent = 'rise above flood stage by '+ forecastRiseAboveFloodStageTime_str + \
                        ' and continue to rise to near ' + `physicalElementStage` + ' '+stageFlowUnits + ' by '+stageTime+'. '

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has no crest
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> GT &
                # <FldStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <MaxFcstStg> <StgFlowUnits> BY <MaxFcstTime>. &
                # ADDITIONAL RISES ARE POSSIBLE THEREAFTER.        
                #                               
                elif maximumForecastStage > floodStage and physicalElementStage == self._rfp.MISSING_VALUE and +\
                    forecastFallBelowFloodStageTime == self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by '+forecastRiseAboveFloodStageTime_str +\
                       ' and continue to rise to near '+`maximumForecastStage`+' '+stageFlowUnits+' by '+\
                       maximumForecastTime_str+'. Additional rises are possible thereafter.'

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT &
                # <FldStg> ) AND ( <FcstFallFSTime> NE MISSING ) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY <HG,0,FF,X,NEXT,TIME>.&
                # THE RIVER WILL FALL BELOW FLOOD STAGE BY <FcstFallFSTime>.
                #
                elif physicalElementStage > floodStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by '+forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near '+ `physicalElementStage`+' '+stageFlowUnits+' by '+physicalElementTime_str + \
                       '. The river will fall below flood stage by '+forecastFallBelowFloodStageTime_str+'. '
            
            else: # observedStage >= floodStage:
                
                # Observed above flood stage/forecast continues above flood stage/no
                # crest in forecast time series
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> GT &
                # <ObsStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <MaxFcstStg> <StgFlowUnits> BY &
                # <MaxFcstTime>.  ADDITIONAL RISES MAY BE POSSIBLE THEREAFTER.
                #        
                if maximumForecastStage > observedStage and physicalElementStage == self._rfp.MISSING_VALUE and \
                     forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                     bulletContent = 'The river will continue rising to near '+ `maximumForecastStage`+' '+stageFlowUnits + \
                     ' by '+ maximumForecastTime_str + '. Additional rises may be possible thereafter. '
            
                # Observed above flood stage/forecast crests but stays above flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) &
                # AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME> THEN BEGIN FALLING.
                #
                elif physicalElementStage > observedStage and forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = 'The river will continue rising to near '+`physicalElementStage`+' '+stageFlowUnits+' by '+\
                        physicalElementTime_str+ ' then begin falling.'
                    
                # Observed above flood stage/forecast crests and falls below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING ) AND ( <FcstCrestStg> GT <ObsStg> ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>. THE RIVER WILL FALL BELOW FLOOD STAGE &
                # <FcstFallFSTime>.
                #
                elif physicalElementStage > observedStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE and \
                    forecastCrestStage > observedStage:
                    bulletContent = 'The river will continue rising to near '+`physicalElementStage`+' '+stageFlowUnits+' by '+\
                       forecastFallBelowFloodStageTime_ms+'. ' 
                        
                # Observed above flood stage/forecast continue fall/not below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "falling" ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO A STAGE OF <SpecFcstStg> <StgFlowUnits> BY &
                # <SpecFcstStgTime>.
                #
                elif maximumForecastStage <= observedStage and stageTrend == 'falling' and \
                    forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    # TODO Need SpecFcstStg and SpecFcstStgTime
                    bulletContent = ''
                    
                # Observed above flood stage/forecast is steady/not fall below flood stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "steady" ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL REMAIN NEAR <MaxFcstStg> <StgFlowUnits>.
                #
                elif maximumForecastStage <= observedStage and stageTrend == 'steady' and \
                    forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    bulletContent = 'The river will remain near '+`maximumForecastStage`+' '+stageFlowUnits+'. '
                    
                # Observed above flood stage/forecast continues fall to below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO BELOW FLOOD STAGE BY &
                # <FcstFallFSTime>.
                #
                elif maximumForecastStage <= observedStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = 'The river will continue to fall to below flood stage by '+forecastFallBelowFloodStageTime_str+'.'

        elif self._section.action in ['ROU']:                    
            #  FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
            #
            # condition: ( ( <Action> SEQ "ROU" ) AND ( <MaxFcstStg> NE MISSING ) )
            # bulletstr: FORECAST...THE RIVER WILL RISE TO NEAR <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
            #
            if maximumForecastStage != self._rfp.MISSING_VALUE:
                bulletContent = 'The river will rise to near '+`maximumForecastStage`+' '+stageFlowUnits+\
                      ' by '+maximumForecastTime_str+'. '                       
        self._tpc.setVal(sectionDict, 'forecastStageBullet', bulletContent, formatMethod=self._section.bulletFormat, formatArgs='Forecast...',
                         productCategory=self._productCategory, productID=self._product.productID) 

    def _forecastStageBullet_new(self, sectionDict, productSegmentGroup, arguments):
        '''                        
        '''
        # From Mark Armstrong -- national baseline templates
        #    roundups2011.0331  -- 
        # TODO -- this is for FLW -- is this the same for FFA, FLS?
        bulletContent = ''
        
        # Gather information
        observedStage, shefQualityCode = self._rfp.getObservedStage(self._productSegment.pointID)
        floodStage = self._rfp.getFloodStage(self._productSegment.pointID)
        
        primaryPE = self._rfp.getPrimaryPhysicalElement(self._productSegment.pointID)
        
        maximumForecastStage, shefQualityCode = self._rfp.getMaximumForecastLevel(self._productSegment.pointID, primaryPE)
        maximumForecastTime_ms = self._rfp.getMaximumForecastTime(self._productSegment.pointID)
        if maximumForecastTime_ms != self._rfp.MISSING_VALUE:
            maximumForecastTime_str = self._getFormattedTime(maximumForecastTime_ms)
        else:
            maximumForecastTime_str = ' at time unknown' 
        forecastRiseAboveFloodStageTime_ms = self._rfp.getForecastRiseAboveFloodStageTime(self._productSegment.pointID)
        forecastRiseAboveFloodStageTime_str = self._getFormattedTime(maximumForecastTime_ms)
            
        forecastCrest = self._rfp.getPhysicalElementValue(self._productSegment.pointID, 'HG', 0, 'FF', 'X', 'NEXT')
        forecastCrestTime = self._rfp.getPhysicalElementValue(self._productSegment.pointID, 'HG', 0, 'FF', 'X', 'NEXT', timeFlag=True)        
        physicalElementTime_str = ' stage time'
        
        forecastFallBelowFloodStageTime_ms = self._rfp.getForecastFallBelowFloodStageTime(self._productSegment.pointID)
        if forecastFallBelowFloodStageTime_ms:
            forecastFallBelowFloodStageTime_str = self._getFormattedTime(forecastFallBelowFloodStageTime_ms)
        else:
            forecastFallBelowFloodStageTime_ms = 0
        stageFlowUnits = self._rfp.getStageFlowUnits(self._productSegment.pointID)
        
        forecastCrestStage = self._rfp.getForecastCrestStage(self._productSegment.pointID)
        stageTrend = self._rfp.getStageTrend(self._productSegment.pointID)
        riverName = self._rfp.getRiverName(self._productSegment.pointID)

        # Create bullet content
        if section.action in ['NEW', 'CON', 'EXT']:  # MAA if action != ROU
                    
            if observedStage == self._rfp.MISSING_VALUE:
                # FORECAST INFORMATION FOR NO OBS SITUATION    
                #  change made 3/17/2009 Mark Armstrong HSD
                #
                # condition: ( ( <ObsStg> EQ MISSING_VALUE) AND ( <MaxFcstStg> GE <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS FORECAST TO HAVE A MAXIMUM VALUE OF <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
                if maximumForecastStage == self._rfp.MISSING_VALUE :
                    bulletContent = 'Forecast is missing, insert forecast bullet here.'

                elif maximumForecastStage >= floodStage:
                    if forecastRiseAboveFloodStageTime_ms != self._rfp.MISSING_VALUE and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + riverName + ' is forecast to rise above flood stage at ' + forecastRiseAboveFloodStageTime_str\
                         + ' to ' + maximumForecastStage + ' ' + stageFlowUnits + ' and fall below flood stage at ' + forecastFallBelowFloodStageTime_str + '.'
                    elif forecastRiseAboveFloodStageTime_ms != self._rpf.MISSING_VALUE and forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + riverName + ' is forecast to rise above flood stage at ' + forecastRiseAboveFloodStageTime_str\
                         + ' to ' + maximumForecastStage + ' ' + stageFlowUnits + '.'
                    elif forecastRiseAboveFloodStageTime_ms == self._rfp.MISSING_VALUE and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + riverName + ' is forecast to reach ' + maximumForecastStage + ' ' + stageFlowUnits\
                         + ' and fall below flood stage at ' + forecastFallBelowFloodStageTime_str + '.'
                    elif forecastRiseAboveFloodStageTime_ms == self._rfp.MISSING_VALUE and forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + riverName + ' is forecast to reach ' + maximumForecastStage + ' ' + stageFlowUnits + \
                        ' by ' + maximumForecastTime_str + '.'
                    # elif riseabovetime == MISSING_VALUE&& fallbelowtime != missing
                    # bulletContent=  'The ' + self._getRiverName() + ' is forecast to reach ' +
                    # maxfcstval + StgFlowUnits + ' and fall below flood stage at ' + fallbelowfloodtime + '.'
                    # elif riseabovetime == MISSING_VALUE&& fallbelowtime == MISSING_VALUE   
                    # bulletContent =  'The ' + self._getRiverName() + ' is forecast to reach ' + maxfcstval + StgFlowUnits + '.'
#                     bulletContent = 'The ' + self._getRiverName() + ' is forecast to have a maximum value of ' + `maximumForecastStage` + ' ' + stageFlowUnits + \
#                       ' by ' + maximumForecastTime_str + '. '
                #
                # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
                #  change made 3/17/2009 Mark Armstrong HSD
                #
                # condition: ( ( <ObsStg> EQ MISSING_VALUE) AND ( <MaxFcstStg> LT <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS FORECAST BELOW FLOOD STAGE WITH A MAXIMUM VALUE OF <MaxFcstStg> &
                # <StgFlowUnits> <MaxFcstTime>.
                elif maximumForecastStage < floodStage:
                    if forecastCrestStage == self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + riverName + ' is forecast to reach ' + maximumForecastStage + ' ' + stageFlowUnits + \
                        ' by ' + maximumForecastTime_str + '.'                    
                    else :
                        bulletContent = 'The ' + riverName + ' is forecast to crest at ' + forecastCrestStage + ' ' + stageFlowUnits + \
                        ' by ' + forecastCrestTime + '. '

            elif observedStage < floodStage:
                # Observed below flood stage/forecast to rise just to flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> EQ <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS EXPECTED TO RISE TO NEAR FLOOD STAGE <MaxFcstTime>.
                #
                if maximumForecastStage == floodStage:
                    bulletContent = 'The river is expected to rise to near flood stage by ' + maximumForecastTime_str
                    
                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/not falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND &
                # ( <HG,0,FF,X,NEXT> GT <FldStg> ) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>.        
                #            
                elif rfcCrest > floodStage and forecastFallBelowFloodStageTime == self._rfp.MISSING:
                    bulletContent = 'rise above flood stage by ' + forecastRiseAboveFloodStageTime_str + \
                        ' and continue to rise to near ' + `rfcCrest` + ' ' + stageFlowUnits + ' by ' + rfcCrestTime + '. '

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has no crest
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> GT &
                # <FldStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING_VALUE) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <MaxFcstStg> <StgFlowUnits> BY <MaxFcstTime>. &
                # ADDITIONAL RISES ARE POSSIBLE THEREAFTER.        
                #                               
                elif maximumForecastStage > floodStage and rfcCrest == self._rfp.MISSING_VALUE and +\
                    forecastFallBelowFloodStageTime == self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by ' + forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near ' + `maximumForecastStage` + ' ' + stageFlowUnits + ' by ' + \
                       maximumForecastTime_str + '. Additional rises are possible thereafter.'

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT &
                # <FldStg> ) AND ( <FcstFallFSTime> NE MISSING_VALUE) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY <HG,0,FF,X,NEXT,TIME>.&
                # THE RIVER WILL FALL BELOW FLOOD STAGE BY <FcstFallFSTime>.
                #
                elif rfcCrest > floodStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by ' + forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near ' + `rfcCrest` + ' ' + stageFlowUnits + ' by ' + rfcCrestTime + \
                       '. The river will fall below flood stage by ' + forecastFallBelowFloodStageTime_str + '. '
            
            else:  # observedStage >= floodStage:
                
                # Observed above flood stage/forecast continues above flood stage/no
                # crest in forecast time series
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> GT &
                # <ObsStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING_VALUE) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <MaxFcstStg> <StgFlowUnits> BY &
                # <MaxFcstTime>.  ADDITIONAL RISES MAY BE POSSIBLE THEREAFTER.
                #        
                if maximumForecastStage > observedStage and rfcCrest == self._rfp.MISSING_VALUE and \
                     forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                     bulletContent = 'The river will continue rising to near ' + `maximumForecastStage` + ' ' + stageFlowUnits + \
                     ' by ' + maximumForecastTime_str + '. Additional rises may be possible thereafter. '
            
                # Observed above flood stage/forecast crests but stays above flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) &
                # AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME> THEN BEGIN FALLING.
                #
                elif rfcCrest > observedStage and forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = 'The river will continue rising to near ' + `rfcCrest` + ' ' + stageFlowUnits + ' by ' + \
                        rfcCrestTime + ' then begin falling.'
                    
                # Observed above flood stage/forecast crests and falls below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING_VALUE) AND ( <FcstCrestStg> GT <ObsStg> ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>. THE RIVER WILL FALL BELOW FLOOD STAGE &
                # <FcstFallFSTime>.
                #
                elif rfcCrest > observedStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE and \
                    forecastCrestStage > observedStage:
                    bulletContent = 'The river will continue rising to near ' + `rfcCrest` + ' ' + stageFlowUnits + ' by ' + \
                       forecastFallBelowFloodStageTime_ms + '. ' 
                        
                # Observed above flood stage/forecast continue fall/not below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "falling" ) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO A STAGE OF <SpecFcstStg> <StgFlowUnits> BY &
                # <SpecFcstStgTime>.
                #
                elif maximumForecastStage <= observedStage and stageTrend == 'falling' and \
                    forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    # TODO Need SpecFcstStg and SpecFcstStgTime
                    bulletContent = ''
                    
                # Observed above flood stage/forecast is steady/not fall below flood stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "steady" ) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL REMAIN NEAR <MaxFcstStg> <StgFlowUnits>.
                #
                elif maximumForecastStage <= observedStage and stageTrend == 'steady' and \
                    forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    bulletContent = 'The river will remain near ' + `maximumForecastStage` + ' ' + stageFlowUnits + '. '
                    
                # Observed above flood stage/forecast continues fall to below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO BELOW FLOOD STAGE BY &
                # <FcstFallFSTime>.
                #
                elif maximumForecastStage <= observedStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = 'The river will continue to fall to below flood stage by ' + forecastFallBelowFloodStageTime_str + '.'

        elif self._section.action in ['ROU']:                    
            #  FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
            #
            # condition: ( ( <Action> SEQ "ROU" ) AND ( <MaxFcstStg> NE MISSING_VALUE) )
            # bulletstr: FORECAST...THE RIVER WILL RISE TO NEAR <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
            #
            if maximumForecastStage != self._rfp.MISSING_VALUE:
                bulletContent = 'The river will rise to near ' + `maximumForecastStage` + ' ' + stageFlowUnits + \
                      ' by ' + maximumForecastTime_str + '. '                       
        self._tpc.setVal(sectionDict, 'forecastStageBullet', bulletContent, formatMethod=self._section.bulletFormat, formatArgs='Forecast...',
                         productCategory=self._productCategory, productID=self._product.productID)
    
    def _pointImpactsBullet(self, sectionDict, productSegmentGroup, arguments):
        productSegment_tuple, vtecRecord, formatArgs = arguments
        bulletContent = ''
        if self._productSegment.ctas:
            bulletContent = ' '.join(self._productSegment.ctas)
        self._tpc.setVal(sectionDict, 'pointImpactsBullet', bulletContent, formatMethod=self._section.bulletFormat, formatArgs='Impact...',
                         productCategory=self._productCategory, productID=self._product.productID) 
    
    def _floodHistoryBullet(self, sectionDict, productSegmentGroup, arguments):
        '''
        FLOOD HISTORY...THIS CREST COMPARES TO A PREVIOUS CREST OF <HistCrestStg> <ImpCompUnits> on <HistCrestDate>.
        '''
        productSegment_tuple, vtecRecord, formatArgs = arguments
        # ImpCompUnits -- self._rfp.getStageFlowUnits(self._productSegment.pointID)
        self._tpc.setVal(sectionDict, 'floodHistoryBullet', 'Flood history bullet', formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
           
    def _endingSynopsis(self, sectionDict, productSegmentGroup, arguments):
        # TODO Add to Product Staging -- FFA, FLS area
        sectionDict['endingSynopsis'] = '\nBrief post-synopsis of hydrometeorological activity\n'
            
    ######################################
    #  Code       
    ######################################           
                                        
    def _setProductInformation(self, vtecRecord, hazardEvent, productSegment):
        if self._issueFlag:
            # Update hazardEvent
            expTime = hazardEvent.get('expirationTime')
            # Take the earliest expiration time
            if (expTime and expTime > productSegment.expireTime) or not expTime:
                hazardEvent.set('expirationTime', productSegment.expireTime)
            hazardEvent.set('issueTime', self._issueTime)
            hazardEvent.addToList('etns', vtecRecord['etn'])
            hazardEvent.addToList('vtecCodes', vtecRecord['act'])
            hazardEvent.addToList('pils', vtecRecord['pil'])
            try:
                hazardEvent.set('previousForcastCategory', self._maxForecastCategory)
            except:
                pass
        else:
            # Reset state if previewing ended
            if hazardEvent.get('previewState') == 'ended':
                hazardEvent.setStatus('ISSUED')

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
    
    def _formatUGC_entries(self, productSegment):
        ugcCodeList = []
        for ugc in productSegment.ugcs:
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
        for vtecString in self._product.vtecEngine.getVTECString(segment):
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
            timeZone = self._product.timeZones[0]
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
                
    def _createPolygonEntries(self, productSegment):
        polygonEntries = []
        for hazardEvent in productSegment.hazardEvents:
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
        TODO
        '''
        return collections.OrderedDict()
    
    def _createImpactedLocationEntries(self, segment):
        '''
        @param segment
        @return list of Ordered Dictionaries representing locations covered by the segment
        '''
        # Must use productSegment.ugcs to handle point hazards
        # In the case of point hazards, the segment 'ugcs' field is really the pointID e.g. DCTN1
        #  rather than the UGC that corresponds to that point.
        # The UGC is set for the segment (see preProcessSegment) in productSegment.ugcs
        locations = []
        for state, portion, areas in  self._areas:
            for area in areas:
                areaName, areaType = area
                locations.append({'locationName': areaName})
        cities = self.getCityInfo(productSegment.ugcs, returnType='list') 
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
                hazardEvent.setStatus('ENDED')
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
            subType = hazardEvent.getSubType()  
            criteria = {'dataType':'hazardMetaData_filter',
                    'filter':{'phen':phen, 'sig':sig, 'subType':subType}
                    }
            metaData = self.bridge.getData(json.dumps(criteria))
            
            if type(metaData) is not types.ListType:
                metaData = metaData.execute(hazardEvent, {})
            if metaData:
                metaDataList.append((metaData[HazardConstants.METADATA_KEY], hazardEvent))
            else:
                metaDataList.append(([], hazardEvent))
            
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
                    hazardEvent.setStatus('ENDED')
                    

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

    def hydrologicCauseMapping(self, hydrologicCause, key):
        mapping = {
            'dam':          {'immediateCause': 'DM', 'typeOfFlooding':'A dam failure'},
            'siteImminent': {'immediateCause': 'DM', 'typeOfFlooding':'A dam break'},
            'siteFailed':   {'immediateCause': 'DM', 'typeOfFlooding':'A dam break'},
            'levee':        {'immediateCause': 'DM', 'typeOfFlooding':'A levee failure'},
            'floodgate':    {'immediateCause': 'DR', 'typeOfFlooding':'A dam floodgate release'},
            'glacier':      {'immediateCause': 'GO', 'typeOfFlooding':'A glacier-dammed lake outburst'},
            'icejam':       {'immediateCause': 'IJ', 'typeOfFlooding':'An ice jam'},
            'snowMelt':     {'immediateCause': 'RS', 'typeOfFlooding':'Extremely rapid snowmelt'},
            'volcano':      {'immediateCause': 'SM', 'typeOfFlooding':'Extremely rapid snowmelt caused by volcanic eruption'},
            'volcanoLahar': {'immediateCause': 'SM', 'typeOfFlooding':'Volcanic induced debris flow'},
            'default':      {'immediateCause': 'ER', 'typeOfFlooding':'Excessive rain'}
            }
        if mapping.has_key(hydrologicCause):
            return mapping[hydrologicCause][key]
        else:
            return mapping['default'][key]

    def getAreaPhrase(self, productSegment, metaData, hazardEvent):
        '''
        Central Kent County in Southwest Michigan
        This includes the cities of City1 and City2

        @param productSegment object
        @param metaData
        @param hazardEvent -- representative for the segment
        
        @return text describing the UGC areas and optional cities
        
        ''' 
        if hazardEvent.get('geoType') == 'area':
            immediateCause = hazardEvent.get('immediateCause')
            ugcPhrase = self._tpc.getAreaPhrase(productSegment.ugcs)
            if self._section.listOfCities:
                ugcPhrase += '\n' + productSegment.cityString
                
            if immediateCause in ['DM', 'DR', 'GO', 'IJ','RS', 'SM']:
                hydrologicCause = hazardEvent.get('hydrologicCause')
                riverName = None
                if immediateCause == 'DM' and hydrologicCause in ['dam', 'siteImminent', 'siteFailed']:
                    damOrLeveeName = self._tpc.getProductStrings(hazardEvent, metaData, 'damOrLeveeName')
                    if damOrLeveeName:
                        damInfo = self._damInfo().get(damOrLeveeName)
                        if damInfo:
                            riverName = damInfo.get('riverName')
                    if not riverName or not damOrLeveeName:
                        return ugcPhrase
                    else:
                        return 'The '+riverName+' below '+damOrLeveeName+ ' in ' + ugcPhrase
                else:
                    typeOfFlooding = self.hydrologicCauseMapping(hydrologicCause, 'typeOfFlooding')
                    return typeOfFlooding+ ' in '+ugcPhrase                
            return ugcPhrase
        else:
            #  <River> <Proximity> <IdName> 
            riverName = self._rfp.getGroupName(productSegment.pointID)
            proximity = self._rfp.getRiverPointProximity(productSegment.pointID) 
            riverPointName = self._rfp.getRiverPointName(productSegment.pointID) 
            return  '\n the '+riverName + ' '+ proximity + ' ' + riverPointName              
                             
    def getAttributionPhrase(self, vtecRecord, hazardEvent, areaPhrase, issueTime, testMode, wfoCity, lineLength=69, endString = '.'):
        '''
        THE NATIONAL WEATHER SERVICE IN DENVER HAS ISSUED A

        * AREAL FLOOD WATCH FOR A PORTION OF SOUTH CENTRAL COLORADO...
          INCLUDING THE FOLLOWING COUNTY...ALAMOSA.
        '''
        nwsPhrase = 'The National Weather Service in ' + wfoCity + ' has '

        #
        # Attribution and 1st bullet (headPhrase)
        #
        headPhrase = None
        attribution = ''
                
        hazName = self._tpc.hazardName(vtecRecord['hdln'], testMode, False)
                
        if len(vtecRecord['hdln']):
            action = vtecRecord['act']
            
           # Handle special cases
            if action == 'EXT' and self._product.productID in ['FFA', 'FLW', 'FLS'] and self._product.geoType == 'point':
                # Use continuing wording for EXT
                action = 'CON'
                                
            if action == 'NEW':
                attribution = nwsPhrase + 'issued a'
                headPhrase = hazName + ' for ' + areaPhrase + endString
    
            elif action == 'CON':
                attribution = 'the ' + hazName + ' continues for'
                headPhrase =  areaPhrase + endString
    
            elif action == 'EXA':
                attribution = nwsPhrase + 'expanded the'
                headPhrase = hazName + ' to include ' + areaPhrase + endString
    
            elif action == 'EXT':
                if action in 'EXT' and self._product.productID in ['FFA', 'FLW', 'FLS'] and self._product.geoType == 'area':
                    attribution = nwsPhrase + 'extended the'
                else:
                    attribution = 'the ' + hazName + ' is now in effect for' 
                headPhrase = areaPhrase + endString
                    
            elif action == 'EXB':
                attribution = nwsPhrase + 'expanded the'
                headPhrase = hazName + ' to include ' + areaPhrase + endString
    
            elif action == 'CAN':
                attribution = 'the ' + hazName + \
                   ' for ' + areaPhrase + ' has been cancelled' + endString
    
            elif action == 'EXP':
                expTimeCurrent = issueTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' has expired' + endString
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, [hazardEvent], expTimeCurrent)
                   attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + endString
                      
        if headPhrase is not None:
            headPhrase = self._tpc.indentText(headPhrase, indentFirstString='',
              indentNextString='  ', maxWidth=lineLength,
              breakStrings=[' ', '-', '...'])
        else:
            headPhrase = ''

        return attribution, headPhrase
    
    def getHazardTimePhrases(self, vtecRecord, hazardEvent, issueTime, lineLength=69):
        '''
        LATE MONDAY NIGHT
        '''
        endTimePhrase = self._tpc.hazardTimePhrases(vtecRecord, hazardEvent, issueTime, prefixSpace=False)
        #endTimePhrase = self._tpc.substituteBulletedText(endTimePhrase, 'TIME IS MISSING', 'DefaultOnly', lineLength)
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
                
        severity = self._tpc.getProductStrings(hazardEvent, metaData, 'floodSeverity')
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
    
        
    def getBasisPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):
        # Basis bullet
        return self.standardBasisPhrase(vtecRecord, hazardEvent, metaData, lineLength)
    
    def standardBasisPhrase(self, vtecRecord, hazardEvent, metaData, lineLength ):            
        defaultBasis = {
            'NEW': ('BASIS FOR THE WATCH', 'Always'),
            'CON': ('DESCRIBE CURRENT SITUATION', 'DefaultOnly'),
            'EXT': ('BASIS FOR EXTENDING THE WATCH', 'DefaultOnly'),
            'EXB': ('BASIS FOR EXPANSION OF THE WATCH', 'DefaultOnly'),
            'EXA': ('BASIS FOR EXPANSION OF THE WATCH', 'DefaultOnly'),
            'CAN': ('BASIS FOR CANCELLATION OF THE WATCH', 'DefaultOnly'),
            }
        basis = self._tpc.getProductStrings(hazardEvent, metaData, 'basis')
        if not basis:
            default, framing = defaultBasis[vtecRecord['act']] 
            basis = self._tpc.frame(default)
        else:
            basisLocation = self._tpc.getProductStrings(hazardEvent, metaData, 'basisLocation')
            basis = basis.replace('!** LOCATION **!', basisLocation)
        #basisPhrase = self._tpc.substituteBulletedText(basis, default, framing, lineLength)            
        return basis
    
    def floodBasisPhrase(self, vtecRecord, hazardEvent, metaData, floodDescription, lineLength=69):
        #  Time is off of last frame of data
        try :
            eventTime = self._sessionDict['framesInfo']['frameTimeList'][-1]
        except :
            eventTime = vtecRecord.get('startTime')            
        eventTime = self._tpc.getFormattedTime(eventTime / 1000, '%I%M %p %Z ',
                                               shiftToLocal=1, stripLeading=1).upper()
        para = 'At ' + eventTime
        basis = self._tpc.getProductStrings(hazardEvent, metaData, 'basis')
        if basis is None :
            basis = ' '+floodDescription+' was reported'
        para += basis + ' '+floodDescription+' '+ self.basisLocation(hazardEvent)
        motion = self.wxHazardMotion(hazardEvent)

        if motion is None :
            para += '.'
        else :
            para += self.basisLocation(hazardEvent, '. This rain was ', \
               '. This storm was ', '. These storms were ', '-')
            para += motion + '.'
        return para
    
    
    def getImpactsPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):       
        # Impacts bullet
        return self.standardImpactsPhrase(vtecRecord, hazardEvent, metaData, lineLength)
    
    def standardImpactsPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):
        if (vtecRecord['act'] == 'NEW' and self._productSegment.canVtecRecord):  # or multRecords:
            framing = 'Always'
        else:
            framing = 'DefaultOnly'
        impacts = self._tpc.getProductStrings(hazardEvent, metaData, 'impacts')
        if not impacts:
            impacts = self._tpc.frame('(OPTIONAL) POTENTIAL IMPACTS OF FLOODING')
        #impactsPhrase = self._tpc.substituteBulletedText(impacts,
        #    '(OPTIONAL) POTENTIAL IMPACTS OF FLOODING', framing, lineLength)        
        return impacts
    
    def floodImpactsPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69):
        '''
        #* LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO CASTLE
        #  PINES...THE PINERY...SURREY RIDGE...SEDALIA...LOUVIERS...HIGHLANDS
        #  RANCH AND BEVERLY HILLS. 
        '''        
        para = 'locations in the warning include but are not limited to '
        para += self.getCityInfo(self._ugcs)
        return '\n' + para + '\n'

    
    def getCTAsPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):
        ctas = self._tpc.getProductStrings(hazardEvent, metaData, 'cta')                
        return ctas
 
    def getCountyInfoForEvent(self, hazardEvent, productSegment) :
        ''' 
        Returns list of tuples:        
        From AreaDictionary:
        ( ugcName, portion, PARISH/COUNTY, fullStateName, stateAbbr, partOfState)
        '''
        countyList = []
        for ugc in productSegment.ugcs:
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
    
    def _getFormattedTime(self, time_ms, format=None, shiftToLocal=True, stripLeading=True): 
        if format is None:
            format = '%I%M %p %A %Z '
        return self._tpc.getFormattedTime(
                time_ms/1000, format, shiftToLocal=shiftToLocal, stripLeading=stripLeading)
               
    def formatPolygonForEvent(self, hazardEvent):
        for polygon in self._extractPolygons(hazardEvent):
            polyStr = 'LAT...LON'
            # 4 points per line
            pointsOnLine = 0
            for lon,lat in polygon:              
                if pointsOnLine == 4:
                    polyStr += '\n' 
                    pointsOnLine = 0
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
    
    #############################################################################
    #  Formatting for Short-fused products
    #  These apply only to TOR and SVR and will be completed later
    ############################################################################# 
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

    # Return None if for some reason the motion was not available.
    def wxHazardMotion(self, hazardEvent, useMph=True,
                        still='stationary', slow='nearly stationary',
                        lead='moving', trail='',
                         minSpd=2.5, round=5.0) :
        stormMotion = hazardEvent.get('stormMotion')
        if stormMotion is None :
            return None
 
    def correctProduct(self, dataList, prevDataList, correctAllSegments):
        millis = SimulatedTime.getSystemTime().getMillis()
        dt = datetime.datetime.fromtimestamp(millis / 1000)
        currentTime = dt.strftime('%d%H%m')
        for i in range(0, len(dataList)):
            data = dataList[i]
   
            wmoHeader = data['wmoHeader']
            wmoHeaderLine = wmoHeader['wmoHeaderLine']
            wmoHeader['wmoHeaderLine'] = wmoHeaderLine[:-6] + currentTime
            
            segments = data['segments']       
            for j in range(0, len(segments)):
                segment = segments[j]
                if correctAllSegments:
                    segment = self.correctSegment(segment)
                else:
                    prevData = prevDataList[i]
                    pSegments = prevData['segments']
                    pSegment = pSegments[j]
                    if segment != pSegment:
                        segment = self.correctSegment(segment)                  
        
            productName = str(data['productName'])
            if '...CORRECTED' not in productName:
                if productName.endswith('...TEST'):
                    data['productName'] = productName[:-7] + '...CORRECTED...TEST'
                else:
                    data['productName'] = productName + '...CORRECTED'
        return dataList
    
    def correctSegment(self, segment):
        if 'vtecRecords' in segment:
            vtecRecordList = segment['vtecRecords']
            for j in range(0,len(vtecRecordList)):
                vtecRecord = vtecRecordList[j]
                if vtecRecord['vtecRecordType'] == 'pvtecRecord':
                    action = vtecRecord['action']
                    vtecRecord['action'] = 'COR'
                    vtecString = vtecRecord['vtecString']
                    updatedVtecString = vtecString.replace(action, 'COR')
                    vtecRecord['vtecString'] = updatedVtecString
                    
        return segment

    def descWxLocForEvent(self, hazardEvent,
             noevent='FROM HEAVY RAIN. THIS RAIN WAS LOCATED', \
             point='FROM A THUNDERSTORM. THIS STORM WAS LOCATED', \
             line='FROM A LINE OF THUNDERSTORMS. THESE STORMS WERE LOCATED', \
             ):
        # Speed
        speed = stormMotion.get('speed')
        if speed < 0 :
            return None
        if speed <= minSpd/10 :
            return still
        if useMph :
            speed *= 1.16
        if speed < minSpd :
            return slow
        
        # Bearing
        bearingLookUp = {
            45: 'southwest ',
            90: 'west ',
            135:'northwest ',
            180:'north ',
            225:'northeast ',
            270:'east ',
            315:'southeast ',
            'default': 'south ',}
        bearing = stormMotion.get('bearing')
        bearing = 45 * (int)((bearing + 22.5) / 45)
        bearing = bearingLookup.get(bearing)
        if bearing is None:
            bearing = bearingLookup['default']
            
        # Motion string
        speed = round * int((speed + round / 2) / round)
        movStr = bearing + 'at ' + str(int(speed))
        if useMph :
            movStr += ' mph'
        else :
            movStr += ' knots'
        if len(lead) > 0 :
            movStr = lead + ' ' + movStr
        if len(trail) > 0 :
            movStr += ' ' + trail
        return movStr
  
    def basisLocation(self, hazardEvent,
             noevent='from heavy rain. This rain was located',\
             point='from a thunderstorm. This storm was located',\
             line='from a line of thunderstorms.  These storms were located',\
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


    def correctProduct(self, dataList, prevDataList, correctAllSegments):
        millis = SimulatedTime.getSystemTime().getMillis()
        dt = datetime.datetime.fromtimestamp(millis / 1000)
        currentTime = dt.strftime('%d%H%m')
        for i in range(0, len(dataList)):
            data = dataList[i]
   
            wmoHeader = data['wmoHeader']
            wmoHeaderLine = wmoHeader['wmoHeaderLine']
            wmoHeader['wmoHeaderLine'] = wmoHeaderLine[:-6] + currentTime
            
            segments = data['segments']               
            for j in range(0, len(segments)):
                segment = segments[j]
                if correctAllSegments:
                    segment = self.correctSegment(segment)
            
                    prevData = prevDataList[i]
                    pSegments = prevData['segments']
                    pSegment = pSegments[j]
                    if str(segment) != str(pSegment):
                        segment = self.correctSegment(segment)                  
        
            productName = str(data['productName'])
            if '...CORRECTED' not in productName:
                if productName.endswith('...TEST'):
                    data['productName'] = productName[:-7] + '...CORRECTED...TEST'
                else:
                    data['productName'] = productName + '...CORRECTED'
        return dataList
    
    def correctSegment(self, segment):
        if 'vtecRecords' in segment:
            vtecRecordList = segment['vtecRecords']
            for j in range(0,len(vtecRecordList)):
                vtecRecord = vtecRecordList[j]
                if vtecRecord['vtecRecordType'] == 'pvtecRecord':
                    action = vtecRecord['action']
                    vtecRecord['action'] = 'COR'
                    vtecString = vtecRecord['vtecString']
                    updatedVtecString = vtecString.replace(action, 'COR')
                    vtecRecord['vtecString'] = updatedVtecString
                    
        return segment

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

