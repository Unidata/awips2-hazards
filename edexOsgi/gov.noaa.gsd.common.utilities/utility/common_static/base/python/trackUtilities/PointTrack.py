import time
from TrackCoordConv import *
from Motion import Motion
from HazardConstants import *

"""
Description: Knows everything about the motion of a single arbitrary
# point on the Earth's surface over time.  The track and movement so defined
# is along a circle defined by the intersection of a plane with the Earth's
# surface.  Because that plane does not necessarily pass through the Earth's
# center, a track can be curved. Furthermore, the speed of motion can be
# subject to a constant acceleration.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""
class PointTrack:

    # default constructor
    def __init__(self):
        self.__origTime = 0
        self.__tcc = TrackCoordConv()
        self.__speedAndAngle = Motion()
        self.__origSpeed = 0.0
        self.__origAcc = 0.0
        self.__minDt = 0
        self.__maxDt = 0
        self.__minX = 0.0
        self.__maxX = 0.0
        self.__npivots = 0

    # tests whether two PointTrack objects have the same internal data
    def same(self, other):
        if self.__npivots!=other.__npivots :
           return False
        if self.__npivots==0 : 
           return True
        if self.__origTime!=other.__origTime : 
           return False
        if self.__origSpeed!=other.__origSpeed or \
           self.__origAcc!=other.__origAcc : 
           return False
        if self.__tcc.same(other.__tcc) :
           return True
        return False

    # acts like a copy constructor
    def _PointTrack(self, other):
        self.__origTime = other.__origTime
        self.__tcc._TrackCoordConv(other.__tcc)
        self.__speedAndAngle._Motion(other.__speedAndAngle)
        self.__origSpeed = other.__origSpeed
        self.__origAcc = other.__origAcc
        self.__minDt = other.__minDt
        self.__maxDt = other.__maxDt
        self.__minX = other.__minX
        self.__maxX = other.__maxX
        self.__npivots = other.__npivots

    # returns True if object describes a meaningful motion
    def PT_ok(self):
        return self.__npivots>0

    # number of pivots the object was constructed with
    def PT_getNpivots(self):
        return self.__npivots

    # Complete the construction of the tracking object by computing the 
    # possible ranges of time and x coordinate.
    def __finishTrackPT(self):
        if self.__origAcc==0.0 and self.__origSpeed==0 :
           return
        self.__minX = self.__tcc.TCC_getXmin()
        self.__maxX = self.__tcc.TCC_getXmax()
        if self.__origAcc==0 :
           self.__maxDt = self.__maxX/self.__origSpeed
           self.__maxX = self.__maxDt*self.__origSpeed
           self.__minDt = -self.__maxDt
           self.__minX = -self.__maxX
        elif self.__origSpeed==0 :
           if self.__origAcc>0 :
              self.__maxDt = math.sqrt(self.__maxX/self.__origAcc)
              self.__maxX = self.__maxDt*self.__origAcc*self.__maxDt
              self.__minX = 0
           else :
              self.__minDt = -math.sqrt(self.__minX/self.__origAcc)
              self.__minX = self.__minDt*self.__origAcc*self.__minDt
              self.__maxX = 0
        elif self.__origAcc>0 :
           q = self.__origSpeed*self.__origSpeed+4*self.__maxX*self.__origAcc
           self.__maxDt = (math.sqrt(q)-self.__origSpeed) / (2*self.__origAcc)
           self.__maxX =  self.__origSpeed*self.__maxDt+ \
                          self.__maxDt*self.__origAcc*self.__maxDt
           q = self.__origSpeed*self.__origSpeed+4*self.__minX*self.__origAcc
           if q<0 :
              q = 0
           self.__minDt = (math.sqrt(q)-self.__origSpeed) / (2*self.__origAcc)
           self.__minX = self.__origSpeed*self.__minDt+ \
                         self.__minDt*self.__origAcc*self.__minDt
        else :
           q = self.__origSpeed*self.__origSpeed+4*self.__minX*self.__origAcc
           self.__minDt = (math.sqrt(q)-self.__origSpeed) / (2*self.__origAcc)
           self.__minX = self.__origSpeed*self.__minDt+ \
                         self.__minDt*self.__origAcc*self.__minDt
           q = self.__origSpeed*self.__origSpeed+4*self.__maxX*self.__origAcc
           if q<0 :
              q = 0
           self.__maxDt = (math.sqrt(q)-self.__origSpeed) / (2*self.__origAcc)
           self.__maxX =  self.__origSpeed*self.__maxDt+ \
                          self.__maxDt*self.__origAcc*self.__maxDt

    # Return the forward coordinate as a function of time displacement from
    # the origin time
    def PT_xOfDt(self, dt) :
        if dt==0 :
           return 0
        if dt>=self.__maxDt :
           return self.__maxX
        if dt<=self.__minDt :
           return self.__minX
        return self.__origSpeed*dt+dt*self.__origAcc*dt

    # Return the time displacement from the origin as a function of
    # the forward coordinate 
    def PT_dtOfX(self, x) :
        if x==0 :
           return 0
        if x>=self.__maxX :
           return self.__maxDt
        if x<=self.__minX :
           return self.__minDt
        if self.__origAcc==0.0 :
           return x/self.__origSpeed
        q = self.__origSpeed*self.__origSpeed+4*x*self.__origAcc
        if q>=0.0 :
           return (math.sqrt(q)-self.__origSpeed) / (2*self.__origAcc)
        elif self.__origAcc<0 :
           return self.__maxDt
        else : 
           return self.__minDt

    # Assuming the tracker is at the forward coordinate corresponding to the
    # specified time, returns the time at which the forward coordinate is
    # that plus the supplied displacement.
    def PT_moveTime(self, time0, dFwdKm) :
        dt = self.PT_dtOfX(self.PT_xOfDt(time0-self.__origTime)+dFwdKm)
        return self.__origTime+dt

    # Does the real work associated with initializing the object based on
    # a location, time, and motion.
    def __initPT(self, latLon0, time0, motion, otime) :
        if latLon0.lat<-90.0 or latLon0.lat>90.0 or \
           latLon0.lon<-180.0 or latLon0.lon>180.0 :
           return
        if motion.bearing<0.0 or motion.bearing>360.0 or \
           motion.speed<-0.16 or motion.speed>300.0 :
           return
        self.__speedAndAngle._Motion(motion)
        self.__npivots = 1
        if motion.speed==0.0 :
           self.__tcc._TCC_latLon_bearing(latLon0, 90)
           self.__speedAndAngle.bearing = 270
           self.__origTime = otime
           return
        # km/s -> knots
        if self.__speedAndAngle.speed<0.0 :
           self.__speedAndAngle.speed = self.__speedAndAngle.speed*-1944.0
        if self.__speedAndAngle.bearing>180.0 :
           bearing = self.__speedAndAngle.bearing-180.0
        else :
           bearing = self.__speedAndAngle.bearing+180.0
        self.__tcc._TCC_latLon_bearing(latLon0, bearing)
        # kts -> km/sec
        self.__origSpeed = self.__speedAndAngle.speed/1944.0
        self.__origTime = time0
        if otime==self.__origTime :
           self.__finishTrackPT()
           return
        self.__tcc._TCC_advanceOrig(self.__origSpeed*(otime-self.__origTime))
        bearing = self.__tcc.TCC_getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        self.__origTime = otime
        self.__finishTrackPT()

    # Public interface for initializing the object based on a location, time,
    # and motion, defaulting the origin time to the current time.
    def _PT_latLon_motion(self, latLon0, time0, motion) :
        self.__initPT(latLon0, time0, motion, time.time())

    # Public interface for initializing the object based on a location, time,
    # and motion, setting the origin time to the supplied value.
    def _PT_latLon_motion_origTime(self, latLon0, time0, motion, otime) :
        self.__initPT(latLon0, time0, motion, otime)

    # Does the real work associated with initializing the object based on
    # two location-time pairs.
    def __initPT2(self, latLon1, time1, latLon2, time2, otime) :
        if latLon1.lat<-90.0 or latLon1.lat>90.0 or \
           latLon1.lon<-180.0 or latLon1.lon>180.0 :
           return
        if latLon2.lat<-90.0 or latLon2.lat>90.0 or \
           latLon2.lon<-180.0 or latLon2.lon>180.0 :
           return
        dt = time2-time1
        if dt==0 :
           return
        self.__npivots = 2
        if latLon1.lat==latLon2.lat and latLon1.lon==latLon2.lon :
           self.__tcc._TCC_latLon_bearing(latLon1, 90.0)
           self.__speedAndAngle.bearing = 270.0
           self.__origTime = otime
           return
        self.__tcc._TCC_two_latLon_time(latLon1, time1, latLon2, time2)
        self.__origSpeed = (self.__tcc.TCC_fwdLeftKmOf(latLon2).fwd - \
                            self.__tcc.TCC_fwdLeftKmOf(latLon1).fwd)/dt
        if self.__origSpeed < 0 :
           self.__origSpeed = -self.__origSpeed
        self.__speedAndAngle.speed = 1944.0*self.__origSpeed
        bearing = self.__tcc.TCC_getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        if time2>time1 :
           self.__origTime = time2
        else :
           self.__origTime = time1
        if otime==self.__origTime :
            self.__finishTrackPT()
            return
        self.__tcc._TCC_advanceOrig(self.__origSpeed*(otime-self.__origTime))
        bearing = self.__tcc.TCC_getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        self.__origTime = otime
        self.__finishTrackPT()

    # Public interface for initializing the object based on two location-time
    # pairs, defaulting the origin time to the current time.
    def _PT_two_latLon(self, latLon1, time1, latLon2, time2) :
        self.__initPT2(latLon1, time1, latLon2, time2, time.time())

    # Public interface for initializing the object based on two location-time
    # pairs, setting the origin time to the supplied value.
    def _PT_two_latLon_origTime(self, latLon1, time1, latLon2, time2, otime) :
        self.__initPT2(latLon1, time1, latLon2, time2, otime)

    # Does the real work associated with initializing the object based on
    # three location-time pairs.
    def __initPT3(self, latLon1, time1, latLon2, time2, latLon3, time3, otime) :
        if latLon1.lat<-90.0 or latLon1.lat>90.0 or \
           latLon1.lon<-180.0 or latLon1.lon>180.0 :
           return
        if latLon2.lat<-90.0 or latLon2.lat>90.0 or \
           latLon2.lon<-180.0 or latLon2.lon>180.0 :
           return
        if latLon3.lat<-90.0 or latLon3.lat>90.0 or \
           latLon3.lon<-180.0 or latLon3.lon>180.0 :
           return
        if time1==time2 or time2==time3 or time1==time3 :
           return
        self.__tcc._TCC_three_latLon_time( \
           latLon1, time1, latLon2, time2, latLon3, time3)
        if not self.__tcc.TCC_ok() :
           return
        if time3>time1 and time3>time2 :
           self.__origTime = time3
           dtA = time1-self.__origTime
           dxA = self.__tcc.TCC_fwdLeftKmOf(latLon1).fwd
           dtB = time2-self.__origTime
           dxB = self.__tcc.TCC_fwdLeftKmOf(latLon2).fwd
        elif time2>time1 and time2>time3 :
           self.__origTime = time2
           dtA = time1-self.__origTime
           dxA = self.__tcc.TCC_fwdLeftKmOf(latLon1).fwd
           dtB = time3-self.__origTime
           dxB = self.__tcc.TCC_fwdLeftKmOf(latLon3).fwd
        else :
           self.__origTime = time1
           dtA = time2-self.__origTime
           dxA = self.__tcc.TCC_fwdLeftKmOf(latLon2).fwd
           dtB = time3-self.__origTime
           dxB = self.__tcc.TCC_fwdLeftKmOf(latLon3).fwd
        if dxA/dtA==dxB/dtB :
           self.__origAcc = 0.0
           self.__origSpeed = dxA/dtA
        else :
           dt2A = dtA*dtA
           dt2B = dtB*dtB
           rrr = dt2A/dt2B
           self.__origSpeed = (dxA-dxB*rrr)/(dtA-dtB*rrr)
           self.__origAcc = (dxA-dtA*self.__origSpeed)/dt2A
        if otime!=self.__origTime :
           dt = otime-self.__origTime
           dx = self.__origSpeed*dt+dt*self.__origAcc*dt
           self.__tcc._TCC_advanceOrig(dx)
           self.__origSpeed = self.__origSpeed+dt*self.__origAcc
           self.__origTime = otime
        self.__speedAndAngle.speed = 1944.0*self.__origSpeed
        bearing = self.__tcc.TCC_getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        self.__npivots = 3
        self.__finishTrackPT()

    # Public interface for initializing the object based on three location-time
    # pairs, defaulting the origin time to the current time.
    def _PT_three_latLon(self, latLon1, time1, latLon2, time2, latLon3, time3) :
        self.__initPT3(latLon1, time1, latLon2, time2, \
                       latLon3, time3, time.time())

    # Public interface for initializing the object based on three location-time
    # pairs, setting the origin time to the supplied value.
    def _PT_three_latLon_origTime(self, latLon1, time1, \
                                  latLon2, time2, latLon3, time3, otime) :
        self.__initPT3(latLon1, time1, latLon2, time2, latLon3, time3, otime)

    def PT_getOrigin(self) :
        return self.__tcc.TCC_getOrigin()

    def PT_getOrigTime(self) :
        return self.__origTime

    def PT_getSpeedAndAngle(self) :
        return self.__speedAndAngle

    def PT_getOrigSpeed(self) :
        return self.__origSpeed

    def PT_getOrigAcc(self) :
        return self.__origAcc

    def PT_getRadius(self) :
        return self.__tcc.TCC_getRadius()

    def PT_getMinDt(self) :
        return self.__minDt

    def PT_getMaxDt(self) :
        return self.__maxDt

    def PT_getMinTime(self) :
        return self.__minDt+self.__origTime

    def PT_getMaxTime(self) :
        return self.__maxDt+self.__origTime

    def PT_getMinX(self) :
        return self.__minX

    def PT_getMaxX(self) :
        return self.__maxX

    def PT_getTCC(self) :
        return self.__tcc

    # returns motion speed and bearing as a function of time.
    def PT_speedAndAngleOf(self, time0) :
        if self.__npivots==0 :
           return Motion(0,0)
        if time0==0 or time0==self.__origTime :
           return self.__speedAndAngle
        #dt = otime - self.__origTime
        dt = time0- self.__origTime
        if dt>self.__maxDt or dt<self.__minDt :
           return Motion(0,0)
        bearing = self.__tcc.TCC_bearingOf(self.PT_xOfDt(dt))
        if bearing > 180.0 :
           bearing = bearing - 180.0
        else :
           bearing = bearing + 180.0
        return Motion((self.__origSpeed+self.__origAcc*dt)*1944.0, bearing)

    # returns lat/lon of point as a function of time.
    def PT_trackPoint(self, time0) :
        if self.__npivots==0 :
           return LatLonCoord(1e12,1e12)
        return self.__tcc.TCC_latLonFrom(  \
                 self.PT_xOfDt(time0-self.__origTime), 0)

    # computes the track relative coordinate of the point at the given time,
    # adds the supplied track relative offset, and returns the lat/lon of that
    # location.
    def PT_offsetPointOf(self, time0, fwdLeftKm) :
        if self.__npivots==0 :
           return LatLonCoord()
        return self.__tcc.TCC_latLonFrom( \
          self.PT_xOfDt(time0-self.__origTime)+fwdLeftKm.fwd, fwdLeftKm.left)

    # computes the track relative coordinate of the point at the given time,
    # adds the supplied track relative offset, and returns the lat/lon of that
    # location.
    def PT_offsetPointFrom(self, time0, fwdKm, leftKm) :
        if self.__npivots==0 :
           return LatLonCoord()
        return self.__tcc.TCC_latLonFrom( \
          self.PT_xOfDt(time0-self.__origTime)+fwdKm, leftKm)

    # returns vector in track relative coordinates representing displacement
    # from location of point at given time to the supplied lat/lon.
    def PT_offsetKm(self, time0, latLon) :
        if self.__npivots==0 :
           return TrackCoord()
        onetc = self.__tcc.TCC_fwdLeftKmOf(latLon)
        onetc.x = onetc.fwd-self.PT_xOfDt(time0-self.__origTime)
        return onetc

    # If there is a time at which the input point is at the same track relative
    # forward coordinate as the object being tracked, return a list containing
    # that time and how far to the left of the track (negative is right) the
    # object is.  Otherwise return an empty list.
    def PT_timeAndLeft(self, latLon) :
        if self.__npivots==0 or self.__origSpeed==0 and self.__origAcc==0 :
           return []
        fwdLft = self.__tcc.TCC_fwdLeftKmOf(latLon)
        if fwdLft.fwd<self.__minX or fwdLft.fwd>self.__maxX :
           return []
        return [ self.__origTime+self.PT_dtOfX(fwdLft.fwd) , fwdLft.left ]

    def PT_doffsetKm(self, latLon0, latLon1) :
        onetc = self.__tcc.fwdLeftKmOf(latLon0)
        onetc._TC_minus(self.__tcc.fwdLeftKmOf(latLon1))
        return onetc

    # Returns a polygon that encloses the area swept out by the point over
    # the given range of time.  The arguments extendMin, extendMax, 
    # extendBck and extendFwd can be used to assign a 'footprint' to the
    # feature beyond a single point.  These arguments are all in kilometers.
    # extendMin is the size of the footprint left and right of the point at
    # at minTime, and extendMax is the size of the footprint left and right of
    # the point at maxTime.  extendBck extends the footprint behind the feature
    # extendFwd extends the footprint ahead of the feature.  If the path is
    # curved, enough points will be added along the direction of the movement
    # to describe the curved movement to the specified precision.
    def PT_enclosedBy(self, minTime, maxTime, \
                      extendMin, extendMax, \
                      extendBck=0.0, extendFwd=0.0, \
                      precision=2.0) :
        if self.__npivots==0 or minTime>maxTime :
           return []
        if extendMin<=0.0 and extendMax<=0.0 or \
           extendBck<=0.0 and extendFwd<=0.0 and minTime==maxTime :
           return []
        if precision<1.0 :
           precision = 1.0
        if extendMin<0.0 :
           extendMin = 0.0
        if extendMax<0.0 :
           extendMax = 0.0
        if extendBck<0.0 :
           extendBck = 0.0
        if extendFwd<0.0 :
           extendFwd = 0.0
        n = 0
        oneRad = self.__tcc.TCC_getRadius()
        if oneRad!=0 :
           if oneRad<0 :
              oneRad = -oneRad
           stepdx = math.sqrt(8*precision*oneRad)
           alldx = extendBck + extendFwd + \
                   self.PT_xOfDt(maxTime-self.__origTime) - \
                   self.PT_xOfDt(minTime-self.__origTime)
           if stepdx<alldx :
              n = int(alldx/stepdx)
        retPoly = []
        retPoly.append(self.PT_offsetPointFrom(maxTime, extendFwd, extendMax))
        retPoly.append(self.PT_offsetPointFrom(maxTime, extendFwd, -extendMax))
        if n>0 :
           i = 0
           while i < n:
               wmin = (i+1.0)/(n+1.0)
               wmax = 1.0-wmin
               i = i+1
               extnow = extendMax*wmax+extendMin*wmin
               tnow = maxTime*wmax+minTime*wmin
               retPoly.append(self.PT_offsetPointFrom(tnow, 0, -extnow))
        retPoly.append(self.PT_offsetPointFrom(minTime, -extendBck, -extendMin))
        retPoly.append(self.PT_offsetPointFrom(minTime, -extendBck, extendMin))
        if n>0 :
           i = 0
           while i < n:
               wmax = (i+1.0)/(n+1.0)
               wmin = 1.0-wmax
               i = i+1
               extnow = extendMax*wmax+extendMin*wmin
               tnow = maxTime*wmax+minTime*wmin
               retPoly.append(self.PT_offsetPointFrom(tnow, 0, extnow))
        return retPoly

    # Returns a polygon that encloses the area swept out by the point over
    # the given range of time, assuming a default size 'footprint' of the
    # point.
    def PT_polygonDef(self, minTime, maxTime) :
        extendMin = 10.0
        extendBck = extendMin
        extendMax = 10.0+(maxTime-minTime)*5.0/3600.0
        extendFwd = extendMax
        return self.PT_enclosedBy(minTime, maxTime, extendMin, extendMax, \
                                  extendBck, extendFwd)

    # For all the locations of the tracked object between minTime and maxTime,
    # computes the time and distance/bearing of the closest approach to the
    # given location.  If the result of the computation is meaningful, a
    # list will be returned containing the following:
    #   Begining time of the closest approach (integer unix time)
    #   Ending time of the closest approach (integer unix time)
    #   Point on the tracked object of the closest approach (LatLonCoord)
    #   Track relative coordinate of point at closest approach (TrackCoord)
    #   Distance to point at closest approach (float)
    #   Bearing to point at closest approach (float)
    # If the computation is not meaninful, will return empty list.
    def PT_nearestOffset(self, latLon, minTime, maxTime) :
        if self.__npivots==0 or minTime>maxTime :
           return []
        timMin = self.PT_getMinDt()
        timMax = self.PT_getMaxDt()
        if minTime>timMax :
           maxTime = minTime
        elif maxTime<timMin :
           minTime = maxTime
        else :
           if maxTime>timMax :
              maxTime = timMax
           if minTime<timMin :
              minTime = timMin
        if minTime==maxTime :
           time0 = minTime
        else :
           talResult = self.PT_timeAndLeft(latLon)
           if len(talResult) == 2 :
               time0 = talResult[0]
               if time0>maxTime :
                  time0 = maxTime
               elif time0<minTime :
                  time0 = minTime
           elif self.__tcc.TCC_fwdLeftKmOf(latLon).fwd >= 0 :
               time0 = maxTime
           else :
               time0 = minTime
        startTime = time0
        endTime = time0
        ontrack = self.PT_trackPoint(time0)
        fwdLft =  self.PT_offsetKm(time0, latLon)
        if latLon.lon==ontrack.lon :
           if latLon.lat==ontrack.lat :
              return [startTime, endTime, ontrack, TrackCoord(), 0.0, 0.0]
           if latLon.lat>=ontrack.lat :
              dist = (latLon.lat-ontrack.lat)*kmPerDegLat
              return [startTime, endTime, ontrack, fwdLft, dist, 0.0]
           dist = (ontrack.lat-latLon.lat)*kmPerDegLat
           return [startTime, endTime, ontrack, fwdLft, dist, 180.0]
        wrkTcc = TrackCoordConv()
        wrkTcc._TCC_two_latLon(latLon, ontrack)
        dist = -wrkTcc.TCC_fwdLeftKmOf(latLon).fwd
        bearing = wrkTcc.TCC_getBearing()
        if bearing<180.0 :
           bearing = bearing+180.0
        else :
           bearing = bearing-180.0
        return [startTime, endTime, ontrack, fwdLft, dist, bearing]
