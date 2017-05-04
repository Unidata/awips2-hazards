import math
from LatLonCoord import LatLonCoord
from TrackCoord import *
from EarthTriplet import EarthTriplet
from HazardConstants import *

"""
Description: 

All the math that converts a point along a track in track relative
coordinates into earth coordinates and vice-versa is embedded in the
class TrackCoordConv.  TrackCoordConv knows nothing about speed of
motion or times, it just knows where the center line of the track is
and how to do coordinate conversions between earth and track relative
coordinates.  TrackCoordConv reinitalizers that take time arguments only
use them for determining order along the track for the input points.
The track so defined is along the intersection of a plane with the
surface of the earth.  When this plane does not pass through the center
of the earth, the track is not a great circle and as such appears as a
curved track on the earth's surface.

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
# and PointTrack.  In these classes, methods with a leading underscore are 
# meant to be private.  Methods that end with an underscore can modify the
# contents of the class; methods with no trailing underscore do not change
# the contents of the class.

# Most of the main public interface for the TrackCoordConv and PointTrack
# classes refers to track relative coordinates as 'forward' and 'left'.
# However, internally these coordinates are often referred to with the
# more generic cartesian coordinate naming of 'X' and 'Y'.  This is a
# side effect of the code having been ported from C++.

class TrackCoordConv:
    def __init__(self):
        self.__ii = EarthTriplet()
        self.__jj = EarthTriplet()
        self.__kk = EarthTriplet()
        self.__dy = 0.0
        self.__xUnit = 0.0
        self.__yUnit = 0.0
        self.__origin = LatLonCoord()
        self.__bearing = -1.0
        self.__radius = 0.0

    # Test for equivalence with another TrackCoordConv object
    def same(self, other):
        if self.__origin.same(other.__origin) and \
           self.__bearing==other.__bearing and self.__radius==other.__radius :
           return True
        return False

    # Make this object a deep copy of another TrackCoordConv object
    def TrackCoordConv_(self, other):
        self.__ii.EarthTriplet_(other.__ii)
        self.__jj.EarthTriplet_(other.__jj)
        self.__kk.EarthTriplet_(other.__kk)
        self.__dy = other.__dy
        self.__xUnit = other.__xUnit
        self.__yUnit = other.__yUnit
        self.__origin.LatLonCoord_(other.__origin)
        self.__bearing = other.__bearing
        self.__radius = other.__radius

    # Shared core for both versions of one point reinitializer.
    def _initTCC(self, origin, bearing, radius) :
        self.__dy = 0.0
        self.__xUnit = 0.0
        self.__yUnit = 0.0
        self.__bearing = 0.0
        self.__radius = 0.0
        if origin.lat<-90.0 or origin.lat>90.0 or \
           origin.lon<-180.0 or origin.lon>180.0 :
           return
        if bearing<0.0 or bearing> 360.0 :
           return
        self.__xUnit = R_EARTH
        self.__yUnit = R_EARTH
        self.__origin.LatLonCoord_(origin)
        self.__bearing = bearing;
        self.__radius = radius;
        self.__ii.latLonInit_(origin)
        self.__jj = EarthTriplet(-self.__ii.yy, self.__ii.xx, 0)
        if not self.__jj.unit_() :
           ang = DEG_TO_RAD*(origin.lon-self.__ii.zz*bearing)
           self.__jj = EarthTriplet(-math.cos(ang), -math.sin(ang), 0)
           self.__bearing = 90.0
        self.__kk.cross_(self.__ii, self.__jj)
        if bearing!= 90.0 :
           bearing = bearing*DEG_TO_RAD;
           self.__jj.weight_(self.__jj, math.sin(bearing), \
                                self.__kk, math.cos(bearing) )
           self.__kk.cross_(self.__ii, self.__jj)
        if radius==0 or radius>999999 or radius<-999999 :
           return
        if radius>10 or radius<-10 :
           r2 = radius*radius;
           y2 = self.__yUnit*self.__yUnit;
           self.__xUnit = math.sqrt(r2*y2/(r2+y2))
        elif radius<0 :
           self.__xUnit = -radius
        else :
           self.__xUnit = radius
        cosRot = self.__xUnit/self.__yUnit
        sinRot = math.sqrt(1-cosRot*cosRot)
        if radius>0 :
           sinRot = -sinRot
        self.__ii.weight_(self.__ii, cosRot, self.__kk, sinRot)
        self.__kk.cross_(self.__ii, self.__jj)
        self.__dy = math.asin(sinRot)*self.__yUnit

    # One point reinitializer.  'bearing' is the meteorological direction in
    # which the forward direction initially points toward exactly at the origin.
    def latLonBearingInit_(self, latLon, bearing) :
        self._initTCC(latLon, bearing, 0)

    # One point reinitializer.  'bearing' is the meteorological direction in
    # which the forward direction initially points toward exactly at the origin.
    # Non-zero radius of curvature mean path departs from great circle.
    def latLonBearingRadiusInit_(self, latLon, bearing, radius) :
        self._initTCC(latLon, bearing, radius)

    # Shared core for both versions of two point reinitializer.
    def _initTCC2(self, beg, to) :
        self.__dy = 0.0
        self.__xUnit = 0.0
        self.__yUnit = 0.0
        self.__bearing = 0.0
        self.__radius = 0.0
        if beg.lat<-90.0 or beg.lat>90.0 or \
           beg.lon<-180.0 or beg.lon>180.0 or \
           to.lat<-90.0 or to.lat>90.0 or \
           to.lon<-180.0 or to.lon>180.0 or \
           beg.lat==to.lat and beg.lon==to.lon :
           return
        self.__origin.LatLonCoord_(to)
        et0 = EarthTriplet()
        et0.latLonInit_(beg)
        self.__ii.latLonInit_(to)
        self.__kk.cross_(et0, self.__ii)
        if not self.__kk.unit_() :
           return
        self.__jj.cross_(self.__kk, self.__ii)
        self.__xUnit = R_EARTH
        self.__yUnit = R_EARTH
        etE = EarthTriplet(-self.__ii.yy, self.__ii.xx, 0)
        if etE.unit_() :
           et0.cross_(self.__ii, etE)
           self.__bearing = math.atan2(self.__jj.dot(etE), \
                                       self.__jj.dot(et0) ) / DEG_TO_RAD
        elif to.lat>0 :
           self.__bearing = (beg.lon-to.lon)-180
        elif beg.lon>to.lon :
           self.__bearing = (beg.lon-to.lon)-180
        else :
           self.__bearing = (beg.lon-to.lon)+180
        if self.__bearing>360 :
           self.__bearing -= 360;
        elif self.__bearing<0 :
            self.__bearing += 360;

    # Two point reinitializer.  'beg' and 'to' are both LatLonCoord objects.
    # The 'to' point becomes the origin of the coordinate system established.
    def twoLatLonInit_(self, beg, to) :
        self._initTCC2(beg, to)

    # Two point reinitializer.  Note that the times only control the order,
    # we track from the earliest time to the latest time, and the point at
    # the latest time becomes the origin of the coordinate system.
    def twoLatLonTimeInit_(self, latLon1, time1, latLon2, time2) :
        if time1<time2 :
           self._initTCC2(latLon1, latLon2)
        else :
           self._initTCC2(latLon2, latLon1)

    # Shared core for both versions of three point reinitializer.
    def _initTCC3(self, beg, next, to) :
        self.__dy = 0.0
        self.__xUnit = 0.0
        self.__yUnit = 0.0
        self.__bearing = 0.0
        self.__radius = 0.0
        if beg.lat<-90.0 or beg.lat>90.0 or \
           beg.lon<-180.0 or beg.lon>180.0 or \
           next.lat<-90.0 or next.lat>90.0 or \
           next.lon<-180.0 or next.lon>180.0 or \
           to.lat<-90.0 or to.lat>90.0 or \
           to.lon<-180.0 or to.lon>180.0 :
           return
        self.__origin.LatLonCoord_(to)
        mag = 0.0
        et0 = EarthTriplet()
        et0.latLonInit_(beg)
        et1 = EarthTriplet()
        et1.latLonInit_(next)
        self.__ii.latLonInit_(to)
        ddA = EarthTriplet()
        ddA.diff_(et1, et0)
        ddB = EarthTriplet()
        ddB.diff_(self.__ii, et1)
        if ddA.dot(ddB)<0 :
           return
        self.__kk.cross_(ddA, ddB)
        if not self.__kk.unit_() :
           self.__kk.cross(et0, self.__ii)
           if not self.__kk.unit_() :
              return
        else :
           mag = self.__ii.dot(self.__kk)
        if mag<=-1.0 or mag>=1.0 :
           return
        self.__jj.cross_(self.__kk, self.__ii)
        if not self.__jj.unit_() :
           return
        self.__xUnit = R_EARTH
        self.__yUnit = R_EARTH
        etE = EarthTriplet(-self.__ii.yy, self.__ii.xx, 0)
        if not etE.unit_() :
           ang = to.lon*DEG_TO_RAD
           etE.latLonInit_(-math.sin(ang), math.cos(ang), 0)
        et0.cross_(self.__jj, etE)
        self.__bearing = \
          math.atan2(self.__jj.dot(etE),self.__jj.dot(et0))/DEG_TO_RAD
        if self.__bearing>360 :
           self.__bearing -= 360;
        elif self.__bearing<0 :
            self.__bearing += 360;
        if mag==0.0 :
           return;
        self.__ii.cross_(self.__jj, self.__kk)
        self.__xUnit = self.__xUnit*math.sqrt(1-mag*mag)
        self.__dy = -math.asin(mag)*self.__yUnit
        self.__radius = self.__xUnit/mag

    # Three point reinitializer.  'beg', 'next' and 'to' are all LatLonCoord
    # objects. The 'to' point becomes the origin of the coordinate system
    # established.
    def threeLatLonInit_(self, beg, next, to) :
        self._initTCC3(beg, next, to)

    # Three point reinitializer.  Note that the times only control the order,
    # we track from the earliest time to the latest time, and the point at
    # the latest time becomes the origin of the coordinate system.
    def threeLatLonTimeInit_(self, \
          latLon1, time1, latLon2, time2, latLon3, time3) :
        if time1<=time2 :
            if time2<=time3 :
                self._initTCC3(latLon1, latLon2, latLon3)
            elif time1<=time3 :
                self._initTCC3(latLon1, latLon3, latLon2)
            else :
                self._initTCC3(latLon3, latLon1, latLon2)
        else :
            if time2>time3 :
                self._initTCC3(latLon3, latLon2, latLon1)
            elif time1>time3 :
                self._initTCC3(latLon2, latLon3, latLon1)
            else :
                self._initTCC3(latLon2, latLon1, latLon3)

    # Most basic forward coordinate transform; returns a TrackCoord for a
    # LatLonCoord.  Units of TrackCoord are in kilometers.
    def fwdLeftKmOf(self, latLon) :
        if self.__xUnit==0.0 :
           return TrackCoord(1e37,1e37)
        if latLon.lat<-90.0 or latLon.lat>90.0 or \
           latLon.lon<-180.0 or latLon.lon>180.0 :
           return TrackCoord(1e37,1e37)
        et = EarthTriplet()
        et.latLonInit_(latLon)
        etx = EarthTriplet(self.__ii.dot(et), self.__jj.dot(et), \
                           self.__kk.dot(et) )
        if etx.xx!=0.0 or etx.yy!=0.0 :
            eqrad = math.sqrt(etx.xx*etx.xx+etx.yy*etx.yy)
            return TrackCoord(math.atan2(etx.yy,etx.xx)*self.__xUnit, \
                              math.atan2(etx.zz,eqrad)*self.__yUnit+self.__dy)
        elif etx.zz>0 :
            return TrackCoord(0, self.__dy+self.__yUnit*PI)
        else :
            return TrackCoord(0, self.__dy-self.__yUnit*PI)

    # Returns LatLonCoord of point on earth's surface that is along the axis of
    # the circle on the earth's surface that defines the track.
    def getPivot(self) :
        if self.__dy==0.0 :
           return LatLonCoord()
        latLon = self.__kk.getLatLon()
        if self.__dy<0.0 :
           return latLon
        latLon.lat = -latLon.lat
        latLon.lon = -latLon.lon
        return latLon

    # Returns LatLonCoord of the (0 fwd, 0 left) point track relative.
    def getOrigin(self) :
        return self.__origin

    # Returns compass bearing of forward direction of the track at the origin.
    def getBearing(self) :
        return self.__bearing

    # Returns the apparent radius of curvature of the track on the
    # earth's surface.
    def getRadius(self) :
        return self.__radius

    # Returns the minimum forward coordinate of the track.
    def getXmin(self) :
        return -self.__xUnit*PI

    # Returns the maximum forward coordinate of the track.
    def getXmax(self) :
        return self.__xUnit*PI

    # Returns the minimum left coordinate of the track.
    def getYmin(self) :
        return self.__dy-self.__yUnit*PI

    # Returns the maximum left coordinate of the track.
    def getYmax(self) :
        return self.__dy+self.__yUnit*PI

    # Returns boolean for whether the track was meaningfully initialized.
    def ok(self) :
        return self.__xUnit!=0

    # Most basic reverse coordinate transform; returns a LatLonCoord for a
    # TrackCoord; units of TrackCoord are in kilometers.
    def latLonOf(self, fwdLeftKm) :
        if self.__xUnit==0.0 :
           return LatLonCoord()
        lonrad = fwdLeftKm.fwd/self.__xUnit;
        if lonrad<-PI :
           lonrad = -PI
        elif lonrad>PI :
           lonrad = PI
        latrad = (fwdLeftKm.left-self.__dy)/self.__yUnit;
        if latrad<-PI/2.0 :
            latrad = -PI/2.0;
        elif latrad>PI/2.0 :
            latrad = PI/2.0;
        coslat = math.cos(latrad);
        etx = EarthTriplet(math.cos(lonrad)*coslat, math.sin(lonrad)*coslat, \
                           math.sin(latrad) )
        et = EarthTriplet( \
         etx.xx*self.__ii.xx+etx.yy*self.__jj.xx+etx.zz*self.__kk.xx, \
         etx.xx*self.__ii.yy+etx.yy*self.__jj.yy+etx.zz*self.__kk.yy, \
         etx.xx*self.__ii.zz+etx.yy*self.__jj.zz+etx.zz*self.__kk.zz)
        return et.getLatLon()

    # Reverse coordinate transform; returns a LatLonCoord for the components
    # of a TrackCoord; units of components are in kilometers.
    def latLonFrom(self, fwdKm, leftKm) :
        if self.__xUnit==0.0 :
           return LatLonCoord()
        lonrad = fwdKm/self.__xUnit;
        if lonrad<-PI :
           lonrad = -PI
        elif lonrad>PI :
           lonrad = PI
        latrad = (leftKm-self.__dy)/self.__yUnit;
        if latrad<-PI/2.0 :
            latrad = -PI/2.0;
        elif latrad>PI/2.0 :
            latrad = PI/2.0;
        coslat = math.cos(latrad);
        etx = EarthTriplet(math.cos(lonrad)*coslat, math.sin(lonrad)*coslat, \
                           math.sin(latrad) )
        et = EarthTriplet( \
         etx.xx*self.__ii.xx+etx.yy*self.__jj.xx+etx.zz*self.__kk.xx, \
         etx.xx*self.__ii.yy+etx.yy*self.__jj.yy+etx.zz*self.__kk.yy, \
         etx.xx*self.__ii.zz+etx.yy*self.__jj.zz+etx.zz*self.__kk.zz)
        return et.getLatLon()

    # Returns compass bearing of the track at the given forward coordinate
    # value in kilometers from origin point.
    def bearingOf(self, fwdKm) :
        if self.__xUnit==0.0 :
           return -1.0
        lonrad = fwdKm/self.__xUnit;
        if lonrad<-PI :
           lonrad = -PI
        elif lonrad>PI :
           lonrad = PI
        latrad = -self.__dy/self.__yUnit;
        coslat = math.cos(latrad);
        etx = EarthTriplet(math.cos(lonrad)*coslat, math.sin(lonrad)*coslat, \
                           math.sin(latrad) )
        et = EarthTriplet( \
         etx.xx*self.__ii.xx+etx.yy*self.__jj.xx+etx.zz*self.__kk.xx, \
         etx.xx*self.__ii.yy+etx.yy*self.__jj.yy+etx.zz*self.__kk.yy, \
         etx.xx*self.__ii.zz+etx.yy*self.__jj.zz+etx.zz*self.__kk.zz)
        etx.cross_(self.__kk, et)
        if not etx.unit_() :
           return -1.0
        etE = EarthTriplet(-et.yy, et.xx, 0)
        b = 0.0
        if not etE.unit_() :
           etE = EarthTriplet(0, 1, 0)
        etN = EarthTriplet()
        etN.cross_(et, etE)
        b = math.atan2( etx.dot(etE), etx.dot(etN) )/DEG_TO_RAD
        if b>360.0 :
           return b-360.0
        if b<0.0 :
           return b+360.0
        return b

    # Relocate the origin of the track the given number of kilometers forward
    # along the existing track.
    def advanceOrig_(self, fwdKm) :
       if self.__xUnit==0.0 :
          return
       ang = fwdKm/self.__xUnit
       self.__jj.weight_(self.__ii, -math.sin(ang), \
                            self.__jj, math.cos(ang))
       self.__ii.cross_(self.__jj, self.__kk)
       et = EarthTriplet()
       if self.__dy==0.0 :
          et.EarthTriplet_(self.__ii)
       else :
          ang = -self.__dy/self.__yUnit
          et.weight_(self.__ii, math.cos(ang), self.__kk, math.sin(ang))
       self.__origin = et.getLatLon()
       etx = EarthTriplet()
       etx.cross_(self.__kk, et)
       if not etx.unit_() :
          return
       etE = EarthTriplet(-et.yy, et.xx, 0)
       if not etE.unit_() :
          etE = EarthTriplet(0, 1, 0)
       etN = EarthTriplet()
       etN.cross_(et, etE)
       self.__bearing = \
           math.atan2( etx.dot(etE), etx.dot(etN) )/DEG_TO_RAD
       if self.__bearing>360.0 :
          self.__bearing = self.__bearing-360.0
       elif self.__bearing<0.0 :
          self.__bearing = self.__bearing+360.0

    # If the track represented by this TrackCoordConv and the track for another
    # intersect, return a list of two LatLonCoord objects which are the two
    # points of intersection. If they do not intersect return empty list.
    def intersect(self, other) :
       retIntersect = []
       if self.__xUnit==0 or other.__xUnit==0 :
          return retIntersect
       LL = EarthTriplet()
       LL.cross_(self.__kk, other.__kk)
       if not LL.unit_() :
          return retIntersect
       if self.__dy==0 and other.__dy==0 :
          latLon = LL.getLatLon()
          retIntersect.append(latLon)
          retIntersect.append(LatLonCoord(-latLon.lat,-latLon.lon))
          return retIntersect
       CC = EarthTriplet()
       CC.mult_(self.__kk, -math.sin(self.__dy/self.__yUnit))
       CCo = EarthTriplet()
       CCo.mult_(other.__kk, -math.sin(other.__dy/other.__yUnit))
       FF = EarthTriplet()
       FF.cross_(LL, self.__kk)
       FFo = EarthTriplet()
       FFo.cross_(LL, other.__kk)
       xxff = FF.xx*FF.xx+FFo.xx*FFo.xx;
       yyff = FF.yy*FF.yy+FFo.yy*FFo.yy;
       zzff = FF.zz*FF.zz+FFo.zz*FFo.zz;
       if zzff<xxff and zzff<yyff :
           if FFo.xx==0.0 :
              ppA = CC.xx
              qqA = FF.xx
              ppAo = CCo.xx
              qqAo = FFo.xx
              ppB = CC.yy
              qqB = FF.yy
              ppBo = CCo.yy
              qqBo = FFo.yy
           else :
              ppA = CC.yy
              qqA = FF.yy
              ppAo = CCo.yy
              qqAo = FFo.yy
              ppB = CC.xx
              qqB = FF.xx
              ppBo = CCo.xx
              qqBo = FFo.xx
       elif yyff<=xxff and yyff<=zzff :
           if FFo.xx==0.0 :
              ppA = CC.xx
              qqA = FF.xx
              ppAo = CCo.xx
              qqAo = FFo.xx
              ppB = CC.zz
              qqB = FF.zz
              ppBo = CCo.zz
              qqBo = FFo.zz
           else :
              ppA = CC.zz
              qqA = FF.zz
              ppAo = CCo.zz
              qqAo = FFo.zz
              ppB = CC.xx
              qqB = FF.xx
              ppBo = CCo.xx
              qqBo = FFo.xx
       elif FFo.yy==0.0 :
           ppA = CC.yy
           qqA = FF.yy
           ppAo = CCo.yy
           qqAo = FFo.yy
           ppB = CC.zz
           qqB = FF.zz
           ppBo = CCo.zz
           qqBo = FFo.zz
       else :
           ppA = CC.zz
           qqA = FF.zz
           ppAo = CCo.zz
           qqAo = FFo.zz
           ppB = CC.yy
           qqB = FF.yy
           ppBo = CCo.yy
           qqBo = FFo.yy

       rrr = qqAo/qqBo
       tt = qqA-rrr*qqB
       if tt==0.0 :
          return retIntersect
       tt = (rrr*(ppB-ppBo)+ppAo-ppA)/tt;
       CC.weight_(CC,1.0,FF,tt)
       rrr = CC.mag();
       if rrr>1.0 :
          return retIntersect
       if rrr==1.0 :
          latLon = CC.getLatLon()
          retIntersect.append(latLon)
          return retIntersect
       rrr = math.sqrt(1-rrr*rrr);
       FF.weight_(CC,1.0,LL,rrr)
       latLon = FF.getLatLon()
       retIntersect.append(latLon)
       FF.weight_(CC,1.0,LL,-rrr)
       latLon = FF.getLatLon()
       retIntersect.append(latLon)
       return retIntersect
