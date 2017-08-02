"""
This class accesses the maps database to retrieve geospatial data and associated
metadata. For example, dam break inundation polygons and associated 'damInfo'
metadata are stored in mapdata.daminfo table. This module will retrieve these
data to be used in recommenders and product generation.

 @since: February 2015
 @author: GSD Hazard Services Team

 History:
 Date         Ticket#    Engineer       Description
 ------------ -------    -----------    --------------------------
 Feb 03, 2015            kmanross       Initial development
 Aug 03, 2015   9920     Robert.Blum    Fixed duplicate alias sql error.
 Aug 14, 2015   9920     Robert.Blum    Additional changes to stay in sync with ufcore.
                                        Parameters are no longer required on mapdata requests.
 Sep 10, 2015  11510     Robert.Blum    Fixed query bug caused by recent merge.
 May 18, 2016  17342     Ben.Phillippe  Added location constraint to polygon query
 8 Jun 16      15523     K. Manross/
                         J. Wakefield   Added BurnScar accessors
 Nov 08, 2016  25643  David.Gillingham  Code cleanup in getPolygonNames and 
                                        getPolygonDict
 Nov 16, 2016  22971     Robert.Blum    Changes for incremental overrides.
"""

from shapely.geometry import Polygon
from ufpy.dataaccess import DataAccessLayer
from Bridge import Bridge
    
class MapsDatabaseAccessor(object):
    def __init__(self):
        self.bridge = Bridge()

    def _extract_poly_coords(self, geom):
        if geom.type == 'Polygon':
            exterior_coords = geom.exterior.coords[:]
            interior_coords = []
            for int in geom.interiors:
                interior_coords += int.coords[:]
        elif geom.type == 'MultiPolygon':
            exterior_coords = []
            interior_coords = []
            for part in geom:
                epc = self._extract_poly_coords(part)  # Recursive call
                exterior_coords += epc['exterior_coords']
                interior_coords += epc['interior_coords']
        else:
            raise ValueError('Unhandled geometry type: ' + repr(geom.type))
        return {'exterior_coords': exterior_coords,
                'interior_coords': interior_coords}

    def getPolygonNames(self, tablename, locationField="name", columns=[]):
        req = DataAccessLayer.newDataRequest()
        req.setDatatype("maps")
        table = "mapdata." + tablename
        req.addIdentifier("table", table)
        req.addIdentifier("geomField", "the_geom")
        req.addIdentifier("locationField", locationField)
        locNames = DataAccessLayer.getAvailableLocationNames(req)
        return list(locNames)
    
    def getPolygonDict(self, tablename, siteID, locationField="name", columns=[]):
        req = DataAccessLayer.newDataRequest()
        req.setDatatype("maps")
        table = "mapdata." + tablename
        req.addIdentifier("table", table)
        req.addIdentifier("geomField", "the_geom")
        req.addIdentifier("locationField", locationField)
        req.addIdentifier("cwa", siteID)
        if columns:
            req.setParameters(columns)

        retGeoms = DataAccessLayer.getGeometryData(req)

        retDict = {}
        for retGeom in retGeoms:
            id = retGeom.getLocationName()
            if id:
                poly = self._extract_poly_coords(retGeom.getGeometry())
                formattedPoly = [list(c) for c in poly['exterior_coords']]
                retDict[id] = formattedPoly

        return retDict

    def getDamInundationMetadata(self, damName):
        damInundationMetadata = self.bridge.getDamMetaData()
        return damInundationMetadata.get(damName)

    def getAllDamInundationMetadata(self):
        return self.bridge.getDamMetaData()

    def getBurnScarMetadata(self, burnscarName):
        burnScarAreaMetadata = self.bridge.getBurnScarMetaData()
        return burnScarAreaMetadata.get(burnscarName)

    def getAllBurnScarMetadata(self):
        return self.bridge.getBurnScarMetaData()
