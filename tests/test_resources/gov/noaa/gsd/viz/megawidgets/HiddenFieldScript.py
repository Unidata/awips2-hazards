def applyInterdependencies(triggerIdentifiers, mutableProperties):

  # If initializing or if the combination field has changed, then change the combo
  # boxes to show the right values for the combination field's values, unless the
  # combination field's value is illegal (i.e. it cannot be broken into two parts
  # and used to set the combo boxes), in which case fall through to the next
  # if statement.
  if triggerIdentifiers == None or "result" in triggerIdentifiers:
      if isinstance(mutableProperties["result"]["values"], list) and \
         len(mutableProperties["result"]["values"]) == 2 and \
         isinstance(mutableProperties["result"]["values"][0], basestring) and \
         isinstance(mutableProperties["result"]["values"][1], basestring) and \
         mutableProperties["result"]["values"][0] in mutableProperties["source"]["choices"] and \
         mutableProperties["result"]["values"][1] in mutableProperties["type"]["choices"]:
          return {
                  "source": {
                             "values": mutableProperties["result"]["values"][0]
                  },
                  "type": {
                             "values": mutableProperties["result"]["values"][1]
                  }
          }
          
  # If initializing and the combination field's value was illegal, or if the
  # source or type combo box fields changed, set the combination field's value
  # to be a list of values from the source and type fields respectively. 
  if triggerIdentifiers == None or "type" in triggerIdentifiers or "source" in triggerIdentifiers:
      return {
              "result": {
                         "values": [ mutableProperties["source"]["values"], mutableProperties["type"]["values"] ]
              }
      }
      

  return None
