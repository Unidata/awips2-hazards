def applyInterdependencies(triggerIdentifiers, mutableProperties):
            
    if triggerIdentifiers != None and "enabled" in triggerIdentifiers:
        
        # Determine whether the longer duration choices should be shown or not.
        enabled = True
        if "enabled" in mutableProperties \
                and "values" in mutableProperties["enabled"]:
            enabled = mutableProperties["enabled"]["values"]
            
        return { "time1": { "enable": enabled }, "time2": { "enable": enabled } }
            
    if triggerIdentifiers != None and "editable" in triggerIdentifiers:
        
        # Determine whether the longer duration choices should be shown or not.
        editable = True
        if "editable" in mutableProperties \
                and "values" in mutableProperties["editable"]:
            editable = mutableProperties["editable"]["values"]
            
        return { "time1": { "editable": editable }, "time2": { "editable": editable } }

    return None
