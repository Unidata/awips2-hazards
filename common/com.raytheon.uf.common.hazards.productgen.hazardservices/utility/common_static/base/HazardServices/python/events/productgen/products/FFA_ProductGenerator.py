'''
    Description: Product Generator for the FFA product.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------

    Jan 26, 2015   4936      Chris.Cody  Implement scripts for Flash Flood Watch Products (FFA,FAA,FLA)
    Jan 31, 2015   4937      Robert.Blum General cleanup along with moving some stuff to the formatter.
    Mar 23, 2015   7165      Robert.Blum Code consolidation - removed _prepareSection().
    Apr 16, 2015   7579      Robert.Blum Updates for amended Product Editor.
    Nov 09, 2015   7532      Robert.Blum Sorting the vtecRecords so that the product parts for sections
                                         are in the correct order.
    Jun 08, 2016   9620      Robert.Blum Changed self._purgeHours to a float since purge hours can
                                         include minutes (15min intervals).
    Jul 06, 2016  18257      Kevin.Bisanz Added eventSet parameter to executeFrom(..)
                                         
    
    @author Chris.Cody
    @version 1.0
    '''
import os, types, copy, collections
from KeyInfo import KeyInfo
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
        self._includeAreaNames = True
        self._includeCityNames = True
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
        cancel_dict = self._checkForCancel(self._inputHazardEvents, productSegmentGroups)
        dialogDict = self._organizeByProductLabel(self._productLevelMetaData_dict, cancel_dict, 'FFA_tabs')

        return dialogDict

    def executeFrom(self, dataList, eventSet, keyInfo=None):
        if keyInfo is not None:
            dataList = self.correctProduct(dataList, eventSet, keyInfo, False)
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
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_area(productSegments))
        elif geoType == 'point':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_point(productSegments))

# Allow interdependencies for the dialog's megawidgets to work.     
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    return MetaData_FFA_FLW_FLS.applyInterdependencies(triggerIdentifiers, mutableProperties)
