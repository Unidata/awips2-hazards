"""
    Description: Interface for retrieving class-based meta data
        
"""

import sys, types, os, string
from HazardServicesConfig import HazardServicesConfig
from HazardConstants import *

from PythonOverrider import importModule
from PathManager import PathManager

def getHazardMetaData(datatype, phenomenon, significance, subType = None) :
    """
    @param datatype: data type representing hazard metadata.  
    @param phenomenon: hazard phenomenon
    @param significance:  optional hazard significance
    @param subType: optional hazard subType
    @return: A tuple of two values, the first being an object (executable
             or not) holding the metadata (or None if there is none), and
             the second being a relative path to the localized file from
             which the metadata was sourced (or None if there is none).
    """
    if phenomenon is not None:
        if subType == "": 
            subType = None
        hazardServicesConfig = HazardServicesConfig(datatype)
        searchCriteria = {'filter':{'name':'HazardMetaData'}}
        hazardMetaDataList = hazardServicesConfig.getConfigData(searchCriteria) or []
        
        for metaDataDict in hazardMetaDataList:
            hazardTypesList = metaDataDict[HAZARD_TYPES_DATA] or []
            
            for hazardType in hazardTypesList:
                if phenomenon in hazardType and (significance is None or significance in hazardType):
                    if subType is None or (subType is not None and subType in hazardType):
                        metaDataEntry = metaDataDict[CLASS_METADATA]
                        if type(metaDataEntry) is types.StringType:
                            locPath = "hazardServices/hazardMetaData/" + metaDataEntry + ".py"
                            result = importMetaData(metaDataEntry)
                            m = result.MetaData()
                            return m, locPath
                        elif metaDataEntry is None:
                            return [], None
                        else:
                            return metaDataEntry, None
    return None, None

def getMetaData(fileName) :
    """
    @param fileName -- name of metaData file in the hazardServices/hazardMetaData directory
           The file must contain a class MetaData with an execute method
    @return: A class object with an execute method for obtaining the metadata
             OR None.
    """
    result = importMetaData(fileName)
    return result.MetaData()

def importMetaData(moduleName):
    locPath = 'hazardServices/hazardMetaData/'
    # Use the base class to get the BASE file path
    scriptName = 'CommonMetaData.py'
    fileName = locPath + scriptName

    pathMgr = PathManager()
    filePath = pathMgr.getLocalizationFile(fileName, loctype='COMMON_STATIC', loclevel='BASE').getFile().name;
    basePath = filePath.replace(scriptName, '')

    # Import all the modules in the formats BASE directory using PythonOverrider.
    for s in basePath.split(os.path.pathsep):
        if os.path.exists(s):
            scriptfiles = os.listdir(s)
            for filename in scriptfiles:
                split = string.split(filename, ".")
                if len(split) == 2 and len(split[0]) > 0 and split[1] == "py":
                    if sys.modules.has_key(split[0]):
                        clearModuleAttributes(split[0])
                    tmpModule = importModule(locPath + filename)

    # Reload the desired metadata module again since above import order
    # is random which may cause subclasses to have old references to superclasses.
    return importModule(locPath + moduleName + '.py')

def clearModuleAttributes(moduleName):
    if sys.modules.has_key(moduleName):
        mod = sys.modules[moduleName]
        modGlobalsToRemove = [k for k in mod.__dict__ if not k.startswith('_')]
        for k in modGlobalsToRemove:
            mod.__dict__.pop(k)