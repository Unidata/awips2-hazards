'''
    Description: Base Class for Legacy Product Generators holding common logic and process.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation
    Nov 10, 2014   4933     Robert.Blum    Added abstract method for returning MetaData.
    Jul 06, 2016  18257     Kevin.Bisanz   Added eventSet parameter to executeFrom(..)
    
    @author Tracy.L.Hansen@noaa.gov
'''
import abc

class Product(object):
    
    metadata = None
    
    def __init__(self):
        return
    
    @abc.abstractmethod
    def defineScriptMetadata(self):
        '''
        @return: Returns a python dictionary which defines basic information
        about the generator, such as author, script version, and description
        '''
        return
    
    def defineDialog(self):
        '''      
        @summary: Defines a dialog that will be presented to the user prior to 
        the product generator's execute routine.  Will use python maps to define widgets.  
        Each key within the map will defined a specific attribute for the widget.
        @return: Python map which correspond to attributes for widgets.
        '''
        return
        
    @abc.abstractmethod
    def execute(self, eventSet, dialogInputMap):
        '''
        @param eventSet: a list of hazard events (hazardEvents) plus
                               a map of additional variables
        @param dialogInputMap: A map containing user selections from the dialog created
              by the defineDialog() routine
        @return productDicts, hazardEvents: 
             Each execution of a generator can produce 1 or more 
             products from the set of hazard events
             For each product, a productID and one dictionary is returned as input for 
             the desired formatters.
             Also, returned is a set of hazard events, updated with product information.
        '''
        return
    
    @abc.abstractmethod
    def executeFrom(self, dataList, eventSet, keyInfo=None):
        
        return

    @abc.abstractmethod
    def getMetadata(self):
        return None 