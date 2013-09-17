
from CAP_Fields import CAP_Fields

# Set the defaults for the CAP Fields
for entry in CAP_Fields:
    for fieldName, values in [
                ("urgency", "Expected"),
                ("severity", "Minor"),
                ("certainty", "Observed"),
                ("responseType", "Avoid"),
                ("WEA_Text", ""),
                ]:
        if entry["fieldName"] == fieldName:
            entry["values"] = values    
            
            
MetaData_FLY = [
            {
             "fieldName": "pointID",
             "fieldType": "Text",
             "label": "Forecast Point:",
             "length": 5,
             "values": "XXXXX"
            },
            {             
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "ER",
            "choices": [
                {"displayString": "ER (Excessive Rainfall)","productString": "ER","identifier": "ER",},
                {"displayString": "SM (Snow Melt)", "productString": "SM","identifier": "SM",},
                {"displayString": "RS (Rain and Snow Melt)", "productString": "RS","identifier": "RS",},
                {"displayString": "DM (Dam or Levee Failure)","productString": "DM","identifier": "DM",},
                {"displayString": "DR (Upstream Dam Release)","productString": "DR","identifier": "DR",},
                {"displayString": "GO (Glacier-Dammed Lake Outburst)","productString": "GO","identifier": "GO",},
                {"displayString": "IJ (Ice Jam)", "productString": "IJ","identifier": "IJ",},
                {"displayString": "IC (Rain and/or Snow melt and/or Ice Jam)","productString": "IC","identifier": "IC",},
                {"displayString": "FS (Upstream Flooding plus Storm Surge)", "productString": "FS","identifier": "FS",},
                {"displayString": "FT (Upstream Flooding plus Tidal Effects)","productString": "FT","identifier": "FT",},
                {"displayString": "ET (Elevated Upstream Flow plus Tidal Effects)","productString": "ET","identifier": "ET",},
                {"displayString": "WT (Wind and/or Tidal Effects)","productString": "WT","identifier": "WT",},
                {"displayString": "OT (Other Effects)","identifier": "OT","productString": "OT",},
                {"displayString": "MC (Other Multiple Causes)","identifier": "MC"},
                {"displayString": "UU (Unknown)", "productString": "UU","identifier": "UU",},
                ],
           },
           {
            "fieldName": "floodSeverity",
            "fieldType":"ComboBox",
            "label":"Flood Severity:",
            "shortValueLabels":"Sev",
            "choices":[
                     {"displayString": "N (None)","identifier": "N","productString": "",},
                     {"displayString": "0 (Areal Flood or Flash Flood Products)","identifier": "0","productString": "",},
                     {"displayString": "1 (Minor)","identifier": "1", "productString": "Minor",},
                     {"displayString": "2 (Moderate)","identifier": "2","productString": "Moderate",},
                     {"displayString": "3 (Major)","identifier": "3", "productString": "Major",},
                     {"displayString": "U (Unknown)","identifier": "U","productString": "",}
                    ],
           },
           {
            "fieldName":"floodRecord",
            "fieldType":"ComboBox",
            "label":"Flood Record Status:",
            "shortValueLabels":"Rec",
            "choices":[
                     {"displayString": "NO (Record Flood Not Expected)","identifier": "NO" },
                     {"displayString": "NR (Near Record or Record Flood Expected)","identifier": "NR"},
                     {"displayString": "UU (Flood Without a Period of Record to Compare)","identifier": "UU" },
                     {"displayString": "OO (for areal flood warnings, areal flash flood products, and flood advisories (point and areal))","identifier": "OO" }
                    ],
           },  
           {
            "fieldName":"riseAbove:crest:fallBelow",
            "fieldType":"TimeScale",
            "valueLabels": {"riseAbove": "Rise Above Time:","crest": "Crest Time:","fallBelow": "Fall Below Time:"},
            "shortValueLabels": {"riseAbove": "Rise","crest": "Crest","fallBelow": "Fall"},
            "relativeValueWeights": {"riseAbove": 3,"crest": 3,"fallBelow": 3},
            "notify":1,
            "spacing": 5,
           },

           # The following product sections contain logic and/or 
           # information from the hydro database
           # and must be created by the product generator:
           # Headline, Crest History, Impact, Data Round-up, Summary, Tabular

           # Basis
           {                      
             "fieldType":"RadioButtons",
             "label":"Basis (Choose 1):",
             "fieldName": "basis",
             "lines": 2,
             "values": "Default",
             "choices": [ 
                     {"displayString": "The current weather is...",
                      "productString": "The current weather is dominated by a !** EDIT HYDROMETEOROLOGICAL BASIS HERE **!. This weather system will produce rainfall amounts ranging from  !**EDIT RAINFALL AMOUNTS **!",
                      "identifier": "Current Weather" },
                     {"displayString": "Default basis statement...",
                      "productString": "!** INSERT HYDROMETEOROLOGICAL BASIS HERE **!",
                      "identifier": "Default"},
                    ]
            },
            # Calls to action                
            {                      
             "fieldType":"CheckList",
             "label":"Calls to Action (1 or more):",
             "fieldName": "cta",
             "lines": 3,
             "values": ["Flooded Roadways"],
             "choices": [ 
                     {"displayString": "No call to action.", 
                      "productString": "No call to action.",
                      "identifier": "None"},
                     {"displayString": "Do not drive through flooded areas...",
                      "productString": "Motorists should not attempt to drive around barricades or drive cars through flooded areas.",
                      "identifier": "Flooded Roadways"},
                     {"displayString": "Walking near riverbanks...",
                      "productString": "Caution is urged when walking near riverbanks.",
                      "identifier": "Riverbanks"},
                     {"displayString": "Turn around...don't drown.",
                      "productString": "Turn around...don't drown.",
                      "identifier": "TADD"},
                     {"displayString": "Stay tuned to NOAA Weather Radio for further information...",
                      "productString": "Stay tuned to further developments by listening to your local radio... television... or NOAA Weather Radio for further information.",
                      "identifier": "NOAA Weather Radio"},
                     {"displayString": "Use caution when driving at night...", 
                      "productString": "Be especially cautious at night when it is harder to recognize the dangers of floods and flash floods.",
                      "identifier": "Driving at Night"},
                     {"displayString": "Most flood deaths occur in automobiles...",
                      "productString": "Most flood deaths occur in automobiles.  Do not attempt to cross bridges... dips... or low spots if water covers the roadway.",
                      "identifier": "Automobile Deaths"},
                     {"displayString": "To escape rising water...",
                      "productString": "To escape rising water... take the shortest path to higher ground.",
                      "identifier": "Rising Water"},
                     {"displayString": "Force of fast-moving flood water...",
                      "productString": "Even 6 inches of fast-moving flood water can knock you off your feet... and a depth of 2 feet will float your car.  Never try to walk... swim... or drive through such swift water.  If you come upon flood waters... stop... turn around and go another way.",
                      "identifier": "Fast-moving Water"},
                     {"displayString": "Last river flood statement on this event...", 
                      "productString": "This will be the last river flood statement on this event.  Stay tuned to developments.",
                      "identifier": "Last Statement"},
                     {"displayString": "A flood advisory means...",
                      "productString": "A flood advisory means minor flooding is possible and rivers are forecast to exceed bankfull.  If you are in the advisory area remain alert to possible flooding... or the possibility of the advisory being upgraded to a warning.",
                      "identifier": "Flood Advisory Definition"},
                     {"displayString": "Report observed flooding...",
                      "productString": "Report observed flooding to local emergency services or law enforcement and request they pass this information on to the National Weather Service.",
                      "identifier": "Observed Flooding"}                     
                    ]
            }
        ] + CAP_Fields
