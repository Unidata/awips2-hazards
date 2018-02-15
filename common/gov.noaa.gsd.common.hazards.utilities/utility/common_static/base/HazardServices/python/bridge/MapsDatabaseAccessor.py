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
 Feb 07, 2017   7137     JRamer         Handle merging of dam-specific and generic
                                        DamMetadData.
"""

from shapely.geometry import Polygon
from ufpy.dataaccess import DataAccessLayer
from Bridge import Bridge
import traceback
import sys

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

    # We now consolidate the dam specific metadata with the non-dam specific
    # metadata and do any variable substitutions that are possible completely
    # internally.
    def getDamInundationMetadata(self, damName, addGeneric=False):
        if not isinstance(damName, str) :
            # This would only happen if the data from the recommender
            # was somehow corrupted.  For now we are letting clients
            # report this condition if necessary.
            return None
        allDamInundationMetadata = self.bridge.getDamMetaData()
        if allDamInundationMetadata == None :
            # This represents a serious error because there should always at
            # least be some default non-dam specific metadata in base.
            tb = traceback.format_stack()
            sys.stderr.write("\nUNEXPECTED CONDITION!!! None returned from Bridge:getDamMetaData()\n")
            for tbentry in tb[:-1] :
                 sys.stderr.write(tbentry)
            sys.stderr.write(tb[-1].split('\n')[0]+"\n\n")
            return None
        damInundationMetadata = allDamInundationMetadata.get(damName)
        # Returning None is the expected behavior if nothing for this dam and
        # requester does not want the generic dam info included.
        if not addGeneric :
            return damInundationMetadata
        genericMetadata = allDamInundationMetadata.get("Dam")
        if genericMetadata == None :
            # This represents a serious error because there should always at
            # least be some default non-dam specific metadata in base.
            tb = traceback.format_stack()
            sys.stderr.write("\nUNEXPECTED CONDITION!!! No non-dam specific metadata available.\n")
            for tbentry in tb[:-1] :
                 sys.stderr.write(tbentry)
            sys.stderr.write(tb[-1].split('\n')[0]+"\n\n")
            return damInundationMetadata
        if damInundationMetadata == None :
            # This most often happens if the dam is in mapdata.daminundation
            # SQL table with no associated metadata in DamMetaData.py. For now
            # we are letting clients report this condition if necessary.
            # We can still fold the dam name into the generic metadata.
            damInundationMetadata = {}
            riverName = '|* riverName *|'
            cityInfo = '|* downstream town*|'
        else :
            riverName = damInundationMetadata.get("riverName", '|* riverName *|')
            cityInfo = damInundationMetadata.get("cityInfo", '|* downstream town*|')
        for k in genericMetadata.keys() :
            if k in damInundationMetadata :
                continue
            v = genericMetadata[k]
            v = v.replace("${damName}", damName)
            v = v.replace("${riverName}", riverName)
            v = v.replace("${cityInfo}", cityInfo)
            damInundationMetadata[k] = v
        return damInundationMetadata

    def getAllDamInundationMetadata(self):
        return self.bridge.getDamMetaData()

    def getBurnScarMetadata(self, burnscarName):
        burnScarAreaMetadata = self.bridge.getBurnScarMetaData()
        return burnScarAreaMetadata.get(burnscarName)

    def getAllBurnScarMetadata(self):
        return self.bridge.getBurnScarMetaData()
