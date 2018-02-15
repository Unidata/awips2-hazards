'''
Flash flood recommender.

Produces FF.W (Flash Flood Warning) hazard recommendations
for small basins or aggregates of small basins using
FFMP preprocessed data sources(QPE, QPF and Guidance).
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler
import HazardDataAccess
import AdvancedGeometry
from VisualFeatures import VisualFeatures
from EventSet import EventSet
from HazardConstants import SESSION_OBJECT_KEY, DELETE_EVENT_IDENTIFIERS_KEY, RESULTS_MESSAGE_KEY
from HazardEventLockUtils import HazardEventLockUtils

from ufpy.dataaccess import DataAccessLayer
from ufpy.dataaccess.PyGeometryData import PyGeometryData
from com.raytheon.uf.common.monitor.config import FFMPSourceConfigurationManager
from com.raytheon.uf.common.monitor.config import FFMPRunConfigurationManager
from com.raytheon.uf.common.dataplugin.ffmp import FFMPBasinData, FFMPBasin, FFMPTemplates, FFMPTemplates
from gov.noaa.gsd.viz.hazards.spatialdisplay import HazardEventGeometryAggregator

from java.util import Date
from java.lang import Long, Float

import JUtil
from shapely import prepared
#
# Keys to values in the attributes dictionary produced
# by the flash flood recommender.
ALL_HUC = 'ALL'
QPE_SOURCE = 'qpeSource'
GUID_SOURCE = 'guidSource'
TYPE_SOURCE = 'typeSource'
RADAR = 'radar'
ACCUMULATION_INTERVAL = 'accumulationInterval'
COMBINE_HAZARD_DISTANCE = 'combineHazardDistance'
RECOMMENDER_AREA = 'recommenderArea'

#  Time and key constants
FLASH_FLOOD_PHENOMENON = 'FF'
FLASH_FLOOD_SIGNIFICANCE = 'W'
FLASH_FLOOD_SUBTYPE = 'Convective'
POTENTIAL_TYPE = 'POTENTIAL'
CURRENT_TIME = 'currentTime'
CURRENT_SITE = 'siteID'
CWA_GEOMETRY = 'cwaGeometry'
INCLUDE_CWA_GEOMETRY = "includeCwaGeometry"
ENTIRE_CWA =  'entire CWA'
USER_DEFINED_AREA = 'user defined area'

DEFAULT_FFW_DURATION_IN_SECONDS = 6*60*60  # 6 hours.

#
# Keys for requests to data access layer
FFMP_KEY = 'ffmp'
WFO_KEY = 'wfo'
SITE_KEY = 'siteKey'
DATA_KEY = 'dataKey'
HUC_KEY = 'huc'
ACCUM_HRS = 'accumHrs'

#
# Keys for accessing small basin map.
PRECIP_VALUE = 'precipValue'
GUIDANCE_VALUE = 'guidanceValue'
COMPARE_VALUE = 'compareValue'
GEOMETRY_KEY = 'geometry'

TYPE_DIFF = 'diff'
TYPE_RATIO = 'ratio'
TYPE_QPE = 'qpe'
VALUE_TYPES = [TYPE_DIFF, TYPE_RATIO, TYPE_QPE]
#
# Supported FFMP QPE, Guidance and Forecast data sources.
# FFMP configuration may be read for these sources for
# information such as duration and expiration time. This
# allows new sources to be incrementally implemented.
# It also allows only supported sources to be
# shown to the forecaster in the recommender dialog.
# See ffmp/FFMPSourceConfig.xml for available FFMP
# data sources.

ffgGuidances = ['FFG0124hr', 'FFG0324hr', 'FFG0624hr']
    
class Recommender(RecommenderTemplate.Recommender):
   
    def __init__(self):
        self.smallBasinMap = {}
        self.logger = logging.getLogger('FlashFloodRecommender')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'FlashFloodRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        self.hazardEventLockUtils = None

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Flash Flood Recommender'
        metadata['author'] = 'GSD'
        metadata['description'] = '''
        Runs against choice of qpe, ratio, or diff and produces hazards based on FFMP data.
        '''
        metadata['eventState'] = 'Potential'
        metadata[INCLUDE_CWA_GEOMETRY] = True
        
        metadata["getSpatialInfoNeeded"] = False
        metadata["handleDialogParameterChangeNeeded"] = True
       
        return metadata

    def defineDialog(self, eventSet):
        '''
        Reads the supported QPE, QPF and FFG sources to build the QPE source,
        QPF source, and Guidance source options on the tool dialog.
       
        @return: A dialog definition to solicit user input before running tool
        '''  
        dialogDict = {'title': 'Flash Flood Recommender'}
       
        qpfChoices = []
        guidChoices = []
       
        qpeDisplayNames = []
        qpfDisplayNames = []
        guidDisplayNames = []
       
        srcConfigMan = FFMPSourceConfigurationManager.getInstance()
        supportedQPESourceList = srcConfigMan.getQPESources()
        
        # Process supported FFMP QPE sources
        for choice in supportedQPESourceList:
            source = srcConfigMan.getSource(choice)
            qpeDisplayNames.append(source.getDisplayName())
        
        supportedGuidanceSourceList = srcConfigMan.getGuidanceDisplayNames()
       
        # Process supported FFMP Guidance sources      
        for choice in supportedGuidanceSourceList:
            guidChoices.append(choice)
            guidDisplayNames.append(choice)
                   
        runCfgMan = FFMPRunConfigurationManager.getInstance()
        products = runCfgMan.getProducts()
       
        radarChoices = []
        for product in products:
            if (product.getProductName() == 'DHR' or product.getProductName() == 'DPR'):
                radarChoices.append(product.getProductKey())
                
        radarChoices = sorted(set(radarChoices))
        radarChoices.insert(0, "---")
       
        sessionObjectManager = eventSet.getAttribute(SESSION_OBJECT_KEY)
        
        valueDict = {}           

        fieldDictList = []

        qpeDictLabel = {}
        qpeDictLabel['fieldType'] = 'Label'
        qpeDictLabel['fieldName'] = 'qpeDictLabel'
        qpeDictLabel['label'] = 'QPE Source:'
        fieldDictList.append(qpeDictLabel)

        qpeDict = {}
        qpeDict['fieldType'] = 'ComboBox'
        qpeDict['fieldName'] = QPE_SOURCE
        qpeDict['choices'] = qpeDisplayNames
        cachedQpeDisplayName = sessionObjectManager.get(QPE_SOURCE)
        if cachedQpeDisplayName and cachedQpeDisplayName in qpeDisplayNames:
            qpeDict['defaultValues'] = cachedQpeDisplayName
        else: 
            qpeDict['defaultValues'] = qpeDisplayNames[0]
        valueDict[QPE_SOURCE] = qpeDict['defaultValues']
        fieldDictList.append(qpeDict)

        radarDictLabel = {}
        radarDictLabel['fieldType'] = 'Label'
        radarDictLabel['fieldName'] = 'radarDictLabel'
        radarDictLabel['label'] = 'Radar:'
        fieldDictList.append(radarDictLabel)

        radarDict = {}
        radarDict['fieldType'] = 'ComboBox'
        radarDict['fieldName'] = RADAR
        radarDict['choices'] = radarChoices
        cachedRadarChoice = sessionObjectManager.get(RADAR)
        if cachedRadarChoice and cachedRadarChoice in radarChoices:
            radarDict['defaultValues'] = cachedRadarChoice
        else:
            radarDict['defaultValues'] = radarChoices[1]
        radarDict['extraData'] = { "choices": radarChoices }
        valueDict[RADAR] = radarDict['defaultValues']
        fieldDictList.append(radarDict)

        guidDictLabel = {}
        guidDictLabel['fieldType'] = 'Label'
        guidDictLabel['fieldName'] = 'guidDictLabel'
        guidDictLabel['label'] = 'Guidance Source:'
        fieldDictList.append(guidDictLabel)
       
        guidDict = {}
        guidDict['fieldType'] = 'ComboBox'
        guidDict['fieldName'] = GUID_SOURCE
        guidDict['choices'] = guidChoices
        cachedGuidChoice = sessionObjectManager.get(GUID_SOURCE)
        if cachedGuidChoice and cachedGuidChoice in guidChoices:
            guidDict['defaultValues'] = cachedGuidChoice
        else:
            guidDict['defaultValues'] = guidChoices[0]
        valueDict[GUID_SOURCE] = guidDict['defaultValues']
        fieldDictList.append(guidDict)

        accumulationIntervalDictLabel = {}
        accumulationIntervalDictLabel['fieldType'] = 'Label'
        accumulationIntervalDictLabel['fieldName'] = 'accumulationIntervalDictLabel'
        accumulationIntervalDictLabel['label'] = 'Time Duration (hrs):'
        fieldDictList.append(accumulationIntervalDictLabel)
       
        accumulationIntervalDict = {}
        accumulationIntervalDict['fieldType'] = 'FractionSpinner'
        accumulationIntervalDict['showScale'] = 0
        accumulationIntervalDict['fieldName'] = ACCUMULATION_INTERVAL
        accumulationIntervalDict['minValue'] = 1
        accumulationIntervalDict['maxValue'] = 24
        accumulationIntervalDict['incrementDelta'] = .25
        accumulationIntervalDict['precision'] = 2
        cachedAccumulationInterval = sessionObjectManager.get(ACCUMULATION_INTERVAL)
        if cachedAccumulationInterval and cachedAccumulationInterval >= accumulationIntervalDict['minValue'] and cachedAccumulationInterval <= accumulationIntervalDict['maxValue']:
            valueDict[ACCUMULATION_INTERVAL] = cachedAccumulationInterval
        else:
            valueDict[ACCUMULATION_INTERVAL] = 1
        fieldDictList.append(accumulationIntervalDict)

        typeDictLabel = {}
        typeDictLabel['fieldType'] = 'Label'
        typeDictLabel['fieldName'] = 'typeDictLabel'
        typeDictLabel['label'] = 'FFMP Fields:'
        fieldDictList.append(typeDictLabel)

        typeDict = {}
        typeDict['fieldType'] = 'ComboBox'
        typeDict['fieldName'] = TYPE_SOURCE
        typeDict['choices'] = VALUE_TYPES
        typeDict['defaultValues'] = VALUE_TYPES[0]
        cachedValueType = sessionObjectManager.get(TYPE_SOURCE)
        if cachedValueType and cachedValueType in VALUE_TYPES:
            valueDict[TYPE_SOURCE] = cachedValueType
        else:
            valueDict[TYPE_SOURCE] = typeDict['defaultValues']
        fieldDictList.append(typeDict)

        compareValueDictLabel = {}
        compareValueDictLabel['fieldType'] = 'Label'
        compareValueDictLabel['fieldName'] = 'compareValueDictLabel'
        compareValueDictLabel['label'] = 'Value (inches/%):'
        fieldDictList.append(compareValueDictLabel)
       
        compareValueDict = {}
        compareValueDict['fieldType'] = 'FractionSpinner'
        compareValueDict['showScale'] = 0
        compareValueDict['fieldName'] = COMPARE_VALUE
        compareValueDict['minValue'] = -6
        compareValueDict['maxValue'] = 6
        compareValueDict['incrementDelta'] = 1.0
        compareValueDict['precision'] = 2
        cachedCompareValue = sessionObjectManager.get(COMPARE_VALUE)
        if cachedCompareValue and cachedCompareValue >= compareValueDict['minValue'] and cachedCompareValue <= compareValueDict['maxValue']:
            valueDict['compareValue'] = cachedCompareValue
        else:
            valueDict['compareValue'] = 1
        fieldDictList.append(compareValueDict)

        recommenderAreaLabel = {}
        recommenderAreaLabel['fieldType'] = 'Label'
        recommenderAreaLabel['fieldName'] = 'recommenderAreaLabel'
        recommenderAreaLabel['label'] = "Recommender Area:"
        fieldDictList.append(recommenderAreaLabel)

        recommenderAreaDict = {
            "fieldType" : "ComboBox",
            "fieldName" : RECOMMENDER_AREA,
            "choices" : [ENTIRE_CWA, USER_DEFINED_AREA]
            }
        valueDict[RECOMMENDER_AREA] = ENTIRE_CWA
        fieldDictList.append(recommenderAreaDict)

        combineHazardDictLabel = {}
        combineHazardDictLabel['fieldType'] = 'Label'
        combineHazardDictLabel['fieldName'] = 'combineHazardDictLabel'
        combineHazardDictLabel['label'] = 'Combine Hazards Within (miles):'
        fieldDictList.append(combineHazardDictLabel)

        combineHazardDict = {}
        combineHazardDict['fieldType'] = 'FractionSpinner'
        combineHazardDict['showScale'] = 0
        combineHazardDict['fieldName'] = COMBINE_HAZARD_DISTANCE
        combineHazardDict['minValue'] = 0
        combineHazardDict['maxValue'] = 1000
        combineHazardDict['incrementDelta'] = .25
        combineHazardDict['precision'] = 2
        cachedCombineHazardsDistance = sessionObjectManager.get(COMBINE_HAZARD_DISTANCE)
        if cachedCombineHazardsDistance and cachedCombineHazardsDistance >= combineHazardDict['minValue'] and cachedCombineHazardsDistance <= combineHazardDict['maxValue']:
            valueDict[COMBINE_HAZARD_DISTANCE] = cachedCombineHazardsDistance
        else:
            valueDict[COMBINE_HAZARD_DISTANCE] = 10.0
        fieldDictList.append(combineHazardDict)

        # A composite with multiple columns is used to better align the widgets
        # in the dialog.
        fieldDictComposite = {}
        fieldDictComposite['fieldType'] = 'Composite'
        fieldDictComposite['fieldName'] = 'combineComposite'
        fieldDictComposite['numColumns'] = 2
        fieldDictComposite['topMargin'] = 5
        fieldDictComposite['bottomMargin'] = 5
        fieldDictComposite['leftMargin'] = 5
        fieldDictComposite['rightMargin'] = 5
        fieldDictComposite['fields'] = fieldDictList

        dialogDict['fields'] = [fieldDictComposite]
        dialogDict['valueDict'] = valueDict

        dialogDict['visualFeatures'] = VisualFeatures([
                                                       self.getUserDefinedBoundingGeometry(False, True, None)
                                                       ])
       
        return dialogDict

    def handleDialogParameterChange(self, eventSet, triggeringDialogIdentifiers, mutableDialogProperties, triggeringVisualFeatureIdentifiers, visualFeatures, collecting):
        '''
        Respond to the dialog state changing in some way (but not the
        dialog being closed), or visual features changing in some way,
        or a request for read-only visual features prior to execution.
        '''
        changedMutableProperties = {}
        newVisualFeatures = None

        # If this invocation is to generate read-only visual features to
        # be displayed while the recommender runs, provide a read-only
        # version of the existing feature, or provide nothing if the
        # "user defined area" option has not been selected.
        if not collecting:
            if mutableDialogProperties[RECOMMENDER_AREA]["values"] == USER_DEFINED_AREA:
                newVisualFeatures = VisualFeatures([ self.getUserDefinedBoundingGeometry(True, False, visualFeatures[0].get("geometry")) ])
            else:
                newVisualFeatures = VisualFeatures([])
            return (changedMutableProperties, newVisualFeatures)

        # Determine whether or not the dialog is being initialized.
        initializing = (not triggeringDialogIdentifiers and not triggeringVisualFeatureIdentifiers)
        
        # Ensure the compare-value megawidget has the right min, max, etc.
        # with regard to the type source.
        if initializing or TYPE_SOURCE in triggeringDialogIdentifiers:
            if mutableDialogProperties[TYPE_SOURCE]["values"] == TYPE_DIFF:
                changedMutableProperties[COMPARE_VALUE] = {
                                                            "maxValue": 6,
                                                            "minValue": -6,
                                                            "incrementDelta": 1.0
                                                            }
            elif mutableDialogProperties[TYPE_SOURCE]["values"] == TYPE_RATIO:
                changedMutableProperties[COMPARE_VALUE] = {
                                                            "maxValue": 200,
                                                            "minValue": 0,
                                                            "incrementDelta": 1.0,
                                                            "values": 100
                                                            }
            elif mutableDialogProperties[TYPE_SOURCE]["values"] == TYPE_QPE:
                changedMutableProperties[COMPARE_VALUE] = {
                                                            "maxValue": 24,
                                                            "minValue": 0,
                                                            "incrementDelta": 1.0,
                                                            "values": 1
                                                            }
    
        # Ensure the radar megawidget has the value based upon the
        # QPE source.
        if initializing or QPE_SOURCE in triggeringDialogIdentifiers:
            if mutableDialogProperties[QPE_SOURCE]["values"] == "DHR":
                changedMutableProperties[RADAR] = {
                                                   "values": mutableDialogProperties[RADAR]["extraData"]["choices"][1]
                                                   }
            elif mutableDialogProperties[QPE_SOURCE]["values"] == "DPR":
                changedMutableProperties[RADAR] = {
                                                   "values": mutableDialogProperties[RADAR]["extraData"]["choices"][1]
                                                   }
            else:
                changedMutableProperties[RADAR] = {
                                                   "values": mutableDialogProperties[RADAR]["extraData"]["choices"][0]
                                                   }

        # If the visual feature has changed, alter the area selection
        # to match. If the visual feature now has an empty geometry,
        # set the geometry to None.
        changedToUserDefinedArea = False
        if triggeringVisualFeatureIdentifiers:
            geometry = visualFeatures[0].get("geometry")
            if geometry:
                if geometry.asShapely().is_empty:
                    visible = (mutableDialogProperties[RECOMMENDER_AREA]["values"] == USER_DEFINED_AREA)
                    newVisualFeatures = VisualFeatures([ self.getUserDefinedBoundingGeometry(visible, True, None) ])
                else:
                    changedMutableProperties[RECOMMENDER_AREA] = {
                                                                  "values": USER_DEFINED_AREA
                                                                  }
                    changedToUserDefinedArea = True

        # Ensure that the visual feature for the user-defined bounding
        # area is shown or hidden as appropriate.
        if initializing or changedToUserDefinedArea or RECOMMENDER_AREA in triggeringDialogIdentifiers:
            visible = (changedToUserDefinedArea or (mutableDialogProperties[RECOMMENDER_AREA]["values"] == USER_DEFINED_AREA))
            newVisualFeatures = VisualFeatures([ self.getUserDefinedBoundingGeometry(visible, True, visualFeatures[0].get("geometry")) ])
        elif not newVisualFeatures:
            newVisualFeatures = visualFeatures
            
        return (changedMutableProperties, newVisualFeatures)

    def getUserDefinedBoundingGeometry(self, visible, editable, geometry):
        '''
        Get the visual feature used to show the user-defined bounding area, visible
        or invisible as specified, editable or not as specified, with the specified
        geometry (which may be None).
        '''
        return {
                "identifier": "userDefinedBoundingArea",
                "visibilityConstraints": "always" if visible else "never",
                "geometry": geometry,
                "fillColor": { "red": 1.0, "green": 1.0, "blue": 1.0, "alpha": 0.25 },
                "bufferColor": { "red": 0.0, "green": 0.0, "blue": 0.0, "alpha": 0.7 },
                "borderThickness": 2,
                "bufferThickness": 1,
                "dragCapability": "all" if editable else "none",
                "editableUsingGeometryOps": editable,
                "rotatable": editable,
                "scaleable": editable
                }
        
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        Runs the Flash Flood Recommender tool
       
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param visualFeatures: Visual features as defined by the
                               defineSpatialInput() method and
                               modified by the user to provide
                               spatial input; ignored.
        
        @return: A list of potential Flash Flood Hazard events. 
        '''
        sessionAttributes = eventSet.getAttributes()

        caveMode = sessionAttributes.get('runMode','PRACTICE').upper()
        self.practice = True
        if caveMode == 'OPERATIONAL':
            self.practice = False

        self.smallBasinMap = {}
        self.currentTime = eventSet.getAttribute(CURRENT_TIME)
        self.currentSite = eventSet.getAttribute(CURRENT_SITE)
        self.cwaGeom = eventSet.getAttribute(CWA_GEOMETRY)

        # Pull these out of the dialog and cache them.
        sessionObjectManager = eventSet.getAttribute(SESSION_OBJECT_KEY)
        self._sourceName = dialogInputMap.get(QPE_SOURCE)  
        sessionObjectManager.set(QPE_SOURCE, self._sourceName)
        self._guidanceName = dialogInputMap.get(GUID_SOURCE)
        sessionObjectManager.set(GUID_SOURCE, self._guidanceName)
        self._radar = dialogInputMap.get(RADAR)
        sessionObjectManager.set(RADAR, self._radar)
        self.type = dialogInputMap.get(TYPE_SOURCE)
        sessionObjectManager.set(TYPE_SOURCE, self.type)  
        self.compareValue = dialogInputMap.get(COMPARE_VALUE)       
        sessionObjectManager.set(COMPARE_VALUE, self.compareValue)     
        self.accumulationHours = float(dialogInputMap.get(ACCUMULATION_INTERVAL))
        sessionObjectManager.set(ACCUMULATION_INTERVAL, self.accumulationHours)
        self.combineHazardDistance = float(dialogInputMap.get(COMBINE_HAZARD_DISTANCE))
        sessionObjectManager.set(COMBINE_HAZARD_DISTANCE, self.combineHazardDistance) 
        
        # Get the area in which to execute. If the user has selected
        # "user defined area", there must be a polygon within the
        # visual feature that the user drew, and the intersection of
        # that polygon and the CWA must be non-empty, otherwise there
        # is nothing for the recommender to do. If the user selected
        # "CWA" as the execution area, use the passed-in CWA.
        if dialogInputMap.get(RECOMMENDER_AREA) == USER_DEFINED_AREA:
            userDefinedArea = visualFeatures[0].get("geometry", None)
            if userDefinedArea:
                userDefinedArea = userDefinedArea.asShapely()
            errorMessagePrefix = None
            if userDefinedArea and not userDefinedArea.is_empty:
                
                # Find the intersection of the user-defined area and the CWA.
                # If the CWA is poorly formed, this could cause problems,
                # so catch any exceptions.
                try:
                    userDefinedArea = userDefinedArea.intersection(self.cwaGeom)
                except:
                    errorMessagePrefix = "Invalid CWA"
                    self.logger.warn("Attempt to intersect with CWA unsuccessful, CWA must be invalid in some way")
            else:
                errorMessagePrefix = "User-defined execution area not supplied"
            if not errorMessagePrefix:
                if userDefinedArea and not userDefinedArea.is_empty:
                    self.executionArea = userDefinedArea
                else:
                    errorMessagePrefix = "User-defined execution area lies outside the CWA"
            if errorMessagePrefix: 
                mergedEvents = EventSet(None)
                mergedEvents.addAttribute(RESULTS_MESSAGE_KEY, errorMessagePrefix + "; no recommendations can be made.")
                return mergedEvents
        else:
            self.executionArea = self.cwaGeom
        self.executionArea = prepared.prep(self.executionArea)
        
        # delete potential hazards
        deleteEventIdentifiers = self.getIdentifiersOfCurrentEventsToBeDeleted(eventSet, sessionAttributes)
        
        self._localize()
        haveGuidance = False
        cont = self.getQPEValues()
        if cont :
            haveGuidance = self.getGuidanceValues()
        else:
            self.logger.warn("No QPE data available")

        mergedEvents = EventSet(None)
        if haveGuidance:
            # Add recommended events if there is guidance.
            recommendedEvents = self.buildEvents(cont);
            mergedEvents.addAll(recommendedEvents)
        elif cont:
            self.logger.warn("No Flash Flood Guidance data available")

        mergedEvents.addAttribute(DELETE_EVENT_IDENTIFIERS_KEY, deleteEventIdentifiers)

        # Notify the user if no events were generated by the recommender.        
        if len(recommendedEvents) == 0:
            mergedEvents.addAttribute(RESULTS_MESSAGE_KEY, "Recommender completed. No recommended hazards.")
        
        return mergedEvents
   
    def getQPEValues(self):
        '''
        Strategy method for reading and accumulating data
        from preprocessed FFMP QPE datasets.
        '''
        request = DataAccessLayer.newDataRequest()
        request.setDatatype(FFMP_KEY)
        request.setParameters(self._sourceName)
        request.addIdentifier(WFO_KEY, self.currentSite)
        request.addIdentifier(SITE_KEY, self._siteKey)
        request.addIdentifier(DATA_KEY, self._dataKey)
        request.addIdentifier(HUC_KEY, ALL_HUC)
        availableTimes = DataAccessLayer.getAvailableTimes(request)
        # determine correct times
        latestTime = 0
        for time in availableTimes :
            tm = time.getRefTime().getTime()
            if tm > latestTime :
                latestTime = tm
       
        timedelta = latestTime - self.accumulationHours * 60 * 60 * 1000
        usedTimes = []
        
        for time in availableTimes :
            if time.getRefTime().getTime() >= timedelta :
                usedTimes.append(time)
       
        basins = []
        if usedTimes:
            geometries = DataAccessLayer.getGeometryData(request, usedTimes)
            for geometry in geometries:
                self.__storeQpe(geometry.getLocationName(), geometry.getNumber(self._sourceName), geometry)
            return True
        return False
               
    def getGuidanceValues(self):
        '''
        Retrieves the guidance values from the correct FFMP source
        '''
        ffmpSourceConfigManager = FFMPSourceConfigurationManager.getInstance()
       
        if self._guidanceName == 'RFCFFG':
            if self.accumulationHours <= 1 :
                guidanceSource = ffgGuidances[0]
            elif self.accumulationHours <= 3 :
                guidanceSource = ffgGuidances[1]
            else :
                guidanceSource = ffgGuidances[2]
        else :
            guidanceSource = self._guidanceName      
        request = DataAccessLayer.newDataRequest()
        request.setDatatype(FFMP_KEY)
        request.setParameters(guidanceSource)
        request.addIdentifier(WFO_KEY, self.currentSite)
        request.addIdentifier(SITE_KEY, self._siteKey)
        request.addIdentifier(HUC_KEY, ALL_HUC)
        request.addIdentifier(ACCUM_HRS, self.accumulationHours)
       
        availableTimes = DataAccessLayer.getAvailableTimes(request)
       
        if availableTimes :
            time = availableTimes[-1]
            geometries = DataAccessLayer.getGeometryData(request, [time])
           
            for geometry in geometries :
                if isinstance(guidanceSource, list):
                    geometry = self._interpolateGuidance(geometry)
                self.__storeGuidance( geometry.getLocationName(), geometry.getNumber(guidanceSource), geometry.getGeometry())
            return True
        return False
       
    def __storeGuidance(self, basinName, value, geom):
        if basinName in self.smallBasinMap:
            basin = self.smallBasinMap[basinName]
            basin[GUIDANCE_VALUE] = value
        else :
            basin = {}
            basin[GUIDANCE_VALUE] = value
            basin[GEOMETRY_KEY] = geom
            self.smallBasinMap[basinName] = basin
       
    def __storeQpe(self, basinName, value, geom):
        if basinName in self.smallBasinMap:
            basin = self.smallBasinMap[basinName]
            if basin.has_key(PRECIP_VALUE) is False :
                basin[PRECIP_VALUE] = value
            else:
                basin[PRECIP_VALUE] += value
        else :
            basin = {}
            basin[PRECIP_VALUE] = value
            basin[GEOMETRY_KEY] = geom
            self.smallBasinMap[basinName] = basin
   
    def _interpolateGuidance(self, geometry):
        return geometry
   
    def buildEvents(self, haveBasins):
        '''
        Builds a list of FF.W hazard events. For each small basin
        with a non-zero estimated precipitation amount, this value
        is compared with the interpolated FFG for that basin. If the
        ratio of precip to FFG is 1 or more, this small basin is
        flagged as a potential location for flash flooding.
       
        @return: An EventSet of potential flash flood (FF.W) hazard events
        '''
        pythonEventSet = EventSetFactory.createEventSet()
        if haveBasins :
            basins = []
            for basinName in self.smallBasinMap:
                basin = self.smallBasinMap[basinName]
                # need better logic here, based on type, don't need guidance
                # need to fix storing of values...
                if basin.has_key(PRECIP_VALUE):
                    precipValue = basin[PRECIP_VALUE]
                    guidanceValue = basin[GUIDANCE_VALUE]
                   
                    if self.type == TYPE_DIFF :
                        value = self._calcDiff(precipValue, guidanceValue)
                    elif self.type == TYPE_RATIO :
                        value = self._calcRatio(precipValue, guidanceValue)
                    elif self.type == TYPE_QPE :
                        value = self._calcQPE(precipValue)
                       
                    # if QPE or ratio and compare value is 0, then the comparison should be >, otherwise >=
                    # Round to the 2 decimal display value otherwise 0 qpe/ratio basing may become part of the hazard.
                    if (round(self.compareValue,2) == 0.0 and (self.type == TYPE_RATIO or self.type == TYPE_QPE)):
                        if round(value, 2) > 0.0:
                            geom = basin[GEOMETRY_KEY]
                            g = geom.getGeometry()
                            basins.append(g)
                    elif value >= self.compareValue:
                        geom = basin[GEOMETRY_KEY]
                        g = geom.getGeometry()
                        basins.append(g)
                       
            if basins:
                
                # Remove any basins that are completely outside the execution area.
                for basin in list(basins):
                    if self.executionArea.intersects(basin) == False:
                        basins.remove(basin)
                hazardEvent = EventFactory.createEvent(self.practice)
                hazardEvent.setSiteID(self.currentSite)
                hazardEvent.setHazardStatus(POTENTIAL_TYPE)
                hazardEvent.setPhenomenon(FLASH_FLOOD_PHENOMENON)
                hazardEvent.setSignificance(FLASH_FLOOD_SIGNIFICANCE)
                hazardEvent.setSubType(FLASH_FLOOD_SUBTYPE)

                geometry = GeometryFactory.createCollection(basins)
                hazardEvent.setGeometry(geometry)
               
                # Convert currentTime from milliseconds to seconds.
                currentTime = long(self.currentTime) / 1000
                creationDateTime = datetime.datetime.utcfromtimestamp(currentTime)
                startDateTime = creationDateTime
                endDateTime = datetime.datetime.utcfromtimestamp((currentTime + DEFAULT_FFW_DURATION_IN_SECONDS))
       
                hazardEvent.setCreationTime(creationDateTime)
                hazardEvent.setStartTime(startDateTime)
                hazardEvent.setEndTime(endDateTime)
                pythonEventSet.add(hazardEvent)
            else :
                self.logger.info("No events returned for Flash Flood Recommender.")
        else :
            self.logger.info("No basins available for Flash Flood Recommender.")
           
        aggregator = HazardEventGeometryAggregator()
        aggregatedEventSet = aggregator.aggregateEvents(JUtil.pyValToJavaObj(pythonEventSet), self.combineHazardDistance)
        
        return aggregatedEventSet;
   
    def _calcDiff(self, qpe, guid):
        '''
        Calculates the diff based on the qpe and guidance
        '''
        if math.isnan(qpe) :
            qpe = 0
        if math.isnan(guid) :
            guid = 0
           
        return qpe - guid
       
    def _calcQPE(self, qpe):
        '''
        Calculates the qpe by just returning the value sent in
        '''
        if math.isnan(qpe) :
            qpe = 0
           
        return qpe
   
    def _calcRatio(self, qpe, guid):
        '''
        Calculates the ratio based on the qpe and guidance
        '''
        if math.isnan(qpe) :
            qpe = 0
        if math.isnan(guid) :
            guid = 1
        if qpe >= 0 and guid > 0 :
            return (qpe / guid) * 100
        return 0  
   
    # TODO might be worth re-looking into this.
    def _localize(self):
        runConfigMgr = FFMPRunConfigurationManager.getInstance()
        domains = runConfigMgr.getDomains()
       
        self._wfo = None
       
        for i in range(domains.size()) :
            domain = domains.get(i)
            if domain.isPrimary():
                self._domain = domain
                self._wfo = domain.getCwa()
                break
       
        if not self._wfo :
            self.logger.info("FFMP Run Configuration Manager unable to get WFO from available Domains..")
            raise LookupError('Unable to determine the primary FFMP CWA.')
       
        products = runConfigMgr.getProducts()
        self._siteKey = None
       
        for i in range(products.size()) :
            product = products.get(i)
            if product.getProductName() == self._sourceName :
                self._siteKey = product.getProductKey()
                break
       
        if not self._siteKey :
             raise LookupError('Unable to find the FFMP site key associated with: ' + self._sourceName + '.')
            
        self._dataKey = self._siteKey
       
        self.ffmpTemplates= FFMPTemplates.getInstance(self._domain, self._siteKey, FFMPTemplates.MODE.CAVE)

    def getIdentifiersOfCurrentEventsToBeDeleted(self, eventSet, sessionAttributes):
        siteID = eventSet.getAttributes().get('siteID')

        if self.hazardEventLockUtils is None:
            self.hazardEventLockUtils = HazardEventLockUtils(self.practice)
        lockedHazardIds = self.hazardEventLockUtils.getLockedEvents()

         # Get current events from Session Manager (could include pending / potential)
        currentEvents = []
        for event in eventSet:
            # Recommender only cares about potential FF.W.Convective hazards
            if (event.getPhenomenon() == FLASH_FLOOD_PHENOMENON
                  and event.getSignificance() == FLASH_FLOOD_SIGNIFICANCE
                  and event.getSubType() == FLASH_FLOOD_SUBTYPE
                  and event.getStatus() == "POTENTIAL"):
                currentEvents.append(event)
        # Add in those from the Database
        databaseEvents = HazardDataAccess.getHazardEventsBySite(siteID, self.practice) 
        eventIDs = [event.getEventID() for event in currentEvents]
        for event in databaseEvents:
            if event.getEventID() not in eventIDs:
                # Recommender only cares about potential FF.W.Convective hazards
                if (event.getPhenomenon() == FLASH_FLOOD_PHENOMENON
                    and event.getSignificance() == FLASH_FLOOD_SIGNIFICANCE
                    and event.getSubType() == FLASH_FLOOD_SUBTYPE
                    and event.getStatus() == "POTENTIAL"):
                    currentEvents.append(event)
                    eventIDs.append(event.getEventID())

        currentEventIdentifiers = []
        for event in currentEvents:
            eventIdentifier = event.getEventID()
            if eventIdentifier not in lockedHazardIds:
                currentEventIdentifiers.append(eventIdentifier)

        return currentEventIdentifiers
   
    def __str__(self):
        return 'Flash Flood Recommender'

