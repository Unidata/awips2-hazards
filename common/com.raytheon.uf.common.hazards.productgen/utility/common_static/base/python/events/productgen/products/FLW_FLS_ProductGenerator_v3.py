'''
    Description: Product Generator for the FLW and FLS products.

    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Nov 24, 2014    4937    Robert.Blum Initial creation
    Jan 31, 2015    4937    Robert.Blum General cleanup and bug fixes.
    
    @author Robert.Blum@noaa.gov
    @version 1.0
'''
import collections
from KeyInfo import KeyInfo
import HydroGenerator

# Bring in the interdependencies script from the metadata file.
import MetaData_FFA_FLW_FLS

class Product(HydroGenerator.Product):

    def __init__(self):
        ''' Hazard Types covered
             ('FA.W', 'Flood1'),  area
             ('FA.Y', 'Flood2'),  area
             ('FL.W', 'Flood3'),  point
             ('FL.Y', 'Flood4'),  point
             ('HY.S', 'Flood5'),  point
        '''
        super(Product, self).__init__()
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'FLW_FLS_ProductGenerator_v3'

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'Raytheon'
        metadata['description'] = 'Product generator for FLW_FLS.'
        metadata['version'] = '1.0'
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: dialog definition to solicit user input before running tool
        '''                  
        productSegmentGroups = self._previewProductSegmentGroups(eventSet)
        self._productLevelMetaData_dict = self._getProductLevelMetaData(self._inputHazardEvents, 'MetaData_FFA_FLW_FLS', productSegmentGroups)          
        cancel_dict = self._checkForCancel(self._inputHazardEvents, productSegmentGroups)
        dialogDict = self._organizeByProductLabel(self._productLevelMetaData_dict, cancel_dict, 'FLW_FLS_tabs')
        return dialogDict

    def _initialize(self):
        super(Product, self)._initialize()
        # This is for the VTEC Engine
        self._productCategory = 'FLW_FLS'
        self._areaName = ''
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # TODO gather this as part of the Hazard Information Dialog
        self._purgeHours = 12
        self._FLW_ProductName = 'Flood Warning'
        self._FLS_ProductName = 'Flood Statement'
        self._FLS_ProductName_Advisory = 'Flood Advisory'
        self._includeAreaNames = True
        self._includeCityNames = False

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
        self.logger.info('Start ProductGeneratorTemplate:execute FLW_FLS')

        # Extract information for execution
        self._getVariables(eventSet, dialogInputMap)
        eventSetAttributes = eventSet.getAttributes()

        if not self._inputHazardEvents:
            return []

        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._inputHazardEvents, eventSetAttributes)

        return productDicts, hazardEvents

    def _prepareSection(self, event, vtecRecord, metaData):
        attributes = event.getHazardAttributes()

        # This creates a list of ints for the eventIDs and also formats the UGCs correctly.
        eventIDs, ugcList = self.parameterSetupForKeyInfo(list(vtecRecord.get('eventID', None)), attributes.get('ugcs', None))

        # Attributes that get skipped. They get added to the dictionary indirectly.
        noOpAttributes = () # Needed for attribution / firstBullet ('ugcs', 'ugcPortions', 'ugcPartsOfState')

        section = {}
        for attribute in attributes:
            # Special case attributes that need additional work before adding to the dictionary
            if attribute == 'additionalInfo':
                additionalInfo, citiesListFlag = self._prepareAdditionalInfo(attributes[attribute] , event, metaData)
                additionalCommentsKey = KeyInfo('additionalComments', self._productCategory, self._productID, eventIDs, ugcList, True, label='Additional Comments')
                section[additionalCommentsKey] = additionalInfo
                section['citiesListFlag'] = citiesListFlag
            elif attribute == 'cta':
                if vtecRecord.get("phen") != "HY":
                    # These are now added at the segment level. Do we want to add here as well?
                    callsToActionValue = self._tpc.getProductStrings(event, metaData, 'cta')
                    section['callsToAction'] = callsToActionValue
            elif attribute in noOpAttributes:
                continue
            else:
                section[attribute] = attributes.get(attribute, None)
                # Add both the identifier and the productString for this attributes.
                # The identifiers are needed for the BasisText module.
                if attribute == 'advisoryType':
                    section[attribute + '_productString'] = self._tpc.getProductStrings(event, metaData, attribute)

        impactedLocationsKey = KeyInfo('impactedLocations', self._productCategory, self._productID, eventIDs, ugcList,True,label='Impacted Locations')
        impactedLocationsValue = self._prepareImpactedLocations(event.getGeometry())
        section[impactedLocationsKey] = impactedLocationsValue

        section['impactedAreas'] = self._prepareImpactedAreas(attributes)
        section['geometry'] = event.getGeometry()
        section['subType'] = event.getSubType()
        section['timeZones'] = self._productSegment.timeZones
        section['vtecRecord'] = vtecRecord
        if vtecRecord.get("phen") != "HY":
            # TODO this does not seem to work, causing the placeholder to be in final product.
            section['impacts'] = self._tpc.getProductStrings(event, metaData, 'impacts')
        else:
            section['impacts'] = ''
        section['endingSynopsis'] = event.get('endingSynopsis')
        section['replacedBy'] = event.get('replacedBy', None)
        section['replaces'] = event.get('replaces', None)
        section['startTime'] = event.getStartTime()
        section['endTime'] = event.getEndTime()
        section['metaData'] = metaData
        section['creationTime'] = event.getCreationTime()

        if event.get('pointID'):
            # Add RiverForecastPoint data to the dictionary
            self._prepareRiverForecastPointData(event.get('pointID'), section, event)

        self._setProductInformation(vtecRecord, event)
        return section

    def _getSegments(self, hazardEvents):
        return self._getSegments_ForPointsAndAreas(hazardEvents)
    
    def _groupSegments(self, segments):
        '''
         Group the segments into the products
            return a list of productSegmentGroup dictionaries
            
         See Pil.py for rules about how the Pil is determined.
         Although the FLW and FLS are segmented, there are rules
            about when separate products are needed.          
        '''
        productSegmentGroups = []
        if self._point_productSegments: 
            productSegmentGroups += self._createPointSegmentGroups()
        if self._area_productSegments:
            productSegmentGroups += self._createAreaSegmentGroups()
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        self._productLevelMetaData_dict = self._getProductLevelMetaData(self._inputHazardEvents, 'MetaData_FFA_FLW_FLS', productSegmentGroups)
        return productSegmentGroups

    def _createPointSegmentGroups(self):
        ''' 
        Handle Point-based segments
        '''
        productSegmentGroups = []
        # Point FLW / FLS are segmented and can contain multiple points
        # All FL.Y actions can be in one FLS
        #   TODO Allow user-defined ordering of segments to reflect geography of river.
        # FL.Y and FL.W need to be in separate FLS products
        self._point_productSegments_forFLW = []
        self._point_productSegments_forFLS = []
        self._point_productSegments_forFLS_Advisory = []
        self._point_productSegments_forHY_S = []
        for productSegment in self._point_productSegments:
            segment = productSegment.segment
            vtecRecords = productSegment.vtecRecords       
            for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                if vtecRecord.get('phenSig') == 'HY.S':
                    self._point_productSegments_forHY_S.append(self.createProductSegment(segment, vtecRecords))
                else:
                    pil = vtecRecord.get('pil')
                    if pil == 'FLW':                           
                       self._point_productSegments_forFLW.append(self.createProductSegment(segment, vtecRecords))
                    elif vtecRecord.get('sig') == 'Y':  
                        self._point_productSegments_forFLS_Advisory.append(self.createProductSegment(segment, vtecRecords))
                    else:                                      
                        self._point_productSegments_forFLS.append(self.createProductSegment(segment, vtecRecords))
                break 

        self._distribute_HY_S_segments() 
        # Make productSegmentGroups
        # Point-based FLW
        if self._point_productSegments_forFLW:
            productSegmentGroups.append(self.createProductSegmentGroup(
                'FLW', self._FLW_ProductName, 'point', self._pointVtecEngine, 'counties', False, self._point_productSegments_forFLW, formatPolygon=True))
        # Point-based FLS - FL.W
        if self._point_productSegments_forFLS:
            productSegmentGroups.append(self.createProductSegmentGroup(
                'FLS', self._FLS_ProductName,'point', self._pointVtecEngine, 'counties', False, self._point_productSegments_forFLS, formatPolygon=True))
        # Point-based FLS - FL.Y
        if self._point_productSegments_forFLS_Advisory:
            productSegmentGroups.append(self.createProductSegmentGroup(
                'FLS', self._FLS_ProductName_Advisory,'point', self._pointVtecEngine, 'counties', False, self._point_productSegments_forFLS_Advisory,
                formatPolygon=True))        
        return productSegmentGroups

    def _createAreaSegmentGroups(self):
        productSegmentGroups = []
        area_productSegments_forFLW = []
        area_productSegments_forFLS = []
        area_productSegments_forFLS_Advisory = []
        for productSegment in self._area_productSegments:
            segment = productSegment.segment
            vtecRecords = productSegment.vtecRecords
            for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                pil = vtecRecord.get('pil')
                if pil == 'FLW':
                    area_productSegments_forFLW.append(self.createProductSegment(segment, vtecRecords))
                elif vtecRecord.get('sig') == 'Y':
                    area_productSegments_forFLS_Advisory.append(self.createProductSegment(segment, vtecRecords))
                else:
                    area_productSegments_forFLS.append(self.createProductSegment(segment, vtecRecords))
                break

        # Make productSegmentGroups        
        # Area-based FLW groups
        if self._forceSingleSegmentInArealFLW():
            for area_productSegment in area_productSegments_forFLW:
                productSegmentGroups.append(self.createProductSegmentGroup(
                    'FLW', self._FLW_ProductName, 'area', self._areaVtecEngine, 'counties', False, [area_productSegment], formatPolygon=True))       
        else:
            if area_productSegments_forFLW:
                productSegmentGroups.append(self.createProductSegmentGroup(
                    'FLW', self._FLW_ProductName, 'area', self._areaVtecEngine, 'counties', False, area_productSegments_forFLW, formatPolygon=True))             

        # Area-based FLS for FL.W groups
        # Need to separate by ETN
        for productSegment in area_productSegments_forFLS:
            segment = productSegment.segment
            vtecRecords = productSegment.vtecRecords
            for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                etn = vtecRecord.get('etn')
                # See if this record matches the ETN of an existing FLS -- 
                found = False
                for segmentGroup in productSegmentGroups:
                    if segmentGroup.productID == 'FLS' and segmentGroup.etn == etn:
                        segmentGroup.addProductSegment(self.createProductSegment(segment,vtecRecords))
                        found = True
                if not found:
                    productSegmentGroups.append(self.createProductSegmentGroup(
                        'FLS', self._FLS_ProductName, 'area', self._areaVtecEngine, 'counties', False, [self.createProductSegment(segment,vtecRecords)], 
                        etn=etn, formatPolygon=True))
                break

        # Area-based FLS - FL.Y groups
        # Need to separate by ETN and also separate NEW, EXT from CAN, EXP, CON
        actionSet1 = ['NEW', 'EXT']
        actionSet2 = ['CAN', 'EXP', 'CON']
        for productSegment in area_productSegments_forFLS_Advisory:
            segment = productSegment.segment
            vtecRecords = productSegment.vtecRecords
            for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                etn = vtecRecord.get('etn')
                act = vtecRecord.get('act')
                # See if this record matches the ETN and actionSet of an existing FLS -- 
                found = False
                for segmentGroup in productSegmentGroups:
                    if segmentGroup.productID == 'FLS' \
                    and segmentGroup.etn == etn \
                    and act in segmentGroup.actions:
                        segmentGroup.addProductSegment(self.createProductSegment(segment, vtecRecords))
                        found = True
                if not found:
                    segmentGroup = self.createProductSegmentGroup(
                        'FLS', self._FLS_ProductName_Advisory, 'area', self._areaVtecEngine, 'counties', False,  [self.createProductSegment(segment, vtecRecords)],
                        etn=etn, formatPolygon=True)
                    if act in actionSet1:
                        actions = actionSet1
                    else:
                        actions = actionSet2
                    segmentGroup.actions = actions
                    productSegmentGroups.append(segmentGroup)
        return productSegmentGroups

    def _addProductParts(self, productSegmentGroup):
        geoType = productSegmentGroup.geoType
        productSegments = productSegmentGroup.productSegments
        if geoType == 'area':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_area(productSegments))
        elif geoType == 'point':
            productSegmentGroup.setProductParts(self._hydroProductParts._productParts_FFA_FLW_FLS_point(productSegments))

    def _forceSingleSegmentInArealFLW(self):
        ''' NOTE:  Although the directive allows multiple segments in the areal FLW (FA.W), 
             WarnGen in practice only allows one segment per FLW 
             For compatibility with Partners, Hazard Services has adopted the same practice
             by default.  
             To allow multiple segments per the directive, override this method and return False.
        '''
        return True

    def _distribute_HY_S_segments(self):
        '''
        Add each HY_S segment to the productSegments that contain segments for the same 'streamName'
        '''
        if not self._point_productSegments_forHY_S:
            return 
        for productSegments in [self._point_productSegments_forFLW, 
                                    self._point_productSegments_forFLS, 
                                    self._point_productSegments_forFLS_Advisory]:
            segments = [productSegment.segment for productSegment in productSegments]
            productStreamNames = self._getStreamNames(segments)
            for productSegment in self._point_productSegments_forHY_S:
                HY_S_segment = productSegment.segment
                streamNames = self._getStreamNames([HY_S_segment])
                for streamName in streamNames:
                    if streamName in productStreamNames:
                        productSegment.append(self.createProductSegment(HY_S_segment, vtecRecords))

    def _getStreamNames(self, segments):
        '''
        Determine the 'streamName's for the given segments
        '''
        hazardEvents = self.getSegmentHazardEvents(segments, self._inputHazardEvents)
        return [hazardEvent.get('streamName') for hazardEvent in hazardEvents]

    #########################################

    def executeFrom(self, dataList, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, prevDataList, True)
        return dataList

# Allow interdependencies for the dialog's megawidgets to work.     
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    return MetaData_FFA_FLW_FLS.applyInterdependencies(triggerIdentifiers, mutableProperties)

