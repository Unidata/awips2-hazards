def applyInterdependencies(triggerIdentifiers, mutableProperties):
    if triggerIdentifiers != None and "advance" in triggerIdentifiers:
        time = mutableProperties["currentTime"]["values"] + 60000
        startTime2 = mutableProperties["start2:end2"]["values"]["start2"]
        endTime2 = mutableProperties["start2:end2"]["values"]["end2"]
        if startTime2 < time:
            startTime2 += 60000
            endTime2 += 60000
        return {
                "start:end": {
                              "minimumTime": {
                                              "start": time
                                              }
                              },
                "start2:end2": {
                                "minimumTime": {
                                                "start2": time
                                                },
                                "values": {
                                           "start2": startTime2,
                                           "end2": endTime2
                                           }
                              },
                "currentTime": {
                                "values": time
                                }
                }
