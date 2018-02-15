'''
  Replacement for PythonOverrider.py. This method allows for a site
  to be specified when importing localization files. This allows for a
  site to import python modules for others sites which is needed for 
  Service Backup. 

''' 

import os.path
import sys

JEP_AVAILABLE = sys.modules.has_key('jep')

if JEP_AVAILABLE:
    from HazardServicesPathManager import HazardServicesPathManager
else:
    SETUP_FILE = '/awips2/fxa/bin/setup.env'
    THRIFT_HOST = 'localhost'
    THRIFT_PORT = '9581'    
    
    import HazardServicesPythonOverriderPure
    
    if os.path.isfile(SETUP_FILE):
        import subprocess
        
        format = 'source {0}; echo ${1}'
        test_host = subprocess.check_output(format.format(SETUP_FILE, "DEFAULT_HOST"), shell=True).strip()
        if test_host:
            THRIFT_HOST = test_host
        test_port = subprocess.check_output(format.format(SETUP_FILE, "DEFAULT_PORT"), shell=True).strip()
        if test_port:
            THRIFT_PORT = test_port

import HazardServicesPythonOverriderCore

def importModule(name, loctype='COMMON_STATIC', level=None, localizedSite=None, 
                localizationUser=None):
    """
    Takes a name (filename and localization path) and the localization type and finds the 
    file and overrides it, and returns the module
    
    Args:
            name : the name and path of the file in localization
            loctype : a string representation of the localization type
            level : a string representation of the localization level (BASE, SITE, etc.)
            localizedSite: the site that localization information should be
                retrieved for (if applicable)
            localizationUser: the user that localization information should
                be retrieved for (if applicable)
    
    Returns:
            a module that has all the correct methods after being overridden
    """
    if not JEP_AVAILABLE:
        if localizationHost is None:
            localizationHost = THRIFT_HOST
        
        if localizationPort is None:
            localizationPort = THRIFT_PORT

        return HazardServicesPythonOverriderPure.importModule(name, localizationHost, localizationPort,
                localizedSite, localizationUser, loctype, level)

    pathManager = HazardServicesPathManager()
    tieredFiles = pathManager.getTieredLocalizationFileForSite(loctype, name, localizedSite)
    availableLevels = pathManager.getAvailableLevels()
    levels = HazardServicesPythonOverriderCore._buildLocalizationLevelsList(availableLevels, level)

    lfiles = []
    for _level in levels :
        if _level in tieredFiles:
            lfiles.append(tieredFiles[_level].getPath())
    themodule = HazardServicesPythonOverriderCore._internalOverride(lfiles)
    return themodule