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
            elif name in ['setUp_product']:
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
                if part in self._editableProductParts:
                    self._editableParts[part] = partText
                    
        return text

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _headlineStatement(self, productDict):
        return productDict.get('headlineStatement', "NO HEADLINE STATEMENT")

    def _narrativeInformation(self, productDict):
        return productDict.get('narrativeInformation', "NO NARRATIVE INFORMATION")

