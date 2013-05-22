"""
 Contains the current state of the Hazard Services application 
 in the Spatial and Temporal Displays as well as the selected events,
 the selected time or time range, the current time, 
 and the current static and dynamic views. 
 
Python is used for this module to work efficiently with 
non-homogeneous data structures. 
 @since: March 2012
 @author: GSD Hazard Services Team 
"""

# To run with new bridge:
from Bridge import Bridge

# To run with old bridge:
#from bridge.Bridge import Bridge

import os, copy, sys, json, types, traceback
import logging, UFStatusHandler
from  sessionManager.ToolHandler import ToolHandler
from sessionManager.PresenterHelper import PresenterHelper
from HazardConstants import *
   
#try:
#    from com.raytheon.uf.viz.core.localization import LocalizationManager #@UnresolvedImport
#    from com.raytheon.viz.core.mode import CAVEMode #@UnresolvedImport
#except:
#    pass

# Put this in when debugging python code called via JEP.

try:
    from jep import * #@unresolvedImport 
except:
    tbData = traceback.format_exc()
    print tbData

def getProxy():
    """
    @return: A proxy object which can be called from Java. This is
             accomplished using JEP.
    """
    p = SessionManager()
    jp = jep.jproxy(p,['gov.noaa.gsd.viz.hazards.display.IHazardServicesModel']) #@UndefinedVariable
    print "SessionManager getProxy: Instantiating SessionManager JP: ", type(jp)
    return jp

class SessionManager(object):
    def __init__(self):
        self.frameCount = 0
        self.frameIndex = 0
        self.frameTimeList = []
        self.hazardEventManager = None
    
    ## Session State   
    def initialize(self, selectedTime, currentTime, staticSettingsID, dynamicSettings_json, 
                   caveMode, siteID, sessionState):
        """
        Initializes the SessionManager.
        
        @param selectedTime: Sets the selected time (in milliseconds)
        @param currentTime: Sets the current time (in milliseconds)
        @param staticSettingsID: Sets the initial settings, e.g., "Canned TOR", "TOR", "Canned WSW", "WSW"
                       "Canned Flood", "Flood".  This can be the "displayName" OR the staticSettingsID.
        @param dynamicSettings_json: Dynamic settings
        @param sessionState: saved session state to initialize from previous session
        """
        
        try:
            from jep import *
        except:
            pass
        
        #import sys
        #sys.path.append()
        
        # Uncomment these lines when debugging 
        #import pydevd    #@UnresolvedImport   
        #pydevd.settrace(suspend=False)
        self.logger = logging.getLogger("SessionManager")
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            "gov.noaa.gsd.viz.hazards.sessionmanager", "SessionManager", level=logging.INFO))
        self.logger.setLevel(logging.INFO)                
        
        self.logger.info("Session Manager initialize" + str(os.environ['PYTHONPATH'].split(os.pathsep)))

        self.bridge = Bridge()
        if self.hazardEventManager is not None:
            self.bridge.setHazardEventManager(self.hazardEventManager)
        self.toolHandler = ToolHandler(self.bridge, self)
        self.presenterHelper = PresenterHelper(self.bridge, self)        

        sessionState = json.loads(sessionState)

        self.selectedTime = selectedTime
        self.selectedTimeRange = None
        self.setAddToSelected(sessionState.get("addToSelected", "off"))
        self.currentTime = currentTime
        self.config = self.getConfig()
        self.hazardVisibility = "on"
                
        self.staticSettingsID = staticSettingsID
        staticSettings = json.loads(self.getStaticSettings(staticSettingsID))
        self.staticSettings = staticSettings
        if dynamicSettings_json != "":
            dynamicSettings = json.loads(dynamicSettings_json)
            self.dynamicSettings = dynamicSettings
        else:
            self.dynamicSettings = staticSettings
        
        self.eventDicts = self.getHazards(self.dynamicSettings)

        events = []
        self.leftoverEvents = []
        for eventState in ["pendingEvents", "potentialEvents", "leftoverEvents"]:
            events += sessionState.get(eventState, [])
        if events: 
            events, leftovers = self.filterEventsForDynamicSettings(events)
            self.eventDicts += events
            self.leftoverEvents = leftovers
                          
        self.selectedEventIDs = sessionState.get("selectedEventIDs", [])
        self.lastSelectedEventID = sessionState.get("lastSelectedEventID", "")

        self.colorTable = json.loads(self.bridge.getColorTable('json').read()) 
        self.earliestVisibleTime = sessionState.get("earliestVisibleTime", "0")
        self.latestVisibleTime = sessionState.get("latestVisibleTime", "0")
                 
        # This assumes that the HazardTypes, HazardCategories, and Hazard MetaData are not changing during a session
        self.hazardTypes = json.loads(self.bridge.getData('{"dataType":"hazardTypes"}'))  
        self.hazardCategories = json.loads(self.bridge.getData('{"dataType":"hazardCategories"}'))
        criteria={"dataType":"hazardMetaData", "filter":{NAME:"HazardMetaData"}}        
        self.hazardMetaData = json.loads(self.bridge.getData(json.dumps(criteria))) 
        self.productGeneratorTable = json.loads(self.bridge.getData('{"dataType":"productGeneratorTable"}'))
        
        self.caveMode = caveMode # "Operational"
        self.siteID = siteID # "OAX"
        self.backupSiteID = self.siteID
        self.wfoSiteID = self.siteID
               
    def getState(self, saveEvents):
        '''
        Returns the current session state
        This can be used for saving the state for a future session
        The session state includes information such as the selectedTime and selectedEvents
        @param saveEvents -- if True, save the current potential, pending, and leftover events
        @return session state
        '''
        state = {}
        if saveEvents:
            state["potentialEvents"] = self.findEventDicts(STATE, POTENTIAL)
            state["pendingEvents"] = self.findEventDicts(STATE, PENDING)
            state["leftoverEvents"] = self.leftoverEvents
        state["addToSelected"] = self.getAddToSelected()
        state["selectedEventIDs"] = self.selectedEventIDs
        state["lastSelectedEventID"] = self.lastSelectedEventID
        state["earliestVisibleTime"] = self.earliestVisibleTime
        state["latestVisibleTime"] = self.latestVisibleTime        
        return json.dumps(state)

    ## Selected Events
    def getSelectedEvents(self):
        """
        @return: The eventIDs of the selected events
        """
        return json.dumps(self.selectedEventIDs)

    def updateSelectedEvents(self, eventIDsJSON, 
                             multipleSelection, originator):
        """
        @param eventIDsJSON: A JSON list containing the ids of the
                             selected events
        @param multipleSelection: Flag indicating whether or not
                                  this is a multiple selection action
        @param originator:    Where this event came from, e.g. "Spatial" or
                              "Temporal"
        @return updateTime -- IF the current selected time does not overlap the
                             first selected event, 
                                 return the new selected time
                              otherwise, return "None"
        """
        eventIDs = json.loads(eventIDsJSON)
        if type(eventIDs) is not types.ListType:
            eventIDs = [eventIDs]
            
        returnVal = "None"
                                    
        if (self.addToSelected or multipleSelection) and originator != "Temporal":
            if not eventIDs: #Do nothing
                return returnVal
            self.lastSelectedEventID = eventIDs[-1]
            self.selectedEventIDs += eventIDs
            self.selectedEventIDs = self.removeDuplicates(self.selectedEventIDs)
        else:
            self.selectedEventIDs = eventIDs
            if not eventIDs:
                return returnVal
            self.lastSelectedEventID = eventIDs[-1]
        first = True 
        for eventID in eventIDs:
            eventDict = self.findSessionEventDicts([eventID])[0]
            # Set the selected time to allow viewing of first new selected event
            if first:
                returnVal = self.changeSelectedTime(eventDict)
                first = False
            # If this event is a potential event, 
            # change the state to pending 
            if eventDict.get(STATE) == POTENTIAL:
                eventDict[STATE] = PENDING                                        
        return returnVal
    
    def setAddToSelected(self, onOff):
        '''
        Turn on / off the AddToSelected Mode
        If on, then new selected events are added to the set
        If off, then new selected events replace the current set
        
        @param onOff --"on" or "off"
        '''
        if onOff == "on": 
            self.addToSelected = True
        else:
            self.addToSelected = False

    def getAddToSelected(self):
        '''
        @return the current AddToSelected mode as "on" or "off"
        '''
        if self.addToSelected: 
            return "on"
        else:
            return "off"

    def getSelectedEventIDs(self):
        '''
        @return the list of current selected eventIDs
        '''
        return self.selectedEventIDs
    
    def getLastSelectedEventID(self):
        '''
        @return the eventID of the most recent selected event
        '''
        return self.lastSelectedEventID
        
    def getEventValues(self, eventIDs, field, returnType, ignoreState=None):
        '''
        Return the list of values for the given field and given eventIDs
          for example, get the list of "callback" values for a set of events
        @param eventIDs -- list of eventIDs
        @param field -- name of field e.g. "callback"
        @param returnType -- if "json" will return the results in json format
        @param ignoreState -- if set to a state (e.g. POTENTIAL) then skip 
                events that have that state
        @return list of values e.g. names of tools
        '''
        
        if eventIDs == "all":
            eventDicts = self.eventDicts
        else:
            if type(eventIDs) is not types.ListType:
                eventIDs = json.loads(eventIDs)
            eventDicts = self.findSessionEventDicts(eventIDs)
            
        valueList = []
        
        for eventDict in eventDicts:
            state = eventDict.get(STATE)
            
            # Skip eventDicts with the user-specified
            # state to ignore. This is here mainly 
            # because potential events are treated 
            # differently than other events
            if ignoreState is not None and ignoreState == state:
                continue
            
            value = eventDict.get(field, None)
            
            if value and value not in valueList:
                valueList.append(value)
                    
        if returnType == "json":
            valueList = json.dumps(valueList)
            
        return valueList
  
    def checkForEventsWithState(self, eventIDs, searchStates):
        """
        Search the user-specified events for a specific state.
        @param eventIDs: "all" or a list of specific eventIDs
        @param searchState: A single state or list of states to search for
        @return: True if the state was found or False
        """
        if type(searchStates) is not types.ListType:
            searchStates = [searchStates]

        if eventIDs == "all":
            eventDicts = self.eventDicts
        else:
            if type(eventIDs) is not types.ListType:
                eventIDs = json.loads(eventIDs)
            eventDicts = self.findSessionEventDicts(eventIDs)
            
        found = False
        
        for eventDict in eventDicts:
            state = eventDict.get(STATE)
            
            if state in searchStates:
                found = True
                break
            
        return found
    
    def newEvent(self, eventArea):
        """ 
        This method is called when the user draws a new polygon from 
        the Spatial Display. It creates an event for this polygon.
        @param eventArea: The event dict representing the new event, mainly
                          interested in the shapes part of it.
        @return: The eventID of the new event.         
        """
        exec "eventArea = " + eventArea
        eventID = self.bridge.newSeqNum()
        startTime = int(self.selectedTime)
        currentTime = int(self.currentTime)
        endTime = int(startTime) + self.staticSettings.get("defaultDuration", 3600)
        shapes = eventArea.get(SHAPES, [])
        self.addEvent(eventID, shapes, currentTime, startTime, endTime)
        return eventID
    
    def copyEvent(self, eventDict):
        '''
        Return a new event dictionary which is a copy of the given eventDict
        @param eventDict to be copied
        '''
        newDict = copy.deepcopy(eventDict)
        newDict[EVENT_ID] = self.bridge.newSeqNum()
        newDict[PREVIEW_STATE] = ""
        return newDict
    
    def updateEventData(self, updateDicts, source=""):
        """ 
        This method is called when the event data is changed.
        For example, it is called when the user changes the selected time or the event type in the 
             Hazard Information Dialog
        @param updateDicts:  Contains information specific to the update
                             action.  For example, the Console
                             sends something like the following when an
                             event's time range is modified:                             
                             {eventID: 1, startTime: <new start time>,
                             endTime: <new end time>}
        @param source: Continuing with the above example, the source would be
                       "Temporal"      
                             
        """
        self.logger.debug("SM -- updateEventData" + json.dumps(updateDicts))
        if type(updateDicts) is types.StringType:
            updateDicts = json.loads(updateDicts)
        if type(updateDicts) is not types.ListType:
            updateDicts = [updateDicts]
        ignoreKeys = self.getIgnoreKeys(source)
        for updateDict in updateDicts:
            eventID = updateDict.get(EVENT_ID, None)
            if not eventID:
                self.logger.error("Session Manager updateEventData -- no eventID!")
                # Need to crash here to catch this problem

            curDicts = self.findSessionEventDicts([eventID])
            if not curDicts: return
            curDict = curDicts[0]
            
            # Check to see if new event required for a new hazard type
            replacedDict = None
            if self.newEventNeededForHazardType(updateDict, curDict, ignoreKeys):
                # Can the new hazard type "replace" the old?
                if self.replacedBy(curDict, updateDict):
                    curDict[PREVIEW_STATE] = ENDED
                    newDict = self.makeNewCopyOfSelectedEvent(curDict, updateDict)
                    replacedDict = curDict
                    newDict[REPLACES] = curDict.get(HEADLINE)
                else:
                    newDict = self.makeNewCopyOfSelectedEvent(curDict, updateDict)                    
                curDict = newDict
                curDict[STATE] = PENDING
                eventID = curDict.get(EVENT_ID)
                
            # Check to see if new event required for a time change
            if self.newEventNeededForTimeChange(updateDict, curDict, ignoreKeys):
                curDict = self.makeNewCopyOfSelectedEvent(curDict, updateDict)
                eventID = curDict.get(EVENT_ID)
                
            # Update curDict with the updateDict fields
            for key in updateDict:
                if key in ignoreKeys: continue
                # Update all the type information
                if key in [FULLTYPE, HAZARD_TYPE]:
                    if updateDict[key] != None:
                        curDict[key] = updateDict[key]
                        self.setEventType(curDict, changedField=key)
                        if replacedDict:
                            replacedDict[REPLACED_BY] = curDict.get(HEADLINE)
                elif key in [START_TIME, END_TIME]:  
                    curDict[key] = updateDict[key]             
                    if curDict.get("groupID"):
                        gDicts = self.findEventDicts("groupID", curDict.get("groupID"))
                        for gDict in gDicts:
                            gDict[key]=updateDict[key]
                else: 
                    curDict[key] = updateDict[key]
                        
    def modifyEventArea(self, jsonText):
        """
        Modifies an event, replacing a portion or portions
        of it with the event provided in jsonText.
        @param jsonText: A JSON string containing a dict to replace portions
                         of the event with.
                         
                         Example modifyDict:
                         {EVENT_ID:"1",SHAPE_TYPE:"polygon",
                         SHAPES:
                         [{POINTS:[[-103.82006673621783,39.75122509913088],
                         [-103.78,39.61],[-104.05,39.57],[-104.06,39.67],[-103.83,39.74]]
                         INCLUDE: "true",
                         }]
                         }
  
        """
        modifyDict = json.loads(jsonText)
        eventID = modifyDict.get(EVENT_ID)
        eventDict = self.findSessionEventDicts([eventID])[0] 
        
        # Check to see if new event required for area change
        if self.newEventNeededForAreaChange(eventDict):
            eventDict = self.makeNewCopyOfSelectedEvent(eventDict, modifyDict)
            eventID = eventDict.get(EVENT_ID)
        
        shapeType = modifyDict.get(SHAPE_TYPE)
        if shapeType == "polygon":
            # Example modifyDict:
            # {EVENT_ID:"1",SHAPE_TYPE:"polygon",
            #  SHAPES:
            #   [{POINTS:[[-103.82006673621783,39.75122509913088],
            #   [-103.78,39.61],[-104.05,39.57],[-104.06,39.67],[-103.83,39.74]]
            #     INCLUDE: "true",
            #   }]
            # }
            oldShapes = eventDict.get(SHAPES)
            newShapes = []
            # Remove the polygons from the old shapes
            for oldShape in oldShapes:
                if oldShape.get(SHAPE_TYPE) != "polygon":
                    newShapes.append(oldShape)
            # Now add in the modified polygons
            for modShape in modifyDict.get(SHAPES):
                newShapes.append({
                                  SHAPE_TYPE:"polygon",
                                  POINTS: modShape.get(POINTS),
                                  INCLUDE: modShape.get(INCLUDE, "true"),
                                  })
            # Add the modified polygons to the event shapes.
            eventDict[SHAPES] = newShapes
            eventDict["polyModified"] = True  
        elif shapeType == "dot" or shapeType == "star" or shapeType == "circle":
            # modifyDict: 
            #    {"newLonLat":[-103.93386183900962,39.945584913954775],
            #    EVENT_ID:"542",
            #    SHAPE_TYPE:"circle",
            #    "pointID":1297140191000}
            #
            modifyCallbackToolName = eventDict.get("modifyCallbackToolName")
            
            if modifyCallbackToolName is not None:
                modifyInfo = { "eventDict":eventDict, "modifyDict":modifyDict, EVENT_ID:eventID }
                self.toolHandler.runTool(modifyCallbackToolName, runData=json.dumps(modifyInfo))                       
                
    def newEventNeededForTimeChange(self, updateDict, curDict, ignoreKeys):
        """
        Returns True if the updateDict contains a time change AND 
        time change is not allowed for the given hazard type. 
         
        @param updateDict -- dictionary of updated hazard event fields
        @param curDict -- the eventDict for the hazard event to be updated
        @param ignoreKeys -- key fields that will be ignored and not changed 
        """
        #return False
        curType = curDict.get(HAZARD_TYPE)
        # Type has not been set yet
        if not curType or curType == "":
            return False
        # If not yet issued, then the hazard type can change freely
        if curDict.get(STATE) != ISSUED:
            return False

        keys = updateDict.keys()
        curType = curDict.get(HAZARD_TYPE) 
        try: 
            if self.hazardTypes.get(type).get("allowTimeChange"):
                return False
        except:
            # Assume that time change is allowed if not found
            self.logger.debug( "SessionManager -- newEventNeededForTimeChange Warning - hazard type not found" + str(type))
            return False
        # Time change is not allowed
        for changeKey in [START_TIME, END_TIME]:
            if changeKey not in ignoreKeys and changeKey in keys:
                return True
        return False

    def newEventNeededForAreaChange(self, eventDict):
        '''
        Return True if areaChange not allowed for the hazard type of the Hazard Event
        '''
        curType = eventDict.get(HAZARD_TYPE)
        # Type has not been set yet
        if not curType or curType == "":
            return False
        # If not yet issued, then the hazard type can change freely
        if eventDict.get(STATE) != ISSUED:
            return False

        try:
            if not self.hazardTypes.get(curType).get("allowAreaChange"):
                return True
        except:
            self.logger.debug("SessionManager -- newEventNeededForAreaChange Warning - hazard type not found " + str(curType))
            self.logger.debug( "     allowAreaChange " + str(self.hazardTypes.get(curType)))
            # Assume area change is allowed if not found
            return True
        return False
    
    def newEventNeededForHazardType(self, updateDict, curDict, ignoreKeys):
        """
        Returns True if the updateDict contains a hazard type for an issued hazard.
          
        @param updateDict -- dictionary of updated hazard event fields
        @param curDict -- the eventDict for the hazard event to be updated
        @param ignoreKeys -- key fields that will be ignored and not changed 
        """                
        oldType = curDict.get(HAZARD_TYPE)
        # Type has not been set yet
        if not oldType or oldType == "":
            return False
        # If not yet issued, then the hazard type can change freely
        if curDict.get(STATE) != ISSUED:
            return False
        
        # See if the hazard type is being changed
        keys = updateDict.keys()
        for key in keys:
            if key in ignoreKeys:
                continue
            if key in [HAZARD_TYPE, FULLTYPE]:
                return True
        return False
        
        # See if the hazard type change is an upgrade or downgrade
        
        # We retain this code for a future refinement.
        # For long-fused hazards that truly upgrade or downgrade, 
        # it could work to retain the same eventID when upgrading/ downgrading, 
        # but for simplicity, for now, we are always creating a new 
        # eventID when the hazard type is changed.

#        upgradeDict=UpDown.upgradeHazardsDict,
#        downgradeDict=UpDown.downgradeHazardsDict
#        for upDownDict in [upgradeDict, downgradeDict]:
#            if newType in upDownDict:
#                if oldType in upDownDict.get(newType):
#                    return False                
#        return True

    def replacedBy(self, curDict, updateDict):
        '''
        @param Current Hazard Event
        @param Update dictionary containing either a changed HAZARD_TYPE 
               or changed FULLTYPE
        @return True if the current Hazard Type can be "replacedBy" the
                changed Hazard Type
                Otherwise, return False
        '''
        oldType = curDict.get(HAZARD_TYPE)
                
        newType = updateDict.get(HAZARD_TYPE)
        if not newType:
            fullType = updateDict.get(FULLTYPE)
            phen, sig, subType = self.getPhenSigSubType(FULLTYPE, fullType) 
            newType, headline, fullType = self.getHazardTypeInfo(phen, sig, subType)
        if not newType:
            return False
        hazardTypeInfo = self.hazardTypes.get(oldType)
        replacedBy = hazardTypeInfo.get(REPLACED_BY)
        if replacedBy:
            if newType in replacedBy:
                return True  
        return False
            
    def makeNewCopyOfSelectedEvent(self, curDict, updateDict):
        newDict = self.copyEvent(curDict)
        eventID = newDict.get(EVENT_ID)
        updateDict[EVENT_ID] = eventID
        self.selectedEventIDs.append(eventID)
        self.eventDicts.append(newDict)
        self.lastSelectedEventID = eventID
        return newDict
 
    def getHazardsForDynamicSettings(self):
        return self.getHazards(self.dynamicSettings)
                
    def deleteEvent(self, eventIDs):
        """
        Deletes an event.  NOTE: THIS NEEDS TO CALL THE bridge if state is proposed!!
        @param eventID: The id of the event to delete. 
        """
        eventIDs = json.loads(eventIDs)
        newEvents = []
        for eventDict in self.eventDicts:
            if eventDict[EVENT_ID] not in eventIDs:
                newEvents.append(eventDict)
        self.eventDicts = newEvents
        
    def removeEvents(self, field, value):
        """
        Removes events meeting the search criteria.
        @param field: the field to search on
        @param value: the value to search on
        """
        eventDicts = []
        for eventDict in self.eventDicts:
            if eventDict[field] != value:
                eventDicts.append(eventDict)
        self.eventDicts = eventDicts        

    def handleAction(self, action, actionInfo):
        """
        Modifies an event based on a curve, riseAbove or fallBelow action
        @param action: "curve", RISE_ABOVE or FALL_BELOW
        @param actionInfo: dict containing information about the event.
        """
        actionInfo = json.loads(actionInfo)
        if action == "curve":
            eventID = actionInfo.get(EVENT_ID)
            curve = actionInfo.get("value")
            if curve:
                draggedPointsLen = 3
            else:
                draggedPointsLen = 2
            self.updateEventData({EVENT_ID: eventID, "draggedPointsLen":draggedPointsLen})
        elif action == RISE_ABOVE:
            eventID = actionInfo.get(EVENT_ID)
            value = actionInfo.get("value")
            self.updateEventData({EVENT_ID: eventID, RISE_ABOVE: value, START_TIME: value})
        elif action == FALL_BELOW:
            eventID = actionInfo.get(EVENT_ID)
            value = actionInfo.get("value")
            self.updateEventData({EVENT_ID: eventID, FALL_BELOW: value, END_TIME: value})

    def changeState(self, eventIDs_JSON, state):
        """
        Alters the state of events.
        @param eventIDs: The eventIDs of the events
        @param state: The new state
        """
        if type(eventIDs_JSON) == types.StringType:
            eventIDs = json.loads(eventIDs_JSON)
        else:
            eventIDs = eventIDs_JSON
        eventDicts = self.findSessionEventDicts(eventIDs)
        for eventDict in eventDicts:
            # Cannot go backwards once ENDED
            if eventDict.get(STATE) == ENDED:
                if state == ISSUED:
                    # Remove from selectedEvents
                    eventID = eventDict.get(EVENT_ID)
                    try:
                        self.selectedEventIDs.remove(eventID)
                    except:
                        pass
                continue
            if state == PREVIEW_ENDED:
                # We are going to preview the ending of the hazard
                # so the actual state does not change
                eventDict[PREVIEW_STATE] = ENDED
            elif state == ISSUED and eventDict.get(PREVIEW_STATE) == ENDED:
                # We have already previewed the ending of the hazard and are issuing it
                eventDict[STATE] = ENDED
                # Remove from selected
                eventID = eventDict.get(EVENT_ID)
                if eventID in self.selectedEventIDs:
                    self.selectedEventIDs.remove(eventID)
                    if eventID == self.lastSelectedEventID:
                        try:
                            self.lastSelectedEventID = self.selectedEventIDs[-1]
                        except:
                            self.lastSelectedEventID = ""
            else:
                eventDict[STATE]= state
                # Turn off previewEnded, replacedBy, replaces if set previously
                eventDict[PREVIEW_ENDED] = ''  
                eventDict[REPLACED_BY] = ''
                eventDict[REPLACES] = ''
        if state in [ISSUED, PROPOSED, ENDED]:
            self.putHazards(eventDicts)

    def reset(self, name):
        """
        Reset to canned events or canned settings.
        This will be used in Practice or Test Mode, not Operationally
        @param name: "events" or "settings"
        """
        criteria = {"dataType":name.lower()}
        self.bridge.reset(json.dumps(criteria)) 
        self.selectedEventIDs = []

    ## Selected Time
    def getSelectedTime(self):
        """
        Return the Selected Time displayed in the Console
        
        @return: The selectedTime in milliseconds
        """
        return self.selectedTime
    
    def updateSelectedTime(self, selectedTime_ms):
        """
        Change the Selected Time displayed in the Console
        @param selectedTime_ms:  The new selected time, milliseconds
        """ 
        self.selectedTime = selectedTime_ms
        
    def getSelectedTimeRange(self):
        """
        Get the Selected Time Range displayed in the Console
        @return: The selectedTimeRange (startTime_ms, endTime_ms) in milliseconds
        """
        return json.dumps(self.selectedTimeRange)
    
    def updateSelectedTimeRange(self, startTime_ms, endTime_ms):
        """
        Update the Selected Time Range displayed in the Console
        
        @param selectedTime_ms:  The new selected time range, milliseconds
        NOTE -- Cave and underlying data is always synced to the Selected Time only
             -- Events are synced with the Selected Time Range if it's there
                          otherwise with the Selected Time
        """
        if int(startTime_ms) < 0:
            self.selectedTimeRange = None
        else:
            self.selectedTimeRange = [startTime_ms, endTime_ms]                                
    
    ## Current Time    
    def getCurrentTime(self):
        """
        Get the current time as displayed in the Console
        
        @return: The currentTime in milliseconds
        """
        return self.currentTime
    
    def updateCurrentTime(self, currentTime_ms):
        """
        Change the current time as displayed in the Console
        
        @param currentTime_ms: The new current time, milliseconds 
        """
        self.currentTime = currentTime_ms
        
    def getFrameInfo(self):
        """
        @return: The number of frames,
                 the current frame index,
                 list of frames times in milliseconds
        """
        return self.frameCount, self.frameIndex, self.frameTimeList
        
    def updateFrameInfo(self, framesJSON):
        """
        @param framesJSON: a json string containing the number
                           of frames available (frameCount), the
                           current frame being displayed in CAVE
                           (frameIndex), and a list of frame times
                           in milliseconds (frameTimes).
        """
        framesDict = json.loads(framesJSON)
        self.frameCount = framesDict["frameCount"]
        self.frameIndex = framesDict["frameIndex"]
        self.frameTimeList = framesDict["frameTimes"]
        
        
    ## Console Visible Time
    def getTimeLineEarliestVisibleTime(self):
        return self.earliestVisibleTime
    
    def getTimeLineLatestVisibleTime(self):
        return self.latestVisibleTime
    
    def setTimeLineVisibleTimes(self, earliest_ms,latest_ms):
        self.earliestVisibleTime = earliest_ms
        self.latestVisibleTime = latest_ms 
    
    ## Settings    
    def getCurrentSettingsID(self):
        """
        @return: The current settingsID (String)
        """
        return self.staticSettingsID
    
    def getStaticSettings(self, settingsID):
        criteria = {
                    "dataType": "settings",
                    "filter": {"settingsID": settingsID}
                    }
        return self.bridge.getData(json.dumps(criteria))  
    
    def newStaticSettings(self, settingsDefinition_json):
        """ 
        Add a new Settings and make it the current one
        """
        settingsDef = json.loads(settingsDefinition_json)
        settingsDef["caveView"] = "True"
        # Add in tools.  This is hard-coded until the View Definition Dialog
        # is enhanced to include it.
        hazCat = settingsDef.get("defaultCategory")
        if hazCat == "Convective":
            settingsDef["toolbarTools"] = [
            {
                "toolName": "StormTrackTool", 
                "displayName": "StormTrack Tool"
            }, 
            {
                "toolName": "FollowUpRecommender", 
                "displayName": "Follow Up"
            },
            {
                "toolName": "CensusTool", 
                "displayName": "Census Data"
            }, 
            ]
        elif hazCat == "Winter Weather":
            settingsDef["toolbarTools"] = [
            {
                "toolName": "WSW_recommender", 
                "displayName": "WSW Recommender"
            }, 
            {
                "toolName": "CensusTool", 
                "displayName": "Census Data"
            }
            ]
        else: # hazCat == "Hydrology":  # or anything else
            settingsDef["toolbarTools"] = [
            {
                "toolName": "FloodRecommender", 
                "displayName": "Flood Recommender"
            }, 
            {
                "toolName": "FlashFloodRecommender", 
                "displayName": "Flash Flood Recommender"
            }, 
            {
                "toolName": "DamBreakFloodRecommender", 
                "displayName": "Dam BreakFlood Recommender"
            }, 
            {
                "toolName": "CensusTool", 
                "displayName": "Census Data"
            }
            ]
        settingsID = settingsDef["displayName"]
        settingsDef["settingsID"] = settingsID
        level = settingsDef.get("level", "site" )
        criteria = {
                    NAME: settingsID,
                    "level": level,
                    "dataType": "settings",
                    "configData": settingsDef
                    }
        self.bridge.putData(json.dumps(criteria))
        return settingsID
    
    def updateStaticSettings(self, settingsDefinition_json):
        settingsDef = json.loads(settingsDefinition_json)
        self.staticSettings = settingsDef
        self.staticSettingsID = settingsDef.get("settingsID")
        settingsID = self.staticSettingsID
        criteria = {
                    NAME: settingsID,
                    "level": "USER",
                    "dataType": "settings",
                    "configData": settingsDef
                    }
        self.bridge.putData(json.dumps(criteria))        
    
    def deleteStaticSettings(self,settingsDefinition_json):
        settingsDef = json.loads(settingsDefinition_json)
        self.staticSettings = settingsDef
        settingsID = settingsDef.get("settingsID")
        criteria = {
                    "dataType": "settings",
                    "settings": {settingsID: settingsDef}
                    }
        self.bridge.deleteData(json.dumps(criteria))        
    
    def getDynamicSettings(self):
        """
        @return: The current dynamic settings (String)
        """
        return json.dumps(self.dynamicSettings)

    def updateDynamicSettings(self,settingsDefinition_json):
        settingsDef = json.loads(settingsDefinition_json)
        self.dynamicSettings = settingsDef    

    def getTimeLineDuration(self):
        """
        @return: The duration of the Temporal timeline in milliseconds.
        """
        duration = self.staticSettings.get("defaultTimeDisplayDuration", 24*3600*1000)
        return str(duration)

    def getSettingsList(self):
        """
        Returns the list of Settingss to display on the Tool Bar
        """
        criteria = {
                    "dataType": "settings",
                    "filter": {
                               "caveSettings":"True",
                               }
                    }
        settingsList = self.bridge.getData(json.dumps(criteria))
        settingsList = json.loads(settingsList)
        newList = []
        for settings in settingsList:
            newList.append({"displayName":settings.get("displayName"), "settingsID":settings.get("settingsID")})
        settingsDict = {"settingsList":newList, "currentSettingsID":self.staticSettingsID}
        return json.dumps(settingsDict)
    
    def getToolList(self):
        """
        Returns the list of Tools to display on the Tool Bar given the current Settings
        """
        criteria = {
                    "dataType": "settings",
                    "filter": {
                               "settingsID":self.staticSettingsID
                               },
                    "fields": ["toolbarTools"]
                    }
        result =  self.bridge.getData(json.dumps(criteria))
        result = json.loads(result)
        tools = result.get("toolbarTools")
        return json.dumps(tools)

    ## Alerts
    def getAlertConfigValues(self):
        criteria = {
                    "dataType": "alerts",
                    }
        return self.bridge.getData(json.dumps(criteria))           

    ## Context Menus
    def getContextMenuEntries(self):
        """
        @return: A list of items to add to the CAVE right-click
                 context menu for a selected event or for
                 a potential event.
        """
        menuList = []
        eventDicts = self.findSessionEventDicts(self.selectedEventIDs);
        
        # Test if there are any context menu entries associated with the selected events.
        for eventDict in eventDicts:
            returnList = []
            contextMenuEntries = eventDict.get("contextMenu")
               
            if contextMenuEntries is not None:
                menuEntriesList = contextMenuEntries.keys()                
                for menuItem in menuEntriesList:
                    returnList.append(menuItem)                                            
            if menuList == []:
                menuList = returnList
            else:
                for entry in menuList:
                    if entry not in returnList:
                        if entry != "Add/Remove Shapes":
                            menuList.remove(entry)
                
                #
                # We need the Add/Remove Shapes option to always be in the 
                # final menuList if it was in one of the originally selected
                # events.
                if "Add/Remove Shapes" in returnList:
                     menuList.append("Add/Remove Shapes")

        menuList.append("Hazard Information Dialog")
                
        # Determine additional entries which must apply to ALL selected events to be displayed.
        end = "End Selected Hazards"
        issue = "Issue Selected Hazards"
        propose = "Propose Selected Hazards"
        delete = "Delete Selected Hazards"
        save = "Save Proposed Hazards"
        
        # Options for altering the display order of hazards
        sendBack = "Send to Back"
        bringFront = "Bring to Front"
        hazardOccurrenceAlerts = "Hazard Occurrence Alerts"
        
        commonList = []
        for eventDict in eventDicts:   
            eventList = []         
            if eventDict.get(STATE) == ISSUED:
                eventList += [end, sendBack, bringFront]
            elif eventDict.get(STATE) == PROPOSED:
                eventList += [issue, delete, save, sendBack, bringFront]
            else:
                eventList += [propose, issue, delete, sendBack, bringFront]
            if not commonList:
                commonList = eventList
            else:
                for item in commonList:
                    if item not in eventList:
                        commonList.remove(item)                        
        menuList += commonList
        
        # Check for potential events and alerts    
        if self.findEventDicts(STATE, POTENTIAL):
            menuList += ["Remove Potential Hazards"]
        menuList += [hazardOccurrenceAlerts]
        return json.dumps(menuList)
    
    def getContextMenuEntryCallback(self, menuItemName):
        eventDicts = self.findSessionEventDicts(self.selectedEventIDs);
        
        for eventDict in eventDicts:
            # Test if there are any context menu entries associated with the selected event.
            contextMenuEntries = eventDict.get("contextMenu")            
            if contextMenuEntries is not None:
                callback = contextMenuEntries[menuItemName]
                break       
        return callback            
                
    ##  Tool Handler
    def runTool(self, toolID, runData=None):
        """
        Runs a tool and stores the results.
        @param toolName: The name of the tool to run
        @param runData: JSON string containing any information required
                  by the tool to run.
                  
        @return: A JSON string containing the tool meta information and
                 result data.
        """
        return self.toolHandler.runTool(toolID, runData=runData)
  
    def getDialogInfo(self, toolID, runData=None):
        """
        @param toolID: The tool to retrieve dialog info for.
        @param runData: Run data (generally session info) which 
                        will help the tool produce the dialog info.
        @return: JSON containing GUI building instructions or None
        """
        return self.toolHandler.getDialogInfo(toolID, runData = runData)

    def getSpatialInfo(self, toolID, runData=None):
        """
        @param toolID: The tool to retrieve dialog info for.
        @param runData: Run data (generally session info) which 
                        will help the tool produce the dialog info.
        @return: JSON containing the what spatial information this
                 tool needs to run or None.
        """
        return self.toolHandler.getSpatialInfo(toolID, runData= runData)
    
    def getMetaData(self, toolID, runData=None):
        """
        @param toolID: The tool to retrieve dialog info for.
        @param runData: Run data (generally session info) which 
                        will help the tool produce the dialog info.
        @return: JSON containing the meta data describing this tool's
                 services
        """
        return self.toolHandler.getMetaData(toolID, runData) 
        
    def createProductsFromEventIDs(self, issue):    
        '''
        '''
        return self.toolHandler.createProductsFromEventIDs(issue)  
         
    def createProductsFromHazardEventSets(self,issueFlag, productList):
        return self.toolHandler.createProductsFromHazardEventSets(issueFlag, productList)

    ## Presenter Helper
    def getComponentData(self, component, eventID):
        """
        @param component: The component to format the list of events for
        @param eventID:  The identifier of the event.
        @return: A list of events tailored for the specified component. For
                 example, the temporal dialog expects the selected event
                 to be first. 
        """
        return self.presenterHelper.getComponentData(component, eventID)

    def getConfigItem(self, itemName):
        """
        Retrieves configuration information for a named item, such as the 
        Hazard information dialog 
        @param itemName: The name of the item for which to retrieve configuration
                         Data
        @return: A String containing the configuration data
        """
        return self.presenterHelper.getConfigItem(itemName)
       
    #####################
    ##  Utility methods    
    def getPendingEvents(self):
        pending = self.findEventDicts(STATE, PENDING)
        return json.dumps(pending)   
    
    def removeDuplicates(self, l):
        ll = []
        for item in l:
            if item not in ll and item:
                ll.append(item)
        return ll            
        
    def changeSelectedTime(self, eventDict):
        '''
        Change selected time or selected time range if 
        first selected event does not overlap
        '''
        if not self.timeOverlap(eventDict):
            startTime = int(eventDict.get(START_TIME))
            endTime = int(eventDict.get(END_TIME))
            if self.selectedTimeRange:
                self.updateSelectedTimeRange(str(startTime), str(endTime))
                return "Range"
            else:
                self.updateSelectedTime(str(startTime)) 
                return "Single"
        return "None"                                                                                     
        
    def getHazards(self, settings):
        """
        @param settings: The settings dictionary
        @return: A list of hazard event dicts.
        """
        criteria = {
                    "dataType": "events",
                    "settings": settings,
                    }
        hazardEventsDict = self.bridge.getData(json.dumps(criteria))
        hazardEventsDict = json.loads(hazardEventsDict) 
        eventDicts = []
        for key in hazardEventsDict:
            eventDicts.append(hazardEventsDict.get(key))
        return eventDicts

    def putHazards(self, eventDicts=None):
        """
        Writes an event or list of events to the database.
        @param eventDicts: The list of events ... if empty, the selected
                        hazards are used. 
        """
        if eventDicts is None: 
            eventDicts = self.findSessionEventDicts(self.selectedEventIDs)

        
        criteria = {
                    "dataType": "events",
                    "eventDicts": eventDicts,
                    "productCategory": None,
                    SITE_ID: self.siteID,
                    }
        self.bridge.putData(json.dumps(criteria))
 
    def getConfig(self):
        """
        @return: Dict - The configuration of Hazard Services
        """
        config = json.loads(self.bridge.getData(json.dumps({"dataType": "config"})))
        return config

    def getLayerData(self):
        """
        @return: XML representing the available geo data layers for the
                 control panel.
        """
        return self.geoXML()
    
        
    def addEvent(self, eventID, shapes, creationTime, startTime, endTime, state=PENDING,
                 draggedPoints=[], modifyCallbackToolName=None):
        """
        Utility method to add an event to the set of eventDicts
        """            
        eventDict = {
                     EVENT_ID: eventID,
                     STATE:state,
                     SHAPES: shapes,
                     START_TIME: startTime,
                     END_TIME: endTime,
                     CREATION_TIME: creationTime,
                     "draggedPoints": draggedPoints,
                     "modifyCallbackToolName": modifyCallbackToolName,
                     }
        eventDict = self.setUpEventFields(eventDict)
        self.eventDicts.append(eventDict)        
            
    def getIgnoreKeys(self, source):
        """
        There are certain things certain components want to ignore. The HID
        should ignore shapes.  It doesn't need to know about geometries.
        @param source: The Hazard Services component
        @return: A list of event keys to ignore
        """
        ignoreKeys = []
        if source == "HID":
            ignoreKeys = [SHAPES]
        return ignoreKeys     
                                        
    def findSessionEventDicts(self, eventIDs=None):
        """
        @param eventIDs: The ids of the events to retrieve
        @return: If one event, return one event dictionary.
                 Otherwise, return a list of eventDicts
        """   
        if eventIDs is None:
            return self.eventDicts     
        eventDicts = []
        for eventDict in self.eventDicts:
            if eventDict.get(EVENT_ID) in eventIDs:
                eventDicts.append(eventDict)
        return eventDicts
    
    def findEventDicts(self, field, values, inputEventDicts=None, keepNoneValues=False):
        """
        Finds events meeting the search criteria.
        @param field: the field to search on
        @param value: the value to search on ; value is a list
        @return: a list of event dictionaries passing the search criteria.
        """
        if inputEventDicts is None:
            inputEventDicts = self.eventDicts
        eventDicts = []
        for eventDict in inputEventDicts:
            if eventDict.get(field) in values: 
                eventDicts.append(eventDict)
            else:
                if keepNoneValues:
                    if eventDict.get(field) is None:
                        eventDicts.append(eventDict)
        return eventDicts
                                                         
    def filterEventsForDynamicSettings(self, eventDicts):
        """ 
        Filter the eventDicts according to the current dynamic settings
        Also return the "leftoverEvents" 
        """
        keptEvents = eventDicts
        
        for settingsField, eventField in [("visibleTypes", HAZARD_TYPE), ("visibleSites",SITE_ID), 
                                          ("visibleStates",STATE)]:
            values = self.dynamicSettings.get(settingsField, [])
            if values:
                keptEvents = self.findEventDicts(eventField, values, keptEvents, keepNoneValues=True)                               
        leftoverEvents = []
        for event in eventDicts:
            if event not in keptEvents:
                leftoverEvents.append(event)
        return keptEvents, leftoverEvents

    def addEvents(self, eventDicts):
        """
        Given a list of potential or pending events (e.g. produced by the Flood Recommender), 
        add them to the eventDict list  
        Remove all current potential events before adding new ones.
        This would probably not be done operationally.
        @param eventDicts: List of events
        """ 
        self.removeEvents(STATE, POTENTIAL)

        for eventDict in eventDicts:
            if eventDict.get(HAZARD_TYPE):
                self.setEventType(eventDict)
            # Check to see if a session eventDict for this event already exists
            if not self.updateExisting(eventDict):
                # Make a new event
                eventID = self.bridge.newSeqNum()
                eventDict[EVENT_ID] = eventID
                eventDict = self.setUpEventFields(eventDict)
                self.eventDicts.append(eventDict)
            state = eventDict.get(STATE)
        # IF there is only one pending event, add it the Selected Events
        if len(eventDicts) == 1 and state == PENDING:
            self.updateSelectedEvents(json.dumps([eventID]), self.addToSelected, "")
        
    def setUpEventFields(self, eventDict): 
        eventDict[SITE_ID] = self.siteID
        eventDict[BACKUP_SITE_ID] = self.backupSiteID
        return eventDict  
    
    def setEventType(self, eventDict, changedField=HAZARD_TYPE):
        """
        Set the type, phen, sig, subType, fullType, headline
        type is phen.sig.subType
        fullType is type + (headline)
        
        @param eventDict: a hazard event dictionary
        @param changedField:  HAZARD_TYPE e.g. "FF.A" or FULLTYPE e.g. "FF.A (FLASH FLOOD WATCH)"
        """
        changedValue = eventDict.get(changedField)
        if not changedValue:
            return 
        phen, sig, subType = self.getPhenSigSubType(changedField, changedValue)  
        eventDict[PHENOMENON] = phen
        eventDict[SIGNIFICANCE] = sig
        eventDict[SUBTYPE] = subType
        type, headline, fullType = self.getHazardTypeInfo(phen, sig, subType)
        eventDict[HAZARD_TYPE] = type
        eventDict[HEADLINE] = headline 
        eventDict[FULLTYPE] = fullType
        
    def getPhenSigSubType(self, field, value):   
        '''
        Given a field (HAZARD_TYPE or FULLTYPE),
        return the PHENONMENON, SIGNIFICANCE, and SUBTYPE        
        '''     
        try:
            phen, sig, subType = value.split(".")
        except:
            phen, sig = value.split(".")
            subType = ""
        if field == FULLTYPE:
            try:
                subType, headline = subType.split("(")
                subType = subType.strip() 
            except:
                sig, headline = sig.split("(")
                headline = headline.strip(")")
        sig = sig.strip()
        return phen, sig, subType
                       
    def getHazardTypeInfo(self, phen, sig, subType):
        type = phen+"."+sig
        if subType:
            type += "."+subType
        type = type.strip()
        hazardTypeInfo = self.hazardTypes.get(type)
        if hazardTypeInfo is not None:
            headline = hazardTypeInfo.get(HEADLINE)
            fullType = type + " ("+headline+")"
        else:
            headline = type
            fullType = type
        return type, headline, fullType
                    
    def updateExisting(self, updateDict):
        """
        If this is a point event, check to see if an existing eventDict matches this point
        If so, update the eventDict with the updateDict information and return True
        Otherwise, return False
        
        NOTES: 
           -- This method will eventually be enhanced to match areal events as well 
            as points.  
            To do so, we will require a user-defined percentage of areal, temporal, and 
            phenomenon overlap to match.
           -- Instead of simply replacing the eventDict with the updateDict, 
            we will eventually need to retain one level of history
        
        """
        pointID = updateDict.get("pointID") 
        if not pointID:
            return False
         
        for (i, eventDict) in enumerate(self.eventDicts):    
            if eventDict.get("pointID") == pointID:
                updateDict[EVENT_ID] = eventDict.get(EVENT_ID)
                updateDict[STATE] = eventDict.get(STATE)
                # TO DO: Retain a level of history
                self.eventDicts[i] = updateDict
                return True
        return False                     
                       
    def timeOverlap(self, eventDict):
        """
        This method returns True if the given event overlaps with the selected time range (if there is one) 
        otherwise, it returns True if it overlaps with the selected time        
        """
        # TODO: eventually, this method will have to look at time sets as well
        startTime = int(eventDict.get(START_TIME))
        endTime = int(eventDict.get(END_TIME))
        # First check for a time range
        if self.selectedTimeRange:
            selectedStart, selectedEnd = self.selectedTimeRange
            selectedStart = int(selectedStart)
            selectedEnd = int(selectedEnd)
            if (startTime <= selectedEnd) and (endTime >= selectedStart):
                    return True
            return False
        # Otherwise, check the selected time
        selectedTime = int(self.selectedTime)
        if startTime <= selectedTime and endTime >= selectedTime:
            return True
        else:
            return False

    # add a new function for filtering states that are not canceled/ended in eventDicts 
    def findEventDictsByStateFilter(self, field, hazardTypesForProduct):
        """
        Finds events meeting the search criteria.
        @param field: the field to search on
        @param hazardTypesForProduct: the candidate value for filtering
        @return: a list of event dictionaries passing the search criteria.
        """
        filterDicts = ['canceled','expired',ENDED]
        
        eventDicts = []
        for eventDict in self.eventDicts:
            for hazardType in hazardTypesForProduct:
                if eventDict.get(field) == hazardType and eventDict.get(STATE) not in filterDicts:
                    eventDicts.append(eventDict)
        return eventDicts

    ## For Testing

    def putEvent(self, eventDictAsJson):
        eventDict = json.loads(eventDictAsJson)
        self.eventDicts.append(eventDict)
        eventDicts = dict()
        eventID = eventDict[EVENT_ID]
        eventDicts[eventID] = eventDict
        self.bridge.writeEventDB(eventDicts)
    
    def setHazardEventManager(self, hazardEventManager):
        self.hazardEventManager = hazardEventManager

    def getSessionEvent(self, eventID):
        event = self.findSessionEventDicts([eventID])[0]
        result = json.dumps(event)
        return result 
    
    def sendSelectedHazardsToFront(self):
        """
        Sends the selected hazard or hazards to the front of the
        event dicts list. These are the ones which are drawn first and
        are searched first in the Spatial Display.
        """
        selectedEventIDsList = self.getSelectedEventIDs()
        selectedEvents = self.findSessionEventDicts(selectedEventIDsList)
        
        if selectedEvents is not None and len(selectedEvents) > 0:
            for selectedEvent in selectedEvents:
                self.eventDicts.remove(selectedEvent)
                self.eventDicts.append(selectedEvent)
      
    def sendSelectedHazardsToBack(self):
        """
        Sends the selected hazard or hazards to the back of the event 
        dicts list. These are the ones which are drawn last and are searched
        last in the Spatial Display
        """        
        selectedEventIDsList = self.getSelectedEventIDs()
        selectedEvents = self.findSessionEventDicts(selectedEventIDsList)
        
        if selectedEvents is not None and len(selectedEvents) > 0:
            for selectedEvent in reversed(selectedEvents):
                self.eventDicts.remove(selectedEvent)
                self.eventDicts.insert(0,selectedEvent)
                
    def handleRecommenderResult(self, toolID, eventList):
        return self.toolHandler.handleRecommenderResult(toolID, eventList)
                
    def handleProductGeneratorResult(self, toolID, generatedProducts):  
        return self.toolHandler.handleProductGeneratorResult(toolID, generatedProducts)
    
    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()

    def printEventDict(self, eventDict, methodName=""):
        """
        Debugging utility for printing an event dict.
        @param eventDict: The event dict to print
        @param methodName: The name of invoking method. 
        """    
        print "SessionManager: "+methodName   
        for key in eventDict:
            if key == SHAPES:
                print "   shapes"
                shapes = eventDict[key]
                for shape in shapes:
                    shapeType = shape.get(SHAPE_TYPE)
                    print "     ", shapeType
                    if shapeType == "circle":
                        for key in shape:
                            print "      ", key, shape.get(key)                        
            elif key == "latLonPolygon":
                print "   ", key
            else:
                print "   ", key, eventDict[key]
        self.flush()

