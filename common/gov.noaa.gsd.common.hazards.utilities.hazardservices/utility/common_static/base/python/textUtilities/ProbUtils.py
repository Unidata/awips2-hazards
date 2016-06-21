'''
Utility for PHIGridRecommender and PreviewGridRecommender
'''
import numpy as np
import datetime, math
import time
import shapely.ops as so
import os, sys
import matplotlib
from matplotlib import path as mPath
from scipy import ndimage
from shapely.geometry import Polygon
from scipy.io import netcdf
from collections import defaultdict
from shutil import copy2
import HazardDataAccess
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
        timestamp = datetime.datetime.fromtimestamp(timestamp)
        timeStamp = timestamp.replace(second=0)

        
        for event in eventSet:
            
            ### Kludgey fix for HWT Week 3
            #if event.getEndTime() <= datetime.datetime.fromtimestamp(eventSet.getAttributes().get("currentTime")/1000):
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
        
        #print 'PU: firstIdx', firstIdx
        
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
        '''

        probability = np.zeros((len(y1), len(x1)))

        nx1, ny1 = y1.shape[0], x1.shape[0]
        x2,y2 = np.meshgrid(x1,y1)
        x3, y3 = x2.flatten(), y2.flatten()
        pnts1 = np.vstack((x3,y3)).T

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
        epoch = (timeStamp-datetime.datetime.fromtimestamp(0)).total_seconds()
        nowTime = datetime.datetime.fromtimestamp(time.time())
        
        
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
        startTime = self._roundTime(event.getStartTime()) 
        endTime = self._roundTime(event.getEndTime())        
        event.setStartTime(startTime)
        event.setEndTime(endTime)
        
    def _getDurationSecs(self, event, truncateAtZero=False):
        return self._getDurationMinutes(event, truncateAtZero) * 60

    def _getDurationMinutes(self, event, truncateAtZero=False):
        # This will round down to the nearest minute
        startTime = self._roundTime(event.getStartTime())
        endTime = self._roundTime(event.getEndTime())
        
        if truncateAtZero:
            endTime = self._getZeroProbTime_minutes(event, startTime, endTime)
                    
        durationMinutes = int((endTime-startTime).total_seconds()/60)
        return durationMinutes

    def _getZeroProbTime_minutes(self, event, startTime_minutes=None, endTime_minutes=None):
        # Return the time of the zero probability OR 
        #   the event end time if there is no zero prob found
        if not startTime_minutes:
            startTime_minutes = self._roundTime(event.getStartTime())
        if not endTime_minutes:
            endTime_minutes = self._roundTime(event.getEndTime())
            
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
                endTime_minutes = self._roundTime(startTime_minutes + zeroIndex * self._timeStep()/60)
        return endTime_minutes

    def _datetimeToMs(self, datetime):
        return float(time.mktime(datetime.timetuple())) * 1000
    
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
        previousDataLaterTime = event.get("previousDataLayerTime")
        issueStart = event.get("eventStartTimeAtIssuance")
        graphVals = event.get("convectiveProbTrendGraph")
        currentStart = long(self._datetimeToMs(event.getStartTime()))
        
        print '==='
        print event.getEventID()
        self.flush()
        
        
        if graphVals is None:
            return self._getGraphProbsBasedOnDuration(event)
        
        if issueStart is None:
            issueStart = currentStart
        
        
        ### Need to clean up, but problem with time trend aging off too fast was 
        ### currentProbTrend-previousProbTrend but timediff was currentTime-issueTime
        ###So saving previousDataLayerTime so that our timeDiffs match probTrendDiffs
        
        #print latestDataLayerTime, previousDataLaterTime, latestDataLayerTime is not None and previousDataLaterTime is not None
        #self.flush()
        
        if latestDataLayerTime is not None and previousDataLaterTime is not None:
            #print '+++ Using latestDataLayerTime-previousDataLaterTime'
            #self.flush()
            previous = datetime.datetime.fromtimestamp(previousDataLaterTime/1000)
            latest = datetime.datetime.fromtimestamp(latestDataLayerTime/1000)
        else:
            #print '--- Using currentStart-issueStart'
            previous = datetime.datetime.fromtimestamp(issueStart/1000)
            latest = datetime.datetime.fromtimestamp(currentStart/1000)

        latestDataLayerTime = latestDataLayerTime if latestDataLayerTime is not None else issueStart
        #print 'Setting previousDataLayerTime', latestDataLayerTime
        #self.flush()
        previousDataLaterTime = event.set("previousDataLayerTime", latestDataLayerTime)

        
        inc = event.get('convectiveProbabilityTrendIncrement', 5)
        #print 'Time Types?'
        #print 'previous', type(previous), previous
        #print 'latest', type(latest), latest
        minsDiff = (latest-previous).seconds/60
        print 'ProbUtils minsDiff', minsDiff
        self.flush()
        
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
        print 'ProbUtils graphVals'
        pprint.pprint(graphVals)
#        print type(probTrend), type(probTrend[0])
        self.flush()
        return graphVals

    def _getGraphProbsBasedOnDuration(self, event):
        probVals = []
        probInc = event.get('convectiveProbabilityTrendIncrement', 5)
        duration = self._getDurationMinutes(event)
        
        ### Round up for some context
        duration = duration+probInc if duration%probInc != 0 else duration
        
        for i in range(0, duration+1, probInc):
        #for i in range(0, duration+probInc+1, probInc):
            y = 100-(i*100/int(duration))
            y = 0 if i >= duration else y
            editable = True if y != 0 else False
            #editable = 1 if y != 0 else 0
            probVals.append({"x":i, "y":y, "editable": editable})
        return probVals
    
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
        rounding = ((seconds+roundTo/2) // roundTo) * roundTo
        return dt + datetime.timedelta(0,rounding-seconds,-dt.microsecond)
    
    def _roundTime_ms(self, ms):
        dt = datetime.datetime.fromtimestamp(ms/1000.0)
        #print "ProbUtils before dt", dt
        #self.flush()
        dt = self._roundTime(dt)
        #print "ProbUtils after dt", dt
        #self.flush()
        return VisualFeatures.datetimeToEpochTimeMillis(dt)

    def _timeDelta_ms(self):
        # A tolerance for comparing millisecond times
        return 40*1000
    
    def _getMillis(self, dt):
        epoch = datetime.datetime.utcfromtimestamp(0)
        delta = dt - epoch
        return delta.total_seconds() * 1000.0
    
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

    
    def flush(self):
        import os
        os.sys.__stdout__.flush()

    
    #########################################
    ### OVERRIDES        

    def _timeStep(self):
        # Time step for downstream polygons and track points
        return 60 # secs

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

    def getOutputDir(self):
        return self._OUTPUTDIR

    def setUpDomainValues(self):
        self._OUTPUTDIR = '/scratch/PHIGridTesting'
        self._buff = 1.
        self._lonPoints = 1200
        self._latPoints = 1000
        self._initial_ulLat = 40.0
        self._initial_ulLon = -80.0
    
    #########################################