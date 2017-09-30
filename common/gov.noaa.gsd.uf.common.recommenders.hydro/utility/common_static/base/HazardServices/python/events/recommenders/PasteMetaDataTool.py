'''
PasteMetaDataTool to attributes from copied hazard event and create new event.
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
import json
import AdvancedGeometry

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('PasteMetaDataTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'UpdateTimesTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'PasteMetaDataTool'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['includeEventTypes'] = [ "SIGMET.Convective" ]
        
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
        Runs the Paste MetaData Tool
        
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
        sys.stderr.write("Running Paste MetaData Tool.\n")

        sys.stderr.flush() 
        
        with open('/scratch/copyBuffer.txt') as openFile:
                eventDict = json.load(openFile)        
        
        currentTime = datetime.datetime.now()
        startTime = eventDict['startTime']
        startTime = datetime.datetime.strptime(startTime, '%Y-%m-%d %H:%M:%S')
        endTime = eventDict['endTime']
        endTime = datetime.datetime.strptime(endTime, '%Y-%m-%d %H:%M:%S')
        
        hazardEvent = EventFactory.createEvent()
        hazardEvent.setCreationTime(currentTime)
        hazardEvent.setStartTime(startTime)
        hazardEvent.setEndTime(endTime)
        hazardEvent.setHazardStatus("pending")
        hazardEvent.setHazardMode("O")
        hazardEvent.setPhenomenon("SIGMET")
        hazardEvent.setSignificance("Convective")
        hazardEvent.set('originalGeomType', eventDict['geomType'])
        
        for key in eventDict:
            hazardEvent.set(str(key), eventDict.get(key))
            
        hazardEvent.set('generated', False)
        
        self._geomType = eventDict['geomType']
        vertices = eventDict['geometry']
        vertices = vertices.pop()
        
        newVerticesList = []
        for vertex in vertices:
            newVert = []
            for vert in vertex:
                vert = vert - 1.5
                newVert.append(vert)
            newVerticesList.append(newVert)
        
        if self._geomType == 'Polygon':
            poly = GeometryFactory.createPolygon(newVerticesList)
        elif self._geomType == 'LineString':
            poly = GeometryFactory.createLineString(newVerticesList)
        elif self._geomType == 'Point':
            newVerticesList = newVerticesList[0]
            poly = GeometryFactory.createPoint(newVerticesList)
            
        poly = AdvancedGeometry.createShapelyWrapper(poly, 0)
            
        hazardEvent.setGeometry(poly)
        
        eventSet.add(hazardEvent) 

        return eventSet    
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'PasteMetaDataTool'