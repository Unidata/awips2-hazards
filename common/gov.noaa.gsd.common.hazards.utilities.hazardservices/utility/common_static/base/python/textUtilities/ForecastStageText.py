'''
    Description: Creates the forecastStageBullet text. 
    
    This module was run stand-alone by Focal Points to develop the desired logic.
    We retain this stand-alone capability so that this logic can be extended 
    by Focal Points to include multiple crests. 
    
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 2015       4375    Tracy Hansen      Initial creation
    Feb 2015       6599    Robert.Blum       Changed to new style class
    @author Tracy.L.Hansen@noaa.gov
'''
import collections, os, types
import RiverForecastPoints

class Empty:
    pass

from TextProductCommon import  TextProductCommon

class ForecastStageText(object):

    def getTestForecastStageText(self, testCase):
        print '\n*********************************'
        section = object()
        for key in testCase:
             exec 'section.'+key+' = testCase.get("'+key+'")'
             print key, testCase.get(key)
        text = self.getForecastStageText(section)
        print '\nResult:', text

    def getForecastStageText(self, section, timeZones):
        self.timeZones = timeZones
        river_description = self.getRiverDescription(section)
        forecast_description = self.getForecastDescription(section)
        bulletContent = river_description+' '+forecast_description
        return bulletContent

    def getRiverDescription(self, section):
        # Alternative --
        #  return 'The '+section.riverName
        return 'The river'
        
    def getForecastDescription(self, section):
        ''' Where are we in the cycle?
        # Determine if we are rising, falling, cresting
        # Determine if we will go above flood stage
        # Currently only one crest reported i.e. the first crest
        #   but later we can build in logic for multiple crests.  
        #   Either a more sophisticated phrase or alert user to more complex forecast e.g.
        #     'River is expected to rise and fall several times in the forecast period'
        
        # <when> <trend> <relativeTo> <timeStr - optional>
        #  is expected to rise above flood stage by Tuesday morning
        #  will continue to fall below flood stage
        #  is expected to remain below flood stage
        
        @param section -- can be a dictionary or object which must contain the RiverForecastPoint 
                          values shown below
        '''
        
        if type(section) is types.DictType or isinstance(section, collections.OrderedDict):
            section = self.createSection(section)

#         # Left in for Focal Points working on the module
#         print 'ForecastStageText Inputs'
#         print 'observedStage', section.observedStage
#         print 'floodStage', section.floodStage
#         print 'forecastCrestStage', section.forecastCrestStage
#         print 'maximumForecastStage', section.maximumForecastStage
#         print 'forecastRiseAboveFloodStageTime_ms', section.forecastRiseAboveFloodStageTime_ms
#         print 'forecastRiseAboveFloodStageTime_str', section.forecastRiseAboveFloodStageTime_str
#         print 'forecastCrestTime_str', section.forecastCrestTime_str
#         print 'forecastFallBelowFloodStageTime_ms', section.forecastFallBelowFloodStageTime_ms
#         print 'forecastFallBelowFloodStageTime_str', section.forecastFallBelowFloodStageTime_str
#         print 'maximumForecastTime_str', section.maximumForecastTime_str
#         print 'stageFlowUnits', section.stageFlowUnits
#         print 'specValue', section.specValue
#         print 'specTime', section.specTime
#         self.flush()
        
        if section.observedStage:
            compareStage = section.observedStage
        else:
            compareStage = 'first spec forecast point  e.g. 0 hours in future'
               
        timeStr = ''     
        fuzzFactor = 0.5

        '''        
        # Determine where we are starting in the cycle and describe what follows
        #   states are : 
        #     below flood stage  -- compareStage < flood stage 
        #     rising             --  compareStage < maximum, riseAbove time can be included if rising above, 
        #     above flood stage  -- compareStage > flood stage
        #     falling            -- compareStage > maximum,  fallBelow time can be included if falling below
        #
        #     at crest stage    -- can be >, = , <  flood stage
        #                          can be >, = , < maximum stage 
        #     at maximum stage  -- can be >, = , < flood stage
        #                          can be >, = , < crest stage
        
        '''

        # risingFalling -- Are we rising or falling?
        if compareStage < section.maximumForecastStage - fuzzFactor:
            self.risingFalling = 'rise'
            self.trend = 'rise'
        elif compareStage > section.maximumForecastStage + fuzzFactor:
            self.risingFalling = 'fall'
            self.trend = 'fall'
        else: # steady
            self.risingFalling = 'remain steady'
            self.trend = ['steady']
        
        # rise above / fall below flood stage
        if section.forecastRiseAboveFloodStageTime_ms and section.forecastFallBelowFloodStageTime_ms:
            if section.forecastRiseAboveFloodStageTime_ms < section.forecastFallBelowFloodStageTime_ms:
                self.firstRiseFallTime = ' above flood stage at ' + section.forecastRiseAboveFloodStageTime_str
                self.secondRiseFallTime = ', then fall below flood stage at ' + section.forecastFallBelowFloodStageTime_str + ' and continue falling'
            elif section.forecastRiseAboveFloodStageTime_ms > section.forecastFallBelowFloodStageTime_ms:
                self.firstRiseFallTime = ' below flood stage at ' + section.forecastFallBelowFloodStageTime_str
                self.secondRiseFallTime = ', then rise above flood stage at ' + section.forecastRiseAboveFloodStageTime_str + ' and continue rising'
        elif section.forecastRiseAboveFloodStageTime_ms and section.forecastFallBelowFloodStageTime_ms is None:
            self.firstRiseFallTime = ' above flood stage at ' + section.forecastRiseAboveFloodStageTime_str + ' and continue rising'
            self.secondRiseFallTime = ''
        elif section.forecastRiseAboveFloodStageTime_ms is None and section.forecastFallBelowFloodStageTime_ms:
            self.firstRiseFallTime = ' below flood stage at ' + section.forecastFallBelowFloodStageTime_str + ' and continue falling'
            self.secondRiseFallTime = ''         
        else: # both missing
            self.firstRiseFallTime = ''
            self.secondRiseFallTime = ''
                        
        # is crest = Maximum forecast stage/flow?  This is a proxy to determine if this a complex event.
        if section.forecastCrestStage !=  section.maximumForecastStage:
            self._crestToMaximum = 'not equal'
        else:
            self._crestToMaximum = 'equal'            

        # is crest above/below/at Flood stage
        if section.forecastCrestStage:
            self.crestStatement=' to a crest of ' + `section.forecastCrestStage` + ' '+section.stageFlowUnits+' at '+section.forecastCrestTime_str
        else:
            self.crestStatement=''  
        # determine final stage/flow
        if self.trend == 'rise' and section.forecastCrestStage and self._crestToMaximum != 'equal':
            self.finalStageFlow='. It will then rise to '+`section.maximumForecastStage`+' '+section.stageFlowUnits+' at '+section.maximumForecastTime_str+'. Additional rises are possible thereafter.'
        elif self.trend == 'rise' and section.forecastCrestStage is None and self._crestToMaximum != 'equal':
            self.finalStageFlow=' to '+`section.maximumForecastStage`+' '+section.stageFlowUnits+' at '+section.maximumForecastTime_str+'. Additional rises are possible thereafter.'
        elif self.trend == 'rise' and section.forecastCrestStage and self._crestToMaximum == 'equal':
            self.finalStageFlow='.'
        elif self.trend == 'fall':
            self.finalStageFlow = ''
            if section.specValue != RiverForecastPoints.Missing_Value_Constant:
                self.finalStageFlow = ' to '+`section.specValue`+' '+section.stageFlowUnits
                if section.specTime != RiverForecastPoints.Missing_Value_Constant:
                    self.finalStageFlow += ' at '+section.specTime
            self.finalStageFlow += '.'
        else: # steady
            if section.maximumForecastStage >= section.floodStage:        
                self.finalStageFlow=' above flood stage at '+`section.maximumForecastStage`+' '+section.stageFlowUnits+'.'
            else:
                self.finalStageFlow=' below flood stage at '+`section.maximumForecastStage`+' '+section.stageFlowUnits+'.'
        phrase = 'is expected to ' + self.risingFalling + self.firstRiseFallTime + self.crestStatement + self.secondRiseFallTime + self.finalStageFlow 
        return phrase
    
    def createSection(self, sectionDict):
        '''
        Interface for V3 formatters
        '''
        self.tpc = TextProductCommon()
        section = Empty()
        section.observedStage = sectionDict.get('observedStage')
        section.floodStage = sectionDict.get('floodStage') 
        section.forecastCrestStage = sectionDict.get('forecastCrestStage') 
        section.maximumForecastStage = sectionDict.get('maximumForecastStage') 
        
        section.forecastRiseAboveFloodStageTime_ms = sectionDict.get('forecastRiseAboveFloodStageTime_ms') 
        section.forecastFallBelowFloodStageTime_ms = sectionDict.get('forecastFallBelowFloodStageTime_ms')
        section.forecastCrestTime_ms = sectionDict.get('forecastCrestTime_ms')
         
        if section.forecastRiseAboveFloodStageTime_ms:
            section.forecastRiseAboveFloodStageTime_str = sectionDict.get('forecastRiseAboveFloodStageTime_str',
                  self.tpc.getFormattedTime(section.forecastRiseAboveFloodStageTime_ms, timeZones=self.timeZones))
        else:
            section.forecastRiseAboveFloodStageTime_str = None
        if section.forecastFallBelowFloodStageTime_ms:
            section.forecastFallBelowFloodStageTime_str = sectionDict.get('forecastFallBelowFloodStageTime_str',
                  self.tpc.getFormattedTime(section.forecastFallBelowFloodStageTime_ms, timeZones=self.timeZones)) 
        else:
            section.forecastFallBelowFloodStageTime_str = None
        if section.forecastCrestTime_ms:
            section.forecastCrestTime_str = sectionDict.get('forecastCrestTime_str',
                  self.tpc.getFormattedTime(section.forecastCrestTime_ms, timeZones=self.timeZones))
        else:
            section.forecastCrestTime_str = None

        section.maximumForecastTime_str = sectionDict.get('maximumForecastTime_str')
        if section.maximumForecastTime_str is None:
            maximumForecastTime_ms = sectionDict.get('maximumForecastTime_ms')
            section.maximumForecastTime_str = self.tpc.getFormattedTime(maximumForecastTime_ms, timeZones=self.timeZones) 
        section.stageFlowUnits = sectionDict.get('stageFlowUnits') 
        section.specValue =  sectionDict.get('specValue') 
        section.specTime = sectionDict.get('specTime') 
        return section
    
    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()


# This code is left in so that Focal Points wanting to override this logic can easily test their modifications to the logic.
def generateTestCases():
    t1 = collections.OrderedDict()
    t1['observedStage'] = 25.0
    t1['floodStage'] =   35.0
    t1['forecastCrestStage'] =  40.0
    t1['maximumForecastStage'] =  45.0
    t1['forecastRiseAboveFloodStageTime_ms'] =  '2014-08-12'
    t1['forecastRiseAboveFloodStageTime_str'] =  'Tuesday morning'
    t1['forecastCrestTime_str'] =  'Wednesday morning'
    t1['forecastFallBelowFloodStageTime_ms'] =  '2014-08-13'
    t1['forecastFallBelowFloodStageTime_str'] =  'Wednesday afternoon'
    t1['maximumForecastTime_str'] =  'Thursday morning'
    t1['stageFlowUnits'] =  'feet'
    t1['specValue'] = 45.0
    t1['specTime'] = 'Thursday morning'
    
    
    t2 = collections.OrderedDict()
    t2['observedStage'] = 40.0
    t2['floodStage'] =   35.0
    t2['forecastCrestStage'] = None 
    t2['maximumForecastStage'] =  35.0
    t2['forecastRiseAboveFloodStageTime_ms'] =  None
    t2['forecastRiseAboveFloodStageTime_str'] =  None
    t2['forecastCrestTime_str'] =  None
    t2['forecastFallBelowFloodStageTime_ms'] =  '2014-08-13'
    t2['forecastFallBelowFloodStageTime_str'] =  'Wednesday afternoon'
    t2['maximumForecastTime_str'] =  'Wednesday morning'
    t2['stageFlowUnits'] =  'feet'
    t2['specValue'] = 30.0
    t2['specTime'] = 'Thursday morning'
    
    t3 = collections.OrderedDict()
    t3['observedStage'] = 40.0
    t3['floodStage'] =   35.0
    t3['forecastCrestStage'] = None 
    t3['maximumForecastStage'] =  35.0
    t3['forecastRiseAboveFloodStageTime_ms'] =  None
    t3['forecastRiseAboveFloodStageTime_str'] =  None
    t3['forecastCrestTime_str'] =  None
    t3['forecastFallBelowFloodStageTime_ms'] =  None
    t3['forecastFallBelowFloodStageTime_str'] =  'Wednesday afternoon'
    t3['maximumForecastTime_str'] =  'Wednesday morning'
    t3['stageFlowUnits'] =  'feet'
    t3['specValue'] = 35.0
    t3['specTime'] = 'Thursday morning'
    
    t4 = collections.OrderedDict()
    t4['observedStage'] = 40.0
    t4['floodStage'] =   35.0
    t4['forecastCrestStage'] = None 
    t4['maximumForecastStage'] =  38.0
    t4['forecastRiseAboveFloodStageTime_ms'] =  '2014-08-13 21:00'
    t4['forecastRiseAboveFloodStageTime_str'] =  'Wednesday evening'
    t4['forecastCrestTime_str'] =  None
    t4['forecastFallBelowFloodStageTime_ms'] =  '2014-08-13 12:00'
    t4['forecastFallBelowFloodStageTime_str'] =  'Wednesday morning'
    t4['maximumForecastTime_str'] =  'Wednesday morning'
    t4['stageFlowUnits'] =  'feet'
    t4['specValue'] = 35.0
    t4['specTime'] = 'Thursday morning'
    
    t5 = collections.OrderedDict()
    t5['observedStage'] = 25.0
    t5['floodStage'] =   35.0
    t5['forecastCrestStage'] = 40.0 
    t5['maximumForecastStage'] =  40.0
    t5['forecastRiseAboveFloodStageTime_ms'] =  '2014-08-13 12:00'
    t5['forecastRiseAboveFloodStageTime_str'] =  'Wednesday morning'
    t5['forecastCrestTime_str'] =  'Wednesday evening'
    t5['forecastFallBelowFloodStageTime_ms'] =  '2014-08-14 12:00'
    t5['forecastFallBelowFloodStageTime_str'] =  'Thursday morning'
    t5['maximumForecastTime_str'] =  'Wednesday evening'
    t5['stageFlowUnits'] =  'feet'
    t5['specValue'] = 35.0
    t5['specTime'] = 'Thursday morning'
    
    t6 = collections.OrderedDict()
    t6['observedStage'] = 25.0
    t6['floodStage'] =   35.0
    t6['forecastCrestStage'] = None 
    t6['maximumForecastStage'] =  40.0
    t6['forecastRiseAboveFloodStageTime_ms'] =  '2014-08-13 12:00'
    t6['forecastRiseAboveFloodStageTime_str'] =  'Wednesday morning'
    t6['forecastCrestTime_str'] =  None
    t6['forecastFallBelowFloodStageTime_ms'] =  None
    t6['forecastFallBelowFloodStageTime_str'] =  None
    t6['maximumForecastTime_str'] =  'Thursday evening'
    t6['stageFlowUnits'] =  'feet'
    t6['specValue'] = 35.0
    t6['specTime'] = 'Thursday morning'
    
    t7 = collections.OrderedDict()
    t7['observedStage'] = 36.0
    t7['floodStage'] =   35.0
    t7['forecastCrestStage'] = None 
    t7['maximumForecastStage'] =  40.0
    t7['forecastRiseAboveFloodStageTime_ms'] =  None
    t7['forecastRiseAboveFloodStageTime_str'] =  None
    t7['forecastCrestTime_str'] =  None
    t7['forecastFallBelowFloodStageTime_ms'] =  None
    t7['forecastFallBelowFloodStageTime_str'] =  None
    t7['maximumForecastTime_str'] =  'Thursday evening'
    t7['stageFlowUnits'] =  'feet'
    t7['specValue'] = 35.0
    t7['specTime'] = 'Thursday morning'
    
    t8 = collections.OrderedDict()
    t8['observedStage'] = 36.0
    t8['floodStage'] =   35.0
    t8['forecastCrestStage'] = 40.0 
    t8['maximumForecastStage'] =  40.0
    t8['forecastRiseAboveFloodStageTime_ms'] =  '2014-08-13 12:00'
    t8['forecastRiseAboveFloodStageTime_str'] =  'Wednesday morning'
    t8['forecastCrestTime_str'] =  'Wednesday evening'
    t8['forecastFallBelowFloodStageTime_ms'] =  None
    t8['forecastFallBelowFloodStageTime_str'] =  None
    t8['maximumForecastTime_str'] =  'Wednesday evening'
    t8['stageFlowUnits'] =  'feet'
    t8['specValue'] = 35.0
    t8['specTime'] = 'Thursday morning'
    
    t9 = collections.OrderedDict()
    t9['observedStage'] = 36.0
    t9['floodStage'] =   35.0
    t9['forecastCrestStage'] = 40.0 
    t9['maximumForecastStage'] =  40.0
    t9['forecastRiseAboveFloodStageTime_ms'] =  None
    t9['forecastRiseAboveFloodStageTime_str'] =  None
    t9['forecastCrestTime_str'] =  'Wednesday evening'
    t9['forecastFallBelowFloodStageTime_ms'] =  None
    t9['forecastFallBelowFloodStageTime_str'] =  None
    t9['maximumForecastTime_str'] =  'Wednesday evening'
    t9['stageFlowUnits'] =  'feet'
    t9['specValue'] = 35.0
    t9['specTime'] = 'Thursday morning'
    
    t10 = collections.OrderedDict()
    t10['observedStage'] = 36.0
    t10['floodStage'] =   35.0
    t10['forecastCrestStage'] = None 
    t10['maximumForecastStage'] =  36.0
    t10['forecastRiseAboveFloodStageTime_ms'] =  None
    t10['forecastRiseAboveFloodStageTime_str'] =  None
    t10['forecastforecastCrestTime_str_str'] =  'Wednesday evening'
    t10['forecastFallBelowFloodStageTime_ms'] =  None
    t10['forecastFallBelowFloodStageTime_str'] =  None
    t10['maximumForecastTime_str'] =  'Wednesday evening'
    t10['stageFlowUnits'] =  'feet'
    t10['specValue'] = 36.0
    t10['specTime'] = 'Thursday morning'
    return [
      t10, 
    ]

# testCases = generateTestCases()
# for testCase in testCases:
#    ForecastStageBullet().getTestForecastStageText(testCase)
