def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None:
      
      return None

  if "editability" in triggerIdentifiers:

    return {
      "graph1": {
        "editable": mutableProperties["editability"]["values"]
      }
    }

  elif "draw" in triggerIdentifiers:
    return {
      "graph1": {
        "values": []
      }
    }

  else:
    return None
