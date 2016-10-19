def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    if triggerIdentifiers is not None and "B" in triggerIdentifiers:
        allow = (mutableProperties["B"]["values"] == False)
        if allow:
            cValue = mutableProperties["C"]["values"]
            dValue = mutableProperties["D"]["values"]
        else:
            cValue = False
            dValue = False
        return {
                "C": {
                      "enable": allow,
                      "values": cValue
                      },
                "D": {
                      "enable": allow,
                      "values": dValue
                      },
                }
    elif triggerIdentifiers is not None and ("C" in triggerIdentifiers or "D" in triggerIdentifiers):
        allow = (mutableProperties["C"]["values"] == False) and (mutableProperties["D"]["values"] == False) 
        if allow:
            value = mutableProperties["B"]["values"]
        else:
            value = False
        return {
                "B": {
                      "enable": allow,
                      "values": value
                      }
                }

    else:
        return None
