from HazardCategories import HazardCategories

#   The order of this list is important.  
#   For example, "FA", "A" may be listed in the hydroHazardTypes, 
#   so the Hazard Information Dialog needs to 
#   process the list in this order taking the first definition of options for "FA", "A"

# NOTE: The "classMetaData" entry is the name of a file containing a "class MetaData" with
#       an execute(self, hazardEvent=None, metaDict=None) method.
#       This method, when run, should return a dictionary holding megawidgets and other
#       related information.
HazardMetaData =[
                {"hazardTypes": [("FF", "W", "Convective")], "classMetaData": "MetaData_FF_W_Convective"},
                {"hazardTypes": [("FF", "W", "NonConvective")], "classMetaData": "MetaData_FF_W_NonConvective"},
                {"hazardTypes": [("FF", "W", "BurnScar")], "classMetaData": "MetaData_FF_W_BurnScar"},
                {"hazardTypes": [("FA", "Y")], "classMetaData": "MetaData_FA_Y"},
                {"hazardTypes": [("FA", "W")], "classMetaData": "MetaData_FA_W"},
                {"hazardTypes": [("FF", "A")], "classMetaData": "MetaData_FF_A"},
                {"hazardTypes": [("FA", "A")], "classMetaData": "MetaData_FA_A"},
                {"hazardTypes": [("FL", "A")], "classMetaData": "MetaData_FL_A",},
                {"hazardTypes": [("FL", "W")], "classMetaData": "MetaData_FL_W"},
                {"hazardTypes": [("FL", "Y")], "classMetaData": "MetaData_FL_Y"},
                {"hazardTypes": [("HY", "O")], "classMetaData": "MetaData_HY_O"},
                {"hazardTypes": [("HY", "S")], "classMetaData": None},
                {"hazardTypes": HazardCategories.get("Convective"), "classMetaData": None},
                {
                "hazardTypes":  HazardCategories.get("Winter Weather") +  HazardCategories.get("Coastal Flood") + HazardCategories.get("Fire Weather") +\
                                HazardCategories.get("Marine") + HazardCategories.get("Non Precip") + HazardCategories.get("Tropical"),
                "classMetaData": None, # empty for now -- MetaData_LongFused,
                },
                {"hazardTypes": HazardCategories.get("Probabilistic"), "classMetaData": None},
]
