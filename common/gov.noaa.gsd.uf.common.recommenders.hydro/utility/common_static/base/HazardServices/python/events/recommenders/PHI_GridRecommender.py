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
import pickle
import AdvancedGeometry
import HazardDataAccess
     
class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('PHIGridRecommender')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'PHIGridRecommender', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

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
#         sys.stderr.write("Running PHI grid recommender.\n    trigger:    " +
#                          str(eventSet.getAttribute("trigger")) + "\n    event type: " + 
#                          str(eventSet.getAttribute("eventType")) + "\n    origin:     " + 
#                          str(eventSet.getAttribute("origin")) + "\n    hazard ID:  " +
#                          str(eventSet.getAttribute("eventIdentifiers")) + "\n    attribute:  " +
#                          str(eventSet.getAttribute("attributeIdentifiers")) + "\n")
#         sys.stderr.flush()
                
        ProbUtils().processEvents(eventSet, writeToFile=True)
        self.storeIssuedHazards(eventSet)

        return 
    
    def storeIssuedHazards(self,probHazardEvents):
        pu = ProbUtils()

        ## Dump just this event to disk since only one hazard in events set?
        attrKeys = ['site', 'status', 'phenomenon', 'significance', 'subtype',
                    'creationtime', 'endtime', 'starttime', 'geometry', 'eventid',
                    'username', 'workstation', 'attributes']
        outDict = {}
        
        issuedTime = datetime.datetime.utcfromtimestamp(probHazardEvents.getAttributes().get("currentTime")/1000).strftime('%m%d%Y_%H%M')
        caveMode = probHazardEvents.getAttributes().get('hazardMode', 'PRACTICE').upper()
        practice = True
        if caveMode == 'OPERATIONAL':
            practice = False
        
        for hazardEvent in probHazardEvents:
            
            if hazardEvent.getStatus().upper() != 'ISSUED':
                continue
            
            # Get the most recent entry in the history list for the event,
            # since that is the one upon which the grid should be based.
            # If it has never been added to the history list, then it
            # should not result in any grid generation.
            event = HazardDataAccess.getMostRecentHistoricalHazardEvent(hazardEvent.getEventID(), practice)
            if event is None:
                continue
            
            
            outDictInit = {k:hazardEvent.__getitem__(k) for k in attrKeys}
            outDict = self.convertAdvancedGeometry(outDictInit)
            
            filename = outDict.get('phenomenon') + '_' + hazardEvent.get('objectID') + '_' + issuedTime
            OUTPUTDIR = os.path.join(pu.getOutputDir(), 'IssuedEventsPickle/All')
            if not os.path.exists(OUTPUTDIR):
                try:
                    os.makedirs(OUTPUTDIR)
                except:
                    sys.stderr.write('Could not create PHI grids output directory:' +OUTPUTDIR+ '.  No output written')

            pickle.dump( outDict, open(OUTPUTDIR+'/'+filename, 'wb'))
    
    def convertAdvancedGeometry(self, it):
        if (isinstance(it, tuple)):
            return tuple([self.convertAdvancedGeometry(elem) for elem in it])
        elif (isinstance(it, list)):
            return [self.convertAdvancedGeometry(elem) for elem in it]
        elif (isinstance(it, dict)):
            return {k: self.convertAdvancedGeometry(v) for k, v in it.items()}
        else:
            if isinstance(it, AdvancedGeometry.AdvancedGeometry):
                return it.asShapely()
            else:
                return it
        
    
    def flush(self):
        import os
        os.sys.__stdout__.flush()


            
def __str__(self):
    return 'PHI Grid Recommender'

