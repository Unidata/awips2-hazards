# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# VTEC_GHG Complex Hazard Tests 
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "GHG_Complex1_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Creating a WS.A hazard for the entire CWA.",
    "name": "GHG_Complex1_1",
    "productType": "WSW",
    "createGrids": [
       (0, 39, "WS.A", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049","FLZ050", "FLZ051", "FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
       ],
    "checkStrings": [
      "FLZ039-042-043-048>052-055>057-060>062-065-",
      "/O.NEW.KTBW.WS.A.0001.100101T0000Z-100102T1500Z/",
       ],
    },
    {
    
    "commentary": """
    Upgrading Area1 from WS.A to WS.W
    Upgrading Area2 from WS.A to ZR.Y
    Area1 (FLZ039, FLZ042, FLZ043, FLZ048, FLZ049, FLZ050, FLZ051) 0-39
    Area2 (FLZ052, FLZ055, FLZ056, FLZ057, FLZ060, FLZ061, FLZ062, FLZ065) 0-39
    """,
    
    "name": "GHG_Complex1_2",
    "productType": "WSW",
    "createGrids": [
       (0, 39, "WS.W", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051"]),
       (0, 39, "ZR.Y", ["FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
       ],
    "checkStrings": [
      "FLZ039-042-043-048>051-",
      "/O.UPG.KTBW.WS.A.0001.000000T0000Z-100102T1500Z/",
      "/O.NEW.KTBW.WS.W.0001.100101T0000Z-100102T1500Z/",
      "FLZ052-055>057-060>062-065-",
      "/O.UPG.KTBW.WS.A.0001.000000T0000Z-100102T1500Z/",
      "/O.NEW.KTBW.ZR.Y.0001.100101T0000Z-100102T1500Z/",
       ],
    },
    {
    
    "commentary": """
    Expanding the existing WS.W hazard (Area1) into the existing ZR.Y hazard (Area2)
    Area1 (FLZ039, FLZ042, FLZ043, FLZ048, FLZ049, FLZ050, FLZ051, FLZ052, FLZ057) 0-39
    Area2 (FLZ055, FLZ056, FLZ060, FLZ061, FLZ062, FLZO65) 0-39
    """,
    
    "name": "GHG_Complex1_3",
    "productType": "WSW",
    "createGrids": [
        (0, 39, "WS.W", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051", "FLZ052", "FLZ057"]),
        (0, 39, "ZR.Y", ["FLZ055", "FLZ056", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
        ],
    "checkStrings": [
      "FLZ052-057-",
      "/O.UPG.KTBW.ZR.Y.0001.000000T0000Z-100102T1500Z/",
      "/O.EXA.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
      "FLZ039-042-043-048>051-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
      "FLZ055-056-060>062-065-",
      "/O.CON.KTBW.ZR.Y.0001.000000T0000Z-100102T1500Z/",
        ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex1_4",
    "productType": "WSW",
    "createGrids": [
        (0, 39, "WS.W", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051", "FLZ052", "FLZ057"]),
        (0, 39, "ZR.Y", ["FLZ055", "FLZ056", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-048>052-057-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
      "FLZ055-056-060>062-065-",
      "/O.CON.KTBW.ZR.Y.0001.000000T0000Z-100102T1500Z/",
                     ],
    },
    {
    
    "commentary": """
    Removing Area2 and the ZR.Y hazard
    Area2 (FLZ055, FLZ056, FLZ060, FLZ061, FLZ062, FLZ065)
    """,
    
    "name": "GHG_Complex1_5",
    "productType": "WSW",
    "createGrids": [
        (0, 39, "WS.W", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051", "FLZ052", "FLZ057"]),
    ],
    "checkStrings": [
      "FLZ055-056-060>062-065-",
      "/O.CAN.KTBW.ZR.Y.0001.000000T0000Z-100102T1500Z/",
      "FLZ039-042-043-048>052-057-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
         ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex1_6",
    "createGrids": [
        (0, 39, "WS.W", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051", "FLZ052", "FLZ057"]),
    ],
    "productType": "WSW",
    "checkStrings": [
      "FLZ039-042-043-048>052-057-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
                     ],
    }, 
    {

    "commentary": """
    Reducing the WS.W hazard coverage of Area1
    Area1 (FLZ048, FLZ049, FLZ050) 0-39
    """,
    
    "name": "GHG_Complex1_7",
    "productType": "WSW",
    "createGrids": [
        (0, 39, "WS.W", ["FLZ048", "FLZ049", "FLZ050"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-051-052-057-",
      "/O.CAN.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
      "FLZ048>050-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
                     ],
    },
    {
    
    "commentary": """
    Creating a WC.Y hazard for Area3
    Area3 (FLZ039, FLZ042, FLZ043, FLZ051, FLZ052, FLZ055, FLZ056, FLZ057, FLZ060, FLZ061, FLZ062, FLZ065) 0-39
    """,
    
    "name": "GHG_Complex1_8",
    "productType": "WSW",
    "createGrids": [
        (0, 39, "WS.W", ["FLZ048", "FLZ049", "FLZ050"]),
        (0, 39, "WC.Y", ["FLZ039", "FLZ042", "FLZ043", "FLZ051", "FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-051-052-055>057-060>062-065-",
      "/O.NEW.KTBW.WC.Y.0001.100101T0000Z-100102T1500Z/",
      "FLZ048>050-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
                     ],
    },
    {
    
    "commentary": """
    Removing Area1 and the WS.W hazard
    Area1 (FLZ048, FLZ049, FLZ050) 0-39
    Reducing the time range of Area3
    Area3 (FLZ039, FLZ042, FLZ043, FLZ051, FLZ052, FLZ055, FLZ056, FLZ057, FLZ060, FLZ061, FLZ062, FLZ065) 0-36
    """,
    
    "name": "GHG_Complex1_9",
    "productType": "WSW",
    "createGrids": [
        (0, 36, "WC.Y", ["FLZ039", "FLZ042", "FLZ043", "FLZ051", "FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
        ],
    "checkStrings": [
       "FLZ048>050-",
       "/O.CAN.KTBW.WS.W.0001.000000T0000Z-100102T1500Z/",
       "FLZ039-042-043-051-052-055>057-060>062-065-",
       "/O.EXT.KTBW.WC.Y.0001.000000T0000Z-100102T1200Z/",
                     ],
    },
    {
    "commentary": "Canceling out all hazards.",
    "name": "GHG_Complex1_10",
    "productType": None,
    "checkStrings": [],
    "clearHazardsTable": 1,
    },
    
    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0000",
        }
    return TestScript.generalTestScript(scripts, defaults)




