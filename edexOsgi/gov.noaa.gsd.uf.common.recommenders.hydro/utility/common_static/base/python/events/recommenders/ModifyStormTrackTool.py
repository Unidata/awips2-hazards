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
        metaDict["toolType"] = "Recommender"
        metaDict["outputFormat"] = "PYTHON"
        metaDict["returnType"] = "ModifiedEventDict"
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

    # Note that since there are hidden millisecond to second conversions
    # (and vice-versa) in the underlying code, we always allow a slop of
    # 999 milliseconds when comparing two millisecond times for equivalence.
    
    def executeImpl(self, sessionAttributes, dialogInputMap, spatialInputMap):
        '''
        @param sessionAttributes: Session attributes associated with eventSet.
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() routine
        @param spatialInputMap: A map containing spatial input as created by the 
        definedSpatialInfo() routine
        @return: updated session attributes.
        '''

        staticSettings = sessionAttributes["staticSettings"]
        eventDict = sessionAttributes["selectedEventDict"]
        modifyDict = spatialInputMap

        defaultDuration = staticSettings.get("defaultDuration", 3600*1000)
        framesInfo = sessionAttributes.get("framesInfo")
        sessionFrameTimes = framesInfo["frameTimeList"]
        lastFrameIndex = len(sessionFrameTimes)-1
        currentTime = long(sessionAttributes["currentTime"])
        startTime = eventDict["startTime"]
        endTime = eventDict["endTime"]

        # Get information about 'dragmeto' point.
        draggedPoint = spatialInputMap["newLonLat"]
        draggedPointTime = spatialInputMap["pointID"]
        if draggedPointTime < VERIFY_MILLISECONDS :
            draggedPointTime = draggedPointTime * MILLIS_PER_SECOND
        if draggedPointTime < 0 :
            draggedPointTime = eventDict["draggedPoints"][0][1];

        # Filter our shape list to remove any points that do not match a frame,
        # note information about the earliest and latest point, plus polygon.
        eventShapes = eventDict.get("shapes", [])
        shapeList = []
        time1 = 0
        time2 = 0
        polygonShape = None
        for eventShape in eventShapes :
            if eventShape.get("shapeType")=="polygon" :
                if "points" in eventShape :
                    polygonShape = eventShape
                continue
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
            for oneFrTim in sessionFrameTimes :
                dtNow = oneFrTim-shapeTime
                if dtNow>-TIME_EQUIVALENCE_TOLERANCE and \
                   dtNow<TIME_EQUIVALENCE_TOLERANCE :
                    shapeList.append(eventShape)
                    break
 
        # Compute a working delta time between frames.
        if lastFrameIndex<1 :
            frameTimeStep = endTime-startTime
        else :
            frameTimeSpan = \
                 sessionFrameTimes[lastFrameIndex]-sessionFrameTimes[0]
            frameTimeStep = frameTimeSpan/lastFrameIndex

        # Note cases where we force ourselves back to the previous state.
        revert = False
        if draggedPointTime>sessionFrameTimes[lastFrameIndex]+frameTimeStep/4 :
            revert = True
        currentFrameTime = long(sessionAttributes.get("selectedTime", 0))
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
        pivotList = eventDict.get("pivots", [])
        pivotLatLonList = []
        pivotTimeList = []
        for pivot in pivotList :
            closestTime = 0
            closestDt = 99999999999
            closestLatLon = None
            for shape in shapeList :
                if shape.get("shapeType")!="point" :
                    continue
                shapeTime = shape.get("pointID", 0)
                if shapeTime==0 :
                    continue
                if shapeTime in pivotTimeList :
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
            pivotLatLonList.append(closestLatLon)
            pivotTimeList.append(closestTime)

        # locate the first matched pivot that is not for the same time as
        # our dragged point.
        i = 0
        nPivots = len(pivotTimeList)
        while i<nPivots :
            dtNow = pivotTimeList[i]-draggedPointTime
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
            pass
        elif i<nPivots :
            latLon2 = pivotLatLonList[i]
            time2 = pivotTimeList[i]
            pointTrack.twoLatLonOrigTimeInit_( \
                 latLon0, draggedPointTime, latLon2, time2, startTime)
            pivotList = [draggedPointTime, pivotTimeList[i] ]
            pivotLatLonList = [latLon0, pivotLatLonList[i] ]
        else :
            pointTrack.latLonMotionOrigTimeInit_( \
                 latLon0, draggedPointTime, motion0, startTime)
            pivotList = [draggedPointTime]
            pivotLatLonList = [latLon0]

        # Create points for our frames.
        shapeList = []
        for frameTime in sessionFrameTimes :
            frameLatLon = pointTrack.trackPoint(frameTime)
            trackPointShape = {}
            trackPointShape["pointType"] = "tracking"
            trackPointShape["shapeType"] = "point"
            trackPointShape["pointID"] = frameTime
            trackPointShape["point"] = [frameLatLon.lon, frameLatLon.lat]
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
        endFrameTime = sessionFrameTimes[lastFrameIndex]
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
            shapeList.append(futurePointShape)

        # Return type should just be set to an object for now.
        #returnType = runDict.get("returnType")
        returnType = "pyObjects"

        # Use existing polygon or ask for new one, as appropriate.
        polyModified = eventDict.get("polyModified", False)
        if revert :
            polyModified = True
        if polygonShape == None :
            polyModified = False
        if polyModified:
            hazardPolygon = polygonShape["points"]
            shapeList.append(polygonShape)
        if not polyModified:
            latLonPoly = pointTrack.polygonDef(startTime, endTime)
            hazardPolygon = []
            for latLonVertex in latLonPoly :
                hazardPolygon.append([latLonVertex.lon, latLonVertex.lat])
            oneshape = { "include" : "true" }
            oneshape["shapeType"] = "polygon"
            oneshape["points"] = hazardPolygon
            shapeList.append(oneshape)

        # Encode our set of working pivot locations and times into our new
        # list of "draggedPoints".
        draggedPoints = []
        nPivots = len(pivotList)
        i = 0
        while i < nPivots :
            latLon = pivotLatLonList[i]
            draggedPoints.append( \
              (  ( latLon.lon, latLon.lat ) , pivotList[i]  )    )
            i = i + 1
        eventDict["draggedPoints"] = draggedPoints

        # Finalize our set of output attributes.
        phenomena = eventDict["phen"]
        sigTrn = { "W" : "WARNING", "A" : "WATCH", "Y" : "ADVISORY" }
        significance = sigTrn.get(eventDict.get("sig"))
        if significance == None :
            significance = eventDict.get("sig")
        subtype = eventDict.get("subType")
        if subtype == "" :
            subtype = None
        hazardState = eventDict.get("state", "")
        if hazardState != "" :
            hazardState = hazardState.upper()
        eventDict["shapes"] = shapeList
        eventDict["stormMotion"] = stormMotion
        eventDict["pivots"] = pivotList

        # Cache some stuff the logic that composes the returned event set needs.
        self.forExecute = {}
        self.forExecute["eventID"] = eventDict["eventID"]
        self.forExecute["SiteID"] =  staticSettings["defaultSiteID"]
        self.forExecute["currentTime"] = currentTime
        self.forExecute["startTime"] = startTime
        self.forExecute["endTime"] = endTime
        self.forExecute["phenomena"] = phenomena
        self.forExecute["significance"] = significance
        self.forExecute["hazardPolygon"] = hazardPolygon

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

        # Try to pick up the existing event from what is passed in.
        sessionAttributes = eventSet.getAttributes()
        events = eventSet.getEvents()
        hazardEvent = None
        for e in events :
            hazardEvent = e
            break

        # Eventually we should have an event passed in to us, which we will
        # only have to modify.
        # if hazardEvent == None :
        #     return []


        # executeImpl does all the stuff we can safely do in a unit test,
        # basically whatever does not require Jep
        resultDict = self.executeImpl(sessionAttributes, \
                                      dialogInputMap, spatialInputMap)

        # Start creating our HazardEvent object, which is really backed by a
        # Java object.  str cast accounts for occasional json promotion of ascii
        # strings to unicode strings, which makes JEP barf.
        hazardEvent = EventFactory.createEvent()
        hazardEvent.setEventID(str(self.forExecute["eventID"]))
        hazardEvent.setSiteID(str(self.forExecute["SiteID"]))
        hazardEvent.setHazardState("PENDING")

        # New recommender framework requires some datetime objects, which must
        # be in units of seconds.
        hazardEvent.setIssueTime( datetime.datetime.fromtimestamp( \
          self.forExecute["currentTime"]/MILLIS_PER_SECOND) )
        hazardEvent.setStartTime( datetime.datetime.fromtimestamp( \
          self.forExecute["startTime"]/MILLIS_PER_SECOND) )
        hazardEvent.setEndTime( datetime.datetime.fromtimestamp( \
          self.forExecute["endTime"]/MILLIS_PER_SECOND) )

        hazardEvent.setPhenomenon(self.forExecute["phenomena"])
        hazardEvent.setSignificance(self.forExecute["significance"])
        geometry = GeometryFactory.createPolygon( \
                  self.forExecute["hazardPolygon"])
        hazardEvent.setGeometry(geometry)
        hazardEvent.setHazardMode("O")
        hazardEvent.setHazardAttributes(resultDict)
        
        return hazardEvent

    def toString(self):
        return "Modify Storm Track Tool!"

