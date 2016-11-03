"""
    Description: Common Meta Data shared among hazard types.
   
    The "displayString" is for labeling the choices in the Hazard Information Dialog.  
    
    Calls to Action are a good example because for the choices the user needs to select, we don't have room to 
    include the full Call To Action such as: 

                  '''Most flood deaths occur in automobiles. Never drive your vehicle into areas where
                  the water covers the roadway. Flood waters are usually deeper than they appear.
                  Just one foot of flowing water is powerful enough to sweep vehicles off the road.
                  When encountering flooded roads make the smart choice...Turn around...Dont drown.'''

    So for that choice in the dialog, we list the "displayString": "Turn around...don't drown".

    The "productString" is then included in the MetaData to correspond to that choice in the dialog.  
    When the user selects that choice, the HazardEvent will have an attribute:

               "cta":  ["turnAroundCTA"]

    Product Generation then uses the "getProductStrings" method (TextProductCommon) to access the full text or 
    corresponding "productString" to include in the product.  This is not "automatic", but is programmed into the 
    Product Generation python code that builds the "Precautionary Preparedness" section. 

    Note that "productStrings" are simply full text corresponding to a short-hand phrase recognizable by the forecaster.  
    For more complex product text such as the "basisBullet", Product Generation takes some information from the MetaData 
    and uses additional information e.g. "startTime" to construct the needed phrase.  This construction is done in 
    the Product Generation python code, and does not come out of the MetaData.    
        
"""

from com.raytheon.uf.common.hazards.hydro import RiverForecastManager
from com.raytheon.uf.common.hazards.hydro import RiverForecastPoint
from com.raytheon.uf.common.hazards.hydro import RiverForecastGroup
from RiverForecastUtils import *

from ProbUtils import ProbUtils
import numpy as np
import datetime, math

import VTECConstants
from LocalizationInterface import LocalizationInterface
from HazardConstants import *
import os, sys, re
from collections import OrderedDict
from MapsDatabaseAccessor import MapsDatabaseAccessor
import pprint
from com.raytheon.uf.common.time import SimulatedTime
from scipy import interpolate


class MetaData(object):

    def initialize(self, hazardEvent, metaDict):
        self.hazardEvent = hazardEvent
        self.metaDict = metaDict
                
        if self.hazardEvent:
            self.hazardStatus = self.hazardEvent.getStatus().lower()
        else:
            self.hazardStatus = "pending"
    
        self.riverForecastUtils = None
        self.riverForecastManager = None
        self.riverForecastPoint = None
        self.probUtils = ProbUtils()
        self.CENTRAL_PROCESSOR = False 

    def editableWhenNew(self):
        return self.hazardStatus == "pending"
    
    def getPointID(self):
        return {
             "fieldName": "pointID",
             "fieldType": "Text",
             "label": "Forecast Point:",
             "maxChars": 5,
             "editable": False,
            }
    
    def getBasisAndImpacts(self, fieldName='basisAndImpactsStatement'):
        # TODO populate with previous text is possible
        label = 'Enter Basis and Impacts '
        return {
         'fieldName': fieldName,
         'fieldType':'Text',
         'label': label,
         'values': '',
         "visibleChars": 60,
         "lines": 6,
         "expandHorizontally": True
        }

    def getBasis(self):
        return {
            "fieldName": "basis",
            "fieldType":"RadioButtons",
            "label":"Basis:",
            "values": "basisEnteredText",
            "choices": self.basisChoices(),
            }        

    def basisChoices(self):
        return [
                self.basisEnteredText()
                ]
        
    
    def getImmediateCause(self):
        values = 'ER'
        choices = self.immediateCauseChoices()
        return {
            "fieldName": "immediateCause",
            "fieldType":"ComboBox",
            "label":"Immediate Cause:",
            "values": values,
            "expandHorizontally": False,
            "choices": choices,
            "editable" : True,
            "refreshMetadata": True,
                }
        
    def immediateCauseChoices(self):
        return [
                self.immediateCauseER(),
                self.immediateCauseSM(),
                self.immediateCauseRS(),
                self.immediateCauseDM(),
                self.immediateCauseDR(),
                self.immediateCauseGO(),
                self.immediateCauseIJ(),
                self.immediateCauseIC(),
                self.immediateCauseFS(),
                self.immediateCauseFT(),
                self.immediateCauseET(),
                self.immediateCauseWT(),
                self.immediateCauseOT(),
                self.immediateCauseMC(),
                self.immediateCauseUU(),

            ]
        
    def immediateCauseER(self):
        return {"identifier":"ER", "displayString":"ER (Excessive Rainfall)"}
    def immediateCauseSM(self):
        return {"identifier":"SM", "displayString":"SM (Snow Melt)"}
    def immediateCauseRS(self):
        return {"identifier":"RS", "displayString":"RS (Rain and Snow Melt)", "productString":"Rain and Snowmelt in"}
    def immediateCauseDM(self):
        return {"identifier":"DM", "displayString":"DM (Dam or Levee Failure)"}
    def immediateCauseDR(self):
        return {"identifier":"DR", "displayString":"DR (Upstream Dam Release)"}
    def immediateCauseGO(self):
        return {"identifier":"GO", "displayString":"GO (Glacier-Dammed Lake Outburst)"}
    def immediateCauseIJ(self):
        return {"identifier":"IJ", "displayString":"IJ (Ice Jam)"}
    def immediateCauseIC(self):
        return {"identifier":"IC", "displayString":"IC (Rain and/or Snow melt and/or Ice Jam)"}
    def immediateCauseFS(self):
        return {"identifier":"FS", "displayString":"FS (Upstream Flooding plus Storm Surge)"}
    def immediateCauseFT(self):
        return {"identifier":"FT", "displayString":"FT (Upstream Flooding plus Tidal Effects)"}
    def immediateCauseET(self):
        return {"identifier":"ET", "displayString":"ET (Elevated Upstream Flow plus Tidal Effects)"}
    def immediateCauseWT(self):
        return {"identifier":"WT", "displayString":"WT (Wind and/or Tidal Effects)"}
    def immediateCauseOT(self):
        return {"identifier":"OT", "displayString":"OT (Other Effects)"}
    def immediateCauseMC(self):
        return {"identifier":"MC", "displayString":"MC (Other Multiple Causes)"}
    def immediateCauseUU(self):
        return {"identifier":"UU", "displayString":"UU (Unknown)"}

    def getFloodSeverity(self):
        return {
             "fieldName": "floodSeverity",
             "fieldType":"ComboBox",
             "label":"Flood Severity:",
             "expandHorizontally": False,
             "values": "U",
             "choices": self.floodSeverityChoices()
            }        
    def floodSeverityChoices(self):
        return [
            self.floodSeverityNone(),
            self.floodSeverityArealOrFlash(),
            self.floodSeverityMinor(),
            self.floodSeverityModerate(),
            self.floodSeverityMajor(),
            self.floodSeverityUnknown()
        ]        
    def floodSeverityNone(self):
            return {"displayString": "N (None)", "identifier": "N", "productString": ""}
    def floodSeverityArealOrFlash(self):
        return {"displayString": "0 (Areal Flood or Flash Flood Products)", "identifier": "0", "productString": ""}
    def floodSeverityMinor(self):
        return {"displayString": "1 (Minor)", "identifier": "1", "productString": "Minor"}
    def floodSeverityModerate(self):
        return {"displayString": "2 (Moderate)", "identifier": "2", "productString": "Moderate"}
    def floodSeverityMajor(self):
        return {"displayString": "3 (Major)", "identifier": "3", "productString": "Major"}
    def floodSeverityUnknown(self):
        return {"displayString": "U (Unknown)", "identifier": "U", "productString": ""}

    def getFloodRecord(self):
        return {
             "fieldName":"floodRecord",
             "fieldType":"ComboBox",
             "label":"Flood Record Status:",
             "expandHorizontally": True,
             "choices":[
                      {"displayString": "NO (Record Flood Not Expected)", "identifier": "NO"},
                      {"displayString": "NR (Near Record or Record Flood Expected)", "identifier": "NR"},
                      {"displayString": "UU (Flood Without a Period of Record to Compare)", "identifier": "UU"},
                      {"displayString": "OO (Flood record status is not applicable)", "identifier": "OO"},
                     ],
            }
    
    def getRiseCrestFall(self):
        return [
                {
                 "fieldName": "riseAbove",
                 "fieldType": "HiddenField"
                 },
                {
                 "fieldName": "crest",
                 "fieldType": "HiddenField"
                 },
                {
                 "fieldName": "fallBelow",
                 "fieldType": "HiddenField"
                 },
                {
                 "fieldName": "riseAboveDescription",
                 "fieldType": "Text",
                 "label": "Rise Above Time:",
                 "visibleChars": 18,
                 "spacing": 5,
                 "editable": False,
                 "interdependencyOnly": True
                 },
                {
                 "fieldName": "crestDescription",
                 "fieldType": "Text",
                 "label": "Crest Time:",
                 "visibleChars": 18,
                 "spacing": 2,
                 "editable": False,
                 "interdependencyOnly": True
                 },
                {
                 "fieldName": "fallBelowDescription",
                 "fieldType": "Text",
                 "label": "Fall Below Time:",
                 "visibleChars": 18,
                 "spacing": 2,
                 "editable": False,
                 "interdependencyOnly": True
                 },
                {
                 "fieldName": "editRiseCrestFallButtonComp",
                 "fieldType": "Composite",
                 "expandHorizontally": False,
                 "fields": [
                            {
                             "fieldType": "Button",
                             "fieldName": "riseCrestFallButton",
                             "label": " Graphical Time Editor... ",
                             "editRiseCrestFall": True
                             }
                            ]
                 }
                ]
                 
    def getInclude(self):
        return {
             "fieldType":"CheckBoxes",
             "fieldName": "include",
             "label": "Include:",
             "choices": self.includeChoices()
             }
                
    def includeEmergency(self):
        return { 
                "identifier": "ffwEmergency",
                "displayString": "**SELECT FOR FLASH FLOOD EMERGENCY**",
                "productString": "...Flash flood emergency for #includeEmergencyLocation#...",
                "detailFields": [
                            {
                             "fieldType": "Composite",
                             "fieldName": "includeEmergencyLocationWrapper",
                             "fullWidthOfDetailPanel": True,
                             "expandHorizontally": True,
                             "fields": [
                                        {
                                         "fieldType": "Text",
                                         "fieldName": "includeEmergencyLocation",
                                         "expandHorizontally": True,
                                         "maxChars": 40,
                                         "visibleChars": 12,
                                         "promptText": "Enter location"
                                         }
                                        ]
                             }
                            ]
                } 
                           
    def eventTypeThunder(self):
        return {
                "identifier":"thunderEvent", "displayString":"Thunderstorm(s)"
                }
    def eventTypeRain(self):
        return {
                "identifier":"rainEvent", "displayString": "Heavy rainfall (no thunder)"
                }
        
    def eventTypeMinorFlooding(self):
        return {
                "identifier":"minorFlooding", "displayString": "Minor flooding occurring"
                }
        
    def eventTypeFlooding(self):
        return {
                "identifier":"flooding", "displayString": "Flooding occurring"
                }
        
    def eventTypeGenericFlooding(self):
        return {
                "identifier":"genericFlooding",
                "displayString": "Generic (provide reasoning)",
                "detailFields": [
                    {
                    "fieldType": "Text",
                    "fieldName": "genericFloodReasoning",
                    "label": "Reasoning:",
                    }
                 ]

                }
        
    def eventTypeFlashFlooding(self):
        return {
                "identifier":"flashFlooding", "displayString": "Flash flooding occurring"
                }
                                           
    def getRainAmt(self):
        return {
               "fieldType":"RadioButtons",
               "label":"Rain so far:",
               "fieldName": "rainAmt",
               "choices": [
                    self.rain_amount_unknown(),
                    self.enterAmount(),
                    ]
                }
        
    def rain_amount_unknown(self):
        return {"identifier":"rainUnknown", "displayString":"Unknown",
                "productString":"", }

    def enterAmount(self):                
        return  {"identifier":"rainKnown", "displayString":"",
                 "detailFields": [
                       {
                        "fieldType": "FractionRange",
                        "fieldName": "rainSoFarLowerBound:rainSoFarUpperBound",
                        "label": "Between",
                        "betweenLabel": "and",
                        "sendEveryChange": False,
                        "minValue": {
                                     "rainSoFarLowerBound": 0,
                                     "rainSoFarUpperBound": 0
                                    },
                        "maxValue": {
                                     "rainSoFarLowerBound": 99,
                                     "rainSoFarUpperBound": 99
                                    },
                        "values": {
                                   "rainSoFarLowerBound": 0,
                                   "rainSoFarUpperBound": 0
                                  },
                        "incrementDelta": 1,
                        "precision": 1
                       },
                       {
                        "fieldType": "Label",
                        "fieldName": "rainAmtSuffix",
                        "label": "inches of rain have fallen"
                       }
                 ]
                }
 
    # Takes the first one listed as the default
    #  This can be overridden by listing a specific default
    def defaultValue(self, choices):
        for choice in choices:
            return choice.get('identifier')       

    def dopplerSource(self):
        return {"identifier":"dopplerSource",
                "displayString": "Doppler Radar indicated",
        }
        
    def dopplerGaugesSource(self):
        return {"identifier":"dopplerGaugesSource",
                "displayString": "Doppler Radar and automated gauges",
        }
        
    def trainedSpottersSource(self):
        return {"identifier":"trainedSpottersSource",
                "displayString": "Trained spotters reported",
        }
    def publicSource(self):
        return {"identifier":"publicSource",
                "displayString": "Public reported",
        }
        
    def localLawEnforcementSource(self):
        return {"identifier":"localLawEnforcementSource",
                "displayString": "Local law enforcement reported",
        }
        
    def emergencyManagementSource(self):
        return {"identifier":"emergencyManagementSource",
                "displayString": "Emergency management reported",
        }
        
    def satelliteSource(self):
        return {"identifier":"satelliteSource",
                "displayString": "Satellite estimates",
        }
        
    def satelliteGaugesSource(self):
        return {"identifier":"satelliteGaugesSource",
                "displayString": "Satellite estimates and gauge reports",
        }
        
    def gaugesSource(self):
        return {"identifier":"gaugesSource",
                "displayString": "Gauge reports",
        }             

        
    def basisEnteredText(self):
        return {"identifier": "basisEnteredText",
                "displayString": "Enter basis statement...",
                "productString": "#basisEnteredText#",
                "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "basisEnteredText",
                             "expandHorizontally": True,
                             "visibleChars": 12,
                             "promptText": "Enter basis text",
                            }]
 
                }

    def getListOfCities(self, defaultOn=True):
        if defaultOn:
            values = ["selectListOfCities"]
        else:
            values = []
        return {
             "fieldType":"CheckBoxes",
             "fieldName": "listOfCities",
             "showAllNoneButtons" : False,
             "choices": [self.selectListOfCities()],
             "lines": 1,
             "values": values,
            }
    def selectListOfCities(self):
        return {"identifier":"selectListOfCities",
                "displayString": "Select for a list of cities",
                "productString": "Arbitrary arguments used by cities list generator."}

    def getLocationsAffected(self, defaultOn=True):
        if defaultOn:
            values = ["selectLocationsAffected"]
        else:
            values = []
        return {
             "fieldType":"CheckBoxes",
             "fieldName": "locationsAffectedCheckBox",
             "showAllNoneButtons" : False,
             "choices": [self.selectLocationsAffected()],
             "lines": 1,
             "values": values,
            }

    def selectLocationsAffected(self):
        return {"identifier":"selectLocationsAffected",
                "displayString": "Select for locations affected",
                "productString": "Arbitrary arguments used by locations affected generator."}
 
    def getAdditionalInfo(self):
            return {
                     "fieldType":"CheckBoxes",
                     "fieldName": "additionalInfo",
                     "showAllNoneButtons" : False,
                     "label": "Additional Info:",
                     "choices": self.additionalInfoChoices(),
                     "lines": 3
                    }                    
    def listOfDrainages(self):
        return {"identifier":"listOfDrainages",
                "displayString": "Automated list of drainages",
                "productString": "This includes the following streams and drainages..." }
    def additionalRain(self):
        return  {"identifier":"addtlRain",
                 "displayString": "Additional rainfall",
                 "productString": 
                    '''Additional rainfall amounts of #additionalRainLowerBound# to #additionalRainUpperBound# inches are possible in the
                    warned area.''',
                 "detailFields": [
                            {
                             "fieldType": "FractionRange",
                             "fieldName": "additionalRainLowerBound:additionalRainUpperBound",
                             "label": "of",
                             "betweenLabel": "to",
                             "sendEveryChange": False,
                             "minValue": {
                                          "additionalRainLowerBound": 0,
                                          "additionalRainUpperBound": 0
                                         },
                             "maxValue": {
                                          "additionalRainLowerBound": 99,
                                          "additionalRainUpperBound": 99
                                         },
                             "values": {
                                        "additionalRainLowerBound": 0,
                                        "additionalRainUpperBound": 0
                                       },
                             "incrementDelta": 1,
                             "precision": 1
                            },
                            {
                             "fieldType": "Label",
                             "fieldName": "additionalRainSuffix",
                             "label": "inches is expected"
                            }
                       ]
                      }
    def floodMoving(self):
        return {"identifier":"floodMoving",
                "displayString": "Flood waters are moving down",
                "productString":
                '''Flood waters are moving down the #riverName# from #upstreamLocation# to 
                #floodLocation#. The flood crest is expected to reach #downstreamLocation# by #additionalInfoFloodMovingTime#.''',
                "detailFields": [
                            {
                             "fieldName":"additionalInfoFloodMovingTime",
                             "fieldType":"Time",
                             "label": "by:"
                            }
                      ]
                     }
        
    def recedingWater(self):  # EXP / CAN
        return {"identifier":"recedingWater",
                "displayString": "Water is receding",
                "productString": 
                '''Flood waters have receded...and are no longer expected to pose a threat
                to life or property. Please continue to heed any road closures.''', }
    def rainEnded(self):  # EXP / CAN
        return {"identifier":"rainEnded",
                "displayString": "Heavy rain ended",
                "productString": 
                '''The heavy rain has ended...and flooding is no longer expected to pose a threat.''', }
 
    def getRiver(self):
        riverName = self.hazardEvent.get('riverName', '')
        return {
             "fieldType": "Text",
             "fieldName": "riverName",
             "expandHorizontally": True,
             "label" : "River name:",
             "maxChars": 40,
             "visibleChars": 12,
             "editable": True,
             "values": riverName,
            } 

    def getFloodLocation(self):
        return {
             "fieldType": "Text",
             "fieldName": "floodLocation",
             "expandHorizontally": True,
             "label" : "Flood location",
             "maxChars": 40,
             "visibleChars": 12,
            } 


    def getUpstreamLocation(self):
        return {
             "fieldType": "Text",
             "fieldName": "upstreamLocation",
             "expandHorizontally": True,
             "label" : "Upstream location:",
             "maxChars": 40,
             "visibleChars": 12,
            } 
 
    def getDownstreamLocation(self):
        return {
             "fieldType": "Text",
             "fieldName": "downstreamLocation",
             "expandHorizontally": True,
             "label" : "Downstream location:",
             "maxChars": 40,
             "visibleChars": 12,
            } 
        
    def getVolcano(self):
        return {
             "fieldType": "Text",
             "fieldName": "volcanoName",
             "expandHorizontally": True,
             "label" : "Volcano name:",
             "maxChars": 40,
             "visibleChars": 12,
            }         
    def getCTAs(self, values=None):
 
        pageFields = { 
                         "fieldType":"CheckBoxes",
                         "fieldName": "cta",
                         "choices": self.getCTA_Choices()
                     }
         
        if values is not None:
            pageFields['values'] = values
             
             
        return {
                 
               "fieldType": "ExpandBar",
               "fieldName": "CTABar",
               "expandHorizontally": True,
               "pages": [
                            {
                             "pageName": "Calls to Action (1 or more)",
                             "pageFields": [pageFields]
                            }
                         ],
                "expandedPages": ["Calls to Action (1 or more)"]      
                }
                       
                       
    def ctaFloodWatchMeans(self):
        return {"displayString": "A Flood Watch means...", "identifier": "FloodWatch",
                "productString":  '''A Flood Watch means there is a potential for flooding
                based on current forecasts.\n\nYou should monitor later forecasts and be
                alert for possible flood warnings.  Those living in areas prone to flooding
                should be prepared to take action should flooding develop.'''}
    def ctaFloodAdvisoryMeans(self):
        return {"identifier": "floodAdvisoryMeansCTA",
               "displayString": "A Flood Advisory means...",
               "productString": 
              '''A flood advisory means river or stream flows are elevated...or ponding 
                 of water in urban or other areas is occurring or is imminent. Do not
                 attempt to travel across flooded roads. Find alternate routes. It takes
                 only a few inches of swiftly flowing water to carry vehicles away.'''}
    def ctaPointFloodAdvisoryMeans(self):
        return {"identifier": "pointFloodAdvisoryMeansCTA",
                "displayString": "A flood advisory means...",
                "productString": ''''A flood advisory means minor flooding is possible and rivers are forecast to exceed bankfull. 
                 If you are in the advisory area remain alert to possible flooding... or the possibility of the advisory being upgraded to a warning.''',
                }
    def ctaFloodWarningMeans(self):
        return {"identifier": "floodWarningMeansCTA",
               "displayString": "A Flood Warning means...",
               "productString": 
              '''A flood warning means that flooding is imminent or has been reported. Stream rises
                 will be slow and flash flooding is not expected. However...all interested parties
                 should take necessary precautions immediately. '''}
    def ctaFlashFloodWatchMeans(self):
        return {"identifier": "flashFloodWatchMeansCTA",
                "displayString": "A Flash Flood Watch means...",
                "productString": '''A Flash Flood Watch means that conditions may develop
                      that lead to flash flooding.  Flash flooding is a very dangerous situation.
                      \n\nYou should monitor later forecasts and be prepared to take action should
                      flash flood warnings be issued.'''}
    def ctaActQuickly(self):
        return {"identifier": "actQuicklyCTA", "displayString": "Act Quickly...",
                "productString": 
                "Move to higher ground now. Act quickly to protect your life."}
    def ctaChildSafety(self):
        return {"identifier": "childSafetyCTA", "displayString": "Child Safety...",
                "productString": 
                '''Keep children away from storm drains...culverts...creeks and streams.
                Water levels can rise rapidly and sweep children away.'''}
    def ctaNightTime(self):
        return  {"identifier": "nighttimeCTA",
                "displayString": "Nighttime flooding...",
                "productString":
               '''Be especially cautious at night when it is harder to recognize the
                  dangers of flooding. If flooding is observed act quickly. move up to higher
                  ground to escape flood waters. Do not stay in areas subject to flooding
                  when water begins rising.'''}
    def ctaSafety(self):
        return {"identifier": "safetyCTA", "displayString": "Safety...by foot or motorist",
                "productString":
                "Do not enter or cross flowing water or water of unknown depth."}
    def ctaTurnAround(self):
        return {"identifier": "turnAroundCTA",
                "displayString": "Turn around...don't drown",
                "productString":
               '''Most flood deaths occur in automobiles. Never drive your vehicle into areas where
                  the water covers the roadway. Flood waters are usually deeper than they appear.
                  Just one foot of flowing water is powerful enough to sweep vehicles off the road.
                  When encountering flooded roads make the smart choice...Turn around...Dont drown.'''}
    def ctaStayAway(self):
        return {"identifier": "stayAwayCTA", "displayString": "Stay away or be swept away",
                "productString":
                '''Stay away or be swept away. River banks and culverts can become
                unstable and unsafe.'''}
    def ctaArroyos(self):
        return {"identifier": "arroyosCTA", "displayString": "Arroyos...",
                "productString": 
                '''Remain alert for flooding even in locations not receiving rain.
                arroyos...Streams and rivers can become raging killer currents
                in a matter of minutes...even from distant rainfall.'''}
    def ctaBurnAreas(self):
        return {"identifier": "burnAreasCTA",
                "displayString": "Burn Areas...",
                "productString": 
                '''Move away from recently burned areas. Life threatening flooding
                of creeks...roads and normally dry arroyos is likely. The heavy
                rains will likely trigger rockslides...mudslides and debris flows
                in steep terrain...especially in and around these areas.'''}
    def ctaReportFlooding(self):
        return {"identifier": "reportFloodingCTA",
                "displayString": "Report flooding to local law enforcement",
                "productString": 
               '''To report flooding...have the nearest law enforcement agency relay
                  your report to the National Weather Service forecast office.'''}
    def ctaAutoSafety(self):
        return {"identifier": "autoSafetyCTA", "displayString": "Auto Safety",
               "productString":
               '''Flooding is occurring or is imminent. Most flood related deaths
                occur in automobiles. Do not attempt to cross water covered bridges...
                dips...or low water crossings. Never try to cross a flowing stream...
                even a small one...on foot. To escape rising water find another
                route over higher ground.'''}
    def ctaDontDrive(self):
        return {"identifier": "dontDriveCTA",
               "displayString": "Do not drive into water",
               "productString":
              '''Do not drive your vehicle into areas where the water covers the
                 roadway. The water depth may be too great to allow your car to
                 cross safely.  Move to higher ground.'''}
    def ctaDoNotDrive(self):
        return {"identifier": "doNotDriveCTA",
                "displayString": "Do not drive through flooded areas...",
                "productString": "Motorists should not attempt to drive around barricades or drive cars through flooded areas."}
    def ctaRiverBanks(self):
        return {"identifier": "riverBanksCTA",
                "displayString": "Walking near riverbanks...",
                "productString": "Caution is urged when walking near riverbanks."}
    def ctaCamperSafety(self):
        return {"identifier": "camperSafetyCTA",
               "displayString": "Camper safety",
               "productString":
              '''Flooding is occurring or is imminent.  It is important to know where
                 you are relative to streams...rivers...or creeks which can become
                 killers in heavy rains.  Campers and hikers should avoid streams
                 or creeks.'''}
    def ctaLowSpots(self):
        return {"identifier": "lowSpotsCTA",
               "displayString": "Low spots in hilly terrain ",
               "productString": 
              '''In hilly terrain there are hundreds of low water crossings which 
                 are potentially dangerous in heavy rain.  Do not attempt to travel
                 across flooded roads. Find alternate routes.  It takes only a
                 few inches of swiftly flowing water to carry vehicles away.'''}
    def ctaPowerFlood(self):
        return {"identifier": "powerFloodCTA",
               "displayString": "Power of flood waters/vehicles",
               "productString":  
              '''Do not underestimate the power of flood waters. Only a few
                 inches of rapidly flowing water can quickly carry away your
                 vehicle.'''}
    def ctaFlashFloodWarningMeans(self):
        return {"identifier": "ffwMeansCTA", "displayString": "A Flash Flood Warning means...",
              "productString": 
             '''A flash flood warning means that flooding is imminent or occurring.
                If you are in the warning area move to higher ground immediately.
                Residents living along streams and creeks should take immediate
                precautions to protect life and property. Do not attempt to cross
                swiftly flowing waters or waters of unknown depth by foot or by
                automobile.'''}
    def ctaUrbanFlooding(self):
        return  {"identifier": "urbanFloodingCTA",
               "displayString": "Urban Flooding",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause ponding of water in urban areas...
                 highways...streets and underpasses as well as other poor drainage areas and low
                 lying spots. Do not attempt to travel across flooded roads. Find alternate routes.
                 It takes only a few inches of swiftly flowing water to carry vehicles away.'''}
    def ctaRuralFlooding(self):
        return {"identifier": "ruralFloodingCTA",
               "displayString": "Rural flooding/small streams",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of small creeks and
                 streams...As well as farm and country roads. Do not attempt to travel across
                 flooded roads. Find alternate routes.'''}
    def ctaRuralUrbanFlooding(self):
        return {"identifier": "ruralUrbanCTA",
               "displayString": "Flooding of rural and urban areas",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of
                 small creeks and streams...highways and underpasses in urban areas.
                 Additionally...country roads and farmlands along the banks of
                 creeks...streams and other low lying areas are subject to
                 flooding.'''}
    def ctaStayTuned(self):
        return {"identifier": "stayTunedCTA",
                "displayString": "Stay tuned to NOAA Weather Radio for further information...",
                "productString": '''Stay tuned to further developments by listening to your local radio... 
                television... or NOAA Weather Radio for further information.'''}
    def ctaRisingWater(self):
        return { "identifier": "risingWaterCTA",
                "displayString": "To escape rising water...",
                "productString": "To escape rising water... take the shortest path to higher ground."}
    def ctaForceOfWater(self):
        return {"identifier": "forceOfWaterCTA",
                "displayString": "Force of fast-moving flood water...",
                "productString": '''Even 6 inches of fast-moving flood water can knock you off your feet... 
                and a depth of 2 feet will float your car.  Never try to walk... swim... or drive through such swift water.  
                If you come upon flood waters... stop... turn around and go another way.'''}
    def ctaLastStatement(self):
        return {"identifier": "lastStatementCTA",
                "displayString": "Last river flood statement on this event...",
                "productString": "This will be the last river flood statement on this event.  Stay tuned to developments."}
    def ctaWarningInEffect(self):
        return {"identifier":  "warningInEffectCTA",
                "displayString": "This warning will be in effect...",
                "productString": "This warning will be in effect until the river falls below its flood stage."}

    def getEndingSynopsis(self):
        label = 'Enter Ending Synopsis '
        return {
         'fieldName': 'endingSynopsis',
         'fieldType':'Text',
         'label': label,
         'values': '',
         "visibleChars": 60,
         "lines": 6,
         "expandHorizontally": True
        }

    '''
    Typical wording one might want to use:
    
    ********
    Heavy rain ended:
    
    Excess runoff from heavy rain has ended over the warned area. 
    If flooding has been observed...Please report it
    to your local law enforcement agency.
    
    The threat for Flash Flooding has ended over the warned area.
    Rainfall amounts were generally light over the region and Flash Flooding
    is no longer expected. 
    
    The heavy rain has ended...and flooding is no longer expected to pose
    a threat.
    
    *********
    Water is receding:
    
    Flood waters have receded...and are no longer expected to pose a
    threat to life or property.  Please continue to heed any road 
    closures.

    Excess runoff from heavy rain has ended over the warned area. Streams
    and creeks in the warned area have receded or were beginning to
    recede...ending the flood threat. If flooding has been observed...
    please report it to your local law enforcement agency.
    
    Moderate to heavy rain has ended in the area and Flash Flooding is
    no longer a concern. 
    
    '''        
    def getEndingOption(self):
        choices = [
                    {"displayString": "Water is receding", "identifier": "Water is receding",
                     "productString": '''Flood waters have receded...and are no longer expected to pose a threat to life or property.
                     Please continue to heed any road closures.'''},
                    {"displayString": "Heavy rain ended", "identifier": "Heavy rain ended",
                     "productString": '''Excess runoff from heavy rain has ended over the warned area. If flooding has been observed...Please report it
                     to your local law enforcement agency.'''},
                    ]
        return {
             "fieldType":"MenuButton",
             "fieldName": "endingOption",
             "label":"Ending Option:",
             "values": choices[0],
             "choices": choices,
                }
        
    # CAP FIELDS
    def getCAP_Fields(self, tupleList=None):
        capFieldsExpandBar = {
                 
               "fieldType": "ExpandBar",
               "fieldName": "CAPBar",
               "expandHorizontally": True,
               "pages": [
                            {
                             "pageName": "CAP",
                             "pageFields":self.getCAPFieldEntries()
                             }
                         ]
                }
         
        if tupleList is not None:
            capFieldsExpandBar = self.setCAP_Fields(tupleList)
         
        return capFieldsExpandBar
    

    def getCAPFieldEntries(self):
        return [ 
                   { 
                    'fieldName': 'urgency',
                    'fieldType':'ComboBox',
                    'label':'Urgency:',
                    'expandHorizontally': True,
                    'values': 'Immediate',
                    'choices': ['Immediate', 'Expected', 'Future', 'Past', 'Unknown']
                    },
                   {     
                    'fieldName': 'responseType',
                    'fieldType':'ComboBox',
                    'label':'Response Type:',
                    'expandHorizontally': True,
                    'values': 'Avoid',
                    'choices': ['Shelter', 'Evacuate', 'Prepare', 'Execute', 'Avoid', 'Monitor', 'Assess', 'AllClear', 'None']
                    },
                   { 
                    'fieldName': 'severity',
                    'fieldType':'ComboBox',
                    'labeMenuItemsl':'Severity:',
                    'expandHorizontally': True,
                    'values': 'Severe',
                    'choices': ['Extreme', 'Severe', 'Moderate', 'Minor', 'Unknown']
                    },
                   { 
                    'fieldName': 'certainty',
                    'fieldType':'ComboBox',
                    'label':'Certainty:',
                    'expandHorizontally': True,
                    'values': 'Likely',
                    'choices': ['Observed', 'Likely', 'Possible', 'Unlikely', 'Unknown']
                    },
                ] + [self.CAP_WEA_Message()]


    # Used to be in subclasses                
    def setCAP_Fields(self, tupleList):
        # Set the defaults for the CAP Fields
        # ## NOTE - since we are using a ExpandBar, we have to
        # ## mind the structure which is a dict of lists 
        # ## of dicts of lists (seriously)
         
        capExpandBar = self.getCAP_Fields()
        for entry in capExpandBar['pages']:
            for capFields in entry['pageFields']:
                for fieldName, values in tupleList:
                    if capFields["fieldName"] == fieldName:
                        capFields["values"] = values  
        return capExpandBar          

                 
    def CAP_WEA_Message(self):
        return {                
                "fieldType":"CheckBoxes",
                "label":"Activate WEA Text",
                "fieldName": "activateWEA",
                "values": self.CAP_WEA_Values(),
                "choices": [{
                      "identifier":  "WEA_activated",
                      "displayString": "",
                      "productString": "",
                      "detailFields": [{
                         'fieldName': 'WEA_Text',
                         'fieldType':'Text',
                         'label':'WEA Text (%s is end time/day):',
                         'values': self.CAP_WEA_Text(),
                         'length': 90,
                         }]
                   }],
                 }
         
    def CAP_WEA_Values(self):
        return []
         
    def CAP_WEA_Text(self):
        return ''
           
    #######################  WEA Messages
    #
    #     Tsunami Warning (coming late 2013)    Tsunami danger on the coast.  Go to high ground or move inland. Check local media. -NWS 
    #     Tornado Warning                       Tornado Warning in this area til hh:mm tzT. Take shelter now. Check local media. -NWS 
    #     Extreme Wind Warning                  Extreme Wind Warning this area til hh:mm tzT ddd. Take shelter. -NWS
    #     Flash Flood Warning                   Flash Flood Warning this area til hh:mm tzT. Avoid flooded areas. Check local media. -NWS
    #     Hurricane Warning                     Hurricane Warning this area til hh:mm tzT ddd. Check local media and authorities. -NWS
    #     Typhoon Warning                       Typhoon Warning this area til hh:mm tzT ddd. Check local media and authorities. -NWS
    #     Blizzard Warning                      Blizzard Warning this area til hh:mm tzT ddd. Prepare. Avoid travel. Check media. -NWS
    #     Ice Storm Warning                     Ice Storm Warning this area til hh:mm tzT ddd. Prepare. Avoid travel. Check media. -NWS
    #     Dust Storm Warning                    Dust Storm Warning in this area til hh:mm tzT ddd. Avoid travel. Check local media. -NWS
    #         
    #     Legend
    #     hh:mm tzT ddd
    #     tzT = timezone
    #     ddd= three letter abbreviation for day of the week 
    #      

    def as_str(self, obj):
        if isinstance(obj, dict):  
            return {self.as_str(key):self.as_str(value) for key, value in obj.items()}
        elif isinstance(obj, list) or isinstance(obj, set):  
            return [self.as_str(value) for value in obj]    
        elif isinstance(obj, unicode):  
            return obj.encode('utf-8')  
        else:
            return obj
        
    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()


####################################################################
#
# Following methods are used for FL.*
#
#

    def getCrestsOrImpacts(self, parm):
        
        # ## Get Search Fields Section
        searchFields = self.getSearchFieldsSection(parm)
        
        # ## Get Forecast Points Section
        fcstPoints = self.getForecastPointsSection(parm)
        
        # ## Get Selected Points Section
        selectedPoints = self.getSelectedForecastPoints(parm)
        
        # ## Want to have Search Params to expand, but as one group
        # ## This will allow us to add the Selected Points Section 
        # ## at the end to be always visible
        
        expandGroup = {
            "fieldName": parm + "ExpandGroup",
            "fieldType":"Group",
            "label": "",
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": False,
            "expandVertically": False,
            "fields": [searchFields]
        }

        expandBar = {
            "fieldName": parm + "ExpandBar",
            "fieldType":"ExpandBar",
            "label": "",
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": False,
            "expandVertically": False,
            "pages": [{ "pageName": parm.capitalize() + " Search Parameters", "pageFields": [expandGroup]}]
        }
        
        fields = [expandBar, fcstPoints, selectedPoints]
        
        label = "Crest Comparison"
        
        if parm == "impacts":
            label = "Impacts Statement"
            
        
        field = {
            "fieldName": parm + "Group",
            "fieldType":"Group",
            "label": label,
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": False,
            "expandVertically": False,
            "fields": fields
        }

        return field


    def getSearchFieldsSection(self, parm):
        
        # ## Get Ref/Stg Flow Combo
        refStageFlow = self.getRefStgFlow(parm)
        
        # ## Get Stg Window sliders
        # ## Get Max Depth Sliders
        # ## Get FlowWindow Slides
        # ## Get Max Offset slider
        stgWindow = self.getStageWindow(parm)
        
        
        # ## Get Search Type
        searchType = self.getSearchType(parm)
        
        # ## Get Apply Button
        apply = self.getApplyButton(parm)
        
        ### !!!! CREST ONLY !!! Get Year Lookback slider  ###
        lookback = self.getYearLookbackSpinner(parm)
        
        # ## Group to hold all widgets
        searchFields = {
            "fieldName": parm + "Compare",
            "fieldType":"Composite",
            "expandHorizontally": False,
            "expandVertically": False,
            "fields": [refStageFlow, stgWindow, searchType, apply]
        }
        
        
        ### !!!! CREST ONLY !!! Get Year Lookback slider  ###
        if parm == "crests":
            searchFields["fields"].insert(2, lookback)
        
        return searchFields
    
    

    def getForecastPointsSection(self, parm):
        pointID = self.hazardEvent.get("pointID")

        if self.riverForecastUtils is None:
             self.riverForecastUtils = RiverForecastUtils()
        if self.riverForecastManager is None:
             self.riverForecastManager = RiverForecastManager()
        
        
        self.riverForecastPoint = self.getRiverForecastPoint(pointID, True)
        PE = self.riverForecastPoint.getPrimaryPE()
        riverPointID =  elf.riverForecastPoint.getLid()
        riverName =  self.riverForecastPoint.getName()
        
        curObs = self.riverForecastUtils.getObservedLevel(self.riverForecastPoint)
        curObs = self.riverForecastUtils.getObservedLevel(self.riverForecastPoint)
        maxFcst = self.riverForecastUtils.getMaximumForecastLevel(self.riverForecastPoint, PE)

        if PE[0] == PE_H :
            # get flood stage
            fldStg = self.riverForecastPoint.getFloodStage()
            fldFlow = MISSING_VALUE
        else :
            # get flood flow
            fldStg = MISSING_VALUE
            fldFlow =  self.riverForecastPoint.getFloodFlow()
    

        crestOrImpact = "Crest"
        label = "\tCurObs\tMaxFcst\tFldStg\tFldFlow\t|\tPE\tBased on PE"

        groupID = self.riverForecastPoint.getGroupId()
        riverForecastGroup = self.riverForecastManager.getRiverForecastGroup(groupID, False)
        riverGrp = riverForecastGroup.getGroupName() 
        
        point = "\t" + riverPointID + "\t- " + riverName

        values = OrderedDict()
        values['CurObs'] = '{:<15.2f}'.format(curObs)
        values['MaxFcst'] = '{:<15.2f}'.format(maxFcst)
        values['FldStg'] = '{:<15.2f}'.format(fldStg)
        values['FldFlow'] = '{:<15.2f}'.format(fldFlow)
        values['Lookup PE'] = '{:15s}'.format(PE)
        values['Based On Lookup PE'] =  self.basedOnLookupPE
        
        riverLabel = {
                      "fieldType": "Label",
                      "fieldName": parm + "ForecastPointsRiverLabel",
                      "label": riverGrp,
                      "bold": True
                  }
        
        valuesTable = {
                       "fieldType": "Table",
                       "fieldName": parm + "ForecastPointsValuesTable",
                       "lines": 1,
                       "columnHeaders": values.keys(),
                       "values": [ values.values() ]
                  }

        group = {
                 "fieldType": "Group",
                 "fieldName": parm + "ForecastPointsGroup",
                 "expandHorizontally": False,
                 "expandVertically": True,
                 "fields" : [riverLabel, valuesTable]
                 
                 }

        return group
    
    
    def getRefStgFlow(self, parm):
        
        choices = ['Current Obs/Max Fcst']
        pointID = self.hazardEvent.get("pointID")
        
        if self.riverForecastUtils is None:
             self.riverForecastUtils = RiverForecastUtils()
        self.riverForecastPoint = self.getRiverForecastPoint(pointID, True)
        primaryPE =  self.riverForecastPoint.getPrimaryPE()
        maxLevel =  self.riverForecastUtils.getMaximumForecastLevel( self.riverForecastPoint, primaryPE)
        if maxLevel != MISSING_VALUE:
            choices.append('Max Forecast')
            
        obLevel =  self.riverForecastUtils.getObservedLevel( self.riverForecastPoint)
        if obLevel != MISSING_VALUE:
            choices.append('Current Observed')
        
        choices.reverse()
        
        refStageFlow = {
                            "fieldType": "ComboBox",
                            "fieldName": parm + "ReferenceStageFlow",
                            "label": "Reference Stage Flow:",
                            "choices": choices,
                            "values":  choices[0],
                            "expandHorizontally": False 
                        }
        return refStageFlow


    def getYearLookbackSpinner(self, parm):
        lookback = {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "YearLookbackSpinner",
                        "label": "Year Lookback:",
                        "minValue":-150,
                        "maxValue":-1,
                        "values":-50,
                        "spacing": 5,
                        "sendEveryChange": False,
                        "expandHorizontally": True,
                        "showScale": True
                    } 
        return lookback

    def getApplyButton(self, parm):
        apply = {
                    "fieldType": "Button",
                    "fieldName": parm + "ApplyButton",
                    "label": "Apply Parameters",
                    "spacing": 10,
                    "refreshMetadata": True
                }
        return apply
            
        
    # Search Type Dropdown (specific to 'crest' or 'impacts')
    def getSearchType(self, parm):
        choices = ["Recent in Stage/Flow, Year Window",
                   "Closest in Stage/Flow, Year Window",
                   "Recent in Stage/Flow Window",
                   "Closest in Stage/Flow Window", "Highest in Stage/Flow Window"
                   ]
        values = "Closest in Stage/Flow, Year Window"
        if parm == "impacts":
            choices = ["All Below Upper Stage/Flow",
                       "Closest in Stage/Flow Window",
                       "Highest in Stage/Flow Window"  
                       ]
            values = "All Below Upper Stage/Flow"
        return {
               "fieldType": "ComboBox",
               "fieldName": parm + "SearchType",
               "label": "Search Type:",
               "choices": choices,
               "values": values,
               "spacing": 5,
               "expandHorizontally": False 
            }
        
    # Stage Widow search criteria widgets (sliders and text fields)
    def getStageWindow(self, parm, low=-4, hi=4):
        return {
            "fieldName": parm + "stageWindowGroup",
            "fieldType":"Group",
            "spacing": 5,
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,
            "expandHorizontally": False,
            "expandVertically": False,
            "fields": [
                    {
                        "fieldType": "IntegerRange",
                        "fieldName": parm + "StageWindowSpinnerLow:" + parm + "StageWindowSpinnerHi",
                        "label": "",
                        "sendEveryChange": False,
                        "minValue": {
                                     parm + "StageWindowSpinnerLow": low,
                                     parm + "StageWindowSpinnerHi": low + 1
                                    },
                        "maxValue": {
                                     parm + "StageWindowSpinnerLow": hi - 1,
                                     parm + "StageWindowSpinnerHi": hi
                                    },
                        "values": {
                                     parm + "StageWindowSpinnerLow": low + 2,
                                     parm + "StageWindowSpinnerHi": hi - 2
                                    },
                        "minimumInterval": 1,
                        "expandHorizontally": True,
                        "showScale": True
                    },
                    {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "maxDepthBelowFloodStage",
                        "label": "Maximum Depth Below Flood Stage:",
                        "minValue":-10,
                        "maxValue": 0,
                        "values":-3,
                        "sendEveryChange": False,
                        "expandHorizontally": True,
                        "showScale": True
                    },
                    {
                        "fieldName": parm + "flowWindowComposite",
                        "fieldType":"Composite",
                        "expandHorizontally": True,
                        "expandVertically": False,
                        "numColumns": 2,
                        "fields": [
                                   {
                                    "fieldType": "IntegerSpinner",
                                    "fieldName": parm + "FlowWindow1",
                                    "label": "Flow Window (%):   (0-100)",
                                    "minValue": 0,
                                    "maxValue": 100,
                                    "values": 10,
                                    "sendEveryChange": False,
                                    "expandHorizontally": True,
                                    "showScale": False
                                    },
                                   {
                                    "fieldType": "IntegerSpinner",
                                    "fieldName": parm + "FlowWindow2",
                                    "label": "(>=0)",
                                    "minValue": 0,
                                    "maxValue": 100,
                                    "values": 10,
                                    "sendEveryChange": False,
                                    "expandHorizontally": True,
                                    "showScale": False
                                    }
                        ]
                    },
                    {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "MaxOffsetBelowFloodFlow",
                        "label": "Max Offset below Flood Flow (%):",
                        "minValue": 0,
                        "maxValue": 100,
                        "values": 10,
                        "sendEveryChange": False,
                        "expandHorizontally": True,
                        "showScale": False
                    }
            ]
        }
        

    """
    The 'Lookup PE' and 'Based on Lookup PE' fields are very similar. Reuse code 
    Note: we are creating a LABEL: TEXT layout in a single group and returning the GROUP
    This is for looking up via physical element ('PE')
    """
    def getLookupPE(self, parm, basedOn=False):
        base = ""
        label = "Lookup PE:"
        value = "HG"
        
        if basedOn:
            base = "BasedOn"
            label = "Based On " + label
            value = "YES"
            
        pre = parm + base
        return {
                "fieldName": pre + "LookupPEGroup",
                "fieldType":"Group",
                "label": "",
                "leftMargin": 5,
                "rightMargin": 5,
                "topMargin": 10,
                "bottomMargin": 10,
                "expandHorizontally": False,
                "expandVertically": False,
                "fields": [ 
                            {
                                "fieldType": "Label",
                                "fieldName": pre + "LookupPELabel",
                                "label": label,
                            },
                            {
                                "fieldType": "Text",
                                "fieldName": pre + "LookupPEText",
                                "label": "",
                                "values": value,
                                "visibleChars": 4,
                                "lines": 1,
                                "expandHorizontally": False,
                            }
                        ]
        }


    """
    Create a radio button list for user to select the "Settings for Selected Forecast Point"
    """
    def getSelectedForecastPoints(self, parm):
        
        filters =  self.setupSearchParameterFilters(parm)

        if self.hazardEvent.get(parm + "ReferenceStageFlow"):
             self.updateSearchParmsWithHazardEvent(self.hazardEvent, parm, filters)

        filterValues = {k:filters[k]['values'] for k in filters}
        
        pointID = self.hazardEvent.get("pointID")
        if  self.riverForecastManager is None:
             self.riverForecastManager = RiverForecastManager()
            
        self.riverForecastPoint = self.getRiverForecastPoint(pointID, True)
        primaryPE =  self.riverForecastPoint.getPrimaryPE()
        
        impactsTextField = None
        if parm == "impacts":
            headerLabel = "Impacts to Use"
            selectionLabel = "ImpactStg/Flow - Start - End - Tendency"
            
            simTimeMils = SimulatedTime.getSystemTime().getMillis()
            currentTime = datetime.datetime.utcfromtimestamp(simTimeMils / 1000)
            
            impactDataList = self.riverForecastManager.getImpactsDataList(pointID, currentTime.month, currentTime.day)
            plist = JUtilHandler.javaCollectionToPyCollection(impactDataList)
            
            characterizations, descriptions =  self.riverForecastUtils.getImpacts(plist, primaryPE,  self.riverForecastPoint, filterValues)
            charDescDict = dict(zip(characterizations, descriptions))
            impactChoices, values =  self.makeImpactsChoices(charDescDict)
            
            if len(impactChoices) == 0:
                # Reset the attribute
                self.hazardEvent.removeHazardAttribute('impactCheckBoxes')

            # default to having all the values checked
            checkedValues = values

            # If searching for all below/upper, only check the closest impact.
            searchType = filters.get('Search Type')
            searchTypeValue = searchType.get('values')

            if searchTypeValue and searchTypeValue == 'All Below Upper Stage/Flow':
                referenceType = filters['Reference Type']
                referenceTypeValue = referenceType.get('values')
                referenceValue = None
                if referenceTypeValue == 'Max Forecast' :
                    referenceValue =  self.riverForecastUtils.getMaximumForecastLevel( self.riverForecastPoint, primaryPE)
                elif referenceTypeValue == 'Current Observed' :
                    referenceValue =  self.riverForecastUtils.getObservedLevel( self.riverForecastPoint)

                floatValues = []
                floatMap = {}
                for value in values:
                    # Split out the float value
                    strings = value.split('-')
                    strings = strings[0].split('_')
                    floatValue = float(strings[1])
                    floatValues.append(floatValue)
                    floatMap[floatValue] = value

                # Sort the float Values
                floatValues.sort()
                for floatValue in floatValues:
                    # Search for the first value above the current referenceValue
                    if floatValue >= referenceValue:
                        checkedValues = [floatMap.get(floatValue)]
                        break

            selectedForecastPoints = {
                                      "fieldType":"CheckBoxes",
                                      "fieldName": "impactCheckBoxes",
                                      "label": "Impacts",
                                      "choices":  self.sortImpactsChoices(impactChoices, values),
                                      "values" : checkedValues,
                                      "extraData" : { "origList" : checkedValues },
                                      }
        else:
            from HazardConstants import MISSING_VALUE
            headerLabel = "Crest to Use"
            selectionLabel = "CrestStg/Flow - CrestDate"
            defCrest, crestList =  self.riverForecastUtils.getHistoricalCrest( self.riverForecastPoint, primaryPE, filterValues)

            if defCrest.startswith(str(MISSING_VALUE)):
                defCrest = ""
                crestList.append("")
            choices = crestList
            value = defCrest
            selectedForecastPoints = {
                    "fieldType": "ComboBox",
                    "fieldName": parm + "SelectedForecastPointsComboBox",
                    "choices": choices,
                    "values": value,
                    "expandHorizontally": False,
                    "expandVertically": True
            }
        
        groupHeaderLabel = {
                       
                       "fieldType": "Label",
                       "fieldName": parm + "GroupForecastPointsLabel",
                       "leftMargin": 40,
                       "rightMargin": 10,
                       "label": headerLabel,
                       
                       }

        selectionHeaderLabel = {
                       
                       "fieldType": "Label",
                       "fieldName": parm + "SelectedForecastPointsLabel",
                       "label": selectionLabel,
                       
                       }
        
        fields = [ groupHeaderLabel, selectionHeaderLabel, selectedForecastPoints ]
        
        grp = {
            "fieldName": parm + "PointsAndTextFieldGroup",
            "fieldType":"Group",
            "label": "",
            "expandHorizontally": False,
            "expandVertically": True,
            "fields": fields
            }
        
        return grp

    def makeImpactsChoices(self, charDescDict):
        choices = []
        values = []
        
        sortedCharsAsKeys = sorted(charDescDict.keys())
        
        for char in sortedCharsAsKeys:
            id = "impactCheckBox_" + str(char)
            desc = charDescDict.get(char)
            entry = {
                     "identifier": id,
                     "displayString":str(char) ,
                     "detailFields": [ 
                                     {
                                     "fieldType": "Text",
                                     "fieldName": "impactTextField_" + str(char),
                                     "expandHorizontally": False,
                                     "visibleChars": 35,
                                     "lines":2,
                                     "values": desc,
                                     "enable": True
                                     }
                                   ]
                     }
            choices.append(entry)
            values.append(id)
        return choices, values

    def sortImpactsChoices(self, choices, values):
        # Sort the choices so they are in order on the HID
        floatValues = []
        floatMap = {}
        for value in values:
            # Split out the float value
            strings = value.split('-')
            strings = strings[0].split('_')
            floatValue = float(strings[1])
            floatValues.append(floatValue)
            floatMap[floatValue] = value

        # Sort the list of floats
        floatValues.sort()
        sortedImpactChoices = []
        for floatValue in floatValues:
            # Get the corresponding Identifier
            impactIdentifier = floatMap.get(floatValue)
            for impact in choices:
                if impact.get('identifier') == impactIdentifier:
                    sortedImpactChoices.append(impact)
                    break
        return sortedImpactChoices

    def setupSearchParameterFilters(self, parm):
        filters = {}
        filters['Reference Type'] = self.getRefStgFlow(parm)
        filters['Search Type'] = self.getSearchType(parm)
        
        # Build a dictionary mapping parameter identifiers to their
        # values. For each field, subdivide the fieldName into
        # strings delimited by ":" since fields may have multiple
        # identifiers, one per parameter, separated by colons, and
        # for any such subdivided ones, associate the values
        # with only the value that goes with that identifier.
        stageWindowFields = self.getStageWindow(parm)['fields']
        stageWindowFieldsFlattened = []
        self.flattenFieldsList(stageWindowFields, stageWindowFieldsFlattened)
        swfDict = {}
        for k in stageWindowFieldsFlattened:
            for identifier in k['fieldName'].split(":"):
                if identifier != k['fieldName']:
                    singleParameter = k.copy()
                    singleParameter['values'] = k['values'][identifier]
                else:
                    singleParameter = k
                swfDict[identifier] = singleParameter
        
        filters['Stage Window Lower'] = swfDict[parm + "StageWindowSpinnerLow"]
        filters['Stage Window Upper'] = swfDict[parm + "StageWindowSpinnerHi"]
        filters['Depth Below Flood Stage'] = swfDict[parm + "maxDepthBelowFloodStage"]
        filters['Flow Window Lower'] = swfDict[parm + "FlowWindow1"]
        filters['Flow Window Upper'] = swfDict[parm + "FlowWindow2"]
        filters['Flow Stage Window'] = swfDict[parm + "MaxOffsetBelowFloodFlow"]
        
        
        if parm == 'crests':
            filters['Year Lookback'] = self.getYearLookbackSpinner(parm)

        return filters
        
    def flattenFieldsList(self, fieldsList, flattenedFieldsList):
        for k in fieldsList:
            flattenedFieldsList.append(k)
            if "fields" in k:
                 self.flattenFieldsList(k["fields"], flattenedFieldsList)
    
    def updateSearchParmsWithHazardEvent(self, hazardEvent, parm, filters):
        
        filters['Reference Type']['values'] = hazardEvent.get(parm + "ReferenceStageFlow")
        filters['Stage Window Lower']['values'] = hazardEvent.get(parm + "StageWindowSpinnerLow")
        filters['Stage Window Upper']['values'] = hazardEvent.get(parm + "StageWindowSpinnerHi")
        filters['Depth Below Flood Stage']['values'] = hazardEvent.get(parm + "maxDepthBelowFloodStage")
        filters['Flow Window Lower']['values'] = hazardEvent.get(parm + "FlowWindow1")
        filters['Flow Window Upper']['values'] = hazardEvent.get(parm + "FlowWindow2")
        filters['Flow Stage Window']['values'] = hazardEvent.get(parm + "MaxOffsetBelowFloodFlow")
        filters['Search Type']['values'] = hazardEvent.get(parm + "SearchType")
     
        if parm == 'crests':
            filters['Year Lookback']['values'] = hazardEvent.get(parm + "YearLookbackSpinner")
     
        return filters

    def getRiverForecastPoint(self, pointID, isDeepQuery=False):
        doQuery = False
        if  self.riverForecastPoint is None:
            doQuery = True
        else:
            if pointID !=  self.riverForecastPoint.getLid():
                doQuery = True
        if doQuery == True:
            if  self.riverForecastManager is None:
                 self.riverForecastManager = RiverForecastManager()
            self.riverForecastPoint =  self.riverForecastManager.getRiverForecastPoint(pointID, isDeepQuery)
        return(self.riverForecastPoint)

    def setDamNameLabel(self, damOrLeveeName):
        return {
             "fieldType": "Text",
             "fieldName": "damOrLeveeName",
             "expandHorizontally": True,
             "label" : "Dam Name: ",
             "visibleChars": 80,
             "editable": False,
             "enable": True,
             "values": damOrLeveeName,
             "valueIfEmpty": "|* Enter Dam or Levee Name *|",
             "promptText": "Enter dam or levee name",
             "bold": True,
            } 

    def getDamOrLevee(self, damOrLeveeName):
        choices = self.damOrLeveeChoices()
        if not damOrLeveeName and choices:
            damOrLeveeName = choices[0]
        damOrLevee = {
                       "fieldName": "damOrLeveeName",
                        "fieldType":"ComboBox",
                        "autocomplete":  True,
                        "allowNewChoice": True,
                        "label":"Dam or Levee:",
                        "editable": self.editableWhenNew(),
                        "enable": True,
                        "values": damOrLeveeName,
                        "choices": choices,
                        } 
        return damOrLevee

    def damOrLeveeChoices(self):
        damList = []
        mapsAccessor = MapsDatabaseAccessor()
        damOrLeveeNames = mapsAccessor.getPolygonNames(DAMINUNDATION_TABLE)
        damOrLeveeNames.sort()
        for damOrLeveeName in damOrLeveeNames:
            ids = {}
            ids["identifier"] = damOrLeveeName
            ids["displayString"] = damOrLeveeName
            ids["productString"] = damOrLeveeName
            damList.append(ids)
        return damList

    def includeFloodPointTable(self):
        return {"identifier":"selectFloodPointTable",
                "displayString": "Select for flood point table"
        }

########################
# ## Probabilistic

    def probability(self):
        return {
            "fieldType": "IntegerSpinner",
            "fieldName": "probability",
            "label": "Probability",
            "minValue": 1,
            "maxValue": 100,
            "values": 50,
            "sendEveryChange": False,
            "expandHorizontally": True,
            "showScale": True,
            }

    # Excessive Rainfall Outlook
    def getRisk(self):
        values = 'riskSlight'
        choices = self.riskChoices()
        return {
            "fieldName": "risk",
            "fieldType":"ComboBox",
            "label":"Risk:",
            "values": values,
            "expandHorizontally": False,
            "choices": choices,
            "editable" : True,
                }
        
    def riskChoices(self):
        return [
                self.riskSeeText(),
                self.riskSlight(),
                self.riskModerate(),
                self.riskHigh(),
            ]
        
    def riskSeeText(self):
        return {"identifier":"riskSeeText", "displayString":"See Text"}
    def riskSlight(self):
        return {"identifier":"riskSlight", "displayString":"SLGT - Slight Risk"}
    def riskModerate(self):
        return {"identifier":"riskModerate", "displayString":"MDT - Moderate Risk"}
    def riskHigh(self):
        return {"identifier":"riskHigh", "displayString":"HIGH - High Risk"}

    # Convection Outlook
    def getConvectionCategory(self):
        values = 'convectionSlight'
        choices = self.convectionCategoryChoices()
        return {
            "fieldName": "convectionCategory",
            "fieldType":"ComboBox",
            "label":"Convection Category:",
            "values": values,
            "expandHorizontally": False,
            "choices": choices,
            "editable" : True,
                }
        
    def convectionCategoryChoices(self):
        return [
                self.convectionThunderstorm(),
                self.convectionMarginal(),
                self.convectionSlight(),
                self.convectionEnhanced(),
                self.convectionModerate(),
                self.convectionHigh(),
            ]
        
    def convectionThunderstorm(self):
        return {"identifier":"convectionThunderstorm", "displayString":"TSTM - Thunderstorm"}
    def convectionMarginal(self):
        return {"identifier":"convectionMarginal", "displayString":"1: MRGL - Marginal"}
    def convectionSlight(self):
        return {"identifier":"convectionSlight", "displayString":"2: SLGT - Slight"}
    def convectionEnhanced(self):
        return {"identifier":"convectionEnhanced", "displayString":"3: ENH - Enhanced"}
    def convectionModerate(self):
        return {"identifier":"convectionModerate", "displayString":"4: MDT - Moderate"}
    def convectionHigh(self):
        return {"identifier":"convectionHigh", "displayString":"5: HIGH - High"}


    def convectiveGetAttrs(self):
        attrs = self.hazardEvent.getHazardAttributes()
        wdir = attrs.get('convectiveObjectDir')
        wspd = attrs.get('convectiveObjectSpdKts')
        
        vals = []
        
        if wdir:
            vals.append(['wdir',wdir])
        if wspd:
            vals.append(['wspd',wspd])
        if not wdir and not wspd:
            vals.append(['N/A', 'N/A'])
                        
        tbl = {
            "fieldType": "Table",
            "fieldName": "convectiveGetAttrs",
            "label": "Cell Attributes (only automated objects):",
            "lines": 4,
            "columnHeaders": [ "Category", "Value"],
            "values": vals,
        }
        
        return tbl
        
    # Prob_Severe and Prob_Tornado
    def convectiveControls(self): 
        mws = self.initializeObject()       
        enable = self.hazardEvent.get('editable', False)
        self.flush()
        # Object Info
        mws.append( self.getConvectiveObjectInfo(enable))
        # Motion Vector
        mws.append( self.getConvectiveMotionVector(enable))
        # Probability Trend
        mws.append( self.getConvectiveProbabilityTrend(enable))
        # Storm Chars
        mws.append( self.getStormCharacteristics(enable))
        # Warning Discussion
        mws.append( self.getConvectiveDiscussion(enable))
        mws.append( self.getPastConvectiveDiscussion())
                
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveGroup",
            "label": "",
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": False,
            "expandVertically": False,
            "fields": mws
        }        
        return grp


    def initializeObject(self):        
        #  Manually drawn hazards are always userOwned
        if self.hazardEvent.get('objectID') is None:
            # Go with eventID as it should be unique
            self.hazardEvent.set('objectID',  'M' + self.hazardEvent.getDisplayEventID())
            self.hazardEvent.set('automationLevel', 'userOwned')
            editable = True
            newManualObject = True
        else:
            editable = self.hazardEvent.get('editable', False)
            newManualObject = False
        print  "CM ConvectiveControls newManualObject, editable", newManualObject, editable
        self.flush()       
        mwList = [
            {
             "fieldType": "HiddenField",
             "fieldName": "editable",
             "values": editable, 
             "modifyRecommender": "SwathRecommender",
             },
            {
             "fieldType": "HiddenField",
             "fieldName": "newManualObject",
             "values": newManualObject,
             },
        ]        
        return mwList
    
    def getConvectiveObjectInfo(self, enable):        
        grp = {
            "fieldType": "Composite",
            "fieldName": "convectiveObjectInfo",
            "label": "",
            "numColumns":3,
            "fields": [
                        self.getModifyButton(enable),
                        self.getAutomationLevel(),
                        self.getAutoShape(enable),
                       ]
        }        
        return grp
    
    def getAutomationLevel(self):
        automationLevelLabels = {
                'userOwned': 'User Owned',
                'automated': 'Automated',
                'attributesAndGeometry': 'Attributes and Geometry',
                'attributesOnly':'Attributes Only',
                }
        automationLabel = automationLevelLabels[self.hazardEvent.get('automationLevel', 'userOwned')]
        return {
             "fieldName": "automationLevel",
             "fieldType": "Label",
             "label": "AutomationLevel: " + automationLabel,
            }

    def getAutoShape(self, enable):
        automationLevel = self.hazardEvent.get('automationLevel', 'automated')
        if automationLevel in ['automated', 'attributesAndGeometry']:
            autoShape = True
        else:
            autoShape = False
        return {
            "fieldType": "CheckBox",
            "fieldName": "autoShape",
            "label": "Automatic Shape",
            "sendEveryChange": False,
            "values": autoShape,
            "modifyRecommender": 'SwathRecommender',
            "enable": enable,
            }
    
    def getModifyButton(self, enable):               
        grp = {
            "fieldType": "Button",
            "fieldName": "modifyButton",
            "label": "     MODIFY     ",
            "enable": not enable,
            "refresh": True,
        }        
        return grp

    def getConvectiveMotionVector(self, enable):
        wdir = self.hazardEvent.get('convectiveObjectDir', 270)
        wspd = self.hazardEvent.get('convectiveObjectSpdKts', 32) 
        
        recommender = '' if  self.CENTRAL_PROCESSOR else "SwathRecommender" 
        
        
        grp = {
            "fieldType": "Composite",
            "fieldName": "convectiveMotionVectorGroup",
            "label": "",
            "numColumns":2,
            "fields": [
                        {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectDir",
                        "sendEveryChange": False,
                        "label": "Motion Vector: Dir (deg)",
                        "minValue": 1,
                        "maxValue": 360,
                        "values": int(wdir),
                        "incrementDelta": 5,
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "enable": enable,
                        },
                        {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectSpdKts",
                        "label": "Spd (kts)",
                        "sendEveryChange": False,
                        "minValue": 0,
                        "maxValue": 200,
                        "values": int(wspd),
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "enable": enable,
                        },
                       {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectDirUnc",
                        "label": "Dir Uncertainty (deg)",
                        "sendEveryChange": False,
                        "minValue": 0,
                        "maxValue": 45,
                        "values": 12,
                        "incrementDelta": 5,
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "enable": enable,
                        },
                       {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectSpdKtsUnc",
                        "label": "Speed Uncertainty (kts)",
                        "sendEveryChange": False,
                        "minValue": 0,
                        "maxValue": 20,
                        "values": int(4.2),
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "enable": enable,
                        },
                       {
                        "fieldType": "CheckBox",
                        "fieldName": "showGrid",
                        "label": "Preview Grid",
                        "sendEveryChange": False,
                        "showScale": False,
                        "values": False,
                        "modifyRecommender": recommender,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "resetMotionVector",
                        "label": "Reset Motion Vector",
                        "values": False,
                        "modifyRecommender": recommender,
                        "enable": enable,
                        },
#                         {
#                         "fieldType": "Button",
#                         "fieldName": "moveStartTime",
#                         "label": "Move Start Time",
#                         "values": False,
#                         "modifyRecommender": recommender,
#                         "enable": enable,
#                         },
                        self.getConvectiveSwathPresets(enable)
                       ]
        }        
        return grp

    def getConvectiveSwathPresets(self, enable):
        recommender = '' if  self.CENTRAL_PROCESSOR else "SwathRecommender" 
        presets = {
            "fieldType": "ComboBox",
            "fieldName": "convectiveSwathPresets",
            "label": "Swath Presets:",
            "choices": ["NoPreset",
                        "RightTurningSupercell",
                        "LeftTurningSupercell",
                        "BroadSwath",
                        "LightBulbSwath",
                        # "CubicSplineInterpolation"
                        ],
            "values": "NoPreset",
            "expandHorizontally": False,
            "width":2,
            "modifyRecommender": recommender,
            "enable": enable,
        }
        
        return presets

    
    def getConvectiveProbabilityTrend(self, enable):
        recommender = '' if  self.CENTRAL_PROCESSOR else "SwathRecommender" 

        trends = {
            "fieldType": "Composite",
            "fieldName": "convectiveTrendsGroup",
            "label": "",
            "numColumns":8,
            "fields": [
                        {
                        "fieldType": "Label",
                        "fieldName": "convectiveProbTrendLabel",
                        "label": "Trend Interpolation:"
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendDraw",
                        "label": "Draw",
                        "enable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendLinear",
                        "label": "Linear",
                        "enable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendExp1",
                        "label": "Exp1",
                        "enable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendExp2",
                        "label": "Exp2",
                        "enable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendBell",
                        "label": "Bell",
                        "enable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendPlus5",
                        "label": "+5",
                        "enable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendMinus5",
                        "label": "-5",
                        "enable": enable,
                        },

                       ]
        }
        
        probInc = 5
        self.hazardEvent.set('convectiveProbabilityTrendIncrement', probInc)
        graphProbs =  self.probUtils.getGraphProbs(self.hazardEvent)  
        colors = [ self.getProbTrendColor(y) for y in range(0,100, 20)]
        
        probs = {
            "fieldType": "Composite",
            "fieldName": "convectiveProbGroup",
            "label": "",
            "numColumns":3,
            "fields": [
                        {
                         "fieldType": "HiddenField",
                         "fieldName": "convectiveProbabilityTrendIncrement",
                         "values": probInc
                         },
                        {
                         "fieldType": "HiddenField",
                         "fieldName": "convectiveProbabilityTrend",
                         "values": [100, 80, 60, 40, 20, 10]
                         },
                        #=======================================================
                        # {
                        # "fieldType": "Button",
                        # "fieldName": "convectiveProbTrendUndo",
                        # "label": "Undo",
                        # },
                        # {
                        # "fieldType": "HiddenField",
                        # "fieldName": "convectiveProbTrendUndoValues",
                        # "values": probVals
                        # },
                        # {
                        # "fieldType": "Button",
                        # "fieldName": "convectiveProbTrendRedo",
                        # "label": "Redo",
                        # },
                        # {
                        # "fieldType": "HiddenField",
                        # "fieldName": "convectiveProbTrendRedoValues",
                        # "values": probVals
                        # },
                        #=======================================================
                        {
                        "fieldType": "Graph",
                        "fieldName": "convectiveProbTrendGraph",
                        "yMinimum": 0,
                        "yMaximum": 100,
                        "xHatchInterval": 5,
                        "yHatchInterval": 10,
                        "xLabelInterval": 10,
                        "yLabelInterval": 20,
                        "heightMultiplier": 1.2,
                        "yLabelSuffix": "%",
                        "drawnPointsInterval": 5,
                        "modifyRecommender": recommender,
                        "sendEveryChange":False,
                        "yColors": colors,
                        "values": graphProbs,
                        "enable": enable,
                        }
                    ]
            }
        
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveProbabilityGroup",
            "label": "",
            "expandHorizontally": False,
            "expandVertically": False,
            "numColumns":1,
            "fields": [
                       trends,
                       probs,
                       ]
        }

        
        return grp
    
    def getProbTrendColor(self, prob):
        ### Should match PHI Prototype Tool
        colors =  {
            (0,20): { "red": 102/255.0, "green": 224/255.0, "blue": 102/255.0 }, 
            (20,40): { "red": 255/255.0, "green": 255/255.0, "blue": 102/255.0 }, 
            (40,60): { "red": 255/255.0, "green": 179/255.0, "blue": 102/255.0 }, 
            (60,80): { "red": 255/255.0, "green": 102/255.0, "blue": 102/255.0 }, 
            (80,101): { "red": 255/255.0, "green": 102/255.0, "blue": 255/255.0 }
        }
        
        
        for k, v in colors.iteritems():
            if float(k[0]) <= prob and prob < float(k[1]):
                return v

        return { "red": 1, "green": 1, "blue": 1 }

    def getConvectiveDiscussion(self, enable):        
        text = {
            "fieldType": "Text",
            "fieldName": "convectiveWarningDecisionDiscussion",
            "label": "Enter Warning Decision Discussion Here",
            "visibleChars": 40,
            "lines": 5,
            "expandHorizontally": False,
            "promptText": "(Enter discussion here...)",
            "enable": enable,
        }
        return text

    def getPastConvectiveDiscussion(self):
         
        pastDisc = self.hazardEvent.get('convectivePastWarningDecisionDiscussion', '')
         
        text = {
            "fieldType": "Text",
            "fieldName": "convectivePastWarningDecisionDiscussion",
            "label": "Warning Decision Discussion",
            "visibleChars": 40,
            "lines": 5,
            "expandHorizontally": False,
            "editable": False,
            "values": pastDisc,
        }
         
        return text

    def getStormCharacteristics(self, enable):
        hailType =  self.buildStormChars('Wind', [str(x) + " mph" for x in [60, 65, 70, 75, 80, 85, 90]], enable)

        windType =  self.buildStormChars('Hail', [str(x)+"\"" for x in [1, 1.5, 2, 2.5, 3, 3.5, ">=4"]], enable)
        
        tornType =  self.buildStormChars('Torn', ['radar indicated', 'radar observed', 'spotter observed'], enable)
        
        threatTypes = {
            "fieldType": "Composite",
            "fieldName": "convectiveStormCharsGroup",
            "label": "Storm Characteristics (included in discussion)",
            "numColumns":3,
            "fields": [hailType, windType, tornType],
            "enable": enable,
            }

        
        return threatTypes

    def buildStormChars(self, typ, vals, enable):
        capType = typ.capitalize()
        labelDict = {'Wind':'Max Wind', 'Hail': 'Max Hail', 'Torn': 'Tornado'}
        values = ["None"]
        values.extend(vals)
        chars = {
            "fieldType": "ComboBox",
            "fieldName": "convectiveStormChars"+capType,
            "label": labelDict.get(capType, 'THREAT'),
            "choices": values,
            "values": "None",
            "expandHorizontally": False,
            "enable": enable,
        }        
        return chars        

        
def applyConvectiveInterdependencies(triggerIdentifiers, mutableProperties):
    print "\n*****************\nCM applyConvective called"
    print 'triggerIdentifiers', triggerIdentifiers
    newManualObjectKey = mutableProperties.get('newManualObject')
    editableKey = mutableProperties.get('editable')

    print "CM newManualObjectKey, editableKey", newManualObjectKey, editableKey
    if newManualObjectKey:
        newManualObject = newManualObjectKey.get('values')
    else:
        newManualObject = True
        print "WARNING -- CM made it to else - saying firstTime is True"       
#     for key in mutableProperties:
#         print key, mutableProperties[key]
    os.sys.__stdout__.flush()
    returnDict = {}
    if (triggerIdentifiers and 'modifyButton' in triggerIdentifiers) or not triggerIdentifiers:
        if not triggerIdentifiers: # None -- set to whatever "editable" is set to
            if newManualObject:
                enable = True 
                returnDict['newManualObject'] = {'values': False}
                print 'CM Script setting newManualObject to False'
                os.sys.__stdout__.flush()
            else:
                enable = False
                returnDict['editable'] = {'values': False}
        elif 'modifyButton' in triggerIdentifiers:  # Modify Button -- set to editable
            enable = True
            returnDict['editable'] = {'values': True}
        for key in ['autoShape', "convectiveObjectDir", "convectiveObjectSpdKts", "convectiveObjectDirUnc", 
                    "convectiveObjectSpdKtsUnc", "resetMotionVector", "convectiveSwathPresets", "convectiveProbTrendDraw",
                    "convectiveProbTrendLinear", "convectiveProbTrendExp1", "convectiveProbTrendExp2", "convectiveProbTrendBell",
                    "convectiveProbTrendPlus5","convectiveProbTrendMinus5", "convectiveProbTrendGraph", "convectiveWarningDecisionDiscussion",
                    "convectiveStormCharsGroup","convectiveStormCharsWind", "convectiveStormCharsHail", "convectiveStormCharsTorn"]:
            returnDict[key] = {'enable' : enable}
        returnDict['modifyButton'] = {'enable' : not enable}
        return returnDict
   

    def convectiveFilter(myIterable, prefix):
        return [tf for tf in myIterable if tf.startswith(prefix)]

    def updateProbtrend(convectiveProbTrendGraphVals, interp):
        '''
        Logic taken from NSSL Prototype PHI tool http://www.nssl.noaa.gov/projects/facets/phi/prob_chart.js
        and 'pythonized'.  Probably a way to speed things up with numpy when there's time to refactor
        '''
        if interp == 'convectiveProbTrendDraw':
            return []
        if interp not in ['convectiveProbTrendBell', 'convectiveProbTrendLinear', 'convectiveProbTrendExp1',
                          'convectiveProbTrendExp2', 'convectiveProbTrendPlus5', 'convectiveProbTrendMinus5']:
            return convectiveProbTrendGraphVals

        probs = [entry.get('y') for entry in convectiveProbTrendGraphVals]
        mins = [entry.get('x') for entry in convectiveProbTrendGraphVals]
        duration = mins[-1]
        delta = np.average(np.diff(mins))
        probsArr = np.array(probs)
        firstVal = float(probsArr[0])
        lastVal = float(probsArr[-1])
        end = float(duration/delta)
        diff = float((lastVal - firstVal) / end)
        
        newProbs = []
        if(interp == 'convectiveProbTrendBell'):
            maxVal = np.amax(probsArr)
            hitMaxVal = False
            end2 = float(np.argmax(probsArr))

        for i in range(len(probsArr)):
            newVal = np.nan
            if(i <= end):
                if(interp == 'convectiveProbTrendLinear'):
                    newVal = firstVal + (i * diff)

                elif(interp == 'convectiveProbTrendExp1'):
                    disNorm = (i / end)
                    newVal = ((lastVal - firstVal) * (disNorm * disNorm)) + firstVal

                elif(interp == 'convectiveProbTrendExp2'):
                    disNorm = 1 - (i / end)
                    newVal = ((firstVal - lastVal) * (disNorm * disNorm)) + lastVal

                elif(interp == 'convectiveProbTrendPlus5'):
                    newVal = probsArr[i] + 5

                elif(interp == 'convectiveProbTrendMinus5'):
                    newVal = probsArr[i] - 5

                elif(interp == 'convectiveProbTrendBell'):
                    if(probsArr[i] == maxVal):
                        hitMaxVal = True
                        newVal = probsArr[i]

                    elif(hitMaxVal):
                        disNorm = ((i - end2) / (end - end2))
                        newVal = ((lastVal - maxVal) * (disNorm * disNorm)) + maxVal

                    else:
                        disNorm = 1 - (i / end2)
                        newVal = ((firstVal - maxVal) * (disNorm * disNorm)) + maxVal

                if(newVal > 100):
                    newVal = 100

                if(newVal < 0):
                    newVal = 0

                newProbs.append(newVal)
                if(i == end):
                    probDiff = newVal - newProbs[len(newProbs) - 2]


            else:
                newVal = newVal + probDiff
                if(newVal > 100):
                    newVal = 100

                if(newVal < 0):
                    newVal = 0

                newProbs.append(newVal)

        if len(newProbs) == len(convectiveProbTrendGraphVals):
            ### To satisfy end always ending at 0
            newProbs[-1] = 0

            for i in range(len(newProbs)):
               convectiveProbTrendGraphVals[i]['y'] = newProbs[i]
        else:
            sys.stderr.write('New probs length does not match old probs length. No change')

        return convectiveProbTrendGraphVals



    if triggerIdentifiers:
        convectTriggers = convectiveFilter(triggerIdentifiers, 'convective')
        convectMutables = convectiveFilter(mutableProperties, 'convective')
        
        if len(convectTriggers) == 0:
            return {}
        
        
        ######################################################
        ## Graph Megawidget Algorithm Buttons
        ######################################################
        
        convectiveProbTrendTriggers = [x for x in convectTriggers if x.startswith('convectiveProbTrend') ]
        if len(convectiveProbTrendTriggers) > 0:
            probVals = mutableProperties['convectiveProbTrendGraph']['values']
            
            trigger = convectiveProbTrendTriggers[0]

            updatedProbTrend = updateProbtrend(probVals, trigger)
        
            ### Set new state of GMW with updated trend
            returnDict['convectiveProbTrendGraph'] = {'values' : updatedProbTrend}

    return returnDict

def applyFLInterdependencies(triggerIdentifiers, mutableProperties):
    
    returnDict = {}

    # Get any changes required for the rise-crest-fall read-only text fields.
    ufnChanges = applyRiseCrestFallInterdependencies(triggerIdentifiers, mutableProperties)

    # ## originalList is used in multiple cases.  Assign it only once
    oListTemp = mutableProperties.get("impactCheckBoxes")
    if oListTemp:
        originalList = oListTemp['extraData']['origList']
    else:
        originalList = None
    

    # ## For Impacts and Crests interaction
    impactsCrestsChanges = None
    if triggerIdentifiers is not None:
        impactsCrestsChanges = {}

        if originalList:
            currentVals = mutableProperties["impactCheckBoxes"]['values']
            textFields = [tf for tf in mutableProperties if tf.startswith('impactTextField_')]
            
            for tf in textFields:
                impactsCrestsChanges[tf] = { "enable" : True}
            
            offCheckList = list(set(originalList).difference(currentVals))
    
            for off in offCheckList:
                offText = 'impactTextField_' + off.split('_')[-1]
                impactsCrestsChanges[offText] = { "enable" : False}
        
        
    if triggerIdentifiers is None:
        impactsCrestsChanges = {}
        if originalList:
            impactsCrestsChanges["impactCheckBoxes"] = { "values": originalList }



    # Return None if no changes were needed for until-further-notice or for
    # impacts and crests; if changes were needed for only one of these,
    # return those changes; and if changes were needed for both, merge the
    # two dictionaries together and return the result.
    if ufnChanges is not None:
        returnDict.update(ufnChanges)
    if impactsCrestsChanges is not None:
        returnDict.update(impactsCrestsChanges)
    
    if len(returnDict):
        return returnDict
    else:
        return None



# Helper function to be called by metadata megawidget interdependency implementations
# that include Rise Above/Crest/Fall Below. It ensures that if these values change,
# the corresponding read-only text fields are changed to reflect the new values.
def applyRiseCrestFallInterdependencies(triggerIdentifiers, mutableProperties):
    
    # Determine which of the three possible description text fields (one for rise, one
    # for crest, and one for fall) are to be updated. If this is initialization time,
    # all should be updated. Otherwise, any that have had their corresponding attribute
    # changed, or that have themselves been changed, are to be updated. (It is possible
    # for the descriptions themselves to have been changed as a result of setting the
    # hazard event's attributes, which would zero them out since these descriptions are
    # unknown to the hazard event's attribute table. 
    IDENTIFIERS = ["riseAbove", "crest", "fallBelow" ]
    if triggerIdentifiers == None:
        toBeUpdated = IDENTIFIERS
    else:
        toBeUpdated = [identifier for identifier in IDENTIFIERS if identifier in triggerIdentifiers]
            
    # If any are to be updated, iterate through them, setting their descriptive text
    # appropriately, and return the resulting changed values.
    if len(toBeUpdated) > 0:
        from HazardConstants import MISSING_VALUE
        newMutableProperties = {}
        from datetime import datetime
        for identifier in toBeUpdated:
            try:
                if mutableProperties[identifier]["values"] == MISSING_VALUE:
                    newMutableProperties[identifier + "Description"] = { "values": "missing" }
                else:
                    timestamp = datetime.utcfromtimestamp(mutableProperties[identifier]["values"] / 1000)
                    newMutableProperties[identifier + "Description"] = { "values": timestamp.strftime("%d-%b-%Y %H:%M") }
            except:
                return None
        return newMutableProperties
    
    return None

def applyInterdependencies(triggerIdentifiers, mutableProperties):

    # See if the trigger identifiers list contains any of the identifiers from the
    # endingSynopsis canned choices; if so, change the text's value to match the associated
    # canned choice text.

    #  Example:
    #  triggerIdentifiers ['endingOption.Water is receding']
    #  mutableProperties {'__scrollableWrapper__': {'enable': True, 'editable': True, 'extraData': {}}, 
    #              'endingOption': {'enable': True, 'editable': True, 'extraData': {}, 'choices': 
    #              [{'displayString': 'Water is receding', 'identifier': 'Water is receding', u
    #                 'productString': 'Flood waters have receded...and are no longer expected to pose a threat to life or property.\n
    #                 Please continue to heed any road closures.'}, 
    #               {'displayString': 'Heavy rain ended', 'identifier': 'Heavy rain ended', 
    #                 'productString': 'Excess runoff from heavy rain has ended over the warned area. If flooding has been observed...
    #                 Please report it\n     to your local law enforcement agency.'}]}, 
    #              'endingSynopsis': {'editable': True, 'enable': True, 'values': '', 'extraData': {}}}
    propertyChanges = None
    if triggerIdentifiers:
        propertyChanges = {}
        for triggerIdentifier in triggerIdentifiers:
            if triggerIdentifier.find('endingOption') >= 0:
                chosen = triggerIdentifier.split('.')[1]
                choices = mutableProperties.get("endingOption").get("choices")
                for choice in choices:
                    if choice['identifier'] == chosen:
                        endingOption = choice.get('productString', chosen)
                        endingOption = endingOption.replace('  ', '')
                        endingOption = endingOption.replace('\n', ' ')
                        endingOption = endingOption.replace('<br/>', '\n')
                        endingOption = endingOption.replace('<br />', '\n')
                        endingOption = endingOption.replace('<br>', '\n')
                propertyChanges["endingSynopsis"] = {
                                "values" : endingOption,
                                } 
    return propertyChanges
                
def toBasis(eventType, eventTypeToBasis):   
    return eventTypeToBasis[eventType]


