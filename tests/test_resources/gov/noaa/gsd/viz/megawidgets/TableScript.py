def applyInterdependencies(triggerIdentifiers, mutableProperties):
  if triggerIdentifiers != None and "incrementCounts" in triggerIdentifiers:
      newList = []
      for sublist in mutableProperties["table1"]["values"]:
          newList.append([sublist[0], sublist[1], str(int(sublist[2]) + 1)])
      return {
              "table1": {
                         "values": newList
                         }
              }

  if triggerIdentifiers != None and "enabled" in triggerIdentifiers:
    return {
      "table1": {
        "enable": mutableProperties["enabled"]["values"]
      }
    }


  return None
