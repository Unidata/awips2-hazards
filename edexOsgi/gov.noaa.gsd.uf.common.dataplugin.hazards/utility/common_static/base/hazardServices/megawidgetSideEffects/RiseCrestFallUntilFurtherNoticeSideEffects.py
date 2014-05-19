# Side effects script for hazard event metadata megawidgets that include the
# Rise Above/Crest/Fall Below time scale megawidget and want to allow the use of
# the Until Further Notice option for the Fall Below time. Ensures that if the
# associated Until Further Notice checkbox is checked, the Fall Below thumb in
# the time scale widget is disabled. See the Java class PythonSideEffectsApplier
# for info as to the arguments and return type required of this function.
#
#    Apr 22, 2014    2925     Chris.Golden        Initial creation.
#    May 15, 2014    2925     Chris.Golden        Fixed bug causing script to do
#                                                 nothing.
#
def applySideEffects(triggerIdentifiers, mutableProperties):
   if triggerIdentifiers == None or "fallBelowUntilFurtherNotice" in triggerIdentifiers:
      enable = True
      if "fallBelowUntilFurtherNotice" in mutableProperties:
         if "values" in mutableProperties["fallBelowUntilFurtherNotice"]:
            if "fallBelowUntilFurtherNotice" in mutableProperties["fallBelowUntilFurtherNotice"]["values"]:
               enable = not mutableProperties["fallBelowUntilFurtherNotice"]["values"]["fallBelowUntilFurtherNotice"]
      return { "riseAbove:crest:fallBelow": { "valueEditables": { "fallBelow": enable } } }
   else:
       return None
