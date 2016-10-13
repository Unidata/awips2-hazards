'''
Utility for PHIGridRecommender and PreviewGridRecommender
'''
import numpy as np
import datetime, math
import time
#from math import *
import shapely
import shapely.ops as so
import shapely.geometry as sg
import shapely.affinity as sa

import os, sys
import matplotlib
from matplotlib import path as mPath
from scipy import ndimage
from shapely.geometry import Polygon
from scipy.io import netcdf
from collections import defaultdict
from shutil import copy2
import HazardDataAccess
import TimeUtils, LogUtils
import AdvancedGeometry
from VisualFeatures import VisualFeatures

class ProbUtils(object):
    def __init__(self):
        self.setUpDomainValues()
        self.setUpDomain()
        
    def processEvents(self, eventSet, writeToFile=False):
        if writeToFile and not os.path.exists(self._OUTPUTDIR):
            try:
                os.makedirs(self._OUTPUTDIR)
            except:
                sys.stderr.write('Could not create PHI grids output directory:' +self._OUTPUTDIR+ '.  No output written')


        probSvrSnapList = []
        probSvrSwathList = []
        probTorSnapList = []
        probTorSwathList = []
        
        ### Note: this is one way to round down minutes... 
        #timeStamp = eventSet.getAttributes().get("issueTime").replace(second=0)
        timestamp = (long(eventSet.getAttributes().get("currentTime"))/1000)
        timestamp = datetime.datetime.utcfromtimestamp(timestamp)
        timeStamp = timestamp.replace(second=0)

        
        for event in eventSet:
            
            ### Kludgey fix for HWT Week 3
            #if event.getEndTime() <= datetime.datetime.utcfromtimestamp(eventSet.getAttributes().get("currentTime")/1000):
            if event.getEndTime() <= timestamp:
                continue
            
            if event.getStatus().upper() != 'ISSUED':
                continue
            
            hazardType = event.getHazardType()
            probGridSnap, probGridSwath = self.getOutputProbGrids(event, timeStamp)
            if hazardType == 'Prob_Severe':
                probSvrSnapList.append(probGridSnap)
                probSvrSwathList.append(probGridSwath)
            if hazardType == 'Prob_Tornado':
                probTorSnapList.append(probGridSnap)
                probTorSwathList.append(probGridSwath)

        if writeToFile:
            if len(probSvrSnapList) > 0:
                #snapCum = np.array(probSvrSnapList)
                #probSvrSnap = np.max(snapCum, axis=0)
                #swathCum = np.array(probSvrSwathList)
                #probSvrSwath = np.max(swathCum, axis=0)
                probSvrSnap = np.max(np.array(probSvrSnapList), axis=0)
                probSvrSwath = np.max(np.array(probSvrSwathList), axis=0)
                self._output(probSvrSnap, probSvrSwath, timeStamp, 'Severe')         
            if len(probTorSnapList) > 0:
                probTorSnap = np.max(np.array(probTorSnapList), axis=0)
                probTorSwath = np.max(np.array(probTorSwathList), axis=0)
                self._output(probTorSnap, probTorSwath, timeStamp, 'Tornado')         

        return 1


    def getOutputProbGrids(self, event, currentTime):                   
        downstreamPolys = event.get('downstreamPolys')
        if not downstreamPolys:
            return
        downstreamTimes = event.get('downstreamTimes')

        ###  Only send downstream poly's >= "now"
        
        ### FIXME: downstreamTimes is currently a tuple (st, et).  If ever becomes just "st" (no-tuple) will need to change j[0] to j
        ### FIXME: might need to round/buffer for event currently being issued.  For now, the event being issued is still 'PENDING', so we'lll use that.
        firstIdx = 0
        if event.getStatus().upper() == 'ISSUED': 
            firstIdx =  next(i for i,j in enumerate(downstreamTimes) if j[0]/1000 >= int(currentTime.strftime('%s')))
        probTrend = self._getInterpolatedProbTrendColors(event)
        
        probGridSwath = self.makeGrid(downstreamPolys[firstIdx:], probTrend[firstIdx:], self._lons, self._lats, 
                                 self._xMin1, self._xMax1, self._yMax1, self._yMin1)
        probGridSnap = self.makeGrid(downstreamPolys[firstIdx:], probTrend[firstIdx:], self._lons, self._lats, 
                                 self._xMin1, self._xMax1, self._yMax1, self._yMin1, accumulate=False)
        
        
        return (probGridSnap, probGridSwath)
       
    def getProbGrid(self, event):
        downstreamPolys = event.get('downstreamPolys')
         
        if not downstreamPolys:
           return
        
        probTrend = self._getInterpolatedProbTrendColors(event)
        probGridSwath = self.makeGrid(downstreamPolys, probTrend, self._lons, self._lats, 
                                 self._xMin1, self._xMax1, self._yMax1, self._yMin1)
        
        return probGridSwath, self._lons, self._lats
        

    #===========================================================================
    # def _getInterpolatedProbTrendColors(self, event):
    #     '''
    #     (range: color) e.g. ((0,20), { "red": 0, "green": 1, "blue": 0 } ), 
    #                       ((20,40), { "red": 1, "green": 1, "blue": 0 }),
    #     
    #     '''
    #     probVals = event.get('convectiveProbTrendGraph', [])
    #     probTrend = [entry.get('y') for entry in probVals]
    #     
    #     probTrendTimeInterval = event.get('convectiveProbabilityTrendIncrement', 5)
    #     
    #     ### Add 1 to duration to get "inclusive" 
    #     probTrendTimeIntervals = np.arange(len(probTrend))*probTrendTimeInterval
    #     oneMinuteTimeIntervals = np.arange(0, probTrendTimeIntervals[-1]+1, 1)
    #     
    #     oneMinuteProbs = np.interp(oneMinuteTimeIntervals, probTrendTimeIntervals, probTrend)
    #     
    #     return oneMinuteProbs
    #===========================================================================
    
    def _getInterpolatedProbTrendColors(self, event, returnOneMinuteTime=False):
        '''
        (range: color) e.g. ((0,20), { "red": 0, "green": 1, "blue": 0 } ), 
                          ((20,40), { "red": 1, "green": 1, "blue": 0 }),
        
        '''
        colorsList = event.get('convectiveProbTrendGraph', [])
        probTrend = [entry.get('y') for entry in colorsList]

        probTrendTimeInterval = event.get('convectiveProbabilityTrendIncrement', 5)

        ### Add 1 to duration to get "inclusive" 
        probTrendTimeIntervals = np.arange(len(probTrend))*probTrendTimeInterval
        oneMinuteTimeIntervals = np.arange(0, probTrendTimeIntervals[-1]+1, 1)

        oneMinuteProbs = np.interp(oneMinuteTimeIntervals, probTrendTimeIntervals, probTrend)

        if returnOneMinuteTime:
            return {'oneMinuteProbs':oneMinuteProbs, 'oneMinuteTimeIntervals': oneMinuteTimeIntervals}
        else:
            return oneMinuteProbs


    def makeGrid(self, swathPolygon, probTrend, x1, y1, xMin1, xMax1, yMax1, yMin1, accumulate=True):
        '''
        Almost all of the code in this method is pulled from 
        Karstens' (NSSL) PHI Prototype tool code with some minor modifications
        
        swathPolygon: Array of swath polygons in the form of advanced geometries.
        '''        
        probability = np.zeros((len(y1), len(x1)))

        nx1, ny1 = y1.shape[0], x1.shape[0]
        x2,y2 = np.meshgrid(x1,y1)
        x3, y3 = x2.flatten(), y2.flatten()
        pnts1 = np.vstack((x3,y3)).T

        # Translate from advanced geometries to shapely ones. Note
        # that the shapely objects that AdvancedGeometry.asShapely()
        # returns are always geometry collections, so the 0th geometry
        # is extracted.
        advancedGeometries = swathPolygon
        swathPolygon = []
        for geometry in advancedGeometries:
            swathPolygon.append(geometry.asShapely()[0])
        union = so.cascaded_union(swathPolygon)
        llLon = (math.floor(union.bounds[0] * 100) / 100.) - 0.01
        llLat = (math.floor(union.bounds[1] * 100) / 100.) - 0.01
        urLon = (math.ceil(union.bounds[2] * 100) / 100.) + 0.01
        urLat = (math.ceil(union.bounds[3] * 100) / 100.) + 0.01

        # shrink domain if object is on the end
        if llLon < xMin1:
            llLon = xMin1
        if urLon > xMax1:
            urLon = xMax1
        if llLat < yMin1:
            llLat = yMin1
        if urLat > yMax1:
            urLat = yMax1

        # more fiddling
        unionBounds = [(llLon,llLat),(llLon,urLat),(urLon,urLat),(urLon,llLat)]
        x = np.arange(llLon,urLon,0.01)
        y = np.arange(llLat+0.01,urLat+0.01,0.01)
        probLocal = np.zeros((len(y), len(x)))

        nx, ny = y.shape[0], x.shape[0]
        x2,y2 = np.meshgrid(x,y)
        x3, y3 = x2.flatten(), y2.flatten()
        pnts = np.vstack((x3,y3)).T

        obj = map(list,unionBounds)
        pathProjected = mPath.Path(obj) # option 1
        maskBounds = pathProjected.contains_points(pnts1) # option 1
        #maskBounds = points_inside_poly(pnts1, obj) # option 2
        maskBounds = maskBounds.reshape((nx1,ny1))
        mask = np.where(maskBounds == True)
        minY = mask[0].min()
        minX = mask[1].min()

        # iterate through 1-min interpolated objects
        # perform 2D Gaussian with p(t) on points in polygon
        # save accumulated output
        if not accumulate:
            polys = [swathPolygon[0]]
        else:
            polys = swathPolygon
        for k in range(len(polys)):
            probabilitySnap = np.zeros((len(y1), len(x1)))
            obj = map(list,list(polys[k].exterior.coords))
            pathProjected = mPath.Path(obj) # option 1
            maskProjected = pathProjected.contains_points(pnts) # option 1
            #maskProjected = points_inside_poly(pnts, obj) # option 2

            maskProjected= maskProjected.reshape((nx,ny))
            distances = ndimage.distance_transform_edt(maskProjected)
            dMax = distances.max()
            if dMax == 0:
                dMax = 1.                
            try:
                probMap = np.ceil(probTrend[k] - probTrend[k] * np.exp((pow(np.array((distances / dMax) * 1475.0),2) / -2.0) / pow(500,2)))
            except:
                probMap = np.ceil(probTrend[-1] - probTrend[-1] * np.exp((pow(np.array((distances / dMax) * 1475.0),2) / -2.0) / pow(500,2)))
                
            probLocal = np.maximum(probMap, probLocal)

        for i in range(len(mask[0])):
            iy = mask[0][i] - minY
            ix = mask[1][i] - minX
            probability[mask[0][i]][mask[1][i]] = probLocal[iy][ix]
                
        return probability
      
    def _output(self, snap, swath, timeStamp, eventType):
        '''
        Creates an output netCDF file in OUTPUTDIR (set near top)
        '''
        epoch = (timeStamp-datetime.datetime.utcfromtimestamp(0)).total_seconds()
        nowTime = datetime.datetime.utcfromtimestamp(time.time())
        
        
        #outDir = os.path.join(self._OUTPUTDIR, nowTime.strftime("%Y%m%d_%H"), eventType)
        outDir = self._OUTPUTDIR

        if not os.path.exists(outDir):
            try:
                os.makedirs(outDir)
            except:
                sys.stderr.write('Could not create PHI grids output directory:' +outDir+ '.  No output written')

        outputFilename = 'PHIGrid_' + eventType + '_' + timeStamp.strftime('%Y%m%d_%H%M%S') + '.nc'
        pathFile = os.path.join(outDir,outputFilename)
    
        try:
            
            f = netcdf.netcdf_file(pathFile,'w')
            f.title = 'Probabilistic Hazards Information grid'
            f.hazard_type = eventType
            f.institution = 'NOAA Hazardous Weather Testbed; ESRL Global Systems Division and National Severe Storms Laboratory'
            f.source = 'AWIPS2 Hazard Services'
            f.history = 'Initially created ' + nowTime.isoformat()
            f.comment = 'These data are experimental'
            #f.time_origin = timeStamp.strftime("%Y-%m-%d %H:%M:%S")
            f.time_origin = timeStamp.strftime("%Y-%m-%d %H:%M:%S")
            f.time_valid = timeStamp.strftime("%Y-%m-%d %H:%M:%S")
            
            f.createDimension('lats', len(self._lats))
            f.createDimension('lons', len(self._lons))
            f.createDimension('time', 1)
            
            latVar = f.createVariable('lats', 'f', ('lats',))
            latVar.long_name = "latitude"
            latVar.units = "degrees_north"
            latVar.standard_name = "latitude"
            latVar[:] = self._lats
            
            lonVar = f.createVariable('lons', 'f', ('lons',))
            lonVar.long_name = "longitude"
            lonVar.units = "degrees_east"
            lonVar.standard_name = "longitude"
            lonVar[:] = self._lons
            
            timeVar = f.createVariable('time', 'f8', ('time',))
            timeVar.long_name  = 'Valid Time'
            timeVar.units = 'seconds since 1970-01-01 00:00:00'
            timeVar.time_origin = '1970-01-01 00:00:00'
            timeVar[:] = epoch
    
            snapProbsVar = f.createVariable('PHIprobsSnapshot'+eventType, 'f', ('time', 'lats', 'lons'))
            #snapProbsVar = f.createVariable('PHIprobsSnapshot'+eventType, 'f', ('lats', 'lons'))
            snapProbsVar.long_name = "Probabilistic Hazard Information grid probabilities at the given time"
            snapProbsVar.units = "%"
            snapProbsVar.coordinates = "time lat lon"
            #snapProbsVar.coordinates = "lat lon"
            snapProbsVar[:] = snap
    
            swathProbsVar = f.createVariable('PHIprobsSwath'+eventType, 'f', ('time', 'lats', 'lons'))
            #swathProbsVar = f.createVariable('PHIprobsSwath'+eventType, 'f', ('lats', 'lons'))
            swathProbsVar.long_name = "Probabilistic Hazard Information grid probability swaths starting at the given time"
            swathProbsVar.units = "%"
            swathProbsVar.coordinates = "time lat lon"
            #swathProbsVar.coordinates = "lat lon"
            swathProbsVar[:] = swath
    
            f.close()
            
        except:
            e = sys.exc_info()[0] # catch *all* exceptions
            sys.stderr.write('Unable to open PHI Grid Netcdf file for output:'+pathFile)
            sys.stderr.write('Error stacktrace:\n\n%s' % e)
        
        #copy2(pathFile, '/awips2/edex/data/manual')
            
        return pathFile


    ###############################
    # Helper methods              #
    ###############################
       
    def _parseIdentifier(self, baseTime_ms, featureIdentifier):
        featureSt = featureIdentifier.split('_')[1]
        return int(featureSt)
                
    def _roundEventTimes(self, event):
        # Current time rounded to nearest minute
        startTime = TimeUtils.roundDatetime(event.getStartTime()) 
        endTime = TimeUtils.roundDatetime(event.getEndTime())        
        event.setStartTime(startTime)
        event.setEndTime(endTime)
        
    def _getDurationSecs(self, event, truncateAtZero=False):
        return self._getDurationMinutes(event, truncateAtZero) * 60

    def _getDurationMinutes(self, event, truncateAtZero=False):
        # This will round down to the nearest minute
        startTime = TimeUtils.roundDatetime(event.getStartTime())
        endTime = TimeUtils.roundDatetime(event.getEndTime())
        
        if truncateAtZero:
            endTime = self._getZeroProbTime_minutes(event, startTime, endTime)
                    
        durationMinutes = int((endTime-startTime).total_seconds()/60)
        return durationMinutes

    def _getZeroProbTime_minutes(self, event, startTime_minutes=None, endTime_minutes=None):
        # Return the time of the zero probability OR 
        #   the event end time if there is no zero prob found
        if not startTime_minutes:
            startTime_minutes = TimeUtils.roundDatetime(event.getStartTime())
        if not endTime_minutes:
            endTime_minutes = TimeUtils.roundDatetime(event.getEndTime())
            
        # Check for zero value prior to endTime
        graphVals = event.get("convectiveProbTrendGraph")
        if graphVals is not None:
            # Determine the zero value to set the endTime 
            zeroIndex = None
            for i in range(len(graphVals)):
                if graphVals[i] < 1:
                    endIndex = i
                    break
            if zeroIndex and zeroIndex != len(graphVals)-1:
                endTime_minutes = TimeUtils.roundDatetime(startTime_minutes + zeroIndex * self._timeStep()/60)
        return endTime_minutes
    
    def _convertMsToSecsOffset(self, time_ms, baseTime_ms=0):
        result = time_ms - baseTime_ms
        return int(result / 1000)
    
    def _updateGraphValsDuration(self, origGraphVals, newGraphVals):
        
        if len(newGraphVals) <= len(origGraphVals):
            newGraphVals = origGraphVals[0:len(newGraphVals)]
        else:
            origGraphVals[-1]['editable'] = True
            for entry in newGraphVals:
                entry['y'] = 0
            newGraphVals[0:len(origGraphVals)] = origGraphVals
            
        newGraphVals[-1]['y'] = 0
        newGraphVals[-1]['editable'] = False
            
        return newGraphVals            
    
    def _getGraphProbs(self, event, latestDataLayerTime=None):
        ### Get difference in minutes and the probability trend
        previousDataLayerTime = event.get("previousDataLayerTime")
        issueStart = event.get("eventStartTimeAtIssuance")
        graphVals = event.get("convectiveProbTrendGraph")
        currentStart = long(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()))
                
        if graphVals is None:
            #LogUtils.logMessage('[HERE-0]')
            return self._getGraphProbsBasedOnDuration(event)
        
        if issueStart is None:
            issueStart = currentStart
        
        
        ### Need to clean up, but problem with time trend aging off too fast was 
        ### currentProbTrend-previousProbTrend but timediff was currentTime-issueTime
        ###So saving previousDataLayerTime so that our timeDiffs match probTrendDiffs
                
        if latestDataLayerTime is not None and previousDataLayerTime is not None:
            #print '+++ Using latestDataLayerTime-previousDataLayerTime'
            #self.flush()
            previous = datetime.datetime.utcfromtimestamp(previousDataLayerTime/1000)
            latest = datetime.datetime.utcfromtimestamp(latestDataLayerTime/1000)
        else:
            #print '--- Using currentStart-issueStart'
            previous = datetime.datetime.utcfromtimestamp(issueStart/1000)
            latest = datetime.datetime.utcfromtimestamp(currentStart/1000)

        latestDataLayerTime = latestDataLayerTime if latestDataLayerTime is not None else issueStart
        #print 'Setting previousDataLayerTime', latestDataLayerTime
        #self.flush()
        previousDataLayerTime = event.set("previousDataLayerTime", latestDataLayerTime)

        
        inc = event.get('convectiveProbabilityTrendIncrement', 5)
        #print 'Time Types?'
        #print 'previous', type(previous), previous
        #print 'latest', type(latest), latest
        minsDiff = (latest-previous).seconds/60
        #print 'ProbUtils minsDiff', minsDiff
        #self.flush()
        
        ### Get interpolated times and probabilities 
        intervalDict = self._getInterpolatedProbTrendColors(event, returnOneMinuteTime=True)
        oneMinuteProbs = intervalDict.get('oneMinuteProbs')
        oneMinuteTimeIntervals = intervalDict.get('oneMinuteTimeIntervals')
        ### New list of zeros as a placeholder
        updatedProbs = zeros = np.zeros(len(oneMinuteTimeIntervals), dtype='i4')
        
        ### Find where the 1-min times > diff
        indices = np.where(oneMinuteTimeIntervals>=minsDiff)
        ### Get the probs corresponding with remaining time
        remainingProbs = oneMinuteProbs[indices]
        ### put those remaining probs to FRONT of zeros array
        updatedProbs[0:len(remainingProbs)] = remainingProbs
        ### Sample at increment 
        fiveMinuteUpdatedProbs = updatedProbs[::inc].tolist()
        #LogUtils.logMessage('Updated', fiveMinuteUpdatedProbs)
        
        ### update original times with shifted probs, if length of arrays match
        if len(fiveMinuteUpdatedProbs) == len(graphVals):
            for i in range(len(graphVals)):
                graphVals[i]['y'] = fiveMinuteUpdatedProbs[i]
        ### Otherwise, using original inc-times, if we have mismatch in length of probs, 
        ### inform user and return original
        else:
            sys.stderr.write('\n\tError updating ProbTrendGraph. Returning default')
            self.flush()
                        
        ### Challenge is to get the graph to show the "rounded up" value, but we 
        ### don't want to muck with the duration, so still uncertain the best way
        graphVals = self._updateGraphValsDuration(graphVals, self._getGraphProbsBasedOnDuration(event))
        #graphVals = self._updateGraphValsDuration(graphVals, self._getGraphProbsBasedOnDuration(event))
        
        import pprint
#        print 'remainingProbs'
#        pprint.pprint(remainingProbs)
#        print '---'
#        print 'updatedProbs'
#        pprint.pprint(updatedProbs)
#        print '---'
#        print 'fiveMinuteUpdatedProbs'
#        pprint.pprint(fiveMinuteUpdatedProbs)
#        print '---'
        #print 'ProbUtils graphVals'
        #pprint.pprint(graphVals)
#        print type(probTrend), type(probTrend[0])
        #self.flush()
        return graphVals

    def _getGraphProbsBasedOnDuration(self, event):
        probVals = []
        probInc = event.get('convectiveProbabilityTrendIncrement', 5)
        duration = self._getDurationMinutes(event)
        
        ### Round up for some context
        duration = duration+probInc if duration%probInc != 0 else duration
        
        max = 100
        if event.get('probSeverAttrs'):
            max = event.get('probSeverAttrs').get('probabilities')
        
        for i in range(0, duration+1, probInc):
        #for i in range(0, duration+probInc+1, probInc):
            y = max - (i * max / int(duration))
            y = 0 if i >= duration else y
            editable = True if y != 0 else False
            #editable = 1 if y != 0 else 0
            probVals.append({"x":i, "y":y, "editable": editable})
        return probVals

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
        duration = self._getDurationMinutes(event)
        probVals = event.get('convectiveProbTrendGraph', event.get('preDraw_convectiveProbTrendGraph', []))
        probTrend = [entry.get('y') for entry in probVals]
        
        probTrendTimeInterval = event.get('convectiveProbabilityTrendIncrement', 5 )
        
        ### Add 1 to duration to get "inclusive" 
        probTrendTimeIntervals = np.arange(len(probTrend))*probTrendTimeInterval
        oneMinuteTimeIntervals = np.arange(0, probTrendTimeIntervals[-1]+1, 1)
        
        if interval >= len(oneMinuteTimeIntervals):
            print "ProbUtils Warning: Oops1: interval >= len(oneMinuteTimeIntervals)", interval, len(oneMinuteTimeIntervals)
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

    def setUpDomain(self):                            
        self._ulLat = self._initial_ulLat + self._buff
        self._ulLon = self._initial_ulLon - self._buff        
        self._xMin1 = self._ulLon 
        self._xMax1 = self._xMin1 + (0.01 * self._lonPoints) 
        self._yMax1 = self._ulLat 
        self._yMin1 = self._yMax1 - (0.01 * self._latPoints) 
        self._lons = np.arange(self._xMin1,self._xMax1,0.01)
        self._lats = np.arange(self._yMin1+0.01,self._yMax1+0.01,0.01)                


    def _reduceShapeIfPolygon(self, initialShape): 
        '''
        @summary Reduce the shape if it is a polygon to have only the maximum allowable
        number of vertices.
        @param initialShape: Advanced geometry.
        @return Reduced polygon as advanced geometry if the original was a
        polygon, otherwise the original advanced geometry.
        '''  
        if initialShape.isPolygonal() and not initialShape.isPotentiallyCurved():
            rotation = initialShape.getRotation()
            numPoints = self._hazardPointLimit()
            tolerance = 0.001
            initialPoly = initialShape.asShapely()
            if type(initialPoly) is shapely.geometry.collection.GeometryCollection:
                initialPoly = initialPoly[0] 
            newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
            while len(newPoly.exterior.coords) > numPoints:
                tolerance += 0.001
                newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
            
            return AdvancedGeometry.createShapelyWrapper(newPoly, rotation)
        return initialShape

    ###############################
    # Compute Motion Vector       #
    ###############################

    def computeMotionVector(self, polygonTuples, currentTime, defaultSpeed=32, defaultDir=270):
        '''
        @param polygonTuples List of tuples expected as:
        [(poly1, startTime1), (poly2, startTime2),,,,(polyN, startTimeN)]
        with each poly being in advanced geometry form.
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

        meanDir = math.atan2(-1*meanU,-1*meanV) * (180 / math.pi)
        meanSpd = math.sqrt(meanU**2 + meanV**2)

        ### Default Uncertainties
        if len(uList) == 1:
            stdDir = 12
            stdSpd = 2.16067
                   
        stdDir = math.atan2(stdV, stdU) * (180 / math.pi)
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
        Ugeo = (-1*Spd) * math.sin(DirGeo * RperD)
        Vgeo = (-1*Spd) * math.cos(DirGeo * RperD)
        return Ugeo, Vgeo

    def weightedMean(self, xList):
        numerator = 0
        denomenator = 0
        for i, val in enumerate(xList):
            numerator += ((i+1)*val)
            denomenator += (i+1)
        return numerator/denomenator

    def getBearing(self, poly1, poly2):
        '''
        poly1: First polygon in advanced geometry form.
        poly2: Second polygon in advanced geometry form.
        '''
        poly1 = poly1.asShapely()
        poly2 = poly2.asShapely()

        lat1 = poly1.centroid.y
        lon1 = poly1.centroid.x
        lat2 = poly2.centroid.y
        lon2 = poly2.centroid.x
        # convert decimal degrees to radians 
        lon1, lat1, lon2, lat2 = map(math.radians, [lon1, lat1, lon2, lat2])

        bearing = math.atan2(math.sin(lon2-lon1)*math.cos(lat2), math.cos(lat1)*math.sin(lat2)-math.sin(lat1)*math.cos(lat2)*math.cos(lon2-lon1))
        bearing = math.degrees(bearing)
        bearing = bearing % 360
        return bearing
    
    def getHaversineDistance(self, poly1, poly2):
        """
        Calculate the great circle distance between two points 
        on the earth (specified in decimal degrees)
        poly1: First polygon in advanced geometry form.
        poly2: Second polygon in advanced geometry form.
        """
        poly1 = poly1.asShapely()
        poly2 = poly2.asShapely()
        
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
        return math.sin(radians(x))

    def cosd(self, x):
        return math.cos(radians(x))
    

    ######################################################
    # Compute Interval (downstream and upstream Polygons #
    ######################################################
            
    def _createIntervalShapes(self, event, eventSetAttrs, nudge, swathPresetClass, eventSt_ms, 
                              timeIntervals, timeDirection='downstream'):
        '''
        This method creates the downstream or upstream shapes given 
          -- event start time shape
          -- a direction and direction uncertainty
          -- a speed and a speed uncertainty
          -- a Preset Choice
          -- a list of timeIntervals -- list of intervals (in secs) relative to eventSt_ms for
             which to produce downstream or upstream shapes
          -- timeDirection -- 'downstream' or 'upstream'
          
        Note that the timeIntervals will be negative for upstream and positive for downstream
                
        From the downstreamShapes and upstreamShapes, the visualFeatures 
            (swath, trackpoints, and upstream shapes) can be determined.
        
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
                            
        ### Get initial shape.  
        # This represents the shape at the event start time resulting from the last nudge.
        if nudge:
            shape = event.getGeometry()
        else:
            downstreamShapes = event.get('downstreamPolys', [])
            if downstreamShapes:
                shape = downstreamShapes[0]
            else:
                shape = event.getGeometry()
                
        # Convert the shape to a shapely polygon.
        poly = shape.asShapely()
        if type(poly) is shapely.geometry.collection.GeometryCollection:
            poly = poly[0] 
                    
        presetChoice = attrs.get('convectiveSwathPresets') if attrs.get('convectiveSwathPresets') is not None else 'NoPreset'
        presetMethod = getattr(swathPresetClass, presetChoice)
                
        # Convert the shapely polygon to Google Coords to make use of Karstens' code
        gglPoly = so.transform(AdvancedGeometry.c4326t3857, poly)
                    
        intervalShapes = []
        intervalTimes = []
        totalSecs = abs(timeIntervals[-1] - timeIntervals[0]) 
        if not totalSecs:
            totalSecs = timeIntervals[0]
        self._prevDirVal = None
        
        for secs in timeIntervals:
            origDirVal = dirVal
            intervalShape, secs = self._getIntervalShape(secs, totalSecs, shape, gglPoly, 
                                                        speedVal, dirVal, spdUVal, dirUVal, 
                                                        timeDirection, presetMethod) 

            intervalShape = self._reduceShapeIfPolygon(intervalShape)
            intervalShapes.append(intervalShape)
            st = self._convertFeatureTime(eventSt_ms, secs)
            et = self._convertFeatureTime(eventSt_ms, secs+self._timeStep())
            intervalTimes.append((st, et))
                    
        if timeDirection == 'downstream':
            event.addHazardAttribute('downstreamPolys',intervalShapes)       
            event.addHazardAttribute('downstreamTimes',intervalTimes) 
        else:
            event.addHazardAttribute('upstreamPolys',intervalShapes)       
            event.addHazardAttribute('upstreamTimes',intervalTimes)     

    def _getIntervalShape(self, secs, totalSecs, shape, gglPoly, speedVal, dirVal, spdUVal, dirUVal, 
                         timeDirection, presetMethod):
        '''
        @param shape: Shape in advanced geometry form.
        @param gglPoly: Shapely polygon version of the shape using Google coordinates. 
        @return Interval shape in advanced geometry form.
        '''
        if timeDirection == 'upstream':
            presetResults = presetMethod(speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs)
            dirValLast = dirVal
        else:
            presetResults = presetMethod(speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs)            
            dirValLast = presetResults['dirVal']
            if self._prevDirVal:
                dirValLast = self._prevDirVal
            speedVal = presetResults['speedVal']
            dirVal = presetResults['dirVal']
            self._prevDirVal = dirVal
            spdUVal = presetResults['spdUVal']
            dirUVal = presetResults['dirUVal']           
        
        gglDownstream = self._computePoly(secs, speedVal, dirVal, spdUVal, dirUVal,
                            dirValLast, gglPoly)
        intervalPoly = so.transform(AdvancedGeometry.c3857t4326, gglDownstream)
        
        # If the interval is upstream, relocate the original shape using the newly
        # calculated polygon's centroid; otherwise, use the newly calculated polygon
        # as the interval. Downstream shapes are never ellipses (i.e. always polygons)
        # because they may be transformed in such a way that they are no longer
        # ellipsoid, i.e. symmetrical with regard to the distance of any given point
        # along their edge with the one at the opposing angle.
        if timeDirection == "upstream":
            intervalShape = AdvancedGeometry.createRelocatedShape(shape, intervalPoly.centroid)
        else:
            intervalShape = AdvancedGeometry.createShapelyWrapper(intervalPoly, shape.getRotation())
        return intervalShape, secs

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

    def _computePoly(self, secs, speedVal, dirVal, spdUVal, dirUVal, dirValLast, threat):
        '''
        @param threat Polygon in Google coordinates.
        @return Polygon in Google coordinates.
        '''
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
        
    def _convertFeatureTime(self, eventSt_ms, secs):
        # Return millis given the event start time and secs offset
        # Round to minutes
        millis = long(eventSt_ms + secs * 1000 )
        return TimeUtils.roundEpochTimeMilliseconds(millis)
    
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
    
    # TO DO:  The Recommender should access HazardTypes.py for this 
    #   information
    def _hazardPointLimit(self):
        return 20

    def _probTrendColors(self):
        '''
        Should match PHI Prototype Tool
        (range: color) e.g. ((0,20), { "red": 0, "green": 1, "blue": 0 } ), 
                          ((20,4getProbGrid0), { "red": 1, "green": 1, "blue": 0 }),
        
        '''            
        colors = {
            (0,20): { "red": 102/255.0, "green": 224/255.0, "blue": 102/255.0 }, 
            (20,40): { "red": 255/255.0, "green": 255/255.0, "blue": 102/255.0 }, 
            (40,60): { "red": 255/255.0, "green": 179/255.0, "blue": 102/255.0 }, 
            (60,80): { "red": 255/255.0, "green": 102/255.0, "blue": 102/255.0 }, 
            (80,101): { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0 }
        }
        return colors

    def getOutputDir(self):
        return self._OUTPUTDIR

    def setUpDomainValues(self):
        self._OUTPUTDIR = '/scratch/PHIGridTesting'
        self._buff = 1.
        self._lonPoints = 1200
        self._latPoints = 1000
        self._initial_ulLat = 43.0
        self._initial_ulLon = -104.00
    
