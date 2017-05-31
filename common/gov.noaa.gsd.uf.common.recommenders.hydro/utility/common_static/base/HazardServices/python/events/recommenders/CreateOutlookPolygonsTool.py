'''
CreateOutlookPolygonsTool to attributes from copied hazard event and create new event.
'''
import datetime, math, TimeUtils
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import time
import shapely
from shapely.geometry import Polygon
import os, sys
from VisualFeatures import VisualFeatures
import Domains
import AviationUtils
import json
import AdvancedGeometry

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('CreateOutlookPolygonsTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'UpdateTimesTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'CreateOutlookPolygonsTool'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['onlyIncludeTriggerEvents'] = True
        metadata['background'] = True
        
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: A dialog definition to solicit user input before running tool
        '''   
        return None
        
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        Runs the Create Outlook Polygons Tool
        
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
        sys.stderr.write("Running Create Outlook Polygons Tool.\n")

        sys.stderr.flush() 
        
        for event in eventSet:
            self.addOutlookPolygons(event)

        return eventSet
    
    def addOutlookPolygons(self, hazardEvent):
        startTime = hazardEvent.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())
         
        vertices = hazardEvent.getFlattenedGeometry()
        vertices = shapely.geometry.base.dump_coords(vertices)
        vertices = vertices.pop()
 
        fcstVerticesDict = {}
         
        for i in range(0,4):
            i = i+1
            newVerticesList = []
            for vertex in vertices:
                newVert = []
                for vert in vertex:
                    vert = vert - 1.5*i
                    newVert.append(vert)
                newVerticesList.append(newVert)
                newVerticesTuple = tuple(newVerticesList)
                fcstVerticesDict['T+'+str(i*3)+'hrs'] = newVerticesTuple
         
        for key, value in fcstVerticesDict.iteritems():
            hazardEvent.set(key, value)               
         
        eventID = hazardEvent.getEventID()
             
        selectedFeatures = []
         
        basePoly = hazardEvent.getGeometry()
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "always",
            "dragCapability": "all",
            "borderColor": "eventType",
            "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): basePoly
            }
        }
        selectedFeatures.append(basePoly) 
         
        for i in range(0, 4):
            poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(fcstVerticesDict['T+'+str((i+1)*3)+'hrs']), 0)
            featureName = 'T+'+str((i+1)*3)+'hrs'            
            featureName = {
                "identifier": 'T+'+str((i+1)*3)+'hrs' + eventID,
                "dragCapability": "all",
                "borderColor": {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 },
                "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
                "label": 'T+'+str((i+1)*3)+'hrs',
                "geometry": {
                    (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): poly
                }
            }                   
            selectedFeatures.append(featureName)
         
        hazardEvent.setVisualFeatures(VisualFeatures(selectedFeatures))    
        
        return True                  
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'CreateOutlookPolygonsTool'