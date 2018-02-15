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
# Manager for generic session objects, allowing them to be stored and
# retrieved. Unlike generic registry objects, these objects are not
# persisted between sessions, nor are they shared between clients.
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    May 30, 2018    14791         Chris.Golden   Initial creation.
#
import JUtil

class GenericSessionObjectManager(object):
    
    def __init__(self, javaObject):
        '''
        @summary Construct an instance wrapping a Java GenericSessionObjectManager
        object.
        @param javaObject Java GenericSessionObjectManager instance to be wrapped.
        '''
        self._javaObject = javaObject

    def get(self, key):
        '''
        @summary Get the generic session object associated with the specified key.
        @param key Key for which to fetch the value.
        @return Value associated with the key, or None if no such value is found.
        '''
        value = self._javaObject.get(key)
        return JUtil.javaObjToPyVal(value) if value is not None else None

    def set(self, key, value):
        '''
        @summary Create an entry associating the specified key with the specified
        value.
        @param key Key for which to create the new entry.
        @param value Value to be associated with the key.
        '''
        value = JUtil.pyValToJavaObj(value) if value is not None else None
        return self._javaObject.set(key, value)

    def getWrappedJavaObject(self):
        '''
        @summary Get the Java object, which will be a GenericSessionObjectManager,
        that is wrapped by this instance.
        @return Java object wrapped by this instance.
        '''
        return self._javaObject

