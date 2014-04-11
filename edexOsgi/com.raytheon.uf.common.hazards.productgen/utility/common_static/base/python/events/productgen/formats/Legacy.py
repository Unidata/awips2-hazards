# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
#
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
#
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
#
# See the AWIPS II Master Rights File ('Master Rights File.pdf') for
# further licensing information.   
# #

#    Formats a dictionary 'data' and generates watch, warning, advidsory legacy text. The dictionary values
#    will be extracted and managed appropriately based on their keys. 
#
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    03/14/13                      jsanchez       Initial Creation.
#    07/08/13        784,1290      Tracy.L.Hansen Added ProductParts and changes for ESF product generator
#    12/11/13        2266          jsanchez       Used ProductUtil to format text. Added Editable to track editable entries.
#
import FormatTemplate
from time import gmtime, strftime
import collections
import types
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from KeyInfo import KeyInfo

class Format(FormatTemplate.Formatter):
    
    def execute(self, data):
        '''
        Main method of execution to generate Legacy text
        @param data: dictionary values provided by the product generator
        @return: Returns the dictionary in Legacy format.
        '''     
        from TextProductCommon import TextProductCommon
        self._tpc = TextProductCommon() 
        self.data = data
        text = self._processProductParts(data, self._tpc.getVal(data, 'productParts', []))
        return ProductUtils.wrapLegacy(str(text))
    
    def _processProductParts(self, dataDict, productParts, skipParts=[]):
        '''
        Adds the product parts to the product
        @param dataDict -- dictionary of information -- could be the product dictionary or a sub-part such as a segment
        @param skipParts -- necessary to avoid repetition when calling this method recursively
        @param productParts -- list of instances of the ProductPart class with information about how to format each product part
        @return text -- product string
        '''
        text = ''
        for part in productParts: 
            valtype = type(part)
            if valtype is str:
                name = part
            elif valtype is tuple:
                name = part[0]
            elif valtype is list:
                # TODO THIS SHOULD BE REMOVED AFTER THE REFACTOR OF HazardServicesProductGenerationHandler.JAVA
                tup = (part[0], part[1])
                part = tup
                name = part[0]
            if name == 'wmoHeader': text += self.processWmoHeader(dataDict['wmoHeader']) + '\n'
            elif name == 'wmoHeader_noCR': text += self.processWmoHeader(dataDict['wmoHeader'])
            elif name == 'easMessage':
                easMessage = self.processEAS(dataDict)
                if easMessage is not None:
                    text += easMessage + '\n'
            elif name == 'productHeader':
                text += self._tpc.getVal(dataDict, 'productName', altDict=self.data) + '\n'
                text += self._tpc.getVal(dataDict, 'senderName', altDict=self.data) + '\n'
                text += self._tpc.getVal(dataDict, 'issuedByString', default='', altDict=self.data)
                text += self.formatIssueTime()
            elif name == 'overview':
                text += '|* DEFAULT OVERVIEW SECTION *|\n\n'
            elif name == 'segments':
                text += self.processSegments(dataDict['segments'], part[1]) 
            elif name == 'sections':
                text += self.processSections(dataDict['sections'], part[1])
            elif name == 'vtecRecords':
                if 'vtecRecords' in dataDict:
                    vtecRecords = dataDict['vtecRecords']
                    for vtecRecord in vtecRecords:
                        text += vtecRecord['vtecString'] + '\n'
            elif name == 'issuanceTimeDate':
                text += self.formatIssueTime()
            elif name == 'callsToAction':
                if 'callsToAction' in dataDict and dataDict['callsToAction']:
                    callsToAction = dataDict['callsToAction']
                    if callsToAction['callToAction']:
                        text += 'PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n'
                        for cta in callsToAction['callToAction']:
                            text += cta + '\n\n'
            elif name == 'polygonText':
                if 'polygonText' in dataDict and dataDict['polygonText']:
                    text += dataDict['polygonText'] + '\n\n'
            elif name == 'endSegment':
                text += '&&\n\n' 
            elif name == 'CR':
                text += '\n'
            elif name == 'cities':
                #self._cityString = 'INCLUDING THE CITIES OF ' + self._tpc.formatUGC_cities(self._ugcs)
                cities = ''
                elements = KeyInfo.getElements('cities', dataDict)
                cityList = dataDict[elements[0]]
                for city in cityList:
                    cities += city + '...'
                text += cities + '\n'
            else:               
                textStr = self._tpc.getVal(dataDict, name)
                if textStr:
                    text += textStr + '\n'
                    
        return text
        
    def processWmoHeader(self, wmoHeader):
        text = wmoHeader['wmoHeaderLine'] + '\n'
        text += wmoHeader['awipsIdentifierLine'] + '\n'
        return text
    
    def processEAS(self, data):
        request = self._tpc.getVal(data, 'easActivationRequested', altDict=self.data)
        if request == 'true':
            segments = self._tpc.getVal(data, 'segments', altDict=self.data)
            segmentList = segments['segment']
            for segment in segmentList:
                vtecRecords = segment['vtecRecords']
                for vtecRecord in vtecRecords:
                    if vtecRecord.get('pvtecRecordType') == 'pvtecRecord' and vtecRecord.get('significance') is 'A':
                        return 'URGENT - IMMEDIATE BROADCAST REQUESTED'
                return 'BULLETIN - EAS ACTIVATION REQUESTED'       
        return None
    
    def formatIssueTime(self):  
        text = ''  
        sentTimeZ = self._tpc.getVal(self.data, 'sentTimeZ_datetime')
        timeZones = self._tpc.getVal(self.data, 'timeZones')
        for timeZone in timeZones:
            text += self._tpc.formatDatetime(sentTimeZ, '%I%M %p %Z %a %e %b %Y', timeZone) + '\n'
        return text + '\n'
        
    def processSegments(self, segments, segmentParts):
        """
        Generates Legacy text from a list of segments
        @param segments: a list of dictionaries 
        @param segmentParts: a list of Product Parts for each segment
        @return: Returns the legacy text of the segments.
        """
        text = ''  
        for segment in segments['segment']:
            text += self._processProductParts(segment, segmentParts)
            text += '$$\n\n'
        return text

    def processSections(self, sections, sectionParts):
        """
        Generates Legacy text from a list of sections
        @param sections: a list of dictionaries 
        @param sectionParts: a list of Product Parts for each section
        @return: Returns the legacy text of the segments.
        """
        text = ''  
        for section in sections['section']:
            text += self._processProductParts(section, sectionParts)
        return text
      
    def cleanDictKeys(self, data):
        '''
        Remove annotations (e.g. :editable) from the dictionary keys
        @param data -- dictionary
        @return -- cleaned dictionary
        '''
        temp = collections.OrderedDict()
        for key in data:
            d = data[key]
            if isinstance(d, dict):
                d = self.cleanDictKeys(d)
            elif isinstance(d, list):
                d = self.cleanList(d)
            if ':' in key:
                idx = key.index(':')
                key = key[:idx]
            temp[key] = d

        return temp
    
    def cleanList(self, list):
        temp = []
        for item in list:
            # TODO Remove dependency to check on str
            valType = type(item)
            if valType is not str:
                if valType is types.ListType:
                    item = self.cleanList(item)
                elif isinstance(item, dict):
                    item = self.cleanDictKeys(item)
            temp.append(item)
        return temp
