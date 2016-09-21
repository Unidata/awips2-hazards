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
import AdvancedGeometry

######
TABLEFILE = '/home/nathan.hardin/Desktop/snap.tbl'

class MetaData(MetaData_AIRMET_SIGMET.MetaData):
    
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.AAWUinitialize(hazardEvent, metaDict)
        sys.stderr.writelines(['Calling SIGMET.Convective', '\n'])
        
        self._setTimeRange(hazardEvent)
        self._geomType = AviationUtils.AviationUtils().getGeometryType(hazardEvent)
        hazardEvent.set('originalGeomType', self._geomType)
        
        trigger = 'generation'
        
        convectiveSigmetDomain = AviationUtils.AviationUtils().selectDomain(hazardEvent,[],self._geomType,trigger)
        
        #startTime = time.time()
        boundingStatement = AviationUtils.AviationUtils().boundingStatement(hazardEvent,self._geomType,TABLEFILE,[],trigger)
        #elapsedTime = time.time() - startTime
        #print "elapsedTime: ", elapsedTime
        
        if self._geomType == 'Polygon':
            self._addVisualFeatures(hazardEvent)
                         
        self.flush()
        
        convectiveSigmetModifiers = ["None", "Developing", "Intensifying", "Diminishing"]
        advisoryType = 'SIGMET.Convective'
       
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getConvectiveSigmetInputs(self._geomType, convectiveSigmetDomain, convectiveSigmetModifiers),
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
        #startTime = TimeUtils.roundDatetime(hazardEvent.getStartTime())
        startTime = hazardEvent.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(hazardEvent.getEndTime())
        
        VOR_points = hazardEvent.getHazardAttributes().get('VOR_points')
        eventID = hazardEvent.getEventID()
        
        polygonArea = AviationUtils.AviationUtils().polygonArea(hazardEvent, self._geomType, None)
        domain = hazardEvent.getHazardAttributes().get('convectiveSigmetDomain')
        direction = hazardEvent.getHazardAttributes().get('convectiveSigmetDirection')
        speed = hazardEvent.getHazardAttributes().get('convectiveSigmetSpeed')
        cloudTop = hazardEvent.getHazardAttributes().get('convectiveSigmetCloudTop')
        cloudTopText = hazardEvent.getHazardAttributes().get('convectiveSigmetCloudTopText')        
        
        status = hazardEvent.getStatus()
        if status == 'ISSUED':
            area = str(polygonArea) + "sq mi"
            numberStr = hazardEvent.getHazardAttributes().get('convectiveSigmetNumberStr')
            number = "\n" + numberStr + domain[0]
        
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL" + str(cloudTopText)
            
            motion = "\n" + str(direction)+"@"+str(speed)+"kts"
            label = number + area + tops + motion
        else:
            area = str(polygonArea) + "sq mi"
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL" + str(cloudTopText)
            else:
                tops = "\nN/A"
            
            motion = "\n" + str(direction)+"@"+str(speed)+"kts"                        
            label = area + tops + motion
            
        selectedFeatures = []
        
        poly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(VOR_points), 0)
        
        basePoly = hazardEvent.getGeometry()
                
        fillColor = {"red": 130 / 255.0, "green": 0 / 255.0, "blue": 0 / 255.0, "alpha": 0.0 }
        borderColor = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 }
                    
        VORPoly = {
            "identifier": "VORPreview_" + eventID,
            "visibilityConstraints": "always",
            "borderColor": "eventType",
            "fillColor": fillColor,
            "label": label,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime)): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": "selected",
            "dragCapability": "all",
            "borderColor": borderColor, #"eventType",
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
        startTime = hazardEvent.getStartTime().replace(second=0,microsecond=0)
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
    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    AMChanges = MetaData_AIRMET_SIGMET.applyInterdependencies(triggerIdentifiers, mutableProperties)
    
    import sys
    sys.stderr.writelines( ['Hello World [SIGMET] !\n'])
                    
    sys.stderr.writelines(['AMChanges: ', str(AMChanges), '\n'])
    return AMChanges                              