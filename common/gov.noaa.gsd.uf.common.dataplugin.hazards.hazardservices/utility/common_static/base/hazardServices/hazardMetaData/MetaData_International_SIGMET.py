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
TABLEFILE = '/home/nathan.hardin/Desktop/volcanoes.csv'

class MetaData(MetaData_AIRMET_SIGMET.MetaData):
    
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.AAWUinitialize(hazardEvent, metaDict)
        sys.stderr.writelines(['Calling SIGMET.International', '\n'])
        
        self._geomType = AviationUtils.AviationUtils().getGeometryType(hazardEvent)
        hazardEvent.set('originalGeomType', self._geomType)
        
        volcanoDict = AviationUtils.AviationUtils().createVolcanoDict()
                         
        self.flush()
        
        advisoryType = 'SIGMET.International'
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getInternationalSigmetInputs(self._geomType, volcanoDict),
                   ]

        return  {
                METADATA_KEY: metaData,
                }         
    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    AMChanges = MetaData_AIRMET_SIGMET.applyInterdependencies(triggerIdentifiers, mutableProperties)
    
    import sys
    sys.stderr.writelines( ['Hello World [SIGMET] !\n'])
                    
    sys.stderr.writelines(['AMChanges: ', str(AMChanges), '\n'])
    return AMChanges                              