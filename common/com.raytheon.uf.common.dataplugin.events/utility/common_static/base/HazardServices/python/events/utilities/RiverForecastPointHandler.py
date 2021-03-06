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

'''
    Handler for RiverForecastPoints to Java and back.
'''

from com.raytheon.uf.common.hazards.hydro import RiverForecastPoint
from com.raytheon.uf.common.python import PyJavaUtil
from RiverForecastPoint import RiverForecastPoint as PyRiverForecastPoint

def pyRiverForecastPointToJavaRiverForecastPoint(val):
    if isinstance(val, PyRiverForecastPoint) == False:
        return False, val
    return True, val.toJavaObj()

def javaRiverForecastPointToPyRiverForecastPoint(obj, customConverter=None):
    if _isJavaConvertible(obj) == False:
        return False, obj
    rfPoint = PyRiverForecastPoint(obj)
    return True, rfPoint

def _isJavaConvertible(obj):
    return PyJavaUtil.isSubclass(obj, RiverForecastPoint)