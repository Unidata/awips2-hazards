'''
Change object Type Tool to change polygons to lines or vice versa.
'''
import datetime, math, TimeUtils
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import time
import shapely
from shapely.geometry import Polygon
from inspect import currentframe, getframeinfo
import os, sys
from VisualFeatures import VisualFeatures
import Domains
import AviationUtils
import AdvancedGeometry

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('ChangeObjectTypeTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'ChangeObjectTypeTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Change Object Type Tool'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['onlyIncludeTriggerEvents'] = True
        
        metadata["getDialogInfoNeeded"] = False
        metadata["getSpatialInfoNeeded"] = False
        
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: A dialog definition to solicit user input before running tool
        '''   
        return None
        
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        Runs the Change Object Type Tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param spatialInputMap:   A map of information retrieved
                                  from the user's interaction with the
                                  spatial display.
        
        @return: A list of potential probabilistic hazard events. 
        '''
        import sys
        sys.stderr.write("Running Change Object Type Tool.\n")

        sys.stderr.flush()
        
        for event in eventSet:
            self._originalGeomType = event.get('originalGeomType')
            geometry = event.getFlattenedGeometry()
            geometry = shapely.geometry.base.dump_coords(geometry)
            geometry = geometry[0]
            
            if self._originalGeomType == 'LineString':
                polygon = event.get('polygon')
                poly = self.lineToPolygon(polygon)
                event.set('originalGeomType','Polygon')
            else:
                poly = self.polygonToLine(geometry)
                event.set('originalGeomType','LineString')
                self._width = 10
                event.set('convectiveSigmetWidth',10)
            
            event.set('convectiveSigmetMetaData',True)    
            event.setGeometry(poly)    
            event.set('originalGeometry', poly)
            self._originalGeomType = event.get('originalGeomType')
            boundingStatement = AviationUtils.AviationUtils().boundingStatement(event,self._originalGeomType,poly,None)

        return eventSet
    
    def lineToPolygon(self, polygon):
        poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(polygon), 0)       
        
        return poly
    
    def polygonToLine(self, geometry):
        newGeometry = []
        
        for x in range(0,3,2):
            lat1 = geometry[x][1]
            lon1 = geometry[x][0]
            lat2 = geometry[x+1][1]
            lon2 = geometry[x+1][0]
            
            newLat = (lat1+lat2)/2
            newLon = (lon1+lon2)/2
            newVertex = (newLon, newLat)
            newGeometry.append(newVertex)
        
        poly = GeometryFactory.createLineString(newGeometry)
        poly = AdvancedGeometry.createShapelyWrapper(poly, 0)

        return poly
    def addVisualFeatures(self, event, poly):
        
        selectedFeatures = []
        
        features = event.getVisualFeatures()
        for feature in features:
            if 'Outlook' in feature['identifier']:
                selectedFeatures.append(feature)     
        
        startTime = event.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=1)
        endTime = TimeUtils.roundDatetime(event.getEndTime())
        eventID = event.getEventID()
        
        polygonArea = AviationUtils.AviationUtils().polygonArea(event, self._originalGeomType, self._width)
        label = AviationUtils.AviationUtils().createLabel(event, polygonArea)
        basePoly = event.getGeometry()
                  
        borderColor = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 }  #yellow  
        
        hazardEventPoly = {
            "identifier": "hazardEventPolygon_" + eventID,
            "visibilityConstraints": "selected",
            "borderColor": borderColor,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "always",
            "dragCapability": "all",
            "borderThickness": "eventType",
            "diameter": "eventType",
            "label": label,
            "borderColor": {"red": 255/255.0, "green": 255/255.0, "blue": 255/255.0, "alpha": 1}, #white
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): basePoly
            }
        }                    

        selectedFeatures.append(basePoly)                      
        selectedFeatures.append(hazardEventPoly)            
        event.setVisualFeatures(VisualFeatures(selectedFeatures))
        return True          
    
    def addPolygonVisualFeatures(self,hazardEvent):       
        selectedFeatures = []
        
        features = hazardEvent.getVisualFeatures()
        for feature in features:
            if 'Outlook' in feature['identifier']:
                selectedFeatures.append(feature)        
                
        startTime = hazardEvent.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())
        
        VOR_points = hazardEvent.getHazardAttributes().get('VOR_points')
        eventID = hazardEvent.getEventID()
        
        polygonArea = AviationUtils.AviationUtils().polygonArea(hazardEvent, self._originalGeomType, None)
        domain = hazardEvent.getHazardAttributes().get('convectiveSigmetDomain')
        direction = hazardEvent.getHazardAttributes().get('convectiveSigmetDirection')
        speed = hazardEvent.getHazardAttributes().get('convectiveSigmetSpeed')
        cloudTop = hazardEvent.getHazardAttributes().get('convectiveSigmetCloudTop')
        cloudTopText = hazardEvent.getHazardAttributes().get('convectiveSigmetCloudTopText')        
        
        status = hazardEvent.getStatus()
        if status == 'ISSUED':
            area = str(polygonArea) + " sq mi"
            numberStr = hazardEvent.getHazardAttributes().get('convectiveSigmetNumberStr')
            number = "\n" + numberStr + domain[0] + "\n"
        
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL " + str(cloudTopText)
            
            motion = "\n" + str(direction)+"@"+str(speed)+"kts"
            label = number + area + tops + motion
        else:
            area = str(polygonArea) + " sq mi"
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL " + str(cloudTopText)
            else:
                tops = "\nN/A"
            
            motion = "\n" + str(direction)+"@"+str(speed)+" kts"                        
            label = area + tops + motion
        
        poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(VOR_points), 0)
        
        basePoly = hazardEvent.getGeometry()
                
        fillColor = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 0.0 }
        borderColor = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 }
                    
        VORPoly = {
            "identifier": "VORPreview_" + eventID,
            "visibilityConstraints": "always",
            "borderColor": "eventType",
            "fillColor": fillColor,
            "label": label,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "selected",
            "dragCapability": "all",
            "borderColor": borderColor,
            "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): basePoly
            }
        }                    

        selectedFeatures.append(basePoly)
        selectedFeatures.append(VORPoly)
        
        hazardEvent.setVisualFeatures(VisualFeatures(selectedFeatures))    
        
        return True                
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'Change Object Type Tool'