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
from RiverForecastUtils import *
from HazardConstants import MISSING_VALUE

from ProbUtils import ProbUtils
import numpy as np
import datetime, math

import VTECConstants
from HazardConstants import *
import os, sys, re
from collections import OrderedDict
from MapsDatabaseAccessor import MapsDatabaseAccessor
import logging
import UFStatusHandler

import pprint
from com.raytheon.uf.common.time import SimulatedTime
from scipy import interpolate

CENTRAL_PROCESSOR_HOSTNAME = "max"

class MetaData(object):
    
    def __init__(self):
        self.logger = logging.getLogger('CommonMetaData')
        for handler in self.logger.handlers:
            self.logger.removeHandler(handler)
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.uf.common.dataplugin.hazards.hazardservices', 'CommonMetaData', level=logging.INFO))
        self.logger.setLevel(logging.INFO)

    def initialize(self, hazardEvent, metaDict):
        self.hazardEvent = hazardEvent
        self.metaDict = metaDict
        userName = metaDict.get('userName')
        workStation = metaDict.get('workStation')
                
        if self.hazardEvent:
            self.hazardStatus = self.hazardEvent.getStatus().lower()
        else:
            self.hazardStatus = "pending"
    
        self.riverForecastUtils = None
        self.riverForecastPoint = None
        self.probUtils = ProbUtils()
        self.CENTRAL_PROCESSOR = False
        import socket
        hostName = socket.gethostname()
        if CENTRAL_PROCESSOR_HOSTNAME == hostName:
            self.CENTRAL_PROCESSOR = True
        if self.CENTRAL_PROCESSOR:
            self.RECOMMENDER = ""
        else:
            self.RECOMMENDER = "SwathRecommender"
            
        self.caveUser = self.probUtils.getCaveUser(userName, workStation)
        print "CM---caveUser--", self.caveUser

    # This validate method is an interface to allow subclasses (MetaData_*.py)
    # to define what it means for the meta-data to be valid, allowing for 
    # cross-megawidget validation.
    #
    # This validate method is defined as base method which defines the need for
    # itself and derived MetaData_*.py classes to return a string which contains
    # either an error message which states why the validation failed, or the
    # value of None, which means that validation was successful.
    #
    # The default implementation of this method, contained below, always returns 
    # the successful string of None. In this way, derived classes only need to
    # implement a validate method when needed.
    def validate(self, hazardEvent):
        return None
           
    def editableWhenNew(self):
        # TODO When proposed for a follow up is implemented add more logic
        #      here to return false for the follow up case.
        return self.hazardStatus == "pending" or self.hazardStatus == "proposed"

    def getSource(self):
        choices = self.getSourceChoices()
        
        # Ensure that the current source is valid
        source = self.hazardEvent.get("source", None)
        identifiers = [choice.get("identifier", "") for choice in choices]
        if not source or source not in identifiers:
            source = self.defaultValue(choices)
            self.hazardEvent.set("source", source)

        return {
            "fieldName": "source",
            "fieldType":"RadioButtons",
            "label":"Source:",
            "values": source,
            "choices": choices,
            }

    def getEventType(self):
        return {
                 "fieldType":"ComboBox",
                 "fieldName": "eventType",
                 "label": "Event type:",
                 "expandVertically": False,
                 "values": "thunderEvent",
                 "choices": self.getEventTypeChoices()
                }
    
    def getRiverLabel(self, param="details"):
        lid = self.hazardEvent.get("pointID", "")
        riverGrp = self.hazardEvent.get("groupName", "")
        return {
            "fieldName": param + "ForecastPointsRiverLabel",
            "fieldType": "Label",
            "label": lid + " - " + riverGrp,
            "bold": True,
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
        
    
    def getImmediateCause(self, values='ER'):
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
            self.floodSeverityNoFlood(),
            self.floodSeverityMinor(),
            self.floodSeverityModerate(),
            self.floodSeverityMajor(),
            self.floodSeverityUnknown()
        ]        
    def floodSeverityNoFlood(self):
        ''' Determines which 'N' selection should be displayed based on the PhenSig.
            Selection is based on section 4.1.2 s(Flood Severity) from NWSI 10-1703:
            For Flood/Flash Flood Watches, Areal Flood Warnings, and Flash Flood Warnings,
            this element is coded as zero (0).
        '''
        phenSig = self.hazardEvent.getPhensig()
        if phenSig in ["FL.A", "FF.A", "FA.W", "FF.W"]:
            floodSeverity = self.floodSeverityArealOrFlash()
        else:
            floodSeverity = self.floodSeverityNone()
        return floodSeverity
    def floodSeverityNone(self):
        return {"displayString": "N (None)","identifier": "N","productString": ""}
    def floodSeverityArealOrFlash(self):
        return {"displayString": "N (Flood or Flash Flood)","identifier": "0","productString": ""}
    def floodSeverityMinor(self):
        return {"displayString": "1 (Minor)", "identifier": "1", "productString": "Minor"}
    def floodSeverityModerate(self):
        return {"displayString": "2 (Moderate)", "identifier": "2", "productString": "Moderate"}
    def floodSeverityMajor(self):
        return {"displayString": "3 (Major)", "identifier": "3", "productString": "Major"}
    def floodSeverityUnknown(self):
        return {"displayString": "U (Unknown)", "identifier": "U", "productString": ""}

    def getHiddenFloodSeverity(self):
        return {
             "fieldName": "floodSeverity",
             "fieldType":"HiddenField",
             "values": "0",
             "overrideOldValues": True
            }        
    def getHiddenFloodSeverityNone(self):
        return {
             "fieldName": "floodSeverity",
             "fieldType":"HiddenField",
             "values": "N",
             "overrideOldValues": True
            }        

    def getFloodRecord(self):
        return {
             "fieldName":"floodRecord",
             "fieldType":"ComboBox",
             "label":"Flood Record Status:",
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
                "productString": "...Flash flood emergency for |* includeEmergencyLocation *|...",
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
                 
    def validateRainSoFar(self,hazardEvent):
        rainAmt = hazardEvent.get("rainAmt")
        if rainAmt == "rainKnown":
            lowerAmt = hazardEvent.get("rainSoFarLowerBound")
            upperAmt = hazardEvent.get("rainSoFarUpperBound")
            hazardEventId = hazardEvent.getEventID()
            return self._validateRange(hazardEventId,lowerAmt,upperAmt,"Rain So Far")
        return None

    def validateEndingOptions(self,hazardEvent):
        endingOptions = hazardEvent.get('endingOption', None)
        
        if endingOptions is not None:
            if 'advisoryUpgraded' in endingOptions and ('rainEnded' in endingOptions or 'recedingWater' in endingOptions):
                return "Cannot select Advisory Upgraded with Water is receding and/or Heavy rain ended Ending Options"
        return None
        
    def validateAdditionalRain(self,hazardEvent,checkStatus=False):
        if checkStatus:
            if hazardEvent.getStatus() in ["elapsed","ending","ended"]:
                return None
        additionalInfo = hazardEvent.get("additionalInfo",[])
        if not "addtlRain" in additionalInfo:
             return None
        lowerAmt = hazardEvent.get("additionalRainLowerBound")
        upperAmt = hazardEvent.get("additionalRainUpperBound")
        hazardEventId = hazardEvent.getEventID()
        return self._validateRange(hazardEventId,lowerAmt,upperAmt,"Additional Rain")

    def _validateRange(self,hazardEventId,lowerAmt,upperAmt,rangeType):
        if (lowerAmt > upperAmt) :
            return hazardEventId + ": Please enter a range ascending from left to right for \"" + rangeType + "\"." 
        else :
            return None

    def validateLocation(self, hazardEvent):
        includeLocation = hazardEvent.get("include")
        if includeLocation is not None:
            location = hazardEvent.get("includeEmergencyLocation")
            if location is None or not location.strip():
                hazardEventId = hazardEvent.getEventID()
                return hazardEventId + ': Enter Flash Flood Emergency Location'
        return None
                                            
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
        return  {"identifier":"rainKnown", "displayString":"Between",
                 "detailFields": [
                    {
                        "fieldType": "FractionSpinner",
                        "fieldName": "rainSoFarLowerBound",
                        "sendEveryChange": False,
                        "minValue": 0,
                        "maxValue": 99,
                        "values": 0,
                        "incrementDelta": 1,
                        "precision": 1
                    },
                    {
                        "fieldType": "FractionSpinner",
                        "fieldName": "rainSoFarUpperBound",
                        "label": " and",
                        "sendEveryChange": False,
                        "minValue": 0,
                        "maxValue": 99,
                        "values": 0,
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
                "productString": "|* basisEnteredText *|",
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
        return {
             "fieldType":"CheckBox",
             "fieldName": "listOfCities",
             "label": "Select for a list of cities",
             "values": defaultOn,
            }

    def getLocationsAffected(self, pathcastDefault=True):
        defidx = 0
        choices = [self.cityList(), self.noLocations()]
        if self.hazardEvent.get("trackPoints") and self.hazardEvent.get("stormMotion"):
            # StormTrack Recommender was used to create hazard
            choices.insert(0, self.pathcast())
            if not pathcastDefault :
                defidx = 1

        return {
             "fieldType":"RadioButtons",
             "fieldName": "locationsAffectedRadioButton",
             "label": "Locations Affected (4th Bullet)",
             "choices": choices,
             "values": choices[defidx].get("identifier"),
            }

    def damInfo(self):
        return {"identifier": "damInfo",
                "displayString": "Dam Info",
            }

    def pathcast(self):
        return {"identifier": "pathcast",
                "displayString": "Pathcast",
            }

    def cityList(self):
        return {"identifier": "cityList",
                "displayString": "List of Cities",
            }

    def noLocations(self):
        return {"identifier": "noLocations",
                "displayString": "None",
            }

    def getForceSegment(self):
        return {
             "fieldType": "Text",
             "fieldName": "forceSeg",
             "expandHorizontally": False,
             "label" : "Segment Number:",
             "maxChars": 4,
             "visibleChars": 4,
             "editable": True,
             "numericOnly" : True
             }

    def getAdditionalInfo(self, refreshMetadata=False):
            return {
                     "fieldType":"CheckBoxes",
                     "fieldName": "additionalInfo",
                     "showAllNoneButtons" : False,
                     "label": "Additional Info:",
                     "choices": self.additionalInfoChoices(),
                     "lines": 3,
                     "refreshMetadata": refreshMetadata,
                    }
    def listOfDrainages(self):
        return {"identifier":"listOfDrainages",
                "displayString": "Automated list of drainages",
                "productString": "This includes the following streams and drainages..." }
    def additionalRain(self):
        return  {"identifier":"addtlRain",
                 "displayString": "Additional rainfall",
                 "productString": 
                    '''Additional rainfall amounts of |* additionalRainLowerBound *| to |* additionalRainUpperBound *| inches are possible in the
                    warned area.''',
                 "detailFields": [
                        {
                             "fieldType": "FractionSpinner",
                             "fieldName": "additionalRainLowerBound",
                             "label": "of",
                             "sendEveryChange": False,
                             "minValue": 0,
                             "maxValue": 99,
                             "values": 0,
                             "incrementDelta": 1,
                             "precision": 1
                        },
                        {
                             "fieldType": "FractionSpinner",
                             "fieldName": "additionalRainUpperBound",
                             "label": " to",
                             "sendEveryChange": False,
                             "minValue": 0,
                             "maxValue": 99,
                             "values": 0,
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
                '''Flood waters are moving down the |* riverName *| from |* upstreamLocation *| to 
                |* floodLocation *|. The flood crest is expected to reach |* downstreamLocation *| by |* additionalInfoFloodMovingTime *|.''',
                "detailFields": [
                            {
                             "fieldName":"additionalInfoFloodMovingTime",
                             "fieldType":"Time",
                             "label": "by:"
                            }
                      ]
                     }
 
    def getRiver(self):
        riverName = ''
        if hasattr(self, 'damOrLeveeName'):
            riverName = self.hazardEvent.get('riverName','')
            if not riverName and self.damOrLeveeName:
                # Attempt to populate the riverName from the dam metadata if available
                mapsAccessor = MapsDatabaseAccessor()
                damMetaData = mapsAccessor.getDamInundationMetadata(self.damOrLeveeName)
                if damMetaData:
                    riverName = damMetaData.get("riverName", "")

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
                             "pageName": "Calls to Action",
                             "pageFields": [pageFields]
                            }
                         ],
                "expandedPages": ["Calls to Action"]      
                }

####################### 12 Standard Hydro CTAs ############################
    def ctaTurnAround(self):
        return {"identifier": "turnAroundCTA",
                "displayString": "Turn around, don't drown",
                "productString":
               '''Turn around, don't drown when encountering flooded roads. Most flood
               deaths occur in vehicles.'''}

    def ctaActQuickly(self):
        return {"identifier": "actQuicklyCTA",
                "displayString": "Act Quickly",
                "productString": 
                "Move to higher ground now. Act quickly to protect your life."}

    def ctaFFWEmergency(self):
        return {"identifier": "ffwEmergencyCTA",
                "displayString": "FLASH FLOOD EMERGENCY",
                "productString": 
                "Move to higher ground now! This is an extremely dangerous and life-threatening situation. Do not attempt to travel unless you are fleeing an area subject to flooding or under an evacuation order."}

    def ctaChildSafety(self):
        return {"identifier": "childSafetyCTA",
                "displayString": "Child Safety",
                "productString": 
                '''Keep children away from storm drains, culverts, creeks and streams.
                Water levels can rise rapidly and sweep children away.'''}

    def ctaNightTime(self):
        return  {"identifier": "nighttimeCTA",
                "displayString": "Nighttime flooding",
                "productString":
                '''Be especially cautious at night when it is harder to recognize the
                dangers of flooding.'''}

    def ctaUrbanFlooding(self):
        return  {"identifier": "urbanFloodingCTA",
               "displayString": "Urban areas",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of small
              creeks and streams, urban areas, highways, streets and underpasses as
              well as other drainage areas and low lying spots.'''}

    def ctaRuralFlooding(self):
        return {"identifier": "ruralFloodingCTA",
               "displayString": "Rural areas",
               "productString": 
              '''Excessive runoff from heavy rainfall will cause flooding of small
              creeks and streams, country roads, farmland, and other low lying
              spots.'''}

    def ctaStayAway(self):
        return {"identifier": "stayAwayCTA",
                "displayString": "Stay away",
                "productString":
                '''Stay away or be swept away. River banks and culverts can become
                unstable and unsafe.'''}
    def ctaLowSpots(self):
        return {"identifier": "lowSpotsCTA",
               "displayString": "Low spots in hilly terrain",
               "productString": 
              '''In hilly terrain there are hundreds of low water crossings which are
              potentially dangerous in heavy rain. Do not attempt to cross flooded
              roads. Find an alternate route.'''}

    def ctaArroyos(self):
        return {"identifier": "arroyosCTA",
                "displayString": "Arroyos",
                "productString": 
                '''Remain alert for flooding even in locations not receiving rain.
                Arroyos, streams, and rivers can become raging killer currents in a
                matter of minutes, even from distant rainfall.'''}

    def ctaBurnAreas(self):
        return {"identifier": "burnAreasCTA",
                "displayString": "Burn Areas",
                "productString": 
                '''Move away from recently burned areas. Life-threatening flooding of
                creeks, roads and normally dry arroyos is likely. The heavy rains
                will likely trigger rockslides, mudslides and debris flows in steep
                terrain, especially in and around these areas.'''}

    def ctaCamperSafety(self):
        return {"identifier": "camperSafetyCTA",
               "displayString": "Camper/Hiker safety",
               "productString":
              '''Flooding is occurring or is imminent. It is important to know where
              you are relative to streams, rivers, or creeks which can become
              killers in heavy rains. Campers and hikers should avoid streams or
              creeks.'''}

    def ctaReportFlooding(self):
        return {"identifier": "reportFloodingCTA",
                "displayString": "Report flooding to law enforcement",
                "productString": 
               '''Please report flooding to your local law enforcement agency when you
               can do so safely.'''}

####################### Means CTAs ############################
    def ctaFlashFloodWarningMeans(self):
        return {"identifier": "ffwMeansCTA",
                "displayString": "Flash Flood Warning means",
                "productString": 
                '''A Flash Flood Warning means that flooding is imminent or occurring.
                If you are in the warned area move to higher ground immediately.
                Residents living along streams and creeks should take immediate
                precautions to protect life and property.'''}

    def ctaFloodAdvisoryMeans(self):
        return {"identifier": "floodAdvisoryMeansCTA",
               "displayString": "Flood Advisory means",
               "productString": 
               '''A Flood Advisory means river or stream flows are elevated, or ponding 
               of water in urban or other areas is occurring or is imminent.'''}

    def ctaFloodWarningMeans(self):
        return {"identifier": "floodWarningMeansCTA",
               "displayString": "Flood Warning means",
               "productString": 
              '''A Flood Warning means that flooding is imminent or occurring. All interested parties should take
              necessary precautions immediately.'''}

    def ctaFloodWatchMeans(self):
        return {"identifier": "FloodWatch",
                "displayString": "Flood Watch means", 
                "productString": '''A Flood Watch means there is a potential for flooding
                based on current forecasts.\n\nYou should monitor later forecasts and be
                alert for possible flood warnings.  Those living in areas prone to flooding
                should be prepared to take action should flooding develop.'''}

    def ctaPointFloodAdvisoryMeans(self):
        return {"identifier": "pointFloodAdvisoryMeansCTA",
                "displayString": "Flood Advisory means",
                "productString": 
                '''A Flood Advisory means minor flooding is possible and rivers are forecast to exceed bankfull. 
                If you are in the advisory area remain alert to possible flooding, or the possibility of the advisory being upgraded to a warning.''',
                }

####################### Product Level CTAs ############################
    def ctaSafety(self):
        return {"identifier": "safetyCTA",
                "displayString": "Safety by foot or motorist",
                "productString":
                "Do not enter or cross flowing water or water of unknown depth."}

    def ctaDoNotDrive(self):
        return {"identifier": "doNotDriveCTA",
                "displayString": "Do not drive through flooded areas",
                "productString": "Motorists should not attempt to drive around barricades or drive cars through flooded areas."}

    def ctaRiverBanks(self):
        return {"identifier": "riverBanksCTA",
                "displayString": "Walking near riverbanks",
                "productString": "Caution is urged when walking near riverbanks."}

    def ctaStayTuned(self):
        return {"identifier": "stayTunedCTA",
                "displayString": "Stay tuned to NOAA Weather Radio for further information",
                "productString": '''Stay tuned to further developments by listening to your local radio, 
                television, or NOAA Weather Radio for further information.'''}

    def ctaAutoSafety(self):
        return {"identifier": "autoSafetyCTA",
                "displayString": "Auto Safety",
               "productString":
               '''Flooding is occurring or is imminent. Most flood related deaths
                occur in automobiles. Do not attempt to cross water covered bridges,
                dips, or low water crossings. Never try to cross a flowing stream,
                even a small one, on foot. To escape rising water find another
                route over higher ground.'''}

    def ctaRisingWater(self):
        return { "identifier": "risingWaterCTA",
                "displayString": "To escape rising water",
                "productString": "To escape rising water, take the shortest path to higher ground."}

    def ctaForceOfWater(self):
        return {"identifier": "forceOfWaterCTA",
                "displayString": "Force of fast-moving flood water",
                "productString": '''Even 6 inches of fast-moving flood water can knock you off your feet 
                and a depth of 2 feet will float your car.  Never try to walk, swim, or drive through such swift water.  
                If you come upon flood waters, stop, turn around and go another way.'''}

    def ctaLastStatement(self):
        return {"identifier": "lastStatementCTA",
                "displayString": "Last river flood statement on this event",
                "productString": "This will be the last river flood statement on this event. Stay tuned to developments."}

    def ctaWarningInEffect(self):
        return {"identifier":  "warningInEffectCTA",
                "displayString": "This warning will be in effect",
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
    *********
    Advisory upgraded:
    
    The threat for widespread areal flooding has increased over the area. 
    Therefore the flood advisory has been upgraded to a flood warning. 
    Please refer to that bulletin for more information.
    '''        
    def getEndingOption(self):
        choices = self.endingOptionChoices()
        return {
             "fieldType":"CheckBoxes",
             "fieldName": "endingOption",
             "label":"Ending Options:",
             "values": [],
             "choices": choices,
                }

    def recedingWater(self):  # EXP / CAN
        return {"identifier":"recedingWater", 
                "displayString": "Water is receding",
                "productString": 'Flood waters have receded.'}
    def rainEnded(self):  # EXP / CAN
        return {"identifier":"rainEnded",
                "displayString": "Heavy rain ended",
                "productString": 'The heavy rain has ended.'}
    def advisoryUpgraded(self):  # EXP / CAN
        return {"identifier":"advisoryUpgraded",
                "displayString": "Advisory Upgraded",
                "productString": "The threat for widespread areal flooding has increased over the area. " \
                    "Therefore the flood advisory has been upgraded to a flood warning. " \
                    "Please refer to that bulletin for more information."}
        
    def riverFlooding(self):  # EXP / CAN
        return {"identifier":"riverFlooding",
                "displayString": "River Flooding",
                "productString": '''Flooding on the |* riverName *| river has receded and is no longer expected
to pose a significant threat. Please continue to heed all road closures.'''}

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

        self.getRiverForecastPoint(pointID, True)
        PE = self.riverForecastPoint.getPhysicalElement()
        
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
    
        values = OrderedDict()
        values['CurObs'] = '{:<15.2f}'.format(curObs)
        values['MaxFcst'] = '{:<15.2f}'.format(maxFcst)
        values['FldStg'] = '{:<15.2f}'.format(fldStg)
        values['FldFlow'] = '{:<15.2f}'.format(fldFlow)
        values['Lookup PE'] = '{:15s}'.format(PE)
        values['Based On Lookup PE'] =  self.basedOnLookupPE
        
        riverLabel = self.getRiverLabel(parm) 
        
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
        self.getRiverForecastPoint(pointID, True)
        primaryPE = self.riverForecastPoint.getPhysicalElement()
        maxLevel =  self.riverForecastUtils.getMaximumForecastLevel( self.riverForecastPoint, primaryPE)
        if maxLevel != MISSING_VALUE:
            choices.append('Max Forecast')
            
        obLevel =  self.riverForecastUtils.getObservedLevel( self.riverForecastPoint)
        if obLevel != MISSING_VALUE:
            choices.append('Current Observed')
        
        choices.reverse()
        
        # Ensure 'Current Obs/Max Fcst' is the default selection (the last element).
        refStageFlow = {
                            "fieldType": "ComboBox",
                            "fieldName": parm + "ReferenceStageFlow",
                            "label": "Reference Stage Flow:",
                            "choices": choices,
                            "values":  choices[-1],
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
        if self.riverForecastUtils is None:
            self.riverForecastUtils = RiverForecastUtils()
            
        self.getRiverForecastPoint(pointID, True)
        primaryPE = self.riverForecastPoint.getPhysicalElement()
        
        impactsTextField = None
        if parm == "impacts":
            headerLabel = "Impacts to Use"
            selectionLabel = "ImpactStg/Flow - Start - End - Tendency"
            
            simTimeMils = SimulatedTime.getSystemTime().getMillis()
            currentTime = datetime.datetime.utcfromtimestamp(simTimeMils / 1000)
            
            impactDataList = self.riverForecastUtils.getImpactsDataList(pointID, currentTime.month, currentTime.day)
            plist = JUtilHandler.javaCollectionToPyCollection(impactDataList)
            
            characterizations, physicalElements, descriptions = self.riverForecastUtils.getImpacts(plist, primaryPE, self.riverForecastPoint, filterValues)
            impactChoices, values = self.makeImpactsChoices(characterizations, physicalElements, descriptions)

            checkedValues = []
            searchType = filters.get('Search Type')
            searchTypeValue = searchType.get('values')

            if searchTypeValue:
                referenceType = filters['Reference Type']
                referenceTypeValue = referenceType.get('values')
                referenceValue = None
                if referenceTypeValue == 'Max Forecast' :
                    referenceValue =  self.riverForecastUtils.getMaximumForecastLevel( self.riverForecastPoint, primaryPE)
                elif referenceTypeValue == 'Current Observed' :
                    referenceValue =  self.riverForecastUtils.getObservedLevel( self.riverForecastPoint)
                elif referenceTypeValue == 'Current Obs/Max Fcst':
                    fcstLevel = self.riverForecastUtils.getMaximumForecastLevel(self.riverForecastPoint, primaryPE)
                    obsLevel = self.riverForecastUtils.getObservedLevel(self.riverForecastPoint)
                    referenceValue = max(fcstLevel,obsLevel)

                stageWindowLower = filters['Stage Window Lower']['values']
                stageWindowUpper = filters['Stage Window Upper']['values']
    
                tupleList, tupleMap = self.createImpactsData(values)
                # Sort the list of tuples
                tupleList.sort()
                if referenceValue == None or referenceValue == MISSING_VALUE:
                    # No obs or maxfcst value to reference. Default to checking
                    # the first impact only.
                    if len(tupleList) > 0 :
                        defaultChoice = tupleMap.get(tupleList[0], None)
                        if defaultChoice:
                            checkedValues.append(defaultChoice)
                else:
                    if searchTypeValue == 'All Below Upper Stage/Flow':
                        for impactTuple in tupleList:
                            value, trend = impactTuple
                            if value <= referenceValue + stageWindowUpper:
                                checkedValues.append(tupleMap.get(impactTuple))
                    else:
                        windowTuples = []
                        if searchTypeValue == 'Closest in Stage/Flow Window':
                            for impactTuple in tupleList:
                                value, trend = impactTuple
                                if value > referenceValue + stageWindowLower and value < referenceValue + stageWindowUpper:
                                    windowTuples.append(impactTuple)

                            closest = (-9999, "Rising")
                            delta = 9999
                            # select closest to the reference value 
                            for impactTuple in windowTuples:
                                value, trend = impactTuple
                                if abs(value - referenceValue) < delta:
                                    closest = impactTuple
                                    delta = abs(value - referenceValue)
                            if (closest[0] != -9999):
                                checkedValues.append(tupleMap.get(closest))

                        elif searchTypeValue == 'Highest in Stage/Flow Window':
                            for impactTuple in tupleList:
                                value, trend = impactTuple
                                if value > referenceValue + stageWindowLower and value < referenceValue + stageWindowUpper:
                                    windowTuples.append(impactTuple)

                            highest = (-9999, "Rising")
                            # select the highest in the list
                            for impactTuple in windowTuples:
                                value, trend = impactTuple
                                if value > highest[0]:
                                    highest = impactTuple;
                            if highest[0] != -9999: 
                                checkedValues.append(tupleMap.get(highest))
                    
            selectedForecastPoints = {
                                      "fieldType":"CheckBoxes",
                                      "fieldName": "impactCheckBoxes",
                                      "label": "Impacts",
                                      "choices":  self.sortImpactsChoices(impactChoices, values),
                                      "values" : checkedValues,
                                      "extraData" : { "origList" : checkedValues },
                                      "overrideOldValues": True
                                      }
            
        else:
            headerLabel = "Crest to Use"
            selectionLabel = "CrestStg/Flow - CrestDate"
            defCrest, crestList =  self.riverForecastUtils.getHistoricalCrest( self.riverForecastPoint, primaryPE, filterValues)

            # Prune any duplicate entries from the list of crests.
            #
            # TODO: This is needed because duplicate entries are being found in
            # at least one case (run RFR with "include points below advisory"
            # checked, select one of the HY.S events, and change its event type
            # to FL.A). Presumably no such duplicates should be returned from
            # the river forecast utils; this should be looked into.
            #
            prunedCrestList = []
            lastCrest = None
            for crest in crestList:
                if crest != lastCrest:
                    lastCrest = crest
                    prunedCrestList.append(crest)
            crestList = prunedCrestList

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
                    "expandVertically": True,
                    "overrideOldValues": True
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

    def makeImpactsChoices(self, characterizations, physicalElements, descriptions):
        choices = []
        values = []
        
        zipped = zip(characterizations, physicalElements, descriptions)
        zipped.sort()
        for char, pe, desc in zipped :
            id = "impactCheckBox_" + pe + '_'+ char
            entry = {
                     "identifier": id,
                     "displayString":str(char) ,
                     "physicalElement": pe,
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

    def createImpactsData(self, values):
        tupleList = []
        tupleMap = {}
        for value in values:
            # Split out the float value
            strings = value.split('-')
            trend = strings[3]
            strings = strings[0].split('_')
            floatValue = float(strings[2])
            impactTuple = (floatValue, trend)
            tupleList.append(impactTuple)
            tupleMap[impactTuple] = value
        return tupleList, tupleMap

    def sortImpactsChoices(self, choices, values):
        # Sort the choices so they are in order on the HID
        floatValues, floatMap = self.createImpactsData(values)

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
            if self.riverForecastUtils is None:
                self.riverForecastUtils = RiverForecastUtils()
            self.riverForecastPoint = self.riverForecastUtils.getRiverForecastPoint(pointID, isDeepQuery)

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
        
        # If no dam or levee name was provided, use the first one
        # if there are choices available; if there are no choices
        # available, use the empty string, and make it the only
        # choice.
        if not damOrLeveeName:
            if len(choices) > 0:
                damOrLeveeName = choices[0]
            else:
                damOrLeveeName = ""
                choices.append(damOrLeveeName)
                
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

        # Add framed text to the list of dam/levee name choices to indicate
        # that the user can manually type into the combo box if the query fails
        # or does not contain the desired dam name.
        framedText =  "|* Enter Dam/Levee Name *|"
        ids = {}
        ids["identifier"] = framedText
        ids["displayString"] = framedText
        ids["productString"] = framedText
        damList.append(ids)

        try:
            mapsAccessor = MapsDatabaseAccessor()
            damOrLeveeNames = mapsAccessor.getPolygonNames(DAMINUNDATION_TABLE)
            for damOrLeveeName in sorted(damOrLeveeNames):
                ids = {}
                ids["identifier"] = damOrLeveeName
                ids["displayString"] = damOrLeveeName
                ids["productString"] = damOrLeveeName
                damList.append(ids)
        except:
            self.logger.exception("Could not retrieve dam inundation data.")
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
        attrs = self.hazardEvent.getHazardAttributes().get('probSeverAttrs', {})
        
        vals = []
        for k,v in attrs.iteritems():
            if isinstance(v, datetime.datetime):
                v = str(v)
            if isinstance(v, long):
                v = str(datetime.datetime.fromtimestamp(v/1000))
                
            vals.append([k, v])
        
        if len(vals) == 0:
            vals.append(['N/A', 'N/A'])
        
                        
        tbl = {
            "fieldType": "Table",
            "fieldName": "convectiveGetAttrs",
            "label": "Cell Attributes (only automated objects):",
            "lines": 4,
            "columnHeaders": [ "Category", "Value"],
            "values": vals,
        }
        
        eb = {
                "fieldType": "ExpandBar",
                "fieldName": "expandBar1",
                "leftMargin": 10,
                "rightMargin": 10,
                "topMargin": 5,
                "bottomMargin": 5,
                "expandHorizontally": False,
                "expandVertically": False,
                "pages": [
                           {
                            "pageName": "ProbSevere Attrs",
                            "pageFields": [tbl]
                            } 
                         ]
              }
                
        
        return eb
        
    # Prob_Severe and Prob_Tornado
    def convectiveControls(self): 
        activate, activateModify = self.probUtils.setActivation(self.hazardEvent, self.caveUser, modify=False)
        mws = self.initializeObject(activate, activateModify) 
        #activate = self.hazardEvent.get('activate', False)      
        #activateModify = self.hazardEvent.get('activateModify', False)      
        print  "CM ConvectiveControls -- activate, activateModify", activate, activateModify
        self.flush()       
        
        # event modification
        mws.append(self.getEventModification(activate, activateModify))
        # automation buttons and shape automation
        mws.append(self.getAutomationSettingsAndShape(activate))
        
#         mws.append( self.getConvectiveObjectInfo(activate, activateModify))
#         
#         # Shape
#         mws.append( self.getConvectiveShape(activate))
        
        # Motion Vector
        mws.append( self.getConvectiveMotionVector(activate))
        # Probability Trend
        mws.append( self.getConvectiveProbabilityTrend(activate))
        # Storm Chars
        mws.append( self.getStormCharacteristics(activate))
        # Warning Discussion
        mws.append( self.getConvectiveDiscussion(activate))
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
    
    def initializeObject(self, activate, activateModify):        
        if self.hazardEvent.get('objectID') is None:
            # Go with eventID as it should be unique
            self.hazardEvent.set('objectID',  'M' + self.hazardEvent.getDisplayEventID())
            self.hazardEvent.set('manuallyCreated', True)
            self.hazardEvent.set('owner', self.caveUser)
        print "CM calling setActivation"
        #print '\n\nCM - manuallyCreated?', self.hazardEvent.get('manuallyCreated'), ' <<<\n\n'
        #self.flush()
        status = self.hazardEvent.getStatus()         
        mwList = [
            {
             "fieldType": "HiddenField",
             "fieldName": "activate",
             "values": activate,
             "doesNotAffectModifyFlag": True,
             },
            {
             "fieldType": "HiddenField",
             "fieldName": "activateModify",
             "values": activateModify,
             "doesNotAffectModifyFlag": True,
             },
            {
             "fieldType": "HiddenField",
             "fieldName": "statusForHiddenField",
             "values": status,
             "doesNotAffectModifyFlag": True,
             },
            {
             "fieldType": "HiddenField",
             "fieldName": "manuallyCreatedStatus",
             "values": self.hazardEvent.get('manuallyCreated', True)
             },
            {
            "fieldType": "HiddenField",
            "fieldName": "owner",
            "doesNotAffectModifyFlag": True,
            "values": self.hazardEvent.get("owner", None)
            },  
            {
            "fieldType": "HiddenField",
            "fieldName": "ownerChangeRequest",
            "modifyRecommender": 'OwnershipTool',
            "doesNotAffectModifyFlag": True,
            "values": self.hazardEvent.get("ownerChangeRequest", None)
            },
        ]        
        return mwList

    def getConvectiveDuration(self, enable):        
        
        print "\nCM-ACTIVATE-getConvectiveDuration:", enable
        self.flush()
        
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveDurationGroup",
            "label": "Duration",
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,                        
            "numColumns":1,
            "fields": [
                        self.getAutoDuration(enable),
                       ],
        }  
        
        return grp 
    
    def getConvectiveShape(self, enable):        
        
        print "\nCM-ACTIVATE-getConvectiveShape:", enable
        self.flush()
        
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveShapeGroup",
            "label": "Shape/Position",
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,                        
            "numColumns":1,
            "fields": [
                        self.getAutoShape(enable),
                       ],
        }  
        
        return grp 
    
    def getAutomationButtons(self, activate):      
        
        enableAutomated = self.getEnableAutomated(activate)
        print "\nCM-ACTIVATE-getConvectiveObjectInfo:", activate
        print '\tenableAutomated:', enableAutomated, '\n'
        self.flush()
                
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveAutomationGroup",
            "label": "Automation",
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,                        
            "numColumns":2,
            "fields": [                       
                        {
                        "fieldType": "Button",
                        "fieldName": "automateAllButton",
                        "label": "Automate All",
                        "editable": enableAutomated,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "automateNoneButton",
                        "label": "Automate None",
                        "editable": enableAutomated,
                        },                           
                    ],
            }
        
        return grp
    
    def getAutomationSettingsAndShape(self, activate):        
        
        print "\nCM-ACTIVATE-getAutomationSettingsAndShape:", activate
        self.flush()       
        
        grp = {
            "fieldType": "Composite",
            "fieldName": "AutomationSettingCompositeGroup",
            "label": "Automation",
            "numColumns":3,
            "fields": [
                       self.getAutomationButtons(activate),
                       self.getConvectiveShape(activate),                          
                       self.getConvectiveDuration(activate),                          
                    ],
            }
        
        return grp
    
    def getConvectiveObjectInfo(self, activate, activateModify):        
        
        print "\nCM-ACTIVATE-getConvectiveObjectInfo:", activate, activateModify
        self.flush()       
        
        grp = {
            "fieldType": "Composite",
            "fieldName": "convectiveCompositeGroup",
            "label": "Automation",
            "numColumns":3,
            "fields": [
                       self.getEventModification(activate, activateModify),
                        {
                        "fieldType": "Label",
                        "fieldName": "labelForNothing",
                        "label": "                   ",
                        },                        
                       self.getAutomationButtons(activate),                          
                    ],
            }
        
        return grp
    
    def getEventModification(self, activate, activateModify):        
        
        print "\nCM-ACTIVATE-getEventModification:", activate, activateModify
        self.flush()       
        
        grp = {
            "fieldType": "Composite",
            "fieldName": "EventModificationCompositeGroup",
            "label": " ",
            "numColumns":1,
            "fields": [
                       self.getEventModificationGroup(activate, activateModify),                          
                    ],
            }
        
        return grp    
    
    def getEventModificationGroup(self, activate, activateModify):        
         
        print "\nCM-ACTIVATE-getEventModificationGroup:", activate, activateModify
        self.flush()

        grp = {
            "fieldType": "Group",
            "fieldName": "EventModificationGroup",
            "label": "Event Modification", ### BUG ALERT: rename?
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,            
            "numColumns":3,
            "fields": [
                        self.getModifyButton(activateModify),
                        self.getCancelButton(activate),
                        self.getCopyButton(),
                    ]
        }        
        return grp
    
    def getAutoDuration(self, enable):
        enableAutomated = self.getEnableAutomated(enable)
        print "\nCM-ACTIVATE-getAutoDuration:", enable
        print '\tenableAutomated:', enableAutomated, '\n'
        self.flush()

        if self.hazardEvent.get("durationAutomated"):
            durationAutomated = True
        else:
            durationAutomated = False
        return {
            "fieldType": "CheckBox",
            "fieldName": "durationAutomated",
            "label": "Automate",
            "sendEveryChange": False,
            "values": durationAutomated,
            "modifyRecommender": self.RECOMMENDER,
            "editable": enableAutomated,
            }
    
    def getAutoShape(self, enable):
        enableAutomated = self.getEnableAutomated(enable)
        print "\nCM-ACTIVATE-getAutoShape:", enable
        print '\tenableAutomated:', enableAutomated, '\n'
        self.flush()

        if self.hazardEvent.get("geometryAutomated"):
            geometryAutomated = True
        else:
            geometryAutomated = False
        return {
            "fieldType": "CheckBox",
            "fieldName": "geometryAutomated",
            "label": "Automate",
            "sendEveryChange": False,
            "values": geometryAutomated,
            "modifyRecommender": self.RECOMMENDER,
            "editable": enableAutomated,
            }
    
    def getModifyButton(self, enable):               
        grp = {
            "fieldType": "Button",
            "fieldName": "modifyButton",
            "label": "     MODIFY     ",
            "editable": enable,
            "modifyRecommender": self.RECOMMENDER,
        }        
        return grp
    
    def getCopyButton(self, enable = False):               
        grp = {
            "fieldType": "Button",
            "fieldName": "copyButton",
            "label": "     Copy     ",
            "editable": enable,
            "modifyRecommender": self.RECOMMENDER,
        }        
        return grp
    
    def getCancelButton(self, activate): 
        
#         if self.hazardStatus == 'issued' and not self.hazardEvent.get('geometryAutomated'): # old: self.hazardEvent.get('automationLevel') in ['userOwned', 'attributesOnly']:
#             enable = True
#         else:
#             enable = False

        # we want the "End Object" button to be in sync with other editing buttons
        enable = activate
              
        grp = {
            "fieldType": "Button",
            "fieldName": "cancelButton",
            "label": "*** End Object ***",
            "editable": enable,
            "modifyRecommender": "ConfirmRecommender", #self.RECOMMENDER,
        }
        return grp
    
    def getMotionVectorAutomationGroup(self, enable, enableAutomated, recommender):        
         
        print "\nCM-ACTIVATE-getMotionVectorAutomationGroup"
        self.flush()

        grp = {
            "fieldType": "Composite",
            "fieldName": "MotionVectorAutomationComposite",
            "label": "motion vector automation composite",
            "numColumns":3,
            "fields": [
                        {
                        "fieldType": "CheckBox",
                        "fieldName": "motionAutomated",
                        "label": "Automate",
                        "values": False,
                        "modifyRecommender": recommender,
                        "editable": enableAutomated,
                        },
                       {
                        "fieldType": "CheckBox",
                        "fieldName": "showGrid",
                        "label": "Preview Grid",
                        "sendEveryChange": False,
                        "showScale": False,
                        "values": False,
                        "modifyRecommender": recommender,
                        "doesNotAffectModifyFlag": True,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "resetMotionVector",
                        "label": "Reset Motion Vector",
                        "values": False,
                        "modifyRecommender": recommender,
                        "editable": enable,
                        },
                    ]
        }        
        return grp

    def getMotionVectorAutomationDetails(self, enable, recommender):        
         
        wdir = self.probUtils.getDefaultMotionVectorKey(self.hazardEvent, 'convectiveObjectDir')
        wspd = self.probUtils.getDefaultMotionVectorKey(self.hazardEvent, 'convectiveObjectSpdKts')
        print "\nCM-ACTIVATE-getMotionVectorAutomationDetails"
        self.flush()

        grp = {
            "fieldType": "Composite",
            "fieldName": "MotionVectorAutomationDetailsComposite",
            "label": "motion vector automation details composite",
            "numColumns":3,
            "fields": [
                        {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectDir",
                        "sendEveryChange": False,
                        "label": "Motion Vector: Dir (deg)",
                        "minValue": 0,
                        "maxValue": 359,
                        "values": int(wdir),
                        "incrementDelta": 5,
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "editable": enable,
                        },
                        {
                         "fieldType": "HiddenField",
                         "fieldName": "convRecPastconvectiveObjectDirHID",
                         "values": self.hazardEvent.get('convRecPastconvectiveObjectDir', self.hazardEvent.get('convectiveObjectDir'))
                         },
                        {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectSpdKts",
                        "label": "Spd (kts)",
                        "sendEveryChange": False,
                        "minValue": 0,
                        "maxValue": 102,
                        "width": 2,
                        "values": int(wspd),
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "editable": enable,
                        },
                        {
                         "fieldType": "HiddenField",
                         "fieldName": "convRecPastconvectiveObjectSpdKtsHID",
                         "values": self.hazardEvent.get('convRecPastconvectiveObjectSpdKts', self.hazardEvent.get('convectiveObjectSpdKts'))
                         },
                       {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectDirUnc",
                        "label": "Dir Uncertainty (deg)",
                        "sendEveryChange": False,
                        "minValue": 12,
                        "maxValue": 45,
                        "values":  self.probUtils.getDefaultMotionVectorKey(self.hazardEvent, 'convectiveObjectDirUnc'),
                        "incrementDelta": 5,
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "editable": enable,
                        },
                        {
                         "fieldType": "HiddenField",
                         "fieldName": "convRecPastconvectiveObjectDirUncHID",
                         "values": self.hazardEvent.get('convRecPastconvectiveObjectDirUnc', self.hazardEvent.get('convectiveObjectDirUnc'))
                         },
                       {
                        "fieldType": "IntegerSpinner",
                        "fieldName": "convectiveObjectSpdKtsUnc",
                        "label": "Speed Uncertainty (kts)",
                        "sendEveryChange": False,
                        "minValue": 4,
                        "maxValue": 20,
                        "width": 2,
                        "values": self.probUtils.getDefaultMotionVectorKey(self.hazardEvent, 'convectiveObjectSpdKtsUnc'),
                        "showScale": False,
                        "modifyRecommender": recommender,
                        "editable": enable,
                        },
                        {
                         "fieldType": "HiddenField",
                         "fieldName": "convRecPastconvectiveObjectSpdKtsUncHID",
                         "values": self.hazardEvent.get('convRecPastconvectiveObjectSpdKtsUnc', self.hazardEvent.get('convectiveObjectSpdKtsUnc'))
                         },
                    ]
        }        
        return grp

    def getConvectiveMotionVector(self, activate):        
        
        print "\nCM-ACTIVATE-getConvectiveMotionVector:", activate
        self.flush()       
        
        grp = {
            "fieldType": "Composite",
            "fieldName": "ConvectiveMotionVectorCompositeGroup",
            "label": " ",
            "numColumns":1,
            "fields": [
                       self.getConvectiveMotionVectorGroup(activate),                          
                    ],
            }
        
        return grp 

    def getConvectiveMotionVectorGroup(self, enable):
        enableAutomated = self.getEnableAutomated(enable)
        print "\nCM-ACTIVATE-getConvectiveMotionVectorGroup:", enable
        print '\tenableAutomated:', enableAutomated, '\n'
        self.flush()
        
        
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveMotionVectorGroup",
            "label": "Motion Vector",
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,                        
            "numColumns":1,
            "fields": [
                       self.getMotionVectorAutomationGroup(enable, enableAutomated, self.RECOMMENDER),
                       self.getMotionVectorAutomationDetails(enable, self.RECOMMENDER),
                       ]
        }        
        return grp

    def getConvectiveSwathPresets(self, enable):
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
            "modifyRecommender": self.RECOMMENDER,
            "editable": enable,
        }
        
        return presets

    def getConvectiveProbabilityTrendAutomation(self, enable, enableAutomated, recommender):        
         
        grp = {
            "fieldType": "Composite",
            "fieldName": "ProbabilityTrendAutomationComposite",
            "label": "probability trend automation composite",
            "numColumns":3,
            "fields": [
                       {
                        "fieldType": "CheckBox",
                        "fieldName": "probTrendAutomated",
                        "label": "Automate",
                        "values": False,
                        "modifyRecommender": recommender,
                        "editable": enableAutomated,
                        },
                        {
                        "fieldType": "Label",
                        "fieldName": "convectiveProbTrendLabel",
                        "label": "Interpolation:"
                        },

                        self.getConvectiveProbabilityButtons(enable),
                    ]
        }        
        return grp
    
    def getConvectiveProbabilityButtons(self, enable):        
         
        grp = {
            "fieldType": "Composite",
            "fieldName": "ProbabilityTrendButton",
            "label": "probability trend button composite",
            "numColumns":7,
            "columnSpacing":3,
            "fields": [
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendDraw",
                        "label": "Draw",
                        "editable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendLinear",
                        "label": "Linear",
                        "editable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendExp1",
                        "label": "Exp1",
                        "editable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendExp2",
                        "label": "Exp2",
                        "editable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendBell",
                        "label": "Bell",
                        "editable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendPlus5",
                        "label": "+5",
                        "editable": enable,
                        },
                        {
                        "fieldType": "Button",
                        "fieldName": "convectiveProbTrendMinus5",
                        "label": "-5",
                        "editable": enable,
                        },

                    ]
        }        
        return grp                        
    
    def getConvectiveProbabilityTrend(self, enable):

        
        probInc = 5
        #self.hazardEvent.set('convectiveProbabilityTrendIncrement', probInc)
        graphProbs =  self.probUtils.getGraphProbs(self.hazardEvent, fromCommonMetaData=True)  
        previousDataLayerTime = self.probUtils.getPreviousDataLayerTime()
        colors = [ self.getProbTrendColor(y) for y in range(0,100, 20)]
                
        enableAutomated = self.getEnableAutomated(enable)
        print "\nCM-ACTIVATE-getProbTrend:", enable
        print '\tenableAutomated:', enableAutomated, '\n'
        self.flush()

        
        grp = {
            "fieldType": "Group",
            "fieldName": "convectiveProbabilityGroup",
            "label": "Prob Trend",
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,                        
            "expandHorizontally": False,
            "expandVertically": False,
            "numColumns":1,
            "fields": [
                       self.getConvectiveProbabilityTrendAutomation(enable, enableAutomated, self.RECOMMENDER),
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
                        "modifyRecommender": self.RECOMMENDER,
                        "sendEveryChange":False,
                        "width": 1,
                        "yColors": colors,
                        "values": graphProbs,
                        "editable": enable,
                        },

                        {
                         "fieldType": "HiddenField",
                         "fieldName": "previousDataLayerTime",
                         "values": previousDataLayerTime
                         },
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
                        {
                         "fieldType": "HiddenField",
                         "fieldName": "convRecPastconvectiveProbTrendGraphHID",
                         "values": self.hazardEvent.get('convRecPastconvectiveProbTrendGraph', self.hazardEvent.get('convectiveProbTrendGraph'))
                         },
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
            "editable": enable,
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

    def getStormCharacteristics(self, activate):        
        
        print "\nCM-ACTIVATE-getStormCharacteristics:", activate
        self.flush()       
        
        grp = {
            "fieldType": "Composite",
            "fieldName": "stormCharacteristicComposite",
            "label": " ",
            "numColumns":1,
            "fields": [
                       self.getStormCharacteristicsComposite(activate),                          
                    ],
            }
        
        return grp

    def getStormCharacteristicsComposite(self, enable):
        windType =  self.buildStormChars('Wind', [str(x) + " mph" for x in ["<40", 40, 50, 60, 70, 80, 90]], enable)

        hailType =  self.buildStormChars("Hail", ["Penny (0.75\")", "Nickel (7/8\")","Quarter (1\")", "Half dollar (1.25\")","Ping Pong (1.5\")", "Golfball (1.75\")","Hen Egg (2\")", "Tennis Ball (2.5\")", "Baseball (2.75\")","Softball(>=4\")", "copious small hail"], enable)
        
        tornType =  self.buildStormChars('Torn', ['radar indicated', 'radar observed', 'spotter observed'], enable)
        
        threatTypes = {
            "fieldType": "Composite",
            "fieldName": "convectiveStormCharsGroup",
            "label": "Storm Characteristics (included in discussion)",
            "numColumns":6,
            "columnSpacing":3,         
            "fields": [
                        {
                        "fieldType": "Label",
                        "fieldName": "maxWindLabel",
                        "label": "Max Wind:"
                        },
                       windType, 
                        {
                        "fieldType": "Label",
                        "fieldName": "maxHailLabel",
                        "label": "Max Hail:"
                        },
                       hailType, 
                        {
                        "fieldType": "Label",
                        "fieldName": "tornadoLabel",
                        "label": "Tornado:"
                        },
                       tornType,
                       ],
            "editable": enable,
            }

        
        return threatTypes

    def buildStormChars(self, typ, vals, enable):
        capType = typ.capitalize()
        labelDict = {'Wind':'', 'Hail': '', 'Torn': ''}
        #labelDict = {'Wind':'Max Wind', 'Hail': 'Max Hail', 'Torn': 'Tornado'}
        fieldName = "convectiveStormChars"+capType
        values = self.hazardEvent.get(fieldName, "None")
        choices = ["None"]
        choices.extend(vals)
        chars = {
            "fieldType": "ComboBox",
            "fieldName": fieldName,
            "label": labelDict.get(capType, 'THREAT'),
            "choices": choices,
            "values": values,
            "expandHorizontally": False,
            "editable": enable,
        }        
        return chars        

    def getEnableAutomated(self, secondBool=None):
        manuallyCreated = self.hazardEvent.get('manuallyCreated', True)
        enableAutomated = False if manuallyCreated else True
        if secondBool is not None:
            enableAutomated = True if enableAutomated and secondBool else False
        return enableAutomated
        
def applyConvectiveInterdependencies(triggerIdentifiers, mutableProperties):

    def convectiveFilter(myIterable, prefix):
        return [tf for tf in myIterable if tf.startswith(prefix)]

    def getCaveUser():
        import socket
        import getpass
        
        return  getpass.getuser()+":"+socket.gethostname()
    
    def isEqualOwner(owner1, owner2):
        #
        # assume the owner is composed of username:workstation
        # sometimes the workstation maybe different for the same machine
        # e.g snow, or snow.fnsl.gov
        # this case we will say the workstation is the same
        #
        print "Owner1---Owner2---", owner1, owner2
        if not owner1 or not owner2:
            return True
        ownerparts1 = owner1.lower().split(':')
        ownerparts2 = owner2.lower().split(':')
        if len(ownerparts1) < 2 or len(ownerparts2) < 2:
            return False
        return (ownerparts1[0].find(ownerparts2[0]) >= 0 or \
                ownerparts2[0].find(ownerparts1[0]) >= 0) and \
                (ownerparts1[1].find(ownerparts2[1]) >= 0 or \
                 ownerparts2[1].find(ownerparts1[1]) >= 0)    

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
        if len(mins) == 0:
            return []
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
    

    print "\n*****************\nCM applyConvective called"
    print 'CM applyConvective triggerIdentifiers ', triggerIdentifiers
    sys.stdout.flush()

    returnDict = {}

    # List of megawidgets that must be made editable or uneditable depending upon the activate
    # value.
    editabilityChangeableMegawidgets = ["resetMotionVector", "convectiveProbTrendDraw",
                "convectiveProbTrendLinear", "convectiveProbTrendExp1", "convectiveProbTrendExp2",
                "convectiveProbTrendBell", "convectiveProbTrendPlus5", "convectiveProbTrendMinus5",
                "convectiveProbTrendGraph", "convectiveWarningDecisionDiscussion",
                "convectiveStormCharsGroup","convectiveStormCharsWind", "convectiveStormCharsHail",
                "convectiveStormCharsTorn", "convectiveObjectDir", "convectiveObjectSpdKts",
                "convectiveObjectDirUnc", "convectiveObjectSpdKtsUnc"]

    # Determine whether or not the Modify, Automate All, or Automate None
    # button was clicked.
    modifyButtonChosen = False
    automateAllButtonChosen = False
    automateNoneButtonChosen = False
    if triggerIdentifiers and len(triggerIdentifiers) == 1:
        modifyButtonChosen = 'modifyButton' in triggerIdentifiers
        automateAllButtonChosen = 'automateAllButton' in triggerIdentifiers
        automateNoneButtonChosen = 'automateNoneButton' in triggerIdentifiers

    # If one of the properties related to motion automation changed value,
    # uncheck motion automated; and if one of the properties related to
    # prob trend automation changed value, uncheck prob trend automated.
#     if triggerIdentifiers:
#         triggerSet = set(triggerIdentifiers)
#         if automateAllButtonChosen:
#             returnDict['motionAutomated'] = {'values' : True}
#             returnDict['probTrendAutomated'] = {'values' : True}
#             returnDict['geometryAutomated'] = {'values' : True}
#             returnDict['durationAutomated'] = {'values' : True}
#         elif automateNoneButtonChosen:
#             returnDict['motionAutomated'] = {'values' : False}
#             returnDict['probTrendAutomated'] = {'values' : False}
#             returnDict['geometryAutomated'] = {'values' : False}
#             returnDict['durationAutomated'] = {'values' : False}        

    # Determine whether the editable flag is true or not (controlling
    # the editability of all the megawidgets listed above), and whether
    # the Modify button should be editable as well. If the Modify button
    # was clicked, these should be set to true and false, respectively.
    editable = False
    isOwner = isEqualOwner(mutableProperties['owner'].get("values", None), getCaveUser())
#     print "CM-- isOwner--", isOwner   
    if "activate" in mutableProperties:
        editable = mutableProperties["activate"].get("values", False) if isOwner else False
    editableModifyButton = True
    if "activateModify" in mutableProperties:
        editableModifyButton = mutableProperties["activateModify"].get("values", True) if isOwner else False
    if modifyButtonChosen: 
        editable = True
        editableModifyButton = False
    print 'CM applyConvective: editable, editableModifyButton =', editable, editableModifyButton
    sys.stdout.flush()

    # Ensure that no matter what else, the time range megawidget is made
    # editable or read-only as appropriate. This has to be done regardless
    # of the trigger because, for example, when the HID comes up, the time
    # range megawidget is enabled by default. 
    returnDict["__startTime__:__endTime__"] = { "editable": editable }
        
    # Ensure the the appropriate megawidgets are editable or uneditable.
    for key in editabilityChangeableMegawidgets:
        returnDict[key] = { 'editable' : editable }
            
    # Ensure the Modify button is enabled or disabled as appropriate.
    returnDict['modifyButton'] = { 'editable' : editableModifyButton }
    
    # Ensure "end object" button is enabled for the owner
#     returnDict['cancelButton'] = {'editable' : True if isOwner else False} 
    
    # Handle Modify, Automate All, and Automate None button presses. It's OK to
    # short-circuit returning here and return immediately instead of further
    # building the returnDict because these buttons can only be alone in the
    # trigger identifiers.
#     if modifyButtonChosen: 
#         manuallyCreatedStatus = mutableProperties.get('manuallyCreatedStatus', {})
#         manuallyCreated = manuallyCreatedStatus.get('values') # Should always be present
#         print "CM-MANUALLY_CREATED:",manuallyCreatedStatus, manuallyCreated , '<<<'
#         if manuallyCreated == 0 or manuallyCreated == False:
#             for key in ['geometryAutomated', "motionAutomated", 'probTrendAutomated','automateAllButton', 'automateNoneButton', 'durationAutomated']:
#                 returnDict[key] = { 'editable' : editable }
#         return returnDict
    if automateAllButtonChosen:
        returnDict['motionAutomated'] = {'values' : True}
        returnDict['probTrendAutomated'] = {'values' : True}
        returnDict['geometryAutomated'] = {'values' : True}
        returnDict['durationAutomated'] = {'values' : True}
        # disable the "End Object" button
        returnDict['cancelButton'] = {'editable' : False}
        print "Setting to AUTOMATE"
        print returnDict
        sys.stdout.flush()
        return returnDict
    elif automateNoneButtonChosen:
        returnDict['motionAutomated'] = {'values' : False}
        returnDict['probTrendAutomated'] = {'values' : False}
        returnDict['geometryAutomated'] = {'values' : False}
        returnDict['durationAutomated'] = {'values' : False}  
        # enable the "End Object" button
        returnDict['cancelButton'] = {'editable' : True}
        print "Setting to MANUAL"
        print returnDict
        sys.stdout.flush()
        return returnDict
    else:
        # diable the "end object" button for fully auto events
        # enable it for full manual or patial manual events
        isFullyAuto = (mutableProperties['geometryAutomated'].get('values', False) and 
                       mutableProperties['motionAutomated'].get('values', False) and
                       mutableProperties['probTrendAutomated'].get('values', False) and 
                       mutableProperties['durationAutomated'].get('values', False) )
        returnDict['cancelButton'] = {'editable' : True if (isOwner and not isFullyAuto) else False}
        print "CM-- isOwner, isFullyAuto---", isOwner, isFullyAuto 
        sys.stdout.flush()         
        
        manuallyCreatedStatus = mutableProperties.get('manuallyCreatedStatus', {})
        manuallyCreated = manuallyCreatedStatus.get('values') # Should always be present
        print "CM-MANUALLY_CREATED:",manuallyCreatedStatus, manuallyCreated , '<<<'
        if manuallyCreated == 0 or manuallyCreated == False:
            for key in ['geometryAutomated', "motionAutomated", 'probTrendAutomated','automateAllButton', 'automateNoneButton', 'durationAutomated']:
                returnDict[key] = { 'editable' : editable }
            
        print "Other cases"
        print returnDict
        sys.stdout.flush()        
        # return immediately if modify button is clicked
        # or else fall through to block following
        if modifyButtonChosen:
            return returnDict

    ### The next 3 if statements are to immediately update respective fields
    ### if reautomated, per Greg.
        
    if triggerIdentifiers:
        convectTriggers = convectiveFilter(triggerIdentifiers, 'convective')
        convectMutables = convectiveFilter(mutableProperties, 'convective')
        
        if len(convectTriggers) > 0:
            
            ######################################################
            ## Graph Megawidget Algorithm Buttons
            ######################################################
            
            convectiveProbTrendTriggers = [x for x in convectTriggers if x.startswith('convectiveProbTrend') ]
            if len(convectiveProbTrendTriggers) > 0:
                probVals = mutableProperties['convectiveProbTrendGraph']['values']
                
                trigger = convectiveProbTrendTriggers[0]
    
                updatedProbTrend = updateProbtrend(probVals, trigger)
            
                ### Set new state of GMW with updated trend
                returnDict['convectiveProbTrendGraph']['values'] = updatedProbTrend
        
    return returnDict

def applyFLInterdependencies(triggerIdentifiers, mutableProperties):
    
    returnDict = {}

    # Get any changes required for the rise-crest-fall read-only text fields.
    ufnChanges = applyRiseCrestFallInterdependencies(triggerIdentifiers, mutableProperties)

    # Return None if no changes were needed for until-further-notice or for
    # impacts and crests; if changes were needed for only one of these,
    # return those changes; and if changes were needed for both, merge the
    # two dictionaries together and return the result.
    if ufnChanges is not None:
        returnDict.update(ufnChanges)
    
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
    propertyChanges = None
    return propertyChanges
                
def toBasis(eventType, eventTypeToBasis):   
    return eventTypeToBasis[eventType]

def writelines(file, lines):
    for line in lines:
        file.write(line)
    file.flush()
