'''
    Description: Base class for all legacy formatters to inherit from.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 31, 2015    4937    Robert.Blum General cleanup and added business
                            logic for nextIssuanceStatement from V2.
'''

import FormatTemplate
import datetime
import collections
import types, re, sys, copy, os
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from QueryAfosToAwips import QueryAfosToAwips
from Bridge import Bridge
from TextProductCommon import TextProductCommon
import ProductTextUtil
from BasisText import BasisText
from AttributionFirstBulletText import AttributionFirstBulletText

from abc import *

class Format(FormatTemplate.Formatter):

    def initialize(self) :
        self.bridge = Bridge()
        self.basisText = BasisText()
        areaDict = self.bridge.getAreaDictionary()
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)
        self.timezones = []

        self._productID = self.productDict.get('productID')
        self._productName = self.productDict.get('productName')
        self._productCategory = self.productDict.get('productCategory')
        self._siteID = self.productDict.get('siteID')
        self._backupSiteID = self.productDict.get('backupSiteID')
        self._issueTime = self.productDict.get('issueTime')
        self._issueFlag = self.productDict.get('issueFlag')
        self._purgeHours = self.productDict.get('purgeHours')
        self._runMode = self.productDict.get('runMode')
        if self._runMode == 'Practice':
            self._testMode = True
        self._siteInfo = self.bridge.getSiteInfo()
        self.setSiteInfo()

    def _printDebugProductParts(self):   
        # IF True will print the product parts and associated text during execution
        return False

    def _processProductParts(self, productDict, productParts, skipParts=[]):
        text = ''
        if type(productParts) is collections.OrderedDict:
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
            if name in self.productPartMethodMapping:
                partText = self.productPartMethodMapping[name](productDict)
            elif name in ['setUp_product', 'setUp_segment', 'setUp_section']:
                pass
            elif name == 'endSegment':
                partText = '\n$$\n\n' 
            elif name == 'CR':
                partText = '\n'
            elif name in ['segments', 'sections']:
                partText = self.processSubParts(productDict.get(name), infoDicts)
            else:
                textStr = self._tpc.getVal(productDict, name)
                if textStr:
                    partText = textStr + '\n' 

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print partText

            if partText is not None:
                text += partText
                if part in self._editableProductParts:
                    self._editableParts[part] = partText
        return text

    @abstractmethod
    def execute(self, eventSet, dialogInputMap):
        '''
        Must be overridden by the Product Formatter
        '''
        pass

    def _createTextProduct(self):
        # Setup the Time Zones
        segments = self.productDict.get('segments', None)
        if segments is not None:
            for segment in segments:
                self.timezones += segment.get('timeZones')
        # Remove duplicate timezones by converting to set
        self.timezones = set(self.timezones)
        # Return timezones back to a list
        self.timezones = list(self.timezones)

        # Build the text product based on the productParts
        text = ''
        productParts = self.productDict.get('productParts')
        text += self._processProductParts(self.productDict, productParts)
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
    #  Note that these methods are shared between all product
    #  formatters. If a change does not apply for all products
    #  the method must be overridden in that specific formatter.
    ######################################################

    ################# Product Level

    def _wmoHeader(self, productDict):
        siteEntry = self._siteInfo.get(self._siteID)
        fullStationID = siteEntry.get('fullStationID')  # KBOU
        ddhhmmTime = self._tpc.getFormattedTime(self._issueTime, '%d%H%M', stripLeading=False)
        a2a = QueryAfosToAwips(self._productID, self._siteID)
        wmoID = a2a.getWMOprod()  # e.g. WUUS53
        header = wmoID + ' ' + fullStationID + ' ' + ddhhmmTime + '\n'
        header += self._productID + self._siteID
        return header + '\n\n'

    def _productHeader(self, productDict):
        text = ''

        if (self._runMode == 'Practice'):
            text += 'Test...' + self._productName + '...Test \n'
        else:
            text += self._productName + '\n'
        text += 'National Weather Service ' + self._wfoCityState + '\n'
        text += self._issuanceTimeDate(productDict)
        return text

    def _easMessage(self, productDict):
        segments = self._tpc.getVal(productDict, 'segments', altDict=self.productDict)
        for segment in segments:
            vtecRecords = segment.get('vtecRecords')
            for vtecRecord in vtecRecords:
                if vtecRecord.get('sig') is 'A':
                    return 'Urgent - Immediate broadcast requested\n'
        return 'Bulletin - EAS activation requested\n'

    def _overviewSynopsis_area(self, productDict):
        productLabel = productDict.get('productLabel')
        synopsisKey = 'overviewSynopsis_' + productLabel
        synopsis = productDict.get(synopsisKey, '')
        if synopsis != '':
            synopsis += '\n\n'
        return synopsis

    def _overviewSynopsis_point(self, productDict):
        productLabel = productDict.get('productLabel')
        synopsisKey = 'overviewSynopsis_' + productLabel
        synopsis = productDict.get(synopsisKey, '')
        if synopsis != '':
            synopsis += '\n\n'
        return synopsis

    def _overviewHeadline_point(self, productDict):
        new_ext_productSegments = []
        con_productSegments = []
        can_exp_productSegments = [] 
        for segment in self.productDict.get('segments'):
            vtecRecords = segment.get('vtecRecords')
            for vtecRecord in vtecRecords:
                action = vtecRecord.get('act')
                if action == 'COR':
                    action = vtecRecord.get('prevAct')
                if action in ('NEW','EXT'):
                    new_ext_productSegments.append(segment)
                elif action == 'CON':
                    con_productSegments.append(segment)
                else:  # CAN, EXP
                    can_exp_productSegments.append(segment) 

        overviewHeadline = ''
        for segments in  [
                        new_ext_productSegments,
                        con_productSegments,
                        can_exp_productSegments]: 
            if segments:
                overviewHeadline += self.getOverviewHeadlinePhrase(segments)
        return overviewHeadline

    def _callsToAction_productLevel(self, productDict):
        text = ''
        productLabel = productDict.get('productLabel')
        ctaKey = 'callsToAction_productLevel_' + productLabel

        elements = KeyInfo.getElements(ctaKey, productDict)
        if len(elements) > 0:
            callsToAction = productDict.get(elements[0])
        else:
            callsToAction = productDict.get(ctaKey, None)

        if callsToAction:
            text = 'Precautionary/Preparedness actions...\n\n'
            for cta in callsToAction:
                cta = cta.strip('\n\t\r')
                cta = re.sub('\s+',' ',cta)
                text += cta + '\n\n'
        return text

    def _nextIssuanceStatement(self, productDict):
        expireTime = self._tpc.getExpireTime(
                    self._issueTime, self._purgeHours, [], fixedExpire=True)
        ### want description from timingWordTableFUZZY4, hence [2]
        ### Using only first timezone. Don't think 1 hr diff enough to include extra description
        nextIssue = self._tpc.timingWordTableFUZZY4(self._issueTime, expireTime, self.timezones[0], 'startTime')[2] + ' at'
        for timeZone in self.timezones:
            fmtIssueTme = self._tpc.getFormattedTime(expireTime, timeZones=[timeZone], format='%I%M %p %Z')
            ### If more than one timezone, will read like: 
            ### "The next statement will be issued at Tuesday morning at 600 AM CST. 500 AM MST."
            nextIssue += ' ' + fmtIssueTme + '.'
        
        partText = 'The next statement will be issued at ' + nextIssue
        partText += '\n\n&&\n\n'
        return partText

    def _additionalInfoStatement(self, productDict):
        # Please override this method for your site
        return 'Additional information is available at <Web site URL>.\n\n'

    def _rainFallStatement(self, productDict):
        return 'The segments in this product are river forecasts for selected locations in the watch area.\n\n'

    ################# Segment Level
    
    def _setUp_segment(self, segmentDict):
        # Save off the segmentDict so that all section productParts have a reference to it
        self._segmentDict = segmentDict
        # ToDo -- Post-Hydro there could be more than one section in a segment
        sectionDict = segmentDict.get('sections')[0]
        self.attributionFirstBullet = AttributionFirstBulletText()
        self.attributionFirstBullet.initialize(
            sectionDict, self._productID, self._issueTime, self._testMode, self._wfoCity, self._tpc)
        return ''

    def _emergencyHeadline(self, segmentDict):
        partText = ''
        includeChoices = segmentDict.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            partText += '...Flash Flood Emergency for ' + segmentDict.get('includeEmergencyLocation') + '...\n\n'
        return partText

    def _ugcHeader(self, segmentDict):
        ugcHeader = self._tpc.formatUGCs(segmentDict.get('ugcs'), segmentDict.get('expireTime'))
        return ugcHeader + '\n'

    def _areaList(self, segmentDict):
        return self._tpc.formatUGC_names(segmentDict.get('ugcs')) + '\n'

    def _cityList(self, segmentDict):
        cities = 'Including the cities of '
        cityList = segmentDict.get('cityList', [])
        cities += self._tpc.getTextListStr(cityList)
        return cities + '\n'

    def _callsToAction(self, segmentDict):
        text = ''
        elements = KeyInfo.getElements('callsToAction', segmentDict)
        if len(elements) > 0:
            callsToAction = segmentDict.get(elements[0])
        else:
            callsToAction = segmentDict.get('callsToAction', None)

        if callsToAction:
            text = 'Precautionary/Preparedness actions...\n\n'
            for cta in callsToAction:
                cta = cta.strip('\n\t\r')
                cta = re.sub('\s+',' ',cta)
                text += cta + '\n\n'
            text += '&&\n\n'

        return text

    # This can be called either at the segment or section level
    # depending on hazard type.
    def _endingSynopsis(self, dictionary):
        # Try to get from the dictionary
        endingSynopsis = dictionary.get('endingSynopsis')
        if endingSynopsis is None:
            # Try to get from sectionDictionary
            sections = dictionary.get('sections')
            if sections:
                endingSynopsis = sections[0].get('endingSynopsis')

            if endingSynopsis is None:
                # Try to get from dialogInputMap  (case of partial cancellation)
                elements = KeyInfo.getElements('endingSynopsis', self.productDict)
                if len(elements) > 0:
                    endingSynopsis = self.productDict.get(elements[0], None)
                else:
                     endingSynopsis = self.productDict.get('endingSynopsis', None)

        if endingSynopsis is None:
            # If still none use framed text
            return '|* Brief post-synopsis of hydrometeorological activity *|\n\n'

        return endingSynopsis + '\n\n'

    def _polygonText(self, segmentDict):
        polyStr = ''

        if (self._runMode == 'Practice'):
            polyStr += 'This is a test message. Do not take action based on this message. \n\n'
    
        polyStr += 'LAT...LON'

        polygonPointLists = []
        for section in segmentDict.get('sections'):
            for geometry in section.get('geometry'):
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

    def _issuanceTimeDate(self, segmentDict):
        text = ''
        for tz in self.timezones:
            text = self._tpc.formatDatetime(self._issueTime, '%l%M %p %Z %a %b %e %Y', tz).strip()
            # only need the first time zone for issue time
            break
        text += '\n'

        # The upper() is needed to pass the WarningDecoder.
        text.upper()

        if (self._runMode == 'Practice'):
            text += '\n...This message is for test purposes only...\n'

        return text

    def _summaryHeadlines(self, segmentDict, includeTiming=True):
        '''
        Creates the summary Headline
        
        @param segmentDict:  dictionary for the segment.
        '''
        # TODO Need to verify that PGFv3 preserves the full
        # full functionality of the 'getHeadlinesAndSections' in
        # TextProductCommon. V3 has split that method into this 
        # method and also '_createSections' in Legacy_Base_Generator.
        headlines = []
        vtecRecords = segmentDict.get('vtecRecords', None)
        hList = copy.deepcopy(vtecRecords)
        if len(hList):
            hList.sort(self._tpc.regularSortHazardAlg)

        while len(hList) > 0:
            vtecRecord = hList[0]

            # Can't make phrases with vtecRecords with no 'hdln' entry 
            if vtecRecord['hdln'] == '':
                hList.remove(vtecRecord)
                continue

            # make sure the vtecRecord is still in effect or within EXP criteria
            if (vtecRecord['act'] != 'EXP' and self._issueTime >= vtecRecord['endTime']) or \
            (vtecRecord['act'] == 'EXP' and self._issueTime > 30*60 + vtecRecord['endTime']):
                hList.remove(vtecRecord)
                continue # no headline for expired vtecRecords

            #assemble the vtecRecord type
            hazStr = vtecRecord['hdln']

            #determine the actionWords
            actionWords = self._tpc.actionControlWord(vtecRecord, self._issueTime)

            # get the immediateCause defaulting to 'ER' if None
            immediateCause = segmentDict.get('immediateCause', 'ER')

            if immediateCause in ['DM']:
                #get the hydrologicCause, damName, and streamName
                hydrologicCause = segmentDict.get('hydrologicCause')

                # Inconsisentency between FFW and FFA attribute name below
                damName = segmentDict.get('damOrLeveeName', None)
                if not damName:
                    dameName = segmentDict.get('damName', None)
                streamName = segmentDict.get('riverName', None)
                if hydrologicCause and hydrologicCause == 'siteImminent':
                    hazStr = hazStr + ' for the imminent failure of '
                else:
                    hazStr = hazStr + ' for the failure of '
                
                if damName:
                    hazStr += damName
                else:
                    hazStr += 'the dam'

                if streamName:
                    hazStr += ' on the ' + streamName
                hazStr += ' ' + actionWords

            elif immediateCause in ['DR', 'GO', 'IJ', 'RS', 'SM']:
                typeOfFlooding = self.typeOfFloodingMapping(immediateCause)
                typeOfFlooding.lower()
                if typeOfFlooding:
                    hazStr += ' for ' + typeOfFlooding + ' ' +actionWords
                else:
                    hazStr = hazStr + ' ' + actionWords
            else:
                # add on the action
                hazStr = hazStr + ' ' + actionWords

            if includeTiming:
                timeWords = self._tpc.getTimingPhrase(vtecRecord, [], self._issueTime, timeZones=self.timezones)
                if len(timeWords):
                    hazStr = hazStr + ' ' + timeWords

            if vtecRecord.get('phen') == 'FF' and vtecRecord.get('sig') != 'A':
                ugcPhrase = self._tpc.getAreaPhrase(segmentDict.get('ugcs'))
                hazStr += ' for ' + ugcPhrase

            if len(hazStr):
                # Call user hook
                localStr = self._tpc.hazard_hook(
                  None, None, vtecRecord['phen'], vtecRecord['sig'], vtecRecord['act'],
                  vtecRecord['startTime'], vtecRecord['endTime'])  # May need to add leading space if non-null 
                headlineStr = '...' + hazStr + localStr + '...\n'

            # Add replaceStr
            replacedBy = segmentDict.get('replacedBy')
            replaces = segmentDict.get('replaces')
            if replacedBy:
                replaceStr =  '...REPLACED BY ' + replacedBy + '...\n'
            elif replaces:
                replaceStr =  '...REPLACES ' + replaces + '...\n'
            else:
                replaceStr = ''
            headlineStr += replaceStr

            # always remove the main vtecRecord from the list
            hList.remove(vtecRecord)
        return headlineStr

    def _basisAndImpactsStatement_segmentLevel(self, segmentDict):
        sectionDict = segmentDict.get('sections', {})[0]
        statement = sectionDict.get('basisAndImpactsStatement_segmentLevel', '')
        if statement == '':
            statement = '|* Current hydrometeorological situation and expected impacts *|'
        return statement + '\n\n'

    def _endSegment(self, segmentDict):
        # Reset to empty dictionary
        self._segmentDict = {}
        return '\n$$\n\n' 

    ###################### Section Level

    def _vtecRecords(self, sectionDict):
        vtecString = ''
        vtecRecords = sectionDict.get('vtecRecords')
        for vtecRecord in vtecRecords:
            vtecString += vtecRecord['vtecstr'] + '\n'
            if 'hvtecstr' in vtecRecord:
                if vtecRecord['hvtecstr'] != None:
                    vtecString += vtecRecord['hvtecstr'] + '\n'
        return vtecString

    def _attribution(self, sectionDict):
        return self.attributionFirstBullet.getAttributionText()

    def _attribution_point(self, sectionDict):
        return self.attributionFirstBullet.getAttributionText()

    def _firstBullet(self, sectionDict):
        return '* '+ self.attributionFirstBullet.getFirstBulletText()

    def _firstBullet_point(self, sectionDict):
        firstBullet = ''
        if sectionDict.get('vtecRecord').get('act') == 'NEW':
            firstBullet += '* '
        firstBullet += self.attributionFirstBullet.getFirstBulletText()
        return firstBullet

    def _timeBullet(self, sectionDict, roundMinutes=15):
        endTime = sectionDict.get('endTime')
        expireTime = self.round(endTime, roundMinutes)
        text = '* '
        if (self._runMode == 'Practice' and sectionDict.get('geoType') != 'point'):
            text += "This is a test message.  "

        text += 'Until '
        timeStr = ''
        for tz in self.timezones:
            if len(timeStr) > 0:
                timeStr += '/'
            timeStr += self._tpc.formatDatetime(expireTime, '%l%M %p %Z', tz).strip()

        text += timeStr

        return text + '\n'

    def _emergencyStatement(self, sectionDict):
        includeChoices = sectionDict.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            return '  This is a Flash Flood Emergency for ' + sectionDict.get('includeEmergencyLocation') + '.\n\n'
        else:
            return ''

    def _impactsBullet(self, sectionDict):
        bulletText = '* '
        if (self._runMode == 'Practice'):
            bulletText += "This is a test message.  "

        impacts = sectionDict.get('impacts')
        if not impacts:
            impacts = self._tpc.frame('(Optional) Potential impacts of flooding')
            
        bulletText += impacts + '\n'
        return bulletText + '\n'

    def _basisAndImpactsStatement(self, sectionDict):
        #TODO Warngen seems to just have the basis here. Does impacts
        # still need to be added?
        bulletText = sectionDict.get('basisAndImpactsStatement',
                            '|* Current hydrometeorological situation and expected impacts *|')
        bulletText += '\n'

        # Add any additional comments here, listOfDrainages, floodMoving, etc.
        additionalCommentsText = self.createAdditionalComments(sectionDict)
        if additionalCommentsText:
            bulletText += '\n' + additionalCommentsText
        return bulletText

    def _locationsAffected(self, sectionDict):
        vtecRecord = sectionDict.get('vtecRecord', {})
        action = vtecRecord.get('act', None)
        if action in ['NEW', 'EXT']:
            heading = '* '
        else:
            heading = ''
        locationsAffected = ''

        if (self._runMode == 'Practice'):
            heading += "This is a test message.  "

        immediateCause = sectionDict.get('immediateCause', None)
        if immediateCause == 'DM' or immediateCause == 'DR':
            damOrLeveeName = sectionDict.get('damOrLeveeName')
            if damOrLeveeName:
                damInfo = self._damInfo().get(damOrLeveeName)
                if damInfo:
                    # Scenario
                    scenario = sectionDict.get('scenario')
                    if scenario:
                        scenarios = damInfo.get('scenarios')
                        if scenarios:
                            scenarioText = scenarios.get(scenario)
                            if scenarioText:
                                locationsAffected += scenarioText + '\n\n'
                    # Rule of Thumb
                    ruleOfThumb = damInfo.get('ruleOfThumb')
                    if ruleOfThumb:
                        locationsAffected += ruleOfThumb + '\n\n'

        # Need to check for List of cities here
        if sectionDict.get('citiesListFlag', False) == True:
            locationsAffected += 'Locations impacted include...' + self._tpc.getTextListStr(self._segmentDict.get('cityList', [])) + '\n\n'

        # Add any other additional Info
        locationsAffected += self.createAdditionalComments(sectionDict)

        if not locationsAffected:
            phen = vtecRecord.get("phen")
            sig = vtecRecord.get("sig")
            geoType = sectionDict.get('geoType')
            if phen in ["FF", "FA", "TO", "SV", "SM", "EW" , "FL" ] and \
               geoType == 'area' and sig != "A" :
                if phen == "FF" :
                    locationsAffected = "Some locations that will experience flash flooding include..."
                elif phen == "FA" or phen == "FL" :
                    locationsAffected = "Some locations that will experience flooding include..."
                else :
                    locationsAffected = "Locations impacted include..."
                locationsAffected += self.createImpactedLocations(sectionDict) + '\n\n'
            else :
                locationsAffected = '|*Forecast path of flood and/or locations to be affected*|' + '\n\n'
        return heading + locationsAffected

    ###################### Utility methods

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

    def createImpactedLocations(self, sectionDict):
        nullReturn = "mainly rural areas of the aforementioned areas."
        elements = KeyInfo.getElements('impactedLocations', sectionDict)
        if len(elements) > 0:
             locations = sectionDict.get(elements[0], [])
        else:
             locations = sectionsDict.get('impactedLocations', [])
             
        if locations:
            return self._tpc.formatDelimitedList(locations)
        return nullReturn

    def getOverviewHeadlinePhrase(self, segments, lineLength=69):
        '''
            ...THE NATIONAL WEATHER SERVICE IN DES MOINES HAS ISSUED A FLOOD
            ADVISORY FOR THE WINNEBAGO RIVER AT MASON CITY...SHELL ROCK RIVER
            AT MARBLE ROCK...CEDAR RIVER AT JANESVILLE...CEDAR RIVER AT CEDAR
            FALLS...AND CEDAR RIVER AT WATERLOO...
        '''
        locationPhrases = []
        areaGroups = []

        # There could be multiple points sharing a VTEC code e.g. NEW
        for segment in segments:
            ugcs = segment.get('ugcs')

            pointAreaGroups = self._tpc.getGeneralAreaList(ugcs)
            areaGroups += pointAreaGroups

            nameDescription, nameTypePhrase = self._tpc.getNameDescription(pointAreaGroups)
            affected = nameDescription + ' '+ nameTypePhrase
            section = segment.get('sections')[0]
            riverName = section.get('riverName_GroupName')
            proximity = section.get('proximity')
            if proximity is None:
                proximity = 'near'
            riverPointName = section.get('riverPointName')
            locationPhrases.append(riverName + ' ' + proximity + ' ' + riverPointName + ' affecting ' + affected + '.')

        locationPhrase = '\n'.join(locationPhrases)
        areaGroups = self._tpc.simplifyAreas(areaGroups)
        states = self._tpc.getStateDescription(areaGroups)
        riverPhrase = 'the following rivers in ' + states

        nwsPhrase = 'The National Weather Service in ' + self._wfoCity
        # Use this to determine which first bullet format to use.
        vtecRecords = segment.get('vtecRecords')
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

            if len(hazName):
                action = vtecRecord.get('act')
                if action == 'COR':
                    action = vtecRecord.get('prevAct')

            if action == 'NEW':
                overview = nwsPhrase + ' has issued a ' + hazName + ' for '+ riverPhrase

            elif action == 'CON':
                overview = nwsPhrase + ' is continuing the ' + hazName + ' for '+ riverPhrase
    
            elif action == 'EXT':
                overview = nwsPhrase + ' is extending the ' + hazName + ' for '+ riverPhrase

            elif action == 'CAN':
                overview = nwsPhrase + ' is canceling the ' + hazName + ' for '+ riverPhrase
    
            elif action == 'EXP':
                expTimeCurrent = self._issueTime
                if vtecRecord.get('endTime') <= expTimeCurrent:
                    overview = 'The ' + hazName + \
                      ' for ' + riverPhrase + ' has expired '
                else:
                    timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
                    overview = 'The ' + hazName + \
                      ' for ' + riverPhrase + ' will expire ' + timeWords

            elif action == 'ROU':
                '''
                ...The National Weather Service in Omaha/Valley is releasing Test 
                FORECAST INFORMATION for the following rivers in southeast 
                Nebraska...
                '''
                overview = nwsPhrase + ' is releasing ' + hazName + ' for '+ riverPhrase

        overview = '...' + overview + '...\n\n'
        locationPhrase = self._tpc.indentText(locationPhrase, indentFirstString='  ',
              indentNextString='  ', maxWidth=69,
              breakStrings=[' ', '-', '...'])
        return overview + locationPhrase + '\n\n'

    def createAdditionalComments(self, segmentDict):
        additionalComments = ''
        elements = KeyInfo.getElements('additionalComments', segmentDict)
        if len(elements) > 0:
            for x in segmentDict.get(elements[0]):
                additionalComment = x

                if additionalComment != '':
                    additionalComments += additionalComment + '\n\n' 
        return additionalComments

    def _getFormattedTime(self, time_ms, format=None, stripLeading=True, emptyValue=None, timeZones=['GMT']):
        if not time_ms:
            return emptyValue
        if format is None:
            format = '%I%M %p %A %Z '
        return self._tpc.getFormattedTime(
                time_ms, format, stripLeading=stripLeading, timeZones=timeZones)

    def round(self, tm, roundMinute=15):
        discard = datetime.timedelta(minutes=tm.minute % roundMinute,
                             seconds=tm.second,
                             microseconds=tm.microsecond)
        tm -= discard
        if discard >= datetime.timedelta(minutes=roundMinute/2):
            tm += datetime.timedelta(minutes=roundMinute)
        return tm

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

