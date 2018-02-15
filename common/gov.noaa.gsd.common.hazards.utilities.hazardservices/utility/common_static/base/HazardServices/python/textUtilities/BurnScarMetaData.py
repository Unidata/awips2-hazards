"""
This class holds the metadata for burn scars, such as impacts and CTAs

    Expects dictionary structure like:

    '[BURN NAME]' : {
        'burnScarName': 'BURN_NAME',
        'burnScarEnd': 'OVER_NAME',
        'namelowimpact': {
            'ctaSelected': 'USE_CTA',
            'productString': 'OVERVIEW',
            'burnDrainage': 'DRAINAGE',
            'burnCTA': 'CTA',
                      },
        'namehighimpact': {
            'ctaSelected': 'USE_CTA',
            'productString': 'OVERVIEW',
            'burnDrainage': 'DRAINAGE',
            'burnCTA': 'CTA',
                      },
    }

    where
    Burn Scar Name comes from 'maps' database table 'mapdata.burnscararea'
    'burnScarName', 'burnScarEnd', 'namelowimpact', and 'namehighimpact' come
    from WarnGen templates.

    E.g.
    "Four Mile Burn Area": {
        "burnScarName": "Four Mile Burn Area", 
        "burnScarEnd": "over the Four Mile Burn Area", 
        "emergencyHeadline": "areas in and around the Four Mile Burn Area", 
        "dropDownLabel": "FourMile Burn Area", 
        "scenarios": {
            "fourmilehighimpact": {
                "displayString": "high impact", 
                "burnCTA": "This is a life-threatening situation for people along Boulder Creek in the Ci
ty of Boulder...in the Four Mile Burn Area...and in Boulder Canyon.  Heavy rainfall will cause extensive 
and severe flash flooding of creeks and streams from the Four Mile Burn Area downstream through the City 
of Boulder.  Some drainage basins impacted include Boulder Creek...Four Mile Creek...Gold Run...Four Mile
 Canyon Creek...and Wonderland Creek.  Severe debris flows can also be anticipated across roads.  Roadway
s and bridges may be washed away in places. If you encounter flood waters...climb to safety.", 
                "productString": "This is a life-threatening situation for people along Boulder Creek in 
the City of Boulder...in the Four Mile Burn Area...and in Boulder Canyon.  Heavy rainfall will cause exte
nsive and severe flash flooding of creeks and streams from the Four Mile Burn Area downstream through the
 City of Boulder.", 
                "burnDrainage": "Some drainage basins impacted include Boulder Creek...Four Mile Creek...
Gold Run...Four Mile Canyon Creek...and Wonderland Creek."
            }, 
            "fourmilelowimpact": {
                "displayString": "low impact", 
                "burnCTA": "This is a life-threatening situation.  Heavy rainfall will cause extensive an
d severe flash flooding of creeks...streams...and ditches in the Four Mile burn area.  Some drainage basi
ns impacted include Four Mile Creek...Gold Run...and Four Mile Canyon Creek.  Severe debris flows can als
o be anticipated across roads.  Roads and driveways may be washed away in places.  If you encounter flood
 waters...climb to safety.", 
                "productString": "This is a life threatening situation.  Heavy rainfall will cause extens
ive and severe flash flooding of creeks...streams...and ditches in the Four Mile burn area.", 
                "burnDrainage": "Some drainage basins impacted include Four Mile Creek...Gold Run...and F
our Mile Canyon Creek."
            }
        }, 
        "featureID": "FourMileBurnArea"
    }

Mostly the metadata text values are meant to be inserted into generated text
products. An exception is the "dropDownLabel"; if present that provides the
description of the burn scar area that goes into the choice list for the burn
scar recommender.
"""
BurnScarMetaData = {
}
