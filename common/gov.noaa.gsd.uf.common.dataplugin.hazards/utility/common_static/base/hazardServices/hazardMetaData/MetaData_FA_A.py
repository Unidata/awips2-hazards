'''
    Description: Hazard Information Dialog Metadata for hazard type FA.A
'''
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
                    # Preserving CAP defaults for future reference.
#                     self.getCAP_Fields([
#                                         ("urgency", "Future"),
#                                         ("severity", "Severe"),
#                                         ("certainty", "Possible"),
#                                         ("responseType", "Prepare"),
#                                         ])
                    ]
        return {
                METADATA_KEY: metaData
                }    
            
