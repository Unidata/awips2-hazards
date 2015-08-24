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
                            self.getListOfCities(False),
                            self.getEndingOption(),
                            self.getEndingSynopsis(), 
                            self.getAdditionalInfo(),
                    ]
        elif self.hazardStatus == 'pending':
            metaData = [
                    self.getListOfCities(False),
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
                    self.getListOfCities(False),
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
        if self.hazardStatus in ["elapsed", "ending", "ended"]:
            return [ 
                self.listOfDrainages(),
                ]
        else:
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

