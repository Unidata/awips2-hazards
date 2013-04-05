# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# Headlines Timing
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "EXTUPG_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Setting initial event",
    "name": "EXTUPG_1",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (0, 24, "ZR.Y", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.NEW.KTBW.ZR.Y.0001.100101T0510Z-100102T0500Z/",
                     ],
    },

    {
    "commentary": "Upgrading all and then extending",
    "name": "EXTUPG_2",
    "drtTime": "20100101_0520",
    "productType": "WSW",
    "decodeVTEC": 0,   #don't decode
    "createGrids": [
       (0, 24, "BZ.W", ["FLZ050"]),
       (24, 36, "ZR.Y", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.UPG.KTBW.ZR.Y.0001.000000T0000Z-100102T0500Z/",
                     "/O.NEW.KTBW.BZ.W.0001.100101T0520Z-100102T0500Z/",
                     "/O.NEW.KTBW.ZR.Y.0002.100102T0500Z-100102T1700Z/",
                     ],
    },

    {
    "commentary": "Upgrading in middle and then extending",
    "name": "EXTUPG_3",
    "drtTime": "20100101_0520",
    "productType": "WSW",
    "decodeVTEC": 0,   #don't decode
    "createGrids": [
       (0, 12, "ZR.Y", ["FLZ050"]),
       (12, 24, "BZ.W", ["FLZ050"]),
       (24, 36, "ZR.Y", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.NEW.KTBW.BZ.W.0001.100101T1700Z-100102T0500Z/",
                     "/O.NEW.KTBW.ZR.Y.0002.100102T0500Z-100102T1700Z/",
                     "/O.EXT.KTBW.ZR.Y.0001.000000T0000Z-100101T1700Z/",
                     ],
    },

    {
    "commentary": "Normal extention in time - overlapping",
    "name": "EXTUPG_4",
    "drtTime": "20100101_0520",
    "productType": "WSW",
    "decodeVTEC": 0,   #don't decode
    "createGrids": [
       (0, 36, "ZR.Y", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.EXT.KTBW.ZR.Y.0001.000000T0000Z-100102T1700Z/",
                     ],
    },

    {
    "commentary": "Normal extention in time - non-overlapping",
    "name": "EXTUPG_5",
    "drtTime": "20100101_0520",
    "productType": "WSW",
    "decodeVTEC": 0,   #don't decode
    "createGrids": [
       (24, 36, "ZR.Y", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.CAN.KTBW.ZR.Y.0001.000000T0000Z-100102T0500Z/",
                     "/O.NEW.KTBW.ZR.Y.0002.100102T0500Z-100102T1700Z/",
                     ],
    },

    {
    "commentary": "Upgrading in middle",
    "name": "EXTUPG_6",
    "drtTime": "20100101_0520",
    "productType": "WSW",
    "decodeVTEC": 0,   #don't decode
    "createGrids": [
       (0, 12, "ZR.Y", ["FLZ050"]),
       (12, 18, "BZ.W", ["FLZ050"]),
       (18, 24, "ZR.Y", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.NEW.KTBW.BZ.W.0001.100101T1700Z-100101T2300Z/",
                     "/O.NEW.KTBW.ZR.Y.0002.100101T2300Z-100102T0500Z/",
                     "/O.EXT.KTBW.ZR.Y.0001.000000T0000Z-100101T1700Z/",
                     ],
    },

    {
    "commentary": "Cleanup.",
    "name": "EXTUPG_7",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)




