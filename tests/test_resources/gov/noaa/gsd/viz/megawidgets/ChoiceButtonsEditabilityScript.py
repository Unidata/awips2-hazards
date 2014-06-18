def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None or "editability" in triggerIdentifiers:

    return {
      "button1": {
        "editable": mutableProperties["editability"]["values"]
      },
      "button2": {
        "editable": mutableProperties["editability"]["values"]
      },
      "button3": {
        "editable": mutableProperties["editability"]["values"]
      }
    }

  else:
    return None
