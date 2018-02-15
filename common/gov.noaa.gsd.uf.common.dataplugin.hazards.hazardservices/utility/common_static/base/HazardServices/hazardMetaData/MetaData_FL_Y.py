import CommonMetaData
from HazardConstants import *
import datetime
import json

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        self.basedOnLookupPE = '{:15s}'.format('YES')
        
        if self.hazardStatus == "ending":
            metaData = [
                        self.getInclude(),
                        ]
            return {
                    METADATA_KEY: metaData
                    }
            
        else:
            pointDetails = [
                        self.getRiverLabel(),
                        self.getImmediateCause(),
                        self.getHiddenFloodSeverityNone(),
                        self.getFloodRecord(),
                        self.getInclude(),
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
                    METADATA_KEY: metaData,
                    METADATA_MODIFIED_HAZARD_EVENT: hazardEvent
                    }    
                        
    def includeChoices(self):
        return [
            self.includeFloodPointTable(),
            ]

# Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
  return CommonMetaData.applyFLInterdependencies(triggerIdentifiers, mutableProperties)

