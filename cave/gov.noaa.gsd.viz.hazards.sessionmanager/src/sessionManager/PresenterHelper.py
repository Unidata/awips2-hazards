"""
 Formats the session state information appropriately for the 
 Presenters and Views
 
Python is used for this module to work efficiently with 
non-homogeneous data structures. 
 @since: March 2012
 @author: GSD Hazard Services Team
"""

import os, json, types
import logging, UFStatusHandler
from HazardConstants import *
from gov.noaa.gsd.viz.hazards.utilities import Utilities

class PresenterHelper(object):
    def __init__(self, bridge, sessionManager):
        self.bridge = bridge
        self.sessionManager = sessionManager
        self.logger = logging.getLogger("PresenterHelper")
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            "gov.noaa.gsd.viz.hazards.sessionmanager", "PresenterHelper", level=logging.INFO))
       
    def getComponentData(self, component, eventID):
        """
        @param component: The component to format the list of events for
        @param eventID:  The identifier of the event.
        @return: A list of events tailored for the specified component. For
                 example, the temporal dialog expects the selected event
                 to be first. 
        """
        if component == "Temporal":
            events = self.getEvents(eventID, self.eventDictToTemporal)
            # Sort order should be: 
            #    Selected Events first
            #    Potential Events next
            #    By creation time or start time IF no creation time
            #events.sort(self.sortEventsTD)
            selectedEventIDs = self.sessionManager.getSelectedEventIDs()
            for event in events:
                eventID = event.get(EVENT_ID)
                if eventID in selectedEventIDs:
                    event["selected"] = True
                    # Make the new selected events first in the list
                    if event.get(STATE) in [PENDING, POTENTIAL]:
                        events.remove(event)
                        events.insert(0, event)
                else:
                    event["selected"] = False
                
            jsonText = json.dumps(events) 
            dynamicSettings = self.sessionManager.dynamicSettings
            dynamicSettings["staticSettingsID"]=self.sessionManager.staticSettingsID
            tdDict = {"events":events, "dynamicSettings":dynamicSettings}
            jsonText = json.dumps(tdDict)
        elif component == "Spatial":
            events = self.getEvents(eventID, self.eventDictToSpatial)
            jsonText = json.dumps(events)
        elif component == "HID":
            hidEvents = []
            selectedEventIDs = self.sessionManager.getSelectedEventIDs()
            for eventID in selectedEventIDs:
                eventDict = self.hidEventInfo(eventID)
                if eventDict:
                    hidEvents.append(eventDict)
            if not hidEvents: jsonText = ""
            else: jsonText = json.dumps(hidEvents)
        
        self.logger.debug( "PresenterHelper getComponentData: component: " + component +" "+ jsonText)
        return jsonText
    
    def sortEventsTD(self, a, b):
        """
        Comparator method supplied to a list sort method.
        This makes sure that potential events are listed first.
        Otherwise, it sorts on creation and/or starttime
        @param a: An event from the list
        @param b: An event from the list to compare to a
        @return: -1 a < b
                  1 a >= b
                  
        """
        aState = a.get(STATE)
        bState = b.get(STATE)
        if aState == POTENTIAL:
            return -1
        if bState == POTENTIAL:
            return 1
        aTime = a.get(CREATION_TIME)
        if aTime is None:
            aTime = a.get(START_TIME)
        bTime = b.get(CREATION_TIME)
        if bTime is None:
            bTime = b.get(START_TIME)
        if (aTime < bTime):
            return -1
        return 1
                
    def getEvents(self, eventID, method, selectedEventIDs=[]):
        """
        This method is called to format the eventDict information for the 
        Spatial Display or Temporal Display.
        @param eventID: The eventID, if eventID == "all", then returns a list
                        of eventDicts that overlap with the current selected time.
        @param method: The method to use to create an event in the returned 
                       event list.
        @return: list of events
        """
        selectedEventIDs = self.sessionManager.getSelectedEventIDs()
        events = []
        self.logger.debug( "PresenterHelper getEvents eventID: " + eventID + " "+ json.dumps(selectedEventIDs))
        if eventID == "all":
            for eventDict in self.sessionManager.eventDicts:
                entry = method(eventDict, selectedEventIDs)
                if entry: events.append(entry)
        else:
            eventDict = self.sessionManager.findSessionEventDicts([eventID])[0]
            entry = method(eventDict, selectedEventIDs)
            if entry: events.append(entry)
        return events

    def hidEventInfo(self, eventID):
        """
        @param eventID: An event ID
        @return: A JSON string containing eventDicts which can be
                 used to populate a Hazard Information Dialog
        """
        eventDicts = self.sessionManager.findSessionEventDicts([eventID])
        if eventDicts:
            eventDict = eventDicts[0]
            self.insertHidFields(eventDict)
            return eventDict
        return None

    def insertHidFields(self, eventDict):
        """
        @param eventDict: An eventDict to insert HID fields into.
        """
        if not eventDict.has_key(HAZARD_TYPE) and not eventDict.has_key("hazardCategory"):
            eventDict["hazardCategory"] = self.sessionManager.staticSettings.get("defaultCategory", "")        
        
    def eventDictToTemporal(self, eventDict, selectedEvents):
        """
        This method converts the given eventDict information into
        Temporal Display format
        """
        tdDict = {}
        # Ignore potential events
        #if eventDict.get(STATE) == POTENTIAL:
        #    return tdDict
        for key in [START_TIME, END_TIME, HAZARD_TYPE, STATE, EVENT_ID, CREATION_TIME, SITE_ID,
                    HEADLINE, FULLTYPE]:
            tdDict[key] = eventDict.get(key)
        tdDict['color'] = self.getFillColor(eventDict);
        tdDict['checked']= eventDict.get("checked", True)
        hazardType = eventDict.get(HAZARD_TYPE)
        try:
            eventDict.set(PHENOMENON, hazardType[:2])
            eventDict.set(SIGNIFICANCE, hazardType[3:])
        except:
            pass
        self.logger.debug( "PresenterHelper eventDictToTemporal selectedEvents" + \
                           json.dumps(selectedEvents)+" "+ eventDict.get(EVENT_ID))
        tdDict['selected'] = eventDict.get(EVENT_ID) in selectedEvents
        return tdDict        
    
    def eventDictToSpatial(self, eventDict, selectedEvents=[]):
        """
        This method converts the given eventDict information into the
        Spatial Display format.
        """
        sdDict = {}
        eventID = eventDict.get(EVENT_ID, '')
        if not eventDict.get('checked', True): return sdDict
        sdDict[EVENT_ID]= eventID
        shapes = []
        isSelected = eventID in self.sessionManager.getSelectedEventIDs()   
        if eventID in selectedEvents:
            isSelected = True     
        eventShapes = eventDict.get(SHAPES, [])
        overlap = self.sessionManager.timeOverlap(eventDict)
        eventType = eventDict.get(HAZARD_TYPE,"")
        state = eventDict.get(STATE, PENDING)        
        dps = self.getDraggedPoints(eventDict)
        if dps:
            dTimes = []
            for (lon, lat), t in dps: dTimes.append(t)

        trackingPointLonLats = []
        label = eventID + " " + eventType

        # overlayShapes are things we want to plot last, and therefore on top,
        # of other things so they are not covered over by other things.
        overlayShapes = []

        # If a tracker is active and frame is not within the valid time of the
        # event, we still want to show polygon for event associated with tracker.
        subduedPolygon = None

        for eventShape in eventShapes:
            # Polygons            
            # Build a list of tracking points.  This will be used to create a
            # line between the tracking points.
            
            if eventShape.get(SHAPE_TYPE) == "polygon":
                if overlap:
                    shapes.append({
                           SHAPE_TYPE:"polygon",
                           INCLUDE: eventShape.get(INCLUDE, "true"),
                           POINTS: eventShape.get(POINTS),
                           "fill color":self.getFillColor(eventDict),
                           "border color":"255 255 255",
                           "borderStyle":self.getBorderStyle(state),
                           "border thick":self.getBorderThick(isSelected),
                           "isVisible": "true",
                           "label": label,
                           "isSelected": isSelected,
                           })
                else :
                    subduedPolygon = {
                           SHAPE_TYPE:"polygon",
                           INCLUDE: eventShape.get(INCLUDE, "true"),
                           POINTS: eventShape.get(POINTS),
                           "fill color":"0 0 0",
                           "border color":"127 127 127",
                           "borderStyle":self.getBorderStyle(state),
                           "border thick":self.getBorderThick(isSelected),
                           "isVisible": "true",
                           "label": label,
                           "isSelected": isSelected
                           }

            # Tracking points
            if eventShape.get(SHAPE_TYPE) in ["star", "circle", POINT, "dot"]:
                if eventShape.get("pointType", "") == "tracking":
                    if eventID in self.sessionManager.getSelectedEventIDs():
                        t = eventShape.get("pointID")                        
                        lonLat = eventShape.get(POINT)
                        trackingPointLonLats.append(lonLat)

                        # We know that there are seconds to milliseconds (and 
                        # vice-versa) conversions going on all over the place.
                        # May be unsafe to use exact equality tests with 
                        # millisecond units to determine when two times match
                        # each other.
                        nowFrame = False
                        slop = long(t)-long(self.sessionManager.getSelectedTime())
                        if slop>-999 and slop<999 :
                            nowFrame = True

                        if nowFrame : rgb = "255 255 255"
                        else :        rgb = "127 127 127"
                        if t in dTimes: shapeType = "star"
                        else:           shapeType = "dot"

                        addShape = {"pointID": t,
                                    SHAPE_TYPE:shapeType,
                                    "centerPoint":lonLat,
                                    "radius"       : 1,
                                    "fill color":self.getTrackColor(t),
                                    "border color":rgb ,
                                    "borderStyle":self.getBorderStyle(state),
                                    "border thick":self.getBorderThick(isSelected),
                                    "isVisible": "true",
                                    "isSelected": isSelected,
                                    }
                        if nowFrame :
                            overlayShapes.append(addShape)
                        else :
                            shapes.append(addShape)

                # Must be forecast point
                elif overlap:  
                        lonLat = eventShape.get(POINT)
                        id = eventShape.get(ID, "")
                        shapes.append({
                                       #"pointID": t,
                                       SHAPE_TYPE:"circle",
                                       "centerPoint":lonLat,
                                       "radius"       : 1,
                                       "fill color": "255 255 255",
                                       "border color":"255 255 255",
                                       "borderStyle":"SOLID",
                                       "border thick":2,
                                       "isVisible": "true",
                                       "isSelected": isSelected,
                                       "label": id,
                                       })  
                        
        if trackingPointLonLats:
            shapes.append({
                           SHAPE_TYPE:"line",
                           INCLUDE: eventShape.get(INCLUDE, "true"),
                           POINTS: trackingPointLonLats,
                           "fill color":self.getFillColor(eventDict),
                           "border color":"127 127 127",
                           "borderStyle":self.getBorderStyle(state),
                           "border thick":self.getBorderThick(isSelected),
                           "isVisible": "true",
                           "label": label,
                           "isSelected": isSelected,
                           })

        if len(trackingPointLonLats)>0 and subduedPolygon :
            shapes.insert(0, subduedPolygon)
        shapes.extend(overlayShapes)

        sdDict[SHAPES] = shapes
        
        geoReference = eventDict.get("geometryReference")
        mapName = eventDict.get("geometryMapName")
        
        if geoReference is not None:
            sdDict["geometryReference"] = geoReference
            
        if mapName is not None:    
            sdDict["geometryMapName"] = mapName
            
        return sdDict

    def getDraggedPoints(self, eventDict):
        """
        @param eventDict: The event dict
        @return: A list of dragged points.
        """
        dp = eventDict.get("draggedPoints", [])
        dpLen = int(eventDict.get('draggedPointsLen', 2))
        #slice = len(dp)-(dpLen) # dpLen-1
        dpRet = dp[:dpLen]
        return dpRet       
            
    def getTrackColor(self, t):
        """
        Sets the storm tracking point color based on selected time.
        @param t: Storm track point time.
        @return: A string with an RGB value.
        """
        if str(t) == str(self.sessionManager.selectedTime):
            return self.sessionManager.config.get("trackingPointColor", "160 32 240")
        else: 
            return self.sessionManager.config.get("trackingStarColor", "0 255 255")
        
    def getBorderStyle(self, state):
        """
        Sets the hazard border style based on the hazard's state.
        @param state: 
        @return: String containing border style
        """
        if state == PENDING:
            return "NONE"
        elif state == PROPOSED:
            return  "DASHED"
        elif state == ISSUED:
            return "SOLID"
        elif state == POTENTIAL:
            return "DOTTED"
        else:
            return "SOLID"
        
    def getBorderThick(self, isSelected):
        """
        @param isSelected: True/False
        @return: The border thickness based on selection state 
        """
        if isSelected:
            return 4
        else:
            return 1
    
    def getFillColor(self, eventDict):
        """
        Returns an event color based on 
        the type of the event (FF.W.Convective, BZ.W, WS.W).
        @param eventDict: The event
        @return: The fill color for the event as a string of RGB values 
        """
        type = eventDict.get(HAZARD_TYPE)
        
        if isinstance(type, unicode):
            type = str(type)
        
        color = Utilities.getHazardFillColor(type)
        colorString = str(int(color.getRed())) + " " + str(int(color.getGreen())) + " " + str(int(color.getBlue()))    
        return colorString
    
    def getMegawidget(self, item, fieldName):
        """
        Get the dictionary with the specified field name under the key "fieldName",
        if such a dictionary is found. If it is found, the dictionary represents a
        megawidget definition.
        @param item: Either a list, in which case this method is called recursively
                     on its elements; a dictionary, in which case it is checked for
                     the specified field name and the choices added if the field
                     name is found, and if it does not have the field name, this
                     method is called recursively on each of its values; or another
                     type of object, in which case nothing is done.
        @param fieldName: Value to search for in the dictionary under the key
                          "fieldName".
        @return Dictionary with the matching fieldName, if any.
        """
        if type(item) is types.DictionaryType:
            if "fieldName" in item and item["fieldName"] == fieldName:
                return item
            else:
                for subItem in item.values():
                    dict = self.getMegawidget(subItem, fieldName)
                    if dict is not None:
                        return dict
        elif type(item) is types.ListType:
            for subItem in item:
                dict = self.getMegawidget(subItem, fieldName)
                if dict is not None:
                    return dict
    
    def addChoicesToMegawidget(self, item, fieldName, choices):
        """
        Add the specified choices value under the key "choices" to the dictionary
        with the specified field name under the key "fieldName", if such a
        dictionary is found. If it is found, the dictionary represents a megawidget
        definition.
        @param item: Either a list, in which case this method is called recursively
                     on its elements; a dictionary, in which case it is checked for
                     the specified field name and the choices added if the field
                     name is found, and if it does not have the field name, this
                     method is called recursively on each of its values; or another
                     type of object, in which case nothing is done.
        @param fieldName: Value to search for in the dictionary under the key
                          "fieldName".
        @param choices: Choices value to insert under the key "choices" if the
                        field name is found.
        """
        if type(item) is types.DictionaryType:
            if "fieldName" in item and item["fieldName"] == fieldName:
                item["choices"] = choices
            else:
                for subItem in item.values():
                    self.addChoicesToMegawidget(subItem, fieldName, choices)
        elif type(item) is types.ListType:
            for subItem in item:
                self.addChoicesToMegawidget(subItem, fieldName, choices)

    def getConfigItem(self, itemName):
        """
        Retrieves configuration information for a named item, such as the 
        Hazard information dialog 
        @param itemName: The name of the item for which to retrieve configuration
                         Data
        @return: A String containing the configuration data
        """
        #criteria = {"dataType": itemName}
        #item = self.bridge.getData(json.dumps(criteria)) 
        #return item
    
        if itemName == "hazardInfoConfig":
            item = json.dumps(self.convertHazardCategories())  
            return json.dumps(self.convertHazardCategories()) 
        elif itemName == "hazardInfoOptions":
            return json.dumps(self.convertHazardMetaData())
        elif itemName == "viewConfig" or itemName == "filterConfig":
            categories = self.convertHazardCategories()["hazardCategories"]
            criteria = {"dataType": "viewConfig"}
            viewConfig = json.loads(self.bridge.getData(json.dumps(criteria)))
            self.addChoicesToMegawidget(viewConfig, "hazardCategoriesAndTypes", categories)
            if itemName == "viewConfig":
                return json.dumps(viewConfig)
            else:
                hazardCategoriesAndTypesMenuMegawidget = self.getMegawidget(viewConfig, "hazardCategoriesAndTypes")
                hazardCategoriesAndTypesMenuMegawidget["fieldType"] = "HierarchicalChoicesMenu"
                hazardCategoriesAndTypesMenuMegawidget["label"] = "Hazard &Types"
                sitesMenuMegawidget = self.getMegawidget(viewConfig, "visibleSites")
                sitesMenuMegawidget["fieldType"] = "CheckBoxesMenu"
                sitesMenuMegawidget["label"] = "Site &IDs"
                statesMenuMegawidget = self.getMegawidget(viewConfig, "visibleStates")
                statesMenuMegawidget["fieldType"] = "CheckBoxesMenu"
                statesMenuMegawidget["label"] = "&States"
                filterConfig = [hazardCategoriesAndTypesMenuMegawidget, sitesMenuMegawidget, statesMenuMegawidget]
                return json.dumps(filterConfig)
        else:
            criteria = {"dataType": itemName}
            item = self.bridge.getData(json.dumps(criteria)) 
        return item 
    
    def convertHazardCategories(self):
        '''
        Convert the hazard types listed in the HazardCategories file to fullTypes
        '''
        categories = self.sessionManager.hazardCategories
        hazardCategories = []  
        for category in categories:
            typeInfoList = []
            hazardTypes = categories.get(category)
            for hazardType in hazardTypes:
                phen, sig, subType = self.getPhenSigSubType(hazardType)
                type, headline, fullType = self.sessionManager.getHazardTypeInfo(phen, sig, subType)
                typeInfo = {
                            "displayString": fullType,
                            "identifier": type,
                            }
                typeInfoList.append(typeInfo)
            hazardCategory = {
                              "displayString": category,
                              "children": typeInfoList,
                              }
            hazardCategories.append(hazardCategory)                       
        return {
                "hazardCategories": hazardCategories,
                "defaultCategory": "Hydrology",
                }
   
    def convertHazardMetaData(self): 
        '''
        Convert the hazard types listed in the HazardMetaData file to fullTypes
        '''
        hazardInfoOptions = [] 
        for metaDataDict in self.sessionManager.hazardMetaData:
            newDict = {}
            hazardTypes = metaDataDict.get("hazardTypes")
            newTypes = []
            for hazardType in hazardTypes:
                phen, sig, subType = self.getPhenSigSubType(hazardType)
                type, headline, fullType = self.sessionManager.getHazardTypeInfo(phen, sig, subType)
                newTypes.append(fullType)                    
            newDict["hazardTypes"] = newTypes
            newDict["metaData"] = metaDataDict["metaData"]
            hazardInfoOptions.append(newDict)
        return hazardInfoOptions
    
    def getPhenSigSubType(self, hazardTypeTuple):
        if len(hazardTypeTuple) == 3:
            phen, sig, subType = hazardTypeTuple
        else:
            phen, sig = hazardTypeTuple
            subType = ""
        return phen, sig, subType
        

    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()




