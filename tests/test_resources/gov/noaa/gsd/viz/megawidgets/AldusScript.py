def applyInterdependencies(triggerIdentifiers, mutableProperties):

  if triggerIdentifiers == None or "overviewType" in triggerIdentifiers:

    type = mutableProperties["overviewType"]["values"]
    if type == "Enter synopsis below":
      return {
        "overviewText": {
          "editable": True,
          "values": ""
        }
      }

    if type == "Canned Synopsis 1":
      text = "This is the first canned synopsis."
    elif type == "Canned Synopsis 2":
      text = "This is the second canned synopsis."
    else:
      text = "This is the third canned synopsis."
        
    return {
      "overviewText": {
        "editable": False,
        "values": text
      }
    }

  else:
    return None
