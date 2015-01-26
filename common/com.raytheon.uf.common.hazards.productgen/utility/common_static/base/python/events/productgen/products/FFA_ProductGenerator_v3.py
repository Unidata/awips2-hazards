'''
    Description: Product Generator for the FFA product.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation    
    Nov      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents, simplifying product
                                                 dictionary
    Oct 22, 2014   5052      mpduff              Fix code error
    Oct 22, 2014   4042      Chris.Golden        Uncommented returning of metadata fields in defineDialog()
                                                 and added apply interdependencies script.
    Dec 15, 2014   3846,4375 Tracy.L.Hansen      'defineDialog' -- Product Level information and Ending Hazards
    Jan 26, 2015   4936      Chris.Cody          Implement scripts for Flash Flood Watch Products (FFA,FAA,FLA)
    
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
    '''
import os, types, copy, sys, json, collections
from KeyInfo import KeyInfo
import HydroGenerator
from HydroProductParts import HydroProductParts
# Bring in the interdependencies script from the metadata file.
import MetaData_FA_A
import MetaData_FF_A
import MetaData_FL_A
import MetaData_FFA_FLW_FLS

from warnings import catch_warnings

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
        self._purgeHours = -1
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
        
        # Extract information for execution
        self._getVariables(eventSet, dialogInputMap)
        eventSetAttributes = eventSet.getAttributes()
        
        # Determine the list of segments given the hazard events 
        segments = self._getSegments(self._inputHazardEvents)
        # Determine the list of products and associated segments given the segments
        productSegmentGroups = self._groupSegments(segments)

        # List of product dictionaries
        productDicts = []
        for productSegmentGroup in productSegmentGroups:
            # Update these first so they are correct when we init the product dictionary.
            self._productID = productSegmentGroup.productID
            self._productName = productSegmentGroup.productName
            self._productLabel = productSegmentGroup.productLabel

            # Init the productDict
            productDict = collections.OrderedDict()
            self._initializeProductDict(productDict, eventSetAttributes)
            productDict['productLabel'] = self._productLabel


            # Add productParts to the dictionary
            productParts = productSegmentGroup.productParts
            productDict['productParts'] = productParts

         # List of product dictionaries
        productDicts = []
        for productSegmentGroup in productSegmentGroups:
            # Update these first so they are correct when we init the product dictionary.
            self._productID = productSegmentGroup.productID
            self._productName = productSegmentGroup.productName
            self._productLabel = productSegmentGroup.productLabel

            # Init the productDict
            productDict = collections.OrderedDict()
            self._initializeProductDict(productDict, eventSetAttributes)
            productDict['productLabel'] = self._productLabel

            # Add productParts to the dictionary
            productParts = productSegmentGroup.productParts
            productDict['productParts'] = productParts

            # Add dialogInputMap entries to the product dictionary
            for key in dialogInputMap.keys():
                # The dialogInputMap only contains the identifier for the CTAs
                # and not the productString. The below code gets the productStrings.
                # TODO Figure out how the replacements should be done.
                # For example #riverName#.
                if key == 'callsToAction_productLevel_' + self._productLabel:
                    dictList = self._productLevelMetaData_dict.get(self._productLabel)
                    for dict in dictList:
                        if dict.get('fieldName') == key:
                            cta_dict = dict
                            break

                    # list of the productStrings to be passed to the formatter
                    newCTAs = []

                    # Loop over the selected CTAs from the staging dialog
                    for value in cta_dict.get('values'):
                        # Compare value with each identifier to find a match
                        for choice in cta_dict.get('choices'):
                            if value == choice.get('identifier'):
                                # Found a match so grab the productString
                                productString = choice.get('productString')

                                # Clean up the productString
                                productString = productString.replace('  ', '')
                                productString = productString.replace('\n', ' ')
                                productString = productString.replace('</br>', '\n')

                                # Add it to the list to be passed to the formatter
                                newCTAs.append(productString)
                                break
                    productDict[key] = newCTAs
                else:
                    # Pass the value as is
                    productDict[key] = dialogInputMap[key]

            segments = []
            event = None
            for productSegment in productSegmentGroup.productSegments:
                self._productSegment = productSegment

                for vtecRecord in productSegment.vtecRecords:
                    for event in eventSet:
                        if event.getEventID() in vtecRecord.get('eventID'):
                            break
                self._initializeMetaData(event)

                # Create the dict for each segment and add it to the list
                self._setupSegment(event)
                segmentDict = self._prepareSegment(event, vtecRecord)
                segments.append(segmentDict)

            productDict['segments'] = segments
            productDict['startTime'] = event.getStartTime()
            productDict['endTime'] = event.getEndTime()
            productDicts.append(productDict)

        # If issuing, save the VTEC records for legacy products
        self._saveVTEC(self._generatedHazardEvents)

        return productDicts, self._generatedHazardEvents

    def _initializeMetaData(self, hazardEvent):
        # Address metadata overrides
        hazardEventType = hazardEvent.getHazardType()
        if hazardEventType == 'FF.A':
            self._metadata = MetaData_FF_A.MetaData()
        elif hazardEventType == 'FA.A':
            self._metadata = MetaData_FA_A.MetaData()
        elif hazardEventType == 'FL.A':
            self._metadata = MetaData_FL_A.MetaData()

        self._metadataDict = self._metadata.execute(hazardEvent=hazardEvent)
            
    # TODO Check on attributes for this product
    def _prepareSegment(self, event, vtecRecord):
        self._setProductInformation(vtecRecord, event)
        attributes = event.getHazardAttributes()

        # This creates a list of ints for the eventIDs and also formats the UGCs correctly.
        eventIDs, ugcList = self.parameterSetupForKeyInfo(list(vtecRecord.get('eventID', None)), attributes.get('ugcs', None))

        # Attributes that get skipped. They get added to the dictionary indirectly.
        noOpAttributes = ('ugcs', 'ugcPortions', 'ugcPartsOfState')
        
        phensig = vtecRecord.get('phensig')

        segment = {}
        segment['hazards'] = [{'act': vtecRecord.get('act'),
                               'phenomenon': vtecRecord.get("phen"),
                               'significance': vtecRecord.get("sig"),
                               'immediateCause' :  attributes['immediateCause'] }]
        if event.get('pointID'):
            # Add RiverForecastPoint data to the dictionary
            self._prepareRiverForecastPointData(event.get('pointID'), segment, event)
                    
        for attribute in attributes:
            # Special case attributes that need additional work before adding to the dictionary
            if attribute == 'additionalInfo':
                
                additionalInfo, citiesListFlag = self._prepareAdditionalInfo(attributes[attribute] , event)
                additionalCommentsKey = KeyInfo('additionalComments', phensig, self._productID, eventIDs, ugcList, editable=True, label='Additional Comments')
                segment[additionalCommentsKey] = additionalInfo
                segment['citiesListFlag'] = citiesListFlag
            elif attribute == 'cta':
                if vtecRecord.get("phen") != "HY":
                    callsToActionValue = self._tpc.getProductStrings(event, self._metadataDict, 'cta')
                else:
                    callsToActionValue = []

                # If the list of CTAs is a list of empty strings
                # do not make it a KeyInfo....this avoids the megawidget
                # error.
                keyInfo = False
                for cta in callsToActionValue:
                    if cta:
                        keyInfo = True
                        break
                if keyInfo:
                    callsToActionKey = KeyInfo('callsToAction', self._productCategory, self._productID, eventIDs, ugcList, True, label='Calls To Action')
                    segment[callsToActionKey] = callsToActionValue
                else:
                    segment['callsToAction'] = callsToActionValue
            elif attribute == 'eventType':
                eventTypeKey = KeyInfo('eventType', phensig, self._productID, eventIDs, ugcList, editable=True, label='Event Type')
                segment[eventTypeKey] = self._tpc.getProductStrings(event, self._metadataDict, 'eventType')
            elif attribute == 'rainAmt':
                segment['rainAmt'] = self._tpc.getProductStrings(event, self._metadataDict, 'rainAmt')
            elif attribute == 'debrisFlows':
                segment['debrisFlows'] = self._tpc.getProductStrings(event, self._metadataDict, 'debrisFlows')
            elif attribute == 'floodSeverity':
                segment['floodSeverity'] = self._tpc.getProductStrings(event, self._metadataDict, 'floodSeverity')
            elif attribute == 'floodRecord':
                segment['floodRecord'] = self._tpc.getProductStrings(event, self._metadataDict, 'floodRecord')
            elif attribute == 'pointID':
                segment['pointID'] = attributes.get(attribute, None)
                
            elif attribute in noOpAttributes:
                continue
            else:
                segment[attribute] = attributes.get(attribute, None)

        # Create Basis statement Data
        self._addBasisToSegment(segment, event, vtecRecord)

        # Create Impact statement Data
        self._addImpactToSegment(segment)
            
        segment['impactedAreas'] = self._prepareImpactedAreas(attributes)
        segment['impactedLocations'] = self._prepareImpactedLocations(event.getGeometry(), [])
        segment['geometry'] = event.getGeometry()
        segment['timeZones'] = self._productSegment.timeZones
        segment['vtecRecords'] = self._productSegment.vtecRecords
        segment['impactsStringForStageFlowTextArea'] = event.get('impactsStringForStageFlowTextArea', None)
        self._cityList(segment, event)

        return segment

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

    def _addBasisToSegment(self, segment, hazardEvent, vtecRecord):
        basisStatement = ""
        
        phensig = vtecRecord.get('phensig')  
        if phensig == "FF.A":
            hazardEventAttributes = hazardEvent.getHazardAttributes()
            basisStatement = hazardEventAttributes['basisStatement']
            if ((basisStatement is None) or (basisStatement == 'Enter basis text')):
                    basisStatement = "" 
        # elif phensig ==  == "FA.A":
        #    FA.A Does not have a Basis Statement 
        # elif phensig == "FL.A":
        #    FL.A Does not have a Basis Statement 
        segment['basisBullet'] = basisStatement
            
        
    def _addImpactToSegment(self, segment):

        impactStatement = ""
        #if phensig ==  == "FF.A":
        #    impactStatement = 'GENERATED IMPACT STATEMENT NYI'
        # elif phensig == "FA.A":
        #    FA.A Does not have an Impact Statement 
        # elif phensig == "FL.A":
        #    FL.A Does not have an Impact Statement 
        
        if ((impactStatement is None) or (impactStatement == 'Enter impact text')):
                impactStatement = "" 
        
        segment['impactStatement'] = impactStatement
        
# Allow interdependencies for the dialog's megawidgets to work.     
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    return MetaData_FFA_FLW_FLS.applyInterdependencies(triggerIdentifiers, mutableProperties)


