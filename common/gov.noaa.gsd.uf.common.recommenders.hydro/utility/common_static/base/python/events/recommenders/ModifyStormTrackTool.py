"""
 Tool which is called each time an element of a storm
 track is modified.  This tool produces a new storm
 track based on these modifications.

@since: June 2013
@author: JRamer
"""
import RecommenderTemplate

import datetime
import time
import math
from PointTrack import *
from GeneralConstants import *

class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        return

    def defineScriptMetadata(self):
        '''
        @return: Returns a python dictionary which defines basic information
        about the recommender, such as author, script version, and description
        '''
        metaDict = {}
        metaDict["toolName"] = "ModifyStormTrackTool"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["eventState"] = "Pending"
        return metaDict

    def defineDialog(self):
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
        return None

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

    # Note that since there are hidden millisecond to second conversions
    # (and vice-versa) in the underlying code, we always allow a slop of
    # 999 milliseconds when comparing two millisecond times for equivalence.
    
    def updateEventAttributes(self, sessionAttributes, eventAttributes, \
                    dialogInputMap, spatialInputMap):
        '''
        @param sessionAttributes: Session attributes associated with eventSet.
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() routine
        @param spatialInputMap: A map containing spatial input as created by the 
        definedSpatialInfo() routine
        @return: updated session attributes.
        '''
        eventDict = eventAttributes
        modifyDict = spatialInputMap

        framesInfo = sessionAttributes.get("framesInfo")
        sessionFrameTimes = framesInfo["frameTimeList"]
        lastFrameIndex = len(sessionFrameTimes)-1
        currentTime = long(sessionAttributes["currentTime"])
        currentFrameIndex = framesInfo["frameIndex"]
        currentFrameTime = sessionFrameTimes[currentFrameIndex]
        startTime = eventDict["startTime"]
        endTime = eventDict["endTime"]

        # Get information about new 'dragmeto' point.
        draggedPoint = spatialInputMap.get("newLatLon")
        if draggedPoint == None :
            draggedPoint = spatialInputMap["newLonLat"]
        draggedPointTime = spatialInputMap["pointID"]
        if draggedPointTime < VERIFY_MILLISECONDS :
            draggedPointTime = draggedPointTime * MILLIS_PER_SECOND
        if draggedPointTime < 0 :
            draggedPointTime = eventDict["draggedPoints"][0][1];

        # Filter our shape list to remove any points that do not match a frame,
        # note information about the earliest and latest point, plus polygon.
        eventShapes = eventDict.get("trackPoints", [])
        shapeList = []
        time1 = 0
        time2 = 0
        for eventShape in eventShapes :
            if eventShape.get("shapeType")!="point" :
                continue
            shapeTime = eventShape.get("pointID", 0)
            if shapeTime==0 :
                continue
            shapePoint = eventShape.get("point", [])
            if len(shapePoint)!=2 :
                continue
            if time1==0 or shapeTime<time1 :
                if time2<time1 :
                    time2 = time1
                    latLon2 = latLon1
                time1 = shapeTime
                latLon1 = LatLonCoord(shapePoint[1], shapePoint[0])
            elif shapeTime>time2 :
                time2 = shapeTime
                latLon2 = LatLonCoord(shapePoint[1], shapePoint[0])
            for frameTime in sessionFrameTimes :
                dtNow = frameTime-shapeTime
                if dtNow>-TIME_EQUIVALENCE_TOLERANCE and \
                   dtNow<TIME_EQUIVALENCE_TOLERANCE :
                    shapeList.append(eventShape)
                    shapeList[-1]["pointID"] = frameTime
                    break
 
        # Compute a working delta time between frames.
        endFrameTime = sessionFrameTimes[lastFrameIndex]
        if lastFrameIndex<1 :
            frameTimeStep = endTime-startTime
        else :
            frameTimeSpan = endFrameTime-sessionFrameTimes[0]
            frameTimeStep = frameTimeSpan/lastFrameIndex

        # Note cases where we force ourselves back to the previous state.
        revert = False
        if draggedPointTime>endFrameTime+frameTimeStep/4 :
            revert = True
        dtNow = currentFrameTime-draggedPointTime
        if dtNow<-TIME_EQUIVALENCE_TOLERANCE or \
           dtNow>TIME_EQUIVALENCE_TOLERANCE :
            revert = True
        if time2 == 0 :
            revert = False
        pointTrack = PointTrack()
        if revert :
            pointTrack.twoLatLonOrigTimeInit_( \
                         latLon1, time1, latLon2, time2, startTime)

        # Try to match up our pivots with our points.
        inputPivotTimes = eventDict.get("pivotTimes", [])
        pivotLatLonWorking = []
        pivotTimeWorking = []
        for pivot in inputPivotTimes :
            closestTime = 0
            closestDt = 99999999999
            closestLatLon = None
            for shape in shapeList :
                if shape.get("shapeType")!="point" :
                    continue
                shapeTime = shape.get("pointID", 0)
                if shapeTime==0 :
                    continue
                if shapeTime in pivotTimeWorking :
                    continue
                shapePoint = shape.get("point", [])
                if len(shapePoint)<2 :
                    continue
                dtNow = pivot-shapeTime
                if dtNow < 0 :
                    dtNow = -dtNow
                if dtNow>=closestDt :
                    continue
                closestTime = shapeTime
                closestDt = dtNow
                closestLatLon = LatLonCoord(shapePoint[1], shapePoint[0])
                if closestDt==0:
                    break
            if closestTime==0:
                continue
            pivotLatLonWorking.append(closestLatLon)
            pivotTimeWorking.append(closestTime)

        # locate the first matched pivot that is not for the same time as
        # our dragged point.
        i = 0
        nPivots = len(pivotTimeWorking)
        while i<nPivots :
            dtNow = pivotTimeWorking[i]-draggedPointTime
            if dtNow<-TIME_EQUIVALENCE_TOLERANCE or \
               dtNow>TIME_EQUIVALENCE_TOLERANCE :
                break
            i = i+1

        # If we have a useable pivot, compute track with two points, otherwise
        # only update the origin but keep the motion.        
        stormMotion = eventDict.get("stormMotion")
        if stormMotion==None :
            defaultSpeed = 20 # kts
            stormMotion = {}
            stormMotion["speed"] = \
               defaultSpeed * 30*MILLIS_PER_MINUTE / (endTime-startTime)
            stormMotion["bearing"] = 225 # from SW
        latLon0 = LatLonCoord(draggedPoint[1], draggedPoint[0])
        motion0 = Motion(stormMotion["speed"], stormMotion["bearing"])
        if revert :
            pivotTimeList = pivotTimeWorking
            pivotLatLonList = pivotLatLonWorking
        elif i<nPivots :
            latLon2 = pivotLatLonWorking[i]
            time2 = pivotTimeWorking[i]
            pointTrack.twoLatLonOrigTimeInit_( \
                 latLon0, draggedPointTime, latLon2, time2, startTime)
            pivotTimeList = [draggedPointTime, time2 ]
            pivotLatLonList = [latLon0, latLon2 ]
        else :
            pointTrack.latLonMotionOrigTimeInit_( \
                 latLon0, draggedPointTime, motion0, startTime)
            pivotTimeList = [draggedPointTime]
            pivotLatLonList = [latLon0]
        pivotList = []
        for pivotTime in pivotTimeList :
            pivotList.append( \
              self.indexOfClosest(pivotTime, sessionFrameTimes) )

        # Create points for our frames.
        shapeList = []
        trackCoordinates = []
        for frameTime in sessionFrameTimes :
            frameLatLon = pointTrack.trackPoint(frameTime)
            trackPointShape = {}
            trackPointShape["pointType"] = "tracking"
            trackPointShape["shapeType"] = "point"
            trackPointShape["pointID"] = frameTime
            trackPointShape["point"] = [frameLatLon.lon, frameLatLon.lat]
            trackCoordinates.append([frameLatLon.lon, frameLatLon.lat])
            shapeList.append(trackPointShape)

        # Reacquire the motion from the start of the event instead of the
        # last frame.  In most cases wont make much difference, but can once
        # we start supporting non-linear tracking.
        motion0 = pointTrack.speedAndAngleOf(startTime)
        stormMotion["speed"] = motion0.speed
        stormMotion["bearing"] = motion0.bearing

        # Make a list of times for projected points for the track that are shown
        # to the user.  We want the last future time to be exactly at the end of
        # the hazard, and we do not want the first future time to be too close
        # to the last frame, so we add the buffer of 1/3 the time step.
        futureTimeList = [endTime]
        nexttime = endTime-frameTimeStep
        endFrameTime = endFrameTime + frameTimeStep/3
        while nexttime > endFrameTime :
            futureTimeList.insert(0, nexttime)
            nexttime = nexttime-frameTimeStep

        # Create a list of tracking points for each future point.
        for futureTime in futureTimeList :
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

        # Use existing polygon or ask for new one, as appropriate.
        hazardPolygon = None
        polyModified = eventDict.get("polyModified", False)
        if revert :
            polyModified = True
        if not polyModified:
            latLonPoly = pointTrack.polygonDef(startTime, endTime)
            hazardPolygon = []
            for latLonVertex in latLonPoly :
                hazardPolygon.append([latLonVertex.lon, latLonVertex.lat])

        # Finalize our set of output attributes.
        eventDict["trackPoints"] = shapeList
        eventDict["stormMotion"] = stormMotion
        eventDict["pivots"] = pivotList
        eventDict["pivotTimes"] = pivotTimeList
        eventDict["lastFrameTime"] = sessionFrameTimes[lastFrameIndex]

        # Cache some stuff the logic that composes the returned Java backed
        # HazardEvent object needs. We will make this a temporary member of the
        # attributes, which the parent method execute() will delete. This way,
        # the correctness of this information can be evaluated in unit tests.
        forJavaObj = {}
        forJavaObj["track"] = trackCoordinates
        forJavaObj["hazardPolygon"] = hazardPolygon
        eventDict["forJavaObj"] = forJavaObj

        return eventDict
    
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

        # Pick up the existing event from what is passed in.
        layerEventId = spatialInputMap.get("eventID")
        events = eventSet.getEvents()
        haveEvent = False
        for event in events :
            if event.getEventID() == layerEventId :
                hazardEvent = event
                haveEvent = True
                break
        if not haveEvent :
            print "No matching input event in ModifyStormTrackTool.py"
        sessionAttributes = eventSet.getAttributes()
        eventAttributes = hazardEvent.getHazardAttributes()

        # updateEventAttributes does all the stuff we can safely do in a unit
        # test, basically whatever does not require Jep.  Remove "stormTrackLine"
        # element, as it is not JSON serializable, which messes up unit tests.
        del eventAttributes["stormTrackLine"]
        resultDict = self.updateEventAttributes(sessionAttributes, \
                                eventAttributes,  dialogInputMap, spatialInputMap)

        # Peel out stuff that is piggybacking on the attributes but is really
        # meant to go into the HazardEvent.
        forJavaObj = resultDict["forJavaObj"]
        del resultDict["forJavaObj"]

        # Update our event and return it.
        resultDict["stormTrackLine"] = \
                   GeometryFactory.createLineString(forJavaObj["track"])
        hazardPolygon = forJavaObj.get("hazardPolygon")
        if hazardPolygon != None :
            geometry = GeometryFactory.createPolygon(hazardPolygon)
            hazardEvent.setGeometry(GeometryFactory.createCollection([geometry]))
        hazardEvent.setHazardAttributes(resultDict)
        return hazardEvent

    def toString(self):
        return "Modify Storm Track Tool!"

