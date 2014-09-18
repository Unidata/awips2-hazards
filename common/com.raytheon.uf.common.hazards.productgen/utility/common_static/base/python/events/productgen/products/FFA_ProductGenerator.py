'''
    Description: Product Generator for the FFA product.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation    
    Nov      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents, simplifying product
                                                 dictionary
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
    '''
import os, types, copy, sys, json, collections
import Legacy_ProductGenerator
from HydroProductParts import HydroProductParts
from Bridge import Bridge

class Product(Legacy_ProductGenerator.Product):
    
    def __init__(self):
        ''' Hazard Types covered
             ('FF.A', 'Flood'),
             ('FA.A', 'Flood'),
             ('FL.A', 'Flood1'),
        '''                                                   
        super(Product, self).__init__() 
                
    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD'
        
        metadata['description'] = 'Product generator for FFA.'
        metadata['version'] = '1.0'
        return metadata
       
    def defineDialog(self, eventSet):
        '''
        @return: dialog definition to solicit user input before running tool
        '''  
         #  Here is an example of a dialog definition which you could use
         #  as a starting point if you want to add information to be
         #  solicited from the user:
        self._initialize()

        # TODO -- set up hazardEvents and productID's 
        # Get Product Level Meta Data
        self.bridge = Bridge() 
        metaData =   self.getMetaData([], {'productID': 'FFA'}, 'MetaData_FFA_FLW_FLS')
        # TODO After Product Staging dialog can handle scripts, change this to:
        # return metaData  
        dialogDict = {}
        productLevelFields = metaData.get('metadata')
          
        # Check for Cancel
        cancelFields = self._checkForCancel(eventSet)
        if cancelFields:
            cancelFields = metaDataList
        else:
            cancelFields = []

        fields = productLevelFields + cancelFields
        if fields:
            dialogDict['fields'] = fields
        return dialogDict
                
    def _initialize(self):
        # TODO Fix problem in framework which does not re-call the constructor
        self.initialize()
        # This is for the VTEC Engine
        self._productCategory = 'FFA'
        # This will be accessed through the afos2awips utility which is still being
        #   tested
        self._areaName = '' 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # TODO gather this as part of the Hazard Information Dialog
        self._purgeHours = 8
        self._FFA_ProductName = 'Flood Watch'
        self._includeAreaNames = True
        self._includeCityNames = True
        self._hydroProductParts = HydroProductParts()
                
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
        self.logger.info('Start ProductGeneratorTemplate:execute FFA')
        
        # Extract information for execution
        self._getVariables(eventSet, dialogInputMap)
        if not self._inputHazardEvents:
            return []
        # Here is the format of the dictionary that is returned for
        #  each product generated: 
        #  [
        #    {
        #     'productID': 'FFA',
        #     'productDict': productDict,
        #     }
        #   ]
        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._inputHazardEvents) 
        return productDicts, hazardEvents        
    
    def _getSegments(self, hazardEvents):
        return self._getSegments_ForPointsAndAreas(hazardEvents)
            
    def _groupSegments(self, segments):
        '''
        Group the segments into the products
         In this case, group the point FFAs and the area FFA's separately
         return a list of productSegmentGroup dictionaries

         All FFA products are segmented
        '''        
        productSegmentGroups = []
        if len(self._point_productSegments):
            pointSegmentGroup = self.createProductSegmentGroup('FFA', self._FFA_ProductName, 'point', self._pointVtecEngine, 'counties', True,
                                                         self._point_productSegments)
            productSegmentGroups.append(pointSegmentGroup)
        if len(self._area_productSegments):
            areaSegmentGroup = self.createProductSegmentGroup('FFA', self._FFA_ProductName, 'area', self._areaVtecEngine, 'publicZones', True,
                                                        self._area_productSegments)
            productSegmentGroups.append(areaSegmentGroup)
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        return productSegmentGroups
    
    def _addProductParts(self, productSegmentGroup):
        geoType = productSegmentGroup.geoType
        productSegments = productSegmentGroup.productSegments
        if geoType == 'area':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_area(productSegments))
        elif geoType == 'point':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_point(productSegments))
           
    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, False)
        return dataList

    def getBasisPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):
        # Basis bullet
        return hazardEvent.get('basis')
            
