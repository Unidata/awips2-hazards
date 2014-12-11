'''
    Description: Hazard Information Dialog Metadata for hazard type FL.W
'''
import CommonMetaData
from HazardConstants import *

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)

        if self.hazardStatus == "ending":
            metaData = [
                        self.getEndingSynopsis(),
                        ]
        else:
            pointDetails = [self.getPointID(),
                            self.getImmediateCause(),
                            self.getFloodSeverity(),
                            self.getFloodRecord(),
                            self.getRiseCrestFall()
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
                                    "pageName": "CTA",
                                    "pageFields": [
                                                   self.getCTAs(["stayTunedCTA"]),
                                                   # Preserving CAP defaults for future reference.
#                                                    self.getCAP_Fields([
#                                                                        ("urgency", "Expected"),
#                                                                        ("severity", "Severe"),
#                                                                        ("certainty", "Likely"),
#                                                                        ("responseType", "None"),
#                                                                        ]) 
                                                   ]
                                   }
                            ]
                     
                    }
               ]
        
        return {
                METADATA_KEY: metaData,
                EVENT_MODIFIERS_KEY: { "crestsApplyButton": "testScript" }
                }    


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
            "width":2,
            "pages": [{ "pageName": parm.capitalize() + " Search Parameters", "pageFields": [expandGroup]}]
        }
        
        fields = [expandBar,selectedPoints,fcstPoints]
        
        label = "Crest Comparison"
        if parm == "impacts":
            label = "Impacts Statement"
            fields.append(self.getImpactsTextField())
            
        
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
            "numColumns":2,
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
        from collections import OrderedDict
        
        crestOrImpact = "Crest"
        label = "\tCurObs\tMaxFcst\tFldStg\tFldFlow\t|\tPE\tBased on PE"
        
        ### FIXME: Get River from recommender or DB 
        river = "Potomac, Rappahannock, and Shena"
        ### FIXME: Get River from recommender or DB 
        point = "\tKITM2\t- Kitzmiller"
        
        ### FIXME: Get values from DB!!!
        values = OrderedDict()
        values['CurObs'] ='3.00'
        values['MaxFcst'] = '3.20'
        values['FldStg'] = '9.00'
        values['FldFlow'] = '10510.00'
        values['Lookup PE'] = 'HG'
        values['Based On Lookup PE'] = 'YES'

        riverLabel = {
                      "fieldType": "Label",
                      "fieldName": parm + "ForecastPointsRiverLabel",
                      "label": river,
                      "bold": True
                  }
        
        headerLabel = {
                       "fieldType": "Label",
                       "fieldName": parm + "ForecastPointsHeaderLabel",
                       "label": ('{:20s}'*len(values.keys())).format(*values.keys()),
                  }
        
        valuesLabel = {
                       "fieldType": "Label",
                       "fieldName": parm + "ForecastPointsValuesLabel",
                       "label": ('{:20s}'*len(values.values())).format(*values.values()),
                       }

        

        group = {
                 "fieldType": "Group",
                 "fieldName": parm + "ForecastPointsGoup",
                 "expandHorizontally": True,
                 "expandVertically": True,
                 "fields" : [riverLabel,headerLabel,valuesLabel]
                 
                 }

        return group
    
    
    
    def getSelectedPointsMods(self,parm):
        
        
        ### Get LookupPE
        lookupPE = self.getLookupPE(parm)
        
        ### Get Based on LookupPE
        basedOnLookupPE = self.getLookupPE(parm,True)
        
        mods = [lookupPE,basedOnLookupPE]
        ### !!! Impacts ONLY !!! Get Text for Stage/Flow  
        if parm == "impacts":
            textStageFlow = self.getTextForStageFlow(parm)
            mods.append(textStageFlow)
       
        selectedPointsMods = {
            "fieldName": parm + "Mods",
            "fieldType":"Group",
            "label": '',
            "expandHorizontally": True,
            "expandVertically": False,
            "numColumns": 3,
            "fields": mods
        }
        
        return selectedPointsMods
    
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
                        "minValue": -50, ### TODO: Find actual values used
                        "maxValue": -1,
                        "values": -10,
                        "expandHorizontally": True,
                        "showScale": True
                    } 
        
        

        
        return lookback

    def getApplyButton(self,parm):
        apply = {
                    "fieldType": "Button",
                    "fieldName": parm + "ApplyButton",
                    "label": "Apply Parameters"
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
                        "minValue": -10, ### TODO: Find actual values used
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
    Text for Stage/Flow
    """
    def getTextForStageFlow(self,parm):
        return {
                "fieldName": parm + "TextForStageFlowGroup",
                "fieldType":"Group",
                "label": "",
                "leftMargin": 5,
                "rightMargin": 5,
                "topMargin": 10,
                "bottomMargin": 10,
                "expandHorizontally": True,
                "expandVertically": False,
                "numColumns": 2,
                "fields": [ 
                            {
                                "fieldType": "Label",
                                "fieldName": parm + "TextForStageFlowLabel",
                                "label": "Text for Stage/Flow",
                            },
                            {
                                "fieldType": "Text",
                                "fieldName": parm + "ValueForStageFlowText",
                                "label": "",
                                ### FIXME: will need to be dynamic based on Imapcts/Stg Flow start/end selected
                                "values": "7.00",
                                "visibleChars": 5,
                                "lines": 1,
                                "expandHorizontally": True,
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
                "expandHorizontally": False,
                "expandVertically": False,
                "numColumns": 2,
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
        
        headerLabel = "Crest to Use"
        selectionLabel = "CrestStg/Flow :: CrestDate"
        
        ### FIXME: Values should be obtained from database via RiverForecastPoints
        choices = [
                       "16.50 :: 10/15/1954",
                       "14.85 :: 11/05/1985",
                       "13.40 :: 09/06/1996",
                       "12.88 :: 01/19/1996",
                       "12.80 :: 08/18/1955",
                    ]
        
        
        if parm == "impacts":
            headerLabel = "Impacts to Use"
            selectionLabel = "ImpactStg/Flow :: Start :: End :: Tendency"
            choices = [
                       "7.00 :: 01/01 :: 12/31 :: Rising",
                       "5.00 :: 01/01 :: 12/31 :: Rising",
            ]
        
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

        selectedForecastPoints = {
                "fieldType": "ComboBox",
                "fieldName": parm + "SelectedForecastPointsComboBox",
                "choices": choices,
                "expandHorizontally": True,
                "expandVertically": True
        }
        
        grp = {
            "fieldName": parm+"ImpactsPointsAndTextFieldGroup",
            "fieldType":"Group",
            "label": "",
            "expandHorizontally": True,
            "expandVertically": True,
            "fields": [ groupHeaderLabel,selectionHeaderLabel,selectedForecastPoints ]
            }
        
           
        return grp


    def getImpactsTextField(self):
        impactsTextField = {
                "fieldType": "Text",
                "fieldName": "impactsStringForStageFlowTextArea",
                "label": "",
                "values": " ENTERING TEXT FOR STAGE/FLOW HERE ",
                "visibleChars": 72,
                "lines": 5,
                "width": 2,
                "expandHorizontally": False,
            }
        
        return impactsTextField      
                
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

        
# Interdependency script entry point.
def applyInterdependencies(triggerIdentifiers, mutableProperties):

    # Get any changes required for fall-below until-further-notice interaction.
    ufnChanges = CommonMetaData.applyRiseCrestFallUntilFurtherNoticeInterdependencies(triggerIdentifiers, mutableProperties)

    parm = "impacts"
    ### FIXME: very specific hard coding here.  Need to make more flexible.
    triggerFieldName = parm + "SelectedForecastPointsComboBox"
    mutableFieldName = parm + "StringForStageFlowTextArea"
    
    ### For Impacts and Crests interaction
    impactsCrestsChanges = None
    if triggerIdentifiers is not None and triggerFieldName in triggerIdentifiers:
             
            if triggerFieldName in mutableProperties and "values" in mutableProperties[triggerFieldName]:
                line = mutableProperties[triggerFieldName]["values"]
                vals = filter(None,line.split('::'))
                impactsCrestsChanges = {
                                       "impactsStringForStageFlowTextArea": { "values" : vals[0] }
                                       }
    
    # Return None if no changes were needed for until-further-notice or for
    # impacts and crests; if changes were needed for only one of these,
    # return those changes; and if changes were needed for both, merge the
    # two dictionaries together and return the resut.            
    if ufnChanges == None:
        return impactsCrestsChanges
    elif impactsCrestsChanges == None:
        return ufnChanges
    else:
        impactsCrestsChanges.update(ufnChanges)
        return impactsCrestsChanges


# Sample event-modifying script entry point
#
# TODO: This is a testing script only; obviously we need something more
# useful here.
def testScript(hazardEvent, data):
    
    # Change point ID to an example value, to show it can be done.
    hazardEvent.addHazardAttribute("pointID", "DONE!");
    
    # Change the immediate cause to one of the new choice values from below.
    hazardEvent.addHazardAttribute("immediateCause", "Script");
    
    # Put together the mutable properties to be changed, again just to show
    # it can be done. The corresponding attribute is changed above to match
    # the new "values" value so that the hazard event is in sync with the
    # values available for immediate cause. 
    data = {
            "immediateCause": {
                               "choices": [ "Script", "Run", "Successfully" ],
                               "values": "Script"
                               }
            }
    
    # Return the two as a tuple.
    return (hazardEvent, data)

