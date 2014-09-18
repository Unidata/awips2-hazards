import math
from LatLonCoord import LatLonCoord
from EarthTriplet import EarthTriplet
from HazardConstants import *

"""
Description: 

Simplification of the math in TrackCoordConv to allow one to
compute the distance and bearing between two LatLonCoord objects.
Distances are in kilometers, and bearings are toward a meteorological
compass bearing in degrees.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Aug 13, 2014            James.E.Ramer       Original version.

@author James.E.Ramer@noaa.gov
@version 1.0
"""

class DistanceBearing:

    # origin needs to be a LatLonCoord
    def __init__(self, origin):
        if origin.lat<-90.0 or origin.lat>90.0 or \
           origin.lon<-180.0 or origin.lon>180.0 :
           self.__origin = LatLonCoord()
           return
        self.__origin = LatLonCoord(origin.lat, origin.lon)
        self.__ii = EarthTriplet()
        self.__ii.latLonInit_(origin)
        self.__jj = EarthTriplet()
        self.__jj = EarthTriplet(-self.__ii.yy, self.__ii.xx, 0)
        if not self.__jj.unit_() :
           ang = DEG_TO_RAD*origin.lon
           self.__jj = EarthTriplet(-math.cos(ang), -math.sin(ang), 0)
        self.__kk = EarthTriplet()
        self.__kk.cross_(self.__ii, self.__jj)

    # Test for equivalence with another DistanceBearing object
    def same(self, other):
        if self.__origin.same(other.__origin) :
           return True
        return False

    # Make this object a deep copy of another DistanceBearing object
    def DistanceBearing_(self, other):
        self.__ii.EarthTriplet_(other.__ii)
        self.__jj.EarthTriplet_(other.__jj)
        self.__kk.EarthTriplet_(other.__kk)
        self.__origin.LatLonCoord_(other.__origin)

    # Returns tuple of distance bearing to specified LatLonCoord
    def getDistanceBearing(self, latLon):
        if self.__origin.lat>1e11 :
            return ( -1, -1 )
        if latLon.lat<-90.0 or latLon.lat>90.0 or \
           latLon.lon<-180.0 or latLon.lon>180.0 :
            return ( -1, -1 )
        if self.__origin.same(latLon) :
            return ( 0, 0 )
        et = EarthTriplet()
        et.latLonInit_(latLon)
        idot = self.__ii.dot(et)
        jdot = self.__jj.dot(et)
        kdot = self.__kk.dot(et)
        if jdot==0 and kdot==0 :
            if idot>0 :
                return ( 0, 0 )
            return ( 2*PI*R_EARTH, 0 )
        dist = math.atan2(math.sqrt(jdot*jdot+kdot*kdot),idot) * R_EARTH
        bearing = math.atan2(jdot, kdot) / DEG_TO_RAD
        if bearing<0 :
            bearing += 360
        return ( dist, bearing )

    # Returns LatLonCoord for specified distance and bearing
    def getLatLon(self, dist, bearing ) :
        if self.__origin.lat>1e11 or dist==0 :
            return self.__origin
        if dist>22222 or dist<-22222 or bearing<-720 or bearing>720 :
            return LatLonCoord()
        bearing = bearing*DEG_TO_RAD
        et = EarthTriplet()
        et.weight_(self.__jj, math.sin(bearing), self.__kk, math.cos(bearing))
        ang = dist/R_EARTH
        etx = EarthTriplet()
        etx.weight_(self.__ii, math.cos(ang), et, math.sin(ang))
        return etx.getLatLon()
