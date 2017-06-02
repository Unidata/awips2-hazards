'''
    Description: Formatter for Probabilistic Products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Dec 2015     13457      Tracy Hansen  Initial creation

'''


import datetime, collections
from collections import OrderedDict
import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format:

    def initialize(self, editableEntries) :
        self.initProductPartMethodMapping()
        super(Format, self).initialize(editableEntries)

    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
#             'setUp_segment': self._setUp_segment,
#             'wmoHeader': self._wmoHeader,
#             'ugcHeader': self._ugcHeader,
#             'productHeader': self._productHeader,
#             'headlineStatement': self._headlineStatement,
#             'narrativeInformation': self._narrativeInformation,
#             'floodPointTable': self._floodPointTable,
#             'initials': self._initials,
        }

    def execute(self, productDict, editableEntries=None, overrideProductText=None):
        self.productDict = productDict
        #self.initialize(editableEntries)
        #self.timezones = productDict['timezones']
        legacyText = productDict.get('productText', "Here is an example product")
        self._editableParts = OrderedDict()
        #legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _headlineStatement(self, productDict):
        # Get saved value from productText table if available
        statement = self._getVal('headlineStatement', productDict)
        if statement is None:
            statement = self._tpc.getValueOrFramedText('headlineStatement', productDict, 'Enter Headline Statement')
        self._setVal('headlineStatement', statement, productDict, 'Headline Statement')
        return statement

    def _narrativeInformation(self, productDict):
        # Get saved value from productText table if available
        narrative = self._getVal('narrativeInformation', productDict)
        if narrative is None:
            narrative = self._tpc.getValueOrFramedText('narrativeInformation', productDict, 'Enter Narrative Information')
        self._setVal('narrativeInformation', narrative, productDict, 'Narrative Information')
        return self._getFormattedText(narrative, endText='\n')
