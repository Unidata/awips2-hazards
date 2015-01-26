'''
    Description: Legacy formatter for hydro FFA products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 07  2015            mduff       Initial release
    Jan 26, 2015 4936       chris.cody  Implement scripts for Flash Flood Watch Products (FFA,FAA,FLA)
'''

import datetime

import types, re, sys
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from QueryAfosToAwips import QueryAfosToAwips
from Bridge import Bridge
from TextProductCommon import TextProductCommon
import Legacy_Hydro_Formatter

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self) :
        super(Format, self).initialize()


    def execute(self, productDict):
        self.productDict = productDict
        self.initialize()

        legacyText = self._createTextProduct()

        return ProductUtils.wrapLegacy(legacyText)

    def _processProductParts(self, productDict, productParts, skipParts=[]):
        text = ''
        if type(productParts) is types.DictType:
            arguments = productParts.get('arguments')
            partsList = productParts.get('partsList')
        else:
            partsList = productParts
        
        for part in partsList:
            valtype = type(part)
            if valtype is types.StringType:
                name = part
            elif valtype is types.TupleType or valtype is types.ListType:
                name = part[0]
                infoDicts = part[1]

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print 'Legacy Part:', name, ': ', 

            partText = ''
            if name == 'wmoHeader':
                partText = self._wmoHeader(self._productID, self._siteID, self._startTime) + '\n\n'
            elif name == 'wmoHeader_noCR': 
                partText = self._wmoHeader(self._productID, self._siteID, self._startTime) + '\n'
            elif name == 'setUp_product':
                pass
            elif name == 'setUp_segment':
                pass
            elif name == 'ugcHeader':
                partText = self._ugcHeader(productDict)
            elif name == 'easMessage':
                segments = self.productDict['segments']
                vtecRecords = []
                for segment in segments:
                    if 'vtecRecords' in segment:
                        vtec = segment['vtecRecords']
                        vtecRecords.extend(vtec)
                easMessage = self._easMessage(vtecRecords)
                if easMessage is not None:
                    partText = easMessage + '\n'
            elif name == 'productHeader':
                partText = self._productHeader()
            elif name == 'overviewHeadline_point':
                partText = self._overviewHeadline_point(productDict)
            elif name == 'overviewSynopsis_point':
                partText = self._overviewSynopsis_point(productDict)
            elif name == 'vtecRecords':
                partText = self._vtecRecords(productDict)
            elif name == 'areaList':
                partText = self._areaList(productDict)
            elif name == 'issuanceTimeDate':
                partText = self._issuanceTimeDate()
            elif name == 'callsToAction' or name =='callsToAction_productLevel':
                partText = self._callsToAction(productDict, name)
                if (self._runMode == 'Practice'):
                    partText += 'This is a test message. Do not take action based on this message. \n\n'
            elif name == 'polygonText':
                partText = self._polygonText(productDict)
            elif name == 'endSegment':
                partText = '\n$$\n\n' 
            elif name == 'CR':
                partText = '\n'
            elif name == 'cityList':
                partText = self._cityList(productDict)
            elif name == 'summaryHeadlines':
                partText = self._summaryHeadlines(productDict)
            elif name == 'segments':
                partText = self.processSubParts(productDict['segments'], infoDicts) 
            elif name == 'sections':
                partText = self.processSubParts(self.productDict['segments'], infoDicts)
            elif name == 'setUp_section':
                pass
            elif name == 'attribution':
                hazards = productDict['hazards']
                # There is only 1 hazard
                for hazard in hazards:
                    partText += self._attribution(hazard, productDict) + '\n'
            elif name == 'firstBullet':
                hazards = productDict['hazards']
                # There is only 1 hazard
                for hazard in hazards:
                    partText += self._firstBullet(hazard, productDict) + '\n'
            elif name == 'timeBullet':
                partText = self._timeBullet(productDict) + '\n'
            elif name == 'basisBullet':
                partText = self._basisBullet(hazard, productDict)
            elif name == 'impactsBullet':
                partText = self._impactsBullet(productDict)
            elif name == 'emergencyHeadline':
                if (self._runMode == 'Practice'):
                    partText = '...This message is for test purposes only... \n\n'
                includeChoices = productDict['include']
                if includeChoices and 'ffwEmergency' in includeChoices:
                    partText += '...Flash Flood Emergency for ' + productDict['includeEmergencyLocation'] + '...\n\n'
            elif name == 'emergencyStatement':
                includeChoices = productDict['include']
                if includeChoices and 'ffwEmergency' in includeChoices:
                    partText = '  This is a Flash Flood Emergency for ' + productDict['includeEmergencyLocation'] + '\n\n'
            elif name == 'locationsAffected':
                partText = self._locationsAffected(productDict)
            elif name == 'endingSynopsis':
                partText = productDict.get('endingSynopsis', '')
                if partText != '':
                    partText += '\n\n'
            elif name == 'floodPointHeader':
                partText = self._floodPointHeader() + '\n'
            elif name == 'floodPointHeadline':
                partText = self._floodPointHeadline() + '\n'
            elif name == 'observedStageBullet':
                partText = '* ' + self._observedStageBullet(productDict) + '\n'
            elif name == 'floodStageBullet':
                partText = '* ' + self._floodStageBullet(productDict) + '\n'
            elif name == 'floodCategoryBullet':
                partText = '* ' + self._floodCategoryBullet(productDict) + '\n'
            elif name == 'otherStageBullet':
                partText = self._otherStageBullet(productDict) + '\n'
            elif name == 'forecastStageBullet':
                partText = self._forecastStageBullet(productDict) + '\n'
            elif name == 'pointImpactsBullet':
                partText = self._pointImpactsBullet(productDict)
            elif name == 'floodPointTable':
                partText = '\n' + self._floodPointTable(productDict) + '\n'
            elif name == 'nextIssuanceStatement':
                partText = self._nextIssuanceStatement(productDict)
            elif name == 'rainFallStatement':
                partText = self._rainFallStatement(productDict)
            elif name == 'additionalInfoStatement':
                partText = self._additionalInfoStatement(productDict)
            else:
                textStr = self._tpc.getVal(productDict, name)
                if textStr:
                    partText = textStr + '\n' 

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print partText

            if partText is not None:
                text += partText
        return text

    def processSubParts(self, infoDicts, subParts):
        """
        Generates Legacy text from a list of subParts e.g. segments or sections
        @param infoDicts: a list of dictionaries for each subPart
        @param subParts: a list of Product Parts for each segment
        @return: Returns the legacy text of the subParts
        """
        
        text = '' 
        for i in range(len(subParts)):
            text += self._processProductParts(infoDicts[i], subParts[i].get('partsList'))
        return text

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _easMessage(self, vtecRecords):
        for vtecRecord in vtecRecords:
            if 'sig' in vtecRecord:
                if vtecRecord['sig'] is 'A':
                    return 'Urgent - Immediate broadcast requested'
        return 'Bulletin - EAS activation requested'

    ################# Segment Level

    ################# Section Level
    def _attribution(self, hazardEvent, productDict):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(productDict)

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = vtecRecord['hdln']

            if len(hazName):
                action = vtecRecord['act']

            if action == 'NEW':
                attribution = nwsPhrase + 'issued a'
            elif action == 'CON':
                attribution = 'the ' + hazName + ' remains in effect for...'
            elif action == 'EXT':
                attribution = 'the ' + hazName + ' is now in effect for...' 
            elif action == 'CAN':
                attribution = 'the ' + hazName + \
                   ' for... ' + areaPhrase + ' has been cancelled.'
            elif action == 'EXP':
                expTimeCurrent = self._startTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' has expired.'
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, [hazardEvent], expTimeCurrent)
                   attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        return attribution + '\n'

    def _firstBullet(self, hazard, productDict):
        headPhrase = '* '
        areaPhrase = self.getAreaPhraseBullet(productDict)

        if self._runMode == 'Practice':
            testText = 'This is a test message.  '
            headPhrase = headPhrase + testText

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = vtecRecord['hdln']

            if len(hazName):
                action = vtecRecord['act']

            if action == 'NEW':
                headPhrase += hazName + ' for...\n'
                headPhrase += areaPhrase
                headPhrase += ' due to '
                immediateCauseCode = hazard['immediateCause']
                immediateCauseText = self.getImmediateCauseText(immediateCauseCode)
                headPhrase += immediateCauseText + '\n'
            elif action == 'CON':
                headPhrase +=  areaPhrase
            elif action == 'EXT':
                headPhrase += ' ' + areaPhrase

        return headPhrase

    def _locationsAffected(self, segmentDict):
        heading = '* '
        locationsAffected = ''

        if (self._runMode == 'Practice'):
            heading += "This is a test message.  "

        immediateCause = segmentDict.get('immediateCause', None)
        if immediateCause == 'DM' or immediateCause == 'DR':
            damOrLeveeName = segmentDict['damOrLeveeName']
            if damOrLeveeName:
                damInfo = self._damInfo().get(damOrLeveeName)
                if damInfo:
                    # Scenario
                    scenario = segmentDict['scenario']
                    if scenario:
                        scenarios = damInfo['scenarios']
                        if scenarios:
                            scenarioText = scenarios[scenario]
                            if scenarioText:
                                locationsAffected += scenarioText + '\n\n'
                    # Rule of Thumb
                    ruleOfThumb = damInfo['ruleOfThumb']
                    if ruleOfThumb:
                        locationsAffected += ruleOfThumb + '\n\n'

        # Need to check for List of cities here
        if segmentDict['citiesListFlag'] == True:
            locationsAffected += 'Locations impacted include...' + self.formatCityList(segmentDict) + '\n\n'

        if not locationsAffected:
            locationsAffected = "Some locations that will experience flash flooding include..."
            locationsAffected += self.createImpactedLocations(segmentDict) + '\n\n'

        # Add any other additional Info
        locationsAffected += self.createAdditionalComments(segmentDict)

        return heading + locationsAffected

    # Generate Watch Basis Bullet block
    def _basisBullet(self, hazardEvent, productDict):
        
        bulletText = None
        basisStatement = productDict['basisBullet']
        if (self.checkForValidString(basisStatement) == True):
            if self._runMode == 'Practice':
                bulletText = '|* This is a test message. Current hydrometeorological basis \n* '
            else:
                bulletText = '* '

            # Add Basis Statement Data
            vtecRecords = productDict['vtecRecords']
            for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
                eventTime = vtecRecord.get('startTime')
                eventTime = self._tpc.getFormattedTime(eventTime, '%I%M %p %Z ', stripLeading=True, timeZones=self.timezones)
                bulletText += 'At ' + eventTime

            bulletText += basisStatement
             
            immediateCauseCode =  productDict['immediateCause']
            immediateCauseText = self.getImmediateCauseText(immediateCauseCode)
            if (self.checkForValidString(immediateCauseText) == True):
                bulletText += ' caused by ' + immediateCauseText 
            else:
                bulletText += ' A Flash Flood Watch'

            bulletText += '\n'
             
            if self._runMode == 'Practice':
                bulletText += '*|'
                
            bulletText += '\n\n'
            
        return bulletText

    # Generate Immediate Impacts Bullet block
    def _impactsBullet(self, segment):
        bulletText = ""
        #Add Impact Statement Data
        impactStatement = segment['impactStatement']
        if (self.checkForValidString(impactStatement) == True):    
            if self._runMode == 'Practice':
                bulletText = 'This is a test message. |* Current hydrometeorological Impacts \n* '
            else:
                bulletText = '* '
            bulletText += impactStatement + '\n'
         
            if self._runMode == 'Practice':
                bulletText += '*|'

        bulletText += '\n\n'
        return bulletText
    
    def _rainFallStatement(self, productDict):

        statementText = 'The segments in this product are river forecasts for selected locations in the watch area.\n'
            
        return statementText

    def _additionalInfoStatement(self, productDict):

        #TODO Please override this method for your site 
        return 'Additional information is available at <Web site URL>.\n\n'

    def _nextIssuanceStatement(self, productDict):

        # TODO fill in time/day phrase 
        statementText = 'The next statement will be issued <time/day phrase>.\n'
        
        return statementText

    def _overviewHeadline_point(self, productDict):

        statementText = '|* Point Overview Headline *|'
            
        return statementText

    def _overviewSynopsis_point(self, productDict):

        statementText = '|* Point Overview Synopsis *|'
            
        return statementText

    #Translate an Immediate Cause code into FFA cause string.
    def getImmediateCauseText(self, immediateCauseCode):
        immediateCauseDict = {"ER":"excessive rain",
                              "SM":"snow Melt",
                              "RS":"rain and snow melt", 
                              "DM":"dam or levee failure",
                              "DR":"upstream dam release",
                              "GO":"glacier-dammed lake outburst",
                              "IJ":"ice jam", 
                              "IC":"rain and/or snow melt and/or ice jam",
                              "FS":"upstream flooding plus storm surge", 
                              "FT":"upstream flooding plus tidal effects",
                              "ET":"elevated upstream flow plus tidal effects",
                              "WT":"wind and/or tidal effects",
                              "OT":"other effects",
                              "MC":"multiple causes",
                              "UU":"Unknown" }
        immediateCauseText = immediateCauseDict[immediateCauseCode]

        return immediateCauseText
    
    #Returns TRUE if it is not null or empty
    def checkForValidString(self, theString):
        retBool = False
        if theString:
            tempString = theString.strip()
            if tempString != "":
                retBool = True
                
        return retBool

