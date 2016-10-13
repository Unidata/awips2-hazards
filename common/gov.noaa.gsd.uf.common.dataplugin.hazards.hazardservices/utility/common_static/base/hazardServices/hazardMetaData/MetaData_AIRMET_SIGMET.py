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
    
    ###INTERNATIONAL SIGMET MEGAWIDGET OPTIONS###
    def getInternationalSigmetInputs(self, geomType):
        if geomType is 'LineString':
            width = self.getInternationalSigmetWidth()
        
        originatingOffice = self.getInternationalSigmetOffice()
        phenomenon = self.getInternationalSigmetPhenomenon()
        extent = self.getInternationalSigmetExtent()
        additionalDetails = self.getInternationalSigmetAdditionalDetails()       

        if geomType is 'LineString':
            fields = [width, originatingOffice, phenomenon, extent, additionalDetails]           
        else:
            fields = [originatingOffice, phenomenon, extent, additionalDetails]
        
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
    
    def getInternationalSigmetWidth(self):        
        width = {
            "fieldType": "Group",
            "fieldName": "convectiveSigmetWidthGroup",
            "label": "",
            "numColumns": 1,
            "enable": True,
            "fields": [
                       {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveSigmetWidth",
                        "sendEveryChange": False,
                        "label": "Line Width (nm)",
                        "minValue": 10,
                        "maxValue": 60,
                        "values": 10,
                        "incrementDelta": 5,
                        #"modifyRecommender": "LineAndPointTool"                             
                        },
                ]
        }

        return width    
    
    def getInternationalSigmetOffice(self):
        
        office = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetOfficeGroup",
            "label": "",
            "numColumns": 3,
            "expandHorizontally": False,
            "fields": [
                       {
                        "fieldType": "RadioButtons",
                        "fieldName": "internationalSigmetOffice",
                        "label": "Originating Office:",
                        "expandHorizontally": False,
                        "values": 'KKCI',
                        "choices": [
                                    "KKCI",
                                    "PANC",
                                    "PHFO",
                                    ]                        
                        },
                       {
                        "fieldType": "ComboBox",
                        "fieldName": "internationalSigmetSequence",
                        "label": "Sequence: ",
                        "expandHorizontally": False,
                        "choices": ["ALPHA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT", "GOLF", "HOTEL", "INDIA", "JULIETT", "KILO", "LIMA", "MIKE"]
                       },                                             
            ]
        }
        return office
    
    def getInternationalSigmetPhenomenon(self):       
        phenomenon = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetPhenomenonGroup",
            "numColumns": 3,
            "fields": [                     
                  {
                   "fieldType": "ComboBox",
                   "fieldName": "internationalSigmetPhenomenon",
                   "label": "Phenomenon:",
                   "choices": ['Obscured Thunderstorms', 'Embedded Thunderstorms', 'Frequent Thunderstorms',
                               'Squall Line', 'Widespread Thunderstorms', 'Isolated Severe Thunderstorms',
                               'Turbulence', 'Severe Icing', 'Icing with Freezing Rain', 'Dust Storm',
                               'Sand Storm', 'Tropical Cyclone', 'Volcanic Ash'
                               ]
                  },
            ]
        }        
        return phenomenon
    
    def getInternationalSigmetExtent(self):
        extent = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetExtentGroup",
            "label": "Extent:",
            "numColumns": 3,
            "fields": [
                       {
                        "fieldType": "RadioButtons",
                        "fieldName": "internationalSigmetExtent",
                        "choices": ['Between','Below','Top'],
                        "values": 'Between',
                        "expandHorizontally": False,
                        },
                       {
                        "fieldType": "Group",
                        "fieldName": "internationalSigmetExtentSubGroup",
                        "expandHorizontally": False,
                        #"numColumns": 1,
                        "fields": [
                           {
                            "fieldType": "IntegerSpinner",
                            "fieldName": "internationalSigmetExtentBottom",
                            "label": "Bottom:  FL",
                            "minValue": 0,
                            "maxValue": 600,
                            "incrementDelta": 10,
                            "values": 100,
                            "expandHorizontally": False,
                            },
                           {
                            "fieldType": "ComboBox",
                            "fieldName": "internationalSigmetExtentTop",
                            "expandHorizontally": False,
                            "label": "Top: ",
                            "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                        "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                        "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                        "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                        "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                        "FL500"
                                        ],
                            "values": "FL300",
                            },                                                                                               
                          ],
                        },         
            ]
        }                  
        return extent
    
    def getInternationalSigmetAdditionalDetails(self):
        additionalDetails = {
            "fieldType": "Group",
            "fieldName": "internationalSigmetAdditionalDetailsGroup",
            "label": "Additional Details:",            
            "numColumns": 1,
            "fields": [
                       {
                        "fieldType": "Group",
                        "fieldName": "internationalSigmetMovement",
                        "expandHorizontally": True,
                        "label": "Movement: ",
                        "numColumns": 3,
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
                         "numColumns": 3,
                         "fields": [
                                   {
                                    "fieldType": "RadioButtons",
                                    "fieldName": "internationalSigmetFcstObs",
                                    "expandHorizontally": False,
                                    "label": "",
                                    "choices": [
                                                "Forecast",
                                                "Observed"
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
            ]
        }                          
        return additionalDetails     
    
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
    
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    
    import sys
    sys.stderr.writelines( ['Hello World!\n'])
    
    try:
        if triggerIdentifiers == None or "convectiveSigmetEmbeddedSvr" in triggerIdentifiers:
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
    except KeyError:
        return None

    if triggerIdentifiers == None or "internationalSigmetOffice" in triggerIdentifiers:
        if "KKCI" in mutableProperties["internationalSigmetOffice"]["values"]:
            return {
                  "internationalSigmetSequence": {
                            "choices": ["ALPHA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT", "GOLF", "HOTEL", "INIDA", "JULIETT", "KILO", "LIMA", "MIKE"]                                
                  }        
            }
        elif "PANC" in mutableProperties["internationalSigmetOffice"]["values"]:
            return {
                  "internationalSigmetSequence": {
                            "choices": ["INDIA", "JULIETT", "KILO", "LIMA", "MIKE", "NOVEMBER", "OSCAR", "PAPA", "QUEBEC"]                                
                  }        
            }
        else:
            return {
                  "internationalSigmetSequence": {
                            "choices": ["NOVEMBER", "OSCAR", "PAPA", "QUEBEC", "ROMEO", "SIERRA", "TANGO", "UNIFORM", "VICTOR", "WHISKEY", "XRAY", "YANKEE", "ZULU"]                                
                  }        
            }                              
    
    if triggerIdentifiers == None or "internationalSigmetExtent" in triggerIdentifiers:
        if "Below" in mutableProperties["internationalSigmetExtent"]["values"] or "Top" in mutableProperties["internationalSigmetExtent"]["values"]:
            return {
                  "internationalSigmetExtentBottom": {
                          "enable": False,                                   
                  }
            }
        else:
            return {
                  "internationalSigmetExtentBottom": {
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