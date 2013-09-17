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
    "hazardsFilter": "WSW", 
    "radar": "true", 
    "defaultBaseLayer": "Google Hybrid", 
    "controlLayers": [
        "Hazards", 
        "LandScan Population", 
        "CO Gov", 
        "CO Airports", 
        "USA WFO Locations", 
        "USA EM State Offices", 
        "CO EOC Offices"
    ], 
    "caveSettings": "True", 
    "pointHazardShape": "triangle", 
    "hazardCategoriesAndTypes": [
        {
            "displayString": "Winter Weather", 
            "children": [
                "WS.W", 
                "BZ.W"
            ]
        }
    ], 
    "draggedTrackPointShape": "star", 
    "defaultControlLayers": [
        "Hazards"
    ], 
    "defaultTimeDisplayDuration": 86400000, 
    "trackPointShape": "circle", 
    "maxDuration": 3600, 
    "durationIncrement": 60, 
    "mapCenter": {
        "lat": 41.06, 
        "lon": -95.91, 
        "zoom": 7
    }, 
    "defaultSiteID": "OAX", 
    "temporalCounts": {
        "distance": 12, 
        "step": 2000, 
        "panDistance": 14400000, 
        "loopCount": 0, 
        "increment": 600000
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
        "State", 
        "Time Remaining",
        "Start Time", 
        "End Time"
    ], 
    "defaultImageLayers": [
        "snowAccumulation"
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
            "sortDir": "none",
        }, 
        "Hazard Type": {
            "type": "string", 
            "fieldName": "type", 
            "sortDir": "ascending",
            "hintTextFieldName": "headline"
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
    ], 
    "defaultHazardCategory": "longFused"
}
