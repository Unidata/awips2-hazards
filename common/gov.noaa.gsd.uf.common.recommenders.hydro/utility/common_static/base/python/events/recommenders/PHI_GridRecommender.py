'''
PHI Grid recommender for probabilistic hazard types.
'''
import datetime, math
import EventFactory, EventSetFactory, GeometryFactory
import RecommenderTemplate
import logging, UFStatusHandler

import math, time
import shapely
import shapely.ops as so
import shapely.geometry as sg
import shapely.affinity as sa
from inspect import currentframe, getframeinfo
import os, sys
import numpy as np
import matplotlib
#matplotlib.use("Agg")
from matplotlib import path as mPath
from scipy import ndimage
from scipy.io import netcdf

from SwathRecommender import Recommender as SwathRecommender
from ProbUtils import ProbUtils
import RiverForecastUtils
import JUtil
     
### FIXME: set path in configuration somewhere
OUTPUTDIR = '/scratch/PHIGridTesting'
### FIXME: need better (dynamic or configurable) way to set domain corner
#buff = 1.
#lonPoints = 1200
#latPoints = 1000
#ulLat = 44.5 + buff
#ulLon = -104.0 - buff

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('PHIGridRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'PHIGridRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        
        #self._ProbUtils = ProbUtils()
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'PHI Grid Recommender'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['includeEventTypes'] = [ "Prob_Severe", "Prob_Tornado" ]
        
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
        
        # For now, just print out a message saying this was run.
        import sys
        sys.stderr.write("Running PHI grid recommender.\n    trigger:    " +
                         str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
                         str(eventSet.getAttribute("eventType")) + "\n    origin:     " + 
                         str(eventSet.getAttribute("origin")) + "\n    hazard ID:  " +
                         str(eventSet.getAttribute("eventIdentifier")) + "\n    attribute:  " +
                         str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
        sys.stderr.flush()
                
        ProbUtils().processEvents(eventSet, writeToFile=True)

#===============================================================================
#         siteID = eventSet.getAttributes().get('siteID')
#         mode = eventSet.getAttributes().get('hazardMode', 'PRACTICE').upper()
#         databaseEvents = HazardDataAccess.getHazardEventsBySite(siteID, mode)
#         filteredDBEvents = []
# 
#         thisEventSetIDs = [evt.getEventID() for evt in eventSet]
#         print 'Prob_Convective Product Generator -- DBEvents:'
#         for evt in databaseEvents:
#             if evt.getStatus().lower() in ["elapsed", "ending", "ended"]:
#                 continue
#             if evt.getEventID() not in thisEventSetIDs:
#               filteredDBEvents.append(evt)  
# 
#         eventSet.addAll(filteredDBEvents)
#         eventSet.addAttribute('issueTime', datetime.datetime.utcfromtimestamp(self._issueTime/1000))
# 
# 
#         pu = ProbUtils()
#         
#         pu.processEvents(eventSet, writeToFile=True)
#===============================================================================


        return 
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()


            
def __str__(self):
    return 'PHI Grid Recommender'

