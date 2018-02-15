import CommonMetaData
from HazardConstants import *
import sys
import traceback

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, hazardEvent=None, metaDict=None):
        self.initialize(hazardEvent, metaDict)

        if hazardEvent is not None:
            self.burnScarName = hazardEvent.get('burnScarName')
            from MapsDatabaseAccessor import MapsDatabaseAccessor
            mapsAccessor = MapsDatabaseAccessor()
            self.burnScarMetaData = \
              mapsAccessor.getBurnScarMetadata(self.burnScarName)

        if self.hazardStatus == "ending":
            metaData = [
                        self.getEndingOption(),
                        ]
        else:
           metaData = [
                     self.getInclude(),
                     self.setBurnScarNameLabel(self.hazardEvent),
                     self.getImmediateCause(),
                     self.getSource(),
                     self.getEventType(),
                     self.getFlashFloodOccurring(),
                     self.getDebrisFlowOptions(),
                     self.getRainAmt(),
                     self.getAdditionalInfo(),
                     self.getScenario(),
                     self.getLocationsAffected(),
                     self.getCTAs(), 
                    ]

        return {
                METADATA_KEY: metaData
                }    
       
    def immediateCauseChoices(self):
        return [
                self.immediateCauseER(),
                self.immediateCauseRS(),
            ]
 
    def setBurnScarNameLabel(self, hazardEvent):
        attrs = hazardEvent.getHazardAttributes()
        bsName = attrs.get('burnScarName')
       
        enabled = False
        edit = False
        if self.hazardStatus == 'pending':
            enabled = True
            edit = True
        
        label = {
            "fieldName": "burnScarName",
            "fieldType":"Text",
            "values": bsName,
            "valueIfEmpty": "|* Enter Burn Scar or Location *|",
            "promptText": "Enter burn scar or location",
            "visibleChars": 40,
            'enable': enabled,
            'editable': edit,
            "bold": True,
            "italic": True
                }  
        
        group = {
                    "fieldType": "Group",
                    "fieldName": "burnScarGroup",
                    "label": "BurnScar: ",
                    "leftMargin": 10,
                    "rightMargin": 10,
                    "topMargin": 10,
                    "bottomMargin": 10,
                    "expandHorizontally": True,
                    "expandVertically": True,
                    "fields": [label]
                }
        
        
        return group
        
 
    def includeChoices(self):
        return [
            self.includeEmergency(),
            ]
        
    def getSourceChoices(self):
        return [
            self.dopplerSource(),
            self.dopplerGaugesSource(),
            self.trainedSpottersSource(),
            self.publicSource(),
            self.localLawEnforcementSource(),
            self.emergencyManagementSource(),
            self.satelliteSource(),
            self.gaugesSource(),
                    ]

    def getEventTypeChoices(self):
        return [
                self.eventTypeThunder(),
                self.eventTypeRain(),
                ]

    def getFlashFloodOccurring(self, defaultOn=False):
        return {
             "fieldType":"CheckBox",
             "fieldName": "flashFlood",
             "label": "Flash flooding occurring",
             "value": defaultOn,
            }

    def getDebrisFlowOptions(self):
        choices = [
                   self.debrisFlowBurnScar(),
                   self.debrisFlowMudSlide(),
                   self.debrisFlowUnknown(),
                   ]
        return {
                 "fieldType":"RadioButtons",
                 "fieldName": "debrisFlow",
                 "label": "Debris Flow Info:",
                 "choices": choices,
                 "values":choices[0]["identifier"]
                }        

    def debrisFlowUnknown(self):
        return {"identifier":"debrisFlowUnknown",
                "displayString": "Unknown"
        }
 
    def debrisFlowBurnScar(self):
        return {"identifier":"debrisFlowBurnScar", 
                "displayString": "Burn scar area with debris flow", 
                "detailFields": [
                            {
                             "fieldType": "Text",
                             "fieldName": "debrisBurnScarDrainage",
                             "expandHorizontally": True,
                             "visibleChars": 12,
                             "promptText": "Enter drainage",
                            }]
               }
    def debrisFlowMudSlide(self):
        return {"identifier":"debrisFlowMudSlide",
                "displayString": "Mud Slides"
		}

    def additionalInfoChoices(self):
        return [ 
            self.listOfDrainages(),
            self.additionalRain(),
            ]

    def includeEmergency(self):
        # Pick up existing emergency headline metadata from base class
        basemeta = super(MetaData ,self).includeEmergency()

        # Attempt to get the information we need to modify this.
        try :
            emergencyText = self.burnScarMetaData["emergencyHeadline"]
        except :
            if not hasattr(self, 'burnScarName') :
                return basemeta
            if not isinstance(self.burnScarName, str) :
                return basemeta
            burnlo = self.burnScarName.lower()
            if burnlo.find(' area')<0 and burnlo.find(' burn')<0 and \
               burnlo.find(' scar')<0 :
                emergencyText = self.burnScarName + " burn area including |*Location*|"
            else :
                emergencyText = self.burnScarName + " including |*Location*|"

        # Remove the prompt and set a value that is the emergencyHeadline string.
        # Catch errors here because it is theoretically possible for an override
        # to make this operation fail.
        try :
            fieldDict = basemeta["detailFields"][0]["fields"][0]
            if fieldDict["maxChars"] < len(emergencyText) :
                fieldDict["maxChars"] = len(emergencyText)
            del fieldDict["promptText"]
            fieldDict["values"] = emergencyText
        except :
            tb = traceback.format_stack()
            sys.stderr.write( \
             "\nUNEXPECTED CONDITION!!! No 'promptText' for emergency checkbutton\n")
            for tbentry in tb[:-1] :
                 sys.stderr.write(tbentry)
            sys.stderr.write(tb[-1].split('\n')[0]+"\n\n")
        return basemeta

    def getScenario(self):
        return {
            "fieldName": "scenario",
            "fieldType":"ComboBox",
            "label":"Scenario:",
            "choices": self.scenarioChoices(),
            }

    def scenarioChoices(self):
        scenarioDict = None
        if self.burnScarMetaData != None :
            scenarioDict = self.burnScarMetaData.get("scenarios")
        if scenarioDict == None :
            # Hard code some defaults just in case
            return [
                    {"identifier": "no_scenario", "displayString": "None",
                     "productString": "|* Enter scenario text. *|" }
                    ]
        scenarioList = []
        for scenarioId in scenarioDict.keys() :
            scenarioList.append( \
                {"identifier": scenarioId, \
                 "displayString": scenarioDict[scenarioId]["displayString"], \
                 "productString": scenarioDict[scenarioId]["productString"]} )
        return scenarioList

    def getCTAs(self,values=None):
        basemeta = super(MetaData ,self).getCTAs(values)

        # If values is not null, then we are not initializing metadata new
        # from scratch and we should just respect those settings.
        if values != None :
            return basemeta

        try:
            basemeta["pages"][0]["pageFields"][0]['values'].append("actQuicklyCTA")
        except:
            try:
                basemeta["pages"][0]["pageFields"][0]['values']=["actQuicklyCTA"]
            except:
                pass

        if self.burnScarMetaData == None :
            return basemeta
        
        try:
            basemeta["pages"][0]["pageFields"][0]['values'].append("burnScarScenarioCTA")
        except:
            try:
                basemeta["pages"][0]["pageFields"][0]['values']=["burnScarScenarioCTA"]
            except:
                pass

        return basemeta

    def getCTA_Choices(self):
        return [
            self.ctaFFWEmergency(),
            self.ctaBurnScarScenario(),
            self.ctaBurnAreas(),
            self.ctaTurnAround(),
            self.ctaActQuickly(),
            self.ctaChildSafety(),
            self.ctaNightTime(),
            self.ctaUrbanFlooding(),
            self.ctaRuralFlooding(),
            self.ctaStayAway(),
            self.ctaLowSpots(),
            self.ctaArroyos(),
            self.ctaCamperSafety(),
            self.ctaReportFlooding(),
            self.ctaFlashFloodWarningMeans(),
            ]

    def ctaBurnScarScenario(self) :
        # tagstr must match same in Legacy_FFW_FFS_Formatter:_callsToAction_sectionLevel()
        tagstr = "|*Burn-Scar*|"
        if self.burnScarName:
            dspStr = self.burnScarName
        else:
            dspStr = "For scenario"
        prdStr = "This is a life threatening situation.  Heavy rainfall will cause "+\
                 "extensive and severe flash flooding of creeks...streams...and ditches "+\
                 "in the "+tagstr+". Severe debris flows can also be anticipated "+\
                 "across roads.  Roads and driveways may be washed away in places. "+\
                 "If you encounter flood waters...climb to safety."
        return {"identifier": "burnScarScenarioCTA",
                "displayString": dspStr,
                "productString": prdStr }

    def CAP_WEA_Values(self):
        if self.hazardStatus == "pending":
           return ["WEA_activated"]
        else:
           return [] 

    def CAP_WEA_Text(self):
        return "Flash Flood Warning this area til %s. Avoid flooded areas. Check local media. -NWS"
    
    def endingOptionChoices(self):
        return [
            self.recedingWater(),
            self.rainEnded(),
            ]

    def validate(self,hazardEvent):
        message1 = self.validateRainSoFar(hazardEvent)
        message2 = self.validateAdditionalRain(hazardEvent,checkStatus=True)
        message3 = self.validateLocation(hazardEvent)
        retmsg = None
        if message1:
            retmsg = message1
        if message2:
            if retmsg:
                retmsg += "\n\n" + message2
            else:
                retmsg = message2
        if message3:
            if retmsg:
                retmsg += "\n\n" + message3
            else:
                retmsg = message3
        return retmsg

    def getLocationsAffected(self):
        # False means don't have pathcast be default Haz Inf Dialog choice
        return super(MetaData ,self).getLocationsAffected(False)

def applyInterdependencies(triggerIdentifiers, mutableProperties):
    propertyChanges = CommonMetaData.applyInterdependencies(triggerIdentifiers, mutableProperties)
    return propertyChanges

