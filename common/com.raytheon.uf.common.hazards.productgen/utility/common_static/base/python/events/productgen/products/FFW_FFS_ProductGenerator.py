'''
    Description: Product Generator for the FFW and FFS products.
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation
    Nov      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents, simplifying product
                                                 dictionary
    Dec 1, 2014    4373      Dan Schaffer        HID Template migration for warngen
    Dec 15, 2014   3846,4375 Tracy.L.Hansen      'defineDialog' -- Product Level information and Ending Hazards
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
'''

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
        self._getVariables(eventSet, dialogInputMap)
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
        # TODO Can this method now be consolidated with the floodBasis method?
        eventTime = vtecRecord.get('startTime')            
        eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z ', stripLeading=True, timeZones=self._productSegment.timeZones)
        para = 'At ' + eventTime + ' '
        basis = self.basisFromHazardEvent(hazardEvent)
        if basis is None :
            basis = ' '+floodDescription+' was reported'
        para += basis
        return para
    
    def getImpactsPhrase(self, vtecRecord, hazardEvent, metaData, lineLength=69):       
        # Impacts bullet
        return self.floodImpactsPhrase(vtecRecord, hazardEvent, metaData, lineLength)
    

    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, True)
        return dataList

    def _damInfo(self):
        from MapsDatabaseAccessor import MapsDatabaseAccessor
        mapsAccessor = MapsDatabaseAccessor()
        damInfoDict = mapsAccessor.getAllDamInundationMetadata()
        
        return damInfoDict


