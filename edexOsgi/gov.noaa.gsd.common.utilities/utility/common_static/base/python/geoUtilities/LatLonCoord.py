"""
Description: Simple container class for a latitude and longitude.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Placed in code set.
Jul 30, 2013            James.E.Ramer       Updated the inline documentation.

@author James.E.Ramer@noaa.gov
@version 1.0
"""

# This is one of a set of helper classes for doing feature tracking.
# The other classes are Motion, TrackCoord, EarthTriplet, TrackCoordConv, and
# PointTrack.  In these classes, methods with a leading underscore are meant
# to be private.  Methods that end with an underscore can modify the contents
# of the class; methods with no trailing underscore do not change the contents
# of the class.

class LatLonCoord:
    def __init__(self, lat0=1.0e12, lon0=1.0e12):
        self.lat = lat0
        self.lon = lon0

    # Test for equivalence with another LatLonCoord object
    def same(self, other):
        if self.lat==other.lat and self.lon==other.lon :
            return True
        return False

    # Make this object a deep copy of another LatLonCoord object
    def LatLonCoord_(self, other):
        self.lat = other.lat
        self.lon = other.lon

