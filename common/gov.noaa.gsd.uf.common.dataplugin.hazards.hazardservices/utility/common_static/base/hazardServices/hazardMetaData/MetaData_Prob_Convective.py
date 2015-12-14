'''
    Description: Hazard Information Dialog Metadata for hazard type Prob_Convection
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
#        if self.hazardStatus in ["elapsed", "ending", "ended"]:
#            metaData = [
#                    #self.getListOfCities(),
#                    self.getEndingSynopsis(), 
#                    ]
#        else:
#            metaData = [
#                    self.getConvectionCategory(),
#                    self.basisStatement(),
#                    ]

        metaData = [
                    self.convectiveControls(),
                    self.convectiveGetAttrs()
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
    propertyChanges = CommonMetaData.applyConvectiveInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges