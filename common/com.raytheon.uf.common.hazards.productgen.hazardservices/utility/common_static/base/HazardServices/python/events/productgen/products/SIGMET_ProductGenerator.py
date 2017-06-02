"""
    Description: Product Generator for the SIGMET product.
    NOTE: This addresses the Hydrologic Outlook describing the 
    possibility of flooding on a near-term forecast horizon, 
    typically more than 24 hours from the event.
    
    It does not address the Water Supply Outlook or Probabilistic
    Hydrologic Outlook products which provide long-term forecast information 
    such as water supply forecasts and probabilistic analysis.  
    However, it could be used as a starting point for Focal Points in 
    developing these other types of ESF products.
"""
import os, types, sys, collections, re
from HydroProductParts import HydroProductParts
import HydroGenerator
from KeyInfo import KeyInfo
import shapely, time

class Product(HydroGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()  
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'SIGMET_ProductGenerator'
        
### FROM AAWUP TEXT FORMATTERS ###########
        self._AdvisoryType = ['SIGMET', 'CONVSIGMET', 'AIRMET', 'RADIOACTIVE',
            'VAA', 'TCA', 'NONE']
        self._HazardType = ['TS', 'SEV ICE', 'OCNL SEV TURB', 'DS', 'SS', 'VA',
            'ERUPTION', 'SSWIND', 'LLWS', 'IFR', 'MTNOBSC', 'FZLVL',
            'TROPCYCLONE', 'UNKNWN']
        self._HazardSubType = ['OBSC', 'EMBD', 'WDSPR', 'SQL', 'ISOL SEV',
            'FZRA', 'NOSUBTYPE']
        self._StatusList = ['NEW', 'AMENDED', 'CORRECTED', 'CORRECTEDAMENDED',
            'CANCELLED', 'EXPIRED']
        
        
        
        self._productLoc = 'ANC'      # product name 
        self._fullStationID = 'PAWU'    # full station identifier (4letter)
        self._wmoID = 'WSAK00'          # WMO ID
        self._pil = 'SIGAK'              # Product pil
        self._areaName = 'NONE'   # Name of state, such as 'GEORGIA' -- optional
        self._wfoCityState = 'NONE'  # Location of WFO - city state
        
        self._textdbPil = 'ANCFASIGAK1'      # Product ID for storing to AWIPS text database.
        self._awipsWANPil = 'PANCSIGAK1'   # Product ID for transmitting to AWIPS WAN.
        self._lineLength = 68           # line length


###################################################
        
                
    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD/Raytheon"
        metadata['description'] = "Product generator for SIGMET."
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
        self._productCategory = "SIGMET"
        self._areaName = '' 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        self._purgeHours = 8
        self._SIGMET_ProductName = 'SIGMET'
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
            
            
        for k in self._inputHazardEvents:
            print 'SIGMET_ProductGenerator.py Execute \n'
            print k
            print '\tType: ', type(k)
            print '\tAttrs:', k.getHazardAttributes()
            print '\tPhen:', k.getPhenomenon(), k.getSubType()
            hazGeometry = k.getFlattenedGeometry()
            print '\tGeom:', hazGeometry
            for g in hazGeometry.geoms:
                print shapely.geometry.base.dump_coords(g)
            self._formatAAWUProduct(k)
        self.flush()
        
        productDict = collections.OrderedDict()
        
        productDict['productID'] =  'SIGMET'
        productDict['productName']='SIGMET'
        
        fcst = self._preProcessProduct('', {})
        fcst = self._postProcessProduct(fcst, {})
        
        #productDict['text'] = 'Here is my SIGMET'
        fcst = str.upper(fcst)
        fcst = fcst.replace('NOT APPLICABLE', '')
        productDict['text'] = fcst

        return [productDict], self._inputHazardEvents

        #return productDicts, hazardEvents

    
    def _formatAAWUProduct(self, hazardEvent):
        attrs = hazardEvent.getHazardAttributes()
        phen = hazardEvent.getPhenomenon()
        sig = hazardEvent.getSignificance()
        subType = hazardEvent.getSubType()
        
        self._advisoryName = str.upper(attrs.get('AAWUAdvisoryType'))
        ### Maybe this is better to use?
        #self._advisoryName = phen
        
        self._cancelV = False
        if re.search('CANCELLED', self._advisoryName):
            self._cancelV = True

        bottom = attrs.get('AAWUVerticalExtentBottom_1')
        top = attrs.get('AAWUVerticalExtentTop_1')
        self._fLvls = bottom + '/' + top
        
        
        self._obsOrFcst = attrs.get('AAWUForecastOrObserved')
        self._intenTrend = attrs.get('AAWUIntensity')
        self._seriesName = attrs.get('AAWUAdvisorySeries')
        self._verNum = attrs.get('AAWUAdvisoryNumber')
        
        ### Need to add to HID for selection
        #self._formatType = ['Lat/Lon','VOR']
        #self._eruptionV = ['YES','NO']
        #self._formatFIR = ['Anchorage', 'Anchorage Arctic', 'Anchorage Oceanic','Petropavlovsk-Kamchatsky']
        self._formatType = 'Lat/Lon'
        self._formatFIR = 'Anchorage'
        self._hazardType = attrs.get('AAWUHazardType')
        self._subHazardType = attrs.get('AAWUHazardSubType')
        if self._subHazardType is not None:
            if re.search(self._subHazardType, 'pplicable'):
                self._subHazardType = ''

        if self._subHazardType and re.search('Freezing Rain', self._subHazardType):
            self._subHazardTypeAft = 'FZRA '
        else:
            self._subHazardTypeAft = ''
        
        self._numLayers = attrs.get('AAWUNumLayers')

        ### Need to acct for multiple with VAA
        dir = attrs.get('AAWUMovementToward_1')
        speed = attrs.get('AAWUMovementSpeed_1')
        if dir== 'STNR' or int(speed) == 0:
            self._movement = 'MOV STNR'
        else:    
            self._movement = ('MOV %s %sKT' % (dir, speed))

        ### Probably needs some formatting work.
        self._volName = 'N/A'
        self._vlatlon = 'N/A'
        volSelection = attrs.get('AAWUVolcanoName')
        if volSelection is not None:
            name,latlon,numbers = volSelection.split('\t\t')
            self._volName = name
            self._vlatlon = latlon.replace('/',' ')
            self._numTimes = [attrs.get('AAWUFcstPeriods_1')]
        
            self._movements = [self._movement]
            self._movements = [self._movement]

        
        self._eruptionV = attrs.get('AAWUEruption')
        if self._eruptionV is None:
            self._eruptionV = 'NO'
        self._prodType = attrs.get('AAWUProdType')
        if self._prodType == 'ASH' or self._prodType == 'Eruption' or self._prodType == 'ERUPTION':
            self._nwmoID = 'WVAK0'
            self._npil = 'WSVAK'
        else:
            self._nwmoID = 'WSAK0'
            self._npil = 'SIGAK'
        
        self._determineTimeRanges(hazardEvent)
        
        slatlon = ''
        hazGeometry = hazardEvent.getFlattenedGeometry()
        for g in hazGeometry.geoms:
             pointsList = shapely.geometry.base.dump_coords(g)
             for pt in pointsList:
                 lon = '%5.0f' % (float(pt[0])*100)
                 lat = '%4.0f' % (float(pt[1])*100)
                 
                 slat = lat.replace('-','S') if int(lat) < 0 else 'N'+lat
                 slon = lon.replace('-','W') if int(lon) < 0 else 'E'+lon
                 
                 if slatlon != '':
                    slatlon = slatlon + ' - '
                 slatlon = slatlon + slat + ' ' + slon
        self._strLatLon = slatlon + '.'
        print 'SIGMET_ProductGenerator.py _formatAAWUProduct \n'         
        print self._strLatLon
        print self._currentTime
        print self._initTimeZ
        print self._endTimeZ
                 
        
        
    def _determineTimeRanges(self, hazEvt):
    
        self._currentTime = time.mktime(hazEvt.getCreationTime().timetuple())
        
        
        epochStartTime = time.mktime(hazEvt.getStartTime().timetuple())
        epochEndTime = time.mktime(hazEvt.getEndTime().timetuple())
        print 'SIGMET_ProductGenerator.py _determineTimeRanges -- CT: ', self._currentTime, type(self._currentTime)
        print 'SIGMET_ProductGenerator.py _determineTimeRanges --ST: ', epochStartTime
        print 'SIGMET_ProductGenerator.py _determineTimeRanges --ET: ', epochEndTime
        
        self._ddhhmmTime = time.strftime('%d%H%M',time.gmtime(
            self._currentTime))
        
        
        if self._cancelV:
            self._initTimeZ = time.strftime('%d%H%M', \
                time.gmtime(self._currentTime + 5*60))
            self._endTimeZ = time.strftime('%d%H%M', \
                time.gmtime(self._currentTime + 20*60))
        else:
            self._initTimeZ = time.strftime('%d%H%M',time.gmtime(epochStartTime))
            self._endTimeZ = time.strftime('%d%H%M',time.gmtime(epochEndTime))

        return


    def generateForecast(self, argDict):
        # Generate Text Phrases for a list of edit areas
        print '========= start generate forecast product ============='
        # Get variables
        fcst = ''
        
#        error = self._getVariables(argDict)
#        if error is not None:
#            return error

        try:
            self._eruptionV
        except NameError:
            self._eruptionV = 'NO'

        print 'SIGMET_ProductGenerator generateForecast -- self._advisoryName1: ', self._advisoryName
        self._cancelV = False
        if re.search('CANCELLED', self._advisoryName):
            self._cancelV = True
        print 'SIGMET_ProductGenerator generateForecast -- self._cancelV: ', self._cancelV        
        self._advisoryName = self._advisoryName.replace('VASIGMET','SIGMET')
        if (self._advisoryName != 'TEST' and self._advisoryName != 'TEST VA'):
            self._advisoryName = self._sType[self._advisoryName]


        if re.search('ASH',self._advisoryName):
            error = self._getVAAInfo()
        elif re.search('ERUPTION',self._advisoryName):
            error = self._getEruptionInfo()   
        elif re.search('RAD',self._advisoryName):
            error = self._getVAAInfo()
        else:
            if re.search('TEST',self._advisoryName):
                self._prodType = 'TEST'
                if re.search('ASH',self._advisoryName):
                    self._testType = 'ASH'
                else:
                    self._testType = 'NORM'
            else:
                error = self._getAdvisoryInfo()                
        if error is not None:
            return error

        error = self._determineTimeRanges(argDict)
        if error is not None:
            return error
                      
        # Sample the data
#        error = self._sampleData(argDict)
#        if error is not None:
#            return error

        # Generate the product
        fcst = self._preProcessProduct('', argDict)

        fcst = self._postProcessProduct(fcst, argDict)

        return fcst


    def _preProcessProduct(self, fcst, argDict):
        # Add product headers

        if self._prodType == 'TEST':
            self._seriesName = 'INDIA'
            self._verNum = 1
            if self._testType == 'NORM':
                self._nwmoID = 'WSAK0'
                self._npil = 'SIGAK'
            else:    
                self._nwmoID = 'WVAK0'
                self._npil = 'WSVAK'
            
        ascNum = ord(self._seriesName[0]) - 72
        if ascNum < 0 or ascNum > 5:
            ascNum = 1
        productName = '%s%s WS %s' % (self._productLoc.strip(),
            self._seriesName[0], self._initTimeZ)

        #productName = self.checkTestMode(argDict, productName)

        fcst = '%s%s%s %s %s\n%s%s\n%s\n' % (fcst, self._nwmoID, str(ascNum),
            self._fullStationID, self._ddhhmmTime, self._npil, str(ascNum),
            productName)

        fcst = '%sPAZA SIGMET %s %s VALID %s/%s PANC-\n' % (fcst,
            self._seriesName, str(self._verNum), self._initTimeZ, self._endTimeZ) 

        if self._cancelV:
            fcst = fcst + str.upper(self._formatFIR) + ' FIR.\n'
            fcst = fcst + 'CNL PAZA SIGMET ' + self._seriesName + ' '
            fcst = fcst + str(self._verNum-1) + ' WEF ' + self._initTimeZ + '.\n'
            #fcst = fcst + '*** ADD ANY REMARKS HERE...DELETE IF NOT USED***\n'

        elif self._prodType == 'TEST':
            fcst = fcst + 'ANCHORAGE FIR.\n\nTHIS IS A TEST. THIS IS A TEST.\n\n'
            fcst = fcst + '*** ADD ANY REMARKS HERE...DELETE IF NOT USED***\n'
        else:
            if self._prodType == 'ASH':
        
                fcst = fcst + str.upper(self._formatFIR) + ' FIR VA ERUPTION '
                fcst = fcst + self._volName + ' VOLCANO PSN ' + self._vlatlon + '\n'
                
                for k in range(2):
                    for l in range(self._numLayers):
                        lout = 1
                        linea = ''
#                       linea = linea + self._subHazardType + self._hazardType + \
#                          self._subHazardTypeAft + str.upper(self._obsOrFcst) + ' '
                        if k == 0 and l == 0:
                            linea = '%sVA CLDS OBS AT %sZ WI' % (linea,
                                self._initTimeZ[2:])
                        if k == 0 and l > 0:
                            linea = linea + 'AND WI '
                        if k >= int(self._numTimes[l]):
                            if k == 1 and l == 0:
                                linea = '%sFCST %sZ ' % (linea, self._endTimeZ[2:])      
                            if self._numLayers > 1:
                                if k <= self._maxNT:
                                    linea = '%sNO VA EXP. %s.' % (linea, self._fLvls[l])
                                elif k > self._maxNT and l == 0:
                                    linea = linea + 'NO VA EXP.'
                                else:
                                    lout = 0
                            else:
                                linea = linea + 'NO VA EXP.'
                        else:
                            if k == 1 and l == 0:
                                linea = '%sFCST %sZ VA CLD WI ' % (linea,
                                    self._endTimeZ[2:])
                            if k == 1 and l > 0:
                                linea = linea + 'AND WI '
                            linea = '%s%s %s.' % (linea, self._strLatLon[l][k],
                                self._fLvls[l])
                        if k == 0:
                            linea = '%s %s. %s.' % (linea, self._movements[l],
                                self._intenTrend)
                        if lout == 1:    
                            newlinea = self._wordWrap(linea)
                            fcst = fcst + newlinea + '\n'
                
                fcst = fcst + '*** ADD ANY REMARKS HERE...DELETE IF NOT USED***\n'
                
            elif re.search('ERUPTION',self._advisoryName):
                fcst = '%s%s FIR VA ERUPTION %s VOLCANO PSN %s\nESTIMATED ASH TOP ' \
                '%s.\n*** MT-SAT/GOES/POES/AVO/KVERT/PILOT REPORT/RADAR ' \
                '{Modify as Needed or Delete}\n' \
                '*** ERUPTION DETAILS: {Modify as Needed or Delete}\n' \
                '*** ADD AY ADDITIONAL REMARKS HERE...DELETE IF NOT USED***\n' \
                % (fcst, str.upper(self._formatFIR), self._volName,
                self._vlatlon, self._fLvls)
                
            elif self._prodType == 'RAD':
                for k in range(2):
                    for l in range(self._numLayers):
                        linea = ''
                        if k == 0:
                            linea = '%s%s FIR RDOACT CLD ' % (linea,
                                str.upper(self._formatFIR))
                        if k == 0 and l == 0:
                            linea = '%sFCST AT %sZ WI ' % (linea,
                                self._initTimeZ[2:])
                        if k >= int(self._numTimes[0]):
                            linea = '%sFCST %sZ NO RDOACT CLD EXP.' % (linea,
                                self._endTimeZ[2:])
                        else:
                            if k == 1 and l == 0:
                                linea = '%sFCST %sZ RDOACT CLD APRX ' % (linea,
                                    self._endTimeZ[2:])      
                            linea = linea + self._strLatLon[l][k]
                        if k == 0:
                            linea = '%s %s. %s. %s.' % (linea, self._fLvls[0],
                                self._movements[l], self._intenTrend)
                        if k == 1:
                            linea = '%s RADIOLOGIC RELEASE NEAR %s.' % (linea,
                                self._rlatlon)
                        newlinea = self._wordWrap(linea)
                        fcst = fcst + newlinea + '\n'
                
                fcst = fcst + '*** ADD ANY REMARKS HERE...DELETE IF NOT USED***\n'           

            else:
                if self._formatType == 'Lat/Lon':

                    linea = str.upper(self._formatFIR) + ' FIR '
                    linea = linea + self._subHazardType + ' ' + str.upper(self._hazardType) + ' ' +\
                        self._subHazardTypeAft + str.upper(self._obsOrFcst) + ' '
                    if self._obsOrFcst == 'Obs':
                        linea = linea + 'AT ' + self._initTimeZ[2:] + 'Z '
                    linea = linea + 'WI '    
                    linea = linea + self._strLatLon + ' ' + self._fLvls + '.'
                    linea = linea + ' ' + self._movement + '. ' + str.upper(self._intenTrend) + '.'
                    newlinea = self._wordWrap(linea)
                    fcst = fcst + newlinea + '\n'
                    fcst = fcst + '*** ADD ANY REMARKS HERE...DELETE IF NOT USED***\n'
                else:   #VOR
                    fcst = fcst + 'ANCHORAGE FIR.\n'
                    linea = self._subHazardType + self._hazardType + \
                        self._subHazardTypeAft
                    linea = linea + self._fLvls + ' AREA WI ' + self._strLatLon
                    linea = linea + ' ' + self._movement + '. ' + self._intenTrend + '.'
                    newlinea = self._wordWrap(linea)
                    fcst = fcst + newlinea + '\n'
                    fcst = fcst + '*** ADD ANY REMARKS HERE...DELETE IF NOT USED***\n'

        print 'SIGMET_ProductGenerator preProcessProduct -- fcst:', fcst
        
        return fcst



    def _postProcessProduct(self, fcst, argDict):
        endTimeStr = time.strftime('%b %Y',time.gmtime(
            self._currentTime))
      
        #userName = os.environ['USER']
        #fcst = fcst + forecasters[userName] +' ' + endTimeStr + ' AAWU\n'
        userName = 'KLM'
        fcst = fcst + userName +' ' + str.upper(endTimeStr) + ' AAWU\n'
#        print aa
        return fcst

#

    def _wordWrap(self, string, width=66):
        newstring = ''
        if len(string) > width:
            while True:
                # find position of nearest whitespace char to the left of 'width'
                marker = width-1
                while not string[marker].isspace():
                    marker = marker - 1

                # remove line from original string and add it to the new string
                newline = string[0:marker] + '\n'
                newstring = newstring + newline
                string = string[marker+1:]

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
WSAK01 PAWU 161707
SIGAK1
ANCI WS 161350
PAZA SIGMET INDIA 2 VALID 161350/161748 PANC-
ANCHORAGE FIR SQL TS OBS AT 1350Z WI N6302 W17908 - N5840 W17726
- N5248 W17817 - N4942 E17958 - N4908 W17647 - N5412 W17314 -
N5904 W17238 - N6332 W17441 - N6302 W17908. MAX TOP FL420. MOV
ENE 1KT. INTSF.
*** ADD ANY REMARKS HERE...DELETE IF NOT USED***
KLM JUN 2015 AAWU
*|
         '''  
        productDict['narrativeForecastInformation'] = self._section.hazardEvent.get('narrativeForecastInformation', default)

    def executeFrom(self, dataList, eventSet, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, eventSet, prevDataList, False)
        else:
            self.updateExpireTimes(dataList)
        return dataList