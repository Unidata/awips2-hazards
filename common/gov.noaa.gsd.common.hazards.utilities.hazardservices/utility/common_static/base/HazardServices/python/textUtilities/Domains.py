'''
Domain configuration
###########################################SAMPLE DOMAIN CONFIGURATION#############################################
#                                                                                                                 #    
#      |              X                |                                |                 X                |      #
#      |              X                |            DOMAIN              |                 X                |      #
#      |              X                |        (e.g. Central)          |                 X                |      #
#      |              X                |                                |                 X                |      #
#    -109           -107             -103                              -92               -87              -83     #
# absMaxBound   upperSoftBound   upperLonBound                    lowerLonBound    lowerSoftBound     absMinBound #
#                                lowerSoftBound                   upperSoftBound                                  #
#                                                                                                                 #
###################################################################################################################
'''

class Domain:
    def __init__(self,domainName,abbrev,pil,wmoHeader,lowerLonBound,upperLonBound,lowerSoftBound,upperSoftBound,absMinBound,absMaxBound,count=0):
        self._domainName = domainName
        self._abbrev = abbrev
        self._count = count
        self._pil = pil
        self._wmoHeader = wmoHeader
        self._lowerLonBound = lowerLonBound
        self._upperLonBound = upperLonBound
        self._lowerSoftBound = lowerSoftBound
        self._upperSoftBound = upperSoftBound
        self._absMinBound = absMinBound
        self._absMaxBound = absMaxBound    
    def domainName(self):
        return self._domainName
    def abbrev(self):
        return self._abbrev
    def pil(self):
        return self._pil
    def wmoHeader(self):
        return self._wmoHeader
    def count(self):
        return self._count
    def setCount(self, value):
        self._count = value
    def incrementCount(self, increment=1):
        self._count += increment
    def lowerLonBound(self):
        #Eastward extent longitude in which domain is certain
        return self._lowerLonBound
    def upperLonBound(self):
        #Westward extent longitude in which domain is certain
        return self._upperLonBound
    def lowerSoftBound(self):
        #Eastward extent of soft boundary
        return self._lowerSoftBound
    def upperSoftBound(self):
        #westward extent of soft boundary
        return self._upperSoftBound
    def absMinBound(self):
        #Eastward absolute maximum 
        return self._absMinBound
    def absMaxBound(self):
        #Westward absolute maximum
        return self._absMaxBound
    
AviationDomains = [
    Domain('East', 'E', 'SIGE', 'WSUS31',None,-83,-83,-87,None,-92),
    Domain('Central', 'C', 'SIGC', 'WSUS32',-92,-103,[-87,-103],[-92,-107],-83,-109),
    Domain('West', 'W', 'SIGW', 'WSUS33',-109,None,-107,-109,-103,None)
    ]
        


