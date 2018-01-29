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
# Store, clear, and retrieve generic registry objects. This is the access
# point for storage of any script-defined objects in the registry, and for
# accessing those stored objects.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    Oct 02, 2017    38506         Chris.Golden   Initial creation.
#    Jan 24, 2018    25765         Chris.Golden   Added sanity checks for
#                                                 queryObjects() parameters.
#
import JUtil, datetime
import TimeUtils
from java.util import ArrayList

from com.raytheon.uf.common.dataplugin.events.hazards.datastorage import GenericRegistryObjectManager
from com.raytheon.uf.common.dataplugin.events.hazards.event import GenericRegistryObject
from com.raytheon.uf.common.dataplugin.events.hazards.request import GenericRegistryObjectQueryRequest,\
    HazardQueryParameter


'''
Store the specified generic registry object in the registry.

@param objectDict Dictionary representing a generic registry object with the
        following entry:
            uniqueID:
                String holding an identifier that is unique across all generic
                registry objects. If the dictionary does not hold this value,
                the behavior is not guaranteed. 
        Additionally, it may hold any other entries with names other than
        "uniqueID", and values that are standard simple objects (booleans,
        numbers, strings, and datetime instances), as well as collections
        (lists, sets) of simple objects.
@param practice Flag indicating whether or not practice mode is in effect.
'''
def storeObject(objectDict, practice):

    objects = ArrayList(1)
    objects.add(_getGenericRegistryObjectFromDict(objectDict))

    manager = GenericRegistryObjectManager(practice)
    manager.store(objects)


'''
Store the specified generic registry objects in the registry.

@param objectDicts Collection of dictionaries, each representing a generic
        registry object, with the following entry:
            uniqueID:
                String holding an identifier that is unique across all generic
                registry objects. If the dictionary does not hold this value,
                the behavior is not guaranteed. 
        Additionally, each dictionary may hold any other entries with names
        other than "uniqueID", and values that are standard simple objects
        (booleans numbers, strings, and datetime instances), as well as
        collections (lists, sets) of simple objects.
@param practice Flag indicating whether or not practice mode is in effect.
'''
def storeObjects(objectDicts, practice):

    objects = _getGenericRegistryObjectsFromDicts(objectDicts)

    manager = GenericRegistryObjectManager(practice)
    manager.store(objects)


'''
Update the specified generic registry object in the registry.

@param objectDict Dictionary representing a generic registry object with the
        following entry:
            uniqueID:
                String holding an identifier that is unique across all generic
                registry objects. If the dictionary does not hold this value,
                the behavior is not guaranteed. 
        Additionally, it may hold any other entries with names other than
        "uniqueID", and values that are standard simple objects (booleans,
        numbers, strings, and datetime instances), as well as collections
        (lists, sets) of simple objects.
@param practice Flag indicating whether or not practice mode is in effect.
'''
def updateObject(objectDict, practice):

    objects = ArrayList(1)
    objects.add(_getGenericRegistryObjectFromDict(objectDict))

    manager = GenericRegistryObjectManager(practice)
    manager.update(objects)


'''
Update the specified generic registry objects in the registry.

@param objectDicts Collection of dictionaries, each representing a generic
        registry object, with the following entry:
            uniqueID:
                String holding an identifier that is unique across all generic
                registry objects. If the dictionary does not hold this value,
                the behavior is not guaranteed. 
        Additionally, each dictionary may hold any other entries with names
        other than "uniqueID", and values that are standard simple objects
        (booleans numbers, strings, and datetime instances), as well as
        collections (lists, sets) of simple objects.
@param practice Flag indicating whether or not practice mode is in effect.
'''
def updateObjects(objectDicts, practice):

    objects = _getGenericRegistryObjectsFromDicts(objectDicts)

    manager = GenericRegistryObjectManager(practice)
    manager.update(objects)


'''
Delete the specified generic registry object from the registry.

@param objectDict Dictionary representing a generic registry object with the
        following entry:
            uniqueID:
                String holding an identifier that is unique across all generic
                registry objects. If the dictionary does not hold this value,
                the behavior is not guaranteed. 
        Additionally, it may hold any other entries with names other than
        "uniqueID", and values that are standard simple objects (booleans,
        numbers, strings, and datetime instances), as well as collections
        (lists, sets) of simple objects.
@param practice Flag indicating whether or not practice mode is in effect.
'''
def removeObject(objectDict, practice):

    objects = ArrayList(1)
    objects.add(_getGenericRegistryObjectFromDict(objectDict))

    manager = GenericRegistryObjectManager(practice)
    manager.remove(objects)


'''
Delete the specified generic registry objects from the registry.

@param objectDicts Collection of dictionaries, each representing a generic
        registry object, with the following entry:
            uniqueID:
                String holding an identifier that is unique across all generic
                registry objects. If the dictionary does not hold this value,
                the behavior is not guaranteed. 
        Additionally, each dictionary may hold any other entries with names
        other than "uniqueID", and values that are standard simple objects
        (booleans numbers, strings, and datetime instances), as well as
        collections (lists, sets) of simple objects.
@param practice Flag indicating whether or not practice mode is in effect.
'''
def removeObjects(objectDicts, practice):

    objects = _getGenericRegistryObjectsFromDicts(objectDicts)

    manager = GenericRegistryObjectManager(practice)
    manager.remove(objects)


'''
Delete all generic registry objects from the registry.

@param practice Flag indicating whether or not practice mode is in effect.
'''
def removeAllObjects(practice):
    manager = GenericRegistryObjectManager(practice)
    manager.removeAll()


'''
Get a list of generic registry objects that match the specified query
parameters.

@param parameters Collection of query parameters, each of which is a tuple.
       The first entry is the name of the property to be matched. The second
       element, which is optional, is the operand to be used in the matching
       attempt, e.g. "=" or "in". The third element (or second, if the
       operand is not included) is the value(s) against which to attempt to
       match the values that the various stored generic registry objects have
       under the associated key. So, for example, the tuple:
           ("eventIdentifiers", "in", [ "event1", "event2", "event3" ])
       would match any objects with the values under "eventIdentifiers" that
       match "event1", "event2", and/or "event3".
@param practice Flag indicating whether or not practice mode is in effect.
@return List of dictionaries representing the generic registry objects that
        match. Each dictionary has the following entry:
            uniqueID:
                String holding an identifier that is unique across all generic
                registry objects.
        Additionally, each dictionary may hold any other entries with names
        other than "uniqueID", and values that are standard simple objects
        (booleans numbers, strings, and datetime instances), as well as
        collections (lists, sets) of simple objects. 
'''
def queryObjects(parameters, practice):

    # Iterate through the parameters, converting each into a Java object.    
    queryParameters = ArrayList(len(parameters))
    for parameter in parameters:
        
        # Get the key and value, and the operand if the tuple is large
        # enough.
        key = parameter[0]
        if key is None:
            raise ValueError("query parameter key cannot be None")
        operand = (None if len(parameter) is 2 else parameter[1])
        value = (parameter[1] if len(parameter) is 2 else parameter[2])
        if value is None:
            raise ValueError("query parameter value cannot be None")
        multipleValues = isinstance(value, (list, set, tuple))
        if multipleValues:
            if len(value) == 0:
                raise ValueError("query parameter value cannot be empty collection")
            for valueElement in value:
                if valueElement is None:
                    raise ValueError("query parameter value collection cannot hold any elements with value of None")
        value = JUtil.pyValToJavaObj(value)
        if multipleValues:
            value = value.toArray()
            
        # Add the appropriately constructed Java object to the parameters
        # list.
        if operand is None:
            queryParameters.add(HazardQueryParameter(key, value))
        else:
            queryParameters.add(HazardQueryParameter(key, operand, value))

    # Create the query and submit it, and return the results.
    manager = GenericRegistryObjectManager(practice)
    map = manager.query(GenericRegistryObjectQueryRequest(practice, queryParameters))
    return _getDictsFromGenericRegistryObjects(map.values())


'''
Private Functions
'''

# Convert the specified dictionary to a Generic Registry Object.
def _getGenericRegistryObjectFromDict(objectDict):
    object = GenericRegistryObject()
    object.setUniqueID(objectDict["uniqueID"])
    objectDict = { key:objectDict[key] for key in objectDict if key != 'uniqueID' }
    object.setProperties(JUtil.pyValToJavaObj(objectDict))
    return object

# Convert the specified list of dictionaries to a Java ArrayList of
# GenericRegistryObject instances.
def _getGenericRegistryObjectsFromDicts(objectDicts):
    objects = ArrayList(len(objectDicts))
    for objectDict in objectDicts:
        objects.add(_getGenericRegistryObjectFromDict(objectDict))
    return objects

# Convert the specified Java GenericRegistryObject to a dictionary.
def _getDictFromGenericRegistryObject(object):
    objectDict = JUtil.javaObjToPyVal(object.getProperties())
    objectDict["uniqueID"] = object.getUniqueID()
    return objectDict

# Convert the specfied Java Collection of GenericRegistryObject instances to
# a Python list of dictionaries, each of the latter representing one of the
# generic registry objects.
def _getDictsFromGenericRegistryObjects(objects):
    objectDicts = []
    iterator = objects.iterator()
    while iterator.hasNext():
        objectDicts.append(_getDictFromGenericRegistryObject(iterator.next()))
    return objectDicts
