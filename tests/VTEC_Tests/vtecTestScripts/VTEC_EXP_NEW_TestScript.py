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
    "name": "EXPNEW_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Setting initial event",
    "name": "EXPNEW_1",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (8, 24+8, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.NEW.KTBW.WS.W.0001.100101T1300Z-100102T1300Z/",
                     ],
    },

    {
    "commentary": "Extending right before EXP ",
    "name": "EXPNEW_2",
    "drtTime": "20100102_1250",
    "decodeVTEC": 0,   #don't decode
    "productType": "WSW",
    "createGrids": [
       (8, 32, "WS.W", ["FLZ050"]),
       (32, 40, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.EXT.KTBW.WS.W.0001.000000T0000Z-100102T2100Z/",
                     ],
    },

    {
    "commentary": "Extending after EXP ",
    "name": "EXPNEW_3",
    "drtTime": "20100102_1301",
    "productType": "WSW",
    "createGrids": [
       (8, 32, "WS.W", ["FLZ050"]),
       (32, 40, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
                     "FLZ050-",
                     "/O.EXP.KTBW.WS.W.0001.000000T0000Z-100102T1300Z/",
                     "/O.NEW.KTBW.WS.W.0002.100102T1301Z-100102T2100Z/",
                     ],
    },


    {
    "commentary": "Cleanup.",
    "name": "EXPNEW_4",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
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




