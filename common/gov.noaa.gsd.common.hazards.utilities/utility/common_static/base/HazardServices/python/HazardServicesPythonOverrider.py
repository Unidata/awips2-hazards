'''
  Replacement for PythonOverrider.py. This method allows for a site
  to be specified when importing localization files. This allows for a
  site to import python modules for others sites which is needed for 
  Service Backup. 
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 Mar 29, 2016    8837    Robert.Blum Initial Creation

''' 
import PythonOverriderCore
from HazardServicesPathManager import HazardServicesPathManager

def importModule(name, loctype='COMMON_STATIC', level=None, localizedSite=None):
    """
    Takes a name (filename and localization path) and the localization type and finds the 
    file and overrides it, and returns the module
    
    Args:
            name : the name and path of the file in localization
            loctype : a string representation of the localization type
            level : a string representation of the localization level (BASE, SITE, etc.)
            localizedSite: the site that localization information should be
                retrieved for (if applicable)
    Returns:
            a module that has all the correct methods after being overridden
    """
    pathManager = HazardServicesPathManager()
    tieredFiles = pathManager.getTieredLocalizationFileForSite(loctype, name, localizedSite)
    availableLevels = pathManager.getAvailableLevels()
    levels = PythonOverriderCore._buildLocalizationLevelsList(availableLevels, level)

    lfiles = []
    for _level in levels :
        if _level in tieredFiles:
            lfiles.append(tieredFiles[_level].getPath())
    themodule = PythonOverriderCore._internalOverride(lfiles)
    return themodule