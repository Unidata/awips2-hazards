"""
10    Description: Product Generator for the FFW and FFS products.
12    
13    SOFTWARE HISTORY
14    Date         Ticket#    Engineer    Description
15    ------------ ---------- ----------- --------------------------
16    April 5, 2013            Tracy.L.Hansen      Initial creation
17    
18    @author Tracy.L.Hansen@noaa.gov
19    @version 1.0
20    """

import os, types, copy, sys, json
import ProductTemplate

class Product(ProductTemplate.Product):
    
    def __init__(self):
        super(Product, self).__init__()              
        self.metaDict = {}
        # This is for the VTEC Engine
        self._productCategory = "FFW_FFS"
        self._wmoID = "WGUS63"
        self._areaName = "" 
        # Does the product use zones or counties for areal hazards
        self._areaUgcType = 'counties'
        # Does the product use zones or counties for point hazards
        self._pointUgcType = 'counties'
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # NOTE: In PV2, this will gathered as part of the Hazard Information Dialog
        self._purgeHours = -1
        self._FFW_ProductName = "FLASH FLOOD WARNING"
        self._FFS_ProductName = "FLASH FLOOD STATEMENT"
        self._includeAreaNames = True
        self._includeCityNames = False

    def getScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD"
        metadata['description'] = "Product generator for FFW_FFS."
        metadata['version'] = "1.0"
        return metadata
       
    def defineDialog(self):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        return {}

    def execute(self, hazardEventSet):          
        '''
        Inputs:
        @param hazardEventSet: a list of hazard events (eventDicts) plus
                               a map of additional variables
        @return productDicts: Each execution of a generator can produce 1 or more 
             products from the set of hazard events
             For each product, a productID and one dictionary is returned as input for 
             the desired formatters.
        '''
        self.logger.info("Start ProductGeneratorTemplate:execute FFW_FFS")
        
        # Extract information for execution
        self._getVariables(hazardEventSet)
        if not self._eventDicts:
            return []
        # Here is the format of the dictionary that is returned for
        #  each product generated: 
        #  [
        #    {
        #     "productID": "FFA",
        #     "productDict": xmlDict,
        #     }
        #   ]
        productDicts = self._makeProducts_FromHazardEvents(self._eventDicts) 
        return productDicts        

    def _groupSegments(self, segments):
        '''
         Group the segments into the products
            return a list of tuples (productInfo, segments)
        
         Check the VTEC for each segment
          IF NEW, EXT -- make a new FFW
          Else CON, CAN:
             Group them into FFS's with same ETN
        '''
        # For short fused areal products, 
        #     we can safely make the assumption of only one hazard/action per segment.
        productSegmentGroups = []
        for segment in segments:
            hazardList = self._vtecEngine.getHazardList(segment)
            for hazard in hazardList:  # NOTE there is only one hazard to process
                action = hazard.get("act")
                etn = hazard.get("etn")
                if action in ["NEW", "EXT"]:
                    # Create new FFW
                    productSegmentGroup = { 
                       "productID" : "FFW",
                       "productName": self._FFW_ProductName,
                       "geoType": "area",
                       "vtecEngine": self._vtecEngine,
                       "mapType": "counties",
                       "segmented": True, #False,
                       "etn":etn,
                       "formatPolygon": True,
                       "segments": [segment]
                       }
                    productSegmentGroups.append(productSegmentGroup)
                else:
                    # See if this record matches the ETN of an existing FFS
                    found = False
                    for productSegmentGroup in productSegmentGroups:
                        if productSegmentGroup.get("productID") == "FFS" and productSegmentGroup.get("etn") == etn:
                            productSegmentGroup["segments"].append(segment)
                            found = True
                    if not found:
                        # Make a new FFS productSegmentGroup
                       productSegmentGroup = {
                            "productID" : "FFS",
                            "productName": self._FFS_ProductName,
                            "geoType": "area",
                            "vtecEngine": self._vtecEngine,
                            "mapType": "counties",
                            "segmented": True,
                            "etn":etn,
                            "formatPolygon": True,
                            "segments": [segment],
                        }
                       productSegmentGroups.append(productSegmentGroup)
        return productSegmentGroups


    def getBasisPhrase(self, hazard, canHazard, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69):
        #  Time is off of last frame of data
        try :
            eventTime = self._sessionDict["framesInfo"]["frameTimeList"][-1]
        except :
            eventTime = hazard.get("startTime")            
        eventTime = self._tpc.getFormattedTime(eventTime/1000, "%I%M %p %Z ", 
                                               shiftToLocal=1, stripLeading=1).upper()
        para = "* at "+eventTime
        basis = self.getMetadataItemForEvent(self.segmentEventDict, self.metaData,  "basis")
        if basis is None :
            basis = "Flash Flooding was reported"
        else :
            basis = " Flash Flooding"
        para += basis + " " + self.descWxLocForEvent(self.segmentEventDict)
        motion = self.descMotionForEvent(self.segmentEventDict)
        if motion == None :
            para += "."
        else :
            para += self.descWxLocForEvent(eventDict, ". THIS RAIN WAS ", \
               ". THIS STORM WAS ", ". THESE STORMS WERE ", "-")
            para += motion+"."
        return "\n"+para, None, None, None
    
    def getImpactsPhrase(self, multRecords, impact, hazard, canHazard, areaPhrase, eventType, eventDict, metaDataList, 
                    creationTime, testMode, wfoCity, lineLength=69 ):
        '''
        #* LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO CASTLE
        #  PINES...THE PINERY...SURREY RIDGE...SEDALIA...LOUVIERS...HIGHLANDS
        #  RANCH AND BEVERLY HILLS. 
        '''     
        para = "* LOCATIONS IN THE WARNING INCLUDE BUT" + \
               " ARE NOT LIMITED TO "
        para += self.getCityInfo(eventDict.get("ugcs"))
        return "\n"+para


