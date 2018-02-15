'''
    Description: Product Generator for the FFA product.
    
    @author Chris.Cody
    @version 1.0
    '''
import os, types, copy, collections
import HydroGenerator

# Bring in the interdependencies script from the metadata file.
import MetaData_FFA_FLW_FLS

class Product(HydroGenerator.Product):

    def __init__(self):
        ''' Hazard Types covered
             ('FF.A', 'Flood'),
             ('FA.A', 'Flood'),
             ('FL.A', 'Flood1'),
        '''
        super(Product, self).__init__() 

        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'FFA_ProductGenerator'

    def _initialize(self) :
        super(Product, self)._initialize()
        self._productID = 'FFA'
        self._productCategory = "FFA" #This is necessary to generate the PIL value
        self._productName = 'Flood Watch'
        self._purgeHours = 8.0
        # Not Polygon-based, so locations listed will not be limited to within the polygon, 
        # but rather than UGC area e.g. county or zone
        self._polygonBased = False

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'Raytheon'
        metadata['description'] = 'Content generator for flash flood watch.'
        metadata['version'] = '1.0'
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: dialog definition to solicit user input before running tool
        '''
        productSegmentGroups = self._previewProductSegmentGroups(eventSet)
        dialogDict = self._organizeByProductLabel(self._productLevelMetaData_dict, 'FFA_tabs')

        return dialogDict

    def executeFrom(self, dataList, eventSet, productParts=None):
        if isinstance(productParts, list) and len(productParts) > 0:
            dataList = self.correctProduct(dataList, eventSet, productParts, False)
        else:
            self.updateExpireTimes(dataList)
        return dataList

    def execute(self, eventSet, dialogInputMap):
        self._initialize()
        self.logger.info('Start ProductGeneratorTemplate:execute FFA')

        self._dialogInputMap = dialogInputMap
        # Extract information for execution
        self._getVariables(eventSet, dialogInputMap)
        eventSetAttributes = eventSet.getAttributes()

        if not self._inputHazardEvents:
            return []

        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._inputHazardEvents, eventSetAttributes)

        return productDicts, hazardEvents

    def getMetadata(self):
        return self._metadata

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
            pointSegmentGroup = self.createProductSegmentGroup(self._productID, self._productName, 'point', self._pointVtecEngine, 'counties', True,
                                                         self._point_productSegments)
            productSegmentGroups.append(pointSegmentGroup)
        if len(self._area_productSegments):
            areaSegmentGroup = self.createProductSegmentGroup(self._productID, self._productName, 'area', self._areaVtecEngine, 'publicZones', True,
                                                        self._area_productSegments)
            productSegmentGroups.append(areaSegmentGroup)
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        self._productLevelMetaData_dict = self._getProductLevelMetaData(self._inputHazardEvents, 'MetaData_FFA_FLW_FLS', productSegmentGroups)          
        return productSegmentGroups
    
    def _addProductParts(self, productSegmentGroup):
        geoType = productSegmentGroup.geoType
        productSegments = productSegmentGroup.productSegments
        for productSegment in productSegments:
            vtecRecords = productSegment.vtecRecords
            vtecRecords.sort(self._tpc.regularSortHazardAlg)
        if geoType == 'area':
            productPartsDict = self._hydroProductParts.productParts_FFA_FLW_FLS_area(productSegments)
        elif geoType == 'point':
            productPartsDict = self._hydroProductParts.productParts_FFA_FLW_FLS_point(productSegments)
        productParts = self.createProductParts(productPartsDict)
        productSegmentGroup.setProductParts(productParts)

# Allow interdependencies for the dialog's megawidgets to work.     
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    return MetaData_FFA_FLW_FLS.applyInterdependencies(triggerIdentifiers, mutableProperties)

