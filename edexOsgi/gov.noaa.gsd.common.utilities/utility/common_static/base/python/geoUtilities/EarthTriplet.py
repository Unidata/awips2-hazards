"""
Description: 
The class EarthTriplet is a medium weight class used to represent a
point on the earth's surface as a unit vector on a shperical earth.
EarthTriplet objects are heavily used as a computation tool in the
TrackCoordConv class, and in the PointTrack class.
 
SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Placed in code set.
Jul 30, 2013            James.E.Ramer       Updated the inline documentation.

@author James.E.Ramer@noaa.gov
@version 1.0
"""

import math
from LatLonCoord import *
from HazardConstants import *

# This is one of a set of helper classes for doing feature tracking.
# The other classes are LatLonCoord, Motion, TrackCoord, TrackCoordConv, and
# PointTrack.  In these classes, methods with a leading underscore are meant
# to be private.  Methods that end with an underscore can modify the contents
# of the class; methods with no trailing underscore do not change the contents
# of the class.

class EarthTriplet:

    def __init__(self, xx0=0.0, yy0=0.0, zz0=0.0):
        self.xx = xx0
        self.yy = yy0
        self.zz = zz0

    # Test for equivalence with another EarthTriplet object
    def same(self, other):
        if self.xx==other.xx and self.yy==other.yy and  self.zz==other.zz :
            return True
        return False

    # Make this object a deep copy of another EarthTriplet object
    def EarthTriplet_(self, other) :
        self.xx = other.xx
        self.yy = other.yy
        self.zz = other.zz

    # Reinitialize this object on behalf of LatLonCoord
    def latLonInit_(self, latLon) :
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

    # Return the vector magnitude of this object.
    def mag(self) :
        mag = self.xx*self.xx + self.yy*self.yy + self.zz*self.zz
        return math.sqrt(mag)

    # Force this object to have a vector magnitude of exactly one.
    # Returns a boolean for whether this was possible.
    def unit_(self) :
        mag = self.xx*self.xx + self.yy*self.yy + self.zz*self.zz
        if mag==0.0 :
            return False
        mag = math.sqrt(mag)
        self.xx /= mag;
        self.yy /= mag;
        self.zz /= mag;
        return True

    # Return the LatLonCoord represented by this object.
    def getLatLon(self) :
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

    # Return the dot product of this with another EarthTriplet object
    def dot(self, other) :
        return self.xx*other.xx + self.yy*other.yy + self.zz*other.zz

    # Return the dot product of this with another EarthTriplet object
    def dotDiffs(self, et1, et2) :
        return (et1.xx-self.xx)*(et2.xx-et1.xx) + \
               (et1.yy-self.yy)*(et2.yy-et1.yy) + \
               (et1.zz-self.zz)*(et2.zz-et1.zz)

    # Add another EarthTriplet object to this one
    def add_(self, et1, et2) :
        self.xx = et1.xx+et2.xx
        self.yy = et1.yy+et2.yy
        self.zz = et1.zz+et2.zz

    # Subtract another EarthTriplet object from this one
    def diff_(self, et1, et2) :
        self.xx = et1.xx-et2.xx
        self.yy = et1.yy-et2.yy
        self.zz = et1.zz-et2.zz

    # Multiply this EarthTriplet object by a scalar.
    def mult_(self, et1, scalar) :
        self.xx = et1.xx*scalar
        self.yy = et1.yy*scalar
        self.zz = et1.zz*scalar

    # Reinitialize this object as the weighted average of two other
    # EarthTriplet objects
    def weight_(self, et1, w1, et2, w2) :
        self.xx = et1.xx*w1 + et2.xx*w2
        self.yy = et1.yy*w1 + et2.yy*w2
        self.zz = et1.zz*w1 + et2.zz*w2

    # Reinitialize this object as the cross product of two other
    # EarthTriplet objects
    def cross_(self, et1, et2) :
        self.xx = et1.yy*et2.zz-et2.yy*et1.zz
        self.yy = et1.zz*et2.xx-et2.zz*et1.xx
        self.zz = et1.xx*et2.yy-et2.xx*et1.yy

