

# Need to put in productStrings 

MetaData_ShortFused =  [
           {
            "fieldName":"backup",
            "choices":[ "FSL", "PUB", "GJT", "CYS" ],
            "label":"Backup:",
            "fieldType":"ComboBox",
           },
           {
            "fieldName":"basis",
            "fieldType":"RadioButtons",
            "label":"Basis for Warning (Choose 1):",
            "choices":[
                     {
                      "displayString": "Doppler radar indicated",
                     },
                     {
                      "displayString": "Confirmed tornado (WSR-88D was tracking a tornado)",
                     },
                     {
                      "displayString": "Confirmed large tornado",
                     },
                     {
                      "displayString": "Weather spotters reported a tornado...",
                     },
                     {
                      "displayString": "Law enforcement reported a tornado...",
                     },
                     {
                      "displayString": "Public reported a tornado",
                     },
                     {
                      "displayString": "Spotters reported a funnel cloud",
                     },
                     {
                      "displayString": "pathcast"
                     }
                    ],
           },
           {
            "fieldName":"cta",
            "fieldType":"CheckBoxes",
            "label":"Calls to Action (1 or more):",
            "choices":[
                     {
                      "displayString": "No call to action",
                     },
                     {
                      "displayString": "Hail/straight line winds in addition to tornado",
                     },
                     {
                      "displayString": "Doppler radar indicated tornado means...",
                     },
                     {
                      "displayString": "Severe tornado",
                     },
                     {
                      "displayString": "Do not use highway overpasses",
                     }
                    ],
           },
]
