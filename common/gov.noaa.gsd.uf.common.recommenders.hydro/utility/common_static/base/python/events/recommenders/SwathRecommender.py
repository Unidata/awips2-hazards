'''
Swath recommender for probabilistic hazard types.
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import math, time
import shapely
import shapely.ops as so
import shapely.geometry as sg
import shapely.affinity as sa



import JUtil
     
class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('SwathRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'SwathRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Swath Recommender'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        
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
        
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        '''
        Runs the Swath Recommender tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param spatialInputMap:   A map of information retrieved
                                  from the user's interaction with the
                                  spatial display.
        
        @return: A list of potential probabilistic hazard events. 
        '''
        
        # For now, just print out a message saying this was run.
        import sys
        sys.stderr.write("Running swath recommender.\n    trigger: " +
                         str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
                         str(eventSet.getAttribute("eventType")) + "\n    hazard ID: " +
                         str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute: " +
                         str(eventSet.getAttribute("attributeIdentifier")) + "\n")
        sys.stderr.flush()
        
        for event in eventSet:
            if event.getHazardAttributes().get('selected'):
                if not event.getHazardAttributes().get('removeEvent'):  ### UNNEEDED ONCE CODE UPDATED TO REMOVE THE EVENT
                    self.createIntervalPolygons(event)
                
                
        
        return eventSet

    def createIntervalPolygons(self, event):
        attrs = event.getHazardAttributes()
        
        ### get dir
        dirVal = attrs.get('convectiveObjectDir')
        if dirVal:
          dirVal = int(dirVal)  
        ### get dirUncertainty (degrees)
        dirUVal = attrs.get('convectiveObjectDirUnc')
        if dirUVal:
            dirUVal = int(dirUVal)
        else:
            dirUVal = 10
        ### get speed
        speedVal = attrs.get('convectiveObjectSpdKts')
        if speedVal:
            speedVal = int(speedVal)
        ### get speedUncertainty (kts)
        spdUVal = attrs.get('convectiveObjectSpdKtsUnc')
        if spdUVal:
            spdUVal = int(spdUVal)
        else:
            spdUVal = 10
        ### get duration (in seconds)
        durationSecs = 2700 # (45 mins)
        ### get initial polygon
        poly = event.getGeometry()
        
        ### Check if single polygon or iterable
        if hasattr(poly,'__iter__'):
            poly = poly[0]
            
        
        
        ### convert poly to Google Coords to make use of Karstens' code
        gglPoly = so.transform(self._c4326t3857,poly)
        
        ### calc for 1-minute intervals over duration
        numIvals = int(durationSecs/60)
        downstreamPolys = []
        for step in range(numIvals):
            secs = step*60
            gglDownstream = self.downStream(secs, speedVal, dirVal, spdUVal, dirUVal, gglPoly)
            downstreamPolys.append(so.transform(self._c3857t4326, gglDownstream))
        
        envelope = shapely.ops.cascaded_union(downstreamPolys)
        
        polys = shapely.geometry.MultiPolygon([poly, envelope])
        event.setGeometry(polys)
        
        

    def downStream(self, secs, speedVal, dirVal, spdUVal, dirUVal, threat):
        dis = secs * speedVal * 0.514444444
        xDis = dis * math.cos(math.radians(270.0 - dirVal))
        yDis = dis * math.sin(math.radians(270.0 - dirVal))
        xDis2 = secs * spdUVal * 0.514444444
        yDis2 = dis * math.tan(math.radians(dirUVal))
        threat = sa.translate(threat,xDis,yDis)
        #threat_orig = sa.translate(threat_orig,xDis,yDis)
        #rot = dirValLast - dirVal
        #threat = sa.rotate(threat,rot,origin='centroid')
        #threat_orig = sa.rotate(threat_orig,rot,origin='centroid')
        #rotVal = -1 * (270 - dirValLast)
        #if rotVal > 0:
        #        rotVal = -1 * (360 - rotVal)
        #threat = sa.rotate(threat,rotVal,origin='centroid')
        coords = threat.exterior.coords
        center = threat.centroid
        newCoords = []
        for c in coords:
                dir = math.atan2(c[1] - center.y,c[0] - center.x)
                x = math.cos(dir) * xDis2
                y = math.sin(dir) * yDis2
                p = sg.Point(c)
                c2 = sa.translate(p,x,y)
                newCoords.append((c2.x,c2.y))
        threat = sg.Polygon(newCoords)
        #rotVal = 270 - dirValLast
        #if rotVal < 0:
        #        rotVal = rotVal + 360
        #threat = sa.rotate(threat,rotVal,origin='centroid')
        return threat



    def _c4326t3857(self, lon, lat):
        """
        Pure python 4326 -> 3857 transform. About 8x faster than pyproj.
        """
        lat_rad = math.radians(lat)
        xtile = lon * 111319.49079327358
        ytile = math.log(math.tan(lat_rad) + (1 / math.cos(lat_rad))) / \
            math.pi * 20037508.342789244
        return(xtile, ytile)
    
    
    def _c3857t4326(self, lon, lat):
        """
        Pure python 3857 -> 4326 transform. About 12x faster than pyproj.
        """
        xtile = lon / 111319.49079327358
        ytile = math.degrees(
            math.asin(math.tanh(lat / 20037508.342789244 * math.pi)))
        return(xtile, ytile)

        
    def __str__(self):
        return 'Swath Recommender'

