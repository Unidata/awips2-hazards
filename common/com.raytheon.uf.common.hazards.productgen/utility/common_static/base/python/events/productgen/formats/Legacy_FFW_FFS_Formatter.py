'''
    Description: Legacy formatter hydro flood products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Oct 24, 2014    4933    Robert.Blum Initial creation
'''

import FormatTemplate
import datetime

import types, re, sys
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from QueryAfosToAwips import QueryAfosToAwips
from Bridge import Bridge
from TextProductCommon import TextProductCommon

class Format(FormatTemplate.Formatter):

    def initialize(self) :
        self.bridge = Bridge()
        areaDict = self.bridge.getAreaDictionary()
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)
        self.timezones = []

        self._productID = self.productDict['productID']
        self._productName = self.productDict['productName']
        self._productCategory = self.productDict['productCategory']
        self._siteID = self.productDict['siteID']
        self._backupSiteID = self.productDict['backupSiteID']
        self._startTime = self.productDict['startTime']
        self._endTime = self.productDict['endTime']
        self._runMode = self.productDict['runMode']
        self._siteInfo = self.bridge.getSiteInfo()
        self.setSiteInfo()

    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()

        # Setup the Time Zones
        # TODO the generator should pass the timezones
        ugcList = []
        segments = productDict['segments']
        for segment in segments:
            for area in segment['impactedAreas']:
                ugcList.append(area['ugc'])
        self.timezones = self._tpc.hazardTimeZones(ugcList)

        # Build the text product based on the productParts
        text = ''
        productParts = productDict['productParts']
        text += self._processProductParts(productDict, productParts)
        return ProductUtils.wrapLegacy(text)

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
                partText = self._callsToAction(productDict)
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
                partText = productDict['summaryHeadlines'] + '\n'
            elif name == 'basisAndImpactsStatement_segmentLevel':
                partText = '|* Current hydrometeorological situation and expected impacts *| \n\n'
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
                pass
            elif name == 'floodPointHeadline':
                pass
            elif name == 'observedStagebullet':
                pass
            elif name == 'floodStageBullet':
                pass
            elif name == 'floodCategoryBullet':
                pass
            elif name == 'otherStageBullet':
                pass
            elif name == 'forecastStageBullet':
                pass
            elif name == 'pointImpactsBullet':
                partText += self._pointImpactsBullet(productDict)
            elif name == 'floodPointTable':
                pass
            else:
                textStr = self._tpc.getVal(productDict, name)
                if textStr:
                    partText = textStr + '\n' 
            # Note: these print statements are left here for debugging
            # They will be useful for Focal Points as they are overriding product generators.                                                    
#             if name not in ['segments', 'sections']:
#                 print 'Legacy Part:', part, ': ', partText
                
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
    
    def _wmoHeader(self, productID, siteID, startTime):
        siteEntry = self._siteInfo.get(siteID)
        fullStationID = siteEntry.get('fullStationID')  # KBOU
        ddhhmmTime = self._tpc.getFormattedTime(startTime, '%d%H%M', stripLeading=False)
        a2a = QueryAfosToAwips(productID, siteID)
        wmoID = a2a.getWMOprod()  # e.g. WUUS53
        header = wmoID + ' ' + fullStationID + ' ' + ddhhmmTime + '\n'
        header += productID + siteID
        return header

    def _easMessage(self, vtecRecords):
        for vtecRecord in vtecRecords:
            if 'sig' in vtecRecord:
                if vtecRecord['sig'] is 'A':
                    return 'Urgent - Immediate broadcast requested'
        return 'Bulletin - EAS activation requested'

    def _productHeader(self):
        text = ''
        if (self._runMode == 'Practice'):
            text += 'Test...' + self._productName + '...Test \n'
        else:
            text += self._productName + '\n'
        text += 'National Weather Service ' + self._wfoCityState + '\n'
        text += self._issuanceTimeDate()
        return text

    ################# Segment Level

    def _ugcHeader(self, segment):
        ugcList = []
        for area in segment['impactedAreas']:
            ugcList.append(area['ugc'])
        ugcList.sort()
        ugctext = self._tpc.makeUGCString(ugcList)
        endTime = self._endTime
        return ugctext + '-' + endTime.strftime('%d%H%m-') + '\n'

    def _vtecRecords(self, segment):
        vtecString = ''
        vtecRecords = segment['vtecRecords']
        for vtecRecord in vtecRecords:
            vtecString += vtecRecord['vtecstr'] + '\n'
            if vtecRecord['hvtecstr']:
                vtecString += vtecRecord['hvtecstr'] + '\n'
        return vtecString

    def _areaList(self, segment):
        ugcList = []
        for area in segment['impactedAreas']:
            ugcList.append(area['ugc'])
        ugcList.sort()
        return self._tpc.formatUGC_names(ugcList) + '\n'

    def _cityList(self, segment):
        cities = 'Including the cities of '
        elements = KeyInfo.getElements('cityList', segment)
        cityList = segment[elements[0]]
        cities += self._tpc.getTextListStr(cityList)
        return cities + '\n'

    def _callsToAction(self, segment):
        text = ''
        elements = KeyInfo.getElements('callsToAction', segment)
        if len(elements) > 0:
            callsToAction = segment[elements[0]]
        else:
            callsToAction = segment['callsToAction']
        if len(callsToAction) > 0:
            text = 'Precautionary/Preparedness actions...\n\n'
            for cta in callsToAction:
                cta = cta.strip('\n\t\r')
                cta = re.sub('\s+',' ',cta)
                text += cta + '\n\n'
            text += '&&\n\n'
        return text

    def _polygonText(self, segment):
        polyStr = 'LAT...LON'

        polygonPointLists = []
        for geometry in segment['geometry']:
            polygonPointLists.append(list(geometry.exterior.coords))

        for polygon in polygonPointLists:
            # 4 points per line
            pointsOnLine = 0
            for lon,lat in polygon:
                if pointsOnLine == 4:
                    polyStr += '\n' 
                    pointsOnLine = 0
                # For end of Aleutians
                if lat > 50 and lon > 0 :
                    lon = 360 - lon
                elif lon < 0 :
                    lon = -lon
                lon = int(100 * lon + 0.5)
                if lat < 0 :
                    lat = -lat
                lat = int(100 * lat + 0.5)
                polyStr += ' ' + str(lat) + ' ' + str(lon)
                pointsOnLine += 1
        return polyStr + '\n'

    ###################### Section Level

    def _attribution(self, hazard, productDict):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(hazard, productDict)

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
            elif action == 'EXA':
                attribution = nwsPhrase + 'expanded the...'
            elif action == 'EXT':
                attribution = 'the ' + hazName + ' is now in effect for...' 
            elif action == 'EXB':
                attribution = nwsPhrase + 'expanded the...'
            elif action == 'CAN':
                attribution = 'the ' + hazName + \
                   ' for... ' + areaPhrase + ' has been cancelled.'
            elif action == 'EXP':
                expTimeCurrent = self._startTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' has expired.'
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, [hazardEvent], expTimeCurrent)
                   attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        return attribution + '\n'

    def _firstBullet(self, hazard, productDict):
        headPhrase = '* '
        areaPhrase = self.createAreaPhrase(hazard, productDict)

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
            elif action == 'EXA':
                headPhrase += hazName + ' to include'
                headPhrase += ' ' + areaPhrase
            elif action == 'EXT':
                headPhrase += ' ' + areaPhrase
            elif action == 'EXB':
                headPhrase += hazName + ' to include'
                headPhrase += ' ' + areaPhrase

        return headPhrase

    def _basisBullet(self, segment):
        bulletText = '* '
        if self._runMode == 'Practice':
            bulletText += 'This is a test message.  '

        elements = KeyInfo.getElements('basis', segment)
        if len(elements) > 0:
            basis = segment[elements[0]]
        else:
            basis = segment['basis']

        if basis is None :
             basis = '...Flash Flooding was reported'

        # Create basis statement
        vtecRecords = segment['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            eventTime = vtecRecord.get('startTime')
            eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z ', stripLeading=True, timeZones=self.timezones)
            bulletText += 'At ' + eventTime
            bulletText += basis + '\n\n'
        return bulletText

    def _timeBullet(self, segment, roundMinutes=15):
        endTime = self._endTime
        expireTime = self.round(endTime, roundMinutes)
        text = '* '
        if (self._runMode == 'Practice'):
            text += "This is a test message.  "

        text += 'Until '
        timeStr = ''
        for tz in self.timezones:
            if len(timeStr) > 0:
                timeStr += '/'
            timeStr += self._tpc.formatDatetime(expireTime, '%l%M %p %Z', tz).strip()

        text += timeStr + '\n'
        return text

    def _pointImpactsBullet(self, segmentDict):
        impacts = segmentDict.get('impactsStringForStageFlowTextArea')

        bulletContent = ''
        ctaText = self._callsToAction(segmentDict)
        if ctaText:
            bulletContent = ' '.join(ctaText)

        if impacts:
           impactsString = impacts + ' ' + bulletContent
        else:
           impactsString =  bulletContent
        return impactsString

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

    def formatCityList(self, segment):
        impactedLocationsDict = segment['impactedLocations']
        elements = KeyInfo.getElements('cityList', segment['impactedLocations'])
        if len(elements) > 0:
             locations = impactedLocationsDict[elements[0]]
        else:
             locations = impactedLocationsDict['cityList']

        text = ''
        counter = 0
        size = len(locations)
        if size == 1:
            text += locations[0] + '.'
        else:        
            for location in locations:
                text += location.capitalize()
                if counter == size - 1:
                    text += '.'
                    break
                elif counter == size - 2:
                    text += ' and '
                else:
                    text += '...'
                counter += 1
        return text

    def createAreaPhrase(self, hazard, productDict):
        ugcList = []
        ugcPortions = {}
        ugcPartsOfState = {}
        
        for area in productDict['impactedAreas']:
            ugcList.append(area['ugc'])
            ugcPortions[area['ugc']] = area['portions']
            ugcPartsOfState[area['ugc']] = area['partsOfState']

        # These need to be ordered by area of state.
        orderedUgcs = []
        for ugc in ugcList :
            orderedUgcs.append(ugc[:2]+ugcPartsOfState.get(ugc, "")+"|"+ugc)
        orderedUgcs.sort()

        areaPhrase = ''
        for ougc in orderedUgcs :
            ugc = ougc.split("|")[1]
            part = ugcPortions.get(ugc, "")
            if part == "" :
                textLine = "  "
            else :
                textLine = "  " + part + " "
            textLine += self._tpc.getInformationForUGC(ugc)+" "
            textLine += self._tpc.getInformationForUGC(ugc, "typeSingular")+" in "
            part = ugcPartsOfState.get(ugc, "")
            if part == "" :
                textLine += self._tpc.getInformationForUGC(ugc, "fullStateName")+"...\n"
            else :
                textLine += part+" "+self._tpc.getInformationForUGC(ugc, "fullStateName")+"...\n"
            areaPhrase += textLine
            
        return areaPhrase

    def createAdditionalComments(self, segment):
        additionalComments = ''
        elements = KeyInfo.getElements('additionalComments', segment)
        if len(elements) > 0:
            for x in segment[elements[0]]:
                additionalComment = x

                if additionalComment != '':
                    additionalComments += additionalComment + '\n\n' 
        return additionalComments

    def setSiteInfo(self):
        # Primary Site
        siteEntry = self._siteInfo.get(self._siteID)
        self._fullStationID = siteEntry.get('fullStationID')  # KBOU
        self._region = siteEntry.get('region')
        self._wfoCity = siteEntry.get('wfoCity')
        self._wfoCityState = siteEntry.get('wfoCityState')
        self._areaName = ''  # siteEntry.get('state')  #  'GEORGIA' 

        # Backup Site
        siteEntry = self._siteInfo.get(self._backupSiteID)        
        self._backupWfoCityState = siteEntry.get('wfoCityState')
        self._backupFullStationID = siteEntry.get('fullStationID')

    def _issuanceTimeDate(self):
        text = ''
        for tz in self.timezones:
            text = self._tpc.formatDatetime(self._startTime, '%l%M %p %Z %a %b %e %Y', tz).strip()
            # only need the first time zone for issue time
            break
        return text + '\n'

    def round(self, tm, roundMinute=15):
        discard = datetime.timedelta(minutes=tm.minute % roundMinute,
                             seconds=tm.second,
                             microseconds=tm.microsecond)
        tm -= discard
        if discard >= datetime.timedelta(minutes=roundMinute/2):
            tm += datetime.timedelta(minutes=roundMinute)
        return tm

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
