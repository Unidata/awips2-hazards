'''
    Description: Creates the attribution and firstBullet text for the FLW/FLS
    product.


    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    12/07/2016   26810      dgilling    Initial creation.

    @author David.Gillingham@noaa.gov
'''

import datetime
import AttributionFirstBulletText as AFB


class AttributionFirstBulletText(AFB.AttributionFirstBulletText):
    """Creates the attribution and first bullet text for FLW/FLS products.
    """

    def __init__(self, sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones=[]):
        super(AttributionFirstBulletText, self).__init__(
                sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones)
    
    def attribution_CAN(self):
        attribution = '...The '
        if self.replacement:
            actionWord = 'replaced'
        else:
            actionWord = 'cancelled'
        if self.geoType == 'area':
            preQualifiers = self.preQualifiers()
            attribution += preQualifiers + self.hazardName + self.qualifiers(titleCase=False, addPreposition=False)
            attribution += ' has been ' + actionWord
            if self.replacedBy:
                attribution += ' with ' + self.replacementName
            attribution += ' for ' + self.areaPhrase
        else:
            attribution += self.hazardName + ' for ' + self.areaPhrase + ' has been ' + actionWord
        attribution += '...'
        return attribution

    def attribution_EXP(self):
        if self.vtecRecord.get('endTime') <= self.issueTime:
            expireWords = ' has expired'
        else:
            timeWords = self.tpc.getTimingPhrase(self.vtecRecord, self.hazardEventDicts, self.issueTime)
            expireWords = ' will expire ' + timeWords
        attribution = '...The '
        if self.geoType == 'area':
            preQualifiers = self.preQualifiers()
            attribution += preQualifiers + self.hazardName + self.qualifiers(titleCase=False, addPreposition=False)
            attribution += expireWords + ' for ' + self.areaPhrase
        else:
            attribution += self.hazardName + ' for ' + self.areaPhrase + expireWords + '.'
        return attribution

    def attribution_UPG(self):
        assert False, \
            'UPG is an invalid action code for the FLW/FLS product.'
        return ''

    def attribution_NEW(self):
        attribution = self.nwsPhrase + 'issued a'
        return attribution

    def attribution_EXB(self):
        assert False, \
            'EXB is an invalid action code for the FLW/FLS product.'
        return ''
 
    def attribution_EXA(self):
        assert False, \
            'EXA is an invalid action code for the FLW/FLS product.'
        return ''
    
    def attribution_EXT(self):
        if self.geoType == 'area': # FFW_FFS, FLW_FLS area, FFA area:
            attribution = self.nwsPhrase + 'extended the'
        else: # point
            attribution = 'The ' + self.hazardName + ' is now in effect ' 
        return attribution
    
    def attribution_CON(self):
        attribution = '...The '
        if self.geoType == 'area':
            endTime = datetime.datetime.utcfromtimestamp((self.endTime/1000.0))
            issueTime = datetime.datetime.utcfromtimestamp((self.issueTime/1000.0))
            duration = endTime - issueTime
            timeStr = self._headlineExpireTimePhrase(endTime, duration, self.timeZones)
            preQualifiers = self.preQualifiers()
            qualifiers = self.qualifiers(titleCase=False, addPreposition=False)
            attribution += preQualifiers + self.hazardName + qualifiers
            attribution += ' remains in effect until ' + timeStr + ' for ' + self.areaPhrase
        else:
            attribution += self.hazardName + ' remains in effect'
        attribution += '...'
        return attribution

    def attribution_ROU(self):
        return self.nwsPhrase + 'released '

    def firstBullet_CAN(self):
        if self.geoType == 'area':
            firstBullet = ''
        else:
            actionValue = ''
            if self.replacement:
                actionWord = 'replaced'
                replacedByValue = self.hazardEventDict.get('replacedBy')
                if replacedByValue:
                    actionValue = ' by a ' + replacedByValue.lower()
            else:
                actionWord = 'cancelled'
            firstBullet = 'The ' + self.hazardName + ' is ' + actionWord + actionValue + ' for\n  ' + self.areaPhrase + '.'
        return firstBullet
    
    def firstBullet_EXP(self):
        return ''

    def firstBullet_UPG(self):
        assert False, \
            'UPG is an invalid action code for the FLW/FLS product.'
        return ''

    def firstBullet_NEW(self):
        preQualifiers = self.preQualifiers()
        qualifiers = self.qualifiers(titleCase=True)        
        forStr = ''
        if self.geoType == 'area':
            if not self.optionalSpecificTypeStr and not self.warningTypeStr:
                forStr = ' for...'
        else:
            forStr =  ' for\n  '
        if qualifiers:
            forStr = ''

        firstBullet = '{}{}{}{}{}'.format(preQualifiers, self.hazardName, qualifiers, forStr, self.areaPhrase)
        if firstBullet[-1] != '.':
            firstBullet += '.'
        return firstBullet

    def firstBullet_EXB(self):
        assert False, \
            'EXB is an invalid action code for the FLW/FLS product.'
        return ''
 
    def firstBullet_EXA(self):
        assert False, \
            'EXA is an invalid action code for the FLW/FLS product.'
        return ''
    
    def firstBullet_EXT(self):
        firstBulletFormat = '{} {}{}{}'
        
        hazardPhrase = ''
        forStr = ''
        if self.geoType == 'area':
            qualifiers = self.preQualifiers()
            hazardPhrase = qualifiers + self.hazardName
            if not self.optionalSpecificTypeStr and not self.warningTypeStr:
                forStr = 'for...'
            else:
                forStr = 'for '
        else:
            hazardPhrase = 'The ' + self.hazardName
            forStr =  'continues for\n  '
        qualifiers = self.qualifiers(titleCase=True)
        if qualifiers:
            forStr = ''
        
        firstBullet = firstBulletFormat.format(hazardPhrase, forStr, qualifiers, self.areaPhrase)
        if firstBullet[-1] != '.':
            firstBullet += '.'
        return firstBullet
    
    def firstBullet_CON(self):
        firstBulletFormat = '{} {}{}{}'
        
        hazardPhrase = ''
        forStr = ''
        if self.geoType == 'area':
            qualifiers = self.preQualifiers()
            hazardPhrase = qualifiers + self.hazardName
            forStr = 'for '
        else:
            hazardPhrase = 'The ' + self.hazardName
            forStr = 'continues for\n  '
        qualifiers = self.qualifiers(titleCase=True, addPreposition=False)
        firstBullet = firstBulletFormat.format(hazardPhrase, forStr, qualifiers, self.areaPhrase)
        if firstBullet[-1] != '.':
            firstBullet += '.'
        return firstBullet

    def firstBullet_ROU(self):
        if self.geoType == 'area':
            forStr = 'for...'
        else:
            forStr = 'for\n  '
        return '{} {}{}.'.format(self.hazardName, forStr, self.areaPhrase)

    def getAreaPhrase(self):
        if self.phenSig in ['FA.Y', 'FA.W']:
            if self.action in ['NEW', 'EXT']:
                areaPhrase = self._getAreaPhraseBullet()
            else:
                areaPhrase = self.tpc.getAreaPhrase(self.vtecRecord.get('id'))
        elif self.phen in 'FL' or self.phenSig == 'HY.S':
            areaPhrase = self._getAreaPhraseForPoints(self.hazardEventDict)
        else:
            areaPhrase = ''
        return areaPhrase.rstrip()

    def preQualifiers(self,):
        qualifiers = ''
        if self.phenSig == 'FA.Y':
            if self.advisoryType:
                qualifiers += self._titleCase(self.advisoryType) + ' '
        return qualifiers

    def qualifiers(self, titleCase, addPreposition=True):
        qualifiers = ''
        if titleCase:
            typeOfFlooding = self._titleCase(self.typeOfFlooding)
        else:
            typeOfFlooding = self.typeOfFlooding

        if self.phenSig == 'FA.W':
            if self.immediateCause in ['ER', 'IC', 'MC', 'UU']:
                if self.warningTypeStr:
                    qualifiers += ' for ' + self.warningTypeStr
                    if addPreposition:
                        qualifiers += ' in...'
            elif self.immediateCause == 'DM' and self.riverName and self.damOrLeveeName:
                qualifiers += '\n'
                qualifiers += self._dm_river_qualifiers()
                if addPreposition:
                    qualifiers += ' in '
            elif typeOfFlooding:
                if self.action in ['NEW', 'EXT']:
                    qualifiers += '\n  ' + typeOfFlooding
                else:
                    qualifiers += ' for ' + typeOfFlooding
                if addPreposition:
                    qualifiers += ' in...'

        elif self.phenSig == 'FA.Y': 
            if self.optionalSpecificTypeStr:
                qualifiers+= ' for ' +  self.optionalSpecificTypeStr.title()
            if self.immediateCause not in ['ER', 'IC']:
                if typeOfFlooding:
                    if self.action in ['NEW', 'EXT']:
                        if self.optionalSpecificTypeStr:
                            qualifiers += ' for..\n  ' + typeOfFlooding
                        else:
                            qualifiers += '\n  ' + typeOfFlooding
                    else:
                        qualifiers += ' for ' + typeOfFlooding
                    if addPreposition:
                        qualifiers += ' in...'
            else:
                if qualifiers and addPreposition:
                    qualifiers += ' for...'

        return qualifiers

