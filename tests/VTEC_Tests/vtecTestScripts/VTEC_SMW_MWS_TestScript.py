# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# SMW test script
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "SMW_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Single SMW issuance",
    "name": "SMW_1a",
    "drtTime": "20150501_0000",
    "gridsStartTime": "20150501_0000",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.5, "MA.W", ["GMZ830"], {'eventID': 101}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.NEW.KTBW.MA.W.0001.150501T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single SMW update",
    "name": "SMW_1b",
    "drtTime": "20150501_0010",
    "gridsStartTime": "20150501_0000",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.5, "MA.W", ["GMZ830"], {'eventID': 101}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.CON.KTBW.MA.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single SMW cancel",
    "name": "SMW_1c",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0020",
    "gridsStartTime": "20150501_0000",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.5, "MA.W", ["GMZ830"], {"state": 'ended', 'eventID': 101}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.CAN.KTBW.MA.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Single SMW expire",
    "name": "SMW_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0025",
    "gridsStartTime": "20150501_0000",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.50, "MA.W", ["GMZ830"], {'eventID': 101}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.EXP.KTBW.MA.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },
    {
    "commentary": "Single SMW expire after event ended",
    "name": "SMW_1d",
    "decodeVTEC": 0,   # don't store the vtec results
    "drtTime": "20150501_0035",
    "gridsStartTime": "20150501_0000",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.50, "MA.W", ["GMZ830"], {'eventID': 101}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.EXP.KTBW.MA.W.0001.000000T0000Z-150501T0030Z/",
                     ],
    },

    {
    "commentary": "Single SMW issuance",
    "name": "SMW_2a",
    "drtTime": "20150502_0000",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.5, "MA.W", ["GMZ830"], {'eventID': 102}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.NEW.KTBW.MA.W.0002.150502T0000Z-150502T0030Z/",
                     ],
    },

    {
    "commentary": "A second SMW issuance, only mentioning the second",
    "name": "SMW_2b",
    "drtTime": "20150502_0003",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.75, "MA.W", ["GMZ830"], {'eventID': 103}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.NEW.KTBW.MA.W.0003.150502T0003Z-150502T0045Z/",
                     ],
    },

    {
    "commentary": "Updating just the 1st SMW",
    "name": "SMW_2c",
    "decodeVTEC": 0,
    "drtTime": "20150502_0009",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.5, "MA.W", ["GMZ830"], {'eventID': 102}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.CON.KTBW.MA.W.0002.000000T0000Z-150502T0030Z/",
                     ],
    },

    {
    "commentary": "Updating both SMWs",
    "name": "SMW_2d",
    "drtTime": "20150502_0015",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.5, "MA.W", ["GMZ830"], {'eventID': 102}),
       (0, 0.75, "MA.W", ["GMZ830"], {'eventID': 103}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.CON.KTBW.MA.W.0003.000000T0000Z-150502T0045Z/",
                     "GMZ830-",
                     "/O.CON.KTBW.MA.W.0002.000000T0000Z-150502T0030Z/",
                     ],
    },

    {
    "commentary": "Issue a new one, cancel one, ignore the 2nd one",
    "name": "SMW_2e",
    "drtTime": "20150502_0022",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.5, "MA.W", ["GMZ830"], {'eventID': 102, 'state': 'ended'}),
       (0, 0.75, "MA.W", ["GMZ830"], {'eventID': 104}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.CAN.KTBW.MA.W.0002.000000T0000Z-150502T0030Z/",
                     "GMZ830-",
                     "/O.NEW.KTBW.MA.W.0004.150502T0022Z-150502T0045Z/",
                     ],
    },


    {
    "commentary": "Issue a new one for two zones",
    "name": "SMW_2f",
    "drtTime": "20150502_0028",
    "productType": "SMW_MWS",
    "createGrids": [
       (0, 0.75, "MA.W", ["GMZ830", "GMZ831"], {'eventID': 105}),
       ],
    "checkStrings": [
                     "GMZ830-831-",
                     "/O.NEW.KTBW.MA.W.0005.150502T0028Z-150502T0045Z/",
                     ],
    },


    {
    "commentary": "expire both",
    "name": "SMW_2g",
    "drtTime": "20150502_0042",
    "productType": "SMW_MWS",
    "createGrids": [
       (0.25, 0.75, "MA.W", ["GMZ830"], {'eventID': 103}),
       (0, 0.75, "MA.W", ["GMZ830"], {'eventID': 104}),
       ],
    "checkStrings": [
                     "GMZ830-",
                     "/O.EXP.KTBW.MA.W.0004.000000T0000Z-150502T0045Z/",
                     "GMZ830-",
                     "/O.EXP.KTBW.MA.W.0003.000000T0000Z-150502T0045Z/",
                     ],
    },
    ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20150502_0000",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)




