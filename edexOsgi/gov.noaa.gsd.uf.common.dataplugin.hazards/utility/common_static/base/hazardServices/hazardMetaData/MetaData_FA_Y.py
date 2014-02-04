



import CommonMetaData

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self._hazardEvent = hazardEvent
        self._metaDict = metaDict
        metaData = [
                    self.getInclude(),
                    self.getImmediateCause(),
                    self.getHydrologicCause(),
                    self.getRainAmt(),
                    self.getBasis(),
                    self.getAdditionalInfo(),
                    self.getCTAs(),                    
                    ] + self.setCAP_Fields()
        return metaData
    
    # INCLUDE  
    def includeChoices(self):
        return [
            self.includeSmallStreams(),
            self.includeUrbanAreas(),
            self.includeArroyoSmallStreams(),
            self.includeHydrologic(),
            ] 
    
    # IMMEDIATE CAUSE  
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
                self.immediateCauseOT(),
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
            #self.basisSpotterHeavyRain(),
            self.basisSpotterThunderstorm(),
            self.basisSpotterIncipientFlooding(),
            self.basisLawEnforcement(),
            #self.basisLawEnforcementHeavyRain(),
            self.basisLawEnforcementThunderstorm(),
            self.basisLawEnforcementIncipientFlooding(),
            self.basisEmergencyManagement(),
            #self.basisEmergencyManagementHeavyRain(),
            self.basisEmergencyManagementThunderstorm(),
            self.basisEmergencyManagementIncipientFlooding(),
            self.basisPublic(),
            #self.basisPublicHeavyRain(),
            self.basisPublicThunderstorm(),
            self.basisPublicIncipientFlooding(),
            self.basisSatellite(),
            self.basisSatelliteGauges(),
            ] 
        
    # ADDITIONAL INFORMATION
    def additionalInfoChoices(self):
        previewState = self._hazardEvent.get("previewState")
        if previewState == "ended":
            return [ 
                self.recedingWater(),
                self.rainEnded(),
                ]
        else:
            return [ 
                self.listOfCities(),
                #self.listOfDrainages(),
                self.additionalRain(),
                self.specificPlace(),
                ]
 
    # CALLS TO ACTION
    def getCTA_Choices(self):
        return [
            self.ctaNoCTA(),
            self.ctaFloodAdvisoryMeans(),
            self.ctaTurnAround(),
            self.ctaUrbanFlooding(),
            self.ctaRuralFlooding(),
            #self.ctaRuralUrbanFlooding(),
            #self.ctaNightTime(),
            self.ctaDontDrive(), 
            #self.ctaCamperSafety(),
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
                ("severity", "Minor"),
                ("certainty", "Likely"),
                ("responseType", "Avoid"),
                ]:
                if entry["fieldName"] == fieldName:
                    entry["values"] = values  
        return capFields          

