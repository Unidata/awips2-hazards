'''
Utility for Aviation Products
'''
import shapely
import EventFactory, EventSetFactory, GeometryFactory
import AdvancedGeometry
import numpy as np
import datetime, math
from math import sin, cos, sqrt, atan2, radians, pi
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
import TimeUtils
from VisualFeatures import VisualFeatures
import Domains
import csv

class AviationUtils:
    
    def getHeightVal(self, arrayVal):
        if arrayVal >= 18000.0:
            heightVal = 'FL%s'%str(arrayVal)[:3]
        else:
            heightVal = '%s%s' % ('0', str(arrayVal)[:2])

        return heightVal
    
    def createFreezingLevelInformation(self):
        import matplotlib.pyplot as plt
        
        grid = np.load('/home/nathan.hardin/Desktop/GFEtoHS/GFEGrids/freezingLevelData.npz')
        lats = grid['lat']
        lons = grid['lon']
        t9Grid = grid['t9Grid']
        t12Grid = grid['t12Grid']
        t15Grid = grid['t15Grid']
        
        #find minimum of three times
        gridMin = np.minimum(t9Grid,t12Grid)
        gridMin = np.minimum(gridMin,t15Grid)
        
        minimum = np.amin(gridMin)
        maximum = np.amax(gridMin)
        
        minimum = int(math.ceil(minimum/100.0))*100
        maximum = int(math.ceil(maximum/100.0))*100
        
        minimum = self.getHeightVal(minimum)
        maximum = self.getHeightVal(maximum)
        
        freezingLevelInformation = str(minimum) + '-' + str(maximum)        
        
        return freezingLevelInformation
    
    def createVolcanoDict(self):
        f = open(self.volcanoFilePath(), 'rb')
        reader = csv.reader(f)
        headers = reader.next()
        column = {}
        for h in headers:
            column[h] = []
        
        for row in reader:
            for h, v in zip(headers, row):
                column[h].append(v)
        
        volcanoName = column['VolcanoName']
        volcanoLatitude = column['LatitudeDecimal']
        volcanoLongitude = column['LongitudeDecimal']
        volcanoNumber = column['VolcanoNumber']
        volcanoElevationMeters = column['SummitElevM']
        volcanoSubregion = column['VolcanicSubregion']
        
        volcanoDict = {}
        for i in range(0,len(volcanoName)):
            volcanoDict[volcanoName[i]] = [volcanoNumber[i], volcanoLatitude[i], volcanoLongitude[i], volcanoElevationMeters[i], volcanoSubregion[i]]        
        
        return volcanoDict    
        
    def getGeometryType(self, hazardEvent):        
        for g in hazardEvent.getFlattenedGeometry():
            geomType = g.geom_type           
        
        return geomType
    
    def updateVisualFeatures(self, event, vertices, polyPoints):
        startTime = event.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(event.getEndTime())
        
        self._originalGeomType = event.get('originalGeomType')
        self._width = event.get('convectiveSigmetWidth')

        polygonArea = self.polygonArea(event, self._originalGeomType, self._width)
        label = self.createLabel(event, polygonArea)
        
        eventID = event.getEventID()
        selectedFeatures = []
        VOR_points = event.getHazardAttributes().get('VOR_points')
               
        if self._originalGeomType != 'Polygon':
            poly = GeometryFactory.createPolygon(polyPoints)
            basePoly = vertices
            if self._originalGeomType == 'Point':
                basePoly = GeometryFactory.createPoint(basePoly)
            elif self._originalGeomType == 'LineString':
                basePoly = GeometryFactory.createLineString(basePoly)
            basePoly = AdvancedGeometry.createShapelyWrapper(basePoly, 0)       
        else:
            poly = GeometryFactory.createPolygon(VOR_points)
            try:
                basePoly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(vertices), 0)
            except ValueError:
                basePoly = event.getGeometry()
        event.setGeometry(basePoly)
            
        poly = AdvancedGeometry.createShapelyWrapper(poly, 0)
        
        if self._originalGeomType != 'Polygon':       
            borderColorHazard = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 } #yellow
            fillColorHazard = {"red": 1, "green": 1, "blue": 1, "alpha": 0}
            borderColorBase = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 } #white
            fillColorBase = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 0.0 }
            hazardPolyVisibility = "always"
            basePolyVisibility = "selected"
            hazardPolyLabel = label
            basePolyLabel = ""
        else:
            borderColorHazard = "eventType"
            fillColorHazard = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 0.0 }
            borderColorBase = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 } #yellow
            fillColorBase = {"red": 1, "green": 1, "blue": 1, "alpha": 0}
            hazardPolyVisibility = "selected"
            basePolyVisibility = "always"
            hazardPolyLabel = ""
            basePolyLabel = label        
        
        hazardEventPoly = {
            "identifier": "hazardEventPolygon_" + eventID,
            "visibilityConstraints": hazardPolyVisibility,
            "diameter": "eventType",
            "borderColor": borderColorHazard,
            "fillColor": fillColorHazard,
            "label": hazardPolyLabel,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": basePolyVisibility,
            "dragCapability": "all",
            "borderThickness": "eventType",
            "diameter": "eventType",
            "borderColor": borderColorBase,
            "fillColor": fillColorBase,
            "label": basePolyLabel,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): basePoly
            }
        }                 

        selectedFeatures.append(basePoly)                      
        selectedFeatures.append(hazardEventPoly)            
        event.setVisualFeatures(VisualFeatures(selectedFeatures))

        return True
    
    def createLabel(self, event, polygonArea):
        domain = event.getHazardAttributes().get('convectiveSigmetDomain')
        direction = event.getHazardAttributes().get('convectiveSigmetDirection')
        speed = event.getHazardAttributes().get('convectiveSigmetSpeed')
        cloudTop = event.getHazardAttributes().get('convectiveSigmetCloudTop')
        cloudTopText = event.getHazardAttributes().get('convectiveSigmetCloudTopText')
        status = event.getStatus()
        
        if status == 'ISSUED':
            area = str(polygonArea) + "sq mi"
            numberStr = event.getHazardAttributes().get('convectiveSigmetNumberStr')
            number = "\n" + numberStr + domain[0]
        
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL" + str(cloudTopText)
            else:
                tops = "\nN/A"                
            
            motion = "\n" + str(direction)+"@"+str(speed)+"kts"
            label = number + area + tops + motion
        else:
            area = str(polygonArea) + "sq mi"
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL" + str(cloudTopText)
            
            motion = "\n" + str(direction)+"@"+str(speed)+"kts"                        
            label = area + tops + motion       
        
        return label         
    
    def polygonArea(self, hazardEvent, geomType, width):
        hazGeometry = hazardEvent.getFlattenedGeometry()
        try:
            for g in hazGeometry.geoms:
                    vertices = shapely.geometry.base.dump_coords(g)
        except AttributeError:
            vertices = shapely.geometry.base.dump_coords(hazGeometry)
        
        if geomType == 'Point':
            polygonArea = pi * width**2
        elif geomType == 'LineString':
            width = width*1.15078
            polygonArea = 0
            for i in range(0, len(vertices)-1):
                lat1 = radians(vertices[i][1])
                lat2 = radians(vertices[i+1][1])
                lon1 = radians(vertices[i][0])
                lon2 = radians(vertices[i+1][0])

                dlon = lon2 - lon1
                dlat = lat2 - lat1
                a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
                c = 2 * atan2(sqrt(a), sqrt(1-a))
                d = 3961 * c 
                polygonArea = polygonArea + d*width
        elif geomType == 'Polygon':
            polygonArea = 0
            if len(vertices) >= 3:
                for i in range(0, len(vertices)-1):
                    area = radians(vertices[i+1][0] - vertices[i][0]) * (2 + sin(radians(vertices[i][1])) + sin(radians(vertices[i+1][1])))
                    polygonArea = polygonArea + area

            polygonArea = abs(polygonArea * 6378137.0 * 6378137.0 / 2.0)
            polygonArea = polygonArea * 3.861e-7
        
        polygonArea = int(round(polygonArea))
        return polygonArea
    
    def lineToPolygon(self, vertices, width):
        leftBuffer = [] 
        rightBuffer = []
        # Compute for start point
        bearing = self.gc_bearing(vertices[0],vertices[1])
        leftBuffer.append(self.gc_destination(vertices[0],width,(bearing-90.0)))
        rightBuffer.append(self.gc_destination(vertices[0],width,(bearing+90.0)))
        # Compute distance from points in middle of line
        for n in range(1,len(vertices)-1):
            b_1to2 = self.gc_bearing(vertices[n-1],vertices[n])
            b_2to3 = self.gc_bearing(vertices[n],vertices[n+1])
            theta = (b_2to3-b_1to2)/2.0
            bearing = b_1to2 + theta
            D = width / math.sin(math.radians(theta+90.0))
            leftBuffer.append(self.gc_destination(vertices[n],D,(bearing-90.0)))
            rightBuffer.append(self.gc_destination(vertices[n],D,(bearing+90.0)))
            # Compute for end point, right and left reversed for different direction
        bearing = self.gc_bearing(vertices[-1],vertices[-2])
        leftBuffer.append(self.gc_destination(vertices[-1],width,(bearing+90.0)))
        rightBuffer.append(self.gc_destination(vertices[-1],width,(bearing-90.0)))
        # Construct final corridor by combining both sides 
        poly = leftBuffer + rightBuffer[::-1] + [leftBuffer[0]]  

        return poly
    
    def pointToPolygon(self, vertices, width):
        buffer = []
        if len(vertices) == 1:
            width = width/2
            for bearing in range(0,360,15):
                loc = self.gc_destination(vertices[0],width,bearing)
                buffer.append((round(loc[0],2),round(loc[1],3)))
            poly = buffer
            
        return poly                
                 
    def createPolygon(self,vertices,width,originalGeomType):
        vertices = [x[::-1] for x in vertices]
        width = float(width) * 1.852  # convert Nautical Miles to KM
        
        if originalGeomType == 'Point':
            poly = self.pointToPolygon(vertices,width)
        elif originalGeomType == 'LineString':
            poly = self.lineToPolygon(vertices,width)
                
        poly = [x[::-1] for x in poly]    

        return poly 

    def gc_bearing(self,latlong_1, latlong_2):
        lat1, lon1 = latlong_1
        lat2, lon2 = latlong_2
        rlat1, rlon1 = math.radians(lat1), math.radians(lon1)
        rlat2, rlon2 = math.radians(lat2), math.radians(lon2)
        dLat = math.radians(lat2 - lat1)
        dLon = math.radians(lon2 - lon1)

        y = math.sin(dLon) * math.cos(rlat2)
        x = math.cos(rlat1) * math.sin(rlat2) - \
            math.sin(rlat1) * math.cos(rlat2) * math.cos(dLon)
        bearing = math.degrees(math.atan2(y,x))
        return bearing

    def gc_destination(self,latlong_1, dist, bearing):
        R = 6378.137 # earth radius in km
        lat1, lon1 = latlong_1
        rlat1, rlon1 = math.radians(lat1), math.radians(lon1)
        d = dist
        bearing = math.radians(bearing)

        rlat2 = math.asin(math.sin(rlat1) * math.cos(d/R) + \
          math.cos(rlat1) * math.sin(d/R) * math.cos(bearing))
        rlon2 = rlon1 + math.atan2(math.sin(bearing) * math.sin(d/R) * math.cos(rlat1), \
          math.cos(d/R) - math.sin(rlat1) * math.sin(rlat2))
        lat2 = math.degrees(rlat2)
        lon2 = math.degrees(rlon2)
        if lon2 > 180.: lon2 = lon2 - 360.
        latlong_2 = (lat2, lon2)
        return latlong_2           
    
    def selectDomain(self, hazardEvent, vertices, geomType, trigger):
        domains = Domains.AviationDomains
        
        if trigger == 'modification':
            pass
        else:
            hazGeometry = hazardEvent.getFlattenedGeometry()
            for g in hazGeometry.geoms:
                vertices = shapely.geometry.base.dump_coords(g)
        
        #create longitude list including all vertices
        hazardLonsList = []   
        for vertice in vertices:
            hazardLonsList.append(vertice[0])
        if geomType == 'Polygon':
            hazardLonsList.pop()
        
        softDict = {}
        sumDict = {}
        for domain in domains:
            sumDict[domain.domainName()] = 0
            
        #Iterate through longitudes to assess where they fall relative to boundaries
        #add points in soft areas to their own lists
        #find absolute difference from hard boundary and sum value
        for lon in hazardLonsList:
            for domain in domains:
                if type(domain.lowerSoftBound()) is list:
                    if lon <= domain.lowerSoftBound()[0] and lon > domain.upperSoftBound()[0]:
                        if domain.domainName() in softDict:
                            softDict[domain.domainName()].append(lon)
                        else:
                            softDict[domain.domainName()] = [lon]
                        sumDict[domain.domainName()] = sumDict[domain.domainName()] + abs(lon+(abs(domain.lowerSoftBound()[0])))
                    elif lon <= domain.lowerSoftBound()[1] and lon > domain.upperSoftBound()[1]:
                        if domain.domainName() in softDict:
                            softDict[domain.domainName()].append(lon)
                        else:
                            softDict[domain.domainName()] = [lon]
                        sumDict[domain.domainName()] = sumDict[domain.domainName()] + abs(lon+(abs(domain.upperSoftBound()[1])))
                else:
                    if lon <= domain.lowerSoftBound() and lon > domain.upperSoftBound():
                        if domain.domainName() in softDict:
                            softDict[domain.domainName()].append(lon)
                        else:
                            softDict[domain.domainName()] = [lon]
                        sumDict[domain.domainName()] = sumDict[domain.domainName()] + abs(lon + (abs(domain.lowerSoftBound())))    
        
        for domain in domains:
            #all points fall within a domain (none in soft boundaries)
            if domain.lowerLonBound() == None:
                if all(lon >= domain.upperLonBound() for lon in hazardLonsList) == True:
                    convectiveSigmetAreas = domain.domainName()
            elif domain.upperLonBound() == None:
                if all(lon <= domain.lowerLonBound() for lon in hazardLonsList) == True:
                    convectiveSigmetAreas = domain.domainName()
            elif all(lon <= domain.lowerLonBound() and lon >= domain.upperLonBound() for lon in hazardLonsList) == True:
                convectiveSigmetAreas = domain.domainName()
            #if any point falls within a hard boundary and other points exist in allowable soft boundary
            elif domain.absMinBound() == None:
                if (any(lon >= domain.upperLonBound() for lon in hazardLonsList) == True) and (all(lon >= domain.absMaxBound() for lon in hazardLonsList) == True):
                    convectiveSigmetAreas = domain.domainName()    
            elif domain.absMaxBound() == None:
                if (any(lon >= domain.lowerLonBound() for lon in hazardLonsList) == True) and (all(lon <= domain.absMinBound() for lon in hazardLonsList) == True):
                    convectiveSigmetAreas = domain.domainName()
            elif (any(lon >= domain.lowerLonBound() and lon <= domain.upperLonBound() for lon in hazardLonsList) == True) and \
                 (all(lon >= absMaxBound() and lon <= absMinBound() for lon in hazardLonsList) == True):
                convectiveSigmetAreas = domain.domainName()       
            
        #all points in the soft boundaries
        maxLength = 0
        domain = []
        if bool(softDict) == True:
            #iterate through the keys (domains) and check how many points from each fall in soft boundary
            #select whichever domain has more points, or if points are equal, select whichever has the
            #maximum absolute difference in longitude from the hard boundary 
            for key, value in softDict.iteritems():
                if len(value) > maxLength:
                    domain = []
                    domain.append(key)
                    maxLength = len(value)
                elif len(value) == maxLength:
                    domain.append(key)       
            if len(domain) == 1:
                convectiveSigmetAreas = domain[0]
            else:
                import operator
                convectiveSigmetAreas = max(sumDict.iteritems(), key=operator.itemgetter(1))[0]            
        hazardEvent.set('convectiveSigmetDomain', convectiveSigmetAreas)                              
        
        return convectiveSigmetAreas        
        
    def boundingStatement(self, hazardEvent, geomType, TABLEFILE, vertices, trigger):
        
        per_row = []
        with open(TABLEFILE, 'r') as fr:
            for line in fr:
                if not line.startswith("!"):
                    per_row.append(line.split())
                    
        self.phenomenon = hazardEvent.getPhenomenon()
        if self.phenomenon == 'SIGMET':
            boundingStatement = self.findClosestPoint(per_row,trigger,hazardEvent,geomType,vertices,7)
            
            if any(char.isdigit() for char in boundingStatement):
                boundingStatement = self.findClosestPoint(per_row,trigger,hazardEvent,geomType,vertices,6)
                
        elif self.phenomenon in ['LLWS', 'Strong_Surface_Wind', 'Turbulence', 'Mountain_Obscuration',
                                    'IFR', 'Icing', 'Multiple_Freezing_Levels']:
            boundingStatement = self.findClosestPoint(per_row,trigger,hazardEvent,geomType,vertices,50)        
        
        hazardEvent.set('boundingStatement', boundingStatement)
        
        return boundingStatement
    
    def findClosestPoint(self,per_row,trigger,hazardEvent,geomType,vertices,numvertices):
        if geomType is not 'Point':
            boundingStatement = 'FROM '
        else:
            boundingStatement = ''
                    
        lats = []
        lons = []
        names = []
        stid = []
        
        # ! SNAP.TBL SAMPLE FORMAT
        # !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        # !
        # !STID    STNM   NAME                            ST CO   LAT    LON   ELV  PRI
        # !(8)     (6)    (32)                            (2)(2)  (5)    (6)   (5)  (2)
        # YSJ00000      9 YSJ                              -  -   4532  -6588     0  1
        # YSJ00001      9 20N_YSJ                          -  -   4565  -6588     0  2
        # YSJ00002      9 30N_YSJ                          -  -   4582  -6588     0  2
        # YSJ00003      9 40N_YSJ                          -  -   4599  -6588     0  2

        for row in per_row:
            stid.append(row[0])
            lats.append(row[5])
            lons.append(row[6])
            names.append(row[2])

        # add decimal points 
        latNew = []
        for lat in lats:
            if len(lat) == 4:
                latNew.append(float(lat[:2] + '.' + lat[2:]))
            else:
                latNew.append(float(lat))
        lats = latNew

        lonNew = []
        for lon in lons:
            if len(lon) == 5:
                lonNew.append(float(lon[:3] + '.' + lon[3:]))
            elif len(lon) == 6:
                lonNew.append(float(lon[:4] + '.' + lon[4:]))
            else:
                lonNew.append(float(lon))    
        lons = lonNew
        
        vorLat = []
        vorLon = []        
        
        if trigger == 'modification':
            pass
        elif trigger == 'swathGeneration':
            vertices = shapely.geometry.base.dump_coords(vertices)
        else:
            for g in hazardEvent.getFlattenedGeometry().geoms:
                vertices = shapely.geometry.base.dump_coords(g)
                
        if geomType == 'Polygon':
            vertices = self._reducePolygon(hazardEvent,vertices, geomType, numvertices)
            vertices = shapely.geometry.base.dump_coords(vertices)
           
        for vertice in vertices:
            hazardLat = vertice[1]
            hazardLon = vertice[0]

            diffList = []
            for x in range(0, len(lats)):
                diffList.append(abs(hazardLat - lats[x]) + abs(hazardLon - lons[x]))

            index = diffList.index(min(diffList))
  
            boundingStatement += names[index] + '-'
            vorLat.append(lats[index])
            vorLon.append(lons[index])

        self._setVORPoints(vorLat, vorLon, hazardEvent)
        selectedVisualFeatures = []
           
        boundingStatement = boundingStatement[:-1]
        
        return boundingStatement        
    
    def _reducePolygon(self, hazardEvent, vertices, geomType, numPoints):
        try:
            initialPoly = GeometryFactory.createPolygon(vertices) 
        except ValueError:
            for g in hazardEvent.getFlattenedGeometry().geoms:
                vertices = shapely.geometry.base.dump_coords(g)
            initialPoly = GeometryFactory.createPolygon(vertices)
          
        #numPoints = 6
        tolerance = 0.001
        newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
        while len(newPoly.exterior.coords) > numPoints:
            tolerance += 0.001
            newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
            
        return newPoly  
    
    def _setVORPoints(self, vorLat, vorLon, hazardEvent):
        VOR_points = zip(vorLon, vorLat)
        hazardEvent.set('VOR_points', VOR_points)
        
        return
    
    #########################################
    ### OVERRIDES
    
    def volcanoFilePath(self):
        return '/home/nathan.hardin/Desktop/volcanoes.csv'           