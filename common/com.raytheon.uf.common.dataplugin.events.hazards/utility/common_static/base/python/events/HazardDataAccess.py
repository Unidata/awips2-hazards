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
# Retrieve hazard events based on eventId
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    09/26/13                      mnash          Initial Creation.
#    12/05/13        2527          bkowal         Use JUtil to convert Hazards
#    4/8/15          7091          hansen         Added getHazardEventsBySite
#    05/13/15        8161          mduff          Changes for Jep upgrade.
# 
#

import JUtil
from com.raytheon.uf.common.dataplugin.events.hazards.datastorage import HazardEventManager
Mode = HazardEventManager.Mode

def getHazardEvent(eventId, mode):        
    manager = HazardEventManager(Mode.valueOf(mode))
    historyList = manager.getByEventID(eventId)
    if ((historyList == None) or (historyList.size() == 0)):
        return None
    
    return JUtil.javaObjToPyVal(historyList.get(historyList.size() - 1))

def getHazardEventsBySite(siteID, mode):
    manager = HazardEventManager(Mode.valueOf(mode))
    hazardEventMap = manager.getBySiteID(siteID)
    if ((hazardEventMap == None) or (hazardEventMap.size() == 0)):
        return []
    hazardEvents = []
    entrySet = hazardEventMap.entrySet()
    iterator = entrySet.iterator()
    while iterator.hasNext():
        item = iterator.next()
        historyList = item.getValue()
        hazardEvents.append(JUtil.javaObjToPyVal(historyList.get(historyList.size() - 1)))    
    return hazardEvents
