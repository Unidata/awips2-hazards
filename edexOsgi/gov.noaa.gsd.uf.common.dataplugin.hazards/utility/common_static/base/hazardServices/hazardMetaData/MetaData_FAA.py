
from CAP_Fields import CAP_Fields

# Set the defaults for the CAP Fields
for entry in CAP_Fields:
    for fieldName, values in [
                ("urgency", "Future"),
                ("severity", "Severe"),
                ("certainty", "Possible"),
                ("responseType", "Prepare"),
                ("WEA_Text", ""),
                ]:
        if entry["fieldName"] == fieldName:
            entry["values"] = values            

MetaData_FAA = [
            {             
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": "ER",
            "expandHorizontally": True,
            "choices": [
                {"displayString": "ER (Excessive Rainfall)","productString": "ER","identifier": "ER"},
                {"displayString": "SM (Snow Melt)", "productString": "SM","identifier": "SM"},
                {"displayString": "RS (Rain and Snow Melt)", "productString": "RS","identifier": "RS"},
                {"displayString": "DM (Dam or Levee Failure)","productString": "DM","identifier": "DM"},
                {"displayString": "DR (Upstream Dam Release)","productString": "DR","identifier": "DR"},
                {"displayString": "GO (Glacier-Dammed Lake Outburst)","productString": "GO","identifier": "GO"},
                {"displayString": "IJ (Ice Jam)", "productString": "IJ","identifier": "IJ"},
                {"displayString": "IC (Rain and/or Snow melt and/or Ice Jam)","productString": "IC","identifier": "IC"},
                {"displayString": "FS (Upstream Flooding plus Storm Surge)", "productString": "FS","identifier": "FS"},
                {"displayString": "FT (Upstream Flooding plus Tidal Effects)","productString": "FT","identifier": "FT"},
                {"displayString": "ET (Elevated Upstream Flow plus Tidal Effects)","productString": "ET","identifier": "ET"},
                {"displayString": "WT (Wind and/or Tidal Effects)","productString": "WT","identifier": "WT"},
                {"displayString": "OT (Other Effects)","identifier": "OT","productString": "OT"},
                {"displayString": "MC (Other Multiple Causes)","identifier": "MC"},
                {"displayString": "UU (Unknown)", "productString": "UU","identifier": "UU"},
                ],
           },
            {                      
             "fieldType":"CheckList",
             "label":"Calls to Action (1 or more):",
             "fieldName": "cta",
             "lines": 3,
             'values': ['FloodWatch'],
             "choices": [
                     {"displayString": "A Flood Watch means...", "identifier": "FloodWatch",
                      "productString":  '''A FLOOD WATCH MEANS THERE IS A POTENTIAL FOR FLOODING BASED ON CURRENT FORECASTS.\n\nYOU SHOULD MONITOR LATER FORECASTS AND BE ALERT FOR POSSIBLE FLOOD WARNINGS. THOSE LIVING IN AREAS PRONE TO FLOODING SHOULD BE PREPARED TO TAKE ACTION SHOULD FLOODING DEVELOP.''',},
                    ]
            },
                
        ] + CAP_Fields
