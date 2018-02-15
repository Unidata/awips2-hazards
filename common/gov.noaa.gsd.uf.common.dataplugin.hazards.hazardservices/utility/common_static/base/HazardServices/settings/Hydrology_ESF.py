Hydrology_ESF = {
    "settingsID" : "Hydrology_ESF",
    "perspectiveIDs" : [],                        
    "displayName": "Hydrology - ESF",
    "visibleTypes": [
        "HY.O",
    ],
    "hazardCategoriesAndTypes": [
        {
        "displayString": "Hydrology",
        "children": [
        "HY.O",
        ]
        }
    ],
    "defaultTimeDisplayDuration": 172800000,
    "defaultCategory" : "Hydrology",
    "defaultDuration": 28800000,
    "visibleColumns": [
        "Event ID",
        "Lock Status",
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
        # "pending",
        "issued",
        "ending",
        "elapsing",
    ],
    "columns": {
        "Event ID": {
            "type": "string",
            "fieldName": "displayEventID",
            "sortDir": "none",
            "width": 110,
        },
        "Lock Status": {
            "type": "string",
            "fieldName": "lockStatus",
            "sortDir": "none",
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
        "Location Name" : {
            "type" : "string",
            "fieldName" : "name",
            "sortDir" : "ascending",
            "sortPriority" : 0
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
        }
    ],
}
