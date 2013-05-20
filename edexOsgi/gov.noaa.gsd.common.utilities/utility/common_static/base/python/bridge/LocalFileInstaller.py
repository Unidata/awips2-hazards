#
# LocalFileInstaller
#
# SVN: $Revision: 17 $  $Date: 2012-03-30 08:00:10 -0600 (Fri, 30 Mar 2012) $  $Author: matthew.foster $
#
# by Matt Foster, TDM, Central Region Hq.
#
# JRamer, GSD, added check() and getList() methods, Nov 2012.
#

import jep #@UnresolvedImport
from com.raytheon.uf.common.localization.stream import LocalizationStreamPutRequest #@UnresolvedImport
from com.raytheon.uf.common.localization.stream import LocalizationStreamGetRequest #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import ListUtilityCommand #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import DeleteUtilityCommand #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import UtilityRequestMessage #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import PrivilegedUtilityRequestMessage #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import ListUtilityResponse #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import DeleteUtilityResponse #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import ListResponseEntry #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import UtilityResponseMessage #@UnresolvedImport
from com.raytheon.uf.common.localization.msgs import AbstractUtilityCommand #@UnresolvedImport
from com.raytheon.uf.common.localization import LocalizationContext #@UnresolvedImport
from com.raytheon.uf.common.localization import LocalizationContext_LocalizationType as LocalizationType #@UnresolvedImport
from com.raytheon.uf.common.localization import LocalizationContext_LocalizationLevel as LocalizationLevel #@UnresolvedImport
from com.raytheon.uf.common.auth.resp import UserNotAuthorized #@UnresolvedImport
from com.raytheon.uf.common.auth.resp import SuccessfulExecution #@UnresolvedImport
from com.raytheon.uf.common.plugin.nwsauth.user import User #@UnresolvedImport
from com.raytheon.uf.common.serialization.comm import RequestRouter #@UnresolvedImport
from com.raytheon.uf.common.auth.req import AbstractPrivilegedRequest #@UnresolvedImport

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
        level = LocalizationLevel.valueOf(lev)
        self.__context.setLocalizationLevel(level)

    def setType(self, lt):
        locType = LocalizationType.valueOf(lt)
        self.__context.setLocalizationType(locType)

    def setName(self, name):
        self.__context.setContextName(name)

    def setMyContextName(self, name):
        self.__lspr.setMyContextName(name)
        self.__lsgr.setMyContextName(name)
        self.__duc.setMyContextName(name)

    def putFile(self, fname, data):
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
        lev = self.__context.getLocalizationLevel()
        if lev.getText().upper() == "BASE":
            raise LocalFileInstallerException("I can GET files from BASE, but I won't DELETE them.  It just wouldn't be right.")
        self.__duc.setFilename(fname)
        resp = None
        try :
            if True :
                urm = PrivilegedUtilityRequestMessage()
                urm.setUser(User(self.__duc.getMyContextName()))
                urm.setCommands([self.__duc])
            else :
                urm = UtilityRequestMessage()
                urm.setCommands([self.__duc])
            resp = self.__rr.route(urm)
            if resp==None :
                return False
        except :
            return False
        return True

    def getFile(self, fname):
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

