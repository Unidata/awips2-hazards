"""
Description: Simple container class for a latitude and longitude.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""
class LatLonCoord:
    def __init__(self, lat0=1.0e12, lon0=1.0e12):
        self.lat = lat0
        self.lon = lon0

    def same(self, other):
        if self.lat==other.lat and self.lon==other.lon :
            return True
        return False

    def _LatLonCoord(self, other):
        self.lat = other.lat
        self.lon = other.lon

