TOR = {
    "settingsID" : "TOR",
    "perspectiveIDs" : ["com.raytheon.uf.viz.d2d.ui.perspectives.D2D5Pane"],
    "displayName": "Canned TOR", 
    "hidHazardCategories": [
        "Short Fused"
    ], 
    "visibleTypes": [
        "TO.W", 
        "SV.W", 
        "EW.W"
    ], 
    "hazardCategoriesAndTypes": [
        {
            "displayString": "Convective", 
            "children": [
                "TO.W", 
                "SV.W", 
                "EW.W"
            ]
        }
    ], 
    "defaultTimeDisplayDuration": 14400000, 
    "maxDuration": 90, 
    "durationIncrement": 1, 
    "mapCenter": {
        "lat": 41.06, 
        "lon": -95.91, 
        "zoom": 7
    }, 
    "defaultSiteID": "OAX", 
    "defaultCategory": "Convective", 
    "visibleSites": [
        "BOU", 
        "OAX"
    ], 
    "defaultDuration": 1800000, 
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
            "type": "number", 
            "fieldName": "etns", 
            "sortDir": "none"
        }, 
        "PILs": {
            "type": "number", 
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
        }
    }, 
    "toolbarTools": [
        {
            "toolName": "StormTrackTool", 
            "displayName": "Storm Track"
        }
    ] 
}
