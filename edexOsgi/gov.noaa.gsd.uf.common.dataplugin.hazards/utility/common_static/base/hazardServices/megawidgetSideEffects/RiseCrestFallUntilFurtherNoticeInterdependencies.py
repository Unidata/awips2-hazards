# Interdependencies script for hazard event metadata megawidgets that include the
# Rise Above/Crest/Fall Below time scale megawidget and want to allow the use of
# the Until Further Notice option for the Fall Below time. Ensures that if the
# associated Until Further Notice checkbox is checked, the Fall Below thumb in
# the time scale megawidget is disabled. See the Java class
# PythonInterdependenciesApplier for info as to the arguments and return type
# required of this function.
#
#    Apr 22, 2014    2925     Chris.Golden        Initial creation.
#    May 15, 2014    2925     Chris.Golden        Fixed bug causing script to do
#                                                 nothing.
#    Jun 17, 2014    3982     Chris.Golden        Changed megawidget 'side effects'
#                                                 to 'interdependencies', and
#                                                 changed to use simpler way of
#                                                 getting and setting values for
#                                                 single-state megawidgets' mutable
#                                                 properties.
#
def applyInterdependencies(triggerIdentifiers, mutableProperties):
   if triggerIdentifiers == None or "fallBelowUntilFurtherNotice" in triggerIdentifiers:
      enable = True
      if "fallBelowUntilFurtherNotice" in mutableProperties:
         if "values" in mutableProperties["fallBelowUntilFurtherNotice"]:
            enable = not mutableProperties["fallBelowUntilFurtherNotice"]["values"]
      return { "riseAbove:crest:fallBelow": { "valueEditables": { "fallBelow": enable } } }
   else:
       return None
