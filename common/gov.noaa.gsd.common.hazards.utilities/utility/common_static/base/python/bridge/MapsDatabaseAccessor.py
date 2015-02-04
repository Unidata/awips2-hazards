"""
This class will access the maps database.  The initial intended functionality 
is to retrieve geospatial data and associated metadata.  For example, dam break
inundation polygons and associated 'damInfo' metadata are stored in mapdata.daminfo
table.  This module will retrieve these data to be used in recommenders and 
product generation.

It is envisioned that this module can/will be extended to access similar information
like burn scar areas.   

 @since: February 2015
 @author: GSD Hazard Services Team
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 Feb 03, 2015            kmanross     Initial development

"""

from shapely.geometry import Polygon
from ufpy.dataaccess import DataAccessLayer

class MapsDatabaseAccessor(object):
    def __init__(self):
        pass
        
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
        
    def getAllDamInfo(self):
        req = DataAccessLayer.newDataRequest()
        req.setDatatype("maps")
        req.addIdentifier("table", "mapdata.daminfo")
        req.addIdentifier("geomField", "the_geom")
        
        columns = [
                   "name",
                   "dam_name",
                   "river_name",
                   "rule_of_thumb",
                   "city_info",
                   "scenario_high_fast",
                   "scenario_high_normal",
                   "scenario_medium_fast",
                   "scenario_medium_normal"
                   ]
        ### Important to set addIdentifier("locationField", "name") !!!
        req.addIdentifier("locationField", columns[0])
        req.setParameters(*columns)
        
        retGeoms = DataAccessLayer.getGeometryData(req)
        
        retList = []
        for retGeom in retGeoms :
            retDict = {}
            poly = self._extract_poly_coords(retGeom.getGeometry())
            formattedPoly = [list(c) for c in poly['exterior_coords']]
            retDict["polygon"] = formattedPoly
            
            for col in columns:
                retDict[col] = retGeom.getString(col)
            retList.append(retDict)
            
        return retList