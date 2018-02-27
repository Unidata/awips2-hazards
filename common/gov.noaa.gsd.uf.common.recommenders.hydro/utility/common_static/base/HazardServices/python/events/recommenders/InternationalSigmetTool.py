'''
International SIGMET Tool to generate and modify visual feature polygons.
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
        self.logger = logging.getLogger('InternationalSigmetTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'InternationalSigmetTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'International SIGMET Tool'
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
        Runs the Line and Point Tool
        
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
        sys.stderr.write("Running International Sigmet Tool.\n    trigger:    " +
                         str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
                         str(eventSet.getAttribute("eventType")) + "\n    origin:     " + 
                         str(eventSet.getAttribute("origin")) + "\n    hazard ID:  " +
                         str(eventSet.getAttribute("eventIdentifiers")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")

        sys.stderr.flush()
        eventSetAttrs = eventSet.getAttributes()
        self._trigger = eventSetAttrs.get('trigger')
        self._attribute = eventSetAttrs.get('attributeIdentifiers')
        eventIdentifiers = eventSetAttrs.get('eventIdentifiers')
        self._eventIdentifier = next(iter(eventIdentifiers)) if eventIdentifiers else None

        for event in eventSet:
            phenomena = event.get("internationalSigmetPhenomenonComboBox")
            if phenomena in ['turbulence', 'icing', 'icingFzra']:
                startTime = event.getStartTime().replace(second=0, microsecond=0)
                endTime = startTime + datetime.timedelta(hours=4)
                event.setEndTime(endTime)
            else:
                startTime = event.getStartTime().replace(second=0, microsecond=0)
                endTime = startTime + datetime.timedelta(hours=6)
                event.setEndTime(endTime)                       
         # Event Modification
            changed = self._processEventModification(event, self._trigger, eventSetAttrs)
            if changed:
                eventSet.add(event)                 

        return eventSet
    
    def _processEventModification(self, event, trigger, eventSetAttrs):
        if not self._makeUpdates(event, trigger, eventSetAttrs):                                
            return False
        return True
        
    def _makeUpdates(self, event, trigger, eventSetAttrs):
        if (trigger == 'hazardEventVisualFeatureChange'):                   
            self._adjustForVisualFeatureChange(event, eventSetAttrs)
      
        return True            
    
    def _adjustForVisualFeatureChange(self, event, eventSetAttrs):
        features = event.getVisualFeatures()
        if not features: features = []
        
        for feature in features:
            featureIdentifierAbbrev = str(feature["identifier"][:11])
            featureIdentifier = feature.get('identifier')
            
            polyDict = feature['geometry']
            for timeBounds, geometry in polyDict.iteritems():
                featurePoly = geometry.asShapely()
                vertices = shapely.geometry.base.dump_coords(featurePoly)
                
                if any(isinstance(i, list) for i in vertices):
                    vertices = vertices[0]
                
                if "basePreview" in featureIdentifier:
                    event.set('newGeometry', vertices)
                if "vaFcstPoly" in featureIdentifier:
                    event.set(featureIdentifierAbbrev, vertices)
                if "vaObsPoly" in featureIdentifier:
                    event.set(featureIdentifierAbbrev, vertices)
                    
        self.updateVisualFeatures(event)    
        
    def getVertices(self, hazardEvent):
        for g in hazardEvent.getFlattenedGeometry().geoms:
            vertices = shapely.geometry.base.dump_coords(g)     
        
        return vertices 
    
    def updateVisualFeatures(self, hazardEvent):
        numFcstLayers = hazardEvent.get("internationalSigmetVALayersSpinner")
        numObsLayers = hazardEvent.get("internationalSigmetObservedLayerSpinner")
        
        startTime = hazardEvent.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())      
        
        eventID = hazardEvent.getEventID()
            
        selectedFeatures = []
                
        newBaseGeometry= hazardEvent.get('newGeometry')       
        basePoly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(newBaseGeometry), 0)
        hazardEvent.setGeometry(basePoly)
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "always",
            "dragCapability": "all",
            "borderColor": "eventType",
            "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
            "label": "Observed Layer 1",            
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): basePoly
            }
        }
        selectedFeatures.append(basePoly) 
        

        for i in range(0, numFcstLayers):
            featureName = 'vaFcstPoly_'+str(i+1)
            fcstGeometry = hazardEvent.get(featureName)
            poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(fcstGeometry), 0)           
            featureName = {
                "identifier": featureName+"_" + eventID,
                "dragCapability": "all",
                "borderColor": {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 },
                "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
                "label": "Forecast Layer"+str(i+1),
                "geometry": {
                    (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): poly
                }
            }                   
            selectedFeatures.append(featureName)
        
        for i in range(0, numObsLayers):
            featureName = 'vaObsPoly'+str(i+1)
            obsGeometry = hazardEvent.get(featureName)
            poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(obsGeometry), 0)
            featureName = {
                "identifier": featureName+"_" + eventID,
                "dragCapability": "all",
                "borderColor": {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 },
                "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
                "label": "Observed Layer"+str(i+2),
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
    return 'Line and Point Tool'