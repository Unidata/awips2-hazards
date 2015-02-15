"""
Null Recommender

@since: February 2015
@author: GSD Hazard Services Team
"""
import RecommenderTemplate

from GeneralConstants import *
 
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        """
        Constructs a Null recommender that can be run to
        cause initialization of the recommender framework
        upon Hazards startup.
        """

    def defineScriptMetadata(self):
        """
        @return: JSON string containing information about this
                 tool
        """
        metaDict = {}
        metaDict["toolName"] = "NullRecommender"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "A Null recommender"
        return metaDict

    def defineDialog(self, eventSet):
        return {}
    
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        return []

    def toString(self):
        return "NullRecommender"
    
