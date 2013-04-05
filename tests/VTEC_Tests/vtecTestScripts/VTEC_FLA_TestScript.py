# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# FL.A test script
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "FLA_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Single FL.A issuance",
    "name": "FLA_1a",
    "drtTime": "20130116_0000",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+3, "FL.A", ["DTXA6"],
        {'eventID': 25, 'riseAbove': 1*24+0, 'crest': 1*24+0,
         'fallBelow': 1*24+12, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "DTXA6-",
                     "/O.NEW.KTBW.FL.A.0001.130117T0000Z-130118T0300Z/",
                     "/DTXA6.0.ER.130117T0000Z.130117T0000Z.130117T1200Z.NO/"
                     ],
    },

    {
    "commentary": "Update prior to event starting, adjustments in crest time",
    "name": "FLA_1b",
    "drtTime": "20130116_1200",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+3, "FL.A", ["DTXA6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "DTXA6-",
                     "/O.CON.KTBW.FL.A.0001.130117T0000Z-130118T0300Z/",
                     "/DTXA6.0.ER.130117T0100Z.130117T0200Z.130117T1200Z.NO/"
                     ],
    },

    {
    "commentary": "Update after event starting",
    "name": "FLA_1c",
    "drtTime": "20130117_0000",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+3, "FL.A", ["DTXA6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "DTXA6-",
                     "/O.CON.KTBW.FL.A.0001.000000T0000Z-130118T0300Z/",
                     "/DTXA6.0.ER.130117T0100Z.130117T0200Z.130117T1200Z.NO/"
                     ],
    },

    {
    "commentary": "Adjusting time of FL.A",
    "name": "FLA_1d",
    "drtTime": "20130117_0100",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.A", ["DTXA6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "DTXA6-",
                     "/O.EXT.KTBW.FL.A.0001.000000T0000Z-130118T0400Z/",
                     "/DTXA6.0.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Cancelling FL.A",
    "name": "FLA_1e",
    "decodeVTEC": 0,
    "drtTime": "20130118_0000",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.A", ["DTXA6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'UU', 'state': 'ended'})
       ],
    "checkStrings": [
                     "DTXA6-",
                     "/O.CAN.KTBW.FL.A.0001.000000T0000Z-130118T0400Z/",
                     "/DTXA6.0.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Expiring FL.A prior to event end",
    "name": "FLA_1f",
    "decodeVTEC": 0,
    "drtTime": "20130118_0345",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.A", ["DTXA6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "DTXA6-",
                     "/O.EXP.KTBW.FL.A.0001.000000T0000Z-130118T0400Z/",
                     "/DTXA6.0.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Expiring FL.A after event end",
    "name": "FLA_1g",
    "drtTime": "20130118_0425",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.A", ["DTXA6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "DTXA6-",
                     "/O.EXP.KTBW.FL.A.0001.000000T0000Z-130118T0400Z/",
                     "/DTXA6.0.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Issue 2 FL.A",
    "name": "FLA_2a",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0000",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+0, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+22, 1*24+22, "FL.A", ["DXCT3"],
        {'eventID': 27, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "DXCT3-",
                     "/O.NEW.KTBW.FL.A.0002.130119T2200Z-130120T2200Z/",
                     "/DXCT3.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     "SAVT1-",
                     "/O.NEW.KTBW.FL.A.0003.130120T0000Z-130121T0000Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],
    },

    {
    "commentary": "Both CON, before event starts",
    "name": "FLA_2b",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_2000",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+0, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+22, 1*24+22, "FL.A", ["DXCT3"],
        {'eventID': 27, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "DXCT3-",
                     "/O.CON.KTBW.FL.A.0002.130119T2200Z-130120T2200Z/",
                     "/DXCT3.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     "SAVT1-",
                     "/O.CON.KTBW.FL.A.0003.130120T0000Z-130121T0000Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],
    },

    {
    "commentary": "Both CON, when 1st event starts",
    "name": "FLA_2c",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_2200",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+0, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+22, 1*24+22, "FL.A", ["DXCT3"],
        {'eventID': 27, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "DXCT3-",
                     "/O.CON.KTBW.FL.A.0002.000000T0000Z-130120T2200Z/",
                     "/DXCT3.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     "SAVT1-",
                     "/O.CON.KTBW.FL.A.0003.130120T0000Z-130121T0000Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],

    },

    {
    "commentary": "Adjust times of both events, while only 1 is active",
    "name": "FLA_2d",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_2300",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+6, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+22, 1*24+20, "FL.A", ["DXCT3"],
        {'eventID': 27, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "DXCT3-",
                     "/O.EXT.KTBW.FL.A.0002.000000T0000Z-130120T2000Z/",
                     "/DXCT3.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     "SAVT1-",
                     "/O.EXT.KTBW.FL.A.0003.130120T0000Z-130121T0600Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],

    },


    {
    "commentary": "Adjust times of both events, while both are active",
    "name": "FLA_2e",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130120_0200",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+8, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+22, 1*24+21, "FL.A", ["DXCT3"],
        {'eventID': 27, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.EXT.KTBW.FL.A.0003.000000T0000Z-130121T0800Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     "DXCT3-",
                     "/O.EXT.KTBW.FL.A.0002.000000T0000Z-130120T2100Z/",
                     "/DXCT3.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],

    },

    {
    "commentary": "Adjust time of just one event, while active",
    "name": "FLA_2f",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130120_1200",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+12, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+22, 1*24+21, "FL.A", ["DXCT3"],
        {'eventID': 27, 'riseAbove': 1*24+6, 'crest': 1*24+19,
         'fallBelow': 1*24+22, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.EXT.KTBW.FL.A.0003.000000T0000Z-130121T1200Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     "DXCT3-",
                     "/O.CON.KTBW.FL.A.0002.000000T0000Z-130120T2100Z/",
                     "/DXCT3.0.ER.130120T0600Z.130120T1900Z.130120T2200Z.NO/",
                     ],

    },


    {
    "commentary": "Cancel one event, while still active. Ignore the other.",
    "name": "FLA_2g",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130120_1400",
    "productType": "FFA",
    "createGrids": [
       (0*24+22, 1*24+21, "FL.A", ["DXCT3"],
        {'eventID': 27, 'riseAbove': 1*24+6, 'crest': 1*24+19,
         'fallBelow': 1*24+22, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO', 'state': 'ended'})
       ],
    "checkStrings": [
                     "DXCT3-",
                     "/O.CAN.KTBW.FL.A.0002.000000T0000Z-130120T2100Z/",
                     "/DXCT3.0.ER.130120T0600Z.130120T1900Z.130120T2200Z.NO/",
                     ],

    },


    {
    "commentary": "Update the remaining event",
    "name": "FLA_2h",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130120_1500",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+12, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.CON.KTBW.FL.A.0003.000000T0000Z-130121T1200Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],

    },


    {
    "commentary": "Allow to expire, just before event end",
    "name": "FLA_2i",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130121_1130",
    "decodeVTEC": 0,
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+12, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.EXP.KTBW.FL.A.0003.000000T0000Z-130121T1200Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],

    },


    {
    "commentary": "Allow to expire, at event end",
    "name": "FLA_2j",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130121_1200",
    "decodeVTEC": 0,
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+12, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.EXP.KTBW.FL.A.0003.000000T0000Z-130121T1200Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],

    },


    {
    "commentary": "Allow to expire, after event end",
    "name": "FLA_2j",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130121_1230",
    "productType": "FFA",
    "createGrids": [
       (1*24+0, 2*24+12, "FL.A", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 1*24+6, 'crest': 1*24+18,
         'fallBelow': 1*24+23, 'floodSeverity': 0, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.EXP.KTBW.FL.A.0003.000000T0000Z-130121T1200Z/",
                     "/SAVT1.0.ER.130120T0600Z.130120T1800Z.130120T2300Z.NO/",
                     ],

    },

    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "FLA_",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

   ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20130116_0000",
        "vtecMode": "O",
        "geoType": 'point',
        }
    return TestScript.generalTestScript(scripts, defaults)




