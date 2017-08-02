'''
Populate TC MetaData Tool to copy metadata for tropical cyclone international SIGMET.
'''
import RecommenderTemplate
import logging, UFStatusHandler

from inspect import currentframe, getframeinfo
import os, sys
from VisualFeatures import VisualFeatures

class Recommender(RecommenderTemplate.Recommender):
    
    def __init__(self):
        self.logger = logging.getLogger('PopulateTCMetaDataTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'PopulateTCMetaDataTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        

    def defineScriptMetadata(self):
        '''
        @return: A dictionary containing information about this
                 tool
        '''
        metadata = {}
        metadata['toolName'] = 'Populate TC MetaDAta Tool'
        metadata['author'] = 'GSD'
        metadata['version'] = '1.0';
        metadata['description'] = '''
        '''
        metadata['eventState'] = 'Pending'
        metadata['onlyIncludeTriggerEvents'] = True
        
        return metadata

    def defineDialog(self, eventSet):
        '''
        @return: A dialog definition to solicit user input before running tool
        '''   
        return None
        
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        Runs the Change Object Type Tool
        
        @param eventSet: A set of events which include session
                         attributes
        @param dialogInputMap: A map of information retrieved from
                               a user's interaction with a dialog.
        @param spatialInputMap:   A map of information retrieved
                                  from the user's interaction with the
                                  spatial display.
        
        @return: A list of potential probabilistic hazard events.
        '''
        import sys
        sys.stderr.write("Running Populate TC MetaData Tool.\n")

        sys.stderr.flush()
        
        
        for event in eventSet:
        
            stormName = event.get("internationalSigmetTCName")
            stormName = stormName.upper()
            
            for filename in os.listdir(self.tcaDirectory()):
                if "knhc" in filename or "phfo" in filename:
                    filePath = os.path.join(self.tcaDirectory(), filename)
                    if stormName in open(filePath).read():
                        tcaFile = filePath
            
            fileList = []
            fileDict = {}
            with open(tcaFile) as f:
                for line in f:
                    if ":" in line:
                        lineList = line.split()
                        fileList.append(lineList)
            
            for entry in fileList:
                if 'DTG:' in entry:
                    event.set("internationalSigmetTCObsTime", entry[1][9:13])
                elif 'TC:' in entry:
                    event.set("internationalSigmetTCName", entry[1])
                elif '06' in entry and 'WIND' not in entry:
                    event.set("internationalSigmetTCFcstTime", entry[5][2:])
                    event.set("internationalSigmetTCFcstPosition", entry[6] + ' ' + entry[7])
                elif 'MOV:' in entry:
                    event.set("internationalSigmetTCSpeed", int(entry[2][:2]))
                    event.set("internationalSigmetTcDirection", entry[1])
                elif 'PSN:' in entry:
                    event.set("internationalSigmetTCPosition", entry[1] + ' ' + entry[2])
            
        return eventSet

    def flush(self):
        import os
        os.sys.__stdout__.flush()
        
    #########################################
    ### OVERRIDES
    
    def tcaDirectory(self):
        return '/scratch/tca/' 
    
                      
def __str__(self):
    return 'Populate TC MetaData Tool'