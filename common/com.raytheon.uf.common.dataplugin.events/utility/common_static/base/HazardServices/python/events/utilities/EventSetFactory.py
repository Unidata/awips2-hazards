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
# Creates an EventSet from Python
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    08/15/13                      mnash       Initial Creation.
#    
# 
#

from EventSet import EventSet
from Event import Event

from com.raytheon.uf.common.dataplugin.events import EventSet as JavaEventSet

def createEventSet(events=None):        
    '''
    @return: an EventSet
    '''
    javaEventSet = JavaEventSet()
    eventSet = EventSet(javaEventSet)
    if events is not None :
        if isinstance(events, list) :
            eventSet.addAll(events)
        elif isinstance(events, Event):
            eventSet.add(events)
    return eventSet