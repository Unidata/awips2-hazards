'''
    Description: FFW NonConvective Product Generator
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Oct 24, 2014    4933    Robert.Blum Initial creation
'''

import ProductTemplate
from Bridge import Bridge
import math, datetime, copy, json
from TextProductCommon import  TextProductCommon
import ProductTextUtil
from ufpy.dataaccess import DataAccessLayer
from shapely.geometry import Polygon 
import logging, UFStatusHandler
from VTECEngineWrapper import VTECEngineWrapper
import HazardConstants
from HazardEvent import HazardEvent
from shapely import geometry
import HazardDataAccess
from BasisText import BasisText

from abc import *

class ProdSegment(object):
    def __init__(self, segment, vtecRecords):
        # 'segment' is (frozenset([list of ugcs or points]), frozenset([list of eventIDs])        
        self.segment = segment
        self.vtecRecords = vtecRecords
        self.vtecRecords_ms = vtecRecords

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

    def addProductSegment(self, productSegment):
        self.productSegments.append(productSegment)

    def setProductParts(self, productParts):
        self.productParts = productParts

class Product(ProductTemplate.Product):

    def createProductSegment(self, segment, vtecRecords):
        return ProdSegment(segment, vtecRecords)
    def createProductSegmentGroup(self, productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn=None, formatPolygon=None):
        return ProductSegmentGroup(productID, productName, geoType, vtecEngine, mapType, segmented, productSegments, etn, formatPolygon)

    def __init__(self):
        # This needs to set by each v3 product generator
        # to allow VTECEngineWrapper to init correctly.
        self._productGeneratorName = ''
        self.basisText = BasisText()

    def _initialize(self):
        self.bridge = Bridge()
        self._areaDictionary = self.bridge.getAreaDictionary()
        self_hazardEventManager = None
        self._tpc = TextProductCommon()
        self._tpc.setUp(self._areaDictionary)
        self._cityLocation = self.bridge.getCityLocation()
        self._siteInfo = self.bridge.getSiteInfo()

        self.logger = logging.getLogger('BaseGenerator')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'com.raytheon.uf.common.hazards.productgen', 'BaseGenerator', level=logging.INFO))
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
            self._dialogInputMap = dialogInputMap
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

    def _preProcessHazardEvents(self, hazardEvents):
        '''
        Can be overridden to preprocess the hazard events
        For example, the Immediate Cause is derived from the Hydrologic Cause for
        an FF.W.NonConvective and needs to be set prior to VTEC processing
        
        @param hazardEvents: hazard events
        '''
        pass

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
        if self._issueFlag:
            for wrapper in self._wrappers:
                wrapper.mergeResults() 
#             self.logger.info(self._productCategory + ' Saving VTEC')
            # Handle Ended eventIDs 
            # Set the state to 'ended' for events that are completely canceled or expired.
            # Note that for some long-fused hazards e.g. FA.A, one eventID could be
            # associated with both a CAN and a NEW and we do not want to change the 
            # state to 'ended'.
            for hazardEvent in hazardEvents:
                vtecCodes = hazardEvent.get('vtecCodes', [])
                if ('CAN' in vtecCodes or 'EXP' in vtecCodes) and not ['NEW', 'CON', 'EXA', 'EXT', 'EXB', 'UPG', 'ROU'] in vtecCodes:
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

    def _setupSegment(self, event):
        self._productSegment.metaDataList, self._productSegment.hazardEvents = self.getSegmentMetaData(self._productSegment.segment)
        hazardEvent = self._productSegment.hazardEvents[0]
        if hazardEvent.get('geoType') == 'area':
           self._productSegment.ugcs = list(self._productSegment.segment[0])
        else:
            self._productSegment.ugcs = event.get('ugcs', [])
        self._productSegment.cityInfo = self.getCityInfo(self._productSegment.ugcs, returnType='list')
        self._productSegment.timeZones = self._tpc.hazardTimeZones(self._productSegment.ugcs)
        self._productSegment.expireTime = self._tpc.getExpireTime(self._issueTime, self._purgeHours, 
                                                                    self._productSegment.vtecRecords_ms)

    def _setProductInformation(self, vtecRecord, hazardEvent):
        if self._issueFlag:
            # Update hazardEvent
            expTime = hazardEvent.get('expirationTime')
            # Take the earliest expiration time
            if (expTime and expTime > self._productSegment.expireTime) or not expTime:
                hazardEvent.set('expirationTime', self._productSegment.expireTime)
            hazardEvent.set('issueTime', self._issueTime)
            hazardEvent.addToList('etns', vtecRecord['etn'])
            hazardEvent.addToList('vtecCodes', vtecRecord['act'])
            hazardEvent.addToList('pils', vtecRecord['pil'])
            try:
                hazardEvent.set('previousForcastCategory', self._maxForecastCategory)
            except:
                pass

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
        
    def basisFromHazardEvent(self, hazardEvent):
        hazardType = hazardEvent.getHazardType()
        hazardEventAttributes = hazardEvent.getHazardAttributes()
        result = self.basisText.getBulletText(hazardType, hazardEventAttributes)
        result = self._tpc.substituteParameters(hazardEvent, result)
        return result