"""
Storm Track Recommender

@since: June 2013
@author: JRamer

Modifications by Chris Golden, June 2016. The functionality of the ModifyStormTrackTool
is now merged into this recommender in the process of adapting storm track functionality
to the new, generic interface between recommenders and Hazard Services, using visual
features instead of special-case Java code in the latter's core to allow the storm track
tool to be user-interactive.
"""
import RecommenderTemplate
from HazardConstants import SELECTED_TIME_KEY

import datetime
import time
import math
import copy
import sys
from PointTrack import *
from GeneralConstants import *
import TimeUtils
import AdvancedGeometry
from VisualFeatures import VisualFeatures
import GeometryFactory
import EventFactory
import logging, UFStatusHandler
from HazardEventLockUtils import HazardEventLockUtils

class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        self.hazardEventLockUtils = None
        self.logger = logging.getLogger('StormTrackTool')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.uf.common.recommenders', 'StormTrackTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)

    def defineScriptMetadata(self):
        '''
        @summary Get metadata for this recommender.
        @return: Dictionary which defines basic information about the recommender,
        such as author, script version, and description.
        '''
        
        metaDict = {}
        metaDict["toolName"] = "StormTrackTool"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Builds and calculates points for a storm track."
        metaDict["eventState"] = "Pending"
        metaDict["getDialogInfoNeeded"] = False
        return metaDict

    def defineSpatialInfo(self, eventSet, visualFeatures, collecting):
        '''
        @summary: Determines spatial information needed by the recommender. Each time
        this recommender is executed, this method is called prior to the execute().
        It will be called with None for the visualFeatures parameter and True for the
        collecting parameter.
       
        Because only one round of visual feature modification is allowed for the
        generated visual features, it will be called one more time after the first
        time, this time to generate read-only features displayed while the recommender
        is executing. This time, the visual feature as modified by the user will be
        provided as a parameter, and collecting will be False.
        
        @param eventSet: Attributes providing the execution context of the recommender.
        @param visualFeatures: Visual features returned by the previous call to this
        method, if any, with any modification made by the user applied.
        @param collecting: Flag indicating whether or not the visual features to be
        generated, if any, are for collecting information from the user, or (if the
        last call to the method for this execution) simply for displaying information
        to the user.
        @return: Visual features to be used by the user to provide spatial input;
        may be empty.
        '''
        
        # If this call is intended to generate read-only visual features,
        # simply take the input visual feature and make it uneditable,
        # and change its text.
        if not collecting:
            visualFeatures[0]["dragCapability"] = "none"
            visualFeatures[0]["label"] = "Generating recommendations..."
            return visualFeatures
        
        # If the trigger was a vanilla execution of the recommender, or a
        # hazard-type-first execution, return a visual feature that will
        # allow the user to specify the starting point of the storm.
        # Otherwise, return nothing, as an existing hazard event's
        # modification is triggering the recommender execution, and thus
        # the storm track already exists.
        if str(eventSet.getAttributes().get("trigger")) in ["none", "hazardTypeFirst"]:
            centerPointLatLon = eventSet.getAttributes().get("centerPointLatLon")
            centerPoint = GeometryFactory.createPoint([float(str(centerPointLatLon.get("lon"))),
                                                       float(str(centerPointLatLon.get("lat")))])
            centerPoint = AdvancedGeometry.createShapelyWrapper(centerPoint, 0)
            eventType = eventSet.getAttributes().get("eventType")
            if eventType is not None:
                label = "Drag to " + str(eventType) + " location"
            else:
                label = "Drag to hazard location"
            return VisualFeatures([
                                   {
                                    "identifier": "stormTrackDot",
                                    "geometry": centerPoint,
                                    "borderThickness": 2,
                                    "fillColor": { "red": 1.0, "green": 1.0, "blue": 1.0, "alpha": 0.8 },
                                    "diameter": 10,
                                    "textOffsetLength": 12,
                                    "label": label,
                                    "textSize": 14,
                                    "dragCapability": "all"
                                    }
                                   ])
        return None
        
    def indexOfClosest(self, value, values):
        '''
        @summary: Get the index of the member of list 'values' that is
                  closest to 'value'
        @param value: Arbitrary number.
        @param values: List of numbers.
        @return: Closest index, or -1 if values is empty list.
        '''
        
        closestIndex = -1
        closestDifference = 0
        tryIndex = 0
        for tryValue in values:
            tryDifference = abs(tryValue-value)
            if closestIndex<0 or tryDifference<closestDifference:
                closestDifference = tryDifference
                closestIndex = tryIndex
            tryIndex = tryIndex+1
        return closestIndex

    def getInitialEventDuration(self, eventSetAttributes):
        '''
        @summary: Get the initial duration of the event. Focal points can customize
        this method to change the initial duration.
        @param eventSetAttributes: sessionAttributes
        @return: Initial length in time of hazard event.
        '''
        
        # Use the default duration for the current hazard, but also assume it
        # does not make much sense for an event associated with a tracked
        # object to last more than three hours.
        initialDuration = eventSetAttributes.get( \
                             "defaultDuration", 3 * MILLIS_PER_HOUR )
        if initialDuration > 3 * MILLIS_PER_HOUR:
            initialDuration = 3 * MILLIS_PER_HOUR
        return initialDuration

    def getInitialWxEventMovement(self, duration, phenomena, significance, subType):
        '''
        @summary: Get the initial weather event motion. Focal points can customize
        this method to change the initial motion.
        @param duration: Duration of event.
        @return: A dictionary containing the "speed" and "bearing".
        '''
        
        # For now default the initial motion and bearing.  The longer the
        # initial duration, the slower our initial motion.
        defaultSpeed = 20  # kts

#
# TODO: COMMENTED OUT JUST FOR TESTING.
#
#         if phenomena[0]=="F":
#             defaultSpeed = 0

        motion = {}
        motion["speed"] = defaultSpeed * 30 * MILLIS_PER_MINUTE / duration
        motion["bearing"] = 225  # from SW
        return motion

    def initializeTypeOfEvent(self, sessionAttributes):
        '''
        @summary: Get the type of the event as a tuple. Focal points can customize
        this method to change the initial type.
        @param sessionAttributes: Session attributes.
        @return: Tuple containing the phenomena, significance, subtype,
                 and phensig, respectively.
        '''
        
        # Pickup the hazard type info from the event that was passed in if that
        # is possible.
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
           
        
        # Since no hazard type was found, for now default to convective flash
        # flood warning.
        phenomena = "FF"
        significance = "W"
        subType = "Convective"
        if "phenomena" in sessionAttributes:
            val = sessionAttributes["phenomena"]
            if isinstance(val, str) and val != "":
                phenomena = val
                subType = ""
        if "significance" in sessionAttributes:
            val = sessionAttributes["significance"]
            if isinstance(val, str) and val != "":
                significance = val
                subType = ""
        if "subType" in sessionAttributes:
            val = sessionAttributes["subType"]
            if isinstance(val, str) and val != "":
                subType = val
        phenSig = phenomena+"."+significance
        return ( phenomena, significance, subType, phenSig )

    def initializeEventAttributes(self, phenomena, significance, subType, \
                                  sessionAttributes, visualFeatures):
        '''
        @summary Initialize the attributes of an event. This is called when the
        recommender is invoked manually by the user, either directly or via the
        hazard-type-first mechanism. It is not used when a track point is dragged
        by the user.
        @param phenomena: Hazard event phenomenon.
        @param significance: Hazard event significance.
        @param subType: Hazard event subtype, if any.
        @param sessionAttributes: Session attributes associated with the event set
        that was passed to the recommender.
        @param visualFeatures: Visual features as defined by the defineSpatialInfo()
        method and modified by the user to provide spatial input.
        @return: Initialized session attributes.
        '''

        # Set the initial event duration in milliseconds. This is a reasonable
        # method for a focal point to customize.
        initialDuration = self.getInitialEventDuration(sessionAttributes)

        # Pull the rest of the time info we need out of the session info.
        framesInfo = sessionAttributes.get("framesInfo")
        sessionFrameTimes = framesInfo["frameTimeList"]
        lastFrameIndex = len(sessionFrameTimes) - 1
        currentTime = long(sessionAttributes["currentTime"])
        startTime = currentTime
        endTime = startTime + initialDuration

        # Get the point that the storm dot was dragged to, and the selected
        # time in milliseconds.
        draggedPoint = visualFeatures[0]["geometry"].asShapely()[0]
        draggedPointTime = sessionAttributes.get(SELECTED_TIME_KEY)

        # Call method that sets the initial motion of the weather event. This
        # is a reasonable method for a focal point to customize.
        stormMotion = self.getInitialWxEventMovement(\
             initialDuration, phenomena, significance, subType)

        # Construct a PointTrack object, and reinitialize it with the default
        # motion located where our starting point was dragged to.
        pointTrack = PointTrack()
        latLon0 = LatLonCoord(draggedPoint.y, draggedPoint.x)
        motion0 = Motion(stormMotion["speed"], stormMotion["bearing"])
        pointTrack.latLonMotionOrigTimeInit_(latLon0, draggedPointTime, motion0, startTime)
        pivotIndex = self.indexOfClosest(draggedPointTime, sessionFrameTimes)
        pivotTime = sessionFrameTimes[pivotIndex]
        pivotTimeList = [pivotTime]
        pivotList = [pivotIndex]
        trackCoordinates = []

        # Create a list of tracking points for each frame. Reuse the lat/lon for
        # consecutive frames less than a minute apart, mostly to deal with All
        # Tilts radar.
        shapeList = []
        prevTime = None
        for frameTime in sessionFrameTimes:
            if not TimeUtils.isSameMinute(frameTime, prevTime):
                frameLatLon = pointTrack.trackPoint(TimeUtils.minuteOf(frameTime))
            trackPointShape = {}
            trackPointShape["pointType"] = "tracking"
            trackPointShape["shapeType"] = "point"
            trackPointShape["pointID"] = frameTime
            trackPointShape["point"] = [frameLatLon.lon, frameLatLon.lat]
            shapeList.append(trackPointShape)
            trackCoordinates.append([frameLatLon.lon, frameLatLon.lat])

        # Reaquire the motion from the start of the event instead of the last
        # frame.  In most cases this won't make much difference, but it can if
        # non-linear tracking is supported at some point.
        if stormMotion["speed"] != 0:
            motion0 = pointTrack.speedAndAngleOf(startTime)
            stormMotion["speed"] = motion0.speed
            stormMotion["bearing"] = motion0.bearing

        # Compute a working frame count, ignoring frames with a less than a
        # minute separating them, mostly to deal with All Tilts radar.
        lastWorkingFrameIndex = -1
        prevTime = None
        for sesFrTime in sessionFrameTimes:
            if not TimeUtils.isSameMinute(sesFrTime, prevTime):
                lastWorkingFrameIndex += 1
            prevTime = sesFrTime

        # Compute a working delta time between frames.
        if lastWorkingFrameIndex < 1:
            frameTimeStep = TimeUtils.minuteOf(endTime) - TimeUtils.minuteOf(startTime)
        else:
            frameTimeSpan = TimeUtils.minuteOf(sessionFrameTimes[lastFrameIndex]) - \
                 TimeUtils.minuteOf(sessionFrameTimes[0])
            frameTimeStep = frameTimeSpan / lastWorkingFrameIndex

        # Make a list of times for projected points for the track that are shown
        # to the user. The last future time should not be exactly at the end of
        # the hazard, and the first future time should not be too close to the
        # last frame, so the buffer of 1/3 the time step is added.
        futureTimeList = [endTime]
        nexttime = endTime - frameTimeStep
        endFrameTime = sessionFrameTimes[lastFrameIndex]
        endFrameTime = endFrameTime + frameTimeStep / 3
        while nexttime > endFrameTime:
            futureTimeList.insert(0, nexttime)
            nexttime = nexttime - frameTimeStep

        # Create a list of tracking points for each future point.
        for futureTime in futureTimeList:
            futureLatLon = pointTrack.trackPoint(futureTime)
            futurePointShape = {}
            futurePointShape["pointType"] = "tracking"
            futurePointShape["shapeType"] = "point"
            futurePointShape["point"] = [futureLatLon.lon, futureLatLon.lat]
            futurePointShape["pointID"] = futureTime
            shapeList.append(futurePointShape)
            trackCoordinates.append([futureLatLon.lon, futureLatLon.lat])

        # Get the polygon.
        if stormMotion["speed"] != 0:
            latLonPoly = pointTrack.polygonDef(startTime, endTime)
        else:
            latLonPoly = pointTrack.enclosedBy(startTime, endTime, \
                                               15.0, 15.0, 15.0, 15.0)
        hazardPolygon = []
        for latLonVertex in latLonPoly:
            hazardPolygon.append([latLonVertex.lon, latLonVertex.lat])

        # Finalize our set of output attributes.
        resultDict = {}
        resultDict["trackPoints"] = shapeList
        resultDict["lastFrameTime"] = sessionFrameTimes[lastFrameIndex]
        resultDict["status"] = "pending"
        resultDict["stormMotion"] = stormMotion
        resultDict["pivots"] = pivotList
        resultDict["pivotTimes"] = pivotTimeList
        resultDict["track"] = trackCoordinates

        # Cache some stuff that the logic that composes the returned Java-backed
        # HazardEvent object needs. This is made a temporary member of the
        # attributes, which the parent method execute() will delete. This way,
        # the correctness of this information can be evaluated in unit tests.
        forJavaObj = {}
        forJavaObj["siteID"] = sessionAttributes["siteID"]
        forJavaObj["currentTime"] = currentTime
        forJavaObj["startTime"] = startTime
        forJavaObj["endTime"] = endTime
        forJavaObj["phenomena"] = phenomena
        forJavaObj["significance"] = significance
        forJavaObj["subType"] = subType
        forJavaObj["hazardPolygon"] = hazardPolygon
        resultDict["forJavaObj"] = forJavaObj

        return resultDict

    def updateEventAttributes(self, visualFeature, sessionAttributes, hazardEvent):
        '''
        @summary Update event attributes in response to a track point visual feature
        drag.
        @param visualFeature: Visual feature representing a track point that was
        dragged. Its identifier, when converted to a long integer, is an epoch time
        in milliseconds.
        @param sessionAttributes: Session attributes associated with the event set
        passed into the recommender.
        @param hazardEvent Hazard event being modified.
        @return: updated event attributes.
        '''

        #
        # TODO: It is probably possible to share code between this method and
        # the initializeEventAttributes() method defined above. This method
        # is taken, with few modifications, from the previously-separate
        # ModifyStormTrackTool, whereas the other method is from the original
        # StormTrackTool.  
        #
        
        # Note that since there are hidden millisecond to second conversions
        # (and vice-versa) in the underlying code, we always allow a slop of
        # 999 milliseconds when comparing two millisecond times for equivalence.
        
        # Get the frame information, current time, start time, and end time.
        framesInfo = sessionAttributes.get("framesInfo")
        sessionFrameTimes = framesInfo["frameTimeList"]
        lastFrameIndex = len(sessionFrameTimes) - 1
        currentTime = long(sessionAttributes["currentTime"])
        currentFrameIndex = framesInfo["frameIndex"]
        currentFrameTime = sessionFrameTimes[currentFrameIndex]
        startTime = TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getStartTime())
        endTime = TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getEndTime())

        # Get the new location of the point that was dragged, and its identifier,
        # which is also an epoch time in milliseconds.
        draggedPoint = visualFeature["geometry"].values()[0].asShapely()[0]
        draggedPointTime = long(visualFeature["identifier"])

        # Filter our shape list to remove any points that do not match a frame,
        # and note information about the earliest and latest point, plus the
        # polygon.
        eventAttributes = hazardEvent.getHazardAttributes()
        eventShapes = eventAttributes.get("trackPoints", [])
        shapeList = []
        time1 = 0
        time2 = 0
        for eventShape in eventShapes:
            if eventShape.get("shapeType") != "point":
                continue
            shapeTime = eventShape.get("pointID", 0)
            if shapeTime == 0:
                continue
            shapePoint = eventShape.get("point", [])
            if len(shapePoint)!=2:
                continue
            if time1 == 0 or shapeTime < time1:
                if time2 < time1:
                    time2 = time1
                    latLon2 = latLon1
                time1 = shapeTime
                latLon1 = LatLonCoord(shapePoint[1], shapePoint[0])
            elif shapeTime > time2:
                time2 = shapeTime
                latLon2 = LatLonCoord(shapePoint[1], shapePoint[0])
            for frameTime in sessionFrameTimes:
                dtNow = frameTime - shapeTime
                if dtNow > -TIME_EQUIVALENCE_TOLERANCE and \
                   dtNow < TIME_EQUIVALENCE_TOLERANCE:
                    shapeList.append(eventShape)
                    shapeList[-1]["pointID"] = frameTime
                    break
 
        # Compute a working frame count, ignoring frames with a less than
        # a minute separating them, mostly to deal with All Tilts radar.
        lastWorkingFrameIndex = -1
        prevTime = None
        for sesFrTime in sessionFrameTimes:
            if not TimeUtils.isSameMinute(sesFrTime, prevTime):
                lastWorkingFrameIndex += 1
            prevTime = sesFrTime

        # Compute a working delta time between frames.
        endFrameTime = sessionFrameTimes[lastFrameIndex]
        if lastWorkingFrameIndex < 1:
            frameTimeStep = TimeUtils.minuteOf(endTime)-TimeUtils.minuteOf(startTime)
        else:
            frameTimeSpan = TimeUtils.minuteOf(endFrameTime) - \
                            TimeUtils.minuteOf(sessionFrameTimes[0])
            frameTimeStep = frameTimeSpan / lastWorkingFrameIndex

        # Note cases where the algorithm forces itself back to the previous
        # state.
        revert = False
        if draggedPointTime > endFrameTime + frameTimeStep / 4:
            revert = True
        dtNow = currentFrameTime - draggedPointTime
        if dtNow < -TIME_EQUIVALENCE_TOLERANCE or \
           dtNow > TIME_EQUIVALENCE_TOLERANCE:
            revert = True
        if time2 == 0:
            revert = False
        pointTrack = PointTrack()
        if revert:
            pointTrack.twoLatLonOrigTimeInit_(latLon1, TimeUtils.minuteOf(time1), \
                         latLon2, TimeUtils.minuteOf(time2), TimeUtils.minuteOf(startTime) )

        # Try to match up the pivots with the points.
        inputPivotTimes = eventAttributes.get("pivotTimes", [])
        pivotLatLonWorking = []
        pivotTimeWorking = []
        for pivot in inputPivotTimes:
            closestTime = 0
            closestDt = 99999999999
            closestLatLon = None
            for shape in shapeList:
                if shape.get("shapeType")!="point":
                    continue
                shapeTime = shape.get("pointID", 0)
                if shapeTime == 0:
                    continue
                if shapeTime in pivotTimeWorking:
                    continue
                shapePoint = shape.get("point", [])
                if len(shapePoint) < 2:
                    continue
                dtNow = pivot - shapeTime
                if dtNow < 0:
                    dtNow = -dtNow
                if dtNow >= closestDt:
                    continue
                closestTime = shapeTime
                closestDt = dtNow
                closestLatLon = LatLonCoord(shapePoint[1], shapePoint[0])
                if closestDt == 0:
                    break
            if closestTime == 0:
                continue
            pivotLatLonWorking.append(closestLatLon)
            pivotTimeWorking.append(closestTime)

        # Locate the first matched pivot that is not for the same time as
        # the dragged point.
        i = 0
        nPivots = len(pivotTimeWorking)
        while i < nPivots:
            dtNow = pivotTimeWorking[i] - draggedPointTime
            if dtNow < -TIME_EQUIVALENCE_TOLERANCE or \
               dtNow > TIME_EQUIVALENCE_TOLERANCE:
                break
            i = i + 1

        # If a useable pivot has been found, compute the track with two points,
        # otherwise only update the origin but keep the motion.        
        stormMotion = eventAttributes.get("stormMotion")
        if stormMotion == None:
            defaultSpeed = 20 # kts
            stormMotion = {}
            stormMotion["speed"] = defaultSpeed *  30 * MILLIS_PER_MINUTE / \
                    (endTime - startTime)
            stormMotion["bearing"] = 225 # from SW
        latLon0 = LatLonCoord(draggedPoint.y, draggedPoint.x)
        motion0 = Motion(stormMotion["speed"], stormMotion["bearing"])
        if revert:
            pivotTimeList = pivotTimeWorking
            pivotLatLonList = pivotLatLonWorking
        elif i < nPivots:
            latLon2 = pivotLatLonWorking[i]
            time2 = pivotTimeWorking[i]
            pointTrack.twoLatLonOrigTimeInit_(latLon0, TimeUtils.minuteOf(draggedPointTime), \
                 latLon2, TimeUtils.minuteOf(time2), TimeUtils.minuteOf(startTime))
            pivotTimeList = [draggedPointTime, time2]
            pivotLatLonList = [latLon0, latLon2]
        else:
            pointTrack.latLonMotionOrigTimeInit_(latLon0, TimeUtils.minuteOf(draggedPointTime), \
                 motion0, TimeUtils.minuteOf(startTime))
            pivotTimeList = [draggedPointTime]
            pivotLatLonList = [latLon0]
        pivotList = []
        for pivotTime in pivotTimeList:
            pivotList.append(self.indexOfClosest(pivotTime, sessionFrameTimes))

        # Create points for the frames. Reuse the lat/lon for consecutive frames
        # less than a minute apart, mostly to deal with All Tilts radar.
        shapeList = []
        trackCoordinates = []
        prevTime = None
        for frameTime in sessionFrameTimes:
            if not TimeUtils.isSameMinute(frameTime, prevTime):
                frameLatLon = pointTrack.trackPoint(TimeUtils.minuteOf(frameTime))
            trackPointShape = {}
            trackPointShape["pointType"] = "tracking"
            trackPointShape["shapeType"] = "point"
            trackPointShape["pointID"] = frameTime
            trackPointShape["point"] = [frameLatLon.lon, frameLatLon.lat]
            trackCoordinates.append([frameLatLon.lon, frameLatLon.lat])
            shapeList.append(trackPointShape)

        # Reaquire the motion from the start of the event instead of the last
        # frame.  In most cases this won't make much difference, but it can if
        # non-linear tracking is supported at some point.
        motion0 = pointTrack.speedAndAngleOf(TimeUtils.minuteOf(startTime))
        stormMotion["speed"] = motion0.speed
        stormMotion["bearing"] = motion0.bearing

        # Make a list of times for projected points for the track that are shown
        # to the user.  The last future time should be exactly at the end of
        # the hazard, and the first future time should not be too close to the
        # last frame, so the buffer of 1/3 the time step is added.
        futureTimeList = [endTime]
        nexttime = endTime - frameTimeStep
        endFrameTime = endFrameTime + frameTimeStep / 3
        while nexttime > endFrameTime:
            futureTimeList.insert(0, nexttime)
            nexttime = nexttime - frameTimeStep

        # Create a list of tracking points for each future point.
        for futureTime in futureTimeList:
            futureLatLon = pointTrack.trackPoint(futureTime)
            futurePointShape = {}
            futurePointShape["pointType"] = "tracking"
            futurePointShape["shapeType"] = "point"
            futurePointShape["pointID"] = futureTime
            futurePointShape["point"] = [futureLatLon.lon, futureLatLon.lat]
            trackCoordinates.append([futureLatLon.lon, futureLatLon.lat])
            shapeList.append(futurePointShape)

        # Return type should just be set to an object for now.
        returnType = "pyObjects"

        # Use existing polygon or get a new one, as appropriate.
        hazardPolygon = None
        polyModified = eventAttributes.get("polyModified", False)
        if revert:
            polyModified = True
        if not polyModified:
            if stormMotion["speed"] != 0:
                latLonPoly = pointTrack.polygonDef(startTime, endTime)
            else:
                latLonPoly = pointTrack.enclosedBy(startTime, endTime, \
                                               15.0, 15.0, 15.0, 15.0)
            hazardPolygon = []
            for latLonVertex in latLonPoly:
                hazardPolygon.append([latLonVertex.lon, latLonVertex.lat])

        # Finalize the set of output attributes.
        eventAttributes["trackPoints"] = shapeList
        eventAttributes["stormMotion"] = stormMotion
        eventAttributes["pivots"] = pivotList
        eventAttributes["pivotTimes"] = pivotTimeList
        eventAttributes["lastFrameTime"] = sessionFrameTimes[lastFrameIndex]
        eventAttributes["track"] = trackCoordinates

        # Cache some stuff the logic that composes the returned Java backed
        # HazardEvent object needs. This is made a temporary member of the
        # attributes, which the parent method execute() will delete. This way,
        # the correctness of this information can be evaluated in unit tests.
        forJavaObj = {}
        forJavaObj["hazardPolygon"] = hazardPolygon
        eventAttributes["forJavaObj"] = forJavaObj

        return eventAttributes

    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        @summary Entry point for recommender execution.
        @eventSet: Set of hazard events, as well as global attributes.
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() routine; ignored by this recommender.
        @param visualFeatures: Visual features used to provide spatial input;
        only provided if the trigger of the execution is a straightforward
        user initiation or a hazard-type-first scenario.
        @return: Event set of created and/or modified hazard events, or None if
        no hazard events were created or modified.
        '''
        sessionAttributes = eventSet.getAttributes()
        
        caveMode = sessionAttributes.get('runMode','PRACTICE').upper()
        self.practice = True
        if caveMode == 'OPERATIONAL':
            self.practice = False

        # If the recommender was executed manually, handle it one way; otherwise,
        # handle it as a modification of an existing hazard event.
        hazardEvent = None
        trigger = str(eventSet.getAttributes().get("trigger"))
        if trigger in ["none", "hazardTypeFirst"]:

            # Pick up the existing event from what is passed in.  If there is an
            # event then a WarnGen type workflow is assumed, otherwise an event
            # will be created from scratch.
            #
            # TODO: The haveEvent variable will never be true, since no event
            # identifier is ever passed in when running this recommender from the
            # H.S. menu or as a result of a hazard-type-first invocation. Redmine
            # issue #19971 would address this, in which case a new type of trigger
            # would have to be added that would be in the test list of possible
            # triggers above. But as it stands now, manual or hazard-type-first
            # invocation will always result in a new hazard event being created.
            #
            hazardEvent = self.getHazardEvent(eventSet)
            haveEvent = hazardEvent is not None
    
            # Artificially inject the initial hazard type into the attributes.
            if haveEvent:
                try:
                    sessionAttributes["phenomena"] = hazardEvent.getPhenomenon()
                except:
                    pass
                try:
                    sessionAttributes["significance"] = hazardEvent.getSignificance()
                except:
                    pass
                try:
                    sessionAttributes["subType"] = hazardEvent.getSubType()
                except:
                    pass
    
            # Call the method that gets the phenomenon, significance, subtype, and
            # phen-sig combo for the hazard event. This is a reasonable method for
            # a focal point to customize.
            ( phenomena, significance, subType, phenSig ) = \
                         self.initializeTypeOfEvent(sessionAttributes)
    
            # It is no longer possible to get the default event duration from the
            # event set attributes, so it must be retrieved from the bridge.
            try:
                import Bridge
                bridge = Bridge.Bridge()
                if subType is not None and len(subType) > 0:
                    hazardTypeEntry = bridge.getHazardTypes( phenSig + "." + subType)
                else:
                    hazardTypeEntry = bridge.getHazardTypes( phenSig )
                try:
                    dur = hazardTypeEntry['defaultDuration']
                except:
                    dur = MILLIS_PER_HOUR
                sessionAttributes["defaultDuration"] = dur
            except:
                import traceback
                tbData = traceback.format_exc()
                sys.stderr.write(tbData)
                sessionAttributes["defaultDuration"] = 3 * MILLIS_PER_HOUR
    
            # Presence of java backed objects in the attributes complicates unit
            # testing, so unravel any of these.
            try:
                sessionAttributes["framesInfo"]["currentFrame"] = \
                  str(sessionAttributes["framesInfo"]["currentFrame"])
            except:
                del sessionAttributes["framesInfo"]["currentFrame"]
    
            # Initialize the event attributes. This method does everything that can
            # be safely done in a unit test, basically whatever does not require Jep.
            resultDict = self.initializeEventAttributes(phenomena, significance, subType, \
                                                        sessionAttributes, visualFeatures)
            forJavaObj = self.getAndDeleteForJavaObj(resultDict)
    
            # If there is not an existing hazard event, create one now.
            if not haveEvent:
                hazardEvent = EventFactory.createEvent(self.practice)
    
                # String cast accounts for occasional JSON promotion of ASCII strings
                # to Unicode strings, which makes JEP barf.
                hazardEvent.setEventID("")
                hazardEvent.setSiteID(str(forJavaObj["siteID"]))
                hazardEvent.setHazardStatus("PENDING")
    
                # Set the creation, start, and end times for the hazard event.
                hazardEvent.setCreationTime(datetime.datetime.utcfromtimestamp( \
                  forJavaObj["currentTime"] / MILLIS_PER_SECOND))
                hazardEvent.setStartTime(datetime.datetime.utcfromtimestamp( \
                  forJavaObj["startTime"] / MILLIS_PER_SECOND))
                hazardEvent.setEndTime(datetime.datetime.utcfromtimestamp( \
                  forJavaObj["endTime"] / MILLIS_PER_SECOND))
    
                # Set the hazard type.
                hazardEvent.setPhenomenon(forJavaObj["phenomena"])
                if forJavaObj["subType"] != "":
                    hazardEvent.setSubType(forJavaObj["subType"])
                hazardEvent.setSignificance(forJavaObj["significance"])
    
            # Set the base geometry.
            assert self.setHazardGeometryFromDictionary(hazardEvent, forJavaObj), "missing event geometry"
    
            # Save the hazard attributes calculated previously.
            hazardEvent.addHazardAttributes(resultDict)
            
        else:

            # Ensure the hazard event has been initialized by this recommender before;
            # if not, do nothing with it.            
            hazardEvent = self.getHazardEvent(eventSet)
            if hazardEvent is None:
                self.logger.info("No identifier of unlocked hazard event found for StormTrackTool to modify.")
                return None
            elif self.isStormTrackedEvent(hazardEvent) is False:
                self.logger.info("Hazard event found for StormTrackTool to modify is not storm tracked.")
                return None

            # If the trigger was the modification of the event itself, handle it one
            # way; otherwise, the trigger is a visual feature modification, which is
            # to be handled another way.
            attributeIdentifiers = eventSet.getAttributes().get("attributeIdentifiers")
            if trigger == "hazardEventModification" and attributeIdentifiers:

                # If the geometry of the event changed, update the visual features
                # to match; if the time range of the event changed, update its
                # underlying data; if its status changed, continue, since the
                # visual features will need to be recreated; and if anything else
                # changed, do nothing. 
                changed = list(attributeIdentifiers)[0]
                if changed == "geometry":
                    
                    # If no visual features are to be displayed, there is nothing to
                    # be done.
                    if self.useVisualFeatures(hazardEvent) == False:
                        return None
                    
                    # Get the new geometry, and the base geometry visual feature.
                    visualFeatures = hazardEvent.getVisualFeatures()
                    geometry = hazardEvent.getGeometry()
                    baseFeature = self.getVisualFeature("baseGeometry", visualFeatures)

                    # If the base geometry visual feature's geometry is already the
                    # same as the new geometry, do nothing.
                    if geometry.asShapely().equals(baseFeature["geometry"].values()[0].asShapely()):
                        return None
                    
                    # Replace the base geometry visual feature's geometry with the
                    # new one, and update the label visual feature appropriately. 
                    baseFeature["geometry"] = { baseFeature["geometry"].keys()[0]: geometry }
                    self.updateLabelVisualFeature(visualFeatures, geometry)
                    hazardEvent.setVisualFeatures(visualFeatures)
                    return hazardEvent
                    
                elif changed == "timeRange":
                    
                    #
                    # TODO: What should happen? Could be one of two cases: Either
                    # the start or end time have changed independently of one
                    # another, in which case the duration has changed, or they
                    # have both advanced, keeping the duration constant.
                    #
                    return None
                
                elif changed == "status":
                    pass
                
                else:
                    return None
            
            else:
                
                # Assume that a visual feature change occurred. If the change is
                # that there are no visual features, continue, since the visual
                # features may need to be created. Otherwise, see which visual
                # feature changed.
                visualFeatureIdentifiers = eventSet.getAttributes().get("visualFeatureIdentifiers")
                if len(visualFeatureIdentifiers) > 0:
                
                    # Get the identifier of the visual feature that changed, and
                    # the visual feature associated with said identifier.
                    changed = list(visualFeatureIdentifiers)[0]
                    visualFeatures = hazardEvent.getVisualFeatures()
                    changedFeature = self.getVisualFeature(changed, visualFeatures)
                    
                    # Either the base geometry changed, or one of the track
                    # points was moved.
                    if changed == "baseGeometry":
                        
                        # Update the underlying hazard event geometry to match
                        # its visual feature representation, and the label visual
                        # feature so that it is located over the centroid of the
                        # new geoemtry. Then return the modified event.
                        geometry = changedFeature["geometry"].values()[0]
                        hazardEvent.setGeometry(geometry)
                        self.updateLabelVisualFeature(visualFeatures, geometry)
                        hazardEvent.setVisualFeatures(visualFeatures)
                        return hazardEvent
                    else:
                        
                        # Since a track point changed, update the event attributes
                        # to reflect the new point, and recreate the hazard event's
                        # geometry if a new one is needed. Do not return yet, as
                        # the visual features need to be recreated. 
                        eventAttributes = self.updateEventAttributes(changedFeature, \
                                                                     eventSet.getAttributes(), \
                                                                     hazardEvent)
                        forJavaObj = self.getAndDeleteForJavaObj(eventAttributes)
                        hazardEvent.setHazardAttributes(eventAttributes)
                        self.setHazardGeometryFromDictionary(hazardEvent, forJavaObj)

        # If a hazard event has been created or modified, add its visual features
        # and return it.
        if hazardEvent is not None:
            self.createVisualFeatures(hazardEvent)
            return hazardEvent
        return None

    def getAndDeleteForJavaObj(self, resultDict):
        '''
        @summary Get the "for Java" object from the specified dictionary under
        the "forJavaObj" key, and delete it from the dictionary as well.
        @param resultDict: Dictionary from which to get and delete the object.
        @return "For Java" object.
        '''
        
        forJavaObj = resultDict["forJavaObj"]
        del resultDict["forJavaObj"]
        return forJavaObj

    def setHazardGeometryFromDictionary(self, hazardEvent, forJavaObj):
        '''
        @summary Get the geometry as a list of coordinates from the
        "hazardPolygon" entry in the specified "for Java" object, and use it
        to create the specified hazard event's geometry and assign it.
        @param hazardEvent: Hazard event to have its geometry set.
        @param forJavaObj: Dictionary from which to get the list of
        coordinates.
        @return True if the geometry was set, False if it could not be found
        in the supplied dictionary.
        '''

        hazardPolygon = forJavaObj.get("hazardPolygon")
        if hazardPolygon is None:
            return False
        hazardEvent.setGeometry(AdvancedGeometry.
                                createShapelyWrapper(GeometryFactory.createPolygon(hazardPolygon), 0))
        return True

    def updateLabelVisualFeature(self, visualFeatures, geometry):
        '''
        @summary Update the geometry of the label visual feature found in the
        specified list of visual features to be the centroid of the specified
        geometry.
        @param visualFeatures: List of visual features in which the label
        feature is to be found.
        @param geometry: New geometry from which the centroid is to be taken.
        '''

        labelFeature = self.getVisualFeature("label", visualFeatures)
        labelFeature["geometry"] = { labelFeature["geometry"].keys()[0]:
                                    AdvancedGeometry.createShapelyWrapper(geometry.
                                                                          asShapely()[0].centroid, 0) }

    def getVisualFeature(self, identifier, visualFeatures):
        '''
        @summary Get the visual feature with the specified identifier from
        the specified list.
        @param identifier Identifier of the visual feature to be fetched.
        @param visualFeatures: List of visual features in which to find the
        feature.
        @return Visual feature that was found, or None otherwise.
        '''

        for visualFeature in visualFeatures:
            if visualFeature["identifier"] == identifier:
                return visualFeature
        return None

    def setVisualFeature(self, visualFeature, visualFeatures):
        '''
        @summary Replace the visual feature in the specified list that has the
        same identifier as the specified visual feature with the latter.
        @param visualFeature: Visual features to be used as as replacement.
        @param visualFeatures: List of visual features in which to perform the
        replacement.
        '''

        identifier = visualFeature["identifier"]
        for index in range(0, len(visualFeatures)):
            if visualFeatures[index]["identifier"] == identifier:
                visualFeatures[index] = visualFeature
                return

    def useVisualFeatures(self, hazardEvent):
        '''
        @summary Determine whether or not the specified hazard event should
        display visual features.
        @param hazardEvent: The hazard event to be checked.
        @return True if the event should display visual features, otherwise
        False.
        '''

        return hazardEvent.getStatus() in ["POTENTIAL", "PENDING", "ISSUED"]
    
    def createVisualFeatures(self, hazardEvent):
        '''
        @summary Create whatever visual features are appropriate for the
        specified hazard event, and assign them to the event. This may result
        in the event having no visual features, or a number of them, as is
        appropriate.
        @param hazardEvent: The hazard event to be modified.
        '''

        # If the hazard event needs visual features, create them.
        visualFeatures = []
        if self.useVisualFeatures(hazardEvent):
    
            # Compile a list of parameter dictionaries providing information about the
            # track points, including the boundary times in which they are to be
            # highlighted and their identifiers. Their identifiers are (when rounded)
            # their start times. The last point has its highlighting upper boundary
            # time calculated to be the same delta from its start time as the previous
            # point's two boundaries have between them. Also make a record of the
            # earliest and latest visibility times for the earliest and latest track
            # points, respectively, as these will serve as visibility boundaries for
            # other visual elements of the hazard event.
            earliestVisibility = None
            latestVisibility = None
            lastPoint = None
            trackPointParams = []
            pivotTimes = hazardEvent.get("pivotTimes", [])
            for point in hazardEvent.get("trackPoints", []):
                point = {
                         "identifier": str(point["pointID"]),
                         "startTime": TimeUtils.roundEpochTimeMilliseconds(float(point["pointID"])),
                         "geometry": AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPoint(point["point"]), 0),
                         "shape": "star" if point["pointID"] in pivotTimes else "circle"
                         }
                if earliestVisibility is None:
                    earliestVisibility = point["startTime"]
                else:
                    lastPoint["endTime"] = point["startTime"]
                trackPointParams.append(point)
                lastPoint = point
            if len(trackPointParams) > 1:
                lastPoint = trackPointParams[-2]
                point["endTime"] = point["startTime"] + lastPoint["endTime"] - lastPoint["startTime"]
                latestVisibility = point["endTime"]
            elif lastPoint is not None:
                point["endTime"] = point["startTime"] + (15 * 60000)
                latestVisibility = point["endTime"]
            else:
                earliestVisibility = sys.maxint
                latestVisibility = sys.minint
    
            # Get the start and end times of the geometry, and ensure that the earliest
            # and latest visibility times encompass the former.
            startTime = TimeUtils.datetimeToEpochTimeMillis(TimeUtils.roundDatetime(hazardEvent.getStartTime()))
            if earliestVisibility > startTime:
                earliestVisibility = startTime - (30 * 60000)
            endTime = TimeUtils.datetimeToEpochTimeMillis(TimeUtils.roundDatetime(hazardEvent.getEndTime()))
            if latestVisibility < endTime:
                latestVisibility = endTime + (30 * 60000)
    
            # Create the base geometry visual feature.
            gray = { "red": 0.5, "green": 0.5, "blue": 0.5 }
            grayOrStandard = {
                           (earliestVisibility, startTime): gray,
                           (startTime, endTime): "eventType",
                           (endTime, latestVisibility): gray
                           }
            baseGeometryFeature = {
                                   "identifier": "baseGeometry",
                                   "visibilityConstraints": "always",
                                   "borderColor": grayOrStandard,
                                   "borderThickness": "eventType",
                                   "borderStyle": "eventType",
                                   "dragCapability": "all", 
                                   "geometry": {
                                                (earliestVisibility, latestVisibility): hazardEvent.getGeometry()
                                                }
                                   }
            visualFeatures.append(baseGeometryFeature)
    
            # Create the storm track line visual feature.
            trackLineCoordinates = hazardEvent.get("track", [])
            if len(trackLineCoordinates) > 0:
                stormTrackLine = {
                                  "identifier": "trackLine",
                                  "visibilityConstraints": "selected",
                                  "borderColor": grayOrStandard,
                                  "borderThickness": "eventType",
                                  "dragCapability": "none", 
                                  "geometry": {
                                               (earliestVisibility, latestVisibility): \
                                               AdvancedGeometry.createShapelyWrapper(GeometryFactory.createLineString(trackLineCoordinates), 0)
                                               }
                                  }
                visualFeatures.append(stormTrackLine)
    
            # Create the storm track point visual features. Each is to look "selected"
            # between its lower and upper time boundaries, the boundaries giving the
            # time period during which it represents the position of the storm along
            # the track. They are also draggable during that time.
            for params in trackPointParams:
                color = self.getTemporallyVariantProperty(earliestVisibility, latestVisibility, \
                                                          params["startTime"], params["endTime"], \
                                                          gray, "eventType")
                stormTrackPoint = {
                                   "identifier": params["identifier"],
                                   "visibilityConstraints": "selected",
                                   "borderColor": color,
                                   "fillColor": color,
                                   "borderThickness": 2,
                                   "borderStyle": "eventType",
                                   "diameter": self.getTemporallyVariantProperty(earliestVisibility, latestVisibility, \
                                                                                    params["startTime"], params["endTime"], \
                                                                                    10, 13),
                                   "symbolShape": params["shape"],
                                   "dragCapability": self.getTemporallyVariantProperty(earliestVisibility, latestVisibility, \
                                                                                       params["startTime"], params["endTime"], \
                                                                                       "none", "all"),
                                   "geometry": {
                                                (earliestVisibility, latestVisibility): params["geometry"]
                                                },
                                   "topmost": self.getTemporallyVariantProperty(earliestVisibility, latestVisibility, \
                                                                                params["startTime"], params["endTime"], \
                                                                                False, True)
                                   }
                visualFeatures.append(stormTrackPoint)
    
            # Create the label visual feature. This is done instead of labeling the base
            # geometry because the latter's label would be occluded by the storm track line
            # and points. It is given a topmost label so that it can be above even the
            # currently "selected" storm track point.
            labelFeature = {
                            "identifier": "label",
                            "visibilityConstraints": "always",
                            "borderColor": { "red": 0.0, "green": 0.0, "blue": 0.0, "alpha": 0.0 },
                            "textSize": "eventType",
                            "label": "eventType",
                            "diameter": 1,
                            "textColor": "eventType",
                            "dragCapability": "none",
                            "topmost": True,
                            "geometry": {
                                         (earliestVisibility, latestVisibility):
                                         AdvancedGeometry.createShapelyWrapper(hazardEvent.
                                                                               getFlattenedGeometry()[0].
                                                                               centroid, 0)
                                         }
                            }
            visualFeatures.append(labelFeature)
            
        hazardEvent.setVisualFeatures(VisualFeatures(visualFeatures))

    def getHazardEvent(self, eventSet):
        '''
        @summary Get the hazard event from the specified event set with the
        identifier provided within the same event set as the first element
        in the list found within an attribute named "eventIdentifiers".
        @param eventSet: Event set to be checked.
        @return Hazard event that was retrieved, or None if none was found.
        '''

        # Get the event identifiers that apply for this execution of the
        # recommender, and take the first of them as the one that is to
        # be operated upon. This recommender should never be called with
        # multiple event identifiers associated with the execution.
        eventIdentifiers = eventSet.getAttributes().get("eventIdentifiers")
        if eventIdentifiers is None:
            return None
        eventIdentifier = next(iter(eventIdentifiers))
        if eventIdentifier is None:
            return None
        eventIdentifier = str(eventIdentifier)

        # Ensure the event identifier is not one that is currently locked.
        if self.hazardEventLockUtils is None:
            self.hazardEventLockUtils = HazardEventLockUtils(self.practice)
        if eventIdentifier in self.hazardEventLockUtils.getLockedEvents():
            return None

        # Return the event that matches the identifier, if any.
        events = eventSet.getEvents()
        for event in events:
            if event.getEventID() == eventIdentifier:
                return event
        return None
    
    def isStormTrackedEvent(self, hazardEvent):
        '''
        @summary Determine whether or not the specified hazard event has been
        previously initialized by this recommender.
        @param hazardEvent: Hazard event to be checked.
        @return True if it has been initialized by this recommender previously,
        otherwise False.
        '''

        return hazardEvent.get("track") is not None
    
    def getTemporallyVariantProperty(self, minTime, maxTime, highlightMinTime, highlightMaxTime, \
                                     lowlightValue, highlightValue):
        '''
        @summary Get a dictionary holding as keys time ranges in milliseconds
        and as values the specified lowlight and highlight properties. Between
        the lower and upper time bounds of highlight, the highlight value is
        used; for other time ranges bounded by the minimum and maximum times,
        the lowlight value is used.
        @param minTime: Minimum time that any range can use as a lower bound,
        in epoch time in milliseconds.
        @param maxTime: Maximum time that any range can use as an  upper bound,
        in epoch time in milliseconds.
        @param highlightMinTime: Lower time bound for the highlight value to be
        used, in epoch time in milliseconds.
        @param highlightMaxTime: Upper time bound for the highlight value to be
        used, in epoch time in milliseconds.
        @param lowlightValue: Value to be used for lowlight.
        @param highlightValue: Value to be used for highlight.
        @return Dictionary holding entries with keys being time ranges in
        milliseconds and values being lowlight and highlight values.
        '''

        if minTime is highlightMinTime and maxTime is highlightMaxTime:
            return {
                    (minTime, maxTime): highlightValue
                    }
        elif minTime is highlightMinTime:
            return {
                    (minTime, highlightMaxTime): highlightValue,
                    (highlightMaxTime, maxTime): lowlightValue
                    }
        elif maxTime is highlightMaxTime:
            return {
                    (minTime, highlightMinTime): lowlightValue,
                    (highlightMinTime, maxTime): highlightValue
                    }
        else:
            return {
                    (minTime, highlightMinTime): lowlightValue,
                    (highlightMinTime, highlightMaxTime): highlightValue,
                    (highlightMaxTime, maxTime): lowlightValue
                    }

    def toString(self):
        return "Storm Track Tool"    
