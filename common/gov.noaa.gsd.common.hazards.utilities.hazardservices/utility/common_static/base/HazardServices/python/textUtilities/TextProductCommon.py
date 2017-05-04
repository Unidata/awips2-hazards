'''
   Description: Provides classes and methods for Product Generation

    @author Tracy.L.Hansen@noaa.gov
'''

import cPickle, os, types, string, copy
import sys, gzip, time, re
import logging, UFStatusHandler
from datetime import datetime
from datetime import timedelta
from dateutil import tz
import EventFactory
import GeometryFactory
import JUtil
import VTECConstants
from KeyInfo import KeyInfo
import ProductTextUtil
from shapely.geometry import Polygon
from ufpy.dataaccess import DataAccessLayer

import json
import traceback

import re

# The size of the buffer for default flood polygons.
DEFAULT_POLYGON_BUFFER = 0.05

class TextProductCommon(object):
    
    '''
    The methods in this class are part of the first cut at a complete Product Generator
    infrastructure.  As development continues, this code will be leveraged
    and refined the Hazard Services Product Generators mature.
    
    Much of this code was taken for re-use from AWIPS II GHG so that functionality
    could be leveraged.  It has been modified as needed to work with the new 
    Hazard Services paradigm.
    
    NOTE : Times are in ms -- vtecRecords are assumed to have been converted to ms.
    
    '''      
    def __init__(self):
        self._partOfStateInfo = {}
        self.currentInfoUGC = None

    def setUp(self, areaDict): 
        self._areaDictionary = areaDict
        self._root = None
        self.logger = logging.getLogger('TextProductCommon')
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            'gov.noaa.gsd.common.utilities', 'TextProductCommon', level=logging.INFO))
        self.logger.setLevel(logging.INFO)

    def setSiteID(self, siteID):
        self._siteID = siteID

    # Here we add some methods that are a level of abstraction around the 
    # areaDictionary.  Later if we want we can swap this functionality out with
    # DAF calls, or whatever.
    def getInformationForUGC(self, ugc, infoType="entityName") :
        '''
        @summary: Returns information about the given ugc.
        @param ugc: ugc, in SSX001 format, where SS is the state abreviation,
                    X is either C for county or Z for zone, and 001 is the FIP
                    code for that county or zone.
        @param infoType: Type of information to return.
        @return: Based on the value for infoType, return the following
            "entityName"         Full plain language name of county or zone
            "typeSingular"       Plain language description of type of entity
            "typePlural"         Plain language description of type of entities
            "type"               Indicator of type of entity
            "primaryLocations"   Important associated cities/landmarks
            "fullStateName"      Plain language name of state entity is in
            "timeZone"           Unix time zone entity is in
            "stateAbrev"         Two letter abreviation of state entity is in
            "partOfState"        Plain language description of part of the state
                                 entity is in
        '''
        if ugc != self.currentInfoUGC :
            self.currentUGCentry = self._areaDictionary.get(ugc)
        if infoType[:4] == "type" :
            if ugc[2] == 'Z':
                if infoType == 'typeSingular' :
                    return 'zone'
                if infoType == 'typePlural' :
                    return 'zones'
                return 'ZONE'
            if ugc[2] != 'C':
                if infoType == 'typeSingular' or infoType == 'typePlural' :
                    return ''
                return '?'
            if int(ugc[3:]) >= 500 or ugc[:2] == "DC" :
                if infoType == 'typeSingular' or infoType == 'typePlural' :
                    return ''
                return 'INDEPENDENT CITY'
            if ugc[:2] == 'LA':
                if infoType == 'typeSingular' :
                    return 'Parish'
                if infoType == 'typePlural' :
                    return 'Parishes'
                return 'PARISH'
            if infoType == 'typeSingular' :
                return 'County'
            if infoType == 'typePlural' :
                return 'Counties'
            return 'COUNTY'
        if infoType == "partOfState" :
            part = self._partOfStateInfo.get(ugc)
            if not (part is None) :
                return part
        if self.currentUGCentry is None :
            if infoType == "stateAbrev" :
                return ugc[:2]
            return ""
        if infoType == "entityName" :
            return self.currentUGCentry.get("ugcName", "")
        if infoType == "primaryLocations" :
            return self.currentUGCentry.get("ugcCityString", "")
        if infoType == "fullStateName" :
            return self.currentUGCentry.get("fullStateName", "")
        if infoType == "timeZone" :
            return self.currentUGCentry.get("ugcTimeZone", "")
        if infoType == "stateAbrev" :
            return self.currentUGCentry.get("stateAbbr", ugc[:2])
        if infoType == "partOfState" :
            return self.currentUGCentry.get("partOfState", "")
        return ""

    # This allow the TextProductCommon class to be seeded with parts of state info
    # from a hazardEvent.
    def setPartOfStateInfo(self, hazardEventAtts) :
        if "ugcPartsOfState" in hazardEventAtts :
            self._partOfStateInfo.update(hazardEventAtts["ugcPartsOfState"])
            

   # Data Access methods
    def mapDataQuery(self, tableName, columns, geometry=None, intersect=False,
                     queryMatch=None, matchColumn=None) :
        '''
        This routine attempts to consolidate many of the common steps required to
        make meaningful queries for static GIS type data from the Data Access
        Framework

        @param  tableName    specific table within the 'mapdata' schema of the
                             'maps' database to retrieve data from
        @param  columns      list of the attributes to retrieve with each returned
                             JGeometryData.
        @param  geometry     If not None, will only return items that intersect
                             that geometry. Can be a single shapely Polygon,
                             a single Geometry, or a GeometryCollection.
        @param  overlap      By default, returned geometries must be within the
                             supplied geometry, if true they only must overlap.
        @param  intersect    By default, returned shapes must be totally within
                             the supplied geometry, if this is True they only must
                             intersect.
        @param  queryMatch   If filtering the query according to the value of
                             one of the columns, the attribute value that must
                             be matched.
        @param  matchColumn  By default, the columns[0] attribute must match the
                             queryMatch value.  matchColumn can supply the id or
                             index of a different attribute to filter the query by.
        '''
        if geometry is None :
            geoList = [ None ]
        else :
            try :
                geoList = geometry.geoms
            except :
                geoList = [ geometry ]

        returnList = []

        if queryMatch :
            if matchColumn not in columns :
                try :
                    matchColumn = columns[matchColumn]
                except :
                    matchColumn = columns[0]

        for geom in geoList :
            if geom is not None and not isinstance(geom, Polygon) :
                continue
            try :
                req = DataAccessLayer.newDataRequest()
                req.setDatatype("maps")
                req.addIdentifier("table", "mapdata." + tableName)
                req.addIdentifier("geomField", "the_geom")
                if queryMatch :
                    req.addIdentifier("inLocation", "true")
                    req.addIdentifier("locationField", matchColumn)
                    req.setLocationNames(queryMatch)
                    # Can not have locationField added as parameter
                    columns.remove(matchColumn)
                else :
                    # Even if not filtering on attribute values, you must
                    # add this to the query or the attribute values will get
                    # out of sync with the attribute keys in what comes back
                    req.addIdentifier("locationField", columns[0])
                    # Can not have locationField added as parameter
                    columns.remove(columns[0])
                if geom is not None :
                    req.setEnvelope(geom.envelope)
                req.setParameters(*columns)
                retGeoms = DataAccessLayer.getGeometryData(req)
                if geom is None :
                    returnList.extend(retGeoms)
                    continue
                for retGeom in retGeoms :
                    retGeomGeom = retGeom.getGeometry()
                    if intersect :
                        if geom.intersects(retGeomGeom) :
                            returnList.append(retGeom)
                    elif geom.contains(retGeomGeom) :
                        returnList.append(retGeom)
            except :
                tbData = traceback.format_exc()
                sys.stderr.write("\n")
                sys.stderr.write("For " + tableName + "\n")
                sys.stderr.write(tbData + "\n")
                pass

        return returnList
    


    #### Product Dictionary methods 

    def getVal(self, dictionary, key, default=None, altDict=None):
        '''
        Convenience method to access dictionary keys and account for :skip and :editable suffixes
        
        @param dictionary 
        @param key, potentially without a suffix e.g. 'info'
        @return the key value accounting for suffixes e.g. 'info:skip'
        '''        
        if dictionary.get(key): 
            return dictionary.get(key)
        elif len(KeyInfo.getElements(key, dictionary)) > 0:
            elements = KeyInfo.getElements(key, dictionary)
            return dictionary[elements[0]]
        if altDict and altDict.get(key):
            return altDict.get(key)
        return default

    def getSavedVal(self, key, eventIDs=None, segment=None, productCategory=None, productID=None):
        '''
        Retrieves any user edited text from the productText table if available.
        Otherwise it returns None.
        '''
        value = None
        if None in [key, productCategory, productID, eventIDs, segment]:
            return None
        productTextList = ProductTextUtil.retrieveProductText(key, productCategory, productID, segment, eventIDs)
        if len(productTextList) > 0:
            value = productTextList[0].getValue()
        return value

    def setVal(self, dictionary, key, value, editable=False, eventIDs=None, segment=None,
                  label=None, productCategory=None, productID=None, displayable=False,
                  required=True, displayLabel=True, useKeyAsLabel=False):
        '''
        Utility method to add a value to a dictionary. It will create the KeyInfo key
        if editable or displayable are set to true. Otherwise it adds the value to 
        dictionary using the default key provided.
        '''
        if editable:
            userEditedKey = KeyInfo(key, productCategory, productID, eventIDs, segment, True, 
                                    label=label, eventIDInLabel=True, required=required,
                                    displayLabel=displayLabel)
        elif displayable:
            if useKeyAsLabel: label = key
            if label is None: label = ''
            userEditedKey = KeyInfo(key, productCategory, productID, eventIDs, segment, editable=False,
                                    displayable=True, label=label, eventIDInLabel=True, required=required,
                                    displayLabel=displayLabel)
        else:
            userEditedKey = key

        dictionary[userEditedKey] = value

    def parameterSetupForKeyInfo(self, dictionary):
        '''
        Utility method for preparing the eventIDs and UGCs to be 
        passed into the KeyInfo constructor. This method determines
        which level of dictionary is passed in (product, segment, section)
        then determines the eventID and ugcs based on that.
        '''
        ugcs = set()
        eventIDs = set()
        if dictionary.get('segments', None):
            # Product Level
            for segment in dictionary.get('segments'):
                ugcs.update(set(segment.get('ugcs', [])))
                for vtecRecord in segment.get('vtecRecords', []):
                    eventIDs.update(set(vtecRecord.get('eventID')))
        elif dictionary.get('sections', None):
            # Segment Level
            ugcs.update(set(dictionary.get('ugcs', [])))
            for vtecRecord in dictionary.get('vtecRecords', []):
                eventIDs.update(set(vtecRecord.get('eventID')))
        elif dictionary.get('vtecRecord',None):
            # Section Level
            ugcs.update(set(dictionary.get('vtecRecord').get('id')))
            eventIDs.update(set(dictionary.get('vtecRecord').get('eventID')))
        else:
            # RVS
            ugcs.update(set(dictionary.get('ugcs', [])))
            eventIDs.update(set(dictionary.get('eventIDs', [])))
        tmpEventIDs = []
        for eventID in eventIDs:
            tmpEventIDs.append(eventID)

        ugcs = list(ugcs)
        ugcs.sort()
        ugcList = ''
        for ugc in ugcs:
            if len(ugcList) > 0:
                ugcList += ', '
            ugcList += ugc
        ugcList = str(ugcList)
        return tmpEventIDs, ugcList

    # ## Formatting helper methods    
                
    def getFormattedTime(self, time_ms, format='%I%M %p %Z %a %b %d %Y',
                        upperCase=False, stripLeading=True, timeZones=['GMT']):  
        text = ''  
        for timeZone in timeZones:
            timeStr = self.formatDatetime(time_ms, format, timeZone)
            if stripLeading and (timeStr[0] == '0' or timeStr[0] == ' '):
                timeStr = timeStr[1:]
            if upperCase:
                timeStr = string.upper(timeStr)
            text += timeStr
        return text

    def formatDatetime(self, dt, format='ISO', timeZone=None):
        '''
        @param dt: datetime object
        @param format: format string e.g. '%H%M %p %Z %a %e %b %Y'
        @param timeZone: time zone e.g.'CST7CDT'.   If None use UTC 
        @return datetime formatted with time zone e.g. '1400 PM CST Mon 12 Feb 2011'
        '''
        if type(dt) in [float, int, long]:
            dt = datetime.fromtimestamp(dt / 1000)
        if timeZone in ['GMT', 'UTC']:
            timeZone = None
        
        from_zone = tz.tzutc()
        new_time = dt.replace(tzinfo=from_zone)
        if timeZone is not None:
            to_zone = tz.gettz(timeZone)
            new_time = new_time.astimezone(to_zone)            
        strTime = new_time.isoformat() if format == 'ISO' else new_time.strftime(format)
        strTime = re.sub(r"\s+", " ", strTime)
        return strTime  
                        
    def reversePolygon(self, polygon):
        '''
        Reverse lat/lon polygon to lon/lat
        '''
        reverse = []
        for lat, lon in polygon:
            reverse.append((lon, lat))
        # Add first point to end so first point matches last point
        lat, lon = polygon[0]
        reverse.append((lon, lat))
        return reverse

    def formatUGCs(self, ugcs, expireTime):
        '''
        Create ugc header with expire time
        'COC123-112330-'        
        '''
        ugcStr = self.makeUGCString(ugcs)
        ddhhmmTime = self.getFormattedTime(expireTime, '%d%H%M', stripLeading=0)
        ugcStr = ugcStr + '-' + ddhhmmTime + '-'
        return ugcStr

    def makeUGCString(self, ugcs):
        '''
        Create the UGC string for product / segment headers.
        '''
        # if nothing in the list, return empty string
        if len(ugcs) == 0:
            return ''
        ugcList = copy.deepcopy(ugcs)
        # Remove any blank UGC lines from the list
        listsize = len(ugcList)
        j = 0
        while j < listsize:
            if ugcList[j] == '':
                del ugcList[j]
            j = j + 1

        # Set up state variables and process initialize ugcStr with first ugc
        # in ugcList
        inSeq = 0
        ugcStr = ugcList[0]
        curState = ugcStr[0:3]
        lastNum = int(ugcList[0][3:])
        firstNum = 0
        lastUgc = ugcList[0]

        # By initializing properly we don't need the first item
        ugcList.remove(ugcList[0])

        for ugc in ugcList:
            ugcState = ugc[:3]
            ugcNumStr = ugc[3:]
            num = int(ugcNumStr)
            if ugcState == curState:
                if num == lastNum + 1:
                    if inSeq > 0:
                        # Replace the last ugcNumStr in sequence with the
                        # current ugcNumStr
                        # e.g.   062>063  becomes 062>064
                        ugcStr = ugcStr[:len(ugcStr) - 3] + ugcNumStr
                        inSeq += 1
                    else:
                        ugcStr += '>' + ugcNumStr
                        inSeq = 1
                else:  # num != lastNum + 1
                    ugcStr = self.checkLastArrow(inSeq, ugcStr)
                    inSeq = 0  # reset sequence when number not in sequence
                    ugcStr += '-' + ugcNumStr
            else:
                ugcStr = self.checkLastArrow(inSeq, ugcStr)
                ugcStr += '-' + ugc
                curState = ugcState
                inSeq = 0  # reset sequence when switching states
            lastNum = num
            lastUgc = ugc

        # May have to clean up last arrow at the end
        ugcStr = self.checkLastArrow(inSeq, ugcStr)
        return ugcStr
 
    def formatUGC_names(self, ugcs, alphabetize=False, separator='-'):
        '''
        For example: Saunders-Douglas-Sarpy-Lancaster-Cass-Otoe-
        '''
        nameList = []
        for ugc in ugcs:
            nameList.append(self.getInformationForUGC(ugc, "entityName"))
        if alphabetize:
            nameList.sort()                            
        return self.formatNameString(nameList, separator) 
    
    def formatNameString(self, nameList, separator, state=None) :
        nameString = ''
        for name in nameList:
            nameString += name + separator
        if state:
            nameString = nameString.rstrip(separator) + ' (' + state + ') '
        return nameString
    
    def formatUGC_namesWithState(self, ugcs, alphabetize=False, separator='; '):
        '''
        For example: Citrus; Hernando; Pasco (Florida)
        '''
        nameList = []
        nameStrings = []
        curState = None
        for ugc in ugcs:
            nameList.append(self.getInformationForUGC(ugc, "entityName"))
            if alphabetize:
                nameList.sort()
            stateName = nameList.append(self.getInformationForUGC(ugc, "fullStateName"))
            if curState is None:
                curState = stateName
            elif stateName != curState:
                nameStrings.append(self.formatNameString(nameList, separator, curState))
                curState = stateName
                nameList = []
        nameStrings.append(self.formatNameString(nameList, separator, curState))
        return self.formatNameString(nameStrings, separator='')
        
    # TODO -- Not Used?
    def formatUGC_cities(self, ugcs, alphabetize=0):
        cityList = []
        for ugc in ugcs:
            entry = self._areaDictionary.get(ugc)
            cityList.append(entry.get('ugcCityString', ugc))
        if alphabetize:
            cityList.sort()
        cityString = ''
        for cityStr in cityList:
            cityString += cityStr
        return cityString
    
    # TODO -- Not Used?
    def makeUGCList(self, areaList):
        '''
         Return new areaList and associated ugcList both sorted by ugcCode.
         Extracts ugcCode from the area dictionary for the each areaName in areaList.
         Will accept complex UGC strings in the area dictionary such as:
         ORZ001-004-WAZ021>023-029.
         However, in this case, one areaName could correspond to multiple
         ugcCodes and thus, the areaList is not guaranteed to follow
         the sorted ugcCode list order.
        '''
        areaDict = self._areaDictionary
        # Make a list of (areaName, ugcCode) tuples
        areaUgcList = []
        for areaName in areaList:
            if areaName in areaDict.keys():
                ugc = areaDict[areaName]['ugcCode']
                if ugc.find('-') >= 0 or ugc.find('>') >= 0:
                    ugcs = self.expandComplexUgc(ugc)
                    for ugc in ugcs:
                        areaUgcList.append((areaName, ugc))
                else:
                    areaUgcList.append((areaName, ugc))
                
        # Sort this list in ugc order
        areaUgcList.sort(self.ugcSort)

        # Make new 'parallel' lists of areaNames and ugcCodes
        ugcList = []
        newAreaList = []
        for areaName, ugcCode in areaUgcList:
            if areaName not in newAreaList:
                newAreaList.append(areaName)
            if ugcCode not in ugcList:
                ugcList.append(ugcCode)
        return newAreaList, ugcList
    
    # ## Formatting methods
    
    def bulletFormat_CR(self, text, label=''):
        text = self.bulletFormat_noCR(text, label)
        if text:
            text += '\n'
        return text
                  
    def bulletFormat_noCR(self, text, label='', defaultText='', frameit='Never', lineLength=69):
        '''
        Returns a properly formatted bulleted text 
        
        @param text:  Can be a single text string or list of text strings
          which will be concatenated together with carriage returns
        @param defaultText: If text is None or 0 length, then
            the default text is used. 
        @param frameit:  can be 
             'Never', in which nothing is wrapped in framing codes, 
             'Always' in which the text (default or cap) is wrapped in framing codes, or 
             'DefaultOnly' in which just the default text is wrapped.
        '''
        if label is None:
            label = ''
                
        if text is not None and len(text):
            if type(text) is types.ListType:
                newText = ''
                for t in text:
                    newText += t + '\n'
                text = newText
            textToUse = text
            if frameit == 'Always':
                textToUse = '|* ' + textToUse + ' *|'
        else:
            textToUse = defaultText
            if frameit == 'Always' or frameit == 'DefaultOnly':
                textToUse = '|* ' + textToUse + ' *|'

        textToUse = '* ' + label + textToUse
        return textToUse

    def getValueOrFramedText(self, key, dictionary, framedText):
        value = dictionary.get(key, None)
        if value is None or value == '':
            return self.frame(framedText)
        return value

    def frame(self, text):
        return '|* ' + text + ' *|'

    def formatDelimitedList(self, items, delimiter='...') :
        if not isinstance(items, list) and not isinstance(items, set) :
            try :
                return str(items)
            except :
                return ""
        # Add items to new list if not None
        newItems = []
        for item in items :
            if item:
                newItems.append(item)

        nLeft = len(newItems)
        fmtList = ""
        for item in newItems :
            fmtList += item
            if nLeft == 2 :
                fmtList += " and "
            elif nLeft > 2 :
                fmtList += delimiter
            nLeft -= 1
        return fmtList

    ###########
    #  Accessing MetaData
    def getProductStrings(self, hazardEvent, metaData, fieldName, productStringIdentifier=None, choiceIdentifier=None,
                          formatMethod=None, formatHashTags=[]):
        '''
        Translates the entries from the Hazard Information Dialog into product strings.
        @param hazardEvent: hazard event with user choices
        @param metaData:  dictionary specifying information to be entered through the Hazard Information Dialog
        @param fieldName: key field in the dictionaries, e.g. 'cta'
        @param productStringIdentifier:  If not None, identifies the particular productString requested.
            For example productStringIdentifier might be 'reportType1':
            return {"identifier":"ER", "displayString":"ER (Excessive Rainfall)",
                "productString": {
                        'reportType1': "Excessive rain causing Flash Flooding was occurring over the warned area", }}
  
        @param choiceIdentifier: for checkbox fields (rather than bullet), specifying the choice for which to retrieve the
           productString, e.g. 'particularStream' choice within 'additionalInformation' field
           IF no choiceIdentifier is specified, then a list of productStrings will be returned
        @return the associated productString.  If a productString is not given, return the displayString.
            If no value is specified in the hazardEvent, return empty string.
        
        For example, In the Hazard Information Dialog, there is a field for specifying the Flood Severity.  
        'immediateCause', the user-entered value might be 'ER (Excessive Rainfall)' and the productString 
        returned would then be 'Excessive rain causing Flash Flooding was occurring over the warned area.'
        '''   
        value = hazardEvent.get(fieldName) 
        if not value:
            return '' 
        if type(value) is types.ListType or isinstance(value, set):
            if choiceIdentifier:
                return self.getMetaDataValue(hazardEvent, metaData, fieldName,
                                             choiceIdentifier, formatMethod, formatHashTags)
            else:
                returnList = []
                for val in value:
                    returnList.append(self.getMetaDataValue(hazardEvent, metaData, fieldName,
                                                            val, formatMethod, formatHashTags))
                return returnList
        else:
            return self.getMetaDataValue(hazardEvent, metaData, fieldName, value, formatMethod, formatHashTags)

    def getEmbeddedDict(self, node, keyValue, search):
        """
        Recursive method to extract an embedded megawidget within a nested list/dict based
        on the field name
        @param node: the next node in the nested list/dict
        @param keyValue: key value - usually will be megawidget 'fieldName'
        @param search: the fieldName we're looking for. Ie. 'cta'
        """
        if isinstance(node, list):
            for element in node:
                result = self.getEmbeddedDict(element, keyValue, search)
                if result is not None:
                   return result
        elif isinstance(node, dict):
            if keyValue in node:
                if node[keyValue] == search:
                    return node
            for value in node.values():
                result = self.getEmbeddedDict(value, keyValue, search)
                if result is not None:
                   return result

    def getMetaDataValue(self, hazardEvent, metaData, fieldName, value, formatMethod, formatHashTags):
        '''
        Given a value, return the corresponding productString (or displayString) from the metaData. 
        @param hazardEvent: hazard event with user choices
        @param metaData:  dictionary specifying information to be entered through the Hazard Information Dialog
        @param fieldName: key field in the dictionaries, e.g. 'cta'
        @param value: chosen value for the key field

        @return the associated productString.  If a productString is not given, return the displayString.
            If no value is specified in the hazardEvent, return empty string.
            
        Example Meta Data field:  The fieldName might be "basis" and the value chosen by the user might be "wxSpot"
        
        def getBasis(self):
            return {
                "fieldName": "basis",
                "fieldType":"RadioButtons",
                "label":"Basis:",
                "values": self.basisDefaultValue(),
                "choices": self.basisChoices(),
                } 
        
        def basisSpotter(self):
            return {"identifier":"wxSpot", 
                    "displayString": "Weather spotters report flooding in", 
                    "productString": "Trained weather spotters reported flooding in #basisWxSpotLocation#.",
                    "detailFields": [
                                {
                                 "fieldType": "Text",
                                 "fieldName": "basisWxSpotLocation",
                                 "expandHorizontally": True,
                                 "maxChars": 40,
                                 "visibleChars": 12,
                                 "values": "|* Enter location *|",
                                }]
                          }

        '''    
        returnVal = ''

        widget = self.getEmbeddedDict(metaData, 'fieldName', fieldName)
        if widget is not None:
            choices = widget.get('choices')
            if choices:
                for choice in widget.get('choices'):
                    if choice.get('identifier') == value or choice.get('displayString') == value:
                        returnVal = choice.get('productString')
                        if returnVal is None:
                            returnVal = choice.get('displayString')
                        #The replace '  ' with '' is necessary for many triple quoted (''') string constants 
                        returnVal = returnVal.replace('.  ', '. ')
                        returnVal = returnVal.replace('  ', '')
                        returnVal = returnVal.replace('\n', ' ')
                        returnVal = returnVal.replace('<br/>', '\n')
                        returnVal = returnVal.replace('<br />', '\n')
                        returnVal = returnVal.replace('<br>', '\n')
                        returnVal = self.substituteParameters(hazardEvent, returnVal, formatMethod, formatHashTags)
                        break
            elif widget.get('fieldName'):
                returnVal = widget.get('fieldName')
        return returnVal
    
    def substituteParameters(self, hazardEvent, returnVal, formatMethod=None, formatHashTags=[]):
        # Search for #...# values  e.g. floodLocation
        hashTags = self.getFramedValues(returnVal, '#', '#')
        for hashTag in hashTags:
            eventValue = hazardEvent.get(hashTag)
            if eventValue is not None:
                if eventValue == '':
                    eventValue = self.getValueOrFramedText(hashTag, hazardEvent, hashTag)
                replaceVal = eventValue
            else:
                replaceVal = self.frame(hashTag)
            if hashTag in formatHashTags:
                try:
                    creationTime = hazardEvent.getCreationTime()
                except:
                    creationTime = hazardEvent.get('creationTime')
                formattedVal = formatMethod(creationTime, hashTag, replaceVal)
            else:
                # Verify it is a string 
                formattedVal = str(replaceVal)
            returnVal = returnVal.replace('#' + hashTag + '#', formattedVal)
        return returnVal

    def getFramedValues(self, text, beginStr='|* ', endStr=' *|'):
        '''
        @param text -- text string 
        @param beginStr -- string to begin a framed value
        @param endStr -- string to end framed value
        
        @return list of values framed by beginStr, endStr within the given text string
        '''
        values = text.split(beginStr)
        framedValues = []
        for value in values:
            value = value.strip()
            if text.find(beginStr + value + endStr) >= 0:
                framedValues.append(value)
        return framedValues        


    ###########


    def checkLastArrow(self, inSeq, ugcStr):
        '''
        Part of formatUGCs
        '''
        if inSeq == 1:
            # Change the last arrow to - since
            # we only had 2 in the sequence e.g.
            # 062>063  should be   062-063
            arrowIndex = ugcStr.rfind('>')
            if arrowIndex >= 0:
                ugcStr = ugcStr[:arrowIndex] + '-' + ugcStr[arrowIndex + 1:]
        return ugcStr    

    def filtFunc_zones(self, atts, compareToken):
        ugc = atts['state'] + 'Z' + atts['zone']
        if ugc in compareToken:
            return True
        else:
            return False
        
    def filtFunc_counties(self, atts, compareToken):
        ugc = atts['state'] + 'C' + atts['fips'][-3:] 
        if ugc in compareToken:
            return True
        else:
            return False

    ############ From GHG Hazard_FFA
    def simplifyAreas(self, areas):
        '''
        Simplifies the area phrases by combining subareas, returns the
        areas.

         rules: 1) multiple states and multiple direction terms in a state,
         only mention the state.  2) Multiple states but single directional
         term in a state, include the directional term.  3) Single state,
         include the directional terms.
         
         @param areas -- list of tuples (state, partOfState, names)
         @result -- sorted list of area names
        '''
        # determine how many states, and how many areas within each state
        stateDict = {}  # key is state, value is count of portions of state
        for state, partOfState, names in areas:
            if stateDict.has_key(state):
                stateDict[state] = stateDict[state] + 1
            else:
                stateDict[state] = 1
   
        # if single state, include all directional terms
        if len(stateDict.keys()) < 2:
            return areas  # unchanged

        # multiple states - multiple direction terms in a state
        # keep states sorted in same order as present. 
        out = []
        for state, partOfState, names in areas:
            if stateDict[state] == 1:
                names.sort()
                out.append((state, partOfState, names))
            elif len(out) == 0 or state != out[-1][0]:  # new state 
                out.append((state, '', names))  # leave out partOfState
            else:  # same state as before
                nmeList = out[-1][2]
                for n in names:
                    nmeList.append(n)
                nmeList.sort()               
        return out      

    def getAreaPhrase(self, ugcs, simplified=True, generalOnly=False):
        areaGroups = self.getGeneralAreaList(ugcs)
        if simplified:
            areaGroups = self.simplifyAreas(areaGroups)
        return self.makeAreaPhrase(areaGroups, generalOnly=generalOnly)
           
    def makeAreaPhrase(self, areaGroups, generalOnly=False):
        '''
        Portions of Iowa and Nebraska...including the following counties...
        in Iowa...Harrison and Monona. 
        in Nebraska...Burt and Thurston
        
        Creates the area phrase based on the groups of areas (areaGroups, 
        such as NE Pennsylvania), and the areas (areas), individual zones.
        returns the area phrase.  Area phrase does not have a terminating
        period.
        
        param @areaGroups: 
            (stateName, partOfState, list of tuples (name, nameType))
            where nameType is COUNTY, ZONE, etc.
        param @generalOnly:  If true returns only the state level description
        
        '''
        areaGroupLen = len(areaGroups)
        if areaGroupLen == 1:
            areaPhrase = 'a portion of '
        else:
            areaPhrase = 'portions of '

        areaPhrase += self.getStateDescription(areaGroups)        
        if generalOnly:
            return areaPhrase 
        
        nameDescription, nameTypePhrase = self.getNameDescription(areaGroups)
        areaPhrase = areaPhrase + '...including the following ' + nameTypePhrase + '...'
        areaPhrase = areaPhrase + nameDescription
      
        return areaPhrase

    def getNameDescription(self, areaGroups):
        '''
        Get description of names e.g.
        
        in Iowa...Harrison and Monona
        in Nebraska...Burt and Thurston
        
        OR 
        Harrison...Burt and Monona
        
        '''        
        # including phrase, have to count what we have
        icCnt = 0
        parishCnt = 0
        zoneCnt = 0
        countyCnt = 0
        for state, partOfState, names in areaGroups:
            for name, nameType in names:
                if nameType == 'ZONE':
                    zoneCnt = zoneCnt + 1
                elif nameType == 'COUNTY':
                    countyCnt = countyCnt + 1
                elif nameType == 'INDEPENDENT CITY':
                    icCnt = icCnt + 1
                elif nameType == 'PARISH':
                    parishCnt = parishCnt + 1
        nameTypes = []
#         if zoneCnt == 1:
#             nameTypes.append('area')
#         elif zoneCnt > 1:
#             nameTypes.append('areas')
        if countyCnt == 1 or zoneCnt == 1:
            nameTypes.append('county')
        elif countyCnt > 1 or zoneCnt > 1:
            nameTypes.append('counties')
        if icCnt == 1:
            nameTypes.append('independent city')
        elif icCnt > 1:
            nameTypes.append('independent cities')
        if parishCnt == 1:
            nameTypes.append('parish')
        elif parishCnt > 1:
            nameTypes.append('parishes')
        nameTypePhrase = ' and '.join(nameTypes)
             
        # list of the specific areas
        nameDescription = ''
        for i in xrange(len(areaGroups)):
            state, partOfState, names = areaGroups[i]
            if state == 'the District of Columbia':
                nameDescription = nameDescription + state
            else:
                # extract out the names
                snames = []
                for name, nameType in names:
                    snames.append(name)

                # single (don't mention state, partOfState again)
                if len(areaGroups) == 1:
                    phrase = '...'.join(snames[0:-1])
                # complex phrasing (state, partOfState, and names)
                else:
                    if i == 0:
                        phrase = 'in '
                    else:
                        phrase = 'In '
                    if partOfState != '' and partOfState != ' ':
                        phrase = phrase + partOfState + ' '
                    phrase = phrase + state + '...' + '...'.join(snames[0:-1])

                if len(snames) == 1:
                    phrase = phrase + snames[-1]
                else:
                    phrase = phrase + ' and ' + snames[-1]
                nameDescription = nameDescription + phrase
            if i != len(areaGroups) - 1:
                nameDescription = nameDescription + '. '  # another one coming, add period
                
        return nameDescription, nameTypePhrase

    def getStateDescription(self, areaGroups):
        ''' 
        Describe the parts of the states
         e.g. Southwest Iowa and Eastern Nebraska
        '''
        areaGroupCount = 0
        areaPhrase = ''
        for state, partOfState, names in areaGroups:
            areaGroupCount += 1
            if areaGroupCount == 1:
                conn = ''
            elif areaGroupCount == len(areaGroups):
                conn = ' and '
            else:
                conn = '...'

            if partOfState == '' or partOfState == ' ':
                areaPhrase = areaPhrase + conn + state
            else:
                areaPhrase = areaPhrase + conn + partOfState + ' ' + state
        return areaPhrase

    def getTextListStr(self, textList, format='dotted'):
        ''' 
        Return a text version of the list of strings e.g.
            Iowa...Harrison and Monona
            
        @param textList -- list of text strings
        @param format -- 'dotted' is separated by dots
                      -- 'separateLines' is one per line
                      -- 'columnFormat' is a column format
        Describe the parts of the states
         e.g. Southwest Iowa and Eastern Nebraska
        '''
        count = 0
        textListStr = ''
        for textStr in textList:
            if type(textStr) is types.TupleType:
                textStr = textStr[0]
            count += 1
            if count == 1:
                conn = ''
            else:
                if format == 'separateLines':
                    conn = '\n'
                elif format == 'dotted':
                    if count == len(textList):
                        conn = ' and '
                    else:
                        conn = '...'
            textListStr += conn + textStr
        return textListStr

    def sortSection(self, r1, r2):
        '''
        Sorts the vtecRecords in a particular order for the sections within
        each segment.  We try to keep this in the same order as the
        headlines order for clarity.
        '''
        return self.regularSortHazardAlg(r1, r2)

           

    ################ From GHG GenericHazards
    def hazardTimePhrases(self, vtecRecord, hazardEvent, issueTime, prefixSpace=True):
        '''
        The _hazardTimePhrases method is passed a hazard key, and returns
        time phrase wording consistent with that generated by the headline
        algorithms.
        '''
        timeWords = self.getTimingPhrase(vtecRecord, [hazardEvent], issueTime)
        if prefixSpace and len(timeWords):
            timeWords = ' ' + timeWords  # add a leading space
        return timeWords

    def substituteBulletedText(self, text, defaultText, frameit='Never', lineLength=69):
        '''
        Returns a properly formatted bulleted text 
        
        @param text:  Can be a single text string or list of text strings
          which will be concatenated together with carriage returns
        @param defaultText: If text is None or 0 length, then
            the default text is used. 
        @param frameit:  can be 
             'Never', in which nothing is wrapped in framing codes, 
             'Always' in which the text (default or cap) is wrapped in framing codes, or 
             'DefaultOnly' in which just the default text is wrapped.
        '''
        if text is not None and len(text):
            if type(text) is types.ListType:
                newText = ''
                for t in text:
                    newText += t + '\n'
                text = newText
            textToUse = text
            if frameit == 'Always':
                textToUse = '|* ' + textToUse + ' *|'
        else:
            textToUse = defaultText
            if frameit == 'Always' or frameit == 'DefaultOnly':
                textToUse = '|* ' + textToUse + ' *|'

        # add bullet codes
        textToUse = '* ' + textToUse

        # format it
        return self.indentText(textToUse, indentFirstString='',
          indentNextString='  ', maxWidth=lineLength,
          breakStrings=[' ', '-', '...'])

    def decodeBulletedText(self, prevText):
        '''
        Returns the bullet paragraph text or None, returns the
        regular text after the bullets.  The afterText is text up to
        the next bullet or up to 'THE NATIONAL WEATHER SERVICE'. Note
        that this only correctly handles the 1st set of entries in 
        a segment, thus double events will only decode the first set
        of bullets and text. The multipleRecords is set to 1 in the
        event that there are multiple sets of bullets. In this case
        only the 1st set was captured/decoded.
        (hazard, time, basis, impact, afterText, multipleRecords)
        '''
        if prevText is None:
            return (None, None, None, None, None, None)

        # find the bullets
        bullets = []
        buf = prevText.split('\n\n* ')
        if len(buf) <= 1:
            return (None, None, None, None, None, None)

        multRecords = 0  # indicator of multiple sets of bullets

        for x in xrange(len(buf)):
            if x == 0:
                continue  # headlines and text before the bullets
            bullets.append(buf[x])

        # find only the bulleted text, defined by the double line feed term.
        # of the text
        regText = ''  # regular text after bullets
        for x in xrange(1, len(bullets)):
            index = bullets[x].find('\n\n')
            if index != -1:
                regText = bullets[x][index + 2:]
                bullets[x] = bullets[x][0:index]  # eliminate after bullet text
                if len(bullets) > x + 2:  # more bullets are present
                    multRecords = 1
                bullets = bullets[0:x + 1]  # only interested in these bullets
                break
                
        # regular text is the remainder of the text.  However we only
        # want text from the last in the series of bullets to the 
        # beginning of any next NWS phrase. 
        lines = regText.split('\n')
        for x in xrange(len(lines)):
            if lines[x].find('THE NATIONAL WEATHER SERVICE') == 0:
                lines = lines[0:x]  # eliminate following lines
                break
        regText = ('\n').join(lines)

        # now clean up the text
        for x in xrange(len(bullets)):
            bullets[x] = string.replace(bullets[x], '\n', ' ')
        removeLF = re.compile(r'(s*[^\n])\n([^\n])', re.DOTALL)
        regText = removeLF.sub(r'\1 \2', regText)

        # extract out each section for returning the values
        if len(bullets) >= 1:
            hazard = bullets[0]
        else:
            hazard = None
        if len(bullets) >= 2:
            time = bullets[1]
        else:
            time = None
        if len(bullets) >= 3:
            basis = bullets[2]
        else:
            basis = None
        if len(bullets) >= 4:
            impact = bullets[3]
        else:
            impact = None
        if len(regText) == 0:
            regText = None  # no regular text after bullets

        return (hazard, time, basis, impact, regText, multRecords)



    ######## From GHG DiscretePhrases
    def messageTESTcheck(self, sessionDict, str):
        if sessionDict.get('testMode', 0):
            lines = str.split('\n')
            str = '...THIS MESSAGE IS FOR TEST PURPOSES ONLY...\n'
            for x in xrange(len(lines) - 1):  # -1 for trailing new line
                line = lines[x]

                # beginning of line
                if line.find('...') == 0:
                    line = line[0:3] + 'TEST ' + line[3:]
                # end of line
                index = line.rfind('...')
                if index != 0 and index == len(line) - 3:
                    line = line[0:-3] + ' TEST...' 

                lines[x] = line

            return str + string.join(lines, '\n')

        # normal mode (not test mode)
        else:
            return str

    def marineSortHazardAlg(self, r1, r2):
        # 1st by start time
        if r1['startTime'] < r2['startTime']:
            return -1
        elif r1['startTime'] > r2['startTime']:
            return 1
        
        # 2nd by action
        actionCodeOrder = ['CAN', 'EXP', 'UPG', 'NEW', 'EXB', 'EXA',
                           'EXT', 'ROU', 'CON']
        try:
            aIndex = actionCodeOrder.index(r1['act'])
        except:
            aIndex = 99
        try:
            bIndex = actionCodeOrder.index(r2['act'])
        except:
            bIndex = 99
        if aIndex < bIndex:
            return -1
        elif aIndex > bIndex:
            return 1

        # 3rd by significance
        sig = ['W', 'Y', 'A']
        try:
            index1 = sig.index(r1['sig'])
        except:
            index1 = 99
        try:
            index2 = sig.index(r2['sig'])
        except:
            index2 = 99
        if index1 < index2:
            return -1
        elif index1 > index2:
            return 1

        # 4th by phen (alphabetically)
        if r1['phen'] < r2['phen']:
            return -1
        elif r1['phen'] > r2['phen']:
            return 1

        # equal
        return 0    

    def regularSortHazardAlg(self, r1, r2):
        actActions = ['NEW', 'EXB', 'EXA', 'EXT', 'ROU', 'CON']
        inactActions = ['CAN', 'EXP', 'UPG']
        actionCodeOrder = actActions + inactActions

        # 1st by general action category
        if r1['act'] in actActions and r2['act'] in inactActions:
            return -1
        elif r1['act'] in inactActions and r2['act'] in actActions:
            return 1

        # 2nd by chronological event starting time
        if r1['startTime'] < r2['startTime']:
            return -1
        elif r1['startTime'] > r2['startTime']:
            return 1

        # 3rd by action code order
        try:
            aIndex = actionCodeOrder.index(r1['act'])
        except:
            aIndex = 99
        try:
            bIndex = actionCodeOrder.index(r2['act'])
        except:
            bIndex = 99
        if aIndex < bIndex:
            return -1
        elif aIndex > bIndex:
            return 1
        
        # 4th by significance
        sig = ['W', 'Y', 'A']
        try:
            index1 = sig.index(r1['sig'])
        except:
            index1 = 99
        try:
            index2 = sig.index(r2['sig'])
        except:
            index2 = 99
        if index1 < index2:
            return -1
        elif index1 > index2:
            return 1

        # 5th by phen (alphabetically)
        if r1['phen'] < r2['phen']:
            return -1
        elif r1['phen'] > r2['phen']:
            return 1

        # equal
        return 0

    def vtecRecordSortAlg(self, a, b):
        # check action code
        actionCodeOrder = ['CAN', 'EXP', 'UPG', 'NEW', 'EXB', 'EXA',
                           'EXT', 'CON', 'ROU']
        try:
            aIndex = actionCodeOrder.index(a['act'])
        except:
            aIndex -= 99
        try:
            bIndex = actionCodeOrder.index(b['act'])
        except:
            bIndex = 99

        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check sig
        sigOrder = ['W', 'Y', 'A', 'O', 'S', 'F']
        try:
            aIndex = sigOrder.index(a['sig'])
        except:
            aIndex = 99
        try:
            bIndex = sigOrder.index(b['sig'])
        except ValueError:
            bIndex = 99

        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check startTime
        if a['startTime'] > b['startTime']:
            return 1
        elif a['startTime'] < b['startTime']:
            return -1

        # phen
        if a['phen'] > b['phen']:
            return 1
        elif a['phen'] < b['phen']:
            return -1

        # subtype
        if a['subtype'] > b['subtype']:
            return 1
        elif a['subtype'] < b['subtype']:
            return -1

        return 0

    def actionControlWord(self, vtecRecord, issuanceTime):
        ''''
         Returns the words to be used in the headline for 'act' field in the
         specified vtecRecord.
        '''
        if not vtecRecord.has_key('act'):
            self.logger.error('Error!  No field act in vtec record.')
            return '<noaction>'

        actionCode = vtecRecord['act']

        # Need to account for COR since this is now called
        # in the formatters to create the summaryHeadline. 
        # Grab the previous action from the vtecRecord.
        if actionCode == 'COR':
            prevActionCode = vtecRecord.get('prevAct', None)
            if prevActionCode:
                actionCode = prevActionCode
            else:
                self.logger.error('Error!  No field prevAct in vtec record.')
                return '<noaction>'

        if actionCode in ['NEW', 'EXA', 'EXB']:
            return 'in effect'
        elif actionCode == 'CON':
            return 'remains in effect'
        elif actionCode == 'CAN':
            return 'is cancelled'
        elif actionCode == 'EXT':
            return 'now in effect'
        elif actionCode == 'EXP':
            deltaTime = issuanceTime - vtecRecord['endTime']
            if deltaTime >= 0:
                return 'has expired'
            else:
                return 'will expire'
        elif actionCode == 'UPG':
            return 'no longer in effect'
        else:
            self.logger.info(actionCode + 'not recognized in actionControlWord.')
            return '<actionControlWord>'

    def getHeadlinesAndSections(self, vtecRecords, metaDataList, productID, issueTime, includeTiming=True):
        '''
        Order vtec records and create the sections for the segment
        
        @param vtecRecords:  vtecRecords for a segment
        @param metaDataList: list of (metaData, hazardEvent) for the segment
        @param productID: product ID e.g. FFA, CWF, etc.
        @param issueTime: in seconds so that it compares to the vtec records
        '''
        sections = []
        headlines = []
        headlineStr = ''
        hList = copy.deepcopy(vtecRecords)
        if len(hList):
            if productID in ['CWF', 'NSH', 'OFF', 'GLF']:
                hList.sort(self.marineSortHazardAlg)
            else:
                hList.sort(self.regularSortHazardAlg)
                
        hazardEvents = [hazardEvent for metaData, hazardEvent in metaDataList]       
                                           
        while len(hList) > 0:
            vtecRecord = hList[0]

            # Can't make phrases with vtecRecords with no 'hdln' entry 
            if vtecRecord['hdln'] == '':
                hList.remove(vtecRecord)
                continue

            # make sure the vtecRecord is still in effect or within EXP criteria
            if (vtecRecord['act'] != 'EXP' and issueTime >= vtecRecord['endTime']) or \
            (vtecRecord['act'] == 'EXP' and issueTime > 30 * 60 + vtecRecord['endTime']):
                hList.remove(vtecRecord)
                continue  # no headline for expired vtecRecords
   
            # assemble the vtecRecord type
            hazStr = vtecRecord['hdln']
            headlines.append(hazStr)
            # hazStr = self.convertToLower(hazStr)

            # if the vtecRecord is a convective watch, tack on the etn
            phenSig = vtecRecord['phen'] + '.' + vtecRecord['sig']
            if phenSig in ['TO.A', 'SV.A']:
                hazStr = hazStr + ' ' + str(vtecRecord['etn'])

            # add on the action
            actionWords = self.actionControlWord(vtecRecord, issueTime)
            hazStr = hazStr + ' ' + actionWords
            
            if includeTiming:
                # get the timing words
                timeWords = self.getTimingPhrase(vtecRecord, hazardEvents, issueTime)
                if len(timeWords):
                    hazStr = hazStr + ' ' + timeWords
            
            if len(hazStr):
                # Call user hook
                localStr = self.hazard_hook(
                  None, None, vtecRecord['phen'], vtecRecord['sig'], vtecRecord['act'],
                  vtecRecord['startTime'], vtecRecord['endTime'])  # May need to add leading space if non-null 
                headlineStr = headlineStr + '...' + hazStr + localStr + '...\n'
            
            # Add to sections
            for metaData, hazardEvent in metaDataList:
                if hazardEvent.getEventID() in vtecRecord['eventID']:
                    sections.append((vtecRecord, metaData, hazardEvent))
                    break 

            # Add replaceStr
            replacedBy = hazardEvent.get('replacedBy')
            replaces = hazardEvent.get('replaces')
            if replacedBy:
                replaceStr = '...REPLACED BY ' + replacedBy + '...\n'
            elif replaces:
                replaceStr = '...REPLACES ' + replaces + '...\n'
            else:
                replaceStr = ''
            headlineStr += replaceStr 
                    
            # always remove the main vtecRecord from the list
            hList.remove(vtecRecord)
            
        return headlineStr, headlines, sections

    def getTimingPhrase(self, vtecRecord, hazardEvents, issueTime, stype=None, etype=None, timeZones=None):
        '''
        vtecRecord has times converted to ms
        issueTime in ms
        '''
        # Returns the timing phrase to use

        # Get the timing type
        if stype is None or etype is None:
            stype, etype = self.getTimingType(vtecRecord, issueTime)

        # Get the time zones for the areas
        if not timeZones:
            timeZones = []
            for hazardEvent in hazardEvents:
                timeZones += self.hazardTimeZones(hazardEvent.get('ugcs'))

        # Get the starting time
        stext = []
        startTime = vtecRecord['startTime']
        if type(stype) is types.MethodType:
            for tz in timeZones:
                newType, info = stype(
                    issueTime, startTime, tz, 'startTime')
                if info is not None and info not in stext:
                    stext.append(info)
            stype = newType
        elif stype == 'EXPLICIT':
            for tz in timeZones:
                info = self.timingWordTableEXPLICIT(issueTime,
                  startTime, tz, 'startTime')
                if info not in stext:
                    stext.append(info)
        elif stype == 'FUZZY4':
            for tz in timeZones:
                info = self.timingWordTableFUZZY4(issueTime,
                  startTime, tz, 'startTime')
                if info not in stext:
                    stext.append(info)
        elif stype == 'FUZZY8':
            for tz in timeZones:
                info = self.timingWordTableFUZZY8(issueTime,
                  startTime, tz, 'startTime')
                if info not in stext:
                    stext.append(info)
        elif stype == 'DAY_NIGHT_ONLY':
            for tz in timeZones:
                info = self.timingWordTableDAYNIGHT(issueTime,
                  startTime, tz, 'startTime')
                if info not in stext:
                    stext.append(info)

        # Get the ending time
        etext = []
        endTime = vtecRecord['endTime']
        if self.untilFurtherNotice(endTime):
            for tz in timeZones:
                etext.append(('', '', 'Further Notice'))
                if type(etype) is types.MethodType:
                    newType, info = etype(issueTime, 0, tz, 'endTime')
                    etype = newType        
        elif type(etype) is types.MethodType:
            for tz in timeZones:
                newType, info = etype(
                    issueTime, endTime, tz, 'endTime')
                if info is not None and info not in etext:
                    etext.append(info)
            etype = newType
        elif etype == 'EXPLICIT':
            for tz in timeZones:
                info = self.timingWordTableEXPLICIT(issueTime,
                  endTime, tz, 'endTime')
                if info not in etext:
                    etext.append(info)
        elif etype == 'FUZZY4':
            for tz in timeZones:
                info = self.timingWordTableFUZZY4(issueTime,
                  endTime, tz, 'endTime')
                if info not in etext:
                    etext.append(info)
        elif etype == 'FUZZY8':
            for tz in timeZones:
                info = self.timingWordTableFUZZY8(issueTime,
                  endTime, tz, 'endTime')
                if info not in etext:
                    etext.append(info)
        elif etype == 'DAY_NIGHT_ONLY':
            for tz in timeZones:
                info = self.timingWordTableDAYNIGHT(issueTime,
                  endTime, tz, 'endTime')
                if info not in etext:
                    etext.append(info)
                    
        # timing connection types
        startPrefix, endPrefix = self.getTimingConnectorType((stype, etype),
          vtecRecord['act'])

        # get the timing phrase
        phrase = self.calculateTimingPhrase(stype, etype, stext, etext,
          startPrefix, endPrefix)

        return phrase
        
    def getTimingType(self, vtecRecord, issueTime):
        '''
        @param vtecRecord -- vtec record with times converted to ms
        @param issueTime in ms
        
        Returns the timing type based on the issuanceTime and hazard record
        @return (startType, endType), which is NONE, EXPLICIT, FUZZY4, FUZZY8
        '''
        
        # Get the local headlines customizable timing
        locStart, locEnd = self.getLocalHeadlinesTiming(
            vtecRecord['key'], vtecRecord['startTime'], vtecRecord['endTime'],
            issueTime, vtecRecord['id'])
                
        # time from issuanceTime
        deltaTstart = vtecRecord['startTime'] - issueTime  # ms past now
        deltaTend = vtecRecord['endTime'] - issueTime  # ms past now
        
        HR = 3600 * 1000  # convenience constants
        MIN = 60 * 1000  # convenience constants
    
        # record in the past, ignore
        if deltaTend <= 0:
            return ('NONE', 'NONE')
    
        # upgrades and cancels
        if vtecRecord['act'] in ['UPG', 'CAN']:
            return ('NONE', 'NONE')  # upgrades/cancels never get timing phrases
    
        # expirations EXP codes are always expressed explicitly, only end time
        if vtecRecord['act'] == 'EXP':
            return ('NONE', 'EXPLICIT')
    
        phensig = vtecRecord['phen'] + '.' + vtecRecord['sig']
    
        # SPC Watches always get explicit times, 3 hour start mention
        spcWatches = ['TO.A', 'SV.A']
        if phensig in spcWatches:
            if deltaTstart < 3 * HR:
                return ('NONE', 'EXPLICIT')
            else:
                return ('EXPLICIT', 'EXPLICIT')

        # Tropical events never get times at all
        tpcEvents = ['TY.A', 'TY.W', 'HU.A', 'HU.S', 'HU.W', 'TR.A', 'TR.W']
        if phensig in tpcEvents:
            return ('NONE', 'NONE')
    
        # special marine case?
        marineHazList = ['SC.Y', 'SW.Y', 'GL.W', 'SR.W', 'HF.W', 'BW.Y',
          'UP.W', 'UP.Y', 'RB.Y', 'SE.W', 'SI.Y']  # treat like watches
        marinePils = ['CWF', 'OFF', 'NSH', 'GLF']  # specific marine pils
        oconusSites = ['PGUM', 'PHFO', 'PAFC', 'PAJK', 'PAFG']
    
        # regular products - not marine
        if vtecRecord['pil'] not in marinePils:
            # advisories/warnings
            if vtecRecord['sig'] in ['Y', 'W']:  # advisories/warnings - explicit
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                else:
                    start = 'EXPLICIT'  # explicit start time after 3 hours
                end = 'EXPLICIT'  # end time always explicit
    
            # watches
            elif vtecRecord['sig'] in ['A']:  # watches - mix of explicit/fuzzy
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                elif deltaTstart < 12 * HR:
                    start = 'EXPLICIT'  # explicit start time 3-12 hours
                else:
                    start = 'FUZZY4'  # fuzzy times after 12 (4/day)
                if deltaTend < 12 * HR:  # explicit end time 0-12 hours
                    end = 'EXPLICIT'
                else:
                    end = 'FUZZY4'  # fuzzy times after 12 (4/day)
            
            # local hazards
            elif locStart is not None and locEnd is not None:
                start = locStart
                end = locEnd
            else: 
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                elif deltaTstart < 12 * HR:
                    start = 'EXPLICIT'  # explicit start time 3-12 hours
                else:
                    start = 'FUZZY4'  # fuzzy times after 12 (4/day)
                if deltaTend < 12 * HR:  # explicit end time 0-12 hours
                    end = 'EXPLICIT'
                else:
                    end = 'FUZZY4'  # fuzzy times after 12 (4/day)
    
    
        # marine - CONUS
        elif vtecRecord['officeid'] not in oconusSites:
    
            # njensen: i changed the < to <= below because the automated tests
            # were failing with a race condition where issueTime would be the
            # exact same time as startTime and therefore the tests would sometimes
            # fall into the wrong if/else block
    
            # advisories/warnings - explicit, but not some phensigs
            if vtecRecord['sig'] in ['Y', 'W'] and phensig not in marineHazList:
                if deltaTstart <= 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                else:
                    start = 'EXPLICIT'  # explicit start time after 3 hours
                end = 'EXPLICIT'  # end time always explicit
    
            # watches - mix of explicit/fuzzy, some phensig treated as watches
            elif vtecRecord['sig'] in ['A'] or phensig in marineHazList:
                if deltaTstart <= 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                elif deltaTstart <= 12 * HR:
                    start = 'EXPLICIT'  # explicit start time 3-12 hours
                else:
                    start = 'FUZZY4'  # fuzzy times after 12 (4/day)
                if deltaTend <= 12 * HR:  # explicit end time 0-12 hours
                    end = 'EXPLICIT'
                else:
                    end = 'FUZZY4'  # fuzzy times after 12 (4/day)
            
            # local hazards - treat as watches
            elif locStart is not None and locEnd is not None:
                start = locStart
                end = locEnd
            else:
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                elif deltaTstart < 12 * HR:
                    start = 'EXPLICIT'  # explicit start time 3-12 hours
                else:
                    start = 'FUZZY4'  # fuzzy times after 12 (4/day)
                if deltaTend < 12 * HR:  # explicit end time 0-12 hours
                    end = 'EXPLICIT'
                else:
                    end = 'FUZZY4'  # fuzzy times after 12 (4/day)
    
        # marine - OCONUS
        else:
    
            # advisories/warnings - explicit, but not some phensigs
            if vtecRecord['sig'] in ['Y', 'W'] and phensig not in marineHazList:
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                else:
                    start = 'EXPLICIT'  # explicit start time after 3 hours
                end = 'EXPLICIT'  # end time always explicit

            # special marine phensigs - treat as watches, with fuzzy8
            elif phensig in marineHazList:
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                else:
                    start = 'FUZZY8'  # fuzzy start times
                end = 'FUZZY8'  # always fuzzy end times

    
            # regular watches - fuzzy4
            elif vtecRecord['sig'] in ['A']:
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                elif deltaTstart < 12 * HR:
                    start = 'EXPLICIT'  # explicit start time 3-12 hours
                else:
                    start = 'FUZZY4'  # fuzzy times after 12 (4/day)
                if deltaTend < 12 * HR:  # explicit end time 0-12 hours
                    end = 'EXPLICIT'
                else:
                    end = 'FUZZY4'  # fuzzy times after 12 (4/day)

            # local hazards - treat as watches
            elif locStart is not None and locEnd is not None:
                start = locStart
                end = locEnd
            else:
                if deltaTstart < 3 * HR:  # no start time in first 3 hours
                    start = 'NONE'
                elif deltaTstart < 12 * HR:
                    start = 'EXPLICIT'  # explicit start time 3-12 hours
                else:
                    start = 'FUZZY4'  # fuzzy times after 12 (4/day)
                if deltaTend < 12 * HR:  # explicit end time 0-12 hours
                    end = 'EXPLICIT'
                else:
                    end = 'FUZZY4'  # fuzzy times after 12 (4/day)
    
        return (start, end)  
    
    def getLocalHeadlinesTiming(self, hazardType, startTime, endTime, issueTime, areaLabel):
        headlinesTiming = self.headlinesTiming(hazardType, startTime, endTime,
          issueTime, areaLabel)
        if headlinesTiming is None:
            locStart = None
            locEnd = None
        else:
            locStart, locEnd = headlinesTiming
            if locStart == 'FUZZY':
                locStart = 'FUZZY4'
            if locEnd == 'FUZZY':
                locEnd = 'FUZZY4'
        return locStart, locEnd

    def headlinesTiming(self, hazardType, startTime, endTime, issueTime, areaLabel):
        '''
        @param hazardType -- hazard type e.g. FF.A or FF.W.Convective
        @param startTime -- startTime of hazard in ms
        @param endTime -- endTime of hazard in ms
        @param issueTime in ms
        @param areaLabel -- ugc code 
        
        Returns the timing type based on the issueTime and hazard 
        @return (startType, endType), which is NONE, EXPLICIT, FUZZY4, FUZZY8
        '''
        
        # Return
        #  'startPhraseType' and 'endPhraseType'
        #   Each can be one of these phraseTypes: 
        #      'EXPLICIT' will return words such as '5 PM'
        #      'FUZZY4' will return words such as 'THIS EVENING'
        #      'DAY_NIGHT_ONLY' use only weekday or weekday 'NIGHT' e.g.
        #         'SUNDAY' or 'SUNDAY NIGHT' or 'TODAY' or 'TONIGHT'
        #         Note: You will probably want to set both the
        #         startPhraseType and endPhraseType to DAY_NIGHT_ONLY to
        #         have this work correctly.
        #      'NONE' will result in no words
        #   OR a method which takes arguments:
        #        issueTime, eventTime, timeZone, and timeType
        #     and returns:
        #        phraseType, (hourStr, hourTZstr, description)
        #     You can use 'timingWordTableFUZZY8' as an example to
        #     write your own method.
        # 
        # If you simply return None, no timing words will be used.

        # Note that you can use the information given to determine which
        # timing phrases to use. In particular, the 'key' is the Hazard
        # key so different local headlines can use different timing.
        #  
        startPhraseType = 'FUZZY'
        endPhraseType = 'FUZZY'

        # Example code -- NOTE: need to convert to unixTime manually
        # if startTime <= issuanceTime + 12 * 3600 * 1000:   # 12 hours past issuance
            # startPhraseType = 'EXPLICIT'
        # if endTime <= issuanceTime + 12 * 3600 * 1000:   # 12 hours past issuance
            # endPhraseType = 'EXPLICIT'

        # return startPhraseType, endPhraseType
        return None, None
    
    def untilFurtherNotice(self, time_ms):
        if time_ms / 1000 >= VTECConstants.UFN_TIME_VALUE_SECS:
            return True
        else:
            return False
    
    def hazardTimeZones(self, areaList):
        '''
        Returns list of time zones for the starting time
        and list of time zones for the ending time.  
        
        The areaList provides a complete list of areas for this headline. 
        startT, endT are the hazard times.
        '''
        
        # get this time zone
        thisTimeZone = os.environ.get('TZ', 'GMT')

        # check to see if we have any areas outside our time zone
        zoneList = []
        for areaName in areaList:
            timeZoneData = self.getInformationForUGC(areaName, "timeZone")
            if timeZoneData == "" :
                timeZoneData = [ thisTimeZone ]
            elif type(timeZoneData) is not types.ListType:
                timeZoneData = [ str(timeZoneData) ]
            for timeZone in timeZoneData:
                if timeZone in zoneList:
                    continue
                if timeZone == thisTimeZone :
                    zoneList.insert(0, timeZone)
                else :
                    zoneList.append(timeZone)

        # if the resulting zoneList is empty, put in our time zone
        if len(zoneList) == 0:
            zoneList.append(thisTimeZone)

        return zoneList
    
    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()    

    def compareDictionaries(self, dict1, dict2):
        '''
        Calculate the difference between two dictionaries as:
        1 items added
        2 items removed
        3 keys same in both but changed values
        '''
        set_current, set_past = set(dict1.keys()), set(dict2.keys())
        intersect = set_current.intersection(set_past)
        addedEntries = set_current - intersect
        removedEntries = set_past - intersect
        changedEntries = set(o for o in intersect if dict2[o] != dict1[o])
        return addedEntries, removedEntries, changedEntries

    def round(self, dt, roundMinute=15):
        discard = timedelta(minutes=dt.minute % roundMinute,
                             seconds=dt.second,
                             microseconds=dt.microsecond)
        dt -= discard
        if discard >= timedelta(minutes=roundMinute/2):
            dt += timedelta(minutes=roundMinute)
        return dt

    def roundFloat(self, value, precision='2', returnString=True):
        '''
            Rounding method to be used for all float values in the product.
            This will ensure that the precision is consistent throughout.
            
            @param value: Value to be rounded
            @param precision: Precision to be rounded to.
            @param returnString: Flag indicating whether a string or float
                                 should be returned.
        '''
        if isinstance(value, float) == False:
            value = float(value)
        if returnString:
            value = format(value, '.' + precision + 'f')
        else:
            value = round(value, precision)
        return value

    def timingWordTableEXPLICIT(self, issueTime, eventTime, timezone,
      timeType='startTime'):
        # returns (timeValue, timeZone, descriptiveWord).  
        # eventTime is either the starting or ending time, based on 
        # the timeType flag. timezone is the time zone for the hazard area

        HR = 3600
        sameDay = [
          (0 * HR, 6 * HR, 'early this morning'),  # midnght-559am
          (6 * HR, 12 * HR - 1, 'this morning'),  # 600am-1159am
          (12 * HR, 12 * HR + 1, 'today'),  # noon
          (12 * HR + 1, 18 * HR - 1, 'this afternoon'),  # 1201pm-559pm
          (18 * HR, 24 * HR, 'this evening')]  # 6pm-1159pm

        nextDay = [
          (0 * HR, 0 * HR + 1, 'tonight'),  # midnght
          (0 * HR, 24 * HR, '<dayOfWeek>'), ]  # midnght-1159pm

        subsequentDay = [
          (0 * HR, 0 * HR + 1, '<dayOfWeek-1> Night'),  # midnght
          (0 * HR, 24 * HR, '<dayOfWeek>'), ]  # midnght-1159pm


        # determine local time
        myTimeZone = os.environ.get('TZ', 'GMT')  # save the defined time zone
        os.environ['TZ'] = timezone  # set the new time zone
        ltissue = time.localtime(issueTime / 1000)  # issuance local time
        ltevent = time.localtime(eventTime / 1000)  # event local time
        # get the hour/min string (e.g., 800 PM)
        dt = self.round(datetime.fromtimestamp(eventTime / 1000))
        hourStr = time.strftime('%I%M %p', dt.timetuple())
        if hourStr[0] == '0':
            hourStr = hourStr[1:]  # eliminate leading zero

        # get the time zone (e.g., MDT)
        hourTZstr = time.strftime('%Z', ltevent)

        # determine the delta days from issuance to event
        diffDays = ltevent[7] - ltissue[7]  # julian day
        if diffDays < 0:  # year wrap around, assume Dec/Jan
            diffDays = ltevent[2] + 31 - ltissue[2]  # day of month

        # get description time phrase
        description = '<day>'
        hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
        if diffDays == 0:
            for (startT, endT, desc) in sameDay:
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break

        else:
            # choose proper table
            if diffDays == 1:
                table = nextDay
            else:
                table = subsequentDay
            for (startT, endT, desc) in table:
                hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break
            dow = ltevent[6]  # day of week
            dowMinusOne = ltevent[6] - 1
            if dowMinusOne < 0:
                dowMinusOne = 6  # week wraparound
            description = string.replace(description, '<dayOfWeek>',
              self.asciiDayOfWeek(dow))  # day of week
            description = string.replace(description, '<dayOfWeek-1>',
              self.asciiDayOfWeek(dowMinusOne))  # day of week

        # special cases NOON
        if hourStr == '12 PM' and description == 'today':
            hourStr = 'Noon'

        # special cases MIDNIGHT
        if hourStr == '12 AM':
            hourStr = 'Midnight'

        os.environ['TZ'] = myTimeZone  # reset the defined time zone

        return (hourStr, hourTZstr, description)


    def timingWordTableFUZZY4(self, issueTime, eventTime, timeZone,
      timeType='startTime'):
        # returns (timeValue, timeZone, descriptiveWord).  
        # eventTime is either the starting or ending time, based on 
        # the timeType flag. timezone is the time zone for the hazard area
        # table is local time, start, end, descriptive phrase
        HR = 3600
        sameDay = [
          (0 * HR, 6 * HR, 'early this morning'),  # midnght-559am
          (6 * HR, 12 * HR, 'this morning'),  # 600am-noon
          (12 * HR, 18 * HR, 'this afternoon'),  # 1200pm-559pm
          (18 * HR, 24 * HR, 'this evening')]  # 6pm-1159pm

        nextDay = [
          (0 * HR, 0 * HR, 'this evening'),  # midnght tonight
          (0 * HR, 6 * HR, 'late tonight'),  # midnght-559am
          (6 * HR, 12 * HR, '<dayOfWeek> morning'),  # 600am-noon
          (12 * HR, 18 * HR, '<dayOfWeek> afternoon'),  # 1200pm-559pm
          (18 * HR, 24 * HR, '<dayOfWeek> evening')]  # 6pm-1159pm

        subsequentDay = [
          (0 * HR, 0 * HR, '<dayOfWeek-1> evening'),  # midnght ystdy 
          (0 * HR, 6 * HR, 'late <dayOfWeek-1> night'),  # midnght-559am
          (6 * HR, 12 * HR, '<dayOfWeek> morning'),  # 600am-noon
          (12 * HR, 18 * HR, '<dayOfWeek> afternoon'),  # 1200pm-559pm
          (18 * HR, 24 * HR, '<dayOfWeek> evening')]  # 6pm-1159pm


        # determine local time
       
        myTimeZone = os.environ.get('TZ', 'GMT')  # save the defined time zone
        os.environ['TZ'] = timeZone  # set the new time zone        
    
        ltissue = time.localtime(issueTime / 1000)  # issuance local time
        ltevent = time.localtime(eventTime / 1000)  # event local time

        # determine the delta days from issuance to event
        diffDays = ltevent[7] - ltissue[7]  # julian day
        if diffDays < 0:  # year wrap around, assume Dec/Jan
            diffDays = ltevent[2] + 31 - ltissue[2]  # day of month

        # get description time phrase
        description = '<day>'
        hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
        if diffDays == 0:
            for (startT, endT, desc) in sameDay:
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':  
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break

        else:
            # choose proper table
            if diffDays == 1:
                table = nextDay
            else:
                table = subsequentDay
            for (startT, endT, desc) in table:
                hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break
            dow = ltevent[6]  # day of week
            dowMinusOne = ltevent[6] - 1
            if dowMinusOne < 0:
                dowMinusOne = 6  # week wraparound
            description = string.replace(description, '<dayOfWeek>',
              self.asciiDayOfWeek(dow))  # day of week
            description = string.replace(description, '<dayOfWeek-1>',
              self.asciiDayOfWeek(dowMinusOne))  # day of week

        os.environ['TZ'] = myTimeZone  # reset the defined time zone

        hourStr = None
        hourTZstr = None
        return (hourStr, hourTZstr, description)


    def timingWordTableFUZZY8(self, issueTime, eventTime, timeZone,
      timeType='startTime'):
        # returns the descriptive word for the event.  eventTime is either
        # the starting or ending time, based on the timeType flag.
        # table is local time, start, end, descriptive phrase-A
  
        HR = 3600
        sameDay = [
          (0 * HR, 3 * HR, 'late <dayOfWeek-1> night'),  # midnght-259am
          (3 * HR, 6 * HR, 'early this morning'),  # 300am-559am
          (6 * HR, 9 * HR, 'this morning'),  # 600am-859am
          (9 * HR, 12 * HR, 'late this morning'),  # 900am-1159am
          (12 * HR, 15 * HR, 'early this afternoon'),  # noon-259pm
          (15 * HR, 18 * HR, 'late this afternoon'),  # 300pm-559pm
          (18 * HR, 21 * HR, 'this evening'),  # 600pm-859pm
          (21 * HR, 24 * HR, 'tonight')]  # 900pm-1159pm

        nextDayStart = [
          (0 * HR, 3 * HR, 'late <dayOfWeek-1> night'),  # midnght-259am
          (3 * HR, 6 * HR, 'early <dayOfWeek> morning'),  # 300am-559am
          (6 * HR, 12 * HR, '<dayOfWeek> morning'),  # 600am-noon
          (12 * HR, 18 * HR, '<dayOfWeek> afternoon'),  # 1200pm-559pm
          (18 * HR, 24 * HR, '<dayOfWeek> evening')]  # 6pm-1159pm

        nextDayEnd = [
          (0 * HR, 0 * HR, 'tonight'),  # midnght tonight
          (0 * HR, 3 * HR, 'late <dayOfWeek-1> night'),  # midnght-259am
          (3 * HR, 6 * HR, 'early <dayOfWeek> morning'),  # 300am-559am
          (6 * HR, 12 * HR, '<dayOfWeek> morning'),  # 600am-noon
          (12 * HR, 18 * HR, '<dayOfWeek> afternoon'),  # 1200pm-559pm
          (18 * HR, 24 * HR, '<dayOfWeek> night')]  # 6pm-1159pm

        subsequentDayStart = [
          (0 * HR, 6 * HR, 'late <dayOfWeek-1> night'),  # midnght-559am
          (6 * HR, 12 * HR, '<dayOfWeek> morning'),  # 600am-noon
          (12 * HR, 18 * HR, '<dayOfWeek> afternoon'),  # 1200pm-559pm
          (18 * HR, 24 * HR, '<dayOfWeek> evening')]  # 6pm-1159pm

        subsequentDayEnd = [
          (0 * HR, 0 * HR, '<dayOfWeek-1> night'),  # midnght tonight
          (0 * HR, 6 * HR, 'early <dayOfWeek> morning'),  # midnght-559am
          (6 * HR, 12 * HR, '<dayOfWeek> morning'),  # 600am-noon
          (12 * HR, 18 * HR, '<dayOfWeek> afternoon'),  # 1200pm-559pm
          (18 * HR, 24 * HR, '<dayOfWeek> night')]  # 6pm-1159pm


        # determine local time
        myTimeZone = os.environ.get('TZ', 'GMT')  # save the defined time zone
        os.environ['TZ'] = timeZone  # set the new time zone
        ltissue = time.localtime(issueTime / 1000)  # issuance local time
        ltevent = time.localtime(eventTime / 1000)  # event local time

        # determine the delta days from issuance to event
        diffDays = ltevent[7] - ltissue[7]  # julian day
        if diffDays < 0:  # year wrap around, assume Dec/Jan
            diffDays = ltevent[2] + 31 - ltissue[2]  # day of month

        # get description time phrase
        description = '<day>'
        hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
        if diffDays == 0:
            for (startT, endT, desc) in sameDay:
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':  
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break

        else:
            # choose proper table
            if timeType == 'startTime':
                if diffDays == 1:
                    table = nextDayStart
                else:
                    table = subsequentDayStart
            else:
                if diffDays == 1:
                    table = nextDayEnd
                else:
                    table = subsequentDayEnd
            for (startT, endT, desc) in table:
                hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break

       # do substitution
        dow = ltevent[6]  # day of week
        dowMinusOne = ltevent[6] - 1
        if dowMinusOne < 0:
            dowMinusOne = 6  # week wraparound
        description = string.replace(description, '<dayOfWeek>',
          self.asciiDayOfWeek(dow))  # day of week
        description = string.replace(description, '<dayOfWeek-1>',
          self.asciiDayOfWeek(dowMinusOne))  # day of week

        os.environ['TZ'] = myTimeZone  # reset the defined time zone

        hourStr = None
        hourTZstr = None
        return (hourStr, hourTZstr, description)

    def timingWordTableDAYNIGHT(self, issueTime, eventTime, timeZone,
      timeType='startTime'):
        # returns (timeValue, timeZone, descriptiveWord).  
        # eventTime is either the starting or ending time, based on 
        # the timeType flag. timezone is the time zone for the hazard area
        # table is local time, start, end, descriptive phrase
        HR = 3600
        sameDay = [
          (0 * HR, self.DAY() * HR, 'early today'),  # midnght-559am
          (self.DAY() * HR, self.NIGHT() * HR, 'today'),  # 600am-6pm
          (self.NIGHT() * HR, 24 * HR, 'tonight')]  # 6pm-midnight

        nextDay = [
          (0 * HR, self.DAY() * HR, 'tonight'),  # midnght-559am
          (self.DAY() * HR, self.NIGHT() * HR, '<dayOfWeek>'),  # 600am-6pm
          (self.NIGHT() * HR, 24 * HR, '<dayOfWeek> night')]  # 6pm-midnight

        subsequentDay = [
          (0 * HR, self.DAY() * HR, '<dayOfWeek-1> night'),  # midnght-559am
          (self.DAY() * HR, self.NIGHT() * HR, '<dayOfWeek>'),  # 600am-6pm
          (self.NIGHT() * HR, 24 * HR, '<dayOfWeek> night')]  # 6pm-midnight

        # determine local time
        myTimeZone = os.environ.get('TZ', 'GMT')  # save the defined time zone
        os.environ['TZ'] = timeZone  # set the new time zone
        ltissue = time.localtime(issueTime / 1000)  # issuance local time
        ltevent = time.localtime(eventTime / 1000)  # event local time

        # determine the delta days from issuance to event
        diffDays = ltevent[7] - ltissue[7]  # julian day
        if diffDays < 0:  # year wrap around, assume Dec/Jan
            diffDays = ltevent[2] + 31 - ltissue[2]  # day of month

        # get description time phrase
        description = '<day>'
        hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
        if diffDays == 0:
            for (startT, endT, desc) in sameDay:
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':  
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break

        else:
            # choose proper table
            if diffDays == 1:
                table = nextDay
            else:
                table = subsequentDay
            for (startT, endT, desc) in table:
                hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
                if hourmin >= startT and hourmin < endT and timeType == 'startTime':
                    description = desc
                    break
                elif hourmin <= endT and timeType == 'endTime':
                    description = desc
                    break
            dow = ltevent[6]  # day of week
            dowMinusOne = ltevent[6] - 1
            if dowMinusOne < 0:
                dowMinusOne = 6  # week wraparound
            description = string.replace(description, '<dayOfWeek>',
              self.asciiDayOfWeek(dow))  # day of week
            description = string.replace(description, '<dayOfWeek-1>',
              self.asciiDayOfWeek(dowMinusOne))  # day of week

        os.environ['TZ'] = myTimeZone  # reset the defined time zone

        hourStr = None
        hourTZstr = None
        return (hourStr, hourTZstr, description)

    def asciiDayOfWeek(self, number):
        # converts number (0-Monday) to day of week
        days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday',
          'Saturday', 'Sunday']
        if number >= 0 and number < 7:
            return days[number]
        else:
            return '?' + `number` + '?'
    
    def getTimingConnectorType(self, timingType, action):
        '''
         Returns the start and end prefix for the given start and end phrase
         type and action code.
        '''
        d = {('NONE', 'NONE'):           (None, None),
             ('NONE', 'EXPLICIT'):       (None, 'until'),
             ('NONE', 'FUZZY4'):         (None, 'through'),
             ('NONE', 'FUZZY8'):         (None, 'through'),
             ('EXPLICIT', 'EXPLICIT'):   ('from', 'to'),
             ('EXPLICIT', 'FUZZY4'):     ('from', 'through'),
             ('EXPLICIT', 'FUZZY8'):     ('from', 'through'),
             ('FUZZY4', 'FUZZY4'):       ('from', 'through'),
             ('FUZZY4', 'FUZZY8'):       ('from', 'through'),
             ('FUZZY8', 'FUZZY4'):       ('from', 'through'),
             ('FUZZY8', 'FUZZY8'):       ('from', 'through'),
             ('NONE', 'DAY_NIGHT_ONLY'):          (None, 'through'),
             ('EXPLICIT', 'DAY_NIGHT_ONLY'):      ('from', 'through'),
             ('FUZZY4', 'DAY_NIGHT_ONLY'):        ('from', 'through'),
             ('FUZZY8', 'DAY_NIGHT_ONLY'):        ('from', 'through'),
             ('DAY_NIGHT_ONLY', 'DAY_NIGHT_ONLY'): ('from', 'through'),
             ('DAY_NIGHT_ONLY', 'NONE'):          ('from', None),
             ('DAY_NIGHT_ONLY', 'EXPLICIT'):      ('from', 'to'),
             ('DAY_NIGHT_ONLY', 'FUZZY4'):        ('from', 'through'),
             ('DAY_NIGHT_ONLY', 'FUZZY8'):        ('from', 'through'),
            }

        # special case for expirations.
        if action == 'EXP':
            return (None, 'AT')

        return d.get(timingType, ('<startPrefix?>', '<endPrefix?>'))

    # calculates the timing phrase based on the timing type, the calculated
    # timing words, and the prefixes
    def calculateTimingPhrase(self, stype, etype, stext, etext, startPrefix,
      endPrefix):

        if (stype, etype) == ('NONE', 'NONE'):
            return ''  # no timing phrase

        elif (stype, etype) in [('NONE', 'EXPLICIT')]:
            return self.ctp_NONE_EXPLICIT(stext, etext, startPrefix, endPrefix)

        elif (stype, etype) in [('NONE', 'FUZZY4'), ('NONE', 'FUZZY8')]:
            return self.ctp_NONE_FUZZY(stext, etext, startPrefix, endPrefix)

        elif (stype, etype) in [('EXPLICIT', 'EXPLICIT')]:
            return self.ctp_EXPLICIT_EXPLICIT(stext, etext, startPrefix,
              endPrefix)

        elif (stype, etype) in [('EXPLICIT', 'FUZZY4'), ('EXPLICIT', 'FUZZY8')]:
            return self.ctp_EXPLICIT_FUZZY(stext, etext, startPrefix, endPrefix)

        elif (stype, etype) in [('FUZZY4', 'FUZZY4'), ('FUZZY8', 'FUZZY4'),
          ('FUZZY4', 'FUZZY8'), ('FUZZY8', 'FUZZY8')]:
            return self.ctp_FUZZY_FUZZY(stext, etext, startPrefix, endPrefix)

        elif (stype, etype) in [('NONE', 'DAY_NIGHT_ONLY')]:
            return self.ctp_NONE_DAYNIGHT(stext, etext, startPrefix, endPrefix)

        elif (stype, etype) in [('EXPLICIT', 'DAY_NIGHT_ONLY')]:
            return self.ctp_EXPLICIT_DAYNIGHT(stext, etext, startPrefix,
              endPrefix)

        elif (stype, etype) in [('FUZZY4', 'DAY_NIGHT_ONLY'),
          ('FUZZY8', 'DAY_NIGHT_ONLY')]:
            return self.ctp_FUZZY_DAYNIGHT(stext, etext, startPrefix, endPrefix)

        elif (stype, etype) in [('DAY_NIGHT_ONLY', 'DAY_NIGHT_ONLY')]:
            return self.ctp_DAYNIGHT_DAYNIGHT(stext, etext, startPrefix,
              endPrefix)

        elif (stype, etype) in [('DAY_NIGHT_ONLY', 'NONE')]:
            return self.ctp_DAYNIGHT_NONE(stext, etext, startPrefix, endPrefix)

        elif (stype, etype) in [('DAY_NIGHT_ONLY', 'EXPLICIT')]:
            return self.ctp_DAYNIGHT_EXPLICIT(stext, etext, startPrefix,
              endPrefix)

        elif (stype, etype) in [('DAY_NIGHT_ONLY', 'FUZZY4'),
          ('DAY_NIGHT_ONLY', 'FUZZY8')]:
            return self.ctp_DAYNIGHT_FUZZY(stext, etext, startPrefix, endPrefix)

        else:
            return '<UnknownPhraseType-' + stype + '/' + etype + '>'

    def getRiverProTimePhrase(self, issueTime, eventTime, timeZone):
        '''
            Returns a timephrase to be used for a specific time. Phrases
            were taken from NHOR's timephra.dat file.
        '''
        HR = 3600
        yesterday = [
          (0 * HR, 3 * HR, 'after midnight yesterday'),
          (3 * HR, 6 * HR, 'early yesterday'),
          (6 * HR, 9 * HR, 'yesterday morning'),
          (9 * HR, 12 * HR, 'yesterday late morning'),
          (12 * HR, 15 * HR, 'yesterday early afternoon'),
          (15 * HR, 18 * HR, 'yesteday afternoon'),
          (18 * HR, 21 * HR, 'yesterday evening'),
          (21 * HR, 24 * HR, 'yesterday late evening')]

        today = [
          (0 * HR, 3 * HR, 'after midnight <Weekday> morning'),
          (3 * HR, 6 * HR, 'early <Weekday> morning'),
          (6 * HR, 9 * HR, '<Weekday> morning'),
          (9 * HR, 12 * HR, 'late <Weekday> morning'),
          (12 * HR, 15 * HR, '<Weekday> afternoon'),
          (15 * HR, 18 * HR, 'late <Weekday> afternoon'),
          (18 * HR, 21 * HR, '<Weekday> evening'),
          (21 * HR, 24 * HR, '<Weekday> night')]

        tomorrow = [
          (0 * HR, 3 * HR, 'after midnight <Weekday>'),
          (3 * HR, 6 * HR, 'early <Weekday>'),
          (6 * HR, 9 * HR, '<Weekday> morning'),
          (9 * HR, 12 * HR, 'late <Weekday> morning'),
          (12 * HR, 15 * HR, 'early <Weekday> afternoon'),
          (15 * HR, 18 * HR, '<Weekday> afternoon'),
          (18 * HR, 21 * HR, '<Weekday> evening'),
          (21 * HR, 24 * HR, 'late <Weekday> evening')]

        subsequentDay = [
          (0 * HR, 3 * HR, 'early <Weekday> morning'),
          (3 * HR, 6 * HR, '<Weekday> morning'),
          (6 * HR, 9 * HR, '<Weekday> morning'),
          (9 * HR, 12 * HR, 'late <Weekday> morning'),
          (12 * HR, 15 * HR, 'early <Weekday> afternoon'),
          (15 * HR, 18 * HR, '<Weekday> afternoon'),
          (18 * HR, 21 * HR, '<Weekday> evening'),
          (21 * HR, 24 * HR, '<Weekday> before midnight')]

        # determine local time
        myTimeZone = os.environ.get('TZ', 'GMT')  # save the defined time zone
        os.environ['TZ'] = timeZone  # set the new time zone
        ltissue = time.localtime(issueTime / 1000)  # issuance local time
        ltevent = time.localtime(eventTime / 1000)  # event local time

        # determine the delta days from issuance to event
        diffDays = ltevent[7] - ltissue[7]  # julian day
        if diffDays < 0:  # year wrap around, assume Dec/Jan
            diffDays = ltevent[2] + 31 - ltissue[2]  # day of month

        # get description time phrase
        description = '<Weekday>'
        hourmin = ltevent[3] * 3600 + ltevent[4] * 60  # hour, minute
        if diffDays == -1:
            for (startT, endT, desc) in yesterday:
                if hourmin >= startT and hourmin < endT:
                    description = desc
                    break
        elif diffDays == 0:
            for (startT, endT, desc) in today:
                if hourmin >= startT and hourmin < endT:
                    description = desc
                    break
        elif diffDays == 1:
            for (startT, endT, desc) in tomorrow:
                if hourmin >= startT and hourmin < endT:
                    description = desc
                    break
        else:
            for (startT, endT, desc) in subsequentDay:
                if hourmin >= startT and hourmin < endT:
                    description = desc
                    break

       # do substitution
        dow = ltevent[6]  # day of week
        description = string.replace(description, '<Weekday>',
          self.asciiDayOfWeek(dow))  # day of week
        os.environ['TZ'] = myTimeZone  # reset the defined time zone
        return description

    # calculates the NONE/EXPLICIT timing phrase
    def ctp_NONE_EXPLICIT(self, stext, etext, startPrefix, endPrefix):
        # single time zone
        if len(etext) == 1:
            hourStr, hourTZstr, description = etext[0]
            if len(description) > 0 :
                description = ' ' + description
            # special cases NOON
            if hourStr == '12 PM':
               hourStr = 'Noon'
            return endPrefix + ' ' + hourStr + ' ' + hourTZstr + ' ' + \
              description
        
        # multiple time zones            
        elif len(etext) > 1:
            hourStr, hourTZstr, description = etext
            if len(description) > 0 :
                description = ' ' + description
            # special cases NOON
            if hourStr == '12 PM':
               hourStr = 'Noon'
            s = endPrefix + ' ' + hourStr + ' ' + hourTZstr + ' '
            for x in xrange(1, len(etext)):
                hourStr, hourTZstr, othDescription = etext[x]
                # special cases NOON
                if hourStr == '12 PM':
                   hourStr = 'Noon'
                s = s + '/' + hourStr + ' ' + hourTZstr + '/'
            return s + description
    
    # calculates the NONE/FUZZY timing phrase
    def ctp_NONE_FUZZY(self, stext, etext, startPrefix, endPrefix):
        # returns phrase like:  THROUGH THIS EVENING
        hourStr, hourTZstr, description = etext[0]  # ending text
        s = endPrefix + ' ' + description
        return s
    
    # calculates the NONE/EXPLICIT timing phrase
    def ctp_EXPLICIT_EXPLICIT(self, stext, etext, startPrefix, endPrefix):
        # return phrases like:  
        #  FROM 2 AM WEDNESDAY TO 2 AM CST THURSDAY
        #  FROM 2 AM TO 5 AM CST THURSDAY
        #  FROM 2 AM CST /1 AM MST/ WEDNESDAY TO 2 AM CST /1 AM MST/ THURSDAY
        #  FROM 2 AM CST /1 AM MST/ TO 6 AM CST /5AM MST/ THURSDAY

        shourStr, shourTZstr, sdescription = stext[0]  # starting text
        ehourStr, ehourTZstr, edescription = etext[0]  # ending text

        # special cases NOON
        if shourStr == '12 PM':
           shourStr = 'Noon'

        # special cases NOON
        if ehourStr == '12 PM':
           ehourStr = 'Noon'

        # special case EARLY THIS MORNING and THIS MORNING, replace with
        # just THIS MORNING
        if sdescription == 'early this morning' and \
          edescription == 'this morning':
            sdescription = 'this morning'  # combine two phrases
 

        # single time zone, same time zone for start/end times - same day
        if len(stext) == 1 and len(etext) == 1 and \
          shourTZstr == ehourTZstr and sdescription == edescription:
            return startPrefix + ' ' + shourStr + ' ' + endPrefix + ' ' + \
              ehourStr + ' ' + ehourTZstr + ' ' + edescription

        # single time zone, same time zone for start/end times - diff day
        if len(stext) == 1 and len(etext) == 1 and \
          shourTZstr == ehourTZstr and sdescription != edescription:
            return startPrefix + ' ' + shourStr + ' ' + sdescription + \
              ' ' + endPrefix + ' ' + ehourStr + ' ' + ehourTZstr + \
              ' ' + edescription

        # mult time zones, same day for start/end times
        if sdescription == edescription:
            s = startPrefix + ' ' + shourStr + ' ' + shourTZstr + ' '
            for x in xrange(1, len(stext)):
                hourStr, hourTZstr, description = stext[x]
                # special cases NOON
                if hourStr == '12 PM':
                   hourStr = 'Noon'
                s = s + '/' + hourStr + ' ' + hourTZstr + '/ '
            s = s + endPrefix + ' ' + ehourStr + ' ' + ehourTZstr + ' '
            for x in xrange(1, len(etext)):
                hourStr, hourTZstr, description = etext[x]
                # special cases NOON
                if hourStr == '12 PM':
                   hourStr = 'Noon'
                s = s + '/' + hourStr + ' ' + hourTZstr + '/ '
            s = s + edescription
            return s

        # mult time zones, different day for start/end times
        else:
            s = startPrefix + ' ' + shourStr + ' ' + shourTZstr + ' '
            for x in xrange(1, len(stext)):
                hourStr, hourTZstr, description = stext[x]
                # special cases NOON
                if hourStr == '12 PM':
                   hourStr = 'Noon'
                s = s + '/' + hourStr + ' ' + hourTZstr + '/ '
            s = s + sdescription + ' ' + endPrefix + ' ' + ehourStr + \
              ' ' + ehourTZstr + ' '
            for x in xrange(1, len(etext)):
                hourStr, hourTZstr, description = etext[x]
                # special cases NOON
                if hourStr == '12 PM':
                   hourStr = 'Noon'
                s = s + '/' + hourStr + ' ' + hourTZstr + '/ '
            s = s + edescription
            return s
    
    # calculates the NONE/EXPLICIT timing phrase
    def ctp_EXPLICIT_FUZZY(self, stext, etext, startPrefix, endPrefix):
        # returns phrase like:
        #    FROM 2 AM CST WEDNESDAY THROUGH LATE WEDNESDAY NIGHT
        #    FROM 2 AM CST /1 AM MST/ WEDNESDAY THROUGH LATE WEDNESDAY NIGHT

        # start phrase
        hourStr, hourTZstr, description0 = stext[0]
        # special cases NOON
        if hourStr == '12 PM':
           hourStr = 'Noon'
        s = startPrefix + ' ' + hourStr + ' ' + hourTZstr + ' ' 
        for x in xrange(1, len(stext)):
            hourStr, hourTZstr, description = stext[x]
            # special cases NOON
            if hourStr == '12 PM':
               hourStr = 'Noon'
            s = s + '/' + hourStr + ' ' + hourTZstr + '/ '
        s = s + description0 + ' '

        # end phrase
        hourStr, hourTZstr, description = etext[0]
        s = s + endPrefix + ' ' + description
        
        return s
    
    # calculates the FUZZY/FUZZY timing phrase
    def ctp_FUZZY_FUZZY(self, stext, etext, startPrefix, endPrefix):
        # return phrases like FROM THIS EVENING THROUGH LATE WEDNESDAY NIGHT
        # return phrases like LATE WEDNESDAY NIGHT

        hourStr, hourTZstr, s_description = stext[0]  # starting text
        hourStr, hourTZstr, e_description = etext[0]  # ending text

        # special case of description the same
        if s_description == e_description:
            return s_description

        # normal case of different descriptions
        s = startPrefix + ' ' + s_description + ' ' + endPrefix + ' ' + \
          e_description

        return s
 
    def ctp_NONE_DAYNIGHT(self, stext, etext, startPrefix, endPrefix):
        # return phrases like THROUGH WEDNESDAY

        hourStr, hourTZstr, e_description = etext[0]  # ending text

        s = endPrefix + ' ' + e_description

        return s

    def ctp_EXPLICIT_DAYNIGHT(self, stext, etext, startPrefix, endPrefix):
        # returns phrase like:
        #    FROM 2 AM CST WEDNESDAY THROUGH WEDNESDAY
        #    FROM 2 AM CST /1 AM MST/ WEDNESDAY THROUGH WEDNESDAY

        # start phrase
        hourStr, hourTZstr, description0 = stext[0]
        # special cases NOON
        if hourStr == '12 PM':
           hourStr = 'Noon'
        s = startPrefix + ' ' + hourStr + ' ' + hourTZstr + ' ' 
        for x in xrange(1, len(stext)):
            hourStr, hourTZstr, description = stext[x]
            # special cases NOON
            if hourStr == '12 PM':
               hourStr = 'Noon'
            s = s + '/' + hourStr + ' ' + hourTZstr + '/ '
        s = s + description0 + ' '

        # end phrase
        hourStr, hourTZstr, description = etext[0]
        s = s + endPrefix + ' ' + description
        
        return s

    def ctp_FUZZY_DAYNIGHT(self, stext, etext, startPrefix, endPrefix):
        # return phrases like FROM THIS EVENING THROUGH WEDNESDAY NIGHT

        hourStr, hourTZstr, s_description = stext[0]  # starting text
        hourStr, hourTZstr, e_description = etext[0]  # ending text

        # special case of description the same
        if s_description == e_description:
            return s_description

        # normal case of different descriptions
        s = startPrefix + ' ' + s_description + ' ' + endPrefix + ' ' + \
          e_description

        return s

    def ctp_DAYNIGHT_DAYNIGHT(self, stext, etext, startPrefix, endPrefix):
        # return phrases like FROM TONIGHT THROUGH WEDNESDAY

        hourStr, hourTZstr, s_description = stext[0]  # starting text
        hourStr, hourTZstr, e_description = etext[0]  # ending text

        # special case of description the same
        if s_description == e_description:
            return s_description

        # normal case of different descriptions
        s = startPrefix + ' ' + s_description + ' ' + endPrefix + ' ' + \
          e_description

        return s

    def ctp_DAYNIGHT_EXPLICIT(self, stext, etext, startPrefix, endPrefix):
        # returns phrase like:
        #    FROM TUESDAY UNTIL 2 AM CST WEDNESDAY
        #    FROM TUESDAY UNTIL 2 AM CST /1 AM MST/ WEDNESDAY

        # start phrase
        hourStr, hourTZstr, description = stext[0]
        s = startPrefix + ' ' + description + ' '

        # end phrase
        hourStr, hourTZstr, description0 = etext[0]
        # special cases NOON
        if hourStr == '12 PM':
           hourStr = 'Noon'
        s = s + endPrefix + ' ' + hourStr + ' ' + hourTZstr + ' ' 
        for x in xrange(1, len(etext)):
            hourStr, hourTZstr, description = etext[x]
            # special cases NOON
            if hourStr == '12 PM':
               hourStr = 'Noon'
            s = s + '/' + hourStr + ' ' + hourTZstr + '/ '
        s = s + description0 + ' '

        return s

    def ctp_DAYNIGHT_NONE(self, stext, etext, startPrefix, endPrefix):
        # return phrases like FROM TONIGHT

        hourStr, hourTZstr, s_description = stext[0]  # starting text

        s = startPrefix + ' ' + s_description

        return s

    def ctp_DAYNIGHT_FUZZY(self, stext, etext, startPrefix, endPrefix):
        # return phrases like FROM TONIGHT THROUGH WEDNESDAY NIGHT

        hourStr, hourTZstr, s_description = stext[0]  # starting text
        hourStr, hourTZstr, e_description = etext[0]  # ending text

        # special case of description the same
        if s_description == e_description:
            return s_description

        # normal case of different descriptions
        s = startPrefix + ' ' + s_description + ' ' + endPrefix + ' ' + \
          e_description

        return s


    # utility for attribution, takes hazard description ['hdln'] field and
    # adds TEST if appropriate in test mode, adds 'A' or 'AN' as appropriate
    # if desired. 
    def hazardName(self, name, testMode, addA=False, removePrefix=True):
 
        if len(name) == 0:
            return name

        name = name.title()
        # Remove Areal or River from the name
        if removePrefix:
            if name.upper().startswith("RIVER ") or name.upper().startswith("AREAL "):
                name = name[6:]

        # test mode
        if testMode:
            phrase = 'Test ' + name  # test mode, prepend 'Test'
        else:
            phrase = name

        # want A or AN?
        if addA:
            if phrase[0] in ['A', 'E', 'I', 'O', 'U', 'a', 'e', 'i', 'o', 'u']:
                phrase = 'an ' + phrase
            else:
                phrase = 'a ' + phrase
        return phrase

######################  FROM GFE Header.py

    def getExpireTime(self, issueTime, purgeHours, vtecRecords, roundMinutes=15,
        fixedExpire=0):
        '''
        Given the issuance time, purgeHours, and the vtecRecords (with times converted to ms),
        returns the appropriate expiration time.  
        
        Expiration time is the earliest of the specified expiration time, 1 hr if a CAN code
        is detected, or the ending time of ongoing events (CON, EXT, EXB, NEW).
        The issueTime and expireTime are ints in milliseconds. 
        
        @param issueTime in ms
        @param purgeHours -- set time past issuance time. 
               The default for this is set by policy e.g. an FFA expires by default
               in 8 hours. However, if there is a hazard end time earlier, then that
               is used.
               if -1, then hazard end time is to be used
        @param vtecRecords in the segment with times converted to ms
        @param roundMinutes
        @param fixedExpire -- indicates to ignore the VTEC actions when computing the 
               expiration time
        
        '''
        if purgeHours > 0:
            expireTime = issueTime + purgeHours * 3600 * 1000
        else:
            expireTime = None
            # Pick the earliest end time of the vtecRecords in the segment
            for vtecRecord in vtecRecords:
                if expireTime is None or vtecRecord.get('endTime') < expireTime:
                    expireTime = vtecRecord.get('endTime')

        if not fixedExpire:
            canExpFound = 0
            activeFound = 0
            laterActive = None  # later end time of all active events
            for vtecRecord in vtecRecords: 
                action = vtecRecord.get('act')
                if action in  ['CAN', 'EXP']:
                    canExpFound = 1
                elif action in ['NEW', 'CON', 'EXT', 'EXB', 'EXA']:
                    activeFound = 1
                    endTime = vtecRecord.get('endTime')
                    if endTime != 0:
                        if laterActive is not None:
                            laterActive = max(laterActive, endTime)
                        else:
                            laterActive = endTime
            if laterActive is not None:
                expireTime = min(expireTime, laterActive)
            elif canExpFound and not activeFound:
                expireTime = min(expireTime, issueTime+3600*1000)  #1hr from now
                
        #ensure expireTime is not before issueTime, and is at least 1 hour
        if expireTime - issueTime < 3600*1000:
            expireTime = issueTime + 3600*1000

        # round to next 'roundMinutes'
        roundValue = roundMinutes * 60 * 1000  # in milliseconds
        delta = expireTime % roundValue  # in milliseconds
        baseTime = int(expireTime / roundValue) * roundValue
        if delta / 60 * 1000 >= 1:  # add the next increment
            expireTime = baseTime + roundValue
        else:  # within 1 minute, don't add the next increment
            expireTime = baseTime
        return expireTime
    
    def getGeneralAreaList(self, areaList):
        '''
        Returns a list of strings that describe the 'areaList', such as
        Southwest Kansas, along with their county/zone names.  Format returned
        is [(stateName, portionOfState, [(county/zone list, type)])].  The type
        is PARISH, COUNTY, ZONE, INDEPENDENT CITY.  Duplicate names are
        eliminated.
        '''
        geoAreas = {}
        
        for areaName in areaList:

            state = self.getInformationForUGC(areaName, "fullStateName")
            if state == "" :
                state == areaName[:2]
            # Special District of Columbia case
            if state == 'DISTRICT OF COLUMBIA':
                state = 'THE DISTRICT OF COLUMBIA'

            zoneName = self.getInformationForUGC(areaName, "entityName")
            if zoneName == "" :
                continue
            partOfState = self.getInformationForUGC(areaName, "partOfState")
            nameType = self.getInformationForUGC(areaName, "type")

            value = (state, partOfState)
            znt = (zoneName, nameType)
                 
            if geoAreas.has_key(value):
                names = geoAreas[value]
                if znt not in names:
                    names.append(znt)
            else:
                geoAreas[value] = [znt]

        # now sort the zoneName or countyNames
        for state, partState in geoAreas.keys():
            names = geoAreas[(state, partState)]
            names.sort()

        # now separate them by land and water
        # Anything to do with WATERS or other related items go last
        waters = ['WATERS', 'LAKE', 'RIVER']
        gaLAND = []
        gaWATER = []
        for g, pg in geoAreas.keys():
            names = geoAreas[(g, pg)]
            words = g.split(' ')
            found = 0
            for w in waters:
                if w in words:
                    found = 1
                    break
            if found:
                gaWATER.append((g, pg, names))
            else:
                gaLAND.append((g, pg, names))

        # convert the output to a list with land first, then waters
        geoAreas = []
        for g in gaLAND:
            geoAreas.append(g)
        for g in gaWATER:
            geoAreas.append(g)

        geoAreas.sort()

        return geoAreas    
    
    def hazard_hook(self, tree, node, phen, sig, act, start, end):
        return ''
        
################### From GFE StringUtils 
    def endline(self, phrase, linelength=66, breakStr=[" ", "..."]):
        "Insert endlines into phrase"

        # Break into sub-phrases separated by \n
        subPhrases = string.split(phrase, "\n")

        # Break each sub-phrase into lines
        str = ""
        for subPhrase in subPhrases:
            if subPhrase == "":
                str = str + "\n"
            else:
                str = str + self.linebreak(subPhrase, linelength, breakStr)
        return str

    def linebreak(self, phrase, linelength, breakStr=[' ', '...'],
                  forceBreakStr=[" ", "/"]):
        # Break phrase into lines of the given linelength
        # Prevents a line break on a number.
        # If no breakStr is found for a given linelength of characters,
        # force a break on the rightmost forceBreakStr.
        text = ''
        start = 0
        end = start + linelength
        subPhrase = phrase[start:end]
        while len(subPhrase) == linelength:
            maxIndex, breakChars = self.findRightMost(subPhrase, breakStr)
            if maxIndex == -1:
                # Didn't find any breakStr; line is too long.
                # Find the rightmost force break string, if possible.
                forceIndex, breakChars = self.findRightMost(subPhrase, forceBreakStr)
                if forceIndex == 0:
                    # space in first position: will be skipped.
                    pass
                elif forceIndex > 0:
                    subPhrase = subPhrase[0:forceIndex]
                    text = '%s%s\n' % (text, subPhrase)
                    start += forceIndex
                else:
                    # no forcebreak spot, either.
                    # break at linelength.
                    text = '%s%s\n' % (text, subPhrase)
                    start += linelength
            elif maxIndex == 0:
                pass  # space in first position: will be skipped
            else:
                text = '%s%s\n' % (text, subPhrase[:maxIndex])
                start += maxIndex
            if breakChars == " ":
                # Skip the space
                start += 1
            end = start + linelength
            subPhrase = phrase[start:end]
        if subPhrase:
            return '%s%s\n' % (text, subPhrase)
        else:
            # It's possible for subPhrase to be [] coming out of the while
            # loop. In that case, we just need to return text.
            return text

    def findRightMost(self, text, breakStr=[" "], nonNumeric=1):
        # Return the index of the right most break string characters
        # and the break characters that were found.
        # If nonNumeric, then make sure the index does not refer to
        # a numeric character.
        # If the break characters are a space, the index indicate
        # the character prior to the space.
        maxIndex = -1
        maxChars = ''
        for breakChars in breakStr:
            index = text.rfind(breakChars)
            done = False
            while index > 0 and not done:
                # Check for a numeric at end of line
                if nonNumeric and breakChars == " " and text[index - 1].isdigit():
                    # Try to find the next right most break char
                    index = text.rfind(breakChars, 0, index - 1)
                    continue
                done = True
            if index > maxIndex:
                maxIndex = index
                maxChars = breakChars
        if maxIndex == -1:
            return maxIndex, maxChars
        if maxChars == ' ':
            index = maxIndex
        else:
            # We want to keep the breakChars, which are assumed not to end
            # with a number
            index = maxIndex + len(maxChars)
        return index, maxChars
  
    def indentText(self, text, indentFirstString='', indentNextString='',
                   maxWidth=69, breakStrings=[' ']):
        '''
        IndentText returns a formatted string which is at most maxWidth
        columns in width, with the first line indented by 'indentFirstString'
        and subsequent lines indented by indentNextString.  Any leading spaces
        in the first line are preserved.
        
        '''
        
        out = ''  # total output
        line = ''  # each line

        # eliminate all new lines and create a list of words
        words = string.split(text, '\n')
        words = self.splitIntoWords(words, breakStrings)
            
        # eliminate all new lines and create a list of words     
        if len(words) == 0:
            return ''

        # find out how many spaces the 1st line has been indented based on
        # the input text.
        additionalIndent = string.find(text, words[0])
        firstLineAdditionalIndent = ''
        for i in xrange(additionalIndent):
            firstLineAdditionalIndent = firstLineAdditionalIndent + ' '

        # now start assembling the output
        line = line + indentFirstString + firstLineAdditionalIndent
        additional = indentFirstString + firstLineAdditionalIndent
        for w in words:
            if len(line) + len(w) + 1 > maxWidth:
                out = out + line + '\n'
                line = indentNextString + w
            else:
                if len(out) == 0 and len(line) == len(additional):
                    line = line + w  # first line, don't add a space
                else:
                    # line = line + ' ' + w   #subsequent words, add a space
                    line = line + w  # subsequent words, add a space
        if len(line):
            out = out + line
        return out

    def splitIntoWords(self, words, breakStrings=[' ']):
        # Break the list of words further
        # using the list of breakStrings.
        for breakStr in breakStrings:
            newWords = []
            # Break each word on the breakStr
            for word in words:
                # Split the word with breakStr
                strWords = string.split(word, breakStr)
                if len(strWords) > 1:
                    newStrWords = []
                    # Add the breakStr back in except for last one
                    index = 0
                    length = len(strWords) - 1
                    for strWord in strWords:
                        if strWord == '':
                            continue
                        if index < length:
                            strWord += breakStr
                        newStrWords.append(strWord)
                        index += 1
                    strWords = newStrWords
                # Add these words to the new words list
                newWords = newWords + strWords
            words = newWords
        return words
    

#  This class is copied from the CallToActions GHG code.
#  For PV2, it will be transitioned to the Hazard Services meta-information
#  in localization.
class CallToActions(object):
    def __init__(self):
        pass

#    def pydevDebug(self):
#        import sys
#        PYDEVD_PATH='/home/rtran/awipsdr4/Ade/eclipse/plugins/org.python.pydev.debug_1.5.4.2010011921/pysrc'
#        if sys.path.count(PYDEVD_PATH) < 1:
#            sys.path.append(PYDEVD_PATH)
#
#        import pydevd
#        pydevd.settrace() 

    # returns the default Call To Action 
    def defaultCTA(self, phensig):
        if self.ctaDict().has_key(phensig):
            func = self.ctaDict()[phensig]
            items = func()
            if len(items) > 0:
                return items[0]
        return ''  # No call to action

    def allCTAs(self, phensig):
        if self.ctaDict().has_key(phensig):
            func = self.ctaDict()[phensig]
            return func() 
        return []  # no Call to actions


    def pilCTAs(self, pil):
        if self.ctaPilDict().has_key(pil):
            func = self.ctaPilDict()[pil]
            return func() 
        return []  # no Call to actions


    # returns list of generic call to action statements
    def genericCTAs(self):
        return [
 '''MONITOR NOAA WEATHER RADIO FOR THE LATEST INFORMATION...FORECASTS...AND WARNINGS.''',
 '''LISTEN TO NOAA WEATHER RADIO OR YOUR LOCAL MEDIA FOR THE LATEST UPDATES ON THIS SITUATION.''',
        ]

        

##### PLEASE KEEP PHENSIG IN ALPHABETICAL ORDER #######

# CallToAction dictionary.  The key is the phen/sig, such as 'BZ.W'.  The
# value is a LIST of call to action statements.  The default formatter
# uses the first one in the list. Users can add additional entries which
# are accessible in the product editor.  The lists are actually function
# calls that the user can override if necessary.
# Updated in 9.3 to sync with VTECTable entries  
    def ctaDict(self):
        return {
         'AF.W': self.ctaAFW,
         'AF.Y': self.ctaAFY,
         'AS.O': self.ctaASO,
         'AS.Y': self.ctaASY,
         'BW.Y': self.ctaBWY,
         'BZ.A': self.ctaBZA,
         'BZ.W': self.ctaBZW,
         'CF.A': self.ctaCFA,
         'CF.W': self.ctaCFW,
         'CF.Y': self.ctaCFY,
         'DS.W': self.ctaDSW,
         'DU.Y': self.ctaDUY,
         'EC.A': self.ctaECA,
         'EC.W': self.ctaECW,
         'EH.A': self.ctaEHA,
         'EH.W': self.ctaEHW,
         'FA.A': self.ctaFAA,
         'FF.A': self.ctaFFA,
         'FG.Y': self.ctaFGY,
         'FR.Y': self.ctaFRY,
         'FW.A': self.ctaFWA,
         'FW.W': self.ctaFWW,
         'FZ.A': self.ctaFZA,
         'FZ.W': self.ctaFZW,
         'GL.A': self.ctaGLA,
         'GL.W': self.ctaGLW,
         'HF.A': self.ctaHFA,
         'HF.W': self.ctaHFW,
         'HT.Y': self.ctaHTY,
         'HU.A': self.ctaHUA,
         'HU.W': self.ctaHUW,
         'HW.A': self.ctaHWA,
         'HW.W': self.ctaHWW,
         'HZ.A': self.ctaHZA,
         'HZ.W': self.ctaHZW,
         'IS.W': self.ctaISW,
         'LE.A': self.ctaLEA,
         'LE.W': self.ctaLEW,
         'LE.Y': self.ctaLEY,
         'LO.Y': self.ctaLOY,
         'LS.A': self.ctaLSA,
         'LS.W': self.ctaLSW,
         'LS.Y': self.ctaLSY,
         'LW.Y': self.ctaLWY,
         'MF.Y': self.ctaMFY,
         'MH.W': self.ctaMHW,
         'MH.Y': self.ctaMHY,
         'MS.Y': self.ctaMSY,
         'RB.Y': self.ctaRBY,
         'SC.Y': self.ctaSCY,
         'SE.A': self.ctaSEA,
         'SE.W': self.ctaSEW,
         'SI.Y': self.ctaSIY,
         'SM.Y': self.ctaSMY,
         'SR.A': self.ctaSRA,
         'SR.W': self.ctaSRW,
         'SU.W': self.ctaSUW,
         'SU.Y': self.ctaSUY,
         'SW.Y': self.ctaSWY,
         'TR.A': self.ctaTRA,
         'TR.W': self.ctaTRW,
         'UP.A': self.ctaUPA,
         'UP.W': self.ctaUPW,
         'UP.Y': self.ctaUPY,
         'WC.A': self.ctaWCA,
         'WC.W': self.ctaWCW,
         'WC.Y': self.ctaWCY,
         'WI.Y': self.ctaWIY,
         'WS.A': self.ctaWSA,
         'WS.W': self.ctaWSW,
         'WW.Y': self.ctaWWY,
         'ZF.Y': self.ctaZFY,
         'ZR.Y': self.ctaZRY,
          }


##### PLEASE KEEP PILS IN ALPHABETICAL ORDER #######

# CallToAction PIL dictionary.  The key is the product pil, such as 'HLS'.
# The entries are available for a particular product.  None of these
# are entered automatically by the formatter, but are available through
# the product editor.
# Users can add additional entries which are accessible in the product 
# editor.  The lists are actually function calls that the user can 
# override if necessary.  
    def ctaPilDict(self):
        return {
          'ADR':  self.ctaPilADR,
          'AFD':  self.ctaPilAFD,
          'AFM':  self.ctaPilAFM,
          'AVA':  self.ctaPilAVA,
          'AVW':  self.ctaPilAVW,
          'CAE':  self.ctaPilCAE,
          'CCF':  self.ctaPilCCF,
          'CDW':  self.ctaPilCDW,
          'CEM':  self.ctaPilCEM,
          'CFW':  self.ctaPilCFW,
          'CWF':  self.ctaPilCWF,
          'EQR':  self.ctaPilEQR,
          'EQW':  self.ctaPilEQW,
          'ESF':  self.ctaPilESF,
          'EVI':  self.ctaPilEVI,
          'FFA':  self.ctaPilFFA,
          'FRW':  self.ctaPilFRW,
          'FWF':  self.ctaPilFWF,
          'FWM':  self.ctaPilFWM,
          'FWS':  self.ctaPilFWS,
          'GLF':  self.ctaPilGLF,
          'HLS':  self.ctaPilHLS,
          'HMW':  self.ctaPilHMW,
          'HWO':  self.ctaPilHWO,
          'LAE':  self.ctaPilLAE,
          'LEW':  self.ctaPilLEW,
          'MWS':  self.ctaPilMWS,
          'MVF':  self.ctaPilMVF,
          'MWW':  self.ctaPilMWW,
          'NOW':  self.ctaPilNOW,
          'NPW':  self.ctaPilNPW,
          'NSH':  self.ctaPilNSH,
          'NUW':  self.ctaPilNUW,
          'OFF':  self.ctaPilOFF,
          'PFM':  self.ctaPilPFM,
          'PNS':  self.ctaPilPNS,
          'RFD':  self.ctaPilRFD,
          'RFW':  self.ctaPilRFW,
          'RHW':  self.ctaPilRHW,
          'SAF':  self.ctaPilSAF,
          'SRF':  self.ctaPilSRF,
          'SFT':  self.ctaPilSFT,
          'SPS':  self.ctaPilSPS,
          'SPW':  self.ctaPilSPW,
          'TOE':  self.ctaPilTOE,
          'VOW':  self.ctaPilVOW,
          'WCN':  self.ctaPilWCN,
          'WSW':  self.ctaPilWSW,
          'ZFP':  self.ctaPilZFP,
          }


#------------------------------------------------------------------------
# CALL TO ACTIONS - winter events
# With the winter weather simplification, specific winter hazard defs are not
# readily available. Forecaster can choose from the specific defs via the defs below.
# Since these statements are so long, we use the descriptive word format.
#------------------------------------------------------------------------
    def winterWScta(self):
        return [
      ('***HEAVY SNOW', '''A WINTER STORM WARNING FOR HEAVY SNOW MEANS SEVERE WINTER WEATHER CONDITIONS ARE EXPECTED OR OCCURRING.  SIGNIFICANT AMOUNTS OF SNOW ARE FORECAST THAT WILL MAKE TRAVEL DANGEROUS. ONLY TRAVEL IN AN EMERGENCY. IF YOU MUST TRAVEL...KEEP AN EXTRA FLASHLIGHT...FOOD...AND WATER IN YOUR VEHICLE IN CASE OF AN EMERGENCY.'''),
      ('***SLEET', '''A WINTER STORM WARNING FOR SLEET MEANS THAT A WINTER STORM SYSTEM IS IMPACTING THE AREA WITH SIGNIFICANT AMOUNTS OF SLEET. TRAVEL IS LIKELY TO BE SEVERELY IMPACTED.'''),
      ('***MIXED PRECIP', '''A WINTER STORM WARNING MEANS SIGNIFICANT AMOUNTS OF SNOW...SLEET...AND ICE ARE EXPECTED OR OCCURRING. STRONG WINDS ARE ALSO POSSIBLE.  THIS WILL MAKE TRAVEL VERY HAZARDOUS OR IMPOSSIBLE.'''),
              ]

    def winterWWcta(self):
        return [
      ('***BLOWING SNOW', '''A WINTER WEATHER ADVISORY FOR BLOWING SNOW MEANS THAT VISIBILITIES WILL BE LIMITED DUE TO STRONG WINDS BLOWING SNOW AROUND. USE CAUTION WHEN TRAVELING...ESPECIALLY IN OPEN AREAS.'''),
      ('***SLEET', '''A WINTER WEATHER ADVISORY FOR SLEET MEANS PERIODS OF SLEET ARE IMMINENT OR OCCURRING. SLEET MAY CAUSE DRIVING TO BECOME EXTREMELY DANGEROUS...SO BE PREPARED TO USE CAUTION WHEN TRAVELING.'''),
      ('***SNOW AND BLOWING SNOW', '''A WINTER WEATHER ADVISORY FOR |*LAKE EFFECT*| SNOW AND BLOWING SNOW MEANS THAT VISIBILITIES WILL BE LIMITED DUE TO A COMBINATION OF FALLING AND BLOWING SNOW. USE CAUTION WHEN TRAVELING...ESPECIALLY IN OPEN AREAS.'''),
      ('***SNOW', '''A WINTER WEATHER ADVISORY FOR SNOW MEANS THAT PERIODS OF SNOW WILL CAUSE PRIMARILY TRAVEL DIFFICULTIES. BE PREPARED FOR SNOW COVERED ROADS AND LIMITED VISIBILITIES...AND USE CAUTION WHILE DRIVING.'''),
      ('***MIXED PRECIP', '''A WINTER WEATHER ADVISORY MEANS THAT PERIODS OF SNOW...SLEET...OR FREEZING RAIN WILL CAUSE TRAVEL DIFFICULTIES. BE PREPARED FOR SLIPPERY ROADS AND LIMITED VISIBILITIES...AND USE CAUTION WHILE DRIVING.'''),
        ]
#------------------------------------------------------------------------
# CALL TO ACTIONS - individual functions for each phen/sig
#------------------------------------------------------------------------
# These are lists of strings.  The first one is used in the formatters,
# the others are available through the call to actions menu.

    def ctaAFW(self):
        return [ 
'''AN ASHFALL WARNING MEANS THAT SIGNIFICANT ACCUMULATION OF VOLCANIC ASH IS EXPECTED OR OCCURRING DUE TO A VOLCANIC ERUPTION OR RESUSPENSION OF PREVIOUSLY DEPOSITED ASH.
 
SEAL WINDOWS AND DOORS.  PROTECT ELECTRONICS AND COVER AIR INTAKES AND OPEN WATER SOURCES.  AVOID DRIVING. REMAIN INDOORS UNLESS ABSOLUTELY NECESSARY.  USE EXTREME CAUTION CLEARING ROOFTOPS OF ASH.

LISTEN TO NOAA WEATHER RADIO OR LOCAL MEDIA FOR FURTHER INFORMATION.''',
        ]

    def ctaAFY(self):
        return [
 '''AN ASHFALL ADVISORY MEANS THAT LARGE AMOUNTS OF ASH WILL BE DEPOSITED IN THE ADVISORY AREA. PERSONS WITH RESPIRATORY ILLNESSES SHOULD REMAIN INDOORS TO AVOID INHALING THE ASH PARTICLES...AND ALL PERSONS OUTSIDE SHOULD COVER THEIR MOUTH AND NOSE WITH A MASK OR CLOTH.''',
        ]

    def ctaASO(self):
        return [
 '''AN AIR STAGNATION OUTLOOK IS ISSUED WHEN AN EXTENDED PERIOD OF WEATHER CONDITIONS ARE ANTICIPATED THAT COULD CONTRIBUTE TO POOR VENTILATION...AND THUS POTENTIALLY POOR AIR QUALITY.  BE PREPARED FOR THESE CONDITIONS TO DEVELOP IN THE NEXT 2 TO 3 DAYS...AND FOR THE ISSUANCE OF AIR STAGNATION ADVISORIES AS THE SITUATION BECOMES IMMINENT.''',
        ]

    def ctaASY(self):
        return [
 '''AN AIR STAGNATION ADVISORY INDICATES THAT DUE TO LIMITED MOVEMENT OF AN AIR MASS ACROSS THE ADVISORY AREA...POLLUTION WILL INCREASE TO DANGEROUS LEVELS. PERSONS WITH RESPIRATORY ILLNESS SHOULD FOLLOW THEIR PHYSICIANS ADVICE FOR DEALING WITH HIGH LEVELS OF AIR POLLUTION.''',
        ]

    def ctaBWY(self):
        return [
 '''A BRISK WIND ADVISORY MEANS THAT WINDS WILL REACH SMALL CRAFT ADVISORY CRITERIA IN AREAS THAT ARE PRIMARILY ICE COVERED. MOVING ICE FLOES COULD DAMAGE SMALL CRAFT.''',
        ]

    def ctaBZA(self):
        return [
'''A BLIZZARD WATCH MEANS THERE IS A POTENTIAL FOR FALLING AND/OR BLOWING SNOW WITH STRONG WINDS AND EXTREMELY POOR VISIBILITIES. THIS CAN LEAD TO WHITEOUT CONDITIONS AND MAKE TRAVEL VERY DANGEROUS.''',
        ]

    def ctaBZW(self):
        return [
 '''A BLIZZARD WARNING MEANS SEVERE WINTER WEATHER CONDITIONS ARE EXPECTED OR OCCURRING. FALLING AND BLOWING SNOW WITH STRONG WINDS AND POOR VISIBILITIES ARE LIKELY. THIS WILL LEAD TO WHITEOUT CONDITIONS...MAKING TRAVEL EXTREMELY DANGEROUS. DO NOT TRAVEL. IF YOU MUST TRAVEL...HAVE A WINTER SURVIVAL KIT WITH YOU. IF YOU GET STRANDED...STAY WITH YOUR VEHICLE.''',
        ]

    def ctaCFA(self):
        return [
 '''A COASTAL FLOOD WATCH MEANS THAT CONDITIONS FAVORABLE FOR FLOODING ARE EXPECTED TO DEVELOP. COASTAL RESIDENTS SHOULD BE ALERT FOR LATER STATEMENTS OR WARNINGS...AND TAKE ACTION TO PROTECT PROPERTY.''',
        ]

    def ctaCFW(self):
        return [
 '''A COASTAL FLOOD WARNING MEANS THAT FLOODING IS OCCURRING OR IMMINENT. COASTAL RESIDENTS IN THE WARNED AREA SHOULD BE ALERT FOR RISING WATER...AND TAKE APPROPRIATE ACTION TO PROTECT LIFE AND PROPERTY.''',
        ]

    def ctaCFY(self):
        return [
 '''A COASTAL FLOOD ADVISORY INDICATES THAT ONSHORE WINDS AND TIDES WILL COMBINE TO GENERATE FLOODING OF LOW AREAS ALONG THE SHORE.''',
        ]

    def ctaDSW(self):
        return [
 '''A DUST STORM WARNING MEANS SEVERELY LIMITED VISIBILITIES ARE EXPECTED WITH BLOWING DUST. TRAVEL COULD BECOME EXTREMELY DANGEROUS. PERSONS WITH RESPIRATORY PROBLEMS SHOULD MAKE PREPARATIONS TO STAY INDOORS UNTIL THE STORM PASSES.''',
        ]

    def ctaDUY(self):
        return [
 '''A BLOWING DUST ADVISORY MEANS THAT BLOWING DUST WILL RESTRICT VISIBILITIES. TRAVELERS ARE URGED TO USE CAUTION.''',
        ]

    def ctaECA(self):
        return [
 '''AN EXTREME COLD WATCH MEANS THAT PROLONGED PERIODS OF VERY COLD TEMPERATURES ARE EXPECTED. ENSURE THAT OUTDOOR ANIMALS HAVE WARM SHELTER...AND THAT CHILDREN WEAR A HAT AND GLOVES.''',
        ]

    def ctaECW(self):
        return [
 '''AN EXTREME COLD WARNING MEAN THAT DANGEROUSLY LOW TEMPERATURES ARE EXPECTED FOR A PROLONGED PERIOD OF TIME. FROSTBITE AND HYPOTHERMIA ARE LIKELY IF EXPOSED TO THESE TEMPERATURES...SO MAKE SURE A HAT...FACEMASK...AND HEAVY GLOVES OR MITTENS ARE AVAILABLE.''',
        ]

    def ctaEHA(self):
        return [
 '''AN EXCESSIVE HEAT WATCH MEANS THAT A PROLONGED PERIOD OF HOT TEMPERATURES IS EXPECTED. THE COMBINATION OF HOT TEMPERATURES AND HIGH HUMIDITY WILL COMBINE TO CREATE A DANGEROUS SITUATION IN WHICH HEAT ILLNESSES ARE POSSIBLE. DRINK PLENTY OF FLUIDS...STAY IN AN AIR-CONDITIONED ROOM...STAY OUT OF THE SUN...AND CHECK UP ON RELATIVES AND NEIGHBORS.''',
        ]

    def ctaEHW(self):
        return [
 '''AN EXCESSIVE HEAT WARNING MEANS THAT A PROLONGED PERIOD OF DANGEROUSLY HOT TEMPERATURES WILL OCCUR. THE COMBINATION OF HOT TEMPERATURES AND HIGH HUMIDITY WILL COMBINE TO CREATE A DANGEROUS SITUATION IN WHICH HEAT ILLNESSES ARE LIKELY. DRINK PLENTY OF FLUIDS...STAY IN AN AIR-CONDITIONED ROOM...STAY OUT OF THE SUN...AND CHECK UP ON RELATIVES AND NEIGHBORS.''',
        ]

    def ctaFAA(self):
        return [
 '''A FLOOD WATCH MEANS THERE IS A POTENTIAL FOR FLOODING BASED ON CURRENT FORECASTS.\n\nYOU SHOULD MONITOR LATER FORECASTS AND BE ALERT FOR POSSIBLE FLOOD WARNINGS. THOSE LIVING IN AREAS PRONE TO FLOODING SHOULD BE PREPARED TO TAKE ACTION SHOULD FLOODING DEVELOP.''',
        ]

    def ctaFFA(self):
        return [
 '''A FLASH FLOOD WATCH MEANS THAT CONDITIONS MAY DEVELOP THAT LEAD TO FLASH FLOODING. FLASH FLOODING IS A VERY DANGEROUS SITUATION.\n\nYOU SHOULD MONITOR LATER FORECASTS AND BE PREPARED TO TAKE ACTION SHOULD FLASH FLOOD WARNINGS BE ISSUED.''',
        ]

    def ctaFGY(self):
        return [
'''A DENSE FOG ADVISORY MEANS VISIBILITIES WILL FREQUENTLY BE REDUCED TO LESS THAN ONE QUARTER MILE. IF DRIVING...SLOW DOWN...USE YOUR HEADLIGHTS...AND LEAVE PLENTY OF DISTANCE AHEAD OF YOU.''',
        ]

    def ctaFRY(self):
        return [
 '''A FROST ADVISORY MEANS THAT FROST IS POSSIBLE. SENSITIVE OUTDOOR PLANTS MAY BE KILLED IF LEFT UNCOVERED.''',
        ]

    def ctaFWA(self):
        return [
 '''A FIRE WEATHER WATCH MEANS THAT CRITICAL FIRE WEATHER CONDITIONS ARE FORECAST TO OCCUR. LISTEN FOR LATER FORECASTS AND POSSIBLE RED FLAG WARNINGS.''',
        ]

    def ctaFWW(self):
        return [
 '''A RED FLAG WARNING MEANS THAT CRITICAL FIRE WEATHER CONDITIONS ARE EITHER OCCURRING NOW...OR WILL SHORTLY. A COMBINATION OF STRONG WINDS...LOW RELATIVE HUMIDITY...AND WARM TEMPERATURES WILL CREATE EXPLOSIVE FIRE GROWTH POTENTIAL.''',
        ]

    def ctaFZA(self):
        return [
 '''A FREEZE WATCH MEANS SUB-FREEZING TEMPERATURES ARE POSSIBLE. THESE CONDITIONS COULD KILL CROPS AND OTHER SENSITIVE VEGETATION.''',
        ]

    def ctaFZW(self):
        return [
 '''A FREEZE WARNING MEANS SUB-FREEZING TEMPERATURES ARE IMMINENT OR HIGHLY LIKELY. THESE CONDITIONS WILL KILL CROPS AND OTHER SENSITIVE VEGETATION.''',
        ]

    def ctaGLA(self):
        return [
 '''A GALE WATCH IS ISSUED WHEN THE RISK OF GALE FORCE WINDS OF 34 TO 47 KNOTS HAS SIGNIFICANTLY INCREASED...BUT THE SPECIFIC TIMING AND/OR LOCATION IS STILL UNCERTAIN. IT IS INTENDED TO PROVIDE ADDITIONAL LEAD TIME FOR MARINERS WHO MAY WISH TO CONSIDER ALTERING THEIR PLANS.''',
        ]

    def ctaGLW(self):
        return [
 '''A GALE WARNING MEANS WINDS OF 34 TO 47 KNOTS ARE IMMINENT OR OCCURING. OPERATING A VESSEL IN GALE CONDITIONS REQUIRES EXPERIENCE AND PROPERLY EQUIPPED VESSELS. IT IS HIGHLY RECOMMENDED THAT MARINERS WITHOUT THE PROPER EXPERIENCE SEEK SAFE HARBOR PRIOR TO THE ONSET OF GALE CONDITIONS.''',
        ]

    def ctaHFA(self):
        return [
 '''A HURRICANE FORCE WIND WATCH IS ISSUED WHEN THE RISK OF HURRICANE FORCE WINDS OF 64 KNOTS OR GREATER HAS SIGNIFICANTLY INCREASED...BUT THE SPECIFIC TIMING AND/OR LOCATION IS STILL UNCERTAIN.  IT IS INTENDED TO PROVIDE ADDITIONAL LEAD TIME FOR MARINERS WHO MAY WISH TO CONSIDER ALTERING THEIR PLANS.''',
        ]

    def ctaHFW(self):
        return [
 '''A HURRICANE FORCE WIND WARNING MEANS WINDS OF 64 KNOTS OR GREATER ARE IMMINENT OR OCCURING. ALL VESSELS SHOULD REMAIN IN PORT...OR TAKE SHELTER AS SOON AS POSSIBLE...UNTIL WINDS AND WAVES SUBSIDE.''',
        ]

    def ctaHTY(self):
        return [
 '''A HEAT ADVISORY MEANS THAT A PERIOD OF HOT TEMPERATURES IS EXPECTED. THE COMBINATION OF HOT TEMPERATURES AND HIGH HUMIDITY WILL COMBINE TO CREATE A SITUATION IN WHICH HEAT ILLNESSES ARE POSSIBLE. DRINK PLENTY OF FLUIDS...STAY IN AN AIR-CONDITIONED ROOM...STAY OUT OF THE SUN...AND CHECK UP ON RELATIVES AND NEIGHBORS.''',
        ]

    def ctaHUA(self):
        return [
 '''A HURRICANE WATCH IS ISSUED WHEN SUSTAINED WINDS OF |* 64 KTS OR 74 MPH *| OR HIGHER ASSOCIATED WITH A HURRICANE ARE POSSIBLE WITHIN 48 HOURS.''',
        ] 

    def ctaHUW(self):
        return [
 '''A HURRICANE WARNING MEANS SUSTAINED WINDS OF |* 64 KTS OR 74 MPH *| OR HIGHER ASSOCIATED WITH A HURRICANE ARE EXPECTED WITHIN 36 HOURS. A HURRICANE WARNING CAN REMAIN IN EFFECT WHEN DANGEROUSLY HIGH WATER OR A COMBINATION OF DANGEROUSLY HIGH WATER AND EXCEPTIONALLY HIGH WAVES CONTINUE...EVEN THOUGH WINDS MAY BE LESS THAN HURRICANE FORCE.''',
        ] 
    
    def ctaHWA(self):
        return [
 '''A HIGH WIND WATCH MEANS THERE IS THE POTENTIAL FOR A HAZARDOUS HIGH WIND EVENT. SUSTAINED WINDS OF AT LEAST 40 MPH...OR GUSTS OF 58 MPH OR STRONGER MAY OCCUR. CONTINUE TO MONITOR THE LATEST FORECASTS.''',
        ]

    def ctaHWW(self):
        return [
 '''A HIGH WIND WARNING MEANS A HAZARDOUS HIGH WIND EVENT IS EXPECTED OR OCCURRING. SUSTAINED WIND SPEEDS OF AT LEAST 40 MPH OR GUSTS OF 58 MPH OR MORE CAN LEAD TO PROPERTY DAMAGE.''',
        ]

    def ctaHZA(self):
        return [
 '''A HARD FREEZE WATCH MEANS SUB-FREEZING TEMPERATURES ARE POSSIBLE. THESE CONDITIONS COULD KILL CROPS AND OTHER SENSITIVE VEGETATION.''',
        ]

    def ctaHZW(self):
        return [
 '''A HARD FREEZE WARNING MEANS SUB-FREEZING TEMPERATURES ARE IMMINENT OR HIGHLY LIKELY. THESE CONDITIONS WILL KILL CROPS AND OTHER SENSITIVE VEGETATION.''',
        ]

    def ctaISW(self):
        return [
 '''AN ICE STORM WARNING MEANS SEVERE WINTER WEATHER CONDITIONS ARE EXPECTED OR OCCURRING. SIGNIFICANT AMOUNTS OF ICE ACCUMULATIONS WILL MAKE TRAVEL DANGEROUS OR IMPOSSIBLE. TRAVEL IS STRONGLY DISCOURAGED. COMMERCE WILL LIKELY BE SEVERELY IMPACTED. IF YOU MUST TRAVEL...KEEP AN EXTRA FLASHLIGHT...FOOD...AND WATER IN YOUR VEHICLE IN CASE OF AN EMERGENCY. ICE ACCUMULATIONS AND WINDS WILL LIKELY LEAD TO SNAPPED POWER LINES AND FALLING TREE BRANCHES THAT ADD TO THE DANGER.''',
        ]

    def ctaLEA(self):
         return [
 '''A LAKE EFFECT SNOW WATCH MEANS THERE IS A POTENTIAL FOR A LARGE AMOUNT OF SNOW IN ONLY A FEW HOURS. VISIBILITIES AND DEPTH OF SNOW CAN VARY GREATLY...IMPACTING TRAVEL SIGNIFICANTLY. CONTINUE TO MONITOR THE LATEST FORECASTS.''',
        ]

    def ctaLEW(self):
        return [
 '''A LAKE EFFECT SNOW WARNING MEANS SIGNIFICANT AMOUNTS OF LAKE-EFFECT SNOW ARE FORECAST THAT WILL MAKE TRAVEL VERY HAZARDOUS OR IMPOSSIBLE. LAKE-EFFECT SNOW SHOWERS TYPICALLY ALIGN THEMSELVES IN BANDS AND WILL LIKELY BE INTENSE ENOUGH TO DROP 1 TO SEVERAL INCHES OF SNOW PER HOUR FOR SEVERAL HOURS. VISIBILITIES VARY GREATLY AND CAN DROP TO ZERO WITHIN MINUTES.  TRAVEL IS STRONGLY DISCOURAGED. COMMERCE COULD BE SEVERELY IMPACTED. IF YOU MUST TRAVEL...KEEP AN EXTRA FLASHLIGHT...FOOD...AND WATER IN YOUR VEHICLE IN CASE OF AN EMERGENCY.''',
        ]

    def ctaLEY(self):
        return [
 '''A LAKE EFFECT SNOW ADVISORY MEANS LAKE-EFFECT SNOW IS FORECAST THAT WILL MAKE TRAVEL DIFFICULT IN SOME AREAS. LAKE-EFFECT SNOW SHOWERS TYPICALLY ALIGN THEMSELVES IN BANDS AND WILL LIKELY BE INTENSE ENOUGH TO DROP SEVERAL INCHES IN LOCALIZED AREAS. USE CAUTION WHEN TRAVELING.''',
        ]

    def ctaLOY(self):
        return [
 '''A LOW WATER ADVISORY MEANS WATER LEVELS ARE EXPECTED TO BE SIGNIFICANTLY BELOW AVERAGE. MARINERS SHOULD USE EXTREME CAUTION AND TRANSIT AT THE SLOWEST SAFE NAVIGABLE SPEED TO MINIMIZE IMPACT.''',
        ]

    def ctaLSA(self):
        return [
 '''A LAKESHORE FLOOD WATCH MEANS THAT CONDITIONS FAVORABLE FOR LAKESHORE FLOODING ARE EXPECTED TO DEVELOP. RESIDENTS ON OR NEAR THE SHORE SHOULD TAKE ACTION TO PROTECT PROPERTY...AND LISTEN FOR LATER STATEMENTS OR WARNINGS.''',
        ]

    def ctaLSW(self):
        return [
 '''A LAKESHORE FLOOD WARNING MEANS THAT FLOODING IS OCCURRING OR IMMINENT ALONG THE LAKE. RESIDENTS ON OR NEAR THE SHORE IN THE WARNED AREA SHOULD BE ALERT FOR RISING WATER...AND TAKE APPROPRIATE ACTION TO PROTECT LIFE AND PROPERTY.''',
        ]

    def ctaLSY(self):
        return [
 '''A LAKESHORE FLOOD ADVISORY INDICATES THAT ONSHORE WINDS WILL GENERATE FLOODING OF LOW AREAS ALONG THE LAKESHORE.''',
        ]

    def ctaLWY(self):
        return [
 '''A LAKE WIND ADVISORY INDICATES THAT WINDS WILL CAUSE ROUGH CHOP ON AREA LAKES. SMALL BOATS WILL BE ESPECIALLY PRONE TO CAPSIZING.''',
        ]

    def ctaMHW(self):
        return [
 '''AN ASHFALL WARNING MEANS THAT SIGNIFICANT ACCUMULATION OF ASHFALL IS EXPECTED ON VESSELS. IT IS RECOMMENDED THAT VESSELS BE PREPARED TO TAKE THE NECESSARY COUNTER MEASURES BEFORE PUTTING TO SEA OR ENTERING THE WARNING AREA.''',
        ]

    def ctaMFY(self):
        return [
'''A DENSE FOG ADVISORY MEANS VISIBILITIES WILL FREQUENTLY BE REDUCED TO LESS THAN ONE MILE. INEXPERIENCED MARINERS...ESPECIALLY THOSE OPERATING SMALLER VESSELS SHOULD AVOID NAVIGATING IN THESE CONDITIONS. ''',
        ]

    def ctaMHY(self):
        return [
'''AN ASHFALL ADVISORY MEANS THAT A LIGHT ACCUMULATION OF ASHFALL IS EXPECTED ON VESSELS. IT IS RECOMMENDED THAT VESSELS BE PREPARED TO TAKE APPROPRIATE COUNTER MEASURES BEFORE PUTTING TO SEA OR ENTERING THE ADVISORY AREA.''',
        ]

    def ctaMSY(self):
        return [
 '''A DENSE SMOKE ADVISORY MEANS WIDESPREAD FIRES WILL CREATE SMOKE...LIMITING VISIBILITIES. INEXPERIENCED MARINERS...ESPECIALLY THOSE OPERATING SMALLER VESSELS SHOULD AVOID NAVIGATING IN THESE CONDITIONS.''',
        ]

    def ctaRBY(self):
        return [
 '''A SMALL CRAFT ADVISORY FOR ROUGH BAR MEANS THAT WAVE CONDITIONS ARE EXPECTED TO BE HAZARDOUS TO SMALL CRAFT IN OR NEAR HARBOR ENTRANCES.''',
        ]

    def ctaSCY(self):
        return [
 '''A SMALL CRAFT ADVISORY MEANS THAT WIND SPEEDS OF 21 TO 33 KNOTS ARE EXPECTED TO PRODUCE HAZARDOUS WAVE CONDITIONS TO SMALL CRAFT. INEXPERIENCED MARINERS...ESPECIALLY THOSE OPERATING SMALLER VESSELS SHOULD AVOID NAVIGATING IN THESE CONDITIONS.''',
        ]

    def ctaSEA(self):
        return [
 '''A HAZARDOUS SEAS WATCH IS ISSUED WHEN THE RISK OF HAZARDOUS SEAS HAS SIGNIFICANTLY INCREASED...BUT THE SPECIFIC TIMING AND/OR LOCATION IS STILL UNCERTAIN.  IT IS INTENDED TO PROVIDE ADDITIONAL LEAD TIME FOR MARINERS WHO MAY WISH TO CONSIDER ALTERING THEIR PLANS.''',
        ]

    def ctaSEW(self):
        return [
 '''A HAZARDOUS SEAS WARNING MEANS HAZARDOUS SEA CONDITIONS ARE IMMINENT OR OCCURING. RECREATIONAL BOATERS SHOULD REMAIN IN PORT...OR TAKE SHELTER UNTIL WAVES SUBSIDE.  COMMERCIAL VESSELS SHOULD PREPARE FOR ROUGH SEAS<85>AND CONSIDER REMAINING IN PORT OR TAKING SHELTER IN PORT UNTIL HAZARDOUS SEAS SUBSIDE.''',
        ]

    def ctaSIY(self):
        return [
 '''A SMALL CRAFT ADVISORY FOR WIND MEANS THAT WIND SPEEDS OF 21 TO 33 KNOTS ARE EXPECTED. INEXPERIENCED MARINERS...ESPECIALLY THOSE OPERATING SMALLER VESSELS SHOULD AVOID NAVIGATING IN THESE CONDITIONS.''',
        ]

    def ctaSMY(self):
        return [
 '''A DENSE SMOKE ADVISORY MEANS WIDESPREAD FIRES WILL CREATE SMOKE...LIMITING VISIBILITIES. IF DRIVING...SLOW DOWN...USE YOUR HEADLIGHTS...AND LEAVE PLENTY OF DISTANCE AHEAD OF YOU IN CASE A SUDDEN STOP IS NEEDED.''',
        ]


    def ctaSRA(self):
        return [
 '''A STORM WATCH IS ISSUED WHEN THE RISK OF STORM FORCE WINDS OF 48 TO 63 KNOTS HAS SIGNIFICANTLY INCREASED...BUT THE SPECIFIC TIMING AND/OR LOCATION IS STILL UNCERTAIN.  IT IS INTENDED TO PROVIDE ADDITIONAL LEAD TIME FOR MARINERS WHO MAY WISH TO CONSIDER ALTERING THEIR PLANS.''',
        ]

    def ctaSRW(self):
        return [
 '''A STORM WARNING MEANS WINDS OF 48 TO 63 KNOTS ARE IMMINENT OR OCCURING. RECREATIONAL BOATERS SHOULD REMAIN IN PORT...OR TAKE SHELTER UNTIL WINDS AND WAVES SUBSIDE. COMMERCIAL VESSELS SHOULD PREPARE FOR VERY STRONG WINDS AND DANGEROUS SEA CONDITIONS...AND CONSIDER REMAINING IN PORT OR TAKING SHELTER IN PORT UNTIL WINDS AND WAVES SUBSIDE.''',
        ]

    def ctaSUW(self):
        return [
 '''A HIGH SURF WARNING INDICATES THAT DANGEROUS...BATTERING WAVE WILL POUND THE SHORELINE. THIS WILL RESULT IN VERY DANGEROUS SWIMMING CONDITIONS...AND DEADLY RIP CURRENTS.''',
        ]

    def ctaSUY(self):
        return [
 '''A HIGH SURF ADVISORY MEANS THAT HIGH SURF WILL AFFECT BEACHES IN THE ADVISORY AREA...PRODUCING RIP CURRENTS AND LOCALIZED BEACH EROSION.''',
        ]

    def ctaSWY(self):
        return [
 '''A SMALL CRAFT ADVISORY FOR HAZARDOUS SEAS MEANS THAT WAVES ARE EXPECTED TO BE HAZARDOUS TO SMALL CRAFT. MARINERS SHOULD AVOID SHOALING AREAS. LONG PERIOD SWELL CAN SHARPEN INTO LARGE BREAKING WAVES IN SHOALING AREAS. IT IS NOT UNUSUAL FOR WAVES TO BREAK MUCH FARTHER FROM SHOALING AREAS THAN IS NORMALLY EXPERIENCED. REMEMBER...BREAKING WAVES CAN EASILY CAPSIZE EVEN LARGER VESSELS.''',
        ]

    def ctaTRA(self):
        return [
 '''A TROPICAL STORM WATCH MEANS SUSTAINED WINDS OF |* 34 TO 63 KT OR 39 TO 73 MPH OR 63 TO 118 KM PER HR *| ARE POSSIBLE DUE TO A TROPICAL STORM WITHIN 48 HOURS.''',
        ] 

    def ctaTRW(self):
        return [
 '''A TROPICAL STORM WARNING MEANS SUSTAINED WINDS OF |* 34 TO 63 KT OR 39 TO 73 MPH OR 63 TO 118 KM PER HR *| ARE EXPECTED DUE TO A TROPICAL STORM WITHIN 36 HOURS.''',
        ] 

    def ctaUPA(self):
        return [
 '''A HEAVY FREEZING SPRAY WATCH IS ISSUED WHEN THE RISK OF HEAVY FREEZING SPRAY HAS SIGNIFICANTLY INCREASED...BUT THE SPECIFIC TIMING AND/OR LOCATION IS STILL UNCERTAIN.  IT IS INTENDED TO PROVIDE ADDITIONAL LEAD TIME FOR MARINERS WHO MAY WISH TO CONSIDER ALTERING THEIR PLANS.''',
        ]

    def ctaUPW(self):
        return [
 '''A HEAVY FREEZING SPRAY WARNING MEANS HEAVY FREEZING SPRAY IS EXPECTED TO RAPIDLY ACCUMULATE ON VESSELS. THESE CONDITIONS CAN BE EXTREMELY HAZARDOUS TO NAVIGATION. IT IS RECOMMENDED THAT MARINERS NOT TRAINED TO OPERATE IN THESE CONDITIONS OR VESSELS NOT PROPERLY EQUIPED TO DO SO...REMAIN IN PORT OR AVOID THE WARING AREA.''',
        ]

    def ctaUPY(self):
        return [
 '''A FREEZING SPRAY ADVISORY MEANS THAT LIGHT TO MODERATE ACCUMULATION OF ICE IS EXPECTED ON VESSELS. OPERATING A VESSEL IN FREEZING SPRAY CAN BE HAZARDOUS. IT IS RECOMMENDED THAT VESSELS BE PREPARED TO TAKE APPROPRIATE COUNTER MEASURES BEFORE PUTTING TO SEA OR ENTER THE ADVISORY AREA.''',
        ]

    def ctaWCA(self):
        return [
 '''A WIND CHILL WATCH MEANS THE THERE IS THE POTENTIAL FOR A COMBINATION OF VERY COLD AIR AND STRONG WINDS TO CREATE DANGEROUSLY LOW WIND CHILL VALUES. MONITOR THE LATEST FORECASTS AND WARNINGS FOR UPDATES ON THIS SITUATION.''',
        ]

    def ctaWCW(self):
        return [
 '''A WIND CHILL WARNING MEANS THE COMBINATION OF VERY COLD AIR AND STRONG WINDS WILL CREATE DANGEROUSLY LOW WIND CHILL VALUES. THIS WILL RESULT IN FROST BITE AND LEAD TO HYPOTHERMIA OR DEATH IF PRECAUTIONS ARE NOT TAKEN.''',
        ]

    def ctaWCY(self):
        return [
 '''A WIND CHILL ADVISORY MEANS THAT VERY COLD AIR AND STRONG WINDS WILL COMBINE TO GENERATE LOW WIND CHILLS. THIS WILL RESULT IN FROST BITE AND LEAD TO HYPOTHERMIA IF PRECAUTIONS ARE NOT TAKEN.  IF YOU MUST VENTURE OUTDOORS...MAKE SURE YOU WEAR A HAT AND GLOVES.''',
        ]

    def ctaWIY(self):
        return [
 '''A WIND ADVISORY MEANS THAT WINDS OF 35 MPH ARE EXPECTED. WINDS THIS STRONG CAN MAKE DRIVING DIFFICULT...ESPECIALLY FOR HIGH PROFILE VEHICLES. USE EXTRA CAUTION.''',
        ]

    def ctaWSA(self):
        return [
 '''A WINTER STORM WATCH MEANS THERE IS A POTENTIAL FOR SIGNIFICANT SNOW...SLEET...OR ICE ACCUMULATIONS THAT MAY IMPACT TRAVEL. CONTINUE TO MONITOR THE LATEST FORECASTS.''',
        ]

    def ctaWSW(self):
        return [
 '''|*Choose the appropriate CTA below and delete the rest*|

A WINTER STORM WARNING FOR HEAVY SNOW MEANS SEVERE WINTER WEATHER CONDITIONS ARE EXPECTED OR OCCURRING.  SIGNIFICANT AMOUNTS OF SNOW ARE FORECAST THAT WILL MAKE TRAVEL DANGEROUS. ONLY TRAVEL IN AN EMERGENCY. IF YOU MUST TRAVEL...KEEP AN EXTRA FLASHLIGHT...FOOD...AND WATER IN YOUR VEHICLE IN CASE OF AN EMERGENCY.

A WINTER STORM WARNING MEANS SIGNIFICANT AMOUNTS OF SNOW...SLEET...AND ICE ARE EXPECTED OR OCCURRING. STRONG WINDS ARE ALSO POSSIBLE.  THIS WILL MAKE TRAVEL VERY HAZARDOUS OR IMPOSSIBLE.

A WINTER STORM WARNING FOR SLEET MEANS THAT A WINTER STORM SYSTEM IS IMPACTING THE AREA WITH SIGNIFICANT AMOUNTS OF SLEET. TRAVEL IS LIKELY TO BE SEVERELY IMPACTED.''',
        ] 

    def ctaWWY(self):
        return [
 '''|*Choose the appropriate CTA below and delete the rest*|

A WINTER WEATHER ADVISORY MEANS THAT PERIODS OF SNOW...SLEET...OR FREEZING RAIN WILL CAUSE TRAVEL DIFFICULTIES. BE PREPARED FOR SLIPPERY ROADS AND LIMITED VISIBILITIES...AND USE CAUTION WHILE DRIVING.

A WINTER WEATHER ADVISORY FOR BLOWING SNOW MEANS THAT VISIBILITIES WILL BE LIMITED DUE TO STRONG WINDS BLOWING SNOW AROUND. USE CAUTION WHEN TRAVELING...ESPECIALLY IN OPEN AREAS.
 
A WINTER WEATHER ADVISORY FOR SLEET MEANS PERIODS OF SLEET ARE IMMINENT OR OCCURRING. SLEET MAY CAUSE DRIVING TO BECOME EXTREMELY DANGEROUS...SO BE PREPARED TO USE CAUTION WHEN TRAVELING.

A WINTER WEATHER ADVISORY FOR |*LAKE EFFECT*| SNOW AND BLOWING SNOW MEANS THAT VISIBILITIES WILL BE LIMITED DUE TO A COMBINATION OF FALLING AND BLOWING SNOW. USE CAUTION WHEN TRAVELING...ESPECIALLY IN OPEN AREAS.

A WINTER WEATHER ADVISORY FOR SNOW MEANS THAT PERIODS OF SNOW WILL CAUSE PRIMARILY TRAVEL DIFFICULTIES. BE PREPARED FOR SNOW COVERED ROADS AND LIMITED VISIBILITIES...AND USE CAUTION WHILE DRIVING.''',

        ] 
        
    def ctaZFY(self):
        return [
 '''A FREEZING FOG ADVISORY MEANS VISIBILITIES WILL FREQUENTLY BE REDUCED TO LESS THAN ONE QUARTER MILE. IF DRIVING...SLOW DOWN...USE YOUR HEADLIGHTS...AND LEAVE PLENTY OF DISTANCE AHEAD OF YOU. ALSO...BE ALERT FOR FROST ON BRIDGE DECKS CAUSING SLIPPERY ROADS.''',
        ]

    def ctaZRY(self):
        return [
 '''A FREEZING RAIN ADVISORY MEANS THAT PERIODS OF FREEZING RAIN OR FREEZING DRIZZLE WILL CAUSE TRAVEL DIFFICULTIES. BE PREPARED FOR SLIPPERY ROADS. SLOW DOWN AND USE CAUTION WHILE DRIVING.''',
        ]

#------------------------------------------------------------------------
# CALL TO ACTIONS - individual functions for each product pil
#------------------------------------------------------------------------
# These are lists of strings.  These are available through the call to 
# actions menu.

    def ctaPilADR(self):
        return [
        ]

    def ctaPilAFD(self):
        return [
        ]

    def ctaPilAFM(self):
        return [
        ]

    def ctaPilAVA(self):
        return [
        ]

    def ctaPilAVW(self):
        return [
        ]

    def ctaPilCAE(self):
        return [
        ]

    def ctaPilCCF(self):
        return [
        ]

    def ctaPilCDW(self):
        return [
        ]

    def ctaPilCEM(self):
        return [
        ]

    def ctaPilCFW(self):
        return [
        ]

    def ctaPilCWF(self):
        return [
        ]

    def ctaPilEQR(self):
        return [
        ]

    def ctaPilESF(self):
        return [
        ]

    def ctaPilEQW(self):
        return [
        ]

    def ctaPilEVI(self):
        return [
        ]

    def ctaPilFFA(self):
        return [
        ]

    def ctaPilFRW(self):
        return [
        ]

    def ctaPilFWF(self):
        return [
        ]

    def ctaPilFWM(self):
        return [
        ]

    def ctaPilFWS(self):
        return [
        ]

    def ctaPilGLF(self):
        return [
        ]

    def ctaPilHLS(self):
        return [('***MINOR FLOODING', '''RESIDENTS CAN EXPECT MINOR FLOODING OF ROADS...ESPECIALLY THOSE WITH POOR DRAINAGE. KNOWN INTERSECTIONS WITH VERY POOR DRAINAGE MAY HAVE WATER LEVELS UP TO 3 FEET. OTHER POOR DRAINAGE AREAS WILL HAVE WATER RISES OF 1 FOOT.'''),
 ('***WIDESPREAD FLOODING', '''RESIDENTS CAN EXPECT WIDESPREAD FLOODING.  IN POOR DRAINAGE AREAS...MINOR TO MODERATE PROPERTY DAMAGE IS EXPECTED...AND SEVERAL MAIN THOROUGHFARES MAY BE CLOSED.  KNOWN INTERSECTIONS WITH VERY POOR DRAINAGE MAY HAVE WATER LEVELS UP TO 5 FEET.  OTHER POOR DRAINAGE AREAS WILL HAVE WATER RISES UP TO 3 FEET.  LEVELS WILL RISE 1 FOOT ELSEWHERE.'''),
 '''SMALL STREAMS WILL SURPASS BANK FULL...BUT ONLY FOR ONE HOUR OR LESS.''',
 ('***WIDESPREAD STREAM FLOODING', '''MOST SMALL STREAMS AND CREEKS WILL SURPASS BANK FULL...FOR UP TO 3 HOURS.  LARGER RIVERS WILL RISE...AND THOSE WHICH RESPOND QUICKLY TO VERY HEAVY RAIN MAY BRIEFLY EXCEED FLOOD STAGE.'''),
 ('***PRIOR NOTICE OF EXTENSIVE AREAL FLOODING', '''EXTENSIVE FLOODING IS EXPECTED |**TODAY OR TONIGHT OR NEXT DAY**| \n\n PERSONS LIVING NEAR OR IN POOR DRAINAGE LOCATIONS SHOULD PREPARE FOR POSSIBLE EVACUATION LATER |**TODAY OR TONIGHT OR NEXT DAY**|. IN THESE AREAS...SIGNIFICANT PROPERTY DAMAGE WILL OCCUR...AND SOME POWER OUTAGES ARE LIKELY.  MINOR PROPERTY DAMAGE IS POSSIBLE ELSEWHERE. \n\nWATER LEVELS IN VERY POOR DRAINAGE AREAS WILL APPROACH 7 FEET.  OTHER POOR DRAINAGE LOCATIONS WILL HAVE RISES BETWEEN 3 AND 5 FEET.  ELSEWHERE...EXPECT WATER RISES TO NEAR 2 FEET.  NUMEROUS MAIN ROADS WILL BE CLOSED.  DRIVING IS HIGHLY DISCOURAGED EXCEPT FOR EMERGENCIES.'''),
 ('***DANGEROUS FLOODING', '''THIS IS A DANGEROUS FLOOD SITUATION!  \n\nPERSONS LIVING IN OR NEAR POOR DRAINAGE AREAS SHOULD EVACUATE IMMEDIATELY.  SIGNIFICANT PROPERTY DAMAGE WILL OCCUR IN THESE LOCATIONS.  MINOR PROPERTY DAMAGE IS POSSIBLE IN OTHER AREAS.  SOME POWER OUTAGES ARE EXPECTED. \n\n WATER LEVELS IN VERY POOR DRAINAGE AREAS WILL APPROACH 7 FEET.  OTHER POOR DRAINAGE LOCATIONS WILL HAVE RISES BETWEEN 3 AND 5 FEET.  ELSEWHERE...EXPECT WATER RISES TO NEAR 2 FEET.  NUMEROUS MAIN ROADS WILL BE CLOSED.  DRIVING IS HIGHLY DISCOURAGED UNTIL WELL AFTER FLOOD WATERS RECEDE. \n\n MOVE TO SAFETY IMMEDIATELY.'''),
 ('***PRIOR NOTICE OF EXTENSIVE RIVER FLOODING', '''EXTENSIVE FLOODING IS EXPECTED |**TODAY OR TONIGHT OR NEXT DAY**|. \n\n BY |**TIME**|...ALL SMALL STREAMS AND CREEKS WILL HAVE SURPASSED BANK FULL.  THESE CONDITIONS WILL LAST BETWEEN 3 AND 6 HOURS.  SOME STREAMS WILL EXCEED THEIR BANKS BY SEVERAL FEET AND MAY FLOOD NEARBY HOMES.  EVACUATIONS ARE POSSIBLE.\n\n RIVERS IN AFFECTED AREAS WILL RISE...WITH SOME REACHING OR EXCEEDING FLOOD STAGE.  NORMALLY QUICK-RISING RIVERS WILL EXCEED FLOOD STAGE BY SEVERAL FEET...FLOODING HOMES ALONG THE RIVERSIDE.  PASTURES WILL ALSO FLOOD...BUT LIVESTOCK LOSSES SHOULD BE MINIMAL.  SEVERAL SECONDARY ROADS AND BRIDGES WILL BE WASHED OUT.  DRIVING IS HIGHLY DISCOURAGED.'''),
 ('***DANGEROUS RIVER FLOODING', '''THIS IS A DANGEROUS SITUATION!  \n\nALL STREAMS...CREEKS..AND SOME RIVERS WILL SURPASS BANKFULL...FOR BETWEEN 3 AND 6 HOURS.  SOME STREAMS WILL EXCEED THEIR BANKS BY SEVERAL FEET...FLOODING NEARBY HOMES.  EVACUATIONS ARE POSSIBLE. \n\n RIVERS IN AFFECTED AREAS WILL RISE...WITH SOME REACHING OR EXCEEDING FLOOD STAGE.  NORMALLY QUICK RISING RIVERS WILL EXCEED FLOOD STAGE BY SEVERAL FEET...FLOODING HOMES ALONG THE RIVERSIDE.  PASTURES WILL ALSO FLOOD...BUT LIVESTOCK LOSSES SHOULD BE MINIMAL.'''),
 ('***CATASTROPHIC FLOODING EXPECTED', '''CATASTROPHIC FLOODING IS EXPECTED LATER |**EDIT DAY OR NIGHT PERIODS**|. \n\n A STATE OF EMERGENCY HAS BEEN ISSUED |**BY AGENCY**| FOR |**EDIT AREA HERE**|. \n\n RESIDENTS IN FLOOD PRONE AREAS SHOULD RUSH TO COMPLETION PREPARATIONS TO PROTECT THEIR PROPERTY...THEN MOVE TO A PLACE OF SAFETY...THIS |**EDIT TIME PERIOD**|. MANDATORY EVACUATIONS ARE UNDERWAY. \n\n |** OPENING PARAGRAPH DESCRIBING ANTECEDENT RAINFALL AND EXPECTED HEAVIER RAINFALL **| \n\n LIFE THREATENING FLOODING IS LIKELY!  IN URBAN AREAS...EXTENSIVE PROPERTY DAMAGE WILL OCCUR IN ALL POOR DRAINAGE AREAS...WITH MODERATE TO MAJOR PROPERTY DAMAGE ELSEWHERE.  WIDESPREAD POWER OUTAGES ARE LIKELY. \n\n IN RURAL LOCATIONS...ALL STREAMS...CREEKS...AND ARROYOS WILL SURPASS BANK FULL FOR MORE THAN 6 HOURS.  EACH WILL EXCEED THEIR BANKS BY SEVERAL FEET...FLOODING HOMES...EVEN THOSE UP TO ONE HALF MILE AWAY FROM THE BANKS. \n\n IN ALL AREAS...HUNDREDS OF ROADS WILL FLOOD.  DOZENS OF SECONDARY ROADS MAY BECOME WASHED OUT IN RURAL AREAS.  NUMEROUS LOW WATER BRIDGES WILL LIKELY WASH OUT AS WELL. \n\n WATER LEVELS WILL EXCEED 5 FEET IN ALL POOR DRAINAGE URBAN AREAS...AND AVERAGE AT LEAST 2 FEET ELSEWHERE.  ALL RIVERS IN AFFECTED AREAS WILL RISE...AND MOST WILL EXCEED FLOOD STAGE.  QUICK RISING RIVERS WILL EXCEED FLOOD STAGE...AND REACH NEAR RECORD CRESTS...CAUSING INUNDATION OF NEARBY HOMES.  IN RURAL LOCATIONS...EXTENSIVE PASTURELAND FLOODING WILL OCCUR AS WATER LEVELS RISE TO 2 FEET OR MORE.  WIDESPREAD LIVESTOCK LOSSES ARE LIKELY.'''),
 ('***CATASTROPHIC FLOODING OCCURRING', '''CATASTROPHIC FLOODING IS OCCURRING IN |**EDIT AREA**|. \n\n STATES OF EMERGENCY REMAIN IN EFFECT FOR THE FOLLOWING LOCATIONS: \n\n |**EDIT COUNTIES AND CITIES HERE**| \n\n RESIDENTS REMAIN PROHIBITED FROM VENTURING OUT.  LAW ENFORCEMENT AND |**MILITARY SUPPORT GROUP EDIT HERE**| EVACUATIONS ARE NOW UNDERWAY. \n\n THIS REMAINS A LIFE THREATENING SITUATION!  EXTENSIVE PROPERTY DAMAGE IS OCCURRING IN ALL POOR DRAINAGE AREAS.  ELSEWHERE...MODERATE TO MAJOR PROPERTY DAMAGE IS OCCURRING.  HUNDREDS OF ROADS ARE CLOSED...AND SOME ARE LIKELY DAMAGED.  SEVERAL AREA BRIDGES ARE WASHED OUT.  STREAMS...CREEKS...AND ARROYOS ARE SEVERAL FEET ABOVE BANK FULL...AND WILL REMAIN SO FOR HOURS.  MANY RIVERS ARE NEARING FLOOD STAGE...AND SOME HAVE ALREADY SURPASSED IT.  HOMES NEAR THESE RIVERS ARE LIKELY FLOODED. FLOOD WATERS WILL CONTINUE FOR SEVERAL MORE HOURS. \n\n WATER LEVELS ARE IN EXCESS OF 5 FEET IN ALL POOR DRAINAGE AREAS.  ELSEWHERE...AVERAGE WATER LEVELS ARE AT LEAST 2 FEET. POWER OUTAGES ARE WIDESPREAD. \n\n STAY TUNED TO NOAA WEATHER RADIO FOR FURTHER INFORMATION ON THIS DANGEROUS FLOOD.  HEED ALL EVACUATION ORDERS FROM LAW ENFORCEMENT OR MILITARY PERSONNEL.'''),
 ('***GENERATOR PRECAUTIONS', '''IF YOU PLAN ON USING A PORTABLE GENERATOR...BE SURE TO OBSERVE ALL SAFETY PRECAUTIONS TO AVOID CARBON MONOXIDE POISONING...ELECTROCUTION...OR FIRE.  BE SURE TO OPERATE YOUR GENERATOR IN A DRY OUTDOOR AREA AWAY FROM WINDOWS...DOORS AND VENTS. CARBON MONOXIDE POISONING DEATHS CAN OCCUR DUE TO IMPROPERLY LOCATED PORTABLE GENERATORS!'''),
 ('***FLAMMABLES PRECAUTION', '''FLAMMABLE LIQUIDS SUCH AS GASOLINE OR KEROSENE SHOULD ONLY BE STORED OUTSIDE OF THE LIVING AREAS IN PROPERLY LABELED...NON GLASS SAFETY CONTAINERS.  DO NOT STORE IN AN ATTACHED GARAGE AS GAS FUMES CAN TRAVEL INTO THE HOME AND POTENTIALLY IGNITE...ESPECIALLY IF THE HOME HAS NATURAL OR PROPANE GAS LINES THAT COULD BECOME DAMAGED DURING THE HURRICANE.'''),
 ('***HURRICANE WARNING DEFINITION', '''A HURRICANE WARNING MEANS SUSTAINED WINDS OF |* 64 KTS OR 74 MPH *| OR HIGHER ASSOCIATED WITH A HURRICANE ARE EXPECTED WITHIN 36 HOURS. A HURRICANE WARNING CAN REMAIN IN EFFECT WHEN DANGEROUSLY HIGH WATER OR A COMBINATION OF DANGEROUSLY HIGH WATER AND EXCEPTIONALLY HIGH WAVES CONTINUE...EVEN THOUGH WINDS MAY BE LESS THAN HURRICANE FORCE.'''),
 ('***HURRICANE WATCH DEFINITION', '''A HURRICANE WATCH IS ISSUED WHEN SUSTAINED WINDS OF |* 64 KTS OR 74 MPH *| OR HIGHER ASSOCIATED WITH A HURRICANE ARE POSSIBLE WITHIN 48 HOURS.'''),
 ('***HURRICANE WIND WARNING DEFINITION', '''A HURRICANE WIND WARNING IS ISSUED WHEN A LANDFALLING HURRICANE IS EXPECTED TO SPREAD HURRICANE FORCE WINDS WELL INLAND. SERIOUS PROPERTY DAMAGE...POWER OUTAGES...BLOWING DEBRIS...AND FALLEN TREES CAN BE EXPECTED AS WINDS REACH OR EXCEED 74 MPH.'''),
 ('***HURRICANE WIND WATCH DEFINITION', '''A HURRICANE WIND WATCH IS ISSUED WHEN A LANDFALLING HURRICANE IS EXPECTED TO SPREAD HURRICANE FORCE WINDS WELL INLAND WITHIN THE NEXT 48 HOURS. PREPARE FOR WINDS IN EXCESS OF 74 MPH.'''),
 ('***TROPICAL STORM WARNING DEFINITION', '''A TROPICAL STORM WARNING MEANS SUSTAINED WINDS OF |* 34 TO 63 KT OR 39 TO 73 MPH OR 63 TO 118 KM PER HR *| ARE EXPECTED DUE TO A TROPICAL CYCLONE WITHIN 36 HOURS.'''),
 ('***TROPICAL STORM WIND WARNING DEFINITION', '''A TROPICAL STORM WIND WARNING MEANS WINDS OF 39 TO 73 MPH ARE EXPECTED DUE TO A LANDFALLING HURRICANE OR TROPICAL STORM. WINDS OF THIS MAGNITUDE ARE LIKELY TO CAUSE SPORADIC POWER OUTAGES...FALLEN TREES...MINOR PROPERTY DAMAGE...AND DANGEROUS DRIVING CONDITIONS FOR HIGH PROFILE VEHICLES.'''),
 ('***TROPICAL STORM WATCH DEFINITION', '''A TROPICAL STORM WATCH MEANS SUSTAINED WINDS OF |* 34 TO 63 KT OR 39 TO 73 MPH OR 63 TO 118 KM PER HR *| ARE POSSIBLE DUE TO A TROPICAL CYCLONE WITHIN 48 HOURS.'''),
 ('***TROPICAL STORM WIND WATCH DEFINITION', '''A TROPICAL STORM WIND WATCH MEANS WINDS OF 39 TO 73 MPH ARE EXPECTED DUE TO A LANDFALLING HURRICANE OR TROPICAL STORM WITHIN 48 HOURS.'''),
        ]

    def ctaPilHMW(self):
        return [
        ]

    def ctaPilHWO(self):
        return [
        ]

    def ctaPilLAE(self):
        return [
        ]

    def ctaPilLEW(self):
        return [
        ]

    def ctaPilMWS(self):
        return [
        ]

    def ctaPilMWW(self):
        return [
 '''MARINERS SHOULD PAY CLOSE ATTENTION TO THE MARINE FORECAST...AND CONSIDER WIND AND SEA CONDITIONS IN PLANNING.''',
        ]

    def ctaPilMVF(self):
        return [
        ]

    def ctaPilNOW(self):
        return [
        ]

    def ctaPilNPW(self):
        return [
        ]

    def ctaPilNSH(self):
        return [
        ]

    def ctaPilNUW(self):
        return [
        ]

    def ctaPilOFF(self):
        return [
        ]

    def ctaPilPFM(self):
        return [
        ]

    def ctaPilPNS(self):
        return [
        ]

    def ctaPilRFD(self):
        return [
        ]

    def ctaPilRFW(self):
        return [
        ]

    def ctaPilRHW(self):
        return [
        ]

    def ctaPilSAF(self):
        return [
        ]

    def ctaPilSRF(self):
        return [
        ]

    def ctaPilSFT(self):
        return [
        ]

    def ctaPilSPS(self):
        return [
        ]

    def ctaPilSPW(self):
        return [
        ]

    def ctaPilTOE(self):
        return [
        ]

    def ctaPilVOW(self):
        return [
        ]

    def ctaPilWCN(self):
        return [
        ]

    def ctaPilWSW(self):
        return [
        ]

    def ctaPilZFP(self):
        return [
        ]

    def getVTECMode(self, runMode, vtecMode):
        returnMode = "T"
        if runMode == "OPERATIONAL" :
            returnMode = "O"
        elif runMode == "TEST" :
            returnMode = "T"
        elif runMode == "PRACTICE" :
            returnMode = vtecMode
        return returnMode
