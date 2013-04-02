import math
from LatLonCoord import LatLonCoord
from TrackCoord import *
from EarthTriplet import EarthTriplet
from HazardConstants import *

"""
Description: All the math that converts a point along a track in track relative
# coordinates into earth coordinates and vice-versa is embedded in the
# class TrackCoordConv.  TrackCoordConv knows nothing about speed of
# motion or times, it just knows where the center line of the track is
# and how to do coordinate conversions between earth and track relative
# coordinates.  TrackCoordConv constructors that take time arguments only
# use them for determining order along the track for the input points.
#
# Any method begining with an underscore can modify the object.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""
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

    def same(self, other):
        if self.__origin.same(other.__origin) and \
           self.__bearing==other.__bearing and self.__radius==other.__radius :
           return True
        return False

    def _TrackCoordConv(self, other):
        self.__ii._EarthTriplet(other.__ii)
        self.__jj._EarthTriplet(other.__jj)
        self.__kk._EarthTriplet(other.__kk)
        self.__dy = other.__dy
        self.__xUnit = other.__xUnit
        self.__yUnit = other.__yUnit
        self.__origin._LatLonCoord(other.__origin)
        self.__bearing = other.__bearing
        self.__radius = other.__radius

    def __initTCC(self, origin, bearing, radius) :
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
        self.__origin._LatLonCoord(origin)
        self.__bearing = bearing;
        self.__radius = radius;
        self.__ii._ET_latLon(origin)
        self.__jj = EarthTriplet(-self.__ii.yy, self.__ii.xx, 0)
        if not self.__jj._ET_unit() :
           ang = DEG_TO_RAD*(origin.lon-self.__ii.zz*bearing)
           self.__jj = EarthTriplet(-math.cos(ang), -math.sin(ang), 0)
           self.__bearing = 90.0
        self.__kk._ET_cross(self.__ii, self.__jj)
        if bearing!= 90.0 :
           bearing = bearing*DEG_TO_RAD;
           self.__jj._ET_weight(self.__jj, math.sin(bearing), \
                                self.__kk, math.cos(bearing) )
           self.__kk._ET_cross(self.__ii, self.__jj)
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
        self.__ii._ET_weight(self.__ii, cosRot, self.__kk, sinRot)
        self.__kk._ET_cross(self.__ii, self.__jj)
        self.__dy = math.asin(sinRot)*self.__yUnit

    def _TCC_latLon_bearing(self, latLon, bearing) :
        self.__initTCC(latLon, bearing, 0)

    def _TCC_latLon_bearing_radius(self, latLon, bearing, radius) :
        self.__initTCC(latLon, bearing, radius)

    def __initTCC2(self, beg, to) :
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
        self.__origin._LatLonCoord(to)
        et0 = EarthTriplet()
        et0._ET_latLon(beg)
        self.__ii._ET_latLon(to)
        self.__kk._ET_cross(et0, self.__ii)
        if not self.__kk._ET_unit() :
           return
        self.__jj._ET_cross(self.__kk, self.__ii)
        self.__xUnit = R_EARTH
        self.__yUnit = R_EARTH
        etE = EarthTriplet(-self.__ii.yy, self.__ii.xx, 0)
        if etE._ET_unit() :
           et0._ET_cross(self.__ii, etE)
           self.__bearing = math.atan2(self.__jj.ET_dot(etE), \
                                       self.__jj.ET_dot(et0) ) / DEG_TO_RAD
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

    def _TCC_two_latLon(self, beg, to) :
        self.__initTCC2(beg, to)

    def _TCC_two_latLon_time(self, latLon1, time1, latLon2, time2) :
        if time1<time2 :
           self.__initTCC2(latLon1, latLon2)
        else :
           self.__initTCC2(latLon2, latLon1)

    def __initTCC3(self, beg, next, to) :
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
        self.__origin._LatLonCoord(to)
        mag = 0.0
        et0 = EarthTriplet()
        et0._ET_latLon(beg)
        et1 = EarthTriplet()
        et1._ET_latLon(next)
        self.__ii._ET_latLon(to)
        ddA = EarthTriplet()
        ddA._ET_diff(et1, et0)
        ddB = EarthTriplet()
        ddB._ET_diff(self.__ii, et1)
        if ddA.ET_dot(ddB)<0 :
           return
        self.__kk._ET_cross(ddA, ddB)
        if not self.__kk._ET_unit() :
           self.__kk.cross(et0, self.__ii)
           if not self.__kk._ET_unit() :
              return
        else :
           mag = self.__ii.ET_dot(self.__kk)
        if mag<=-1.0 or mag>=1.0 :
           return
        self.__jj._ET_cross(self.__kk, self.__ii)
        if not self.__jj._ET_unit() :
           return
        self.__xUnit = R_EARTH
        self.__yUnit = R_EARTH
        etE = EarthTriplet(-self.__ii.yy, self.__ii.xx, 0)
        if not etE._ET_unit() :
           ang = to.lon*DEG_TO_RAD
           etE._ET_latLon(-math.sin(ang), math.cos(ang), 0)
        et0._ET_cross(self.__jj, etE)
        self.__bearing = \
          math.atan2(self.__jj.ET_dot(etE),self.__jj.ET_dot(et0))/DEG_TO_RAD
        if self.__bearing>360 :
           self.__bearing -= 360;
        elif self.__bearing<0 :
            self.__bearing += 360;
        if mag==0.0 :
           return;
        self.__ii._ET_cross(self.__jj, self.__kk)
        self.__xUnit = self.__xUnit*math.sqrt(1-mag*mag)
        self.__dy = -math.asin(mag)*self.__yUnit
        self.__radius = self.__xUnit/mag

    def _TCC_three_latLon(self, beg, next, to) :
        self.__initTCC3(beg, next, to)

    def _TCC_three_latLon_time(self, \
          latLon1, time1, latLon2, time2, latLon3, time3) :
        if time1<=time2 :
            if time2<=time3 :
                self.__initTCC3(latLon1, latLon2, latLon3)
            elif time1<=time3 :
                self.__initTCC3(latLon1, latLon3, latLon2)
            else :
                self.__initTCC3(latLon3, latLon1, latLon2)
        else :
            if time2>time3 :
                self.__initTCC3(latLon3, latLon2, latLon1)
            elif time1>time3 :
                self.__initTCC3(latLon2, latLon3, latLon1)
            else :
                self.__initTCC3(latLon2, latLon1, latLon3)

    def TCC_fwdLeftKmOf(self, latLon) :
        if self.__xUnit==0.0 :
           return TrackCoord(1e37,1e37)
        if latLon.lat<-90.0 or latLon.lat>90.0 or \
           latLon.lon<-180.0 or latLon.lon>180.0 :
           return TrackCoord(1e37,1e37)
        et = EarthTriplet()
        et._ET_latLon(latLon)
        etx = EarthTriplet(self.__ii.ET_dot(et), self.__jj.ET_dot(et), \
                           self.__kk.ET_dot(et) )
        if etx.xx!=0.0 or etx.yy!=0.0 :
            eqrad = math.sqrt(etx.xx*etx.xx+etx.yy*etx.yy)
            return TrackCoord(math.atan2(etx.yy,etx.xx)*self.__xUnit, \
                              math.atan2(etx.zz,eqrad)*self.__yUnit+self.__dy)
        elif etx.zz>0 :
            return TrackCoord(0, self.__dy+self.__yUnit*PI)
        else :
            return TrackCoord(0, self.__dy-self.__yUnit*PI)

    def TCC_pivot(self) :
        if self.__dy==0.0 :
           return LatLonCoord()
        latLon = self.__kk.ET_getLatLon()
        if self.__dy<0.0 :
           return latLon
        latLon.lat = -latLon.lat
        latLon.lon = -latLon.lon
        return latLon

    def TCC_getOrigin(self) :
        return self.__origin

    def TCC_getBearing(self) :
        return self.__bearing

    def TCC_getRadius(self) :
        return self.__radius

    def TCC_getXmin(self) :
        return -self.__xUnit*PI

    def TCC_getXmax(self) :
        return self.__xUnit*PI

    def TCC_getYmin(self) :
        return self.__dy-self.__yUnit*PI

    def TCC_getYmax(self) :
        return self.__dy+self.__yUnit*PI

    def TCC_ok(self) :
        return self.__xUnit!=0

    def TCC_latLonOf(self, fwdLeftKm) :
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
        return et.ET_getLatLon()

    def TCC_latLonFrom(self, fwdKm, leftKm) :
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
        return et.ET_getLatLon()

    def TCC_bearingOf(self, fwdKm) :
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
        etx._ET_cross(self.__kk, et)
        if not etx._ET_unit() :
           return -1.0
        etE = EarthTriplet(-et.yy, et.xx, 0)
        b = 0.0
        if not etE._ET_unit() :
           etE = EarthTriplet(0, 1, 0)
        etN = EarthTriplet()
        etN._ET_cross(et, etE)
        b = math.atan2( etx.ET_dot(etE), etx.ET_dot(etN) )/DEG_TO_RAD
        if b>360.0 :
           return b-360.0
        if b<0.0 :
           return b+360.0
        return b

    def _TCC_advanceOrig(self, fwdKm) :
       if self.__xUnit==0.0 :
          return
       ang = fwdKm/self.__xUnit
       self.__jj._ET_weight(self.__ii, -math.sin(ang), \
                            self.__jj, math.cos(ang))
       self.__ii._ET_cross(self.__jj, self.__kk)
       et = EarthTriplet()
       if self.__dy==0.0 :
          et._EarthTriplet(self.__ii)
       else :
          ang = -self.__dy/self.__yUnit
          et._ET_weight(self.__ii, math.cos(ang), self.__kk, math.sin(ang))
       self.__origin = et.ET_getLatLon()
       etx = EarthTriplet()
       etx._ET_cross(self.__kk, et)
       if not etx._ET_unit() :
          return
       etE = EarthTriplet(-et.yy, et.xx, 0)
       if not etE._ET_unit() :
          etE = EarthTriplet(0, 1, 0)
       etN = EarthTriplet()
       etN._ET_cross(et, etE)
       self.__bearing = \
           math.atan2( etx.ET_dot(etE), etx.ET_dot(etN) )/DEG_TO_RAD
       if self.__bearing>360.0 :
          self.__bearing = self.__bearing-360.0
       elif self.__bearing<0.0 :
          self.__bearing = self.__bearing+360.0

    def TCC_intersect(self, other) :
       retIntersect = []
       if self.__xUnit==0 or other.__xUnit==0 :
          return retIntersect
       LL = EarthTriplet()
       LL._ET_cross(self.__kk, other.__kk)
       if not LL._ET_unit() :
          return retIntersect
       if self.__dy==0 and other.__dy==0 :
          latLon = LL.ET_getLatLon()
          retIntersect.append(latLon)
          retIntersect.append(LatLonCoord(-latLon.lat,-latLon.lon))
          return retIntersect
       CC = EarthTriplet()
       CC._ET_mult(self.__kk, -math.sin(self.__dy/self.__yUnit))
       CCo = EarthTriplet()
       CCo._ET_mult(other.__kk, -math.sin(other.__dy/other.__yUnit))
       FF = EarthTriplet()
       FF._ET_cross(LL, self.__kk)
       FFo = EarthTriplet()
       FFo._ET_cross(LL, other.__kk)
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
       CC._ET_weight(CC,1.0,FF,tt)
       rrr = CC.ET_mag();
       if rrr>1.0 :
          return retIntersect
       if rrr==1.0 :
          latLon = CC.ET_getLatLon()
          retIntersect.append(latLon)
          return retIntersect
       rrr = math.sqrt(1-rrr*rrr);
       FF._ET_weight(CC,1.0,LL,rrr)
       latLon = FF.ET_getLatLon()
       retIntersect.append(latLon)
       FF._ET_weight(CC,1.0,LL,-rrr)
       latLon = FF.ET_getLatLon()
       retIntersect.append(latLon)
       return retIntersect
