'''
    Description: Legacy formatter for RVS products
'''

import datetime, collections

import types, re, sys
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self, editableEntries):
        super(Format, self).initialize(editableEntries)

    def execute(self, productDict, editableEntries):
        self.productDict = productDict
        self.initialize(editableEntries)
        self.timezones = productDict['timezones']
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self.editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def headlineStatement(self, productDict, productPart):
        # Update the Product Part with the generated Text
        productPart.setGeneratedText("|* Enter Headline Statement *|")
        return self.getFormattedText(productPart, endText='\n')

    def narrativeInformation(self, productDict, productPart):
        # Update the Product Part with the generated Text
        productPart.setGeneratedText("|* Enter Narrative Information *|")
        return self.getFormattedText(productPart, endText='\n')
