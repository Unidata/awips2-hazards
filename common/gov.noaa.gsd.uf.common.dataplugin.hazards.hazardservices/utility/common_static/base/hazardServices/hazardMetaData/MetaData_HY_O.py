'''
    Description: Hazard Information Dialog Metadata for hazard type HY.O
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
            self.getNarrativeForecastInformation(), 
            # Preserving CAP defaults for future reference.                   
#             self.getCAP_Fields([
#                                   ("urgency", "Future"),
#                                   ("severity", "Severe"),
#                                   ("certainty", "Possible"),
#                                   ("responseType", "Monitor"),
#                                 ]) 
#                     ]
        ]
        return {
                METADATA_KEY: metaData
                }    


    # IMMEDIATE CAUSE
    def getNarrativeForecastInformation(self):
        return {
             "fieldType": "Text",
             "fieldName": "narrativeForecastInformation",
             "expandHorizontally": True,
             "visibleChars": 25,
             "lines": 10,
             "values": '''|*
 Headline defining the type of flooding being addressed 
      (e.g., flash flooding, main stem
      river flooding, snow melt flooding)

 Area covered
 
 Possible timing of the event
 
 Relevant factors 
         (e.g., synoptic conditions, 
         quantitative precipitation forecasts (QPF), or
         soil conditions)
         
 Definition of an outlook (tailored to the specific situation)
 
 A closing statement indicating when additional information will be provided.
 *|
                ''',
            }

