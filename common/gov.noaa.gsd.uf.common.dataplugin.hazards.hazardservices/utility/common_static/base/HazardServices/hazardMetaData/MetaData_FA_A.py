'''
    Description: Hazard Information Dialog Metadata for hazard type FA.A
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == "ending":
            metaData = []
        else:
            metaData = [
                    self.getForceSegment(),
                    self.getImmediateCause(),
                    self.getHiddenFloodSeverity(),
                    self.getCTAs(),
                    ]
            if hazardEvent is not None and self.hazardStatus != "ending":
                damOrLeveeName = hazardEvent.get('damOrLeveeName')
                immediateCause = hazardEvent.get("immediateCause")
                if immediateCause == self.immediateCauseDM()['identifier']:
                    metaData.insert(1, self.getDamOrLevee(damOrLeveeName))

        return {
                METADATA_KEY: metaData
                }    
            
    # CALLS TO ACTION
    def getCTA_Choices(self):
        return [
            self.ctaSafety(),
            self.ctaStayAway(),
            ]

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges