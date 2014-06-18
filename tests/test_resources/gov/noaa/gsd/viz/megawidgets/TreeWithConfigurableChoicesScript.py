def applyInterdependencies(triggerIdentifiers, mutableProperties):

    if triggerIdentifiers == None:
        triggerIdentifiers = ["showConvective"]
        mutableProperties["showConvective"]["values"] = True
    if "showConvective" in triggerIdentifiers:
        choices =  mutableProperties["tree1"]["choices"]
        
        if mutableProperties["showConvective"]["values"] == True and choices[0]["displayString"] != "Convective":
            choices = [{
                "displayString": "Convective",
                "children": [ "EW.W", "SV.W", "TO.W", "SV.A", "TO.A" ]
            }] + choices
            
        elif mutableProperties["showConvective"]["values"] == False and choices[0]["displayString"] == "Convective":
            choices.pop(0)
                        
        return {
            "tree1": {
                "choices": choices
            }
        }
    else:
        return None
