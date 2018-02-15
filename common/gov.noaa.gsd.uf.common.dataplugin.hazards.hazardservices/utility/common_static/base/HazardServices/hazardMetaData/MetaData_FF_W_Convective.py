'''
    Description: Hazard Information Dialog Metadata for hazard type FF.W.Convective
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):

    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == "ending":
                metaData = [
                            self.getEndingOption(),
                    ]
        else:
            metaData = [
                    self.getInclude(),
                    self.getImmediateCause(),
                    self.getSource(),
                    self.getEventType(),
                    self.getFlashFloodOccurring(),
                    self.getRainAmt(),
                    self.getAdditionalInfo(),
                    self.getFloodLocation(),
                    self.getLocationsAffected(),
                    self.getCTAs(),
                        ]
        return {
                METADATA_KEY: metaData
                }    
        
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
        
    def includeChoices(self):
        return [
            self.includeEmergency(),
            ]     
         
    def getSourceChoices(self):
        return [
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

    def getEventTypeChoices(self):
        return [
                self.eventTypeThunder(),
                self.eventTypeRain(),
                ]
        
    def getFlashFloodOccurring(self, defaultOn=False):
        return {
             "fieldType":"CheckBox",
             "fieldName": "flashFlood",
             "label": "Flash flooding occurring",
             "value": defaultOn,
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
            self.ctaFFWEmergency(),
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
            self.ctaFlashFloodWarningMeans(),
            ]

    def endingOptionChoices(self):
        return [
            self.recedingWater(),
            self.rainEnded(),
            ]

    def getLocationsAffected(self):
        # False means don't have pathcast be default Haz Inf Dialog choice
        return super(MetaData ,self).getLocationsAffected(False)

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges

