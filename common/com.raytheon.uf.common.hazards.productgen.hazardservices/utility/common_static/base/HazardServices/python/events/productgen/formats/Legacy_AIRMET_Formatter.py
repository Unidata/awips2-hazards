import FormatTemplate
import types, re, sys, collections, os
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
from collections import OrderedDict
import AviationUtils

'''
WAUS46 KKCI 261709
WA6S SFOS WA 261709
AIRMET SIERRA FOR IFR VALID UNTIL 262309
.
NO SIGNIFICANT IFR EXP.

=WA6T SFOT WA 261709
AIRMET TANGO UPDATE 2 FOR TURB/STF SFC WINDS/LLWS VALID UNTIL 262309
.
NO SIGNIFICANT TURB/STG SFC WINDS/LLWS EXP.

=WA6Z SFOZ WA 261709
AIRMET ZULU UPDATE 1 FOR ICE AND FZLVL VALID UNTIL 262309

FZLVL...028-129.
MULT FRZLVL FL200-FL300 FROM 40W_MOT-30WSW_BIS-50W_GFK-40W_MOT
'''

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self):
        super(Format, self).initialize()
        self.initProductPartMethodMapping()

        self._productGeneratorName = 'AIRMET_ProductGenerator'

    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'easMessage': self._easMessage,
            'productHeader': self._productHeader,
            'narrativeForecastInformation': self._narrativeForecastInformation
                                }

    def execute(self, productDict, editableEntries=None, overrideProductText=None):
        self.productDict = productDict
        self._editableParts = OrderedDict()
        parts = self.productDict.get('productParts')
        eventDicts = self.productDict.get('eventDicts')
        self.updateNumberDict = self.productDict.get('updateNumberDict')

        self._fcstList = {}

        originatingOfficeList = self.createOriginatingOfficeList(eventDicts)
        zoneList = self.createZoneList(eventDicts)
        advisoryTypeDict = self.createAdvisoryTypeDict(eventDicts)

        headerDict = self.createHeaderDict(originatingOfficeList, zoneList, advisoryTypeDict)
        bodyDict = self.createBodyDict(eventDicts, originatingOfficeList, zoneList)
        freezingLevelDict = self.createFreezingLevelDict(eventDicts, originatingOfficeList, zoneList)

        productDict = self.createProduct(headerDict, bodyDict, freezingLevelDict, originatingOfficeList, zoneList)
        legacyText = productDict.get('text')

        if self.issueFlag == "True":
            self.outputText(productDict)
        self.flush()

        return [ProductUtils.wrapLegacy(legacyText)],self._editableParts

    ######################################################
    #  Product Part Methods
    ######################################################
    def createOriginatingOfficeList(self,eventDicts):
        originatingOfficeList = []

        for eventDict in eventDicts:
            eventDictParts = eventDict.get('parts')
            originatingOffice = eventDictParts.get('originatingOffice')
            if originatingOffice not in originatingOfficeList:
                originatingOfficeList.append(originatingOffice)

        return originatingOfficeList

    def createZoneList(self, eventDicts):
        zoneList = []

        for eventDict in eventDicts:
            eventDictParts = eventDict.get('parts')
            zone = eventDictParts.get('zone')
            if zone not in zoneList:
                zoneList.append(zone)

        return zoneList

    def createAdvisoryTypeDict(self,eventDicts):
        advisoryTypeDict = {}

        for eventDict in eventDicts:
            eventDictParts = eventDict.get('parts')
            phenomenon = eventDict.get('hazardType')
            originatingOffice = eventDictParts.get('originatingOffice')
            zone = eventDictParts.get('zone')
            if phenomenon in ['LLWS', 'Strong_Surface_Wind', 'Turbulence']:
                series = 'TANGO'
            elif phenomenon in ['Mountain_Obscuration', 'IFR']:
                series = 'SIERRA'
            else:
                series = 'ZULU'

            if eventDictParts.get('advisoryType') in ['Correction', 'Amendment', 'Cancellation']:
                if originatingOffice in advisoryTypeDict:
                    if zone in advisoryTypeDict[originatingOffice]:
                        if series in advisoryTypeDict[originatingOffice][zone]:
                            if eventDictParts.get('advisoryType') in advisoryTypeDict[originatingOffice][zone]:
                                pass
                            else:
                                advisoryTypeDict[originatingOffice][zone].append(eventDictParts.get('advisoryType'))
                        else:
                            advisoryTypeDict[originatingOffice][zone][series] = []
                            advisoryTypeDict[originatingOffice][zone][series].append(eventDictParts.get('advisoryType'))
                    else:
                        advisoryTypeDict[originatingOffice][zone] = {}
                        advisoryTypeDict[originatingOffice][zone][series] = []
                        advisoryTypeDict[originatingOffice][zone][series].append(eventDictParts.get('advisoryType'))
                else:
                    advisoryTypeDict[originatingOffice] = {}
                    advisoryTypeDict[originatingOffice][zone] = {}
                    advisoryTypeDict[originatingOffice][zone][series] = []
                    advisoryTypeDict[originatingOffice][zone][series].append(eventDictParts.get('advisoryType'))

            self.issueTime = str(eventDictParts.get('currentTime'))
            self.startTime = str(eventDictParts.get('startTime'))
            self.endTime = str(eventDictParts.get('endTime'))
            self.advisoryType = str(eventDictParts.get('advisoryType'))

        return advisoryTypeDict

    def createFcstStr(self, eventDictParts):
        fcstStr = ''

        boundingStatement = self.getBoundingStatement(eventDictParts)
        timeConstraintStr = self.getTimeConstraintStr(eventDictParts)
        outlookStr = self.getOutlookStr(eventDictParts)

        if eventDictParts.get('advisoryType') in ['Cancellation', 'Amendment']:
            updateStr = '...UPDATE'
        elif eventDictParts.get('advisoryType') == 'Correction':
            updateStr = '...CORRECTION'
        else:
            updateStr = ''

        #due to variations, put together forecast product based on hazard type
        #except for multiple freezing levels use in freezing level creation
        if self.phenomenon == 'LLWS':
            fcstStr = fcstStr + "AIRMET LLWS..." + eventDictParts.get('states') + updateStr + '\n'
            fcstStr = fcstStr + boundingStatement + '\n'
            if eventDictParts.get('advisoryType') == 'Cancellation':
                fcstStr = fcstStr + 'CANCEL AIRMET. LLWS HAS DIMINISHED.' + '\n'
            else:
                fcstStr = fcstStr + timeConstraintStr + 'AREAS LLWS CONDITIONS EXP.' + eventDictParts.get('intensityTrend') + '\n'
                fcstStr = fcstStr + outlookStr + '\n'
        elif self.phenomenon == 'Strong_Surface_Wind':
            fcstStr = fcstStr + "AIRMET STG SFC WNDS..." + eventDictParts.get('states') + updateStr + '\n'
            fcstStr = fcstStr + boundingStatement + '\n'
            if eventDictParts.get('advisoryType') == 'Cancellation':
                fcstStr = fcstStr + 'CANCEL AIRMET. STG SFC WNDS HAVE DIMINISHED.' + '\n'
            else:
                fcstStr = fcstStr + timeConstraintStr + 'SUSTAINED SFC WND 30 KTS OR GTR.' + eventDictParts.get('intensityTrend') + '\n'
                fcstStr = fcstStr + outlookStr + '\n'
        elif self.phenomenon == 'Turbulence':
            fcstStr = fcstStr + "AIRMET TURB..." + eventDictParts.get('states') + updateStr + '\n'
            fcstStr = fcstStr + boundingStatement + '\n'
            if eventDictParts.get('advisoryType') == 'Cancellation':
                fcstStr = fcstStr + 'CANCEL AIRMET. TURB HAS DIMINISHED.' + '\n'
            else:
                fcstStr = fcstStr + timeConstraintStr + 'MOD TURB ' + eventDictParts.get('verticalExtent') + '. ' + eventDictParts.get('intensityTrend') + '\n'
                fcstStr = fcstStr + outlookStr + '\n'
        elif self.phenomenon == 'Mountain_Obscuration':
            fcstStr = fcstStr + "AIRMET MTN OBSCN..." + eventDictParts.get('states') + updateStr + '\n'
            fcstStr = fcstStr + boundingStatement + '\n'
            if eventDictParts.get('advisoryType') == 'Cancellation':
                fcstStr = fcstStr + 'CANCEL AIRMET. MTN OBSCN HAS DIMINISHED.' + '\n'
            else:
                fcstStr = fcstStr + timeConstraintStr + 'MTNS OBSC BY ' + eventDictParts.get('phenomenon') + '.' + eventDictParts.get('intensityTrend') + '\n'
                fcstStr = fcstStr + outlookStr + '\n'
        elif self.phenomenon == 'IFR':
            fcstStr = fcstStr + "AIRMET IFR..." + eventDictParts.get('states') + updateStr + '\n'
            fcstStr = fcstStr + boundingStatement + '\n'
            if eventDictParts.get('advisoryType') == 'Cancellation':
                fcstStr = fcstStr + 'CANCEL AIRMET. IFR HAS DIMINISHED.' + '\n'
            else:
                fcstStr = fcstStr + timeConstraintStr + eventDictParts.get('restrictions') + ' ' + eventDictParts.get('phenomenon') + '.' + eventDictParts.get('intensityTrend') + '\n'
                fcstStr = fcstStr + outlookStr + '\n'
        elif self.phenomenon == 'Icing':
            fcstStr = fcstStr + "AIRMET ICE..." + eventDictParts.get('states') + updateStr + '\n'
            fcstStr = fcstStr + boundingStatement + '\n'
            if eventDictParts.get('advisoryType') == 'Cancellation':
                fcstStr = fcstStr + 'CANCEL AIRMET. ICE HAS DIMINISHED.' + '\n'
            else:
                fcstStr = fcstStr + timeConstraintStr + 'MOD ICEIC ' + eventDictParts.get('verticalExtent') + ". " + eventDictParts.get('intensityTrend') + '\n'
                fcstStr = fcstStr + outlookStr + '\n'
        else:
            fcstStr = ''

        return fcstStr

    def getOutlookStr(self, eventDictParts):
        phenomDict = {'LLWS': 'LLWS ', 'Multiple_Freezing_Levels ': 'MULT FZ LVLS ', 'IFR': 'IFR ',
                      'Icing': 'ICE ', 'Strong_Surface_Wind': 'STG SFC WND ', 'Turbulence': 'TURB ',
                      'Mountain_Obscuration': 'MTN OBSCN '}

        if self.phenomenon in ['LLWS', 'Multiple_Freezing_Levels']:
            outlookStr = ''
        else:
            if eventDictParts.get('outlookBoundingStatement') == None:
                outlookStr = ''
            else:
                outlookStr = 'OTLK VALID ' + eventDictParts.get('endTime')[2:] + '-' + eventDictParts.get('outlookEndTime')[2:] + 'Z...'
                outlookStr = outlookStr + phenomDict[self.phenomenon] + eventDictParts.get('states') + '\n'
                outlookStr = outlookStr + 'BOUNDED BY ' + eventDictParts.get('outlookBoundingStatement')[5:]
        return outlookStr

    def getTimeConstraintStr(self, eventDictParts):
        timeConstraint = eventDictParts.get('timeConstraint')
        timeConstraintTime = eventDictParts.get('timeConstraintTime')

        if timeConstraint == 'None':
            timeConstraintStr = ''
        elif timeConstraint == 'Occasional':
            timeConstraintStr = 'OCNL '
        elif timeConstraint == 'After':
            timeConstraintStr = 'AFT ' + str(timeConstraintTime) + 'Z '
        elif timeConstraint == 'Until':
            timeConstraintStr = 'TIL ' + str(timeConstraintTime) + 'Z '
        elif timeConstraint == 'By':
            timeConstraintStr = 'BY ' + str(timeConstraintTime) + 'Z '
        else:
            timeConstraintStr = 'CONDS CONTG BYD ' + str(timeConstraintTime) + 'Z '

        return timeConstraintStr

    def createFreezingLevelDict(self, eventDicts, originatingOfficeList, zoneList):
        freezingLevelDict = {'KKCI': {'SFO': '',
                                      'SLC': '',
                                      'DFW': '',
                                      'CHI': '',
                                      'BOS': '',
                                      'MIA': '',},
                             'PAWU': {'JNU': '',
                                      'ANC': '',
                                      'FAI': '',},
                             'PHFO': {'HNL': '',}}

        for eventDict in eventDicts:
            eventDictParts = eventDict.get('parts')
            if eventDict.get('hazardType') == 'Multiple_Freezing_Levels':
                freezingLevelDict[eventDictParts.get('originatingOffice')][eventDictParts.get('zone')] = eventDictParts.get('freezingLevel') + eventDictParts.get('boundingStatement')

        return freezingLevelDict

    def createFreezingLevelStr(self):
        freezingLevelInformation = AviationUtils.AviationUtils().createFreezingLevelInformation()
        freezingLevelStr = 'FZLVL...' + freezingLevelInformation + '.\n'

        return freezingLevelStr

    def createBodyDict(self,eventDicts,originactingOfficeList,zoneList):
        bodyDict = {'KKCI': {'SFO': {'SIERRA': {},'TANGO': {},'ZULU': {}},
                             'SLC': {'SIERRA': {},'TANGO': {},'ZULU': {}},
                             'DFW': {'SIERRA': {},'TANGO': {},'ZULU': {}},
                             'CHI': {'SIERRA': {},'TANGO': {},'ZULU': {}},
                             'BOS': {'SIERRA': {},'TANGO': {},'ZULU': {}},
                             'MIA': {'SIERRA': {},'TANGO': {},'ZULU': {}}},
                    'PAWU': {'JNU': {'SIERRA': {},'TANGO': {},'ZULU': {}},
                             'ANC': {'SIERRA': {},'TANGO': {},'ZULU': {}},
                             'FAI': {'SIERRA': {},'TANGO': {},'ZULU': {}}},
                    'PHFO': {'HNL': {'SIERRA': {},'TANGO': {},'ZULU': {}}}}

        for eventDict in eventDicts:
            eventDictParts = eventDict.get('parts')
            self.issueFlag = eventDict.get('issueFlag',False)
            self.status = eventDict.get('status',False)
            eventID = eventDict.get('eventID')
            zone = eventDictParts.get('zone')
            originatingOffice = eventDictParts.get('originatingOffice')
            self.phenomenon = eventDict.get('hazardType')
            if self.phenomenon in ['LLWS', 'Strong_Surface_Wind', 'Turbulence']:
                series = 'TANGO'
            elif self.phenomenon in ['Mountain_Obscuration', 'IFR']:
                series = 'SIERRA'
            else:
                series = 'ZULU'

            fcstStr = self.createFcstStr(eventDictParts)
            bodyDict[originatingOffice][zone][series][eventID] = fcstStr

        return bodyDict

    def createHeaderDict(self,originatingOfficeList,zoneList, advisoryTypeDict):
        headerDict = {}

        headerDictAll = {'KKCI': {'SFO': {"HEADER": "WAUS46 KKCI "+self.issueTime,
                                          "SIERRA": "WA6S SFOS WA "+self.startTime,
                                          "TANGO": "=WA6T SFOT WA "+self.startTime,
                                          "ZULU": "=WA6Z SFOZ WA "+self.startTime},
                                  'SLC': {"HEADER": "WAUS45 KKCI "+self.issueTime,
                                          "SIERRA": "WA5S SLCS WA "+self.startTime,
                                          "TANGO": "=WA5T SLCT WA "+self.startTime,
                                          "ZULU": "=WA5Z SLCZ WA "+self.startTime},
                                  'DFW': {"HEADER": "WAUS44 KKCI "+self.issueTime,
                                          "SIERRA": "WA4S DFWS WA "+self.startTime,
                                          "TANGO": "=WA4T DFWT WA "+self.startTime,
                                          "ZULU": "=WA4Z DFWZ WA "+self.startTime},
                                  'CHI': {"HEADER": "WAUS43 KKCI "+self.issueTime,
                                          "SIERRA": "WA3S CHIS WA "+self.startTime,
                                          "TANGO": "=WA3T CHIT WA "+self.startTime,
                                          "ZULU": "=WA3Z CHIZ WA "+self.startTime},
                                  'BOS': {"HEADER": "WAUS41 KKCI "+self.issueTime,
                                          "SIERRA": "WA1S BOSS WA "+self.startTime,
                                          "TANGO": "=WA1T BOST WA "+self.startTime,
                                          "ZULU": "=WA1Z BOSZ WA "+self.startTime},
                                  'MIA': {"HEADER": "WAUS42 KKCI "+self.issueTime,
                                          "SIERRA": "WA2S MIAS WA "+self.startTime,
                                          "TANGO": "=WA2T MIAT WA "+self.startTime,
                                          "ZULU": "=WA2Z MIAZ WA "+self.startTime}},
                         'PAWU': {'JNU': {"HEADER": "WAAK47 PAWU "+self.issueTime,
                                          "SIERRA": "WA7O\nJNUS WA "+self.startTime,
                                          "TANGO": "=JNUT WA "+self.startTime,
                                          "ZULU": "=JNUZ WA "+self.startTime},
                                  'ANC': {"HEADER": "WAAK48 PAWU "+self.issueTime,
                                          "SIERRA": "WA8O\nANCS WA "+self.startTime,
                                          "TANGO": "=ANCT WA "+self.startTime,
                                          "ZULU": "=ANCZ WA "+self.startTime},
                                  'FAI': {"HEADER": "WAAK49 PAWU "+self.issueTime,
                                          "SIERRA": "WA9O\nFAIS WA "+self.startTime,
                                          "TANGO": "=FAIT WA "+self.startTime,
                                          "ZULU": "=FAIZ WA "+self.startTime}},
                         'PHFO': {'HNL': {"HEADER": "WAHW31 PHFO "+self.issueTime,
                                          "SIERRA": "WA0HI\nHNLS WA "+self.startTime,
                                          "TANGO": "=HNLT WA "+self.startTime,
                                          "ZULU": "=HNLZ WA "+self.startTime}}}

        for originatingOffice in originatingOfficeList:
            headerDict[originatingOffice] = headerDictAll[originatingOffice]

        for originatingOffice in advisoryTypeDict:
            for zone in advisoryTypeDict[originatingOffice]:
                for series in advisoryTypeDict[originatingOffice][zone]:
                    advisoryTypeList = advisoryTypeDict[originatingOffice][zone][series]
                    if 'Amendment' in advisoryTypeList or 'Cancellation' in advisoryTypeList:
                        headerDictAll[originatingOffice][zone]["HEADER"] = headerDictAll[originatingOffice][zone]["HEADER"] + " AAA"
                        headerDictAll[originatingOffice][zone][series] = headerDictAll[originatingOffice][zone][series] + ' AMD'
                    else: #correction
                        headerDictAll[originatingOffice][zone]["HEADER"] = headerDictAll[originatingOffice][zone]["HEADER"] + " CCA"
                        headerDictAll[originatingOffice][zone][series] = headerDictAll[originatingOffice][zone][series] + ' COR'

        return headerDict

    def createProduct(self, headerDict, bodyDict, freezingLevelDict, originatingOfficeList, zoneList):
        fcst = ''
        updateNumberStr = ' '
        updateNumberDict = self.updateNumberDict['updateNumber']
        freezingLevelStr = self.createFreezingLevelStr()

        for office in originatingOfficeList:
            for zone in headerDict[office]:
                #header
                fcst = fcst + headerDict[office][zone]['HEADER'] + '\n'
                #sierra series
                fcst = fcst + headerDict[office][zone]['SIERRA'] + '\n'
                if updateNumberDict[zone]['SIERRA'] != 0:
                    updateNumberStr = ' UPDATE ' + str(updateNumberDict[zone]['SIERRA']) + ' '
                else:
                    updateNumberStr = ' '
                fcst = fcst + 'AIRMET SIERRA' + updateNumberStr + 'FOR IFR VALID UNTIL ' + self.endTime + '\n'
                if bool(bodyDict[office][zone]['SIERRA']) is False:
                    fcst = fcst + '.' + '\n'
                    fcst = fcst + 'NO SIGNIFICANT IFR EXP.' + '\n'
                    fcst = fcst + '\n'
                else:
                    for entry in bodyDict[office][zone]['SIERRA']:
                        fcst = fcst + bodyDict[office][zone]['SIERRA'][entry]
                #tango series
                fcst = fcst + headerDict[office][zone]['TANGO'] + '\n'
                if updateNumberDict[zone]['TANGO'] != 0:
                    updateNumberStr = ' UPDATE ' + str(updateNumberDict[zone]['TANGO']) + ' '
                else:
                    updateNumberStr = ' '
                fcst = fcst + 'AIRMET TANGO' + updateNumberStr + 'FOR TURB/STG SFC WINDS/LLWS VALID UNTIL ' + self.endTime + '\n'
                if bool(bodyDict[office][zone]['TANGO']) is False:
                    fcst = fcst + '.' + '\n'
                    fcst = fcst + 'NO SIGNIFICANT TURB/STG SFC WINDS/LLWS EXP.' + '\n'
                    fcst = fcst + '\n'
                else:
                    for entry in bodyDict[office][zone]['TANGO']:
                        fcst = fcst + bodyDict[office][zone]['TANGO'][entry]
                #zulu series
                fcst = fcst + headerDict[office][zone]['ZULU'] + '\n'
                if updateNumberDict[zone]['ZULU'] != 0:
                    updateNumberStr = ' UPDATE ' + str(updateNumberDict[zone]['ZULU']) + ' '
                else:
                    updateNumberStr = ' '
                fcst = fcst + 'AIRMET ZULU' + updateNumberStr + 'FOR ICE AND FZLVL VALID UNTIL ' + self.endTime + '\n'
                if bool(bodyDict[office][zone]['ZULU']) is False:
                    fcst = fcst + '.' + '\n'
                    fcst = fcst + 'NO SIGNIFICANT ICE EXP.' + '\n'
                    fcst = fcst + freezingLevelDict[office][zone]
                else:
                    for entry in bodyDict[office][zone]['ZULU']:
                        fcst = fcst + bodyDict[office][zone]['ZULU'][entry]
                    fcst = fcst + freezingLevelStr + '\n' + freezingLevelDict[office][zone]

                fcst = fcst + '\n\n' + '--------------------'
                fcst = fcst + '\n' + '--------------------' + '\n\n'

        body = self.wordWrap(fcst)

        productDict = collections.OrderedDict()
        productDict['productID'] = 'SIGMET.International'
        productDict['productName'] = 'INTERNATIONAL SIGMET'
        productDict['text'] = fcst

        return productDict

    def getBoundingStatement(self, eventDictParts):
        if eventDictParts.get('originatingOffice') == 'PAWU':
            vertices = eventDictParts.get('geometry')
            boundingStatement = self.createLatLonStatement(vertices)
        else:
            if eventDictParts.get('currentBoundingStatement') is not None:
                boundingStatement = eventDictParts.get('currentBoundingStatement')
            else:
                boundingStatement = eventDictParts.get('boundingStatement')

        return boundingStatement

    def createLatLonStatement(self, vertices):
        import math

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

    def wordWrap(self, string, width=68):
        newstring = ''
        if len(string) > width:
            while True:
                # find position of nearest whitespace char to the left of 'width'
                marker = width - 1
                if not string[marker].isspace():
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
        OUTPUTDIR = AviationUtils.AviationUtils().outputAirmetFilePath()

        outAllAdvisories = 'AIRMET_' + self.issueTime + '.txt'
        pathAllFile = os.path.join(OUTPUTDIR, outAllAdvisories)

        if not os.path.exists(OUTPUTDIR):
            try:
                os.makedirs(OUTPUTDIR)
            except:
                sys.stderr.write('Could not create output directory')

        with open(pathAllFile, 'w') as outFile:
            outFile.write(productDict['text'])

        return
