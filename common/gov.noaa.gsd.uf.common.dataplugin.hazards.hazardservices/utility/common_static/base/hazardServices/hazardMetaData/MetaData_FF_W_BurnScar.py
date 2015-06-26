import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus in ["ending", "ended"]:
            metaData = [
                        self.getListOfCities(False),
                        self.getEndingOption(),
                        self.getEndingSynopsis()
                        ]
        elif self.hazardStatus == 'pending':
           metaData = [
                     self.getInclude(),
                     self.setBurnScarNameLabel(hazardEvent),
                     self.getImmediateCause(),
                     self.getSource(),
                     self.getEventType(),
                     self.getDebrisFlowOptions(),
                     self.getRainAmt(),
                     self.getAdditionalInfo(),
                     self.getLocationsAffected(False),
                     self.getCTAs(), 
                     # Preserving CAP defaults for future reference.
#                      self.getCAP_Fields([
#                                           ("urgency", "Immediate"),
#                                           ("severity", "Severe"),
#                                           ("certainty", "Likely"),
#                                           ("responseType", "Avoid"),
#                                          ])
                    ]
        else: # issued
           metaData = [
                     self.getBasisAndImpacts('basisAndImpactsStatement_segmentLevel'), 
                     self.getInclude(),
                     self.setBurnScarNameLabel(hazardEvent),
                     self.getImmediateCause(),
                     self.getSource(),
                     self.getEventType(),
                     self.getDebrisFlowOptions(),
                     self.getRainAmt(),
                     self.getAdditionalInfo(),
                     self.getListOfCities(False),
                     # TODO this should only be on the HID for EXT and not CON
                     self.getLocationsAffected(False),
                     self.getCTAs(), 
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
       
        enabled = False
        edit = False
        if self.hazardStatus == 'pending':
            enabled = True
            edit = True
        
        label = {
            "fieldName": "burnScarName",
            "fieldType":"Text",
            "values": bsName,
            "valueIfEmpty": "|* Enter Burn Scar or Location *|",
            "promptText": "Enter burn scar or location",
            "visibleChars": 40,
            'enable': enabled,
            'editable': edit,
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
                 "fieldType":"ComboBox",
                 "fieldName": "eventType",
                 "label": "Event type:",
                 "expandVertically": False,
                 "values": "thunderEvent",
                 "choices": [
                        self.eventTypeThunder(),
                        self.eventTypeRain(),
                        self.eventTypeFlashFlooding(),
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
                             "promptText": "Enter drainage",
                            }]
               }
    def debrisFlowMudSlide(self):
        return {"identifier":"debrisFlowMudSlide",
                "displayString": "Mud Slides"
		}
 
    def additionalInfoChoices(self):
        return [ 
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

