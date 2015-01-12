import collections
from KeyInfo import KeyInfo
import HydroGenerator

# Import all the metaData for hazards covered by this generator
# TODO Address metadata overrides
import MetaData_FF_W_Convective
import MetaData_FF_W_NonConvective
import MetaData_FF_W_BurnScar

'''
Description: Product Generator for the FFW and FFS products.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
April 5, 2013            Tracy.L.Hansen      Initial creation
Nov      2013  2368      Tracy.L.Hansen      Changing from eventDicts to hazardEvents, simplifying product
                                             dictionary
Oct 24, 2014   4933      Robert.Blum         Implement Product Generation Framework v3
Dec 18, 2014   4933      Robert.Blum         Fixing issue with rebase conflict that was missed in previous checkin.
Jan 12, 2015   4937      Robert.Blum         Refactor to use new generator class hierarchy 
                                             introduced with ticket 4937.

@author Tracy.L.Hansen@noaa.gov
@version 1.0
'''

class Product(HydroGenerator.Product):

    def __init__(self) :
        ''' Hazard Types covered
             ('FF.W.Convective',     'Flood'),
             ('FF.W.NonConvective',  'Flood'), 
             ('FF.W.BurnScar',       'Flood'), 
        '''
        super(Product, self).__init__()

        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'FFW_FFS_ProductGenerator_v3'

    def _initialize(self) :
        super(Product, self)._initialize()
        self._productID = 'FFW'
        self._productCategory = 'FFW_FFS'
        self._productName = 'Flash Flood Warning'
        self._purgeHours = -1
        self._FFW_ProductName = 'Flash Flood Warning'
        self._FFS_ProductName = 'Flash Flood Statement'
        self._includeAreaNames = True
        self._includeCityNames = False

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'Raytheon'
        metadata['description'] = 'Content generator for flash flood warning.'
        metadata['version'] = '1.0'
        return metadata

    def defineDialog(self, eventSet):
        return {}

    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, True)
        return dataList

    def execute(self, eventSet, dialogInputMap):
        self._initialize()

        self.logger.info('Start ProductGeneratorTemplate:execute FFW_FFS_v3')

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

            # Init the productDict
            productDict = collections.OrderedDict()
            self._initializeProductDict(productDict, eventSetAttributes)

            # Add productParts to the dictionary
            productParts = productSegmentGroup.productParts
            productDict['productParts'] = productParts

            # Add dialogInputMap entries to the product dictionary
            for key in dialogInputMap.keys():
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

    def _preProcessHazardEvents(self, hazardEvents):        
        '''        
        Set Immediate Cause for FF.W.NonConvective prior to VTEC processing        
        '''
        for hazardEvent in hazardEvents:
            if hazardEvent.getHazardType() == 'FF.W.NonConvective':
                immediateCause = self.hydrologicCauseMapping(hazardEvent.get('hydrologicCause'), 'immediateCause')
                hazardEvent.set('immediateCause', immediateCause)
                
    def _initializeMetaData(self, event):
        # TODO Address metadata overrides
        subType = event.getSubType()
        if subType == 'NonConvective':
            self._metadata = MetaData_FF_W_NonConvective.MetaData()
        elif subType == 'Convective':
            self._metadata = MetaData_FF_W_Convective.MetaData()
        else:
            self._metadata = MetaData_FF_W_BurnScar.MetaData()
        self._metadataDict = self._metadata.execute(hazardEvent=event)

    def _prepareSegment(self, event, vtecRecord):
        self._setProductInformation(vtecRecord, event)
        attributes = event.getHazardAttributes()

        # This creates a list of ints for the eventIDs and also formats the UGCs correctly.
        eventIDs, ugcList = self.parameterSetupForKeyInfo(list(vtecRecord.get('eventID', None)), attributes.get('ugcs', None))

        # Attributes that get skipped. They get added to the dictionary indirectly.
        noOpAttributes = ('ugcs', 'ugcPortions', 'ugcPartsOfState')

        segment = {}
        segment['hazards'] = [{'act': vtecRecord.get('act'),
                               'phenomenon': vtecRecord.get("phen"),
                               'significance': vtecRecord.get("sig")}]
        for attribute in attributes:
            # Special case attributes that need additional work before adding to the dictionary
            if attribute == 'additionalInfo':
                additionalInfo, citiesListFlag = self._prepareAdditionalInfo(attributes[attribute] , event)
                additionalCommentsKey = KeyInfo('additionalComments', self._productCategory, self._productID, eventIDs, ugcList, editable=True, label='Additional Comments')
                segment[additionalCommentsKey] = additionalInfo
                segment['citiesListFlag'] = citiesListFlag
            elif attribute == 'cta':
                callsToActionKey = KeyInfo('callsToAction', self._productCategory, self._productID, eventIDs, ugcList, editable=True, label='Calls To Action')
                segment[callsToActionKey] = self._tpc.getProductStrings(event, self._metadataDict, 'cta')
            elif attribute == 'eventType':
                eventTypeKey = KeyInfo('eventType', self._productCategory, self._productID, eventIDs, ugcList, editable=True, label='Event Type')
                segment[eventTypeKey] = self._tpc.getProductStrings(event, self._metadataDict, 'eventType')
            elif attribute == 'rainAmt':
                segment['rainAmt'] = self._tpc.getProductStrings(event, self._metadataDict, 'rainAmt')
            elif attribute == 'debrisFlows':
                segment['debrisFlows'] = self._tpc.getProductStrings(event, self._metadataDict, 'debrisFlows')
            elif attribute in noOpAttributes:
                continue
            else:
                segment[attribute] = attributes.get(attribute, None)

        # Create basis statement
        basisText = self.basisFromHazardEvent(event)

        # Add it to the dictionary
        basisKey = KeyInfo('basisBullet', self._productCategory, self._productID, eventIDs, ugcList, editable=True, label='Basis')
        segment[basisKey] = basisText

        segment['typeOfFlooding'] = self.hydrologicCauseMapping(attributes.get('hydrologicCause',None), 'typeOfFlooding') 
        segment['impactedAreas'] = self._prepareImpactedAreas(attributes)
        segment['impactedLocations'] = self._prepareImpactedLocations(event.getGeometry(), [])
        segment['geometry'] = event.getGeometry()
        segment['subType'] = event.getSubType()
        segment['timeZones'] = self._productSegment.timeZones
        segment['vtecRecords'] = self._productSegment.vtecRecords
        segment['impactsStringForStageFlowTextArea'] = event.get('impactsStringForStageFlowTextArea', None)
        self._cityList(segment, event)

        return segment

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

