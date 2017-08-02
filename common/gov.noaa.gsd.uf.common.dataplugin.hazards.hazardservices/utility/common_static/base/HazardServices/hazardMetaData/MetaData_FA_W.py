'''
    Description: Hazard Information Dialog Metadata for hazard type FA.W
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus in ["elapsed", "ending", "ended"]:
            # FA.W point hazards do not have the endingSynopsis productPart
            # Removing the metaData for it so it does not show up in the HID.
            if hazardEvent.get('geoType', '') == 'area':
                metaData = [
                            self.getPreviousEditedText(),
                            self.getWarningType(),
                            self.getImmediateCause(),
                            self.getEndingOption(),
                            ]
            else:
                metaData = []
        elif self.hazardStatus == 'pending':
            metaData = [
                    self.getPreviousEditedText(),
                    self.getWarningType(),
                    self.getImmediateCause(),
                    self.getHiddenFloodSeverity(),
                    self.getSource(),
                    self.getEventType(),
                    self.getFloodOccurring(),
                    self.getRainAmt(),
                    self.getLocationsAffected(),
                    self.getAdditionalInfo(refreshMetadata=True),
                    ]
            if (hazardEvent is not None
                and hazardEvent.get('additionalInfo') is not None
                and 'floodMoving' in hazardEvent.get('additionalInfo')):
                metaData += [
                        self.getRiver(),
                        self.getFloodLocation(),
                        self.getUpstreamLocation(),
                        self.getDownstreamLocation(),
                        ]
            metaData += [
                    self.getCTAs(),  
                    # Preserving CAP defaults for future reference.                  
#                     self.getCAP_Fields([
#                                         ("urgency", "Expected"),
#                                         ("severity", "Severe"),
#                                         ("certainty", "Likely"),
#                                         ("responseType", "None"),
#                                        ]),
                    ]
            if hazardEvent is not None:
                immediateCause = hazardEvent.get("immediateCause")
                if immediateCause == self.immediateCauseDM()['identifier']:
                    damOrLeveeName = hazardEvent.get('damOrLeveeName')
                    metaData.insert(2, self.getDamOrLevee(damOrLeveeName))
        else: # 'issued'
            metaData = [
                    self.getPreviousEditedText(),
                    self.getWarningType(),
                    self.getImmediateCause(),
                    self.getHiddenFloodSeverity(),
                    self.getSource(),
                    self.getEventType(),
                    self.getFloodOccurring(),
                    self.getRainAmt(),
                    self.getLocationsAffected(),
                    self.getAdditionalInfo(refreshMetadata=True),
                    ]
            if (hazardEvent is not None
                and hazardEvent.get('additionalInfo') is not None
                and 'floodMoving' in hazardEvent.get('additionalInfo')):
                metaData += [
                        self.getRiver(),
                        self.getFloodLocation(),
                        self.getUpstreamLocation(),
                        self.getDownstreamLocation(),
                        ]
            metaData += [
                    self.getCTAs(),
                ]
            
        return {
                METADATA_KEY: metaData
                }
        
    def getWarningType(self):
        return {   
             "fieldType":"RadioButtons",
             "fieldName": "warningType",
             "label":"Type of Warning:",
             "values": "genericFloodWarning",
             "choices": self.warningChoices(),
             "editable" : self.editableWhenNew(),
             }    
    
    def warningChoices(self):
        return [
            self.genericFloodWarning(),
            self.smallStreamsWarning(),
            self.urbanSmallStreamsWarning(),
            ]
        
    def genericFloodWarning(self):
                return {"identifier":"genericFloodWarning",
                        "displayString": "Flood warning: generic",
                        "productString":""
                 }
                
    def smallStreamsWarning(self):
                return {"identifier":"smallStreamsWarning","displayString": "Flood warning for small streams",
                        "productString":"Small Streams"
                 }
    def urbanSmallStreamsWarning(self):
                return {"identifier":"urbanSmallStreamsWarning",
                        "displayString": "Flood warning for urban areas and small streams",
                        "productString":"Urban areas and small streams"
                 }
        
    def immediateCauseChoices(self):
        return [
                self.immediateCauseER(),
                self.immediateCauseSM(),
                self.immediateCauseRS(),
                self.immediateCauseIJ(),
                self.immediateCauseIC(),
                self.immediateCauseMC(),
                self.immediateCauseUU(),
                self.immediateCauseDM(),
                self.immediateCauseDR(),
                self.immediateCauseGO(),
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
                 "fieldType":"DetailedComboBox",
                 "fieldName": "eventType",
                 "label": "Event type:",
                 "expandVertically": True,
                 "values": "thunderEvent",
                 "choices": [
                        self.eventTypeThunder(),
                        self.eventTypeRain(),
                        self.eventTypeGenericFlooding(),
                        ]
                    }

    def additionalInfoChoices(self):
        return [ 
            self.listOfDrainages(),
            self.additionalRain(),
            self.floodMoving(),
            ]
 
    def getCTA_Choices(self):
        return [
            self.ctaFloodWarningMeans(),
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

    def getFloodOccurring(self, defaultOn=False):
        return {
             "fieldType":"CheckBox",
             "fieldName": "flood",
             "label": "Flooding occurring",
             "value": defaultOn,
            }

    def endingOptionChoices(self):
        return [
            self.recedingWater(),
            self.rainEnded(),
            ]

    def validate(self,hazardEvent):
        message1 = self.validateRainSoFar(hazardEvent)
        message2 = self.validateAdditionalRain(hazardEvent)
        if (message1 is None and message2 is None):
            retmsg = None
        elif (message1 is None and message2 is not None):
            retmsg = message2
        elif (message1 is not None and message2 is None):
            retmsg = message1
        elif (message1 is not None and message2 is not None):
            retmsg = "\n" + message1 + "\n \n" + message2
        return retmsg
        
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges
