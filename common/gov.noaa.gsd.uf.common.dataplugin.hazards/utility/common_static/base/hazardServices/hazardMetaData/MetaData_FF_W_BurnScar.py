import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        metaData = [
                     self.getInclude(),
                     self.getFloodSeverity(),
                     self.getHydrologicCause(),
                     self.getBasis(),                                        
                     self.getAdditionalInfo(),
                     self.getCTAs(), 
                     self.getDamOrLevee(),
                     self.getScenario(),
                     self.getRiver(),
                     self.getFloodLocation(),
                     self.getUpstreamLocation(),
                     self.getDownstreamLocation(),
                     self.getVolcano(),                  
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
       

    # INCLUDE  -- include
    #
    #<bullet bulletName="ffwEmergency" bulletText="**SELECT FOR FLASH FLOOD EMERGENCY**" bulletGroup="ffwEMER" floodSeverity="3" parseString="FLASH FLOOD EMERGENCY"/>
    #
    def includeChoices(self):
        return [
            self.includeEmergency(),
            ]      
        
    # FLOOD SEVERITY -- floodSeverity
    #
    # <bullet bulletText="******** FLOOD SEVERITY (choose 1) *******" bulletType="title"/>
    # <bullet bulletName="sevUnk" bulletText="Unknown" bulletDefault="true" bulletGroup="floodSeverity" floodSeverity="U"/>
    # <bullet bulletName="sev1" bulletText="Minor flood" bulletGroup="floodSeverity" floodSeverity="1"/>
    # <bullet bulletName="sev2" bulletText="Moderate flood" bulletGroup="floodSeverity" floodSeverity="2"/>
    # <bullet bulletName="sev3" bulletText="Major flood" bulletGroup="floodSeverity" floodSeverity="3" parseString="FLASH FLOOD EMERGENCY"/>
    #
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

    # HYDROLOGIC CAUSE -- hydrologicCause
    #    
    #  nonConvectiveFlashFloodWarning.xml
    #
    # <bullet bulletText="******** PRIMARY CAUSE (choose 1) *******" bulletType="title"/>
    # <bullet bulletName="dam" bulletText="Dam failure - generic" bulletGroup="ic" bulletDefault="true" parseString="A DAM FAILURE" showString=""DAM",".DM.","-LEVEE""/>
    # <bullet bulletName="siteimminent" bulletText="Dam break - site specific (pick below) - imminent failure" bulletGroup="ic" parseString="THE IMMINENT FAILURE OF" showString=""DAM",".DM.","-LEVEE""/>
    # <bullet bulletName="sitefailed" bulletText="Dam break - site specific (pick below) - failure has occurred" bulletGroup="ic" parseString="THE FAILURE OF" showString=""DAM",".DM.","-LEVEE""/>
    # <bullet bulletName="levee" bulletText="Levee failure" bulletGroup="ic" parseString="A LEVEE FAILURE" showString="LEVEE FAILURE"/>
    # <bullet bulletName="floodgate" bulletText="Floodgate opening" bulletGroup="ic" parseString="FLOODGATE RELEASE" showString="A DAM FLOODGATE RELEASE"/>
    # <bullet bulletName="glacier" bulletText="Glacier-dammed lake outburst" bulletGroup="ic" parseString=".GO." showString=".GO."/>
    # <bullet bulletName="icejam" bulletText="Ice jam" bulletGroup="ic" parseString=".IJ." showString=".IJ."/>
    # <bullet bulletName="rain" bulletText="Rapid snowmelt (with or without rain)" bulletGroup="ic" parseString=".RS." showString=".RS."/>
    # <!--  modified by GP  -->
    # <bullet bulletName="volcano" bulletText="Volcano induced snowmelt" bulletGroup="ic" parseString="VOLCANIC INDUCED SNOWMELT" showString=".SM."/>
    # <bullet bulletName="volcanoLahar" bulletText="Volcano induced lahar/debris flow" bulletGroup="ic" parseString="VOLCANIC INDUCED DEBRIS FLOW" showString=".SM."/>
    # <!--  GP end  -->
    #
    #
    # nonConvectiveFlashFloodWarning.vm
    #     
    # #set($emergencyHeadline = "!** ENTER LOCATION **!")
    # #if(${list.contains(${bullets}, "levee")})
    #     #set($ic = "DM")
    #     #set($hycType = "A LEVEE FAILURE")
    #     #set($reportType1 = "A LEVEE ON THE !** **! RIVER AT !** **! FAILED CAUSING FLASH FLOODING OF IMMEDIATELY SURROUNDING AREAS")
    # #elseif(${list.contains(${bullets}, "floodgate")})
    #     #set($ic = "DR")
    #     #set($hycType = "A DAM FLOODGATE RELEASE")
    #     #set($reportType1 = "THE FLOODGATES ON THE !** **! DAM WERE OPENED CAUSING FLASH FLOODING DOWNSTREAM ON THE !** **! RIVER")
    # #elseif(${list.contains(${bullets}, "glacier")})
    #     #set($ic = "GO")
    # ### modified by GP
    #     #set($hycType = "A GLACIER-DAMMED LAKE OUTBURST")
    #     #set($ctaSelected = "YES")
    #     #set($reportType1 = "A GLACIER-DAMMED LAKE AT !** **! IS RAPIDLY RELEASING LARGE QUANTITIES OF IMPOUNDED WATER RESULTING IN FLASH FLOODING !** **!")
    #     #set($glacierCTA = "STAY AWAY FROM IMPACTED WATERWAYS. WATER LEVELS CAN RISE VERY RAPIDLY EVEN IN DRY WEATHER. VERY COLD GLACIAL MELT WATER INCREASES THE RISK FOR HYPOTHERMIA.")
    # ### GP end
    # #elseif(${list.contains(${bullets}, "icejam")})
    #     #set($ic = "IJ")
    #     #set($hycType = "AN ICE JAM")
    #     #set($reportType1 = "AN ICE JAM ON THE !** **! RIVER AT !** **! BROKE CAUSING FLASH FLOODING DOWNSTREAM")
    # #elseif(${list.contains(${bullets}, "rain")})
    #     #set($ic = "RS")
    #     #set($hycType = "EXTREMELY RAPID SNOWMELT")
    #     #set($reportType1 = "EXTREMELY RAPID SNOWMELT !** COMBINED WITH HEAVY RAIN **! IS GENERATING FLASH FLOODING")
    # #elseif(${list.contains(${bullets}, "volcano")})
    #     #set($ic = "SM")
    # ### modified by GP    
    #     #set($hycType = "EXTREMELY RAPID SNOWMELT CAUSED BY VOLCANIC ERUPTION")
    #     #set($ctaSelected = "YES")
    #     #set($reportType1 = "ACTIVITY OF THE !** **! VOLCANO WAS CAUSING RAPID SNOWMELT ON ITS SLOPES AND GENERATING FLASH FLOODING")
    #     #set($volcanoCTA = "PERSONS IN THE VICINITY OF !** DRAINAGE **! SHOULD HEAD TO HIGHER GROUND IMMEDIATELY. FLOODS DUE TO VOLCANO INDUCED SNOWMELT CAN OCCUR VERY RAPIDLY AND IMPACT AREAS WELL AWAY FROM NORMAL WATERWAY CHANNELS.")
    # ### end GP
    # #elseif(${list.contains(${bullets}, "volcanoLahar")})
    #     #set($ic = "SM")
    # ### modified by GP    
    #     #set($hycType = "VOLCANIC INDUCED DEBRIS FLOW")
    #     #set($ctaSelected = "YES")
    #     #set($reportType1 = "ACTIVITY OF THE !** **! VOLCANO WAS CAUSING RAPID MELTING OF SNOW AND ICE ON THE MOUNTAIN. THIS WILL RESULT IN A TORRENT OF MUD...ASH...ROCK AND HOT WATER TO FLOW DOWN THE MOUNTAIN THROUGH !** DRAINAGE **! AND GENERATE FLASH FLOODING")
    #     #set($volcanoCTA = "PERSONS IN THE VICINITY OF !** DRAINAGE **! SHOULD HEAD TO HIGHER GROUND IMMEDIATELY. VOLCANIC DEBRIS FLOWS ARE EXTREMELY DANGEROUS. VOLCANIC DEBRIS FLOWS CAN IMPACT AREAS WELL AWAY FROM NORMAL WATERWAY CHANNELS.")
    # ### end GP
    # #elseif(${list.contains(${bullets}, "dam")})
    #     #set($ic = "DM")
    #     #set($hycType = "A DAM FAILURE")
    #     #set($reportType1 = "THE !** **! DAM FAILED CAUSING FLASH FLOODING DOWNSTREAM ON THE !** **! RIVER")
    #     #set($addInfo = "!** **! DAM ON THE !** **! RIVER UPSTREAM FROM !** **! HAS GIVEN WAY AND HIGH WATERS ARE NOW MOVING TOWARD !** **!. AREAS DOWNSTREAM FROM THE DAM ALONG THE !** **! RIVER SHOULD BE PREPARED FOR FLOODING. TAKE NECESSARY PRECAUTIONS IMMEDIATELY")
    # #elseif(${list.contains(${bullets}, "siteimminent")})
    #     #set($ic = "DM")
    #     #set($hycType = "A DAM BREAK")
    #     #set($reportType1 = "THE IMMINENT FAILURE OF !** **! DAM")
    #     #set($reportType2 = "THE IMMINENT FAILURE OF")
    #     #set($addInfo = "!** **! DAM ON THE !** **! RIVER UPSTREAM FROM !** **! HAS GIVEN WAY AND HIGH WATERS ARE NOW MOVING TOWARD !** **!. AREAS DOWNSTREAM FROM THE DAM ALONG THE !** **! RIVER SHOULD BE PREPARED FOR FLOODING. TAKE NECESSARY PRECAUTIONS IMMEDIATELY")
    # #elseif(${list.contains(${bullets}, "sitefailed")})
    #     #set($ic = "DM")
    #     #set($hycType = "A DAM BREAK")
    #     #set($reportType1 = "THE FAILURE OF !** **! DAM")
    #     #set($reportType2 = "THE FAILURE OF")
    #     #set($addInfo = "!** **! DAM ON THE !** **! RIVER UPSTREAM FROM !** **! HAS GIVEN WAY AND HIGH WATERS ARE NOW MOVING TOWARD !** **!. AREAS DOWNSTREAM FROM THE DAM ALONG THE !** **! RIVER SHOULD BE PREPARED FOR FLOODING. TAKE NECESSARY PRECAUTIONS IMMEDIATELY")
    # #else
    #     #set($ic = "ER")
    #     #set($hycType = "EXCESSIVE RAIN")
    #     #set($reportType1 = "EXCESSIVE RAIN CAUSING FLASH FLOODING WAS OCCURING OVER THE WARNED AREA")
    # #end
    
    def getHydrologicCause(self):
        return {   
            # The immediate cause will be automatically assigned based on the hydrologic cause chosen.  
             "fieldType":"RadioButtons",
             "fieldName": "hydrologicCause",
             "label":"Hydrologic Cause:",
             "values": "dam",
             "choices": self.hydrologicCauseChoices(),
             }
    def hydrologicCauseChoices(self):
        return [
            self.hydrologicCauseDam(),
            self.hydrologicCauseSiteImminent(),
            self.hydrologicCauseSiteFailed(),
            self.hydrologicCauseLevee(),           
            self.hydrologicCauseFloodGate(),
            self.hydrologicCauseGlacialOutburst(),
            self.hydrologicCauseIceJam(),
            self.hydrologicCauseSnowMelt(),            
            self.hydrologicCauseVolcano(),
            self.hydrologicCauseVolcanoLahar(),
            ]
    def hydrologicCauseDam(self):
        return {"identifier":"dam", "displayString":"Dam failure - generic", 
                "productString":"The #damOrLeveeName# dam failed causing Flash Flooding downstream on the #RiverName# River"}
    def hydrologicCauseSiteImminent(self):
        return {"identifier":"siteImminent", "displayString":"Dam break - site specific - imminent failure", 
                "productString":"The imminent failure of #damOrLeveeName# Dam"}
        
                #damOrLeveeName# Dam on the #riverName# River upstream from #upstreamLocation# has given way and high waters are
                #now moving toward #downstreamLocation#. Areas downstream from the dam along the #riverName# should be prepared for flooding.  
                #Take necessary precautions immediately
                
    def hydrologicCauseSiteFailed(self):
        return {"identifier":"siteFailed", "displayString":"Dam - site specific - failure has occurred", 
                "productString":"The failure of #damOrLeveeName# Dam"}
    def hydrologicCauseLevee(self):
        return {"identifier":"levee", "displayString":"Levee failure", 
                "productString":"A levee on the #riverName# at #floodLocation# failed causing Flash Flooding"}
    def hydrologicCauseFloodGate(self):
        return {"identifier":"floodgate",  "displayString":"Floodgate opening", 
                "productString":"The flood gates on the #damOrLeveeName# Dam were opened causing Flash Flooding downstream on the #riverName# River"}
    def hydrologicCauseGlacialOutburst(self):
        return {"identifier":"glacier", "displayString":"Glacier-dammed lake outburst", 
                "productString":'''A glacier-dammed lake at #floodLocation# is rapidly releasing large quantities of impounded
                                  water resulting in Flash Flooding'''}
    def hydrologicCauseIceJam(self):
        return {"identifier":"icejam", "displayString":"Ice jam", 
                "productString":"An ice jam on the #riverName# River at #upstreamLocation# broke causing Flash Flooding downstream"}
    def hydrologicCauseSnowMelt(self):
        return {"identifier":"snowMelt", "displayString":"Rapid snowmelt (with or without rain)", 
                "productString":"Extremely rapid snowmelt |* combined with heavy rain *| is generating flash flooding"}
    def hydrologicCauseVolcano(self):
        return {"identifier":"volcano", "displayString":"Volcano induced snowmelt", 
                "productString":'''Activity of the #volcanoName# Volcano was causing rapid snowmelt on its slopes and 
                generating flash flooding.'''}
    def hydrologicCauseVolcanoLahar(self):
        return {"identifier":"volcanoLahar", "displayString":"Volcano induced lahar/debris flow", 
                "productString":'''Activity of the #volcanoName# was causing rapid melting of snow and ice on the mountain.  T
                This will result in a torrent of mud...ash...rock and hot water to flow down the mountain through #downstreamLocation# 
                and generate Flash Flooding'''}

    
    # BASIS -- basis
    #
    #  nonConvectiveFlashFloodWarning.xml
    #
    # <bullet bulletText="****** REPORTED BY (choose 1) ******" bulletType="title"/>
    # <bullet bulletName="county" bulletText="County dispatch" bulletGroup="reportedBy" bulletDefault="true" parseString="COUNTY DISPATCH REPORTED"/>
    # <bullet bulletName="lawEnforcement" bulletText="Law enforcement" bulletGroup="reportedBy" parseString="LOCAL LAW ENFORCEMENT REPORTED"/>
    # <bullet bulletName="corps" bulletText="Corps of engineers" bulletGroup="reportedBy" parseString="CORPS OF ENGINEERS REPORTED"/>
    # <bullet bulletName="damop" bulletText="Dam operator" bulletGroup="reportedBy" parseString="DAM OPERATORS REPORTED"/>
    # <bullet bulletName="bureau" bulletText="Bureau of reclamation" bulletGroup="reportedBy" parseString="BUREAU OF RECLAMATION REPORTED"/>
    # <bullet bulletName="public" bulletText="Public" bulletGroup="reportedBy" parseString="THE PUBLIC REPORTED"/>
    # <!--  added by GP  -->
    # <bullet bulletName="onlyGauge" bulletText="Gauge reports" bulletGroup="source" parseString="GAUGE REPORTS "/>
    # <bullet bulletName="CAP" bulletText="Civil Air Patrol" bulletGroup="source" parseString="CIVIL AIR PATROL "/>
    # <bullet bulletName="alaskaVoc" bulletText="Alaska Volcano Observatory" bulletGroup="source" parseString="ALASKA VOLCANO OBSERVATORY "/>
    # <bullet bulletName="cascadeVoc" bulletText="Cascades Volcano Observatory" bulletGroup="source" parseString="CASCADES VOLCANO OBSERVATORY "/>
    # <!--   GP end  -->
    #    
    def basisChoices(self):
        return [  
            self.basisCountyDispatch(),
            self.basisLawEnforcement(),
            self.basisCorpsOfEngineers(),
            self.basisDamOperator(),
            self.basisBureauOfReclamation(),            
            self.basisPublic(),
            self.basisGauges(),
            self.basisCivilAirPatrol(),
            self.basisAlaskaVolcanoObservatory(),
            self.basisCascadesVolcanoObservatory(),
            ]  


    # ADDITIONAL INFORMATION -- 'additionalChoices'
    def additionalInfoChoices(self):
        ### FIXME: Not sure why this logic is here. Can't find
        ### and mention of 'previewState' in all of HazSvcs (klm 20140723)
        #=======================================================================
        # if self.previewState == "ENDED":
        #     return [ 
        #         self.recedingWater(),
        #         self.rainEnded(),
        #         ]
        # else:
        #     return [ 
        #         self.listOfCities(),
        #         self.listOfDrainages(),
        #         self.floodMoving(),
        #         ]
        #=======================================================================
        return [ 
                self.listOfCities(),
                self.listOfDrainages(),
                self.floodMoving(),
                ]

    
    # DAM OR LEVEE and SCENARIO -- damOrLevee  scenario
    #
    #  nonConvectiveFlashFloodWarning.xml
    #
    # <damInfoBullets>
    # <damInfoBullet bulletGroup="dam" bulletText="Big Rock Dam (Fairfield County)" bulletName="BigRockDam"  parseString="BIG ROCK"  coords="LAT...LON 4109 7338 4116 7311 4116 7320"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - high fast" bulletName="BigRockhighfast"  parseString="COMPLETE FAILURE OF BIG ROCK"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - high normal" bulletName="BigRockhighnormal"  parseString="COMPLETE FAILURE OF BIG ROCK"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - medium fast" bulletName="BigRockmediumfast"  parseString="COMPLETE FAILURE OF BIG ROCK"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - medium normal" bulletName="BigRockmediumnormal"  parseString="COMPLETE FAILURE OF BIG ROCK"/>
    # <damInfoBullet bulletGroup="ruleofthumb" bulletText="rule of thumb" bulletName="BigRockruleofthumb"  parseString="FLOOD WAVE ESTIMATE"/>
    # <damInfoBullet bulletGroup="dam" bulletText="Branched Oak Dam (Westchester County)" bulletName="BranchedOakDam"  parseString="BRANCHED OAK"  coords="LAT...LON 4106 7373 4097 7366 4090 7376 4102 7382"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - high fast" bulletName="BranchedOakhighfast"  parseString="COMPLETE FAILURE OF BRANCHED OAK"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - high normal" bulletName="BranchedOakhighnormal"  parseString="COMPLETE FAILURE OF BRANCHED OAK"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - medium fast" bulletName="BranchedOakmediumfast"  parseString="COMPLETE FAILURE OF BRANCHED OAK"/>
    # <damInfoBullet bulletGroup="scenario" bulletText="scenario - medium normal" bulletName="BranchedOakmediumnormal"  parseString="COMPLETE FAILURE OF BRANCHED OAK"/>
    # <damInfoBullet bulletGroup="ruleofthumb" bulletText="rule of thumb" bulletName="BranchedOakruleofthumb"  parseString="FLOOD WAVE ESTIMATE"/>
    # </damInfoBullets> 
    #            
    def getDamOrLevee(self):
        #damOrLeveeName = self.hazardEvent.get('damName')
        #if damOrLeveeName is None:
        damOrLeveeName = 'Branched Oak Dam'            
        return {
            "fieldName": "damOrLeveeName",
            "fieldType":"RadioButtons",
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
            "fieldType":"RadioButtons",
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
       

    def getRiver(self):
        return {
             "fieldType": "Text",
             "fieldName": "riverName",
             "expandHorizontally": True,
             "maxChars": 40,
             "visibleChars": 12,
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

    # CALLS TO ACTION -- 'cta'
    def getCTA_Choices(self):
        return [
            #self.ctaNoCTA(),
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
        if self.hazardStatus == "PENDING":
           return ["WEA_activated"]
        else:
           return [] 
       
    def CAP_WEA_Text(self):
        return "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"
    
