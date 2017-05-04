import Motion as Motion
import LatLonCoord as LatLonCoord
import PointTrack as PointTrack
from KML import KML

"""
Description: TODO

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""

class StormTrack:
    def __init__(self, root):
        self._root = root
        self._kml = KML(root)
        
    def getStormTrack(self, points, stormTrackStart, hazardStart, hazardEnd, motion=0, footprint=0, returnType="kml"):
        # points is a list of the form [((lat, lon), t)] 
        # all times are in secs
        
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
        # extendMin = extentMax = 10 km
        extendMin = 10
        extendMax = 20
        polygon = pt.PT_enclosedBy(minTime, maxTime, extendMin, extendMax, extendMin, extendMax)
        
        polyDict = {}
        polyPoints = []
        for point in polygon:
            polyPoints.append((point.lon, point.lat))
            
        polyDict["outer"] = [polyPoints]
        
        if returnType == "kml":
            return self._kml.createStormTrack(points, polyDict)
        else:
            return polyDict, ptTimeList

    def createStormTrack(self, points, stormTrackStart, hazardStart, hazardEnd, motion, footprint, returnTime1=False):
        # Create point track

        # Input:
        #    points -- list of [(lon,lat), time)] where time is in milliseconds past 1970
        #       What about climate data?
        #    motion -- (speed, bearing)
        #    footprint -- what is that?
        #    

        # Unpack points
        i = 0
        latLonList=[]
        timeList=[]
        for latLon, t in points:
            lat, lon = latLon
            latLon = LatLonCoord(lat, lon)
            latLonList.append(latLon)
            # Convert from ms to seconds
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
        times = []
        # For now, just make it at 5-minute intervals
        maxT = t0+duration
        while t0 < maxT:
            times.append(t0)
            t0 = t0 + 5*60
        return times


