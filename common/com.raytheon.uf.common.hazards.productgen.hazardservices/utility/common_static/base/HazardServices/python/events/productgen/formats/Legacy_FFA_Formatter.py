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
    Apr 30, 2015 7579       Robert.Blum Changes for multiple hazards per section.
    May 07, 2015 6979       Robert.Blum EditableEntries are passed in for reuse.
    May 14, 2015 7376       Robert.Blum Changed to look for only None and not
                                        empty string.
    Jun 03, 2015 8530       Robert.Blum Added method for new initials productPart.
    Nov 09, 2015 7532       Robert.Blum CTAs are now section level for FA.A and FF.A.
    Feb 03, 2016 14983      Kevin.Bisanz Added time description to time bullet in _timeBullet(...).
    Feb 10, 2016 14797      Kevin.Bisanz Added _impactsBullet(...) to populate the product editor.
    Mar 21, 2016 15640      Robert.Blum Fixed custom edits not getting put in final product.
    May 11, 2016 16914      Robert.Blum Removed rainfall statement from FL.A hazards.
    May 24, 2016 19147      dgilling    Use correct functions to create time bullet.
    May 27, 2016 19080      dgilling    Use FFAAttributionFirstBulletText to generate
                                        attribution phrase and first bullet.
    Jun 07, 2016 19135      dgilling    Add missing test message disclaimers.
    Jun 20, 2016 19135      Robert.Blum Removed Test message text from bullets.
    Jul 29, 2016 19473      Roger.Ferrel Include test messages when in Test mode.

'''

import datetime, collections

import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
import FFAAttributionFirstBulletText

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
            'callsToAction_sectionLevel': self._callsToAction_sectionLevel,
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
            'endSection': self._endSection,
            'groupSummary': self._groupSummary,
            'endSegment': self._endSegment,
            'initials': self._initials,
                                }

    def execute(self, productDict, editableEntries, overrideProductText):
        self.overrideProductText = overrideProductText
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

    def _setUp_section(self, sectionDict):
        self.attributionFirstBullet = FFAAttributionFirstBulletText.AttributionFirstBulletText(
            sectionDict, self._productID, self._issueTime, self._testMode, self._wfoCity, self._tpc, self.timezones)
        return ''

    def _firstBullet(self, sectionDict):
        firstBullet = self.attributionFirstBullet.getFirstBulletText()
        return self._getFormattedText(firstBullet, startText='* ', endText='\n\n')

    def _timeBullet(self, sectionDict):
        hazards = sectionDict.get('hazardEvents', [])
        vtecRecord = sectionDict.get('vtecRecord')
        if self._tpc.isRiverProHazard(hazards[0]):
            bulletText = self.getRiverProTimeBullet(hazards[0], vtecRecord)
        else:
            bulletText = self._tpc.hazardTimePhrases(vtecRecord, hazards, self._issueTime, False)
        bulletText = self._tpc.substituteBulletedText(bulletText, "Time is missing", "DefaultOnly")
        endText = '\n'
        # All hazards in the section should have the same geoType
        if hazards[0].get('geoType', '') == 'area':
            endText += '\n'
        return self._getFormattedText(bulletText, endText=endText)

    def _basisBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getVal('basisBullet', sectionDict)
        if bulletText is None:
            bulletText = ''
            # Could be multiple events - combine the basisStatements from the HID
            hazards = sectionDict.get('hazardEvents')
            basisStatements = []
            for hazard in hazards:
                basisStatement = hazard.get('basisStatement', None)
                if basisStatement:
                    basisStatements.append(basisStatement)
            if len(basisStatements) > 0:
                bulletText += '\n'.join(basisStatements)
            else:
                bulletText += '|* current hydrometeorological basis *|'
        self._setVal('basisBullet', bulletText, sectionDict, 'Basis Bullet')

        return self._getFormattedText(bulletText, startText='* ', endText='\n\n')

    def _impactsBullet(self, sectionDict):
        # Get saved value from productText table if available
        bulletText = self._getVal('impactsBullet', sectionDict)

        if bulletText is None:
            bulletText = ''
            # Could be multiple events - combine the impactsStatements from the HID
            hazards = sectionDict.get('hazardEvents')
            impactsStatements = []
            for hazard in hazards:
                impactsStatement = hazard.get('impactsStatement', None)
                if impactsStatement:
                    impactsStatements.append(impactsStatement)
            if len(impactsStatements) > 0:
                bulletText += '\n'.join(impactsStatements)
            else:
                bulletText += '|* current hydrometeorological impacts *|'
        self._setVal('impactsBullet', bulletText, sectionDict, 'Impacts Bullet')

        return self._getFormattedText(bulletText, startText='* ', endText='\n\n')

    def _callsToAction_sectionLevel(self, sectionDict):
        sectionCTAs = super(Format, self)._callsToAction_sectionLevel(sectionDict)
        if self._testMode:
            sectionCTAs += 'THIS IS A TEST MESSAGE. DO NOT TAKE ACTION BASED ON THIS TEST MESSAGE.\n'
        return sectionCTAs
