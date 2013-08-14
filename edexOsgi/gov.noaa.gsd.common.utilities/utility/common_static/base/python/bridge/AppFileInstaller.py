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

from ufpy import ThriftClient
from HazardServicesLogger import *

import numpy
import sys
import traceback
import os
import time
import subprocess

codeRootAFI = None
hazardServicesOsgiPkgs = None
baselineOsgiPkgs = None

class AppFileInstaller():
    def __init__(self, host="ec", port="9581"):
        self.__tc = ThriftClient.ThriftClient(host+":"+port+"/services")
        self.__context = LocalizationContext()
        self.__lspr = self.createPutRequest()
        self.__lspr.setContext(self.__context)
        self.__lsgr = LocalizationStreamGetRequest()
        self.__lsgr.setContext(self.__context)
        self.__luc = self.createListRequest()
        self.__luc.setContext(self.__context)
        self.__duc = self.createDeleteRequest()
        self.__duc.setContext(self.__context)
        self.__devCodeRoot = self.devCodeRoot()

    # Returns the path to a hard source code directory on the local host
    # under which base localization files can be found.  If it is determined
    # that access should be through EDEX, will return empty string.
    def devCodeRoot(self) :
        global codeRootAFI

        # The default behavior of this class is to access only base level
        # localization files from the source code directories if run within
        # a unit test, and to get every this from EDEX otherwise.  Setting the
        # value of the LOCALIZATION_DATA_SOURCE environment variable allows
        # one to change this behavior.
        ldsEnv = os.environ.get("LOCALIZATION_DATA_SOURCE")
        if ldsEnv == "EDEX" :
            return ""   # Always go to edex.
        elif ldsEnv == "CODE" or ldsEnv == "OVERRIDE":
            pass        # Always go to source files.
        else :
            # Default behavior, go to source files only if a unit test.
            stackstr = str(traceback.format_stack())
            srchstr = """unittest.main()"""
            i = stackstr.find(srchstr)
            if (i<0) :
                return ""

        # Allow user to specify that override files can be picked up directly
        # from a locally mounted /awips2/edex/data/utility/.
        self.__localOverrides = ldsEnv == "OVERRIDE"

        # Keep codeRoot cached in a global static.
        if codeRootAFI!=None :
            return codeRootAFI
        codeRootAFI = ""

        # Get absolute path to this source code using current working directory
        # and contents of __file__ variable.
        here = os.environ["PWD"]
        me = __file__
        if me[0]!="/" :
            me = here+"/"+me

        # Break this path into its individual directory parts and locate the
        # "root" part.
        rootName = "hazardServices"
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
                break
        if rootPart < 1 :
            return codeRootAFI
        for part in pathList[0:rootPart-1] :
            codeRootAFI += "/"+part
        HazardServicesLogger.getInstance().logMessage(\
          "Accessing localization files from code base.", "Info")
        return codeRootAFI

    # Returns the path to a hard file on the local host to access 
    # directly, if applicable, for base level localization files.
    # Otherwise just returns empty string.
    def locateCodeFile(self, endPath) :
        global hazardServicesOsgiPkgs
        global baselineOsgiPkgs
        if self.__devCodeRoot=="" :
            return ""

        # First see if the specified localization file can be found under
        # hazardService code.  We create a global cache of the list of package
        # directories that carry hazardServices EDEX installable files, putting
        # the test package first on the list so unit tests prefer that.
        if hazardServicesOsgiPkgs == None :
            cmd = "find "+self.__devCodeRoot+"/hazardServices/edexOsgi/ "+ \
                  " -maxdepth 1 -mindepth 1 -type d"
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
            (stdout, stderr) = p.communicate()
            hazardServicesOsgiPkgs = stdout.split("\n")
            if len(hazardServicesOsgiPkgs)>0 :
                if hazardServicesOsgiPkgs[-1]=="" :
                    del hazardServicesOsgiPkgs[-1]
            testPath = self.__devCodeRoot+"/hazardServices/tests"
            hazardServicesOsgiPkgs.insert(0, testPath)
        for pkg in hazardServicesOsgiPkgs :
            baseFilePath = pkg+"/utility/common_static/base/"+endPath
            if os.path.exists(baseFilePath) :
                return baseFilePath

        # Now see if the specified localization file can be found under
        # AWIPS2_baseline code.  We create a global cache of the list of package
        # directories that carry AWIPS2_baseline EDEX installable files, putting
        # the test package first on the list so unit tests prefer that.
        if baselineOsgiPkgs == None :
            cmd = "find "+self.__devCodeRoot+"/AWIPS2_baseline/edexOsgi/ "+ \
                  " -maxdepth 1 -mindepth 1 -type d -name '*common*'"
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
            (stdout, stderr) = p.communicate()
            baselineOsgiPkgs = stdout.split("\n")
            if len(baselineOsgiPkgs)>0 :
                if baselineOsgiPkgs[-1]=="" :
                    del baselineOsgiPkgs[-1]
            testPath = self.__devCodeRoot+"/AWIPS2_baseline/tests"
            baselineOsgiPkgs.insert(0, testPath)
        for pkg in baselineOsgiPkgs :
            baseFilePath = pkg+"/utility/common_static/base/"+endPath
            if os.path.exists(baseFilePath) :
                return baseFilePath

        return ""

    # Returns None if using EDEX.  Otherwise returns the path to a hard file
    # on a locally accessible disk to directly interact with.
    def devCodePath(self, endPath) :
        if self.__devCodeRoot=="" :
            return None
        levelStr = self.__context.getLocalizationLevel().getText().lower()
        if levelStr == "base" :
            filePath = self.locateCodeFile(endPath)
        elif self.__localOverrides :
            filePath = '/awips2/edex/data/utility/' + \
               self.__context.getLocalizationType().getText().lower()+'/' + \
               levelStr+'/'+ \
               self.__context.getContextName()+'/'+endPath
        else :
            filePath = ""
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
        nonEdexPath = self.devCodePath(fname)
        if nonEdexPath!=None:
            if nonEdexPath=="" :
                return False
            try :
                ffff = open(nonEdexPath, "w")
                ffff.write(data)
                ffff.close()
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
        nonEdexPath = self.devCodePath(fname)
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
            print "PrivilegedUtilityRequestMessage constructed"
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
        nonEdexPath = self.devCodePath(fname)
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
        nonEdexPath = self.devCodePath(dirname)
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
        nonEdexPath = self.devCodePath(filname)
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

