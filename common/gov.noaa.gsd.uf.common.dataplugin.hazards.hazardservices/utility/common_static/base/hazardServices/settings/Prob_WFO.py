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


Prob_WFO = {
    "settingsID" : "Prob_WFO",
    "perspectiveIDs" : ["com.raytheon.viz.hydro.HydroPerspective",
                        "com.raytheon.viz.mpe.ui.MPEPerspective",
                        "com.raytheon.uf.viz.d2d.ui.perspectives.D2D5Pane",
                        "com.raytheon.viz.ui.GFEPerspective"],
#     "perspectiveIDs" :  ["com.raytheon.viz.hydro.HydroPerspective",
#                         "com.raytheon.viz.mpe.ui.MPEPerspective",
#                         "com.raytheon.uf.viz.d2d.ui.perspectives.D2D5Pane",
#                         "com.raytheon.viz.ui.GFEPerspective"],
    "displayName": "Probabilistic Convective",
    "possibleSites": ["National"],
    "visibleSites": ["National"],
    
    "deselectAfterIssuing": True,
    
    "visibleTypes": [
        "Prob_Tornado",
        "Prob_Severe",
    ],
    "hazardCategoriesAndTypes": [
        {
        "displayString": "Prob Convective",
        "children": [
        "Prob_Tornado",
        "Prob_Severe",
        ]
        }
    ],
    "defaultTimeDisplayDuration": 10000000, #14400000,  # 172800000,
    "defaultCategory" : "Prob Convective",
    "defaultDuration": 28800000,
    "timeResolution": "seconds",
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
        "pending",
        "issued",
        "ending",
        #"ended",
        #"elapsed",
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
            # "sortPriority": 1,
            "sortDir": "ascending",
            "hintTextFieldName": "headline",
            "displayEmptyAs": "Undefined"
            
        },
        "Status": {
            # "sortPriority": 2,
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
        #=======================================================================
        # "VTEC Codes": {
        #     "type": "string",
        #     "fieldName": "vtecCodes",
        #     "sortDir": "none"
        # },
        # "ETNs": {
        #     "type": "string",
        #     "fieldName": "etns",
        #     "sortDir": "none"
        # },
        # "PILs": {
        #     "type": "string",
        #     "fieldName": "pils",
        #     "sortDir": "none"
        # },
        #=======================================================================
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
            "toolName": "PHIConfigurationTool",
            "displayName": "PHI Config Tool",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "ConvectiveRecommender",
            "displayName": "Convective Recommender (PHI)",
            "toolType": "RECOMMENDER",
            "visible":True,
        },
        {
            "toolName": "PHI_GridRecommender",
            "displayName": "PHI Grid Recommender",
            "toolType": "RECOMMENDER",
            "visible":True,
        }
    ],
}
