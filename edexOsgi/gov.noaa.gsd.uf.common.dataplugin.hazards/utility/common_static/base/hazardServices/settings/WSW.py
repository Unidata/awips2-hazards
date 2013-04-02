WSW = {
    "settingsID": "WSW", 
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
        "lat": 39.3, 
        "lon": -104.7, 
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
            "type": "string", 
            "fieldName": "state", 
            "sortDir": "none"
        }, 
        "Start Time": {
            "type": "date", 
            "fieldName": "startTime", 
            "sortDir": "none"
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
            "width": 126, 
            "fieldName": "purgeTime", 
            "type": "date"
        }, 
        "Issue Time": {
            "sortDir": "none", 
            "width": 120, 
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
        {
            "toolName": "WSW_recommender", 
            "displayName": "WSW Recommender"
        }, 
        {
            "toolName": "CensusTool", 
            "displayName": "Census Data"
        }
    ], 
    "defaultHazardCategory": "longFused"
}
