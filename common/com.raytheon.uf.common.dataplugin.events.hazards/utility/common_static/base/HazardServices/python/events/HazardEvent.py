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
# A mutable hazard event, allowing the querying and changing of
# a hazard event's propertis.
#

import JUtil

from ReadableHazardEvent import ReadableHazardEvent

import TimeUtils

from shapely.geometry.base import BaseGeometry

import AdvancedGeometry

from VisualFeatures import VisualFeatures

from java.util import Date
from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants
HazardStatus = HazardConstants.HazardStatus

class HazardEvent(ReadableHazardEvent):

    def setSiteID(self, site):
        self.jobj.setSiteID(site)
    
    def setEventID(self, eventId):
        self.jobj.setEventID(eventId)
    
    def setStatus(self, hazardStatus):
        self.setHazardStatus(hazardStatus)
    
    def setVisibleInHistoryList(self, visible):
        self.jobj.setVisibleInHistoryList(visible)
    
    def setHazardStatus(self, hazardStatus):
        if hazardStatus is not None :
            self.jobj.setStatus(HazardStatus.valueOf(str(hazardStatus).upper()))
        else :
            self.jobj.setStatus(HazardStatus.PENDING)
    
    def setIssuanceCount(self, issuanceCount):
        self.jobj.setIssuanceCount(issuanceCount)
    
    def setPhenomenon(self, phenomenon):
        self.jobj.setPhenomenon(phenomenon)
    
    def setSignificance(self, significance):
        self.jobj.setSignificance(significance)
    
    def setSubType(self, subtype):
        self.jobj.setSubType(subtype)
    
    def setStartTime(self, startTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(startTime)))
        self.jobj.setStartTime(dt)
          
    def setEndTime(self, endTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(endTime)))
        self.jobj.setEndTime(dt)
    
    def setCreationTime(self, creationTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(creationTime)))
        self.jobj.setCreationTime(dt)

    def setExpirationTime(self, expirationTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(expirationTime)))
        self.jobj.setExpirationTime(dt)

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
    
    def setVisualFeatures(self, visualFeatures):
        self.jobj.setVisualFeatures(JUtil.pyValToJavaObj(visualFeatures))
    
    def setHazardAttributes(self, hazardAttributes):
        self.jobj.setHazardAttributes(JUtil.pyValToJavaObj(hazardAttributes))
        
    def addHazardAttribute(self, key, value):
        self.jobj.addHazardAttribute(key, JUtil.pyValToJavaObj(value))
    
    def addHazardAttributes(self, hazardAttributes):
        self.jobj.addHazardAttributes(JUtil.pyValToJavaObj(hazardAttributes))
        
    def removeHazardAttribute(self, key):
        self.jobj.removeHazardAttribute(key)
    
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
        
    def __setitem__(self, key, value):
        lowerKey = key.lower()
        if lowerKey == 'site':
            self.setSiteID(value)
        elif lowerKey == 'status':
            self.setStatus(value)
        elif lowerKey == 'issuancecount':
            self.setIssuanceCount(value)
        elif lowerKey == 'visibleinhistorylist':
            self.setVisibleInHistoryList(value)
        elif lowerKey == 'phenomenon' or lowerKey == 'phen':
            self.setPhenomenon(value)
        elif lowerKey == 'significance' or lowerKey == 'sig':
            self.setSignificance(value)
        elif lowerKey == 'subtype':
            self.setSubType(value)
        elif lowerKey == 'starttime': 
            self.setStartTime(value)
        elif lowerKey == 'endtime':
            self.setEndTime(value)
        elif lowerKey == 'creationtime':
            self.setCreationTime(value)
        elif lowerKey == 'expirationtime':
            self.setExpirationTime(value)
        elif lowerKey == 'geometry' or lowerKey == 'geom':
            self.setGeometry(value)
        elif lowerKey == 'attributes':
            self.setHazardAttributes(value)         
