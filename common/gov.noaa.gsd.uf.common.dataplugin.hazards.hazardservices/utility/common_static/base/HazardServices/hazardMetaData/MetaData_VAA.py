'''
    Description: Hazard Information Dialog Metadata for Volcanic Ash Advisory
'''
import CommonMetaData
import VolcanoMetaData
import AviationUtils
from HazardConstants import *
from VisualFeatures import VisualFeatures
import time, datetime

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        
        hazardEvent.setVisualFeatures(VisualFeatures([]))
        
        volcanoDict = VolcanoMetaData.VolcanoMetaData().getVolcanoDict()
        volcanoNamesList = list(volcanoDict.keys())
        volcanoNamesList.sort()
        
        volcanoIndicators = VolcanoMetaData.VolcanoMetaData().getVolcanoIndicators()

        self.nextAdvisoryTime = str(time.strftime('%Y%m%d/%H%M', time.gmtime(time.mktime(hazardEvent.getEndTime().timetuple()))))
        
        metaData = [
            self.getAction(),
            self.getVolcanoName(volcanoNamesList),
            self.getVAANumber(),
            self.getVolcanoHeader(),
            self.getVolcanoStatus(),
            self.getEruptionDetails(),           
            self.getVolcanoLayers(),
            self.getInfoSource(volcanoIndicators),
            self.getVolcanoProductType(),
            self.getConfidence(), 
            self.getCoordinatingVAAC(),            
            self.getRemarks(),
            self.getNextAdvisory(),
            self.getForecasterInitials(),
        ]
        return {
                METADATA_KEY: metaData
                }     
    def getAction(self):
        return {
                "fieldType": "ComboBox",
                "fieldName": "volcanoAction",
                "label": "Product Type",
                "choices": ["VAA", "Near VAA", "Initial Eruption", "Last Advisory", "Resuspended Ash"],
                "values": "VAA",
                }
    def getVolcanoName(self, volcanoNamesList):
        return {
                "fieldType": "ComboBox",
                "fieldName": "volcanoName",
                "autocomplete": True,
                "label": "Select Volcano Name:",
                "choices": volcanoNamesList,
                "values": volcanoNamesList[0],                                               
                }
    def getVAANumber(self):
        return {
                "fieldType": "Group",
                "fieldName": "volcanoNumberGroup",
                "label": "",
                "numColumns": 1,
                "fields": [
                           {
                            "fieldType": "CheckBox",
                            "fieldName": "volcanoChangeAdvisoryNumber",
                            "label": "Manually Override Advisory Number?",
                            "values": False
                            },
                           {
                            "fieldType": "IntegerSpinner",
                            "fieldName": "volcanoNewAdvisoryNumber",
                            "label": "New Advisory Number:",
                            "minValue": 1,
                            "maxValue": 999,
                            "values": 1,
                            "enable": False,
                            }
                           ]
                }        
    def getVolcanoHeader(self):
        return {
                "fieldType": "Group",
                "fieldName": "volcanoHeaderGroup",
                "label": "",
                "numColumns": 1,
                "fields": [
                           {
                            "fieldType": "CheckBox",
                            "fieldName": "volcanoNewHeader",
                            "label": "Create New Header Number?",
                            "values": False
                            },
                           {
                            "fieldType": "ComboBox",
                            "fieldName": "volcanoNewHeaderNumber",
                            "label": "New Header Number:",
                            "choices": ["FVAK22", "FVAK23", "FVAK24", "FVAK25", "FVAK26"],
                            "values": "FVAK22",
                            "enable": False,
                            }
                           ]
                } 
    def getVolcanoStatus(self):
        return {
                "fieldType": "ComboBox",
                "fieldName": "volcanoStatus",
                "label": "Volcano Status",
                "choices": ["GREEN", "YELLOW", "ORANGE", "RED"],
                "values": "RED",
                }
    def getEruptionDetails(self):
        return {
                "fieldType": "Text",
                "fieldName": "volcanoEruptionDetails",
                "label": "Eruption Details:",
                "visibleChars": 40,
                "lines": 1,
                "expandHorizontally": True,
                #"values": "VA ASSOCIATED WITH THE DD/HHmmZ ERUPTION."                            
                }        
    def getVolcanoLayers(self):
        return {
                "fieldType": "Group",
                "fieldName": "volcanoLayersGroup",
                "label": "",
                "numColumns": 1,
                "fields": [                                               
                         {
                         "fieldType": "IntegerSpinner",
                         "fieldName": "volcanoLayersSpinner",
                         "label": "Number of Layers: ",
                         "minValue": 0,
                         "maxValue": 3,
                         "increment": 1,
                         "expandHorizontally": False,
                         },
                         {
                         "fieldType": "Button",
                         "fieldName": "volcanoCreateVAALayers",
                         "label": "Create Forecast Layers",
                         "modifyRecommender": "VAALayerTool"
                         },                                                                            
                         {
                          "fieldType": "Group",
                          "fieldName": "volcanoLayer1",
                          "expandHorizontally": True,
                          "numColumns": 2,
                          "enable": False,
                          "label": "Layer 1",
                          "fields": [
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerBottom1",
                                      "expandHorizontally": False,
                                      "label": "Bottom: ",
                                      "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                  "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                  "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                  "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                  "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                  "FL500"],
                                      "values": "SFC",
                                      },
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerTop1",
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
                                      "fieldName": "volcanoLayerSpeed1",
                                      "label": "Speed (kts):",
                                      "minValue": 0,
                                      "maxValue": 100,
                                      "values": 0,
                                      "incrementDelta": 5,
                                      "expandHorizontally": False,
                                      },
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerDirection1",
                                      "expandHorizontally": False,
                                      "label": "Towards:",
                                      "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                  "WNW","NW","NNW"
                                                  ]
                                      },
                                     {
                                      "fieldType": "IntegerSpinner",
                                      "fieldName": "volcanoLayerForecast1",
                                      "label": "Forecast Time Periods",
                                      "minValue": 0,
                                      "maxValue": 4,
                                      "values": 0,
                                      },                                                                                            
                            ],
                         },
                         {
                          "fieldType": "Group",
                          "fieldName": "volcanoLayer2",
                          "expandHorizontally": True,
                          "enable": False,
                          "numColumns": 2,
                          "label": "Layer 2",
                          "fields": [
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerBottom2",
                                      "expandHorizontally": False,
                                      "label": "Bottom: ",
                                      "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                  "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                  "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                  "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                  "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                  "FL500"],
                                      "values": "SFC",
                                      },
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerTop2",
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
                                      "fieldName": "volcanoLayerSpeed2",
                                      "label": "Speed (kts):",
                                      "minValue": 0,
                                      "maxValue": 100,
                                      "values": 0,
                                      "incrementDelta": 5,
                                      "expandHorizontally": False,
                                      },
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerDirection2",
                                      "expandHorizontally": False,
                                      "label": "Towards:",
                                      "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                  "WNW","NW","NNW"
                                                  ]
                                      },
                                     {
                                      "fieldType": "IntegerSpinner",
                                      "fieldName": "volcanoLayerForecast2",
                                      "label": "Forecast Time Periods",
                                      "minValue": 0,
                                      "maxValue": 4,
                                      "values": 0,
                                      },                                                                                                                                                                                                 
                                     ],
                          },
                         {
                          "fieldType": "Group",
                          "fieldName": "volcanoLayer3",
                          "enable": False,
                          "expandHorizontally": True,
                          "numColumns": 2,
                          "label": "Layer 3",
                          "fields": [
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerBottom3",
                                      "expandHorizontally": False,
                                      "label": "Bottom: ",
                                      "choices": ["SFC", "FL010", "FL020", "FL030", "FL040", "FL050", "FL060", "FL070", "FL080", "FL090",
                                                  "FL100", "FL110", "FL120", "FL130", "FL140", "FL150", "FL160", "FL170", "FL180", "FL190",
                                                  "FL200", "FL210", "FL220", "FL230", "FL240", "FL250", "FL260", "FL270", "FL280", "FL290",
                                                  "FL300", "FL310", "FL320", "FL330", "FL340", "FL350", "FL360", "FL370", "FL380", "FL390",
                                                  "FL400", "FL410", "FL420", "FL430", "FL440", "FL450", "FL460", "FL470", "FL480", "FL490",
                                                  "FL500"],
                                      "values": "SFC",
                                      },
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerTop3",
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
                                      "fieldName": "volcanoLayerSpeed3",
                                      "label": "Speed (kts):",
                                      "minValue": 0,
                                      "maxValue": 100,
                                      "values": 0,
                                      "incrementDelta": 5,
                                      "expandHorizontally": False,
                                      },
                                     {
                                      "fieldType": "ComboBox",
                                      "fieldName": "volcanoLayerDirection3",
                                      "expandHorizontally": False,
                                      "label": "Towards:",
                                      "choices": ["N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W",
                                                  "WNW","NW","NNW"
                                                  ]
                                      },
                                     {
                                      "fieldType": "IntegerSpinner",
                                      "fieldName": "volcanoLayerForecast3",
                                      "label": "Forecast Time Periods",
                                      "minValue": 0,
                                      "maxValue": 4,
                                      "values": 0,
                                      },                                                                                                                                                                                                 
                                     ],
                          },                           
                    ],
                }
    def getInfoSource(self,indicators):
        return {
                "fieldType": "CheckBoxes",
                "fieldName": "volcanoInfoSource",
                "label": "Indicated By:",
                "values": "avo",
                "choices": indicators,
#                 "choices": [
#                             {"identifier": "himawari",
#                              "displayString": "HIMAWARI"},
#                             {"identifier": "goes",
#                              "displayString": "GOES"},
#                             {"identifier": "poes",
#                              "displayString": "POES"},
#                             {"identifier": "lightning",
#                              "displayString": "LIGHTNING"},                            
#                             {"identifier": "avo",
#                              "displayString": "AVO"},
#                             {"identifier": "kvert",
#                              "displayString": "KVERT"},
#                             {"identifier": "pilot",
#                              "displayString": "PILOT REPORT"},
#                             {"identifier": "radar",
#                              "displayString": "RADAR"},
#                             {"identifier": "ship",
#                              "displayString": "SHIP REPORT"},
#                             {"identifier": "webcam",
#                              "displayString": "AVO WEBCAM"},                                                                                                                                                                                                                                                                                                                                                                                                                
#                             ]
                }
    def getConfidence(self):
        return {
                "fieldType": "RadioButtons",
                "fieldName": "volcanoConfidence",
                "label": "Confidence",
                "choices": ["N/A","LOW","HIGH"],
                "values": "N/A",
                }
    def getVolcanoProductType(self):
        return {
                "fieldType": "ComboBox",
                "fieldName": "volcanoProductType",
                "label": "Action",
                "choices": ["Normal", "VAA & Near VAA", "Handover From", "Handover To", "Partial Handover"],
                "values": "Normal",
                }
    def getCoordinatingVAAC(self):
        return {
                "fieldType": "RadioButtons",
                "fieldName": "volcanoCoordinatingVAAC",
                "label": "Coordinating VAAC",
                "choices": ["Tokyo", "Washington", "Montreal"],
                "enable": False,
                }        
    def getRemarks(self):
        return {
                "fieldType": "Text",
                "fieldName": "volcanoRemarks",
                "label": "Remarks:",
                "lines": 2,
                "expandHorizontally": True,
                "values": ""                            
                }
    def getNextAdvisory(self):        
        return {
                "fieldType": "Text",
                "fieldName": "volcanoNextAdvisory",
                "label": "Next Advisory:",
                "visibleChars": 40,
                "lines": 1,
                "expandHorizontally": True,
                "values": "WILL BE ISSUED BY " + self.nextAdvisoryTime + "Z"                           
                }
    def getForecasterInitials(self):
        return {
                "fieldType": "Text",
                "fieldName": "volcanoForecasterInitials",
                "label": "Forecaster Initials",
                "visibleChars": 3,
                "values": "###"
                }                                

        
## # Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, megawidgetDict):    
    import sys
    CommonMetaData.writelines(sys.stderr, ['VAA Interdependency Script Triggered\n'])
    
    nextAdvisoryTime = megawidgetDict["volcanoNextAdvisory"]["values"]      
                
    ###CONTROLLING REMARKS SECTION FOR PRODUCT TYPE/COORDINATING VAAC###
    if triggerIdentifiers:
        returnDict = {"volcanoInfoSource": {"enable": True},
                      "volcanoConfidence": {"enable": True},
                      "volcanoProductType": {"enable": True},
                      "volcanoCoordinatingVAAC": {"enable": True},
                      "volcanoNextAdvisory": {"enable": True},
                      "volcanoNextAdvisory": {"values": "WILL BE ISSUED BY YYYYMMDD/HHHHZ"}, # + self.nextAdvisoryTime + "Z"}, #{"values": nextAdvisoryTime},
                      "volcanoRemarks": {"values": ""},
                      "volcanoLayersSpinner": {"enable": True},
                      "volcanoCreateVAALayers": {"enable": True},                
                      }
                
        ###ALL OTHER INTERDEPENDENCIES###                 
        if ("volcanoProductType" in triggerIdentifiers) or ("volcanoCoordinatingVAAC" in triggerIdentifiers) or \
           ("volcanoAction" in triggerIdentifiers) or ("volcanoProductType" in triggerIdentifiers) or \
           ("volcanoLayersSpinner" in triggerIdentifiers) or ("volcanoLayerForecast1" in triggerIdentifiers) or \
           ("volcanoLayerForecast2" in triggerIdentifiers) or ("volcanoLayerForecast3" in triggerIdentifiers) or \
           ("volcanoCreateVAALayers" in triggerIdentifiers) or ("volcanoRemarks" in triggerIdentifiers):
            ###RESUSPENDED ASH###
            if megawidgetDict["volcanoAction"]["values"] == 'Resuspended Ash':
                returnDict["volcanoEruptionDetails"] = {"values": "RESUSPENDED VA"}
                returnDict["volcanoRemarks"] = {"values": "STRONG WINDS RESUSPENDING VA."}
            ###LAST ADVISORY###
            elif megawidgetDict["volcanoAction"]["values"] == 'Last Advisory':
                returnDict["volcanoEruptionDetails"] = {"values": "VA ASSOCIATED WITH THE DD/HHmmZ ERUPTION."}
                returnDict["volcanoConfidence"] = {"enable": False}
                returnDict["volcanoProductType"] = {"enable": False}
                returnDict["volcanoCoordinatingVAAC"] = {"enable": False}
                returnDict["volcanoNextAdvisory"] = {"values": "NO FURTHER ADVISORIES"}                
                returnDict["volcanoRemarks"] = {"values": "VA SIGNAL HAS FADED FROM ALL SATELLITE IMAGERIES. VA HAS DISSIPATED."}
                returnDict["volcanoLayersSpinner"] = {"enable": False}
                returnDict["volcanoCreateVAALayers"] = {"enable": False}
            ###INITIAL ERUPTION###
            elif megawidgetDict["volcanoAction"]["values"] == 'Initial Eruption':                
                returnDict["volcanoProductType"] = {"enable": False}
                returnDict["volcanoConfidence"] = {"enable": False}
                returnDict["volcanoCoordinatingVAAC"] = {"enable": False}
                returnDict["volcanoNextAdvisory"] = {"values": "NO LATER THAN YYYYmmdd/HHMMZ"} #+updateTime}                
                returnDict["volcanoEruptionDetails"] = {"values": "VA ASSOCIATED WITH THE DD/HHmmZ ERUPTION."}
                returnDict["volcanoRemarks"] = {"enable": False}
                returnDict["volcanoRemarks"] = {"values": "A MORE DETAILED ADVISORY WILL FOLLOW AS SOON AS POSSIBLE."}
                returnDict["volcanoLayersSpinner"] = {"enable": False}
                returnDict["volcanoLayersSpinner"] = {"values": 1}
                returnDict["volcanoLayer1"] = {"enable": True}
                returnDict["volcanoLayer2"] = {"enable": False}
                returnDict["volcanoLayer3"] = {"enable": False}                             
            ###NEAR VAA###
            elif megawidgetDict["volcanoAction"]["values"] == 'Near VAA':
                returnDict["volcanoInfoSource"] =  {"enable": False}
                returnDict["volcanoConfidence"] = {"enable": False}
                returnDict["volcanoProductType"] = {"enable": False}
                returnDict["volcanoEruptionDetails"] = {"values": "VA ASSOCIATED WITH THE DD/HHmmZ ERUPTION."}
                returnDict["volcanoNextAdvisory"] = {"values": ""}
                if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                    returnDict["volcanoRemarks"] = {"values": "PLEASE SEE FVFE01 RJTD ISSUED BY TOKYO VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."} 
                elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                    returnDict["volcanoRemarks"] = {"values": "PLEASE SEE FVXX2 [012345] KNES ISSUED BY WASHINGTON VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}              
                else:
                    returnDict["volcanoRemarks"] = {"values": "PLEASE SEE FVCN01 CWAO ISSUED BY MONTREAL VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}
                returnDict["volcanoLayersSpinner"] = {"enable": False}
                returnDict["volcanoCreateVAALayers"] = {"enable": False}                                   
            ###VAA###
            else:
                returnDict["volcanoNextAdvisory"] = {"values": "WILL BE ISSUED BY YYYYMMDD/HHHHZ"}
                returnDict["volcanoLayersSpinner"] = {"enable": True}
                returnDict["volcanoCreateVAALayers"] = {"enable": True}                
                returnDict["volcanoEruptionDetails"] = {"values": "VA ASSOCIATED WITH THE DD/HHmmZ ERUPTION."}                                
                #Normal
                if megawidgetDict["volcanoProductType"]["values"] == 'Normal':
                    returnDict["volcanoCoordinatingVAAC"] = {"enable": False}
                #VAA and NEAR VAA
                elif megawidgetDict["volcanoProductType"]["values"] == 'VAA & Near VAA':
                    returnDict["volcanoCoordinatingVAAC"] = {"enable": True}
                    if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                        returnDict["volcanoRemarks"] = {"values": "ANOTHER ERUPTION HAS OCCURRED AT {DD/HHMM} UTC. PLEASE SEE FVFE01 RJTD ISSUED BY TOKYO VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}
                    elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                        returnDict["volcanoRemarks"] = {"values": "ANOTHER ERUPTION HAS OCCURRED AT {DD/HHMM} UTC. PLEASE SEE FVXX2 [012345] KNES ISSUED BY WASHINGTON VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}
                    else:
                        returnDict["volcanoRemarks"] = {"values": "ANOTHER ERUPTION HAS OCCURRED AT {DD/HHMM} UTC. PLEASE SEE FVCN01 CWAO ISSUED BY MONTREAL VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}
                #HANDOVER FROM
                elif megawidgetDict["volcanoProductType"]["values"] == 'Handover From':
                    returnDict["volcanoCoordinatingVAAC"] = {"enable": True}
                    if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                        returnDict["volcanoRemarks"] = {"values": "VAAC TOKYO HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVFE01 RJTD."}
                    elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                        returnDict["volcanoRemarks"] = {"values": "VAAC WASHINGTON HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVXX2 [012345] KNES."}
                    else:
                        returnDict["volcanoRemarks"] = {"values": "VAAC MONTREAL HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVCN01 CWAO."}                               
                #HANDOVER TO
                elif megawidgetDict["volcanoProductType"]["values"] == 'Handover To':
                    returnDict["volcanoCoordinatingVAAC"] = {"enable": True}
                    if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                        returnDict["volcanoNextAdvisory"] = {"values": "NO FURTHER ADVISORIES"}
                        returnDict["volcanoRemarks"] = {"values": "THE RESPONSBILITY FOR THIS EVENT IS BEING TRANSFERRED TO VAAC TOKYO. THE NEXT ADVISORY WILL BE ISSUED BY VAAC TOKYO BY {HHMM} UTC UNDER HEADER FVFE01 RJTD."}
                    elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                        returnDict["volcanoNextAdvisory"] = {"values": "NO FURTHER ADVISORIES"}
                        returnDict["volcanoRemarks"] = {"values": "THE RESPONSBILITY FOR THIS EVENT IS BEING TRANSFERRED TO VAAC WASHINGTON. THE NEXT ADVISORY WILL BE ISSUED BY VAAC WASHINGTON BY {HHMM} UTC UNDER HEADER FVXX2 [012345] KNES."}
                    else:
                        returnDict["volcanoNextAdvisory"] = {"values": "NO FURTHER ADVISORIES"}
                        returnDict["volcanoRemarks"] = {"values": "THE RESPONSBILITY FOR THIS EVENT IS BEING TRANSFERRED TO VAAC MONTREAL. THE NEXT ADVISORY WILL BE ISSUED BY VAAC MONTREAL BY {HHMM} UTC UNDER HEADER FVCN01 CWAO."}
                #PARTIAL HANDOVER
                elif megawidgetDict["volcanoProductType"]["values"] == 'Partial Handover':
                    returnDict["volcanoCoordinatingVAAC"] = {"enable": True}
                    if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                        returnDict["volcanoRemarks"] = {"values": "VAAC TOKYO HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVFE01 RJTD."}
                    elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                        returnDict["volcanoRemarks"] = {"values": "VAAC WASHINGTON HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVXX2 [012345] KNES."}
                    else:
                        returnDict["volcanoRemarks"] = {"values": "VAAC MONTREAL HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVCN01 CWAO."}   
        
        ###CONTROLLING LAYER OPTION FOR INTL SIGMET BASED ON VA SELECTION###
        if "volcanoLayersSpinner" in triggerIdentifiers:
            returnDict["volcanoLayer1"] = {"enable": False}
            returnDict["volcanoLayer2"] = {"enable": False}
            returnDict["volcanoLayer3"] = {"enable": False}
            if megawidgetDict["volcanoLayersSpinner"]["values"] == 1:
                returnDict["volcanoLayer1"] = {"enable": True}                     
            elif megawidgetDict["volcanoLayersSpinner"]["values"] == 2:
                returnDict["volcanoLayer1"] = {"enable": True} 
                returnDict["volcanoLayer2"] = {"enable": True} 
            elif megawidgetDict["volcanoLayersSpinner"]["values"] == 3:
                returnDict["volcanoLayer1"] = {"enable": True} 
                returnDict["volcanoLayer2"] = {"enable": True}
                returnDict["volcanoLayer3"] = {"enable": True}
            else:
                returnDict["volcanoLayer1"] = {"enable": False} 
                returnDict["volcanoLayer2"] = {"enable": False}
                returnDict["volcanoLayer3"] = {"enable": False}
                
        if "volcanoNewHeader" in triggerIdentifiers:
            if megawidgetDict["volcanoNewHeader"]["values"] == True:
                returnDict["volcanoNewHeaderNumber"] = {"enable": True}
            else:
                returnDict["volcanoNewHeaderNumber"] = {"enable": False}
                
        if "volcanoChangeAdvisoryNumber" in triggerIdentifiers:
            if megawidgetDict["volcanoChangeAdvisoryNumber"]["values"] == True:
                returnDict["volcanoNewAdvisoryNumber"] = {"enable": True}
            else:
                returnDict["volcanoNewAdvisoryNumber"] = {"enable": False}                        
                               
        return returnDict                             