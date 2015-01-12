'''
    Description: Legacy formatter for FFW products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Oct 24, 2014    4933    Robert.Blum Initial creation
    Jan 12  2015    4937    Robert.Blum Refactor to inherit from new
                            formatter classes.
'''

import FormatTemplate
import datetime

import types, re, sys
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from QueryAfosToAwips import QueryAfosToAwips
from Bridge import Bridge
from TextProductCommon import TextProductCommon
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self) :
        super(Format, self).initialize()

    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()

        legacyText = self._createTextProduct()

        return ProductUtils.wrapLegacy(legacyText)

    def _processProductParts(self, productDict, productParts, skipParts=[]):
        text = ''
        if type(productParts) is types.DictType:
            arguments = productParts.get('arguments')
            partsList = productParts.get('partsList')
        else:
            partsList = productParts

        for part in partsList:
            valtype = type(part)
            if valtype is types.StringType:
                name = part
            elif valtype is types.TupleType or valtype is types.ListType:
                name = part[0]
                infoDicts = part[1]

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print 'Legacy Part:', name, ': ', 

            partText = ''
            if name == 'wmoHeader':
                partText = self._wmoHeader(self._productID, self._siteID, self._startTime) + '\n\n'
            elif name == 'wmoHeader_noCR': 
                partText = self._wmoHeader(self._productID, self._siteID, self._startTime) + '\n'
            elif name == 'setUp_product':
                pass
            elif name == 'setUp_segment':
                pass
            elif name == 'ugcHeader':
                partText = self._ugcHeader(productDict)
            elif name == 'easMessage':
                easMessage = self._easMessage(productDict['vtecRecords'])
                if easMessage is not None:
                    partText = easMessage + '\n'
            elif name == 'productHeader':
                partText = self._productHeader()
            elif name == 'overview':
                partText = '|* DEFAULT OVERVIEW SECTION *|\n\n'
            elif name == 'vtecRecords':
                partText = self._vtecRecords(productDict)
            elif name == 'areaList':
                partText = self._areaList(productDict)
            elif name == 'issuanceTimeDate':
                partText = self._issuanceTimeDate()
            elif name == 'callsToAction':
                partText = self._callsToAction(productDict, name)
                if (self._runMode == 'Practice'):
                    partText += 'This is a test message. Do not take action based on this message. \n\n'
            elif name == 'polygonText':
                partText = self._polygonText(productDict)
            elif name == 'endSegment':
                partText = '\n$$\n\n' 
            elif name == 'CR':
                partText = '\n'
            elif name == 'cityList':
                partText = self._cityList(productDict)
            elif name == 'summaryHeadlines':
                partText = self._summaryHeadlines(productDict)
            elif name == 'basisAndImpactsStatement_segmentLevel':
                partText = self._basisAndImpactsStatement_segmentLevel(productDict)
            elif name == 'segments':
                partText = self.processSubParts(productDict['segments'], infoDicts) 
            elif name == 'sections':
                partText = self.processSubParts(self.productDict['segments'], infoDicts)
            elif name == 'setUp_section':
                pass
            elif name == 'attribution':
                hazards = productDict['hazards']
                # There is only 1 hazard
                for hazard in hazards:
                    partText += self._attribution(hazard, productDict) + '\n'
            elif name == 'firstBullet':
                hazards = productDict['hazards']
                # There is only 1 hazard
                for hazard in hazards:
                    partText += self._firstBullet(hazard, productDict) + '\n'
            elif name == 'timeBullet':
                partText = self._timeBullet(productDict) + '\n'
            elif name == 'basisBullet':
                partText = self._basisBullet(productDict)
            elif name == 'emergencyHeadline':
                if (self._runMode == 'Practice'):
                    partText = '...This message is for test purposes only... \n\n'
                includeChoices = productDict['include']
                if includeChoices and 'ffwEmergency' in includeChoices:
                    partText += '...Flash Flood Emergency for ' + productDict['includeEmergencyLocation'] + '...\n\n'
            elif name == 'emergencyStatement':
                includeChoices = productDict['include']
                if includeChoices and 'ffwEmergency' in includeChoices:
                    partText = '  This is a Flash Flood Emergency for ' + productDict['includeEmergencyLocation'] + '\n\n'
            elif name == 'locationsAffected':
                partText = self._locationsAffected(productDict)
            elif name == 'endingSynopsis':
                partText = productDict.get('endingSynopsis', '')
                if partText != '':
                    partText += '\n\n'
            elif name == 'floodPointHeader':
                partText = self._floodPointHeader() + '\n'
            elif name == 'floodPointHeadline':
                partText = self._floodPointHeadline() + '\n'
            elif name == 'observedStageBullet':
                partText = '* ' + self._observedStageBullet(productDict) + '\n'
            elif name == 'floodStageBullet':
                partText = '* ' + self._floodStageBullet(productDict) + '\n'
            elif name == 'floodCategoryBullet':
                partText = '* ' + self._floodCategoryBullet(productDict) + '\n'
            elif name == 'otherStageBullet':
                partText = self._otherStageBullet(productDict) + '\n'
            elif name == 'forecastStageBullet':
                partText = self._forecastStageBullet(productDict) + '\n'
            elif name == 'pointImpactsBullet':
                partText = self._pointImpactsBullet(productDict)
            elif name == 'floodPointTable':
                partText = '\n' + self._floodPointTable(productDict) + '\n'
            else:
                textStr = self._tpc.getVal(productDict, name)
                if textStr:
                    partText = textStr + '\n' 

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print partText

            text += partText
        return text

    def processSubParts(self, infoDicts, subParts):
        """
        Generates Legacy text from a list of subParts e.g. segments or sections
        @param infoDicts: a list of dictionaries for each subPart
        @param subParts: a list of Product Parts for each segment
        @return: Returns the legacy text of the subParts
        """
        text = '' 
        for i in range(len(subParts)):
            text += self._processProductParts(infoDicts[i], subParts[i].get('partsList'))
        return text

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _easMessage(self, vtecRecords):
        for vtecRecord in vtecRecords:
            if 'sig' in vtecRecord:
                if vtecRecord['sig'] is 'A':
                    return 'Urgent - Immediate broadcast requested'
        return 'Bulletin - EAS activation requested'

    ################# Segment Level

    ################# Section Level
    def _attribution(self, hazardEvent, productDict):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(productDict)

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = vtecRecord['hdln']

            if len(vtecRecord['hdln']):
                action = vtecRecord['act']

            if action == 'NEW':
                attribution = nwsPhrase + 'issued a'
            elif action == 'CON':
                attribution = 'the ' + hazName + ' remains in effect for...'
            elif action == 'EXT':
                attribution = 'the ' + hazName + ' is now in effect for...' 
            elif action == 'CAN':
                attribution = 'the ' + hazName + \
                   ' for... ' + areaPhrase + ' has been cancelled.'
            elif action == 'EXP':
                expTimeCurrent = self._startTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' has expired.'
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
                   attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        return attribution + '\n'

    def _firstBullet(self, hazard, productDict):
        headPhrase = '* '
        areaPhrase = self.getAreaPhraseBullet(productDict)

        if self._runMode == 'Practice':
            testText = 'This is a test message.  '
            headPhrase = headPhrase + testText

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = vtecRecord['hdln']

            if len(vtecRecord['hdln']):
                action = vtecRecord['act']

            if action == 'NEW':
                headPhrase += hazName + ' for...\n'
                # TODO - Fix this
                headPhrase += productDict['typeOfFlooding'] + '\n'
                headPhrase += areaPhrase
            elif action == 'CON':
                headPhrase +=  areaPhrase
            elif action == 'EXT':
                headPhrase += ' ' + areaPhrase

        return headPhrase

    def _basisBullet(self, segment):
        bulletText = '* '
        if self._runMode == 'Practice':
            bulletText += 'This is a test message.  '

        elements = KeyInfo.getElements('basisBullet', segment)
        if len(elements) > 0:
            basis = segment[elements[0]]
        else:
            basis = segment['basisBullet']

        if basis is None :
             basis = '...Flash Flooding was reported'

        # Create basis statement
        vtecRecords = segment['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            eventTime = vtecRecord.get('startTime')
            eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z ', stripLeading=True, timeZones=self.timezones)
            bulletText += 'At ' + eventTime
            bulletText += basis
        return bulletText + '\n\n'

    def _locationsAffected(self, segmentDict):
        heading = '* '
        locationsAffected = ''

        if (self._runMode == 'Practice'):
            heading += "This is a test message.  "

        immediateCause = segmentDict.get('immediateCause', None)
        if immediateCause == 'DM' or immediateCause == 'DR':
            damOrLeveeName = segmentDict['damOrLeveeName']
            if damOrLeveeName:
                damInfo = self._damInfo().get(damOrLeveeName)
                if damInfo:
                    # Scenario
                    scenario = segmentDict['scenario']
                    if scenario:
                        scenarios = damInfo['scenarios']
                        if scenarios:
                            scenarioText = scenarios[scenario]
                            if scenarioText:
                                locationsAffected += scenarioText + '\n\n'
                    # Rule of Thumb
                    ruleOfThumb = damInfo['ruleOfThumb']
                    if ruleOfThumb:
                        locationsAffected += ruleOfThumb + '\n\n'

        # Need to check for List of cities here
        if segmentDict['citiesListFlag'] == True:
            locationsAffected += 'Locations impacted include...' + self.formatCityList(segmentDict) + '\n\n'

        if not locationsAffected:
            locationsAffected = "Some locations that will experience flash flooding include..."
            locationsAffected += self.createImpactedLocations(segmentDict) + '\n\n'

        # Add any other additional Info
        locationsAffected += self.createAdditionalComments(segmentDict)

        return heading + locationsAffected

    def createImpactedLocations(self, segment):
        nullReturn = "mainly rural areas of the aforementioned areas."
        columns = ["name", "warngenlev"]
        try :
            cityGeoms = self._tpc.mapDataQuery("city", columns, segment["geometry"])
        except :
            return nullReturn
        if not isinstance(cityGeoms, list) :
            return nullReturn
        names12 = []
        namesOther = []
        for cityGeom in cityGeoms :
            try:
                name = cityGeom.getString(columns[0])
                if not name:
                    continue
                levData = str(cityGeom.getString(columns[1]))
                if levData == "1" or levData == "2" :
                      names12.append(name)
                else :
                      namesOther.append(name)
            except :
                pass

        if len(names12)>0 :
            return self._tpc.formatDelimitedList(names12)
        if len(namesOther)>0 :
            return self._tpc.formatDelimitedList(namesOther)
        return nullReturn

    def _damInfo(self):
        return {
                'Big Rock Dam': {
                        'riverName': 'Phil River',
                        'cityInfo': 'Evan...located about 3 miles',
                        'scenarios': {
                            'highFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 18 feet in 16 minutes.',
                            'highNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 23 feet in 31 minutes.',
                            'mediumFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 14 feet in 19 minutes.',
                            'mediumNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 17 feet in 32 minutes.',
                            },
                        'ruleOfThumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                        and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.''',
                    },
                'Branched Oak Dam': {
                        'riverName': 'Kells River',
                        'cityInfo': 'Dangelo...located about 6 miles',
                        'scenarios': {
                            'highFast': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 19 feet in 32 minutes.',
                            'highNormal': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 26 feet in 56 minutes.',
                            'mediumFast': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 14 feet in 33 minutes.',
                            'mediumNormal': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 20 feet in 60 minutes.',
                            },
                        'ruleOfThumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                        and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.''',
                    },
                }
