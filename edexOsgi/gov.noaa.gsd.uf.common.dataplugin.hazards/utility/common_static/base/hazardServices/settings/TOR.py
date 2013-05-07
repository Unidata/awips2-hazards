TOR = {
    "settingsID" : "TOR",
    "displayName": "Canned TOR", 
    "hidHazardCategories": [
        "Short Fused"
    ], 
    "visibleTypes": [
        "TO.W", 
        "SV.W", 
        "EW.W"
    ], 
    "defaultBaseLayer": "Google Hybrid", 
    "hazardsFilter": "TOR", 
    "radar": "true", 
    "defaultVectorLayers": [], 
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
            "displayString": "Convective", 
            "children": [
                "TO.W", 
                "SV.W", 
                "EW.W"
            ]
        }
    ], 
    "draggedTrackPointShape": "star", 
    "defaultControlLayers": [
        "Hazards"
    ], 
    "defaultTimeDisplayDuration": 14400000, 
    "trackPointShape": "circle", 
    "maxDuration": 90, 
    "durationIncrement": 1, 
    "mapCenter": {
        "lat": 39.3, 
        "lon": -104.7, 
        "zoom": 7
    }, 
    "defaultSiteID": "BOU", 
    "temporalCounts": {
        "distance": 12, 
        "step": 2000, 
        "panDistance": 7200000, 
        "loopCount": 0, 
        "increment": 300000
    }, 
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
        "Start Time", 
        "End Time"
    ], 
    "defaultImageLayers": [], 
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
            "width": 60, 
            "fieldName": "state", 
            "type": "string"
        }, 
        "Start Time": {
            "sortDir": "none", 
            "width": 126, 
            "fieldName": "startTime", 
            "type": "date"
        }, 
        "End Time": {
            "sortDir": "none", 
            "width": 128, 
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
        "Purge Time": {
            "sortDir": "none", 
            "width": 130, 
            "fieldName": "purgeTime", 
            "type": "date"
        }, 
        "Issue Time": {
            "sortDir": "none", 
            "width": 123, 
            "fieldName": "issueTime", 
            "type": "date"
        }, 
        "Site ID": {
            "type": "string", 
            "fieldName": "siteID", 
            "sortDir": "none"
        }, 
        "VTEC Code": {
            "type": "string", 
            "fieldName": "vtecCode", 
            "sortDir": "none"
        }, 
        "ETN": {
            "type": "number", 
            "fieldName": "ETN", 
            "sortDir": "none"
        }, 
        "Description": {
            "sortDir": "none", 
            "width": 100, 
            "fieldName": "description", 
            "type": "string"
        }
    }, 
    "toolbarTools": [
    ], 
    "defaultHazardCategory": "shortFused"
}
