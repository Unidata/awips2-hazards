'''
CopyMetaDataTool to copy attributes of selected hazard events.
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

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('CopyMetaDataTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'UpdateTimesTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'CopyMetaDataTool'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['includeEventTypes'] = [ "SIGMET.Convective" ]
        
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: A dialog definition to solicit user input before running tool
        '''   
        return None
        
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        Runs the Copy MetaData Tool
        
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
        sys.stderr.write("Running Copy MetaData Tool.\n")

        sys.stderr.flush()

        for event in eventSet:
            if event.get('selected') == True:
                geometry = event.getFlattenedGeometry()
                geometry = shapely.geometry.base.dump_coords(geometry)
                
                eventDict = {'type': "SIGMET.Convective",
                             'startTime': str(event.getStartTime()),
                             'endTime': str(event.getEndTime()),
                             'geometry': geometry,
                             'geomType': AviationUtils.AviationUtils().getGeometryType(event)}
                for attr in event.getHazardAttributes():
                    if attr in ['originalGeometry', 'visibleGeometry']:
                        pass
                    else:
                        eventDict[attr] = event.get(attr)
                    
                with open('/scratch/copyBuffer.txt', 'w') as outFile:
                    json.dump(eventDict, outFile)                
    
    def getGeometry(self, event):
        for g in event.getFlattenedGeometry().geoms:
            geometry = shapely.geometry.base.dump_coords(g)
            
        return geometry
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'CopyMetaDataTool'