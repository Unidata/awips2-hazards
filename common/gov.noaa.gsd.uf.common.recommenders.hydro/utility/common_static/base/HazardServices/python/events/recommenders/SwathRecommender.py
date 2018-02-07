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
from HazardConstants import SAVE_TO_HISTORY_KEY, SAVE_TO_DATABASE_KEY, KEEP_SAVED_TO_DATABASE_LOCKED_KEY, SELECTED_TIME_KEY

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
        dirWtTup = (0., 30.)
        dirWt = dirWtTup[1] * secs / totalSecs
        dirVal = dirVal + dirWt
        speedVal = speedVal * spdWt
      
        returnDict = {
                      'speedVal':speedVal,
                      'dirVal':dirVal,
                      'spdUVal':spdUVal,
                      'dirUVal':dirUVal
                      }
        return returnDict
    
    def LeftTurningSupercell(self, speedVal, dirVal, spdUVal, dirUVal, secs, totalSecs): 
        spdWt = 0.75
        dirWtTup = (0., 30.)
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
        spdUValWtTup = (0., 15)
        dirUValWtTup = (0., 40)
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
        spdUValWtTup = (0., 7)
        dirUValWtTup = (0., 20)
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
        self.probUtils = ProbUtils()
        self.BUFFER_COLOR = { "red": 0.0, "green": 0.0, "blue": 0.0, "alpha": 0.7 }
        self.BUFFER_THICKNESS = 3.0
        
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
        metadata['onlyIncludeTriggerEvents'] = True
        metadata['includeDataLayerTimes'] = True
        
        metadata["getDialogInfoNeeded"] = False
        metadata["getSpatialInfoNeeded"] = False
        
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
        self.setPrintFlags()
                 
        eventSetAttrs = eventSet.getAttributes()
        trigger = eventSetAttrs.get('trigger')
        origin = eventSetAttrs.get('origin')
        self.currentTime = long(eventSetAttrs.get("currentTime")) 
        self.selectedTime = None if eventSetAttrs.get(SELECTED_TIME_KEY) is None else long(eventSetAttrs.get(SELECTED_TIME_KEY))
        self.setDataLayerTimes(eventSetAttrs)                
        self.attributeIdentifiers = eventSetAttrs.get('attributeIdentifiers', [])

        self.printEventSet("\n*********************\nRunning SwathRecommender", eventSet, eventLevel=1)

        if self.attributeIdentifiers is None:
            self.attributeIdentifiers = []

        resultEventSet = EventSetFactory.createEventSet(None)
        self.saveToDatabase = True
        self.saveToHistory = False
        self.keepLocked = True
        
        userName = eventSetAttrs.get('userName')
        workStation = eventSetAttrs.get('workStation') 
        # for automated event, username / workstation may not be present
        if userName and workStation:  
            self.caveUser = userName + ":" + workStation
        else:
            self.caveUser = None
        print "caveUser is ", self.caveUser
        
        # IF there are any editable objects, we do not want to set the selected time ahead for
        #   a timeInterval update
        self.editableObjects = False
        if trigger == 'dataLayerUpdate':
            for event in eventSet:
                editable, selected = self.isEditableSelected(event)
                if editable:
                    self.editableObjects = True
                
        for event in eventSet:

            event.set('visibleGeometry', 'highResolutionGeometryIsVisible')

            print 'SR:', event.get('objectID'), event.getStatus(), event.getStartTime(), event.getEndTime()
            self.lastSelectedTime = self.selectedTime
                                    
            # Determine if we want to process this event or skip it
            if not self.selectEventForProcessing(event, trigger, origin, eventSetAttrs, resultEventSet):
                continue

            # Begin Graph Draw
            if self.beginGraphDraw(event, trigger):
                 resultEventSet.add(event)
                 continue

            self.movedStartTime = False
            self.initializeEvent(event)
            self.eventBookkeeping(event, origin)
            
            print 'SR -- YG: -activate, activateModify-- ', event.get('activate'), event.get('activateModify')
            print "SR: editableHazard, selectedHazard, editableObjects -- YG", self.editableHazard, self.selectedHazard, self.editableObjects
            self.flush()
            
            # React to the change in selection state, if that is what
            # triggered this execution of the recommender.
            if trigger == 'hazardEventSelection':
                
                # Ensure that if the event has been deselected, or if
                # it has been selected but has previously been issued,
                # the changes made to its activate-related attributes
                # do not keep it locked. 
                if event.get('selected') is False or event.getStatus() != 'PENDING':
                    self.keepLocked = False
                
                print 'SR: Hazard event', event.getEventID(), 'selection state is now', event.get('selected')
                #print 'SR -- YG: -activate, activateModify-- ', event.get('activate'), event.get('activateModify')
                self.probUtils.setActivation(event, self.caveUser)
                self.editableHazard, self.selectedHazard = self.isEditableSelected(event)
                print 'SR -- YG: -activate, activateModify-- ', event.get('activate'), event.get('activateModify')
                print "SR: editableHazard, selectedHazard, editableObjects -- YG", self.editableHazard, self.selectedHazard, self.editableObjects
                print "SR: lastSelectedTime, starttime -- YG", self.lastSelectedTime, event.getStartTime()
                self.flush()
                self.setVisualFeatures(event)
                resultEventSet.add(event)
                continue
            
            # Adjust Hazard Event Attributes
            changes = False
            if trigger == 'frameChange':
                if not self.adjustForFrameChange(event, eventSetAttrs, resultEventSet):
                    continue
                    
            elif trigger == 'dataLayerUpdate':
                self.processDataLayerUpdate(event, eventSetAttrs, resultEventSet)
                continue
                
            elif trigger == 'timeInterval':
                continue
                
            elif trigger == 'hazardEventModification':

                if 'status' in self.attributeIdentifiers and event.getStatus() in ['ELAPSED', 'ENDED', 'ISSUED']:
                    self.keepLocked = False
                
                changes = self.adjustForEventModification(event, eventSetAttrs, resultEventSet)
                #if not changes:
                # Handle other dialog buttons
                self.handleAdditionalEventModifications(event, resultEventSet)
                    #continue
                if 'modifyButton' in self.attributeIdentifiers or (self.editableHazard and self.movedStartTime): 
                    resultEventSet.addAttribute(SELECTED_TIME_KEY, self.eventSt_ms)
                    self.lastSelectedTime = self.eventSt_ms
                    print "SR Setting selected time to eventSt"
                    print "SR Setting selected time to eventSt --YG", self.eventSt_ms
                    self.flush()
                    changes = True
                    
            elif trigger == 'hazardEventVisualFeatureChange':
                if not self.adjustForVisualFeatureChange(event, eventSetAttrs):
                    continue
                resultEventSet.addAttribute(SELECTED_TIME_KEY, self.dataLayerTimeToLeft)
                print "SR Setting selected time to dataLayerTimeToLeft"
                print "SR Setting selected time to dataLayerTimeToLeft --YG", self.dataLayerTimeToLeft
                self.lastSelectedTime =  self.dataLayerTimeToLeft
                self.flush()
                changes = True
                
            elif trigger == 'autoUpdate':
                changes = True
            
            if not changes:
                continue

            print self.logMessage("Re-calculating")                                                
            # Re-calculate Motion Vector-related and Probabilistic Information
            self.getIntervalPolys(event, eventSetAttrs, 'forecast')
            if self.editableHazard and event.get('settingMotionVector'):
                self.getIntervalPolys(event, eventSetAttrs, 'upstream')
            else:
                event.set('upstreamPolys', [])
                    
            if trigger == 'dataLayerUpdate' and not self.editableObjects:
                if self.latestDataLayerTime <= self.selectedTime:
                    graphProbs = self.probUtils.getGraphProbs(event, self.latestDataLayerTime)
                    event.set('convectiveProbTrendGraph', graphProbs)
            
            # Construct Updated Visual Features
            self.setVisualFeatures(event)         
                                
            # Add revised event to result
            event.set('lastSelectedTime', self.lastSelectedTime)
            print "SR setting lastSelectedTime to --- YG --", self.lastSelectedTime
            self.flush()
            resultEventSet.add(event)
        
        # Save to history list if appropriate; otherwise, if needed, save to database,
        # and in the latter case, note whether to keep the event locked or not. (For
        # saves to history, the lock is automatically let go.)    
        if self.saveToHistory:
            resultEventSet.addAttribute(SAVE_TO_HISTORY_KEY, True)
        elif self.saveToDatabase:
            resultEventSet.addAttribute(SAVE_TO_DATABASE_KEY, True)
            resultEventSet.addAttribute(KEEP_SAVED_TO_DATABASE_LOCKED_KEY, self.keepLocked)

        self.printEventSet("*****\nFinishing SwathRecommender", eventSet, eventLevel=1)
        return resultEventSet      
    
    def setDataLayerTimes(self, eventSetAttrs):
        # Data Layer Times are in ms past the epoch
        dlTimes = eventSetAttrs.get("dataLayerTimes")
        if not dlTimes: 
            # Set default data layer times at one minute intervals  
            dlTimes = []
            for i in range(self.upstreamTimeLimit()):
                dlTimes.append(TimeUtils.roundEpochTimeMilliseconds(
                                self.currentTime - i * 60 * 1000, delta=datetime.timedelta(seconds=1)))
            dlTimes.sort()
            
        # Round data layer times to seconds
        self.dataLayerTimes = []
        self.dataLayerTimeToLeft = self.currentTime
        for dlTime in dlTimes:
            newTime = TimeUtils.roundEpochTimeMilliseconds(int(dlTime), delta=datetime.timedelta(seconds=1))
            if self.selectedTime >= newTime:
                self.dataLayerTimeToLeft = newTime
            self.dataLayerTimes.append(newTime)
         
        if self.dataLayerTimes:
            self.latestDataLayerTime = self.dataLayerTimes[-1]
        else:
            self.latestDataLayerTime = self.currentTime  

        print 'latestDataLayerTime', self.probUtils.displayMsTime(self.latestDataLayerTime)
        print 'dataLayerTimes'
        for t in self.dataLayerTimes:
            print self.probUtils.displayMsTime(t),
        print 'currentTime', self.probUtils.displayMsTime(self.currentTime)
        print 'framesInfo', eventSetAttrs.get('framesInfo')
        self.flush()
                
    def selectEventForProcessing(self, event, trigger, origin, eventSetAttrs, resultEventSet):
        ''' 
        Return True if the event needs to be processed
        Otherwise return False
        '''            
        
        # Make sure that already elapsed events are never processed.
        if event.getStatus() == "ELAPSED":
            # elapsed event may need to be added again so that the other UI can see it 
#             event.setStatus('ELAPSED')
#             event.set('statusForHiddenField', 'ELAPSED')
#             resultEventSet.add(event)            
#             self.keepLocked = False
            return False
        
        # Make sure that triggers resulting from database changes are never
        # processed.
        if origin == 'database':
            return False

        # For event modification, visual feature change, or selection change, 
        #   we only want to process the events identified in the eventSetAttrs,
        #   so skip all others       
        if trigger in ['hazardEventModification', 'hazardEventVisualFeatureChange', 'hazardEventSelection']:
            eventIdentifiers = eventSetAttrs.get('eventIdentifiers')
            if event.getEventID() not in eventIdentifiers:
                return False

        # Check for Elapsed 
        eventEndTime_ms = long(TimeUtils.datetimeToEpochTimeMillis(event.getEndTime()))
        if self.currentTime - eventEndTime_ms > self.elapsedTimeLimit():
            # Set to Elapsed 
            event.setStatus('ELAPSED')
            event.set('statusForHiddenField', 'ELAPSED')
            resultEventSet.add(event)
            self.saveToHistory = True
            self.keepLocked = False
            return False

        # Skip ending, previously ended, and potential events 
        if event.getStatus() in ['ENDING', 'ENDED', 'POTENTIAL']:
            return False

        # Check for end time < current time and end the event
        if eventEndTime_ms < self.currentTime:
            # Set to ended
            event.setStatus('ENDED')
            event.set('statusForHiddenField', 'ENDED')
            resultEventSet.add(event)
            self.saveToHistory = True
            self.keepLocked = False
            return False
            # BUG ALERT?? Should we still process it?

        return True
    
    def beginGraphDraw(self, event, trigger):
        ''' 
        Return True if the user has clicked "Draw" on the prob trend graph.
        In this case, we'll want to bypass swath recommender processing
        ''' 
        if trigger == 'hazardEventModification' and len(self.attributeIdentifiers) is 1 and \
          'convectiveProbTrendGraph' in self.attributeIdentifiers and \
          event.get('convectiveProbTrendGraph', []) == []:
            # Save off probTrend so that if the user edits some other attribute before drawing the 
            # graph, we have something to reset to
            event.set('preDraw_convectiveProbTrendGraph', event.get('prev_convectiveProbTrendGraph'))  
            return True
        else:
            return False  
        
    def eventBookkeeping(self, event, origin):  
        '''
        Set up values to be used in swath calculations
        '''      
        self.pendingHazard = event.getStatus() in ["PENDING", "POTENTIAL"]
        self.editableHazard, self.selectedHazard = self.isEditableSelected(event)
        print "SR Bookkeeping Event status", event.getStatus()
        print "SR Event selected", event.get('selected')
        print "SR Event activate, editableHazard", event.get('activate'), self.editableHazard
        print "SR Start Time", self.probUtils.displayMsTime(self.eventSt_ms)
        self.flush()

    def isEditableSelected(self, event):
        selected = event.get('selected', False)
        #print 'SR [FARNSWORTH] ID:', event.get('objectID')
        #print "SR [FARNSWORTH] isEditable  selected, activate", selected, event.get('activate', False)
        #print "SR [FARNSWORTH] isEditable automationLevel, status", event.get('automationLevel'), event.getStatus()

        if selected == 0: selected = False  
        return selected and event.get('activate', False), selected        
        
    #################################
    # Update of Event Attributes    #
    #################################
    
    def initializeEvent(self, event):
        mvCentroids = event.get('motionVectorCentroids')
        if not mvCentroids and event.getStatus() in ["PENDING", "POTENTIAL"]:
            # Initialize event polygon
            # Set start time and set eventSt_ms
            # If automated, start time is ProbSevere time as set by ConvectiveRecommender
            # Otherwise, set to latestDataLayer time
            if not event.get('geometryAutomated'):  # old: event.get('automationLevel') in ['automated', 'attributesAndGeometry']:
                self.moveStartTime(event, self.dataLayerTimeToLeft)                
            # BUG ALERT?? Is this correct?  Should we only set this to True if not 'automated' or 'attributesAndGeometry'?
            event.set('settingMotionVector', True)
            
            self.eventSt_ms = long(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()))
            centroids = [event.getGeometry().asShapely().centroid]
            st = self.probUtils.convertFeatureTime(self.eventSt_ms, 0)
            et = self.probUtils.convertFeatureTime(self.eventSt_ms, self.probUtils.timeStep())
            mvTimes = [(st, et)]
            event.set('motionVectorCentroids', centroids)
            event.set('motionVectorTimes', mvTimes)
        else:
            self.probUtils.roundEventTimes(event)
            self.eventSt_ms = long(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()))
        print "SR event start, current", self.eventSt_ms, self.currentTime
        self.flush() 

#     def setAttributesAndGeometry(self, event):
#         event.set('automationLevel', 'attributesAndGeometry')
#         manualAttrs = event.get('manualAttributes', [])  
#         manualAttrs += ['convectiveObjectDir', 'convectiveObjectSpdKts', 'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc']
#         event.set('manualAttributes', manualAttrs)
#         if event.get('objectID') and not event.get('objectID').startswith('M'):
#             event.set('objectID', 'M' + event.get('objectID'))
#         print "SR calling setActivation for setting attributesAndGeometry"
#         self.flush()
#         self.probUtils.setActivation(event)              
#         self.editableHazard, self.selectedHazard = self.isEditableSelected(event)

#     def checkUserOwned(self, event): 
#         automationLevel = event.get('automationLevel')
#         if automationLevel in ['automated', 'attributesAndGeometry']:
#             return
#         manualAttrs = event.get('manualAttrs')
#         userOwned = True
#         for attr in ['convectiveObjectDir', 'convectiveObjectSpdKts', 'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc',
#                       'duration', 'convectiveProbTrendGraph', 'convectiveWarningDecisionDiscussion']:
#              if attr not in manualAttrs:
#                 userOwned = False
#                 break
#         if not userOwned:
#              return   
#                     
#         event.set('automationLevel', 'userOwned')
#         if event.get('objectID') and not event.get('objectID').startswith('M'):
#              event.set('objectID', 'M' + event.get('objectID'))
#         print "SR calling setActivation for setting userOwned"
#         self.flush()
#         self.probUtils.setActivation(event)
#         self.editableHazard, self.selectedHazard = self.isEditableSelected(event)
            
    def adjustForFrameChange(self, event, eventSetAttrs, resultEventSet):
        # Assumption is that D2D frame change was used to get to the latest data layer
        #
        # If hazard is editable AND automationLevel is 'userOwned' or 'attributesOnly'
        #    If selected time is at latest data layer and start time < selected time:
        #       Move start time, advance polys
        #       return True
        # return False
        if self.editableHazard and not event.get('geometryAutomated'): # old: event.get('automationLevel') in ['userOwned', 'attributesOnly']:
            # BUG ALERT?? Is equality working here?
            if self.selectedTime == self.latestDataLayerTime:
                if self.eventSt_ms < self.selectedTime:
                    self.moveStartTime(event, self.latestDataLayerTime)
                    self.advanceForecastPolys(event, eventSetAttrs)
                    graphProbs = self.probUtils.getGraphProbs(event, self.latestDataLayerTime)
                    event.set('convectiveProbTrendGraph', graphProbs)
                    # This field is displayed in the Console to show if there has been a 
                    #   data layer update while the object has been being edited                    
                    event.set('dataLayerStatus', 'Synced')
                    resultEventSet.add(event)
                    return True
        return False
        
    def visualCueForDataLayerUpdate(self, event):
        # If editable:
        #    If start time is not at data layer:
        #        Set attribute 'dataLayerStatus': 'Data Layer Updated' (which will give a visual cue)
        print "SR Visual Cue eventSt, latestDataLayer", self.eventSt_ms, self.latestDataLayerTime
        self.flush()
        if self.eventSt_ms != self.latestDataLayerTime:
            event.set('dataLayerStatus', 'Updated')
            #event.set('dataLayerStatus', 'Data Layer Updated')            

    def processDataLayerUpdate(self, event, eventSetAttrs, resultEventSet):
        if not self.editableObjects:
            if self.latestDataLayerTime <= self.selectedTime:
                resultEventSet.addAttribute(SELECTED_TIME_KEY, self.latestDataLayerTime)
                print "SR Setting selected time to latestDataLayer due to time interval update", self.editableHazard, self.editableObjects
                self.flush()
        elif self.editableHazard:
            self.visualCueForDataLayerUpdate(event)
            resultEventSet.add(event)
            #resultEventSet.addAttribute(SELECTED_TIME_KEY, self.eventSt_ms)
            
            if event.get('lastSelectedTime') is not None:
                 resultEventSet.addAttribute(SELECTED_TIME_KEY, event.get('lastSelectedTime'))

    def adjustForEventModification(self, event, eventSetAttrs, resultEventSet):        
        print '\n---SR: Entering adjustForEventModification...'
        print "SR entering adjustForEventModification -- YG"
        print self.attributeIdentifiers
        self.flush()
        
        changed = False

        if 'selected' in self.attributeIdentifiers:
            return False               
        
        if 'status' in self.attributeIdentifiers:
            self.probUtils.setActivation(event, self.caveUser)
            self.editableHazard, self.selectedHazard = self.isEditableSelected(event)
            return True

        if 'resetMotionVector' in self.attributeIdentifiers: 
#              for key in ['convectiveObjectDir', 'convectiveObjectSpdKts',
#                        'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc']: 
#                  default = self.probUtils.defaultValueDict().get(key)
#                  event.set(key, default)
             event.set('settingMotionVector', True) 

             motionVectorCentroids = [event.getGeometry().asShapely().centroid]
             st = self.probUtils.convertFeatureTime(self.eventSt_ms, 0)
             et = self.probUtils.convertFeatureTime(self.eventSt_ms, self.probUtils.timeStep())
             motionVectorTimes = [(st, et)]          
             event.set('motionVectorCentroids', motionVectorCentroids) 
             event.set('motionVectorTimes', motionVectorTimes) 
#              if event.get('automationLevel') == 'automated': 
#                  self.setAttributesAndGeometry(event)
             return True        
        
        # Handle Modify Button
        if 'modifyButton' in self.attributeIdentifiers:  # User Hit modify button
            if event.getStatus() == 'ISSUED':         
                if not event.get('geometryAutomated'): # old: event.get('automationLevel') in ['userOwned', 'attributesOnly']:
                    self.moveStartTime(event, self.latestDataLayerTime, moveEndTime=True)
                    self.advanceForecastPolys(event, eventSetAttrs)
                    graphProbs = self.probUtils.getGraphProbs(event, self.latestDataLayerTime)
                    event.set('convectiveProbTrendGraph', graphProbs)
                print "SR setting activation for hitting Modify"
                self.flush()
                event.set('activate', True)
                event.set('activateModify', False)
                #self.probUtils.setActivation(event)
                self.editableHazard = True
            return True
        
        # Handle Auto Shape and other auto events
        if 'geometryAutomated' in self.attributeIdentifiers or "motionAutomated" in self.attributeIdentifiers or "probTrendAutomated" in self.attributeIdentifiers:
            # check if all three automation are there, and current status
            # to see if we need to set the new owner
            if event.get("geometryAutomated") and event.get("motionAutomated") and event.get("probTrendAutomated"):
                # automate event, no owner
                if event.get("owner", None):
                    event.set("owner", None)
                    print "SW: manual to automate, reset the owner to NONE" 
                # reset the object ID as well
                if event.get("objectID") and event.get("objectID").startswith('m'):
                    event.set("objectID", event.get('objectID')[1:])
                    
                # automate event, no need to check the owner
                #print "SW: automated event, set activation "
                #self.probUtils.setActivation(event)                                   
            else:
                # not all are automate, have to be manual
                if not event.get("owner", None):
                    event.set("owner", self.caveUser)
                    print "SW: automate to manual, set the owner to ", self.caveUser
                # manual event, need to change the object ID as well
                if event.get('objectID') and not event.get('objectID').startswith('m'):
                    event.set('objectID', 'm'+event.get('objectID'))
                    
                #print "SW: manual event, set activation"
                #self.probUtils.setActivation(event, self.caveUser)                         
            
            self.editableHazard, self.selectedHazard = self.isEditableSelected(event)            
            return True
                
        # Get Convective Attributes from the MetaData. 
        # These should supercede and update the ones stored in the event
        self.updateConvectiveAttrs(event) 
     
        # Compare previous values to the new values to determine if any changes were made.  
        # Update the previous values along the way.
        triggerCheckList = ['convectiveObjectSpdKtsUnc', 'convectiveObjectDirUnc', 'convectiveProbTrendGraph',
                            'convectiveObjectDir', 'convectiveObjectSpdKts', 'convectiveSwathPresets',
                            'duration']
        newTriggerAttrs = {t:event.get(t) for t in triggerCheckList}
        newTriggerAttrs['duration'] = self.probUtils.getDurationMinutes(event)
        
        changedAttrs = []
        
        for t in triggerCheckList:
            newVal = newTriggerAttrs.get(t)
            prevName = "prev_" + t
            prevVal = event.get(prevName)
            if t == 'convectiveProbTrendGraph':
                if newVal is None: newVal = []
                if prevVal is None: prevVal = [] 
                
            # Test to see if there is a change
            if prevVal != newVal:
                changed = True
                if t in ['convectiveObjectDir', 'convectiveObjectSpdKts',
                       'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc']:
                    self.probUtils.updateApplicationDict({t:newVal})

                
                if t == 'duration':
                    graphProbs = self.probUtils.getGraphProbs(event, self.latestDataLayerTime)
                    # LogUtils.logMessage('[1]', graphProbs)
                    event.set('convectiveProbTrendGraph', graphProbs)
                elif t == 'convectiveProbTrendGraph':
                    self.ensureLastGraphProbZeroAndUneditable(event)
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
    
    def handleAdditionalEventModifications(self, event, resultEventSet):
        print "SR- Entering handleAdditionalEventModification --YG"
        self.flush()
        if "status" in self.attributeIdentifiers or "showGrid" in self.attributeIdentifiers:
            self.setVisualFeatures(event)
            resultEventSet.add(event)
            return
        if 'cancelButton' in self.attributeIdentifiers: 
            print "SR Setting to Elapsed"
            self.flush()
            event.setStatus('ELAPSED')
            event.set('statusForHiddenField', 'ELAPSED')
            resultEventSet.add(event)
            self.saveToHistory = True
            self.keepLocked = False
            return        

    def ensureLastGraphProbZeroAndUneditable(self, event):
        probVals = event.get('convectiveProbTrendGraph', [])
        if len(probVals) == 0:
            return
        lastPoint = probVals[-1]
        if lastPoint["y"] != 0 or lastPoint["editable"]:
            probVals[-1] = { "x": lastPoint["x"], "y": 0, "editable": False }
            
        event.set('convectiveProbTrendGraph', probVals)
    
    def updateConvectiveAttrs(self, event):
        print "Entering updateConvectiveAttrs... YG "
        self.flush()
        
        convectiveAttrs = event.get('convectiveAttrs')
        if not convectiveAttrs:
            return
        if 'wdir' in convectiveAttrs:
            event.set('convectiveObjectDir', convectiveAttrs['wdir'])
        if 'wspd' in convectiveAttrs:
            event.set('convectiveObjectSpdKts', convectiveAttrs['wspd'])
            
    def moveStartTime(self, event, startMS, moveEndTime=True):
        print "SR moving start time to", self.probUtils.displayMsTime(startMS)
        self.flush()
        newStart = datetime.datetime.utcfromtimestamp(startMS / 1000)
        # if abs((newStart - event.getStartTime()).total_seconds()*1000) > self.probUtils.timeDelta_ms():
        #    return False     
        if moveEndTime or self.pendingHazard:
            durationSecs = self.probUtils.getDurationSecs(event)
            endMS = startMS + (durationSecs * 1000)
            event.setEndTime(datetime.datetime.utcfromtimestamp(endMS / 1000))
        event.setStartTime(newStart)
        self.probUtils.roundEventTimes(event)
        self.eventSt_ms = long(TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()))
        self.movedStartTime = True
        return True
                
    ##############################################
    # Forecast bookkeeping                     #
    ##############################################    
                        
    def advanceForecastPolys(self, event, eventSetAttrs):
        ''' 
        Move forecastPolys to pastPolys
        Reset start time poly
        ''' 
        ####### 
        # Forecast Polys
        forecastPolys = event.get('forecastPolys', [])
        forecastTimes = event.get('forecastTimes', [])
        print 'SR Advancing forecastTimes', forecastTimes
        self.flush()
        if not forecastPolys or not forecastTimes:
            return
        index = 0
        for i in range(len(forecastTimes)):           
            times = forecastTimes[i]
            if times is None:
                print "SwathRecommender Warning: forecast times None", forecastTimes
                self.flush()
                return
            
            st, et = forecastTimes[i]
            if st >= self.latestDataLayerTime:
                print "SR Advancing to ", i, self.probUtils.displayMsTime(st), self.probUtils.displayMsTime(self.latestDataLayerTime)    
                self.flush()
                if i > 0:
                    index = i-1
                else:
                    index = 0
                break
                
        # Reset geometry to new interior start time shape
        geometry = event.getGeometry()
        centroid = forecastPolys[index].asShapely().centroid
        newGeometry = self.probUtils.reduceShapeIfPolygon(AdvancedGeometry.createRelocatedShape(geometry, centroid))
        event.setGeometry(newGeometry) 
        
        event.set('forecastPolys', forecastPolys[index:])
        event.set('forecastTimes', forecastTimes[index:])
                             
        ######
        #  Past Polys        
        pastPolys = event.get('pastPolys', [])
        pastTimes = event.get('pastTimes', [])
        newPastPolys = pastPolys + forecastPolys[:index]
        newPastTimes = pastTimes + forecastTimes[:index]
        
        # Truncate pastPolys if there is a maximum limit set
        maxPast = self.maxPastPolygons()
        if maxPast and len(newPastPolys) > maxPast:
            index = len(newPastPolys) - maxPast
            newPastPolys = newPastPolys[index:]
            newPastTimes = newPastTimes[index:]
        print "SR advance pastTimes", newPastTimes
        self.flush()
        event.set('pastPolys', newPastPolys)
        event.set('pastTimes', newPastTimes)
      
    def setEventGeometry(self, event, shape):
        '''
        @summary Set the event geometry to that specified.
        @param event Event to be set.
        @param shape Shape in advanced geometry form.
        '''
        shape = self.probUtils.reduceShapeIfPolygon(shape)
        event.setGeometry(shape)

    ##############################################
    # Upstream / Forecast set up               #
    ##############################################    
 
    def getIntervalPolys(self, event, eventSetAttrs, timeDirection='forecast'):
        timeIntervals = []
        if timeDirection == 'upstream':
            # Follow dataLayerTimes for upstream polys.
            # Do not include the latest data layer time. 
            # DataLayerTimes are in ms past the epoch
            # Convert to secs relative to eventSt_ms
            for dlTime in self.dataLayerTimes[:-1]:
                secs = int((dlTime - self.eventSt_ms) / 1000)
                timeIntervals.append(secs)
                if abs(secs) > self.upstreamTimeLimit()*3600:
                    break
        else:
            # Follow timeStep for forecast polys
            durationSecs = self.probUtils.getDurationSecs(event, truncateAtZero=True)
            timeStep = self.probUtils.timeStep()
            i = 0
            while i <= durationSecs:
                timeIntervals.append(i)
                # If object start time is Prob Severe time, put first forecast polygon on the dataLayerTime
                if i == 0 and event.get('geometryAutomated') and event.get('motionAutomated') and event.get('probTrendAutomated') and self.latestDataLayerTime > self.eventSt_ms:
                     # old: event.get('automationLevel') not in ['userOwned', 'attributesOnly', 'attributesAndGeometry'] \
                     # NOTE:  We're assuming this meant:  event.get('automationLevel') == 'automated'
                   #and self.latestDataLayerTime > self.eventSt_ms:
                    i += int((self.latestDataLayerTime - self.eventSt_ms) / 1000)
                else:
                    i += timeStep
                    timeStep = self.probUtils.timeStep(i/60)
        print "SR getIntervalPolys", timeDirection, timeIntervals
        self.flush()
        if timeIntervals:
            self.probUtils.createIntervalPolys(event, eventSetAttrs, SwathPreset(),
                                             self.eventSt_ms, timeIntervals, timeDirection)
    
    ###############################
    # Visual Features and Nudging #
    ###############################  
      
    def adjustForVisualFeatureChange(self, event, eventSetAttrs):
        '''
        Based on the visual feature change, update the motion vector
                      
        Update event geometry i.e. polygon at event start time, if changed        
        '''
        print "SR entering adjustForVisualFeatureChange --YG"
        print "SR AdjustforVisualFeature editable", self.editableHazard
        self.flush()
        if not self.editableHazard:
            return False
            
        # Add the visualFeature change to the motion vector centroids       
        motionVectorCentroids = event.get('motionVectorCentroids', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
            
        features = event.getVisualFeatures()
        if not features: features = []

        # We are only changing one visual feature per SwathRecommender execution
        # because only one visual feature is editable at any one time.
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
                # Add the feature to the motionVectorCentroids 
                print "SR adjustForVF before updateShapes -- motionVectorCentroids", motionVectorCentroids, motionVectorTimes
                print "SR adjustForVF before updateShapes -- featureSt", featureSt
                self.flush()
                motionVectorCentroids, motionVectorTimes = self.updateShapes(motionVectorCentroids, motionVectorTimes,
                             featurePoly, featureSt, featureEt, centroid=True, limit=self.maxMotionVectorCentroids())
                print "SR adjustForVF after updateShapes -- motionVectorCentroids", motionVectorCentroids
                self.flush()
                event.set('motionVectorCentroids', motionVectorCentroids)
                event.set('motionVectorTimes', motionVectorTimes)
                                                         
                # If the feature is at the event start time, add it to the 
                #  event geometry
                if abs(featureSt - self.eventSt_ms) <= self.probUtils.timeDelta_ms():
                    event.setGeometry(featurePoly)
        
        if len(motionVectorCentroids) <= 1:
            return True
                                      
        # Re-compute the motion vector and uncertainty
        motionVectorTuples = []
        for i in range(len(motionVectorCentroids)):
            poly = AdvancedGeometry.createRelocatedShape(event.getGeometry(), motionVectorCentroids[i])
            st, et = motionVectorTimes[i]
            motionVectorTuples.append((poly, st, et))
            print 'SR motionVector Poly, startTime:', poly.asShapely().centroid, self.probUtils.displayMsTime(st)
            print 'SR    ', poly.asShapely()
            self.flush()
            
        # print "SR motionVectorTuples", len(motionVectorTuples)
        # self.flush()
        
        newMotion = self.probUtils.computeMotionVector(motionVectorTuples, self.eventSt_ms) 
        updateDict = {}
        for key in ['convectiveObjectDir', 'convectiveObjectSpdKts', 'convectiveObjectDirUnc', 'convectiveObjectSpdKtsUnc']:
            value = int(newMotion.get(key))
            # Save new motion vector to Application Dictionary
            if key in ['convectiveObjectDir', 'convectiveObjectSpdKts']:
                updateDict[key] = value           
            event.set(key, value)
        print "SR updateApplicationDict adjust", updateDict
        self.flush()
        self.probUtils.updateApplicationDict(updateDict)
            
        return True

    def updateShapes(self, shapes, times, newShape, newSt, newEt, centroid=False, limit=None):
        # Add the newPoly to the list of shapes and times
        #  The shapes could be centroids or geometry shapes
        # If one currently exists at the newSt, replace it
        # Keep the lists in time order
        # If limit, then add the new poly and then truncate the list to the 
        #  given limit e.g. we may only want 10 past polygons or 2 motion vector polygons
    
        # Convert to tuples
        tuples = []
        for i in range(len(shapes)):
            shape = shapes[i]
            st, et = times[i]
            tuples.append((shape, st, et))

        newTuples = []
        found = False
        if centroid:
            newShape = newShape.asShapely().centroid
        for shape, st, et in tuples:
            if abs(st - newSt) <= self.probUtils.timeDelta_ms():                        
                newTuples.append((newShape, st, et))
                found = True
            else: 
                newTuples.append((shape, st, et))
        # Otherwise, add a new shape 
        if not found:                    
            newTuples.append((newShape, newSt, newEt))
 
        newTuples.sort(self.sortShapes) 
        # CHECK THIS -- for Motion Vector may have to keep last one added to replace it
        if limit and len(newTuples) > limit:
            newTuples = newTuples[:limit]
        newShapes = [shape for shape, st, et in newTuples]
        newTimes = [(st, et) for shape, st, et in newTuples]            
        return newShapes, newTimes         
 
    def setVisualFeatures(self, event):
        print self.logMessage("Setting Visual Features")
        self.flush()

        forecastPolys = event.get('forecastPolys')
        if not forecastPolys:
            return
        
        features = []
        startTime_ms = self.dataLayerTimes[0]
        featuresDisplay = self.featuresDisplay()
        
        # Forecast Features -- polygons, track points, relocated dashed, last motionVector
        features += self.forecastVisualFeatures(event, startTime_ms)
        
        # Swath
        if featuresDisplay.get('swath'):
            swathFeature = self.swathFeature(event, startTime_ms, forecastPolys)                
            features.append(swathFeature) 
        
        # Motion vector centroids
        if featuresDisplay.get('motionVectorCentroids'):
            features += self.motionVectorFeatures(event, startTime_ms)     
        
        # Previous Time Features
        features += self.previousTimeVisualFeatures(event)
        
        # Preview Grid
        if featuresDisplay.get('previewGrid'):
            features += self.getPreviewGridFeatures(event, startTime_ms)
                           
        if features:
            event.setVisualFeatures(VisualFeatures(features))
            
        if self.printVisualFeatures:
             self.printFeatures(event, "Visual Features", features)
        
    def forecastVisualFeatures(self, event, startTime_ms):
        print "SR -- entering forecastVisualFeatures --YG"
        self.flush()
        forecastPolys = event.get('forecastPolys')
        if not forecastPolys:
            return
        forecastTimes = event.get('forecastTimes')
        featuresDisplay = self.featuresDisplay()
        
        geometry = event.getGeometry()
                
        features = []
        # Forecast Polygons, Track Points, Relocated Forecast 
        numIntervals = len(forecastPolys)
        
        # Prob Label
        probTrendValues = self.probUtils.getInterpolatedProbTrendColors(event)
        self.label = str(event.get('objectID')) + " " + event.getHazardType()
        
        # Border thickness
        if self.selectedHazard:
            borderThickness = 'eventType'
        else:
            borderThickness = 4

        startTimeShapeFound = False
        firstForecastSt_ms = self.eventSt_ms

        print "SR Forecast Visual Features  eventSt_ms", self.probUtils.displayMsTime(self.eventSt_ms), self.eventSt_ms
        #print "SR editable, automationLevel", self.editableHazard and event.get('automationLevel') in ['userOwned', 'attributesOnly'], event.get('automationLevel')
        self.flush()
           
        for i in range(numIntervals):
            poly = forecastPolys[i]
            polySt_ms, polyEt_ms = forecastTimes[i]

            if i <= 1:
                print "SR forecast i, polySt, eventSt", i, self.probUtils.displayMsTime(polySt_ms), polySt_ms, polySt_ms == self.eventSt_ms
                self.flush()
            if polySt_ms == self.eventSt_ms:
                startTimeShapeFound = True
            
            if i == 0:
                firstForecastSt_ms = polySt_ms                
            try: 
                probTrend = int(probTrendValues[i])
                probStr = ' ' + `probTrend` + '%'
            except:
                probStr = ''
            label = self.label + probStr

              
            forecastFeature = {
              "identifier": "swathRec_forecast_" + str(polySt_ms),
              "visibilityConstraints": "always",
              "borderColor": "eventType",
              "borderThickness": borderThickness,
              "borderStyle": "eventType",
              "bufferColor": self.BUFFER_COLOR,
              "bufferThickness": self.BUFFER_THICKNESS,
              "textSize": "eventType",
              "label": label,
              "textColor": "eventType",
              "dragCapability": "none",
              "scaleable": False,
              "rotatable": False,
              "geometry": {
                   (polySt_ms, polyEt_ms): poly
                  }
                }
            if featuresDisplay.get('forecastPolys'):
                features.append(forecastFeature)
               
            # Track Points self.probUtils.displayMsTime(st)
            centroid = poly.asShapely().centroid
            color = self.probUtils.getInterpolatedProbTrendColor(event, i, numIntervals)
            trackPointFeature = {
              "identifier": "swathRec_trackPoint_" + str(polySt_ms),
              "visibilityConstraints": "selected",
              "borderColor": color,
              "borderThickness": 2,
              "diameter": 5,
              "geometry": {
                  (startTime_ms,
                   TimeUtils.datetimeToEpochTimeMillis(event.getEndTime()) + 1000):
                   AdvancedGeometry.createShapelyWrapper(centroid, 0)
               }
            }
            if featuresDisplay.get('trackPoints'):
                features.append(trackPointFeature)
             
            # Dashed relocated shape and centroid show up if editable 
            dragCapability = 'none'
            editable = False                
            relocatedShape = self.probUtils.reduceShapeIfPolygon(AdvancedGeometry.
                                                                   createRelocatedShape(geometry, centroid))    
            if polySt_ms == self.eventSt_ms:
                print "SR ======================= editableHazard ", self.editableHazard
                if self.editableHazard and not event.get('geometryAutomated'): # old: event.get('automationLevel') in ['userOwned', 'attributesOnly']:
                    dragCapability = 'all'
                    editable = True 
                    print "SR relocatedShape, editable -YG ", self.eventSt_ms
                    self.flush()
              
            relocatedFeature = {
              "identifier": "swathRec_relocated_" + str(polySt_ms),
              "visibilityConstraints": "selected",
              "borderColor": "eventType",
              "borderThickness": "eventType",
              "borderStyle": "dashed",
              "bufferColor": self.BUFFER_COLOR,
              "bufferThickness": self.BUFFER_THICKNESS,
              "textSize": "eventType",
              "dragCapability": dragCapability,
              "scaleable": editable,
              "rotatable": editable,
              "editableUsingGeometryOps": editable,
              "useForCentering": True,
              "geometry": {
                  (polySt_ms, polyEt_ms): relocatedShape
                   }
            }
            #print "SR Test I 34 -- dashed polys", featuresDisplay.get('dashedPolys'),  self.selectedHazard,  event.get('automationLevel') in ['userOwned', 'attributesOnly']
            #self.flush()
            if featuresDisplay.get('dashedPolys') and self.selectedHazard and not event.get('geometryAutomated'): # old: event.get('automationLevel') in ['userOwned', 'attributesOnly']:
                features.append(relocatedFeature)
            

            centroidFeature = {
              "identifier": "swathRec_relocatedCentroid_" + str(polySt_ms),
              "visibilityConstraints": "selected",
              "borderColor": { "red": 0, "green": 0, "blue": 0 },
              "borderThickness": 2,
              "fillColor": { "red": 1, "green": 1, "blue": 1},
              "diameter": 6,
               "geometry": {
                  (polySt_ms, polyEt_ms): AdvancedGeometry.createShapelyWrapper(centroid, 0)
                   }
            }
            if featuresDisplay.get('dashedPolyCentroid') and self.selectedHazard:
               features.append(centroidFeature)
                   
        # Start time may be prior to first forecast shape and we need to display it
        if not startTimeShapeFound:
            print "****SR startTimeShape not found -- attempt to use geometry"
            self.flush()
            if self.editableHazard and not event.get('geometryAutomated'): # old: event.get('automationLevel') in ['userOwned', 'attributesOnly']:
                #print "SR forecast poly equal to start time -- setting editable"
                print "SR setting editable...YG "
                self.flush()
                dragCapability = 'all'
                editable = True
            else:
                dragCapability = 'none'
                editable = False
            # print "SR dragCapability", dragCapability
            self.flush()
                
            polySt_ms = self.eventSt_ms
            polyEt_ms = firstForecastSt_ms
            try: 
                probTrend = int(probTrendValues[0])
                probStr = ' ' + `probTrend` + '%'
            except:
                probStr = ''
            label = self.label + probStr

            startTimeFeature = {
              "identifier": "swathRec_forecast_" + str(polySt_ms),
              "visibilityConstraints": "always",
              "borderColor": "eventType",
              "borderThickness": borderThickness,
              "borderStyle": "eventType",
              "bufferColor": self.BUFFER_COLOR,
              "bufferThickness": self.BUFFER_THICKNESS,
              "textSize": "eventType",
              "label": label,
              "textColor": "eventType",
              "dragCapability": dragCapability,
              "scaleable": editable,
              "rotatable": editable,
              "geometry": {
                   (polySt_ms, polyEt_ms): geometry
                  }
                }
            if featuresDisplay.get('startTimeShape'):
               features.append(startTimeFeature)
               
            # centroid
            centroid = poly.asShapely().centroid
            color = self.probUtils.getInterpolatedProbTrendColor(event, 0, numIntervals)
            trackPointFeature = {
              "identifier": "swathRec_trackPoint_" + str(polySt_ms),
              "visibilityConstraints": "selected",
              "borderColor": color,
              "borderThickness": 2,
              "diameter": 5,
              "geometry": {
                  (startTime_ms,
                   TimeUtils.datetimeToEpochTimeMillis(event.getEndTime()) + 1000):
                   AdvancedGeometry.createShapelyWrapper(centroid, 0)
               }
            }
            if featuresDisplay.get('trackPoints'):
                features.append(trackPointFeature)    
        return features

    def swathFeature(self, event, startTime_ms, forecastPolys):
        advancedGeometries = forecastPolys
        forecastPolys = []
        for geometry in advancedGeometries:
            forecastPolys.append(geometry.asShapely())
        envelope = shapely.ops.cascaded_union(forecastPolys)
        swath = {
              "identifier": "swathRec_swath",
              "visibilityConstraints": "selected",
              "borderColor": { "red": 1, "green": 1, "blue": 0 },
              "borderThickness": 3,
              "borderStyle": "dotted",
              "bufferColor": self.BUFFER_COLOR,
              "bufferThickness": self.BUFFER_THICKNESS,
              "geometry": {
                  (startTime_ms,
                   TimeUtils.datetimeToEpochTimeMillis(event.getEndTime()) + 1000):
                   AdvancedGeometry.createShapelyWrapper(envelope, 0)
               }
              }
        return swath

    def motionVectorFeatures(self, event, startTime_ms):
        # Show current (time=0) object centroid when both initiating and modifying issued objects.
        features = [] 
        motionVectorCentroids = event.get('motionVectorCentroids', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
        for i in range(len(motionVectorCentroids)):
            st, et = motionVectorTimes[i]                        
            centroid = AdvancedGeometry.createShapelyWrapper(motionVectorCentroids[i], 0)
            feature = {
              "identifier": "swathRec_motionVector_" + str(st),
              "visibilityConstraints": "selected",
              "borderColor": { "red": 0, "green": 0, "blue": 0 },
              "borderThickness": 2,
              "fillColor": { "red": 1, "green": 1, "blue": 1},
              "diameter": 6,
              "geometry": {
                  (startTime_ms,
                   TimeUtils.datetimeToEpochTimeMillis(event.getEndTime()) + 1000):
                   centroid
               }
            }
            features.append(feature)
        return features

        
    def previousTimeVisualFeatures(self, event):    
        # Previous time polygons -- prior to current time
        #
        # If time has marched forward from the initial creation of the hazard, we could have
        #  motionVectorPolys, pastPolys, and upstreamPolys (IF still pending)
        #
        # Choose to use in this order of preference:
        #      motionVector polygon
        #      then the past polygon
        #      then if the hazard is editable
        #         the upstream polygon
        #
        # All are editable if the hazard has not been issued yet.
        # 
        # print "\nDoing Previous time polys"
        pastPolys = event.get('pastPolys', [])
        pastTimes = event.get('pastTimes', [])
        motionVectorCentroids = event.get('motionVectorCentroids', []) 
        motionVectorTimes = event.get('motionVectorTimes', [])
        upstreamPolys = event.get('upstreamPolys', []) 
        upstreamTimes = event.get('upstreamTimes', [])
        previousFeatures = []
        
        if self.editableHazard and event.get('settingMotionVector'):
            resettingMotionVector = True
        else:
            resettingMotionVector = False
        print 'SR previousTimeVisualFeatures resettingMotionVector, pastTimes', resettingMotionVector, pastTimes
        self.flush()
                        
        # Don't want to show previous polys for times at or after eventSt
        for i in range(len(self.dataLayerTimes[:-1])):
            polySt_ms = self.dataLayerTimes[i]
            if polySt_ms >= self.eventSt_ms:
                continue
            
            polyEt_ms = self.dataLayerTimes[i + 1]
            # Don't want to overlap with start time polygon
            if polyEt_ms > self.eventSt_ms:
                polyEt_ms = self.eventSt_ms
#             if not resettingMotionVector:
#                 print "SR previous st, et", self.probUtils.displayMsTime(polySt_ms), self.probUtils.displayMsTime(polyEt_ms)
#                 print "pastTimes", pastTimes
#                 for st,et in pastTimes:
#                     print "      ", self.probUtils.displayMsTime(st), self.probUtils.displayMsTime(et)  
#                 self.flush()
            
            # If resetting motion vector, display the motion vector and upstream polys 
            if resettingMotionVector:
                poly, polyType = self.findPreviousPoly(event, polySt_ms,
                                          motionVectorCentroids, motionVectorTimes,
                                          [], [], upstreamPolys, upstreamTimes)
                if not poly:
                    continue
                dragCapability = 'whole'
                color = 'eventType'
                if polyType == 'upstream':  # yellow
                    color = { "red": 1, "green": 1, "blue": 0 }
                elif polyType == 'motionVector':  # purple / dark pink
                    color = { "red": 1, "green": 0, "blue": 1 }

                # Display previous time shape                         
                previousFeature = {              
                  "identifier": "swathRec_previous_" + str(polySt_ms),
                  "visibilityConstraints": "selected",
                  "borderColor":  color,  
                  "borderThickness": "eventType",
                  "borderStyle": "eventType",
                  "bufferColor": self.BUFFER_COLOR,
                  "bufferThickness": self.BUFFER_THICKNESS,
                  "dragCapability": dragCapability,
                  "textSize": "eventType",
                  "label": self.label,
                  "textColor": "eventType",
                  "geometry": {
                      (polySt_ms, polyEt_ms): poly
                   }
                  }
                previousFeatures.append(previousFeature)  
                
            # Display the past polygons if selected and not resetting motion vector (could be editable)
            # BUG ALERT?? May need to only display past IF we are selected AND not editable
            else:          
                if not pastTimes:
                    continue
                poly, polyType = self.findPreviousPoly(event, polySt_ms,
                                          [], [], pastPolys, pastTimes, [], []) 
                if not poly:
                    continue
                #print "SR Adding past poly for time", self.probUtils.displayMsTime(polySt_ms), poly
                #self.flush()

                # 'past': # blue green
                color = { "red": 0, "green": 1, "blue": 1 }
                previousFeature = {              
                  "identifier": "swathRec_previous_" + str(polySt_ms),
                  "visibilityConstraints": "always",
                  "borderColor":  color,   
                  "borderThickness": "eventType",
                  "borderStyle": "eventType",
                  "bufferColor": self.BUFFER_COLOR,
                  "bufferThickness": self.BUFFER_THICKNESS,
                  "dragCapability": 'none',
                  "textSize": "eventType",
                  "label": self.label,
                  "textColor": "eventType",
                  "geometry": {
                      (polySt_ms, polyEt_ms): poly
                   }
                  }
                previousFeatures.append(previousFeature)              
            
        return previousFeatures


    def findPreviousPoly(self, event, polySt_ms, motionVectorCentroids, motionVectorTimes, pastPolys, pastTimes,
                          upstreamPolys, upstreamTimes):
        featuresDisplay = self.featuresDisplay()
        if featuresDisplay.get('motionVectorCentroids'):
            for i in range(len(motionVectorCentroids)):
                st, et = motionVectorTimes[i]
                #print "SR  difference ", self.probUtils.displayMsTime(st), self.probUtils.displayMsTime(polySt_ms), abs(st - polySt_ms), self.probUtils.timeDelta_ms()
                #self.flush()
                if abs(st - polySt_ms) < self.probUtils.timeDelta_ms():
                    print "SR previous using motion vector", i, self.probUtils.displayMsTime(st)
                    self.flush()
                    shape = AdvancedGeometry.createRelocatedShape(event.getGeometry(), motionVectorCentroids[i])
                    return shape, 'motionVector'
        if featuresDisplay.get('pastPolys'):
            for i in range(len(pastPolys)):
                st, et = pastTimes[i]
                #print 'SR find previous testing', self.probUtils.displayMsTime(st), abs(st-polySt_ms), self.probUtils.timeDelta_ms()
                #self.flush()
                if abs(st - polySt_ms) < self.probUtils.timeDelta_ms():
                    #print "SR previous using past", i, self.probUtils.displayMsTime(st)
                    #self.flush()
                    return pastPolys[i], 'past'
        if featuresDisplay.get('upstreamPolys'):
            for i in range(len(upstreamPolys)):
                st, et = upstreamTimes[i]
                if abs(st - polySt_ms) < self.probUtils.timeDelta_ms():
                    #print "SR previous upstream poly", self.probUtils.displayMsTime(st)
                    #self.flush()
                    return upstreamPolys[i], 'upstream'
        return None, 'none'

    def getPreviewGridFeatures(self, event, upstreamSt_ms):
        if not event.get('showGrid'): 
            return []
        gridFeatures = []            
        probGrid, lons, lats = self.probUtils.getProbGrid(event)
        polyTupleDict = self.createPolygons(probGrid, lons, lats)
                
#        print '\n\n%%%%%%%'
#        print 'SR - probGrid, lons, lats', probGrid, lons, lats
#        print 'SR - polyTupleDict', polyTupleDict
                
        # Generate and add preview-grid-related visual features        
        for key in sorted(polyTupleDict): 
            poly = polyTupleDict[key]
            
                                
            # ## Should match PHI Prototype Tool
            colorFill = {
                '0': { "red": 102 / 255.0, "green": 224 / 255.0, "blue": 102 / 255.0, "alpha": 0.4 },
                '20': { "red": 255 / 255.0, "green": 255 / 255.0, "blue": 102 / 255.0, "alpha": 0.4 },
                '40': { "red": 255 / 255.0, "green": 179 / 255.0, "blue": 102 / 255.0, "alpha": 0.4 },
                '60': { "red": 255 / 255.0, "green": 102 / 255.0, "blue": 102 / 255.0, "alpha": 0.4 },
                '80': { "red": 255 / 255.0, "green": 102 / 255.0, "blue": 255 / 255.0, "alpha": 0.4 }
                }
            
            
            if poly.is_valid:
                gridPreviewPoly = {
                    "identifier": "gridPreview_" + key,
                    "visibilityConstraints": "selected",
                    "borderColor": { "red": 0, "green": 0, "blue": 0 },  # colorFill[key],
                    "fillColor": colorFill[key],
                    "bufferColor": self.BUFFER_COLOR,
                    "bufferThickness": self.BUFFER_THICKNESS,
                    "geometry": {
                        (upstreamSt_ms,
                        # (TimeUtils.datetimeToEpochTimeMillis(event.getStartTime()), 
                         TimeUtils.datetimeToEpochTimeMillis(event.getEndTime()) + 1000):
                                 AdvancedGeometry.createShapelyWrapper(poly, 0)
                    }
                }
                
                gridFeatures.append(gridPreviewPoly)
                            
        return gridFeatures
    
    def createPolygons(self, probGrid, lons, lats):
        polyDict = {}
        
        levels = np.linspace(0, 100, 6)

        X, Y = np.meshgrid(lons, lats)
        plt.figure()
        
        CS = plt.contour(X, Y, probGrid, levels=levels)

        prob = ['0', '20', '40', '60', '80']
        probIndex = [0.0, 20.0, 40.0, 60.0, 80.0]
        polyTupleDict = {}

        for c in range(0, (len(CS.levels) - 1)):
            contourVal = CS.levels[c]
            coords = CS.collections[c].get_paths()

            polygons = []
            for coord in coords:
                v = coord.vertices
                x = v[:, 0]
                y = v[:, 1]
                poly = GeometryFactory.createPolygon([(i[0], i[1]) for i in zip(x, y)])
                polygons.append(poly)
                
            mp = GeometryFactory.createMultiPolygon(polygons, 'polygons')
            if contourVal in probIndex and len(polygons):    
                    polyTupleDict[prob[c]] = mp
                
        return polyTupleDict

    ###############################
    # Helper methods              #
    ###############################
            
    def sortShapes(self, p1, p2):
        # Sort shape tuples by start time
        poly1, st1, et1 = p1
        poly2, st2, et2 = p2
        if st1 < st2:
            return -1
        if st2 < st1:
            return 1
        return 0
            
    def printFeatures(self, event, label, features):
        print label, event.getEventID(), " = ", str(len(features)), ' ----'
        self.flush()
        # for feature in features:
        #    print feature

    def printEventSet(self, label, eventSet, eventLevel=1):            
        print label
        if self.printEventSetAttributes:
            if isinstance(eventSet, list):
                print "No values in EventSet"
                return
                
            eventSetAttrs = eventSet.getAttributes()
            print "trigger: ", eventSetAttrs.get("trigger")
            print "eventIdentifiers: ", eventSetAttrs.get("eventIdentifiers")
            print "origin: ", eventSetAttrs.get("origin")
            print "attributeIdentifiers: ", eventSetAttrs.get("attributeIdentifiers")
            if self.selectedTime is not None:
                print SELECTED_TIME_KEY, self.probUtils.displayMsTime(self.selectedTime)
        if eventLevel:
            print "Events:", len(eventSet.getEvents())
            for event in eventSet:
                self.printEvent(None, event, eventLevel)         

    def printEvent(self, label, event, eventLevel=1):
        if self.printEvent:
            import pprint
            if label: print label
            if eventLevel >= 1:
                print 'ID:', event.getEventID(), event.get('objectID')
                print "start, end", event.getStartTime(), event.getEndTime()
                #print "automationLevel", event.get('automationLevel')
                print 'automation -- geometry, motion, probTrend:', event.get('geometryAutomated'), event.get('motionAutomated'), event.get('probTrendAutomated'), 
                print 'settingMotionVector', event.get('settingMotionVector')
                print 'dataLayerStatus', event.get('dataLayerStatus')                
                # print "visual Features"                            
                # for visualFeature in event.getVisualFeatures():
                #    print "     ", str(visualFeature.get('identifier'))

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
        
    def logMessage(self, *args):
        import inspect, os
        s = ", ".join(str(x) for x in list(args))
        fName = os.path.basename(inspect.getfile(inspect.currentframe()))
        lineNo = inspect.currentframe().f_back.f_lineno
        return '\t**** [' + str(fName) + ' // Line ' + str(lineNo) + ']:' + s


    #########################################
    # ## OVERRIDES

    
    def upstreamTimeLimit(self):
        # Number of minutes backward in time for upstream polygons
        return 30 
    
    def elapsedTimeLimit(self):
        # Number of milliseconds past the end time to elapse an object
        return 10 * 60000  # 60 minutes

    def maxPastPolygons(self):
        # Maximum number of past polygons to store
        # Older polygons are dropped off as time progresses
        return None
        
    def maxMotionVectorCentroids(self):
        # Maximum number of motion vector centroids
        # If None, then unlimited
        return None
                        
    def setPrintFlags(self):
        self.printVisualFeatures = False
        self.printEventSetAttributes = True
        self.printEventSetEvents = True
        self.printEvents = True
        
    def featuresDisplay(self):
        return {
               'upstreamPolys': True,
               'forecastPolys': True,
               'startTimeShape': True,
               'trackPoints': True,
               'dashedPolys': True,
               'dashedPolyCentroids': True,
               'motionVectorCentroids': True,
               'pastPolys': True,
               'swath': True,
               'basePoly': True,
               'previewGrid': True,
               }
        
