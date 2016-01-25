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
from inspect import currentframe, getframeinfo
import os


import JUtil
     

#===============================================================================
# Users wanting to add new storm paths should enter a new method in this class
# with the same name as one of the choices found in 
# CommonMetaData.py:_getConvectiveSwathPresets()
# and using the same arguments to the new method in this class as the existing
# methods.
#
# For example, if I want to add anew path named 'foo', I would add 'foo'
# as a choice in CommonMetaData.py:_getConvectiveSwathPresets()
# and a method below as:
#
# def foo(self, speedVal, dirVal, spdUVal, dirUVal, step, numIvals):
#
# which would do some calculations based on the values passed in and
# then return a dictionary:
# 
#        returnDict = {
#                      'speedVal':speedVal, 
#                      'dirVal':dirVal, 
#                      'spdUVal':spdUVal,
#                      'dirUVal':dirUVal
#                      }
#
# 
#===============================================================================
class SwathPreset(object):
    def __init__(self):
        pass
    
    def NoPreset(self, speedVal, dirVal, spdUVal, dirUVal, step, numIvals):
        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def RightTurningSupercell(self, speedVal, dirVal, spdUVal, dirUVal, step, numIvals): 
        spdWt = 0.75
        dirWtTup = (0.,30.)
        dirWt = dirWtTup[1] * step / numIvals
        dirVal = dirVal + dirWt
        speedVal =  speedVal * spdWt
      
        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def LeftTurningSupercell(self, speedVal, dirVal, spdUVal, dirUVal, step, numIvals): 
        spdWt = 0.75
        dirWtTup = (0.,30.)
        dirWt = -1. * dirWtTup[1] * step / numIvals
        dirVal = dirVal + dirWt
        speedVal = speedVal * spdWt
        
        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def BroadSwath(self, speedVal, dirVal, spdUVal, dirUVal, step, numIvals):
        spdUValWtTup = (0.,15)
        dirUValWtTup = (0.,40)
        spdUValWt = spdUValWtTup[1] * step / numIvals
        spdUVal = spdUVal + (spdUValWtTup[1] - spdUValWt)
        dirUValWt = dirUValWtTup[1] * step / numIvals
        dirUVal = dirUVal + (dirUValWtTup[1] - dirUValWt)

        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def LightBulbSwath(self, speedVal, dirVal, spdUVal, dirUVal, step, numIvals):
        spdUValWtTup = (0.,7)
        dirUValWtTup = (0.,20)
        spdUValWt = spdUValWtTup[1] * step / numIvals
        spdUVal = spdUVal + (spdUValWtTup[0] + spdUValWt)
        dirUValWt = dirUValWtTup[1] * step / numIvals
        dirUVal = dirUVal + (dirUValWtTup[0] + dirUValWt)

        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
     
class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('SwathRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'SwathRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        self.sp = SwathPreset()
        

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
            
        presetChoice = attrs.get('convectiveSwathPresets')
        presetMethod = getattr(self.sp,presetChoice )
        
        
        ### convert poly to Google Coords to make use of Karstens' code
        fi_filename = os.path.basename(getframeinfo(currentframe()).filename)
        total = time.time()
        st0 = time.time()
        gglPoly = so.transform(self._c4326t3857,poly)
        print '[',fi_filename, getframeinfo(currentframe()).lineno, '] took ', time.time()-st0, 'seconds'
        
        ### calc for 1-minute intervals over duration
        numIvals = int(durationSecs/60)
        downstreamPolys = []
        st0 = time.time()
        for step in range(numIvals):
            origDirVal = dirVal
            presetResults = presetMethod(speedVal, dirVal, spdUVal, dirUVal, step, numIvals)
            secs = step*60
            start = time.time()
            gglDownstream = self.downStream(secs,
                                            presetResults['speedVal'],
                                            presetResults['dirVal'],
                                            presetResults['spdUVal'],
                                            presetResults['dirUVal'],
                                            origDirVal,
                                            gglPoly)
            print '\t[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-start, 'seconds'
            downstreamPolys.append(so.transform(self._c3857t4326, gglDownstream))
        
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-st0, 'seconds'
        start = time.time()
        envelope = shapely.ops.cascaded_union(downstreamPolys)
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-start, 'seconds'
        
        start = time.time()
        envelope = shapely.ops.cascaded_union(downstreamPolys)
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-start, 'seconds'
        start = time.time()
        envelope = shapely.ops.cascaded_union(downstreamPolys)
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-start, 'seconds'
        start = time.time()
        polys = shapely.geometry.MultiPolygon([poly, envelope])
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-start, 'seconds'
        start = time.time()
        envelope = shapely.ops.cascaded_union(downstreamPolys)
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-start, 'seconds'
        start = time.time()
        event.setGeometry(polys)
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] took ', time.time()-start, 'seconds'
        
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] === FINAL took ', time.time()-total, 'seconds ==='
        print '[', fi_filename, getframeinfo(currentframe()).lineno,'] ...for polygon with', len(poly.exterior.coords), 'points'
        
        

    def downStream(self, secs, speedVal, dirVal, spdUVal, dirUVal, origDirVal, threat):
        dis = secs * speedVal * 0.514444444
        xDis = dis * math.cos(math.radians(270.0 - dirVal))
        yDis = dis * math.sin(math.radians(270.0 - dirVal))
        xDis2 = secs * spdUVal * 0.514444444
        yDis2 = dis * math.tan(math.radians(dirUVal))
        threat = sa.translate(threat,xDis,yDis)
    
        if origDirVal:
            rot = origDirVal - dirVal
            threat = sa.rotate(threat,rot,origin='centroid')
    
            #rotVal = -1 * (270 - dirVal)
            #if rotVal > 0:
            #    rotVal = -1 * (360 - rotVal)
            #print '\tRotVal1:', rotVal
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
        #if origDirVal:
        #    rotVal = 270 - origDirVal
        #    if rotVal < 0:
        #        rotVal = rotVal + 360
        #    print '\tRotVal2:', rotVal
        #    threat = sa.rotate(threat,rotVal,origin='centroid')
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

