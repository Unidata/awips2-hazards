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
                     self.setBurnScarNameLabel(hazardEvent),
                     self.getImmediateCause(),
                     self.getEventSpecificSource(),
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
 
    def setBurnScarNameLabel(self, hazardEvent):
        attrs = hazardEvent.getHazardAttributes()
        bsName = attrs.get('burnScarName')
       
        if bsName is None:
            bsName = "Unnamed"
        
        label = {
            "fieldName": "burnScarLabel",
            "fieldType":"Label",
            "label": bsName,
            "bold": True,
            "italic": True
                }  
        
        group = {
                    "fieldType": "Group",
                    "fieldName": "burnScarGroup",
                    "label": "BurnScar: ",
                    "leftMargin": 10,
                    "rightMargin": 10,
                    "topMargin": 10,
                    "bottomMargin": 10,
                    "expandHorizontally": True,
                    "expandVertically": True,
                    "fields": [label]
                }
        
        
        return group
        
 
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

