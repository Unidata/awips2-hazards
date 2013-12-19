"""
Description: 
This file contains a static global methods meant to help set up the code import
environment for python unit tests.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Aug 16, 2013            JRamer      Pulled out of testCaseSupportRoutines.

@author James.E.Ramer@noaa.gov
@version 1.0
"""
import os
import sys
import subprocess

def constructPaths(rootPath, relPaths, default='.') :
    '''
    @summary: For one rootPath and set of relative paths, return set of
              python paths to use.  A relPath of "*" means find all useable
              directories with python under the rootPath.
    @param rootPath: Path where additional python directories might exist.
    @param relPaths: List of paths relative to the root directory to add
                     to the sys.path data structure.
    @param fromSibling: List of paths relative to the sibling root directory
                        to add to the sys.path data structure.
    @return: List of the paths to python directories.. 
    '''
    if isinstance(relPaths, list) or isinstance(relPaths, tuple) :
        relList = relPaths
    elif not isinstance(relPaths, str) and not isinstance(relPaths, unicode) :
        relList = [ default ]
    elif len(relPaths)==0 :
        relList = [ default ]
    else :
        relList = [ relPaths ]
    pyPathParts = []
    for part in relList :
        if not isinstance(part, str) and not isinstance(part, unicode) :
            continue
        if part=="." or part=="" :
            pyPathParts.append(rootPath)
            continue
        elif part[-1] != "*" :
            pyPathParts.append(rootPath+"/"+str(part))
            continue
        cmd = "find "+rootPath
        if len(part)>1 :
            cmd += "/"+part[:-1]
        cmd +=" -name '*py' -exec dirname '{}' \; | sort -u | " + \
              "grep -v base/hazardServices | grep -v '/tests/'"
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        result = stdout.rsplit('\n')
        for part in result :
            if len(part)>0 :
                pyPathParts.append(part)
    return pyPathParts

# Client provides lists of extra paths added to the sys.path data structure,
# which controls the directories one can import from.  It is important that
# any relative paths provided NOT have either a leading or trailing /.
# The root directory is located by steping upward through the directory
# structure from where this source code file is until a directory is
# encountered with a basename of 'edexOsgi', and then the root is assumed to
# be the next step up.
# fromRoot specifies a path or list of paths relative to the root directory
# to add to the sys.path data structure.  Default value of fromRoot, as well
# as a literal "*", will trigger adding all subdirectories under the root
# directory with meaningful python.
# This also pulls out of the sys.path data structure any paths that are in
# the EDEX utility directories.
# Optional argument fromHere is used to specify paths to add relative to
# where this source code is located.  Optional argument fromSibling is used
# to specify paths to add to a "sibling directory" to the root directory.
# Such a sibling directory must have also have an 'edexOsgi' subdirectory
# in it. For now, the root directory will usually have a basename of
# "hazardServices", and the sibling root directory will usually have a
# basename of "AWIPS2_baseline".
def updateSysPath(fromRoot=None, fromHere=None,
                  fromSibling=None, stepsBelowRoot=2) :
    '''
    @summary: Used to manage the set of import paths in sys.path data structure
              for unit tests.
    @param fromRoot: List of paths relative to the root directory to add
                     to the sys.path data structure.
    @param fromHere: List of paths relative to the directory where this module
                     resides to add to the sys.path data structure.
    @param fromSibling: List of paths relative to the sibling root directory
                        to add to the sys.path data structure.
    @param stepsBelowRoot: Number of steps below the main root directory to
                           assume that this module is positioned.
    @return: Full path to root directory. 
    '''

    # Get absolute path to this source code using current working directory
    # and contents of __file__ variable.
    me = __file__
    if me[0]!="/" :
        here = os.getcwd()
        me = here+"/"+me

    # Break this path into its individual directory parts so we can locate the
    # "root" directory.
    pathList = []
    pathParts = me.split("/")
    m = len(pathParts)-1
    basename = pathParts[m]
    pathParts = pathParts[:m]
    nparts = 0
    for part in pathParts :
        if part == '.' :
            pass
        elif part == '..' :
            nparts = nparts - 1
            pathList = pathList[0:nparts]
        elif len(part)>0 :
            nparts = nparts + 1
            pathList.append(part)

    # Reconstitute full path to this source code directory.
    meDir = "/"+"/".join(pathList)

    # Try to verify that the proper root directory exists approximately
    # the designated number of steps above here.
    rootPart = nparts-stepsBelowRoot
    if rootPart>0 :
        rootDir = "/"+"/".join(pathList[0:rootPart])
        byAutoRoot = not os.path.isdir(rootDir+"/edexOsgi")
        if byAutoRoot and rootPart<nparts :
            rootPart += 1
            rootDir = "/"+"/".join(pathList[0:rootPart])
            byAutoRoot = not os.path.isdir(rootDir+"/edexOsgi")
        if byAutoRoot and rootPart>1 :
            rootPart -= 2
            rootDir = "/"+"/".join(pathList[0:rootPart])
            byAutoRoot = not os.path.isdir(rootDir+"/edexOsgi")
    else :
        byAutoRoot = True

    # If we could not find our root designator, there are a couple of fallbacks
    while byAutoRoot :
        rootDir = "/awips2/edex/data/utility/common_static/base/python"
        if os.path.isdir(rootDir) :
            break
        rootDir = os.environ.get("HOME", "xxx")+"/caveData/common/base/python"
        if os.path.isdir(rootDir) :
            break
        sys.stderr.write("location of "+basename+" does not make sense\n")
        return None

    # Reconstitute full paths to the root directories.
    siblingDir = ""
    if fromSibling==None :
        pass
    elif byAutoRoot :
        if os.path.isdir("/awips2/fxa/bin/src") :
            siblingDir = "/awips2/fxa/bin/src"
    else :
        cmd = 'find /'+"/".join(pathList[:rootPart-1])+ \
              ' -mindepth 2 -maxdepth 2 '+ \
              '-type d ! -path "*/.*" -name edexOsgi | grep -v '+rootDir
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        potentialSiblings = stdout.split("\n")
        if len(potentialSiblings)>0 :
            if potentialSiblings[-1]=="" :
                del potentialSiblings[-1]
        for psib in potentialSiblings :
           if psib.lower().find("baseline")>=0 :
               siblingDir = psib[:-9]
               break
           if psib.lower().find("awips2")>=0 :
               siblingDir = psib[:-9]
               break
        if siblingDir=="" and len(potentialSiblings)>0 :
            siblingDir = potentialSiblings[0][:-9]
        if siblingDir=="" :
            sys.stderr.write("No sibling directory for path building found.\n")

    # Initialize with current contents of sys.path, and add all the
    # requested paths.
    pyPathParts = sys.path
    nPreset = len(pyPathParts)
    pyPathParts.extend(constructPaths(rootDir, fromRoot, "*"))
    if fromHere!=None :
        pyPathParts.extend(constructPaths(meDir, fromHere))
    lastVerify = len(pyPathParts)
    if siblingDir!="" :
        pyPathParts.extend(constructPaths(siblingDir, fromSibling))
        if not byAutoRoot :
            lastVerify = len(pyPathParts)
    pyPathParts.append(meDir)

    # Eliminate redundancies and paths to EDEX localization file directories.
    newPyPath = []
    for part in pyPathParts :
        nPreset = nPreset-1
        lastVerify = lastVerify-1
        if not byAutoRoot and part.find("edex/data/utility")>=0 :
            continue
        if part in newPyPath :
            continue
        if nPreset<0 and lastVerify>=0 and not os.path.isdir(part) :
            sys.stderr.write("path entry does not exist:\n")
            sys.stderr.write(part+"\n")
            continue
        newPyPath.append(part)

    sys.path = newPyPath
    return rootDir
