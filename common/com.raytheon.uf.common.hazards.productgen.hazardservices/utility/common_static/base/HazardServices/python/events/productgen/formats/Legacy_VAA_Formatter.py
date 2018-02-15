import sys, collections, os
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
from collections import OrderedDict
import AviationUtils


OUTPUTDIR = AviationUtils.AviationUtils().outputVAAFilePath()

'''
FVAK21 PAWU 261700
VAAAK1
VA ADVISORY
DTG: 20170405/1700Z
VAAC: ANCHORAGE
VOLCANO: ADAGDAK 311800
PSN: N5198 W17659
AREA: Aleutian Islands
SUMMIT ELEV: 2001 FT (610 M)
ADVISORY NR: 2017/002
INFO SOURCE: AVO
AVIATION COLOR CODE: RED
ERUPTION DETAILS: VA CONTINUOUSLY OBS ON SATELLITE IMAGERY
OBS VA DTG: 05/1700Z
RMK: ***ADD CUSTOM REMARKS HERE...DELETE IF NOT USED***
NXT ADVISORY: WILL BE ISSUED BY YYYYMMDD/HHmmZ
### APR 2017 AAWU
'''

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self):
        super(Format, self).initialize()

        self._productGeneratorName = 'VAA_ProductGenerator' 
        
    def execute(self, productDict, editableEntries=None):
        self.productDict = productDict
        self._editableParts = []
        parts = self.productDict.get('productParts')
        eventDicts = self.productDict.get('eventDicts')
        
        self._monthAbbrev = self.getMonthAbbrev()
        
        for eventDict in eventDicts:
            self._issueFlag = eventDict.get('issueFlag',False)
            
            productDict = self.createProduct(eventDict)

            if self._issueFlag == "True":
                self.outputJSON(productDict)
                self.outputText(productDict)
                
        legacyText = productDict.get('text')
 
        self.flush()
        return [ProductUtils.wrapLegacy(legacyText)],self._editableParts

    ######################################################
    def getMonthAbbrev(self):
        import datetime
        now = datetime.datetime.now()
        month = now.strftime("%b")
        return month.upper()
                           
    def createProduct(self, eventDict):
        fcst = ''
        eventDictParts = eventDict.get('parts')
        
        self._volcanoName = eventDictParts.get('volcanoName')
        self._advisoryNumber = eventDictParts.get('advisoryNumber')
        numLayers = eventDictParts.get('numLayers')
        productType = eventDictParts.get('productType')
        
        if eventDictParts.get('volcanoHeader') is True:
            fcst = fcst + eventDictParts.get('volcanoHeaderNumber') + ' PAWU ' + eventDictParts.get('currentTime') + '\n'
        else:
            fcst = fcst + 'FVAK21 PAWU ' + eventDictParts.get('currentTime') + '\n'
        fcst = fcst + 'VAAAK1' + '\n'
        fcst = fcst + 'VA ADVISORY' + '\n\n'
        fcst = fcst + 'DTG: ' + eventDictParts.get('dateStr')+'/'+eventDictParts.get('startTime')+'Z\n\n'
        fcst = fcst + 'VAAC: ' + eventDictParts.get('vaacOffice')+'\n\n'
        fcst = fcst + 'VOLCANO: '+self._volcanoName+' '+eventDictParts.get('volcanoNumber')+'\n\n'
        fcst = fcst + 'PSN: ' + eventDictParts.get('volcanoLatLon')+'\n\n'
        fcst = fcst + 'AREA: ' + eventDictParts.get('volcanoSubregion') + '\n\n'
        fcst = fcst + 'SUMMIT ELEV: ' + eventDictParts.get('volcanoElevation') + '\n\n'
        fcst = fcst + 'ADVISORY NR: ' + eventDictParts.get('dateStr')[:4] + '/' + self._advisoryNumber + '\n\n'
        
        if productType in ['VAA', 'Resuspended Ash']:
            fcst = fcst + 'INFO SOURCE: ' + eventDictParts.get('informationSource') + '\n\n'
            fcst = fcst + 'AVIATION COLOR CODE: ' + eventDictParts.get('volcanoStatus') + '\n\n'
            fcst = fcst + 'ERUPTION DETAILS: ' + eventDictParts.get('eruptionDetails') + '\n\n'
            fcst = fcst + 'OBS VA DTG: ' + eventDictParts.get('dateStr')[-2:] + '/' + eventDictParts.get('startTime') + 'Z\n\n'
            fcst = fcst + 'OBS VA CLD: ' + eventDictParts.get('layer1Location') + ' ' + eventDictParts.get('layer2Location') + eventDictParts.get('layer3Location') + '\n\n'
            
            if numLayers in [0, 1]:
                fcst = fcst + 'FCST VA CLD +6HR: ' + eventDictParts.get('layer1Forecast6') + '\n\n'
                fcst = fcst + 'FCST VA CLD +12HR: ' + eventDictParts.get('layer1Forecast12') + '\n\n'
                fcst = fcst + 'FCST VA CLD +18HR: ' + eventDictParts.get('layer1Forecast18') + '\n\n'
            elif numLayers == 2:
                fcst = fcst + 'FCST VA CLD +6HR: ' + eventDictParts.get('layer1Forecast6') + eventDictParts.get('layer2Forecast6') + '\n\n'
                fcst = fcst + 'FCST VA CLD +12HR: ' + eventDictParts.get('layer1Forecast12') + eventDictParts.get('layer2Forecast12') + '\n\n'
                fcst = fcst + 'FCST VA CLD +18HR: ' + eventDictParts.get('layer1Forecast18') + eventDictParts.get('layer2Forecast18') + '\n\n'                                           
            else:
                fcst = fcst + 'FCST VA CLD +6HR: ' + eventDictParts.get('layer1Forecast6') + eventDictParts.get('layer2Forecast6') + eventDictParts.get('layer3Forecast6') + '\n\n'
                fcst = fcst + 'FCST VA CLD +12HR: ' + eventDictParts.get('layer1Forecast12') + eventDictParts.get('layer2Forecast12') + eventDictParts.get('layer3Forecast12') + '\n\n'
                fcst = fcst + 'FCST VA CLD +18HR: ' + eventDictParts.get('layer1Forecast18') + eventDictParts.get('layer2Forecast18') + eventDictParts.get('layer3Forecast18') + '\n\n'
        
        elif productType == 'Last Advisory':
            fcst = fcst + 'INFO SOURCE: ' + eventDictParts.get('informationSource') + '\n\n'
            fcst = fcst + 'AVIATION COLOR CODE: ' + eventDictParts.get('volcanoStatus') + '\n\n'
            fcst = fcst + 'ERUPTION DETAILS: ' + eventDictParts.get('eruptionDetails') + '\n\n'
            fcst = fcst + 'OBS VA DTG: ' + eventDictParts.get('dateStr')[-2:] + '/' + eventDictParts.get('startTime') + 'Z\n\n'
            fcst = fcst + 'OBS VA CLD: VA NOT IDENTIFIABLE FROM SATELLITE\n\n'
            fcst = fcst + 'FCST VA CLD +6HR: NO VA EXP\n\n'
            fcst = fcst + 'FCST VA CLD +12HR: NO VA EXP\n\n'
            fcst = fcst + 'FCST VA CLD +18HR: NO VA EXP\n\n'
            
        elif productType == 'Initial Eruption':
            fcst = fcst + 'INFO SOURCE: ' + eventDictParts.get('informationSource') + '\n\n'
            fcst = fcst + 'AVIATION COLOR CODE: ' + eventDictParts.get('volcanoStatus') + '\n\n'
            fcst = fcst + 'ERUPTION DETAILS: ' + eventDictParts.get('eruptionDetails') + '\n\n'
            fcst = fcst + 'OBS VA DTG: ' + eventDictParts.get('dateStr')[-2:] + '/' + eventDictParts.get('startTime') + 'Z\n\n'
            fcst = fcst + 'OBS VA CLD: ' + eventDictParts.get('layer1Location') + ' ' + eventDictParts.get('layer2Location') + eventDictParts.get('layer3Location') + '\n\n'
            fcst = fcst + 'FCST VA CLD +6HR: NOT AVAILABLE\n\n'
            fcst = fcst + 'FCST VA CLD +12HR: NOT AVAILABLE\n\n'
            fcst = fcst + 'FCST VA CLD +18HR: NOT AVAILABLE\n\n'                                                            
        
        if eventDictParts.get('confidence') in ['LOW', 'HIGH']:
            fcst = fcst + 'RMK: T+0 CONFIDENCE ' + eventDictParts.get('confidence') + '. ' + eventDictParts.get('remarks') + '\n\n'
        else:
            if eventDictParts.get('remarks') == '':
                pass
            else:    
                fcst = fcst + 'RMK: ' + eventDictParts.get('remarks') + '\n\n'
        
        if productType != 'Near VAA':
            if productType == 'VAA' and numLayers == 0:
                fcst = fcst + 'NXT ADVISORY: NO FURTHER ADVISORIES EXPECTED\n\n'
            else:    
                fcst = fcst + 'NXT ADVISORY: ' + eventDictParts.get('nextAdvisory') + '\n\n'
            
        fcst = fcst + eventDictParts.get('forecasterInitials') + ' ' + self._monthAbbrev + ' ' + eventDictParts.get('dateStr')[:4] + ' AAWU'
        
        body = self.wordWrap(fcst)
        
        productDict = collections.OrderedDict()        
        productDict['productID'] = 'VAA'
        productDict['productName'] = 'Volcanic Ash Advisory'        
        productDict['text'] = fcst
        productDict['eventDictParts'] = eventDictParts           
            
        return productDict    
    
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
    
    def outputJSON(self, productDict):
        import json
        
        outFile = 'VAA_' + self._volcanoName + '_' + self._advisoryNumber + '.JSON'
        pathAllFile = os.path.join(OUTPUTDIR, outFile)
        
        if not os.path.exists(OUTPUTDIR):
            try:
                os.makedirs(OUTPUTDIR)
            except:
                sys.stderr.write('Could not create output directory')
                
        with open(pathAllFile, 'w') as outFile:
            json.dump(productDict['eventDictParts'], outFile)
   
        return        
                    
    
    def outputText(self, productDict):            
        outFile = 'VAA_' + self._volcanoName + '_' + self._advisoryNumber + '.txt'
        pathAllFile = os.path.join(OUTPUTDIR, outFile)
        
        if not os.path.exists(OUTPUTDIR):
            try:
                os.makedirs(OUTPUTDIR)
            except:
                sys.stderr.write('Could not create output directory')
                
        with open(pathAllFile, 'w') as outFile:
            outFile.write(productDict['text'])
    
        return