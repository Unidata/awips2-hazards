"""
Burn Scar Flood Recommender
Initially patterned after the Dam Break Flood Recommender

@since: October 2014
@author: GSD Hazard Services Team
"""
import RecommenderTemplate
import logging, UFStatusHandler
import os, sys

from ConfigurationUtils import ConfigUtils

DEFAULTPHIGRIDOUTPUTPATH = '/scratch/PHIGridTesting'
LOWTHRESHSOURCE = "phiConfigLowThreshold"
ULLONSOURCE = "phiConfigUpperLeftLon"
ULLATSOURCE = "phiConfigUpperLeftLat"
NUMLONPOINTSSOURCE = "phiConfigNumLonPoints"
NUMLATPOINTSSOURCE = "phiConfigNumLatPoints"
BUFFERSOURCE = "phiConfigDomainBuffer"
OUTDIRSOURCE = "phiConfigPHIOutputGridLocation"
 
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        self.logger = logging.getLogger('LineAndPointTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'LineAndPointTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
        self._configUtils = ConfigUtils()

        
    def defineScriptMetadata(self):
        """
        @return: JSON string containing information about this
                 tool
        """
        metaDict = {}
        metaDict["toolName"] = "PHI Configuration Tool"
        metaDict["author"] = "GSD"
        metaDict["version"] = "1.0"
        metaDict["description"] = "Set Hazard Services wide configuration information for PHI Processing"
        metaDict["eventState"] = "Pending"
        
                
        # This tells Hazard Services to not notify the user when the recommender
        # creates no hazard events. Since this recommender is to be run in response
        # to hazard event changes, etc. it would be extremely annoying for the user
        # to be constantly dismissing the warning message dialog if no hazard events
        # were being created. 
        metaDict['background'] = True
        
        return metaDict

    def defineDialog(self, eventSet):
        """
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @return: MegaWidget dialog definition to solicit user input before running tool
        """        
        dialogDict = {"title": "PHI Configuration Tool"}
        fieldDictList = []
        valueDict = {}
        
        lowThresholdDict = {}
        lowThresholdDict["fieldName"] = LOWTHRESHSOURCE
        lowThresholdDict["label"] = "Set Low Threshold Value"
        lowThresholdDict["fieldType"] = "IntegerSpinner"
        lowThresholdDict["minValue"] = 0
        lowThresholdDict["maxValue"] = 100
        lowThresholdDict["values"] = self._configUtils.getLowThreshold()
        lowThresholdDict["showScale"] = True
        lowThresholdDict["incrementDelta"] = 10
        valueDict[LOWTHRESHSOURCE] = lowThresholdDict["values"]
        fieldDictList.append(lowThresholdDict)
        
        upperLeftLonDict = {}
        upperLeftLonDict["fieldName"] = ULLONSOURCE
        upperLeftLonDict["label"] = "Set Upper Left Longitude"
        upperLeftLonDict["fieldType"] = "FractionSpinner"
        upperLeftLonDict["minValue"] = -129.0
        upperLeftLonDict["maxValue"] = -67.5
        upperLeftLonDict["values"] = self._configUtils.getDomainULLon()
        upperLeftLonDict["showScale"] = True
        upperLeftLonDict["incrementDelta"] = 0.1
        valueDict[ULLONSOURCE] = upperLeftLonDict["values"]
        fieldDictList.append(upperLeftLonDict)
        
        upperLeftLatDict = {}
        upperLeftLatDict["fieldName"] = ULLATSOURCE
        upperLeftLatDict["label"] = "Set Upper Left Latitude"
        upperLeftLatDict["fieldType"] = "FractionSpinner"
        upperLeftLatDict["minValue"] = 27.0
        upperLeftLatDict["maxValue"] = 50.0
        upperLeftLatDict["values"] = self._configUtils.getDomainULLat()
        upperLeftLatDict["showScale"] = True
        upperLeftLatDict["incrementDelta"] = 0.1
        valueDict[ULLATSOURCE] = upperLeftLatDict["values"]
        fieldDictList.append(upperLeftLatDict)
        
        numLonPointsDict = {}
        numLonPointsDict["fieldName"] = NUMLONPOINTSSOURCE
        numLonPointsDict["label"] = "Set Number of Longitude Points"
        numLonPointsDict["fieldType"] = "IntegerSpinner"
        numLonPointsDict["minValue"] = 100
        numLonPointsDict["maxValue"] = 1500
        numLonPointsDict["values"] = self._configUtils.getDomainLonPoints()
        numLonPointsDict["showScale"] = True
        numLonPointsDict["incrementDelta"] = 1
        valueDict[NUMLONPOINTSSOURCE] = numLonPointsDict["values"]
        fieldDictList.append(numLonPointsDict)
        
        numLatPointsDict = {}
        numLatPointsDict["fieldName"] = NUMLATPOINTSSOURCE
        numLatPointsDict["label"] = "Set Number of Latitude Points"
        numLatPointsDict["fieldType"] = "IntegerSpinner"
        numLatPointsDict["minValue"] = 100
        numLatPointsDict["maxValue"] = 1500
        numLatPointsDict["values"] = self._configUtils.getDomainLatPoints()
        numLatPointsDict["showScale"] = True
        numLatPointsDict["incrementDelta"] = 1
        valueDict[NUMLATPOINTSSOURCE] = numLatPointsDict["values"]
        fieldDictList.append(numLatPointsDict)
        
        bufferDict = {}
        bufferDict["fieldName"] = BUFFERSOURCE
        bufferDict["label"] = "Set Buffer Around Domain (in Degrees Lon/Lat)"
        bufferDict["fieldType"] = "FractionSpinner"
        bufferDict["minValue"] = 0.25
        bufferDict["maxValue"] = 3.0
        bufferDict["values"] = self._configUtils.getDomainBuffer()
        bufferDict["showScale"] = True
        bufferDict["incrementDelta"] = 0.25
        valueDict[BUFFERSOURCE] = bufferDict["values"]
        fieldDictList.append(bufferDict)
        
        lowerRightLonDict = {}
        lowerRightLonDict["fieldName"] = "phiConfigLowerRightLon"
        lowerRightLonDict["label"] = "Lower Right Lon"
        lowerRightLonDict["fieldType"] = "Text"
        lowerRightLonDict["values"] = -1
        lowerRightLonDict["editable"] = False
        valueDict["phiConfigLowerRightLon"] = lowerRightLonDict["values"]
        fieldDictList.append(lowerRightLonDict)

        lowerRightLatDict = {}
        lowerRightLatDict["fieldName"] = "phiConfigLowerRightLat"
        lowerRightLatDict["label"] = "Lower Right Lat"
        lowerRightLatDict["fieldType"] = "Text"
        lowerRightLatDict["values"] = -1
        lowerRightLatDict["editable"] = False
        valueDict["phiConfigLowerRightLat"] = lowerRightLatDict["values"]
        fieldDictList.append(lowerRightLatDict)
        

        
        outputDirDict = {}
        outputDirDict["fieldName"] = OUTDIRSOURCE
        outputDirDict["label"] = "Set Location for PHI Output Grids"
        outputDirDict["fieldType"] = "Text"
        outputDirDict["lines"] = 1
        outputDirDict["values"] = self._configUtils.getOutputDir()
        outputDirDict["visibleChars"] = 40
        valueDict[OUTDIRSOURCE] = outputDirDict["values"]
        fieldDictList.append(outputDirDict)
        
        
        dialogDict["fields"] = fieldDictList
        dialogDict["valueDict"] = valueDict
        
        return dialogDict
    
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        """
        @eventSet: List of objects that was converted from Java IEvent objects
        @param dialogInputMap: A map containing user selections from the dialog
        created by the defineDialog() method.
        @param visualFeatures: Visual features as defined by the defineSpatialInfo()
        method and modified by the user to provide spatial input; ignored.
        @return: List of objects that will be later converted to Java IEvent
        objects
        """
        self._configUtils.setConfigDict(
                                          lowThresh = dialogInputMap.get(LOWTHRESHSOURCE),
                                          initial_ulLon = dialogInputMap.get(ULLONSOURCE),
                                          initial_ulLat = dialogInputMap.get(ULLATSOURCE),
                                          OUTPUTDIR = dialogInputMap.get(OUTDIRSOURCE),
                                          buff = dialogInputMap.get(BUFFERSOURCE),
                                          lonPoints = dialogInputMap.get(NUMLONPOINTSSOURCE),
                                          latPoints = dialogInputMap.get(NUMLATPOINTSSOURCE),
                                          )
        

        return None

    def toString(self):
        return "PHIConfigurationTool"
    
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    returnDict = {}
    if triggerIdentifiers == None:
        return returnDict
    
    if 'phiConfigUpperLeftLat' in triggerIdentifiers or "phiConfigNumLatPoints" in triggerIdentifiers:
        ulLatVal = mutableProperties['phiConfigUpperLeftLat']['values']
        latPts = mutableProperties['phiConfigNumLatPoints']['values']
        lrLatVal =  ulLatVal - (0.01 * latPts)
        returnDict['phiConfigLowerRightLat'] = {'values': lrLatVal}

    if 'phiConfigUpperLeftLon' in triggerIdentifiers or "phiConfigNumLonPoints" in triggerIdentifiers:
        ulLonVal = mutableProperties['phiConfigUpperLeftLon']['values']
        lonPts = mutableProperties['phiConfigNumLonPoints']['values']
        lrLonVal =  ulLonVal + (0.01 * lonPts)
        returnDict['phiConfigLowerRightLon'] = {'values':lrLonVal}

    return returnDict
        