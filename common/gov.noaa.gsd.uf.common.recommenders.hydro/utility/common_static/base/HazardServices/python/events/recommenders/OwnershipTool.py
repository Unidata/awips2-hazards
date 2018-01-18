"""
Ownership Tool Recommender
Initially patterned after the Burn Scar Recommender

@since: January 2018
@author: GSD Hazard Services Team
"""
import datetime
import logging
import RecommenderTemplate
import numpy
import GeometryFactory
import EventFactory
import EventSetFactory

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
       
        self.logger = logging.getLogger('OwnershipTool')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'OwnershipTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)

        
    def defineScriptMetadata(self):
        """
        @return: JSON string containing information about this
                 tool
        """
        metaDict = {}
        metaDict["toolName"] = "OwnershipTool"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "change the ownership of selected hazard events."
        metaDict["eventState"] = "Pending"
        metaDict["getSpatialInfoNeeded"] = False
        return metaDict

    def defineDialog(self, eventSet):
        """
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @return: MegaWidget dialog definition to solicit user input before running tool
        """        
        dialogDict = {"title": "Ownership Tool"}
        
        print "Event Set in DefineDialog is ", eventSet
        
        eventSetAttrs = eventSet.getAttributes()
        trigger = eventSetAttrs.get('trigger')
        print "OT: definedialog trigger --", trigger

        self.MORE_THAN_ONE_ERROR_MSG = "Cannot run ownership tool on more than one event"
        self.NO_SELECTION_ERROR_MSG = "Cannot run ownership tool without selected event"
        self.NEW_OWNER_MSG = "Do you want to confirm the ownership change?"
                
        labelFieldDict = {}
        labelFieldDict["fieldName"] = "labelName"
        labelFieldDict["label"] = "Cannot run ownership tool on more than one event"
        labelFieldDict["fieldType"] = "Label"
        
        # function to return the total of selected events
        selectedEvent = None
        totalSelectedEvents = 0
        for event in eventSet:
            eventID = event.getEventID()
            print "Event ID ", eventID
            selected = event.get('selected', False)
            if selected:
                totalSelectedEvents  = totalSelectedEvents + 1
                selectedEvent = event
           
        if trigger == "none":
            valueDict = {}
            print "total of selected events in definedDialog ", totalSelectedEvents
            if totalSelectedEvents != 1:
                labelFieldDict["label"] = self.NO_SELECTION_ERROR_MSG if totalSelectedEvents==0 else self.MORE_THAN_ONE_ERROR_MSG 
                dialogDict["fields"] = labelFieldDict
                dialogDict["valueDict"] = valueDict
                return dialogDict
            else:
                currentOwnerFieldDict = {}
                currentOwnerFieldDict["fieldName"] = "currentOwner"
                currentOwnerFieldDict["label"] = "Current Owner"
                currentOwnerFieldDict["fieldType"] = "Text"
                currentOwnerFieldDict["editable"] = False
                currentOwnerFieldDict["values"] = selectedEvent.getWorkStation() + ':' + selectedEvent.getUserName()
                
                # function to return list of owners
                lstOwner = ['A','B','C']
                newOwnerFieldDict = {}
                newOwnerFieldDict["fieldName"] = "newOwner"
                newOwnerFieldDict["label"] = "Please select new owner:"
                newOwnerFieldDict["fieldType"] = "ComboBox"
                newOwnerFieldDict["choices"] = lstOwner
                newOwnerFieldDict["values"] =  None
        
                valueDict = {"newOwner": None}
                
                fieldDicts = [currentOwnerFieldDict, newOwnerFieldDict]
                dialogDict["fields"] = fieldDicts
                dialogDict["valueDict"] = valueDict
        
                return dialogDict
        
        if trigger == "hazardEventModification":
            valueDict = {}
            if selectedEvent:
                currentOwner = selectedEvent.getUserName()
                newOwner = selectedEvent.get('ownerChangeRequest', "")
                if newOwner != "" and newOwner != currentOwner:
                    # do nothing if there is no change
                    # or else prompt dialog for user to accept/decline
                    labelFieldDict["label"] = self.NEW_OWNER_MSG 
                    dialogDict["fields"] = labelFieldDict
                    dialogDict["valueDict"] = valueDict
                    return dialogDict
        return {}                
    
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
        print "OT: EventSet is ", eventSet

        eventSetAttrs = eventSet.getAttributes()
        trigger = eventSetAttrs.get('trigger')
        print "OT: Event trigger ", trigger
#         selectedEventIDs = eventSetAttrs.get("selectedEventIDs", [])
#         print "In Ownership tool "
#         print "selectedEvent ID is ", selectedEventIDs

        totalSelectedEvents = 0
        selectedEvent = None
        for event in eventSet:
            eventID = event.getEventID()
            print "Event ID ", eventID
            selected = event.get('selected', False)
            if selected:
                totalSelectedEvents  = totalSelectedEvents + 1
                selectedEvent = event

        print "OT: Total of selected events is ", totalSelectedEvents
        resultEventSet = EventSetFactory.createEventSet(None)
        self.setOriginDict = {}
        resultEventSet.addAttribute(SET_ORIGIN_KEY, self.setOriginDict)
        self.saveToDatabase = True

        if selectedEvent:
            if trigger == "none":
                if totalSelectedEvents == 1:
                    selectedEvent.addHazardAttribute("ownerChangeRequest", "newowner")
                    self.saveToDatabase = True
                    resultEventSet.add(selectedEvent)
            elif trigger == "hazardEventModification":
                currentOwner = selectedEvent.getUserName()
                newOwner = selectedEvent.get('ownerChangeRequest', "")
                if newOwner != "" and newOwner != currentOwner:
                    # depending on the user interaction, accept or reset
                    selectedEvent.addHazardAttribute("userName", newOwner)
                    self.saveToDatabase = True 
                    resultEventSet.add(selectedEvent)                
                    
            if self.saveToDatabase:
                resultEventSet.addAttribute(SAVE_TO_DATABASE_KEY, True)

        print "Finishing ownershiptool"
        return resultEventSet                

    def toString(self):
        return "OwnershipTool"
       
        
