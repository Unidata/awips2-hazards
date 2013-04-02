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
    "name": "TESTMODE_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "name": "TESTMODE_FFA",
    "commentary": "Checking Test Mode for FFA",
    "productType": "FFA",
    "createGrids": [
       (0, 23, "FF.A", ["FLZ049","FLZ050"], {'immediateCause': 'ER'}),
       ],
    "checkStrings": [
      "FLZ049-050-",
      "/T.NEW.KTBW.FF.A.0001.100101T0400Z-100101T2300Z/",
      "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
      ],
    },
    {
    "name": "TESTMODE_FFA1",
    "commentary": "Checking Test Mode for FFA",
    "productType": "FFA",
    "vtecMode": "O",
    "createGrids": [
       (24, 30, "FF.A", ["FLZ049","FLZ050"], {'immediateCause': 'ER'}),
       ],
    "checkStrings": [
      "FLZ049-050-",
      "/O.NEW.KTBW.FF.A.0001.100102T0000Z-100102T0600Z/",
      "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/",
      ],
    },
    {
    "commentary": "Deleting hazard grids.",
    "name": "TESTMODE_Cleanup",
    "productType": None,
    "checkStrings": [],
    "clearHazardsTable": 1,
    },
    ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    gridsStartTime = "20100101_0000"
    drtTime = "20100101_0400"
    defaults = {
        "gridsStartTime": gridsStartTime,
        "drtTime": drtTime,
        "decodeVTEC": 0,
        "vtecMode": "T",
        }
    return TestScript.generalTestScript(scripts, defaults)




