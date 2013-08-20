

config = {
     "platform":  "awips",  # "web"  (or "cave")
     "cavePort": "None",  # "ouzel:8087"
     "irisURL": "None",   #  Iris interoperability
     "serverDataPath": "/scratch/ci/ihis/trunk/server/serverData/",  # "../serverData/"
     # If "Local", getHazards and putHazards will read/write using a local file.
     # If None, do nothing for putHazards -- getHazards will use canned data according to view.
     "databaseURL": "None",  # NOT hooked up yet  (Tracy)
     "resolutions": [31776.48108108108, 24092, 12046, 6023.205942622951, 3072, 2048.9754098360654, 1372, 922.0389344262295, 471.2643442622951, 261.813524589, 174.542349726, 43.2075609226752],  
     "defaultView": "WSW",  
     "views": [             
               {
               "id": "TOR",
               "displayName": "Canned TOR",
               "defaultHazardCategory": "shortFused",
               "defaultCategory": "Convective",
               "defaultDuration" : 30*60*1000,   # 30 minutes,
               "maxDuration" : 90,       # 90 minutes
               "durationIncrement" : 1,  # 1 minute
               "defaultTimeDisplayDuration": 4*3600*1000, # 4 hours
               "mapCenter" : {"lon": -104.7,"lat": 39.3,"zoom": 7},
               "temporalCounts": {"increment": 300000,"loopCount": 0,"distance": 12,"step": 2000,"panDistance": 7200000},
               "hazardsFilter": "TOR",  # This is currently useless - the hazardTypes is used to determine which hazards to load
               "defaultImageLayers": [],
               "defaultVectorLayers": [],
               "defaultBaseLayer": "Google Hybrid",
               "radar": "true",
             "controlLayers": ["Hazards", "LandScan Population", "CO Gov", "CO Airports", "USA WFO Locations", "USA EM State Offices", "CO EOC Offices"], 
               "defaultControlLayers": ["Hazards"],
               "draggedTrackPointShape": "star",
               "trackPointShape": "circle",
               "pointHazardShape": "triangle",
               "hidHazardCategories": ["Short Fused"],
               "hazardTypes": ["TO.W", "SV.W", "EW.W"],
               "defaultSiteID": "BOU",
               "visibleSites": ["BOU", "OAX"],
                },
               {
               "id": "WSW",
               "displayName": "Canned WSW",
               "defaultHazardCategory": "longFused",
               "defaultCategory": "Winter Weather",
               "defaultDuration" :  8*3600*1000,  # 8 hours
               "maxDuration" : 3600,       #2 days
               "durationIncrement" : 60,  # 1 hour
               "defaultTimeDisplayDuration": 24*3600*1000,  # 24 hours
               "mapCenter" : {"lon": -104.7,"lat": 39.3,"zoom": 7},
               "temporalCounts":  {"increment": 600000,"loopCount": 0,"distance": 12,"step": 2000,"panDistance": 14400000},
               "hazardsFilter": "WSW",  # This will be enhanced later
               "defaultImageLayers": ["snowAccumulation"], 
               "defaultBaseLayer": "Google Hybrid",
               "radar": "true",
               "controlLayers": ["Hazards", "LandScan Population", "CO Gov", "CO Airports", "USA WFO Locations", "USA EM State Offices", "CO EOC Offices"],     
               "defaultControlLayers": ["Hazards"],
               "draggedTrackPointShape": "star",
               "trackPointShape": "circle",
               "pointHazardShape": "triangle",
               "hidHazardCategories": ["Winter Weather"],
               "hazardTypes":["WS.W", "BZ.W"],
               "defaultSiteID": "OAX",
                "visibleSites": ["BOU", "OAX"],
               },
               {
               "id": "Flood",
               "displayName": "Canned Flood",
               "defaultHazardCategory": "hydro",
               "defaultCategory": "Hydrology",
               "defaultDuration" :  8*3600*1000,  # 8 hours
               "maxDuration" : 7260,       #4 days and 1 hour
               "durationIncrement" : 60,  # 1 hour
               "defaultTimeDisplayDuration": 48*3600*1000,  # 48 hours
               "mapCenter" : {"lon": -104.4,"lat": 40.1,"zoom": 7},
               "temporalCounts":  {"increment": 600000,"loopCount": 0,"distance": 12,"step": 2000,"panDistance": 14400000},
               "hazardsFilter": "Flood",  # This will be enhanced later
               "defaultImageLayers": [], 
               "defaultVectorLayers": ["riverPoints"],
               "defaultBaseLayer": "Google Physical",
               "radar": "true",
             "controlLayers": ["Hazards", "LandScan Population", "CO Gov", "CO Airports", "USA WFO Locations", "USA EM State Offices", "CO EOC Offices"], 
               "defaultControlLayers": ["Hazards"],
               "draggedTrackPointShape": "star",
               "trackPointShape": "circle",
               "pointHazardShape": "triangle",
               "hidHazardCategories": ["Hydrology"],
               "hazardTypes": ["FF.Y","FF.A", "FF.W.Conventive", "FF.W.NonConvective","FA.Y","FA.A","FA.W","FL.Y","FL.A","FL.W"],
               "defaultSiteID": "BOU",
                "visibleSites": ["BOU", "OAX"]
               },
               {
               "id": "OFP",
               "displayName": "Canned Partner",
               "defaultHazardCategory": "partner",
               "defaultDuration" :  8*3600*1000,  # 8 hours
               "maxDuration" : 7260,       #4 days and 1 hour
               "durationIncrement" : 60,  # 1 hour
               "defaultTimeDisplayDuration": 48*3600*1000,  # 48 hours
               "mapCenter" : {"lon": -79.56482,"lat": 35.773258,"zoom": 7},
               "temporalCounts":  {"increment": 600000,"loopCount": 0,"distance": 12,"step": 2000,"panDistance": 14400000},
               "hazardsFilter": "WSW",  # This will be enhanced later
               "defaultImageLayers": ["LandScan Population"], 
               "defaultVectorLayers": ["CO Hospitals", "CO Airports"],
               "defaultBaseLayer": "Google Hybrid",
               "radar": "false",
             "controlLayers": ["Hazards", "River Forecast Points", "LandScan Population", "CO Gov", "CO Airports", "USA WFO Locations", "USA EM State Offices", "CO EOC Offices"], 
               "defaultControlLayers": ["Hazards"],
                "draggedTrackPointShape": "star",
                "trackPointShape": "circle",
                "pointHazardShape": "triangle",
               "defaultSiteID": "RAH",
                "visibleSites": ["RAH"]
                },
            ],
      }

########################
#  HID CONFIG 

################

# Definition of method to converge lists of hazard type names and their
# IDs into a single list holding dictionaries with entries for both
# names and IDs.
def convergeLists(l1, l2):
    resultList = []
    for i in range(len(l1)):
        newEntry = { "displayString": l1[i], "identifier": l2[i] }
        resultList.append(newEntry)
    return resultList

# UPPER PORTION OF HID
# Basic HID Hazard Categories and Hazard Types 
#    This section is common to all hazards

# GROUPS FOR HAZARD CATEGORIES 
# When displayed, these groups should be sorted into alphabetical order
# Lists of the identifiers of each type are provided as well.
shortFusedHazardTypeNames = [
    "EW.W (EXCESSIVE WIND WARNING)", "SV.W (SEVERE THUNDERSTORM WARNING)","TO.W (TORNADO WARNING)"
   ]
shortFusedHazardTypeIds = [ "EW.W", "SV.W", "TO.W" ];
shortFusedHazardTypes = convergeLists(shortFusedHazardTypeNames, shortFusedHazardTypeIds)

coastalFloodHazardTypeNames = [
    "CF.Y (COASTAL FLOOD ADVISORY)","CF.A (COASTAL FLOOD WATCH)","CF.W (COASTAL FLOOD WARNING)",
    "CR.S (COASTAL FLOOD STATEMENT)",
    "LS.A (LAKESHORE FLOOD WATCH)","LS.W (LAKESHORE FLOOD WARNING)","LS.S (LAKESHORE FLOOD STATEMENT)",
    "LS.Y (LAKESHORE FLOOD ADVISORY)", 
    "SU.Y (HIGH SURF ADVISORY)", "SU.W (HIGH SURF WARNING)",
   ]
coastalFloodHazardTypeIds = [ "CF.Y", "CF.A", "CF.W", "CR.S", "LS.A", "LS.W", "LS.S", "LS.Y", "SU.Y", "SU.W" ]
coastalFloodHazardTypes = convergeLists(coastalFloodHazardTypeNames, coastalFloodHazardTypeIds)

convectiveWatchesHazardTypeNames = [
    "SV.A (SEVERE THUNDERSTORM WATCH)","TO.A (TORNADO WATCH)",
   ]
convectiveWatchesHazardTypeIds = [ "SV.A", "TO.A" ]
convectiveWatchesHazardTypes = convergeLists(convectiveWatchesHazardTypeNames, convectiveWatchesHazardTypeIds)

fireWeatherHazardTypeNames = ["FW.A (FIRE WEATHER WATCH)","FW.W (RED FLAG WARNING)",]
fireWeatherHazardTypeIds = [ "FW.A", "FW.W" ]
fireWeatherHazardTypes = convergeLists(fireWeatherHazardTypeNames, fireWeatherHazardTypeIds)

marineHazardTypeNames = [
    "SE.A (HAZARDOUS SEAS WATCH)","SE.W (HAZARDOUS SEAS WARNING)",
    "BW.Y (BRISK WIND ADVISORY)", 
    "GL.A (GALE WATCH)", "GL.W (GALE WARNING)", 
    "HF.W (HURRICANE FORCE WIND WARNING)","HF.A (HURRICANE FORCE WIND WATCH)", 
    "LO.Y (LOW WATER ADVISORY)",
    "MA.S (MARINE WEATHER STATEMENT)","MA.W (SPECIAL MARINE WARNING)", 
    "MH.Y (ASHFALL ADVISORY)","MH.W (ASHFALL WARNING)", 
    "MF.Y (DENSE FOG ADVISORY)",
    "MS.Y (DENSE SMOKE ADVISORY)", 
    "SI.Y (SMALL CRAFT ADVISORY FOR WINDS)","SC.Y (SMALL CRAFT ADVISORY)", "SW.Y (SMALL CRAFT ADVISORY FOR HAZARDOUS SEAS)",
    "RB.Y (SMALL CRAFT ADVISORY FOR ROUGH BAR)",
    "SR.A (STORM WATCH)", "SR.W (STORM WARNING)",
    "UP.A (HEAVY FREEZING SPRAY WATCH)","UP.Y (FREEZING SPRAY ADVISORY)", "UP.W (HEAVY FREEZING SPRAY WARNING)", 
   ]
marineHazardTypeIds = [ "SE.A", "SE.W", "BW.Y", "GL.A", "GL.W", "HF.W", "HF.A", "LO.Y", "MA.S", "MA.W", "MH.Y", "MH.W",
                       "MF.Y", "MS.Y", "SI.Y", "SC.Y", "SW.Y", "RB.Y", "SR.A", "SR.W", "UP.A", "UP.Y", "UP.W" ] 
marineHazardTypes = convergeLists(marineHazardTypeNames, marineHazardTypeIds)

nonPrecipHazardTypeNames = [
    "AF.W (ASHFALL WARNING)", "AF.Y (ASHFALL ADVISORY)", 
    "AQ.Y (AIR QUALITY ALERT)", 
    "AS.O (AIR STAGNATION OUTLOOK)", "AS.Y (AIR STAGNATION ADVISORY)", 
    "DU.Y (BLOWING DUST ADVISORY)","DS.W (DUST STORM WARNING)", 
    "EH.W (EXCESSIVE HEAT WARNING)", "EH.A (EXCESSIVE HEAT WATCH)",
    "HT.Y (HEAT ADVISORY)", 
    "EC.W (EXTREME COLD WARNING)", "EC.A (EXTREME COLD WATCH)", 
    "FG.Y (DENSE FOG ADVISORY)", "FZ.W (FREEZE WARNING)", "FZ.A (FREEZE WATCH)",
    "HZ.W (HARD FREEZE WARNING)", "HZ.A (HARD FREEZE WATCH)",  
    "FR.Y (FROST ADVISORY)",
    "ZF.Y (FREEZING FOG ADVISORY)",
    "HW.A (HIGH WIND WATCH)","HW.W (HIGH WIND WARNING)", 
    "LW.Y (LAKE WIND ADVISORY)", "SM.Y (DENSE SMOKE ADVISORY)", 
    "WI.Y (WIND ADVISORY)",
   ]
nonPrecipHazardTypeIds = [ "AF.W", "AF.Y", "AQ.Y", "AS.O", "AS.Y", "DU.Y", "DS.W", "EH.W", "EH.A", "HT.Y", "EC.W", "EC.A", 
                          "FG.Y", "FZ.W", "FZ.A", "HZ.W", "HZ.A", "FR.Y", "ZF.Y", "HW.A","HW.W", "LW.Y", "SM.Y", "WI.Y" ]
nonPrecipHazardTypes = convergeLists(nonPrecipHazardTypeNames, nonPrecipHazardTypeIds)

tropicalCycloneHazardTypeNames = [
    "TR.W (TROPICAL STORM WARNING)","TR.A (TROPICAL STORM WATCH)", 
    "HU.W (HURRICANE WARNING)","HU.S (TROPICAL CYCLONE LOCAL STATEMENT)", "HU.A (HURRICANE WATCH)",
    "HI.A (HURRICANE WIND WATCH)","HI.W (HURRICANE WIND WARNING)",
    "TI.W (TROPICAL STORM WIND WARNING)","TI.A (TROPICAL STORM WIND WATCH)",
    ]
tropicalCycloneHazardTypeIds = [ "TR.W", "TR.A", "HU.W", "HU.S", "HU.A", "HI.A", "HI.W", "TI.W", "TI.A" ]
tropicalCycloneHazardTypes = convergeLists(tropicalCycloneHazardTypeNames, tropicalCycloneHazardTypeIds)

typhoonHazardTypeNames = ["TY.A (TYPHOON WATCH)", "TY.W (TYPHOON WARNING)", "HU.S"]
typhoonHazardTypeIds = [ "TY.A", "TY.W", "HU.S" ]
typhoonHazardTypes = convergeLists(typhoonHazardTypeNames, typhoonHazardTypeIds)

tsunamiHazardTypeNames = ["TS.A (TSUNAMI WATCH)","TS.W (TSUNAMI WARNING)",]
tsunamiHazardTypeIds = [ "TS.A", "TS.W" ]
tsunamiHazardTypes = convergeLists(tsunamiHazardTypeNames, tsunamiHazardTypeIds)

winterWeatherHazardTypeNames = [
    "BZ.W (BLIZZARD WARNING)", "BZ.A (BLIZZARD WATCH)", "ZR.Y (FREEZING RAIN ADVISORY)",
    "IS.W (ICE STORM WARNING)", 
    "LE.W (LAKE EFFECT SNOW WARNING)","LE.Y (LAKE EFFECT SNOW ADVISORY)", "LE.A (LAKE EFFECT SNOW WATCH)",
    "WC.W (WIND CHILL WARNING)", "WC.Y (WIND CHILL ADVISORY)","WC.A (WIND CHILL WATCH)", 
    "WS.W (WINTER STORM WARNING)","WS.A (WINTER STORM WATCH)", 
    "WW.Y (WINTER WEATHER ADVISORY)",
   ]
winterWeatherHazardTypeIds = [
    "BZ.W", "BZ.A", "ZR.Y", "IS.W", "LE.W", "LE.Y", "LE.A", "WC.W", "WC.Y", "WC.A", "WS.W", "WS.A", "WW.Y" ]
winterWeatherHazardTypes = convergeLists(winterWeatherHazardTypeNames, winterWeatherHazardTypeIds)

hydroHazardTypeNames = [
    "FF.Y (FLASH FLOOD ADVISORY)","FF.A (FLASH FLOOD WATCH)", "FF.W.Convective (FLASH FLOOD WARNING)", 
    "FF.W.NonConvective (FLASH FLOOD WARNING)", 
    "FA.Y (AREAL FLOOD ADVISORY)", "FA.A (AREAL FLOOD WATCH)","FA.W (AREAL FLOOD WARNING)",                  
    "FL.Y (FLOOD ADVISORY)","FL.A (FLOOD WATCH)","FL.W (FLOOD WARNING)",
   ]
hydroHazardTypeIds = [ "FF.Y", "FF.A", "FF.W.Convective", "FF.W.NonConvective", "FA.Y", "FA.A", "FA.W", "FL.Y", "FL.A", "FL.W" ]
hydroHazardTypes = convergeLists(hydroHazardTypeNames, hydroHazardTypeIds)


hazardTypeNames = shortFusedHazardTypeNames + coastalFloodHazardTypeNames + convectiveWatchesHazardTypeNames +  \
              fireWeatherHazardTypeNames + marineHazardTypeNames +  nonPrecipHazardTypeNames + tropicalCycloneHazardTypeNames + \
              typhoonHazardTypeNames + tsunamiHazardTypeNames +  winterWeatherHazardTypeNames +  hydroHazardTypeNames        
hazardTypeIds = shortFusedHazardTypeIds + coastalFloodHazardTypeIds + convectiveWatchesHazardTypeIds +  \
              fireWeatherHazardTypeIds + marineHazardTypeIds +  nonPrecipHazardTypeIds + tropicalCycloneHazardTypeIds + \
              typhoonHazardTypeIds + tsunamiHazardTypeIds +  winterWeatherHazardTypeIds +  hydroHazardTypeIds

# Hazard Categories
#  These should be displayed in the order given here (not sorted alphabetically)
#
hazardCategoryAndTypes = [
                    
                    # WAS "label" AND "hazardTypes"
                        {
                         "displayString": "Convective",
                         "children": shortFusedHazardTypes + convectiveWatchesHazardTypes,
                         },
                         {
                          "displayString": "Winter Weather",
                          "children":  winterWeatherHazardTypes,              
                          },
                        {
                         "displayString": "Hydrology",
                         "children": hydroHazardTypes,
                        },
                         {
                          "displayString": "Coastal Flood",
                          "children":  coastalFloodHazardTypes,              
                          },
                         {
                          "displayString": "Fire Weather",
                          "children":  fireWeatherHazardTypes,              
                          },
                         {
                          "displayString": "Marine",
                          "children":  marineHazardTypes,              
                          },
                         {
                          "displayString": "Non Precip",
                          "children":  nonPrecipHazardTypes,              
                          },
                         {
                          "displayString": "Tropical",
                          "children":  tropicalCycloneHazardTypes,              
                          },
                 ]

hazardInfoConfig = {
    "hazardCategories": hazardCategoryAndTypes,
    "defaultCategory": "Hydrology",
        } 


##################
# OPTIONS
# Hazard specific options for the HID
# NOTE: the "fieldName" field is the name of the eventDict field to store this information
# NOTE: The format of the Options has changed from a dictionary of dictionaries to a list of dictionaries

shortFusedOptions =  [
           {
            "fieldName":"backup",
            "choices":[ "FSL", "PUB", "GJT", "CYS" ],
            "label":"Backup:",
            "fieldType":"ComboBox",
           },
           {
            "fieldName": "polygonComposite",
            "fieldType": "Composite",
            "numColumns": 2,
            "fields": [
                       {
                        "fieldName":"makePolygon",
                        "fieldType":"Button",
                        "label":"Make Polygon",
                        "notify": 1,
                       },
                       {
                        "fieldName":"curve",
                        "fieldType":"CheckBoxes",
                        "choices": [ "Curve" ],
                        "notify": 1,
                       },
                      ],
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

longFusedOptions = [] 

hydroPointOptions =  [
#           {
#            "fieldName": "type",
#            "choices":[
#                     {
#                      "displayString": "FF.Y (FLASH FLOOD ADVISORY)",
#                      "identifier": "FF.Y"
#                     },
#                     {
#                      "displayString": "FF.A (FLASH FLOOD WATCH)",
#                      "identifier": "FF.A"
#                     },
#                     {
#                      "displayString": "FF.W (FLASH FLOOD WARNING)",
#                      "identifier": "FF.W"
#                     },
#                     {
#                      "displayString": "FL.Y (FLOOD ADVISORY)",
#                      "identifier": "FL.Y"
#                     },
#                     {
#                      "displayString": "FL.A (FLOOD WATCH)",
#                      "identifier": "FL.A"
#                     },
#                     {
#                      "displayString": "FL.W (FLOOD WARNING)",
#                      "identifier": "FL.W" 
#                     }
#                    ],
#            "fieldType":"ComboBox",
#            "label":"Hazard Type:",
#            "shortValueLabels":"Type",
#           },
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
                      "identifier": "N" 
                     },
                     {
                      "displayString": "0 (Areal Flood or Flash Flood Products)",
                      "identifier": "0"
                     },
                     {
                      "displayString": "1 (Minor)",
                      "identifier": "1" 
                     },
                     {
                      "displayString": "2 (Moderate)",
                      "identifier": "2" 
                     },
                     {
                      "displayString": "3 (Major)",
                      "identifier": "3" 
                     },
                     {
                      "displayString": "U (Unknown)",
                      "identifier": "U" 
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

fayOptions = [
           {
            "fieldType":"RadioButtons",
            "fieldName": "nonGenericAdvisory",
            "label": "If not generic advisory:",
            "choices": [ 
                     {
                      "displayString": "Small stream", 
                     },
                     {
                      "displayString": "Urban and small stream", 
                     },
                     {
                      "displayString": "Arroyo and small stream", 
                     },
                     {
                      "displayString": "Hydrologic",
                     }
                    ]
            },
            {
             "fieldType":"CheckBoxes",
             "fieldName": "causes",
             "label":"If hydrologic: conditions/causes if applicable:",
             "choices":[ 
                     {
                      "displayString": "Rain", 
                     },
                     {
                      "displayString": "Snow melt", 
                     },
                     {
                      "displayString": "Ice jam", 
                     },
                     {
                      "displayString": "Rapid river rises", 
                     },
                     {
                      "displayString": "Minor flooding for poor drainage",
                     }
                    ]
            },
            {
             "fieldType":"CheckList",
             "label":"Calls to Action (1 or more):",
             "fieldName": "cta",
             "lines": 10,
             "choices": [
                     {
                      "displayString": "No call to action",
                     },
                     {
                      "displayString": "Automated list of drainages",
                     },
                     {
                      "displayString": "Additional rainfall expected...",
                     },
                     {
                      "displayString": "Turn around...don't drown",
                     },
                     {
                      "displayString": "Urban flooding",
                     },
                     {
                      "displayString": "Rural flooding/small streams",
                     },
                     {
                      "displayString": "Do not drive into water",
                     },
                     {
                      "displayString": "A Flood Advisory means...",
                     },
                     {
                      "displayString": "Low spots in hilly terrain",
                     },
                     {
                      "displayString": "Power of flood waters/vehicles",
                     },
                     {
                      "displayString": "Report flooding to local law enforcement",
                     }
                    ]
            }
        ]

faaOptions = [
            {
             "fieldName": "immediateCause",
             "choices": [
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
            },
     ]


fawOptions = [
            {
             "fieldType":"RadioButtons",
             "fieldName": "include",
             "label": "Include:",
             "choices": [ 
                     {
                      "displayString": "Small streams", 
                     },
                     {
                      "displayString": "Urban areas and small streams",
                     }
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
                     {
                      "displayString": "Snow melt", 
                     },
                     {
                      "displayString": "Rain and Snow Melt", 
                     },
                     {
                      "displayString": "Dam break", 
                     },
                     {
                      "displayString": "Flood gate opening", 
                     },
                     {
                      "displayString": "Ice Jam", 
                     },
                     {
                      "displayString": "Ice Jam/Rain/Snow melt", 
                     },
                     {
                      "displayString": "Glacial Lake Outburst", 
                     },
                     {
                      "displayString": "Multiple Causes", 
                     },
                     {
                      "displayString": "Unknown Cause",
                     }
                    ]
            },
            {                      
             "fieldType":"CheckList",
             "label":"Calls to Action (1 or more):",
             "fieldName": "cta",
             "lines": 3,
             "choices": [ 
                     {
                      "displayString": "No call to action", 
                     },
                     {
                      "displayString": "Automated list of drainages", 
                     },
                     {
                      "displayString": "Additional rainfall expected", #(need dialog to specify how much)", 
                     },
                     {
                      "displayString": "Turn around...don't drown", 
                     },
                     {
                      "displayString": "Urban flooding", 
                     },
                     {
                      "displayString": "Rural flooding/small streams", 
                     },
                     {
                      "displayString": "Flooding is occurring in a particular stream/river", 
                     },
                     {
                      "displayString": "Nighttime flooding", 
                     },
                     {
                      "displayString": "Do not drive into water", 
                     },
                     {
                      "displayString": "A Flood Warning means...", 
                     },
                     {
                      "displayString": "Camper safety", 
                     },
                     {
                      "displayString": "Low spots in hilly terrain", 
                     },
                     {
                      "displayString": "Power of flood waters/vehicles", 
                     },
                     {
                      "displayString": "Report flooding to local law enforcement",
                     }
                    ]
            }
        ]

ffwOptions = [
            {
             "fieldType":"CheckBoxes",
             "fieldName": "include",
             "label": "",
             "choices": [ 
                     {
                      "displayString": "Also Snow Melt", 
                     },
                     {
                      "displayString": "Flooding not directly reported, only heavy rain",
                     }
                    ]
            },
            {
             "fieldType":"RadioButtons",
             "fieldName": "basis",
             "label": "Reported By (Choose 1):",
             "choices": [ 
                     {
                      "displayString": "Doppler Radar indicated...", 
                     },
                     {
                      "displayString": "Local law enforcement reported...", 
                     },
                     {
                      "displayString": "Trained weather spotters reported...", 
                     },
                     {
                      "displayString": "Public reported...", 
                     },
                     {
                      "displayString": "pathcast",
                     }
                   ]
            },
            {
             "fieldType":"CheckList",
             "label":"Calls to Action (1 or more):",
             "fieldName": "cta",
             "lines": 13,
             "choices": [ 
                     {
                      "displayString": "No call to action", 
                     },
                     {
                      "displayString": "Automated list of drainages", 
                     },
                     {
                      "displayString": "Additional rainfall expected...", 
                     },
                     {
                      "displayString": "Urban flooding", 
                     },
                     {
                      "displayString": "Rural flooding/small streams", 
                     },
                     {
                      "displayString": "Flooding is occurring in a particular stream/river", 
                     },
                     {
                      "displayString": "Nighttime flooding", 
                     },
                     {
                      "displayString": "Do not drive into water", 
                     },
                     {
                      "displayString": "Turn around...don't drown", 
                     },
                     {
                      "displayString": "Camper safety", 
                     },
                     {
                      "displayString": "Low spots in hilly terrain ", 
                     },
                     {
                      "displayString": "A Flash Flood Warning means...", 
                     },
                     {
                      "displayString": "Power of flood waters/vehicles", 
                     },
                     {
                      "displayString": "Report flooding to local law enforcement",
                     }
                   ]
            }
        ]
 

# HID Options which change depending on the hazard type
#  Options can be specified per hazard type
#  The order of this list is important.  
#   For example, FA.A may be listed in the hydroHazardTypes, so the HID needs to 
#   process the list in this order taking the first definition of options for FA.A.
hazardInfoOptions = [
               {
                "hazardTypes": ["FA.Y (AREAL FLOOD ADVISORY)"],
                "options": fayOptions,
                "pointOptions": hydroPointOptions,
                },
                {
                "hazardTypes": ["FA.A (AREAL FLOOD WATCH)"],
                "options": faaOptions,
                },
                {
                "hazardTypes": ["FA.W (AREAL FLOOD WARNING)"],
                "options": fawOptions,
                "pointOptions": hydroPointOptions,
                },
                {
                "hazardTypes": ["FF.W.Convective (FLASH FLOOD WARNING)"],
                "options": ffwOptions,
                "pointOptions": hydroPointOptions,
                },
                {
                "hazardTypes": ["FF.W.NonConvective (FLASH FLOOD WARNING)"],
                "options": ffwOptions,
                "pointOptions": hydroPointOptions,
                },
               {
               "hazardTypes": shortFusedHazardTypeNames + convectiveWatchesHazardTypeNames,
               "options": shortFusedOptions,
               },
               {
                "hazardTypes": winterWeatherHazardTypeNames + coastalFloodHazardTypeNames + fireWeatherHazardTypeNames + 
                                marineHazardTypeNames + nonPrecipHazardTypeNames + tropicalCycloneHazardTypeNames,
                "options": longFusedOptions,
               },
               {
                "hazardTypes": hydroHazardTypeNames,
                "options": hydroPointOptions,
               },
               ]
 
 
########################
## VIEW CONFIG
           
controlLayers = [
                     {
                      "displayString": "Hazards", 
                     },
                     {
                      "displayString": "River Forecast Points", 
                     },
                     {
                      "displayString": "LandScan Population", 
                     },
                     {
                      "displayString": "CO Gov", 
                     },
                     {
                      "displayString": "CO Airports", 
                     },
                     {
                      "displayString": "USA WFO Locations", 
                     },
                     {
                      "displayString": "USA EM State Offices", 
                     },
                     {
                      "displayString": "CO EOC Offices"
                     }
                ]
siteIDs = [ 
                     {
                      "displayString": "BOU", 
                     },
                     {
                      "displayString": "PUB", 
                     },
                     {
                      "displayString": "GJT", 
                     },
                     {
                      "displayString": "CYS", 
                     },
                     {
                      "displayString": "OAX", 
                     },
                     {
                      "displayString": "FSD", 
                     },
                     {
                      "displayString": "DMX",
                     },
                     {
                      "displayString": "GID", 
                     },
                     {
                      "displayString": "EAX", 
                     },
                     {
                      "displayString": "TOP", 
                     },
                     {
                      "displayString": "RAH"
                     }
            ]

manifestations = [ 
                     {
                      "displayString": "Temporal Display", 
                     },
                     {
                      "displayString": "Spatial Display", 
                     },
                     {
                      "displayString": "Beep", 
                     },
                     {
                      "displayString": "Popup", 
                     },
                     {
                      "displayString": "Blink", 
                     },
                     {
                      "displayString": "Text", 
                     },
            ]

states = [ 
                     {
                      "displayString": "potential", 
                     },
                     {
                      "displayString": "proposed", 
                     },

                     {
                      "displayString": "pending", 
                     },

                     {
                      "displayString": "issued", 
                     },

                     {
                      "displayString": "ended", 
                     },

            ]
columns = [ 
                     {
                      "displayString": "Event ID", 
                     },
                     {
                      "displayString": "Hazard Type", 
                     },
                     {
                      "displayString": "State", 
                     },
                     {
                      "displayString": "Start Time", 
                     },
                     {
                      "displayString": "End Time", 
                     },
                     {
                      "displayString": "Phen", 
                     },
                     {
                      "displayString": "Sig", 
                     },
                     {
                      "displayString": "Expiration Time",
                     },
                     {
                      "displayString": "Issue Time", 
                     },
                     {
                      "displayString": "Site ID", 
                     },
                     {
                      "displayString": "VTEC Codes", 
                     },
                     {
                      "displayString": "ETNs"
                     },
                     {
                      "displayString": "PILs"
                     },
                     {
                      "displayString": "Description"
                     }

            ]

baseLayers = [ 
                     {
                      "displayString": "Google Hybrid"
                     }
            ]
hazardCategories = [ 
                     {
                      "displayString": "Convective", 
                     },
                     {
                      "displayString": "Winter Weather", 
                     },
                     {
                      "displayString": "Hydrology", 
                     },
                     {
                      "displayString": "Coastal Flood", 
                     },
                     {
                      "displayString": "Fire Weather", 
                     },
                     {
                      "displayString": "Marine", 
                     },
                     {
                      "displayString": "Non Precip", 
                     },
                     {
                      "displayString": "Tropical"
                     }
                     ]


viewDefaultValues = {
 "defaultHazardCategory": "shortFused",
 "hazardsFilter": "TOR",  # This is currently useless - the hazardTypes is used to determine which hazards to load
 "defaultImageLayers": [],
 "defaultVectorLayers": [],
 "radar": "true",
 "hazardTypes": [],
 "defaultSiteID": "OAX",
 "visibleSites": [],
 "defaultCategory": "Hydrology",
 "hidHazardCategories": [],
 "hazardCategoriesAndTypes": [],
 "defaultDuration": 28800000,
 "maxDuration": 525600,
 "defaultTimeDisplayDuration": 14400000,
 "durationIncrement": 1,
 "temporalCounts": {
  "increment": 600000,
  "loopCount": 0,
  "distance": 12,
  "step": 2000,
  "panDistance": 14400000,
 },
 "draggedTrackPointShape": "star",
 "trackPointShape": "circle",
 "pointHazardShape": "triangle",
 "defaultControlLayers": [ "Hazards" ],
 "defaultBaseLayer": "Google Hybrid",
}

# This lists the fields that are possible in a Setting
# The Setting Dialog will use this information to display and allow manipulation
# of a Setting Definition from the user.
#
# NOTE:
#
# Some megawidgets defined here include the non-megawidget-specifier key
# "columnName", with a value that matches one of the "columns" names. These
# key-value pairs are used to determine which filters may be paired with which
# console table columns. 
viewConfig = [
              {
               "fieldName": "tabbedPanel",
               "fieldType": "TabbedComposite",
               "leftMargin": 10,
               "rightMargin": 10,
               "topMargin": 7,
               "bottomMargin": 7,
               "spacing": 10,
               "expandHorizontally": 1,
               "pages": [
                         {
                          "pageName": "Hazards Filter",
                          "numColumns": 3,
                          "pageFields": [
                                         {
                                          "fieldName": "hazardCategoriesAndTypes",
                                          "label": "Hazard Categories and Types:",
                                          "fieldType": "HierarchicalChoicesTree",
                                          "lines": 16,
                                          "columnName": "Hazard Type"
                                         },
                                         {
                                          "fieldName": "visibleSites",
                                          "label": "Site IDs:",
                                          "fieldType": "CheckList",
                                          "choices": siteIDs,
                                          "lines": 16,
                                          "columnName": "Site ID"
                                         },              
                                         {
                                          "fieldName": "visibleStates",
                                          "label": "State:",
                                          "fieldType": "CheckList",
                                          "choices": states,
                                          "lines": 16,
                                          "columnName": "State"
                                         },              
                                        ],
                         },
                         {
                          "pageName": "Console",
                          "pageFields": [
                                         {
                                          "fieldName": "visibleColumnsGroup",
                                          "label": "Columns",
                                          "fieldType": "Group",
                                          "leftMargin": 10,
                                          "rightMargin": 10,
                                          "bottomMargin": 10,
                                          "fields": [
                                                     {
                                                      "fieldName": "visibleColumns",
                                                      "label": "Available:",
                                                      "selectedLabel": "Selected:",
                                                      "fieldType": "ListBuilder",
                                                      "choices": columns,
                                                      "lines": 10
                                                     }
                                          ]
                                         },
                                        ],
                         },
                        ],
              },
            ]
              
                
alertConfig = [
  {
    "fieldName": "tabbedPanel",
    "fieldType": "TabbedComposite",
    "leftMargin": 10,
    "rightMargin": 10,
    "topMargin": 7,
    "bottomMargin": 7,
    "spacing": 5,
    "expandHorizontally": 1,
    "pages": [
      {
        "pageName": "AlertConfig1",
        "numColumns": 2,
        "pageFields": [
          {
            "fieldName": "hazardCategoriesAndTypes",
            "label": "Hazard Categories and Types:",
            "fieldType": "HierarchicalChoicesTree",
            "choices": hazardCategoryAndTypes,
            "lines": 23,
          },
          {
            "fieldName": "criteria",
            "label": "Criteria:",
            "fieldType": "Group",
            "leftMargin": 7,
            "rightMargin": 7,
            "topMargin": 3,
            "bottomMargin": 5,
            "fields": [
              {
                "fieldName": "criterium1",
                "label": "Criterium 1",
                "fieldType": "Group",
                "leftMargin": 7,
                "rightMargin": 7,
                "topMargin": 0,
                "bottomMargin": 5,
                "expandHorizontally": 1,
                "fields": [
                  {
                    "fieldName": "criterium1>status",
                    "fieldType": "CheckBoxes",
                    "choices": ["Enabled"],
                    "notify": 1,
                  },              
                  {
                    "fieldName": "criterium1>minutes",
                    "label": "Minutes Before Expiration:",
                    "fieldType": "IntegerSpinner",
                    "minValue": 1,
                    "maxValue": 30,
                    "incrementDelta": 1,
                    "showScale": 1
                  },              
                  {
                    "fieldName": "criterium1>manifestations",
                    "label": "Manifestations:",
                    "fieldType": "CheckList",
                    "choices": manifestations,
                    "lines": 7,
                  },              
                ],
              },
              {
                "fieldName": "criterium2",
                "label": "Criterium 2",
                "fieldType": "Group",
                "leftMargin": 7,
                "rightMargin": 7,
                "topMargin": 0,
                "bottomMargin": 5,
                "expandHorizontally": 1,
                "fields": [
                  {
                    "fieldName": "criterium2>status",
                    "fieldType": "CheckBoxes",
                    "choices": ["Enabled"],
                    "notify": 1,
                  },              
                  {
                    "fieldName": "criterium2>minutes",
                    "label": "Minutes Before Expiration:",
                    "fieldType": "IntegerSpinner",
                    "minValue": 1,
                    "maxValue": 30,
                    "incrementDelta": 1,
                    "showScale": 1
                  },              
                  {
                    "fieldName": "criterium2>manifestations",
                    "label": "Manifestations:",
                    "fieldType": "CheckList",
                    "choices": manifestations,
                    "lines": 7,
                  },              
                ],
              },
            ],
          },
        ],
      },
      {
        "pageName": "AlertConfig2",
        "numColumns": 2,
        "pageFields": [
          {
            "fieldName": "hazardCategoriesAndTypes",
            "label": "Hazard Categories and Types:",
            "fieldType": "HierarchicalChoicesTree",
            "choices": hazardCategoryAndTypes,
            "lines": 23,
          },
          {
            "fieldName": "criteria",
            "label": "Criteria:",
            "fieldType": "Group",
            "leftMargin": 7,
            "rightMargin": 7,
            "topMargin": 3,
            "bottomMargin": 5,
            "fields": [
              {
                "fieldName": "criterium1",
                "label": "Criterium 1",
                "fieldType": "Group",
                "leftMargin": 7,
                "rightMargin": 7,
                "topMargin": 0,
                "bottomMargin": 5,
                "expandHorizontally": 1,
                "fields": [
                  {
                    "fieldName": "criterium1>status",
                    "fieldType": "CheckBoxes",
                    "choices": ["Enabled"],
                    "notify": 1,
                  },              
                  {
                    "fieldName": "criterium1>minutes",
                    "label": "Minutes Before Expiration:",
                    "fieldType": "IntegerSpinner",
                    "minValue": 1,
                    "maxValue": 30,
                    "incrementDelta": 1,
                    "showScale": 1
                  },              
                  {
                    "fieldName": "criterium1>manifestations",
                    "label": "Manifestations:",
                    "fieldType": "CheckList",
                    "choices": manifestations,
                    "lines": 7,
                  },              
                ],
              },
              {
                "fieldName": "criterium2",
                "label": "Criterium 2",
                "fieldType": "Group",
                "leftMargin": 7,
                "rightMargin": 7,
                "topMargin": 0,
                "bottomMargin": 5,
                "expandHorizontally": 1,

                "fields": [
                  {
                    "fieldName": "criterium2>status",
                    "fieldType": "CheckBoxes",
                    "choices": ["Enabled"],
                    "notify": 1,
                  },              
                  {
                    "fieldName": "criterium2>minutes",
                    "label": "Minutes Before Expiration:",
                    "fieldType": "IntegerSpinner",
                    "minValue": 1,
                    "maxValue": 30,
                    "incrementDelta": 1,
                    "showScale": 1
                  },              
                  {
                    "fieldName": "criterium2>manifestations",
                    "label": "Manifestations:",
                    "fieldType": "CheckList",
                    "choices": manifestations,
                    "lines": 7,
                  },              
                ],
              },
            ],
          },
        ],
      },
    ],
  },
]

hazardInstanceAlertConfig = [
  {
    "fieldName": "criteria",
    "label": "Criteria:",
    "fieldType": "Group",
    "leftMargin": 7,
    "rightMargin": 7,
    "topMargin": 3,
    "bottomMargin": 5,
    "fields": [
      {
        "fieldName": "criterium1",
        "label": "Criterium 1",
        "fieldType": "Group",
        "leftMargin": 7,
        "rightMargin": 7,
        "topMargin": 0,
        "bottomMargin": 5,
        "expandHorizontally": 1,
        "fields": [
          {
            "fieldName": "criterium1>status",
            "fieldType": "CheckBoxes",
            "choices": ["Enabled"],
            "notify": 1,
            },              
          {
            "fieldName": "criterium1>minutes",
            "label": "Minutes Before Expiration:",
            "fieldType": "IntegerSpinner",
            "minValue": 1,
            "maxValue": 30,
            "incrementDelta": 1,
            "showScale": 1
          },              
          {
            "fieldName": "criterium1>manifestations",
            "label": "Manifestations:",
            "fieldType": "CheckList",
            "choices": manifestations,
            "lines": 7,
          },              
        ],
      },
      {
        "fieldName": "criterium2",
        "label": "Criterium 2",
        "fieldType": "Group",
        "leftMargin": 7,
        "rightMargin": 7,
        "topMargin": 0,
        "bottomMargin": 5,
        "expandHorizontally": 1,
        "fields": [
          {
            "fieldName": "criterium2>status",
            "fieldType": "CheckBoxes",
            "choices": ["Enabled"],
            "notify": 1,
            },              
          {
            "fieldName": "criterium2>minutes",
            "label": "Minutes Before Expiration:",
            "fieldType": "IntegerSpinner",
            "minValue": 1,
            "maxValue": 30,
            "incrementDelta": 1,
            "showScale": 1
          },              
          {
            "fieldName": "criterium2>manifestations",
            "label": "Manifestations:",
            "fieldType": "CheckList",
            "choices": manifestations,
            "lines": 7,
          },              
        ],
      },
    ],
  },
]
