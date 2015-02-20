import FormatTemplate

import types, re, sys, collections
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self) :
        super(Format, self).initialize()
        self.initProductPartMethodMapping()
        
    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'easMessage': self._easMessage,
            'productHeader': self._productHeader,
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'narrativeForecastInformation': self._narrativeForecastInformation
                                }
        
    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()
        self._editableParts = {}
        self._editableProductParts = self._getEditableParts(productDict)
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
            elif name in ['setUp_product', 'setUp_segment', 'setUp_section']:
                pass
            elif name == 'endSegment':
                partText = '\n$$\n\n' 
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
        return text
    
    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    ################# Segment Level

    ################# Section Level    
    def _narrativeForecastInformation(self, segmentDict):        
        text = ''
        narrative = segmentDict.get('narrativeForecastInformation')
        if narrative:
            text = narrative
            text += '\n\n'
        return text
    