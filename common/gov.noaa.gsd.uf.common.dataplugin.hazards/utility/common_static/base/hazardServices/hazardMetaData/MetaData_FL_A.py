'''
    Description: Hazard Information Dialog Metadata for hazard type FL.A
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == "ending":
            metaData = [
                            self.getEndingSynopsis(),
            ]
        else:
            metaData = [
                     self.getPointID(),
                     self.getImmediateCause(),
                     self.getFloodSeverity(),
                     self.getFloodRecord(),
                     self.getRiseCrestFall(),
                     self.getRiseCrestFallButton(),
                     self.getCTAs("stayTunedCTA"),    
                     # Preserving CAP defaults for future reference.                
#                      self.getCAP_Fields([
#                                          ("urgency", "Future"),
#                                          ("severity", "Severe"),
#                                          ("certainty", "Possible"),
#                                          ("responseType", "Prepare"),
#                                         ])
                    ]
        return {
                METADATA_KEY: metaData
                }    
                
    def getCTA_Choices(self):
        return [
            self.ctaFloodWatchMeans(),
            self.ctaStayTuned(),
            self.ctaReportFlooding(),
            ]
