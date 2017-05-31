'''
VAA Layer Tool to generate visual features of forecast ash location.
'''
import datetime, TimeUtils
import GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler
import time
import shapely
from VisualFeatures import VisualFeatures
import AdvancedGeometry

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('VAALayerTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'VAALayerTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'VAA Layer Tool'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['onlyIncludeTriggerEvents'] = True
        
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
        sys.stderr.write("Running VAA Layer Tool.\n")

        sys.stderr.flush()
        
        for event in eventSet:
            numLayers = event.get("volcanoLayersSpinner")
            
            layerDict = {}
            
            for i in range(1,numLayers+1):
                forecastStr = 'volcanoLayerForecast'+str(i)
                forecastPeriods = event.get(forecastStr)
                layerDict[i] = forecastPeriods
            
            self.addForecastLayersVisualFeatures(event, layerDict)
            
        return eventSet
    
    def addForecastLayersVisualFeatures(self, hazardEvent, layerDict):
        startTime = hazardEvent.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())
        
        vertices = hazardEvent.getFlattenedGeometry()
        vertices = shapely.geometry.base.dump_coords(vertices)
        vertices = vertices.pop()

        fcstVerticesDict = {}
        
        for key in layerDict:
            for i in range(0,layerDict[key]+1):
                newVerticesList = []
                for vertex in vertices:
                    newVert = []
                    newLat = vertex[1] - 1.5*(int(key)-1)
                    newLon = vertex[0] + 1.5*i
                    newVert.append(newLon)
                    newVert.append(newLat)
                    newVerticesList.append(newVert)
                newVerticesTuple = tuple(newVerticesList)
                fcstVerticesDict["Layer"+str(key)+'fcst'+str(i)] = newVerticesTuple       
        
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
        
        borderColorDict = {1: {0: {"red": 255 / 255.0, "green": 0 / 255.0, "blue": 127 / 255.0, "alpha": 1.0 },
                               4: {"red": 255 / 255.0, "green": 0 / 255.0, "blue": 127 / 255.0, "alpha": 1.0 },
                               3: {"red": 255 / 255.0, "green": 51 / 255.0, "blue": 153 / 255.0, "alpha": 1.0 },
                               2: {"red": 255 / 255.0, "green": 102 / 255.0, "blue": 178 / 255.0, "alpha": 1.0 },
                               1: {"red": 255 / 255.0, "green": 153 / 255.0, "blue": 204 / 255.0, "alpha": 1.0 }},
                           2: {0: {"red": 229 / 255.0, "green": 204 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               4: {"red": 127 / 255.0, "green": 0 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               3: {"red": 153 / 255.0, "green": 51 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               2: {"red": 178 / 255.0, "green": 102 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               1: {"red": 204 / 255.0, "green": 153 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 }},
                           3: {0: {"red": 204 / 255.0, "green": 204 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               4: {"red": 0 / 255.0, "green": 0 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               3: {"red": 51 / 255.0, "green": 51 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               2: {"red": 102 / 255.0, "green": 102 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                               1: {"red": 153 / 255.0, "green": 153 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 }}}
        labelDict = {1: {0: 'Layer1',
                         1: 'Layer1 +6',
                         2: 'Layer1 +12',
                         3: 'Layer1 +18',
                         4: 'Layer1 +24'},
                     2: {0: 'Layer2',
                         1: 'Layer2 +6',
                         2: 'Layer2 +12',
                         3: 'Layer2 +18',
                         4: 'Layer2 +24'},
                     3: {0: 'Layer3',
                         1: 'Layer3 +6',
                         2: 'Layer3 +12',
                         3: 'Layer3 +18',
                         4: 'Layer3 +24'},
                     }        
        
        for key in layerDict:
            for i in range(0,layerDict[key]+1):
                if key == 1 and i == 0:
                    pass
                else:
                    poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(fcstVerticesDict["Layer"+str(key)+'fcst'+str(i)]), 0)
                    featureName = "Layer"+str(key)+'fcst'+str(i) 
                    featureName = {
                        "identifier": "Layer"+str(key)+'fcst'+str(i)+"_" + eventID,
                        "dragCapability": "all",
                        "visibilityConstraints": "selected",
                        "borderColor": borderColorDict[key][i],
                        "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
                        "label": labelDict[key][i],
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
    return 'Create VAA Layer Forecast Tool'