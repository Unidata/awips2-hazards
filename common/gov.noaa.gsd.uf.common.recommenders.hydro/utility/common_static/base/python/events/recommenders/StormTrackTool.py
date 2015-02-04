"""
Storm Track Recommender

@since: June 2013
@author: JRamer
"""
import RecommenderTemplate
    

import datetime
import time
import math
import copy
import sys
from PointTrack import *
from GeneralConstants import *
import TrackToolCommon

class Recommender(TrackToolCommon.TrackToolCommon):

    def __init__(self):
        return

    def defineScriptMetadata(self):
        '''
        @return: Returns a python dictionary which defines basic information
        about the recommender, such as author, script version, and description
        '''
        metaDict = {}
        metaDict["toolName"] = "StormTrackTool"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Builds and calculates points for a storm track."
        metaDict["eventState"] = "Pending"
        return metaDict

    def defineDialog(self, eventSet):
        '''      
        @summary: Defines a dialog that will be presented to the user prior to 
        the recommender's execute routine.  Will use python maps to define
        widgets.  
        Each key within the map will defined a specific attribute for the widget.
        @return: Python map which correspond to attributes for widgets.
        '''
        return None

    def defineSpatialInfo(self):
        '''
        @summary: Determines spatial information needed by the recommender.
        @return: Unknown
        @todo: fix comments, further figure out spatial info
        '''
        resultDict = {"outputType":"spatialInfo",
                      "label":"Drag Me To Storm", "returnType":"Point"}
        return resultDict

    def indexOfClosest(self, value, values):
        '''
        @summary: Returns the index of the member of list 'values' that is
                  closest to 'value'
        @param value: Arbitrary number
        @param values: List of numbers.
        @return: closest index or -1 if values is empty list.
        '''
        closestIndex = -1
        closestDifference = 0
        tryIndex = 0
        for tryValue in values :
            tryDifference = abs(tryValue-value)
            if closestIndex<0 or tryDifference<closestDifference :
                closestDifference = tryDifference
                closestIndex = tryIndex
            tryIndex = tryIndex+1
        return closestIndex

    def getInitialEventDuration(self, eventSetAttributes) :
        '''
        @summary: Focal points can customize this method to change the initial
                  duration of the event.
        @param eventSetAttributes: sessionAttributes
        @return: Initial length of Hazard Event.
        '''
        # Use the default duration for the current hazard, but also assume it
        # does not make much sense for an event associated with a tracked
        # object to last more than three hours.
        initialDuration = eventSetAttributes.get( \
                             "defaultDuration", 3 * MILLIS_PER_HOUR )
        if initialDuration > 3 * MILLIS_PER_HOUR :
            initialDuration = 3 * MILLIS_PER_HOUR
        return initialDuration

    def getInitialWxEventMovement(self, duration, phenomena, significance, subType) :
        '''
        @summary: Focal points can customize this method to change the initial
                  motion of the weather event.
        @param duration: Duration of event
        @return: A dictionary containing the "speed" and "bearing".
        '''
        # For now default the initial motion and bearing.  The longer the
        # initial duration, the slower our initial motion.
        defaultSpeed = 20  # kts
        if phenomena[0]=="F" :
            defaultSpeed = 0
        motion = {}
        motion["speed"] = defaultSpeed * 30 * MILLIS_PER_MINUTE / duration
        motion["bearing"] = 225  # from SW
        return motion

    def initializeTypeOfEvent(self, sessionAttributes) :
        '''
        @summary: Focal points can customize this method to change the initial
                  phenomena, significance, subtype, and phensig.
        @param sessionAttributes: session attributes.
        @return: A tuple containing the phenomena, significance, subtype,
                 and phensig.
        '''
        
        eventType = sessionAttributes.get('eventType')
        if eventType:
            if  '.' in eventType:
                eventTypeFields = eventType.split('.')
                phenomena, significance = eventTypeFields[0], eventTypeFields[1]
            
                if len(eventTypeFields) == 3:
                    subType = eventTypeFields[2]
                else:
                    subType = None
                
                phenSig = '.'.join([phenomena, significance])
                
                return ( phenomena, significance, subType, phenSig )
           
        
        # Pickup the hazard type stuff from the event that was passed in
        # if that was possible.  Otherwise, for now we default it to convective
        # flash flood warning.
        phenomena = "FF"
        significance = "W"
        subType = "Convective"
        if "phenomena" in sessionAttributes :
            val = sessionAttributes["phenomena"]
            if isinstance(val, str) and val != "" :
                phenomena = val
                subType = ""
        if "significance" in sessionAttributes :
            val = sessionAttributes["significance"]
            if isinstance(val, str) and val != "" :
                significance = val
                subType = ""
        if "subType" in sessionAttributes :
            val = sessionAttributes["subType"]
            if isinstance(val, str) and val != "" :
                subType = val
        phenSig = phenomena+"."+significance
        return ( phenomena, significance, subType, phenSig )

    def updateEventAttributes(self, sessionAttributes, dialogInputMap, \
                                spatialInputMap):
        '''
        @param sessionAttributes: Session attributes associated with eventSet.
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() routine
        @param spatialInputMap: A map containing spatial input as created by the 
        definedSpatialInfo() routine
        @return: updated session attributes.
        '''

        # Call method that initializes the phenomena, significance, and subtype
        # for hazard. This is a reasonable thing for a focal point to customize.
        ( phenomena, significance, subType, phenSig ) = \
                 self.initializeTypeOfEvent(sessionAttributes)

        # Call method that sets the initial event duration in milliseconds.
        # This is a reasonable thing for a focal point to customize.
        initialDuration = self.getInitialEventDuration(sessionAttributes)

        # Pull the rest of the time info we need out of the session info.
        framesInfo = sessionAttributes.get("framesInfo")
        sessionFrameTimes = framesInfo["frameTimeList"]
        lastFrameIndex = len(sessionFrameTimes) - 1
        currentTime = long(sessionAttributes["currentTime"])
        startTime = currentTime
        endTime = startTime + initialDuration

        # Get information about 'dragmeto' point. In previous incarnations,
        # there has been some uncertainty as to what the units of the
        # draggedPointTime are.
        spatialInput = spatialInputMap["spatialInfo"]

        points = spatialInput["points"]
        draggedPointTuple = points[0]
        draggedPoint = draggedPointTuple[0]
        draggedPointTime = long(draggedPointTuple[1])
        if draggedPointTime < VERIFY_MILLISECONDS :
            draggedPointTime = draggedPointTime * MILLIS_PER_SECOND

        # Call method that sets the initial motion of the weather event.
        # This is a reasonable thing for a focal point to customize.
        stormMotion = self.getInitialWxEventMovement(\
             initialDuration, phenomena, significance, subType)

        # Construct a PointTrack object, and reinitialize it with the default
        # motion located where our starting point was dragged to.
        pointTrack = PointTrack()
        latLon0 = LatLonCoord(draggedPoint[0], draggedPoint[1])
        motion0 = Motion(stormMotion["speed"], stormMotion["bearing"])
        pointTrack.latLonMotionOrigTimeInit_(\
             latLon0, draggedPointTime, motion0, startTime)
        pivotIndex = self.indexOfClosest(draggedPointTime, sessionFrameTimes)
        pivotTime = sessionFrameTimes[pivotIndex]
        pivotTimeList = [ pivotTime ]
        pivotList = [ pivotIndex ]
        trackCoordinates = []

        # Create a list of tracking points for each frame. Reuse the lat/lon for
        # consecutive frames less than a minute apart, mostly to deal with All
        # Tilts radar.
        shapeList = []
        prevTime = None
        for frameTime in sessionFrameTimes :
            if not self.sameMinute(frameTime, prevTime) :
                frameLatLon = pointTrack.trackPoint(self.minuteOf(frameTime))
            trackPointShape = {}
            trackPointShape["pointType"] = "tracking"
            trackPointShape["shapeType"] = "point"
            trackPointShape["pointID"] = frameTime
            trackPointShape["point"] = [frameLatLon.lon, frameLatLon.lat]
            shapeList.append(trackPointShape)
            trackCoordinates.append([frameLatLon.lon, frameLatLon.lat])

        # Reacquire the motion from the start of the event instead of the
        # last frame.  In most cases wont make much difference, but can once
        # we start supporting non-linear tracking.
        if stormMotion["speed"] != 0 :
            motion0 = pointTrack.speedAndAngleOf(startTime)
            stormMotion["speed"] = motion0.speed
            stormMotion["bearing"] = motion0.bearing

        # Compute a working frame count, ignoring frames with a less than
        # a minute separating them, mostly to deal with All Tilts radar.
        lastWorkingFrameIndex = -1
        prevTime = None
        for sesFrTime in sessionFrameTimes :
            if not self.sameMinute(sesFrTime, prevTime) :
                lastWorkingFrameIndex += 1
            prevTime = sesFrTime

        # Compute a working delta time between frames.
        if lastWorkingFrameIndex < 1 :
            frameTimeStep = self.minuteOf(endTime) - self.minuteOf(startTime)
        else :
            frameTimeSpan = \
                 self.minuteOf(sessionFrameTimes[lastFrameIndex]) - \
                 self.minuteOf(sessionFrameTimes[0])
            frameTimeStep = frameTimeSpan / lastWorkingFrameIndex

        # Make a list of times for projected points for the track that are shown
        # to the user.  We want the last future time to be exactly at the end of
        # the hazard, and we do not want the first future time to be too close
        # to the last frame, so we add the buffer of 1/3 the time step.
        futureTimeList = [endTime]
        nexttime = endTime - frameTimeStep
        endFrameTime = sessionFrameTimes[lastFrameIndex]
        endFrameTime = endFrameTime + frameTimeStep / 3
        while nexttime > endFrameTime :
            futureTimeList.insert(0, nexttime)
            nexttime = nexttime - frameTimeStep

        # Create a list of tracking points for each future point.
        for futureTime in futureTimeList :
            futureLatLon = pointTrack.trackPoint(futureTime)
            futurePointShape = {}
            futurePointShape["pointType"] = "tracking"
            futurePointShape["shapeType"] = "point"
            futurePointShape["point"] = [futureLatLon.lon, futureLatLon.lat]
            futurePointShape["pointID"] = futureTime
            shapeList.append(futurePointShape)
            trackCoordinates.append([futureLatLon.lon, futureLatLon.lat])

        # now get the polygon
        if stormMotion["speed"] != 0 :
            latLonPoly = pointTrack.polygonDef(startTime, endTime)
        else :
            latLonPoly = pointTrack.enclosedBy(startTime, endTime, \
                                               15.0, 15.0, 15.0, 15.0)
        hazardPolygon = []
        for latLonVertex in latLonPoly :
            hazardPolygon.append([latLonVertex.lon, latLonVertex.lat])

        # Finalize our set of output attributes.
        resultDict = {}
        resultDict["modifyCallbackToolName"] = "ModifyStormTrackTool"
        resultDict["creationTime"] = currentTime
        resultDict["trackPoints"] = shapeList
        resultDict["startTime"] = startTime
        resultDict["endTime"] = endTime
        resultDict["lastFrameTime"] = sessionFrameTimes[lastFrameIndex]
        resultDict["status"] = "pending"
        resultDict["stormMotion"] = stormMotion
        resultDict["pivots"] = pivotList
        resultDict["pivotTimes"] = pivotTimeList
        resultDict["type"] = phenSig

        # Cache some stuff the logic that composes the returned Java backed
        # HazardEvent object needs. We will make this a temporary member of the
        # attributes, which the parent method execute() will delete. This way,
        # the correctness of this information can be evaluated in unit tests.
        forJavaObj = {}
        forJavaObj["SiteID"] = sessionAttributes["siteID"]
        forJavaObj["currentTime"] = currentTime
        forJavaObj["startTime"] = startTime
        forJavaObj["endTime"] = endTime
        forJavaObj["phenomena"] = phenomena
        forJavaObj["significance"] = significance
        forJavaObj["subType"] = subType
        forJavaObj["track"] = trackCoordinates
        forJavaObj["hazardPolygon"] = hazardPolygon
        resultDict["forJavaObj"] = forJavaObj


        return resultDict

    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        '''
        @eventSet: List of objects that was converted from Java IEvent objects
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() routine
        @param spatialInputMap: A map containing spatial input as created by the 
        definedSpatialInfo() routine
        @return: List of objects that will be later converted to Java IEvent
        objects
        '''

        # for a unit test these will fail as we will not have JEP available, so
        # we import them in here
        import EventFactory
        import GeometryFactory

        # Pick up the existing event from what is passed in.  If there is an event
        # we assume WarnGen type workflow, otherwise we will define a default event
        # type in here.
        layerEventId = spatialInputMap.get("eventID")
        events = eventSet.getEvents()
        haveEvent = False
        for event in events :
            if event.getEventID() == layerEventId :
                hazardEvent = event
                haveEvent = True
                break

        # Artificially inject the initial hazard type into the attributes.
        sessionAttributes = eventSet.getAttributes()
        if haveEvent :
            try :
                sessionAttributes["phenomena"] = hazardEvent.getPhenomenon()
            except :
                pass
            try :
                sessionAttributes["significance"] = hazardEvent.getSignificance()
            except :
                pass
            try :
                sessionAttributes["subType"] = hazardEvent.getSubType()
            except :
                pass

        # It is no longer possible to get the default event duration from the event
        # set attributes, so we get that from the Bridge here and push it in.
        try :
            ( phenomena, significance, subType, phenSig ) = \
                 self.initializeTypeOfEvent(sessionAttributes)
            import Bridge
            bridge = Bridge.Bridge()
            if subType is not None and len(subType) > 0:
                hazardTypeEntry = bridge.getHazardTypes( phenSig + "." + subType)
            else :
                hazardTypeEntry = bridge.getHazardTypes( phenSig )
            try :
                dur = hazardTypeEntry['defaultDuration']
            except :
                dur = MILLIS_PER_HOUR
            sessionAttributes["defaultDuration"] = dur
        except :
            import traceback
            import sys
            tbData = traceback.format_exc()
            sys.stderr.write(tbData)
            sessionAttributes["defaultDuration"] = 3 * MILLIS_PER_HOUR

        # Presence of java backed objects in the attributes complicates unit testing,
        # so unravel any of these.
        try :
            sessionAttributes["framesInfo"]["currentFrame"] = \
              str(sessionAttributes["framesInfo"]["currentFrame"])
        except :
            del sessionAttributes["framesInfo"]["currentFrame"]

        # updateEventAttributes does all the stuff we can safely do in a unit
        # test, basically whatever does not require Jep
        resultDict = self.updateEventAttributes(sessionAttributes, \
                                      dialogInputMap, spatialInputMap)

        # Peel out stuff that is piggybacking on the attributes but is really
        # meant to go into the HazardEvent.
        forJavaObj = resultDict["forJavaObj"]
        del resultDict["forJavaObj"]

        # Start creating our HazardEvent object, which is really backed by a
        # Java object.  str cast accounts for occasional json promotion of ascii
        # strings to unicode strings, which makes JEP barf.
        if not haveEvent :
            hazardEvent = EventFactory.createEvent()
            hazardEvent.setEventID("")
            hazardEvent.setSiteID(str(forJavaObj["SiteID"]))
            hazardEvent.setHazardStatus("PENDING")

        # New recommender framework requires some datetime objects, which must
        # be in units of seconds.
        if not haveEvent :
            hazardEvent.setCreationTime(datetime.datetime.fromtimestamp(\
              forJavaObj["currentTime"] / MILLIS_PER_SECOND))
            hazardEvent.setStartTime(datetime.datetime.fromtimestamp(\
              forJavaObj["startTime"] / MILLIS_PER_SECOND))
            hazardEvent.setEndTime(datetime.datetime.fromtimestamp(\
              forJavaObj["endTime"] / MILLIS_PER_SECOND))

        if not haveEvent :
            hazardEvent.setPhenomenon(forJavaObj["phenomena"])
            if forJavaObj["subType"] != "" :
                hazardEvent.setSubType(forJavaObj["subType"])
            hazardEvent.setSignificance(forJavaObj["significance"])

        #
        # The track should not be considered to be an actual hazard geometry.
        # The hazard geometry is just the the polygon. Creating a line geometry
        # as an attribute is a convenient way to return track point information
        # to the client.
        resultDict["stormTrackLine"] = \
            GeometryFactory.createLineString(forJavaObj["track"])
        geometry = GeometryFactory.createPolygon(forJavaObj["hazardPolygon"])
        hazardEvent.setGeometry(GeometryFactory.createCollection([geometry]))

        hazardEvent.setHazardMode("O")
        hazardEvent.setHazardAttributes(resultDict)

        return hazardEvent

    def toString(self):
        return "Storm Track Tool!"    
