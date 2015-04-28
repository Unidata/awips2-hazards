'''
    Description: Hazard Information Dialog Metadata for hazard type FA.A
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == 'ending':
            metaData = [
                    self.getEndingSynopsis(), 
                    self.getListOfCities(),
                    ]
        else:
            metaData = [
                    self.getImmediateCause(),
                    self.basisStatement(),
                    self.getListOfCities(),
                    self.getCTAs(), 
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
            
    # CALLS TO ACTION
    def getCTA_Choices(self):
        return [
            self.ctaSafety(),
            self.ctaStayAway(),
            ]

    def basisStatement(self):
        return {
             "fieldType": "Text",
             "fieldName": "basisStatement",
             "expandHorizontally": True,
             "visibleChars": 60,
             "lines": 6,
             "promptText": "Enter basis text",
            } 
