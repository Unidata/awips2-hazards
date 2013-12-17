import CommonMetaData

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEventSet=None):
        self._hazardEventSet = hazardEventSet
        metaData = [
                    self.getImmediateCause(),
                    self.getInclude(),
                    self.getEventType(),
                    self.getRainAmt(),
                    self.getBasis(),
                    self.getDebrisFlowOptions(),
                    self.getAdditionalInfo(),
                    self.getCTAs(),
                    self.setCAP_Fields(),
                    ]
        return metaData
    
    # CAP fields        
    def setCAP_Fields(self):
        # Set the defaults for the CAP Fields
        for entry in self.getCAP_Fields():
            for fieldName, values in [
                        ("urgency", "Immediate"),
                        ("responseType", "Avoid"),
                        ("severity", "Severe"),
                        ("certainty", "Likely"),
                        ("WEA_Text", "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"),
                        ]:
                if entry["fieldName"] == fieldName:
                    entry["values"] = values  
        return CAP_Fields          
    
    # INCLUDE  
    def includeChoices(self):
        return [
            self._includeEmergency(),
            ]      
    
    # EVENT TYPE    
    def _getEventType(self):
        return {
                 "fieldType":"RadioButtons",
                 "fieldName": "eventType",
                 "label": "Event type (Choose 1):",
                 "values": "thunderEvent",
                 "choices": [
                        self._eventTypeThunder(),
                        self._eventTypeRain(),
                        self._eventTypeUndef(),
                        ]
            }                   
    def eventTypeThunder(self):
        return {
                "identifier":"thunderEvent", "displayString":"Thunderstorm(s)",
                "productString":"thunderstorms producing heavy rain",
                }
    def eventTypeRain(self):
        return {
                "identifier":"rainEvent", "displayString": "Due to only heavy rain",
                "productString": "heavy rain",
                }
    def eventTypeUndef(self):
        return {
                "identifier":"undefEvent", "displayString": "Flash flooding occurring",
                "productString": "flash flooding occurring",
                }
    
    # RAIN SO FAR    
    def getRainAmt(self):
        return {
               "fieldType":"RadioButtons",
               "label":"Rain so far:",
               "fieldName": "rainAmt",
               "choices": [
                    self.one_inch(),
                    self.two_inches(),
                    self.three_inches(),
                    self.enterAmount(),
                    ]
                }
    def one_inch(self):
        return {"identifier":"rain1", "displayString":"One inch so far",
                "productString":"up to one inch of rain has already fallen.",}
    def two_inches(self):
         return {"identifier":"rain2", "displayString":"Two inches so far",
                 "productString":"up to two inches of rain has already fallen.",}
    def three_inches(self):
        return  {"identifier":"rain3", "displayString":"Three inches so far",
                 "productString":"up to three inches of rain has already fallen.",}
    def enterAmt(self):                
        return {"identifier":"rainEdit", "displayString":"User defined amount",
                "productString":"!** RAINFALL AMOUNTS **! inches of rain have fallen.",}

    # BASIS
    def basisChoices(self):
        return [  
            self.basisDoppler(),
            self.basisDopplerGauges(),
            self.basisSpotter(),
            self.basisPublic(),
            self.basisLawEnforcement(),
            self.basisEmergencyMgmt()
            ]  

    def getDebrisFlowOptions(self):
        return {
                 "fieldType":"RadioButtons",
                 "fieldName": "debrisFlows",
                 "label": "Debris Flow Info:",
                 "choices": [
                        self.debrisBurnScar(),
                        self.debrisMudSlide(),
                        ]
                }        
    def debrisBurnScar(self):
        return {"identifier":"burnScar", "displayString": "Burn scar area with debris flow", 
                "productString": 
                '''Excessive rainfall over the burn scar will result in debris flow moving
                through the !** DRAINAGE **!. The debris flow can consist of 
                rock...mud...vegetation and other loose materials.''',}
    def debrisMudSlide(self):
        return {"identifier":"mudSlide", "displayString": "Mud Slides", 
                "productString": 
                '''Excessive rainfall over the warning area will cause mud slides near steep
                terrain. The mud slide can consist of rock...mud...vegetation and other
                loose materials.''',}

    # ADDITIONAL INFORMATION
    def additionalInfoChoices(self):
        return [ 
                self.listOfCities(),
                self.listOfDrainages(),
                self.additionalRain(),
                self.particularStream(),
                self.recedingWater(),
                self.rainEnded(),
                ]

    # CALLS TO ACTION
    def getCTA_Choices(self):
        return [
            self.ctaNoCTA(),
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

        
