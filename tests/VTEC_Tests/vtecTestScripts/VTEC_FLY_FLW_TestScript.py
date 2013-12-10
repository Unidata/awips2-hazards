# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# FL.W test script
# FL.Y test script
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "FLW_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "Single FL.W issuance",
    "name": "FLW_1a",
    "drtTime": "20130116_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+3, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+0, 'crest': 1*24+0,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "KSCM6-",
                     "/O.NEW.KTBW.FL.W.0001.130117T0000Z-130118T0300Z/",
                     "/KSCM6.1.ER.130117T0000Z.130117T0000Z.130117T1200Z.NO/"
                     ],
    },

    {
    "commentary": "Update prior to starting time, add ROU observation",
    "name": "FLW_1a_adding ROU observation",
    "drtTime": "20130116_0100",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+3, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+0, 'crest': 1*24+0,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (1*24+0, 2*24+3, "HY.S", ["ABCD6"],
        {'eventID': 99, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       ],
    "checkStrings": [
                     "KSCM6-",
                     "/O.CON.KTBW.FL.W.0001.130117T0000Z-130118T0300Z/",
                     "/KSCM6.1.ER.130117T0000Z.130117T0000Z.130117T1200Z.NO/",
                     "ABCD6-",
                     "/O.ROU.KTBW.HY.S.0000.000000T0000Z-000000T0000Z/",
                     "/ABCD6.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     ],
    },

    {
    "commentary": "Adding until further notice event",
    "name": "FLW_1a_adding UFN",
    "drtTime": "20130116_0100",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+10, 2*24+3, "FL.W", ["KSCM7"],
        {'eventID': 88, 'floodSeverity': 1, 'immediateCause': 'IJ',
         'riseAbove': 1*24+0, 'crest': 1*24+0,
         'floodRecord': 'NO', 'ufn': 1}),
       ],
    "checkStrings": [
                     "KSCM7-",
                     "/O.NEW.KTBW.FL.W.0002.130116T1000Z-000000T0000Z/",
                     "/KSCM7.1.IJ.130117T0000Z.130117T0000Z.000000T0000Z.NO/",
                     ],
    },

    {
    "commentary": "Update prior to event starting, adjustments in crest time",
    "name": "FLW_1b",
    "drtTime": "20130116_1200",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+3, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+10, 2*24+3, "FL.W", ["KSCM7"],
        {'eventID': 88, 'floodSeverity': 1, 'immediateCause': 'IJ',
         'riseAbove': 1*24+0, 'crest': 1*24+0,
         'floodRecord': 'NO', 'ufn': 1}),
       ],
    "checkStrings": [
                     "KSCM7-",
                     "/O.CON.KTBW.FL.W.0002.000000T0000Z-000000T0000Z/",
                     "/KSCM7.1.IJ.130117T0000Z.130117T0000Z.000000T0000Z.NO/",
                     "KSCM6-",
                     "/O.CON.KTBW.FL.W.0001.130117T0000Z-130118T0300Z/",
                     "/KSCM6.1.ER.130117T0100Z.130117T0200Z.130117T1200Z.NO/"
                     ],
    },

    {
    "commentary": "Update after event starting",
    "name": "FLW_1c",
    "drtTime": "20130117_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+3, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "KSCM6-",
                     "/O.CON.KTBW.FL.W.0001.000000T0000Z-130118T0300Z/",
                     "/KSCM6.1.ER.130117T0100Z.130117T0200Z.130117T1200Z.NO/"
                     ],
    },

    {
    "commentary": "Adjusting time of FL.W",
    "name": "FLW_1d",
    "drtTime": "20130117_0100",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "KSCM6-",
                     "/O.EXT.KTBW.FL.W.0001.000000T0000Z-130118T0400Z/",
                     "/KSCM6.1.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Cancelling FL.W",
    "name": "FLW_1e",
    "decodeVTEC": 0,
    "drtTime": "20130118_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'UU', 'state': 'ended'})
       ],
    "checkStrings": [
                     "KSCM6-",
                     "/O.CAN.KTBW.FL.W.0001.000000T0000Z-130118T0400Z/",
                     "/KSCM6.1.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Expiring FL.W prior to event end",
    "name": "FLW_1f",
    "decodeVTEC": 0,
    "drtTime": "20130118_0345",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "KSCM6-",
                     "/O.EXP.KTBW.FL.W.0001.000000T0000Z-130118T0400Z/",
                     "/KSCM6.1.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Expiring FL.W after event end",
    "name": "FLW_1g",
    "drtTime": "20130118_0425",
    "productType": "FLW_FLS",
    "createGrids": [
       (1*24+0, 2*24+4, "FL.W", ["KSCM6"],
        {'eventID': 25, 'riseAbove': 1*24+1, 'crest': 1*24+2,
         'fallBelow': 1*24+12, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "KSCM6-",
                     "/O.EXP.KTBW.FL.W.0001.000000T0000Z-130118T0400Z/",
                     "/KSCM6.1.ER.130117T0100Z.130117T0200Z.130117T1200Z.UU/"
                     ],
    },

    {
    "commentary": "Single FL.W issuance, in prep for 2 FL.Ws",
    "name": "FLW_2a",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.W", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 0*24+6, 'crest': 0*24+18,
         'fallBelow': 0*24+23, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.NEW.KTBW.FL.W.0003.130119T0000Z-130120T0000Z/",
                     "/SAVT1.1.ER.130119T0600Z.130119T1800Z.130119T2300Z.NO/"
                     ],
    },

    {
    "commentary": "Another FL.W issuance, with 1st one CON",
    "name": "FLW_2b",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0200",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.W", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 0*24+6, 'crest': 0*24+18,
         'fallBelow': 0*24+23, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+0, 1*24+0, "FL.W", ["AMYM6"],
        {'eventID': 27, 'riseAbove': 0*24+7, 'crest': 0*24+19,
         'fallBelow': 0*24+22, 'floodSeverity': 2, 'immediateCause': 'DM',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "AMYM6-",
                     "/O.NEW.KTBW.FL.W.0004.130119T0200Z-130120T0000Z/",
                     "/AMYM6.2.DM.130119T0700Z.130119T1900Z.130119T2200Z.UU/",
                     "SAVT1-",
                     "/O.CON.KTBW.FL.W.0003.000000T0000Z-130120T0000Z/",
                     "/SAVT1.1.ER.130119T0600Z.130119T1800Z.130119T2300Z.NO/",
                     ],
    },

    {
    "commentary": "only updating the 2nd one, after it is active",
    "name": "FLW_2c",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0300",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.W", ["AMYM6"],
        {'eventID': 27, 'riseAbove': 0*24+7, 'crest': 0*24+19,
         'fallBelow': 0*24+22, 'floodSeverity': 2, 'immediateCause': 'DM',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "AMYM6-",
                     "/O.CON.KTBW.FL.W.0004.000000T0000Z-130120T0000Z/",
                     "/AMYM6.2.DM.130119T0700Z.130119T1900Z.130119T2200Z.UU/"
                     ],
    },

    {
    "commentary": "Extending in time the 1st one, updating the 2nd one",
    "name": "FLW_2d",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0700",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+12, "FL.W", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 0*24+6, 'crest': 0*24+19,
         'fallBelow': 1*24+10, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NR'}),
       (0*24+0, 1*24+0, "FL.W", ["AMYM6"],
        {'eventID': 27, 'riseAbove': 0*24+7, 'crest': 0*24+19,
         'fallBelow': 0*24+22, 'floodSeverity': 2, 'immediateCause': 'DM',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.EXT.KTBW.FL.W.0003.000000T0000Z-130120T1200Z/",
                     "/SAVT1.1.ER.130119T0600Z.130119T1900Z.130120T1000Z.NR/",
                     "AMYM6-",
                     "/O.CON.KTBW.FL.W.0004.000000T0000Z-130120T0000Z/",
                     "/AMYM6.2.DM.130119T0700Z.130119T1900Z.130119T2200Z.UU/"
                     ],
    },

    {
    "commentary": "CAN 1st one, EXT the 2nd one",
    "name": "FLW_2e",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_1200",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+12, "FL.W", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 0*24+6, 'crest': 0*24+19,
         'fallBelow': 1*24+10, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NR', 'state': 'ended'}),
       (0*24+0, 1*24+3, "FL.W", ["AMYM6"],
        {'eventID': 27, 'riseAbove': 0*24+7, 'crest': 0*24+19,
         'fallBelow': 1*24+2, 'floodSeverity': 2, 'immediateCause': 'DM',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.CAN.KTBW.FL.W.0003.000000T0000Z-130120T1200Z/",
                     "/SAVT1.1.ER.130119T0600Z.130119T1900Z.130120T1000Z.NR/",
                     "AMYM6-",
                     "/O.EXT.KTBW.FL.W.0004.000000T0000Z-130120T0300Z/",
                     "/AMYM6.2.DM.130119T0700Z.130119T1900Z.130120T0200Z.UU/"
                     ],
    },


    {
    "commentary": "EXP the 2nd one",
    "name": "FLW_2f",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130120_0300",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+12, "FL.W", ["SAVT1"],
        {'eventID': 26, 'riseAbove': 0*24+6, 'crest': 0*24+19,
         'fallBelow': 1*24+10, 'floodSeverity': 1, 'immediateCause': 'ER',
         'floodRecord': 'NR', 'state': 'ended'}),
       (0*24+0, 1*24+3, "FL.W", ["AMYM6"],
        {'eventID': 27, 'riseAbove': 0*24+7, 'crest': 0*24+19,
         'fallBelow': 1*24+2, 'floodSeverity': 2, 'immediateCause': 'DM',
         'floodRecord': 'UU'})
       ],
    "checkStrings": [
                     "AMYM6-",
                     "/O.EXP.KTBW.FL.W.0004.000000T0000Z-130120T0300Z/",
                     "/AMYM6.2.DM.130119T0700Z.130119T1900Z.130120T0200Z.UU/"
                     ],
    },

    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "FLY_2",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Two FL.Y issuances, one starting now, one later",
    "name": "FLY_2a",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.Y", ["SAVT1"],
        {'eventID': 26, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+2, 1*24+0, "FL.Y", ["AMYM6"],
        {'eventID': 27, 'floodSeverity': 'N', 'immediateCause': 'DM',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "SAVT1-",
                     "/O.NEW.KTBW.FL.Y.0001.130119T0000Z-130120T0000Z/",
                     "/SAVT1.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "AMYM6-",
                     "/O.NEW.KTBW.FL.Y.0002.130119T0200Z-130120T0000Z/",
                     "/AMYM6.N.DM.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     ],
    },

    {
    "commentary": "A 3rd issuance.",
    "name": "FLY_2b",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0300",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.Y", ["SAVT1"],
        {'eventID': 26, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+0, 1*24+0, "FL.Y", ["AMYM6"],
        {'eventID': 27, 'floodSeverity': 'N', 'immediateCause': 'DM',
         'floodRecord': 'NO'}),
       (1*24+0, 2*24+0, "FL.Y", ["BILT9"],
        {'eventID': 28, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "BILT9-",
                     "/O.NEW.KTBW.FL.Y.0003.130120T0000Z-130121T0000Z/",
                     "/BILT9.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "AMYM6-",
                     "/O.CON.KTBW.FL.Y.0002.000000T0000Z-130120T0000Z/",
                     "/AMYM6.N.DM.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "SAVT1-",
                     "/O.CON.KTBW.FL.Y.0001.000000T0000Z-130120T0000Z/",
                     "/SAVT1.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     ],
    },

    {
    "commentary": "cancelling 2nd FL.Y, replacing with FL.W",
    "name": "FLY_2c",
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_0800",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.Y", ["SAVT1"],
        {'eventID': 26, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO'}),
       (0*24+0, 1*24+0, "FL.Y", ["AMYM6"],
        {'eventID': 27, 'floodSeverity': 'N', 'immediateCause': 'DM',
         'floodRecord': 'NO', 'state': 'ended'}),
       (0*24+8, 1*24+6, "FL.W", ["AMYM6"],
        {'eventID': 29, 'riseAbove': 0*24+7, 'crest': 0*24+20,
         'fallBelow': 0*24+23, 'floodSeverity': 3, 'immediateCause': 'ER',
         'floodRecord': 'NR'}),
       (1*24+0, 2*24+0, "FL.Y", ["BILT9"],
        {'eventID': 28, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO'})
       ],
    "checkStrings": [
                     "AMYM6-",
                     "/O.CAN.KTBW.FL.Y.0002.000000T0000Z-130120T0000Z/",
                     "/AMYM6.N.DM.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "AMYM6-",
                     "/O.NEW.KTBW.FL.W.0001.130119T0800Z-130120T0600Z/",
                     "/AMYM6.3.ER.130119T0700Z.130119T2000Z.130119T2300Z.NR/",
                     "SAVT1-",
                     "/O.CON.KTBW.FL.Y.0001.000000T0000Z-130120T0000Z/",
                     "/SAVT1.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "BILT9-",
                     "/O.CON.KTBW.FL.Y.0003.130120T0000Z-130121T0000Z/",
                     "/BILT9.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     ],
    },

    {
    "commentary": "Cancel all, 1 min before one expires anyway.",
    "name": "FLY_2d",
    "decodeVTEC": 0,
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130119_2359",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.Y", ["SAVT1"],
        {'eventID': 26, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO', 'state': 'ended'}),
       (0*24+8, 1*24+6, "FL.W", ["AMYM6"],
        {'eventID': 29, 'riseAbove': 0*24+7, 'crest': 0*24+20,
         'fallBelow': 0*24+23, 'floodSeverity': 3, 'immediateCause': 'ER',
         'floodRecord': 'NR', 'state': 'ended'}),
       (1*24+0, 2*24+0, "FL.Y", ["BILT9"],
        {'eventID': 28, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'state': 'ended'})],
    "checkStrings": [
                     "AMYM6-",
                     "/O.CAN.KTBW.FL.W.0001.000000T0000Z-130120T0600Z/",
                     "/AMYM6.3.ER.130119T0700Z.130119T2000Z.130119T2300Z.NR/",
                     "SAVT1-",
                     "/O.CAN.KTBW.FL.Y.0001.000000T0000Z-130120T0000Z/",
                     "/SAVT1.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "BILT9-",
                     "/O.CAN.KTBW.FL.Y.0003.130120T0000Z-130121T0000Z/",
                     "/BILT9.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     ],
    },


    {
    "commentary": "Cancel all, at expiration time of 1 of them",
    "name": "FLY_2e",
    "decodeVTEC": 0,
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130120_0000",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.Y", ["SAVT1"],
        {'eventID': 26, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO', 'state': 'ended'}),
       (0*24+8, 1*24+6, "FL.W", ["AMYM6"],
        {'eventID': 29, 'riseAbove': 0*24+7, 'crest': 0*24+20,
         'fallBelow': 0*24+23, 'floodSeverity': 3, 'immediateCause': 'ER',
         'floodRecord': 'NR', 'state': 'ended'}),
       (1*24+0, 2*24+0, "FL.Y", ["BILT9"],
        {'eventID': 28, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'state': 'ended'})],
    "checkStrings": [
                     "AMYM6-",
                     "/O.CAN.KTBW.FL.W.0001.000000T0000Z-130120T0600Z/",
                     "/AMYM6.3.ER.130119T0700Z.130119T2000Z.130119T2300Z.NR/",
                     "BILT9-",
                     "/O.CAN.KTBW.FL.Y.0003.000000T0000Z-130121T0000Z/",
                     "/BILT9.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "SAVT1-",
                     "/O.EXP.KTBW.FL.Y.0001.000000T0000Z-130120T0000Z/",
                     "/SAVT1.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     ],
    },


    {
    "commentary": "Cancel most, at expiration time +1 minute for one of them",
    "name": "FLY_2f",
    "decodeVTEC": 0,
    "gridsStartTime": "20130119_0000",
    "drtTime": "20130120_0001",
    "productType": "FLW_FLS",
    "createGrids": [
       (0*24+0, 1*24+0, "FL.Y", ["SAVT1"],
        {'eventID': 26, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'floodRecord': 'NO', 'state': 'ended'}),
       (0*24+8, 1*24+6, "FL.W", ["AMYM6"],
        {'eventID': 29, 'riseAbove': 0*24+7, 'crest': 0*24+20,
         'fallBelow': 0*24+23, 'floodSeverity': 3, 'immediateCause': 'ER',
         'floodRecord': 'NR', 'state': 'ended'}),
       (1*24+0, 2*24+0, "FL.Y", ["BILT9"],
        {'eventID': 28, 'floodSeverity': 'N', 'immediateCause': 'ER',
         'state': 'ended'})],
    "checkStrings": [
                     "AMYM6-",
                     "/O.CAN.KTBW.FL.W.0001.000000T0000Z-130120T0600Z/",
                     "/AMYM6.3.ER.130119T0700Z.130119T2000Z.130119T2300Z.NR/",
                     "BILT9-",
                     "/O.CAN.KTBW.FL.Y.0003.000000T0000Z-130121T0000Z/",
                     "/BILT9.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     "SAVT1-",
                     "/O.EXP.KTBW.FL.Y.0001.000000T0000Z-130120T0000Z/",
                     "/SAVT1.N.ER.000000T0000Z.000000T0000Z.000000T0000Z.NO/",
                     ],
    },


   ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20130116_0000",
        "vtecMode": "O",
        "geoType": 'point',
        }
    return TestScript.generalTestScript(scripts, defaults)




