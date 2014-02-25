# Side effects script for the River Flood Recommender dialog box's megawidgets.
# Ensures that if a forecast type is chosen, the forecast confidence percentage
# spinner/scale megawidget is disabled or enabled as appropriate, and is set to
# a preset value as appropriate. See the Java class PythonSideEffectsApplier for
# info as to the arguments and return type required of this function.
#
#    Feb 14, 2014    2161     Chris.Golden        Initial creation.
#    Feb 24, 2014    2161     Bryon.Lawrence      Modified to include new
#                                                 megawidgets in dialog (code
#                                                 adapted by Chris.Golden and
#                                                 moved into this file).
#
def applySideEffects(triggerIdentifiers, mutableProperties):
   if triggerIdentifiers == None or "forecastType" in triggerIdentifiers:
      if mutableProperties["forecastType"]["values"]["forecastType"] == "Watch":
         return { "forecastConfidencePercentage": { "enable": False, "values": { "forecastConfidencePercentage": 50 } }, "includeNonFloodPoints": { "enable": False, "values": { "includeNonFloodPoints": False } } }
      elif mutableProperties["forecastType"]["values"]["forecastType"] == "Warning":
         return { "forecastConfidencePercentage": { "enable": False, "values": { "forecastConfidencePercentage": 80 } }, "includeNonFloodPoints": { "enable": True } }
      else:
         return { "forecastConfidencePercentage": { "enable": True } }
   elif "forecastConfidencePercentage" in triggerIdentifiers:
      if mutableProperties["forecastConfidencePercentage"]["values"]["forecastConfidencePercentage"] >= 80:
         return {"includeNonFloodPoints":{ "enable": True } }
      else:
         return {"includeNonFloodPoints":{ "enable": False, "values": { "includeNonFloodPoints": False } } }
   else:
      return None
