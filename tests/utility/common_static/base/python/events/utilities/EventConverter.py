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
# Globally import and sets up instances of the recommenders.
#   
#
#    
#    SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/22/13                      mnash        Initial Creation.
#    
# 
#

import os

from PathManager import PathManager
from PathManager import COMMON_STATIC, BASE, SITE, USER

from com.raytheon.uf.common.dataplugin.events import EventSet

from EventSet import EventSet

def findConverter(eventSet):        
    # for now get the BASE site only, maybe we should add lower levels of localization later
    pathMgr = PathManager()
    context = pathMgr.getContext(COMMON_STATIC, BASE)
    
    # TODO, needs to better handle the /
    path = os.path.join('python', 'events')
    events = pathMgr.listFiles(context, path, [".py"], False, True)
    if eventSet.size() > 0 :
        iter = eventSet.iterator()
        #assumes that every thing in the event set is of the same type
        next = iter.next()
        # for each python module in the directory
        for event in events :
            filename = os.path.splitext(event)[0]
            importedEvent = __import__(filename, globals(), locals(), [], -1)
            if hasattr(importedEvent,'canConvert'):
                if importedEvent.canConvert(next) :
                    return importedEvent
    return None

def convert(eventSet, converter):
    pyEvents = EventSet(eventSet)
    iter = eventSet.iterator()
    while iter.hasNext() :
        next = iter.next()
        ev = converter.convert(next)
        pyEvents.add(ev)
    return pyEvents