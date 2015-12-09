
import collections
 
#
# Each entry in this dictionary defines a category, with the value for each category
# being a list of tuples, with each of the tuples in turn defining a hazard type.
# Note that the tuples may have one, two, or three elements, the first being the
# phenomenon, the second the significance, and the third the sub-type.
#
# Note that if a single element tuple is being specified, it must have a comma (,)
# following its single element; otherwise, Python will interpret it as a string
# instead of a tuple, leading to errors.
#
#  Please note:  If you have just one entry in a tuple, you need to add an extra comma.
#  For example:
#            "Prob Convective" :[("Prob_Tornado",), ("Prob_Severe",)],    

# HazardCategories = collections.OrderedDict()
# HazardCategories["Prob Convective"] = [("Prob_Tornado",), ("Prob_Severe",)]
# HazardCategories["Prob Storm Prediction"] = [("Prob_Convection",)]
# HazardCategories["Prob Weather Prediction"] =[("Prob_Rainfall",)]
# HazardCategories["Hydrology"] = [("FF", "A"), ("FF", "W", "Convective"), ("FF", "W", "NonConvective"), ("FF", "W", "BurnScar"),
#                       ("FA", "Y"), ("FA", "A"), ("FA", "W"), 
#                       ("FL", "A"), ("FL", "W"), ("FL", "Y"), ("HY", "S"), ("HY", "O")],
#                        #("FF", "Y")],  # This is not used...



HazardCategories = collections.OrderedDict(
        {
          
        ### Probabilistic
        "Prob Convective" :[("Prob_Tornado",), ("Prob_Severe",)],
        "Prob Storm Prediction" :[("Prob_Convection",)],
        "Prob Weather Prediction" :[("Prob_Rainfall",)],
         
        ### WFO Deterministic
        "Convective": [ ("EW","W"), ("SV","W"), ("TO","W"), ("SV","A"), ("TO","A")],
         
        "Winter Weather": [("BZ", "W"), ("BZ", "A"), ("ZR", "Y"), ("IS", "W"), ("LE", "W"),("LE", "Y"), ("LE", "A"), 
                           ("WC", "W"), ("WC", "Y"), ("WC", "A"), ("WS", "W"), ("WS", "A"), ("WW", "Y")], 
                                         
        "Hydrology": [("FF", "A"), ("FF", "W", "Convective"), ("FF", "W", "NonConvective"), ("FF", "W", "BurnScar"),
                      ("FA", "Y"), ("FA", "A"), ("FA", "W"), 
                      ("FL", "A"), ("FL", "W"), ("FL", "Y"), ("HY", "S"), ("HY", "O")],
                       #("FF", "Y")],  # This is not used...         
         
        "Coastal Flood": [("BH","S"), ("CF", "A"), ("CF", "W"), ("CF", "Y"), ("CF", "S"), ("LS", "A"), ("LS", "W"), ("LS", "Y"), ("SU", "W"), ("SU", "Y")],
        
        "Fire Weather": [("FW", "A"), ("FW", "W")], 
                      
        "Marine":  [("SE", "A"), ("SE", "W"), ("BW", "Y"), ("GL", "A"), ("GL", "W"), ("HF", "W"), ("HF", "A"), 
                    ("LO", "Y"), ("MA", "S"), ("MA", "W"), ("MH", "Y"), ("MH", "W"), ("MF", "Y"), ("MS", "Y"), 
                    ("SI", "Y"), ("SC", "Y"), ("SW", "Y"), ("RB", "Y"), ("SR", "A"), ("SR", "W"), ("UP", "A"), 
                    ("UP", "Y"), ("UP", "W") ] ,
                                   
        "Non Precip": [("AF", "W"), ("AF", "Y"), ("AQ", "Y"), ("AS", "O"), ("AS", "Y"), ("DU", "Y"), ("DS", "W"), 
                       ("EH", "W"), ("EH", "A"),("HT", "Y"), ("EC", "W"), ("EC", "A"),  ("FG", "Y"), ("FZ", "W"), 
                       ("FZ", "A"), ("HZ", "W"), ("HZ", "A"),("FR", "Y"), ("ZF", "Y"), ("HW", "A"),("HW", "W"), 
                       ("LW", "Y"), ("SM", "Y"), ("WI", "Y") ],
                        
        "Tropical": [("TR", "W"), ("TR", "A"), ("HU", "W"), ("HU", "S"), ("HU", "A"), ("HI", "A"), ("HI", "W"), 
                     ("TI", "W"), ("TI", "A"), ("TY", "A"),("TY", "W"), ("TS", "A"), ("TS", "W")],
                    
#        "Probabilistic": [("Prob_Tornado",), ("Prob_Wind",), ("Prob_Hail",)]
        }
)