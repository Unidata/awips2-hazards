'''
    Description: A Sample Twitter formatter.
'''

import FormatTemplate
from TextProductCommon import TextProductCommon
from Bridge import Bridge
import re
from collections import OrderedDict

class Format(FormatTemplate.Formatter):

    def initialize(self, editableEntries):
        self.bridge = Bridge()
        areaDict = self.bridge.getAreaDictionary()
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)
        self._issueTime = self.productDict.get('issueTime')
        self._runMode = self.productDict.get('runMode')
        self._testMode = self._runMode in ['Practice', 'Test']

        # List that will hold the Product Part entries to be 
        # displayed in the Product Editor. 
        # Since this is just a sample format, no editableParts
        # are defined.
        if editableEntries:
            self.editableParts = editableEntries
        else:
            self.editableParts = []

        # Setup the Time Zones
        self.timezones = []
        segments = self.productDict.get('segments')
        for segment in segments:
            self.timezones += segment.get('timeZones')

    def execute(self, productDict, editableEntries):
        self.productDict = productDict
        self.initialize(editableEntries)
        product = self.createTwitterProduct()
        return [product], self.editableParts

    def createTwitterProduct(self):
        text = ''
        segments = self.productDict['segments']
        size = len(segments)
        index = 0
        for segment in segments:
            sections = segment.get('sections')
            for section in sections:
                text += self.createAttribution(section)
            # CTAs are segment level
            text += self.addCTAs(segment)
            # Add break between segments
            if (index + 1 < size):
                text += '\n\n'
            index += 1

        text += self.createOfficeTag(self.productDict.get('siteID'))
        return self._tpc.linebreak(text, 69)

    def createAttribution(self, sectionDict):
        vtecRecord = sectionDict.get('vtecRecord')
        hazName = self._tpc.hazardName(vtecRecord.get('hdln'), self._testMode, False)
        action = vtecRecord.get('act')
        if action == 'COR':
            action = vtecRecord.get('prevAct')
        # Endtime for all hazards in the section will be the same.
        hazard = sectionDict.get('hazardEvents')[0]
        endTime = hazard.get('endTime')
        endTimePhrase = ' until ' + endTime.strftime('%l%M %p %Z').strip() + '.'
        areaPhrase = self.createAreaPhrase(sectionDict)
        attribution = hazName

        if action == 'NEW':
            attribution += ' issued for ' + areaPhrase + endTimePhrase
        elif action == 'CON':
            attribution += ' remains in effect for ' + areaPhrase + endTimePhrase
        elif action == 'EXT':
            attribution += ' has been extended for ' + areaPhrase + endTimePhrase
        elif action in ['EXA', 'EXB']:
            attribution += ' has been expanded to include ' + areaPhrase + endTimePhrase
        elif action == 'CAN':
            attribution += ' has been cancelled for ' + areaPhrase + '.'
        elif action == 'EXP':
            expTimeCurrent = self._issueTime
            if vtecRecord.get('endTime') <= expTimeCurrent:
                attribution += ' has expired for ' + areaPhrase + '.'
            else:
               timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
               attribution += ' will expire ' + timeWords + ' for ' + areaPhrase + '.'
        return attribution
    
    def createAreaPhrase(self, sectionDict):
        areaPhrase = ''
        vtecRecord = sectionDict.get('vtecRecord')
        counter = 0
        size = len(vtecRecord.get('id', []))
        for ugc in vtecRecord.get('id', []):
            areaPhrase += self._tpc.getInformationForUGC(ugc, 'entityName')
            if size > 1:
                if counter < size - 2:
                    areaPhrase += ', '
                elif counter < size - 1:
                    areaPhrase += ' and '
            counter += 1
          
        if size > 1:
             areaPhrase += ' counties'
        else:
            areaPhrase += ' county'
        return areaPhrase

    def addCTAs(self, segmentDict):
        callsToAction = self._tpc.getVal(segmentDict, 'callsToAction', None)

        if callsToAction and callsToAction != '':
            ctaText = '\n\n'
            ctaText += callsToAction.rstrip()
            return ctaText
        return ''

    def createOfficeTag(self, siteID):
        officeTag = ' #' + siteID
        return officeTag
