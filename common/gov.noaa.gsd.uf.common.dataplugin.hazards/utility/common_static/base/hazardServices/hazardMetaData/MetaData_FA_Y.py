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
                        self.getEndingSynopsis(),
                        ]
        else:
            metaData = [
                    self.getAdvisoryType(),
                    self.getImmediateCause(),
                    self.getOptionalSpecificType(),
                    self.getEventSpecificSource(),
                    self.getRainAmt(),
                    self.getAdditionalInfo(),
                    self.getCTAs(),   
                    # Preserving CAP defaults for future reference.                 
#                     self.getCAP_Fields([
#                                           ("urgency", "Expected"),
#                                           ("severity", "Minor"),
#                                           ("certainty", "Likely"),
#                                           ("responseType", "Avoid"),
#                                         ]) 
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
            self.urbanAreas(),
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
                        "productString":"TBD - What should this be?",
                 }
                 
    def rapidRiverRises(self):
                return {"identifier":"rapidRiverRises",
                        "displayString": "Rapid River Rises",
                        "productString":"For Rapid River Rises",
                 }
                
    def poorDrainage(self):
                return {"identifier":"poorDrainage",
                        "displayString": "Minor Flooding Of Poor Drainage",
                        "productString":"Minor Flooding in Poor Drainage Areas",
                 }
        
    def generalMinorFlooding(self):
                return {"identifier":"generalMinorFlooding",
                        "displayString": "General (minor flooding)",
                        "productString":"General Minor Flooding "
                 }
                
    def smallStreams(self):
        return {"identifier":"smallStreams",
                "displayString": "Small streams",
                "productString":"Small Stream Flooding "
                 }
        
    def urbanAreas(self):
        return {"identifier":"urbanAreas",
                  "displayString": "Urban areas and small streams",
                  "productString":"Urban and Small Stream "
                  }
    def urbanAreasSmallStreams(self):
        return  {"identifier":"urbanAreasSmallStreams",
                 "displayString": "Urban areas and small streams",
                 "productString":"Urban and Small Stream Flooding "
                 }
    def arroyoSmallStreams(self):
        return {"identifier":"arroyoSmallStreams",
                "displayString": "Arroyo and small streams",
                "productString":"Arroyo and Small Stream Flooding "
                 }
    def hydrologic(self):
        return {"identifier":"hydrologic", 
                "displayString": "Hydrologic flooding",
                  "productString":"Hydrologic Flooding "
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
                            self.eventTypeMinorFlooding(),
                            ]
                        }
                    ]
            }  
        
    def additionalInfoChoices(self):
        return [ 
            self.listOfCities(),
            self.additionalRain(),
            self.floodLocation(),
            self.listOfDrainages()
            ]
 
    def getCTA_Choices(self):
        return [
            self.ctaFloodAdvisoryMeans(),
            self.ctaTurnAround(),
            self.ctaUrbanFlooding(),
            self.ctaRuralFlooding(),
            self.ctaDontDrive(), 
            self.ctaLowSpots(),
            self.ctaPowerFlood(),
            self.ctaReportFlooding(),
            ]

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges
