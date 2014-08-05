def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None:
      
      return None

  if "editability" in triggerIdentifiers:

    return {
      "combo1": {
        "editable": mutableProperties["editability"]["values"]
      },
      "combo2": {
        "editable": mutableProperties["editability"]["values"]
      }
    }

  elif "enabled" in triggerIdentifiers:

    return {
      "combo1": {
        "enable": mutableProperties["enabled"]["values"]
      },
      "combo2": {
        "enable": mutableProperties["enabled"]["values"]
      }
    }

  else:
    return None
