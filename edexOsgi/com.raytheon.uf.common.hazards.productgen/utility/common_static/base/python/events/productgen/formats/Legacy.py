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
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
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
#    
# 
#
import FormatTemplate
from time import gmtime, strftime
import collections
import dateutil.parser
import types

class Format(FormatTemplate.Formatter):
    
    def execute(self, data):
        """
        Main method of execution to generate Legacy text
        @param data: dictionary values provided by the product generator
        @return: Returns the dictionary in Legacy format.
        """
        data = self.cleanDictKeys(data)
        text = ''

        text += self.processWmoHeader(data['wmoHeader']) + '\n'
        easMessage = self.processEAS(data)
        if easMessage is not None:
            text += easMessage + '\n'
        text += data['productName'] + '\n'
        text += data['senderName'] + '\n'
        text += strftime('%H%M %p %Z %a %b %Y', gmtime()) + '\n\n'
        # Test if this is a watch
        text += '|* DEFAULT OVERVIEW SECTION *|\n\n'
        text += self.processSegments(data['segments'])
        return str(text.upper())
    
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
                    if vtecRecord.get("pvtecRecordType") == "pvtecRecord" and vtecRecord.get('significance') is 'A':
                        return 'URGENT - IMMEDIATE BROADCAST REQUESTED'
                return 'BULLETIN - EAS ACTIVATION REQUESTED'       
        return None
        
    def processSegments(self, segments):
        """
        Generates Legacy text from a list of segments
        @param data: a list of dictionaries 
        @return: Returns the legacy text of the segments.
        """  
        text = ''
        for segment in segments['segment']:
            text += segment["ugcHeader"] + "\n"
            if 'vtecRecords' in segment:
                vtecRecords = segment['vtecRecords']
                for vtecRecord in vtecRecords:
                    text += vtecRecord['vtecString'] + '\n'
            areaString = segment.get('areaString', "")
            if areaString:
                text += areaString + "\n"
            cityString = segment.get('cityString', "")
            if cityString:
                text += cityString + "\n"              
            text += strftime('%H%M %p %Z %a %b %Y', gmtime()) + '\n\n'

            text += segment['description'] + '\n'

            if 'callsToAction' in segment and segment['callsToAction']:
                callsToAction = segment['callsToAction']
                text += 'PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n'
                for cta in callsToAction['callToAction']:
                    text += cta + '\n\n'
                text += '&&\n\n'
            
            if 'polygonText' in segment and segment['polygonText']:
                text += segment['polygonText'] + '\n\n'
                
            text += '$$'
        return text
        
    def cleanDictKeys(self, data):
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