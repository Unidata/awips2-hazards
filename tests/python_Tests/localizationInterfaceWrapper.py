#!/usr/bin/env python
#
#  This is a very simple driver for the LocalizationInterface class
#  that can be called from the shell for testing purposes.
#
import json
import os
import sys
import subprocess

if len(sys.argv)==1 :
    print """\
This program is a driver for the LocalizationInterface class that can be called
from a shell for testing purposes.

Usage:

localizationInterfaceWrapper.py {+edex_host} \\
     {-r|-d|-D|-p|-x|-l|-v|-Z} {localization_id} {user_name} {host_name} \\
     {COMMON_STATIC|EDEX_STATIC|CAVE_STATIC} {CODE|OVERRIDE} \\
     {Base|Configured|Site|Desk|Workstation|User} \\
     localization_file_path {data_file_path} {'prefix*suffix'}

The only mandatory argument is the localization_file_path.
The +edex_host argument must be first, and the data_file_path must be after the
localization_file_path; all other arguments can be in any order.  The default
operation type is -r, which returns the text of a localization file.  The other
operation types are as follows: -d returns a data structure for a localization
file (-D applies incremental override to imports), -p writes out a localization
file, -x deletes a localization file, -l lists localization files, and -v shows
the localization levels available for a given localization file. -Z modifies
the other operations such that the code will directly call AppFileInstaller;
this does not fully support all options and fewer things can default. If no
localizaton level {Base, etc} is given, incremental override will be applied
to what is returned.  If multiple levels are given, LocalizationInterface will
be called for each level independently, if this makes sense for the chosen
operation type.  The localization type (COMMON_STATIC, etc) and the
localizaton level are case insensitive.  For the -l operation, the
localization_file_path is actually a directory, and this is the only operation
where the 'prefix*suffix' argument (the * is literal, and the quotes are
needed) applies.  The data_file_path is only used for the -p operation;
the contents of that file are what is written to the specified localization
file. Otherwise the -p operation uses what is piped in from stdin.  The literal
arguments CODE and OVERRIDE invoke a mode where files are read straight out of
source code directories instead of transacting with EDEX.  CODE means only
read Base level files, OVERRIDE will allow files of other levels to be read,
but as flat files from under /awips2/edex/data/utility/.

"""
    exit()

from updateSysPath import updateSysPath

baselinePaths = [ "edexOsgi/com.raytheon.uf.tools.cli/impl/src", \
                  "pythonPackages/ufpy" ]
updateSysPath(fromSibling=baselinePaths)

byAppFileInstaller = False

# Alternate edex host must be leading argument, indicated by leading +.
edexhost = ""
argIdx = 1
if len(sys.argv)>argIdx and sys.argv[argIdx][0]=="+" :
    edexhost = sys.argv[argIdx][1:]
    argIdx = 2

typeArg = "COMMON_STATIC"
levelArg = ""
siteArg = ""
userArg = ""
hostArg = ""
nameArg = ""
pathArg = ""
inputArg = ""
prefix = ""
suffix = ""
levelList = []

opArg = "-r"

while argIdx < len(sys.argv) :

    onearg = sys.argv[argIdx]
    if onearg=="--exit" :
        exit()
    argIdx = argIdx+1

    # Check for operation type
    if len(onearg)==2 and onearg[0]=="-" :
        if onearg[1]=="Z" :
            byAppFileInstaller = True
        else :
            opArg = onearg
        continue

    # Try for a file path
    if onearg.find("/")>0:
        if pathArg=="" :
            pathArg = onearg
        else :
            inputArg = onearg
        continue

    # If contains a *, assume we use it to build prefixes and suffixes
    i = onearg.find("*")
    if i>=0:
        prefix = onearg[:i]
        suffix = onearg[i+1:]
        continue

    uparg = onearg.upper()
    loarg = onearg.lower()

    # Try for a host name.
    if loarg==onearg and hostArg=="" :
        cmd = "nslookup "+onearg+ \
              "  | grep Name | tr '\t' ' '| sed 's/Name: *//g'"
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        if len(stdout)>0 :
            hostArg = stdout
            nameArg = hostArg
            continue

    # Try again for a file path
    if onearg.find(".")>0:
        if pathArg=="" :
            pathArg = onearg
        else :
            inputArg = onearg
        continue

    # Try for pulling straight out of source code.
    if onearg == "CODE" or onearg == "OVERRIDE" or onearg == "CODE+":
        os.environ["LOCALIZATION_DATA_SOURCE"] = onearg
        continue

    # Try for a localization level
    if uparg=="BASE" :
        levelArg = "Base"
        levelList.append(levelArg)
        continue
    elif uparg=="CONFIGURED" :
        levelArg = "Configured"
        levelList.append(levelArg)
        continue
    elif uparg=="SITE" :
        levelArg = "Site"
        levelList.append(levelArg)
        continue
    elif uparg=="DESK" :
        levelArg = "Desk"
        levelList.append(levelArg)
        continue
    elif uparg=="WORKSTATION" :
        levelArg = "Workstation"
        levelList.append(levelArg)
        continue
    elif uparg=="USER" :
        levelArg = "User"
        levelList.append(levelArg)
        continue

    # Try for a localization type
    if uparg[:6]=="COMMON" :
        typeArg = "COMMON_STATIC"
        continue
    elif uparg[:4]=="EDEX" :
        typeArg = "EDEX_STATIC"
        continue
    elif uparg[:4]=="CAVE" :
        typeArg = "CAVE_STATIC"
        continue

    # Now if all the same case, assume a site or user
    if uparg==onearg and onearg.isalpha() and len(onearg)==3 :
        siteArg = onearg
        nameArg = onearg
        continue
    if loarg==onearg and onearg.isalpha() and len(onearg)<9 :
        cmd = "test -d ~"+onearg+" && echo yes"
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        (stdout, stderr) = p.communicate()
        if stdout[:3]=="yes" :
            userArg = onearg
            nameArg = onearg
            continue

    # Try again for a file path
    if pathArg=="" :
        pathArg = onearg
    else :
        inputArg = onearg

myLI = None
myAFI = None
if byAppFileInstaller :
    if len(levelList) == 0 :
        sys.stderr.write( \
          "Can't go direct to AppFileInstaller with no level specified.\n")
        exit()
    if edexhost=="" :
        sys.stderr.write("AppFileInstaller can't default edex host.\n")
        exit()
    from AppFileInstaller import AppFileInstaller
    myAFI = AppFileInstaller(edexhost)
    myAFI.setType(typeArg)
    myAFI.setLevel(levelArg)
    myAFI.setName(nameArg)
else :
    from LocalizationInterface import LocalizationInterface
    myLI = LocalizationInterface(edexhost)

# Depending on the name modifier(s) supplied, put the one most applicable
# into the siteUser variable.
adaptLevelToArg = False
siteUser = ""
if siteArg!="" and siteUser=="" :
    if adaptLevelToArg and levelArg=="" and hostArg==userArg :
        levelArg = "Site"
    if levelArg=="" or levelArg=="Site" or levelArg=="Configured" :
        siteUser = siteArg
if userArg!="" and siteUser=="" :
    if adaptLevelToArg and levelArg=="" and hostArg==siteArg :
        levelArg = "User"
    if levelArg=="" or levelArg=="User" :
        siteUser = userArg
if hostArg!="" and siteUser=="" :
    if adaptLevelToArg and levelArg=="" and userArg==siteArg :
        levelArg = "Workstation"
    if levelArg=="" or levelArg=="Workstation" :
        siteUser = userArg

# It does not make sense to run these commands independently for multiple
# Localization levels.

# Write file
if opArg == "-p" :
    inputDiag = "Data from "+inputArg
    if inputArg=="" :
        inputArg = sys.stdin.read()
        inputDiag = "Data from stdin"
    elif myAFI :
        try :
            ffff = open(inputArg, "r")
            indata = ffff.read()
            ffff.close()
            inputArg = indata
        except:
            pass
    if myLI :
        output = myLI.putLocFile(inputArg, pathArg, typeArg,
                                 levelArg, siteUser)
    else :
        myAFI.setMyContextName(os.environ["USER"])
        output = myAFI.putFile(pathArg, inputArg)
    if output :
        sys.stdout.write("Success\n")
    elif myLI:
        sys.stderr.write("FAILED:\nputLocFile('"+inputDiag+"', '"+ \
          pathArg+"', '"+typeArg+"', '"+ levelArg+"', '"+siteUser+"')\n")
    else :
        sys.stderr.write("FAILED:\nputFile('"+pathArg+"', '"+ \
                          inputDiag+"')\n")
    exit()

# List levels for a file
if opArg == "-v" :
    if myLI == None :
        sys.stderr.write("AppFileInstaller can't implement option -v.\n")
        exit()
    result = myLI.locFileLevels(pathArg, typeArg, siteUser)
    if result==None :
        sys.stderr.write("FAILED:\locFileLevels('"+pathArg+"', '"+\
                         typeArg+"', '"+ levelArg+"', '"+siteUser+"')\n")
    elif isinstance(result, list) :
        print str(result)+"\n"
    exit()

if opArg == "-a" :
    if myLI == None :
        sys.stderr.write( "AppFileInstaller can't unit test Accumulator.\n")
        exit()
    output = myLI.getLocFile(pathArg, typeArg, levelArg, siteUser)
    if output==None :
        sys.stderr.write("FAILED:\ngetLocFile('"+pathArg+"', '"+\
                         typeArg+"', '"+ levelArg+"', '"+siteUser+"')\n")
        exit()
    exec output
    oneAcc = Accumulator()
    structure = oneAcc.returnStructures()[0][1]
    print json.dumps(structure, indent=4, sort_keys=True)+"\n"
    exit()

# If multiple localization levels, run each independently. Otherwise run
# one command with an empty localization level.
if len(levelList) == 0 :
    levelList = [ "" ]
for oneLevel in levelList :
    levelArg = oneLevel

    # Depending on the name modifier(s) supplied, put the one most applicable
    # into the siteUser variable.
    adaptLevelToArg = False
    siteUser = ""
    if siteArg!="" and siteUser=="" :
        if adaptLevelToArg and levelArg=="" and hostArg==userArg :
            levelArg = "Site"
        if levelArg=="" or levelArg=="Site" or levelArg=="Configured" :
            siteUser = siteArg
    if userArg!="" and siteUser=="" :
        if adaptLevelToArg and levelArg=="" and hostArg==siteArg :
            levelArg = "User"
        if levelArg=="" or levelArg=="User" :
            siteUser = userArg
    if hostArg!="" and siteUser=="" :
        if adaptLevelToArg and levelArg=="" and userArg==siteArg :
            levelArg = "Workstation"
        if levelArg=="" or levelArg=="Workstation" :
            siteUser = userArg

    if byAppFileInstaller :
        myAFI.setType(typeArg)
        myAFI.setLevel(levelArg)
        myAFI.setName(siteUser)

    # Read file contents
    if opArg == "-r" :
        if myLI :
            output = myLI.getLocFile(pathArg, typeArg, levelArg, siteUser)
        else :
            output = myAFI.getFile(pathArg)
        if output!=None :
            sys.stdout.write(output+"\n")
        elif myLI:
            sys.stderr.write("FAILED:\ngetLocFile('"+pathArg+"', '"+\
                             typeArg+"', '"+ levelArg+"', '"+siteUser+"')\n")
        else :
            sys.stderr.write("FAILED:\ngetFile('"+pathArg+"')\n")
        continue

    # Read file data
    if opArg == "-d" or opArg == "-D":
        if myLI == None :
            sys.stderr.write("AppFileInstaller can't implement option -d.\n")
            exit()
        overrideImp = opArg == "-D"
        result = myLI.getLocData(pathArg, typeArg, levelArg, siteUser, \
                                 True, overrideImp)
        if result==None :
            sys.stderr.write("FAILED:\ngetLocFile('"+pathArg+"', '"+\
                             typeArg+"', '"+ levelArg+"', '"+siteUser+"')\n")
        elif isinstance(result, str) or isinstance(result, unicode) :
            print result+"\n"
        else :
            print json.dumps(result, indent=4, sort_keys=True)+"\n"
        continue

    # List files
    if opArg == "-l" :
        if myLI :
            result = myLI.listLocFiles(pathArg, typeArg, levelArg, siteUser, \
                                       prefix, suffix)
        else :
            if prefix!="" or suffix!="" :
                sys.stderr.write( \
                  "AppFileInstaller can't implement name filtering\n")
            result = myAFI.getList(pathArg)
        if isinstance(result, list) :
            print json.dumps(result, indent=4)+"\n"
        elif result!=None :
            pass
        elif myLI :
            sys.stderr.write("FAILED:\nlistLocFiles('"+pathArg+"', '"+\
                             typeArg+"', '"+ levelArg+"', '"+siteUser+"')\n")
        else :
            sys.stderr.write("FAILED:\ngetList('"+pathArg+"')\n")
        continue

    # Delete file
    if opArg == "-x" :
        if myLI :
            output = myLI.rmLocFile(pathArg, typeArg, levelArg, siteUser)
        else :
            myAFI.setMyContextName(os.environ["USER"])
            output = myAFI.rmFile(pathArg)
        if output :
            sys.stdout.write("Success\n")
        elif myLI:
            sys.stderr.write("FAILED:\nrmLocFile('"+ \
                pathArg+"', '"+typeArg+"', '"+ levelArg+"', '"+siteUser+"')\n")
        else :
            sys.stderr.write("FAILED:\nrmFile('"+pathArg+"'')\n")
        continue

    sys.stderr.write("Unknown operation type '"+opArg+"'\n")
    exit()

#
