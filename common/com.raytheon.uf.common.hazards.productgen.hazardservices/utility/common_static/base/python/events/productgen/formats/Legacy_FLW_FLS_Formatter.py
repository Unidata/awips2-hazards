'''
    Description: Legacy formatter for FLW_FLS products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
    Jan 31, 2015 4937       Robert.Blum General cleanup along with implementing a dictionary
                                        mapping of productParts to the associated methods.
    Feb 20, 2015 4937       Robert.Blum Added groupSummary productPart method to mapping.
    Mar 17, 2015 6958       Robert.Blum BasisBullet only has start time if it is Warning.
    Apr 16, 2015 7579       Robert.Blum Updates for amended Product Editor.
    Apr 30, 2015 7579       Robert.Blum Changes for multiple hazards per section.
    May 07, 2015 6979       Robert.Blum EditableEntries are passed in for reuse.
    May 14, 2015 7376       Robert.Blum Changed to look for only None and not
                                        empty string.
    Jun 03, 2015 8530       Robert.Blum Added method for additionalComments productPart.
'''

import datetime,collections
import types, re, sys
from KeyInfo import KeyInfo
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
            'additionalComments': self._additionalComments,
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
            'setUp_section': self._setUp_section,
            'groupSummary': self._groupSummary,
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

    ################# Segment Level

    ################# Section Level

    def _timeBullet(self, sectionDict):
        endText = ''
        timeBullet = super(Format, self)._timeBullet(sectionDict)
        hazards = sectionDict.get('hazardEvents')
        # Assume all hazards in the section have the same geoType
        if hazards[0].get('geoType', '') == 'area':
            endText += '\n'
        return self._getFormattedText(timeBullet, endText=endText)

    def _basisBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getVal('basisBullet', sectionDict)
        if bulletText is None:
            vtecRecord = sectionDict.get('vtecRecord')
            act = vtecRecord.get('act')
            if act == 'COR':
                act = vtecRecord.get('prevAct')
            phen = vtecRecord.get("phen")
            sig = vtecRecord.get('sig')
            bulletText = ''
            # Add startTime only for warnings
            if sig == 'W':
                if self.timezones:
                    # use first time zone in the list
                    bulletText += 'At ' + self._tpc.formatDatetime(self._issueTime, '%l%M %p %Z', self.timezones[0]).strip()

            # Use basisFromHazardEvent for WarnGen only hazards
            if phen == 'FA' and sig in ['W', 'Y']:
                hazardType = phen + '.' + sig
                # FFW_FFS sections will only contain one hazard
                hazards = sectionDict.get('hazardEvents')
                basis = self.basisText.getBulletText(hazardType, hazards[0])
                basis = self._tpc.substituteParameters(hazards[0], basis)
            else:
                # TODO Need to create basisText for Non-WarnGen hazards
                basis = "...Flooding from heavy rain. This rain was located over the warned area."

            bulletText += basis
        self._setVal('basisBullet', bulletText, sectionDict, 'Basis Bullet')

        startText = '* '
        if (self._runMode == 'Practice'):
            startText += "This is a test message.  "
        return self._getFormattedText(bulletText, startText=startText, endText='\n\n')
