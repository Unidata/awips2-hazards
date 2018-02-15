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

from com.raytheon.uf.viz.hazards.sessionmanager.config.impl import MetaDataAndHazardEvent

def getMetaData(javaHazardEvent, javaMetaDict):
    """
    @param javaHazardEvent: Hazard event from Java.  
    @param javaMetaDict: Dictionary holding environmental parameters, if any.
    @return: JSON-encoded dictionary holding nothing if there is no metadata, or else a
             list of megawidget specifier dictionaries under the key METADATA_KEY, and
             the relative path to the localized file in which any scripts are to be found
             under the key METADATA_FILE_PATH_KEY.
             
    """
    hazardEvent = JUtil.javaObjToPyVal(javaHazardEvent)
    metaDict = JUtil.javaObjToPyVal(javaMetaDict)
    site = metaDict.get("site", None)
    metaObject, filePath = HazardMetaDataAccessor.getHazardMetaData(HAZARD_METADATA,
                                                    hazardEvent.getPhenomenon(),
                                                    hazardEvent.getSignificance(),
                                                    hazardEvent.getSubType(),
                                                    site)
    if hasattr(metaObject, "execute") and callable(getattr(metaObject, "execute")):
        metaData = metaObject.execute(hazardEvent, metaDict)
        metaData[METADATA_FILE_PATH_KEY] = filePath

        # If there is a HazardEvent object in the dictionary because
        # the generation process modified it, then it must be removed
        # temporarily while JSONing the dictionary, as HazardEvent
        # objects are not JSONable. Then it is re-added to the
        # dictionary before being returned.
        if METADATA_MODIFIED_HAZARD_EVENT in metaData:
            modifiedHazardEvent = JUtil.pyValToJavaObj(metaData[METADATA_MODIFIED_HAZARD_EVENT])
            del metaData[METADATA_MODIFIED_HAZARD_EVENT]
        else:
            modifiedHazardEvent = None
        return MetaDataAndHazardEvent(json.dumps(metaData), modifiedHazardEvent)
    else:
        return MetaDataAndHazardEvent('{"' + METADATA_KEY + '": [] }', None)

def validate(javaHazardEvents):
    """
    @param javaHazardEvents: Hazard events from Java. 
    @return: Either None if there are no validation problems, or a string describing
    the problem if there is one.
    """
    errorMsgs = []
    # Fetch the metadata object that may be used for validation.
    for javaEvent in javaHazardEvents:
        hazardEvent = JUtil.javaObjToPyVal(javaEvent)
        metaObject, filePath = HazardMetaDataAccessor.getHazardMetaData(HAZARD_METADATA,
                                                        hazardEvent.getPhenomenon(),
                                                        hazardEvent.getSignificance(),
                                                        hazardEvent.getSubType(),
                                                        None)

        # Ensure there is a validate() method, and call it, returning the result.
        if hasattr(metaObject, "validate") and callable(getattr(metaObject, "validate")):
            errorMsg = metaObject.validate(hazardEvent)
            if errorMsg:
                errorMsgs.append(errorMsg)
    if errorMsgs:
        return "\n\n".join(errorMsgs)
    return None
