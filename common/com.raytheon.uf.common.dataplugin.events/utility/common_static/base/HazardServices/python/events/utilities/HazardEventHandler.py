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
# Handler for Hazard Events to Java and back.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    12/02/13         2527         bkowal         Initial Creation.
#    12/14/17        20739         Chris.Golden   Added abilty to convert to
#                                                 and from a read-only hazard
#                                                 event.
#

from com.raytheon.uf.common.dataplugin.events.hazards.event import IReadableHazardEvent
from com.raytheon.uf.common.dataplugin.events.hazards.event import IHazardEvent
from com.raytheon.uf.common.python import PyJavaUtil

from ReadableHazardEvent import ReadableHazardEvent
from HazardEvent import HazardEvent
from Event import Event

def pyHazardEventToJavaHazardEvent(val):
    if isinstance(val, Event) == False:
        return False, val
    return True, val.toJavaObj()

def javaHazardEventToPyHazardEvent(obj, customConverter=None):
    if PyJavaUtil.isSubclass(obj, IHazardEvent):
        return True, HazardEvent(obj)
    elif PyJavaUtil.isSubclass(obj, IReadableHazardEvent):
        return True, ReadableHazardEvent(obj)
    else:
        return False, obj
