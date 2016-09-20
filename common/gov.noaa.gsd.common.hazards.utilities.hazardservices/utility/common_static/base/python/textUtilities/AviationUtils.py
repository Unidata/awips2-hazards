import shapely
import EventFactory, EventSetFactory, GeometryFactory
'''
Utility for Aviation Products
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
import TimeUtils
from VisualFeatures import VisualFeatures
import Domains

class AviationUtils:
    def getGeometryType(self, hazardEvent):        
        for g in hazardEvent.getFlattenedGeometry():
            geomType = g.geom_type           
        
        return geomType    
    
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
        if geomType is not 'Point':
            boundingStatement = 'FROM '
        else:
            boundingStatement = ''
        
        per_row = []
        with open(TABLEFILE, 'r') as fr:
            for line in fr:
                per_row.append(line.split())

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
        
        headerLines = 4

        for x in range(headerLines, len(per_row)):
            stid.append(per_row[x][0])
            lats.append(per_row[x][5])
            lons.append(per_row[x][6])
            names.append(per_row[x][2])

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
        else:
            for g in hazardEvent.getFlattenedGeometry().geoms:
                vertices = shapely.geometry.base.dump_coords(g)
                
        if geomType == 'Polygon':
            vertices = self._reducePolygon(vertices, geomType)
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
        hazardEvent.set('boundingStatement', boundingStatement)
        
        return boundingStatement
    
    def _reducePolygon(self, vertices, geomType):
        initialPoly = GeometryFactory.createPolygon(vertices)
          
        numPoints = 6
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