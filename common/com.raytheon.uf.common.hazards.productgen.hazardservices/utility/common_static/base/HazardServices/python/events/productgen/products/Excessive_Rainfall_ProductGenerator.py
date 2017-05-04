"""
    Description: Excessive Rainfall Outlook
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
        self._productGeneratorName = 'Excessive_Rainfall_ProductGenerator'

    def _initialize(self) :
        super(Product, self)._initialize()
        self._productID = 'ERO'
        self._productCategory = 'Excessive_Rainfall'
        self._productName = 'Excessive_Rainfall Outlook'
        self._purgeHours = 8
        self._includeAreaNames = False
        self._includeCityNames = False

        self._vtecProduct = False

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD'
        metadata['description'] = 'Product generator for Excessive Rainfall.'
        metadata['version'] = '1.0'
        return metadata

    def defineDialog(self, eventSet):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        dialogDict = {"title": "Excessive Rainfall Product"}

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
        # Bypass for now
        return {}
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
        self.logger.info("Start ProductGeneratorTemplate:execute RVS")

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
            if phensig in ["Prob_Rainfall",'Prob_Rainfall.SeeText', 'Prob_Rainfall.Slight',
                           'Prob_Rainfall.Moderate','Prob_Rainfall.High']:
                probHazardEvents.append(hazardEvent)
                

        if not probHazardEvents:
            return [], []

        # Update this with correct hazards
        self._inputHazardEvents = probHazardEvents
        
        productDict = collections.OrderedDict()
        self._initializeProductDict(productDict, eventSetAttributes)
        productDict['productText'] = self._getDummyText()
        productDicts = [productDict]

        #productDicts, hazardEvents = self._makeProducts_FromHazardEvents(rainfallHazardEvents, eventSetAttributes)

        return productDicts, probHazardEvents
    
    def _getDummyText(self):
        return '''
EXCESSIVE RAINFALL DISCUSSION
NWS WEATHER PREDICTION CENTER COLLEGE PARK MD
951 AM EST MON NOV 30 2015

...VALID 15Z MON NOV 30 2015 - 12Z TUE DEC 01 2015...
...REFERENCE AWIPS GRAPHIC UNDER...DAY 1 EXCESSIVE RAINFALL...


SLIGHT RISK OF RAINFALL EXCEEDING FFG TO THE RIGHT OF A LINE FROM
30 NNW EKQ 25 NE LOZ 10 NW LNP 10 SSE TRI 35 NW AVL 20 NNE 4A9
10 WSW DCU 30 NNW MSL 20 WNW BNA 30 NNW EKQ.


...15Z OUTLOOK DISCUSSION...

PREVIOUS OUTLOOK AND DISCUSSION LOOK ON TRACK GIVEN THE 12Z UPPER
AIR DATA AND THE LATEST SATELLITE/RADAR IMAGERY.  NOTED THAT THE
12Z SET OF FLASH FLOOD GUIDANCE INCREASED A BIT OVER THE 06Z
GUIDANCE AT THE ONE AND THREE HOUR THRESHOLDS...BUT EVEN THESE NEW
VALUES MAY BE ATTAINABLE IF ENOUGH THE FORCING IS ABLE TO WORK IN
TANDEM WITH MODEST INSTABILITY.


BANN

...CENTRAL AND EASTERN TENNESSEE AND ADJACENT STATES...

MODERATE RAIN SHOULD BE REMARKABLY PERSISTENT THROUGH MONDAY NIGHT
OVER TENNESSEE...AND EXTENDING INTO ADJACENT PARTS OF
KY/AL/GA/NC/VA. INSTABILITY WILL REMAIN LIMITED TO NEAR ZERO...BUT
PRECIPITATION MAY BECOME WEAKLY CONVECTIVE AS DEEP LAYER ASCENT
AND LOW LEVEL CONVERGENCE STRENGTHENS MONDAY EVENING IN ADVANCE OF
THE DEVELOPING GREAT PLAINS CYCLONE. EVEN IN THE ABSENCE OF
INSTABILITY...SIX HOUR PRECIPITATION AMOUNTS HAVE BEEN RELATIVELY
IMPRESSIVE AT FIRST ORDER OBSERVATION SITES...WITH A FEW REPORTING
TOTALS GREATER THAN AN INCH...AND MESONET OBS EVEN HEAVIER. STORM
TOTALS THUS FAR HAVE EXCEEDED 1.5 TO 2 INCHES PER OBSERVATIONS AND
RADAR ESTIMATES...ESPECIALLY OVER CENTRAL AND EASTERN TENNESSEE. A
BACK EDGE OF PRECIPITATION MAY BRIEFLY MOVE THROUGH THE REGION
THIS MORNING...FOLLOWING THE MOVEMENT OF A MID LEVEL SPEED MAX.
WIDESPREAD PRECIPITATION SHOULD REDEVELOP BY MIDDAY AS WARM
ADVECTION DEVELOPS. THERE IS A SLIGHT TREND FOR THE FORECAST
24-HOUR PRECIPITATION MAXIMUM THROUGH 12Z TUESDAY TO BACK UP A BIT
TO THE NORTH AND WEST...NOW ALIGNED THROUGH CENTRAL TENNESSEE AND
SOUTHEAST KENTUCKY. THIS OWES TO AMPLIFICATION OF THE LOW LEVEL
PATTERN MONDAY NIGHT...WITH STRONGER AND MORE BACKED SOUTHERLY
FLOW DEVELOPING. THIS TREND DOES BACK THE STRONGEST FORCING OUT OF
THE TALLER TERRAIN OF THE WESTERN CAROLINAS...WHICH SHOULD CUT
DOWN ON AMOUNTS SOMEWHAT IN THAT AREA...BUT GENERALLY MOIST
SOUTHWESTERLY LOWER TO MIDDLE LEVEL FLOW WILL CONTINUE TO FAVOR
OFF AND ON RAIN.

FLASH FLOOD GUIDANCE VALUES HAD FALLEN TO AROUND 1 TO 1.5 INCHES
IN 3 HOURS OR 1.5 TO 2 INCHES IN 6 HOURS. IF FFG WERE TO DIMINISH
FURTHER...GIVEN THE ONGOING RAINFALL...THE RATES MAY BECOME
ACHIEVABLE...ESPECIALLY WITH THE STRONGER FORCING DEVELOPING LATER
MONDAY NIGHT. WPC MAINTAINED A SLIGHT RISK OF EXCESSIVE
RAINFALL...TRIMMING BACK JUST SLIGHTLY ON THE SOUTH AND EAST SIDES
OF THE RISK AREA.

BURKE
        
        
        
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
                    'ExcessiveRainfall',  self._productName, 'area', None, 'counties', False, [])) 
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

