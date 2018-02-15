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
# Handler for SHEF data to Java and back.
#  

from com.raytheon.uf.common.hazards.hydro import Hydrograph, HydrographForecast, HydrographObserved, HydrographPrecip
from com.raytheon.uf.common.python import PyJavaUtil

from Hydrograph import Hydrograph as PyHydrograph
from HydrographForecast import HydrographForecast as PyHydrographForecast
from HydrographObserved import HydrographObserved as PyHydrographObserved
from HydrographPrecip import HydrographPrecip as PyHydrographPrecip

def pyHydrographToJavaHydrograph(val):
    if isinstance(val, PyHydrograph) == False:
        return False, val
    return True, val.toJavaObj()

def javaHydrographToPyHydrograph(obj, customConverter=None):
    if PyJavaUtil.isSubclass(obj, HydrographForecast):
        hydrograph = PyHydrographForecast(obj)
    elif PyJavaUtil.isSubclass(obj, HydrographObserved):
        hydrograph = PyHydrographObserved(obj)
    elif PyJavaUtil.isSubclass(obj, HydrographPrecip):
        hydrograph = PyHydrographPrecip(obj)
    else:
        return False, obj
    return True, hydrograph
