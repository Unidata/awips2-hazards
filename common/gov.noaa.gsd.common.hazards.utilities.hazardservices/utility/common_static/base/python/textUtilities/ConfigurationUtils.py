'''
Utility for PHIGridRecommender and PreviewGridRecommender
'''

import sys, os
import json



class ConfigUtils(object):
    def __init__(self):
        self._filename = '/scratch/hazardServicesConfig.json'
        self._ContentsDict = {}
        self._defaultLowThreshold = 40
        self._defaultPHIOutputDir = '/scratch/PHIGridTesting'
        self._defaultDomainBuffer = 1.0
        self._defaltDomainLonPoints = 1200
        self._defaltDomainLatPoints = 1000
        self._defaultDomainULLon = -104.0
        self._defaultDomainULLat = 43.0
        
        self._lowThreshKey = 'lowThreshold'
        self._outputDirKey = 'OUTPUTDIR'
        self._domainBufferKey = 'domainBuffer'
        self._domainLonPointsKey = 'domainLonPoints'
        self._domainLatPointsKey = 'domainLatPoints'
        self._domainULLonKey = 'domainULLon'
        self._domainULLatKey = 'domainULLat'
        self._defaultContents = {
                                 self._lowThreshKey: self._defaultLowThreshold,
                                 self._outputDirKey: self._defaultPHIOutputDir,
                                 self._domainBufferKey: self._defaultDomainBuffer,
                                 self._domainLonPointsKey: self._defaltDomainLonPoints,
                                 self._domainLatPointsKey: self._defaltDomainLatPoints,
                                 self._domainULLonKey: self._defaultDomainULLon,
                                 self._domainULLatKey: self._defaultDomainULLat
                                 }

    def setConfigDict(self, lowThresh = None,
                            initial_ulLon = None, 
                            initial_ulLat = None, 
                            OUTPUTDIR = None, 
                            buff = None, 
                            lonPoints = None, 
                            latPoints = None
                            ):
        
        self._ContentsDict[self._lowThreshKey] = lowThresh if lowThresh is not None else self._defaultLowThreshold
        self._ContentsDict[self._outputDirKey] = OUTPUTDIR if OUTPUTDIR is not None else self._defaultPHIOutputDir
        self._ContentsDict[self._domainBufferKey] = buff if buff is not None else self._defaultDomainBuffer
        self._ContentsDict[self._domainLonPointsKey] = lonPoints if lonPoints is not None else self._defaltDomainLonPoints
        self._ContentsDict[self._domainLatPointsKey] = latPoints if latPoints is not None else self._defaltDomainLatPoints
        self._ContentsDict[self._domainULLonKey] = initial_ulLon if initial_ulLon is not None else self._defaultDomainULLon
        self._ContentsDict[self._domainULLatKey] = initial_ulLat if initial_ulLat is not None else self._defaultDomainULLat
        self._writeJson()

    def setLowThreshold(self, val=None):
        self._readJson()
        self._ContentsDict[self._lowThreshKey] = val if val is not Nonme else self._defaultLowThreshold
        self._writeJson()
    
    def setPHIOutputDir(self, val=None):
        self._readJson()
        self._ContentsDict[self._outputDirKey] = val if val is not Nonme else self._defaultPHIOutputDir
        self._writeJson()

    def setDomainBuffer(self, val=None):
        self._readJson()
        self._ContentsDict[self._domainBufferKey] = val  if val is not Nonme else self._defaultDomainBuffer
        self._writeJson()

    def setDomainLonPoints(self, val=None):
        self._readJson()
        self._ContentsDict[self._domainLonPointsKey] = val if val is not Nonme else self._defaltDomainLonPoints
        self._writeJson()

    def setDomainLatPoints(self, val=None):
        self._readJson()
        self._ContentsDict[self._domainLatPointsKey] = val if val is not Nonme else self._defaltDomainLatPoints
        self._writeJson()

    def setDomainULLon(self, val=None):
        self._readJson()
        self._ContentsDict[self._domainULLonKey] = val if val is not Nonme else self._defaultDomainULLon
        self._writeJson()

    def setDomainULLat(self, val=None):
        self._readJson()
        self._ContentsDict[self._domainULLatKey] = val if val is not Nonme else self._defaultDomainULLat
        self._writeJson()

    def getConfigDict(self):
        self._readJson()
        return self._ContentsDict
    
    def getLowThreshold(self):
        self._readJson()
        return self._ContentsDict.get(self._lowThreshKey, self._defaultLowThreshold)

    def getPHIOutputDir(self):
        self._readJson()
        return self._ContentsDict.get(self._outputDirKey, self._defaultPHIOutputDir)

    def getDomainBuffer(self):
        self._readJson()
        return self._ContentsDict.get(self._domainBufferKey, self._defaultDomainBuffer)

    def getDomainLonPoints(self):
        self._readJson()
        return self._ContentsDict.get(self._domainLonPointsKey, self._defaltDomainLonPoints )

    def getDomainLatPoints(self):
        self._readJson()
        return self._ContentsDict.get(self._domainLatPointsKey, self._defaltDomainLatPoints)

    def getDomainULLon(self):
        self._readJson()
        return self._ContentsDict.get(self._domainULLonKey, self._defaultDomainULLon)

    def getDomainULLat(self):
        self._readJson()
        return self._ContentsDict.get(self._domainULLatKey, self._defaultDomainULLat)

    def getOutputDir(self):
        self._readJson()
        return self._ContentsDict.get(self._outputDirKey, self._defaultPHIOutputDir)


    
    def _writeJson(self):
        try:
            with open(self._filename, 'w') as outfile:
                json.dump(self._ContentsDict, outfile)
        except:
            sys.stderr.write('\nUnable to open config file: '+self._filename+' for writing.  Please check path and permissions')
    
    def _readJson(self):
        try:
            with open(self._filename) as contents:
                self._ContentsDict = json.load(contents)
        except:
            sys.stderr.write('\nUnable to open config file: '+self._filename+' for reading. Setting to defaults.\n')
            self._ContentsDict = self._defaultContents.copy()
    
