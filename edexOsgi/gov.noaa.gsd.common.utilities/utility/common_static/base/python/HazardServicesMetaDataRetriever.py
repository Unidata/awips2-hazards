"""
    Description: Interface for retrieving class-based meta data
        
"""
import json
from HazardConstants import *
import HazardMetaDataAccessor
    
def getMetaData(phenomenon, significance, subType = None):
    metaObject = HazardMetaDataAccessor.getMetaData(HAZARD_METADATA, phenomenon, significance, subType)
    if hasattr(metaObject, "execute") and callable(getattr(metaObject, "execute")):
        metaData = metaObject.execute()
        metaData = json.dumps(metaData)
        return metaData
    else:
        return '[]'
