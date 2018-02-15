'''
    Description: Hazard Information Dialog Metadata for hazard type HY.O
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        return {
                METADATA_KEY: []
                }

