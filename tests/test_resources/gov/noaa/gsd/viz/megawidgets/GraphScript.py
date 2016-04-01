def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None:
      
      return None

  if "editability" in triggerIdentifiers:

    return {
      "graph1": {
        "editable": mutableProperties["editability"]["values"]
      }
    }

  else:
    return None
