'''
Preview grid recommender for probabilistic hazard types.
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import time
import shapely
import shapely.ops as so
import shapely.geometry as sg
import GeometryFactory
from shapely.geometry import Polygon
import shapely.affinity as sa
from inspect import currentframe, getframeinfo
import os, sys
import numpy as np
import matplotlib
#matplotlib.use("Agg")
from matplotlib import path as mPath
import matplotlib.cm as cm
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
from scipy import ndimage
from scipy.io import netcdf

import TimeUtils
from SwathRecommender import Recommender as SwathRecommender
from ProbUtils import ProbUtils
import AdvancedGeometry
from VisualFeatures import VisualFeatures
import JUtil
     
### FIXME: set path in configuration somewhere
OUTPUTDIR = '/scratch/PHIGridTesting'
### FIXME: need better (dynamic or configurable) way to set domain corner
#buff = 1.
#lonPoints = 1200
#latPoints = 1000
#ulLat = 44.5 + buff
#ulLon = -104.0 - buff

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('PreviewGridRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'PreviewGridRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Preview Grid Recommender'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['onlyIncludeTriggerEvent'] = True
        #metadata['includeEventTypes'] = [ "Prob_Severe", "Prob_Tornado" ]
        
        # This tells Hazard Services to not notify the user when the recommender
        # creates no hazard events. Since this recommender is to be run in response
        # to hazard event changes, etc. it would be extremely annoying for the user
        # to be constantly dismissing the warning message dialog if no hazard events
        # were being created. 
        metadata['background'] = True
        
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: A dialog definition to solicit user input before running tool
        '''   
        return None
        
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        Runs the Swath Recommender tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param visualFeatures: Visual features as defined by the
                               defineSpatialInfo() method and
                               modified by the user to provide
                               spatial input; ignored.
        
        @return: A list of potential probabilistic hazard events. 
        '''
        
        # For now, just print out a message saying this was run.
        import sys
        sys.stderr.write("Running Preview grid recommender.\n    trigger:    " +
                         str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
                         str(eventSet.getAttribute("eventType")) + "\n    origin:     " + 
                         str(eventSet.getAttribute("origin")) + "\n    hazard ID:  " +
                         str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
        
        #testing
        #self._trigger = {'trigger':eventSet.getAttribute("trigger"), 'attrs':eventSet.getAttribute("attributeIdentifiers")}
        sys.stderr.flush()
        
        # Iterate through the events that were passed in, processing each in turn
        # by first removing all preview-grid-related visual features (if any are
        # found), since even if generating them, any old ones need to be removed
        # first; and then, if the "show grid" flag has been set to true, generating
        # new ones.
        changed = False        
        for event in eventSet:
            changed = (self.removeVisualFeatures(event) or changed)
            if event.getHazardAttributes().get('showGrid') == True:
                vsualFeatures = []
                probGrid, lons, lats = ProbUtils().getProbGrid(event)
                polyTupleDict = self.createPolygons(probGrid, lons, lats)
                changed = (self.addVisualFeatures(event, polyTupleDict) or changed)
               
        # Return nothing unless something was changed, since returning nothing
        # tells Hazard Services no changes were made by the recommender.
        if changed:
            return eventSet
        return None
    
    # Remove any visual features generated as part of preview grids previously,
    # returning True if this results in changes to the hazard event, and False
    # otherwise.
    def removeVisualFeatures(self, event):
        visualFeatures = event.getVisualFeatures()
        
        if not visualFeatures:
            return False
        
        newFeatures = []
        changed = False
        for feature in visualFeatures:
            if not feature.get('identifier').find('gridPreview')>=0:
                newFeatures.append(feature)
            else:
                changed = True
                
        if changed:        
            event.setVisualFeatures(VisualFeatures(newFeatures))   

        return changed
         
    # Generate and add preview-grid-related visual features, returning True if
    # this results in changes to the hazard event, and False otherwise.
    def addVisualFeatures(self, event, polyTupleDict):
                
        features = event.getVisualFeatures()
        
        for tuple in polyTupleDict:  
            if tuple == '0':
                try:
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple],[polyTupleDict['20']])
                except KeyError:
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple])                       
            elif tuple == '20':
                try: 
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple],[polyTupleDict['40']])
                except KeyError:
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple])                     
            elif tuple == '40':
                try:
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple],[polyTupleDict['60']]) 
                except KeyError:
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple])                                            
            elif tuple == '60':
                try:
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple],[polyTupleDict['80']])                   
                except KeyError:
                    poly = GeometryFactory.createPolygon(polyTupleDict[tuple])                       
            else:
                poly = GeometryFactory.createPolygon(polyTupleDict[tuple])
            
            poly = AdvancedGeometry.createShapelyWrapper(poly, 0)
                
            ### Should match PHI Prototype Tool
            colorFill =  {
                '0': { "red": 102/255.0, "green": 224/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '20': { "red": 255/255.0, "green": 255/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '40': { "red": 255/255.0, "green": 179/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '60': { "red": 255/255.0, "green": 102/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '80': { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0, "alpha": 0.4 }
                }
                    
            gridPreviewPoly = {
                "identifier": "gridPreview_" + tuple,
                "visibilityConstraints": "selected",
                "borderColor": colorFill[tuple],
                "fillColor": colorFill[tuple],
                "geometry": {
                    (TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()), TimeUtils.datetimeToEpochTimeMillis(event.getEndTime())): poly
                }
            }

            features.append(gridPreviewPoly)
            
        event.setVisualFeatures(VisualFeatures(features))

        return True      
    
    def createPolygons(self, probGrid, lons, lats):
        polyDict = {}
        
        #probGrid, lons, lats = ProbUtils().getProbGrid(event)
                
        levels = np.linspace(0,100,6)

        X, Y = np.meshgrid(lons, lats)
        plt.figure()
        CS = plt.contour(X, Y, probGrid, levels=levels)

        prob = ['0', '20', '40', '60', '80']
        probIndex = [0.0, 20.0, 40.0, 60.0, 80.0]
        polyTupleDict = {}

        for c in range(0,(len(CS.levels) - 1)):
            contourVal = CS.levels[c]
            coords = CS.collections[c].get_paths()

            if len(coords):
                points = coords[0].vertices
                
                longitudes = []
                latitudes = []
                
                for point in range(0, len(points)):
                    longitudes.append(points[point][0])
                    latitudes.append(points[point][1])
                
                if contourVal in probIndex:    
                    polyTupleDict[prob[c]] = zip(longitudes, latitudes)
                    
        return polyTupleDict
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'Preview Grid Recommender'