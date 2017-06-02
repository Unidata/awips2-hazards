import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus in ["elapsed", "ending", "ended"]:
            metaData = [
                        self.getPreviousEditedText(),
                        self.getEndingOption(),
                        ]
        elif self.hazardStatus == 'pending':
           metaData = [
                     self.getPreviousEditedText(),
                     self.getInclude(),
                     self.setBurnScarNameLabel(self.hazardEvent),
                     self.getImmediateCause(),
                     self.getSource(),
                     self.getEventType(),
                     self.getFlashFloodOccurring(),
                     self.getDebrisFlowOptions(),
                     self.getRainAmt(),
                     self.getAdditionalInfo(),
                     self.getLocationsAffected(self.hazardEvent),
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
                     self.getPreviousEditedText(),
                     self.getInclude(),
                     self.setBurnScarNameLabel(hazardEvent),
                     self.getImmediateCause(),
                     self.getSource(),
                     self.getEventType(),
                     self.getFlashFloodOccurring(),
                     self.getDebrisFlowOptions(),
                     self.getRainAmt(),
                     self.getAdditionalInfo(),
                     # TODO this should only be on the HID for EXT and not CON
                     self.getLocationsAffected(hazardEvent),
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
                        ]
                }

    def getFlashFloodOccurring(self, defaultOn=False):
        return {
             "fieldType":"CheckBox",
             "fieldName": "flashFlood",
             "label": "Flash flooding occurring",
             "value": defaultOn,
            }

    def getDebrisFlowOptions(self):
        choices = [
                   self.debrisFlowBurnScar(),
                   self.debrisFlowMudSlide(),
                   self.debrisFlowUnknown(),
                   ]
        return {
                 "fieldType":"RadioButtons",
                 "fieldName": "debrisFlow",
                 "label": "Debris Flow Info:",
                 "choices": choices,
                 "values":choices[0]["identifier"]
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
            self.listOfDrainages(),
            self.additionalRain(),
            ]
        
    def getCTA_Choices(self):
        return [
            self.ctaFlashFloodWarningMeans(),
            self.ctaBurnAreas(),
            self.ctaTurnAround(),
            self.ctaActQuickly(),
            self.ctaChildSafety(),
            self.ctaNightTime(),
            self.ctaUrbanFlooding(),
            self.ctaRuralFlooding(),
            self.ctaStayAway(),
            self.ctaLowSpots(),
            self.ctaArroyos(),
            self.ctaCamperSafety(),
            self.ctaReportFlooding(),
            ]
        
    def CAP_WEA_Values(self):
        if self.hazardStatus == "pending":
           return ["WEA_activated"]
        else:
           return [] 

    def CAP_WEA_Text(self):
        return "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"
    
    def endingOptionChoices(self):
        return [
            self.recedingWater(),
            self.rainEnded(),
            ]

    def validate(self,hazardEvent):
        message1 = self.validateRainSoFar(hazardEvent)
        message2 = self.validateAdditionalRain(hazardEvent,checkStatus=True)
        message3 = self.validateLocation(hazardEvent)
        retmsg = None
        if message1:
            retmsg = message1
        if message2:
            if retmsg:
                retmsg += "\n\n" + message2
            else:
                retmsg = message2
        if message3:
            if retmsg:
                retmsg += "\n\n" + message3
            else:
                retmsg = message3
        return retmsg

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges

