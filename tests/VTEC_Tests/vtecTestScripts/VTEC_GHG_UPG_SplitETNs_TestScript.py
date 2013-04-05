# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# Headlines Timing
#
# Author:
# ----------------------------------------------------------------------------
#DR20138 translation from CLE zones to TBW zones
OHZ003 = "FLZ009"
OHZ006 = "FLZ010"
OHZ007 = "FLZ011"
OHZ008 = "FLZ012"
OHZ009 = "FLZ013"
OHZ010 = "FLZ014"
OHZ011 = "FLZ015"
OHZ012 = "FLZ016"
OHZ013 = "FLZ017"
OHZ014 = "FLZ018"
OHZ017 = "FLZ019"
OHZ018 = "FLZ020"
OHZ019 = "FLZ021"
OHZ020 = "FLZ022"
OHZ021 = "FLZ023"
OHZ022 = "FLZ024"
OHZ023 = "FLZ025"
OHZ027 = "FLZ026"
OHZ028 = "FLZ027"
OHZ029 = "FLZ028"
OHZ030 = "FLZ029"
OHZ031 = "FLZ030"
OHZ032 = "FLZ031"
OHZ033 = "FLZ032"
OHZ036 = "FLZ033"
OHZ037 = "FLZ034"
OHZ038 = "FLZ035"
OHZ047 = "FLZ036"
OHZ089 = "FLZ037"
PAZ001 = "FLZ038"
PAZ002 = "FLZ039"
PAZ003 = "FLZ040"

#DR20138 - defaultEditAreas, to expand past standard set of zones for TBW
def1 = """
    Definition['displayName'] = None
"""
def2 = """
    Definition['displayName'] = None
    Definition["defaultEditAreas"] = [
 ("FLZ009", "ONE"),
 ("FLZ010", "TWO"),
 ("FLZ011", "THREE"),
 ("FLZ012", "FOUR"),
 ("FLZ013", "FIVE"),
 ("FLZ014", "SIX"),
 ("FLZ015", "SEVEN"),
 ("FLZ016", "EIGHT"),
 ("FLZ017", "NINE"),
 ("FLZ018", "TEN"),
 ("FLZ019", "ELEVEN"),
 ("FLZ020", "TWELVE"),
 ("FLZ021", "THIRTEEN"),
 ("FLZ022", "FOURTEEN"),
 ("FLZ023", "FIFTEEN"),
 ("FLZ024", "SIXTEEN"),
 ("FLZ025", "SEVENTEEN"),
 ("FLZ026", "EIGHTEEN"),
 ("FLZ027", "NINETEEN"),
 ("FLZ028", "TWENTY"),
 ("FLZ029", "TWENTYONE"),
 ("FLZ030", "TWENTYTWO"),
 ("FLZ031", "TWENTYTHREE"),
 ("FLZ032", "TWENTYFOUR"),
 ("FLZ033", "TWENTYFIVE"),
 ("FLZ034", "TWENTYSIX"),
 ("FLZ035", "TWENTYSEVEN"),
 ("FLZ036", "TWENTYEIGHT"),
 ("FLZ037", "TWENTYNINE"),
 ("FLZ038", "THIRTY"),
 ("FLZ039", "THIRTYONE"),
 ("FLZ040", "THIRTYTWO"),]

"""

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "SplitETN_1a",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Initial WS.A",
    "name": "SplitETN_1b",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (0, 48, "WS.A", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.NEW.KTBW.WS.A.0001.100101T0510Z-100103T0500Z/",
       ],
    },

    {
    "commentary": "EXT WS.A with WS.W in the middle",
    "name": "SplitETN_1c",
    "drtTime": "20100101_0710",
    "productType": "WSW",
    "createGrids": [
       (0, 24, "WS.A", ["FLZ039","FLZ042"]),
       (24, 36, "WS.W", ["FLZ039","FLZ042"]),
       (36, 48, "WS.A", ["FLZ039","FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.NEW.KTBW.WS.W.0001.100102T0500Z-100102T1700Z/",
       "/O.NEW.KTBW.WS.A.0002.100102T1700Z-100103T0500Z/",
       "/O.EXT.KTBW.WS.A.0001.000000T0000Z-100102T0500Z/",
       ],
    },

    {
    "commentary": "upgrade 1st WS.A to WS.W" ,
    "name": "SplitETN_1d",
    "drtTime": "20100101_0810",
    "productType": "WSW",
    "createGrids": [
       (0, 24, "WS.W", ["FLZ039", "FLZ042"]),
       (24, 36, "WS.W", ["FLZ039", "FLZ042"]),
       (36, 48, "WS.A", ["FLZ039", "FLZ042"]),
       ],
    "checkStrings": [
       "FLZ039-042-",
       "/O.CAN.KTBW.WS.A.0001.000000T0000Z-100102T0500Z/",
       "/O.EXT.KTBW.WS.W.0001.100101T0810Z-100102T1700Z/",
       "/O.CON.KTBW.WS.A.0002.100102T1700Z-100103T0500Z/",
       ],
    },


    {
    "commentary": "Deleting hazard grids.",
    "name": "SplitETN_1e",
    "productType": "WSW",
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "SplitETN_DR20138 setup existing hazards (double events, auto seg)- NEW",
    "name": "SplitETN_DR20138_4a",
    "clearHazardsTable": 1,
    "drtTime": "20100307_1632",
    "gridsStartTime": "20100307_0000",
    "productType": "WSW",
    "createGrids": [
       (16, 48, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (96, 144, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
    ],
    "checkStrings": [
      "FLZ039-042-043-048>050-",
      "/O.NEW.KTBW.WS.W.0001.100307T1632Z-100309T0000Z/",
      "/O.NEW.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
       ],
    },
    {
    "commentary": "SplitETN_DR20138 setup existing hazards (double events, auto seg)- CONs",
    "name": "SplitETN_DR20138_4b",
    "drtTime": "20100307_1632",
    "gridsStartTime": "20100307_0000",
    "productType": "WSW",
    "createGrids": [
       (16, 48, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (96, 144, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
    ],
    "checkStrings": [
      "FLZ039-042-043-048>050-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100309T0000Z/",
      "/O.CON.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
       ],
    },
    {
    "commentary": "SplitETN_DR20138 subtest - double events, double split",
    "name": "SplitETN_DR20138_4c",
    "drtTime": "20100307_2126",
    "gridsStartTime": "20100307_0000",
    "productType": "WSW",
    "createGrids": [
       (16, 24, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (24, 36, "BZ.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (36, 48, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (96, 108, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (108, 128, "BZ.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (128, 144, "WS.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
     ],
    "checkStrings": [
      "FLZ039-042-043-048>050-",
      "/O.NEW.KTBW.BZ.W.0001.100308T0000Z-100308T1200Z/",
      "/O.NEW.KTBW.WS.W.0003.100308T1200Z-100309T0000Z/",
      "/O.NEW.KTBW.BZ.W.0002.100311T1200Z-100312T0800Z/",
      "/O.NEW.KTBW.WS.W.0004.100312T0800Z-100313T0000Z/",
      "/O.EXT.KTBW.WS.W.0001.000000T0000Z-100308T0000Z/",
      "/O.EXT.KTBW.WS.W.0002.100311T0000Z-100311T1200Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "SplitETN_DR20138_4d",
    "productType": "WSW",
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "SplitETN_DR20138 setup existing hazards (double event, force segs)- NEW",
    "name": "SplitETN_DR20138_5a",
    "clearHazardsTable": 1,
    "drtTime": "20100307_1632",
    "gridsStartTime": "20100307_0000",
    "productType": "WSW",
    "createGrids": [
       (16, 48, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (16, 48, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (16, 48, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
       (96, 144, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (96, 144, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (96, 144, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
    ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.NEW.KTBW.WS.W.0001.100307T1632Z-100309T0000Z/",
      "/O.NEW.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
      "FLZ043-048-",
      "/O.NEW.KTBW.WS.W.0001.100307T1632Z-100309T0000Z/",
      "/O.NEW.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
      "FLZ049-050-",
      "/O.NEW.KTBW.WS.W.0001.100307T1632Z-100309T0000Z/",
      "/O.NEW.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
       ],
    },
    {
    "commentary": "SplitETN_DR20138 setup existing hazards (double event, force seg)- CONs",
    "name": "SplitETN_DR20138_5b",
    "drtTime": "20100307_1632",
    "gridsStartTime": "20100307_0000",
    "productType": "WSW",
    "createGrids": [
       (16, 48, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (16, 48, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (16, 48, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
       (96, 144, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (96, 144, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (96, 144, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
    ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100309T0000Z/",
      "/O.CON.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
      "FLZ043-048-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100309T0000Z/",
      "/O.CON.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
      "FLZ049-050-",
      "/O.CON.KTBW.WS.W.0001.000000T0000Z-100309T0000Z/",
      "/O.CON.KTBW.WS.W.0002.100311T0000Z-100313T0000Z/",
       ],
    },
    {
    "commentary": "SplitETN_DR20138 subtest (double event, force segs) - double split",
    "name": "SplitETN_DR20138_5c",
    "drtTime": "20100307_2126",
    "gridsStartTime": "20100307_0000",
    "productType": "WSW",
    "createGrids": [
       (16, 24, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (16, 24, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (16, 24, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
       (24, 36, "BZ.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (36, 48, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (36, 48, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (36, 48, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
       (96, 108, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (96, 108, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (96, 108, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
       (108, 128, "BZ.W", ['FLZ039','FLZ042','FLZ043','FLZ048','FLZ049','FLZ050']),
       (128, 144, "WS.W", ['FLZ039','FLZ042'], {'forceSeg': 1}),
       (128, 144, "WS.W", ['FLZ043','FLZ048'], {'forceSeg': 2}),
       (128, 144, "WS.W", ['FLZ049','FLZ050'], {'forceSeg': 3}),
     ],
    "checkStrings": [
      "FLZ039-042-",
      "/O.NEW.KTBW.BZ.W.0001.100308T0000Z-100308T1200Z/",
      "/O.NEW.KTBW.WS.W.0003.100308T1200Z-100309T0000Z/",
      "/O.NEW.KTBW.BZ.W.0002.100311T1200Z-100312T0800Z/",
      "/O.NEW.KTBW.WS.W.0004.100312T0800Z-100313T0000Z/",
      "/O.EXT.KTBW.WS.W.0001.000000T0000Z-100308T0000Z/",
      "/O.EXT.KTBW.WS.W.0002.100311T0000Z-100311T1200Z/",
      "FLZ043-048-",
      "/O.NEW.KTBW.BZ.W.0001.100308T0000Z-100308T1200Z/",
      "/O.NEW.KTBW.WS.W.0003.100308T1200Z-100309T0000Z/",
      "/O.NEW.KTBW.BZ.W.0002.100311T1200Z-100312T0800Z/",
      "/O.NEW.KTBW.WS.W.0004.100312T0800Z-100313T0000Z/",
      "/O.EXT.KTBW.WS.W.0001.000000T0000Z-100308T0000Z/",
      "/O.EXT.KTBW.WS.W.0002.100311T0000Z-100311T1200Z/",
      "FLZ049-050-",
      "/O.NEW.KTBW.BZ.W.0001.100308T0000Z-100308T1200Z/",
      "/O.NEW.KTBW.WS.W.0003.100308T1200Z-100309T0000Z/",
      "/O.NEW.KTBW.BZ.W.0002.100311T1200Z-100312T0800Z/",
      "/O.NEW.KTBW.WS.W.0004.100312T0800Z-100313T0000Z/",
      "/O.EXT.KTBW.WS.W.0001.000000T0000Z-100308T0000Z/",
      "/O.EXT.KTBW.WS.W.0002.100311T0000Z-100311T1200Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "SplitETN_DR20138_5d",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Setting initial event",
    "name": "DR20311_TwoVTEC_1",
    "drtTime": "20100101_0510",
    "productType": "MWW",
    "createGrids": [
       (0, 24, "SI.Y", ["GMZ876"]),
       ],
    "checkStrings": [
       "GMZ876-",
       "/O.NEW.KTBW.SI.Y.0001.100101T0510Z-100102T0500Z/",
                     ],
    },

    {
    "commentary": "Adding SI.Y to zone 2 starting now and lasting 6 hours",
    "name": "DR20311_TwoVTEC_2",
    "drtTime": "20100101_0520",
    "productType": "MWW",
    "createGrids": [
       (0, 6, "SI.Y", ["GMZ876","GMZ873"]),
       (6, 24, "SI.Y", ["GMZ876"]),
       ],
    "checkStrings": [
       "GMZ873-",
       "/O.EXB.KTBW.SI.Y.0001.000000T0000Z-100101T1100Z/",
       "GMZ876-",
       "/O.CON.KTBW.SI.Y.0001.000000T0000Z-100102T0500Z/",
                     ],
    },

    {
    "commentary": "Add new SI.Y to zone 2 starting 12h from now and lasting 6",
    "name": "DR20311_TwoVTEC_3a",
    "drtTime": "20100101_0530",
    "productType": "MWW",
    "decodeVTEC": 0,
    "createGrids": [
       (0, 6, "SI.Y", ["GMZ876","GMZ873"]),
       (6, 12, "SI.Y", ["GMZ876"]),
       (12, 18, "SI.Y", ["GMZ876","GMZ873"]),
       (18, 24, "SI.Y", ["GMZ876"]),
       ],
    "checkStrings": [
        "GMZ873-",
        "/O.CON.KTBW.SI.Y.0001.000000T0000Z-100101T1100Z/",
        "/O.NEW.KTBW.SI.Y.0002.100101T1700Z-100101T2300Z/",
        "GMZ876-",
        "/O.CON.KTBW.SI.Y.0001.000000T0000Z-100102T0500Z/",
                     ],
    },

    {
    "commentary": "Extend 1st grid for z2 by 1 hour.",
    "name": "DR20311_TwoVTEC_3b",
    "drtTime": "20100101_0530",
    "productType": "MWW",
    "decodeVTEC": 0,
    "createGrids": [
       (0, 7, "SI.Y", ["GMZ876","GMZ873"]),
       (7, 12, "SI.Y", ["GMZ876"]),
       (12, 18, "SI.Y", ["GMZ876","GMZ873"]),
       (18, 24, "SI.Y", ["GMZ876"]),
       ],
    "checkStrings": [
        "GMZ873-",
        "/O.EXT.KTBW.SI.Y.0001.000000T0000Z-100101T1200Z/",
        "/O.NEW.KTBW.SI.Y.0002.100101T1700Z-100101T2300Z/",
        "GMZ876-",
        "/O.CON.KTBW.SI.Y.0001.000000T0000Z-100102T0500Z/",
                     ],
    },


    {
    "commentary": "Cleanup.",
    "name": "DR20311_TwoVTEC_7",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Setting initial events",
    "name": "DR21090_TwoVTEC_Setup",
    "clearHazardsTable": 1,
    "drtTime": "20200320_1210",
    "productType": "NPW",
    "gridsStartTime": "20200320_0000",
    "createGrids": [
       (16, 44, "WI.Y", ["FLZ042"]),
       (44, 72, "WI.Y", ["FLZ042", "FLZ039"]),
       ],
    "checkStrings": [
        "FLZ042-",
        "/O.NEW.KTBW.WI.Y.0001.200320T1600Z-200323T0000Z/",
        "FLZ039-",
        "/O.NEW.KTBW.WI.Y.0001.200321T2000Z-200323T0000Z/",
                     ],
    },

    {
    "commentary": "Add new WI.Y to zone 2 before existing event.",
    "name": "DR21090_TwoVTEC_EXB_CON",
    "drtTime": "20200320_1500",
    "productType": "NPW",
    "gridsStartTime": "20200320_0000",
    "createGrids": [
       (16, 24, "WI.Y", ["FLZ042", "FLZ039"]),
       (24, 44, "WI.Y", ["FLZ042"]),
       (44, 72, "WI.Y", ["FLZ042", "FLZ039"]),
       ],
    "decodeVTEC": 0,
    "checkStrings": [
       "FLZ039-",
       "/O.NEW.KTBW.WI.Y.0002.200320T1600Z-200321T0000Z/",
       "/O.CON.KTBW.WI.Y.0001.200321T2000Z-200323T0000Z/",
       "FLZ042-",
       "/O.CON.KTBW.WI.Y.0001.200320T1600Z-200323T0000Z/",
       ],
    },

    {
    "commentary": "Add new WI.Y to zone 2 before and extend existing event.",
    "name": "DR21090_TwoVTEC_EXB_EXT",
    "drtTime": "20200320_1500",
    "productType": "NPW",
    "gridsStartTime": "20200320_0000",
    "createGrids": [
       (16, 24, "WI.Y", ["FLZ042", "FLZ039"]),
       (24, 44, "WI.Y", ["FLZ042"]),
       (44, 70, "WI.Y", ["FLZ042", "FLZ039"]),
       ],
    "decodeVTEC": 0,
    "checkStrings": [
        "FLZ039-",
        "/O.NEW.KTBW.WI.Y.0002.200320T1600Z-200321T0000Z/",
        "/O.EXT.KTBW.WI.Y.0001.200321T2000Z-200322T2200Z/",
        "FLZ042-",
        "/O.EXT.KTBW.WI.Y.0001.200320T1600Z-200322T2200Z/",
                     ],
    },

    {
    "commentary": "Cleanup.",
    "name": "DR21090_Cleanup",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Setting initial event FZ.A",
    "name": "DR20850_1",
    "drtTime": "20100403_1447",
    "gridsStartTime": "20100406_0000",
    "productType": "NPW",
    "createGrids": [
       (5, 48+13, "FZ.A", ["FLZ048","FLZ049","FLZ052","FLZ056","FLZ061","FLZ062"]),
       ],
    "checkStrings": [
       "FLZ048-049-052-056-061-062-",
       "/O.NEW.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",

                     ],
    },
    {
    "commentary": "Adding the WI.Y",
    "name": "DR20850_2",
    "drtTime": "20100404_1607",
    "gridsStartTime": "20100406_0000",
    "productType": "NPW",
    "createGrids": [
       (5, 48+13, "FZ.A", ["FLZ048","FLZ049","FLZ052","FLZ056","FLZ061","FLZ062"]),
       (-48, -48+21, "WI.Y", ["FLZ048","FLZ049"]),
       ],
    "checkStrings": [
       "FLZ048-049-",
       "/O.NEW.KTBW.WI.Y.0001.100404T1607Z-100404T2100Z/",
       "/O.CON.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",
       "FLZ052-056-061-062-",
       "/O.CON.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",
                     ],
    },
    {
    "commentary": "Extending the WI.Y",
    "name": "DR20850_3",
    "drtTime": "20100404_2050",
    "gridsStartTime": "20100406_0000",
    "productType": "NPW",
    "createGrids": [
       (5, 48+13, "FZ.A", ["FLZ048","FLZ049","FLZ052","FLZ056","FLZ061","FLZ062"]),
       (-48, -48+23, "WI.Y", ["FLZ048","FLZ049"]),
       ],
    "checkStrings": [
       "FLZ048-049-",
       "/O.EXT.KTBW.WI.Y.0001.000000T0000Z-100404T2300Z/",
       "/O.CON.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",
       "FLZ052-056-061-062-",
       "/O.CON.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",
                     ],
    },
    {
    "commentary": "Expiring the WI.Y",
    "name": "DR20850_4",
    "drtTime": "20100404_2253",
    "gridsStartTime": "20100406_0000",
    "productType": "NPW",
    "createGrids": [
       (5, 48+13, "FZ.A", ["FLZ048","FLZ049","FLZ052","FLZ056","FLZ061","FLZ062"]),
       (-48, -48+23, "WI.Y", ["FLZ048","FLZ049"]),
       ],
    "checkStrings": [
       "FLZ048-049-",
       "/O.EXP.KTBW.WI.Y.0001.000000T0000Z-100404T2300Z/",
       "/O.CON.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",
       "FLZ052-056-061-062-",
       "/O.CON.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",
                     ],
    },
    {
    "commentary": "Issuing the FZ.A again",
    "name": "DR20850_5",
    "drtTime": "20100404_2308",
    "gridsStartTime": "20100406_0000",
    "productType": "NPW",
    "createGrids": [
       (5, 48+13, "FZ.A", ["FLZ048","FLZ049","FLZ052","FLZ056","FLZ061","FLZ062"]),
       ],
    "checkStrings": [
       "FLZ048-049-052-056-061-062-",
       "/O.CON.KTBW.FZ.A.0001.100406T0500Z-100408T1300Z/",
                     ],
    },
    {
    "commentary": "Switching to multiple FZ.A events, plus a FZ.W",
    "name": "DR20850_6",
    "drtTime": "20100405_0644",
    "gridsStartTime": "20100406_0000",
    "productType": "NPW",
    "createGrids": [
       (6, 14, "FZ.W", ["FLZ052","FLZ056","FLZ061","FLZ062"]),
       (6, 14, "FZ.A", ["FLZ048", "FLZ049"]),
       (6, 14, "FZ.A", ["FLZ048", "FLZ049"]),
       (24+0, 24+14, "FZ.A", ["FLZ048", "FLZ049", "FLZ052","FLZ056","FLZ061","FLZ062"]),
       (48+6, 48+14, "FZ.A", ["FLZ048", "FLZ049", "FLZ052","FLZ056","FLZ061","FLZ062"]),
       ],
    "checkStrings": [
       "FLZ052-056-061-062-",
       "/O.NEW.KTBW.FZ.W.0001.100406T0600Z-100406T1400Z/",
       "/O.NEW.KTBW.FZ.A.0002.100408T0600Z-100408T1400Z/",
       "/O.EXT.KTBW.FZ.A.0001.100407T0000Z-100407T1400Z/",
       "FLZ048-049-",
       "/O.NEW.KTBW.FZ.A.0003.100407T0000Z-100407T1400Z/",
       "/O.NEW.KTBW.FZ.A.0002.100408T0600Z-100408T1400Z/",
       "/O.EXT.KTBW.FZ.A.0001.100406T0600Z-100406T1400Z/",
                     ],
    },

    {
    "commentary": "CON into 2nd FZ.A event",
    "name": "DR20850_7",
    "drtTime": "20100407_0201",
    "gridsStartTime": "20100406_0000",
    "productType": "NPW",
    "createGrids": [
       (6, 14, "FZ.W", ["FLZ052","FLZ056","FLZ061","FLZ062"]),
       (6, 14, "FZ.A", ["FLZ048", "FLZ049"]),
       (24+0, 24+14, "FZ.A", ["FLZ048", "FLZ049", "FLZ052","FLZ056","FLZ061","FLZ062"]),
       (48+6, 48+14, "FZ.A", ["FLZ048", "FLZ049", "FLZ052","FLZ056","FLZ061","FLZ062"]),
       ],
    "checkStrings": [
                    "FLZ048-049-",
                    "/O.CON.KTBW.FZ.A.0003.000000T0000Z-100407T1400Z/",
                    "/O.CON.KTBW.FZ.A.0002.100408T0600Z-100408T1400Z/",
                    "FLZ052-056-061-062-",
                    "/O.CON.KTBW.FZ.A.0001.000000T0000Z-100407T1400Z/",
                    "/O.CON.KTBW.FZ.A.0002.100408T0600Z-100408T1400Z/",
                     ],
    },

    {
    "commentary": "Cleanup.",
    "name": "DR20850_8",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Split grids in one zone, continuous in 2nd zone",
    "name": "DR21233_Reentrant_1",
    "clearHazardsTable": 1,
    "decodeVTEC": 0,
    "drtTime": "20200320_1210",
    "productType": "MWW",
    "gridsStartTime": "20200320_0000",
    "createGrids": [
       (16, 32, "SC.Y", ["GMZ850", "GMZ853"]),
       (32, 64, "SC.Y", ["GMZ853"]),
       (64, 80, "SC.Y", ["GMZ850", "GMZ853"]),
       ],
    "checkStrings": [
        "GMZ850-",
        "/O.NEW.KTBW.SC.Y.0001.200320T1600Z-200321T0800Z/",
        "/O.NEW.KTBW.SC.Y.0002.200322T1600Z-200323T0800Z/",
        "GMZ853-",
        "/O.NEW.KTBW.SC.Y.0001.200320T1600Z-200323T0800Z/",
        ],
    },

    {
    "commentary": "Grid in 2nd zone is adjacent to grids in 1st zone.",
    "name": "DR21233_Reentrant_2",
    "clearHazardsTable": 1,
    "decodeVTEC": 0,
    "drtTime": "20200320_1210",
    "productType": "MWW",
    "gridsStartTime": "20200320_0000",
    "createGrids": [
       (16, 32, "SC.Y", ["GMZ850"]),
       (32, 64, "SC.Y", ["GMZ853"]),
       (64, 80, "SC.Y", ["GMZ850"]),
       ],
    "checkStrings": [
        "GMZ850-",
        "/O.NEW.KTBW.SC.Y.0001.200320T1600Z-200321T0800Z/",
        "/O.NEW.KTBW.SC.Y.0002.200322T1600Z-200323T0800Z/",
        "GMZ853-",
        "/O.NEW.KTBW.SC.Y.0001.200321T0800Z-200322T1600Z/",
        ],
    },

    {
    "commentary": "Add grid to 3rd zone with same time range as 2nd grid in 1st zone",
    "name": "DR21233_Reentrant_3",
    "clearHazardsTable": 1,
    "decodeVTEC": 0,
    "drtTime": "20200320_1210",
    "productType": "MWW",
    "gridsStartTime": "20200320_0000",
    "createGrids": [
       (16, 32, "SC.Y", ["GMZ850"]),
       (32, 64, "SC.Y", ["GMZ853"]),
       (64, 80, "SC.Y", ["GMZ850", "GMZ856"]),
       ],
    "checkStrings": [
        "GMZ850-",
        "/O.NEW.KTBW.SC.Y.0001.200320T1600Z-200321T0800Z/",
        "/O.NEW.KTBW.SC.Y.0002.200322T1600Z-200323T0800Z/",
        "GMZ853-",
        "/O.NEW.KTBW.SC.Y.0001.200321T0800Z-200322T1600Z/",
        "GMZ856-",
        "/O.NEW.KTBW.SC.Y.0002.200322T1600Z-200323T0800Z/",
        ],
    },

    {
    "commentary": "One more wave",
    "name": "DR21233_Reentrant_4",
    "clearHazardsTable": 1,
    "decodeVTEC": 0,
    "drtTime": "20200320_1210",
    "productType": "MWW",
    "gridsStartTime": "20200320_0000",
    "createGrids": [
       (16, 32, "SC.Y", ["GMZ850"]),
       (32, 64, "SC.Y", ["GMZ853"]),
       (64, 80, "SC.Y", ["GMZ850", "GMZ856"]),
       (80, 112, "SC.Y", ["GMZ853"]),
       (112, 128, "SC.Y", ["GMZ850", "GMZ856"]),
       ],
    "checkStrings": [
        "GMZ850-",
        "/O.NEW.KTBW.SC.Y.0001.200320T1600Z-200321T0800Z/",
        "/O.NEW.KTBW.SC.Y.0002.200322T1600Z-200323T0800Z/",
        "/O.NEW.KTBW.SC.Y.0003.200324T1600Z-200325T0800Z/",
        "GMZ853-",
        "/O.NEW.KTBW.SC.Y.0001.200321T0800Z-200322T1600Z/",
        "/O.NEW.KTBW.SC.Y.0002.200323T0800Z-200324T1600Z/",
        "GMZ856-",
        "/O.NEW.KTBW.SC.Y.0002.200322T1600Z-200323T0800Z/",
        "/O.NEW.KTBW.SC.Y.0003.200324T1600Z-200325T0800Z/",
        ],
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




