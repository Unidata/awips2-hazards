#
# Event-Driven Tools
#
#   A list of tool sequences that are to be run at regular intervals when the CAVE clock is
#   ticking forward.  Each one is represented by a dictionary containing the following entries:
#
#         type            Type of the tools to be run; must be a string in all-caps matching
#                         one of the enumerated type choices defined in ToolType.java (i.e.
#                         "RECOMMENDER", "HAZARD_PRODUCT_GENERATOR", or
#                         "NON_HAZARD_PRODUCT_GENERATOR").
#
#         identifiers     List of identifiers of the tools (e.g. "FlashFloodRecommender") to
#                         be run in the sequence they are specified, with each one in the
#                         sequence only commencing execution when the previous one has
#                         completed. Duplicate entries (the same identifier more than once
#                         in this list) are not allowed. The list may contain only a single
#                         identifier if desired, of course.
#
#         intervalMinutes Integer providing the number of minutes that should elapse between
#                         executions of this sequence (assuming the CAVE clock is not frozen).
#
#   Note that when Hazard Services first starts up, all sequences  in this list will be run
#   once. Subsequent executions will occur at the intervals given (if CAVE time is not
#   frozen), or whenever the CAVE time is changed, frozen, or unfrozen.
#
EventDrivenTools = [
                   #{ "toolType": "RECOMMENDER", "toolIdentifiers": [ "ConvectiveRecommender" ], "triggerType": "TIME_INTERVAL", "intervalMinutes": 1 },
                   { "toolType": "RECOMMENDER", "toolIdentifiers": [ "SwathRecommender" ], "triggerType": "DATA_LAYER_CHANGE", "dataTypes": [ "RADAR" ] },
                   ]
