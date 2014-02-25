
from CAP_Fields import CAP_Fields

# Set the defaults for the CAP Fields
for entry in CAP_Fields:
    for fieldName, values in [
                ("urgency", "Expected"),
                ("severity", "Severe"),
                ("certainty", "Likely"),
                ("responseType", "None"),
                ("WEA_Text", ""),
                ]:
        if entry["fieldName"] == fieldName:
            entry["values"] = values    
            
# Need to put in productStrings

MetaData_FAW = [
            {
             "fieldType":"RadioButtons",
             "fieldName": "advisoryType",
             "label": "Include:",
             "choices": [ 
                 {"identifier":"+SS",
                  "displayString": "Small streams",
                  "productString":"Small Stream "},
                 {"identifier":"+US",
                  "displayString": "Urban areas and small streams",
                  "productString":"Urban and Small Stream "}
                 ]
            },
            {
             "fieldName": "miscLabel",
             "fieldType":"Label",
             "wrap": True,
             "label":"If either 'include small streams' or 'include urban areas and small streams' above was selected, 'floodgate opening' in immediate cause below should not be selected.  If 'floodgate opening' is the desired option, do not select 'include small streams' or 'include urban areas and small streams'. ",
            },
            {
             "fieldType":"RadioButtons",
             "fieldName": "immediateCause",
             "label":"Primary Cause:",
             "values": "ER",
             "choices": [ 
                {"identifier":"ER", "productString":"ER", "displayString":"ER (Excessive Rainfall)"},
                {"identifier":"SM", "productString":"SM", "displayString":"SM (Snow Melt)"},
                {"identifier":"RS", "productString":"RS", "displayString":"RS (Rain and Snow Melt)"},
                {"identifier":"DM", "productString":"DM", "displayString":"DM (Dam or Levee Failure)"},
                {"identifier":"DR", "productString":"DR", "displayString":"DR (Flood gate opening)"},
                {"identifier":"GO", "productString":"GO", "displayString":"GO (Glacier-Dammed Lake Outburst)"},
                {"identifier":"IJ", "productString":"IJ", "displayString":"IJ (Ice Jam)"},
                {"identifier":"IC", "productString":"IC", "displayString":"IC (Rain and/or Snow melt and/or Ice Jam)"},
                {"identifier":"MC", "productString":"MC", "displayString":"MC (Other Multiple Causes)"},
                {"identifier":"UU", "productString":"UU", "displayString":"UU (Unknown)"},
                    ]
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
                }]
            },
            # BASIS
            {
             "fieldType":"RadioButtons",
             "fieldName": "basis",
             "label": "Reported By (Choose 1):",
             "values": "radInd",
             "choices": [    
                     {"identifier":"radInd", "displayString": "Doppler Radar indicated...", 
                      "productString": "Doppler radar indicated heavy rain that will cause flooding.",},
                     {"identifier":"radarTS", "displayString": "Radar indicated thunderstorm...", 
                      "productString": '''Doppler radar indicated thunderstorms producing heavy
                                          rain which will cause flooding.''',},
                     {"identifier":"radGagInd", "displayString": "Doppler Radar and automated gauges", 
                      "productString": '''Doppler Radar and automated rain guages indicated that heavy rain
                                          was falling over the area. that heavy rain will cause flooding.''',},
                     {"identifier":"radGagTS", "displayString": "Radar and gauges with thunderstorm", 
                      "productString": '''Doppler radar and automated rain gauges indicated thunderstorms
                                          with heavy rain over the area. That rain will cause flooding.''',},
                      {"identifier":"wxSpot", "displayString": "Weather spotters report flooding in", 
                      "productString": "Trained weather spotters reported flooding in !** LOCATION **!.",
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisWxSpotLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"wxSpotHR", "displayString": "Weather spotters report heavy rain in", 
                      "productString": '''Trained weather spotters reported heavy rain in !** LOCATION **!
                                          that will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisWxSpotHrLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"wxSpotTS", "displayString": "Weather spotters report thunderstorm in", 
                      "productString": '''Trained weather spotters reported heavy rain in !** LOCATION **!
                                          due to thunderstorms that will cause flooding. ''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisWxSpotTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"lawEnf", "displayString": "Law enforcement report flooding in",
                      "productString": "Local law enforcement reported flooding in !** LOCATION **!.",
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisLawEnfLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"lawEnfHR", "displayString": "Law enforcement report heavy rain in",
                      "productString": '''Local law enforcement reported heavy rain in !** LOCATION **!
                                          that will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisLawEnfHrLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"lawEnfTS", "displayString": "Law enforcement report thunderstorm in",
                      "productString": '''Local law enforcement reported thunderstorms with heavy rain
                                          over !** LOCATION **! that will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisLawEnfTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"emerMgmt", "displayString": "Emergency Mgmt report flooding in",
                      "productString": "Emergency management reported flooding in !** LOCATION **!.",
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEmerMgmtLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"emerMgmtHR", "displayString": "Emergency Mgmt report heavy rain",
                      "productString": '''Emergency management reported heavy rain in !** LOCATION **!. The
                                          heavy rain will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEmerMgmtHrLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"emerMgmtTS", "displayString": "Emergency Mgmt report thunderstorm in",
                      "productString": '''Emergency management reported thunderstorms with heavy rain
                                          in !** LOCATION **!. The heavy rain will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEmerMgmtTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"public", "displayString": "Public report flooding in",
                      "productString": "The public reported flooding in !** LOCATION **!.",
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisPublicLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"publicHR", "displayString": "Public report heavy rain in",
                      "productString": '''The public reported heavy rain in !** LOCATION **!. That
                                          heavy rain will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisPublicHrLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"publicTS", "displayString": "Public report thunderstorm in",
                      "productString": '''The public reported thunderstorms with heavy rain in
                                          !** LOCATION **!. The heavy rain will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisPublicTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"satInd", "displayString": "Satellite estimates indicate heavy rain in", 
                      "productString": '''Satellite estimates indicate heavy rain in !** LOCATION **!.
                                          That heavy rain will cause flooding.''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisSatIndLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"satGagInd", "displayString": "Satellite estimates and automated gauges", 
                      "productString": '''Satellite estimates and rain gauge data indicate heavy 
                                          rainfall that will cause flooding in the warning area.'''}
                       ]
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
                             "fieldType":"Time",
                             "label": "by:",
                             "fullWidthOfColumn": False
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
              {"identifier": "floodWwarningMeansCTA",
               "displayString": "A Flood Warning means...",
               "productString": 
              '''A flood warning means that flooding is imminent or has been reported. Stream rises
                 will be slow and flash flooding is not expected. However...all interested parties
                 should take necessary precautions immediately. '''},
               {"identifier": "turnAroundCTA",
                "displayString": "Turn around...don't drown",
                "productString":
               '''Most flood deaths occur in automobiles. Never drive your vehicle into areas where
                  the water covers the roadway. Flood waters are usually deeper than they appear.
                  Just one foot of flowing water is powerful enough to sweep vehicles off the road.
                  When encountering flooded roads make the smart choice...Turn around...Dont drown.'''},
              {"identifier": "urbanFloodingCTA",
               "displayString": "Urban Flooding",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause ponding of water in urban areas...
                 highways...streets and underpasses as well as other poor drainage areas and low
                 lying spots. Do not attempt to travel across flooded roads. Find alternate routes.
                 It takes only a few inches of swiftly flowing water to carry vehicles away.'''},
              {"identifier": "ruralFloodingCTA",
               "displayString": "Rural flooding/small streams",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of small creeks and
                 streams...As well as farm and country roads. Do not attempt to travel across
                 flooded roads. Find alternate routes.'''},
              {"identifier": "ruralUrbanCTA",
               "displayString": "Flooding of rural and urban areas",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of
                 small creeks and streams...highways and underpasses in urban areas.
                 Additionally...country roads and farmlands along the banks of
                 creeks...streams and other low lying areas are subject to
                 flooding.'''},
               {"identifier": "nighttimeCTA",
                "displayString": "Nighttime flooding...",
                "productString":
               '''Be especially cautious at night when it is harder to recognize the
                  dangers of flooding. If flooding is observed act quickly. move up to higher
                  ground to escape flood waters. Do not stay in areas subject to flooding
                  when water begins rising.'''},
              {"identifier": "dontDriveCTA",
               "displayString": "Do not drive into water",
               "productString":
              '''Do not drive your vehicle into areas where the water covers the
                 roadway. The water depth may be too great to allow your car to
                 cross safely.  Move to higher ground.'''},
              {"identifier": "camperSafetyCTA",
               "displayString": "Camper safety",
               "productString":
              '''Flooding is occurring or is imminent.  It is important to know where
                 you are relative to streams...rivers...or creeks which can become
                 killers in heavy rains.  Campers and hikers should avoid streams
                 or creeks.'''},
              {"identifier": "lowSpotsCTA",
               "displayString": "Low spots in hilly terrain ",
               "productString": 
              '''In hilly terrain there are hundreds of low water crossings which 
                 are potentially dangerous in heavy rain.  Do not attempt to travel
                 across flooded roads. Find alternate routes.  It takes only a
                 few inches of swiftly flowing water to carry vehicles away.'''},
              {"identifier": "powerFloodCTA",
               "displayString": "Power of flood waters/vehicles",
               "productString":  
              '''Do not underestimate the power of flood waters. Only a few
                 inches of rapidly flowing water can quickly carry away your
                 vehicle.'''},
               {"identifier": "reportFloodingCTA",
                "displayString": "Report flooding to local law enforcement",
                "productString": 
               '''To report flooding...have the nearest law enforcement agency relay
                  your report to the National Weather Service forecast office.'''},
                    ]
            },
        ] + CAP_Fields
