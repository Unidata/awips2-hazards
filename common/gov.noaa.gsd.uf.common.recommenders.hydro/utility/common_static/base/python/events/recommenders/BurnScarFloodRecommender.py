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
        self.burnScarPolyDict = {"Fourmile":[
                                                [-97.95962463128558,42.60445289481582],
                                                [-98.02838956432441,42.570070428296404],
                                                [-98.03985038649755,42.55860960612327],
                                                [-98.04558079758412,42.51276631743072],
                                                [-98.05704161975726,42.47265343982474],
                                                [-98.05704161975726,42.415349328959046],
                                                [-98.05704161975726,42.386697273526195],
                                                [-98.04558079758412,42.380966862439635],
                                                [-98.03985038649755,42.369506040266494],
                                                [-97.70748654347655,42.47265343982474],
                                                [-97.71321695456311,42.49557508417101],
                                                [-97.74186900999597,42.529957550690426],
                                                [-97.77052106542881,42.53568796177699],
                                                [-97.79344270977509,42.52422713960386],
                                                [-97.85074682064078,42.529957550690426],
                                                [-97.85074682064078,42.547148783950135],
                                                [-97.8450164095542,42.57580083938298],
                                                [-97.85074682064078,42.650296183508374],
                                                [-97.86793805390047,42.684678650027784],
                                                [-97.89659010933332,42.684678650027784],
                                                [-97.94243339802588,42.684678650027784],
                                                [-97.9768158645453,42.67321782785465],
                                                [-97.99973750889157,42.66748741676808],
                                                [-98.0111983310647,42.66175700568151],
                                                [-97.95962463128558,42.60445289481582]],
                               "Waldo Canyon":[[-96.6,40.675], [-96.5,40.675], [-96.5,40.45], [-96.6,40.45], [-96.6,40.675]]

                               }

        
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
        
        burnScarList = ["Fourmile", "Waldo Canyon"]
        burnScarFieldDict["choices"] = burnScarList
        
        fieldDicts = [burnScarFieldDict]
        dialogDict["fields"] = fieldDicts
        
        valueDict = {"burnScarName": "Fourmile"}
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
        burnScarName = dialogDict["burnScarName"]
        
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
        
