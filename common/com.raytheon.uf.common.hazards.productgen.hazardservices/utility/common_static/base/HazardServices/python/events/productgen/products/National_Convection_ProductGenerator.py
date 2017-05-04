"""
    Description: Convective Outlook
"""
import collections
import Prob_Generator
import HazardDataAccess

class Product(Prob_Generator.Product):

    def __init__(self) :
        ''' Hazard Types covered
        '''
        super(Product, self).__init__()

        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'National_Convection_ProductGenerator'

    def _initialize(self) :
        super(Product, self)._initialize()
        self._productID = 'Convection'
        self._productCategory = 'Convection'
        self._productName = 'Convective Outlook'
        self._purgeHours = 8
        self._includeAreaNames = False
        self._includeCityNames = False

        self._vtecProduct = False

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD'
        metadata['description'] = 'Product generator for National Convection.'
        metadata['version'] = '1.0'
        return metadata

    def defineDialog(self, eventSet):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        dialogDict = {"title": "Convective Outlook"}

#         floodPointTableDict = {
#             "fieldType": "CheckBoxes", 
#             "fieldName": "floodPointTable",
#             "choices": [{'displayString':'Include Flood Point Table', 'identifier': 'include'}],
#             "defaultValues": 'include',
#             "values": 'include',
#             }
# 
#         selectedHazardsDict = {
#             "fieldType": "RadioButtons", 
#             "fieldName": "selectedHazards",
#             "label": "Hazard Events",
#             "choices": ["Use selected set of hazards", "Report on all hazards"]
#             }

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


    def executeFrom(self, dataList, keyInfo=None):
        if keyInfo is not None:
            dataList = self.correctProduct(dataList, keyInfo, False)
        return dataList

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
        self.logger.info("Start ProductGeneratorTemplate:execute Convective")

        whichEvents = dialogInputMap.get('selectedHazards')
        hazardEvents = eventSet.getEvents()
        eventSetAttributes = eventSet.getAttributes()

        # Extract information for execution
        self._getVariables(eventSet, dialogInputMap)


        probHazardEvents = []
        for hazardEvent in hazardEvents:
            phen = hazardEvent.getPhenomenon()
            sig = hazardEvent.getSignificance()
            if sig is not None:
                phensig = phen + '.' + sig
            else:
                phensig = phen
            if phensig in ["Prob_Convection",'Prob_Convection.Thunderstorms', 'Prob_Convection.Marginal',
                           'Prob_Convection.Slight','Prob_Convection.Enhanced','Prob_Convection.Moderate',
                           'Prob_Convection.High']:
                probHazardEvents.append(hazardEvent)
                

        if not probHazardEvents:
            return [], []

        # Update this with correct hazards
        self._inputHazardEvents = probHazardEvents
                
        productDict = collections.OrderedDict()
        self._initializeProductDict(productDict, eventSetAttributes)
        productDict['productText'] = self._getDummyText()
        productDicts = [productDict]

        #productDicts, hazardEvents = self._makeProducts_FromHazardEvents(probHazardEvents, eventSetAttributes)

        return productDicts, probHazardEvents
    
    def _getDummyText(self):
        return '''
        SPC AC 020058

   DAY 1 CONVECTIVE OUTLOOK  
   NWS STORM PREDICTION CENTER NORMAN OK
   0658 PM CST SUN NOV 01 2015

   VALID 020100Z - 021200Z

   ...THERE IS A SLGT RISK OF SVR TSTMS ACROSS PARTS OF THE CNTRL GULF
   COAST AND VICINITY...

   ...THERE IS A MRGL RISK OF SVR TSTMS SURROUNDING THE SLGT ACROSS
   PARTS OF THE SOUTHEAST...

   ...SUMMARY...
   THUNDERSTORMS CAPABLE OF A COUPLE OF TORNADOES AND ISOLATED DAMAGING
   WIND GUSTS MAY AFFECT PARTS OF THE CENTRAL GULF COAST AND VICINITY
   OVERNIGHT TONIGHT INTO EARLY MONDAY MORNING.

   ...PORTIONS OF THE CNTRL GULF COAST AND VICINITY...

   WATER VAPOR LOOPS INDICATE A STRONG MID/UPPER-LEVEL PV MAX ADVANCING
   EWD OVER PARTS OF ERN TX...PRESENTLY APPROACHING THE WRN EDGE OF A
   WARM CONVEYOR STRUCTURE DRAPED ACROSS THE SERN STATES. THE
   INTERACTION OF THE BACKGROUND CIRCULATION WITH THE WARM CONVEYOR IS
   RESULTING IN A BAROCLINIC LEAF STRUCTURE EVIDENT OVER PARTS OF THE
   LOWER MS VALLEY AND TN VALLEY TO THE MIDDLE/WRN GULF...SIGNALING THE
   INTENSIFICATION OF LARGE-SCALE BAROCLINICITY OVER THE REGION. WARM
   AND MOIST AIR -- E.G. SFC DEWPOINTS IN THE LOWER 70S -- IS EVIDENT S
   OF A FRONT ANALYZED FROM SWRN GA TO THE WRN FL PANHANDLE TO ABOUT 70
   NM OFF THE COAST OF LA.

   AS LOW-LEVEL MASS FIELDS INVOF THE FRONT RESPOND TO THE APPROACH OF
   THE PV MAX...THE FRONT IS EXPECTED TO SLOWLY ADVANCE NWD IN THE WAKE
   OF EVENING PRECIPITATION. A FEW SFC OBSERVATIONS OFF THE COAST OF
   SERN LA/SWRN AL ALREADY INDICATE SOME TEMPORAL VEERING OF THE
   FLOW...AFFIRMING THIS NOTION. WHILE STATICALLY STABLE CONDITIONS
   PRESENTLY EXIST INLAND...THE AFOREMENTIONED PROCESSES SHOULD FOSTER
   NON-ZERO BUOYANCY -- ALBEIT QUITE MARGINAL -- EXTENDING INLAND FROM
   THE COAST DURING THE OVERNIGHT HOURS.

   STRENGTHENING LOW-LEVEL ASCENT AHEAD OF A SFC CYCLONE DEVELOPING
   NEWD ALONG THE FRONT WILL LIKELY CONTRIBUTE TO A BAND OF 
   CONVECTION -- WITH EMBEDDED ROTATING CONVECTIVE ELEMENTS -- GIVEN 60
   KT OF DEEP SHEAR ALREADY SAMPLED BY THE LIX 00Z RAOB. LOW-LEVEL
   SHEAR SHOULD BE SUFFICIENT TO SUPPORT STORM-SCALE CIRCULATIONS
   CAPABLE OF A COUPLE OF TORNADOES AND ISOLATED DMGG WIND GUSTS. THE
   SLGT-RISK AREA -- ASSOCIATED WITH 5-PERCENT TORNADO PROBABILITIES
   DURING THE OVERNIGHT HOURS INTO EARLY MONDAY MORNING -- HAS BEEN
   SHIFTED WWD. THIS IS TO ACCOUNT FOR THE LATEST OBSERVATIONAL TRENDS
   AND DEPICTIONS FROM HIGH-RESOLUTION NUMERICAL MODEL GUIDANCE
   REGARDING THE MOST LIKELY CORRIDOR FOR THIS LOW-CAPE/HIGH-SHEAR SVR
   RISK.

   ..COHEN.. 11/02/2015

   CLICK TO GET WUUS01 PTSDY1 PRODUCT

   NOTE: THE NEXT DAY 1 OUTLOOK IS SCHEDULED BY 0600Z
   '''

    def _getSegments(self, hazardEvents):
        '''
        @param hazardEvents: list of Hazard Events
        @return a list of segments for the hazard events
        '''
        return self._getSegments_ForPointsAndAreas(hazardEvents)

    def _groupSegments(self, segments):
        '''
        Products do not have segments, so create a productSegmentGroup with no segments. 
        '''        
        productSegmentGroups = []
        productSegmentGroups.append(self.createProductSegmentGroup(
                    'ProbProduct',  self._productName, 'area', None, 'counties', False, [])) 
        for productSegmentGroup in productSegmentGroups:
            self._addProductParts(productSegmentGroup)
        return productSegmentGroups

    def _addProductParts(self, productSegmentGroup):
        productSegments = productSegmentGroup.productSegments
        productSegmentGroup.setProductParts(self._hydroProductParts._productParts_RVS(productSegments))

    def _createProductLevelProductDictionaryData(self, productDict):
        hazardEventsList = self._generatedHazardEvents
        if hazardEventsList is not None:
            hazardEventDicts = []
            for hazardEvent in hazardEventsList:
                metaData = self.getHazardMetaData(hazardEvent)
                hazardEventDict = self._createHazardEventDictionary(hazardEvent, {}, metaData)
                hazardEventDicts.append(hazardEventDict)
            productDict['hazardEvents'] = hazardEventDicts

        ugcs = []
        eventIDs = []
        for hazardEvent in self._generatedHazardEvents:
            ugcs.extend(hazardEvent.get('ugcs'))
            eventIDs.append(hazardEvent.getEventID())
        productDict['ugcs'] = ugcs
        productDict['eventIDs'] = eventIDs
        timezones = self._tpc.hazardTimeZones(ugcs)
        productDict['timezones'] = timezones

        expireTime = self._tpc.getExpireTime(self._issueTime, self._purgeHours, [], fixedExpire=True)
        productDict['expireTime'] = expireTime
        productDict['issueTime'] = self._issueTime

    def _createAndAddSegmentsToDictionary(self, productDict, productSegmentGroup):
        pass
    
    def _headlineStatement(self, productDict, productSegmentGroup, arguments=None):        
        productDict['headlineStatement'] =  self._dialogInputMap.get('headlineStatement')

    def _narrativeInformation(self, productDict, productSegmentGroup, arguments=None):        
        productDict['narrativeInformation'] =  self._dialogInputMap.get('narrativeInformation')

