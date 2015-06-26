import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus in ["ending", "ended"]:
            metaData = [
                        self.getEndingSynopsis(),
                        self.getListOfCities(),
                ]
        else:
            metaData = [
                self.getImmediateCause(),
                self.getBasisStatement(),
                self.getListOfCities(),
                self.getCTAs(), 
                    # Preserving CAP defaults for future reference.                   
#                     self.getCAP_Fields([
#                                           ("urgency", "Future"),
#                                           ("severity", "Severe"),
#                                           ("certainty", "Possible"),
#                                           ("responseType", "Monitor"),
#                                         ]) 
                    ]
        if hazardEvent is not None:
            damOrLeveeName = hazardEvent.get('damOrLeveeName')
            immediateCause = hazardEvent.get("immediateCause")
            if hazardEvent.get('cause') == 'Dam Failure' and damOrLeveeName:
                # Ran recommender so already have the Dam/Levee name
                metaData.insert(1,self.setDamNameLabel(damOrLeveeName))
            elif immediateCause == self.immediateCauseDM()['identifier']:
                # Add the combo box to select the name
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

    def getBasisStatement(self):
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
