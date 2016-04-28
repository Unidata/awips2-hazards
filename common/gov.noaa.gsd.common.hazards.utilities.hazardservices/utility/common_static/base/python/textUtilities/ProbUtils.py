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

OUTPUTDIR = '/scratch/PHIGridTesting'
### FIXME: need better (dynamic or configurable) way to set domain corner
buff = 1.
lonPoints = 1200
latPoints = 1000
ulLat = 37.0 + buff
ulLon = -103.0 - buff
#ulLat = 44.5 + buff
#ulLon = -104.0 - buff

class ProbUtils(object):
    def __init__(self):
        
        # create null grid encompassing floater domain
        self._xMin1 = ulLon
        self._xMax1 = self._xMin1 + (0.01 * lonPoints)
        self._yMax1 = ulLat
        self._yMin1 = self._yMax1 - (0.01 * latPoints)
        self._lons = np.arange(self._xMin1,self._xMax1,0.01)
        self._lats = np.arange(self._yMin1+0.01,self._yMax1+0.01,0.01)

    def processEvents(self, eventSet, writeToFile=False):        

        probSvrList = []
        probTorList = []
        
        swathDictSvr = defaultdict(list)
        snapDictSvr = defaultdict(list)
        swathDictTor = defaultdict(list)
        snapDictTor = defaultdict(list)

        for event in eventSet:
            timeStamp = event.getStartTime()
            hazardType = event.getHazardType()
            start = time.time()
            probsDict = self.getOutputProbGrids(event)
            print hazardType + " [0] Took %.2f seconds" % (time.time()-start)
            if hazardType == 'Prob_Severe':
                for tm in probsDict:
                    swathDictSvr[tm].append(probsDict[tm].get('swath'))
                    snapDictSvr[tm].append(probsDict[tm].get('snap'))
            if hazardType == 'Prob_Tornado':
                for tm in probsDict:
                    swathDictTor[tm].append(probsDict[tm].get('swath'))
                    snapDictTor[tm].append(probsDict[tm].get('snap'))

        for tm in swathDictSvr:
            swathDictSvr[tm] = np.max(np.array(swathDictSvr[tm]), axis=0)
        for tm in snapDictSvr:
            snapDictSvr[tm] = np.max(np.array(snapDictSvr[tm]), axis=0)
        for tm in swathDictTor:
            swathDictTor[tm] = np.max(np.array(swathDictTor[tm]), axis=0)
        for tm in snapDictTor:
            snapDictTor[tm] = np.max(np.array(snapDictTor[tm]), axis=0)
            
        if writeToFile:
            if len(swathDictSvr) > 0:
                start = time.time()
                self._output(snapDictSvr, swathDictSvr, timeStamp, 'Severe')         
                print "[1] Took %.2f seconds" % (time.time()-start)
            if len(swathDictTor) > 0:
                self._output(snapDictTor, swathDictTor, timeStamp, 'Tornado')         
                print "[2] Took %.2f seconds" % (time.time()-start)

        return 1


    def getOutputProbGrids(self, event):                   
        downstreamPolys = event.get('downstreamPolys')
        if not downstreamPolys:
            return
        downstreamTimes = event.get('downstreamTimes')

        probTrend = self._getInterpolatedProbTrendColor(event)
        returnDict = self.makeGrid(downstreamPolys, probTrend, self._lons, self._lats, 
                                 self._xMin1, self._xMax1, self._yMax1, self._yMin1, downstreamTimes)
        
        return returnDict
       
    def getProbGrid(self, event):
        downstreamPolys = event.get('downstreamPolys')
         
        if not downstreamPolys:
           return
        
        probTrend = self._getInterpolatedProbTrendColor(event)
        probGridSwath = self.makeGrid(downstreamPolys, probTrend, self._lons, self._lats, 
                                 self._xMin1, self._xMax1, self._yMax1, self._yMin1)
        return probGridSwath, self._lons, self._lats
        

    def _getInterpolatedProbTrendColor(self, event):
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
        
        return oneMinuteProbs

    
    def makeGrid(self, downstreamPolys, probTrend, x1, y1, xMin1, xMax1, yMax1, yMin1, downstreamTimes=None):
        '''
        Almost all of the code in this method is pulled from 
        Karstens' (NSSL) PHI Prototype tool code with some minor modifications
        '''
        
    
        nx1, ny1 = y1.shape[0], x1.shape[0]
        x2,y2 = np.meshgrid(x1,y1)
        x3, y3 = x2.flatten(), y2.flatten()
        pnts1 = np.vstack((x3,y3)).T
    
        union = so.cascaded_union(downstreamPolys)
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
        #for k in range(objInt,int(gjson.properties['data']['duration']) + 1):
        
        ### Having probLocalSwath set to zeros before the k loop AND
        ### probabilitySwath *reset* to zeros WITHIN the k loop
        ### is critical for getting "eroding swaths" 
        probLocalSwath = np.zeros((len(y), len(x)))
        
        ### Running the k loop "backwards" so we can get "eroding swaths".
        ### FIXME: rework this with numpy for snapshot and full swath
        returnDict = {}
        for k in range(len(downstreamPolys), 0, -1):
            probabilitySwath = np.zeros((len(y1), len(x1)))
            probabilitySnap = np.zeros((len(y1), len(x1)))
            probLocalSnap = np.zeros((len(y), len(x)))
            k -= 1
            obj = map(list,list(downstreamPolys[k].exterior.coords))
            pathProjected = mPath.Path(obj) # option 1
            maskProjected = pathProjected.contains_points(pnts) # option 1
            #maskProjected = points_inside_poly(pnts, obj) # option 2
    
            maskProjected= maskProjected.reshape((nx,ny))
            distances = ndimage.distance_transform_edt(maskProjected)
            dMax = distances.max()
            if dMax == 0:
                dMax = 1.
            probMap = np.ceil(probTrend[k] - probTrend[k] * np.exp((pow(np.array((distances / dMax) * 1475.0),2) / -2.0) / pow(500,2)))
            
            probLocalSwath = np.maximum(probMap, probLocalSwath)
            
            ### using downstreamTimes as a flag for creating the intermediate grids
            ### speeds up processing by 50% if downstreamTimes is None hence the 
            ### odd "repeat" of these "if downstreamTimes" blocks
            if downstreamTimes is not None:
                
                for i in range(len(mask[0])):
                    iy = mask[0][i] - minY
                    ix = mask[1][i] - minX
                    probabilitySwath[mask[0][i]][mask[1][i]] = probLocalSwath[iy][ix]
                    probabilitySnap[mask[0][i]][mask[1][i]] = probMap[iy][ix]

                returnDict[downstreamTimes[k][0]] = {'snap':probabilitySnap, 'swath':probabilitySwath}
                
            
        if downstreamTimes is None:

            for i in range(len(mask[0])):
                iy = mask[0][i] - minY
                ix = mask[1][i] - minX
                probabilitySwath[mask[0][i]][mask[1][i]] = probLocalSwath[iy][ix]
                probabilitySnap[mask[0][i]][mask[1][i]] = probMap[iy][ix]
    
            return probabilitySwath 
    
        return returnDict
      
    def _output(self, snapProbsDict, swathProbsDict, timeStamp, eventType):
        '''
        Creates an output netCDF file in OUTPUTDIR (set near top)
        '''
        epoch = (timeStamp-datetime.datetime.fromtimestamp(0)).total_seconds()
        nowTime = datetime.datetime.fromtimestamp(time.time())
        for timeStepEpoch in sorted(snapProbsDict.keys()):
            outDir = os.path.join(OUTPUTDIR, nowTime.strftime("%Y%m%d_%H"), eventType, timeStamp.strftime("%Y%m%d_%H%M%S"))
    
            if not os.path.exists(outDir):
                try:
                    os.makedirs(outDir)
                except:
                    sys.stderr.write('Could not create PHI grids output directory:' +outDir+ '.  No output written')
    
            timeStepDateTime = datetime.datetime.fromtimestamp(timeStepEpoch/1000)
            minuteStep = ((timeStepEpoch/1000)-epoch)/60
            swath = swathProbsDict[timeStepEpoch]
            snap = snapProbsDict[timeStepEpoch]
            

            outputFilename = 'PHIGrid_' + eventType + '_' + timeStamp.strftime('%Y%m%d_%H%M%S') + '_f%02d'%minuteStep + '.nc'
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
                f.time_origin = timeStepDateTime.strftime("%Y-%m-%d %H:%M:%S")
                f.time_valid = timeStepDateTime.strftime("%Y-%m-%d %H:%M:%S")
                
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
                timeVar[:] = timeStepEpoch/1000

                #===============================================================
                # tauVar = f.createVariable('tau', 'i4', ('time',))
                # tauVar.long_name  = 'Tau'
                # tauVar.units = 'minutes since analysis'
                # tauVar.time_origin = timeStamp.strftime("%Y-%m-%d %H:%M:%S")
                # tauVar[:] = minuteStep
                #===============================================================
                
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
    
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()

    
        #########################################
    ### OVERRIDES
        
    def _timeStep(self):
        # Time step for downstream polygons and track points
        return 60 # secs

    #########################################
