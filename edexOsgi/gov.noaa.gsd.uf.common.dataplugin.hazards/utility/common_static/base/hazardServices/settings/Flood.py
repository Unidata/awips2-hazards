Flood = {
    "settingsID" : "Flood",
    "displayName": "Canned Flood", 
    "hidHazardCategories": [
        "Hydrology"
    ], 
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
    "defaultBaseLayer": "Google Physical", 
    "hazardsFilter": "Flood", 
    "radar": "true", 
    "defaultVectorLayers": [
        "riverPoints"
    ], 
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
    "draggedTrackPointShape": "star", 
    "defaultControlLayers": [
        "Hazards"
    ], 
    "defaultTimeDisplayDuration": 172800000, 
    "trackPointShape": "circle", 
    "maxDuration": 7260, 
    "durationIncrement": 60, 
    "mapCenter": {
        "lat": 40.1, 
        "lon": -104.4, 
        "zoom": 7
    }, 
    "defaultSiteID": "BOU", 
    "temporalCounts": {
        "distance": 12, 
        "step": 2000, 
        "panDistance": 14400000, 
        "loopCount": 0, 
        "increment": 600000
    }, 
    "defaultCategory": "Hydrology", 
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
        "Purge Time": {
            "sortDir": "none", 
            "width": 123, 
            "fieldName": "purgeTime", 
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
    ], 
    "defaultHazardCategory": "hydro"
}
