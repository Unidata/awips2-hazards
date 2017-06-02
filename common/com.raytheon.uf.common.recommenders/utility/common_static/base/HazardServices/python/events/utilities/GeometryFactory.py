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
#    
# 
#

from shapely.geometry import *
from shapely import wkt
from shapely.ops import cascaded_union

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
    Takes any type of geometry and puts them
    into a GeometryCollection.  This is done
    in a backwards way as shapely doesn't allow
    addition of arbitrary geometries to a
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
    @param polygons: a list of polygons
    @return: the union of all of the polygons in the provided list
    '''    
    return cascaded_union(polygons) 
