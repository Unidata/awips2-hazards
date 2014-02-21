# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# More complex Hazards Tests 
#
# Author:
# ----------------------------------------------------------------------------


scripts = [
    {
    "name": "Hazards0",
    "productType": None,
    "commentary": "Deleting hazard grids.",
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "name": "Hazards1",
    "commentary": "Setting up CF.Y for three zones to generate a NEW vtec.",
    "productType": "CFW",
    "createGrids": [
       (22, 28, "CF.Y", ["FLZ048", "FLZ049", "FLZ050"]),
       ],
    "checkStrings": [
       "FLZ048>050-",
       "/O.NEW.KTBW.CF.Y.0001.100101T2200Z-100102T0400Z/",
                  ],
    },
    {
    "name": "Hazards2",
    "productType": "CFW",
    "commentary": "No grid changes, CF.Y in three zones to generate a CON vtec.",
    "createGrids": [
       ( 22, 28, "CF.Y", ["FLZ048", "FLZ049", "FLZ050"]),
       ],
    "checkStrings": [
       "FLZ048>050-",
       "/O.CON.KTBW.CF.Y.0001.100101T2200Z-100102T0400Z/",
                  ],
    },
    {
    "name": "Hazards3",
    "productType": "CFW",
    "commentary": "Extending ending time of the CF.Y in three zones to generate a EXT vtec.",
    "createGrids": [
        (22, 34, "CF.Y", ["FLZ048", "FLZ049", "FLZ050"]),
        ],
    "checkStrings": [
        "FLZ048>050-",
        "/O.EXT.KTBW.CF.Y.0001.100101T2200Z-100102T1000Z/",
                  ],
    },
    {
    "name": "Hazards4",
    "productType": "CFW",
    "commentary": "Moving starting time earlier for the CF.Y in three zones to generate a EXT vtec.",
    "createGrids": [
        (16, 28, "CF.Y", ["FLZ048", "FLZ049", "FLZ050"]),
        ],
    "checkStrings": [
       "FLZ048>050-",
       "/O.EXT.KTBW.CF.Y.0001.100101T1600Z-100102T0400Z/",
                  ],
    },
    {
    "name": "Hazards5",
    "productType": "CFW",
    "commentary": "Adding the CF.Y to one more zone to generate a EXA with CON vtec in two segments.",
    "createGrids": [
        (16, 28, "CF.Y", ["FLZ048", "FLZ049", "FLZ050", "FLZ065"]),
    ],
    "checkStrings": [
        "FLZ065-",
        "/O.EXA.KTBW.CF.Y.0001.100101T1600Z-100102T0400Z/",
        "FLZ048>050-",
        "/O.CON.KTBW.CF.Y.0001.100101T1600Z-100102T0400Z/",
                  ],
    },
    {
    "name": "Hazards6",
    "productType": "CFW",
    "commentary": "Changing the ending time of the CF.Y and adding another zone to generate a EXB with EXT vtec in two segments.",
    "createGrids": [
        (22, 34, "CF.Y", ["FLZ048", "FLZ049", "FLZ050", "FLZ065", "FLZ039"]),
        ],
    "checkStrings": [
       "FLZ039-",
       "/O.EXB.KTBW.CF.Y.0001.100101T2200Z-100102T1000Z/",
       "FLZ048>050-065-",
       "/O.EXT.KTBW.CF.Y.0001.100101T2200Z-100102T1000Z/",
                  ],
    }, 
    {
    "name": "Hazards7",
    "productType": "CFW",
    "commentary": "Removing the CF.Y from two zones, but leaving it in the other three zones to generate a CAN and CON vtec in two segments.",
    "createGrids": [
        (22, 34, "CF.Y", ["FLZ048", "FLZ049", "FLZ050"]),
        ],
    "checkStrings": [
       "FLZ039-065-",
       "/O.CAN.KTBW.CF.Y.0001.100101T2200Z-100102T1000Z/",
       "FLZ048>050-",
       "/O.CON.KTBW.CF.Y.0001.100101T2200Z-100102T1000Z/",
                  ],
    },
    {
    "name": "Hazards8",
    "productType": "CFW",
    "commentary": "Upgrading the CF.Y to a CF.W to geenerate a UPG/NEW vtec.",
    "createGrids": [
        ( 22, 34, "CF.W", ["FLZ048", "FLZ049", "FLZ050"]),
        ],
    "checkStrings": [
        "FLZ048>050-",
        "/O.UPG.KTBW.CF.Y.0001.100101T2200Z-100102T1000Z/",
        "/O.NEW.KTBW.CF.W.0001.100101T2200Z-100102T1000Z/",
                  ],
    },
    {
    "name": "Hazards9",
    "productType": "CFW",
    "commentary": "Adding a new CF.Y event in one zone, while leaving the existing CF.W in the other zones, to generate two segments with a NEW/CON CF.Y/CF.W and CON CF.W.",
    "createGrids": [
        (22, 34, "CF.W", ["FLZ048", "FLZ049", "FLZ050"]),
        (35, 41, "CF.Y", ["FLZ049"]),
        ],
    "checkStrings": [
        "FLZ049-",
        "/O.NEW.KTBW.CF.Y.0002.100102T1100Z-100102T1700Z/",
        "/O.CON.KTBW.CF.W.0001.100101T2200Z-100102T1000Z/",
        "FLZ048-050-",
        "/O.CON.KTBW.CF.W.0001.100101T2200Z-100102T1000Z/",
                  ],
    },
    {
    "name": "Hazards10",
    "productType": "CFW",
    "commentary": "Removing all hazards, to generate CAN statements for CF.W and CF.Y events, in separate segments.",
    "createGrids": [
       ],
    "checkStrings": [
                     "FLZ049-",
                     "/O.CAN.KTBW.CF.W.0001.100101T2200Z-100102T1000Z/",
                     "/O.CAN.KTBW.CF.Y.0002.100102T1100Z-100102T1700Z/",
                     "FLZ048-050-",
                     "/O.CAN.KTBW.CF.W.0001.100101T2200Z-100102T1000Z/",
         ],
    },
    {
    "name": "HazardsCleanup",
    "commentary": "Cleanup of hazards grids and hazard table",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    ]
       

import TestScript as TestScript
def testScript():
    defaults = {
        "gridsStartTime": "20100101_0000",
        "database": "<site>_GRID__Official_00000000_0000",
        "publishGrids": 1,
        "decodeVTEC": 1,
        "orderStrings": 1,
        }
    return TestScript.generalTestScript(scripts, defaults)









