def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None or "enabled" in triggerIdentifiers:

    return {
      "text1": {
        "enable": mutableProperties["enabled"]["values"]
      },
      "text2": {
        "enable": mutableProperties["enabled"]["values"]
      }
    }

  else:
    return None
