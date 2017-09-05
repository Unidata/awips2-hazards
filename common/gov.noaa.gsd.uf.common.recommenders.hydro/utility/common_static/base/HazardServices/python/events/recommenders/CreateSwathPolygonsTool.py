'''
CreateSwathPolygonsTool to attributes from copied hazard event and create new event.
'''
import datetime, math, TimeUtils
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import time
import shapely
from shapely.geometry import Polygon
from shapely.ops import cascaded_union
import os, sys
from VisualFeatures import VisualFeatures
import Domains
import AviationUtils
import json
import AdvancedGeometry

######
TABLEFILE = AviationUtils.AviationUtils().snapTblFilePath()

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('CreateSwathPolygonsTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'CreateSwathPolygonsTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'CreateSwathPolygonsTool'
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
        Runs the Create Swath Polygons Tool
        
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
        sys.stderr.write("Running Create Swath Polygons Tool.\n")

        sys.stderr.flush() 
        
        for event in eventSet:
            self.createSwathPolygons(event)

        return eventSet
    
    def createSwathPolygons(self, hazardEvent):
        visualFeatureGeomDict = {}
        features = hazardEvent.getVisualFeatures()
        if not features:
            feature = []
            
        for feature in features:
            featureIdentifier = feature.get('identifier')
            polyDict = feature['geometry']
            for timeBounds, geometry in polyDict.iteritems():
                featurePoly = geometry.asShapely()
                vertices = shapely.geometry.base.dump_coords(featurePoly)
                visualFeatureGeomDict[featureIdentifier] = vertices[0]
        
        currentSwathList = []
        fcstSwathList = []
        
        for key, vertices in visualFeatureGeomDict.iteritems():
            if ('current' in key) or ('fcst' in key):
                pass
            elif ('base' in key) or ('3hrs' in key):
                currentSwathList.append(vertices)
            elif '6hrs' in key:
                currentSwathList.append(vertices)
                fcstSwathList.append(vertices)
            else:
                fcstSwathList.append(vertices)
        
        currentSwathPoly = self.createCascadedUnion(currentSwathList)
        fcstSwathPoly = self.createCascadedUnion(fcstSwathList)
        
        hazardEvent.set('currentSwathPoly', currentSwathPoly)
        hazardEvent.set('fcstSwathPoly', fcstSwathPoly)
        
        currentBoundingStatement = AviationUtils.AviationUtils().boundingStatement(hazardEvent,'Polygon',TABLEFILE,currentSwathPoly,'swathGeneration')
        hazardEvent.set('currentBoundingStatement', currentBoundingStatement)
        outlookBoundingStatement = AviationUtils.AviationUtils().boundingStatement(hazardEvent,'Polygon',TABLEFILE,fcstSwathPoly,'swathGeneration')
        hazardEvent.set('outlookBoundingStatement', outlookBoundingStatement)
        
        hazardEvent.setGeometry(currentSwathPoly)
        self.createSwathVisualFeatures(hazardEvent, currentSwathPoly, fcstSwathPoly, visualFeatureGeomDict)
        
        return 
    
    def getStatesList(self, hazardEvent):
        self._hazardZonesDict = hazardEvent.getHazardAttributes().get('hazardArea')
                   
        statesList = []
        for key in self._hazardZonesDict:
            states = key[:2]
            if states in statesList:
                pass
            else:
                statesList.append(states)
        statesListStr = ''
        for states in statesList:
            statesListStr += states + ' '
            
        hazardEvent.set('statesList', statesListStr)
            
        return          
        
    def createCascadedUnion(self, verticesList):
        cascadedUnionPoly = cascaded_union([Polygon(verticesList[0]),
                                            Polygon(verticesList[1]),
                                            Polygon(verticesList[2])])
        
        return cascadedUnionPoly
    
    def createSwathVisualFeatures(self, hazardEvent, currentSwathPoly, fcstSwathPoly, visualFeatureGeomDict):
        startTime = hazardEvent.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())        
        
        eventID = hazardEvent.getEventID()
        
        selectedFeatures = self.addPreviousVisualFeatures(hazardEvent, visualFeatureGeomDict)
        
        currentSwathPoly = AdvancedGeometry.createShapelyWrapper(currentSwathPoly, 0)
        fcstSwathPoly = AdvancedGeometry.createShapelyWrapper(fcstSwathPoly, 0)
        
        currentSwathPoly = {
            "identifier": "currentSwathPoly_" + eventID,
            "visibilityConstraints": "selected",
            "diameter": "eventType",
            "borderColor": {"red": 128 / 255.0, "green": 0 / 255.0, "blue": 128 / 255.0, "alpha": 1.0 },
            "fillColor": {"red": 128 / 255.0, "green": 0 / 255.0, "blue": 128 / 255.0, "alpha": 0.4 },
            "label": "Current Swath",
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): currentSwathPoly
            }
        }        
        
        fcstSwathPoly = {
            "identifier": "fcstSwathPoly_" + eventID,
            "visibilityConstraints": "selected",
            "diameter": "eventType",
            "borderColor": {"red": 255 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 },
            "fillColor": {"red": 255 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 0.0 },
            "label": "Forecast Swath",
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): fcstSwathPoly
            }
        }        
        
        selectedFeatures.append(currentSwathPoly)
        selectedFeatures.append(fcstSwathPoly)
        hazardEvent.setVisualFeatures(VisualFeatures(selectedFeatures))
        
        return True   
        
    def addPreviousVisualFeatures(self, hazardEvent, visualFeatureGeomDict):
        selectedFeatures = []
                 
        features = hazardEvent.getVisualFeatures()
        if not features:
            feature = []
             
        for feature in features:
            featureIdentifier = feature.get('identifier')
            if "current" in featureIdentifier or "fcst" in featureIdentifier:
                pass
            else:
                selectedFeatures.append(feature)  
        
        return selectedFeatures                  
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'CreateSwathPolygonsTool'