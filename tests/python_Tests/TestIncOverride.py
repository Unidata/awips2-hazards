#!/usr/bin/env python

import os
import sys
import subprocess
import json
import traceback
import unittest
import time
import copy

from updateSysPath import updateSysPath
from testCaseSupportRoutines import formatForUnitTesting
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
    accessType = os.environ.get("LOCALIZATION_DATA_SOURCE", "")
    if accessType == "" :
        os.environ["LOCALIZATION_DATA_SOURCE"] = "OVERRIDE"
    baselinePaths = [ "edexOsgi/com.raytheon.uf.tools.cli/impl/src", \
                      "pythonPackages/ufpy" ]
    rootDir = updateSysPath(fromSibling=baselinePaths)
    from LocalizationInterface import LocalizationInterface
    myLI = LocalizationInterface("")
    setUpDone = True

# This is a unit test that verifies that some specific cases of incremental
# override work correctly.
class TestIncOverride(unittest.TestCase):

    def setUp(self):
        noteSpecificTestModule(__file__)
        setUpCore()

    def test_case_python_data(self) :
        testLocFile = "hazardServices/hazardTypes/HazardTypes.py"
        sys.stdout.write("Testing python data incremental override.\n")

        baseData = myLI.getLocData(testLocFile, 'COMMON_STATIC', 'Base')
        if not isinstance(baseData, dict) :
            sys.stderr.write("Could not read base for: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        overrideData = '''
HazardTypes = { \
    'AF.W' :  { 'hazardConflictList': ['AF.A'],
                'expirationTime': (-60, 60),
                 'inclusionTest' : False }
}
'''
        isOK = myLI.putLocFile(overrideData, testLocFile, \
                               'COMMON_STATIC', 'User', '')
        if not isOK :
            sys.stderr.write("Could not put user file for: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        overrideData = myLI.getLocData(testLocFile, 'COMMON_STATIC')
        if not isinstance(baseData, dict) :
            sys.stderr.write("Could not read: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        # print json.dumps(overrideData, indent=4, sort_keys=True)+"\n"

        if baseData == overrideData :
            sys.stderr.write("Override ineffective for: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        overrideAFW = overrideData['AF.W']
        baseAFW = baseData['AF.W']
        if baseAFW['expirationTime'] != overrideAFW['expirationTime'] :
            sys.stderr.write("Lock on expirationTime failed for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")
        if baseAFW['hazardConflictList'] == overrideAFW['hazardConflictList'] :
            sys.stderr.write("Update on hazardConflictList failed for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")
        if baseAFW['inclusionTest'] == overrideAFW['inclusionTest'] :
            sys.stderr.write("Update on inclusionTest failed for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")
       
        del overrideAFW['hazardConflictList'][1];
        overrideAFW['inclusionTest'] =  True;
        overrideData['AF.W'] = overrideAFW;

        if baseData != overrideData :
            sys.stderr.write("Override worked incorrectly for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        isOK = myLI.rmLocFile(testLocFile, 'COMMON_STATIC', 'User', '')
        if not isOK :
            sys.stderr.write("Could not remove user file for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        self.assertTrue(True)


    def test_case_python_class(self) :

        from LatLonCoord import LatLonCoord
        from Motion import Motion

        testLocFile = "python/trackUtilities/PointTrack.py"
        sys.stdout.write("Testing python class incremental override.\n")

        overrideCode = '''
class PointTrack:

    def extraMethod(self) :
        return "extra method output"

    def getSpeedAndAngle(self) :
        return Motion(3, 33)
'''
        overrideYes = True
        if overrideYes :
            isOK = myLI.putLocFile(overrideCode, testLocFile, \
                                   'COMMON_STATIC', 'User', '')
        else :
            isOK = True
        if not isOK :
            sys.stderr.write("Could not put user file for: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        overrideCode = myLI.getLocFile(testLocFile, 'COMMON_STATIC')
        if not isinstance(overrideCode, str) :
            sys.stderr.write("Could not read: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        if overrideYes :
            isOK = myLI.rmLocFile(testLocFile, 'COMMON_STATIC', 'User', '')
        else :
            isOK = True
        if not isOK :
            sys.stderr.write("Could not remove user file for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        # Would like to just exec the overriden class, but some of the
        # imports inside the class fail, so will use a temporary copy of
        # the class as a file, even though this is ugly.
        direct = False
        if direct :
            exec overrideCode
            from PointTrack import PointTrack
        else :
            scratchPath = sys.path[0]+"/PointTrack.py"
            ffff = open(scratchPath, "w")
            ffff.write(overrideCode)
            ffff.close()
            from PointTrack import PointTrack
            os.remove(scratchPath)

        pointTrack = PointTrack()
        latLon0 = LatLonCoord(40, -105)
        motion0 = Motion(20, 45)
        time0 = int(time.time())
        pointTrack.latLonMotionOrigTimeInit_(latLon0, time0, motion0, time0)
        if time0!=pointTrack.PT_getOrigTime() or \
           not motion0.same(pointTrack.speedAndAngleOf(time0)) :
            sys.stderr.write("override class not working correctly\n")
            self.assertEqual("TEST_CASE","OK")
        time1 = time0+1800000
        latLon1 = pointTrack.trackPoint(time1)
        invert = pointTrack.timeAndLeft(latLon1)
        if int(invert[0]+0.5)!=time1 or int(invert[1]*10)!=0 or \
           latLon1.same(latLon0) :
            sys.stderr.write("override class not working correctly\n")
            self.assertEqual("TEST_CASE","OK")

        try :
            ok = pointTrack.extraMethod()=="extra method output"
        except :
            ok = False
        if motion0.same(pointTrack.getSpeedAndAngle()) or not ok :
            sys.stderr.write("override class in unexpected state\n")
            self.assertEqual("TEST_CASE","OK")

        if not direct :
            os.remove(scratchPath+"c")

        self.assertTrue(True)


    def test_case_xml(self) :
        testLocFile = "hazardServices/alerts/HazardAlertsConfig.xml"
        sys.stdout.write("Testing XML incremental override.\n")

        baseData = myLI.getLocData(testLocFile, 'COMMON_STATIC', 'Base')
        if not isinstance(baseData, dict) :
            sys.stderr.write("Could not read base for: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        overrideData = \
'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   <hazardAlerts>
      <eventExpiration>
         <configuration override="byKey category">
            <category>Flood Watch</category>
            <criteria override="byKey name">
               <name>1st warning</name>
               <expirationTime>7</expirationTime>
            </criteria>
         </configuration>
      </eventExpiration>
   </hazardAlerts>
'''
        isOK = myLI.putLocFile(overrideData, testLocFile, \
                               'COMMON_STATIC', 'User', '')
        if not isOK :
            sys.stderr.write("Could not put user file for: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        overrideData = myLI.getLocData(testLocFile, 'COMMON_STATIC')
        if not isinstance(baseData, dict) :
            sys.stderr.write("Could not read: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        isOK = myLI.rmLocFile(testLocFile, 'COMMON_STATIC', 'User', '')
        if not isOK :
            sys.stderr.write("Could not remove user file for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        if baseData == overrideData :
            sys.stderr.write("Override ineffective for: "+testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        config = \
         overrideData["hazardAlerts"]["eventExpiration"]["configuration"]
        for member in config :
            if member['category'] != 'Flood Watch' :
                continue
            i = 0
            newCrit = []
            for crit in member['criteria'] :
                if crit.get("name")=="1st warning" :
                    critcopy = copy.deepcopy(crit)
                    critcopy["expirationTime"] = "8"
                    newCrit.append(critcopy)
                else :
                    newCrit.append(crit)
            member['criteria'] = newCrit

        baseTest = formatForUnitTesting(baseData)
        overrideTest = formatForUnitTesting(overrideData)
        if baseTest != overrideTest :
            sys.stderr.write("Override worked incorrectly for: "+ \
                             testLocFile+"\n")
            self.assertEqual("TEST_CASE","OK")

        self.assertTrue(True)

if __name__ == '__main__':
    unittest.main()
#
