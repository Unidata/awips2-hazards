"""
    Description: Product Generator for the FFA product.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation    
    Nov      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents, simplifying product
                                                 dictionary
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
    """
import os, types, copy, sys, json
import ProductTemplate

class Product(ProductTemplate.Product):
    
    def __init__(self):
        super(Product, self).__init__()       
        # This is for the VTEC Engine
        self._productCategory = "FFA"
        # This will be accessed through the afos2awips utility which is still being
        #   tested
        self._areaName = "" 
        # Does the product use zones or counties for areal hazards
        self._areaUgcType = "publicZones"
        # Does the product use zones or counties for point hazards
        self._pointUgcType = "counties"
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # NOTE: In PV2, this will gathered as part of the Hazard Information Dialog
        self._purgeHours = 8
        self._FFA_ProductName = "FLOOD WATCH"
        self._includeAreaNames = True
        self._includeCityNames = True
                
    def getScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD"
        metadata['description'] = "Product generator for FFA."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        return {}
         #  Here is an example of a dialog definition which you could use
         #  as a starting point if you want to add information to be
         #  solicited from the user:

#        overviewHeadlineDict = {}
#        overviewHeadlineDict["fieldName"] = "overviewHeadline"
#        overviewHeadlineDict["label"] = "Overview Headline"
#        overviewHeadlineDict["fieldType"] = "Text"
#        overviewHeadlineDict["maxChars"] = 200
#        overviewHeadlineDict["visibleChars"] = 50
#        overviewHeadlineDict["expandHorizontally"] = 1
#
#        overviewFieldDict = {}
#        overviewFieldDict["fieldName"] = "overview"
#        overviewFieldDict["label"] = "Overview"
#        overviewFieldDict["fieldType"] = "Text"
#        overviewFieldDict["maxChars"] = 200
#        overviewFieldDict["visibleChars"] = 50
#        overviewFieldDict["expandHorizontally"] = 1        
#        
#        dialogDict = {}
#        fieldDicts = [overviewHeadlineDict, overviewFieldDict]
#        dialogDict["fields"] = fieldDicts
#        
#        valueDict = {"overviewHeadline": "Enter overview headline here.", "overview":"Enter overview here."}
#        dialogDict["valueDict"] = valueDict        
#        return dialogDict
                
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
        self.logger.info("Start ProductGeneratorTemplate:execute FFA")
        
        # Extract information for execution
        self._getVariables(eventSet)
        if not self._hazardEvents:
            return []
        # Here is the format of the dictionary that is returned for
        #  each product generated: 
        #  [
        #    {
        #     "productID": "FFA",
        #     "productDict": productDict,
        #     }
        #   ]
        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._hazardEvents) 
        return productDicts, hazardEvents        
    
    def _getSegments(self, hazardEvents):
        return self._getSegments_ForPointsAndAreas(hazardEvents)
            
    def _groupSegments(self, segments):
        '''
        Group the segments into the products
         In this case, group the point FFAs and the area FFA's separately
         return a list of tuples (productType, segments)
        
         All FFA products are segmented
        '''        
        productSegmentGroups = []
        if len(self._pointSegments):
            pointSegmentGroup = {
                       "productID": "FFA",
                       "productName": self._FFA_ProductName,
                       "geoType": "point",
                       "vtecEngine": self._pointVtecEngine,
                       "mapType": "counties",
                       "segmented": True,
                       "segments": self._pointSegments,
                       }
            productSegmentGroups.append(pointSegmentGroup)
        if len(self._areaSegments):
            areaSegmentGroup = {
                       "productID": "FFA",
                       "productName": self._FFA_ProductName,
                       "geoType": "area",
                       "vtecEngine": self._areaVtecEngine,
                       "mapType": "publicZones",
                       "segmented": True,
                       "segments": self._areaSegments
                       }
            productSegmentGroups.append(areaSegmentGroup)
        return productSegmentGroups
    
        

           
