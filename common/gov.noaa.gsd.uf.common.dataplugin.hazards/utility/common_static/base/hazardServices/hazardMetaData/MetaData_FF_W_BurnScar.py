'''
    Description: Hazard Information Dialog Metadata for hazard type FF.W.BurnScar
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == "ending":
            metaData = [
                        self.getEndingOption(),
                        self.getEndingSynopsis()
                        ]
        else:
           metaData = [
                     self.getInclude(),
                     self.getImmediateCause(),
                     self.getEventSpecificSource(),
                     self.getDebrisFlowOptions(),                                       
                     self.getRainAmt(),
                     self.getAdditionalInfo(),
                     self.getCTAs(), 
                     # Preserving CAP defaults for future reference.                  
#                      self.getCAP_Fields([
#                                           ("urgency", "Immediate"),
#                                           ("severity", "Severe"),
#                                           ("certainty", "Likely"),
#                                           ("responseType", "Avoid"),
#                                          ])
                    ]
        return {
                METADATA_KEY: metaData
                }    
       
    def immediateCauseChoices(self):
        return [
                self.immediateCauseER(),
                self.immediateCauseRS(),
            ]
 
    def includeChoices(self):
        return [
            self.includeEmergency(),
            ]
        
    def getSource(self):
        choices = [
            self.dopplerSource(),
            self.dopplerGaugesSource(),
            self.trainedSpottersSource(),
            self.publicSource(),
            self.localLawEnforcementSource(),
            self.emergencyManagementSource(),
            self.satelliteSource(),
            self.gaugesSource(),
                    ]
        return {
            "fieldName": "source",
            "fieldType":"RadioButtons",
            "label":"Source:",
            "values": self.defaultValue(choices),
            "choices": choices,                
                }  
        
    def getEventType(self):
        return {
                "fieldType": "Composite",
                # TODO Eliminate this wrapper when RM 5037 is addressed.
                "fieldName": "eventTypeWrapper",
                "fields": [
                    {
                     "fieldType":"RadioButtons",
                     "fieldName": "eventType",
                     "label": "Event type:",
                     "values": "thunderEvent",
                     "choices": [
                            self.eventTypeThunder(),
                            self.eventTypeRain(),
                            self.eventTypeFlashFlooding(),
                            ]
                        }
                    ]
            }  

    def getDebrisFlowOptions(self):
        choices = [
                   self.debrisFlowUnknown(),
                   self.debrisFlowBurnScar(),
                   self.debrisFlowMudSlide(),
                   ]
        return {
                 "fieldType":"RadioButtons",
                 "fieldName": "debrisFlow",
                 "label": "Debris Flow Info:",
                 "choices": choices,
                 "values":choices[0]
                }        

    def debrisFlowUnknown(self):
        return {"identifier":"debrisFlowUnknown",
        "displayString": "Unknown"
        }
 
    def debrisFlowBurnScar(self):
        return {"identifier":"debrisFlowBurnScar", 
                "displayString": "Burn scar area with debris flow", 
                "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "debrisBurnScarDrainage",
                             "expandHorizontally": True,
                             "visibleChars": 12,
                             "values": "Enter drainage",
                            }]
               }
    def debrisFlowMudSlide(self):
        return {"identifier":"debrisFlowMudSlide",
                "displayString": "Mud Slides"
        }
 
    def additionalInfoChoices(self):
        return [ 
            self.listOfCities(),
            self.additionalRain(),
            self.floodLocation(),
            self.listOfDrainages(),
            ]
            
    def getCTA_Choices(self):
        return [
            self.ctaFlashFloodWarningMeans(),
            self.ctaActQuickly(),
            self.ctaChildSafety(),
            self.ctaNightTime(),
            self.ctaSafety(),
            self.ctaTurnAround(),
            self.ctaStayAway(),
            self.ctaArroyos(),
            self.ctaBurnAreas(),
            self.ctaReportFlooding(),
            ]
         
        
    def CAP_WEA_Values(self):
        if self.hazardStatus == "pending":
           return ["WEA_activated"]
        else:
           return [] 
       
    def CAP_WEA_Text(self):
        return "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"
    
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges

