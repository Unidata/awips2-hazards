import collections
import JUtil
from ufpy.dataaccess import DataAccessLayer
from TextProductCommon import TextProductCommon
import HazardDataAccess
from KeyInfo import KeyInfo
from Bridge import Bridge
import BaseGenerator
from HydroProductParts import HydroProductParts

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

@author Tracy.L.Hansen@noaa.gov
@version 1.0
'''

class Product(BaseGenerator.Product):

    def __init__(self) :
        ''' Hazard Types covered
             ('FF.W.Convective',     'Flood'),
             ('FF.W.NonConvective',  'Flood'), 
             ('FF.W.BurnScar',       'Flood'), 
        '''
        self._hydroProductParts = HydroProductParts()
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
        self._getVariables(eventSet)
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

    def _initializeProductDict(self, productDict, eventSetAttributes):
        siteID = eventSetAttributes['siteID']
        backupSiteID = eventSetAttributes['backupSiteID']

        productDict['productID'] = self._productID
        productDict['productName'] = self._productName
        productDict['productCategory'] = self._productCategory
        productDict['siteID'] = siteID
        productDict['backupSiteID'] = backupSiteID
        productDict['runMode'] = eventSetAttributes['runMode']
        productDict['issueFlag'] = self._issueFlag
        productDict['issueTime'] = self._issueTime

    def _prepareSegment(self, event, vtecRecord):
        self._setProductInformation(vtecRecord, event)
        attributes = event.getHazardAttributes()
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
                additionalCommentsKey = KeyInfo('additionalComments', self._productCategory, self._productID, [], '', True, label='Additional Comments')
                segment[additionalCommentsKey] = additionalInfo
                segment['citiesListFlag'] = citiesListFlag
            elif attribute == 'cta':
                callsToActionKey = KeyInfo('callsToAction', self._productCategory, self._productID, [], '', True, label='Calls To Action')
                segment[callsToActionKey] = self._tpc.getProductStrings(event, self._metadataDict, 'cta')
            elif attribute == 'eventType':
                eventTypeKey = KeyInfo('eventType', self._productCategory, self._productID, [], '', True, label='Event Type')
                segment[eventTypeKey] = self._tpc.getProductStrings(event, self._metadataDict, 'eventType')
            elif attribute == 'rainAmt':
                segment['rainAmt'] = self._tpc.getProductStrings(event, self._metadataDict, 'rainAmt', precision=2)
            elif attribute == 'debrisFlows':
                segment['debrisFlows'] = self._tpc.getProductStrings(event, self._metadataDict, 'debrisFlows')
            elif attribute in noOpAttributes:
                continue
            else:
                segment[attribute] = attributes.get(attribute, None)

        # Create basis statement
        basisText = self.basisFromHazardEvent(event)

        # Add it to the dictionary
        basisKey = KeyInfo('basis', self._productCategory, self._productID, [], '', True, label='Basis')
        segment[basisKey] = basisText

        summaryHeadlines_value, headlines, sections = self._tpc.getHeadlinesAndSections(self._productSegment.vtecRecords, self._productSegment.metaDataList, 
                                                                                        self._productID, self._issueTime_secs)
        segment['summaryHeadlines'] = summaryHeadlines_value
        segment['headlines'] = headlines
        segment['typeOfFlooding'] = self.hydrologicCauseMapping(attributes.get('hydrologicCause',None), 'typeOfFlooding') 
        segment['impactedAreas'] = self._prepareImpactedAreas(attributes)
        segment['impactedLocations'] = self._prepareImpactedLocations(event.getGeometry(), [])
        segment['geometry'] = event.getGeometry()
        segment['subType'] = event.getSubType()

        segment['vtecRecords'] = self._productSegment.vtecRecords
        segment['impactsStringForStageFlowTextArea'] = event.get('impactsStringForStageFlowTextArea', None)
        self._cityList(segment, event)

        return segment

    def _cityList(self, segmentDict, event):
        segment = self._productSegment.segment
        ids, eventIDs = segment
        cityList = []
        for city, ugcCity in self._productSegment.cityInfo:
            cityList.append(city)
        self._tpc.setVal(segmentDict, 'cityList', cityList, editable=True, label='Included Cities', eventIDs=list(eventIDs), segment=segment,
                         productCategory=self._productCategory, productID=self._productID) 

    def _prepareImpactedAreas(self, attributes):
        impactedAreas = []
        ugcs = attributes['ugcs'] 
        if 'ugcPortions' in attributes:
            portions = attributes['ugcPortions'] 
        else:
            portions = None
        if 'ugcPartsOfState' in attributes:
            partsOfState = attributes['ugcPartsOfState']   
        else:
            partsOfState = None
        for ugc in ugcs:
            area = {}
            # query countytable           
            area['ugc'] = ugc
            area['name'] = self._areaDictionary[ugc]['ugcName']
            if portions:
                area['portions'] = portions[ugc]
            area['type'] = ugc[2]
            # query state table
            area['state'] = self._areaDictionary[ugc]['fullStateName']
            area['timeZone'] = self._areaDictionary[ugc]['ugcTimeZone']
            if partsOfState:
                area['partsOfState'] = partsOfState[ugc]
            impactedAreas.append(area)
        return impactedAreas

    def _prepareImpactedLocations(self, geometry, configurations):
        impactedLocations = {} 
        # TODO Implement the configuration to set different variable names, sources, contraints, etc.
        locationsKey = KeyInfo('cityList', self._productCategory, self._productID,[], '',True,label='Impacted Locations')
        locations = self._retrievePoints(geometry, 'city')
        impactedLocations[locationsKey] = locations
        return impactedLocations

    def _prepareAdditionalInfo(self, attributeValue, event):
        additionalInfo = []
        citiesListFlag = False
        if attributeValue:
            for identifier in attributeValue:
                if identifier == 'listOfDrainages':
                    # Not sure if this query is correct
                    drainages = self._retrievePoints(event["geometry"], 'basins')
                    paraText = self._tpc.formatDelimitedList(drainages)
                    if len(paraText)==0 :
                        continue
                    productString = self._tpc.getProductStrings(event, self._metadataDict, 'additionalInfo', choiceIdentifier='listOfDrainages')
                    paraText = productString + paraText + "."
                    additionalInfo.append(paraText)
                elif identifier == 'listOfCities':
                    citiesListFlag = True
                else:
                    productString = self._tpc.getProductStrings(event, self._metadataDict, 'additionalInfo', choiceIdentifier=identifier)
                    additionalInfo.append(productString)
        return additionalInfo, citiesListFlag

    def _retrievePoints(self, geometryCollection, tablename, constraints=None, sortBy=None):
        req = DataAccessLayer.newDataRequest()
        req.setDatatype('maps')
        req.addIdentifier('table','mapdata.' + tablename)
        req.addIdentifier('geomField','the_geom')
        req.setParameters('name')
        locations = []
        for geom in geometryCollection:
            req.setEnvelope(geom.envelope)
            geometryData = DataAccessLayer.getGeometryData(req)
            for data in geometryData:
                name = data.getLocationName()
                locations.append(name)
        return locations

    def getMetadata(self):
        return self._metadata

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
            

    def hydrologicCauseMapping(self, hydrologicCause, key):
        mapping = {
            'dam':          {'immediateCause': 'DM', 'typeOfFlooding':'A dam failure in...'},
            'siteImminent': {'immediateCause': 'DM', 'typeOfFlooding':'A dam break in...'},
            'siteFailed':   {'immediateCause': 'DM', 'typeOfFlooding':'A dam break in...'},
            'levee':        {'immediateCause': 'DM', 'typeOfFlooding':'A levee failure in...'},
            'floodgate':    {'immediateCause': 'DR', 'typeOfFlooding':'A dam floodgate release in...'},
            'glacier':      {'immediateCause': 'GO', 'typeOfFlooding':'A glacier-dammed lake outburst in...'},
            'icejam':       {'immediateCause': 'IJ', 'typeOfFlooding':'An ice jam in...'},
            'snowMelt':     {'immediateCause': 'RS', 'typeOfFlooding':'Extremely rapid snowmelt in...'},
            'volcano':      {'immediateCause': 'SM', 'typeOfFlooding':'Extremely rapid snowmelt caused by volcanic eruption in...'},
            'volcanoLahar': {'immediateCause': 'SM', 'typeOfFlooding':'Volcanic induced debris flow in...'},
            'default':      {'immediateCause': 'ER', 'typeOfFlooding':'Excessive rain in...'}
            }
        if mapping.has_key(hydrologicCause):
            return mapping[hydrologicCause][key]
        else:
            return mapping['default'][key]

