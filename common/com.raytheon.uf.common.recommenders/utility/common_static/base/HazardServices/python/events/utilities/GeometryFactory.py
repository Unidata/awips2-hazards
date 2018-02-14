# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
# 
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
# 
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
# 
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
# #


#
# Mirrors GeometryFactory, for ease of use when creating geometries within Python
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/22/13                      mnash          Initial Creation.
#    02/29/16        15016         kbisanz        Update createCollection(..) to
#                                                 return None if provided an
#                                                 empty GeometryCollection
#    02/13/18        20595         Chris.Golden   Made performCascadedUnion() more
#                                                 robust, and added a method that
#                                                 makes invalid polygons valid by
#                                                 removing self-crossing edges,
#                                                 edges that run along one another,
#                                                 and self-intersecting edges.
#

from shapely.geometry import *
from shapely import wkt
from shapely.ops import cascaded_union, polygonize_full

def createLineString(coordinates):
    '''
    @param coordinates: The coordinates of the line, in lat/lon space, in order.
    @return: LineString, a shapely geometry of the line
    '''
    ls = LineString(coordinates)
    return ls
    
def createMultiLineString(coordinates):
    '''
    @param coordinates: The coordinates of the lines, in lat/lon space.  There must
    be separate arrays within the coordinates in order for there to be multiple lines.
    @return: MultiLineString, a shapely geometry of multiple lines
    '''
    mls = MultiLineString(coordinates)
    return mls
    
def createPoint(coordinate):
    '''
    @param coordinate: The coordinate of the point, in lat/lon space
    @return: Point, a shapely geometry
    '''
    p = Point(coordinate)
    return p
    
def createMultiPoint(coordinates):
    '''
    @param coordinates: The coordinates of the points, in lat/lon space
    @return: MultiPoint, a shapely geometry
    '''
    mp = MultiPoint(coordinates)
    return mp
    
def createPolygon(shell, holes=None):
    '''
    @param shell: The coordinates of the polygon outer shell, in order, in lat/lon space
    @param holes: The coordinates of the polygon holes, in order, in lat/lon space
    @return: 
    '''
    p = Polygon(shell, holes)
    return p
    
def createMultiPolygon(polygons, context_type):
    '''
    @param polygons: A list of polygons
    @return: MultiPolygon
    '''
    mp = MultiPolygon(polygons, context_type)
    return mp

def createCollection(geometries):
    '''
    Takes any type of geometry and puts them into a GeometryCollection.  This is done
    in a backwards way as shapely doesn't allow addition of arbitrary geometries to a
    GeometryCollection.
    '''
    # remove any empty geometries
    for g in list(geometries) :
        if g.is_empty :
            geometries.remove(g)

    if len(geometries) > 0:
        # If geometries still has items, create a new collection
        text = 'GEOMETRYCOLLECTION (%s)' % ', '.join(g.wkt for g in geometries)
        retval = wkt.loads(text)
    else:
        # No items in geometries
        retval = None

    return retval

def performCascadedUnion(polygons):
    '''
    Perform a safe cascaded union, attempting to correct null or invalid geometries,
    and ignoring those that cannot be corrected.
    @param polygons: A list of polygons
    @return: The union of all of the polygons in the provided list
    '''
    return cascaded_union([shape if shape.is_valid else shape.buffer(0) 
                          for shape in [polygon for polygon in polygons 
                                        if polygon and not polygon.is_empty]])

def correctPolygonIfInvalid(polygon, bufferRadius):
    '''
    Correct the specified polygon if it is invalid in the following ways: it is
    self-crossing, self-intersecting, and/or that has edges running on top of one
    another. Correction is performed by breaking it into its valid subpolygons, then
    unioning these with any points of intersection (buffered to the specified radius)
    as well as any edges (again buffered to the specified radius) that lay atop one
    another in the original invalid polygon.
    @param polygon: Polygon to be corrected, if any correction is needed.
    @param bufferRadius: Radius to be applied when buffering any points of
    intersection or edges that run on top of one another.
    @return: Corrected polygon, or original polygon if no correction is needed.
    '''
    
    # Do no correcting if the polygon is already valid.
    if polygon.is_valid:
        return polygon
    
    # Take the exterior of the invalid polygon, intersect it with itself,
    # and then polygonizing the resulting multi-line strings. This in turn
    # provides a list of valid sub-polygons and a list of "dangles", line
    # strings that represent edges of the original invalid polygon that
    # ran along on top of one another.
    #
    # Note that this part of the algorithm is taken from:
    #
    #     https://gis.stackexchange.com/questions/243144/bowtie-or-hourglass-polygon-validity-issue-when-self-crossing-point-is-not-defin
    #
    # but differs in that it does not use polygonize(), which would only
    # yield the resulting sub-polygons, but instead needs the dangles as
    # well.
    exterior = polygon.exterior
    multiLineStrings = exterior.intersection(exterior)
    subPolygons, dangles, cuts, invalids = polygonize_full(multiLineStrings)
    subPolygons = list(subPolygons)
    
    # If there is more than one sub-polygon, compare all sub-polygons'
    # vertices with one another to find any intersection points, and for
    # any such intersections, record them. Then turn each into a polygon
    # using the provided buffer radius, and add it to the list of sub-
    # polygons.
    if len(subPolygons) > 1:
        intersectionCoords = set()
        subPolygonsCoords = [set(subPolygon.exterior.coords) for subPolygon in subPolygons]
        for index1 in range(0, len(subPolygons)):
            for index2 in range(index1 + 1, len(subPolygons)):
                intersectionCoords = intersectionCoords.union(subPolygonsCoords[index1].intersection(subPolygonsCoords[index2]))
        for coord in intersectionCoords:
            subPolygons.append(Point(coord).buffer(bufferRadius, 2))
    
    # Turn any edges that ran on top of one another in the original
    # invalid polygon into polygons by buffering them, and add them to
    # the list of sub-polygons.
    for dangle in list(dangles):
        subPolygons.append(dangle.buffer(bufferRadius, 2))
    
    # Finally, take the union of all the sub-polygons.
    return cascaded_union(subPolygons)
