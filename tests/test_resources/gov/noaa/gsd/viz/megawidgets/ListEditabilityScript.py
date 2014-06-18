def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None or "editability" in triggerIdentifiers:

    return {
      "list1": {
        "editable": mutableProperties["editability"]["values"]
      },
      "list2": {
        "editable": mutableProperties["editability"]["values"]
      }
    }

  else:
    return None
