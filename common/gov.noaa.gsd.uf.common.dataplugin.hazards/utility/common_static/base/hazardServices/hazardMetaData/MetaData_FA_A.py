import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == 'ending':
            metaData = [
                    self.getEndingSynopsis(), 
                    ]
        else:
            metaData = [
                    self.getImmediateCause(),
                    self.getCAP_Fields([
                                        ("urgency", "Future"),
                                        ("severity", "Severe"),
                                        ("certainty", "Possible"),
                                        ("responseType", "Prepare"),
                                        ])
                    ]
        return {
                METADATA_KEY: metaData
                }    
            
