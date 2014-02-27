"""
Description: Allows access to localization files in a way that automatically
             combines data from different localization levels in an intelligent
             manner. 

SOFTWARE HISTORY
Date         Ticket#    Engineer          Description
------------ ---------- -----------       --------------------------
        2012            JRamer            Original version.
Oct 30, 2013            JRamer            Add Desk level, consolidate argument 
                                          checking logic for various access methods.
Feb 14, 2013   2161     Chris.Golden      Fixed bug causing runtime error due to
                                          incorrect import usage.
"""
import xml.etree.ElementTree as ET
import sys
import traceback
import subprocess
import os
import stat
import glob
import json
import socket
import re
import getpass
from jsonCombine import jsonCombine
from xml2Json import xml2Json
from HazardServicesImporter import HazardServicesImporter
from UFStatusLogger import UFStatusLogger

# The following imports are JEP dependent.
try:
    from LocalFileInstaller import *
except :
    from AppFileInstaller import *

try:
    from UErunner import UErunner
except:
    pass

try:
    import PathManager
except:
    pass

# The purpose of this class is to provide a very generalized interface to
# localization data.

# Note about the 'siteUser' argument.
#
# Many of these methods take an argument named 'siteUser'.  This is used to
# provide an alternate non-default value for either the user, the localization
# site id, or the Cave workstation host.  If the siteUser is an empty string,
# meaningful defaults will always be provided; the user defaults to the current
# unix account name, the localization site will default to the current
# localization being used by whatever EDEX one is interacting with, and the
# Cave workstation host will default to the current local hostname.  If a
# non-empty value for siteUser is provided, the software can figure out
# whether it is a site id, a user, or a workstation host, and apply it where
# needed and ignore it when it is not applicable.  The downside of this
# approach is that for operations that apply to multiple localization levels
# (e.g. Base/Site/User) one cannot adapt all of the levels simultaneously
# using this argument.  If this approach turns out to be problematic we may
# rethink it.

# Note about the 'levelArg' argument.
#
# Many of these methods take an argument named 'levelArg'.  This is used
# to specify what is called the Localization Level in A-II localization
# terminology.  Unless otherwise noted, the allowable values are "Base",
# "Configured", "Site", "Workstation", or "User", and if left blank the
# operation will be performed on all Localization Levels for which there
# are files at the specified path.  This argument is interpretted case
# insensitive.

# Note about the 'typeArg' argument.
#
# Many of these methods take an argument named 'typeArg'.  This is used to
# specify what is called the Localization Type in A-II localization terminology.
# The allowed values are "COMMON_STATIC", "EDEX_STATIC", or "CAVE_STATIC",
# and unless otherwise noted will default to "COMMON_STATIC" if blank.
# Furthermore, this argument is interpretted case insensitive, and one can
# leave the "_STATIC" off the end.
#
# Note about exception handling.
#
# For most of the access where each localization level is stepped through,
# exceptions are purposely swallowed and no tracebacks given. This is because
# it is expected that access to at least one or more of the six currently
# supported localization levels will fail.  However, if the getLocFile()
# method is called for one specific localization level, a traceback will be
# be given upon a failure.
# 

caveEdexHost = None
defEdexPort = os.environ.get("DEFAULT_PORT", "9581")
caveEdexPort = defEdexPort
edexLocMap = { "" : "" }
edexDeskMap = { "" : "" }
hostnameF = None

class LocalizationInterface():

    fullLocLevelList = \
     [ "User", "Workstation", "Desk", "Site", "Configured", "Base" ]
    userIdx = 0
    wsIdx = 1
    deskIdx = 2
    siteIdx = 3
    conIdx = 4
    baseIdx = 5

    # If the id of the localization host is an empty string, defaults to the
    # EDEX host currently being used by Cave.
    def __init__(self, edexHost="") :
        global caveEdexHost
        global caveEdexPort
        global edexLocMap
        self.__curUser = getpass.getuser()
        self.__defLoc = None
        self.__defDesk = None
        self.__logger = UFStatusLogger.getInstance()
        self.__repat = None
        self.__resrch = None
        self.__reclass = None
        self.__selectedSite = ""
        if edexHost!="" :
            self.__locServer = edexHost
            try :
                self.__lfi = LocalFileInstaller(edexHost)
            except :
                self.__lfi = AppFileInstaller(edexHost, defEdexPort)
            return
        if caveEdexHost!=None :
            self.__locServer = caveEdexHost
            try :
                self.__lfi = LocalFileInstaller(caveEdexHost)
            except :
                self.__lfi = AppFileInstaller(caveEdexHost, caveEdexPort)
            return

        # Read file localization.prefs out of ~/caveData to determine the
        # edex server cave is using.  This logic should now be completely
        # platform independent.
        caveEdexHost = ""
        caveEdexLoc = ""
        prefspath = os.path.join( "~", "caveData", ".metadata", \
                      ".plugins", "org.eclipse.core.runtime", ".settings", \
                      "localization.prefs" )
        prefspath = os.path.expanduser(prefspath)
        try :
            ffff = open(prefspath)
            prefsData =  ffff.read().split("\n")
            ffff.close()
            for prefsLine in prefsData :
                if len(prefsLine)<10 :
                    continue
                if prefsLine[:9]=="siteName=" :
                    caveEdexLoc = prefsLine[9:].split()[0]
                    if caveEdexHost!="" :
                        break
                    continue
                if len(prefsLine)<20 :
                    continue
                if prefsLine[:18]!="httpServerAddress=" :
                    continue
                i = prefsLine.find("//")
                if i<0 :
                    break
                j = i+3
                while prefsLine[j]!="\\" and prefsLine[j]!=":" :
                    j = j+1
                caveEdexHost = prefsLine[i+2:j]
                j = j + 1
                if not prefsLine[j].isdigit() :
                    j = j + 1
                if not prefsLine[j].isdigit() :
                    break
                i = j
                while prefsLine[j].isdigit() :
                    j = j+1
                caveEdexPort = prefsLine[i:j]
                if caveEdexLoc!="" :
                    break
        except :
            pass

        if caveEdexHost == "" :
            caveEdexHost = os.environ.get("DEFAULT_HOST","")

        if caveEdexHost == "" :
            msg = "Could not determine host of current EDEX server."
            self.__logger.logMessage(msg, "Error")

        self.__locServer = caveEdexHost
        if caveEdexLoc!="" :
            edexLocMap[caveEdexHost] = caveEdexLoc
        try :
            self.__lfi = LocalFileInstaller(caveEdexHost)
        except :
            self.__lfi = AppFileInstaller(caveEdexHost, caveEdexPort)

    # This method determines the current host id.
    def getThisHost(self) :
        global hostnameF
        if hostnameF!=None :
            return hostnameF
        try:
            # If we are on the network
            hostnameF = socket.gethostbyaddr(socket.gethostname())[0]
        except:
            # If we are off the network
            hostnameF = "localhost"

        return hostnameF

    # This would normally only be called to support service backup operations.
    # If blank, then reverts to default localization for EDEX.
    def setDefLoc(self, newDefLoc="") :
        if newDefLoc == "" :
            self.__defLoc = self.curEdexLoc()
        else :
            self.__defLoc = newDefLoc
        self.__selectedSite = self.__defLoc
        return self.__defLoc

    # Return the localization id associated with the currently connected EDEX.
    # This method contains a robust, albeit heavy handed, method for determining
    # the default localization id for the EDEX one is performing localization
    # file interactions with.
    def curEdexLoc(self) :
        global edexLocMap
        lookupLoc = edexLocMap.get(self.__locServer)
        if lookupLoc!=None :
            return lookupLoc

        if self.__locServer=="localhost" :
            # If EDEX is on localhost, then this by definition is Unix,
            # and so we can ignore platform independence considerations.
            try :
                ffff = open('/awips2/edex/bin/setup.env', 'r')
                try :
                    siteData = ""
                    line1 = ffff.readline()
                    line2 = ffff.readline()
                    line3 = ffff.readline()
                    if line1.find("AW_SITE_IDENTIFIER=")>0 :
                        siteData = line1
                    elif line2.find("AW_SITE_IDENTIFIER=")>0 :
                        siteData = line2
                    elif line3.find("AW_SITE_IDENTIFIER=")>0 :
                        siteData = line3
                    if len(siteData)>0 :
                        siteData = siteData[siteData.find("=")+1:].strip()
                        if len(siteData)==3 and siteData.isalpha() and \
                           siteData.isupper() :
                            edexLocMap[self.__locServer] = siteData
                            return siteData
                except :
                    pass
                finally :
                    ffff.close()
            except :
                pass

        # Use the uEngine to ask EDEX what its default site is.
        cacheenv = os.environ.get("DEFAULT_HOST", "-")
        os.environ["DEFAULT_HOST"] = self.__locServer
        contents = None
        try :
            myscript = """
from com.raytheon.uf.common.message.response import ResponseMessageGeneric
from com.raytheon.uf.edex.core.props import PropertiesFactory
site = PropertiesFactory.getInstance().getEnvProperties().getEnvValue('SITENAME')
return ResponseMessageGeneric(site)
"""
            ue = UErunner()
            contents = ue.returnContents(script=myscript)
        except :
            contents = None
        if cacheenv != "-" :
            os.environ["DEFAULT_HOST"] = cacheenv
        if contents != None :
            edexLocMap[self.__locServer] = str(contents)
            return str(contents)

        msg = "Could not determine localization id for EDEX server " + \
              self.__locServer
        self.__logger.logMessage(msg, "Error")
        edexLocMap[self.__locServer] = ""
        return ""

    # Get the current working localization id.
    def getDefLoc(self) :
        if self.__defLoc==None :
            self.__defLoc = self.curEdexLoc()
        self.__selectedSite = self.__defLoc
        return self.__defLoc

    # Get the current working user id.
    def getDefUser(self) :
        return self.__curUser

    # Get the current working EDEX host.
    def getEdexHost(self) :
        return self.__locServer

    # Get the current working EDEX port.
    def getEdexPort(self) :
        global caveEdexPort
        return caveEdexPort

    # Get the current default value for Desk.
    def getDefDesk(self) :
        global edexDeskMap
        if self.__defDesk!=None :
            return self.__defDesk
        self.__defDesk = edexDeskMap.get(self.__locServer)
        if self.__defDesk!=None :
            return self.__defDesk
        # First try JEP enabled way, then non-JEP enabled way
        try :
            pathMgr = PathManager.PathManager()
            try :
                self.__defDesk = \
                   pathMgr._getContext("CAVE_STATIC","DESK").getContextName()
            except :
                self.__defDesk = ""
        except :
            self.__defDesk = os.environ.get("DEFAULT_DESK", "")
        if self.__defDesk == None :
            self.__defDesk = ""
        edexDeskMap[self.__locServer] = self.__defDesk
        return self.__defDesk

    def processLevelArg(self, levelArg, restricted=False) :
        '''
        @summary: Interprets the level argument and return a list of levels
                  or a single level to process, as appropriate.
        @param levelArg: String that determines the level(s) to process.
        @param restricted: If true, an operation that can only occur for one
                           level.
        @return: None if levelArg was inappropriate, a list of levels if
                 restricted is False, a single level if restricted is True.
        '''
        locLevel = levelArg.upper()
        if locLevel=="USER" :
            if restricted :
                return "User"
            return [ "User" ]
        elif locLevel=="WORKSTATION" :
            if restricted :
                return "Workstation"
            return [ "Workstation" ]
        elif locLevel=="SITE" :
            if restricted :
                return "Site"
            return [ "Site" ]
        elif locLevel=="DESK" :
            if restricted :
                return "Desk"
            return [ "Desk" ]
        elif restricted :
            return None
        elif locLevel=="CONFIGURED" :
            return [ "Configured" ]
        elif locLevel=="BASE" :
            return [ "Base" ]
        return LocalizationInterface.fullLocLevelList

    def processTypeArg(self, typeArg, restricted=False) :
        '''
        @summary: Interprets the type argument and returns a case controlled
                  version of it.
        @param restricted: If true, will not default if no exact match to the 
                           leading characters.
        @return: None if typeArg was inappropriate, otherwise a case controlled
                 version of typeArg.
        '''
        locType = typeArg.upper()
        if typeArg[:6]=="COMMON" :
            return "COMMON_STATIC"
        elif typeArg[:4]=="EDEX" :
            return "EDEX_STATIC"
        elif typeArg[:4]=="CAVE" :
            return "CAVE_STATIC"
        elif not restricted :
            return "COMMON_STATIC"
        return None

    def getDefaultNameList(self) :
        '''
        @summary: Returns the default name to use for each localization level.
        '''
        sss = self.getDefLoc()
        return [self.__curUser, self.getThisHost(), self.getDefDesk(), \
                sss, sss, "" ]

    def processSiteUserArg(self, siteUserArg, locLevels) :
        '''
        @summary: Interprets the siteUser argument and returns a list of
                  qualifier names for each level or a single qualifier name
                  for a single level, as appropriate.
        @param siteUserArg: String that supplies an exception to the default
                            set of qualifier names.
        @return: None if siteUserArg was inappropriate, otherwise either a
                 list of qualifier names for each level or a single qualifier
                 name for a single level, as appropriate.
        '''
        singleLevel = True
        locLevel = ""
        if not isinstance(locLevels, list) :
            locLevel = locLevels
        elif len(locLevels) == 1 :
            locLevel = locLevels[0]
        else :
            singleLevel = False

        # Simplest cases of no exception or a single level.
        if locLevel == "Base" :
            return ""
        if siteUserArg=="" or siteUserArg==None :
            if not singleLevel :
                return self.getDefaultNameList()
            if locLevel == "User" :
                return self.__curUser
            elif locLevel == "Workstation":
                return self.getThisHost()
            elif locLevel == "Desk":
                return self.getDefDesk()
            elif locLevel == "Site" or locLevel == "Configured" :
                return self.getDefLoc()
            return None
        elif singleLevel :
            if locLevel == "Site" or locLevel == "Configured" :
                # If a client supplied value of siteUser changes the site to
                # use, we want to record this.
                self.__selectedSite = siteUserArg
            return siteUserArg

        # Defined exception and multiple levels, we have to apply the
        # exception to the proper level.
        defNameList = self.getDefaultNameList()
        if len(siteUserArg)==3 and siteUserArg.isalpha() and \
            siteUserArg.isupper() :
            # If a client supplied value of siteUser changes the site to use,
            # we want to record this.
            self.__selectedSite = siteUserArg
            defNameList[self.siteIdx] = siteUserArg
            defNameList[self.conIdx] = siteUserArg
        elif len(siteUserArg)>2 and len(siteUserArg)<=8 and \
             siteUserArg.isalpha() and siteUserArg.islower() :
            defNameList[site.userIdx] = siteUserArg
        elif siteUser.find(".")>0 :
            defNameList[self.wsIdx] = siteUserArg
        else :
            defNameList[self.deskIdx] = siteUserArg
        return defNameList

    def processPathArg(self, pathArg) :
        '''
        @summary: Interprets arguments that are paths or parts of paths to
                  localization files.
        @return: Path arguments with any need interpretation of meta characters.
        '''
        locPath0 = pathArg
        i = locPath0.find("###")
        while i>=0 :
            locPath0 = locPath0[:i]+self.__selectedSite+locPath0[i+3:]
            i = locPath0.find("###", i+3)
        return locPath0
        
    # This routine reads in the xml parsing information through the 
    # localization and supplies it to the xml2Json class.  The
    # xml2Json class holds this information globally, so once supplied
    # this does not need to be done again. 
    def initializeForXml(self, locType) :
        '''
        @summary: This routine reads in the xml parsing information through the 
                  localization and supplies it to the xml2Json class.
        @param locType: The data this reads is for the common type, so this is
                        the type to restore to our FileInstaller once we have
                        completed the read operation.
        '''
        if xml2Json.haveParsingInfo() :
            return
        locLevels = LocalizationInterface.fullLocLevelList
        locNames = self.getDefaultNameList()
        pathToXml = "python/localizationUtilities/xml2Json.xml"
        xmlRoots = []
        self.__lfi.setType("COMMON_STATIC")
        lll = len(locLevels)-1
        while lll>=0 :
            locLevel = locLevels[lll]
            locName = locNames[lll]
            lll -= 1

            if locLevel!="Base" and locName=="" :
                continue

            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)

            try :
                result = self.__lfi.getFile(pathToXml)
            except :
                continue
            if not isinstance(result, str) :
                continue
            try :
                xmlRoot = ET.fromstring(result)
                xmlRoots.append(xmlRoot)
            except :
                msg = locLevel+" "+locName+" of xml2Json.xml not xml."
                self.__logger.logMessage(msg, "Error")
        if len(xmlRoots)==0 :
            msg = "no useable data in "+pathToXml
            self.__logger.logMessage(msg, "Error")
        else :
            xml2Json.supplyParsingInfo(xmlRoots)
        self.__lfi.setType(locType)

    # For now returns 1=JSON, 2=XML, 4=Python data, 8=Python class,
    # 16=Misc python, 32=other
    # This is not meant to be called by outside clients.
    def checkDataType(self, dataString, baseRoot=None) :
        '''
        @summary: This routine classifies a data structure held in a string,
                  and is not meant to be called by outside clients.
        @param dataString: Text of data structure.
        @return: 1=JSON, 2=XML, 4=Python data, 8=Python class,
                 16=Misc python, 32=other
        '''
        self.__repat = None
        self.__resrch = None
        self.__reclass = None
        while baseRoot :
            patstr = '^'+baseRoot+r' *='
            self.__repat = re.compile(patstr, re.MULTILINE)
            self.__resrch = self.__repat.search(dataString)
            if self.__resrch :
                return 4
            patstr = '^ *class *'+baseRoot+'.*: *$'
            self.__repat = re.compile(patstr, re.MULTILINE)
            self.__resrch = self.__repat.search(dataString)
            if self.__resrch :
                self.__reclass = baseRoot
                return 8
            patstr = '^ *class .*: *$'
            self.__repat = re.compile(patstr, re.MULTILINE)
            self.__resrch = self.__repat.search(dataString)
            if not self.__resrch :
                self.__repat = None
                break
            class2 = self.__repat.search(dataString, self.__resrch.end())
            if class2 :
                return 16
            b = dataString.find("class ",self.__resrch.start())+6
            while dataString[b] < 'A' :
                b = b+1
            e = b+1
            while dataString[e] >= '0' :
                e = e+1
            self.__reclass = dataString[b:e]
            return 8
        e = len(dataString)-1
        if e<1 :
            return 0
        b = 0
        while b<e and dataString[b].isspace() :
            b=b+1
        if b>=e :
            return 32
        while dataString[e].isspace() :
            e=e-1
        if dataString[b]=='{' and dataString[e]=='}' :
            return 1
        if dataString[b]=='[' and dataString[e]==']' :
            return 1
        if dataString[b]=='<' and dataString[e]=='>' :
            return 2
        return 32

    # This method searches for calls in python to a member method called
    # setExecutionPath and inserts an argument for the file path.
    def insertExecutionPaths(self, pyText, locPath, \
                             locType, locLevel, locName) :
        pathArg = locType.lower()+"/"+locLevel.lower()+"/"
        if len(locName)>0 :
            pathArg += locName+"/"
        pathArg = 'self.setExecutionPath("'+pathArg+locPath+'")'
        patstr = '^ *self.setExecutionPath *[(].*[)] *$'
        self.__repat = re.compile(patstr, re.MULTILINE)
        i = 0
        while True :
            self.__resrch = self.__repat.search(pyText, i)
            if self.__resrch == None :
                break
            b = pyText.find("self",self.__resrch.start())
            e = self.__resrch.end()
            pyText = pyText[:b]+pathArg+pyText[e:]
            i = b+len(pathArg)
        return pyText

    # This method allows one to write data to a localization file.  'fileData'
    # is a string that will become the contents of the resulting localization
    # file; for a short string it will try to first interpret it as the name
    # of a file to read the data from.  'locPath' is the relative path to the
    # file within the level, site or user directories.  'siteUser' and
    # 'typeArg' are as in the Notes at top of this source file, except that
    # the type will not default to "COMMON_STATIC"; it must be specified.
    # For 'levelArg', the value must be "Site", "Workstaion", or "User";
    # it is illegal to write out Base or Configured localization files.
    # Nor will 'levelArg' default if left blank.  'contextUser' is the username
    # used to determine whether one is allowed to write out the file in
    # question, and defaults to the current user.  This method will return
    # True if the operation was successful, and False otherwise; it should
    # not throw exceptions.
    # 
    def putLocFile(self, fileData, locPath, typeArg, levelArg, siteUser, \
                   contextUser="") :

        if locPath=="" :
            msg = "No path to localizaton file provided."
            self.__logger.logMessage(msg, "Error")
            return False

        locLevel = self.processLevelArg(levelArg, True)
        if locLevel == None :
            msg = "Can't put localization file with level '" + levelArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locType = self.processTypeArg(typeArg, True)
        if locLevel == None :
            msg = "Can't put localization file with type '" + typeArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locName = self.processSiteUserArg(siteUser, locLevel)
        if locName == None or locName == "" :
            msg = "Can't put localization file with name '" + siteUser + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locPath0 = self.processPathArg(locPath)

        self.__lfi.setType(locType)
        self.__lfi.setLevel(locLevel)
        self.__lfi.setName(locName)
        if contextUser=="" :
            self.__lfi.setMyContextName(self.__curUser)
        else :
            self.__lfi.setMyContextName(contextUser)

        if isinstance(fileData, dict) or isinstance(fileData, tuple) or \
           isinstance(fileData, list) :
            try :
                pyRoot = self.getPyRootFromFileName(locPath0)
                if pyRoot :
                    result = HazardServicesImporter.formatAsPythonInit( \
                                  fileData, pyRoot)
                else :
                    result = json.dumps(fileData, indent=4)
                return self.__lfi.putFile(locPath0, result)
            except :
                msg = "Can't put localization file "+locPath0
                self.__logger.logMessage(msg, "Error")
            return False

        if not isinstance(fileData, str) :
            msg = "Input fileData argument must be string, dict, or list."
            self.__logger.logMessage(msg, "Error")
            return False

        if len(fileData)<200 :
            try:
                ffff = open(fileData, 'r')
                result = ffff.read()
                ffff.close()
            except:
                result = fileData
        else :
            result = fileData

        try :
            return self.__lfi.putFile(locPath0, result)
        except :
            msg = "Can't put localization file '"+locPath0
            self.__logger.logMessage(msg, "Error")

        return False

    # This method allows one to delete a localization file.  'locPath' is the
    # relative path to the file within the level, site or user directories.
    # 'siteUser' and 'typeArg' are as in the Notes at top of this source file,
    # except that the type will not default to "COMMON_STATIC"; it must be
    # specified.  For 'levelArg', the value must be "Site", "Workstaion", or
    # "User"; it is illegal to delete out Base or Configured localization files.
    # Nor will 'levelArg' default if left blank.  'contextUser' is the username
    # used to determine whether one is allowed to write out the file in
    # question, and defaults to the current user.  This method will return
    # True if the operation was successful, and False otherwise; it should
    # not throw exceptions.
    # 
    def rmLocFile(self, locPath, typeArg, levelArg, siteUser, \
                  contextUser="") :

        if locPath=="" :
            msg = "No path to localizaton file provided."
            self.__logger.logMessage(msg, "Error")
            return False

        locLevel = self.processLevelArg(levelArg, True)
        if locLevel == None :
            msg = "Can't remove localization file with level '" + levelArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locType = self.processTypeArg(typeArg, True)
        if locLevel == None :
            msg = "Can't remove localization file with type '" + typeArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locName = self.processSiteUserArg(siteUser, locLevel)
        if locName == None or locName == "" :
            msg = "Can't remove localization file with name '" + siteUser + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locPath0 = self.processPathArg(locPath)

        self.__lfi.setType(locType)
        self.__lfi.setLevel(locLevel)
        self.__lfi.setName(locName)
        if contextUser=="" :
            self.__lfi.setMyContextName(self.__curUser)
        else :
            self.__lfi.setMyContextName(contextUser)
        return self.__lfi.rmFile(locPath0)

    # This method allows one to get listings of localization directories.   
    # dirPath0 is the relative path to the directory under the level, site
    # and/or user directories.  'typeArg', 'siteUser', and 'levelArg' are
    # as in the Notes at top of this source file.  If 'levelArg' is blank,
    # then a listing will be produced for all levels and combined into a list
    # of all unique file names present.  If non-blank, the 'prefix' and/or
    # 'suffix' arguments can be used to filter the list of files that come
    # back.  If there was an error will return  None, otherwise will return
    # a list; it should not throw exceptions. Sometimes this list can be
    # empty if the directory is there but no matching files are present.
    #
    def listLocFiles(self, dirPath, typeArg, levelArg="", siteUser="", \
                     prefix="", suffix="") :

        if dirPath=="" :
            msg = "No path to localizaton directory provided."
            self.__logger.logMessage(msg, "Error")
            return None

        locLevels = self.processLevelArg(levelArg)
        locType = self.processTypeArg(typeArg)
        locNames = self.processSiteUserArg(siteUser, locLevels)

        self.__lfi.setType(locType)
        if len(locLevels)==1 :
            self.__lfi.setLevel(locLevels[0])
            self.__lfi.setName(locNames)
            try :
                result = self.__lfi.getList(dirPath)
            except :
                return None
        else :
            fail = True
            lll = 0
            fileset = {}
            while lll<len(locLevels):

                locLevel = locLevels[lll]
                locName = locNames[lll]

                lll = lll + 1

                if locLevel != "Base" and locName == "" :
                    continue

                self.__lfi.setLevel(locLevel)
                self.__lfi.setName(locName)
                try:
                    result = self.__lfi.getList(dirPath)
                except:
                    continue
                fail = False
                for one in result :
                    fileset[one] = None
            if fail :
                return None
            result = fileset.keys()

        p = len(prefix)
        s = len(suffix)
        if p+s==0 :
            return result

        prefix0 = self.processPathArg(prefix)
        suffix0 = self.processPathArg(suffix)

        iii = len(result)
        while iii>0 :
            iii = iii-1
            if p>0 and result[iii][:p]!=prefix0 :
                del result[iii]
                continue
            if s>0 and result[iii][-s:]!=suffix0 :
                del result[iii]
                continue

        return result


    # This method allows one to determine which localization levels actually
    # have files present for a given relative localization file path.
    # 'locPath' is the relative path to the file under the level, site
    # and/or user directories.  'siteUser' and 'typeArg' are as in
    # the Notes at top of this source file.  On failure will return None, 
    # otherwise will return a list containing one or more of the following:
    # "User", "Workstation", "Site", "Configured", "Base".
    # This routine should not throw exceptions.
    #
    def locFileLevels(self, locPath, typeArg, siteUser="") :

        if locPath=="" :
            msg = "No path to localizaton file provided."
            self.__logger.logMessage(msg, "Error")
            return None

        locLevels = LocalizationInterface.fullLocLevelList
        locType = self.processTypeArg(typeArg)
        locNames = self.processSiteUserArg(siteUser, locLevels)
        locPath0 = self.processPathArg(locPath)

        self.__lfi.setType(locType)
        fail = True
        lll = 0
        goodList = []
        while lll<len(locLevels):
            locLevel = locLevels[lll]
            locName = locNames[lll]
            lll = lll + 1

            if locLevel != "Base" and locName == "" :
                continue

            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)

            try:
                if self.__lfi.check(locPath0) :
                    goodList.append(locLevel)
                fail = False
            except:
                pass

        if fail :
            return None
        return goodList


    # This method allows one to get the contents of localization files.   
    # locPath is the relative path to the file within the level, site
    # and/or user directories.  'typeArg', 'siteUser', and 'levelArg' are
    # as in the Notes at top of this source file.
    # If a specific levelArg is given, then an attempt will be made to get
    # the contents of that one localization file, and those contents will be
    # returned as a string.  If the levelArg is blank, then an attempt will
    # be made to retrive data for each possible localization level.
    # If these contain JSON data they will be deserialized, combined into a
    # single object using the incremental override rules as in the jsonCombine
    # class, reserialized, and returned as a JSON string.
    # If these contain python logic initializing the same variable as the
    # file basename without the .py extension, then the initialization of the
    # variable will be used as deserialized JSON, the same override logic
    # applied, and the combined data structures will be returned as a valid
    # python variable initialization.
    # If these contain python code defining a class with the file basename,
    # these will automatically be daisy chained into an inheritance
    # heirarcy.
    # Otherwise the data for the least general localization level found will
    # be returned as a string.  A failure will return None; this routine
    # should not throw exceptions.
    #
    def getLocFile(self, locPath, typeArg, levelArg="", siteUser="") :

        if locPath=="" :
            msg = "No path to localizaton file provided."
            self.__logger.logMessage(msg, "Error")
            return None

        locLevels = self.processLevelArg(levelArg)
        locType = self.processTypeArg(typeArg)
        locNames = self.processSiteUserArg(siteUser, locLevels)
        locPath0 = self.processPathArg(locPath)

        self.__lfi.setType(locType)
        if len(locLevels)==1 :
            self.__lfi.setLevel(locLevels[0])
            self.__lfi.setName(locNames)
            try :
                result = self.__lfi.getFile(locPath0)
                pyRoot = self.getPyRootFromFileName(locPath0)
                if pyRoot == None :
                    return result
                t = self.checkDataType(result, pyRoot)
                if t==8 or t==16 :
                    result = self.insertExecutionPaths( \
                               result, locPath0, locType, locLevel, locName)
                return result
            except :
                tbData = traceback.format_exc()
                print tbData
                pass
            return None

        myJC = jsonCombine()
        myx2j = None
        classData = []
        className = []
        classDef = []
        classLevel = []
        pyClass = ""
        ttt = 0
        last = None
        fail = True
        lll = len(locLevels)-1
        while lll>=0:
            locLevel = locLevels[lll]
            locName = locNames[lll]
            lll = lll - 1

            if locLevel != "Base" and locName == "" :
                continue

            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)

            try:
                result = self.__lfi.getFile(locPath0)
                pyRoot = self.getPyRootFromFileName(locPath0)
                t = self.checkDataType(result, pyRoot)
                if t == 0 :
                    continue
                
                if t == 1 or t == 4:
                    last = result
                    #
                    # Try treating this as a Python file and retrieving
                    # the variable definition (which is JSON-like)
                    if t == 4 :
                        result = self.processPython(result, pyRoot)

                    if myJC.accumulate(result) :
                        ttt |= t
                elif t == 8 or t == 16 :
                    classDef.append(self.__resrch)
                    result = self.insertExecutionPaths( \
                               result, locPath0, locType, locLevel, locName)
                    classData.append(result)
                    classLevel.append(locLevel)
                    if pyClass=="" :
                        pyClass = self.__reclass
                    elif self.__reclass != pyClass :
                        t = 16
                    last = result
                    ttt |= t
                elif t==2 :
                    last = result
                    if myx2j==None :
                        self.initializeForXml(locType)
                        myx2j = xml2Json()
                    outXml = myx2j.convert(result)
                    if myJC.accumulate(outXml) :
                        ttt |= t
                else :
                    last = result
                    ttt |= 32
            except:
                continue
            fail = False

        if fail or ttt==0:
            return None
        if ttt==2 and myx2j!=None :
            return myx2j.unconvert(myJC.combine())
        if ttt==4 :
            return HazardServicesImporter.formatAsPythonInit( \
                            myJC.combine(), pyRoot)
        if ttt==1 :
            return json.dumps(myJC.combine(), indent=4)
        if len(classDef)<=1 :
            return last

        # Combine code where it is not all the same class.
        if ttt>=16 and ttt<32 :
            importData = []
            classBody = ""
            i = 0
            while i<len(classDef) :
                b = classDef[i].start()
                preClass = classData[i][:b].split("\n")
                classBody += classData[i][b:]
                i += 1
                for oneI in preClass :
                    if len(importData)==0 :
                        importData.append(oneI)
                    elif oneI == "" :
                        if importData[-1]!="" :
                            importData.append(oneI)
                    elif oneI[:4]!="from" and oneI[:4]!="import" :
                        importData.append(oneI)
                    elif not oneI in importData :
                        importData.append(oneI)
            return "\n".join(importData)+classBody

        if ttt!=8 :
            return last

        # Logic that daisy chains classes into an inheritance heirarchy.
        prevName = ""
        importData = []
        classBody = ""
        classTail = ""
        m = len(classDef)-1
        i = 0
        while i<=m :
            b = classDef[i].start()
            e = classDef[i].end()
            c = classData[i].find(pyClass,b,e)+len(pyClass)
            bb = classData[i].find("(",c,e)+1
            ee = classData[i].find(")",c,e)
            if i>0 :
                # We have to rename the base class.
                if bb>0 and ee>bb :
                    suprClas = classData[i][bb:ee].strip()
                    classData[i] = classData[i][:bb]+prevName+ \
                                   classData[i][ee:]
                    bb = classData[i].find(suprClas+".__init__",c)
                    if bb>0 :
                        ee = bb+len(suprClas)
                        classData[i] = classData[i][:bb]+prevName+ \
                                       classData[i][ee:]
                else :
                    classData[i] = classData[i][:c]+"("+prevName+")"+ \
                                   classData[i][c:]
            if i<m :
                # We have to rename the class.
                prevName = "_"+classLevel[i]
                classData[i] = classData[i][:c]+prevName+classData[i][c:]
                prevName = pyClass+prevName
            preClass = classData[i][:b].split("\n")
            e = classData[i].find("\ndef main",c)
            if (e>0) :
                classBody += classData[i][b:e]
                classTail = classData[i][e:]
            else :
                classBody += classData[i][b:]
            i += 1
            for oneI in preClass :
                if len(importData)==0 :
                    importData.append(oneI)
                elif oneI == "" :
                    if importData[-1]!="" :
                        importData.append(oneI)
                elif oneI[:4]!="from" and oneI[:4]!="import" :
                    importData.append(oneI)
                elif not oneI in importData :
                    importData.append(oneI)

        return "\n".join(importData)+classBody+classTail


    # This method gets object representations of the contents of localization
    # files. locPath is the relative path to the file within the level, site
    # and/or user directories.  'typeArg', 'siteUser', and 'levelArg' are
    # as in the Notes at top of this source file.
    # If a specific levelArg is given, then an attempt will be made to access
    # that one localization file, otherwise an attempt will be made to access
    # data for each possible localization level. 
    # If the file(s) contain JSON or XML, they will be deserialized into
    # JSON serializable objects, and if multiple files were accessed they will
    # combined into a single object using the incremental override rules as
    # in the jsonCombine class.  
    # If these contain python logic initializing the same variable as the
    # file basename without the .py extension, then the initialization of the
    # variable will be used as deserialized JSON, the same override logic
    # applied, and the combined data structures will be returned.
    # Otherwise the string representing the data for the least general
    # localization level found will be returned. A failure will return None;
    # this routine should not throw exceptions.
    def getLocData(self, locPath, typeArg, levelArg="", siteUser="",
                   incrementalOverride=True, incrementalOverrideImports=False) :

        if locPath=="" :
            msg = "No path to localizaton file provided."
            self.__logger.logMessage(msg, "Error")
            return None

        locLevels = self.processLevelArg(levelArg)
        locType = self.processTypeArg(typeArg)
        locNames = self.processSiteUserArg(siteUser, locLevels)
        locPath0 = self.processPathArg(locPath)

        myJC = jsonCombine()
        myx2j = None
        self.__lfi.setType(locType)
        if len(locLevels)==1 :
            self.__lfi.setLevel(locLevels[0])
            self.__lfi.setName(locNames)
            try :
                result = self.__lfi.getFile(locPath0)
                pyRoot = self.getPyRootFromFileName(locPath0)
                t = self.checkDataType(result, pyRoot)
                
                if t==4 :
                    #
                    # Try treating this as a Python file and retrieving
                    # the variable definition (which is JSON-like)
                    pythonValue = self.processPython(result, pyRoot, \
                      incrementalOverrideImports=incrementalOverrideImports)
                    
                    # No matter what, still have to convert python to data.
                    #if not incrementalOverride:
                    #    return pythonValue
                    
                    if  myJC.accumulate(pythonValue) :
                        return myJC.combine()
                elif t==1 :
                    if myJC.accumulate(result) :
                        return myJC.combine()
                elif t==2 :
                    self.initializeForXml(locType)
                    myx2j = xml2Json()
                    outXml = myx2j.convert(result)
                    if outXml==None :
                        return result
                    return myx2j.simplify(outXml)

                return result
            except :
                pass
            return None

        last = None
        fail = True
        jsonYes = False

        # Loop direction changes depending on whether we are doing
        # incremental or replace override.
        if incrementalOverride:
            lll = len(locLevels)-1
            d = -1
            eee = -1
        else :
            lll = 0
            d = 1
            eee = len(locLevels)

        while lll!=eee:
            locLevel = locLevels[lll]
            locName = locNames[lll]
            lll = lll + d

            if locLevel != "Base" and locName == "" :
                continue

            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
            try:
                result = self.__lfi.getFile(locPath0)
                pyRoot = self.getPyRootFromFileName(locPath0)
                t = self.checkDataType(result, pyRoot)
                last = result

                if t==8 :
                    result = self.getLocFile( \
                              locPath, typeArg, levelArg, siteUser)
                    return result
                elif t==4 :
                    #
                    # Try treating this as a Python file and retrieving
                    # the variable definition (which is JSON-like)
                    pythonValue = self.processPython(result, pyRoot, \
                       incrementalOverrideImports=incrementalOverrideImports)
                    if not myJC.accumulate(pythonValue) :
                        continue
                    jsonYes = True
                elif t==1 :
                    if myJC.accumulate(result) :
                        jsonYes = True
                elif t==2 :
                    if myx2j==None :
                        self.initializeForXml(locType)
                        myx2j = xml2Json()
                    outXml = myx2j.convert(result)
                    if myJC.accumulate(outXml) :
                        jsonYes = True
            except:
                continue

            if not incrementalOverride:
                if jsonYes :
                    return myJC.combine()
                return last
            fail = False

        if fail :
            return None
        if not jsonYes:
            return last

        if myx2j!=None :
            return myx2j.simplify(myJC.combine())
        return myJC.combine()
    
    def convertPythonToJSON(self, pythonValue):
        """
        Tries to convert Python to JSON. The JSON represents
        the contents of the specified python value.
        @param pythonValue: The python value to convert to JSON
        @return: The python converted to JSON (a string)
        """

            
        result = None

        if pythonValue is not None:
                    
            try:
                result = json.dumps(pythonValue)
            except:
                traceback.print_exc()
        
        return result
    
    def processPython(self, pythonString, variableName, 
                            incrementalOverrideImports=True):
        """
        Processes a python file. Returns the value of the 
        specified variableName.
        @param pythonString: The python string representing the contents of .py file.
        @param variableName: The name of the variable containing the
                             python result after the exec
        @return: The value of the specified variable 
        """

        # Load the Hazard Services Importer. This handles
        # incremental override of imports.
        hsImporter = HazardServicesImporter.getInstance(incrementalOverrideImports=incrementalOverrideImports)

        sys.meta_path.append(hsImporter)
        
        try:
            exec pythonString
        except:
            ffff = open("/tmp/processPythonFail.txt", "w")
            ffff.write(pythonString+"\n")
            ffff.close()
            os.chmod("/tmp/processPythonFail.txt", 0666)
            traceback.print_exc()
            
        result = None
        
        try:
            result = eval(variableName)
        except:
            traceback.print_exc()
        
        # Unload the Hazard Services Importer. This is not
        # something we need to keep around.
        sys.meta_path.remove(hsImporter)
        
        return result

    
    def getPyRootFromFileName(self, filename):
        """
        @param filename: The filename
        @return: A variable name derived from the filename
        """
        baseName = os.path.basename(filename)
        if baseName[-3:] == ".py" :
            return os.path.splitext(baseName)[0]
        return None

