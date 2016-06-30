import FormatTemplate

import types, re, sys, collections, os
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
from collections import OrderedDict

OUTPUTDIR = '/scratch/convectiveSigmetTesting'

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self):
        super(Format, self).initialize()
        self.initProductPartMethodMapping()

        self._productGeneratorName = 'Convective_SIGMET_ProductGenerator' 
        
    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'easMessage': self._easMessage,
            'productHeader': self._productHeader,
            'narrativeForecastInformation': self._narrativeForecastInformation
                                }
        
    def execute(self, productDict, editableEntries=None):    
        self.productDict = productDict
        self._editableParts = OrderedDict()
        
        parts = self.productDict.get('productParts')
        events = self.productDict.get('events')

        self._fcstList = {}
        
        for event in events:
            self._formatConvectiveSigmet(event)
            self._fcst = self._preProcessProduct(event, '', {})
            self._fcst = str.upper(self._fcst)
            self._fcstList[event['sigmetNumber']+event['domain']] = self._fcst
            self.flush()
            
        productDict = self._outputFormatter(events, self._fcstList)
        self._outputText(productDict)
        
        legacyText = productDict.get('text')

        return [ProductUtils.wrapLegacy(legacyText)],self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level
    def _formatConvectiveSigmet(self, event):                
        self._SIGMET_ProductName = 'CONVECTIVE SIGMET'
        self._convectiveSigmetNumberStr = event['sigmetNumber']
        self._convectiveSigmetDomain = event['domain']
        self._endTime = event['parts']['endTime']
        self._statesListStr = event['parts']['states']
        self._boundingStatement = event['parts']['boundingStatement']
        self._convectiveSigmetModifierStr = event['parts']['modifier']
        self._convectiveSigmetModeStr = event['parts']['mode']
        self._hazardEmbeddedStr = event['parts']['embedded']
        self._hazardMotionStr = event['parts']['motion']
        self._convectiveSigmetCloudTopStr = event['parts']['cloudTop']
        self._convectiveSigmetAdditionalHazardsStr = event['parts']['additionalHazards']
        self._currentTime = event['parts']['currentTime']
        self._startTime = event['parts']['startTime']
        self._startDate = event['parts']['startDate']                
        self._endDate = event['parts']['endDate']
        
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
        
        if event['parts']['specialTime'] is not None:
            self._specialTime = event['parts']['specialTime']          
                    
    def _outputFormatter(self, events, fcstList):                         
        fcstDict = self._createFcstDict(events, fcstList)                             
        fcstDict = self._orderFcstDict(fcstDict)
        headerDict = self._createHeader(fcstDict)
        fcst = self._createFcst(fcstDict, headerDict)
        
        productDict = collections.OrderedDict()        
        productDict['productID'] = 'SIGMET.Convective'
        productDict['productName'] = 'CONVECTIVE SIGMET'        
        productDict['text'] = fcst            
            
        return productDict
    
    def _createFcstDict(self, events, fcstList):        
        self._domains = ['East', 'Central', 'West']

        fcstDict = collections.OrderedDict()
        for domain in self._domains:
            fcstDict[domain] = {}
            fcstDict[domain]['special']= {}
            fcstDict[domain]['regular']= {}
            for event in events:
                numberStr = event['sigmetNumber']
                number = int(numberStr)              
                if event['domain'] == domain and event['specialIssuance'] == True:
                    fcst = fcstList[numberStr+domain]
                    fcstDict[domain]['special'][number] = fcst
                elif event['domain'] == domain and event['specialIssuance'] == False:
                    fcst = fcstList[numberStr+domain]
                    fcstDict[domain]['regular'][number] = fcst
                else:
                    continue
                                                      
        return fcstDict 
    
    def _orderFcstDict(self,fcstDict):    
        
        for domain in self._domains:
            if len(fcstDict[domain]['special'].keys()):
                fcstDict[domain]['special'] = collections.OrderedDict(sorted(fcstDict[domain]['special'].items()))
            if len(fcstDict[domain]['regular'].keys()):
                fcstDict[domain]['regular'] = collections.OrderedDict(sorted(fcstDict[domain]['regular'].items()))                       
        
        return fcstDict            

    def _createFcst(self, fcstDict, headerDict):
        fcst = ''
        
        for key in fcstDict:
            fcst = fcst + headerDict[key] + '\n'
            if len(fcstDict[key]['special'].keys()):
                for entry in fcstDict[key]['special']:
                    fcst = fcst + fcstDict[key]['special'][entry] + '\n\n'
            if len(fcstDict[key]['regular'].keys()):
                for entry in fcstDict[key]['regular']:
                    fcst = fcst + fcstDict[key]['regular'][entry] + '\n\n'
            if not len(fcstDict[key]['regular'].keys()) and not len(fcstDict[key]['special'].keys()):
                fcst = fcst + 'CONVECTIVE SIGMET...NONE\n\n'
            fcst = fcst + 'NNNN' + '\n\n'      
        
        return fcst         
    
    def _createHeader(self, fcstDict):        
        domainDict = {'East': 'E', 'Central': 'C', 'West': 'W'}
        headerDict = {}
        
        for domain in self._domains:
            if len(fcstDict[domain]['special'].keys()):
                header = '%s %s%s %s %s\n' % (self._zczc, self._productLoc, self._pilDict[domainDict[domain]],
                    self._all, self._specialTime)            
                header = '%s%s %s %s\n' % (header, self._wmoHeaderDict[domainDict[domain]], self._fullStationID,
                    self._specialTime)
                header = '%s%s%s %s %s' % (header, self._productLoc, domainDict[domain],
                    self._productIdentifier, self._specialTime)
            else:
                header = '%s %s%s %s %s\n' % (self._zczc, self._productLoc, self._pilDict[domainDict[domain]],
                    self._all, self._startTime)
                header = '%s%s %s %s\n' % (header, self._wmoHeaderDict[domainDict[domain]], self._fullStationID,
                    self._startTime)
                header = '%s%s%s %s %s' % (header, self._productLoc, domainDict[domain],
                    self._productIdentifier, self._startTime)
            
            headerDict[domain] = header
                  
        return headerDict    
    
    def _outputText(self, productDict):              
        outDirAll = os.path.join(OUTPUTDIR, self._startDate)
        outAllAdvisories = 'convectiveSIGMET_' + self._startDate + '.txt'
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
        domainDict = {'East': 'E', 'Central': 'C', 'West': 'W'}
        self._convectiveSigmetDomainStr = domainDict[self._convectiveSigmetDomain]
        
        fcst = '%s%s %s%s\n' % (fcst, self._SIGMET_ProductName, self._convectiveSigmetNumberStr, self._convectiveSigmetDomainStr)        
        fcst = fcst + 'VALID UNTIL ' + self._endTime + 'Z\n'
        fcst = '%s%s\n' % (fcst, self._statesListStr)
        fcst = '%s%s\n' % (fcst, self._boundingStatement)
        fcst = '%s%s%s %s%s %s' % (fcst, self._convectiveSigmetModifierStr, self._convectiveSigmetModeStr, self._hazardEmbeddedStr,
            self._hazardMotionStr, self._convectiveSigmetCloudTopStr)
        
        if len(self._convectiveSigmetAdditionalHazardsStr):
            fcst = '\n%s%s' % (fcst, self._convectiveSigmetAdditionalHazardsStr)
        
        fcst = fcst.replace("_", " ")       
        
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

    ################# Segment Level

    ################# Section Level    
    def _narrativeForecastInformation(self, segmentDict):
        text = ''
        narrative = segmentDict.get('narrativeForecastInformation')
        if narrative:
            text = narrative
            text += '\n\n'
        return text