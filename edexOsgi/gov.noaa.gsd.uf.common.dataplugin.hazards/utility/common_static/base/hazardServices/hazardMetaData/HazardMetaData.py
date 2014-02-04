

from HazardCategories import HazardCategories
from MetaData_FAY import MetaData_FAY
from MetaData_FFA import MetaData_FFA
from MetaData_FAA import MetaData_FAA
from MetaData_FAW import MetaData_FAW
from MetaData_FLA import MetaData_FLA
from MetaData_FLW import MetaData_FLW
from MetaData_FLY import MetaData_FLY
from MetaData_FFW_Convective import MetaData_FFW_Convective
from MetaData_FFW_NonConvective import MetaData_FFW_NonConvective
from MetaData_ShortFused import MetaData_ShortFused

#   The order of this list is important.  
#   For example, "FA", "A" may be listed in the hydroHazardTypes, 
#   so the Hazard Information Dialog needs to 
#   process the list in this order taking the first definition of options for "FA", "A"

# NOTE: The "metaData" entry is a list of mega widgets.
#       The "classMetaData" entry is the name of a file containing a "class MetaData" with
#       an execute(self, hazardEvent=None, metaDict=None) method.
#       This method, when run, should return the list of mega widgets.
HazardMetaData =[
                {"hazardTypes": [("FF", "W", "Convective")],"metaData": MetaData_FFW_Convective, 
                 "classMetaData": "MetaData_FF_W_Convective"},
                {"hazardTypes": [("FF", "W", "NonConvective")],"metaData": MetaData_FFW_NonConvective,
                 "classMetaData": "MetaData_FF_W_NonConvective"},
                {"hazardTypes": [("FA", "Y")],"metaData": MetaData_FAY,
                 "classMetaData": "MetaData_FA_Y"},
                {"hazardTypes": [("FA", "W")],"metaData": MetaData_FAW,
                 "classMetaData": "MetaData_FA_W"},
                {"hazardTypes": [("FF", "A")],"metaData": MetaData_FFA,
                 "classMetaData": "MetaData_FF_A"},
                {"hazardTypes": [("FA", "A")],"metaData": MetaData_FAA,
                 "classMetaData": "MetaData_FA_A"},
                {"hazardTypes": [("FL", "A")],"metaData": MetaData_FLA,
                 "classMetaData": "MetaData_FL_A",},
                {"hazardTypes": [("FL", "W")],"metaData": MetaData_FLW,
                 "classMetaData": "MetaData_FL_W"},
                {"hazardTypes": [("FL", "Y")],"metaData": MetaData_FLY,
                 "classMetaData": "MetaData_FL_Y"},
                {"hazardTypes": [("HY", "O"), ("HY", "S")],"metaData": [], "classMetaData": None},
                {"hazardTypes": HazardCategories.get("Convective"),"metaData": MetaData_ShortFused, "classMetaData": None},
                {
                "hazardTypes":  HazardCategories.get("Winter Weather") +  HazardCategories.get("Coastal Flood") + HazardCategories.get("Fire Weather") +\
                                HazardCategories.get("Marine") + HazardCategories.get("Non Precip") + HazardCategories.get("Tropical"),
                "metaData": [], "classMetaData": None, # empty for now -- MetaData_LongFused,
                },
]
