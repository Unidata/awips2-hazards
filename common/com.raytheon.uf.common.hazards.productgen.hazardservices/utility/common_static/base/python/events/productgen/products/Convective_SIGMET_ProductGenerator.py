"""
    Description: Product Generator for the Convective SIGMET product.
"""
import os, types, sys, collections, re
import math
from HydroProductParts import HydroProductParts 
import GeometryFactory
import HydroGenerator
from KeyInfo import KeyInfo
import shapely, time, datetime, TimeUtils
import HazardDataAccess
import MetaData_Convective_SIGMET
import Domains
import AviationUtils
from VisualFeatures import VisualFeatures
from com.raytheon.uf.common.time import SimulatedTime

OUTPUTDIR = '/scratch/convectiveSigmetTesting'
######
TABLEFILE = '/home/nathan.hardin/Desktop/snap.tbl'

class Product(HydroGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()  
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'Convective_SIGMET_ProductGenerator'
        self._productID = 'SIGMET.Convective'

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

        productDict:  {'eventDicts': [{'eventID': 'HZ-2015-OAX-001591', 'geomType': 'Polygon', 'domain': 'Central',
                       'specialIssuance': 0, 'sigmetNumber': '10', 'issueFlag': 'False',
                       'parts': OrderedDict([('currentTime', 1430067612.0), ('startTime', '261755'), ('endTime', '261955'),
                       ('specialTime', None), ('startDate', '20150426_1755'), ('endDate', '20150426_1955'),
                       ('states', 'ND SD '), ('boundingStatement', 'FROM 30ENE_BIS-30N_PIR-40SSE_ABR-60E_FAR-30ENE_BIS'),
                       ('mode', 'AREA TS'), ('modifier', ''), ('embedded', ''), ('motion', 'MOV LTL.'),
                       ('cloudTop', 'TOPS ABV FL450.'), ('additionalHazards', ''), ('geometry',
                       [(-101.33362229363624, 47.062326102362164), (-100.19353547445509, 44.923106228413104),
                       (-98.59381085554291, 44.818574400475214), (-98.36687654436633, 46.85652864219297),
                       (-101.33362229363624, 47.062326102362164)])]), 'status': 'PENDING'}],
                       'productName': 'CONVECTIVE SIGMET',
                       'productParts': ['currentTime', 'startTime', 'endTime', 'specialTime', 'startDate', 'endDate',
                       'states', 'boundingStatement', 'mode', 'modifier', 'embedded', 'motion', 'cloudTop', 'additionalHazards',
                       'geometry'], 'productID': 'SIGMET.Convective'}
        '''
        self._initialize() 
        self.logger.info("Start ProductGeneratorTemplate:execute ESF")
        
        # Extract information for execution
        self._inputHazardEvents = eventSet.getEvents()
        eventSetAttrs = eventSet.getAttributes()
        metaDict = eventSet.getAttributes()
        self._issueFlag = metaDict.get('issueFlag')
        
        # #raise exception error if trying to issue before HH:40
        # millis = SimulatedTime.getSystemTime().getMillis()
        # currentTime = datetime.datetime.utcfromtimestamp(millis / 1000)
        # if currentTime.minute < 40 and self._issueFlag:
        #    raise ValueError('You cannot issue products before HH:40. Wait until 40 minutes after the hour to issue.')
        
        if dialogInputMap:
            self._storeDialogInputMap(dialogInputMap)
        else:
            self._dialogInputMap = {}
            
        self._issueTime = long(metaDict.get("currentTime"))
        self._validTime = eventSet.getAttributes().get("currentTime") / 1000
        
        domains = Domains.AviationDomains
        for domain in domains:
            domain.setCount(0)
        
        parts = ['currentTime', 'startTime', 'endTime', 'specialTime', 'startDate', 'endDate', 'states',
                 'boundingStatement', 'mode', 'modifier', 'embedded', 'motion', 'cloudTop', 'additionalHazards', 'geometry']
        
        productDict = {}
        productDict['productParts'] = parts
        eventDicts = [] 
        
        # Find the latest startTime for the event
        self._latestStartTime = None
        for event in self._inputHazardEvents:
            if self._latestStartTime is None:
                if event.get('convectiveSigmetSpecialIssuance') == True:
                    pass
                else:
                    self._latestStartTime = event.getStartTime()
            else:
                if event.get('convectiveSigmetSpecialIssuance') == True:
                    pass
                else:
                    startTime = event.getStartTime()
                    if startTime > self._latestStartTime:
                        self._latestStartTime = startTime
                          
        for event in self._inputHazardEvents:
            event.set('issueTime', self._issueTime)
            self._originalGeomType = event.get('originalGeomType')
            if not self._eventValid(event, self._latestStartTime):
                continue
            self._geomType = AviationUtils.AviationUtils().getGeometryType(event)
            self._convectiveSigmetSpecialIssuance = event.get('convectiveSigmetSpecialIssuance')
            self._convectiveSigmetDomain = event.get('convectiveSigmetDomain')
            
            dict = {}
            dict['eventID'] = event.getEventID()
            dict['geomType'] = self._geomType
            dict['sigmetNumber'] = self._sigmetNumber(event, domains)
            dict['domain'] = event.get('convectiveSigmetDomain')
            dict['specialIssuance'] = event.get('convectiveSigmetSpecialIssuance')
            dict['issueFlag'] = self._issueFlag
            dict['status'] = event.getStatus()
            
            partDict = collections.OrderedDict()
            for partName in parts:
                exec "partStr = self._" + partName + "(event)"
                partDict[partName] = partStr
            
            dict['parts'] = partDict
            eventDicts.append(dict)
            
        for event in eventSet:
            self._adjustForVisualFeatureChange(event, eventSetAttrs)
        
        productDict['eventDicts'] = eventDicts
        productDict['productID'] = 'SIGMET.Convective'
        productDict['productName'] = 'CONVECTIVE SIGMET'     

        return [productDict], self._inputHazardEvents
    
    def _adjustForVisualFeatureChange(self, event, eventSetAttrs):
        self._originalGeomType = event.get('originalGeomType')
        self._width = event.getHazardAttributes().get('convectiveSigmetWidth')
        features = event.getVisualFeatures()
        if not features: features = []

        for feature in features:           
            if 'base' in feature["identifier"]:
            
                polyDict = feature["geometry"]
                for timeBounds, geometry in polyDict.iteritems():
                    featurePoly = geometry.asShapely()
                    vertices = shapely.geometry.base.dump_coords(featurePoly)
                    if any(isinstance(i, list) for i in vertices):
                        vertices = vertices[0]
                    convectiveSigmetDomain = AviationUtils.AviationUtils().selectDomain(event, vertices, self._originalGeomType, 'modification')
                    boundingStatement = AviationUtils.AviationUtils().boundingStatement(event, self._originalGeomType, TABLEFILE, vertices, 'modification')
                        
                    if self._originalGeomType != 'Polygon':
                        poly = AviationUtils.AviationUtils().createPolygon(vertices, self._width, self._originalGeomType)
                        AviationUtils.AviationUtils().updateVisualFeatures(event, vertices, poly)
                    else:
                        poly = []
                        AviationUtils.AviationUtils().updateVisualFeatures(event, vertices, poly)
    
######################START ACTUAL PRODUCT GENERATION METHODS ###################################                                                          
 
    def _eventValid(self, event, latestStartTime):
        
        if event.getStatus() in ['ELAPSED', 'ENDED', 'ENDING']:  
            event.setStatus('ELAPSED')              
            return False
        # Throw out events with startTime earlier than the latest start time
        if event.getStartTime() < latestStartTime:
            event.setStatus('ELAPSED')
            return False
        return True
    
    def _geometry(self, hazardEvent):      
        for g in hazardEvent.getFlattenedGeometry().geoms:
            geometry = shapely.geometry.base.dump_coords(g)
#         if self._geomType != 'LineString':
#             for g in hazardEvent.getFlattenedGeometry().geoms:
#                 geometry = shapely.geometry.base.dump_coords(g)
#         else:
#             geometry = hazardEvent.get('polygon')
#             geometry.pop()
        
        return geometry
    
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
        endTimeZ = time.strftime('%H%M', time.gmtime(epochEndTime))     

        return endTimeZ
    
    def _specialTime(self, hazardEvent):
        if self._convectiveSigmetSpecialIssuance == True:
            specialTime = time.strftime('%d%H%M', \
                time.gmtime(self._validTime))            
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
    
    def _sigmetNumber(self, hazardEvent, domains):
        startTime = hazardEvent.getStartTime()
        startTimeTuple = hazardEvent.getStartTime().timetuple()
        startTimeStr = str(startTimeTuple[2]) + ' ' + str(startTimeTuple[3]) + ':' + str(startTimeTuple[4])
        if hazardEvent.getStatus() in ["PENDING"]:
            if self._issueFlag == "True":
                convectiveSigmetNumberStr = self._setConvectiveSigmetNumber(hazardEvent)
            else:
                convectiveSigmetNumberStr = self._getConvectiveSigmetNumber(hazardEvent)
                convectiveSigmetNumberStr = self._applyCount(convectiveSigmetNumberStr, domains)                                                        
        elif hazardEvent.getStatus() in ["ISSUED"]:
            validTime = hazardEvent.get('validTime')
            validTime = validTime / 1000
            validTime = datetime.datetime.fromtimestamp(validTime).strftime('%d %H:%M')
            if startTimeStr[:5] == validTime[:5]:
                # if startTime and validTime are equal get the existing number          
                convectiveSigmetNumberStr = hazardEvent.get('convectiveSigmetNumberStr')
            else:
                # if startTime and validTime are not equal, iterate to the next number
                convectiveSigmetNumberStr = self._getConvectiveSigmetNumber(hazardEvent)
                if self._issueFlag == 'True':
                    convectiveSigmetNumberStr = self._setConvectiveSigmetNumber(hazardEvent)
                else:
                    convectiveSigmetNumberStr = self._applyCount(convectiveSigmetNumberStr, domains)         
        else:
            convectiveSigmetNumberStr = self._getConvectiveSigmetNumber(hazardEvent)            
            
        if hazardEvent.getStatus() in ["PENDING", "ISSUED"]:
            if hazardEvent.getStatus() == "PENDING" and (self._issueFlag is not "True"):
                self._addCount(domains)
            if hazardEvent.getStatus() == "ISSUED" and (startTimeStr[:5] != validTime[:5]):
                self._addCount(domains)
                
        hazardEvent.set('convectiveSigmetNumberStr', convectiveSigmetNumberStr)
        hazardEvent.set('validTime', time.mktime(hazardEvent.getStartTime().timetuple()) * 1000)            
                
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
                        if ('hundred' + self._convectiveSigmetDomain) in convectiveSigmetNumberDict:
                            convectiveSigmetNumberDict[('hundred' + self._convectiveSigmetDomain)] = 2
                        else:
                            convectiveSigmetNumberDict[('hundred' + self._convectiveSigmetDomain)] = 1
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
        
        if ('hundred' + self._convectiveSigmetDomain) in convectiveSigmetNumberDict:
            newSigmetNumber = str(newSigmetNumber).zfill(2)
        else:
            newSigmetNumber = str(newSigmetNumber)
               
        return newSigmetNumber
    
    def _getConvectiveSigmetNumber(self, hazardEvent):
        import json
        epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
        initTimeZ = time.strftime('%d%H%M', time.gmtime(epochStartTime))
        self._initTimeZ = initTimeZ 
        
        if os.path.isfile('/scratch/convectiveSigmetNumber.txt'):
            with open('/scratch/convectiveSigmetNumber.txt') as openFile:
                convectiveSigmetNumberDict = json.load(openFile)
                previousDate = convectiveSigmetNumberDict["Time"][:2]
                newDate = self._initTimeZ[:2]
            if previousDate != newDate:
                convectiveSigmetNumber = 1
            else:
                if self._convectiveSigmetDomain in convectiveSigmetNumberDict:
                    convectiveSigmetNumber = convectiveSigmetNumberDict[self._convectiveSigmetDomain]
                    convectiveSigmetNumber += 1
                else:
                    convectiveSigmetNumber = 1
        else:
            convectiveSigmetNumber = 1
            
        convectiveSigmetNumber = str(convectiveSigmetNumber)
        
        return convectiveSigmetNumber
    
    def _applyCount(self, convectiveSigmetNumberStr, domains): 
        for domain in domains:
            if self._convectiveSigmetDomain == domain.domainName():    
                convectiveSigmetNumberStr = str(int(convectiveSigmetNumberStr) + domain.count())            
        return convectiveSigmetNumberStr                                
    
    def _addCount(self, domains):
        for domain in domains:
            if self._convectiveSigmetDomain == domain.domainName():
                domain.incrementCount()
    
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
                
        return modeStr
    
    def _modifier(self, hazardEvent):
        self._convectiveSigmetModifier = hazardEvent.getHazardAttributes().get('convectiveSigmetModifier')
        convectiveSigmetModifierDict = {'Developing': 'DVLPG ', 'Intensifying': 'INTSF ', 'Diminishing': 'DMSHG ', 'None': ''}
        modifierStr = convectiveSigmetModifierDict[self._convectiveSigmetModifier]
                
        return modifierStr
    
    def _embedded(self, hazardEvent):
        self._convectiveSigmetWidth = str(hazardEvent.getHazardAttributes().get('convectiveSigmetWidth'))
        self._convectiveSigmetEmbeddedSvr = hazardEvent.getHazardAttributes().get('convectiveSigmetEmbeddedSvr')
        
        hazardEmbeddedDict = {'Severe': 'SEV ', 'Embedded': 'EMBD '}
        embeddedStr = ""
        
        if self._geomType == "LineString":
            for selection in self._convectiveSigmetEmbeddedSvr:
                embeddedStr += hazardEmbeddedDict[selection]               
            embeddedStr = embeddedStr + self._convectiveSigmetWidth + ' NM WIDE '
        elif self._geomType == "Polygon":
            for selection in self._convectiveSigmetEmbeddedSvr:
                embeddedStr += hazardEmbeddedDict[selection]
        elif self._geomType == "Point":
            for selection in self._convectiveSigmetEmbeddedSvr:
                embeddedStr += hazardEmbeddedDict[selection]
            self._convectiveSigmetWidth = int(self._convectiveSigmetWidth) * 2
            self._convectiveSigmetWidth = str(self._convectiveSigmetWidth)
            embeddedStr = embeddedStr + 'D' + self._convectiveSigmetWidth + ' NM '
                    
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
            if len(str(self._convectiveSigmetCloudTopText)) == 2:
                cloudTopStr = "TOPS TO FL" + "0" + str(self._convectiveSigmetCloudTopText) + '.'
            else:
                cloudTopStr = "TOPS TO FL" + str(self._convectiveSigmetCloudTopText) + '.'
        elif self._convectiveSigmetCloudTop == "topsAbove":
            cloudTopStr = "TOPS ABV FL450."
                    
        return cloudTopStr
    
    def _additionalHazards(self, hazardEvent):        
        self._additionalHazardsList = hazardEvent.getHazardAttributes().get('convectiveSigmetAdditionalHazards')
        self._hail = hazardEvent.getHazardAttributes().get('hailText')
        self._wind = hazardEvent.getHazardAttributes().get('windText')              
        
        additionalHazardsStr = ""
        if "tornadoesCheckBox" in self._additionalHazardsList:
            additionalHazardsStr += "TORNADOES..."
        if "hailCheckBox" in self._additionalHazardsList and "windCheckBox" in self._additionalHazardsList:
            additionalHazardsStr += "HAIL TO " + self._hail + " IN...WIND GUSTS TO " + self._wind + " KTS POSS."
        elif "hailCheckBox" in self._additionalHazardsList or "windCheckBox" in self._additionalHazardsList:
            if "hailCheckBox" in self._additionalHazardsList:
                additionalHazardsStr += "HAIL TO " + self._hail + " IN POSS."
            if "windCheckBox" in self._additionalHazardsList:
                additionalHazardsStr += "WIND GUSTS TO " + self._wind + " KTS POSS."
                    
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
