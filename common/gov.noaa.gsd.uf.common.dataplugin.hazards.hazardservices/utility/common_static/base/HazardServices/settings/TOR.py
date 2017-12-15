    # Settings 
    #
    # NOTE: The following can be added to a Settings file to trump the values in StartUpConfig
    #     "mapCenter", "possibleSites", "visibleSites", "eventIdDisplayType"
    # Examples:  
    #"mapCenter": {
    #    "lat": 41.06,
    #    "lon":-95.91,
    #    "zoom": 7
    #},
    #"possibleSites": ["BOU","PUB","GJT","CYS","OAX","FSD","DMX","GID","EAX","TOP","RAH"],
    #"visibleSites":  ["BOU", "OAX"]
    #
    # eventIdDisplayType is one of:  "ALWAYS_FULL", "FULL_ON_DIFF", "PROG_ON_DIFF", "ALWAYS_SITE", "ONLY_SERIAL"  
    #"eventIdDisplayType" : "PROG_ON_DIFF",

TOR = {
    "settingsID" : "TOR",
    "perspectiveIDs" : ["com.raytheon.uf.viz.d2d.ui.perspectives.D2D5Pane"],
    "displayName": "", 
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
    "defaultDuration": 1800000, 
    "visibleColumns": [
        "Event ID",
        "Lock Status",
        "Hazard Type", 
        "Status",
        "Time Remaining",
        "Start Time", 
        "End Time"
    ], 
    "visibleStatuses": [
        "potential",
        "proposed",
        #"pending",
        "issued",
        "ending",
    ],  
    "columns": {
        "Event ID": {
            "type": "string", 
            "fieldName": "displayEventID", 
            "sortDir": "none"
        }, 
        "Lock Status": {
            "type": "string",
            "fieldName": "lockStatus",
            "sortDir": "none",
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
            "sortDir": "none",
            "displayEmptyAs": "[]"
        }, 
        "ETNs": {
            "type": "string", 
            "fieldName": "etns", 
            "sortDir": "none",
            "displayEmptyAs": "[]"
        }, 
        "PILs": {
            "type": "string", 
            "fieldName": "pils", 
            "sortDir": "none",
            "displayEmptyAs": "[]"
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
    ],

}

