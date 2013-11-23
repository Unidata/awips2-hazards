#!/usr/bin/env python

import os
import sys
import subprocess
import json
import traceback
import unittest

from updateSysPath import updateSysPath
from testCaseSupportRoutines import setTestCaseLocation
from testCaseSupportRoutines import getNextTestFile
from testCaseSupportRoutines import UnitTestFramework
from testCaseSupportRoutines import noteSpecificTestModule

# Put the actual logic for the setup outside the class because we do not
# want to run the crucial part of this logic multiple times.
setUpDone = False
myLI = None
def setUpCore():
    global setUpDone
    global myLI
    if setUpDone :
        return
    baselinePaths = [ "edexOsgi/com.raytheon.uf.tools.cli/impl/src", \
                      "pythonPackages/ufpy" ]
    rootDir = updateSysPath(fromSibling=baselinePaths)
    metaRoot = rootDir+\
       "/edexOsgi/gov.noaa.gsd.uf.common.dataplugin.hazards/utility/"+ \
       "common_static/base"
    setTestCaseLocation(metaRoot, "hazardServices/hazardMetaData", "*.py")
    from LocalizationInterface import LocalizationInterface
    myLI = LocalizationInterface("")
    setUpDone = True

# This is a unit test that verifies that all hazard metadata parses without
# problems.  Does not verify the content.
class TestMetadataParse(UnitTestFramework):

    def setUp(self):
        noteSpecificTestModule(__file__)
        self.onBehalfOfFilePaths()
        setUpCore()

    # Test itself needs to be a member method in order to use the
    # assertEqual() method.
    def performNextTest(self, locPath) :
        global myLI
        result = myLI.getLocData(locPath, "COMMON_STATIC", "Base", "")
        sys.stdout.write("\n"+locPath+"\n")
        sys.stdout.flush()
        if isinstance(result, dict) or isinstance(result, list) :
           self.assertTrue(True)
        else :
           self.assertEqual("PARSE of "+locPath,"OK")

if __name__ == '__main__':
    unittest.main()
#
