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
    