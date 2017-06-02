"""
    Description: Product Generator for the Volcanic Ash Advisory product.
"""
import os, collections
import math
import HydroGenerator
import shapely, time, datetime
import AviationUtils


class Product(HydroGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()  
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'VAA_ProductGenerator'
        self._productID = 'VAA'

###################################################
        
                
    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD/Raytheon"
        metadata['description'] = "Product generator for VAA."
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
        self._productCategory = "VAA"
        self._areaName = '' 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        self._purgeHours = 8
        self._SIGMET_ProductName = 'VAA'
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

        productDict:  {'eventDicts': [{'eventID': 'HZ-2015-OAX-002353', 'geomType': None, 
        'issueFlag': 'False', 'parts': OrderedDict([('currentTime', '261700'), 
        ('startTime', '1700'), ('endTime', '2300'), ('forecastTime6', '26/2300'), 
        ('forecastTime12', '27/0500'), ('forecastTime18', '27/1100'), 
        ('forecastTime24', '27/1700'), ('dateStr', '20170419'), ('vaacOffice', 'ANCHORAGE'), 
        ('productType', 'VAA'), ('intensityTrend', None), ('volcanoName', 'ADAGDAK'), 
        ('volcanoNumber', '311800'), ('volcanoLatLon', 'N5198 W17659'), 
        ('volcanoSubregion', 'Aleutian Islands'), ('volcanoElevation', '2001 FT (610 M)'), 
        ('advisoryNumber', '001'), ('informationSource', 'AVO'), ('volcanoStatus', 'RED'), 
        ('eruptionDetails', 'VA CONTINUOUSLY OBS ON SATELLITE IMAGERY'), ('numLayers', 0), 
        ('layer1Location', 'VA NOT IDENTIFIABLE FROM SATELLITE'), ('layer2Location', ''), 
        ('layer3Location', ''), ('layer1Forecast6', 'NO VA EXP'), ('layer1Forecast12', 'NO VA EXP'), 
        ('layer1Forecast18', 'NO VA EXP'), ('layer1Forecast24', 'NO VA EXP'), 
        ('layer2Forecast6', ''), ('layer2Forecast12', ''), ('layer2Forecast18', ''), 
        ('layer2Forecast24', ''), ('layer3Forecast6', ''), ('layer3Forecast12', ''), 
        ('layer3Forecast18', ''), ('layer3Forecast24', ''), 
        ('remarks', '***ADD CUSTOM REMARKS HERE...DELETE IF NOT USED***'), 
        ('nextAdvisory', 'WILL BE ISSUED BY YYYYMMDD/HHmmZ'), ('forecasterInitials', '###')]),
         'status': 'PENDING', 'hazardType': 'VAA'}], 'productName': 'VAA', 
         'productParts': ['currentTime', 'startTime', 'endTime', 'forecastTime6', 'forecastTime12', 
         'forecastTime18', 'forecastTime24', 'dateStr', 'vaacOffice', 'productType', 'intensityTrend', 
         'volcanoName', 'volcanoNumber', 'volcanoLatLon', 'volcanoSubregion', 'volcanoElevation', 
         'advisoryNumber', 'informationSource', 'volcanoStatus', 'eruptionDetails', 'numLayers', 
         'layer1Location', 'layer2Location', 'layer3Location', 'layer1Forecast6', 
         'layer1Forecast12', 'layer1Forecast18', 'layer1Forecast24', 'layer2Forecast6', 
         'layer2Forecast12', 'layer2Forecast18', 'layer2Forecast24', 'layer3Forecast6', 
         'layer3Forecast12', 'layer3Forecast18', 'layer3Forecast24', 'remarks', 'nextAdvisory', 
         'forecasterInitials'], 'productID': 'VAA'}

        '''
        self._initialize() 
        self.logger.info("Start ProductGeneratorTemplate:execute ESF")
        
        # Extract information for execution
        self._inputHazardEvents = eventSet.getEvents()
        eventSetAttrs = eventSet.getAttributes()
        metaDict = eventSet.getAttributes()
        self._issueFlag = metaDict.get('issueFlag')
        self._currentTime = eventSet.getAttributes().get("currentTime") / 1000
        
        self._volcanoDict = AviationUtils.AviationUtils().createVolcanoDict()
        
        if dialogInputMap:
            self._storeDialogInputMap(dialogInputMap)
        else:
            self._dialogInputMap = {}
        
        parts = ['currentTime', 'startTime', 'endTime', 'forecastTime6',
                 'forecastTime12', 'forecastTime18', 'forecastTime24',
                 'dateStr', 'vaacOffice', 'productType', 'intensityTrend',
                 'volcanoName', 'volcanoNumber', 'volcanoLatLon', 'volcanoSubregion',
                 'volcanoElevation', 'advisoryNumber', 'informationSource',
                 'volcanoStatus', 'eruptionDetails', 'numLayers', 'layer1Location',
                 'layer2Location', 'layer3Location', 'layer1Forecast6',
                 'layer1Forecast12', 'layer1Forecast18', 'layer1Forecast24',
                 'layer2Forecast6', 'layer2Forecast12', 'layer2Forecast18',
                 'layer2Forecast24', 'layer3Forecast6', 'layer3Forecast12',
                 'layer3Forecast18', 'layer3Forecast24', 'confidence', 'remarks',
                 'nextAdvisory', 'forecasterInitials']
        
        productDict = {}
        productDict['productParts'] = parts
        eventDicts = []
                          
        for event in self._inputHazardEvents:
            self._eventPhenomenon = event.getPhenomenon()
            self._geomType = event.get('originalGeomType')
            
            dict = {}
            dict['eventID'] = event.getEventID()
            dict['geomType'] = self._geomType
            dict['issueFlag'] = self._issueFlag
            dict['hazardType'] = self._eventPhenomenon
            dict['status'] = event.getStatus()
            
            partDict = collections.OrderedDict()
            for partName in parts:
                execStr = 'partStr = self.' + partName + '(event)'
                exec execStr
                partDict[partName] = partStr
            
            dict['parts'] = partDict
            eventDicts.append(dict)
        
        productDict['eventDicts'] = eventDicts
        productDict['productID'] = 'VAA'
        productDict['productName'] = 'VAA' 

        return [productDict], self._inputHazardEvents
    
######################PRODUCT PART METHODS###################################                
    def currentTime(self, hazardEvent):
        currentTime = time.strftime('%d%H%M', time.gmtime(self._currentTime))
        self.currentTimeStr = currentTime
        return currentTime
    
    def startTime(self, hazardEvent):
        self._epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
        startTime = time.strftime('%H%M', time.gmtime(self._epochStartTime))
        self.startTimeStr = startTime 
        return startTime
    
    def endTime(self, hazardEvent):
        self.epochEndTime = time.mktime(hazardEvent.getEndTime().timetuple())
        endTime = time.strftime('%H%M', time.gmtime(self.epochEndTime)) 
        self.endTimeStr = endTime
        return endTime
    
    def forecastTime6(self, hazardEvent):
        newTime = hazardEvent.getStartTime() + datetime.timedelta(hours=6)
        forecastTime6 = newTime.strftime('%d/%H%M')
        self._forecastTime6 = forecastTime6
        return forecastTime6
    
    def forecastTime12(self, hazardEvent):
        newTime = hazardEvent.getStartTime() + datetime.timedelta(hours=12)
        forecastTime12 = newTime.strftime('%d/%H%M')
        self._forecastTime12 = forecastTime12
        return forecastTime12
    
    def forecastTime18(self, hazardEvent):
        newTime = hazardEvent.getStartTime() + datetime.timedelta(hours=18)
        forecastTime18 = newTime.strftime('%d/%H%M')
        self._forecastTime18 = forecastTime18
        return forecastTime18
    
    def forecastTime24(self, hazardEvent):
        newTime = hazardEvent.getStartTime() + datetime.timedelta(hours=24)
        forecastTime24 = newTime.strftime('%d/%H%M')
        self._forecastTime24 = forecastTime24
        return forecastTime24
    
    def dateStr(self, hazardEvent):
        return datetime.datetime.today().strftime('%Y%m%d')
    
    def vaacOffice(self, hazardEvent):
        return 'ANCHORAGE'
    
    def productType(self, hazardEvent):
        return hazardEvent.get('volcanoAction')   
    
    def intensityTrend(self, hazardEvent):
        intensityStrDict = {'No Change': None, 'Intensifying': ' INTSF.', 'Weakening': ' WKN.'}
        
        return intensityStrDict[hazardEvent.get('volcanoIntensity')]                
    
    def volcanoName(self, hazardEvent):
        volcanoName = hazardEvent.get('volcanoName')
        self._volcanoName = volcanoName
        return volcanoName
    
    def volcanoNumber(self, hazardEvent):
        volcanoNumber = self._volcanoDict[self._volcanoName][0]
        return volcanoNumber
    
    def volcanoLatLon(self, hazardEvent):
        latitude = float(self._volcanoDict[self._volcanoName][1])
        longitude = float(self._volcanoDict[self._volcanoName][2])
        
        latMinute, latDegree = math.modf(latitude)
        lonMinute, lonDegree = math.modf(longitude)        
        
        if latDegree < 0:
            lat = 'S'+str(abs(int(latDegree)))+str(abs(int(latMinute*100)))
        else:
            lat = 'N'+str(int(latDegree))+str(int(latMinute*100))
    
        if lonDegree < 0:
            lon = 'W'+str(abs(int(lonDegree)))+str(abs(int(lonMinute*100)))
        else:
            lon = 'E'+str(int(lonDegree))+str(int(lonMinute*100))
            
        volcanoLatLon = lat + ' ' + lon

        return volcanoLatLon
    
    def volcanoSubregion(self, hazardEvent):
        volcanoSubregion = self._volcanoDict[self._volcanoName][4]
        return volcanoSubregion
    
    def volcanoElevation(self, hazardEvent):
        elevationMeters = float(self._volcanoDict[self._volcanoName][3])
        elevationFeet = elevationMeters * 3.28
        volcanoElevation = str(int(round(elevationFeet)))+' FT ('+str(int(round(elevationMeters)))+' M)'

        return volcanoElevation
    
    def advisoryNumber(self, hazardEvent):
        import json

        currentYear = datetime.datetime.today().strftime('%Y')
        
        vaaNumberDict = {}
        
        #if file already exists
        if os.path.isfile('/scratch/vaaNumber.txt'):
            with open('/scratch/vaaNumber.txt') as openFile:
                vaaNumberDict = json.load(openFile)  
            year = vaaNumberDict['year']
            #if not same year, reset and set number to one
            if year != currentYear:
                advisoryNumber = 1
            #if same year
            else:
                #check to see if volcano is in dict
                if self._volcanoName in vaaNumberDict:
                    #if it's in dict iterate by one
                    advisoryNumber = vaaNumberDict[self._volcanoName] + 1
                #if it's not in dict set to one
                else:
                    advisoryNumber = 1
        else:
            advisoryNumber = 1
        
        vaaNumberDict['year'] = currentYear    
        vaaNumberDict[self._volcanoName] = advisoryNumber
            
        if self._issueFlag == "True":
            with open('/scratch/vaaNumber.txt', 'w') as outFile:
                json.dump(vaaNumberDict, outFile)
                
        if advisoryNumber < 10:
            advisoryNumber = '00' + str(advisoryNumber)
        elif 100 > advisoryNumber > 9:
            advisoryNumber = '0' + str(advisoryNumber)
        else:
            advisoryNumber = str(advisoryNumber)      
            
        return advisoryNumber
    
    def informationSource(self, hazardEvent):
        informationSourceDict = {'mt-sat': 'MT-SAT', 'goes': 'GOES', 'poes': 'POES',
                         'avo': 'AVO', 'kvert': 'KVERT', 'pilot': 'PILOT REPORT',
                         'radar': 'RADAR', 'ship': 'SHIP REPORT', 'webcam': 'AVO WEBCAM'}
        
        informationSourceList = hazardEvent.get('volcanoInfoSource')
        
        if not informationSourceList:
            return None
        else:
            informationSource = ''
            for source in informationSourceList:
                informationSource += informationSourceDict[source] + '/'
            informationSource = informationSource[:-1]
               
        return informationSource
    
    def volcanoStatus(self, hazardEvent):
        return hazardEvent.get('volcanoStatus')
    
    def eruptionDetails(self, hazardEvent):
        return hazardEvent.get('volcanoEruptionDetails')
    
    def numLayers(self, hazardEvent):
        return hazardEvent.get('volcanoLayersSpinner')
    
    def layer1Location(self, hazardEvent):
        layer1Location = ''
        
        if hazardEvent.get("volcanoLayersSpinner") == 0:
            layer1Location = layer1Location + 'VA NOT IDENTIFIABLE FROM SATELLITE'
            self._visualFeatureGeomDict = None
        else:
            self._visualFeatureGeomDict = self.createVisualFeatureGeomDict(hazardEvent)
            self._latLonStatementDict = self.createLatLonStatementDict(self._visualFeatureGeomDict)
            
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "basePreview" in key:
                    vertices = value
                    layer1Location = layer1Location + hazardEvent.get('volcanoLayerBottom1') + '/' + hazardEvent.get('volcanoLayerTop1')
                    layer1Location = layer1Location + ' ' + self._latLonStatementDict[key] + ' MOV ' + hazardEvent.get("volcanoLayerDirection1")
                    layer1Location = layer1Location + ' ' + str(hazardEvent.get("volcanoLayerSpeed1")) + 'KT.'

        return layer1Location
    
    def layer2Location(self, hazardEvent):
        layer2Location = ''
        
        if hazardEvent.get("volcanoLayersSpinner") == 0:
            layer2Location = layer2Location
        else:        
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer2fcst0" in key:
                    vertices = value
                    layer2Location = layer2Location + hazardEvent.get('volcanoLayerBottom2') + '/' + hazardEvent.get('volcanoLayerTop2')
                    layer2Location = layer2Location + ' ' + self._latLonStatementDict[key] + ' MOV ' + hazardEvent.get("volcanoLayerDirection2")
                    layer2Location = layer2Location + ' ' + str(hazardEvent.get("volcanoLayerSpeed2")) + 'KT.' 
                           
        return layer2Location
    
    def layer3Location(self, hazardEvent):
        layer3Location = ''
        
        if hazardEvent.get("volcanoLayersSpinner") == 0:
            layer3Location = layer3Location         
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer3fcst0" in key:
                    vertices = value
                    layer3Location = layer3Location + hazardEvent.get('volcanoLayerBottom3') + '/' + hazardEvent.get('volcanoLayerTop3')
                    layer3Location = layer3Location + ' ' + self._latLonStatementDict[key] + ' MOV ' + hazardEvent.get("volcanoLayerDirection3")
                    layer3Location = layer3Location + ' ' + str(hazardEvent.get("volcanoLayerSpeed3")) + 'KT.' 
                            
        return layer3Location
    
    def layer1Forecast6(self, hazardEvent):
        layer1Forecast6 = ''
        if self._visualFeatureGeomDict is None:
            layer1Forecast6 = 'NO VA EXP'
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer1fcst1" in key:
                    layer1Forecast6 = self._forecastTime6 + ' ' + hazardEvent.get('volcanoLayerBottom1') + '/' + hazardEvent.get('volcanoLayerTop1')
                    layer1Forecast6 = layer1Forecast6 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer1Forecast6:
            layer1Forecast6 = 'NO VA EXP'

        return layer1Forecast6
    
    def layer1Forecast12(self, hazardEvent):
        layer1Forecast12 = ''
        if self._visualFeatureGeomDict is None:
            layer1Forecast12 = 'NO VA EXP'
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer1fcst2" in key:
                    layer1Forecast12 = self._forecastTime12 + ' ' + hazardEvent.get('volcanoLayerBottom1') + '/' + hazardEvent.get('volcanoLayerTop1')
                    layer1Forecast12 = layer1Forecast12 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer1Forecast12:
            layer1Forecast12 = 'NO VA EXP'
                    
        return layer1Forecast12
    
    def layer1Forecast18(self, hazardEvent):
        layer1Forecast18 = ''
        if self._visualFeatureGeomDict is None:
            layer1Forecast18 = 'NO VA EXP'
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer1fcst3" in key:
                    layer1Forecast18 = self._forecastTime18 + ' ' + hazardEvent.get('volcanoLayerBottom1') + '/' + hazardEvent.get('volcanoLayerTop1')
                    layer1Forecast18 = layer1Forecast18 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer1Forecast18:
            layer1Forecast18 = 'NO VA EXP'
        
        return layer1Forecast18
    
    def layer1Forecast24(self, hazardEvent):
        layer1Forecast24 = ''
        if self._visualFeatureGeomDict is None:
            layer1Forecast24 = 'NO VA EXP'
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer1fcst4" in key:
                    layer1Forecast24 = self._forecastTime24 + ' ' + hazardEvent.get('volcanoLayerBottom1') + '/' + hazardEvent.get('volcanoLayerTop1')
                    layer1Forecast24 = layer1Forecast24 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer1Forecast24:
            layer1Forecast24 = 'NO VA EXP'
        
        return layer1Forecast24
    
    def layer2Forecast6(self, hazardEvent):
        layer2Forecast6 = ''
        if self._visualFeatureGeomDict is None:
            layer2Forecast6 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer2fcst1" in key:
                    layer2Forecast6 = ' ' + self._forecastTime6 + ' ' + hazardEvent.get('volcanoLayerBottom2') + '/' + hazardEvent.get('volcanoLayerTop2')
                    layer2Forecast6 = layer2Forecast6 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer2Forecast6:
            layer2Forecast6 = ''
        
        return layer2Forecast6
    
    def layer2Forecast12(self, hazardEvent):
        layer2Forecast12 = ''
        if self._visualFeatureGeomDict is None:
            layer2Forecast12 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer2fcst2" in key:
                    layer2Forecast12 = ' ' + self._forecastTime12 + ' ' + hazardEvent.get('volcanoLayerBottom2') + '/' + hazardEvent.get('volcanoLayerTop2')
                    layer2Forecast12 = layer2Forecast12 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer2Forecast12:
            layer2Forecast12 = ''
        
        return layer2Forecast12
    
    def layer2Forecast18(self, hazardEvent):
        layer2Forecast18 = ''
        if self._visualFeatureGeomDict is None:
            layer2Forecast18 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer2fcst3" in key:
                    layer2Forecast18 = ' ' + self._forecastTime18 + ' ' + hazardEvent.get('volcanoLayerBottom2') + '/' + hazardEvent.get('volcanoLayerTop2')
                    layer2Forecast18 = layer2Forecast18 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer2Forecast18:
            layer2Forecast18 = ''
        
        return layer2Forecast18
    
    def layer2Forecast24(self, hazardEvent):
        layer2Forecast24 = ''
        if self._visualFeatureGeomDict is None:
            layer2Forecast24 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer2fcst4" in key:
                    layer2Forecast24 = ' ' + self._forecastTime24 + ' ' + hazardEvent.get('volcanoLayerBottom2') + '/' + hazardEvent.get('volcanoLayerTop2')
                    layer2Forecast24 = layer2Forecast24 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer2Forecast24:
            layer2Forecast24 = ''
        
        return layer2Forecast24    

    def layer3Forecast6(self, hazardEvent):
        layer3Forecast6 = ''
        if self._visualFeatureGeomDict is None:
            layer3Forecast6 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer3fcst1" in key:
                    layer3Forecast6 = ' ' + self._forecastTime6 + ' ' + hazardEvent.get('volcanoLayerBottom3') + '/' + hazardEvent.get('volcanoLayerTop3')
                    layer3Forecast6 = layer3Forecast6 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer3Forecast6:
            layer3Forecast6 = ''
        
        return layer3Forecast6
    
    def layer3Forecast12(self, hazardEvent):
        layer3Forecast12 = ''
        if self._visualFeatureGeomDict is None:
            layer3Forecast12 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer3fcst2" in key:
                    layer3Forecast12 = ' ' + self._forecastTime12 + ' ' + hazardEvent.get('volcanoLayerBottom3') + '/' + hazardEvent.get('volcanoLayerTop3')
                    layer3Forecast12 = layer3Forecast12 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer3Forecast12:
            layer3Forecast12 = ''
        
        return layer3Forecast12
    
    def layer3Forecast18(self, hazardEvent):
        layer3Forecast18 = ''
        if self._visualFeatureGeomDict is None:
            layer3Forecast18 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer3fcst3" in key:
                    layer3Forecast18 = ' ' + self._forecastTime18 + ' ' + hazardEvent.get('volcanoLayerBottom3') + '/' + hazardEvent.get('volcanoLayerTop3')
                    layer3Forecast18 = layer3Forecast18 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer3Forecast18:
            layer3Forecast18 = ''
        
        return layer3Forecast18
    
    def layer3Forecast24(self, hazardEvent):
        layer3Forecast24 = ''
        if self._visualFeatureGeomDict is None:
            layer3Forecast24 = ''
        else:
            for key, value in self._visualFeatureGeomDict.iteritems():
                if "Layer3fcst4" in key:
                    layer3Forecast24 = ' ' + self._forecastTime24 + ' ' + hazardEvent.get('volcanoLayerBottom3') + '/' + hazardEvent.get('volcanoLayerTop3')
                    layer3Forecast24 = layer3Forecast24 + ' ' + self._latLonStatementDict[key] + '.'
        if not layer3Forecast24:
            layer3Forecast24 = ''
        
        return layer3Forecast24
    
    def confidence(self, hazardEvent):
        return hazardEvent.get('volcanoConfidence')
    
    def remarks(self, hazardEvent):
        return hazardEvent.get('volcanoRemarks')
    
    def nextAdvisory(self, hazardEvent):
        return hazardEvent.get('volcanoNextAdvisory')
    
    def forecasterInitials(self, hazardEvent):
        return hazardEvent.get('volcanoForecasterInitials')

######################HELPER METHODS ###################################
    def createLatLonStatementDict(self, visualFeatureGeomDict):
        latLonStatementDict = {}
        
        for key, value in self._visualFeatureGeomDict.iteritems():
            latLonStatement = self.createLatLonStatement(value)
            latLonStatementDict[key] = latLonStatement
        
        return latLonStatementDict
    
    def createLatLonStatement(self, vertices):
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
        latLonStatement = "".join(locationList)
                
        return latLonStatement
    
    def createVisualFeatureGeomDict(self, hazardEvent):
        visualFeatureGeomDict = {}
        features = hazardEvent.getVisualFeatures()
        if not features:
            vertices = self.geometry(hazardEvent)
            visualFeatureGeomDict = {'basePreview': vertices}
            
        for feature in features:
            featureIdentifier = feature.get('identifier')
            polyDict = feature['geometry']
            for timeBounds, geometry in polyDict.iteritems():
                featurePoly = geometry.asShapely()
                vertices = shapely.geometry.base.dump_coords(featurePoly)
                visualFeatureGeomDict[featureIdentifier] = vertices[0]
            
        return visualFeatureGeomDict
    
    def geometry(self, hazardEvent):
        for g in hazardEvent.getFlattenedGeometry().geoms:
            geometry = shapely.geometry.base.dump_coords(g)
        return geometry
 
    def executeFrom(self, dataList, eventSet, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, eventSet, prevDataList, False)
        else:
            self.updateExpireTimes(dataList)
        return dataList
