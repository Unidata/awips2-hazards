"""
This class holds the metadata for DamBreaks, such as scenarios and rules of thumb

 @since: February 2015
 @author: GSD Hazard Services Team
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 Feb24, 2015            kmanross     Initial development

"""


"""
    Expects dictionary structure like: where
    DAM NAME comes from 'maps' database table 'mapdata.daminundation'
    'riverName', 'cityInfo', 'scenarios', and 'ruleOfThumb' come from
    warngen templates, or your local agency that can supply such information 
    
    '[DAM NAME]' : {
        'riverName': 'TBD_RIVER',
        'cityInfo': 'TBD_CITY',
        'scenarios': {
            'highFast': 'TBD_SCEN_HF',
            'highNormal': 'TBD_SCEN_HN',
            'mediumFast': 'TBD_SCEN_MF',
            'mediumNormal': 'TBD_SCEN_MN',
                      },
            'ruleOfThumb': '''TBD_ROT''',
    }
        
    Eg.                 
    'Papio Dam Site 13 (Lawerence Youngman Lake)': {
        'riverName': 'Phil River',
        'cityInfo': 'Evan...located about 3 miles',
        'scenarios': {
            'highFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 18 feet in 16 minutes.',
            'highNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 23 feet in 31 minutes.',
            'mediumFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 14 feet in 19 minutes.',
            'mediumNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 17 feet in 32 minutes.',
                      },
            'ruleOfThumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.
                            ''',
        },
"""
damInundationMetadata = {    
}