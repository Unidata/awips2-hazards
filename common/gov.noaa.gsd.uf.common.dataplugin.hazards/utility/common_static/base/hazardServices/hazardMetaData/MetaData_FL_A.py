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
                     self.getPointID(),
                     self.getImmediateCause(),
                     self.getFloodSeverity(),
                     self.getFloodRecord(),
                     self.getDebrisFlowOptions(),
                     self.getCTAs(),                    
                    ] + self.setCAP_Fields()
        return {
                METADATA_KEY: metaData
                }    
                
        # CALLS TO ACTION
    def getCTAs(self):
        return {
                "fieldType":"CheckList",
                "label":"Calls to Action (1 or more):",
                "fieldName": "cta",
                "values": ["stayTunedCTA"],
                "choices": self.getCTA_Choices()
                }        
    def getCTA_Choices(self):
        return [
            self.ctaNoCTA(),
            self.ctaFloodWatchMeans(),
            self.ctaStayTuned(),
            self.ctaReportFlooding(),
            ]

    # CAP fields        
    def setCAP_Fields(self):
        # Set the defaults for the CAP Fields
        capFields = self.getCAP_Fields()
        for entry in capFields:
            for fieldName, values in [
                        ("urgency", "Future"),
                        ("severity", "Severe"),
                        ("certainty", "Possible"),
                        ("responseType", "Prepare"),
                        ]:
                if entry["fieldName"] == fieldName:
                    entry["values"] = values  
        return capFields          
        
