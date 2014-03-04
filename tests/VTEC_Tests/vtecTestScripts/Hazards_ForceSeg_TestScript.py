# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# Forcing segments.
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "ForceSeg_1a",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Initial with different eventIDs",
    "name": "ForceSeg_1b",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'eventID': 602}),
       ],
    "checkStrings": [
       "FLZ039-042-043-",
       "/O.NEW.KTBW.WS.A.0001.100101T0510Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "CON using different eventIDs",
    "name": "ForceSeg_1c",
    "drtTime": "20100101_0710",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'eventID': 602}),
       ],
    "checkStrings": [
       "FLZ039-042-043-",
       "/O.CON.KTBW.WS.A.0001.000000T0000Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "Cleanup.",
    "name": "ForceSeg_2a",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Initial with same eventIDs but different force segments",
    "name": "ForceSeg_2b",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'forceSeg': 1, 'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'forceSeg': 2, 'eventID': 601}),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.NEW.KTBW.WS.A.0001.100101T0510Z-100103T0500Z/",
       "FLZ043-",
       "/O.NEW.KTBW.WS.A.0001.100101T0510Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "CON using same eventIDs but different force segments",
    "name": "ForceSeg_2c",
    "drtTime": "20100101_0710",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'forceSeg': 1, 'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'forceSeg': 2, 'eventID': 601}),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.CON.KTBW.WS.A.0001.000000T0000Z-100103T0500Z/",
       "FLZ043-",
       "/O.CON.KTBW.WS.A.0001.000000T0000Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "Cleanup.",
    "name": "ForceSeg_3a",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },


    {
    "commentary": "Initial with different eventIDs and force segments",
    "name": "ForceSeg_3b",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'forceSeg': 1, 'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'forceSeg': 2, 'eventID': 602}),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.NEW.KTBW.WS.A.0001.100101T0510Z-100103T0500Z/",
       "FLZ043-",
       "/O.NEW.KTBW.WS.A.0001.100101T0510Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "CON using different eventIDs and force segments",
    "name": "ForceSeg_3c",
    "drtTime": "20100101_0710",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'forceSeg': 1, 'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'forceSeg': 2, 'eventID': 602}),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.CON.KTBW.WS.A.0001.000000T0000Z-100103T0500Z/",
       "FLZ043-",
       "/O.CON.KTBW.WS.A.0001.000000T0000Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "Cleanup.",
    "name": "ForceSeg_4a",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },


    {
    "commentary": "Initial with no eventID and no forceSeg",
    "name": "ForceSeg_4b",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'eventID': 601})
       ],
    "checkStrings": [
       "FLZ039-042-043-",
       "/O.NEW.KTBW.WS.A.0001.100101T0510Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "Adding eventID and forcing Segment",
    "name": "ForceSeg_4c",
    "drtTime": "20100101_0710",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"], {'forceSeg': 1, 'eventID': 601}),
       (0, 48, "WS.A", ["FLZ043"], {'forceSeg': 2, 'eventID': 601}),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.CON.KTBW.WS.A.0001.000000T0000Z-100103T0500Z/",
       "FLZ043-",
       "/O.CON.KTBW.WS.A.0001.000000T0000Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "Cleanup.",
    "name": "ForceSeg_End",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)



