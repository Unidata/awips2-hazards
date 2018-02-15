import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.immediateCauseValues='ER'
        setDamnNameLabel = None
        if hazardEvent is not None:
            damOrLeveeName = hazardEvent.get('damOrLeveeName')
            immediateCause = hazardEvent.get("immediateCause")
            if hazardEvent.get('cause') == 'Dam Failure' and damOrLeveeName:
                setDamnNameLabel = True
                self.immediateCauseValues = 'DM'
            elif immediateCause == self.immediateCauseDM()['identifier']:
                setDamnNameLabel = False
                self.immediateCauseValues = 'DM'

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
        if setDamnNameLabel is not None:
            if setDamnNameLabel is True:
                # Ran recommender so already have the Dam/Levee name
                metaData.insert(1, self.setDamNameLabel(damOrLeveeName))
            else :
                # Add the combo box to select the name
                metaData.insert(1, self.getDamOrLevee(damOrLeveeName))
                
        return {
                METADATA_KEY: metaData
                }

    def getImmediateCause(self):
        return super(MetaData, self).getImmediateCause(values=self.immediateCauseValues)

    # CALLS TO ACTION
    def getCTA_Choices(self):
        return [
            self.ctaSafety(),
            self.ctaStayAway(),
            ]

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges
