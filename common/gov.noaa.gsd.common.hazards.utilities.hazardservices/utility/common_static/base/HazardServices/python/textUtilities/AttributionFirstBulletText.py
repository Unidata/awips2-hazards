'''
    Description: Creates the attribution and firstBullet text.
     
    To reduce code redundancy and encapsulate business logic, attribution and firstBullet logic 
    has been consolidated into this module. 
    
    For the attribution and firstBullet, there are methods for each VTEC code.  
    These methods could be further broken down if needed as more is learned about Focal Point overrides 
    and as more hazard types are incorporated.

    @author Tracy.L.Hansen@noaa.gov
'''


import abc
import calendar
import datetime
import sys
import traceback

from dateutil import tz


class AttributionFirstBulletText(object):
    """Creates the attribution and first bullet text for hazard products.
    
    This is implemented as an abstract base class, and subclasses must 
    implement the following methods:
        attribution_CAN()
        attribution_EXP()
        attribution_UPG()
        attribution_NEW()
        attribution_EXB()
        attribution_EXA()
        attribution_EXT() 
        attribution_CON()
        attribution_ROU()
        firstBullet_CAN()
        firstBullet_EXP()
        firstBullet_UPG()
        firstBullet_NEW()
        firstBullet_EXB()
        firstBullet_EXA()
        firstBullet_EXT() 
        firstBullet_CON()
        firstBullet_ROU()
        getAreaPhrase()
    """
    
    __metaclass__ = abc.ABCMeta
    
    def __init__(self, sectionDict, productID, issueTime, testMode, wfoCity, tpc, timeZones=[]):
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

        self.nwsPhrase = 'The National Weather Service in ' + self.wfoCity + ' has '
        self.areaPhrase = self.getAreaPhrase()

        # Check for the replacedBy attribute
        self.replacement = False
        self.replacementName = ''
        self.replacedBy = self.hazardEventDict.get('replacedBy', None)
        if self.replacedBy:
            self.replacement = True
            self.replacementName = self.tpc.hazardName(self.replacedBy, False, True)

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
            damInfo = self._damInfo(self.damOrLeveeName)
            if damInfo:
                self.riverName = damInfo.get('riverName')
        if not self.riverName:
            self.riverName = self.hazardEventDict.get('riverName')
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

    @abc.abstractmethod
    def attribution_CAN(self):
        raise NotImplementedError

    @abc.abstractmethod
    def attribution_EXP(self):
        raise NotImplementedError

    @abc.abstractmethod
    def attribution_UPG(self):
        raise NotImplementedError

    @abc.abstractmethod
    def attribution_NEW(self):
        raise NotImplementedError

    @abc.abstractmethod
    def attribution_EXB(self):
        raise NotImplementedError
 
    @abc.abstractmethod
    def attribution_EXA(self):
        raise NotImplementedError

    @abc.abstractmethod
    def attribution_EXT(self):
        raise NotImplementedError

    @abc.abstractmethod
    def attribution_CON(self):
        raise NotImplementedError

    @abc.abstractmethod
    def attribution_ROU(self):
        raise NotImplementedError
            
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

    @abc.abstractmethod
    def firstBullet_CAN(self):
        raise NotImplementedError

    @abc.abstractmethod
    def firstBullet_EXP(self):
        raise NotImplementedError

    @abc.abstractmethod
    def firstBullet_UPG(self):
        raise NotImplementedError

    @abc.abstractmethod
    def firstBullet_NEW(self):
        raise NotImplementedError

    @abc.abstractmethod
    def firstBullet_EXB(self):
        raise NotImplementedError
 
    @abc.abstractmethod
    def firstBullet_EXA(self):
        raise NotImplementedError

    @abc.abstractmethod
    def firstBullet_EXT(self):
        raise NotImplementedError

    @abc.abstractmethod
    def firstBullet_CON(self):
        raise NotImplementedError

    @abc.abstractmethod
    def firstBullet_ROU(self):
        raise NotImplementedError

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

    @abc.abstractmethod
    def getAreaPhrase(self):
        raise NotImplementedError

    def _getAreaPhraseForPoints(self, hazardEventDict):
        proximity = hazardEventDict.get('proximity', '')
        # TODO fix rfp to never return None or decide what the below default value should be
        if not proximity:
            proximity = 'near'
        return 'the {} {} {}'.format(
                                     hazardEventDict.get('groupName', ''), 
                                     proximity, 
                                     hazardEventDict.get('riverPointName', ''))

    def _getAreaPhraseBullet(self):
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

    def _dm_river_qualifiers(self):
        qualifiers = ''

        hycType = None
        damInfo = self._damInfo(self.damOrLeveeName)
        if damInfo:
            hycType = damInfo.get("hycType")
        if hycType == None :
            hycType = 'the '+self.riverName + ' below ' + self.damOrLeveeName
            
        # Add the type of flooding to the string if available
        if self.typeOfFlooding:
            typeOfFloodingCap = self.typeOfFlooding.capitalize()
            qualifiers += typeOfFloodingCap + ' on '
            qualifiers += hycType
        else:
            qualifiers += hycType[0].upper()+hycType[1:]

        return qualifiers

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
            'snowMelt':     {'immediateCause': 'RS', 'typeOfFlooding':'extremely rapid snowmelt'}, # matches WarnGen products
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
            'RS' : '', #Blanked out to match GFE products
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

    def _damInfo(self, damOrLeveeName):
        return self.tpc.damInfoFor(self, damOrLeveeName)

    def _headlineExpireTimePhrase(self, expireTime, duration, timeZones):
        expireTime = expireTime.replace(tzinfo=tz.tzutc())
        durationInMin = duration.total_seconds() / 60
        if durationInMin >= 360:
            dateTimeFormat = "%I%M %p %Z %A"
        else:
            dateTimeFormat = "%I%M %p %Z"
        time1 = self._formatUseNoonMidnight(expireTime, dateTimeFormat, 15, timeZones[0])
        secondTimeZone = timeZones[1] if len(timeZones) > 1 else timeZones[0]
        time2 = self._formatUseNoonMidnight(expireTime, dateTimeFormat, 15, secondTimeZone)
        timeStr = self._formatTwoTimes(time1, time2)
        return timeStr.upper()    

    def _formatUseNoonMidnight(self, dateTime, dateFormat, interval, timeZone):
        workingDate = dateTime
        if interval > 0:
            workingDate = self.tpc.round(dateTime, interval)
            
        locTz = tz.gettz(timeZone)
        workingDate = workingDate.astimezone(locTz)
        
        dateTimeStr = workingDate.strftime(dateFormat)
        if dateTimeStr[0] == '0':
            dateTimeStr = dateTimeStr[1:]
        dateTimeStr = dateTimeStr.replace("1200 AM", "midnight", 1)
        dateTimeStr = dateTimeStr.replace("1200 PM", "noon", 1)
        return dateTimeStr

    def _formatTwoTimes(self, time1, time2):
        timeStr = time1
        if time2 and time1 != time2:
            timeStr += '/' + time2 + '/'
        return timeStr
