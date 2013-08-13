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
#    06/25/13                      blawrenc     Changed to event.getPath()
#                                               getFilePath() does not exist.
# 
#

import os
import imp
import LocalizationUtil

from PathManager import PathManager
from EventSet import EventSet

from com.raytheon.uf.common.dataplugin.events import EventSet as JavaEventSet

def findConverter(eventSet):        
    # for now get the BASE file only, maybe we should add lower levels of localization later
    pathMgr = PathManager()
    
    path = os.path.join('python', 'events')
    events = pathMgr.listFiles(path, ['.py'], False, True, loctype='COMMON_STATIC', loclevel='BASE')
    if eventSet.size() > 0 :
        iter = eventSet.iterator()
        # assumes that every thing in the event set is of the same type
        next = iter.next()
        # for each python module in the directory
        if events is not None :
            for event in events :
                importedEvent = LocalizationUtil.loadModule(event.getPath())
                if hasattr(importedEvent, 'canConvert'):
                    if importedEvent.canConvert(next) :
                        return importedEvent
    return None

def convert(eventSet, converter):
    pyEvents = set()
    iter = eventSet.iterator()
    while iter.hasNext() :
        next = iter.next()
        ev = converter.convert(next)
        pyEvents.add(ev)
    return pyEvents
