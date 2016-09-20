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
from AdvancedGeometry import AdvancedGeometry
from gov.noaa.gsd.common.utilities.geometry import IAdvancedGeometry

#
# Handler for translating advanced geometries between Java and Python.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer        Description
#    ------------    ----------    ------------    --------------------------
#    Sep 13, 2016      15934       Chris.Golden    Initial Creation.
#

# Convert the specified Python advanced geometry to its Java counterpart.
def pyAdvancedGeometryToJavaAdvancedGeometry(val):

    if isinstance(val, AdvancedGeometry):
        return True, val.getWrappedJavaObject()

    return False, val

# Convert the specified Java advanced geometry to its Python counterpart.
def javaAdvancedGeometryToPyAdvancedGeometry(obj, customConverter = None):

    if PyJavaUtil.isSubclass(obj, IAdvancedGeometry):
        return True, AdvancedGeometry(obj)

    return False, obj

