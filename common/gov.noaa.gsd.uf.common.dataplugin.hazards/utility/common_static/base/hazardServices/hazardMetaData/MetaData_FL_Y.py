import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
                    self.getPointID(),
                    self.getImmediateCause(),
                    self.getFloodSeverity(),
                    self.getFloodRecord(),
                    self.getRiseCrestFall(),
                    self.getRainAmt(),
                    self.getCTAs("doNotDriveCTA"),                    
                    self.getCAP_Fields([
                                        ("urgency", "Expected"),
                                        ("severity", "Minor"),
                                        ("certainty", "Observed"),
                                        ("responseType", "Avoid"),
                                       ])
                    ]
        return {
                METADATA_KEY: metaData
                }    
                        
        
    def getCTA_Choices(self):
        return [
            self.ctaNoCTA(),
            self.ctaFloodAdvisoryMeans(),
            self.ctaDoNotDrive(),
            self.ctaRiverBanks(),
            self.ctaTurnAround(),
            self.ctaStayTuned(),
            self.ctaNightTime(),
            self.ctaAutoSafety(),
            self.ctaRisingWater(),
            self.ctaForceOfWater(),
            self.ctaLastStatement(),
            self.ctaWarningInEffect(),
            self.ctaReportFlooding(),
            ]
        
# Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
  return CommonMetaData.applyRiseCrestFallUntilFurtherNoticeInterdependencies(triggerIdentifiers, mutableProperties)

