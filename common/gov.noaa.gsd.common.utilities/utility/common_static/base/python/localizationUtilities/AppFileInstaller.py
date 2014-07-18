"""
Description: Allows access to individual localization files in an environment
             with no JEP access

# SVN: $Revision: 17 $  $Date: 2012-03-30 08:00:10 -0600 (Fri, 30 Mar 2012) $  $Author: matthew.foster $
#
# by Matt Foster, TDM, Central Region Hq.
#
SOFTWARE HISTORY
Date         Ticket#    Engineer          Description
------------ ---------- -----------       --------------------------
Mar 30, 2012            Matt Foster(CRH)  From SCP, originally named LocalFileInstaller
Nov 15, 2012            JRamer            Added check() and getList() methods
Aug 22, 2013            JRamer            Fix delete, allow direct access to
                                          source code loc files for unit tests.
Oct 24, 2013            JRamer            Chmod created flat files to
                                          be world writable.
Nov 1, 2013             JRamer            All hazardServices specific logic gone
"""

from dynamicserialize.dstypes.com.raytheon.uf.common.localization.stream import LocalizationStreamPutRequest
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.stream import LocalizationStreamGetRequest
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import ListUtilityCommand
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import DeleteUtilityCommand
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import UtilityRequestMessage
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import PrivilegedUtilityRequestMessage
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import ListUtilityResponse
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import DeleteUtilityResponse
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import ListResponseEntry
from dynamicserialize.dstypes.com.raytheon.uf.common.localization.msgs import UtilityResponseMessage
from dynamicserialize.dstypes.com.raytheon.uf.common.localization import LocalizationContext
from dynamicserialize.dstypes.com.raytheon.uf.common.localization import LocalizationType
from dynamicserialize.dstypes.com.raytheon.uf.common.localization import LocalizationLevel
from dynamicserialize.dstypes.com.raytheon.uf.common.auth.resp import UserNotAuthorized
from dynamicserialize.dstypes.com.raytheon.uf.common.auth.resp import SuccessfulExecution
from dynamicserialize.dstypes.com.raytheon.uf.common.plugin.nwsauth.user import User

# NOTE, THE LOGIC THAT ALLOWS STANDALONE UNIT TESTS TO RETRIEVE BASE LEVEL
# LOCALIZATION FILES OUT OF THE CODE BASE IS COMPLETELY DEPENDENT ON THIS
# MODULE BEING PHYSICALLY LOCATED SOMEWHERE UNDER THE TOP LEVEL REPOSITORY
# SUBDIRECTORY NAMED .../common/.

from ufpy import ThriftClient
try:
    from UFStatusLogger import UFStatusLogger
except :
    pass

import numpy
import sys
import traceback
import os
import time
import subprocess

myBranchAFI = None
siblingBranchAFI = None
myBranchOsgiPkgs = None
siblingOsgiPkgs = None

class AppFileInstaller():
    def __init__(self, host="ec", port="9581"):
        self.__tc = ThriftClient.ThriftClient(host+":"+port+"/services")
        self.__context = LocalizationContext()
        self.__useEdex = self.__checkIfUsingEdex()
        self.__lspr = self.createPutRequest()
        self.__lspr.setContext(self.__context)
        self.__lsgr = LocalizationStreamGetRequest()
        self.__lsgr.setContext(self.__context)
        self.__luc = self.createListRequest()
        self.__luc.setContext(self.__context)
        self.__duc = self.createDeleteRequest()
        self.__duc.setContext(self.__context)

    # Returns a boolean that specifies whether to use EDEX to retrieve
    # localization files.  This only works if this code actually resides
    # somewhere under the top level repository subdirectory named .../common/.
    def __checkIfUsingEdex(self) :
        global myBranchAFI
        global siblingBranchAFI

        # The default behavior of this class is to access only base level
        # localization files from the source code directories if run within
        # a unit test, and to get everything from EDEX otherwise.  Setting the
        # value of the LOCALIZATION_DATA_SOURCE environment variable allows
        # one to change this behavior.
        self.__localOverrides = False
        self.__readConfigured = False
        ldsEnv = os.environ.get("LOCALIZATION_DATA_SOURCE")
        if ldsEnv == "EDEX" :
            return True   # Always go to edex.
        elif ldsEnv == "OVERRIDE" :
            # Read 'base' files from source, fully interoperate with locally
            # mounted /awips2/edex/data/utility for other levels.
            self.__localOverrides = True
            self.__readConfigured = True
        elif ldsEnv == "CODE+" :
            # Read 'base' files from source, read 'configured' from locally
            # mounted /awips2/edex/data/utility, nothing for other levels.
            self.__readConfigured = True
        elif ldsEnv != "CODE" :
            # Default, go to source to get only base files if a unit test;
            # otherwise go to edex.
            stackstr = str(traceback.format_stack())
            srchstr = """unittest.main()"""
            i = stackstr.find(srchstr)
            if (i<0) :
                return True

        # Keep codeRoot cached in a global static.
        if myBranchAFI!=None :
            return False
        myBranchAFI = ""
        siblingBranchAFI = ""

        # Get absolute path to this source code using current working directory
        # and contents of __file__ variable.
        me = __file__
        if me[0]!="/" :
            here = os.getcwd()
            me = here+"/"+me

        # Break this path into its individual directory parts and locate the
        # common/ part.
        pathList = []
        pathParts = me.split("/")
        m = len(pathParts)-1
        basename = pathParts[m]
        pathParts = pathParts[:m]
        nparts = 0
        commonPart = -1
        ok = False
        for part in pathParts :
            if part == '.' :
                pass
            elif part == '..' :
                nparts = nparts - 1
                pathList = pathList[0:nparts]
            elif part == "common" :
                commonPart = nparts
                break
            elif len(part)>0 :
                nparts = nparts + 1
                pathList.append(part)

        # No common found, force an exception throw from ctor.
        if commonPart < 1 :
            self.__context = None
            return False

        # Make root path to code branch this module is in.
        myBranchAFI = "/"+"/".join(pathList)
        try :
            UFStatusLogger.getInstance().logMessage(\
              "Accessing localization files from code base.", "Info")
        except :
            pass

        # Attempt to locate the proper sibling branch, which apparently
        # is still using edexOsgi for the top of its localization file
        # heirarchy.
        cmd = 'find /'+"/".join(pathList[:-1])+' -mindepth 2 -maxdepth 2 '+ \
              '-type d ! -path "*/.*" -name edexOsgi | grep -v '+myBranchAFI
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        potentialSiblings = stdout.split("\n")
        if len(potentialSiblings)>0 :
            if potentialSiblings[-1]=="" :
                del potentialSiblings[-1]
        if len(potentialSiblings) == 0 :
            return False
        if len(potentialSiblings) == 1 :
            siblingBranchAFI = potentialSiblings[0][:-9]
            return False
        for psib in potentialSiblings :
           if psib.lower().find("baseline")>=0 :
               siblingBranchAFI = psib[:-9]
               return False
           if psib.lower().find("awips2")>=0 :
               siblingBranchAFI = psib[:-9]
               return False
        siblingBranchAFI = potentialSiblings[0][:-9]
        return False

    # Returns the path to a hard file on the local host to access 
    # directly, if applicable, for base level localization files.
    # Otherwise just returns empty string.
    def __locateCodeFile(self, endPath) :
        global myBranchOsgiPkgs
        global siblingOsgiPkgs
        global myBranchAFI
        global siblingBranchAFI
        if self.__useEdex :
            return ""

        # First see if the specified localization file can be found under the
        # primary code branch.  We create a global cache of the list of package
        # directories that carry hazardServices EDEX installable files, putting
        # the test package first on the list so unit tests prefer that.
        if myBranchOsgiPkgs == None :
            cmd = "find "+myBranchAFI+"/common/ "+myBranchAFI+"/edex/ "+ \
                  " -maxdepth 1 -mindepth 1 -type d"
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
            (stdout, stderr) = p.communicate()
            myBranchOsgiPkgs = stdout.split("\n")
            if len(myBranchOsgiPkgs)>0 :
                if myBranchOsgiPkgs[-1]=="" :
                    del myBranchOsgiPkgs[-1]
            testPath = myBranchAFI+"/tests"
            myBranchOsgiPkgs.insert(0, testPath)
        for pkg in myBranchOsgiPkgs :
            baseFilePath = pkg+"/utility/common_static/base/"+endPath
            if os.path.exists(baseFilePath) :
                return baseFilePath

        # Now see if the specified localization file can be found under the
        # sibling code branch.  We create a global cache of the list of package
        # directories that carry baseline EDEX installable files, putting
        # the test package first on the list so unit tests prefer that.
        if siblingBranchAFI=="" :
            return ""
        if siblingOsgiPkgs == None :
            cmd = "find "+siblingBranchAFI+"/edexOsgi/ "+ \
                  " -maxdepth 1 -mindepth 1 -type d -name '*common*'"
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
            (stdout, stderr) = p.communicate()
            siblingOsgiPkgs = stdout.split("\n")
            if len(siblingOsgiPkgs)>0 :
                if siblingOsgiPkgs[-1]=="" :
                    del siblingOsgiPkgs[-1]
            testPath = siblingBranchAFI+"/tests"
            siblingOsgiPkgs.insert(0, testPath)
        for pkg in siblingOsgiPkgs :
            baseFilePath = pkg+"/utility/common_static/base/"+endPath
            if os.path.exists(baseFilePath) :
                return baseFilePath

        return ""

    # Returns None if using EDEX.  Otherwise returns the path to a hard file
    # on a locally accessible disk to directly interact with.
    def __devCodePath(self, endPath, output=False) :
        if self.__useEdex :
            return None
        levelStr = self.__context.getLocalizationLevel().getText().lower()
        if levelStr == "base" :
            if output :
                return ""
            return self.__locateCodeFile(endPath)
        elif levelStr == "configured" :
            if output or not self.__readConfigured :
                return ""
        elif not self.__localOverrides :
            return ""
        filePath = '/awips2/edex/data/utility/' + \
               self.__context.getLocalizationType().getText().lower()+'/' + \
               levelStr+'/'+ \
               self.__context.getContextName()+'/'+endPath
        return filePath

    def createPutRequest(self):
        req = LocalizationStreamPutRequest()
        req.setEnd(True)

        return req

    def createDeleteRequest(self):
        req = DeleteUtilityCommand()

        return req

    def createListRequest(self):
        req = ListUtilityCommand()
        req.setRecursive(False)
        req.setFilesOnly(True)

        return req

    def setLevel(self, lev):
        '''
        @summary: Public interface for setting the localization level to use.
        @param lev: Localization level, should be "Base", "Configured", "Site",
                    "Workstation", or "User"
        '''
        level = LocalizationLevel(lev)
        self.__context.setLocalizationLevel(level)

    def setType(self, lt):
        '''
        @summary: Public interface for setting the localization type to use.
        @param type: Localization type, should be "COMMON_STATIC", "EDEX_STATIC",
                     or "CAVE_STATIC"
        '''
        locType = LocalizationType(lt)
        self.__context.setLocalizationType(locType)

    def setName(self, name):
        '''
        @summary: Public interface for setting the localization name to use.
        @param name: Localization name, for which the meaningful values to use
                     depend on which level is set.
        '''
        self.__context.setContextName(name)

    def setMyContextName(self, name):
        '''
        @summary: Public interface for setting the context name to use.
        @param name: Localization context name, the user name that applies to
                     priveleged operations.
        '''
        self.__lspr.setMyContextName(name)
        self.__lsgr.setMyContextName(name)
        self.__duc.setMyContextName(name)

    def putFile(self, fname, data):
        '''
        @summary: Public interface for writing a localization file.
                  Operation not enabled for Base and Configured level.
        @param fname: File path to file, below level/name path components.
        @param data: Text to place in newly created localization file.
        @return: boolean for whether operation was successful.
        '''
        lev = self.__context.getLocalizationLevel()
        if lev.getText().upper() == "BASE" :
            raise AppFileInstallerException("I can GET files from BASE," + \
                      "but I won't PUT them.  It just wouldn't be right.")
        nonEdexPath = self.__devCodePath(fname, True)
        if nonEdexPath!=None:
            if nonEdexPath=="" :
                return False
            nonEdexDir = "/".join(nonEdexPath.split("/")[:-1])
            try:
                os.stat(nonEdexDir)
            except:
                try :
                    os.makedirs(nonEdexDir, 0777)
                except:
                    return False
            try :
                ffff = open(nonEdexPath, "w")
                ffff.write(data)
                ffff.close()
                try :
                    os.chmod(nonEdexPath, 0666)
                except:
                    pass
                return True
            except :
                pass
            return False
        resp = None
        self.__lspr.setFileName(fname)
        size = len(fname)
        self.__lspr.setBytes(numpy.fromstring(data, dtype=numpy.int8))
        try:
            resp = self.__tc.sendRequest(self.__lspr)
        except ThriftClient.ThriftRequestException:
            raise AppFileInstallerException("putFile: Error sending request to server")
        
        if isinstance(resp, UserNotAuthorized):
            raise AppFileInstallerException("UserNotAuthorized: "+resp.getMessage())
        elif isinstance(resp, SuccessfulExecution):
            return True
        else:
            raise AppFileInstallerException("Unexpected/no response from server in putFile")

        return False

    def rmFile(self, fname):
        '''
        @summary: Public interface for deleting a localization file.
                  Operation not enabled for Base and Configured level.
        @param fname: File path to file, below level/name path components.
        @return: boolean for whether operation was successful.
        '''
        lev = self.__context.getLocalizationLevel()
        if lev.getText().upper() == "BASE" :
            raise AppFileInstallerException("I can GET files from BASE," + \
                      "but I won't DELETE them.  It just wouldn't be right.")
        nonEdexPath = self.__devCodePath(fname, True)
        if nonEdexPath!=None:
            if nonEdexPath=="" :
                return False
            try :
                os.remove(nonEdexPath)
                return True
            except :
                pass
            return False
        self.__duc.setContext(self.__context)
        self.__duc.setFilename(fname)
        resp = None
        try :
            urm = PrivilegedUtilityRequestMessage()
            urm.setCommands([self.__duc])
            resp = self.__tc.sendRequest(urm)
            if resp==None :
                return False
        except :
            traceback.print_exc()
            return False
        return True

    def getFile(self, fname):
        '''
        @summary: Public interface for reading a localization file.
        @param fname: File path to file, below level/name path components.
        @return: String representing the contents of the file, None if
                 a failure occured.
        '''
        levText = self.__context.getLocalizationLevel().getText().upper()
        nonEdexPath = self.__devCodePath(fname)
        if nonEdexPath!=None:
            if nonEdexPath=="" :
                return None
            try :
                ffff = open(nonEdexPath, "r")
                data = ffff.read()
                ffff.close()
                return data
            except :
                pass
            return None
        self.__lsgr.setFileName(fname)
        fileStr = None
        try:
            resp = self.__tc.sendRequest(self.__lsgr)
            fileStr = resp.response.getBytes().tostring()
        except ThriftClient.ThriftRequestException:
            raise AppFileInstallerException("getFile: Error sending request to server")

        return fileStr

    def getList(self, dirname):
        '''
        @summary: Public interface for listing localization files.
        @param dirname: File path to localizaton data directory, below
                        level/name path components.
        @return: List of files found, empty list otherwise.
        '''
        nonEdexPath = self.__devCodePath(dirname)
        if nonEdexPath!=None:
            if nonEdexPath=="" :
                return []
            try :
                list = os.listdir(nonEdexPath)
                return list
            except :
                pass
            return []
        self.__luc.setSubDirectory(dirname)
        nnn = len(dirname)+1
        resp = None
        retList = []
        try:
            urm = UtilityRequestMessage()
            urm.setCommands([self.__luc])
            resp = self.__tc.sendRequest(urm)
            respList = resp.getResponses()
            entries = respList[0].getEntries()
            for one in entries :
                onefil = one.getFileName()
                if onefil == dirname :
                    retList.append(onefil)
                else :
                    retList.append(onefil[nnn:])
        except ThriftClient.ThriftRequestException:
            raise AppFileInstallerException("getList: Error sending request to server")

        return retList

    def check(self, filname):
        '''
        @summary: Public interface for verifying a localization file exists.
        @param fname: File path to file, below level/name path components.
        @return: boolean for whether file exists.
        '''
        nonEdexPath = self.__devCodePath(filname)
        if nonEdexPath!=None:
            if nonEdexPath=="" :
                return False
            try :
                ffff = open(nonEdexPath, "r")
                ffff.close()
                return True
            except :
                pass
            return False
        self.__luc.setSubDirectory(filname)
        resp = None
        try:
            urm = UtilityRequestMessage()
            urm.setCommands([self.__luc])
            resp = self.__tc.sendRequest(urm)
            respList = resp.getResponses()
            entries = respList[0].getEntries()
            if len(entries)==1 and entries[0].getFileName()==filname :
                return True
        except ThriftClient.ThriftRequestException:
            raise AppFileInstallerException("getList: Error sending request to server")

        return False

class AppFileInstallerException(Exception):
    def __init__(self, value):
        self.parameter = value

    def __str__(self):
        return repr(self.parameter)

