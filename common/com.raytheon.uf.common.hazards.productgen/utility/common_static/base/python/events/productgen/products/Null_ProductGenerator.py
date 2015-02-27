'''
    Description: Null product generator called at Hazard Services init
    time so that initializing the product generation framework
    can be done up front.
'''
import collections
import Legacy_Base_Generator

class Product(Legacy_Base_Generator.Product):
    
    def __init__(self):
        super(Product, self).__init__()       

        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'Null_ProductGenerator'
        
                
    def getScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD"
        metadata['description'] = "Null Product generator."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self, eventSet):
        return {}

    def _initialize(self) :
        pass
    
    def executeFrom(self, dataList, prevDataList=None):
        return dataList

            
    def execute(self, eventSet, dialogInputMap):          
        return [], []
    
