

MetaData_FFW_NonConvective = [
            {             
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "shortValueLabels":"Cau",
            "choices": [
                {
                 "displayString": "ER (Excessive Rainfall)",
                 "productString": "ER",
                 "identifier": "ER",
                },
                {
                 "displayString": "SM (Snow Melt)", 
                 "productString": "SM",
                 "identifier": "SM",
                },
                {
                 "displayString": "RS (Rain and Snow Melt)", 
                 "productString": "RS",
                 "identifier": "RS",
                },
                {
                 "displayString": "DM (Dam or Levee Failure)",
                 "productString": "DM",
                 "identifier": "DM",
                },
                {
                 "displayString": "DR (Upstream Dam Release)",
                 "productString": "DR",
                 "identifier": "DR",
                },
                {
                 "displayString": "GO (Glacier-Dammed Lake Outburst)",
                 "productString": "GO",
                 "identifier": "GO",
                },
                {
                 "displayString": "IJ (Ice Jam)", 
                 "productString": "IJ",
                 "identifier": "IJ",
                },
                {
                 "displayString": "IC (Rain and/or Snow melt and/or Ice Jam)",
                 "productString": "IC",
                 "identifier": "IC",
                },
                {
                 "displayString": "FS (Upstream Flooding plus Storm Surge)", 
                 "productString": "FS",
                 "identifier": "FS",
                },
                {
                 "displayString": "FT (Upstream Flooding plus Tidal Effects)",
                 "productString": "FT",
                 "identifier": "FT",
                },
                {
                 "displayString": "ET (Elevated Upstream Flow plus Tidal Effects)",
                 "productString": "ET",
                 "identifier": "ET",
                },
                {
                 "displayString": "WT (Wind and/or Tidal Effects)",
                 "productString": "WT",
                 "identifier": "WT",
                },
                {
                 "displayString": "OT (Other Effects)",
                 "identifier": "OT",
                 "productString": "OT",
                },
                {
                 "displayString": "UU (Unknown)", 
                 "productString": "UU",
                 "identifier": "UU",
                },
                ],
           },             
            {
            # INCLUDE 
             "fieldType":"CheckBoxes",
             "fieldName": "include",
             "label": "",
             "choices": [
                 {
                  "displayString": "Also Snow Melt", 
                  "productString": "RAPID SNOW MELT IS CAUSING FLOODING.",
                 },
                 {
                  "displayString": "Flooding not directly reported, only heavy rain",
                  "productString": "FLOODING IS NOT DIRECTLY REPORTED, ONLY HEAVY RAINS.",
                 }
                ] ,
            },
            # BASIS
            {
             "fieldType":"RadioButtons",
             "fieldName": "basis",
             "label": "Reported By (Choose 1):",
             "choices": [    
                     {
                      "displayString": "Doppler Radar indicated...", 
                      "productString": "Doppler Radar indicated",
                     },
                     {
                      "displayString": "Local law enforcement reported...", 
                      "productString": "Local law enforcement reported...",
                     },
                     {
                      "displayString": "Trained weather spotters reported...", 
                      "productString": "Trained weather spotters reported...",
                     },
                     {
                      "displayString": "Public reported...", 
                      "productString": "Public reported...",
                     },
                     {
                      "displayString": "pathcast",
                      "productString": "",
                     }
                   ], 
            },
           # CALLS TO ACTION
           {
           "fieldType":"CheckList",
           "label":"Calls to Action (1 or more):",
           "fieldName": "cta",
           "choices": [
               {
                "displayString": "No call to action",
                "productString": ""
                },{
                "displayString": "Automated list of drainages",
                "productString": "THIS INCLUDES THE FOLLOWING STREAMS AND DRAINAGES..."
                },{
                "displayString": "Additional rainfall expected...",
                "productString": "ADDITIONAL RAINFALL AMOUNTS OF !** EDIT AMOUNT **! ARE POSSIBLE IN THE WARNED AREA."
                },{
                "displayString": "Urban flooding",
                "productString": "EXCESSIVE RUNOFF FROM HEAVY RAINFALL WILL CAUSE ELEVATED LEVELS ON SMALL CREEKS AND STREAMS...AND PONDING OF WATER IN URBAN AREAS...HIGHWAYS...STREETS AND UNDERPASSES AS WELL AS OTHER POOR DRAINAGE AREAS AND LOW LYING SPOTS."
                },{
                "displayString": "Rural flooding/small streams",
                "productString": "EXCESSIVE RUNOFF FROM HEAVY RAINFALL WILL CAUSE ELEVATED LEVELS ON SMALL CREEKS AND STREAMS...AND PONDING OF WATER ON COUNTRY ROADS AND FARMLAND ALONG THE BANKS OF CREEKS AND STREAMS."
                },{
                "displayString": "Flooding is occurring in a particular stream/river",
                "productString": "FLOOD WATERS ARE MOVING DOWN !**name of channel**! FROM !**location**! TO !**location**!. THE FLOOD CREST IS EXPECTED TO REACH !**location(s)**! BY !**time(s)**!."
                },{
                "displayString": "Nighttime flooding",
                "productString": "BE ESPECIALLY CAUTIOUS AT NIGHT WHEN IT IS HARDER TO RECOGNIZE THE DANGERS OF FLOODING. IF FLOODING IS OBSERVED ACT QUICKLY. MOVE UP TO HIGHER GROUND TO ESCAPE FLOOD WATERS. DO NOT STAY IN AREAS SUBJECT TO FLOODING WHEN WATER BEGINS RISING."
                },{
                "displayString": "Do not drive into water",
                "productString": "DO NOT DRIVE YOUR VEHICLE INTO AREAS WHERE THE WATER COVERS THE ROADWAY. THE WATER DEPTH MAY BE TOO GREAT TO ALLOW YOUR CAR TO CROSS SAFELY. MOVE TO HIGHER GROUND."
                },{
                "displayString": "Turn around...don't drown",
                "productString": "MOST FLOOD DEATHS OCCUR IN AUTOMOBILES. NEVER DRIVE YOUR VEHICLE INTO AREAS WHERE THE WATER COVERS THE ROADWAY. FLOOD WATERS ARE USUALLY DEEPER THAN THEY APPEAR. JUST ONE FOOT OF FLOWING WATER IS POWERFUL ENOUGH TO SWEEP VEHICLES OFF THE ROAD. WHEN ENCOUNTERING FLOODED ROADS MAKE THE SMART CHOICE...TURN AROUND...DONT DROWN."
                },{
                "displayString": "Camper safety",
                "productString": "FLOODING IS OCCURRING OR IS IMMINENT.  IT IS IMPORTANT TO KNOW WHERE YOU ARE RELATIVE TO STREAMS...RIVERS...OR CREEKS WHICH CAN BECOME KILLERS IN HEAVY RAINS.  CAMPERS AND HIKERS SHOULD AVOID STREAMS OR CREEKS."
                },{
                "displayString": "Low spots in hilly terrain ",
                "productString": "IN HILLY TERRAIN THERE ARE HUNDREDS OF LOW WATER CROSSINGS WHICH ARE POTENTIALLY DANGEROUS IN HEAVY RAIN. DO NOT ATTEMPT TO TRAVEL ACROSS FLOODED ROADS. FIND ALTERNATE ROUTES. IT TAKES ONLY A FEW INCHES OF SWIFTLY FLOWING WATER TO CARRY VEHICLES AWAY."
                },{
                "displayString": "A Flash Flood Warning means...",
                "productString": "flashFloodWarningMeans is not defined now"
                },{
                "displayString": "Power of flood waters/vehicles",
                "productString": "DO NOT UNDERESTIMATE THE POWER OF FLOOD WATERS. ONLY A FEW INCHES OF RAPIDLY FLOWING WATER CAN QUICKLY CARRY AWAY YOUR VEHICLE."
                },{
                "displayString": "Report flooding to local law enforcement",
                "productString": "TO REPORT FLOODING...HAVE THE NEAREST LAW ENFORCEMENT AGENCY RELAY YOUR REPORT TO THE NATIONAL WEATHER SERVICE FORECAST OFFICE."
                }]
           }  
]
