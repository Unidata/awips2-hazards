'''
    Description: Legacy formatter for FLW_FLS products
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Jan 12, 2015    4937    Robert.Blum Initial creation
'''

import datetime
import collections
import types, re, sys
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
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
                easMessage = self._easMessage(productDict)
                if easMessage is not None:
                    partText = easMessage + '\n'
            elif name == 'productHeader':
                partText = self._productHeader()
            elif name == 'overviewHeadline_area':
                partText = self._overviewHeadline_area()
            elif name == 'overviewHeadline_point':
                partText = self._overviewHeadline_point()
            elif name == 'overviewSynopsis_area':
                partText = self._overviewSynopsis_area(productDict)
            elif name == 'overviewSynopsis_point':
                partText = self._overviewSynopsis_point(productDict)
            elif name == 'cityList':
                partText = self._cityList(productDict)
            elif name == 'vtecRecords':
                partText = self._vtecRecords(productDict)
            elif name == 'issuanceTimeDate':
                partText = self._issuanceTimeDate()
                if (self._runMode == 'Practice'):
                    partText += '\n...This message is for test purposes only...\n'
            elif name == 'nextIssuanceStatement':
                # TODO fill in time/day phrase
                partText = 'The next statement will be issued <time/day phrase>.\n'
                partText += '\n&&\n\n'
            elif name in ['callsToAction', 'callsToAction_productLevel']:
                partText = self._callsToAction(productDict, name)
            elif name == 'polygonText':
                if (self._runMode == 'Practice'):
                    partText = 'This is a test message. Do not take action based on this message. \n\n'
                partText += self._polygonText(productDict)
            elif name == 'endSegment':
                partText = '\n$$\n\n' 
            elif name == 'CR':
                partText = '\n'
            elif name == 'segments':
                partText = self.processSubParts(productDict['segments'], infoDicts) 
            elif name == 'sections':
                partText = self.processSubParts(self.productDict['segments'], infoDicts)
            elif name == 'setUp_section':
                pass
            elif name == 'attribution':
                partText = self._attribution(productDict, productDict['hazards']) + '\n'
            elif name == 'attribution_point':
                partText = self._attribution_point(productDict, productDict['hazards'])  + '\n'
            elif name == 'firstBullet':
                partText = self._firstBullet(productDict) + '\n'
            elif name == 'firstBullet_point':
                partText = self._firstBullet_point(productDict) + '\n'
            elif name == 'timeBullet':
                partText = self._timeBullet(productDict)
                if productDict.get('geoType', '') == 'area':
                    partText+= '\n'
            elif name == 'basisBullet':
                partText = self._basisBullet(productDict) + '\n'
            elif name == 'additionalInfoStatement':
                partText = 'Additional information is available at <Web site URL>.\n\n'
            elif name == 'endingSynopsis':
                partText = productDict['endingSynopsis']
                if partText is None:
                    partText = '|* Brief post-synopsis of hydrometeorological activity *|\n\n'
                else:
                    partText = partText+'\n\n'
            elif name == 'basisAndImpactsStatement':
                partText = self._basisAndImpactsStatement(productDict) + '\n'
            elif name == 'rainFallStatement':
                partText = 'The segments in this product are river forecasts for selected locations in the watch area.'
            elif name == 'areaList':
                partText = self._areaList(productDict)
            elif name == 'summaryHeadlines':
                partText = self._summaryHeadlines(productDict)
            elif name == 'wrapUp_product':
                pass
            elif name == 'pointImpactsBullet':
                partText = self._pointImpactsBullet(productDict)
            elif name == 'impactsBullet':
                partText = self._impactsBullet(productDict) + '\n'
            elif name == 'observedStageBullet':
                partText = '* ' + self._observedStageBullet(productDict) + '\n'
            elif name == 'recentActivityBullet':
                partText = self._recentActivityBullet(productDict) + '\n'
            elif name == 'forecastStageBullet':
                partText = self._forecastStageBullet(productDict) + '\n'
            elif name == 'otherStageBullet':
                partText = self._otherStageBullet(productDict) + '\n'
            elif name == 'floodStageBullet':
                partText = '* ' + self._floodStageBullet(productDict) + '\n'
            elif name == 'floodCategoryBullet':
                partText = '* ' + self._floodCategoryBullet(productDict) + '\n'
            elif name == 'floodHistoryBullet':
                partText = '* ' + self._floodHistoryBullet(productDict) + '\n'
            elif name == 'floodPointTable':
                partText = '\n' + self._floodPointTable(productDict) + '\n'
            else:
                textStr = self._tpc.getVal(productDict, name)
                if textStr:
                    partText = textStr + '\n'

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print partText

            text += partText
        return text

    ######################################################
    #  Product Part Methods 
    ######################################################

    ################# Product Level

    def _easMessage(self, productDict):
        segments = self._tpc.getVal(productDict, 'segments', altDict=self.productDict)
        for segment in segments:
            vtecRecords = segment['vtecRecords']
            for vtecRecord in vtecRecords:
                if vtecRecord['sig'] is 'A':
                    return 'Urgent - Immediate broadcast requested'
        return 'Bulletin - EAS activation requested'

    ################# Segment Level

    def _overviewHeadline_point(self):
        new_ext_productSegments = []
        con_productSegments = []
        can_exp_productSegments = [] 
        for segment in self.productDict['segments']:
            vtecRecords = segment['vtecRecords']
            for vtecRecord in vtecRecords:
                action = vtecRecord.get('act')
                if action in ('NEW','EXT'):
                    new_ext_productSegments.append(segment)
                elif action == 'CON':
                    con_productSegments.append(segment)
                else:  # CAN, EXP
                    can_exp_productSegments.append(segment) 

        overviewHeadline = ''
        for segments in  [
                        new_ext_productSegments,
                        con_productSegments,
                        can_exp_productSegments]: 
            if segments:
                overviewHeadline += self.getOverviewHeadlinePhrase(segments)
        return overviewHeadline

    ###################### Section Level

    def _attribution(self, productDict, hazardEvents):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(productDict)

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = self._tpc.hazardName(vtecRecord['hdln'], self._testMode, False)

            if len(vtecRecord['hdln']):
                action = vtecRecord['act']

            if action == 'NEW':
                attribution = nwsPhrase + 'issued a'
            elif action == 'CON':
                attribution += '...The ' + hazName
                if vtecRecord['phensig'] == 'FA.W':
                    if productDict.get('warningType'):
                        attribution += ' for ' +productDict.get('warningType')
                    if productDict.get('typeOfFlooding'):
                        attribution += ' for ' + productDict.get('typeOfFlooding')
                attribution += ' remains in effect for ' + areaPhrase + '...'
            elif action == 'EXT':
                attribution = nwsPhrase + 'extended the '
            elif action == 'CAN':
                attribution = '...The ' + hazName
                if vtecRecord['phensig'] == 'FA.W':
                    if productDict.get('warningType'):
                        attribution += ' for ' +productDict.get('warningType')
                    if productDict.get('typeOfFlooding'):
                        attribution += ' for ' + productDict.get('typeOfFlooding')
                attribution += ' for ' + areaPhrase + ' has been cancelled...'
            elif action == 'EXP':
                expTimeCurrent = self._issueTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'The ' + hazName + \
                      ' for ' + areaPhrase + ' has expired.'
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
                   attribution = 'The ' + hazName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
            elif action == 'ROU':
                attribution = nwsPhrase + 'released '

        return attribution + '\n'

    def _attribution_point(self, productDict, hazardEvents):
        nwsPhrase = 'The National Weather Service in ' + self._wfoCity + ' has '
        attribution = ''
        areaPhrase = self.createAreaPhrase(productDict)

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = self._tpc.hazardName(vtecRecord['hdln'], self._testMode, False)

            if len(vtecRecord['hdln']):
                action = vtecRecord['act']

            if action == 'NEW':
                attribution = nwsPhrase + 'issued a'
            elif action == 'CON':
                attribution = 'The ' + hazName + ' remains in effect for...'
            elif action == 'EXT':
                attribution = 'The ' + hazName + ' is now in effect for...' 
            elif action == 'CAN':
                attribution = 'The ' + hazName + \
                   ' for... ' + areaPhrase + ' has been cancelled.'
            elif action == 'EXP':
                expTimeCurrent = self._issueTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'The ' + hazName + \
                      ' for ' + areaPhrase + ' has expired.'
                else:
                   timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
                   attribution = 'The ' + hazName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        return attribution

    def _firstBullet(self, productDict):
        firstBullet = '* '
        areaPhrase = self.createAreaPhrase(productDict)

        if self._runMode == 'Practice':
            firstBullet += 'This is a test message.  '

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = self._tpc.hazardName(vtecRecord['hdln'], self._testMode, False)

            if len(vtecRecord['hdln']):
                action = vtecRecord['act']

            if action == 'NEW':
                if vtecRecord['phensig'] == 'FA.W':
                    if productDict['warningType']:
                        firstBullet += hazName + ' for ' + productDict['warningType'] + ' for...\n'
                    else:
                        firstBullet += hazName + ' for...\n'
                    if productDict['typeOfFlooding']:
                        #TODO ProductUtils.wrapLegacy should handle the below indent
                        firstBullet += '  ' +productDict['typeOfFlooding'] + ' in...\n'
                    firstBullet += areaPhrase
                elif vtecRecord['phensig'] == 'FA.Y':
                    # Add advisoryType to first bullet if in dictionary
                    if productDict['advisoryType']:
                        firstBullet += productDict['advisoryType'] + ' ' + hazName
                    else:
                        firstBullet += hazName
                    # Add optionalSpecificType to first bullet if in dictionary
                    if productDict['optionalSpecificType']:
                        firstBullet+= ' for ' +  productDict['optionalSpecificType'] + ' for...\n'
                    else:
                        firstBullet += ' for...\n'
                    # Add typeOfFlooding to first bullet if in dictionary
                    if productDict['typeOfFlooding']:
                        #TODO ProductUtils.wrapLegacy should handle the below indent
                        firstBullet += '  ' +productDict['typeOfFlooding'] + ' in...\n'
                    firstBullet += areaPhrase
                else:
                    firstBullet += hazName + ' for'
                    firstBullet += areaPhrase

            elif action == 'CON':
                if vtecRecord['phensig'] == 'FA.W':
                    firstBullet += '...The ' + hazName
                    if productDict.get('warningType'):
                        firstBullet += ' for ' +productDict.get('warningType')
                    if productDict.get('typeOfFlooding'):
                        firstBullet += ' for ' + productDict.get('typeOfFlooding')
                    firstBullet += ' remains in effect for...'
                firstBullet +=  areaPhrase

            elif action == 'EXT':
                if vtecRecord['phensig'] == 'FA.W':
                    if productDict['warningType']:
                        firstBullet += hazName + ' for ' + productDict['warningType'] + ' for...\n'
                    else:
                        firstBullet += hazName + ' for...\n'
                    if productDict['typeOfFlooding']:
                        #TODO ProductUtils.wrapLegacy should handle the below indent
                        firstBullet += '  ' +productDict['typeOfFlooding'] + ' in...\n'
                    firstBullet += areaPhrase
                else:
                    firstBullet += hazName + ' for...\n'
                    firstBullet += areaPhrase

            elif action == 'CAN':
                firstBullet = ''
    
            elif action == 'EXP':
                firstBullet = ''

            elif action == 'ROU':
                firstBullet += hazName + ' for '
                firstBullet += areaPhrase
        return firstBullet + '\n'

    def _firstBullet_point(self, productDict):
        firstBullet = ''
        areaPhrase = self.createAreaPhrase(productDict)

        # Use this to determine which first bullet format to use.
        vtecRecords = productDict['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = self._tpc.hazardName(vtecRecord['hdln'], self._testMode, False)

            if len(vtecRecord['hdln']):
                action = vtecRecord['act']

            if action == 'NEW':
                firstBullet += '* ' + hazName + ' for\n'
                firstBullet += areaPhrase
            elif action in ['CON', 'EXT']:
                firstBullet +=  'The ' + hazName + ' continues for\n' + '  ' + areaPhrase
            elif action == 'CAN':
                firstBullet +=  'The ' + hazName + ' is cancelled for\n' + '  ' + areaPhrase
            elif action == 'ROU':
                firstBullet += hazName + ' for\n' + '  ' + areaPhrase

        return firstBullet

    def _basisBullet(self, segment):
        act = segment['hazards'][0]['act']
        text = ''

        if act in ['NEW', 'ROU', 'EXT']:
            text += '* '

        if (self._runMode == 'Practice'):
            text += "This is a test message.  "

        if self.timezones:
            # use first time zone in the list
            text += 'At ' + self._tpc.formatDatetime(self._startTime, '%l%M %p %Z', self.timezones[0]).strip()

        elements = KeyInfo.getElements('basisBullet', segment)
        if len(elements) > 0:
             basis = segment[elements[0]]
        else:
             basis = segment.get('basisBullet', None)
        if basis:
            text+= basis
        else:
            text+= "...Flooding from heavy rain. This rain was located over the warned area."
        return text + '\n'

    ###################### Utility Methods

    def getOverviewHeadlinePhrase(self, segments, lineLength=69):
        '''
            ...THE NATIONAL WEATHER SERVICE IN DES MOINES HAS ISSUED A FLOOD
            ADVISORY FOR THE WINNEBAGO RIVER AT MASON CITY...SHELL ROCK RIVER
            AT MARBLE ROCK...CEDAR RIVER AT JANESVILLE...CEDAR RIVER AT CEDAR
            FALLS...AND CEDAR RIVER AT WATERLOO...
        '''
        locationPhrases = []
        areaGroups = []

        # There could be multiple points sharing a VTEC code e.g. NEW
        for segment in segments:
            ugcs = []
            for area in segment['impactedAreas']:
                ugcs.append(area['ugc'])

            pointAreaGroups = self._tpc.getGeneralAreaList(ugcs)
            areaGroups += pointAreaGroups

            nameDescription, nameTypePhrase = self._tpc.getNameDescription(pointAreaGroups)
            affected = nameDescription + ' '+ nameTypePhrase
            riverName = segment['riverName_GroupName']
            proximity = segment['proximity']
            if proximity is None:
                proximity = 'near'
            riverPointName = segment['riverPointName']
            locationPhrases.append(riverName + ' ' + proximity + ' ' + riverPointName + ' affecting ' + affected + '.')  

        locationPhrase = '\n'.join(locationPhrases)
        areaGroups = self._tpc.simplifyAreas(areaGroups)
        states = self._tpc.getStateDescription(areaGroups)
        riverPhrase = 'the following rivers in ' + states

        nwsPhrase = 'The National Weather Service in ' + self._wfoCity
        # Use this to determine which first bullet format to use.
        vtecRecords = segment['vtecRecords']
        for vtecRecord in vtecRecords:  # NOTE there is only one vtecRecord / hazard to process
            hazName = self._tpc.hazardName(vtecRecord['hdln'], self._testMode, False)

            if len(hazName):
                action = vtecRecord['act']

            if action == 'NEW':
                overview = nwsPhrase + ' has issued a ' + hazName + ' for '+ riverPhrase

            elif action == 'CON':
                overview = nwsPhrase + ' is continuing the ' + hazName + ' for '+ riverPhrase
    
            elif action == 'EXT':
                overview = nwsPhrase + ' is extending the ' + hazName + ' for '+ riverPhrase

            elif action == 'CAN':
                overview = nwsPhrase + ' is canceling the ' + hazName + ' for '+ riverPhrase
    
            elif action == 'EXP':
                expTimeCurrent = self._issueTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    overview = 'The ' + hazName + \
                      ' for ' + riverPhrase + ' has expired '
                else:
                    timeWords = self._tpc.getTimingPhrase(vtecRecord, [], expTimeCurrent, timeZones=self.timezones)
                    overview = 'The ' + hazName + \
                      ' for ' + riverPhrase + ' will expire ' + timeWords

            elif action == 'ROU':
                '''
                ...The National Weather Service in Omaha/Valley is releasing Test 
                FORECAST INFORMATION for the following rivers in southeast 
                Nebraska...
                '''
                overview = nwsPhrase + ' is releasing ' + hazName + ' for '+ riverPhrase

        overview = '...' + overview + '...\n\n'
        locationPhrase = self._tpc.indentText(locationPhrase, indentFirstString='  ',
              indentNextString='  ', maxWidth=69,
              breakStrings=[' ', '-', '...'])
        return overview + locationPhrase + '\n\n'