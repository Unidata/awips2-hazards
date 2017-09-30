'''
Create VA Layer Forecast Tool to copy metadata for tropical cyclone international SIGMET.
'''
import datetime, TimeUtils
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import time
import shapely
from shapely.geometry import Polygon
from inspect import currentframe, getframeinfo
import os, sys
from VisualFeatures import VisualFeatures
import AdvancedGeometry


class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('CreateVALayerForecastTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'CreateVALayerForecastTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Create VA Layer Forecast Tool'
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
        Runs the Create VA Layer Forecast Tool
        
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
        sys.stderr.write("Running Create VA Layer Forecast Tool.\n")

        sys.stderr.flush()
        
        
        for event in eventSet:
            if event.get("internationalSigmetPhenomenonComboBox") == "volcanicAsh":
                numLayers = event.get("internationalSigmetVALayersSpinner")
                self.addVAForecastVisualFeatures(event, numLayers)
            
        return eventSet
    
    def addVAForecastVisualFeatures(self, hazardEvent, numLayers):
        startTime = hazardEvent.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())
        
        vertices = hazardEvent.getFlattenedGeometry()
        vertices = shapely.geometry.base.dump_coords(vertices)
        vertices = vertices.pop()

        fcstVerticesDict = {}
        
        for i in range(0,numLayers):
            i = i+1
            newVerticesList = []
            for vertex in vertices:
                newVert = []
                for vert in vertex:
                    vert = vert - 1.5*i
                    newVert.append(vert)
                newVerticesList.append(newVert)
                newVerticesTuple = tuple(newVerticesList)
                fcstVerticesDict['vaFcstPoly'+str(i)] = newVerticesTuple
        
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
        
        for i in range(0, numLayers):
            poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(fcstVerticesDict['vaFcstPoly'+str(i+1)]), 0)
            featureName = 'vaFcstPoly'+str(i+1)            
            featureName = {
                "identifier": "vaFcstPoly"+str(i+1)+"_" + eventID,
                "dragCapability": "all",
                "borderColor": {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 },
                "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
                "label": "Forecast Layer" + str(i+1),
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
    return 'Create VA Layer Forecast Tool'