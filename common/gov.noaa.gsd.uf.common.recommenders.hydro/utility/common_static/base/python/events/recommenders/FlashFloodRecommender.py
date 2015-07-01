'''
Flash flood recommender.

Produces FF.W (Flash Flood Warning) hazard recommendations
for small basins or aggregates of small basins using
FFMP preprocessed data sources(QPE, QPF and Guidance).

@since: May 2014
@author: Matt Nash
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate

#from shapely import geometry

from ufpy.dataaccess import DataAccessLayer
from ufpy.dataaccess.PyGeometryData import PyGeometryData
from com.raytheon.uf.common.monitor.config import FFMPSourceConfigurationManager
from com.raytheon.uf.common.monitor.config import FFMPRunConfigurationManager

from com.raytheon.uf.common.dataplugin.ffmp import FFMPBasinData, FFMPBasin, FFMPTemplates, FFMPTemplates_MODE as FFMPMode

from java.util import Date
from java.lang import Long, Float

import JUtil
#
# Keys to values in the attributes dictionary produced
# by the flash flood recommender.
ALL_HUC = 'ALL'
QPE_SOURCE = 'qpeSource'
GUID_SOURCE = 'guidSource'
TYPE_SOURCE = 'typeSource'
ACCUMULATION_INTERVAL = 'accumulationInterval'

#  Time and key constants
FLASH_FLOOD_PHENOMENON = 'FF'
FLASH_FLOOD_SIGNIFICANCE = 'W'
FLASH_FLOOD_SUBTYPE = 'Convective'
POTENTIAL_TYPE = 'POTENTIAL'
CURRENT_TIME = 'currentTime'
CURRENT_SITE = 'siteID'

DEFAULT_FFW_DURATION_IN_SECONDS = 6*60*60  # 6 hours.

#
# Keys for requests to data access layer
FFMP_KEY = 'ffmp'
WFO_KEY = 'wfo'
SITE_KEY = 'siteKey'
DATA_KEY = 'dataKey'
HUC_KEY = 'huc'

#
# Keys for accessing small basin map.
PRECIP_VALUE = 'precipValue'
GUIDANCE_VALUE = 'guidanceValue'
COMPARE_VALUE = 'compareValue'
GEOMETRY_KEY = 'geometry'

VALUE_TYPES = ['diff', 'ratio', 'qpe']
#
# Supported FFMP QPE, Guidance and Forecast data sources.
# FFMP configuration may be read for these sources for
# information such as duration and expiration time. This
# allows new sources to be incrementally implemented.
# It also allows only supported sources to be
# shown to the forecaster in the recommender dialog.
# See ffmp/FFMPSourceConfig.xml for available FFMP
# data sources.
supportedQPESourceList = ['DHR']
supportedGuidanceSourceList = ['FFG']

ffgGuidances = ['FFG0124hr', 'FFG0324hr', 'FFG0624hr']
     
class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.smallBasinMap = {}

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
        
        return metadata

    def defineDialog(self, eventSet):
        '''
        Reads the supported QPE, QPF and FFG sources to build the QPE source,
        QPF source, and Guidance source options on the tool dialog.
        
        @return: A dialog definition to solicit user input before running tool
        '''   
        dialogDict = {'title': 'Flash Flood Recommender'}
        
        qpeChoices = []
        qpfChoices = []
        guidChoices = []
        
        qpeDisplayNames = []
        qpfDisplayNames = []
        guidDisplayNames = []
        
        #
        # Process supported FFMP QPE sources
        for choice in supportedQPESourceList:
            qpeChoices.append(choice)
            qpeDisplayNames.append(choice)
             
        #
        # Process supported FFMP Guidance sources       
        for choice in supportedGuidanceSourceList:
            guidChoices.append(choice)
            guidDisplayNames.append(choice)
                    
        valueDict = {}            
        qpeDict = {}
        fieldDictList = []
        qpeDict['fieldType'] = 'ComboBox'
        qpeDict['fieldName'] = QPE_SOURCE
        qpeDict['label'] = 'QPE Source:'
        qpeDict['choices'] = qpeChoices
        qpeDict['defaultValues'] = qpeChoices[0]
        valueDict[QPE_SOURCE] = qpeDict['defaultValues']
        fieldDictList.append(qpeDict)

        guidDict = {}
        guidDict['fieldType'] = 'ComboBox'
        guidDict['fieldName'] = GUID_SOURCE
        guidDict['label'] = 'Guidance Source:'
        guidDict['choices'] = guidChoices
        guidDict['defaultValues'] = guidChoices[0]
        valueDict[GUID_SOURCE] = guidDict['defaultValues']
        fieldDictList.append(guidDict)
        
        accumulationIntervalDict = {}
        accumulationIntervalDict['fieldType'] = 'IntegerSpinner'
        accumulationIntervalDict['showScale'] = 1
        accumulationIntervalDict['fieldName'] = ACCUMULATION_INTERVAL
        accumulationIntervalDict['label'] = 'Accumulation Interval:'
        accumulationIntervalDict['minValue'] = 1
        accumulationIntervalDict['maxValue'] = 24
        accumulationIntervalDict['incrementDelta'] = 1
        valueDict[ACCUMULATION_INTERVAL] = 1
        fieldDictList.append(accumulationIntervalDict)

        typeDict = {}
        typeDict['fieldType'] = 'ComboBox'
        typeDict['fieldName'] = TYPE_SOURCE
        typeDict['label'] = 'FFMP Fields:'
        typeDict['choices'] = VALUE_TYPES
        typeDict['defaultValues'] = VALUE_TYPES[0]
        valueDict[TYPE_SOURCE] = typeDict['defaultValues']
        fieldDictList.append(typeDict)
        
        # TODO maybe use side effects here?
        compareValueDict = {}
        compareValueDict['fieldType'] = 'FractionSpinner'
        compareValueDict['showScale'] = 0
        compareValueDict['fieldName'] = COMPARE_VALUE
        compareValueDict['label'] = 'Value'
        compareValueDict['minValue'] = 0
        compareValueDict['maxValue'] = 24
        compareValueDict['incrementDelta'] = 1
        compareValueDict['precision'] = 1
        valueDict['compareValue'] = 1
        fieldDictList.append(compareValueDict)

        dialogDict['fields'] = fieldDictList
        dialogDict['valueDict'] = valueDict
        
        return dialogDict
    
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        '''
        Runs the Flash Flood Recommender tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param spatialInputMap:   A map of information retrieved
                                  from the user's interaction with the
                                  spatial display.
        
        @return: A list of potential Flash Flood Hazard events. 
        '''
        self.currentTime = eventSet.getAttribute(CURRENT_TIME)
        self.currentSite = eventSet.getAttribute(CURRENT_SITE)

        self._sourceName = dialogInputMap.get(QPE_SOURCE)   
        self._guidanceName = dialogInputMap.get(GUID_SOURCE)
        
        self.accumulationHours = int(dialogInputMap.get(ACCUMULATION_INTERVAL))
        
        self.type = dialogInputMap.get(TYPE_SOURCE)  # pull this out of the dialog
        
        self.compareValue = dialogInputMap.get(COMPARE_VALUE)
        self._localize()
        cont = self.getQPEValues()
        if cont :
            self.getGuidanceValues()
       
        return self.buildEvents(cont)
    
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
            if time.getRefTime().getTime() > timedelta :
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
        
        if self._guidanceName == 'FFG':
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
            self.counties = set()
            basins = []
            for basinName in self.smallBasinMap:
                basin = self.smallBasinMap[basinName]
                # need better logic here, based on type, don't need guidance
                # need to fix storing of values...
                if basin.has_key(PRECIP_VALUE):
                    precipValue = basin[PRECIP_VALUE]
                    guidanceValue = basin[GUIDANCE_VALUE]
                    
                    if guidanceValue > 0.0:
                        if self.type == 'diff' :
                            value = self._calcDiff(precipValue, guidanceValue)
                        elif self.type == 'ratio' :
                            value = self._calcRatio(precipValue, guidanceValue)
                    if self.type == 'qpe' :
                        value = self._calcQPE(precipValue)
                    if value >= self.compareValue:
                        basinMetadata = self.ffmpTemplates.getBasin(JUtil.pyValToJavaObj(long(basinName)))
                        county = basinMetadata.getCounty()
                        if county:
                            self.counties.add(county)
                        
            if self.counties :
                # Find the county for each basin, put in
                # set, combine all the county Geometries into a 
                # single geometry.
                hazardEvent = EventFactory.createEvent()
                hazardEvent.setSiteID(self.currentSite)
                hazardEvent.setHazardStatus(POTENTIAL_TYPE)
                hazardEvent.setPhenomenon(FLASH_FLOOD_PHENOMENON)
                hazardEvent.setSignificance(FLASH_FLOOD_SIGNIFICANCE)
                hazardEvent.setSubType(FLASH_FLOOD_SUBTYPE)
            
                countyGeometries = []
                rawGeometryList = [ ]

                for countyname in self.counties:
                    req = DataAccessLayer.newDataRequest()
                    req.setDatatype('maps')
                    req.addIdentifier('table','mapdata.county')
                    columns = ['countyname']
                    req.addIdentifier('locationField', columns[0])
                    req.setParameters(*columns)
                    req.addIdentifier('geomField','the_geom')
                    req.addIdentifier('countyname', countyname)
                    req.addIdentifier('cwa', self._wfo)
                    countyGeomList = DataAccessLayer.getGeometryData(req)
                    if countyGeomList is not None and len(countyGeomList) > 0:
                        for countyGeom in countyGeomList:
                            if countyGeom is not None:
                                rawGeometryList.append(countyGeom.getGeometry())
                    
                geoCollection = GeometryFactory.createCollection(rawGeometryList)
                unionGeometry = GeometryFactory.performCascadedUnion(geoCollection)
                hazardEvent.setGeometry(unionGeometry)
                
                # Convert currentTime from milliseconds to seconds.
                currentTime = long(self.currentTime) / 1000
                creationDateTime = datetime.datetime.fromtimestamp(currentTime)
                startDateTime = creationDateTime
                endDateTime = datetime.datetime.fromtimestamp((currentTime + DEFAULT_FFW_DURATION_IN_SECONDS))
        
                hazardEvent.setCreationTime(creationDateTime)
                hazardEvent.setStartTime(startDateTime)
                hazardEvent.setEndTime(endDateTime)
                pythonEventSet.add(hazardEvent)
            else : 
                raise Exception("No events returned!")
        else :
            raise Exception('No basins available!')
        
        return pythonEventSet
    
    def _calcDiff(self, qpe, guid):
        '''
        Calculates the diff based on the qpe and guidance
        '''
        if math.isnan(qpe) :
            qpe = 0
        if math.isnan(guid) :
            guid = 0
            
        qpe = round(qpe, 2)
        guid = round(guid, 2)
        
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
        if qpe >= 0 and guid >= 0 :
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
        
        self.ffmpTemplates= FFMPTemplates.getInstance(self._domain, self._siteKey, FFMPMode.CAVE)
        
    def __str__(self):
        return 'Flash Flood Recommender'
