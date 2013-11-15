"""
Description: 
This file contains static global methods meant to help set up the code import
environment for python unit tests, and for reading in test cases.

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

# Client provides lists of extra paths added to the sys.path data structure,
# which controls the directories one can import from.
# The root directory is located by steping upward through the directory
# structure from where this source code file is until a directory is
# encountered with the base name the same as the argument rootName.
# fromRoot specifies a path or list of paths relative to the root directory
# to add to the sys.path data structure.
# This also pulls out of the sys.path data structure any paths that are in
# the EDEX utility directories.
# Optional arguments siblingRoot and fromSibling are used to specify paths
# under an additional directory at the same level as the root directory.
# For now, rootName will typically be "hazardServices", and siblingRoot,
# if used, will typically be "AWIPS2_baseline".
def updateSysPath(rootName, fromRoot, fromHere=None,
                  siblingRoot=None, fromSibling=None) :
    '''
    @summary: Used to manage the set of import paths in sys.path data structure
              for unit tests.
    @param rootName: basename of root directory for current software workset,
                     for now typically "hazardServices".
    @param fromRoot: List of paths relative to the root directory to add
                     to the sys.path data structure.
    @param fromHere: List of paths relative to the directory where this module
                     resides to add to the sys.path data structure.
    @param siblingRoot: basename of additional software root directory at
                        same level as main root directory.  For now, if used,
                        will typically be "AWIPS2_baseline".
    @param fromSibling: List of paths relative to the sibling root directory
                        to add to the sys.path data structure.
    @return: Full path to root directory. 
    '''

    # Get absolute path to this source code using current working directory
    # and contents of __file__ variable.
    here = os.environ["PWD"]
    me = __file__
    if me[0]!="/" :
        me = here+"/"+me

    # Break this path into its individual directory parts and locate the
    # "root" part.
    pathList = []
    pathParts = me.split("/")
    m = len(pathParts)-1
    basename = pathParts[m]
    pathParts = pathParts[:m]
    nparts = 0
    rootPart = -1
    ok = False
    for part in pathParts :
        if part == '.' :
            pass
        elif part == '..' :
            nparts = nparts - 1
            pathList = pathList[0:nparts]
        elif len(part)>0 :
            nparts = nparts + 1
            pathList.append(part)
        if part == rootName :
            rootPart = nparts

    if rootPart < 0 :
        sys.stderr.write("location of "+basename+" does not make sense\n")
        return None

    # Reconstitute full paths to the root directory and the source code
    # directory.
    rootDir = ""
    for part in pathList[0:rootPart] :
        rootDir += "/"+part
    meDir = rootDir
    for part in pathList[rootPart:] :
        meDir += "/"+part
    siblingDir = ""
    if siblingRoot!=None :
        for part in pathList[0:rootPart-1] :
            siblingDir += "/"+part
        siblingDir += "/"+siblingDir

    # Initialize with current contents of sys.path, and add all the
    # requested paths.
    nPreset = len(sys.path)
    pyPathParts = sys.path
    if isinstance(fromRoot, str) or isinstance(fromRoot, unicode) :
        pyPathParts.append(rootDir+"/"+fromRoot)
    elif isinstance(fromRoot, list) or isinstance(fromRoot, tuple) :
        for part in fromRoot :
            pyPathParts.append(rootDir+"/"+part)
    if isinstance(fromHere, str) or isinstance(fromHere, unicode) :
        pyPathParts.append(meDir+"/"+fromHere)
    elif isinstance(fromHere, list) or isinstance(fromHere, tuple) :
        for part in fromHere :
             pyPathParts.append(meDir+"/"+part)
    if siblingDir=="" :
        pass
    elif isinstance(fromSibling, str) or isinstance(fromSibling, unicode) :
        pyPathParts.append(siblingDir+"/"+fromSibling)
    elif isinstance(fromSibling, list) or isinstance(fromSibling, tuple) :
        for part in fromSibling :
             pyPathParts.append(siblingDir+"/"+part)
    pyPathParts.append(meDir)

    # Eliminate redundancies and paths to EDEX localization file directories.
    newPyPath = []
    for part in pyPathParts :
        nPreset = nPreset-1
        if part.find("edex/data/utility")>=0 :
            continue
        if part in newPyPath :
            continue
        if nPreset<0 and not os.path.isdir(part) :
            sys.stderr.write("path entry does not exist:\n")
            sys.stderr.write(part+"\n")
            continue
        newPyPath.append(part)

    sys.path = newPyPath
    return rootDir

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

    # If we have no list of input test cases, make that list.
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

        # Use traceback to get name of the test program.
        tbData = traceback.format_stack()
        testName = ""
        for tbEntry in tbData :
            if tbEntry.find('File "Test')>0 :
                testName = tbEntry.split('"')[1][:-3]
                break
            i = tbEntry.find('.py"')
            if i<0 :
                continue
            while i>0 and tbEntry[i]!='/' :
                i = i - 1
            if tbEntry[i:i+5]!="/Test" :
                continue
            testName = tbEntry[i+1:].split('"')[0][:-3]
            break
        if testName=="" :
            sys.stderr.write("Could not identify name of unit test program.\n")
            return None

        # Make subdirectory name out of test program name, find all files in
        # there and present as test cases.  Avoid editor journal files and
        # python byte code files.
        testName = mydir+"/"+testName[4:]+"TestCases"
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

    # Return next test case file.
    if testCaseIndex>=len(inputTestCaseFiles) :
        return None
    testCasePath = inputTestCaseFiles[testCaseIndex]
    testCaseIndex = testCaseIndex + 1
    return testCasePath

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


# This global static method has a bare minimum set up for unit tests of
# recomenders.
setUpDone = False
def commonRecommenderTestSetUp():
    '''
    @summary: A bare minimum setup for recommender unit tests.
    '''
    global setUpDone
    if setUpDone :
        return
    edexPython = \
     "edexOsgi/gov.noaa.gsd.common.utilities/utility/"+ \
     "common_static/base/python"
    recommenderPython = \
      "edexOsgi/gov.noaa.gsd.uf.common.recommenders.hydro/utility/"+ \
      "common_static/base/python/events/recommenders"
    configPython = \
      "edexOsgi/com.raytheon.uf.common.recommenders/utility/"+ \
      "common_static/base/python/events/recommenders/config"
    fromRoot = []
    fromRoot.append(edexPython+"/geoUtilities")
    fromRoot.append(edexPython+"/trackUtilities")
    fromRoot.append(edexPython+"/generalUtilities")
    fromRoot.append(edexPython+"/logUtilities")
    fromRoot.append(edexPython+"/bridge")
    fromRoot.append(recommenderPython)
    fromRoot.append(configPython)
    updateSysPath("hazardServices", fromRoot)
    setUpDone = True


