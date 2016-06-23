"""
    Description: Probabilistic Hazard Information
"""
import collections, time
import Prob_Generator
import HazardDataAccess
#from PHI_GridRecommender import Recommender as PHI_GridRecommender
import TimeUtils
from ProbUtils import ProbUtils
import json, pickle, os
import datetime

class Product(Prob_Generator.Product):

    def __init__(self) :
        ''' Hazard Types covered
        '''
        super(Product, self).__init__()

        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'Prob_Convective_ProductGenerator'

    def _initialize(self) :
        super(Product, self)._initialize()
        self._productID = 'Probabilistic Hazard Information'
        self._productCategory = 'Probabilistic Hazard Information'
        self._productName = 'Probabilistic Hazard Information Outlook'
        self._purgeHours = 8
        self._includeAreaNames = False
        self._includeCityNames = False

        self._vtecProduct = False
        self._probUtils = ProbUtils()

    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = 'GSD'
        metadata['description'] = 'Product generator for Probabilistic Hazard Information.'
        metadata['version'] = '1.0'
        return metadata

    def defineDialog(self, eventSet):
        """
        @return: dialog definition to solicit user input before running tool
        """  
        dialogDict = {"title": "Probabilistic Hazard Information"}

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

#         headlineStatement = {
#              "fieldType": "Text",
#              "fieldName": "headlineStatement",
#              "expandHorizontally": True,
#              "visibleChars": 25,
#              "lines": 1,
#              "values": "|* Enter Headline Statement *|",
#             } 
# 
#         narrativeInformation = {
#              "fieldType": "Text",
#              "fieldName": "narrativeInformation",
#              "expandHorizontally": True,
#              "visibleChars": 25,
#              "lines": 25,
#              "values": "|* Enter Narrative Information *|",
#            } 

#         fieldDicts = [headlineStatement, narrativeInformation]
#         dialogDict["metadata"] = fieldDicts
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
            if phensig in ["Prob_Tornado",'Prob_Severe']:
                probHazardEvents.append(hazardEvent)                

        if not probHazardEvents:
            return [], []

        self._inputHazardEvents = probHazardEvents
        productDicts = []
        for hazardEvent in probHazardEvents:
            self._WFO = 'XXX'
            hazardEvent.set('issueTime', self._issueTime)
            
            ### Capture discussion & threats, timestamp and place in readonly disc box
            newDisc = datetime.datetime.utcfromtimestamp(self._issueTime/1000).strftime('%m/%d/%Y %H:%M :: ')
            threatFields = ['convectiveStormCharsHail', 'convectiveStormCharsWind', 'convectiveStormCharsTorn']
            threats = [hazardEvent.get(threat) for threat in threatFields]
            newDisc += str(threats) + '  >>>  ' 
            thisDisc = hazardEvent.get('convectiveWarningDecisionDiscussion', '')
            if len(thisDisc) > 0:
                thisDiscEntries = thisDisc.split('\n---\n')
                thisDiscEntries[0] = newDisc + thisDiscEntries[0]
                thisDiscEntries = [''] + thisDiscEntries
                newDisc = '\n---\n'.join(thisDiscEntries)
                
            else:
                newDisc += '\n---\n'
                
            ### Reset for next time HID appears
            [hazardEvent.set(threat, None) for threat in threatFields]
            hazardEvent.set('convectiveWarningDecisionDiscussion', newDisc)
            
            hazardEvent.set('eventStartTimeAtIssuance', long(TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getStartTime())))
            hazardEvent.set('durationSecsAtIssuance', self._probUtils._getDurationSecs(hazardEvent))
            hazardEvent.set('graphProbsAtIssuance', hazardEvent.get('convectiveProbTrendGraph'))
            # Set expire time. This should coincide with the zero probability.
            # TO DO --convert hazard end time to millis
            hazardEvent.set('expirationTime', int(TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getEndTime())))
            hazardAttrs = hazardEvent.getHazardAttributes()
            self._objectID = hazardEvent.get('objectID') if hazardEvent.get('objectID') else hazardEvent.getDisplayEventID()
            self._userOwned = hazardEvent.get('userOwned', False)
            self._percentage = hazardEvent.get('probabilities', '54')
            # Convert to a string
            self._direction = self._convertDirection(hazardEvent.get('convectiveObjectDir', 270))
            # Convert to mph
            self._speed = round(hazardAttrs.get('convectiveObjectSpdKts', 32)  * 1.15)
            st =  TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getStartTime())
            et =  TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getEndTime())
            self._startTime = self._timeFormat(st)
            self._endTime = self._timeFormat(et)
            self._headline = hazardAttrs.get('headline', 'Probabilistic Severe')
            self._location = self._getLocation(hazardEvent)
            defaultDiscussion = ''' Mesocyclone interacted with line producing brief spin-up. Not confident in enduring tornadoes...but more brief spinups are possible as more interactions occur.'''
            self._discussion = thisDisc #hazardAttrs.get('convectiveWarningDecisionDiscussion', defaultDiscussion)            
            #print "Prob Convective PG hazardAttrs", hazardAttrs
            #print "    start, end time, issueTime", hazardEvent.getStartTime(), hazardEvent.getEndTime(), hazardEvent.get('issueTime')
            if hazardEvent.getStatus() in ['ENDING']:
                print "Prob Convective Product Generator setting status to Ended", hazardEvent.getEventID()
                self.flush()
                hazardEvent.setStatus('ENDED')
                           
            productDict = collections.OrderedDict()
            self._initializeProductDict(productDict, eventSetAttributes)
            productDict['productText'] =  self._getText()
            productDict['issueTime'] = self._issueTime
            productDicts.append(productDict)
            
        if self._issueFlag:

#===============================================================================
#             siteID = eventSet.getAttributes().get('siteID')
#             mode = eventSet.getAttributes().get('hazardMode', 'PRACTICE').upper()
#             databaseEvents = HazardDataAccess.getHazardEventsBySite(siteID, mode)
#             filteredDBEvents = []
# 
#             thisEventSetIDs = [evt.getEventID() for evt in eventSet]
#             print 'Prob_Convective Product Generator -- DBEvents:'
#             for evt in databaseEvents:
#                 if evt.getStatus().lower() in ["elapsed", "ending", "ended"]:
#                     continue
#                 if evt.getEventID() not in thisEventSetIDs:
#                   filteredDBEvents.append(evt)  
# 
#             eventSet.addAll(filteredDBEvents)
#             eventSet.addAttribute('issueTime', datetime.datetime.utcfromtimestamp(self._issueTime/1000))
# 
# 
#             pu = ProbUtils()
#             
#             pu.processEvents(eventSet, writeToFile=True)
#===============================================================================
            pu = ProbUtils()

            ## Dump just this event to disk since only one hazard in events set?
            attrKeys = ['site', 'status', 'phenomenon', 'significance', 'subtype',
                        'creationtime', 'endtime', 'starttime', 'geometry', 'eventid',
                        'username', 'workstation', 'attributes']
            outDict = {}
            for hazardEvent in probHazardEvents:
                outDict = {k:hazardEvent.__getitem__(k) for k in attrKeys}

                filename = outDict.get('phenomenon') + '_' + outDict.get('eventid') + '_' + str(self._issueTime)
                OUTPUTDIR = os.path.join(pu.getOutputDir(), 'IssuedEventsPickle')
                if not os.path.exists(OUTPUTDIR):
                    try:
                        os.makedirs(OUTPUTDIR)
                    except:
                        sys.stderr.write('Could not create PHI grids output directory:' +OUTPUTDIR+ '.  No output written')

                pickle.dump( outDict, open(OUTPUTDIR+'/'+filename, 'wb'))
                
        #productDicts, hazardEvents = self._makeProducts_FromHazardEvents(probHazardEvents, eventSetAttributes)

        return productDicts, probHazardEvents

    def _getText(self):
        fcst =  '''
        Probabilistic Hazard Information Bulletin        
        '''
        fcst = fcst + '''
        WHAT:  ''' + self._headline + '    '   + `self._percentage` + '%'
        fcst = fcst + '''
           Thread ID: ''' + self._objectID + '  User-Owned Threat: ' + str(self._userOwned)
        fcst = fcst + '''
           
        WHEN: 
            Start: ''' + self._startTime + '''
            End:  ''' + self._endTime + '''
        
        WHERE: 
            '''+self._location
      
        fcst = fcst + '''
            Moving '''+self._direction + ''' at ''' + `self._speed` + ''' mph
            WFO: ''' + self._WFO + '''            
        '''
        fcst = fcst + '''
        DISCUSSION: ''' + self._discussion
        return fcst
    
    def _timeFormat(self, inputTime):
        format='%I:%M %p %Z %d %a %b, %Y'
        return self._tpc.getFormattedTime(inputTime, format)
        #return '7:05 pm Thu May 7th, 2015'
                 
    def _convertDirection(self, inputDirection):
         return self.dirTo16PtText(inputDirection)

    def _getLocation(self, hazardEvent):
        ugcs = hazardEvent.get('ugcs')
        return str(ugcs)          
#         # Get hazard geometry
#         
#         #Here's the query we'd be going for:
#         #  SELECT countyname FROM mapdata.county WHERE the_geom && ST_SetSrid('BOX3D(-96.963120 41.692394, -96.508260 42.128302)'::box3d,4326)
#
#         geometry = hazardEvent.getGeometry()
#         columns = ['countyname']
#         print "Prob_Convective_ProductGenerator", geometry
#         self.flush()
#         counties = self.mapDataQuery('county', columns, geometry=geometry, intersect=True)
#         print "counties", counties
#         self.flush()
#         # Find cwa's that overlap it
#         columns = ['wfo']
#         cwas = self.mapDataQuery('cwa', columns, geometry=geometry, intersect=True)
#         print "cwas", cwas
#         self.flush()
#         return 'North of MyTown'        


    ### From GFE TextUtils.py
    def dirTo16PtText(self, numDir):
        "Convert the numerical direction to a string: N, NE, E, ..."
        dirList = self.dir16PtList()
        for dir in dirList:
            if numDir >= dir[1] and numDir < dir[2]:
                return dir[0]
        print "WARNING -- illegal direction for conversion: ", numDir
        return None

    def dir16PtList(self):
        dirSpan = 22.5 # 22.5 degrees per direction
        base = 11.25 # start with N
        return [
            ('N',   360-base,          361),
            ('N',   0,                 base),
            ('NNE', base            ,  base + 1*dirSpan),
            ('NE',  base + 1*dirSpan,  base + 2*dirSpan),
            ('ENE', base + 2*dirSpan,  base + 3*dirSpan),
            ('E',   base + 3*dirSpan,  base + 4*dirSpan),
            ('ESE', base + 4*dirSpan,  base + 5*dirSpan),
            ('SE',  base + 5*dirSpan,  base + 6*dirSpan),
            ('SSE', base + 6*dirSpan,  base + 7*dirSpan),
            ('S',   base + 7*dirSpan,  base + 8*dirSpan),
            ('SSW', base + 8*dirSpan,  base + 9*dirSpan),
            ('SW',  base + 9*dirSpan,  base + 10*dirSpan),
            ('WSW', base + 10*dirSpan,  base + 11*dirSpan),
            ('W',   base + 11*dirSpan,  base + 12*dirSpan),
            ('WNW', base + 12*dirSpan,  base + 13*dirSpan),
            ('NW',  base + 13*dirSpan,  base + 14*dirSpan),
            ('NNW', base + 14*dirSpan,  base + 15*dirSpan),
            ]

    
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

