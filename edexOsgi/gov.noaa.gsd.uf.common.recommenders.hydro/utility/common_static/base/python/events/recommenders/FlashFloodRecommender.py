"""
Flash flood recommender.

Produces FF.W (Flash Flood Warning) hazard recommendations
for small basins or aggregates of small basins using
FFMP preprocessed data sources(QPE, QPF and Guidance).

@since: July 2013
@author: Bryon Lawrence
"""
import datetime
import EventFactory
import GeometryFactory
import RecommenderTemplate
import sys
import EventSetFactory

from ufpy.dataaccess import DataAccessLayer

from com.raytheon.uf.common.monitor.config import FFMPSourceConfigurationManager

#
# Keys to values in the attributes dictionary produced
# by the flash flood recommender.
ALL_HUC = 'ALL'
QPE_SOURCE = 'qpeSource'
GUID_SOURCE = 'guidSource'
FORECAST_SOURCE = 'qpfSource'
ACCUMULATION_INTERVAL = 'accumulationInterval'

#
# Keys required to access FFMP datasets.
SITE_KEY = 'koax'
DATA_KEY = 'koax'
CWA = 'OAX'
WFO = 'OAX'

#
#  Time and key constants
FFW_PHENOMENON = 'FF'
FFW_SIGNIFICANCE = 'W'
FFW_SUBTYPE = 'Convective'
POTENTIAL_TYPE = 'POTENTIAL'

DEFAULT_FFW_DURATION_IN_SECONDS = 10800 # 3 hours.
SECONDS_PER_HOUR = 3600

#
# Keys for requests to data access layer
FFMP_KEY = 'ffmp'
WFO_KEY = 'wfo'
SITE_REQUEST_KEY = 'siteKey'
DATA_REQUEST_KEY = 'dataKey'
HUC_REQUEST_KEY = 'huc'

#
# Keys for accessing small basin map.
PRECIP_VALUE_KEY = 'precipValue'
GUIDANCE_VALUE_KEY = 'guidanceValue'
GEOMETRY_KEY = 'geometry'


#
# Supported FFMP QPE, Guidance and Forecast data sources.
# FFMP configuration may be read for these sources for
# information such as duration and expiration time. This
# allows new sources to be incrementally implemented.
# It also allows only supported sources to be
# shown to the forecaster in the recommender dialog.
# See ffmp/FFMPSourceConfig.xml for available FFMP
# data sources.
supportedQPESourceList = [{'displayString':'DHR', 'identifier':'DHR'}]
supportedGuidanceSourceList = [{'displayString':'RFCFFG', 'identifier':'RFCFFG'}]
supportedForecastSourceList = []

     
class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        # 
        # Strategies for handling supported QPE, Guidance and
        # Forecast data sources. When a focal point wants to update
        # this recommender to support a new QPE/FFG/Forecast source,
        # new strategy methods can be added to handle these new
        # datasets, and these dicts can be updated to reflect them.
        self.QPEStrategies = {'DHR':self.getAccumulatedDHR}
        self.GuidanceStrategies = {'RFCFFG':self.getRFCGuidance}
        
        self.smallBasinMap = {}


    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metaDict = {}
        metaDict['toolName'] = 'Flash Flood Recommender'
        metaDict['author'] = 'GSD'
        metaDict['toolType'] = 'Recommender'
        metaDict['returnType'] = 'IEvent List'
        metaDict['eventState'] = 'Potential'
        
        return metaDict

    def defineDialog(self):
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
        for choiceDict in supportedQPESourceList:
            qpeChoices.append(choiceDict)
            qpeDisplayNames.append(choiceDict['displayString'])
             
        #
        # Process supported FFMP Guidance sources       
        for choiceDict in supportedGuidanceSourceList:
            guidChoices.append(choiceDict)
            guidDisplayNames.append(choiceDict['displayString'])
            
        #
        # Process supported FFMP forecast sources
        for choiceDict in supportedForecastSourceList:
            qpfChoices.append(choiceDict)
            qpfDisplayNames.append(choiceDict['displayString'])
        
        valueDict = {}            
        qpeDict = {}
        fieldDictList = []
        qpeDict['fieldType'] = 'ComboBox'
        qpeDict['fieldName'] = QPE_SOURCE
        qpeDict['label'] = 'QPE Source:'
        qpeDict['choices'] = qpeChoices
        qpeDict['defaultValues'] = qpeChoices[0]['identifier']
        valueDict[QPE_SOURCE] =  qpeDict['defaultValues']
        fieldDictList.append(qpeDict)

        if qpfChoices:
            checkBoxDict = {}
            checkBoxDict['fieldType'] = 'CheckBoxes'
            checkBoxDict['fieldName'] = 'includeQPF'
            checkBoxDict['choices'] = [{'displayString':'Include QPF', 'identifier':'yes'}]
            checkBoxDict['defaultValues'] = 'yes'
            valueDict['includeQPF'] = 'yes'
            fieldDictList.append(checkBoxDict)
        
            qpfDict = {}
            qpfDict['fieldType'] = 'ComboBox'
            qpfDict['fieldName'] = FORECAST_SOURCE
            qpfDict['label'] = 'QPF Source:'
            qpfDict['choices'] = qpfChoices
            qpfDict['defaultValues'] = qpfChoices[0]['identifier']
            valueDict[FORECAST_SOURCE] = qpfDict['defaultValues']
            fieldDictList.append(qpfDict)
            

        guidDict = {}
        guidDict['fieldType'] = 'ComboBox'
        guidDict['fieldName'] = GUID_SOURCE
        guidDict['label'] = 'Guidance Source:'
        guidDict['choices'] = guidChoices
        guidDict['defaultValues'] = guidChoices[0]['identifier']
        valueDict[GUID_SOURCE] = guidDict['defaultValues']
        fieldDictList.append(guidDict)
        
        accumulationIntervalDict = {}
        accumulationIntervalDict['fieldType'] = 'IntegerSpinner'
        accumulationIntervalDict['showScale'] = 1
        accumulationIntervalDict['fieldName'] = ACCUMULATION_INTERVAL
        accumulationIntervalDict['label'] = 'Accumulation Interval'
        accumulationIntervalDict['minValue'] = 1
        accumulationIntervalDict['maxValue'] = 24
        accumulationIntervalDict['incrementDelta'] = 1
        valueDict[ACCUMULATION_INTERVAL] = 1
        fieldDictList.append(accumulationIntervalDict)

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

        self.sessionAttributes = eventSet.getAttributes()
    
        self.sourceName = dialogInputMap.get(QPE_SOURCE)   
        self.ffgName = dialogInputMap.get(GUID_SOURCE)
        self.accumulationHours = int(dialogInputMap.get(ACCUMULATION_INTERVAL))
        self.getRecommendation()
        
        return self.buildEventList()
        
    
    def getRecommendation(self):
        '''
        Retrieves the QPE and FFG data required for
        producing FF.W hazard recommendations for the 
        small basins in the forecast area.
        '''
        self.getAccumulatedQPE()
        self.getGuidanceValues()
 
    def getAccumulatedQPE(self):
        '''
        Calls the correct QPE processing strategy method based
        on the user-selected QPE source. 
        '''
        self.QPEStrategies[self.sourceName]()

    def getGuidanceValues(self):
        '''
        Calls the correct FFG processing strategy method based
        on the user-selected FFG source.
        '''
        self.GuidanceStrategies[self.ffgName]()
        
           
    def getAccumulatedDHR(self):
        '''
        Strategy method for reading and accumulating data
        from preprocessed FFMP DHR datasets.
        '''
        #
        # Expiration time of the DHR source in seconds.
        SOURCE_EXPIRATION = 600   # seconds
        
        #
        # Retrieve a list of available times for DHR
        # data
        request = DataAccessLayer.newDataRequest()
        request.setDatatype(FFMP_KEY)
        request.setParameters(self.sourceName)
        request.addIdentifier(WFO_KEY, WFO)
        request.addIdentifier(SITE_REQUEST_KEY, SITE_KEY)
        request.addIdentifier(DATA_REQUEST_KEY, DATA_KEY)
        request.addIdentifier(HUC_REQUEST_KEY, ALL_HUC)
        availableTimes = DataAccessLayer.getAvailableTimes(request)
        
        if availableTimes:
            #
            # Determine the start and end times
            # of the accumulation interval
            accumulationEndTime = availableTimes[-1].getRefTime()
            accumulationStartTime = accumulationEndTime - self.accumulationHours * SECONDS_PER_HOUR;
            
            dateTimesToAccumulateOver = []
            
            for availableTime in availableTimes:
                if availableTime.getRefTime() > accumulationStartTime and \
                   availableTime.getRefTime() < accumulationEndTime:
                    dateTimesToAccumulateOver.append(availableTime)
                    
            dateTimesToAccumulateOver.reverse()      
            
            #
            # Loop over the dates to retrieve data for.
            # Accumulate the data.
            previousDate = availableTimes[-1]
            
            for accumulateDate in dateTimesToAccumulateOver:
                geometryDataList = DataAccessLayer.getGeometryData(request, [accumulateDate])

                #
                # Determine multiplicative factor scale factor for
                # accumulated precipitation.
                # This logic block is similar to that used in 
                # FFMPBasin.getAccumValue()
                factor = 0.0
                
                if previousDate.getRefTime() - accumulateDate.getRefTime() > SOURCE_EXPIRATION:
                    factor = (float(previousDate.getRefTime() - (previousDate.getRefTime() - SOURCE_EXPIRATION))/float(SECONDS_PER_HOUR))
                else:
                    factor = (float(previousDate.getRefTime() - accumulateDate.getRefTime())/float(SECONDS_PER_HOUR))
                
                for geometryData in geometryDataList:
                    precipitationValue = geometryData.getNumber(self.sourceName)
                    precipitationValue = precipitationValue * factor
                    self.storeQPEValue(geometryData.getLocationName(), precipitationValue, \
                                       geometryData.getGeometry()) 
                    
                previousDate = accumulateDate
                            
    
    def getRFCGuidance(self):
        '''
        Strategy method for reading, interpolating and storing RFC FFG datasets.
        This method reads preprocessed FFMP RFCFFG datasets. It determines the RFCFFG
        dataset whose duration most closely matches the requested accumulation interval.
        These data are then interpolated for each basin which has a non-zero QPE.
        '''

        #
        # Read the available data sets for the RFC FFG guidance source. 
        ffmpSourceConfigManager = FFMPSourceConfigurationManager.getInstance()
        product = ffmpSourceConfigManager.getProduct(self.sourceName)

        guidanceList = product.getGuidanceSourcesByType(self.ffgName)
        
        bestGuidanceSource = None
        minDurationDiff = sys.maxint
   
        #
        # Find the RFC FFG data set with the best matching duration
        # to the requested accumulation interval.
        for i in range(guidanceList.size()):
            guidanceSource = guidanceList.get(i)
            durationHour = guidanceSource.getDurationHour()
            diff = abs(durationHour - self.accumulationHours)
            
            if minDurationDiff > diff:
                bestGuidanceSource = guidanceSource
                minDurationDiff = diff
                
        #
        # Retrieve a list of available times for this dataset.        
        bestGuidanceSourceName = bestGuidanceSource.getSourceName()
        request = DataAccessLayer.newDataRequest()
        request.setDatatype(FFMP_KEY)
        request.setParameters(bestGuidanceSourceName)
        request.addIdentifier(WFO_KEY, WFO)
        request.addIdentifier(SITE_REQUEST_KEY, SITE_KEY)
        request.addIdentifier(HUC_REQUEST_KEY, ALL_HUC)
        availableTimes = DataAccessLayer.getAvailableTimes(request)

        if availableTimes:            
            #
            # Retrieve the most recent dataset.
            mostRecentTime = availableTimes[-1]

            #
            # Retrieve the ffg data for this time.
            guidanceDataList = DataAccessLayer.getGeometryData(request, [mostRecentTime])
        
            #
            # Apply adjustment to totals.
            interpFactor = self.accumulationHours / bestGuidanceSource.getDurationHour()
            
            #
            # Store the guidance data in the small basin map.
            if guidanceDataList:
                
                for guidanceData in guidanceDataList:
                    basinName = guidanceData.getLocationName()
                    value = guidanceData.getNumber(bestGuidanceSourceName)
                    self.storeGuidanceValue(basinName, value * interpFactor)

    def storeGuidanceValue(self, basinName, guidanceValue):
        '''
        For use by FFG processing strategy methods. Stores
        the resultant interpolated FFG value for a basin
        in the small basin map.
        
        @param basinName:  The name of the basin to store the FFG value
                           for
        @param guidanceValue: The interpolated FFG value in inches for
                              the basin. 
        '''
        #
        # Only store guidance values for basins with QPE
        if basinName in self.smallBasinMap:
            basin = self.smallBasinMap[basinName]
            basin[GUIDANCE_VALUE_KEY] = guidanceValue

    def storeQPEValue(self, basinName, precipValue, geometry):
        '''
        For use by QPE processing strategy methods. Stores the resultant
        estimated precipitation amount for a basin in the small basin
        map.
        
        @param basinName:  The name of the basin to store the precip estimate
                           for
        @param precipValue: The precipitation estimate in inches for the
                            basin.
        @param geometry:   The basin geometry, or any geometry the strategy
                           method wants to associate with this hazard.
        '''
        # 
        # Don't store precipitation values of 0.
        if precipValue > 0.0:
                        
            if basinName in self.smallBasinMap:
                basin = self.smallBasinMap[basinName]
                accumulatedValue = basin[PRECIP_VALUE_KEY]
                basin[PRECIP_VALUE_KEY] = accumulatedValue + precipValue
            else:
                basin = {}
                basin[PRECIP_VALUE_KEY] = precipValue
                basin[GEOMETRY_KEY] = geometry
                self.smallBasinMap[basinName] = basin
                
    def buildEventList(self):
        '''
        Builds a list of FF.W hazard events. For each small basin
        with a non-zero estimated precipitation amount, this value
        is compared with the interpolated FFG for that basin. If the
        ration of precip to FFG is 1 or more, this small basin is
        flagged as a potential location for flash flooding.
        
        @return: An EventSet of potential flash flood (FF.W) hazard events
        '''
        pythonEventSet = EventSetFactory.createEventSet()
        
        for basinName in self.smallBasinMap:
            basin = self.smallBasinMap[basinName]
            precipValue = basin[PRECIP_VALUE_KEY]
            guidanceValue = basin[GUIDANCE_VALUE_KEY]
            
            if guidanceValue > 0.0:
                ratio = precipValue / guidanceValue
            
                if ratio >= 1.0:
                    hazardEvent = EventFactory.createEvent()
                    hazardEvent.setEventID('')
                    hazardEvent.setSiteID(SITE_KEY)
                    hazardEvent.setHazardState(POTENTIAL_TYPE)
                    hazardEvent.setPhenomenon(FFW_PHENOMENON)
                    hazardEvent.setSignificance(FFW_SIGNIFICANCE)
                    hazardEvent.setSubtype(FFW_SUBTYPE)
                    geometry = basin[GEOMETRY_KEY]
                    hazardEvent.setGeometry(GeometryFactory.createCollection([geometry]))
                    
                    currentTime = long(self.sessionAttributes['currentTime'])
                    
                    #
                    # Convert currentTime from milliseconds to seconds.
                    currentTime = currentTime / 1000
                    creationDateTime = datetime.datetime.fromtimestamp(currentTime)
                    startDateTime = datetime.datetime.fromtimestamp(currentTime)
                    endDateTime = datetime.datetime.fromtimestamp((currentTime + DEFAULT_FFW_DURATION_IN_SECONDS))
            
                    hazardEvent.setIssueTime(creationDateTime)
                    hazardEvent.setStartTime(startDateTime)
                    hazardEvent.setEndTime(endDateTime)
                    pythonEventSet.add(hazardEvent)
                    
        return pythonEventSet
        
        
    def toString(self):
        return 'FlashFloodRecommender'
    
    
