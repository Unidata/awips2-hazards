'''
Preview grid recommender for probabilistic hazard types.
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import time
import shapely
import shapely.ops as so
import shapely.geometry as sg
import GeometryFactory
from shapely.geometry import Polygon
import shapely.affinity as sa
from inspect import currentframe, getframeinfo
import os, sys
import numpy as np
import matplotlib
#matplotlib.use("Agg")
from matplotlib import path as mPath
import matplotlib.cm as cm
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
from scipy import ndimage
from scipy.io import netcdf

from SwathRecommender import Recommender as SwathRecommender
from ProbUtils import ProbUtils
from VisualFeatures import VisualFeatures
import JUtil
     
### FIXME: set path in configuration somewhere
OUTPUTDIR = '/scratch/PHIGridTesting'
### FIXME: need better (dynamic or configurable) way to set domain corner
#buff = 1.
#lonPoints = 1200
#latPoints = 1000
#ulLat = 44.5 + buff
#ulLon = -104.0 - buff

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('PreviewGridRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'PreviewGridRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Preview Grid Recommender'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['onlyIncludeTriggerEvent'] = True
        #metadata['includeEventTypes'] = [ "Prob_Severe", "Prob_Tornado" ]
        
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
        sys.stderr.write("Running Preview grid recommender.\n    trigger:    " +
                         str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
                         str(eventSet.getAttribute("eventType")) + "\n    origin:     " + 
                         str(eventSet.getAttribute("origin")) + "\n    hazard ID:  " +
                         str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
        
        #testing
        #self._trigger = {'trigger':eventSet.getAttribute("trigger"), 'attrs':eventSet.getAttribute("attributeIdentifiers")}
        sys.stderr.flush()
                
        for event in eventSet:
            if event.getHazardAttributes().get('showGrid') == True:
                selectedVisualFeatures = []
                polyDict = {}
                print "PreviewGridRec: show preview grid"
                probGrid, lons, lats = ProbUtils().getProbGrid(event)
                
                #print "probDict back in previewgridrecommender: ", probGrid, lons, lats #Operable through this line
                
                levels = np.linspace(0,100,6)

                X, Y = np.meshgrid(lons, lats)
                plt.figure()
                CS = plt.contour(X, Y, probGrid, levels=levels)
                plt.clabel(CS, inline=1, fontsize=10)
                plt.title('PHI Prob Contours at 2015-11-11T18:36:37')
                #plt.show()

                numContours = range(len(CS.levels))
                for c in range(0,(len(CS.levels) - 1)):
                    contourVal = CS.levels[c]
                    coords = CS.collections[c].get_paths()
                    
                    #print "Contour Interval: ", contourVal
        
                    #for x in range(0,len(coords)):
                        #print "Polygon Index: ", x, ": ", coords[x].vertices
            
                points = coords[0].vertices
                poly = Polygon(points)
                poly = GeometryFactory.createPolygon(poly)
                print "PreviewGridRec: bounds: ", poly.bounds
                
                #ProbUtils.createPolygons(self,cumProbs)
                #self.createPolygons(cumProbs)
                #self._outputPreviewGrid(cumProbs, lats, lons, timeStamp)
                
                        ### Should match PHI Prototype Tool
                #colors =  {
                #(0,20): { "red": 102/255.0, "green": 224/255.0, "blue": 102/255.0 }, 
                #(20,40): { "red": 255/255.0, "green": 255/255.0, "blue": 102/255.0 }, 
                #(40,60): { "red": 255/255.0, "green": 179/255.0, "blue": 102/255.0 }, 
                #(60,80): { "red": 255/255.0, "green": 102/255.0, "blue": 102/255.0 }, 
                #(80,101): { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0 }
                #}
                
                #------testing
                colorDict = { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0 }
                
                gridPreviewPoly = {
                   "identifier": "gridPreview_" + str(10),
                    "borderColor": colorDict,
                    "fillColor": colorDict,
                    "geometry": {
                        (VisualFeatures.datetimeToEpochTimeMillis(event.getStartTime()), VisualFeatures.datetimeToEpochTimeMillis(event.getEndTime())): poly
                    }
                }
                selectedFeatures = event.getSelectedVisualFeatures()
                print "PreviewGridRec: Selected Features before appending polygons: ", selectedFeatures
                selectedFeatures.append(gridPreviewPoly)
                event.setSelectedVisualFeatures(VisualFeatures(selectedFeatures))
                print "PreviewGridRec: Selected Features after appending polygons: ", selectedFeatures
                
            elif event.getHazardAttributes().get('showGrid', False) == False:
                pass
            
                #  Nate -- FYI - it is helpful to include a source in a print statement
                #     to make it easy to find an remove later :)
                
                #print "PreviewGridRecommender -- do not show preview grid"
                #selectedVisualFeatures = event.getSelectedVisualFeatures()
                #newFeatures = []
                #for feature in selectedVisualFeatures:
                #    if not feature.get('identifier').find('grid')>=0:
                #        newFeatures.append(feature)
                #        
                #event.setSelectedVisualFeatures(newFeatures)                
        return

    def createPolygons(self, pathFile, cumProbs, lats, lons):
    
        from scipy.io import netcdf
        import numpy as np
        import matplotlib
        import numpy as np
        import matplotlib.cm as cm
        import matplotlib.mlab as mlab
        import matplotlib.pyplot as plt
        from VisualFeatures import VisualFeatures

        #print "PreviewGridRec: ", pathFile
        #dataFile = netcdf.netcdf_file('/scratch/PHIGridTesting/20160331_16/PHIGrid_2015-11-11T18:36:00.nc', 'r')
        dataFile = netcdf.netcdf_file(pathFile, 'r')

        latsVar = dataFile.variables['lats']
        lonsVar = dataFile.variables['lons']
        probsVar = dataFile.variables['PHIprobs']

        latsArr = latsVar[:].copy()
        lonsArr = lonsVar[:].copy()
        probsArr = probsVar[:].copy()[0]

        #print probsArr

        levels = np.linspace(0,100,6)

        X, Y = np.meshgrid(lonsArr, latsArr)
        plt.figure()
        CS = plt.contour(X, Y, probsArr, levels=levels)
        plt.clabel(CS, inline=1, fontsize=10)
        plt.title('PHI Prob Contours at 2015-11-11T18:36:37')
        #plt.show()

        numContours = range(len(CS.levels))
        for c in range(0,(len(CS.levels) - 1)):
            contourVal = CS.levels[c]
            coords = CS.collections[c].get_paths()
            #print "Contour Interval: ", contourVal
        
            #for x in range(0,len(coords)):
                #print "Polygon Index: ", x, ": ", coords[x].vertices
            
        points = coords[0].vertices
        poly = Polygon(points)
        print "PreviewGridRec: bounds: ", poly.bounds
    
        return 
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()
                      
"""
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
"""       
def __str__(self):
    return 'Preview Grid Recommender'