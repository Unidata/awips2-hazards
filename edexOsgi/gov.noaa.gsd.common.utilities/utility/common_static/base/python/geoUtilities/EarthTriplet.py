"""
Description: The class EarthTriplet is a simple container class used to represent a
point on the earth's surface as a unit vector on a spherical earth.
EarthTriplet objects are heavily used as a computation tool in the
TrackCoordConv class, and in the several static member functions 
associated with the PointTrack class.

Any method beginning with an underscore can modify the object.
 
SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
MMM DD, YYYY            Tracy.L.Hansen      Initial creation
  
@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""

import math
from LatLonCoord import *
from HazardConstants import *

class EarthTriplet:

    def __init__(self, xx0=0.0, yy0=0.0, zz0=0.0):
        self.xx = xx0
        self.yy = yy0
        self.zz = zz0

    def same(self, other):
        if self.xx==other.xx and self.yy==other.yy and  self.zz==other.zz :
            return True
        return False

    def _EarthTriplet(self, other) :
        self.xx = other.xx
        self.yy = other.yy
        self.zz = other.zz

    def _ET_latLon(self, latLon) :
        if  latLon.lat==90.0 or latLon.lat==-90.0 :
            self.xx = 0.0
            self.yy = 0.0
            if latLon.lat > 0 :
                self.zz = 1.0
            else :
                self.zz = -1.0
            return
        latrad = DEG_TO_RAD*latLon.lat
        lonrad = DEG_TO_RAD*latLon.lon
        coslat = math.cos(latrad)
        self.xx = math.cos(lonrad)*coslat
        self.yy = math.sin(lonrad)*coslat
        self.zz = math.sin(latrad)

    def ET_mag(self) :
        mag = self.xx*self.xx + self.yy*self.yy + self.zz*self.zz
        return math.sqrt(mag)

    def _ET_unit(self) :
        mag = self.xx*self.xx + self.yy*self.yy + self.zz*self.zz
        if mag==0.0 :
            return False
        mag = math.sqrt(mag)
        self.xx /= mag;
        self.yy /= mag;
        self.zz /= mag;
        return True

    def ET_getLatLon(self) :
        retLatLon = LatLonCoord()
        if self.xx==0.0 and self.yy==0.0 :
            if self.zz==0 :
                return retLatLon
            retLatLon .lon = 0.0
            if self.zz>0 :
                retLatLon.lat = 90
            else :
                retLatLon.lat = -90
            return retLatLon
        retLatLon.lon = math.atan2(self.yy,self.xx)/DEG_TO_RAD
        retLatLon.lat = math.atan2(self.zz, \
                         math.sqrt(self.xx*self.xx+self.yy*self.yy))/DEG_TO_RAD
        return retLatLon

    def ET_dot(self, other) :
        return self.xx*other.xx + self.yy*other.yy + self.zz*other.zz

    def ET_dotDiffs(self, et1, et2) :
        return (et1.xx-self.xx)*(et2.xx-et1.xx) + \
               (et1.yy-self.yy)*(et2.yy-et1.yy) + \
               (et1.zz-self.zz)*(et2.zz-et1.zz)

    def _ET_add(self, et1, et2) :
        self.xx = et1.xx+et2.xx
        self.yy = et1.yy+et2.yy
        self.zz = et1.zz+et2.zz

    def _ET_diff(self, et1, et2) :
        self.xx = et1.xx-et2.xx
        self.yy = et1.yy-et2.yy
        self.zz = et1.zz-et2.zz

    def _ET_mult(self, et1, scalar) :
        self.xx = et1.xx*scalar
        self.yy = et1.yy*scalar
        self.zz = et1.zz*scalar

    def _ET_weight(self, et1, w1, et2, w2) :
        self.xx = et1.xx*w1 + et2.xx*w2
        self.yy = et1.yy*w1 + et2.yy*w2
        self.zz = et1.zz*w1 + et2.zz*w2

    def _ET_cross(self, et1, et2) :
        self.xx = et1.yy*et2.zz-et2.yy*et1.zz
        self.yy = et1.zz*et2.xx-et2.zz*et1.xx
        self.zz = et1.xx*et2.yy-et2.xx*et1.yy



