"""
Description: Simple container class for a speed and direction of motion.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""

class Motion:
    def __init__(self, speed0=-1.0, bearing0=-1.0):
        self.speed = speed0
        self.bearing = bearing0

    def same(self, other):
        if self.speed==other.speed and self.bearing==other.bearing :
            return True
        return False

    def _Motion(self, other):
        self.speed = other.speed
        self.bearing = other.bearing
