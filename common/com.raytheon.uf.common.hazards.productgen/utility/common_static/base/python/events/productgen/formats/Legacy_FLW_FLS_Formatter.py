'''
    Description: Legacy formatter for FLW_FLS products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 31, 2015 4937       Robert.Blum General cleanup along with implementing a dictionary
                                        mapping of productParts to the associated methods.
    Feb 20, 2015 4937       Robert.Blum Added groupSummary productPart method to mapping
'''

import datetime,collections
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
            'locationsAffected': self._locationsAffected,
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
            'floodPointTable': self._floodPointTable,
            'setUp_segment': self._setUp_segment,
            'groupSummary': self._groupSummary,
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

    ################# Segment Level

    ################# Section Level

    def _timeBullet(self, segmentDict):
        timeBullet = super(Format, self)._timeBullet(segmentDict)
        if segmentDict.get('geoType', '') == 'area':
            timeBullet+= '\n'
        return timeBullet

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
            basis = self.basisText.getBulletText(hazardType, sectionDict)
            basis = self._tpc.substituteParameters(sectionDict, basis)
        else:
            # TODO Need to create basisText for Non-WarnGen hazards
            basis = "...Flooding from heavy rain. This rain was located over the warned area."

        bulletText+= basis

        return bulletText + '\n\n'

