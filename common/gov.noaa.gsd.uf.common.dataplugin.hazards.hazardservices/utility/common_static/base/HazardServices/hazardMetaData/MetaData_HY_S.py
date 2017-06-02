'''
    Description: Hazard Information Dialog Metadata for hazard type HY.S
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
                    self.getHiddenFloodSeverityNone(),
        ]
        return {
                METADATA_KEY: metaData
                }    

