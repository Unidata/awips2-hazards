'''
    Description: Hazard Information Dialog Metadata for Prob_Rainfall
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus in ["elapsed", "ending", "ended"]:
            metaData = [
                    #self.getListOfCities(),
                    self.getEndingSynopsis(), 
                    ]
        else:
            metaData = [
                    #self.getListOfCities(),
                    self.getRisk(),
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