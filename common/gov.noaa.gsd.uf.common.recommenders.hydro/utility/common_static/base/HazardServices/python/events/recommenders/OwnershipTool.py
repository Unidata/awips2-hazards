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
       
        self.logger = logging.getLogger('OwnershipTool')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'OwnershipTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        
        self.probUtils = ProbUtils()
        
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

    def getOwners(self, ownerToExclude = None):
       
        owners = ["awips", "Greg.Stumpf", "Kevin.Manross", "Yujun.Guo",]
        
        if ownerToExclude:
            newOwners = []
            for owner in owners:
                if owner not in ownerToExclude:
                # string contains another string
                    newOwners.append(owner)
            return newOwners
        else:
            return owners
        
    def getWorkstations(self):
       
        workstations = ["Snow", "Max", "Zoidberg", "Farnsworth",]
        ewpWorkStations = ["ewp"+str(i+1)+".hwt.nssl" for i in range(14)]
        
        return ewpWorkStations + workstations

    def defineDialog(self, eventSet):
        """
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @return: MegaWidget dialog definition to solicit user input before running tool
        """        
        dialogDict = {"title": "Ownership Tool"}

        eventSetAttrs = eventSet.getAttributes()
        trigger = eventSetAttrs.get('trigger')
        print "OT: definedialog trigger --", trigger
        userName = eventSetAttrs.get('userName')
        workStation = eventSetAttrs.get('workStation')
        caveUser = userName + ":" + workStation
        print "OT: cave User is ", caveUser

        labelFieldDict = {}
        labelFieldDict["fieldName"] = "labelName"
        labelFieldDict["fieldType"] = "Label"
        
        if trigger == 'hazardEventModification':
            for event in eventSet: # There will only be one
                newOwner = event.get('ownerChangeRequest', None)
                print "OT: newOwner", newOwner, type(newOwner)
                if not newOwner:
                    continue
                
                forceTakenFlag = False
                if "=>" in newOwner:
                    # force owner format: currentOwner=> newOwner
                    [currentOwner, newOwner] = newOwner.split("=>")
                    forceTakenFlag = True
                else:
                    currentOwner = event.get('owner', caveUser) # in case currentOwner is not set..
                
                if self.probUtils.isEqualOwner(caveUser, currentOwner):
                    if not self.probUtils.isEqualOwner(caveUser, newOwner):
                        # put up dialog for caveUser:
                        if forceTakenFlag:
                            str = newOwner + " has taken the ownership of event " + event.getEventID()+"!"                        
                        else:
                            str = "Do you want "+ newOwner + " to take over event " + event.getEventID()+"?"
                    else:
                        # caveUser is already the newOwner, do nothing
                        continue
                else:
                    if self.probUtils.isEqualOwner(caveUser, newOwner):
                        # put up dialog for caveUser:
                        if forceTakenFlag:
                            str = "You are being forced to take ownership of event " + event.getEventID()+"!"                        
                        else:                        
                            str = "Do you want to take over event " + event.getEventID()+"?"
                    else:
                        # caveUser is not the newOwner either
                        continue
                                                         
                labelFieldDict["label"] = str 
                valueDict = {}
                
                dialogDict["fields"] = [ labelFieldDict ]
                dialogDict["valueDict"] = valueDict
                if forceTakenFlag:
                    dialogDict["buttons"] = [ { "identifier": "ok", "label": "Ok", "default": True, "close": True, }, ]
                else:
                    dialogDict["buttons"] = [ { "identifier": "accept", "label": "Accept", "default": True }, { "identifier": "decline", "label": "Decline", "close": True } ]
                return dialogDict
                    
        else:  # trigger is None
            # call function to return the total of selected events
            self.totalSelectedEvents, self.selectedEvent = self.getSelectedEventAndTotal(eventSet)                 
                     
            if self.totalSelectedEvents != 1:
                valueDict = {}
                moreThanOneMsg = "Cannot run ownership tool on more than one selected event."
                noSelectionMsg = "Cannot run ownership tool without a selected event."
                
                labelFieldDict["label"] = noSelectionMsg if self.totalSelectedEvents==0 else moreThanOneMsg 
                dialogDict["fields"] = labelFieldDict
                dialogDict["valueDict"] = valueDict
                dialogDict["buttons"] = [ { "identifier": "cancel", "label": "OK", "close": True, "cancel": True, "default": True } ]
                return dialogDict  
                                
            # Put up dialog to make ownerChangeRequest
            labelFieldDict["label"] = " Selected Event: " + self.selectedEvent.getEventID()
    
            currentOwner = self.selectedEvent.get("owner", caveUser)
            currentOwnerFieldDict = {}
            currentOwnerFieldDict["fieldName"] = "currentOwner"
            currentOwnerFieldDict["label"] = "Current Owner"
            currentOwnerFieldDict["fieldType"] = "Text"
            currentOwnerFieldDict["editable"] = False            
            currentOwnerFieldDict["values"] = currentOwner
            #currentOwnerFieldDict["values"] = selectedEvent.getWorkStation() + ':' + selectedEvent.getUserName()
                
            # the owner list may not want to include currentOwner
            print "Current owner is ", currentOwner 
            owners = self.getOwners()#currentOwner)
            print "Owner list ", owners
            
            newOwnerFieldDict1 = {}
            newOwnerFieldDict1["fieldName"] = "newOwnerName"
            newOwnerFieldDict1["label"] = "User name:"
            newOwnerFieldDict1["fieldType"] = "ComboBox"
            newOwnerFieldDict1["choices"] = owners
            newOwnerFieldDict1["values"] = owners[0]
            
            workstations = self.getWorkstations()
            newOwnerFieldDict2 = {}
            newOwnerFieldDict2["fieldName"] = "newOwnerWorkstation"
            newOwnerFieldDict2["label"] = "Workstation:"
            newOwnerFieldDict2["fieldType"] = "ComboBox"
            newOwnerFieldDict2["choices"] = workstations  
            newOwnerFieldDict2["values"] = workstations[0]          
                        
            newOwnerFieldDict = {}
            newOwnerFieldDict["fieldName"] = "newOwner"
            newOwnerFieldDict["label"] = "Please select new owner:"
            newOwnerFieldDict["fieldType"] = "Composite"
            newOwnerFieldDict['numColumns'] = 2
            newOwnerFieldDict["fields"] = [newOwnerFieldDict1, newOwnerFieldDict2]             
                        
            # ADD Checkbox for "Force Owner"
            forceOwnerFieldDict = {}
            forceOwnerFieldDict['fieldName'] = "forceOwnerCheck"
            forceOwnerFieldDict['label'] = ""
            forceOwnerFieldDict['fieldType'] = "CheckBoxes"
            forceOwnerFieldDict['choices'] = [
                                              {
                                               'identifier': "forceOwner",
                                               'displayString': "Force Owner",
                                               },
                                              ]
            #forceOwnerFieldDict['values'] = "forceOwner"
        
            valueDict = {
                         'newOwnerName':owners[0],
                         'newOwnerWorkstation':workstations[0],
                         'forceOwnerCheck':[],
                         }
            #valueDict = {"newOwner": ["a"]}
            fieldDicts = [labelFieldDict, currentOwnerFieldDict, newOwnerFieldDict, forceOwnerFieldDict]
            dialogDict["fields"] = fieldDicts
            dialogDict["valueDict"] = valueDict
            dialogDict["buttons"] = [ { "identifier": "request", "label": "Request", "default": True }, { "identifier": "cancel", "label": "Cancel", "close": True, "cancel": True } ]
        
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
        eventSetAttrs = eventSet.getAttributes()
        trigger = eventSetAttrs.get('trigger')
        print "OT: Execute Event trigger ", trigger
        print "dialogInputMap ", dialogInputMap

        userName = eventSetAttrs.get('userName')
        workStation = eventSetAttrs.get('workStation') 
        # for automated event, username / workstation may not be present
        self.caveUser = self.probUtils.getCaveUser(userName, workStation)
        print "caveUser is ", self.caveUser
        
        resultEventSet = EventSetFactory.createEventSet(None)
        if not dialogInputMap:
            return resultEventSet
                
        if trigger == 'hazardEventModification':
            print "Trigger is hazardEventModification"
            for event in eventSet:      
                print "Event..", event       
                ownerChangeRequest = event.get('ownerChangeRequest', None)
                if ownerChangeRequest:
                    if dialogInputMap.get("__dismissChoice__") == "accept":  
                        event.set('owner', ownerChangeRequest)
                        self.probUtils.setActivation(event, self.caveUser)
                    # following code is executed for all choices: "accept", "decline", and "ok"
                    event.set('ownerChangeRequest', None)
                    resultEventSet.add(event)
                    resultEventSet.addAttribute(SAVE_TO_DATABASE_KEY, True)   
        else: # trigger is None
            if not self.selectedEvent or self.totalSelectedEvents > 1:
                return resultEventSet
#             newOwner = dialogInputMap.get("newOwner", None)
            newOwnerName = dialogInputMap.get("newOwnerName", None)
            newOwnerWorkstation = dialogInputMap.get("newOwnerWorkstation", None)
            if not newOwnerName or not newOwnerWorkstation:
                return resultEventSet 
            newOwner = newOwnerName + ":" + newOwnerWorkstation

            forceOwner = dialogInputMap.get("forceOwnerCheck", None)
            print "Trigger is none "
            print "newOwner is ", newOwner
            print "forceOwner is ", forceOwner
            currentOwner = self.selectedEvent.get("owner", None)
            
            if forceOwner:
                self.selectedEvent.set('owner',newOwner)
                self.probUtils.setActivation(self.selectedEvent, self.caveUser)                
                self.selectedEvent.set("ownerChangeRequest",currentOwner+"=>"+newOwner)
            else:
                self.selectedEvent.set('ownerChangeRequest', newOwner)
            resultEventSet.add(self.selectedEvent)   
            resultEventSet.addAttribute(SAVE_TO_DATABASE_KEY, True)                           
        return resultEventSet      

    def getSelectedEventAndTotal(self, eventSet):
        
        totalSelectedEvents = 0
        selectedEvent = None
        for event in eventSet:
            eventID = event.getEventID()
            selected = event.get('selected', False)
            if selected:
                totalSelectedEvents  = totalSelectedEvents + 1
                selectedEvent = event
        return totalSelectedEvents, selectedEvent        

    def toString(self):
        return "OwnershipTool"
