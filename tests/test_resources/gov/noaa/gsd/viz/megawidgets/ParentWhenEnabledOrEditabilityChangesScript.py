def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None:
      
      return None

  if "editability" in triggerIdentifiers:

    return {
      "container1": {
        "editable": mutableProperties["editability"]["values"]
      },
      "parent1:parent2:parent3": {
        "editable": mutableProperties["editability"]["values"]
      }
    }

  elif "enabled" in triggerIdentifiers:

    return {
      "container1": {
        "enable": mutableProperties["enabled"]["values"]
      },
      "parent1:parent2:parent3": {
        "enable": mutableProperties["enabled"]["values"]
      }
    }

  else:
    return None
