#
# Event-Driven Tools
#
#   A list of tools that are to be run at regular intervals when the CAVE clock is ticking
#   forward.  Each one is represented by a dictionary containing the following entries:
#
#         type            Type; must be a string in all-caps matching one of the enumerated
#                         type choices defined in ToolType.java (i.e. "RECOMMENDER",
#                         "HAZARD_PRODUCT_GENERATOR", "NON_HAZARD_PRODUCT_GENERATOR").
#
#         identifier      Identifier of the tool, e.g. "FlashFloodRecommender".
#
#         intervalMinutes Integer providing the number of minutes that should elapse between
#                         executions of this tool (assuming the CAVE clock is not frozen).
#
#   Note that when Hazard Services first starts up, all tools in this list will be run once.
#   Subsequent executions will occur at the intervals given (if CAVE time is not frozen), or
#   whenever the CAVE time is changed, frozen, or unfrozen.
#
EventDrivenTools = [
                    { "type": "RECOMMENDER", "identifier": "ConvectiveRecommender", "intervalMinutes": 2 },
                    #{ "type": "RECOMMENDER", "identifier": "SwathRecommender", "intervalMinutes": 2 },
                    #{ "type": "RECOMMENDER", "identifier": "PHIGridRecommender", "intervalMinutes": 2 }
                   ]
