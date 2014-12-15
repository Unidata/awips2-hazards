Flood = {
    "settingsID" : "Flood",
    "perspectiveIDs" : ["com.raytheon.viz.hydro.HydroPerspective",
                        "com.raytheon.viz.mpe.ui.MPEPerspective"],
    "displayName": "Canned Flood", 
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
                "FL.W"
            ]
        }
    ], 
    "hazardsFilter" : "Flood",     
    "defaultTimeDisplayDuration": 172800000, 
    "defaultCategory" : "Hydrology",
    "mapCenter": {
        "lat": 41.06, 
        "lon": -95.91, 
        "zoom": 7
    }, 
    "visibleSites": [
        "BOU", 
        "OAX"
    ], 
    "defaultDuration": 28800000, 
    "visibleColumns": [
        "Event ID",
        "Hazard Type", 
        "Status", 
        "Time Remaining",
        "Start Time", 
        "End Time"
    ], 
    "visibleStatuses": [
        "potential",
        "proposed",
        "pending",
        "issued",
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
            "sortPriority": 1,
            "sortDir": "ascending",
            "hintTextFieldName": "headline",
            "displayEmptyAs": "Undefined"
            
        }, 
        "Status": {
            "sortPriority": 2,
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
        "Time Remaining": {
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
            "sortDir": "none", 
            "fieldName": "riverMile", 
            "type": "number"
        },
        "Stream": {
            "sortDir": "none", 
            "fieldName": "streamName", 
            "type": "string"
        },
    }, 
    "toolbarTools": [
        {
            "toolName": "DamBreakFloodRecommender", 
            "displayName": "Dam/Levee Break Flood Recommender"
        }, 
        {
            "toolName": "BurnScarFloodRecommender", 
            "displayName": "Burn Scar Flood Recommender"
        }, 
        {
            "toolName": "RiverFloodRecommender", 
            "displayName": "River Flood Recommender"
        },
        {
            "toolName": "FlashFloodRecommender", 
            "displayName": "Flash Flood Recommender"
        },
        {
            "toolName": "StormTrackTool", 
            "displayName": "Storm Track"
        }
    ]
}
