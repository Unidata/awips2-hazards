'''
    Description: Hazard Information Dialog Metadata for hazard type FF.W.Convective
'''
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
                    self.getImmediateCause(),
                    self.getSource(),
                    self.getEventType(),
                    self.getRainAmt(),
                    self.getAdditionalInfo(),
                    self.getFloodLocation(),
                    self.getLocationsAffected(False),
                    self.getCTAs(),
                    # Preserving CAP defaults for future reference.                 
#                     self.getCAP_Fields([
#                                         ("urgency", "Immediate"),
#                                         ("severity", "Severe"),
#                                         ("certainty", "Likely"),
#                                         ("responseType", "Avoid"),
#                                        ])
                    ]
        else: # issued
            metaData = [
                    self.getPreviousEditedText(),
                    self.getInclude(),
                    self.getImmediateCause(),
                    self.getSource(),
                    self.getEventType(),
                    self.getRainAmt(),
                    self.getAdditionalInfo(),
                    self.getFloodLocation(),
                    # TODO this should only be on the HID for EXT and not CON
                    self.getLocationsAffected(False),
                    self.getCTAs(),
            ]
        return {
                METADATA_KEY: metaData
                }    
        
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
            self.satelliteGaugesSource(),
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
                 "values": "thunderEvent",
                 "choices": [
                        self.eventTypeThunder(),
                        self.eventTypeRain(),
                        self.eventTypeFlashFlooding(),
                        ]
                }

    def additionalInfoChoices(self):
        return [ 
            self.listOfDrainages(),
            self.additionalRain(),
            ]

    def immediateCauseChoices(self):
        return [
                self.immediateCauseER(),
                self.immediateCauseRS(),
            ]

    def getCTA_Choices(self):
        return [
            self.ctaFlashFloodWarningMeans(),
            self.ctaTurnAround(),
            self.ctaActQuickly(),
            self.ctaChildSafety(),
            self.ctaNightTime(),
            self.ctaUrbanFlooding(),
            self.ctaRuralFlooding(),
            self.ctaStayAway(),
            self.ctaLowSpots(),
            self.ctaArroyos(),
            self.ctaBurnAreas(),
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

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges

