'''
    Description: Hazard Information Dialog Metadata for probabilistic hazard types
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
            self.getSampleButton(),
            self.getSampleCheckBox()
        ]
        return {
                METADATA_KEY: metaData
                }    


    def getSampleButton(self):
        return {
                "fieldType": "Button",
                "fieldName": "sampleButton",
                "spacing": 5,
                "label": "Trigger Swath Recommender",
                "modifyRecommender": "SwathRecommender"
                }
    
    def getSampleCheckBox(self):
        return {
                "fieldType": "CheckBox",
                "fieldName": "sampleCheckBox",
                "spacing": 10,
                "label": "Toggling this also triggers swath recommender",
                "modifyRecommender": "SwathRecommender"
                }

