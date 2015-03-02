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
from com.raytheon.uf.common.monitor.config import FFMPRunConfigurationManager

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
#The following variables need to be overridden at the site level e.g. 'koax'
SITE_KEY = ''
DATA_KEY = ''

#The following variables need to be overridden at the site level e.g. 'OAX'
CWA = ''
WFO = ''

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
QPESourceDict = {}
GuidanceSourceList = [{'displayString':'RFCFFG', 'identifier':'RFCFFG'}]
ForecastSourceList = []

     
class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        # 
        # Strategies for handling supported QPE, Guidance and
        # Forecast data sources. When a focal point wants to update
        # this recommender to support a new QPE/FFG/Forecast source,
        # new strategy methods can be added to handle these new
        # datasets, and these dicts can be updated to reflect them.
        self.QPEStrategies = {}
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
        metaDict['version'] = '1.0'
        metaDict['description'] = 'Uses FFMP data to get current and aggregated data similar to FFMP..'
        metaDict['eventState'] = 'Potential'
        
        return metaDict

    def defineDialog(self, eventSet):
        '''
        Reads the supported QPE, QPF and FFG sources to build the QPE source,
        QPF source, and Guidance source options on the tool dialog.
        
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @return: A dialog definition to solicit user input before running tool
        '''        
        self.initializeFFMPConfig()
        dialogDict = {'title': 'Flash Flood Recommender'}
        
        valueDict = {}            
        dialogDict['fields'] = [{
                              'fieldType':'ComboBox', 
                              'fieldName':QPE_SOURCE,
                              'label':'QPE DHR Source',
                              'choices':QPESourceDict.values()
                              }]
        dialogDict['valueDict'] = {
                                   ACCUMULATION_INTERVAL : 0.25,
                                   QPE_SOURCE: QPESourceDict.values()[0]['identifier'],
                                   GUID_SOURCE: GuidanceSourceList[0]['identifier']
                                   }
        

        if ForecastSourceList:
            dialogDict['valueDict']['includeQPF'] = 'yes'
            
            dialogDict['fields'].append({
                                  'fieldType':'CheckBoxes',
                                  'fieldName':'includeQPF',
                                  'choices':[{'displayString':'Include QPF', 'identifier':'yes'}],
                                  'defaultValues':'yes'
                                  })
            
            dialogDict['fields'].append({
                                  'fieldType':'ComboBox',
                                  'fieldName':FORECAST_SOURCE,
                                  'label':'QPF Source',
                                  'choices':ForecastSourceList
                                  })
            dialogDict['valueDict'][FORECAST_SOURCE] = ForecastSourceList[0]['identifier']
        
        dialogDict['fields'].append({
                              'fieldType':'ComboBox',
                              'fieldName':GUID_SOURCE,
                              'label':'Guidance Source:',
                              'choices':GuidanceSourceList})
        
        dialogDict['fields'].append({
                              'fieldType':'FractionSpinner',
                              'precision':2,
                              'showScale':1,
                              'fieldName':ACCUMULATION_INTERVAL,
                              'label':'Accumulation Interval',
                              'minValue':0.25,
                              'maxValue':24,
                              'incrementDelta':0.25})
        
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
        self.selectedQPESource = QPESourceDict[dialogInputMap.get(QPE_SOURCE)]   
        self.selectedGuidanceSource = dialogInputMap.get(GUID_SOURCE)
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
        self.QPEStrategies[self.selectedQPESource['identifier']]()

    def getGuidanceValues(self):
        '''
        Calls the correct FFG processing strategy method based
        on the user-selected FFG source.
        '''
        self.GuidanceStrategies[self.selectedGuidanceSource]()
        
           
    def getAccumulatedDHR(self):
        '''
        Strategy method for reading and accumulating data
        from preprocessed FFMP DHR datasets.
        '''
        #
        # Expiration time of the DHR source in seconds.
        SOURCE_EXPIRATION = 600   # seconds
        
        dataKey = self.selectedQPESource['dataKey']
        siteKey = self.selectedQPESource['siteKey']
        #
        # Retrieve a list of available times for DHR
        # data
        request = DataAccessLayer.newDataRequest()
        request.setDatatype(FFMP_KEY)
        request.setParameters(self.selectedQPESource['productName'])
        request.addIdentifier(WFO_KEY, WFO)
        request.addIdentifier(SITE_REQUEST_KEY, siteKey)
        request.addIdentifier(DATA_REQUEST_KEY, dataKey)
        request.addIdentifier(HUC_REQUEST_KEY, ALL_HUC)
        availableTimes = DataAccessLayer.getAvailableTimes(request)
        
        if availableTimes:
            #
            # Determine the start and end times
            # of the accumulation interval
            accumulationEndTime = availableTimes[-1].getRefTime().getTime()
            accumulationStartTime = accumulationEndTime - (self.accumulationHours * SECONDS_PER_HOUR * 1000);
            
            dateTimesToAccumulateOver = []
            
            for availableTime in availableTimes:
                 if availableTime.getRefTime().getTime() > accumulationStartTime and \
                   availableTime.getRefTime().getTime() < accumulationEndTime:
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
                
                if previousDate.getRefTime().getTime() - accumulateDate.getRefTime().getTime() > SOURCE_EXPIRATION * 1000:
                    factor = (float(previousDate.getRefTime().getTime() - (previousDate.getRefTime().getTime() - (SOURCE_EXPIRATION * 1000)))/float(SECONDS_PER_HOUR * 1000))
                else:
                    factor = (float(previousDate.getRefTime().getTime() - accumulateDate.getRefTime().getTime())/float(SECONDS_PER_HOUR * 1000))
                
                for geometryData in geometryDataList:
                    precipitationValue = geometryData.getNumber(self.selectedQPESource['productName'])
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
        # Read the available data sets for the specified source. 
        ffmpSourceConfigManager = FFMPSourceConfigurationManager.getInstance()
        bestGuidanceSource = ffmpSourceConfigManager.getSource(self.selectedQPESource['productName'])
        if not bestGuidanceSource:
            raise LookupError('Unable to find the source configuration for FFMP source: ' + self.selectedQPESource['productName'])
                
        #
        # Retrieve a list of available times for this dataset.        
        bestGuidanceSourceName = bestGuidanceSource.getSourceName()
        request = DataAccessLayer.newDataRequest()
        request.setDatatype(FFMP_KEY)
        request.setParameters(bestGuidanceSourceName)
        request.addIdentifier(WFO_KEY, WFO)
        request.addIdentifier(SITE_REQUEST_KEY, self.selectedQPESource['siteKey'])
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
            if bestGuidanceSource.getDurationHour() == 0:
                interpFactor = self.accumulationHours
            else:
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
                    hazardEvent.setSiteID(self.sessionAttributes.get('siteID'))
                    hazardEvent.setHazardStatus(POTENTIAL_TYPE)
                    hazardEvent.setPhenomenon(FFW_PHENOMENON)
                    hazardEvent.setSignificance(FFW_SIGNIFICANCE)
                    hazardEvent.setSubType(FFW_SUBTYPE)
                    geometry = basin[GEOMETRY_KEY]
                    hazardEvent.setGeometry(GeometryFactory.createCollection([geometry]))
                    
                    currentTime = long(self.sessionAttributes['currentTime'])
                    
                    #
                    # Convert currentTime from milliseconds to seconds.
                    currentTime = currentTime / 1000
                    creationDateTime = datetime.datetime.fromtimestamp(currentTime)
                    startDateTime = datetime.datetime.fromtimestamp(currentTime)
                    endDateTime = datetime.datetime.fromtimestamp((currentTime + DEFAULT_FFW_DURATION_IN_SECONDS))
            
                    hazardEvent.setCreationTime(creationDateTime)
                    hazardEvent.setStartTime(startDateTime)
                    hazardEvent.setEndTime(endDateTime)
                    pythonEventSet.add(hazardEvent)
               
        return pythonEventSet
    
    def initializeFFMPConfig(self):            
        # Dynamically populate the QPE sources from the FFMP config
        runner = FFMPRunConfigurationManager.getInstance().getRunner(CWA)
        if runner is not None:
            products = runner.getProducts()
            if products is not None:
                for i in range(0,products.size()):
                    productKey = str(products.get(i).getProductKey())
                    productName = str(products.get(i).getProductName())
                    identifier = productKey+'_'+productName
                    
                    entry = {'displayString':productKey, 
                             'identifier':identifier, 
                             'dataKey':DATA_KEY, 
                             'siteKey':SITE_KEY, 
                             'productName':productName}
                    
                    if productName == 'DHR':
                        entry['dataKey'] = productKey
                        entry['siteKey'] = productKey
                        
                    QPESourceDict[identifier] = entry 
                    
                    self.QPEStrategies[identifier] = self.getAccumulatedDHR;
        
        
    def toString(self):
        return 'FlashFloodRecommender'
