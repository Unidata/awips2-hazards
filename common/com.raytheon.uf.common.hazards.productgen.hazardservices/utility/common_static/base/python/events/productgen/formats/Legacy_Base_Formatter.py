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
    Apr 27, 2015    7579    Robert.Blum Removed non-editable fields from Product Editor. Only the 
                                        ugcHeader and vtecString remain for context.
    Apr 28, 2015    7914    Robert.Blum Fixed indent error from merge.
    Apr 28, 2015    7579    Robert.Blum Updated CityList to get saved values from the productText table.
    Apr 30, 2015    7579    Robert.Blum Changes for multiple hazards per section.
    May 08, 2015    7864    Robert.Blum Added "Rain So Far" statement to basisAndImpacts product part.
    May 11, 2015    7918    Robert.Blum Moved round method to TextProductCommon and fixed bug
                                        when comparing dictionaries.
    May 07, 2015    6979    Robert.Blum EditableEntries are passed in for reuse.
    May 14, 2015    7376    Robert.Blum Changed to look for only None and not
                                        empty string. Also some additional bug fixes.
    May 21, 2015    7959    Robert.Blum Consolidated the Dam/Levee name into one attribute.
    May 21, 2015    8181    Robert.Blum Adjustments to cityList product part for being required vs optional.
    Jun 03, 2015    8530    Robert.Blum Misc. changes for getting the Product Text closer to the final thing.
    Jun 05, 2015    8531    Chris.Cody  Changes to conform to WarnGen/RiverPro outputs
    Jun 05, 2015    8530    Robert.Blum Additional changes to get Test Message statement correct.
    Jun 10, 2015    8532    Robert.Blum Changes for mixed case.
    Jun 26, 2015    8181    Robert.Blum Changes for cityList/locationsAffected. 
    Jun 26, 2015    7919    Robert.Blum Changes for EXP where they may not be a summaryHeadline.
    Jul 06, 2015    7747    Robert.Blum Changes for adding framed text when text fields are left blank on HID.
    Jul 21, 2015    9640    Robert.Blum Fixed hazard name in summaryHeadlines.
    Jul 27, 2015    9637    Robert.Blum Changes to _polygonText() for point hazards.
    Jul 28, 2015    9687    Robert.Blum Changes for new KeyInfo field - displayLabel.
    Jul 30, 2015    9681    Robert.Blum Fixed incorrect if statement in polygonText.
    Aug 01, 2015    9634    Robert.Blum Changing MND Header to all caps.
    Aug 03, 2015    9632    Robert.Blum Initials are now auto filled.
    Aug 19, 2015    9558    Robert.Blum Fixed _polygonText() to handle MultiPolygons
    Aug 24, 2015    9553    Robert.Blum Removed _basisAndImpactsStatement_segmentLevel()
    Aug 25, 2015    9638    Robert.Blum Initials product part is now optional and updates to 
                                        reflect changes from the product editor.
    Aug 25, 2015    9992    Robert.Blum Fixed product level CTAs when none are selected.
    Aug 25, 2015    9627    Robert.Blum Removed canceling wording from replacements.
    Sep 02, 2015    9637    Robert.Blum Removed gage point from polygonText and also duplicates.
    Sep 15, 2015    8687    Robert.Blum Changes to use DamMetaData.py.
'''

import FormatTemplate
import HazardConstants
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
import ForecasterInitials

from abc import *

class Format(FormatTemplate.Formatter):

    def initialize(self, editableEntries=None) :
        self.bridge = Bridge()
        self.basisText = BasisText()
        areaDict = self.bridge.getAreaDictionary()
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)
        self.timezones = []

        # Dictionary that will hold the KeyInfo entries of the
        # product part text strings to be displayed in the Product
        # Editor. 
        if editableEntries:
            self._useProductTextTable = False
            self._editableParts = editableEntries
        else:
            self._useProductTextTable = True
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
            elif name in ['setUp_product']:
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
    def execute(self, productDict, editableEntries=None):
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
        if self._siteID == HazardConstants.NATIONAL:
            self._siteID = 'OAX'
        siteEntry = self._siteInfo.get(self._siteID)
        fullStationID = siteEntry.get('fullStationID')  # KBOU
        ddhhmmTime = self._tpc.getFormattedTime(self._issueTime, '%d%H%M', stripLeading=False)
        a2a = QueryAfosToAwips(self._productID, self._siteID)
        wmoID = a2a.getWMOprod()  # e.g. WUUS53
        header = wmoID + ' ' + fullStationID + ' ' + ddhhmmTime + '\n'
        header += self._productID + self._siteID
        return header + '\n'

    def _productHeader(self, productDict):
        text = self._productName

        if self.productDict.get('correction', False):
            text += '...CORRECTED'
        if (self._runMode == 'Practice'):
            text = 'TEST...' + text + '...TEST \n'
        else:
            text += '\n'
        text += 'NATIONAL WEATHER SERVICE ' + self._wfoCityState + '\n'
        text += self.getIssuanceTimeDate(productDict)
        if (self._runMode == 'Practice' and self._productID == 'FFW'):
            text += '\n...THIS MESSAGE IS FOR TEST PURPOSES ONLY...\n'
        return text.upper()

    def _easMessage(self, productDict):
        # ALL CAPS per Mixed Case Text Guidelines
        segments = self._tpc.getVal(productDict, 'segments', altDict=self.productDict)
        easText = 'BULLETIN - EAS ACTIVATION REQUESTED'
        for segment in segments:
            vtecRecords = segment.get('vtecRecords')
            for vtecRecord in vtecRecords:
                if vtecRecord.get('sig') is 'A':
                    easText = 'URGENT - IMMEDIATE BROADCAST REQUESTED'
                    break
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

        overviewHeadline = self._getVal('overviewHeadline', productDict)
        if overviewHeadline is None:
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
        text = self._getVal(ctaKey, productDict)

        if text is None:
            text = ''
            callsToAction =  self._tpc.getVal(productDict, ctaKey, None)
            if callsToAction:
                text = ''
                for cta in callsToAction:
                    cta = cta.strip('\n\t\r')
                    cta = re.sub('\s+',' ',cta)
                    text += cta + '\n\n'
                text = text.rstrip()
        self._setVal(ctaKey, text, productDict, 'Calls To Action - Product Level', required=False)
        return self._getFormattedText(text, startText='Precautionary/Preparedness actions...\n\n', endText='\n\n')

    def _nextIssuanceStatement(self, productDict):
        # Get saved value from productText table if available
        partText = self._getVal('nextIssuanceStatement', productDict)
        if partText is None:
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
        return self._getFormattedText(partText, endText='\n\n') + '&&\n\n'

    def _additionalInfoStatement(self, productDict):
        # Get saved value from productText table if available
        text = self._getVal('additionalInfoStatement', productDict)
        if text is None:
            # Please override this method for your site
            text = 'Additional information is available at <Web site URL>.'
        self._setVal('additionalInfoStatement', text, productDict, 'Additional Info Statement')
        return self._getFormattedText(text, endText='\n\n')

    def _rainFallStatement(self, productDict):
        # Get saved value from productText table if available
        text = self._getVal('rainFallStatement', productDict)
        if text is None:
            text = 'The segments in this product are river forecasts for selected locations in the watch area.'
        self._setVal('rainFallStatement', text, productDict, 'Rainfall Statement')
        return self._getFormattedText(text, endText='\n\n')

    def _initials(self, productDict):
        if self._useProductTextTable == False:
            # Change on Product Editor use getVal()
            initials = self._getVal('initials', productDict)
        else:
            initials = ForecasterInitials.getForecasterIdentification()
        self._setVal('initials', initials, productDict, 'Initials', required=False)
        return initials

    ################# Segment Level

    def _setUp_segment(self, segmentDict):
        # Save off the segmentDict so that all section productParts have a reference to it
        self._segmentDict = segmentDict
        return ''

    def _ugcHeader(self, segmentDict):
        ugcHeader = self._tpc.formatUGCs(segmentDict.get('ugcs'), segmentDict.get('expireTime'))
        self._setVal('ugcHeader', ugcHeader, segmentDict, editable=False)
        return ugcHeader + '\n'

    def _areaList(self, segmentDict):
        areaList =  self._tpc.formatUGC_names(segmentDict.get('ugcs'))
        areaList = self._tpc.linebreak(areaList, 69, breakStr=[' ', '-'] ).rstrip()
        return areaList + '\n'

    def _cityList(self, segmentDict):
        # Get saved value from productText table if available
        required = None
        cityListText = self._getVal('cityList', segmentDict)
        if cityListText is None:
            cityListText = ''
            cityList = set()
            for sectionDict in segmentDict.get('sections', []):
                for hazard in sectionDict.get('hazardEvents', []):
                    phen = hazard.get('phen')
                    sig = hazard.get('sig')
                    phensig = phen + '.' + sig
                    listOfCities = hazard.get('listOfCities', [])
                    # Add the cities if boxed checked in HID
                    if 'selectListOfCities'in listOfCities:
                        cityList.update(hazard.get('cityList', []))
            if cityList:
                cityListText = 'Including the cities of '
                cityListText += self._tpc.getTextListStr(list(cityList))
        self._setVal('cityList', cityListText, segmentDict, 'City List', required=False)
        return self._getFormattedText(cityListText, endText='\n')

    def _callsToAction(self, segmentDict):
        # Get saved value from productText table if available
        callsToAction = self._getVal('callsToAction', segmentDict)
        if callsToAction is None:
            callsToAction =  self._tpc.getVal(segmentDict, 'callsToAction', '')
            if callsToAction and callsToAction != '':
                callsToAction = callsToAction.rstrip()
        self._setVal('callsToAction', callsToAction, segmentDict, 'Calls To Action', required=False)
        return self._getFormattedText(callsToAction, startText='Precautionary/Preparedness actions...\n\n', endText='\n\n&&\n\n')

    def _polygonText(self, segmentDict):
        isPointBasedHazard = False
        polyStr = ''

        polygonPointLists = []
        for section in segmentDict.get('sections', []):
            for hazard in section.get('hazardEvents', []):
                if hazard.get('geoType') == 'point':
                    isPointBasedHazard = True;
                for geometry in hazard.get('geometry'):
                    if geometry.geom_type == HazardConstants.SHAPELY_POLYGON:
                        polygonPointLists.append(list(geometry.exterior.coords))
                    elif geometry.geom_type == HazardConstants.SHAPELY_POINT:
                        continue # Dont add gage point to polygon text
                    elif geometry.geom_type == HazardConstants.SHAPELY_LINE:
                        polygonPointLists.append(list(geometry.coords))
                    else:
                        for geo in geometry:
                            polygonPointLists.append(list(geo.exterior.coords))

        lonLatPoints = []
        for polygon in polygonPointLists:
            for lon,lat in polygon:
                # For end of Aleutians
                if lat > 50 and lon > 0 :
                    lon = 360 - lon
                elif lon < 0 :
                    lon = -lon
                lon = int(100 * lon + 0.5)
                if lat < 0 :
                    lat = -lat
                lat = int(100 * lat + 0.5)
                lonLatPoints.append((lon, lat))

        # Remove duplicates now that the
        # points have been reduced
        lonLatDict = OrderedDict.fromkeys(lonLatPoints)
        

        # Construct the text string
        if isPointBasedHazard:
            # Point Based Hazard
            polyStr = '\n&&\n\n\n'
        polyStr += 'LAT...LON'

        # 4 points per line
        pointsOnLine = 0
        for lon, lat in lonLatDict:
            if pointsOnLine == 4:
                polyStr += '\n' 
                pointsOnLine = 0
            polyStr += ' ' + str(lat) + ' ' + str(lon)
            pointsOnLine += 1

        if (self._runMode == 'Practice' and isPointBasedHazard == False):
            polyStr = 'This is a test message. Do not take action based on this message. \n\n' + polyStr
        return polyStr + '\n'

    def _issuanceTimeDate(self, segmentDict):
        text = self.getIssuanceTimeDate(segmentDict)
        vtecRecords = segmentDict.get('vtecRecords', [])
        phensig = vtecRecords[0].get('key', '')
        if (self._runMode == 'Practice' and (phensig in ['FA.W', 'FA.Y'] or self._productID == 'FFS')):
            text += '\n...THIS MESSAGE IS FOR TEST PURPOSES ONLY...\n'
        return text

    def _summaryHeadlines(self, segmentDict, includeTiming=True):
        '''
        Creates the summary Headline
        @param segmentDict:  dictionary for the segment.
        '''
        # Get saved value from productText table if available
        headlineStr = self._getVal('summaryHeadlines', segmentDict)
        if headlineStr is None:
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

                # Get the corresponding section dictionary
                section = None
                for sectionDict in segmentDict.get('sections', []):
                    added, removed, changed = self._tpc.compareDictionaries(sectionDict.get('vtecRecord', {}), vtecRecord)
                    # Equal if nothing was added/removed/changed in the 2 dictionaries
                    if not added and not removed and not changed:
                        section = sectionDict
                        break

                # Can not make a headline without the corresponding
                # section to the vtecRecord.
                if not section:
                    hList.remove(vtecRecord)
                    continue

                # assemble the vtecRecord type
                hazStr = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

                # the section could have multiple hazards, need to combine 
                # the info for each hazard into one summaryheadline.
                # ImmediateCause will be the same for all.
                immediateCause = None
                hydrologicCause = None
                damOrLeveeNames = []
                streamNames = []
                replacesList = []
                replacedByList = []
                for hazard in section.get('hazardEvents'):
                    # get the immediateCause defaulting to 'ER' if None
                    immediateCause = hazard.get('immediateCause', 'ER')
                    hydrologicCause = hazard.get('hydrologicCause')
                    damOrLeveeName = hazard.get('damOrLeveeName', None)
                    if damOrLeveeName:
                        damOrLeveeNames.append(damOrLeveeName)
                    streamName = hazard.get('riverName', None)
                    if streamName:
                        streamNames.append(streamName)
                    replacedBy = hazard.get('replacedBy')
                    if replacedBy:
                        replacedByList.append(replacedBy)
                    replaces = hazard.get('replaces')
                    if replaces:
                        replacesList.append(replaces)

                #determine the actionWords
                if replacedByList:
                    # replaced added below
                    actionWords = 'is'
                else:
                    actionWords = self._tpc.actionControlWord(vtecRecord, self._issueTime)

                if immediateCause in ['DM']:
                    if hydrologicCause and hydrologicCause == 'siteImminent':
                        hazStr = hazStr + ' for the imminent failure of '
                    else:
                        hazStr = hazStr + ' for the failure of '

                    # Add the damOrLeveeNames - could be multiple
                    if len(damOrLeveeNames) > 0:
                        names = self._tpc.formatDelimitedList(damOrLeveeNames, delimiter=', ')
                        hazStr += names
                    else:
                        hazStr += 'the dam'

                    # Add the streamNames - could be multiple
                    if len(streamNames) > 0:
                        hazStr += ' on the '
                        names = self._tpc.formatDelimitedList(streamNames, delimiter=', ')
                        hazStr += names

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
                    ugcPhrase = self._tpc.getAreaPhrase(section.get('ugcs'))
                    hazStr += ' for ' + ugcPhrase

                if len(hazStr):
                    # Call user hook
                    localStr = self._tpc.hazard_hook(
                      None, None, vtecRecord['phen'], vtecRecord['sig'], vtecRecord['act'],
                      vtecRecord['startTime'], vtecRecord['endTime'])  # May need to add leading space if non-null 
                    if replacedByList:
                        headlineStr = '...' + hazStr + localStr
                    else:
                        headlineStr = '...' + hazStr + localStr + '...'

                # Add replaceStr
                if len(replacedByList) > 0:
                    replaceStr =  ' REPLACED BY '
                    names = self._tpc.formatDelimitedList(replacedByList, delimiter=', ')
                    replaceStr += names + '...'
                elif len(replacesList) > 0:
                    replaceStr =  '\n...REPLACES '
                    names = self._tpc.formatDelimitedList(replacesList, delimiter=', ')
                    replaceStr += names + '...'
                else:
                    replaceStr = ''
                headlineStr += replaceStr

                # always remove the main vtecRecord from the list
                hList.remove(vtecRecord)

        # All CAPS per Mixed Case Guidelines
        if headlineStr == None:
            headlineStr = ''
        headlineStr = headlineStr.upper()
        self._setVal('summaryHeadlines', headlineStr, segmentDict, 'Summary Headlines', required=False)
        return self._getFormattedText(headlineStr, endText='\n')

    def _endSegment(self, segmentDict):
        # Reset to empty dictionary
        self._segmentDict = {}
        return '\n$$\n\n' 

    ###################### Section Level

    def _setUp_section(self, sectionDict):
        self.attributionFirstBullet = AttributionFirstBulletText()
        self.attributionFirstBullet.initialize(
            sectionDict, self._productID, self._issueTime, self._testMode, self._wfoCity, self._tpc, self.timezones)
        return ''

    def _vtecRecords(self, sectionDict):
        vtecString = ''
        vtecRecords = sectionDict.get('vtecRecords')
        for vtecRecord in vtecRecords:
            vtecString += vtecRecord['vtecstr'] + '\n'
            if 'hvtecstr' in vtecRecord:
                if vtecRecord['hvtecstr'] != None:
                    vtecString += vtecRecord['hvtecstr'] + '\n'

        self._setVal('vtecRecords', vtecString, sectionDict, editable=False)
        return vtecString

    def _emergencyHeadline(self, sectionDict):
        # FFW_FFS will only have one hazard per section
        headline = ''
        hazard = sectionDict.get('hazardEvents')[0]
        includeChoices = hazard.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            # ALL CAPS per Mixed Case Text Guidelines
            emergencyLocation = self._tpc.getValueOrFramedText('includeEmergencyLocation', hazard, 'Enter Emergency Location').upper()
            headline = '...FLASH FLOOD EMERGENCY FOR ' + emergencyLocation + '...'
        return self._getFormattedText(headline, endText='\n\n')

    def _attribution(self, sectionDict):
        attribution = self.attributionFirstBullet.getAttributionText()
        return self._getFormattedText(attribution, endText='\n\n')

    def _attribution_point(self, sectionDict):
        attribution = self.attributionFirstBullet.getAttributionText()
        return self._getFormattedText(attribution, endText='\n\n')

    def _firstBullet(self, sectionDict):
        firstBullet = self.attributionFirstBullet.getFirstBulletText()

        startText = '* '
        if (self._runMode == 'Practice'):
            startText += "This is a test message.  "
        return self._getFormattedText(firstBullet, startText=startText, endText='\n\n')

    def _firstBullet_point(self, sectionDict):
        firstBullet = self.attributionFirstBullet.getFirstBulletText()

        startText = ''
        if sectionDict.get('vtecRecord').get('act') == 'NEW':
            startText = '* '
        return self._getFormattedText(firstBullet, startText=startText, endText='\n')

    def _timeBullet(self, sectionDict, roundMinutes=15):
        '''
        Displays the expiration time/date of the hazard, based on the following logic. 
        When hazard extends:
        - Into the next day . . . should include day
        - More than 1 week . . . should include date
        '''
        # Get the endTime from the first hazard
        hazard = sectionDict.get('hazardEvents', None)[0]
        bulletText = 'Until '
        endTime = hazard.get('endTime')
        expireTime = self._tpc.round(endTime, roundMinutes)

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
        bulletText += timeStr

        startText = '* '
        if (self._runMode == 'Practice' and hazard.get('geoType') != 'point'):
            startText += "This is a test message.  "
        return self._getFormattedText(bulletText, startText=startText, endText='\n')

    def _emergencyStatement(self, sectionDict):
        # FFW_FFS will only have one hazard per section
        statement = ''
        hazard = sectionDict.get('hazardEvents')[0]
        includeChoices = hazard.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            emergencyLocation = self._tpc.getValueOrFramedText('includeEmergencyLocation', hazard, 'Enter Emergency Location').upper()
            statement = '  This is a Flash Flood Emergency for ' + emergencyLocation + '.'
        return self._getFormattedText(statement, endText='\n\n')

    def _impactsBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getVal('impactsBullet', sectionDict)
        if bulletText is None:
            bulletText = ''
            impacts = []
            for hazard in sectionDict.get('hazardEvents'):
                hazardImpacts = hazard.get('impacts')
                if hazardImpacts:
                    impacts.append(hazardImpacts)
            if len(impacts) > 0:
                bulletText += '\n'.join(impacts)
        self._setVal('impactsBullet', bulletText, sectionDict, 'Impacts Bullet', required=False)
        startText = '* '
        if (self._runMode == 'Practice'):
            startText += "This is a test message.  "
        return self._getFormattedText(bulletText, startText=startText, endText='\n\n')

    def _basisAndImpactsStatement(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getVal('basisAndImpactsStatement', sectionDict)
        if bulletText is None:
            bulletText = ''
            statements = []
            hazardEventDicts = sectionDict.get('hazardEvents', [])
            rainSoFarText = self.rainSoFar(hazardEventDicts)
            for hazardEventDict in hazardEventDicts:
                statement = hazardEventDict.get('basisAndImpactsStatement', None)
                # Check for a empty string from the HID
                if statement:
                    statements.append(statement)
            if len(statements) > 0:
                bulletText += '\n'.join(statements)
                bulletText += rainFallText
            else:
                if rainSoFarText and rainSoFarText != '':
                    bulletText += rainSoFarText
                else:
                    bulletText += '|* Current hydrometeorological situation and expected impacts *|'

        self._setVal('basisAndImpactsStatement', bulletText, sectionDict, 'Basis and Impacts Bullet')
        startText = '* '
        if (self._runMode == 'Practice'):
            startText += "This is a test message.  "
        return self._getFormattedText(bulletText, startText=startText, endText='\n\n')

    def _locationsAffected(self, sectionDict):
        locationsAffected = ''
        vtecRecord = sectionDict.get('vtecRecord', {})
        action = vtecRecord.get('act', None)
        # FA.W, FA.Y, and FF.W will only have one hazard per section
        hazardEventDict = sectionDict.get('hazardEvents')[0]

        # This is a optional bullet check to see if it should be included
        listOfLocationsAffected = hazardEventDict.get("locationsAffectedCheckBox", [])
        if "selectLocationsAffected" in listOfLocationsAffected:

            # Get saved value from productText table if available
            locationsAffected = self._getVal('locationsAffected', sectionDict)
            if locationsAffected is None:
                locationsAffected = ''
                immediateCause = hazardEventDict.get('immediateCause', None)
                if immediateCause == 'DM' or immediateCause == 'DR':
                    damOrLeveeName = hazardEventDict.get('damOrLeveeName')
                    if damOrLeveeName:
                        damInfo = self._damInfo().get(damOrLeveeName)
                        if damInfo:
                            # Scenario
                            scenario = hazardEventDict.get('scenario')
                            if scenario:
                                scenarios = damInfo.get('scenarios')
                                if scenarios:
                                    scenarioText = scenarios.get(scenario)
                                    if scenarioText:
                                        locationsAffected += scenarioText + '\n\n'
                            # Rule of Thumb
                            ruleOfThumb = damInfo.get('ruleofthumb')
                            if ruleOfThumb:
                                locationsAffected += ruleOfThumb + '\n\n'

                if not locationsAffected:
                    phen = vtecRecord.get("phen")
                    sig = vtecRecord.get("sig")
                    geoType = hazardEventDict.get('geoType')
                    if phen == "FF" :
                        locationsAffected = "Some locations that will experience flash flooding include..."
                    elif phen == "FA" or phen == "FL" :
                        locationsAffected = "Some locations that will experience flooding include..."
                    else :
                        locationsAffected = "Locations impacted include..."
                    locationsAffected += self.createLocationsAffected(hazardEventDict)

        self._setVal('locationsAffected', locationsAffected, sectionDict, 'Locations Affected', required=False)

        startText = ''
        if action in ['NEW', 'EXT']:
            startText += '* '
        if (self._runMode == 'Practice'):
            startText += "This is a test message.  "
        return self._getFormattedText(locationsAffected.rstrip(),startText=startText, endText='\n\n')

    def _additionalComments(self, sectionDict):
        # Get saved value from productText table if available
        additionalComments = self._getVal('additionalComments', sectionDict)
        if additionalComments is None:
            # FA.W, FA.Y, and FF.W will only have one hazard per section
            hazard = sectionDict.get('hazardEvents')[0]
            additionalComments = self.createAdditionalComments(hazard)
        self._setVal('additionalComments', additionalComments, sectionDict, 'Additional Comments', required=False)
        return self._getFormattedText(additionalComments, startText='', endText='\n\n')

    def _endingSynopsis(self, dictionary):
        # Get saved value from productText table if available
        text = self._getVal('endingSynopsis', dictionary)
        if text is None:
            text = ''
            endingSynopsisList = []
            if dictionary.get('sections', None):
                # Segment Level
                for section in dictionary.get('sections', []):
                    for hazard in section.get('hazardEvents', []):
                        endingSynopsis = hazard.get('endingSynopsis')
                        if endingSynopsis:
                            endingSynopsisList.append(hazard.get('endingSynopsis'))
            else:
                # Section Level
                for hazard in dictionary.get('hazardEvents', []):
                    endingSynopsis = hazard.get('endingSynopsis')
                    if endingSynopsis:
                        endingSynopsisList.append(endingSynopsis)

            if len(endingSynopsisList) > 0:
                text = '\n'.join(endingSynopsisList)
            else:
                # Try to get from dialogInputMap  (case of partial cancellation)
                text = self._tpc.getVal(self.productDict, 'endingSynopsis', None)
                if text is None:
                    # If still none use framed text
                    text = '|* Brief post-synopsis of hydrometeorological activity *|'

        self._setVal('endingSynopsis', text, dictionary, 'Ending Synopsis')
        return self._getFormattedText(text, endText='\n\n')

    ###################### Utility methods

    def rainSoFar(self, hazardEventDicts):
        result = ""
        lowerBound = None
        upperBound = None
        for hazardEventDict in hazardEventDicts:
            rainAmt = hazardEventDict.get("rainAmt");
            if rainAmt == "rainKnown":
                hazardLowerBound = hazardEventDict.get("rainSoFarLowerBound")
                hazardUpperBound = hazardEventDict.get("rainSoFarUpperBound")
                if hazardLowerBound:
                    if not lowerBound or hazardLowerBound < lowerBound:
                        lowerBound = hazardLowerBound
                if hazardUpperBound:
                    if not upperBound or hazardUpperBound > upperBound:
                        upperBound = hazardUpperBound
        # Must have a upperbound
        if not upperBound:
            return result
        else:
            rainText = " inches of rain have fallen. "
            upperBound = "{:2.1f}".format(upperBound)
            if lowerBound:
                lowerBound = "{:2.1f}".format(lowerBound)
            if lowerBound == upperBound or lowerBound == None:
                result = "Up to " + upperBound + rainText
            else:
                result = " Between " + lowerBound + " and " + upperBound + rainText
        return result

    def overviewSynopsis(self, productDict):
        # Get saved value from productText table if available
        productLabel = productDict.get('productLabel')
        synopsisKey = 'overviewSynopsis_' + productLabel
        synopsis = self._getVal(synopsisKey, productDict)

        if synopsis is None:
            synopsis = productDict.get(synopsisKey, '')
        self._setVal('overviewSynopsis_' + productLabel, synopsis, productDict, 'Overview Synopsis', required=False)
        return self._getFormattedText(synopsis, endText='\n\n')

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

    def createLocationsAffected(self, hazardDict):
        nullReturn = " mainly rural areas of the aforementioned areas."
        locations = hazardDict.get('locationsAffected', [])
        if locations:
            return self._tpc.formatDelimitedList(locations) + '.'
        else:
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
        replacement = False

        # There could be multiple points sharing a VTEC code e.g. NEW
        for segment in segments:
            ugcs = segment.get('ugcs')

            pointAreaGroups = self._tpc.getGeneralAreaList(ugcs)
            areaGroups += pointAreaGroups
            

            nameDescription, nameTypePhrase = self._tpc.getNameDescription(pointAreaGroups)
            affected = nameDescription + ' '+ nameTypePhrase
            # Note there should only be one section with one hazard to process
            for section in segment.get('sections', []):
                for hazard in section.get('hazardEvents', []):
                    riverName = hazard.get('riverName_GroupName')
                    proximity = hazard.get('proximity')
                    replacement = hazard.get('replacedBy', False)
                    if proximity is None:
                        proximity = 'near'
                    riverPointName = hazard.get('riverPointName')
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
                if replacement:
                    actionWord = 'replacing'
                else:
                    actionWord = 'canceling'
                overview = nwsPhrase + ' is ' + actionWord + ' the ' + hazName + ' for '+ riverPhrase
    
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

    def createAdditionalComments(self, hazardDict):
        text = ''
        additionalComments =  self._tpc.getVal(hazardDict, 'additionalComments', [])
        for comment in additionalComments:
            if comment and comment != '':
                text += comment + '\n\n'
        return text.rstrip()

    def _getFormattedTime(self, time_ms, format=None, stripLeading=True, emptyValue=None, timeZones=['GMT']):
        if not time_ms:
            return emptyValue
        if format is None:
            format = '%I%M %p %A %Z '
        return self._tpc.getFormattedTime(
                time_ms, format, stripLeading=stripLeading, timeZones=timeZones)

    def _setVal(self, key, value, dictionary, label=None, editable=True, displayable=True, required=True,
                displayLabel=True):
        '''
        Helper method to call _setVal() in TextProductCommon. This method automatically
        sets the productCategory=self._productCategory, productID='', and editable=True
        parameters for you.
        '''
        eventIDs, ugcList = self._tpc.parameterSetupForKeyInfo(dictionary)
        self._tpc.setVal(self._editableParts, key, value, editable=editable, eventIDs=eventIDs,
                         segment=ugcList, label=label, displayable=displayable, required=required,
                         displayLabel=displayLabel, productCategory=self._productCategory, productID='')

    def _getVal(self, key, dictionary):
        '''
        Helper method that will either get the value of the product part from the productTextTable or
        self._editableParts if it was passed into the formatter.
        
        Could return a string, a empty string, or None.
        '''
        eventIDs, ugcList = self._tpc.parameterSetupForKeyInfo(dictionary)
        if self._useProductTextTable:
            return self._getSavedVal(key, eventIDs, ugcList)
        else:
            userEditedKey = KeyInfo(key, self._productCategory, '', eventIDs, ugcList, True)
            return self._editableParts.get(userEditedKey)

    def _getSavedVal(self, key, eventIDs, ugcList):
        '''
        Helper method to call _getSavedVal() in TextProductCommon. This method automatically
        sets the productCategory=self._productCategory, productID='' parameters for you.
        '''
        return self._tpc.getSavedVal(key, eventIDs=eventIDs, segment=ugcList, 
                                     productCategory=self._productCategory, productID='')

    def _getFormattedText(self, text, startText='', endText=''):
        '''
        Utility method to add beginning and ending text to a string only
        if the string exists. Otherwise it returns a empty string.
        
        @param text: text for a specific product part
        @param startText: text that should come before the product part text
        @param endText: text that should follow the product part text
        '''
        if text:
            return startText + text + endText
        return ''

    def _damInfo(self):
        from MapsDatabaseAccessor import MapsDatabaseAccessor
        mapsAccessor = MapsDatabaseAccessor()
        damInfoDict = mapsAccessor.getAllDamInundationMetadata()
        return damInfoDict

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

