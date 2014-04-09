Flood = {
    "settingsID" : "Flood",
    "perspectiveIDs" : ["com.raytheon.viz.hydro.HydroPerspective",
                        "com.raytheon.viz.mpe.ui.MPEPerspective"],
    "displayName": "Canned Flood", 
    "visibleTypes": [
        "FF.A", 
        "FF.W.Convective", 
        "FF.W.NonConvective", 
        "FA.Y", 
        "FA.A", 
        "FA.W", 
        "FL.Y", 
        "FL.A", 
        "FL.W"
    ], 
    "hazardCategoriesAndTypes": [
        {
            "displayString": "Hydrology", 
            "children": [
                "FF.A", 
                "FF.W.Convective", 
                "FF.W.NonConvective", 
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
        "State", 
        "Time Remaining",
        "Start Time", 
        "End Time"
    ], 
    "visibleStates": [
        "potential",
        "proposed",
        "pending",
        "issued",
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
            "sortDir": "ascending",
            "hintTextFieldName": "headline",
            "displayEmptyAs": "Undefined"
            
        }, 
        "State": {
             "sortDir": "none", 
            "width": 61, 
            "fieldName": "state", 
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
