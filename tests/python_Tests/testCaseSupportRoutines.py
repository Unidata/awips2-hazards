"""
Description: 
This file contains static global methods meant to help set up the reading
in of test cases for python unit tests, plus a framework class to help ease
setting them up.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Aug 16, 2013            JRamer      Pulled out of storm track unit tests

@author James.E.Ramer@noaa.gov
@version 1.0
"""
import os
import sys
import traceback
import subprocess
import json
import unittest

from updateSysPath import updateSysPath

specificTestModuleName = "unknown"
def noteSpecificTestModule(fileFromModule) :
    '''
    @summary: This needs to be called with the __file__ variable from
              the specific test code as the first line of the setUp.  When a
              unit test is run from the command line, this is not needed
              because we can pick up the needed name from the traceback, but
              this fails when tests are run as a pyUnit test out of eclipse.
    @param fileFromModule: __file__ variable from specific test.
    '''
    global specificTestModuleName
    specificTestModuleName = fileFromModule

# Allows one to override the default location for test cases.
findWD = None
findPath = None
findName = None
def setTestCaseLocation(findWDarg, findPathArg, findNameArg=None) :
    '''
    @summary: Allows one to override the default location for test cases
    @param findWDarg: Working directory that find command is run from.
                      Set to an empty string for full paths.
    @param findPathArg: Directory path given to find command looking for
                        test cases.
    @param findNameArg: Name or glob pattern for files to find, defaults
                        to *.py.
    '''
    global findWD
    global findPath
    global findName
    if isinstance(findWDarg, str) or isinstance(findWDarg, unicode) :
        findWD = findWDarg
    else :
        findWD = os.environ["PWD"]
    if isinstance(findPathArg, str) or isinstance(findPathArg, unicode) :
        findPath = findPathArg
    else :
        findPath = "."
    if isinstance(findNameArg, str) or isinstance(findNameArg, unicode) :
        findName = findNameArg
    else :
        findName = "*.py"

# Returns test file for the next test case.  Returns None when there
# are no more test case files.  By default starts looking in the same
# directory this code is staged in, but in a subdirectory name the same
# as the unit test program without the leading "Test" but with a trailing
# "TestCases".  These default search parameters can be changed if the
# static global function setTestCaseLocation() is called.
inputTestCaseFiles = None
testCaseIndex = 0
testProgramName = ""
testCaseName = ""
def getNextTestFile() :
    '''
    @summary: Returns file paths containing test cases.
    @return: Path of file containing next test case.  None if no more test
             cases.
    '''
    global findWD
    global findPath
    global findName
    global inputTestCaseFiles
    global testCaseIndex
    global testProgramName
    global testCaseName
    global specificTestModuleName

    # Return next test case file if we already have list of test cases.
    if inputTestCaseFiles!=None :
        if testCaseIndex>=len(inputTestCaseFiles) :
            return None
        testCasePath = inputTestCaseFiles[testCaseIndex]
        testCaseIndex = testCaseIndex + 1
        return testCasePath

    # Use traceback to get name of the test program.
    tbData = traceback.format_stack()
    try :
        testProgramName = tbData[0].split('"')[1].split('/')[-1].split('.')[0]
        if testProgramName=="runfiles" :
            testProgramName = \
               specificTestModuleName.split('/')[-1].split('.')[0]
        if testProgramName[:4]=="Test" :
            testCaseName = testProgramName[4:]
        else :
            testCaseName = testProgramName
    except :
        testProgramName = ""
        testCaseName = ""

    # Try to get list of test case files from EDEX.
    if os.environ.get("LOCALIZATION_DATA_SOURCE")=="EDEX" and findWD!=None:
        typeArg = "COMMON_STATIC"
        if findWD.find("edex_static")>0 :
            typeArg = "EDEX_STATIC"
        elif findWD.find("cave_static")>0 :
            typeArg = "CAVE_STATIC"
        i = findName.find("*")
        if i<0 :
            prefix = findName
            suffix = ""
        else :
            prefix = findName[:i]
            suffix = findName[i+1:]
        # We still try to leverage source code directory structure to get the
        # proper list of EDEX directories to search.
        cmd = "( cd "+findWD+" ; " + "find "+findPath+" -type d )"
        from LocalizationInterface import LocalizationInterface
        myLI = LocalizationInterface("")
        try :
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
            (stdout, stderr) = p.communicate()
            locDirs = stdout.split("\n")
        except :
            locDirs = [ findPath ]
        for locDir in locDirs :
            if locDir=="" :
                continue
            cases = myLI.listLocFiles(locDir, typeArg, "", "", prefix, suffix)
            if cases==None :
                continue
            for case in cases :
                if case=="" or case==None :
                    continue
                if inputTestCaseFiles==None :
                    inputTestCaseFiles = [ ]
                inputTestCaseFiles.append( locDir+"/"+case )

    # Try to get test cases straight out of source code files.
    if inputTestCaseFiles!=None :
        pass
    elif findWD!=None :
        # Case where the default paths have been overridden.
        if findWD=="" :
            cmd = "find "+findPath+" ! -type d -name '"+findName+"'"
        else :
            cmd = "( cd "+findWD+" ; " + \
                  "find "+findPath+" ! -type d -name '"+findName+"' )"
        try :
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
            (stdout, stderr) = p.communicate()
            inputTestCaseFiles = stdout.split("\n")
            if len(inputTestCaseFiles)>0 :
                if inputTestCaseFiles[-1]=="" :
                   del inputTestCaseFiles[-1]
        except :
            inputTestCaseFiles = []
            sys.stderr.write("User supplied paths to test cases failed.\n")
            return None
    else :
        if testCaseName=="" :
            sys.stderr.write("Could not identify name of unit test program.\n")
            return None

        # Case where we use default location.
        # First, identify directory of this source code.
        inputTestCaseFiles = []
        here = os.environ["PWD"]
        me = __file__
        if me[0]!="/" :
            me = here+"/"+me
        i = len(me)-1
        while me[i]!='/' :
            i = i - 1
        mydir = me[:i]

        # Make subdirectory name out of test program name, find all files in
        # there and present as test cases.  Avoid editor journal files and
        # python byte code files.
        testName = mydir+"/"+testCaseName+"TestCases"
        cmd = "find "+testName+" ! -type d"
        try :
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
            (stdout, stderr) = p.communicate()
            inputTestCaseFiles = stdout.split("\n")
            i = len(inputTestCaseFiles)-1
            while i>=0 :
                if len(inputTestCaseFiles[i])==0 :
                    del inputTestCaseFiles[i]
                elif inputTestCaseFiles[i][-1]=="~" or \
                     inputTestCaseFiles[i][-4:]==".pyc" :
                    del inputTestCaseFiles[i]
                i = i-1
        except :
            inputTestCaseFiles = []
            sys.stderr.write("Error finding test cases.\n")
            return None

    # Return first test case file.
    testCaseIndex = 1
    if len(inputTestCaseFiles)==0 :
        return None
    return inputTestCaseFiles[0]

# Returns data structures for next test case as a tuple of
# (Input test data, Expected result).  Assumes python files contain
# variable assignments for variables InputTestCaseData and possibly
# TestCaseResults.
def getNextTestCase() :
    '''
    @summary: Returns a test case based on reading from files whose paths
              are supplied by the testFileContents method.
    @return: A test case tuple of (Input test data, Expected result).
             If both are None, then no more test cases.  If first tuple
             element is boolean False, then second is a file from which
             there was a problem obtaining test data.
    '''
    testFile = getNextTestFile()
    if testFile == None :
        return (None, None)
    testFileContents = ""
    try :
        ffff = open(testFile, "r")
        testFileContents = ffff.read()
        ffff.close()
    except :
        sys.stderr.write("Could not read "+testFile+"\n")
        return (False, testFile)

    if len(testFileContents)<2 :
        sys.stderr.write("No data in "+testFile+"\n")
        return (False, testFile)

    if testFile[-3:]!=".py" :
        return (testFileContents, None)

    inputTestData = None
    outputTestResult = None
    try:
        exec testFileContents
        inputTestData = eval("InputTestCaseData")
        outputTestResult = eval("TestCaseResults")
    except:
        if inputTestData==None :
            sys.stderr.write("Error parsing "+testFile+"\n")
            traceback.print_exc()
    if inputTestData==None :
        return (False, testFile)
    return (inputTestData, outputTestResult)


def findUnescapedCharacter(string, char, start=0) :
    '''
    @summary: Find first occurrence of a character in a string, on the condition
              the character is not `escaped` (e.g. preceded by backslash).
    @param string: String to search in.
    @param char: Character we are searching for.
    @param start: Index of string to begin search at. 
    @return: Index of character, or -1 on failure.
    '''
    charIndex = string.find(char, start)
    while charIndex>0 :
        if string[charIndex-1]!="\\" :
            return charIndex
        start = charIndex+1
        charIndex = string.find(char, start)
    return charIndex


def findFirstCharOf(string, charList, start=0) :
    '''
    @summary: Find first occurrence of any character in provided list of
              character.  Will not identify `escaped` characters.
    @param string: String to search in.
    @param charList: List of characters we are searching for.
    @param start: Index of string to begin search at. 
    @return: Index of character, or -1 if none of the provided characters found.
    '''
    firstIndex = sys.maxint
    for char in charList :
        index = findUnescapedCharacter(string, char, start)
        if index>=start and index<firstIndex :
            firstIndex = index
    if firstIndex==sys.maxint :
        return -1
    return firstIndex


def findNextQuotedString(string, start=0) :
    '''
    @summary: Returns tuple of string indices of quote marks for the first
              quoted string found after given starting location.
    @param string: String to search in.
    @param start: Index of string to begin search at. 
    @return: Tuple for index of first and last quote marks of quoted string.
             If a quote mark is not found instead return the length of the
             string (rather than -1) for its location. This is non-standard,
             but works better in this application.
    '''
    strlen = len(string)
    openQuote = findFirstCharOf(string, ["'", '"'], start)
    if openQuote<0 :
        return (strlen, strlen)
    closeQuote = findUnescapedCharacter( \
          string, string[openQuote], openQuote+1)
    if closeQuote<0 :
        return (openQuote, strlen)
    return (openQuote, closeQuote+1)


def findNextFloatingPointNumber(string, start, end) :
    '''
    @summary: within the given range of indices of an ascii string, find
              the first occurence of a floating poing number
    @param string: String possibly containing an ascii floating point number
    @param start: index to begin search
    @param end: index that end search
    @return: tri-tuple with string indices; (first digit of number, location of
             decimal point, index just past last digit).  All tuple members -1
             if no floating poing number found
    '''
    nextDecimal = string.find(".", start, end)
    while nextDecimal>=0 :
        startOfNumber = nextDecimal
        while startOfNumber>start and string[startOfNumber-1].isdigit() :
            startOfNumber = startOfNumber-1
        endOfNumber = nextDecimal+1
        while endOfNumber<end and string[endOfNumber].isdigit() :
            endOfNumber = endOfNumber+1
        if endOfNumber-startOfNumber>1 :
            return (startOfNumber, nextDecimal, endOfNumber)
        nextDecimal = string.find(".", nextDecimal+1, end)
    return (-1, -1, -1)

def findSignificantDigits(string, startOfNumber, decimalPoint, \
                          endOfNumber, precision=6) :
    '''
    @summary: returns a tuple indicating the range of significant digits for
              an ascii representation of a floating point number, given the
              requested precision.
    @param string: String containing ascii floating point number
    @param startOfNumber: index of first digit of number
    @param decimalPoint: index of decimalPoint
    @param endOfNumber: index just past last digit of number
    @return: tuple with range of significant digits
    '''
    startSigDigits = startOfNumber
    while startSigDigits<endOfNumber and string[startSigDigits]<"1" :
        startSigDigits = startSigDigits+1
    if startSigDigits>=endOfNumber :
        return (-1, -1)
    endSigDigits = startSigDigits+precision
    if endSigDigits>decimalPoint :
        endSigDigits = endSigDigits+1
    if endSigDigits>endOfNumber :
        endSigDigits = endOfNumber
    while string[endSigDigits-1]<"1" :
        endSigDigits = endSigDigits-1
    return (startSigDigits, endSigDigits)


def truncateNumberToSigDigits(string, startSigDigits, \
                              decimalPoint, endSigDigits) :
    '''
    @summary: returns truncated ascii representation of floating point number
              occuring inside of variable 'string' based on indicated
              range of significant digits.
    @param string: String containing ascii floating point number
    @param startSigDigits: index of first significant digit
    @param decimalPoint: index of decimalPoint
    @param endSigDigits: index just past last significant digit
    @return: truncated ascii representation of floating point number
    '''
    if startSigDigits<0 :
        return "0.0"
    if startSigDigits>=decimalPoint :
        return string[decimalPoint:endSigDigits]
    if endSigDigits>decimalPoint :
        return string[startSigDigits:endSigDigits]
    if endSigDigits==decimalPoint :
        return string[startSigDigits:endSigDigits+1]
    return string[startSigDigits:endSigDigits] + \
           "0"*(decimalPoint-endSigDigits) + "."


# Formats data structures in a useful way for unit testing.  Leverages the
# json library to guarantee dictionaries do not test as different solely
# because they hashed differently.
def formatForUnitTesting(testData, precision=6, doIndent=False) :
    '''
    @summary: Formats data structures in a useful way for unit testing
    @param testData: arbitrary JSON serializable data structure for testing
    @param precision: digits of precision to retain for floating point numbers
    @param doIndent: boolean for whether to indenting.
    @return: String suitable for doing equivalence testing for unit tests.
    '''
    # Use json to format this with keys sorted
    if doIndent :
        jsonResult = json.dumps(testData, indent=4, sort_keys=True)
    else :
        jsonResult = json.dumps(testData, sort_keys=True)
    lenResult = len(jsonResult)
    finalResult = ""

    i = 0
    while i<lenResult :
        (openQuote, closeQuote) = \
           findNextQuotedString(jsonResult, i)
        (startOfNumber, decimalPoint, endOfNumber) = \
           findNextFloatingPointNumber(jsonResult, i, openQuote)
        while startOfNumber>=0 :
            finalResult += jsonResult[i:startOfNumber]
            (startSigDigits, endSigDigits) = \
                  findSignificantDigits(jsonResult, startOfNumber, \
                              decimalPoint, endOfNumber, precision)
            finalResult += truncateNumberToSigDigits( \
                  jsonResult, startSigDigits, decimalPoint, endSigDigits)
            i = endOfNumber
            (startOfNumber, decimalPoint, endOfNumber) = \
               findNextFloatingPointNumber(jsonResult, i, openQuote)
        finalResult += jsonResult[i:closeQuote]
        i = closeQuote

    return finalResult

def defaultRecommenderSetup(fileFromModule) :
    '''
    @summary: This encapsulates a bare minimum amount of setUp code for
              recommender tests that have their test cases in a subdirectory.
              This needs to be called with the __file__ variable from
              the specific test code as the first line of the setUp.  When a
              unit test is run from the command line, this is not needed
              because we can pick up the needed name from the traceback, but
              this fails when tests are run as a pyUnit test out of eclipse.
    @param fileFromModule: __file__ variable from specific test.
    '''
    global frameworkSetUpDone
    global specificTestModuleName
    if frameworkSetUpDone :
        return
    specificTestModuleName = fileFromModule
    baselinePaths = [ "edexOsgi/com.raytheon.uf.tools.cli/impl/src", \
                      "pythonPackages/ufpy",
                      "edexOsgi/com.raytheon.uf.common.localization.python/utility"+ \
                      "/common_static/base/python" ]
    updateSysPath(fromSibling=baselinePaths)
    frameworkSetUpDone = True


# This class is a intermediate class for performing unit tests using the python
# unit test framework.  Implementor must derive from this class and at a
# minimum supply a derived class specific version of performNextTest().  See
# existing tests for examples.
frameworkTestCount = 0
frameworkDerivedObject = None
frameworkSetUpDone = False
class UnitTestFramework(unittest.TestCase):

    # Every specific unit test must supply its own setUp, wherein either
    # defaultRecommenderSetup or noteSpecificTestModule needs to be called
    # with the __file__ variable as an argument.
    # def setUp(self):
    #    -- either --
    #    noteSpecificTestModule(__file__)
    #    my specific setup code
    #
    #    -- or --
    #
    #    defaultRecommenderSetup(__file__)

    # Every specific unit test must supply its own performNextTest(),
    # which has the specific code that performs the test in question.
    # By default, this method must be defined like:
    #
    #   def performNextTest(self, inputTestData, expectedResult) :
    #
    # unless onBehalfOfFilePaths was called in the setup, in which case the
    # method must be defined like:
    #
    #   def performNextTest(self, testFilePath) :

    # Call this in user supplied setUp to test files instead of test cases.
    def onBehalfOfFilePaths(self):
        self.useFilePaths = True

    # Call this if a local EDEX must be accessible even if primary test data
    # is coming out of code base.
    def mustHaveLocalEDEX(self, msg=None):
        global frameworkTestCount
        if frameworkTestCount!=0 :
            return
        if os.environ.get("LOCALIZATION_DATA_SOURCE", "")=="EDEX":
            return
        cmd = "ps -U awips -f | grep edex | wc -l"
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        n = int(stdout.split("\n")[0])
        if n>=3 :
            return
        if isinstance(msg, str) :
            sys.stdout.write("\n"+msg+"\n")
        else :
            sys.stdout.write("\nThis test cannot run without a local EDEX.\n")
        frameworkTestCount = sys.maxint

    # Logic that gets the next test needs to be a member method in order to use
    # the skipTest method.
    def nextTest(self) :
        global frameworkTestCount

        # Record instance of derived class in a global variable, so we can
        # always use that to access the performNextTest() method.
        global frameworkDerivedObject
        if frameworkDerivedObject==None :
            if self.__class__.__name__=="UnitTestFramework" :
                self.skipTest("")
                return
            frameworkDerivedObject = self

        if frameworkTestCount==sys.maxint :
            self.skipTest("")
            return
        try :
            byPaths = self.useFilePaths
        except :
            byPaths = False

        # Pass paths to test files into performNextTest()
        if byPaths :
            locPath = getNextTestFile()
            if locPath==None :
                sys.stdout.write("\n"+str(frameworkTestCount)+ \
                                 " tests implemented.\n")
                frameworkTestCount = sys.maxint
                self.skipTest("")
                return
            frameworkTestCount = frameworkTestCount+1
            frameworkDerivedObject.performNextTest(locPath)
            return

        # Default behavior of passing test cases into performNextTest()
        (inputTestData, expectedResult) = getNextTestCase()
        if inputTestData==None :
            sys.stdout.write("\n"+str(frameworkTestCount)+ \
                             " tests implemented.\n")
            frameworkTestCount = sys.maxint
            self.skipTest("")
            return
        frameworkTestCount = frameworkTestCount+1
        if "caseDesc" in inputTestData :
            sys.stdout.write("\n"+inputTestData["caseDesc"]+"\n")
        frameworkDerivedObject.performNextTest(inputTestData, expectedResult)

    # Reports differences in test cases in a focused way that will usually
    # only show the differences, rather than printing out the entire test case
    # contents.
    def reportTestCaseOutcome(self, expected, result, precision=6) :
        '''
        @summary: User friendly reporting of differences in test cases.
        @param unitTestClass: self of unit test class this is called from.
        @param expected: expected test case output
        @param result: actual test case outpyt
        @param precision: digits of precision for floating point comparisons.
        '''
        global testCaseName
        global testCaseIndex
        if formatForUnitTesting(expected, precision) == \
           formatForUnitTesting(result, precision) :
            self.assertTrue(True)
            return
        fileSuffix = str(10000+testCaseIndex)[1:]
        if testCaseName=="" :
            fileSuffix = str(os.getpid())+"."+fileSuffix
        else :
            fileSuffix = testCaseName+"."+fileSuffix
        tmpExpected = "/tmp/expected"+fileSuffix
        tmpResult = "/tmp/result"+fileSuffix
        ffff = open(tmpExpected, "w")
        ffff.write(formatForUnitTesting(expected, precision, True))
        ffff.close()
        os.chmod(tmpExpected, 0666)
        ffff = open(tmpResult, "w")
        ffff.write(formatForUnitTesting(result, precision, True))
        ffff.close()
        os.chmod(tmpResult, 0666)
        cmd = "diff -c "+tmpExpected+" "+tmpResult
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        sys.stderr.write(stdout+"\n")
        self.assertEqual("TEST_CASE","OK")

    # Allows one to find up to 50 test cases, uncomment/add more if needed.
    # Because of a quirk in how the unit test class find test case members,
    # we get two tests for each of these, one for the derived class and one
    # for this class.
    def test_case_0001(self) :
        self.nextTest()

    def test_case_0002(self) :
        self.nextTest()

    def test_case_0003(self) :
        self.nextTest()

    def test_case_0004(self) :
        self.nextTest()

    def test_case_0005(self) :
        self.nextTest()

    def test_case_0006(self) :
        self.nextTest()

    def test_case_0007(self) :
        self.nextTest()

    def test_case_0008(self) :
        self.nextTest()

    def test_case_0009(self) :
        self.nextTest()

    def test_case_0010(self) :
        self.nextTest()

    def test_case_0011(self) :
        self.nextTest()

    def test_case_0012(self) :
        self.nextTest()

    def test_case_0013(self) :
        self.nextTest()

    def test_case_0014(self) :
        self.nextTest()

    def test_case_0015(self) :
        self.nextTest()

    def test_case_0016(self) :
        self.nextTest()

    def test_case_0017(self) :
        self.nextTest()

    def test_case_0018(self) :
        self.nextTest()

    def test_case_0019(self) :
        self.nextTest()

    def test_case_0020(self) :
        self.nextTest()

    def test_case_0021(self) :
        self.nextTest()

    def test_case_0022(self) :
        self.nextTest()

    def test_case_0023(self) :
        self.nextTest()

    def test_case_0024(self) :
        self.nextTest()

    def test_case_0025(self) :
        self.nextTest()

"""
    def test_case_0026(self) :
        self.nextTest()

    def test_case_0027(self) :
        self.nextTest()

    def test_case_0028(self) :
        self.nextTest()

    def test_case_0029(self) :
        self.nextTest()

    def test_case_0030(self) :
        self.nextTest()

    def test_case_0031(self) :
        self.nextTest()

    def test_case_0032(self) :
        self.nextTest()

    def test_case_0033(self) :
        self.nextTest()

    def test_case_0034(self) :
        self.nextTest()

    def test_case_0035(self) :
        self.nextTest()

    def test_case_0036(self) :
        self.nextTest()

    def test_case_0037(self) :
        self.nextTest()

    def test_case_0038(self) :
        self.nextTest()

    def test_case_0039(self) :
        self.nextTest()

    def test_case_0040(self) :
        self.nextTest()

    def test_case_0041(self) :
        self.nextTest()

    def test_case_0042(self) :
        self.nextTest()

    def test_case_0043(self) :
        self.nextTest()

    def test_case_0044(self) :
        self.nextTest()

    def test_case_0045(self) :
        self.nextTest()

    def test_case_0046(self) :
        self.nextTest()

    def test_case_0047(self) :
        self.nextTest()

    def test_case_0048(self) :
        self.nextTest()

    def test_case_0049(self) :
        self.nextTest()

    def test_case_0050(self) :
        self.nextTest()
"""
