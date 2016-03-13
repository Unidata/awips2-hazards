



StartUpConfig = {                                                   
    #########################
    "isNational": True, #False, 
    
    #########################
    #  MUST OVERRIDE!!
    #  Site Configuration - The following MUST BE overridden at the site level 
    
    # Map Center -- The Spatial Display will center on this lat / lon by default with the given zoom level
    "mapCenter": {
        "lat": 41.06,
        "lon":-95.91,
        "zoom": 7
    },
    # Possible Sites -- Hazards from these sites can be selected to be visible in the Hazard Services display.
    #    They will appear in the Settings dialog as a check list from which to choose
    # Example:  "possibleSites": ["BOU","PUB","GJT","CYS","OAX","FSD","DMX","GID","EAX","TOP","RAH"],
    "possibleSites": ["National"],
    
    # Visible Sites -- Hazards from these sites will be, by default, visible in the Hazard Services display
    # Example:  "visibleSites":  ["BOU", "OAX"]
    "visibleSites": ["National"],
    
    # Backup Sites 
    # Example:  "backupSites":  ["PUB", "GJT"]
    "backupSites": ["National"],

    # Directory of mounted X.400 directory where exported Site Config data is stored.
    "siteBackupBaseDir" : "CHANGEME",

    # NOTE: The following can be added to a Settings file to trump the values in StartUpConfig
    #     "mapCenter", "possibleSites", "visibleSites", "eventIdDisplayType"
    
    #########################
    
    #########################
    #  General Display
    # eventIdDisplayType is one of:  "ALWAYS_FULL", "FULL_ON_DIFF", "PROG_ON_DIFF", "ALWAYS_SITE", "ONLY_SERIAL"  
    "eventIdDisplayType" : "FULL_ON_DIFF",
       
    #########################
    # Hazard Information Dialog
    #    
    # Title text that is to be shown in the tabs of the Hazard Information Dialog for each event.
    # Must be a list of strings, with each string being one of the following:
    #
    #    eventID        Event identifier.
    #    siteID         Site identifier.
    #    status         Event status (e.g. issued)
    #    phenomenon     Hazard phenomenon (e.g. FF)
    #    significance   Hazard significance (e.g. W)
    #    subType        Hazard sub-type, if any (e.g. NonConvective) 
    #    hazardType     Hazard type (e.g. FF.W.NonConvective)
    #    <attrName>     See below.
    #
    # Any string that does not match any of the literals above is treated as
    # a hazard attribute name, is then used to pull out the corresponding
    # attribute value, if any. An example of an attribute name is pointID.
    #
    # Note that any element of this list that yields an empty string is
    # skipped, so if the list is [ "eventID", "pointID" ] and there is no
    # point ID for a particular hazard event, then only the event ID is
    # shown in that event's title text in the Hazard Information Dialog tab.
    "hazardDetailTabText" : [ "eventID", "hazardType", "pointID" ],
    
    # Flag indicating whether or not the scale bar with two sliders on it
    # should be shown below the start-end time UI element in the Hazard Information Dialog.
    "showHazardDetailStartEndTimeScale": False,

    # Flag indicating whether or not the Hazard Information Dialog's layout should be optimized
    # for a wider window.
    "hazardDetailWide": False,  
    
    # Flag indicating whether to include the Issue button on the Hazard Information Dialog
    "includeIssueButton": True,
    
    #########################
    # Console
    "Console": {
                "TimeLineNavigation": "onToolBar", # "onToolBar" or "belowTimeLine",
                },

    #########################
    # Recommenders
    "gagePointFirstRecommender" : "RiverFloodRecommender",
    
    #########################
    # Product Generation

    # PIL order in which Product Generation should take place.
    "disseminationOrder" : [ 'FFW', 'FLW', 'FFS', 'FLS', 'FFA'],
    
    "Alerts": [],

    }
