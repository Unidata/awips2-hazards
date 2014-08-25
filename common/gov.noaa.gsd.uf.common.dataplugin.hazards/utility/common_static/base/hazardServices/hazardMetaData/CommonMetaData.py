"""
    Description: Common Meta Data shared among hazard types.
    TODO -- Organize this file to be easy to follow and work with 
    This will encourage best practices...
        
"""

import VTECConstants
from LocalizationInterface import LocalizationInterface

class MetaData:
    
    def initialize(self, hazardEvent, metaDict):    
        self.hazardEvent = hazardEvent
        self.metaDict = metaDict
        if self.hazardEvent:
            self.hazardStatus = self.hazardEvent.getStatus().lower()
        else:
            self.hazardStatus = "pending"
            
    # INTERDEPENDENCIES SCRIPT FROM A SEPARATE FILE
    def getInterdependenciesScriptFromLocalizedFile(self, scriptFileName):
        localizationInterface = LocalizationInterface()
        return localizationInterface.getLocFile("hazardServices/megawidgetSideEffects/" + scriptFileName, "common")
    
    # POINT ID
    def getPointID(self):
        return {
             "fieldName": "pointID",
             "fieldType": "Text",
             "label": "Forecast Point:",
             "maxChars": 5,
             #"values": "XXXXX"
            }
        
    # IMMEDIATE CAUSE
    def getImmediateCause(self):
        return {
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "ER",
            "expandHorizontally": True,
            "choices": [
                self.immediateCauseER(),
                self.immediateCauseSM(),
                self.immediateCauseRS(),
                self.immediateCauseDM(),
                self.immediateCauseDR(),
                self.immediateCauseGO(),
                self.immediateCauseIJ(),
                self.immediateCauseIC(),
                self.immediateCauseFS(),
                self.immediateCauseFT(),
                self.immediateCauseET(),
                self.immediateCauseWT(),
                self.immediateCauseOT(),
                self.immediateCauseMC(),
                self.immediateCauseUU(),
                ]
                }
    def immediateCauseER(self):
        return {"identifier":"ER", "productString":"ER", "displayString":"ER (Excessive Rainfall)"}
    def immediateCauseSM(self):
        return {"identifier":"SM", "productString":"SM", "displayString":"SM (Snow Melt)"}
    def immediateCauseRS(self):
        return {"identifier":"RS", "productString":"RS", "displayString":"RS (Rain and Snow Melt)"}
    def immediateCauseDM(self):
        return {"identifier":"DM", "productString":"DM", "displayString":"DM (Dam or Levee Failure)"}
    def immediateCauseDR(self):
        return {"identifier":"DR", "productString":"DR", "displayString":"DR (Upstream Dam Release)"}
    def immediateCauseGO(self):
        return {"identifier":"GO", "productString":"GO", "displayString":"GO (Glacier-Dammed Lake Outburst)"}
    def immediateCauseIJ(self):
        return {"identifier":"IJ", "productString":"IJ", "displayString":"IJ (Ice Jam)"}
    def immediateCauseIC(self):
        return {"identifier":"IC", "productString":"IC", "displayString":"IC (Rain and/or Snow melt and/or Ice Jam)"}
    def immediateCauseFS(self):
        return {"identifier":"FS", "productString":"FS", "displayString":"FS (Upstream Flooding plus Storm Surge)"}
    def immediateCauseFT(self):
        return {"identifier":"FT", "productString":"FT", "displayString":"FT (Upstream Flooding plus Tidal Effects)"}
    def immediateCauseET(self):
        return {"identifier":"ET", "productString":"ET", "displayString":"ET (Elevated Upstream Flow plus Tidal Effects)"}
    def immediateCauseWT(self):
        return {"identifier":"WT", "productString":"WT", "displayString":"WT (Wind and/or Tidal Effects)"}
    def immediateCauseOT(self):
        return {"identifier":"OT", "productString":"OT", "displayString":"OT (Other Effects)"}
    def immediateCauseMC(self):
        return {"identifier":"MC", "productString":"MC", "displayString":"MC (Other Multiple Causes)"}
    def immediateCauseUU(self):
        return {"identifier":"UU", "productString":"UU", "displayString":"UU (Unknown)"}
     
 
    # HYDROLOGIC CAUSE 
    def getHydrologicCause(self):
        return {   
            # The immediate cause will be automatically assigned based on the hydrologic cause chosen.  
             "fieldType":"RadioButtons",
             "fieldName": "hydrologicCause",
             "label":"Hydrologic Cause:",
             "values": "snowMelt",
             "choices": self.hydrologicCauseChoices(),
             }
    def hydrologicCauseChoices(self):
        return [
            self.hydrologicCauseSnowMelt(),
            self.hydrologicCauseRainSnow(),
            self.hydrologicCauseIceJam(),
            self.hydrologicCauseFloodGate(),
            self.hydrologicCauseGlacialOutburst(),
            self.hydrologicCauseGroundWater(),
            self.hydrologicCauseRiverRises(),
            self.hydrologicCauseBadDrainage(),
            ]
    def hydrologicCauseSnowMelt(self):
        return {"identifier":"snowMelt", "displayString":"Melting snow", "productString":"Melting snow"}
    def hydrologicCauseRainSnow(self):
        return {"identifier":"rainSnow", "displayString":"Rain and melting snow", "productString":"Rain and melting snow"}
    def hydrologicCauseIceJam(self):
        return {"identifier":"iceJam", "displayString":"Ice Jam Flooding", "productString":"Ice Jam Flooding"}
    def hydrologicCauseFloodGate(self):
        return {"identifier":"floodGate",  "displayString":"Floodgate Release", "productString":"A dam floodgate release"}
    def hydrologicCauseGlacialOutburst(self):
        return {"identifier":"glacialOutburst", "displayString":"Glacier-dammed lake outburst", "productString":"A glacier-dammed lake outburst"}
    def hydrologicCauseGroundWater(self):
        return {"identifier":"groundWater", "displayString":"Ground Water Flooding", "productString":"Ground water flooding"}
    def hydrologicCauseRiverRises(self):
        return {"identifier":"riverRises", "displayString":"Rapid River Rises", "productString":"Rapid river rises"}
    def hydrologicCauseBadDrainage(self):
        return {"identifier":"badDrainage", "displayString":"Poor Drainage Areas", "productString":"Minor flooding of poor drainage areas"}
 
    # FLOOD SEVERITY
    def getFloodSeverity(self):
        return {
             "fieldName": "floodSeverity",
             "fieldType":"ComboBox",
             "label":"Flood Severity:",
             "expandHorizontally": True,
             "values": "U",
             "choices": self.floodSeverityChoices()
            }        
    def floodSeverityChoices(self):
        return [
            self.floodSeverityNone(),
            self.floodSeverityArealOrFlash(),
            self.floodSeverityMinor(),
            self.floodSeverityModerate(),
            self.floodSeverityMajor(),
            self.floodSeverityUnknown()
        ]        
    def floodSeverityNone(self):
            return {"displayString": "N (None)","identifier": "N","productString": ""}
    def floodSeverityArealOrFlash(self):
        return {"displayString": "0 (Areal Flood or Flash Flood Products)","identifier": "0","productString": ""}
    def floodSeverityMinor(self):
        return {"displayString": "1 (Minor)","identifier": "1", "productString": "Minor"}
    def floodSeverityModerate(self):
        return {"displayString": "2 (Moderate)","identifier": "2","productString": "Moderate"}
    def floodSeverityMajor(self):
        return {"displayString": "3 (Major)","identifier": "3", "productString": "Major"}
    def floodSeverityUnknown(self):
        return {"displayString": "U (Unknown)","identifier": "U","productString": ""}



    # FLOOD RECORD    
    def getFloodRecord(self):
        return {
             "fieldName":"floodRecord",
             "fieldType":"ComboBox",
             "label":"Flood Record Status:",
             "expandHorizontally": True,
             "choices":[
                      {"displayString": "NO (Record Flood Not Expected)","identifier": "NO"},
                      {"displayString": "NR (Near Record or Record Flood Expected)","identifier": "NR"},
                      {"displayString": "UU (Flood Without a Period of Record to Compare)","identifier": "UU"},
                      {"displayString": "OO (for areal flood warnings, areal flash flood products, and flood advisories (point and areal))","identifier": "OO"},
                     ],
            }
         
    # RISE CREST FALL
    def getRiseCrestFall(self):
        return {
            "fieldName":"riseAbove:crest:fallBelow",
            "fieldType":"TimeScale",
            "valueLabels": {"riseAbove": "Rise Above Time:","crest": "Crest Time:","fallBelow": "Fall Below Time:"},
            "minimumTimeInterval": 60000,
            "spacing": 5,
            "timeDescriptors": {
                str(int(VTECConstants.UFN_TIME_VALUE_SECS) * 1000): "N/A"
               },
            "detailFields": {
                "fallBelow": [
                        {
                         "fieldName": "fallBelowUntilFurtherNotice",
                         "fieldType": "CheckBox",
                         "label": "Until further notice"
                        }]
               }
           }
   
    # INCLUDE
    def getInclude(self):
        return {
             "fieldType":"CheckBoxes",
             "fieldName": "include",
             "label": "Include:",
             "choices": self.includeChoices()
             }        
    def includeEmergency(self):
        return { 
                "identifier": "ffwEmergency",
                "displayString": "**SELECT FOR FLASH FLOOD EMERGENCY**",
                "productString": "...Flash flood emergency for #includeEmergencyLocation#...",
                "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "includeEmergencyLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12,
                             #"values": "|* Enter Location *|"
                             "values": "Enter Location"
                            }]
 
                }            
    def includeSnowMelt(self):
        return {
                "identifier":"+SM", "displayString":"Also Snow Melt",
                "productString":"Rapid snow melt is causing flooding."
                }
    def includeFlooding(self):
        return  {
                 "identifier":"-FL", "displayString": "Flooding not directly reported, only heavy rain",
                 "productString": "Flooding is not directly reported, only heavy rains.",
                 }                
    def includeSmallStreams(self):
        return {"identifier":"+SS","displayString": "Small streams","productString":"Small Stream Flooding "
                 }
    def includeUrbanAreas(self):
        return {"identifier":"+US",
                  "displayString": "Urban areas and small streams",
                  "productString":"Urban and Small Stream "
                  }
    def includeUrbanAreasSmallStreams(self):
        return  {"identifier":"+US","displayString": "Urban areas and small streams",
                 "productString":"Urban and Small Stream Flooding "
                 }
    def includeArroyoSmallStreams(self):
        return {"identifier":"+AS","displayString": "Arroyo and small streams",
                  "productString":"Arroyo and Small Stream Flooding "
                 }
    def includeHydrologic(self):
        return {"identifier":"+HA", "displayString": "Hydrologic Flooding",
                  "productString":"Minor Flooding "
                  }
 
                 
    # EVENT TYPE    
    def getEventType(self):
        return {
                 "fieldType":"RadioButtons",
                 "fieldName": "eventType",
                 "label": "Event type (Choose 1):",
                 "values": "thunderEvent",
                 "choices": [
                        self.eventTypeThunder(),
                        self.eventTypeRain(),
                        self.eventTypeUndef(),
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
                    self.rain_amount_unknown(),
                    self.one_inch(),
                    self.two_inches(),
                    self.three_inches(),
                    self.enterAmount(),
                    ]
                }
        
    def rain_amount_unknown(self):
        return {"identifier":"rainNull", "displayString":"Unknown",
                "productString":"",}


    def one_inch(self):
        return {"identifier":"rain1", "displayString":"One inch so far",
                "productString":"up to one inch of rain has already fallen.",}
    def two_inches(self):
         return {"identifier":"rain2", "displayString":"Two inches so far",
                 "productString":"up to two inches of rain has already fallen.",}
    def three_inches(self):
        return  {"identifier":"rain3", "displayString":"Three inches so far",
                 "productString":"up to three inches of rain has already fallen.",}
    def enterAmount(self):                
        return  {"identifier":"rainEdit", "displayString":"",
                 "productString":"#rainAmtRainEditInches# inches of rain have fallen.",
                 "detailFields": [
                       {
                        "fieldType": "FractionSpinner",
                        "fieldName": "rainAmtRainEditInches",
                        "minValue": 0,
                        "maxValue": 99,
                        "incrementDelta": 1,
                        "precision": 1
                       },
                       {
                        "fieldType": "Label",
                        "fieldName": "rainAmtRainEditLabel",
                        "label": "inches of rain have fallen"
                       }
                 ]
                }
 
    # BASIS
    def getBasis(self):
        return {
            "fieldName": "basis",
            "fieldType":"RadioButtons",
            "label":"Basis:",
            "values": self.basisDefaultValue(),
            "choices": self.basisChoices(),
            } 
    # Takes the first one listed as the default
    #  This can be overridden by listing a specific default
    def basisDefaultValue(self):
        for choice in self.basisChoices():
            return choice.get('identifier')       
    def basisCountyDispatch(self):
        return {"identifier":"county", 
                "displayString": "County dispatch",
                "productString": "County dispatch reported flooding in #floodLocation#"}
    def basisCorpsOfEngineers(self):
        return {"identifier":"corpsOfEngineers", 
                "displayString": "Corps of engineers",
                "productString": "Corps of engineers reported flooding in #floodLocation#"}
    def basisDamOperator(self):
        return {"identifier":"damOperator", 
                "displayString": "Dam operator",
                "productString": "Dam operators reported flooding in #floodLocation#"}
    def basisBureauOfReclamation(self):
        return {"identifier":"bureauOfReclamation", 
                "displayString": "Bureau of reclamation",
                "productString": "The Bureau of Reclamation reported flooding in #floodLocation#"}
    def basisCivilAirPatrol(self):
        return {"identifier":"civilAirPatrol", 
                "displayString": "Civil Air Patrol",
                "productString": "The Civil Air Patrol reported flooding in #floodLocation#"}
    def basisAlaskaVolcanoObservatory(self):
        return {"identifier":"alaskaVolcano", 
                "displayString": "Alaska Volcano Observatory",
                "productString": "The Alaska Volcano Observatory reported flooding in #floodLocation#"}
    def basisCascadesVolcanoObservatory(self):
        return {"identifier":"cascadesVolcano", 
                "displayString": "Cascades Volcano Observatory",
                "productString": "The Cascades Volcano Observatory reported flooding in #floodLocation#"}
    def basisDoppler(self):
        return {"identifier":"radInd", 
                "displayString": "Doppler Radar indicated...", 
                "productString": "Doppler Radar indicated"}
    def basisDopplerThunderstorm(self):
        return {"identifier":"radarTS", 
                "displayString": "Radar indicated thunderstorm...", 
                "productString": '''Doppler radar indicated thunderstorms producing heavy
                                    rain which will cause flooding.'''}
    def basisDopplerGauges(self):
        return {"identifier":"radGagInd", 
                "displayString": "Doppler Radar and automated gauges", 
                "productString": '''Doppler Radar and automated rain gauges indicated heavy rain
                                was falling over the area. That heavy rain will cause flooding.'''}
    def basisDopplerGaugesThunderstorm(self):
        return {"identifier":"radGagTS", 
                "displayString": "Radar and gauges with thunderstorm", 
                "productString": '''Doppler radar and automated rain gauges indicated thunderstorms
                        with heavy rain over the area. That rain will cause flooding.'''}
    def basisGauges(self):
        return {"identifier":"radGagInd", 
                "displayString": "Automated gauges", 
                "productString": '''Automated rain gauges indicated heavy rainwas falling over the area.'''}
    def basisSpotter(self):
        return {"identifier":"wxSpot", 
                "displayString": "Weather spotters report flooding in", 
                "productString": "Trained weather spotters reported flooding in #floodLocation#"}
    def basisSpotterHeavyRain(self):
        return {"identifier":"wxSpotHR", 
                "displayString": "Weather spotters report heavy rain in", 
                "productString": '''Trained weather spotters reported heavy rain in #floodLocation# 
                                          that will cause flooding.'''}
    def basisSpotterThunderstorm(self):
        return {"identifier":"wxSpotTS", 
                "displayString": "Weather spotters report thunderstorm in", 
                "productString": '''Trained weather spotters reported heavy rain in #floodLocation# 
                                          due to thunderstorms that will cause flooding'''}
    def basisSpotterIncipientFlooding(self):
        return {"identifier":"wxSpotInc",
                "displayString": "Weather spotters report incipient flooding in", 
                "productString": '''Trained weather spotters reported !**HYDROLOGIC CAUSE**! in #floodLocation# 
                                       that will cause !**ADV TYPE**!'''}
    def basisLawEnforcement(self):
        return {"identifier":"lawEnf", 
                "displayString": "Law enforcement report flooding in",
                "productString": "Local law enforcement reported flooding in #floodLocation#"}
    def basisLawEnforcementHeavyRain(self):
        return {"identifier":"lawEnfHR", 
                "displayString": "Law enforcement report heavy rain in",
                "productString": '''Local law enforcement reported heavy rain in #floodLocation#
                                    that will cause flooding'''}
    def basisLawEnforcementThunderstorm(self):
        return {"identifier":"lawEnfTS", 
                "displayString": "Law enforcement report thunderstorm in",
                "productString": '''Local law enforcement reported thunderstorms with heavy rain
                                          over #floodLocation# that will cause flooding'''}
    def basisLawEnforcementIncipientFlooding(self):
        return {"identifier":"lawEnfInc", 
                "displayString": "Law enforcement report incipient flooding in",
                "productString": '''Local law enforcement reported !**HYROLOGIC CAUSE**! in
                                          # floodLocation# that will cause !**ADV TYPE**!'''}
    def basisEmergencyManagement(self):
        return {"identifier":"emerMgmt", 
                "displayString": "Emergency Mgmt report flooding in",
                "productString": "Emergency management reported flooding in #floodLocation#"}
    def basisEmergencyManagementHeavyRain(self):
        return {"identifier":"emerMgmtHR", 
                "displayString": "Emergency Mgmt report heavy rain",
                "productString": '''Emergency management reported heavy rain in #floodLocation#. The
                                          heavy rain will cause flooding'''}
    def basisEmergencyManagementThunderstorm(self):
        return {"identifier":"emerMgmtTS", 
                "displayString": "Emergency Mgmt report thunderstorm in",
                "productString": '''Emergency management reported thunderstorms with heavy rain
                                          in #floodLocation#. The heavy rain will cause flooding'''}
    def basisEmergencyManagementIncipientFlooding(self):
        return {"identifier":"emerMgmtInc", 
                "displayString": "Emergency Mgmt report incipient flooding in",
                "productString": '''Emergency management reported !**HY CAUSE**! in
                                          #floodLocation# that will cause !**ADV TYPE**!'''}
    def basisPublic(self):
        return {"identifier":"public", 
                "displayString": "Public report flooding in",
                "productString": "The public reported flooding in #floodLocation#"}
    def basisPublicHeavyRain(self):
        return {"identifier":"publicHR", 
                "displayString": "Public report heavy rain in",
                "productString": '''The public reported heavy rain in #floodLocation#. That
                                          heavy rain will cause flooding'''}
    def basisPublicThunderstorm(self):
        return {"identifier":"publicTS", 
                "displayString": "Public report thunderstorm in",
                "productString": '''The public reported thunderstorms with heavy rain in
                                          #floodLocation#. The heavy rain will cause flooding'''}
    def basisPublicIncipientFlooding(self):
        return {"identifier":"publicInc", 
                "displayString": "Public report incipient flooding in",
                "productString": '''The public reported !**HY CAUSE**! in
                                          #floodLocation# that will cause !**ADV TYPE**!'''}
    def basisSatellite(self):
        return {"identifier":"satInd", 
                "displayString": "Satellite estimates indicate heavy rain in", 
                "productString": '''Satellite estimates indicate heavy rain in #floodLocation#.
                                          That heavy rain will cause flooding'''}
    def basisSatelliteGauges(self):
        return {"identifier":"satGagInd", 
                "displayString": "Satellite estimates and automated gauges", 
                "productString": '''Satellite estimates and rain gauge data indicate heavy 
                                rainfall that will cause flooding in the warning area.'''
                                }        
    def basisHeavyRain(self):
        return {"identifier": "heavyRain",
                "displayString": "Flooding due to heavy rain is possible...",
                "productString": '''Flooding due to heavy rain is possible. The exact amount...intensity...timing...and location 
                    of the rain that will occur is still uncertain. Once there is more certainty...a flood warning or advisory will be issued.''',
                      }        
    def basisEnteredText(self):
        return {"identifier": "basisEnteredText",
                "displayString": "Enter basis statement...",
                "productString": "#basisEnteredText#",
                "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEnteredText",
                             "expandHorizontally": True,
                             "visibleChars": 12,
                             #"values": "|* Enter Location *|"
                             "values": "Enter basis text",
                            }]
 
                }
 
    # DEBRIS FLOW OPTIONS
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
        return {"identifier":"burnScar", 
                "displayString": "Burn scar area with debris flow", 
                "productString": 
                '''Excessive rainfall over the burn scar will result in debris flow moving
                through the #debrisBurnScarDrainage#. The debris flow can consist of 
                rock...mud...vegetation and other loose materials.''',
                "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "debrisBurnScarDrainage",
                             "expandHorizontally": True,
                             "visibleChars": 12,
                             #"values": "|* Enter drainage *|",
                             "values": "Enter drainage",
                            }]
}
    def debrisMudSlide(self):
        return {"identifier":"mudSlide",
                "displayString": "Mud Slides", 
                "productString": 
                '''Excessive rainfall over the warning area will cause mud slides near steep
                terrain. The mud slide can consist of rock...mud...vegetation and other
                loose materials.''',}
 
    # ADDITIONAL INFORMATION
    def getAdditionalInfo(self):
            return {
                     "fieldType":"CheckList",
                     "fieldName": "additionalInfo",
                     "label": "Additional Info:",
                     "choices": self.additionalInfoChoices()
                    }                    
    def listOfCities(self):
        return {"identifier":"listOfCities", 
                "displayString": "Select for a list of cities", 
                "productString": "ARBITRARY ARGUMENTS USED BY CITIES LIST GENERATOR." }
    def listOfDrainages(self):
        return {"identifier":"listOfDrainages", 
                "displayString": "Automated list of drainages", 
                "productString": "ARBITRARY ARGUMENTS USED BY DRAINAGES LIST GENERATOR." }
    def additionalRain(self):
        return  {"identifier":"addtlRain",
                 "displayString": "Additional rainfall of", 
                 "productString": 
                    '''Additional rainfall amounts of #additionalInfoAddtlRainInches# inches are possible in the
                       warned area.''',
                 "detailFields": [
                            {
                             "fieldType": "FractionSpinner",
                             "fieldName": "additionalInfoAddtlRainInches",
                             "minValue": 0,
                             "maxValue": 99,
                             "incrementDelta": 1,
                             "precision": 1
                            },
                            {
                             "fieldType": "Label",
                             "fieldName": "additionalInfoAddtlRainLabel",
                             "label": "inches expected"
                            }
                       ]
                      }
    def floodMoving(self):
        return {"identifier":"floodMoving",
                "displayString": "Flood waters are moving down",
                "productString": 
                      '''Flood waters are moving down #riverName# from #upstreamLocation# to 
                       #floodLocation#. The flood crest is expected to reach #downstreamLocation# by
                       #additionalInfoFloodMovingTime#.''',
                "detailFields": [
                            {
                             "fieldName":"additionalInfoFloodMovingTime",
                             "fieldType":"Time",
                             "label": "by:"
                            }
                      ]
                     }
    def specificPlace(self):
        return {"identifier":"specificPlace",
                "displayString": "Specify location of minor flooding:", 
                "productString": 
                    '''!**IMPACTED LOCATION**! is the most likely place to experience minor flooding. ''',
                "detailFields": [
                        {
                         "fieldType": "Text",
                         "fieldName": "additionalInfoSpecificPlaceLocation",
                         "expandHorizontally": True,
                         "maxChars": 40,
                         "visibleChars": 12
                        }]
                }
    def recedingWater(self):  # EXP / CAN
        return {"identifier":"recedingWater", 
                "displayString": "Water is receding",
                "productString": 
                '''Flood waters have receded...and are no longer expected to pose a threat
                to life or property. Please continue to heed any road closures.''',}
    def rainEnded(self):  # EXP / CAN
        return {"identifier":"rainEnded",
                "displayString": "Heavy rain ended",
                "productString": 
                '''The heavy rain has ended...and flooding is no longer expected to pose a threat.''',}
 
        
    # CALLS TO ACTION
    # Make collapsable
    def getCTAs(self,values=None):
 
        pageFields = { 
                         "fieldType":"CheckList",
                         "label":"Calls to Action (1 or more):",
                         "fieldName": "cta",
                         "choices": self.getCTA_Choices()
                     }
         
        if values is not None:
            pageFields['values'] = values
             
             
        return {
                 
               "fieldType": "ExpandBar",
               "fieldName": "CTABar",
               "expandHorizontally": True,
               "expandVertically": True,
               "pages": [
                            {
                             "pageName": "Calls to Action",
                             "pageFields": [pageFields]
                            }
                         ]
                }        

                       
                       
    def ctaNoCTA(self):
        return {"identifier": "noCTA", 
                "displayString": "No call to action",
                "productString": ""}
    def ctaFloodWatchMeans(self):
        return {"displayString": "A Flood Watch means...", "identifier": "FloodWatch",
                "productString":  '''A Flood Watch means there is a potential for flooding
                based on current forecasts.\n\nYou should monitor later forecasts and be
                alert for possible flood warnings.  Those living in areas prone to flooding
                should be prepared to take action should flooding develop.'''}
    def ctaFloodAdvisoryMeans(self):
        return {"identifier": "floodAdvisoryMeansCTA",
               "displayString": "A Flood Advisory means...",
               "productString": 
              '''A flood advisory means river or stream flows are elevated...or ponding 
                 of water in urban or other areas is occurring or is imminent. Do not
                 attempt to travel across flooded roads. Find alternate routes. It takes
                 only a few inches of swiftly flowing water to carry vehicles away.'''}
    def ctaPointFloodAdvisoryMeans(self):
        return {"identifier": "pointFloodAdvisoryMeansCTA",
                "displayString": "A flood advisory means...",
                "productString": ''''A flood advisory means minor flooding is possible and rivers are forecast to exceed bankfull. 
                 If you are in the advisory area remain alert to possible flooding... or the possibility of the advisory being upgraded to a warning.''',
                }
    def ctaFloodWarningMeans(self):
        return {"identifier": "floodWwarningMeansCTA",
               "displayString": "A Flood Warning means...",
               "productString": 
              '''A flood warning means that flooding is imminent or has been reported. Stream rises
                 will be slow and flash flooding is not expected. However...all interested parties
                 should take necessary precautions immediately. '''}
    def ctaFlashFloodWatchMeans(self):
        return {"identifier": "flashFloodWatchMeansCTA",
                "displayString": "A Flash Flood Watch means...", 
                "productString": '''A Flash Flood Watch means that conditions may develop
                      that lead to flash flooding.  Flash flooding is a very dangerous situation.
                      \n\nYou should monitor later forecasts and be prepared to take action should
                      flash flood warnings be issued.'''}
    def ctaActQuickly(self):
        return {"identifier": "actQuicklyCTA","displayString": "Act Quickly...",
                "productString": 
                "Move to higher ground now. Act quickly to protect your life."}
    def ctaChildSafety(self):
        return {"identifier": "childSafetyCTA","displayString": "Child Safety...",
                "productString": 
                '''Keep children away from storm drains...culverts...creeks and streams.
                Water levels can rise rapidly and sweep children away.'''}
    def ctaNightTime(self):
        return  {"identifier": "nighttimeCTA",
                "displayString": "Nighttime flooding...",
                "productString":
               '''Be especially cautious at night when it is harder to recognize the
                  dangers of flooding. If flooding is observed act quickly. move up to higher
                  ground to escape flood waters. Do not stay in areas subject to flooding
                  when water begins rising.'''}
    def ctaSafety(self):
        return {"identifier": "safetyCTA","displayString": "Safety...by foot or motorist",
                "productString":
                "Do not enter or cross flowing water or water of unknown depth."}
    def ctaTurnAround(self):
        return {"identifier": "turnAroundCTA",
                "displayString": "Turn around...don't drown",
                "productString":
               '''Most flood deaths occur in automobiles. Never drive your vehicle into areas where
                  the water covers the roadway. Flood waters are usually deeper than they appear.
                  Just one foot of flowing water is powerful enough to sweep vehicles off the road.
                  When encountering flooded roads make the smart choice...Turn around...Dont drown.'''}
    def ctaStayAway(self):
        return {"identifier": "stayAwayCTA","displayString": "Stay away or be swept away",
                "productString":
                '''Stay away or be swept away. River banks and culverts can become
                unstable and unsafe.'''}
    def ctaArroyos(self):
        return {"identifier": "arroyosCTA","displayString": "Arroyos...",
                "productString": 
                '''Remain alert for flooding even in locations not receiving rain.
                arroyos...Streams and rivers can become raging killer currents
                in a matter of minutes...even from distant rainfall.'''}
    def ctaBurnAreas(self):
        return {"identifier": "burnAreasCTA",
                "displayString": "Burn Areas...",
                "productString": 
                '''Move away from recently burned areas. Life threatening flooding
                of creeks...roads and normally dry arroyos is likely. The heavy
                rains will likely trigger rockslides...mudslides and debris flows
                in steep terrain...especially in and around these areas.'''}
    def ctaReportFlooding(self):
        return {"identifier": "reportFloodingCTA",
                "displayString": "Report flooding to local law enforcement",
                "productString": 
               '''To report flooding...have the nearest law enforcement agency relay
                  your report to the National Weather Service forecast office.'''}
    def ctaAutoSafety(self):
        return {"identifier": "autoSafetyCTA","displayString": "Auto Safety",
               "productString":
               '''Flooding is occurring or is imminent. Most flood related deaths
                occur in automobiles. Do not attempt to cross water covered bridges...
                dips...or low water crossings. Never try to cross a flowing stream...
                even a small one...on foot. To escape rising water find another
                route over higher ground.'''}
    def ctaDontDrive(self):
        return {"identifier": "dontDriveCTA",
               "displayString": "Do not drive into water",
               "productString":
              '''Do not drive your vehicle into areas where the water covers the
                 roadway. The water depth may be too great to allow your car to
                 cross safely.  Move to higher ground.'''}
    def ctaDoNotDrive(self):
        return {"identifier": "doNotDriveCTA",
                "displayString": "Do not drive through flooded areas...",
                "productString": "Motorists should not attempt to drive around barricades or drive cars through flooded areas."}
    def ctaRiverBanks(self):
        return {"identifier": "riverBanksCTA",
                "displayString": "Walking near riverbanks...",
                "productString": "Caution is urged when walking near riverbanks."}
    def ctaCamperSafety(self):
        return {"identifier": "camperSafetyCTA",
               "displayString": "Camper safety",
               "productString":
              '''Flooding is occurring or is imminent.  It is important to know where
                 you are relative to streams...rivers...or creeks which can become
                 killers in heavy rains.  Campers and hikers should avoid streams
                 or creeks.'''}
    def ctaLowSpots(self):
        return {"identifier": "lowSpotsCTA",
               "displayString": "Low spots in hilly terrain ",
               "productString": 
              '''In hilly terrain there are hundreds of low water crossings which 
                 are potentially dangerous in heavy rain.  Do not attempt to travel
                 across flooded roads. Find alternate routes.  It takes only a
                 few inches of swiftly flowing water to carry vehicles away.'''}
    def ctaPowerFlood(self):
        return {"identifier": "powerFloodCTA",
               "displayString": "Power of flood waters/vehicles",
               "productString":  
              '''Do not underestimate the power of flood waters. Only a few
                 inches of rapidly flowing water can quickly carry away your
                 vehicle.'''}
    def ctaFlashFloodWarningMeans(self):
        return {"identifier": "ffwMeansCTA","displayString": "A Flash Flood Warning means...",
              "productString": 
             '''A flash flood warning means that flooding is imminent or occurring.
                If you are in the warning area move to higher ground immediately.
                Residents living along streams and creeks should take immediate
                precautions to protect life and property. Do not attempt to cross
                swiftly flowing waters or waters of unknown depth by foot or by
                automobile.'''}
    def ctaUrbanFlooding(self):
        return  {"identifier": "urbanFloodingCTA",
               "displayString": "Urban Flooding",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause ponding of water in urban areas...
                 highways...streets and underpasses as well as other poor drainage areas and low
                 lying spots. Do not attempt to travel across flooded roads. Find alternate routes.
                 It takes only a few inches of swiftly flowing water to carry vehicles away.'''}
    def ctaRuralFlooding(self):
        return {"identifier": "ruralFloodingCTA",
               "displayString": "Rural flooding/small streams",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of small creeks and
                 streams...As well as farm and country roads. Do not attempt to travel across
                 flooded roads. Find alternate routes.'''}
    def ctaRuralUrbanFlooding(self):
        return {"identifier": "ruralUrbanCTA",
               "displayString": "Flooding of rural and urban areas",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of
                 small creeks and streams...highways and underpasses in urban areas.
                 Additionally...country roads and farmlands along the banks of
                 creeks...streams and other low lying areas are subject to
                 flooding.'''}
    def ctaStayTuned(self):
        return {"identifier": "stayTunedCTA",
                "displayString": "Stay tuned to NOAA Weather Radio for further information...",
                "productString": '''Stay tuned to further developments by listening to your local radio... 
                television... or NOAA Weather Radio for further information.'''}
    def ctaRisingWater(self):
        return { "identifier": "risingWaterCTA",
                "displayString": "To escape rising water...",
                "productString": "To escape rising water... take the shortest path to higher ground."}
    def ctaForceOfWater(self):
        return {"identifier": "forceOfWaterCTA",
                "displayString": "Force of fast-moving flood water...",
                "productString": '''Even 6 inches of fast-moving flood water can knock you off your feet... 
                and a depth of 2 feet will float your car.  Never try to walk... swim... or drive through such swift water.  
                If you come upon flood waters... stop... turn around and go another way.'''}
    def ctaLastStatement(self):
        return {"identifier": "lastStatementCTA",
                "displayString": "Last river flood statement on this event...", 
                "productString": "This will be the last river flood statement on this event.  Stay tuned to developments."}
    def ctaWarningInEffect(self):
        return {"identifier":  "warningInEffectCTA",
                "displayString": "This warning will be in effect...",
                "productString": "This warning will be in effect until the river falls below its flood stage."}
    
    def getEndingSynopsis(self):
        return {
         'fieldName': 'endingSynopsis',
         'fieldType':'Text',
         'label':'Enter Ending Synopsis',
         'values': '',
         'length': 90,
         }
        
    # CAP FIELDS
    def getCAP_Fields(self,tupleList=None):
        capFieldsExpandBar = {
                 
               "fieldType": "ExpandBar",
               "fieldName": "CAPBar",
               "expandHorizontally": True,
               "expandVertically": True,
               "pages": [
                            {
                             "pageName": "CAP",
                             "pageFields":self.getCAPFieldEntries()
                             }
                         ]
                }
         
        if tupleList is not None:
            capFieldsExpandBar = self.setCAP_Fields(tupleList)
         
        return capFieldsExpandBar
    

    def getCAPFieldEntries(self):
        return [ 
                   { 
                    'fieldName': 'urgency',
                    'fieldType':'ComboBox',
                    'label':'Urgency:',
                    'expandHorizontally': True,
                    'values': 'Immediate',
                    'choices': ['Immediate', 'Expected', 'Future','Past','Unknown']
                    },
                   {     
                    'fieldName': 'responseType',
                    'fieldType':'ComboBox',
                    'label':'Response Type:',
                    'expandHorizontally': True,
                    'values': 'Avoid',
                    'choices': ['Shelter','Evacuate','Prepare','Execute','Avoid','Monitor','Assess','AllClear','None']
                    },                    
                   { 
                    'fieldName': 'severity',
                    'fieldType':'ComboBox',
                    'label':'Severity:',
                    'expandHorizontally': True,
                    'values': 'Severe',
                    'choices': ['Extreme','Severe','Moderate','Minor','Unknown']
                    },
                   { 
                    'fieldName': 'certainty',
                    'fieldType':'ComboBox',
                    'label':'Certainty:',
                    'expandHorizontally': True,
                    'values': 'Likely',
                    'choices': ['Observed','Likely','Possible','Unlikely','Unknown']
                    },
                ]  + [self.CAP_WEA_Message()]


    # Used to be in subclasses                
    def setCAP_Fields(self, tupleList):
        # Set the defaults for the CAP Fields
        ### NOTE - since we are using a ExpandBar, we have to
        ### mind the structure which is a dict of lists 
        ### of dicts of lists (seriously)
         
        capExpandBar = self.getCAP_Fields()
        for entry in capExpandBar['pages']:
            for capFields in entry['pageFields']:
                for fieldName, values in tupleList:
                    if capFields["fieldName"] == fieldName:
                        capFields["values"] = values  
        return capExpandBar          

                 
    def CAP_WEA_Message(self):
        return {                
                "fieldType":"CheckBoxes",
                "label":"Activate WEA Text",
                "fieldName": "activateWEA",
                "values": self.CAP_WEA_Values(),
                "choices": [{
                      "identifier":  "WEA_activated",
                      "displayString": "",
                      "productString": "",       
                      "detailFields": [{
                         'fieldName': 'WEA_Text',
                         'fieldType':'Text',
                         'label':'WEA Text (%s is end time/day):',
                         'values': self.CAP_WEA_Text(),
                         'length': 90,
                         }]
                   }],
                 }
         
    def CAP_WEA_Values(self):
        return []
         
    def CAP_WEA_Text(self):
        return ''
           
    #######################  WEA Messages
    #
    #     Tsunami Warning (coming late 2013)    Tsunami danger on the coast.  Go to high ground or move inland. Check local media. -NWS 
    #     Tornado Warning                       Tornado Warning in this area til hh:mm tzT. Take shelter now. Check local media. -NWS 
    #     Extreme Wind Warning                  Extreme Wind Warning this area til hh:mm tzT ddd. Take shelter. -NWS
    #     Flash Flood Warning                   Flash Flood Warning this area til hh:mm tzT. Avoid flooded areas. Check local media. -NWS
    #     Hurricane Warning                     Hurricane Warning this area til hh:mm tzT ddd. Check local media and authorities. -NWS
    #     Typhoon Warning                       Typhoon Warning this area til hh:mm tzT ddd. Check local media and authorities. -NWS
    #     Blizzard Warning                      Blizzard Warning this area til hh:mm tzT ddd. Prepare. Avoid travel. Check media. -NWS
    #     Ice Storm Warning                     Ice Storm Warning this area til hh:mm tzT ddd. Prepare. Avoid travel. Check media. -NWS
    #     Dust Storm Warning                    Dust Storm Warning in this area til hh:mm tzT ddd. Avoid travel. Check local media. -NWS
    #         
    #     Legend
    #     hh:mm tzT ddd
    #     tzT = timezone
    #     ddd= three letter abbreviation for day of the week 
    #      