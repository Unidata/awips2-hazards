import copy, traceback
import json
import os
import sys, types
from HazardServicesConfig import HazardServicesConfig

pythonPath = None

class HazardMetaData :
    
    @staticmethod   
    def getMetaData(datatype, phenomena, significance, subtype = None) :
        """
        Static Method! There is no "self" reference passed in. This is
        a selfless method.
        
        @param datatype: data type representing hazard
                         metadata.  Should be "hazardMetaData"
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
                            metaDataEntry = metaDataDict['classMetaData'] #['metaData']
                            
                            if type(metaDataEntry) is types.StringType:
                                from PythonOverriderPure import importModule
                                from LocalizationInterface import LocalizationInterface
                                li = LocalizationInterface("") 
                                locPath = "hazardServices/hazardMetaData/" + metaDataEntry + ".py"
                                try :
                                    result = importModule(locPath, li.getEdexHost(), \
                                         li.getEdexPort(), li.getDefLoc(), li.getDefUser() )
                                    m = result.MetaData()
                                    return m
                                except :
                                    traceback.print_exc()
                                    self.assertEqual("PARSE of "+locPath,"OK")
                                    return
                            elif metaDataEntry is None:
                                return []
                            else:
                                return metaDataEntry

        return None
    
