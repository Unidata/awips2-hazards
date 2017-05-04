'''
Utility for PHIGridRecommender and PreviewGridRecommender
'''

import sys, os
import json

class ConfigUtils(object):
    def __init__(self):
        #### NOTE: Change this path to your directory shared between "Processor" and "UI" machine
        self.filename = '/home/kevin.manross/realtime-a2/hazardServicesConfig.json'
        self.ContentsDict = {}
        self.defaultLowThreshold = 40
        self.defaultPHIOutputDir = '/scratch/PHIGridTesting'
        self.defaultDomainBuffer = 0.5
        self.defaultDomainULLon = -104.0
        self.defaultDomainULLat = 37.0
        self.defaultDomainLRLon = -92.0
        self.defaultDomainLRLat = 27.0
        
        
        self.lowThreshKey = 'lowThreshold'
        self.outputDirKey = 'OUTPUTDIR'
        self.domainBufferKey = 'domainBuffer'
        self.domainULLonKey = 'domainULLon'
        self.domainULLatKey = 'domainULLat'
        self.domainLRLonKey = 'domainLRLon'
        self.domainLRLatKey = 'domainLRLat'
        self.defaultContents = {
                                 self.lowThreshKey: self.defaultLowThreshold,
                                 self.outputDirKey: self.defaultPHIOutputDir,
                                 self.domainBufferKey: self.defaultDomainBuffer,
                                 self.domainULLonKey: self.defaultDomainULLon,
                                 self.domainULLatKey: self.defaultDomainULLat,
                                 self.domainLRLonKey: self.defaultDomainLRLon,
                                 self.domainLRLatKey: self.defaultDomainLRLat
                                 }

    def setConfigDict(self, lowThresh = None,
                            initial_ulLon = None, 
                            initial_ulLat = None, 
                            initial_lrLon = None, 
                            initial_lrLat = None, 
                            OUTPUTDIR = None, 
                            buff = None, 
                            ):
        
        self.ContentsDict[self.lowThreshKey] = lowThresh if lowThresh is not None else self.defaultLowThreshold
        self.ContentsDict[self.outputDirKey] = OUTPUTDIR if OUTPUTDIR is not None else self.defaultPHIOutputDir
        self.ContentsDict[self.domainBufferKey] = buff if buff is not None else self.defaultDomainBuffer
        self.ContentsDict[self.domainULLonKey] = initial_ulLon if initial_ulLon is not None else self.defaultDomainULLon
        self.ContentsDict[self.domainULLatKey] = initial_ulLat if initial_ulLat is not None else self.defaultDomainULLat
        self.ContentsDict[self.domainLRLonKey] = initial_lrLon if initial_lrLon is not None else self.defalltDomainLRLon
        self.ContentsDict[self.domainLRLatKey] = initial_lrLat if initial_lrLat is not None else self.defalltDomainLRLat
        self.writeJson()

    def setLowThreshold(self, val=None):
        self.readJson()
        self.ContentsDict[self.lowThreshKey] = val if val is not None else self.defaultLowThreshold
        self.writeJson()
    
    def setPHIOutputDir(self, val=None):
        self.readJson()
        self.ContentsDict[self.outputDirKey] = val if val is not None else self.defaultPHIOutputDir
        self.writeJson()

    def setDomainBuffer(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainBufferKey] = val  if val is not None else self.defaultDomainBuffer
        self.writeJson()

    def setDomainULLon(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainULLonKey] = val if val is not None else self.defaultDomainULLon
        self.writeJson()

    def setDomainULLat(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainULLatKey] = val if val is not None else self.defaultDomainULLat
        self.writeJson()

    def setDomainLRLon(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainLRLonKey] = val if val is not None else self.defaultDomainLRLon
        self.writeJson()

    def setDomainLRLat(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainLRLatKey] = val if val is not None else self.defaultDomainLRLat
        self.writeJson()

    def getConfigDict(self):
        self.readJson()
        return self.ContentsDict
    
    def getLowThreshold(self):
        self.readJson()
        return self.ContentsDict.get(self.lowThreshKey, self.defaultLowThreshold)

    def getPHIOutputDir(self):
        self.readJson()
        return self.ContentsDict.get(self.outputDirKey, self.defaultPHIOutputDir)

    def getDomainBuffer(self):
        self.readJson()
        return self.ContentsDict.get(self.domainBufferKey, self.defaultDomainBuffer)

    def getDomainULLon(self):
        self.readJson()
        return self.ContentsDict.get(self.domainULLonKey, self.defaultDomainULLon)

    def getDomainULLat(self):
        self.readJson()
        return self.ContentsDict.get(self.domainULLatKey, self.defaultDomainULLat)

    def getDomainLRLon(self):
        self.readJson()
        return self.ContentsDict.get(self.domainLRLonKey, self.defaultDomainLRLon)

    def getDomainLRLat(self):
        self.readJson()
        return self.ContentsDict.get(self.domainLRLatKey, self.defaultDomainLRLat)

    def getOutputDir(self):
        self.readJson()
        return self.ContentsDict.get(self.outputDirKey, self.defaultPHIOutputDir)


    
    def writeJson(self):
        try:
            with open(self.filename, 'w') as outfile:
                json.dump(self.ContentsDict, outfile)
        except:
            sys.stderr.write('\nUnable to open config file: '+self.filename+' for writing.  Please check path and permissions')
    
    def readJson(self):
        try:
            with open(self.filename) as contents:
                self.ContentsDict = json.load(contents)
        except:
            sys.stderr.write('\nUnable to open config file: '+self.filename+' for reading. Setting to defaults.\n')
            self.ContentsDict = self.defaultContents.copy()
    
