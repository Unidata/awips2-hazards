#!/usr/bin/env python

import unittest
import os
import sys

from testCaseSupportRoutines import defaultRecommenderSetup
from testCaseSupportRoutines import UnitTestFramework

# This is a unit test of the track initializer, StormTrackTool.
class TestStormTrack(UnitTestFramework) :

    def setUp(self) :
        defaultRecommenderSetup(__file__)

    # 
    def performNextTest(self, inputTestData, expectedResult) :
        sessionAttributes = inputTestData["sessionAttributes"]
        dialogInputMap = {}
        spatialInputMap = inputTestData["spatialInputMap"]
        if os.environ.get("LOCALIZATION_DATA_SOURCE")=="EDEX" :
            from LocalizationInterface import LocalizationInterface
            myLI = LocalizationInterface("")
            codeImport = myLI.getLocFile( \
               "python/events/recommenders/StormTrackTool.py",
               "COMMON_STATIC")
            exec codeImport
        from StormTrackTool import Recommender
        recommenderObject = Recommender()
        result = recommenderObject.updateEventAttributes( \
                 sessionAttributes, dialogInputMap, spatialInputMap)

        self.reportTestCaseOutcome(result, expectedResult)

if __name__ == '__main__':
    unittest.main()
#
