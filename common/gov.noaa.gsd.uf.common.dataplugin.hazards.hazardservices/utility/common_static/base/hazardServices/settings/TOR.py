TOR = {
    "settingsID" : "TOR",
    "perspectiveIDs" : ["com.raytheon.uf.viz.d2d.ui.perspectives.D2D5Pane"],
    "displayName": "", 
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
    #The following variable needs to be overridden at the site level e.g. 
    #    Example:  "possibleSites": ["BOU","PUB","GJT","CYS","OAX","FSD","DMX","GID","EAX","TOP","RAH"],
    "possibleSites": [],
    #The following variable needs to be overridden at the site level
    #    Example:  "visibleSites":  ["BOU", "OAX"]
    "visibleSites": [], 
    "defaultDuration": 1800000, 
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
        "Time to Expiration": {
            "sortDir": "none", 
            "fieldName": "alert", 
            "type": "countdown"
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
    ] 
}
