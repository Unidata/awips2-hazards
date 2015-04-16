'''
    Description: Legacy formatter for hydro FFA products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 07  2015            mduff       Initial release
    Jan 26, 2015 4936       chris.cody  Implement scripts for Flash Flood Watch Products (FFA,FAA,FLA)
    Jan 31, 2015 4937       Robert.Blum General cleanup along with implementing a dictionary mapping of 
                                        productParts to the associated methods.
    Feb 20, 2015 4937       Robert.Blum Added groupSummary productPart method to mapping
    Mar 17, 2015 6958       Robert.Blum Removed the start time from basisBullet.
    Apr 16, 2015 7579       Robert.Blum Updates for amended Product Editor.
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
            'overviewHeadline_point': self._overviewHeadline_point,
            'overviewSynopsis_area': self._overviewSynopsis_area,
            'overviewSynopsis_point': self._overviewSynopsis_point,
            'vtecRecords': self._vtecRecords,
            'areaList': self._areaList,
            'issuanceTimeDate': self._issuanceTimeDate,
            'callsToAction': self._callsToAction,
            'callsToAction_productLevel': self._callsToAction_productLevel,
            'emergencyHeadline': self._emergencyHeadline,
            'emergencyStatement': self._emergencyStatement,
            'locationsAffected': self._locationsAffected,
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
            'rainFallStatement': self._rainFallStatement,
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
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    ################# Segment Level

    ################# Section Level

    def _timeBullet(self, sectionDict):
        timeBullet = super(Format, self)._timeBullet(sectionDict)
        if sectionDict.get('geoType', '') == 'area':
            timeBullet+= '\n'
        return timeBullet

    def _basisBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getSavedVal('basisBullet', sectionDict)
        if not bulletText:
            vtecRecord = sectionDict.get('vtecRecord')
            startTime = sectionDict.get('startTime')
            act = vtecRecord.get('act')
            if act == 'COR':
                act == vtecRecord.get('prevAct')
            bulletText = ''

            if (self._runMode == 'Practice'):
                bulletText += "This is a test message.  "

            basisStatement = sectionDict.get('basisStatement')
            if basisStatement:
                bulletText += basisStatement
            else:
                bulletText += ' |* current hydrometeorological basis *| '
        self._setVal('basisBullet', bulletText, sectionDict, 'Basis Bullet')
        return '* ' + bulletText + '\n\n'
