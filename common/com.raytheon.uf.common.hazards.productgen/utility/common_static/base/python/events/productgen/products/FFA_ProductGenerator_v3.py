'''
    Description: Product Generator for the FFA product.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 26, 2015   4936      Chris.Cody  Implement scripts for Flash Flood Watch Products (FFA,FAA,FLA)
    Jan 31, 2015   4937      Robert.Blum General cleanup along with moving some stuff to the formatter.
    
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
        self._productGeneratorName = 'FFA_ProductGenerator_v3'

    def _initialize(self) :
        super(Product, self)._initialize()
        self._FAA_ProductName = 'Flood Watch'
        self._FFA_ProductName = 'Flash Flood Watch'
        self._FLA_ProductName = 'Flood Watch'
        self._productID = 'FFA'
        self._productCategory = "FFA" #This is necessary to generate the PIL value
        self._productName = self._FFA_ProductName
        self._purgeHours = 8
        self._includeAreaNames = True
        self._includeCityNames = True

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

    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, False)
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

    # TODO Check on attributes for this product
    def _prepareSection(self, event, vtecRecord, metaData):
        attributes = event.getHazardAttributes()

        # This creates a list of ints for the eventIDs and also formats the UGCs correctly.
        eventIDs, ugcList = self.parameterSetupForKeyInfo(list(vtecRecord.get('eventID', None)), attributes.get('ugcs', None))

        # Attributes that get skipped. They get added to the dictionary indirectly.
        noOpAttributes = ('ugcs', 'ugcPortions', 'ugcPartsOfState')

        section = collections.OrderedDict()
        for attribute in attributes:
            # Special case attributes that need additional work before adding to the dictionary
            if attribute == 'additionalInfo':
                additionalInfo, citiesListFlag = self._prepareAdditionalInfo(attributes[attribute] , event, metaData)
                additionalCommentsKey = KeyInfo('additionalComments', self._productCategory, self._productID, eventIDs, ugcList, editable=True, label='Additional Comments')
                section[additionalCommentsKey] = additionalInfo
                section['citiesListFlag'] = citiesListFlag
            elif attribute == 'cta':
                # These are now added at the segment level. Do we want to add here as well?
                callsToActionValue = self._tpc.getProductStrings(event, metaData, 'cta')
                section['callsToAction'] = callsToActionValue
            elif attribute == 'floodSeverity':
                section['floodSeverity'] = self._tpc.getProductStrings(event, metaData, 'floodSeverity')
            elif attribute == 'floodRecord':
                section['floodRecord'] = self._tpc.getProductStrings(event, metaData, 'floodRecord')
            elif attribute in noOpAttributes:
                continue
            else:
                section[attribute] = attributes.get(attribute, None)

        section['impactedAreas'] = self._prepareImpactedAreas(attributes)
        section['impactedLocations'] = self._prepareImpactedLocations(event.getGeometry(), [])
        section['geometry'] = event.getGeometry()
        section['timeZones'] = self._productSegment.timeZones
        section['vtecRecord'] = vtecRecord
        section['impacts'] = self._tpc.getProductStrings(event, metaData, 'impacts')
        section['impactsStringForStageFlowTextArea'] = event.get('impactsStringForStageFlowTextArea', None)
        section['startTime'] = event.getStartTime()
        section['endTime'] = event.getEndTime()
        self._cityList(section, event)

        if event.get('pointID'):
            # Add RiverForecastPoint data to the dictionary
            self._prepareRiverForecastPointData(event.get('pointID'), section, event)

        self._setProductInformation(vtecRecord, event)
        return section

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
        if geoType == 'area':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_area(productSegments))
        elif geoType == 'point':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_point(productSegments))
           
    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, False)
        return dataList

# Allow interdependencies for the dialog's megawidgets to work.     
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    return MetaData_FFA_FLW_FLS.applyInterdependencies(triggerIdentifiers, mutableProperties)

