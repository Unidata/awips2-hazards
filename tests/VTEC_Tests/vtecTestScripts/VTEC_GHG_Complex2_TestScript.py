# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# more VTEC_GHG Complex Hazard Tests 
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "GHG_Complex2_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    
    "commentary": """
    Creating a WS.A hazard for Area1 and Area2
    Area1 (FLZ039, FLZ042, FLZ043) 21-45
    Area2 (FLZ052, FLZ055, FLZ056) 27-45
    Note that we had to condense the times into two time ranges to accomodate for the overlapping
    """,
    
    "name": "GHG_Complex2_1",
    "productType": "WSW",
    "createGrids": [
        (21, 27, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ052", "FLZ055", "FLZ056"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-",
      "/O.NEW.KTBW.WS.A.0001.100101T2100Z-100102T2100Z/",
      "FLZ052-055-056-",
      "/O.NEW.KTBW.WS.A.0001.100102T0300Z-100102T2100Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex2_2",
    "productType": "WSW",
    "createGrids": [
        (21, 27, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ052", "FLZ055", "FLZ056"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-",
      "/O.CON.KTBW.WS.A.0001.100101T2100Z-100102T2100Z/",
      "FLZ052-055-056-",
      "/O.CON.KTBW.WS.A.0001.100102T0300Z-100102T2100Z/",
                     ],
    },
    {
    
    "commentary": """
    Expanding the WS.A hazard of Area2
    Area2 (FLZ052, FLZ055, FLZ056, FLZ057, FLZ060, FLZ061) 27-45
    """,
    
    "name": "GHG_Complex2_3",
    "productType": "WSW",
    "createGrids": [
        (21, 27, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061"]),
        ],
    "checkStrings": [
      "FLZ057-060-061-",
      "/O.EXA.KTBW.WS.A.0001.100102T0300Z-100102T2100Z/",
      "FLZ039-042-043-",
      "/O.CON.KTBW.WS.A.0001.100101T2100Z-100102T2100Z/",
      "FLZ052-055-056-",
      "/O.CON.KTBW.WS.A.0001.100102T0300Z-100102T2100Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.", 
    "name": "GHG_Complex2_4",
    "productType": "WSW",
    "createGrids": [
        (21, 27, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.A", ["FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-",
      "/O.CON.KTBW.WS.A.0001.100101T2100Z-100102T2100Z/",
      "FLZ052-055>057-060-061-",
      "/O.CON.KTBW.WS.A.0001.100102T0300Z-100102T2100Z/",
                     ],
    },
    {
    
    "commentary": """
    Replacing the WS.A hazard in Area1 with a WS.W hazard
    Area1 (FLZ039, FLZ042, FLZ043) 21-45
    Replacing a portion of the WS.A hazard in Area2 with no hazards
    Area2 (FLZ052, FLZ055, FLZ056, FLZ057) 27-45
    Replacing a portion of the WS.A hazard in Area2 with a WW.Y hazard
    Area2 (FLZ060, FLZ061) 27-45
    """,
    
    "name": "GHG_Complex2_5",
    "productType": "WSW",
    "createGrids": [
        (21, 27, "WS.W", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.W", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WW.Y", ["FLZ060", "FLZ061"]), 
        ],
    "checkStrings": [
      "FLZ052-055>057-",
      "/O.CAN.KTBW.WS.A.0001.100102T0300Z-100102T2100Z/",
      "FLZ039-042-043-",
      "/O.UPG.KTBW.WS.A.0001.100101T2100Z-100102T2100Z/",
      "/O.NEW.KTBW.WS.W.0001.100101T2100Z-100102T2100Z/",
      "FLZ060-061-",
      "/O.UPG.KTBW.WS.A.0001.100102T0300Z-100102T2100Z/",
      "/O.NEW.KTBW.WW.Y.0001.100102T0300Z-100102T2100Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex2_6",
    "productType": "WSW",
    "createGrids": [
        (21, 27, "WS.W", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WS.W", ["FLZ039", "FLZ042", "FLZ043"]),
        (27, 45, "WW.Y", ["FLZ060", "FLZ061"]), 
        ],
    "checkStrings": [
                     "FLZ039-042-043-",
                     "/O.CON.KTBW.WS.W.0001.100101T2100Z-100102T2100Z/",
                     "FLZ060-061-",
                     "/O.CON.KTBW.WW.Y.0001.100102T0300Z-100102T2100Z/",
                     ],
    }, 
    {
    
    "commentary": """
    Changing the time of Area1
    Area1 (FLZ039, FLZ042, FLZ043) 0-45
    Changing the time of Area2
    Area2 (FLZ060, FLZ061) 0-45
    """,
    
    "name": "GHG_Complex2_7",
    "productType": "WSW",
    "createGrids": [
        (0, 45, "WS.W", ["FLZ039", "FLZ042", "FLZ043"]),
        (0, 45, "WW.Y", ["FLZ060", "FLZ061"]),
        ],
    "checkStrings": [
       "FLZ039-042-043-",
       "/O.EXT.KTBW.WS.W.0001.100101T0000Z-100102T2100Z/",
       "FLZ060-061-",
       "/O.EXT.KTBW.WW.Y.0001.100101T0000Z-100102T2100Z/",
       ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex2_8",
    "productType": "WSW",
    "createGrids": [
        (0, 45, "WS.W", ["FLZ039", "FLZ042", "FLZ043"]),
        (0, 45, "WW.Y", ["FLZ060", "FLZ061"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T2100Z/",
      "FLZ060-061-",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100102T2100Z/",
                     ],
    },
    {
    
    "commentary": """
    Removing a portion of the WS.W hazard from Area1 and setting it to no hazards
    Area1 (FLZ039) 0-45
    Removing the WW.Y hazard for all of Area2
    Area2 (FLZ060, FLZ061) 0-45
    """,

    "name": "GHG_Complex2_9",
    "productType": "WSW",
    "createGrids": [
        (0, 45, "WS.W", ["FLZ042", "FLZ043"]),
        ],
    "checkStrings": [
      "FLZ039-",
      "/O.CAN.KTBW.WS.W.0001.000000T0000Z-100102T2100Z/",
      "FLZ060-061-",
      "/O.CAN.KTBW.WW.Y.0001.000000T0000Z-100102T2100Z/",
      "FLZ042-043-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T2100Z/",
                     ],
    },
    {
    "commentary": "Canceling out all hazards.",
    "name": "GHG_Complex2_10",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    ]
       

import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0000",
        "cmdLineVars": "{('Issued By', 'issuedBy'): None}",
        }
    return TestScript.generalTestScript(scripts, defaults)








