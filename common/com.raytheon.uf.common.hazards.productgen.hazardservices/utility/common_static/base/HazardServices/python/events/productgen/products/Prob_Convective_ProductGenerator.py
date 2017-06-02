"""
    Description: Probabilistic Hazard Information
"""
import collections, time
import Prob_Generator
import HazardDataAccess
#from PHI_GridRecommender import Recommender as PHI_GridRecommender
import TimeUtils
from ProbUtils import ProbUtils
from VisualFeatures import VisualFeatures
import json, pickle, os, sys
import datetime

class Product(Prob_Generator.Product):

    def __init__(self) :
        ''' Hazard Types covered
        '''
        super(Product, self).__init__()

        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self.productGeneratorName = 'Prob_Convective_ProductGenerator'

    def initialize(self) :
        super(Product, self)._initialize()
        self._productID = 'Probabilistic Hazard Information'
        self._productCategory = 'Probabilistic Hazard Information'
        self._productName = 'Probabilistic Hazard Information Outlook'
        self._purgeHours = 8
        self._includeAreaNames = False
        self._includeCityNames = False

        self._vtecProduct = False
        self.probUtils = ProbUtils()

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

        return dialogDict


    def executeFrom(self, dataList, eventSet, keyInfo=None):
        if keyInfo is not None:
            # TODO: Hydro product generators pass eventSet to the correctProduct() method.
            # Should the Prob_Generator be augmented to take eventSet as well?
            dataList = self.correctProduct(dataList, keyInfo, False)
        # TODO: Should there be an "else: self.updateExpireTimes(dataList)" here as there
        # is in Hydro-based generators? The latter is not an existing method in the
        # superclass, but is in Legacy_Base_Generator.

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
        self.initialize()
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

        self.inputHazardEvents = probHazardEvents
        productDicts = []
        for hazardEvent in probHazardEvents:
            self.WFO = 'XXX'
            hazardEvent.set('issueTime', self._issueTime)
            
            ### Capture discussion & threats, timestamp and place in readonly disc box
            newDisc = datetime.datetime.utcfromtimestamp(self._issueTime/1000).strftime('%m/%d/%Y %H:%M :: ')
            threatFields = ['convectiveStormCharsWind', 'convectiveStormCharsHail', 'convectiveStormCharsTorn']
            threats = [hazardEvent.get(threat) for threat in threatFields]
            newDisc += str(threats) + '  >>> \n' + hazardEvent.get('convectiveWarningDecisionDiscussion', '')
            newDisc += '\n---\n'

            prevDisc = newDisc + hazardEvent.get('convectivePastWarningDecisionDiscussion', '')
            

            ### Reset for next time HID appears
            #[hazardEvent.set(threat, None) for threat in threatFields]
            hazardEvent.set('convectivePastWarningDecisionDiscussion', prevDisc)
            hazardEvent.set('convectiveWarningDecisionDiscussion', '')
            
            hazardEvent.set('eventStartTimeAtIssuance', long(TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getStartTime())))
            hazardEvent.set('durationSecsAtIssuance', self.probUtils.getDurationSecs(hazardEvent))
            hazardEvent.set('graphProbsAtIssuance', hazardEvent.get('convectiveProbTrendGraph'))
            # Set expire time. This should coincide with the zero probability.
            # TO DO --convert hazard end time to millis
            hazardEvent.set('expirationTime', int(TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getEndTime())))
            hazardAttrs = hazardEvent.getHazardAttributes()
            
            self.automationLevel = hazardEvent.get('automationLevel', 'userOwned')
            self.percentage = hazardEvent.get('probabilities', '54')
            # Convert to a string
            self.direction = self.convertDirection(hazardEvent.get('convectiveObjectDir', 270))
            # Convert to mph
            self.speed = round(hazardAttrs.get('convectiveObjectSpdKts', 32)  * 1.15)
            st =  TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getStartTime())
            et =  TimeUtils.datetimeToEpochTimeMillis(hazardEvent.getEndTime())
            self.startTime = self.timeFormat(st)
            self.endTime = self.timeFormat(et)
            self.headline = hazardAttrs.get('headline', 'Probabilistic Severe')
            self.location = self.getLocation(hazardEvent)
            defaultDiscussion = ''' Mesocyclone interacted with line producing brief spin-up. Not confident in enduring tornadoes...but more brief spinups are possible as more interactions occur.'''
            self.discussion = prevDisc 
            if hazardEvent.getStatus() in ['ENDING']:
                print "Prob Convective Product Generator setting status to Ended", hazardEvent.getEventID()
                self.flush()
                hazardEvent.setStatus('ENDED')
                hazardEvent.set('statusForHiddenField', 'ENDED')
                           
            
            ### Requested by Greg:
            ### "There should be an 'M' appended to the ID of any object that is wholly or partially manual"
            hazardObjectID = hazardEvent.get('objectID', hazardEvent.getDisplayEventID())
            if hazardObjectID != 'automated':
                if not hazardObjectID.startswith('M'):
                    hazardEvent.set('objectID', 'M'+hazardObjectID) 
            self.objectID = hazardEvent.get('objectID')

            productDict = collections.OrderedDict()
            self._initializeProductDict(productDict, eventSetAttributes)
            productDict['productText'] =  self.getText()
            productDict['issueTime'] = self._issueTime
            productDicts.append(productDict)
            
            if self._issueFlag:
                self.cleanupHazard(hazardEvent)            
            
        if self._issueFlag:            
            self.storeIssuedHazards(probHazardEvents)                
     
        #productDicts, hazardEvents = self.makeProducts_FromHazardEvents(probHazardEvents, eventSetAttributes)

        return productDicts, probHazardEvents

    def getText(self):
        fcst =  '''
        Probabilistic Hazard Information Bulletin        
        '''
        fcst = fcst + '''
        WHAT:  ''' + self.headline + '    '   + `self.percentage` + '%'
        fcst = fcst + '''
           Thread ID: ''' + self.objectID + '  Automation Level: ' + str(self.automationLevel)
        fcst = fcst + '''
           
        WHEN: 
            Start: ''' + self.startTime + '''
            End:  ''' + self.endTime + '''
        
        WHERE: 
            '''+self.location
      
        fcst = fcst + '''
            Moving '''+self.direction + ''' at ''' + `self.speed` + ''' mph
            WFO: ''' + self.WFO + '''            
        '''
        fcst = fcst + '''
        DISCUSSION: ''' + self.discussion
        return fcst
    
    def timeFormat(self, inputTime):
        format='%I:%M %p %Z %d %a %b, %Y'
        return self._tpc.getFormattedTime(inputTime, format)
        #return '7:05 pm Thu May 7th, 2015'
                 
    def convertDirection(self, inputDirection):
         return self.dirTo16PtText(inputDirection)

    def getLocation(self, hazardEvent):
        ugcs = hazardEvent.get('ugcs')
        return str(ugcs)          
#         # Get hazard geometry
#         
#         #Here's the query we'd be going for:
#         #  SELECT countyname FROM mapdata.county WHERE the_geom && ST_SetSrid('BOX3D(-96.963120 41.692394, -96.508260 42.128302)'::box3d,4326)
#
#         geometry = hazardEvent.getFlattenedGeometry()
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
            ('SW',  base + 9*dirSpan,  base + 10*dirSpan),        # Remove motion vector polygons
            ('WSW', base + 10*dirSpan,  base + 11*dirSpan),
            ('W',   base + 11*dirSpan,  base + 12*dirSpan),
            ('WNW', base + 12*dirSpan,  base + 13*dirSpan),
            ('NW',  base + 13*dirSpan,  base + 14*dirSpan),
            ('NNW', base + 14*dirSpan,  base + 15*dirSpan),
            ]

    def cleanupHazard(self, hazardEvent):
        # Remove previous visual features
        visualFeatures = hazardEvent.getVisualFeatures()
        newFeatures = []                            
        for visualFeature in visualFeatures:
            if not 'previous' in str(visualFeature.get('identifier')):
                newFeatures.append(visualFeature)
        hazardEvent.setVisualFeatures(VisualFeatures(newFeatures))
        # Set editableObject and settingMotionVector to False
        hazardEvent.set('editableObject', False)
        hazardEvent.set('settingMotionVector', False)
        hazardEvent.set('upstreamPolys', [])
        hazardEvent.set('statusForHiddenField', 'ISSUED')
        hazardEvent.set('dataLayerStatus', '')
                
    def storeIssuedHazards(self,probHazardEvents):
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
    
    def getSegments(self, hazardEvents):
        '''
        @param hazardEvents: list of Hazard Events
        @return a list of segments for the hazard events
        '''
        return self.getSegments_ForPointsAndAreas(hazardEvents)

    def groupSegments(self, segments):
        '''
        Products do not have segments, so create a productSegmentGroup with no segments. 
        '''        
        productSegmentGroups = []
        productSegmentGroups.append(self.createProductSegmentGroup(
                    'ProbProduct',  self._productName, 'area', None, 'counties', False, [])) 
        for productSegmentGroup in productSegmentGroups:
            self.addProductParts(productSegmentGroup)
        return productSegmentGroups

    def addProductParts(self, productSegmentGroup):
        productSegments = productSegmentGroup.productSegments
        productSegmentGroup.setProductParts(self.hydroProductParts.productParts_RVS(productSegments))

    def createProductLevelProductDictionaryData(self, productDict):
        hazardEventsList = self.generatedHazardEvents
        if hazardEventsList is not None:
            hazardEventDicts = []
            for hazardEvent in hazardEventsList:
                metaData = self.getHazardMetaData(hazardEvent)
                hazardEventDict = self.createHazardEventDictionary(hazardEvent, {}, metaData)
                hazardEventDicts.append(hazardEventDict)
            productDict['hazardEvents'] = hazardEventDicts

        ugcs = []
        eventIDs = []
        for hazardEvent in self.generatedHazardEvents:
            ugcs.extend(hazardEvent.get('ugcs'))
            eventIDs.append(hazardEvent.getEventID())
        productDict['ugcs'] = ugcs
        productDict['eventIDs'] = eventIDs
        timezones = self._tpc.hazardTimeZones(ugcs)
        productDict['timezones'] = timezones

        expireTime = self._tpc.getExpireTime(self._issueTime, self._purgeHours, [], fixedExpire=True)
        productDict['expireTime'] = expireTime
        productDict['issueTime'] = self._issueTime

    def createAndAddSegmentsToDictionary(self, productDict, productSegmentGroup):
        pass
    
    def headlineStatement(self, productDict, productSegmentGroup, arguments=None):        
        productDict['headlineStatement'] =  self.dialogInputMap.get('headlineStatement')

    def narrativeInformation(self, productDict, productSegmentGroup, arguments=None):        
        productDict['narrativeInformation'] =  self.dialogInputMap.get('narrativeInformation')

