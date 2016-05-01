'''
Swath recommender for probabilistic hazard types.
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
from VisualFeatures import VisualFeatures
from ProbUtils import ProbUtils
import logging, UFStatusHandler
import matplotlib.pyplot as plt

import math, time
from math import *
import shapely
import shapely.ops as so
import shapely.geometry as sg
import shapely.affinity as sa
from inspect import currentframe, getframeinfo
import os, sys
from java.util import Date
import numpy as np


import JUtil     

#===============================================================================
# Users wanting to add new storm paths should enter a new method in this class
# with the same name as one of the choices found in 
# CommonMetaData.py:_getConvectiveSwathPresets()
# and using the same arguments to the new method in this class as the existing
# methods.
#
# For example, if I want to add a new path named 'foo', I would add 'foo'
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
    
class PolygonFeature:
    def __init__(self, polygon, start, end, prob):
        pass
    
         
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
        metadata['includeEventTypes'] = [ "Prob_Severe", "Prob_Tornado" ]
        metadata['onlyIncludeTriggerEvent'] = True
        metadata['includeLatestDataLayerTime'] = [ "radar" ]
        
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
        
        Please see this Google Doc for the SwathRecommender Design:
        https://docs.google.com/document/d/1Ry9Es0bZheazBnkTkpKUbnzpHejKomwrv_z-fIFji1k/edit
        
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
        self._setPrintFlags()
        self._printEventSet("\nRunning SwathRecommender", eventSet, eventLevel=1)
                
        self.sp = SwathPreset()        
        eventSetAttrs = eventSet.getAttributes()
        trigger = eventSetAttrs.get('trigger')
        self._currentTime = long(eventSetAttrs.get("currentTime"))
        self._latestDataLayerTime = eventSetAttrs.get("latestDataLayerTime", self._currentTime)
        self._attributeIdentifiers = eventSetAttrs.get('attributeIdentifiers')

        resultEventSet = EventSetFactory.createEventSet(None)
                
        for event in eventSet:                           
            event.set('issueTime', self._currentTime) # Try removing this and see if we can issue
             
            # Find the one event in the case of modification           
            if trigger in ['hazardEventModification', 'hazardEventVisualFeatureChange']:
                eventIdentifier = eventSetAttrs.get('eventIdentifier')            
                if eventIdentifier and eventIdentifier != event.getEventID():
                    continue                
            if event.getStatus() in ['ELAPSED', 'ENDED']:
                continue
            
            # Begin Graph Draw
            if trigger == 'hazardEventModification' and len(self._attributeIdentifiers) is 1 and \
                    'convectiveProbTrendGraph' in self._attributeIdentifiers and \
                    event.get('convectiveProbTrendGraph', []) == []:
                self._processBeginGraphDraw(event, trigger)
                resultEventSet.add(event)
                continue

            # Event Bookkeeping
            self._pendingHazard = event.getStatus() in ["PENDING", "POTENTIAL"] 
            self._initializeEvent(event)                      
            if eventSetAttrs.get('origin') == 'user':
                self._setUserOwned(event)
                       
            # Data Layer Update 
            if trigger == 'dataLayerUpdate':
                self._processDataLayerUpdate(event)
                resultEventSet.add(event)
                continue
                        
            # Event Modification
            changed = self._processEventModification(event, trigger, eventSetAttrs) 
            # Still need to update visual features if status changes
            if "status" in self._attributeIdentifiers or "showGrid" in self._attributeIdentifiers:
                self._setVisualFeatures(event)
                resultEventSet.add(event)
            if changed:
                resultEventSet.add(event)  
                                                                        
        return resultEventSet    
    
    def _processDataLayerUpdate(self, event):    
        self._setStartTime(event, self._latestDataLayerTime)                
        self._advanceDownstreamPolys(event)  
        self._setVisualFeatures(event)   
        
    def _processBeginGraphDraw(self, event, trigger):
        # If this execution was triggered by the user initiating a "draw new
        # points on graph" action, make a note of this; otherwise, initialize
        # motion vector polygons and advance the downstream polygons.
        event.set('preDraw_convectiveProbTrendGraph', event.get('prev_convectiveProbTrendGraph'))                      
        
    def _processEventModification(self, event, trigger, eventSetAttrs):
        # TODO -- Still some refactoring needed to handle all cases more 
        #  elegantly
        #self._printEvent("Before Update", event) 
                        
        # Make updates to the event
        if not self._makeUpdates(event, trigger, eventSetAttrs):                                
            return False
        
        # Recalculate the polygons (downstream and upstream)
        self._advanceDownstreamPolys(event)
        self._createIntervalPolys(event, eventSetAttrs, timeDirection="downstream")            
        if self._pendingHazard:
            self._createIntervalPolys(event, eventSetAttrs, timeDirection="upstream")  
            
        # Update Visual Features                                          
        self._setVisualFeatures(event)         
        return True       
                           
        #self._printEvent("After Update", event)  
        
    def _makeUpdates(self, event, trigger, eventSetAttrs):    
        # Make updates to motion vector, duration due to user adjustments to 
        #  hazard attributes or visual features
        if trigger == 'hazardEventModification' and not self._needUpdate(event): 
            return False
        if trigger == 'hazardEventVisualFeatureChange':                 
            self._adjustForVisualFeatureChange(event, eventSetAttrs)
        return True
               
    def _setUserOwned(self, event):         
        event.set('convectiveUserOwned', True)
        if event.get('objectID') and not event.get('objectID').startswith('M'):
            event.set('objectID',  'M' + event.get('objectID'))

    def _setStartTime(self, event, startMS):
        ### get duration (in seconds)
        event.setStartTime(datetime.datetime.fromtimestamp(startMS/1000))
        if self._pendingHazard:
            durationSecs = int(round((event.getEndTime()-event.getStartTime()).total_seconds()))
            endMS = startMS+(durationSecs*1000)
            event.setEndTime(datetime.datetime.fromtimestamp(endMS/1000))
        self._roundEventTimes(event)
        self._eventSt_ms = long(self._datetimeToMs(event.getStartTime()))
                
    ##############################################
    # Downstream and current polygon bookkeeping #
    ##############################################
    
    def _initializeEvent(self, event):
        # Initialize with event polygon
        mvPolys = event.get('motionVectorPolys')
        if not mvPolys:
            self._setStartTime(event, self._latestDataLayerTime)                
            mvPolys = [event.getGeometry()]
            st = self._convertFeatureTime(0)
            et = self._convertFeatureTime(self._timeStep())
            mvTimes = [(st, et)]
            event.set('motionVectorPolys', mvPolys)
            event.set('motionVectorTimes', mvTimes)
        else:
            self._roundEventTimes(event)
            self._eventSt_ms = long(self._datetimeToMs(event.getStartTime()))
        print "SR event start, current", self._eventSt_ms, self._currentTime
        self.flush()
                        
    def _advanceDownstreamPolys(self, event):
        ''' 
        As the event start time moves forward, 
            move downstreamPolys to historyPolys
        ''' 
        ####### 
        # Downstream Polys
        downstreamPolys = event.get('downstreamPolys', [])
        downstreamTimes = event.get('downstreamTimes', [])
        if not downstreamPolys or not downstreamTimes:
            return
        index = 0
        for i in range(len(downstreamTimes)):           
            times = downstreamTimes[i]
            if times is None:
                print "SwathRecommender Warning: downstream times None", downstreamTimes
                self.flush()
                return
            
            st, et = downstreamTimes[i]
            print "SR Advance downstream st, eventSt_ms", st >= self._eventSt_ms, st, self._eventSt_ms
            self.flush() 
            if st >= self._eventSt_ms:    
                index = i
                break
        print "SR Advance index", index
        self.flush()
        
        event.set('downstreamPolys', downstreamPolys[index:])
        event.set('downstreamTimes', downstreamTimes[index:])
        
        ######
        #  History Polys        
        historyPolys = event.get('historyPolys', [])
        historyTimes = event.get('historyTimes', [])
        newHistPolys = historyPolys + downstreamPolys[:index]
        newHistTimes = historyTimes + downstreamTimes[:index]
        
        # Truncate historyPolys if there is a maximum limit set
        maxHistory = self._historySteps()
        if maxHistory and len(newHistPolys) > maxHistory:
            index = len(newHistPolys) - maxHistory
            newHistPolys = newHistPolys[index:]
            newHistTimes = newHistTimes[index:]
        event.set('historyPolys', newHistPolys)
        event.set('historyTimes', newHistTimes)
         
    def _setEventGeometry(self, event, polygon):
        polygon = self._reducePolygon(polygon)
        event.setGeometry(polygon)
        
    def _reducePolygon(self, initialPoly):
   
        numPoints = self._hazardPointLimit()
        tolerance = 0.001
        newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
        while len(newPoly.exterior.coords) > numPoints:
            tolerance += 0.001
            newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
            
        return newPoly    

    def _relocatePolygon(self, newCentroid, initialPoly):
        if isinstance(initialPoly, shapely.geometry.collection.GeometryCollection):
            initialPoly = initialPoly.geoms[0]
        initialPoly_gglGeom = so.transform(self._c4326t3857, initialPoly)
        newCentroid_gglGeom = so.transform(self._c4326t3857, newCentroid)
        xdis = newCentroid_gglGeom.x-initialPoly_gglGeom.centroid.x
        ydis = newCentroid_gglGeom.y-initialPoly_gglGeom.centroid.y
        newPoly_gglGeom = sa.translate(initialPoly_gglGeom,\
                                                xoff=xdis, yoff=ydis)
        newPoly = so.transform(self._c3857t4326, newPoly_gglGeom)
        return newPoly

    ##################################
    # Check for Update of Attributes #
    ##################################

    def _needUpdate(self, event):
        update = False
        
        # Compare the previous values to the new values to determine if any changes are necessary.  
        # Update the previous values along the way.
        triggerCheckList = ['convectiveObjectSpdKtsUnc', 'convectiveObjectDirUnc', 'convectiveProbTrendGraph',
                            'convectiveObjectDir', 'convectiveObjectSpdKts', 'convectiveSwathPresets', 
                            'duration']
                
        attrs = event.getHazardAttributes()
        
        newTriggerAttrs = {t:attrs.get(t) for t in triggerCheckList}
        newTriggerAttrs['duration'] = self._calcEventDuration(event)
        
        # Get the values from the MetaData. These should supercede and update the ones stored in the event
        self._updateConvectiveAttrs(event) 
                
        for t in triggerCheckList:
            prevName = "prev_"+t
            prevVal = event.get(prevName)
            if t == 'convectiveProbTrendGraph' and prevVal is None:
                prevVal = []
            newVal = newTriggerAttrs.get(t)
            if t == 'convectiveProbTrendGraph' and newVal is None:
                newVal = []
            # Test to see if there is a change
            if prevVal != newVal:
                update = True
                if t == 'duration':
                    self._updateGraphProbsBasedOnDuration(event)
                elif t == 'convectiveProbTrendGraph':
                    self._ensureLastGraphProbZeroAndUneditable(event)
            # Update previous value
            event.set(prevName, newVal)
            
        # Only return true if updating should occur, and ensure that if updating is to occur, there are
        # valid probability trend graph points ready for calculations. There could be none currently
        # because the user had previously commenced a "draw points on graph" action, but did not complete
        # it.
        if update:
            if attrs.get('convectiveProbTrendGraph') == []:
                event.set('convectiveProbTrendGraph', event.get('preDraw_convectiveProbTrendGraph'))
            return True
        return False

    def _updateGraphProbsBasedOnDuration(self, event):
        probVals = []
        probInc = event.get('convectiveProbabilityTrendIncrement', 5)
        duration = self._calcEventDuration(event)
        ### Round up for some context
        for i in range(0, duration+1, probInc):
        #for i in range(0, duration+probInc+1, probInc):
            y = 100-(i*100/int(duration))
            y = 0 if i >= duration else y
            #editable = True if y != 0 else False
            editable = 1 if y != 0 else 0
            probVals.append({"x":i, "y":y, "editable": editable})
        event.set('convectiveProbTrendGraph', probVals)

    def _ensureLastGraphProbZeroAndUneditable(self, event):
        probVals = event.get('convectiveProbTrendGraph', [])
        if len(probVals) == 0:
            return
        lastPoint = probVals[-1]
        if lastPoint["y"] != 0 or lastPoint["editable"]:
            probVals[-1] = { "x": lastPoint["x"], "y": 0, "editable": False }
            event.set('convectiveProbTrendGraph', probVals)

    def _calcEventDuration(self, event):
        # This will round to the nearest minute
        startTime = self._roundTime(event.getStartTime())
        endTime = self._roundTime(event.getEndTime())
        durationMinutes = int((endTime-startTime).total_seconds()/60)
        #durationMinutes = int(round((endTime-startTime).total_seconds() / 60))
        return durationMinutes
    
    def _updateConvectiveAttrs(self, event):
        convectiveAttrs = event.get('convectiveAttrs')
        if not convectiveAttrs:
            return
        if 'wdir' in convectiveAttrs:
            event.set('convectiveObjectDir', convectiveAttrs['wdir'])
        if 'wspd' in convectiveAttrs:
            event.set('convectiveObjectSpdKts', convectiveAttrs['wspd'])

    ###############################
    # Compute Motion Vector       #
    ###############################

    def computeMotionVector(self, polygonTuples, currentTime, defaultSpeed=32, defaultDir=270):
        '''
        polygonTuples is a list of tuples expected as:
        [(poly1, startTime1), (poly2, startTime2),,,,(polyN, startTimeN)]
        '''
        meanU = None
        meanV = None
        uList = []
        vList = []

        ### Sort polygonTuples by startTime
        sortedPolys = sorted(polygonTuples, key=lambda tup: tup[1])

        ### Get create sorted list of u's & v's
#        for i in range(len(sortedPolys)):
#            if i == 0:
#                ### Use default motionVector. 
#                ### Note, need to invert dir since Meteorological winds
#                ### by definition are entered in as *from*
#                speed = defaultSpeed*0.514444
#                bearing = (180+defaultDir)%360
#                u, v = self.get_uv(speed, bearing)
#            else: ### use i, i-1 pair
#                p1 = sortedPolys[i-1][0]
#                t1 = sortedPolys[i-1][1]
#                p2 = sortedPolys[i][0]
#                t2 = sortedPolys[i][1]
#                dist = self.getHaversineDistance(p1, p2)
#                speed = dist/((t2-t1)/1000)
#                bearing = self.getBearing(p1, p2)
#                u, v = self.get_uv(speed, bearing)
#
#            uList.append(u)
#            vList.append(v)

        ### Get create sorted list of u's & v's
        for i in range(1, len(sortedPolys)):
            p1 = sortedPolys[i-1][0]
            t1 = sortedPolys[i-1][1]
            p2 = sortedPolys[i][0]
            t2 = sortedPolys[i][1]
            dist = self.getHaversineDistance(p1, p2)
            speed = dist/((t2-t1)/1000)
            bearing = self.getBearing(p1, p2)
            u, v = self.get_uv(speed, bearing)

            uList.append(u)
            vList.append(v)

            
        uStatsDict = self.weightedAvgAndStdDev(uList)
        vStatsDict = self.weightedAvgAndStdDev(vList)

        meanU = uStatsDict['weightedAverage']
        stdU = uStatsDict['stdDev']

        meanV = vStatsDict['weightedAverage']
        stdV = vStatsDict['stdDev']

        meanDir = atan2(-1*meanU,-1*meanV) * (180 / math.pi)
        meanSpd = math.sqrt(meanU**2 + meanV**2)

        ### Default Uncertainties
        if len(uList) == 1:
            stdDir = 12
            stdSpd = 2.16067
                   
        stdDir = atan2(stdV, stdU) * (180 / math.pi)
        stdDir = 45 if stdDir < 45 else stdDir
        stdDir = 12 if stdDir < 12 else stdDir

        stdSpd = math.sqrt(stdU**2 + stdV**2)
        stdSpd = 10.2889 if stdSpd > 10.2889 else stdSpd
        stdSpd = 2.16067 if stdSpd < 2.16067 else stdSpd

        meanSpd = 102 if meanSpd > 102 else meanSpd

        
        return {
                'convectiveObjectDir' : meanDir%360,
                'convectiveObjectDirUnc' : (stdDir%360)/2,
                'convectiveObjectSpdKts' : meanSpd*1.94384,
                'convectiveObjectSpdKtsUnc' : (stdSpd*1.94384)/2
                }    

    def weightedAvgAndStdDev(self, xList):
        arr = np.array(xList)
        weights = range(1,len(arr)+1)
        weightedAvg = np.average(arr, weights=weights)
        weightedVar = np.average((arr-weightedAvg)**2, weights=weights)
        return {'weightedAverage':weightedAvg, 'stdDev': math.sqrt(weightedVar)}

    def get_uv(self, Spd, DirGeo):
        '''
        from https://www.eol.ucar.edu/content/wind-direction-quick-reference
        '''
        RperD = (math.pi / 180)
        DirGeo = (180+DirGeo)%360
        Ugeo = (-1*Spd) * sin(DirGeo * RperD)
        Vgeo = (-1*Spd) * cos(DirGeo * RperD)
        return Ugeo, Vgeo

    def weightedMean(self, xList):
        numerator = 0
        denomenator = 0
        for i, val in enumerate(xList):
            numerator += ((i+1)*val)
            denomenator += (i+1)
        return numerator/denomenator

    def getBearing(self, poly1, poly2):
        lat1 = poly1.centroid.y
        lon1 = poly1.centroid.x
        lat2 = poly2.centroid.y
        lon2 = poly2.centroid.x
        # convert decimal degrees to radians 
        lon1, lat1, lon2, lat2 = map(math.radians, [lon1, lat1, lon2, lat2])

        bearing = atan2(sin(lon2-lon1)*cos(lat2), cos(lat1)*sin(lat2)-sin(lat1)*cos(lat2)*cos(lon2-lon1))
        bearing = degrees(bearing)
        bearing = bearing % 360
        return bearing
    
    def getHaversineDistance(self, poly1, poly2):
        """
        Calculate the great circle distance between two points 
        on the earth (specified in decimal degrees)
        """
        lat1 = poly1.centroid.y
        lon1 = poly1.centroid.x
        lat2 = poly2.centroid.y
        lon2 = poly2.centroid.x
        # convert decimal degrees to radians 
        lon1, lat1, lon2, lat2 = map(math.radians, [lon1, lat1, lon2, lat2])
        # haversine formula 
        dlon = lon2 - lon1
        dlat = lat2 - lat1
        a = math.sin(dlat/2)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon/2)**2
        c = 2 * math.asin(math.sqrt(a))
        meters = 6367000 * c # 6367000 Earth's radius in meters
        return meters

    def sind(self, x):
        return sin(radians(x))

    def cosd(self, x):
        return cos(radians(x))
    

    ######################################################
    # Compute Interval (downstream and upstream Polygons #
    ######################################################
            
    def _createIntervalPolys(self, event, eventSetAttrs, timeDirection='downstream'):
        '''
        This method creates the downstream or upstream polygons given 
          -- current time polygon 
          -- a direction and direction uncertainty
          -- a speed and a speed uncertainty
          -- a Preset Choice
                
        From the downstreamPolys, the visualFeatures (swath, trackpoints, and upstream polygons)
        can be derived.
        
        '''        
        attrs = event.getHazardAttributes()

        # Set up direction and speed values
        ### get dir
        dirVal = attrs.get('convectiveObjectDir')
        if not dirVal:
            dirVal = self._defaultWindDir()
        dirVal = int(dirVal)
        ### get dirUncertainty (degrees)
        dirUVal = attrs.get('convectiveObjectDirUnc')
        if dirUVal:
            dirUVal = int(dirUVal)
        else:
            dirUVal = 12
        ### get speed
        speedVal = attrs.get('convectiveObjectSpdKts')
        if not speedVal:
            speedVal = self._defaultWindSpeed()
        speedVal = int(speedVal)
        # get speedUncertainty
        spdUVal = attrs.get('convectiveObjectSpdKtsUnc')
        if spdUVal:
            spdUVal = int(spdUVal)
        else:
            spdUVal = int(2.16067*1.94384)
                            
        ### Get initial polygon.  
        # This represents the polygon at the current time resulting from the last nudge.
        poly = event.getGeometry()
        
        ### Check if single polygon or iterable -- no longer need this because swath is a
        # visual feature.
        if hasattr(poly,'__iter__'):
            poly = poly[0]
            
        presetChoice = attrs.get('convectiveSwathPresets') if attrs.get('convectiveSwathPresets') is not None else 'NoPreset'
        presetMethod = getattr(self.sp,presetChoice )
                
        ### convert poly to Google Coords to make use of Karstens' code
        fi_filename = os.path.basename(getframeinfo(currentframe()).filename)
        total = time.time()
        gglPoly = so.transform(self._c4326t3857,poly)
        
        ### calc for intervals (default time step) over duration
        if timeDirection == 'downstream':
            durationSecs = int(round((event.getEndTime()-event.getStartTime()).total_seconds()))
            numIvals = int(durationSecs/self._timeStep())
        else:
            numIvals = self._upstreamTimeSteps()
            
        intervalPolys = []
        intervalTimes = []
        self.flush()
        for step in range(numIvals):
            origDirVal = dirVal
            if timeDirection == 'upstream':
                increment = -1*(step + 1)
                secs = increment * self._timeStep()
                upstreamCentroid = self.computeUpstreamCentroid(poly.centroid, dirVal, speedVal, secs)
                intervalPoly = self._relocatePolygon(upstreamCentroid, poly)
            else:
                increment = step
                presetResults = presetMethod(speedVal, dirVal, spdUVal, dirUVal, step, numIvals)
                
                dirValLast = presetResults['dirVal']
                if step > 0:
                    prevPresetResults = presetMethod(speedVal, dirVal, spdUVal, dirUVal, step-1, numIvals)
                    dirValLast = prevPresetResults['dirVal']
                
                secs = increment * self._timeStep()
                gglDownstream = self._downstream(secs,
                                            presetResults['speedVal'],
                                            presetResults['dirVal'],
                                            presetResults['spdUVal'],
                                            presetResults['dirUVal'],
                                            dirValLast,
                                            gglPoly)
                intervalPoly = so.transform(self._c3857t4326, gglDownstream)
            intervalPoly = self._reducePolygon(intervalPoly)
            intervalPolys.append(intervalPoly)
            st = self._convertFeatureTime(secs)
            et = self._convertFeatureTime(secs+self._timeStep())
            #print "SR interval times", timeDirection, secs, self._displayMsTime(st), self._displayMsTime(et)
            #self.flush()
            intervalTimes.append((st, et))
                    
        if timeDirection == 'downstream':
            event.addHazardAttribute('downstreamPolys',intervalPolys)       
            event.addHazardAttribute('downstreamTimes',intervalTimes) 
        else:
            self._upstreamPolys = intervalPolys
            self._upstreamTimes = intervalTimes      
        #print '[', fi_filename, getframeinfo(currentframe()).lineno,'] === FINAL took ', time.time()-total, 'seconds for ', event.get('objectID'), ' ==='

    def computeUpstreamCentroid(self, centroid, dirDeg, spdKts, time):
        diffSecs = abs(time)
        d = (spdKts*0.514444*diffSecs)/1000
        R = 6378.1 #Radius of the Earth

        brng = radians(dirDeg)
        lon1, lat1 = centroid.coords[0]

        lat2 = math.degrees((d/R) * math.cos(brng)) + lat1
        lon2 = math.degrees((d/(R*math.sin(math.radians(lat2)))) * math.sin(brng)) + lon1

        newCentroid = shapely.geometry.point.Point(lon2, lat2)
        return newCentroid

    def _downstream(self, secs, speedVal, dirVal, spdUVal, dirUVal, dirValLast, threat):
        speedVal = float(speedVal)
        dirVal = float(dirVal)
        dis = secs * speedVal * 0.514444444
        xDis = dis * math.cos(math.radians(270.0 - dirVal))
        yDis = dis * math.sin(math.radians(270.0 - dirVal))
        xDis2 = secs * spdUVal * 0.514444444
        yDis2 = dis * math.tan(math.radians(dirUVal))
        threat = sa.translate(threat,xDis,yDis)
        rot = dirValLast - dirVal
        threat = sa.rotate(threat,rot,origin='centroid')
        rotVal = -1 * (270 - dirValLast)
        if rotVal > 0:
            rotVal = -1 * (360 - rotVal)

        threat = sa.rotate(threat,rotVal,origin='centroid')
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
        rotVal = 270 - dirValLast
        if rotVal < 0:
            rotVal = rotVal + 360
        threat = sa.rotate(threat,rotVal,origin='centroid')

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


    ###############################
    # Visual Features and Nudging #
    ###############################  
      
    def _adjustForVisualFeatureChange(self, event, eventSetAttrs):
        '''
        Based on the visual feature change, update the motion vector
                      
        Update event geometry i.e. polygon at event start time, if changed        
        '''
        # Add the visualFeature change to the motion vector polygons       
        motionVectorPolys = event.get('motionVectorPolys', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
            
        selectedFeatures = event.getSelectedVisualFeatures()
        baseFeatures = event.getBaseVisualFeatures()
        if not selectedFeatures: selectedFeatures = []
        if not baseFeatures: baseFeatures = []
        features = selectedFeatures + baseFeatures
        # We are only changing one attribute per SwathRecommender execution
        changedIdentifier = list(eventSetAttrs.get('attributeIdentifiers'))[0]
        if changedIdentifier.find('swathRec_') != 0:
            return

        for feature in features:
            featureIdentifier = feature.get('identifier')
            # Find the feature that has changed
            if featureIdentifier == changedIdentifier:
                # Get feature polygon
                polyDict = feature["geometry"]
                #  This will work because we only have one polygon in our features
                #  TODO refactor to have multiple polygons per feature
                for timeBounds, geometry in polyDict.iteritems():
                    featureSt, featureEt = timeBounds
                    featureSt = long(featureSt)
                    featurePoly = geometry
                # Add the feature to the motionVectorPolys 
                motionVectorPolys, motionVectorTimes = self._updatePolys(motionVectorPolys, motionVectorTimes, featurePoly, featureSt, featureEt)
                event.set('motionVectorPolys', motionVectorPolys)
                event.set('motionVectorTimes', motionVectorTimes)
                                     
                # If the feature is prior to the event start time, 
                #   add it to the history list
                if featureSt < self._eventSt_ms:
                    histPolys = event.get('historyPolys', []) 
                    histTimes = event.get('historyTimes', [])
                    histPolys, histTimes = self._updatePolys(histPolys, histTimes, featurePoly, featureSt, featureEt)
                    event.set('historyPolys', histPolys)
                    event.set('historyTimes', histTimes)
                    
                # If the feature is at the event start time, add it to the 
                #  event geometry
                if abs(featureSt-self._eventSt_ms) <= self._timeDelta_ms():
                    event.setGeometry(featurePoly)
                                        
        print "SR Feature ST", featureSt, self._displayMsTime(featureSt)
        self.flush()
        
        if len(motionVectorPolys) <= 1:
            return
                                      
        # Re-compute the motion vector and uncertainty
        motionVectorTuples = []
        for i in range(len(motionVectorPolys)):
            poly = motionVectorPolys[i]
            st, et = motionVectorTimes[i]
            motionVectorTuples.append((poly, st, et))
            
        print "SR motionVectorTuples", len(motionVectorTuples)
        self.flush()
            
        newMotion = self.computeMotionVector(motionVectorTuples, self._eventSt_ms, #self._currentTime,                     
                   event.get('convectiveObjectSpdKts', self._defaultWindSpeed()),
                   event.get('convectiveObjectDir',self._defaultWindDir())) 
        for attr in ['convectiveObjectDir', 'convectiveObjectSpdKts',
                      'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc']:
            event.set(attr, int(newMotion.get(attr)))

    def _updatePolys(self, polys, times, newPoly, newSt, newEt):
        # Add the newPoly to the list of polys and times
        # If one currently exists at the newSt, replace it
        # Keep the lists in time order
    
        # Convert to tuples
        tuples = []
        for i in range(len(polys)):
            poly = polys[i]
            st, et = times[i]
            tuples.append((poly, st, et))

        newTuples = []
        found = False
        for poly, st, et in tuples:
            if abs(st-newSt) <= self._timeDelta_ms():                        
                newTuples.append((newPoly, st, et))
                found = True
            else: 
                newTuples.append((poly, st, et))
        # Otherwise, add a new motionVectorPoly 
        if not found:                    
            newTuples.append((newPoly, newSt, newEt))
 
        newTuples.sort(self._sortPolys)                
        newPolys = [poly for poly, st, et in newTuples]
        newTimes = [(st,et) for poly, st, et in newTuples]            
        return newPolys, newTimes         

    def _setVisualFeatures(self, event):
        downstreamPolys = event.get('downstreamPolys')
        if not downstreamPolys:
            return
        
        baseVisualFeatures = [] 
        selectedVisualFeatures = [] 
        upstreamSt_ms = self._eventSt_ms - (self._timeStep() * 1000 * self._upstreamTimeSteps())
        upstreamSt_ms = self._roundTime_ms(upstreamSt_ms)
        
        # Down Stream Features -- polygons, track points, relocated last motionVector
        dsBaseVisualFeatures, dsSelectedVisualFeatures = self._downstreamVisualFeatures(
                                    event, upstreamSt_ms)
        baseVisualFeatures += dsBaseVisualFeatures
        selectedVisualFeatures += dsSelectedVisualFeatures
        
        # Swath
        downstreamTimes = event.get('downstreamTimes')
        #print '>>>SR Calling Swath Draw: dsTime[0], upstreamSt_ms', downstreamTimes[0], upstreamSt_ms
        swathFeature = self._swathFeature(event, upstreamSt_ms, downstreamPolys)                
        selectedVisualFeatures.append(swathFeature) 
        
        # Motion vector centroids
        motionVectorFeatures = self._motionVectorFeatures(event, upstreamSt_ms)     
        selectedVisualFeatures += motionVectorFeatures 
        
        # Previous Time Features
        previousFeatures = self._previousTimeVisualFeatures(event)
        selectedVisualFeatures += previousFeatures
        
        # Preview Grid
        previewGridFeatures = self._getPreviewGridFeatures(event, upstreamSt_ms)
        selectedVisualFeatures += previewGridFeatures
                    
                
        # Replace Visual Features                   
        if baseVisualFeatures:
            event.setBaseVisualFeatures(VisualFeatures(baseVisualFeatures))
        if selectedVisualFeatures:
            event.setSelectedVisualFeatures(VisualFeatures(selectedVisualFeatures))
            
        if self._printVisualFeatures:
             self._printFeatures(event, "Base Visual Features", baseVisualFeatures)
             self._printFeatures(event, "Selected Visual Features", selectedVisualFeatures)

    def _screenFeatures(self, features, identifierStr):
        # Screen out the features containing the identifierStr as a prefix
        existingFeatures = []
        if features is None:
            return existingFeatures
        for feature in features:
            index = feature.get('identifier').find(identifierStr)
            if index != 0:
                existingFeatures.append(feature)
        return existingFeatures
        
    def _downstreamVisualFeatures(self, event, upstreamSt_ms):
        downstreamPolys = event.get('downstreamPolys')
        if not downstreamPolys:
            return
        downstreamTimes = event.get('downstreamTimes')
        geometry = event.getGeometry() 
        
        selectedVisualFeatures = []
        baseVisualFeatures = []
        # Downstream Polygons, Track Points, Relocated Downstream 
        numIntervals = len(downstreamPolys) 
        print "SR Number of downstream polys", numIntervals
        self.flush()       
        for i in range(numIntervals):
            poly = downstreamPolys[i]
            polySt_ms, polyEt_ms = downstreamTimes[i]
            polyEnd = polyEt_ms - 60*1000 # Take a minute off
   
#             # First downstream polygon is at current time and always editable until Issued
#             if self._pendingHazard and i == 0:
#                 dragCapability = "all"
# #                 print "SR Current Time", self._displayMsTime(self._currentTime)
# #                 print "SR First Polygon (editable) time set", self._displayMsTime(polySt_ms), self._displayMsTime(polyEnd)
# #                 self.flush() 
#             else:
#                 dragCapability = "none"
            dragCapability = "none"
              
            downstreamFeature = {
              "identifier": "swathRec_downstream_"+str(polySt_ms),
              "borderColor": "eventType",
              "borderThickness": "eventType",
              "borderStyle": "eventType",
              "textSize": "eventType",
              "label": str(event.get('objectID')) + " " + event.getHazardType(),
              "textColor": "eventType",
              "dragCapability": dragCapability, 
              "geometry": {
                   (polySt_ms, polyEnd): poly
                  }
                }
            baseVisualFeatures.append(downstreamFeature)
               
            # Track Points 
            centroid = poly.centroid
            color = self._getInterpolatedProbTrendColor(event, i, numIntervals)
            trackPointFeature = {
              "identifier": "swathRec_trackPoint_"+str(polySt_ms),
              "borderColor": color,
              "borderThickness": 2,
              "diameter": 5,
              "geometry": {
                  (upstreamSt_ms,
                   VisualFeatures.datetimeToEpochTimeMillis(event.getEndTime())):
                   centroid
               }
            }
            selectedVisualFeatures.append(trackPointFeature)
             
            # Relocated initial going downstream
            if i == 0:
                #print "SR -- Relocated Polygon check [", str(i), "]: ", polySt_ms > self._eventSt_ms, str(polySt_ms), str(self._eventSt_ms)
                self.flush()
            if polySt_ms >= self._eventSt_ms:
                if i == 0:
                    dragCapability = "all"
                    print "SR EDITABLE First Relocated Polygon time set", str(polySt_ms), self._currentTime
                    print " poly time, current time", self._displayMsTime(polySt_ms), self._displayMsTime(self._currentTime)
                    self.flush() 
                else:
                    dragCapability = "none"
                    
                relocatedPoly = self._reducePolygon(self._relocatePolygon(centroid, geometry))
                relocatedFeature = {
                  "identifier": "swathRec_relocated_"+str(polySt_ms),
                  "borderColor": "eventType",
                  "borderThickness": "eventType",
                  "borderStyle": "dashed",
                  "textSize": "eventType",
                  "dragCapability": dragCapability, 
                  "geometry": {
                      (polySt_ms, polyEnd): relocatedPoly
                       }
                }
                selectedVisualFeatures.append(relocatedFeature)
                
        return baseVisualFeatures, selectedVisualFeatures

    def _swathFeature(self, event, upstreamSt_ms, downstreamPolys):
        envelope = shapely.ops.cascaded_union(downstreamPolys)
        swath = {
              "identifier": "swathRec_swath",
              "borderColor": { "red": 1, "green": 1, "blue": 0 },
              "borderThickness": 3,
              "borderStyle": "dotted",
              "geometry": {
                  (upstreamSt_ms,
                   VisualFeatures.datetimeToEpochTimeMillis(event.getEndTime())):
                   envelope
               }
              }
        return swath

    def _motionVectorFeatures(self, event, upstreamSt_ms):
        motionVectorPolys = event.get('motionVectorPolys', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
        features = []
        for i in range(len(motionVectorTimes)):
            st, et = motionVectorTimes[i]
            poly = motionVectorPolys[i]                        
            centroid = poly.centroid
            feature = {
              "identifier": "swathRec_motionVector_"+str(st),
              "borderColor": { "red": 1, "green": 1, "blue": 1 },
              "borderThickness": 2,
              "diameter": 6,
              "geometry": {
                  #(st, et): centroid
                  (upstreamSt_ms,
                   VisualFeatures.datetimeToEpochTimeMillis(event.getEndTime())):
                   centroid
               }
            }
            features.append(feature)
        return features

    def _getPreviewGridFeatures(self, event, upstreamSt_ms):
        if not event.get('showGrid'): 
            return []
        gridFeatures = []            
        probGrid, lons, lats = ProbUtils().getProbGrid(event)
        polyTupleDict = self._createPolygons(probGrid, lons, lats)
                
        # Generate and add preview-grid-related visual features        
        for key in polyTupleDict: 
            upperVal = str(int(key) + 20)
            try:
                poly = GeometryFactory.createPolygon(polyTupleDict[key],[polyTupleDict[upperVal]])
            except KeyError:
                poly = GeometryFactory.createPolygon(polyTupleDict[key])                       
                
            print "SR previewGrid poly", type(poly)
            self.flush()                       
                                
            ### Should match PHI Prototype Tool
            colorFill =  {
                '0': { "red": 102/255.0, "green": 224/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '20': { "red": 255/255.0, "green": 255/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '40': { "red": 255/255.0, "green": 179/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '60': { "red": 255/255.0, "green": 102/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '80': { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0, "alpha": 0.4 }
                }
            
            if poly.is_valid:
                gridPreviewPoly = {
                    "identifier": "gridPreview_" + key,
                    "borderColor":  colorFill[key], # { "red": 1, "green": 1, "blue": 0 },
                    "fillColor": colorFill[key],
                    "geometry": {
                        (upstreamSt_ms,
                        #(VisualFeatures.datetimeToEpochTimeMillis(event.getStartTime()), 
                         VisualFeatures.datetimeToEpochTimeMillis(event.getEndTime())): poly
                    }
                }
                gridFeatures.append(gridPreviewPoly)
                            
        return gridFeatures
    
    def _createPolygons(self, probGrid, lons, lats):
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
        
    def _previousTimeVisualFeatures(self, event):    
        # Previous time polygons -- prior to current time
        #
        # If time has marched forward from the initial creation of the hazard, we could have
        #  motionVectorPolys, historyPolys, and upstreamPolys (IF still pending)
        #
        # Choose to use in this order of preference:
        #      motionVector polygon
        #      then the history polygon
        #      then if the hazard is pending
        #         the upstream polygon
        #
        # All are editable if the hazard has not been issued yet.
        # 
        #print "\nDoing Previous time polys"
        historyPolys = event.get('historyPolys', [])
        historyTimes = event.get('historyTimes', [])
        motionVectorPolys = event.get('motionVectorPolys', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
        previousFeatures = []
                
        timeSteps = max(self._upstreamTimeSteps(), len(historyPolys))
        if self._pendingHazard:
            dragCapability = 'whole'
            color = { "red": 1, "green": 1, "blue": 0 }
        else:
            dragCapability = 'none'
            color = "eventType"
        print "SR Previous polys pending, drag", self._pendingHazard, dragCapability
        self.flush()
            
        for i in range(timeSteps):
            # Work backwards from current time
            polySt_ms = self._eventSt_ms - (i+1)*self._timeStep()*1000
            polySt_ms = self._roundTime_ms(polySt_ms)
            poly = self._findPreviousPoly(polySt_ms,
                                          motionVectorPolys, motionVectorTimes, 
                                          historyPolys, historyTimes)
            if not poly:
                continue
            polyEt_ms = polySt_ms + ((self._timeStep()-60)*1000)
                                
            previousFeature = {              
              "identifier": "swathRec_previous_"+str(polySt_ms),
              "borderColor":  color, #{ "red": 1, "green": 1, "blue": 0 }, #"eventType", 
              "borderThickness": "eventType",
              "borderStyle": "eventType",
              "dragCapability": dragCapability,
              "textSize": "eventType",
              "label": str(event.get('objectID')) + " " + event.getHazardType(),
              "textColor": "eventType",
              "geometry": {
                  (polySt_ms, polyEt_ms): poly
               }
              }
            previousFeatures.append(previousFeature)
        return previousFeatures


    def _findPreviousPoly(self, polySt_ms, motionVectorPolys, motionVectorTimes, historyPolys, historyTimes):
        for i in range(len(motionVectorTimes)):
            st, et = motionVectorTimes[i]
            if abs(st-polySt_ms) < self._timeDelta_ms():
                return motionVectorPolys[i]
        for i in range(len(historyTimes)):
            st, et = historyTimes[i]
            if abs(st-polySt_ms) < self._timeDelta_ms():
                return historyPolys[i]
        if not self._pendingHazard:
            return None
        for i in range(len(self._upstreamTimes)):
            st, et = self._upstreamTimes[i]
            if abs(st-polySt_ms) < self._timeDelta_ms():
                return self._upstreamPolys[i]
        return None

    ###############################
    # Helper methods              #
    ###############################
    
    ## TO DO -- Refactor these into their own module shared by Recommenders
    def _parseIdentifier(self, baseTime_ms, featureIdentifier):
        featureSt = featureIdentifier.split('_')[1]
        return int(featureSt)
                
    def _roundEventTimes(self, event):
        # Current time rounded to nearest minute
        startTime = self._roundTime(event.getStartTime()) 
        endTime = self._roundTime(event.getEndTime())        
        event.setStartTime(startTime)
        event.setEndTime(endTime)

    def _datetimeToMs(self, datetime):
        return float(time.mktime(datetime.timetuple())) * 1000
    
    def _convertFeatureTime(self, secs):
        # Return millis given the event start time and secs offset
        # Round to minutes
        millis = long(self._eventSt_ms + secs * 1000 )
        return self._roundTime_ms(millis)

    def _convertMsToSecsOffset(self, time_ms, baseTime_ms=0):
        result = time_ms - baseTime_ms
        return int(result / 1000)
    
    def _roundTime(self, dt=None, dateDelta=datetime.timedelta(minutes=1)):
        """Round a datetime object to a multiple of a timedelta
        dt : datetime.datetime object, default now.
        dateDelta : timedelta object, we round to a multiple of this, default 1 minute.
        Author: Thierry Husson 2012 - Use it as you want but don't blame me.
                Stijn Nevens 2014 - Changed to use only datetime objects as variables
        """
        roundTo = dateDelta.total_seconds()    
        if dt is None : dt = datetime.datetime.now()
        seconds = (dt - dt.min).seconds
        # // is a floor division, not a comment on following line:
        rounding = (seconds+roundTo/2) // roundTo * roundTo
        return dt + datetime.timedelta(0,rounding-seconds,-dt.microsecond)
    
    def _roundTime_ms(self, ms):
        dt = datetime.datetime.fromtimestamp(ms/1000.0)
        dt = self._roundTime(dt)
        return VisualFeatures.datetimeToEpochTimeMillis(dt)

    def _timeDelta_ms(self):
        # A tolerance for comparing millisecond times
        return 40*1000
    
    def _displayMsTime(self, time_ms):
        return time.strftime('%Y-%m-%d %H:%M:%S', time.gmtime(time_ms/1000))
        
    def _getInterpolatedProbTrendColor(self, event, interval, numIvals):
        '''
        (range: color) e.g. ((0,20), { "red": 0, "green": 1, "blue": 0 } ), 
                          ((20,40), { "red": 1, "green": 1, "blue": 0 }),
        
        '''
        duration = self._calcEventDuration(event)
        colorsList = event.get('convectiveProbTrendGraph', event.get('preDraw_convectiveProbTrendGraph', []))
        probTrend = [entry.get('y') for entry in colorsList]
        
        probTrendTimeInterval = event.get('convectiveProbabilityTrendIncrement', 5 )
        
        ### Add 1 to duration to get "inclusive" 
        probTrendTimeIntervals = np.arange(len(probTrend))*probTrendTimeInterval
        oneMinuteTimeIntervals = np.arange(0, probTrendTimeIntervals[-1]+1, 1)
        
        if interval >= len(oneMinuteTimeIntervals):
            print "SwathRecommender Warning: Oops1: interval >= len(oneMinuteTimeIntervals)", interval, len(oneMinuteTimeIntervals)
            ### Return white dot
            return self._getProbTrendColor(-1)
        
        oneMinuteProbs = np.interp(oneMinuteTimeIntervals, probTrendTimeIntervals, probTrend)
        prob = oneMinuteProbs[interval]
        
        return self._getProbTrendColor(prob)

    def _getProbTrendColor(self, prob):
        ### Should match PHI Prototype Tool
        colors = self._probTrendColors()
        
        for k, v in colors.iteritems():
            if float(k[0]) <= prob and prob < float(k[1]):
                return v

        return { "red": 1, "green": 1, "blue": 1 }

                
    def _sortPolys(self, p1, p2):
        # Sort polygon tuples by start time
        poly1, st1, et1 = p1
        poly2, st2, et2 = p2
        if st1 < st2:
            return -1
        if st2 < st1:
            return 1
        return 0
            
    def _printFeatures(self, event, label, features):
        print label, event.getEventID(), " = ", str(len(features)), ' ----'
        self.flush()
        #for feature in features:
        #    print feature

    def _printEventSet(self, label, eventSet, eventLevel=1):            
        print label
        if self._printEventSetAttributes:
            # TODO Kevin can we just use print?
            eventSetAttrs = eventSet.getAttributes()
            print "trigger: ", eventSetAttrs.get("trigger")
            print "eventIdentifier: ", eventSetAttrs.get("eventIdentifier")
            print "origin: ", eventSetAttrs.get("origin")
            print "attributeIdentifiers: ", eventSetAttrs.get("attributeIdentifiers")
            #print "eventType: ", eventSetAttrs.get("eventType")
        if eventLevel:
            print "Events:", len(eventSet.getEvents())
            for event in eventSet:
                self._printEvent(None, event, eventLevel)
#             sys.stderr.write("Running swath recommender.\n    trigger: " +
#                          str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
#                          str(eventSet.getAttribute("eventType")) + "\n    hazard ID: " +
#                          str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute: " +
#                          str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
         

    def _printEvent(self, label, event, eventLevel=1):
        if self._printEvent:
            import pprint
            if label: print label
            if eventLevel >=1:
                print event.getEventID(), event.get('objectID')
                print "start, end", event.getStartTime(), event.getEndTime()
                print "userOwned", event.get('convectiveUserOwned')
            if eventLevel >= 2:
                print '=== attrs ==='
                pprint.pprint(event.getHazardAttributes())
                print '=== ... ==='
            if eventLevel >= 3:
                print '=== geometry ==='
                pprint.pprint(event.getGeometry())
                print '=== ... ==='
            self.flush()
                
    def __str__(self):
        return 'Swath Recommender'

    def flush(self):
        import os
        os.sys.__stdout__.flush()


    #########################################
    ### OVERRIDES
    
    def _defaultWindSpeed(self):
        return 32
    
    def _defaultWindDir(self):
        return 270
    
    def _timeStep(self):
        # Time step for downstream polygons and track points
        return 60 # secs

    def _upstreamTimeSteps(self):
        # Number of time steps for upstream polygons
        return 30 

    def _historySteps(self):
        # Maximum number of history polygons to store
        # Older polygons are dropped off as time progresses
        return None
            
    def _probTrendColors(self):
        '''
        Should match PHI Prototype Tool
        (range: color) e.g. ((0,20), { "red": 0, "green": 1, "blue": 0 } ), 
                          ((20,40), { "red": 1, "green": 1, "blue": 0 }),
        
        '''            
        colors = {
            (0,20): { "red": 102/255.0, "green": 224/255.0, "blue": 102/255.0 }, 
            (20,40): { "red": 255/255.0, "green": 255/255.0, "blue": 102/255.0 }, 
            (40,60): { "red": 255/255.0, "green": 179/255.0, "blue": 102/255.0 }, 
            (60,80): { "red": 255/255.0, "green": 102/255.0, "blue": 102/255.0 }, 
            (80,101): { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0 }
        }
        return colors
        
    # TO DO:  The Recommender should access HazardTypes.py for this 
    #   information
    def _hazardPointLimit(self):
        return 20
    
    def _setPrintFlags(self):
        self._printVisualFeatures = False
        self._printEventSetAttributes = True
        self._printEventSetEvents = True
        self._printEvents = True


    #########################################
# REMOVE ME when stable    
#         # Old code to go over with Kevin
#         # Set up direction and speed values
#         trigger = eventSetAttrs.get('trigger')
#         if trigger == 'hazardEventModification':
#             ### get dir
#             dirVal = attrs.get('convectiveObjectDir')
#             # What if not dirVal?  Then what would we use?
#             if dirVal:
#                 dirVal = int(dirVal)
#                 ### If the user adjusts the DIR, save that adjustment to be picked up by the 
#                 ### automated call (PHI Grid Recommender)
#                 # Tracy: Why can't the PHI Grid Recommender just use the convectiveObjectDir directly?
#                 if probSevereAttrs:
#                     probSevereAttrs['wdir'] = dirVal
#                 else:
#                     probSevereAttrs = {'wdir':dirVal}
#                 event.set('probSeverAttrs', probSevereAttrs)
#                 
#             # There is potential for a bug in this code such that probSevereAttrs only has one or 
#             # other of the wdir, wspd values and results in a crash somewhere down the road.
#             
#             ### get dirUncertainty (degrees)
#             dirUVal = attrs.get('convectiveObjectDirUnc')
#             if dirUVal:
#                 dirUVal = int(dirUVal)
#             else:
#                 dirUVal = 10
#             ### get speed
#             speedVal = attrs.get('convectiveObjectSpdKts')
#             if speedVal:
#                 speedVal = int(speedVal)
#                 ### If the user adjusts the SPD, save that adjustment to be picked up by the 
#                 ### automated call (PHI Grid Recommender)
#                 #  Tracy: Same question as above
#                 if probSevereAttrs: 
#                     probSevereAttrs['wspd'] = speedVal
#                 else:
#                     probSevereAttrs = {'wspd':speedVal}
#                 event.set('probSeverAttrs', probSevereAttrs)
#                 ### get speedUncertainty (kts)
#             spdUVal = attrs.get('convectiveObjectSpdKtsUnc')
#             if spdUVal:
#                 spdUVal = int(spdUVal)
#             else:
#                 spdUVal = 10
#             #print 'SwathRec HandDrawn...', dirVal, speedVal, event.get('probSeverAttrs')
#             # This is not necessarily handdrawn
# #         else:  # I don't think this will happen -- 
# #             dirVal = probSevereAttrs.get('wdir')
# #             speedVal = probSevereAttrs.get('wspd')
# #             dirUVal = 10
# #             spdUVal = 10
# #             return False
# #             #print 'SwathRec Automated...', dirVal, speedVal, event.get('probSeverAttrs')

                
