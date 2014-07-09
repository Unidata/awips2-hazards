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

import os, types, copy, sys, json
import Legacy_ProductGenerator
from HydroProductParts import HydroProductParts

class Product(Legacy_ProductGenerator.Product):
    
    def __init__(self):
        ''' Hazard Types covered
             ('FF.W.Convective',     'Flood'),
             ('FF.W.NonConvective',  'Flood'),  
        '''           
        super(Product, self).__init__()              

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD'
        metadata['description'] = 'Product generator for FFW_FFS.'
        metadata['version'] = '1.0'
        return metadata
       
    def defineDialog(self):
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
   
    def _groupSegments(self, segments):
        '''
         Group the segments into the products
            return a list of productSegmentGroup dictionaries
        
         Check the pil 
          IF FFW -- make a new FFW -- there can only be one segment per FFW
          Group the segments into FFS products with same ETN
        '''
        # For short fused areal products, 
        #     we can safely make the assumption of only one hazard/action per segment.
        productSegmentGroups = []
        for segment in segments:
            vtecRecords = self.getVtecRecords(segment)
            for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
                pil = vtecRecord.get('pil')
                etn = vtecRecord.get('etn')
                if pil == 'FFW':
                    # Create new FFW
                    productSegmentGroup = { 
                       'productID' : pil,
                       'productName': self._FFW_ProductName,
                       'geoType': 'area',
                       'vtecEngine': self._vtecEngine,
                       'mapType': 'counties',
                       'segmented': False,
                       'etn':etn,
                       'formatPolygon': True,
                       'segment_vtecRecords_tuples': [(segment, vtecRecords)],
                       }
                    productSegmentGroups.append(productSegmentGroup)
                else: # FFS
                    # See if this record matches the ETN of an existing FFS
                    found = False
                    for productSegmentGroup in productSegmentGroups:
                        if productSegmentGroup.get('productID') == 'FFS' and productSegmentGroup.get('etn') == etn:
                            productSegmentGroup['self._area_segment_vtecRecords_tuples'].append((segment, vtecRecords))
                            found = True
                    if not found:
                        # Make a new FFS productSegmentGroup
                       productSegmentGroup = {
                            'productID' : pil,
                            'productName': self._FFS_ProductName,
                            'geoType': 'area',
                            'vtecEngine': self._vtecEngine,
                            'mapType': 'counties',
                            'segmented': True,
                            'etn':etn,
                            'formatPolygon': True,
                            'segment_vtecRecords_tuples': [(segment, vtecRecords)],
                        }
                       productSegmentGroups.append(productSegmentGroup)
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
            #print 'FFW_FFS ProductSegmentGroup \n', productSegmentGroup
        #self.flush()
        return productSegmentGroups
        
    def _addProductParts(self, productSegmentGroup):
        productID = productSegmentGroup.get('productID')
        segment_vtecRecords_tuples = productSegmentGroup.get('segment_vtecRecords_tuples')
        if productID == 'FFW':
            productSegmentGroup['productParts'] = self._hydroProductParts._productParts_FFW(segment_vtecRecords_tuples)
        elif productID == 'FFS':
            productSegmentGroup['productParts'] = self._hydroProductParts._productParts_FFS(segment_vtecRecords_tuples)
        del productSegmentGroup['segment_vtecRecords_tuples']
        
    def getBasisPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69):
        #  Time is off of last frame of data
        try :
            eventTime = self._sessionDict['framesInfo']['frameTimeList'][-1]
        except :
            eventTime = vtecRecord.get('startTime')            
        eventTime = self._tpc.getFormattedTime(eventTime/1000, '%I%M %p %Z ', 
                                               shiftToLocal=1, stripLeading=1).upper()
        para = '* at '+eventTime
        basis = self.getMetadataItemForEvent(hazardEvent, metaData,  'basis')
        if basis is None :
            basis = ' Flash Flooding was reported'
        para += basis +  'Flash Flooding ' + self.descWxLocForEvent(hazardEvent)
        motion = self.descMotionForEvent(hazardEvent)

        if motion is None :
            para += '.'
        else :
            para += self.descWxLocForEvent(hazardEvent, '. THIS RAIN WAS ', \
               '. THIS STORM WAS ', '. THESE STORMS WERE ', '-')
            para += motion+'.'
        return para
    
    def getImpactsPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69 ):
        '''
        #* LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO CASTLE
        #  PINES...THE PINERY...SURREY RIDGE...SEDALIA...LOUVIERS...HIGHLANDS
        #  RANCH AND BEVERLY HILLS. 
        '''        
        para = '* LOCATIONS IN THE WARNING INCLUDE BUT' + \
               ' ARE NOT LIMITED TO '
        para += self.getCityInfo(self._ugcs)
        return '\n'+para + '\n'
    
    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, True)
        return dataList


