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
# 
#
import FormatTemplate
from time import gmtime, strftime
import collections
import types
from TextProductCommon import TextProductCommon

class Format(FormatTemplate.Formatter):
    
    def execute(self, data):
        '''
        Main method of execution to generate Legacy text
        @param data: dictionary values provided by the product generator
        @return: Returns the dictionary in Legacy format.
        '''
        self._tpc = TextProductCommon()        
        data = self.cleanDictKeys(data)
        self.data = data 
        text = self._processProductParts(data, self._tpc.getVal(data, 'productParts', []))
        return str(text.upper())
    
    def _processProductParts(self, dataDict, productParts, skipParts=[]):
        '''
        Adds the product parts to the product
        @param dataDict -- dictionary of information -- could be the product dictionary or a sub-part such as a segment
        @param skipParts -- necessary to avoid repetition when calling this method recursively
        @param productParts -- list of instances of the ProductPart class with information about how to format each product part
        @return text -- product string
        '''
        text = ''
        for productPart in productParts: 
            name = productPart.getName()
            if name == 'wmoHeader': text += self.processWmoHeader(dataDict['wmoHeader']) + '\n'
            elif name == 'wmoHeader_noCR': text += self.processWmoHeader(dataDict['wmoHeader'])
            elif name == 'easMessage':
                easMessage = self.processEAS(dataDict)
                if easMessage is not None:
                    text += easMessage + '\n'
            elif name == 'productHeader':
                text += dataDict['productName'] + '\n'
                text += dataDict['senderName'] + '\n'
                text += self.formatIssueTime()
            elif name == 'overview':
                text += '|* DEFAULT OVERVIEW SECTION *|\n\n'
            elif name == 'segments':
                text += self.processSegments(dataDict['segments'], productPart.getProductParts()) 
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
                    text += 'PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n'
                    for cta in callsToAction['callToAction']:
                        text += cta + '\n\n'
                    text += '&&\n\n'
            elif name == 'polygonText':
                if 'polygonText' in dataDict and dataDict['polygonText']:
                    text += dataDict['polygonText'] + '\n\n'
            elif name == 'end':
                text += '$$' 
            elif name == 'CR':
                text += '\n'
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
        request = data['easActivationRequested']
        #TODO Check using an ignoreCase
        if request == 'true':
            segments = data['segments']
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
            text += self._tpc.formatDatetime(sentTimeZ, '%I%M %p %Z %a %b %Y', timeZone) + '\n'
        return text + '\n'
        
    def processSegments(self, segments, segmentParts):
        """
        Generates Legacy text from a list of segments
        @param data: a list of dictionaries 
        @return: Returns the legacy text of the segments.
        """
        text = ''  
        for segment in segments['segment']:
            text += self._processProductParts(segment, segmentParts)
        return text
        
    def cleanDictKeys(self, data):
        '''
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
    
    
