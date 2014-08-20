import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        if self.hazardStatus == "ending":
                metaData = [
                            self.getEndingSynopsis(), 
                    ]
        else:
            metaData = [
                    self.getImmediateCause(),
                    self.getInclude(),
                    self.getEventType(),
                    self.getRainAmt(),
                    self.getBasis(),
                    self.getDebrisFlowOptions(),
                    self.getAdditionalInfo(),
                    self.getCTAs(),                    
                    self.getCAP_Fields([
                                        ("urgency", "Immediate"),
                                        ("severity", "Severe"),
                                        ("certainty", "Likely"),
                                        ("responseType", "Avoid"),
                                       ])
                    ]
        return {
                METADATA_KEY: metaData
                }    
        
    # INCLUDE  
    def includeChoices(self):
        return [
            self.includeEmergency(),
            self.includeSnowMelt(),
            self.includeFlooding(),
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
        if self.hazardStatus == "ending":
            return [ 
                self.recedingWater(),
                self.rainEnded(),
                ]
        else:
            return [ 
                self.listOfCities(),
                self.listOfDrainages(),
                self.additionalRain(),
                self.floodMoving(),
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

    def CAP_WEA_Values(self):
        if self.hazardStatus == "pending":
                return ["WEA_activated"] 
        else:
            return []
       
    def CAP_WEA_Text(self):
        return "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"
    
