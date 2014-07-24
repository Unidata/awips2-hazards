# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# TOR test script
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "TOR_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Single TOR issuance",
    "name": "TOR_1a",
    "drtTime": "20150501_0000",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.TO.W.0001.150501T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single TOR update",
    "name": "TOR_1b",
    "drtTime": "20150501_0010",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single TOR cancel",
    "name": "TOR_1c",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0020",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'status': 'ending', 'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Single TOR expire",
    "name": "TOR_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0025",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.50, "TO.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Single TOR expire after event ending",
    "name": "TOR_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0035",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.50, "TO.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Deleting hazard grids.",
    "name": "TOR_1e",
    "productType": "TOR_SVR_SVS",
    "checkStrings": [],
    "clearHazardsTable": 1,
    },

    {
    "commentary": "Single TOR issuance",
    "name": "TOR_2a",
    "drtTime": "20150501_0000",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.TO.W.0001.150501T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "A second TOR issuance, only mentioning the second",
    "name": "TOR_2b",
    "drtTime": "20150501_0003",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 26}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.TO.W.0002.150501T0003Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Updating just the 1st TOR",
    "name": "TOR_2c",
    "decodeVTEC": 0,
    "drtTime": "20150501_0009",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Updating both TORs",
    "name": "TOR_2d",
    "drtTime": "20150501_0015",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 25}),
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 26}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.TO.W.0002.000000T0000Z-150501T0030Z/",
                     "FLC049-",
                     "/O.CON.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Issue a new one, cancel one, ignore the 2nd one",
    "name": "TOR_2e",
    "drtTime": "20150501_0022",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 25, 'status': 'ending'}),
       (0.25, 0.75, "TO.W", ["FLC049"], {'eventID': 27}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     "FLC049-",
                     "/O.NEW.KTBW.TO.W.0003.150501T0022Z-150501T0045Z/",
                     ],
    },


    {
    "commentary": "Do an expire on the 2nd TOR, ignore the others",
    "name": "TOR_2f",
    "drtTime": "20150501_0028",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 26}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.TO.W.0002.000000T0000Z-150501T0030Z/",
                     ],
    },


    {
    "commentary": "do a followup on the 3rd",
    "name": "TOR_2g",
    "drtTime": "20150501_0032",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.75, "TO.W", ["FLC049"], {'eventID': 27}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.TO.W.0003.000000T0000Z-150501T0045Z/",
                     ],
    },


    {
    "commentary": "do a followup on the 3rd after it expires",
    "name": "TOR_2g",
    "drtTime": "20150501_0049",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.75, "TO.W", ["FLC049"], {'eventID': 27}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.TO.W.0003.000000T0000Z-150501T0045Z/",
                     ],
    },

    {
    "commentary": "issue 3 TORs at same time, in different counties.",
    "name": "TOR_3a",
    "drtTime": "20150501_0100",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (1, 1.75, "TO.W", ["FLC049"], {'eventID': 31}),
       (1, 1.50, "TO.W", ["FLC049"], {'eventID': 32}),
       (1, 1.75, "TO.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.NEW.KTBW.TO.W.0006.150501T0100Z-150501T0145Z/",
                     "FLC049-",
                     "/O.NEW.KTBW.TO.W.0005.150501T0100Z-150501T0145Z/",
                     "FLC049-",
                     "/O.NEW.KTBW.TO.W.0004.150501T0100Z-150501T0130Z/",
                     ],
    },

    {
    "commentary": "cancel 2 of the 3 TO.W events",
    "name": "TOR_3b",
    "drtTime": "20150501_0110",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (1, 1.75, "TO.W", ["FLC049"], {'eventID': 31}),
       (1, 1.50, "TO.W", ["FLC049"], {'eventID': 32, 'status':'ending'}),
       (1, 1.75, "TO.W", ["FLC050"], {'eventID': 33, 'status':'ending'}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.CAN.KTBW.TO.W.0006.000000T0000Z-150501T0145Z/",
                     "FLC049-",
                     "/O.CAN.KTBW.TO.W.0004.000000T0000Z-150501T0130Z/",
                     "FLC049-",
                     "/O.CON.KTBW.TO.W.0005.000000T0000Z-150501T0145Z/",
                     ],
    },

    {
    "commentary": "update the remaining one",
    "name": "TOR_3c",
    "drtTime": "20150501_0115",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (1, 1.75, "TO.W", ["FLC049"], {'eventID': 31}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.TO.W.0005.000000T0000Z-150501T0145Z/",
                     ],
    },

    {
    "commentary": "issue TO.W for 2 counties",
    "name": "TOR_4a",
    "drtTime": "20150501_0200",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "TO.W", ["FLC049", "FLC050"], {'eventID': 32}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.NEW.KTBW.TO.W.0007.150501T0200Z-150501T0230Z/",
                     ],

    },

    {
    "commentary": "issue another TO.W, ignoring the first one",
    "name": "TOR_4b",
    "drtTime": "20150501_0210",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.75, "TO.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.NEW.KTBW.TO.W.0008.150501T0210Z-150501T0245Z/",
                     ],

    },

    {
    "commentary": "update all TO.Ws",
    "name": "TOR_4c",
    "drtTime": "20150501_0215",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "TO.W", ["FLC049", "FLC050"], {'eventID': 32}),
       (2, 2.75, "TO.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.CON.KTBW.TO.W.0008.000000T0000Z-150501T0245Z/",
                     "FLC049-050-",
                     "/O.CON.KTBW.TO.W.0007.000000T0000Z-150501T0230Z/",
                     ],

    },
    {
    "commentary": "cancel 1st TO.W",
    "name": "TOR_4d",
    "decodeVTEC": 0,
    "drtTime": "20150501_0218",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "TO.W", ["FLC049", "FLC050"], {'eventID': 32, 'status': 'ending'}),
       (2, 2.75, "TO.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.CAN.KTBW.TO.W.0007.000000T0000Z-150501T0230Z/",
                     "FLC050-",
                     "/O.CON.KTBW.TO.W.0008.000000T0000Z-150501T0245Z/",
                     ],
# we want the segments to be [ ['FLC049', 'FLC050'], ['FLC050'] ]
# currently code will generate [ ['FLC049'], ['FLC050'] ]

    },
    {
    "commentary": "cancel 1st TO.W",
    "name": "TOR_4e",
    "drtTime": "20150501_0224",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "TO.W", ["FLC049", "FLC050"], {'eventID': 32, 'status': 'ending'}),
       (2, 2.75, "TO.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.CAN.KTBW.TO.W.0007.000000T0000Z-150501T0230Z/",
                     "FLC050-",
                     "/O.CON.KTBW.TO.W.0008.000000T0000Z-150501T0245Z/",
                     ],
    },

    {
    "commentary": "let expire the 2nd TO.W",
    "name": "TOR_4f",
    "drtTime": "20150501_0239",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.75, "TO.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.EXP.KTBW.TO.W.0008.000000T0000Z-150501T0245Z/",
                     ],
    },

    {
    "commentary": "nothing left",
    "name": "TOR_4g",
    "drtTime": "20150501_0245",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       ],
    "checkStrings": [
                     ],
    },

    {
    "commentary": "clear table",
    "name": "SVR_1",
    "drtTime": "20150501_0245",
    "productType": "TOR_SVR_SVS",
    "clearHazardsTable": 1,
    },

    {
    "commentary": "Single SVR issuance",
    "name": "SVR_1a",
    "drtTime": "20150501_0000",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.SV.W.0001.150501T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single SVR update",
    "name": "SVR_1b",
    "drtTime": "20150501_0010",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single SVR cancel",
    "name": "SVR_1c",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0020",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'status': 'ending', 'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.SV.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Single SVR expire",
    "name": "SVR_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0025",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.50, "SV.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.SV.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Single SVR expire after event ending",
    "name": "SVR_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0035",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.50, "SV.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.SV.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Deleting hazard grids.",
    "name": "SVR_1e",
    "productType": "TOR_SVR_SVS",
    "checkStrings": [],
    "clearHazardsTable": 1,
    },

    {
    "commentary": "Single SVR issuance",
    "name": "SVR_2a",
    "drtTime": "20150501_0000",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.SV.W.0001.150501T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "A second SVR issuance, only mentioning the second",
    "name": "SVR_2b",
    "drtTime": "20150501_0003",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 26}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.SV.W.0002.150501T0003Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Updating just the 1st SVR",
    "name": "SVR_2c",
    "decodeVTEC": 0,
    "drtTime": "20150501_0009",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 25}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Updating both SVRs",
    "name": "SVR_2d",
    "drtTime": "20150501_0015",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 25}),
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 26}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0002.000000T0000Z-150501T0030Z/",
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Issue a new one, cancel one, ignore the 2nd one",
    "name": "SVR_2e",
    "drtTime": "20150501_0022",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 25, 'status': 'ending'}),
       (0.25, 0.75, "SV.W", ["FLC049"], {'eventID': 27}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.SV.W.0001.000000T0000Z-150501T0030Z/",
                     "FLC049-",
                     "/O.NEW.KTBW.SV.W.0003.150501T0022Z-150501T0045Z/",
                     ],
    },


    {
    "commentary": "Do an expire on the 2nd SVR, ignore the others",
    "name": "SVR_2f",
    "drtTime": "20150501_0028",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 26}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.SV.W.0002.000000T0000Z-150501T0030Z/",
                     ],
    },


    {
    "commentary": "do a followup on the 3rd",
    "name": "SVR_2g",
    "drtTime": "20150501_0032",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.75, "SV.W", ["FLC049"], {'eventID': 27}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0003.000000T0000Z-150501T0045Z/",
                     ],
    },


    {
    "commentary": "do a followup on the 3rd after it expires",
    "name": "SVR_2g",
    "drtTime": "20150501_0049",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.75, "SV.W", ["FLC049"], {'eventID': 27}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.SV.W.0003.000000T0000Z-150501T0045Z/",
                     ],
    },

    {
    "commentary": "issue 3 SVRs at same time, in different counties.",
    "name": "SVR_3a",
    "drtTime": "20150501_0100",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (1, 1.75, "SV.W", ["FLC049"], {'eventID': 31}),
       (1, 1.50, "SV.W", ["FLC049"], {'eventID': 32}),
       (1, 1.75, "SV.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.NEW.KTBW.SV.W.0006.150501T0100Z-150501T0145Z/",
                     "FLC049-",
                     "/O.NEW.KTBW.SV.W.0005.150501T0100Z-150501T0145Z/",
                     "FLC049-",
                     "/O.NEW.KTBW.SV.W.0004.150501T0100Z-150501T0130Z/",
                     ],
    },

    {
    "commentary": "cancel 2 of the 3 SV.W events",
    "name": "SVR_3b",
    "drtTime": "20150501_0110",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (1, 1.75, "SV.W", ["FLC049"], {'eventID': 31}),
       (1, 1.50, "SV.W", ["FLC049"], {'eventID': 32, 'status':'ending'}),
       (1, 1.75, "SV.W", ["FLC050"], {'eventID': 33, 'status':'ending'}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.CAN.KTBW.SV.W.0006.000000T0000Z-150501T0145Z/",
                     "FLC049-",
                     "/O.CAN.KTBW.SV.W.0004.000000T0000Z-150501T0130Z/",
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0005.000000T0000Z-150501T0145Z/",
                     ],
    },

    {
    "commentary": "update the remaining one",
    "name": "SVR_3c",
    "drtTime": "20150501_0115",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (1, 1.75, "SV.W", ["FLC049"], {'eventID': 31}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0005.000000T0000Z-150501T0145Z/",
                     ],
    },

    {
    "commentary": "issue SV.W for 2 counties",
    "name": "SVR_4a",
    "drtTime": "20150501_0200",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "SV.W", ["FLC049", "FLC050"], {'eventID': 32}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.NEW.KTBW.SV.W.0007.150501T0200Z-150501T0230Z/",
                     ],

    },

    {
    "commentary": "issue another SV.W, ignoring the first one",
    "name": "SVR_4b",
    "drtTime": "20150501_0210",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.75, "SV.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.NEW.KTBW.SV.W.0008.150501T0210Z-150501T0245Z/",
                     ],

    },

    {
    "commentary": "update all SV.Ws",
    "name": "SVR_4c",
    "drtTime": "20150501_0215",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "SV.W", ["FLC049", "FLC050"], {'eventID': 32}),
       (2, 2.75, "SV.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.CON.KTBW.SV.W.0008.000000T0000Z-150501T0245Z/",
                     "FLC049-050-",
                     "/O.CON.KTBW.SV.W.0007.000000T0000Z-150501T0230Z/",
                     ],

    },
    {
    "commentary": "cancel 1st SV.W",
    "name": "SVR_4d",
    "decodeVTEC": 0,
    "drtTime": "20150501_0218",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "SV.W", ["FLC049", "FLC050"], {'eventID': 32, 'status': 'ending'}),
       (2, 2.75, "SV.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.CAN.KTBW.SV.W.0007.000000T0000Z-150501T0230Z/",
                     "FLC050-",
                     "/O.CON.KTBW.SV.W.0008.000000T0000Z-150501T0245Z/",
                     ],
    },
    {
    "commentary": "cancel 1st SV.W",
    "name": "SVR_4e",
    "drtTime": "20150501_0224",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.5, "SV.W", ["FLC049", "FLC050"], {'eventID': 32, 'status': 'ending'}),
       (2, 2.75, "SV.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.CAN.KTBW.SV.W.0007.000000T0000Z-150501T0230Z/",
                     "FLC050-",
                     "/O.CON.KTBW.SV.W.0008.000000T0000Z-150501T0245Z/",
                     ],
    },

    {
    "commentary": "let expire the 2nd SV.W",
    "name": "SVR_4f",
    "drtTime": "20150501_0239",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (2, 2.75, "SV.W", ["FLC050"], {'eventID': 33}),
       ],
    "checkStrings": [
                     "FLC050-",
                     "/O.EXP.KTBW.SV.W.0008.000000T0000Z-150501T0245Z/",
                     ],
    },

    {
    "commentary": "nothing left",
    "name": "SVR_4g",
    "drtTime": "20150501_0245",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       ],
    "checkStrings": [
                     ],
    },

    {
    "commentary": "Single TOR issuance",
    "name": "TORSVR_1a",
    "drtTime": "20150501_0000",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "TO.W", ["FLC049"], {'eventID': 40}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.TO.W.0001.150501T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single SVR issuance",
    "name": "TORSVR_1b",
    "drtTime": "20150501_0010",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 41}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.SV.W.0009.150501T0010Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Update SVR",
    "name": "TORSVR_1c",
    "drtTime": "20150501_0019",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.5, "SV.W", ["FLC049"], {'eventID': 41}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.SV.W.0009.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Cancel both TOR and SVR - step 1",
    "name": "TORSVR_1d",
    "drtTime": "20150501_0025",
    "decodeVTEC": 0,   # don't store the vtec results
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.50, "TO.W", ["FLC049"], {'eventID': 40, 'status': 'ending'}),
       (0, 0.50, "SV.W", ["FLC049"], {'eventID': 41, 'status': 'ending'}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     "FLC049-",
                     "/O.CAN.KTBW.SV.W.0009.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "expire both TOR and SVR - step 2",
    "name": "TORSVR_1e",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0029",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       (0, 0.50, "TO.W", ["FLC049"], {'eventID': 40}),
       (0, 0.50, "SV.W", ["FLC049"], {'eventID': 41}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.TO.W.0001.000000T0000Z-150501T0030Z/",
                     "FLC049-",
                     "/O.EXP.KTBW.SV.W.0009.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "nothing left",
    "name": "TORSVR_1f",
    "drtTime": "20150501_0245",
    "productType": "TOR_SVR_SVS",
    "createGrids": [
       ],
    "checkStrings": [
                     ],
    },

    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20150501_0000",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)




