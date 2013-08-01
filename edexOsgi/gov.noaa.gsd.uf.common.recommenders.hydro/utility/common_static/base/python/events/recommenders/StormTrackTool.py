"""
Storm Track Recommender

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
        metaDict["toolName"] = "StormTrackTool"
        metaDict["author"] = "GSD"
        metaDict["toolType"] = "Recommender"
        metaDict["outputFormat"] = "PYTHON"
        metaDict["returnType"] = "IEvent List"
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
        resultDict = {"outputType":"spatialInfo",
                      "label":"Drag Me To Storm", "returnType":"Point"}
        return resultDict

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

        # We assume it does not make much sense for an event associated with a
        # tracked object to last more than three hours.
        defaultDuration = staticSettings.get("defaultDuration", MILLIS_PER_HOUR)
        if defaultDuration > 3*MILLIS_PER_HOUR :
            defaultDuration = 3*MILLIS_PER_HOUR

        # Pull the rest of the time info we need out of the session info.
        framesInfo = sessionAttributes.get("framesInfo")
        sessionFrameTimes = framesInfo["frameTimeList"]
        lastFrameIndex = len(sessionFrameTimes)-1
        currentTime = long(sessionAttributes["currentTime"])
        startTime = currentTime
        endTime = startTime + defaultDuration

        # Get information about 'dragmeto' point. In previous incarnations,
        # there has been some uncertainty as to what the units of the
        # draggedPointTime are.
        points = spatialInputMap["spatialInfo"]["points"]
        draggedPointTuple = points[0]
        draggedPoint = draggedPointTuple[0]
        draggedPointTime = long(draggedPointTuple[1])
        if draggedPointTime < VERIFY_MILLISECONDS :
            draggedPointTime = draggedPointTime * MILLIS_PER_SECOND

        # For now default the initial motion and bearing.  Longer the default
        # duration, the slower our default motion.
        defaultSpeed = 20 # kts
        stormMotion = {}
        stormMotion["speed"] = \
              defaultSpeed * 30*MILLIS_PER_MINUTE / defaultDuration
        stormMotion["bearing"] = 225 # from SW

        # Construct a PointTrack object, and reinitialize it with the default
        # motion located where our starting point was dragged to.
        pointTrack = PointTrack()
        latLon0 = LatLonCoord(draggedPoint[0], draggedPoint[1])
        motion0 = Motion(stormMotion["speed"], stormMotion["bearing"])
        pointTrack.latLonMotionOrigTimeInit_( \
             latLon0, draggedPointTime, motion0, startTime)
        pivotList = [ draggedPointTime ]

        # Create a list of tracking points for each frame.
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

        # Compute a working delta time between frames.
        if lastFrameIndex<1 :
            frameTimeStep = endTime-startTime
        else :
            frameTimeSpan = \
                 sessionFrameTimes[lastFrameIndex]-sessionFrameTimes[0]
            frameTimeStep = frameTimeSpan/lastFrameIndex

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

        # now get the polygon
        latLonPoly = pointTrack.polygonDef(startTime, endTime)
        hazardPolygon = []
        for latLonVertex in latLonPoly :
            hazardPolygon.append([latLonVertex.lon, latLonVertex.lat])
        polygonShape = { "include" : "true" }
        polygonShape["shapeType"] = "polygon"
        polygonShape["points"] = hazardPolygon
        shapeList.append(polygonShape)

        # Finalize our set of output attributes.
        resultDict = {}
        resultDict["modifyCallbackToolName"] = "ModifyStormTrackTool"
        resultDict["creationTime"] = currentTime
        resultDict["shapes"] = shapeList
        resultDict["startTime"] = startTime
        resultDict["endTime"] = endTime
        resultDict["draggedPoints"] = \
            [ (  (latLon0.lon, latLon0.lat), draggedPointTime ) ]
        resultDict["state"] = "pending"
        resultDict["stormMotion"] = stormMotion
        resultDict["pivots"] = pivotList

        # Eventually we want the hazard type to be totally undefined when
        # we fire up the tracker.  However, for now we can make the initial
        # type be sensitive to the current setting.
        phenomena = "TO"
        resultDict["type"] = "TO.W"
        significance = "WARNING"
        visibleTypes = staticSettings.get("visibleTypes")
        if visibleTypes != None :
            if "FF.W.Convective" in visibleTypes :
                phenomena = "FF"
                resultDict["type"] = "FF.W.Convective"

        # Cache some stuff the logic that composes the returned event set needs.
        self.forExecute = {}
        self.forExecute["SiteID"] =  staticSettings["defaultSiteID"]
        self.forExecute["currentTime"] = currentTime
        self.forExecute["startTime"] = startTime
        self.forExecute["endTime"] = endTime
        self.forExecute["phenomena"] = phenomena
        self.forExecute["significance"] = significance
        self.forExecute["hazardPolygon"] = hazardPolygon

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

        # executeImpl does all the stuff we can safely do in a unit test,
        # basically whatever does not require Jep
        resultDict = self.executeImpl(eventSet.getAttributes(), \
                                      dialogInputMap, spatialInputMap)

        # Start creating our HazardEvent object, which is really backed by a
        # Java object.  str cast accounts for occasional json promotion of ascii
        # strings to unicode strings, which makes JEP barf.
        hazardEvent = EventFactory.createEvent()
        hazardEvent.setEventID("")
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
        return "Storm Track Tool!"    
