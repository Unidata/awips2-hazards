"""
Description: Simple container class for a track relative position.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Placed in code set.
Jul 30, 2013            James.E.Ramer       Updated the inline documentation.

@author James.E.Ramer@noaa.gov
@version 1.0
"""

# This is one of a set of helper classes for doing feature tracking.
# The other classes are LatLonCoord, Motion, EarthTriplet, TrackCoordConv,
# and PointTrack.  In these classes, methods with a leading underscore are 
# meant to be private.  Methods that end with an underscore can modify the
# contents of the class; methods with no trailing underscore do not change
# the contents of the class.

class TrackCoord:

    def __init__(self, fwd0=0.0,left0=0.0):
        self.fwd = fwd0
        self.left = left0

    # Test for equivalence with another TrackCoord object
    def same(self, other):
        if self.fwd==other.fwd and self.left==other.left :
            return True
        return False

    # Make this object a deep copy of another TrackCoord object
    def TrackCoord_(self, other):
        self.fwd = other.fwd
        self.left = other.left

    # Subtract another TrackCoord object from this one
    def minus_(self, other):
        self.fwd = self.fwd-other.fwd
        self.left = self.left-other.left

    # Add another TrackCoord object to this one
    def plus_(self, other):
        self.fwd = self.fwd+other.fwd
        self.left = self.left+other.left
