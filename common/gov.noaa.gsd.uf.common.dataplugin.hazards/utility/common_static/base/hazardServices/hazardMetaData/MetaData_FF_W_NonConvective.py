'''
    Description: Hazard Information Dialog Metadata for hazard type FF.W.NonConvective
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)

        immediateCause = None
        if hazardEvent is not None:
            immediateCause = hazardEvent.get("immediateCause")

        metaData = self.buildMetaDataList(self.hazardStatus, immediateCause)
        
        return {
                METADATA_KEY: metaData
                }    
       
    def buildMetaDataList(self, status, immediateCause):
        
        
        addDam = [
                  self.immediateCauseDam()["identifier"],
                  self.immediateCauseSiteImminent()["identifier"],
                  self.immediateCauseSiteFailed()["identifier"],
                  self.immediateCauseLevee()["identifier"],
                  self.immediateCauseFloodGate()["identifier"]
                  ]
        
        addVolcano = [
                      self.immediateCauseVolcano()["identifier"],
                      self.immediateCauseVolcanoLahar()["identifier"]
                      ]
        
        
        if status == 'pending':
            metaData = [
                     self.getInclude(),
                     self.getFloodSeverity(),
                     self.getImmediateCause(),
                     self.getSource(immediateCause),
                     self.getImpactedLocations(),
                     self.getAdditionalInfo(),
                     self.getScenario(),
                     self.getRiver(),
                     self.getFloodLocation(),
                     self.getUpstreamLocation(),
                     self.getDownstreamLocation(),
                     self.getCTAs(), 
                        ]
        elif status == "issued":
            metaData = [
                     self.getFloodSeverity(),
                     self.getImmediateCause(editable=False),
                     self.getSource(immediateCause),
                     self.getImpactedLocations(),
                     self.getAdditionalInfo(),
                     self.getScenario(),
                     self.getRiver(editable=False),
                     self.getFloodLocation(),
                     self.getUpstreamLocation(),
                     self.getDownstreamLocation(),
                     self.getCTAs(), 
                     # Preserving CAP defaults for future reference.
#                      self.getCAP_Fields([
#                                           ("urgency", "Immediate"),
#                                           ("severity", "Severe"),
#                                           ("certainty", "Likely"),
#                                           ("responseType", "Avoid"),
#                                          ]) 
                    ]  
        else:
            metaData = [
                    self.getEndingSynopsis(), 
                    ]
            return metaData
        
        if immediateCause is not None:
            
            if immediateCause in addDam:
                metaData.insert(5, self.getDamOrLevee())
                
            if immediateCause in addVolcano:
                metaData.insert(len(metaData)-2,self.getVolcano())
                
        return metaData

    def getSource(self, immediateCause):
        choices = self.sourceChoices(immediateCause)
        
        return {
            "fieldName": "source",
            "fieldType":"RadioButtons",
            "label":"Source:",
            "values": self.defaultValue(choices),
            "choices": choices,
            } 
        
    # INCLUDE  -- include
    #
    #<bullet bulletName="ffwEmergency" bulletText="**SELECT FOR FLASH FLOOD EMERGENCY**" bulletGroup="ffwEMER" floodSeverity="3" parseString="FLASH FLOOD EMERGENCY"/>
    # TODO -- put in side-effect to set floodSeverity to 3 if chosen
    def includeChoices(self):
        return [
            self.includeEmergency(),
            ]      
        
    def floodSeverityChoices(self):
        return [
            self.floodSeverityUnknown(),
            self.floodSeverityMinor(),
            self.floodSeverityModerate(),
            self.floodSeverityMajor()
        ]        
    def floodSeverityUnknown(self):
        return {"identifier": "U", "displayString": "Unknown","productString": ""}
    def floodSeverityMinor(self):
        return {"identifier": "1", "displayString": "Minor flood", "productString": "Minor"}
    def floodSeverityModerate(self):
        return {"identifier": "2", "displayString": "Moderate flood","productString": "Moderate"}
    def floodSeverityMajor(self):
        return {"identifier": "3", "displayString": "Major flood","productString": "Major"}
    
    def getImmediateCause(self, editable=True):
        return {   
            # The immediate cause will be automatically assigned based on the hydrologic cause chosen.  
             "fieldType":"ComboBox",
             "fieldName": "immediateCause",
             "label":"Immediate Cause:",
             "editable": editable,
             "values": "dam",
             "choices": self.immediateCauseChoices(),
             "refreshMetadata": True
             }
        
        
    def immediateCauseChoices(self):
        return [
            self.immediateCauseDam(),
            self.immediateCauseSiteImminent(),
            self.immediateCauseSiteFailed(),
            self.immediateCauseLevee(),           
            self.immediateCauseFloodGate(),
            self.immediateCauseGlacialOutburst(),
            self.immediateCauseIceJam(),
            self.immediateCauseSnowMelt(),            
            self.immediateCauseVolcano(),
            self.immediateCauseVolcanoLahar(),
            ]
    def immediateCauseDam(self):
        return {"identifier":"dam", "displayString":"Dam failure - generic"}
    def immediateCauseSiteImminent(self):
        return {"identifier":"siteImminent", "displayString":"Dam break - site specific - imminent failure"}
                
    def immediateCauseSiteFailed(self):
        return {"identifier":"siteFailed", "displayString":"Dam - site specific - failure has occurred"}
    def immediateCauseLevee(self):
        return {"identifier":"levee", "displayString":"Levee failure"}
    def immediateCauseFloodGate(self):
        return {"identifier":"floodgate",  "displayString":"Floodgate opening"}
    def immediateCauseGlacialOutburst(self):
        return {"identifier":"glacier", "displayString":"Glacier-dammed lake outburst"}
    def immediateCauseIceJam(self):
        return {"identifier":"icejam", "displayString":"Ice jam"}
    def immediateCauseSnowMelt(self):
        return {"identifier":"snowMelt", "displayString":"Rapid snowmelt (with or without rain)"}
    def immediateCauseVolcano(self):
        return {"identifier":"volcano", "displayString":"Volcano induced snowmelt"}
    def immediateCauseVolcanoLahar(self):
        return {"identifier":"volcanoLahar", "displayString":"Volcano induced lahar/debris flow"}

    def sourceChoices(self, immediateCause):
        
        
        addDam = [
                  self.immediateCauseDam()["identifier"],
                  self.immediateCauseSiteImminent()["identifier"],
                  self.immediateCauseSiteFailed()["identifier"],
                  self.immediateCauseLevee()["identifier"],
                  self.immediateCauseFloodGate()["identifier"]
                  ]
        
        addVolcano = [
                      self.immediateCauseVolcano()["identifier"],
                      self.immediateCauseVolcanoLahar()["identifier"]
                      ]
        
        choices = [  
            self.countyDispatchSource(),
            self.localLawEnforcementSource(),
            self.corpsOfEngineersSource(),
            self.bureauOfReclamationSource(),            
            self.publicSource(),
            self.gaugesSource(),
            self.civilAirPatrolSource(),
            ] 
                    
        if immediateCause in addDam:
            choices.insert(3, self.damOperatorSource())
                
        if immediateCause in addVolcano:
            choices.append(self.alaskaVolcanoObservatorySource())
            choices.append(self.cascadesVolcanoObservatorySource())
        
        
        return choices

    def getImpactedLocations(self):
            return {
                     "fieldType":"CheckBoxes",
                     "fieldName": "impactedLocations",
                     "showAllNoneButtons" : False,
                     "label": "Impacted Locations:",
                     "choices": [self.listOfCities()],
                     "lines": 1
                    }

    def getAdditionalInfo(self):
            result = CommonMetaData.MetaData.getAdditionalInfo(self)
            result["values"] = "listOfDrainages"
            return result
             
    def additionalInfoChoices(self):
        return [ 
            self.listOfDrainages(),
            self.floodMoving(),
            ]
          
    def getDamOrLevee(self):

        damOrLeveeName = 'Branched Oak Dam'            
        return {
            "fieldName": "damOrLeveeName",
            "fieldType":"ComboBox",
            "label":"Dam or Levee:",
            "values": damOrLeveeName,
            "choices": self.damOrLeveeChoices(),
            } 
    def damOrLeveeChoices(self):
        return [
                self.BranchedOakDam(),
                self.CouncilBluffsLevee()
                ]        
    def BranchedOakDam(self):
        return {"identifier":"Branched Oak Dam", 
                "displayString": "Branched Oak Dam",
                "productString": "Branched Oak Dam"}
    def CouncilBluffsLevee(self):
        return {"identifier":"Council Bluffs Levee", 
                "displayString": "Council Bluffs Levee",
                "productString": "Council Bluffs Levee"}    


    def getScenario(self):
        return {
            "fieldName": "scenario",
            "fieldType":"ComboBox",
            "label":"Scenario:",
            "choices": self.scenarioChoices(),
            } 
    def scenarioChoices(self):
        return [
                self.scenarioHighFast(),
                self.scenarioHighNormal(),
                self.scenarioMediumFast(),
                self.scenarioMediumNormal(),
                ]        
    def scenarioHighFast(self):
        return {"identifier":"highFast", 
                "displayString": "High Fast",
                "productString": "High Fast"}
    def scenarioHighNormal(self):
        return {"identifier":"highNormal", 
                "displayString": "High Normal",
                "productString": "High Normal"}
    def scenarioMediumFast(self):
        return {"identifier":"mediumFast", 
                "displayString": "Medium Fast",
                "productString": "Medium Fast"}
    def scenarioMediumNormal(self):
        return {"identifier":"mediumNormal", 
                "displayString": "Medium Normal",
                "productString": "Medium Normal"}
       

    def getRiver(self, editable=True):
        return {
             "fieldType": "Text",
             "fieldName": "riverName",
             "expandHorizontally": True,
             "maxChars": 40,
             "visibleChars": 12,
             "editable": editable,
             "values": "|* Enter river name *|",
            } 

    def getFloodLocation(self):
        return {
             "fieldType": "Text",
             "fieldName": "floodLocation",
             "expandHorizontally": True,
             "maxChars": 40,
             "visibleChars": 12,
             "values": "|* Enter flood location *|",
            } 


    def getUpstreamLocation(self):
        return {
             "fieldType": "Text",
             "fieldName": "upstreamLocation",
             "expandHorizontally": True,
             "maxChars": 40,
             "visibleChars": 12,
             "values": "|* Enter upstream location *|",
            } 
 
    def getDownstreamLocation(self):
        return {
             "fieldType": "Text",
             "fieldName": "downstreamLocation",
             "expandHorizontally": True,
             "maxChars": 40,
             "visibleChars": 12,
             "values": "|* Enter downstream location *|",
            } 
        
    def getVolcano(self):
        return {
             "fieldType": "Text",
             "fieldName": "volcanoName",
             "expandHorizontally": True,
             "maxChars": 40,
             "visibleChars": 12,
             "values": "|* Enter volcano name *|",
            } 

    def countyDispatchSource(self):
        return {"identifier":"countySource", 
                "displayString": "County dispatch"
               }
    def corpsOfEngineersSource(self):
        return {"identifier":"corpsOfEngineersSource", 
                "displayString": "Corps of engineers"
               }
    def damOperatorSource(self):
        return {"identifier":"damOperatorSource", 
                "displayString": "Dam operator"
               }
    def bureauOfReclamationSource(self):
        return {"identifier":"bureauOfReclamationSource", 
                "displayString": "Bureau of reclamation"
               }
    def civilAirPatrolSource(self):
        return {"identifier":"civilAirPatrolSource", 
                "displayString": "Civil Air Patrol"
               }
    def alaskaVolcanoObservatorySource(self):
        return {"identifier":"alaskaVolcanoSource", 
                "displayString": "Alaska Volcano Observatory"
               }
    def cascadesVolcanoObservatorySource(self):
        return {"identifier":"cascadesVolcanoSource", 
                "displayString": "Cascades Volcano Observatory"
               }

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
        
    def cancellationStatement(self):
        return {
             "fieldType": "Text",
             "fieldName": "cancellationStatement",
             "expandHorizontally": True,
             "visibleChars": 12,
             "lines": 2,
             "values": "Enter source text",
            } 

         
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
