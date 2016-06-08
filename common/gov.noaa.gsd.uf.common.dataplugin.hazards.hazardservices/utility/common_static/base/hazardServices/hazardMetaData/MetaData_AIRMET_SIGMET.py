import CommonMetaData
from HazardConstants import *
import datetime
import json
from com.raytheon.uf.common.time import SimulatedTime
import sys

class MetaData(CommonMetaData.MetaData):
    
    
    def AAWUinitialize(self, hazardEvent, metaDict):
        self.initialize(hazardEvent, metaDict)
        
        sys.stderr.writelines(['Calling AIRMET_SIGMET', '\n'])
        
        self._flightLevels = [ "N/A", "SFC", "FL010", "FL020", "FL030", "FL040", "FL050",
                                    "FL060", "FL070", "FL080", "FL090", "FL100", "FL110",
                                    "FL120", "FL130", "FL140", "FL150", "FL160", "FL170",
                                    "FL180", "FL190", "FL200", "FL210", "FL220", "FL230",
                                    "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                    "FL300", "FL310", "FL320", "FL330", "FL340", "FL350",
                                    "FL360", "FL370", "FL380", "FL390", "FL400", "FL410",
                                    "FL420", "FL430", "FL440", "FL450" ]
        
        self._compassDirs16pt = ["STNR", "E", "ENE", "ESE", "N", "NE", "NNE", "NNW", "NW", "S", "SE", "SSE",
                                 "SSW", "SW", "W", "WNW", "WSW"]
        
        self._allDesigs = ["ALPHA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT", "GOLF", "HOTEL",
                                "INDIA", "JULIET", "KILO", "LIMA", "MIKE", "NOVEMBER","OSCAR", "PAPA",
                                "QUEBEC", "ROMEO", "SIERRA", "TANGO", "UNIFORM", "VICTOR", "WHISKEY",
                                "XRAY", "YANKEE", "ZULU"]
        
    
        #millis = SimulatedTime.getSystemTime().getMillis()
        #currentTime = datetime.datetime.fromtimestamp(millis / 1000)


    def getAdvisoryType(self, adv):
        #=======================================================================
        # aTypeLabel = {
        #     "fieldName": "AAWUAdvisoryTypeLabel",
        #     "fieldType":"Label",
        #     "label":"Advisory Type:",
        #     "bold": False,
        #     "italic": False,
        #     "expandHorizontally": False,
        #     }
        # aType = {
        #     "fieldName": "AAWUAdvisoryType",
        #     "fieldType":"Label",
        #     "label":adv,
        #     "bold": True,
        #     "italic": True,
        #     "expandHorizontally": False,
        #     }
        #  
        # aTypeGroup = {
        #        "fieldType":"Group",
        #        "fieldName":"AAWUAdvisoryTypeGroup",
        #        "fields": [aTypeLabel, aType]
        #                }
        #  
        #  
        # 
        # return aTypeGroup
        #=======================================================================
    
        ### Using ComboBox instead of Label since Label does not have
        ### 'values' attribute and therefore it is not passed as a 
        ### hazard attribute
        aType = {
            "fieldName": "AAWUAdvisoryType",
            "fieldType":"ComboBox",
            "label":"Advisory Type:",
            "values": adv,
            "expandHorizontally": False,
            "editable":False,
            "choices": [adv]
            }
        
        return aType

        
        
    def getHazardType(self, types):
        
        hType = {
            "fieldName": "AAWUHazardType",
            "fieldType":"ComboBox",
            "label":"Hazard Type:",
            #"values": "",
            "expandHorizontally": False,
            "choices": types
            }
        
        return hType
    
    def getHazardSubtype(self, selectedHazard=None):
        
        hazType = {
                   "Thunderstorm": ["Obscured", "Embedded", "Widespread", "Squall Line", "Isolated Severe"],
                   "Severe Icing": ["Not Applicable", "with Freezing Rain"]
                   }
        
        choice = hazType.get(selectedHazard)
        if choice is None:
            choice = ["Not Applicable"]
        
        subType = {
            "fieldName": "AAWUHazardSubType",
            "fieldType":"ComboBox",
            "label":"Hazard Subtype:",
            "values": choice[0],
            "expandHorizontally": False,
            "choices": choice,
            "enable":False
            }
        
        return subType
    
    def getVerticalExtent(self):
        
        
        topChoices = {
            "fieldName": "AAWUVerticalExtentTop_1",
            "fieldType":"ComboBox",
            "label":"Top:",
            #"values": "",
            "expandHorizontally": False,
            "choices": self._flightLevels
            }

        bottomChoices = {
            "fieldName": "AAWUVerticalExtentBottom_1",
            "fieldType":"ComboBox",
            "label":"Bottom:",
            #"values": "",
            "expandHorizontally": False,
            "choices": self._flightLevels
            }

        extentGroup = {
               "fieldType":"Group",
               "fieldName":"AAWUVerticalExtentGroup",
               #"numColumns":2,
               "fields": [topChoices, bottomChoices]
               }
        
        return extentGroup
    
    def getMaxCbTops(self):
        maxCbTops = {
            "fieldName": "AAWUMaxCbTops",
            "fieldType":"ComboBox",
            "label":"Max Cb Tops:",
            #"values": "",
            "expandHorizontally": False,
            "choices": self._flightLevels
            }
        return maxCbTops
    
    def getMovement(self):
        speed = {
                    "fieldType": "IntegerSpinner",
                    "fieldName": "AAWUMovementSpeed_1",
                    "label": "Speed (kts):",
                    "minValue": 1,
                    "maxValue": 100,
                    "values": 1,
                    "incrementDelta": 5,
                    "sendEveryChange": False,
                    "expandHorizontally": True,
                    "showScale": False
                 }
        
        dir = {
            "fieldName": "AAWUMovementToward_1",
            "fieldType":"ComboBox",
            "label":"Toward:",
            #"values": "",
            "expandHorizontally": False,
            "choices": self._compassDirs16pt
            }
    
        movementGroup = {
               "fieldType":"Group",
               "fieldName":"AAWUMovementGroup",
               #"numColumns":2,
               "fields": [speed, dir]
               }
        
        return movementGroup
    
    def getIntensity(self):
        intensity = {
            "fieldName": "AAWUIntensity",
            "fieldType":"ComboBox",
            "label":"Intensity:",
            #"values": "",
            "expandHorizontally": False,
            "choices": ["No Change", "Intensifying", "Weakening"]
            }
        
        return intensity
    
    def getForecastOrObserved(self):
        fcstOrObs = {
                    "fieldType": "RadioButtons",
                    "label": "This is:",
                    "fieldName": "AAWUForecastOrObserved",
                    "choices": ["Forecast", "Observed"]
                    }
        return fcstOrObs
    
    def getAdvisoryArea(self, advType):
        
        choices = ["Draw Freehand On Map", 
                   "Select Zones From Map", 
                   {
                        "identifier": "AAWUSZL",
                        "displayString": "",
                        "detailFields": [
                        {
                            "fieldName": "AAWUSeriesZonesList",
                            "fieldType": "ComboBox",
                            "label": "Select Zones From List:",
                            "choices": [
                               'ADAK TO ATTU',
                               'AK PEN',
                               'ARCTIC SLP CSTL',
                               'BRISTOL BAY',
                               'CNTRL GLF CST',
                               'CNTRL SE AK',
                               'COOK INLET AND SUSITNA VALLEY',
                               'COPPER RIVER BASIN',
                               'ERN GLF CST',
                               'KODIAK IS',
                               'KOYUKUK AND UPR KOBUK VLY',
                               'KUSKOKWIM VLY',
                               'LWR YKN VLY',
                               'LYNN CANAL AND GLACIER BAY',
                               'NORTH SLOPES OF BROOKS RANGE',
                               'NRN SEWARD PEN AND LWR KOBUK VLY',
                               'PRIBILOF ISLANDS AND SOUTHEAST BERING SEA',
                               'SE AK CSTL WTRS',
                               'SRN SE AK',
                               'SRN SEWARD PEN AND ERN NORTON SOUND',
                               'ST LAWRENCE IS AND WRN NORTON SOUND',
                               'TANANA VLY',
                               'UNIMAK PASS TO ADAK',
                               'UPR YKN VLY',
                               'YKN-KUSKOKWIM VLY',
                            ]
                         
                         }
                        ]
                     }
                   ]
        
        if advType == 'SIGMET':
            thisChoice = [choices[0]]
        else:
            thisChoice = choices
        
        advArea = {
                    "fieldType": "RadioButtons",
                    "label": "Define Advisory Area:",
                    "fieldName": "AAWUAdvisoryArea",
                    "choices": thisChoice
                    }
        return advArea
    
    def getAdvisoryName(self, desigs):
        number = {
                    "fieldType": "IntegerSpinner",
                    "fieldName": "AAWUAdvisoryNumber",
                    "label": "Number:",
                    "minValue": 1,
                    "maxValue": 20,
                    "values": 1,
                    "sendEveryChange": False,
                    "expandHorizontally": True,
                    "showScale": False,
                    "enable":False
                 }
        
        series = {
            "fieldName": "AAWUAdvisorySeries",
            "fieldType":"ComboBox",
            "label":"Series:",
            "values": desigs[0],
            "expandHorizontally": False,
            "choices": self._allDesigs,
            "enable":False
            }
    
        override = {
                    "fieldType": "CheckBox",
                    "fieldName": "AAWUSeriesOverride",
                    "label": "Override Auto:",
                    "values": False
                    }

    
        seriesGroup = {
               "fieldType":"Group",
               "fieldName":"AAWUSeriesGroup",
               #"numColumns":2,
               "fields": [series, number, override]
               }
        
        return seriesGroup
    
    def getProdType(self):
        ### Using ComboBox instead of Label since Label does not have
        ### 'values' attribute and therefore it is not passed as a 
        ### hazard attribute
        aType = {
            "fieldName": "AAWUProdType",
            "fieldType":"ComboBox",
            "label":"Prod Type:",
            "values": 'NO ASH',
            "expandHorizontally": False,
            "editable":False,
            "enabled":False,
            "choices": ['NO ASH']
            }
        
        return aType

    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    import sys
    sys.stderr.writelines( ['Hello World!\n'])
    
    seriesOverride = None
    if triggerIdentifiers:
        seriesOverride = {}
        sys.stderr.writelines( [str(triggerIdentifiers),'\n=====\n'])
        for ti in triggerIdentifiers:
            if ti.find('AAWUSeriesOverride') >= 0:
                sys.stderr.writelines( [str(mutableProperties.get('AAWUSeriesOverride')),'\n'])
                val = mutableProperties.get('AAWUSeriesOverride')['values']
                seriesOverride['AAWUAdvisorySeries'] = {"enable":val}
                seriesOverride['AAWUAdvisoryNumber'] = {"enable":val}
                
        
    sys.stderr.writelines(['Override:', str(seriesOverride), '\n\n'])
    return seriesOverride