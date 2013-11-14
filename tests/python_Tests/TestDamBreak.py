#!/usr/bin/env python

import unittest
import os
import sys

from testCaseSupportRoutines import defaultRecommenderSetup
from testCaseSupportRoutines import UnitTestFramework

# This is a unit test of DamBreakFloodRecommender.
class TestStormTrack(UnitTestFramework):

    def setUp(self) :
        defaultRecommenderSetup(__file__)

    # 
    def performNextTest(self, inputTestData, expectedResult) :
        sessionDict = inputTestData["sessionDict"]
        dialogDict = inputTestData["dialogDict"]
        spatialDict = None
        if os.environ.get("LOCALIZATION_DATA_SOURCE")=="EDEX" :
            from LocalizationInterface import LocalizationInterface
            myLI = LocalizationInterface("")
            codeImport = myLI.getLocFile( \
               "python/events/recommenders/DamBreakFloodRecommender.py",
               "COMMON_STATIC")
            exec codeImport
        from DamBreakFloodRecommender import Recommender
        recommenderObject = Recommender()
        result = recommenderObject.updateEventAttributes( \
                 sessionDict, dialogDict, spatialDict)

        self.reportTestCaseOutcome(result, expectedResult)

if __name__ == '__main__':
    unittest.main()
#
