import time
from TrackCoordConv import *
from Motion import Motion
from HazardConstants import *
from GeneralConstants import *

"""
Description: 
The PointTrack class uses a TrackCoordConv object to define the path of
a track.  The PointTrack class also knows about times and speed of motion.
Furthermore, the speed of motion can be subject to a constant acceleration.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Placed in code set.
Jul 30, 2013            James.E.Ramer       Updated the inline documentation.

@author James.E.Ramer@noaa.gov
@version 1.0
"""

# This is one of a set of helper classes for doing feature tracking.
# The other classes are LatLonCoord, Motion, EarthTriplet, TrackCoord,
# and TrackCoordConv.  In these classes,  methods with a leading underscore are 
# meant to be private.  Methods that end with an underscore can modify the
# contents of the class; methods with no trailing underscore do not change
# the contents of the class.

# Most of the main public interface for the TrackCoordConv and PointTrack
# classes refers to track relative coordinates as 'forward' and 'left'.
# However, internally these coordinates are often referred to with the
# more generic cartesian coordinate naming of 'X' and 'Y'.  This is a
# side effect of the code having been ported from C++.

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

    # This logic was ported from C++ that internally uses seconds.
    def _toSec(self, timeIn) :
        if timeIn > VERIFY_MILLISECONDS :
            return timeIn / 1000
        return timeIn

    # Test for equivalence with another PointTrack object
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

    # Make this object a deep copy of another PointTrack object
    def PointTrack_(self, other):
        self.__origTime = other.__origTime
        self.__tcc.TrackCoordConv_(other.__tcc)
        self.__speedAndAngle.Motion_(other.__speedAndAngle)
        self.__origSpeed = other.__origSpeed
        self.__origAcc = other.__origAcc
        self.__minDt = other.__minDt
        self.__maxDt = other.__maxDt
        self.__minX = other.__minX
        self.__maxX = other.__maxX
        self.__npivots = other.__npivots

    # returns True if object describes a meaningful motion
    def ok(self):
        return self.__npivots>0

    # number of pivots the object was constructed with
    def getNpivots(self):
        return self.__npivots

    # Complete the construction of the tracking object by computing the 
    # possible ranges of time and x coordinate.
    def _finishTrack(self):
        if self.__origAcc==0.0 and self.__origSpeed==0 :
           return
        self.__minX = self.__tcc.getXmin()
        self.__maxX = self.__tcc.getXmax()
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
    def xOfDt(self, dt) :
        if dt==0 :
           return 0
        if dt>=self.__maxDt :
           return self.__maxX
        if dt<=self.__minDt :
           return self.__minX
        return self.__origSpeed*dt+dt*self.__origAcc*dt

    # Return the time displacement from the origin as a function of
    # the forward coordinate 
    def dtOfX(self, x) :
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
    def moveTime(self, time0_, dFwdKm) :
        time0 = self._toSec(time0_)
        dt = self.dtOfX(self.xOfDt(time0-self.__origTime)+dFwdKm)
        return self.__origTime+dt

    # Does the real work associated with initializing the object based on
    # a location, time, and motion.
    def _initPT(self, latLon0, time0, motion, otime) :
        if latLon0.lat<-90.0 or latLon0.lat>90.0 or \
           latLon0.lon<-180.0 or latLon0.lon>180.0 :
           return
        if motion.bearing<0.0 or motion.bearing>360.0 or \
           motion.speed<-0.16 or motion.speed>300.0 :
           return
        self.__speedAndAngle.Motion_(motion)
        self.__npivots = 1
        if motion.speed==0.0 :
           self.__tcc.latLonBearingInit_(latLon0, 90)
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
        self.__tcc.latLonBearingInit_(latLon0, bearing)
        # kts -> km/sec
        self.__origSpeed = self.__speedAndAngle.speed/1944.0
        self.__origTime = time0
        if otime==self.__origTime :
           self._finishTrack()
           return
        self.__tcc.advanceOrig_(self.__origSpeed*(otime-self.__origTime))
        bearing = self.__tcc.getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        self.__origTime = otime
        self._finishTrack()

    # Public interface for initializing the object based on a location, time,
    # and motion, defaulting the origin time to the current time.
    def latLonMotionInit_(self, latLon0, time0_, motion) :
        time0 = self._toSec(time0_)
        self._initPT(latLon0, time0, motion, time.time())

    # Public interface for initializing the object based on a location, time,
    # and motion, setting the origin time to the supplied value.
    def latLonMotionOrigTimeInit_(self, latLon0, time0_, motion, otime_) :
        '''
        @summary: Public interface for initializing the object based on a
                  location, time, and motion, with separately specified
                  origin point for track coordinate system.
        @param latLon: LatLoonCoord object for location of feature at time0_
        @param time0_: Time at which feature is located at latLon0 in
                       either seconds or milliseconds.
        @param motion: Motion object describe movement of feature at time0_
        @param otime_: Time at which forward track coordinate is 0.0km
        '''
        time0 = self._toSec(time0_)
        otime = self._toSec(otime_)
        self._initPT(latLon0, time0, motion, otime)

    # Does the real work associated with initializing the object based on
    # two location-time pairs.
    def _initPT2(self, latLon1, time1, latLon2, time2, otime) :
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
           self.__tcc.latLonBearingInit_(latLon1, 90.0)
           self.__speedAndAngle.bearing = 270.0
           self.__origTime = otime
           return
        self.__tcc.twoLatLonTimeInit_(latLon1, time1, latLon2, time2)
        self.__origSpeed = (self.__tcc.fwdLeftKmOf(latLon2).fwd - \
                            self.__tcc.fwdLeftKmOf(latLon1).fwd)/dt
        if self.__origSpeed < 0 :
           self.__origSpeed = -self.__origSpeed
        self.__speedAndAngle.speed = 1944.0*self.__origSpeed
        bearing = self.__tcc.getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        if time2>time1 :
           self.__origTime = time2
        else :
           self.__origTime = time1
        if otime==self.__origTime :
            self._finishTrack()
            return
        self.__tcc.advanceOrig_(self.__origSpeed*(otime-self.__origTime))
        bearing = self.__tcc.getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        self.__origTime = otime
        self._finishTrack()

    # Public interface for initializing the object based on two location-time
    # pairs, defaulting the origin time to the current time.
    def twoLatLonInit_(self, latLon1, time1_, latLon2, time2_) :
        time1 = self._toSec(time1_)
        time2 = self._toSec(time2_)
        self._initPT2(latLon1, time1, latLon2, time2, time.time())

    # Public interface for initializing the object based on two location-time
    # pairs, setting the origin time to the supplied value.
    def twoLatLonOrigTimeInit_(self, latLon1, time1_, \
                                latLon2, time2_, otime_) :
        '''
        @summary: Public interface for initializing the object based on two
                  location-time pairs, with separately specified
                  origin point for track coordinate system.
        @param latLon1: LatLoonCoord object for location of feature at time1_
        @param time1_: Time at which feature is located at latLon1 in
                       either seconds or milliseconds.
        @param latLon2: LatLoonCoord object for location of feature at time2_
        @param time1_: Time at which feature is located at latLon2 in
                       either seconds or milliseconds.
        @param otime_: Time at which forward track coordinate is 0.0km
        '''
        time1 = self._toSec(time1_)
        time2 = self._toSec(time2_)
        otime = self._toSec(otime_)
        self._initPT2(latLon1, time1, latLon2, time2, otime)

    # Does the real work associated with initializing the object based on
    # three location-time pairs.
    def _initPT3(self, latLon1, time1, latLon2, time2, latLon3, time3, otime) :
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
        self.__tcc.threeLatLonTimeInit_( \
           latLon1, time1, latLon2, time2, latLon3, time3)
        if not self.__tcc.ok() :
           return
        if time3>time1 and time3>time2 :
           self.__origTime = time3
           dtA = time1-self.__origTime
           dxA = self.__tcc.fwdLeftKmOf(latLon1).fwd
           dtB = time2-self.__origTime
           dxB = self.__tcc.fwdLeftKmOf(latLon2).fwd
        elif time2>time1 and time2>time3 :
           self.__origTime = time2
           dtA = time1-self.__origTime
           dxA = self.__tcc.fwdLeftKmOf(latLon1).fwd
           dtB = time3-self.__origTime
           dxB = self.__tcc.fwdLeftKmOf(latLon3).fwd
        else :
           self.__origTime = time1
           dtA = time2-self.__origTime
           dxA = self.__tcc.fwdLeftKmOf(latLon2).fwd
           dtB = time3-self.__origTime
           dxB = self.__tcc.fwdLeftKmOf(latLon3).fwd
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
           self.__tcc.advanceOrig_(dx)
           self.__origSpeed = self.__origSpeed+dt*self.__origAcc
           self.__origTime = otime
        self.__speedAndAngle.speed = 1944.0*self.__origSpeed
        bearing = self.__tcc.getBearing()
        if bearing>180.0 :
           self.__speedAndAngle.bearing = bearing-180.0
        else :
           self.__speedAndAngle.bearing = bearing+180.0
        self.__npivots = 3
        self._finishTrack()

    # Public interface for initializing the object based on three location-time
    # pairs, defaulting the origin time to the current time.
    def threeLatLonInit_(self, latLon1, time1_, latLon2, time2_, \
                         latLon3, time3_) :
        time1 = self._toSec(time1_)
        time2 = self._toSec(time2_)
        time3 = self._toSec(time3_)
        self._initPT3(latLon1, time1, latLon2, time2, \
                       latLon3, time3, time.time())

    # Public interface for initializing the object based on three location-time
    # pairs, setting the origin time to the supplied value.
    def threeLatLonOrigTimeInit_(self, latLon1, time1_, latLon2, time2_, \
                                  latLon3, time3_, otime_) :
        time1 = self._toSec(time1_)
        time2 = self._toSec(time2_)
        time3 = self._toSec(time3_)
        otime = self._toSec(otime_)
        self._initPT3(latLon1, time1, latLon2, time2, latLon3, time3, otime)

    # Returns LatLonCoord of the (0 fwd, 0 left) point track relative.
    def getOrigin(self) :
        return self.__tcc.getOrigin()

    # Returns the time at which the tracked object is at the origin.
    def PT_getOrigTime(self) :
        return self.__origTime

    # Returns the Motion associated with the origin point.
    def getSpeedAndAngle(self) :
        return self.__speedAndAngle

    # Returns the speed of movement associated with the origin point.
    def getOrigSpeed(self) :
        return self.__origSpeed

    # Returns the forward rate of acceleration associated with the origin point.
    def getOrigAcc(self) :
        return self.__origAcc

    # Returns the radius of curvature of the track associated with the
    # origin point.
    def getRadius(self) :
        return self.__tcc.getRadius()

    # Returns the minimum time displacement from the origin time that can be
    # unambiguously resolved.
    def getMinDt(self) :
        return self.__minDt

    # Returns the maximum time displacement from the origin time that can be
    # unambiguously resolved.
    def getMaxDt(self) :
        return self.__maxDt

    # Returns the time associated with the point half-way backward along the 
    # circle representing the track path.
    def getMinTime(self) :
        return self.__minDt+self.__origTime

    # Returns the time associated with the point half-way forward along the 
    # circle representing the track path.
    def getMaxTime(self) :
        return self.__maxDt+self.__origTime

    # Returns the minimum workable forward coordinate, which is half-way 
    # backward along the circle representing the track path.
    def getMinX(self) :
        return self.__minX

    # Returns the maximum workable forward coordinate, which is half-way 
    # forward along the circle representing the track path.
    def getMaxX(self) :
        return self.__maxX

    # Returns the TrackCoordConv object being used by this PointTrack object.
    def PT_getTCC(self) :
        return self.__tcc

    # Returns speed and bearing as a function of time, as a Motion object.
    def speedAndAngleOf(self, time0_) :
        '''
        @summary: Returns movement vector of point as a function of time.
        @param time0_: Time to compute movement of feature for, in
                       either seconds or milliseconds.
        @return: Movement vector of feature at time0_, as a Motion object.
        '''
        time0 = self._toSec(time0_)
        if self.__npivots==0 :
           return Motion(0,0)
        if time0==0 or time0==self.__origTime :
           return self.__speedAndAngle
        #dt = otime - self.__origTime
        dt = time0- self.__origTime
        if dt>self.__maxDt or dt<self.__minDt :
           return Motion(0,0)
        bearing = self.__tcc.bearingOf(self.xOfDt(dt))
        if bearing > 180.0 :
           bearing = bearing - 180.0
        else :
           bearing = bearing + 180.0
        return Motion((self.__origSpeed+self.__origAcc*dt)*1944.0, bearing)

    # returns lat/lon of point as a function of time, as a LatLonCoord object.
    def trackPoint(self, time0_) :
        '''
        @summary: Returns lat/lon of point as a function of time.
        @param time0_: Time to compute location of feature for, in
                       either seconds or milliseconds.
        @return: Location of feature at time0_, as a LatLonCoord object.
        '''
        time0 = self._toSec(time0_)
        if self.__npivots==0 :
           return LatLonCoord(1e12,1e12)
        return self.__tcc.latLonFrom(  \
                 self.xOfDt(time0-self.__origTime), 0)

    # computes the track relative coordinate of the point at the given time,
    # adds the supplied track relative offset (as TrackCoord object), and 
    # returns the lat/lon of that location.
    def offsetPointOf(self, time0_, fwdLeftKm) :
        time0 = self._toSec(time0_)
        if self.__npivots==0 :
           return LatLonCoord()
        return self.__tcc.latLonFrom( \
          self.xOfDt(time0-self.__origTime)+fwdLeftKm.fwd, fwdLeftKm.left)

    # computes the track relative coordinate of the point at the given time,
    # adds the supplied track relative offset coordinates, and returns the
    #  lat/lon of that location.
    def offsetPointFrom(self, time0_, fwdKm, leftKm) :
        time0 = self._toSec(time0_)
        if self.__npivots==0 :
           return LatLonCoord()
        return self.__tcc.latLonFrom( \
          self.xOfDt(time0-self.__origTime)+fwdKm, leftKm)

    # returns vector in track relative coordinates representing displacement
    # from location of point at given time to the supplied lat/lon, as a
    # TrackCoord object.
    def offsetKm(self, time0_, latLon) :
        time0 = self._toSec(time0_)
        if self.__npivots==0 :
           return TrackCoord()
        onetc = self.__tcc.fwdLeftKmOf(latLon)
        onetc.x = onetc.fwd-self.xOfDt(time0-self.__origTime)
        return onetc

    # If there is a time at which the input point is at the same track relative
    # forward coordinate as the object being tracked, return a list containing
    # that time and how far to the left of the track (negative is right) the
    # object is.  Otherwise return an empty list.
    def timeAndLeft(self, latLon) :
        if self.__npivots==0 or self.__origSpeed==0 and self.__origAcc==0 :
           return []
        fwdLft = self.__tcc.fwdLeftKmOf(latLon)
        if fwdLft.fwd<self.__minX or fwdLft.fwd>self.__maxX :
           return []
        return [ self.__origTime+self.dtOfX(fwdLft.fwd) , fwdLft.left ]

    # In the track coordinate system, return the vector displacement from the
    # first LatLonCoord object to the second.  Returned as a TrackCoord object
    # with units of kilometers.
    def doffsetKm(self, latLon0, latLon1) :
        onetc = self.__tcc.fwdLeftKmOf(latLon0)
        onetc.minus_(self.__tcc.fwdLeftKmOf(latLon1))
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
    def enclosedBy(self, minTime_, maxTime_, \
                      extendMin, extendMax, \
                      extendBck=0.0, extendFwd=0.0, \
                      precision=2.0) :
        minTime = self._toSec(minTime_)
        maxTime = self._toSec(maxTime_)
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
        oneRad = self.__tcc.getRadius()
        if oneRad!=0 :
           if oneRad<0 :
              oneRad = -oneRad
           stepdx = math.sqrt(8*precision*oneRad)
           alldx = extendBck + extendFwd + \
                   self.xOfDt(maxTime-self.__origTime) - \
                   self.xOfDt(minTime-self.__origTime)
           if stepdx<alldx :
              n = int(alldx/stepdx)
        retPoly = []
        retPoly.append(self.offsetPointFrom(maxTime, extendFwd, extendMax))
        retPoly.append(self.offsetPointFrom(maxTime, extendFwd, -extendMax))
        if n>0 :
           i = 0
           while i < n:
               wmin = (i+1.0)/(n+1.0)
               wmax = 1.0-wmin
               i = i+1
               extnow = extendMax*wmax+extendMin*wmin
               tnow = maxTime*wmax+minTime*wmin
               retPoly.append(self.offsetPointFrom(tnow, 0, -extnow))
        retPoly.append(self.offsetPointFrom(minTime, -extendBck, -extendMin))
        retPoly.append(self.offsetPointFrom(minTime, -extendBck, extendMin))
        if n>0 :
           i = 0
           while i < n:
               wmax = (i+1.0)/(n+1.0)
               wmin = 1.0-wmax
               i = i+1
               extnow = extendMax*wmax+extendMin*wmin
               tnow = maxTime*wmax+minTime*wmin
               retPoly.append(self.offsetPointFrom(tnow, 0, extnow))
        return retPoly

    # Returns a polygon that encloses the area swept out by the point over
    # the given range of time, assuming a default size 'footprint' of the
    # point.
    def polygonDef(self, minTime_, maxTime_) :
        '''
        @summary: Returns a polygon that encloses the area swept out by the
                  feature over the given range of time, assuming a default
                  assuming a default size 'footprint' of the tracked point.
        @param minTime_: Start of time range; either seconds or milliseconds.
        @param maxTime_: End of time range; either seconds or milliseconds.
        @return: A list of LatLonCoord objects describing the area swept.
        '''
        minTime = self._toSec(minTime_)
        maxTime = self._toSec(maxTime_)
        extendMin = 10.0
        extendBck = extendMin
        extendMax = 10.0+(maxTime-minTime)*5.0/3600.0
        extendFwd = extendMax
        return self.enclosedBy(minTime, maxTime, extendMin, extendMax, \
                                  extendBck, extendFwd)

    # For all the locations of the tracked object between minTime and maxTime,
    # computes the time and distance/bearing of the closest approach to the
    # given location.  If the result of the computation is meaningful, a
    # list will be returned containing the following:
    #   Begining time of the closest approach (integer unix time in secs)
    #   Ending time of the closest approach (integer unix time in secs)
    #   Point on the tracked object of the closest approach (LatLonCoord)
    #   Track relative coordinate of point at closest approach (TrackCoord)
    #   Distance to point at closest approach (float)
    #   Bearing to point at closest approach (float)
    # If the computation is not meaninful, will return empty list.
    def nearestOffset(self, latLon, minTime_, maxTime_) :
        minTime = self._toSec(minTime_)
        maxTime = self._toSec(maxTime_)
        if self.__npivots==0 or minTime>maxTime :
           return []
        timMin = self.getMinDt()
        timMax = self.getMaxDt()
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
           talResult = self.timeAndLeft(latLon)
           if len(talResult) == 2 :
               time0 = talResult[0]
               if time0>maxTime :
                  time0 = maxTime
               elif time0<minTime :
                  time0 = minTime
           elif self.__tcc.fwdLeftKmOf(latLon).fwd >= 0 :
               time0 = maxTime
           else :
               time0 = minTime
        startTime = time0
        endTime = time0
        ontrack = self.trackPoint(time0)
        fwdLft =  self.offsetKm(time0, latLon)
        if latLon.lon==ontrack.lon :
           if latLon.lat==ontrack.lat :
              return [startTime, endTime, ontrack, TrackCoord(), 0.0, 0.0]
           if latLon.lat>=ontrack.lat :
              dist = (latLon.lat-ontrack.lat)*kmPerDegLat
              return [startTime, endTime, ontrack, fwdLft, dist, 0.0]
           dist = (ontrack.lat-latLon.lat)*kmPerDegLat
           return [startTime, endTime, ontrack, fwdLft, dist, 180.0]
        wrkTcc = TrackCoordConv()
        wrkTcc.twoLatLonInit_(latLon, ontrack)
        dist = -wrkTcc.fwdLeftKmOf(latLon).fwd
        bearing = wrkTcc.getBearing()
        if bearing<180.0 :
           bearing = bearing+180.0
        else :
           bearing = bearing-180.0
        return [startTime, endTime, ontrack, fwdLft, dist, bearing]
