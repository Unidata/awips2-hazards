def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    # Do nothing unless the "until further notice" checkbox has changed state.
    if triggerIdentifiers != None and "fallBelowUntilFurtherNotice" in triggerIdentifiers:
        
        # Determine whether the "fall below" state should be editable or read-only.
        # If "until further notice" is turned on, it should be read-only.
        editable = True
        if "fallBelowUntilFurtherNotice" in mutableProperties \
                and "values" in mutableProperties["fallBelowUntilFurtherNotice"]:
            editable = not mutableProperties["fallBelowUntilFurtherNotice"]["values"]

        # If "until further notice" is on, remember the interval as it is now
        # between the "crest" and "fall below" times in case "until further
        # notice" is toggled off in the future, and set the "fall below" time
        # to the special "until further notice" value. Otherwise, if a saved
        # interval is found, set the "fall below" time to be that distance in
        # time from the "crest" time; if no saved interval is found, just set
        # it to be an hour from the "crest" time.
        if editable == False:
            
            # For the actual script, this value is defined as a constant in
            # another Python module that is imported, but for simplicity's
            # sake and to avoid having to set up the include path in Jep for
            # the test, it is simply defined here since this test is stand-
            # alone anyway.
            UFN_TIME_VALUE_SECS = float(2**31-1)
            
            interval = mutableProperties["riseAbove:crest:fallBelow"]["values"]["fallBelow"] - \
                    mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"]
            
            fallBelow = UFN_TIME_VALUE_SECS * 1000
            
            return { "riseAbove:crest:fallBelow": {
                                                   "valueEditables": { "fallBelow": False },
                                                   "extraData": { "lastInterval": interval },
                                                   "values": { "fallBelow": fallBelow }
                                                   }
                    }
        else:
            
            if "extraData" in mutableProperties["riseAbove:crest:fallBelow"] \
                    and "lastInterval" in mutableProperties["riseAbove:crest:fallBelow"]["extraData"]:
                interval = mutableProperties["riseAbove:crest:fallBelow"]["extraData"]["lastInterval"]
            else:
                interval = 3600 * 1000

            fallBelow = mutableProperties["riseAbove:crest:fallBelow"]["values"]["crest"] + interval
            
            return { "riseAbove:crest:fallBelow": {
                                                   "valueEditables": { "fallBelow": True },
                                                   "values": { "fallBelow": fallBelow }
                                                   }
                    }
            
    else:
        return None
