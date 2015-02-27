'''
    Description: Creates the attribution and firstBullet text.
     
    To reduce code redundancy and encapsulate business logic, attribution and firstBullet logic 
    has been consolidated into this module. 
    
    For the attribution and firstBullet, there are methods for each VTEC code.  
    These methods could be further broken down if needed as more is learned about Focal Point overrides 
    and as more hazard types are incorporated.
     
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 2015       4375    Tracy Hansen      Initial creation
    Feb 2015       4937    Robert.Blum       Check if proximity is None
    Feb 2015       6599    Robert.Blum       Changed to new style class
    @author Tracy.L.Hansen@noaa.gov
'''
import collections, os, types, datetime
from com.raytheon.uf.common.time import SimulatedTime


class AttributionFirstBulletText(object):
    def initialize(self, sectionDict, productID, issueTime, testMode, wfoCity, tpc, areaPhrase=None, endString='. '):
        # Variables to establish
        self.sectionDict = sectionDict
        self.productID = productID
        self.issueTime = issueTime
        self.testMode = testMode
        self.wfoCity = wfoCity
        self.tpc = tpc               
        self.vtecRecord = sectionDict.get('vtecRecord')
        self.phen = self.vtecRecord.get("phen")
        self.sig = self.vtecRecord.get("sig")
        self.phenSig = self.vtecRecord.get('phensig')
        self.subType = self.vtecRecord.get('subtype')
        self.action = self.vtecRecord.get('act')
        self.hazardName = self.tpc.hazardName(self.vtecRecord.get('hdln'), testMode, False)
        self.geoType = sectionDict.get('geoType')        
        self.metaData = sectionDict.get('metaData')
        self.pointID = sectionDict.get('pointID')
        
        self.cityString = sectionDict.get('cityString', '')
        self.ugcs = sectionDict.get('ugcs', [])
        self.ugcPortions = sectionDict.get('ugcPortions', {})
        self.ugcPartsOfState = sectionDict.get('ugcPartsOfState', {})
        self.immediateCause = sectionDict.get('immediateCause')
        self.hydrologicCause = sectionDict.get('hydrologicCause')
        if self.hydrologicCause:
            self.typeOfFlooding = self.hydrologicCauseMapping(self.hydrologicCause, 'typeOfFlooding')
        else:
            self.typeOfFlooding = self.immediateCauseMapping(self.immediateCause)
        self.creationTime = sectionDict.get('creationTime')

        self.endString = endString
        self.listOfCities = sectionDict.get('citiesListFlag', False) 
        self.warningType = sectionDict.get('warningType')
        
        self.advisoryType = sectionDict.get('advisoryType_productString')
        self.optionalSpecificType = sectionDict.get('optionalSpecificType')
        self.damOrLeveeName = self.tpc.getProductStrings(sectionDict, self.metaData, 'damOrLeveeName')
        self.damName = sectionDict.get('damName')
        self.riverName = None
        if self.damName:
            damInfo = self._damInfo().get(self.damName)
            if damInfo:
                self.riverName = damInfo.get('riverName')
        self.streamName = sectionDict.get('streamName')

        self.nwsPhrase = 'The National Weather Service in ' + self.wfoCity + ' has '
        
        if not areaPhrase:
            if self.phen in ["FF", "FA", "TO", "SV", "SM", "EW" , "FL" ] and \
                self.action in [ "NEW", "EXA", "EXT", "EXB" ] and \
                self.geoType == 'area' and self.sig != "A" :
                self.areaPhrase = self.getAreaPhraseBullet()
            else :
                self.areaPhrase = self.getAreaPhrase(sectionDict)
        
    def initialize_withHazardEvent(self, hazardEvent, vtecRecord, metaData, productID, issueTime, testMode, wfoCity, tpc, rfp, areaPhrase=None, endString='. '):
        hazardEvent.set('creationTime', hazardEvent.getCreationTime())
        hazardEvent.set('vtecRecord', vtecRecord)
        hazardEvent.set('metaData', metaData)
        pointID = hazardEvent.get('pointID')
        if pointID:
            if not rfp:
                millis = SimulatedTime.getSystemTime().getMillis()
                currentTime = datetime.datetime.fromtimestamp(millis / 1000)
                from RiverForecastPoints import RiverForecastPoints
                rfp = RiverForecastPoints(currentTime)
            hazardEvent.set('riverName_GroupName', rfp.getGroupName(pointID))
            hazardEvent.set('proximity', rfp.getRiverPointProximity(pointID))
            hazardEvent.set('riverPointName', rfp.getRiverPointName(pointID))
        self.initialize(hazardEvent, productID, issueTime, testMode, wfoCity, tpc, areaPhrase=None, endString='. ' )        

    # attribution
    def getAttributionText(self):
        if self.action == 'CAN':
            attribution = self.attribution_CAN()
        elif self.action == 'EXP':
            attribution = self.attribution_EXP()
        elif self.action == 'UPG':
            attribution = self.attribution_UPG()
        elif self.action == 'NEW':
            attribution = self.attribution_NEW()
        elif self.action == 'EXB':
            attribution = self.attribution_EXB()
        elif self.action == 'EXA':
            attribution = self.attribution_EXA()
        elif self.action == 'EXT':
            attribution = self.attribution_EXT() 
        elif self.action == 'CON':
            attribution = self.attribution_CON()
        elif self.action == 'ROU':
            attribution = self.attribution_ROU()
        return attribution + '\n\n'
    
    def attribution_CAN(self):
        attribution = '...The ' + self.hazardName
        if self.phenSig == 'FA.W':
            attribution += self.qualifiers()
        attribution += ' for ' + self.areaPhrase + ' has been canceled...'
        return attribution
    
    def attribution_EXP(self):
        expireTimeCurrent = self.issueTime
        if self.vtecRecord.get('endTime') <= expireTimeCurrent:
            attribution = 'the ' + self.hazardName + ' for ' + self.areaPhrase + ' has expired.'
        else:
            timeWords = self.tpc.getTimingPhrase(self.vtecRecord, [], expireTimeCurrent)
            attribution = 'the ' + self.hazardName + \
                      ' for ' + areaPhrase + ' will expire ' + timeWords + '.'
        return attribution

    def attribution_UPG(self):
        attribution = self.nwsPhrase + 'upgraded the'
        return attribution

    def attribution_NEW(self):
        attribution = self.nwsPhrase + 'issued a'
        return attribution

    def attribution_EXB(self):
        attribution = self.nwsPhrase + 'expanded the'
        return attribution
 
    def attribution_EXA(self):
        attribution = self.nwsPhrase + 'expanded the'
        return attribution
    
    def attribution_EXT(self):
        if self.geoType == 'area': # FFW_FFS, FLW_FLS area, FFA area:
            attribution = self.nwsPhrase + 'extended the'
        else: # point
            attribution = 'The ' + self.hazardName + ' is now in effect ' 
        return attribution
    
    def attribution_CON(self):
        attribution = '...The ' + self.hazardName + ' remains in effect '
        return attribution
       
    def attribution_ROU(self):
        attribution = self.nwsPhrase + 'released '
        return attribution
            
    # First Bullet        
    def getFirstBulletText(self):
        if self.action == 'CAN':
            firstBullet = self.firstBullet_CAN()
        elif self.action == 'EXP':
            firstBullet = self.firstBullet_EXP()
        elif self.action == 'UPG':
            firstBullet = self.firstBullet_UPG()
        elif self.action == 'NEW':
            firstBullet = self.firstBullet_NEW()
        elif self.action == 'EXB':
            firstBullet = self.firstBullet_EXB()
        elif self.action == 'EXA':
            firstBullet = self.firstBullet_EXA()
        elif self.action == 'EXT':
            firstBullet = self.firstBullet_EXT() 
        elif self.action == 'CON':
            firstBullet = self.firstBullet_CON()
        elif self.action == 'ROU':
            firstBullet = self.firstBullet_ROU()
        
        if self.testMode and self.geoType == 'area':
            prefix = 'This is a test message.  '
            firstBullet += '\n'
        else:
            prefix = ''
        return prefix + firstBullet + '\n'
    
    def firstBullet_CAN(self):
        if self.geoType == 'area':
            firstBullet = ''
        else:
            firstBullet = 'The ' + self.hazardName + ' is canceled for\n' + self.areaPhrase
        return firstBullet
    
    def firstBullet_EXP(self):
        firstBullet = ''
        return firstBullet

    def firstBullet_UPG(self):
        # TODO - Post-Hydro fix this
        firstBullet = self.hazardName + ' for...\n'
        firstBullet +=self.areaPhrase
        return firstBullet

    def firstBullet_NEW(self):
        if self.geoType == 'area':
            firstBullet = self.hazardName + ' for...'            
        else:
            firstBullet = self.hazardName + ' for\n'
        firstBullet += self.qualifiers()                                    
        firstBullet += self.areaPhrase
        return firstBullet

    def firstBullet_EXB(self):
        firstBullet = self.hazardName + ' to include'
        firstBullet += ' ' + self.areaPhrase
        return firstBullet
 
    def firstBullet_EXA(self):
        firstBullet = self.hazardName + ' to include'
        firstBullet += ' ' + self.areaPhrase
        return firstBullet
    
    def firstBullet_EXT(self):
        if self.productID in ['FFA'] and self.geoType == 'area':
            firstBullet = ' ' 
        elif self.geoType == 'area':
            firstBullet = self.hazardName + ' for...'
        else:
            firstBullet = 'The ' + self.hazardName + ' continues for\n'
        qualifiers = self.qualifiers()
        if qualifiers:
            firstBullet += qualifiers + '\n'
        firstBullet += self.areaPhrase
        return firstBullet
    
    def firstBullet_CON(self):
        if self.geoType == 'area' and (self.productID in ['FFA'] or self.phenSig == 'FA.W'):
            firstBullet = ' for ' 
        elif self.geoType == 'area':
            firstBullet = self.hazardName + ' for...'
        else:
            firstBullet = 'The ' + self.hazardName + ' continues for\n'
        qualifiers = self.qualifiers()
        if qualifiers:
            firstBullet += qualifiers + '\n'
        firstBullet += self.areaPhrase
        return firstBullet
       
    def firstBullet_ROU(self):
        if self.geoType == 'area':
            forStr = ' for...'
        else:
            forStr = ' for\n'
        firstBullet = self.hazardName + forStr
        firstBullet += self.areaPhrase
        return firstBullet
        
    def qualifiers(self):
        qualifiers = ''

        if self.phenSig in ['FF.A', 'FA.A'] and self.action not in ['CAN', 'EXP']:
            if self.immediateCause == 'DM':
                if self.riverName and self.damName:
                    qualifiers += 'The ' + self.riverName + ' below ' + self.damName + ' in '
                                
        elif self.phenSig in ['FF.W', 'FA.W'] and self.action not in ['CAN', 'EXP']:
            if self.immediateCause in ['ER', 'IC', 'MC', 'UU']:
                if self.warningType:
                    warningTypeStr = self.tpc.getProductStrings(self.sectionDict, self.metaData, 'warningType', self.warningType)
                    if warningTypeStr:
                        qualifiers += ' for ' + warningTypeStr + ' in...'
            elif self.immediateCause == 'DM' and self.riverName and self.damName:
                qualifiers += 'The ' + self.riverName + ' below ' + self.damName + ' in '
            elif self.subType == 'BurnScar' and self.burnScarName:
                    qualifiers += self.burnScarName + ' in ' 
            elif self.typeOfFlooding:
                qualifiers += '\n' + self.typeOfFlooding + ' in...'
                                
        elif self.phenSig == 'FA.Y' and self.action not in ['CAN', 'EXP']:
            if self.immediateCause in ['ER', 'IC']:
                if self.advisoryType:
                    qualifiers += self.advisoryType + ' '
                if self.optionalSpecificType:
                    optionalSpecificTypeStr = self.tpc.getProductStrings(self.sectionDict, self.metaData, 'optionalSpecificType', self.optionalSpecificType)
                    if optionalSpecificTypeStr:
                        qualifiers+= ' with ' +  optionalSpecificTypeStr +' in...'
            else:
                if self.advisoryType:
                    qualifiers += self.advisoryType + ' '
                if self.typeOfFlooding:
                    #TODO ProductUtils.wrapLegacy should handle the below indent
                    qualifiers += ' with ' + self.typeOfFlooding + ' in...\n'
                     
        return qualifiers

            
    # areaPhrase
    def getAreaPhraseBullet(self):
        '''
        @return: Plain language list of counties/zones in the hazard appropriate
                 for bullet format products
        ''' 
        # These need to be ordered by area of state.
        orderedUgcs = []
        for ugc in self.ugcs :
            orderedUgcs.append(ugc[:2] + self.ugcPartsOfState.get(ugc, "") + "|" + ugc)
        orderedUgcs.sort()

        areaPhrase = "\n"
        for ougc in orderedUgcs :
            ugc = ougc.split("|")[1]
            part = self.ugcPortions.get(ugc, "")
            if part == "" :
                textLine = "  "
            else :
                textLine = "  " + part + " "
            textLine += self.tpc.getInformationForUGC(ugc) + " "
            textLine += self.tpc.getInformationForUGC(ugc, "typeSingular") + " in "
            part = self.ugcPartsOfState.get(ugc, "")
            if part == "" :
                textLine += self.tpc.getInformationForUGC(ugc, "fullStateName") + "...\n"
            else :
                textLine += part + " " + self.tpc.getInformationForUGC(ugc, "fullStateName") + "...\n"
            areaPhrase += textLine

        return areaPhrase.rstrip()
           
    def getAreaPhrase(self, sectionDict):
        '''
        Central Kent County in Southwest Michigan
        This includes the cities of City1 and City2

        @param sectionDict       
        @return text describing the UGC areas and optional cities
        
        ''' 
        if self.geoType == 'area':
            ugcPhrase = self.tpc.getAreaPhrase(self.ugcs)
            if self.listOfCities:
                ugcPhrase += '\n' + self.cityString
            return ugcPhrase
        else:
            proximity = sectionDict.get('proximity', '')
            # TODO fix rfp to never return None or decide what the below default value should be
            if not proximity:
                proximity = 'near'
            return  '  the ' + sectionDict.get('riverName_GroupName', '') + ' ' + proximity + ' ' + sectionDict.get('riverPointName', '') + '.'


    # The following tables are temporarily here until we determine the best central place to keep them.        
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

    def typeOfFloodingMapping(self, immediateCuase):
        mapping = {
            'DM' : 'A levee failure',
            'DR' : 'A dam floodgate release',
            'GO' : 'A glacier-dammed lake outburst',
            'IJ' : 'An ice jam',
            'RS' : 'Extremely rapid snowmelt',
            'SM' : 'Extremely rapid snowmelt caused by volcanic eruption'
            }
        if mapping.has_key(immediateCuase):
            return mapping[immediateCuase]
        else:
            return ''

    def immediateCauseMapping(self, immediateCauseCode):
        immediateCauseDict = {"ER":"excessive rain",
                              "SM":"snowmelt",
                              "RS":"rain and snowmelt", 
                              "DM":"a dam or levee failure",
                              "DR":"a dam floodgate release",
                              "GO":"a glacier-dammed lake outburst",
                              "IJ":"an ice jam", 
                              "IC":"rain and/or snow melt and/or ice jam",
                              "FS":"upstream flooding plus storm surge", 
                              "FT":"upstream flooding plus tidal effects",
                              "ET":"elevated upstream flow plus tidal effects",
                              "WT":"wind and/or tidal effects",
                              "OT":"other effects",
                              "MC":"multiple causes",
                              "UU":"Unknown" }
        immediateCauseText = immediateCauseDict.get(immediateCauseCode, '')
        return immediateCauseText
            
    def _damInfo(self):
        from MapsDatabaseAccessor import MapsDatabaseAccessor
        mapsAccessor = MapsDatabaseAccessor()
        damInfoDict = mapsAccessor.getAllDamInundationMetadata()
        return damInfoDict

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()
               
        
    '''
    NOTE: This code is temporarily retained until it is verified that all business logic from V2 has been incorporated into V3.  
    For example, V3 was missing the attribution / firstBullet logic for EXA and EXB. That logic was then migrated from this code.
    '''
#     def initialize_v2(self, productID, productSegment, hazardEvent, vtecRecord, metaData, issueTime, testMode, wfoCity,
#                  tpc, rfp, hydrologicCauseMapping, listOfCities=False, areaPhrase=None, endString='. '):
#         self.hazardEvent = hazardEvent
#         self.vtecRecord = vtecRecord
#         self.productID = productID
#         self.productSegment = productSegment
#         self.issueTime = issueTime
#         self.testMode = testMode
#         self.wfoCity = wfoCity
#         self.tpc = tpc
#         self.rfp = rfp
#         self.endString = endString
#         self.listOfCities = listOfCities
#         self.hydrologicCauseMapping = hydrologicCauseMapping
#         
#         self.tpc.setPartOfStateInfo(hazardEvent["attributes"])
#                 
#         if not areaPhrase:
#             phen = vtecRecord.get("phen")
#             sig = vtecRecord.get("sig")
#             action = vtecRecord.get("act")
#             geoType = hazardEvent.get('geoType')
#             if phen in ["FF", "FA", "TO", "SV", "SM", "EW" , "FL" ] and \
#                 action in [ "NEW", "EXA", "EXT", "EXB" ] and \
#                 geoType == 'area' and sig != "A" :
#                 areaPhrase = self.getAreaPhraseBullet(productSegment, metaData, hazardEvent)
#             else :
#                 areaPhrase = self.getAreaPhrase(productSegment, metaData, hazardEvent)
# 
#         self.attribution, self.firstBullet = self.getAttributionPhrase(
#                    vtecRecord, hazardEvent, areaPhrase, issueTime, testMode, wfoCity)
# 
# 
# 
# class AttributionFirstBulletText_v2:    
#     def __init__(self, productID, productSegment, hazardEvent, vtecRecord, metaData, issueTime, testMode, wfoCity,
#                  tpc, rfp, hydrologicCauseMapping, listOfCities=False, areaPhrase=None, endString='. '):
#         self.hazardEvent = hazardEvent
#         self.vtecRecord = vtecRecord
#         self.productID = productID
#         self.productSegment = productSegment
#         self.issueTime = issueTime
#         self.testMode = testMode
#         self.wfoCity = wfoCity
#         self.tpc = tpc
#         self.rfp = rfp
#         self.endString = endString
#         self.listOfCities = listOfCities
#         self.hydrologicCauseMapping = hydrologicCauseMapping
#         self.geoType = hazardEvent.get('geoType')
# 
#         
#         self.tpc.setPartOfStateInfo(hazardEvent["attributes"])
#         
#         if not areaPhrase:
#             phen = vtecRecord.get("phen")
#             sig = vtecRecord.get("sig")
#             action = vtecRecord.get("act")
#             geoType = hazardEvent.get('geoType')
#             if phen in ["FF", "FA", "TO", "SV", "SM", "EW" , "FL" ] and \
#                 action in [ "NEW", "EXA", "EXT", "EXB" ] and \
#                 geoType == 'area' and sig != "A" :
#                 areaPhrase = self.getAreaPhraseBullet(productSegment, metaData, hazardEvent)
#             else :
#                 areaPhrase = self.getAreaPhrase(productSegment, metaData, hazardEvent)
# 
#         self.attribution, self.firstBullet = self.getAttributionPhrase(
#                    vtecRecord, hazardEvent, areaPhrase, issueTime, testMode, wfoCity)
# 
#     def initialize(self, sectionDict, productID, productSegment, hazardEvent, vtecRecord, metaData, issueTime, testMode, wfoCity,
#                  tpc, rfp, hydrologicCauseMapping, listOfCities=False, areaPhrase=None, endString='. '):
#         pass
# 
#     def getAttributionText(self):
#         return self.attribution
#     
#     def getFirstBulletText(self):
#         '''
#            methods based on hazard type...
#            Use current code, V2, V3, and directives.
#         '''
#         return self.firstBullet
#             
#     def getAreaPhraseBullet(self, productSegment, metaData, hazardEvent):
#         '''
#         @param productSegment object
#         @param metaData
#         @param hazardEvent -- representative for the segment
#         @return: Plain language list of counties/zones in the hazard appropriate
#                  for bullet format procducts
#         ''' 
#         ugcs = hazardEvent.get('ugcs', [])
#         ugcPortions = hazardEvent.get('ugcPortions', {})
#         ugcPartsOfState = hazardEvent.get('ugcPartsOfState', {})
# 
#         # These need to be ordered by area of state.
#         orderedUgcs = []
#         for ugc in ugcs :
#             orderedUgcs.append(ugc[:2] + ugcPartsOfState.get(ugc, "") + "|" + ugc)
#         orderedUgcs.sort()
# 
#         areaPhrase = "\n"
#         for ougc in orderedUgcs :
#             ugc = ougc.split("|")[1]
#             part = ugcPortions.get(ugc, "")
#             if part == "" :
#                 textLine = "  "
#             else :
#                 textLine = "  " + part + " "
#             textLine += self.tpc.getInformationForUGC(ugc) + " "
#             textLine += self.tpc.getInformationForUGC(ugc, "typeSingular") + " in "
#             part = ugcPartsOfState.get(ugc, "")
#             if part == "" :
#                 textLine += self.tpc.getInformationForUGC(ugc, "fullStateName") + "\n"
#             else :
#                 textLine += part + " " + self.tpc.getInformationForUGC(ugc, "fullStateName") + "\n"
#             areaPhrase += textLine
# 
#         return areaPhrase
#     
#     def getAttributionPhrase(self, vtecRecord, hazardEvent, areaPhrase, issueTime, testMode, wfoCity, lineLength=69):
#         '''
#         THE NATIONAL WEATHER SERVICE IN DENVER HAS ISSUED A
# 
#         * AREAL FLOOD WATCH FOR A PORTION OF SOUTH CENTRAL COLORADO...
#           INCLUDING THE FOLLOWING COUNTY...ALAMOSA.
#         '''
#         nwsPhrase = 'The National Weather Service in ' + wfoCity + ' has '
#         endString = self.endString
#         
#         # Attribution and 1st bullet (headPhrase)
#         #
#         headPhrase = None
#         attribution = ''
# 
#         # Use this to determine which first bullet format to use.
#         phen = vtecRecord.get("phen")
# 
#         hazName = self.tpc.hazardName(vtecRecord['hdln'], testMode, False)
# 
#         if len(vtecRecord['hdln']):
#             action = vtecRecord['act']
#             
#            # Handle special cases
#             if action == 'EXT' and self.productID in ['FFA', 'FLW', 'FLS'] and self.geoType == 'point':
#                 # Use continuing wording for EXT
#                 action = 'CON'
#                                 
#             if action == 'NEW':
#                 attribution = nwsPhrase + 'issued a'
#                 headPhrase = hazName + ' for'
#                 headPhrase += ' ' + areaPhrase + endString
#     
#             elif action == 'CON':
#                 attribution = 'the ' + hazName + ' remains in effect for'
#                 headPhrase = areaPhrase + endString
#     
#             elif action == 'EXA':
#                 attribution = nwsPhrase + 'expanded the'
#                 headPhrase = hazName + ' to include'
#                 headPhrase = ' ' + areaPhrase + endString
#     
#             elif action == 'EXT':
#                 if action in 'EXT' and self.productID in ['FFA', 'FLW', 'FLS'] and self.geoType == 'area':
#                     attribution = nwsPhrase + 'extended the '
#                 else:
#                     attribution = 'the ' + hazName + ' is now in effect for' 
#                 headPhrase = ' ' + areaPhrase + endString
#                     
#             elif action == 'EXB':
#                 attribution = nwsPhrase + 'expanded the'
#                 headPhrase = hazName + ' to include'
#                 headPhrase = ' ' + areaPhrase + endString
#     
#             elif action == 'CAN':
#                 attribution = 'the ' + hazName + \
#                    ' for ' + areaPhrase + ' has been canceled ' + endString
#     
#             elif action == 'EXP':
#                 expTimeCurrent = issueTime
#                 if vtecRecord['endTime'] <= expTimeCurrent:
#                     attribution = 'the ' + hazName + \
#                       ' for ' + areaPhrase + ' has expired ' + endString
#                 else:
#                    timeWords = self.tpc.getTimingPhrase(vtecRecord, [hazardEvent], expTimeCurrent)
#                    attribution = 'the ' + hazName + \
#                       ' for ' + areaPhrase + ' will expire ' + timeWords + endString
# 
#         if headPhrase is not None:
#             headPhrase = self.tpc.indentText(headPhrase, indentFirstString='',
#               indentNextString='  ', maxWidth=lineLength,
#               breakStrings=[' ', '-', '...'])
#         else:
#             headPhrase = ''
# 
#         return attribution, headPhrase
#        
#     def getAreaPhrase(self, productSegment, metaData, hazardEvent):
#         '''
#         Central Kent County in Southwest Michigan
#         This includes the cities of City1 and City2
# 
#         @param productSegment object
#         @param metaData
#         @param hazardEvent -- representative for the segment
#         
#         @return text describing the UGC areas and optional cities
#         
#         ''' 
#         if hazardEvent.get('geoType') == 'area':
#             immediateCause = hazardEvent.get('immediateCause')
#             ugcPhrase = self.tpc.getAreaPhrase(productSegment.ugcs)
#             if self.listOfCities:
#                 ugcPhrase += '\n' + productSegment.cityString
#                 
#             if immediateCause in ['DM', 'DR', 'GO', 'IJ', 'RS', 'SM']:
#                 hydrologicCause = hazardEvent.get('hydrologicCause')
#                 riverName = None
#                 if immediateCause == 'DM' and hydrologicCause in ['dam', 'siteImminent', 'siteFailed']:
#                     damOrLeveeName = self.tpc.getProductStrings(hazardEvent, metaData, 'damOrLeveeName')
#                     if damOrLeveeName:
#                         damInfo = self._damInfo().get(damOrLeveeName)
#                         if damInfo:
#                             riverName = damInfo.get('riverName')
#                     if not riverName or not damOrLeveeName:
#                         return ugcPhrase
#                     else:
#                         return 'The ' + riverName + ' below ' + damOrLeveeName + ' in ' + ugcPhrase
#                 else:
#                     typeOfFlooding = self.hydrologicCauseMapping(hydrologicCause, 'typeOfFlooding')
#                     return typeOfFlooding + ' in ' + ugcPhrase                
#             return ugcPhrase
#         else:
#             millis = SimulatedTime.getSystemTime().getMillis()
#             if not self.rfp:
#                 currentTime = datetime.datetime.fromtimestamp(millis / 1000)
#                 from RiverForecastPoints import RiverForecastPoints
#                 self.rfp = RiverForecastPoints(currentTime)
#              #  <River> <Proximity> <IdName> 
#             riverName = self.rfp.getGroupName(productSegment.pointID)
#             proximity = self.rfp.getRiverPointProximity(productSegment.pointID) 
#             riverPointName = self.rfp.getRiverPointName(productSegment.pointID) 
#             return  '\n the ' + riverName + ' ' + proximity + ' ' + riverPointName  
#         

