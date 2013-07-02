

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
#from MetaData_LongFused import MetaData
from MetaData_HydroPoint import MetaData_HydroPoint

#   The order of this list is important.  
#   For example, "FA", "A" may be listed in the hydroHazardTypes, 
#   so the Hazard Information Dialog needs to 
#   process the list in this order taking the first definition of options for FA", "A", "
HazardMetaData =[
                {
                "hazardTypes": [("FF", "W", "Convective")],
                "metaData": MetaData_FFW_Convective,
                },
                {
                "hazardTypes": [("FF", "W", "NonConvective")],
                "metaData": MetaData_FFW_NonConvective,
                },
                {
                "hazardTypes": [("FA", "Y")],
                "metaData": MetaData_FAY, 
                 },
                {
                "hazardTypes": [("FA", "W")],
                "metaData": MetaData_FAW,
                },
                {
                "hazardTypes": [("FF", "A")],
                "metaData": MetaData_FFA,
                },
                {
                "hazardTypes": [("FA", "A")],
                "metaData": MetaData_FAA,
                },
                {
                "hazardTypes": [("FL", "A")],
                "metaData": MetaData_FLA,
                },
                {
                "hazardTypes": [("FL", "W")],
                "metaData": MetaData_FLW,
                },
                {
                "hazardTypes": [("FL", "Y")],
                "metaData": MetaData_FLY,
                },
               {
               "hazardTypes": HazardCategories.get("Convective"),
               "metaData": MetaData_ShortFused,
               },
               {
                "hazardTypes":  HazardCategories.get("Winter Weather") +  HazardCategories.get("Coastal Flood") + HazardCategories.get("Fire Weather") +\
                                HazardCategories.get("Marine") + HazardCategories.get("Non Precip") + HazardCategories.get("Tropical"),
                "metaData": [], # empty for now -- MetaData_LongFused,
               },
]
