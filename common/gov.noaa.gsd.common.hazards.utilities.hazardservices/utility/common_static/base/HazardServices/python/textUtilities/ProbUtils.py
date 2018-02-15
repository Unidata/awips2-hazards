'''
Utility for PHIGridRecommender and PreviewGridRecommender
'''
import numpy as np
import datetime, math
import time
import json
#from math import *
import shapely
import shapely.ops as so
import shapely.geometry as sg
import shapely.affinity as sa

import os, sys, re
import matplotlib
from matplotlib import path as mPath
from scipy import ndimage
from shapely.geometry import Polygon
from scipy.io import netcdf
from collections import defaultdict
from shutil import copy2
import HazardDataAccess
import TimeUtils, LogUtils
import GeometryFactory
import AdvancedGeometry
from VisualFeatures import VisualFeatures
from ConfigurationUtils import ConfigUtils

import GenericRegistryObjectDataAccess
from HazardConstants import *

from socket import gethostname
from ufpy import qpidingest

DEG_TO_RAD = np.pi / 180.0
RAD_TO_DEG = 180.0 / np.pi

class ProbUtils(object):
    def __init__(self, practice=True):
        self.setUpDomain(practice)
        self._previousDataLayerTime = None

    def handleObjectIDNaming(self,hazardEvent):
        
        hazardObjectID = hazardEvent.get('objectID', hazardEvent.getDisplayEventID())
        
        ### Fully manual should have 'M' prepended
        if hazardEvent.get('manuallyCreatedStatus'):
            if len(re.findall('[A-Za-z]', str(hazardObjectID))) == 0:
                hazardEvent.set('objectID', 'M'+hazardObjectID)
        else:
            #### Fully automated should have nothing prepended
            if hazardEvent.get("geometryAutomated") and hazardEvent.get("motionAutomated") and hazardEvent.get("probTrendAutomated"):
                if len(re.findall('[A-Za-z]', str(hazardObjectID))) > 0:
                    recommendedID = re.findall('\d+', str(hazardObjectID))[0]
                    hazardEvent.set('objectID', recommendedID)

            #### automated but all automation taken over should have 'm' prepended
            elif not hazardEvent.get("geometryAutomated") and not hazardEvent.get("motionAutomated") and not hazardEvent.get("probTrendAutomated"):
                if len(re.findall('[A-Za-z]', str(hazardObjectID))) > 0:
                    recommendedID = re.findall('\d+', str(hazardObjectID))[0]
                else:
                    recommendedID = str(hazardObjectID)
                hazardEvent.set('objectID', 'M'+recommendedID)
                
            #### Partially automated should have 'm' prepended
            else:
                if len(re.findall('[A-Za-z]', str(hazardObjectID))) > 0:
                    recommendedID = re.findall('\d+', str(hazardObjectID))[0]
                else:
                    recommendedID = str(hazardObjectID)
                hazardEvent.set('objectID', 'm'+recommendedID)
        
        return hazardEvent.get('objectID')
    
    def processEvents(self, eventSet, writeToFile=False):
        if writeToFile and not os.path.exists(self.OUTPUTDIR):
            try:
                os.makedirs(self.OUTPUTDIR)
            except:
                sys.stderr.write('Could not create PHI grids output directory:' +self.OUTPUTDIR+ '.  No output written')


        probSvrSnapList = []
        probSvrSwathList = []
        probTorSnapList = []
        probTorSwathList = []
        
        ### Note: this is one way to round down minutes... 
        #timeStamp = eventSet.getAttributes().get("issueTime").replace(second=0)
        timestamp = (long(eventSet.getAttributes().get("currentTime"))/1000)
        timestamp = datetime.datetime.utcfromtimestamp(timestamp)
        timeStamp = timestamp.replace(second=0)

        mode = eventSet.getAttributes().get('hazardMode', 'PRACTICE').upper()
        practice = True
        if mode == 'OPERATIONAL':
            practice = False
        
        for event in eventSet:
            
            ### Kludgey fix for HWT Week 3
            #if event.getEndTime() <= datetime.datetime.utcfromtimestamp(eventSet.getAttributes().get("currentTime")/1000):
            if event.getEndTime() <= timestamp:
                continue
            
            # Ensure the event is currently issued.
            if event.getStatus().upper() != 'ISSUED':
                continue

            # Get the most recent entry in the history list for the event,
            # since that is the one upon which the grid should be based.
            # If it has never been added to the history list, then it
            # should not result in any grid generation.
            event = HazardDataAccess.getMostRecentHistoricalHazardEvent(event.getEventID(), practice)
            if event is None:
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
                outputFileName = self.output(probSvrSnap, probSvrSwath, timeStamp, 'Severe')         
                #self.sendGridToQPID(outputFileName)
            if len(probTorSnapList) > 0:
                probTorSnap = np.max(np.array(probTorSnapList), axis=0)
                probTorSwath = np.max(np.array(probTorSwathList), axis=0)
                outputFileName = self.output(probTorSnap, probTorSwath, timeStamp, 'Tornado')         
                #self.sendGridToQPID(outputFileName)

        return 1

    def sendGridToQPID(self, filename):
        ### Might need to change this
        qpidServer = gethostname() #location where qpidd is running (currently assuming it is run locally)
        
        header = ''
        
        if 'Severe' in filename:
            header = 'PHIGrid_Severe_STUFF'
        if 'Tornado' in filename:
            header = 'PHIGrid_Tornado_STUFF'
        
        if os.path.exists(filename) and len(header) > 0:
        #=======================================================================
        #     try:
        #         conn = qpidingest.IngestViaQPID(host=self.qpidServer)
        #         conn.sendmessage(filename,self.probSevereHeader)
        #         conn.close()
        #     except:
        #         LogUtils.logMessage('[ERROR] :: Problem inserting the PHI Grid File ', filename, ' into QPID')
        # else:
        #     LogUtils.logMessage('[WARNING] :: PHI Grid filename: ', filename, ' does not exist. Skipping')
        #=======================================================================
            
            conn = qpidingest.IngestViaQPID(host=qpidServer)
            print '\n\nSENDING...', qpidServer, filename, header, '\n'
            conn.sendmessage(filename, header)
            conn.close()
        else:
            LogUtils.logMessage('[WARNING] :: PHI Grid filename: ', filename, ' does not exist. Skipping')
            

    def getOutputProbGrids(self, event, currentTime):                   
        forecastPolys = event.get('forecastPolys')
        if not forecastPolys:
            return
        forecastTimes = event.get('forecastTimes')

        ###  Only send forecast poly's >= "now"
        
        ### FIXME: forecastTimes is currently a tuple (st, et).  If ever becomes just "st" (no-tuple) will need to change j[0] to j
        ### FIXME: might need to round/buffer for event currently being issued.  For now, the event being issued is still 'PENDING', so we'lll use that.
        firstIdx = 0
        if event.getStatus().upper() == 'ISSUED': 
            firstIdx =  next(i for i,j in enumerate(forecastTimes) if j[0]/1000 >= int(currentTime.strftime('%s')))
        probTrend = self.getInterpolatedProbTrendColors(event)
        #probTrend = self.getGraphProbs(event)
        
        probGridSwath = self.makeGrid(forecastPolys[firstIdx:], probTrend[firstIdx:], self.lons, self.lats, 
                                 self.xMin1, self.xMax1, self.yMax1, self.yMin1)
        probGridSnap = self.makeGrid(forecastPolys[firstIdx:], probTrend[firstIdx:], self.lons, self.lats, 
                                 self.xMin1, self.xMax1, self.yMax1, self.yMin1, accumulate=False)
        
        
        return (probGridSnap, probGridSwath)
       
    def getProbGrid(self, event):
        forecastPolys = event.get('forecastPolys')
         
        if not forecastPolys:
           return
        
        probTrend = self.getInterpolatedProbTrendColors(event)
        #probTrend = self.getGraphProbs(event)
        probGridSwath = self.makeGrid(forecastPolys, probTrend, self.lons, self.lats, 
                                 self.xMin1, self.xMax1, self.yMax1, self.yMin1)
        
        return probGridSwath, self.lons, self.lats
        

    #===========================================================================
    # def getInterpolatedProbTrendColors(self, event):
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
    
    def getInterpolatedProbTrendColors(self, event, returnOneMinuteTime=False):
        '''
        (range: color) e.g. ((0,20), { "red": 0, "green": 1, "blue": 0 } ), 
                          ((20,40), { "red": 1, "green": 1, "blue": 0 }),
        
        '''
        colorsList = event.get('convectiveProbTrendGraph', self.getGraphProbsBasedOnDuration(event))
        #print '\n\n[PU]: colorsList', colorsList
        probTrend = [entry.get('y') for entry in colorsList]
        #print '\t[PU]: probTrend', probTrend
        
        duration = self.getDurationMinutes(event)

        probTrendTimeInterval = int(event.get('convectiveProbabilityTrendIncrement', 5))
        

        #print '\t[PU]: probTrendTimeInterval', probTrendTimeInterval, event.get('convectiveProbabilityTrendIncrement'), type(event.get('convectiveProbabilityTrendIncrement'))

        ### Add 1 to duration to get "inclusive" 
        probTrendTimeIntervals = np.arange(len(probTrend))*probTrendTimeInterval
        
        # This print statement had to be commented out as it was causing problems after restarting
        # H.S. within the same CAVE session; the fact that numpy doesn't play well with multiple
        # interpreters was causing some strange errors.
        # print '\t[PU]: probTrendTimeIntervals', probTrendTimeIntervals
        
        #oneMinuteTimeIntervals = np.arange(0, probTrendTimeIntervals[-1]+1, 1)
        #print "PU: OneminuteTimeIntervals--Old:", oneMinuteTimeIntervals
        oneMinuteTimeIntervalsNew = self.getIntervalMinutes(duration)
        
        #oneMinuteProbs = np.interp(oneMinuteTimeIntervals, probTrendTimeIntervals, probTrend)
        oneMinuteProbs = np.interp(oneMinuteTimeIntervalsNew, probTrendTimeIntervals, probTrend)

        if returnOneMinuteTime:
            return {'oneMinuteProbs':oneMinuteProbs, 'oneMinuteTimeIntervals': oneMinuteTimeIntervalsNew}
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
        union = GeometryFactory.performCascadedUnion(swathPolygon)
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
      
    def output(self, snap, swath, timeStamp, eventType):
        '''
        Creates an output netCDF file in OUTPUTDIR (set near top)
        '''
        epoch = (timeStamp-datetime.datetime.utcfromtimestamp(0)).total_seconds()
        nowTime = datetime.datetime.utcfromtimestamp(time.time())
        
        
        #outDir = os.path.join(self.OUTPUTDIR, nowTime.strftime("%Y%m%d_%H"), eventType)
        outDir = self.OUTPUTDIR

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
            
            f.createDimension('lats', len(self.lats))
            f.createDimension('lons', len(self.lons))
            f.createDimension('time', 1)
            
            latVar = f.createVariable('lats', 'f', ('lats',))
            latVar.long_name = "latitude"
            latVar.units = "degrees_north"
            latVar.standard_name = "latitude"
            latVar[:] = self.lats
            
            lonVar = f.createVariable('lons', 'f', ('lons',))
            lonVar.long_name = "longitude"
            lonVar.units = "degrees_east"
            lonVar.standard_name = "longitude"
            lonVar[:] = self.lons
            
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
       
    def parseIdentifier(self, baseTime_ms, featureIdentifier):
        featureSt = featureIdentifier.split('_')[1]
        return int(featureSt)
                
    def roundEventTimes(self, event):
        # Rounded to nearest second
        startTime = TimeUtils.roundDatetime(event.getStartTime(), delta=datetime.timedelta(seconds=1)) 
        endTime = TimeUtils.roundDatetime(event.getEndTime(), delta=datetime.timedelta(seconds=1))        
        event.setStartTime(startTime)
        event.setEndTime(endTime)
        
    def getDurationSecs(self, event, truncateAtZero=False):
        
        # we may not want to truncate the forecastpoly for manual event
        if truncateAtZero and not (event.get('geometryAutomated') and event.get('motionAutomated') and event.get('probTrendAutomated')):
            return self.getDurationMinutes(event) * 60
        return self.getDurationMinutes(event, truncateAtZero) * 60

    def getDurationMinutes(self, event, truncateAtZero=False):
        # This will round down to the nearest minute
        startTime = TimeUtils.roundDatetime(event.getStartTime())
        endTime = TimeUtils.roundDatetime(event.getEndTime())
        
        if truncateAtZero:
            endTime = self.getZeroProbTime_minutes(event, startTime, endTime)
                    
        durationMinutes = int((endTime-startTime).total_seconds()/60)
        return durationMinutes

    def getZeroProbTime_minutes(self, event, startTime_minutes=None, endTime_minutes=None):
        # Return the time of the zero probability OR 
        #   the event end time if there is no zero prob found
        if not startTime_minutes:
            startTime_minutes = TimeUtils.roundDatetime(event.getStartTime())
        if not endTime_minutes:
            endTime_minutes = TimeUtils.roundDatetime(event.getEndTime())
            
        # Check for zero value prior to endTime
        graphVals = event.get("convectiveProbTrendGraph")
        inc = event.get('convectiveProbabilityTrendIncrement', 5)
        if graphVals is not None:
            # Determine the zero value to set the endTime 
            zeroIndex = None
            for i in range(len(graphVals)):
                if graphVals[i]['y'] < 1:
                    zeroIndex = i
                    break
            if zeroIndex and zeroIndex != len(graphVals)-1:
                #endTime_minutes = TimeUtils.roundDatetime(startTime_minutes + zeroIndex * self.timeStep()/60)
                endTime_minutes = TimeUtils.roundDatetime(startTime_minutes+datetime.timedelta(minutes=zeroIndex * inc ))
        return endTime_minutes
    
    def convertMsToSecsOffset(self, time_ms, baseTime_ms=0):
        result = time_ms - baseTime_ms
        return int(result / 1000)
    
    def updateGraphValsDuration(self, origGraphVals, newGraphVals):
        
        if len(newGraphVals) <= len(origGraphVals):
            print "PU-0 len(newGraphVals) <= len(origGraphVals)"
            newGraphVals = origGraphVals[0:len(newGraphVals)]
        else:
            print "PU-1 "
            origGraphVals[-1]['editable'] = True
            for entry in newGraphVals:
                print '\t setting y=0'
                entry['y'] = 0
            newGraphVals[0:len(origGraphVals)] = origGraphVals
            
        newGraphVals[-1]['y'] = 0
        newGraphVals[-1]['editable'] = False
            
        import pprint
        print 'PU--'
        pprint.pprint(newGraphVals)
            
        return newGraphVals            

    def getPreviousDataLayerTime(self):
        return self._previousDataLayerTime
    
    def getGraphProbs(self, event, latestDataLayerTime=None, fromCommonMetaData=False):
        ### Get difference in minutes and the probability trend
        previousDataLayerTime = event.get("previousDataLayerTime")
        issueStart = event.get("eventStartTimeAtIssuance")
        graphVals = event.get("convectiveProbTrendGraph")

        currentStart = long(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()))
                
        if graphVals is None:
            #LogUtils.logMessage('[HERE-0]')
            return self.getGraphProbsBasedOnDuration(event)
                
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

        if fromCommonMetaData:
            self._previousDataLayerTime = latestDataLayerTime
        else:
            previousDataLayerTime = event.set("previousDataLayerTime", latestDataLayerTime)

        
        inc = event.get('convectiveProbabilityTrendIncrement', 5)
        minsDiff = (latest-previous).seconds/60
        
        ### Get interpolated times and probabilities 
        intervalDict = self.getInterpolatedProbTrendColors(event, returnOneMinuteTime=True)
        oneMinuteProbs = intervalDict.get('oneMinuteProbs')
        oneMinuteTimeIntervals = intervalDict.get('oneMinuteTimeIntervals')
#         ### New list of zeros as a placeholder
#         updatedProbs = zeros = np.zeros(len(oneMinuteTimeIntervals), dtype='i4')
#         
#         ### Find where the 1-min times > diff
#         indices = np.where(oneMinuteTimeIntervals>=minsDiff)
#         ### Get the probs corresponding with remaining time
#         remainingProbs = oneMinuteProbs[indices]
#         ### put those remaining probs to FRONT of zeros array
#         updatedProbs[0:len(remainingProbs)] = remainingProbs
#         print "YG--updatedProbs---", updatedProbs
#         ### Sample at increment 
#         fiveMinuteUpdatedProbs = updatedProbs[::inc].tolist()
#         #LogUtils.logMessage('Updated', fiveMinuteUpdatedProbs)

#         if len(fiveMinuteUpdatedProbs) == len(graphVals):
#             for i in range(len(graphVals)):
#                 graphVals[i]['y'] = fiveMinuteUpdatedProbs[i]
#         ### Otherwise, using original inc-times, if we have mismatch in length of probs, 
#         ### inform user and return original
#         else:
#             sys.stderr.write('\n\tError updating ProbTrendGraph. Returning default')
#             self.flush()
        
        # re-write the copy of aging off probs
        # New list of zeros as a placeholder
        fiveMinuteIntervals = np.array([entry.get('x') for entry in graphVals])      
        fiveMinuteProbs = np.interp(fiveMinuteIntervals, oneMinuteTimeIntervals, oneMinuteProbs)          
        indices = np.where(fiveMinuteIntervals>=minsDiff)
        ### Get the probs corresponding with remaining time
        remainingProbs = fiveMinuteProbs[indices]
        ### put those remaining probs to FRONT of zeros array
        fiveMinuteProbs[0:len(remainingProbs)] = remainingProbs
        fiveMinuteProbs[len(remainingProbs):] = 0
        ### update original times with shifted probs, if length of arrays match
        for i in range(len(graphVals)):
            graphVals[i]['y'] = fiveMinuteProbs[i]
                
        ### Challenge is to get the graph to show the "rounded up" value, but we 
        ### don't want to muck with the duration, so still uncertain the best way
        newGraphVals = self.getGraphProbsBasedOnDuration(event)
#         if event.get('automationLevel') == 'automated':
        if event.get('geometryAutomated') and event.get('motionAutomated') and event.get('probTrendAutomated'):
            graphVals = newGraphVals
        else:
            graphVals = self.updateGraphValsDuration(graphVals, newGraphVals)
        
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

    def getGraphProbsBasedOnDuration(self, event):
        probVals = []
        probInc = event.get('convectiveProbabilityTrendIncrement', 5)
        duration = self.getDurationMinutes(event)
        
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

    def timeDelta_ms(self):
        # A tolerance for comparing millisecond times
        return 60*1000
    
    def displayMsTime(self, time_ms):
        return time.strftime('%Y-%m-%d %H:%M:%S', time.gmtime(time_ms/1000))

    def getIntervalMinutes(self, duration):
        
        tenMinuteIntervals = []
        fiveMinuteIntervals = []
        oneMinuteIntervals = []
        
        stepForDuration = self.timeStep(duration)/60
        if stepForDuration == 10:
            tenMinuteIntervals = range(120, duration+1, 10)
            fiveMinuteIntervals = range(60, 120, 5)
            oneMinuteIntervals = range(60)
        elif stepForDuration == 5:
            fiveMinuteIntervals = range(60, duration+1, 5)
            oneMinuteIntervals = range(60)
        else:
            oneMinuteIntervals = range(min(60, duration)+1)
        
        return np.array(oneMinuteIntervals + fiveMinuteIntervals + tenMinuteIntervals)
        
    def reCalcInterpolatedProb(self, event):
        
        print "PU---Re-calculating interpolated prob..."
        duration = self.getDurationMinutes(event)
        probVals = event.get('convectiveProbTrendGraph', event.get('preDraw_convectiveProbTrendGraph', []))
        probTrend = [entry.get('y') for entry in probVals]
            
        probTrendTimeInterval = event.get('convectiveProbabilityTrendIncrement', 5 )
            
        ### Add 1 to duration to get "inclusive" 
        probTrendTimeIntervals = np.arange(len(probTrend))*probTrendTimeInterval        
        oneMinuteTimeIntervals = np.arange(0, probTrendTimeIntervals[-1]+1, 1)
        self.oneMinuteTimeIntervalsNew = self.getIntervalMinutes(duration)
                        
        #oneMinuteProbs = np.interp(oneMinuteTimeIntervals, probTrendTimeIntervals, probTrend)
        self.oneMinuteProbs = np.interp(self.oneMinuteTimeIntervalsNew, probTrendTimeIntervals, probTrend)            
        
    def getInterpolatedProbTrendColor(self, event, interval, numIvals):
        '''
        (range: color) e.g. ((0,20), { "red": 0, "green": 1, "blue": 0 } ), 
                          ((20,40), { "red": 1, "green": 1, "blue": 0 }),
        
        '''

        # interpolation is done once when necessary        

#         duration = self.getDurationMinutes(event)
#         probVals = event.get('convectiveProbTrendGraph', event.get('preDraw_convectiveProbTrendGraph', []))
#         probTrend = [entry.get('y') for entry in probVals]
#              
#         probTrendTimeInterval = event.get('convectiveProbabilityTrendIncrement', 5 )
#              
#         ### Add 1 to duration to get "inclusive" 
#         probTrendTimeIntervals = np.arange(len(probTrend))*probTrendTimeInterval        
#         oneMinuteTimeIntervals = np.arange(0, probTrendTimeIntervals[-1]+1, 1)
#                         
#         oneMinuteProbs = np.interp(oneMinuteTimeIntervals, probTrendTimeIntervals, probTrend)
        
#         if interval >= len(oneMinuteTimeIntervals):
#             print "ProbUtils Warning: Oops1: interval >= len(oneMinuteTimeIntervals)", interval, len(oneMinuteTimeIntervals)
#             ### Return white dot
#             return self.getProbTrendColor(-1)
        if interval >= len(self.oneMinuteTimeIntervalsNew):
            print "ProbUtils Warning: Oops1: interval >= len(oneMinuteTimeIntervalsNew)", interval, len(self.oneMinuteTimeIntervalsNew)
            ### Return white dot
            return self.getProbTrendColor(-1)
        prob = self.oneMinuteProbs[interval]
        
        return self.getProbTrendColor(prob)

    def getProbTrendColor(self, prob):
        ### Should match PHI Prototype Tool
        colors = self.probTrendColors()
        
        for k, v in colors.iteritems():
            if float(k[0]) <= prob and prob < float(k[1]):
                return v

        return { "red": 1, "green": 1, "blue": 1 }      

    def reduceShapeIfPolygon(self, initialShape): 
        '''
        @summary Reduce the shape if it is a polygon to have only the maximum allowable
        number of vertices.
        @param initialShape: Advanced geometry.
        @return Reduced polygon as advanced geometry if the original was a
        polygon, otherwise the original advanced geometry.
        '''  
        if initialShape.isPolygonal() and not initialShape.isPotentiallyCurved():
            rotation = initialShape.getRotation()
            numPoints = self.hazardPointLimit()
            tolerance = 0.001
            initialPoly = initialShape.asShapely()
            if type(initialPoly) is shapely.geometry.collection.GeometryCollection:
                initialPoly = initialPoly[0] 
            if len(initialPoly.exterior.coords) <= numPoints:
                return initialShape
            newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
            while len(newPoly.exterior.coords) > numPoints:
                tolerance += 0.001
                newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
        
            return AdvancedGeometry.createShapelyWrapper(newPoly, rotation)
        return initialShape

    ###############################
    # Compute Motion Vector       #
    ###############################

    def computeMotionVector(self, polygonTuples, currentTime):
        '''
        @param polygonTuples List of tuples expected as:
        [(poly1, startTime1), (poly2, startTime2),,,,(polyN, startTimeN)]
        with each poly being in advanced geometry form.
        '''
        meanU = None
        meanV = None
        uList = []
        vList = []
        spdList = []
        dirList = []

        ### Sort polygonTuples by startTime
        sortedPolys = sorted(polygonTuples, key=lambda tup: tup[1])

        ### Get create sorted list of u's & v's
        for i in range(1, len(sortedPolys)):
            p1 = sortedPolys[i-1][0]
            t1 = sortedPolys[i-1][1]
            p2 = sortedPolys[i][0]
            t2 = sortedPolys[i][1]
            dist = self.getHaversineDistance(p1, p2) # meters
            speed = dist/((t2-t1)/1000)
            bearing = self.getBearing(p1, p2)

            spdList.append(speed)
            dirList.append(bearing)
            
            
        spdStats = self.weightedAvgAndStdDevSPD(spdList)
        meanSpd = self.convertMsecToKts(spdStats.get('weightedAverage'))
        print 'PU[1] - spdStats.get(\'weightedAverage\'), meanSpd', spdStats.get('weightedAverage'), meanSpd
        stdSpd = self.convertMsecToKts(spdStats.get('stdDev'))
        dirStats = self.weightedAvgAndStdDevDIR(dirList)
        meanDir = dirStats.get('weightedAverage')
        stdDir = dirStats.get('stdDev')
        
        if np.isnan(stdDir):
            # if NaN, go minimum
            stdDir = 12
        elif stdDir > 45:
            stdDir = 45
        elif stdDir < 12:
            stdDir = 12

        if np.isnan(stdSpd):
            # if NaN, go minimum
            stdSpd = 4
        elif stdSpd > 20:
            stdSpd = 20
        elif stdSpd < 4:
            stdSpd = 4

        if np.isnan(meanSpd):
            # if NaN, go minimum
            meanSpd = 1
        elif meanSpd > 102:
            meanSpd = 102
        elif meanSpd < 0:
            meanSpd = 0

        if np.isnan(meanDir):
            # if NaN, go minimum
            meanDir = 1 
        elif meanDir > 359:
            meanDir = meanDir%360
        elif meanDir < 0:
            meanDir = meanDir%360

        return {
                'convectiveObjectDir' : meanDir,
                'convectiveObjectDirUnc' : stdDir,
                'convectiveObjectSpdKts' : meanSpd,
                'convectiveObjectSpdKtsUnc' : stdSpd
                }    


    def weightedAvgAndStdDevSPD(self, xList):
        spdList = np.array(xList)
        weights = range(1,len(spdList)+1)
        weightedAvg = np.average(spdList, weights=weights)
        print 'PU[0] - spdList, weights, weightedAvg', spdList, weights, weightedAvg
        weightedStd = np.sqrt(np.average((spdList-weightedAvg)**2, weights=weights))
        return {'weightedAverage':weightedAvg, 'stdDev': weightedStd}
    
    def weightedAvgAndStdDevDIR(self, xList):
        dirList = np.array(xList)
        weights = range(1,len(dirList)+1)
        wrad = dirList*DEG_TO_RAD
        wsin = np.average(np.sin(wrad), weights=weights)
        wcos = np.average(np.cos(wrad), weights=weights)
        wdir_avg = ((np.arctan2(wsin, wcos)*RAD_TO_DEG)+360)%360
        e = np.sqrt(1-(wsin*wsin+wcos*wcos))
        wdir_std = RAD_TO_DEG*np.arcsin(e)*(1+0.1547*np.power(e,3))
        return {'weightedAverage':wdir_avg, 'stdDev': wdir_std}

        

    #############################################
    ## Conversion methods from GFE SmartScript ##

    def MagDirToUV(self, mag, dir):
        DEG_TO_RAD = np.pi / 180.0
        # Note sign change for components so math to meteor. coords works
        uw = - math.sin(dir * DEG_TO_RAD) * mag
        vw = - math.cos(dir * DEG_TO_RAD) * mag
        return (uw, vw)

    def UVToMagDir(self, u, v):
        RAD_TO_DEG = 180.0 / np.pi
        # Sign change to make math to meteor. coords work
        u = -u
        v = -v
        if type(u) is np.ndarray or type(v) is np.ndarray:
            speed = np.sqrt(u * u + v * v)
            dir = np.arctan2(u, v) * RAD_TO_DEG
            dir[np.greater_equal(dir, 360)] -= 360
            dir[np.less(dir, 0)] += 360
        else:
            speed = math.sqrt(u * u + v * v)
            dir = math.atan2(u, v) * RAD_TO_DEG
            while dir < 0.0:
                dir = dir + 360.0
            while dir >= 360.0:
                dir = dir - 360.0
        return (speed, dir)

    def convertMsecToKts(self, value_Msec):
        # Convert from meters/sec to Kts
        return value_Msec * 3600.0 / 1852.0


    #############################################


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
        bearing = (bearing+180) % 360
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
        meters = 6371000 * c # Earth's radius in meters
        return meters


    ######################################################
    # Compute Interval (forecast and upstream ) Polygons #
    ######################################################
            

    def createIntervalPolys(self, event, eventSetAttrs, swathPresetClass, startTime_ms, 
                             timeIntervals, timeDirection='forecast'):
        '''
        This method creates the forecast or upstream polygons given 
          -- event which contains:
               -- a direction and direction uncertainty
               -- a speed and a speed uncertainty
          -- eventSetAttrs
          -- swathPresetClass
          -- startTime_ms 
          -- a list of timeIntervals -- list of intervals (in secs) relative to given start time for
             which to produce forecast or upstream polygons
             For forecast, startTime_ms will be current time
             For upstream, startTime_ms will be the eventSt_ms
          -- timeDirection -- 'forecast' or 'upstream'
          
        Note that the timeIntervals will be negative for upstream and positive for forecast
                
        From the forecastPolys and upstreamPolys, the visualFeatures 
            (swath, trackpoints, and upstream polygons) can be determined.        
        '''
        
        # Set up direction and speed values
        ### get Wind Dir
        dirVal = self.getDefaultMotionVectorKey(event, 'convectiveObjectDir')             
        ### get dirUncertainty (degrees)
        dirUVal = self.getDefaultMotionVectorKey(event, 'convectiveObjectDirUnc')
        ### get speed
        speedVal = self.getDefaultMotionVectorKey(event, 'convectiveObjectSpdKts')
        # get speedUncertainty
        spdUVal = self.getDefaultMotionVectorKey(event, 'convectiveObjectSpdKtsUnc')
                            
        ### Get initial shape.  
        # This represents the shape at the event start time resulting from the last
        shape = event.getGeometry()
                
        # Convert the shape to a shapely polygon.
        poly = shape.asShapely()
        if type(poly) is shapely.geometry.collection.GeometryCollection:
            poly = poly[0]
                    
        presetChoice = event.get('convectiveSwathPresets') if event.get('convectiveSwathPresets') is not None else 'NoPreset'
        presetMethod = getattr(swathPresetClass, presetChoice)
                
        # Convert the shapely polygon to Google Coords to make use of Karstens' code
        gglPoly = so.transform(AdvancedGeometry.c4326t3857, poly)
                            
        intervalShapes = []
        intervalTimes = []
        totalSecs = abs(timeIntervals[-1] - timeIntervals[0]) 
        if not totalSecs:
            totalSecs = timeIntervals[0]
        self.prevDirVal = None
        
        # looks like there is an extra polygon created behind the last interval
        # which may not be wanted
#        for i in range(len(timeIntervals)):
        for i in range(len(timeIntervals)-1):
            secs = timeIntervals[i]
            origDirVal = dirVal
            
            intervalShape, secs = self.getIntervalShape(secs, totalSecs, shape, gglPoly, 
                                                        speedVal, dirVal, spdUVal, dirUVal, 
                                                        timeDirection, presetMethod)
            intervalShape = self.reduceShapeIfPolygon(intervalShape)

            # Convert the shape to its shapely geometry, and ensure it is not a collection.
            intervalGeometry = intervalShape.asShapely()
            if type(intervalGeometry) is shapely.geometry.collection.GeometryCollection:
                intervalGeometry = intervalGeometry[0]
            
            # If the base geometry is concave, this can lead to interval shapes that are
            # invalid. These need to be corrected before proceeding further.
            changed = False
            if not intervalGeometry.is_valid:
                changed = True
                intervalGeometry = GeometryFactory.correctPolygonIfInvalid(intervalGeometry, 0.001)
            
            # If the resulting shape has holes in it, de-hole it.
            if intervalGeometry.interiors:
                changed = True
                intervalGeometry = GeometryFactory.createPolygon(intervalGeometry.exterior.coords)
            
            if changed:
                intervalShape = AdvancedGeometry.createShapelyWrapper(intervalGeometry,  intervalShape.getRotation())
                                                  
            intervalShapes.append(intervalShape)
            
            st = self.convertFeatureTime(startTime_ms, secs)
#             if i < len(timeIntervals)-1:
#                 endSecs = timeIntervals[i+1] - secs
#             else:
#                 endSecs = self.timeStep()
            endSecs = timeIntervals[i+1] - secs
            
            et = self.convertFeatureTime(startTime_ms, secs+endSecs)
            intervalTimes.append((st, et))                    
                    
        if timeDirection == 'forecast':
            event.addHazardAttribute('forecastPolys',intervalShapes)       
            event.addHazardAttribute('forecastTimes',intervalTimes) 
        else:
            event.addHazardAttribute('upstreamPolys',intervalShapes)       
            event.addHazardAttribute('upstreamTimes',intervalTimes)   
            
        print "PU---CreateIntervalPoly--intervalTimes --", intervalTimes  

    def getIntervalShape(self, secs, totalSecs, shape, gglPoly, speedVal, dirVal, spdUVal, dirUVal, 
                         timeDirection, presetMethod):
        '''
        @param shape: Shape in advanced geometry form.
        @param gglPoly: Shapely polygon version of the shape using Google coordinates. 
        @return Interval shape in advanced geometry form.
        '''
        presetResults = {'dirVal':dirVal, 'speedVal': speedVal, 'spdUVal':spdUVal, 'dirUVal':dirUVal}
        presetResultsOLD = presetMethod(speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs)
        
        #print 'PU[3] - presetResultsOLD, presetResults', presetResultsOLD, presetResults
        
        if timeDirection == 'upstream':
#            presetResults = presetMethod(speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs)
            dirValLast = dirVal
        else:
#            presetResults = presetMethod(speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs)            
            dirValLast = presetResults['dirVal']
            if self.prevDirVal:
                dirValLast = self.prevDirVal
            speedVal = presetResults['speedVal']
            dirVal = presetResults['dirVal']
            self.prevDirVal = dirVal
            spdUVal = presetResults['spdUVal']
            dirUVal = presetResults['dirUVal']           
        
        gglDownstream = self.computePoly(secs, speedVal, dirVal, spdUVal, dirUVal,
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


    def computePoly(self, secs, speedVal, dirVal, spdUVal, dirUVal, dirValLast, threat):
        '''
        @param threat Polygon in Google coordinates.
        @return Polygon in Google coordinates.
        '''
        
        ### BugAlert: this 1.25x multiplier is an empirical value suggested by Greg
        ### after playing with the distance tool.  Need to find
        ### source of 80% slowdown of downstream polygons.
        ### Darrel Kingfield found the issue of calculating distances on Mercator-like
        ### projections has a (1/math.cos(centerLat*(math.pi/180))) scaling factor
        ### and at about 33 degreees latitude this works out to approximately 1.2x
        tmpPoly = so.transform(AdvancedGeometry.c3857t4326, threat)
        centerLat = tmpPoly.centroid.y
        speedVal = float(speedVal) * (1/math.cos(centerLat*(math.pi/180)))  # (calculated via testing)
#        speedVal = float(speedVal)
        dirVal = float(dirVal)
        dis = secs * speedVal * 0.514444444
        
        xDis = dis * math.cos(math.radians(270.0 - dirVal))
        yDis = dis * math.sin(math.radians(270.0 - dirVal))
        xDis2 = secs * spdUVal * 0.514444444
        yDis2 = dis * math.tan(math.radians(dirUVal))
        
        #print '\n\n PU[4] - dis, xDis, yDis', dis, xDis, yDis, '\n'
        
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
            
    def convertFeatureTime(self, startTime_ms, secs):
        # Return millis given the event start time and secs offset
        # Round to seconds
        millis = long(startTime_ms + secs * 1000 )
        return TimeUtils.roundEpochTimeMilliseconds(millis, delta=datetime.timedelta(seconds=1))
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()


    #########################################
    ### Application Dictionary Helper methods        
    # Temporarily read / write dictionary to /tmp file
    # Eventually will build a dictionary that can be
    #   persisted and passed to Recommenders, MetaData,
    #   and Product Generation.
    
    def writeApplicationDict(self, dictionary):
        f = open('/tmp/appDict.json', 'w')
        f.write(json.dumps(dictionary))
            
    def readApplicationDict(self):
        try:
            f = open('/tmp/appDict.json', 'r')
        except:
            return {}
        return json.load(f)
    
    def updateApplicationDict(self, updateDict):
        appDict = self.readApplicationDict()
        for key in updateDict:
            appDict[key]=updateDict[key]
        self.writeApplicationDict(appDict)
    
    def getApplicationValue(self, key, default):
        appDict = self.readApplicationDict()
        return appDict.get(key, default)
    
    def getDefaultMotionVectorKey(self, event, key): 
        return int(event.get(key, self.getApplicationValue(key, self.defaultValueDict().get(key,0)))) 
    
    #########################################
    ### Common Methods shared among modules
    def setActivation(self, event, caveUser=None, modify=True):
        '''
        Set the activate and activeModify attributes of the event

        If caveUser is present, and not the owner of the event, deactivated and modify button deactivated

        If selecting user-owned (automationLevel) pending (status) hazard: activated and Modify button deactivated
        If selecting automated (automationLevel) and pending (status): deactivated and Modify button deactivated
        If selecting either of the other two possible (automationLevel) and pending (status): deactivated and Modify button activated
        If selecting issued hazard: deactivated and Modify button activated
        If selecting Ending, Ended, Elapsed hazard: deactivated and Modify button deactivated.
        '''
        currentOwner = event.get("owner", None)
        
        #if caveUser and caveUser != currentOwner:
        if caveUser and currentOwner:
            lowerUser = caveUser.lower()
            lowerOwner = currentOwner.lower()
            if lowerUser.find(lowerOwner) < 0 and lowerOwner.find(lowerUser)< 0:
                print "Cave user is not the event owner, cannot modify event", caveUser, currentOwner
                activate = False
                activateModify = False
                print "PU Setting activate, activateModify", activate, activateModify
                self.flush()            
                if modify:
                    event.set('activate', activate)
                    event.set('activateModify', activateModify)            
                return activate, activateModify
        
        #automationLevel = event.get('automationLevel')
        status = event.getStatus()
#         print "PU setActivation", automationLevel, status
        print "PU setActivation", event.get('geometryAutomated'), event.get('motionAutomated'), event.get('probTrendAutomated'), status
        self.flush()
        if status == 'PENDING':
            if not event.get('geometryAutomated') and not event.get('motionAutomated') and not event.get('probTrendAutomated'):
                activate = True
                activateModify = False 
            elif event.get('geometryAutomated') and event.get('motionAutomated') and event.get('probTrendAutomated'):
                activate = False
                activateModify = False
#             if automationLevel == 'userOwned':
#                 activate = True
#                 activateModify = False
#             elif automationLevel == 'automated':
#                 activate = False
#                 activateModify = False
            else:
                activate = False
                activateModify = True
        elif status == 'ISSUED':
            activate = False
            activateModify = True
        else: # status Ending, Ended, Elapsed
            activate = False
            activateModify = False       

        print "PU Setting activate, activateModify", activate, activateModify
        self.flush()
        if modify:
            event.set('activate', activate)
            event.set('activateModify', activateModify)
        return activate, activateModify
                    
    #########################################
    ### OVERRIDES        
    
    def defaultValueDict(self):
        return {
            'convectiveObjectDir': 270,
            'convectiveObjectSpdKts': 32,
            'convectiveObjectDirUnc': 12,
            'convectiveObjectSpdKtsUnc': int(2.16067*1.94384),
            }

    def timeStep(self, index = 0):
        # Time step for forecast polygons and track points
        # input index is the index of minutes
        # return the total of seconds...
        if (index < 60):
            return 60
        elif (index < 120):
            return 5*60
        else:
            return 10*60
    
    # TO DO:  The Recommender should access HazardTypes.py for this 
    #   information
    def hazardPointLimit(self):
        return 20

    def probTrendColors(self):
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
#        objectDicts = GenericRegistryObjectDataAccess.queryObjects([("uniqueID", "phiConfig")], practice)
#        OUTPUTDIR = objectDicts.get(OUTPUTDIRKEY, DEFAULTPHIOUTPUTDIR)
        return self.OUTPUTDIR
    
#    def getThreshold(self):
#        objectDicts = GenericRegistryObjectDataAccess.queryObjects([("uniqueID", "phiConfig")], practice)
#        lowThreshold = objectDicts.get(LOWTHRESHKEY, DEFAULTLOWTHRESHOLD)
#        return lowThreshold
#    
#    def getBuffer(self):
#        objectDicts = GenericRegistryObjectDataAccess.queryObjects([("uniqueID", "phiConfig")], practice)
#        buff = objectDicts.get(DOMAINBUFFERKEY, DEFAULTDOMAINBUFFER)
#        return buff

    def setUpDomain(self, practice):                            
        returnList = GenericRegistryObjectDataAccess.queryObjects([("uniqueID", "phiConfig"),("objectType", "phiConfigData")], practice)
        
        if len(returnList) == 1:
            objectDicts = returnList[0]
        elif len(returnList) == 0:
            objectDicts = {}
        else:
            sts.stderr.write("!!!! PU - GenericRegistryObjectDataAccess.queryObject returned multiple dictionaries. Reverting to default values")
            objectDicts = {}
        print '\n\n################ PU - QUERY  #############' 
        print objectDicts
        print type(objectDicts)
        sys.stdout.flush()

        self.OUTPUTDIR = objectDicts.get(OUTPUTDIRKEY, DEFAULTPHIOUTPUTDIR)
        self.buff = objectDicts.get(DOMAINBUFFERKEY, DEFAULTDOMAINBUFFER)
        self.lowThreshold = objectDicts.get(LOWTHRESHKEY, DEFAULTLOWTHRESHOLD)
        self.ulLon = objectDicts.get(DOMAINULLONKEY, DEFAULTDOMAINULLON)
        self.ulLat = objectDicts.get(DOMAINULLATKEY, DEFAULTDOMAINULLAT)
        self.lrLon = objectDicts.get(DOMAINLRLONKEY, DEFAULTDOMAINLRLON)
        self.lrLat = objectDicts.get(DOMAINLRLATKEY, DEFAULTDOMAINLRLAT)
        
        self.xMin1 = self.ulLon - self.buff   
        self.xMax1 = self.lrLon + self.buff
        self.yMax1 = self.ulLat + self.buff
        self.yMin1 = self.lrLat - self.buff
        self.lons = np.arange(self.xMin1,self.xMax1,0.01)
        self.lats = np.arange(self.yMin1+0.01,self.yMax1+0.01,0.01)                
        
        sys.stdout.flush()


    def getCaveUser(self, userName, workStation):
        # To turn off Ownership, return None
        return None
        # To turn on Ownership, do the following:
#         if userName and workStation:
#             if "ewp" in workStation and len(workStation.split('.')) == 1:
#                 workStation += ".hwt.nssl"
#             return userName + ':' + workStation
#         return None

