Prob_WFO = {
    "settingsID" : "Prob_WFO",
    "perspectiveIDs" : [],
    "displayName": "Probabilistic WFO",
    "visibleTypes": [
        "Prob_Tornado",
        "Prob_Severe",
    ],
    "hazardCategoriesAndTypes": [
        {
        "displayString": "Prob Convective",
        "children": [
        "Prob_Tornado",
        "Prob_Severe",
        ]
        }
    ],
    #"hazardsFilter" : "Hydrology_All",
    "defaultTimeDisplayDuration": 172800000,
    "defaultCategory" : "Prob Convective",
    "mapCenter": {
        "lat": 41.06,
        "lon":-95.91,
        "zoom": 7
    },
    # The following variable needs to be overridden at the site level e.g. 
    #    Example:  "possibleSites": ["BOU","PUB","GJT","CYS","OAX","FSD","DMX","GID","EAX","TOP","RAH"],
    "possibleSites": [ ],
    # The following variable needs to be overridden at the site level
    #    Example:  "visibleSites":  ["BOU", "OAX"]
    "visibleSites": [],
    "defaultDuration": 28800000,
    "visibleColumns": [
        "Event ID",
        "Hazard Type",
        "Status",
        "Stream",
        "Point ID",
        "Start Time",
        "End Time",
    ],
    "visibleStatuses": [
        "potential",
        "proposed",
        "pending",
        "issued",
        "elapsed",
        "ending",
        "ended"
    ],
    "columns": {
        "Event ID": {
            "type": "string",
            "fieldName": "displayEventID",
            "sortDir": "none"
        },
        "Hazard Type": {
            "type": "string",
            "fieldName": "type",
            # "sortPriority": 1,
            "sortDir": "ascending",
            "hintTextFieldName": "headline",
            "displayEmptyAs": "Undefined"
            
        },
        "Status": {
            # "sortPriority": 2,
            "sortDir": "ascending",
            "width": 61,
            "fieldName": "status",
            "type": "string"
        },
        "Start Time": {
            "sortDir": "none",
            "width": 125,
            "fieldName": "startTime",
            "type": "date"
        },
        "End Time": {
            "sortDir": "none",
            "width": 125,
            "fieldName": "endTime",
            "type": "date"
        },
        "Phen": {
            "sortDir": "none",
            "width": 50,
            "fieldName": "phen",
            "type": "string"
        },
        "Sig": {
            "sortDir": "none",
            "width": 50,
            "fieldName": "sig",
            "type": "string"
        },
        "Expiration Time": {
            "sortDir": "none",
            "width": 123,
            "fieldName": "expirationTime",
            "type": "date"
        },
        "Creation Time": {
            "sortDir": "none",
            "width": 122,
            "fieldName": "creationTime",
            "type": "date"
        },
        "Issue Time": {
            "sortDir": "none",
            "width": 122,
            "fieldName": "issueTime",
            "type": "date"
        },
        "Site ID": {
            "type": "string",
            "fieldName": "siteID",
            "sortDir": "none"
        },
        "VTEC Codes": {
            "type": "string",
            "fieldName": "vtecCodes",
            "sortDir": "none"
        },
        "ETNs": {
            "type": "string",
            "fieldName": "etns",
            "sortDir": "none"
        },
        "PILs": {
            "type": "string",
            "fieldName": "pils",
            "sortDir": "none"
        },
        "Time to Expiration": {
            "sortDir": "none",
            "fieldName": "alert",
            "type": "countdown"
        },
        "Point ID": {
            "sortDir": "none",
            "fieldName": "pointID",
            "type": "string"
        },
        "River Mile": {
            "sortPriority": 2,
            "sortDir": "ascending",
            "fieldName": "riverMile",
            "type": "number"
        },
        "Stream": {
            "sortPriority": 1,
            "sortDir": "ascending",
            "fieldName": "streamName",
            "type": "string"
        },
        "Workstation": {
            "sortDir": "none",
            "fieldName": "workStation", 
            "type": "string"
        },
        "User Name": {
            "sortDir": "none",
            "fieldName": "userName", 
            "type": "string"
        },
    },
    "toolbarTools": [
#         {
#             "toolName": "DamBreakFloodRecommender",
#             "displayName": "Dam/Levee Break Flood Recommender",
#             "toolType": "RECOMMENDER",
#             "visible":True,
#         },
    ],
    # eventIdDisplayType is one of:  "ALWAYS_FULL", "FULL_ON_DIFF", "PROG_ON_DIFF", "ALWAYS_SITE", "ONLY_SERIAL"  
    #"eventIdDisplayType" : "ALWAYS_FULL"
    "eventIdDisplayType" : "ONLY_SERIAL"
}
