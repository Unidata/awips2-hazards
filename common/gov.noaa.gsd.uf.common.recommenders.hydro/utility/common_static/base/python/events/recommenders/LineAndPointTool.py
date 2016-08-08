'''
Line and Point Tool to generate polygons from line and point hazard events.
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

######
TABLEFILE = '/home/nathan.hardin/Desktop/snap.tbl'

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
        metadata['onlyIncludeTriggerEvent'] = True
        
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
                         str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")

        sys.stderr.flush()
        
        eventSetAttrs = eventSet.getAttributes()
        self._trigger = eventSetAttrs.get('trigger')
        self._attribute = eventSetAttrs.get('attributeIdentifiers')
        self._eventIdentifier = eventSetAttrs.get('eventIdentifier')
        self._issueFlag = eventSetAttrs.get('issueFlag')
      
        changed = False
        for event in eventSet:
            validTime = event.get('validTime')
            self._originalGeomType = event.get('originalGeomType')
            event.set('originalGeometry', event.getGeometry())
            self._width = event.getHazardAttributes().get('convectiveSigmetWidth')
            vertices = self._getVertices(event)
            VOR_points = event.getHazardAttributes().get('VOR_points')
            
            #for points and lines, calculate polygon and add visual feature
            if self._originalGeomType != 'Polygon':
                if self._trigger == 'hazardEventModification':
                    if event.get('generated'):
                        self._adjustForVisualFeatureChange(event, eventSetAttrs)                       
                    else:
                        poly = self.createPolygon(vertices,self._width)
                        event.set('polygon', poly) 
                        changed = (self.addVisualFeatures(event,poly) or changed)
                    event.set('generated',True)                     
                
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
        if trigger == 'hazardEventModification': 
            return False            
        if trigger == 'hazardEventVisualFeatureChange' and len(self._attribute) is 1:                
            self._adjustForVisualFeatureChange(event, eventSetAttrs)
      
        return True            
    
    def _adjustForVisualFeatureChange(self, event, eventSetAttrs):
        features = event.getVisualFeatures()
        if not features: features = []

        changedIdentifier = list(eventSetAttrs.get('attributeIdentifiers'))[0]

        for feature in features:
            featureIdentifier = feature.get('identifier')
            # Find the feature that has changed
            if (featureIdentifier == changedIdentifier) or (changedIdentifier == 'convectiveSigmetWidth' and 'basePreview' in featureIdentifier):
                # Get feature polygon
                polyDict = feature["geometry"]
                for timeBounds, geometry in polyDict.iteritems():
                    featurePoly = geometry
                    vertices = shapely.geometry.base.dump_coords(featurePoly)
                    if any(isinstance(i, list) for i in vertices):
                        vertices = vertices[0]
                    convectiveSigmetDomain = AviationUtils.AviationUtils().selectDomain(event,vertices,self._originalGeomType,'modification')
                    boundingStatement = AviationUtils.AviationUtils().boundingStatement(event,self._originalGeomType,TABLEFILE,vertices,'modification')
                        
                    if self._originalGeomType != 'Polygon':
                        poly = self.createPolygon(vertices,self._width)
                        self._updateVisualFeatures(event, vertices, poly)
                    else:
                        poly = []
                        self._updateVisualFeatures(event, vertices, poly)                       
                    
    def _updateVisualFeatures(self, event, vertices, polyPoints):
        #if feature is changed/moved update visual features
        startTime = TimeUtils.roundDatetime(event.getStartTime())
        endTime = TimeUtils.roundDatetime(event.getEndTime())
        eventID = event.getEventID()
        selectedFeatures = []
        VOR_points = event.getHazardAttributes().get('VOR_points')
               
        if self._originalGeomType != 'Polygon':
            poly = GeometryFactory.createPolygon(polyPoints)
            basePoly = vertices
            if self._originalGeomType == 'Point':
                basePoly = GeometryFactory.createPoint(basePoly)
            elif self._originalGeomType == 'LineString':
                basePoly = GeometryFactory.createLineString(basePoly)
            event.setGeometry(basePoly)        
        else:
            poly = GeometryFactory.createPolygon(VOR_points)
            basePoly = GeometryFactory.createPolygon(vertices)
            event.setGeometry(basePoly)
        
        if self._originalGeomType != 'Polygon':       
            borderColorHazard = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 }
            fillColorHazard = {"red": 1, "green": 1, "blue": 1, "alpha": 0}
            borderColorBase = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 }
            fillColorBase = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 0.5 }
        else:
            borderColorHazard = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 }
            fillColorHazard = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 0.5 }
            borderColorBase = "eventType"
            fillColorBase = {"red": 1, "green": 1, "blue": 1, "alpha": 0}       
        
        hazardEventPoly = {
            "identifier": "hazardEventPolygon_" + eventID,
            "visibilityConstraints": "always",
            "diameter": "eventType",
            "borderColor": borderColorHazard,
            "fillColor": fillColorHazard,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "selected",
            "dragCapability": "all",
            "borderThickness": "eventType",
            "diameter": "eventType",
            "borderColor": borderColorBase,
            "fillColor": fillColorBase,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): basePoly
            }
        }                    

        selectedFeatures.append(basePoly)                      
        selectedFeatures.append(hazardEventPoly)            
        event.setVisualFeatures(VisualFeatures(selectedFeatures))

        return True
    
    def _setVORPoints(self, vorLat, vorLon, event):
        VOR_points = zip(vorLon, vorLat)
        event.set('VOR_points', VOR_points)
        
        return       
        
    def _getVertices(self, hazardEvent):
        for g in hazardEvent.getGeometry().geoms:
            vertices = shapely.geometry.base.dump_coords(g)     
        
        return vertices 

    def addVisualFeatures(self, event, points):
        startTime = TimeUtils.roundDatetime(event.getStartTime())
        endTime = TimeUtils.roundDatetime(event.getEndTime())
        eventID = event.getEventID()
        selectedFeatures = []
        
        poly = GeometryFactory.createPolygon(points)        
        
        if self._originalGeomType == 'Point':
            basePoly = event.get('originalGeometry')
            basePoly = basePoly[0]
        elif self._originalGeomType == 'LineString':
            basePoly = event.getGeometry()
            basePoly = basePoly[0]                          
        else:
            basePoly = event.getGeometry()
                
        borderColor = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 }       
        
        hazardEventPoly = {
            "identifier": "hazardEventPolygon_" + eventID,
            "visibilityConstraints": "selected",
            "borderColor": borderColor,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "always",
            "dragCapability": "all",
            "borderThickness": "eventType",
            "diameter": "eventType",
            "borderColor": {"red": 130/255.0, "green": 0/255.0, "blue": 0/255.0, "alpha": 1},
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): basePoly
            }
        }                    

        selectedFeatures.append(basePoly)                      
        selectedFeatures.append(hazardEventPoly)            
        event.setVisualFeatures(VisualFeatures(selectedFeatures))

        return True      
    
    def lineToPolygon(self, vertices, width):
        leftBuffer = [] 
        rightBuffer = []
        # Compute for start point
        bearing = self.gc_bearing(vertices[0],vertices[1])
        leftBuffer.append(self.gc_destination(vertices[0],width,(bearing-90.0)))
        rightBuffer.append(self.gc_destination(vertices[0],width,(bearing+90.0)))
        # Compute distance from points in middle of line
        for n in range(1,len(vertices)-1):
            b_1to2 = self.gc_bearing(vertices[n-1],vertices[n])
            b_2to3 = self.gc_bearing(vertices[n],vertices[n+1])
            theta = (b_2to3-b_1to2)/2.0
            bearing = b_1to2 + theta
            D = width / math.sin(math.radians(theta+90.0))
            leftBuffer.append(self.gc_destination(vertices[n],D,(bearing-90.0)))
            rightBuffer.append(self.gc_destination(vertices[n],D,(bearing+90.0)))
            # Compute for end point, right and left reversed for different direction
        bearing = self.gc_bearing(vertices[-1],vertices[-2])
        leftBuffer.append(self.gc_destination(vertices[-1],width,(bearing+90.0)))
        rightBuffer.append(self.gc_destination(vertices[-1],width,(bearing-90.0)))
        # Construct final corridor by combining both sides 
        poly = leftBuffer + rightBuffer[::-1] + [leftBuffer[0]]  

        return poly
    
    def pointToPolygon(self, vertices, width):
        buffer = []
        if len(vertices) == 1:
            width = width/2
            for bearing in range(0,360,15):
                loc = self.gc_destination(vertices[0],width,bearing)
                buffer.append((round(loc[0],2),round(loc[1],3)))
            poly = buffer
            
        return poly                
                 
    def createPolygon(self,vertices,width):
        vertices = [x[::-1] for x in vertices]
        width = float(width) * 1.852  # convert Nautical Miles to KM
        
        if self._originalGeomType == 'Point':
            poly = self.pointToPolygon(vertices,width)
        elif self._originalGeomType == 'LineString':
            poly = self.lineToPolygon(vertices,width)
                
        poly = [x[::-1] for x in poly]    

        return poly 

    def gc_bearing(self,latlong_1, latlong_2):
        lat1, lon1 = latlong_1
        lat2, lon2 = latlong_2
        rlat1, rlon1 = math.radians(lat1), math.radians(lon1)
        rlat2, rlon2 = math.radians(lat2), math.radians(lon2)
        dLat = math.radians(lat2 - lat1)
        dLon = math.radians(lon2 - lon1)

        y = math.sin(dLon) * math.cos(rlat2)
        x = math.cos(rlat1) * math.sin(rlat2) - \
            math.sin(rlat1) * math.cos(rlat2) * math.cos(dLon)
        bearing = math.degrees(math.atan2(y,x))
        return bearing

    def gc_destination(self,latlong_1, dist, bearing):
        R = 6378.137 # earth radius in km
        lat1, lon1 = latlong_1
        rlat1, rlon1 = math.radians(lat1), math.radians(lon1)
        d = dist
        bearing = math.radians(bearing)

        rlat2 = math.asin(math.sin(rlat1) * math.cos(d/R) + \
          math.cos(rlat1) * math.sin(d/R) * math.cos(bearing))
        rlon2 = rlon1 + math.atan2(math.sin(bearing) * math.sin(d/R) * math.cos(rlat1), \
          math.cos(d/R) - math.sin(rlat1) * math.sin(rlat2))
        lat2 = math.degrees(rlat2)
        lon2 = math.degrees(rlon2)
        if lon2 > 180.: lon2 = lon2 - 360.
        latlong_2 = (lat2, lon2)
        return latlong_2   
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
    
                      
def __str__(self):
    return 'Line and Point Tool'