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
#    12/14/17       20739          Chris.Golden   Made subclass of ReadableHazardEvent.
#    01/26/18       33428          Chris.Golden   Added issuance count.
#

import JUtil

from ReadableHazardEvent import ReadableHazardEvent

import TimeUtils

from shapely.geometry.base import BaseGeometry

import AdvancedGeometry

from VisualFeatures import VisualFeatures

from java.util import Date
from com.raytheon.uf.common.dataplugin.events.hazards import HazardConstants
ProductClass = HazardConstants.ProductClass
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
    
    def setCreationTime(self, creationTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(creationTime)))
        self.jobj.setCreationTime(dt)
    
    def setStartTime(self, startTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(startTime)))
        self.jobj.setStartTime(dt)
          
    def setEndTime(self, endTime):
        dt = Date(long(TimeUtils.datetimeToEpochTimeMillis(endTime)))
        self.jobj.setEndTime(dt)

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
    
    def setHazardMode(self, hazardMode):
        try :
            self.jobj.setHazardMode(HazardConstants.productClassFromAbbreviation(hazardMode))
        except Exception, e :
            try :
                self.jobj.setHazardMode(HazardConstants.productClassFromName(hazardMode))
            except Exception, e:
                self.jobj.setHazardMode(ProductClass.TEST)        
    
    def setHazardAttributes(self, hazardAttributes):
        self.jobj.setHazardAttributes(JUtil.pyValToJavaObj(hazardAttributes))
        
    def addHazardAttribute(self, key, value):
        self.jobj.addHazardAttribute(key, JUtil.pyValToJavaObj(value))
        
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
        elif lowerKey == 'creationtime':
            self.setCreationTime(value)
        elif lowerKey == 'starttime': 
            self.setStartTime(value)
        elif lowerKey == 'endtime':
            self.setEndTime(value)
        elif lowerKey == 'geometry' or lowerKey == 'geom':
            self.setGeometry(value)
        elif lowerKey == 'mode' or lowerKey == 'hazardmode':
            self.setHazardMode(value)
        elif lowerKey == 'attributes':
            self.setHazardAttributes(value)         
