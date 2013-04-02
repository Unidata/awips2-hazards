# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# Hazards_TestScript
#
# Author:
# ----------------------------------------------------------------------------


# The following tuples are of the form:

scripts = [
    {"name":"CFW1",
     "productType" : "CFW",
     "commentary": "Basic testing of CFW product with CF.Y.",
     "createGrids": [
        (0, 96, "CF.Y", ["FLZ039"]),
        ],
     "checkStrings" : [
        "FLZ039-",
        "/O.NEW.KTBW.CF.Y.0001.100526T2300Z-100530T2300Z/",
        ],
     },
    
    {"name":"FFA1", 
     "productType" : "FFA", 
     "commentary": "Basic testing of FFA product with FF.A.",
     "createGrids": [
        (4, 7, "FF.A", ["FLZ039"], {'immediateCause': 'ER'}),
        ],
     "checkStrings" : [
          "FLZ039-",
          "/O.NEW.KTBW.FF.A.0001.100527T0300Z-100527T0600Z/",
          "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
        ],

     },
    
    {"name":"MWS1", 
     "productType" : "MWS", 
     "commentary": "Basic testing of MWS product with MA.S",
     "createGrids": [
        (9, 13, "MA.S", ['GMZ830']),
        ],
     "checkStrings" : [
         "GMZ830-",
         "/O.NEW.KTBW.MA.S.0001.100527T0800Z-100527T1200Z/",
        ],
     },
    
    {"name":"MWW1", 
     "productType" : "MWW", 
     "commentary": "Basic testing of MWW product with GL.A",
     "createGrids": [
        (0, 1, "GL.A", ['GMZ830']),
        ],
     "checkStrings" : [
        "GMZ830-",
        "/O.NEW.KTBW.GL.A.0001.100526T2300Z-100527T0000Z/",
        ],
     },

    {"name":"MWW2",
     "productType" : "MWW",
     "commentary": "Basic testing of MWW product with SE.W",
     "createGrids": [
        (0, 1, "SE.W", ['GMZ830']),
        ],
     "checkStrings" : [
        "GMZ830-",
        "/O.NEW.KTBW.SE.W.0001.100526T2300Z-100527T0000Z/",
        ],
     },

    {"name":"NPW1",
     "productType" : "NPW",
     "commentary": "Basic testing of NPW product with AS.O",
     "createGrids": [
        (0, 1, "AS.O", ["FLZ039"]),
        ],
     "checkStrings" : [
        "FLZ039-",
        "/O.NEW.KTBW.AS.O.0001.100526T2300Z-100527T0000Z/",
        ],
     },
   
    {"name":"NPW2",
     "productType" : "NPW",
     "commentary": "Basic testing of NPW product with AF.Y",
     "createGrids": [
        (0, 1, "AF.Y", ["FLZ039"]),
        ],
     "checkStrings" : [
        "FLZ039-",
        "/O.NEW.KTBW.AF.Y.0001.100526T2300Z-100527T0000Z/",
        ],
     },
 
    {"name":"RFW1", 
     "productType" : "RFW", 
     "commentary": "Basic testing of RFW product with FW.W",
     "createGrids": [
        (2, 15, "FW.W", ["FLZ039"]),
        ],
     "checkStrings" : [
         "FLZ039-",
         "/O.NEW.KTBW.FW.W.0001.100527T0100Z-100527T1400Z/",
        ],
     },

    {"name":"WCN1", 
     "productType" : "WCN", 
     "commentary": "Basic testing of WCN product with TO.A",
     "createGrids": [
        (0, 6, "TO.A", ["FLC015", "GMZ830"], {'forceEtn': 123}),
        ],
     "checkStrings" : [
        "FLC015-",
        "/O.NEW.KTBW.TO.A.0123.100526T2300Z-100527T0500Z/",
        "GMZ830-",
        "/O.NEW.KTBW.TO.A.0123.100526T2300Z-100527T0500Z/",

       ],
     },
    
    {"name":"WSW1", 
     "productType" : "WSW", 
     "commentary": "Basic testing of WSW product with BZ.W",
     "createGrids": [
        (12, 21, "BZ.W", ["FLZ039"]),
        ],
     "checkStrings" : [
        "FLZ039-",
        "/O.NEW.KTBW.BZ.W.0001.100527T1100Z-100527T2000Z/",
        ],
     },    
    {"name":"AQA1",
     "productType" : "AQA",
     "commentary": "Basic testing of AQA product with AQ.Y",
     "createGrids": [
        (0, 1, "AQ.Y", ["FLZ039"]),
        ],
     "checkStrings" : [
        "FLZ039-",
        "/O.NEW.KTBW.AQ.Y.0001.100526T2300Z-100527T0000Z/",
        ],
     },

    ]

import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "publishGrids" : 1,
        "vtecMode" : "O",
        "clearHazardsTable": 1,
        "gridsStartTime": "20100526_2300",
        "orderStrings": 1,
        }
    return TestScript.generalTestScript(scripts, defaults)

