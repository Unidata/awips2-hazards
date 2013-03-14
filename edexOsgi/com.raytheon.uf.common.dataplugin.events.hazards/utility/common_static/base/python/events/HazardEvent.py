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
# A HazardEvent, based on Event, which returns information of hazardous activity
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

import JUtil, datetime

from Event import Event

from java.util import Date
from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants

class HazardEvent(Event, JUtil.JavaWrapperClass):
        
    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
        self.site = None
        self.eventId = None
        self.hazardState = None
        self.issueTime = None
        self.startTime = None
        self.endTime = None
        self.phenomenon = None
        self.significance = None
        self.subtype = None
        self.hazardAttributes = None
        self.hazardMode = None
        self.geometry = None
    
    def getSiteID(self):
        return self.site

    def setSiteID(self, site):
        self.site = site
    
    def getEventID(self):
        return self.eventId
    
    def setEventID(self, eventId):
        self.eventId = eventId
        
    def getHazardState(self):
        return self.hazardState
    
    def setHazardState(self, hazardState):
        self.hazardState = hazardState
        
    def getPhenomemon(self):
        return self.phenomenon
    
    def setPhenomenon(self, phenomenon):
        self.phenomenon = phenomenon
        
    def getSignificance(self):
        return self.significance
    
    def setSignificance(self, significance):
        self.significance = significance
        
    def getSubtype(self):
        return self.subtype
    
    def setSubtype(self, subtype):
        self.subtype = subtype
        
    def getIssueTime(self):
        return self.issueTime
    
    def setIssueTime(self, issueTime):
        dt = Date(long(self.getMillis(issueTime)))
        self.issueTime = dt
            
    def setEndTime(self, endTime):
        dt = Date(long(self.getMillis(endTime)))
        self.endTime = dt
    
    def setStartTime(self, startTime):
        dt = Date(long(self.getMillis(startTime)))
        self.startTime = dt
        
    def setGeometry(self, geometry):
        self.geometry = geometry
        
    def getHazardMode(self):
        return self.hazardMode
    
    def setHazardMode(self, hazardMode):
        self.hazardMode = hazardMode
        
    def getHazardAttributes(self):
        return self.hazardAttributes
    
    def setHazardAttributes(self, hazardAttributes):
        self.hazardAttributes = hazardAttributes
    
    def getMillis(self, date):
        epoch = datetime.datetime.utcfromtimestamp(0)
        delta = date - epoch
        return delta.total_seconds() * 1000
    
    def __getitem__(self, key):
        lowerKey = key.lower()
        if lowerKey == 'site':
            return getSiteID()
        elif lowerKey == 'state':
            return getState()
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            return getPhenomenon()
        elif lowerKey == 'significance' or lowerKey == 'sig':
            return getSignificance()
        elif lowerKey == 'subtype':
            return getSubtype()
        elif lowerKey == 'issuetime':
            return getIssueTime()
        elif lowerKey == 'endtime':
            return getEndTime()
        elif lowerKey == 'starttime': 
            return getStartTime()
        elif lowerKey == 'geometry' or lowerKey == 'geom':
            return getGeometry()
        elif lowerKey == 'mode' or lowerKey == 'hazardmode':
            return getHazardMode()
        elif lowerKey == 'eventid':
            return getEventID()
        elif lowerKey == 'attributes':
            return getHazardAttributes()
        else :
            raise TypeError()
        
    def __setitem__(self, key, value):
        lowerKey = key.lower()
        if lowerKey == 'site':
            setSiteID(value)
        elif lowerKey == 'state':
            setState(value)
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            setPhenomenon(value)
        elif lowerKey == 'significance' or lowerKey == 'sig':
            setSignificance(value)
        elif lowerKey == 'subtype':
            setSubtype(value)
        elif lowerKey == 'issuetime':
            setIssueTime(value)
        elif lowerKey == 'endtime':
            setEndTime(value)
        elif lowerKey == 'starttime': 
            setStartTime(value)
        elif lowerKey == 'geometry' or lowerKey == 'geom':
            setGeometry(value)
        elif lowerKey == 'mode' or lowerKey == 'hazardmode':
            setHazardMode(value)
        elif lowerKey == 'attributes':
            setHazardAttributes(value)         
    
    def __eq__(self, other):
        return jobj.equals(other.jobj)
    
    def __ne__(self, other):
        return not __eq__(other)
    
    def toJavaObj(self):
        self.jobj.setSiteID(self.site)
        self.jobj.setState(self.hazardState)
        self.jobj.setPhenomenon(self.phenomenon)
        self.jobj.setSignificance(self.significance)
        self.jobj.setSubtype(self.subtype)
        self.jobj.setIssueTime(self.issueTime)
        self.jobj.setEndTime(self.endTime)
        self.jobj.setStartTime(self.startTime)
        self.jobj.setHazardMode(self.hazardMode)
        if self.geometry is not None:
            self.jobj.setGeometry(self.geometry.wkt)
        else :
            self.jobj.setGeometry(None)
        self.jobj.setHazardAttributes(JUtil.pyDictToJavaMap(self.hazardAttributes))
        return self.jobj                                        
