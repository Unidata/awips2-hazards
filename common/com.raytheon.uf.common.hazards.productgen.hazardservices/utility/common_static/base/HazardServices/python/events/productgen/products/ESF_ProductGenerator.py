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
"""
import os, types, sys, collections
from HydroProductParts import HydroProductParts
import HydroGenerator
from KeyInfo import KeyInfo

class Product(HydroGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()  
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'ESF_ProductGenerator'
                
    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD/Raytheon"
        metadata['description'] = "Product generator for ESF."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self, eventSet):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        return {}

    def _initialize(self):
        super(Product, self)._initialize()
        # This is for the VTEC Engine
        self._productID = "ESF"
        self._productCategory = "ESF"
        self._areaName = '' 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        self._purgeHours = 8.0
        self._ESF_ProductName = 'Hydrologic Outlook'
        self._includeAreaNames = False
        self._includeCityNames = False
        self._vtecProduct = False
        # Polygon-based, so locations listed will be limited to within the polygon rather than county area
        self._polygonBased = True

    def execute(self, eventSet, dialogInputMap):
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
        self._initialize() 
        self.logger.info("Start ProductGeneratorTemplate:execute ESF")
        
        # Extract information for execution
        self._getVariables(eventSet)
        eventSetAttributes = eventSet.getAttributes()
        
        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._inputHazardEvents, eventSetAttributes)

        return productDicts, hazardEvents
                
    def _groupSegments(self, segments):
        '''
        Group the segments into the products
        
         ESF products are not segmented, so make a product from each 'segment' i.e. HY.O event
        '''        
        productSegmentGroups = []
        for segment in segments:
            vtecRecords = self.getVtecRecords(segment)
            productSegmentGroups.append(self.createProductSegmentGroup('ESF', self._ESF_ProductName, 'area', self._vtecEngine, 'counties', False,
                                            [self.createProductSegment(segment, vtecRecords)]))            
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        return productSegmentGroups
    
    def _addProductParts(self, productSegmentGroup):
        productSegments = productSegmentGroup.productSegments
        productSegmentGroup.setProductParts(self._hydroProductParts._productParts_ESF(productSegments))

    def _narrativeForecastInformation(self, segmentDict, productSegmentGroup, productSegment):  
        default = '''
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
        productDict['narrativeForecastInformation'] = self._section.hazardEvent.get('narrativeForecastInformation', default)

    def executeFrom(self, dataList, eventSet, keyInfo=None):
        if keyInfo is not None:
            dataList = self.correctProduct(dataList, eventSet, keyInfo, False)
        else:
            self.updateExpireTimes(dataList)
        return dataList
