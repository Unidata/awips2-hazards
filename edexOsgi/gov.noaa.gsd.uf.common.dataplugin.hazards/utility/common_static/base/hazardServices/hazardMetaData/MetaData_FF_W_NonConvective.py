import CommonMetaData

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
                    self.getImmediateCause(),
                    self.getInclude(),
                    self.getEventType(),
                    self.getRainAmt(),
                    self.getBasis(),
                    self.getDebrisFlowOptions(),
                    self.getAdditionalInfo(),
                    self.getCTAs(),                    
                    ] + self.setCAP_Fields()
        return metaData
       
    # INCLUDE  
    def includeChoices(self):
        return [
            self.includeEmergency(),
            ]      
    
    
    # BASIS
    def basisChoices(self):
        return [  
            self.basisDoppler(),
            self.basisDopplerGauges(),
            self.basisSpotter(),
            self.basisPublic(),
            self.basisLawEnforcement(),
            self.basisEmergencyManagement()
            ]  


    # ADDITIONAL INFORMATION
    def additionalInfoChoices(self):
        if self.previewState == "ended":
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
         
    # CAP fields        
    def setCAP_Fields(self):
        # Set the defaults for the CAP Fields
        capFields = self.getCAP_Fields()
        for entry in capFields:
            entryFieldName = entry["fieldName"]
            # Urgency, Severity, Certainty, ResponseType
            for fieldName, values in [
                        ("urgency", "Immediate"),
                        ("severity", "Severe"),
                        ("certainty", "Likely"),
                        ("responseType", "Avoid"),
                        ]:
                if entryFieldName == fieldName:
                    entry["values"] = values
        return capFields          
        
    def CAP_WEA_Values(self):
        if self.hazardState == "pending":
           return ["WEA_activated"]
        else:
           return [] 
       
    def CAP_WEA_Text(self):
        return "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"
