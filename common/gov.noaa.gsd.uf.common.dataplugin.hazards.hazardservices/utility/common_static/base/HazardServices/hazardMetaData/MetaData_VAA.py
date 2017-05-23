'''
    Description: Hazard Information Dialog Metadata for Volcanic Ash Advisory
'''
import CommonMetaData
import AviationUtils
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)
        
        volcanoDict = AviationUtils.AviationUtils().createVolcanoDict()
        volcanoNamesList = list(volcanoDict.keys())
        volcanoNamesList.sort() 
        
        metaData = [
            self.getAction(),
            self.getVolcanoName(volcanoNamesList),
            self.getVolcanoStatus(),
            self.getEruptionDetails(),           
            self.getVolcanoLayers(),
            self.getVolcanoIntensity(),
            self.getInfoSource(),
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
                "choices": ["VAA", "Near VAA", "Last Advisory"],
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
                "values": "VA CONTINUOUSLY OBS ON SATELLITE IMAGERY"                            
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
                                      "values": "FL200",
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
                                      "values": "FL200",
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
                                      "values": "FL200",
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
    def getVolcanoIntensity(self):
        return {
                "fieldType": "ComboBox",
                "fieldName": "volcanoIntensity",
                "label": "Volcano Intensity",
                "choices": ["No Change", "Intensifying", "Weakening"],
                "values": "No Change",
                }
    def getInfoSource(self):
        return {
                "fieldType": "CheckBoxes",
                "fieldName": "volcanoInfoSource",
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
                            {"identifier": "ship",
                             "displayString": "SHIP REPORT"},
                            {"identifier": "webcam",
                             "displayString": "AVO WEBCAM"},                                                                                                                                                                                                                                                                                                                                                                                                                
                            ]
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
                "visibleChars": 40,
                "lines": 4,
                "expandHorizontally": True,
                "values": "***ADD CUSTOM REMARKS HERE...DELETE IF NOT USED***"                            
                }
    def getNextAdvisory(self):
        return {
                "fieldType": "Text",
                "fieldName": "volcanoNextAdvisory",
                "label": "Next Advisory:",
                "visibleChars": 40,
                "lines": 1,
                "expandHorizontally": True,
                "values": "WILL BE ISSUED BY YYYYMMDD/HHmmZ"                            
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
    sys.stderr.writelines( ['VAA Interdependency Script Triggered\n'])         
                
    ###CONTROLLING REMARKS SECTION FOR PRODUCT TYPE/COORDINATING VAAC###
    if (triggerIdentifiers is None) or ("volcanoProductType" in triggerIdentifiers) or ("volcanoCoordinatingVAAC" in triggerIdentifiers) or ("volcanoAction" in triggerIdentifiers):
        returnDict = {"volcanoLayersGroup": {"enable": True},
                      "volcanoIntensity": {"enable": True},
                      "volcanoEruptionDetails": {"values": "VA CONTINUOUSLY OBS ON SATELLITE IMAGERY."},
                      "volcanoInfoSource": {"enable": True},
                      "volcanoConfidence": {"enable": True},
                      "volcanoProductType": {"enable": True},
                      "volcanoCoordinatingVAAC": {"enable": True},
                      "volcanoNextAdvisory": {"enable": True},
                      "volcanoNextAdvisory": {"values": "WILL BE ISSUED BY YYYYMMDD/HHmmZ"},
                      "volcanoRemarks": {"values": "***ADD CUSTOM REMARKS HERE...DELETE IF NOT USED***"},
                      }
        ###LAST ADVISORY###
        if megawidgetDict["volcanoAction"]["values"] == 'Last Advisory':
            returnDict["volcanoLayersGroup"] = {"enable": False}
            returnDict["volcanoIntensity"] = {"enable": False}
            returnDict["volcanoEruptionDetails"] = {"values": "VA ASSOCIATED WITH THE DD/HHmmZ ERUPTION."}
            returnDict["volcanoConfidence"] = {"enable": False}
            returnDict["volcanoProductType"] = {"enable": False}
            returnDict["volcanoCoordinatingVAAC"] = {"enable": False}
            returnDict["volcanoNextAdvisory"] = {"values": "NO FURTHER ADVISORIES"}
            returnDict["volcanoRemarks"] = {"values": "VA SIGNAL HAS FADED FROM ALL SATELLITE IMAGERIES. VA HAS DISSIPATED."}                   
        ###NEAR VAA###
        elif megawidgetDict["volcanoAction"]["values"] == 'Near VAA':
            returnDict["volcanoLayersGroup"] = {"enable": False}
            returnDict["volcanoInfoSource"] =  {"enable": False}
            returnDict["volcanoConfidence"] = {"enable": False}
            returnDict["volcanoProductType"] = {"enable": False}
            returnDict["volcanoNextAdvisory"] = {"enable": False}
            if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                returnDict["volcanoRemarks"] = {"values": "PLEASE SEE FVFE01 RJTD ISSUED BY TOKYO VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."} 
            elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                returnDict["volcanoRemarks"] = {"values": "PLEASE SEE FVXX2 [012345] KNES ISSUED BY WASHINGTON VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}              
            else:
                returnDict["volcanoRemarks"] = {"values": "PLEASE SEE FVCN01 CWAO ISSUED BY MONTREAL VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}               
        ###VAA###
        elif megawidgetDict["volcanoAction"]["values"] == 'VAA':                    
            #Normal
            if megawidgetDict["volcanoProductType"]["values"] == 'Normal':
                returnDict["volcanoCoordinatingVAAC"] = {"enable": False}
            #VAA and NEAR VAA
            elif megawidgetDict["volcanoProductType"]["values"] == 'VAA & Near VAA':
                if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                    returnDict["volcanoRemarks"] = {"values": "ANOTHER ERUPTION HAS OCCURRED AT {DD/HHMM} UTC. PLEASE SEE FVFE01 RJTD ISSUED BY TOKYO VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}
                elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                    returnDict["volcanoRemarks"] = {"values": "ANOTHER ERUPTION HAS OCCURRED AT {DD/HHMM} UTC. PLEASE SEE FVXX2 [012345] KNES ISSUED BY WASHINGTON VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}
                else:
                    returnDict["volcanoRemarks"] = {"values": "ANOTHER ERUPTION HAS OCCURRED AT {DD/HHMM} UTC. PLEASE SEE FVCN01 CWAO ISSUED BY MONTREAL VAAC WHICH DESCRIBES CONDITIONS NEAR THE ANCHORAGE VAAC AREA OF RESPONSIBILITY."}
            #HANDOVER FROM
            elif megawidgetDict["volcanoProductType"]["values"] == 'Handover From':
                if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                    returnDict["volcanoRemarks"] = {"values": "VAAC TOKYO HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVFE01 RJTD."}
                elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                    returnDict["volcanoRemarks"] = {"values": "VAAC WASHINGTON HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVXX2 [012345] KNES."}
                else:
                    returnDict["volcanoRemarks"] = {"values": "VAAC MONTREAL HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVCN01 CWAO."}                               
            #HANDOVER TO
            elif megawidgetDict["volcanoProductType"]["values"] == 'Handover To':
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
                if megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Tokyo':
                    returnDict["volcanoRemarks"] = {"values": "VAAC TOKYO HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVFE01 RJTD."}
                elif megawidgetDict["volcanoCoordinatingVAAC"]["values"] == 'Washington':
                    returnDict["volcanoRemarks"] = {"values": "VAAC WASHINGTON HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVXX2 [012345] KNES."}
                else:
                    returnDict["volcanoRemarks"] = {"values": "VAAC MONTREAL HAS TRANSFERRED RESPONSIBILITY OF THIS EVENT TO VAAC ANCHORAGE. THIS ADVISORY UPDATES MESSAGE FVCN01 CWAO."}
        return returnDict    
                                  
    ###CONTROLLING LAYER OPTION FOR INTL SIGMET BASED ON VA SELECTION###
    if triggerIdentifiers is None or "volcanoLayersSpinner" in triggerIdentifiers:
        returnDict = {"volcanoLayer3": {"enable": False},
                      "volcanoLayer2": {"enable": False},
                      "volcanoLayer1": {"enable": False},}
        if megawidgetDict["volcanoLayersSpinner"]["values"] == 1:
            returnDict["volcanoLayer1"] = {"enable": True}                     
        elif megawidgetDict["volcanoLayersSpinner"]["values"] == 2:
            returnDict["volcanoLayer1"] = {"enable": True} 
            returnDict["volcanoLayer2"] = {"enable": True} 
        elif megawidgetDict["volcanoLayersSpinner"]["values"] == 3:
            returnDict["volcanoLayer1"] = {"enable": True} 
            returnDict["volcanoLayer2"] = {"enable": True}
            returnDict["volcanoLayer3"] = {"enable": True}        
        return returnDict
             
    #DISABLE COORDINATING VAAC UNLESS PRODUCT TYPE IS APPROPRIATE
    if triggerIdentifiers is None or "volcanoProductType" in triggerIdentifiers:
        if ("Handover From" in megawidgetDict["volcanoProductType"]["values"]) or \
           ("Handover To" in megawidgetDict["volcanoProductType"]["values"]) or \
           ("Partial Handover" in megawidgetDict["volcanoProductType"]["values"]):
            return {"volcanoCoordinatingVAAC": {"enable": True},}
        elif ("Normal" in megawidgetDict["volcanoProductType"]["values"]) or \
             ("VAA & Near VAA" in megawidgetDict["volcanoProductType"]["values"]):
            return {"volcanoCoordinatingVAAC": {"enable": False},}                                   