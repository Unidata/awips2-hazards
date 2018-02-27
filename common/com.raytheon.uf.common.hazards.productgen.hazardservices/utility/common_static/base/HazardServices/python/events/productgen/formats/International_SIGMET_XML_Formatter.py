import FormatTemplate
from xml.etree.ElementTree import Element, SubElement, tostring
from xml.dom import minidom
import os, collections, datetime, dateutil.parser
from TextProductCommon import TextProductCommon

import types, re, sys
from KeyInfo import KeyInfo
from com.raytheon.uf.common.hazards.productgen import ProductUtils
import Legacy_Hydro_Formatter
from collections import OrderedDict
import Domains
import AviationUtils

OUTPUTDIR = AviationUtils.AviationUtils().outputInternationalSigmetFilePath()

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self):
        super(Format, self).initialize()
        self.initProductPartMethodMapping()
        
        self._productGeneratorName = 'International_SIGMET_ProductGenerator'
        self._domains = Domains.Domains()
        
    def initProductPartMethodMapping(self):
        self.productPartMethodMapping = {
            'wmoHeader': self._wmoHeader,
            'ugcHeader': self._ugcHeader,
            'easMessage': self._easMessage,
            'productHeader': self._productHeader,
            'narrativeForecastInformation': self._narrativeForecastInformation
                                }

    def execute(self, productDict, editableEntries=None, overrideProductText=None):    
        self.productDict = productDict
        self._editableParts = OrderedDict()
        domains = Domains.AviationDomains
        
        parts = self.productDict.get('productParts') 
        eventDicts = self.productDict.get('eventDicts')        

        self._rootPrefix = 'iwxxm:SIGMET'
        self._prefix = 'iwxxm:'
        
        for eventDict in eventDicts:
            eventDictParts = eventDict.get('parts')
            self._cancellation = eventDictParts.get('cancellation')
            self._phenomenon = eventDictParts.get('phenomenon', 'NULL')
            
            xmlParts = self.getXMLParts()                       
            
            self._issueFlag = eventDict.get('issueFlag',False)
            self._status = eventDict.get('status',"PENDING")
            self._formatConvectiveSigmet(eventDict, domains)
            xml = self._createXML(eventDict, xmlParts, domains)
            xmlString = str(xml)
            #xmlString = tostring(xml, 'utf-8')
            #xmlOut = self.prettify(xmlString)
            #xmlString = str(xmlOut)

            if self._status in ["PENDING","ISSUED"]:
                if self._issueFlag == "True":
                    self._outputXML(xmlString)
        return [ProductUtils.wrapLegacy(xmlString)],self._editableParts
    
    def getXMLParts(self):
        if self._cancellation == True:
            xmlParts = ['issuingAirTrafficServicesUnit', 'originatingMeteorologicalWatchOffice',
                        'sequenceNumber', 'validPeriod', 'cancelledSequenceNumber',
                        'cancelledValidPeriod', 'phenomenon', 'analysis']
        else:
            if self._phenomenon == 'TC':
                xmlParts = ['issuingAirTrafficServicesUnit', 'originatingMeteorologicalWatchOffice', 'sequenceNumber',
                            'validPeriod', 'phenomenon','analysis', 'forecastPositionAnalysisTC', 'tropicalCyclone']                
            elif self._phenomenon == 'VA ERUPTION':
                xmlParts = ['issuingAirTrafficServicesUnit', 'originatingMeteorologicalWatchOffice', 'sequenceNumber',
                            'validPeriod', 'phenomenon', 'analysis', 'forecastPositionAnalysisVolcano', 'eruptingVolcano']                    
            else:
                xmlParts = ['issuingAirTrafficServicesUnit', 'originatingMeteorologicalWatchOffice', 'sequenceNumber',
                            'validPeriod', 'phenomenon', 'analysis'] 
        
        return xmlParts
    
    def _formatConvectiveSigmet(self, eventDict, domains):                
        eventDictParts = eventDict.get('parts')
        
        self._sequenceName = eventDictParts.get('sequenceName', None)
        self._fullStationID = eventDictParts.get('originatingOffice','NULL')
        self._firAbbreviation = eventDictParts.get('firAbbreviation', 'NULL')
        self._sequenceNumber = eventDictParts.get('sequenceNumber','NULL')
        self._startTime = eventDictParts.get('startTime','DDHHMM')
        self._endTime = eventDictParts.get('endTime','DDHHMM')      
        self._startDate = eventDictParts.get('startDate','YYYYMMDD_HHmm')               
        self._endDate = eventDictParts.get('endDate','YYYYMMDD_HHmm')        
        self._hazardMotionStr = eventDictParts.get('motion','MOV LTL.')
        self._timePeriod = 'tp-'+self._startDate[:8]+'T'+self._startTime[2:]+ \
                           'Z-'+self._endDate[:8]+'T'+self._endTime[2:]
                           
        self._beginPositionText = self._startDate[:4]+'-'+self._startDate[4:6]+'-'+ \
                                  self._startTime[:2]+'T'+self._startTime[2:4]+':'+ \
                                  self._startTime[4:6]+':00Z'
                                  
        self._endPositionText = self._endDate[:4]+'-'+self._endDate[4:6]+'-'+ \
                                self._endTime[:2]+'T'+self._endTime[2:4]+':'+ \
                                self._endTime[4:6]+':00Z'        
           
        self._vertices = eventDictParts.get('geometry',None)
        
    def _makeElement(self, root, key, addPrefix=True):
        return SubElement(root, self._prefix+key)         
    
    def prettify(self, xmlString):
        reparsed = minidom.parseString(xmlString)
        xmlString = reparsed.toprettyxml(indent='    ')        
        
        return xmlString
        
    def _createXML(self, eventDict, xmlParts, domains):
        xml = self.makeRootElement()
        
        for part in xmlParts:
            exec "self."+part+"(xml, eventDict, domains)"
        self.flush()
        
        return xml
    
    def makeRootElement(self):     
        xml = Element(self._rootPrefix)
                
        xml.set('xsi:schemaLocation', \
                "http://icao.int/iwxxm/2.1 http://schemas.wmo.int/iwxxm/2.1/iwxxm.xsd http://def.wmo.int/metce/2013 http://schemas.wmo.int/metce/1.2/metce.xsd http://www.opengis.net/samplingSpatial/2.0 http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd")
        xml.set('gml:id','sigmet-'+self._firAbbreviation+'-'+self._firAbbreviation+'-'+self._startDate[:7]+self._startDate[9:]+'Z') #gml:id="sigmet-YUCC-20120825160000Z"
        xml.set("permissibleUsage", "OPERATIONAL")
        if self._cancellation == True:
            xml.set('status', "CANCELLATION")
        else:
            xml.set('status',"NORMAL")
        
        return xml
        
    def issuingAirTrafficServicesUnit(self, xml, eventDict, domains):        
        key = 'issuingAirTrafficServicesUnit'
        issuingAirTrafficServicesUnit = self._makeElement(xml, key)
        
        atsUnit = SubElement(issuingAirTrafficServicesUnit, 'aixm:Unit')
        atsUnit.set('gml:id', 'fic-'+self._firAbbreviation)
        
        atsTimeSlice = SubElement(atsUnit, 'aixm:timeSlice')
        
        atsUnitTimeSlice = SubElement(atsTimeSlice, 'aixm:UnitTimeSlice')
        atsUnitTimeSlice.set('gml:id', 'fic-'+self._firAbbreviation+'-ts')
        
        atsValidTime = SubElement(atsUnitTimeSlice, 'gml:validTime/')
        
        atsInterpretation = SubElement(atsUnitTimeSlice, 'aixm:interpretation')
        atsInterpretation.text = 'SNAPSHOT'
        
        atsName = SubElement(atsUnitTimeSlice, 'aixm:name')
        atsName.text = self._firAbbreviation + ' FIC'
        
        atsType = SubElement(atsUnitTimeSlice, 'aixm:type')
        atsType.text = 'FIC'
        
        atsDesignator = SubElement(atsUnitTimeSlice, 'aixm:designator')
        atsDesignator.text = self._firAbbreviation
        
    def originatingMeteorologicalWatchOffice(self, xml, eventDict, domains):
        key = 'originatingMeteorologicalWatchOffice'
        originatingMeteorologicalWatchOffice = self._makeElement(xml, key)
        
        mwoUnit = SubElement(originatingMeteorologicalWatchOffice, 'aixm:Unit')
        mwoUnit.set('gml:id', 'wmo-'+self._fullStationID)
        
        mwoTimeSlice = SubElement(mwoUnit, 'aixm:timeSlice')
        
        mwoUnitTimeSlice = SubElement(mwoTimeSlice, 'aixm:UnitTimeSlice')
        mwoUnitTimeSlice.set('gml:id', 'mwo-'+self._fullStationID+'-ts')
        
        mwoValidTime = SubElement(mwoUnitTimeSlice, 'gml:validTime/')
        
        mwoInterpretation = SubElement(mwoUnitTimeSlice, 'aixm:interpretation')
        mwoInterpretation.text = 'SNAPSHOT'
        
        mwoName = SubElement(mwoUnit, 'aixm:name')
        mwoName.text = self._fullStationID+' MWO'
        
        mwoType = SubElement(mwoUnit, 'aixm:type')
        mwoType.text = 'MWO'
        
        mwoDesignator = SubElement(mwoUnit, 'aixm:designator')
        mwoDesignator.text = self._fullStationID
                
    def sequenceNumber(self, xml, eventDict, domains):
        key = 'sequenceNumber'
        
        sequenceNumber = self._makeElement(xml, key)
        sequenceNumber.text = str(self._sequenceNumber)
                
    def validPeriod(self, xml, eventDict, domains):           
        key = 'validPeriod'
        validPeriod = self._makeElement(xml, key)
        
        timePeriod = SubElement(validPeriod, 'gml:TimePeriod')
        timePeriod.set('gml:id', self._timePeriod)
        
        beginPosition = SubElement(timePeriod, 'gml:beginPosition')
        beginPosition.text = self._beginPositionText
        
        endPosition = SubElement(timePeriod, 'gml:endPosition')
        endPosition.text = self._endPositionText
        
    def cancelledSequenceNumber(self, xml, eventDict, domains):
        key = 'cancelledSequenceNumber'
        
        cancelledSequenceNumber = self._makeElement(xml, key)
        cancelledSequenceNumber.text = str(self._sequenceNumber - 1)
        
    def cancelledValidPeriod(self, xml, eventDict, domains):
        import GenericRegistryObjectDataAccess
        import time
        
        objectDicts = GenericRegistryObjectDataAccess.queryObjects(
            [("objectType", "InternationalSIGMET"),
             ("uniqueID", self._sequenceName)],
            True)

        key = 'canacelledValidPeriod'
        cancelledValidPeriod = self._makeElement(xml, key)
        
        print "objectDicts: ", objectDicts
        
        if objectDicts:
            eventStartTime = objectDicts[0]['eventStartTime']
            eventEndTime = objectDicts[0]['eventEndTime']
            
            startTimeStr = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(eventStartTime))
            endTimeStr = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(eventEndTime))            

            beginPositionStr = startTimeStr[:10] + 'T' + startTimeStr[11:] + 'Z' 
            endPositionStr = endTimeStr[:10] + 'T' + endTimeStr[11:] + 'Z'
            
            timePeriod = SubElement(cancelledValidPeriod, 'gml:TimePeriod')
            beginPosition = SubElement(timePeriod, 'gml:beginPosition')
            beginPosition.text = beginPositionStr
            endPosition = SubElement(timePeriod, 'gml:endPosition')
            endPosition.text = endPositionStr     
                
    def phenomenon(self, xml, eventDict, domains):
        key = 'phenomenon'      
        
        phenomenonDict = {'OBSC TS': "http://codes.wmo.int/49-2/SigWxPhenomena/_OBSC_TS",
                          'EMBD TS': "http://codes.wmo.int/49-2/SigWxPhenomena/_EMBD_TS",
                          'FRQ TS': "http://codes.wmo.int/49-2/SigWxPhenomena/_FRQ_TS",
                          'SQL TS': "http://codes.wmo.int/49-2/SigWxPhenomena/_SQL_TS",
                          'WDSPR TS': "NO ENTRY",
                          'ISOL SEV TS': "NO ENTRY",
                          'SEV TURB': "http://codes.wmo.int/49-2/SigWxPhenomena/_SEV_TURB",
                          'SEV ICE': "http://codes.wmo.int/49-2/SigWxPhenomena/_SEV_ICE",
                          'SEV ICE FZRA': "http://codes.wmo.int/49-2/SigWxPhenomena/_SEV_ICE_FZRA",
                          'SEV DS': "http://codes.wmo.int/49-2/SigWxPhenomena/_HVY_DS",
                          'SEV SS': "http://codes.wmo.int/49-2/SigWxPhenomena/_HVY_SS",
                          'RDOACT RELEASE': "NO ENTRY",
                          'SEV MTW': "http://codes.wmo.int/49-2/SigWxPhenomena/_SEV_MTW",
                          'TC': "http://codes.wmo.int/49-2/SigWxPhenomena/_TC",
                          'VA ERUPTION': "http://codes.wmo.int/49-2/SigWxPhenomena/_VA",
                          }
        
        phenomLink = phenomenonDict[self._phenomenon]
        
        phenomenon = self._makeElement(xml, key)
        phenomenon.set('xlink:href', phenomLink)
        
    def forecastPositionAnalysisTC(self, xml, eventDict, domains):
        eventDictParts = eventDict.get('parts')
        tropicalCycloneProductPartsDict = eventDictParts.get('tropicalCycloneProductPartsDict')
        
        key = 'forecastPositionAnalysis'
        forecastPositionAnalysis = self._makeElement(xml, key)
        
        omObservation = SubElement(forecastPositionAnalysis, 'om:OM_Observation')
        omObservation.set('gml:id', "analysis-"+self._startDate[:8]+'T'+self._startTime[2:]+ 'Z2')
        
        omType = SubElement(omObservation, "om:type")
        omType.set('xlink:href',"http://codes.wmo.int/49-2/observation-type/iwxxm/2.1/SIGMETPositionAnalysis")
        
        omPhenomenonTime = SubElement(omObservation, "om:phenomenonTime")
        timeInstant = SubElement(omPhenomenonTime, "gml:TimeInstant")
        timeInstant.set('gml:id', "ti-"+self._startDate[:8]+'T'+self._startTime[2:]+ 'Z2')
        timePosition = SubElement(timeInstant, "gml:timePosition")
        timePosition.text = self._startDate[:4] + '-' + self._startDate[4:6] + '-' + self._startDate[6:] + \
                            'T' + self._startTime[:2] + ':' + self._startTime[2:4] + ":" + self._startTime[4:] + 'Z'
                            
        omResultTime = SubElement(omObservation, "om:resultTime")
        timeInstant = SubElement(omPhenomenonTime, "gml:TimeInstant")
        timeInstant.set('gml:id', "ti-"+self._startDate[:8]+'T'+self._startTime[2:]+ 'Z3')
        timePosition = SubElement(timeInstant, "gml:timePosition")
        timePosition.text = self._startDate[:4] + '-' + self._startDate[4:6] + '-' + self._startDate[6:] + \
                            'T' + self._startTime[:2] + ':' + self._startTime[2:4] + ":" + self._startTime[4:] + 'Z'
                            
        omValidTime = SubElement(omObservation, "om:validTime")
        omValidTime.set('xlink:href', "#tp" + self._startDate[:8] + 'T' + self._startTime[2:] + 'Z' + self._endDate[:8] + 'T' + self._endTime[2:] + 'Z')
        
        omProcedure = SubElement(omObservation, "om:procedure")
        omProcedure.set('xlink:href', "#p-49-2-sigmet")
        
        omObservedProperty = SubElement(omObservation, "om:observedProperty")
        omObservedProperty.set('xlink:href', "http://codes.wmo.int/49-2/observable-property/sigmet/positionAnalysis")
        
        omFeatureOfInterest = SubElement(omObservation, "om:featureOfInterest")
        omFeatureOfInterest.set('xlink:href', "#sampling-surface-Amswell")
        
        omResult = SubElement(omObservation, "om:result")
        positionCollection = SubElement(omResult, "iwxxm:SIGMETPositionCollection")
        positionCollection.set('gml:id', "position-collection-result-2")
        
        member = SubElement(positionCollection, "iwxxm:member")
        position = SubElement(member, "iwxxm:SIGMETPosition")
        position.set('gml:id', "sigmet-fcst-N2706")
        
        geometry = SubElement(position, "iwxxm:geometry")
        airspaceVolume = SubElement(geometry, 'aixm:AirspaceVolume')
        airspaceVolume.set('gml:id', "as2")
        
        horizontalProjection = SubElement(airspaceVolume, "aixm:horizontalProjection")
        surface = SubElement(horizontalProjection, "aixm:Surface")
        surface.set('gml:id', "sfc002")
        surface.set('srsName', "http://www.opengis.net/def/crs/EPSG/0/4326")
        
        polygonPatches = SubElement(surface, "gml:polygonPatches")
        polygonPatch = SubElement(polygonPatches, "gml:PolygonPatch")
        exterior = SubElement(polygonPatch, "gml:exterior")
        ring = SubElement(exterior, "gml:Ring")
        curveMember = SubElement(ring, "gml:curveMember")
        curve = SubElement(curveMember, "gml:Curve")
        curve.set('gml:id', "curve001")
        
        segments = SubElement(curve, 'gml:segments')
        circleByCenter = SubElement(segments, 'gml:CircleByCenterPoint')
        circleByCenter.set('numArc', "1")
        
        pos = SubElement(circleByCenter, "gml:pos")
        pos.text = tropicalCycloneProductPartsDict['centerLocation'][3:-1]
        
        radius = SubElement(circleByCenter, "gml:radius")
        radius.set('uom', "[nmi_i]")
        radius.text = 0
        
        
    def forecastPositionAnalysisVolcano(self, xml, eventDict, domains):
        eventDictParts = eventDict.get('parts')
        volcanoProductPartsDict = eventDictParts.get('volcanoProductPartsDict')        
        
        key = 'forecastPositionAnalysis'
        forecastPositionAnalysis = self._makeElement(xml, key)
        
        omObservation = SubElement(forecastPositionAnalysis, 'om:OM_Observation')
        omObservation.set('gml:id', "va-forecast-position-" + eventDictParts.get('originatingOffice') + '-' + self._startDate[:8]+'T'+self._startTime[2:]+ 'Z')
        
        omType = SubElement(omObservation, "om:type")
        omType.set('xlink:href',"http://codes.wmo.int/49-2/observation-type/iwxxm/2.1/SIGMETPositionAnalysis")
        
        omPhenomenonTime = SubElement(omObservation, "om:phenomenonTime")
        timeInstant = SubElement(omPhenomenonTime, "gml:TimeInstant")
        timeInstant.set('gml:id', "ti-"+self._startDate[:8]+'T'+self._startTime[2:]+ 'Z')
        timePosition = SubElement(timeInstant, "gml:timePosition")
        timePosition.text = self._startDate[:4] + '-' + self._startDate[4:6] + '-' + self._startDate[6:] + \
                            'T' + self._startTime[:2] + ':' + self._startTime[2:4] + ":" + self._startTime[4:] + 'Z'
                   
        omResultTime = SubElement(omObservation, "om:resultTime")
        timeInstant = SubElement(omPhenomenonTime, "gml:TimeInstant")
        timeInstant.set('gml:id', "ti-"+self._startDate[:8]+'T'+self._startTime[2:]+ 'Z3')
        timePosition = SubElement(timeInstant, "gml:timePosition")
        timePosition.text = self._startDate[:4] + '-' + self._startDate[4:6] + '-' + self._startDate[6:] + \
                            'T' + self._startTime[:2] + ':' + self._startTime[2:4] + ":" + self._startTime[4:] + 'Z'
                            
        omValidTime = SubElement(omObservation, "om:validTime")
        omValidTime.set('xlink:href', "#tp" + self._startDate[:8] + 'T' + self._startTime[2:] + 'Z' + self._endDate[:8] + 'T' + self._endTime[2:] + 'Z')
        
        omProcedure = SubElement(omObservation, "om:procedure")
        omProcedure.set('xlink:href', "http://codes.wmo.int/49-2/observation-type/IWXXM/1.0/SIGMETPositionAnalysis")
        
        omObservedProperty = SubElement(omObservation, "om:observedProperty")
        omObservedProperty.set('xlink:href', "http://codes.wmo.int/49-2/SigWxPhenomena/VA")
        
        omFeatureOfInterest = SubElement(omObservation, "om:featureOfInterest")
        omFeatureOfInterest.set('xlink:href', "#sampling-surface-Amswell")
        
        omResult = SubElement(omObservation, "om:result")
        positionCollection = SubElement(omResult, "iwxxm:SIGMETPositionCollection")
        positionCollection.set('gml:id', "mpc-" + eventDictParts.get('originatingOffice') + '-' + self._startDate[:8]+'T'+self._startTime[2:]+ 'Z')
        
        member = SubElement(positionCollection, "iwxxm:member")
        position = SubElement(member, "iwxxm:SIGMETPosition")
        position.set('gml:id', "mp-va-fcst-" + eventDictParts.get('originatingOffice') + '-' + self._startDate[:8]+'T'+self._startTime[2:]+ 'Z')
        
        geometry = SubElement(position, "iwxxm:geometry")
        airspaceVolume = SubElement(geometry, 'aixm:AirspaceVolume')
        airspaceVolume.set('gml:id', "av-va-fcst-position-" + eventDictParts.get('originatingOffice') + '-' + self._startDate[:8]+'T'+self._startTime[2:]+ 'Z')
        
        horizontalProjection = SubElement(airspaceVolume, "aixm:horizontalProjection")
        surface = SubElement(horizontalProjection, "aixm:Surface")
        surface.set('gml:id', "polygon-va-fcst-position-" + eventDictParts.get('originatingOffice') + '-' + self._startDate[:8]+'T'+self._startTime[2:]+ 'Z')
        surface.set('uomLabels', "deg deg")
        surface.set('axisLabels', "Lat Lon")
        surface.set('srsDimension', "2")
        surface.set('srsName', "http://www.opengis.net/def/crs/EPSG/0/4326")
        
        polygonPatches = SubElement(surface, "gml:polygonPatches")
        polygonPatch = SubElement(polygonPatches, "gml:PolygonPatch")
        exterior = SubElement(polygonPatch, "gml:exterior")
        linearRing = SubElement(exterior, "gml:linearRing")
        
        posList = SubElement(linearRing, "gml:posList")
        posList.text = eventDictParts.get('location')[3:]             
        
    def tropicalCyclone(self, xml, eventDict, domains):
        eventDictParts = eventDict.get('parts')
        tropicalCycloneProductPartsDict = eventDictParts.get('tropicalCycloneProductPartsDict')
        
        key = 'tropicalCyclone'
        tropicalCyclone = self._makeElement(xml, key)
        
        tropicalCycloneMetce = SubElement(tropicalCyclone, 'metce:TropicalCyclone')
        tropicalCycloneMetce.set('gml:id',"TC-"+tropicalCycloneProductPartsDict['name'])
        
        tropicalCycloneName = SubElement(tropicalCycloneMetce, 'metce:name')
        tropicalCycloneName.text = tropicalCycloneProductPartsDict['name'] 
        
    def eruptingVolcano(self, xml, eventDict, domains):
        eventDictParts = eventDict.get('parts')
        volcanoProductPartsDict = eventDictParts.get('volcanoProductPartsDict')
        
        key = 'eruptingVolcano'
        eruptingVolcano = self._makeElement(xml, key)
        
        volcanoMetce = SubElement(eruptingVolcano, 'metce:Volcano')
        volcanoMetce.set('gml:id',"v-"+volcanoProductPartsDict['simpleName'])
        
        volcanoName = SubElement(volcanoMetce, 'metce:name')
        volcanoName.text = volcanoProductPartsDict['simpleName']
        
        volcanoPosition = SubElement(volcanoMetce, 'metce:position')
        
        volcanoPoint = SubElement(volcanoPosition, 'gml:Point')
        volcanoPoint.set('gml:id', "ref-point-"+volcanoProductPartsDict['simpleName'])
        volcanoPoint.set('uomLabels', "deg deg")
        volcanoPoint.set('axisLabels', "Lat Lon")
        volcanoPoint.set('srsDimension', "2")
        volcanoPoint.set('srsName', "http://www.opengis.net/def/crs/EPSG/0/4326")
        
        volcanoPos = SubElement(volcanoPoint, 'gml:pos')
        volcanoPos.text = volcanoProductPartsDict['simplePosition']
        
    def analysis(self, xml, eventDict, domains):
        key = 'analysis'
        analysis = self._makeElement(xml, key)

        #Load SubElements for analysis section
        omObservation = self._omObservation(analysis)
        self._omType(omObservation)
        self._phenomenonTime(omObservation)
        omResultTime = self._omResultTime(omObservation)
        timeInstant = self._timeInstant(omResultTime)
        self._timePosition(timeInstant)
        self._validTime(omObservation)
        procedure = self._procedure(omObservation)
        process = self._process(procedure)
        self._processDescription(process)
        self._observedProperty(omObservation)
        self._featureOfInterest(omObservation, eventDict)
        result = self._result(omObservation)
        
        if self._cancellation == True:
            pass
        else:
            evolvingMetCondition = self._evolvingMetCondition(result, eventDict)      
            geometry = self._geometry(evolvingMetCondition)
            airspaceVolume = self._airspaceVolume(geometry)
            self._upperLimit(airspaceVolume, eventDict)
            self._upperLimitReference(airspaceVolume)
            horizontalProjection = self._horizontalProjection(airspaceVolume)        
    #===================================================================================
    #END OF MAIN XML TAGS
    #-----------------------------------------------------------------------------------
    #BEGINNING OF SUB METHODS FOR ANALYSIS TAG
    #===================================================================================       
        
    def _omObservation(self, parent):
        omObservationText = 'analysis-' + self._startDate[:8] + 'T' + self._startTime[2:] + 'Z'
        
        omObservation = SubElement(parent, 'om:OM_Observation')
        omObservation.set('gml:id', omObservationText)                
        
        return omObservation
        
    def _omType(self, parent):
        omType = SubElement(parent, 'om:type')
        omType.set('xlink:href', "http://codes.wmo.int/49-2/observation-type/iwxxm/2.1/SIGMETEvolvingConditionAnalysis")        
        
        return omType
        
    def _omResultTime(self, parent):
        omResultTime = SubElement(parent, 'om:resultTime')        
        
        return omResultTime
        
    def _timeInstant(self, parent):
        self._timeInstantText = 'ti-'+self._startTime[:2]+'T'+self._startTime[2:]+'Z'        
        
        timeInstant = SubElement(parent, 'gml:TimeInstant')
        timeInstant.set('gml:id', self._timeInstantText)        
        
        return timeInstant
        
    def _timePosition(self, parent):
        timePosition = SubElement(parent, 'gml:timePosition')
        timePosition.text = self._beginPositionText         
        
    def _phenomenonTime(self, parent):
        self._timeInstantText = 'ti-'+self._startTime[:2]+'T'+self._startTime[2:]+'Z'
        
        phenomenonTime = SubElement(parent, 'om:phenomenonTime')
        phenomenonTime.set('xlink:href', '#'+self._timeInstantText)        
        
    def _validTime(self, parent):
        validTimeText = '#tp-'+self._startTime[:2]+'T'+self._startTime[2:]+'Z-'+self._endTime[:2]+'T'+self._endTime[2:]+'Z'
                
        validTime = SubElement(parent, 'om:validTime')
        validTime.set('xlink:href', validTimeText)
                
    def _procedure(self, parent):
        procedure = SubElement(parent, 'om:procedure')        
        
        return procedure
        
    def _process(self, parent):
        process = SubElement(parent, 'metce:Process')
        process.set('gml:id', "p-49-2-SIGMET")        
        
        return process
        
    def _processDescription(self, parent):
        processDescription = SubElement(parent, 'gml:description')
        processDescription.text = "WMO No. 49 Volume 2 Meteorological Service for International Air Navigation APPENDIX 6-1 TECHNICAL SPECIFICATIONS RELATED TO SIGMET INFORMATION"        
        
    def _observedProperty(self, parent):
        observedProperty = SubElement(parent, 'om:observedProperty')
        observedProperty.set('xlink:href', "http://codes.wmo.int/49-2/observable-property/SIGMETEvolvingConditionAnalysis")        
        
    def _featureOfInterest(self, parent, eventDict):
        eventDictParts = eventDict.get('parts')
        
        featureOfInterest = SubElement(parent, 'om:featureOfInterest')
        spatialSampling = SubElement(featureOfInterest, 'sams:SF_SpatialSamplingFeature')
        spatialSampling.set('gml:id', "sampling-surface-" + eventDictParts.get('firAbbreviation'))
        
        type = SubElement(spatialSampling, 'sf:type')
        type.set('xlink:href', "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface")
        
        sampledFeature = SubElement(spatialSampling, 'sf:sampledFeature')
        airspace = SubElement(sampledFeature, 'aixm:Airspace')
        airspace.set('gml:id', "fir-" + eventDictParts.get('originatingOffice'))
        
        timeSlice = SubElement(airspace, 'aixm:timeSlice')
        airspaceTimeSlice = SubElement(timeSlice, 'aixm:AirspaceTimeSlice')
        airspaceTimeSlice.set('gml:id', "fir-" + eventDictParts.get('originatingOffice') + "-ts")
        
        validTime = SubElement(airspaceTimeSlice, 'gml:validTime')
        interpretation = SubElement(airspaceTimeSlice, 'aixm:interpretation')
        interpretation.text = "SNAPSHOT"
        airspaceType = SubElement(airspaceTimeSlice, 'aixm:type')
        airspaceType.text = "OTHER:FIR_UIR"
        designator = SubElement(airspaceTimeSlice, 'aixm:designator')
        designator.text = eventDictParts.get('originatingOffice')
        name = SubElement(airspaceTimeSlice, 'aixm:name')
        name.text = eventDictParts.get('firAbbreviation') + "FIR/UIR"
        
        shape = SubElement(spatialSampling, 'sams:shape')
        shape.set('nilReason', "witheld")        
        
    def _result(self, parent):
        result = SubElement(parent, 'om:result')
        
        if self._cancellation == True:
            result.set('nilReason', "inapplicable")        
        
        return result
        
    def _evolvingMetCondition(self, parent, eventDict):
        eventDictParts = eventDict.get('parts')
        
        intensityChangeDict = {'DVLPG ': 'INTENSIFY', 'INTSF ': 'INTENSIFY', 'DMSHG ': 'WEAKEN', '': 'NO_CHANGE'}        
        
        evolvingMetCondition = SubElement(parent, 'iwxxm:SIGMETEvolvingConditionCollection')
        evolvingMetCondition.set('gml:id', "fcst1")
        evolvingMetCondition.set('timeIndicator', "FORECAST")
        
        member = SubElement(evolvingMetCondition, 'iwxxm:member')
        evolvingCondition = SubElement(member, 'iwxxm:SIGMETEvolvingCondition')
        evolvingCondition.set('gml:id','sec1')
        evolvingCondition.set('intensityChange', eventDictParts.get('intensityTrend'))
        
        directionOfMotion = SubElement(evolvingCondition, 'iwxxm:directionOfMotion')
        directionOfMotion.set('uom', "deg")
        directionOfMotion.text = self._hazardMotionStr[8:12]
        
        return evolvingMetCondition
        
    def _geometry(self, parent):
        geometry = SubElement(parent, 'iwxxm:geometry')
        
        return geometry        
        
    def _airspaceVolume(self, parent):     
        airspaceVolume = SubElement(parent, 'aixm:AirspaceVolume')
        airspaceVolume.set('gml:id', "as1")
        
        return airspaceVolume        
        
    def _upperLimit(self, parent, eventDict):
        eventDictParts = eventDict.get('parts')
        
        if self._phenomenon == 'VA ERUPTION':
            volcanoProductPartsDict = eventDictParts.get('volcanoProductPartsDict')
            try:
                cloudStrText = volcanoProductPartsDict['observedLayerTop']
            except KeyError:
                cloudStrText = ''
            
        else:
            cloudStr = eventDictParts.get('verticalExtent')
            if "SFC" in cloudStr:
                cloudStrText = cloudStr[6:-1]
            else:
                cloudStrText = cloudStr[8:-1]       
        
        upperLimit = SubElement(parent, 'aixm:upperLimit')
        upperLimit.set('uom', "FL")
        upperLimit.text = cloudStrText        
        
    def _upperLimitReference(self, parent):
        upperLimitReference = SubElement(parent, 'aixm:upperLimitReference')
        upperLimitReference.text = 'STD'        
        
    def _horizontalProjection(self, parent):
        horizontalProjection = SubElement(parent, 'aixm:horizontalProjection')
        
        surface = SubElement(horizontalProjection, 'aixm:Surface')
        surface.set('gml:id',"sfc1")
        surface.set('uomLabels', "deg deg")
        surface.set('axisLabels', "Lat Lon")
        surface.set('srsDimension', "2")
        surface.set('srsName', "http://www.opengis.net/def/crs/EPSG/0/4326")
        
        polygonPatches = SubElement(surface, 'gml:polygonPatches')
        polygonPatch = SubElement(polygonPatches, 'gml:PolygonPatch')
        exterior = SubElement(polygonPatch, 'gml:exterior')
        linearRing = SubElement(exterior, 'gml:LinearRing')
        posList = SubElement(linearRing, 'gml:posList')
        
        posListText = ''
        count = str(len(self._vertices))
        for vertice in self._vertices:
            posListText = posListText+("{0:.2f}".format(vertice[1]))+' '+("{0:.2f}".format(vertice[0]))+' '
            
        posList.text = posListText         
        
        return horizontalProjection     
    
    def _outputXML(self, xmlString):              
        outDir = os.path.join(OUTPUTDIR, self._startDate)
        outAdvisory = 'internationalSIGMET.xml'
        pathFile = os.path.join(outDir, outAdvisory)
        
        if not os.path.exists(outDir):
            try:
                os.makedirs(outDir)
            except:
                sys.stderr.write('Could not create output directory')
        with open(pathFile, 'w') as outFile:
            outFile.write(xmlString)
        return    