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
        sys.stderr.writelines(['Calling Strong Surface Wind', '\n'])
        
        self._geomType = AviationUtils.AviationUtils().getGeometryType(hazardEvent)
        hazardEvent.set('originalGeomType', self._geomType)
        
        trigger = 'generation'
        boundingStatement = AviationUtils.AviationUtils().boundingStatement(hazardEvent,self._geomType,TABLEFILE,[],trigger)        
                         
        self.flush()
        
        advisoryType = 'Strong_Surface_Wind'
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getSSWInputs(self._geomType),
                   ]

        return  {
                METADATA_KEY: metaData,
                }         
    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    AMChanges = MetaData_AIRMET_SIGMET.applyInterdependencies(triggerIdentifiers, mutableProperties)
    
    import sys
    sys.stderr.writelines( ['Hello World [Strong Surface Wind] !\n'])
                    
    sys.stderr.writelines(['AMChanges: ', str(AMChanges), '\n'])
    return AMChanges                              