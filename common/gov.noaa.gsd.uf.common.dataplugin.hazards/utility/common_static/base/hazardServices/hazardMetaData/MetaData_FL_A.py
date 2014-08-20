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
                     self.getDebrisFlowOptions(),
                     self.getCTAs("stayTunedCTA"),                    
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
                
    def getCTA_Choices(self):
        return [
            self.ctaNoCTA(),
            self.ctaFloodWatchMeans(),
            self.ctaStayTuned(),
            self.ctaReportFlooding(),
            ]

