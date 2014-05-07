"""
    Description: Interface for retrieving class-based meta data
        
"""
import json
from HazardConstants import *
import HazardMetaDataAccessor
import JUtil
from HazardEventHandler import javaHazardEventToPyHazardEvent
JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)

from GeometryHandler import shapelyToJTS, jtsToShapely
JUtil.registerPythonToJava(shapelyToJTS)
JUtil.registerJavaToPython(jtsToShapely)
    
def getMetaData(javaHazardEvent, javaMetaDict):
    hazardEvent = JUtil.javaObjToPyVal(javaHazardEvent)
    metaDict = JUtil.javaObjToPyVal(javaMetaDict)
    metaObject = HazardMetaDataAccessor.getMetaData(HAZARD_METADATA,
                                                    hazardEvent.getPhenomenon(),
                                                    hazardEvent.getSignificance(),
                                                    hazardEvent.getSubtype())
    if hasattr(metaObject, "execute") and callable(getattr(metaObject, "execute")):
        metaData = metaObject.execute(hazardEvent, metaDict)
        metaData = json.dumps(metaData)
        return metaData
    else:
        return '{"' + METADATA_KEY + '": [] }'
