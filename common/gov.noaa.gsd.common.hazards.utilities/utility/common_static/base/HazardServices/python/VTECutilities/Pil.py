#------------------------------------------------------------------
#  Pil
#------------------------------------------------------------------
# This object determines the product pil given a Product Generator 
# Category e.g. "FFA", "FLW_FLS", "FFW_FFS" and a VTEC action code.
#------------------------------------------------------------------

from HazardConstants import *
# For the VTEC tests to work, we need to be able to bypass the RiverForecastPoints module
     
class Pil(object):

    def __init__(self, productCategory, vtecRecord, hazardEvent):
        self._productCategory = productCategory
        self._vtecRecord = vtecRecord
        self._hazardEvent = hazardEvent
        self._action = self._vtecRecord.get('act')
        self._FLOOD_CATEGORY_MAPPING = { 
                     None: 0,  # Lowest flood severity
                     'N':  1,
                     'U':  2,
                     '0':  3,
                      0 :  3,
                     '1':  4,
                      1 :  4,
                     '2':  5,
                      2 :  5,
                     '3':  6,
                      3:   6,  # Highest flood severity
                }

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
                    prevSeverity = self._hazardEvent.get('previousFloodSeverity')                        
                    newSeverity = self._vtecRecord.get('hvtec', {}).get('floodSeverity')
                    if self._getComparableSeverity(newSeverity) > self._getComparableSeverity(prevSeverity):
                        pil = "FLW"
                    else:
                        pil = "FLS"
        return pil

    def _getComparableSeverity(self, category):
        return self._FLOOD_CATEGORY_MAPPING[category]
