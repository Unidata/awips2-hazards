# #
# This software was developed and / or modified by the
# National Oceanic and Atmospheric Administration (NOAA), 
# Earth System Research Laboratory (ESRL), 
# Global Systems Division (GSD), 
# Evaluation & Decision Support Branch (EDS)
# 
# Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
# #


#
# Handler for translating visual features between Java and Python.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer        Description
#    ------------    ----------    ------------    --------------------------
#    Mar 01, 2016      15676       Chris.Golden    Initial Creation.
#

import json
from shapely.geometry.base import BaseGeometry
from shapely import wkt

from VisualFeatures import VisualFeatures
from gov.noaa.gsd.common.visuals import VisualFeaturesList
from gov.noaa.gsd.common.visuals import VisualFeaturesListJsonConverter
from com.raytheon.uf.common.python import PyJavaUtil


# JSON encoder that handles dictionary values that are
# shapely geometries, as well as dictionary keys that
# are tuples.
class VisualFeaturesListJsonEncoder(json.JSONEncoder):

    # Perform pre-encoding for the specified object,
    # converting shapely geometry objects to WKT strings,
    # and converting dictionary keys that are tuples into
    # strings in which the tuple elements are separated
    # by a single space (since tuples as dictionary keys
    # are not possible with JSON).
    def preEncode(self, object):

        # Encode geometries in Well-Known Text format.
        if issubclass(type(object), BaseGeometry):
            return object.to_wkt()

        # If the object is a dictionary, convert any keys
        # that are tuples into strings, and do any pre-
        # encoding needed for the values.
        if isinstance(object, dict):
            newObject = {}
            for key, value in object.iteritems():
                if isinstance(key, tuple):
                    key = str(key[0]) + " " + str(key[1])
                newObject[key] = self.preEncode(value)
            return newObject

        # If the object is a list, do any pre-encoding
        # needed on the elements.
        if isinstance(object, list):
            newObject = []
            for element in object:
                newObject.append(self.preEncode(element))
            return newObject

        # No pre-encoding was needed, so simply return
        # the original object.
        return object

    # Encode the specified object.
    def encode(self, object):
        return super(VisualFeaturesListJsonEncoder,
                     self).encode(self.preEncode(object))


# JSON decoder that converts WKT strings to shapely
# geometries, and changes dictionary keys that are
# strings consisting of two timestamps separated by
# a space into two-element tuples.
class VisualFeaturesListJsonDecoder(json.JSONDecoder):

    # Initialize the instance.
    def __init__(self, *args, **kwargs):
        json.JSONDecoder.__init__(self, object_hook = self.dictToObject,
                                  *args, **kwargs)

    # If the key is "geometry", treat the specified
    # object as a Well-Known Text formatted string
    # and convert it to a geometry; otherwise, just
    # return the object untouched.
    def changeWktToGeometry(self, key, object):
        if key == "geometry":
            return wkt.loads(object)
        return object

    # Handle conversions of dictionaries to objects.
    def dictToObject(self, dictionary):

        # Do the post-decoding work on any dictionary
        # used to define a visual feature.
        if "identifier" in dictionary:

            # For any values in the dictionary that are
            # themselves dictionaries, and that are not
            # color definitions, iterate through the
            # keys, replacing any that are two elements
            # separated by a space with tuples. Also,
            # for any values of the subdictionaries or
            # of the main dictionary itself which are
            # Well-Known Text, convert them into
            # geometries.
            newDict = {}
            for key, value in dictionary.iteritems():
                if isinstance(value, dict) and "red" not in value:
                    newSubDict = {}
                    for subKey, subValue in value.iteritems():
                        keySubstrings = subKey.split(" ")
                        subValue =  self.changeWktToGeometry(key, subValue)
                        if len(keySubstrings) == 2:
                            newSubDict[tuple(keySubstrings)] = subValue
                        else:
                            newSubDict[key] = subValue
                    newDict[key] = newSubDict
                else:
                    newDict[key] = self.changeWktToGeometry(key, value)
            return newDict
        return dictionary


# Convert the specified Python list of dictionaries
# representing visual features to a VisualFeaturesList
# Java object.
def pyVisualFeaturesToJavaVisualFeatures(val):

    if val is None:
        return True, None
    
    if issubclass(type(val), VisualFeatures):
        
        # Convert the list of dictionaries to JSON.
        asJson = json.dumps(val, cls = VisualFeaturesListJsonEncoder)
        
        # Convert the JSON to Java objects.
        return True, VisualFeaturesListJsonConverter.fromJson(asJson)
    return False, val


# Convert the specified VisualFeaturesList Java object
# to a Python list of dictionaries representing
# visual features.
def javaVisualFeaturesToPyVisualFeatures(obj, customConverter = None):
    
    if obj is None:
        return True, None
    
    if PyJavaUtil.isSubclass(obj, VisualFeaturesList):
        
        # Convert the Java objects to JSON.
        asJson = VisualFeaturesListJsonConverter.toJson(obj)
        
        # Convert the JSON to a list of dictionaries.
        return True, VisualFeatures(json.loads(asJson, cls = VisualFeaturesListJsonDecoder))

    return False, obj

