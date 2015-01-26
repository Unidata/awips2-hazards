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
    Aug 15, 2014   4243      Chris.Golden        Changed to work with latest version of hazard event metadata.
    Dec 1, 2014    4373      Dan Schaffer        HID Template migration for warngen
    Dec 15, 2014   3846,4375 Tracy.L.Hansen      'defineDialog' -- Product Level information and Ending Hazards
    @author Tracy.L.Hansen@noaa.gov
'''

import ProductTemplate

from Bridge import Bridge
from LocalizationInterface import *

import math
import traceback
import LatLonCoord
import DistanceBearing
from ufpy.dataaccess import DataAccessLayer
from shapely.geometry import Polygon 

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

import ProductTextUtil
from com.raytheon.uf.common.time import SimulatedTime
from BasisText import BasisText

from abc import *

class Prod:
    pass

class ProdSegment:
    def __init__(self, segment, vtecRecords):
        # 'segment' is (frozenset([list of ugcs or points]), frozenset([list of eventIDs])        
        self.segment = segment
        self.vtecRecords = vtecRecords
        self.vtecRecords_ms = vtecRecords
        
    def str(self):
        print '  segment', self.segment 
        print '  vtecRecords: ', self.vtecRecords
        
        
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
        self.determineSegmentInfo()
        self.productLabel = self.determineProductLabel()
        
    def addProductSegment(self, productSegment):
        self.productSegments.append(productSegment)
        
    def setProductParts(self, productParts):
        self.productParts = productParts
        
    def determineSegmentInfo(self):
        '''
        Return a list of information for each segment:
           Each segment will have a dictionary of  
               {'act': action, 'eventIDs': eventID, 'type':hazardType, 'etn':etn)
               
        NOTE: most segments have only one action, eventID, hazardType
            but some long-fused products can have multiple
        '''
        self.segmentInfoList = []
        self.actions = []
        self.eventIDs = []
        self.hazardTypes = []
        self.etns = []
        for productSegment in self.productSegments:
            actionEventList = []
            for vtecRecord in productSegment.vtecRecords:
                action = vtecRecord.get('act')
                eventIDs = vtecRecord.get('eventID')
                hazardType = vtecRecord.get('key')
                etn = vtecRecord.get('etn')
                info = {
                    'action': action,
                    'eventIDs': eventIDs,
                    'type': hazardType,
                    'etn': etn,
                }
                actionEventList.append(info)
                self.actions.append(action)
                self.eventIDs += list(eventIDs)
                self.hazardTypes.append(hazardType)
                self.etns.append(etn)
            self.segmentInfoList.append(actionEventList)
               
    def determineProductLabel(self):
        ''' 
          The Product Label is a unique identifier for use in the Product Staging dialog
          It could also be used in the Product Editor and if included in the Product Dictionary
             
            FFA_area, FFA_point
            FLW_point, FLS_point_advisory, FLS_point_warning
            FLW_area_eventID_hazardType, FLS_area_eventID_hazardType
              Note: The hazard type is added for clarity, not needed for uniqueness
        
        '''
        # FFA_area, FFA_point, FLW_point
        productLabel = self.productID + '_' + self.geoType
        if self.geoType == 'point' and self.productID in ['FLS']:
            # FLS_point_advisory, FLS_point_warning
            if self.productName.find('Advisory') >= 0:
                productLabel += '_advisory'
            else:
                productLabel += '_warning'                
        elif self.geoType == 'area' and self.productID in ['FLW', 'FLS']:
            # FLW_area_eventID_hazardType, FLS_area_eventID_hazardType
            eventID = self.eventIDs[0]
            hazardType = self.hazardTypes[0].replace('.','_')
            productLabel += '_' + eventID + '_' + hazardType
        return productLabel
           
    def str(self):
        print 'productID: ', self.productID, self.productName, self.productLabel
        print 'geoType: ', self.geoType, 'mapType: ', self.mapType, 'segmented: ', self.segmented, 'etn: ', self.etn, 'actions: ', self.actions
        print 'productSegments: ', len(self.productSegments)
        for productSegment in self.productSegments:
            print productSegment.str()
        
class Product(ProductTemplate.Product):
    __metaclass = ABCMeta
 
    def __init__(self):  
        '''
        General Note:  All times are in milliseconds unless designated 
        otherwise e.g. issueTime_secs.
        
        '''  
        # self.initialize()
        
    def createProduct(self):
        return Prod()
    def createProductSegment(self, segment, vtecRecords):
        return ProdSegment(segment, vtecRecords)
    def createProductSection(self):
        return ProdSection()
    def createProductSegmentGroup(self, productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn=None, formatPolygon=None):
        return ProductSegmentGroup(productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn, formatPolygon)

    def initialize(self):
        self.bridge = Bridge()        
        self._hazardTypes = self.bridge.getHazardTypes()         
        areaDict = self.bridge.getAreaDictionary()
        self._cityLocation = self.bridge.getCityLocation()
        self._siteInfo = self.bridge.getSiteInfo()
        
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)
        self._pp = ProductParts()
        self._rfp = None  # RiverForecastPoints()

        self.logger = logging.getLogger('Legacy_ProductGenerator')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'com.raytheon.uf.common.hazards.productgen', 'Legacy_ProductGenerator', level=logging.INFO))
        self.logger.setLevel(logging.INFO)  
                
        # Default is True -- Products which are not VTEC can override and set to False
        self._vtecProduct = True       
        self._vtecEngine = None
        self._productCategory = ''
        self.basisText = BasisText()

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD developers'
        metadata['description'] = 'Base Class for Product Generators'
        metadata['version'] = '1.0'
        return metadata
       
    @abstractmethod
    def defineDialog(self, eventSet):
        '''
        @return: dialog definition to solicit user input before running tool
        '''  
        pass

    def _previewProductSegmentGroups(self, eventSet):
        self._initialize()
        self._getVariables(eventSet)
        if not self._inputHazardEvents:
            return {}
        # Important to turn off "issue" so that the ETN number will not be advanced
        # when previewing the VTEC segmentation
        self._issueFlag = False
        segments = self._getSegments(self._inputHazardEvents)
        return self._groupSegments(segments)

    def _getProductLevelMetaData(self, inputHazardEvents, metaDataFile, productSegmentGroups):
        # Use product segment groups to determine the products 
        metaDataDict = {}
        self.bridge = Bridge()
        eventIDs = [hazardEvent.getEventID() for hazardEvent in self._generatedHazardEvents]
        for productSegmentGroup in productSegmentGroups:
            productGroup = {
                'productID': productSegmentGroup.productID,
                'productName': productSegmentGroup.productName,
                'geoType': productSegmentGroup.geoType,
                'productLabel': productSegmentGroup.productLabel,
                'productParts': productSegmentGroup.productParts,
                'actions':productSegmentGroup.actions,
                'eventIDs': eventIDs,
                }
            # Just send one group -- we'll have a separate sub-tab for each product           
            metaDict = {'productSegmentGroup': productGroup, 'productCategory': self._productCategory}
            metaData = self.getMetaData(inputHazardEvents, metaDict, metaDataFile) 
            metaDataDict[productSegmentGroup.productLabel] = metaData.get('metadata')  
        return metaDataDict

    def _checkForCancel(self, inputHazardEvents, productSegmentGroups):
        ''' Check for partial or automatic cancel
        
        Partial Cancel -- 
            Look for an eventID with both CAN segments and non-CAN segments
        Automatic Cancel -- 
           Look for an eventID with a CAN segment that is not included in the 
           inputHazardEvents
           
        @param inputHazardEvents -- hazard events given as input to the product generator
        @param productSegmentGroups
        @return metaDataDict -- organized by productSegmentGroup / productLabel:
           {productLabel: megawidgets to display for cancellations}
        '''
        inputEventIDs = [hazardEvent.getEventID() for hazardEvent in inputHazardEvents]
        mode = self._sessionDict.get('hazardMode', 'PRACTICE').upper()
        # Determine all the actions in a product associated with each eventID
        #    eventID : [actions list]
        actionDict = {}
        ugcDict = {}
        metaDataDict = {}
        for productSegmentGroup in productSegmentGroups:
            for productSegment in productSegmentGroup.productSegments:
                vtecRecords = productSegment.vtecRecords
                actions = []
                for vtecRecord in vtecRecords:
                    action = vtecRecord['act']
                    actions.append(action)
                ids, eventIDs = productSegment.segment
                if 'CAN' in actions:
                    description = self._tpc.formatUGC_names(ids)
                for eventID in eventIDs:
                    eventActions = actionDict.get(eventID, [])
                    for action in actions:
                        if action not in eventActions:
                            eventActions.append(action)
                    actionDict[eventID] = eventActions
        
            # Look for Partial and Automatic Cancellation
            canceledEventIDs = []
            for eventID in actionDict:
                if eventID not in inputEventIDs:
                    # Automatic Cancellation -- need to retrieve hazard event.
                    hazardEvent = HazardDataAccess.getHazardEvent(str(eventID), mode)
                    inputHazardEvents.add(hazardEvent)
                    canceledEventIDs.append(eventID)
                eventActions = actionDict.get(eventID, [])
                if 'CAN' in eventActions:
                    non_CAN = False
                    for eventAction in eventActions:
                        if eventAction not in ['CAN']:
                            # Partial cancellation -- Cancel segments plus non-Cancel segments associated with the same eventID
                            canceledEventIDs.append(eventID)
                            break        
        
            # Get the Meta Data for the canceledEventIDs
            # We will assume that all the eventIDs for a giving productSegmentGroup are in the same segment, 
            # so we only have to get the metaData for the "lowest" eventID (by convention)
            # Note: 
            #    -- the product staging information needs to be unpacked accordingly and 
            #    -- the "sectionHazardEvent" therefore should be the "lowest" eventID so
            #       that this metaData is picked up.
            metaDataList = []
            canceledEventIDs.sort()
            for eventID in canceledEventIDs:
                for hazardEvent in self._inputHazardEvents:
                    if eventID == hazardEvent.getEventID():
                        if not metaDataList:
                            saveStatus = hazardEvent.getStatus()
                            hazardEvent.setStatus('ending')
                            metaDataList += self.getHazardMetaData(hazardEvent).get('metadata', [])                
                            hazardEvent.setStatus(saveStatus)
                    
            if metaDataList:
                metaDataList = [{
                    "fieldType": "Label",
                    "fieldName": "label1",
                    "label": "Enter information for cancellation -- " + description + ".",
                    "bold": True,
                    "italic": True
                    }] + metaDataList
            metaDataDict[productSegmentGroup.productLabel] = metaDataList
        return metaDataDict    
    
    def _organizeByProductLabel(self, productLevelMetaData_dict, cancel_dict, fieldName):
        # Organize by productLabel with a tab for each productLabel
        pages = []
        pageNames = []
        for productLabel in productLevelMetaData_dict:
            productFields = productLevelMetaData_dict.get(productLabel, [])
            cancelFields = cancel_dict.get(productLabel, [])
            if productFields is None: productFields = []
            if cancelFields is None: cancelFields = []
            fields = productFields + cancelFields
            pageNames.append(productLabel)
            # Add a tab for the productLabel
            if fields:
                page = {
                    "pageName": productLabel,
                    "pageFields": fields
                }
                pages.append(page)
        # Add any tabs that we missed
        for productLabel in cancel_dict:
            if productLabel not in pageNames:
                pageNames.append(productLabel)
                fields = cancel_dict.get(productLabel)
                # add a tab for the productLabel
                if fields:
                    page = {
                        "pageName": productLabel,
                        "pageFields": fields
                    }
                    pages.append(page)
        tabs = [{
            "fieldType": "TabbedComposite",
            "fieldName": fieldName,
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": True,
            "expandVertically": True,
            "pages": pages,
        }]
                
        dialogDict = {}
        if pages:
            dialogDict['metadata'] = tabs
            # dialogDict['metadata'] = fields
        return dialogDict

    
    @abstractmethod
    def execute(self, eventSet, dialogInputMap):          
        '''
        Must be overridden by the Product Generator
        '''
        pass
    
    def _getVariables(self, eventSet, dialogInputMap=None): 
        '''
         Set up class variables
        ''' 
        self._inputHazardEvents = eventSet.getEvents()
        metaDict = eventSet.getAttributes()
        if dialogInputMap:
            self._storeDialogInputMap(dialogInputMap)
        else:
            self._dialogInputMap = {}
                
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

    def _storeDialogInputMap(self, dialogInputMap):
        self._dialogInputMap = dialogInputMap
        eventIDs = [hazardEvent.getEventID() for hazardEvent in self._inputHazardEvents]
        for eventID in eventIDs:
            for key in dialogInputMap:
                value = dialogInputMap.get(key)
                # Some values may be lists e.g. calls to action
                value = json.dumps(value)
                ProductTextUtil.createOrUpdateProductText(key, '', '', '', [eventID], value)    
        self.flush()
                    
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
#             print '  ', hazardEvent.getStatus(), str(hazardEvent)
#         self.flush()
#         #return [], self._generatedHazardEvents
        return productDicts, self._generatedHazardEvents

    def _printDict(self, dictionary, indent=''):
        for key in dictionary:
            value = dictionary.get(key)
            if type(value) is collections.OrderedDict or type(value) is types.DictType:
                print key, ': {'
                self._printDict(value, indent + '  ')
                print '}'
            elif type(value) is types.ListType:
                print key, ': ['
                for val in value:
                    if type(val) is collections.OrderedDict or type(val) is types.DictType:
                        print ' {'
                        self._printDict(val, indent + '  ')
                        print '}'
                    else:
                        print val
                print ']'
            else:
                print key, ':', value
              
    def _showProductParts(self):
        # IF True will label the editable pieces in the Product Editor with product parts
        return True
    
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


    @abstractmethod
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
        headerDict['awipsIdentifierLine'] = self._product.productID + self._siteID
        ddhhmmTime = self._tpc.getFormattedTime(self._issueTime, '%d%H%M', stripLeading=False)
        headerDict['wmoHeaderLine'] = self._product.wmoID + ' ' + self._fullStationID + ' ' + ddhhmmTime
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
        productDict['overviewHeadline'] = '|* Overview Headline *|'
        
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
                if action in ('NEW', 'EXT'):
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
        # ( <Action> SEQ "NEW" )
        # bulletstr: <EventTime>.
        # condition: ( ( <Action> SEQ "NEW" ) AND ( <EventEndTime> GT <EventBeginTime> ) )
        # bulletstr: <EventTime>...OR UNTIL THE WARNING IS CANCELLED.
        # condition: ( ( <Action> SEQ "NEW" ) AND ( <EventEndTime> LE <EventBeginTime> ) )
        # bulletstr: <EventTime>.
        #
        if not self._rfp:
            from RiverForecastPoints import RiverForecastPoints
            millis = SimulatedTime.getSystemTime().getMillis()
            currentTime = datetime.datetime.fromtimestamp(millis / 1000)
            self._rfp = RiverForecastPoints(currentTime)
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
            
            pointAreaGroups = self._tpc.getGeneralAreaList(ugcs)
            areaGroups += pointAreaGroups

            nameDescription, nameTypePhrase = self._tpc.getNameDescription(pointAreaGroups)
            affected = nameDescription + ' ' + nameTypePhrase
            riverName = self._rfp.getGroupName(pointID)
            proximity = self._rfp.getRiverPointProximity(pointID) 
            riverPointName = self._rfp.getRiverPointName(pointID)
            locationPhrases.append('       ' + riverName + ' ' + proximity + ' ' + riverPointName + ' affecting ' + affected + '.')  
                  
        locationPhrase = '\n'.join(locationPhrases)   
        areaGroups = self._tpc.simplifyAreas(areaGroups)
        states = self._tpc.getStateDescription(areaGroups)
        riverPhrase = 'the following rivers in ' + states
        # Use the last segment_vtecRecord_tuple (segment, vtecRecords)
        # since that information will apply to all points in the product
        vtecRecord = vtecRecords[0]       
        attribution, headPhrase = self.getAttributionPhrase(
                    vtecRecord, hazardEvent, riverPhrase, self._issueTime, self._testMode, self._wfoCity, endString='...')                
        overview = '...' + attribution + headPhrase + '\n\n' + locationPhrase + '\n'
        return overview

    def _overviewSynopsis_area(self, productDict, productSegmentGroup, arguments=None):
        '''
        FFA -- FF.A, FA.A
        '''
        productDict['overviewSynopsis_area'] = self._dialogInputMap.get('overviewSynopsisText_' + productSegmentGroup.productLabel, '') + '\n'

    def _overviewSynopsis_point(self, productDict, productSegmentGroup, arguments=None):
        '''
        FFA_point, FLW, FLS:
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
        productDict['overviewSynopsis_point'] = self._dialogInputMap.get('overviewSynopsisText_' + productSegmentGroup.productLabel, '') + '\n'
        
    def _rainFallStatement(self, productDict, productSegmentGroup, arguments=None):
        '''
        If product is not a cancellation or expiration, include the following:
         
        THE SEGMENTS IN THIS PRODUCT ARE RIVER FORECASTS FOR SELECTED 
        LOCATIONS IN THE WATCH AREA [(optional:) BASED ON CURRENTLY AVAILABLE          
        RAINFALL FORECASTS RANGING FROM <QPF lower range> TO <QPF upper range> 
        INCHES OVER THE <river/basin name(s)>]... 
        '''
        productDict['rainFallStatement'] = 'The segments in this product are river forecasts for selected locations in the watch area.\n'
               
    def _callsToAction_productLevel(self, productDict, productSegmentGroup, arguments=None):
        '''
        We pass here because the Calls To Action will be gathered as we go through the segments.
        The 'wrapUp_product' method will then add the cta's to the product dictionary
        '''
        productLevelMetaData = self._productLevelMetaData_dict[productSegmentGroup.productLabel]
        ctas = self._tpc.getProductStrings(self._dialogInputMap, productLevelMetaData, 'cta_' + productSegmentGroup.productLabel) 
        productDict['callsToAction_productLevel'] = ctas
   
    def _additionalInfoStatement(self, productDict, productSegmentGroup, arguments=None):
        # Please override this method for your site
        productDict['additionalInfoStatement'] = 'Additional information is available at <Web Site URL>.'

        
    def _nextIssuanceStatement(self, productDict, productSegmentGroup, arguments=None):
        
        ugcs = []
        for productSegment in productSegmentGroup.productSegments:
           ugcs.extend(self._getProductSegmentUGCs(productSegment))
           
        ugcs.sort()
        
        timeZones = self._tpc.hazardTimeZones(ugcs)
        expireTime = self._tpc.getExpireTime(
                    self._issueTime, self._purgeHours, [], fixedExpire=True)
        
        ### want description from timingWordTableFUZZY4, hence [2]
        ### Using only first timezone. Don't think 1 hr diff enough to include extra description
        nextIssue = self._tpc.timingWordTableFUZZY4(self._issueTime, expireTime, timeZones[0], 'startTime')[2] + ' at'
        for timeZone in timeZones:
            fmtIssueTme = self._tpc.getFormattedTime(expireTime, timeZones=[timeZone], format='%I%M %p %Z')
            ### If more than one timezone, will read like: 
            ### "The next statement will be issued at Tuesday morning at 600 AM CST. 500 AM MST."
            nextIssue += ' ' + fmtIssueTme + '.'
        
        productDict['nextIssuanceStatement'] = 'The next statement will be issued at ' + nextIssue
        
    def _floodPointTable(self, productDict, productSegmentGroup, arguments=None):
        # TODO Could be called at product level (no arguments or segment level with _productSegment argument
        pass
        
    def _wrapUp_product(self, productDict, productSegmentGroup, arguments=None):
        # productDict['ctas'] = productSegment.ctas
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
        self.flush()
        segment, vtecRecords = productSegment_tuple 
        productSegment = self.createProductSegment(segment, vtecRecords)
        productSegment.metaDataList, productSegment.hazardEvents = self.getSegmentMetaData(segment)
               
        # There may be multiple (metaData, hazardEvent) pairs in a segment 
        #   An example would be for a NPW product which had a Frost Advisory and a Wind Advisory in one segment
        # There will be a section for each 
        # 'segment' is (frozenset([list of ugcs or points]), frozenset([list of eventIDs])        
            
        # UGCs and Expire Time
        # Assume that the geoType is the same for all hazard events in the segment i.e. area or point

        hazardEvent = productSegment.hazardEvents[0]
        productSegment.ugcs = self._getProductSegmentUGCs(productSegment)
        productSegment.pointID = hazardEvent.get('pointID')
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

    def _getProductSegmentUGCs(self,productSegment):
        segment = productSegment.segment
        hazardEvents = self.getSegmentMetaData(segment)[1]
        hazardEvent = hazardEvents[0]
        ugcs = []
        
        if hazardEvent.get('geoType') == 'area':
            ugcs = list(segment[0])
        else:
            ugcs = hazardEvent.get('ugcs', [])
            
        return ugcs

    def _ugcHeader(self, segmentDict, productSegmentGroup, arguments):
        segment = self._productSegment.segment
        vtecRecords = self._productSegment.vtecRecords
        segmentDict['ugcCodes'] = self._formatUGC_entries(self._productSegment)
        self._ugcHeader_value = self._tpc.formatUGCs(self._productSegment.ugcs, self._productSegment.expireTime)
        segmentDict['ugcHeader'] = self._ugcHeader_value
        self._tpc.setVal(segmentDict, 'displayUgcHeader', self._ugcHeader_value, displayable=True, editable=False, label='UGCs (Locked)',
                         productCategory=self._productCategory, productID=self._product.productID) 
       
    def _vtecRecords(self, segmentDict, productSegmentGroup, arguments):
        segment = self._productSegment.segment
        vtecRecords = self._productSegment.vtecRecords
        vtecRecordEntries = self._vtecRecordEntries(segment)
        segmentDict['vtecRecords'] = vtecRecordEntries
        # Set up display for Editor
        partText = ''
        for vtecRecord in vtecRecordEntries:
            partText += vtecRecord['vtecString'] + '\n'

        self._tpc.setVal(segmentDict, 'displayVtecRecords', partText, displayable=True, label='VTEC (Locked)',
                         productCategory=self._productCategory, productID=self._product.productID) 

    def _areaList(self, segmentDict, productSegmentGroup, arguments):
         # Area String        
        self._tpc.setVal(segmentDict, 'areaList', self._tpc.formatUGC_names(self._productSegment.ugcs), displayable=True, label='Areas (Locked)',
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
        segmentDict['basisAndImpactsStatement_segmentLevel'] = '|* Current hydrometeorological situation and expected impacts *| \n'
        
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
                            self._section.hazardEvent, self._section.metaData, 'additionalInfo', choiceIdentifier='floodMoving', 
                            formatMethod=self.floodTimeStr, formatHashTags=['additionalInfoFloodMovingTime'])
        # Use the phen, sig, life cycle point, and whether an areal product to determine
        # the format of the areaPhrase.  Maybe this should be in a table???
        self._tpc.setPartOfStateInfo(sectionHazardEvent["attributes"])
        phen = self._section.vtecRecord.get("phen")
        sig = self._section.vtecRecord.get("sig")
        action = self._section.vtecRecord.get("act")
        geoType = sectionHazardEvent.get('geoType')
        if phen in ["FF", "FA", "TO", "SV", "SM", "EW" , "FL" ] and \
           action in [ "NEW", "EXA", "EXT", "EXB" ] and \
           geoType == 'area' and sig != "A" :
            self._section.areaPhrase = self.getAreaPhraseBullet(self._productSegment, sectionMetaData, sectionHazardEvent)
        else :
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
             
        # Point-specific information
        if self._productSegment.pointID:
            self._getPointInformation()            

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

    def floodTimeStr(self, creationTime, flood_time_ms):
        creationTimeInSeconds = int(creationTime.strftime("%s"))
        floodTimeInSeconds = flood_time_ms/1000
        floodTime = datetime.datetime.fromtimestamp(floodTimeInSeconds)
        creationWeekDay = creationTime.strftime("%A")
        floodWeekDay = floodTime.strftime("%A")
        SECONDS_PER_WEEK = 86400*7
        if floodTimeInSeconds > (creationTimeInSeconds + SECONDS_PER_WEEK) :
            result= floodTime.strftime("%b %d %Y at %H:%M %p")
        elif creationWeekDay != floodWeekDay:
            result= floodTime.strftime("%A at %H:%M %p")
        else:
            result= floodTime.strftime("%H:%M %p")
        return result
               
    def _getPointInformation(self):
        # Import RiverForecastPoints if not there
        if not self._rfp:
            from RiverForecastPoints import RiverForecastPoints
            millis = SimulatedTime.getSystemTime().getMillis()
            currentTime = datetime.datetime.fromtimestamp(millis / 1000)
            self._rfp = RiverForecastPoints(currentTime)
        
        # Gather information\
        pointID = self._productSegment.pointID
        timeZones = self._productSegment.timeZones
        
        self._section.riverName = self._rfp.getRiverName(pointID)
        # Observed and Flood Stages
        self._section.observedStage, shefQualityCode = self._rfp.getObservedStage(pointID)
        self._section.floodStage = self._rfp.getFloodStage(pointID)
        
        # Maximum Forecast Stage       
        self._section.primaryPE = self._rfp.getPrimaryPhysicalElement(pointID)
        self._section.maximumForecastStage = self._rfp.getMaximumForecastLevel(pointID, self._section.primaryPE)
        self._section.maximumForecastTime_ms = self._rfp.getMaximumForecastTime(pointID)
        self._section.maximumForecastTime_str = self._getFormattedTime(self._section.maximumForecastTime_ms, emptyValue='at time unknown', timeZones=timeZones)
        
        # Rise    
        self._section.forecastRiseAboveFloodStageTime_ms = self._rfp.getForecastRiseAboveFloodStageTime(pointID)
        self._section.forecastRiseAboveFloodStageTime_str = self._getFormattedTime(self._section.forecastRiseAboveFloodStageTime_ms, timeZones=timeZones)
                        
        # Crest
        self._section.forecastCrestStage = self._rfp.getForecastCrestStage(pointID)
        # self._section.forecastCrest = self._rfp.getPhysicalElementValue(pointID, 'HG', 0, 'FF', 'X', 'NEXT')
        self._section.forecastCrest = '30 feet'
        # self._section.forecastCrestTime_ms = self._rfp.getPhysicalElementValue(pointID, 'HG', 0, 'FF', 'X', 'NEXT', timeFlag=True) 
        self._section.forecastCrestTime_ms = self._rfp.getForecastCrestTime(pointID)       
        self._section.forecastCrestTime_str = self._getFormattedTime(self._section.forecastCrestTime_ms, timeZones=timeZones)        
        self._section.physicalElementTime_str = ' stage time'
        
        # Fall
        self._section.forecastFallBelowFloodStageTime_ms = self._rfp.getForecastFallBelowFloodStageTime(pointID)
        if not self._section.forecastFallBelowFloodStageTime_ms:
            self._section.forecastFallBelowFloodStageTime_ms = self._rfp.MISSING_VALUE
       
        self._section.forecastFallBelowFloodStageTime_str = self._getFormattedTime(self._section.forecastFallBelowFloodStageTime_ms, timeZones=timeZones)
        self._section.stageFlowUnits = self._rfp.getStageFlowUnits(pointID)
        
        # Trend
        self._section.stageTrend = self._rfp.getStageTrend(pointID)
        
    def _emergencyHeadline(self, sectionDict, productSegmentGroup, arguments):
        # Check to see if emergencyHeadine is to be included
        hazardEvent = self._section.hazardEvent
        includeChoices = hazardEvent.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            location = hazardEvent.get('includeEmergencyLocation')
            if location is None:
                location = self._tpc.frame('Enter location')
            sectionDict['emergencyHeadline'] = '...Flash Flood Emergency for ' + location + '...\n'

    def _attribution(self, sectionDict, productSegmentGroup, arguments):
        attribution = self._section.attribution
        if attribution:
            if self._section.bulletFormat == 'bulletFormat_CR':
                attribution = attribution + '\n'
            self._tpc.setVal(sectionDict, 'attribution', attribution, editable=True,
                        eventIDs=[self._section.hazardEvent.getEventID()],
                        segment=self._productSegment.segment, 
                        productCategory=self._productCategory, productID=self._product.productID,
                        useKeyAsLabel=self._showProductParts())  
            sectionDict['description'] += attribution + '\n'

    def _firstBullet(self, sectionDict, productSegmentGroup, arguments):
        if self._section.firstBullet:
            self._tpc.setVal(sectionDict, 'firstBullet', self._section.firstBullet, editable=True,
                        eventIDs=[self._section.hazardEvent.getEventID()],
                        segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                        productCategory=self._productCategory, productID=self._product.productID,
                        useKeyAsLabel=self._showProductParts()) 
            sectionDict['description'] += self._section.firstBullet + '\n'

    def _timeBullet(self, sectionDict, productSegmentGroup, arguments):        
        timeBullet = self.getHazardTimePhrases(self._section.vtecRecord, self._section.hazardEvent, self._issueTime)
        self._tpc.setVal(sectionDict, 'timeBullet', timeBullet, editable=True, eventIDs=[self._section.hazardEvent.getEventID()],
                segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                productCategory=self._productCategory, productID=self._product.productID,
                useKeyAsLabel=self._showProductParts()) 
        sectionDict['description'] += timeBullet + '\n'

    def _basisBullet(self, sectionDict, productSegmentGroup, arguments):
        basisBullet = self.getBasisPhrase(self._section.vtecRecord, self._section.hazardEvent, self._section.metaData)
        if basisBullet:
            self._tpc.setVal(sectionDict, 'basisBullet', basisBullet, editable=True, eventIDs=[self._section.hazardEvent.getEventID()],
                    segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                    productCategory=self._productCategory, productID=self._product.productID,
                    useKeyAsLabel=self._showProductParts()) 
            sectionDict['description'] += basisBullet + '\n'
        
    def _impactsBullet(self, sectionDict, productSegmentGroup, arguments):
        impactsBullet = self.getImpactsPhrase(self._section.vtecRecord, self._section.hazardEvent, self._section.metaData)
        self._tpc.setVal(sectionDict, 'impactsBullet', impactsBullet, editable=True, eventIDs=[self._section.hazardEvent.getEventID()],
                    segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                    productCategory=self._productCategory, productID=self._product.productID,
                    useKeyAsLabel=self._showProductParts()) 
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
        basisAndImpactsStatement = '|* Current hydrometeorological situation and expected impacts *|\n'
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
            locationsAffected += "\n\n" + self._section.floodMoving + '. '
            
        if not locationsAffected:
            phen = self._section.vtecRecord.get("phen")
            sig = self._section.vtecRecord.get("sig")
            geoType = self._section.hazardEvent.get('geoType')
            if phen in ["FF", "FA", "TO", "SV", "SM", "EW" , "FL" ] and \
               geoType == 'area' and sig != "A" :
                if phen == "FF" :
                    locationsAffected = "Some locations that will experience flash flooding include..."
                elif phen == "FA" or phen == "FA" :
                    locationsAffected = "Some locations that will experience flooding include..."
                else :
                    locationsAffected = "Locations impacted include..."
                locationsAffected += self.impactedLocations(self._section.hazardEvent)
            else :
                locationsAffected = '|*Forecast path of flood and/or locations to be affected*|' + '\n'

        self._tpc.setVal(sectionDict, 'locationsAffected', locationsAffected, editable=True, label='Locations Affected',
                         eventIDs=[self._section.hazardEvent.getEventID()],
                         segment=self._productSegment.segment, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
        sectionDict['description'] += locationsAffected + '\n'

    def impactedLocations(self, hazardEvent) :
        nullReturn = "mainly rural areas of the aforementioned areas."
        columns = ["name", "warngenlev"]
        try :
            cityGeoms = self._tpc.mapDataQuery("city", columns, hazardEvent["geometry"])
        except :
            return nullReturn
        if not isinstance(cityGeoms, list) :
            return nullReturn
        names12 = []
        namesOther = []
        for cityGeom in cityGeoms :
            try:
                name = cityGeom.getString(columns[0])
                if not name:
                    continue
                levData = str(cityGeom.getString(columns[1]))
                if levData == "1" or levData == "2" :
                      names12.append(name)
                else :
                      namesOther.append(name)
            except :
                pass

        if len(names12) > 0 :
            return self._tpc.formatDelimitedList(names12)
        if len(namesOther) > 0 :
            return self._tpc.formatDelimitedList(namesOther)
        return nullReturn

    def _roadsAffected(self, sectionDict, productSegmentGroup, arguments):
        roadsAffected = self.affectedRoads(self._section.hazardEvent)
        if roadsAffected is None :
            return
        self._tpc.setVal(sectionDict, 'roadsAffected', roadsAffected, editable=True, label='Roads Affected',
                         eventIDs=[self._section.hazardEvent.getEventID()],
                         segment=self._productSegment.segment,
                         productCategory=self._productCategory, productID=self._product.productID)
        sectionDict['description'] += roadsAffected + '\n'
                
    def affectedRoads(self, hazardEvent) :
        # Access the list of roads and location relative to state
        #  e.g. I90 in Nebraska
        ugcs = hazardEvent.get("ugcs")
        import LocalizationInterface
        myLI = LocalizationInterface.LocalizationInterface()
        highwayInfo = myLI.getLocData("warngen/mileMarkers.xml", "COMMON_STATIC")
        if not isinstance(highwayInfo, dict) or not highwayInfo :
            return None
        geometries = hazardEvent["geometry"]
        stateMap = {}
        for ugc in ugcs :
            fullState = self._tpc.getInformationForUGC(ugc, "fullStateName")
            stateMap[ugc[0].lower()] = fullState
            stateMap[ugc[:2].lower()] = fullState

        # Loop through roads to gather the mile markers
        #   affectedRoadData = {
        #        "highway": [
        #                      (marker1, marker2),
        #                      (marker3, marker4),
        #                          ...
        #                    ]
        #      }
        #     
        columns = ["name", "id"]
        affectedRoadData = {}
        checked = set([])
        for tableList in highwayInfo.keys() :
            highway = highwayInfo[tableList]
            try :
                tableName = highway["pointSource"]
                if tableName in checked :
                    continue
                checked.add(tableName)
                try :
                    markerGeoms = self._tpc.mapDataQuery(tableName, columns, hazardEvent["geometry"])
                except :
                    markerGeoms = []
                prevId = -99999
                for markerGeom in markerGeoms :
                    try :
                        marker = str(markerGeom.getString(columns[0]))
                        if not marker :
                            continue
                        idVal = int(markerGeom.getString(columns[1]))
                        if not tableName in affectedRoadData :
                            affectedRoadData[tableName] = [[marker, marker]]
                        elif idVal == prevId + 1 :
                            affectedRoadData[tableName][-1][1] = marker
                        else:
                            affectedRoadData[tableName].append([marker, marker])
                        prevId = idVal
                    except :
                        pass
            except :
                pass

        if not affectedRoadData:
            return None

        # Translate the highway table names into plain language for route names
        # Must parse the table name because the route name is not explicitly mentioned in milemarkers.xml
        markerText = "This includes the following highways...\n"
        for road in affectedRoadData.keys() :
            lenRoadStr = len(road)
            i = 0
            while i < lenRoadStr and road[i] > "9" :
                i += 1
            if i >= lenRoadStr :
                continue
            j = i + 1
            while j < lenRoadStr and road[j] <= "9" :
                j += 1
            if road[0] == "i" :
                hwyDesc = "Interstate "
            elif road[0] == "h" :
                hwyDesc = "Highway "
            else :
                hwyDesc = "Route "
            hwyDesc += road[i:j]
            if road[-2:] == "mm" :
                suffix = road[j:-2]
            else :
                suffix = road[j:]
            if suffix != "" :
                state = stateMap.get(suffix[:2])
                if state == None :
                    state = stateMap.get(suffix[0])
                if state != None :
                    hwyDesc += " in " + state
            markerList = affectedRoadData[road]
            conjunction = ""
            for markerItem in markerList :
                if markerItem[0] == markerItem[1] :
                    rngDesc = " at mile marker " + markerItem[0]
                else :
                    rngDesc = " between mile marker " + markerItem[0] + \
                              " and " + markerItem[1]
                hwyDesc += conjunction + rngDesc
                conjunction = "...and"
            markerText += hwyDesc + ".\n"

        return markerText



    #  Point based
    
    def _attribution_point(self, sectionDict, productSegmentGroup, arguments):
        attribution = self._section.attribution
        if attribution:
            if self._section.bulletFormat == 'bulletFormat_CR':
                attribution = self._section.attribution + '\n'
            self._tpc.setVal(sectionDict, 'attribution_point', attribution,
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] += attribution + '\n'
        
       
    #  Point based
    
    def _attribution_point(self, sectionDict, productSegmentGroup, arguments):
        attribution = self._section.attribution
        if attribution:
            if self._section.bulletFormat == 'bulletFormat_CR':
                attribution = self._section.attribution + '\n'
            self._tpc.setVal(sectionDict, 'attribution_point', attribution,
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] += attribution + '\n'

    def _firstBullet_point(self, sectionDict, productSegmentGroup, arguments):
        if self._section.firstBullet:
            self._tpc.setVal(sectionDict, 'firstBullet_point', self._section.firstBullet, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
            sectionDict['description'] += self._section.firstBullet + '\n'
                
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
            # AT <ObsTime> THE <StgFlowName> WAS <ObsStg> <StgFlowUnits>.
            #  ObsTime --> hhmm am/pm <day> time_zone
            observedTime_ms = self._rfp.getObservedTime(self._productSegment.pointID)
            stageFlowName = self._rfp.getStageFlowName(self._productSegment.pointID)
            observedStage, shefQualityCode = self._rfp.getObservedStage(self._productSegment.pointID)
            self._stageFlowUnits = self._rfp.getStageFlowUnits(self._productSegment.pointID)
            # 900 AM EDT FRIDAY 
            observedTime = self._getFormattedTime(observedTime_ms, timeZones=self._productSegment.timeZones)
            bulletContent = 'At ' + observedTime + ' the ' + stageFlowName + ' was ' + `observedStage` + ' ' + self._stageFlowUnits + '.'
        self._tpc.setVal(sectionDict, 'observedStageBullet', bulletContent, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 

    def _floodStageBullet(self, sectionDict, productSegmentGroup, arguments):
        floodStage = self._rfp.getFloodStage(self._productSegment.pointID)
        if floodStage != self._rfp.MISSING_VALUE:
            # FLOOD STAGE IS <FldStg> <StgFlowUnits>.
            self._stageFlowUnits = self._rfp.getStageFlowUnits(self._productSegment.pointID)
            bulletContent = 'Flood stage is ' + `floodStage` + ' ' + self._stageFlowUnits + '.'
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
            bulletContent = observedCategoryName + ' flooding is occurring and ' + maxFcstCategoryName + ' flooding is forecast.'
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
            observedTime = self._getFormattedTime(observedTime_ms, timeZones=self._productSegment.timeZones)
            maxStage, shefQualityCode = self._rfp.getMaximum24HourObservedStage(self._productSegment.pointID)
            bulletContent = 'Recent Activity...The maximum river stage in the 24 hours ending at ' + observedTime + ' was ' + `maxStage` + ' feet. '            
        self._tpc.setVal(sectionDict, 'recentActivityBullet', bulletContent, formatMethod=self._section.bulletFormat,
                         productCategory=self._productCategory, productID=self._product.productID) 
    
    
    def _forecastStageBullet(self, sectionDict, productSegmentGroup, arguments):    
        observedStage = self._section.observedStage
        maximumForecastStage = self._section.maximumForecastStage
        floodStage = self._section.floodStage
        forecastCrestStage = self._section.forecastCrestStage
        forecastCrestTime_str = self._section.forecastCrestTime_str
        stageFlowUnits = self._section.stageFlowUnits
        maximumForecastTime_str = self._section.maximumForecastTime_str
        forecastFallBelowFloodStageTime_ms = self._section.forecastFallBelowFloodStageTime_ms
        forecastFallBelowFloodStageTime_str = self._section.forecastFallBelowFloodStageTime_str
        forecastRiseAboveFloodStageTime_str = self._section.forecastRiseAboveFloodStageTime_str
        riverDescription = self._getRiverDescription()
        
        bulletContent = ''
        # Create bullet content
        if self._section.maximumForecastStage == self._rfp.MISSING_VALUE :
                bulletContent = '|* Forecast is missing, insert forecast bullet here. *|'

        elif self._section.action != 'ROU':                    
            if observedStage == self._rfp.MISSING_VALUE:
                # FORECAST INFORMATION FOR NO OBS SITUATION    
                #  change made 3/17/2009 Mark Armstrong HSD
                #
                # condition: ( ( <ObsStg> EQ MISSING ) AND ( <MaxFcstStg> GE <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS FORECAST TO HAVE A MAXIMUM VALUE OF <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
                if maximumForecastStage >= floodStage:
                    bulletContent = riverDescription + ' is forecast to have a maximum value of ' + `maximumForecastStage` + ' ' + stageFlowUnits + \
                      ' by ' + maximumForecastTime_str + '. '
                #
                # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
                #  change made 3/17/2009 Mark Armstrong HSD
                #
                # condition: ( ( <ObsStg> EQ MISSING ) AND ( <MaxFcstStg> LT <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS FORECAST BELOW FLOOD STAGE WITH A MAXIMUM VALUE OF <MaxFcstStg> &
                # <StgFlowUnits> <MaxFcstTime>.
                elif maximumForecastStage < floodStage:
                    bulletContent = riverDescription + ' is forecast below flood stage with a maximum value of ' + `maximumForecastStage` + ' ' + stageFlowUnits + \
                      ' by ' + maximumForecastTime_str + '. '

            elif observedStage < floodStage:
                # Observed below flood stage/forecast to rise just to flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> EQ <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS EXPECTED TO RISE TO NEAR FLOOD STAGE <MaxFcstTime>.
                #
                if maximumForecastStage == floodStage:
                    bulletContent = riverDescription + ' is expected to rise to near flood stage by ' + maximumForecastTime_str
                    
                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/not falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND &
                # ( <HG,0,FF,X,NEXT> GT <FldStg> ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>.        
                #            
                elif forecastCrestStage > floodStage and forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by ' + forecastRiseAboveFloodStageTime_str + \
                        ' and continue to rise to near ' + `forecastCrestStage` + ' ' + stageFlowUnits + ' by ' + forecastCrestTime_str + '. '

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has no crest
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> GT &
                # <FldStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <MaxFcstStg> <StgFlowUnits> BY <MaxFcstTime>. &
                # ADDITIONAL RISES ARE POSSIBLE THEREAFTER.        
                #                               
                elif maximumForecastStage > floodStage and forecastCrestStage == self._rfp.MISSING_VALUE and +\
                    forecastFallBelowFloodStageTime == self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by ' + forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near ' + `maximumForecastStage` + ' ' + stageFlowUnits + ' by ' + \
                       maximumForecastTime_str + '. Additional rises are possible thereafter.'

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT &
                # <FldStg> ) AND ( <FcstFallFSTime> NE MISSING ) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY <HG,0,FF,X,NEXT,TIME>.&
                # THE RIVER WILL FALL BELOW FLOOD STAGE BY <FcstFallFSTime>.
                #
                elif forecastCrestStage > floodStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by ' + forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near ' + `forecastCrestStage` + ' ' + stageFlowUnits + ' by ' + forecastCrestTime_str + \
                       '. The river will fall below flood stage by ' + forecastFallBelowFloodStageTime_str + '. '
            
            else:  # observedStage >= floodStage:
                
                # Observed above flood stage/forecast continues above flood stage/no
                # crest in forecast time series
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> GT &
                # <ObsStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <MaxFcstStg> <StgFlowUnits> BY &
                # <MaxFcstTime>.  ADDITIONAL RISES MAY BE POSSIBLE THEREAFTER.
                #        
                if maximumForecastStage > observedStage and forecastCrestStage == self._rfp.MISSING_VALUE and \
                     forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                     bulletContent = riverDescription + ' will continue rising to near ' + `maximumForecastStage` + ' ' + stageFlowUnits + \
                     ' by ' + maximumForecastTime_str + '. Additional rises may be possible thereafter. '
            
                # Observed above flood stage/forecast crests but stays above flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) &
                # AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME> THEN BEGIN FALLING.
                #
                elif forecastCrestStage > observedStage and forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = riverDescription + ' will continue rising to near ' + `forecastCrestStage` + ' ' + stageFlowUnits + ' by ' + \
                        forecastCrestTime_str + ' then begin falling.'
                    
                # Observed above flood stage/forecast crests and falls below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING ) AND ( <FcstCrestStg> GT <ObsStg> ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>. THE RIVER WILL FALL BELOW FLOOD STAGE &
                # <FcstFallFSTime>.
                #
                elif forecastCrestStage > observedStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE and \
                    forecastCrestStage > observedStage:
                    bulletContent = riverDescription + ' will continue rising to near ' + `forecastCrestStage` + ' ' + stageFlowUnits + ' by ' + \
                       forecastFallBelowFloodStageTime_str + '. ' 
                        
                # Observed above flood stage/forecast continue fall/not below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "falling" ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO A STAGE OF <SpecFcstStg> <StgFlowUnits> BY &
                # <SpecFcstStgTime>.
                #
                elif maximumForecastStage <= observedStage and self._section.stageTrend == 'falling' and \
                    forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    # TODO Need SpecFcstStg and SpecFcstStgTime
                    bulletContent = ''
                    
                # Observed above flood stage/forecast is steady/not fall below flood stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "steady" ) AND ( <FcstFallFSTime> EQ MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL REMAIN NEAR <MaxFcstStg> <StgFlowUnits>.
                #
                elif maximumForecastStage <= observedStage and self._section.stageTrend == 'steady' and \
                    forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    bulletContent = riverDescription + ' will remain near ' + `maximumForecastStage` + ' ' + stageFlowUnits + '. '
                    
                # Observed above flood stage/forecast continues fall to below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO BELOW FLOOD STAGE BY &
                # <FcstFallFSTime>.
                #
                elif maximumForecastStage <= observedStage and forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = riverDescription + ' will continue to fall to below flood stage by ' + forecastFallBelowFloodStageTime_str + '.'

        elif self._section.action in ['ROU']:                    
            #  FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
            #
            # condition: ( ( <Action> SEQ "ROU" ) AND ( <MaxFcstStg> NE MISSING ) )
            # bulletstr: FORECAST...THE RIVER WILL RISE TO NEAR <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
            #
            if maximumForecastStage != self._rfp.MISSING_VALUE:
                bulletContent = riverDescription + ' will rise to near ' + `maximumForecastStage` + ' ' + stageFlowUnits + \
                      ' by ' + maximumForecastTime_str + '. '                       
        self._tpc.setVal(sectionDict, 'forecastStageBullet', bulletContent, formatMethod=self._section.bulletFormat, formatArgs='Forecast...',
                         productCategory=self._productCategory, productID=self._product.productID) 
        
    def _getRiverDescription(self):
        '''
        To use the actual river name:
        
        return self._section.riverName
        '''
        return 'The river'

    def _forecastStageBullet_new(self, sectionDict, productSegmentGroup, arguments):
        '''                        
        '''
        # From Mark Armstrong -- national baseline templates
        #    roundups2011.0331  -- 
        # TODO -- this is for FLW -- is this the same for FFA, FLS?
        bulletContent = ''
        
        # Create bullet content
        if section.action in ['NEW', 'CON', 'EXT']:  # MAA if action != ROU
                    
            if self._section.observedStage == self._rfp.MISSING_VALUE:
                # FORECAST INFORMATION FOR NO OBS SITUATION    
                #  change made 3/17/2009 Mark Armstrong HSD
                #
                # condition: ( ( <ObsStg> EQ MISSING_VALUE) AND ( <MaxFcstStg> GE <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS FORECAST TO HAVE A MAXIMUM VALUE OF <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
                if self._section.maximumForecastStage == self._rfp.MISSING_VALUE :
                    bulletContent = 'Forecast is missing, insert forecast bullet here.'

                elif self._section.maximumForecastStage >= self._section.floodStage:
                    if self._section.forecastRiseAboveFloodStageTime_ms != self._rfp.MISSING_VALUE and self._section.forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + self._section.riverName + ' is forecast to rise above flood stage at ' + self._section.forecastRiseAboveFloodStageTime_str\
                         + ' to ' + self._section.maximumForecastStage + ' ' + self._section.stageFlowUnits + ' and fall below flood stage at ' + self._section.forecastFallBelowFloodStageTime_str + '.'
                    elif self._section.forecastRiseAboveFloodStageTime_ms != self._rpf.MISSING_VALUE and self._section.forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + self._section.riverName + ' is forecast to rise above flood stage at ' + self._section.forecastRiseAboveFloodStageTime_str\
                         + ' to ' + self._section.maximumForecastStage + ' ' + self._section.stageFlowUnits + '.'
                    elif self._section.forecastRiseAboveFloodStageTime_ms == self._rfp.MISSING_VALUE and self._section.forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + self._section.riverName + ' is forecast to reach ' + self._section.maximumForecastStage + ' ' + self._section.stageFlowUnits\
                         + ' and fall below flood stage at ' + self._section.forecastFallBelowFloodStageTime_str + '.'
                    elif self._section.forecastRiseAboveFloodStageTime_ms == self._rfp.MISSING_VALUE and self._section.forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + self._section.riverName + ' is forecast to reach ' + self._section.maximumForecastStage + ' ' + self._section.stageFlowUnits + \
                        ' by ' + self._section.maximumForecastTime_str + '.'
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
                elif self._section.maximumForecastStage < self._section.floodStage:
                    if self._section.forecastCrestStage == self._rfp.MISSING_VALUE:
                        bulletContent = 'The ' + riverName + ' is forecast to reach ' + self._section.maximumForecastStage + ' ' + self._section.stageFlowUnits + \
                        ' by ' + self._section.maximumForecastTime_str + '.'                    
                    else :
                        bulletContent = 'The ' + self._section.riverName + ' is forecast to crest at ' + self._section.forecastCrestStage + ' ' + self._section.stageFlowUnits + \
                        ' by ' + self._section.forecastCrestTime + '. '

            elif observedStage < floodStage:
                # Observed below flood stage/forecast to rise just to flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> EQ <FldStg> ) )
                # bulletstr: FORECAST...THE RIVER IS EXPECTED TO RISE TO NEAR FLOOD STAGE <MaxFcstTime>.
                #
                if self._section.maximumForecastStage == self._section.floodStage:
                    bulletContent = 'The river is expected to rise to near flood stage by ' + self._section.maximumForecastTime_str
                    
                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/not falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND &
                # ( <HG,0,FF,X,NEXT> GT <FldStg> ) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>.        
                #            
                elif self._section.rfcCrest > self._section.floodStage and self._section.forecastFallBelowFloodStageTime == self._rfp.MISSING:
                    bulletContent = 'rise above flood stage by ' + self._section.forecastRiseAboveFloodStageTime_str + \
                        ' and continue to rise to near ' + `self._section.rfcCrest` + ' ' + self._section.stageFlowUnits + ' by ' + self._section.rfcCrestTime + '. '

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has no crest
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <MaxFcstStg> GT &
                # <FldStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING_VALUE) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <MaxFcstStg> <StgFlowUnits> BY <MaxFcstTime>. &
                # ADDITIONAL RISES ARE POSSIBLE THEREAFTER.        
                #                               
                elif self._section.maximumForecastStage > self._section.floodStage and rfcCrest == self._rfp.MISSING_VALUE and +\
                    self._section.forecastFallBelowFloodStageTime == self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by ' + self._section.forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near ' + `self._section.maximumForecastStage` + ' ' + self._section.stageFlowUnits + ' by ' + \
                       self._section.maximumForecastTime_str + '. Additional rises are possible thereafter.'

                # Observed below flood stage/forecast above flood stage/forecast time
                # series has a crest/falling below flood stage
                #
                # condition: ( ( <ObsStg> LT <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT &
                # <FldStg> ) AND ( <FcstFallFSTime> NE MISSING_VALUE) )
                # bulletstr: FORECAST...RISE ABOVE FLOOD STAGE BY <FcstRiseFSTime> &
                # AND CONTINUE TO RISE TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY <HG,0,FF,X,NEXT,TIME>.&
                # THE RIVER WILL FALL BELOW FLOOD STAGE BY <FcstFallFSTime>.
                #
                elif self._section.rfcCrest > self._section.floodStage and self._section.forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = 'rise above flood stage by ' + self._section.forecastRiseAboveFloodStageTime_str + \
                       ' and continue to rise to near ' + `self._section.rfcCrest` + ' ' + self._section.stageFlowUnits + ' by ' + self._section.rfcCrestTime + \
                       '. The river will fall below flood stage by ' + self._section.forecastFallBelowFloodStageTime_str + '. '
            
            else:  # observedStage >= floodStage:
                
                # Observed above flood stage/forecast continues above flood stage/no
                # crest in forecast time series
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> GT &
                # <ObsStg> ) AND ( <HG,0,FF,X,NEXT> EQ MISSING_VALUE) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <MaxFcstStg> <StgFlowUnits> BY &
                # <MaxFcstTime>.  ADDITIONAL RISES MAY BE POSSIBLE THEREAFTER.
                #        
                if self._section.maximumForecastStage > self._section.observedStage and rfcCrest == self._rfp.MISSING_VALUE and \
                     self._section.forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                     bulletContent = 'The river will continue rising to near ' + `self._section.maximumForecastStage` + ' ' + self._section.stageFlowUnits + \
                     ' by ' + self._section.maximumForecastTime_str + '. Additional rises may be possible thereafter. '
            
                # Observed above flood stage/forecast crests but stays above flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) &
                # AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME> THEN BEGIN FALLING.
                #
                elif self._section.rfcCrest > self._section.observedStage and self._section.forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                        bulletContent = 'The river will continue rising to near ' + `self._section.rfcCrest` + ' ' + self._section.stageFlowUnits + ' by ' + \
                        self._section.rfcCrestTime + ' then begin falling.'
                    
                # Observed above flood stage/forecast crests and falls below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <HG,0,FF,X,NEXT> GT <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING_VALUE) AND ( <FcstCrestStg> GT <ObsStg> ) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE RISING TO NEAR <HG,0,FF,X,NEXT> <StgFlowUnits> BY &
                # <HG,0,FF,X,NEXT,TIME>. THE RIVER WILL FALL BELOW FLOOD STAGE &
                # <FcstFallFSTime>.
                #
                elif self._section.rfcCrest > self._section.observedStage and self._section.forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE and \
                    self._section.forecastCrestStage > self._section.observedStage:
                    bulletContent = 'The river will continue rising to near ' + `self._section.rfcCrest` + ' ' + self._section.stageFlowUnits + ' by ' + \
                       self._section.forecastFallBelowFloodStageTime_ms + '. ' 
                        
                # Observed above flood stage/forecast continue fall/not below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "falling" ) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO A STAGE OF <SpecFcstStg> <StgFlowUnits> BY &
                # <SpecFcstStgTime>.
                #
                elif self._section.maximumForecastStage <= self._section.observedStage and self._section.stageTrend == 'falling' and \
                    self._section.forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    # TODO Need SpecFcstStg and SpecFcstStgTime
                    bulletContent = ''
                    
                # Observed above flood stage/forecast is steady/not fall below flood stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <StgTrend> SEQ "steady" ) AND ( <FcstFallFSTime> EQ MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL REMAIN NEAR <MaxFcstStg> <StgFlowUnits>.
                #
                elif self._section.maximumForecastStage <= self._section.observedStage and self._section.stageTrend == 'steady' and \
                    self._section.forecastFallBelowFloodStageTime_ms == self._rfp.MISSING_VALUE:
                    bulletContent = 'The river will remain near ' + `self._section.maximumForecastStage` + ' ' + self._section.stageFlowUnits + '. '
                    
                # Observed above flood stage/forecast continues fall to below flood
                # stage
                #
                # condition: ( ( <ObsStg> GE <FldStg> ) AND ( <MaxFcstStg> LE <ObsStg> ) AND &
                # ( <FcstFallFSTime> NE MISSING_VALUE) )
                # bulletstr: FORECAST...THE RIVER WILL CONTINUE TO FALL TO BELOW FLOOD STAGE BY &
                # <FcstFallFSTime>.
                #
                elif self._section.maximumForecastStage <= self._section.observedStage and self._section.forecastFallBelowFloodStageTime_ms != self._rfp.MISSING_VALUE:
                    bulletContent = 'The river will continue to fall to below flood stage by ' + self._section.forecastFallBelowFloodStageTime_str + '.'

        elif self._section.action in ['ROU']:                    
            #  FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
            #
            # condition: ( ( <Action> SEQ "ROU" ) AND ( <MaxFcstStg> NE MISSING_VALUE) )
            # bulletstr: FORECAST...THE RIVER WILL RISE TO NEAR <MaxFcstStg> <StgFlowUnits> <MaxFcstTime>.
            #
            if self._section.maximumForecastStage != self._rfp.MISSING_VALUE:
                bulletContent = 'The river will rise to near ' + `self._section.maximumForecastStage` + ' ' + self._section.stageFlowUnits + \
                      ' by ' + self._section.maximumForecastTime_str + '. '                       
        self._tpc.setVal(sectionDict, 'forecastStageBullet', bulletContent, formatMethod=self._section.bulletFormat, formatArgs='Forecast...',
                         productCategory=self._productCategory, productID=self._product.productID)
    
    def _pointImpactsBullet(self, sectionDict, productSegmentGroup, arguments):
        # Pull out the list of chosen impact text fields
        hazardEvent = self._section.hazardEvent
        
        impacts = []
        validVals = hazardEvent.get('impactCheckBoxes')
        
        if validVals is None:
            return None
        
        for key in validVals:
            if key.startswith('impactCheckBox_'):
                height, impactValue = self._parseImpactKey(key)
                value = hazardEvent.get(key)
                textFieldName = 'impactTextField_'+impactValue
                impacts.append((height, hazardEvent.get(textFieldName)))
                    
                    
        impactStrings = []
        for height, textField in impacts:  
            impactString = '* Impact...At ' + height + ' feet...'+textField
            impactStrings.append(impactString)
        if impactStrings:
            impactBulletsString = '\n'.join(impactStrings)
        else:
            impactBulletsString = ''
        self._tpc.setVal(sectionDict, 'pointImpactsBullet', impactBulletsString, 
                  productCategory=self._productCategory, productID=self._product.productID)          
               
             
    def _parseImpactKey(self, key):
       parts = key.rsplit('_')
       if len(parts) > 1:
           impactValue = parts[1]
           height = impactValue.rsplit('-')[0]
       else:
           impactValue = ''
           height = ''
       return height, impactValue
   
    def _floodHistoryBullet(self, sectionDict, productSegmentGroup, arguments):
        '''
        FLOOD HISTORY...THIS CREST COMPARES TO A PREVIOUS CREST OF <HistCrestStg> <ImpCompUnits> on <HistCrestDate>.
        '''
        
        pointID = self._productSegment.pointID
        units = self._rfp.getImpactCompUnits(pointID)
        productSegment_tuple, vtecRecord, formatArgs = arguments
        crestString = ''
        crestContents = self._section.hazardEvent.get('crestsSelectedForecastPointsComboBox')
        if crestContents is not None:
            crest,crestDate = crestContents.split(' ')
            crestString = "This crest compares to a previous crest of " + crest + " " + units + " on " + crestDate +"."
        self._tpc.setVal(sectionDict, 'floodHistoryBullet', crestString, formatMethod=self._section.bulletFormat, formatArgs='FLOOD HISTORY...',
                         productCategory=self._productCategory, productID=self._product.productID) 
           
    def _endingSynopsis(self, sectionDict, productSegmentGroup, arguments):
        # Try to get from hazard event meta data
        endingSynopsis = self._section.hazardEvent.get('endingSynopsis')
        if endingSynopsis is None:
            # Try to get from dialogInputMap  (case of partial cancellation)
            endingSynopsis = self._dialogInputMap.get('endingSynopsis')
        if not endingSynopsis:
            # Default value
            endingSynopsis = '|* Brief post-synopsis of hydrometeorological activity *|'
        sectionDict['endingSynopsis'] = endingSynopsis
            
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
            ugcEntry = collections.OrderedDict()
            ugcEntry['state'] = self._tpc.getInformationForUGC(ugc, "stateAbrev")
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
               operationalMode=opMode, testHarnessMode=False, vtecProduct=self._vtecProduct, issueFlag=self._issueFlag)
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
                        
            newHazardEvents.append(hazardEvent)
 
        return newHazardEvents

    def getSegmentMetaData(self, segment) :
        '''
        @param: eventInfo
        '''
        # Get meta data for this segment
        #  May need to get multiple hazardEvents and meta data
        metaDataList = []
        segmentEvents = self.getSegmentHazardEvents([segment])
        for hazardEvent in segmentEvents:
            metaDataList.append((self.getHazardMetaData(hazardEvent), hazardEvent))
        return metaDataList, segmentEvents
    
    def getHazardMetaData(self, hazardEvent):
        phen = hazardEvent.getPhenomenon()   
        sig = hazardEvent.getSignificance()  
        subType = hazardEvent.getSubType()  
        criteria = {'dataType':'hazardMetaData_filter',
                'filter':{'phen':phen, 'sig':sig, 'subType':subType}
                }
        metaData, filePath = self.bridge.getData(json.dumps(criteria))            
        if type(metaData) is not types.ListType:
            metaData = metaData.execute(hazardEvent, {}) 
        return metaData

    def getMetaData(self, hazardEvents, metaDict, metaDataFileName): 
        eventDicts = []
        for hazardEvent in hazardEvents:
            eventDict = {}
            eventDict['eventID'] = hazardEvent.getEventID()
            eventDict['hazardType'] = hazardEvent.getHazardType()
            eventDict['status'] = hazardEvent.getStatus()
            eventDicts.append(eventDict)
        criteria = {'dataType':'metaData', 'fileName':metaDataFileName}
        metaData = self.bridge.getData(json.dumps(criteria)) 
        if type(metaData) is not types.ListType:
            metaData = metaData.execute(eventDicts, metaDict) 
        return metaData


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
                if ('CAN' in vtecCodes or 'EXP' in vtecCodes):
                    ended = True
                    for code in ['NEW', 'CON', 'EXA', 'EXT', 'EXB', 'UPG', 'ROU']:
                        if code in vtecCodes:
                            ended = False
                            break
                    if ended:
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
        @param hazardEventList: Set of hazardEvents 
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
                mode = self._sessionDict.get('hazardMode', 'PRACTICE').upper()
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
                
            if immediateCause in ['DM', 'DR', 'GO', 'IJ', 'RS', 'SM']:
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
                        return 'The ' + riverName + ' below ' + damOrLeveeName + ' in ' + ugcPhrase
                else:
                    typeOfFlooding = self.hydrologicCauseMapping(hydrologicCause, 'typeOfFlooding')
                    return typeOfFlooding + ' in ' + ugcPhrase                
            return ugcPhrase
        else:
            #  <River> <Proximity> <IdName> 
            riverName = self._rfp.getGroupName(productSegment.pointID)
            proximity = self._rfp.getRiverPointProximity(productSegment.pointID) 
            riverPointName = self._rfp.getRiverPointName(productSegment.pointID) 
            return  '\n the ' + riverName + ' ' + proximity + ' ' + riverPointName              
                             
    def getAreaPhraseBullet(self, productSegment, metaData, hazardEvent):
        '''
        @param productSegment object
        @param metaData
        @param hazardEvent -- representative for the segment
        @return: Plain language list of counties/zones in the hazard appropriate
                 for bullet format procducts
        ''' 
        ugcs = hazardEvent.get('ugcs', [])
        ugcPortions = hazardEvent.get('ugcPortions', {})
        ugcPartsOfState = hazardEvent.get('ugcPartsOfState', {})

        # These need to be ordered by area of state.
        orderedUgcs = []
        for ugc in ugcs :
            orderedUgcs.append(ugc[:2] + ugcPartsOfState.get(ugc, "") + "|" + ugc)
        orderedUgcs.sort()

        areaPhrase = "\n"
        for ougc in orderedUgcs :
            ugc = ougc.split("|")[1]
            part = ugcPortions.get(ugc, "")
            if part == "" :
                textLine = "  "
            else :
                textLine = "  " + part + " "
            textLine += self._tpc.getInformationForUGC(ugc) + " "
            textLine += self._tpc.getInformationForUGC(ugc, "typeSingular") + " IN "
            part = ugcPartsOfState.get(ugc, "")
            if part == "" :
                textLine += self._tpc.getInformationForUGC(ugc, "fullStateName") + "\n"
            else :
                textLine += part + " " + self._tpc.getInformationForUGC(ugc, "fullStateName") + "\n"
            areaPhrase += textLine

        return areaPhrase
            
                             
    def getAttributionPhrase(self, vtecRecord, hazardEvent, areaPhrase, issueTime, testMode, wfoCity, lineLength=69, endString='.'):
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

        # Use this to determine which first bullet format to use.
        phen = vtecRecord.get("phen")

        hazName = self._tpc.hazardName(vtecRecord['hdln'], testMode, False)

        if len(vtecRecord['hdln']):
            action = vtecRecord['act']
            
           # Handle special cases
            if action == 'EXT' and self._product.productID in ['FFA', 'FLW', 'FLS'] and self._product.geoType == 'point':
                # Use continuing wording for EXT
                action = 'CON'
                                
            if action == 'NEW':
                attribution = nwsPhrase + 'issued a'
                headPhrase = hazName + ' for'
                headPhrase += ' ' + areaPhrase + endString
    
            elif action == 'CON':
                attribution = 'the ' + hazName + ' remains in effect for'
                headPhrase = areaPhrase + endString
    
            elif action == 'EXA':
                attribution = nwsPhrase + 'expanded the'
                headPhrase = hazName + ' to include'
                headPhrase = ' ' + areaPhrase + endString
    
            elif action == 'EXT':
                if action in 'EXT' and self._product.productID in ['FFA', 'FLW', 'FLS'] and self._product.geoType == 'area':
                    attribution = nwsPhrase + 'extended the '
                else:
                    attribution = 'the ' + hazName + ' is now in effect for' 
                headPhrase = ' ' + areaPhrase + endString
                    
            elif action == 'EXB':
                attribution = nwsPhrase + 'expanded the'
                headPhrase = hazName + ' to include'
                headPhrase = ' ' + areaPhrase + endString
    
            elif action == 'CAN':
                attribution = 'the ' + hazName + \
                   ' for ' + areaPhrase + ' has been cancelled ' + endString
    
            elif action == 'EXP':
                expTimeCurrent = issueTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' has expired ' + endString
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
        # endTimePhrase = self._tpc.substituteBulletedText(endTimePhrase, 'TIME IS MISSING', 'DefaultOnly', lineLength)
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
        timeOfStage = self._tpc.getFormattedTime(time.mktime(stageTime.timetuple()), '%I:%M %p %A',
                                                 stripLeading=True, timeZones=self._productSegment.timeZones) 
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
                crestTime = self._tpc.getFormattedTime(int(crest), '%A %p', stripLeading=True,
                                                        timeZones=self._productSegment.timeZones)  # 'Monday Morning'
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
    
    def standardBasisPhrase(self, vtecRecord, hazardEvent, metaData, lineLength):            
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
        # basisPhrase = self._tpc.substituteBulletedText(basis, default, framing, lineLength)            
        return basis
    
    def floodBasisPhrase(self, vtecRecord, hazardEvent, metaData, floodDescription, lineLength=69):
        #  Time is off of last frame of data
        try :
            eventTime = self._sessionDict['framesInfo']['frameTimeList'][-1]
        except :
            eventTime = vtecRecord.get('startTime')            

        eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z', stripLeading=True,
                                                timeZones=self._productSegment.timeZones)
        para = 'At ' + eventTime
        # TODO Need to handle these cases properly
        if hazardEvent.getHazardType() == "FL.W" or hazardEvent.getHazardType() == "HY.S":
            basis = ' ' + floodDescription + ' ' + self.basisLocation(hazardEvent) + '.'
        else:
            basis = self.basisFromHazardEvent(hazardEvent)        
        para += basis
        motion = self.wxHazardMotion(hazardEvent, \
                  still='This storm was stationary', \
                  slow='. This storm was nearly stationary.')

        if motion is not None :
            para += motion
        return para
    
    def basisFromHazardEvent(self, hazardEvent):
        hazardType = hazardEvent.getHazardType()
        hazardEventAttributes = hazardEvent.getHazardAttributes()
        result = self.basisText.getBulletText(hazardType, hazardEventAttributes)
        result = self._tpc.substituteParameters(hazardEvent, result)
        return result
    
    def floodLocation(self, hazardEvent, floodDescription):
        return ' ' + floodDescription + ' ' + self.basisLocation(hazardEvent)
    
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
        # impactsPhrase = self._tpc.substituteBulletedText(impacts,
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
            ugcName = self._tpc.getInformationForUGC(ugc, "entityName")
            if ugcName == "" :
                continue
            portion = ''
            if ugc[:2] == 'LA':
                equiv = 'PARISH'
            else:
                equiv = 'COUNTY'
            fullState = self._tpc.getInformationForUGC(ugc, "fullStateName")
            stateAbbrev = self._tpc.getInformationForUGC(ugc, "stateAbrev")
            partOfState = self._tpc.getInformationForUGC(ugc, "partOfState")
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
    
    def _getFormattedTime(self, time_ms, format=None, stripLeading=True, emptyValue=None, timeZones=['GMT']): 
        if not time_ms:
            return emptyValue
        if format is None:
            format = '%I%M %p %A %Z '
        return self._tpc.getFormattedTime(
                time_ms, format, stripLeading=stripLeading, timeZones=timeZones)
               
    def formatPolygonForEvent(self, hazardEvent):
        for polygon in self._extractPolygons(hazardEvent):
            polyStr = 'LAT...LON'
            # 4 points per line
            pointsOnLine = 0
            for lon, lat in polygon:              
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

    def _plainTextOfBearing(self, bearing, byFrom=False) :
        bearingDict = {
             45: 'northeast',
             90: 'east',
            135: 'southeast',
            180: 'south',
            225: 'southwest',
            270: 'west',
            315: 'northwest',
            }
        if bearing < 0 :
            bearing += 360
        if byFrom :
            if bearing > 180 :
                bearing -= 180
            else :
                bearing += 180
        b45 = 45 * (int)((bearing + 22.5) / 45)
        return  bearingDict.get(b45, 'north')

 # Return None if for some reason the motion was not available.
    def wxHazardMotion(self, hazardEvent, useMph=True,
                        still='stationary', slow='nearly stationary',
                        lead='...moving', trail='',
                        minSpd=2.5, round=5.0) :
        stormMotion = hazardEvent.get('stormMotion')
        if stormMotion is None :
            return None
        if stormMotion == None :
            return None
        try :
            speed = stormMotion.get('speed')
            if speed < 0 :
                return None
            if speed <= minSpd / 10 :
                return still
            if useMph :
                speed *= 1.16
            if speed < minSpd :
                return slow
            bearing = self._plainTextOfBearing(stormMotion.get('bearing'), True) + ' '
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
        except :
            return None
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
            for j in range(0, len(vtecRecordList)):
                vtecRecord = vtecRecordList[j]
                if vtecRecord['vtecRecordType'] == 'pvtecRecord':
                    action = vtecRecord['action']
                    vtecRecord['action'] = 'COR'
                    vtecString = vtecRecord['vtecString']
                    updatedVtecString = vtecString.replace(action, 'COR')
                    vtecRecord['vtecString'] = updatedVtecString
                    
        return segment

    def formatTimeMotionLocationForEvent(self, hazardEvent) :

        # Pick up the structures we need from the attributes, exit
        # with null result if any missing.
        try :
            st = hazardEvent.getStartTime()
            try :
                startTime = long(st.strftime("%s")) * 1000
            except :
                startTime = st
            stormMotion = hazardEvent.get("stormMotion")
            trackPoints = hazardEvent.get("trackPoints")
            nTrack = len(trackPoints)
            wxEventTime = hazardEvent.get("lastFrameTime")
            if startTime is None or stormMotion is None or trackPoints is None or \
               wxEventTime is None or nTrack == 0:
                return None
        except :
            return None

        # Identify the tracking points that bracket the startTime
        beforeTime = None
        afterTime = None
        try :
            nTrack -= 1
            while (nTrack > 0) :
                if (trackPoints[nTrack - 1]["pointID"] < startTime) :
                    break
                nTrack -= 1
            trackPoint = trackPoints[nTrack]
            point = trackPoint['point']
            afterLatLon = LatLonCoord.LatLonCoord(point[1], point[0])
            afterTime = trackPoint["pointID"] / 1000
            t = 0
            while t < nTrack :
                if (trackPoints[t + 1]["pointID"] >= startTime) :
                    break
                t += 1
            if t < nTrack :
                trackPoint = trackPoints[t]
                point = trackPoint['point']
                beforeLatLon = LatLonCoord.LatLonCoord(point[1], point[0])
                beforeTime = trackPoint["pointID"] / 1000
        except :
            return None
        if afterTime is None :
            return None
        startTime /= 1000
        wxEventTime /= 1000

        # For now we make the linear tracking assumption.
        speed = stormMotion["speed"]
        if speed == 0 or afterTime == startTime or beforeTime == startTime :
            bearing = stormMotion["bearing"]
            if beforeTime == startTime :
                useLatLon = beforeLatLon
            else :
                useLatLon = afterLatLon
        elif beforeTime == None :
            db = DistanceBearing.DistanceBearing(afterLatLon)
            bearing = stormMotion["bearing"]
            d = speed * (afterTime - startTime) / 1944
        elif startTime > afterTime :
            db = DistanceBearing.DistanceBearing(afterLatLon)
            (d, b) = db.getDistanceBearing(beforeLatLon)
            bearing = b
            if b > 180 :
                b -= 180
            else :
                b += 180
            d = d * (startTime - afterTime) / (afterTime - beforeTime)
            useLatLon = db.getLatLon(d, b)
        elif startTime < beforeTime :
            db = DistanceBearing.DistanceBearing(beforeLatLon)
            (d, b) = db.getDistanceBearing(afterLatLon)
            if b > 180 :
                b -= 180
            else :
                b += 180
            bearing = b
            d = d * (beforeTime - startTime) / (afterTime - beforeTime)
            useLatLon = db.getLatLon(d, b)
        else :
            db = DistanceBearing.DistanceBearing(afterLatLon)
            (d, b) = db.getDistanceBearing(beforeLatLon)
            d = d * (afterTime - startTime) / (afterTime - beforeTime)
            useLatLon = db.getLatLon(d, b)
            bearing = b

        bearing = int(0.5 + bearing)
        speed = int(0.5 + speed)
        daytime = startTime % 86400
        hh = daytime / 3600
        mm = (daytime % 3600) / 60
        tmlLine = "TIME...MOT...LOC " + "%2.2d" % hh + "%2.2d" % mm + "Z " + \
                  "%3.3d" % bearing + "DEG " + "%d" % speed + "KT "
        tmlLine += "%d" % int(0.5 + useLatLon.lat * 100) + " "
        if useLatLon.lon < 0 :
            tmlLine += "%d" % int(0.5 - useLatLon.lon * 100)
        elif useLatLon.lat < 40 :
            tmlLine += "%d" % int(0.5 + useLatLon.lon * 100)
        else :
            tmlLine += "%d" % int(0.5 - (useLatLon.lon - 360) * 100)

        return tmlLine

    def affectedDrainages(self, hazardEvent, \
             lead="Affected drainages include...", \
             trail=".") :
        
        columns = ["name"]
        try :
            basinGeoms = self._tpc.mapDataQuery("basins", columns, \
                                                hazardEvent["geometry"], True)
        except :
            return None
        if not isinstance(basinGeoms, list) :
            return None
        basinNames = set([ ])
        for basinGeom in basinGeoms :
            try :
                basinName = basinGeom.getString(columns[0])
                if basinName != "" :
                    basinNames.add(basinName)
            except :
                pass

        paraText = self._tpc.formatDelimitedList(basinNames)
        if len(paraText) == 0 :
            return None
        return lead + paraText + trail


    def basisLocation(self, hazardEvent,
             noevent='from heavy rain. This rain was located', \
             point='from a thunderstorm. This storm was located', \
             line='from a line of thunderstorms.  These storms were located', \
             lead='', \
             trail='over the warned area', \
             useMiles=True, over=2.0, near=3.5, round=5.0) :

        #  Time is off of last frame of data
        attributes = hazardEvent["attributes"]
        eventTime = attributes.get("lastFrameTime")

        # Handle no event time
        if eventTime is None :
            return self.setDefaultBasisLocation(noevent, lead, trail)        

        pointText = point
        lineText = line

        columns = ["name", "state", "warngenlev"]
        try :
            cityList = self._tpc.mapDataQuery("city", columns, hazardEvent["geometry"])
        except :
            cityList = []

        lastFrameTrackPoint = None
        firstFrameTrackPoint = None
        trackPoints = attributes.get("trackPoints")
        if trackPoints is not None :
            firstFrameTrackPoint = trackPoints[0]
            for trackPoint in trackPoints :
                if trackPoint["pointID"] > eventTime :
                    break
                lastFrameTrackPoint = trackPoint

        wxLatLon = None
        wxTime = None
        try :
            wxTime = lastFrameTrackPoint['pointID']
            point = lastFrameTrackPoint['point']
            wxLatLon = LatLonCoord.LatLonCoord(point[1], point[0])
        except :
            return self.setDefaultBasisLocation(noevent, lead, trail)

        coslat = math.cos(HazardConstants.DEG_TO_RAD * wxLatLon.lat)
        level1CityShape = None
        nearCityShape = None
        bestd2 = 999999999.0
        for cityLevel in [1, 2, 3] :
            if cityLevel == 3 and nearCityShape is not None :
                break
            for cityGeom in cityList :
                try :
                    if str(cityGeom.getString("warngenlev")) != str(cityLevel) :
                        continue
                    cityLatLon = LatLonCoord.LatLonCoord(\
                       cityGeom.getGeometry().y, cityGeom.getGeometry().x)
                except :
                    continue
                dlat = cityLatLon.lat - wxLatLon.lat
                dlon = (cityLatLon.lon - wxLatLon.lon) * coslat
                d2 = dlat * dlat + dlon * dlon
                if d2 >= bestd2 :
                    continue
                bestd2 = d2
                if cityLevel == 1 :
                    level1CityShape = cityGeom
                    nearCityShape = cityGeom
                else :
                    nearCityShape = cityGeom

        # If no level one city in the hazard area, look further
        if level1CityShape is None :
            dlat = 2.0
            dlon = dlat / coslat
            vertices = [ (wxLatLon.lon - dlon, wxLatLon.lat - dlat),
                         (wxLatLon.lon - dlon, wxLatLon.lat + dlat),
                         (wxLatLon.lon + dlon, wxLatLon.lat + dlat),
                         (wxLatLon.lon + dlon, wxLatLon.lat - dlat),
                         (wxLatLon.lon - dlon, wxLatLon.lat - dlat) ]
            polygon = Polygon(vertices)
            try :
                cityGeoms = self._tpc.mapDataQuery("city", columns, polygon, False, \
                                                   "1", "warngenlev")
            except :
                cityGeoms = []
            bestd2 = 999999999.0
            for cityGeom in cityGeoms :
                try :
                    cityLatLon = LatLonCoord.LatLonCoord(\
                       cityGeom.getGeometry().y, cityGeom.getGeometry().x)
                except :
                    continue
                dlat = cityLatLon.lat - wxLatLon.lat
                dlon = (cityLatLon.lon - wxLatLon.lon) * coslat
                d2 = dlat * dlat + dlon * dlon
                if d2 >= bestd2 :
                    continue
                bestd2 = d2
                level1CityShape = cityGeom

        # Most common case...note wx location based on nearby cities/landmarks.
        # First sanity check some parameters.
        if round < 1 :
            round = 1
        if over < 1 :
            over = 1
        if near < over :
            near = over

        # Do the level one city first, we could use it later with county fallback.
        level1CityDesc = ""
        if level1CityShape is not None and level1CityShape != nearCityShape :
            level1CityDesc = self.cityReferenceLocation(level1CityShape)
        if nearCityShape is not None :
            nearDesc = self.cityReferenceLocation(nearCityShape)
            nearDesc = pointText + " " + nearDesc
            if not level1CityDesc:
                return nearDesc
            return nearDesc + "...or about " + level1CityDesc

        # Now we fall back to describing in terms of rural areas of counties if doable.
        ugcs = attributes.get("ugcs", [])
        ugcParts = attributes.get("ugcPortions", {})
        dlat = 0.002
        dlon = dlat / coslat
        vertices = [ (wxLatLon.lon - dlon, wxLatLon.lat - dlat),
                     (wxLatLon.lon - dlon, wxLatLon.lat + dlat),
                     (wxLatLon.lon + dlon, wxLatLon.lat + dlat),
                     (wxLatLon.lon + dlon, wxLatLon.lat - dlat),
                     (wxLatLon.lon - dlon, wxLatLon.lat - dlat) ]
        polygon = Polygon(vertices)
        columns = ["countyname", "state", "fips"]
        try :
            countyGeoms = self._tpc.mapDataQuery("county", columns, polygon, True)
        except :
            countyGeoms = []
        for countyGeom in countyGeoms :
            try :
                st = countyGeom.getString("state")
                ugc = st + "C" + countyGeom.getString("fips")[2:]
                if not ugc in ugcs :
                    continue
                ruralDesc = pointText + " over mainly rural areas of "
                part = ugcParts.get(ugc, "")
                if part != "" :
                    ruralDesc += part + " "
                ruralDesc += countyGeom.getString("countyname")
                ruralDesc += " " + self._tpc.getInformationForUGC(ugc, "typeSingular")
                if level1CityDesc == "" :
                    return ruralDesc
                return ruralDesc + "...or about " + level1CityDesc
            except :
                pass

        # Nothing else to do, old default logic.
        return self.setDefaultBasisLocation(noevent, lead, trail)
    
    def setDefaultBasisLocation(self, noevent, lead, trail):
        if lead == '-' :
            return noevent
        wxLoc = ''
        if lead:
            wxLoc = lead + ' '
        wxLoc += noevent
        if trail:
            wxLoc += ' ' + trail
        return wxLoc
    
    def cityReferenceLocation(self, cityShape):
        mainDesc = cityShape.getString(columns[0])
        cityLatLon = LatLonCoord.LatLonCoord(\
                   cityShape.getGeometry().y, cityShape.getGeometry().x)
        distBearing = DistanceBearing.DistanceBearing(cityLatLon)
        (dist, bearing) = distBearing.getDistanceBearing(wxLatLon)
        if useMiles :
            dist = dist / 1.61
        else :
            dist = 0.54 * dist
        if dist < over :
            mainDesc = "over " + mainDesc
        elif dist < near :
            mainDesc = "near " + mainDesc
        else :
            dist = str(int(round * int((dist + round / 2) / round)))
            bearing = self._plainTextOfBearing(bearing)
            if useMiles :
                mainDesc = dist + " miles " + bearing + " of " + mainDesc
            else :
                mainDesc = dist + " nautical miles " + bearing + " of " + mainDesc
        return cityDesc
            


    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

