
import collections

HazardCategories = collections.OrderedDict(
        {
        "Convective": [ ("EW","W"), ("SV","W"), ("TO","W"), ("SV","A"), ("TO","A")],
        
        "Winter Weather": [("BZ", "W"), ("BZ", "A"), ("ZR", "Y"), ("IS", "W"), ("LE", "W"),("LE", "Y"), ("LE", "A"), 
                           ("WC", "W"), ("WC", "Y"), ("WC", "A"), ("WS", "W"), ("WS", "A"), ("WW", "Y")], 
                                        
        "Hydrology": [("FF", "A"), ("FF", "W", "Convective"), ("FF", "W", "NonConvective"), ("FA", "Y"), ("FA", "A"), ("FA", "W"), 
                       ("FL", "A"), ("FL", "W"), ("FL", "Y"), ("HY", "S")],
                       #("FF", "Y")],  # This are not used...         
        
        "Coastal Flood": [("CF", "Y"), ("CF", "A"), ("CF", "W"), ("CR", "S"), ("LS", "A")],
        
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
        }
)