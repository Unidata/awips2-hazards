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

class Format(Legacy_Hydro_Formatter.Format):

    def initialize(self):
        super(Format, self).initialize()
        self.initProductPartMethodMapping()

        self._productGeneratorName = 'AIRMET_ProductGenerator'
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

        parts = self.productDict.get('productParts')
        eventDicts = self.productDict.get('eventDicts')

        self._rootPrefix = 'iwxxm:AIRMET'
        self._prefix = 'iwxxm:'

        for eventDict in eventDicts:
            eventDictParts = eventDict.get('parts')
            self._hazardType = eventDictParts.get('hazardType', 'NULL')

            xmlParts = self.getXMLParts()

            self._issueFlag = eventDict.get('issueFlag',False)
            self._status = eventDict.get('status',"PENDING")
            self._formatAirmet(eventDict)
            xml = self._createXML(eventDict, xmlParts)
            xmlString = str(xml)
            xmlString = tostring(xml, 'utf-8')
            xmlString = str(xmlString)
            #xmlOut = self.prettify(xmlString)
            #xmlString = str(xmlOut)

            if self._status in ["PENDING","ISSUED"]:
                if self._issueFlag == "True":
                    self._outputXML(xmlOut)
        return [ProductUtils.wrapLegacy(xmlString)],self._editableParts

    def getXMLParts(self):
        xmlParts = ['issuingAirTrafficServicesUnit', 'originatingMeteorologicalWatchOffice',
                    'sequenceNumber', 'validPeriod', 'phenomenon', 'analysis']

        return xmlParts

    def _formatAirmet(self, eventDict):
        eventDictParts = eventDict.get('parts')

        self._sequenceName = eventDictParts.get('sequenceName', None)
        self._originatingOffice = eventDictParts.get('originatingOffice','NULL')
        self._zone = eventDictParts.get('zone','NULL')
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

    def _createXML(self, eventDict, xmlParts):
        xml = self.makeRootElement()

        for part in xmlParts:
            exec "self."+part+"(xml, eventDict)"
        self.flush()

        return xml

    def makeRootElement(self):
        xml = Element(self._rootPrefix)

        xml.set('xsi:schemaLocation', \
                "http://www.aixm.aero/schema/5.1.1 http://www.aixm.aero/schema/5.1.1_profiles/AIXM_WX/5.1.1a/AIXM_Features.xsd      http://def.wmo.int/metce/2013 http://schemas.wmo.int/metce/1.2/metce.xsd      http://icao.int/iwxxm/2.1 http://schemas.wmo.int/iwxxm/2.1/iwxxm.xsd")
        xml.set('gml:id','airmet-'+self._originatingOffice+'-'+self._startDate[:7]+self._startDate[9:]+'Z')
        xml.set("permissibleUsage", "OPERATIONAL")

        return xml

    def issuingAirTrafficServicesUnit(self, xml, eventDict):
        key = 'issuingAirTrafficServicesUnit'
        issuingAirTrafficServicesUnit = self._makeElement(xml, key)

        atsUnit = SubElement(issuingAirTrafficServicesUnit, 'aixm:Unit')
        atsUnit.set('gml:id', 'fic-'+self._originatingOffice)

        atsTimeSlice = SubElement(atsUnit, 'aixm:timeSlice')

        atsUnitTimeSlice = SubElement(atsTimeSlice, 'aixm:UnitTimeSlice')
        atsUnitTimeSlice.set('gml:id', 'fic-'+self._originatingOffice+'-ts')

        atsValidTime = SubElement(atsUnitTimeSlice, 'gml:validTime/')

        atsInterpretation = SubElement(atsUnitTimeSlice, 'aixm:interpretation')
        atsInterpretation.text = 'SNAPSHOT'

        atsName = SubElement(atsUnitTimeSlice, 'aixm:name')
        atsName.text = self._originatingOffice + ' FIC'

        atsType = SubElement(atsUnitTimeSlice, 'aixm:type')
        atsType.text = 'FIC'

        atsDesignator = SubElement(atsUnitTimeSlice, 'aixm:designator')
        atsDesignator.text = self._originatingOffice

    def originatingMeteorologicalWatchOffice(self, xml, eventDict):
        key = 'originatingMeteorologicalWatchOffice'
        originatingMeteorologicalWatchOffice = self._makeElement(xml, key)

        mwoUnit = SubElement(originatingMeteorologicalWatchOffice, 'aixm:Unit')
        mwoUnit.set('gml:id', 'wmo-'+self._originatingOffice)

        mwoTimeSlice = SubElement(mwoUnit, 'aixm:timeSlice')

        mwoUnitTimeSlice = SubElement(mwoTimeSlice, 'aixm:UnitTimeSlice')
        mwoUnitTimeSlice.set('gml:id', 'wmo-'+self._originatingOffice+'-ts')

        mwoValidTime = SubElement(mwoUnitTimeSlice, 'gml:validTime/')

        mwoInterpretation = SubElement(mwoUnitTimeSlice, 'aixm:interpretation')
        mwoInterpretation.text = 'SNAPSHOT'

        mwoName = SubElement(mwoUnit, 'aixm:name')
        mwoName.text = self._originatingOffice+' MWO'

        mwoType = SubElement(mwoUnit, 'aixm:type')
        mwoType.text = 'MWO'

        mwoDesignator = SubElement(mwoUnit, 'aixm:designator')
        mwoDesignator.text = self._originatingOffice

    def sequenceNumber(self, xml, eventDict):
        key = 'sequenceNumber'
        sequenceNumber = self._makeElement(xml, key)

        updateNumberDict = self.productDict.get('updateNumberDict')
        updateNumber = updateNumberDict['updateNumber'][self._zone]

        if self._hazardType in ['LLWS', 'Strong_Surface_Wind', 'Turbulence']:
            sequenceNum = updateNumber['TANGO']
        elif self._hazardType in ['Mountain_Obscuration', 'IFR']:
            sequenceNum = updateNumber['SIERRA']
        else:
            sequenceNum = updateNumber['ZULU']

        sequenceNumber.text = str(sequenceNum+1)

    def validPeriod(self, xml, eventDict):
        key = 'validPeriod'
        validPeriod = self._makeElement(xml, key)

        timePeriod = SubElement(validPeriod, 'gml:TimePeriod')
        timePeriod.set('gml:id', self._timePeriod)

        beginPosition = SubElement(timePeriod, 'gml:beginPosition')
        beginPosition.text = self._beginPositionText

        endPosition = SubElement(timePeriod, 'gml:endPosition')
        endPosition.text = self._endPositionText

    def phenomenon(self, xml, eventDict):
        key = 'phenomenon'

        phenomenonDict = {'STRONG_SURFACE_WIND': "http://codes.wmo.int/49-2/AirWxPhenomena/_SFC_WIND",
                          'LLWS': "http://codes.wmo.int/49-2/AirWxPhenomena",
                          'TURBULENCE': "http://codes.wmo.int/49-2/AirWxPhenomena/_MOD_TURB",
                          'MOUNTAIN_OBSCURATION': "http://codes.wmo.int/49-2/AirWxPhenomena/_MT_OBSC",
                          'IFR': "http://codes.wmo.int/49-2/AirWxPhenomena/",
                          'ICING': "http://codes.wmo.int/49-2/AirWxPhenomena/_MOD_ICE",
                          'MULTIPLE_FREEZING_LEVELS': "http://codes.wmo.int/49-2/AirWxPhenomena/",
                          '': 'http://codes.wmo.int/49-2/AirWxPhenomena',
                          'NULL': 'http://codes.wmo.int/49-2/AirWxPhenomena',
                          }

        phenomLink = phenomenonDict[self._hazardType]

        phenomenon = self._makeElement(xml, key)
        phenomenon.set('xlink:href', phenomLink)



    def analysis(self, xml, eventDict): ##########COMPLETE###########
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
        evolvingMetCondition = self._evolvingMetCondition(result, eventDict)
        geometry = self._geometry(evolvingMetCondition)
        airspaceVolume = self._airspaceVolume(geometry)

        if self._hazardType in ['Turbulence', 'Icing', 'Multiple_Freezing_Levels']:
            self._lowerLimit(airspaceVolume, eventDict)
            self._lowerLimitReference(airspaceVolume)

        horizontalProjection = self._horizontalProjection(airspaceVolume)
    #===================================================================================
    #END OF MAIN XML TAGS
    #-----------------------------------------------------------------------------------
    #BEGINNING OF SUB METHODS FOR ANALYSIS TAG
    #===================================================================================

    def _omObservation(self, parent):
        omObservation = SubElement(parent, 'om:OM_Observation')
        omObservation.set('gml:id', "analysis")

        return omObservation

    def _omType(self, parent):
        omType = SubElement(parent, 'om:type')
        omType.set('xlink:href', "http://codes.wmo.int/49-2/observation-type/iwxxm/2.1/AIRMETEvolvingConditionAnalysis")

        return omType

    def _phenomenonTime(self, parent):
        self._timeInstantText = 'ti-'+self._startTime[:2]+'T'+self._startTime[2:]+'Z'

        phenomenonTime = SubElement(parent, 'om:phenomenonTime')
        phenomenonTime.set('xlink:href', '#'+self._timeInstantText)

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

    def _validTime(self, parent):
        validTimeText = '#tp-'+self._startTime[:2]+'T'+self._startTime[2:]+'Z-'+self._endTime[:2]+'T'+self._endTime[2:]+'Z'

        validTime = SubElement(parent, 'om:validTime')
        validTime.set('xlink:href', validTimeText)

    def _procedure(self, parent):
        procedure = SubElement(parent, 'om:procedure')

        return procedure

    def _process(self, parent):
        process = SubElement(parent, 'metce:Process')
        process.set('gml:id', "p-49-2-airmet")

        return process

    def _processDescription(self, parent):
        processDescription = SubElement(parent, 'gml:description')
        processDescription.text = "WMO No. 49 Volume 2 Meteorological Service for International Air Navigation APPENDIX 6-1 TECHNICAL SPECIFICATIONS RELATED TO AIRMET INFORMATION"

    def _observedProperty(self, parent):
        observedProperty = SubElement(parent, 'om:observedProperty')
        observedProperty.set('xlink:href', "http://codes.wmo.int/49-2/observable-property/AIRMETEvolvingConditionAnalysis")

    def _featureOfInterest(self, parent, eventDict):
        eventDictParts = eventDict.get('parts')

        featureOfInterest = SubElement(parent, 'om:featureOfInterest')
        spatialSampling = SubElement(featureOfInterest, 'sams:SF_SpatialSamplingFeature')
        spatialSampling.set('gml:id', "sampling-surface-Amswell")

        type = SubElement(spatialSampling, 'sams:type')
        type.set('xlink:href', "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface")

        sampledFeature = SubElement(spatialSampling, 'sam:sampledFeature')
        airspace = SubElement(sampledFeature, 'aixm:Airspace')
        airspace.set('gml:id', "uuid.15c2c2ba-c5f2-47b5-9ada-1964d51b82c0")

        timeSlice = SubElement(airspace, 'aixm:timeSlice')
        airspaceTimeSlice = SubElement(timeSlice, 'aixm:AirspaceTimeSlice')
        airspaceTimeSlice.set('gml:id', "ats3")

        validTime = SubElement(airspaceTimeSlice, 'gml:validTime')
        interpretation = SubElement(airspaceTimeSlice, 'aixm:interpretation')
        interpretation.text = "SNAPSHOT"
        airspaceType = SubElement(airspaceTimeSlice, 'aixm:type')
        airspaceType.text = "FIR"
        designator = SubElement(airspaceTimeSlice, 'aixm:designator')
        designator.text = self._originatingOffice
        name = SubElement(airspaceTimeSlice, 'aixm:name')
        name.text = self._zone + "FIR"

        shape = SubElement(spatialSampling, 'sams:shape')
        shape.set('nilReason', "withheld")

    def _result(self, parent):
        result = SubElement(parent, 'om:result')

        return result

    def _evolvingMetCondition(self, parent, eventDict):
        eventDictParts = eventDict.get('parts')

        intensityChangeDict = {'DVLPG ': 'INTENSIFY', 'INTSF ': 'INTENSIFY', 'DMSHG ': 'WEAKEN', '': 'NO_CHANGE'}

        evolvingMetCondition = SubElement(parent, 'iwxxm:AIRMETEvolvingConditionCollection')
        evolvingMetCondition.set('gml:id', "cb-aec1")
        evolvingMetCondition.set('timeIndicator', "OBSERVATION")

        member = SubElement(evolvingMetCondition, 'iwxxm:member')
        evolvingCondition = SubElement(member, 'iwxxm:AIRMETEvolvingCondition')
        evolvingCondition.set('gml:id','cb-aec1')
        evolvingCondition.set('intensityChange', eventDictParts.get('intensityTrend'))

        directionOfMotion = SubElement(evolvingCondition, 'iwxxm:directionOfMotion')
        directionOfMotion.set('xsi:nil', "true")
        directionOfMotion.set('uom', "N/A")
        directionOfMotion.set('nilReason',"inapplicable")

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

        upperLimit = SubElement(parent, 'aixm:upperLimit')
        upperLimit.set('uom', "FL")
        upperLimit.text = eventDictParts.get('verticalExtent')

    def _upperLimitReference(self, parent):
        upperLimitReference = SubElement(parent, 'aixm:upperLimitReference')
        upperLimitReference.text = 'STD'

    def _horizontalProjection(self, parent):
        horizontalProjection = SubElement(parent, 'aixm:horizontalProjection')

        surface = SubElement(horizontalProjection, 'aixm:Surface')
        surface.set('gml:id',"obs-sfc")
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
        OUTPUTDIR = AviationUtils.AviationUtils().outputAirmetFilePath()

        outDir = os.path.join(OUTPUTDIR, self._startDate)
        outAdvisory = 'airmet.xml'
        pathFile = os.path.join(outDir, outAdvisory)

        if not os.path.exists(outDir):
            try:
                os.makedirs(outDir)
            except:
                sys.stderr.write('Could not create output directory')
        with open(pathFile, 'w') as outFile:
            outFile.write(xmlString)
        return
