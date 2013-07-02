
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
             "fieldName": "include",
             "label": "Include:",
             "choices": [ 
                     {"displayString": "Small streams",},
                     {"displayString": "Urban areas and small streams",}
                    ]
            },
            {
             "fieldName": "miscLabel",
             "fieldType":"Label",
             "label":"If either 'include small streams' or 'include urban areas and small streams' above was selected, 'floodgate opening' in primary cause below should not be selected.  If 'floodgate opening' is the desired option, do not select 'include small streams' or 'include urban areas and small streams'. ",
            },
            {
             "fieldType":"RadioButtons",
             "fieldName": "primaryCause",
             "label":"Primary Cause other than rain:",
             "choices": [ 
                     {"displayString": "Snow melt",},
                     {"displayString": "Rain and Snow Melt",},
                     {"displayString": "Dam break", },
                     {"displayString": "Flood gate opening",},
                     {"displayString": "Ice Jam",},
                     {"displayString": "Ice Jam/Rain/Snow melt",},
                     {"displayString": "Glacial Lake Outburst",},
                     {"displayString": "Multiple Causes",},
                     {"displayString": "Unknown Cause",}
                    ]
            },
            {                      
             "fieldType":"CheckList",
             "label":"Calls to Action (1 or more):",
             "fieldName": "cta",
             "lines": 3,
             "choices": [ 
                     {"displayString": "No call to action", },
                     {"displayString": "Automated list of drainages",},
                     {"displayString": "Additional rainfall expected",}, #(need dialog to specify how much)"
                     {"displayString": "Turn around...don't drown",},
                     {"displayString": "Urban flooding",},
                     {"displayString": "Rural flooding/small streams", },
                     {"displayString": "Flooding is occurring in a particular stream/river",},
                     {"displayString": "Nighttime flooding",},
                     {"displayString": "Do not drive into water",},
                     {"displayString": "A Flood Warning means...", },
                     {"displayString": "Camper safety",},
                     {"displayString": "Low spots in hilly terrain",},
                     {"displayString": "Power of flood waters/vehicles",},
                     {"displayString": "Report flooding to local law enforcement",}
                    ]
            },
        ] + CAP_Fields
