"""
    Description: Interface for retrieving class-based meta data
        
"""

import sys, types
from HazardServicesConfig import HazardServicesConfig
from HazardConstants import *

from PythonOverrider import importModule
    
def getMetaData(datatype, phenomenon, significance, subType = None) :
    """
    @param datatype: data type representing hazard metadata.  
    @param phenomenon: hazard phenomenon
    @param significance:  hazard significance
    @param subType: optional hazard subType
    @return: A tuple of two values, the first being an object (executable
             or not) holding the metadata (or None if there is none), and
             the second being a relative path to the localized file from
             which the metadata was sourced (or None if there is none).
    """
    if phenomenon is not None and significance is not None:
        if subType == "": 
            subType = None
        hazardServicesConfig = HazardServicesConfig(datatype)
        searchCriteria = {'filter':{'name':'HazardMetaData'}}
        hazardMetaDataList = hazardServicesConfig.getConfigData(searchCriteria) or []
        
        for metaDataDict in hazardMetaDataList:
            hazardTypesList = metaDataDict[HAZARD_TYPES_DATA] or []
            
            for hazardType in hazardTypesList:
                if phenomenon in hazardType and significance in hazardType:
                    if subType is None or (subType is not None and subType in hazardType):
                        metaDataEntry = metaDataDict[CLASS_METADATA]                        
                        if type(metaDataEntry) is types.StringType:
                            locPath = "hazardServices/hazardMetaData/" + metaDataEntry + ".py"
                            result = importModule(locPath) 
                            m = result.MetaData()
                            return m, locPath
                        elif metaDataEntry is None:
                            return [], None
                        else:
                            return metaDataEntry, None
    return None, None

