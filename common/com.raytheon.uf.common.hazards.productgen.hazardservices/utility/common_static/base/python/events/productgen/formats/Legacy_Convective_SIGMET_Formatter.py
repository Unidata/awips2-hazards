import FormatTemplate

import types, re, sys, collections, os
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
from collections import OrderedDict
import Domains

OUTPUTDIR = '/scratch/convectiveSigmetTesting'

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self):
        super(Format, self).initialize()
        self.initProductPartMethodMapping()

        self._productGeneratorName = 'Convective_SIGMET_ProductGenerator' 
        self._domains = Domains.Domains()
        
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
        
        domains = Domains.AviationDomains
        
        parts = self.productDict.get('productParts')
        eventDicts = self.productDict.get('eventDicts')

        self._fcstList = {}
        
        for eventDict in eventDicts:
            self._issueFlag = eventDict.get('issueFlag',False)
            self._status = eventDict.get('status',False)
            self._formatConvectiveSigmet(eventDict)
            self._fcst = self._preProcessProduct('', domains)
            self._fcst = str.upper(self._fcst)
            self._fcstList[eventDict['sigmetNumber']+eventDict['domain']] = self._fcst
            self.flush()
            
        productDict = self._assembleProduct(eventDicts, self._fcstList, domains)
        
        if self._status == "PENDING" or self._status == "ISSUED":
            if self._issueFlag == "True":
                self._outputText(productDict)
        
        legacyText = productDict.get('text')

        return [ProductUtils.wrapLegacy(legacyText)],self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################
    def _formatConvectiveSigmet(self, eventDict):                
        self._SIGMET_ProductName = 'CONVECTIVE SIGMET'
        self._convectiveSigmetNumberStr = eventDict['sigmetNumber']
        self._convectiveSigmetDomain = eventDict['domain']
        
        eventDictParts = eventDict.get('parts')
        
        self._statesListStr = eventDictParts.get('states','NULL')
        self._convectiveSigmetModifierStr = eventDictParts.get('modifier','')
        self._hazardMotionStr = eventDictParts.get('motion','MOV LTL.')
        self._convectiveSigmetCloudTopStr = eventDictParts.get('cloudTop','TOPS ABV FL450.')
        self._startTime = eventDictParts.get('startTime','DDHHMM')
        self._endTime = eventDictParts.get('endTime','DDHHMM')      
        self._startDate = eventDictParts.get('startDate','YYYYMMDD_HHmm')               
        self._endDate = eventDictParts.get('endDate','YYYYMMDD_HHmm')
        self._vertices = eventDictParts.get('geometry',None)
        self._boundingStatement = eventDictParts.get('boundingStatement','')
        self._convectiveSigmetModeStr = eventDictParts.get('mode','AREA')
        self._hazardEmbeddedStr = eventDictParts.get('embedded','')
        self._convectiveSigmetAdditionalHazardsStr = eventDictParts.get('additionalHazards','')
        self._currentTime = eventDictParts.get('currentTime','DDHHMM')
        
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
        
        if eventDictParts.get('specialTime') is not None:
            self._specialTime = eventDictParts.get('specialTime')          
                    
    def _assembleProduct(self, eventDicts, fcstList, domains):                       
        fcstDict = self._createFcstDict(eventDicts, fcstList, domains)                             
        fcstDict = self._orderFcstDict(fcstDict, domains)
        headerDict = self._createHeader(fcstDict, domains)
        fcst = self._createFcst(fcstDict, headerDict)
        
        productDict = collections.OrderedDict()        
        productDict['productID'] = 'SIGMET.Convective'
        productDict['productName'] = 'CONVECTIVE SIGMET'        
        productDict['text'] = fcst            
            
        return productDict
    
    def _createFcstDict(self, eventDicts, fcstList, domains):        
        fcstDict = collections.OrderedDict()
        for domain in domains:
            fcstDict[domain.domainName()] = {}
            fcstDict[domain.domainName()]['special']= {}
            fcstDict[domain.domainName()]['regular']= {}
            for event in eventDicts:
                numberStr = event.get('sigmetNumber',None)
                number = int(numberStr)              
                if event.get('domain') == domain.domainName() and event.get('specialIssuance') == True:
                    fcst = fcstList[numberStr+(domain.domainName())]
                    fcstDict[domain.domainName()]['special'][number] = fcst
                elif event.get('domain') == domain.domainName() and event.get('specialIssuance') == False:
                    fcst = fcstList[numberStr+(domain.domainName())]
                    fcstDict[domain.domainName()]['regular'][number] = fcst
                else:
                    continue
                                                      
        return fcstDict 
    
    def _orderFcstDict(self,fcstDict, domains):    
        for domain in domains:
            if len(fcstDict[domain.domainName()]['special'].keys()):
                fcstDict[domain.domainName()]['special'] = collections.OrderedDict(sorted(fcstDict[domain.domainName()]['special'].items()))
            if len(fcstDict[domain.domainName()]['regular'].keys()):
                fcstDict[domain.domainName()]['regular'] = collections.OrderedDict(sorted(fcstDict[domain.domainName()]['regular'].items()))                       
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
    
    def _createHeader(self, fcstDict, domains):        
        headerDict = {}

        for domain in domains:
            if len(fcstDict[domain.domainName()]['special'].keys()):
                timeStr = self._specialTime
            else:
                timeStr = self._startTime
            header = '%s %s%s %s %s\n' % (self._zczc, self._productLoc, domain.pil(),
                self._all, timeStr)            
            header = '%s%s %s %s\n' % (header, domain.wmoHeader(), self._fullStationID,
                timeStr)
            header = '%s%s%s %s %s' % (header, self._productLoc, domain.abbrev(),
                self._productIdentifier, timeStr)
            headerDict[domain.domainName()] = header       
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
            
    def _preProcessProduct(self,fcst,domains):
        for domain in domains:
            if domain.domainName() == self._convectiveSigmetDomain:
                self._convectiveSigmetDomainStr = domain.abbrev()
        
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