import copy
import json
import os
import sys
from HazardServicesConfig import HazardServicesConfig

pythonPath = None

class HazardMetaData :
    
    @staticmethod   
    def getMetaData(datatype, phenomena, significance, subtype = None) :
        """
        Static Method! There is no "self" reference passed in. This is
        a selfless method.
        
        @param datatype: data type representing hazard
                         metadata.  Should be "hazardMetaData:
        @param phenomena: hazard phenomena
        @param significance:  hazard significance
        @param subtype: optional hazard subtype
        @return: A dict containing the hazard metadata
                 for this phen sig or None.
        """
        if phenomena is not None and significance is not None:
            if subtype == "": subtype = None
            hazardServicesConfig = HazardServicesConfig(datatype)
            searchCriteria = {'filter':{'name':'HazardMetaData'}}
            hazardMetaDataList = hazardServicesConfig.getConfigData(searchCriteria) or []
            
            for metaDataDict in hazardMetaDataList:
                hazardTypesList = metaDataDict['hazardTypes'] or []
                
                for hazardType in hazardTypesList:
                    if phenomena in hazardType and significance in hazardType:
                        if subtype is None or (subtype is not None and subtype in hazardType):
                            return metaDataDict['metaData']

        return None
    
