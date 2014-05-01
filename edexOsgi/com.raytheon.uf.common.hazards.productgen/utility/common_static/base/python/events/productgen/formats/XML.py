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
from com.raytheon.uf.common.hazards.productgen import ProductUtils
from xml.etree.ElementTree import Element, SubElement, tostring
import FormatTemplate
import os
import collections
from KeyInfo import KeyInfo

class Format(FormatTemplate.Formatter):
    
    def execute(self, productDict):
        '''
        Main method of execution to generate XML
        @param productDict: dictionary values provided by the product generator
        @return: Returns the dictionary in XML format.
        '''
        xml = Element('product')
        self.dictionary(xml, productDict)
        return ProductUtils.prettyXML(tostring(xml), True)
    
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
        'cityList',
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
    
    def dictionary(self, xml, productDict):
        '''
        Returns the dictionary in XML format.
        @param productDict: dictionary values
        @return: Returns the dictionary in XML format.
        '''   
        if productDict is not None:
            for key in productDict:
                value = productDict[key]
                editable = False
                if isinstance(key, KeyInfo):
                    editable = key.isEditable()
                    key = key.getName()
                
                if key not in self.xmlKeys():
                    continue
                if isinstance(value, dict):
                    subElement = SubElement(xml,key)
                    self.dictionary(subElement, value)
                elif isinstance(value, list):
                    if key == 'cityList':
                        subElement = SubElement(xml,'cityList')               
                        if editable:
                            subElement.attrib['editable'] = 'true'
                        self.list(subElement, 'city', value) 
                    else:
                        self.list(xml, key, value)
                else:
                    subElement = SubElement(xml,key)
                    subElement.text = value
                    if editable:
                        subElement.attrib['editable'] = 'true'
    
    def list(self, xml, key, data):
        '''
        Returns the list in XML format.
        @param data: list of values
        @return: Returns the list in XML format.
        '''
        editable = False
        if isinstance(key, KeyInfo):
            editable = key.isEditable()
            key = key.getName()  
        if data is not None:
            for value in data:

                subElement = SubElement(xml, key)
                if editable:
                    subElement.attrib['editable'] = 'true'
                if isinstance(value, dict):
                    self.dictionary(subElement, value)
                else:          
                    subElement.text = value
