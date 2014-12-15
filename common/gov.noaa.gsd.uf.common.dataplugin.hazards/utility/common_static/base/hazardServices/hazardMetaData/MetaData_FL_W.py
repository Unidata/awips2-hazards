import CommonMetaData
import datetime
import json
from HazardConstants import *
from RiverForecastPoints import RiverForecastPoints
from com.raytheon.uf.common.time import SimulatedTime
import sys
from collections import OrderedDict


class MetaData(CommonMetaData.MetaData):
    
    _basedOnLookupPE = '{:15s}'.format('YES')
    
    def execute(self, hazardEvent=None, metaDict=None):

        self.initialize(hazardEvent, metaDict)

        millis = SimulatedTime.getSystemTime().getMillis()
        currentTime = datetime.datetime.fromtimestamp(millis / 1000)
        self._rfp = RiverForecastPoints(currentTime)

        if self.hazardStatus == "ending":
            metaData = [
                        self.getEndingSynopsis(),
                        ]
        else:
            pointDetails = [self.getPointID(),
                            self.getImmediateCause(),
                            self.getFloodSeverity(),
                            self.getFloodRecord(),
                            self.getRiseCrestFall(),
                            self.getHiddenFallLastInterval()
                            ]
         
            crests = [self.getCrestsOrImpacts("crests")]
             
            impacts = [self.getCrestsOrImpacts("impacts")]
            
            metaData = [
                           {
                    "fieldType": "TabbedComposite",
                    "fieldName": "FLWTabbedComposite",
                    "leftMargin": 10,
                    "rightMargin": 10,
                    "topMargin": 10,
                    "bottomMargin": 10,
                    "expandHorizontally": True,
                    "expandVertically": True,
                    "pages": [
                                  {
                                    "pageName": "Point Details",
                                    "pageFields": pointDetails
                                   },
                                  {
                                    "pageName": "Crest Comparison",
                                    "pageFields": crests
                                   },
                                  {
                                    "pageName": "Impacts Statement",
                                    "pageFields": impacts
                                   },
                                  {
                                    "pageName": "CTA and CAP",
                                    "pageFields": [
                                                   self.getCTAs(["stayTunedCTA"]),
                                                   # Preserving CAP defaults for future reference.
                                                   #self.getCAP_Fields([
                                                   #                    ("urgency", "Expected"),
                                                   #                    ("severity", "Severe"),
                                                   #                    ("certainty", "Likely"),
                                                   #                    ("responseType", "None"),
                                                   #                    ]) 
                                                   ]
                                   }
                            ]
                     
                    }
               ]


        retStuff = {
                METADATA_KEY: metaData,
                } 

        return retStuff


    def getCrestsOrImpacts(self,parm):
        
        ### Get Search Fields Section
        searchFields = self.getSearchFieldsSection(parm)
        
        ### Get Forecast Points Section
        fcstPoints = self.getForecastPointsSection(parm)
        
        ### Get Selected Points Section
        selectedPoints = self.getSelectedForecastPoints(parm)
        
        ### Want to have Search Params to expand, but as one group
        ### This will allow us to add the Selected Points Section 
        ### at the end to be always visible
        
        expandGroup = {
            "fieldName": parm + "ExpandGroup",
            "fieldType":"Group",
            "label": "",
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": True,
            "expandVertically": False,
            "fields": [searchFields]
        }

        expandBar =  {
            "fieldName": parm + "ExpandBar",
            "fieldType":"ExpandBar",
            "label": "",
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": True,
            "expandVertically": False,
            "pages": [{ "pageName": parm.capitalize() + " Search Parameters", "pageFields": [expandGroup]}]
        }
        
        fields = [expandBar,fcstPoints,selectedPoints]
        
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
            "expandHorizontally": True,
            "expandVertically": False,
            "fields": fields
        }

        return field


    def getSearchFieldsSection(self, parm):
        
        ### Get Ref/Stg Flow Combo
        refStageFlow = self.getRefStgFlow(parm)
        
        ### Get Stg Window sliders
        ### Get Max Depth Sliders
        ### Get FlowWindow Slides
        ### Get Max Offset slider
        stgWindow = self.getStageWindow(parm)
        
        
        ### Get Search Type
        searchType = self.getSearchType(parm)
        
        ### Get Apply Button
        apply = self.getApplyButton(parm)
        
        ### Group to hold all widgets
        searchFields = {
            "fieldName": parm + "Compare",
            "fieldType":"Group",
            "label": parm.capitalize() + " Search Parameters:",
            "leftMargin": 10,
            "rightMargin": 10,
            "topMargin": 10,
            "bottomMargin": 10,
            "expandHorizontally": True,
            "expandVertically": False,
            "fields": [refStageFlow,stgWindow,searchType,apply]
        }
        
        
        ### !!!! CREST ONLY !!! Get Year Lookback slider  ###
        if parm == "crests":
            lookback = self.getYearLookbackSlider(parm)
            searchFields["fields"].insert(2,lookback)
        
        return searchFields
    
    

    def getForecastPointsSection(self,parm):
        millis = SimulatedTime.getSystemTime().getMillis()
        currentTime = datetime.datetime.fromtimestamp(millis / 1000)
        pointID = self.hazardEvent.get("pointID")
        
        PE = self._rfp.getPrimaryPhysicalElement(pointID)
        riverPointID = self._rfp.getRiverPointIdentifier(pointID)
        riverName = self._rfp.getRiverPointName(pointID)
        curObs = self._rfp.getObservedLevel(pointID)
        if isinstance(curObs, (tuple,list)):
            curObs = curObs[0]
        maxFcst = self._rfp.getMaximumForecastLevel(pointID, PE)
        if isinstance(maxFcst, (tuple,list)):
            maxFcst = maxFcst[0]
        (fldStg, fldFlow) = self._rfp.getFloodLevels(pointID)

        crestOrImpact = "Crest"
        label = "\tCurObs\tMaxFcst\tFldStg\tFldFlow\t|\tPE\tBased on PE"
        
        riverGrp = self._rfp.getGroupName(pointID) 
        
        point = "\t" + riverPointID + "\t- " + riverName

        values = OrderedDict()
        values['CurObs'] = '{:<15.2f}'.format(curObs)
        values['MaxFcst'] = '{:<15.2f}'.format(maxFcst)
        values['FldStg'] = '{:<15.2f}'.format(fldStg)
        values['FldFlow'] = '{:<15.2f}'.format(fldFlow)
        values['Lookup PE'] = '{:15s}'.format(PE)
        values['Based On Lookup PE'] = self._basedOnLookupPE
        
        riverLabel = {
                      "fieldType": "Label",
                      "fieldName": parm + "ForecastPointsRiverLabel",
                      "label": riverGrp,
                      "bold": True
                  }
        
        headerLabel = {
                       "fieldType": "Label",
                       "fieldName": parm + "ForecastPointsHeaderLabel",
                       "label": ('{:15s}'*len(values.keys())).format(*values.keys()),
                  }
        
        valuesLabel = {
                       "fieldType": "Label",
                       "fieldName": parm + "ForecastPointsValuesLabel",
                       "label": ''.join(values.values()),
                       }

        

        group = {
                 "fieldType": "Group",
                 "fieldName": parm + "ForecastPointsGoup",
                 "expandHorizontally": True,
                 "expandVertically": True,
                 "fields" : [riverLabel,headerLabel,valuesLabel]
                 
                 }

        return group
    
    
    def getRefStgFlow(self, parm):
        refStageFlow = {
                            "fieldType": "ComboBox",
                            "fieldName": parm + "ReferenceStageFlow",
                            "label": "Reference Stage Flow",
                            "choices": ["Current Observed", "Max Forecast", "Current Obs/Max Fcst"],
                            "values": "Current Observed",
                            "expandHorizontally": True 
                        }
        return refStageFlow


    def getYearLookbackSlider(self,parm):
        lookback = {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "YearLookbackSpinner",
                        "label": "Year Lookback",
                        "minValue": -150,
                        "maxValue": -1,
                        "values": -10,
                        "expandHorizontally": True,
                        "showScale": True
                    } 
        return lookback

    def getApplyButton(self,parm):
        apply = {
                    "fieldType": "CheckBox",
                    "fieldName": parm + "ApplyButton",
                    "label": "Toggle to Apply Parameters",
                     "refreshMetadata": True
                }
        return apply
            
        
    # Search Type Dropdown (specific to 'crest' or 'impacts')
    def getSearchType(self,parm):
        choices = ["Recent in Stage/Flow, Year Window",
                   "Closest in Stage/Flow, Year Window",
                   "Recent in Stage/Flow Window",
                   "Closest in Stage/Flow Window","Highest in Stage/Flow Window"
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
               "label": "Search Type",
               "choices": choices,
               "values": values,
               "expandHorizontally": True 
            }
        
    # Stage Widow search criteria widgets (sliders and text fields)
    def getStageWindow(self,parm,low=-4,hi=4):
        return {
            "fieldName": parm + "stageWindowGroup",
            "fieldType":"Group",
            "leftMargin": 5,
            "rightMargin": 5,
            "topMargin": 5,
            "bottomMargin": 5,
            "numColumns": 2,
            "expandHorizontally": True,
            "expandVertically": False,
            "fields": [
                    {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "StageWindowSpinnerLow",
                        "label": "",
                        "minValue": low,
                        "maxValue": 0,
                        "values": low+1,
                        "expandHorizontally": True,
                        "showScale": True
                    },
                    {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "StageWindowSpinnerHi",
                        "label": "",
                        "minValue": 0,
                        "maxValue": hi,
                        "values": hi-1,
                        "expandHorizontally": True,
                        "showScale": True
                    },
                    {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "maxDepthBelowFloodStage",
                        "label": "Maximum Depth Below Flood Stage",
                        "minValue": -10, 
                        "maxValue": 0,
                        "values": -3,
                        "width": 2,
                        "expandHorizontally": True,
                        "showScale": True
                    },
                    {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "FlowWindow1",
                        "label": "Flow Window (%):   (0-100)",
                        "minValue": 0,
                        "maxValue": 100,
                        "values": 10,
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
                        "expandHorizontally": True,
                        "showScale": False
                    },
                    {
                        "fieldType": "IntegerSpinner",
                        "fieldName": parm + "MaxOffsetBelowFloodFlow",
                        "label": "Max Offset below Flood Flow (%):",
                        "minValue": 0,
                        "maxValue": 100,
                        "values": 10,
                        "width": 2,
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
    def getLookupPE(self,parm,basedOn=False):
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
                "expandHorizontally": True,
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
                                "expandHorizontally": True,
                            }
                        ]
        }


    """
    Create a radio button list for user to select the "Settings for Selected Forecast Point"
    """        
    def getSelectedForecastPoints(self,parm):
        
        filters = {}
        if self.hazardEvent.get(parm+"ReferenceStageFlow"):
            filters = scrapeSearchParms(self.hazardEvent, parm)
            
            
        else:
            filters['Reference Type'] = self.getRefStgFlow(parm)['values']
            filters['Search Type'] = self.getSearchType(parm)['values']
            
            stageWindowFields = self.getStageWindow(parm)['fields']
            swfDict = {}
            for k in  stageWindowFields:
                swfDict[k['fieldName']] = k['values']
            
            filters['Stage Window Lower'] = swfDict[parm+"StageWindowSpinnerLow"]
            filters['Stage Window Upper'] = swfDict[parm+"StageWindowSpinnerHi"]
            filters['Depth Below Flood Stage'] = swfDict[parm+"maxDepthBelowFloodStage"]
            filters['Flow Window Lower'] = swfDict[parm+"FlowWindow1"]
            filters['Flow Window Upper'] = swfDict[parm+"FlowWindow2"]
            filters['Flow Stage Window'] = swfDict[parm+"MaxOffsetBelowFloodFlow"]
            
            
            if parm == 'crests':
                filters['Year Lookback'] = self.getYearLookbackSlider(parm)['values']
        
        pointID = self.hazardEvent.get("pointID")
        
        impactsTextField = None
        if parm == "impacts":
            headerLabel = "Impacts to Use"
            selectionLabel = "ImpactStg/Flow - Start - End - Tendency"
            characterizations, descriptions = self._rfp.getImpacts(pointID, filters)
            charDescDict = dict(zip(characterizations, descriptions))
            impactChoices, values = self._makeImpactsChoices(charDescDict)
            
            selectedForecastPoints = {
                                      "fieldType":"CheckBoxes",
                                      "fieldName": "impactCheckBoxes",
                                      "label": "Impacts",
                                      "choices": impactChoices,
                                      "values" : values,
                                      "extraData" : { "origList" : values },
                                      }
                
        else:
            headerLabel = "Crest to Use"
            selectionLabel = "CrestStg/Flow - CrestDate"
            defCrest, crestList = self._rfp.getHistoricalCrest(pointID, filters)
            choices = crestList
            value = defCrest
            selectedForecastPoints = {
                    "fieldType": "ComboBox",
                    "fieldName": parm + "SelectedForecastPointsComboBox",
                    "choices": choices,
                    "values": value,
                    "expandHorizontally": True,
                    "expandVertically": True
            }
            
        
        groupHeaderLabel  = {
                       
                       "fieldType": "Label",
                       "fieldName": parm+"GroupForecastPointsLabel",
                       "leftMargin": 40,
                       "rightMargin": 10,
                       "label": headerLabel,
                       
                       }

        selectionHeaderLabel = {
                       
                       "fieldType": "Label",
                       "fieldName": parm+"SelectedForecastPointsLabel",
                       "label": selectionLabel,
                       
                       }
        
        fields = [ groupHeaderLabel,selectionHeaderLabel,selectedForecastPoints ]
        
        grp = {
            "fieldName": parm+"PointsAndTextFieldGroup",
            "fieldType":"Group",
            "label": "",
            "expandHorizontally": True,
            "expandVertically": True,
            "fields": fields
            }
        
        return grp

    # BASIS
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
        
                
    def getCTA_Choices(self):
        return [
            self.ctaFloodWarningMeans(),
            self.ctaDoNotDrive(),
            self.ctaRiverBanks(),
            self.ctaTurnAround(),
            self.ctaStayTuned(),
            self.ctaNightTime(),
            self.ctaAutoSafety(),
            self.ctaRisingWater(),
            self.ctaForceOfWater(),
            self.ctaLastStatement(),
            self.ctaWarningInEffect(),
            self.ctaReportFlooding(),
            ]

    def _makeImpactsChoices(self, charDescDict):
        choices = []
        values = []
        
        sortedCharsAsKeys = sorted(charDescDict.keys())
        
        for char in sortedCharsAsKeys:
            id = "impactCheckBox_"+str(char)
            desc = charDescDict.get(char)
            entry = {
                     "identifier": id,
                     "displayString":str(char) ,
                    "detailFields": [ 
                                     {
                                     "fieldType": "Text",
                                     "fieldName": "impactTextField_"+str(char),
                                     "expandHorizontally": False,
                                     "visibleChars": 35,
                                     "lines":2,
                                     "values": desc,
                                     "enabled": True
                                     }
                                   ]
                     }
            choices.append(entry)
            values.append(id)
        return choices, values
        
        
# Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):

    # Get any changes required for fall-below until-further-notice interaction.
    ufnChanges = CommonMetaData.applyRiseCrestFallUntilFurtherNoticeInterdependencies(triggerIdentifiers, mutableProperties)

    ### originalList is used in multiple cases.  Assign it only once
    oListTemp = mutableProperties.get("impactCheckBoxes")
    if oListTemp:
        originalList = oListTemp['extraData']['origList']
    else:
        originalList = None
    

    ### For Impacts and Crests interaction
    impactsCrestsChanges = None
    if triggerIdentifiers is not None:# and any(ti.startswith('impactCheckBox_') for ti in mutableProperties):
        impactsCrestsChanges = {}

        if originalList:
            currentVals = mutableProperties["impactCheckBoxes"]['values']
            textFields = [tf for tf in mutableProperties if tf.startswith('impactTextField_')]
            
            for tf in textFields:
                impactsCrestsChanges[tf] = { "enable" : True}
            
            offCheckList = list(set(originalList).difference(currentVals))
    
            for off in offCheckList:
                offText = 'impactTextField_'+off.split('_')[-1]
                impactsCrestsChanges[offText] = { "enable" : False}
        
        
    if triggerIdentifiers is None:
        impactsCrestsChanges = {}
        if originalList:
            impactsCrestsChanges["impactCheckBoxes"] = { "values": originalList }

    # Return None if no changes were needed for until-further-notice or for
    # impacts and crests; if changes were needed for only one of these,
    # return those changes; and if changes were needed for both, merge the
    # two dictionaries together and return the result.
    if ufnChanges == None:
        return impactsCrestsChanges
    elif impactsCrestsChanges == None:
        return ufnChanges
    else:
        impactsCrestsChanges.update(ufnChanges)
        return impactsCrestsChanges

def scrapeSearchParms(hazardEvent, parm):
    filters = {}
    filters['Reference Type'] = hazardEvent.get(parm+"ReferenceStageFlow")
    filters['Stage Window Lower'] = hazardEvent.get(parm+"StageWindowSpinnerLow")
    filters['Stage Window Upper'] = hazardEvent.get(parm+"StageWindowSpinnerHi")
    filters['Depth Below Flood Stage'] = hazardEvent.get(parm+"maxDepthBelowFloodStage")
    filters['Flow Window Lower'] = hazardEvent.get(parm+"FlowWindow1")
    filters['Flow Window Upper'] = hazardEvent.get(parm+"FlowWindow2")
    filters['Flow Stage Window'] = hazardEvent.get(parm+"MaxOffsetBelowFloodFlow")
    filters['Search Type'] = hazardEvent.get(parm+"SearchType")
    if parm == 'crests':
        filters['Year Lookback'] = hazardEvent.get(parm+"YearLookbackSpinner")
    return filters

