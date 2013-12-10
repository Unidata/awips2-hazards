# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# ETN REuse test case - CAR
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "ETNReuse_1a",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Initial setup of two GL.As",
    "name": "ETNReuse_1b",
    "drtTime": "20101203_0200",
    "gridsStartTime": "20101203_0000",
    "productType": "MWW",
    "createGrids": [
       (0, 7, "GL.A", ["GMZ870"]),
       (18, 24+15, "GL.A", ["GMZ870"]),
       ],
    "checkStrings": [
       "GMZ870-",
       "/O.NEW.KTBW.GL.A.0001.101203T0200Z-101203T0700Z/",
       "/O.NEW.KTBW.GL.A.0002.101203T1800Z-101204T1500Z/",
       ],
    },

    {
    "commentary": "Initial setup of two GL.As - switch to CONs - P1",
    "name": "ETNReuse_1c",
    "drtTime": "20101203_0202",
    "gridsStartTime": "20101203_0000",
    "productType": "MWW",
    "createGrids": [
       (0, 7, "GL.A", ["GMZ870"]),
       (18, 24+15, "GL.A", ["GMZ870"]),
       ],
    "checkStrings": [
       "GMZ870-",
       "/O.CON.KTBW.GL.A.0001.000000T0000Z-101203T0700Z/",
       "/O.CON.KTBW.GL.A.0002.101203T1800Z-101204T1500Z/",
       ],
    },

    {
    "commentary": "1stGL.A EXP, adjust remaining GL.A to allow a SR.A - P2" ,
    "name": "ETNReuse_1d",
    "drtTime": "20101203_0820",
    "gridsStartTime": "20101203_0000",
    "productType": "MWW",
    "createGrids": [
       (8, 18, "GL.A", ["GMZ870"]),
       (18, 24+3, "SR.A", ["GMZ870"]),
       (24+3, 24+16, "GL.A", ["GMZ870"]),
       ],
    "checkStrings": [
      "GMZ870-",
      "/O.NEW.KTBW.GL.A.0003.101203T0820Z-101203T1800Z/",
      "/O.NEW.KTBW.SR.A.0001.101203T1800Z-101204T0300Z/",
      "/O.EXT.KTBW.GL.A.0002.101204T0300Z-101204T1600Z/",
       ],
    },

    {
    "commentary": "CAN SR.A, EXT the GL.A - P3",
    "name": "ETNReuse_1e",
    "drtTime": "20101203_1940",
    "gridsStartTime": "20101203_0000",
    "productType": "MWW",
    "createGrids": [
       (0, 7, "GL.A", ["GMZ870"]),
       (19, 24+15, "GL.A", ["GMZ870"]),
       ],
    "checkStrings": [
      "GMZ870-",
      "/O.CAN.KTBW.SR.A.0001.000000T0000Z-101204T0300Z/",
      "/O.EXT.KTBW.GL.A.0002.101203T1940Z-101204T1500Z/",
       ],
    },

    {
    "commentary": "NEW GL.A and SR.A",
    "name": "ETNReuse_1f",
    "drtTime": "20101206_0834",
    "gridsStartTime": "20101206_0000",
    "productType": "MWW",
    "createGrids": [
       (24+1, 24+11, "SR.A", ["GMZ870"]),
       (24+11, 24+23, "GL.A", ["GMZ870"]),
       ],
    "checkStrings": [
      "GMZ870-",
      "/O.NEW.KTBW.SR.A.0002.101207T0100Z-101207T1100Z/",
      "/O.NEW.KTBW.GL.A.0004.101207T1100Z-101207T2300Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "ETNReuse_1g",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

# Shannon NHDW test case, split, create, delete, create, ETN codes and action
    {
    "commentary": "Deleting hazard grids.",
    "name": "ETNReuse_2a",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Step1-initial HT.Y, etn0001",
    "name": "ETNReuse_2b",
    "drtTime": "20100706_2056",
    "gridsStartTime": "20100706_0000",
    "cmdLineVars": None,
    "productType": "NPW",
    "createGrids": [
       (21, 22, "HT.Y", ["FLZ052"]),
       ],
    "checkStrings": [
      "FLZ052-",
      "/O.NEW.KTBW.HT.Y.0001.100706T2100Z-100706T2200Z/",
       ],
    },

    {
    "commentary": "Step2-initial HT.Y, etn0002",
    "name": "ETNReuse_2c",
    "drtTime": "20101206_2056",
    "gridsStartTime": "20101206_0000",
    "cmdLineVars": None,
    "comboFlag": 0,
    "combinations": None,
    "productType": "NPW",
    "createGrids": [
       (21, 48+9, "HT.Y", ["FLZ050","FLZ051"]),
       ],
    "checkStrings": [
      "FLZ050-051-",
      "/O.NEW.KTBW.HT.Y.0002.101206T2100Z-101208T0900Z/",
       ],
    },


    {
    "commentary": "Step3-insert EH.W in middle of HT.Y",
    "name": "ETNReuse_2d",
    "drtTime": "20101206_2104",
    "gridsStartTime": "20101206_0000",
    "cmdLineVars": None,
    "comboFlag": 0,
    "combinations": None,
    "productType": "NPW",
    "createGrids": [
       (21, 24+8, "HT.Y", ["FLZ050","FLZ051"]),
       (24+8, 24+17, "EH.W", ["FLZ050","FLZ051"]),
       (24+17, 48+9, "HT.Y", ["FLZ050","FLZ051"]),
       ],
    "checkStrings": [
      "FLZ050-051-",
      "/O.NEW.KTBW.EH.W.0001.101207T0800Z-101207T1700Z/",
      "/O.NEW.KTBW.HT.Y.0003.101207T1700Z-101208T0900Z/",
      "/O.EXT.KTBW.HT.Y.0002.000000T0000Z-101207T0800Z/",
       ],
    },

    {
    "commentary": "Step4-delete first HT.Y, leaving EH.W and HT.Y",
    "name": "ETNReuse_2e",
    "drtTime": "20101206_2107",
    "gridsStartTime": "20101206_0000",
    "cmdLineVars": None,
    "comboFlag": 0,
    "combinations": None,
    "productType": "NPW",
    "createGrids": [
       (24+8, 24+17, "EH.W", ["FLZ050","FLZ051"]),
       (24+17, 48+9, "HT.Y", ["FLZ050","FLZ051"]),
       ],
    "checkStrings": [
       "FLZ050-051-",
       "/O.CAN.KTBW.HT.Y.0002.000000T0000Z-101207T0800Z/",
       "/O.CON.KTBW.EH.W.0001.101207T0800Z-101207T1700Z/",
       "/O.CON.KTBW.HT.Y.0003.101207T1700Z-101208T0900Z/",
       ],
    },

    {
    "commentary": "Step5-create HT.Y again in 1st slot",
    "name": "ETNReuse_2f",
    "drtTime": "20101206_2113",
    "gridsStartTime": "20101206_0000",
    "cmdLineVars": None,
    "comboFlag": 0,
    "combinations": None,
    "productType": "NPW",
    "createGrids": [
       (23, 24+8, "HT.Y", ["FLZ050","FLZ051"]),
       (24+8, 24+17, "EH.W", ["FLZ050","FLZ051"]),
       (24+17, 48+9, "HT.Y", ["FLZ050","FLZ051"]),
       ],
    "checkStrings": [
      "FLZ050-051-",
      "/O.NEW.KTBW.HT.Y.0004.101206T2300Z-101207T0800Z/",
      "/O.CON.KTBW.EH.W.0001.101207T0800Z-101207T1700Z/",
      "/O.CON.KTBW.HT.Y.0003.101207T1700Z-101208T0900Z/",
       ],
    },

    {
    "commentary": "Step6-remove first HT.Y",
    "name": "ETNReuse_2g",
    "drtTime": "20101206_2115",
    "gridsStartTime": "20101206_0000",
    "cmdLineVars": None,
    "comboFlag": 0,
    "combinations": None,
    "productType": "NPW",
    "createGrids": [
       (24+8, 24+17, "EH.W", ["FLZ050","FLZ051"]),
       (24+17, 48+9, "HT.Y", ["FLZ050","FLZ051"]),
       ],
    "checkStrings": [
      "FLZ050-051-",
      "/O.CAN.KTBW.HT.Y.0004.101206T2300Z-101207T0800Z/",
      "/O.CON.KTBW.EH.W.0001.101207T0800Z-101207T1700Z/",
      "/O.CON.KTBW.HT.Y.0003.101207T1700Z-101208T0900Z/",
       ],
    },

    {
    "commentary": "Step7-put back in part of HT.Y in 1st slot",
    "name": "ETNReuse_2h",
    "drtTime": "20101206_2134",
    "gridsStartTime": "20101206_0000",
    "cmdLineVars": None,
    "comboFlag": 0,
    "combinations": None,
    "productType": "NPW",
    "createGrids": [
       (21, 24+1, "HT.Y", ["FLZ050","FLZ051"]),
       (24+8, 24+17, "EH.W", ["FLZ050","FLZ051"]),
       (24+17, 48+9, "HT.Y", ["FLZ050","FLZ051"]),
       ],
    "checkStrings": [
      "FLZ050-051-",
      "/O.NEW.KTBW.HT.Y.0005.101206T2134Z-101207T0100Z/",
      "/O.CON.KTBW.EH.W.0001.101207T0800Z-101207T1700Z/",
      "/O.CON.KTBW.HT.Y.0003.101207T1700Z-101208T0900Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "ETNReuse_2i",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "database": "<site>_GRID__Official_00000000_0000",
        "publishGrids": 1,
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)




