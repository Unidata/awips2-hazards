# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# More VTEC_GHG Complex Hazard Tests 
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "GHG_Complex6_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {

    "commentary": """
    Creating a FW.A hazard for Area1
    Area1 (FLZ039, FLZ042) 18-27
    """,
    
    "name": "GHG_Complex6_1",
    "productType": "RFW",
    "createGrids": [
       (18, 27, "FW.A", ["FLZ039", "FLZ042"]),
       ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.NEW.KTBW.FW.A.0001.100101T1800Z-100102T0300Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex6_2",
    "productType": "RFW",
    "createGrids": [
       (18, 27, "FW.A", ["FLZ039", "FLZ042"]),
       ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CON.KTBW.FW.A.0001.100101T1800Z-100102T0300Z/",
                     ],
    },
    {

    "commentary": """
    Upgrading the FW.A hazard in Area1 to a FW.W hazard
    """,
    
    "name": "GHG_Complex6_3",
    "productType": "RFW",
    "createGrids": [
        (18, 27, "FW.W", ["FLZ039", "FLZ042"]),
        ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.UPG.KTBW.FW.A.0001.100101T1800Z-100102T0300Z/",
      "/O.NEW.KTBW.FW.W.0001.100101T1800Z-100102T0300Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex6_4",
    "productType": "RFW",
    "createGrids": [
        (18, 27, "FW.W", ["FLZ039", "FLZ042"]),
        ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CON.KTBW.FW.W.0001.100101T1800Z-100102T0300Z/",
                     ],
    },
    {

    "commentary": """
    Creating a new FW.A hazard for Area1 with a new time range
    Area1 (FLZ039, FLZ042) 42-54
    """,
    
    "name": "GHG_Complex6_5",
    "productType": "RFW",
    "createGrids": [
        (18, 27, "FW.W", ["FLZ039", "FLZ042"]),
        (42, 54, "FW.A", ["FLZ039", "FLZ042"]),],
    "checkStrings": [
      "FLZ039-042-",
      "/O.NEW.KTBW.FW.A.0002.100102T1800Z-100103T0600Z/",
      "/O.CON.KTBW.FW.W.0001.100101T1800Z-100102T0300Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex6_6",
    "productType": "RFW",
    "createGrids": [
        (18, 27, "FW.W", ["FLZ039", "FLZ042"]),
        (42, 54, "FW.A", ["FLZ039", "FLZ042"]),],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CON.KTBW.FW.W.0001.100101T1800Z-100102T0300Z/",
      "/O.CON.KTBW.FW.A.0002.100102T1800Z-100103T0600Z/",
                     ],
    }, 
    {

    "commentary": """
    Changing the FW.W hazard in Area1 to no hazards
    Area1 (FLZ039, FLZ042) 18-27
    """,
    
    "name": "GHG_Complex6_7",
    "productType": "RFW",
    "createGrids": [
        (42, 54, "FW.A", ["FLZ039", "FLZ042"]),],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CAN.KTBW.FW.W.0001.100101T1800Z-100102T0300Z/",
      "/O.CON.KTBW.FW.A.0002.100102T1800Z-100103T0600Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex6_8",
    "productType": "RFW",
    "createGrids": [
        (42, 54, "FW.A", ["FLZ039", "FLZ042"]),],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CON.KTBW.FW.A.0002.100102T1800Z-100103T0600Z/",
                     ],
    },
    {
    "commentary": "Canceling out all hazards.",
    "name": "GHG_Complex6_9",
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







