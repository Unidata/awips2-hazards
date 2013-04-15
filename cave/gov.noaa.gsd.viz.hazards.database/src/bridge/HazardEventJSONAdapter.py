'''
/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
'''
try:
    import JUtil
    import GeometryFactory
    import EventFactory
    
    # GSD has requested assistance from RTS on how to avoid the "Unresolved"
    # annotations.  In our eclipse editor, failing to include this causes
    # a syntax error.
    from java.util import ArrayList #@UnresolvedImport
    from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants #@UnresolvedImport
    from com.raytheon.uf.common.dataplugin.events.hazards.datastorage import HazardQueryBuilder #@UnresolvedImport
except:
    pass
import datetime

#
# Adapts to/from HazardEvent/JSON representations of events
#
#
#
#     SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    Feb 21, 2013           daniel.s.schaffer@noaa.gov       Initial Creation.
#



def hazardHistoryConverter(obj):
    objtype = obj.jclassname
    if objtype == "com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList":
        javaEvent = obj.get(0)
        eventID = javaEvent.getEventID()
        event = dict()
        event["eventID"] = eventID
        event["startTime"] = toDate(javaEvent.getStartTime())
        event["endTime"] = toDate(javaEvent.getEndTime())
        event["phen"] = javaEvent.getPhenomenon()
        event["sig"] = javaEvent.getSignificance()
        event["subType"] = javaEvent.getSubtype()
        
        #
        # The subtype is not guaranteed to always be present.
        if event["subType"] is not None and len(event["subType"]) > 0:
            event["type"] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance() + "." + javaEvent.getSubtype()
        else:
            event["type"] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance()
            
        event["state"] = str(javaEvent.getState()).lower()
        event["siteID"] = javaEvent.getSiteID()
        event["shapes"] = geometryConverter(javaEvent.getGeometry())
        
        attributes = javaEvent.getHazardAttributes()

        keys = attributes.keySet()
        iterator = keys.iterator()
            
        while iterator.hasNext():
            key = iterator.next()
            value = attributes.get(key)
            asPython = JUtil.javaObjToPyVal(value)
            event[str(key)] = asPython
                
    return event

def eventConverter(javaEvent):
    objtype = javaEvent.jclassname
    if objtype == "com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent":
        eventID = javaEvent.getEventID()
        event = dict()
        event["eventID"] = eventID
        event["startTime"] = toDate(javaEvent.getStartTime())
        event["endTime"] = toDate(javaEvent.getEndTime())
        event["phen"] = javaEvent.getPhenomenon()
        event["sig"] = javaEvent.getSignificance()
        event["subType"] = javaEvent.getSubtype()
        
        #
        # The subType is not guaranteed to always be present.
        if event["subType"] is not None and len(event["subType"]) > 0:
            event["type"] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance() + "." + javaEvent.getSubtype()
        else:
            event["type"] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance()
            
        event["state"] = str(javaEvent.getState()).lower()
        event["siteID"] = javaEvent.getSiteID()
        event["shapes"] = geometryConverter(javaEvent.getGeometry())

        
        attributes = javaEvent.getHazardAttributes()

        keys = attributes.keySet()
        iterator = keys.iterator()
            
        while iterator.hasNext():
            key = iterator.next()
            value = attributes.get(key)
            asPython = JUtil.javaObjToPyVal(value)
            event[str(key)] = asPython
                
    return event

def generatedProductConverter(javaGeneratedProduct):    
    objtype = javaGeneratedProduct.jclassname
    if objtype == "com.raytheon.uf.common.dataplugin.events.hazards.productGeneration.IGeneratedProduct":
        productID = javaGeneratedProduct.getProductID()
        asciiText = javaGeneratedProduct.getEntry('LegacyText').get(0)
        return {'productID': productID, 'asciiText': asciiText}

def toDate(date):
    result = date.getTime()
    return result

def asDatetime(dateAsMillis):
    result = datetime.datetime.fromtimestamp(dateAsMillis / 1000)
    return result

def geometryConverter(geometry):
    dict = {}
    dict["include"] = "true"
    dict["shapeType"] = "polygon"
    pointsList = []
    
    coordinates = geometry.getCoordinates()
    
    # Skip the last point since JSON representation doesn't repeat it.
    size = coordinates.__len__() - 1
    for i in range(size):
        coordinate = coordinates[i]
        pointsList.append([coordinate.x, coordinate.y])
        
    # Although polygons are stored in the Hazards JSON, they are referenced with
    # the name "points".
    dict["points"] = pointsList
    result = [dict]
    return result

def unicodeToString(input):
    if isinstance(input, dict):
        return {unicodeToString(key): unicodeToString(value) for key, value in input.iteritems()}
    elif isinstance(input, list):
        return [unicodeToString(element) for element in input]
    elif isinstance(input, unicode):
        return input.encode('utf-8')
    else:
        return input
        
class HazardEventJSONAdapter:


    def __init__(self):
        pass
    
    def setHazardEventManager(self, hazardEventManager):
        self.hazardEventManager = hazardEventManager
        
    def getEventsByFilter(self, filter):
        queryBuilder = HazardQueryBuilder()
        for filterKey in filter:
            values = filter.get(filterKey)
            if values is not None:
                for value in values:
                    keyValue = str(value)
                    
                    # Deal with the fact that the HazardEvents code requires an enumeration
                    # for HazardConstants.STATE but not other fields.
                    if filterKey == HazardConstants.STATE:
                        keyValue = HazardConstants.hazardStateFromString(keyValue)
                    queryBuilder.addKey(filterKey, keyValue)

        asJava = self.hazardEventManager.getEventsByFilter(queryBuilder.getQuery())
        result = JUtil.javaMapToPyDict(asJava, hazardHistoryConverter)
        return result
    
    def addJavaFilter(self, map, key, values):
        if values is not None:
            javaValues = ArrayList()
            for value in values:
                javaValues.add(str(value))
            map.put(key, javaValues)
            
    def removeAllEvents(self):
        filter = {}
        events = self.getEventsByFilter(filter)
        self.removeEvents(events)
        
    def removeEvents(self, events):
        eventsToRemove = ArrayList()
        for eventDictsKey in events:
            oldEvents = self.hazardEventManager.getByEventID(str(eventDictsKey))
            for i in range(oldEvents.size()):
                eventsToRemove.add(oldEvents.get(i))
        self.hazardEventManager.removeEvents(eventsToRemove)
        
    def storeEvents(self, eventDicts):
        self.removeEvents(eventDicts)
        
        hazardEventsAsJava = ArrayList()
        for eventDictsKey in eventDicts:               
            hazardEvent = EventFactory.createEvent()
            eventDict = eventDicts[eventDictsKey]
            eventDict = unicodeToString(eventDict)
            
            hazardAttributes = {}
            
            for key in eventDict:
                if key == "eventID":
                    eventID = eventDict[key]
                    hazardEvent.setEventID(eventID)
            
                elif key == "state":
                    hazardEvent.setHazardState(eventDict[key])
            
                elif key == "phen":
                    hazardEvent.setPhenomenon(eventDict[key])
                    
                elif key == "sig":
                    hazardEvent.setSignificance(eventDict[key])
                    
                elif key == "subType":
                    hazardEvent.setSubtype(eventDict[key])
                    
                elif key == "startTime":
                    time = asDatetime(eventDict[key])
                    hazardEvent.setStartTime(time)
                    
                    # For now set the issue time to the start time because
                    # the hazard event code currently requires an issue 
                    # time.  Potential events don't have issue times so 
                    # this needs to be re-thought.  See Issue #694.
                    hazardEvent.setIssueTime(time)
            
                elif key == "endTime":
                    time = asDatetime(eventDict[key])
                    hazardEvent.setEndTime(time)
            
                elif key == "siteID":
                    hazardEvent.setSiteID(eventDict[key])
            
                elif key == "shapes":
                    shapes = eventDict[key]
                    for shape in shapes:
                    
                        # For now, only handle polygons.
                        if shape["shapeType"] == "polygon":
                            points = shape["points"]
            
                            coordinates = list()
                            for point in points:
                                coordinates.append([point[0], point[1]])
                        
                            if coordinates:
                                # On write, we must close off the linear ring
                                coordinates.append(coordinates[0])
                        
                            geometry = GeometryFactory.createPolygon(coordinates, holes=None) 
                            hazardEvent.setGeometry(geometry)

                else:
                    hazardAttributes[key] = eventDict[key] 
            hazardEvent.setHazardAttributes(hazardAttributes)
            
            # See Issue #696
            hazardEvent.setHazardMode("T")
            
            hazardEventAsJava = JUtil.pyValToJavaObj(hazardEvent)
            
            # Need to set event ID so that it doesn't end up with the internal
            # value.
            hazardEventAsJava.setEventID(JUtil.pyValToJavaObj(eventID))
            hazardEventAsJava = self.hazardEventManager.createEvent(hazardEventAsJava)
            hazardEventsAsJava.add(hazardEventAsJava)
        self.hazardEventManager.storeEvents(hazardEventsAsJava)

    def getEventsList(self, eventDicts):
        hazardEventsAsJava = ArrayList()
        for eventDictsKey in eventDicts:
            oldEvents = self.hazardEventManager.getByEventID(str(eventDictsKey))
            
            if oldEvents.size() > 0:
                eventsToRemove = ArrayList()
                eventsToRemove.add(oldEvents.get(0))
                self.hazardEventManager.removeEvents(eventsToRemove)
                
            hazardEvent = EventFactory.createEvent()
            eventDict = eventDicts[eventDictsKey]
            eventDict = unicodeToString(eventDict)
            
            hazardAttributes = {}
            
            for key in eventDict:
                if key == "eventID":
                    eventID = eventDict[key]
                    hazardEvent.setEventID(eventID)
            
                elif key == "state":
                    hazardEvent.setHazardState(eventDict[key])
            
                elif key == "phen":
                    hazardEvent.setPhenomenon(eventDict[key])
                    
                elif key == "sig":
                    hazardEvent.setSignificance(eventDict[key])
                    
                elif key == "subType":
                    hazardEvent.setSubtype(eventDict[key])
                    
                elif key == "startTime":
                    time = asDatetime(eventDict[key])
                    hazardEvent.setStartTime(time)
                    hazardEvent.setIssueTime(time)
            
                elif key == "endTime":
                    time = asDatetime(eventDict[key])
                    hazardEvent.setEndTime(time)
            
                elif key == "siteID":
                    hazardEvent.setSiteID(eventDict[key])
            
                elif key == "shapes":
                    shapes = eventDict[key]
                    for shape in shapes:
                    
                        # For now, only handle polygons.
                        if shape["shapeType"] == "polygon":
                            points = shape["points"]
            
                            coordinates = list()
                            for point in points:
                                coordinates.append([point[0], point[1]])
                        
                            # On write, we must close off the linear ring
                            coordinates.append(coordinates[0])
                        
                            geometry = GeometryFactory.createPolygon(coordinates, holes=None) 
                            hazardEvent.setGeometry(geometry)

                else:
                    hazardAttributes[key] = eventDict[key] 
            hazardEvent.setHazardAttributes(hazardAttributes)
            
            hazardEvent.setHazardMode("T")
            
            hazardEventAsJava = JUtil.pyValToJavaObj(hazardEvent)
            
            hazardEventAsJava.setEventID(JUtil.pyValToJavaObj(eventID))
            hazardEventAsJava = self.hazardEventManager.createEvent(hazardEventAsJava)
            hazardEventsAsJava.add(hazardEventAsJava)
