"""
    Description: Common Meta Data shared among hazard types.
"""

import VTECConstants
from LocalizationInterface import LocalizationInterface

class MetaData():
    
    def initialize(self, hazardEvent, metaDict):    
        self.hazardEvent = hazardEvent
        self.metaDict = metaDict
        if self.hazardEvent:
            self.hazardStatus = self.hazardEvent.getStatus().lower()
        else:
            self.hazardStatus = "pending"
    
            
    def editableWhenNew(self):
        return self.hazardStatus == "pending"
    
    def getPointID(self):
        return {
             "fieldName": "pointID",
             "fieldType": "Text",
             "label": "Forecast Point:",
             "maxChars": 5,
            }
        
    def getImmediateCause(self):
        return {
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "ER",
            "expandHorizontally": False,
            "choices": self.immediateCauseChoices(),
            "editable" : self.editableWhenNew(),
                }
        
    def immediateCauseChoices(self):
        return [
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
        
    def immediateCauseER(self):
        return {"identifier":"ER", "displayString":"ER (Excessive Rainfall)"}
    def immediateCauseSM(self):
        return {"identifier":"SM", "displayString":"SM (Snow Melt)"}
    def immediateCauseRS(self):
        return {"identifier":"RS", "displayString":"RS (Rain and Snow Melt)"}
    def immediateCauseDM(self):
        return {"identifier":"DM", "displayString":"DM (Dam or Levee Failure)"}
    def immediateCauseDR(self):
        return {"identifier":"DR", "displayString":"DR (Upstream Dam Release)"}
    def immediateCauseGO(self):
        return {"identifier":"GO", "displayString":"GO (Glacier-Dammed Lake Outburst)"}
    def immediateCauseIJ(self):
        return {"identifier":"IJ", "displayString":"IJ (Ice Jam)"}
    def immediateCauseIC(self):
        return {"identifier":"IC", "displayString":"IC (Rain and/or Snow melt and/or Ice Jam)"}
    def immediateCauseFS(self):
        return {"identifier":"FS", "displayString":"FS (Upstream Flooding plus Storm Surge)"}
    def immediateCauseFT(self):
        return {"identifier":"FT", "displayString":"FT (Upstream Flooding plus Tidal Effects)"}
    def immediateCauseET(self):
        return {"identifier":"ET", "displayString":"ET (Elevated Upstream Flow plus Tidal Effects)"}
    def immediateCauseWT(self):
        return {"identifier":"WT", "displayString":"WT (Wind and/or Tidal Effects)"}
    def immediateCauseOT(self):
        return {"identifier":"OT", "displayString":"OT (Other Effects)"}
    def immediateCauseMC(self):
        return {"identifier":"MC", "displayString":"MC (Other Multiple Causes)"}
    def immediateCauseUU(self):
        return {"identifier":"UU", "displayString":"UU (Unknown)"}
     
    def getEventSpecificSource(self):
        return {
               "fieldType":"Group",
               "fieldName":"eventSpecificSource",
               "numColumns":2,
               "fields" : [
                       self.getSource(),
                       self.getEventType(),
                       ]
               }

    def getFloodSeverity(self):
        return {
             "fieldName": "floodSeverity",
             "fieldType":"ComboBox",
             "label":"Flood Severity:",
             "expandHorizontally": False,
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
                             "fieldType": "Composite",
                             "fieldName": "includeEmergencyLocationWrapper",
                             "fullWidthOfDetailPanel": True,
                             "expandHorizontally": True,
                             "fields": [
                                        {
                                         "fieldType": "Text",
                                         "fieldName": "includeEmergencyLocation",
                                         "expandHorizontally": True,
                                         "maxChars": 40,
                                         "visibleChars": 12,
                                         "values": "Enter Location"
                                         }
                                        ]
                             }
                            ]
                } 
                           
    def eventTypeThunder(self):
        return {
                "identifier":"thunderEvent", "displayString":"Thunderstorm(s)"
                }
    def eventTypeRain(self):
        return {
                "identifier":"rainEvent", "displayString": "Heavy rainfall (no thunder)"
                }
        
    def eventTypeMinorFlooding(self):
        return {
                "identifier":"minorFlooding", "displayString": "Minor flooding occurring"
                }
        
    def eventTypeFlooding(self):
        return {
                "identifier":"flooding", "displayString": "Flooding occurring"
                }
        
    def eventTypeGenericFlooding(self):
        return {
                "identifier":"genericFlooding", "displayString": "Generic (provide reasoning)"
                }
        
    def eventTypeFlashFlooding(self):
        return {
                "identifier":"flashFlooding", "displayString": "Flash flooding occurring"
                }
                                           
    def getRainAmt(self):
        return {
               "fieldType":"RadioButtons",
               "label":"Rain so far:",
               "fieldName": "rainAmt",
               "choices": [
                    self.rain_amount_unknown(),
                    self.enterAmount(),
                    ]
                }
        
    def rain_amount_unknown(self):
        return {"identifier":"rainUnknown", "displayString":"Unknown",
                "productString":"",}

    def enterAmount(self):                
        return  {"identifier":"rainKnown", "displayString":"",
                 "detailFields": [
                       {
                        "fieldType": "Label",
                        "fieldName": "rainAmtPrefix",
                        "label": "Between"
                       },
                       {
                        "fieldType": "FractionSpinner",
                        "fieldName": "rainSoFarLowerBound",
                        "minValue": 0,
                        "maxValue": 99,
                        "incrementDelta": 1,
                        "precision": 1
                       },
                       {
                        "fieldType": "Label",
                        "fieldName": "rainAmtAnd",
                        "label": "and"
                       },
                       {
                        "fieldType": "FractionSpinner",
                        "fieldName": "rainSoFarUpperBound",
                        "minValue": 0,
                        "maxValue": 99,
                        "incrementDelta": 1,
                        "precision": 1
                       },
                       {
                        "fieldType": "Label",
                        "fieldName": "rainAmtSuffix",
                        "label": "inches of rain have fallen"
                       }
                 ]
                }
 
    # Takes the first one listed as the default
    #  This can be overridden by listing a specific default
    def defaultValue(self, choices):
        for choice in choices:
            return choice.get('identifier')       

    def dopplerSource(self):
        return {"identifier":"dopplerSource", 
                "displayString": "Doppler Radar indicated",
        }
        
    def dopplerGaugesSource(self):
        return {"identifier":"dopplerGaugesSource", 
                "displayString": "Doppler Radar and automated gauges",
        }
        
    def trainedSpottersSource(self):
        return {"identifier":"trainedSpottersSource", 
                "displayString": "Trained spotters reported",
        }
    def publicSource(self):
        return {"identifier":"publicSource", 
                "displayString": "Public reported",
        }
        
    def localLawEnforcementSource(self):
        return {"identifier":"localLawEnforcementSource", 
                "displayString": "Local law enforcement reported",
        }
        
    def emergencyManagementSource(self):
        return {"identifier":"emergencyManagementSource", 
                "displayString": "Emergency management reported",
        }
        
    def satelliteSource(self):
        return {"identifier":"satelliteSource", 
                "displayString": "Satellite estimates",
        }
        
    def satelliteGaugesSource(self):
        return {"identifier":"satelliteGaugesSource", 
                "displayString": "Satellite estimates and gauge reports",
        }
        
    def gaugesSource(self):
        return {"identifier":"gaugesSource", 
                "displayString": "Gauge reports",
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
                             "values": "Enter basis text",
                            }]
 
                }
 
    def getAdditionalInfo(self):
            return {
                     "fieldType":"CheckBoxes",
                     "fieldName": "additionalInfo",
                     "showAllNoneButtons" : False,
                     "label": "Additional Info:",
                     "choices": self.additionalInfoChoices(),
                     "values":"listOfCities",
                     "lines": 3
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
                 "displayString": "Additional rainfall", 
                 "productString": 
                    '''Additional rainfall amounts of #additionalInfoAddtlRainInches# inches are possible in the
                       warned area.''',
                 "detailFields": [
                            {
                            "fieldType": "Label",
                            "fieldName": "additionalRainPrefix",
                            "label": "of"
                            },
                            {
                             "fieldType": "FractionSpinner",
                             "fieldName": "additionalRainLowerBound",
                             "minValue": 0,
                             "maxValue": 99,
                             "incrementDelta": 1,
                             "precision": 1
                            },
                            {
                            "fieldType": "Label",
                            "fieldName": "additionalRainTo",
                            "label": "to"
                            },
                            {
                             "fieldType": "FractionSpinner",
                             "fieldName": "additionalRainUpperBound",
                             "minValue": 0,
                             "maxValue": 99,
                             "incrementDelta": 1,
                             "precision": 1
                            },
                            {
                             "fieldType": "Label",
                             "fieldName": "additionalRainSuffix",
                             "label": "inches is expected"
                            }
                       ]
                      }
    def floodMoving(self):
        return {"identifier":"floodMoving",
                "displayString": "Flooding is occurring in:",
                "detailFields": [
                            {
                             "fieldName":"floodLocationName",
                             "fieldType":"Text",
                             "label": ""
                            }
                      ]
                     }
    def floodLocation(self):
        return {"identifier":"floodLocation",
                "displayString": "Specify location of flooding:", 
                "detailFields": [
                        {
                         "fieldType": "Text",
                         "fieldName": "floodLocation",
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
 
        
    def getCTAs(self,values=None):
 
        pageFields = { 
                         "fieldType":"CheckList",
                         "label":"Calls to Action (1 or more):",
                         "fieldName": "cta",
                         "showAllNoneButtons" : False,
                         "choices": self.getCTA_Choices()
                     }
         
        if values is not None:
            pageFields['values'] = values
             
             
        return {
                 
               "fieldType": "ExpandBar",
               "fieldName": "CTABar",
               "expandHorizontally": True,
               "pages": [
                            {
                             "pageName": "Calls to Action",
                             "pageFields": [pageFields]
                            }
                         ]
                }        

                       
                       
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
        return {"identifier": "floodWarningMeansCTA",
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
         'lines': 2,
         'visibleChars': 40,
         'length': 90,
         }
        
    def getEndingOption(self):
        choices = [
                    "Water is receding",
                    "Heavy rain ended",
                    ]
        return {
             "fieldType":"RadioButtons",
             "fieldName": "endingOption",
             "label":"Ending Option:",
             "values": choices[0],
             "choices": choices,
                }
        
    # CAP FIELDS
    def getCAP_Fields(self,tupleList=None):
        capFieldsExpandBar = {
                 
               "fieldType": "ExpandBar",
               "fieldName": "CAPBar",
               "expandHorizontally": True,
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

# Helper function to be called by metadata megawidget interdependency implementations
# that include Rise Above/Crest/Fall Below and need to allow Until Further Notice to
# be applied to Fall Below. It ensures that if the associated Until Further Notice
# checkbox is checked, the Fall Below thumb in the time scale megawidget is disabled.
def applyRiseCrestFallUntilFurtherNoticeInterdependencies(triggerIdentifiers, mutableProperties):
    
    # Do nothing unless the "until further notice" checkbox has changed state, or
    # initialization is occurring.
    if triggerIdentifiers == None or "fallBelowUntilFurtherNotice" in triggerIdentifiers:
        
        # Determine whether the "fall below" state should be editable or read-only.
        # If "until further notice" is turned on, it should be read-only.
        editable = True
        if "fallBelowUntilFurtherNotice" in mutableProperties \
                and "values" in mutableProperties["fallBelowUntilFurtherNotice"]:
            editable = not mutableProperties["fallBelowUntilFurtherNotice"]["values"]

        # If "until further notice" has just been turned on, remember the interval
        # as it is now between the "crest" and "fall below" times in case "until
        # further notice" is toggled off in the future, and set the "fall below"
        # time to the special "until further notice" value. Otherwise, if "until
        # further notice" has just been turned off, set the "fall below" time to
        # be an interval offset from the "crest" time. If a saved interval is
        # found, use that as the interval; otherwise, make the interval equal to
        # the one between the "riseAbove" and "crest" times. Finally, if it has
        # not just been turned on or off, ensure that the "fallBelow" state is
        # still read-only or editable as appropriate. This last case will occur,
        # for example, when the script is called as part of the megawidgets'
        # initialization.
        from VTECConstants import UFN_TIME_VALUE_SECS
        ufnTime = UFN_TIME_VALUE_SECS * 1000L
        if "riseAbove:crest:fallBelow" in mutableProperties:
            if editable == False and \
                    mutableProperties["riseAbove:crest:fallBelow"]["values"]["fallBelow"] != ufnTime:
                
                interval = long(mutableProperties["riseAbove:crest:fallBelow"]["values"]["fallBelow"] - \
                        mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"])
                fallBelow = ufnTime
                
                return { "riseAbove:crest:fallBelow": {
                                                       "valueEditables": { "fallBelow": False },
                                                       "extraData": { "lastInterval": interval },
                                                       "values": { "fallBelow": fallBelow }
                                                       }
                        }
            elif editable == True and \
                    mutableProperties["riseAbove:crest:fallBelow"]["values"]["fallBelow"] == ufnTime:
                
                if "extraData" in mutableProperties["riseAbove:crest:fallBelow"] \
                        and "lastInterval" in mutableProperties["riseAbove:crest:fallBelow"]["extraData"]:
                    interval = mutableProperties["riseAbove:crest:fallBelow"]["extraData"]["lastInterval"]
                else:
                    interval = long(mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"] - \
                            mutableProperties["riseAbove:crest:fallBelow"]["values"]["riseAbove"])
                fallBelow = mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"] + interval
                
                return { "riseAbove:crest:fallBelow": {
                                                       "valueEditables": { "fallBelow": True },
                                                       "values": { "fallBelow": fallBelow }
                                                       }
                        }
            else:
                return { "riseAbove:crest:fallBelow": { "valueEditables": { "fallBelow": editable } } }
        else:
            return None
    else:
        return None

def applyInterdependencies(triggerIdentifiers, mutableProperties):

    propertyChanges = {}
    if (triggerIdentifiers == None or "endingOption" in triggerIdentifiers) and \
        "endingOption" in mutableProperties:
        endingOption = mutableProperties["endingOption"]["values"]
        propertyChanges["endingSynopsis"] = {
                        "values" : endingOption,                        
                        }        
    return propertyChanges
                
