#------------------------------------------------------------------
#  Pil
#------------------------------------------------------------------
# This object determines the product pil given a Product Generator 
# Category e.g. "FFA", "FLW_FLS", "FFW_FFS" and a VTEC action code.
#------------------------------------------------------------------

from HazardConstants import *

class Pil:

    def __init__(self, productCategory, action):
        self._productCategory = productCategory
        self._action = action
        
    def getPil(self):
        if self._productCategory == "FFA":
            return "FFA"
        if self._productCategory == "FLW_FLS":
            if self._action in ["NEW", "EXT"]:
                return "FLW"
            else:
                return "FLS"
        if self._productCategory == "FFW_FFS":
            if self._action in ["NEW", "EXT"]:
                return "FFW"
            else:
                return "FFS"
        if self._productCategory == "ESF":
            return "ESF"
            
       