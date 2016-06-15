import CommonMetaData
import MetaData_AIRMET_SIGMET
from HazardConstants import *
import datetime
import json
from com.raytheon.uf.common.time import SimulatedTime
import sys
import shapely
import time, datetime
from EventSet import EventSet
import GeometryFactory
from VisualFeatures import VisualFeatures

class MetaData(MetaData_AIRMET_SIGMET.MetaData):
    
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.AAWUinitialize(hazardEvent, metaDict)
        sys.stderr.writelines(['Calling SIGMET.Convective', '\n'])
        
        self._setTimeRange(hazardEvent)
        
        convectiveSigmetDomain = self._selectDomain(hazardEvent)

        boundingStatement = self._setBoundingStatement(hazardEvent)
        hazardEvent.set('boundingStatement', boundingStatement)
                         
        self.flush()
        
        convectiveSigmetModifiers = ["None", "Developing", "Intensifying", "Diminishing"]
        advisoryType = 'SIGMET.Convective'
       
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getConvectiveSigmetInputs(convectiveSigmetDomain, convectiveSigmetModifiers),
                   ]

        return  {
                METADATA_KEY: metaData,
                }
        
    def _setBoundingStatement(self, hazardEvent):
        boundingStatement = 'FROM '
        
        per_row = []
        with open('/home/nathan.hardin/laptop/Desktop/Convective_SIGMET/snap.tbl', 'r') as fr:
            for line in fr:
                per_row.append(line.split())

        lats = []
        lons = []
        names = []
        stid = []

        siteExceptionList = ['ANN', 'LVD', 'BKA', 'SSR', 'JNU', 'YAK', 'MDO',
                'JOH', 'ODK', 'HOM', 'ENA', 'ANC', 'BGQ', 'ORT', 'GKN', 'TKA']

        for x in range(16, len(per_row)):
            stid.append(per_row[x][0])

        for x in range(16, len(per_row)):
            if stid[x - 16][:3] in siteExceptionList:
                pass
            else: 
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
        
        for g in hazardEvent.getGeometry().geoms:
            vertices = shapely.geometry.base.dump_coords(g)
        
        vertices = self._reducePolygon(vertices)
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
        self._addVisualFeatures(hazardEvent)
            
        boundingStatement = boundingStatement[:-1]
        
        return boundingStatement
    
    def _reducePolygon(self, vertices):
        
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
    
    def _roundTime(self, dt=None, dateDelta=datetime.timedelta(minutes=1)):
        roundTo = dateDelta.total_seconds()
        if dt is None: dt = datetime.datetime.now()
        seconds = (dt - dt.min).seconds
        rounding = ((seconds+roundTo/2)// roundTo) * roundTo
        return dt + datetime.timedelta(0,rounding-seconds,-dt.microsecond)    
    
    def _addVisualFeatures(self, hazardEvent):
        
        startTime = self._roundTime(hazardEvent.getStartTime())
        endTime = self._roundTime(hazardEvent.getEndTime())
        
        VOR_points = hazardEvent.getHazardAttributes().get('VOR_points')
        eventID = hazardEvent.getEventID()
        
        selectedFeatures = []
        
        poly = GeometryFactory.createPolygon(VOR_points)
        basePoly = hazardEvent.getGeometry()
                
        fillColor = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 0.5 }
        borderColor = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 }
                    
        VORPoly = {
            "identifier": "VORPreview_" + eventID,
            "visibilityConstraints": "selected",
            "borderColor": borderColor,
            "fillColor": fillColor,
            "geometry": {
                (VisualFeatures.datetimeToEpochTimeMillis(startTime), VisualFeatures.datetimeToEpochTimeMillis(endTime)): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "always",
            "borderColor": "eventType",
            "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
            #"fillColor": "eventType",
            "geometry": {
                (VisualFeatures.datetimeToEpochTimeMillis(startTime), VisualFeatures.datetimeToEpochTimeMillis(endTime)): basePoly
            }
        }                    

        selectedFeatures.append(basePoly)
        selectedFeatures.append(VORPoly)
         
        hazardEvent.setVisualFeatures(VisualFeatures(selectedFeatures))    
        #hazardEvent.setSelectedVisualFeatures(VisualFeatures(selectedFeatures))
        #hazardEvent.setBaseVisualFeatures(VisualFeatures([basePoly]))
        
        return True    
        
    def _selectDomain(self, hazardEvent):
        hazGeometry = hazardEvent.getGeometry()
        for g in hazGeometry.geoms:
            vertices = shapely.geometry.base.dump_coords(g)
        
        hazardLonsList = []
        westSoftList = []
        centralSoftList = []
        eastSoftList = []    
        for vertice in vertices:
            hazardLonsList.append(vertice[0])
        
        hazardLonsList.pop()
        
        westSum = 0
        centralSum = 0
        eastSum = 0
        
        for lon in hazardLonsList:
            if lon <= -107 and lon > -109:
                westSoftList.append(lon)
                eastSum = eastSum + abs(lon + 107)
            elif (lon > -107 and lon < -103):
                centralSoftList.append(lon)
                centralSum = centralSum + abs(lon + 107)
            elif (lon <= -87 and lon > -92):
                centralSoftList.append(lon)
                centralSum = centralSum + abs(lon + 87)
            elif lon > -87 and lon < -83:
                eastSoftList.append(lon)
                eastSum = eastSum + abs(lon + 87)
            
        #All points in the same domain    
        if all(lon <= -109 for lon in hazardLonsList) == True:
            convectiveSigmetAreas = ["West"]
        elif all(lon >= -83 for lon in hazardLonsList) == True:
            convectiveSigmetAreas = ["East"]
        elif all(lon >= -103 and lon <= -92 for lon in hazardLonsList) == True:
            convectiveSigmetAreas = ["Central"]
            
        #For points overlapping soft boundary
        elif (any(lon >= -83 for lon in hazardLonsList) == True) and (len(eastSoftList) or len(centralSoftList)):
            convectiveSigmetAreas = ["East"]
        elif (any(lon >= -103 and lon <= -92 for lon in hazardLonsList) == True) and ((len(centralSoftList) or len(eastSoftList)) or (len(centralSoftList) or len(westSoftList))):
            convectiveSigmetAreas = ["Central"]
        elif (any(lon <= -109 for lon in hazardLonsList) == True) and (len(westSoftList) or len(centralSoftList)):
            convectiveSigmetAreas = ["West"]
            
        #All points in soft boundaries
        elif len(westSoftList) and len(centralSoftList):
            if len(westSoftList) > len(centralSoftList):
                convectiveSigmetAreas = ["West"]
            elif len(westSoftList) < len(centralSoftList):
                convectiveSigmetAreas = ["Central"]
            elif len(westSoftList) == len(centralSoftList):
                if westSum > centralSum:
                    convectiveSigmetAreas = ["West"]
                else:
                    convectiveSigmetAreas = ["Central"]
        elif len(centralSoftList) and len(eastSoftList):
            if len(centralSoftList) > len(eastSoftList):
                convectiveSigmetAreas = ["Central"]
            elif len(centralSoftList) < len(eastSoftList):
                convectiveSigmetAreas = ["East"]
            elif len(centralSoftList) == len(eastSoftList):
                if centralSum > eastSum:
                    convectiveSigmetAreas = ["Central"]
                else:
                    convectiveSigmetAreas = ["East"]
                    
        #For overlapping domains
        elif (any(lon <= -109 for lon in hazardLonsList) == True) and (any(lon >= -103 for lon in hazardLonsList) == True):
            raise ValueError('You cannot overlap the boundaries between domains')
        elif (any(lon <= -92 for lon in hazardLonsList) == True) and (any(lon >= -83 for lon in hazardLonsList) == True):        
            raise ValueError('You cannot overlap the boundaries between domains')                                
        
        return convectiveSigmetAreas
    
    def _setTimeRange(self, hazardEvent):
        self._startTime = hazardEvent.getStartTime()
        self._startTimeHour = hazardEvent.getStartTime().hour
        self._startTimeMinute = hazardEvent.getStartTime().minute
        
        if self._startTimeMinute < 55:
            minuteDiff = 55 - self._startTimeMinute
            self._newStart = self._startTime + datetime.timedelta(minutes=minuteDiff)
        elif self._startTimeMinute > 55:
            minuteDiff = self._startTimeMinute - 55
            self._newStart = self._startTime + datetime.timedelta(minutes=(55+minuteDiff))
        elif self._startTimeMinute == 55:
            self._newStart = self._startTime
            
        self._newEnd = self._newStart + datetime.timedelta(hours=2)
        
        hazardEvent.setStartTime(self._newStart)
        hazardEvent.setEndTime(self._newEnd)
        
        return                            