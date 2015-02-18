'''
    Description: The base class for all PGFv3 Product Generators to
                 inherit from.

    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Oct 24, 2014    4933    Robert.Blum Initial creation
    Jan 12, 2015    4937    Robert.Blum Refactored to support PGFv3.
    Jan 31, 2015    4937    Robert.Blum General cleanup along with add floodTimeStr
                                        business logic from v2.
'''

import ProductTemplate
from Bridge import Bridge
import math, datetime, copy, json
from TextProductCommon import  TextProductCommon
from com.raytheon.uf.common.time import SimulatedTime
import ProductTextUtil
from ufpy.dataaccess import DataAccessLayer
from shapely.geometry import Polygon 
import logging, UFStatusHandler
from VTECEngineWrapper import VTECEngineWrapper
import HazardConstants
from HazardEvent import HazardEvent
from shapely import geometry
import HazardDataAccess
from KeyInfo import KeyInfo
import os, collections

from abc import *

class ProdSegment(object):
    def __init__(self, segment, vtecRecords):
        # 'segment' is (frozenset([list of ugcs or points]), frozenset([list of eventIDs])        
        self.segment = segment
        self.vtecRecords = vtecRecords
        self.vtecRecords_ms = vtecRecords

    def str(self):
        print '  segment', self.segment 
        print '  vtecRecords: ', self.vtecRecords

class ProductSegmentGroup(object):
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

    def createProductSegment(self, segment, vtecRecords):
        return ProdSegment(segment, vtecRecords)
    def createProductSegmentGroup(self, productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn=None, formatPolygon=None):
        return ProductSegmentGroup(productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn, formatPolygon)

    def __init__(self):
        # This needs to set by each v3 product generator
        # to allow VTECEngineWrapper to init correctly.
        self._productGeneratorName = ''

    def _initialize(self):
        self.bridge = Bridge()
        self._areaDictionary = self.bridge.getAreaDictionary()
        self._cityLocation = self.bridge.getCityLocation()

        self_hazardEventManager = None
        self._tpc = TextProductCommon()
        self._tpc.setUp(self._areaDictionary)
        self._siteInfo = self.bridge.getSiteInfo()

        self.logger = logging.getLogger('Legacy_Base_Generator')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'com.raytheon.uf.common.hazards.productgen', 'Legacy_Base_Generator', level=logging.INFO))
        self.logger.setLevel(logging.INFO)  

        # Default is True -- Products which are not VTEC can override and set to False
        self._vtecProduct = True
        self._vtecEngine = None

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
        actionDict = {}
        ugcDict = {}
        metaDataDict = {}
        for productSegmentGroup in productSegmentGroups:
            for productSegment in productSegmentGroup.productSegments:
                vtecRecords = productSegment.vtecRecords
                actions = []
                for vtecRecord in vtecRecords:
                    action = vtecRecord.get('act')
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

        self._issueTime = int(metaDict.get('currentTime'))
        self._issueTime_secs = self._issueTime / 1000
        self._siteID = metaDict.get('siteID')
        self._tpc.setSiteID(self._siteID)
        self._backupSiteID = metaDict.get('backupSiteID', self._siteID)
        inputFields = metaDict.get('inputFields', {})
        self._overviewHeadline_value = inputFields.get('overviewHeadline', '') 
        self._sessionDict = metaDict.get('sessionDict', {})
        self._testMode = self._sessionDict.get('testMode', False)

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

    def _addDialogInputMapToDict(self, dialogInputMap, productDict):
        '''
        Adds each key from the dialogInputMap to the product dictionary.
        For certain keys it replaces the values which are the metadata
        identifiers with the productStrings from the metaData.

        @param dialogInputMap - dictionary from product staging dialog.
        @param productDict - dictionary which the entries are added to.: 
        '''
        for key in dialogInputMap.keys():
            if key == 'callsToAction_productLevel_' + self._productLabel:
                dictList = self._productLevelMetaData_dict.get(self._productLabel)
                for dict in dictList:
                    if dict.get('fieldName') == key:
                        cta_dict = dict
                        break
                # list of the productStrings to be passed to the formatter
                newCTAs = []
                # Loop over the selected CTAs from the staging dialog
                for value in cta_dict.get('values'):
                    # Compare value with each identifier to find a match
                    for choice in cta_dict.get('choices'):
                        if value == choice.get('identifier'):
                            # Found a match so grab the productString
                            productString = choice.get('productString')
    
                            # Clean up the productString
                            productString = productString.replace('  ', '')
                            productString = productString.replace('\n', ' ')
                            productString = productString.replace('</br>', '\n')
    
                            # Add it to the list to be passed to the formatter
                            newCTAs.append(productString)
                            break
                productDict[key] = newCTAs
            else:
                # Pass the value as is
                productDict[key] = dialogInputMap[key]

    def _preProcessHazardEvents(self, hazardEvents):
        '''
        Can be overridden to preprocess the hazard events
        For example, the Immediate Cause is derived from the Hydrologic Cause for
        an FF.W.NonConvective and needs to be set prior to VTEC processing
        
        @param hazardEvents: hazard events
        '''
        pass

    def _makeProducts_FromHazardEvents(self, hazardEvents, eventSetAttributes):
        # Determine the list of segments given the hazard events 
        segments = self._getSegments(hazardEvents)
        self.logger.info('Product Generator --  Number of segments=' + str(len(segments)))

        # Determine the list of products and associated segments given the segments
        productSegmentGroups = self._groupSegments(segments)

        # Create each product dictionary and add to the list of productDicts
        productDicts = []
        for productSegmentGroup in productSegmentGroups:
            # Update these first so they are correct when we init the product dictionary.
            self._productID = productSegmentGroup.productID
            self._productName = productSegmentGroup.productName
            self._productLabel = productSegmentGroup.productLabel

            # Init the productDict
            productDict = collections.OrderedDict()
            self._initializeProductDict(productDict, eventSetAttributes)
            productDict['productLabel'] = self._productLabel

            # Add productParts to the dictionary
            productParts = productSegmentGroup.productParts
            productDict['productParts'] = productParts
        
            self._createProductLevelProductDictionaryData(productDict)
            # Add dialogInputMap entries to the product dictionary
            self._addDialogInputMapToDict(self._dialogInputMap, productDict)

            # Create the segments and add them to the product dictionary
            self._createAndAddSegmentsToDictionary(productDict, productSegmentGroup)

            # Add any remaining info to the productDict
            self._wrapUpProductDict(productDict)

            # Add the productDict to the list
            productDicts.append(productDict)

        # If issuing, save the VTEC records for legacy products       
        self._saveVTEC(self._generatedHazardEvents) 

        return productDicts, self._generatedHazardEvents


    def _createProductLevelProductDictionaryData(self, productDict):
        pass

    def _createAndAddSegmentsToDictionary(self, productDict, productSegmentGroup):
        segmentDicts = []

        for productSegment in productSegmentGroup.productSegments:

            # Create the segment dicitonary
            segmentDict = collections.OrderedDict()
            self._productSegment = productSegment

            # Setup critical info for the segment
            self._setupSegment()

            # Create and order the sections for the segment:     
            self._productSegment.sections = self._createSections(self._productSegment.vtecRecords_ms, self._productSegment.metaDataList, 
                                                           self._productID, self._issueTime_secs)

            if not self._productSegment.sections:
                self._setProductInformation(self._productSegment.vtecRecords_ms[0], self._productSegment.hazardEvents[0])
             
            # Add the sections to the segment dictionary
            self._addSectionsToSegment(segmentDict)

            # Add CTAs to segment as keyinfo
            if self._productSegment.ctas:
                # If the list of CTAs is a list of empty strings
                # do not make it a KeyInfo....this avoids the megawidget
                # error.
                keyInfo = False
                for cta in self._productSegment.ctas:
                    if cta:
                        keyInfo = True
                        break
                if keyInfo:
                    callsToActionKey = KeyInfo('callsToAction', self._productCategory, self._productID, [], '', True, label='Calls To Action')
                    segmentDict[callsToActionKey] = self._productSegment.ctas
                else:
                    segmentDict['callsToAction'] = self._productSegment.ctas
            else:
                segmentDict['callsToAction'] = self._productSegment.ctas

            # Add any remaining segment level data here
            segmentDict['expireTime'] = self._productSegment.expireTime
            segmentDict['vtecRecords'] = self._productSegment.vtecRecords
            segmentDict['ugcs'] = self._productSegment.ugcs
            segmentDict['timeZones'] = self._productSegment.timeZones

            segmentDicts.append(segmentDict)

        # Add the segmentDicts to the productDict
        productDict['segments'] = segmentDicts
        
    def _addSectionsToSegment(self, segmentDict):
        # If no sections add an empty list and return
        if not self._productSegment.sections:
            segmentDict['sections'] = []
            return

        sectionDicts = []
        for section in self._productSegment.sections:
            sectionVtecRecord, sectionMetaData, sectionHazardEvent = section

            ctas = self.getCTAsPhrase(sectionVtecRecord, sectionHazardEvent, sectionMetaData)
            if ctas:
                self._productSegment.ctas += ctas

            sectionDict = self._prepareSection(sectionHazardEvent, sectionVtecRecord, sectionMetaData)
            sectionDicts.append(sectionDict)

        # Add the list of section dictionaries to the segment dictionary
        segmentDict['sections'] = sectionDicts

    def _createSections(self, vtecRecords, metaDataList, productID, issueTime):
        '''
        Order vtec records and create the sections for the segment

        @param vtecRecords:  vtecRecords for a segment
        @param metaDataList: list of (metaData, hazardEvent) for the segment
        @param productID: product ID e.g. FFA, CWF, etc.
        @param issueTime: in seconds so that it compares to the vtec records
        '''        
        sections = []
        hList = copy.deepcopy(vtecRecords)

        if len(hList):
            if productID in ['CWF', 'NSH', 'OFF', 'GLF']:
                hList.sort(self._tpc.marineSortHazardAlg)
            else:
                hList.sort(self._tpc.regularSortHazardAlg)

        hazardEvents = [hazardEvent for metaData, hazardEvent in metaDataList]

        while len(hList) > 0:
            vtecRecord = hList[0]

            # Can't make a section with vtecRecords with no 'hdln' entry 
            if vtecRecord.get('hdln') == '':
                hList.remove(vtecRecord)
                continue

            # make sure the vtecRecord is still in effect or within EXP criteria
            if (vtecRecord.get('act') != 'EXP' and issueTime >= vtecRecord.get('endTime')) or \
            (vtecRecord.get('act') == 'EXP' and issueTime > 30 * 60 + vtecRecord.get('endTime')):
                hList.remove(vtecRecord)
                continue  # no headline for expired vtecRecords

            # Add to sections
            for metaData, hazardEvent in metaDataList:
                if hazardEvent.getEventID() in vtecRecord.get('eventID'):
                    sections.append((vtecRecord, metaData, hazardEvent))
                    break 

            # always remove the main vtecRecord from the list
            hList.remove(vtecRecord)
        return sections

    def _showProductParts(self):
        # IF True will label the editable pieces in the Product Editor with product parts
        return False

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
               operationalMode=opMode, testHarnessMode=False, vtecProduct=self._vtecProduct, 
               issueFlag=self._issueFlag, productGeneratorName= self._productGeneratorName)
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

    def _initializeProductDict(self, productDict, eventSetAttributes):
        siteID = eventSetAttributes['siteID']
        backupSiteID = eventSetAttributes['backupSiteID']

        productDict['productID'] = self._productID
        productDict['productName'] = self._productName
        productDict['productCategory'] = self._productCategory
        productDict['siteID'] = siteID
        productDict['backupSiteID'] = backupSiteID
        productDict['runMode'] = eventSetAttributes.get('runMode')
        productDict['issueFlag'] = self._issueFlag
        productDict['issueTime'] = self._issueTime
        productDict['purgeHours'] = self._purgeHours


    def _wrapUpProductDict(self, productDict):
        pass

    def _setupSegment(self):
        self._productSegment.metaDataList, self._productSegment.hazardEvents = self.getSegmentMetaData(self._productSegment.segment)
        hazardEvent = self._productSegment.hazardEvents[0]
        if hazardEvent.get('geoType') == 'area':
           self._productSegment.ugcs = list(self._productSegment.segment[0])
        else:
            self._productSegment.ugcs = hazardEvent.get('ugcs', [])
        
        self._productSegment.ugcs.sort()
        self._productSegment.ctas = []
        self._productSegment.pointID = hazardEvent.get('pointID')
        self._productSegment.cityInfo = self.getCityInfo(self._productSegment.ugcs, returnType='list')
        self._productSegment.timeZones = self._tpc.hazardTimeZones(self._productSegment.ugcs)
        self._productSegment.expireTime = self._tpc.getExpireTime(self._issueTime, self._purgeHours, 
                                                                    self._productSegment.vtecRecords_ms)

        # Check for special case where a CAN/EXP is paired with a
        # NEW/EXA/EXB/EXT
        #
        # find any 'CAN' with non-CAN for reasons of text capture
        canVtecRecord = None
        for vtecRecord in self._productSegment.vtecRecords_ms:
            if vtecRecord['act'] in ['CAN', 'EXP', 'UPG']:
                canVtecRecord = vtecRecord
                break  # take the first one
        self._productSegment.canVtecRecord = canVtecRecord

    def _prepareImpactedAreas(self, attributes):
        impactedAreas = []
        ugcs = attributes.get('ugcs') 
        if 'ugcPortions' in attributes:
            portions = attributes.get('ugcPortions') 
        else:
            portions = None
        if 'ugcPartsOfState' in attributes:
            partsOfState = attributes.get('ugcPartsOfState')
        else:
            partsOfState = None
        for ugc in ugcs:
            area = {}
            # query countytable           
            area['ugc'] = ugc
            area['name'] = self._areaDictionary.get(ugc).get('ugcName')
            if portions:
                area['portions'] = portions.get(ugc)
            area['type'] = ugc[2]
            # query state table
            area['state'] = self._areaDictionary.get(ugc).get('fullStateName')
            area['timeZone'] = self._areaDictionary.get(ugc).get('ugcTimeZone')
            if partsOfState:
                area['partsOfState'] = partsOfState.get(ugc)
            impactedAreas.append(area)
        return impactedAreas

    def _prepareImpactedLocations(self, geometry, configurations):
        impactedLocations = {} 
        # TODO Implement the configuration to set different variable names, sources, contraints, etc.
        locationsKey = KeyInfo('cityList', self._productCategory, self._productID,[], '',True,label='Impacted Locations')
        locations = self._retrievePoints(geometry, 'city')
        impactedLocations[locationsKey] = locations
        return impactedLocations

    def _prepareAdditionalInfo(self, attributeValue, event, metaData):
        additionalInfo = []
        citiesListFlag = False
        if len(attributeValue) > 0:
            for identifier in attributeValue:
                if identifier == 'listOfDrainages':
                    # Not sure if this query is correct
                    drainages = self._retrievePoints(event['geometry'], 'basins')
                    paraText = self._tpc.formatDelimitedList(drainages)
                    if len(paraText)==0 :
                        continue
                    productString = self._tpc.getProductStrings(event, metaData, 'additionalInfo', choiceIdentifier=identifier)
                    paraText = productString + paraText + "."
                    additionalInfo.append(paraText)
                elif identifier == 'listOfCities':
                    citiesListFlag = True
                elif identifier == 'floodMoving':
                    productString = self._tpc.getProductStrings(event, metaData, 'additionalInfo', choiceIdentifier=identifier,
                                    formatMethod=self.floodTimeStr, formatHashTags=['additionalInfoFloodMovingTime'])
                    additionalInfo.append(productString)
                else:
                    productString = self._tpc.getProductStrings(event, metaData, 'additionalInfo', choiceIdentifier=identifier)
                    additionalInfo.append(productString)
        return additionalInfo, citiesListFlag

    def floodTimeStr(self, creationTime, hashTag, flood_time_ms):
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

    def _retrievePoints(self, geometryCollection, tablename, constraints=None, sortBy=None):
        req = DataAccessLayer.newDataRequest()
        req.setDatatype('maps')
        req.addIdentifier('table','mapdata.' + tablename)
        req.addIdentifier('geomField','the_geom')
        req.setParameters('name')
        locations = []
        for geom in geometryCollection:
            req.setEnvelope(geom.envelope)
            geometryData = DataAccessLayer.getGeometryData(req)
            for data in geometryData:
                name = data.getLocationName()
                locations.append(name)
        return locations

    def _setProductInformation(self, vtecRecord, hazardEvent):
        if self._issueFlag:
            # Need to make sure we are updating ALL the hazard events for the segment.
            # There could be more than one.
            eventIDs = vtecRecord.get('eventID')
            for eventID in eventIDs:
                if eventID == hazardEvent.getEventID(): 
                    updateEvent = hazardEvent
                else:
                    for updateEvent in self._generatedHazardEvents:
                        if updateEvent.getEventID() == eventID:
                            break                    
                # Update hazardEvent
                expTime = updateEvent.get('expirationTime')
                # Take the earliest expiration time
                if (expTime and expTime > self._productSegment.expireTime) or not expTime:
                    updateEvent.set('expirationTime', self._productSegment.expireTime)
                updateEvent.set('issueTime', self._issueTime)
                updateEvent.addToList('etns', vtecRecord['etn'])
                updateEvent.addToList('vtecCodes', vtecRecord['act'])
                updateEvent.addToList('pils', vtecRecord['pil'])
                try:
                    updateEvent.set('previousForecastCategory', self._maxFcstCategory)
                except:
                    pass

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

    def _cityList(self, segmentDict, event):
        segment = self._productSegment.segment
        ids, eventIDs = segment
        cityList = []
        for city, ugcCity in self._productSegment.cityInfo:
            cityList.append(city)
        segmentDict['cityList'] = cityList

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

    def correctProduct(self, dataList, prevDataList, correctAllSegments):
        millis = SimulatedTime.getSystemTime().getMillis()
        dt = datetime.datetime.fromtimestamp(millis / 1000)
        currentTime = dt.strftime('%d%H%m')
        for i in range(0, len(dataList)):
            data = dataList[i]
            data['startTime'] = currentTime
            segments = data.get('segments')
            for j in range(0, len(segments)):
                segment = segments[j]
                if correctAllSegments:
                    segment = self.correctSegment(segment)
                else:
                    prevData = prevDataList[i]
                    pSegments = prevData.get('segments')
                    pSegment = pSegments[j]
                    if segment != pSegment:
                        segment = self.correctSegment(segment)

            productName = str(data.get('productName'))
            if '...CORRECTED' not in productName:
                if productName.endswith('...TEST'):
                    data['productName'] = productName[:-7] + '...CORRECTED...TEST'
                else:
                    data['productName'] = productName + '...CORRECTED'
        return dataList

    def correctSegment(self, segment):
        if 'vtecRecords' in segment:
            vtecRecords = segment.get('vtecRecords')
            for vtecRecord in vtecRecords:
                vtecRecord['act'] = 'COR'
        return segment

    def getCTAsPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):
        ctas = self._tpc.getProductStrings(hazardEvent, metaData, 'cta')
        return ctas

    def parameterSetupForKeyInfo(self, eventIDs, ugcs):
        '''
        Utility method for preparing the eventIDs and UGCs to be 
        passed into the KeyInfo constructor.
        '''
        tmpEventIDs = []
        for eventID in eventIDs:
            tmpEventIDs.append(int(eventID))

        ugcs.sort()
        ugcList = ''
        for ugc in ugcs:
            if len(ugcList) > 0:
                ugcList += ', '
            ugcList += ugc
        ugcList = str(ugcList)
        return tmpEventIDs, ugcList

    def _floodPointTable(self):

        floodPointDataList = []
        
        hazardEventsList = self._generatedHazardEvents
        if hazardEventsList is not None:
            for hazardEvent in hazardEventsList:
                floodPointDict = { }
                floodPointDict['pointID'] = hazardEvent.get('pointID')
                floodPointDict['streamName'] = hazardEvent.get('streamName')
                floodPointDataList.append(floodPointDict)
                
        return floodPointDataList

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()
        
