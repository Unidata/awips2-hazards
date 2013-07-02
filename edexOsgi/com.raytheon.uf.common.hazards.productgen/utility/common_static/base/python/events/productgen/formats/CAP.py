'''
    Description: Create CAP messages corresponding to a single legacy product
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    June     2013  648     Tracy.L.Hansen  Format CAP XML messages
    
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
        
##########################
OUTPUT :  List of CAP Messages 

SAMPLE CAP Message
NOTE: A CAP message has only one segment.  
If a product has multiple segments, a CAP message will be generated for each segment.

            <?xml version = '1.0' encoding = 'UTF-8' standalone = 'yes'?>
            <?xml-stylesheet href='http://www.weather.gov/alerts-beta/capatomproduct.xsl'
            type='text/xsl'?>
            <alert xmlns = 'urn:oasis:names:tc:emergency:cap:1.1'>
            <!-- http-date = Tue, 25 Jan 2011 08:37:00 GMT -->
            <identifier>NOAA-NWS-ALERTS-
            FL20110125203700FlashFloodWarning20110125213000FL.TBWSVRTBW.f809e7f8f
            fe0c3658e925873d720fe9c</identifier>
            <sender>w-nws.webmaster@noaa.gov</sender>
            <sent>2011-01-24T19:00:00-05:00</sent>
            <status>Actual</status>
            <msgType>Alert</msgType>
            <scope>Public</scope>
            <note>Alert for Citrus; Hernando; Pasco (Florida) Issued by the National
            Weather Service</note>
            <references></references>
            <info>
            <category>Met</category>
            <event>Flash Flood Warning</event>
            <urgency>Immediate</urgency>
            <severity>Severe</severity>
            <certainty>Observed</certainty>
            <eventCode>
            <valueName>SAME</valueName>
            <value>SVR</value>
            </eventCode>
            <effective>2011-01-25T15:37:00-05:00</effective>
            <expires>2011-01-25T16:30:00-05:00</expires>
            <senderName>NWS Tampa Bay (West Central Florida)</senderName>
            <headline>Flash Flood Warning issued January 25 at 3:37PM EST
            expiring January 25 at 4:30PM EST by NWS Tampa Bay</headline>
            <description>THE NATIONAL WEATHER SERVICE IN RUSKIN HAS ISSUED A
            * FLASH FLOOD WARNING FOR...
            CITRUS COUNTY IN FLORIDA.
            HERNANDO COUNTY IN FLORIDA.
            PASCO COUNTY IN FLORIDA.
            * UNTIL 430 PM EST
            * AT 330 PM EST...FLASH FLOODING WAS REPORTED FROM HEAVY RAIN. THIS RAIN WAS LOCATED OVER THE WARNED AREA.
            * LOCATIONS IN THE WARNING INCLUDE BUT ARE NOT LIMITED TO PLATTSMOUTH...LINCOLN...NEBRASKA CITY...PAWNEE CITY...TABLE ROCK...</description>
            -3-
            <instruction>MOST FLOOD DEATHS OCCUR IN AUTOMOBILES. NEVER DRIVE YOUR VEHICLE INTO AREAS WHERE THE WATER COVERS THE ROADWAY. FLOOD WATERS ARE USUALLY DEEPER THAN THEY APPEAR. JUST ONE FOOT OF FLOWING WATER IS POWERFUL ENOUGH TO SWEEP VEHICLES OFF THE ROAD. WHEN ENCOUNTERING FLOODED ROADS MAKE THE SMART CHOICE...TURN AROUND...DONT DROWN.</instruction>
            <parameter>
            <valueName>WMOHEADER</valueName>
            <value></value>
            </parameter>
            <parameter>
            <valueName>UGC</valueName>
            <value>FLC017-053-101</value>
            </parameter>
            <parameter>
            <valueName>VTEC</valueName>
            <value>/O.NEW.KTBW.FF.W.0003.110125T2037Z-110125T2130Z/</value>
            </parameter>
            <parameter>
            <valueName>TIME...MOT...LOC</valueName>
            <value>2037Z 270DEG 42KT 2878 8270 2817 8314</value>
            </parameter>
            <area>
            <areaDesc>Citrus; Hernando; Pasco</areaDesc>
            <polygon>+28.23,-82.76 +28.54,-82.66 +28.66,-82.68 +28.70,-82.65 +28.74,-
            82.69 +28.75,-82.65 +28.76,-82.69 +28.86,-82.73 +28.89,-82.66 +28.95,-82.73
            +28.97,-82.31 +28.79,-82.17 +28.72,-82.25 +28.64,-82.26 +28.57,-82.21
            +28.52,-82.05 +28.44,-82.05 +28.17,-82.22 +28.17,-82.86 +28.23,-
            82.76</polygon>
            <geocode>
            <valueName>FIPS6</valueName>
            <value>012017</value>
            </geocode>
            <geocode>
            <valueName>FIPS6</valueName>
            <value>012053</value>
            </geocode>
            <geocode>
            <valueName>FIPS6</valueName>
            <value>012101</value>
            </geocode>
            <geocode>
            <valueName>UGC</valueName>
            <value>FLC017</value>
            </geocode>
            <geocode>
            <valueName>UGC</valueName>
            <value>FLC053</value>
            </geocode>
            <geocode>
            <valueName>UGC</valueName>
            <value>FLC101</value>
            </geocode>
            </area>
            -4-
            </info>
            </alert>


##########################
INPUT : Product Dictionary for one legacy product

SAMPLE Product Dictionary
 {
        'disclaimer': 'This XML wrapped text product should be considered COMPLETELY EXPERIMENTAL. The National Weather Service currently makes NO GUARANTEE WHATSOEVER that this product will continue to be supplied without interruption. The format of this product MAY CHANGE AT ANY TIME without notice.', 
        'senderName': 'NATIONAL WEATHER SERVICE OMAHA/VALLEY NE', 
        'productName': 'FLOOD WATCH', 
        'productID': 'FFA', 
        'wmoHeader': {
            'TTAAii': 'WGUS63', 
            'originatingOffice': 'KOAX', 
            'productID': 'FFA', 
            'siteID': 'OAX', 
            'wmoHeaderLine': 'WGUS63 KOAX 080400', 
            'awipsIdentifierLine': 'FFAOAX'
        }, 
        'overview': '', 
        'synopsis': '', 
        'segments': {
            'segment': [
                {
                    'description:editable': '...FLASH FLOOD WATCH in effect through this morning...\nTHE NATIONAL WEATHER SERVICE IN OMAHA/VALLEY HAS ISSUED A\n\n* TEST FLASH FLOOD WATCH FOR PORTIONS OF EAST CENTRAL NEBRASKA AND \n SOUTHEAST NEBRASKA...INCLUDING THE FOLLOWING AREAS...IN EAST \n CENTRAL NEBRASKA...Douglas...Sarpy AND Saunders. IN SOUTHEAST \n NEBRASKA...Cass...Lancaster AND Otoe.\n\n* from late Monday night through Tuesday morning\n* |* BASIS FOR THE WATCH *|\n\n* |* (OPTIONAL...text truncated
                    'ugcCodes': {
                        'ugcCode': [
                            {
                                'state': 'NE', 
                                'type': 'Zone', 
                                'number': '051', 
                                'text': 'NEZ051', 
                                'subArea': ''
                            }, 
                            {
                                'state': 'NE', 
                                'type': 'Zone', 
                                'number': '052', 
                                'text': 'NEZ052', 
                                'subArea': ''
                            }, 
                            {
                                'state': 'NE', 
                                'type': 'Zone', 
                                'number': '053', 
                                'text': 'NEZ053', 
                                'subArea': ''
                            }, 
                            {
                                'state': 'NE', 
                                'type': 'Zone', 
                                'number': '066', 
                                'text': 'NEZ066', 
                                'subArea': ''
                            }, 
                            {
                                'state': 'NE', 
                                'type': 'Zone', 
                                'number': '067', 
                                'text': 'NEZ067', 
                                'subArea': ''
                            }, 
                            {
                                'state': 'NE', 
                                'type': 'Zone', 
                                'number': '068', 
                                'text': 'NEZ068', 
                                'subArea': ''
                            }
                        ]
                    }, 
                    'ugcHeader': 'NEZ051>053-066>068-081200-', 
                    'areaString': 'Saunders-Douglas-Sarpy-Lancaster-Cass-Otoe-', 
                    'cityString': 'INCLUDING THE CITIES OF ...WAHOO...ASHLAND...YUTAN...OMAHA...BELLEVUE...PAPILLION...LA VISTA...LINCOLN...PLATTSMOUTH...NEBRASKA CITY', 
                    'areaType': 'area', 
                    'expireTime': '2011-02-08T12:00:00', 
                    'vtecRecords': [
                        {
                            'vtecRecordType': 'pvtecRecord', 
                            'name': 'pvtecRecord', 
                            'productClass': '0', 
                            'action': 'NEW', 
                            'site': 'KOAX', 
                            'phenomena': 'FF', 
                            'significance': 'A', 
                            'eventTrackingNumber': '0001', 
                            'startTimeVTEC': '110208T0400Z', 
                            'startTime': '2011-02-08T04:00:00', 
                            'endTimeVTEC': '110208T1200Z', 
                            'endTime': '2011-02-08T12:00:00', 
                            'vtecString': '0.NEW.KOAX.FF.A.0001.110208T0400Z-110208T1200Z'
                        }
                    ], 
                    'polygons': {
                        'polygon': [
                            {
                                'point': [
                                    {
                                        'latitude': '41.117893219', 
                                        'longitude': '-96.6356201172'
                                    }, 
                                    {
                                        'latitude': '40.7864837646', 
                                        'longitude': '-96.6356201172'
                                    }, 
                                    {
                                        'latitude': '40.5813293457', 
                                        'longitude': '-96.3042144775'
                                    }, 
                                    {
                                        'latitude': '40.8180465698', 
                                        'longitude': '-95.9254608154'
                                    }, 
                                    {
                                        'latitude': '41.2914848328', 
                                        'longitude': '-96.0517120361'
                                    }, 
                                    {
                                        'latitude': '41.117893219', 
                                        'longitude': '-96.6356201172'
                                    }
                                ]
                            }
                        ]
                    }, 
                    'timeMotionLocation': {}, 
                    'impactedLocations': {
                        'location': [
                            {
                                'locationName': 'Douglas'
                            }, 
                            {
                                'locationName': 'Sarpy'
                            }, 
                            {
                                'locationName': 'Saunders'
                            }, 
                            {
                                'locationName': 'Cass'
                            }, 
                            {
                                'locationName': 'Lancaster'
                            }, 
                            {
                                'locationName': 'Otoe'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.0405', 
                                    'longitude': '-96.3709'
                                }
                            }, 
                            {
                                'locationName': 'ASHLAND'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.2432', 
                                    'longitude': '-96.3973'
                                }
                            }, 
                            {
                                'locationName': 'YUTAN'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.2152', 
                                    'longitude': '-96.62'
                                }
                            }, 
                            {
                                'locationName': 'WAHOO'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.2639', 
                                    'longitude': '-96.0117'
                                }
                            }, 
                            {
                                'locationName': 'OMAHA'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.1572', 
                                    'longitude': '-96.0405'
                                }
                            }, 
                            {
                                'locationName': 'PAPILLION'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.1564', 
                                    'longitude': '-95.9226'
                                }
                            }, 
                            {
                                'locationName': 'BELLEVUE'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.1843', 
                                    'longitude': '-96.0391'
                                }
                            }, 
                            {
                                'locationName': 'LA VISTA'
                            }, 
                            {
                                'point': {
                                    'latitude': '40.8164', 
                                    'longitude': '-96.6882'
                                }
                            }, 
                            {
                                'locationName': 'LINCOLN'
                            }, 
                            {
                                'point': {
                                    'latitude': '41.0077', 
                                    'longitude': '-95.8914'
                                }
                            }, 
                            {
                                'locationName': 'PLATTSMOUTH'
                            }, 
                            {
                                'point': {
                                    'latitude': '40.6762', 
                                    'longitude': '-95.8607'
                                }
                            }, 
                            {
                                'locationName': 'NEBRASKA CITY'
                            }
                        ]
                    }, 
                    'observations': {}, 
                    'callsToAction': {
                        'callToAction': [
                            'A FLASH FLOOD WATCH MEANS THAT CONDITIONS MAY DEVELOP THAT LEAD TO FLASH FLOODING. FLASH FLOODING IS A VERY DANGEROUS SITUATION.\n\nYOU SHOULD MONITOR LATER FORECASTS AND BE PREPARED TO TAKE ACTION SHOULD FLASH FLOOD WARNINGS BE ISSUED.'
                        ]
                    }, 
                    'polygonText': ''
                    'status:skip':'Actual',
                    'CAP_areaString:skip': 'Saunders; Douglas; Sarpy; Lancaster; Cass; Otoe (Nebraska)',
                    'info:skip': [{
                            'category': 'Met', 
                            'event': 'Flash Flood Warning', 
                            'responseType': 'Monitor', 
                            'urgency': 'Past', 
                            'severity': 'Severe', 
                            'certainty': 'Observed', 
                            'onset': '2011-02-08T04:00:00', 
                            'WEA_text': '', 
                            'pil': 'FFAOAX', 
                            'eventEndingTime', '2011-02-08T12:00:00',
                            }]
                }
            ]
        }, 
        'easActivationRequested': 'true', 
        'sentTimeZ': '2011-02-08T04:00:00', 
        'sentTimeLocal': '2011-02-08T04:00:00'
    }

'''

import FormatTemplate
from xml.etree.ElementTree import Element, SubElement, tostring
import os, collections, datetime, dateutil.parser
from TextProductCommon import TextProductCommon

class Format(FormatTemplate.Formatter):
    
    def execute(self, prodDict):
        '''
        Main method of execution to generate CAP (Common Alerting Protocol) messages
        Loops through the segments of the product dictionary producing a CAP message for each
        
        @param prodDict: dictionary values provided by the product generator
        @return: Returns the resulting CAP messages in XML format.
        '''         
        self._tpc = TextProductCommon()
        self.capVersion = 'urn:oasis:names:tc:emergency:cap:1.2'
        self.issuedBy = ' Issued by '

        messages = []       
        if prodDict is not None:
            # For each segment of the prodDict, there will be a separate CAP message
            for segDict in prodDict.get('segments')['segment']:
                # Establish time zone to use for the segment
                self._tz = self._tpc.getVal(segDict, 'timeZones')[0]
                xml = Element('alert')
                xml.attrib['xmlns'] = self.capVersion
                self.createCAP_Message(xml, prodDict, segDict)
                messages.append(tostring(xml))
        return messages
   
    def createCAP_Message(self, xml, prodDict, segDict):
        '''
        Returns the CAP message in XML format for the given segment.

        @param xml: XML data structure that we are building
        @param prodDict: dictionary values for an entire legacy product
        @param segDict: dictionary of values for a segment of the legacy product
        @return: Returns the resulting CAP message XML format.
        ''' 
        # Main Section                  
        for tag in self.CAP_tags(): 
            self.createXML(xml, tag, prodDict, segDict) 
        # Info Section
        infoDicts = self._tpc.getVal(segDict, 'info')
        for infoDict in infoDicts:
            infoElement = SubElement(xml, 'info')
            for tag in self.info_tags():
                self.createXML(infoElement, tag, prodDict, segDict, infoDict)
            # Parameters and Event Code
            self.createBlocks(infoElement, prodDict, segDict, infoDict)
            # Area
            self.createAreas(infoElement, prodDict, segDict, infoDict)

    class tag:
        '''
        The tag class holds the information about how to create an XML tag and value.
        It is used by the method createXML.
        '''
        def __init__(self, tagName, method=None, prodKey=None, segKey=None, infoKey=None, value=None):  
            self.method = method 
            self.tagName = tagName
            self.prodKey = prodKey
            self.segKey = segKey 
            self.infoKey = infoKey  
            self.value = value
             
    def CAP_tags(self):
        '''
        Specifies the tags in the Main Section
        '''
        return [ 
            self.tag('identifier', method=self.createIdentifier),
            self.tag('sender', value=self.sender()),
            self.tag('sent', prodKey='sentTimeLocal'),  #2011-01-25T15:37:00-05:00
            self.tag('status', segKey='status'),
            self.tag('msgType', self.createMsgType),
            self.tag('scope', value='Public'),
            self.tag('code', value='IPAWSv1.0'),
            self.tag('note', method=self.createNote),
            self.tag('references', method=self.createReferences),
            ]
    
    def sender(self):
        return 'w-nws.webmaster@noaa.gov'
        
    def info_tags(self):
        '''
        Specifies the tags in the Info Sections
        '''
        return [
            self.tag('category', value='Met'),
            self.tag('event', infoKey='event'),
            self.tag('responseType', infoKey='responseType'),
            self.tag('urgency', infoKey='urgency'),
            self.tag('certainty', infoKey='certainty'),
            self.tag('effective', prodKey='sentTimeLocal'), #2011-01-25T15:37:00-05:00
            self.tag('onset', self.createOnset), 
            self.tag('expires', self.createExpires), 
            self.tag('senderName'),  # NWS Tampa Bay (West Central Florida
            self.tag('headline', self.createHeadline),
            self.tag('description', segKey='description'),
            self.tag('instruction', method=self.createCallsToAction),
            self.tag('web', value='http://www.weather.gov')
            ]

    def createXML(self, xml, tag, prodDict, segDict, infoDict=None):  
        '''
        For a given tag object, determine it's value and create an XML entry for it
        
        @param xml: parent node in Element tree 
        @param tag: a tag object specifying the tag and its value
        @prodDict: dictionary values provided by the product generator
        @segDict: dictionary of values for a segment of the legacy product
        @infoDict: dictionary of values for an info section. 
          There could be multiple info sections when multiple hazards are in a segment.
          The info section can also be used for generating different languages.
        '''    
        subElement = SubElement(xml, tag.tagName)
        value = None
        if tag.value:
            value = tag.value
        elif tag.method:
            value = tag.method(prodDict, segDict, infoDict)
        elif tag.prodKey:
            value = self._tpc.getVal(prodDict, tag.prodKey)
        elif tag.segKey:
            value = self._tpc.getVal(segDict, tag.segKey)
        elif tag.infoKey:
            value = self._tpc.getVal(infoDict, tag.infoKey)
        if value: 
            subElement.text = value   


    '''
    The 'create' methods all take these arguments 
        @param xml: parent node in Element tree 
        @prodDict: dictionary values provided by the product generator
        @segDict: dictionary of values for a segment of the legacy product
        @infoDict: dictionary of values for an info section. 
    
    '''                                                           
    def createBlocks(self, xml, prodDict, segDict, infoDict):
        '''
        Create the parameters and eventCode blocks for the CAP info section
        '''
        if 'vtecRecords' in segDict:
            vtecRecords = segDict['vtecRecords']
            for vtecRecord in vtecRecords:
                vtecString = vtecRecord['vtecString']
                self.createBlock(xml, 'parameter', {'valueName':'VTEC', 'value':vtecString})             
        self.createBlock(xml, 'parameter', {'valueName':'EAS-ORG', 'value':'WXR'})             
        self.createBlock(xml, 'parameter', {'valueName':'PIL', 'value':segDict.get('pil')}) 
        dt = self._tpc.getVal(infoDict, 'eventEndingTime_datetime')
        eventEndingTime = self._tpc.formatDatetime(dt, timeZone=self._tz)       
        self.createBlock(xml, 'parameter', {'valueName':'eventEndingTime', 'value':eventEndingTime})             
        weaText = segDict.get('WEA_text', '')
        if weaText:
            dt = self._tpc.getVal(segDict, 'expireTime_datetime')
            dStr = self._tpc.formatDatetime(dt, '%I:%M %p %Z %a', self._tz)
            weaText = weaText.replace('%s', dStr)
        self.createBlock(xml, 'parameter', {'valueName':'CMAMtext', 'value':weaText})             
        self.createBlock(xml, 'parameter', {'valueName':'TIME...MOT...LOC', 'value':segDict.get('timeMotionLocation')})             
        self.createBlock(xml, 'eventCode', {'valueName':'SAME', 'value':prodDict.get('productID')})             
                
    def createAreas(self, xml, prodDict, segDict, infoDict):  
        '''
        create the areas portion of the CAP info section
        '''      
        areaElement = SubElement(xml, 'area')
        self.xmlSubElement(areaElement, 'areaDesc', segDict.get('areaString'))
        if prodDict['productID'] in ['TOR', 'SVR', 'SVS', 'SMW', 'MWS', 'FFW', 'FFS', 'FLS', 'EWW']:
            polyStr = ''
            polygons = segDict.get('polygons')
            polyList = polygons.get('polygon')
            if polyList:
                for polygon in polyList:
                    for point in polygon.get('point'):
                        polyStr += point.get('latitude')+', '+point.get('longitude') + ' '                    
                    self.xmlSubElement(areaElement, 'polygon', polyStr)
        # geoCodes
        if 'ugcCodes' in segDict:
            ugcs = segDict['ugcCodes']['ugcCode']
            for ugcDict in ugcs:
                self.createBlock(areaElement, 'geocode', {'valueName':'UGC', 'value':ugcDict.get('text')} )     
     
    def createIdentifier(self, prodDict, segDict, infoDict): 
        # TODO identifier 
        self.identifier = 'NOAA-NWS-ALERTS-FL20110125203700FlashFloodWarning20110125213000FL.TBWSVRTBW.f809e7f8ffe0c3658e925873d720fe9c' 
        return self.identifier
    
    def createMsgType(self, prodDict, segDict, infoDict):
        '''
         NOTE: First check for Alert, then Update, then Cancel
         
         VTEC NEW, EXA, EXT, EXB --> 'Alert' - Initial information requiring attention by targeted recipients
         VTEC CON, UPG, COR --> 'Update' - Updates and supercedes the earlier message(s) identified in <references> 
         VTEC CAN, EXP --> 'Cancel' - Cancels the earlier message(s) identified in <references> 
        
        @return CAP msgType based on segment VTEC
        '''
        self.msgType = 'Alert'
        if 'vtecRecords' in segDict:
            vtecRecords = segDict['vtecRecords']
            for CAP_action, VTEC_codes in [('Alert', ['NEW', 'EXA', 'EXT', 'EXB']),
                                           ('Update', ['CON', 'UPG', 'COR']),
                                           ('Cancel', ['CAN', 'EXP'])]:                                  
                for vtecRecord in vtecRecords:
                    if vtecRecord['vtecRecordType'] == 'pvtecRecord':
                        action = vtecRecord.get('action')
                        if action in VTEC_codes:
                            self.msgType = CAP_action
                            return self.msgType
        return self.msgType

    
    def createNote(self, prodDict, segDict, infoDict):
        '''
        Create the note e.g. Alert for Citrus; Hernando; Pasco (Florida) Issued by the National Weather Service

        @return: note string        
        '''
        aStr = self._tpc.getVal(segDict, 'CAP_areaString')
        note = self.msgType + ' for '+ aStr + self.issuedBy + 'the National Weather Service'
        return note

    def createReferences(self, prodDict, segDict, infoDict):
        '''
        <references>sender,identifier,sent</references> 
        Where sender,identifier, and sent are the sender, identifier, and sent elements from the earlier 
        CAP message or messages that this one replaces. When multiple messages are referenced, they 
        are separated by whitespace. 
        Example <references>w-nws.webmaster@noaa.gov,NWS-130404-301701-246008,2010-12-29T09:36:23-07:00</references>
        
        Inclusion: Included whenever the NWS updates or cancels an alert for which a CAP message has been produced.

        '''
        reference = ''
        # TODO -- Outstanding issue
        #if self.msgType in ['Update', 'Cancel']:
        #    reference = 'w-nws.webmaster@noaa.gov, '+self.identifier + ', '+prodDict.get('sentTimeLocal')
        return reference

    def createEvent(self, prodDict, segDict, infoDict):
        '''
        e.g. Flash Flood Warning

        @prodDict: dictionary values provided by the product generator
        @segDict: dictionary of values for a segment of the legacy product
        @infoDict: dictionary of values for an info section. 

        '''
        self.event = self._tpc.getVal(segDict, "headlines")[0]
        return self.event
    
    def createOnset(self, prodDict, segDict, infoDict):
        '''
        Create onset entry 
        '''
        dt = self._tpc.getVal(infoDict, 'onset_datetime')
        return self._tpc.formatDatetime(dt, timeZone=self._tz)

    def createExpires(self, prodDict, segDict, infoDict):
        '''
        Create expires entry
        '''
        dt = self._tpc.getVal(segDict, 'expireTime_datetime')
        return self._tpc.formatDatetime(dt, timeZone=self._tz)
    
    
    def createHeadline(self, prodDict, segDict, infoDict):
        # TODO Outstanding issue: Handle multiple events per segment
        '''
        "Flash Flood Warning issued January 25 at 3:37PM EST expiring January 25 at 4:30PM EST by NWS Tampa Bay"

        <headline>WWA issued Month DD at hh:mmAM/PM LST/LDT until Month DD at hh:mmAM/PM LST/LDT by NWS Office</headline>
                
        WWA = Watch, Warning, Advisory, or special statement 
        MONTH = Month spelled out 
        DD = Day (1-31) 
        hh = Hour (1-12) 
        mm = Minutes (00-59) 
        LST/LDT = Local Standard Time or Local Daylight Time as appropriate
        Office = Name of the NWS office which issued the alert
        For very long duration or open-ended alerts (e.g., long duration floods, hurricanes, tsunamis, 
        etc.) which are in effect until further notice, the format for <headline> is as follows.
        <headline>WWA issued Month DD at hh:mmAM/PM LST/LDT until further notice by NWS Office</headline>

        @prodDict: dictionary values provided by the product generator
        @segDict: dictionary of values for a segment of the legacy product
        @infoDict: dictionary of values for an info section. 

        '''
        st = infoDict.get('onset_datetime')
        format = '%B %d at %I:%M%p %Z'
        st = self._tpc.formatDatetime(st, format=format, timeZone=self._tz)  
        et = infoDict.get('eventEndingTime_datetime')
        if et:
            et = ' expiring ' + self._tpc.formatDatetime(et, format=format, timeZone=self._tz)  
        else:
            et = '' 
        return infoDict.get('event') + ' issued ' + st + et + ' by NWS ' + infoDict.get('sentBy')
    
    def createCallsToAction(self, prodDict, segDict, infoDict):
        '''
        
        @prodDict: dictionary values provided by the product generator
        @segDict: dictionary of values for a segment of the legacy product
        @infoDict: dictionary of values for an info section. 
        
        '''
        if 'callsToAction' in segDict and segDict['callsToAction']:
            callsToAction = segDict['callsToAction']
            text = ''
            for cta in callsToAction['callToAction']:
                text += cta + '\n'
        return text
    
    # Helper methods for creating xml blocks
    def createBlock(self, xml, blockTag, blockElements): 
        blockEle = SubElement(xml, blockTag)
        for key in blockElements:
            self.xmlSubElement(blockEle, key, blockElements[key])
                       
    def xmlSubElement(self, xml, tag, text=None, attrs={}):
        sub = SubElement(xml, tag, attrs)
        if text is not None:
            sub.text = text
