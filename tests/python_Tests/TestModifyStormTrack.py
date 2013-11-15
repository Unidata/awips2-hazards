#!/usr/bin/env python

import os
import sys
import subprocess
import json
import traceback
import unittest

from testCaseSupportRoutines import commonRecommenderTestSetUp
from testCaseSupportRoutines import getNextTestCase

testCount = 0

# This is a unit test of the track updater, ModifyStormTrackTool.
class TestModifyStormTrack(unittest.TestCase):

    # Here we set up the exact set of paths we need in sys.paths
    # so we can access the code we need.
    def setUp(self):
        commonRecommenderTestSetUp()

    # Test itself needs to be a member method in order to use the
    # assertEqual() method.
    def nextTest(self) :
        global testCount
        if testCount==999999 :
            self.skipTest("")
            return
        (inputTestData, expectedResult) = getNextTestCase()
        if inputTestData==None :
            sys.stdout.write("\n"+str(testCount)+" tests implemented.\n")
            testCount = 999999
            self.skipTest("")
            return
        testCount = testCount+1
        if inputTestData == False :
            self.assertEqual(expectedResult,"OK")
            return
        sessionAttributes = inputTestData["sessionAttributes"]
        eventAttributes = inputTestData["eventAttributes"]
        dialogInputMap = {}
        spatialInputMap = inputTestData["spatialInputMap"]
        if "caseDesc" in inputTestData :
            sys.stdout.write("\n"+inputTestData["caseDesc"]+"\n")
        from ModifyStormTrackTool import Recommender
        recommenderObject = Recommender()
        result = recommenderObject.updateEventAttributes( sessionAttributes, \
                 eventAttributes, dialogInputMap, spatialInputMap)

        self.assertEqual(json.dumps(result, sort_keys=True), \
                         json.dumps(expectedResult, sort_keys=True) )

    def test_ModifyStormTrackA(self) :
        self.nextTest()

    def test_ModifyStormTrackB(self) :
        self.nextTest()

    def test_ModifyStormTrackC(self) :
        self.nextTest()

    def test_ModifyStormTrackD(self) :
        self.nextTest()

    def test_ModifyStormTrackE(self) :
        self.nextTest()

    def test_ModifyStormTrackF(self) :
        self.nextTest()

    def test_ModifyStormTrackG(self) :
        self.nextTest()

    def test_ModifyStormTrackH(self) :
        self.nextTest()

    def test_ModifyStormTrackI(self) :
        self.nextTest()

    def test_ModifyStormTrackJ(self) :
        self.nextTest()

    def test_ModifyStormTrackK(self) :
        self.nextTest()

    def test_ModifyStormTrackL(self) :
        self.nextTest()

    def test_ModifyStormTrackM(self) :
        self.nextTest()

    def test_ModifyStormTrackN(self) :
        self.nextTest()

    def test_ModifyStormTrackO(self) :
        self.nextTest()

    def test_ModifyStormTrackP(self) :
        self.nextTest()

    def test_ModifyStormTrackQ(self) :
        self.nextTest()

    def test_ModifyStormTrackR(self) :
        self.nextTest()

    def test_ModifyStormTrackS(self) :
        self.nextTest()

    def test_ModifyStormTrackT(self) :
        self.nextTest()

    def test_ModifyStormTrackU(self) :
        self.nextTest()

    def test_ModifyStormTrackV(self) :
        self.nextTest()

    def test_ModifyStormTrackW(self) :
        self.nextTest()

    def test_ModifyStormTrackX(self) :
        self.nextTest()

    def test_ModifyStormTrackY(self) :
        self.nextTest()

    def test_ModifyStormTrackZ(self) :
        self.nextTest()

if __name__ == '__main__':
    unittest.main()
#
