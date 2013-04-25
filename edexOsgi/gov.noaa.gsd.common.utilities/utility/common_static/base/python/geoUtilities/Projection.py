#!/usr/bin/env python
from math import *
#from Numeric import *
import numpy
# Some constants
RAD_TO_DEG = 360.0 / 2.0 / pi
DEG_TO_RAD = 1 / RAD_TO_DEG

"""
Description: Provides classes and methods for creating and
 manipulating projections.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""
# A dictionary of known projections for convenience only
knownProjections = {"Grid201": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-150.00, -20.826),
                                "latLonUR" : (-20.90846, 30.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (65, 65),
                                "lonOrigin" : -105.0,
                                },
                    "Grid202": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-141.028, 7.838),
                                "latLonUR" : (-18.576, 35.617),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (65, 43),
                                "lonOrigin" : -105.0,
                                "poleCoord" : (33, 45),
                                },
                    "Grid203": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-185.837, 19.132),
                                "latLonUR" : (-53.660, 57.634),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (45, 39),
                                "lonOrigin" : -150.0,
                                "poleCoord" : (27, 37),
                                },
                    "Grid204": {"name" : "MERCATOR",
                                "latLonLL" : (-250.0, -25.0),
                                "latLonUR" : (-109.129, 60.644),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (93, 68),
                                "lonCenter" : -179.564,
                                },
                    "Grid205": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-84.904, 0.616),
                                "latLonUR" : (-15.000, 45.620),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (45, 39),
                                "lonOrigin" : -60.0,
                                "poleCoord" : (27, 57),
                                },
                    "Grid206": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-117.991, 22.289),
                                "latLonUR" : (-73.182, 51.072),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (51, 41),
                                },
                    "Grid207": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-175.641, 42.085),
                                "latLonUR" : (-93.689, 63.976),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (49, 35),
                                "lonOrigin" : -150.0,
                                "poleCoord" : (25, 51),
                                },
                    "Grid208": {"name" : "MERCATOR",
                                "latLonLL" : (-166.219, 10.656),
                                "latLonUR" : (-147.844, 27.917),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (25, 25),
                                "lonCenter" : -157.082
                                },
                    "Grid209": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-117.991, 22.289),
                                "latLonUR" : (-73.182, 51.072),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (101, 81),
                                },
                    "Grid210": {"name" : "MERCATOR",
                                "latLonLL" : (-77.000, 9.000),
                                "latLonUR" : (-58.625, 26.422),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (25, 25),
                                "lonCenter" : -67.812,
                                },
                    "Grid211": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-133.459, 12.190),
                                "latLonUR" : (-49.385, 57.290),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (93, 65),
                                "poleCoord" : (53.000, 179.362),
                                },
                    "Grid212": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-133.459, 12.190),
                                "latLonUR" : (-49.385, 57.290),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (185, 129),
                                "poleCoord" : (105.000, 357.723),
                                },
                    "Grid213": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-141.028, 7.838),
                                "latLonUR" : (-18.577, 35.617),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (129, 85),
                                "lonOrigin" : -105.0,
                                "poleCoord" : (65, 89),
                                },
                    "Grid214": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-175.641, 42.085),
                                "latLonUR" : (-93.689, 63.975),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (97, 69),
                                "lonOrigin" : -150.0,
                                "poleCoord" : (49, 101),
                                },
                    "Grid214AK": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-178.571, 40.5301),
                                "latLonUR" : (-93.689, 63.975),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (104, 70),
                                "lonOrigin" : -150.0,
                                "poleCoord" : (49, 101),
                                },
                    "Grid215": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-133.459, 12.190),
                                "latLonUR" : (-49.385, 57.290),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (369, 257),
                                "poleCoord" : (209.0, 714.446),
                                },
                    "Grid216": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-173.000, 30.000),
                                "latLonUR" : (-62.850, 70.111),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (139, 107),
                                "lonOrigin" : -135.0,
                                "poleCoord" : (94.909, 121.198),
                                },
                    "Grid217": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-175.641, 42.085),
                                "latLonUR" : (-93.689, 63.975),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (289, 205),
                                "lonOrigin" : -150.0,
                                "poleCoord" : (145, 301),
                                },
                    "Grid218": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-133.459, 12.190),
                                "latLonUR" : (-49.385, 57.290),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (614, 428),
                                "poleCoord" : (417.002, 1427.8923),
                                },
                    "Grid219": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-119.559, 25.008),
                                "latLonUR" : (60.339, 24.028),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (385, 465),
                                "lonOrigin" : -80.0,
                                "poleCoord" : (190.988, 231.000),
                                },
                    "Grid221": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-145.500, 1.000),
                                "latLonUR" : (-2.566, 46.352),
                                "latLonOrigin" : (-107.0, 50.0),
                                "stdParallel_1" : 50.0,
                                "stdParallel_2" : 50.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (349, 277),
                                "poleCoord" : (174.507, 307.764),
                                },
                    "Grid222": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-145.500, 1.000),
                                "latLonUR" : (-2.566, 46.352),
                                "latLonOrigin" : (-107.0, 50.0),
                                "stdParallel_1" : 50.0,
                                "stdParallel_2" : 50.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (59, 47),
                                "poleCoord" : (29.918, 52.127),
                                },
                    "Grid225": {"name" : "MERCATOR",
                                "latLonLL" : (-250.0, -25.0),
                                "latLonUR" : (-109.129, 60.644),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (185, 135),
                                "lonCenter" : -179.564,
                                },
                    "Grid226": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-133.459, 12.190),
                                "latLonUR" : (-49.385, 57.290),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (737, 513),
                                "poleCoord" : (579, 1422.960),
                                },
                    "Grid227": {"name" : "LAMBERT_CONFORMAL",
                                "latLonLL" : (-133.459, 12.190),
                                "latLonUR" : (-49.385, 57.290),
                                "latLonOrigin" : (-95.0, 25.0),
                                "stdParallel_1" : 25.0,
                                "stdParallel_2" : 25.0,
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (1473, 1025),
                                "poleCoord" : (1157.0, 2844.92),
                                },
                    "Grid228": {"name" : "LATLON",
                                "latLonLL" : (0.0, 90.0),
                                "latLonUR" : (359.0, -90.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (144, 73),
                                },
                    "Grid229": {"name" : "LATLON",
                                "latLonLL" : (0.0, 90.0),
                                "latLonUR" : (359.0, -90.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (360, 181),
                                },
                    "Grid230": {"name" : "LATLON",
                                "latLonLL" : (0.0, 90.0),
                                "latLonUR" : (359.5, -90.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (720, 361),
                                },
                    "Grid231": {"name" : "LATLON",
                                "latLonLL" : (0.0, 0.0),
                                "latLonUR" : (359.5, 90.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (720, 181),
                                },
                    "Grid232": {"name" : "LATLON",
                                "latLonLL" : (0.0, 0.0),
                                "latLonUR" : (359.5, 90.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (360, 91),
                                },
                    "Grid233": {"name" : "LATLON",
                                "latLonLL" : (0.0, -78.0),
                                "latLonUR" : (358.750, 78.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (288, 157),
                                },
                    "Grid234": {"name" : "LATLON",
                                "latLonLL" : (-98.000, 15.0),
                                "latLonUR" : (-65.000, -45.0),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (133, 121),
                                },
                    "Grid234": {"name" : "LATLON",
                                "latLonLL" : (0.250, 89.750),
                                "latLonUR" : (359.750, -89.750),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (720, 360),
                                },
                    "HRAP": {"name" : "POLAR_STEREOGRAPHIC",
                                "latLonLL" : (-119.036, 23.097),
                                "latLonUR" : (-75.945396, 53.480095),
                                "gridPointLL" : (1, 1),
                                "gridPointUR" : (801, 881),
                                "lonOrigin" : -105.0,
                                }
                    }

class Projection:
    # returns a latitude and a longitude grid of values that represents the
    # center of each grid cell as defined by the origin, extent and gridSize
    def latLonGrid(self, origin, extent, gridSize):
        # Each input parameter is a tuple (x, y)
        latGrid = numpy.zeros((gridSize[1], gridSize[0]), float)
        lonGrid = numpy.zeros((gridSize[1], gridSize[0]), float)
        for x in range(0, gridSize[0]):
            for y in range(0, gridSize[1]):
                awipsX = (x * extent[0] / (gridSize[0] - 1)) + origin[0]
                awipsY = (y * extent[1] / (gridSize[1] - 1)) + origin[1]
                lat, lon = self.AWIPSToLatLon(awipsX, awipsY)
                latGrid[y, x] = lat
                lonGrid[y, x] = lon

        return latGrid, lonGrid

    def latLonToGrid(self, origin, extent, gridSize, lat, lon):
        # Each input parameter is a tuple (x, y)
        awips = self.latLonToAWIPS(lat, lon)
        x = int(((awips[0] - origin[0]) / extent[0] * (gridSize[0] - 1)) + 0.5)
        y = int(((awips[1] - origin[1]) / extent[1] * (gridSize[1] - 1)) + 0.5)

        return x, y

#  Lambert Conformal projection class
class Lambert (Projection):
    def __init__(self, latLonLL, latLonUR, latLonOrigin, stdParallel_1,
                 stdParallel_2, gridPointLL, gridPointUR, **kw):
        # Save some data
        self._latLonLL = latLonLL
        self._latLonUR = latLonUR
        self._latLonOrigin = latLonOrigin
        self._stdParallel_1 = stdParallel_1
        self._stdParallel_2 = stdParallel_2
        self._gridPointLL = gridPointLL
        self._gridPointUR = gridPointUR
        #  Make some calculations
        if self._stdParallel_1 != self._stdParallel_2:
            numerator = log(cos(self._stdParallel_1 * DEG_TO_RAD) /
                            cos(self._stdParallel_2 * DEG_TO_RAD))
            denominator = log(tan(pi/4 + self._stdParallel_2 * DEG_TO_RAD/2) \
                              / tan(pi/4 + self._stdParallel_1 * DEG_TO_RAD/2))
            self._n = numerator / denominator
        else:
            self._n = sin(self._stdParallel_1 * DEG_TO_RAD);

        ## F constant
        self._F = cos(self._stdParallel_1 * DEG_TO_RAD) * \
                 pow(tan(pi/4 + self._stdParallel_1 * DEG_TO_RAD/2), self._n) \
                 / self._n

        self._rhoOrigin = self._F / pow(tan(pi/4 + self._latLonOrigin[1] * \
                         DEG_TO_RAD/2), self._n)

        self._minLat = self._latLonLL[1]
        self._maxLat = self._latLonUR[1]
        self._minLon = self._latLonLL[0]
        self._maxLon = self._latLonUR[0]

        self._minX, self._minY = self.latLonToXY(self._latLonLL[1],
                                               self._latLonLL[0])
        self._maxX, self._maxY = self.latLonToXY(self._latLonUR[1],
                                               self._latLonUR[0])

        self._xGridsPerRad = (self._gridPointUR[0] - self._gridPointLL[0]) / \
                             (self._maxX - self._minX)
        self._yGridsPerRad = (self._gridPointUR[1] - self._gridPointLL[1]) / \
                             (self._maxY - self._minY)

    # Accessors
    def getLatLonLL(self):
        return self._latLonLL

    def getLatLonUR(self):
        return self._latLonUR

    def getGridPointLL(self):
        return self._gridPointLL

    def getGridPointUR(self):
        return self._gridPointUR

    def getStdParallel_1(self):
        return self._stdParallel_1

    def getStdParallel_2(self):
        return self._stdParallel_2

    def getLatLonOrigin(self):
        return self._latLonOrigin


    def latLonToXY(self, lat, lon):
        rho = self._F / pow(tan(pi/4 + lat * DEG_TO_RAD/2), self._n)

        ## Calculate polar coordinate for minLat, minLon
        theta = self._n * (lon - self._latLonOrigin[0]);

        ## Compute x, y
        x = rho * sin(theta * DEG_TO_RAD);
        y = self._rhoOrigin - rho * cos(theta * DEG_TO_RAD);

        return (x, y)

    def latLonToAWIPS(self, lat, lon):
        x, y = self.latLonToXY(lat, lon)
        xwc = self._gridPointLL[0] + (x - self._minX) * self._xGridsPerRad
        ywc = self._gridPointLL[1] + (y - self._minY) * self._yGridsPerRad

        return (xwc ,ywc)

    def AWIPSToLatLon(self, xwc, ywc):
        x = ((xwc - self._gridPointLL[0]) / self._xGridsPerRad) + self._minX
        y = ((ywc - self._gridPointLL[1]) / self._yGridsPerRad) + self._minY

        p = sqrt(pow(x, 2.0) + pow((self._rhoOrigin - y), 2))
        theta = numpy.arctan(x / (self._rhoOrigin - y))

        lon = theta / self._n + self._latLonOrigin[0] * DEG_TO_RAD
        lat = 2 * numpy.arctan(pow((self._F / p), 1 / self._n)) - pi / 2

        return ((lat * RAD_TO_DEG), (lon * RAD_TO_DEG))

# Polar Stereographic projection class
class PolarStereo(Projection):
    def __init__(self, latLonLL, latLonUR, gridPointLL, gridPointUR,
                 lonOrigin, **kw):
        # Save the data
        self._latLonLL = latLonLL
        self._latLonUR = latLonUR
        self._gridPointLL = gridPointLL
        self._gridPointUR = gridPointUR
        self._lonOrigin = lonOrigin

        # Make some preliminary calculations
        self._minLat = self._latLonLL[1]
        self._maxLat = self._latLonUR[1]
        self._minLon = self._latLonLL[0]
        self._maxLon = self._latLonUR[0]

        self._minX, self._minY = self.latLonToXY(self._latLonLL[1],
                                               self._latLonLL[0])
        self._maxX, self._maxY = self.latLonToXY(self._latLonUR[1],
                                               self._latLonUR[0])

        self._xGridsPerRad = (self._gridPointUR[0] - self._gridPointLL[0]) / \
                            (self._maxX - self._minX)
        self._yGridsPerRad = (self._gridPointUR[1] - self._gridPointLL[1]) / \
                             (self._maxY - self._minY)

    # Accessors
    def getLatLonLL(self):
        return self._latLonLL

    def getLatLonUR(self):
        return self._latLonUR

    def getGridPointLL(self):
        return self._gridPointLL

    def getGridPointUR(self):
        return self._gridPointUR

    def getLonOrigin(self):
        return self._lonOrigin

    def latLonToXY(self, lat, lon):

        x = 2 * tan(pi/4.0 - lat * DEG_TO_RAD / 2.0) * sin(lon * DEG_TO_RAD - \
          self._lonOrigin * DEG_TO_RAD);
        y = -2 * tan(pi/4.0 - lat * DEG_TO_RAD / 2.0) * cos(lon * DEG_TO_RAD - \
          self._lonOrigin * DEG_TO_RAD);

        return (x, y)

    def latLonToAWIPS(self, lat, lon):

        while lon < self._minLon:
            lon += 360.0
        while lon > self._maxLon:
            lon -= 360.0

        lon = lon * DEG_TO_RAD
        lat = lat * DEG_TO_RAD

        x = 2 * tan(pi / 4 - lat / 2) * sin(lon - self._lonOrigin * DEG_TO_RAD)
        y = -2 * tan(pi / 4 - lat / 2) * cos(lon - self._lonOrigin * DEG_TO_RAD)

        xwc = self._gridPointLL[0] + (x - self._minX) * self._xGridsPerRad
        ywc = self._gridPointLL[1] + (y - self._minY) * self._yGridsPerRad

        return (xwc, ywc)

    def AWIPSToLatLon(self, xwc, ywc):
        x = ((float(xwc) - self._gridPointLL[0]) / self._xGridsPerRad) + self._minX
        y = ((float(ywc) - self._gridPointLL[1]) / self._yGridsPerRad) + self._minY

        lon = (self._lonOrigin * DEG_TO_RAD + numpy.arctan2(x, -y)) * RAD_TO_DEG
        lat = (numpy.arcsin(cos(2 * numpy.arctan2(sqrt(x * x + y * y) , 2)))) * RAD_TO_DEG

        return (lat, lon)

# Mercator projection class
class Mercator(Projection):
    def __init__(self, latLonLL, latLonUR, gridPointLL, gridPointUR, lonCenter, **kw):
        # Save some data
        self._latLonLL = latLonLL
        self._latLonUR = latLonUR
        self._gridPointLL = gridPointLL
        self._gridPointUR = gridPointUR
        self._lonCenter = lonCenter
        # Make some calculations
        self._minLat = self._latLonLL[1]
        self._maxLat = self._latLonUR[1]
        self._minLon = self._latLonLL[0]
        self._maxLon = self._latLonUR[0]

        self._minX, self._minY = self.latLonToXY(self._latLonLL[1],
                                                 self._latLonLL[0])
        self._maxX, self._maxY = self.latLonToXY(self._latLonUR[1],
                                                 self._latLonUR[0])

        self._xGridsPerRad = (self._gridPointUR[0] - self._gridPointLL[0])\
                             / (self._maxX - self._minX)
        self._yGridsPerRad = (self._gridPointUR[1] - self._gridPointLL[1])\
                             / (self._maxY - self._minY)

    # Accessors
    def getLatLonLL(self):
        return self._latLonLL

    def getLatLonUR(self):
        return self._latLonUR

    def getGridPointLL(self):
        return self._gridPointLL

    def getGridPointUR(self):
        return self._gridPointUR

    def getLonCenter(self):
        return self._lonCenter

    def latLonToXY(self, lat, lon):

        x = lon * DEG_TO_RAD - self._lonCenter * DEG_TO_RAD
        y = log(tan(pi / 4 + lat * DEG_TO_RAD / 2))

        return (x, y)

    def latLonToAWIPS(self, lat, lon):
        lon = lon * DEG_TO_RAD
        lat = lat * DEG_TO_RAD

        x = lon - self._lonCenter * DEG_TO_RAD
        y = log(tan(pi / 4 + lat / 2))

        xwc = self._gridPointLL[0] + (x - self._minX) * self._xGridsPerRad
        ywc = self._gridPointLL[1] + (y - self._minY) * self._yGridsPerRad

        return (xwc, ywc)

    def AWIPSToLatLon(self, xwc, ywc):
        x = ((xwc - self._gridPointLL[0]) / self._xGridsPerRad)\
            + self._minX
        y = ((ywc - self._gridPointLL[1]) / self._yGridsPerRad) \
            + self._minY

        lat = 2 * numpy.arctan(exp(y)) - pi / 2
        lon = x + self._lonCenter * DEG_TO_RAD

        return ((lat * RAD_TO_DEG), (lon * RAD_TO_DEG))

# Latitude/Longitude projection class
class LatLon(Projection):
    def __init__(self, latLonLL, latLonUR, gridPointLL, gridPointUR, **kw):
        self._latLonLL = latLonLL
        self._latLonUR = latLonUR
        self._gridPointLL = gridPointLL
        self._gridPointUR = gridPointUR

        if self._latLonUR[0] < self._latLonLL[0]:
            self._maxLon = self._latLonUR[0] + 360.0
        else:
            self._maxLon = self._latLonUR[0]

        self._deltaX = (self._maxLon - self._latLonLL[0]) / \
                      (self._gridPointUR[0] - self._gridPointLL[0])
        self._deltaY = (self._latLonUR[1] - self._latLonLL[1]) / \
                      (self._gridPointUR[1] - self._gridPointLL[1])


    # Accessors
    def getLatLonLL(self):
        return self._latLonLL

    def getLatLonUR(self):
        return self._latLonUR

    def getGridPointLL(self):
        return self._gridPointLL

    def getGridPointUR(self):
        return self._gridPointUR

    def latLonToAWIPS(self, lat, lon):
        while lon < self._latLonLL[0]:
            lon = lon + 360.0
        while lon > self._maxLon:
            lon = lon - 360.0

        ywc = (lat - self._latLonLL[1]) / self._deltaY + \
              self._gridPointLL[1]
        xwc = (lon - self._latLonLL[0]) / self._deltaX + \
              self._gridPointLL[0]

        return (xwc, ywc)

    def AWIPSToLatLon(self, xwc, ywc):
        lat = self._latLonLL[1] + ((ywc - self._gridPointLL[1]) * \
                                   self._deltaY)
        lon = self._latLonLL[0] + ((xwc - self._gridPointLL[0]) * \
                                   self._deltaX)

        while lon > 180.0:
            lon = lon - 360.0
        while lon < -180.0:
            lon = lon + 360.0

        return (lat, lon)

# Main function of the Projection module returns a projection of the
# appropriate type
def makeProj(id, **kw):
    if knownProjections.has_key(id):
        return apply(makeProj, (knownProjections[id]['name'],),
                     knownProjections[id])

    if id == "LAMBERT_CONFORMAL":
        return apply(Lambert, (), kw)
    elif id == "POLAR_STEREOGRAPHIC":
        return apply(PolarStereo, (), kw)
    elif id == "MERCATOR":
        return apply(Mercator, (), kw)
    elif id == "LATLON":
        return apply(LatLon, (), kw)
    raise ValueError("Unknown Projection type: " + id)
