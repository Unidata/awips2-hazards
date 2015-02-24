"""
Dam Break Flood Recommender

@since: June 2012
@author: GSD Hazard Services Team
"""
import datetime
import RecommenderTemplate
import numpy
import GeometryFactory

from MapsDatabaseAccessor import MapsDatabaseAccessor

from HazardConstants import *
from GeneralConstants import *
 
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        """
        Constructs the Dam Break Flood Recommender
        Note that the focal point will have to provide a
        damPolygonDict for the dams in his service area. Or, 
        the focal point can override the 
        getFloodPolygonForDam method.
        @param executiveService: A reference to the Executive Service
        @param errorCB Error callback.
        """
        self.DEFAULT_FFW_DURATION_IN_MS = 10800000

        
    def defineScriptMetadata(self):
        """
        @return: JSON string containing information about this
                 tool
        """
        metaDict = {}
        metaDict["toolName"] = "DamBreakFloodRecommender"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Calculates inundation areas based on dams."
        metaDict["eventState"] = "Pending"
        return metaDict

    def defineDialog(self, eventSet):
        """
        @return: MegaWidget dialog definition to solicit user input before running tool
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        """        
        dialogDict = {"title": "Dam/Levee Break Flood Recommender"}
        
        damFieldDict = {}
        damFieldDict["fieldName"] = "damName"
        damFieldDict["label"] = "Please Select a Dam or Levee"
        damFieldDict["fieldType"] = "ComboBox"
        damFieldDict["autocomplete"] = True
        
        ############################################################
        # Note, need a way to pop up dialog if cannot connect to db table
        ############################################################
        mapsAccessor = MapsDatabaseAccessor()
        self.damPolygonDict = mapsAccessor.getPolygonDict(DAMINUNDATION_TABLE)
            
        damList = sorted(self.damPolygonDict.keys())
        damFieldDict["choices"] = damList
        
        urgencyFieldDict = {}
        urgencyFieldDict["fieldName"] = "urgencyLevel"
        urgencyFieldDict["label"] = "Please Select Level of Urgency"
        urgencyFieldDict["fieldType"] = "RadioButtons"
        
        urgencyList = ["WARNING (Structure has Failed / Structure Failure Imminent)",\
                       "WATCH (Potential Structure Failure)"]
        urgencyFieldDict["choices"] = urgencyList
        
        fieldDicts = [damFieldDict, urgencyFieldDict]
        dialogDict["fields"] = fieldDicts
        
        valueDict = {"damName": damList[0], "urgencyLevel":urgencyList[0]}
        dialogDict["valueDict"] = valueDict
        
        return dialogDict
    
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        """
        @eventSet: List of objects that was converted from Java IEvent objects
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() routine
        @param spatialInputMap: A map containing spatial input as created by the 
        definedSpatialInfo() routine
        @return: List of objects that will be later converted to Java IEvent
        objects
        """
        # for a unit test these will fail as we will not have JEP available, so
        # we import them in here
        import EventFactory
        import EventSetFactory

        # updateEventAttributes does all the stuff we can safely do in a unit
        # test, basically whatever does not require Jep. It is up to the test
        # to inject a pure python version of the hazard event.
        hazardEvent = EventFactory.createEvent()

        hazardEvent = self.updateEventAttributes(hazardEvent, eventSet.getAttributes(), \
                                      dialogInputMap, spatialInputMap)

        return EventSetFactory.createEventSet(hazardEvent)

    def toString(self):
        return "DamBreakFloodRecommender"
    
    def updateEventAttributes(self, hazardEvent, sessionDict, dialogDict, spatialDict):
        """
        Creates the hazard event, based on user dialog input and session dict information. 
        
        @param hazardEvent: An empty hazard event to fill with hazard information.
                            This is injectable so that test versions of this object
                            can be used.
        @param sessionDict: A dict of Hazard Services session information
        @param dialogDict: A dict of Hazard Services dialog information  
        @param spatialDict: A dict of Hazard Services spatial input information
        
        @return: A hazard event representing a dam break flash flood watch or warning 
        """
        damName = dialogDict.get("damName")
        
        hazardGeometry =  self.getFloodPolygonForDam(damName)

        if hazardGeometry is None:
            return None

        significance = "A"
        subType = None

        urgencyLevel = dialogDict["urgencyLevel"]
        
        if "WARNING" in urgencyLevel:
            significance = "W"
            subType = "NonConvective"
            
        currentTime = long(sessionDict["currentTime"])
        startTime = currentTime
        endTime = startTime + self.DEFAULT_FFW_DURATION_IN_MS
        
        hazardEvent.setEventID("")
        hazardEvent.setSiteID(str(sessionDict["siteID"]))
        hazardEvent.setHazardStatus("PENDING")
        hazardEvent.setHazardMode("O")
        hazardEvent.setPhenomenon("FF")
        hazardEvent.setSignificance(significance)
        hazardEvent.setSubType(subType)

        # New recommender framework requires some datetime objects, which must
        # be in units of seconds.
        hazardEvent.setCreationTime(datetime.datetime.fromtimestamp(\
                                   currentTime / MILLIS_PER_SECOND))
        hazardEvent.setStartTime(datetime.datetime.fromtimestamp(\
                                   startTime / MILLIS_PER_SECOND))
        hazardEvent.setEndTime(datetime.datetime.fromtimestamp(\
                                   endTime / MILLIS_PER_SECOND))
        hazardEvent.setGeometry(GeometryFactory.createCollection([hazardGeometry]))

        hazardEvent.setHazardAttributes({"cause":"Dam Failure",
                                          "damName":damName
                                         })
        return hazardEvent
        
    
    def getFloodPolygonForDam(self, damName):
        """
        Returns a user-defined flood hazard polygon for 
        a dam. The base version of this tool does nothing. It is up to the implementer
        to override this method.
        
        @param  damName: The name of the dam for which to retrieve a 
                         hazard polygon
                           
        @return Geometry: A Shapely geometry representing
                          the flood hazard polygon
        """
        if damName in self.damPolygonDict:
            return GeometryFactory.createPolygon(self.damPolygonDict[damName])
        else:
            return None    
        
