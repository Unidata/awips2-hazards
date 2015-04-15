def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None:
      
      return None

  if "editability" in triggerIdentifiers:

    return {
      "container1": {
        "editable": mutableProperties["editability"]["values"]
      }
    }

  elif "enabled" in triggerIdentifiers:

    return {
      "container1": {
        "enable": mutableProperties["enabled"]["values"]
      }
    }
    
  elif "expand1and3" in triggerIdentifiers:
      
    return {
      "container1": {
        "expandedPages": [ "First Bar", "Third Bar" ]
      }
    }

  else:
    return None
