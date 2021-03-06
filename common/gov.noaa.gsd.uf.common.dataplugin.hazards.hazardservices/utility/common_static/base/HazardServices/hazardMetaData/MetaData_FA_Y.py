'''
    Description: Hazard Information Dialog Metadata for hazard type FA.Y
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
                    self.getAdvisoryType(),
                    self.getImmediateCause(),
                    self.getHiddenFloodSeverityNone(),
                    self.getOptionalSpecificType(),
                    self.getSource(),
                    self.getEventType(),
                    self.getMinorFloodOccurring(),
                    self.getRainAmt(),
                    self.getLocationsAffected(),
                    self.getAdditionalInfo(),
                    self.getCTAs(),
                ] 
        return {
                METADATA_KEY: metaData
                }    
    
    def getAdvisoryType(self):
        return {   
             "fieldType":"RadioButtons",
             "fieldName": "advisoryType",
             "label":"Type of Advisory:",
             "values": "generalMinorFlooding",
             "choices": self.advisoryChoices(),
             "editable" : self.editableWhenNew(),
             }
       
    def advisoryChoices(self):
        return [
            self.generalMinorFlooding(),
            self.smallStreams(),
            self.urbanAreasSmallStreams(),
            self.arroyoSmallStreams(),
            self.hydrologic(),
            ]
        
    def getOptionalSpecificType(self):
        return {   
             "fieldType":"ComboBox",
             "fieldName": "optionalSpecificType",
             "label":"Optional Specific Type:",
             "values": "noSpecificFlooding",
             "choices": [
                         self.noSpecificFlooding(),
                         self.rapidRiverRises(),
                         self.poorDrainage(),
                         ],
             "editable" : self.editableWhenNew(),
             }
        
    def noSpecificFlooding(self):
                return {"identifier":"noSpecificFlooding",
                        "displayString": "None",
                        "productString":"",
                 }
                 
    def rapidRiverRises(self):
                return {"identifier":"rapidRiverRises",
                        "displayString": "Rapid River Rises",
                        "productString":"rapid river rises",
                 }
                
    def poorDrainage(self):
                return {"identifier":"poorDrainage",
                        "displayString": "Minor Flooding Of Poor Drainage",
                        "productString": "poor drainage areas",
                 }
        
    def generalMinorFlooding(self):
                return {"identifier":"generalMinorFlooding",
                        "displayString": "General (minor flooding)",
                        "productString":""
                 }
                
    def smallStreams(self):
        return {"identifier":"smallStreams",
                "displayString": "Small streams",
                "productString":"Small streams"
                 }

    def urbanAreasSmallStreams(self):
        return  {"identifier":"urbanAreasSmallStreams",
                 "displayString": "Urban areas and small streams",
                 "productString":"Urban and small stream"
                 }
    def arroyoSmallStreams(self):
        return {"identifier":"arroyoSmallStreams",
                "displayString": "Arroyo and small streams",
                "productString":"Arroyo and small streams"
                 }
    def hydrologic(self):
        return {"identifier":"hydrologic", 
                "displayString": "Hydrologic flooding",
                  "productString":"Hydrologic"
                  }
        
    def immediateCauseChoices(self):
        return [
                self.immediateCauseER(),
                self.immediateCauseSM(),
                self.immediateCauseRS(),
                self.immediateCauseIJ(),
                self.immediateCauseIC(),
                self.immediateCauseDR(),
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
            self.gaugesSource(),
                    ]
    def getEventTypeChoices(self):
        return [
                self.eventTypeThunder(),
                self.eventTypeRain(),
                ]

    def getMinorFloodOccurring(self, defaultOn=False):
        return {
             "fieldType":"CheckBox",
             "fieldName": "minorFlood",
             "label": "Minor flooding occurring",
             "value": defaultOn,
            }

    def additionalInfoChoices(self):
        return [ 
            self.additionalRain(),
            self.floodLocation(),
            self.listOfDrainages(),
            ]
 
    def getCTA_Choices(self):
        return [
            self.ctaFloodAdvisoryMeans(),
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
        
    def floodLocation(self):
        return {"identifier":"floodLocation",
                "displayString": "Specify location of flooding:",
                "productString": "|* floodLocation *| is the most likely place to experience minor flooding.",
                "detailFields": [
                        {
                         "fieldType": "Text",
                         "fieldName": "floodLocation",
                         "expandHorizontally": True,
                         "maxChars": 40,
                         "visibleChars": 12
                        }]
                }

    def endingOptionChoices(self):
        return [
            self.recedingWater(),
            self.rainEnded(),
            self.advisoryUpgraded()
            ]

    def validate(self,hazardEvent):
        messageList = []
        
        messageList.append(self.validateRainSoFar(hazardEvent))
        messageList.append(self.validateAdditionalRain(hazardEvent))
        messageList.append(self.validateEndingOptions(hazardEvent))
        
        retmsg = None
        for message in messageList:
            if message is not None:
                if (retmsg is None):
                    retmsg = "\n" + message 
                else:
                    retmsg += "\n \n" + message
        return retmsg

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges
