# #
# This software was developed and / or modified by the
# National Oceanic and Atmospheric Administration (NOAA), 
# Earth System Research Laboratory (ESRL), 
# Global Systems Division (GSD), 
# Evaluation & Decision Support Branch (EDS)
# 
# Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
# #

from com.raytheon.uf.common.python import PyJavaUtil
from GenericSessionObjectManager import GenericSessionObjectManager
from com.raytheon.uf.common.dataplugin.events import GenericSessionObjectManager as JavaGenericSessionObjectManager

#
# Handler for translating generic session object managers between Java and
# Python.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer        Description
#    ------------    ----------    ------------    --------------------------
#    May 30, 2018      14791       Chris.Golden    Initial creation.
#

# Convert the specified Python generic session object manager to its Java
# counterpart.
def pyGenericSessionObjectManagerToJavaGenericSessionObjectManager(val):

    if isinstance(val, GenericSessionObjectManager):
        return True, val.getWrappedJavaObject()

    return False, val

# Convert the specified Java generic session object manager to its Python
# counterpart.
def javaGenericSessionObjectManagerToPyGenericSessionObjectManager(obj, customConverter = None):

    if PyJavaUtil.isSubclass(obj, JavaGenericSessionObjectManager):
        return True, GenericSessionObjectManager(obj)

    return False, obj

