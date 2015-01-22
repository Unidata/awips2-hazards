'''
    Description: Base class for all legacy formatters to inherit from.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
'''

import FormatTemplate
import datetime
import collections
import types, re, sys, copy
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from QueryAfosToAwips import QueryAfosToAwips
from Bridge import Bridge
from TextProductCommon import TextProductCommon
import ProductTextUtil

from abc import *

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
        self._issueTime = self.productDict['issueTime']
        self._issueFlag = self.productDict['issueFlag']
        self._runMode = self.productDict['runMode']
        if self._runMode == 'Practice':
            self._testMode = True
        self._siteInfo = self.bridge.getSiteInfo()
        self.setSiteInfo()

    def _printDebugProductParts(self):   
        # IF True will print the product parts and associated text during execution
        return False

    @abstractmethod
    def _processProductParts(self, eventSet):
        '''
        Must be overridden by the Product Fromatter
        '''
        pass

    @abstractmethod
    def execute(self, eventSet, dialogInputMap):
        '''
        Must be overridden by the Product Formatter
        '''
        pass

    def _createTextProduct(self):
        # Setup the Time Zones
        segments = self.productDict['segments']
        for segment in segments:
            self.timezones += segment['timeZones']

        # Build the text product based on the productParts
        text = ''
        productParts = self.productDict['productParts']
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

    def _wmoHeader(self, productID, siteID, startTime):
        siteEntry = self._siteInfo.get(siteID)
        fullStationID = siteEntry.get('fullStationID')  # KBOU
        ddhhmmTime = self._tpc.getFormattedTime(startTime, '%d%H%M', stripLeading=False)
        a2a = QueryAfosToAwips(productID, siteID)
        wmoID = a2a.getWMOprod()  # e.g. WUUS53
        header = wmoID + ' ' + fullStationID + ' ' + ddhhmmTime + '\n'
        header += productID + siteID
        return header

    def _productHeader(self):
        text = ''
        if (self._runMode == 'Practice'):
            text += 'Test...' + self._productName + '...Test \n'
        else:
            text += self._productName + '\n'
        text += 'National Weather Service ' + self._wfoCityState + '\n'
        text += self._issuanceTimeDate()
        return text

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

    def _overviewHeadline_area(self):
        text =  '|* Overview Headline *|'
        return text + '\n\n'

    ################# Segment Level

    def _ugcHeader(self, segment):
        ugcList = []
        for area in segment['impactedAreas']:
            ugcList.append(area['ugc'])
        ugcList.sort()
        ugctext = self._tpc.makeUGCString(ugcList)
        endTime = self._endTime
        return ugctext + '-' + endTime.strftime('%d%H%m-') + '\n'

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

    def _callsToAction(self, productDict, name):
        text = ''
        if name == 'callsToAction_productLevel':
            productLabel = productDict.get('productLabel')
            ctaKey = 'callsToAction_productLevel_' + productLabel
        else:
            ctaKey = name

        elements = KeyInfo.getElements(ctaKey, productDict)
        if len(elements) > 0:
            callsToAction = productDict[elements[0]]
        else:
            callsToAction = productDict.get(ctaKey, None)

        if callsToAction:
            text = 'Precautionary/Preparedness actions...\n\n'
            for cta in callsToAction:
                cta = cta.strip('\n\t\r')
                cta = re.sub('\s+',' ',cta)
                text += cta + '\n\n'
            if name == 'callsToAction':
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

    def _summaryHeadlines(self, segmentDict, includeTiming=True):
        '''
        Creates the summary Headline
        
        @param segmentDict:  dictionary for the segment.
        '''
        headlines = []
        headlineStr = ''
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

            # add on the action
            actionWords = self._tpc.actionControlWord(vtecRecord, self._issueTime)
            hazStr = hazStr + ' ' + actionWords
            
            if includeTiming:
                timeWords = self._tpc.getTimingPhrase(vtecRecord, [], self._issueTime, timeZones=self.timezones)
                if len(timeWords):
                    hazStr = hazStr + ' ' + timeWords

            if len(hazStr):
                # Call user hook
                localStr = self._tpc.hazard_hook(
                  None, None, vtecRecord['phen'], vtecRecord['sig'], vtecRecord['act'],
                  vtecRecord['startTime'], vtecRecord['endTime'])  # May need to add leading space if non-null 
                headlineStr = headlineStr + '...' + hazStr + localStr + '...\n'

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

    def _basisAndImpactsStatement_segmentLevel(self, segment):
        bulletText = '|* Current hydrometeorological situation and expected impacts *|'
        return bulletText + '\n\n'

    ###################### Section Level

    def _vtecRecords(self, segment):
        vtecString = ''
        vtecRecords = segment['vtecRecords']
        for vtecRecord in vtecRecords:
            vtecString += vtecRecord['vtecstr'] + '\n'
            if 'hvtecstr' in vtecRecord:
                if vtecRecord['hvtecstr'] != None:
                    vtecString += vtecRecord['hvtecstr'] + '\n'
        return vtecString

    def _timeBullet(self, segment, roundMinutes=15):
        endTime = self._endTime
        expireTime = self.round(endTime, roundMinutes)
        text = '* '
        if (self._runMode == 'Practice' and segment['geoType'] != 'point'):
            text += "This is a test message.  "

        text += 'Until '
        timeStr = ''
        for tz in self.timezones:
            if len(timeStr) > 0:
                timeStr += '/'
            timeStr += self._tpc.formatDatetime(expireTime, '%l%M %p %Z', tz).strip()

        text += timeStr

        return text + '\n'

    def _impactsBullet(self, segment):
        bulletText = '* '
        if (self._runMode == 'Practice'):
            bulletText += "This is a test message.  "

        impacts = segment['impacts']
        if not impacts:
            impacts = self._tpc.frame('(Optional) Potential impacts of flooding')
            
        bulletText += impacts + '\n'

        # Add any additioinal comments here, listOfDrainages, floodMoving, etc.
        additionalCommentsText = self.createAdditionalComments(segment)
        if additionalCommentsText:
            bulletText += '\n' + additionalCommentsText
        return bulletText

    def _basisAndImpactsStatement(self, segment):
        #TODO Warngen seems to just have the basis here. Does impacts
        # still need to be added?
        bulletText = self._basisBullet(segment)

        # Add any additioinal comments here, listOfDrainages, floodMoving, etc.
        additionalCommentsText = self.createAdditionalComments(segment)
        if additionalCommentsText:
            bulletText += '\n' + additionalCommentsText
            return bulletText
        else:
            return bulletText

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

    def getAreaPhraseBullet(self, productDict):
        ugcList = []
        ugcPortions = {}
        ugcPartsOfState = {}

        for area in productDict['impactedAreas']:
            ugcList.append(area['ugc'])
            if 'portions' in area:
                ugcPortions[area['ugc']] = area['portions']
            if 'partsOfState' in area:
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
                textLine += self._tpc.getInformationForUGC(ugc, "fullStateName")+"..."
            else :
                textLine += part+" "+self._tpc.getInformationForUGC(ugc, "fullStateName")+"..."
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

    def _issuanceTimeDate(self):
        text = ''
        for tz in self.timezones:
            text = self._tpc.formatDatetime(self._startTime, '%l%M %p %Z %a %b %e %Y', tz).strip()
            # only need the first time zone for issue time
            break
        text += '\n'
        # The upper() is needed to pass the WarningDecoder.
        return text.upper()

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

    def createAreaPhrase(self, productDict):
        phen = productDict['hazards'][0]['phenomenon']
        sig = productDict['hazards'][0]["significance"]
        action = productDict['hazards'][0]['act']
        geoType = productDict['geoType']

        if phen in ["FF", "FA", "FL" ] and \
           action in [ "NEW", "EXA", "EXT", "EXB" ] and \
           geoType == 'area' and sig != "A" :
            areaPhrase = self.getAreaPhraseBullet(productDict)
        else :
            areaPhrase = self.getAreaPhrase(productDict)
        return areaPhrase