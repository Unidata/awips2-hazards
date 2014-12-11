'''
    Description: Product staging dialog metadata.
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, eventDicts=None, metaDict=None):
        self._eventDicts = eventDicts
        if metaDict:
            self._productID = metaDict.get('productID')
        else:
            self._productID = None
        metaData = [
                    {
                     "fieldType": "Composite",
                     "fieldName": "overviewSynopsisContainer",
                     "expandHorizontally": True,
                     "numColumns": 2,
                     "spacing": 5,
                     "fields": [
                                {
                                 "fieldType": "Label",
                                 "fieldName": "overviewSynopsisExplanation",
                                 "label": "Overview Synopsis:"
                                 },
                                {
                                 "fieldType": "MenuButton",
                                 "fieldName": "overviewSynopsisCanned",
                                 "label": "Choose Canned Text",
                                 "choices": self.getSynopsisChoices()
                                 }
                                ]
                     },
                    {
                     "fieldType": "Text",
                     "fieldName": "overviewSynopsisText",
                     "visibleChars": 60,
                     "lines": 6,
                     "expandHorizontally": True
                     }
                    ] + [self.getCTAs()]
        return {
                METADATA_KEY: metaData
                }


        
    def synopsisTempSnowMelt(self):
        return {"identifier":"tempSnowMelt", 
                "displayString":"Warm Temperatures and Snow Melt",
                "productString":"Warm temperatures will melt high mountain snowpack and " +
                "increase river flows."
        }
    def synopsisDayNightTemps(self):
        return {"identifier":"dayNightTemps", 
                "displayString":"Warm day and night Temperatures",
                "productString":"Warm daytime temperatures along with low temperatures " +
                "remaining above freezing overnight will accelerate snow melt. River " +
                "flows will increase quickly and remain high for the next week."
        }
    def synopsisSnowMeltReservoir(self):
        return {"identifier":"snowMeltReservoir", 
                "displayString":"Snow melt and Reservoir releases",
                "productString":"High mountain snow melt and increased reservoir " +
                "releases will cause the river flows to become high. Expect minor " +
                "flooding downstream from the dam."
        }
    def synopsisRainOnSnow(self):
        return {"identifier":"rainOnSnow", 
                "displayString":"Rain On Snow",
                "productString":"Heavy rain will fall on a deep primed snowpack leading " +
                "to the melt increasing. Flows in Rivers will increase quickly and " +
                "reach critical levels."
        }
    def synopsisIceJam(self):
        return {"identifier":"iceJamFlooding", 
                "displayString":"Ice Jam Flooding",
                "productString":"An ice jam will cause water to infiltrate the lowlands " +
                "along the river."
        }
    def synopsisCategoryIncrease(self):
        return {"identifier":"categoryIncrease", 
                "displayString":"Increase in Category",
                "productString":"Heavy rainfall will increase the severity of flooding " +
                "on the #riverName#."
        }
        
    def getSynopsisChoices(self):
        return [ 
                self.synopsisTempSnowMelt(),
                self.synopsisDayNightTemps(),
                self.synopsisSnowMeltReservoir(),
                self.synopsisRainOnSnow(),
                self.synopsisIceJam(),
                self.synopsisCategoryIncrease()
                ]
        
    # Create a dictionary mapping synopsis identifiers to their product strings.
    @staticmethod
    def createSynopsisProductStringsFromIdentifiersMapping():
        map = {}
        metaData = MetaData()
        for choice in metaData.getSynopsisChoices():
            map["overviewSynopsisCanned." + choice["identifier"]] = choice["productString"]
        return map

    
    def getCTAs(self):
        if self._productID == 'FFA':
            values = ['safetyCTA']
        else:
            values = ["stayTunedCTA"]
        values = ['doNotDriveCTA']
        return {
                "fieldType":"CheckList",
                "label":"Calls to Action (1 or more):",
                "fieldName": "cta",
                "values": values,
                "choices": self.getCTA_Choices()
                }        
    def getCTA_Choices(self):
        if self._productID == 'FFA':
            return [
                    self.ctaSafety(),
                    self.ctaStayAway(),
                    ]
        else:
            return [
                self.ctaFloodWarningMeans(),
                self.ctaDoNotDrive(),
                self.ctaRiverBanks(),
                self.ctaTurnAround(),
                self.ctaStayTuned(),
                self.ctaNightTime(),
                self.ctaAutoSafety(),
                self.ctaRisingWater(),
                self.ctaForceOfWater(),
                self.ctaLastStatement(),
                self.ctaWarningInEffect(),
                self.ctaReportFlooding(),
                ]

    def getCTA_Choices(self):
        return [
            self.ctaFloodAdvisoryMeans(),
            self.ctaDoNotDrive(),
            self.ctaRiverBanks(),
            self.ctaTurnAround(),
            self.ctaStayTuned(),
            self.ctaNightTime(),
            self.ctaAutoSafety(),
            self.ctaRisingWater(),
            self.ctaForceOfWater(),
            self.ctaLastStatement(),
            self.ctaWarningInEffect(),
            self.ctaReportFlooding(),
            ]

        
# Initialize the class-scoped dictionary mapping synopsis identifiers to product strings. 
MetaData.synopsisProductStringsFromIdentifiers = MetaData.createSynopsisProductStringsFromIdentifiersMapping()


# Ensure that when the megawidgets are initialized and then subsequently whenever the
# user chooses a new choice from the canned synopses dropdown, the text in the synopsis
# text area is changed to match.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    # See if the trigger identifiers list contains any of the identifiers from the
    # synopsis canned choices; if so, change the text's value to match the associated
    # canned choice text.
    if triggerIdentifiers is not None and not set(triggerIdentifiers).isdisjoint(MetaData.synopsisProductStringsFromIdentifiers.keys()):
        return {
        "overviewSynopsisText": {
        "values": MetaData.synopsisProductStringsFromIdentifiers[set(triggerIdentifiers).intersection(set(MetaData.synopsisProductStringsFromIdentifiers.keys())).pop()]
        }
    }
    return None
