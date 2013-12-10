# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# More VT?EC_GHG Complex Hazard Tests 
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.", 
    "name": "GHG_Complex5_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    
    "commentary": """
    Creating a WS.A hazard for Area1, Area2, Area3, and Area4
    Note that the ETN number illustrates the Area (:1, :2, :3, :4)
    During this run Area3 and Area4 are combined into the same segment
    Area1 (FLZ039, FLZ042) 24-53
    Area2 (FLZ052) 24-53
    Area3 (FLZ061) 24-53
    Area4 (FLZ062) 24-53
    """,
    
    "name": "GHG_Complex5_1",
    "productType": "WSW",
    "createGrids": [
        (24, 53, "WS.A", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (24, 53, "WS.A", ["FLZ052"], {'forceSeg': 2}),
        (24, 53, "WS.A", ["FLZ061", "FLZ062"], {'forceSeg': 3}),
        ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.NEW.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "FLZ052-",
      "/O.NEW.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "FLZ061-062-",
      "/O.NEW.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex5_2",
    "productType": "WSW",
    "createGrids": [
        (24, 53, "WS.A", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (24, 53, "WS.A", ["FLZ052"], {'forceSeg': 2}),
        (24, 53, "WS.A", ["FLZ061", "FLZ062"], {'forceSeg': 3}),
        ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CON.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "FLZ052-",
      "/O.CON.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "FLZ061-062-",
      "/O.CON.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
                     ],
    },
    {

    "commentary": """
    Upgrading the WS.A hazard to a WS.W hazard in all Areas
    Area2, Area3, and Area4 have new start times
    Area2 (FLZ052) 27-53
    Area3 (FLZ061) 27-53
    Area4 (FLZ062) 36-53
    Note that we had to condense the times into 3 time ranges to accomodate the overlapping
    """,
    
    "name": "GHG_Complex5_3",
    "productType": "WSW",
    "createGrids": [
        (24, 27, "WS.W", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (27, 36, "WS.W", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (27, 36, "WS.W", ["FLZ052"], {'forceSeg': 2}),
        (27, 36, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (36, 53, "WS.W", ["FLZ052"], {'forceSeg': 2}),
        (36, 53, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ062"], {'forceSeg': 4}),
        ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.UPG.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "/O.NEW.KTBW.WS.W.0001.100102T0000Z-100103T0500Z/",
      "FLZ052-",
      "/O.UPG.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "/O.NEW.KTBW.WS.W.0001.100102T0300Z-100103T0500Z/",
      "FLZ061-",
      "/O.UPG.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "/O.NEW.KTBW.WS.W.0001.100102T0300Z-100103T0500Z/",
      "FLZ062-",
      "/O.UPG.KTBW.WS.A.0001.100102T0000Z-100103T0500Z/",
      "/O.NEW.KTBW.WS.W.0001.100102T1200Z-100103T0500Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex5_4",
    "productType": "WSW",
    "createGrids": [
        (24, 27, "WS.W", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (27, 36, "WS.W", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (27, 36, "WS.W", ["FLZ052"], {'forceSeg': 2}),
        (27, 36, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ039", "FLZ042"], {'forceSeg': 1}),
        (36, 53, "WS.W", ["FLZ052"], {'forceSeg': 2}),
        (36, 53, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ062"], {'forceSeg': 4}),
        ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CON.KTBW.WS.W.0001.100102T0000Z-100103T0500Z/",
      "FLZ052-",
      "/O.CON.KTBW.WS.W.0001.100102T0300Z-100103T0500Z/",
      "FLZ061-",
      "/O.CON.KTBW.WS.W.0001.100102T0300Z-100103T0500Z/",
      "FLZ062-",
      "/O.CON.KTBW.WS.W.0001.100102T1200Z-100103T0500Z/",
                     ],
    },
    {

    "commentary": """
    Replacing a portion of the WS.W hazard in Area1 with a BZ.W hazard
    Area1 (FLZ042) 24-53
    Replacing the WS.W hazard in Area2 with a WW.Y hazard
    Area2 (FLZ052) 27-53
    """,
    
    "name": "GHG_Complex5_5",
    "productType": "WSW",
    "createGrids": [
        (24, 27, "WS.W", ["FLZ039"], {'forceSeg': 1}),
        (24, 27, "BZ.W", ["FLZ042"]),
        (24, 27, "WS.W", ["FLZ042"], {'forceSeg': 1}),
        (27, 36, "WS.W", ["FLZ039"], {'forceSeg': 1}),
        (27, 36, "BZ.W", ["FLZ042"]),
        (27, 36, "WS.W", ["FLZ042"], {'forceSeg': 1}),
        (27, 36, "WW.Y", ["FLZ052"]),
        (27, 36, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ039"], {'forceSeg': 1}),
        (36, 53, "BZ.W", ["FLZ042"]),
        (36, 53, "WS.W", ["FLZ042"], {'forceSeg': 1}),
        (36, 53, "WW.Y", ["FLZ052"]),
        (36, 53, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ062"], {'forceSeg': 4}),
        ],
    "checkStrings": [
      "FLZ042-",
      "/O.CAN.KTBW.WS.W.0001.100102T0000Z-100103T0500Z/",
      "/O.NEW.KTBW.BZ.W.0001.100102T0000Z-100103T0500Z/",
      "FLZ052-",
      "/O.CAN.KTBW.WS.W.0001.100102T0300Z-100103T0500Z/",
      "/O.NEW.KTBW.WW.Y.0001.100102T0300Z-100103T0500Z/",
      "FLZ039-",
      "/O.CON.KTBW.WS.W.0001.100102T0000Z-100103T0500Z/",
      "FLZ061-",
      "/O.CON.KTBW.WS.W.0001.100102T0300Z-100103T0500Z/",
      "FLZ062-",
      "/O.CON.KTBW.WS.W.0001.100102T1200Z-100103T0500Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex5_6",
    "productType": "WSW",
    "createGrids": [
        (24, 27, "WS.W", ["FLZ039"], {'forceSeg': 1}),
        (24, 27, "BZ.W", ["FLZ042"]),
        (24, 27, "WS.W", ["FLZ042"], {'forceSeg': 1}),
        (27, 36, "WS.W", ["FLZ039"], {'forceSeg': 1}),
        (27, 36, "BZ.W", ["FLZ042"]),
        (27, 36, "WS.W", ["FLZ042"], {'forceSeg': 1}),
        (27, 36, "WW.Y", ["FLZ052"]),
        (27, 36, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ039"], {'forceSeg': 1}),
        (36, 53, "BZ.W", ["FLZ042"]),
        (36, 53, "WS.W", ["FLZ042"], {'forceSeg': 1}),
        (36, 53, "WW.Y", ["FLZ052"]),
        (36, 53, "WS.W", ["FLZ061"], {'forceSeg': 3}),
        (36, 53, "WS.W", ["FLZ062"], {'forceSeg': 4}),
        ],
    "checkStrings": [
      "FLZ042-",
      "/O.CON.KTBW.BZ.W.0001.100102T0000Z-100103T0500Z/",
      "FLZ039-",
      "/O.CON.KTBW.WS.W.0001.100102T0000Z-100103T0500Z/",
      "FLZ061-",
      "/O.CON.KTBW.WS.W.0001.100102T0300Z-100103T0500Z/",
      "FLZ062-",
      "/O.CON.KTBW.WS.W.0001.100102T1200Z-100103T0500Z/",
      "FLZ052-",
      "/O.CON.KTBW.WW.Y.0001.100102T0300Z-100103T0500Z/",
                     ],
    }, 
    {
    "commentary": "Canceling out all hazards.",
    "name": "GHG_Complex5_7",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    ]
       

import TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0000",
        }
    return TestScript.generalTestScript(scripts, defaults)



