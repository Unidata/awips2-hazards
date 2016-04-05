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

OUTPUTDIR = '/scratch/PHIGridTesting'
### FIXME: need better (dynamic or configurable) way to set domain corner
buff = 1.
lonPoints = 1200
latPoints = 1000
ulLat = 44.5 + buff
ulLon = -104.0 - buff

class ProbUtils(object):
    def __init__(self):
        
        self._timeStamp = datetime.datetime.fromtimestamp(0)
        
        # create null grid encompassing floater domain
        self._xMin1 = ulLon
        self._xMax1 = self._xMin1 + (0.01 * lonPoints)
        self._yMax1 = ulLat
        self._yMin1 = self._yMax1 - (0.01 * latPoints)
        self._lons = np.arange(self._xMin1,self._xMax1,0.01)
        self._lats = np.arange(self._yMin1+0.01,self._yMax1+0.01,0.01)

    def processEvents(self, eventSet, writeToFile=False):        
        probList = []

        for event in eventSet:
            probGrid = self.getProbGrid(event)
            if probGrid:
                probList.append(probGrid)
                
        if writeToFile:
            if probList:
                probsCube = np.array(probList)
                cumProbs = np.max(probsCube, axis=0)       
                self._outputPreviewGrid(cumProbs, self._lats, self._lons, self._timeStamp)         
        return probList

    def getProbGrid(self, event):                   
        ### Get polygon (swath)
        ### get initial polygon
        #poly = event.getHazardAttributes().get('downStreamPolygons')
        #if poly is None:
        #    continue
        polys = event.getHazardAttributes().get('downstreamPolys')
        if polys is None:
            return None
        
        ### Get probabilities
        ### FIXME: will want to do this in the long run, but will fudge for now
        ### Will want to update values of convectiveProbabilityTrend based on duration setting
        #probTrend = event.getHazardAttributes('convectiveProbabilityTrend')
        endTime = event.getEndTime()
        startTime = event.getStartTime()
        if startTime > self._timeStamp:
            self._timeStamp = startTime
        duration_seconds = (endTime-startTime).total_seconds()
        #numIvals = int(duration_seconds/60.0)
        numIvals = int(duration_seconds/float(self._timeStep()))
        probTrend = []
        for i in range(numIvals):
            probTrend.append(100-(i*100/numIvals))
        
        probGrid = self.makeGrid(polys, probTrend, self._lons, self._lats, 
                                 self._xMin1, self._xMax1, self._yMax1, self._yMin1)
                 
        return probGrid, self._lons, self._lats

    
    def makeGrid(self, swathPolygon, probTrend, x1, y1, xMin1, xMax1, yMax1, yMin1):
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
        #for k in range(objInt,int(gjson.properties['data']['duration']) + 1):
        for k in range(len(swathPolygon)):
            obj = map(list,list(swathPolygon[k].exterior.coords))
            pathProjected = mPath.Path(obj) # option 1
            maskProjected = pathProjected.contains_points(pnts) # option 1
            #maskProjected = points_inside_poly(pnts, obj) # option 2
    
            maskProjected= maskProjected.reshape((nx,ny))
            distances = ndimage.distance_transform_edt(maskProjected)
            dMax = distances.max()
            if dMax == 0:
                dMax = 1.
            probMap = np.ceil(probTrend[k] - probTrend[k] * np.exp((pow(np.array((distances / dMax) * 1475.0),2) / -2.0) / pow(500,2)))
            probLocal = np.maximum(probMap, probLocal)
    
        for i in range(len(mask[0])):
            iy = mask[0][i] - minY
            ix = mask[1][i] - minX
            probability[mask[0][i]][mask[1][i]] = probLocal[iy][ix]
    
        return probability
      
    def _output(self, cumProbs, lats, lons, timeStamp):
        '''
        Creates an output netCDF file in OUTPUTDIR (set near top)
        '''
        epoch = (timeStamp-datetime.datetime.fromtimestamp(0)).total_seconds()
        nowTime = datetime.datetime.fromtimestamp(time.time())
        outDir = os.path.join(OUTPUTDIR, nowTime.strftime("%Y%m%d_%H"))
        outputFilename = 'PHIGrid_' + timeStamp.isoformat() + '.nc'
        if not os.path.exists(outDir):
            try:
                os.makedirs(outDir)
            except:
                sys.stderr.write('Could not create PHI grids output directory:' +outDir+ '.  No output written')

        pathFile = os.path.join(outDir,outputFilename)
        
        try:
            f = netcdf.netcdf_file(pathFile,'w')
            f.title = 'Probabilistic Hazards Information grid'
            f.institution = 'NOAA Hazardous Weather Testbed; ESRL Global Systems Division and National Severe Storms Laboratory'
            f.source = 'AWIPS2 Hazard Services'
            f.history = 'Initially created ' + nowTime.isoformat()
            f.comment = 'These data are experimental'
            f.time_origin = timeStamp.strftime("%Y-%m-%d %H:%M:%S")
            
            f.createDimension('lats', len(lats))
            f.createDimension('lons', len(lons))
            f.createDimension('time', 1)
            
            timeVar = f.createVariable('time', 'f8', ('time',))
            timeVar.time = 'time'
            timeVar.units = 'seconds since 1970-1-1 0:0:0'
            timeVar[:] = epoch
            
            latVar = f.createVariable('lats', 'f', ('lats',))
            latVar.long_name = "latitude"
            latVar.units = "degrees_north"
            latVar.standard_name = "latitude"
            latVar[:] = lats
            
            lonVar = f.createVariable('lons', 'f', ('lons',))
            lonVar.long_name = "longitude"
            lonVar.units = "degrees_east"
            lonVar.standard_name = "longitude"
            lonVar[:] = lons
            
            probsVar = f.createVariable('PHIprobs', 'f', ('time', 'lats', 'lons'))
            probsVar.long_name = "Probabilistic Hazard Information grid probabilities"
            probsVar.units = "%"
            probsVar.coordinates = "time lat lon"
            probsVar[:] = cumProbs

            f.close()
            
        except:
            sys.stderr.write('Unable to open PHI Grid Netcdf file for output:'+pathFile)
            
            
        return pathFile
    
    def _outputPreviewGrid(self, cumProbs, lats, lons, timeStamp):
        '''
        Creates an output netCDF file in OUTPUTDIR (set near top)
        '''
        epoch = (timeStamp-datetime.datetime.fromtimestamp(0)).total_seconds()
        nowTime = datetime.datetime.fromtimestamp(time.time())
        outDir = os.path.join(OUTPUTDIR, nowTime.strftime("%Y%m%d_%H"))
        outputFilename = 'PreviewGrid_' + timeStamp.isoformat() + '.nc'
        if not os.path.exists(outDir):
            try:
                os.makedirs(outDir)
            except:
                sys.stderr.write('Could not create PHI grids output directory:' +outDir+ '.  No output written')

        pathFile = os.path.join(outDir,outputFilename)
        
        try:
            f = netcdf.netcdf_file(pathFile,'w')
            f.title = 'Probabilistic Hazards Information grid'
            f.institution = 'NOAA Hazardous Weather Testbed; ESRL Global Systems Division and National Severe Storms Laboratory'
            f.source = 'AWIPS2 Hazard Services'
            f.history = 'Initially created ' + nowTime.isoformat()
            f.comment = 'These data are experimental'
            f.time_origin = timeStamp.strftime("%Y-%m-%d %H:%M:%S")
            
            f.createDimension('lats', len(lats))
            f.createDimension('lons', len(lons))
            f.createDimension('time', 1)
            
            timeVar = f.createVariable('time', 'f8', ('time',))
            timeVar.time = 'time'
            timeVar.units = 'seconds since 1970-1-1 0:0:0'
            timeVar[:] = epoch
            
            latVar = f.createVariable('lats', 'f', ('lats',))
            latVar.long_name = "latitude"
            latVar.units = "degrees_north"
            latVar.standard_name = "latitude"
            latVar[:] = lats
            
            lonVar = f.createVariable('lons', 'f', ('lons',))
            lonVar.long_name = "longitude"
            lonVar.units = "degrees_east"
            lonVar.standard_name = "longitude"
            lonVar[:] = lons
            
            probsVar = f.createVariable('PHIprobs', 'f', ('time', 'lats', 'lons'))
            probsVar.long_name = "Probabilistic Hazard Information grid probabilities"
            probsVar.units = "%"
            probsVar.coordinates = "time lat lon"
            probsVar[:] = cumProbs

            f.close()
            
        except:
            sys.stderr.write('Unable to open PHI Grid Netcdf file for output:'+pathFile)
   
        return
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()

    
        #########################################
    ### OVERRIDES
        
    def _timeStep(self):
        # Time step for downstream polygons and track points
        return 60 # secs

    #########################################
