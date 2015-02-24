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
'''

import datetime, collections

import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self) :
        self.initProductPartMethodMapping()
        super(Format, self).initialize()

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
            'callsToAction': self._callsToAction,
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
            'rainFallStatement': self._rainFallStatement,
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
            'groupSummary': self._groupSummary,
            'endSegment': self._endSegment,
                                }

    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()

        self._editableProductParts = self._getEditableParts(productDict)
        self._editableParts = {}
        legacyText = self._createTextProduct()
        return [[ProductUtils.wrapLegacy(legacyText)],self._editableParts]

    def _processProductParts(self, productDict, productParts, skipParts=[]):
        
        text = ''
        if type(productParts) is collections.OrderedDict:
            arguments = productParts.get('arguments')
            partsList = productParts.get('partsList')
        else:
            partsList = productParts

        for part in partsList:
            valtype = type(part)
            if valtype is types.StringType:
                name = part
            elif valtype is types.TupleType or valtype is types.ListType:
                name = part[0]
                infoDicts = part[1]

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print 'Legacy Part:', name, ': ', 

            partText = ''
            if name in self.productPartMethodMapping:
                partText = self.productPartMethodMapping[name](productDict)
            elif name in ['setUp_product', 'setUp_section']:
                pass
            elif name == 'CR':
                partText = '\n'
            elif name in ['segments', 'sections']:
                partText = self.processSubParts(productDict.get(name), infoDicts)
            else:
                textStr = self._tpc.getVal(productDict, name)
                if textStr:
                    partText = textStr + '\n' 

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print partText

            if partText is not None:
                text += partText
                if part in self._editableProductParts:
                    self._editableParts[part] = partText
                    
        return text


    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    ################# Segment Level

    ################# Section Level
    
    def _attribution(self, segmentDict):
        return self.attributionFirstBullet.getAttributionText()

    def _attribution_point(self, segmentDict):
        return self.attributionFirstBullet.getAttributionText()

    def _firstBullet(self, segmentDict):
        return '* '+self.attributionFirstBullet.getFirstBulletText()

    def _firstBullet_point(self, segmentDict):
        return self.attributionFirstBullet.getFirstBulletText()

    def _timeBullet(self, segmentDict):
        timeBullet = super(Format, self)._timeBullet(segmentDict)
        if segmentDict.get('geoType', '') == 'area':
            timeBullet+= '\n'
        return timeBullet

    def _basisBullet(self, sectionDict):
        vtecRecord = sectionDict.get('vtecRecord')
        startTime = sectionDict.get('startTime')
        act = vtecRecord.get('act')
        bulletText = ''

        if act in ['NEW', 'CON','ROU', 'EXT']:
            bulletText += '* '

        if (self._runMode == 'Practice'):
            bulletText += "This is a test message.  "

        if self.timezones:
            # use first time zone in the list
            bulletText += 'At ' + self._tpc.formatDatetime(startTime, '%l%M %p %Z', self.timezones[0]).strip()

        basisStatement = sectionDict.get('basisStatement')
        if basisStatement:
            bulletText += ' ' + basisStatement + ' '
        else:
            bulletText += ' |* current hydrometeorological basis *| '

        bulletText += '\n\n'
        return bulletText

