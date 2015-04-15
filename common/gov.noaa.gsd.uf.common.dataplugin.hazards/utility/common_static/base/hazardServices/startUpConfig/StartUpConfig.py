



StartUpConfig = {
    "Alerts": [],
    
    # Title text that is to be shown in the tabs of the HID for each event.
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
    # shown in that event's title text in the HID tab.
    "hazardDetailTabText" : [ "eventID", "hazardType", "pointID" ],
    
    # Flag indicating whether or not the scale bar with two sliders on it
    # should be shown below the start-end time UI element in the HID.
    "showHazardDetailStartEndTimeScale": False,

    # Flag indicating whether or not the HID's layout should be optimized
    # for a wider window.
    "hazardDetailWide": False,  
    
    # PIL order in which Product Generation should take place.
    "disseminationOrder" : [ 'FFW', 'FLW', 'FFS', 'FLS', 'FFA'],
    "Console": {
                "TimeLineNavigation": "onToolBar", # "onToolBar" or "belowTimeLine",
                },
    "gagePointFirstRecommender" : "RiverFloodRecommender"
    }
