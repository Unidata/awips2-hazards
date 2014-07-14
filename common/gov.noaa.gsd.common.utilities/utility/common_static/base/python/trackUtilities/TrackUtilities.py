"""
Description: TODO

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 15, 2012            Bryon.Lawrence@noaa.gov      Initial creation

@author Bryon.Lawrence@noaa.gov
@version 1.0
"""

import types

from ShapeUtilities import ShapeUtilities
from PointTrack import PointTrack
from Motion import Motion
from LatLonCoord import LatLonCoord
from KML import KML
from Utility import Utility

class TrackUtilities:

    def __init__(self):
        self.clientFrameTimes = None

    def setClientFrameTimes(self, newClientFrameTimes=None):
        if newClientFrameTimes == None or len(newClientFrameTimes) == 0 :
            self.clientFrameTimes = None
        else :
            self.clientFrameTimes = newClientFrameTimes
            nf = len(self.clientFrameTimes)
            i = 0
            while i < nf :
                if self.clientFrameTimes[i] > 2147483647 :
                    self.clientFrameTimes[i] = self.clientFrameTimes[i]/1000
                i = i+1
    
    def getStormTrackStart(self, startTime):  
        if self.clientFrameTimes != None :
            return self.clientFrameTimes[0]
        # Storm Track start is hard-coded for now to 50 minutes before the event start time
        # This was taken from the JavaScript version
        return startTime - 10*60*1000*5
    
    def replaceTrackingPoints(self, ptTimes, shapes, pointType="tracking"):
        # Replace the tracking points in shapes with the ptTimes given
        # First, Remove tracking points from shapes
        newShapes = []
        shapeUtilities = ShapeUtilities()
        
        for shape in shapes:
            if shape.get("pointType", "") != pointType:
                newShapes.append(shape)            
        # Add in new tracking points
        for ptTime in ptTimes:
            lonLat, t = ptTime
            pointShape = shapeUtilities.makeShape("point", lonLat, pointID=t)
            pointShape["pointType"]= pointType
            newShapes.append(pointShape)
        return newShapes

    def getStormTrack(self, points, stormTrackStart, hazardStart, hazardEnd, motion=0, footprint=0, returnType="kml"):
        
        motion = int(motion)
        footprint = int(footprint)
        pt,time1 = self.createStormTrack(points, stormTrackStart, hazardStart, hazardEnd, motion, footprint, True)
        
        # Determine the frame times
        trackDuration = (hazardEnd-stormTrackStart)  
        times = self.getFrameTimes(stormTrackStart, trackDuration)

        # Gather the points in the track
        points = []
        for tSec in times:
            points.append((pt.PT_trackPoint(tSec), tSec))

        ptTimeList = []
        for point, tSec in points:
            # Return ms in ptTimeList
            ptTimeList.append(((point.lon, point.lat), tSec*1000))

        # Create polygon
        hazardDuration = (hazardEnd-hazardStart) 
        minTime = hazardStart
        maxTime = hazardStart+hazardDuration
        # extendMin = extendMax = 10km
        extendMin = 10
        extendMax = 20
        polygon = pt.PT_enclosedBy(minTime, maxTime, extendMin, extendMax, extendMin, extendMax)
        
        polyDict = {}
        polyPoints = []
        for point in polygon:
            polyPoints.append((point.lon, point.lat))
            
        polyDict["outer"] = [polyPoints]
        
        return polyDict, ptTimeList, pt
    
    def createStormTrack(self, points, stormTrackStart, hazardStart, hazardEnd, motion, footprint, returnTime1=False):
        # Create point track

        # Input:
        #    points -- list of [(lon,lat), time)] where time is in milliseconds past 1970
        #       What about climate data?
        #    motion -- (speed, bearing)
        #    footprint -- what is that?
        #    
        i = 0
        
        latLonList=[]
        timeList=[]
        for latLon, t in points:
            lat, lon = latLon

            latLon = LatLonCoord(lat, lon)
            #exec "latLon"+`i`+ "= latLon"
            latLonList.append(latLon)
            # Convert from ms to seconds
            #exec "time"+`i`+"=t"
            timeList.append(t)
            i +=1
            
        drtTime = hazardStart
        # Initialize
        pt = PointTrack()
        if motion == 0:
            speed = 20 # kts
            bearing = 225 # degrees
        else:
            speed, bearing = motion
        motion = Motion(speed, bearing)
         
        if len(points) == 1:
            pt._PT_latLon_motion_origTime(latLonList[0], timeList[0], motion, drtTime)
        elif len(points) == 2:
            pt._PT_two_latLon_origTime(latLonList[0], timeList[0], latLonList[1], timeList[1], drtTime)
        elif len(points) == 3:
            pt._PT_three_latLon_origTime(latLonList[0], timeList[0], latLonList[1], timeList[1], latLonList[2], timeList[2], drtTime)
        if returnTime1: return pt, timeList[0]
        else: return pt
                
    def getFrameTimes(self, t0, duration):
        if self.clientFrameTimes != None :
            return self.clientFrameTimes
        times = []
        # For now, just make it at 5-minute intervals
        maxT = t0+duration
        while t0 < maxT:
            times.append(t0)
            t0 = t0 + 5*60
        return times
    
    def unpackPointTimeList(self, ptTimeList, sort=True):
        # ptTimeList is a str of the form: [((lon, lat), time)]
        if type(ptTimeList) != types.ListType:
            exec "pts = "+ptTimeList
        else:
            pts = ptTimeList
        if not pts: return []
        retList = []
        for lonLat, t in pts:
            lon, lat = lonLat
            t = int(float(t)/1000.0)  # convert from ms to seconds
            retList.append(((lat, lon), t))
        if sort:
            retList.sort(self.sortByTime)
        return retList
    
    def getDraggedPoints(self, eventDict):
        dp = eventDict.get("draggedPoints", [])
        dpLen = int(eventDict.get('draggedPointsLen', 2))
        dpRet = dp[:dpLen]
        return dpRet
    
    def sortByTime(self, pt1, pt2):
        latLon1, t1 = pt1
        latLon2, t2 = pt2
        if t1 < t2: return -1
        elif t2 < t1: return 1
        else: return 0
       
