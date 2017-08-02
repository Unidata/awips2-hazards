"""
This class holds the metadata for burn scars, such as impacts and CTAs

    Expects dictionary structure like:

    '[BURN NAME]' : {
        'burnScarName': 'BURN_NAME',
        'burnScarEnd': 'OVER_NAME',
        'namelowimpact': {
            'ctaSelected': 'USE_CTA',
            'burnScar': 'OVERVIEW',
            'burnDrainage': 'DRAINAGE',
            'burnCTA': 'CTA',
                      },
        'namehighimpact': {
            'ctaSelected': 'USE_CTA',
            'burnScar': 'OVERVIEW',
            'burnDrainage': 'DRAINAGE',
            'burnCTA': 'CTA',
                      },
    }

    where
    Burn Scar Name comes from 'maps' database table 'mapdata.burnscararea'
    'burnScarName', 'burnScarEnd', 'namelowimpact', and 'namehighimpact' come
    from WarnGen templates.

    E.g.
    'FourMileBurnAreaOnly': {
        'burnScarName': 'Fourmile burn area',
        'burnScarEnd': 'over the Fourmile burn area',
        'fourmilelowimpact': {
            'ctaSelected': 'YES',
            'burnScar': 'This is a life threatening situation. Heavy rainfall will cause extensive and severe flash flooding of creeks, streams, and ditches in the Fourmile burn area.',
            'burnDrainage': 'Some drainage basins impacted include Fourmile Creek, Gold Run, and Fourmile Canyon Creek.',
            'burnCTA': 'This is a life threatening situation. Heavy rainfall will cause extensive and severe flash flooding of creeks, streams, and ditches in the Fourmile burn area. Some drainage basins impacted include Fourmile Creek, Gold Run, and Fourmile Canyon Creek. Severe debris flows can also be anticipated across roads. Roads and driveways may be washed away in places. If you encounter flood waters, climb to safety.',
                      },
        'fourmilehighimpact': {
            'ctaSelected': 'YES',
            'burnScar': 'This is a life threatening situation for people along Boulder Creek in the City of Boulder, in the FourMile burn area, and in Boulder Canyon. Heavy rainfall will cause extensive and severe flash flooding of creeks and streams from the FourMile burn area downstream through the City of Boulder.',
            'burnDrainage': 'Some drainage basins impacted include Boulder Creek, Fourmile Creek, Gold Run, Fourmile Canyon Creek, and Wonderland Creek.',
            'burnCTA': 'This is a life threatening situation. Heavy rainfall will cause extensive and severe flash flooding of creeks, streams, and ditches in the Fourmile burn area. Some drainage basins impacted include Fourmile Creek, Gold Run, and Fourmile Canyon Creek. Severe debris flows can also be anticipated across roads. Roads and driveways may be washed away in places. If you encounter flood waters, climb to safety.',
                      },
        },

Only one of the impact groups may be present, or there may be simply a 'name'
group with ctaSelected, burnScar, etc.
"""
BurnScarMetaData = {
}
