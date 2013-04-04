

MetaData_HydroPoint = [
           {
            "fieldName": "immediateCause",
            "choices":[
                     {
                      "displayString": "DM (Upstream or Levee Failure)",
                      "identifier": "DM" 
                     },
                     {
                      "displayString": "DR (Upstream Dam/Reservoir Release)",
                      "identifier": "DR" 
                     },
                     {
                      "displayString": "ER (Excessive Rainfall)",
                      "identifier": "ER" 
                     },
                     {
                      "displayString": "ET (Elevation Upstream Flow & Tidal)",
                      "identifier": "ET" 
                     },
                     {
                      "displayString": "FS (Upstream Flood & Storm Surge)",
                      "identifier": "FS" 
                     },
                     {
                      "displayString": "FT (Upstream Flood & Tidal)",
                      "identifier": "FT" 
                     },
                     {
                      "displayString": "GO (Glacial Dam Outburst)",
                      "identifier": "GO" 
                     },
                     {
                      "displayString": "IC (Ice Jam, Rain, Snowmelt)",
                      "identifier": "IC" 
                     },
                     {
                      "displayString": "IJ (Ice Jam)",
                      "identifier": "IJ" 
                     },
                     {
                      "displayString": "MC (Other Multiple Causes)",
                      "identifier": "MC" 
                     },
                     {
                      "displayString": "OT (Other Effects)",
                      "identifier": "OT" 
                     },
                     {
                      "displayString": "RS (Rain & Snowmelt)",
                      "identifier": "RS" 
                     },
                     {
                      "displayString": "SM (Snowmelt)",
                      "identifier": "SM" 
                     },
                     {
                      "displayString": "UU (Unknown)",
                      "identifier": "UU" 
                     },
                     {
                      "displayString": "WT (Wind &/or Tidal Effects)",
                      "identifier": "WT" 
                     }
                    ],
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "shortValueLabels":"Cau",
           },
           {
            "fieldName": "floodSeverity",
            "fieldType":"ComboBox",
            "label":"Flood Severity:",
            "shortValueLabels":"Sev",
            "choices":[
                     {
                      "displayString": "N (None)",
                      "identifier": "N",
                      "productString": "",
                     },
                     {
                      "displayString": "0 (Areal Flood or Flash Flood Products)",
                      "identifier": "0",
                      "productString": "",
                     },
                     {
                      "displayString": "1 (Minor)",
                      "identifier": "1", 
                      "productString": "Minor",
                     },
                     {
                      "displayString": "2 (Moderate)",
                      "identifier": "2",
                      "productString": "Moderate",
                     },
                     {
                      "displayString": "3 (Major)",
                      "identifier": "3", 
                      "productString": "Major",
                     },
                     {
                      "displayString": "U (Unknown)",
                      "identifier": "U", 
                      "productString": "",
                     }
                    ],
           },
           {
            "fieldName":"floodRecord",
            "fieldType":"ComboBox",
            "label":"Flood Record Status:",
            "shortValueLabels":"Rec",
            "choices":[
                     {
                      "displayString": "NO (Record Flood Not Expected)",
                      "identifier": "NO" 
                     },
                     {
                      "displayString": "NR (Near Record or Record Flood Expected)",
                      "identifier": "NR" 
                     },
                     {
                      "displayString": "UU (Flood Without a Period of Record to Compare)",
                      "identifier": "UU" 
                     },
                     {
                      "displayString": "OO (for areal flood warnings, areal flash flood products, and flood advisories (point and areal))",
                      "identifier": "OO" 
                     }
                    ],
           },  
           {
            "fieldName":"riseAbove:crest:fallBelow",
            "fieldType":"TimeScale",
            "valueLabels": {
                            "riseAbove": "Rise Above Time:",
                            "crest": "Crest Time:",
                            "fallBelow": "Fall Below Time:"
                           },
            "shortValueLabels": {
                             "riseAbove": "Rise",
                             "crest": "Crest",
                             "fallBelow": "Fall"
                           },
            "relativeValueWeights": {
                             "riseAbove": 3,
                             "crest": 3,
                             "fallBelow": 3
                           },
            "notify":1,
            "spacing": 5,
           },
]