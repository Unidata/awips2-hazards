'''
Line and Point Tool to generate polygons from line and point hazard events.
'''
import datetime, time, TimeUtils
import GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler
import shapely
from shapely.geometry import Polygon
from VisualFeatures import VisualFeatures
import AviationUtils
import AdvancedGeometry

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('LineAndPointTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'LineAndPointTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Line and Point Tool'
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
        sys.stderr.write("Running Line and Point Tool.\n    trigger:    " +
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
        
        identifiers = list(eventSetAttrs.get('attributeIdentifiers'))
        baseAttribute = False
        hazardAttribute = False
        for identifier in identifiers:
            if 'hazardEvent' in identifier:
                hazardAttribute = True
            else:
                pass
            
            if 'basePreview' in identifier:
                baseAttribute = True
            else:
                pass
      
        changed = False
        for event in eventSet:
            validTime = event.get('validTime')
            self._originalGeomType = event.get('originalGeomType')
            event.set('originalGeometry', event.getFlattenedGeometry())
            self._width = event.getHazardAttributes().get('convectiveSigmetWidth')
            vertices = self._getVertices(event)
            VOR_points = event.getHazardAttributes().get('VOR_points')
            
            if self._originalGeomType == 'Polygon':
                if self._trigger == 'hazardEventModification':
                    self.addPolygonVisualFeatures(event)
                if self._trigger == 'hazardEventVisualFeatureChange':
                    if baseAttribute == False:
                        self.addPolygonVisualFeatures(event)
                    else:
                        if hazardAttribute:
                            self.addPolygonVisualFeatures(event)
                        else: 
                            changed = self._processEventModification(event, self._trigger, eventSetAttrs)    
            if self._originalGeomType != 'Polygon':
                if self._trigger == 'hazardEventVisualFeatureChange':
                    if baseAttribute ==  False:
                        poly = AviationUtils.AviationUtils().createPolygon(vertices,self._width,self._originalGeomType)
                        event.set('polygon', poly) 
                        changed = (self.addVisualFeatures(event,poly) or changed)
                        event.set('generated',True)
                    else:
                        if hazardAttribute:
                            poly = AviationUtils.AviationUtils().createPolygon(vertices,self._width,self._originalGeomType)
                            event.set('polygon', poly) 
                            changed = (self.addVisualFeatures(event,poly) or changed)
                            event.set('generated',True)                            
                        else:
                            changed = self._processEventModification(event, self._trigger, eventSetAttrs)                    
                if self._trigger == 'hazardEventModification':
                    if event.get('generated'):
                        self._adjustForVisualFeatureChange(event, eventSetAttrs)                       
                    else:
                        poly = AviationUtils.AviationUtils().createPolygon(vertices,self._width,self._originalGeomType)
                        event.set('polygon', poly) 
                        changed = (self.addVisualFeatures(event,poly) or changed)
                    event.set('generated',True)                     
                
            # Event Modification
            changed = self._processEventModification(event, self._trigger, eventSetAttrs)
            if changed:
                eventSet.add(event)                 

        return eventSet
    
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
            "borderColor": borderColor, #"eventType",
            "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): basePoly
            }
        }                    

        selectedFeatures.append(basePoly)
        selectedFeatures.append(VORPoly)
        
        hazardEvent.setVisualFeatures(VisualFeatures(selectedFeatures))    
        
        return True            
    
    def _processEventModification(self, event, trigger, eventSetAttrs):
        if not self._makeUpdates(event, trigger, eventSetAttrs):                                
            return False
        return True
        
    def _makeUpdates(self, event, trigger, eventSetAttrs):
        if (trigger == 'hazardEventVisualFeatureChange') or (trigger == 'hazardEventModification' and self._originalGeomType == 'Polygon'):                    
            self._adjustForVisualFeatureChange(event, eventSetAttrs)
      
        return True            
    
    def _adjustForVisualFeatureChange(self, event, eventSetAttrs):
        features = event.getVisualFeatures()
        if not features: features = []

        identifiers = list(eventSetAttrs.get('attributeIdentifiers'))
        
        if identifiers:
            changedIdentifier = list(eventSetAttrs.get('attributeIdentifiers'))[0]
        else:
            return

        for feature in features:            
            if 'base' in feature["identifier"]:
                featureIdentifier = feature.get('identifier')
                # Find the feature that has changed
                changedIdentifierList = ['convectiveSigmetWidth', 'convectiveSigmetDirection',
                                         'convectiveSigmetSpeed', 'convectiveSigmetCloudTop', 'convectiveSigmetCloudTopText', 'geometry']
                if (featureIdentifier == changedIdentifier) or (changedIdentifier in changedIdentifierList and 'basePreview' in featureIdentifier) or ('basePreview' in changedIdentifier):
                    # Get feature polygon
                    polyDict = feature["geometry"]
                    for timeBounds, geometry in polyDict.iteritems():
                        featurePoly = geometry.asShapely()
                        vertices = shapely.geometry.base.dump_coords(featurePoly)
                        if any(isinstance(i, list) for i in vertices):
                            vertices = vertices[0]
                        convectiveSigmetDomain = AviationUtils.AviationUtils().selectDomain(event,vertices,self._originalGeomType,'modification')
                        boundingStatement = AviationUtils.AviationUtils().boundingStatement(event,self._originalGeomType,vertices,'modification')
                            
                        if self._originalGeomType != 'Polygon':
                            poly = AviationUtils.AviationUtils().createPolygon(vertices,self._width,self._originalGeomType)
                            AviationUtils.AviationUtils().updateVisualFeatures(event,vertices,poly)
                        else:
                            poly = []
                            AviationUtils.AviationUtils().updateVisualFeatures(event,vertices,poly)
    
    def _setVORPoints(self, vorLat, vorLon, event):
        VOR_points = zip(vorLon, vorLat)
        event.set('VOR_points', VOR_points)
        
        return       
        
    def _getVertices(self, hazardEvent):
        for g in hazardEvent.getFlattenedGeometry().geoms:
            vertices = shapely.geometry.base.dump_coords(g)     
        
        return vertices 

    def addVisualFeatures(self, event, points):
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
        
        poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(points), 0)         
        
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
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'Line and Point Tool'