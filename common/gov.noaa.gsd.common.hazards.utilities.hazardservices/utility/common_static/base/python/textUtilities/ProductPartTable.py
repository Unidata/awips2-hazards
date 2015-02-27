'''
    Description: Designates which parts for each product are editable
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    01/15/2015   5109       bphillip    Initial creation
    02/26/2015   6599     Robert.Blum   Changed to new style class
'''

class ProductPartTable(object):
    
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
                'callsToAction',
                'locationsAffected'],
                            
                            'FFW': [
                'attribution',
                'firstBullet',
                'timeBullet',
                'basisBullet',
                'impactsBullet',
                'emergencyStatement',
                'callsToAction',
                'locationsAffected']
                }
    
    DefaultEditableProductParts = [
                'attribution',
                'firstBullet',
                'timeBullet',
                'basisBullet',
                'impactsBullet',
                'emergencyStatement',
                'callsToAction',
                'locationsAffected']

    
    def __init__(self):
        pass
