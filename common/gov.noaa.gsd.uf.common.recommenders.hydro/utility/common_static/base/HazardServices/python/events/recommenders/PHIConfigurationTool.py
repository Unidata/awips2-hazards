"""
Burn Scar Flood Recommender
Initially patterned after the Dam Break Flood Recommender

@since: October 2014
@author: GSD Hazard Services Team
"""
import RecommenderTemplate
import logging, UFStatusHandler
import os, sys

import GenericRegistryObjectDataAccess
from HazardConstants import *

#from ConfigurationUtils import ConfigUtils

DEFAULTPHIGRIDOUTPUTPATH = '/scratch/PHIGridTesting'
LOWTHRESHSOURCE = "phiConfigLowThreshold"
ULLONSOURCE = "phiConfigUpperLeftLon"
ULLATSOURCE = "phiConfigUpperLeftLat"
LRLONSOURCE = "phiConfigLowerRightLon"
LRLATSOURCE = "phiConfigLowerRightLat"
NUMLONPOINTSSOURCE = "phiConfigNumLonPoints"
NUMLATPOINTSSOURCE = "phiConfigNumLatPoints"
BUFFERSOURCE = "phiConfigDomainBuffer"
OUTDIRSOURCE = "phiConfigPHIOutputGridLocation"

LONMAX = -67.5
LONMIN = -129.0
LATMAX = 50.0
LATMIN = 27.0

 
class Recommender(RecommenderTemplate.Recommender):

    def __init__(self):
        self.logger = logging.getLogger('PHIConfigTool')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'PHIConfigTool', level=logging.INFO))
        self.logger.setLevel(logging.INFO)
#        self._configUtils = ConfigUtils()

        
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
        
        metaDict["getSpatialInfoNeeded"] = False
        
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
        
        caveMode = eventSet.getAttributes().get('runMode','PRACTICE').upper()
        practice = (False if caveMode == 'OPERATIONAL' else True)
        
        returnList = GenericRegistryObjectDataAccess.queryObjects([("uniqueID", "phiConfig"),("objectType", "phiConfigData")], practice)
        if len(returnList) == 1:
            objectDicts = returnList[0]
        elif len(returnList) == 0:
            objectDicts = {}
        else:
            sts.stderr.write("!!!! PHIConfig - GenericRegistryObjectDataAccess.queryObject returned multiple dictionaries. Reverting to default values")
            objectDicts = {}
        
        
        lowThresh = objectDicts.get(LOWTHRESHKEY, DEFAULTLOWTHRESHOLD)
        ulLon = objectDicts.get(DOMAINULLONKEY, DEFAULTDOMAINULLON)
        ulLat = objectDicts.get(DOMAINULLATKEY, DEFAULTDOMAINULLAT)
        lrLon = objectDicts.get(DOMAINLRLONKEY, DEFAULTDOMAINLRLON)
        lrLat = objectDicts.get(DOMAINLRLATKEY, DEFAULTDOMAINLRLAT)
        buffer = objectDicts.get(DOMAINBUFFERKEY, DEFAULTDOMAINBUFFER)
        outputDir = objectDicts.get(OUTPUTDIRKEY, DEFAULTPHIOUTPUTDIR)
        
        
        lowThresholdDict = {}
        lowThresholdDict["fieldName"] = LOWTHRESHSOURCE
        lowThresholdDict["label"] = "Set Low Threshold Value"
        lowThresholdDict["fieldType"] = "IntegerSpinner"
        lowThresholdDict["minValue"] = 0
        lowThresholdDict["maxValue"] = 101
        lowThresholdDict["values"] = lowThresh
        lowThresholdDict["showScale"] = True
        lowThresholdDict["incrementDelta"] = 10
        valueDict[LOWTHRESHSOURCE] = lowThresholdDict["values"]
        fieldDictList.append(lowThresholdDict)
        
        upperLeftLonDict = {}
        upperLeftLonDict["fieldName"] = ULLONSOURCE
        upperLeftLonDict["label"] = "Set Upper Left LONGitude"
        upperLeftLonDict["fieldType"] = "FractionSpinner"
        upperLeftLonDict["minValue"] = LONMIN
        upperLeftLonDict["maxValue"] = LONMAX
        upperLeftLonDict["values"] = ulLon
        upperLeftLonDict["showScale"] = True
        upperLeftLonDict["incrementDelta"] = 0.1
        valueDict[ULLONSOURCE] = upperLeftLonDict["values"]
        fieldDictList.append(upperLeftLonDict)
        
        upperLeftLatDict = {}
        upperLeftLatDict["fieldName"] = ULLATSOURCE
        upperLeftLatDict["label"] = "Set Upper Left LATitude"
        upperLeftLatDict["fieldType"] = "FractionSpinner"
        upperLeftLatDict["minValue"] = LATMIN
        upperLeftLatDict["maxValue"] = LATMAX
        upperLeftLatDict["values"] = ulLat
        upperLeftLatDict["showScale"] = True
        upperLeftLatDict["incrementDelta"] = 0.1
        valueDict[ULLATSOURCE] = upperLeftLatDict["values"]
        fieldDictList.append(upperLeftLatDict)
        
        lowerRightLonDict = {}
        lowerRightLonDict["fieldName"] = LRLONSOURCE
        lowerRightLonDict["label"] = "Set Lower Right LONGitude"
        lowerRightLonDict["fieldType"] = "FractionSpinner"
        lowerRightLonDict["minValue"] = LONMIN
        lowerRightLonDict["maxValue"] = LONMAX
        lowerRightLonDict["values"] = lrLon
        lowerRightLonDict["showScale"] = True
        lowerRightLonDict["incrementDelta"] = 0.1
        valueDict[LRLONSOURCE] = lowerRightLonDict["values"]
        fieldDictList.append(lowerRightLonDict)
        
        lowerRightLatDict = {}
        lowerRightLatDict["fieldName"] = LRLATSOURCE
        lowerRightLatDict["label"] = "Set Lower Right LATitude"
        lowerRightLatDict["fieldType"] = "FractionSpinner"
        lowerRightLatDict["minValue"] = LATMIN
        lowerRightLatDict["maxValue"] = LATMAX
        lowerRightLatDict["values"] = lrLat
        lowerRightLatDict["showScale"] = True
        lowerRightLatDict["incrementDelta"] = 0.1
        valueDict[LRLATSOURCE] = lowerRightLatDict["values"]
        fieldDictList.append(lowerRightLatDict)
        
        bufferDict = {}
        bufferDict["fieldName"] = BUFFERSOURCE
        bufferDict["label"] = "Set Buffer Around Domain (in Degrees Lon/Lat)"
        bufferDict["fieldType"] = "FractionSpinner"
        bufferDict["minValue"] = 0.25
        bufferDict["maxValue"] = 3.0
        bufferDict["values"] = buffer
        bufferDict["showScale"] = True
        bufferDict["incrementDelta"] = 0.25
        valueDict[BUFFERSOURCE] = bufferDict["values"]
        fieldDictList.append(bufferDict)
        
        outputDirDict = {}
        outputDirDict["fieldName"] = OUTDIRSOURCE
        outputDirDict["label"] = "Set Location for PHI Output Grids"
        outputDirDict["fieldType"] = "Text"
        outputDirDict["lines"] = 1
        outputDirDict["values"] = outputDir
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
        objectDict = {
                        "uniqueID":"phiConfig",
                        "objectType": "phiConfigData",
                        LOWTHRESHKEY:dialogInputMap.get(LOWTHRESHSOURCE),
                        DOMAINULLONKEY:dialogInputMap.get(ULLONSOURCE),
                        DOMAINULLATKEY:dialogInputMap.get(ULLATSOURCE),
                        DOMAINLRLONKEY:dialogInputMap.get(LRLONSOURCE),
                        DOMAINLRLATKEY:dialogInputMap.get(LRLATSOURCE),
                        OUTPUTDIRKEY:dialogInputMap.get(OUTDIRSOURCE),
                        DOMAINBUFFERKEY:dialogInputMap.get(BUFFERSOURCE),
                    }

        caveMode = eventSet.getAttributes().get('runMode','PRACTICE').upper()
        practice = (False if caveMode == 'OPERATIONAL' else True)

        GenericRegistryObjectDataAccess.storeObject(objectDict, practice)
        
        self.remakeDomainMap(practice)

        return None
    
    
    def remakeDomainMap(self, practice):
        pass

#        returnList = GenericRegistryObjectDataAccess.queryObjects([("uniqueID", "phiConfig")], practice)
#        if len(returnList) == 1:
#            objectDicts = returnList[0]
#        elif len(returnList) == 0:
#            objectDicts = {}
#        else:
#            sts.stderr.write("!!!! PHIConfig - GenericRegistryObjectDataAccess.queryObject returned multiple dictionaries. Reverting to default values")
#            objectDicts = {}
#        
#        
#        lowThresh = objectDicts.get(LOWTHRESHKEY, DEFAULTLOWTHRESHOLD)
#        ulLon = objectDicts.get(DOMAINULLONKEY, DEFAULTDOMAINULLON)
#        ulLat = objectDicts.get(DOMAINULLATKEY, DEFAULTDOMAINULLAT)
#        lrLon = objectDicts.get(DOMAINLRLONKEY, DEFAULTDOMAINLRLON)
#        lrLat = objectDicts.get(DOMAINLRLATKEY, DEFAULTDOMAINLRLAT)
#        buffer = objectDicts.get(DOMAINBUFFERKEY, DEFAULTDOMAINBUFFER)
#        outputDir = objectDicts.get(OUTPUTDIRKEY, DEFAULTPHIOUTPUTDIR)
#        
#        import shapefile
#        import filecmp
#        self.filepath
#        w=shapefile.Writer(shapefile.POLYGON)
#        w.poly(parts=[[[ulLon,ulLat],[lrLon,ulLat],[lrLon,lrLat],[ulLon,lrLat]]])
#        w.save('%s/config/PHI-HS-Domain'%self.filepath)
#        localHSfilename='%s/config/PHI-HS-Domain.shp'%self.filepath
#        a2ShpFile='/awips2/edex/data/utility/common_static/site/OAX/shapefiles/Domains/PHIHS/PHI-HS-Domain.shp'
#
#        if filecmp.cmp(a2ShpFile,localHSfilename,shallow=False):
#            pass
#        else:
#            print "Shapefile changed"
#            w.save('/awips2/edex/data/utility/common_static/site/OAX/shapefiles/Domains/PHIHS/PHI-HS-Domain')
#            syscmd='/awips2/database/sqlScripts/share/sql/maps/importShapeFile.sh /awips2/edex/data/utility/common_static/site/OAX/shapefiles/Domains/PHIHS/PHI-HS-Domain.shp mapdata phihs'
#            os.system(syscmd)
#

    

    def toString(self):
        return "PHIConfigurationTool"
    
     
def applyInterdependencies(triggerIdentifiers, mutableProperties):

    returnDict = {}
    if triggerIdentifiers == None:
        return returnDict
    
    if 'phiConfigUpperLeftLat' in triggerIdentifiers:
        ulLatVal = mutableProperties['phiConfigUpperLeftLat']['values']
        lrLatVal = mutableProperties['phiConfigLowerRightLat']['values']
        if ulLatVal <= lrLatVal + 1:
            lrLatVal =  ulLatVal - 1.0
            if lrLatVal < LATMIN:
                lrLatVal = LATMIN
                ulLatVal = LATMIN + 1.0
        returnDict['phiConfigLowerRightLat'] = {'values': lrLatVal}
        returnDict['phiConfigUpperLeftLat'] = {'values': ulLatVal}

    if 'phiConfigLowerRightLat' in triggerIdentifiers:
        ulLatVal = mutableProperties['phiConfigUpperLeftLat']['values']
        lrLatVal = mutableProperties['phiConfigLowerRightLat']['values']
        if ulLatVal <= lrLatVal + 1:
            ulLatVal =  lrLatVal + 1.0
            if ulLatVal > LATMAX:
                ulLatVal = LATMAX
                lrLatVal= LATMAX - 1.0
        returnDict['phiConfigLowerRightLat'] = {'values': lrLatVal}
        returnDict['phiConfigUpperLeftLat'] = {'values': ulLatVal}

    if 'phiConfigUpperLeftLon' in triggerIdentifiers:
        ulLonVal = mutableProperties['phiConfigUpperLeftLon']['values']
        lrLonVal = mutableProperties['phiConfigLowerRightLon']['values']
        if ulLonVal >= lrLonVal - 1:
            lrLonVal =  ulLonVal + 1.0
            if lrLonVal > LONMAX:
                lrLonVal = LONMAX
                ulLonVal = LONMAX - 1.0
        returnDict['phiConfigLowerRightLon'] = {'values': lrLonVal}
        returnDict['phiConfigUpperLeftLon'] = {'values': ulLonVal}

    if 'phiConfigLowerRightLon' in triggerIdentifiers:
        ulLonVal = mutableProperties['phiConfigUpperLeftLon']['values']
        lrLonVal = mutableProperties['phiConfigLowerRightLon']['values']
        if ulLonVal >= lrLonVal - 1:
            ulLonVal =  lrLonVal - 1.0
            if ulLonVal < LONMIN:
                ulLonVal = LONMIN
                lrLonVal = LONMIN + 1.0 
        returnDict['phiConfigUpperLeftLon'] = {'values': ulLonVal}
        returnDict['phiConfigLowerRightLon'] = {'values': lrLonVal}



    return returnDict
      