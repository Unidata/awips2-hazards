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
#    05/13/15        8161          mduff          Changes for Jep upgrade.
#    05/26/15        8112          Chris.Cody     Add get handling for 0 values
#    07/31/15        7458          Robert.Blum    Added username and workstation.
#    08/03/15        8836          Chris.Cody     Changes for a configurable Event Id
#    03/01/16       15676          Chris.Golden   Added visual features to hazard event.
#    04/05/16       15676          Chris.Golden   Ensured that the getXxxxVisualFeatures()
#                                                 methods never return None; they now always
#                                                 return a VisualFeatures (list) object,
#                                                 empty or otherwise.
#    06/10/16       19537          Chris.Golden   Combined base and selected visual feature
#                                                 lists for each hazard event into one,
#                                                 replaced by visibility constraints
#                                                 based upon selection state to individual
#                                                 visual features.
#    06/23/16       19537          Chris.Golden   Changed to use new TimeUtils methods, and
#                                                 to use UTC when converting from an epoch
#                                                 time to a datetime. Also added ability to
#                                                 set product geometry, to mirror earlier
#                                                 change in IHazardEvent.java.
#    09/02/16       15934          Chris.Golden   Changed to include methods handling
#                                                 advanced geometries in place of JTS
#                                                 and shapely geometries.
#    02/01/17       15556          Chris.Golden   Added visible-in-history-list flag. Also
#                                                 added insert time record and getter.
#    05/24/17       15561          Chris.Golden   Added getPhensig() method.
#

import JUtil, datetime
from shapely import wkt

from Event import Event

import TimeUtils

from shapely.geometry.base import BaseGeometry

import AdvancedGeometry

from AdvancedGeometryHandler import pyAdvancedGeometryToJavaAdvancedGeometry, javaAdvancedGeometryToPyAdvancedGeometry
JUtil.registerPythonToJava(pyAdvancedGeometryToJavaAdvancedGeometry)
JUtil.registerJavaToPython(javaAdvancedGeometryToPyAdvancedGeometry)

from VisualFeatures import VisualFeatures
from VisualFeaturesHandler import pyVisualFeaturesToJavaVisualFeatures, javaVisualFeaturesToPyVisualFeatures

# This is a kludge. It turns out that (at least with the Java-object-to-Python-value
# conversions) the Java objects of type VisualFeaturesList are converted as standard
# lists, which means that their elements (VisualFeature instances) are not converted
# at all. The reason is that the Java-to-Python conversion handlers are called in the
# order they are registered, and thus the handler provided here for VisualFeaturesList
# objects is never used. For now, since JUtil cannot be changed as it's part of the
# AWIPS2 baseline, the (ugly) solution is to directly access its lists of handlers and
# prepend the visual feature conversion handlers to them.
#
# TODO: Come up with something better! Perhaps get rid of the VisualFeaturesList and
# just use a standard list, and then register handlers to convert to and from Java
# VisualFeature objects. 
JUtil.pythonHandlers.insert(0, pyVisualFeaturesToJavaVisualFeatures)
JUtil.javaHandlers.insert(0, javaVisualFeaturesToPyVisualFeatures)

from java.util import Date
from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants
ProductClass = HazardConstants.ProductClass
HazardStatus = HazardConstants.HazardStatus

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

    def getDisplayEventID(self):
        return self.jobj.getDisplayEventID()
    
    def getStatus(self):
        return self.getHazardStatus()
    
    def setStatus(self, hazardStatus):
        self.setHazardStatus(hazardStatus)
        
    def isVisibleInHistoryList(self):
        return self.jobj.isVisibleInHistoryList()
    
    def setVisibleInHistoryList(self, visible):
        self.jobj.setVisibleInHistoryList(visible)

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
    
    def getPhensig(self):
        return self.jobj.getPhensig()
    
    def getInsertTime(self):
        '''
        @summary Get the time at which the hazard event was last persisted to the database.
        Note that no corresponding setInsertTime() is provided, since this is not something
        that should be set within the context of a recommender, product generator, etc.
        @return Persist time, or None if the hazard event has not been persisted.
        '''
        timestamp = self.jobj.getInsertTime()
        if timestamp is None:
            return None
        return datetime.datetime.utcfromtimestamp(timestamp.getTime() / 1000.0)
        
    def getCreationTime(self):
        return datetime.datetime.utcfromtimestamp(self.jobj.getCreationTime().getTime() / 1000.0)
    
    def setCreationTime(self, creationTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(creationTime)))
        self.jobj.setCreationTime(dt)
      
    def getEndTime(self):
        if self.jobj.getEndTime() is not None :
            return datetime.datetime.utcfromtimestamp(self.jobj.getEndTime().getTime() / 1000.0)
        else :
            return None
          
    def setEndTime(self, endTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(endTime)))
        self.jobj.setEndTime(dt)
    
    def getStartTime(self):
        return datetime.datetime.utcfromtimestamp(self.jobj.getStartTime().getTime() / 1000.0)
    
    def setStartTime(self, startTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(startTime)))
        self.jobj.setStartTime(dt)

    def getUserName(self):
        return self.jobj.getUserName()
    
    def setUserName(self, userName):
        self.jobj.setUserName(userName)

    def getWorkStation(self):
        return self.jobj.getWorkStation()
    
    def setWorkStation(self, workStation):
        self.jobj.setWorkStation(workStation)

    # Get the geometry. The returned geometry will be of type
    # AdvancedGeometry.
    def getGeometry(self):
        return JUtil.javaObjToPyVal(self.jobj.getGeometry())

    # Get the flattened geometry, a shapely version of the geometry
    # that replaces any curves with simulated curves made up of
    # straight line segments.
    def getFlattenedGeometry(self):
        return JUtil.javaObjToPyVal(self.jobj.getFlattenedGeometry())
    
    def getProductGeometry(self):
        return JUtil.javaObjToPyVal(self.jobj.getProductGeometry())

    # Set the geometry to that specified. Note that the parameter
    # may either be a Java subclass of IAdvancedGeometry, a
    # Python AdvancedGeometry. or a Python shapely geometry.
    def setGeometry(self, geometry):
        
        # Convert any shapely geometries to AdvancedGeometry.
        if issubclass(type(geometry), BaseGeometry):
            geometry = AdvancedGeometry.createShapelyWrapper(geometry, 0)
            
        # Use the advanced geometry's wrapped Java object.
        if isinstance(geometry, AdvancedGeometry.AdvancedGeometry):
            geometry = geometry.getWrappedJavaObject()
        self.jobj.setGeometry(geometry)
    
    def setProductGeometry(self, geometry):
        if geometry is not None :
            self.jobj.setProductGeometry(JUtil.pyValToJavaObj(geometry))

    def getVisualFeatures(self):
        
        # This may return a list, and not a VisualFeatures object (which is a
        # list subclass), so build one of the latter out of the former.
        visualFeatures = JUtil.javaObjToPyVal(self.jobj.getVisualFeatures())
        if visualFeatures is None:
            return VisualFeatures([])
        return VisualFeatures(visualFeatures)
    
    def setVisualFeatures(self, visualFeatures):
        self.jobj.setVisualFeatures(JUtil.pyValToJavaObj(visualFeatures))

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
         Note that a return of 0 will be evaluated as None.
         '''
        value = JUtil.javaObjToPyVal(self.jobj.getHazardAttribute(key))
        if isinstance(value,int) and value == 0:
            return int(0)
        if isinstance(value,long) and value == 0:
            return long(0)
        if isinstance(value,float) and value == 0:
            return float(0.0)
        
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
        self.setUserName(javaClass.getUserName())
        self.setWorkStation(javaClass.getWorkStation())
        if javaClass.getCreationTime() is not None:
            self.setCreationTime(datetime.datetime.utcfromtimestamp(javaClass.getCreationTime().getTime() / 1000.0))
        else :
            self.setCreationTime(datetime.datetime.now())
        if javaClass.getStartTime() is not None:
            self.setStartTime(datetime.datetime.utcfromtimestamp(javaClass.getStartTime().getTime() / 1000.0))
        else:
            self.setStartTime(datetime.datetime.now())
        if javaClass.getEndTime() is not None:
            self.setEndTime(datetime.datetime.utcfromtimestamp(javaClass.getEndTime().getTime() / 1000.0))
        else:
            self.setEndTime(datetime.datetime.now())
        if javaClass.getHazardMode() is not None:
            self.setHazardMode(javaClass.getHazardMode().name())
        else :
            self.setHazardMode(None)
        self.setGeometry(javaClass.getGeometry())
        if javaClass.getProductGeometry() is not None :
            self.setProductGeometry(wkt.loads(javaClass.getProductGeometry().toText()))
        else :
            self.setProductGeometry(None)
        self.setHazardAttributes(JUtil.javaObjToPyVal(javaClass.getHazardAttributes()))
    
    def __getitem__(self, key):
        lowerKey = key.lower()
        if lowerKey == 'site':
            return self.getSiteID()
        elif lowerKey == 'status':
            return self.getStatus()
        elif lowerKey == 'visibleinhistorylist':
            return self.isVisibleInHistoryList()
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            return self.getPhenomenon()
        elif lowerKey == 'significance' or lowerKey == 'sig':
            return self.getSignificance()
        elif lowerKey == 'subtype':
            return self.getSubType()
        elif lowerKey == 'phensig':
            return self.getPhensig()
        elif lowerKey == 'creationtime':
            return self.getCreationTime()
        elif lowerKey == 'inserttime':
            return self.getInsertTime()
        elif lowerKey == 'endtime':
            return self.getEndTime()
        elif lowerKey == 'starttime': 
            return self.getStartTime()
        elif lowerKey == 'geometry' or lowerKey == 'geom':
            return self.getGeometry()
        elif lowerKey == 'flattenedgeometry':
            return self.getFlattenedGeometry()
        elif lowerKey == 'mode' or lowerKey == 'hazardmode':
            return self.getHazardMode()
        elif lowerKey == 'eventid':
            return self.getEventID()
        elif lowerKey == 'username':
            return self.getUserName()
        elif lowerKey == 'workstation':
            return self.getWorkStation()
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
        elif lowerKey == 'visibleinhistorylist':
            self.setVisibleInHistoryList(value)
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
        elif lowerKey == 'username':
            return self.setUserName(value)
        elif lowerKey == 'workstation':
            return self.setWorkStation(value)
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