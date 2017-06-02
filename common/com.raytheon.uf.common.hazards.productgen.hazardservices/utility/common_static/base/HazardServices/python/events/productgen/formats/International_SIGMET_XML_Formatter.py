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

OUTPUTDIR = '/scratch/internationalSIGMET' 

###TODO: Put example XML formatter here 

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
        
        xmlParts = ['issuingAirTrafficServicesUnit', 'originatingMeteorologicalWatchOffice', 'sequenceNumber',
                    'validPeriod', 'phenomenon', 'analysis', 'basisForIssuance', 'continuation',
                    'isSpecialIssuance', 'isCorrection']
        self._rootPrefix = 'iwxxm-us:SIGMET'
        self._prefix = 'iwxxm:'
        
        parts = self.productDict.get('productParts') 
        eventDicts = self.productDict.get('eventDicts')
        
        for eventDict in eventDicts:
            self._specialIssuance = eventDict.get('specialIssuance',False)
            self._issueFlag = eventDict.get('issueFlag',False)
            self._status = eventDict.get('status',"PENDING")
            self._formatConvectiveSigmet(eventDict, domains)
            xml = self._createXML(eventDict, xmlParts, domains)
            xmlString = tostring(xml, 'utf-8')
            xmlOut = self.prettify(xmlString)
            xmlString = str(xmlOut)

            if self._status in ["PENDING","ISSUED"]:
                if self._issueFlag == "True":
                    self._outputXML(xmlOut)
        return [ProductUtils.wrapLegacy(xmlString)],self._editableParts
    
    def prettify(self, xmlString):
        reparsed = minidom.parseString(xmlString)
        xmlString = reparsed.toprettyxml(indent='    ')        
        
        return xmlString
        
    def _createXML(self, eventDict, xmlParts, domains):
        xml = self._makeRootElement()
        
        #for part in xmlParts:
        #    exec "self._"+part+"(xml, eventDict, domains)"
        #self.flush()
        
        return xml
    
    def _makeRootElement(self):
        namespaces = {
                      'xlink':'http://www.w3.org/1999/xlink',
                      'xsi':'http://www.w3.org/2001/XMLSchema-instance',
                      'gml':'http://www.opengis.net/gml/3.2',
                      'om':'http://www.opengis.net/om/2.0',
                      'sf':'http://www.opengis.net/sampling/2.0',
                      'sams':'http://www.opengis.net/samplingSpatial/2.0',
                      'metce':'http://def.wmo.int/metce/2013',
                      'saf':'http://icao.int/saf/1.1',
                      'iwxxm':'http://icao.int/iwxxm/1.1',
                      'iwxxm-us':'http://nws.weather.gov/schemas/IWXXM-US/1.0/Release',
                      }
        
        xml = Element(self._rootPrefix)
        
        for name in namespaces:
            xml.set('xmlns:'+name,namespaces[name])
                
        xml.set('xsi:schemaLocation', \
                "http://nws.weather.gov/schemas/IWXXM-US/1.0/Release http://nws.weather.gov/schemas/IWXXM-US/1.0/Release/schemas/usSigmet.xsd")
        xml.set('gml:id','sigmet-conv-'+self._productLoc+self._abbrev+'-'+self._convectiveSigmetNumberStr+
                self._abbrev+'-'+self._startDate[:7]+self._startDate[9:]+'Z')
        xml.set('status','NORMAL')
        
        return xml
        
    def _issuingAirTrafficServicesUnit(self, xml, eventDict, domains):        
        key = 'issuingAirTrafficServicesUnit'
        
        issuingAirTrafficServicesUnit = self._makeElement(xml, key)
        
        atsUnit = SubElement(issuingAirTrafficServicesUnit, 'saf:Unit')
        atsUnit.set('gml:id', 'atsu-'+self._productLoc + self._abbrev)
        
        atsDesignator = SubElement(atsUnit, 'saf:designator')
        atsDesignator.text = self._productLoc + self._abbrev
        
    def _originatingMeteorologicalWatchOffice(self, xml, eventDict, domains):
        key = 'originatingMeteorologicalWatchOffice'
        
        originatingMeteorologicalWatchOffice = self._makeElement(xml, key)
        
        mwoUnit = SubElement(originatingMeteorologicalWatchOffice, 'saf:Unit')
        mwoUnit.set('gml:id', 'mwo-'+self._fullStationID)
        
        mwoName = SubElement(mwoUnit, 'saf:name')
        mwoName.text = self._fullStationID+' MWO'
        
        mwoType = SubElement(mwoUnit, 'saf:type')
        mwoType.text = 'MWO'
        
        mwoDesignator = SubElement(mwoUnit, 'saf:designator')
        mwoDesignator.text = self._fullStationID
                
    def _sequenceNumber(self, xml, eventDict, domains):
        key = 'sequenceNumber'
        
        sequenceNumber = self._makeElement(xml, key)
        sequenceNumber.text = self._convectiveSigmetNumberStr + self._abbrev
                
    def _validPeriod(self, xml, eventDict, domains):
        endPositionText = self._endDate[:4]+'-'+self._endDate[4:6]+'-'+self._endTime[:2]+'T'+self._endTime[2:4]+':'+self._endTime[4:6]+':00Z'           
        key = 'validPeriod'
        
        validPeriod = self._makeElement(xml, key)
        
        timePeriod = SubElement(validPeriod, 'gml:TimePeriod')
        timePeriod.set('gml:id', self._timePeriod)
        
        beginPosition = SubElement(timePeriod, 'gml:beginPosition')
        beginPosition.text = self._beginPositionText
        
        endPosition = SubElement(timePeriod, 'gml:endPosition')
        endPosition.text = endPositionText
                
    def _phenomenon(self, xml, eventDict, domains):
        key = 'phenomenon'
        phenomLink = "http://nws.weather.gov/codes/NWSI10-811/2011/USAeronauticalSignificantWeatherPhenomenon/CONVECTIVE_SIGMET"
        
        phenomenon = self._makeElement(xml, key)
        phenomenon.set('xlink:href', phenomLink)
                
    def _analysis(self, xml, eventDict, domains):
        key = 'analysis'
        analysis = self._makeElement(xml, key)

        #Load SubElements for analysis section
        omObservation = self._omObservation(analysis)
        self._omType(omObservation)
        omResultTime = self._omResultTime(omObservation)
        timeInstant = self._timeInstant(omResultTime)
        self._timePosition(timeInstant)
        self._phenomenonTime(omObservation)
        self._validTime(omObservation)
        procedure = self._procedure(omObservation)
        process = self._process(procedure)
        self._processDescription(process)
        self._observedProperty(omObservation)
        self._featureOfInterest(omObservation)
        result = self._result(omObservation)
        evolvingMetCondition = self._evolvingMetCondition(result)
        
        if self._hazardMotionStr != 'MOV LTL.':
            self._directionOfMotion(evolvingMetCondition)
                    
        geometry = self._geometry(evolvingMetCondition)
        airspaceVolume = self._airspaceVolume(geometry)
        self._upperLimit(airspaceVolume)
        self._upperLimitReference(airspaceVolume)
        horizontalProjection = self._horizontalProjection(airspaceVolume)
        polygon = self._polygon(horizontalProjection)
        exterior = self._exterior(polygon)
        linearRing = self._linearRing(exterior)
        self._posList(linearRing)
        self._speedOfMotion(evolvingMetCondition)
        self._convectionGeometry(result)
        self._convectionHeightForecast(result)
        
    def _omObservation(self, parent):
        omObservationText = 'a1-' + self._startTime[:2] + 'T' + self._startTime[2:] + 'Z'
        
        omObservation = SubElement(parent, 'om:OM_Observation')
        omObservation.set('gml:id', omObservationText)                
        
        return omObservation
        
    def _omType(self, parent):
        omType = SubElement(parent, 'om:type')
        omType.set('xlink:href', "http://codes.wmo.int/49-2/observation-type/IWXXM/1.1/SIGMETEvolvingConditionAnalysis")        
        
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
        process.set('gml:id', "process-wmo-49-2-SIGMET")        
        
        return process
        
    def _processDescription(self, parent):
        processDescription = SubElement(parent, 'gml:description')
        processDescription.text = "WMO Doc 49-2 Appendix 6.1: SIGMET"        
        
    def _observedProperty(self, parent):
        observedProperty = SubElement(parent, 'om:observedProperty')
        observedProperty.set('xlink:href', "http://codes.wmo.int/49-2/observable-property/SIGMETEvolvingConditionAnalysis")        
        
    def _featureOfInterest(self, parent):
        featureOfInterest = SubElement(parent, 'om:featureOfInterest')
        featureOfInterest.set('xlink:href', "#sampling-surface")        
        
    def _result(self, parent):
        result = SubElement(parent, 'om:result')        
        
        return result
        
    def _evolvingMetCondition(self, parent):
        intensityChangeDict = {'DVLPG ': 'INTENSIFY', 'INTSF ': 'INTENSIFY', 'DMSHG ': 'WEAKEN', '': 'NO_CHANGE'}        
        
        evolvingMetCondition = SubElement(parent, 'iwxxm:EvolvingMeteorologicalCondition')
        evolvingMetCondition.set('gml:id', "emc-conv-a1")
        evolvingMetCondition.set('intensityChange', intensityChangeDict[self._convectiveSigmetModifierStr])
        
        return evolvingMetCondition
    
    def _directionOfMotion(self, parent):
        directionOfMotion = SubElement(parent, 'iwxxm:directionOfMotion')
        directionOfMotion.set('uom',"deg")
        directionOfMotion.text = self._hazardMotionStr[8:12]        
        
    def _geometry(self, parent):
        geometry = SubElement(parent, 'iwxxm:geometry')
        
        return geometry        
        
    def _airspaceVolume(self, parent):
        airspaceVolumeText = 'av-conv-obs-pos-'+self._productLoc+self._abbrev+'-'+ \
                              self._convectiveSigmetNumberStr+self._abbrev+'-'+ \
                              self._startDate[:8]+self._startDate[9:]+'Z-a1'        
        
        airspaceVolume = SubElement(parent, 'saf:AirspaceVolume')
        airspaceVolume.set('gml:id', airspaceVolumeText)
        
        return airspaceVolume        
        
    def _upperLimit(self, parent):
        if self._convectiveSigmetCloudTopStr == 'TOPS ABV FL450.':
            self._upperLimitText = '450'
        else:
            self._upperLimitText = self._convectiveSigmetCloudTopStr[10:-1]        
        
        upperLimit = SubElement(parent, 'saf:upperLimit')
        upperLimit.set('uom', "FL")
        upperLimit.text = self._upperLimitText        
        
    def _upperLimitReference(self, parent):
        upperLimitReference = SubElement(parent, 'saf:upperLimitReference')
        upperLimitReference.text = 'STD'        
        
    def _horizontalProjection(self, parent):
        horizontalProjection = SubElement(parent, 'saf:horizontalProjection')        
        
        return horizontalProjection
        
    def _polygon(self, parent):
        polygonID = 'polygon-conv-a1-obs-position-'+self._productLoc+self._abbrev+'-'+ \
                              self._convectiveSigmetNumberStr+self._abbrev+'-'+ \
                              self._startDate[:8]+self._startDate[9:]+'Z-a1'        
        
        polygon = SubElement(parent, 'gml:Polygon')
        polygon.set('gml:id', polygonID)
        polygon.set('srsDimension', '2')
        polygon.set('srsName', 'http://www.opengis.net/def/crs/EPSG/0/4326')        
        
        return polygon
    
    def _exterior(self, parent):
        exterior = SubElement(parent, 'gml:exterior')        
        return exterior
    
    def _linearRing(self, parent):
        linearRing = SubElement(parent, 'gml:LinearRing')        
        return linearRing
    
    def _posList(self, parent):
        posListText = ''
        count = str(len(self._vertices))
        for vertice in self._vertices:
            posListText = posListText+("{0:.2f}".format(vertice[1]))+' '+("{0:.2f}".format(vertice[0]))+' '        
        
        posList = SubElement(parent, 'gml:posList')
        posList.set('count', count)
        posList.text = posListText        
        
    def _speedOfMotion(self, parent):
        if self._hazardMotionStr == 'MOV LTL.':
            speedOfMotionText = '-999'
        else:
            speedOfMotionText = self._hazardMotionStr[12:14]        
        
        speedOfMotion = SubElement(parent, 'iwxxm:speedOfMotion')
        speedOfMotion.set('uom',"[nmi_i]")
        speedOfMotion.text = speedOfMotionText
        
    def _convectionGeometry(self, parent):
        convectionGeometryLinkDict = {'Polygon': 'AREA_OF_THUNDERSTORMS/', 'Point': 'ISOLATED_THUNDERSTORMS/', 'LineString': 'LINE_OF_THUNDERSTORMS/'}    
        convectionGeometryLink = "http://nws.weather.gov/codes/NWSI10-811/2011/SIGMETConvectionGeometry/" + \
                                 convectionGeometryLinkDict[self._geomType]        
        
        convectionGeometry = SubElement(parent, 'iwxxm-us:convectionGeometry')
        convectionGeometry.set('xlink:href', convectionGeometryLink)        
        
    def _convectionHeightForecast(self, parent):
        if self._convectiveSigmetCloudTopStr == 'TOPS ABV FL450.':
            aboveBelowIndicatorLink = "http://nws.weather.gov/codes/NWSI10-811/2011/ConvectionTopIndicator/TOPS_ABOVE/"
        else:
            aboveBelowIndicatorLink = "http://nws.weather.gov/codes/NWSI10-811/2011/ConvectionTopIndicator/TOPS_TO/"        

        convectionHeightForecast = SubElement(parent, 'iwxxm-us:ConvectionHeightForecast')
        aboveBelowIndicator = SubElement(convectionHeightForecast, 'iwxxm-us:aboveBelowIndicator')
        aboveBelowIndicator.set('xlink:href', aboveBelowIndicatorLink)
        convectionHeightValue = SubElement(convectionHeightForecast, 'iwxxm-us:convectionHeightValue')
        convectionHeightValue.set('uom', "FL")
        convectionHeightValue.text = self._upperLimitText        
        
    def _basisForIssuance(self, xml, eventDict, domains):
        key = 'basisForIssuance'
        basisLink = "http://nws.weather.gov/codes/NWSI10-811/2011/BasisForIssuance/OBSERVATION"
        
        basisForIssuance = self._makeElement(xml, key)
        basisForIssuance.set('xlink:href', basisLink)
        
    def _continuation(self, xml, eventDict, domains):
        key = 'continuationAfterInitialValidPeriod'
        continuationText = 'false'
        
        continuation = self._makeElement(xml, key)
        continuation.text = continuationText
        
    def _isSpecialIssuance(self, xml, eventDict, domains):
        key = 'isSpecialIssuance'
        #if self._specialIssuance == True:
        if self._specialIssuance:
            specialText = 'true'
        else:
            specialText = 'false'
        
        isSpecialIssuance = self._makeElement(xml, key)
        isSpecialIssuance.text = specialText
        
    def _isCorrection(self, xml, eventDict, domains):
        key = 'isCorrection'
        correctionText = 'false'
        
        isCorrection = self._makeElement(xml, key)
        isCorrection.text = correctionText
        
    def _makeElement(self, root, key, addPrefix=True):
        return SubElement(root, self._prefix+key)                            

    def _formatConvectiveSigmet(self, eventDict, domains):
        self._productLoc = 'MKC' 
        self._fullStationID = 'KKCI'                
        self._SIGMET_ProductName = 'CONVECTIVE SIGMET'
        self._convectiveSigmetNumberStr = eventDict.get('sigmetNumber','1')
        self._convectiveSigmetDomain = eventDict.get('domain','East')
        self._geomType = eventDict.get('geomType','Polygon')
                
        eventDictParts = eventDict.get('parts')
        
        self._statesListStr = eventDictParts.get('states','NULL')
        self._convectiveSigmetModifierStr = eventDictParts.get('modifier','')
        self._hazardMotionStr = eventDictParts.get('motion','MOV LTL.')
        self._convectiveSigmetCloudTopStr = eventDictParts.get('cloudTop','TOPS ABV FL450.')
        self._startTime = eventDictParts.get('startTime','DDHHMM')
        self._endTime = eventDictParts.get('endTime','DDHHMM')      
        self._startDate = eventDictParts.get('startDate','YYYYMMDD_HHmm')               
        self._endDate = eventDictParts.get('endDate','YYYYMMDD_HHmm')
        self._vertices = eventDictParts.get('geometry',None)

        
        for domain in domains:
            if domain.domainName() == self._convectiveSigmetDomain:
                self._abbrev = domain.abbrev()
        
        if eventDictParts.get('specialTime') is not None:
            self._specialTime = eventDictParts.get('specialTime')
            startTime = eventDictParts.get('specialTime')
        else:
            startTime = self._startTime
        self._startTime = startTime
        
        self._timePeriod = 'tp-'+self._startTime[:2]+'T'+self._startTime[2:]+'Z-'+ \
                           self._endTime[:2]+'T'+self._endTime[2:]+'Z'
        self._beginPositionText = self._startDate[:4]+'-'+self._startDate[4:6]+'-'+ \
                                  self._startTime[:2]+'T'+self._startTime[2:4]+':'+ \
                                  self._startTime[4:6]+':00Z'        
    
    def _outputXML(self, xmlString):              
        outDir = os.path.join(OUTPUTDIR, self._startDate)
        outAdvisory = 'internationalSIGMET_'+self._convectiveSigmetNumberStr+self._convectiveSigmetDomain+'.xml'
        pathFile = os.path.join(outDir, outAdvisory)
        
        if not os.path.exists(outDir):
            try:
                os.makedirs(outDir)
            except:
                sys.stderr.write('Could not create output directory')
        with open(pathFile, 'w') as outFile:
            outFile.write(xmlString)
        return    