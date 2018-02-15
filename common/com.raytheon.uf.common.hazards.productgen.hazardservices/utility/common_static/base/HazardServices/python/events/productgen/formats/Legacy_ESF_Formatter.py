'''
    Description: Legacy formatter for hydro ESF products
'''
import FormatTemplate

import types, re, sys, collections
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self, editableEntries):
        super(Format, self).initialize(editableEntries)

    def execute(self, productDict, editableEntries):
        self.productDict = productDict
        self.initialize(editableEntries)
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self.editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    ################# Segment Level

    ################# Section Level    
    def narrativeForecastInformation(self, sectionDict, productPart):
        narrative = '''|*
 Headline defining the type of flooding being addressed 
      (e.g., flash flooding, main stem
      river flooding, snow melt flooding)

 Area covered
 
 Possible timing of the event
 
 Relevant factors 
         (e.g., synoptic conditions, 
         quantitative precipitation forecasts (QPF), or
         soil conditions)
         
 Definition of an outlook (tailored to the specific situation)
 
 A closing statement indicating when additional information will be provided.
 *|'''
        # Update the Product Part with the generated Text
        productPart.setGeneratedText(narrative)
        return self.getFormattedText(productPart, endText='\n\n')
