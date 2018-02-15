'''
    Description: Legacy formatter for hydro FFA products
'''

import datetime, collections

import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
import AttributionFirstBulletText_FFA

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

    ################# Section Level

    def setUp_section(self, sectionDict, productPart):
        self.attributionFirstBullet = AttributionFirstBulletText_FFA.AttributionFirstBulletText(
            sectionDict, self._productID, self._issueTime, self._testMode, self._wfoCity, self._tpc, self.timezones)
        return ''

    def firstBullet(self, sectionDict, productPart):
        firstBullet = self.attributionFirstBullet.getFirstBulletText()
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(firstBullet)
        return self.getFormattedText(productPart, startText='* ', endText='\n\n')

    def timeBullet(self, sectionDict, productPart):
        hazards = sectionDict.get('hazardEvents', [])
        vtecRecord = sectionDict.get('vtecRecord')
        if self._tpc.isRiverProHazard(hazards[0]):
            bulletText = self.getRiverProTimeBullet(hazards[0], vtecRecord)
        else:
            bulletText = self._tpc.hazardTimePhrases(vtecRecord, hazards, self._issueTime, False)
        bulletText = bulletText[0].upper() + bulletText[1:]
        bulletText = self._tpc.substituteBulletedText(bulletText, "Time is missing", "DefaultOnly")
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)
        endText = '\n'
        # All hazards in the section should have the same geoType
        if hazards[0].get('geoType', '') == 'area':
            endText += '\n'
        return self.getFormattedText(productPart, endText=endText)

    def basisBullet(self, sectionDict, productPart):
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

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)
        return self.getFormattedText(productPart, startText='* ', endText='\n\n')

    def impactsBullet(self, sectionDict, productPart):
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

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)
        return self.getFormattedText(productPart, startText='* ', endText='\n\n')

    def callsToAction_sectionLevel(self, sectionDict, productPart):
        sectionCTAs = super(Format, self).callsToAction_sectionLevel(sectionDict, productPart)
        if self._testMode:
            sectionCTAs += 'THIS IS A TEST MESSAGE. DO NOT TAKE ACTION BASED ON THIS TEST MESSAGE.\n'
        return sectionCTAs
