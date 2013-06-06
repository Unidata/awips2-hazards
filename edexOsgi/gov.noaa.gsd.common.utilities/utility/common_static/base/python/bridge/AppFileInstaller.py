# 
# AppFileInstaller
#
# SVN: $Revision: 17 $  $Date: 2012-03-30 08:00:10 -0600 (Fri, 30 Mar 2012) $  $Author: matthew.foster $
#
# by Matt Foster, TDM, Central Region Hq.
#
# JRamer, GSD, added check() and getList() methods, Nov 2012.
#


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

import numpy
import sys

class AppFileInstaller():
    def __init__(self, host="ec"):
        self.__tc = ThriftClient.ThriftClient(host+":9581/services")
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
        level = LocalizationLevel(lev)
        self.__context.setLocalizationLevel(level)

    def setType(self, lt):
        locType = LocalizationType(lt)
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
        if lev.getText().upper() == "BASE":
            raise AppFileInstallerException("I can GET files from BASE, but I won't PUT them.  It just wouldn't be right.")
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
            return resp.getResponse()
        else:
            raise AppFileInstallerException("Unexpected/no response from server in putFile")


    def rmFile(self, fname):
        # print "In rmFile('"+fname+"')"
        lev = self.__context.getLocalizationLevel()
        if lev.getText().upper() == "BASE":
            raise AppFileInstallerException("I can GET files from BASE, but I won't DELETE them.  It just wouldn't be right.")
        self.__duc.setFilename(fname)
        # print "file name set in DeleteUtilityCommand"
        resp = None
        try :
            if True :
                urm = PrivilegedUtilityRequestMessage()
                # print "PrivilegedUtilityRequestMessage constructed"
                urm.setUser(User(self.__duc.getMyContextName()))
                # print "user set"
                urm.setCommands([self.__duc])
            else :
                urm = UtilityRequestMessage()
                # print "UtilityRequestMessage constructed"
                urm.setCommands([self.__duc])
            # print "DeleteUtilityCommand given to the RequestMessage"
            resp = self.__tc.sendRequest(urm)
            # print "Response returned"
            if resp==None :
                return False
        except :
            # print "Threw exception"
            return False
        print resp.getErrorText()
        return True

    def getFile(self, fname):
        self.__lsgr.setFileName(fname)
        resp = None
        try:
            resp = self.__tc.sendRequest(self.__lsgr)
            fileStr = resp.response.getBytes().tostring()
        except ThriftClient.ThriftRequestException:
            raise AppFileInstallerException("getFile: Error sending request to server")

        return fileStr

    def getList(self, dirname):
        self.__luc.setSubDirectory(dirname)
        nnn = len(dirname)+1
        resp = None
        try:
            urm = UtilityRequestMessage()
            urm.setCommands([self.__luc])
            resp = self.__tc.sendRequest(urm)
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
            raise AppFileInstallerException("getList: Error sending request to server")

        return retList

    def check(self, filname):
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

