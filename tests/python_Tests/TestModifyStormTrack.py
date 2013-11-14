#!/usr/bin/env python

import os
import sys
import subprocess
import json
import unittest

from testCaseSupportRoutines import UnitTestFramework
from testCaseSupportRoutines import defaultRecommenderSetup

# This is a unit test of the track updater, ModifyStormTrackTool.
class TestModifyStormTrack(UnitTestFramework):

    def setUp(self) :
        defaultRecommenderSetup(__file__)

    # 
    def performNextTest(self, inputTestData, expectedResult) :
        sessionAttributes = inputTestData["sessionAttributes"]
        eventAttributes = inputTestData["eventAttributes"]
        dialogInputMap = {}
        spatialInputMap = inputTestData["spatialInputMap"]
        if os.environ.get("LOCALIZATION_DATA_SOURCE")=="EDEX" :
            from LocalizationInterface import LocalizationInterface
            myLI = LocalizationInterface("")
            codeImport = myLI.getLocFile( \
               "python/events/recommenders/ModifyStormTrackTool.py",
               "COMMON_STATIC")
            exec codeImport
        from ModifyStormTrackTool import Recommender
        recommenderObject = Recommender()
        result = recommenderObject.updateEventAttributes( sessionAttributes, \
                 eventAttributes, dialogInputMap, spatialInputMap)

        self.reportTestCaseOutcome(result, expectedResult)

if __name__ == '__main__':
    unittest.main()
#
