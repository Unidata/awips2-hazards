import CommonMetaData
import TimeUtils
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
import AviationUtils

######
TABLEFILE = '/home/nathan.hardin/Desktop/snap.tbl'

class MetaData(MetaData_AIRMET_SIGMET.MetaData):
    
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.AAWUinitialize(hazardEvent, metaDict)
        sys.stderr.writelines(['Calling SIGMET.Convective', '\n'])
        
        self._setTimeRange(hazardEvent)
        geomType = AviationUtils.AviationUtils().getGeometryType(hazardEvent)
        hazardEvent.set('originalGeomType', geomType)
        
        trigger = 'generation'
        
        convectiveSigmetDomain = AviationUtils.AviationUtils().selectDomain(hazardEvent,[],geomType,trigger)
        
        boundingStatement = AviationUtils.AviationUtils().boundingStatement(hazardEvent,geomType,TABLEFILE,[],trigger)
        if geomType == 'Polygon':
            self._addVisualFeatures(hazardEvent)
                         
        self.flush()
        
        convectiveSigmetModifiers = ["None", "Developing", "Intensifying", "Diminishing"]
        advisoryType = 'SIGMET.Convective'
       
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getConvectiveSigmetInputs(geomType, convectiveSigmetDomain, convectiveSigmetModifiers),
                   ]

        return  {
                METADATA_KEY: metaData,
                }
    
    def _roundTime(self, dt=None, dateDelta=datetime.timedelta(minutes=1)):
        roundTo = dateDelta.total_seconds()
        if dt is None: dt = datetime.datetime.now()
        seconds = (dt - dt.min).seconds
        rounding = ((seconds+roundTo/2)// roundTo) * roundTo
        return dt + datetime.timedelta(0,rounding-seconds,-dt.microsecond)    
    
    def _addVisualFeatures(self, hazardEvent):
        startTime = TimeUtils.roundDatetime(hazardEvent.getStartTime())
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())
        
        VOR_points = hazardEvent.getHazardAttributes().get('VOR_points')
        eventID = hazardEvent.getEventID()
        
        selectedFeatures = []
        
        poly = GeometryFactory.createPolygon(VOR_points)
        
        basePoly = hazardEvent.getGeometry()
                
        fillColor = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 0.5 }
        borderColor = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 }
                    
        VORPoly = {
            "identifier": "VORPreview_" + eventID,
            "visibilityConstraints": "always",
            "borderColor": borderColor,
            "fillColor": fillColor,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "selected",
            "dragCapability": "all",
            "borderColor": "eventType",
            "fillColor": {"red": 1, "green": 1, "blue": 1, "alpha": 0},
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): basePoly
            }
        }                    

        selectedFeatures.append(basePoly)
        selectedFeatures.append(VORPoly)
         
        hazardEvent.setVisualFeatures(VisualFeatures(selectedFeatures))    
        
        return True    
    
    def _setTimeRange(self, hazardEvent):
        startTime = hazardEvent.getStartTime()
        startTimeHour = hazardEvent.getStartTime().hour
        startTimeMinute = hazardEvent.getStartTime().minute
        
        if startTimeMinute < 55:
            minuteDiff = 55 - startTimeMinute
            newStart = startTime + datetime.timedelta(minutes=minuteDiff)
        elif startTimeMinute > 55:
            minuteDiff = startTimeMinute - 55
            newStart = startTime + datetime.timedelta(minutes=(60-minuteDiff))
        elif startTimeMinute == 55:
            newStart = startTime
            
        newEnd = newStart + datetime.timedelta(hours=2)
        
        hazardEvent.setStartTime(newStart)
        hazardEvent.setEndTime(newEnd)
        
        return                            