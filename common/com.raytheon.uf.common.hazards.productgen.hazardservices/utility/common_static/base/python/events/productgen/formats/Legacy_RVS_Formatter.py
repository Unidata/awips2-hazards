'''
    Description: Legacy formatter for FFW products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 20, 2015    5109    Chris.Cody  Initial creation
    Apr 16, 2015    7579    Robert.Blum Updates for amended Product Editor.
    May 07, 2015    6979    Robert.Blum EditableEntries are passed in for reuse.
    May 14, 2015    7376    Robert.Blum Changed to look for only None and not
                                        empty string.
    Jun 03, 2015    8530    Robert.Blum Added method for initials productPart.
'''


import datetime, collections

import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self, editableEntries) :
        self.initProductPartMethodMapping()
        super(Format, self).initialize(editableEntries)

    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
            'setUp_segment': self._setUp_segment,
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'productHeader': self._productHeader,
            'headlineStatement': self._headlineStatement,
            'narrativeInformation': self._narrativeInformation,
            'floodPointTable': self._floodPointTable,
            'initials': self._initials,
        }

    def execute(self, productDict, editableEntries=None):
        self.productDict = productDict
        self.initialize(editableEntries)
        self.timezones = productDict['timezones']
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _headlineStatement(self, productDict):
        # Get saved value from productText table if available
        statement = self._getVal('headlineStatement', productDict)
        if statement is None:
            statement = productDict.get('headlineStatement', "NO HEADLINE STATEMENT")
        self._setVal('headlineStatement', statement, productDict, 'Headline Statement')
        return statement

    def _narrativeInformation(self, productDict):
        # Get saved value from productText table if available
        narrative = self._getVal('narrativeInformation', productDict)
        if narrative is None:
            narrative = productDict.get('narrativeInformation', "NO NARRATIVE INFORMATION")
        self._setVal('narrativeInformation', narrative, productDict, 'Narrative Information')
        return self._getFormattedText(narrative, endText='\n')
