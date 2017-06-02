'''
    Description: Creates the attribution and firstBullet text for the FFA product.


    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    05/26/2016   19080      dgilling    Initial creation.

    @author David.Gillingham@noaa.gov
'''

import AttributionFirstBulletText


class AttributionFirstBulletText(AttributionFirstBulletText.AttributionFirstBulletText):

    def __init__(self, sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones=[], areaPhrase=None):
        super(AttributionFirstBulletText, self).__init__(
                sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones, areaPhrase)
    
    def attribution_CAN(self):
        if self.geoType == 'area':
            attributionFormat = 'The {} for {} has been cancelled.'
        else:
            attributionFormat = 'The {} is cancelled for\n{}'
        return attributionFormat.format(self.hazardName, self.areaPhrase)

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
            attribution = 'The {} has expired for\n{}'.format(self.hazardName, self.areaPhrase)
        return attribution

    def attribution_UPG(self):
        assert False, \
            'UPG is an invalid action code for the FFA product.'
        return ''
    
    def attribution_EXB(self):
        assert self.geoType != 'point', \
            'Point-typed hazard events can\'t issue EXBs for the FFA product.'
        return super(AttributionFirstBulletText, self).attribution_EXB()
 
    def attribution_EXA(self):
        assert self.geoType != 'point', \
            'Point-typed hazard events can\'t issue EXAs for the FFA product.'
        return super(AttributionFirstBulletText, self).attribution_EXA()
    
    def attribution_EXT(self):
        if self.geoType == 'area':
            attribution = 'The {} is now in effect for'.format(self.hazardName)
        else:
            attribution = 'The {} continues for\n{}'.format(self.hazardName, self.areaPhrase)
        return attribution
    
    def attribution_CON(self):
        if self.geoType == 'area':
            attribution = 'The {} continues for'.format(self.hazardName)
        else:
            attribution = 'The {} continues for\n{}'.format(self.hazardName, self.areaPhrase)
        return attribution

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
            forStr = ' for\n'
        if qualifiers:
            firstBullet += qualifiers
            forStr = ''
        firstBullet += forStr + self.areaPhrase
        if self.geoType == 'area':
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

    def qualifiers(self, addPreposition=True):
        qualifiers = ''
        if self.phenSig in ['FF.A', 'FA.A']:
            if self.immediateCause == 'DM':
                if self.riverName and self.damOrLeveeName:
                    qualifiers += ' for...\n'
                    qualifiers += self.dm_river_qualifiers()
                    if addPreposition:
                        qualifiers += ' in '

        return qualifiers

