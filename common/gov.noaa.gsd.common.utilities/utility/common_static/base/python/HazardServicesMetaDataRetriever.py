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
    """
    @param javaHazardEvent: Hazard event from Java.  
    @param javaMetaDict: Dictionary holding environmental parameters, if any.
    @return: JSON-encoded dictionary holding nothing if there is no metadata, or else a
             list of megawidget specifier dictionaries under the key METADATA_KEY, the
             relative path to the localized file in which any scripts are to be found
             under the key METADATA_FILE_PATH_KEY, and optionally, a dictionary under
             the key EVENT_MODIFIERS_KEY pairing button megawidget identifiers with the
             names of functions. Each of the latter must be defined in the file held by
             METADATA_FILE_PATH_KEY file as taking a Python hazard event and returning
             either None (if the event was not modified), or a Python hazard event (if
             the function changed the event).
             
    """
    hazardEvent = JUtil.javaObjToPyVal(javaHazardEvent)
    metaDict = JUtil.javaObjToPyVal(javaMetaDict)
    metaObject, filePath = HazardMetaDataAccessor.getMetaData(HAZARD_METADATA,
                                                    hazardEvent.getPhenomenon(),
                                                    hazardEvent.getSignificance(),
                                                    hazardEvent.getSubType())
    if hasattr(metaObject, "execute") and callable(getattr(metaObject, "execute")):
        metaData = metaObject.execute(hazardEvent, metaDict)
        metaData[METADATA_FILE_PATH_KEY] = filePath
        metaData = json.dumps(metaData)
        return metaData
    else:
        return '{"' + METADATA_KEY + '": [] }'
