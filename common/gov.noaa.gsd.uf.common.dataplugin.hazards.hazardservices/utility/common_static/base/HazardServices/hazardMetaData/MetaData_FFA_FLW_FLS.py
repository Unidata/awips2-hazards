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
        prevProductLabel = eventDicts[0].get('productLabel', None)
        geoType = productSegmentGroup.get('geoType')
        productParts = productSegmentGroup.get('productParts')
        productCategory = metaDict.get('productCategory')
        productID = productSegmentGroup.get('productID')
        eventIDs = productSegmentGroup.get('eventIDs')
        suffix = "_"+productLabel
        self.immediateCauses = set()
        self.allCAN = True
        if self._eventDicts:
            for eventDict in self._eventDicts:
                self.immediateCauses.add(eventDict.get('immediateCause', 'ER'))
                if eventDict.get('status') != 'ENDING':
                    self.allCAN = False
                    break
                    
        # Product level CTA's are only for the point-based hazards
        ctas = {}
        if geoType == 'point':
            if not self.allCAN:
                previousValues = set()
                for hazard in self._eventDicts:
                    tempVals = self.getPreviousStagingValue(hazard, "callsToAction_productLevel", productLabel, prevProductLabel)
                    if tempVals:
                        previousValues.update(tempVals)
                ctas = self.getCTAs(productLabel, previousValues)
        else:
            if 'overviewSynopsis_area' not in [part.getName() for part in productParts]:
                return {
                METADATA_KEY:[]
                }

        metaData = []

        # Only add the overview if there is canned choices
        choices = self.getSynopsisChoices(productLabel)
        choiceIdentifiers = set([choice["identifier"] for choice in choices])
        if choices:
            
            # Determine if there is a previous value to use.
            previousValues = set()
            for hazard in self._eventDicts:
                previousValue = self.getPreviousStagingValue(hazard, "overviewSynopsisCanned", productLabel, prevProductLabel)
                if previousValue:
                    previousValues.add(previousValue)

            # Use the previous value if one is found and if it
            # is valid (i.e. it is found within the current set
            # of choices). 
            prevValue = None
            if previousValues:
                prevValue = list(previousValues)[0]
                if prevValue not in choiceIdentifiers:
                    prevValue = None
                    
            overview = {
                    "fieldType": "ComboBox",
                    "fieldName": "overviewSynopsisCanned" + suffix,
                    "label": "Overview Synopsis for " + productLabel,
                    "choices": choices,
                    "values" : prevValue,
                    }
            metaData.append(overview)

        if ctas:
            metaData.append(ctas)
        return {
            METADATA_KEY: metaData
            }

    def synopsisBlank(self):
        return {"identifier":"blankSynopsis",
                "displayString":"",
                "productString": "",
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
            productString = "Heavy rainfall may increase the severity of flooding on the |* riverName *|."
        else:
            productString = "Heavy rainfall will increase the severity of flooding on the |* riverName *|."
        return {"identifier":"categoryIncrease",
                "displayString":"Increase in Category",
                "productString": productString,
        }

    def getSynopsisChoices(self, productLabel):
        # No options make sense for CANs
        if self.allCAN:
            return []
        choices = [ 
                    self.synopsisBlank(),
                    self.synopsisTempSnowMelt(productLabel),
                    self.synopsisDayNightTemps(productLabel),
                    self.synopsisSnowMeltReservoir(productLabel),
                    self.synopsisRainOnSnow(productLabel),
                    self.synopsisIceJam(productLabel),
                    self.synopsisCategoryIncrease(productLabel)
                ]

        identifiers = set()
        for immediateCause in self.immediateCauses:
            if immediateCause == 'SM':
                identifiers.add(self.synopsisTempSnowMelt(productLabel).get('identifier'))
                identifiers.add(self.synopsisDayNightTemps(productLabel).get('identifier'))
                identifiers.add(self.synopsisSnowMeltReservoir(productLabel).get('identifier'))
                identifiers.add(self.synopsisCategoryIncrease(productLabel).get('identifier'))
            elif immediateCause == 'RS':
                identifiers.add(self.synopsisRainOnSnow(productLabel).get('identifier'))
            elif immediateCause == 'IC':
                identifiers.add(self.synopsisTempSnowMelt(productLabel).get('identifier'))
                identifiers.add(self.synopsisDayNightTemps(productLabel).get('identifier'))
                identifiers.add(self.synopsisSnowMeltReservoir(productLabel).get('identifier'))
                identifiers.add(self.synopsisRainOnSnow(productLabel).get('identifier'))
                identifiers.add(self.synopsisIceJam(productLabel).get('identifier'))
                identifiers.add(self.synopsisCategoryIncrease(productLabel).get('identifier'))
            elif immediateCause == 'IJ':
                identifiers.add(self.synopsisIceJam(productLabel).get('identifier'))
            elif immediateCause == 'MC':
                identifiers.add(self.synopsisTempSnowMelt(productLabel).get('identifier'))
                identifiers.add(self.synopsisDayNightTemps(productLabel).get('identifier'))
                identifiers.add(self.synopsisSnowMeltReservoir(productLabel).get('identifier'))
                identifiers.add(self.synopsisRainOnSnow(productLabel).get('identifier'))
                identifiers.add(self.synopsisIceJam(productLabel).get('identifier'))
                identifiers.add(self.synopsisCategoryIncrease(productLabel).get('identifier'))
            elif immediateCause == 'UU':
                identifiers.add(self.synopsisTempSnowMelt(productLabel).get('identifier'))
                identifiers.add(self.synopsisDayNightTemps(productLabel).get('identifier'))
                identifiers.add(self.synopsisSnowMeltReservoir(productLabel).get('identifier'))
                identifiers.add(self.synopsisRainOnSnow(productLabel).get('identifier'))
                identifiers.add(self.synopsisIceJam(productLabel).get('identifier'))
                identifiers.add(self.synopsisCategoryIncrease(productLabel).get('identifier'))
            elif immediateCause == 'DR':
                identifiers.add(self.synopsisSnowMeltReservoir(productLabel).get('identifier'))

        availableChoices = []
        for identifier in identifiers:
            for choice in choices:
                if choice.get('identifier') == identifier:
                    availableChoices.append(choice)
                    break

        if availableChoices:
            # Give a option for no canned text.
            availableChoices.insert(0, self.synopsisBlank())
        return availableChoices

    def getCTAs(self, productLabel, cta_value):
        
        # Ensure that there are some values chosen.
        values = cta_value
        if not values:
            if productLabel.find('FFA')>=0:
                values = ['safetyCTA']
            else:
                values = ["turnAroundCTA"]
                
        # Weed out any values that are not valid choices.
        choices = self.getCTA_Choices(productLabel)
        choiceIdentifiers = set([choice["identifier"] for choice in choices])
        values = list(set(values).intersection(choiceIdentifiers))
        
        return {
                "fieldType":"CheckBoxes",
                "label":"Calls to Action (1 or more):",
                "fieldName": "callsToAction_productLevel_"+productLabel,
                "values": values,
                "choices": choices
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

    def getPreviousStagingValue(self, hazard, key, productLabel, prevProductLabel):
        if prevProductLabel:
            prevKey = key+"_" + prevProductLabel
            return hazard.get(key + "_" + productLabel, hazard.get(prevKey))
        return hazard.get(key + "_" + productLabel)
         
 
# Ensure that when the megawidgets are initialized and then subsequently whenever the
# user chooses a new choice from the canned synopses dropdown, the text in the synopsis
# text area is changed to match.
def applyInterdependencies(triggerIdentifiers, mutableProperties):
    if triggerIdentifiers is not None:     
        # See if the trigger identifiers list contains any of the identifiers from the
        # synopsis canned choices; if so, change the text's value to match the associated
        # canned choice text.
    
        # Example:
        # triggerIdentifiers: ['overviewSynopsisCanned_FFA_area.dayNightTemps']
        # mutableProperties:  {'overviewSynopsisExplanation_FFA_area': {'enable': True, 'editable': True, 'extraData': {}}, 
        #          'overviewSynopsis_FFA_area': {'editable': True, 'enable': True, 'values': '', 'extraData': {}}, 
        #          'overviewSynopsisCanned_FFA_area': {'enable': True, 'editable': True, 'extraData': {}, 'choices': 
        #           [{'displayString': 'Warm Temperatures and Snow Melt', 'productString': 
        #                 'Warm temperatures may melt high mountain snowpack and increase river flows.', 'identifier': 'tempSnowMelt'}, 
        #            {'displayString': 'Warm day and night Temperatures', 'productString': 
        #                 'Warm daytime temperatures along with low temperatures remaining above freezing overnight may accelerate snow melt. 
        #                   River flows may increase quickly and remain high for the next week.', 'identifier': 'dayNightTemps'}, 
        #            {'displayString': 'Snow melt and Reservoir releases', 'productString': 'High mountain snow melt and increased 
        #                   reservoir releases may cause the river flows to become high. Possible minor flooding downstream from the dam.', 
        #                   'identifier': 'snowMeltReservoir'}, 
        #            {'displayString': 'Rain On Snow', 'productString': 'Heavy rain may fall on a deep primed snowpack leading to 
        #                   the melt increasing. Flows in Rivers may increase quickly and reach critical levels.', 'identifier': 'rainOnSnow'}, 
        #            {'displayString': 'Ice Jam Flooding', 'productString': 'An ice jam may cause water to infiltrate the 
        #                   lowlands along the river.', 'identifier': 'iceJamFlooding'}, 
        #            {'displayString': 'Increase in Category', 'productString': 'Heavy rainfall may increase the severity of 
        #                   flooding on the |* riverName *|.', 'identifier': 'categoryIncrease'}]}, 
        #        'FFA_tabs': {'enable': True, 'editable': True, 'extraData': {}}, 
        #        'overviewSynopsisContainer_FFA_area': {'enable': True, 'editable': True, 'extraData': {}}}
        return {}      
    return None

