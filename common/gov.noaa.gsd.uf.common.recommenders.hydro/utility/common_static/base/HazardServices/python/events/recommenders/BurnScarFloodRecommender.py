"""
Burn Scar Flood Recommender
Initially patterned after the Dam Break Flood Recommender

@since: October 2014
@author: GSD Hazard Services Team
"""
import datetime
import logging
import RecommenderTemplate
import numpy
import GeometryFactory
import EventFactory
import EventSetFactory
from HazardEventLockUtils import HazardEventLockUtils

import UFStatusHandler

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

        self.HAZARD_TYPE_MAP = {
                                "W": "Warning",
                                "A": "Watch"
                                }
        
        self.NO_MAP_DATA_ERROR_MSG = '''No mapdata found for BURN SCAR areas.
See the alertviz log for more information.
(Please verify your maps database contains the table mapdata.{})

Please click CANCEL and manually draw an inundation area. 
 '''.format(BURNSCARAREA_TABLE)

        self.LOCKED_HAZARD_RESULTS_OUTPUT = '''The {} already has a {} Event: {} that is {} and locked.
Unable to make new recommendations at this time.'''

        self.HAZARD_EXISTS_RESULTS_OUTPUT = '''The {} already has a {} Event: {} that is issued.
Unable to make new recommendations at this time.'''
        
        self.logger = logging.getLogger('BurnScarFloodRecommender')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'BurnScarFloodRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        self.hazardEventLockUtils = None
        
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
        metaDict["getSpatialInfoNeeded"] = False
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
        
        self.burnScarPolyDict = {}
        try:
            mapsAccessor = MapsDatabaseAccessor()
            self.burnScarPolyDict = mapsAccessor.getPolygonDict(BURNSCARAREA_TABLE, eventSet.getAttribute("localizedSiteID"))
        except:
            self.logger.exception("Could not retrieve burn scar impact areas.")

        if not self.burnScarPolyDict:
            burnScarFieldDict["values"] = self.NO_MAP_DATA_ERROR_MSG
            burnScarFieldDict["fieldType"] = "Label"
            valueDict = {"burnScarName": None}
            dialogDict["fields"] = burnScarFieldDict
            dialogDict["valueDict"] = valueDict
            return dialogDict

        # We want the "dropDownLabel" attribute in the metadata to be what is
        # used to choose the burn scar in the recommender dialog, but default back
        # to the name in the shape file if that is not available.
        self.labelToImpact = {}
        burnScarList = sorted(self.burnScarPolyDict.keys())
        try :
            burnScarMeta = mapsAccessor.getAllBurnScarMetadata()
            ddBurnScarList = []
            for scar in burnScarList :
                try :
                    dropDown = burnScarMeta[scar]["dropDownLabel"]
                except :
                    dropDown = scar
                ddBurnScarList.append(dropDown)
                self.labelToImpact[dropDown] = scar
            burnScarList = ddBurnScarList
        except :
            self.logger.exception("Could not retrieve burn scar metadata.")

        burnScarFieldDict["choices"] = burnScarList

        urgencyFieldDict = {}
        urgencyFieldDict["fieldName"] = "urgencyLevel"
        urgencyFieldDict["label"] = "Please Select Level of Urgency"
        urgencyFieldDict["fieldType"] = "RadioButtons"
        urgencyFieldDict["choices"] = ["WARNING", "WATCH"]

        dialogDict["fields"] = [burnScarFieldDict, urgencyFieldDict]
        
        if len(burnScarList):
            burnScarName =  burnScarList[0]
        else:
            burnScarName = None
        dialogDict["valueDict"] = {"burnScarName": burnScarName, "urgencyLevel":"WARNING"}
        return dialogDict
    
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        """
        @eventSet: List of objects that was converted from Java IEvent objects
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() method.
        @param visualFeatures: Visual features as defined by the defineSpatialInfo()
        method and modified by the user to provide spatial input; ignored.
        @return: List of objects that will be later converted to Java IEvent
        objects
        """
        urgencyLevel = dialogInputMap["urgencyLevel"]

        sessionAttributes = eventSet.getAttributes()
        burnScarName = dialogInputMap.get("burnScarName")
        burnScarName = self.labelToImpact.get(burnScarName, burnScarName)

        # Text to be displayed in the Results dialog
        resultsText = "Recommender ran for {}:\n".format(burnScarName)

        caveMode = sessionAttributes.get('runMode','PRACTICE').upper()
        practice = True
        if caveMode == 'OPERATIONAL':
            practice = False

        if self.hazardEventLockUtils is None:
            self.hazardEventLockUtils = HazardEventLockUtils(practice)

        lockedHazardIds = self.hazardEventLockUtils.getLockedEvents()

        newEventSet = EventSetFactory.createEventSet()
        hazardEvent = None
        for currentEvent in eventSet:
            if currentEvent.getHazardAttributes().get("burnScarName") == burnScarName:
                locked = currentEvent.getEventID() in lockedHazardIds
                currentHazardType = self.HAZARD_TYPE_MAP.get(currentEvent.getSignificance())
                currentEventID = currentEvent.getDisplayEventID()
                status = currentEvent.getStatus()
                if status == "ISSUED":
                    if ("WARNING" in urgencyLevel and currentEvent.getSignificance() != "W") or \
                       ("WATCH" in urgencyLevel and currentEvent.getSignificance() != "A"):
                        if not locked:
                            currentEvent.setStatus('ending')
                            newEventSet.add(currentEvent)
                            break
                        else:
                            # Current Watch or Warning is Locked, don't recommend anything
                            # Update the resultsText to indicate this
                            resultsText += self.LOCKED_HAZARD_RESULTS_OUTPUT.format(burnScarName, currentHazardType, currentEventID, status.lower())
                            newEventSet.addAttribute(RESULTS_MESSAGE_KEY, resultsText)
                            return newEventSet
                    else:
                        # Current event and urgency level match, return empty eventSet
                        # Update the resultsText to indicate this
                        resultsText += self.HAZARD_EXISTS_RESULTS_OUTPUT.format(burnScarName, currentHazardType, currentEventID)
                        newEventSet.addAttribute(RESULTS_MESSAGE_KEY, resultsText)
                        return newEventSet
                elif status not in ['ENDED', 'ELAPSED']:
                    if not locked:
                        # Current Event is not issue, update it.
                        hazardEvent = currentEvent
                    else:
                        # Current Event is locked do not create a new event
                        # Update the resultsText to indicate this
                        resultsText += self.LOCKED_HAZARD_RESULTS_OUTPUT.format(burnScarName, currentHazardType, currentEventID, status.lower())
                        newEventSet.addAttribute(RESULTS_MESSAGE_KEY, resultsText)
                        return newEventSet

        eventIsNew = False
        if hazardEvent is None:
            hazardEvent = EventFactory.createEvent(practice)
            eventIsNew = True

        hazardEvent = self.updateEventAttributes(hazardEvent, eventSet.getAttributes(), \
                                      dialogInputMap, visualFeatures, eventIsNew)

        newEventSet.add(hazardEvent)
        resultsText += self.createResultsStringFromEventSet(newEventSet)
        newEventSet.addAttribute(RESULTS_MESSAGE_KEY, resultsText)
        return newEventSet

    def toString(self):
        return "BurnScarFloodRecommender"
    
    def updateEventAttributes(self, hazardEvent, sessionDict, dialogDict, visualFeatures, eventIsNew):
        """
        Creates the hazard event, based on user dialog input and session dict information. 
        
        @param hazardEvent: An empty hazard event to fill with hazard information.
                            This is injectable so that test versions of this object
                            can be used.
        @param sessionDict: A dict of Hazard Services session information
        @param dialogDict: A dict of Hazard Services dialog information  
        @param visualFeatures: List of visual features holding spatial input information
        @param eventIsNew: Flag indicating whether or not the event has just been created.
        
        """
        burnScarName = dialogDict.get("burnScarName")
        if not burnScarName:
            return None
        burnScarName = self.labelToImpact.get(burnScarName, burnScarName)
        hazardGeometry =  self.getFloodPolygonForBurnScar(burnScarName)

        if hazardGeometry is None:
            return None

        significance = "A"
        subType = None

        urgencyLevel = dialogDict["urgencyLevel"]

        if "WARNING" in urgencyLevel:
            significance = "W"
            subType = "BurnScar"

        currentTime = long(sessionDict["currentTime"])
        startTime = currentTime
        endTime = startTime + self.DEFAULT_FFW_DURATION_IN_MS

        if eventIsNew:
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

        hazardEvent.addHazardAttributes({"cause":"Burn Scar Flooding",
                                          "burnScarName":burnScarName
                                         })
        return hazardEvent
    
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
        
    def createResultsStringFromEventSet(self, eventSet):
        resultsString = ''
        for event in eventSet:
            status = event.getStatus()
            hazardType = self.HAZARD_TYPE_MAP.get(event.getSignificance())
            eventID = event.getDisplayEventID()
            if status == "PENDING":
                resultsString += "{} Event: {} has been created or updated.\n".format(hazardType, eventID)
            elif status == "PROPOSED":
                resultsString += "{} {} Event: {} has been updated.\n".format(status.capitalize(), hazardType, eventID)
            elif status == "ENDING":
                resultsString += "{} Event: {} has been set to {}.\n".format(hazardType, eventID, status.capitalize())
            else:
                self.logger.exception("Burn Scar Recommender produced invalid event.")
        return resultsString
