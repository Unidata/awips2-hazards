'''
    Description: Hazard Information Dialog Metadata for hazard type FL.A
'''
import CommonMetaData
from HazardConstants import *
import datetime
import json

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        self._basedOnLookupPE = '{:15s}'.format('YES')

        if self.hazardStatus == "ending":
            metaData = [
                            self.getEndingSynopsis(),
            ]
        else:
            
            pointDetails = [self.getPointID(),
                            self.getImmediateCause(),
                            self.getFloodSeverity(),
                            self.getFloodRecord()
                            ]
            pointDetails.extend(self.getRiseCrestFall())
         
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
                                                   self.getCTAs(["stayTunedCTA"]),
                                                   # Preserving CAP defaults for future reference.
                                                   #self.getCAP_Fields([
                                                   #                    ("urgency", "Future"),
                                                   #                    ("severity", "Severe"),
                                                   #                    ("certainty", "Possible"),
                                                   #                    ("responseType", "Prepare"),
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
            self.ctaFloodWatchMeans(),
            self.ctaStayTuned(),
            self.ctaReportFlooding(),
            ]

# Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
  return CommonMetaData.applyFLInterdependencies(triggerIdentifiers, mutableProperties)

