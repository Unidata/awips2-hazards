'''
    Description: Base class for all legacy formatters to inherit from.
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
import traceback
import logging, UFStatusHandler
from ProductPart import ProductPart

from abc import *

class Format(FormatTemplate.Formatter):

    def __init__(self):
        self.bridge = Bridge()
        self.basisText = BasisText()
        areaDict = self.bridge.getAreaDictionary()
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)
        self._setUpLogging()

    def initialize(self, editableEntries) :

        # To ensure time calls are based on Zulu
        os.environ['TZ'] = "GMT0"
        self.timezones = []

        # List that will hold the ProductPart objects
        # to be displayed in the Product Editor. 
        if editableEntries:
            # If this list is passed in, it means
            # product generation is being triggered by a
            # change on the product editor.
            self.editableParts = editableEntries
        else:
            self.editableParts = []

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

    def _setUpLogging(self):
        self.logger = logging.getLogger('Legacy_Base_Formatter')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'com.raytheon.uf.common.hazards.productgen', 'Legacy_Base_Formatter', level=logging.INFO))
        self.logger.setLevel(logging.INFO)

    def _printDebugProductParts(self):   
        # IF True will print the product parts and associated text during execution
        return False

    def processProductParts(self, dictionary, productParts):
        text = ''
        for part in productParts:
            if isinstance(part, ProductPart):
                productPart = part
                name = productPart.getName()
                methodName = productPart.getFormatMethod()
                if not methodName:
                    self.logger.error("No Product Part Method defined for {}. Check ProductParts.py and ensure a formatMethod is defined.".format(name))
                    continue

                # Use the editable part if passed in
                if part in self.editableParts:
                    productPart = self.editableParts[self.editableParts.index(part)]
                elif part.isDisplayable() or part.isEditable():
                    self.logger.error("Displayable/Editable product part was not found in the editableParts list.")

                partText = ''
                try:
                    # Get the product part method and call it
                    method = getattr(self, methodName)
                    partText += method(dictionary, productPart)
                except Exception as err:
                    self.logger.error(err)
                    continue

                if self._printDebugProductParts():
                    if name not in ['segments', 'sections']:
                        print 'Legacy Part:', name, ': ', partText

                if partText is not None:
                    if productPart.getGeneratedText() is None:
                        productPart.setGeneratedText(partText)
                    # Append it to the actual product
                    text += partText
            else:
                self.logger.error( "Found non Product Part object in parts list: " + str(part))
                continue
        return text

    @abstractmethod
    def execute(self, productDict, editableEntries):
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
        text += self.processProductParts(self.productDict, productParts)
        return text

    def processSubParts(self, dictionary, productPart):
        """
        Generates Legacy text from a list of subParts e.g. segments or sections
        @param dictionary: dictionary containing data for this Product Part
        @param productPart: productPart that contains subParts to process
        @return: Returns the legacy text of the subParts
        """
        text = '' 
        subPartDicts = dictionary.get(productPart.getName())
        subParts = productPart.getSubParts()
        for i in range(len(subParts)):
            text += self.processProductParts(subPartDicts[i], subParts[i])
        return text

    ######################################################
    #  Product Part Methods 
    #  Note that these methods are shared between all product
    #  formatters. If a change does not apply for all products
    #  the method must be overridden in that specific formatter.
    ######################################################

    ################# Product Level

    def CR(self, dictionary, productPart):
        return '\n'

    def wmoHeader(self, productDict, productPart):
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

    def productHeader(self, productDict, productPart):
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

    def easMessage(self, productDict, productPart):
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

    def overviewSynopsis_area(self, productDict, productPart):
        return self.overviewSynopsis(productDict, productPart)

    def overviewSynopsis_point(self, productDict, productPart):
        return self.overviewSynopsis(productDict, productPart)

    def overviewHeadline_point(self, productDict, productPart):
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

        overviewHeadline = ''
        for segments in  [
                        new_ext_productSegments,
                        con_productSegments,
                        can_exp_productSegments]: 
            if segments:
                overviewHeadline += self.getOverviewHeadlinePhrase(segments)

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(overviewHeadline)

        # Return the product text
        return productPart.getProductText()

    def callsToAction_productLevel(self, productDict, productPart):
        startText = 'PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n'
        endText = '\n\n'

        productLabel = productDict.get('productLabel')
        ctaKey = 'callsToAction_productLevel_' + productLabel

        text = ""
        callsToAction = productDict.get(ctaKey)
        if callsToAction:
            for cta in callsToAction:
                cta = cta.strip('\n\t\r')
                cta = re.sub('\s+',' ',cta)
                text += cta + endText
            text = text.rstrip()
        productPart.setGeneratedText(text)
        return self.getFormattedText(productPart, startText=startText, endText=endText)

    def nextIssuanceStatement(self, productDict,productPart):
        expireTime = None
        for segmentDict in productDict.get("segments", []):
            tmpExpireTime = segmentDict.get(HazardConstants.EXPIRATION_TIME)
            if expireTime is None or expireTime > tmpExpireTime:
                expireTime = tmpExpireTime

        nextIssue = "|* Time/Day Phrase *|."
        if expireTime:
            nextIssue = self._tpc.timingWordTableFUZZY4(self._issueTime, expireTime, self.timezones[0], 'startTime')[2] + ' at'
            for timeZone in self.timezones:
                fmtIssueTme = self._tpc.getFormattedTime(expireTime, timeZones=[timeZone], format='%I%M %p %Z')
                ### If more than one timezone, will read like: 
                ### "The next statement will be issued Tuesday morning at 600 AM CST. 500 AM MST."
                nextIssue += ' ' + fmtIssueTme + '.'
        partText = 'The next statement will be issued ' + nextIssue

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(partText)
        return self.getFormattedText(productPart, endText='\n\n') + '&&\n\n'

    def additionalInfoStatement(self, productDict, productPart):
        # Override this method for your site
        text = 'Additional information is available at www.weather.gov.'

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(text)
        return self.getFormattedText(productPart, endText='\n\n')

    def initials(self, productDict, productPart):
        initials = ForecasterInitials.getForecasterIdentification()

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(initials)
        return productPart.getProductText()

    ################# Segment Level

    def setUp_segment(self, segmentDict, productPart):
        # Save off the segmentDict so that all section productParts have a reference to it
        self._segmentDict = segmentDict
        return ''

    def endSegment(self, segmentDict, productPart):
        return '\n$$\n\n'

    def ugcHeader(self, segmentDict, productPart):
        ugcs = segmentDict.get('ugcs', [])
        expirationTime = segmentDict.get(HazardConstants.EXPIRATION_TIME)
        ugcHeader = self._tpc.formatUGCs(ugcs, expirationTime)

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(ugcHeader)
        return ugcHeader + '\n'

    def areaList(self, segmentDict, productPart):
        sections = segmentDict.get('sections')
        hazardEvent = sections[0].get('hazardEvents')[0]
        if self._tpc.isWarnGenHazard(hazardEvent):
            areaList = self._tpc.formatUGC_namesWithStateAbrev(segmentDict.get('ugcs'))
        else:
            areaList =  self._tpc.formatUGC_names(segmentDict.get('ugcs'))
        areaList = self._tpc.linebreak(areaList, 69, breakStr=[' ', '-'] ).rstrip()
        return areaList + '\n'

    def cityList(self, segmentDict, productPart):
        cityListText = ''
        cityList = set()
        for sectionDict in segmentDict.get('sections', []):
            for hazard in sectionDict.get('hazardEvents', []):
                cityList.update(hazard.get('cityList', []))
        if cityList:
            cityListText = 'Including the cities of '
            cityListText += self._tpc.punctuateList(list(cityList))
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(cityListText)
        return self.getFormattedText(productPart, endText='\n')

    def vtecRecords(self, segmentDict, productPart):
        vtecString = ''
        vtecRecords = segmentDict.get('vtecRecords')
        vtecRecords.sort(self._tpc.vtecRecordSortAlg)
        for vtecRecord in vtecRecords:
            vtecString += vtecRecord['vtecstr'] + '\n'
            if 'hvtecstr' in vtecRecord:
                if vtecRecord['hvtecstr'] != None:
                    vtecString += vtecRecord['hvtecstr'] + '\n'

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(vtecString.strip('\n'))
        return vtecString

    def polygonText(self, segmentDict, productPart):
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

        if len(lonLatDict) > 0:
            polyStr += 'LAT...LON'

            # 4 points per line
            pointsOnLine = 0
            for lon, lat in lonLatDict:
                if pointsOnLine == 4:
                    polyStr += '\n     '
                    pointsOnLine = 0
                polyStr += ' ' + str(lat) + ' ' + str(lon)
                pointsOnLine += 1
        else:
            # Per issue #28016 it's OK for FL* to not have a LAT...LON string.
            # Anything else, we will throw an exception.
            if not isPointBasedHazard:
                raise Exception('Did not find lonLat points for hazard')

        if self._testMode:
            hazard = section.get('hazardEvents', [])[0]
            if self._tpc.isRiverProHazard(hazard):
                polyStr += '\n\nTHIS IS A TEST MESSAGE. DO NOT TAKE ACTION BASED ON THIS.'
            elif self._tpc.isWarnGenHazard(hazard):
                polyStr = 'THIS IS A TEST MESSAGE. DO NOT TAKE ACTION BASED ON THIS MESSAGE.\n\n' + polyStr
        return polyStr + '\n'

    def issuanceTimeDate(self, segmentDict, productPart):
        text = self.getIssuanceTimeDate(segmentDict)
        vtecRecords = segmentDict.get('vtecRecords', [])
        phensig = vtecRecords[0].get('key', '')
        if self._testMode and self._productID in ['FFS', 'FFA', 'FLS', 'FLW']:
            text += '\n...THIS MESSAGE IS FOR TEST PURPOSES ONLY...\n'
        return text

    def summaryHeadlines(self, segmentDict, productPart, includeTiming=True):
        '''
        Creates the summary Headline
        @param segmentDict:  dictionary for the segment.
        '''
        headlineStr = ''
        vtecRecords = segmentDict.get('vtecRecords', None)
        hList = copy.deepcopy(vtecRecords)
        if len(hList):
            hList.sort(self._tpc.regularSortHazardAlg)

        while len(hList) > 0:
            vtecRecord = hList[0]
            headline = ''

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

            phen = vtecRecord.get('phen', '')
            sig = vtecRecord.get('sig', '')
            phenSig = phen + '.' + sig
            if phenSig in ['FF.W', 'FA.Y', 'FA.W']:
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

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(headlineStr)
        return self.getFormattedText(productPart, endText='\n')

    def endSegment(self, segmentDict, productPart):
        # Reset to empty dictionary
        self._segmentDict = {}
        return '\n$$\n\n' 

    ###################### Section Level

    def setUp_section(self, sectionDict, productPart):
        return ''

    def endSection(self, sectionDict, productPart):
        return ''

    def emergencyHeadline(self, dictionary, productPart):
        # Product part is both section and segment level
        if 'sections' in dictionary:
            # FFW_FFS will only have one hazard per section
            sectionDict = dictionary.get('sections')[0]
        else:
            sectionDict = dictionary

        headline = ''
        hazard = sectionDict.get('hazardEvents')[0]
        includeChoices = hazard.get('include')
        if includeChoices and 'ffwEmergency' in includeChoices:
            # ALL CAPS per Mixed Case Text Guidelines
            emergencyLocation = self._tpc.getValueOrFramedText('includeEmergencyLocation', hazard, 'Enter Emergency Location').upper()
            headline = '...FLASH FLOOD EMERGENCY FOR ' + emergencyLocation + '...'

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(headline)
        return self.getFormattedText(productPart, endText='\n\n')

    def attribution(self, sectionDict, productPart):
        attribution = self.attributionFirstBullet.getAttributionText()
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(attribution)
        return self.getFormattedText(productPart, endText='\n\n')

    def attribution_point(self, sectionDict, productPart):
        attribution = self.attributionFirstBullet.getAttributionText()
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(attribution)
        return self.getFormattedText(productPart, endText='\n\n')

    def firstBullet(self, sectionDict, productPart):
        firstBullet = self.attributionFirstBullet.getFirstBulletText()

        startText = '* '
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(firstBullet)
        return self.getFormattedText(productPart, startText=startText, endText='\n\n')

    def firstBullet_point(self, sectionDict, productPart):
        firstBullet = self.attributionFirstBullet.getFirstBulletText()

        startText = ''
        if sectionDict.get('vtecRecord').get('act') == 'NEW':
            startText = '* '
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(firstBullet)
        return self.getFormattedText(productPart, startText=startText, endText='\n')

    def timeBullet(self, sectionDict, productPart, roundMinutes=15, includeTimingDesc=False):
        '''
        Displays the start and/or end time/date of the hazard. There is specific logic for
        RiverPro hazards found in getRiverProTimeBullet otherwise the following logic is 
        used. 
        When hazard extends:
        - Less than 1 week . . . should include day
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
            expireTime = hazard.get(HazardConstants.EXPIRATION_TIME, None)
            if not expireTime:
                expireTime = self._tpc.round(endTime, roundMinutes)

            # Determine how far into the future the end time is.
            issueTime = datetime.datetime.fromtimestamp(float(self._issueTime)/1000) # Local time
            tdelta = expireTime - issueTime
    
            if (tdelta.days == 6 and expireTime.date().weekday() == issueTime.date().weekday()) or \
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
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)
        return self.getFormattedText(productPart, startText=startText, endText='\n')

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

    def emergencyStatement(self, sectionDict, productPart):
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
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(statement)
        return self.getFormattedText(productPart, endText='\n\n')

    def impactsBullet(self, sectionDict, productPart):
        bulletText = ''
        impacts = []
        for hazard in sectionDict.get('hazardEvents'):
            hazardImpacts = hazard.get('impacts')
            if hazardImpacts:
                impacts.append(hazardImpacts)
        if len(impacts) > 0:
            bulletText += '\n'.join(impacts)

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)
        startText = '* '
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self.getFormattedText(productPart, startText=startText, endText='\n\n')

    def basisAndImpactsStatement(self, sectionDict, productPart):
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

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)

        startText = ''
        act = sectionDict.get('vtecRecord',{}).get('act')
        if act in ['NEW', 'EXT']:
            startText = '* '
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self.getFormattedText(productPart, startText=startText, endText='\n\n')

    def locationsAffected(self, sectionDict, productPart):
        vtecRecord = sectionDict.get('vtecRecord', {})
        action = self._getAction(vtecRecord)
        # FA.W, FA.Y, and FF.W will only have one hazard per section
        hazardEventDict = sectionDict.get('hazardEvents')[0]

        locationsAffected = ''
        # This is a optional bullet check to see if it should be included
        locationsAffectedChoice = hazardEventDict.get("locationsAffectedRadioButton", None)
        if locationsAffectedChoice == "damInfo":
            damOrLeveeName = hazardEventDict.get('damOrLeveeName')
            if damOrLeveeName:
                damInfo = self._damInfo(damOrLeveeName)
                if damInfo:
                    # addInfo
                    addInfo = damInfo.get('addInfo', \
                         'The nearest downstream town is |*downstream town*| from the dam.')
                    locationsAffected = addInfo

                    # Scenario
                    scenarioText = "|* Enter Scenario Text *|"
                    scenario = hazardEventDict.get('scenario')
                    if scenario:
                        scenarios = damInfo.get('scenarios')
                        if scenarios:
                            scenarioText = scenarios.get(scenario)
                            if isinstance(scenarioText, dict) :
                                scenarioText = scenarioText.get("productString")

                    # If both addInfo and scenarioText are hardcoded default, only
                    # include addInfo.
                    if scenarioText.find('|*')<0 or addInfo.find('|*')>=0:
                        locationsAffected += ' '+scenarioText

                    # Rule of Thumb
                    ruleOfThumbText = damInfo.get('ruleofthumb')
                    if isinstance(ruleOfThumbText, str) :
                        locationsAffected += "\n\n"+ruleOfThumbText

                    locationsAffected += "\n\n"
                else:
                    locationsAffected += "|* Enter CityInfo, Scenario and/or Rule of Thumb Text *|\n\n"
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

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(locationsAffected.rstrip())

        startText = ''
        if action in ['NEW', 'EXT']:
            startText += '* '
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self.getFormattedText(productPart,startText=startText, endText='\n\n')

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

    def additionalComments(self, sectionDict, productPart):
        # FA.W, FA.Y, and FF.W will only have one hazard per section
        hazard = sectionDict.get('hazardEvents')[0]
        additionalComments = self.createAdditionalComments(hazard)
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(additionalComments)
        return self.getFormattedText(productPart, startText='', endText='\n\n')

    def endingSynopsis(self, dictionary, productPart):
        phensigs = []
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
                text = ""
                for phensig in phensigs:
                    if phensig in ['FF.W', 'FA.W', 'FA.Y'] and hazard.get('replacedBy') is None:
                        # Hazard is not being replaced
                        text = 'Flooding is no longer expected to pose a threat. Please continue to heed remaining road closures.'
                        break

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(text)
        return self.getFormattedText(productPart, endText='\n\n')

    def callsToAction_sectionLevel(self, sectionDict, productPart):
        callsToAction =  self._tpc.getVal(sectionDict, HazardConstants.CALLS_TO_ACTION, '')
        if callsToAction and callsToAction != '':
            callsToAction = callsToAction.rstrip()
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(callsToAction)
        return self.getFormattedText(productPart, startText='PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n', endText='\n\n&&\n\n')

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

    def overviewSynopsis(self, productDict, productPart):
        productLabel = productDict.get('productLabel')
        synopsisKey = 'overviewSynopsisCanned_' + productLabel
        synopsis = productDict.get(synopsisKey, "").strip()
        productPart.setGeneratedText(synopsis)
        startText = '.'
        endText='.\n\n'
        productText = productPart.getProductText()
        if productText:
            # If productText starts/ends with period, remove it from startText/endText
            if productText[0] == '.':
                startText = startText[:-1]
            if productText[-1] == '.':
                endText = endText[1:]
        return self.getFormattedText(productPart, startText=startText, endText=endText)

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
                    riverName = hazard.get('groupName')
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

    def getFormattedText(self, productPart, startText='', endText=''):
        '''
        Utility method to add beginning and ending text to a string only
        if the string exists. Otherwise it returns a empty string.
        
        @param productPart: Product Part that contains the text to be formatted
        @param startText: text that should come before the product part text
        @param endText: text that should follow the product part text
        '''
        productText = productPart.getProductText()
        if productText:
            return startText + productText + endText
        return ''

    def _damInfo(self, damOrLeveeName):
        return self._tpc.damInfoFor(self, damOrLeveeName)

    def _getAction(self, vtecRecord):
        action = vtecRecord.get('act', None)
        if action == 'COR':
            action = vtecRecord.get('prevAct')
        return action

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()
