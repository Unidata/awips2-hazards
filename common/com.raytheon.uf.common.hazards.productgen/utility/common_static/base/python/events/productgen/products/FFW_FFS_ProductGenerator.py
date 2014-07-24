'''
10    Description: Product Generator for the FFW and FFS products.
12    
13    SOFTWARE HISTORY
14    Date         Ticket#    Engineer    Description
15    ------------ ---------- ----------- --------------------------
16    April 5, 2013            Tracy.L.Hansen      Initial creation
      Nov      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents, simplifying product
                                                 dictionary
17    
18    @author Tracy.L.Hansen@noaa.gov
19    @version 1.0
20    '''

import os, types, copy, sys, json, collections
import Legacy_ProductGenerator
from HydroProductParts import HydroProductParts

class Product(Legacy_ProductGenerator.Product):
    
    def __init__(self):
        ''' Hazard Types covered
             ('FF.W.Convective',     'Flood'),
             ('FF.W.NonConvective',  'Flood'), 
             ('FF.W.BurnScar',       'Flood'), 
        '''           
        super(Product, self).__init__()              

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD'
        metadata['description'] = 'Product generator for FFW_FFS.'
        metadata['version'] = '1.0'
        return metadata
       
    def defineDialog(self, eventSet):
        '''
        @return: dialog definition to solicit user input before running tool
        '''  
        return {}

    def _initialize(self):
        # TODO Fix problem in framework which does not re-call the constructor
        self.initialize()
        # This is for the VTEC Engine
        self._productCategory = 'FFW_FFS'
        self._areaName = '' 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # TODO gather this as part of the Hazard Information Dialog
        self._purgeHours = -1
        self._FFW_ProductName = 'Flash Flood Warning'
        self._FFS_ProductName = 'Flash Flood Statement'
        self._includeAreaNames = True
        self._includeCityNames = False
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
        self.logger.info('Start ProductGeneratorTemplate:execute FFW_FFS')
        
        # Extract information for execution
        self._getVariables(eventSet)
        if not self._inputHazardEvents:
            return []
        # Here is the format of the dictionary that is returned for
        #  each product generated: 
        #  [
        #    {
        #     'productID': 'FFA',
        #     'productDict': xmlDict,
        #     }
        #   ]
        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._inputHazardEvents) 
        return productDicts, hazardEvents  
    
    def _preProcessHazardEvents(self, hazardEvents):
        '''
        Set Immediate Cause for FF.W.NonConvective prior to VTEC processing
        '''
        for hazardEvent in hazardEvents:
            if hazardEvent.getHazardType() == 'FF.W.NonConvective':
                immediateCause = self.hydrologicCauseMapping(hazardEvent.get('hydrologicCause'), 'immediateCause')
                hazardEvent.set('immediateCause', immediateCause)
              
    def _groupSegments(self, segments):
        '''
         Group the segments into the products
            return a list of productSegmentGroup dictionaries
        
         Check the pil 
          IF FFW -- make a new FFW -- there can only be one segment per FFW
          Group the segments into FFS products with same ETN
        '''
        # For short fused areal products, 
        #   we can safely make the assumption of only one hazard/action per segment.
        productSegmentGroups = []
        for segment in segments:
            vtecRecords = self.getVtecRecords(segment)
            for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
                pil = vtecRecord.get('pil')
                etn = vtecRecord.get('etn')
                if pil == 'FFW':
                    # Create new FFW
                    productSegmentGroup = self.createProductSegmentGroup(pil, self._FFW_ProductName, 'area', self._vtecEngine, 'counties', False,
                                            [self.createProductSegment(segment, vtecRecords)], etn=etn, formatPolygon=True)
                    productSegmentGroups.append(productSegmentGroup)
                else:  # FFS
                    # See if this record matches the ETN of an existing FFS
                    found = False
                    for productSegmentGroup in productSegmentGroups:
                        if productSegmentGroup.productID == 'FFS' and productSegmentGroup.etn == etn:
                            productSegmentGroup.addProductSegment(self.createProductSegment(segment, vtecRecords))
                            found = True
                    if not found:
                        # Make a new FFS productSegmentGroup
                       productSegmentGroup = self.createProductSegmentGroup(pil, self._FFS_ProductName, 'area', self._vtecEngine, 'counties', True,
                                                [self.createProductSegment(segment, vtecRecords)], etn=etn, formatPolygon=True)
                       productSegmentGroups.append(productSegmentGroup)
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        return productSegmentGroups
        
    def _addProductParts(self, productSegmentGroup):
        productID = productSegmentGroup.productID
        productSegments = productSegmentGroup.productSegments
        if productID == 'FFW':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFW(productSegments))
        elif productID == 'FFS':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFS(productSegments))
        
    def getBasisPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):
        # Basis bullet
        if hazardEvent.getSubType() == 'NonConvective':
            return self.nonConvectiveBasisPhrase(vtecRecord, hazardEvent, metaData, 'Flash Flooding', lineLength)
        else:
            return self.floodBasisPhrase(vtecRecord, hazardEvent, metaData, 'Flash Flooding', lineLength)

    def nonConvectiveBasisPhrase(self, vtecRecord, hazardEvent, metaData, floodDescription, lineLength=69):
        eventTime = vtecRecord.get('startTime')            
        eventTime = self._tpc.getFormattedTime(eventTime / 1000, '%I%M %p %Z ',shiftToLocal=1, stripLeading=1).upper()
        para = 'At ' + eventTime
        basis = self._tpc.getProductStrings(hazardEvent, metaData, 'basis')
        para += basis + '.'
        return para
    
    def getImpactsPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):       
        # Impacts bullet
        return self.floodImpactsPhrase(vtecRecord, hazardEvent, metaData, lineLength)
    

    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, True)
        return dataList
    
    def _damInfo(self):
        return {
                'Big Rock Dam': {
                        'riverName': 'Phil River',
                        'cityInfo': 'Evan...located about 3 miles',
                        'scenarios': {
                            'highFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 18 feet in 16 minutes.',
                            'highNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 23 feet in 31 minutes.',
                            'mediumFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 14 feet in 19 minutes.',
                            'mediumNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 17 feet in 32 minutes.',
                            },
                        'ruleOfThumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                        and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.''',
                    },
                'Branched Oak Dam': {
                        'riverName': 'Kells River',
                        'cityInfo': 'Dangelo...located about 6 miles',
                        'scenarios': {
                            'highFast': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 19 feet in 32 minutes.',
                            'highNormal': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 26 feet in 56 minutes.',
                            'mediumFast': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 14 feet in 33 minutes.',
                            'mediumNormal': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 20 feet in 60 minutes.',
                            },
                        'ruleOfThumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                        and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.''',
                    },                
                }


