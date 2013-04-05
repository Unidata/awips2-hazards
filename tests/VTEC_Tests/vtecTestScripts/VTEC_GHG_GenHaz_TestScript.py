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
    "name": "Hazard_GenHaz_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "NEW GenHaz",
    "name": "Hazard_GenHaz_1",
    "drtTime": "20100101_0510",
    "productType": "NPW",
    "createGrids": [
       (3, 5, "DU.Y", ["FLZ049"]),
       ],
    "checkStrings": [
      "FLZ049-",
      "/O.NEW.KTBW.DU.Y.0001.100101T0800Z-100101T1000Z/",
       ],
    },

    {
    "commentary": "EXT GenHaz",
    "name": "Hazard_GenHaz_2",
    "drtTime": "20100101_0530",
    "productType": "NPW",
    "createGrids": [
       (3, 12, "DS.W", ["FLZ049"]),
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.UPG.KTBW.DU.Y.0001.100101T0800Z-100101T1000Z/",
       "/O.NEW.KTBW.DS.W.0001.100101T0800Z-100101T1700Z/",
       ],
    },

    {
    "commentary": "EXA/EXT GenHaz",
    "name": "Hazard_GenHaz_3",
    "drtTime": "20100101_0830",
    "productType": "NPW",
    "createGrids": [
       (3, 15, "DS.W", ["FLZ049", "FLZ057"]),
       ],
    "checkStrings": [
       "FLZ057-",
       "/O.EXB.KTBW.DS.W.0001.000000T0000Z-100101T2000Z/",
       "FLZ049-",
       "/O.EXT.KTBW.DS.W.0001.000000T0000Z-100101T2000Z/",
       ],
    },

    {
    "commentary": "downgrade GenHaz",
    "name": "Hazard_GenHaz_4",
    "drtTime": "20100101_1100",
    "productType": "NPW",
    "createGrids": [
       (3, 15, "DU.Y", ["FLZ049", "FLZ057"]),
       ],
    "checkStrings": [
       "FLZ049-057-",
       "/O.CAN.KTBW.DS.W.0001.000000T0000Z-100101T2000Z/",
       "/O.NEW.KTBW.DU.Y.0002.100101T1100Z-100101T2000Z/",
       ],
    },

    {
    "commentary": "CAN 1 area GenHaz, NEW watch",
    "name": "Hazard_GenHaz_5",
    "drtTime": "20100101_1400",
    "productType": "NPW",
    "createGrids": [
       (3, 15, "DU.Y", ["FLZ057"]),
       (49, 75, "HW.A", ["FLZ057"]),
       ],
    "checkStrings": [
       "FLZ049-",
       "/O.CAN.KTBW.DU.Y.0002.000000T0000Z-100101T2000Z/",
       "FLZ057-",
       "/O.NEW.KTBW.HW.A.0001.100103T0600Z-100104T0800Z/",
       "/O.CON.KTBW.DU.Y.0002.000000T0000Z-100101T2000Z/",
       ],
    },

    {
    "commentary": "EXP GenHaz before",
    "name": "Hazard_GenHaz_6",
    "drtTime": "20100101_1950",
    "productType": "NPW",
    "createGrids": [
       (3, 15, "DU.Y", ["FLZ057"]),
       (45, 75, "HW.A", ["FLZ057"]),
       ],
    "decodeVTEC": 0,  #turn vtec decoder off
    "checkStrings": [
       "FLZ057-",
       "/O.EXP.KTBW.DU.Y.0002.000000T0000Z-100101T2000Z/",
       "/O.EXT.KTBW.HW.A.0001.100103T0200Z-100104T0800Z/",
       ],
    },

    {
    "commentary": "EXP GenHaz after, EXT/EXA HW.A",
    "name": "Hazard_GenHaz_7",
    "drtTime": "20100101_2000",
    "productType": "NPW",
    "createGrids": [
       (3, 15, "DU.Y", ["FLZ057"]),
       (45, 75, "HW.A", ["FLZ057"]),
       ],
    "checkStrings": [
       "FLZ057-",
       "/O.EXP.KTBW.DU.Y.0002.000000T0000Z-100101T2000Z/",
       "/O.EXT.KTBW.HW.A.0001.100103T0200Z-100104T0800Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "Hazard_GenHaz_Cleanup1",
    "productType": None,
    "checkStrings": [],
    "clearHazardsTable": 1,
    },

    {
    "commentary": "Words for 2 NEWS",
    "name": "Hazard_GenHaz_8",
    "drtTime": "20100101_1100",
    "decodeVTEC": 0,  #turn vtec decoder off
    "productType": "NPW",
    "createGrids": [
       (6, 12, "DU.Y", ["FLZ057"]),
       (18, 24, "HW.A", ["FLZ057"]),
       ],
    "checkStrings": [
       "FLZ057-",
       "/O.NEW.KTBW.DU.Y.0001.100101T1100Z-100101T1700Z/",
       "/O.NEW.KTBW.HW.A.0001.100101T2300Z-100102T0500Z/",
       ],
    },

    {
    "commentary": "Words for 3 NEWS",
    "name": "Hazard_GenHaz_9",
    "drtTime": "20100101_1100",
    "decodeVTEC": 0,  #turn vtec decoder off
    "productType": "NPW",
    "createGrids": [
       (6, 12, "DU.Y", ["FLZ057"]),
       (18, 24, "HW.A", ["FLZ057"]),
       (36, 48, "HW.A", ["FLZ057"]),
       ],
    "checkStrings": [
       "FLZ057-",
       "/O.NEW.KTBW.DU.Y.0001.100101T1100Z-100101T1700Z/",
       "/O.NEW.KTBW.HW.A.0001.100101T2300Z-100102T0500Z/",
       "/O.NEW.KTBW.HW.A.0002.100102T1700Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "Hazard_GenHaz_Cleanup",
    "productType": None,
    "checkStrings": [],
    "clearHazardsTable": 1,
    },
    ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "publishGrids": 1,
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)




