"""
    Description: Product Generator for the Convective SIGMET product.
"""
import os, types, sys, collections, re
from HydroProductParts import HydroProductParts 
import GeometryFactory
import HydroGenerator
from KeyInfo import KeyInfo
import shapely, time, datetime
import HazardDataAccess
import MetaData_Convective_SIGMET

OUTPUTDIR = '/scratch/convectiveSigmetTesting'

class Product(HydroGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()  
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'Convective_SIGMET_ProductGenerator'

###################################################
        
                
    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD/Raytheon"
        metadata['description'] = "Product generator for SIGMET.Convective."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self, eventSet):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        return {}

    def _initialize(self):
        super(Product, self)._initialize()
        # This is for the VTEC Engine
        self._productCategory = "SIGMET.Convective"
        self._areaName = '' 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        self._purgeHours = 8
        self._SIGMET_ProductName = 'CONVECTIVE SIGMET'
        self._includeAreaNames = False
        self._includeCityNames = False
        self._vtecProduct = False
                
    def execute(self, eventSet, dialogInputMap):          
        '''
        Inputs:
        @param eventSet: a list of hazard events (hazardEvents) plus
                               a map of additional variables
        @return productDicts, hazardEvents: 
             Each execution of a generator can produce 1 or more 
             products from the set of hazard events
             For each product, a productID and one dictionary is returned as input for 
             the desired formatters.
             Also, returned is a set of hazard events, updated with product information.

        '''
        self._initialize() 
        self.logger.info("Start ProductGeneratorTemplate:execute ESF")
        
        # Extract information for execution
        self._inputHazardEvents = eventSet.getEvents()
        metaDict = eventSet.getAttributes()
        self._issueFlag = metaDict.get('issueFlag')
        if dialogInputMap:
            self._storeDialogInputMap(dialogInputMap)
        else:
            self._dialogInputMap = {}
           
        self._spclTime = eventSet.getAttributes().get("currentTime")/1000
        
        self._countEast = 0
        self._countCentral = 0
        self._countWest = 0
        
        parts = ['currentTime', 'startTime', 'endTime', 'specialTime', 'startDate', 'endDate', 'states',
                 'boundingStatement', 'mode', 'modifier', 'embedded', 'motion', 'cloudTop', 'additionalHazards']
        
        productDict = {}
        productDict['productParts'] = parts
        eventDicts = [] 
          
        for event in self._inputHazardEvents:
            self._geomType = self._getGeometryType(event)
            self._convectiveSigmetSpecialIssuance = event.getHazardAttributes().get('convectiveSigmetSpecialIssuance')
            self._convectiveSigmetDomain = event.getHazardAttributes().get('convectiveSigmetDomain')
            
            dict = {}
            dict['eventID'] = event.getEventID()
            dict['sigmetNumber'] = self._sigmetNumber(event)
            dict['domain'] = event.getHazardAttributes().get('convectiveSigmetDomain')
            dict['specialIssuance'] = event.getHazardAttributes().get('convectiveSigmetSpecialIssuance')
            
            partDict = collections.OrderedDict()
            for partName in parts:
                exec "partStr = self._" + partName + "(event)"
                partDict[partName] = partStr
            
            dict['parts'] = partDict
            eventDicts.append(dict)
        
        productDict['events'] = eventDicts
        productDict['productID'] = 'SIGMET.Convective'
        productDict['productName'] = 'CONVECTIVE SIGMET'         

        return [productDict], self._inputHazardEvents
    
    def _getGeometryType(self, hazardEvent):        
        for g in hazardEvent.getGeometry():
            geomType = g.geom_type
        
        return geomType
    
    def _currentTime(self, hazardEvent):
        currentTime = time.mktime(hazardEvent.getCreationTime().timetuple())
        
        return currentTime
    
    def _startTime(self, hazardEvent):
        epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
        initTimeZ = time.strftime('%d%H%M', time.gmtime(epochStartTime))
        self._initTimeZ = initTimeZ        
        
        return initTimeZ
    
    def _endTime(self, hazardEvent):                   
        epochEndTime = time.mktime(hazardEvent.getEndTime().timetuple())
        endTimeZ = time.strftime('%d%H%M', time.gmtime(epochEndTime))     

        return endTimeZ
    
    def _specialTime(self, hazardEvent):
        if self._convectiveSigmetSpecialIssuance == True:
            specialTime = time.strftime('%d%H%M', \
                time.gmtime(self._spclTime))            
            return specialTime
        else:
            return None
    
    def _startDate(self, hazardEvent):
        epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
         
        if self._convectiveSigmetSpecialIssuance == True:
            initDateZ = time.strftime('%Y%m%d_%H%M', \
                time.gmtime(epochStartTime))       
        else:
            initDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochStartTime))        

        return initDateZ
    
    def _endDate(self, hazardEvent):
        epochEndTime = time.mktime(hazardEvent.getEndTime().timetuple())

        if self._convectiveSigmetSpecialIssuance == True:
            endDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochEndTime))            
        else:
            endDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochEndTime))            

        return endDateZ                        
    
    def _sigmetNumber(self, hazardEvent):
        if hazardEvent.getStatus() in ["PENDING"]:
            if self._issueFlag == "True":
                convectiveSigmetNumberStr = self._setConvectiveSigmetNumber(hazardEvent)
            else:
                convectiveSigmetNumberStr = self._getConvectiveSigmetNumber(hazardEvent)
                convectiveSigmetNumberStr = self._applyCount(convectiveSigmetNumberStr, self._countEast, self._countCentral, self._countWest)                                                        
        elif hazardEvent.getStatus() in ["ISSUED"]:
            convectiveSigmetNumberStr = hazardEvent.get('convectiveSigmetNumberStr')
        else:
            convectiveSigmetNumberStr = self._getConvectiveSigmetNumber(hazardEvent)            
            
        if hazardEvent.getStatus() in ["PENDING"]:
            if self._issueFlag is not "True":
                self._countEast, self._countCentral, self._countWest = self._addCount(self._countEast, self._countCentral, self._countWest)
                
        if hazardEvent.getStatus() in ["PENDING", "ISSUED"]:
            hazardEvent.set('convectiveSigmetNumberStr', convectiveSigmetNumberStr)
            hazardEvent.set('validTime', self._spclTime)             
                
        return convectiveSigmetNumberStr        
    
    def _setConvectiveSigmetNumber(self, hazardEvent):
        import json
        
        epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
        initTimeZ = time.strftime('%d%H%M', time.gmtime(epochStartTime))
        self._initTimeZ = initTimeZ 
        
        if os.path.isfile('/scratch/convectiveSigmetNumber.txt'):
            with open('/scratch/convectiveSigmetNumber.txt') as openFile:
                convectiveSigmetNumberDict = json.load(openFile)
                previousDate = convectiveSigmetNumberDict["Time"][:2]
                newDate = self._initTimeZ[:2]
            
            if previousDate == newDate:
                if self._convectiveSigmetDomain in convectiveSigmetNumberDict:
                    previousSigmetNumber = int(convectiveSigmetNumberDict[self._convectiveSigmetDomain])
                
                    if previousSigmetNumber == 99:
                        newSigmetNumber = 1
                        if 'hundred' in convectiveSigmetNumberDict:
                            convectiveSigmetNumberDict['hundred'] = 2
                        else:
                            convectiveSigmetNumberDict['hundred'] = 1
                    else:
                        newSigmetNumber = previousSigmetNumber + 1
                    convectiveSigmetNumberDict[self._convectiveSigmetDomain] = newSigmetNumber
                    convectiveSigmetNumberDict['Time'] = self._initTimeZ
                else:
                    newSigmetNumber = 1
                    convectiveSigmetNumberDict[self._convectiveSigmetDomain] = newSigmetNumber
                    convectiveSigmetNumberDict['Time'] = self._initTimeZ
            else:
                newSigmetNumber = 1
                convectiveSigmetNumberDict = {}
                convectiveSigmetNumberDict[self._convectiveSigmetDomain] = newSigmetNumber
                convectiveSigmetNumberDict['Time'] = self._initTimeZ                 
                
            with open('/scratch/convectiveSigmetNumber.txt', 'w') as outFile:
                json.dump(convectiveSigmetNumberDict, outFile)    
                
        else:
            newSigmetNumber = 1
            convectiveSigmetNumberDict = {'Time': self._initTimeZ, self._convectiveSigmetDomain: 1}       
            with open('/scratch/convectiveSigmetNumber.txt', 'w') as outFile:
                json.dump(convectiveSigmetNumberDict, outFile)
        
        if 'hundred' in convectiveSigmetNumberDict:
            newSigmetNumber = str(newSigmetNumber).zfill(2)
        else:
            newSigmetNumber = str(newSigmetNumber)
               
        return newSigmetNumber
    
    def _getConvectiveSigmetNumber(self, hazardEvent):
        import json
        
        if os.path.isfile('/scratch/convectiveSigmetNumber.txt'):
            with open('/scratch/convectiveSigmetNumber.txt') as openFile:
                convectiveSigmetNumberDict = json.load(openFile)
            if self._convectiveSigmetDomain in convectiveSigmetNumberDict:
                convectiveSigmetNumber = convectiveSigmetNumberDict[self._convectiveSigmetDomain]
                convectiveSigmetNumber += 1
            else:
                convectiveSigmetNumber = 1
        else:
            convectiveSigmetNumber = 1
            
        convectiveSigmetNumber = str(convectiveSigmetNumber)
        
        return convectiveSigmetNumber
    
    def _applyCount(self, convectiveSigmetNumberStr, countEast, countCentral, countWest):
        if self._convectiveSigmetDomain == 'East':    
            convectiveSigmetNumberStr = str(int(convectiveSigmetNumberStr) + countEast)
        elif self._convectiveSigmetDomain == 'Central':
            convectiveSigmetNumberStr = str(int(convectiveSigmetNumberStr) + countCentral)
        elif self._convectiveSigmetDomain == 'West':
            convectiveSigmetNumberStr = str(int(convectiveSigmetNumberStr) + countWest)
            
        return convectiveSigmetNumberStr                                
    
    def _addCount(self, countEast, countCentral, countWest):
        if self._convectiveSigmetDomain == 'East':
            countEast = countEast + 1
        elif self._convectiveSigmetDomain == 'Central':
            countCentral = countCentral + 1
        elif self._convectiveSigmetDomain == 'West':
            countWest = countWest + 1
            
        return countEast, countCentral, countWest
    
    def _boundingStatement(self, hazardEvent):
        
        boundingStatement = hazardEvent.getHazardAttributes().get('boundingStatement')
        
        return boundingStatement
            
    def _states(self, hazardEvent):
        self._hazardZonesDict = hazardEvent.getHazardAttributes().get('hazardArea')
                   
        statesList = []
        for key in self._hazardZonesDict:
            states = key[:2]
            if states in statesList:
                pass
            else:
                statesList.append(states)
        statesListStr = ''
        for states in statesList:
            statesListStr += states + ' '
            
        return statesListStr
    
    def _domain(self, hazardEvent):        
        self._convectiveSigmetDomain = hazardEvent.getHazardAttributes().get('convectiveSigmetDomain')
        hazardEvent.set('convectiveSigmetDomain', self._convectiveSigmetDomain) 
        convectiveSigmetDomainDict = {'East': 'E', 'Central': 'C', 'West': 'W'}
        
        domainStr = convectiveSigmetDomainDict[self._convectiveSigmetDomain]
                
        return domainStr
    
    def _mode(self, hazardEvent):
        convectiveSigmetModeDict = {'Polygon': 'AREA', 'LineString': 'LINE', 'Point': 'ISOL'}
        modeStr = convectiveSigmetModeDict[self._geomType]
        modeStr += ' TS'
                
        return modeStr
    
    def _modifier(self, hazardEvent):
        self._convectiveSigmetModifier = hazardEvent.getHazardAttributes().get('convectiveSigmetModifier')
        convectiveSigmetModifierDict = {'Developing': 'DVLPG ', 'Intensifying': 'INTSF ', 'Diminishing': 'DMSHG ', 'None': ''}
        modifierStr = convectiveSigmetModifierDict[self._convectiveSigmetModifier]
                
        return modifierStr
    
    def _embedded(self, hazardEvent):
        self._convectiveSigmetWidth = str(hazardEvent.getHazardAttributes().get('convectiveSigmetWidth'))
        self._convectiveSigmetEmbeddedSvr = hazardEvent.getHazardAttributes().get('convectiveSigmetEmbeddedSvr')
        
        hazardEmbeddedDict = {'Severe': 'SEV', 'Embedded': ' EMBD'}
        embeddedStr = ""
        
        if self._geomType == "LineString":
            for selection in self._convectiveSigmetEmbeddedSvr:
                embeddedStr += hazardEmbeddedDict[selection]               
            embeddedStr = embeddedStr + ' ' + self._convectiveSigmetWidth + ' NM WIDE '
        elif self._geomType == "Polygon":
            for selection in self._convectiveSigmetEmbeddedSvr:
                embeddedStr += hazardEmbeddedDict[selection]
        elif self._geomType == "Point":
            for selection in self._convectiveSigmetEmbeddedSvr:
                embeddedStr += hazardEmbeddedDict[selection]
            embeddedStr = embeddedStr + ' ' + self._convectiveSigmetWidth + ' NM WIDE '
                    
        return embeddedStr
    
    def _motion(self, hazardEvent):
        self._convectiveSigmetDirection = hazardEvent.getHazardAttributes().get('convectiveSigmetDirection')
        self._convectiveSigmetSpeed = hazardEvent.getHazardAttributes().get('convectiveSigmetSpeed')
        
        if self._convectiveSigmetDirection < 10:
            self._convectiveSigmetDirectionStr = '00' + str(self._convectiveSigmetDirection)
        elif self._convectiveSigmetDirection >= 10 and self._convectiveSigmetDirection < 100:
            self._convectiveSigmetDirectionStr = '0' + str(self._convectiveSigmetDirection)
        else:
            self._convectiveSigmetDirectionStr = str(self._convectiveSigmetDirection)     
        
        if self._convectiveSigmetSpeed < 10:
            self._convectiveSigmetSpeedStr = '0' + str(self._convectiveSigmetSpeed)
        else:
            self._convectiveSigmetSpeedStr = str(self._convectiveSigmetSpeed)
        
        hazardMotionStr = 'MOV FROM ' + self._convectiveSigmetDirectionStr + self._convectiveSigmetSpeedStr + 'KT.'
        
        if hazardMotionStr == 'MOV FROM 00000KT.':
            hazardMotionStr = 'MOV LTL.'
                    
        return hazardMotionStr
    
    def _cloudTop(self, hazardEvent):
        self._convectiveSigmetCloudTop = hazardEvent.getHazardAttributes().get('convectiveSigmetCloudTop')
        self._convectiveSigmetCloudTopText = hazardEvent.getHazardAttributes().get('convectiveSigmetCloudTopText')
        
        if self._convectiveSigmetCloudTop == "topsTo":
            cloudTopStr = "TOPS TO FL" + str(self._convectiveSigmetCloudTopText) + '.'
        elif self._convectiveSigmetCloudTop == "topsAbove":
            cloudTopStr = "TOPS ABV FL450."
                    
        return cloudTopStr
    
    def _additionalHazards(self, hazardEvent):
        self._convectiveSigmetTornadoes = hazardEvent.getHazardAttributes().get('tornadoesCheckBox')
        self._convectiveSigmetHailWind = hazardEvent.getHazardAttributes().get('hailWindComboBox')        
        self._convectiveSigmetHailSpinner = str(hazardEvent.getHazardAttributes().get('hailSpinner'))        
        self._convectiveSigmetWindSpinner = str(hazardEvent.getHazardAttributes().get('windSpinner'))
        
        additionalHazardsStr = ""
        if self._convectiveSigmetTornadoes == True:
            additionalHazardsStr += "TORNADOES..."
        if self._convectiveSigmetHailWind == "hailWindCanned":
            additionalHazardsStr += "HAIL TO 1 IN...WIND GUSTS TO 50KT POSS."            
        elif self._convectiveSigmetHailWind == "hailWindCustom":
            additionalHazardsStr += "HAIL TO " + self._convectiveSigmetHailSpinner + " IN..."
            additionalHazardsStr += "WIND GUSTS TO " + self._convectiveSigmetWindSpinner + "KTS POSS."
                    
        return additionalHazardsStr
    
    def _narrativeForecastInformation(self, segmentDict, productSegmentGroup, productSegment):  
        default = '''
|* 
WSUS32 KKCI 111839
MKCC WST 111839
CONVECTIVE SIGMET 5C
VALID UNTIL 112038Z
NE IA MO KS MN SD 
FROM PIR-20SSW SLN-30WSW COU-40WSW MSP-PIR
INTSF AREA SEV EMBD MOV FROM 13016KT. TOPS ABV FL450.
WIND GUSTS TO 100KTS RPRTD.
*|
         '''  
        productDict['narrativeForecastInformation'] = self._section.hazardEvent.get('narrativeForecastInformation', default)

    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, False)
        return dataList
