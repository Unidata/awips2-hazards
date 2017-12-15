# #
# This software was developed and / or modified by the
# National Oceanic and Atmospheric Administration (NOAA), 
# Earth System Research Laboratory (ESRL), 
# Global Systems Division (GSD), 
# Evaluation & Decision Support Branch (EDS)
# 
# Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
# #


#
# A readable hazard event, based on Event, which allows the querying of various
# properties of a hazard event. Subclasses may provide the ability to change
# said properties.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    12/14/17        20739         Chris.Golden   Initial creation.
#

import JUtil, datetime

from Event import Event

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

from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants
ProductClass = HazardConstants.ProductClass
HazardStatus = HazardConstants.HazardStatus

class ReadableHazardEvent(Event, JUtil.JavaWrapperClass):
        
    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
        
    def getSiteID(self):
        return self.jobj.getSiteID()
    
    def getEventID(self):
        return self.jobj.getEventID()

    def getDisplayEventID(self):
        return self.jobj.getDisplayEventID()
    
    def getStatus(self):
        return self.getHazardStatus()
        
    def isVisibleInHistoryList(self):
        return self.jobj.isVisibleInHistoryList()

    def getHazardStatus(self):
        return self.jobj.getStatus().name()
            
    def getPhenomenon(self):
        return self.jobj.getPhenomenon()
        
    def getSignificance(self):
        return self.jobj.getSignificance()
        
    def getSubType(self):
        return self.jobj.getSubType()
    
    def getHazardType(self):
        return self.jobj.getHazardType()
    
    def getPhensig(self):
        return self.jobj.getPhensig()
    
    def getInsertTime(self):
        timestamp = self.jobj.getInsertTime()
        if timestamp is None:
            return None
        return datetime.datetime.utcfromtimestamp(timestamp.getTime() / 1000.0)
        
    def getCreationTime(self):
        return datetime.datetime.utcfromtimestamp(self.jobj.getCreationTime().getTime() / 1000.0)
    
    def getStartTime(self):
        return datetime.datetime.utcfromtimestamp(self.jobj.getStartTime().getTime() / 1000.0)
      
    def getEndTime(self):
        if self.jobj.getEndTime() is not None :
            return datetime.datetime.utcfromtimestamp(self.jobj.getEndTime().getTime() / 1000.0)
        else :
            return None

    def getUserName(self):
        return self.jobj.getWsId().getUserName()

    def getWorkStation(self):
        return self.jobj.getWsId().getHostName()

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

    def getVisualFeatures(self):
        
        # This may return a list, and not a VisualFeatures object (which is a
        # list subclass), so build one of the latter out of the former.
        visualFeatures = JUtil.javaObjToPyVal(self.jobj.getVisualFeatures())
        if visualFeatures is None:
            return VisualFeatures([])
        return VisualFeatures(visualFeatures)

    def getHazardMode(self):
        if self.jobj.getHazardMode() is not None :
            return self.jobj.getHazardMode().name()
        else :
            return None
        
    def getHazardAttributes(self):
        return JUtil.javaObjToPyVal(self.jobj.getHazardAttributes())
    
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
        elif lowerKey == 'starttime': 
            return self.getStartTime()
        elif lowerKey == 'endtime':
            return self.getEndTime()
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
