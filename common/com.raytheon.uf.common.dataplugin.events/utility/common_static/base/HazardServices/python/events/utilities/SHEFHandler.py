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

from com.raytheon.uf.common.hazards.hydro import SHEFBase, SHEFForecast, SHEFObserved, SHEFPrecip
from com.raytheon.uf.common.python import PyJavaUtil

from SHEFBase import SHEFBase as PySHEFBase
from SHEFForecast import SHEFForecast as PySHEFForecast
from SHEFObserved import SHEFObserved as PySHEFObserved
from SHEFPrecip import SHEFPrecip as PySHEFPrecip

def pySHEFToJavaSHEF(val):
    if isinstance(val, PySHEFBase) == False:
        return False, val
    return True, val.toJavaObj()

def javaSHEFToPySHEF(obj, customConverter=None):
    if PyJavaUtil.isSubclass(obj, SHEFForecast):
        shef = PySHEFForecast(obj)
    elif PyJavaUtil.isSubclass(obj, SHEFObserved):
        shef = PySHEFObserved(obj)
    elif PyJavaUtil.isSubclass(obj, SHEFPrecip):
        shef = PySHEFPrecip(obj)
    else:
        return False, obj
    return True, shef
