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
    Apr 16, 2015    7579    Robert.Blum Updates for amended Product Editor.
    Apr 27, 2015    7579    Robert.Blum Removed non-editable fields from product editor.
    Apr 30, 2015    7579    Robert.Blum Changes for multiple hazards per section.
    May 07, 2015    6979    Robert.Blum EditableEntries are passed in for reuse.
    May 14, 2015    7376    Robert.Blum Changed to look for only None and not
                                        empty string.
    Jun 03, 2015    8530    Robert.Blum Added ProductPart for initials and the additionalComments.
'''


import datetime, collections

import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self, editableEntries) :
        self.initProductPartMethodMapping()
        super(Format, self).initialize(editableEntries)

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
            'additionalComments': self._additionalComments,
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
            'setUp_section': self._setUp_section,
            'endSegment': self._endSegment,
            'initials': self._initials,
                               }

    def execute(self, productDict, editableEntries=None):
        self.productDict = productDict
        self.initialize(editableEntries)
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level
    def _easMessage(self, productDict):
        # ALL CAPS per Mixed Case Text Guidelines
        easMessage = 'BULLETIN - EAS ACTIVATION REQUESTED'
        vtecRecords = productDict.get('vtecRecords')
        for vtecRecord in vtecRecords:
            if 'sig' in vtecRecord:
                if vtecRecord['sig'] is 'A':
                    easMessage = 'URGENT - IMMEDIATE BROADCAST REQUESTED'
                    break
        return easMessage + '\n'

    ################# Segment Level

    ################# Section Level

    def _timeBullet(self, sectionDict):
        bulletText = super(Format, self)._timeBullet(sectionDict)
        return self._getFormattedText(bulletText, endText='\n')

    def _basisBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getVal('basisBullet', sectionDict)
        if bulletText is None:
            bulletText = ''
            vtecRecord = sectionDict.get('vtecRecord')
            phen = vtecRecord.get('phen')
            sig = vtecRecord.get('sig')
            hazards = sectionDict.get('hazardEvents')
            # FFW_FFS sections will only contain one hazard
            subType = hazards[0].get('subType')
            hazardType = phen + '.' + sig + '.' + subType
            basis = self.basisText.getBulletText(hazardType, hazards[0])
            basis = self._tpc.substituteParameters(hazards[0], basis)
            if basis is None :
                 basis = '...Flash Flooding was reported'

            # Create basis statement
            eventTime = vtecRecord.get('startTime')
            eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z ', stripLeading=True, timeZones=self.timezones)
            bulletText += 'At ' + eventTime.rstrip()
            bulletText += basis
        self._setVal('basisBullet', bulletText, sectionDict, 'Basis Bullet')

        startText = '* '
        if (self._runMode == 'Practice'):
            startText += "This is a test message.  "
        return self._getFormattedText(bulletText, startText=startText, endText='\n\n')

    def _damInfo(self):
        from MapsDatabaseAccessor import MapsDatabaseAccessor
        mapsAccessor = MapsDatabaseAccessor()
        damInfoDict = mapsAccessor.getAllDamInundationMetadata()
        return damInfoDict
