#!/usr/bin/env python

import unittest
import os
import sys

from testCaseSupportRoutines import defaultRecommenderSetup
from testCaseSupportRoutines import UnitTestFramework
from MockHazardEvent import HazardEvent

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
        hazardEvent = HazardEvent()
        
        #
        # Injecting a non java-backed test hazard event here.
        result = recommenderObject.updateEventAttributes( \
                 hazardEvent, sessionDict, dialogDict, spatialDict)

        resultDict = result.__dict__
        self.reportTestCaseOutcome(resultDict, expectedResult)

if __name__ == '__main__':
    unittest.main()
#
