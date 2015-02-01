'''
    Description: Legacy formatter for FLW_FLS products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 31, 2015 4937       Robert.Blum General cleanup along with implementing a dictionary
                                        mapping of productParts to the associated methods.
'''

import datetime
import collections
import types, re, sys
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self) :
        self.initProductPartMethodMapping()
        super(Format, self).initialize()

    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'easMessage': self._easMessage,
            'productHeader': self._productHeader,
            'overviewHeadline_point': self._overviewHeadline_point,
            'overviewSynopsis_area': self._overviewSynopsis_area,
            'overviewSynopsis_point': self._overviewSynopsis_point,
            'vtecRecords': self._vtecRecords,
            'areaList': self._areaList,
            'issuanceTimeDate': self._issuanceTimeDate,
            'callsToAction': self._callsToAction,
            'callsToAction_productLevel': self._callsToAction_productLevel,
            'polygonText': self._polygonText,
            'cityList': self._cityList,
            'summaryHeadlines': self._summaryHeadlines,
            'attribution': self._attribution,
            'attribution_point': self._attribution_point,
            'firstBullet': self._firstBullet,
            'firstBullet_point': self._firstBullet_point,
            'timeBullet': self._timeBullet,
            'basisBullet': self._basisBullet,
            'additionalInfoStatement': self._additionalInfoStatement,
            'nextIssuanceStatement': self._nextIssuanceStatement,
            'endingSynopsis': self._endingSynopsis,
            'basisAndImpactsStatement': self._basisAndImpactsStatement,
            'pointImpactsBullet': self._pointImpactsBullet,
            'impactsBullet': self._impactsBullet,
            'observedStageBullet': self._observedStageBullet,
            'recentActivityBullet': self._recentActivityBullet,
            'floodStageBullet': self._floodStageBullet,
            'floodCategoryBullet': self._floodCategoryBullet,
            'floodHistoryBullet': self._floodHistoryBullet,
            'otherStageBullet': self._otherStageBullet,
            'forecastStageBullet': self._forecastStageBullet,
            'floodPointTable': self._floodPointTable
                                }

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
            if name in self.productPartMethodMapping:
                partText = self.productPartMethodMapping[name](productDict)
            elif name in ['setUp_product', 'setUp_segment', 'setUp_section', 'wrapUp_product']:
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

            text += partText
        return text

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    ################# Segment Level

    ################# Section Level

    def _attribution(self, segmentDict):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(segmentDict)

        # Get the text for the type of flooding based on immediateCause
        typeOfFlooding = self.immediateCauseMapping(segmentDict.get('immediateCause', None))
        warningType = segmentDict.get('warningType')

        # Use this to determine which first bullet format to use.
        vtecRecord = segmentDict.get('vtecRecord')
        hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

        if len(vtecRecord.get('hdln')):
            action = vtecRecord.get('act')

        if action == 'NEW':
            attribution = nwsPhrase + 'issued a'
        elif action == 'CON':
            attribution += '...The ' + hazName
            if vtecRecord.get('phensig') == 'FA.W':
                if warningType:
                    attribution += ' for ' + warningType
                if typeOfFlooding:
                    attribution += ' for ' + typeOfFlooding
            attribution += ' remains in effect for ' + areaPhrase + '...'
        elif action == 'EXT':
            attribution = nwsPhrase + 'extended the '
        elif action == 'CAN':
            attribution = '...The ' + hazName
            if vtecRecord.get('phensig') == 'FA.W':
                if warningType:
                    attribution += ' for ' + warningType
                if typeOfFlooding:
                    attribution += ' for ' + typeOfFlooding
            attribution += ' for ' + areaPhrase + ' has been canceled...'
        elif action == 'EXP':
            expTimeCurrent = self._issueTime
            if vtecRecord.get('endTime') <= expTimeCurrent:
                attribution = 'The ' + hazName + \
                  ' for ' + areaPhrase + ' has expired.'
            else:
               timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
               attribution = 'The ' + hazName + \
                  ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        elif action == 'ROU':
            attribution = nwsPhrase + 'released '

        return attribution + '\n\n'

    def _attribution_point(self, segmentDict):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(segmentDict)

        # Use this to determine which first bullet format to use.
        vtecRecord = segmentDict.get('vtecRecord')
        hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

        if len(vtecRecord.get('hdln')):
            action = vtecRecord.get('act')

        if action == 'NEW':
            attribution = nwsPhrase + 'issued a'
        elif action == 'CON':
            attribution = 'The ' + hazName + ' remains in effect for...'
        elif action == 'EXT':
            attribution = 'The ' + hazName + ' is now in effect for...' 
        elif action == 'CAN':
            attribution = 'The ' + hazName + \
               ' for... ' + areaPhrase + ' has been canceled.'
        elif action == 'EXP':
            expTimeCurrent = self._issueTime
            if vtecRecord.get('endTime') <= expTimeCurrent:
                attribution = 'The ' + hazName + \
                  ' for ' + areaPhrase + ' has expired.'
            else:
               timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
               attribution = 'The ' + hazName + \
                  ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        return attribution + '\n'

    def _firstBullet(self, segmentDict):
        firstBullet = '* '
        areaPhrase = self.createAreaPhrase(segmentDict)

        if self._runMode == 'Practice':
            firstBullet += 'This is a test message.  '

        warningType = segmentDict.get('warningType')
        typeOfFlooding = segmentDict.get('typeOfFlooding')
        advisoryType = segmentDict.get('advisoryType_productString')
        optionalSpecificType = segmentDict.get('optionalSpecificType')

        # Use this to determine which first bullet format to use.
        vtecRecord = segmentDict.get('vtecRecord')
        hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

        if len(vtecRecord.get('hdln')):
            action = vtecRecord.get('act')

        if action == 'NEW':
            if vtecRecord.get('phensig') == 'FA.W':
                if warningType:
                    firstBullet += hazName + ' for ' + warningType + ' for...\n'
                else:
                    firstBullet += hazName + ' for...\n'
                if typeOfFlooding:
                    #TODO ProductUtils.wrapLegacy should handle the below indent
                    firstBullet += '  ' + typeOfFlooding + ' in...\n'
                firstBullet += areaPhrase
            elif vtecRecord.get('phensig') == 'FA.Y':
                # Add advisoryType to first bullet if in dictionary
                if advisoryType:
                    firstBullet += advisoryType + ' ' + hazName
                else:
                    firstBullet += hazName
                # Add optionalSpecificType to first bullet if in dictionary
                if optionalSpecificType:
                    firstBullet+= ' for ' +  optionalSpecificType + ' for...\n'
                else:
                    firstBullet += ' for...\n'
                # Add typeOfFlooding to first bullet if in dictionary
                if typeOfFlooding:
                    #TODO ProductUtils.wrapLegacy should handle the below indent
                    firstBullet += '  ' + typeOfFlooding + ' in...\n'
                firstBullet += areaPhrase
            else:
                firstBullet += hazName + ' for'
                firstBullet += areaPhrase

        elif action == 'CON':
            if vtecRecord.get('phensig') == 'FA.W':
                firstBullet += '...The ' + hazName
                if warningType:
                    firstBullet += ' for ' + warningType
                if typeOfFlooding:
                    firstBullet += ' for ' + typeOfFlooding
                firstBullet += ' remains in effect for...'
            firstBullet +=  areaPhrase

        elif action == 'EXT':
            if vtecRecord.get('phensig') == 'FA.W':
                if warningType:
                    firstBullet += hazName + ' for ' + warningType + ' for...\n'
                else:
                    firstBullet += hazName + ' for...\n'
                if typeOfFlooding:
                    #TODO ProductUtils.wrapLegacy should handle the below indent
                    firstBullet += '  ' + typeOfFlooding + ' in...\n'
                firstBullet += areaPhrase
            else:
                firstBullet += hazName + ' for...\n'
                firstBullet += areaPhrase

        elif action == 'CAN':
            firstBullet = ''

        elif action == 'EXP':
            firstBullet = ''

        elif action == 'ROU':
            firstBullet += hazName + ' for '
            firstBullet += areaPhrase
        return firstBullet + '\n'

    def _firstBullet_point(self, segmentDict):
        firstBullet = ''
        areaPhrase = self.createAreaPhrase(segmentDict)

        # Use this to determine which first bullet format to use.
        vtecRecord = segmentDict.get('vtecRecord')
        hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

        if len(vtecRecord.get('hdln')):
            action = vtecRecord.get('act')

        if action == 'NEW':
            firstBullet += '* ' + hazName + ' for\n'
            firstBullet += areaPhrase
        elif action in ['CON', 'EXT']:
            firstBullet +=  'The ' + hazName + ' continues for\n' + '  ' + areaPhrase
        elif action == 'CAN':
            firstBullet +=  'The ' + hazName + ' is canceled for\n' + '  ' + areaPhrase
        elif action == 'ROU':
            firstBullet += hazName + ' for\n' + '  ' + areaPhrase
        return firstBullet + '\n'

    def _basisBullet(self, sectionDict):
        vtecRecord = sectionDict.get('vtecRecord')
        act = vtecRecord.get('act')
        phen = vtecRecord.get("phen")
        sig = vtecRecord.get('sig')
        bulletText = ''

        if act in ['NEW', 'ROU', 'EXT']:
            bulletText += '* '

        if (self._runMode == 'Practice'):
            bulletText += "This is a test message.  "

        if self.timezones:
            # use first time zone in the list
            bulletText += 'At ' + self._tpc.formatDatetime(self._issueTime, '%l%M %p %Z', self.timezones[0]).strip()

        # Use basisFromHazardEvent for WarnGen only hazards
        if phen == 'FA' and sig in ['W', 'Y']:
            hazardType = phen + '.' + sig
            basis = self.basisText.getBulletText(hazardType, segmentDict)
            basis = self._tpc.substituteParameters(segmentDict, basis)
        else:
            # TODO Need to create basisText for Non-WarnGen hazards
            basis = "...Flooding from heavy rain. This rain was located over the warned area."

        bulletText+= basis

        return bulletText + '\n\n'

