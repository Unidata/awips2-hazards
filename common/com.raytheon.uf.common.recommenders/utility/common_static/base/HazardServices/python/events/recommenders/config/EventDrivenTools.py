#
# Event-Driven Tools
#
#   A list of tool sequences that are to be run in response to different events occurring, as
#   indicated by the value of "triggerType" in each dictionary. The value may be any of the
#   following:
#
#         TIME_INTERVAL     Triggered at regular intervals when the CAVE clock is ticking
#                           forward.
#
#         FRAME_CHANGE      Triggered whenever the frame changes due to the user pressing
#                           the step forward or back buttons, etc.
#
#                           Note that only one entry of this type may be present, though it
#                           may of course run multiple tools in sequence. If more than one
#                           is found, Hazard Service's behavior is undefined. 
#
#         DATA_LAYER_CHANGE In the D2D perspective, triggered whenever the Time Match Basis
#                           (TMB) product changes (i.e. a new TMB is selected), or if the
#                           TMB's data times themselves change.
#
#                           Note that only one entry of this type may be present, though it
#                           may of course run multiple tools in sequence. If more than one
#                           is found, Hazard Service's behavior is undefined. 
# 
#   Each dictionary in the list also has the following mandatory and optional entries:
#
#         toolType          Type of the tools to be run; must be a string in all-caps matching
#                           one of the enumerated type choices defined in ToolType.java (i.e.
#                           "RECOMMENDER", "HAZARD_PRODUCT_GENERATOR", or
#                           "NON_HAZARD_PRODUCT_GENERATOR").
#
#         toolIdentifiers   List of identifiers of the tools (e.g. "FlashFloodRecommender")
#                           to be run in the sequence they are specified, with each one in the
#                           sequence only commencing execution when the previous one has
#                           completed. Duplicate entries (the same identifier more than once
#                           in this list) are not allowed. The list may contain only a single
#                           identifier if desired, of course.
#
#         intervalMinutes   (Only for TIME_INTERVAL) Integer providing the number of minutes
#                           that should elapse between executions of this sequence (assuming
#                           the CAVE clock is not frozen).
#
#   Note that when Hazard Services first starts up, TIME_INTERVAL entries in this list will
#   be run once. Subsequent executions will occur at the intervals given (if CAVE time is not
#   frozen), or whenever the CAVE time is changed, frozen, or unfrozen.
#

_CENTRAL_PROCESSOR = False 

EventDrivenTools = []
if _CENTRAL_PROCESSOR:
     cpEntry = { "toolType": "RECOMMENDER", "toolIdentifiers": [ "ConvectiveRecommender", "PHI_GridRecommender"],
                      "triggerType": "TIME_INTERVAL", "intervalMinutes": 1 }
     EventDrivenTools.append(cpEntry)
else:
#     cpEntry = { "toolType": "RECOMMENDER", "toolIdentifiers": [ "SwathRecommender" ], 
#                        "triggerType": "TIME_INTERVAL", "intervalMinutes": 1 }
     cpEntry = { "toolType": "RECOMMENDER", "toolIdentifiers": [ "SwathRecommender" ], 
                        "triggerType": "DATA_LAYER_CHANGE", "dataTypes": [ "RADAR" ]}
     EventDrivenTools.append(cpEntry)

    
