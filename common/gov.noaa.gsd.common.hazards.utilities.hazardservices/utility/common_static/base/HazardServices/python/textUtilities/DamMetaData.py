"""
This class holds the metadata for DamBreaks, such as scenarios and rules of thumb

    Expects dictionary structure like: where
    DAM NAME comes from 'maps' database table 'mapdata.daminundation'
    'riverName', 'cityInfo', 'scenarios', and 'ruleofthumb' come from
    warngen templates, or your local agency that can supply such information

    '[DAM NAME]' : {
        'riverName': 'TBD_RIVER',
        'cityInfo': 'TBD_CITY',
        'scenarios': {
            'highfast': 'TBD_SCEN_HF',
            'highnormal': 'TBD_SCEN_HN',
            'mediumfast': 'TBD_SCEN_MF',
            'mediumnormal': 'TBD_SCEN_MN',
                      },
            'ruleofthumb': '''TBD_ROT''',
    }

    Eg.
    'Papio Dam Site 13 (Lawerence Youngman Lake)': {
        'riverName': 'Phil River',
        'cityInfo': 'Evan...located about 3 miles',
        'scenarios': {
            'highfast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 18 feet in 16 minutes.',
            'highnormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 23 feet in 31 minutes.',
            'mediumfast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 14 feet in 19 minutes.',
            'mediumnormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 17 feet in 32 minutes.',
                      },
            'ruleofthumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.
                            ''',
        },
"""
DamMetaData = {
}
