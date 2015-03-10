'''
    Description: A Sample Twitter formatter.

    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 04, 2015    6322    Robert.Blum Initial creation 
'''

import FormatTemplate
from KeyInfo import KeyInfo
from TextProductCommon import TextProductCommon
from Bridge import Bridge
import re

class Format(FormatTemplate.Formatter):

    def initialize(self):
        self.bridge = Bridge()
        areaDict = self.bridge.getAreaDictionary()
        self._tpc = TextProductCommon()
        self._tpc.setUp(areaDict)
        self._issueTime = self.productDict.get('issueTime')
        self._runMode = self.productDict.get('runMode')
        if self._runMode == 'Practice':
            self._testMode = True
        else:
            self._testMode = False

        # Setup the Time Zones
        self.timezones = []
        segments = self.productDict.get('segments')
        for segment in segments:
            self.timezones += segment.get('timeZones')

    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()

        return [[self.createTwitterProduct()], {}]

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
            #Add break between segments
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
        endTime = sectionDict.get('endTime')
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
               attribution += ' will expire ' + timeWords + ' for ' + areaPhrase +'.'
        return attribution
    
    def createAreaPhrase(self, sectionDict):
        areaPhrase = ''
        counter = 0
        size = len(sectionDict.get('impactedAreas'))
        for area in sectionDict.get('impactedAreas'):
            areaPhrase += area['name']
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
        elements = KeyInfo.getElements('callsToAction', segmentDict)
        if len(elements) > 0:
            callsToAction = segmentDict.get(elements[0])
        else:
            callsToAction = segmentDict.get('callsToAction', None)

        if callsToAction:
            ctaText = '\n\n'
            index = 0
            size = len(callsToAction)
            for cta in callsToAction:
                cta = cta.strip('\n\t\r')
                cta = re.sub('\s+',' ',cta)
                ctaText += cta
                #Add break between CTAs
                if (index + 1 < size):
                    ctaText += '\n\n'
                index += 1
            return ctaText
        return ''

    def createOfficeTag(self, siteID):
        officeTag = ' #' + siteID
        return officeTag
