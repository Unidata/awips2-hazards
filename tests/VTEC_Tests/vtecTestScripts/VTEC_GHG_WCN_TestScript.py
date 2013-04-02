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
    "name": "WCN_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Testing midnight issuance - 3 hr event.",
    "name": "WCN_1",
    "drtTime": "20100101_0510",
    "productType": "WCN",
    "createGrids": [
       (0, 3, "TO.A", ["FLC017","GMZ870"], {'forceEtn': 111}),
       ],
    "checkStrings": [
                     "FLC017-",
                     "/E.NEW.KTBW.TO.A.0111.100101T0510Z-100101T0800Z/",
                     "GMZ870-",
                     "/E.NEW.KTBW.TO.A.0111.100101T0510Z-100101T0800Z/",
                     ],
    },

    {
    "commentary": "Testing continuation.",
    "name": "WCN_2",
    "drtTime": "20100101_0530",
    "productType": "WCN",
    "createGrids": [
       (0, 3, "TO.A", ["FLC017","GMZ870"], {'forceEtn': 111}),
       ],
    "checkStrings": [
                     "FLC017-",
                     "/E.CON.KTBW.TO.A.0111.000000T0000Z-100101T0800Z/",
                     "GMZ870-",
                     "/E.CON.KTBW.TO.A.0111.000000T0000Z-100101T0800Z/",
                     ],
    },

    {
    "commentary": "Testing expire before expire time.",
    "name": "WCN_3",
    "drtTime": "20100101_0745",
    "productType": "WCN",
    "decodeVTEC": 0,
    "createGrids": [
       (0, 3, "TO.A", ["FLC017","GMZ870"], {'forceEtn': 111}),
       ],
    "checkStrings": [
                     "FLC017-",
                     "/E.EXP.KTBW.TO.A.0111.000000T0000Z-100101T0800Z/",
                     "GMZ870-",
                     "/E.EXP.KTBW.TO.A.0111.000000T0000Z-100101T0800Z/",
                     ],
    },


    {
    "commentary": "Testing expire after expire time.",
    "name": "WCN_4",
    "drtTime": "20100101_0815",
    "productType": "WCN",
    "createGrids": [
       (0, 3, "TO.A", ["FLC017","GMZ870"], {'forceEtn': 111}),
       ],
    "checkStrings": [
                     "FLC017-",
                     "/E.EXP.KTBW.TO.A.0111.000000T0000Z-100101T0800Z/",
                     "GMZ870-",
                     "/E.EXP.KTBW.TO.A.0111.000000T0000Z-100101T0800Z/",
                     ],
    },

    {
    "commentary": "Testing new issuance of SV.A",
    "name": "WCN_5",
    "drtTime": "20100101_0900",
    "productType": "WCN",
    "createGrids": [
       (0, 7, "SV.A", ["FLC017","GMZ870"], {'forceEtn': 112}),
       ],
    "checkStrings": [
                     "FLC017-",
                     "/E.NEW.KTBW.SV.A.0112.100101T0900Z-100101T1200Z/",
                     "GMZ870-",
                     "/E.NEW.KTBW.SV.A.0112.100101T0900Z-100101T1200Z/",
                     ],
    },
    {
    "commentary": "Testing cancel of SV.A",
    "name": "WCN_6",
    "drtTime": "20100101_1100",
    "productType": "WCN",
    "createGrids": [
       ],
    "checkStrings": [
                     "FLC017-",
                     "/E.CAN.KTBW.SV.A.0112.000000T0000Z-100101T1200Z/",
                     "GMZ870-",
                     "/E.CAN.KTBW.SV.A.0112.000000T0000Z-100101T1200Z/",
                     ],
    },

    {
    "commentary": "Testing new issuance of SV.A",
    "name": "WCN_7",
    "drtTime": "20100101_1205",
    "productType": "WCN",
    "createGrids": [
       (0, 12, "SV.A", ["FLC017","GMZ870"], {'forceEtn': 115}),
       ],
    "checkStrings": [
                     "FLC017-",
                     "/E.NEW.KTBW.SV.A.0115.100101T1205Z-100101T1700Z/",
                     "GMZ870-",
                     "/E.NEW.KTBW.SV.A.0115.100101T1205Z-100101T1700Z/",
                     ],
    },
    {
    "commentary": "Testing EXA SV.A",
    "name": "WCN_8",
    "drtTime": "20100101_1300",
    "productType": "WCN",
    "createGrids": [
       (0, 12, "SV.A", ["FLC017","FLC053","GMZ870"], {'forceEtn': 115}),
       ],
    "checkStrings": [
                     "FLC053-",
                     "/E.EXA.KTBW.SV.A.0115.000000T0000Z-100101T1700Z/",
                     "FLC017-",
                     "/E.CON.KTBW.SV.A.0115.000000T0000Z-100101T1700Z/",
                     "GMZ870-",
                     "/E.CON.KTBW.SV.A.0115.000000T0000Z-100101T1700Z/",
                     ],
    },
    {
    "commentary": "Testing CON after EXA SV.A",
    "name": "WCN_9",
    "drtTime": "20100101_1400",
    "productType": "WCN",
    "createGrids": [
       (0, 12, "SV.A", ["FLC017","FLC053","GMZ870"], {'forceEtn': 115}),
       ],
    "checkStrings": [
                     "FLC017-053-",
                     "/E.CON.KTBW.SV.A.0115.000000T0000Z-100101T1700Z/",
                     "GMZ870-",
                     "/E.CON.KTBW.SV.A.0115.000000T0000Z-100101T1700Z/",
                     ],
    },
    {
    "commentary": "Testing EXT, EXB after EXA SV.A",
    "name": "WCN_10",
    "drtTime": "20100101_1500",
    "productType": "WCN",
    "createGrids": [
       (0, 14, "SV.A", ["FLC101","FLC017","FLC053","GMZ870"], {'forceEtn': 115}),
       ],
    "checkStrings": [
                     "FLC101-",
                     "/E.EXB.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     "FLC017-053-",
                     "/E.EXT.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     "GMZ870-",
                     "/E.EXT.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     ],
    },
    {
    "commentary": "Testing NEW/CAN TO.A",
    "name": "WCN_11",
    "drtTime": "20100101_1700",
    "productType": "WCN",
    "createGrids": [
       (0, 14, "TO.A", ["FLC101","FLC017"], {'forceEtn': 116}),
       (14, 19, "TO.A", ["FLC101","FLC017"], {'forceEtn': 116}),
       (0, 14, "SV.A", ["GMZ870"], {'forceEtn': 115}),
       ],
    "checkStrings": [
                     "FLC017-101-",
                     "/E.CAN.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     "/E.NEW.KTBW.TO.A.0116.100101T1700Z-100102T0000Z/",
                     "FLC053-",
                     "/E.CAN.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     "GMZ870-",
                     "/E.CON.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     ],
    },
    {
    "commentary": "Testing before EXP/EXA TO.A",
    "name": "WCN_12",
    "drtTime": "20100101_1850",
    "decodeVTEC": 0,  #don't decode the VTEC this time
    "productType": "WCN",
    "createGrids": [
       (0, 14, "TO.A", ["FLC101","FLC017"], {'forceEtn': 116}),
       (14, 19, "TO.A", ["FLC101","FLC017","GMZ870"], {'forceEtn': 116}),
       (0, 14, "SV.A", ["GMZ870"], {'forceEtn': 115}),
       ],
    "checkStrings": [
                     "GMZ870-",
                     "/E.EXP.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     "/E.EXB.KTBW.TO.A.0116.100101T1900Z-100102T0000Z/",
                     "FLC017-101-",
                     "/E.CON.KTBW.TO.A.0116.000000T0000Z-100102T0000Z/",
                     ],
       },
{
    "commentary": "Testing after EXP/EXA TO.A",
    "name": "WCN_13",
    "drtTime": "20100101_1910",
    "decodeVTEC": 0,  #don't decode the VTEC this time
    "productType": "WCN",
    "createGrids": [
       (14, 19, "TO.A", ["FLC101","FLC017","GMZ870"], {'forceEtn': 116}),
       ],
    "checkStrings": [
                     "GMZ870-",
                     "/E.EXP.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
                     "/E.EXA.KTBW.TO.A.0116.000000T0000Z-100102T0000Z/",
                     "FLC017-101-",
                     "/E.CON.KTBW.TO.A.0116.000000T0000Z-100102T0000Z/",
                     ],
    },

    {
    "commentary": "Canceling out all hazards.",
    "name": "WCN_14",
    "drtTime": "20100101_1850",
    "productType": "WCN",
    "createGrids": [
       ],
    "checkStrings": [
       "FLC017-101-",
       "/E.CAN.KTBW.TO.A.0116.000000T0000Z-100102T0000Z/",
       "GMZ870-",
       "/E.CAN.KTBW.SV.A.0115.000000T0000Z-100101T1900Z/",
       ],
    },
    
    {
    "commentary": "Deleting hazard grids.",
    "name": "WCN_15",
    "productType": None,
    "clearHazardsTable": 1,
    },
    ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "vtecMode": "E",
        }
    return TestScript.generalTestScript(scripts, defaults)




