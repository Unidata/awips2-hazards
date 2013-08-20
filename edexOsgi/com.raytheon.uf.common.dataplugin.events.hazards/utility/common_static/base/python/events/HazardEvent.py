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
#    08/20/13        1360          blawrenc    Changed toStr() to __str__() for debugging
# 
#

import JUtil, datetime
import shapely

from Event import Event

from java.util import Date
from com.vividsolutions.jts.io import WKTReader
from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants, HazardConstants_ProductClass as ProductClass, HazardConstants_HazardState as HazardState


class HazardEvent(Event, JUtil.JavaWrapperClass):
        
    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
        self.reader = WKTReader()
        self.toPythonObj(wrappedObject)
        
    def getSiteID(self):
        return self.jobj.getSiteID()

    def setSiteID(self, site):
        self.jobj.setSiteID(site)
    
    def getEventID(self):
        return self.jobj.getEventID()
    
    def setEventID(self, eventId):
        self.jobj.setEventID(eventId)
    
    def getHazardState(self):
        return self.jobj.getState().name()
    
    def setHazardState(self, hazardState):
        if hazardState is not None :
            self.jobj.setState(HazardState.valueOf(str(hazardState).upper()))
        else :
            self.jobj.setState(HazardState.PENDING)
            
    def getPhenomemon(self):
        return self.jobj.getPhenomenon()
    
    def setPhenomenon(self, phenomenon):
        self.jobj.setPhenomenon(phenomenon)
        
    def getSignificance(self):
        return self.jobj.getSignificance()
    
    def setSignificance(self, significance):
        self.jobj.setSignificance(significance)
        
    def getSubtype(self):
        return self.jobj.getSubtype()
    
    def setSubtype(self, subtype):
        self.jobj.setSubtype(subtype)
        
    def getIssueTime(self):
        return datetime.datetime.fromtimestamp(self.jobj.getIssueTime().getTime() / 1000.0)
    
    def setIssueTime(self, issueTime):
        dt = Date(long(self._getMillis(issueTime)))
        self.jobj.setIssueTime(dt)
      
    def getEndTime(self):
        if self.jobj.getEndTime() is not None :
            return datetime.datetime.fromtimestamp(self.jobj.getEndTime().getTime() / 1000.0)
        else :
            return None
          
    def setEndTime(self, endTime):
        dt = Date(long(self._getMillis(endTime)))
        self.jobj.setEndTime(dt)
    
    def getStartTime(self):
        return datetime.datetime.fromtimestamp(self.jobj.getStartTime().getTime() / 1000.0)
    
    def setStartTime(self, startTime):
        dt = Date(long(self._getMillis(startTime)))
        self.jobj.setStartTime(dt)
    
    def getGeometry(self):
        return shapely.wkt.loads(self.jobj.getGeometry().asText())
    
    def setGeometry(self, geometry):
        if geometry is not None :
            self.jobj.setGeometry(self.reader.read(geometry.wkt))
    
    def getHazardMode(self):
        if self.jobj.getHazardMode() is not None :
            return self.jobj.getHazardMode().name()
        else :
            return None
    
    def setHazardMode(self, hazardMode):
        try :
            self.jobj.setHazardMode(HazardConstants.productClassFromAbbreviation(hazardMode))
        except Exception, e :
            try :
                self.jobj.setHazardMode(HazardConstants.productClassFromName(hazardMode))
            except Exception, e:
                self.jobj.setHazardMode(ProductClass.TEST)        
        
    def getHazardAttributes(self):
        return JUtil.javaMapToPyDict(self.jobj.getHazardAttributes())
    
    def setHazardAttributes(self, hazardAttributes):
        self.jobj.setHazardAttributes(JUtil.pyDictToJavaMap(hazardAttributes))
    
    def _getMillis(self, date):
        epoch = datetime.datetime.utcfromtimestamp(0)
        delta = date - epoch
        return delta.total_seconds() * 1000.0
    
    def toPythonObj(self, javaClass):
        '''
        @summary: Converts a Java HazardEvent to the corresponding Python version
        '''  
        self.setSiteID(javaClass.getSiteID())
        if javaClass.getState() is not None:    
            self.setHazardState(javaClass.getState().name())
        else :
            self.setHazardState(HazardState.PENDING)
        self.setPhenomenon(javaClass.getPhenomenon())
        self.setSignificance(javaClass.getSignificance())
        self.setSubtype(javaClass.getSubtype())
        if javaClass.getIssueTime() is not None:
            self.setIssueTime(datetime.datetime.fromtimestamp(javaClass.getIssueTime().getTime() / 1000.0))
        else :
            self.setIssueTime(datetime.datetime.now())
        if javaClass.getStartTime() is not None:
            self.setStartTime(datetime.datetime.fromtimestamp(javaClass.getStartTime().getTime() / 1000.0))
        else:
            self.setStartTime(datetime.datetime.now())
        if javaClass.getEndTime() is not None:
            self.setEndTime(datetime.datetime.fromtimestamp(javaClass.getEndTime().getTime() / 1000.0))
        else:
            self.setEndTime(datetime.datetime.now())
        if javaClass.getHazardMode() is not None:
            self.setHazardMode(javaClass.getHazardMode().name())
        else :
            self.setHazardMode(None)
        if javaClass.getGeometry() is not None :
            self.setGeometry(shapely.wkt.loads(javaClass.getGeometry().toText()))
        else :
            self.setGeometry(None)
        self.setHazardAttributes(JUtil.javaMapToPyDict(javaClass.getHazardAttributes()))
    
    def __getitem__(self, key):
        lowerKey = key.lower()
        if lowerKey == 'site':
            return getSiteID()
        elif lowerKey == 'state':
            return self.getState()
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            return self.getPhenomenon()
        elif lowerKey == 'significance' or lowerKey == 'sig':
            return self.getSignificance()
        elif lowerKey == 'subtype':
            return self.getSubtype()
        elif lowerKey == 'issuetime':
            return self.getIssueTime()
        elif lowerKey == 'endtime':
            return self.getEndTime()
        elif lowerKey == 'starttime': 
            return self.getStartTime()
        elif lowerKey == 'geometry' or lowerKey == 'geom':
            return self.getGeometry()
        elif lowerKey == 'mode' or lowerKey == 'hazardmode':
            return self.getHazardMode()
        elif lowerKey == 'eventid':
            return self.getEventID()
        elif lowerKey == 'attributes':
            return self.getHazardAttributes()
        else :
            raise TypeError()
        
    def __setitem__(self, key, value):
        lowerKey = key.lower()
        if lowerKey == 'site':
            self.setSiteID(value)
        elif lowerKey == 'state':
            self.setState(value)
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            self.setPhenomenon(value)
        elif lowerKey == 'significance' or lowerKey == 'sig':
            self.setSignificance(value)
        elif lowerKey == 'subtype':
            self.setSubtype(value)
        elif lowerKey == 'issuetime':
            self.setIssueTime(value)
        elif lowerKey == 'endtime':
            self.setEndTime(value)
        elif lowerKey == 'starttime': 
            self.setStartTime(value)
        elif lowerKey == 'geometry' or lowerKey == 'geom':
            self.setGeometry(value)
        elif lowerKey == 'mode' or lowerKey == 'hazardmode':
            self.setHazardMode(value)
        elif lowerKey == 'attributes':
            self.setHazardAttributes(value)         
    
    def __eq__(self, other):
        return self.jobj.equals(other.jobj)
    
    def __ne__(self, other):
        return not self.__eq__(other)
    
    def __str__(self):
        string = 'HazardEvent: ' + self.jobj.toString() +\
            '\ngeometry: ' + str(self.jobj.getGeometry())
        return string
    
    def toJavaObj(self):
        return self.jobj
    
def canConvert(javaClass):
    # the possible classes that can be converted to this class
    hazardEventClasses = ['com.raytheon.uf.common.dataplugin.events.hazards.event.PracticeHazardEvent', 'com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent', 'com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent']
    if javaClass.jclassname in hazardEventClasses:
        return True
    return False
    
def convert(javaClass):
    event = HazardEvent(javaClass)
    return event
