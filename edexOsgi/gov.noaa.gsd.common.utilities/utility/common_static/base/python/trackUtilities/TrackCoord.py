"""
Description: Simple container class for a track relative position.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""

class TrackCoord:

    def __init__(self, fwd0=0.0,left0=0.0):
        self.fwd = fwd0
        self.left = left0

    def same(self, other):
        if self.fwd==other.fwd and self.left==other.left :
            return True
        return False

    def _TrackCoord(self, other):
        self.fwd = other.fwd
        self.left = other.left

    def _TC_minus(self, other):
        self.fwd = self.fwd-other.fwd
        self.left = self.left-other.left

    def _TC_plus(self, other):
        self.fwd = self.fwd+other.fwd
        self.left = self.left+other.left
