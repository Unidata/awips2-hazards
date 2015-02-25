"""
Description: Allows access to individual localization files in an environment
             with JEP access

SOFTWARE HISTORY
Date         Ticket#    Engineer          Description
------------ ---------- -----------       --------------------------
May 3, 2013             BLawrence         Created from AppFileInstaller, swapped out
                                          ThriftClient in favor of RequestRouter.
Aug 22, 2013            JRamer            Fix delete operation, add some pydoc.
Feb 24, 2015 4204       Robert.Blum       Updated User import
                                          
"""

import jep

    
from com.raytheon.uf.common.localization.stream import LocalizationStreamPutRequest
from com.raytheon.uf.common.localization.stream import LocalizationStreamGetRequest
from com.raytheon.uf.common.localization.msgs import ListUtilityCommand
from com.raytheon.uf.common.localization.msgs import DeleteUtilityCommand
from com.raytheon.uf.common.localization.msgs import UtilityRequestMessage
from com.raytheon.uf.common.localization.msgs import PrivilegedUtilityRequestMessage
from com.raytheon.uf.common.localization.msgs import ListUtilityResponse
from com.raytheon.uf.common.localization.msgs import DeleteUtilityResponse
from com.raytheon.uf.common.localization.msgs import ListResponseEntry
from com.raytheon.uf.common.localization.msgs import UtilityResponseMessage
from com.raytheon.uf.common.localization.msgs import AbstractUtilityCommand
from com.raytheon.uf.common.localization.msgs import AbstractPrivilegedUtilityCommand
from com.raytheon.uf.common.localization import LocalizationContext
from com.raytheon.uf.common.localization import LocalizationContext_LocalizationType as LocalizationType
from com.raytheon.uf.common.localization import LocalizationContext_LocalizationLevel as LocalizationLevel
from com.raytheon.uf.common.auth.resp import UserNotAuthorized
from com.raytheon.uf.common.auth.resp import SuccessfulExecution
from com.raytheon.uf.common.auth.user import User
from com.raytheon.uf.common.serialization.comm import RequestRouter
from com.raytheon.uf.common.auth.req import AbstractPrivilegedRequest

import numpy
import sys, os, traceback
import getpass

class LocalFileInstaller():
    def __init__(self, host="ec"):
        self.__rr = RequestRouter
        self.__context = LocalizationContext()
        self.__lspr = self.createPutRequest()
        self.__lspr.setContext(self.__context)
        self.__lsgr = LocalizationStreamGetRequest()
        self.__lsgr.setContext(self.__context)
        self.__luc = self.createListRequest()
        self.__luc.setContext(self.__context)
        self.__duc = self.createDeleteRequest()
        self.__duc.setContext(self.__context)

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
        level = LocalizationLevel.valueOf(lev)
        self.__context.setLocalizationLevel(level)

    def setType(self, lt):
        '''
        @summary: Public interface for setting the localization type to use.
        @param type: Localization type, should be "COMMON_STATIC", "EDEX_STATIC",
                     or "CAVE_STATIC"
        '''
        locType = LocalizationType.valueOf(lt)
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
        @return: Throws LocalFileInstallerException on failure.
        '''
        resp = None
        lev = self.__context.getLocalizationLevel()
        if lev.toString().upper() == "BASE":
            raise LocalFileInstallerException("I can GET files from BASE, but I won't PUT them.  It just wouldn't be right.")
        self.__lspr.setFileName(fname)
        self.__lspr.setUser(User(self.__duc.getMyContextName()))
        size = len(fname)
        self.__lspr.setBytes(numpy.fromstring(data, dtype=numpy.int8))
        
        try:
            resp = self.__rr.route(self.__lspr)
        except ThriftClient.ThriftRequestException:
            raise LocalFileInstallerException("putFile: Error sending request to server")

    def rmFile(self, fname):
        '''
        @summary: Public interface for deleting a localization file.
                  Operation not enabled for Base and Configured level.
        @param fname: File path to file, below level/name path components.
        @return: Throws LocalFileInstallerException on failure.
        '''
        lev = self.__context.getLocalizationLevel()
        if lev.toString().upper() == "BASE":
            raise LocalFileInstallerException("I can GET files from BASE, but I won't DELETE them.  It just wouldn't be right.")
        self.__duc.setFilename(fname)
        resp = None
        try :
            urm = PrivilegedUtilityRequestMessage()
            urmArray = jep.jarray(1, AbstractPrivilegedUtilityCommand)
            urmArray[0] = self.__duc
            urm.setCommands(urmArray)
            resp = self.__rr.route(urm)
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
        @return: String representing the contents of the file, throws
                 LocalFileInstallerException on failure.
        '''
        user = getpass.getuser()
        self.__lsgr = AbstractPrivilegedRequest.createRequest(LocalizationStreamGetRequest().getClass(), User(user))
        self.__lsgr.setFileName(fname)
        self.__lsgr.setContext(self.__context)
        resp = None
        try:
            resp = self.__rr.route(self.__lsgr)
            putReq = resp.getResponse()
            fileStr = str(putReq.getBytes())
        except:
            raise LocalFileInstallerException("getFile: Error sending request to server")
        return fileStr

    def getList(self, dirname):
        '''
        @summary: Public interface for listing localization files.
        @param dirname: File path to localizaton data directory, below
                        level/name path components.
        @return: List of files found, throws LocalFileInstallerException
                 on failure.
        '''
        self.__luc.setSubDirectory(dirname)
        nnn = len(dirname)+1
        resp = None
        try: 
            urm = UtilityRequestMessage()
            urmArray = jep.jarray(1, AbstractUtilityCommand)
            urmArray[0] = self.__luc
            urm.setCommands(urmArray)
            resp = self.__rr.route(urm)            
            respList = resp.getResponses()
            entries = respList[0].getEntries()
            retList = []
            for one in entries :
                onefil = one.getFileName()
                if onefil == dirname :
                    retList.append(onefil)
                else :
                    retList.append(onefil[nnn:])
        except ThriftClient.ThriftRequestException:
            raise LocalFileInstallerException("getList: Error sending request to server")
        return retList

    def check(self, filname):
        '''
        @summary: Public interface for verifying a localization file exists.
        @param fname: File path to file, below level/name path components.
        @return: boolean for whether file exists, may throw
                 LocalFileInstallerException for certain failures.
        '''
        self.__luc.setSubDirectory(filname)
        resp = None
        try:
            urm = UtilityRequestMessage()
            urmArray = jep.jarray(1, AbstractUtilityCommand)
            urmArray[0] = self.__luc
            urm.setCommands(urmArray)
            resp = self.__rr.route(urm)
            respList = resp.getResponses()
            entries = respList[0].getEntries()
            if len(entries)==1 and entries[0].getFileName()==filname :
                return True
        except ThriftClient.ThriftRequestException:
            raise LocalFileInstallerException("getList: Error sending request to server")

        return False

class LocalFileInstallerException(Exception):
    def __init__(self, value):
        self.parameter = value

    def __str__(self):
        return repr(self.parameter)

