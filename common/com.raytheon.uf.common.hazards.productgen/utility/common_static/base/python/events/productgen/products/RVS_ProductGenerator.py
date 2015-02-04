import os, types, copy, sys, json, collections
import Legacy_ProductGenerator
from HydroProductParts import HydroProductParts
from TableText import FloodPointTable
from TableText import Column
from com.raytheon.uf.common.time import SimulatedTime
import datetime

class Product(Legacy_ProductGenerator.Product):
    
    def __init__(self):
        super(Product, self).__init__()       
                
    def getScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD"
        metadata['description'] = "Product generator for RVS."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self, eventSet):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        dialogDict = {"title": "RVS Product"}

        headlineStatement = {
             "fieldType": "Text",
             "fieldName": "headlineStatement",
             "expandHorizontally": True,
             "visibleChars": 25,
             "lines": 1,
             "values": "|* Enter Headline Statement *|",
            } 

        narrativeInformation = {
             "fieldType": "Text",
             "fieldName": "narrativeInformation",
             "expandHorizontally": True,
             "visibleChars": 25,
             "lines": 25,
             "values": "|* Enter Narrative Information *|",
            } 
                        
        fieldDicts = [headlineStatement, narrativeInformation]
        dialogDict["metadata"] = fieldDicts        
        return dialogDict

    def _initialize(self):
        # TODO Fix problem in framework which does not re-call the constructor
        self.initialize()
        self._productCategory = "RVS"
        self._areaName = "" 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # TODO gather this as part of the Hazard Information Dialog
        self._purgeHours = 8
        self._RVS_ProductName = "RVS"
        self._includeAreaNames = False
        self._includeCityNames = False
        self._vtecProduct = False
        self._hydroProductParts = HydroProductParts()
        if not self._rfp:
            from RiverForecastPoints import RiverForecastPoints
            millis = SimulatedTime.getSystemTime().getMillis()
            currentTime = datetime.datetime.fromtimestamp(millis / 1000)
            self._rfp = RiverForecastPoints(currentTime)
                
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
        self.logger.info("Start ProductGeneratorTemplate:execute RVS")
        
        # Extract information for execution
        self._getVariables(eventSet)
        if not self._inputHazardEvents:
            return []
        # Here is the format of the dictionary that is returned for
        #  each product generated: 
        #  [
        #    {
        #     "productID": "RVS",
        #     "productDict": productDict,
        #     }
        #   ]
        rvsHazardEvents = []
        for hazardEvent in self._inputHazardEvents:
            if hazardEvent.getHazardType not in ['FL.W', 'FL.Y', 'FL.A', 'HY.S']:
                rvsHazardEvents.append(hazardEvent)  
        self._dialogInputMap = dialogInputMap          
        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(rvsHazardEvents) 
        return productDicts, hazardEvents        
    
    def _getSegments(self, hazardEvents):
        '''
        @param hazardEvents: list of Hazard Events
        @return a list of segments for the hazard events
        '''
        self._generatedHazardEvents = self.determineShapeTypesForHazardEvents(hazardEvents)
        return []

    def getTimeZones(self, hazardEvents):
        ugcs = []
        for hazardEvent in hazardEvents:
            ugcs.extend(hazardEvent.get('ugcs'))
        return self._tpc.hazardTimeZones(ugcs)
                      
    def _groupSegments(self, segments):
        '''
        RVS products do not have segments, so create a productSegmentGroup with no segments. 
        '''        
        productSegmentGroups = []
        productSegmentGroups.append(self.createProductSegmentGroup('RVS', self._RVS_ProductName, 'area', None, 'counties', False, [])) 
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        return productSegmentGroups
    
    def _addProductParts(self, productSegmentGroup):
        productSegments = productSegmentGroup.productSegments
        productSegmentGroup.setProductParts(self._hydroProductParts._productParts_RVS(productSegments))
    
    def _headlineStatement(self, productDict, productSegmentGroup, arguments=None):        
        self._product.timeZones = self.getTimeZones(self._generatedHazardEvents)
        productDict['headlineStatement'] =  self._dialogInputMap.get('headlineStatement')

    def _narrativeInformation(self, productDict, productSegmentGroup, arguments=None):        
        productDict['narrativeInformation'] =  self._dialogInputMap.get('narrativeInformation')
        
    def _floodPointTable(self, productDict, productSegmentGroup, arguments=None):
        # Define desired columns
        millis = SimulatedTime.getSystemTime().getMillis() 
        columns = []
        columns.append(Column('floodStage', width=6, align='<', labelLine1='Fld', labelAlign1='<', labelLine2='Stg', labelAlign2='<'))
        columns.append(Column('observedStage', self._issueTime, width=20, align='<',labelLine1='Observed', labelAlign1='^', labelLine2='Stg    Day    Time', labelAlign2='<'))
        columns.append(Column('forecastStage_next3days', self._issueTime, width=20, labelLine1='Forecast', labelAlign1='^'))
        productDict['floodPointTable'] = FloodPointTable(self._generatedHazardEvents, columns, millis, self._product.timeZones, self._rfp).makeTable()

    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, False)
        return dataList
           
