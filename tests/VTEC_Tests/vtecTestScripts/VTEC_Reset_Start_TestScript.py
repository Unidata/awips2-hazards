# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
#
# Author:
# ----------------------------------------------------------------------------
    
scripts = [

    {
    "commentary": "Start WW.Y for 24 hours.",
    "name": "DR21060_SetUp",
    "drtTime": "20200120_0510",
    "productType": "WSW",
    "clearHazardsTable": 1,
    "decodeVTEC": 1,
    "createGrids": [
       (0, 24, "WW.Y", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.NEW.KTBW.WW.Y.0001.200120T0510Z-200121T0500Z/",
       ],
    },

    {
    "commentary": "Split the event in two. No CANcel as start time didn't change.",
    "name": "DR21060_Split",
    "drtTime": "20200120_0520",
    "productType": "WSW",
    "createGrids": [
       (0, 10, "WW.Y", ["FLZ039","FLZ042"]),
       (12, 24, "WW.Y", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.NEW.KTBW.WW.Y.0002.200120T1700Z-200121T0500Z/",
       "/O.EXT.KTBW.WW.Y.0001.000000T0000Z-200120T1500Z/",
       ],
    },

    {
    "commentary": "Delay the start.",
    "name": "DR21060_Delay_Start",
    "drtTime": "20200120_0520",
    "productType": "WSW",
    "createGrids": [
       (2, 10, "WW.Y", ["FLZ039","FLZ042"]),
       (12, 24, "WW.Y", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.CAN.KTBW.WW.Y.0001.000000T0000Z-200121T0500Z/",
       "/O.NEW.KTBW.WW.Y.0002.200120T0700Z-200120T1500Z/",
       "/O.NEW.KTBW.WW.Y.0003.200120T1700Z-200121T0500Z/",
       ],
    },

    {
    "commentary": "Upgrade the early portion to BZ.W.",
    "name": "DR21060_Upgrade_1",
    "drtTime": "20200120_0520",
    "productType": "WSW",
    "createGrids": [
       (0, 12, "BZ.W", ["FLZ039","FLZ042"]),
       (12, 24, "WW.Y", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.UPG.KTBW.WW.Y.0001.000000T0000Z-200121T0500Z/",
       "/O.NEW.KTBW.BZ.W.0001.200120T0520Z-200120T1700Z/",
       "/O.NEW.KTBW.WW.Y.0002.200120T1700Z-200121T0500Z/",
       ],
    },

    {
    "commentary": "Start the upgrade later and end it sooner.",
    "name": "DR21060_Upgrade_2",
    "drtTime": "20200120_0520",
    "productType": "WSW",
    "createGrids": [
       (3, 10, "WS.W", ["FLZ039","FLZ042"]),
       (12, 24, "WW.Y", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.UPG.KTBW.WW.Y.0001.000000T0000Z-200121T0500Z/",
       "/O.NEW.KTBW.WS.W.0001.200120T0800Z-200120T1500Z/",
       "/O.NEW.KTBW.WW.Y.0002.200120T1700Z-200121T0500Z/",
       ],
    },

    {
    "commentary": "Upgrade comes at the tail end.",
    "name": "DR21060_Upgrade_3",
    "drtTime": "20200120_0520",
    "productType": "WSW",
    "createGrids": [
       (2, 12, "WW.Y", ["FLZ039","FLZ042"]),
       (15, 20, "BZ.W", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.UPG.KTBW.WW.Y.0001.000000T0000Z-200121T0500Z/",
       "/O.NEW.KTBW.BZ.W.0001.200120T2000Z-200121T0100Z/",
       "/O.NEW.KTBW.WW.Y.0002.200120T0700Z-200120T1700Z/",
       ],
    },
    
    {
    "commentary": "Replace an early portion with WC.Y.",
    "name": "DR21060_Replacement",
    "drtTime": "20200120_0520",
    "productType": "WSW",
    "createGrids": [
       (2, 10, "WC.Y", ["FLZ039","FLZ042"]),
       (12, 24, "WW.Y", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.CAN.KTBW.WW.Y.0001.000000T0000Z-200121T0500Z/",
       "/O.NEW.KTBW.WC.Y.0001.200120T0700Z-200120T1500Z/",
       "/O.NEW.KTBW.WW.Y.0002.200120T1700Z-200121T0500Z/",
       ],
    },
    
    {
    "commentary": "Cleanup.",
    "name": "Cleanup",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 0,
        "gridsStartTime": "20200120_0500",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)
