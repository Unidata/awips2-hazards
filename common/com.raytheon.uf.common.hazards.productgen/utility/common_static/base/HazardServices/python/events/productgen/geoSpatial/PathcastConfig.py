'''
    Description: Configuration file for PathCast.
    
    maxCount - Max number of cities list for a single trackpoint.
    maxGroup - Max number of groups of cities to include in the product for a pathcast.
    withInPolygon - Whether or not all the locations must be with the warning polygon.
    thresholdInMiles - Distance to buffer the Storm Track when determining which locations to use.
    areaSource, areaField, parentAreaField - Used to determine what table and columns to query to 
                                             to gather area features for the StormTrack. Currently 
                                             this is county based. But may need updated to also 
                                             handle zone/marine zone when more hazard types are 
                                             implemented.
    pointSource, pointField - Used to determine what table and columns to query to gather the locations
                              to be used in the pathcast.

    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Aug 08, 2016 21056      Robert.Blum Initial creation
'''
PathcastConfig = {
                  "maxCount" : 10,
                  "maxGroup" : 8,
                  "withInPolygon" : True,
                  "thresholdInMiles" : 8.0,
                  "areaSource" : "county",
                  "areaField" : "countyName",
                  "parentAreaField" : "state",
                  "pointSource" : "warngenloc",
                  "pointField" : "name",
                  }
