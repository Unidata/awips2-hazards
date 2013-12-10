# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# FF.W test script
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "FFW_FFS_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Single FF.W issuance",
    "name": "FFW_FFS_1a",
    "drtTime": "20150501_0000",
    "productType": "FFW_FFS",
    "createGrids": [
       (0, 2.5, "FF.W.Convective", ["FLC049"], {'eventID': 25,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.FF.W.0001.150501T0000Z-150501T0230Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },

    {
    "commentary": "Single FF.W update",
    "name": "FFW_FFS_1b",
    "drtTime": "20150501_0100",
    "productType": "FFW_FFS",
    "createGrids": [
       (0, 2.5, "FF.W.Convective", ["FLC049"], {'eventID': 25,
        'immediateCause': 'IC', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.FF.W.0001.000000T0000Z-150501T0230Z/",
                     "/00000.0.IC.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },



    {
    "commentary": "Add another FF.W, update the existing one",
    "name": "FFW_FFS_1c",
    "drtTime": "20150501_0115",
    "productType": "FFW_FFS",
    "createGrids": [
       (0, 2.5, "FF.W.Convective", ["FLC049"], {'eventID': 25,
        'immediateCause': 'IC', 'floodSeverity': 0}),
       (1.25, 3.0, "FF.W.Convective", ["FLC049", "FLC050"], {'eventID': 26,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.NEW.KTBW.FF.W.0002.150501T0115Z-150501T0300Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLC049-",
                     "/O.CON.KTBW.FF.W.0001.000000T0000Z-150501T0230Z/",
                     "/00000.0.IC.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },



    {
    "commentary": "Adjust time of 2nd only",
    "name": "FFW_FFS_1d",
    "drtTime": "20150501_0130",
    "productType": "FFW_FFS",
    "createGrids": [
       #(0, 2.5, "FF.W.Convective", ["FLC049"], {'eventID': 25,
       # 'immediateCause': 'IC', 'floodSeverity': 0}),
       (1.25, 3.5, "FF.W.Convective", ["FLC049", "FLC050"], {'eventID': 26,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXT.KTBW.FF.W.0002.000000T0000Z-150501T0330Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     #"FLC049-",
                     #"/O.CON.KTBW.FF.W.0001.000000T0000Z-150501T0230Z/",
                     #"/00000.0.IC.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },


    {
    "commentary": "Update 1st one only",
    "name": "FFW_FFS_1e",
    "drtTime": "20150501_0206",
    "productType": "FFW_FFS",
    "createGrids": [
       (0, 2.5, "FF.W.Convective", ["FLC049"], {'eventID': 25,
        'immediateCause': 'IC', 'floodSeverity': 0}),
       #(1.25, 3.5, "FF.W.Convective", ["FLC049", "FLC050"], {'eventID': 26,
       # 'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     #"FLC049-050-",
                     #"/O.EXT.KTBW.FF.W.0002.000000T0000Z-150501T0330Z/",
                     #"/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLC049-",
                     "/O.CON.KTBW.FF.W.0001.000000T0000Z-150501T0230Z/",
                     "/00000.0.IC.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },


    {
    "commentary": "Issue new dam break, cancel the convective, update other",
    "name": "FFW_FFS_1f",
    "drtTime": "20150501_0206",
    "productType": "FFW_FFS",
    "createGrids": [
       (0, 2.5, "FF.W.Convective", ["FLC049"], {'eventID': 25,
        'immediateCause': 'IC', 'floodSeverity': 0, 'state': 'ended'}),
       (1.25, 3.5, "FF.W.Convective", ["FLC049", "FLC050"], {'eventID': 26,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       (2, 4.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.FF.W.0001.000000T0000Z-150501T0230Z/",
                     "/00000.0.IC.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLC049-050-",
                     "/O.NEW.KTBW.FF.W.0003.150501T0206Z-150501T0445Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLC049-050-",
                     "/O.CON.KTBW.FF.W.0002.000000T0000Z-150501T0330Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "Update dam break, allow other to expire",
    "name": "FFW_FFS_1g",
    "drtTime": "20150501_0325",
    "productType": "FFW_FFS",
    "createGrids": [
       (1.25, 3.5, "FF.W.Convective", ["FLC049", "FLC050"], {'eventID': 26,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       (2, 4.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXP.KTBW.FF.W.0002.000000T0000Z-150501T0330Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLC049-050-",
                     "/O.CON.KTBW.FF.W.0003.000000T0000Z-150501T0445Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "Extend time of dam break",
    "name": "FFW_FFS_1h",
    "drtTime": "20150501_0325",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXT.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "Update prior to exp window",
    "name": "FFW_FFS_1h",
    "decodeVTEC": 0,
    "drtTime": "20150501_0534",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.CON.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "Update during expire window",
    "name": "FFW_FFS_1i",
    "decodeVTEC": 0,
    "drtTime": "20150501_0544",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXP.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "Update at expire time",
    "name": "FFW_FFS_1j",
    "decodeVTEC": 0,
    "drtTime": "20150501_0545",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXP.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "Update after expire time",
    "name": "FFW_FFS_1k",
    "decodeVTEC": 0,
    "drtTime": "20150501_0550",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXP.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },



    {
    "commentary": "Cancel prior to exp window",
    "name": "FFW_FFS_1l",
    "decodeVTEC": 0,
    "drtTime": "20150501_0534",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0, 'state': 'ended'}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.CAN.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "cancel during expire window",
    "name": "FFW_FFS_1m",
    "decodeVTEC": 0,
    "drtTime": "20150501_0544",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0, 'state': 'ended'}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.CAN.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "cancel at expire time",
    "name": "FFW_FFS_1n",
    "decodeVTEC": 0,
    "drtTime": "20150501_0545",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0, 'state': 'ended'}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXP.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },


    {
    "commentary": "cancel after expire time",
    "name": "FFW_FFS_1o",
    "drtTime": "20150501_0550",
    "productType": "FFW_FFS",
    "createGrids": [
       (2, 5.75, "FF.W.NonConvective", ["FLC049", "FLC050"], {'eventID': 27,
        'immediateCause': 'DM', 'floodSeverity': 0, 'state': 'ended'}),
       ],
    "checkStrings": [
                     "FLC049-050-",
                     "/O.EXP.KTBW.FF.W.0003.000000T0000Z-150501T0545Z/",
                     "/00000.0.DM.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     ],
    },



    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20150501_0000",
        "vtecMode": "O",
        "geoType": "area",
        }
    return TestScript.generalTestScript(scripts, defaults)




