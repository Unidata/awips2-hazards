
from CAP_Fields import CAP_Fields

# Set the defaults for the CAP Fields
for entry in CAP_Fields:
    for fieldName, values in [
                ("urgency", "Immediate"),
                ("responseType", "Avoid"),
                ("severity", "Severe"),
                ("certainty", "Likely"),
                ("WEA_Text", "Flash Flood Warning this area %s. Avoid flood areas. Check local media. -NWS"),
                ]:
        if entry["fieldName"] == fieldName:
            entry["values"] = values            


MetaData_FFW_NonConvective = [
            {             
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "DM",
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
             "label": "",
             "choices": [
                { "identifier": "ffwEmergency",
                  "displayString": "**SELECT FOR FLASH FLOOD EMERGENCY**",
                  "productString": "...Flash flood emergency for !** LOCATION **!..."
                },
                 ],
            },
           {
            # SEVERITY 
             "fieldType":"RadioButtons",
             "fieldName": "include",
             "label": "Severity",
             "choices": [
                { "identifier": "sev1", "productString": "1",
                  "displayString": "Minor flood"},
                { "identifier": "sev2", "productString": "2",
                  "displayString": "Moderate flood"},
                { "identifier": "sev3", "productString": "3",
                  "displayString": "Major flood"},
                { "identifier": "sevUnk", "productString": "U",
                  "displayString": "Severity Unknown"},
                 ],
            },
            # BASIS
            {
             "fieldType":"RadioButtons",
             "fieldName": "basis",
             "label": "Reported By (Choose 1):",
             "values": "lawEnf",
             "choices": [    
                     {"identifier":"countyDisp", "displayString": "County dispatch...", 
                      "productString": "County dispatch reported",},
                     {"identifier":"lawEnf", "displayString": "Law enforcement...",
                      "productString": "Local law enforcement reported",},
                      {"identifier":"corpsEng", "displayString": "Corps of engineers...", 
                      "productString": "Corps of engineers reported",},
                      {"identifier":"wxSpot", "displayString": "Dam operator...", 
                      "productString": "Dam operators reported",},
                     {"identifier":"emerMgmt", "displayString": "Bureau of reclamation...",
                      "productString": "Bureau of reclamation reported",},
                     {"identifier":"public", "displayString": "Public reported...",
                      "productString": "The public reported",},
                       ], 
            },
           # ADDITIONAL INFO
             {
              "fieldType":"CheckList",
              "fieldName": "additionalInfo",
              "label": "Additional Info:",
              "choices": [    
                     {"identifier":"listOfCities", "displayString": "Select for a list of cities", 
                       "productString": "ARBITRARY ARGUMENTS USED BY CITIES LIST GENERATOR." },
                     {"identifier":"pathCast", "displayString": "Select for pathcast", 
                       "productString": "The flood will be near..." },
                     {"identifier":"listOfDrainages", "displayString": "Automated list of drainages", 
                      "productString": "ARBITRARY ARGUMENTS USED BY DRAINAGES LIST GENERATOR." },
                     {"identifier":"particularStream",
                      "displayString": "Flooding is occurring in a particular stream/river", 
                      "productString": 
                    '''FLood waters are moving down !**NAME OF CHANNEL**! from !**LOCATION**! to 
                       !**LOCATION**!. The flood crest is expected to reach !**LOCATION(S)**! by
                       !**TIME(S)**!.''',},
                     {"identifier":"endGeneric", "displayString": "EXP-CAN:Generic Statement",
                      "productString": 
                    '''The water is receding...and is no longer expected to pose a significant threat.
                       Please continue to heed any road closures.''',},
                     {"identifier":"endRiver", "displayString": "EXP-CAN:River Flooding",
                      "productString": 
                    '''Flooding on the !** **! river has receded and is no longer expected to pose
                       a significant threat. Please continue to heed all road closures.''',},
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
                ]
           }, 
        ] + CAP_Fields
