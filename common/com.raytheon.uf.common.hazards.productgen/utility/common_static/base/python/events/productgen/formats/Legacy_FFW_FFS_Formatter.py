'''
    Description: Legacy formatter for FFW products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Oct 24, 2014    4933    Robert.Blum Initial creation
    Jan 12  2015    4937    Robert.Blum Refactor to inherit from new
                                        formatter classes.
    Jan 31, 2015    4937    Robert.Blum General cleanup along with implementing a dictionary
                                        mapping of productParts to the associated methods.
'''


import datetime

import types, re, sys
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
            'vtecRecords': self._vtecRecords,
            'areaList': self._areaList,
            'issuanceTimeDate': self._issuanceTimeDate,
            'callsToAction': self._callsToAction,
            'polygonText': self._polygonText,
            'cityList': self._cityList,
            'summaryHeadlines': self._summaryHeadlines,
            'basisAndImpactsStatement_segmentLevel': self._basisAndImpactsStatement_segmentLevel,
            'emergencyHeadline': self._emergencyHeadline,
            'attribution': self._attribution,
            'firstBullet': self._firstBullet,
            'timeBullet': self._timeBullet,
            'basisBullet': self._basisBullet,
            'emergencyStatement': self._emergencyStatement,
            'locationsAffected': self._locationsAffected,
            'endingSynopsis': self._endingSynopsis,
            'floodPointHeader': self._floodPointHeader,
            'floodPointHeadline': self._floodPointHeadline,
            'observedStageBullet': self._observedStageBullet,
            'floodStageBullet': self._floodStageBullet,
            'floodCategoryBullet': self._floodCategoryBullet,
            'otherStageBullet': self._otherStageBullet,
            'forecastStageBullet': self._forecastStageBullet,
            'pointImpactsBullet': self._pointImpactsBullet,
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

            text += partText
        return text

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _easMessage(self, productDict):
        vtecRecords = productDict.get('vtecRecords')
        for vtecRecord in vtecRecords:
            if 'sig' in vtecRecord:
                if vtecRecord['sig'] is 'A':
                    return 'Urgent - Immediate broadcast requested\n'
        return 'Bulletin - EAS activation requested\n'

    ################# Segment Level

    ################# Section Level
    def _attribution(self, segmentDict):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(segmentDict)

        # Use this to determine which first bullet format to use.
        vtecRecord = segmentDict.get('vtecRecord')
        hazName = hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

        if hazName:
            action = vtecRecord.get('act')

        if action == 'NEW':
            attribution = nwsPhrase + 'issued a'
        elif action == 'CON':
            attribution = 'the ' + hazName + ' remains in effect for...'
        elif action == 'EXT':
            attribution = nwsPhrase + 'has extended the'
#                 attribution = 'the ' + hazName + ' is now in effect for...' 
        elif action == 'CAN':
            attribution = 'the ' + hazName + \
               ' for... ' + areaPhrase + ' has been canceled.'
        elif action == 'EXP':
            expTimeCurrent = self._issueTime
            if vtecRecord.get('endTime') <= expTimeCurrent:
                attribution = 'the ' + hazName + \
                  ' for ' + areaPhrase + ' has expired.'
            else:
               timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
               attribution = 'the ' + hazName + \
                  ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        return attribution + '\n\n'

    def _firstBullet(self, segmentDict):
        firstBullet = '* '
        areaPhrase = self.getAreaPhrase(segmentDict)

        if self._runMode == 'Practice':
            testText = 'This is a test message.  '
            firstBullet = firstBullet + testText

        # Use this to determine which first bullet format to use.
        vtecRecord = segmentDict.get('vtecRecord')
        hazName = hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)

        if hazName:
            action = vtecRecord.get('act')

        if action == 'NEW':
            firstBullet += hazName + ' for...\n'
            firstBullet += areaPhrase
        elif action == 'CON':
            firstBullet +=  areaPhrase
        elif action == 'EXT':
            firstBullet += hazName + ' for...\n'
            firstBullet += areaPhrase

        return firstBullet + '\n'

    def _timeBullet(self, segmentDict):
        bulletText = super(Format, self)._timeBullet(segmentDict)
        return bulletText + '\n'

    def _basisBullet(self, sectionDict):
        bulletText = '* '
        if self._runMode == 'Practice':
            bulletText += 'This is a test message.  '

        vtecRecord = sectionDict.get('vtecRecord')
        phen = vtecRecord.get('phen')
        sig = vtecRecord.get('sig')
        subType = sectionDict.get('subType')
        hazardType = phen + '.' + sig + '.' + subType
        basis = self.basisText.getBulletText(hazardType, sectionDict)
        basis = self._tpc.substituteParameters(sectionDict, basis)

        if basis is None :
             basis = '...Flash Flooding was reported'

        # Create basis statement
        eventTime = vtecRecord.get('startTime')
        eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z ', stripLeading=True, timeZones=self.timezones)
        bulletText += 'At ' + eventTime
        bulletText += basis
        return bulletText + '\n\n'

    def _damInfo(self):
        from MapsDatabaseAccessor import MapsDatabaseAccessor
        damList = []
        mapsAccessor = MapsDatabaseAccessor()
        damInfoList = mapsAccessor.getAllDamInfo()
        
        damInfoDict = {}
        for damInfo in damInfoList:
            scenarios = {}
            scenarios['highFast'] = damInfo['scenario_high_fast']
            scenarios['highNormal'] = damInfo['scenario_high_normal']
            scenarios['mediumFast'] = damInfo['scenario_medium_fast']
            scenarios['mediumNormal'] = damInfo['scenario_medium_normal']
            basicInfo = {}
            basicInfo['riverName'] = damInfo['river_name']
            basicInfo['cityInfo'] = damInfo['city_info']
            basicInfo['ruleOfThumb'] = damInfo['rule_of_thumb']
            basicInfo['scenarios'] = scenarios
            damInfoDict[damInfo['name']] = basicInfo
            
        return damInfoDict
            
