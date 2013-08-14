#!/usr/bin/env python

import os
import sys
import subprocess
import json
import traceback
import unittest

from testCaseSupportRoutines import updateSysPath
from testCaseSupportRoutines import setTestCaseLocation
from testCaseSupportRoutines import getNextTestFile

# Put the actual logic for the setup outside the class because we do not
# want to run the crucial part of this logic multiple times.
setUpDone = False
testCount = 0
myLI = None
def setUpCore():
    global setUpDone
    global testCount
    global myLI
    if setUpDone :
        return
    edexPython = \
     "edexOsgi/gov.noaa.gsd.common.utilities/utility/"+ \
     "common_static/base/python"
    fromRoot = []
    fromRoot.append(edexPython+"/logUtilities")
    fromRoot.append(edexPython+"/bridge")
    rootDir = updateSysPath("hazardServices", fromRoot)
    metaRoot = rootDir+\
       "/edexOsgi/gov.noaa.gsd.uf.common.dataplugin.hazards/utility/"+ \
       "common_static/base"
    setTestCaseLocation(metaRoot, "hazardServices/hazardMetaData", "*.py")
    from LocalizationInterface import LocalizationInterface
    myLI = LocalizationInterface("")
    setUpDone = True
    testCount = 0

# This is a unit test that verifies that all hazard metadata parses without
# problems.  Does not verify the content.
class TestMetadataParse(unittest.TestCase):

    def setUp(self):
        setUpCore()

    # Test itself needs to be a member method in order to use the
    # assertEqual() method.
    def nextTest(self) :
        global myLI
        global testCount
        if testCount==999999 :
            self.skipTest("")
            return
        locPath = getNextTestFile()
        if locPath==None :
            sys.stdout.write("\n"+str(testCount)+" tests implemented.\n")
            testCount = 999999
            self.skipTest("")
            return
        testCount = testCount+1
        result = myLI.getLocData(locPath, "COMMON_STATIC", "Base", "")
        sys.stdout.write("\n"+locPath+"\n")
        sys.stdout.flush()
        if isinstance(result, dict) or isinstance(result, list) :
           self.assertEqual("OK","OK")
        else :
           self.assertEqual("PARSE of "+locPath,"OK")

    def test_MetadataParseA(self) :
        self.nextTest()

    def test_MetadataParseB(self) :
        self.nextTest()

    def test_MetadataParseC(self) :
        self.nextTest()

    def test_MetadataParseD(self) :
        self.nextTest()

    def test_MetadataParseE(self) :
        self.nextTest()

    def test_MetadataParseF(self) :
        self.nextTest()

    def test_MetadataParseG(self) :
        self.nextTest()

    def test_MetadataParseH(self) :
        self.nextTest()

    def test_MetadataParseI(self) :
        self.nextTest()

    def test_MetadataParseJ(self) :
        self.nextTest()

    def test_MetadataParseK(self) :
        self.nextTest()

    def test_MetadataParseL(self) :
        self.nextTest()

    def test_MetadataParseM(self) :
        self.nextTest()

    def test_MetadataParseN(self) :
        self.nextTest()

    def test_MetadataParseO(self) :
        self.nextTest()

    def test_MetadataParseP(self) :
        self.nextTest()

    def test_MetadataParseQ(self) :
        self.nextTest()

    def test_MetadataParseR(self) :
        self.nextTest()

    def test_MetadataParseS(self) :
        self.nextTest()

    def test_MetadataParseT(self) :
        self.nextTest()

    def test_MetadataParseU(self) :
        self.nextTest()

    def test_MetadataParseV(self) :
        self.nextTest()

    def test_MetadataParseW(self) :
        self.nextTest()

    def test_MetadataParseX(self) :
        self.nextTest()

    def test_MetadataParseY(self) :
        self.nextTest()

    def test_MetadataParseZ(self) :
        self.nextTest()

if __name__ == '__main__':
    unittest.main()
#
