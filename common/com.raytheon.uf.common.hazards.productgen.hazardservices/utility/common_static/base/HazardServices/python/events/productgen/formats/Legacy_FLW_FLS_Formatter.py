'''
    Description: Legacy formatter for FLW_FLS products
'''

import datetime,collections
import types, re, sys
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
import AttributionFirstBulletText_FLW_FLS
import HazardConstants

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self, editableEntries):
        super(Format, self).initialize(editableEntries)

    def execute(self, productDict, editableEntries):
        self.productDict = productDict
        self.initialize(editableEntries)
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self.editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    ################# Segment Level
    
    def endSegment(self, segmentDict, productPart):
        # Reset to empty dictionary
        self._segmentDict = {}
        endSegmentText = '\n$$\n\n'
        if self._testMode:
            endSegmentText += 'THIS IS A TEST MESSAGE. DO NOT TAKE ACTION BASED ON THIS.\n\n'
        return endSegmentText

    ################# Section Level

    def setUp_section(self, sectionDict, productPart):
        self.attributionFirstBullet = AttributionFirstBulletText_FLW_FLS.AttributionFirstBulletText(
            sectionDict, self._productID, self._issueTime, self._testMode, self._wfoCity, self._tpc, self.timezones)
        return ''

    def timeBullet(self, sectionDict, productPart):
        endText = ''
        timeBullet = super(Format, self).timeBullet(sectionDict, productPart)
        hazards = sectionDict.get('hazardEvents')
        # Assume all hazards in the section have the same geoType
        if hazards[0].get('geoType', '') == 'area':
            endText += '\n'
        return timeBullet + endText

    def basisBullet(self, sectionDict, productPart):
        vtecRecord = sectionDict.get('vtecRecord')
        act = self._getAction(vtecRecord)
        phen = vtecRecord.get("phen")
        sig = vtecRecord.get('sig')
        bulletText = ''
        # Add startTime only for warnings and advisories.
        if sig in ['W', 'Y']:
            if self.timezones:
                # use first time zone in the list
                bulletText += 'At ' + self._tpc.formatDatetime(self._issueTime, '%l%M %p %Z', self.timezones[0]).strip()

        # Use basisFromHazardEvent for WarnGen only hazards
        if phen == 'FA' and sig in ['W', 'Y']:
            hazardType = phen + '.' + sig
            # FFW_FFS sections will only contain one hazard
            hazards = sectionDict.get('hazardEvents')
            basis = self.basisText.getBulletText(hazardType, hazards[0], vtecRecord)
            basis = self._tpc.substituteParameters(hazards[0], basis)
        else:
            # TODO Need to create basisText for Non-WarnGen hazards
            basis = "Flooding from heavy rain. This rain was located over the warned area."

        bulletText += ', ' + basis
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)

        if act in ['NEW', 'EXT']:
            startText = '* '
        else:
            startText = ''
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self.getFormattedText(productPart, startText=startText, endText='\n\n')
