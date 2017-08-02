    # Settings 
    #
    # NOTE: The following can be added to a Settings file to trump the values in StartUpConfig
    #     "mapCenter", "possibleSites", "visibleSites", "eventIdDisplayType"
    # Examples:  
    #"mapCenter": {
    #    "lat": 41.06,
    #    "lon":-95.91,
    #    "zoom": 7
    #},
    #"possibleSites": ["BOU","PUB","GJT","CYS","OAX","FSD","DMX","GID","EAX","TOP","RAH"],
    #"visibleSites":  ["BOU", "OAX"]
    #
    # eventIdDisplayType is one of:  "ALWAYS_FULL", "FULL_ON_DIFF", "PROG_ON_DIFF", "ALWAYS_SITE", "ONLY_SERIAL"  
    #"eventIdDisplayType" : "PROG_ON_DIFF",

Hydrology_River = {
    "settingsID" : "Hydrology_River",
    "perspectiveIDs" : [],
    "displayName": "", # "Hydrology - River", 
    
    "mapCenter": {
        "lat": 41.06,
        "lon":-95.91,
        "zoom": 7
    },
    "possibleSites": ["BOU","PUB","GJT","CYS","OAX","FSD","DMX","GID","EAX","TOP","RAH"],
    "visibleSites":  ["BOU", "OAX"],

    "visibleTypes": [
#         "FF.A", 
#         "FF.W.Convective", 
#         "FF.W.NonConvective", 
#         "FF.W.BurnScar",
#         "FA.Y", 
#         "FA.A", 
#         "FA.W", 
        "FL.Y", 
        "FL.A", 
        "FL.W",
        #"HY.O",
        "HY.S"
    ], 
    "hazardCategoriesAndTypes": [
        {
        "displayString": "Hydrology", 
        "children": [
#         "FF.A", 
#         "FF.W.Convective", 
#         "FF.W.NonConvective", 
#         "FF.W.BurnScar",
#         "FA.Y", 
#         "FA.A", 
#         "FA.W", 
        "FL.Y", 
        "FL.A", 
        "FL.W",
        #"HY.O",
        "HY.S"
        ]
        }
    ], 
    "defaultTimeDisplayDuration": 172800000, 
    "defaultCategory" : "Hydrology",
    "defaultDuration": 28800000, 
    "visibleColumns": [
        "Event ID",
        "Hazard Type", 
        "Status", 
        "Stream",
        "Point ID",
        "Start Time", 
        "End Time",
        "Expiration Time",
        "VTEC Codes",
    ], 
    "visibleStatuses": [
        "potential",
        "proposed",
        #"pending",
        "issued",
        "elapsed",
        "ending",
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
            #"sortPriority": 1,
            "sortDir": "ascending",
            "hintTextFieldName": "headline",
            "displayEmptyAs": "Undefined"
            
        }, 
        "Status": {
            #"sortPriority": 2,
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
            "sortDir": "none",
            "displayEmptyAs": "[]"
        }, 
        "ETNs": {
            "type": "string", 
            "fieldName": "etns", 
            "sortDir": "none",
            "displayEmptyAs": "[]"
        }, 
        "PILs": {
            "type": "string", 
            "fieldName": "pils", 
            "sortDir": "none",
            "displayEmptyAs": "[]"
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
        {
            "toolName": "RiverFloodRecommender", 
            "displayName": "River Flood Recommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "StormTrackTool", 
            "displayName": "Storm Track",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
    ],

}


