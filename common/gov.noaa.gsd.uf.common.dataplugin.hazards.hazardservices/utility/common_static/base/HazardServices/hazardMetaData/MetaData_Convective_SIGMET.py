import CommonMetaData
import TimeUtils
import MetaData_AIRMET_SIGMET
from HazardConstants import *
import time, datetime
from EventSet import EventSet
import GeometryFactory
from VisualFeatures import VisualFeatures
import AviationUtils
import AdvancedGeometry

class MetaData(MetaData_AIRMET_SIGMET.MetaData):
    
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.AAWUinitialize(hazardEvent, metaDict)
        hazardEvent.setVisualFeatures(VisualFeatures([]))
        
        self._setTimeRange(hazardEvent)
        self._geomType = AviationUtils.AviationUtils().getGeometryType(hazardEvent)
        trigger = 'generation'
        
        convectiveSigmetDomain = AviationUtils.AviationUtils().selectDomain(hazardEvent,[],self._geomType,trigger)
        
        boundingStatement = AviationUtils.AviationUtils().boundingStatement(hazardEvent,self._geomType,[],trigger)
                         
        self.flush()
        
        convectiveSigmetModifiers = ["None", "Developing", "Intensifying", "Diminishing"]
        advisoryType = 'SIGMET.Convective'
       
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getConvectiveSigmetInputs(self._geomType, convectiveSigmetDomain, convectiveSigmetModifiers),
                   ]

        return  {
                METADATA_KEY: metaData,
                METADATA_MODIFIED_HAZARD_EVENT: hazardEvent
                }    
    
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
    
    return AMChanges                              