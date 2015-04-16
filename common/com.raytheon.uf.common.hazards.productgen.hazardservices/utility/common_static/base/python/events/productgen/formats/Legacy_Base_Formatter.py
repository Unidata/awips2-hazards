'''
    Description: Base class for all legacy formatters to inherit from.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 31, 2015    4937    Robert.Blum General cleanup and added business
                                        logic for nextIssuanceStatement from V2.
    Mar 16, 2015    6955    Robert.Blum Improved logic for timeBullet to add date/day when needed.
    Mar 20, 2015    7149    Robert.Blum Adjusted _callsToAction() to handle a string
                                        instead of a list.
    Apr 16, 2015    7579    Robert.Blum Updates for amended Product Editor.
'''

import FormatTemplate
import datetime
from collections import OrderedDict
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

        # Dictionary that will hold the KeyInfo entries of the
        # product part text strings to be displayed in the Product
        # Editor. 
        self._editableParts = OrderedDict()

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
        if type(productParts) is OrderedDict:
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

        self._setVal('wmoHeader', header, productDict, 'WMO Header', editable=False)
        return header + '\n\n'

    def _productHeader(self, productDict):
        text = self._productName

        if self.productDict.get('correction', False):
            text += '...CORRECTED'
        if (self._runMode == 'Practice'):
            text = 'Test...' + text + '...Test \n'
        else:
            text += '\n'
        text += 'National Weather Service ' + self._wfoCityState + '\n'
        text += self.getIssuanceTimeDate(productDict)

        self._setVal('productHeader', text, productDict, 'Product Header', editable=False)
        return text

    def _easMessage(self, productDict):
        segments = self._tpc.getVal(productDict, 'segments', altDict=self.productDict)
        easText = 'Bulletin - EAS activation requested'
        for segment in segments:
            vtecRecords = segment.get('vtecRecords')
            for vtecRecord in vtecRecords:
                if vtecRecord.get('sig') is 'A':
                    easText = 'Urgent - Immediate broadcast requested'
                    break
        self._setVal('easMessage', easText, productDict, 'EAS Meassage', editable=False)
        return easText + '\n'

    def _overviewSynopsis_area(self, productDict):
        return self.overviewSynopsis(productDict)

    def _overviewSynopsis_point(self, productDict):
        return self.overviewSynopsis(productDict)

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
        self._setVal('overviewHeadline', overviewHeadline, productDict, 'Overview Headline')
        return overviewHeadline

    def _callsToAction_productLevel(self, productDict):
        productLabel = productDict.get('productLabel')
        ctaKey = 'callsToAction_productLevel_' + productLabel
        text = self._getSavedVal(ctaKey, productDict)

        if not text:
            callsToAction =  self._tpc.getVal(productDict, ctaKey, None)
            if callsToAction:
                text = ''
                for cta in callsToAction:
                    cta = cta.strip('\n\t\r')
                    cta = re.sub('\s+',' ',cta)
                    text += cta + '\n\n'
                text = text.rstrip()
        self._setVal('callsToAction_productLevel', text, productDict, 'Calls To Action - Product Level')
        text = 'Precautionary/Preparedness actions...\n\n' + text + '\n\n'
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
        self._setVal('nextIssuanceStatement', partText, productDict, 'Next Issuance Statement')
        return partText + '\n\n&&\n\n'

    def _additionalInfoStatement(self, productDict):
        # Get saved value from productText table if available
        text = self._getSavedVal('additionalInfoStatement', productDict)
        if not text:
            # Please override this method for your site
            text = 'Additional information is available at <Web site URL>.'
        self._setVal('additionalInfoStatement', text, productDict, 'Additional Info Statement')
        return text + '\n\n'

    def _rainFallStatement(self, productDict):
        # Get saved value from productText table if available
        text = self._getSavedVal('rainFallStatement', productDict)
        if not text:
            text = 'The segments in this product are river forecasts for selected locations in the watch area.'
        self._setVal('rainFallStatement', text, productDict, 'Rainfall Statement')
        return text + '\n\n'

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
            # Get saved value from productText table if available
            partText = self._getSavedVal('emergencyHeadline', segmentDict)
            if not partText:
                partText += '...Flash Flood Emergency for ' + segmentDict.get('includeEmergencyLocation') + '...'
            self._setVal('emergencyHeadline', partText, segmentDict, 'Emergency Headline')
            partText += '\n\n'
        return partText

    def _ugcHeader(self, segmentDict):
        ugcHeader = self._tpc.formatUGCs(segmentDict.get('ugcs'), segmentDict.get('expireTime'))
        self._setVal('ugcHeader', ugcHeader, segmentDict, 'UGC Header', editable=False)
        return ugcHeader + '\n'

    def _areaList(self, segmentDict):
        areaList =  self._tpc.formatUGC_names(segmentDict.get('ugcs'))
        self._setVal('areaList', areaList, segmentDict, 'Area List', editable=False)
        return areaList + '\n'

    def _cityList(self, segmentDict):
        cities = 'Including the cities of '
        cityList = segmentDict.get('cityList', [])
        cities += self._tpc.getTextListStr(cityList)
        self._setVal('cityList', cities, segmentDict, 'City List', editable=False)
        return cities + '\n'

    def _callsToAction(self, segmentDict):
        # Get saved value from productText table if available
        text = self._getSavedVal('callsToAction', segmentDict)
        if not text:
            callsToAction =  self._tpc.getVal(segmentDict, 'callsToAction', '')
            if callsToAction and callsToAction != '':
                text = callsToAction.rstrip()
        self._setVal('callsToAction', text, segmentDict, 'Calls To Action')
        if text:
            text = 'Precautionary/Preparedness actions...\n\n' + text + '\n\n'
        return text + '&&\n\n'

    # This can be called either at the segment or section level
    # depending on hazard type.
    def _endingSynopsis(self, dictionary):
        # Get saved value from productText table if available
        endingSynopsis = self._getSavedVal('endingSynopsis', dictionary)
        if not endingSynopsis:
            # Try to get from the dictionary
            endingSynopsis = dictionary.get('endingSynopsis')
            if endingSynopsis is None:
                # Try to get from sectionDictionary
                sections = dictionary.get('sections')
                if sections:
                    endingSynopsis = sections[0].get('endingSynopsis')

                if endingSynopsis is None:
                    # Try to get from dialogInputMap  (case of partial cancellation)
                    endingSynopsis = self._tpc.getVal(self.productDict, 'endingSynopsis', None)

            if endingSynopsis is None:
                # If still none use framed text
                endingSynopsis = '|* Brief post-synopsis of hydrometeorological activity *|\n\n'

        self._setVal('endingSynopsis', endingSynopsis, dictionary, 'Ending Synopsis')
        return endingSynopsis + '\n\n'

    def _polygonText(self, segmentDict):
        polyStr = 'LAT...LON'

        polygonPointLists = []
        for section in segmentDict.get('sections'):
            for geometry in section.get('geometry'):
                subGeoType = geometry.type
                if subGeoType is not None and subGeoType is not 'LineString':
                    polygonPointLists.append(list(geometry.exterior.coords))
                else:
                    polygonPointLists.append(list(geometry.coords))

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
        self._setVal('polygonText', polyStr, segmentDict, 'Polygon Text', editable=False)
        if (self._runMode == 'Practice'):
            polyStr = 'This is a test message. Do not take action based on this message. \n\n' + polyStr
        return polyStr + '\n'

    def _issuanceTimeDate(self, segmentDict):
        text = self.getIssuanceTimeDate(segmentDict)
        self._setVal('issuanceTimeDate', text, segmentDict, 'Issuance Time', editable=False)
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

        self._setVal('summaryHeadlines', headlineStr, segmentDict, 'Summary Headlines')
        return headlineStr

    def _basisAndImpactsStatement_segmentLevel(self, segmentDict):
        # Get saved value from productText table if available
        statement = self._getSavedVal('basisAndImpactsStatement_segmentLevel', segmentDict)
        if not statement:
            sectionDict = segmentDict.get('sections', {})[0]
            statement = sectionDict.get('basisAndImpactsStatement_segmentLevel', None)
            # Check for a empty string from the HID
            if statement == '' or statement == None:
                statement = '|* Current hydrometeorological situation and expected impacts *|'
        self._setVal('basisAndImpactsStatement_segmentLevel', statement, segmentDict, 'Basis and Impacts Statement')
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

        self._setVal('vtecRecords', vtecString, sectionDict, 'VTEC Records', editable=False)
        return vtecString

    def _attribution(self, sectionDict):
        # Get saved value from productText table if available
        attribution = self._getSavedVal('attribution', sectionDict)
        if not attribution:
            attribution = self.attributionFirstBullet.getAttributionText()
        self._setVal('attribution', attribution, sectionDict, 'Attribution')
        return attribution + '\n\n'

    def _attribution_point(self, sectionDict):
        # Get saved value from productText table if available
        attribution = self._getSavedVal('attribution_point', sectionDict)
        if not attribution:
            attribution = self.attributionFirstBullet.getAttributionText()
        self._setVal('attribution_point', attribution, sectionDict, 'Attribution')
        return attribution + '\n\n'

    def _firstBullet(self, sectionDict):
        # Get saved value from productText table if available
        firstBullet = self._getSavedVal('firstBullet', sectionDict)
        if not firstBullet:
            firstBullet = self.attributionFirstBullet.getFirstBulletText()
        self._setVal('firstBullet', firstBullet, sectionDict, 'First Bullet')
        return '* ' + firstBullet + '\n\n'

    def _firstBullet_point(self, sectionDict):
        # Get saved value from productText table if available
        firstBullet = self._getSavedVal('firstBullet_point', sectionDict)
        if not firstBullet:
            firstBullet += self.attributionFirstBullet.getFirstBulletText()
        self._setVal('firstBullet_point', firstBullet, sectionDict, 'First Bullet')
        if sectionDict.get('vtecRecord').get('act') == 'NEW':
            firstBullet = '* ' + firstBullet
        return firstBullet + '\n'

    def _timeBullet(self, sectionDict, roundMinutes=15):
        '''
        Displays the expiration time/date of the hazard, based on the following logic. 
        When hazard extends:
        - Into the next day . . . should include day
        - More than 1 week . . . should include date
        '''
        # Get saved value from productText table if available
        bulletText = self._getSavedVal('timeBullet', sectionDict)
        if not bulletText:
            bulletText = ''
            if (self._runMode == 'Practice' and sectionDict.get('geoType') != 'point'):
                bulletText += "This is a test message.  "
            bulletText += 'Until '

            endTime = sectionDict.get('endTime')
            expireTime = self.round(endTime, roundMinutes)

            # Determine how far into the future the expire time is.
            issueTime = datetime.datetime.fromtimestamp(float(self._issueTime)/1000)
            tdelta = endTime - issueTime

            if (tdelta.days == 6 and endTime.date().weekday() == issueTime.date().weekday()) or \
                tdelta.days > 6:
                format = '%l%M %p %Z %a %b %d'
            elif issueTime.day != endTime.day:
                format = '%l%M %p %Z %a'
            else:
                format = '%l%M %p %Z'

            timeStr = ''
            for tz in self.timezones:
                if len(timeStr) > 0:
                    timeStr += '/'
                timeStr += self._tpc.formatDatetime(expireTime, format, tz).strip()
            bulletText += timeStr + '.'

        self._setVal('timeBullet', bulletText, sectionDict, 'Time Bullet')
        return '* ' + bulletText + '\n'

    def _emergencyStatement(self, sectionDict):
        includeChoices = sectionDict.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            # Get saved value from productText table if available
            statement = self._getSavedVal('emergencyStatement', sectionDict)
            if not statement:
                statement = '  This is a Flash Flood Emergency for ' + sectionDict.get('includeEmergencyLocation') + '.'
            self._setVal('emergencyStatement', statement, sectionDict, 'Emergency Statement')
            return statement + '\n\n'
        else:
            return ''

    def _impactsBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getSavedVal('impactsBullet', sectionDict)
        if not bulletText:
            bulletText = ''
            if (self._runMode == 'Practice'):
                bulletText += "This is a test message.  "

            impacts = sectionDict.get('impacts')
            if not impacts:
                impacts = self._tpc.frame('(Optional) Potential impacts of flooding')

            bulletText += impacts
        self._setVal('impactsBullet', bulletText, sectionDict, 'Impacts Bullet')
        return '* ' + bulletText + '\n\n'

    def _basisAndImpactsStatement(self, segmentDict):
        # Get saved value from productText table if available
        bulletText = self._getSavedVal('basisAndImpactsStatement', sectionDict)
        if not bulletText:
            bulletText = ''
            if (self._runMode == 'Practice'):
                bulletText += "This is a test message.  "
            statement = segmentDict.get('basisAndImpactsStatement', None)
            # Check for a empty string from the HID
            if statement == '' or statement == None:
                bulletText += '|* Current hydrometeorological situation and expected impacts *|'
            else:
                bulletText += statement
        self._setVal('basisAndImpactsStatement', bulletText, segmentDict, 'Basis and Impacts Bullet')
        return '* ' + bulletText + '\n\n'

    def _locationsAffected(self, sectionDict):
        vtecRecord = sectionDict.get('vtecRecord', {})
        action = vtecRecord.get('act', None)

        # Get saved value from productText table if available
        locationsAffected = self._getSavedVal('locationsAffected', sectionDict)
        if not locationsAffected:
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
                    
            locationsAffected = heading + locationsAffected
        self._setVal('locationsAffected', locationsAffected, sectionDict, 'Locations Affected')
        if action in ['NEW', 'EXT']:
            locationsAffected = '* ' + locationsAffected
        return locationsAffected

    ###################### Utility methods

    def overviewSynopsis(self, productDict):
        # Get saved value from productText table if available
        productLabel = productDict.get('productLabel')
        synopsisKey = 'overviewSynopsis_' + productLabel
        synopsis = self._getSavedVal(synopsisKey, productDict)

        if not synopsis:
            synopsis = productDict.get(synopsisKey, '')
        self._setVal('overviewSynopsis_' + productLabel, synopsis, productDict, 'Overview Synopsis')
        if synopsis:
            synopsis += '\n\n'
        return synopsis

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

    def getIssuanceTimeDate(self, segmentDict):
        text = ''
        for tz in self.timezones:
            text = self._tpc.formatDatetime(self._issueTime, '%l%M %p %Z %a %b %e %Y', tz).strip()
            # only need the first time zone for issue time
            break
        text += '\n'

        # The upper() is needed to pass the WarningDecoder.
        text.upper()
        return text

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

    def _setVal(self, key, value, dictionary, label=None, editable=True, displayable=True):
        '''
        Helper method to call _setVal() in TextProductCommon. This method automatically
        sets the productCategory=self._productCategory, productID='', and editable=True
        parameters for you.
        '''
        eventIDs, ugcList = self._tpc.parameterSetupForKeyInfo(dictionary)
        self._tpc.setVal(self._editableParts, key, value, editable=editable, eventIDs=eventIDs,
                         segment=ugcList, label=label, displayable=displayable,
                         productCategory=self._productCategory, productID='')

    def _getSavedVal(self, key, dictionary):
        '''
        Helper method to call _getSavedVal() in TextProductCommon. This method automatically
        sets the productCategory=self._productCategory, productID='' parameters for you.
        '''
        eventIDs, ugcList = self._tpc.parameterSetupForKeyInfo(dictionary)
        return self._tpc.getSavedVal(key, eventIDs=eventIDs, segment=ugcList, 
                                     productCategory=self._productCategory, productID='')

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

