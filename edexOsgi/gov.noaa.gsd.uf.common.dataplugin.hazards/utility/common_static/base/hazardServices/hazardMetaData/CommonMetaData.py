

class MetaData:
    
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
                self.immediateCauseRS(),
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

    # INCLUDE
    def getInclude(self):
        return {
             "fieldType":"CheckBoxes",
             "fieldName": "include",
             "label": "Include",
             "choices": self.includeChoices()
             }        
    def includeChoices(self):
        return []
    
    def includeEmergency(self):
        return { 
                "identifier": "ffwEmergency",
                "displayString": "**SELECT FOR FLASH FLOOD EMERGENCY**",
                "productString": "...Flash flood emergency for !** LOCATION **!..."
                }            
    def includeSnowMelt(self):
        return {
                "identifier":"+SM", "displayString":"Also Snow Melt",
                "productString":"Rapid snow melt is causing flooding."
                }
    def includeFlooding(self):
                {
                 "identifier":"-FL", "displayString": "Flooding not directly reported, only heavy rain",
                 "productString": "Flooding is not directly reported, only heavy rains.",
                 }

    # BASIS
    def getBasis(self):
        return {
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "",
            "choices": self.basisChoices(),
            }        
    def basisChoices(self):
        return []        
    def basisDoppler(self):
        return {"identifier":"radInd", "displayString": "Doppler Radar indicated...", 
                "productString": "Doppler Radar indicated",}
    def basisDopplerGauges(self):
        return {"identifier":"radGagInd", "displayString": "Doppler Radar and automated gauges", 
                "productString": "Doppler Radar and automated rain gauges indicated",}
    def basisSpotter(self):
        return {"identifier":"wxSpot", "displayString": "Trained weather spotters reported...", 
                "productString": "Trained weather spotters reported",}
    def basisPublic(self):
        return {"identifier":"public", "displayString": "Public reported...",
                "productString": "The public reported",}
    def basisLawEnforcement(self):
        return {"identifier":"lawEnf", "displayString": "Local law enforcement reported...",
                "productString": "Local law enforcement reported",}
    def basisEmergencyMgmt(self):
        return {"identifier":"emerMgmt", "displayString": "Emergency management reported...",
                "productString": "Emergency management reported",}

    # ADDITIONAL INFORMATION
    def getAdditionalInfo(self):
            return {
                     "fieldType":"CheckList",
                     "fieldName": "additionalInfo",
                     "label": "Additional Info:",
                     "choices": self.additionalInfoChoices()
                    }                    
    def additionalInfoChoices(self):
        return []        
    def listOfCities(self):
        return {"identifier":"listOfCities", "displayString": "Select for a list of cities", 
                    "productString": "ARBITRARY ARGUMENTS USED BY CITIES LIST GENERATOR." }
    def listOfDrainages(self):
        return {"identifier":"listOfDrainages", "displayString": "Automated list of drainages", 
                "productString": "ARBITRARY ARGUMENTS USED BY DRAINAGES LIST GENERATOR." }
    def additionalRain(self):
        return {"identifier":"addtlRain", "displayString": "Additional rainfall of XX inches expected", 
                "productString": 
                '''Additional rainfall amounts of !** EDIT AMOUNT **! are possible in the
                warned area.''',}
    def particularStream(self):
        return {"identifier":"particularStream",
                "displayString": "Flooding is occurring in a particular stream/river", 
                "productString": 
                '''FLood waters are moving down !**NAME OF CHANNEL**! from !**LOCATION**! to 
                !**LOCATION**!. The flood crest is expected to reach !**LOCATION(S)**! by
                !**TIME(S)**!.''',}
    def recedingWater(self):
        return {"identifier":"recedingWater", "displayString": "EXP-CAN:Water is receding",
                "productString": 
                '''Flood waters have receded...and are no longer expected to pose a threat
                to life or property. Please continue to heed any road closures.''',}
    def rainEnded(self):
        return {"identifier":"rainEnded", "displayString": "EXP-CAN:Heavy rain ended",
                "productString": 
                '''The heavy rain has ended...and flooding is no longer expected to pose a threat.''',}

    # CALLS TO ACTION
    def getCTAs(self):
        return {
                "fieldType":"CheckList",
                "label":"Calls to Action (1 or more):",
                "fieldName": "cta",
                "values": "noCTA",
                "choices": self.getCTA_Choices()
                }        
    def getCTA_Choices(self):
        return []
    
    def ctaNoCTA(self):
        return {"identifier": "noCTA", "displayString": "No call to action",
                "productString": ""}
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
        return {"identifier": "nighttimeCTA","displayString": "Nighttime flooding...",
                "productString":
                '''Be especially cautious at night when it is harder to recognize the
                dangers of flooding.'''}
    def ctaSafety(self):
        return {"identifier": "safetyCTA","displayString": "Safety...by foot or motorist",
                "productString":
                "Do not enter or cross flowing water or water of unknown depth."}
    def ctaTurnAround(self):
        return {"identifier": "turnAroundCTA","displayString": "Turn around...don't drown",
                "productString":
                '''Turn around...dont drown when encountering flooded roads.
                Most flood deaths occur in vehicles.'''}
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
        return {"identifier": "reportFloodingCTA","displayString": "Report flooding to local law enforcement",
                "productString": 
                '''Please report flooding to your local law enforcement agency when
                you can do so safely.'''}

    def ctaAutoSafety(self):
        return {"identifier": "autoSafetyCTA","displayString": "Auto Safety",
               "productString":
               '''Flooding is occurring or is imminent. Most flood related deaths
                occur in automobiles. Do not attempt to cross water covered bridges...
                dips...or low water crossings. Never try to cross a flowing stream...
                even a small one...on foot. To escape rising water find another
                route over higher ground.'''}
    def ctaDontDrive(self):
        return {"identifier": "dontDriveCTA","displayString": "Do not drive into water",
              "productString":
             '''Do not drive your vehicle into areas where the water covers the
                roadway. The water depth may be too great to allow your car to
                cross safely.  Move to higher ground.'''}
    def ctaCamperSafety(self):
        return {"identifier": "camperSafetyCTA","displayString": "Camper safety",
              "productString":
             '''Flooding is occurring or is imminent.  It is important to know where
                you are relative to streams...rivers...or creeks which can become
                killers in heavy rains.  Campers and hikers should avoid streams
                or creeks.'''}
    def ctaLowSpots(self):
        return {"identifier": "lowSpotsCTA","displayString": "Low spots in hilly terrain ",
              "productString": 
             '''In hilly terrain there are hundreds of low water crossings which 
                are potentially dangerous in heavy rain.  Do not attempt to travel
                across flooded roads. Find an alternate route.  It takes only a
                few inches of swiftly flowing water to carry vehicles away.'''}
    def ctaPowerFlood(self):
        return {"identifier": "powerFloodCTA","displayString": "Power of flood waters/vehicles",
              "productString":  
             '''Do not underestimate the power of flood waters. Only a few
                inches of rapidly flowing water can quickly carry away your
                vehicle.'''}
    def ctaFF_W_Means(self):
        return {"identifier": "ffwMeansCTA","displayString": "A Flash Flood Warning means...",
              "productString": 
             '''A flash flood warning means that flooding is imminent or occurring.
                If you are in the warning area move to higher ground immediately.
                Residents living along streams and creeks should take immediate
                precautions to protect life and property. Do not attempt to cross
                swiftly flowing waters or waters of unknown depth by foot or by
                automobile.'''}
    def ctaUrbanFlooding(self):
        return {"identifier": "urbanFloodingCTA","displayString": "Urban Flooding",
              "productString": 
             '''Excessive runoff from heavy rainfall will cause flooding of
                small creeks and streams...urban areas...highways...streets
                and underpasses as well as other drainage areas and low lying
                spots.'''}
    def ctaRuralFlooding(self):
        return {"identifier": "ruralFloodingCTA","displayString": "Rural flooding/small streams",
              "productString": 
             '''Excessive runoff from heavy rainfall will cause flooding of
                small creeks and streams...country roads...as well as farmland
                as well as other drainage areas and low lying spots.'''}
    def ctaRuralUrbanFlooding(self):
        return {"identifier": "ruralUrbanCTA","displayString": "Rural and Urban Flooding",
              "productString": 
             '''Excessive runoff from heavy rainfall will cause flooding of
                small creeks and streams...highways and underpasses.
                Additionally...country roads and farmlands along the banks of
                creeks...streams and other low lying areas are subject to
                flooding.'''}
    
    # CAP FIELDS
    def getCAP_Fields(self):    
        return [ 
               { 
                'fieldName': 'urgency',
                'fieldType':'ComboBox',
                'label':'Urgency:',
                'values': 'Immediate',
                'choices': ['Immediate', 'Expected', 'Future','Past','Unknown']
                },
               { 
    
                'fieldName': 'responseType',
                'fieldType':'ComboBox',
                'label':'Response Type:',
                'values': 'Avoid',
                'choices': ['Shelter','Evacuate','Prepare','Execute','Avoid','Monitor','Assess','AllClear','None']
                },                    
               { 
                'fieldName': 'severity',
                'fieldType':'ComboBox',
                'label':'Severity:',
                'values': 'Severe',
                'choices': ['Extreme','Severe','Moderate','Minor','Unknown']
                },
               { 
                'fieldName': 'certainty',
                'fieldType':'ComboBox',
                'label':'Certainty:',
                'values': 'Likely',
                'choices': ['Observed','Likely','Possible','Unlikely','Unknown']
                },
                {
                'fieldName': 'WEA_Text',
                'fieldType':'Text',
                'label':'WEA Text (%s is end time/day):',
                'values': '',
                'length': 90,
                 },                   
                ]    
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
