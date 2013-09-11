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

# This is a unit test of the track initializer, StormTrackTool.
class TestStormTrack(unittest.TestCase):

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
        dialogInputMap = {}
        spatialInputMap = inputTestData["spatialInputMap"]
        if "caseDesc" in inputTestData :
            sys.stdout.write("\n"+inputTestData["caseDesc"]+"\n")
        from StormTrackTool import Recommender
        recommenderObject = Recommender()
        result = recommenderObject.updateEventAttributes( \
                 sessionAttributes, dialogInputMap, spatialInputMap)

        self.assertEqual(json.dumps(result, sort_keys=True), \
                         json.dumps(expectedResult, sort_keys=True) )

    def test_StormTrackA(self) :
        self.nextTest()

    def test_StormTrackB(self) :
        self.nextTest()

    def test_StormTrackC(self) :
        self.nextTest()

    def test_StormTrackD(self) :
        self.nextTest()

    def test_StormTrackE(self) :
        self.nextTest()

    def test_StormTrackF(self) :
        self.nextTest()

    def test_StormTrackG(self) :
        self.nextTest()

    def test_StormTrackH(self) :
        self.nextTest()

    def test_StormTrackI(self) :
        self.nextTest()

    def test_StormTrackJ(self) :
        self.nextTest()

    def test_StormTrackK(self) :
        self.nextTest()

    def test_StormTrackL(self) :
        self.nextTest()

    def test_StormTrackM(self) :
        self.nextTest()

    def test_StormTrackN(self) :
        self.nextTest()

    def test_StormTrackO(self) :
        self.nextTest()

    def test_StormTrackP(self) :
        self.nextTest()

    def test_StormTrackQ(self) :
        self.nextTest()

    def test_StormTrackR(self) :
        self.nextTest()

    def test_StormTrackS(self) :
        self.nextTest()

    def test_StormTrackT(self) :
        self.nextTest()

    def test_StormTrackU(self) :
        self.nextTest()

    def test_StormTrackV(self) :
        self.nextTest()

    def test_StormTrackW(self) :
        self.nextTest()

    def test_StormTrackX(self) :
        self.nextTest()

    def test_StormTrackY(self) :
        self.nextTest()

    def test_StormTrackZ(self) :
        self.nextTest()

if __name__ == '__main__':
    unittest.main()
#
