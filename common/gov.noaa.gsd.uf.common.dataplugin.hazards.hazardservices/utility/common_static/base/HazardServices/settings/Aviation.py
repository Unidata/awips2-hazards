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


Aviation = {
    "settingsID" : "Aviation",
    "perspectiveIDs" : [],
#    "perspectiveIDs" : ["com.raytheon.viz.hydro.HydroPerspective",
#                        "com.raytheon.viz.mpe.ui.MPEPerspective",
#                        "com.raytheon.uf.viz.d2d.ui.perspectives.D2D5Pane",
#                        "com.raytheon.viz.ui.GFEPerspective"],    
    "displayName": "Aviation",
    "possibleSites": ["National"],
    "visibleSites": ["National"],    
    "visibleTypes": [
        "SIGMET.NonConvective",
        "SIGMET.Convective",
        "SIGMET.International",
        "VAA",
        "LLWS",
        "Strong_Surface_Wind",
        "Turbulence",
        "Mountain_Obscuration",
        "IFR",
        "Icing",
        "Multiple_Freezing_Levels",
    ],
    "hazardCategoriesAndTypes": [
        {
        "displayString": "Aviation",
        "children": [
        "SIGMET.NonConvective",
        "SIGMET.Convective",
        "SIGMET.International",
        "VAA",
        "LLWS",
        "Strong_Surface_Wind",
        "Turbulence",
        "Mountain_Obscuration",
        "IFR",
        "Icing",
        "Multiple_Freezing_Levels",          
        ]
        },
    ],
    "defaultTimeDisplayDuration": 172800000,
    "defaultCategory" : "Aviation",
    "defaultDuration": 28800000,
    "visibleColumns": [
        "Event ID",
        "Object ID",
        "Hazard Type",
        "Status",
        "Start Time",
        "End Time",
    ],
    "visibleStatuses": [
        "potential",
        "proposed",
        "issued",
    ],
    "columns": {
        "Event ID": {
            "type": "string",
            "fieldName": "displayEventID",
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
        "Object ID": {
            "sortDir": "none",
            "fieldName": "objectID",
            "type": "string"
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
            "toolName": "UpdateTimesTool",
            "displayName": "Update times to next hour",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "CopyMetaDataTool",
            "displayName": "Copy metadata from event",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "PasteMetaDataTool",
            "displayName": "Paste metadata to new event",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "TurbulenceAndIcingRecommender",
            "displayName": "TurbulenceAndIcingRecommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "CreateHazardEventFromPoints",
            "displayName": "CreateHazardEventFromPoints",
            "toolType": "RECOMMENDER",
            "visible":True,
        },                                          
        {
            "toolName": "StormSurgeRecommender",
            "displayName": "StormSurgeRecommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        },                                                                                                                                                                                                
    ],
    "eventIdDisplayType" : "ONLY_SERIAL"
}