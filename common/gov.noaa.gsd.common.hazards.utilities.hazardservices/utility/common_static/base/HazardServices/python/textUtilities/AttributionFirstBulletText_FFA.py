"""Module containing methods to build the attribution and first bullet text for FFA products.

    @author David.Gillingham@noaa.gov
"""

import AttributionFirstBulletText as AFB


class AttributionFirstBulletText(AFB.AttributionFirstBulletText):
    """Creates the attribution and first bullet text for FFA products.
    """

    def __init__(self, sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones=[]):
        super(AttributionFirstBulletText, self).__init__(
                sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones)
    
    def attribution_CAN(self):
        if self.geoType == 'area':
            attributionFormat = 'The {} for {} has been cancelled.'
        else:
            attributionFormat = 'The {} is cancelled for\n  {}.'
        attribution = attributionFormat.format(self.hazardName, self.areaPhrase)
        if self.geoType == 'point' and self.replacement:
            # strip period from end of area phrase since we need to add more
            # information to the sentence
            attribution = attribution[:-1]
            attribution += '\n  and has been replaced'
            if self.replacedBy:
                attribution += ' with ' + self.replacementName
            attribution += '.'
        
        return attribution

    def attribution_EXP(self):
        if self.geoType == 'area':
            expireTimeCurrent = self.issueTime
            if self.vtecRecord.get('endTime') <= expireTimeCurrent:
                expireWords = ' has expired'
            else:
                timeWords = self.tpc.getTimingPhrase(self.vtecRecord, self.hazardEventDicts, expireTimeCurrent)
                expireWords = ' will expire ' + timeWords
            attribution = 'The {} for {} {}.'.format(self.hazardName, self.areaPhrase, expireWords)
        else:
            attribution = 'The {} has expired for\n  {}.'.format(self.hazardName, self.areaPhrase)
        if self.geoType == 'point' and self.replacement:
            # strip period from end of area phrase since we need to add more
            # information to the sentence
            attribution = attribution[:-1]
            attribution += '\n  and has been replaced'
            if self.replacedBy:
                attribution += ' with ' + self.replacementName
            attribution += '.'
        
        return attribution

    def attribution_UPG(self):
        assert False, \
            'UPG is an invalid action code for the FFA product.'
        return ''

    def attribution_NEW(self):
        attribution = self.nwsPhrase + 'issued a'
        return attribution

    def attribution_EXB(self):
        assert self.geoType != 'point', \
            'Point-typed hazard events can\'t issue EXBs for the FFA product.'
        return self.nwsPhrase + 'expanded the'
 
    def attribution_EXA(self):
        assert self.geoType != 'point', \
            'Point-typed hazard events can\'t issue EXAs for the FFA product.'
        return self.nwsPhrase + 'expanded the'
    
    def attribution_EXT(self):
        if self.geoType == 'area':
            attribution = 'The {} is now in effect for'.format(self.hazardName)
        else:
            attribution = 'The {} continues for\n  {}.'.format(self.hazardName, self.areaPhrase)
        return attribution
    
    def attribution_CON(self):
        if self.geoType == 'area':
            attribution = 'The {} continues for'.format(self.hazardName)
        else:
            attribution = 'The {} continues for\n  {}.'.format(self.hazardName, self.areaPhrase)
        return attribution

    def attribution_ROU(self):
        assert False, \
            'ROU is an invalid action code for the FFA product.'
        return ''

    def firstBullet_CAN(self):
        return ''
    
    def firstBullet_EXP(self):
        return ''

    def firstBullet_UPG(self):
        assert False, \
            'UPG is an invalid action code for the FFA product.'
        return ''

    def firstBullet_NEW(self):
        firstBullet = self.hazardName
        qualifiers = self.qualifiers()
        forStr = ''
        if self.geoType == 'area':
            forStr = ' for '
        else:
            forStr = ' for\n  '
        if qualifiers:
            firstBullet += qualifiers
            forStr = ''
        firstBullet += forStr + self.areaPhrase
        firstBullet += '.'
        return firstBullet

    def firstBullet_EXB(self):
        assert self.geoType != 'point', \
            'Point-typed hazard events can\'t issue EXBs for the FFA product.'
        return '{} to include {}.'.format(self.hazardName, self.areaPhrase)
 
    def firstBullet_EXA(self):
        assert self.geoType != 'point', \
            'Point-typed hazard events can\'t issue EXAs for the FFA product.'
        return '{} to include {}.'.format(self.hazardName, self.areaPhrase)
    
    def firstBullet_EXT(self):
        if self.geoType == 'area':
            firstBullet = '{}.'.format(self.areaPhrase)
        else:
            firstBullet = ''
        return firstBullet
    
    def firstBullet_CON(self):
        if self.geoType == 'area':
            firstBullet = '{}.'.format(self.areaPhrase)
        else:
            firstBullet = ''
        return firstBullet

    def firstBullet_ROU(self):
        assert False, \
            'ROU is an invalid action code for the FFA product.'
        return ''

    def getAreaPhrase(self):
        if self.geoType != 'point':
            areaPhrase = self.tpc.getAreaPhrase(self.vtecRecord.get('id'))
        else:
            areaPhrase = self._getAreaPhraseForPoints(self.hazardEventDict)
        return areaPhrase.rstrip()

    def qualifiers(self, addPreposition=True):
        qualifiers = ''
        if self.phenSig in ['FF.A', 'FA.A']:
            if self.immediateCause == 'DM':
                if self.riverName and self.damOrLeveeName:
                    qualifiers += ' for...\n'
                    qualifiers += self._dm_river_qualifiers()
                    if addPreposition:
                        qualifiers += ' in '

        return qualifiers

