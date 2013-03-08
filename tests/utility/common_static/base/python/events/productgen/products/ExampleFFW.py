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
import ProductTemplate
import collections

class Product(ProductTemplate.Product):
    
    def getScriptMetadata(self):
        metadata = {}
        metadata['author'] = "Johnny Dough"
        metadata['description'] = "A sample product generator for FFW."
        metadata['version'] = "1.0"
        return metadata
    
    def defineDialog(self):
        dialog = {'productID':'FFW'}
        return dialog
    
    def execute(self, hazardEventSet):
        self.disclaimer()
        self.senderName(hazardEventSet)
        self.wmoHeader(hazardEventSet)
        self.segments(hazardEventSet)
        self.data['productID'] = 'FFW'
        return [self.data]
        
    def disclaimer(self):
        disclaimer = 'This XML wrapped text product should be considered COMPLETELY EXPERIMENTAL. '
        disclaimer += 'The National Weather Service currently makes NO GUARANTEE WHATSOEVER that this '
        disclaimer += 'product will continue to be supplied without interuption. The format of this '
        disclaimer += 'product MAY CHANGE AT ANY TIME without notice.'
        self.data['disclaimer'] = disclaimer
        
    def senderName(self, hazardEventSet):
        self.data['senderName'] = 'National Weather Service Denver CO'
        
    def wmoHeader(self, hazardEventSet):
        wmoHeader = collections.OrderedDict()
        wmoHeader['TTAAii'] = 'WGUS55'
        wmoHeader['originatingOffice'] = 'KBOU'
        wmoHeader['sentTimeZ'] = '2012-07-27T19:07:00Z'
        wmoHeader['sentTimeLocal'] = '2012-07-27T13:07:00MDT'
        wmoHeader['productCategory'] = 'FFW'
        wmoHeader['geoID'] = 'BOU'
        wmoHeader['wmoHeaderLine'] = 'WGUS55 KBOU 271907'
        wmoHeader['awipsIdentifierLine'] = 'FFWBOU'
        self.data['wmoHeader'] = wmoHeader
    
    def segments(self, hazardEventSet):
        segmentList = []
        
        iterator = hazardEventSet.iterator()
        # Loop through the hazardEventSet
        while iterator.hasNext():
            hazardEvent = iterator.next();
            segment = self.segment(hazardEvent)
            segmentList.append(segment)
        
        segments = collections.OrderedDict()
        segments['segment'] = segmentList
        self.data['segments'] = segments
    
    def segment(self, hazardEvent):
        segment = collections.OrderedDict()
        segment['easActivationRequested'] = self.easActivationRequested(hazardEvent)
        segment['ugcCodes'] = self.ugcCodes(hazardEvent)
        segment['expireTime'] = self.expireTime(hazardEvent)
        segment['purgeTime'] = self.purgeTime(hazardEvent)
        segment['vtecRecords'] = self.vtecRecords(hazardEvent)
        segment['polygon'] = self.polygon(hazardEvent)
        segment['timeMotionLocation'] = self.timeMotionLocation(hazardEvent)
        segment['impactedLocations'] = self.impactedLocations(hazardEvent)
        segment['observations'] = self.observations(hazardEvent)
        segment['callsToAction'] = self.callsToAction(hazardEvent)
        segment['polygonText'] = self.polygonText(hazardEvent)
        
        return segment
    
    def easActivationRequested(self, hazardEvent):
        return 'true'
    
    def ugcCodes(self, hazardEvent):
        ugc = collections.OrderedDict()
        ugc['state'] = 'CO'
        ugc['type'] = 'County'
        ugc['number'] = '069'
        ugc['text'] = 'COC069'
        ugc['subArea'] = 'Central'
        
        ugcCodeList = [ugc]
        ugcCodes = collections.OrderedDict()
        ugcCodes['ugcCode'] = ugcCodeList
        
        return ugcCodes
    
    def expireTime(self, hazardEvent):
        return '2012-07-27T22:00:00Z'
    
    def purgeTime(self, hazardEvent):
        return '2012-07-27T22:00:00Z'
    
    def vtecRecords(self, hazardEvent):
        vtecRecords = collections.OrderedDict()
        pvtecRecord = collections.OrderedDict()
        hvtecRecord = collections.OrderedDict()
        
        pvtecRecord['productClass'] = 'O'
        pvtecRecord['action'] = 'NEW'
        pvtecRecord['site'] = 'KBOU'
        pvtecRecord['phenomena'] = 'FF'
        pvtecRecord['significance'] = 'W'
        pvtecRecord['eventTrackingNumber'] = '0095'
        pvtecRecord['startTimeVTEC'] = '120727T1907Z'
        pvtecRecord['startTime'] = '2012-07-26T22:33:00Z'
        pvtecRecord['endTimeVTEC'] = '120727T2200Z'
        pvtecRecord['endTime'] = '2012-07-26T23:15:00Z'
        pvtecRecord['vtecString'] = '/O.NEW.KBOU.FF.W.0022.120727T1907Z-120727T2200Z/'
        
        hvtecRecord['nwsli'] = '00000'
        hvtecRecord['floodSeverity'] = '0'
        hvtecRecord['immediateCause'] = 'ER'
        hvtecRecord['floodBeginTimeVTEC'] = '000000T0000Z'
        hvtecRecord['floodCrestTimeVTEC'] = '000000T0000Z'
        hvtecRecord['floodEndTimeVTEC'] = '000000T0000Z'
        hvtecRecord['floodBeginTime'] = '0000-00-00T00:00:00Z'
        hvtecRecord['floodCrestTime'] = '0000-00-00T00:00:00Z'
        hvtecRecord['floodEndTime'] = '0000-00-00T00:00:00Z'
        hvtecRecord['floodRecordStatus'] = 'OO'
        hvtecRecord['vtecString'] = '/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/'
        
        vtecRecords['pvtecRecord'] = pvtecRecord
        vtecRecords['hvtecRecord'] = hvtecRecord
        
        return vtecRecords
       
    def polygon(self, hazardEvent):
        cta1 = 'Excessive runoff from this storm will cause flash flooding of creeks '
        cta1 += 'and streams, roads, and roadside culverts. The heavy rains could '
        cta1 += 'also trigger rock slides or debris flows in the high park burn area.'
        cta2 = 'To report any flooding to the nearest law enforcement agency relay '
        cta2 += 'your report to the national weather service forecast office in '
        cta2 += 'denver.'
        
        pt1 = collections.OrderedDict()
        pt1['latitude'] = '40.47'
        pt1['longitude'] = '-105.52'
        
        pt2 = collections.OrderedDict()
        pt2['latitude'] = '40.72'
        pt2['longitude'] = '-105.32'
        
        pt3 = collections.OrderedDict()
        pt3['latitude'] = '40.60'
        pt3['longitude'] = '-105.31'
        
        pt4 = collections.OrderedDict()
        pt4['latitude'] = '40.52'
        pt4['longitude'] = '-105.52'
        
        pointList = [pt1, pt2, pt3, pt4]    
        polygon = collections.OrderedDict()
        polygon['point'] = pointList

        return polygon
    
    def timeMotionLocation(self, hazardEvent):
        timeMotionLocation = collections.OrderedDict()

        timeMotionLocation['time'] = '2012-07-27T19:02:00Z'
                
        motion = collections.OrderedDict()
        motion['direction'] = '045'
        motion['speed'] = '10'
        motion['unit'] = 'mph'
        timeMotionLocation['motion'] = motion
        
        location = collections.OrderedDict()
        pt1 = collections.OrderedDict()
        pt1['latitude'] = '40.47'
        pt1['longitude'] = '-105.52'
        pointList = [pt1]
        location['point'] = pointList
        timeMotionLocation['location'] = location
        
        return timeMotionLocation
    
    def impactedLocations(self, hazardEvent):
        l1 = {"locationName":"High Park Burn Area"}
        l2 = {"locationName":"Mishowaka"}
        l3 = {"locationName":"Buckhorn Mountain"}
        l4 = {"locationName":"Cache La Poudre River"}
        l5 = {"locationName":"South Fork Cache La Poudre River"}
        l6 = {"locationName":"Pendergrass Creek"}
        l7 = {"locationName":"Bennett Creek"}
        l8 = {"locationName":"Gordon Creek"}
        l9 = {"locationName":"Redstone Creek"}
        l10 = {"locationName":"Buckhorn Creek"}
        
        locationList = [l1,l2,l3,l4,l5,l6,l7,l8,l9,l10]
        
        impactedLocations = collections.OrderedDict()
        impactedLocations['location'] = locationList
        return impactedLocations
    
    def observations(self, hazardEvent):
        observation = collections.OrderedDict()
        observation['description'] = 'Heavy Rain'
        observation['time'] = '2012-07-27T19:02:00Z'
        observation['source'] = 'Radar'
        
        location = collections.OrderedDict()
        point = {"latitude":"40.47", "longitude":"-105.52"}
        location['point'] = point
        observation['location'] = location
        
        observations = collections.OrderedDict()
        observations['observation'] = observation
        return observations
    
    def callsToAction(self, hazardEvent):
        cta1 = 'Excessive runoff from this storm will cause flash flooding of creeks '
        cta1 += 'and streams, roads, and roadside culverts. The heavy rains could '
        cta1 += 'also trigger rock slides or debris flows in the high park burn area.'
        cta2 = 'To report any flooding to the nearest law enforcement agency relay '
        cta2 += 'your report to the national weather service forecast office in '
        cta2 += 'denver.'
        
        ctaList = [cta1, cta2]    
        callsToAction = collections.OrderedDict()
        callsToAction['callToAction'] = ctaList

        return callsToAction
    
    def polygonText(self, hazardEvent):
        return 'Lat...Lon 4067 10552 4072 10532 4060 10531 4058 10552'