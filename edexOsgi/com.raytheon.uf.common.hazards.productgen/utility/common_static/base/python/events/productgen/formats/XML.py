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

#    Formats a dictionary 'data' and generates XML. The dictionary keys
#    will be represent the XMG tag whereas the dictionary value will be  
#    the text surrounded by the XML tag. 
#
#    If the value is a dictionary, then the value will become a sub-XML tag. 
#    If the value is a list, then sub-XML tags with the same tag will be created.
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/10/13                      jsanchez       Initial Creation.
#    11/05/13        2266          jsanchez       Used ProductUtil to format xml into pretty xml.
#    
# 
#
from Editable import Editable
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from xml.etree.ElementTree import Element, SubElement, tostring
import FormatTemplate
import os
import collections

class Format(FormatTemplate.Formatter):
    
    def execute(self, data):
        '''
        Main method of execution to generate XML
        @param data: dictionary values provided by the product generator
        @return: Returns the dictionary in XML format.
        '''
        self.editables = Editable(data)  
        xml = Element('product')
        self.dictionary(xml, data)

        return self.formatFrom(xml), self.editables._getResult()
    
    def xmlKeys(self): 
        return [   
        'disclaimer',
        'senderName',
        'productName',
        'productID',
        'wmoHeader',
        'TTAAii',
        'originatingOffice',
        'productID',
        'siteID',
        'wmoHeaderLine',
        'awipsIdentifierLine',
        'overview',
        'synopsis',
        
        'segments',
        'segment',
        'ugcCodes',
        'ugcCode',
        'state',
        'type',
        'number',
        'text',
        'subArea',
        'ugcHeader',
        'areaString',
        'cityString',
        'areaType',
        'expireTime',
        'vtecRecords',
        'vtecRecordType',
        
        'sections',
        'section',
        'name',
        'productClass',
        'action',
        'site',
        'phenomenon',
        'significance',
        'eventTrackingNumber',
        'startTimeVTEC',
        'startTime',
        'endTimeVTEC',
        'endTime',
        'vtecString',
        'nwsli',
        'floodSeverity',
        'immediateCause',
        'floodBeginTimeVTEC',
        'floodCrestTimeVTEC',
        'floodEndTimeVTEC',
        'floodBeginTime',
        'floodCrestTime',
        'floodEndTime',
        'floodRecordStatus',
        'polygons',
        'polygon',
        'point',
        'latitude',
        'longitude',
        'timeMotionLocation',
        'impactedLocations',
        'location',
        'locationName',
        'observations',
        
        'description',
        
        'callsToAction',
        'callToAction',
        'polygonText',
        'easActivationRequested',
        'sentTimeZ',
        'sentTimeLocal',
        ]
    
    def dictionary(self, xml, data):
        '''
        Returns the dictionary in XML format.
        @param data: dictionary values
        @return: Returns the dictionary in XML format.
        '''   
        if data is not None:
            for key in data:
                editable = False
                if ':editable' in key:
                    editable = True
                if key not in self.xmlKeys():
                    if ':editable' in key:
                        editable = True
                        tmp = key[:-9]
                        if tmp not in self.xmlKeys():
                            continue
                    else:
                        continue
                value = data[key]
                if isinstance(value, dict):
                    subElement = SubElement(xml,key)
                    self.dictionary(subElement, value)
                elif isinstance(value, list):
                    self.list(xml, key, value)
                else:
                    if editable:
                        key = key[:-9]
                    subElement = SubElement(xml,key)
                    subElement.text = value
                    if editable:
                        subElement.attrib['editable'] = 'true'
                        self.editables.add(key,ProductUtils.prettyXML(tostring(subElement), False))
    
    def list(self, xml, key, data):
        '''
        Returns the list in XML format.
        @param data: list of values
        @return: Returns the list in XML format.
        '''
        editable = False
        if ':editable' in key:
            key = key[:-9]
            editable = True    
        if data is not None:
            for value in data:
                subElement = SubElement(xml, key)
                if editable:
                    subElement.attrib['editable'] = 'true'
                if isinstance(value, dict):
                    self.dictionary(subElement, value)
                else:          
                    subElement.text = value
                    
    def formatFrom(self, text):
        return ProductUtils.prettyXML(tostring(text), True)