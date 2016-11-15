import CommonMetaData
from HazardConstants import *
import HazardDataAccess
from EventSet import EventSet
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
        #currentTime = datetime.datetime.utcfromtimestamp(millis / 1000)


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
    
    ###CONVECTIVE SIGMET MEGAWIDGET OPTIONS###
    def getConvectiveSigmetInputs(self, geomType, domain, modifiers):
        if geomType is not 'Polygon':
            width = self.getWidth(geomType)
        if geomType is not 'Point':
            changeType = self.changeType(geomType)
            
        if geomType in ['Point', 'LineString']:
            values = ["Severe"]
            hazards = ["hailCheckBox","windCheckBox"]
        else:
            values = []
            hazards = []
            
        correction = self.getConvectiveSigmetCorrection()
        domain = self.getConvectiveSigmetDomain(domain)
        embeddedSvr = self.getConvectiveSigmetEmbeddedSevere(geomType,values,hazards)
        modifier = self.getConvectiveSigmetModifier(modifiers)
        motion = self.getConvectiveSigmetMotion()
        tops = self.getConvectiveSigmetTops()           
        
        if geomType is 'Point':
            fields = [width, correction, domain, embeddedSvr,
                      modifier, motion, tops]
        elif geomType is 'LineString':
            fields = [changeType, width, correction, domain, embeddedSvr,
                      modifier, motion, tops]            
        else:
            fields = [changeType, correction, domain,
                      embeddedSvr, modifier, motion, tops]
        
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetGroup",
            "label": "",
            "expandHorizontally": True,
            "expandVertically": True,
            "numColumns":1,
            "fields": fields
            }
                               
        return grp
    
    def changeType(self, geomType):
        if geomType == 'Polygon':
            label = 'Change Area to Line'
        else:
            label = 'Change Line to Area'
            
        changeType = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetChangeTypeGroup",
            "label": "",
            "numColumns": 3,
            "fields": [
                       {
                        "fieldType": "Button",
                        "fieldName": "convectiveSigmetChangeType",
                        "label": label,
                        "modifyRecommender": "ChangeObjectTypeTool"
                        },
                       {
                        "fieldType": "HiddenField",
                        "fieldName": "convectiveSigmetMetaData",
                        "refreshMetadata": True, #replaces string "True"
                        "values": False,
                        },                      
            ]
        }        
        return changeType
    
    def getWidth(self, geomType):        
        if geomType == 'LineString':
            label = "Line Width (nm)"
            enable = True
        elif geomType == 'Point':
            label = "Cell Radius (nm)"
            enable = True
        
        width = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetWidthGroup",
            "label": "",
            "numColumns": 3,
            "enable": enable,
            "fields": [
                       {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveSigmetWidth",
                        "sendEveryChange": False,
                        "label": label,
                        "minValue": 10,
                        "maxValue": 60,
                        "values": 10,
                        "incrementDelta": 5,
                        "modifyRecommender": "LineAndPointTool"                             
                        },
                ]
        }

        return width
    
    def getConvectiveSigmetCorrection(self):
        millis = SimulatedTime.getSystemTime().getMillis()
        currentTime = datetime.datetime.utcfromtimestamp(millis / 1000)
        if currentTime.minute > 25:
            enable = False
        else:
            enable = True        

        
        correction = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetCorrectionGroup",
            "label": "",
            "numColumns": 3,
            "enable": enable,
            "fields": [
                       {
                        "fieldType": "CheckBox",
                        "fieldName": "convectiveSigmetCorrection",
                        "label": "Correction?",
                        "values": False,
                        },
                       {
                        "fieldType": "CheckBox",
                        "fieldName": "convectiveSigmetSpecialIssuance",
                        "label": "Special Issuance?",
                        "values": False,
                        }                       
            ]
        }
        
        return correction        
    
    def getConvectiveSigmetDomain(self,domain):
        domainChoices = ["East", "Central", "West"]
        domain = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetNumberGroup",
            "label": "",
            "numColumns": 3,
            "fields": [
                        {
                        "fieldName": "convectiveSigmetDomain",
                        "fieldType":"RadioButtons",
                        "label":"Domain:",
                        "expandHorizontally": False,
                        "choices": domainChoices,
                        "values": domain,
                        "enable": False
                        }                       
                       ]
                  }
        
        return domain
    
    def getConvectiveSigmetEmbeddedSevere(self, geomType, values, hazards):
        embeddedSvr = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetEmbeddedSvrGroup",
            "label": "",
            "numColumns": 3,
            "fields": [
                        {
                           "fieldName": "convectiveSigmetEmbeddedSvr",
                           "fieldType": "CheckBoxes",
                           "label": "Qualifier:",
                           "choices": [
                                       {
                                       "identifier": "Embedded",
                                       "displayString": "Embedded"
                                       },
                                       {
                                       "identifier": "Severe",
                                       "displayString": "Severe",
                                        }
                                       ],
                            "values": values,
                         },
                         {
                            "fieldType": "CheckBoxes",
                            "fieldName": "convectiveSigmetAdditionalHazards",
                            "label": "Additional Hazards:",
                            "enable": False,
                            "choices": [
                                       {
                                         "identifier": "tornadoesCheckBox",
                                         "displayString":"Tornadoes",
                                       },
                                       {
                                         "identifier": "hailCheckBox",
                                         "displayString":"Hail Size (inches): ",
                                         "detailFields": [
                                                     {
                                                      "fieldType": "Text",
                                                      "fieldName": "hailText",
                                                      "expandHorizontally": False,
                                                      "maxChars": 3,
                                                      "visibleChars": 3,
                                                      "values": "1"
                                                      },    
                                                     ] 
                                       },
                                       {
                                         "identifier": "windCheckBox",
                                         "displayString":"Wind Gusts (knots): ",
                                         "detailFields": [
                                                     {
                                                      "fieldType": "Text",
                                                      "fieldName": "windText",
                                                      "expandHorizontally": False,
                                                      "maxChars": 3,
                                                      "visibleChars": 3,
                                                      "values": "50"
                                                      }     
                                                     ]                         
                                       },
                                     ],
                             "values": hazards                          
                          },                                                                                                                                                  
                        ],
                    }                       

        return embeddedSvr
    
    def getConvectiveSigmetModifier(self, modifiers):
        modifier = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetModifierGroup",
            "label": "",
            "numColumns": 3,
            "fields": [
                        {
                        "fieldName": "convectiveSigmetModifier",
                        "fieldType":"ComboBox",
                        "label":"Modifier:",
                        "expandHorizontally": False,
                        "choices": modifiers,
                        }                       
                       ],
            "values": "None of the Above",
                  }
        
        return modifier
    
    def getConvectiveSigmetMotion(self):
        motion = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetMotionGroup",
            "label": "",
            "numColumns": 3,
            "fields": [
                       {
                       "fieldName": "convectiveSigmetDirection",
                       "fieldType":"IntegerSpinner",
                       "label":"Direction (ddd):",
                       "minValue": 0,
                       "maxValue": 360,
                       "values": 0,
                       "incrementDelta": 10,
                       "expandHorizontally": False,
                       "showScale": False,
                       "modifyRecommender": "LineAndPointTool"                        
                       },
                       {
                       "fieldName": "convectiveSigmetSpeed",
                       "fieldType":"IntegerSpinner",
                       "label":"Speed (kts):",
                       "minValue": 0,
                       "maxValue": 60,
                       "values": 0,
                       "incrementDelta": 5,
                       "expandHorizontally": False,
                       "showScale": False,
                       "modifyRecommender": "LineAndPointTool"
                       }                                              
                       ]
                  }
        
        return motion
    
    def getConvectiveSigmetTops(self):
        tops = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetTopsGroup",
            "label": "",
            "numColumns": 3,
            "fields": [
                       {
                       "fieldName": "convectiveSigmetCloudTop",
                       "fieldType":"RadioButtons",
                       "label":"Cloud Top Flight Level:",
                       "modifyRecommender": "LineAndPointTool",
                       "choices": [
                        {
                         "identifier": "topsAbove",
                         "displayString": "Tops above FL450",
                        },
                        {
                         "identifier": "topsTo",
                         "displayString": "Tops to FL",
                         "detailFields": [
                            {
                             "fieldType": "IntegerSpinner",
                             "fieldName": "convectiveSigmetCloudTopText",
                             "expandHorizontally": False,
                             "minValue": 150,
                             "maxValue": 450,
                             "values": 400,
                             "incrementDelta": 10,
                             "modifyRecommender": "LineAndPointTool" 
                            }
                          ]
                         },
                       ],
                       }                       
                       ]
                  }        
        
        return tops                               

    ###INTERNATIONAL SIGMET MEGAWIDGET OPTIONS###
    def getInternationalSigmetInputs(self, geomType, volcanoDict):
        volcanoNamesList = list(volcanoDict.keys())
        volcanoNamesList.sort()               
        
        if geomType in ['LineString', 'Point']:
            if geomType == 'LineString':
                label = "Line Width (nm)"
            else:
                label = "Radius (nm)"
            width = self.getInternationalSigmetWidth(label)
            
        originatingOffice = self.getInternationalSigmetOffice()
        phenomenon = self.getInternationalSigmetPhenomenon(volcanoNamesList, volcanoDict)
        additionalRemarks = self.getInternationalSigmetAdditionalRemarks()      
        if geomType in ['LineString','Point']:
            fields = [originatingOffice, width, phenomenon, additionalRemarks]           
        else:
            fields = [originatingOffice, phenomenon, additionalRemarks]
        
        grp = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetGroup",
            "label": "",
            "expandHorizontally": True,
            "expandVertically": True,
            "numColumns":1,
            "fields": fields
            }

        return grp
    
    def getInternationalSigmetWidth(self, label):        
        width = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetWidthGroup",
            "label": "",
            "numColumns": 1,
            "enable": True,
            "fields": [
                       {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "internationalSigmetWidth",
                        "sendEveryChange": False,
                        "label": label,
                        "minValue": 10,
                        "maxValue": 300,
                        "values": 40,
                        "incrementDelta": 5,                             
                        },
                ]
        }

        return width    
    
    def getInternationalSigmetOffice(self):
        
        office = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetOfficeGroup",
            "label": "",
            "numColumns": 1,
            "fields": [
                       {
                        "fieldType": "ComboBox",
                        "fieldName": "internationalSigmetOffice",
                        "label": "Originating Office:",
                        "expandHorizontally": False,
                        "values": 'KKCI',
                        "choices": ["KKCI", "PAWU", "PHFO"],                        
                        },
                       {
                        "fieldType": "ComboBox",
                        "fieldName": "internationalSigmetFIR",
                        "label": "FIR:",
                        "expandHorizontally": False,
                        "values": 'KZWY',
                        "choices": ['KZWY','KZMA','KZHU','TZJS','KZAK'],
                        },
                       {
                        "fieldType": "ComboBox",
                        "fieldName": "internationalSigmetSequence",
                        "label": "Sequence: ",
                        "expandHorizontally": False,
                        "choices": ["ALFA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT", "GOLF", "HOTEL", "INDIA", "JULIETT", "KILO", "LIMA", "MIKE"]
                       },
                       {
                        "fieldType": "CheckBox",
                        "fieldName": "internationalSigmetCancellation",
                        "label": "CANCEL THIS SERIES",           
                       },                                                                    
            ]
        }
        return office
    
    def getInternationalSigmetPhenomenon(self, volcanoNamesList, volcanoDict):       
        phenomenon = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetPhenomenonGroup",
            "numColumns": 2,
            "fields": [                     
                  {
                   "fieldType": "DetailedComboBox",
                   "fieldName": "internationalSigmetPhenomenonComboBox",
                   "label": "Phenomenon:",
                   "values": "obscuredThunderstorms",
                   "modifyRecommender": "CreateVALayerForecastTool",
                   "choices": [
                               {
                                "identifier": "obscuredThunderstorms",
                                "displayString": "Obscured Thunderstorms",
                                "detailFields": [
                                                 {
                                                  "fieldType": "ComboBox",
                                                  "fieldName": "cbTops",
                                                  "expandHorizontally": False,
                                                  "label": "Max Tops (Cb): ",
                                                  "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                              "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                              "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                              "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                              "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                              "FL500"],
                                                  "values": "FL300",
                                                  },
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetMovement",
                                                  "expandHorizontally": True,
                                                  "label": "Movement: ",
                                                  "numColumns": 2,
                                                  "fields": [
                                                             {
                                                              "fieldType": "IntegerSpinner",
                                                              "fieldName": "internationalSigmetSpeed",
                                                              "label": "Speed (kts):",
                                                              "minValue": 0,
                                                              "maxValue": 100,
                                                              "values": 0,
                                                              "incrementDelta": 5,
                                                              "expandHorizontally": False,
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetDirection",
                                                              "expandHorizontally": False,
                                                              "label": "Towards:",
                                                              "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                                          "WNW","NW","NNW"
                                                                          ]
                                                              },
                                                             ]
                                                  },
                                                  {
                                                   "fieldType": "Group",
                                                   "fieldName": "internationalSigmetFcstObsGroup",
                                                   "expandHorizontally": True,
                                                   "label": "This is:",
                                                   "numColumns": 1,
                                                   "fields": [
                                                             {
                                                              "fieldType": "RadioButtons",
                                                              "fieldName": "internationalSigmetFcstObs",
                                                              "expandHorizontally": True,
                                                              "label": "",
                                                              "choices": [
                                                                          "Forecast",
                                                                          "Observed",
                                                                          ]
                                                             },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetIntensity",
                                                              "expandHorizontally": False,
                                                              "label": "Intensity Trend:",
                                                              "choices": ['No Change','Intensifying','Weakening'],
                                                              "values": 'No Change',                                    
                                                             },
                                                    ] 
                                                  },                                                                                               
                                                 ],
                                },
                               {
                                "identifier": "embeddedThunderstorms",
                                "displayString": "Embedded Thunderstorms",
                                "detailFields": "obscuredThunderstorms"
                                },
                               {
                                "identifier": "frequentThunderstorms",
                                "displayString": "Frequent Thunderstorms",
                                "detailFields": "obscuredThunderstorms"
                                }, 
                               {
                                "identifier": "squallLineThunderstorms",
                                "displayString": "Squall Line",
                                "detailFields": "obscuredThunderstorms"
                                },                                
                               {
                                "identifier": "widespreadThunderstorms",
                                "displayString": "Widespread Thunderstorms",
                                "detailFields": "obscuredThunderstorms"
                                },
                               {
                                "identifier": "isolatedSevereThunderstorms",
                                "displayString": "Isolated Severe Thunderstorms",
                                "detailFields": "obscuredThunderstorms"
                                },
                               {
                                "identifier": "turbulence",
                                "displayString": "Severe Turbulence",
                                "detailFields": [
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetTurbulenceExtentSubGroup",
                                                  "expandHorizontally": True,
                                                  "numColumns": 2,
                                                  "fields": [
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetTurbulenceExtentBottom",
                                                              "expandHorizontally": False,
                                                              "label": "Bottom: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL200",
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetTurbulenceExtentTop",
                                                              "expandHorizontally": False,
                                                              "label": "Top: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL300",
                                                              },                                                                                               
                                                             ],
                                                  },
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetTurbulenceMovement",
                                                  "expandHorizontally": True,
                                                  "label": "Movement: ",
                                                  "numColumns": 2,
                                                  "fields": [
                                                             {
                                                              "fieldType": "IntegerSpinner",
                                                              "fieldName": "internationalSigmetTurbulenceSpeed",
                                                              "label": "Speed (kts):",
                                                              "minValue": 0,
                                                              "maxValue": 100,
                                                              "values": 0,
                                                              "incrementDelta": 5,
                                                              "expandHorizontally": False,
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetTurbulenceDirection",
                                                              "expandHorizontally": False,
                                                              "label": "Towards:",
                                                              "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                                          "WNW","NW","NNW"
                                                                          ]
                                                              },
                                                             ]
                                                  },
                                                  {
                                                   "fieldType": "Group",
                                                   "fieldName": "internationalSigmetTurbObsGroup",
                                                   "expandHorizontally": True,
                                                   "label": "This is:",
                                                   "numColumns": 1,
                                                   "fields": [
                                                             {
                                                              "fieldType": "RadioButtons",
                                                              "fieldName": "internationalSigmetTurbObs",
                                                              "expandHorizontally": False,
                                                              "label": "",
                                                              "choices": [
                                                                          "Forecast",
                                                                          "Observed",
                                                                          ]
                                                             },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetTurbIntensity",
                                                              "expandHorizontally": False,
                                                              "label": "Intensity Trend:",
                                                              "choices": ['No Change','Intensifying','Weakening'],
                                                              "values": 'No Change',                                    
                                                             },                                                               
                                                    ] 
                                                  },                                                                        
                                                 ],
                                },
                               {
                                "identifier": "icing",
                                "displayString": "Severe Icing",
                                "detailFields": "turbulence"
                                }, 
                               {
                                "identifier": "icingFzra",
                                "displayString": "Severe Icing with FZRA",
                                "detailFields": "turbulence"
                                }, 
                               {
                                "identifier": "dustStorm",
                                "displayString": "Dust Storm",
                                "detailFields": "turbulence"
                                },
                               {
                                "identifier": "sandStorm",
                                "displayString": "Sand Storm",
                                "detailFields": "turbulence"
                                },
                               {
                                "identifier": "radioactiveRelease",
                                "displayString": "Radioactive Release",
                                "detailFields": "turbulence"
                                },
                               {
                                "identifier": "severeMountainWave",
                                "displayString": "Severe Mountain Wave",
                                "detailFields": "turbulence"
                                },                                                                                                                                                                                                                                                                                                                                                         
                               {
                                "identifier": "tropicalCyclone",
                                "displayString": "Tropical Cyclone",
                                "detailFields": [                                                  
                                                 {
                                                  "fieldType": "Text",
                                                  "fieldName": "internationalSigmetTCName",
                                                  "label": "Tropical Cyclone Name:",
                                                  "visibleChars": 12,
                                                  "lines": 1,
                                                  "expandHorizontally": False,
                                                  },
                                                 {
                                                  "fieldType": "Button",
                                                  "fieldName": "internationalSigmetTCPopulate",
                                                  "label": "Automatically Populate MetaData",
                                                  "modifyRecommender": "PopulateTCMetaDataTool"
                                                  },
                                                 {
                                                  "fieldType": "HiddenField",
                                                  "fieldName": "internationalSigmetTCMetaData",
                                                  "refreshMetadata": True,
                                                  "values": False,
                                                  },                                                  
                                                 {
                                                  "fieldType": "ComboBox",
                                                  "fieldName": "internationalSigmetTCObsTime",
                                                  "expandHorizontally": False,
                                                  "label": "Observation Time (UTC)",
                                                  "choices": ['0000','0300','0600','0900','1200','1500','1800','2100'],
                                                  "values": '0000',
                                                  },
                                                 {
                                                  "fieldType": "Text",
                                                  "fieldName": "internationalSigmetTCPosition",
                                                  "label": "Current Center Position:",
                                                  "visible Chars": 12,
                                                  "lines": 1,
                                                  "expandHorizontally": False,
                                                  },
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetTCMovement",
                                                  "expandHorizontally": True,
                                                  "label": "Current Motion: ",
                                                  "numColumns": 2,
                                                  "fields": [
                                                             {
                                                              "fieldType": "IntegerSpinner",
                                                              "fieldName": "internationalSigmetTCSpeed",
                                                              "label": "Speed (kts):",
                                                              "minValue": 0,
                                                              "maxValue": 50,
                                                              "values": 10,
                                                              "incrementDelta": 1,
                                                              "expandHorizontally": False,
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetTCDirection",
                                                              "expandHorizontally": False,
                                                              "label": "Towards:",
                                                              "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                                          "WNW","NW","NNW"
                                                                          ]
                                                              },
                                                             ]
                                                  },
                                                 {
                                                  "fieldType": "ComboBox",
                                                  "fieldName": "internationalSigmetTCIntensity",
                                                  "expandHorizontally": False,
                                                  "label": "Intensity Trend:",
                                                  "choices": ['No Change','Intensifying','Weakening'],
                                                  "values": 'No Change',                                    
                                                  },                                                                                                                                                   
                                                 {
                                                  "fieldType": "ComboBox",
                                                  "fieldName": "cbTopsTC",
                                                  "expandHorizontally": False,
                                                  "label": "Max Tops (Cb):",
                                                  "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                              "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                              "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                              "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                              "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                              "FL500"],
                                                  "values": "FL300",
                                                  },
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetTCFcst",
                                                  "expandHorizontally": True,
                                                  "label": "Forecast Time And Position",
                                                  "numColumns": 2,
                                                  "fields": [
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetTCFcstTime",
                                                              "expandHorizontally": True,
                                                              "label": "Forecast Time (UTC)",
                                                              "choices": ['0000','0300','0600','0900','1200','1500','1800','2100'],
                                                              "values": '0000',
                                                              },
                                                            {
                                                             "fieldType": "Text",
                                                             "fieldName": "internationalSigmetTCFcstPosition",
                                                             "label": "Forecast Center Position:",
                                                             "visible Chars": 12,
                                                             "lines": 1,
                                                             "expandHorizontally": True,
                                                             },
                                                             ]
                                                  },                                                
                                                 ],
                                },
                               {
                                "identifier": "volcanicAsh",
                                "displayString": "Volcanic Ash",
                                "detailFields": [
                                                 {
                                                  "fieldType": "ComboBox",
                                                  "fieldName": "internationalSigmetVolcanoNameVA",
                                                  "autocomplete": True,
                                                  "label": "Select Volcano Name:",
                                                  "choices": volcanoNamesList,
                                                  "values": volcanoNamesList[0],                                               
                                                  },                                               
                                                 {
                                                 "fieldType": "IntegerSpinner",
                                                 "fieldName": "internationalSigmetVALayersSpinner",
                                                 "label": "Number of Layers: ",
                                                 "modifyRecommender": "CreateVALayerForecastTool",
                                                 "minValue": 1,
                                                 "maxValue": 3,
                                                 "increment": 1,
                                                 "expandHorizontally": False,
                                                 },                                                 
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetVALayer1",
                                                  "expandHorizontally": True,
                                                  "numColumns": 2,
                                                  "label": "Layer 1",
                                                  "fields": [
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVAExtentBottom1",
                                                              "expandHorizontally": False,
                                                              "label": "Bottom: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL200",
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVAExtentTop1",
                                                              "expandHorizontally": False,
                                                              "label": "Top: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL300",
                                                              }, 
                                                             {
                                                              "fieldType": "IntegerSpinner",
                                                              "fieldName": "internationalSigmetVASpeed1",
                                                              "label": "Speed (kts):",
                                                              "minValue": 0,
                                                              "maxValue": 100,
                                                              "values": 0,
                                                              "incrementDelta": 5,
                                                              "expandHorizontally": False,
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVADirection1",
                                                              "expandHorizontally": False,
                                                              "label": "Towards:",
                                                              "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                                          "WNW","NW","NNW"
                                                                          ]
                                                              },                                                                                                                                                           
                                                             ],
                                                  },
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetVALayer2",
                                                  "expandHorizontally": True,
                                                  "enable": False,
                                                  "numColumns": 2,
                                                  "label": "Layer 2",
                                                  "fields": [
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVAExtentBottom2",
                                                              "expandHorizontally": False,
                                                              "label": "Bottom: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL200",
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVAExtentTop2",
                                                              "expandHorizontally": False,
                                                              "label": "Top: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL300",
                                                              }, 
                                                             {
                                                              "fieldType": "IntegerSpinner",
                                                              "fieldName": "internationalSigmetVASpeed2",
                                                              "label": "Speed (kts):",
                                                              "minValue": 0,
                                                              "maxValue": 100,
                                                              "values": 0,
                                                              "incrementDelta": 5,
                                                              "expandHorizontally": False,
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVADirection2",
                                                              "expandHorizontally": False,
                                                              "label": "Towards:",
                                                              "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                                          "WNW","NW","NNW"
                                                                          ]
                                                              },                                                                                                                                                           
                                                             ],
                                                  },
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetVALayer3",
                                                  "expandHorizontally": True,
                                                  "enable": False,
                                                  "numColumns": 2,
                                                  "label": "Layer 3",
                                                  "fields": [
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVAExtentBottom3",
                                                              "expandHorizontally": False,
                                                              "label": "Bottom: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL200",
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVAExtentTop3",
                                                              "expandHorizontally": False,
                                                              "label": "Top: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL300",
                                                              }, 
                                                             {
                                                              "fieldType": "IntegerSpinner",
                                                              "fieldName": "internationalSigmetVASpeed3",
                                                              "label": "Speed (kts):",
                                                              "minValue": 0,
                                                              "maxValue": 100,
                                                              "values": 0,
                                                              "incrementDelta": 5,
                                                              "expandHorizontally": False,
                                                              },
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetVADirection3",
                                                              "expandHorizontally": False,
                                                              "label": "Towards:",
                                                              "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                                          "WNW","NW","NNW"
                                                                          ]
                                                              },                                                                                                                                                           
                                                             ],
                                                  },                                                                                                                                                                                                
                                                 ],
                                },
                               {
                                "identifier": "volcanicEruption",
                                "displayString": "Volcanic Eruption",
                                "detailFields": [
                                                 {
                                                  "fieldType": "ComboBox",
                                                  "fieldName": "internationalSigmetVolcanoNameEruption",
                                                  "autocomplete": True,
                                                  "label": "Select Volcano Name:",
                                                  "choices": volcanoNamesList,
                                                  "values": volcanoNamesList[0],                                               
                                                  },
                                                 {
                                                  "fieldType": "TimeScale",
                                                  "fieldName": "internationalSigmetVolcanoEruptionTime",
                                                  "valueLabels": {"internationalSigmetVolcanoEruptionTime": "Time of Eruption (UTC):"},
                                                  },                                                 
                                                 {
                                                  "fieldType": "CheckBoxes",
                                                  "fieldName": "internationalSigmetEruptionIndicator",
                                                  "label": "Indicated By:",
                                                  "values": "avo",
                                                  "choices": [
                                                              {"identifier": "mt-sat",
                                                               "displayString": "MT-SAT"},
                                                              {"identifier": "goes",
                                                               "displayString": "GOES"},
                                                              {"identifier": "poes",
                                                               "displayString": "POES"},
                                                              {"identifier": "avo",
                                                               "displayString": "AVO"},
                                                              {"identifier": "kvert",
                                                               "displayString": "KVERT"},
                                                              {"identifier": "pilot",
                                                               "displayString": "PILOT REPORT"},
                                                              {"identifier": "radar",
                                                               "displayString": "RADAR"},                                                                                                                                                                                                                                                                                                                                                                                    
                                                              ]
                                                  },                                                
                                                 {
                                                  "fieldType": "Group",
                                                  "fieldName": "internationalSigmetEruptionLayer",
                                                  "expandHorizontally": True,
                                                  "numColumns": 2,
                                                  "fields": [
                                                             {
                                                              "fieldType": "ComboBox",
                                                              "fieldName": "internationalSigmetEruptionExtentTop",
                                                              "expandHorizontally": False,
                                                              "label": "Estimated Ash Top: ",
                                                              "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                                          "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                                          "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                                          "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                                          "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                                          "FL500"],
                                                              "values": "FL300",
                                                              },                                                                                                                                                           
                                                             ],
                                                  },                                                                                                                                               
                                                 ],
                                },                                 
                        ],
                  },
            ]
        }        
        return phenomenon
    
    def getInternationalSigmetAdditionalRemarks(self):
        additionalRemarks = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetAdditionalRemarksGroup",
            "label": "Additional Remarks",
            "numColumns": 1,
            "enable": True,
            "fields": [
                       {
                        "fieldType": "ComboBox",
                        "fieldName": "internationalSigmetCannedRemarks",
                        "expandHorizontally": True,
                        "label": "Canned Remarks:",
                        "choices": ["None", "Canned Response 1", "Canned Response 2"],
                        "values": "None", 
                        },
                       {
                        "fieldType": "Text",
                        "fieldName": "internationalSigmetAdditionalRemarks",
                        "label": "Custom Remarks:",
                        "visibleChars": 40,
                        "lines": 4,
                        "expandHorizontally": True,
                        "values": "***ADD CUSTOM REMARKS HERE...DELETE IF NOT USED***"                            
                        },
                ]
        }     
        
        return additionalRemarks             
    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    import sys
    sys.stderr.writelines( ['Hello World!\n'])
    
    ###FOR CONVECTIVE SIGMET MUST HAVE HAIL/WIND IF SEVERE IS CHECKED###
    if triggerIdentifiers is None or "convectiveSigmetEmbeddedSvr" in triggerIdentifiers:
        if triggerIdentifiers is None:
            return None
        else:
            if "Severe" in mutableProperties["convectiveSigmetEmbeddedSvr"]["values"]:
                return {
                      "convectiveSigmetAdditionalHazards": {
                                "enable": True,
                                "values": ["hailCheckBox", "windCheckBox"]
                      }
                }
            else:
                return {
                      "convectiveSigmetAdditionalHazards": {
                                "enable": False,
                                "values": []
                      }
                }
    else:
        return None
    
    ###DISABLE EVERYTHING IF CHOOSING TO CANCEL INTERNATIONAL SIGMET###
    if triggerIdentifiers is None or "internationalSigmetCancellation" in triggerIdentifiers:
        if mutableProperties["internationalSigmetCancellation"]["values"] == True:
            return {
                    "internationalSigmetPhenomenonGroup": {
                            "enable": False,                                      
                    }
            }
        else:
            return {
                    "internationalSigmetPhenomenonGroup": {
                            "enable": True,                                      
                    }
            }            

    ###CONTROLLING SERIES NAMES FOR INTERNATIONAL SIGMET BASED ON ORIGINATING OFFICE(MWO)###
    if triggerIdentifiers is None or "internationalSigmetOffice" in triggerIdentifiers:
        if "KKCI" in mutableProperties["internationalSigmetOffice"]["values"]:
            return {
                  "internationalSigmetSequence": {
                            "choices": ["ALFA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT", "GOLF", "HOTEL", "INDIA", "JULIETT", "KILO", "LIMA", "MIKE"]                                
                  },
                  "internationalSigmetFIR": {
                            "choices": ['KZWY','KZMA','KZHU','TZJS','KZAK']                                
                  }                             
            }
        elif "PAWU" in mutableProperties["internationalSigmetOffice"]["values"]:
            return {
                  "internationalSigmetSequence": {
                            "choices": ["INDIA", "JULIETT", "KILO", "LIMA", "MIKE", "NOVEMBER", "OSCAR", "PAPA", "QUEBEC"]                                
                  },
                  "internationalSigmetFIR": {
                            "choices": ['PAZA'],
                            "enable": False,                                
                  }                              
            }
        else:
            return {
                  "internationalSigmetSequence": {
                            "choices": ["NOVEMBER", "OSCAR", "PAPA", "QUEBEC", "ROMEO", "SIERRA", "TANGO", "UNIFORM", "VICTOR", "WHISKEY", "XRAY", "YANKEE", "ZULU"]                                
                  },
                  "internationalSigmetFIR": {
                            "choices": ['KZAK'],
                            "enable": False,                                
                  }                            
            }  
            
    ###CONTROLLING LAYER OPTION FOR INTL SIGMET BASED ON VA SELECTION###
    if triggerIdentifiers is None or "internationalSigmetVALayersSpinner" in triggerIdentifiers:
        if mutableProperties["internationalSigmetVALayersSpinner"]["values"] == 1:
            return {
                    "internationalSigmetVALayer3": {
                          "enable": False
                    },
                    "internationalSigmetVALayer2": {
                          "enable": False
                    },
                    "internationalSigmetVALayer1": {
                          "enable": True
                    },    
            }                      
        elif mutableProperties["internationalSigmetVALayersSpinner"]["values"] == 2:
            return {
                    "internationalSigmetVALayer3": {
                              "enable": False                                
                    },
                    "internationalSigmetVALayer2": {
                              "enable": True                                
                    },
                    "internationalSigmetVALayer1": {
                              "enable": True                                
                    },
            }
        else:
            return {
                    "internationalSigmetVALayer3": {
                              "enable": True                                
                    },
                    "internationalSigmetVALayer2": {
                              "enable": True                                
                    },
                    "internationalSigmetVALayer1": {
                              "enable": True                                
                    },                                                    
            }
            
    ###AIRMET INTERDEPENDECIES###
    if triggerIdentifiers is None or "airmetOffice" in triggerIdentifiers:
        if "KKCI" in mutableProperties["airmetOffice"]["values"]:
            return {
                  "airmetZone": {
                            "choices": ['SFO','SLC','DFW','CHI','BOS','MIA']                                
                  }                             
            }
        elif "PAWU" in mutableProperties["airmetOffice"]["values"]:
            return {
                  "airmetZone": {
                            "choices": ['01 Arctic Coast Coastal',
                                        '02 North Slopes of Brooks Range',
                                        '03 Upper Yukon Valley',
                                        '04 Koyukuk and Upper Kobuk Valley',
                                        '05 Northern Seward Peninsula - Lower Kobuk Valley',
                                        '06 Southern Seward Peninsula - Eastern Norton Sound',
                                        '07 Tanana Valley',
                                        '08 Lower Yukon Valley',
                                        '09 Kuskokwim Valley',
                                        '10 Yukon-Kuskokwim Delta',
                                        '11 Bristol Bay',
                                        '12 Lynn Canal and Glacier Bay',
                                        '13 Central Southeast Alaska',
                                        '14 Southern Southeast Alaska',
                                        '15 Coastal Southeast Alaska',
                                        '16 Eastern Gulf Coast',
                                        '17 Copper River Basin',
                                        '18 Cook Inlet-Susitna Valley',
                                        '19 Central Gulf Coast',
                                        '20 Kodiak Island',
                                        '21 Alaska Peninsula - Port Heiden to Unimak Pass',
                                        '22 Unimak Pass to Adak',
                                        '23 St.Lawrence Island-Bering Sea Coast',
                                        '24 Adak to Attu',
                                        '25 Pribilof Islands and Southeast Bering Sea'],
                            "enable": True,                                
                  }                              
            }
        else:
            return {
                  "airmetZone": {
                            "choices": ['From Kauai to Maui', 'Immediately south through west of mountains'],
                            "enable": True,                                
                  }                            
            }              
        
    
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