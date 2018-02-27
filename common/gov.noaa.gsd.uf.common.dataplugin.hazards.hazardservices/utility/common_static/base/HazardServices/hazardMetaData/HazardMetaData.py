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
                {"hazardTypes": [("HY", "S")], "classMetaData": "MetaData_HY_S"},
                {"hazardTypes": HazardCategories.get("Convective"), "classMetaData": None},
                {
                 "hazardTypes":  HazardCategories.get("Winter Weather") +  HazardCategories.get("Coastal Flood") + HazardCategories.get("Fire Weather") +\
                                 HazardCategories.get("Marine") + HazardCategories.get("Non Precip"), # + HazardCategories.get("Tropical"),
                 "classMetaData": None, # empty for now -- MetaData_LongFused,
                },
                                
                {"hazardTypes": HazardCategories.get('Prob Storm Prediction'), "classMetaData": "MetaData_Prob_Convection"},                
                {"hazardTypes": HazardCategories.get('Prob Weather Prediction'), "classMetaData": "MetaData_Prob_Rainfall"},

                {"hazardTypes": [("Prob_Tornado",), ("Prob_Severe",)], "classMetaData":  "MetaData_Prob_Convective"},
                
                {"hazardTypes": [("SIGMET", "NonConvective")], "classMetaData": "MetaData_SIGMET_W"},
                {"hazardTypes": [("SIGMET","Convective")], "classMetaData": "MetaData_Convective_SIGMET"},
                {"hazardTypes": [("SIGMET","International")], "classMetaData": "MetaData_International_SIGMET"},
                {"hazardTypes": [("VAA",)], "classMetaData": "MetaData_VAA"},
                {"hazardTypes": [("LLWS",)], "classMetaData": "MetaData_LLWS"},
                {"hazardTypes": [("Strong_Surface_Wind",)], "classMetaData": "MetaData_Strong_Surface_Wind"},
                {"hazardTypes": [("Turbulence",)], "classMetaData": "MetaData_Turbulence"},
                {"hazardTypes": [("Mountain_Obscuration",)], "classMetaData": "MetaData_Mountain_Obscuration"},
                {"hazardTypes": [("IFR",)], "classMetaData": "MetaData_IFR"},
                {"hazardTypes": [("Icing",)], "classMetaData": "MetaData_Icing"},
                {"hazardTypes": [("Multiple_Freezing_Levels",)], "classMetaData": "MetaData_Multiple_Freezing_Levels"},
                
                {"hazardTypes": [("SS", "A")], "classMetaData": "MetaData_StormSurge"},
                {"hazardTypes": [("SS", "W")], "classMetaData": "MetaData_StormSurge"},
                
]
