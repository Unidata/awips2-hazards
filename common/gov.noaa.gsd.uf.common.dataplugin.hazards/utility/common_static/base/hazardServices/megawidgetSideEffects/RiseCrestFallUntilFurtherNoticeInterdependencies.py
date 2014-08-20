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
#    Jun 24, 2014    4009     Chris.Golden        Added code to change the actual
#                                                 fall below time to the appropriate
#                                                 value (this used to be hard-coded
#                                                 in the session event manager).
#
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    parm = "impacts"
    ### FIXME: very specific hard coding here.  Need to make more flexible.
    triggerFieldName = parm + "SelectedForecastPointsComboBox"
    mutableFieldName = parm + "StringForStageFlowTextArea"

    
    # Do nothing unless the "until further notice" checkbox has changed state, or
    # initialization is occurring.
    if triggerIdentifiers == None or "fallBelowUntilFurtherNotice" in triggerIdentifiers:
        
        # Determine whether the "fall below" state should be editable or read-only.
        # If "until further notice" is turned on, it should be read-only.
        editable = True
        if "fallBelowUntilFurtherNotice" in mutableProperties \
                and "values" in mutableProperties["fallBelowUntilFurtherNotice"]:
            editable = not mutableProperties["fallBelowUntilFurtherNotice"]["values"]

        # If "until further notice" has just been turned on, remember the interval
        # as it is now between the "crest" and "fall below" times in case "until
        # further notice" is toggled off in the future, and set the "fall below"
        # time to the special "until further notice" value. Otherwise, if "until
        # further notice" has just been turned off, set the "fall below" time to
        # be an interval offset from the "crest" time. If a saved interval is
        # found, use that as the interval; otherwise, make the interval equal to
        # the one between the "riseAbove" and "crest" times. Finally, if it has
        # not just been turned on or off, ensure that the "fallBelow" state is
        # still read-only or editable as appropriate. This last case will occur,
        # for example, when the script is called as part of the megawidgets'
        # initialization.
        from VTECConstants import UFN_TIME_VALUE_SECS
        ufnTime = UFN_TIME_VALUE_SECS * 1000L
        if "riseAbove:crest:fallBelow" in mutableProperties:
            if editable == False and \
                    mutableProperties["riseAbove:crest:fallBelow"]["values"]["fallBelow"] != ufnTime:
                
                interval = long(mutableProperties["riseAbove:crest:fallBelow"]["values"]["fallBelow"] - \
                        mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"])
                fallBelow = ufnTime
                
                return { "riseAbove:crest:fallBelow": {
                                                       "valueEditables": { "fallBelow": False },
                                                       "extraData": { "lastInterval": interval },
                                                       "values": { "fallBelow": fallBelow }
                                                       }
                        }
            elif editable == True and \
                    mutableProperties["riseAbove:crest:fallBelow"]["values"]["fallBelow"] == ufnTime:
                
                if "extraData" in mutableProperties["riseAbove:crest:fallBelow"] \
                        and "lastInterval" in mutableProperties["riseAbove:crest:fallBelow"]["extraData"]:
                    interval = mutableProperties["riseAbove:crest:fallBelow"]["extraData"]["lastInterval"]
                else:
                    interval = long(mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"] - \
                            mutableProperties["riseAbove:crest:fallBelow"]["values"]["riseAbove"])
                fallBelow = mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"] + interval
                
                return { "riseAbove:crest:fallBelow": {
                                                       "valueEditables": { "fallBelow": True },
                                                       "values": { "fallBelow": fallBelow }
                                                       }
                        }
            else:
                return { "riseAbove:crest:fallBelow": { "valueEditables": { "fallBelow": editable } } }
        else:
            return None
    
    ### For Impacts and Crests interaction
    if triggerIdentifiers == None or triggerFieldName in triggerIdentifiers:
             
            if triggerFieldName in mutableProperties and "values" in mutableProperties[triggerFieldName]:
                line = mutableProperties[triggerFieldName]["values"]
                vals = filter(None,line.split('::'))
                
                return {
                        "impactsStringForStageFlowTextArea": { "values" : vals[0] }
                        }
                
            else:
                return None
        
    else:
        return None
