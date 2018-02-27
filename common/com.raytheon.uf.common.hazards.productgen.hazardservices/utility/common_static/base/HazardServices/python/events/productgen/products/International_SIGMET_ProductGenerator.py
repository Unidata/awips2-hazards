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

class Product(HydroGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()  
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'International_SIGMET_ProductGenerator'
        self._productID = 'SIGMET.International'

###################################################
        
                
    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD/Raytheon"
        metadata['description'] = "Product generator for SIGMET.International."
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
        self._productCategory = "SIGMET.International"
        self._areaName = '' 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        self._purgeHours = 8
        self._SIGMET_ProductName = 'INTERNATIONAL SIGMET'
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

        productDict:  {'eventDicts': [{'eventID': 'HZ-2015-OAX-001101', 'geomType': 'Polygon',
                       'parts': OrderedDict([('originatingOffice', 'KKCI'), ('firAbbreviation', 'KZWY'),
                       ('phenomenon', 'OBSC TS'), ('sequenceName', 'ALFA'), ('startTime', '261701'),
                       ('endTime', '262101'), ('sequenceNumber', 1), ('wmoHeader', 'WSNT01'),
                       ('awipsHeader', 'SIGA0A'), ('firName', 'NEW YORK OCEANIC FIR'),
                       ('location', 'WI N470 W10126 - N4425 W10110 - N4424 W9940 - N4642 W984 - N470 W10126.'),
                       ('forecastObserved', 'FCST'), ('verticalExtent', 'TOP FL300.'), ('movement', 'STNR.'),
                       ('intensityTrend', 'NC.'),('volcanoProductPartsDict', None), ('tropicalCycloneProductPartsDict', None),
                       ('previousStartTime', None), ('previousEndTime', None), ('cancellation', 0)]),
                       'status': 'PENDING', 'issueFlag': 'False'}], 'productName': 'INTERNATIONAL SIGMET',
                       'productParts': ['originatingOffice', 'firAbbreviation', 'phenomenon', 'sequenceName',
                       'startTime', 'endTime', 'sequenceNumber', 'wmoHeader', 'awipsHeader', 'firName', 'location',
                       'forecastObserved', 'verticalExtent', 'movement', 'intensityTrend','volcanoProductPartsDict',
                       'tropicalCycloneProductPartsDict', 'previousStartTime','previousEndTime', 'cancellation'],
                       'productID': 'SIGMET.International'}

        '''
        self._initialize() 
        self.logger.info("Start ProductGeneratorTemplate:execute ESF")
        
        # Extract information for execution
        self._inputHazardEvents = eventSet.getEvents()
        eventSetAttrs = eventSet.getAttributes()
        metaDict = eventSet.getAttributes()
        self._issueFlag = metaDict.get('issueFlag')
        
        if dialogInputMap:
            self._storeDialogInputMap(dialogInputMap)
        else:
            self._dialogInputMap = {}
        
        parts = ['originatingOffice', 'firAbbreviation', 'phenomenon', 'sequenceName', 'startTime', 'endTime',
                 'startDate', 'endDate', 'sequenceNumber', 'additionalRemarks', 
                 'wmoHeader', 'awipsHeader', 'firName', 'location', 'forecastObserved', 'verticalExtent', 'movement',
                 'intensityTrend', 'volcanoProductPartsDict', 'tropicalCycloneProductPartsDict',
                 'previousStartTime', 'previousEndTime',  'cancellation', 'geometry']
        
        productDict = {}
        productDict['productParts'] = parts
        eventDicts = []
        
        caveMode = eventSet.getAttributes().get('hazardMode','PRACTICE').upper()
        self.practice = (False if caveMode == 'OPERATIONAL' else True) 
                          
        for event in self._inputHazardEvents:
            self._geomType = event.get('originalGeomType')
            
            dict = {}
            dict['eventID'] = event.getEventID()
            dict['geomType'] = self._geomType
            dict['issueFlag'] = self._issueFlag
            dict['status'] = event.getStatus()
            
            partDict = collections.OrderedDict()
            for partName in parts:
                exec "partStr = self." + partName + "(event)"
                partDict[partName] = partStr
            
            dict['parts'] = partDict
            eventDicts.append(dict)
        
        productDict['eventDicts'] = eventDicts
        productDict['productID'] = 'SIGMET.International'
        productDict['productName'] = 'INTERNATIONAL SIGMET'  

        return [productDict], self._inputHazardEvents
    
######################START ACTUAL PRODUCT GENERATION METHODS ################################### 
    def originatingOffice(self, hazardEvent):
        originatingOffice = hazardEvent.get('internationalSigmetOffice')
        self.originatingOffice = originatingOffice
        return originatingOffice
     
    def firAbbreviation(self, hazardEvent):
        if self.originatingOffice == 'PAWU':
            firAbbreviation = 'PAZA'
        elif self.originatingOffice == 'PHFO':
            firAbbreviation = 'KZAK'
        else:
            firAbbreviation = hazardEvent.get("internationalSigmetFIR")
        self.firAbbreviation = firAbbreviation
        return firAbbreviation
    
    def firName(self, hazardEvent):
        firNameDict = {'PAZA': 'ANCHORAGE FIR', 'KZAK': 'OAKLAND OCEANIC FIR',
                       'KZWY': 'NEW YORK OCEANIC FIR', 'KZMA': 'MIAMI OCEANIC FIR',
                       'TZJS': 'SAN JUAN OCEANIC FIR', 'KZHU': 'HOUSTON OCEANIC FIR'}
        firName = firNameDict[self.firAbbreviation]
        return firName    
    
    def sequenceName(self, hazardEvent):
        sequenceName = hazardEvent.get('internationalSigmetSequence')
        self.sequenceName = sequenceName
        return sequenceName
    
    def sequenceNumber(self, hazardEvent):        
        import GenericRegistryObjectDataAccess
        import time
        
        startTime = str(hazardEvent.getStartTime())
        endTime = str(hazardEvent.getEndTime())
        
        eventStartTimeTuple = time.strptime(startTime, '%Y-%m-%d %H:%M:%S')
        eventEndTimeTuple = time.strptime(endTime, '%Y-%m-%d %H:%M:%S')
        
        self.eventStartTimeEpoch = int(time.mktime(eventStartTimeTuple))
        self.eventEndTimeEpoch = int(time.mktime(eventEndTimeTuple))
        
        #first call database to see if entry exists
        objectDicts = GenericRegistryObjectDataAccess.queryObjects(
            [("objectType", "InternationalSIGMET"),
             ("uniqueID", self.sequenceName)],
            self.practice)       
        #if entry already exists in database
        if objectDicts:
            #need check to see if it's been more than 24 hours since last issuance for given series name
            cancellation = objectDicts[0]['cancellation']
            previousStartTime = objectDicts[0]['startTime']
            currentStartTime = int(time.time())
            if cancellation == 'yes' and (currentStartTime - previousStartTime) < 86400:
                sequenceNumber = None
            else:
                sequenceNumber = objectDicts[0]['sequenceNumber'] + 1
                if self._issueFlag == 'True':
                    objectDict = {'objectType': 'InternationalSIGMET', 'uniqueID': self.sequenceName, 'sequenceNumber': sequenceNumber, 'startTime': int(time.time()),
                                  'cancellation': 'no', 'eventStartTime': self.eventStartTimeEpoch, 'eventEndTime': self.eventEndTimeEpoch}
                    GenericRegistryObjectDataAccess.storeObject(objectDict,self.practice)                
        #if entry does not exist in database
        else:
            sequenceNumber = 1
            if self._issueFlag == 'True':
                objectDict = {'objectType': 'InternationalSIGMET', 'uniqueID': self.sequenceName, 'sequenceNumber': sequenceNumber, 'startTime': int(time.time()),
                              'cancellation': 'no', 'eventStartTime': self.eventStartTimeEpoch, 'eventEndTime': self.eventEndTimeEpoch}
                GenericRegistryObjectDataAccess.storeObject(objectDict,self.practice)
            
        return sequenceNumber
    
    def additionalRemarks(self, hazardEvent):
        return hazardEvent.get('internationalSigmetAdditionalRemarks')    
    
    def cancellation(self, hazardEvent):
        import GenericRegistryObjectDataAccess
        import time
        
        cancellation = hazardEvent.get('internationalSigmetCancellation')
        if cancellation == "None":
            cancellation = False
        else:
            cancellation = True
            
        if (cancellation is True) and (self._issueFlag == "True"):
            hazardEvent.setStatus('ELAPSED')
            objectDict = {'objectType': 'InternationalSIGMET', 'uniqueID': self.sequenceName, 'sequenceNumber': 0, 'startTime': int(time.time()),
                          'cancellation': 'yes', 'eventStartTime': self.eventStartTimeEpoch, 'eventEndTime': self.eventEndTimeEpoch}
            GenericRegistryObjectDataAccess.storeObject(objectDict,self.practice)                    
        
        return cancellation    
    
    def startTime(self, hazardEvent):
        epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
        startTime = time.strftime('%d%H%M', time.gmtime(epochStartTime))
        self.startTime = startTime 
        
        return startTime
    
    def endTime(self, hazardEvent):
        epochEndTime = time.mktime(hazardEvent.getEndTime().timetuple())
        endTime = time.strftime('%d%H%M', time.gmtime(epochEndTime)) 
        self.endTime = endTime
        
        return endTime
    
    def startDate(self, hazardEvent):
        epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
        initDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochStartTime))        

        return initDateZ
    
    def endDate(self, hazardEvent):
        epochEndTime = time.mktime(hazardEvent.getEndTime().timetuple())
        endDateZ = time.strftime('%Y%m%d_%H%M', time.gmtime(epochEndTime))            

        return endDateZ     
    
    def wmoHeader(self, hazardEvent):
        if self._phenomenon in ["obscuredThunderstorms", "embeddedThunderstorms", "frequentThunderstorms",
                                "squallLineThunderstorms", "widespreadThunderstorms", "isolatedSevereThunderstorms",
                                "turbulence", "icing", "icingFzra", "dustStorm", "sandStorm", "radioactiveRelease",
                                "severeMountainWave"]:
            if self.originatingOffice == 'KKCI':
                if self.firAbbreviation == 'KZAK':
                    wmoHeaderDict = {'ALFA': 'WSPN01', 'BRAVO': 'WSPN02', 'CHARLIE': 'WSPN03', 'DELTA': 'WSPN04',
                                     'ECHO': 'WSPN05', 'FOXTROT': 'WSPN06','GOLF': 'WSPN07', 'HOTEL': 'WSPN08',
                                     'INDIA': 'WSPN09', 'JULIETT': 'WSPN10', 'KILO': 'WSPN11', 'LIMA': 'WSPN12',
                                     'MIKE': 'WSPN13'}
                else:
                    wmoHeaderDict = {'ALFA': 'WSNT01', 'BRAVO': 'WSNT02', 'CHARLIE': 'WSNT03', 'DELTA': 'WSNT04',
                                     'ECHO': 'WSNT05', 'FOXTROT': 'WSNT06','GOLF': 'WSNT07', 'HOTEL': 'WSNT08',
                                     'INDIA': 'WSNT09', 'JULIETT': 'WSNT10', 'KILO': 'WSNT11', 'LIMA': 'WSNT12',
                                     'MIKE': 'WSNT13'}
            elif self.originatingOffice == 'PAWU':
                    wmoHeaderDict = {'INDIA': 'WSAK01', 'JULIETT': 'WSAK02', 'KILO': 'WSAK03', 'LIMA': 'WSAK04',
                                     'MIKE': 'WSAK05', 'NOVEMBER': 'WSAK06', 'OSCAR': 'WSAK07', 'PAPA': 'WSAK08',
                                     'QUEBEC': 'WSAK09'}
            else:
                    wmoHeaderDict = {'NOVEMBER': 'WSPA01', 'OSCAR': 'WSPA02', 'PAPA': 'WSPA03', 'QUEBEC': 'WSPA04',
                                     'ROMEO': 'WSPA05', 'SIERRA': 'WSPA06', 'TANGO': 'WSPA07', 'UNIFORM': 'WSPA08',
                                     'VICTOR': 'WSPA09', 'WHISKEY': 'WSPA10', 'XRAY': 'WSPA11', 'YANKEE': 'WSPA12',
                                     'ZULU': 'WSPA13'}
        elif self._phenomenon in ['volcanicAsh', 'volcanicEruption']:
            if self.originatingOffice == 'KKCI':
                if self.firAbbreviation == 'KZAK':
                    wmoHeaderDict = {'ALFA': 'WSVN01', 'BRAVO': 'WSVN02', 'CHARLIE': 'WSVN03', 'DELTA': 'WSVN04',
                                     'ECHO': 'WSVN05', 'FOXTROT': 'WSVN06','GOLF': 'WSVN07', 'HOTEL': 'WSVN08',
                                     'INDIA': 'WSVN09', 'JULIETT': 'WSVN10', 'KILO': 'WSVN11', 'LIMA': 'WSVN12',
                                     'MIKE': 'WSVN13'}
                else:
                    wmoHeaderDict = {'ALFA': 'WVNT01', 'BRAVO': 'WVNT02', 'CHARLIE': 'WVNT03', 'DELTA': 'WVNT04',
                                     'ECHO': 'WVNT05', 'FOXTROT': 'WVNT06','GOLF': 'WVNT07', 'HOTEL': 'WVNT08',
                                     'INDIA': 'WVNT09', 'JULIETT': 'WVNT10', 'KILO': 'WVNT11', 'LIMA': 'WVNT12',
                                     'MIKE': 'WVNT13'}
            elif self.originatingOffice == 'PAWU':
                    wmoHeaderDict = {'INDIA': 'WVAK01', 'JULIETT': 'WVAK02', 'KILO': 'WVAK03', 'LIMA': 'WVAK04',
                                     'MIKE': 'WVAK05', 'NOVEMBER': 'WVAK06', 'OSCAR': 'WVAK07', 'PAPA': 'WVAK08',
                                     'QUEBEC': 'WVAK09'}
            else:
                    wmoHeaderDict = {'NOVEMBER': 'WVPA01', 'OSCAR': 'WVPA02', 'PAPA': 'WVPA03', 'QUEBEC': 'WVPA04',
                                     'ROMEO': 'WVPA05', 'SIERRA': 'WVPA06', 'TANGO': 'WVPA07', 'UNIFORM': 'WVPA08',
                                     'VICTOR': 'WVPA09', 'WHISKEY': 'WVPA10', 'XRAY': 'WVPA11', 'YANKEE': 'WVPA12',
                                     'ZULU': 'WVPA13'}        
        else:
            if self.originatingOffice == 'KKCI':
                if self.firAbbreviation == 'KZAK':
                    wmoHeaderDict = {'ALFA': 'WCPN01', 'BRAVO': 'WCPN02', 'CHARLIE': 'WCPN03', 'DELTA': 'WCPN04',
                                     'ECHO': 'WCPN05', 'FOXTROT': 'WCPN06','GOLF': 'WCPN07', 'HOTEL': 'WCPN08',
                                     'INDIA': 'WCPN09', 'JULIETT': 'WCPN10', 'KILO': 'WCPN11', 'LIMA': 'WCPN12',
                                     'MIKE': 'WCPN13'}
                else:
                    wmoHeaderDict = {'ALFA': 'WCNT01', 'BRAVO': 'WCNT02', 'CHARLIE': 'WCNT03', 'DELTA': 'WCNT04',
                                     'ECHO': 'WCNT05', 'FOXTROT': 'WCNT06','GOLF': 'WCNT07', 'HOTEL': 'WCNT08',
                                     'INDIA': 'WCNT09', 'JULIETT': 'WCNT10', 'KILO': 'WCNT11', 'LIMA': 'WCNT12',
                                     'MIKE': 'WCNT13'}
            else:
                    wmoHeaderDict = {'NOVEMBER': 'WCPA01', 'OSCAR': 'WCPA02', 'PAPA': 'WCPA03', 'QUEBEC': 'WCPA04',
                                     'ROMEO': 'WCPA05', 'SIERRA': 'WCPA06', 'TANGO': 'WCPA07', 'UNIFORM': 'WCPA08',
                                     'VICTOR': 'WCPA09', 'WHISKEY': 'WCPA10', 'XRAY': 'WCPA11', 'YANKEE': 'WCPA12',
                                     'ZULU': 'WCPA13'}            
            
        wmoHeader = wmoHeaderDict[self.sequenceName]
        return wmoHeader
    
    def awipsHeader(self, hazardEvent):
        if self._phenomenon in ["obscuredThunderstorms", "embeddedThunderstorms", "frequentThunderstorms",
                                "squallLineThunderstorms", "widespreadThunderstorms", "isolatedSevereThunderstorms",
                                "turbulence", "icing", "icingFzra", "dustStorm", "sandStorm", "radioactiveRelease",
                                "severeMountainWave"]:
            if self.originatingOffice == 'KKCI':
                if self.firAbbreviation == 'KZAK':
                    awipsHeaderDict = {'ALFA': 'SIGP0A', 'BRAVO': 'SIGP0B', 'CHARLIE': 'SIGP0C', 'DELTA': 'SIGP0D',
                                     'ECHO': 'SIGP0E', 'FOXTROT': 'SIGP0F','GOLF': 'SIGP0G', 'HOTEL': 'SIGP0H',
                                     'INDIA': 'SIGP0I', 'JULIETT': 'SIGP0J', 'KILO': 'SIGP0K', 'LIMA': 'SIGP0L',
                                     'MIKE': 'SIGP0M'}
                else:
                    awipsHeaderDict = {'ALFA': 'SIGA0A', 'BRAVO': 'SIGA0B', 'CHARLIE': 'SIGA0C', 'DELTA': 'SIGA0D',
                                     'ECHO': 'SIGA0E', 'FOXTROT': 'SIGA0F','GOLF': 'SIGA0G', 'HOTEL': 'SIGA0H',
                                     'INDIA': 'SIGA0I', 'JULIETT': 'SIGA0J', 'KILO': 'SIGA0K', 'LIMA': 'SIGA0L',
                                     'MIKE': 'SIGA0M'}
            elif self.originatingOffice == 'PAWU':
                    awipsHeaderDict = {'INDIA': 'SIGAK1', 'JULIETT': 'SIGAK2', 'KILO': 'SIGAK3', 'LIMA': 'SIGAK4',
                                     'MIKE': 'SIGAK5', 'NOVEMBER': 'SIGAK6', 'OSCAR': 'SIGAK7', 'PAPA': 'SIGAK8',
                                     'QUEBEC': 'SIGAK9'}
            else:
                    awipsHeaderDict = {'NOVEMBER': 'SIGPAN', 'OSCAR': 'SIGPAO', 'PAPA': 'SIGPAP', 'QUEBEC': 'SIGPAQ',
                                     'ROMEO': 'SIGPAR', 'SIERRA': 'SIGPAS', 'TANGO': 'SIGPAT', 'UNIFORM': 'SIGPAU',
                                     'VICTOR': 'SIGPAV', 'WHISKEY': 'SIGPAW', 'XRAY': 'SIGPAX', 'YANKEE': 'SIGPAY',
                                     'ZULU': 'SIGPA'}
        elif self._phenomenon in ['volcanicAsh', 'volcanicEruption']:
            if self.originatingOffice == 'KKCI':
                if self.firAbbreviation == 'KZAK':
                    awipsHeaderDict = {'ALFA': 'WSVP0A', 'BRAVO': 'WSVP0B', 'CHARLIE': 'WSVP0C', 'DELTA': 'WSVP0D',
                                     'ECHO': 'WSVP0E', 'FOXTROT': 'WSVP0F','GOLF': 'WSVP0G', 'HOTEL': 'WSVP0H',
                                     'INDIA': 'WSVP0I', 'JULIETT': 'WSVP0J', 'KILO': 'WSVP0K', 'LIMA': 'WSVP0L',
                                     'MIKE': 'WSVP0M'}
                else:
                    awipsHeaderDict = {'ALFA': 'WSVA0A', 'BRAVO': 'WSVA0B', 'CHARLIE': 'WSVA0C', 'DELTA': 'WSVA0D',
                                     'ECHO': 'WSVA0E', 'FOXTROT': 'WSVA0F','GOLF': 'WSVA0G', 'HOTEL': 'WSVA0H',
                                     'INDIA': 'WSVA0I', 'JULIETT': 'WSVA0J', 'KILO': 'WSVA0K', 'LIMA': 'WSVA0L',
                                     'MIKE': 'WSVA0M'}
            elif self.originatingOffice == 'PAWU':
                    awipsHeaderDict = {'INDIA': 'WSVAK1', 'JULIETT': 'WSVAK2', 'KILO': 'WSVAK3', 'LIMA': 'WSVAK4',
                                     'MIKE': 'WSVAK5', 'NOVEMBER': 'WSVAK6', 'OSCAR': 'WSVAK7', 'PAPA': 'WSVAK8',
                                     'QUEBEC': 'WSVAK9'}
            else:
                    awipsHeaderDict = {'NOVEMBER': 'WSVPAN', 'OSCAR': 'WSVPAO', 'PAPA': 'WSVPAP', 'QUEBEC': 'WSVPAQ',
                                     'ROMEO': 'WSVPAR', 'SIERRA': 'WSVPAS', 'TANGO': 'WSVPAT', 'UNIFORM': 'WSVPAU',
                                     'VICTOR': 'WSVPAV', 'WHISKEY': 'WSVPAW', 'XRAY': 'WSVPAX', 'YANKEE': 'WSVPAY',
                                     'ZULU': 'WSVPAZ'}        
        else:
            if self.originatingOffice == 'KKCI':
                if self.firAbbreviation == 'KZAK':
                    awipsHeaderDict = {'ALFA': 'WSTP0A', 'BRAVO': 'WSTP0B', 'CHARLIE': 'WSTP0C', 'DELTA': 'WSTP0D',
                                     'ECHO': 'WSTP0E', 'FOXTROT': 'WSTP0F','GOLF': 'WSTP0G', 'HOTEL': 'WSTP0H',
                                     'INDIA': 'WSTP0I', 'JULIETT': 'WSTP0J', 'KILO': 'WSTP0K', 'LIMA': 'WSTP0L',
                                     'MIKE': 'WSTP0M'}
                else:
                    awipsHeaderDict = {'ALFA': 'WSTA0A', 'BRAVO': 'WSTA0B', 'CHARLIE': 'WSTA0C', 'DELTA': 'WSTA0D',
                                     'ECHO': 'WSTA0E', 'FOXTROT': 'WSTA0F','GOLF': 'WSTA0G', 'HOTEL': 'WSTA0H',
                                     'INDIA': 'WSTA0I', 'JULIETT': 'WSTA0J', 'KILO': 'WSTA0K', 'LIMA': 'WSTA0L',
                                     'MIKE': 'WSTA0M'}
            else:
                    awipsHeaderDict = {'NOVEMBER': 'WSTPAN', 'OSCAR': 'WSTPAO', 'PAPA': 'WSTPAP', 'QUEBEC': 'WSTPAQ',
                                     'ROMEO': 'WSTPAR', 'SIERRA': 'WSTPAS', 'TANGO': 'WSTPAT', 'UNIFORM': 'WSTPAU',
                                     'VICTOR': 'WSTPAV', 'WHISKEY': 'WSTPAW', 'XRAY': 'WSTPAX', 'YANKEE': 'WSTPAY',
                                     'ZULU': 'WSTPAZ'}             
            
        awipsHeader = awipsHeaderDict[self.sequenceName]      
        return awipsHeader
    
    def phenomenon(self, hazardEvent):
        self._phenomenon = hazardEvent.get('internationalSigmetPhenomenonComboBox')
        
        phenomenonDict = {"obscuredThunderstorms": 'OBSC TS', "embeddedThunderstorms": 'EMBD TS', "frequentThunderstorms": 'FRQ TS',
                          "squallLineThunderstorms": 'SQL TS', "widespreadThunderstorms": 'WDSPR TS', "isolatedSevereThunderstorms": 'ISOL SEV TS',
                          "turbulence": 'SEV TURB', "icing": 'SEV ICE', "icingFzra": 'SEV ICE FZRA', "dustStorm": 'SEV DS', "sandStorm": 'SEV SS',
                          "radioactiveRelease": 'RDOACT RELEASE', "severeMountainWave": 'SEV MTW', "tropicalCyclone": 'TC', 
                          "volcanicAsh": 'VA ERUPTION', "volcanicEruption": 'VA ERUPTION'}
        phenomenon = phenomenonDict[self._phenomenon]
        return phenomenon
    
    def location(self, hazardEvent):
        vertices = self.geometry(hazardEvent)
        location = AviationUtils.AviationUtils().createIntlSigmetLatLonString(self._geomType,vertices)           
        
        return location
    
    def forecastObserved(self, hazardEvent):
        if self._phenomenon in ["obscuredThunderstorms", "embeddedThunderstorms", "frequentThunderstorms",
                                "squallLineThunderstorms", "widespreadThunderstorms", "isolatedSevereThunderstorms"]:
            forecastObserved = hazardEvent.get("internationalSigmetFcstObs")
        elif self._phenomenon in ["turbulence", "icing", "icingFzra", "dustStorm", "sandStorm",
                                  "radioactiveRelease", "severeMountainWave"]: 
            forecastObserved = hazardEvent.get("internationalSigmetTurbObs")
        else:
            return None
   
        if forecastObserved == 'Forecast':
            forecastObservedStr = 'FCST'
        elif forecastObserved == 'Observed':
            forecastObservedStr = 'OBS AT ' + self.startTime[2:] + 'Z'
        else:
            return None
            
        forecastObserved = forecastObservedStr
        return forecastObserved
    
    def verticalExtent(self, hazardEvent):
        if self._phenomenon in ["obscuredThunderstorms", "embeddedThunderstorms", "frequentThunderstorms",
                        "squallLineThunderstorms", "widespreadThunderstorms", "isolatedSevereThunderstorms"]:
            verticalExtent = hazardEvent.get("cbTops")
            verticalExtentStr = 'TOP ' + verticalExtent + '.'
        elif self._phenomenon in ["turbulence", "icing", "icingFzra", "dustStorm", "sandStorm",
                                  "radioactiveRelease", "severeMountainWave"]: 
            verticalExtentBottom = hazardEvent.get("internationalSigmetTurbulenceExtentBottom")
            verticalExtentTop = hazardEvent.get("internationalSigmetTurbulenceExtentTop")
            verticalExtentStr = verticalExtentBottom+'/'+verticalExtentTop + '.'
        elif self._phenomenon in ['tropicalCyclone']:
            verticalExtent = hazardEvent.get('cbTopsTC')
            verticalExtentStr = 'FRQ TS TOP ' + verticalExtent
        elif self._phenomenon in ["volcanicAsh"]:
            return None
        else:
            verticalExtent = hazardEvent.get("internationalSigmetEruptionExtentTop")
            verticalExtentStr = 'ESTIMATED ASH TOP TO '+ verticalExtent + '.'
                        
        verticalExtent = verticalExtentStr
        return verticalExtent
    
    def movement(self, hazardEvent):
        if self._phenomenon in ["obscuredThunderstorms", "embeddedThunderstorms", "frequentThunderstorms",
                        "squallLineThunderstorms", "widespreadThunderstorms", "isolatedSevereThunderstorms"]:
            speed = hazardEvent.get("internationalSigmetSpeed")
            direction = hazardEvent.get("internationalSigmetDirection")
        elif self._phenomenon in ["turbulence", "icing", "icingFzra", "dustStorm", "sandStorm",
                                  "radioactiveRelease", "severeMountainWave"]:
            speed = hazardEvent.get("internationalSigmetTurbulenceSpeed")
            direction = hazardEvent.get("internationalSigmetTurbulenceDirection")
        elif self._phenomenon in ["tropicalCyclone"]:
            speed = hazardEvent.get('internationalSigmetTCSpeed')
            direction = hazardEvent.get('internationalSigmetTCDirection')
        else:
            return None
        
        if speed == 0:
            movementStr = 'STNR.'
        else:
            movementStr = 'MOV ' + direction + ' ' + str(speed) + 'KT.'
            
        movement = movementStr           
        return movement
    
    def intensityTrend(self, hazardEvent):
        if self._phenomenon not in ['volcanicAsh', 'volcanicEruption']:
            intensityDict = {'No Change': 'NC.', 'Intensifying': 'INTSF.', 'Weakening': 'WKN.'}
            if self._phenomenon in ['tropicalCyclone']:
                intensity = hazardEvent.get('internationalSigmetTCIntensity')     
            if self._phenomenon in ["obscuredThunderstorms", "embeddedThunderstorms", "frequentThunderstorms",
                            "squallLineThunderstorms", "widespreadThunderstorms", "isolatedSevereThunderstorms"]:
                intensity = hazardEvent.get("internationalSigmetIntensity")        
            elif self._phenomenon in ["turbulence", "icing", "icingFzra", "dustStorm", "sandStorm",
                                      "radioactiveRelease", "severeMountainWave"]:
                intensity = hazardEvent.get("internationalSigmetTurbIntensity")
            intensityTrend = intensityDict[intensity]
        else:
            return None
            
        return intensityTrend

    def volcanoProductPartsDict(self, hazardEvent):
        volcanoProductPartsDict = {}
        
        if self._phenomenon not in ['volcanicAsh', 'volcanicEruption']:
            return None
        else:
            volcanoDict = AviationUtils.AviationUtils().createVolcanoDict()
            
        if self._phenomenon == 'volcanicAsh':
            volcanoProductPartsDict['type'] = 'volcanicAsh'
            volcanoName = hazardEvent.get("internationalSigmetVolcanoNameVA")
            numFcstLayers = hazardEvent.get("internationalSigmetVALayersSpinner")
            numObsLayers = hazardEvent.get("internationalSigmetObservedLayerSpinner")
            volcanoProductPartsDict['numFcstLayers'] = numFcstLayers
            volcanoProductPartsDict['numObsLayers'] = numObsLayers
            volcanoProductPartsDict['resuspension'] = hazardEvent.get("internationalSigmetVolcanoResuspension")
            volcanoProductPartsDict['observedLayerTop'] = hazardEvent.get("internationalSigmetObservedExtentTop")
            volcanoProductPartsDict['observedLayerBottom'] = hazardEvent.get("internationalSigmetObservedExtentBottom")
            
            obsSpeed = hazardEvent.get("internationalSigmetObservedSpeed")
            obsDirection = hazardEvent.get("internationalSigmetObservedDirection")
            if obsSpeed == 0:
                obsMotionStr = 'STNR.'
            else:
                obsMotionStr = 'MOV ' + obsDirection + ' ' + str(obsSpeed) + 'KT.'
            volcanoProductPartsDict['observedLayerMotion'] = obsMotionStr
            
            for i in range(0, numObsLayers):
                i = i+1
                vertices = hazardEvent.get('vaObsPoly'+str(i))
                vaLayerObsStr = self.getVALayerStr(vertices)
                volcanoProductPartsDict['vaObsPoly'+str(i)] = vaLayerObsStr
            
            for i in range(0, numFcstLayers):
                i = i+1
                vertices = hazardEvent.get('vaFcstPoly'+str(i))
                vaLayerFcstStr = self.getVALayerStr(vertices)
                volcanoProductPartsDict['vaFcstPoly'+str(i)] = vaLayerFcstStr
                
            for i in range(0,numObsLayers):
                volcanoProductPartsDict['obsLayer'+str(i+1)] = {}
                volcanoProductPartsDict['obsLayer'+str(i+1)]["bottom"] = hazardEvent.get("internationalSigmetObservedExtentBottom"+str(i+1))
                volcanoProductPartsDict['obsLayer'+str(i+1)]["top"] = hazardEvent.get("internationalSigmetObservedExtentTop"+str(i+1))
                
                speed = hazardEvent.get("internationalSigmetObservedSpeed"+str(i+1))
                direction = hazardEvent.get("internationalSigmetObservedDirection"+str(i+1))
                if speed == 0:
                    motionStr = 'STNR.'
                else:
                    motionStr = 'MOV ' + direction + ' ' + str(speed) + 'KT.'
                volcanoProductPartsDict['obsLayer'+str(i+1)]["motion"] = motionStr                
            
            for i in range(0,numFcstLayers):
                volcanoProductPartsDict['fcstLayer'+str(i+1)] = {}
                volcanoProductPartsDict['fcstLayer'+str(i+1)]["bottom"] = hazardEvent.get("internationalSigmetVAExtentBottom"+str(i+1))
                volcanoProductPartsDict['fcstLayer'+str(i+1)]["top"] = hazardEvent.get("internationalSigmetVAExtentTop"+str(i+1))
                
                speed = hazardEvent.get("internationalSigmetVASpeed"+str(i+1))
                direction = hazardEvent.get("internationalSigmetVADirection"+str(i+1))
                if speed == 0:
                    motionStr = 'STNR.'
                else:
                    motionStr = 'MOV ' + direction + ' ' + str(speed) + 'KT.'
                volcanoProductPartsDict['fcstLayer'+str(i+1)]["motion"] = motionStr
                
        elif self._phenomenon == 'volcanicEruption':
            volcanoProductPartsDict['type'] = 'volcanicEruption'
            volcanoName = hazardEvent.get("internationalSigmetVolcanoNameEruption")
            volcanoIndicator = hazardEvent.get("internationalSigmetEruptionIndicator")
            volcanoIndicatorStr = self.getVolcanoIndicatorStr(volcanoIndicator)
            volcanoProductPartsDict['indicator'] = volcanoIndicatorStr
            volcanoTime = hazardEvent.get("internationalSigmetVolcanoEruptionTime")
            volcanoTimeStr = self.getVolcanoTimeStr(volcanoTime)
            volcanoProductPartsDict['time'] = volcanoTimeStr
            
        volcanoProductPartsDict['simpleName'] = volcanoName    
        volcanoProductPartsDict['name'] = volcanoName + ' VOLCANO'
        volcanoDict = volcanoDict[volcanoName]
        
        volcanoProductPartsDict['simplePosition'] = volcanoDict[1] + ' ' + volcanoDict[2]
        volcanoPosition = self.getVolcanoPosition(volcanoDict)
        volcanoProductPartsDict['position'] = volcanoPosition
        
        return volcanoProductPartsDict
    
    def getVALayerStr(self, vertices):
        newVerticesList = []
        locationList = []
        for vertex in vertices:
            newVert = []
            for vert in vertex:
                decimal, degree = math.modf(vert)
                minute = (decimal*60)/100
                vert = round(int(degree) + minute, 2) 
                newVert.append(vert)
                newVert.reverse()
            newVerticesList.append(newVert)
        
        for vertices in newVerticesList:
            latMinute, latDegree = math.modf(vertices[0])
            lonMinute, lonDegree = math.modf(vertices[1])
        
            if latDegree < 0:
                lat = 'S'+str(abs(int(latDegree)))+str(abs(int(latMinute*100)))
            else:
                lat = 'N'+str(int(latDegree))+str(int(latMinute*100))
        
            if lonDegree < 0:
                lon = 'W'+str(abs(int(lonDegree)))+str(abs(int(lonMinute*100)))
            else:
                lon = 'E'+str(int(lonDegree))+str(int(lonMinute*100))
        
            locationList.append(lat+' '+lon)
            locationList.append(' - ')
        
        locationList.pop()        
           
        vaLayerFcstStr = "".join(locationList)
        vaLayerFcstStr = "WI " + vaLayerFcstStr + '.'    
        
        return vaLayerFcstStr
    
    def getVolcanoIndicatorStr(self, volcanoIndicator):
        indicatorDict = {'mt-sat': 'MT-SAT', 'goes': 'GOES', 'poes': 'POES',
                         'avo': 'AVO', 'kvert': 'KVERT', 'pilot': 'PILOT REPORT',
                         'radar': 'RADAR'}
        if not volcanoIndicator:
            return None
        else:
            volcanoIndicatorStr = ''
            for indicator in volcanoIndicator:
                volcanoIndicatorStr += indicatorDict[indicator] + '/'
            volcanoIndicatorStr = volcanoIndicatorStr[:-1]
        
        return volcanoIndicatorStr
    
    def getVolcanoTimeStr(self, volcanoTime):
        volcanoTime = volcanoTime / 1000.0
        volcanoTimeStr = datetime.datetime.fromtimestamp(volcanoTime).strftime('%d/%H%M')
        
        return volcanoTimeStr
    
    def getVolcanoPosition(self,volcanoDict):
        lat = float(volcanoDict[1])
        lon = float(volcanoDict[2])
        
        latDecimal, latDegree = math.modf(lat)
        lonDecimal, lonDegree = math.modf(lon)

        latMinute = (latDecimal*60)/100
        lonMinute = (lonDecimal*60)/100
        lat = int(round(int(latDegree) + latMinute, 2)*100)
        lon = int(round(int(lonDegree) + lonMinute, 2)*100)

        if lat < 0:
            lat = 'S'+str(abs(lat))
        else:
            lat = 'N'+str(lat)

        if lon < 0:
            lon = 'W'+str(abs(lon))
        else:
            lon = 'E'+str(lon)

        volcanoPosition = 'PSN ' + lat + ' ' + lon        
        
        return volcanoPosition
    
    def tropicalCycloneProductPartsDict(self, hazardEvent):
        tropicalCycloneProductPartsDict = {}
        
        if self._phenomenon not in ['tropicalCyclone']:
            return None
        
        if self._geomType == 'Point':
            radius = hazardEvent.get('internationalSigmetWidth')
            radiusStr = 'WI ' + str(radius) + 'NM OF CENTER.'
            tropicalCycloneProductPartsDict['radius'] = radiusStr
        
        tropicalCycloneName = hazardEvent.get('internationalSigmetTCName')
        tropicalCycloneProductPartsDict['name'] = tropicalCycloneName.upper()
        
        observationTime = hazardEvent.get('internationalSigmetTCObsTime')
        observationTimeStr = 'OBS AT ' + observationTime + 'Z'
        tropicalCycloneProductPartsDict['observationTime'] = observationTimeStr
        
        centerLocation = hazardEvent.get('internationalSigmetTCPosition')
        centerLocationStr = 'NR ' + centerLocation.upper() + '.'
        tropicalCycloneProductPartsDict['centerLocation'] = centerLocationStr
        
        fcstTime = hazardEvent.get('internationalSigmetTCFcstTime')
        fcstPosition = hazardEvent.get('internationalSigmetTCFcstPosition')
        forecastPosition = 'FCST ' + fcstTime + 'Z TC CENTER ' + fcstPosition.upper() + '.'
        tropicalCycloneProductPartsDict['fcstPosition'] = forecastPosition
        
        return tropicalCycloneProductPartsDict
    
    def previousStartTime(self, hazardEvent):
        import json
        if os.path.isfile(self.outputNumberFile()):
            with open(self.outputNumberFile()) as openFile:
                internationalSigmetNumberDict = json.load(openFile)
            if self.sequenceName in internationalSigmetNumberDict:
                previousStartTime = internationalSigmetNumberDict[self.sequenceName]['startTime']
            else:
                return None
        else:
            
            return None
        
        return previousStartTime
    
    def previousEndTime(self, hazardEvent):
        import json
        if os.path.isfile(self.outputNumberFile()):
            with open(self.outputNumberFile()) as openFile:
                internationalSigmetNumberDict = json.load(openFile)
            if self.sequenceName in internationalSigmetNumberDict:
                    previousEndTime = internationalSigmetNumberDict[self.sequenceName]['endTime']
            else:
                return None
        else:
            return None
        
        return previousEndTime                                                        
    
    def geometry(self, hazardEvent):
        for g in hazardEvent.getFlattenedGeometry().geoms:
            geometry = shapely.geometry.base.dump_coords(g)
        
        return geometry
    
    def _narrativeForecastInformation(self, segmentDict, productSegmentGroup, productSegment):  
        default = '''
|* 
WSNT01 KKCI 261700
SIGA0A
KZWY SIGMET ALFA 5 VALID 261700/262100 KKCI-
KZWY NEW YORK OCEANIC FIR OBSC TS FCST WI N4727 W10040 - N4413
W10134 - N440 W9739 - N4727 W10040. TOP FL300. STNR. NC.
*|
         '''  
        productDict['narrativeForecastInformation'] = self._section.hazardEvent.get('narrativeForecastInformation', default)

    def executeFrom(self, dataList, eventSet, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, eventSet, prevDataList, False)
        else:
            self.updateExpireTimes(dataList)
        return dataList
    
    #########################################
    ### OVERRIDES
    
    def outputNumberFile(self):
        return '/scratch/internationalSigmetNumber.txt'
