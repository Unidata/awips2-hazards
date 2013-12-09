#!/usr/bin/env python

import unittest
import os
import sys

from testCaseSupportRoutines import noteSpecificTestModule
from testCaseSupportRoutines import UnitTestFramework
from updateSysPath import updateSysPath

# Put the actual logic for the setup outside the class because we do not
# want to run the crucial part of this logic multiple times.
setUpDone = False
myLI = None
myMI = None
def setUpCore():
    global setUpDone
    global myLI
    global myMI
    if setUpDone :
        return
    accessType = os.environ.get("LOCALIZATION_DATA_SOURCE", "")
    if accessType == "" :
        os.environ["LOCALIZATION_DATA_SOURCE"] = "OVERRIDE"
    baselinePaths = [ "edexOsgi/com.raytheon.uf.tools.cli/impl/src", \
                      "pythonPackages/ufpy" ]
    rootDir = updateSysPath(fromSibling=baselinePaths)
    from LocalizationInterface import LocalizationInterface
    myLI = LocalizationInterface("")
    if False and os.environ.get("LOCALIZATION_DATA_SOURCE")=="EDEX" :
        codeImport = myLI.getLocFile( \
           "python/python/shapeUtilities/MapInfo.py",
           "COMMON_STATIC")
        exec codeImport
    from MapInfo import MapInfo
    myMI = MapInfo()
    setUpDone = True

# This is a unit test of the track initializer, StormTrackTool.
class TestPartOfState(UnitTestFramework):

    def setUp(self) :
        noteSpecificTestModule(__file__)
        setUpCore()
        # The Data Access Framework, which the MapInfo class is completely
        # dependent on, cannot interact with the code base to get its data.
        msg = "MapInfo needs DAF and thus needs a local EDEX."
        self.mustHaveLocalEDEX(msg)

    # 
    def performNextTest(self, inputTestData, expectedResult) :
        global myLI
        global myMI
        testLocFile = "hazardServices/productGeneratorTable/feAreaTable.xml"
        polygon = inputTestData["inputPolygon"]
        site = inputTestData["siteID"]
        haveOverride = "overrideXml" in inputTestData
        if haveOverride :
            overrideXmlLines = inputTestData["overrideXml"]
            overrideData = ""
            for line in overrideXmlLines :
                overrideData += line+"\n"
            isOK = myLI.putLocFile(overrideData, testLocFile, \
                                   'COMMON_STATIC', 'User', '')
            if not isOK :
                sys.stderr.write("Could not put user file for: "+ \
                                 testLocFile+"\n")
                self.assertEqual("TEST_CASE","OK")
        #
        # Tell the MapInfo class to reinitialize the parts of state table.
        myMI.getPartOfStateFromGeom(True)
        #
        geomList = myMI.getMapPolygons("counties", [ polygon ], siteID=site)
        partOfStateList = []
        for geom in geomList :
            partOfStateList.append(myMI.getPartOfStateFromGeom(geom))
        if haveOverride :
            isOK = myLI.rmLocFile(testLocFile, 'COMMON_STATIC', 'User', '')
            if not isOK :
                sys.stderr.write("Could not remove user file for: "+ \
                                 testLocFile+"\n")
                self.assertEqual("TEST_CASE","OK")
        if False :
            print str(partOfStateList)
            print str(expectedResult)
        if len(partOfStateList)!=len(expectedResult) :
            sys.stderr.write(str(len(geomList))+" geometries returned, "+ \
                             str(len(expectedResult))+" expected.\n")
            self.assertEqual("TEST_CASE","OK")
        for partOfState in partOfStateList :
            if not partOfState in expectedResult :
                sys.stderr.write("Part of state "+partOfState+\
                                 " not expected.\n")
                self.assertEqual("TEST_CASE","OK")
            expectedResult.remove(partOfState)
        self.assertTrue(True)

if __name__ == '__main__':
    unittest.main()
#
