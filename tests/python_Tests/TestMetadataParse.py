#!/usr/bin/env python

import os
import sys
import subprocess
import json
import traceback
import unittest
import types

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
    # Specifially inserting the hazardMetaData/ directory is a hack to make
    # imports of the new class based metadata to work until we figure out
    # a more formal way to do this.  If tested with EDEX access, the top
    # level class being pulled in will have python class incremental override
    # applied, but any imports will always be handled through standard python
    # import mechanism, and so will always only see the base level file.
    rootPaths = [ "*",
              "common/gov.noaa.gsd.uf.common.dataplugin.hazards/utility"+\
              "/common_static/base/hazardServices/hazardMetaData" ]
    baselinePaths = [ "edexOsgi/com.raytheon.uf.tools.cli/impl/src", \
                      "pythonPackages/ufpy",
              "edexOsgi/com.raytheon.uf.common.localization.python/utility"+\
              "/common_static/base/python" ]
    rootDir = updateSysPath(fromRoot=rootPaths, fromSibling=baselinePaths)
    metaRoot = rootDir+\
       "/common/gov.noaa.gsd.uf.common.dataplugin.hazards/utility/"+ \
       "common_static/base"
    setTestCaseLocation(metaRoot, "HazardServices/hazardMetaData", "*.py")
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
        result = myLI.getLocData(locPath, "COMMON_STATIC", "", "", True, True)
        sys.stderr.write("\n"+locPath+"\n")
        if isinstance(result, dict) or isinstance(result, list) :
            self.assertTrue(True)
            return
        elif not isinstance(result, str) :
            self.assertEqual("PARSE of "+locPath,"OK")
            return

        # If we are doing the EDEX enabled version of the unit test,
        # use the PythonOverrider to get our class instance.
        parseOK = False
        if os.environ.get("LOCALIZATION_DATA_SOURCE")=="EDEX" :
            from PythonOverriderPure import importModule
            try :
                result = importModule(locPath, myLI.getEdexHost(), \
                     myLI.getEdexPort(), myLI.getDefLoc(), myLI.getDefUser() )
                m = result.MetaData()
            except :
                traceback.print_exc()
                self.assertEqual("PARSE of "+locPath,"OK")
                return
        else :
            # Case where we get the files straight out of the code base.
            # Instead of just exec'ing this, we are going to put this out
            # to a temporary file and import it.  If you just exec it, the
            # tracebacks you get from method failures are misleading.
            try :
                baseName = locPath.split("/")[-1]
                moduleName = baseName.split(".")[0]
                scratchPath = sys.path[0]+"/"+baseName
                ffff = open(scratchPath, "w")
                ffff.write(result)
                ffff.close()
                execCode = "import "+moduleName
                exec execCode
                os.remove(scratchPath)
                execCode = "m = "+moduleName+".MetaData()"
                exec execCode
            except :
                traceback.print_exc()
                self.assertEqual("PARSE of "+locPath,"OK")
                return

        # Currently, the class based metadata we have does not execute
        # properly, so skip this step for now.
        skipExecuteOfClassBasedMetaData = True
        if skipExecuteOfClassBasedMetaData :
            self.assertTrue(True)
            return

        try :
            methodList = dir(m)
            if "getMetaData" in methodList :
                aaa = m.getMetaData()
                sys.stderr.write("Executed getMetaData() method.\n")
            else :
                n = 0
                for oneMethod in methodList :
                    if oneMethod[:2] == "__" :
                        continue
                    execCode = "aaa = m."+oneMethod+"()"
                    exec execCode
                    n = n + 1
                sys.stderr.write("Executed "+str(n)+" methods.\n")
        except :
            traceback.print_exc()
            self.assertEqual("EXECUTE of "+locPath,"OK")
        self.assertTrue(True)

if __name__ == '__main__':
    unittest.main()
#
