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
    Jun 2015       8530    Robert.Blum       Changes to conform to WarnGen outputs.
    Jun 2015       8532    Robert.Blum       Changes to conform to GFE/WarnGen output.
    Aug 2015       9641    Robert.Blum       Fixed duplicate "for" in first bullets.
    Aug 2015       9627    Robert.Blum       Removed canceling wording from replacements.
    Sep 2015       9590    Robert.Blum       Removed metadata from the product dictionary.
    Oct 2015       11832   Robert.Blum       Fixed commented out code that is no longer correct.
    Nov 2015       7532    Robert.Blum       Fixed spelling of cancelled.
    Dec 2015       12479   Robert.Blum       Fixed bug that was preventing the "for..." from being
                                             added to the first line of the first bullet.
    Jan 2016       12768   Kevin.Bisanz      Fix FF.W dam validation issue.
                                             Added dm_river_qualifiers().
    Mar 2016       16055   Kevin.Bisanz      Fixed call to self.tpc.getTimingPhrase(...)
                                             when expiring a product.
    Mar 2016       16039   Thomas.Gurney     Fix first line of segment in FL.* CON products
    Apr 2016       15252   Kevin.Bisanz      Add 'rain' key in hydrologicCauseMapping(...)
    May 2016       16046   Kevin.Bisanz      Referenced replacing event in firstBullet_CAN if event is replaced.
    May 2016       19080   David.Gillingham  Renamed initialize to __init__.
    Jun 2016       14069   David.Gillingham  Fix wording for FA.Y events.
    Jul 2016       19926   Kevin.Bisanz      Title cased typeOfFlooding text for
                                             FF.W, FA.W, and FA.Y in
                                             qualifiers(..);  Fixed validation
                                             error with FA.Y EXT.
    Jul 2016       19214   Kevin.Bisanz      Refactored part of getAreaPhraseBullet() into
                                             TextProductCommon.makeUGCInformation(..)
    Aug 2016       20654   Kevin.Bisanz      Update getAreaPhraseBullet to handle
                                             independent cities

    @author Tracy.L.Hansen@noaa.gov
'''
import collections, os, types, datetime
from com.raytheon.uf.common.time import SimulatedTime


class AttributionFirstBulletText(object):
    def __init__(self, sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones=[], areaPhrase=None):
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
        if self.action == 'COR':
            self.action = self.vtecRecord.get('prevAct')
        self.endTime = self.vtecRecord.get('endTime')
        prependTestToHazard = self.testMode and self.tpc.isGFEHazard(self.vtecRecord)
        self.hazardName = self.tpc.hazardName(self.vtecRecord.get('hdln'), prependTestToHazard, False)

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
#             if listOfCities:
#                 self.cityListFlag = True
#                 self.cityList.extend(hazardEventDict.get('cityList', []))

        self.nwsPhrase = 'The National Weather Service in ' + self.wfoCity + ' has '

        if not areaPhrase:
            self.areaPhrase = self.getAreaPhrase()

        # TODO - Rewrite module (mainly qualifiers method) to handle sections
        # that have multiple hazard events. There is no gaurantee that the 
        # below attributes will be the same for all the hazards in the section.
        # For now using values from first hazard.
        self.pointID = self.hazardEventDict.get('pointID')
        self.hydrologicCause = self.hazardEventDict.get('hydrologicCause')
        if self.hydrologicCause:
            self.typeOfFlooding = self.hydrologicCauseMapping(self.hydrologicCause, 'typeOfFlooding')
        else:
            self.typeOfFlooding = self.immediateCauseMapping(self.immediateCause)
        self.warningType = self.hazardEventDict.get('warningType')
        self.warningTypeStr = None
        if self.warningType:
            self.warningTypeStr = self.hazardEventDict.get('warningType_productString')
        self.advisoryType = self.hazardEventDict.get('advisoryType_productString')
        self.optionalSpecificType = self.hazardEventDict.get('optionalSpecificType')
        self.optionalSpecificTypeStr = None
        if self.optionalSpecificType:
            self.optionalSpecificTypeStr = self.hazardEventDict.get('optionalSpecificType_productString')
        self.burnScarName = self.hazardEventDict.get('burnScarName')
        self.damOrLeveeName = self.hazardEventDict.get('damOrLeveeName')
        self.riverName = None
        if self.damOrLeveeName:
            damInfo = self._damInfo().get(self.damOrLeveeName)
            if damInfo:
                self.riverName = damInfo.get('riverName')
        if not self.riverName:
            self.riverName = self.hazardEventDict.get('riverName')
        self.streamName = self.hazardEventDict.get('streamName')

        # Check for the replacedBy attribute
        self.replacement = False
        if self.hazardEventDict.get('replacedBy', None):
            self.replacement = True

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
        attribution = '...The '
        if self.replacement:
            actionWord = 'replaced'
        else:
            actionWord = 'cancelled'

        if self.phenSig in ['FA.W', 'FA.Y']:
            preQualifiers = self.preQualifiers()
            attribution += preQualifiers + self.hazardName + self.qualifiers(titleCase=False, addPreposition=False)
            attribution += ' has been ' + actionWord + ' for ' + self.areaPhrase + '...'
        else:
            attribution += self.hazardName + ' for ' + self.areaPhrase + ' has been ' + actionWord + '...'
        return attribution
    
    def attribution_EXP(self):
        expireTimeCurrent = self.issueTime
        if self.vtecRecord.get('endTime') <= expireTimeCurrent:
            expireWords = ' has expired'
        else:
            timeWords = self.tpc.getTimingPhrase(self.vtecRecord, self.hazardEventDicts, expireTimeCurrent)
            expireWords = ' will expire ' + timeWords
        attribution = '...The '
        if self.phenSig in ['FA.W', 'FA.Y']:
            preQualifiers = self.preQualifiers()
            attribution += preQualifiers + self.hazardName + self.qualifiers(titleCase=False, addPreposition=False)
            attribution += expireWords + ' for ' + self.areaPhrase
        else:
            attribution += self.hazardName + ' for ' + self.areaPhrase + expireWords + '.'
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
        attribution = '...The '
        if self.phenSig in ['FA.Y', 'FA.W']:
            if self.timeZones is not None and len(self.timeZones) > 0:
                timeStr = self.tpc.getFormattedTime(self.endTime, format='%H%M %p %Z', timeZones=self.timeZones)
            else:
                timeStr = self.tpc.getFormattedTime(self.endTime, format='%H%M %p %Z')
            preQualifiers = self.preQualifiers()
            qualifiers = self.qualifiers(titleCase=False, addPreposition=False)
            attribution += preQualifiers + self.hazardName + qualifiers
            
            attribution += ' remains in effect until ' + timeStr + ' for ' + self.areaPhrase + '...'
        else:
            attribution += self.hazardName + ' remains in effect '
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
            actionValue = ''
            if self.replacement:
                actionWord = 'replaced'
                replacedByValue = self.hazardEventDict.get('replacedBy')
                if replacedByValue:
                    actionValue = ' by a ' + replacedByValue.lower()
            else:
                actionWord = 'cancelled'
            firstBullet = 'The ' + self.hazardName + ' is ' + actionWord + actionValue + ' for\n' + self.areaPhrase
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
        preQualifiers = self.preQualifiers()
        firstBullet = preQualifiers + self.hazardName
        qualifiers = self.qualifiers(titleCase=True)
        forStr = ''
        if self.geoType == 'area':
            if self.phenSig in ['FF.W', 'FA.W', 'FA.Y']:
                if not self.optionalSpecificTypeStr and not self.warningTypeStr:
                    firstBullet += ' for...'
            else:
                forStr = ' for '
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
        preQualifiers = self.preQualifiers()
        forStr = ''
        if self.geoType == 'area':
            firstBullet = preQualifiers + self.hazardName
            if self.phenSig in ['FF.W', 'FA.W', 'FA.Y']:
                if not self.optionalSpecificTypeStr and not self.warningTypeStr:
                    firstBullet += ' for...'
            else:
                forStr = ' for '
        else:
            firstBullet = 'The ' + self.hazardName
            forStr =  ' continues for\n'
        qualifiers = self.qualifiers(titleCase=True)
        if qualifiers:
            firstBullet += qualifiers
            forStr = ''
        firstBullet += forStr + self.areaPhrase
        return firstBullet
    
    def firstBullet_CON(self):
        firstBullet = ''
        forStr = ''
        if self.geoType == 'area':
            preQualifiers = self.preQualifiers()
            firstBullet = preQualifiers + self.hazardName
            if self.phenSig == 'FF.W':
                firstBullet += ' for...'
            else:
                forStr = ' for '
        else:
            firstBullet = 'The ' + self.hazardName
            forStr =  ' continues for\n'
        qualifiers = self.qualifiers(titleCase=True, addPreposition=False)
        if qualifiers:
            firstBullet += qualifiers
        firstBullet += forStr + self.areaPhrase
        return firstBullet

    def firstBullet_ROU(self):
        if self.geoType == 'area':
            forStr = ' for...'
        else:
            forStr = ' for\n'
        firstBullet = self.hazardName + forStr
        firstBullet += self.areaPhrase
        return firstBullet

    def preQualifiers(self,):
        qualifiers = ''
        if self.phenSig == 'FA.Y':
            if self.advisoryType:
                qualifiers += self._titleCase(self.advisoryType) + ' '
        return qualifiers

    def dm_river_qualifiers(self):
        qualifiers = ''

        # Add the type of flooding to the string if available
        if self.typeOfFlooding:
            typeOfFloodingCap = self.typeOfFlooding.capitalize()
            qualifiers += typeOfFloodingCap + ' on '
            qualifiers += 'the '
        else:
            qualifiers += 'The '

        qualifiers += self.riverName + ' below ' + self.damOrLeveeName

        return qualifiers

    def qualifiers(self, titleCase, addPreposition=True):
        qualifiers = ''
        if titleCase:
            typeOfFlooding = self._titleCase(self.typeOfFlooding)
        else:
            typeOfFlooding = self.typeOfFlooding

        if self.phenSig == 'FF.W':
            if self.immediateCause == 'DM' and self.riverName and self.damOrLeveeName:
                qualifiers += '\n'
                qualifiers += self.dm_river_qualifiers()

                if addPreposition:
                    qualifiers += ' in '
            elif self.subType == 'BurnScar' and self.burnScarName:
                qualifiers += '\n' + self.burnScarName
                if addPreposition:
                    qualifiers += ' in ' 
            elif typeOfFlooding and self.immediateCause not in ['ER', 'IC', 'MC', 'UU']:
                qualifiers += '\n' + typeOfFlooding
                if addPreposition:
                    qualifiers += ' in...'

        elif self.phenSig == 'FA.W':
            if self.immediateCause in ['ER', 'IC', 'MC', 'UU']:
                if self.warningTypeStr:
                    qualifiers += ' for ' + self.warningTypeStr
                    if addPreposition:
                        qualifiers += ' in...'
            elif self.immediateCause == 'DM' and self.riverName and self.damOrLeveeName:
                qualifiers += '\n'
                qualifiers += self.dm_river_qualifiers()
                if addPreposition:
                    qualifiers += ' in '
            elif typeOfFlooding:
                if self.action in ['NEW', 'EXT']:
                    qualifiers += '\n  ' + typeOfFlooding
                else:
                    qualifiers += ' for ' + typeOfFlooding
                if addPreposition:
                    qualifiers += ' in...'

        elif self.phenSig == 'FA.Y': 
            if self.optionalSpecificTypeStr:
                qualifiers+= ' for ' +  self.optionalSpecificTypeStr.title()
            if self.immediateCause not in ['ER', 'IC']:
                if typeOfFlooding:
                    if self.action in ['NEW', 'EXT']:
                        if self.optionalSpecificTypeStr:
                            qualifiers += ' for..\n  ' + typeOfFlooding
                        else:
                            qualifiers += '\n  ' + typeOfFlooding
                    else:
                        qualifiers += ' for ' + typeOfFlooding
                    if addPreposition:
                        qualifiers += ' in...'
            else:
                if qualifiers and addPreposition:
                    qualifiers += ' for...'
        return qualifiers

    def _titleCase(self, inputString):
        '''
        Given an input string, return the title case version of that string. 
        This is a simple title case algorithm which capitalizes the first word
        and every word not in a list of exceptions.
        
        Note:  Leading and trailing spaces are removed.  Multiple spaces are
        collapsed to a single space.

        @param inputString
        @return The title cased string.
        '''
        # Lists are from section 3.49 of the U.S. Government Printing
        # Office Style Manual:
        # https://www.gpo.gov/fdsys/pkg/GPO-STYLEMANUAL-2008/pdf/GPO-STYLEMANUAL-2008.pdf
        articles = ['a','an','the']
        prepositions = ['at','by','for','in','of','on','to','up']
        conjunctions = ['and','as','but','if','or','not']

        # Extra items for Hazard Services.
        extra=['and/or']

        # Words that should not be capitalized.
        exceptions = []
        exceptions.extend(articles)
        exceptions.extend(prepositions)
        exceptions.extend(conjunctions)
        exceptions.extend(extra)

        capWords = []

        # Capitalize the first word and every word not in the exception list.
        if inputString:
            firstWord = True
            words = inputString.split()
            for word in words:
                if firstWord or word not in exceptions:
                    capWords.append(word.capitalize())
                else:
                    capWords.append(word)
                firstWord = False

        # Rejoin everything with spaces.
        return ' '.join(capWords)

    # areaPhrase
    def getAreaPhrase(self):
        if self.geoType != 'point' and ( self.productID in ['FFA', 'FFS'] or 
        (self.phenSig in ['FA.Y', 'FA.W'] and self.action not in ['NEW', 'EXT'])):
            areaPhrase = self.tpc.getAreaPhrase(self.vtecRecord.get('id'))
        elif self.productID == 'FFW':
            areaPhrase = self.getAreaPhraseBullet()
        elif self.phenSig in ['FA.W', 'FA.Y']:
            areaPhrase = self.getAreaPhraseBullet()
        elif self.phen in 'FL' or self.phenSig == 'HY.S':
            areaPhrase = self.getAreaPhraseForPoints(self.hazardEventDict)
        else:
            areaPhrase = ''
        return areaPhrase.rstrip()

    def getAreaPhraseBullet(self):
        '''
        @return: Plain language list of counties/zones in the hazard(s) appropriate
                 for bullet format products
        ''' 
        locationDicts = self.hazardEventDict['locationDicts']

        areaPhrase = ""
        # For each UGC, build a string like:
        # Southeastern Montgomery County in central Maryland
        for entry in locationDicts :
            ugc = entry["ugc"]
            ugcType = entry["typeSingular"]
            independentCityFlag = ugcType.startswith("independent city")

            pieces = []

            # Independent city lines start with "The"
            if independentCityFlag:
                pieces.append("The")

            # Add the portion(s) of the UGC.  E.g. Southeastern
            portionsOfUgc = entry.get('ugcPortions', '')
            if portionsOfUgc:
                pieces.append(self.tpc.formatDelimitedList(portionsOfUgc, ', '))

            # Add the name.  E.g. Montgomery
            pieces.append(entry.get('entityName'))

            # Add the ugcType.  E.g. County
            # Independent cities don't have a ugcType on the line.
            if ugcType and not independentCityFlag:
                pieces.append(ugcType)

            # Add the part of state and the state.  E.g. central Maryland
            partOfState = entry.get('ugcPartsOfState', '')
            state = entry.get('fullStateName', '')
            if state:
                if partOfState:
                    pieces.append("in")
                    pieces.append(partOfState)
                    pieces.append(state)
                else :
                    pieces.append("in")
                    pieces.append(state)

            textLine = "\n" + " ".join(pieces) + "..."

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
            'dam':          {'immediateCause': 'DM', 'typeOfFlooding':'a dam failure'},
            'siteImminent': {'immediateCause': 'DM', 'typeOfFlooding':'a dam break'},
            'siteFailed':   {'immediateCause': 'DM', 'typeOfFlooding':'a dam break'},
            'levee':        {'immediateCause': 'DM', 'typeOfFlooding':'a levee failure'},
            'floodgate':    {'immediateCause': 'DR', 'typeOfFlooding':'a dam floodgate release'},
            'glacier':      {'immediateCause': 'GO', 'typeOfFlooding':'a glacier-dammed lake outburst'},
            'icejam':       {'immediateCause': 'IJ', 'typeOfFlooding':'an ice jam'},
            'rain':         {'immediateCause': 'RS', 'typeOfFlooding':'rain and snowmelt'},
            'snowMelt':     {'immediateCause': 'RS', 'typeOfFlooding':'extremely rapid snowmelt'},
            'volcano':      {'immediateCause': 'SM', 'typeOfFlooding':'extremely rapid snowmelt caused by volcanic eruption'},
            'volcanoLahar': {'immediateCause': 'SM', 'typeOfFlooding':'volcanic induced debris flow'},
            'default':      {'immediateCause': 'ER', 'typeOfFlooding':'excessive rain'}
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
