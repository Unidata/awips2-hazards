"""
10    Description: Product Generator for the FLW and FLS products.
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
20    """

import os, types, copy, sys, json
import Legacy_ProductGenerator

class Product(Legacy_ProductGenerator.Product):
    
    def __init__(self):
        ''' Hazard Types covered
             ('FA.W', "Flood1"),  area
             ('FA.Y', "Flood2"),  area
             ('FL.W', "Flood3"),  point          
             ('FL.Y', "Flood4"),  point           
             ('HY.S', "Flood5"),  point
        '''
        super(Product, self).__init__()              
        self.metaDict = {}
        # This is for the VTEC Engine
        self._productCategory = "FLW_FLS"
        self._areaName = "" 
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # NOTE: In PV2, this will gathered as part of the Hazard Information Dialog
        self._purgeHours = 12
        self._FLW_ProductName = "Flood Warning"
        self._FLS_ProductName = "Flood Statement"
        self._FLS_ProductName_Advisory = "Flood Advisory"
        self._includeAreaNames = True
        self._includeCityNames = False

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD"
        metadata['description'] = "Product generator for FLW_FLS."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        return {}

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
        self.logger.info("Start ProductGeneratorTemplate:execute FLW_FLS")
        
        # Extract information for execution
        self._getVariables(eventSet)
        if not self._inputHazardEvents:
            return []
        # Here is the format of the dictionary that is returned for
        #  each product generated: 
        #  [
        #    {
        #     "productID": "FFA",
        #     "productDict": xmlDict,
        #     }
        #   ]
        productDicts, hazardEvents = self._makeProducts_FromHazardEvents(self._inputHazardEvents) 
        return productDicts, hazardEvents        
    
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
        if self._pointSegments: 
            productSegmentGroups += self._createPointSegmentGroups()
        if self._areaSegments:
            productSegmentGroups += self._createAreaSegmentGroups()
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
        self._pointSegments_forFLW = []
        self._pointSegments_forFLS = []
        self._pointSegments_forFLS_Advisory = []
        self._pointSegments_forHY_S = []
        for segment in self._pointSegments:
           vtecRecords = self.getVtecRecords(segment, self._pointVtecEngine)
           for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                if vtecRecord.get('phenSig') == 'HY.S':
                    self._pointSegments_forHY_S.append(segment)
                else:
                    pil = vtecRecord.get("pil")
                    if pil == "FLW":                           
                       self._pointSegments_forFLW.append(segment)
                    elif vtecRecord.get('sig') == "Y":  
                        self._pointSegments_forFLS_Advisory.append(segment)
                    else:                                      
                        self._pointSegments_forFLS.append(segment)
                break 
        
        self._distribute_HY_S_segments() 
        # Make productSegmentGroups
        # Point-based FLW
        if self._pointSegments_forFLW:
            productSegmentGroups.append(self._getProductSegmentGroup(
                "FLW", self._FLW_ProductName, "point", self._pointVtecEngine, self._pointSegments_forFLW))
        # Point-based FLS - FL.W
        if self._pointSegments_forFLS:
            productSegmentGroups.append(self._getProductSegmentGroup(
                "FLS", self._FLS_ProductName,"point", self._pointVtecEngine, self._pointSegments_forFLS))
        # Point-based FLS - FL.Y
        if self._pointSegments_forFLS_Advisory:
            productSegmentGroups.append(self._getProductSegmentGroup(
                "FLS", self._FLS_ProductName_Advisory,"point", self._pointVtecEngine, self._pointSegments_forFLS_Advisory))        
        return productSegmentGroups

    def _createAreaSegmentGroups(self):
        ''' 
        Handle Area Segments
        '''
        productSegmentGroups = []
                
        areaSegments_forFLW = []
        areaSegments_forFLS = []
        areaSegments_forFLS_Advisory = []
        for segment in self._areaSegments:
           vtecRecords = self.getVtecRecords(segment, self._areaVtecEngine)
           for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                pil = vtecRecord.get("pil")
                if pil == "FLW":                           
                    areaSegments_forFLW.append(segment)
                elif vtecRecord.get('sig') == "Y":  
                    areaSegments_forFLS_Advisory.append(segment)
                else:                                      
                    areaSegments_forFLS.append(segment)
                break              

        # Make productSegmentGroups        
        # Area-based FLW groups
        if self._forceSingleSegmentInArealFLW():
            for areaSegment in areaSegments_forFLW:
                productSegmentGroups.append(self._getProductSegmentGroup(
                    "FLW", self._FLW_ProductName, "area", self._areaVtecEngine, [areaSegment]))        
        else:
            if areaSegments_forFLW:
                productSegmentGroups.append(self._getProductSegmentGroup(
                    "FLW", self._FLW_ProductName, "area", self._areaVtecEngine, areaSegments_forFLW))       
            
        # Area-based FLS for FL.W groups
        # Need to separate by ETN
        for segment in areaSegments_forFLS:
           vtecRecords = self.getVtecRecords(segment, self._areaVtecEngine)
           for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                etn = vtecRecord.get("etn")            
                # See if this record matches the ETN of an existing FLS -- 
                found = False
                for segmentGroup in productSegmentGroups:
                    if segmentGroup.get("productID") == "FLS" and segmentGroup.get("etn") == etn:
                        segmentGroup["segments"].append(segment)
                        found = True
                if not found: 
                    productSegmentGroups.append(self._getProductSegmentGroup(
                        "FLS", self._FLS_ProductName, "area", self._areaVtecEngine, [segment], etn))        
                break
            
        # Area-based FLS - FL.Y groups
        # Need to separate by ETN and also separate NEW, EXT from CAN, EXP, CON
        actionSet1 = ["NEW", "EXT"]
        actionSet2 = ["CAN", "EXP", "CON"]
        for segment in areaSegments_forFLS_Advisory:
           vtecRecords = self.getVtecRecords(segment, self._areaVtecEngine)
           for vtecRecord in vtecRecords:  # assuming vtecRecords (sections) within each segment have the same pil, phenSig
                etn = vtecRecord.get("etn")   
                act = vtecRecord.get("act")         
                # See if this record matches the ETN and actionSet of an existing FLS -- 
                found = False
                for segmentGroup in productSegmentGroups:
                    if segmentGroup.get("productID") == "FLS" \
                    and segmentGroup.get("etn") == etn \
                    and act in segmentGroup.get("actions"):
                        segmentGroup["segments"].append(segment)
                        found = True
                if not found:
                    segmentGroup = self._getProductSegmentGroup(
                        "FLS", self._FLS_ProductName_Advisory, "area", self._areaVtecEngine, [segment], etn)        
                    if act in actionSet1:
                        actions = actionSet1
                    else:
                        actions = actionSet2
                    segmentGroup['actions'] = actions
                    productSegmentGroups.append(segmentGroup)
        return productSegmentGroups

    def _getProductSegmentGroup(self, productID, productName, geoType, vtecEngine, segments, etn=None):
        segmentGroup = { 
           "productID": productID,
           "productName": productName,
           "geoType": geoType,
           "vtecEngine": vtecEngine,
           "segments": segments,
           "mapType": "counties",
           "segmented": True,
           "formatPolygon": True,
           }
        if etn:
           segmentGroup["etn"] = etn
        return segmentGroup
                
    def _forceSingleSegmentInArealFLW(self):
        ''' NOTE:  Although the directive allows multiple segments in the areal FLW (FA.W), 
             WarnGen in practice only allowed one segment per FLW 
             For compatibility with Partners, Hazard Services has adopted the same practice
             by default.  
             To allow multiple segments per the directive, override this method and return False.
        '''
        return True
    
    def _distribute_HY_S_segments(self):
        '''
        Add each HY_S segment to the productSegments that contain segments for the same 'streamName'
        '''
        if not self._pointSegments_forHY_S:
            return 
        for productSegmentGroup in [self._pointSegments_forFLW, 
                                    self._pointSegments_forFLS, 
                                    self._pointSegments_forFLS_Advisory]:
            productStreamNames = self._getStreamNames(productSegmentGroup)
            for HY_S_segment in self._pointSegments_forHY_S:
                streamNames = self._getStreamNames([HY_S_segment])
                for streamName in streamNames:
                    if streamName in productStreamNames:
                        productSegmentGroup.append(HY_S_segment)                    
        
    def _getStreamNames(self, segments):
        '''
        Determine the 'streamName's for a segments
        '''
        hazardEvents = self.getSegmentHazardEvents(segments, self._inputHazardEvents)
        return [hazardEvent.get('streamName') for hazardEvent in hazardEvents]
            
    def getBasisPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69):
        #  Time is off of last frame of data
        try :
            eventTime = self._sessionDict["framesInfo"]["frameTimeList"][-1]
        except :
            eventTime = vtecRecord.get("startTime")            
        eventTime = self._tpc.getFormattedTime(eventTime / 1000, "%I%M %p %Z ",
                                               shiftToLocal=1, stripLeading=1).upper()
        para = "* at " + eventTime
        basis = self.getMetadataItemForEvent(hazardEvent, metaData, "basis")
        if basis is None :
            basis = " Flooding was reported"
        para += basis + " " + self.descWxLocForEvent(hazardEvent)
        motion = self.descMotionForEvent(hazardEvent)
        if motion == None :
            para += "."
        else :
            para += self.descWxLocForEvent(hazardEvent, ". THIS RAIN WAS ", \
               ". THIS STORM WAS ", ". THESE STORMS WERE ", "-")
            para += motion + "."
        return "\n" + para
    
    def getImpactsPhrase(self, vtecRecord, canVtecRecord, hazardEvent, metaData, lineLength=69):
        '''
        #* LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO CASTLE
        #  PINES...THE PINERY...SURREY RIDGE...SEDALIA...LOUVIERS...HIGHLANDS
        #  RANCH AND BEVERLY HILLS. 
        '''        
        para = "* LOCATIONS IN THE WARNING INCLUDE BUT" + \
               " ARE NOT LIMITED TO "
        para += self.getCityInfo(self._ugcs)
        return "\n" + para
    
    def executeFrom(self, dataList):
        # TODO update the issue time, VTEC ETN, etc.
        return dataList

