'''
    Description: Product staging dialog metadata.
'''
import CommonMetaData
from HazardConstants import *
import collections, types
import ProductTextUtil
import json

class MetaData(CommonMetaData.MetaData):
    
    def execute(self, eventDicts=None, metaDict=None):        
        self._eventDicts = eventDicts
        productSegmentGroup = metaDict.get('productSegmentGroup')
        productLabel = productSegmentGroup.get('productLabel')
        geoType = productSegmentGroup.get('geoType')
        productParts = productSegmentGroup.get('productParts')
        productCategory = metaDict.get('productCategory')
        productID = productSegmentGroup.get('productID')
        eventIDs = productSegmentGroup.get('eventIDs')
        suffix = "_"+productLabel
                         
        # Set up initial values -- Use previous values from User Edited Text database if available
        for field in ['overviewSynopsis', 'callsToAction_productLevel']:
            exec field +'_value = ""'
            for eventID in eventIDs:
                textObjects =  ProductTextUtil.retrieveProductText(field+suffix, '', '', '', [eventID]) 
                if textObjects: 
                    value = self.as_str(json.loads(textObjects[0].getValue()))
                    exec field +'_value = value'
                    break
                    
        # Product level CTA's are only for the point-based hazards
        if geoType == 'point':
            ctas = [self.getCTAs(productLabel, callsToAction_productLevel_value)]
        else:
            if 'overviewSynopsis_area' not in productParts.get('partsList'):
                return {
                METADATA_KEY:[]
                }
            ctas = []

        label = 'Overview Synopsis for '+productLabel
        choices = self.getSynopsisChoices(productLabel)
        metaData = [
                    {
                     "fieldType": "Composite",
                     "fieldName": "overviewSynopsisContainer" + suffix,
                     "expandHorizontally": True,
                     "numColumns": 2,
                     "spacing": 5,
                     "fields": [
                                {
                                 "fieldType": "Label",
                                 "fieldName": "overviewSynopsisExplanation" + suffix,
                                 "label": label,
                                 },
                                {
                                 "fieldType": "MenuButton",
                                 "fieldName": "overviewSynopsisCanned" + suffix,
                                 "label": "Choose Canned Text",
                                 "choices": choices,
                                 }
                                ]
                     },
                    {
                     "fieldType": "Text",
                     "fieldName": 'overviewSynopsis' + suffix,
                     "visibleChars": 60,
                     "lines": 6,
                     "expandHorizontally": True,
                     "values": overviewSynopsis_value,
                     }
                    ] + ctas
        return {
                METADATA_KEY: metaData
                }
        
    def synopsisTempSnowMelt(self, productLabel):
        if productLabel.find('FFA')>=0:
            productString = "Warm temperatures may melt high mountain snowpack and increase river flows."
        else:
            productString = "Warm temperatures will melt high mountain snowpack and increase river flows."            
        return {"identifier":"tempSnowMelt",
                "displayString":"Warm Temperatures and Snow Melt",
                "productString": productString,
        }
    def synopsisDayNightTemps(self, productLabel):
        if productLabel.find('FFA')>=0:
            productString = "Warm daytime temperatures along with low temperatures " +\
                "remaining above freezing overnight may accelerate snow melt. River " +\
                "flows may increase quickly and remain high for the next week."
        else:
            productString = "Warm daytime temperatures along with low temperatures " +\
                "remaining above freezing overnight will accelerate snow melt. River " +\
                "flows will increase quickly and remain high for the next week."
            
        return {"identifier":"dayNightTemps",
                "displayString":"Warm day and night Temperatures",
                "productString":productString,
        }
    def synopsisSnowMeltReservoir(self, productLabel):
        if productLabel.find('FFA')>=0:
            productString = "High mountain snow melt and increased reservoir " +\
                "releases may cause the river flows to become high. Possible minor " +\
                "flooding downstream from the dam."
        else:
            productString = "High mountain snow melt and increased reservoir " +\
                "releases will cause the river flows to become high. Expect minor " +\
                "flooding downstream from the dam."           
        return {"identifier":"snowMeltReservoir",
                "displayString":"Snow melt and Reservoir releases",
                "productString":productString,
        }
    def synopsisRainOnSnow(self, productLabel):
        if productLabel.find('FFA')>=0:
            productString = "Heavy rain may fall on a deep primed snowpack leading " +\
                "to the melt increasing. Flows in Rivers may increase quickly and " +\
                "reach critical levels."
        else:
            productString = "Heavy rain will fall on a deep primed snowpack leading " +\
                "to the melt increasing. Flows in Rivers will increase quickly and " +\
                "reach critical levels."            
        return {"identifier":"rainOnSnow",
                "displayString":"Rain On Snow",
                "productString":productString,
        }
    def synopsisIceJam(self, productLabel):
        if productLabel.find('FFA')>=0:
            productString = "An ice jam may cause water to infiltrate the lowlands along the river."
        else:
            productString = "An ice jam will cause water to infiltrate the lowlands along the river."            
        return {"identifier":"iceJamFlooding",
                "displayString":"Ice Jam Flooding",
                "productString": productString,
        }
    def synopsisCategoryIncrease(self, productLabel):
        if productLabel.find('FFA')>=0:
            productString = "Heavy rainfall may increase the severity of flooding on the #riverName#."
        else:
            productString = "Heavy rainfall will increase the severity of flooding on the #riverName#."            
        return {"identifier":"categoryIncrease",
                "displayString":"Increase in Category",
                "productString": productString,
        }
        
    def getSynopsisChoices(self, productLabel):
        return [ 
                self.synopsisTempSnowMelt(productLabel),
                self.synopsisDayNightTemps(productLabel),
                self.synopsisSnowMeltReservoir(productLabel),
                self.synopsisRainOnSnow(productLabel),
                self.synopsisIceJam(productLabel),
                self.synopsisCategoryIncrease(productLabel)
                ]
      
    
    def getCTAs(self, productLabel, cta_value):
        values = cta_value
        if not values:
            if productLabel.find('FFA')>=0:
                values = ['safetyCTA']
            else:
                values = ["stayTunedCTA"]
        return {
                "fieldType":"CheckList",
                "label":"Calls to Action (1 or more):",
                "fieldName": "callsToAction_productLevel_"+productLabel,
                "values": values,
                "choices": self.getCTA_Choices(productLabel)
                }        
    def getCTA_Choices(self, productLabel):
        if productLabel.find('FAA')>=0:
            #FA.A (FAA) Does not have Calls To Action CTA Choices
            return [ ]
        elif productLabel.find('FFA')>=0:
            return [
                    self.ctaSafety(),
                    self.ctaStayAway(),
                    ]
        elif productLabel.find('advisory')>=0:
            return  [
                self.ctaFloodAdvisoryMeans(),
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
        else:
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
         
 
# Ensure that when the megawidgets are initialized and then subsequently whenever the
# user chooses a new choice from the canned synopses dropdown, the text in the synopsis
# text area is changed to match.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    if triggerIdentifiers is not None: 	
	# See if the trigger identifiers list contains any of the identifiers from the
	# synopsis canned choices; if so, change the text's value to match the associated
	# canned choice text.
         
        def extractParts(identifier):
            s1 = identifier.rsplit('.',1)
            choice = s1[1]
            triggerField = s1[0]
            s2 = s1[0].split('_',1)
            productLabel = s2[1]
            return productLabel, choice, triggerField
        returnDict = {}
        for triggerIdentifier in triggerIdentifiers:
            # Example: triggerIdentifier:  overviewSynopsisCanned_FLW_area_14550_FA.W.dayNightTemps
            if triggerIdentifier.find('overviewSynopsisCanned') >= 0:
                productLabel, choice, triggerField = extractParts(triggerIdentifier)
                # Find productString for choice
                choices = mutableProperties.get(triggerField).get('choices')
                for choiceDict in choices:
                    if choiceDict.get('identifier') == choice:
                        productString = choiceDict.get('productString')
                changeField = 'overviewSynopsis_'+productLabel

                returnDict[changeField] =  {
                  "values": productString
                }  
        return returnDict
    return None

