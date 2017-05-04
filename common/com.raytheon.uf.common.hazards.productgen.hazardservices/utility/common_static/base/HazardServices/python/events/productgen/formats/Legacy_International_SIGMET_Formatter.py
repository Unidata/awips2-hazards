import FormatTemplate

import types, re, sys, collections, os
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
from collections import OrderedDict
import Domains
import AviationUtils

OUTPUTDIR = '/scratch/internationalSIGMET'

'''
WSNT01 KKCI 261700
SIGA0A
KZWY SIGMET ALFA 1 VALID 261700/262100 KKCI-
KZWY NEW YORK OCEANIC FIR OBSC TS OBS AT 1700Z WI N4730 W10145 -
N4430 W10130 - N4445 W9915 - N4715 W9830. TOP FL300.
MOV ESE 20KT. INTSF.
'''

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self):
        super(Format, self).initialize()
        self.initProductPartMethodMapping()

        self._productGeneratorName = 'International_SIGMET_ProductGenerator' 
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
        
        print "Legacy_Convective_SIGMET_Formatter - execute - eventDicts: ", eventDicts

        self._fcstList = {}
        
        for eventDict in eventDicts:
            self._issueFlag = eventDict.get('issueFlag',False)
            self._status = eventDict.get('status',False)
            eventDictParts = eventDict.get('parts')            
            self._sequenceNumber = str(eventDictParts.get('sequenceNumber'))
            self._sequenceName = eventDictParts.get('sequenceName')
            
            header = self.createHeader(eventDict)
            body = self.createBody(eventDict)
            productDict = self.createProduct(header, body)
            self.flush()
        
            if self._status == "PENDING" or self._status == "ISSUED":
                if self._issueFlag == "True":
                    self.outputText(productDict)
        
        legacyText = productDict.get('text')

        return [ProductUtils.wrapLegacy(legacyText)],self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################
    def _formatConvectiveSigmet(self, eventDict):                
        self._SIGMET_ProductName = 'International SIGMET'
        
    def createHeader(self, eventDict):
        eventDictParts = eventDict.get('parts')
        
        header = '%s %s %s\n' % (eventDictParts.get('wmoHeader'), eventDictParts.get('originatingOffice'), eventDictParts.get('startTime'))
        header = '%s%s\n' % (header, eventDictParts.get('awipsHeader'))
        header = '%s%s %s %s %s %s %s%s%s %s%s\n' % (header, eventDictParts.get('firAbbreviation'), 'SIGMET', eventDictParts.get('sequenceName'),
                                                     eventDictParts.get('sequenceNumber'), 'VALID', eventDictParts.get('startTime'), '/',
                                                     eventDictParts.get('endTime'), eventDictParts.get('originatingOffice'), '-')
            
        return header                                    

    def createBody(self,eventDict):
        eventDictParts = eventDict.get('parts')
        
        cancellation = eventDictParts.get('cancellation')
        if cancellation:
            body = '%s %s %s %s %s %s%s%s %s' % (eventDictParts.get('firAbbreviation'), eventDictParts.get('firName'), 'CNL SIGMET',
                                       eventDictParts.get('sequenceName'), str(int(eventDictParts.get('sequenceNumber'))-1),
                                       eventDictParts.get('previousStartTime'), '/', eventDictParts.get('previousEndTime'),
                                       eventDictParts.get('additionalRemarks'))
        else:
            if eventDictParts.get('phenomenon') == 'VA ERUPTION':
                volcanoProductPartsDict = eventDictParts.get('volcanoProductPartsDict')
                if volcanoProductPartsDict['type'] == 'volcanicAsh':
                    body = '%s %s %s %s %s %s%s %s' % (eventDictParts.get('firName'), eventDictParts.get('phenomenon'),
                                         volcanoProductPartsDict['name'], volcanoProductPartsDict['position'],
                                         'VA CLDS OBS AT', eventDictParts.get('startTime')[2:],'Z', eventDictParts.get('location'))
                    
                    numLayers = volcanoProductPartsDict['numLayers']
                    
                    for i in range(0,numLayers):
                        if i == 0:
                            body = '%s %s%s%s%s %s' % (body, volcanoProductPartsDict['layer'+str(i+1)]['bottom'],'/',volcanoProductPartsDict['layer'+str(i+1)]['top'],'.',
                                                    volcanoProductPartsDict['layer'+str(i+1)]['motion'])
                            body = '%s\n%s %s%s %s %s%s%s%s' % (body, 'FCST', eventDictParts.get('endTime')[2:],'Z VA CLD',
                                                       volcanoProductPartsDict['vaFcstPoly1'], volcanoProductPartsDict['layer'+str(i+1)]['bottom'],
                                                       '/',volcanoProductPartsDict['layer'+str(i+1)]['top'],'.')
                        else:
                            body = '%s\n%s %s %s%s%s%s' % (body, 'AND', volcanoProductPartsDict['vaFcstPoly'+str(i+1)], volcanoProductPartsDict['layer'+str(i+1)]['bottom'],
                                                             '/',volcanoProductPartsDict['layer'+str(i+1)]['top'],'.')                                        
                else:
                    body = '%s %s %s %s %s' % (eventDictParts.get('firName'),eventDictParts.get('phenomenon'),
                                               volcanoProductPartsDict['name'], volcanoProductPartsDict['position'],
                                               eventDictParts.get('verticalExtent'))
                    body = '%s %s %s %s %s' % (body, volcanoProductPartsDict['indicator'], 'INDICATE AN ERUPTION AT',
                                         volcanoProductPartsDict['time'], 'UTC.')
                    if eventDictParts.get('additionalRemarks'):
                        body = '%s %s' % (body, eventDictParts.get('additionalRemarks'))   
            elif eventDictParts.get('phenomenon') == 'TC':
                tropicalCycloneProductPartsDict = eventDictParts.get('tropicalCycloneProductPartsDict')
                body = '%s %s %s %s' % (eventDictParts.get('firAbbreviation'), eventDictParts.get('firName'),
                                     eventDictParts.get('phenomenon'), tropicalCycloneProductPartsDict['name'])
                body = '%s %s %s %s %s %s' % (body, tropicalCycloneProductPartsDict['observationTime'],
                                              tropicalCycloneProductPartsDict['centerLocation'], eventDictParts.get('movement'),
                                              eventDictParts.get('intensityTrend'), eventDictParts.get('verticalExtent'))
                if eventDict.get('geomType') == 'Point':
                    body = '%s %s %s' % (body, tropicalCycloneProductPartsDict['radius'],
                                         tropicalCycloneProductPartsDict['fcstPosition'])
                else:
                    body = '%s %s %s' % (body, eventDictParts.get('location'), tropicalCycloneProductPartsDict['fcstPosition'])              
            else:            
                body = '%s %s %s %s %s' % (eventDictParts.get('firAbbreviation'), eventDictParts.get('firName'),
                                             eventDictParts.get('phenomenon'), eventDictParts.get('forecastObserved'),
                                             eventDictParts.get('location'))
                body = '%s %s %s %s' % (body, eventDictParts.get('verticalExtent'), eventDictParts.get('movement'),
                                           eventDictParts.get('intensityTrend'))
                
                if eventDictParts.get('additionalRemarks'):
                    body = '%s %s' % (body, eventDictParts.get('additionalRemarks'))

        return body
                    
    def createProduct(self, header, body):                       
        body = self.wordWrap(body)
        fcst = header + body
        
        productDict = collections.OrderedDict()        
        productDict['productID'] = 'SIGMET.International'
        productDict['productName'] = 'INTERNATIONAL SIGMET'        
        productDict['text'] = fcst            
            
        return productDict
    
    def volcanoDetails(self):
        volcanoDict = AviationUtils.AviationUtils().createVolcanoDict()
        
        return volcanoDict
    
    def wordWrap(self, string, width=68):
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
    
    def outputText(self, productDict):            
        outAllAdvisories = 'internationalSIGMET_' + self._sequenceName + self._sequenceNumber + '.txt'
        pathAllFile = os.path.join(OUTPUTDIR, outAllAdvisories)
        
        if not os.path.exists(OUTPUTDIR):
            try:
                os.makedirs(OUTPUTDIR)
            except:
                sys.stderr.write('Could not create output directory')
                
        with open(pathAllFile, 'w') as outFile:
            outFile.write(productDict['text'])
    
        return