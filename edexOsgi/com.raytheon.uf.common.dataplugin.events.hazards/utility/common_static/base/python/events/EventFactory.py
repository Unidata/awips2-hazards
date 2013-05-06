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
# Mirrors GeometryFactory, for ease of use when creating geometries within Python
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/22/13                      mnash       Initial Creation.
#    
# 
#

from Event import NullEvent
from HazardEvent import HazardEvent
from com.raytheon.uf.common.dataplugin.events.hazards.event import BaseHazardEvent

def createNullEvent():        
    '''
    @return: a NullEvent, indicating that no area or information about an event is 
    necessary
    '''
    ne = NullEvent()
    return ne

def createEvent():
    '''
    @return: a BaseEvent, any event that has an area and holds other information
    '''
    bhe = BaseHazardEvent()
    he = HazardEvent(bhe)
    return he
    