'''
    Description: Legacy formatter for FFW products
'''

import datetime, collections

import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
import AttributionFirstBulletText_FFW_FFS
import HazardConstants

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self, editableEntries) :
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
    def easMessage(self, productDict, productPart):
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

    def setUp_section(self, sectionDict, productPart):
        self.attributionFirstBullet = AttributionFirstBulletText_FFW_FFS.AttributionFirstBulletText(
            sectionDict, self._productID, self._issueTime, self._testMode, self._wfoCity, self._tpc, self.timezones)
        return ''

    def timeBullet(self, sectionDict, productPart):
        bulletText = super(Format, self).timeBullet(sectionDict, productPart)
        return bulletText + '\n'

    def basisBullet(self, sectionDict, productPart):
        vtecRecord = sectionDict.get('vtecRecord', {})
        action = vtecRecord.get('act', '')
        if action == 'COR':
            action = vtecRecord.get('prevAct', '')

        bulletText = ''
        phen = vtecRecord.get('phen')
        sig = vtecRecord.get('sig')
        hazards = sectionDict.get('hazardEvents')
        # FFW_FFS sections will only contain one hazard
        subType = hazards[0].get('subType')
        hazardType = phen + '.' + sig + '.' + subType
        basis = self.basisText.getBulletText(hazardType, hazards[0], vtecRecord)
        damOrLeveeName = hazards[0].get('damOrLeveeName')
        if '|* riverName *|' in basis and damOrLeveeName:
            # replace the riverName with the one from DamMetaData.py
            damInfo = self._damInfo(damOrLeveeName)
            if damInfo:
                riverName = damInfo.get('riverName')
                if riverName:
                    basis = basis.replace('|* riverName *|', riverName)
        basis = self._tpc.substituteParameters(hazards[0], basis)
        if basis is None :
             basis = 'Flash Flooding was reported'

        # Create basis statement
        eventTime = vtecRecord.get('startTime')
        eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z ', stripLeading=True, timeZones=self.timezones)
        bulletText += 'At ' + eventTime.rstrip() + ', ' + basis

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(bulletText)

        startText = ''
        if action in ['NEW', 'EXT']:
            startText = '* '
        if self._testMode:
            startText += "THIS IS A TEST MESSAGE. "
        return self.getFormattedText(productPart, startText=startText, endText='\n\n')


    def callsToAction_sectionLevel(self, sectionDict, productPart):
        callsToAction =  self._tpc.getVal(sectionDict, HazardConstants.CALLS_TO_ACTION, '')
        if callsToAction and callsToAction != '':
            callsToAction = callsToAction.rstrip()

        # This logic may end up being fairly brittle, but here we change the
        # wording for certain CTAs depending on certain choices made for
        # burn scar warnings.
        burnScarName = None
        try :
            burnScarName = sectionDict['hazardEvents'][0]['burnScarName']
            isEmergency = 'ffwEmergency' in sectionDict['hazardEvents'][0]['include']
        except :
            isEmergency = False

        if isEmergency :
            srchstr = "act quickly to protect your life"
            i = callsToAction.lower().find(srchstr)
            if i > 0 :
                i = callsToAction.rfind("\n", 0, i)
                if i<0 :
                    i = 0
                callsToAction = callsToAction[:i]+\
                  "Move to higher ground now. This is an extremely dangerous and life "+\
                  "threatening situation. Do not attempt to travel unless you are "+\
                  "fleeing an area subject to flooding or under an evacuation order.\n\n"+\
                  callsToAction[i:]

        # tagstr must match same in MetaData_FF_W_BurnScar:ctaBurnScarScenario()
        tagstr = "|*Burn-Scar*|"
        i = callsToAction.find(tagstr) 
        if i>0 and isinstance(burnScarName, str) :
            from MapsDatabaseAccessor import MapsDatabaseAccessor
            mapsAccessor = MapsDatabaseAccessor()
            burnScarMetaData = \
              mapsAccessor.getBurnScarMetadata(burnScarName)
            try :
                scenario =  sectionDict['hazardEvents'][0]['scenario']
                burnCTA = burnScarMetaData["scenarios"][scenario]["burnCTA"]
                i1 = callsToAction.rfind("\n", 0, i)
                i2 = callsToAction.find("\n", i)
                if i1>=0 :
                    burnCTA = callsToAction[:i1+1]+burnCTA
                if i2>0 :
                    callsToAction = burnCTA+callsToAction[i2:]
                else :
                    callsToAction = burnCTA
                i = -1
            except :
                pass
            if i>=0 :
                i2 = i+len(tagstr)
                callsToAction = callsToAction[:i]+burnScarName+callsToAction[i2:]

        # Update the Product Part with the generated Text
        productPart.setGeneratedText(callsToAction)
        return self.getFormattedText(productPart, startText='PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n', endText='\n\n&&\n\n')
