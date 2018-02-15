"""
This class holds the metadata for DamBreaks, such as scenarios and rules of thumb.
Base version only has current default non-dam specific metadata.  Can create a
configured level version based on warnGen configuration by running the script
parseWarngenTemplate.py

Here is an idealized example of some dam specific metadata:

    "Branched Oak Dam": {
        "riverName": "Kells River", 
        "damName": "Branched Oak Dam", 
        "cityInfo": "Dangelo...located about 6 miles", 
        "scenarios": {
            "highfast": {
                "displayString": "high fast", 
                "productString": "If a complete failure of the dam occurs...the water depth at Dangelo could exceed 19 feet in 32 minutes."
            }, 
            "highnormal": {
                "displayString": "high normal", 
                "productString": "If a complete failure of the dam occurs...the water depth at Dangelo could exceed 26 feet in 56 minutes."
            }, 
            "mediumfast": {
                "displayString": "medium fast", 
                "productString": "If a complete failure of the dam occurs...the water depth at Dangelo could exceed 14 feet in 33 minutes."
            }, 
            "mediumnormal": {
                "displayString": "medium normal", 
                "productString": "If a complete failure of the dam occurs...the water depth at Dangelo could exceed 20 feet in 60 minutes."
            }
        }, 
        "ruleofthumb": "Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind dam and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.", 
        "dropDownLabel": "Branched Oak Dam (Buchanan County)", 
        "featureID": "BranchedOakDam"
    }, 

Mostly the metadata text values are meant to be inserted into generated text
products. An exception is the "dropDownLabel"; if present that provides the
description of the dam that goes into the choice list for the dam break
recommender.
"""
# Because for DamMetaData there is a distinction between dam specific and 
# non-dam specific metadata, it makes sense to post a default set of 
# non-dam specific metadata.
DamMetaData = {
      "Dam": {
        "sitespecSelected": "YES", 
        "hycType": "the ${riverName} below ${damName}", 
        "emergencyText": "towns and cities immediately below ${damName} on the ${riverName}", 
        "headline": "for ${reportType2} ${damName} on the ${riverName}", 
        "reportType1": "${reportType2} ${damName} on the ${riverName}", 
        "addInfo": "The nearest downstream town is ${cityInfo} from the dam.", 
        "sitespecCTA": "If you are in low lying areas below the ${damName} you should move to higher ground immediately."
     }
}
