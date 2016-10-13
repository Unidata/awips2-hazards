'''
Swath recommender for probabilistic hazard types.
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import TimeUtils
import AdvancedGeometry
from VisualFeatures import VisualFeatures
from ProbUtils import ProbUtils
import logging, UFStatusHandler
import matplotlib.pyplot as plt
import LogUtils

import math, time
from math import *
import shapely
import shapely.ops as so
import shapely.geometry as sg
import shapely.affinity as sa
from inspect import currentframe, getframeinfo
import os, sys
from java.util import Date
import numpy as np


import JUtil     

#===============================================================================
# Users wanting to add new storm paths should enter a new method in this class
# with the same name as one of the choices found in 
# CommonMetaData.py:_getConvectiveSwathPresets()
# and using the same arguments to the new method in this class as the existing
# methods.
#
# For example, if I want to add a new path named 'foo', I would add 'foo'
# as a choice in CommonMetaData.py:_getConvectiveSwathPresets()
# and a method below as:
#
# def foo(self, speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs):
#
# which would do some calculations based on the values passed in and
# then return a dictionary:
# 
#        returnDict = {
#                      'speedVal':speedVal, 
#                      'dirVal':dirVal, 
#                      'spdUVal':spdUVal,
#                      'dirUVal':dirUVal
#                      }
#
# 
#===============================================================================
class SwathPreset(object):
    def __init__(self):
        pass
    
    def NoPreset(self, speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs):
        returnDict = {
                      'speedVal':speedVal,
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def RightTurningSupercell(self, speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs): 
        spdWt = 0.75
        dirWtTup = (0.,30.)
        dirWt = dirWtTup[1] * secs / totalSecs
        dirVal = dirVal + dirWt
        speedVal =  speedVal * spdWt
      
        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def LeftTurningSupercell(self, speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs): 
        spdWt = 0.75
        dirWtTup = (0.,30.)
        dirWt = -1. * dirWtTup[1] * secs / totalSecs
        dirVal = dirVal + dirWt
        speedVal = speedVal * spdWt
        
        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def BroadSwath(self, speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs):
        spdUValWtTup = (0.,15)
        dirUValWtTup = (0.,40)
        spdUValWt = spdUValWtTup[1] * secs / totalSecs
        spdUVal = spdUVal + (spdUValWtTup[1] - spdUValWt)
        dirUValWt = dirUValWtTup[1] * secs / totalSecs
        dirUVal = dirUVal + (dirUValWtTup[1] - dirUValWt)

        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def LightBulbSwath(self, speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs):
        spdUValWtTup = (0.,7)
        dirUValWtTup = (0.,20)
        spdUValWt = spdUValWtTup[1] * secs / totalSecs
        spdUVal = spdUVal + (spdUValWtTup[0] + spdUValWt)
        dirUValWt = dirUValWtTup[1] * secs / totalSecs
        dirUVal = dirUVal + (dirUValWtTup[0] + dirUValWt)

        returnDict = {
                      'speedVal':speedVal, 
                      'dirVal':dirVal, 
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
         
class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('SwathRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'SwathRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        self._probUtils = ProbUtils()
        
    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Swath Recommender'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['includeEventTypes'] = [ "Prob_Severe", "Prob_Tornado" ]
        metadata['onlyIncludeTriggerEvent'] = True
        metadata['includeDataLayerTimes'] = True
        
        # This tells Hazard Services to not notify the user when the recommender
        # creates no hazard events. Since this recommender is to be run in response
        # to hazard event changes, etc. it would be extremely annoying for the user
        # to be constantly dismissing the warning message dialog if no hazard events
        # were being created. 
        metadata['background'] = True
        
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: A dialog definition to solicit user input before running tool
        '''   
        return None
            
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        
        Please see this Google Doc for the SwathRecommender Design:
        https://docs.google.com/document/d/1Ry9Es0bZheazBnkTkpKUbnzpHejKomwrv_z-fIFji1k/edit
        
        Runs the Swath Recommender tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param visualFeatures: Visual features as defined by the
                               defineSpatialInfo() method and
                               modified by the user to provide
                               spatial input; ignored.
        
        @return: A list of potential probabilistic hazard events. 
        '''                
        self._setPrintFlags()
        self._printEventSet("\nRunning SwathRecommender", eventSet, eventLevel=1)
                 
        eventSetAttrs = eventSet.getAttributes()
        trigger = eventSetAttrs.get('trigger')
        origin = eventSetAttrs.get('origin')
        self._currentTime = long(eventSetAttrs.get("currentTime"))        
        self._setDataLayerTimes(eventSetAttrs)                
        self._attributeIdentifiers = eventSetAttrs.get('attributeIdentifiers', [])

        resultEventSet = EventSetFactory.createEventSet(None)
                
        for event in eventSet:
             
            if not self._selectEventForProcessing(event, trigger, eventSetAttrs, resultEventSet):
                continue
            
            # Begin Graph Draw
            if self._beginGraphDraw(event, trigger):
                 resultEventSet.add(event)
                 continue

            self._eventBookkeeping(event, origin)
            
            # Origin Database
            #  From another machine: create Visual Features
            if origin == 'database':
                self._initializeEvent(event)
                self._setVisualFeatures(event)
                resultEventSet.add(event)
                continue
                       
            # Adjust Hazard Event Attributes
            self._initializeEvent(event)
            if origin == 'user':
                self._setUserOwned(event)
            
            if trigger == 'dataLayerUpdate':
                self._adjustForDataLayerUpdate(event)                

            elif trigger == 'hazardEventModification':
                changes = self._adjustForEventModification(event)
                #print self.logMessage("Changes", changes)
                #self.flush()
                if not changes:
                    # Handle a status change or "showGrid"
                    if "status" in self._attributeIdentifiers or "showGrid" in self._attributeIdentifiers:
                        self._setVisualFeatures(event)
                        resultEventSet.add(event)
                    continue
            elif trigger == 'hazardEventVisualFeatureChange':
                self._adjustForVisualFeatureChange(event, eventSetAttrs)
            elif trigger == 'autoUpdate':
                self._adjustForAutoUpdate(event, eventSetAttrs)

            print self.logMessage("Re-calculating")                                                
            # Re-calculate Motion Vector-related and Probabilistic Information
            self._advanceDownstreamPolys(event, eventSetAttrs)
            self._getIntervalPolys(event, eventSetAttrs, 'downstream')
            if self._showUpstream:
                self._getIntervalPolys(event, eventSetAttrs, 'upstream')
                    
            if trigger == 'dataLayerUpdate':
                graphProbs = self._probUtils.getGraphProbs(event, self._latestDataLayerTime)
                #LogUtils.logMessage('[0]', graphProbs)
                event.set('convectiveProbTrendGraph', graphProbs)
            
            # Construct Updated Visual Features
            self._setVisualFeatures(event)         

            # Set the read-only attribute to True, since functionality to
            # use it and modify it has not yet been implemented.
            #
            # TODO: Set it as outlined in the Swath Recommender Design doc
            # when modify/read-only functionality is added.
            event.set("readOnly", True)
                
            # Add revised event to result
            resultEventSet.add(event)              

        return resultEventSet      

    def _setDataLayerTimes(self, eventSetAttrs):
        # Data Layer Times are in ms past the epoch
        dlTimes = eventSetAttrs.get("dataLayerTimes")
        if not dlTimes: 
            # Set default data layer times at one minute intervals  
            dlTimes = []
            for i in range(self._upstreamTimeLimit()):
                dlTimes.append(TimeUtils.roundEpochTimeMilliseconds(
                                    self._currentTime - i*60*1000))
            dlTimes.sort()
        # Round data layer times to floor of minute
        self._dataLayerTimes = []
        for dlTime in dlTimes:
            newTime = TimeUtils.roundEpochTimeMilliseconds(int(dlTime))
            # Cut off at upstreamTimeLimit e.g. no earlier than 30 minutes back in time
            if abs(self._currentTime - newTime) / (60*1000) > self._upstreamTimeLimit():
                    continue
            self._dataLayerTimes.append(newTime)
        if self._dataLayerTimes:
            self._latestDataLayerTime = self._dataLayerTimes[-1]
        else:
            self._latestDataLayerTime = self._currentTime        

        print 'latestDataLayerTime', self._probUtils._displayMsTime(self._latestDataLayerTime)
        print 'dataLayerTimes'
        for t in self._dataLayerTimes:
            print self._probUtils._displayMsTime(t) 
        print 'currentTime', self._probUtils._displayMsTime(self._currentTime)
        self.flush()
                
    def _selectEventForProcessing(self, event, trigger, eventSetAttrs, resultEventSet):
        ''' 
        Return True if the event needs to be processed
        Otherwise return False
        '''            
        # For event modification or visual feature change, 
        #   we only want to process the one event identified in the eventSetAttrs,
        #   so skip all others           
        if trigger in ['hazardEventModification', 'hazardEventVisualFeatureChange']:
            eventIdentifier = eventSetAttrs.get('eventIdentifier')            
            if eventIdentifier and eventIdentifier != event.getEventID():
                    return False
        # Check for end time < current time and end the event
        eventEndTime_ms = long(TimeUtils.datetimeToEpochTimeMillis(event.getEndTime()))
        if eventEndTime_ms < self._currentTime:
            event.setStatus('ENDED')
            resultEventSet.add(event)
        # Skip ended, elapsed events 
        if event.getStatus() in ['ELAPSED', 'ENDED']:                
            return False
        return True                  
    
    def _beginGraphDraw(self, event, trigger):
        ''' 
        Return True if the user has clicked "Draw" on the prob trend graph.
        In this case, we'll want to bypass swath recommender processing
        ''' 
        if trigger == 'hazardEventModification' and len(self._attributeIdentifiers) is 1 and \
          'convectiveProbTrendGraph' in self._attributeIdentifiers and \
          event.get('convectiveProbTrendGraph', []) == []:
            # Save off probTrend so that if the user edits some other attribute before drawing the 
            # graph, we have something to reset to
            event.set('preDraw_convectiveProbTrendGraph', event.get('prev_convectiveProbTrendGraph'))  
            return True
        else:
            return False  
        
    def _eventBookkeeping(self, event, origin):  
        '''
        Set up values to be used in swath calculations
        '''      
        print "SR Event status", event.getStatus()
        print "SR Event selected", event.get('selected')
        self._nudge = False
        self._pendingHazard = event.getStatus() in ["PENDING", "POTENTIAL"]
        self._showUpstream = self._pendingHazard
        self._selectedHazard = event.get('selected')        

    #################################
    # Update of Event Attributes    #
    #################################

    def _initializeEvent(self, event):
        # Initialize with event polygon
        mvPolys = event.get('motionVectorPolys')
        if not mvPolys:
            # Set start time and set eventSt_ms:
            self._moveStartTime(event, self._latestDataLayerTime)                
            mvPolys = [event.getGeometry()]
            st = self._probUtils._convertFeatureTime(self._eventSt_ms, 0)
            et = self._probUtils._convertFeatureTime(self._eventSt_ms, self._probUtils._timeStep())
            mvTimes = [(st, et)]
            event.set('motionVectorPolys', mvPolys)
            event.set('motionVectorTimes', mvTimes)
        else:
            self._moveStartTime(event, self._latestDataLayerTime)
            self._probUtils._roundEventTimes(event)
            self._eventSt_ms = long(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()))
        print "SR event start, current", self._eventSt_ms, self._currentTime
        self.flush()
        
    def _setUserOwned(self, event):         
        event.set('convectiveUserOwned', True)
        if event.get('objectID') and not event.get('objectID').startswith('M'):
            event.set('objectID',  'M' + event.get('objectID'))
            
    def _adjustForDataLayerUpdate(self, event):
        self._moveStartTime(event, self._latestDataLayerTime, moveEndTime=False)
        
    def _adjustForEventModification(self, event):
        changed = False
        
        # Handle Reset Motion Vector
        if 'resetMotionVector' in self._attributeIdentifiers: 
            event.set('convectiveObjectDir', 270)
            event.set('convectiveObjectSpdKts', 32) 
            motionVectorPolys = event.get('motionVectorPolys', []) 
            motionVectorTimes = event.get('motionVectorTimes', [])
            if motionVectorPolys:
                motionVectorPolys = [motionVectorPolys[-1]]
                motionVectorTimes = [motionVectorTimes[-1]]
                event.set('motionVectorPolys', motionVectorPolys) 
                event.set('motionVectorTimes', motionVectorTimes) 
            self._showUpstream = True
            return True
        
        # Handle Move Start Time
        if 'moveStartTime' in self._attributeIdentifiers: 
            #self._moveStartTime(event, self._latestDataLayerTime)
            return True
                                
        # Get Convective Attributes from the MetaData. 
        # These should supercede and update the ones stored in the event
        self._updateConvectiveAttrs(event) 
     
        # Compare previous values to the new values to determine if any changes were made.  
        # Update the previous values along the way.
        triggerCheckList = ['convectiveObjectSpdKtsUnc', 'convectiveObjectDirUnc', 'convectiveProbTrendGraph',
                            'convectiveObjectDir', 'convectiveObjectSpdKts', 'convectiveSwathPresets', 
                            'duration']
        newTriggerAttrs = {t:event.get(t) for t in triggerCheckList}
        newTriggerAttrs['duration'] = self._probUtils._getDurationMinutes(event)
                
        for t in triggerCheckList:
            newVal = newTriggerAttrs.get(t)
            prevName = "prev_"+t
            prevVal = event.get(prevName)
            if t == 'convectiveProbTrendGraph':
                if newVal is None: newVal = []
                if prevVal is None: prevVal = [] 
                
            # Test to see if there is a change
            if prevVal != newVal:
                changed = True
                if t == 'duration':
                    graphProbs = self._probUtils._getGraphProbs(event, self._latestDataLayerTime)
                    #LogUtils.logMessage('[1]', graphProbs)
                    event.set('convectiveProbTrendGraph', graphProbs)
                elif t == 'convectiveProbTrendGraph':
                    self._ensureLastGraphProbZeroAndUneditable(event)
                # Update previous value
                event.set(prevName, newVal)
            
        # Ensure that if changes were made, there are valid probability trend graph points ready for calculations. 
        # There could be none currently if the user had previously commenced a "draw points on graph" action, 
        # but did not complete it.
        # TODO -- think about why we need 'preDraw' -- can't we just always rely on 'prev'?
        if changed:
            if not event.get('convectiveProbTrendGraph'):
                event.set('convectiveProbTrendGraph', event.get('preDraw_convectiveProbTrendGraph', []))
            return True
        
        return False

    def _ensureLastGraphProbZeroAndUneditable(self, event):
        probVals = event.get('convectiveProbTrendGraph', [])
        if len(probVals) == 0:
            return
        lastPoint = probVals[-1]
        if lastPoint["y"] != 0 or lastPoint["editable"]:
            probVals[-1] = { "x": lastPoint["x"], "y": 0, "editable": False }
            event.set('convectiveProbTrendGraph', probVals)
    
    def _updateConvectiveAttrs(self, event):
        convectiveAttrs = event.get('convectiveAttrs')
        if not convectiveAttrs:
            return
        if 'wdir' in convectiveAttrs:
            event.set('convectiveObjectDir', convectiveAttrs['wdir'])
        if 'wspd' in convectiveAttrs:
            event.set('convectiveObjectSpdKts', convectiveAttrs['wspd'])
            
    def _moveStartTime(self, event, startMS, moveEndTime=True):
        newStart = datetime.datetime.utcfromtimestamp(startMS/1000)
        #if abs((newStart - event.getStartTime()).total_seconds()*1000) > self._probUtils._timeDelta_ms():
        #    print "returning false"
        #    return False     
        if moveEndTime or self._pendingHazard:
            durationSecs = self._probUtils._getDurationSecs(event)
            endMS = startMS+(durationSecs*1000)
            event.setEndTime(datetime.datetime.utcfromtimestamp(endMS/1000))
        event.setStartTime(newStart)
        self._probUtils._roundEventTimes(event)
        self._eventSt_ms = long(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()))
        return True
                
    ##############################################
    # Downstream bookkeeping                     #
    ##############################################    
                        
    def _advanceDownstreamPolys(self, event, eventSetAttrs):
        ''' 
        As the event start time moves forward, 
            move downstreamPolys to historyPolys
        ''' 
        ####### 
        # Downstream Polys
        downstreamPolys = event.get('downstreamPolys', [])
        downstreamTimes = event.get('downstreamTimes', [])
        if not downstreamPolys or not downstreamTimes:
            return
        index = 0
        for i in range(len(downstreamTimes)):           
            times = downstreamTimes[i]
            if times is None:
                print "SwathRecommender Warning: downstream times None", downstreamTimes
                self.flush()
                return
            
            st, et = downstreamTimes[i]
            #print "SR Advance downstream st, eventSt_ms", st >= self._eventSt_ms, st, self._eventSt_ms
            self.flush() 
            if st >= self._eventSt_ms:    
                index = i
                break
        #print "SR Advance index", index
        self.flush()
        
        event.set('downstreamPolys', downstreamPolys[index:])
        event.set('downstreamTimes', downstreamTimes[index:])
        
        # If trigger is visualFeature, then don't do this
        # otherwise do this
        trigger = eventSetAttrs.get('trigger')
        #if not trigger == "hazardEventVisualFeatureChange":
        #    self._setEventGeometry(event, downstreamPolys[index])
        
        ######
        #  History Polys        
        historyPolys = event.get('historyPolys', [])
        historyTimes = event.get('historyTimes', [])
        newHistPolys = historyPolys + downstreamPolys[:index]
        newHistTimes = historyTimes + downstreamTimes[:index]
        
        # Truncate historyPolys if there is a maximum limit set
        maxHistory = self._historySteps()
        if maxHistory and len(newHistPolys) > maxHistory:
            index = len(newHistPolys) - maxHistory
            newHistPolys = newHistPolys[index:]
            newHistTimes = newHistTimes[index:]
        event.set('historyPolys', newHistPolys)
        event.set('historyTimes', newHistTimes)
         
    def _setEventGeometry(self, event, polygon):
        polygon = self._probUtils._reducePolygon(polygon)
        event.setGeometry(AdvancedGeometry.createShapelyWrapper(polygon, 0))

    ##############################################
    # Upstream / Downstream set up               #
    ##############################################    
 
    def _getIntervalPolys(self, event, eventSetAttrs, timeDirection='downstream'):
        timeIntervals = []
        if timeDirection == 'upstream':
            # Follow dataLayerTimes for upstream polys.
            # Do not include the latest data layer time. 
            # DataLayerTimes are in ms past the epoch
            # Convert to secs relative to eventSt_ms
            for dlTime in self._dataLayerTimes[:-1]:
                secs = int((dlTime - self._eventSt_ms) / 1000)
                timeIntervals.append(secs)
        else:
            # Follow timeStep for downstream polys
            durationSecs = self._probUtils._getDurationSecs(event, truncateAtZero=True)
            timeStep = self._probUtils._timeStep()
            i= 0
            while i <= durationSecs:
                timeIntervals.append(i)
                i += timeStep
        #print "SR getIntervalPolys", timeDirection, timeIntervals
        #self.flush()
        self._probUtils._createIntervalPolys(event, eventSetAttrs, self._nudge, SwathPreset(), 
                                             self._eventSt_ms, timeIntervals, timeDirection)

    ###############################
    # Visual Features and Nudging #
    ###############################  
      
    def _adjustForAutoUpdate(self, event, eventSetAttrs):
        self._nudge = True
        #=======================================================================
        # # If nudging an issued event, restore the issuedDuration
        # if not self._showUpstream:
        #     durationSecs = event.get("durationSecsAtIssuance")
        #     if durationSecs is not None:
        #         endTimeMS = TimeUtils.roundEpochTimeMilliseconds(self._eventSt_ms + durationSecs *1000)
        #         event.setEndTime(datetime.datetime.utcfromtimestamp(endTimeMS/1000))
        #         #graphProbs = self._probUtils._getGraphProbsBasedOnDuration(event)
        #         graphProbs = event.get("graphProbsAtIssuance")
        #         event.set('convectiveProbTrendGraph', graphProbs)
        # #=======================================================================
        # # newMotion = self._probUtils.computeMotionVector(motionVectorTuples, self._eventSt_ms, #self._currentTime,                     
        # #            event.get('convectiveObjectSpdKts', self._defaultWindSpeed()),
        # #            event.get('convectiveObjectDir',self._defaultWindDir())) 
        # # for attr in ['convectiveObjectDir', 'convectiveObjectSpdKts',
        # #               'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc']:
        # #     event.set(attr, int(newMotion.get(attr)))
        # #=======================================================================
        #=======================================================================
        
        
        
 
    ###############################
    # Visual Features and Nudging #
    ###############################  
      
    def _adjustForVisualFeatureChange(self, event, eventSetAttrs):
        '''
        Based on the visual feature change, update the motion vector
                      
        Update event geometry i.e. polygon at event start time, if changed        
        '''
        # Add the visualFeature change to the motion vector polygons       
        motionVectorPolys = event.get('motionVectorPolys', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
            
        features = event.getVisualFeatures()
        if not features: features = []

        # We are only changing one attribute per SwathRecommender execution
        changedIdentifier = list(eventSetAttrs.get('attributeIdentifiers'))[0]

        for feature in features:
            featureIdentifier = feature.get('identifier')
            # Find the feature that has changed
            if featureIdentifier == changedIdentifier:
                # Get feature polygon
                polyDict = feature["geometry"]
                #  This will work because we only have one polygon in our features
                #  TODO refactor to have multiple polygons per feature
                for timeBounds, geometry in polyDict.iteritems():
                    featureSt, featureEt = timeBounds
                    featureSt = long(featureSt)
                    featurePoly = geometry
                # Add the feature to the motionVectorPolys 
                motionVectorPolys, motionVectorTimes = self._updatePolys(motionVectorPolys, motionVectorTimes, featurePoly, featureSt, featureEt)
                event.set('motionVectorPolys', motionVectorPolys)
                event.set('motionVectorTimes', motionVectorTimes)
                                     
                # If the feature is prior to the event start time, 
                #   add it to the history list
                if featureSt < self._eventSt_ms:
                    histPolys = event.get('historyPolys', []) 
                    histTimes = event.get('historyTimes', [])
                    histPolys, histTimes = self._updatePolys(histPolys, histTimes, featurePoly, featureSt, featureEt)
                    event.set('historyPolys', histPolys)
                    event.set('historyTimes', histTimes)
                    
                # If the feature is at the event start time, add it to the 
                #  event geometry
                if abs(featureSt-self._eventSt_ms) <= self._probUtils._timeDelta_ms():
                    event.setGeometry(featurePoly)
                    self._nudge = True
                    # If nudging an issued event, restore the issuedDuration
                    if not self._showUpstream:
                        durationSecs = event.get("durationSecsAtIssuance")
                        if durationSecs is not None:
                            endTimeMS = TimeUtils.roundEpochTimeMilliseconds(self._eventSt_ms + durationSecs *1000)
                            event.setEndTime(datetime.datetime.utcfromtimestamp(endTimeMS/1000))
                            #graphProbs = self._probUtils._getGraphProbsBasedOnDuration(event)
                            graphProbs = event.get("graphProbsAtIssuance")
                            #LogUtils.logMessage('[2]', graphProbs)
                            event.set('convectiveProbTrendGraph', graphProbs)
                                        
        #print "SR Feature ST", featureSt, self._probUtils._displayMsTime(featureSt)
        #self.flush()
        
        if len(motionVectorPolys) <= 1:
            return
                                      
        # Re-compute the motion vector and uncertainty
        motionVectorTuples = []
        for i in range(len(motionVectorPolys)):
            poly = motionVectorPolys[i]
            st, et = motionVectorTimes[i]
            motionVectorTuples.append((poly, st, et))
            print 'SR motionVector Poly, startTime:', poly.asShapely().centroid, st
            self.flush()
            
        #print "SR motionVectorTuples", len(motionVectorTuples)
        #self.flush()
            
        newMotion = self._probUtils.computeMotionVector(motionVectorTuples, self._eventSt_ms, #self._currentTime,                     
                   event.get('convectiveObjectSpdKts', self._defaultWindSpeed()),
                   event.get('convectiveObjectDir',self._defaultWindDir())) 
        for attr in ['convectiveObjectDir', 'convectiveObjectSpdKts',
                      'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc']:
            event.set(attr, int(newMotion.get(attr)))

    def _updatePolys(self, polys, times, newPoly, newSt, newEt):
        # Add the newPoly to the list of polys and times
        # If one currently exists at the newSt, replace it
        # Keep the lists in time order
    
        # Convert to tuples
        tuples = []
        for i in range(len(polys)):
            poly = polys[i]
            st, et = times[i]
            tuples.append((poly, st, et))

        newTuples = []
        found = False
        for poly, st, et in tuples:
            if abs(st-newSt) <= self._probUtils._timeDelta_ms():                        
                newTuples.append((newPoly, st, et))
                found = True
            else: 
                newTuples.append((poly, st, et))
        # Otherwise, add a new motionVectorPoly 
        if not found:                    
            newTuples.append((newPoly, newSt, newEt))
 
        newTuples.sort(self._sortPolys)                
        newPolys = [poly for poly, st, et in newTuples]
        newTimes = [(st,et) for poly, st, et in newTuples]            
        return newPolys, newTimes         

 
    def _setVisualFeatures(self, event):
        print self.logMessage("Setting Visual Features")
        self.flush()

        downstreamPolys = event.get('downstreamPolys')
        if not downstreamPolys:
            return
        
        features = []
        upstreamSt_ms = self._dataLayerTimes[0]
        
        # Down Stream Features -- polygons, track points, relocated last motionVector
        features += self._downstreamVisualFeatures(event, upstreamSt_ms)
        
        # Swath
        swathFeature = self._swathFeature(event, upstreamSt_ms, downstreamPolys)                
        features.append(swathFeature) 
        
        # Motion vector centroids
        features += self._motionVectorFeatures(event, upstreamSt_ms)     
        
        # Previous Time Features
        features += self._previousTimeVisualFeatures(event)
        
        # Preview Grid
        features += self._getPreviewGridFeatures(event, upstreamSt_ms)
                           
        # Replace Visual Features
        if features:
            event.setVisualFeatures(VisualFeatures(features))
            
        if self._printVisualFeatures:
             self._printFeatures(event, "Visual Features", features)
        
    def _downstreamVisualFeatures(self, event, upstreamSt_ms):
        downstreamPolys = event.get('downstreamPolys')
        if not downstreamPolys:
            return
        downstreamTimes = event.get('downstreamTimes')
        
        # TODO: This should probably just be event.getGeometry() once ellipses
        # are handled by this recommender. Right now, it is flattened so that
        # any ellipse, etc. is treated as a polygon.
        geometry = event.getFlattenedGeometry()
        
        features = []
        # Downstream Polygons, Track Points, Relocated Downstream 
        numIntervals = len(downstreamPolys) 
        probTrend = event.get('convectiveProbTrendGraph')[0]
        probStr = ' '+ `probTrend['y']`+'%'
        self._label = str(event.get('objectID')) + " " + event.getHazardType() + probStr
   
        for i in range(numIntervals):
            poly = downstreamPolys[i]
            polySt_ms, polyEt_ms = downstreamTimes[i]
            polyEnd = polyEt_ms - 60*1000 # Take a minute off
              
            downstreamFeature = {
              "identifier": "swathRec_downstream_"+str(polySt_ms),
              "visibilityConstraints": "always",
              "borderColor": "eventType",
              "borderThickness": "eventType",
              "borderStyle": "eventType",
              "textSize": "eventType",
              "label": self._label,
              "textColor": "eventType",
              "dragCapability": "none", 
              "geometry": {
                   (polySt_ms, polyEnd): poly
                  }
                }
            features.append(downstreamFeature)
               
            # Track Points 
            centroid = poly.asShapely().centroid
            color = self._probUtils._getInterpolatedProbTrendColor(event, i, numIntervals)
            trackPointFeature = {
              "identifier": "swathRec_trackPoint_"+str(polySt_ms),
              "visibilityConstraints": "selected",
              "borderColor": color,
              "borderThickness": 2,
              "diameter": 5,
              "geometry": {
                  (upstreamSt_ms,
                   TimeUtils.datetimeToEpochTimeMillis(event.getEndTime())):
                   AdvancedGeometry.createShapelyWrapper(centroid, 0)
               }
            }
            features.append(trackPointFeature)
             
            # Relocated initial going downstream
            showCentroid = False
            if polySt_ms >= self._eventSt_ms:
                if i == 0:
                    dragCapability = "all"
                    showCentroid = True
                else:
                    dragCapability = "none"
                    showCentroid = False
                    
                relocatedPoly = self._probUtils._reducePolygon(self._probUtils._relocatePolygon(centroid, geometry))
                relocatedFeature = {
                  "identifier": "swathRec_relocated_"+str(polySt_ms),
                  "visibilityConstraints": "selected",
                  "borderColor": "eventType",
                  "borderThickness": "eventType",
                  "borderStyle": "dashed",
                  "textSize": "eventType",
                  "dragCapability": dragCapability, 
                  "geometry": {
                      (polySt_ms, polyEnd): AdvancedGeometry.createShapelyWrapper(relocatedPoly, 0)
                       }
                }
                features.append(relocatedFeature)
                
            if showCentroid:
               centroidFeature = {
                  "identifier": "swathRec_relocatedCentroid_"+str(polySt_ms),
                  "visibilityConstraints": "selected",
                  "borderColor": { "red": 0, "green": 0, "blue": 0 },
                  "borderThickness": 2,
                  "fillColor": { "red": 1, "green": 1, "blue": 1},
                  "diameter": 6,
                   "geometry": {
                      (polySt_ms, polyEnd): AdvancedGeometry.createShapelyWrapper(centroid, 0)
                       }
                }
               features.append(centroidFeature)
     
        return features

    def _swathFeature(self, event, upstreamSt_ms, downstreamPolys):
        advancedGeometries = downstreamPolys
        downstreamPolys = []
        for geometry in advancedGeometries:
            downstreamPolys.append(geometry.asShapely())
        envelope = shapely.ops.cascaded_union(downstreamPolys)
        swath = {
              "identifier": "swathRec_swath",
              "visibilityConstraints": "selected",
              "borderColor": { "red": 1, "green": 1, "blue": 0 },
              "borderThickness": 3,
              "borderStyle": "dotted",
              "geometry": {
                  (upstreamSt_ms,
                   TimeUtils.datetimeToEpochTimeMillis(event.getEndTime())):
                   AdvancedGeometry.createShapelyWrapper(envelope, 0)
               }
              }
        return swath

    def _motionVectorFeatures(self, event, upstreamSt_ms):
        motionVectorPolys = event.get('motionVectorPolys', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
        features = []
        for i in range(len(motionVectorTimes)):
            st, et = motionVectorTimes[i]
            poly = motionVectorPolys[i].asShapely()                        
            centroid = AdvancedGeometry.createShapelyWrapper(poly.centroid, 0)
            feature = {
              "identifier": "swathRec_motionVector_"+str(st),
              "visibilityConstraints": "selected",
              "borderColor": { "red": 0, "green": 0, "blue": 0 },
              "borderThickness": 2,
              "fillColor": { "red": 1, "green": 1, "blue": 1},
              "diameter": 6,
              "geometry": {
                  #(st, et): centroid
                  (upstreamSt_ms,
                   TimeUtils.datetimeToEpochTimeMillis(event.getEndTime())):
                   centroid
               }
            }
            features.append(feature)
        return features

    def _getPreviewGridFeatures(self, event, upstreamSt_ms):
        if not event.get('showGrid'): 
            return []
        gridFeatures = []            
        probGrid, lons, lats = self._probUtils.getProbGrid(event)
        polyTupleDict = self._createPolygons(probGrid, lons, lats)
                
        # Generate and add preview-grid-related visual features        
        for key in polyTupleDict: 
            upperVal = str(int(key) + 20)
            try:
                poly = GeometryFactory.createPolygon(polyTupleDict[key],[polyTupleDict[upperVal]])
            except KeyError:
                poly = GeometryFactory.createPolygon(polyTupleDict[key])                       
                
            #print "SR previewGrid poly", type(poly)
            #self.flush()                       
                                
            ### Should match PHI Prototype Tool
            colorFill =  {
                '0': { "red": 102/255.0, "green": 224/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '20': { "red": 255/255.0, "green": 255/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '40': { "red": 255/255.0, "green": 179/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '60': { "red": 255/255.0, "green": 102/255.0, "blue": 102/255.0, "alpha": 0.4 }, 
                '80': { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0, "alpha": 0.4 }
                }
            
            if poly.is_valid:
                gridPreviewPoly = {
                    "identifier": "gridPreview_" + key,
                    "visibilityConstraints": "selected",
                    "borderColor": { "red": 0, "green": 0, "blue": 0 }, # colorFill[key],
                    "fillColor": colorFill[key],
                    "geometry": {
                        (upstreamSt_ms,
                        #(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()), 
                         TimeUtils.datetimeToEpochTimeMillis(event.getEndTime())):
                                 AdvancedGeometry.createShapelyWrapper(poly, 0)
                    }
                }
                gridFeatures.append(gridPreviewPoly)
                            
        return gridFeatures
    
    def _createPolygons(self, probGrid, lons, lats):
        polyDict = {}
        
        #probGrid, lons, lats = ProbUtils().getProbGrid(event)
                
        levels = np.linspace(0,100,6)

        X, Y = np.meshgrid(lons, lats)
        plt.figure()
        CS = plt.contour(X, Y, probGrid, levels=levels)

        prob = ['0', '20', '40', '60', '80']
        probIndex = [0.0, 20.0, 40.0, 60.0, 80.0]
        polyTupleDict = {}

        for c in range(0,(len(CS.levels) - 1)):
            contourVal = CS.levels[c]
            coords = CS.collections[c].get_paths()

            if len(coords):
                points = coords[0].vertices
                
                longitudes = []
                latitudes = []
                
                for point in range(0, len(points)):
                    longitudes.append(points[point][0])
                    latitudes.append(points[point][1])
                
                if contourVal in probIndex:    
                    polyTupleDict[prob[c]] = zip(longitudes, latitudes)
                    
        return polyTupleDict
        
    def _previousTimeVisualFeatures(self, event):    
        # Previous time polygons -- prior to current time
        #
        # If time has marched forward from the initial creation of the hazard, we could have
        #  motionVectorPolys, historyPolys, and upstreamPolys (IF still pending)
        #
        # Choose to use in this order of preference:
        #      motionVector polygon
        #      then the history polygon
        #      then if the hazard is pending
        #         the upstream polygon
        #
        # All are editable if the hazard has not been issued yet.
        # 
        #print "\nDoing Previous time polys"
        historyPolys = event.get('historyPolys', [])
        historyTimes = event.get('historyTimes', [])
        motionVectorPolys = event.get('motionVectorPolys', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
        upstreamPolys = event.get('upstreamPolys', []) 
        upstreamTimes = event.get('upstreamTimes', [])
        previousFeatures = []
                
        if self._showUpstream:
            dragCapability = 'whole'
            color = { "red": 1, "green": 1, "blue": 0 }
        else:
            dragCapability = 'none'
            color = "eventType"
            
        # Don't want to show upstream polys for latestDataLayerTime
        for i in range(len(self._dataLayerTimes[:-1])):
            polySt_ms = self._dataLayerTimes[i]
            poly = self._findPreviousPoly(polySt_ms,
                                          motionVectorPolys, motionVectorTimes, 
                                          historyPolys, historyTimes, upstreamPolys, upstreamTimes)
            if not poly:
                continue
            polyEt_ms = self._dataLayerTimes[i+1] - self._probUtils._timeStep()*1000
            #print "SR previous st, et", self._probUtils._displayMsTime(polySt_ms), self._probUtils._displayMsTime(polyEt_ms)
            #self.flush()
                                
            previousFeature = {              
              "identifier": "swathRec_previous_"+str(polySt_ms),
              "visibilityConstraints": "selected",
              "borderColor":  color, #{ "red": 1, "green": 1, "blue": 0 }, #"eventType", 
              "borderThickness": "eventType",
              "borderStyle": "eventType",
              "dragCapability": dragCapability,
              "textSize": "eventType",
              "label": self._label, 
              "textColor": "eventType",
              "geometry": {
                  (polySt_ms, polyEt_ms): poly
               }
              }
            previousFeatures.append(previousFeature)
        return previousFeatures


    def _findPreviousPoly(self, polySt_ms, motionVectorPolys, motionVectorTimes, historyPolys, historyTimes,
                          upstreamPolys, upstreamTimes):
        for i in range(len(motionVectorTimes)):
            st, et = motionVectorTimes[i]
            if abs(st-polySt_ms) < self._probUtils._timeDelta_ms():
                #print "SR upstream using motion vector", i, st
                #self.flush()
                return motionVectorPolys[i]
        for i in range(len(historyTimes)):
            st, et = historyTimes[i]
            if abs(st-polySt_ms) < self._probUtils._timeDelta_ms():
                #print "SR previous history", st
                #self.flush()
                return historyPolys[i]
        if not self._showUpstream:
            return None
        for i in range(len(upstreamTimes)):
            st, et = upstreamTimes[i]
            if abs(st-polySt_ms) < self._probUtils._timeDelta_ms():
                #print "SR previous upstream poly", st
                #self.flush()
                return upstreamPolys[i]
        return None

    ###############################
    # Helper methods              #
    ###############################
        
    def _sortPolys(self, p1, p2):
        # Sort polygon tuples by start time
        poly1, st1, et1 = p1
        poly2, st2, et2 = p2
        if st1 < st2:
            return -1
        if st2 < st1:
            return 1
        return 0
            
    def _printFeatures(self, event, label, features):
        print label, event.getEventID(), " = ", str(len(features)), ' ----'
        self.flush()
        #for feature in features:
        #    print feature

    def _printEventSet(self, label, eventSet, eventLevel=1):            
        print label
        if self._printEventSetAttributes:
            # TODO Kevin can we just use print?
            if isinstance(eventSet, list):
                print "No values in EventSet"
                return
                
            eventSetAttrs = eventSet.getAttributes()
            print "trigger: ", eventSetAttrs.get("trigger")
            print "eventIdentifier: ", eventSetAttrs.get("eventIdentifier")
            print "origin: ", eventSetAttrs.get("origin")
            print "attributeIdentifiers: ", eventSetAttrs.get("attributeIdentifiers")
            #print "eventType: ", eventSetAttrs.get("eventType")
        if eventLevel:
            print "Events:", len(eventSet.getEvents())
            for event in eventSet:
                self._printEvent(None, event, eventLevel)
#             sys.stderr.write("Running swath recommender.\n    trigger: " +
#                          str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
#                          str(eventSet.getAttribute("eventType")) + "\n    hazard ID: " +
#                          str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute: " +
#                          str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
         

    def _printEvent(self, label, event, eventLevel=1):
        if self._printEvent:
            import pprint
            if label: print label
            if eventLevel >=1:
                print 'ID:', event.getEventID(), event.get('objectID')
                print "start, end", event.getStartTime(), event.getEndTime()
                print "userOwned", event.get('convectiveUserOwned')
            if eventLevel >= 2:
                print '=== attrs ==='
                pprint.pprint(event.getHazardAttributes())
                print '=== ... ==='
            if eventLevel >= 3:
                print '=== geometry ==='
                pprint.pprint(event.getFlattenedGeometry())
                print '=== ... ==='
            self.flush()
                
    def __str__(self):
        return 'Swath Recommender'

    def flush(self):
        import os
        os.sys.__stdout__.flush()


    #########################################
    ### OVERRIDES
    
    def _defaultWindSpeed(self):
        return 32
    
    def _defaultWindDir(self):
        return 270
    
    def _upstreamTimeLimit(self):
        # Number of minutes backward in time for upstream polygons
        return 30 

    def _historySteps(self):
        # Maximum number of history polygons to store
        # Older polygons are dropped off as time progresses
        return None
                        
    def _setPrintFlags(self):
        self._printVisualFeatures = False
        self._printEventSetAttributes = True
        self._printEventSetEvents = True
        self._printEvents = True
        
        
    def logMessage(self, *args):
        import inspect, os
        s = ", ".join(str(x) for x in list(args))
        fName = os.path.basename(inspect.getfile(inspect.currentframe()))
        lineNo = inspect.currentframe().f_back.f_lineno
        return '\t**** [' + str(fName) + ' // Line ' + str(lineNo) + ']:' + s

