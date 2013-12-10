"""
    Description: Product Generator for the ESF product.
    NOTE: This addresses the Hydrologic Outlook describing the 
    possibility of flooding on a near-term forecast horizon, 
    typically more than 24 hours from the event.
    
    It does not address the Water Supply Outlook or Probabilistic
    Hydrologic Outlook products which provide long-term forecast information 
    such as water supply forecasts and probabilistic analysis.  
    However, it could be used as a starting point for Focal Points in 
    developing these other types of ESF products.

    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer            Description
    ------------ ---------- ----------- --------------------------
    July 3, 2013  1290      Tracy.L.Hansen      Initial creation
    Nov      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents, simplifying product
                                                 dictionary
    
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
"""
import os, types, copy, sys, json
import ProductTemplate
from ProductPart import ProductPart


class Product(ProductTemplate.Product):
    
    def __init__(self):
        super(Product, self).__init__()       
        self._productCategory = "ESF"
        self._areaName = "" 
        # Does the product use zones or counties for areal hazards
        self._areaUgcType = "counties"
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # NOTE: In PV2, this will gathered as part of the Hazard Information Dialog
        self._purgeHours = 8
        self._ESF_ProductName = "Hydrologic Outlook"
        self._includeAreaNames = False
        self._includeCityNames = False
        self._vtecProduct = False
                
    def getScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD"
        metadata['description'] = "Product generator for ESF."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        return {}
                
    def execute(self, eventSet):          
        '''
        Inputs:
        @param eventSet: a list of hazard events (hazardEvents) plus
                               a map of additional variables
        @return productDicts, hazardEvents: 
             Each execution of a generator can produce 1 or more 
             products from the set of hazard events
             For each product, a productID and one dictionary is returned as input for 
             the desired formatters.
             Also, returned is a set of hazard events, updated with product information.

        '''
        self.logger.info("Start ProductGeneratorTemplate:execute ESF")
        
        # Extract information for execution
        self._getVariables(eventSet)
        if not self._hazardEvents:
            return []
        # Here is the format of the dictionary that is returned for
        #  each product generated: 
        #  [
        #    {
        #     "productID": "ESF",
        #     "productDict": productDict,
        #     }
        #   ]
        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._hazardEvents) 
        return productDicts, hazardEvents        
    
    
    def _productParts(self, productID):
        '''
        List of product parts in the order they appear in the product
        Orders and defines the Product Parts for the given productID
        '''
        return [
            ProductPart('wmoHeader_noCR'),
            ProductPart('ugcHeader'),
            ProductPart('CR'),
            ProductPart('productHeader'),
            ProductPart('narrativeForecastInformation'),
            ProductPart('end'),
            ]
            
    def _groupSegments(self, segments):
        '''
        Group the segments into the products
        
         ESF products are not segmented, so make a product from each segment
        '''        
        productSegmentGroups = []
        for segment in segments:
            segmentGroup = {
                       "productID": "ESF",
                       "productName": self._ESF_ProductName,
                       "geoType": "area",
                       "vtecEngine": self._vtecEngine,
                       "mapType": "counties",
                       "segmented": False,
                       "segments": [segment],
                       }
            productSegmentGroups.append(segmentGroup)
        return productSegmentGroups
    
    def _addToProductDict(self, productDict):
        '''
        This method can be overridden by the Product Generators to add specific product information to the productDict
        '''
        productDict['ugcHeader'] = self._ugcHeader
        productDict['narrativeForecastInformation'] = '''
         |* 
         Headline defining the type of flooding being addressed 
              (e.g., flash flooding, main stem
              river flooding, snow melt flooding)

         Area covered
         
         Possible timing of the event
         
         Relevant factors 
                 (e.g., synoptic conditions, 
                 quantitative precipitation forecasts (QPF), or
                 soil conditions)
                 
         Definition of an outlook (tailored to the specific situation)
         
         A closing statement indicating when additional information will be provided.
         *|
         '''

        

           
