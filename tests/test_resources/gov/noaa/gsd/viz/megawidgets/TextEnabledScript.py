def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None or "enabled" in triggerIdentifiers or "editable" in triggerIdentifiers:

    return {
      "text1": {
        "enable": mutableProperties["enabled"]["values"],
        "editable": mutableProperties["editable"]["values"]
      },
      "text2": {
        "enable": mutableProperties["enabled"]["values"],
        "editable": mutableProperties["editable"]["values"]
      }
    }

  else:
    return None
