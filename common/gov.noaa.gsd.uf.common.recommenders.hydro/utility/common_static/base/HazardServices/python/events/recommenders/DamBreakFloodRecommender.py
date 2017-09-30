"""
Dam Break Flood Recommender

@since: June 2012
@author: GSD Hazard Services Team
"""
import datetime
import logging
import RecommenderTemplate
import numpy
import GeometryFactory
import HazardDataAccess

import UFStatusHandler

from MapsDatabaseAccessor import MapsDatabaseAccessor

from HazardConstants import *
from GeneralConstants import *
from EventSet import EventSet
 
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        self.DEFAULT_FFW_DURATION_IN_MS = 10800000
        
        self.NO_MAP_DATA_ERROR_MSG = '''No mapdata found for DAM BREAK inundation.
See the alertviz log for more information.
(Please verify your maps database contains the table mapdata.{})

Please click CANCEL and manually draw an inundation area. 
 '''.format(DAMINUNDATION_TABLE)
        
        self.logger = logging.getLogger('DamBreakFloodRecommender')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'DamBreakFloodRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)

        
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
        metaDict["getSpatialInfoNeeded"] = False
        return metaDict

    def defineDialog(self, eventSet):
        """
        @return: MegaWidget dialog definition to solicit user input before running tool
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        """        
        dialogDict = {"title": "Dam/Levee Break Flood Recommender"}
        
        damFieldDict = {}
        damFieldDict["fieldName"] = "damOrLeveeName"
        damFieldDict["label"] = "Please Select a Dam or Levee"
        damFieldDict["fieldType"] = "ComboBox"
        damFieldDict["autocomplete"] = True
        
        self.damPolygonDict = {}
        try:
            self.mapsAccessor = MapsDatabaseAccessor()
            self.damPolygonDict = self.mapsAccessor.getPolygonDict(DAMINUNDATION_TABLE, eventSet.getAttribute("localizedSiteID"))
        except:
            self.logger.exception("Could not retrieve dam inundation data.")

        if not self.damPolygonDict:
            damFieldDict["values"] = self.NO_MAP_DATA_ERROR_MSG
            damFieldDict["fieldType"] = "Label"
            valueDict = {"damOrLeveeName": None}
            dialogDict["fields"] = damFieldDict
            dialogDict["valueDict"] = valueDict
            return dialogDict

            
        damOrLeveeNameList = sorted(self.damPolygonDict.keys())
        damFieldDict["choices"] = damOrLeveeNameList
        
        urgencyFieldDict = {}
        urgencyFieldDict["fieldName"] = "urgencyLevel"
        urgencyFieldDict["label"] = "Please Select Level of Urgency"
        urgencyFieldDict["fieldType"] = "RadioButtons"
        
        urgencyList = ["WARNING (Structure has Failed / Structure Failure Imminent)",\
                       "WATCH (Potential Structure Failure)"]
        urgencyFieldDict["choices"] = urgencyList
        
        fieldDicts = [damFieldDict, urgencyFieldDict]
        dialogDict["fields"] = fieldDicts
        
        if len(damOrLeveeNameList):
            damOrLeveeName =  damOrLeveeNameList[0]
        else:
            damOrLeveeName = None
        valueDict = {"damOrLeveeName": damOrLeveeName, "urgencyLevel":urgencyList[0]}
        dialogDict["valueDict"] = valueDict
        
        return dialogDict
    
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        """
        @eventSet: List of objects that was converted from Java IEvent objects
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() routine
        @param visualFeatures: Visual features as defined by the defineSpatialInfo()
        method and modified by the user to provide spatial input; ignored.
        @return: List of objects that will be later converted to Java IEvent
        objects
        """
        # for a unit test these will fail as we will not have JEP available, so
        # we import them in here
        import EventFactory
        import EventSetFactory

        urgencyLevel = dialogInputMap["urgencyLevel"]

        currentEvents = HazardDataAccess.getCurrentEvents(eventSet)
        damOrLeveeName = dialogInputMap.get("damOrLeveeName")
        
        newEventSet = EventSetFactory.createEventSet()
        hazardEvent = None
        for currentEvent in currentEvents:
            if currentEvent.getHazardAttributes().get("damOrLeveeName") == damOrLeveeName:
                if currentEvent.getStatus() == "ISSUED":
                    if ("WARNING" in urgencyLevel and currentEvent.getSignificance() != "W") or \
                       ("WATCH" in urgencyLevel and currentEvent.getSignificance() != "A"):
                        currentEvent.setStatus('ending')
                        newEventSet.add(currentEvent)
                        break
                    else:
                        # current Event and urgency level match
                        # return empty eventSet
                        return newEventSet
                else:
                    # Current Event is not issue, update it.
                    hazardEvent = currentEvent

        if hazardEvent is None:
            hazardEvent = EventFactory.createEvent()

        # updateEventAttributes does all the stuff we can safely do in a unit
        # test, basically whatever does not require Jep. It is up to the test
        # to inject a pure python version of the hazard event.
        hazardEvent = self.updateEventAttributes(hazardEvent, eventSet.getAttributes(), \
                                      dialogInputMap)

        newEventSet.add(hazardEvent)
        return newEventSet

    def toString(self):
        return "DamBreakFloodRecommender"
    
    def updateEventAttributes(self, hazardEvent, sessionDict, dialogDict):
        """
        Creates the hazard event, based on user dialog input and session dict information. 
        
        @param hazardEvent: An empty hazard event to fill with hazard information.
                            This is injectable so that test versions of this object
                            can be used.
        @param sessionDict: A dict of Hazard Services session information
        @param dialogDict: A dict of Hazard Services dialog information  
        
        @return: A hazard event representing a dam break flash flood watch or warning 
        """
        
        damOrLeveeName = dialogDict.get("damOrLeveeName")
        if not damOrLeveeName:
            return None
        
        hazardGeometry =  self.getFloodPolygonForDam(damOrLeveeName)

        if hazardGeometry is None:
            return None

        riverName = self.getRiverNameForDam(damOrLeveeName)

        significance = "A"
        subType = None

        urgencyLevel = dialogDict["urgencyLevel"]
        
        if "WARNING" in urgencyLevel:
            significance = "W"
            subType = "NonConvective"
            
        currentTime = long(sessionDict["currentTime"])
        startTime = currentTime
        endTime = startTime + self.DEFAULT_FFW_DURATION_IN_MS
        
        if hazardEvent.getEventID() == None:
            hazardEvent.setEventID("")
            hazardEvent.setHazardStatus("PENDING")
            
        hazardEvent.setSiteID(str(sessionDict["siteID"]))
        hazardEvent.setPhenomenon("FF")
        hazardEvent.setSignificance(significance)
        hazardEvent.setSubType(subType)

        # New recommender framework requires some datetime objects, which must
        # be in units of seconds.
        hazardEvent.setCreationTime(datetime.datetime.utcfromtimestamp(\
                                   currentTime / MILLIS_PER_SECOND))
        hazardEvent.setStartTime(datetime.datetime.utcfromtimestamp(\
                                   startTime / MILLIS_PER_SECOND))
        hazardEvent.setEndTime(datetime.datetime.utcfromtimestamp(\
                                   endTime / MILLIS_PER_SECOND))
        hazardEvent.setGeometry(GeometryFactory.createCollection([hazardGeometry]))

        hazardEvent.setHazardAttributes({"cause":"Dam Failure",
                                          "damOrLeveeName":damOrLeveeName,
                                          "riverName": riverName,
                                         })
        return hazardEvent
        
    
    def getFloodPolygonForDam(self, damOrLeveeName):
        """
        Returns a user-defined flood hazard polygon for 
        a dam. The base version of this tool does nothing. It is up to the implementer
        to override this method.
        
        @param  damOrLeveeName: The name of the dam or levee for which to retrieve a 
                         hazard polygon
                           
        @return Geometry: A Shapely geometry representing
                          the flood hazard polygon
        """
        if damOrLeveeName in self.damPolygonDict:
            return GeometryFactory.createPolygon(self.damPolygonDict[damOrLeveeName])
        else:
            return None    
        
    def getRiverNameForDam(self, damOrLeveeName):
        """
        Returns the River that the Dam or Levee is located on.
        """
        damMetaData = self.mapsAccessor.getDamInundationMetadata(damOrLeveeName)
        if damMetaData:
            return damMetaData.get("riverName", None)
        return None
