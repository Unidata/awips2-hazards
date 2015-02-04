'''
    Description: Creates the attribution and firstBullet text. 
     
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 2015       4375    Tracy Hansen      Initial creation
    @author Tracy.L.Hansen@noaa.gov
'''
import collections, os, types, datetime
from com.raytheon.uf.common.time import SimulatedTime

class AttributionFirstBulletText:
    def __init__(self, productID, productSegment, hazardEvent, vtecRecord, metaData, issueTime, testMode, wfoCity,
                 tpc, rfp, hydrologicCauseMapping, listOfCities=False, areaPhrase=None, endString='. '):
        self.hazardEvent = hazardEvent
        self.vtecRecord = vtecRecord
        self.productID = productID
        self.productSegment = productSegment
        self.issueTime = issueTime
        self.testMode = testMode
        self.wfoCity = wfoCity
        self.tpc = tpc
        self.rfp = rfp
        self.endString = endString
        self.listOfCities = listOfCities
        self.hydrologicCauseMapping = hydrologicCauseMapping
        
        self.tpc.setPartOfStateInfo(hazardEvent["attributes"])
        
        if not areaPhrase:
            phen = vtecRecord.get("phen")
            sig = vtecRecord.get("sig")
            action = vtecRecord.get("act")
            geoType = hazardEvent.get('geoType')
            if phen in ["FF", "FA", "TO", "SV", "SM", "EW" , "FL" ] and \
                action in [ "NEW", "EXA", "EXT", "EXB" ] and \
                geoType == 'area' and sig != "A" :
                areaPhrase = self.getAreaPhraseBullet(productSegment, metaData, hazardEvent)
            else :
                areaPhrase = self.getAreaPhrase(productSegment, metaData, hazardEvent)

        self.attribution, self.firstBullet = self.getAttributionPhrase(
                   vtecRecord, hazardEvent, areaPhrase, issueTime, testMode, wfoCity)

    def getAttributionText(self):
        return self.attribution
    
    def getFirstBulletText(self):
        '''
           methods based on hazard type...
           Use current code, V2, V3, and directives.
        '''
        return self.firstBullet
            
    def getAreaPhraseBullet(self, productSegment, metaData, hazardEvent):
        '''
        @param productSegment object
        @param metaData
        @param hazardEvent -- representative for the segment
        @return: Plain language list of counties/zones in the hazard appropriate
                 for bullet format procducts
        ''' 
        ugcs = hazardEvent.get('ugcs', [])
        ugcPortions = hazardEvent.get('ugcPortions', {})
        ugcPartsOfState = hazardEvent.get('ugcPartsOfState', {})

        # These need to be ordered by area of state.
        orderedUgcs = []
        for ugc in ugcs :
            orderedUgcs.append(ugc[:2] + ugcPartsOfState.get(ugc, "") + "|" + ugc)
        orderedUgcs.sort()

        areaPhrase = "\n"
        for ougc in orderedUgcs :
            ugc = ougc.split("|")[1]
            part = ugcPortions.get(ugc, "")
            if part == "" :
                textLine = "  "
            else :
                textLine = "  " + part + " "
            textLine += self.tpc.getInformationForUGC(ugc) + " "
            textLine += self.tpc.getInformationForUGC(ugc, "typeSingular") + " in "
            part = ugcPartsOfState.get(ugc, "")
            if part == "" :
                textLine += self.tpc.getInformationForUGC(ugc, "fullStateName") + "\n"
            else :
                textLine += part + " " + self.tpc.getInformationForUGC(ugc, "fullStateName") + "\n"
            areaPhrase += textLine

        return areaPhrase
    
    def getAttributionPhrase(self, vtecRecord, hazardEvent, areaPhrase, issueTime, testMode, wfoCity, lineLength=69):
        '''
        THE NATIONAL WEATHER SERVICE IN DENVER HAS ISSUED A

        * AREAL FLOOD WATCH FOR A PORTION OF SOUTH CENTRAL COLORADO...
          INCLUDING THE FOLLOWING COUNTY...ALAMOSA.
        '''
        nwsPhrase = 'The National Weather Service in ' + wfoCity + ' has '
        endString = self.endString
        
        # Attribution and 1st bullet (headPhrase)
        #
        headPhrase = None
        attribution = ''

        # Use this to determine which first bullet format to use.
        phen = vtecRecord.get("phen")

        hazName = self.tpc.hazardName(vtecRecord['hdln'], testMode, False)

        if len(vtecRecord['hdln']):
            action = vtecRecord['act']
            
           # Handle special cases
            if action == 'EXT' and self._product.productID in ['FFA', 'FLW', 'FLS'] and self._product.geoType == 'point':
                # Use continuing wording for EXT
                action = 'CON'
                                
            if action == 'NEW':
                attribution = nwsPhrase + 'issued a'
                headPhrase = hazName + ' for'
                headPhrase += ' ' + areaPhrase + endString
    
            elif action == 'CON':
                attribution = 'the ' + hazName + ' remains in effect for'
                headPhrase = areaPhrase + endString
    
            elif action == 'EXA':
                attribution = nwsPhrase + 'expanded the'
                headPhrase = hazName + ' to include'
                headPhrase = ' ' + areaPhrase + endString
    
            elif action == 'EXT':
                if action in 'EXT' and self._product.productID in ['FFA', 'FLW', 'FLS'] and self._product.geoType == 'area':
                    attribution = nwsPhrase + 'extended the '
                else:
                    attribution = 'the ' + hazName + ' is now in effect for' 
                headPhrase = ' ' + areaPhrase + endString
                    
            elif action == 'EXB':
                attribution = nwsPhrase + 'expanded the'
                headPhrase = hazName + ' to include'
                headPhrase = ' ' + areaPhrase + endString
    
            elif action == 'CAN':
                attribution = 'the ' + hazName + \
                   ' for ' + areaPhrase + ' has been canceled ' + endString
    
            elif action == 'EXP':
                expTimeCurrent = issueTime
                if vtecRecord['endTime'] <= expTimeCurrent:
                    attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' has expired ' + endString
                else:
                   timeWords = self.tpc.getTimingPhrase(vtecRecord, [hazardEvent], expTimeCurrent)
                   attribution = 'the ' + hazName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + endString

        if headPhrase is not None:
            headPhrase = self.tpc.indentText(headPhrase, indentFirstString='',
              indentNextString='  ', maxWidth=lineLength,
              breakStrings=[' ', '-', '...'])
        else:
            headPhrase = ''

        return attribution, headPhrase
       
    def getAreaPhrase(self, productSegment, metaData, hazardEvent):
        '''
        Central Kent County in Southwest Michigan
        This includes the cities of City1 and City2

        @param productSegment object
        @param metaData
        @param hazardEvent -- representative for the segment
        
        @return text describing the UGC areas and optional cities
        
        ''' 
        if hazardEvent.get('geoType') == 'area':
            immediateCause = hazardEvent.get('immediateCause')
            ugcPhrase = self.tpc.getAreaPhrase(productSegment.ugcs)
            if self.listOfCities:
                ugcPhrase += '\n' + productSegment.cityString
                
            if immediateCause in ['DM', 'DR', 'GO', 'IJ', 'RS', 'SM']:
                hydrologicCause = hazardEvent.get('hydrologicCause')
                riverName = None
                if immediateCause == 'DM' and hydrologicCause in ['dam', 'siteImminent', 'siteFailed']:
                    damOrLeveeName = self.tpc.getProductStrings(hazardEvent, metaData, 'damOrLeveeName')
                    if damOrLeveeName:
                        damInfo = self._damInfo().get(damOrLeveeName)
                        if damInfo:
                            riverName = damInfo.get('riverName')
                    if not riverName or not damOrLeveeName:
                        return ugcPhrase
                    else:
                        return 'The ' + riverName + ' below ' + damOrLeveeName + ' in ' + ugcPhrase
                else:
                    typeOfFlooding = self.hydrologicCauseMapping(hydrologicCause, 'typeOfFlooding')
                    return typeOfFlooding + ' in ' + ugcPhrase                
            return ugcPhrase
        else:
            millis = SimulatedTime.getSystemTime().getMillis()
            if not self.rfp:
                currentTime = datetime.datetime.fromtimestamp(millis / 1000)
                from RiverForecastPoints import RiverForecastPoints
                self.rfp = RiverForecastPoints(currentTime)
             #  <River> <Proximity> <IdName> 
            riverName = self.rfp.getGroupName(productSegment.pointID)
            proximity = self.rfp.getRiverPointProximity(productSegment.pointID) 
            riverPointName = self.rfp.getRiverPointName(productSegment.pointID) 
            return  '\n the ' + riverName + ' ' + proximity + ' ' + riverPointName  
        
    def hydrologicCauseMapping(self, hydrologicCause, key):
        mapping = {
            'dam':          {'immediateCause': 'DM', 'typeOfFlooding':'A dam failure'},
            'siteImminent': {'immediateCause': 'DM', 'typeOfFlooding':'A dam break'},
            'siteFailed':   {'immediateCause': 'DM', 'typeOfFlooding':'A dam break'},
            'levee':        {'immediateCause': 'DM', 'typeOfFlooding':'A levee failure'},
            'floodgate':    {'immediateCause': 'DR', 'typeOfFlooding':'A dam floodgate release'},
            'glacier':      {'immediateCause': 'GO', 'typeOfFlooding':'A glacier-dammed lake outburst'},
            'icejam':       {'immediateCause': 'IJ', 'typeOfFlooding':'An ice jam'},
            'snowMelt':     {'immediateCause': 'RS', 'typeOfFlooding':'Extremely rapid snowmelt'},
            'volcano':      {'immediateCause': 'SM', 'typeOfFlooding':'Extremely rapid snowmelt caused by volcanic eruption'},
            'volcanoLahar': {'immediateCause': 'SM', 'typeOfFlooding':'Volcanic induced debris flow'},
            'default':      {'immediateCause': 'ER', 'typeOfFlooding':'Excessive rain'}
            }
        if mapping.has_key(hydrologicCause):
            return mapping[hydrologicCause][key]
        else:
            return mapping['default'][key]                                         

    #  Temporary solution -- 
    # TODO -- use damInfo from Maps Database            
    def _damInfo(self):
        return {
                'Big Rock Dam': {
                        'riverName': 'Phil River',
                        'cityInfo': 'Evan...located about 3 miles',
                        'scenarios': {
                            'highFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 18 feet in 16 minutes.',
                            'highNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 23 feet in 31 minutes.',
                            'mediumFast': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 14 feet in 19 minutes.',
                            'mediumNormal': 'If a complete failure of the dam occurs...the water depth at Evan could exceed 17 feet in 32 minutes.',
                            },
                        'ruleOfThumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                        and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.''',
                    },
                'Branched Oak Dam': {
                        'riverName': 'Kells River',
                        'cityInfo': 'Dangelo...located about 6 miles',
                        'scenarios': {
                            'highFast': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 19 feet in 32 minutes.',
                            'highNormal': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 26 feet in 56 minutes.',
                            'mediumFast': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 14 feet in 33 minutes.',
                            'mediumNormal': 'If a complete failure of the dam occurs...the water depth at Dangelo could exceed 20 feet in 60 minutes.',
                            },
                        'ruleOfThumb': '''Flood wave estimate based on the dam in Idaho: Flood initially half of original height behind the dam 
                                        and 3-4 mph; 5 miles in 1/2 hours; 10 miles in 1 hour; and 20 miles in 9 hours.''',
                    },                
                }


