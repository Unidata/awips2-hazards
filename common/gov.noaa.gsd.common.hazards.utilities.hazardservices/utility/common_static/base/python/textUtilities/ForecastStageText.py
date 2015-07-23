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
    Mar 2015       6963    Robert.Blum       Stage values have precision of 2 and 
                                             times have minutes formatted to '00'.
    Apr 2015       7271    Chris.Golden      Changed to use MISSING_VALUE
                                             constant.
    Apr 2015       7579    Robert.Blum       Updated variable names.
    May 19, 2015   6562    Chris.Cody        Implemented "work-around" code. This needs to be changed under a different issue.
    May 26, 2015   7634    Chris.Cody        Correct improper forcast bullet string generation
    Jun 25, 2015   ????    Chris.Cody        Correct for missing first and second Rise Fall Time error
    Jun 25, 2015    8313   Benjamin.Phillippe Fixed issued event loading when time is changed
    Jul 23, 2015    9643   Robert.Blum       All rounding is now done in one common place so that it can 
                                             be easily overridden by sites.
    @author Tracy.L.Hansen@noaa.gov
'''
import collections, os, types
from HazardConstants import MISSING_VALUE

MISSING_VALUE_STRING = str(MISSING_VALUE)

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

    def getForecastStageText(self, hazard, timeZones):
        self.timeZones = timeZones
        river_description = self.getRiverDescription(hazard)
        forecast_description = self.getForecastDescription(hazard)
        bulletContent = river_description+' '+forecast_description
        return bulletContent

    def getRiverDescription(self, hazard):
        # Alternative --
        #  return 'The '+ hazard.get('riverName_RiverName')
        return 'The river'
        
    def getForecastDescription(self, hazard):
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
        
        @param hazard -- can be a dictionary or object which must contain the RiverForecastPoint 
                          values shown below
        '''

        if type(hazard) is types.DictType or isinstance(hazard, collections.OrderedDict):
            hazard = self.createHazard(hazard)

#         # Left in for Focal Points working on the module
#         print 'ForecastStageText Inputs'
#         print 'observedStage', hazard.observedStage
#         print 'floodStage', hazard.floodStage
#         print 'forecastCrestStage', hazard.forecastCrestStage
#         print 'maximumForecastStage', hazard.maximumForecastStage
#         print 'forecastRiseAboveFloodStageTime_ms', hazard.forecastRiseAboveFloodStageTime_ms
#         print 'forecastRiseAboveFloodStageTime_str', hazard.forecastRiseAboveFloodStageTime_str
#         print 'forecastCrestTime_str', hazard.forecastCrestTime_str
#         print 'forecastFallBelowFloodStageTime_ms', hazard.forecastFallBelowFloodStageTime_ms
#         print 'forecastFallBelowFloodStageTime_str', hazard.forecastFallBelowFloodStageTime_str
#         print 'maximumForecastTime_str', hazard.maximumForecastTime_str
#         print 'stageFlowUnits', hazard.stageFlowUnits
#         print 'specValue', hazard.specValue
#         print 'specTime', hazard.specTime
#         self.flush()
        
        if hazard.observedStage:
            compareStage = hazard.observedStage
        else:
            #'first spec forecast point  e.g. 0 hours in future'
            compareStage = MISSING_VALUE
               
        timeStr = ''     
        fuzzFactor = 0.5

        # String variables for the rounded stage values
        if hazard.forecastCrestStage:
            forecastCrestStage = self.tpc.roundFloat(hazard.forecastCrestStage, returnString=True)
        else:
            forecastCrestStage = None
            
        if hazard.maximumForecastStage:
            maximumForecastStage = self.tpc.roundFloat(hazard.maximumForecastStage, returnString=True)
        else:
            maximumForecastStage = None
            
        if hazard.specValue and hazard.specValue != MISSING_VALUE_STRING:
            specValue = self.tpc.roundFloat(hazard.specValue, returnString=True)
        else:
            specValue = None
            

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
        if compareStage != MISSING_VALUE:
            if hazard.maximumForecastStage and compareStage < hazard.maximumForecastStage - fuzzFactor:
                self.risingFalling = 'rise'
                self.trend = 'rise'
            elif hazard.maximumForecastStage and compareStage > hazard.maximumForecastStage + fuzzFactor:
                self.risingFalling = 'fall'
                self.trend = 'fall'
            else: # steady
                self.risingFalling = 'remain steady'
                self.trend = 'steady'
        else: # steady
            self.risingFalling = 'remain steady'
            self.trend = 'steady'

        self.firstRiseFallTime = ''
        self.secondRiseFallTime = ''
        # rise above / fall below flood stage
        if hazard.forecastRiseAboveFloodStageTime_ms and hazard.forecastFallBelowFloodStageTime_ms:
            if hazard.forecastRiseAboveFloodStageTime_ms < hazard.forecastFallBelowFloodStageTime_ms:
                self.firstRiseFallTime = ' above flood stage at ' + hazard.forecastRiseAboveFloodStageTime_str
                self.secondRiseFallTime = ', then fall below flood stage at ' + hazard.forecastFallBelowFloodStageTime_str + ' and continue falling'
            elif hazard.forecastRiseAboveFloodStageTime_ms > hazard.forecastFallBelowFloodStageTime_ms:
                self.firstRiseFallTime = ' below flood stage at ' + hazard.forecastFallBelowFloodStageTime_str
                self.secondRiseFallTime = ', then rise above flood stage at ' + hazard.forecastRiseAboveFloodStageTime_str + ' and continue rising'
            else: # both missing
                self.firstRiseFallTime = ''
                self.secondRiseFallTime = ''
        elif hazard.forecastRiseAboveFloodStageTime_ms and (hazard.forecastFallBelowFloodStageTime_ms is None or hazard.forecastFallBelowFloodStageTime_ms == 0): 
            self.firstRiseFallTime = ' above flood stage at ' + hazard.forecastRiseAboveFloodStageTime_str + ' and continue rising'
            self.secondRiseFallTime = ''
        elif (hazard.forecastRiseAboveFloodStageTime_ms is None or hazard.forecastRiseAboveFloodStageTime_ms == 0) and hazard.forecastFallBelowFloodStageTime_ms:
            self.firstRiseFallTime = ' below flood stage at ' + hazard.forecastFallBelowFloodStageTime_str + ' and continue falling'
            self.secondRiseFallTime = ''         
        else: # both missing
            self.firstRiseFallTime = ''
            self.secondRiseFallTime = ''
                        
        # is crest = Maximum forecast stage/flow?  This is a proxy to determine if this a complex event.
        if hazard.forecastCrestStage !=  hazard.maximumForecastStage:
            self._crestToMaximum = 'not equal'
        else:
            self._crestToMaximum = 'equal'            

        # is crest above/below/at Flood stage
        if hazard.forecastCrestStage:
            self.crestStatement=' to a crest of ' + forecastCrestStage + ' '+hazard.stageFlowUnits+' at '+hazard.forecastCrestTime_str
        else:
            self.crestStatement=''  
        # determine final stage/flow
        if self.trend == 'rise' and hazard.forecastCrestStage and self._crestToMaximum != 'equal':
            self.finalStageFlow='. It will then rise to '+maximumForecastStage+' '+hazard.stageFlowUnits+' at '+hazard.maximumForecastTime_str+'. Additional rises are possible thereafter.'
        elif self.trend == 'rise' and hazard.forecastCrestStage is None and self._crestToMaximum != 'equal':
            self.finalStageFlow=' to '+maximumForecastStage+' '+hazard.stageFlowUnits+' at '+hazard.maximumForecastTime_str+'. Additional rises are possible thereafter.'
        elif self.trend == 'rise' and hazard.forecastCrestStage and self._crestToMaximum == 'equal':
            self.finalStageFlow='.'
        elif self.trend == 'fall':
            self.finalStageFlow = ''
            if hazard.specValue != MISSING_VALUE_STRING:
                self.finalStageFlow = ' to '+ specValue + ' '+hazard.stageFlowUnits
                if hazard.specTime != MISSING_VALUE:
                    self.finalStageFlow += ' at '+ hazard.specTime
            self.finalStageFlow += '.'
        else: # steady
            if maximumForecastStage is not None:
                if hazard.maximumForecastStage >= hazard.floodStage:        
                    self.finalStageFlow=' above flood stage at '+maximumForecastStage+' '+hazard.stageFlowUnits+'.'
                elif hazard.maximumForecastStage != MISSING_VALUE:
                    self.finalStageFlow=' below flood stage at '+maximumForecastStage+' '+hazard.stageFlowUnits+'.'
                else:
                    self.finalStageFlow='.'
            else:
                self.finalStageFlow='.'
        phrase = 'is expected to ' + self.risingFalling + self.firstRiseFallTime + self.crestStatement + self.secondRiseFallTime + self.finalStageFlow 
        return phrase
    
    def createHazard(self, hazardDict):
        '''
        Interface for V3 formatters
        '''
        self.tpc = TextProductCommon()
        hazard = Empty()
        hazard.observedStage = hazardDict.get('observedStage')
        hazard.floodStage = hazardDict.get('floodStage') 
        hazard.forecastCrestStage = hazardDict.get('forecastCrestStage') 
        if hazard.forecastCrestStage and hazard.forecastCrestStage == MISSING_VALUE:
            hazard.forecastCrestStage = None
            
        hazard.maximumForecastStage = hazardDict.get('maximumForecastStage') 
        
        hazard.forecastRiseAboveFloodStageTime_ms = hazardDict.get('forecastRiseAboveFloodStageTime_ms') 
        hazard.forecastFallBelowFloodStageTime_ms = hazardDict.get('forecastFallBelowFloodStageTime_ms')
        hazard.forecastCrestTime_ms = hazardDict.get('forecastCrestTime_ms')
         
        if hazard.forecastRiseAboveFloodStageTime_ms:
            hazard.forecastRiseAboveFloodStageTime_str = hazardDict.get('forecastRiseAboveFloodStageTime_str',
                  self.tpc.getFormattedTime(hazard.forecastRiseAboveFloodStageTime_ms, format='%I00 %p %Z %a %b %d %Y',
                                            timeZones=self.timeZones))
        else:
            hazard.forecastRiseAboveFloodStageTime_str = None
        if hazard.forecastFallBelowFloodStageTime_ms:
            hazard.forecastFallBelowFloodStageTime_str = hazardDict.get('forecastFallBelowFloodStageTime_str',
                  self.tpc.getFormattedTime(hazard.forecastFallBelowFloodStageTime_ms, format='%I00 %p %Z %a %b %d %Y',
                                            timeZones=self.timeZones)) 
        else:
            hazard.forecastFallBelowFloodStageTime_str = None
        if hazard.forecastCrestTime_ms and hazard.forecastCrestTime_ms != MISSING_VALUE:
            hazard.forecastCrestTime_str = hazardDict.get('forecastCrestTime_str',
                  self.tpc.getFormattedTime(hazard.forecastCrestTime_ms, format='%I00 %p %Z %a %b %d %Y',
                                            timeZones=self.timeZones))
        else:
            hazard.forecastCrestTime_str = None

        hazard.maximumForecastTime_str = hazardDict.get('maximumForecastTime_str')
        if hazard.maximumForecastTime_str is None:
            maximumForecastTime_ms = hazardDict.get('maximumForecastTime_ms')
            if maximumForecastTime_ms:
                hazard.maximumForecastTime_str = self.tpc.getFormattedTime(maximumForecastTime_ms,format='%I00 %p %Z %a %b %d %Y',
                                                                        timeZones=self.timeZones)
                  
        hazard.stageFlowUnits = hazardDict.get('stageFlowUnits') 
        hazard.specValue =  hazardDict.get('specValue') 
        hazard.specTime = hazardDict.get('specTime') 
        return hazard
    
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
