"""
    Description: Product Generator for the Convective SIGMET product.
"""
import os, types, sys, collections, re
from HydroProductParts import HydroProductParts
import GeometryFactory
from VisualFeatures import VisualFeatures
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
        
        self._productLoc = 'MKC'  # product name 
        self._fullStationID = 'KKCI'  # full station identifier (4letter)
        self._wmoHeaderDict = {'E': 'WSUS31', 'C': 'WSUS32', 'W': 'WSUS33'}  # WMO header based on region
        self._productIdentifier = 'WST'
        self._pilDict = {'E': 'SIGE', 'C': 'SIGC', 'W': 'SIGW'}  # Product pil
        self._areaName = 'NONE'  # Name of state, such as 'GEORGIA' -- optional
        self._wfoCityState = 'NONE'  # Location of WFO - city state
        self._zczc = 'ZCZC'
        self._all = 'ALL'
        self._textdbPil = 'ANCFASIGAK1'  # Product ID for storing to AWIPS text database.
        self._awipsWANPil = 'PANCSIGAK1'  # Product ID for transmitting to AWIPS WAN.
        self._lineLength = 68  # line length


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
        if dialogInputMap:
            self._storeDialogInputMap(dialogInputMap)
        else:
            self._dialogInputMap = {}
           
        self._specialTime = eventSet.getAttributes().get("currentTime")/1000
        
        countEast = 0
        countCentral = 0
        countWest = 0 
          
        for event in self._inputHazardEvents:
            print 'Convective_SIGMET_ProductGenerator.py Execute \n'
            print event
            print '\tType: ', type(event)
            print '\tAttrs:', event.getHazardAttributes()
            print '\tPhen:', event.getPhenomenon(), event.getSubType()
            self._hazGeometry = event.getGeometry()
            print '\tGeom:', self._hazGeometry
            for g in self._hazGeometry.geoms:
                print shapely.geometry.base.dump_coords(g)
                
            self._setVariables(event)
                 
            self._convectiveSigmetSpecialIssuance = event.getHazardAttributes().get('convectiveSigmetSpecialIssuance')
            event.set('specialIssuance', self._convectiveSigmetSpecialIssuance) 

            self._determineTimeRanges(event)            
            self._convectiveSigmetDomain = event.getHazardAttributes().get('convectiveSigmetDomain')           
            self._boundingStatement = event.getHazardAttributes().get('boundingStatement')
            
            if event.getStatus() in ["PENDING"]:
                if metaDict.get('issueFlag') == "True":
                    self._convectiveSigmetNumberStr = self._setConvectiveSigmetNumber(event)
                else:
                    self._convectiveSigmetNumberStr = self._getConvectiveSigmetNumber(event)
                    self._convectiveSigmetNumberStr = self._applyCount(self._convectiveSigmetNumberStr, countEast, countCentral, countWest)                                                        
            elif event.getStatus() in ["ISSUED"]:
                self._convectiveSigmetNumberStr = event.get('convectiveSigmetNumberStr')
            else:
                self._convectiveSigmetNumberStr = self._getConvectiveSigmetNumber(event)            
            
            if event.getStatus() in ["PENDING"]:
                if metaDict.get('issueFlag') is not "True":
                    countEast, countCentral, countWest = self._addCount(countEast, countCentral, countWest)

            selectedVisualFeatures = []
            if event.getStatus() in ["PENDING", "ISSUED"]:
                event.set('convectiveSigmetNumberStr', self._convectiveSigmetNumberStr)
                event.set('validTime', self._initTimeZ)    
                self._formatConvectiveSigmet(event)           
            
            event.set('convectiveSigmetDomain', self._convectiveSigmetDomain)
        
            self._fcst = self._preProcessProduct(event, '', {})        
            self._fcst = str.upper(self._fcst)            
            self.flush()
            
        productDict = self._outputFormatter(eventSet)
        self._outputText(productDict)

        return [productDict], self._inputHazardEvents
    
    def _setVariables(self, hazardEvent):
        attrs = hazardEvent.getHazardAttributes()
        phen = hazardEvent.getPhenomenon()
        sig = hazardEvent.getSignificance()

        self._hazardZonesDict = attrs.get('hazardArea')
        self._hazardHeadline = attrs.get('headline')
        self._hazardAdvisoryType = attrs.get('AAWUAdvisoryType')
        self._convectiveSigmetMode = attrs.get('convectiveSigmetMode')
        self._convectiveSigmetEmbeddedLine = attrs.get('convectiveSigmetEmbeddedLine')
        self._convectiveSigmetEmbeddedArea = attrs.get('convectiveSigmetEmbeddedArea')
        self._convectiveSigmetEmbeddedIsolated = attrs.get('convectiveSigmetEmbeddedIsolated')
        self._convectiveSigmetModifier = attrs.get('convectiveSigmetModifier')
        self._convectiveSigmetDirection = attrs.get('convectiveSigmetDirection')
        self._convectiveSigmetSpeed = attrs.get('convectiveSigmetSpeed')
        self._convectiveSigmetLineWidth = str(attrs.get('convectiveSigmetLineWidth'))
        self._convectiveSigmetCellDiameter = str(attrs.get('convectiveSigmetCellDiameter'))                
        self._convectiveSigmetCloudTop = attrs.get('convectiveSigmetCloudTop')
        self._convectiveSigmetCloudTopText = attrs.get('convectiveSigmetCloudTopText')
        self._convectiveSigmetTornadoes = attrs.get('tornadoesCheckBox')
        self._convectiveSigmetHailWind = attrs.get('hailWindComboBox')        
        self._convectiveSigmetHailSpinner = str(attrs.get('hailSpinner'))        
        self._convectiveSigmetWindSpinner = str(attrs.get('windSpinner'))
        
        return        
    
    def _determineTimeRanges(self, hazEvt):
        self._advisoryName = str.upper(hazEvt.getHazardAttributes().get('AAWUAdvisoryType'))
        
        self._cancelV = False
        if re.search('CANCELLED', self._advisoryName):
            self._cancelV = True        
    
        self._currentTime = time.mktime(hazEvt.getCreationTime().timetuple())
        
        epochStartTime = time.mktime(hazEvt.getStartTime().timetuple())
        epochEndTime = time.mktime(hazEvt.getEndTime().timetuple())
        
        self._ddhhmmTime = time.strftime('%d%H%M', time.gmtime(
            self._currentTime))
                
        if self._cancelV:
            self._initTimeZ = time.strftime('%d%H%M', \
                time.gmtime(self._currentTime + 5 * 60))
            self._endTimeZ = time.strftime('%d%H%M', \
                time.gmtime(self._currentTime + 20 * 60))
            self._initDateZ = time.strftime('%Y%m%d_%H%M', \
                time.gmtime(self._currentTime + 5 * 60))
            self._endDateZ = time.strftime('%Y%m%d_%H%M', \
                time.gmtime(self._currentTime + 20 * 60))            
        elif self._convectiveSigmetSpecialIssuance == True:
            self._initTimeZ = time.strftime('%d%H%M', \
                time.gmtime(epochStartTime))
            self._specialTime = time.strftime('%d%H%M', \
                time.gmtime(self._specialTime))
            self._endTimeZ = time.strftime('%d%H%M', time.gmtime(epochEndTime))
            self._initDateZ = time.strftime('%Y%m%d_%H%M', \
                time.gmtime(epochStartTime))
            self._endDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochEndTime))            
        else:
            self._initTimeZ = time.strftime('%d%H%M', time.gmtime(epochStartTime))
            self._endTimeZ = time.strftime('%d%H%M', time.gmtime(epochEndTime))
            self._initDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochStartTime))
            self._endDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochEndTime))            

        return
    
    def _setConvectiveSigmetNumber(self, hazardEvent):
        import json
        
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
    
    def _formatConvectiveSigmet(self, hazardEvent):

        self._getStatesStr(hazardEvent)
        self._getDomainStr(hazardEvent)
        self._getModeStr(hazardEvent)
        self._getModifierStr(hazardEvent)
        self._getEmbeddedStr(hazardEvent)
        self._getMotionStr(hazardEvent)
        self._getCloudTopStr(hazardEvent)
        self._getAdditionalHazardsStr(hazardEvent)
            
    def _getStatesStr(self, hazardEvent):           
        statesList = []
        for key in self._hazardZonesDict:
            states = key[:2]
            if states in statesList:
                pass
            else:
                statesList.append(states)
        self._statesListStr = ''
        for states in statesList:
            self._statesListStr += states + ' '
            
        return
    
    def _getDomainStr(self, hazardEvent):
        convectiveSigmetDomainDict = {'East': 'E', 'Central': 'C', 'West': 'W'}
        self._convectiveSigmetDomainStr = convectiveSigmetDomainDict[self._convectiveSigmetDomain]
                
        return
    
    def _getModeStr(self, hazardEvent):
        convectiveSigmetModeDict = {'area': 'AREA', 'isolated': 'ISOL', 'line': 'LINE'}
        self._convectiveSigmetModeStr = convectiveSigmetModeDict[self._convectiveSigmetMode]
        self._convectiveSigmetModeStr += ' TS'
                
        return
    
    def _getModifierStr(self, hazardEvent):
        convectiveSigmetModifierDict = {'Developing': 'DVLPG', 'Intensifying': 'INTSF', 'Diminishing': 'DMSHG', 'None': ''}
        self._convectiveSigmetModifierStr = convectiveSigmetModifierDict[self._convectiveSigmetModifier]
                
        return
    
    def _getEmbeddedStr(self, hazardEvent):
        hazardEmbeddedDict = {'Severe': 'SEV', 'Embedded': ' EMBD'}
        self._hazardEmbeddedStr = ""
        if self._convectiveSigmetMode == "line":
            for selection in self._convectiveSigmetEmbeddedLine:
                self._hazardEmbeddedStr += hazardEmbeddedDict[selection]               
            self._hazardEmbeddedStr = self._hazardEmbeddedStr + ' ' + self._convectiveSigmetLineWidth + ' NM WIDE'
        elif self._convectiveSigmetMode == "area":
            for selection in self._convectiveSigmetEmbeddedArea:
                self._hazardEmbeddedStr += hazardEmbeddedDict[selection]
        elif self._convectiveSigmetMode == "isolated":
            for selection in self._convectiveSigmetEmbeddedIsolated:
                self._hazardEmbeddedStr += hazardEmbeddedDict[selection]
            self._hazardEmbeddedStr = self._hazardEmbeddedStr + ' ' + self._convectiveSigmetCellDiameter + ' NM WIDE'
                    
        return
    
    def _getMotionStr(self, hazardEvent):
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
        
        self._hazardMotionStr = 'MOV FROM ' + self._convectiveSigmetDirectionStr + self._convectiveSigmetSpeedStr + 'KT.'
        
        if self._hazardMotionStr == 'MOV FROM 00000KT.':
            self._hazardMotionStr = 'MOV LTL.'
                    
        return
    
    def _getCloudTopStr(self, hazardEvent):
        if self._convectiveSigmetCloudTop == "topsTo":
            self._convectiveSigmetCloudTopStr = "TOPS TO FL" + str(self._convectiveSigmetCloudTopText) + '.'
        elif self._convectiveSigmetCloudTop == "topsAbove":
            self._convectiveSigmetCloudTopStr = "TOPS ABV FL450."
                    
        return
    
    def _getAdditionalHazardsStr(self, hazardEvent):
        self._convectiveSigmetAdditionalHazardsStr = ""
        if self._convectiveSigmetTornadoes == True:
            self._convectiveSigmetAdditionalHazardsStr += "TORNADOES..."
        if self._convectiveSigmetHailWind == "hailWindCanned":
            self._convectiveSigmetAdditionalHazardsStr += "HAIL TO 1 IN...WIND GUSTS TO 50KT POSS."            
        elif self._convectiveSigmetHailWind == "hailWindCustom":
            self._convectiveSigmetAdditionalHazardsStr += "HAIL TO " + self._convectiveSigmetHailSpinner + " IN..."
            self._convectiveSigmetAdditionalHazardsStr += "WIND GUSTS TO " + self._convectiveSigmetWindSpinner + "KTS POSS."
                    
        return
    
    def _outputFormatter(self, eventSet):        
        eastFcstDict, eastFcstSpecialDict, centralFcstDict, centralFcstSpecialDict, westFcstDict, westFcstSpecialDict \
        = self._createFcstDict(eventSet)
        
        eastFcstList, centralFcstList, westFcstList, eastFcstSpecialList, centralFcstSpecialList, westFcstSpecialList \
        = self._createFcstList(eastFcstDict,eastFcstSpecialDict,centralFcstDict,centralFcstSpecialDict,westFcstDict, \
                               westFcstSpecialDict)                       
        
        headerEast, headerCentral, headerWest = self._createHeader(eastFcstSpecialList, centralFcstSpecialList, westFcstSpecialList)
        
        eastFcst, centralFcst, westFcst = self._createFcst(eastFcstList, centralFcstList, westFcstList, eastFcstSpecialList, \
                                                           centralFcstSpecialList, westFcstSpecialList)                        
        
        fcst = headerEast + eastFcst + 'NNNN' + '\n\n'
        fcst = fcst + headerCentral + centralFcst + 'NNNN' + '\n\n'
        fcst = fcst + headerWest + westFcst + '\n' 
        
        productDict = collections.OrderedDict()        
        productDict['productID'] = 'SIGMET.Convective'
        productDict['productName'] = 'CONVECTIVE SIGMET'        
        productDict['text'] = fcst            
            
        return productDict
    
    def _createFcstDict(self, eventSet):
        eastFcstDict = {}
        centralFcstDict = {}
        westFcstDict = {}
        eastFcstSpecialDict = {}
        centralFcstSpecialDict = {}
        westFcstSpecialDict = {}
   
        for event in eventSet:
            fcst = event.get('formattedText')
            domain = event.get('convectiveSigmetDomain')
            specialIssuance = event.get('specialIssuance')
            numberStr = event.get('convectiveSigmetNumberStr')
            number = int(numberStr[:1])
            
            if domain == 'East':
                if specialIssuance == True:
                    eastFcstSpecialDict[number] = fcst
                else:
                    eastFcstDict[number] = fcst
            elif domain == 'Central':
                if specialIssuance == True:
                    centralFcstSpecialDict[number] = fcst
                else:
                    centralFcstDict[number] = fcst                    
            elif domain == 'West':
                if specialIssuance == True:
                    westFcstSpecialDict[number] = fcst                    
                else:
                    westFcstDict[number] = fcst
                    
        eastFcstDict = collections.OrderedDict(sorted(eastFcstDict.items()))
        centralFcstDict = collections.OrderedDict(sorted(centralFcstDict.items()))
        westFcstDict = collections.OrderedDict(sorted(westFcstDict.items()))
        eastFcstSpecialDict = collections.OrderedDict(sorted(eastFcstSpecialDict.items()))
        centralFcstSpecialDict = collections.OrderedDict(sorted(centralFcstSpecialDict.items()))
        westFcstSpecialDict = collections.OrderedDict(sorted(westFcstSpecialDict.items()))
                                                      
        return eastFcstDict, eastFcstSpecialDict, centralFcstDict, centralFcstSpecialDict, westFcstDict, westFcstSpecialDict
    
    def _createFcstList(self,eastFcstDict,eastFcstSpecialDict,centralFcstDict,centralFcstSpecialDict,westFcstDict,westFcstSpecialDict):
        eastFcstList = []
        centralFcstList = []
        westFcstList = []
        eastFcstSpecialList = []
        centralFcstSpecialList = []
        westFcstSpecialList = []
        
        for value in eastFcstDict.iteritems():
            eastFcstList.append(value[1])
        for value in centralFcstDict.iteritems():
            centralFcstList.append(value[1])        
        for value in westFcstDict.iteritems():
            westFcstList.append(value[1])
        for value in eastFcstSpecialDict.iteritems():
            eastFcstSpecialList.append(value[1])
        for value in centralFcstSpecialDict.iteritems():
            centralFcstSpecialList.append(value[1])
        for value in westFcstSpecialDict.iteritems():
            westFcstSpecialList.append(value[1])                        
                    
        return eastFcstList, centralFcstList, westFcstList, eastFcstSpecialList, centralFcstSpecialList, westFcstSpecialList
    
    def _createFcst(self,eastFcstList, centralFcstList, westFcstList, eastFcstSpecialList, centralFcstSpecialList, westFcstSpecialList):
        eastFcst = ''
        centralFcst = ''
        westFcst = ''
        
        if eastFcstList:
            for entry in eastFcstList:
                eastFcst = eastFcst + entry + '\n\n'
            if eastFcstSpecialList:
                for entry in eastFcstSpecialList:
                    eastFcst = entry + '\n\n' + eastFcst
        else:
            if eastFcstSpecialList:
                for entry in eastFcstSpecialList:
                    eastFcst = eastFcst + entry + '\n\n'
            else:
                eastFcst = 'CONVECTIVE SIGMET...NONE\n\n'
                
        if centralFcstList:
            for entry in centralFcstList:
                centralFcst = centralFcst + entry + '\n\n'
            if centralFcstSpecialList:
                for entry in centralFcstSpecialList:
                    centralFcst = entry + '\n\n' + centralFcst                
        else:
            if centralFcstSpecialList:
                for entry in centralFcstSpecialList:
                    centralFcst = centralFcst + entry + '\n\n'
            else:
                centralFcst = 'CONVECTIVE SIGMET...NONE\n\n'
            
        if westFcstList:
            for entry in westFcstList:
                westFcst = westFcst + entry + '\n\n'
            if westFcstSpecialList:
                for entry in westFcstSpecialList:
                    westFcst = entry + '\n\n' + westFcst                 
        else:
            if westFcstSpecialList:
                for entry in westFcstSpecialList:
                    westFcst = westFcst + entry + '\n\n'
            else:
                westFcst = 'CONVECTIVE SIGMET...NONE\n\n'        
        
        return eastFcst, centralFcst, westFcst         
    
    def _createHeader(self, eastFcstSpecialList, centralFcstSpecialList, westFcstSpecialList):
        
        fcstSpecialListDict = {'east': eastFcstSpecialList, 'central': centralFcstSpecialList, 'west': westFcstSpecialList}
        domainDict = {'east': 'E', 'central': 'C', 'west': 'W'}
        headerList = []
        
        for key in domainDict:
            print "key: ", key
            header = ''
            if len(fcstSpecialListDict[key]):
                header = '%s %s%s %s %s\n' % (self._zczc, self._productLoc, self._pilDict[domainDict[key]],
                    self._all, self._specialTime)            
                header = '%s%s %s %s\n' % (header, self._wmoHeaderDict[domainDict[key]], self._fullStationID,
                    self._specialTime)
                header = '%s%s%s %s %s' % (header, self._productLoc, domainDict[key],
                    self._productIdentifier, self._specialTime)
            else:
                header = '%s %s%s %s %s\n' % (self._zczc, self._productLoc, self._pilDict[domainDict[key]],
                    self._all, self._initTimeZ)
                header = '%s%s %s %s\n' % (header, self._wmoHeaderDict[domainDict[key]], self._fullStationID,
                    self._initTimeZ)
                header = '%s%s%s %s %s' % (header, self._productLoc, domainDict[key],
                    self._productIdentifier, self._initTimeZ)
            headerList.append(header)
        
        for header in headerList:
            if header[11] == 'E':
                headerEast = header
            elif header[11] == 'C':
                headerCentral = header
            else:
                headerWest = header
     
        return headerEast, headerCentral, headerWest           
    
    def _outputText(self, productDict):              
        outDirAll = os.path.join(OUTPUTDIR, self._initDateZ)
        outAllAdvisories = 'convectiveSIGMET_' + self._initDateZ + '.txt'
        pathAllFile = os.path.join(outDirAll, outAllAdvisories)
        
        if not os.path.exists(outDirAll):
            try:
                os.makedirs(outDirAll)
            except:
                sys.stderr.write('Could not create output directory')

        with open(pathAllFile, 'w') as outFile:
            outFile.write(productDict['text'])
    
        return
            
    def _preProcessProduct(self, event, fcst, argDict):
        fcst = '%s%s %s%s\n' % (fcst, self._SIGMET_ProductName, self._convectiveSigmetNumberStr, self._convectiveSigmetDomainStr)        
        fcst = fcst + 'VALID UNTIL ' + self._endTimeZ + 'Z\n'
        fcst = '%s%s\n' % (fcst, self._statesListStr)
        fcst = '%s%s\n' % (fcst, self._boundingStatement)
        fcst = '%s%s %s %s %s %s' % (fcst, self._convectiveSigmetModifierStr, self._convectiveSigmetModeStr, self._hazardEmbeddedStr,
            self._hazardMotionStr, self._convectiveSigmetCloudTopStr)
        
        if len(self._convectiveSigmetAdditionalHazardsStr):
            fcst = '\n%s%s' % (fcst, self._convectiveSigmetAdditionalHazardsStr)
        
        fcst = fcst.replace("_", " ")

        print 'Convective_SIGMET_ProductGenerator.py preProcessProduct -- fcst:', fcst        
        event.set('formattedText', fcst)
        
        return fcst

    def _postProcessProduct(self, fcst, argDict):
        endTimeStr = time.strftime('%b %Y', time.gmtime(
            self._currentTime))
      
        fcst = fcst + '\n' + 'OUTLOOK VALID ' + 'DDHHMM - DDHHMM+4\n' 
        fcst = fcst + 'TS ARE NOT EXPD TO REQUIRE WST ISSUANCES.\n'
        fcst = fcst + '\n' + 'NNNN'

        return fcst

    def _wordWrap(self, string, width=66):
        newstring = ''
        if len(string) > width:
            while True:
                # find position of nearest whitespace char to the left of 'width'
                marker = width - 1
                while not string[marker].isspace():
                    marker = marker - 1

                # remove line from original string and add it to the new string
                newline = string[0:marker] + '\n'
                newstring = newstring + newline
                string = string[marker + 1:]

                # break out of loop when finished
                if len(string) <= width:
                    break
    
        return newstring + string
                
    def _groupSegments(self, segments):
        '''
        Group the segments into the products
        
         ESF products are not segmented, so make a product from each 'segment' i.e. HY.O event
        '''        
        productSegmentGroups = []
        for segment in segments:
            vtecRecords = self.getVtecRecords(segment)
            productSegmentGroups.append(self.createProductSegmentGroup('ESF', self._ESF_ProductName, 'area', self._vtecEngine, 'counties', False,
                                            [self.createProductSegment(segment, vtecRecords)]))            
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        return productSegmentGroups
    
    def _addProductParts(self, productSegmentGroup):
        productSegments = productSegmentGroup.productSegments
        productSegmentGroup.setProductParts(self._hydroProductParts._productParts_ESF(productSegments))

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
