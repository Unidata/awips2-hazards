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
        self.defaultDomainBuffer = 1.0
        self.defaltDomainLonPoints = 1200
        self.defaltDomainLatPoints = 1000
        self.defaultDomainULLon = -104.0
        self.defaultDomainULLat = 43.0
        
        self.lowThreshKey = 'lowThreshold'
        self.outputDirKey = 'OUTPUTDIR'
        self.domainBufferKey = 'domainBuffer'
        self.domainLonPointsKey = 'domainLonPoints'
        self.domainLatPointsKey = 'domainLatPoints'
        self.domainULLonKey = 'domainULLon'
        self.domainULLatKey = 'domainULLat'
        self.defaultContents = {
                                 self.lowThreshKey: self.defaultLowThreshold,
                                 self.outputDirKey: self.defaultPHIOutputDir,
                                 self.domainBufferKey: self.defaultDomainBuffer,
                                 self.domainLonPointsKey: self.defaltDomainLonPoints,
                                 self.domainLatPointsKey: self.defaltDomainLatPoints,
                                 self.domainULLonKey: self.defaultDomainULLon,
                                 self.domainULLatKey: self.defaultDomainULLat
                                 }

    def setConfigDict(self, lowThresh = None,
                            initial_ulLon = None, 
                            initial_ulLat = None, 
                            OUTPUTDIR = None, 
                            buff = None, 
                            lonPoints = None, 
                            latPoints = None
                            ):
        
        self.ContentsDict[self.lowThreshKey] = lowThresh if lowThresh is not None else self.defaultLowThreshold
        self.ContentsDict[self.outputDirKey] = OUTPUTDIR if OUTPUTDIR is not None else self.defaultPHIOutputDir
        self.ContentsDict[self.domainBufferKey] = buff if buff is not None else self.defaultDomainBuffer
        self.ContentsDict[self.domainLonPointsKey] = lonPoints if lonPoints is not None else self.defaltDomainLonPoints
        self.ContentsDict[self.domainLatPointsKey] = latPoints if latPoints is not None else self.defaltDomainLatPoints
        self.ContentsDict[self.domainULLonKey] = initial_ulLon if initial_ulLon is not None else self.defaultDomainULLon
        self.ContentsDict[self.domainULLatKey] = initial_ulLat if initial_ulLat is not None else self.defaultDomainULLat
        self.writeJson()

    def setLowThreshold(self, val=None):
        self.readJson()
        self.ContentsDict[self.lowThreshKey] = val if val is not Nonme else self.defaultLowThreshold
        self.writeJson()
    
    def setPHIOutputDir(self, val=None):
        self.readJson()
        self.ContentsDict[self.outputDirKey] = val if val is not Nonme else self.defaultPHIOutputDir
        self.writeJson()

    def setDomainBuffer(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainBufferKey] = val  if val is not Nonme else self.defaultDomainBuffer
        self.writeJson()

    def setDomainLonPoints(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainLonPointsKey] = val if val is not Nonme else self.defaltDomainLonPoints
        self.writeJson()

    def setDomainLatPoints(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainLatPointsKey] = val if val is not Nonme else self.defaltDomainLatPoints
        self.writeJson()

    def setDomainULLon(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainULLonKey] = val if val is not Nonme else self.defaultDomainULLon
        self.writeJson()

    def setDomainULLat(self, val=None):
        self.readJson()
        self.ContentsDict[self.domainULLatKey] = val if val is not Nonme else self.defaultDomainULLat
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

    def getDomainLonPoints(self):
        self.readJson()
        return self.ContentsDict.get(self.domainLonPointsKey, self.defaltDomainLonPoints )

    def getDomainLatPoints(self):
        self.readJson()
        return self.ContentsDict.get(self.domainLatPointsKey, self.defaltDomainLatPoints)

    def getDomainULLon(self):
        self.readJson()
        return self.ContentsDict.get(self.domainULLonKey, self.defaultDomainULLon)

    def getDomainULLat(self):
        self.readJson()
        return self.ContentsDict.get(self.domainULLatKey, self.defaultDomainULLat)

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
    
