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

######
TABLEFILE = '/home/nathan.hardin/Desktop/snap.tbl'

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
            #if event.get("convectiveSigmetChangeType"):
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
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'Change Object Type Tool'