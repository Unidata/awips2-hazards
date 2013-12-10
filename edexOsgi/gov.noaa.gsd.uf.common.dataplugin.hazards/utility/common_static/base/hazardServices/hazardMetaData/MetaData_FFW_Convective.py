

from CAP_Fields import CAP_Fields

# Set the defaults for the CAP Fields
for entry in CAP_Fields:
    for fieldName, values in [
                ("urgency", "Immediate"),
                ("responseType", "Avoid"),
                ("severity", "Severe"),
                ("certainty", "Likely"),
                ("WEA_Text", "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"),
                ]:
        if entry["fieldName"] == fieldName:
            entry["values"] = values            
 
MetaData_FFW_Convective = [
            {             
            # use "_override_by_key_fieldName_", in override file to support incremental override
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "ER",
            "expandHorizontally": True,
            "choices": [
                # use "_override_by_key_identifier_", in override file to support incremental override
                {"identifier":"ER", "productString":"ER", "displayString":"ER (Excessive Rainfall)"},
                {"identifier":"SM", "productString":"SM", "displayString":"SM (Snow Melt)"},
                {"identifier":"RS", "productString":"RS", "displayString":"RS (Rain and Snow Melt)"},
                {"identifier":"DM", "productString":"DM", "displayString":"DM (Dam or Levee Failure)"},
                {"identifier":"DR", "productString":"DR", "displayString":"DR (Upstream Dam Release)"},
                {"identifier":"GO", "productString":"GO", "displayString":"GO (Glacier-Dammed Lake Outburst)"},
                {"identifier":"IJ", "productString":"IJ", "displayString":"IJ (Ice Jam)"},
                {"identifier":"IC", "productString":"IC", "displayString":"IC (Rain and/or Snow melt and/or Ice Jam)"},
                {"identifier":"FS", "productString":"FS", "displayString":"FS (Upstream Flooding plus Storm Surge)"},
                {"identifier":"FT", "productString":"FT", "displayString":"FT (Upstream Flooding plus Tidal Effects)"},
                {"identifier":"ET", "productString":"ET", "displayString":"ET (Elevated Upstream Flow plus Tidal Effects)"},
                {"identifier":"WT", "productString":"WT", "displayString":"WT (Wind and/or Tidal Effects)"},
                {"identifier":"OT", "productString":"OT", "displayString":"OT (Other Effects)"},
                {"identifier":"MC", "productString":"MC", "displayString":"MC (Other Multiple Causes)"},
                {"identifier":"UU", "productString":"UU", "displayString":"UU (Unknown)"},
                ],
           },
           {
            # INCLUDE 
             "fieldType":"CheckBoxes",
             "fieldName": "include",
             "label": "Include",
             "choices": [
                { "identifier": "ffwEmergency",
                  "displayString": "**FLASH FLOOD EMERGENCY**:",
                  "productString": "...Flash flood emergency for !** LOCATION **!...",
                  "detailFields": [
                      {
                       "fieldType": "Text",
                       "fieldName": "includeFfwEmergencyLocation",
                       "expandHorizontally": True,
                       "maxChars": 40,
                       "visibleChars": 12
                      }]
                },
                {"identifier":"+SM", "displayString":"Also Snow Melt",
                 "productString":"Rapid snow melt is causing flooding.",},
                {"identifier":"-FL", "displayString": "Flooding not directly reported, only heavy rain",
                 "productString": "Flooding is not directly reported, only heavy rains.",},
                 ],
            },
            {
            # EVENT
             "fieldType":"RadioButtons",
             "fieldName": "eventType",
             "label": "Event type (Choose 1):",
             "values": "thunderEvent",
             "choices": [
                {"identifier":"thunderEvent", "displayString":"Thunderstorm(s)",
                 "productString":"thunderstorms producing heavy rain",},
                {"identifier":"rainEvent", "displayString": "Due to only heavy rain",
                 "productString": "heavy rain",},
                {"identifier":"undefEvent", "displayString": "Flash flooding occurring",
                 "productString": "flash flooding occurring",},
                 ],
            },
           {
           # RAIN SO FAR
           "fieldType":"RadioButtons",
           "label":"Rain so far:",
           "fieldName": "rainAmt",
           "choices": [
                {"identifier":"rain1", "displayString":"One inch so far",
                 "productString":"up to one inch of rain has already fallen.",},
                {"identifier":"rain2", "displayString":"Two inches so far",
                 "productString":"up to two inches of rain has already fallen.",},
                {"identifier":"rain3", "displayString":"Three inches so far",
                 "productString":"up to three inches of rain has already fallen.",},
                {"identifier":"rainEdit", "displayString":"",
                 "productString":"!** RAINFALL AMOUNTS **! inches of rain have fallen.",
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
                },
               ], 
            },
            # BASIS
            {
             "fieldType":"RadioButtons",
             "fieldName": "basis",
             "label": "Reported By (Choose 1):",
             "values": "radInd",
             "choices": [    
                     {"identifier":"radInd", "displayString": "Doppler Radar indicated...", 
                      "productString": "Doppler Radar indicated",},
                     {"identifier":"radGagInd", "displayString": "Doppler Radar and automated gauges", 
                      "productString": "Doppler Radar and automated rain gauges indicated",},
                      {"identifier":"wxSpot", "displayString": "Trained weather spotters reported...", 
                      "productString": "Trained weather spotters reported",},
                     {"identifier":"public", "displayString": "Public reported...",
                      "productString": "The public reported",},
                     {"identifier":"lawEnf", "displayString": "Local law enforcement reported...",
                      "productString": "Local law enforcement reported",},
                     {"identifier":"emerMgmt", "displayString": "Emergency management reported...",
                      "productString": "Emergency management reported",},
                       ], 
            },
           # DEBRIS FLOW OPTIONS
            {
             "fieldType":"RadioButtons",
             "fieldName": "debrisFlows",
             "label": "Debris Flow Info:",
             "choices": [    
                     {"identifier":"burnScar", "displayString": "Burn scar area with debris flow through", 
                      "productString": 
                  '''Excessive rainfall over the burn scar will result in debris flow moving
                     through the !** DRAINAGE **!. The debris flow can consist of 
                     rock...mud...vegetation and other loose materials.''',
                     "detailFields": [
                          {
                           "fieldType": "Text",
                           "fieldName": "debrisFlowsBurnScarLocation",
                           "expandHorizontally": True,
                           "maxChars": 40,
                           "visibleChars": 12
                           }]
                     },
                     {"identifier":"mudSlide", "displayString": "Mud Slides", 
                      "productString": 
                  '''Excessive rainfall over the warning area will cause mud slides near steep
                     terrain. The mud slide can consist of rock...mud...vegetation and other
                     loose materials.''',},
                       ], 
            },
           # ADDITIONAL INFO
            {
             "fieldType":"CheckBoxes",
             "fieldName": "additionalInfo",
             "label": "Additional Info:",
             "choices": [    
                     {"identifier":"listOfCities", "displayString": "Select for a list of cities", 
                      "productString": "ARBITRARY ARGUMENTS USED BY CITIES LIST GENERATOR." },
                     {"identifier":"listOfDrainages", "displayString": "Automated list of drainages", 
                      "productString": "ARBITRARY ARGUMENTS USED BY DRAINAGES LIST GENERATOR." },
                     {"identifier":"addtlRain", "displayString": "Additional rainfall of", 
                      "productString": 
                    '''Additional rainfall amounts of !** EDIT AMOUNT **! are possible in the
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
                     },
                     {"identifier":"particularStream",
                      "displayString": "Flood waters are moving down", 
                      "productString": 
                    '''FLood waters are moving down !**NAME OF CHANNEL**! from !**LOCATION**! to 
                       !**LOCATION**!. The flood crest is expected to reach !**LOCATION(S)**! by
                       !**TIME(S)**!.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "additionalInfoParticularStreamName",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            },
                            {
                             "fieldType": "Composite",
                             "fieldName": "additionalInfoParticularStreamRow2",
                             "numColumns": 4,
                             "columnSpacing": 3,
                             "expandHorizontally": True,
                             "fields": [
                                  {
                                   "fieldType": "Label",
                                   "fieldName": "additionalInfoParticularStreamLabel1",
                                   "label": "from"
                                  },
                                  {
                                   "fieldType": "Text",
                                   "fieldName": "additionalInfoParticularStreamFrom",
                                   "expandHorizontally": True,
                                   "maxChars": 40,
                                   "visibleChars": 12
                                  },
                                  {
                                   "fieldType": "Label",
                                   "fieldName": "additionalInfoParticularStreamLabel2",
                                   "label": "to"
                                  },
                                  {
                                   "fieldType": "Text",
                                   "fieldName": "additionalInfoParticularStreamTo",
                                   "expandHorizontally": True,
                                   "maxChars": 40,
                                   "visibleChars": 12
                                  }
                             ]
                            },
                            {
                             "fieldType": "Composite",
                             "fieldName": "additionalInfoParticularStreamRow3",
                             "numColumns": 1,
                             "columnSpacing": 2,
                             "expandHorizontally": True,
                             "fields": [
                                  {
                                   "fieldType": "Label",
                                   "fieldName": "additionalInfoParticularStreamLabel3",
                                   "label": "The flood crest is expected to reach:"
                                  },
                                  {
                                   "fieldType": "Text",
                                   "fieldName": "additionalInfoParticularStreamDestination",
                                   "expandHorizontally": True,
                                   "maxChars": 40,
                                   "visibleChars": 12
                                  }
                             ]
                            },
                            {
                             "fieldName":"additionalInfoParticularStreamTime",
                             "fieldType":"TimeScale",
                             "valueLabels": "by:"
                            }
                      ]
                     },
                     {"identifier":"recedingWater", "displayString": "EXP-CAN:Water is receding",
                      "productString": 
                    '''Flood waters have receded...and are no longer expected to pose a threat
                       to life or property. Please continue to heed any road closures.''',},
                     {"identifier":"rainEnded", "displayString": "EXP-CAN:Heavy rain ended",
                      "productString": 
                    '''The heavy rain has ended...and flooding is no longer expected to pose a threat.''',},
                       ], 
            },
           # CALLS TO ACTION
           {
           "fieldType":"CheckList",
           "label":"Calls to Action (1 or more):",
           "fieldName": "cta",
           "values": "noCTA",
           "choices": [
               {"identifier": "noCTA",
                "displayString": "No call to action",
                "productString": ""
               },
               {"identifier": "actQuicklyCTA",
                "displayString": "Act Quickly...",
                "productString": 
                  "Move to higher ground now. Act quickly to protect your life."
               },
               {"identifier": "childSafetyCTA",
                "displayString": "Child Safety...",
                "productString": 
               '''Keep children away from storm drains...culverts...creeks and streams.
                  Water levels can rise rapidly and sweep children away.'''},
               {"identifier": "nighttimeCTA",
                "displayString": "Nighttime flooding...",
                "productString":
               '''Be especially cautious at night when it is harder to recognize the
                  dangers of flooding.'''},
               {"identifier": "safetyCTA",
                "displayString": "Safety...by foot or motorist",
                "productString":
                   "Do not enter or cross flowing water or water of unknown depth."},
               {"identifier": "turnAroundCTA",
                "displayString": "Turn around...don't drown",
                "productString":
               '''Turn around...dont drown when encountering flooded roads.
                  Most flood deaths occur in vehicles.'''},
               {"identifier": "stayAwayCTA",
                "displayString": "Stay away or be swept away",
                "productString":
               '''Stay away or be swept away. River banks and culverts can become
                  unstable and unsafe.'''},
               {"identifier": "arroyosCTA",
                "displayString": "Arroyos...",
                "productString": 
               '''Remain alert for flooding even in locations not receiving rain.
                  arroyos...Streams and rivers can become raging killer currents
                  in a matter of minutes...even from distant rainfall.'''},
               {"identifier": "burnAreasCTA",
                "displayString": "Burn Areas...",
                "productString": 
               '''Move away from recently burned areas. Life threatening flooding
                  of creeks...roads and normally dry arroyos is likely. The heavy
                  rains will likely trigger rockslides...mudslides and debris flows
                  in steep terrain...especially in and around these areas.'''},
               {
                "identifier": "reportFloodingCTA",
                "displayString": "Report flooding to local law enforcement",
                "productString": 
               '''Please report flooding to your local law enforcement agency when
                  you can do so safely.'''},

# We are leaving these in for convenience, commented out.
#              {"identifier": "autoSafetyCTA",
#               "displayString": "Auto Safety",
#               "productString":
#              '''Flooding is occurring or is imminent. Most flood related deaths
#                 occur in automobiles. Do not attempt to cross water covered bridges...
#                 dips...or low water crossings. Never try to cross a flowing stream...
#                 even a small one...on foot. To escape rising water find another
#                 route over higher ground.'''},
#              {"identifier": "dontDriveCTA",
#               "displayString": "Do not drive into water",
#               "productString":
#              '''Do not drive your vehicle into areas where the water covers the
#                 roadway. The water depth may be too great to allow your car to
#                 cross safely.  Move to higher ground.'''},
#              {"identifier": "camperSafetyCTA",
#               "displayString": "Camper safety",
#               "productString":
#              '''Flooding is occurring or is imminent.  It is important to know where
#                 you are relative to streams...rivers...or creeks which can become
#                 killers in heavy rains.  Campers and hikers should avoid streams
#                 or creeks.'''},
#              {"identifier": "lowSpotsCTA",
#               "displayString": "Low spots in hilly terrain ",
#               "productString": 
#              '''In hilly terrain there are hundreds of low water crossings which 
#                 are potentially dangerous in heavy rain.  Do not attempt to travel
#                 across flooded roads. Find an alternate route.  It takes only a
#                 few inches of swiftly flowing water to carry vehicles away.'''},
#              {"identifier": "powerFloodCTA",
#               "displayString": "Power of flood waters/vehicles",
#               "productString":  
#              '''Do not underestimate the power of flood waters. Only a few
#                 inches of rapidly flowing water can quickly carry away your
#                 vehicle.'''},
#              {"identifier": "ffwMeansCTA",
#               "displayString": "A Flash Flood Warning means...",
#               "productString": 
#              '''A flash flood warning means that flooding is imminent or occurring.
#                 If you are in the warning area move to higher ground immediately.
#                 Residents living along streams and creeks should take immediate
#                 precautions to protect life and property. Do not attempt to cross
#                 swiftly flowing waters or waters of unknown depth by foot or by
#                 automobile.'''},
#              {"identifier": "urbanFloodingCTA",
#               "displayString": "Urban Flooding",
#               "productString": 
#              '''Excessive runoff from heavy rainfall will cause flooding of
#                 small creeks and streams...urban areas...highways...streets
#                 and underpasses as well as other drainage areas and low lying
#                 spots.'''},
#              {"identifier": "ruralFloodingCTA",
#               "displayString": "Rural flooding/small streams",
#               "productString": 
#              '''Excessive runoff from heavy rainfall will cause flooding of
#                 small creeks and streams...country roads...as well as farmland
#                 as well as other drainage areas and low lying spots.'''},
#              {"identifier": "ruralUrbanCTA",
#               "displayString": "Rural and Urban Flooding",
#               "productString": 
#              '''Excessive runoff from heavy rainfall will cause flooding of
#                 small creeks and streams...highways and underpasses.
#                 Additionally...country roads and farmlands along the banks of
#                 creeks...streams and other low lying areas are subject to
#                 flooding.'''},
               ]
           },
        ] + CAP_Fields
        
 
 
 
        
