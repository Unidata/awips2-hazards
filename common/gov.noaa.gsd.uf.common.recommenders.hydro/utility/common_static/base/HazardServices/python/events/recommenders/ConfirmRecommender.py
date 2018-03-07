"""
Confirmation Recommender

@since: March 2018
@author: GSD Hazard Services Team
"""
import datetime
import logging
import RecommenderTemplate
import numpy
import GeometryFactory
import EventFactory
import EventSetFactory
from ProbUtils import ProbUtils
import UFStatusHandler

from MapsDatabaseAccessor import MapsDatabaseAccessor

from HazardConstants import *
from GeneralConstants import *
 
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        """
        Constructs the Ownership Tool Recommender
        @param executiveService: A reference to the Executive Service
        @param errorCB Error callback.
        """
       
        self.logger = logging.getLogger('Confirm Recommender')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'ConfirmRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        
    def defineScriptMetadata(self):
        """
        @return: JSON string containing information about this
                 tool
        """
        metaDict = {}
        metaDict["toolName"] = "ConfirmRecommender"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Show confirmation dialog so that user can interact"
        metaDict["eventState"] = "Pending"
        metaDict["getSpatialInfoNeeded"] = False
        return metaDict

    def defineDialog(self, eventSet):
        """
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @return: MegaWidget dialog definition to solicit user input before running tool
        """        
        dialogDict = {"title": "End Object"}

        eventSetAttrs = eventSet.getAttributes()
        self.selectedEvent = None

        for event in eventSet:
            eventID = event.getEventID()
            selected = event.get('selected', False)
            if selected:
                self.selectedEvent = event
                break

        labelFieldDict = {}
        labelFieldDict["fieldName"] = "labelName"
        labelFieldDict["fieldType"] = "Label"
        
        str = "Are you sure you want to end object " + self.selectedEvent.get("objectID")+"?"
        labelFieldDict["label"] = str 
        valueDict = {}
                
        dialogDict["fields"] = [ labelFieldDict ]
        dialogDict["valueDict"] = valueDict
        dialogDict["buttons"] = [ { "identifier": "no", "label": "No", "default": True, "close": True},
                                 { "identifier": "yes", "label": "Yes", "close": False }, 
                                 ]
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
        
        if not dialogInputMap:
            return False

        eventSetAttrs = eventSet.getAttributes()
        self.currentTime = long(eventSetAttrs.get("currentTime")) 
        
        resultEventSet = EventSetFactory.createEventSet(None)
                            
        if dialogInputMap.get("__dismissChoice__") == "yes":
            print "ConfirmR Setting to ENDED"
            self.selectedEvent.setStatus('ENDED')
            self.selectedEvent.set('statusForHiddenField', 'ENDED')
            self.selectedEvent.setEndTime(datetime.datetime.utcfromtimestamp(self.currentTime / 1000))
            resultEventSet.add(self.selectedEvent)
            resultEventSet.addAttribute(SAVE_TO_DATABASE_KEY, True)

        print "*****\nFinishing ConfirmRecommender"
        return resultEventSet  

    def toString(self):
        return "ConfirmRecommender"
