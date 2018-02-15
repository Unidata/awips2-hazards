'''
Description: Product Generator for the FFW and FFS products.

@author Tracy.L.Hansen@noaa.gov
@version 1.0
'''
import collections
import HydroGenerator

class Product(HydroGenerator.Product):

    def __init__(self) :
        ''' Hazard Types covered
             ('FF.W.Convective',     'Flood'),
             ('FF.W.NonConvective',  'Flood'), 
             ('FF.W.BurnScar',       'Flood'), 
        '''
        super(Product, self).__init__()

        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'FFW_FFS_ProductGenerator'

    def _initialize(self) :
        super(Product, self)._initialize()
        self._productID = 'FFW'
        self._productCategory = 'FFW_FFS'
        self._productName = 'Flash Flood Warning'
        self._purgeHours = -1.0
        self._FFW_ProductName = 'Flash Flood Warning'
        self._FFS_ProductName = 'Flash Flood Statement'
        # Polygon-based, so locations listed will be limited to within the polygon rather than county area
        self._polygonBased = True

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'Raytheon'
        metadata['description'] = 'Content generator for flash flood warning.'
        metadata['version'] = '1.0'
        return metadata

    def defineDialog(self, eventSet):
        return {}

    def executeFrom(self, dataList, eventSet, productParts=None):
        if isinstance(productParts, list) and len(productParts) > 0:
            dataList = self.correctProduct(dataList, eventSet, productParts, True)
        else:
            self.updateExpireTimes(dataList)
        return dataList

    def execute(self, eventSet, dialogInputMap):
        self._initialize()

        self.logger.info('Start ProductGeneratorTemplate:execute FFW_FFS')

        # Extract information for execution
        self._getVariables(eventSet, dialogInputMap)
        eventSetAttributes = eventSet.getAttributes()

        if not self._inputHazardEvents:
            return []

        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._inputHazardEvents, eventSetAttributes)

        return productDicts, hazardEvents

    def _preProcessHazardEvents(self, hazardEvents):
        '''
        Add expirationTime to each event and set
        immediate cause for FF.W.NonConvective prior to VTEC processing
        '''
        for hazardEvent in hazardEvents:
            expirationTime = self._tpc.round(hazardEvent.getEndTime())
            hazardEvent.setExpirationTime(expirationTime)
            if hazardEvent.getHazardType() == 'FF.W.NonConvective':
                immediateCause = self.hydrologicCauseMapping(hazardEvent.get('hydrologicCause'))
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
            productPartsDict = self._hydroProductParts.productParts_FFW(productSegments)
        elif productID == 'FFS':
            productPartsDict = self._hydroProductParts.productParts_FFS(productSegments)
        productParts = self.createProductParts(productPartsDict)
        productSegmentGroup.setProductParts(productParts)
