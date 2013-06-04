import traceback
try:
    import JUtil
    import GeometryFactory
    import EventFactory
    from HazardConstants import *
    from java.util import ArrayList
    from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants
    from com.raytheon.uf.common.dataplugin.events.hazards.datastorage import HazardQueryBuilder
except:
    tbData = traceback.format_exc()
    print tbData

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


ENCLOSED = True

def setEnclosed(trueFalse):
    global ENCLOSED
    ENCLOSED = trueFalse
    
def hazardHistoryConverter(obj):
    objtype = obj.jclassname
    if objtype == "com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList":
        javaEvent = obj.get(0)
        eventID = javaEvent.getEventID()
        event = dict()
        event[EVENT_ID] = eventID
        event[START_TIME] = toDate(javaEvent.getStartTime())
        event[END_TIME] = toDate(javaEvent.getEndTime())
        event[PHENOMENON] = javaEvent.getPhenomenon()
        event[SIGNIFICANCE] = javaEvent.getSignificance()
        event[SUBTYPE] = javaEvent.getSubtype()
        
        #
        # The subtype is not guaranteed to always be present.
        if event[SUBTYPE] is not None and len(event[SUBTYPE]) > 0:
            event[HAZARD_TYPE] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance() + "." + javaEvent.getSubtype()
        else:
            event[HAZARD_TYPE] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance()
            
        event[STATE] = str(javaEvent.getState()).lower()
        event[SITE_ID] = javaEvent.getSiteID()
        event[SHAPES] = geometryConverter(javaEvent.getGeometry())
        
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
    if objtype in ["com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent",
                   "gov.noaa.gsd.viz.hazards.events.HazardServicesEvent"]:
        eventID = javaEvent.getEventID()
        event = dict()
        event[EVENT_ID] = eventID
        event[START_TIME] = toDate(javaEvent.getStartTime())
        event[END_TIME] = toDate(javaEvent.getEndTime())
        event[PHENOMENON] = javaEvent.getPhenomenon()
        event[SIGNIFICANCE] = javaEvent.getSignificance()
        event[SUBTYPE] = javaEvent.getSubtype()        
        #
        # The subType is not guaranteed to always be present.
        if event[SUBTYPE] is not None and len(event[SUBTYPE]) > 0:
            event[HAZARD_TYPE] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance() + "." + javaEvent.getSubtype()
        else:
            event[HAZARD_TYPE] = javaEvent.getPhenomenon() + "." + javaEvent.getSignificance()
            
        event[STATE] = str(javaEvent.getState()).lower()
        event[SITE_ID] = javaEvent.getSiteID()
        event[SHAPES] = geometryConverter(javaEvent.getGeometry())

        
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
    if objtype in["com.raytheon.uf.common.dataplugin.events.hazards.productGeneration.IGeneratedProduct", 
                  "com.raytheon.uf.common.hazards.productgen.GeneratedProduct"]:
        productID = javaGeneratedProduct.getProductID()
        jEntries = javaGeneratedProduct.getEntries()
        pyEntries = JUtil.javaMapToPyDict(jEntries)
        return {'productID': productID, 'entries': pyEntries}

def toDate(date):
    result = date.getTime()
    return result

def asDatetime(dateAsMillis):
    result = datetime.datetime.fromtimestamp(dateAsMillis / 1000)
    return result

def geometryConverter(geometry):
    """
    Converts a geometry object into a list of
    shape dictionaries.  This supports the
    case where an event may have more than
    one polygon associated with it.
    
    @param  geometry: The geometry object to convert.
    """
    global ENCLOSED

    geometryList = []
    geometryCount = geometry.getNumGeometries()
    
    for i in range(geometryCount):
        geometryList.append(geometry.getGeometryN(i))

    shapeList = []
        
    for geometry in geometryList:     
        dict = {}
        dict[INCLUDE] = "true"
        dict[SHAPE_TYPE] = POLYGON
        pointsList = []
    
        coordinates = geometry.getCoordinates()
    
        if ENCLOSED:
            # Skip the last point since JSON representation doesn't repeat it.
            size = coordinates.__len__() - 1
        else:
            size = coordinates.__len__()
            
        for i in range(size):
            coordinate = coordinates[i]
            pointsList.append([coordinate.x, coordinate.y])
        
            # Although polygons are stored in the Hazards JSON, they are referenced with
            # the name POINTS.
        dict[POINTS] = pointsList
        shapeList.append(dict)
    return shapeList

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
                if key == EVENT_ID:
                    eventID = eventDict[key]
                    hazardEvent.setEventID(eventID)
            
                elif key == STATE:
                    hazardEvent.setHazardState(eventDict[key])
            
                elif key == PHENOMENON:
                    hazardEvent.setPhenomenon(eventDict[key])
                    
                elif key == SIGNIFICANCE:
                    hazardEvent.setSignificance(eventDict[key])
                    
                elif key == SUBTYPE:
                    hazardEvent.setSubtype(eventDict[key])
                    
                elif key == START_TIME:
                    time = asDatetime(eventDict[key])
                    hazardEvent.setStartTime(time)
                    
                    # For now set the issue time to the start time because
                    # the hazard event code currently requires an issue 
                    # time.  Potential events don't have issue times so 
                    # this needs to be re-thought.  See Issue #694.
                    hazardEvent.setIssueTime(time)
            
                elif key == END_TIME:
                    time = asDatetime(eventDict[key])
                    hazardEvent.setEndTime(time)
            
                elif key == SITE_ID:
                    hazardEvent.setSiteID(eventDict[key])
            
                elif key == SHAPES:
                    shapes = eventDict[key]
                    polygonList = list()
                    
                    for shape in shapes:
                    
                        # For now, only handle polygons.
                        if shape[SHAPE_TYPE] == POLYGON:
                            points = shape[POINTS]
            
                            coordinates = list()
                            for point in points:
                                coordinates.append([point[0], point[1]])
                        
                            if coordinates:
                                # On write, we must close off the linear ring
                                coordinates.append(coordinates[0])
                        
                            geometry = GeometryFactory.createPolygon(coordinates, holes=None)
                            polygonList.append(geometry)
                    
                    if len(polygonList) == 1:         
                        hazardEvent.setGeometry(polygonList[0])
                    elif len(polygonList) > 0:
                        multiPolygon = GeometryFactory.createMultiPolygon(polygonList, 'polygons')
                        hazardEvent.setGeometry(multiPolygon)

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
                if key == EVENT_ID:
                    eventID = eventDict[key]
                    hazardEvent.setEventID(eventID)
            
                elif key == STATE:
                    hazardEvent.setHazardState(eventDict[key])
            
                elif key == PHENOMENON:
                    hazardEvent.setPhenomenon(eventDict[key])
                    
                elif key == SIGNIFICANCE:
                    hazardEvent.setSignificance(eventDict[key])
                    
                elif key == SUBTYPE:
                    hazardEvent.setSubtype(eventDict[key])
                    
                elif key == START_TIME:
                    time = asDatetime(eventDict[key])
                    hazardEvent.setStartTime(time)
                    hazardEvent.setIssueTime(time)
            
                elif key == END_TIME:
                    time = asDatetime(eventDict[key])
                    hazardEvent.setEndTime(time)
            
                elif key == SITE_ID:
                    hazardEvent.setSiteID(eventDict[key])
            
                elif key == SHAPES:
                    shapes = eventDict[key]
                    for shape in shapes:
                    
                        # For now, only handle polygons.
                        if shape[SHAPE_TYPE] == POLYGON:
                            points = shape[POINTS]
            
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
