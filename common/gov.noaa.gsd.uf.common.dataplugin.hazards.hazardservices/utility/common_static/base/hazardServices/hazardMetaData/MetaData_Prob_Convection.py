'''
    Description: Hazard Information Dialog Metadata for hazard type Prob_Convection
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus in ["elapsed", "ending", "ended"]:
            metaData = [
                    self.getEndingSynopsis(), 
                    ]
        else:
            if self.hazardEvent.getSignificance() is None:
                metaData = [self.getConvectionCategory(), self.probability()]
            else:
                metaData = []
            metaData += [
                    self.basisStatement(),
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
            

    def basisStatement(self):
        return {
             "fieldType": "Text",
             "fieldName": "basisStatement",
             "expandHorizontally": True,
             "visibleChars": 60,
             "lines": 6,
             "promptText": "Enter basis text",
            } 

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges