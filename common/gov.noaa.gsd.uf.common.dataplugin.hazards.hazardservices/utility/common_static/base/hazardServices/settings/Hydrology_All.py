Hydrology_All = {
    "settingsID" : "Hydrology_All",
    "perspectiveIDs" : ["com.raytheon.viz.hydro.HydroPerspective",
                        "com.raytheon.viz.mpe.ui.MPEPerspective",
                        "com.raytheon.uf.viz.d2d.ui.perspectives.D2D5Pane",
                        "com.raytheon.viz.ui.GFEPerspective"],
    "displayName": "Hydrology - All",
    "visibleTypes": [
        "FF.A",
        "FF.W.Convective",
        "FF.W.NonConvective",
        "FF.W.BurnScar",
        "FA.Y",
        "FA.A",
        "FA.W",
        "FL.Y",
        "FL.A",
        "FL.W",
        "HY.O",
        "HY.S"
    ],
    "hazardCategoriesAndTypes": [
        {
        "displayString": "Hydrology",
        "children": [
        "FF.A",
        "FF.W.Convective",
        "FF.W.NonConvective",
        "FF.W.BurnScar",
        "FA.Y",
        "FA.A",
        "FA.W",
        "FL.Y",
        "FL.A",
        "FL.W",
        "HY.O",
        "HY.S"
        ]
        }
    ],
    #"hazardsFilter" : "Hydrology_All",
    "defaultTimeDisplayDuration": 172800000,
    "defaultCategory" : "Hydrology",
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
            "fieldName": "eventID",
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
        "Description": {
            "sortDir": "none",
            "width": 100,
            "fieldName": "description",
            "type": "string"
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
            "toolName": "DamBreakFloodRecommender",
            "displayName": "Dam/Levee Break Flood Recommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "BurnScarFloodRecommender",
            "displayName": "Burn Scar Flood Recommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "RiverFloodRecommender",
            "displayName": "River Flood Recommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "FlashFloodRecommender",
            "displayName": "Flash Flood Recommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "StormTrackTool",
            "displayName": "Storm Track",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "ModifyStormTrackTool",
            "toolType": "RECOMMENDER",
            "visible":False,
        },
        {
            "toolName": "RVS_ProductGenerator",
            "displayName": "Generate RVS",
            "toolType": "NON_HAZARD_PRODUCT_GENERATOR",
            "visible":True,
        }
    ]
}
