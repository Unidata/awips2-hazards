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
# TODO -- Waiting for Issue 4042 -- Add this information in when we have the Product Staging MegaWidget work completed. 
#              {
#               "fieldType": "ComboBox",
#               "fieldName": "overviewSynopsis",
#               "label": "Overview Synopsis:",
#               "expandHorizontally": True,
#               "choices": [ 
#                          self.synopsisTempSnowMelt(),
#                          self.synopsisDayNightTemps(),
#                          self.synopsisSnowMeltReservoir(),
#                          self.synopsisRainOnSnow(),
#                          self.synopsisIceJam(),
#                          self.synopsisCategoryIncrease(),
#                          "Enter synopsis below" 
#                           ]
#              },
#             {
#              "fieldType": "Text",
#              "fieldName": "overviewSynopsisText",
#              "label": "Overview Synopsis:",
#              "visibleChars": 120,
#              "lines": 6,
#              "expandHorizontally": True
#             }
            ] #+ [self.getCTAs()]
        return {
                METADATA_KEY: metaData,
                #INTERDEPENDENCIES: applyInterdependencies,
                }


        
    def synopsisTempSnowMelt(self):
        return {"identifier":"tempSnowMelt", 
                "displayString":"Warm Temperatures and Snow Melt",
                "productString":'''Warm temperatures will melt high mountain snowpack and increase river flows. #overviewSynopsisText#>'''
        }
    def synopsisDayNightTemps(self):
        return {"identifier":"dayNightTemps", 
                "displayString":"Warm day and night Temperatures",
                "productString":'''Warm daytime temperatures along with low temperatures remaining above freezing 
                  overnight will accelerate snow melt. River flows will increase quickly and remain high for the next week. 
                  #overviewSynopsisText#>'''                    
        }
    def synopsisSnowMeltReservoir(self):
        return {"identifier":"snowMeltReservoir", 
                "displayString":"Snow melt and Reservoir releases",
                "productString":'''High mountain snow melt and increased reservoir releases will cause the river flows to become high. 
                Expect minor flooding downstream from the dam.
                #overviewSynopsisText#>'''
        }
    def synopsisRainOnSnow(self):
        return {"identifier":"rainOnSnow", 
                "displayString":"Rain On Snow",
                "productString":'''Heavy rain will fall on a deep primed snowpack leading to the melt increasing. 
                Flows in Rivers will increase quickly and reach critical levels. #overviewSynopsisText#>'''
        }
    def synopsisIceJam(self):
        return {"identifier":"iceJamFlooding", 
                "displayString":"Ice Jam Flooding",
                "productString":'''An ice jam will cause water to infiltrate the lowlands along the river.  
                #overviewSynopsisText#>'''
        }
    def synopsisCategoryIncrease(self):
        return {"identifier":"categoryIncrease", 
                "displayString":"Increase in Category",
                "productString":'''Heavy rainfall will increase the severity of flooding on the #riverName#.  
                #overviewSynopsisText#>'''
        }
    
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
                self.ctaNoCTA(),
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
            self.ctaNoCTA(),
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
 