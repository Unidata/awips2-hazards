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
#    May 06, 2016   18202          Robert.Blum    Changes for operational mode.
#    Jun 09, 2016   17516          mduff          Added getCurrentEvents.
#    02/27/17       29138          Chris.Golden   Changed to work with new
#                                                 version of HazardEventManager.
#    03/16/17       29138          Chris.Golden   Added method to retrieve
#                                                 latest historical version.
#    02/23/18       28387          Chris.Golden   Use new method to get events
#                                                 by site ID and phenomenons.
#

import JUtil
from com.raytheon.uf.common.dataplugin.events.hazards.datastorage import HazardEventManager
from EventSet import EventSet

def getHazardEvent(eventId, practice):
    manager = HazardEventManager(practice)
    event = manager.getLatestByEventID(eventId, True)
    if event is None:
        return None
    return JUtil.javaObjToPyVal(event)

def getMostRecentHistoricalHazardEvent(eventId, practice):
    manager = HazardEventManager(practice)
    events = manager.getHistoryByEventID(eventId, False)
    if not events:
        return None
    return JUtil.javaObjToPyVal(events[-1])

def getHazardEventsBySite(siteID, practice, phenomenons=None):
    manager = HazardEventManager(practice)
    hazardEventMap = manager.getLatestByPhenomenonsAndSiteID(phenomenons, siteID, True)
    if ((hazardEventMap == None) or (hazardEventMap.size() == 0)):
        return []
    hazardEvents = []
    entrySet = hazardEventMap.entrySet()
    iterator = entrySet.iterator()
    while iterator.hasNext():
        item = iterator.next()
        hazardEvents.append(JUtil.javaObjToPyVal(item.getValue()))
    return hazardEvents

# Given the specified event set that holds current events provided to the
# caller, as well as the site identifier and the hazard mode, get the list
# of events including the ones passed in but augmented with any from the
# database, but filtering out ended and elapsed events before returning the
# list. Also filter out any events that do not match the specified
# phenomenons, if any are specified.
def getCurrentEvents(eventSet, phenomenons=None):
    
    # Get the site identifier, and determine whether or not practice mode is
    # in effect.
    siteID = eventSet.getAttributes().get('siteID')        
    caveMode = eventSet.getAttributes().get('runMode','PRACTICE').upper()
    practice = True
    if caveMode == 'OPERATIONAL':
        practice = False

    # Get the current events that were passed in.
    currentEvents = []
    for event in eventSet:
        currentEvents.append(event)

    # Get the events from the database for this site, and add any that are
    # not already in the passed-in event set to the list of current events.
    databaseEvents = getHazardEventsBySite(siteID, practice, phenomenons) 
    eventIDs = [event.getEventID() for event in currentEvents]
    for event in databaseEvents:
        if event.getEventID() not in eventIDs:
            currentEvents.append(event)
            eventIDs.append(event.getEventID())

    # Filter out elapsed and ended hazards.
    validEvents = []
    for event in currentEvents:
        if event.getStatus() != 'ELAPSED' and event.getStatus() != 'ENDED':
            validEvents.append(event)
    return validEvents
