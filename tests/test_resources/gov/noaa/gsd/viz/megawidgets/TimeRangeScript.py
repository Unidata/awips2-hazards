def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    # Do nothing unless the "until further notice" checkbox has changed state.
    if triggerIdentifiers != None and "endTimeUntilFurtherNotice" in triggerIdentifiers:
        
        # Determine whether the "end time" state should be editable or read-only.
        # If "until further notice" is turned on, it should be read-only.
        editable = True
        if "endTimeUntilFurtherNotice" in mutableProperties \
                and "values" in mutableProperties["endTimeUntilFurtherNotice"]:
            editable = not mutableProperties["endTimeUntilFurtherNotice"]["values"]

        # If "until further notice" is on, remember the interval as it is now
        # between the "start time" and "end time" in case "until further notice"
        # is toggled off in the future, and set the "end time" to the special
        # "until further notice" value. Otherwise, if a saved interval is found,
        # set the "end time" to be that distance in time from the "start time";
        # if no saved interval is found, just set it to be a day from the "start
        # time".
        if editable == False:
            
            # Normally this is defined in an imported module, but for the sake
            # of this demo the value is hard-coded here.
            UFN_TIME_VALUE_SECS = float(2**31-1)
            
            interval = mutableProperties["start:end"]["values"]["end"] - \
                    mutableProperties["start:end"]["values"]["start"]
            
            end = UFN_TIME_VALUE_SECS * 1000
            
            return { "start:end": {
                                   "valueEditables": { "end": False },
                                   "values": { "end": end }
                                   },
                     "hiddenEndTimeLastInterval": {
                                                   "values": interval
                                                   }
                    }
        else:
            
            if "hiddenEndTimeLastInterval" in mutableProperties \
                    and "values" in mutableProperties["hiddenEndTimeLastInterval"] \
                    and mutableProperties["hiddenEndTimeLastInterval"]["values"] > 0:
                interval = mutableProperties["hiddenEndTimeLastInterval"]["values"]
            else:
                interval = 24 * 3600 * 1000

            end = mutableProperties["start:end"]["values"]["start"] + interval
            
            return { "start:end": {
                                   "valueEditables": { "end": True },
                                   "values": { "end": end }
                                   },
                     "hiddenEndTimeLastInterval": {
                                                   "values": 0
                                                   }
                    }
            
    if triggerIdentifiers != None and "useLongerDurations" in triggerIdentifiers:
        
        # Determine whether the longer duration choices should be shown or not.
        useLongerDurations = False
        if "useLongerDurations" in mutableProperties \
                and "values" in mutableProperties["useLongerDurations"]:
            useLongerDurations = mutableProperties["useLongerDurations"]["values"]
            
        if useLongerDurations:
            return { "start:end": {
                                   "durationChoices": [ "2 days", "3 days", "5 days", "7 days", "10 days", "12 days" ],
                                   "values": { "end": mutableProperties["start:end"]["values"]["start"] + 432000000 }
                                   }
                    }
    
        else:
            return { "start:end": {
                                   "durationChoices": [ "12 hours", "1 day", "2 days", "3 days", "5 days" ],
                                   "values": { "end": mutableProperties["start:end"]["values"]["start"] + 43200000 }
                                   }
                    }
            
    if triggerIdentifiers != None and "enabled" in triggerIdentifiers:
        
        # Determine whether the longer duration choices should be shown or not.
        enabled = True
        if "enabled" in mutableProperties \
                and "values" in mutableProperties["enabled"]:
            enabled = mutableProperties["enabled"]["values"]
            
        return { "start:end": { "enable": enabled } }
            
    if triggerIdentifiers != None and "editable" in triggerIdentifiers:
        
        # Determine whether the longer duration choices should be shown or not.
        editable = True
        if "editable" in mutableProperties \
                and "values" in mutableProperties["editable"]:
            editable = mutableProperties["editable"]["values"]
            
        return { "start:end": { "editable": editable } }

    return None
