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
    Oct 06, 2015   11832    Robert.Blum Fixed fieldType of certain checkboxes on the HID to be singular.
    Oct 19, 2015   11846    Robert.Blum Added checkbox to HID to determine whether or not to use the
                                        User edited saved text.
    Nov 09, 2015    7532    Robert.Blum Changes for multiple sections per segment.
    Dec 02, 2015   13311    Robert.Blum Per the directives && are only added if CTAs are in included,
                                        this fixes a validation issue with WarnGen Products.
    Dec 08, 2015   12998    Robert.Blum Removed bullet from basisAndImpacts product part when it is not
                                        a NEW or EXT.
    Dec 10, 2015   12723    Robert.Blum CityList is no longer optional for FF.A and FA.A. It has
                                        been completely removed for WarnGen products.
    Dec 17, 2015   14037    Robert.Blum Fixed CTA header to be all caps.
    Dec 17, 2015   14034    Robert.Blum Fixed indentation on LAT...LON string.
    Dec 18, 2015   14035    Robert.Blum Added newline to cityList in the 4th bullet.
    Dec 18, 2015   14036    Robert.Blum 4th bullet cityList is now delimited by commas.
    Jan 28, 2016   13012    Robert.Blum Changes to go along with the new EndingOption megawidgets.
    Feb 03, 2016   14983    Kevin.Bisanz Added optional parameter to _timeBullet(...) to include time
                                         description.
    Mar 21, 2016   15640    Robert.Blum  Fixed custom edits not getting put in final product.
    Mar 22, 2016   15645    Robert.Blum  Added logic to correctly produce RiverPro's timeBullet.
    Mar 31, 2016    8837    Robert.Blum  Added "Issued by" line in the product header for Service backup.
    Apr 18, 2016   16038    Robert.Blum  RiverPro timeBullet fix for endTimes.
    Apr 27, 2016   15012    Roger.Ferrel overviewSynopsis add leading period to the return value.
    May 06, 2016   18202    Robert.Blum  Changes for operational mode.
    May 11, 2016   16914    Robert.Blum  Removed rainfall statement from FL.A hazards.
    May 13, 2016   16913   Ben.Phillippe Fixed minor error with geometry
    May 17, 2016   16046    Kevin.Bisanz Referenced replacing event in overview if event is replaced.
    May 19, 2016   16545    Robert.Blum  Added overview for when flood category changes and also
                                         fixed duplicate states issue.
    May 23, 2016   19077    Robert.Blum  Added state abreviations to areaList for WarnGen hazard types.
    May 27, 2016   19080    dgilling     Change how AttributionFirstBulletText is called.
    Jun 07, 2016   19135    dgilling     Add missing test message disclaimers.
    Jun 15, 2016   14069    dgilling     Ensure test message is in all caps for each bullet.
    Jun 21, 2016    9620    Robert.Blum  Added comments to clarify local time.
    Jun 22, 2016   19928    Thomas.Gurney Fix capitalization of hazard name for replacement productss
    Jun 23, 2016   19537    Chris.Golden Changed to use UTC when converting epoch time to datetime.
    Jun 23, 2016   16045    Kevin.Bisanz Removed unneeded trailing space from
                                         _getFormattedTime(..) output.
    Jun 23, 2016   18215    Robert.Blum  Fixed mixed case issues with MND Header.
    Jun 24, 2016   19929   Thomas.Gurney Remove flood severity change statement from NEW products
    Jun 27, 2016   18232    Robert.Blum  Emergency Headline and Statement are now editable product parts.
    Jun 29, 2016   18227    Robert.Blum  Corrected Flash Flood Emergency Text to match WarnGen.
    Jun 29, 2016   18209    Robert.Blum  Fixed retrieving user edits on followups where the pil changes.
    Jul 13, 2016   18257    Kevin.Bisanz If action==COR, use prevAct instead for locationsAffected.
    Jul 13, 2016   18269    Roger.Ferrel Verification of location now done in HID.
                                         Headline and Statement no longer need to be editable.
    Jul 18, 2016   18269    Kevin.Bisanz Fix typo calling getAction instead of _getAction
    Jul 22, 2016   19214    Kevin.Bisanz Add locationsAffectedFallBack if spatial
                                         query for locationsAffected returns nothing.
    Jul 25, 2016   20381    dgilling     Correct formatting of locations in headline for FL.W.
    Jul 29. 2016   19473    Roger.Ferrel Include test messages when in Test mode.
    Aug 10, 2016   20619    Roger.Ferrel Modified lookup for synopsis in overviewSynopsis.
    Aug 08, 2016   21056    Robert.Blum  Implemented pathcast.
    Aug 11, 2016   20655    Robert.Blum  Fixed lookup for Product Level CTAs.
    Aug 12, 2016   20654    Kevin.Bisanz Add _locationsAffectedFallBack(..)
    Aug 22, 2016   21056    Robert.Blum  Update to pathcast for creating the fall back locations.
    Aug 22, 2016   20381    Robert.Blum  Fixed RiverPro Headlines.
    Aug 23, 2016   21473    Robert.Blum  Fixed Flash Flood Emergency text.
    Aug 24, 2016   21443   Ben.Phillippe Fixed next issuance wording
    Aug 25, 2016   21458    Robert.Blum  _additionalInfoStatement now uses framed text.
    Aug 29, 2016   21444   Ben.Phillippe Time zone now before day of week in formatted time
    Sep 06, 2016   19202    Sara.Stewart added advisoryUpgraded/endingOption checks
    Sep 20, 2016   21609    Kevin.Bisanz FF products now use locationsAffectedFallBack
                                         text in summary headline.
    Sep 21, 2016   21462    Robert.Blum  Added logic for new Dam Info radio button choice.
    Sep 28, 2016   21624    Roger.Ferrel Summary message on dam failure now states "potential failure".
    Oct 18, 2016   22489    Robert.Blum  Fixed cityList product part to use comma's instead of "...". 
    Oct 21, 2016   21565    Mark.Fegan   orrected wording for replaced products.
    Nov 07, 2016    22119   Kevin.Bisanz  Remove unused ProductTextUtil import.
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
from BasisText import BasisText
import ForecasterInitials
import PathcastText
import TimeUtils

from abc import *

class Format(FormatTemplate.Formatter):

    def __init__(self):
        self.bridge = Bridge()
        self.basisText = BasisText()
        areaDict = self.bridge.getAreaDictionary()
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)

    def initialize(self, editableEntries=None) :

        # To ensure time calls are based on Zulu
        os.environ['TZ'] = "GMT0"
        self.timezones = []

        # Dictionary that will hold the KeyInfo entries of the
        # product part text strings to be displayed in the Product
        # Editor. 
        if editableEntries:
            # If this dictionary is passed in, it means
            # product generation is being triggered by a
            # change on the product editor. In this case
            # use those values instead of those from the 
            # Product Text table.
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
        self._testMode = self._runMode in ['Practice', 'Test']
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
    def execute(self, productDict, editableEntries, overrideProductText):
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
        if self._testMode:
            text = 'TEST...' + text + '...TEST \n'
        else:
            text += '\n'

        text += 'National Weather Service ' + self._wfoCityState + '\n'
        if self._backupWfoCityState and self._backupWfoCityState != self._wfoCityState:
            text += 'Issued by National Weather Service ' + self._backupWfoCityState + '\n'

        text += self.getIssuanceTimeDate(productDict)
        if self._testMode and self._productID in ['FFW', 'FLW', 'FLS']:
            text += '\n...THIS MESSAGE IS FOR TEST PURPOSES ONLY...\n'
        return text

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
                action = self._getAction(vtecRecord)
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
            callsToAction =  self._tpc.getVal(productDict, ctaKey, None)
            if callsToAction:
                text = ""
                for cta in callsToAction:
                    cta = cta.strip('\n\t\r')
                    cta = re.sub('\s+',' ',cta)
                    text += cta + '\n\n'
                text = text.rstrip()
        if text is None:
            # Try the previous product label to account for PIL changing
            prevProductLabel = productDict.get('previousProductLabel', None)
            if prevProductLabel:
                key = 'callsToAction_productLevel_' + prevProductLabel
                text = self._getVal(key, productDict)
            else:
                text = ""
        self._setVal(ctaKey, text, productDict, 'Calls To Action - Product Level', required=False)
        return self._getFormattedText(text, startText='PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n', endText='\n\n')

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
                ### "The next statement will be issued Tuesday morning at 600 AM CST. 500 AM MST."
                nextIssue += ' ' + fmtIssueTme + '.'
            
            partText = 'The next statement will be issued ' + nextIssue
        self._setVal('nextIssuanceStatement', partText, productDict, 'Next Issuance Statement')
        return self._getFormattedText(partText, endText='\n\n') + '&&\n\n'

    def _additionalInfoStatement(self, productDict):
        # Get saved value from productText table if available
        text = self._getVal('additionalInfoStatement', productDict)
        if text is None:
            # Please override this method for your site
            text = 'Additional information is available at www.weather.gov.'
        self._setVal('additionalInfoStatement', text, productDict, 'Additional Info Statement')
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
        sections = segmentDict.get('sections')
        hazardEvent = sections[0].get('hazardEvents')[0]
        if self._tpc.isWarnGenHazard(hazardEvent):
            areaList = self._tpc.formatUGC_namesWithStateAbrev(segmentDict.get('ugcs'))
        else:
            areaList =  self._tpc.formatUGC_names(segmentDict.get('ugcs'))
        areaList = self._tpc.linebreak(areaList, 69, breakStr=[' ', '-'] ).rstrip()
        return areaList + '\n'

    def _cityList(self, segmentDict):
        # Get saved value from productText table if available
        cityListText = self._getVal('cityList', segmentDict)
        if cityListText is None:
            cityListText = ''
            cityList = set()
            for sectionDict in segmentDict.get('sections', []):
                for hazard in sectionDict.get('hazardEvents', []):
                    cityList.update(hazard.get('cityList', []))
            if cityList:
                cityListText = 'Including the cities of '
                cityListText += self._tpc.punctuateList(list(cityList))
        self._setVal('cityList', cityListText, segmentDict, 'City List', required=False)
        return self._getFormattedText(cityListText, endText='\n')

    def _vtecRecords(self, segmentDict):
        vtecString = ''
        vtecRecords = segmentDict.get('vtecRecords')
        vtecRecords.sort(self._tpc.vtecRecordSortAlg)
        for vtecRecord in vtecRecords:
            vtecString += vtecRecord['vtecstr'] + '\n'
            if 'hvtecstr' in vtecRecord:
                if vtecRecord['hvtecstr'] != None:
                    vtecString += vtecRecord['hvtecstr'] + '\n'

        self._setVal('vtecRecords', vtecString, segmentDict, editable=False)
        return vtecString

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
                            if geo.geom_type == HazardConstants.SHAPELY_POINT:
                                continue
                            else:
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
                polyStr += '\n     ' 
                pointsOnLine = 0
            polyStr += ' ' + str(lat) + ' ' + str(lon)
            pointsOnLine += 1

        if self._testMode:
            hazard = section.get('hazardEvents', [])[0]
            if self._tpc.isRiverProHazard(hazard):
                polyStr += '\n\nTHIS IS A TEST MESSAGE. DO NOT TAKE ACTION BASED ON THIS.'
            elif self._tpc.isWarnGenHazard(hazard):
                polyStr = 'THIS IS A TEST MESSAGE. DO NOT TAKE ACTION BASED ON THIS MESSAGE.\n\n' + polyStr
        return polyStr + '\n'

    def _issuanceTimeDate(self, segmentDict):
        text = self.getIssuanceTimeDate(segmentDict)
        vtecRecords = segmentDict.get('vtecRecords', [])
        phensig = vtecRecords[0].get('key', '')
        if self._testMode and self._productID in ['FFS', 'FFA', 'FLS', 'FLW']:
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
            headlineStr = ''
            vtecRecords = segmentDict.get('vtecRecords', None)
            hList = copy.deepcopy(vtecRecords)
            if len(hList):
                hList.sort(self._tpc.regularSortHazardAlg)

            while len(hList) > 0:
                vtecRecord = hList[0]
                headline = ''

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
                actionWords = ''
                # Note: replaced is handled below
                if not replacedByList:
                    actionWords = self._tpc.actionControlWord(vtecRecord, self._issueTime)

                if immediateCause in ['DM']:
                    hazStr += ' for the '
                    if vtecRecord['sig'] and vtecRecord['sig'] == 'A':
                        hazStr += 'potential '
                    if hydrologicCause and hydrologicCause == 'siteImminent':
                        hazStr += 'imminent ' 
                    hazStr += 'failure of '

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
                    # Use same text in the summary headline as the locations
                    # affected text when the hazard is not near any city.
                    locationDicts = hazard.get('locationDicts')
                    ugcPhrase = self._locationsAffectedFallBack(locationDicts)
                    hazStr += ' for ' + ugcPhrase

                if len(hazStr):
                    # Call user hook
                    localStr = self._tpc.hazard_hook(
                      None, None, vtecRecord['phen'], vtecRecord['sig'], vtecRecord['act'],
                      vtecRecord['startTime'], vtecRecord['endTime'])  # May need to add leading space if non-null 
                    if replacedByList:
                        headline = '...' + hazStr + localStr
                    else:
                        headline = '...' + hazStr + localStr + '...'

                # Add replaceStr
                if len(replacedByList) > 0:
                    names = self._tpc.formatDelimitedList(replacedByList, delimiter=', ')
                    article = 'A'
                    if re.match('[AaEeIiOoUu]', names):
                        article = 'AN'
                    replaceStr = ' HAS BEEN REPLACED WITH {} '.format(article)
                    replaceStr += names + '...'
                elif len(replacesList) > 0:
                    replaceStr =  '\n...REPLACES '
                    names = self._tpc.formatDelimitedList(replacesList, delimiter=', ')
                    replaceStr += names + '...'
                else:
                    replaceStr = ''
                headline += replaceStr

                # always remove the main vtecRecord from the list
                hList.remove(vtecRecord)

                # Could have multiple headlines per segment
                if headlineStr:
                    headlineStr += '\n'
                # collapse any multiple spaces
                headlineStr += re.sub(' +',' ',headline)
            
        # All CAPS per Mixed Case Guidelines
        headlineStr = headlineStr.upper()
        self._setVal('summaryHeadlines', headlineStr, segmentDict, 'Summary Headlines', required=False)
        return self._getFormattedText(headlineStr, endText='\n')

    def _endSegment(self, segmentDict):
        # Reset to empty dictionary
        self._segmentDict = {}
        return '\n$$\n\n' 

    ###################### Section Level

    def _setUp_section(self, sectionDict):
        return ''

    def _endSection(self, sectionDict):
        return ''

    def _emergencyHeadline(self, dictionary):
        # Product part is both section and segment level
        if 'sections' in dictionary:
            # FFW_FFS will only have one hazard per section
            sectionDict = dictionary.get('sections')[0]
        else:
            sectionDict = dictionary

        # Get saved value from productText table if available
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
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self._getFormattedText(firstBullet, startText=startText, endText='\n\n')

    def _firstBullet_point(self, sectionDict):
        firstBullet = self.attributionFirstBullet.getFirstBulletText()

        startText = ''
        if sectionDict.get('vtecRecord').get('act') == 'NEW':
            startText = '* '
        return self._getFormattedText(firstBullet, startText=startText, endText='\n')

    def _timeBullet(self, sectionDict, roundMinutes=15, includeTimingDesc=False):
        '''
        Displays the start and/or end time/date of the hazard. There is specific logic for
        RiverPro hazards found in getRiverProTimeBullet otherwise the following logic is 
        used. 
        When hazard extends:
        - Into the next day . . . should include day
        - More than 1 week . . . should include date
        '''
        # Get the endTime from the first hazard
        hazard = sectionDict.get('hazardEvents', None)[0]
        vtecRecord = sectionDict.get('vtecRecord')

        if self._tpc.isRiverProHazard(hazard):
            bulletText = self.getRiverProTimeBullet(hazard, vtecRecord)
        else:
            bulletText = 'Until '
            endTime = hazard.get('endTime')

            # Determine how far into the future the end time is.
            issueTime = datetime.datetime.fromtimestamp(float(self._issueTime)/1000) # Local time
            tdelta = endTime - issueTime

            if (tdelta.days == 6 and endTime.date().weekday() == issueTime.date().weekday()) or \
                tdelta.days > 6:
                format = '%l%M %p %Z %a %b %d'
            else:
                format = '%l%M %p %Z %a'

            timeStr = ''
            for tz in self.timezones:
                if len(timeStr) > 0:
                    timeStr += '/'
                timeStr += self._tpc.formatDatetime(endTime, format, tz).strip()

                if includeTimingDesc == True:
                    endTime = vtecRecord.get('endTime')
                    timePhrase = self._timeBulletTimeDesc(self._issueTime, endTime, tz)
                    if len(timePhrase) > 0:
                        timeStr += ' ' + timePhrase

            bulletText += timeStr

        startText = '* '
        if self._testMode and (hazard.get('geoType') != 'point'):
            startText += "THIS IS A TEST MESSAGE. "
        return self._getFormattedText(bulletText, startText=startText, endText='\n')

    def _timeBulletTimeDesc(self, issueTime, endTime, tz):
        '''
        Returns the time description (e.g. 'this afternoon') to be used for
        the time bullet.
        '''
        timeDesc = ''
        (hour, hourTZ, desc) = self._tpc.timingWordTableEXPLICIT(issueTime, endTime, tz, 'endTime')
        if desc != None:
            timeDesc = desc

        return timeDesc

    def _emergencyStatement(self, sectionDict):
        # FFW_FFS will only have one hazard per section
        statement = ''
        hazard = sectionDict.get('hazardEvents')[0]
        includeChoices = hazard.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            emergencyLocation = self._tpc.getValueOrFramedText('includeEmergencyLocation', hazard, 'Enter Emergency Location')
            vtecRecord = sectionDict.get('vtecRecord',{})
            act = self._getAction(vtecRecord)
            if act in ['NEW', 'EXT']:
                statement += '  '
            statement += 'This is a FLASH FLOOD EMERGENCY for ' + emergencyLocation + '. This is a PARTICULARLY DANGEROUS SITUATION. SEEK HIGHER GROUND NOW!'
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
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
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
        startText = ''
        act = sectionDict.get('vtecRecord',{}).get('act')
        if act in ['NEW', 'EXT']:
            startText = '* '
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self._getFormattedText(bulletText, startText=startText, endText='\n\n')

    def _locationsAffected(self, sectionDict):
        locationsAffected = ''
        vtecRecord = sectionDict.get('vtecRecord', {})
        action = self._getAction(vtecRecord)
        # FA.W, FA.Y, and FF.W will only have one hazard per section
        hazardEventDict = sectionDict.get('hazardEvents')[0]

        # Get saved value from productText table if available
        locationsAffected = self._getVal('locationsAffected', sectionDict)
        if locationsAffected is None:
            locationsAffected = ''

            # This is a optional bullet check to see if it should be included
            locationsAffectedChoice = hazardEventDict.get("locationsAffectedRadioButton", None)
            if locationsAffectedChoice == "damInfo":
                damOrLeveeName = hazardEventDict.get('damOrLeveeName')
                if damOrLeveeName:
                    damInfo = self._damInfo().get(damOrLeveeName)
                    if damInfo:
                        # Scenario
                        scenarioText = None
                        scenario = hazardEventDict.get('scenario')
                        if scenario:
                            scenarios = damInfo.get('scenarios')
                            if scenarios:
                                scenarioText = scenarios.get(scenario)
                        if not scenarioText:
                            scenarioText = "|* Enter Scenario Text *|" 
                        locationsAffected += scenarioText + '\n\n'
                        # Rule of Thumb
                        ruleOfThumb = damInfo.get('ruleofthumb')
                        if ruleOfThumb:
                            locationsAffected += ruleOfThumb + '\n\n'
                        else:
                            locationsAffected += "|* Enter Rule of Thumb *|\n\n"
                    else:
                        locationsAffected += "|* Enter Scenario and Rule of Thumb Text *|\n\n"
            elif locationsAffectedChoice == "cityList":
                phen = vtecRecord.get("phen")
                sig = vtecRecord.get("sig")
                geoType = hazardEventDict.get('geoType')
                if phen == "FF" :
                    locationsAffected = "Some locations that will experience flash flooding include..."
                elif phen == "FA" or phen == "FL" :
                    locationsAffected = "Some locations that will experience flooding include..."
                else :
                    locationsAffected = "Locations impacted include..."
                locationsAffected += '\n  ' + self.createLocationsAffected(hazardEventDict)
            elif locationsAffectedChoice == "pathcast":
                locationDicts = hazardEventDict.get('locationDicts', None)
                if locationDicts:
                    locationsFallBack = self._locationsAffectedFallBack(locationDicts)
                else:
                    locationsFallBack = 'the aforementioned areas'
                pathcastText = PathcastText.PathcastText(sectionDict, self._testMode, self._getAction(vtecRecord), self.timezones, locationsFallBack)
                locationsAffected += pathcastText.getPathcastText()

        self._setVal('locationsAffected', locationsAffected, sectionDict, 'Locations Affected', required=False)

        startText = ''
        if action in ['NEW', 'EXT']:
            startText += '* '
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self._getFormattedText(locationsAffected.rstrip(),startText=startText, endText='\n\n')

    def _locationsAffectedFallBack(self, locationDicts):
        '''
        @param locationDicts List of dictionaries with information about each UGC
        @return: locationsStr text to be used if the spatial query returns
                nothing.  For example:
                "West Central Gage, Northeastern Jefferson and Southeastern Saline Counties"

        Note:  Influenced by AttributionFirstBulletText.getAreaPhrase()
        '''
        typeToLocations = {}

        # get duplicate UGC data from DuplicateUGCs.py
        duplicateUGCs = self.bridge.getDuplicateUGCs()

        useStates = False   # Should the state be included in the text?

        # For each UGC, build a string like:
        # Southeastern Montgomery
        # Eventually the type (e.g. "counties") will be appended to the list.
        for entry in locationDicts:
            ugc = entry.get('ugc')
            ugcType = entry.get('typeSingular')
            independentCityFlag = ugcType.startswith('independent city')

            # If this UGC is in the list of UGCs with duplicate names in the
            # same WFO, the state name should be included.
            useStates |= (ugc in duplicateUGCs)

            pieces = []

            # Independent city lines start with "The"
            if independentCityFlag:
                pieces.append('The')

            # Add the portion(s) of the UGC.  E.g. Southeastern
            portionsOfUgc = entry.get('ugcPortions', '')
            if portionsOfUgc:
                pieces.append(self._tpc.formatDelimitedList(portionsOfUgc, ', '))

            # Add the name.  E.g. Montgomery
            pieces.append(entry.get('entityName'))

            location = ' '.join(pieces)

            # Save the location, organized by type. Potentially the
            # ugcs could be of multiple types.  For example independent
            # cities and counties in the LWX CWA.  For each type, store the UGC
            # and the text string built above.
            if ugcType in typeToLocations:
                locationEntries, locations = typeToLocations.get(ugcType)
            else:
                locationEntries = []
                locations = []
            locationEntries.append(entry)
            locations.append(location)
            typeToLocations[ugcType] = (locationEntries, locations)

        # Now that all the text strings are built, combine them.
        types = typeToLocations.keys()
        locationsList = []
        for ugcType in types:
            locationEntries, locations = typeToLocations[ugcType]

            boundaries = [(0, len(locationEntries))]
            if useStates:
                # Get start/end index of locations with common state and part
                # of state info.
                boundaries = self._calcLocationBoundaries(locationEntries)

            for boundary in boundaries:
                (start, end) = boundary
                locationText = self._joinLocationsWithType(ugcType, locationEntries[start:end], locations[start:end]);
                if useStates:
                    partOfState = locationEntries[start].get('ugcPartsOfState')
                    state = locationEntries[start].get('fullStateName')
                    if partOfState or state:
                       locationText += ' in'
                       if partOfState:
                           locationText += ' {0}'.format(partOfState)
                       if state:
                           locationText += ' {0}'.format(state)
                locationsList.append(locationText)

        # Now join the county list with the zone list with the independent city list etc
        locationsStr = self._tpc.formatDelimitedList(locationsList, ', ')

        return locationsStr

    def _calcLocationBoundaries(self, locationEntries):
        '''
        Determines the boundary of each group of locations with a different
        state or part of state.  Each tuple has a start and end value such that
        locationEntries[start:end] is a group which has the same state and part
        of state information. For example: [(0, 7), (7, 8), (8, 14), (14, 19),
        (19, 26)].  The start index is inclusive while the end index is
        exclusive.  The values may be used to slice the locationEntries list.
        Each slice of locationEntries[start:end] contains elements with a
        common state and part of state.

        @param locationEntries List of dicts with info about each location
                (state, part of state, etc).  Assumption: The list is ordered
                by state and then part of state.
        @return List of tuples with start/end index values for each group which
                has the same state and part of state information.
        '''
        boundaries = []
        if len(locationEntries) > 0:
            prevPartsOfState = "NonExistentPartOfState"
            prevState = "NonExistentState"
            startSlice = -1
            endSlice = -1

            # Loop over each location entry and note when a new state or part
            # of state begins.
            for i in range(0, len(locationEntries)):
                curPartsOfState = locationEntries[i].get('ugcPartsOfState')
                curState = locationEntries[i].get('fullStateName')
                if(prevPartsOfState != curPartsOfState or prevState != curState):
                    if i != 0:
                        # Record start/end of previous slice.
                        boundaries.append((startSlice, endSlice))
                    startSlice = i
                endSlice = i+1
                prevPartsOfState = curPartsOfState
                prevState = curState
            # Record the last slice explicitly because there is no following slice.
            boundaries.append((startSlice, endSlice))

        return boundaries

    def _joinLocationsWithType(self, ugcType, locationEntries, locations):
        '''
        Joins each string into a comma delimited list and appends the
        type of UGC.  For example:
        "West Central Gage, Northeastern Jefferson and Southeastern Saline Counties"
        @param ugcType The type of this UGC.  E.g. county
        @param locationEntries List of dicts with info about each location
                (state, part of state, etc)
        @param locations List of locations.  E.g. "Southeastern Saline"
        @return String with the provided locations joined together
        '''
        # Join the locations
        locationText = self._tpc.formatDelimitedList(locations, ', ')

        independentCityFlag = ugcType.startswith('independent city')
        if not independentCityFlag:
            typeSingularOrPlural = 'typeSingular'
            if len(locations) > 1:
                typeSingularOrPlural = 'typePlural'
            # All the UGCs should be the same ugcType, so use [0].
            ugcTypeStr = self._tpc.getInformationForUGC(locationEntries[0].get('ugc'), typeSingularOrPlural)
            if ugcTypeStr:
                # Add the type (e.g. county (or plural)) to the end.
                locationText += ' ' + ugcTypeStr

        return locationText

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
        phensigs = []
        if text is None:
            text = ''
            endingSynopsisList = []
            if dictionary.get('sections', None):
                # Segment Level
                for section in dictionary.get('sections', []):
                    for hazard in section.get('hazardEvents', []):
                        phensigs.append(hazard.get('phen') + '.' +hazard.get('sig'))
                        endingOption = hazard.get('endingOption')
                        if endingOption:
                            endingSynopsisList.append(self.getEndingSynopsisFromEndingOptions(hazard))
                        else:
                            endingSynopsis = hazard.get('endingSynopsis')
                            if endingSynopsis:
                                endingSynopsisList.append(hazard.get('endingSynopsis'))
            else:
                # Section Level
                for hazard in dictionary.get('hazardEvents', []):
                    phensigs.append(hazard.get('phen') + '.' +hazard.get('sig'))
                    endingOption = hazard.get('endingOption')
                    if endingOption:
                        endingSynopsisList.append(self.getEndingSynopsisFromEndingOptions(hazard))
                    else:
                        endingSynopsis = hazard.get('endingSynopsis')
                        if endingSynopsis:
                            endingSynopsisList.append(hazard.get('endingSynopsis'))

            if len(endingSynopsisList) > 0:
                text = '\n'.join(endingSynopsisList)
            else:
                # Try to get from dialogInputMap  (case of partial cancellation)
                text = self._tpc.getVal(self.productDict, 'endingSynopsis', None)
                if text is None:
                    for phensig in phensigs:
                        if phensig in ['FF.W', 'FA.W', 'FA.Y']:
                            # If still none use framed text
                            text = 'Flooding is no longer expected to pose a threat. Please continue to heed remaining road closures.'
                            break
                    if text is None:
                        text = '|* Brief post-synopsis of hydrometeorological activity *|'

        self._setVal('endingSynopsis', text, dictionary, 'Ending Synopsis')
        return self._getFormattedText(text, endText='\n\n')

    def _callsToAction_sectionLevel(self, sectionDict):
        # Get saved value from productText table if available
        callsToAction = self._getVal(HazardConstants.CALLS_TO_ACTION, sectionDict)
        if callsToAction is None:
            callsToAction =  self._tpc.getVal(sectionDict, HazardConstants.CALLS_TO_ACTION, '')
            if callsToAction and callsToAction != '':
                callsToAction = callsToAction.rstrip()
        self._setVal(HazardConstants.CALLS_TO_ACTION, callsToAction, sectionDict, 'Calls To Action', required=False)
        return self._getFormattedText(callsToAction, startText='PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n', endText='\n\n&&\n\n')

    ###################### Utility methods

    def getRiverProTimeBullet(self, hazardDict, vtecRecord):
        '''
            Mimics RiverPro's <EventTime> variable. It uses T_WWA format and considers some special
            conditions as well. It generally states as "from <EventStartTime> to <EventEndTime>", 
            The following lists some other special cases:
            a. If event ending time is missing, <EventTime> outputs: "from event start time until further notice"
            b. If event start time is missing, <EventTime> outputs: "until event ending time"
            c. If event start time and ending time are missing, <EventTime> outputs: "until further notice"
            d. If event becomes effective within 3 hours from the issuance time, <EventTime> outputs "until event ending time"
            e. If event start time and ending time have the same time phrase, <EventTime> outputs "during event ending time".
        '''
        text = ""

        # Get the times
        startTime = hazardDict.get('startTime')
        startTimeMillis = TimeUtils.datetimeToEpochTimeMillis(startTime)
        endTime = hazardDict.get('endTime')
        endTimeMillis = TimeUtils.datetimeToEpochTimeMillis(endTime)

        # Determine if the startTime is in the vtec string
        startTimePhrase = None
        vtecString = vtecRecord.get("vtecstr")
        split = vtecString.split(".")
        if len(split) >= 6:
            startTimeStr = split[6]
            if startTimeStr.startswith('000000T0000Z') == False:
                # convert to T_WWA format
                startTimePhrase = self._tpc.timingWordTableFUZZY4(self._issueTime, startTimeMillis, self.timezones[0])[2]

        # Check if endTime is UFN
        if self._tpc.untilFurtherNotice(endTimeMillis):
            endTimePhrase = None
        else:
            # convert to T_WWA format
            endTimePhrase = self._tpc.timingWordTableFUZZY4(self._issueTime, endTimeMillis,  self.timezones[0])[2]

        # Handle the special cases
        if startTimePhrase == endTimePhrase and endTimePhrase != None: # Case E
            text = "During " + endTimePhrase
        elif endTimePhrase:
            if startTimePhrase: 
                issueTime = datetime.datetime.utcfromtimestamp(float(self._issueTime)/1000)
                tdelta = startTime - issueTime
                if tdelta.days == 0 and tdelta.seconds <= 10800: # Case D
                    text = "Until " + endTimePhrase
                else: # General Case
                    text = "From " + startTimePhrase + " to " + endTimePhrase
            else: # Case B
                text = "Until " + endTimePhrase
        else:
            if startTimePhrase: # Case A
                text = "From " + startTimePhrase + " until further notice"
            else: # Case C
                text = "Until further notice"
        return text  + "."

    def getEndingSynopsisFromEndingOptions(self, hazard):                              
        endingOptionsList = []
        endingOptions = hazard.get('endingOption', None)  
        if endingOptions:
            for option in endingOptions:
                endingOptionsList.append(option)

        if len(endingOptionsList) > 0:
            text = ' '.join(endingOptionsList)
            subType = hazard.get('subType')
            # make sure the advisoryUpgraded checkbox is not checked
            # before saying flooding is no longer a threat
            if subType != 'NonConvective' and 'advisoryUpgraded' not in hazard.get('endingOption_identifiers'):
                text += ' Flooding is no longer expected to pose a threat. Please continue to heed remaining road closures.'
        else:
            text = 'Flooding is no longer expected to pose a threat. Please continue to heed remaining road closures.'
        return text

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
            synopsis = productDict.get(synopsisKey)
        if synopsis is None:
            # Try the previous product label to account for PIL changing
            prevProductLabel = productDict.get('previousProductLabel', None)
            if prevProductLabel:
                key = 'overviewSynopsis_' + prevProductLabel
                synopsis = self._getVal(key, productDict)
                if synopsis is None:
                    synopsis = productDict.get(key, '')
            else:
                synopsis = ''
        self._setVal(synopsisKey, synopsis, productDict, 'Overview Synopsis', required=False)
        return self._getFormattedText(synopsis.strip(), startText='.', endText='.\n\n')

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
        locations = hazardDict.get('locationsAffected', [])
        locationsAffected = ''
        if locations:
            locationsAffected = self._tpc.formatDelimitedList(locations, delimiter=', ') + '.'
        else:
            locationsAffected = 'mainly rural areas of '
            locationDicts = hazardDict.get('locationDicts', None)
            if locationDicts:
                locationsAffected += self._locationsAffectedFallBack(locationDicts)
            else:
                locationsAffected += 'the aforementioned areas'

        return locationsAffected

    def getIssuanceTimeDate(self, segmentDict):
        text = ''
        for tz in self.timezones:
            text = self._tpc.formatDatetime(self._issueTime, '%l%M %p %Z %a %b %e %Y', tz).strip()
            # only need the first time zone for issue time
            break
        text += '\n'

        return text

    def getOverviewHeadlinePhrase(self, segments, lineLength=69):
        '''
            ...THE NATIONAL WEATHER SERVICE IN DES MOINES HAS ISSUED A FLOOD
            ADVISORY FOR THE WINNEBAGO RIVER AT MASON CITY...SHELL ROCK RIVER
            AT MARBLE ROCK...CEDAR RIVER AT JANESVILLE...CEDAR RIVER AT CEDAR
            FALLS...AND CEDAR RIVER AT WATERLOO...
        '''
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity

        locationPhrases = []
        replacement = False
        riseInFcstCategory = False
        riseInObsCategory = False
        states = set()

        # There could be multiple points sharing a VTEC code e.g. NEW
        for segment in segments:
            ugcs = segment.get('ugcs')
            affectedAreas = set()
            for ugc in ugcs:
                states.add(self._tpc.getInformationForUGC(ugc, "fullStateName"))
                affectedAreas.add(self._tpc.getInformationForUGC(ugc))
            affected = self._tpc.formatDelimitedList(affectedAreas, delimiter=", ") + " "
            if len(affectedAreas) > 1:
                affected += "counties"
            else:
                affected += "county"
            # Note there should only be one section with one hazard to process
            for section in segment.get('sections', []):
                for hazard in section.get('hazardEvents', []):
                    if hazard.get('phen') in [ 'FL' ] and hazard.get('sig') in [ 'A', 'W' ]:
                        # the headline in river flood products should contain both the city and state
                        nwsPhrase = 'The National Weather Service in ' + self._wfoCityState
                    riverName = hazard.get('riverName_GroupName')
                    proximity = hazard.get('proximity')
                    replacement = hazard.get('replacedBy', False)
                    if proximity is None:
                        proximity = 'near'
                    riverPointName = hazard.get('riverPointName')
                    locationPhrases.append(riverName + ' ' + proximity + ' ' + riverPointName + ' affecting ' + affected + '.')
                    # Determine if there was a rise in flood category
                    prevObsCat = hazard.get('previousObservedCategory', None)
                    prevFcstCat = hazard.get('previousForecastCategory', None)
                    prevObsCatName = hazard.get('previousObservedCategoryName', "|* PREVIOUS OBSERVED CATEGORY *|")
                    prevFcstCatName = hazard.get('previousForecastCategoryName', "|* PREVIOUS FORECAST CATEGORY *|")
                    obsCat = hazard.get('observedCategory', None)
                    fcstCat = hazard.get('maxFcstCategory', None)
                    obsCatName = hazard.get("observedCategoryName")
                    fcstCatName = hazard.get("maxFcstCategoryName")
                    if prevFcstCat != None and fcstCat > 0 and fcstCat > prevFcstCat:
                        riseInFcstCategory = True
                    if prevObsCat != None and obsCat > 0 and obsCat > prevObsCat:
                        riseInObsCategory = True

        formattingFunc = lambda s: self._tpc.indentText(s, indentFirstString='  ', 
              indentNextString='  ', maxWidth=69,
              breakStrings=[' ', '-', '...'])
        locationPhrase = '\n\n'.join([formattingFunc(s) for s in locationPhrases])
        riverPhrase = ' for the following rivers in ' + self._tpc.formatDelimitedList(states, useAnd=False)

        # Use this to determine which first bullet format to use.
        vtecRecords = segment.get('vtecRecords')
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

            if len(hazName):
                action = self._getAction(vtecRecord)

            actionControlPhrase = self._tpc.actionControlPhrase(vtecRecord, self._issueTime, replacement=replacement)
            if action in ['CON', 'EXT']:
                if riseInObsCategory or riseInFcstCategory:
                    if riseInObsCategory:
                        overview = "Observed flooding changed from " + prevObsCatName + " to " + obsCatName
                    else:
                        overview = "Forecast flooding changed from " + prevFcstCatName + " to " + fcstCatName
                    overview += " severity and increased in duration" + riverPhrase
                else:
                    overview = nwsPhrase + actionControlPhrase + hazName + riverPhrase
            elif action == 'CAN':
                actionValue = ''
                if replacement:
                    actionWord = 'replacing'
                    if len(replacement) > 0:
                        actionValue = ' with a ' + replacement.title()
                overview = nwsPhrase + actionControlPhrase + hazName + actionValue + riverPhrase
            elif action == 'EXP':
                overview = 'The ' + hazName + riverPhrase + actionControlPhrase
                if vtecRecord.get('endTime') > self._issueTime:
                    timeWords = self._tpc.getTimingPhrase(vtecRecord, [], self._issueTime, timeZones=self.timezones)
                    overview += timeWords
            elif action in ['NEW', 'ROU']:
                overview = nwsPhrase + actionControlPhrase + hazName + riverPhrase

        overview = '...' + overview + '...\n\n'
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
            format = '%I%M %p %Z %A'
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
            if self.usePreviousEditedText(dictionary):
                return self._getSavedVal(key, eventIDs, ugcList)
            else:
                # Return None so default text is generated with current HID values
                return None
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

    def usePreviousEditedText(self, dictionary):

        # Product Editor is regenerating for a change
        if self._useProductTextTable == False:
            return False
        elif self.overrideProductText:
            return True
        else:
            # Determine what level of the product dictionary was passed in
            if dictionary.get('segments', None):
                # Product Level
                for segment in dictionary.get('segments', []):
                    for section in segment.get('sections', []):
                        for hazard in section.get('hazardEvents', []):
                            if hazard.get('previousEditedTextCheckBox', False):
                                return True
            elif dictionary.get('sections', None):
                # Segment Level Table
                for section in dictionary.get('sections', []):
                    for hazard in section.get('hazardEvents', []):
                        if hazard.get('previousEditedTextCheckBox', False):
                            return True
            else:
                # Section Level
                for hazard in dictionary.get('hazardEvents', []):
                    if hazard.get('previousEditedTextCheckBox', False):
                        return True

        return False
    
    def _getAction(self, vtecRecord):
        action = vtecRecord.get('act', None)
        if action == 'COR':
            action = vtecRecord.get('prevAct')
        return action

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

