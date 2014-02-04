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
    "name": "Hazard_FFA_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "NEW FFA",
    "name": "Hazard_FFA_1",
    "drtTime": "20100101_0510",
    "productType": "FFA",
    "createGrids": [
       (0, 3, "FA.A", ["FLZ049"], {'immediateCause': 'ER'}),
       ],
    "checkStrings": [
                     "FLZ049-",
                     "/X.NEW.KTBW.FA.A.0001.100101T0510Z-100101T0800Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "CON FFA",
    "name": "Hazard_FFA_2",
    "drtTime": "20100101_0530",
    "productType": "FFA",
    "createGrids": [
       (0, 3, "FA.A", ["FLZ049"], {'immediateCause': 'SM'}),
       ],
    "checkStrings": [
                     "FLZ049-",
                     "/X.CON.KTBW.FA.A.0001.000000T0000Z-100101T0800Z/",
                     "/00000.0.SM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "EXA FFA",
    "name": "Hazard_FFA_3",
    "drtTime": "20100101_0700",
    "productType": "FFA",
    "createGrids": [
       (0, 3, "FA.A", ["FLZ049","FLZ057"], {'immediateCause': 'DM'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.EXA.KTBW.FA.A.0001.000000T0000Z-100101T0800Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.CON.KTBW.FA.A.0001.000000T0000Z-100101T0800Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "CAN FFA, NEW FFA",
    "name": "Hazard_FFA_4",
    "drtTime": "20100101_0720",
    "productType": "FFA",
    "createGrids": [
       (0, 8, "FF.A", ["FLZ057"], {'immediateCause': 'IJ'}),
       (24, 32, "FF.A", ["FLZ057"], {'immediateCause': 'IJ'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.CAN.KTBW.FA.A.0001.000000T0000Z-100101T0800Z/",
                     "/X.NEW.KTBW.FF.A.0001.100101T0720Z-100101T1300Z/",
                     "/00000.0.IJ.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "/X.NEW.KTBW.FF.A.0002.100102T0500Z-100102T1300Z/",
                     "/00000.0.IJ.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.CAN.KTBW.FA.A.0001.000000T0000Z-100101T0800Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "EXP FFA, 2 NEW FFA",
    "name": "Hazard_FFA_5",
    "drtTime": "20100101_1300",
    "productType": "FFA",
    "createGrids": [
       (24, 32, "FF.A", ["FLZ057"], {'immediateCause': 'FS'}),
       (46, 62, "FF.A", ["FLZ057"], {'immediateCause': 'FS'}),
       (45, 46, "FA.A", ["FLZ049"], {'immediateCause': 'FS'}),
       (46, 62, "FA.A", ["FLZ049"], {'immediateCause': 'FS'}),
       (62, 68, "FA.A", ["FLZ049"], {'immediateCause': 'FS'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.EXP.KTBW.FF.A.0001.000000T0000Z-100101T1300Z/",
                     "/00000.0.IJ.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "/X.NEW.KTBW.FF.A.0003.100103T0300Z-100103T1900Z/",
                     "/00000.0.FS.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "/X.CON.KTBW.FF.A.0002.100102T0500Z-100102T1300Z/",
                     "/00000.0.FS.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.NEW.KTBW.FA.A.0002.100103T0200Z-100104T0100Z/",
                     "/00000.0.FS.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "CON test of multiple events",
    "name": "Hazard_FFA_6",
    "drtTime": "20100102_0300",
    "productType": "FFA",
    "createGrids": [
       (24, 32, "FF.A", ["FLZ057"], {'immediateCause': 'RS'}),
       (46, 62, "FF.A", ["FLZ057"], {'immediateCause': 'RS'}),
       (45, 46, "FA.A", ["FLZ049"], {'immediateCause': 'RS'}),
       (46, 62, "FA.A", ["FLZ049"], {'immediateCause': 'RS'}),
       (62, 68, "FA.A", ["FLZ049"], {'immediateCause': 'RS'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.CON.KTBW.FF.A.0002.100102T0500Z-100102T1300Z/",
                     "/00000.0.RS.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "/X.CON.KTBW.FF.A.0003.100103T0300Z-100103T1900Z/",
                     "/00000.0.RS.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.CON.KTBW.FA.A.0002.100103T0200Z-100104T0100Z/",
                     "/00000.0.RS.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "middle of 1st event",
    "name": "Hazard_FFA_7",
    "drtTime": "20100102_0700",
    "productType": "FFA",
    "createGrids": [
       (24, 32, "FF.A", ["FLZ057"], {'immediateCause': 'ER'}),
       (46, 62, "FF.A", ["FLZ057"], {'immediateCause': 'ER'}),
       (45, 46, "FA.A", ["FLZ049"], {'immediateCause': 'ER'}),
       (46, 62, "FA.A", ["FLZ049"], {'immediateCause': 'ER'}),
       (62, 68, "FA.A", ["FLZ049"], {'immediateCause': 'ER'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.CON.KTBW.FF.A.0002.000000T0000Z-100102T1300Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "/X.CON.KTBW.FF.A.0003.100103T0300Z-100103T1900Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.CON.KTBW.FA.A.0002.100103T0200Z-100104T0100Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "joining two events",
    "name": "Hazard_FFA_8",
    "drtTime": "20100102_1200",
    "productType": "FFA",
    "createGrids": [
       (24, 45, "FF.A", ["FLZ057"], {'immediateCause': 'IC'}),
       (45, 62, "FF.A", ["FLZ057"], {'immediateCause': 'IC'}),
       (45, 62, "FA.A", ["FLZ049"], {'immediateCause': 'IC'}),
       (62, 68, "FA.A", ["FLZ049"], {'immediateCause': 'IC'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.CAN.KTBW.FF.A.0002.000000T0000Z-100102T1300Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "/X.EXT.KTBW.FF.A.0003.100102T1200Z-100103T1900Z/",
                     "/00000.0.IC.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.CON.KTBW.FA.A.0002.100103T0200Z-100104T0100Z/",
                     "/00000.0.IC.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "into the tail end of the events",
    "name": "Hazard_FFA_9",
    "drtTime": "20100103_1100",
    "productType": "FFA",
    "createGrids": [
       (24, 45, "FF.A", ["FLZ057"], {'immediateCause': 'SM'}),
       (45, 62, "FF.A", ["FLZ057"], {'immediateCause': 'SM'}),
       (45, 62, "FA.A", ["FLZ049"], {'immediateCause': 'SM'}),
       (62, 68, "FA.A", ["FLZ049"], {'immediateCause': 'SM'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.CON.KTBW.FF.A.0003.000000T0000Z-100103T1900Z/",
                     "/00000.0.SM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.CON.KTBW.FA.A.0002.000000T0000Z-100104T0100Z/",
                     "/00000.0.SM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "exp 1st event, continue 2nd event",
    "name": "Hazard_FFA_10",
    "drtTime": "20100103_1855",
    "productType": "FFA",
    "createGrids": [
       (24, 45, "FF.A", ["FLZ057"], {'immediateCause': 'DR'}),
       (45, 62, "FF.A", ["FLZ057"], {'immediateCause': 'DR'}),
       (45, 62, "FA.A", ["FLZ049"], {'immediateCause': 'DR'}),
       (62, 68, "FA.A", ["FLZ049"], {'immediateCause': 'DR'}),
       ],
    "checkStrings": [
                     "FLZ057-",
                     "/X.EXP.KTBW.FF.A.0003.000000T0000Z-100103T1900Z/",
                     "/00000.0.DR.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLZ049-",
                     "/X.CON.KTBW.FA.A.0002.000000T0000Z-100104T0100Z/",
                     "/00000.0.DR.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },
    {
    "commentary": "cancel 2nd event",
    "name": "Hazard_FFA_11",
    "drtTime": "20100104_0000",
    "productType": "FFA",
    "createGrids": [
       ],
    "checkStrings": [
                     "FLZ049-",
                     "/X.CAN.KTBW.FA.A.0002.000000T0000Z-100104T0100Z/",
                     "/00000.0.DR.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "Hazard_FFA_12",
    "productType": None,
    "checkStrings": [],
    "clearHazardsTable": 1,
    },

    {
    "commentary": "EXP test setup",
    "name": "Hazard_FFA_13",
    "drtTime": "20100101_0510",
    "productType": "FFA",
    "createGrids": [
       (0, 3, "FA.A", ["FLZ049"], {'immediateCause': 'ER'}),
       ],
    "checkStrings": [
                     "FLZ049-",
                     "/X.NEW.KTBW.FA.A.0001.100101T0510Z-100101T0800Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },

    {
    "commentary": "EXP test",
    "name": "Hazard_FFA_14a",
    "drtTime": "20100101_0740",
    "productType": "FFA",
    "decodeVTEC": 0,
    "createGrids": [
       ],
    "checkStrings": [
                     "FLZ049-",
                     "/X.CAN.KTBW.FA.A.0001.000000T0000Z-100101T0800Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },

    {
    "commentary": "EXP test",
    "name": "Hazard_FFA_14b",
    "drtTime": "20100101_0740",
    "productType": "FFA",
    "createGrids": [
       (0, 3, "FA.A", ["FLZ049"], {'immediateCause': 'ER'}),
       ],
    "checkStrings": [
                     "FLZ049-",
                     "/X.EXP.KTBW.FA.A.0001.000000T0000Z-100101T0800Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "Hazard_FFA_15",
    "productType": None,
    "checkStrings": [],
    "clearHazardsTable": 1,
    },

    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "database": "<site>_GRID__Official_00000000_0000",
        "publishGrids": 1,
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "orderStrings": 1,
        "vtecMode": "X",
        "deleteGrids": [("Fcst", "Hazards", "SFC", "all", "all")],
        }
    return TestScript.generalTestScript(scripts, defaults)




