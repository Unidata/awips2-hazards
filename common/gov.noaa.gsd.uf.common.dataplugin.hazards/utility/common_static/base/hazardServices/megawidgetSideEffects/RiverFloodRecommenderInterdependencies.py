# Interdependencies script for the River Flood Recommender dialog box's megawidgets.
# Ensures that if a forecast type is chosen, the forecast confidence percentage
# spinner/scale megawidget is disabled or enabled as appropriate, and is set to
# a preset value as appropriate. See the Java class PythonInterdependenciesApplier
# for info as to the arguments and return type required of this function.
#
#    Feb 14, 2014    2161     Chris.Golden        Initial creation.
#    Feb 24, 2014    2161     Bryon.Lawrence      Modified to include new
#                                                 megawidgets in dialog (code
#                                                 adapted by Chris.Golden and
#                                                 moved into this file).
#    Jun 17, 2014    3982     Chris.Golden        Changed megawidget 'side effects'
#                                                 to 'interdependencies', and
#                                                 changed to use simpler way of
#                                                 getting and setting values for
#                                                 single-state megawidgets' mutable
#                                                 properties.
#
def applyInterdependencies(triggerIdentifiers, mutableProperties):
   if triggerIdentifiers == None or "forecastType" in triggerIdentifiers:
      if mutableProperties["forecastType"]["values"] == "Watch":
         return { "forecastConfidencePercentage": { "enable": False, "values": 50 }, "includeNonFloodPoints": { "enable": False, "values": False } }
      elif mutableProperties["forecastType"]["values"] == "Warning":
         return { "forecastConfidencePercentage": { "enable": False, "values": 80 }, "includeNonFloodPoints": { "enable": True } }
      else:
         return { "forecastConfidencePercentage": { "enable": True } }
   elif "forecastConfidencePercentage" in triggerIdentifiers:
      if mutableProperties["forecastConfidencePercentage"]["values"] >= 80:
         return {"includeNonFloodPoints":{ "enable": True } }
      else:
         return {"includeNonFloodPoints":{ "enable": False, "values": False } }
   else:
      return None
