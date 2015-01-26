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

#    Formats a dictionary 'data' and generates watch, warning, advisory legacy text. The dictionary values
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
    
    def execute(self, productDict):
        '''
        Main method of execution to generate Legacy text
        @param productDict: dictionary values provided by the product generator
        @return: Returns the dictionary in Legacy format.
        '''     
        from TextProductCommon import TextProductCommon
        self._tpc = TextProductCommon()        
        self.productDict = productDict
        productParts = self._tpc.getVal(productDict, 'productParts', [])
        text = self._processProductParts(productDict, productParts.get('partsList'))
        printText = text.replace('\n','CR\n')
        text = self._tpc.endline(text)
        return str(text) 
    
    def _printDebugProductParts(self):   
        # IF True will print the product parts and associated text during execution
        return False
    
    def _processProductParts(self, productDict, productParts, skipParts=[]):
        '''
        Adds the product parts to the product
        @param productDict -- dictionary of information -- could be the product dictionary or a sub-part such as a segment
        @param skipParts -- necessary to avoid repetition when calling this method recursively
        @param productParts -- list of instances of the ProductPart class with information about how to format each product part
        @return text -- product string
        '''
        text = ''
        for part in productParts:    
         
            valtype = type(part)
            if valtype is str:
                name = part
            elif valtype is tuple or valtype is list:
                name = part[0]
                infoDicts = part[1]

            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print 'Legacy Part:', name, ': ', 
            
            partText = ''                                   
            if name == 'wmoHeader': 
                partText = self.processWmoHeader(productDict['wmoHeader']) + '\n'
            elif name == 'wmoHeader_noCR': 
                partText = self.processWmoHeader(productDict['wmoHeader'])
            elif name == 'easMessage':
                easMessage = self.processEAS(productDict)
                if easMessage is not None:
                    partText = easMessage + '\n'
                else:
                    partText = ''
            elif name == 'productHeader':
                partText = self._tpc.getVal(productDict, 'productName', altDict=self.productDict) + '\n'
                partText += self._tpc.getVal(productDict, 'senderName', altDict=self.productDict) + '\n'
                partText += self._tpc.getVal(productDict, 'issuedByString', default='', altDict=self.productDict)
                partText += self.formatIssueTime()
            elif name == 'overview':
                partText = '|* DEFAULT OVERVIEW SECTION *|\n\n'
            elif name == 'vtecRecords':
                if 'vtecRecords' in productDict:
                    vtecRecords = productDict['vtecRecords']
                    for vtecRecord in vtecRecords:
                        partText += vtecRecord['vtecString'] + '\n'
            elif name == 'issuanceTimeDate':
                partText = self.formatIssueTime()
            elif name in ['callsToAction', 'callsToAction_productLevel']:
                callsToAction = productDict[name]
                if callsToAction:
                    partText += 'Precautionary/Preparedness Actions...\n\n'
                    for cta in callsToAction:
                        partText += cta + '\n\n'
                    if name in ['callsToAction_productLevel']:
                        partText += '&&\n\n'
            elif name == 'polygonText':
                if 'polygonText' in productDict and productDict['polygonText']:
                    partText += productDict['polygonText'] + '\n\n'
            elif name == 'endSegment':
                partText += '\n$$\n\n' 
            elif name == 'CR':
                partText += '\n'
            elif name == 'cityList':
                cities = 'Including the cities of '
                elements = KeyInfo.getElements('cityList', productDict)
                cityList = productDict[elements[0]]
                cities += self._tpc.getTextListStr(cityList)
                partText += cities + '\n'
            elif name == 'segments':
                text += self.processSubParts(productDict['segments'], infoDicts) 
            elif name == 'sections':
                text += self.processSubParts(productDict['sections'], infoDicts)
            else:    
                textStr = self._tpc.getVal(productDict, name)
                if textStr:
                    partText = textStr + '\n' 
                    
            if self._printDebugProductParts():
                if name not in ['segments', 'sections']:
                    print partText
                
            text += partText
        return text
        
    def processWmoHeader(self, wmoHeader):
        text = wmoHeader['wmoHeaderLine'] + '\n'
        text += wmoHeader['awipsIdentifierLine'] + '\n'
        return text
    
    def processEAS(self, productDict):
        request = self._tpc.getVal(productDict, 'easActivationRequested', altDict=self.productDict)
        if request == 'true':
            segments = self._tpc.getVal(productDict, 'segments', altDict=self.productDict)
            for segment in segments:
                vtecRecords = segment['vtecRecords']
                for vtecRecord in vtecRecords:
                    if vtecRecord.get('vtecRecordType') == 'pvtecRecord' and vtecRecord.get('significance') is 'A':
                        return 'URGENT - IMMEDIATE BROADCAST REQUESTED'
                return 'BULLETIN - EAS ACTIVATION REQUESTED'       
        return None
    
    def formatIssueTime(self):  
        text = ''  
        sentTimeZ = self._tpc.getVal(self.productDict, 'sentTimeZ_datetime')
        timeZones = self._tpc.getVal(self.productDict, 'timeZones')
        for timeZone in timeZones:
            text += self._tpc.formatDatetime(sentTimeZ, '%I%M %p %Z %a %b %e %Y', timeZone)
        return text + '\n'
            
    def processSubParts(self, subParts, infoDicts):
        """
        Generates Legacy text from a list of subParts e.g. segments or sections
        @param subParts: a list of dictionaries for each subPart
        @param partsLists: a list of Product Parts for each segment
        @return: Returns the legacy text of the subParts
        """
        text = '' 
        for i in range(len(subParts)):
            text += self._processProductParts(subParts[i], infoDicts[i].get('partsList'))
        return text
      
    def cleanDictKeys(self, productDict):
        '''
        Remove annotations (e.g. :editable) from the dictionary keys
        @param productDict -- dictionary
        @return -- cleaned dictionary
        '''
        temp = collections.OrderedDict()
        for key in productDict:
            d = productDict[key]
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
