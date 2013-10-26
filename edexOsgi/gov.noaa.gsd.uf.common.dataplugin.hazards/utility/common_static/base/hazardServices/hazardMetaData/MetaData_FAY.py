

from CAP_Fields import CAP_Fields

# Set the defaults for the CAP Fields
for entry in CAP_Fields:
    for fieldName, values in [
                ("urgency", "Expected"),
                ("severity", "Minor"),
                ("certainty", "Likely"),
                ("responseType", "Avoid"),
                ("WEA_Text", ""),
                ]:
        if entry["fieldName"] == fieldName:
            entry["values"] = values    
            
# Need to put in productStrings

MetaData_FAY = [
           {
            "fieldType":"RadioButtons",
            "fieldName": "advisoryType",
            "label": "If not generic advisory:",
            "choices": [ 
                 {"identifier":"+SS",
                  "displayString": "Small streams",
                  "productString":"Small Stream "},
                 {"identifier":"+US",
                  "displayString": "Urban areas and small streams",
                  "productString":"Urban and Small Stream "},
                 {"identifier":"+AS",
                  "displayString": "Arroyo and small streams",
                  "productString":"Arroyo and Small Stream "},
                 {"identifier":"+HA",
                  "displayString": "Arroyo and small streams",
                  "productString":"Arroyo and Small Stream "}
                    ]
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
                {"identifier":"OT", "productString":"OT", "displayString":"OT (Other Effects)"},
                {"identifier":"UU", "productString":"UU", "displayString":"UU (Unknown)"},
                    ]
            },
            # With this metadata, user must make sure that the hydrologic cause selection
            # makes sense given the immediate cause.  Eventually this needs to be rethought.
            {
             "fieldType":"RadioButtons",
             "fieldName": "hydrologicCause",
             "label":"Hydrologic Cause:",
             "choices": [ 
                {"identifier":"snowMelt", "productString":"Melting snow", 
                  "displayString":"Melting snow"},
                {"identifier":"rainSnow", "productString":"Rain and melting snow", 
                  "displayString":"Rain and melting snow"},
                {"identifier":"iceJam", "productString":"Ice Jam Flooding", 
                  "displayString":"Ice Jam Flooding"},
                {"identifier":"floodGate", "productString":"A dam floodgate release", 
                  "displayString":"Floodgate Release"},
                {"identifier":"glacialOutburst", "productString":"A glacier-dammed lake outburst", 
                  "displayString":"Glacier-dammed lake outburst"},
                {"identifier":"groundWater", "productString":"Ground water flooding", 
                  "displayString":"Ground Water Flooding"},
                {"identifier":"riverRises", "productString":"Rapid river rises", 
                  "displayString":"Rapid River Rises"},
                {"identifier":"badDrainage", "productString":"Minor flooding of poor drainage areas", 
                  "displayString":"Poor Drainage Areas"},
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
                }], 
            },
            # Basis entries have manual edit markers for cause and advisory type; eventually
            # these need to be supplied automatically based on other choices.
            # BASIS
            {
             "fieldType":"RadioButtons",
             "fieldName": "basis",
             "label": "Reported By (Choose 1):",
             "values": "radInd",
             "choices": [    
                     {"identifier":"radInd", "displayString": "Doppler Radar indicated...", 
                      "productString": '''Doppler radar indicated !**HY CAUSE**! that will
                                          cause !**ADV TYPE**! in the advisory area''',},
                     {"identifier":"radarTS", "displayString": "Radar indicated thunderstorm...", 
                      "productString": '''Doppler radar indicated !**HY CAUSE**! due to
                                          thunderstorms. This will cause !**ADV TYPE**! in
                                          the advisory area''',},
                     {"identifier":"radGagInd", "displayString": "Doppler Radar and automated gauges", 
                      "productString": '''Doppler Radar and automated rain guages indicated !**HY CAUSE**!
                                          that will cause !**ADV TYPE**! in the advisory area''',},
                     {"identifier":"radGagTS", "displayString": "Radar and gauges with thunderstorm", 
                      "productString": '''Doppler radar and automated rain gauges indicated !**HY CAUSE**!
                                          due to thunderstorms. This will cause !**ADV TYPE**! in
                                          the advisory area''',},
                      {"identifier":"wxSpot", "displayString": "Weather spotters report flooding in", 
                      "productString": '''Trained weather spotters reported !**HY CAUSE**! causing
                                       !**ADV TYPE**! in !**LOCATION**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisWxSpotLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"wxSpotTS", "displayString": "Weather spotters report thunderstorm in", 
                      "productString": '''Trained weather spotters reported !**HY CAUSE**! in !**LOCATION**!
                                          due to thunderstorms.  This will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisWxSpotTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"wxSpotInc", "displayString": "Weather spotters report incipient flooding in", 
                      "productString": '''Trained weather spotters reported !**HY CAUSE**! in !**LOCATION**!
                                       that will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisWxSpotIncLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"lawEnf", "displayString": "Law enforcement report flooding in",
                      "productString": '''Local law enforcement reported !**HY CAUSE**! causing
                                          !**ADV TYPE**! in !**LOCATION**! ''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisLawEnfLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"lawEnfTS", "displayString": "Law enforcement report thunderstorm in",
                      "productString": '''Local law enforcement reported !**HY CAUSE**! in !**LOCATION**!
                                          due to thunderstorms.  This will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisLawEnfTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"lawEnfInc", "displayString": "Law enforcement report incipient flooding in",
                      "productString": '''Local law enforcement reported !**HY CAUSE**! in
                                          !**LOCATION**! that will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisLawEnfIncLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"emerMgmt", "displayString": "Emergency Mgmt report flooding in",
                      "productString": '''Emergency management reported !**HY CAUSE**! causing
                                          !**ADV TYPE**! in !**LOCATION**! ''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEmerMgmtLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"emerMgmtTS", "displayString": "Emergency Mgmt report thunderstorm in",
                      "productString": '''Emergency management reported !**HY CAUSE**! in !**LOCATION**!
                                          due to thunderstorms.  This will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEmerMgmtTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"emerMgmtInc", "displayString": "Emergency Mgmt report incipient flooding in",
                      "productString": '''Emergency management reported !**HY CAUSE**! in
                                          !**LOCATION**! that will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEmerMgmtIncLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"public", "displayString": "Public report flooding in",
                      "productString": '''The public reported !**HY CAUSE**! causing
                                          !**ADV TYPE**! in !**LOCATION**! ''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisPublicLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"publicTS", "displayString": "Public report thunderstorm in",
                      "productString": '''The public reported !**HY CAUSE**! in !**LOCATION**!
                                          due to thunderstorms.  This will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisPublicTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"publicInc", "displayString": "Public report incipient flooding in",
                      "productString": '''The public reported !**HY CAUSE**! in
                                          !**LOCATION**! that will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisPublicIncLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"satInd", "displayString": "Satellite estimates indicated in", 
                      "productString": '''Satellite estimates indicate !**HY CAUSE**! in
                                          !**LOCATION**! that will cause !**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisSatIndLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      },
                      {"identifier":"satIndTS", "displayString": "Satellite estimates and automated gauges in", 
                      "productString": '''Satellite estimates indicate !**HY CAUSE**! from thunderstorms
                                          over !** LOCATION **! that will cause!**ADV TYPE**!''',
                      "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisSatIndTsLocation",
                             "expandHorizontally": True,
                             "maxChars": 40,
                             "visibleChars": 12
                            }]
                      }
                 ], 
            },
           # ADDITIONAL INFO
            {
             "fieldType":"CheckBoxes",
             "fieldName": "additionalInfo",
             "label": "Additional Info:",
             "choices": [    
                     {"identifier":"listOfCities", "displayString": "Include the list of cities", 
                     },
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
                     {"identifier":"specificPlace",
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
                      },
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
              {"identifier": "dontDriveCTA",
               "displayString": "Do not drive into water",
               "productString":
              '''Do not drive your vehicle into areas where the water covers the
                 roadway. The water depth may be too great to allow your car to
                 cross safely.  Move to higher ground.'''},
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
              {"identifier": "floodAdvisoryMeansCTA",
               "displayString": "A Flood Advisory means...",
               "productString": 
              '''A flood advisory means river or stream flows are elevated...or ponding 
                 of water in urban or other areas is occurring or is imminent. Do not
                 attempt to travel across flooded roads. Find alternate routes. It takes
                 only a few inches of swiftly flowing water to carry vehicles away.'''},
                    ]
            },

        ] + CAP_Fields
