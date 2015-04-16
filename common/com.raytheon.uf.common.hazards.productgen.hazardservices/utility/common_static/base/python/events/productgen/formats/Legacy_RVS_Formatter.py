'''
    Description: Legacy formatter for FFW products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 20, 2015    5109    Chris.Cody  Initial creation
    Apr 16, 2015    7579    Robert.Blum Updates for amended Product Editor.
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
        self.timezones = productDict['timezones']
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _headlineStatement(self, productDict):
        # Get saved value from productText table if available
        statement = self._getSavedVal('headlineStatement', productDict)
        if not statement:
            statement = productDict.get('headlineStatement', "NO HEADLINE STATEMENT")
        self._setVal('headlineStatement', statement, productDict, 'Headline Statement')
        return statement

    def _narrativeInformation(self, productDict):
        # Get saved value from productText table if available
        narrative = self._getSavedVal('narrativeInformation', productDict)
        if not narrative:
            narrative = productDict.get('narrativeInformation', "NO NARRATIVE INFORMATION")
        self._setVal('narrativeInformation', narrative, productDict, 'Narrative Information')
        return narrative


