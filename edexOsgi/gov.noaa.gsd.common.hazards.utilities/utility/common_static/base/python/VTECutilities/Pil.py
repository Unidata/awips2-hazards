#------------------------------------------------------------------
#  Pil
#------------------------------------------------------------------
# This object determines the product pil given a Product Generator 
# Category e.g. "FFA", "FLW_FLS", "FFW_FFS" and a VTEC action code.
#------------------------------------------------------------------

from HazardConstants import *
import os

class Pil:

    def __init__(self, productCategory, vtecRecord, hazardEvent):
        self._productCategory = productCategory
        self._vtecRecord = vtecRecord
        self._hazardEvent = hazardEvent
        self._action = self._vtecRecord.get('act')
        try:
            import RiverHazardCommon
            self._rhc = RiverHazardCommon.RiverHazardCommon()
        except:
            pass
        
    def getPil(self):  
        pil = None      
        if self._productCategory == "FFA":
            pil = "FFA"
        
        elif self._productCategory == "FFW_FFS":
            if self._action in ["NEW", "EXT"]:
                pil = "FFW"
            else:
                pil = "FFS"
            
        elif self._productCategory == "ESF":
            pil = "ESF"
        
        elif self._productCategory == "FLW_FLS":
            phenSig = self._vtecRecord.get('phensig')
                        
            if phenSig in ["FL.Y", "FA.Y"]:
                pil = "FLS"
            
            elif phenSig == "HY.S":
                #   The selection of PIL will be determined
                #   in the Product Generator based on other
                #   river forecast points in the product
                pil = "FLW"
            
            elif phenSig == "FA.W":               
                if self._action in ["NEW", "EXT"]:
                    pil = "FLW"
                else:  # CAN, EXP, CON
                     pil = "FLS"
                
            elif phenSig == "FL.W":
                if self._action == "NEW":
                     pil = "FLW"
                elif self._action in ["CAN", "EXP"]:
                     pil = "FLS"
                
                else:
                    # actions CON, EXT
                    try:
                        fcstCategory = self._rhc.getForecastCategory(self._hazardEvent)
                        prevCateogry = self._rhc.getPreviousCategory(self._hazardEvent)
                    except:
                        fcstCategory = 1
                        prevCategory = 1
                    if fcstCategory > prevCategory:
                         pil = "FLW"
                    else:
                         pil = "FLS"
        return pil
            
       