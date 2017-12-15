import CommonMetaData
import MetaData_AIRMET_SIGMET
from HazardConstants import *
import datetime
import json
from com.raytheon.uf.common.time import SimulatedTime
import sys

class MetaData(MetaData_AIRMET_SIGMET.MetaData):
    
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.AAWUinitialize(hazardEvent, metaDict)
        CommonMetaData.writelines(sys.stderr, ['Calling SIGMET.W', '\n'])

        sigmetTypes = ["Thunderstorm", "Severe Turbulence", "Severe Icing", "Widespread Duststorm",
                       "Widespread Sandstorm", "Tropical Cyclone"]

        AAWUSigmetDesigs = ["INDIA", "JULIET", "KILO", "LIMA", "MIKE"]
        HFOSigmetDesigs = ["SIERRA", "TANGO", "UNIFORM", "VICTOR", "WHISKEY", "XRAY", "YANKEE", "ZULU"]
        GUMSigmetDesigs = ["INDIA", "JULIET", "KILO", "LIMA", "MIKE"]

        advisoryType = 'SIGMET'
       
        metaData = [
                        self.getAdvisoryType(advisoryType),
                        self.getHazardType(sigmetTypes),
                        self.getHazardSubtype(),
                        self.getVerticalExtent(),
                        self.getMaxCbTops(),
                        self.getMovement(),
                        self.getIntensity(),
                        self.getForecastOrObserved(),
                        self.getAdvisoryArea(advisoryType),
                        self.getAdvisoryName(AAWUSigmetDesigs),
                   ]

        return  {
                METADATA_KEY: metaData,
                } 
                
    
    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    AMChanges = MetaData_AIRMET_SIGMET.applyInterdependencies(triggerIdentifiers, mutableProperties)
    if AMChanges is None:
        AMChanges = {}
    
    import sys
    CommonMetaData.writelines(sys.stderr, ['Hello World [SIGMET] !\n'])


    subHaz = None
    if triggerIdentifiers:
        subHaz = {}
        for ti in triggerIdentifiers:
            if ti.find('AAWUHazardType') >= 0:
                ht =  mutableProperties.get('AAWUHazardType')['values']
                hazType = {
                   "Thunderstorm": ["Obscured", "Embedded", "Widespread", "Squall Line", "Isolated Severe"],
                   "Severe Icing": ["Not Applicable", "with Freezing Rain"]
                   }
                sel = hazType.get(ht)
                CommonMetaData.writelines(sys.stderr, ['HT: ', str(ht), ' -- SEL: ', str(sel), '\n'])
                if sel is None:
                    choices = ['Not Applicable']
                    enable = False
                else:
                    choices = sel
                    enable = True
                subHaz['AAWUHazardSubType'] = {"choices":choices, "enable": enable, "values": choices[0] }   
                AMChanges['AAWUHazardSubType'] = subHaz['AAWUHazardSubType']
                
                if ht == "Thunderstorm":
                    subHaz['AAWUMaxCbTops'] = {"enable": True }  
                else:
                    subHaz['AAWUMaxCbTops'] = {"enable": False }  
                AMChanges['AAWUMaxCbTops'] = subHaz['AAWUMaxCbTops']
                
            
        CommonMetaData.writelines(sys.stderr, ['SubHaz:', str(subHaz), '\n'])
                    
    CommonMetaData.writelines(sys.stderr, ['AMChanges: ', str(AMChanges), '\n'])
    return AMChanges