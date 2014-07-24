WSW = {
    "settingsID": "WSW",
    "perspectiveIDs": ["com.raytheon.viz.ui.GFEPerspective"], 
    "displayName": "Canned WSW", 
    "hidHazardCategories": [
        "Winter Weather"
    ], 
    "visibleTypes": [
        "WS.W", 
        "BZ.W"
    ], 
    "hazardCategoriesAndTypes": [
        {
            "displayString": "Winter Weather", 
            "children": [
                "WS.W", 
                "BZ.W"
            ]
        }
    ], 
    "defaultTimeDisplayDuration": 86400000, 
    "maxDuration": 3600, 
    "durationIncrement": 60, 
    "mapCenter": {
        "lat": 41.06, 
        "lon": -95.91, 
        "zoom": 7
    }, 
    "defaultCategory": "Winter Weather", 
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
            "sortDir": "ascending",
            "hintTextFieldName": "headline",
            "displayEmptyAs": "Undefined"
        }, 
        "Status": {
             "sortDir": "none", 
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
        }
    },          
    "toolbarTools": [
    ] 
}
