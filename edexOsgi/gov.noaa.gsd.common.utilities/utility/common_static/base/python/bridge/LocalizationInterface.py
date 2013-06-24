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
from jsonCombine import *
from xml2Json import *
import HazardServicesImporter
from HazardServicesLogger import *

try:
    from LocalFileInstaller import *
except :
    from AppFileInstaller import *

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

caveEdexHost = None
edexLocMap = { "" : "" }
hostnameF = None

class LocalizationInterface():

    #hsImporter = bridge.HazardServicesImporter.HazardServicesImporter()

    # If the id of the localization host is an empty string, defaults to the
    # EDEX host currently being used by Cave.
    def __init__(self, edexHost="") :
        global caveEdexHost
        self.__curUser = getpass.getuser()
        self.__defLoc = None
        self.__logger = HazardServicesLogger.getInstance()
        self.__repat = None
        self.__resrch = None
        self.__javaenv = False
        if edexHost!="" :
            self.__locServer = edexHost
            try :
                self.__lfi = LocalFileInstaller(edexHost)
                self.__javaenv = True
            except :
                self.__lfi = AppFileInstaller(edexHost)
            return
        if caveEdexHost!=None :
            self.__locServer = caveEdexHost
            try :
                self.__lfi = LocalFileInstaller(caveEdexHost)
                self.__javaenv = True
            except :
                self.__lfi = AppFileInstaller(caveEdexHost)
            return
        caveEdexHost = ""
        prefspath = os.environ["HOME"] + "/caveData/.metadata/.plugins" + \
           "/org.eclipse.core.runtime/.settings/localization.prefs"
        try :
            ffff = open(prefspath)
            prefsData =  ffff.read()
            ffff.close()
            while True :
                i = prefsData.find("httpServerAddress=")
                if i<0 :
                    break
                i = prefsData.find("//",i)
                if i<0 :
                    break
                j = i+3
                while prefsData[j]!="\\" and prefsData[j]!=":" :
                    j = j+1
                caveEdexHost = prefsData[i+2:j]
                break
        except :
            pass
        if caveEdexHost == "" :
            msg = "Could not determine host of current EDEX server."
            self.__logger.logMessage(msg, "Error")
        self.__locServer = caveEdexHost
        try :
            self.__lfi = LocalFileInstaller(caveEdexHost)
            self.__javaenv = True
        except :
            self.__lfi = AppFileInstaller(caveEdexHost)

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
        return self.__defLoc

    # Return the localization id associated with the currently connected EDEX.
    # This method contains a robust, albeit heavy handed, method for determining
    # the default localization id for the EDEX one is performing localization
    # file interactions with.
    def curEdexLoc(self) :
        lookupLoc = edexLocMap.get(self.__locServer)
        if lookupLoc!=None :
            return lookupLoc

        if self.__locServer=="localhost" :
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

        cmd = 'export DEFAULT_HOST='+self.__locServer+' ; '
        cmd = cmd + '( echo from com.raytheon.uf.common.message.response '
        cmd = cmd +        'import ResponseMessageGeneric ; '
        cmd = cmd + 'echo from com.raytheon.uf.edex.core.props '
        cmd = cmd +        'import PropertiesFactory ; '
        cmd = cmd + 'echo "site = PropertiesFactory.getInstance().'
        cmd = cmd +        "getEnvProperties().getEnvValue('SITENAME')"+'" ; '
        cmd = cmd + 'echo "return ResponseMessageGeneric(site)" ) | '
        cmd = cmd + '/awips2/fxa/bin/uengine -r python'
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        c = stdout.find('</contents>')
        if c>10 and stdout[c-4]=='>' :
            siteData = stdout[c-3:c]
            if siteData.isalpha() and siteData.isupper() :
                edexLocMap[self.__locServer] = siteData
                return siteData

        msg = "Could not determine localization id for EDEX server " + \
              self.__locServer
        self.__logger.logMessage(msg, "Error")
        edexLocMap[self.__locServer] = ""
        return ""


    # Get the current working localization id.
    def getDefLoc(self) :
        if self.__defLoc==None :
            self.__defLoc = self.curEdexLoc()
        return self.__defLoc


    # For now returns 1=JSON, 2=XML, 4=Python data, 8=Python class,
    # 16=Misc python, 32=other
    # This is not meant to be called by outside clients.
    def checkDataType(self, dataString, baseRoot=None) :
        if baseRoot :
            patstr = '^'+baseRoot+r' *='
            self.__repat = re.compile(patstr, re.MULTILINE)
            self.__resrch = self.__repat.search(dataString)
            if self.__resrch :
                return 4
            patstr = '^ *class *'+baseRoot+'.*: *$'
            self.__repat = re.compile(patstr, re.MULTILINE)
            self.__resrch = self.__repat.search(dataString)
            if self.__resrch :
                return 8
            patstr = '^ *class .*: *$'
            self.__repat = re.compile(patstr, re.MULTILINE)
            self.__resrch = self.__repat.search(dataString)
            if self.__resrch :
                return 16
            self.__repat = None
            self.__resrch = None
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

    # This method formats an object into python that can initialize the
    # indicated variable as this object.  This method is not normally meant
    # to be called by outside clients.
    def formatAsPythonInit(self, dataObject, variableName) :
        result = json.dumps(dataObject, indent=4)

        # JSON has different representations for the python symbols
        # None, True, and False.
        c1 = 0
        c2 = result.find('"',c1)
        while True :
            if c2<0 :
                c2 = len(result)
            if c2-c1>4 :
                c = result.find("null",c1,c2)
                while c>=0 :
                    result = result[:c]+"None"+result[c+4:]
                    c = result.find("null",c+4,c2)
                c = result.find("true",c1,c2)
                while c>=0 :
                    result = result[:c]+"True"+result[c+4:]
                    c = result.find("true",c+4,c2)
                c = result.find("false",c1,c2)
                while c>=0 :
                    result = result[:c]+"False"+result[c+5:]
                    c = result.find("false",c+5,c2)
            c1 = result.find('"',c2+1)
            if c1<0 :
                break
            c2 = result.find('"',c1+1)

        return variableName + " = \\\n" + result + "\n"

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

        locLevel = levelArg.upper()
        if locLevel=="SITE" :
            locLevel = "Site"
        elif locLevel=="WORKSTATION" :
            locLevel = "Workstation"
        elif locLevel=="USER" :
            locLevel = "User"
        else :
            msg = "Can't put localization file with level '" + levelArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locType = typeArg.upper()
        if typeArg[:6]=="COMMON" :
            locType = "COMMON_STATIC"
        elif typeArg[:4]=="EDEX" :
            locType = "EDEX_STATIC"
        elif typeArg[:4]=="CAVE" :
            locType = "CAVE_STATIC"
        else :
            msg = "Can't put localization file with type '" + typeArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        if len(siteUser)==3 and siteUser.isalpha() and siteUser.isupper() :
            sss = siteUser
            locName = ""
        else :
            sss = self.getDefLoc()
            locName = siteUser

        locName = siteUser
        if locName=="" :
            if locLevel == "User" :
                locName = self.__curUser
            elif locLevel == "Site" :
                locName = sss
            else :
                locName = self.getThisHost()
            if locName == "" :
                return False

        locPath0 = locPath
        i = locPath0.find("###")
        while i>=0 :
            locPath0 = locPath0[:i]+sss+locPath0[i+3:]
            i = locPath0.find("###", i+3)

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
                    result = self.formatAsPythonInit(fileData, pyRoot)
                else :
                    result = json.dumps(fileData, indent=4)

                self.__lfi.putFile(locPath0, result)
                return True
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
            self.__lfi.putFile(locPath0, result)
            return True
        except :
            msg = "Can't put localization file '"+locPath0
            self.__logger.logMessage(msg, "Error")

        return False

    # This method allows one to submit an arbitrary command through the
    # uEngine to an arbitrary host.  If the host is "postgres", will attempt
    # to verify that the command is run on the host that has postgres running.
    def submitCommand(self, submit, host="") :
        tmpcmdfile = "tmpcmd"+str(os.getpid())+".csh"
        if host=="postgres" :
            mycmd = ""
            tmpcmddata = "#!/bin/csh\n"
            tmpcmddata += 'if ( "$1" != "" ) then\n'
            tmpcmddata += '    set n = `ps -C postmaster -U awips | '
            tmpcmddata +=           'grep postmaster | wc -l`\n'
            tmpcmddata += '    if ( $n == 0 ) then\n'
            tmpcmddata += '        ssh -q "dx1" $0\n'
            tmpcmddata += '        exit\n'
            tmpcmddata += '    endif\n'
            tmpcmddata += 'endif\n'
            tmpcmddata += submit+'\n'
            tmpcmddata += \
               '( ( sleep 3 ; rm -f $0 ) & ) >& /dev/null\n'
        elif host!="" :
            mycmd = ""
            tmpcmddata = "#!/bin/csh\n"
            tmpcmddata += 'if ( "$1" != "" ) then\n'
            tmpcmddata += '    ssh -q "$1" $0\n'
            tmpcmddata += '    exit\n'
            tmpcmddata += 'endif\n'
            tmpcmddata += submit+'\n'
            tmpcmddata += \
               '( ( sleep 3 ; rm -f $0 ) & ) >& /dev/null\n'
        else :
            tmpcmddata = ""
            mycmd = submit
        d = chr(34)
        myscript = """
from com.raytheon.uf.common.message.response import ResponseMessageGeneric
import subprocess
import os
import stat
"""
        if len(tmpcmddata)>0 :
            myscript += """
if os.path.isdir("/home/awips/bin") :
    tmpcmddir = "/home/awips/bin/"
elif os.path.isdir("/data_store") :
    tmpcmddir = "/data_store/"
else :
    tmpcmddir = os.environ["HOME"]+"/"
"""
            myscript += "\nexechost = "+d+d+d+host+d+d+d
            myscript += "\ntmpcmdfile = "+d+d+d+tmpcmdfile+d+d+d
            myscript += "\ntmpcmddata = "+d+d+d+tmpcmddata+d+d+d
            myscript += """
tmpcmdpath = tmpcmddir+tmpcmdfile
ffff = open(tmpcmdpath, 'w')
ffff.write(tmpcmddata)
ffff.close()
os.chmod(tmpcmdpath, stat.S_IRWXU|stat.S_IRWXG|stat.S_IROTH|stat.S_IXOTH)
mycmd = tmpcmdpath+" "+exechost
"""
        else :
            myscript += "\nmycmd = "+d+d+d+mycmd+' '+d+d+d
        myscript += """
p = subprocess.Popen(mycmd, shell=True, stdout=subprocess.PIPE)
(stdout, stderr) = p.communicate()
return ResponseMessageGeneric(stdout)
"""
        mypyfile = "/tmp/"+str(os.getpid())+".py"
        fff = open(mypyfile, "w")
        fff.write(myscript)
        fff.close()
        cmd = 'export DEFAULT_HOST='+self.__locServer+' ; '
        cmd = cmd + '/awips2/fxa/bin/uengine -r python < '+mypyfile
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        os.remove(mypyfile)
        return stdout

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

        locLevel = levelArg.upper()
        if locLevel=="SITE" :
            locLevel = "Site"
        elif locLevel=="WORKSTATION" :
            locLevel = "Workstation"
        elif locLevel=="USER" :
            locLevel = "User"
        else :
            msg = "Can't remove localization file with level '" + levelArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        locType = typeArg.upper()
        if typeArg[:6]=="COMMON" :
            locType = "COMMON_STATIC"
        elif typeArg[:4]=="EDEX" :
            locType = "EDEX_STATIC"
        elif typeArg[:4]=="CAVE" :
            locType = "CAVE_STATIC"
        else :
            msg = "Can't remove localization file with type '" + typeArg + "'"
            self.__logger.logMessage(msg, "Error")
            return False

        if len(siteUser)==3 and siteUser.isalpha() and siteUser.isupper() :
            sss = siteUser
            locName = ""
        else :
            sss = self.getDefLoc()
            locName = siteUser

        locName = siteUser
        if locName=="" :
            if locLevel == "User" :
                locName = self.__curUser
            elif locLevel == "Site" :
                locName = sss
            else :
                locName = self.getThisHost()
            if locName == "" :
                return False

        locPath0 = locPath
        i = locPath0.find("###")
        while i>=0 :
            locPath0 = locPath0[:i]+sss+locPath0[i+3:]
            i = locPath0.find("###", i+3)

        if self.__javaenv :
            self.__lfi.setType(locType)
            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
            if contextUser=="" :
                self.__lfi.setMyContextName(self.__curUser)
            else :
                self.__lfi.setMyContextName(contextUser)
            return self.__lfi.rmFile(locPath0)

        # For now lets do an end run by submitting this to the uEngine
        filePath = '/awips2/edex/data/utility/'+locType.lower()+'/'+ \
                   locLevel.lower()+'/'+locName+'/'+locPath0
        cmd = 'export DEFAULT_HOST='+self.__locServer+' ; '
        cmd = cmd + '( echo import subprocess ; echo import os ;'
        cmd = cmd + 'echo from com.raytheon.uf.common.message.response '
        cmd = cmd +        'import ResponseMessageGeneric ; '
        cmd = cmd + 'echo mycmd = '+"'"+'"rm -f '+filePath+'"'+"' ;"
        cmd = cmd + 'echo "p = subprocess.Popen(mycmd, shell=True, '
        cmd = cmd +        'stdout=subprocess.PIPE)" ;'
        cmd = cmd + 'echo "(stdout, stderr) = p.communicate()" ; '
        cmd = cmd + 'echo "return ResponseMessageGeneric('+"''"+')" ) | '
        cmd = cmd + '/awips2/fxa/bin/uengine -r python'
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        if stderr==None :
            return True
        return False


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

        locLevel = levelArg.upper()
        if locLevel=="SITE" :
            locLevels = [ "Site" ]
        elif locLevel=="USER" :
            locLevels = [ "User" ]
        elif locLevel=="WORKSTATION" :
            locLevels = [ "Workstation" ]
        elif locLevel=="BASE" :
            locLevels = [ "Base" ]
        elif locLevel=="CONFIGURED" :
            locLevels = [ "Configured" ]
        else :
            locLevels = [ "User", "Workstation", "Site", "Configured", "Base" ]
        locLevel = locLevels[0]

        locType = typeArg.upper()
        if typeArg[:6]=="COMMON" :
            locType = "COMMON_STATIC"
        elif typeArg[:4]=="EDEX" :
            locType = "EDEX_STATIC"
        elif typeArg[:4]=="CAVE" :
            locType = "CAVE_STATIC"
        else :
            locType = "COMMON_STATIC"

        if len(siteUser)==3 and siteUser.isalpha() and siteUser.isupper() :
            sss = siteUser
            locName = ""
        else :
            sss = self.getDefLoc()
            locName = siteUser

        if len(locLevels)>1 :
            if siteUser.find(".")>0 :
                locNames = [self.__curUser, siteUser, sss, sss, "" ]
            elif len(siteUser)>0 :
                hhh = self.getThisHost()
                locNames = [siteUser, hhh, sss, sss, "" ]
            else :
                hhh = self.getThisHost()
                locNames = [self.__curUser, hhh, sss, sss, "" ]
        elif locLevel=="User" :
            if locName=="" :
                locName = self.__curUser
        elif locLevel=="Workstation" :
            if locName=="" :
                locName = self.getThisHost()
        elif locLevel!="Base" :
            locName = sss

        self.__lfi.setType(locType)
        if len(locLevels)==1 :
            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
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
                self.__lfi.setLevel(locLevel)
                self.__lfi.setName(locName)
                lll = lll + 1
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

        prefix0 = prefix
        i = prefix0.find("###")
        while i>=0 :
            prefix0 = prefix0[:i]+sss+prefix0[i+3:]
            i = prefix0.find("###", i+3)
        suffix0 = suffix
        i = suffix0.find("###")
        while i>=0 :
            suffix0 = suffix0[:i]+sss+suffix0[i+3:]
            i = suffix0.find("###", i+3)

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

        locLevels = [ "User", "Workstation", "Site", "Configured", "Base" ]

        locType = typeArg.upper()
        if typeArg[:6]=="COMMON" :
            locType = "COMMON_STATIC"
        elif typeArg[:4]=="EDEX" :
            locType = "EDEX_STATIC"
        elif typeArg[:4]=="CAVE" :
            locType = "CAVE_STATIC"
        else :
            locType = "COMMON_STATIC"

        if len(siteUser)==3 and siteUser.isalpha() and siteUser.isupper() :
            sss = siteUser
            locName = ""
        else :
            sss = self.getDefLoc()
            locName = siteUser

        if locName.find(".")>0 :
            locNames = [self.__curUser, locName, sss, sss, "" ]
        elif len(locName)>0 :
            hhh = self.getThisHost()
            locNames = [locName, hhh, sss, sss, "" ]
        else :
            hhh = self.getThisHost()
            locNames = [self.__curUser, hhh, sss, sss, "" ]

        locPath0 = locPath
        i = locPath0.find("###")
        while i>=0 :
            locPath0 = locPath0[:i]+sss+locPath0[i+3:]
            i = locPath0.find("###", i+3)

        self.__lfi.setType(locType)
        fail = True
        lll = 0
        goodList = []
        while lll<len(locLevels):
            locLevel = locLevels[lll]
            locName = locNames[lll]
            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
            lll = lll + 1
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
    # these will automaticallyh be daisy chained into an inheritance
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

        locLevel = levelArg.upper()
        if locLevel=="SITE" :
            locLevels = [ "Site" ]
        elif locLevel=="USER" :
            locLevels = [ "User" ]
        elif locLevel=="BASE" :
            locLevels = [ "Base" ]
        elif locLevel=="CONFIGURED" :
            locLevels = [ "Configured" ]
        elif locLevel=="WORKSTATION" :
            locLevels = [ "Workstation" ]
        else :
            locLevels = [ "User", "Workstation", "Site", "Configured", "Base" ]
        locLevel = locLevels[0]

        locType = typeArg.upper()
        if typeArg[:6]=="COMMON" :
            locType = "COMMON_STATIC"
        elif typeArg[:4]=="EDEX" :
            locType = "EDEX_STATIC"
        elif typeArg[:4]=="CAVE" :
            locType = "CAVE_STATIC"
        else :
            locType = "COMMON_STATIC"

        if len(siteUser)==3 and siteUser.isalpha() and siteUser.isupper() :
            sss = siteUser
            locName = ""
        else :
            sss = self.getDefLoc()
            locName = siteUser

        if len(locLevels)>1 :
            if locName.find(".")>0 :
                locNames = [self.__curUser, locName, sss, sss, "" ]
            elif len(locName)>0 :
                hhh = self.getThisHost()
                locNames = [locName, hhh, sss, sss, "" ]
            else :
                hhh = self.getThisHost()
                locNames = [self.__curUser, hhh, sss, sss, "" ]
        elif locLevel=="User" :
            if locName=="" :
                locName = self.__curUser
        elif locLevel=="Workstation" :
            if locName=="" :
                locName = self.getThisHost()
        elif locLevel=="Base" :
            locName = ""
        else :
            locName = sss

        locPath0 = locPath
        i = locPath0.find("###")
        while i>=0 :
            locPath0 = locPath0[:i]+sss+locPath0[i+3:]
            i = locPath0.find("###", i+3)

        self.__lfi.setType(locType)
        if len(locLevels)==1 :
            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
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
            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
            lll = lll - 1
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
                    pyClass = pyRoot
                    classDef.append(self.__resrch)
                    result = self.insertExecutionPaths( \
                               result, locPath0, locType, locLevel, locName)
                    classData.append(result)
                    classLevel.append(locLevel)
                    last = result
                    ttt |= t
                elif t==2 :
                    last = result
                    if myx2j==None :
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
            return self.formatAsPythonInit(myJC.combine(), pyRoot)
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

        locLevel = levelArg.upper()
        if locLevel=="SITE" :
            locLevels = [ "Site" ]
        elif locLevel=="USER" :
            locLevels = [ "User" ]
        elif locLevel=="BASE" :
            locLevels = [ "Base" ]
        elif locLevel=="CONFIGURED" :
            locLevels = [ "Configured" ]
        elif locLevel=="WORKSTATION" :
            locLevels = [ "Workstation" ]
        else :
            locLevels = [ "User", "Workstation", "Site", "Configured", "Base" ]
        locLevel = locLevels[0]

        locType = typeArg.upper()
        if typeArg[:6]=="COMMON" :
            locType = "COMMON_STATIC"
        elif typeArg[:4]=="EDEX" :
            locType = "EDEX_STATIC"
        elif typeArg[:4]=="CAVE" :
            locType = "CAVE_STATIC"
        else :
            locType = "COMMON_STATIC"

        if len(siteUser)==3 and siteUser.isalpha() and siteUser.isupper() :
            sss = siteUser
            locName = ""
        else :
            sss = self.getDefLoc()
            locName = siteUser

        if len(locLevels)>1 :
            if locName.find(".")>0 :
                locNames = [self.__curUser, locName, sss, sss, "" ]
            elif len(locName)>0 :
                hhh = self.getThisHost()
                locNames = [locName, hhh, sss, sss, "" ]
            else :
                hhh = self.getThisHost()
                locNames = [self.__curUser, hhh, sss, sss, "" ]
        elif locLevel=="User" :
            if locName=="" :
                locName = self.__curUser
        elif locLevel=="Workstation" :
            if locName=="" :
                locName = self.getThisHost()
        elif locLevel=="Base" :
            locName = ""
        else :
            locName = sss

        locPath0 = locPath
        i = locPath0.find("###")
        while i>=0 :
            locPath0 = locPath0[:i]+sss+locPath0[i+3:]
            i = locPath0.find("###", i+3)

        myJC = jsonCombine()
        myx2j = None
        self.__lfi.setType(locType)
        if len(locLevels)==1 :
            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
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
            self.__lfi.setLevel(locLevel)
            self.__lfi.setName(locName)
            lll = lll + d
            try:
                result = self.__lfi.getFile(locPath0)
                pyRoot = self.getPyRootFromFileName(locPath0)
                t = self.checkDataType(result, pyRoot)
                last = result
                
                if t==4 :
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
        hsImporter = HazardServicesImporter.HazardServicesImporter.getInstance(incrementalOverrideImports=incrementalOverrideImports)

        sys.meta_path.append(hsImporter)
        
        try:
            exec pythonString
        except:
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

