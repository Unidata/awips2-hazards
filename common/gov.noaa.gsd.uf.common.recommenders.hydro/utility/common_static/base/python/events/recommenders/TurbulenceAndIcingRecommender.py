'''
TurbulenceAndIcingRecommender creates hazard events in HS based on Turb and Icing grids in GFE.
'''
import datetime
import EventFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler
import shapely
from shapely.geometry import Polygon
import os, sys
import AdvancedGeometry
import numpy as np
import matplotlib.pyplot as plt
from scipy.io import netcdf
import AviationUtils

#########
OUTPUTDIR = '/home/nathan.hardin/Desktop/GFEtoHS/GFEGrids/'

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('TurbulenceAndIcingRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'TurbulenceAndIcingRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'TurbulenceAndIcingRecommender'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['onlyIncludeTriggerEvent'] = True
        
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
        
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        Runs the TurbulenceAndIcingRecommender
        '''
        import sys
        sys.stderr.write("Running TurbulenceAndIcingRecommender.\n")

        sys.stderr.flush()
        
        lats, lons = self.createLatLonGrid()       
        #create events for both turbulence and icing
        for filename in os.listdir(OUTPUTDIR):
            if "turb" in filename and filename.endswith('.nc'):
                turbFcstDict = self.createTurbFcstDict(filename)
                self.hazardEventLoader(eventSet, turbFcstDict['high'], lats, lons, [1.99])
                self.hazardEventLoader(eventSet, turbFcstDict['low'], lats, lons, [1.99])
            elif "icing" in filename and filename.endswith('.nc'):
                icingFcstDict = self.createIcingFcstDict(filename)
                self.hazardEventLoader(eventSet, icingFcstDict, lats, lons, [1.99])               

        return eventSet
    
    def hazardEventLoader(self,eventSet, fcstDict, lats, lons, contourInt):
        for key in fcstDict:
            coverage = fcstDict[key]['coverage']
            top = fcstDict[key]['top']
            base = fcstDict[key]['base']
            uniqueTop = np.unique(top)
            uniqueBase = np.unique(base)
            uniqueBaseList = []
            for x in np.nditer(uniqueBase):
                if x == 0:
                    pass
                else:
                    uniqueBaseList.append(x)
            
            #lowest base and highest top for each grid
            baseMin = np.amin(uniqueBaseList)
            topMax = np.amax(uniqueTop)
            
            baseVal = AviationUtils.AviationUtils().getHeightVal(baseMin)
            topVal = AviationUtils.AviationUtils().getHeightVal(topMax)  
            
            vertices = self.getVertices(lons,lats,coverage, contourInt)
            
            #if there's a contour, create polygon and create hazard event
            if vertices:
                polygonList = self.createPolygonList(vertices)    
                     
                for polygon in polygonList:
                    self.createHazardEvents(eventSet, polygon,fcstDict,key,baseVal,topVal)             
        
        return
    
    def createIcingFcstDict(self,filename):
        #read netCDF file and get different grids
        f = netcdf.netcdf_file(OUTPUTDIR + filename, 'r')
        gridHistory = f.variables['IcingBase_SFC_GridHistory'][:]
        icingCoverage = f.variables['IcingCoverage_SFC'][:]
        icingBase = f.variables['IcingBase_SFC'][:]
        icingTop = f.variables['IcingTop_SFC'][:]
        f.close()
        
        icingFcstDict = {}
        for i in range(0,len(gridHistory)):
            startTime = ''
            endTime = ''
            startTimeList = gridHistory[i][46:56]
            for time in startTimeList:
                startTime += time
            endTimeList = gridHistory[i][57:68]
            for time in endTimeList:
                endTime += time
            
            icingFcstDict[startTime] = {}
            icingFcstDict[startTime]['endTime'] = endTime
            icingFcstDict[startTime]['base'] = icingBase[i]
            icingFcstDict[startTime]['top'] = icingTop[i]             
            icingFcstDict[startTime]['coverage'] = icingCoverage[i]
            icingFcstDict[startTime]['phenomenon'] = 'Icing'
            icingFcstDict[startTime]['baseMetaID'] = 'icingBottom'
            icingFcstDict[startTime]['topMetaID'] = 'icingTop'
                             
        return icingFcstDict
    
    def createTurbFcstDict(self,filename):
        f = netcdf.netcdf_file(OUTPUTDIR + filename, 'r')
        gridHistory = f.variables['TurbHighLvlCoverage_SFC_GridHistory'][:]
        turbCoverageHigh = f.variables['TurbHighLvlCoverage_SFC'][:]
        turbCoverageLow = f.variables['TurbLowLvlCoverage_SFC'][:]
        turbHighBase = f.variables['TurbHighLvlBase_SFC'][:]
        turbLowBase = f.variables['TurbLowLvlBase_SFC'][:]
        turbHighTop = f.variables['TurbHighLvlTop_SFC'][:]
        turbLowTop = f.variables['TurbLowLvlTop_SFC'][:]
        f.close()
        
        turbFcstDict = {}
        turbFcstDict['high'] = {}
        turbFcstDict['low'] = {}
        
        for i in range(0,len(gridHistory)):
            startTime = ''
            endTime = ''
            startTimeList = gridHistory[i][56:66]
            for time in startTimeList:
                startTime += time
            endTimeList = gridHistory[i][67:77]
            for time in endTimeList:
                endTime += time

            turbFcstDict['high'][startTime] = {}
            turbFcstDict['high'][startTime]['endTime'] = endTime
            turbFcstDict['high'][startTime]['base'] = turbHighBase[i]
            turbFcstDict['high'][startTime]['top'] = turbHighTop[i]
            turbFcstDict['high'][startTime]['coverage'] = turbCoverageHigh[i]
            turbFcstDict['high'][startTime]['phenomenon'] = 'Turbulence'
            turbFcstDict['high'][startTime]['baseMetaID'] = "highLevelTurbulenceBetweenFLBottom"
            turbFcstDict['high'][startTime]['topMetaID'] = "highLevelTurbulenceBelowFLTop"            
            
            turbFcstDict['low'][startTime] = {}
            turbFcstDict['low'][startTime]['endTime'] = endTime
            turbFcstDict['low'][startTime]['base'] = turbHighBase[i]
            turbFcstDict['low'][startTime]['top'] = turbHighTop[i]
            turbFcstDict['low'][startTime]['coverage'] = turbCoverageHigh[i]
            turbFcstDict['low'][startTime]['phenomenon'] = 'Turbulence'
            turbFcstDict['low'][startTime]['baseMetaID'] = "turbulenceBetweenFLBottom"
            turbFcstDict['low'][startTime]['topMetaID'] = "turbulenceBelowFLTop"                         
        
        return turbFcstDict
    
    def createLatLonGrid(self):
        #read lat/lon npz grid
        lats = np.load(OUTPUTDIR + 'aawu_lat_grid.npz')
        lons = np.load(OUTPUTDIR + 'aawu_lon_grid.npz')
        #kludge so we can view over CONUS
        lats = lats - 20
        lons = lons + 60
        
        return lats, lons         
        
    
    def createHazardEvents(self, eventSet, polygon, fcstDict, key, baseVal, topVal):
        poly = GeometryFactory.createPolygon(polygon)
        poly = AdvancedGeometry.createShapelyWrapper(poly, 0) 
        #create hazard event
        currentTime = datetime.datetime.now()
        startTime = datetime.datetime.utcfromtimestamp(int(key))
        endTime = datetime.datetime.utcfromtimestamp(int(fcstDict[key]['endTime'])) 
              
        hazardEvent = EventFactory.createEvent()
        hazardEvent.setCreationTime(currentTime)
        hazardEvent.setStartTime(startTime)
        hazardEvent.setEndTime(endTime)
        hazardEvent.setHazardStatus("pending")
        hazardEvent.setHazardMode("O")
        
        hazardEvent.setPhenomenon(fcstDict[key]['phenomenon']) 
        
        #set metadata top and bottom based on top/bottom grid
        hazardEvent.set(fcstDict[key]['baseMetaID'], baseVal)
        hazardEvent.set(fcstDict[key]['topMetaID'], topVal)
              
        hazardEvent.setGeometry(poly)
        eventSet.add(hazardEvent)
                 
        return
    
    def createPolygonList(self, vertices):
        polygonList = []
        for vertex in vertices:
            newPolygon = []
            v = vertex.vertices
            for i in range(0,len(v)):
                newVertexList = []
                newVertexList.append(v[i,0])
                newVertexList.append(v[i,1])
                newPolygon.append(newVertexList)
            poly = Polygon(newPolygon)
            polygonList.append(poly)
            
        return polygonList
        
    def getVertices(self, lons, lats, coverage, contourInt):
        #create contour of coverage and take those lat/lons for hazard event creation
        plt.figure()
        cs = plt.contour(lons,lats,coverage,contourInt)
        plt.clabel(cs,inline=1,fontsize=10)
        #plt.show()
        vertices = cs.collections[0].get_paths()        
        
        return vertices
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()       
                      
def __str__(self):
    return 'TurbulenceAndIcingRecommender'