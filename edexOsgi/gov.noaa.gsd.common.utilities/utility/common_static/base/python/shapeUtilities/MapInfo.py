"""
Description: Map Information class - has routines to read from
 AWIPS II Maps Geo Database and convert into lookup values and
 provide polygons.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation
Apr 19, 2013            blawrenc            Removed unused methods for
                                            code review prep.

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""
import  cPickle, os

import MapAttributes as MapAttributes
from LatLonCoord import *
from ufpy.dataaccess import DataAccessLayer
from shapely.geometry import Polygon 

class MapInfo:

    def __init__(self):
        """
        constructor
        """
    
    def getDescriptions(self, polygon, ugcType="c"):
        '''
        This is stubbed for now.  
        It will be written using the Data Access Layer in PV2
        '''
        return []
        coordList = []
        # Convert to list of LatLonCoords
        for lat, lon in polygon:
            coordList.append(LatLonCoord(lat, lon))
        ugc = getUGCs(ugcType)        
        return ugc.getDescriptions(coordList)

    # ---------------------------------------------------------------------
    # Map and Polygon Routines
    # ---------------------------------------------------------------------      
    def getUGCsMatchPolygons(self, mapType, userPolygons, filtFunc=None, compareToken=None, siteID=None):
        """
        Returns a list of UGCs that overlap the user-supplied polygon for
        the given mapType.  
        
        @param mapType -- counties or publicZones
        @param userPolygons -- Limit areas overlapping the polygons
                           -- format is a list of points (lon, lat)
        @param filtFunc -- function for filtering attributes
        @param compareToken -- filter values for comparison
        @siteID -- if supplied filter according to siteID
        @return -- list of UGC codes
        """                 
        # If what is passed in is a point, we will expand it into a really
        # tiny polygon and go from there.
        polygons = []
        for polygon in userPolygons:
            if len(polygon) == 1 :
                lon,lat = polygon[0]
                polygon = [
                               (lon, lat-0.0001, ),
                               (lon+0.0001, lat, ),
                               (lon, lat+0.0001, ),
                               (lon-0.0001, lat, ),
                               (lon, lat-0.0001)
                               ]
            else:
        
                polygon = self._ensureClosedPolygon(polygon)
            polygons.append(polygon)
            
        # Get the geometries for the mapType and filters
        geometries = self.getMapPolygons(mapType, polygons, filtFunc, compareToken, siteID)
        
        # Construct the UGCs from filtered geometries
        attUGCFunc = MapAttributes.ugcFromAttributes.get(mapType, None)
        if attUGCFunc is None:
            return None
        if mapType == "publicZones":
            mapAtts = ['state', 'zone','shortname']
        elif mapType == "counties":
            mapAtts = ['state', 'fips','countyname']
        ugcList = []
        for geometry in geometries:
            atts = {}
            for att in mapAtts:
                atts[att]= geometry.getString(att)
            ugc = attUGCFunc(atts)
            ugcList.append(ugc)
        return ugcList

    def getMapPolygons(self, mapType, userPolygons=None, filtFunc=None, 
      compareToken=None, siteID=None):
        """
        Return the geometries and attributes for counties or zones
        
        @param mapType -- counties or publicZones
        @param userPolygons -- if supplied will limit to areas overlapping the polygons
                           -- format is a list of points (lon, lat)
        @param filtFunc -- function for filtering attibutes
        @param compareToken -- filter values for comparison
        @siteID -- if supplied filter according to siteID
        @return -- geometries that meet the criteria
        """
        
        req = DataAccessLayer.newDataRequest()
        req.setDatatype("maps") # the factory to use
        if mapType == "publicZones":
            req.addIdentifier("table","mapdata.zone") # the table to get from
        elif mapType == "counties":
            req.addIdentifier("table","mapdata.county") # the table to get from           
        req.addIdentifier("geomField","the_geom") # the geometry field in the table to return
        req.addIdentifier("inLocation", "true") # adds another filter based on location
        req.addIdentifier("locationField","cwa") # for extra querying based on location, search the cwa field for the cwas below
        req.setLocationNames(siteID, "BOU", "DMX") # adds this to the filter
        req.addIdentifier("cwa",siteID) # returns all geometries in this cwa        
        if mapType == "publicZones":
            mapAtts = ['state', 'zone', 'shortname']
            req.setParameters("shortname","state", 'zone') # other parameters that you want besides the geometry
        elif mapType == "counties":
            mapAtts = ['state', 'fips', 'countyname']
            req.setParameters("countyname", "state","fips") 
        geometries = DataAccessLayer.getGeometryData(req)
        
        # Make a shapely polygon from userPolygon
        #  Ugly!! There's got to be a better way :)
#        size = len(userPolygon)
#        i = 0
#        points = []
#        while i < size:
#            lon = userPolygon[i]
#            lat = userPolygon[i+1]
#            points.append((lon, lat))
#            i+=2

        # Make Shapely Polygons and BBoxes
        polygons = []
        bboxes = []
        for polygon in userPolygons:
            poly = Polygon(polygon)
            polygons.append(poly)
            bboxes.append(poly.envelope)
                                     
        # Filter on Bounding Box
        # get the bounding boxes (required for final record and for bbox
        # filtering.
        bboxGeometries = []   
        for geometry in geometries:
            geomBBox = geometry.getGeometry().envelope
            for bbox in bboxes:
                if bbox is not None and not bbox.intersects(geomBBox):
                    continue   #outside of bounding box
                bboxGeometries.append(geometry)   #cache it

        # Filter on Attributes
        # get the attributes to do filtering, we only need to do the
        # records that made it through the bounding box filter.
        attGeometries = []
        for geometry in bboxGeometries: 
            atts = {}
            for att in mapAtts:
                atts[att]= geometry.getString(att)
            if filtFunc is not None and not filtFunc(atts, compareToken):
                continue #doesn't meet filter criteria
            attGeometries.append(geometry)   #cache it
        
        # Filter on polygons
        # Get the polygons, we only need to do the records that made it
        # through the attribute filter.
        polyGeometries = []
        for geometry in attGeometries:
            for poly in polygons:
                if poly.intersects(geometry.getGeometry()):
                    polyGeometries.append(geometry)        
        return polyGeometries
            

    # ---------------------------------------------------------------------
    # Common Routines
    # ---------------------------------------------------------------------

    def _ensureClosedPolygon(self, polygon):
        '''
        ensureClosedPolygon
         Passed a (lon, lat) polygon ensures that the last point
         is equal to the first point, if not, adds the point.
        '''
        if len(polygon) >= 2 and polygon[0] != polygon[-1]:
            polygon.append(polygon[0])   #add last point equal to 1st point
        return polygon

