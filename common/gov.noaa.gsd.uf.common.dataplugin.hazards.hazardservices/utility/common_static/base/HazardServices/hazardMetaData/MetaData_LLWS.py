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
from VisualFeatures import VisualFeatures

class MetaData(MetaData_AIRMET_SIGMET.MetaData):
    
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.AAWUinitialize(hazardEvent, metaDict)
        CommonMetaData.writelines(sys.stderr, ['Calling LLWS', '\n'])
        
        hazardEvent.setVisualFeatures(VisualFeatures([]))
        
        self._geomType = AviationUtils.AviationUtils().getGeometryType(hazardEvent)
        hazardEvent.set('originalGeomType', self._geomType)
        
        trigger = 'generation'
        boundingStatement = AviationUtils.AviationUtils().boundingStatement(hazardEvent,self._geomType,[],trigger)
                         
        self.flush()
        
        advisoryType = 'LLWS'
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getLLWSInputs(self._geomType),
                   ]

        return  {
                METADATA_KEY: metaData,
                }         
    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):    
    AMChanges = MetaData_AIRMET_SIGMET.applyInterdependencies(triggerIdentifiers, mutableProperties)

    return AMChanges                              