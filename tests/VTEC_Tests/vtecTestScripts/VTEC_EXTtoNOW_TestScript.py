# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# VTEC EXT time adjust to NOW tests
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "VTEC_EXPtoNow_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "1 zone setup in future",
    "name": "VTEC_EXPtoNow_1a",
    "drtTime": "20100101_0000",
    "productType": "NPW",
    "createGrids": [
       (24, 36, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.NEW.KTBW.DU.Y.0001.100102T0000Z-100102T1200Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - EXT to future",
    "name": "VTEC_EXPtoNow_1b",
    "drtTime": "20100101_0400",
    "productType": "NPW",
    "createGrids": [
       (24, 48, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.EXT.KTBW.DU.Y.0001.100102T0000Z-100103T0000Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - EXT back towards present",
    "name": "VTEC_EXPtoNow_1c",
    "drtTime": "20100101_1300",
    "productType": "NPW",
    "createGrids": [
       (18, 48, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.EXT.KTBW.DU.Y.0001.100101T1800Z-100103T0000Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - EXT back to now",
    "name": "VTEC_EXPtoNow_1d",
    "drtTime": "20100101_1500",
    "productType": "NPW",
    "createGrids": [
       (15, 48, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.EXT.KTBW.DU.Y.0001.100101T1500Z-100103T0000Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - CAN",
    "name": "VTEC_EXPtoNow_1e",
    "drtTime": "20100101_1900",
    "productType": "NPW",
    "createGrids": [
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.CAN.KTBW.DU.Y.0001.000000T0000Z-100103T0000Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future (NEW->CON->EXT test)",
    "name": "VTEC_EXPtoNow_2a",
    "drtTime": "20100102_0000",
    "productType": "NPW",
    "createGrids": [
       (30, 36, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.NEW.KTBW.DU.Y.0002.100102T0600Z-100102T1200Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->CON->EXT test)",
    "name": "VTEC_EXPtoNow_2b",
    "drtTime": "20100102_0200",
    "productType": "NPW",
    "createGrids": [
       (30, 36, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.CON.KTBW.DU.Y.0002.100102T0600Z-100102T1200Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->CON->EXT test)",
    "name": "VTEC_EXPtoNow_2c",
    "drtTime": "20100102_0400",
    "productType": "NPW",
    "createGrids": [
       (28, 36, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.EXT.KTBW.DU.Y.0002.100102T0400Z-100102T1200Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->CON->EXT test)",
    "name": "VTEC_EXPtoNow_2d",
    "drtTime": "20100102_0600",
    "productType": "NPW",
    "createGrids": [
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.CAN.KTBW.DU.Y.0002.000000T0000Z-100102T1200Z/",
       ],
    },


    {
    "commentary": "1 zone setup in future (NEW->EXT test)",
    "name": "VTEC_EXPtoNow_3a",
    "drtTime": "20100103_0000",
    "productType": "NPW",
    "createGrids": [
       (50, 60, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.NEW.KTBW.DU.Y.0003.100103T0200Z-100103T1200Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->EXT test)",
    "name": "VTEC_EXPtoNow_3b",
    "drtTime": "20100103_0100",
    "productType": "NPW",
    "createGrids": [
       (49, 60, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.EXT.KTBW.DU.Y.0003.100103T0100Z-100103T1200Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->EXT test)",
    "name": "VTEC_EXPtoNow_3c",
    "drtTime": "20100103_0200",
    "productType": "NPW",
    "createGrids": [
       (49, 60, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.CON.KTBW.DU.Y.0003.000000T0000Z-100103T1200Z/",
       ],
    },


    {
    "commentary": "1 zone setup in future (EXP,NEW->EXT+ test)",
    "name": "VTEC_EXPtoNow_3d",
    "drtTime": "20100103_1200",
    "productType": "NPW",
    "createGrids": [
       (65, 70, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.EXP.KTBW.DU.Y.0003.000000T0000Z-100103T1200Z/",
      "/O.NEW.KTBW.DU.Y.0004.100103T1700Z-100103T2200Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->EXT+ test)",
    "name": "VTEC_EXPtoNow_3e",
    "drtTime": "20100103_1240",
    "productType": "NPW",
    "createGrids": [
       (65, 80, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.EXT.KTBW.DU.Y.0004.100103T1700Z-100104T0800Z/",
       ],
    },
    {
    "commentary": "1 zone setup in future - (NEW->EXT+ test)",
    "name": "VTEC_EXPtoNow_3f",
    "drtTime": "20100103_1659",
    "productType": "NPW",
    "createGrids": [
       (65, 80, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.CON.KTBW.DU.Y.0004.100103T1700Z-100104T0800Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->EXT+ test)",
    "name": "VTEC_EXPtoNow_3g",
    "drtTime": "20100103_1700",
    "productType": "NPW",
    "createGrids": [
       (65, 80, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.CON.KTBW.DU.Y.0004.000000T0000Z-100104T0800Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->EXT+ test)",
    "name": "VTEC_EXPtoNow_3h",
    "drtTime": "20100103_1701",
    "productType": "NPW",
    "createGrids": [
       (65, 80, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.CON.KTBW.DU.Y.0004.000000T0000Z-100104T0800Z/",
       ],
    },

    {
    "commentary": "1 zone setup in future - (NEW->EXT+ test)",
    "name": "VTEC_EXPtoNow_3i",
    "drtTime": "20100104_0747",
    "productType": "NPW",
    "createGrids": [
       (65, 80, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.EXP.KTBW.DU.Y.0004.000000T0000Z-100104T0800Z/",
       ],
    },

    {
    "commentary": "2 zone setup in future (NEW->EXA test)",
    "name": "VTEC_EXPtoNow_4a",
    "drtTime": "20100105_0000",
    "productType": "NPW",
    "createGrids": [
       (100, 120, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.NEW.KTBW.DU.Y.0005.100105T0400Z-100106T0000Z/",
       ],
    },

    {
    "commentary": "2 zone setup in future (NEW->EXT/EXB test)",
    "name": "VTEC_EXPtoNow_4b",
    "drtTime": "20100105_0218",
    "productType": "NPW",
    "decodeVTEC": 0,
    "createGrids": [
       (100, 130, "DU.Y", ["FLZ049","FLZ057"]),
       ],
    "checkStrings": [
      "FLZ057-",
      "/O.EXB.KTBW.DU.Y.0005.100105T0400Z-100106T1000Z/",
      "FLZ049-",
      "/O.EXT.KTBW.DU.Y.0005.100105T0400Z-100106T1000Z/",
       ],
    },

    {
    "commentary": "2 zone setup in future (NEW->EXT/EXB test)",
    "name": "VTEC_EXPtoNow_4c",
    "drtTime": "20100105_0218",
    "productType": "NPW",
    "decodeVTEC": 0,
    "createGrids": [
       (99, 130, "DU.Y", ["FLZ049","FLZ057"]),
       ],
    "checkStrings": [
       "FLZ057-",
       "/O.EXB.KTBW.DU.Y.0005.100105T0300Z-100106T1000Z/",
       "FLZ049-",
       "/O.EXT.KTBW.DU.Y.0005.100105T0300Z-100106T1000Z/",
       ],
    },

    {
    "commentary": "2 zone setup in future (NEW->EXT/EXB test)",
    "name": "VTEC_EXPtoNow_4d",
    "drtTime": "20100105_0218",
    "productType": "NPW",
    "createGrids": [
       (98, 130, "DU.Y", ["FLZ049","FLZ057"]),
       ],
    "checkStrings": [
       "FLZ057-",
       "/O.EXB.KTBW.DU.Y.0005.100105T0218Z-100106T1000Z/",
       "FLZ049-",
       "/O.EXT.KTBW.DU.Y.0005.100105T0218Z-100106T1000Z/",
       ],
    },

    {
    "commentary": "2 zone setup in future (NEW->EXT/EXB test)",
    "name": "VTEC_EXPtoNow_4e",
    "drtTime": "20100105_0400",
    "productType": "NPW",
    "createGrids": [
       (98, 130, "DU.Y", ["FLZ049","FLZ057"]),
       ],
    "checkStrings": [
      "FLZ049-057-",
      "/O.CON.KTBW.DU.Y.0005.000000T0000Z-100106T1000Z/",
       ],
    },

    {
    "commentary": "2 zone setup in future (NEW->EXT/EXB test)",
    "name": "VTEC_EXPtoNow_4f",
    "drtTime": "20100105_0522",
    "productType": "NPW",
    "createGrids": [
       ],
    "checkStrings": [
      "FLZ049-057-",
      "/O.CAN.KTBW.DU.Y.0005.000000T0000Z-100106T1000Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "VTEC_EXPtoNow_Cleanup",
    "productType": None,
    "checkStrings": [],
    "clearHazardsTable": 1,
    },
    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "publishGrids": 1,
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0000",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)




