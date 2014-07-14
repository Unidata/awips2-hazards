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
# Contouring help.  This is not guaranteed to work between different releases of Python
# and matplotlib because it uses low level (below main API) methods that may change with 
# no warning.  That being said, for this release (Python 2.7) these do work.
#
#
#     SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    05/29/13                      mnash       Initial Creation.
#
#
#

import matplotlib._cntr as cntr
from shapely.geometry import *
import numpy.ma as mask
import numpy

def contour(lons, lats, masked_grid):
    """
    Contours a masked_grid (booleans) for lats and lons passed in.
    
    Args: 
            lons : a grid of the longitudes
            lats : a grid of the latitudes
            masked_grid : a grid of booleans
            Note : the grids must be the same dimension and size
    Returns:
            A multipolygon  
    """
    contours = cntr.Cntr(lons, lats, masked_grid)
    polygons = list()
    for contour in contours.trace(0.5):
        polygons.append(asPolygon(contour))

    # make a new multipolygon, even though it may be a single polygon
    mp = asMultiPolygon(polygons)
    return mp

def contourMultipleValues(lons, lats, rawdata, values):
    """
    Contours a grid for lats and lons passed in based on multiple possible values
    
    Args: 
            lons : a grid of the longitudes
            lats : a grid of the latitudes
            rawdata : a grid of values
            values : the values of the rawdata to contour (list)
            Note : the grids must be the same dimension and size
    Returns:
            A multipolygon  
    """    
    # reshape to 1D to get the mask
    currentShape = rawdata.shape
    tmpdata = rawdata.flatten()
    masked_grid = mask.masked_array(numpy.in1d(tmpdata, values))
    masked_grid = masked_grid.reshape(currentShape)
    return contour(lons, lats, masked_grid)

def contourValuesBelow(lons, lats, rawdata, contourBelowValue):
    """
    Contours a grid for lats and lons passed in having values below the given value
    
    Args: 
            lons : a grid of the longitudes
            lats : a grid of the latitudes
            rawdata : a grid of values
            contourBelowValue : the value of which to contour anything below
            Note : the grids must be the same dimension and size
    Returns:
            A multipolygon  
    """
    masked_grid = mask.masked_less(rawdata, contourBelowValue)
    return contour(lons, lats, masked_grid)

def contourValuesAbove(lons, lats, rawdata, contourAboveValue):
    """
    Contours a grid for lats and lons passed in having values above the given value
    
    Args: 
            lons : a grid of the longitudes
            lats : a grid of the latitudes
            rawdata : a grid of values
            contourAboveValue : the value of which to contour anything above
            Note : the grids must be the same dimension and size
    Returns:
            A multipolygon  
    """
    masked_grid = mask.masked_greater(rawdata, contourAboveValue)
    return contour(lons, lats, masked_grid)

def contourValuesAboveEqual(lons, lats, rawdata, contourAboveValue):
    """
    Contours a grid for lats and lons passed in having values above the given value or equal
    
    Args: 
            lons : a grid of the longitudes
            lats : a grid of the latitudes
            rawdata : a grid of values
            contourAboveValue : the value of which to contour anything above or equal
            Note : the grids must be the same dimension and size
    Returns:
            A multipolygon  
    """
    masked_grid = mask.masked_greater_equal(rawdata, contourAboveValue)
    return contour(lons, lats, masked_grid)

def contourValuesBelowEqual(lons, lats, rawdata, contourBelowValue):
    """
    Contours a grid for lats and lons passed in having values below the given value or equal
    
    Args: 
            lons : a grid of the longitudes
            lats : a grid of the latitudes
            rawdata : a grid of values
            contourBelowValue : the value of which to contour anything below or equal
            Note : the grids must be the same dimension and size
    Returns:
            A multipolygon  
    """
    masked_grid = mask.masked_less_equal(rawdata, contourBelowValue)
    return contour(lons, lats, masked_grid)


