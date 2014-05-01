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
import os, types, copy, sys, json
import Legacy_ProductGenerator
from HydroProductParts import HydroProductParts

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
       
    def defineDialog(self):
        '''
        @return: dialog definition to solicit user input before running tool
        '''  
         #  Here is an example of a dialog definition which you could use
         #  as a starting point if you want to add information to be
         #  solicited from the user:

        overviewHeadlineDict = {}
        overviewHeadlineDict['fieldName'] = 'overviewHeadline'
        overviewHeadlineDict['label'] = 'Overview Headline'
        overviewHeadlineDict['fieldType'] = 'Text'
        overviewHeadlineDict['maxChars'] = 200
        overviewHeadlineDict['visibleChars'] = 50
        overviewHeadlineDict['expandHorizontally'] = False

        overviewFieldDict = {}
        overviewFieldDict['fieldName'] = 'overview'
        overviewFieldDict['label'] = 'Overview'
        overviewFieldDict['fieldType'] = 'Text'
        overviewFieldDict['maxChars'] = 200
        overviewFieldDict['visibleChars'] = 50
        overviewFieldDict['expandHorizontally'] = False        
        
        dialogDict = {}
        fieldDicts = [overviewHeadlineDict, overviewFieldDict]
        dialogDict['fields'] = fieldDicts
        
        valueDict = {'overviewHeadline': 'Enter overview headline here.', 'overview':'Enter overview here.'}
        dialogDict['values'] = valueDict        

	# TODO Not ready to actually move this into repo.
	# If you want to test the dialogInfo, comment this and uncomment 
	# the next.
	return {}
        # return dialogDict
                
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
        self._getVariables(eventSet)
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
        if len(self._point_segment_vtecRecords_tuples):
            pointSegmentGroup = {
                       'productID': 'FFA',
                       'productName': self._FFA_ProductName,
                       'geoType': 'point',
                       'vtecEngine': self._pointVtecEngine,
                       'mapType': 'counties',
                       'segmented': True,
                       'segment_vtecRecords_tuples': self._point_segment_vtecRecords_tuples,
                       }
            productSegmentGroups.append(pointSegmentGroup)
        if len(self._area_segment_vtecRecords_tuples):
            areaSegmentGroup = {
                       'productID': 'FFA',
                       'productName': self._FFA_ProductName,
                       'geoType': 'area',
                       'vtecEngine': self._areaVtecEngine,
                       'mapType': 'publicZones',
                       'segmented': True,
                       'segment_vtecRecords_tuples': self._area_segment_vtecRecords_tuples,
                       }
            productSegmentGroups.append(areaSegmentGroup)
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
            #print 'FFA ProductSegmentGroup \n', productSegmentGroup
        #self.flush()
        return productSegmentGroups
    
    def _addProductParts(self, productSegmentGroup):
        geoType = productSegmentGroup.get('geoType')
        segment_vtecRecords_tuples = productSegmentGroup.get('segment_vtecRecords_tuples')
        if geoType == 'area':
            productSegmentGroup['productParts'] = self._hydroProductParts._productParts_FFA_FLW_FLS_area(segment_vtecRecords_tuples)
        elif geoType == 'point':
            productSegmentGroup['productParts'] = self._hydroProductParts._productParts_FFA_FLW_FLS_point(segment_vtecRecords_tuples)
        del productSegmentGroup['segment_vtecRecords_tuples'] 
           
    def executeFrom(self, dataList):
        # NOTE -- To properly update the VTEC and ETN's properly it is necessary to call the
        #  execute method.
        # This method should not be called for when the user wants to Issue.
        return dataList
           
