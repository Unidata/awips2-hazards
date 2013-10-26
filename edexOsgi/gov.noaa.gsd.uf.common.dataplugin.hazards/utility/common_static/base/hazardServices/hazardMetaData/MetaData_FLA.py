
from CAP_Fields import CAP_Fields

# Set the defaults for the CAP Fields
for entry in CAP_Fields:
    for fieldName, values in [
                ("urgency", "Future"),
                ("responseType", "Prepare"),
                ("severity", "Severe"),
                ("certainty", "Possible"),
                ("WEA_Text", ""),
                ]:
        if entry["fieldName"] == fieldName:
            entry["values"] = values            


MetaData_FLA = [
           {
             "fieldName": "pointID",
             "fieldType": "Text",
             "label": "Forecast Point:",
             "maxChars": 5,
             "values": "XXXXX"
            },                
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
             "fieldName": "floodSeverity",
             "fieldType":"ComboBox",
             "label":"Flood Severity:",
             "shortValueLabels":"Sev",
             "expandHorizontally": True,
             "choices":[
                      {"displayString": "N (None)","identifier": "N","productString": ""},
                      {"displayString": "0 (Areal Flood or Flash Flood Products)","identifier": "0","productString": ""},
                      {"displayString": "1 (Minor)","identifier": "1", "productString": "Minor"},
                      {"displayString": "2 (Moderate)","identifier": "2","productString": "Moderate"},
                      {"displayString": "3 (Major)","identifier": "3", "productString": "Major"},
                      {"displayString": "U (Unknown)","identifier": "U","productString": ""}
                     ],
            },
            {
             "fieldName":"floodRecord",
             "fieldType":"ComboBox",
             "label":"Flood Record Status:",
             "shortValueLabels":"Rec",
             "expandHorizontally": True,
             "choices":[
                      {"displayString": "NO (Record Flood Not Expected)","identifier": "NO"},
                      {"displayString": "NR (Near Record or Record Flood Expected)","identifier": "NR"},
                      {"displayString": "UU (Flood Without a Period of Record to Compare)","identifier": "UU"},
                      {"displayString": "OO (for areal flood warnings, areal flash flood products, and flood advisories (point and areal))","identifier": "OO"},
                     ],
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
                     {"displayString": "Flooding due to heavy rain is possible...",
                      "productString": "Flooding due to heavy rain is possible. The exact amount...intensity...timing...and location of the rain that will occur is still uncertain. Once there is more certainty...a flood warning or advisory will be issued.",
                      "identifier": "Heavy rain" },
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
             "values": ["NOAA Weather Radio"],
             "choices": [ 
                     {"displayString": "No call to action.",
                      "productString": "No call to action.",
                      "identifier": "None"},   
                     {"displayString": "Stay tuned to NOAA Weather Radio for further information...",
                      "productString": "Stay tuned to further developments by listening to your local radio... television... or NOAA Weather Radio for further information.",
                      "identifier": "NOAA Weather Radio"},                     
                     {"displayString": "A flood watch means...",
                      "productString": "A flood watch means flooding is possible but not imminent.  If you are in the watch area remain alert to possible flooding.  Residents and those with interests near the river should monitor rising water levels and be prepared for possible flood warnings.",
                      "identifier": "Flood Watch Defined"},
                     {"displayString": "Report observed flooding...",
                      "productString": "Report observed flooding to local emergency services or law enforcement and request they pass this information on to the National Weather Service.",
                      "identifier": "Report Flooding"}
                    ]
            }
        ] + CAP_Fields
