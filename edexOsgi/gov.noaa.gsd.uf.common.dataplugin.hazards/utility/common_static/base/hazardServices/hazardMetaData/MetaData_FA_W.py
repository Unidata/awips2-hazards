import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
                    self.getInclude(),
                    self.getImmediateCauseMessage(),
                    self.getImmediateCause(),
                    self.getRainAmt(),
                    self.getBasis(),
                    self.getAdditionalInfo(),
                    self.getCTAs(),                    
                    ] + self.setCAP_Fields()
        return {
                METADATA_KEY: metaData
                }    
    
    # INCLUDE  
    def includeChoices(self):
        return [
            self.includeSmallStreams(),
            self.includeUrbanAreasSmallStreams(),
            ] 
        
    def getImmediateCauseMessage(self): 
        return {
             "fieldName": "miscLabel",
             "fieldType":"Label",
             "wrap": True,
             "label":"If either 'include small streams' or 'include urban areas and small streams' above was selected, 'floodgate opening' in immediate cause below should not be selected.  If 'floodgate opening' is the desired option, do not select 'include small streams' or 'include urban areas and small streams'. ",
            }
        
    def getImmediateCause(self):
        return {
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "ER",
            "choices": [
                self.immediateCauseER(),
                self.immediateCauseSM(),
                self.immediateCauseRS(),
                self.immediateCauseDM(),
                self.immediateCauseDR(),
                self.immediateCauseGO(),
                self.immediateCauseIJ(),
                self.immediateCauseIC(),
                self.immediateCauseMC(),
                self.immediateCauseUU(),
                ]
                }

      # BASIS
    def basisChoices(self):
        return [  
            self.basisDoppler(),
            self.basisDopplerThunderstorm(),
            self.basisDopplerGauges(),
            self.basisDopplerGaugesThunderstorm(),            
            self.basisSpotter(),
            self.basisSpotterHeavyRain(),
            self.basisSpotterThunderstorm(),
            self.basisLawEnforcement(),
            self.basisLawEnforcementHeavyRain(),
            self.basisLawEnforcementThunderstorm(),
            self.basisEmergencyManagement(),
            self.basisEmergencyManagementHeavyRain(),
            self.basisEmergencyManagementThunderstorm(),
            self.basisPublic(),
            self.basisPublicHeavyRain(),
            self.basisPublicThunderstorm(),
            self.basisSatellite(),
            self.basisSatelliteGauges(),
            ] 
        
    # ADDITIONAL INFORMATION
    def additionalInfoChoices(self):
        if self.hazardState == "ended":
            return [ 
                self.recedingWater(),
                self.rainEnded(),
                ]
        else:
            return [ 
                self.listOfCities(),
                self.listOfDrainages(),
                self.additionalRain(),
                self.particularStream(),
                ]
 
    # CALLS TO ACTION
    def getCTA_Choices(self):
        return [
            self.ctaNoCTA(),
            self.ctaFloodWarningMeans(),
            self.ctaTurnAround(),
            self.ctaUrbanFlooding(),
            self.ctaRuralFlooding(),
            self.ctaRuralUrbanFlooding(),
            self.ctaNightTime(),
            self.ctaDontDrive(), 
            self.ctaCamperSafety(),
            self.ctaLowSpots(),
            self.ctaPowerFlood(),
            self.ctaReportFlooding(),
            ]
        
    # CAP fields        
    def setCAP_Fields(self):
        # Set the defaults for the CAP Fields
        capFields = self.getCAP_Fields()
        for entry in capFields:
            for fieldName, values in [
                ("urgency", "Expected"),
                ("severity", "Severe"),
                ("certainty", "Likely"),
                ("responseType", "None"),
                ]:
                if entry["fieldName"] == fieldName:
                    entry["values"] = values  
        return capFields          

