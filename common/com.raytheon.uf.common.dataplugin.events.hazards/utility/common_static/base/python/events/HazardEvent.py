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
#    01/22/13                      mnash          Initial Creation.
#    08/20/13        1360          blawrenc       Changed toStr() to __str__() for debugging
#    12/05/13        2527          bkowal         Removed obsolete conversion methods
# 
#

import JUtil, datetime
from shapely import wkt

from Event import Event

from java.util import Date
from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants, HazardConstants_ProductClass as ProductClass, HazardConstants_HazardStatus as HazardStatus


class HazardEvent(Event, JUtil.JavaWrapperClass):
        
    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
        self.toPythonObj(wrappedObject)
        
    def getSiteID(self):
        return self.jobj.getSiteID()

    def setSiteID(self, site):
        self.jobj.setSiteID(site)
    
    def getEventID(self):
        return self.jobj.getEventID()
    
    def setEventID(self, eventId):
        self.jobj.setEventID(eventId)
    
    def getStatus(self):
        return self.getHazardStatus()
    
    def setStatus(self, hazardStatus):
        self.setHazardStatus(hazardStatus)

    def getHazardStatus(self):
        return self.jobj.getStatus().name()
    
    def setHazardStatus(self, hazardStatus):
        if hazardStatus is not None :
            self.jobj.setStatus(HazardStatus.valueOf(str(hazardStatus).upper()))
        else :
            self.jobj.setStatus(HazardStatus.PENDING)
            
    def getPhenomenon(self):
        return self.jobj.getPhenomenon()
    
    def setPhenomenon(self, phenomenon):
        self.jobj.setPhenomenon(phenomenon)
        
    def getSignificance(self):
        return self.jobj.getSignificance()
    
    def setSignificance(self, significance):
        self.jobj.setSignificance(significance)
        
    def getSubType(self):
        return self.jobj.getSubType()
    
    def setSubType(self, subtype):
        self.jobj.setSubType(subtype)
        
    def getHazardType(self):
        return self.jobj.getHazardType()
        
    def getCreationTime(self):
        return datetime.datetime.fromtimestamp(self.jobj.getCreationTime().getTime() / 1000.0)
    
    def setCreationTime(self, creationTime):
        dt = Date(long(self._getMillis(creationTime)))
        self.jobj.setCreationTime(dt)
      
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
        return JUtil.javaObjToPyVal(self.jobj.getGeometry())
    
    def setGeometry(self, geometry):
        if geometry is not None :
            self.jobj.setGeometry(JUtil.pyValToJavaObj(geometry))
    
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
        return JUtil.javaObjToPyVal(self.jobj.getHazardAttributes())
    
    def setHazardAttributes(self, hazardAttributes):
        self.jobj.setHazardAttributes(JUtil.pyValToJavaObj(hazardAttributes))
        
    def addHazardAttribute(self, key, value):
        self.jobj.addHazardAttribute(key, JUtil.pyValToJavaObj(value))
        
    def removeHazardAttribute(self, key):
        self.jobj.removeHazardAttribute(key)

    def get(self, key, default=None):
        '''
         Get the value of the hazard attribute with given key.
         '''
        value = JUtil.javaObjToPyVal(self.jobj.getHazardAttribute(key))
        if not value:
            value = default
        return value
    
    def set(self, key, value):
        '''
        Set the hazard attribute with given key to given value.
        '''
        self.jobj.addHazardAttribute(key, JUtil.pyValToJavaObj(value))

    def addToList(self, key, value):
        '''
        The equivalent of hazardAttributes.setdefault(key, []).append(value)
        '''
        currentVal = JUtil.javaObjToPyVal(self.jobj.getHazardAttribute(key))
        if currentVal:
            currentVal.append(value)
        else:
            currentVal = [value]
        self.jobj.addHazardAttribute(key, JUtil.pyValToJavaObj(currentVal))
               
    def _getMillis(self, date):
        epoch = datetime.datetime.utcfromtimestamp(0)
        delta = date - epoch
        return delta.total_seconds() * 1000.0
    
    def toPythonObj(self, javaClass):
        '''
        @summary: Converts a Java HazardEvent to the corresponding Python version
        '''  
        self.setSiteID(javaClass.getSiteID())
        if javaClass.getStatus() is not None:    
            self.setHazardStatus(javaClass.getStatus().name())
        else :
            self.setHazardStatus(HazardStatus.PENDING)
        self.setPhenomenon(javaClass.getPhenomenon())
        self.setSignificance(javaClass.getSignificance())
        self.setSubType(javaClass.getSubType())
        if javaClass.getCreationTime() is not None:
            self.setCreationTime(datetime.datetime.fromtimestamp(javaClass.getCreationTime().getTime() / 1000.0))
        else :
            self.setCreationTime(datetime.datetime.now())
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
            self.setGeometry(wkt.loads(javaClass.getGeometry().toText()))
        else :
            self.setGeometry(None)
        self.setHazardAttributes(JUtil.javaObjToPyVal(javaClass.getHazardAttributes()))
    
    def __getitem__(self, key):
        lowerKey = key.lower()
        if lowerKey == 'site':
            return getSiteID()
        elif lowerKey == 'status':
            return self.getStatus()
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            return self.getPhenomenon()
        elif lowerKey == 'significance' or lowerKey == 'sig':
            return self.getSignificance()
        elif lowerKey == 'subtype':
            return self.getSubType()
        elif lowerKey == 'creationtime':
            return self.getCreationTime()
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
        elif lowerKey == 'status':
            self.setStatus(value)
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            self.setPhenomenon(value)
        elif lowerKey == 'significance' or lowerKey == 'sig':
            self.setSignificance(value)
        elif lowerKey == 'subtype':
            self.setSubType(value)
        elif lowerKey == 'creationtime':
            self.setCreationTime(value)
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
        string = 'HazardEvent: ' + self.jobj.toString() + \
            '\ngeometry: ' + str(self.jobj.getGeometry())
        return string
    
    def toJavaObj(self):
        return self.jobj
