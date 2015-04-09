import CommonMetaData
from HazardConstants import *
import datetime
import json
from RiverForecastPoints import RiverForecastPoints
from com.raytheon.uf.common.time import SimulatedTime

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        millis = SimulatedTime.getSystemTime().getMillis()
        currentTime = datetime.datetime.fromtimestamp(millis / 1000)
        self._rfp = RiverForecastPoints(currentTime)
        self._basedOnLookupPE = '{:15s}'.format('YES')
        
        pointDetails = [self.getPointID(),
                        self.getImmediateCause(),
                        self.getFloodSeverity(),
                        self.getFloodRecord(),
                        self.getRiseCrestFall(),
                        self.getRiseCrestFallButton(),
                        self.getHiddenFallLastInterval(),
                        self.getRainAmt(),
                        ]
     
        crests = [self.getCrestsOrImpacts("crests")]
         
        impacts = [self.getCrestsOrImpacts("impacts")]
        
        metaData = [
                       {
                "fieldType": "TabbedComposite",
                "fieldName": "FLWTabbedComposite",
                "leftMargin": 10,
                "rightMargin": 10,
                "topMargin": 10,
                "bottomMargin": 10,
                "expandHorizontally": True,
                "expandVertically": True,
                "pages": [
                              {
                                "pageName": "Point Details",
                                "pageFields": pointDetails
                               },
                              {
                                "pageName": "CTA",
                                "pageFields": [
                                               self.getCTAs(['doNotDriveCTA']),
                                               # Preserving CAP defaults for future reference.
                                               #self.getCAP_Fields([
                                               #("urgency", "Expected"),
                                               #("severity", "Minor"),
                                               #("certainty", "Observed"),
                                               #("responseType", "Avoid"),
                                               #                    ]) 
                                               ]
                               },
                              {
                                "pageName": "Crest Comparison",
                                "pageFields": crests
                               },
                              {
                                "pageName": "Impacts Statement",
                                "pageFields": impacts
                               }
                        ]
                 
                }
           ]
        
        
        return {
                METADATA_KEY: metaData
                }    
                        
        
    def getCTA_Choices(self):
        return [
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
  return CommonMetaData.applyFLInterdependencies(triggerIdentifiers, mutableProperties)

