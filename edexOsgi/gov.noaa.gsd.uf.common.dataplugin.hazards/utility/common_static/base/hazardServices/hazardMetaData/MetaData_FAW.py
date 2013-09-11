
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
             "label":"If either 'include small streams' or 'include urban areas and small streams' above was selected, 'floodgate opening' in primary cause below should not be selected.  If 'floodgate opening' is the desired option, do not select 'include small streams' or 'include urban areas and small streams'. ",
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
                {"identifier":"rainEdit", "displayString":"User defined amount",
                 "productString":"!** RAINFALL AMOUNTS **! inches of rain have fallen.",},
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
                      {"identifier":"wxSpot", "displayString": "Weather spotters report flooding...", 
                      "productString": "Trained weather spotters reported flooding in !** LOCATION **!.",},
                      {"identifier":"wxSpotHR", "displayString": "Weather spotters report heavy rain...", 
                      "productString": '''Trained weather spotters reported heavy rain in !** LOCATION **!
                                          that will cause flooding.''',},
                      {"identifier":"wxSpotTS", "displayString": "Weather spotters report heavy thunderstorm rain...", 
                      "productString": '''Trained weather spotters reported heavy rain in !** LOCATION **!
                                          due to thunderstorms that will cause flooding. ''',},
                     {"identifier":"lawEnf", "displayString": "Law enforcement reported flooding...",
                      "productString": "Local law enforcement reported flooding in !** LOCATION **!.",},
                     {"identifier":"lawEnfHR", "displayString": "Law enforcement reported heavy rain...",
                      "productString": '''Local law enforcement reported heavy rain in !** LOCATION **!
                                          that will cause flooding.''',},
                     {"identifier":"lawEnfTS", "displayString": "Law enforcement reported heavy thunderstorm rain...",
                      "productString": '''Local law enforcement reported thunderstorms with heavy rain
                                          over !** LOCATION **! that will cause flooding.''',},
                     {"identifier":"emerMgmt", "displayString": "Emergency Mgmt reported flooding...",
                      "productString": "Emergency management reported flooding in !** LOCATION **!.",},
                     {"identifier":"emerMgmtHR", "displayString": "Emergency Mgmt reported heavy rain...",
                      "productString": '''Emergency management reported heavy rain in !** LOCATION **!. The
                                          heavy rain will cause flooding.''',},
                     {"identifier":"emerMgmtTS", "displayString": "Emergency Mgmt reported heavy thunderstorm rain...",
                      "productString": '''Emergency management reported thunderstorms with heavy rain
                                          in !** LOCATION **!. The heavy rain will cause flooding.''',},
                     {"identifier":"public", "displayString": "Public reported flooding...",
                      "productString": "The public reported flooding in !** LOCATION **!.",},
                     {"identifier":"publicHR", "displayString": "Public reported heavy rain...",
                      "productString": '''The public reported heavy rain in !** LOCATION **!. That
                                          heavy rain will cause flooding.''',},
                     {"identifier":"publicTS", "displayString": "Public reported heavy thunderstorm rain...",
                      "productString": '''The public reported thunderstorms with heavy rain in
                                          !** LOCATION **!. The heavy rain will cause flooding.''',},
                     {"identifier":"satInd", "displayString": "Satellite estimates indicated...", 
                      "productString": '''Satellite estimates indicate heavy rain in !** LOCATION **!.
                                          That heavy rain will cause flooding.''',},
                     {"identifier":"satGagInd", "displayString": "Satellite estimates and automated gauges", 
                      "productString": '''Satellite estimates and rain gauge data indicate heavy 
                                          rainfall that will cause flooding in the warning area.''',},
                       ], 
            },
            {
             'fieldType':'Text',
             'fieldName': 'basisLocation',
             'label': 'Reported Location:',
             'values': '',
             'length': 90,
             },
           # ADDITIONAL INFO
            {
             "fieldType":"CheckList",
             "fieldName": "additionalInfo",
             "label": "Additional Info:",
             "choices": [    
                     {"identifier":"listOfCities", "displayString": "Include the list of cities", 
                     },
                     {"identifier":"listOfDrainages", "displayString": "Include the Automated list of drainages", 
                     },
                     {"identifier":"addtlRain", "displayString": "Additional rainfall of XX inches expected", 
                      "productString": 
                    '''Additional rainfall amounts of !** EDIT AMOUNT **! are possible in the
                       warned area.''',},
                     {"identifier":"particularStream",
                      "displayString": "Flooding is occurring in a particular stream/river", 
                      "productString": 
                    '''FLood waters are moving down !**NAME OF CHANNEL**! from !**LOCATION**! to 
                       !**LOCATION**!. The flood crest is expected to reach !**LOCATION(S)**! by
                       !**TIME(S)**!.''',},
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
