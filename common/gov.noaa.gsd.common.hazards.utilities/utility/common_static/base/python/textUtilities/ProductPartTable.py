'''
    Description: Designates which parts for each product are editable
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    01/15/2015   5109       bphillip    Initial creation
'''

class ProductPartTable:
    
    # FIXME: This dictionary needs to be updated with actual values
    # The ones present now are simply place holders and best guesses
    EditableProductParts = {
                            'FFA': [
                'attribution',
                'firstBullet',
                'timeBullet',
                'basisBullet',
                'impactsBullet',
                'cityList',
                'emergencyStatement',
                'locationsAffected'],
                            
                            'FFW': [
                'attribution',
                'firstBullet',
                'timeBullet',
                'basisBullet',
                'impactsBullet',
                'cityList',
                'emergencyStatement',
                'locationsAffected']
                }
    
    DefaultEditableProductParts = [
                'attribution',
                'firstBullet',
                'timeBullet',
                'basisBullet',
                'impactsBullet',
                'cityList',
                'emergencyStatement',
                'locationsAffected']

    
    def __init__(self):
        pass
