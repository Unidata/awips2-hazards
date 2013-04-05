# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# More VTEC_GHG Complex Hazard Tests! 
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.", 
    "name": "GHG_Complex4_0",
    "productType": None,
    "clearHazardsTable" : 1,
    "checkStrings": [],
    },
    {

    "commentary": """
    Creating a FG.Y hazard for Area1 and Area2
    Area1 (FLZ039, FLZ042, FLZ043) 0-1
    Area2 (FLZ048, FLZ049, FLZ050) 1-24
    """,
    
    "name": "GHG_Complex4_1",
    "productType": "NPW",
    "createGrids": [
        (0, 1, "FG.Y", ["FLZ039", "FLZ042", "FLZ043"]),
        (1, 24, "FG.Y", ["FLZ048", "FLZ049", "FLZ050"]),
        ],
    "drtTime": "20100101_0000",
    "checkStrings": [
      "FLZ039-042-043-",
      "/O.NEW.KTBW.FG.Y.0001.100101T0000Z-100101T0100Z/",
      "FLZ048>050-",
      "/O.NEW.KTBW.FG.Y.0001.100101T0100Z-100102T0000Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex4_2",
    "drtTime": "20100101_0015",
    "productType": "NPW",
    "createGrids": [
        (0, 1, "FG.Y", ["FLZ039", "FLZ042", "FLZ043"]),
        (1, 24, "FG.Y", ["FLZ048", "FLZ049", "FLZ050"]),
        ],
    "checkStrings": [
      "FLZ039-042-043-",
      "/O.CON.KTBW.FG.Y.0001.000000T0000Z-100101T0100Z/",
      "FLZ048>050-",
      "/O.CON.KTBW.FG.Y.0001.100101T0100Z-100102T0000Z/",
                     ],      
    },
    {

    "commentary": """
    Creating a new area (Area3) with a HW.A hazard
    Note that we jumped ahead in time 1 hour in order for the FG.Y hazard in Area1 to expire
    Area3 (FLZ043, FLZ052, FLZ057) 33-75
    Area1 (FLZ039, FLZ042, FLZ043) 0-1
    """,
    
    "name": "GHG_Complex4_3",
    "productType": "NPW",
    "drtTime":"20100101_0115",
    "createGrids": [
        (0, 1, "FG.Y", ["FLZ039", "FLZ042", "FLZ043"]),
        (1, 24, "FG.Y", ["FLZ048", "FLZ049", "FLZ050"]),
        (33, 75, "HW.A", ["FLZ043", "FLZ052", "FLZ057"]),
        ],
    "checkStrings": [
      "FLZ043-",
      "/O.EXP.KTBW.FG.Y.0001.000000T0000Z-100101T0100Z/",
      "/O.NEW.KTBW.HW.A.0001.100102T0900Z-100104T0300Z/",
      "FLZ039-042-",
      "/O.EXP.KTBW.FG.Y.0001.000000T0000Z-100101T0100Z/",
      "FLZ052-057-",
      "/O.NEW.KTBW.HW.A.0001.100102T0900Z-100104T0300Z/",
      "FLZ048>050-",
      "/O.CON.KTBW.FG.Y.0001.000000T0000Z-100102T0000Z/",
                     ],
    },
    {
    
    "commentary": """
    Removing Area2 and the FG.Y hazard
    Area2 (FLZ048, FLZ049, FLZ050) 1-24
    Upgrading the HW.A hazard in Area3 to a HW.W hazard
    Area3 (FLZ043, FLZ052, FLZ057) 33-75
    """,
    
    "name": "GHG_Complex4_4",
    "productType": "NPW",
    "drtTime":"20100101_0115",
    "createGrids": [
        (0, 1, "FG.Y", ["FLZ039", "FLZ042", "FLZ043"]),
        (33, 75, "HW.W", ["FLZ043", "FLZ052", "FLZ057"]),
        ],        
    "checkStrings": [
      "FLZ048>050-",
      "/O.CAN.KTBW.FG.Y.0001.000000T0000Z-100102T0000Z/",
      "FLZ043-052-057-",
      "/O.UPG.KTBW.HW.A.0001.100102T0900Z-100104T0300Z/",
      "/O.NEW.KTBW.HW.W.0001.100102T0900Z-100104T0300Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex4_5",
    "productType": "NPW",
    "drtTime":"20100101_0115",
    "createGrids": [
        (0, 1, "FG.Y", ["FLZ039", "FLZ042", "FLZ043"]),
        (33, 75, "HW.W", ["FLZ043", "FLZ052", "FLZ057"]),
        ],        
    "checkStrings": [
      "FLZ043-052-057-",
      "/O.CON.KTBW.HW.W.0001.100102T0900Z-100104T0300Z/",
                  ],
    },
    {
    
    "commentary": """
    Reducing a portion of the HW.W hazard in Area3 and setting it to no hazards
    Area3 (FLZ057) 33-75
    """,
    
    "name": "GHG_Complex4_6",
    "productType": "NPW",
    "drtTime":"20100101_0115",
    "createGrids": [
        (0, 1, "FG.Y", ["FLZ039", "FLZ042", "FLZ043"]),
        (33, 75, "HW.W", ["FLZ043", "FLZ052"]),
        ],
    "checkStrings": [
       "FLZ057-",
       "/O.CAN.KTBW.HW.W.0001.100102T0900Z-100104T0300Z/",
       "FLZ043-052-",
       "/O.CON.KTBW.HW.W.0001.100102T0900Z-100104T0300Z/",
                     ],
    }, 
    {

    "commentary": """
    Replacing the HW.W hazard in Area3 with no hazards
    Area3 (FLZ043, FLZ052) 33-75
    """,
    
    "name": "GHG_Complex4_7",
    "drtTime":"20100101_0115",
    "productType": "NPW",
    "createGrids": [
        (0, 1, "FG.Y", ["FLZ039", "FLZ042", "FLZ043"]),
        ],
    "checkStrings": [
      "FLZ043-052-",
      "/O.CAN.KTBW.HW.W.0001.100102T0900Z-100104T0300Z/",
                     ],
    },
    {
    "commentary": "Canceling out all hazards.",
    "name": "GHG_Complex4_8",
    "productType": None,
    "drtTime":"20100101_0115",
    "checkStrings": [],
    "clearHazardsTable": 1,
    },
    ]
       

import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0000",
        }
    return TestScript.generalTestScript(scripts, defaults)













