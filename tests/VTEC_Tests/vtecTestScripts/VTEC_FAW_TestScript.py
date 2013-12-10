# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
#  FA.W test script
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "FLW_FLS_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Single FA.W issuance",
    "name": "FLW_FLS_1a",
    "drtTime": "20150501_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 25, 'immediateCause': 'ER',
         'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.FA.W.0001.150501T0000Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },

    {
    "commentary": "Single FA.W update with time extension",
    "name": "FLW_FLS_1b",
    "drtTime": "20150501_0010",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.75, "FA.W", ["FLC049"], {'eventID': 25, 'immediateCause': 'ER',
         'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXT.KTBW.FA.W.0001.000000T0000Z-150501T0145Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
    {
    "commentary": "Single FA.W cancel",
    "name": "FLW_FLS_1c",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0020",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.75, "FA.W", ["FLC049"], {"state": 'ended', 'eventID': 25,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.FA.W.0001.000000T0000Z-150501T0145Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
    {
    "commentary": "Single FA.W expire",
    "name": "FLW_FLS_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0125",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.75, "FA.W", ["FLC049"], {'eventID': 25,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.FA.W.0001.000000T0000Z-150501T0145Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
    {
    "commentary": "Single FA.W expire after event ended",
    "name": "FLW_FLS_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0155",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.75, "FA.W", ["FLC049"], {'eventID': 25,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.FA.W.0001.000000T0000Z-150501T0145Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
    {
    "commentary": "Deleting hazard grids.",
    "name": "FLW_FLS_1e",
    "productType": "FLW_FLS",
    "checkStrings": [],
    "clearHazardsTable": 1,
    },
 
    {
    "commentary": "Single FA.W issuance",
    "name": "FLW_FLS_2a",
    "drtTime": "20150501_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 25,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.FA.W.0001.150501T0000Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
    {
    "commentary": "A second FA.W issuance, only mentioning the second",
    "name": "FLW_FLS_2b",
    "drtTime": "20150501_0003",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 26,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.NEW.KTBW.FA.W.0002.150501T0003Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
    {
    "commentary": "Updating just the 1st FA.W",
    "name": "FLW_FLS_2c",
    "decodeVTEC": 0,
    "drtTime": "20150501_0009",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 25,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.FA.W.0001.000000T0000Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
    {
    "commentary": "Updating both FA.Ws",
    "name": "FLW_FLS_2d",
    "drtTime": "20150501_0015",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 25,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 26,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CON.KTBW.FA.W.0002.000000T0000Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLC049-",
                     "/O.CON.KTBW.FA.W.0001.000000T0000Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
    {
    "commentary": "Issue a new one, cancel one, ignore the 2nd one",
    "name": "FLW_FLS_2e",
    "drtTime": "20150501_0122",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 25, 'state': 'ended',
        'immediateCause': 'ER', 'floodSeverity': 0}),
       (1.25, 1.75, "FA.W", ["FLC049"], {'eventID': 27,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.CAN.KTBW.FA.W.0001.000000T0000Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
                     "FLC049-",
                     "/O.NEW.KTBW.FA.W.0003.150501T0122Z-150501T0145Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
 
    {
    "commentary": "Do an expire on the 2nd FA.W, ignore the others",
    "name": "FLW_FLS_2f",
    "drtTime": "20150501_0128",
    "productType": "FLW_FLS",
    "createGrids": [
       (0, 1.5, "FA.W", ["FLC049"], {'eventID': 26,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.FA.W.0002.000000T0000Z-150501T0130Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
 
    {
    "commentary": "do a time change on the 3rd",
    "name": "FLW_FLS_2g",
    "drtTime": "20150501_0135",
    "productType": "FLW_FLS",
    "createGrids": [
       (1.25, 2.75, "FA.W", ["FLC049"], {'eventID': 27,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXT.KTBW.FA.W.0003.000000T0000Z-150501T0245Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
 
    {
    "commentary": "do a followup on the 3rd after it expires",
    "name": "FLW_FLS_2h",
    "drtTime": "20150501_0249",
    "productType": "FLW_FLS",
    "createGrids": [
       (1.25, 2.75, "FA.W", ["FLC049"], {'eventID': 27,
        'immediateCause': 'ER', 'floodSeverity': 0}),
       ],
    "checkStrings": [
                     "FLC049-",
                     "/O.EXP.KTBW.FA.W.0003.000000T0000Z-150501T0245Z/",
                     "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/"
                     ],
    },
 
    {
    "commentary": "nothing left",
    "name": "FLW_FLS_2i",
    "drtTime": "20150501_0322",
    "productType": "FLW_FLS",
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




