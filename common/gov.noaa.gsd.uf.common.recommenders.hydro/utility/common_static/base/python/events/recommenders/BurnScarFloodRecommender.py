"""
Burn Scar Flood Recommender
Initially patterned after the Dam Break Flood Recommender

@since: October 2014
@author: GSD Hazard Services Team
"""
import datetime
import RecommenderTemplate
import numpy
import GeometryFactory
import EventFactory
import EventSetFactory

from MapsDatabaseAccessor import MapsDatabaseAccessor

from HazardConstants import *
from GeneralConstants import *
 
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        """
        Constructs the Burn Scar Flood Recommender
        Note that the focal point will have to provide a
        burnScarPolyDict for the burn scars in his service area. Or, 
        the focal point can override the 
        getFloodPolygonForBurnScar method.
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
        metaDict["toolName"] = "BurnScarFloodRecommender"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Calculates inundation areas based on burn scars."
        metaDict["eventState"] = "Pending"
        return metaDict

    def defineDialog(self, eventSet):
        """
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @return: MegaWidget dialog definition to solicit user input before running tool
        """        
        dialogDict = {"title": "Burn Scar Flood Recommender"}
        
        burnScarFieldDict = {}
        burnScarFieldDict["fieldName"] = "burnScarName"
        burnScarFieldDict["label"] = "Please Select a Burn Scar"
        burnScarFieldDict["fieldType"] = "ComboBox"
        burnScarFieldDict["autocomplete"] = True
        
        mapsAccessor = MapsDatabaseAccessor()
        try:
            self.burnScarPolyDict = mapsAccessor.getPolygonDict(BURNSCARAREA_TABLE)
        except:
            burnScarFieldDict["label"] = '''No shapefiles found for BURN SCAR areas.  
Please click CANCEL and manually draw an inundation area. 
(Please verify your maps database for mapdata. '''+ BURNSCARAREA_TABLE + ''')'''
            burnScarFieldDict["fieldType"] = "Label"
            valueDict = {"burnScarName": None}
            dialogDict["fields"] = burnScarFieldDict
            dialogDict["valueDict"] = valueDict
            return dialogDict
            
        
        burnScarList = sorted(self.burnScarPolyDict.keys())
        burnScarFieldDict["choices"] = burnScarList
        
        fieldDicts = [burnScarFieldDict]
        dialogDict["fields"] = fieldDicts
        
        if len(burnScarList):
            burnScarName =  burnScarList[0]
        else:
            burnScarName = None
        valueDict = {"burnScarName": burnScarName}
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

        hazardEvent = EventFactory.createEvent()
        pythonEventSet = EventSetFactory.createEventSet(hazardEvent)

        for thisEvent in pythonEventSet:
            self.updateEventAttributes(thisEvent, eventSet.getAttributes(), \
                                      dialogInputMap, spatialInputMap)

        return pythonEventSet

    def toString(self):
        return "BurnScarFloodRecommender"
    
    def updateEventAttributes(self, hazardEvent, sessionDict, dialogDict, spatialDict):
        """
        Creates the hazard event, based on user dialog input and session dict information. 
        
        @param hazardEvent: An empty hazard event to fill with hazard information.
                            This is injectable so that test versions of this object
                            can be used.
        @param sessionDict: A dict of Hazard Services session information
        @param dialogDict: A dict of Hazard Services dialog information  
        @param spatialDict: A dict of Hazard Services spatial input information
        
        """
        burnScarName = dialogDict.get("burnScarName")
        if not burnScarName:
            return None
        
        hazardGeometry =  self.getFloodPolygonForBurnScar(burnScarName)

        if hazardGeometry is None:
            return None

        significance = "W"
        subType = "BurnScar"

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

        hazardEvent.setHazardAttributes({"cause":"Burn Scar Flooding",
                                          "burnScarName":burnScarName
                                         })
        
    
    def getFloodPolygonForBurnScar(self, burnScarName):
        """
        Returns a user-defined flood hazard polygon for 
        a burn scar. The base version of this tool does nothing. It is up to the implementer
        to override this method.
        
        @param  burnScarName: The name of the burnScar for which to retrieve a 
                         hazard polygon
                           
        @return Geometry: A Shapely geometry representing
                          the flood hazard polygon
        """
        if burnScarName in self.burnScarPolyDict:
            return GeometryFactory.createPolygon(self.burnScarPolyDict[burnScarName])
        else:
            return None    
        
