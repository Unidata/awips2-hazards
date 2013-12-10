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
    "name": "GHG_Complex3_0",
    "productType":None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {

    "commentary": """
    Creating a WW.Y hazard for Area1, Area2, Area3, and Area4
    Area1 (FLZ039, FLZ042) 0-30
    Area2 (FLZ048, FLZ049) 0-23
    Area3 (FLZ052, FLZ055) 27-30
    Area4 (FLZ060, FLZ061) 23-30
    Note that we had to condense this to 3 different time ranges to accomodate the overlaps in time
    """,
    
    "name": "GHG_Complex3_1",
    "productType": "WSW",
    "createGrids": [
       (0, 23, "WW.Y", ["FLZ039", "FLZ042", "FLZ048", "FLZ049"]), 
       (23, 27, "WW.Y", ["FLZ039", "FLZ042", "FLZ060", "FLZ061"]),
       (27, 30, "WW.Y", ["FLZ039", "FLZ042", "FLZ052", "FLZ055", "FLZ060", "FLZ061"]),
       ],
    "checkStrings": [
      "FLZ048-049-",
      "/O.NEW.KTBW.WW.Y.0001.100101T0000Z-100101T2300Z/",
      "FLZ039-042-",
      "/O.NEW.KTBW.WW.Y.0001.100101T0000Z-100102T0600Z/",
      "FLZ060-061-",
      "/O.NEW.KTBW.WW.Y.0001.100101T2300Z-100102T0600Z/",
      "FLZ052-055-",
      "/O.NEW.KTBW.WW.Y.0001.100102T0300Z-100102T0600Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex3_2",
    "productType": "WSW",
    "createGrids": [
       (0, 23, "WW.Y", ["FLZ039", "FLZ042", "FLZ048", "FLZ049"]), 
       (23, 27, "WW.Y", ["FLZ039", "FLZ042", "FLZ060", "FLZ061"]),
       (27, 30, "WW.Y", ["FLZ039", "FLZ042", "FLZ052", "FLZ055", "FLZ060", "FLZ061"]),
       ],
    "checkStrings": [
      "FLZ048-049-",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100101T2300Z/",
      "FLZ039-042-",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100102T0600Z/",
      "FLZ060-061-",
      "/O.CON.KTBW.WW.Y.0001.100101T2300Z-100102T0600Z/",
      "FLZ052-055-",
      "/O.CON.KTBW.WW.Y.0001.100102T0300Z-100102T0600Z/",
                     ],
    },
    {
    
    "commentary": """
    Creating a WC.Y hazard for the entire CWA
    Area5 (FLZ039, FLZ042, FLZ043, FLZ048, FLZ049, FLZ050, FLZ051, FLZ052, FLZ055, FLZ056, FLZ057, FLZ060, FLZ061, FLZ062, FLZ065) 0-23
    """,

    "name": "GHG_Complex3_3",
    "productType": "WSW",
    "createGrids": [
        (0, 23, "WC.Y", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051", "FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
        (0, 23, "WC.Y", ["FLZ039", "FLZ042", "FLZ048", "FLZ049"]), #new
        (0, 23, "WW.Y", ["FLZ039", "FLZ042", "FLZ048", "FLZ049"]), #new
        (23, 27, "WW.Y", ["FLZ039", "FLZ042", "FLZ060", "FLZ061"]),
        (27, 30, "WW.Y", ["FLZ039", "FLZ042", "FLZ052", "FLZ055", "FLZ060", "FLZ061"]),
        ],
    "checkStrings": [
      "FLZ048-049-",
      "/O.NEW.KTBW.WC.Y.0001.100101T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100101T2300Z/",
      "FLZ039-042-",
      "/O.NEW.KTBW.WC.Y.0001.100101T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100102T0600Z/",
      "FLZ060-061-",
      "/O.NEW.KTBW.WC.Y.0001.100101T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.100101T2300Z-100102T0600Z/",
      "FLZ052-055-",
      "/O.NEW.KTBW.WC.Y.0001.100101T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.100102T0300Z-100102T0600Z/",
      "FLZ043-050-051-056-057-062-065-",
      "/O.NEW.KTBW.WC.Y.0001.100101T0000Z-100101T2300Z/",
                     ],
    },
    {
    "commentary": "No changes are made to the existing hazards.",
    "name": "GHG_Complex3_4",
    "productType": "WSW",
    "createGrids": [
        (0, 23, "WC.Y", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051", "FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
        (0, 23, "WW.Y", ["FLZ039", "FLZ042", "FLZ048", "FLZ049"]), #new
        (0, 23, "WC.Y", ["FLZ039", "FLZ042", "FLZ048", "FLZ049"]), #new
        (23, 27, "WW.Y", ["FLZ039", "FLZ042", "FLZ060", "FLZ061"]),
        (27, 30, "WW.Y", ["FLZ039", "FLZ042", "FLZ052", "FLZ055", "FLZ060", "FLZ061"]),
        ],
    "checkStrings": [
      "FLZ048-049-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100101T2300Z/",
      "FLZ039-042-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100102T0600Z/",
      "FLZ060-061-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.100101T2300Z-100102T0600Z/",
      "FLZ052-055-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.100102T0300Z-100102T0600Z/",
      "FLZ043-050-051-056-057-062-065-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
                     ],
    },
    {

    "commentary": """
    Removing Area2 and the WW.Y hazard
    Area2 (FLZ048, FLZ049) 0-23
    """,
    
    "name": "GHG_Complex3_5",
    "productType": "WSW",
    "createGrids": [
        (0, 23, "WC.Y", ["FLZ039", "FLZ042", "FLZ043", "FLZ048", "FLZ049", "FLZ050", "FLZ051", "FLZ052", "FLZ055", "FLZ056", "FLZ057", "FLZ060", "FLZ061", "FLZ062", "FLZ065"]),
        (0, 23, "WW.Y", ["FLZ039", "FLZ042"]), 
        (0, 23, "WC.Y", ["FLZ039", "FLZ042"]),
        (23, 27, "WW.Y", ["FLZ039", "FLZ042", "FLZ060", "FLZ061"]),
        (27, 30, "WW.Y", ["FLZ039", "FLZ042", "FLZ052", "FLZ055", "FLZ060", "FLZ061"]),
        ],
    "checkStrings": [
      "FLZ048-049-",
      "/O.CAN.KTBW.WW.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "FLZ039-042-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.000000T0000Z-100102T0600Z/",
      "FLZ060-061-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.100101T2300Z-100102T0600Z/",
      "FLZ052-055-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
      "/O.CON.KTBW.WW.Y.0001.100102T0300Z-100102T0600Z/",
      "FLZ043-050-051-056-057-062-065-",
      "/O.CON.KTBW.WC.Y.0001.000000T0000Z-100101T2300Z/",
                     ],
    },
    {
    "commentary": "Canceling out all hazards.",
    "name": "GHG_Complex3_6",
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







