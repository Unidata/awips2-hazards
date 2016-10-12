# #
# This software was developed and / or modified by the
# National Oceanic and Atmospheric Administration (NOAA), 
# Earth System Research Laboratory (ESRL), 
# Global Systems Division (GSD), 
# Evaluation & Decision Support Branch (EDS)
# 
# Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
# #

from java.util import ArrayList
from gov.noaa.gsd.common.utilities import JsonConverter
from gov.noaa.gsd.common.utilities.geometry import IAdvancedGeometry
from gov.noaa.gsd.common.utilities.geometry import AdvancedGeometryUtilities
from gov.noaa.gsd.common.utilities.geometry import LinearUnit

import json
import JUtil
import GeometryHandler

#
# Advanced geometry class and functions, providing the hierarchy corresponding
# to that based in Java on IAdvancedGeometry and the functionality equivalent
# to that provided in Java by AdvancedGeometryUtilities.
#
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer        Description
#    ------------    ----------    ------------    --------------------------
#    Sep 13, 2016      15934       Chris.Golden    Initial creation.
#    Sep 21, 2016      15934       Chris.Golden    Changed to work with new version
#                                                  of AdvancedGeometryUtilities.
#    Sep 29, 2016      15928       Chris.Golden    Minor changes to comments due to
#                                                  switch to angles being specified
#                                                  in radians, and to corrections.
#
class AdvancedGeometry(object):
    
    def __init__(self, javaObject):
        '''
        @summary Construct an instance wrapping a Java IAdvancedGeometry object.
        @param javaObject Java IAdvancedGeometry instance to be wrapped.
        '''
        self._javaObject = javaObject
        
    def copyOf(self):
        '''
        @summary Build and return a copy of this geometry.
        @return Copy of this geometry.
        '''
        return AdvancedGeometry(self._javaObject.copyOf())

    def isPunctual(self):
        '''
        @summary Determine whether or not this geometry (or at least one of
        its component subgeometries, if it is a collection) is a single point.
        @return True if this geometry, or one or more of its subgeometries,
        is a point; False otherwise.
        '''
        return self._javaObject.isPunctual()

    def isLineal(self):
        '''
        @summary Determine whether or not this geometry (or at least one of
        its component subgeometries, if it is a collection) is lineal (that
        is, one or more line segments or curves, but not enclosing anything).
        @return True if this geometry, or one or more of its subgeometries,
        is lineal; False otherwise.
        '''
        return self._javaObject.isLineal()

    def isPolygonal(self):
        '''
        @summary Determine whether or not this geometry (or at least one of
        its component subgeometries, if it is a collection) is polygonal
        (that is, it encloses an area).
        @return True if this geometry, or one or more of its subgeometries,
        is polygonal; False otherwise.
        '''
        return self._javaObject.isPolygonal()

    def isPotentiallyCurved(self):
        '''
        @summary Determine whether or not this geometry (or at least one of
        its component subgeometries, if it is a collection) may include one
        or more curves.
        @return True if this geometry, or one or more of its subgeometries,
        may include one or more curves; False otherwise.
        '''
        return self._javaObject.isPotentiallyCurved()

    def isValid(self):
        '''
        @summary Determine whether or not this geometry is valid.
        @return True if this geometry is valid; False otherwise.
        '''
        return self._javaObject.isValid()

    def asShapely(self):
        '''
        @summary Return a shapely version of this geometry. If the geometry
        contains curves, each of these will be flattened into a series of
        line segments.
        @return Shapely version of the geometry.
        '''
        return GeometryHandler.jtsToShapely(AdvancedGeometryUtilities.
                                            getJtsGeometryAsCollection(self._javaObject))[1]        

    def getWrappedJavaObject(self):
        '''
        @summary Get the Java object, which subclasses IAdvancedGeometry,
        that is wrapped by this instance.
        @return Java object wrapped by this instance.
        '''
        return self._javaObject
    
    def __getstate__(self):
        '''
        @summary Get the state in dictionary form, used when the object is being
        pickled to avoid having the pickle attempt run into a Java object.
        @return State in dictionary form.
        '''
        return JUtil.javaObjToPyVal(JsonConverter.toDictionary(self._javaObject))
    
    def __setstate__(self, state):
        '''
        @summary Set the state from a dictionary generated previously via the
         __getState()__ method, used when the object is being unpickled
        @param state State in dictionary form.
        '''
        self._javaObject = JsonConverter.fromJson(json.dumps(state), IAdvancedGeometry)

#
# Functions to create advanced geometries.
#

def createEllipse(x, y, width, height, units, rotation):
    '''
    @summary Create an Ellipse object.
    @param x: Longitude of the center point of the ellipse.
    @param y: Latitude of the center point of the ellipse.
    @param width: Width, in the units given by the units parameter.
    @param height: Height, in the units given by the units parameter.
    @param units: Units in which width and height are specified; may
    be any of the LINEAR_UNITS_XXXX constants defined within the
    HazardConstants.py.
    @param rotation: Rotation in counterclockwise radians of the ellipse.
    @return: Ellipse in Java IAdvancedGeometry form.
    '''
    return AdvancedGeometry(AdvancedGeometryUtilities.createEllipse(float(x), float(y),
                                                                    float(width), float(height),
                                                                    LinearUnit.getInstanceWithIdentifier(units),
                                                                    float(rotation)))

def createShapelyWrapper(geometry, rotation):
    '''
    @summary Create a shapely wrapper object.
    @param geometry: Shapely geometry to be wrapped.
    @param rotation: Rotation in counterclockwise radians of the wrapped geometry.
    @return: Geometry wrapper in Java IAdvancedGeometry form.
    '''
    return AdvancedGeometry(AdvancedGeometryUtilities.createGeometryWrapper(GeometryHandler.
                                                                            shapelyToJTS(geometry)[1],
                                                                            float(rotation)))

def createCollection(*args):
    '''
    @summary Create an advanced geometry collection made up of the one or more
    advanced geometries provided as parameters.
    @param args: One or more advanced geometries in IAdvancedGeometry form
    to be included in the collection.
    @return: Collection in Java IAdvancedGeometry form.
    '''
    return createCollectionFromList(args)

def createCollectionFromList(advancedGeometries):
    '''
    @summary Create an advanced geometry collection made up of the list of one
    or more advanced geometries provided as the parameter.
    @param advancedGeometries: List of one or more advanced geometries in
    IAdvancedGeometry form to be included in the collection.
    @return: Collection in Java IAdvancedGeometry form.
    '''
    advancedGeometryList = ArrayList(len(advancedGeometries))
    for advancedGeometry in advancedGeometries:
        advancedGeometryList.add(advancedGeometry._javaObject)
    return AdvancedGeometry(AdvancedGeometryUtilities.createCollection(advancedGeometryList))

    
