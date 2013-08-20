"""
10    Description: Product Generator for the FLW and FLS products.
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
        self._productCategory = "FLW_FLS"
        self._areaName = "" 
        # Does the product use zones or counties for areal hazards
        self._areaUgcType = 'counties'
        # Does the product use zones or counties for point hazards
        self._pointUgcType = 'counties'
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        # NOTE: In PV2, this will gathered as part of the Hazard Information Dialog
        self._purgeHours = -1
        self._FLW_ProductName = "Flood Warning"
        self._FLS_ProductName = "Flood Statement"
        self._includeAreaNames = True
        self._includeCityNames = False

    def getScriptMetadata(self):
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
        self.logger.info("Start ProductGeneratorTemplate:execute FLW_FLS")
        
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
    
    def _getSegments(self, eventDicts):
        return self._getSegments_ForPointsAndAreas(eventDicts)

    def _groupSegments(self, segments):
        '''
         Group the segments into the products
            return a list of tuples (productInfo, segments)
         
         Check the pil 
          IF FLW -- make a new FLW -- there can only be one segment per FLW
          Group the segments into FLS products with same ETN
          
        '''
        productSegmentGroups = []
        for segment in segments:
            segmentEventDict = self.getSegmentEventDicts(self._eventDicts, [segment])[0]
            geoType = segmentEventDict.get("geoType")
            vtecRecords = self.getVtecRecords(segment)
            for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord to process
                pil = vtecRecord.get("pil")
                etn = vtecRecord.get("etn")
                if pil == "FLW":
                    # Create new FLW
                    productSegmentGroup = { 
                       "productID" : "FLW",
                       "productName": self._FLW_ProductName,
                       "geoType": geoType,
                       "vtecEngine": self._vtecEngine,
                       "mapType": "counties",
                       "segmented": False,
                       "etn":etn,
                       "formatPolygon": True,
                       "segments": [segment]
                       }
                    productSegmentGroups.append(productSegmentGroup)
                else: # FLS
                    # See if this record matches the ETN of an existing FLS
                    found = False
                    for productSegmentGroup in productSegmentGroups:
                        if productSegmentGroup.get("productID") == "FLS" and productSegmentGroup.get("etn") == etn:
                            productSegmentGroup["segments"].append(segment)
                            found = True
                    if not found:
                        # Make a new FLS productSegmentGroup
                       productSegmentGroup = {
                            "productID" : pil,
                            "productName": self._FLW_ProductName,
                            "geoType": geoType,
                            "vtecEngine": self._vtecEngine,
                            "mapType": "counties",
                            "segmented": True,
                            "etn":etn,
                            "formatPolygon": True,
                            "segments": [segment],
                        }
                       productSegmentGroups.append(productSegmentGroup)
        return productSegmentGroups


    def getBasisPhrase(self, vtecRecord, canVtecRecord, eventDict, metaData, lineLength=69):
        #  Time is off of last frame of data
        try :
            eventTime = self._sessionDict["framesInfo"]["frameTimeList"][-1]
        except :
            eventTime = vtecRecord.get("startTime")            
        eventTime = self._tpc.getFormattedTime(eventTime/1000, "%I%M %p %Z ", 
                                               shiftToLocal=1, stripLeading=1).upper()
        para = "* at "+eventTime
        basis = self.getMetadataItemForEvent(eventDict, metaData,  "basis")
        if basis is None :
            basis = " Flooding was reported"
        para += basis + " " + self.descWxLocForEvent(eventDict)
        motion = self.descMotionForEvent(eventDict)
        if motion == None :
            para += "."
        else :
            para += self.descWxLocForEvent(eventDict, ". THIS RAIN WAS ", \
               ". THIS STORM WAS ", ". THESE STORMS WERE ", "-")
            para += motion+"."
        return "\n"+para
    
    def getImpactsPhrase(self, vtecRecord, canVtecRecord, eventDict, metaData, lineLength=69 ):
        '''
        #* LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO CASTLE
        #  PINES...THE PINERY...SURREY RIDGE...SEDALIA...LOUVIERS...HIGHLANDS
        #  RANCH AND BEVERLY HILLS. 
        '''        
        para = "* LOCATIONS IN THE WARNING INCLUDE BUT" + \
               " ARE NOT LIMITED TO"
        para += self.getCityInfo(self._ugcs)
        return "\n"+para

