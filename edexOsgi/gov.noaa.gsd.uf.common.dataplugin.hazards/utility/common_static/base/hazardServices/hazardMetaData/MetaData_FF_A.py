import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
                    self.getImmediateCause(),
                    self.getCTAs(),                    
                    ] + self.setCAP_Fields()
        return {
                METADATA_KEY: metaData
                }    

    # CALLS TO ACTION
    def getCTA_Choices(self):
        return [
            self.ctaFlashFloodWatchMeans(),
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
                ("responseType", "Monitor"),
                ]:
                if entry["fieldName"] == fieldName:
                    entry["values"] = values  
        return capFields          

