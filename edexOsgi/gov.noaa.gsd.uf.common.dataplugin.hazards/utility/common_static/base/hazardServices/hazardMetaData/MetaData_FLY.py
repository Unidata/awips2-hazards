
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
        ] + CAP_Fields
