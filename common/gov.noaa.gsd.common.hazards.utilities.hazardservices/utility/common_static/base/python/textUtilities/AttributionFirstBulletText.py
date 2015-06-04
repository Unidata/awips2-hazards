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
    Apr 2015       7375    Robert.Blum       Fixed first bullet to include hazard type
                                             for FFA area-EXT hazards.
    Apr 2015       7579    Robert.Blum       Removed some '\n', they are added in the
                                             formatter now.
    Apr 2015       7140    Tracy Hansen      Adding FA.W / FA.Y / FL.Y /  logic 
    Apr 2015       7579    Robert.Blum       Changes for multiple hazards per section.
    May 2015       7376    Robert.Blum       Fixed burnscar error.
    May 2015       7959    Robert.Blum       Consolidated the Dam/Levee name into one attribute.
    May 2015       8181    Robert.Blum       Minor changes to listOfCities.
    Jun 2015       8530    Robert.Blum       Corrected first bullet for FF.W products.
    Jun 05, 2015   8531    Chris.Cody        Changes to conform to WarnGen/RiverPro outputs
    @author Tracy.L.Hansen@noaa.gov
'''
import collections, os, types, datetime
from com.raytheon.uf.common.time import SimulatedTime


class AttributionFirstBulletText(object):

    def initialize(self, sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones=[], areaPhrase=None):
        # Variables to establish
        self.sectionDict = sectionDict
        self.productID = productID
        self.issueTime = issueTime
        self.testMode = testMode
        self.wfoCity = wfoCity
        self.tpc = tpc
        self.timeZones = timeZones
        self.vtecRecord = sectionDict.get('vtecRecord')
        self.phen = self.vtecRecord.get("phen")
        self.sig = self.vtecRecord.get("sig")
        self.phenSig = self.vtecRecord.get('phensig')
        self.subType = self.vtecRecord.get('subtype')
        self.action = self.vtecRecord.get('act')
        self.endTime = self.vtecRecord.get('endTime')
        self.hazardName = self.tpc.hazardName(self.vtecRecord.get('hdln'), testMode, False)

        # Assume that the following attributes are the same
        # for all the hazards in the section
        self.hazardEventDicts = sectionDict.get('hazardEvents', [])
        self.hazardEventDict = self.hazardEventDicts[0]
        self.geoType = self.hazardEventDict.get('geoType')
        self.immediateCause = self.hazardEventDict.get('immediateCause')

        # The below list of cities matches the directives but not WarnGen.
        # Also it uses the "Select for a list of cities" checkbox on the HID.
        # WarnGen has this same selection but it toggles the 4th bullet.
        # Commenting this out so the HID checkbox can be repurposed to toggle
        # the 4th bullet to match WarnGen.

#         self.cityList = []
#         self.cityListFlag = False
#         for hazardEventDict in self.hazardEventDicts:
#             listOfCities = hazardEventDict.get('listOfCities', [])
#             if 'selectListOfCities'in listOfCities:
#                 self.cityListFlag = True
#                 self.cityList.extend(hazardEventDict.get('cityList', []))

        self.nwsPhrase = 'The National Weather Service in ' + self.wfoCity + ' has '

        if not areaPhrase:
            if self.productID in ['FFA', 'FFS'] and self.geoType != 'point':
                self.areaPhrase = self.tpc.getAreaPhrase(sectionDict.get('ugcs'))
            elif self.productID == 'FFW':
                self.areaPhrase = self.getAreaPhraseBullet(optionalCities=True)
            elif self.phenSig in ['FA.W', 'FA.Y']:
                self.areaPhrase = self.getAreaPhraseBullet()
            elif self.phen in 'FL' or self.phenSig == 'HY.S':
                self.areaPhrase = self.getAreaPhraseForPoints(self.hazardEventDict)
            else:
                self.areaPhrase = ''
            self.areaPhrase.rstrip()

        # TODO - Rewrite module (mainly qualifiers method) to handle sections
        # that have multiple hazard events. There is no gaurantee that the 
        # below attributes will be the same for all the hazards in the section.
        # For now using values from first hazard.
        self.metaData = self.hazardEventDict.get('metaData')
        self.pointID = self.hazardEventDict.get('pointID')
        self.hydrologicCause = self.hazardEventDict.get('hydrologicCause')
        if self.hydrologicCause:
            self.typeOfFlooding = self.hydrologicCauseMapping(self.hydrologicCause, 'typeOfFlooding')
        else:
            self.typeOfFlooding = self.immediateCauseMapping(self.immediateCause)
        self.warningType = self.hazardEventDict.get('warningType')
        self.advisoryType = self.hazardEventDict.get('advisoryType_productString')
        self.optionalSpecificType = self.hazardEventDict.get('optionalSpecificType')
        self.burnScarName = self.hazardEventDict.get('burnScarText')
        self.damOrLeveeName = self.hazardEventDict.get('damOrLeveeName')
        self.riverName = None
        if self.damOrLeveeName:
            damInfo = self._damInfo().get(self.damOrLeveeName)
            if damInfo:
                self.riverName = damInfo.get('riverName')
        self.streamName = self.hazardEventDict.get('streamName')

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
        return attribution
    
    def attribution_CAN(self):
        attribution = '...The ' + self.hazardName
        if self.phenSig in ['FA.W', 'FA.Y']:
            attribution += self.qualifiers(addPreposition=False)
            attribution += ' has been canceled for...' + self.areaPhrase
        else:
            attribution += ' for ' + self.areaPhrase + ' has been canceled...'
        return attribution
    
    def attribution_EXP(self):
        expireTimeCurrent = self.issueTime
        if self.vtecRecord.get('endTime') <= expireTimeCurrent:
            expireWords = ' has expired'
        else:
            timeWords = self.tpc.getTimingPhrase(self.vtecRecord, [self.sectionDict], expireTimeCurrent)
            expireWords = ' will expire ' + timeWords            
        attribution = '...The ' + self.hazardName
        if self.phenSig in ['FA.W', 'FA.Y']:
            attribution += self.qualifiers(addPreposition=False)
            attribution += expireWords + ' for ' + self.areaPhrase
        else:
            attribution += ' for ' + self.areaPhrase + expireWords + '.'
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
        attribution = '...The ' + self.hazardName 
        if self.phenSig in ['FA.Y', 'FA.W']:
            if self.timeZones is not None and len(self.timeZones) > 0:
                timeStr = self.tpc.getFormattedTime(self.endTime, format='%H%M %p %Z', timeZones=self.timeZones)
            else:
                timeStr = self.tpc.getFormattedTime(self.endTime, format='%H%M %p %Z')
            continueStr = ' remains in effect until ' + timeStr
            forStr = ' for...'
            qualifiers = self.qualifiers(addPreposition=False)
            if qualifiers:
                attribution += qualifiers
            attribution += continueStr + forStr + self.areaPhrase
        else:
            attribution += ' remains in effect '
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

        return firstBullet

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
        firstBullet = self.hazardName
        qualifiers = self.qualifiers()
        forStr = ''
        if self.geoType == 'area':
            if self.phenSig == 'FF.W':
                firstBullet += ' for...'
            else:
                forStr = ' for...'
        else:
            forStr =  ' for\n'
        if qualifiers:
            firstBullet += qualifiers
            forStr = ''
        firstBullet += forStr + self.areaPhrase
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
        forStr = ''
        if self.geoType == 'area':
            firstBullet = self.hazardName
            if self.phenSig == 'FF.W':
                firstBullet += ' for...'
            else:
                forStr = ' for...'
        else:
            firstBullet = 'The ' + self.hazardName
            forStr =  ' continues for\n'
        qualifiers = self.qualifiers()
        if qualifiers:
            firstBullet += qualifiers + '\n'
            forStr = ''
        firstBullet += forStr + self.areaPhrase
        return firstBullet
    
    def firstBullet_CON(self):
        firstBullet = ''
        forStr = ''
        if self.geoType == 'area':
            firstBullet = self.hazardName
            if self.phenSig == 'FF.W':
                firstBullet += ' for...'
            else:
                forStr = ' for...'
        else:
            firstBullet = 'The ' + self.hazardName
            forStr =  ' for\n'
        qualifiers = self.qualifiers(addPreposition=False)
        if qualifiers:
            firstBullet += qualifiers
        firstBullet += ' continues ' + forStr + self.areaPhrase
        return firstBullet

    def firstBullet_ROU(self):
        if self.geoType == 'area':
            forStr = ' for...'
        else:
            forStr = ' for\n'
        firstBullet = self.hazardName + forStr
        firstBullet += self.areaPhrase
        return firstBullet
        
    def qualifiers(self, addPreposition=True):
        qualifiers = ''

        if self.phenSig in ['FF.A', 'FA.A']:
            if self.immediateCause == 'DM':
                if self.riverName and self.damOrLeveeName:
                    qualifiers += ' for...\nThe ' + self.riverName + ' below ' + self.damOrLeveeName
                    if addPreposition:
                        qualifiers += ' in '
                                
        elif self.phenSig in ['FF.W', 'FA.W']:
            if self.immediateCause in ['ER', 'IC', 'MC', 'UU']:
                if self.warningType:
                    warningTypeStr = self.tpc.getProductStrings(self.sectionDict, self.metaData, 'warningType', self.warningType)
                    if warningTypeStr:
                        qualifiers += ' for ' + warningTypeStr
                        if addPreposition:
                            qualifiers += ' in...'
            elif self.immediateCause == 'DM' and self.riverName and self.damOrLeveeName:
                qualifiers += ' for...\nThe ' + self.riverName + ' below ' + self.damOrLeveeName
                if addPreposition:
                    qualifiers += ' in '
            elif self.subType == 'BurnScar' and self.burnScarName:
                    qualifiers += self.burnScarName
                    if addPreposition:
                        qualifiers += ' in ' 
            elif self.typeOfFlooding:
                qualifiers += '\n' + self.typeOfFlooding
                if addPreposition:
                    qualifiers += ' in...'
                                
        elif self.phenSig == 'FA.Y': 
            if self.immediateCause in ['ER', 'IC']:
                if self.advisoryType:
                    qualifiers += self.advisoryType + ' '
                if self.optionalSpecificType:
                    optionalSpecificTypeStr = self.tpc.getProductStrings(self.sectionDict, self.metaData, 'optionalSpecificType', self.optionalSpecificType)
                    if optionalSpecificTypeStr:
                        qualifiers+= ' with ' +  optionalSpecificTypeStr
                        if addPreposition:
                            qualifiers += ' in...'
            else:
                if self.advisoryType:
                    qualifiers += self.advisoryType + ' '
                if self.typeOfFlooding:
                    #TODO ProductUtils.wrapLegacy should handle the below indent
                    qualifiers += ' with ' + self.typeOfFlooding
                    if addPreposition:
                        qualifiers += ' in...\n'
                     
        return qualifiers

    # areaPhrase
    def getAreaPhraseBullet(self, optionalCities=False):
        '''
        @return: Plain language list of counties/zones in the hazard(s) appropriate
                 for bullet format products
        ''' 
        # These need to be ordered by area of state.
        orderedUgcs = []
        portions = {}
        ugcPartsOfState = {}
        for hazardEventDict in self.hazardEventDicts:
            ugcs = hazardEventDict.get('ugcs', [])
            ugcPortions = hazardEventDict.get('ugcPortions', {})
            ugcPartsOfState.update(hazardEventDict.get('ugcPartsOfState', {}))
            for ugc in ugcs:
                currentPortion = portions.get(ugc)
                if not currentPortion:
                    currentPortion = set()
                currentPortion.update([ugcPortions.get(ugc)])
                portions[ugc] = currentPortion
                orderedUgcs.append(ugc[:2] + ugcPartsOfState.get(ugc, "") + "|" + ugc)
        orderedUgcs.sort()

        areaPhrase = ""
        for ougc in orderedUgcs :
            ugc = ougc.split("|")[1]
            part = portions.get(ugc, '')
            textLine = '\n'
            if part == ""  or part == None:
                textLine += "  "
            else :
                size = len(part)
                counter = 0
                textLine += "  "
                for portion in part:
                    textLine += portion
                    if size > 1:
                        if counter < size - 2:
                            textLine += ', '
                        elif counter < size - 1:
                            textLine += ' and '
                    counter += 1
                textLine += " "
            textLine += self.tpc.getInformationForUGC(ugc) + " "
            textLine += self.tpc.getInformationForUGC(ugc, "typeSingular") + " in "
            part = ugcPartsOfState.get(ugc, "")
            if part == "" :
                textLine += self.tpc.getInformationForUGC(ugc, "fullStateName") + "..."
            else :
                textLine += part + " " + self.tpc.getInformationForUGC(ugc, "fullStateName") + "..."
            areaPhrase += textLine

        # The below list of cities matches the directives but not WarnGen.
        # Also it uses the "Select for a list of cities" checkbox on the HID.
        # WarnGen has this same selection but it toggles the 4th bullet.
        # Commenting this out so the HID checkbox can be repurposed to toggle
        # the 4th bullet to match WarnGen.
 
#         if optionalCities and self.cityListFlag and self.cityList and self.phenSig == 'FF.W':
#             cities = '\n  This includes the cities of '
#             cities += self.tpc.getTextListStr(self.cityList) + '.'
#             areaPhrase += cities

        return areaPhrase

    def getAreaPhraseForPoints(self, hazardEventDict):
        proximity = hazardEventDict.get('proximity', '')
        # TODO fix rfp to never return None or decide what the below default value should be
        if not proximity:
            proximity = 'near'
        return  '  the ' + hazardEventDict.get('riverName_GroupName', '') + ' ' + proximity + ' ' + hazardEventDict.get('riverPointName', '') + '.'


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
