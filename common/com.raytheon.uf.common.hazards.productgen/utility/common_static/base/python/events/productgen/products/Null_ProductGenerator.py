'''
    Description: Null product generator called at Hazard Services init
    time so that initializing the product generation framework
    can be done up front.
'''
import collections
import Legacy_ProductGenerator

class Product(Legacy_ProductGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()       
                
    def getScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD"
        metadata['description'] = "Null Product generator."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self, eventSet):
        return {}

    def _initialize(self):
        pass
                
    def execute(self, eventSet, dialogInputMap):          
        return [], []
    
