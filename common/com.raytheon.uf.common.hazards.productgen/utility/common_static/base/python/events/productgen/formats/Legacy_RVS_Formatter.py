'''
    Description: Legacy formatter for FFW products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 20, 2015    5109    Chris.Cody  Initial creation
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
            'setUp_segment': self._setUp_segment,
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'productHeader': self._productHeader,
            'headlineStatement': self._headlineStatement,
            'narrativeInformation': self._narrativeInformation,
            'floodPointTable': self._floodPointTable,
                                }

    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()

        self._editableProductParts = self._getEditableParts(productDict)
        self._editableParts = {}
        
        self.timezones = productDict['timezones']
        
        legacyText = self._createTextProduct()
        return [[ProductUtils.wrapLegacy(legacyText)],self._editableParts]

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _headlineStatement(self, productDict):
        return productDict.get('headlineStatement', "NO HEADLINE STATEMENT")

    def _narrativeInformation(self, productDict):
        return productDict.get('narrativeInformation', "NO NARRATIVE INFORMATION")

