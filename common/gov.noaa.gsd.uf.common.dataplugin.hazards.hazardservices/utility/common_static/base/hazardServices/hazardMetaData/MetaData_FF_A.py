import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == "ending":
            metaData = [
                        self.getEndingSynopsis(), 
                ]
        else:
                metaData = [
                    self.getImmediateCause(),
                    self.getBasisStatement(),
                    self.getCTAs(), 
                    # Preserving CAP defaults for future reference.                   
#                     self.getCAP_Fields([
#                                           ("urgency", "Future"),
#                                           ("severity", "Severe"),
#                                           ("certainty", "Possible"),
#                                           ("responseType", "Monitor"),
#                                         ]) 
                    ]
        return {
                METADATA_KEY: metaData
                }    


    # IMMEDIATE CAUSE
    def getImmediateCause(self):
        damName = self.hazardEvent.get('damName')
        if damName:
            values = 'DM'
        else:
            values = 'ER'
        return {
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": values,
            "expandHorizontally": True,
            "choices": [
                self.immediateCauseER(),
                self.immediateCauseSM(),
                self.immediateCauseRS(),
                self.immediateCauseDM(),
                self.immediateCauseDR(),
                self.immediateCauseGO(),
                self.immediateCauseIJ(),
                self.immediateCauseIC(),
                self.immediateCauseFS(),
                self.immediateCauseFT(),
                self.immediateCauseET(),
                self.immediateCauseWT(),
                self.immediateCauseOT(),
                self.immediateCauseMC(),
                self.immediateCauseUU(),
                ]
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
             "visibleChars": 12,
             "values": "Enter basis text",
            } 
                   
