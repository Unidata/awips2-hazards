"""
Description: Simple container class for a speed and direction of motion.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Placed in code set.
Jul 30, 2013            James.E.Ramer       Updated the inline documentation.

@author James.E.Ramer@noaa.gov
@version 1.0
"""

# This is one of a set of helper classes for doing feature tracking.
# The other classes are LatLonCoord, TrackCoord, EarthTriplet, TrackCoordConv,
# and PointTrack.  In these classes, methods with a leading underscore are 
# meant to be private.  Methods that end with an underscore can modify the
# contents of the class; methods with no trailing underscore do not change
# the contents of the class.

class Motion:
    def __init__(self, speed0=-1.0, bearing0=-1.0):
        self.speed = speed0
        self.bearing = bearing0

    # Test for equivalence with another Motion object
    def same(self, other):
        if self.speed==other.speed and self.bearing==other.bearing :
            return True
        return False

    # Make this object a deep copy of another Motion object
    def Motion_(self, other):
        self.speed = other.speed
        self.bearing = other.bearing
