'''
PHI Grid recommender for probabilistic hazard types.
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
import os, sys
import numpy as np
import matplotlib
#matplotlib.use("Agg")
from matplotlib import path as mPath
from scipy import ndimage
from scipy.io import netcdf

from SwathRecommender import Recommender as SwathRecommender
import JUtil
     
### FIXME: set path in configuration somewhere
OUTPUTDIR = '/scratch/PHIGridTesting'
### FIXME: need better (dynamic or configurable) way to set domain corner
buff = 1.
lonPoints = 1200
latPoints = 1000
ulLat = 44.5 + buff
ulLon = -104.0 - buff

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('PHIGridRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'PHIGridRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'PHI Grid Recommender'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['includeEventTypes'] = [ "Prob_Severe", "Prob_Tornado" ]
        
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
        sys.stderr.write("Running PHI grid recommender.\n    trigger:    " +
                         str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
                         str(eventSet.getAttribute("eventType")) + "\n    origin:     " + 
                         str(eventSet.getAttribute("origin")) + "\n    hazard ID:  " +
                         str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
        sys.stderr.flush()
        
        swathRec = SwathRecommender()
        eventSet.addAttribute('origin',"PHI_GridRecommender")
        swathRecEventSet = swathRec.execute(eventSet, None, None)
               
        self.processEvents(swathRecEventSet)
        return 

    def processEvents(self, eventSet):
        
        timeStamp = datetime.datetime.fromtimestamp(0)
        
        # create null grid encompassing floater domain
        xMin1 = ulLon
        xMax1 = xMin1 + (0.01 * lonPoints)
        yMax1 = ulLat
        yMin1 = yMax1 - (0.01 * latPoints)
        lons = np.arange(xMin1,xMax1,0.01)
        lats = np.arange(yMin1+0.01,yMax1+0.01,0.01)

        
        probList = []
        for event in eventSet:
            if not event.getHazardAttributes().get('removeEvent'):  ### UNNEEDED ONCE CODE UPDATED TO REMOVE THE EVENT
                
                ### Get polygon (swath)
                ### get initial polygon
                polys = event.getHazardAttributes().get('downstreamPolys')
                if polys is None:
                    continue
                
                ### Get probabilities
                ### FIXME: will want to do this in the long run, but will fudge for now
                ### Will want to update values of convectiveProbabilityTrend based on duration setting
                #probTrend = event.getHazardAttributes('convectiveProbabilityTrend')
                endTime = event.getEndTime()
                startTime = event.getStartTime()
                if startTime > timeStamp:
                    timeStamp = startTime
                duration_seconds = (endTime-startTime).total_seconds()
                numIvals = int(duration_seconds/float(self._timeStep()))
                probTrend = []
                for i in range(numIvals):
                    probTrend.append(100-(i*100/numIvals))

                probGrid = self.makeGrid(polys, probTrend, lons, lats, xMin1, xMax1, yMax1, yMin1)
                probList.append(probGrid)
                    
        if len(probList):
            probsCube = np.array(probList)
            cumProbs = np.max(probsCube, axis=0)
            
            self._output(cumProbs, lats, lons, timeStamp)
            
        return

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
            
            
        return
            

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
        # perform 2D Gausian with p(t) on points in polygon
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

        
    def __str__(self):
        return 'PHI Grid Recommender'

    #########################################
    ### OVERRIDES -- 
    # TODO -- This needs to be on one override file used by all the 
    #   Prob Convective Recommenders 
        
    def _timeStep(self):
        # Time step for downstream polygons and track points
        return 60 # secs

    #########################################

