"""Module containing methods to build the attribution and first bullet text for FFW/FFS products.

    @author David.Gillingham@noaa.gov
"""

import AttributionFirstBulletText as AFB


class AttributionFirstBulletText(AFB.AttributionFirstBulletText):
    """Creates the attribution and first bullet text for FFW/FFS products.
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
        attribution += self.hazardName + ' for ' + self.areaPhrase + ' has been ' + actionWord + '...'
        return attribution

    def attribution_EXP(self):
        expireTimeCurrent = self.issueTime
        if self.vtecRecord.get('endTime') <= expireTimeCurrent:
            expireWords = ' has expired'
        else:
            timeWords = self.tpc.getTimingPhrase(self.vtecRecord, self.hazardEventDicts, expireTimeCurrent)
            expireWords = ' will expire ' + timeWords
        attribution = '...The '
        attribution += self.hazardName + ' for ' + self.areaPhrase + expireWords + '.'
        return attribution

    def attribution_UPG(self):
        assert False, \
            'UPG is an invalid action code for the FFW/FFS product.'
        return ''

    def attribution_NEW(self):
        return self.nwsPhrase + 'issued a'

    def attribution_EXB(self):
        assert False, \
            'EXB is an invalid action code for the FFW/FFS product.'
        return ''
 
    def attribution_EXA(self):
        assert False, \
            'EXA is an invalid action code for the FFW/FFS product.'
        return ''
    
    def attribution_EXT(self):
        return self.nwsPhrase + 'extended the'
    
    def attribution_CON(self):
        attribution = '...The '
        attribution += self.hazardName + ' remains in effect...'
        return attribution

    def attribution_ROU(self):
        assert False, \
            'ROU is an invalid action code for the FFW/FFS product.'
        return ''

    def firstBullet_CAN(self):
        return ''
    
    def firstBullet_EXP(self):
        return ''

    def firstBullet_UPG(self):
        assert False, \
            'UPG is an invalid action code for the FFW/FFS product.'
        return ''

    def firstBullet_NEW(self):
        firstBullet = self.hazardName
        qualifiers = self.qualifiers(titleCase=True)
        if not self.optionalSpecificTypeStr and not self.warningTypeStr:
            firstBullet += ' for...'
        if qualifiers:
            firstBullet += qualifiers
        firstBullet += self.areaPhrase
        return firstBullet

    def firstBullet_EXB(self):
        assert False, \
            'EXB is an invalid action code for the FFW/FFS product.'
        return ''
 
    def firstBullet_EXA(self):
        assert False, \
            'EXA is an invalid action code for the FFW/FFS product.'
        return ''
    
    def firstBullet_EXT(self):
        firstBullet = self.hazardName
        if not self.optionalSpecificTypeStr and not self.warningTypeStr:
            firstBullet += ' for...'
        qualifiers = self.qualifiers(titleCase=True)
        if qualifiers:
            firstBullet += qualifiers
        firstBullet += self.areaPhrase
        return firstBullet
    
    def firstBullet_CON(self):
        firstBullet = self.hazardName
        if self.phenSig == 'FF.W':
            firstBullet += ' for...'
        qualifiers = self.qualifiers(titleCase=True, addPreposition=False)
        if qualifiers:
            firstBullet += qualifiers
        firstBullet += self.areaPhrase
        return firstBullet

    def firstBullet_ROU(self):
        assert False, \
            'ROU is an invalid action code for the FFW/FFS product.'
        return ''

    def getAreaPhrase(self):
        if self.productID == 'FFW':
            areaPhrase = self._getAreaPhraseBullet()
        else:
            areaPhrase = self.tpc.getAreaPhrase(self.vtecRecord.get('id'))
        return areaPhrase.rstrip()

    def qualifiers(self, titleCase, addPreposition=True):
        qualifiers = ''
        if titleCase:
            typeOfFlooding = self._titleCase(self.typeOfFlooding)
        else:
            typeOfFlooding = self.typeOfFlooding

        if self.phenSig == 'FF.W':
            if self.immediateCause == 'DM' and self.riverName and self.damOrLeveeName:
                qualifiers += '\n'
                qualifiers += self.dm_river_qualifiers()

                if addPreposition:
                    qualifiers += ' in '
            elif self.subType == 'BurnScar' and self.burnScarName:
                qualifiers += '\n' + self.burnScarName
                if addPreposition:
                    qualifiers += ' in ' 
            elif typeOfFlooding and self.immediateCause not in ['ER', 'IC', 'MC', 'UU']:
                qualifiers += '\n' + typeOfFlooding
                if addPreposition:
                    qualifiers += ' in...'
        
        return qualifiers

