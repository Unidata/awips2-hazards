'''
    Description: Legacy formatter for hydro ESF products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Apr 30, 2015    7579    Robert.Blum Changes for multiple hazards per section.
    May 07, 2015    6979    EditableEntries are passed in for reuse.
    May 14, 2015    7376    Robert.Blum Changed to look for only None and not
                                        empty string.
    Jun 03, 2015    8530    Robert.Blum Added method for new initials productPart
                                        and removed duplicate $$.
    Jul 06, 2015    7747    Robert.Blum Changes for adding framed text when text fields are left blank on HID.
'''
import FormatTemplate

import types, re, sys, collections
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self, editableEntries) :
        super(Format, self).initialize(editableEntries)
        self.initProductPartMethodMapping()
        
    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'easMessage': self._easMessage,
            'productHeader': self._productHeader,
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'narrativeForecastInformation': self._narrativeForecastInformation,
            'initials': self._initials,
        }

    def execute(self, productDict, editableEntries=None):
        self.productDict = productDict
        self.initialize(editableEntries)
        legacyText = self._createTextProduct()
        return [ProductUtils.wrapLegacy(legacyText)], self._editableParts

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    ################# Segment Level

    ################# Section Level    
    def _narrativeForecastInformation(self, sectionDict):
        # Get saved value from productText table if available
        narrative = self._getVal('narrativeForecastInformation', sectionDict)
        if narrative is None:
            # ESF sections will only contain one hazard
            hazard = sectionDict.get('hazardEvents')[0]
            narrative = self._tpc.getValueOrFramedText('narrativeForecastInformation', hazard, 'Enter Narrative Forecast Information')
        self._setVal('narrativeForecastInformation', narrative, sectionDict, 'Narrative Forecast Information')
        return self._getFormattedText(narrative, endText='\n\n')
