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


import datetime, collections

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
            'floodPointTable': self._floodPointTable,
            'setUp_segment': self._setUp_segment,
            'endSegment': self._endSegment,
                               }

    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()

        self._editableProductParts = self._getEditableParts(productDict)
        self._editableParts = {}
        legacyText = self._createTextProduct()
        return [[ProductUtils.wrapLegacy(legacyText)],self._editableParts]

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
        bulletText += 'At ' + eventTime.rstrip()
        bulletText += basis
        return bulletText + '\n\n'

    def _damInfo(self):
        from MapsDatabaseAccessor import MapsDatabaseAccessor
        mapsAccessor = MapsDatabaseAccessor()
        damInfoDict = mapsAccessor.getAllDamInundationMetadata()
            
        return damInfoDict
            