"""
    Description: Product Generator for the AIRMET product.
"""
import os, types, sys, collections, re
import math
from HydroProductParts import HydroProductParts
import GeometryFactory
import HydroGenerator
from KeyInfo import KeyInfo
import shapely, time, datetime, TimeUtils
import HazardDataAccess
import MetaData_Convective_SIGMET
import Domains
import AviationUtils
from VisualFeatures import VisualFeatures
from com.raytheon.uf.common.time import SimulatedTime

class Product(HydroGenerator.Product):

    def __init__(self):
        super(Product, self).__init__()
        # Used by the VTECEngineWrapper to access the productGeneratorTable
        self._productGeneratorName = 'AIRMET_ProductGenerator'
        self._productID = 'AIRMET'

###################################################


    def defineScriptMetadata(self):
        metadata = collections.OrderedDict()
        metadata['author'] = "GSD/Raytheon"
        metadata['description'] = "Product generator for AIRMET."
        metadata['version'] = "1.0"
        return metadata

    def defineDialog(self, eventSet):
        """
        @return: dialog definition to solicit user input before running tool
        """
        return {}

    def _initialize(self):
        super(Product, self)._initialize()
        # This is for the VTEC Engine
        self._productCategory = "AIRMET"
        self._areaName = ''
        # Number of hours past issuance time for expireTime
        # If -1, use the end time of the hazard
        self._purgeHours = 8
        self._SIGMET_ProductName = 'AIRMET'
        self._includeAreaNames = False
        self._includeCityNames = False
        self._vtecProduct = False

    def execute(self, eventSet, dialogInputMap):
        '''
        Inputs:
        @param eventSet: a list of hazard events (hazardEvents) plus
                               a map of additional variables
        @return productDicts, hazardEvents:
             Each execution of a generator can produce 1 or more
             products from the set of hazard events
             For each product, a productID and one dictionary is returned as input for
             the desired formatters.
             Also, returned is a set of hazard events, updated with product information.

        productDict:  {'eventDicts': [{'eventID': 'HZ-2015-OAX-001701', 'geomType': 'Polygon',
                       'issueFlag': 'False', 'parts': OrderedDict([('originatingOffice', 'KKCI'), ('currentTime', '261700'),
                       ('zone', 'SFO'), ('advisoryType', 'Amendment'), ('startTime', '261700'), ('endTime', '262300'),
                       ('states', 'SD NE '), ('intensityTrend', None), ('timeConstraint', 'Occasional'),
                       ('timeConstraintTime', None), ('outlookTime', 'None'), ('geometry', [(-101.52309257319133, 44.97942299109807),
                       (-101.16159072586119, 40.70713630246483), (-98.7031818013116, 44.606242897736266),
                       (-101.52309257319133, 44.97942299109807)]), ('faaHeader', {'SIERRA': 'SFOS WA', 'ZULU': 'SFOZ WA', 'TANGO': 'SFOT WA'}),
                       ('awipsHeader', {'SIERRA': 'WA6S', 'ZULU': 'WA6Z', 'TANGO': 'WA6T'}), ('wmoHeader', 'WAUS46 KKCI'), ('severity', None),
                       ('phenomenon', None), ('restrictions', None), ('verticalExtent', None), ('freezingLevel', None), ('boundingStatement',
                       'FROM 20SSE_DPR-30SW_LBF-50SSW_ABR-20SSE_DPR'), ('issuanceType', 'Amendment'), ('outlookBoundingStatement', None)]),
                       'status': 'PENDING', 'hazardType': 'Strong_Surface_Wind'}], 'productName': 'AIRMET',
                       'updateNumberDict': {u'issuancePackage': u'261701', u'updateNumber': {u'JNU': {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0},
                       u'ANC': {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}, u'MIA': {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}, u'CHI':
                       {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}, u'DFW': {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}, u'BOS':
                       {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}, u'HNL': {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}, u'SFO':
                       {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 2}, u'FAI': {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}, u'SLC':
                       {u'SIERRA': 0, u'ZULU': 0, u'TANGO': 0}}, u'originalIssuanceTime': u'261701'}, 'productParts':
                       ['originatingOffice', 'currentTime', 'zone', 'advisoryType', 'startTime', 'endTime', 'states', 'intensityTrend',
                       'timeConstraint', 'timeConstraintTime', 'outlookTime', 'geometry', 'faaHeader', 'awipsHeader', 'wmoHeader',
                       'severity', 'phenomenon', 'restrictions', 'verticalExtent', 'freezingLevel', 'boundingStatement', 'issuanceType',
                       'outlookBoundingStatement'], 'productID': 'AIRMET'}
        '''
        self._initialize()
        self.logger.info("Start ProductGeneratorTemplate:execute ESF")

        # Extract information for execution
        self._inputHazardEvents = eventSet.getEvents()
        eventSetAttrs = eventSet.getAttributes()
        metaDict = eventSet.getAttributes()
        self._issueFlag = metaDict.get('issueFlag')
        self._currentTime = eventSet.getAttributes().get("currentTime") / 1000

        caveMode = eventSet.getAttributes().get('hazardMode','PRACTICE').upper()
        self.practice = (False if caveMode == 'OPERATIONAL' else True)

        if dialogInputMap:
            self._storeDialogInputMap(dialogInputMap)
        else:
            self._dialogInputMap = {}

        parts = ['originatingOffice', 'currentTime', 'zone', 'advisoryType',
                 'startTime', 'endTime', 'outlookEndTime', 'states', 'intensityTrend',
                 'timeConstraint', 'timeConstraintTime', 'geometry',
                 'faaHeader', 'awipsHeader', 'wmoHeader', 'severity',
                 'phenomenon', 'restrictions', 'verticalExtent', 'freezingLevel',
                 'boundingStatement', 'issuanceType', 'outlookBoundingStatement',
                 'currentBoundingStatement']

        productDict = {}
        productDict['productParts'] = parts
        eventDicts = []

        # Find the latest startTime for the event
        self._latestStartTime = None
        for event in self._inputHazardEvents:
            if self._latestStartTime is None:
                self._latestStartTime = event.getStartTime()
            else:
                startTime = event.getStartTime()
                if startTime > self._latestStartTime:
                    self._latestStartTime = startTime

        self._latestStartTime = self._latestStartTime.strftime('%d%H%M')

        updateDict = self.createUpdateDict(eventSet)
        updateNumberDict = self.updateNumberDict(updateDict)
        productDict['updateNumberDict'] = updateNumberDict

        for event in self._inputHazardEvents:
            self._eventPhenomenon = event.getPhenomenon()
            self._geomType = event.get('originalGeomType')

            dict = {}
            dict['eventID'] = event.getEventID()
            dict['geomType'] = self._geomType
            dict['issueFlag'] = self._issueFlag
            dict['hazardType'] = self._eventPhenomenon
            dict['status'] = event.getStatus()

            partDict = collections.OrderedDict()
            for partName in parts:
                execStr = 'partStr = self.' + partName + '(event)'
                exec execStr
                partDict[partName] = partStr

            dict['parts'] = partDict
            eventDicts.append(dict)

            if not self.eventValid(event, dict):
                continue

        productDict['eventDicts'] = eventDicts
        productDict['productID'] = 'AIRMET'
        productDict['productName'] = 'AIRMET'

        return [productDict], self._inputHazardEvents

    def eventValid(self, event, eventDict):
        eventDictParts = eventDict['parts']
        issueFlag = eventDict['issueFlag']
        eventStatus = eventDict['status']
        advisoryType = eventDictParts['advisoryType']

        if eventStatus in ['ELAPSED', 'ENDED', 'ENDING']:
            event.setStatus('ELAPSED')
            return False
        if issueFlag == 'True' and advisoryType == 'Cancellation':
            event.setStatus('ELAPSED')
            return False
        return True

    def createUpdateDict(self,eventSet):
        updateDict = {'updateBoolean': {'BOS': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'MIA': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'DFW': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'CHI': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'SLC': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'SFO': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'JNU': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'ANC': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'FAI': {'SIERRA': False,'TANGO': False,'ZULU': False},
                                        'HNL': {'SIERRA': False,'TANGO': False,'ZULU': False}}}

        self._inputHazardEvents = eventSet.getEvents()
        for event in self._inputHazardEvents:
            hazardType = event.getPhenomenon()
            issuanceType = event.get("llwsType")
            zone = event.get('llwsZone')

            if issuanceType in ['Amendment', 'Correction', 'Cancellation']:
                if hazardType in ['LLWS','Strong_Surface_Wind', 'Turbulence']:
                    updateDict['updateBoolean'][zone]['TANGO'] = True
                elif hazardType in ['Mountain_Obscuration', 'IFR']:
                    updateDict['updateBoolean'][zone]['SIERRA'] = True
                else:
                    updateDict['updateBoolean'][zone]['ZULU'] = True

        return updateDict

    def updateNumberDict(self, updateDict):
        import GenericRegistryObjectDataAccess

        originalIssuanceTime = time.strftime('%d%H%M', time.gmtime(self._currentTime))

        objectDict = {'objectType': 'AIRMET',
                      'uniqueID': 'airmetNumberDict',
                      'issuancePackage': self._latestStartTime,
                      'originalIssuanceTime': originalIssuanceTime,
                      'updateNumber': {'BOS': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'MIA': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'DFW': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'CHI': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'SLC': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'SFO': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'JNU': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'ANC': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'FAI': {'SIERRA': 0,'TANGO': 0,'ZULU': 0},
                                       'HNL': {'SIERRA': 0,'TANGO': 0,'ZULU': 0}}}

        #if file already exists
        objectDicts = GenericRegistryObjectDataAccess.queryObjects(
            [("objectType", 'AIRMET'),
             ("uniqueID", "airmetNumberDict")],
            self.practice)

        if objectDicts:
            oldIssuancePackage = objectDicts['issuancePackage']
            #if not same issuance time set to new updateNumberDict
            if oldIssuancePackage != objectDict['issuancePackage']:
                pass
            else:
            #if same issuance time read in and do updates to numbering
                for key, value in objectDicts.iteritems():
                    for key1, value1 in value.iteritems():
                        for key2, value2 in value1.iteritems():
                            if value2 == True:
                                objectDicts['updateNumber'][key1][key2] += 1

            #if issuing update the dictionary with all of the new entries
            if self._issueFlag == "True":
                GenericRegistryObjectDataAccess.storeObject(objectDicts,self.practice)
        #if file doesn't exist
        else:
            if self._issueFlag == "True":
                GenericRegistryObjectDataAccess.storeObject(objectDict,self.practice)

        return objectDict

    def originatingOffice(self, hazardEvent):
        originatingOffice = hazardEvent.get('llwsOffice')
        self.originatingOfficeStr = originatingOffice
        return originatingOffice

    def currentTime(self, hazardEvent):
        currentTime = time.strftime('%d%H%M', time.gmtime(self._currentTime))
        self.currentTimeStr = currentTime
        return currentTime

    def zone(self, hazardEvent):
        zone = hazardEvent.get('llwsZone')
        self.zoneStr = zone
        return zone

    def advisoryType(self, hazardEvent):
        return hazardEvent.get("llwsType")

    def startTime(self, hazardEvent):
        epochStartTime = time.mktime(hazardEvent.getStartTime().timetuple())
        startTime = time.strftime('%d%H%M', time.gmtime(epochStartTime))
        self.startTimeStr = startTime
        return startTime

    def endTime(self, hazardEvent):
        self.epochEndTime = time.mktime(hazardEvent.getEndTime().timetuple())
        endTime = time.strftime('%d%H%M', time.gmtime(self.epochEndTime))
        self.endTimeStr = endTime
        return endTime

    def outlookEndTime(self, hazardEvent):
        return time.strftime('%d%H%M', time.gmtime(self.epochEndTime + (6 * 3600)))

    def states(self, hazardEvent):
        self._hazardZonesDict = hazardEvent.getHazardAttributes().get('hazardArea')

        statesList = []
        for key in self._hazardZonesDict:
            states = key[:2]
            if states in statesList:
                pass
            else:
                statesList.append(states)
        statesListStr = ''
        for states in statesList:
            statesListStr += states + ' '

        return statesListStr

    def timeConstraint(self, hazardEvent):
        if self._eventPhenomenon != 'Multiple_Freezing_Levels':
            timeConstraint = hazardEvent.get('llwsTimeConstraint')
            return timeConstraint
        else:
            return None

    def timeConstraintTime(self, hazardEvent):
        if self._eventPhenomenon != 'Multiple_Freezing_Levels':
            timeConstraint = hazardEvent.get('llwsTime')
            return timeConstraint
        else:
            return None

    def outlookTime(self, hazardEvent):
        outlookTimeDict = {'LLWS': 'llwsTime', 'Strong_Surface_Wind': 'sswTime',
                                    'Turbulence': 'turbulenceTime', 'Mountain_Obscuration': 'mountainObscurationTime',
                                    'IFR': 'ifrTime', 'Icing': 'icingTime', 'Freezing_Level': 'freezingLevelTime'}

        if self._eventPhenomenon != 'Multiple_Freezing_Levels':
            outlookTime = str(hazardEvent.get(outlookTimeDict[self._eventPhenomenon]))
            return outlookTime
        else:
            return None

    def intensityTrend(self, hazardEvent):
        intensityStrDict = {'None': '', 'No Change': ' NC.', 'Intensify': ' INTSF.', 'Weaken': ' WKN.', 'Improve': ' IMPR.', 'Deteriorate': ' DTRT.'}

        if self._eventPhenomenon == 'Multiple_Freezing_Levels':
            return None
        else:
            intensityTrend = hazardEvent.get('llwsIntensity')
            intensityTrendStr = intensityStrDict[intensityTrend]
            return intensityTrendStr

    def wmoHeader(self, hazardEvent):
        if self.originatingOfficeStr == 'KKCI':
            wmoHeaderDict = {'BOS': 'WAUS41 KKCI',
                             'MIA': 'WAUS42 KKCI',
                             'CHI': 'WAUS43 KKCI',
                             'DFW': 'WAUS44 KKCI',
                             'SLC': 'WAUS45 KKCI',
                             'SFO': 'WAUS46 KKCI'}
        elif self.originatingOfficeStr == 'PAWU':
            wmoHeaderDict = {'JNU': 'WAAK47 PAWU', 'ANC': 'WAAK48 PAWU', 'FAI': 'WAAK49 PAWU'}
        else:
            wmoHeaderDict = {'HNL': 'WAHW31 PHFO'}

        wmoHeader = wmoHeaderDict[self.zoneStr]
        return wmoHeader

    def awipsHeader(self, hazardEvent):
        if self.originatingOfficeStr == 'KKCI':
            awipsHeaderDict = {'BOS': {'SIERRA': 'WA1S', 'TANGO': 'WA1T', 'ZULU': 'WA1Z'},
                             'MIA': {'SIERRA': 'WA2S', 'TANGO': 'WA2T', 'ZULU': 'WA2Z'},
                             'CHI': {'SIERRA': 'WA3S', 'TANGO': 'WA3T', 'ZULU': 'WA3Z'},
                             'DFW': {'SIERRA': 'WA4S', 'TANGO': 'WA4T', 'ZULU': 'WA4Z'},
                             'SLC': {'SIERRA': 'WA5S', 'TANGO': 'WA5T', 'ZULU': 'WA5Z'},
                             'SFO': {'SIERRA': 'WA6S', 'TANGO': 'WA6T', 'ZULU': 'WA6Z'}}
        elif self.originatingOfficeStr == 'PAWU':
            awipsHeaderDict = {'JNU': 'WA7O', 'ANC': 'WA8O', 'FAI': 'WA9O'}
        else:
            awipsHeaderDict = {'HNL': 'WA0HI'}

        awipsHeader = awipsHeaderDict[self.zoneStr]
        return awipsHeader

    def faaHeader(self, hazardEvent):
        if self.originatingOfficeStr == 'KKCI':
            faaHeaderDict = {'BOS': {'SIERRA': 'BOSS WA', 'TANGO': 'BOST WA', 'ZULU': 'BOSZ WA'},
                             'MIA': {'SIERRA': 'MIAS WA', 'TANGO': 'MIAT WA', 'ZULU': 'MIAZ WA'},
                             'CHI': {'SIERRA': 'CHIS WA', 'TANGO': 'CHIT WA', 'ZULU': 'CHIZ WA'},
                             'DFW': {'SIERRA': 'DFWS WA', 'TANGO': 'DFWT WA', 'ZULU': 'DFWZ WA'},
                             'SLC': {'SIERRA': 'SLCS WA', 'TANGO': 'SLCT WA', 'ZULU': 'SLCZ WA'},
                             'SFO': {'SIERRA': 'SFOS WA', 'TANGO': 'SFOT WA', 'ZULU': 'SFOZ WA'}}
        elif self.originatingOfficeStr == 'PAWU':
            faaHeaderDict = {'JNU': {'SIERRA': 'JNUS WA', 'TANGO': '=JNUT WA', 'ZULU': '=JNUZ WA'},
                             'ANC': {'SIERRA': 'ANCS WA', 'TANGO': '=ANCT WA', 'ZULU': '=ANCZ WA'},
                             'FAI': {'SIERRA': 'FAIS WA', 'TANGO': '=FAIT WA', 'ZULU': '=FAIZ WA'}}
        else:
            faaHeaderDict = {'HNL': {'SIERRA': 'HNLS WA', 'TANGO': '=HNLT WA', 'ZULU': '=HNLZ WA'}}

        faaHeader = faaHeaderDict[self.zoneStr]
        return faaHeader

    def severity(self, hazardEvent):
        if self._eventPhenomenon == 'Turbulence':
            severity = hazardEvent.get('turbulenceComboBox')
            self.severityStr = severity
            return severity
        else:
            return None

    def phenomenon(self, hazardEvent):
        phenomenonStr = ''
        phenomenonDict = {'Precipitation': 'PCPN', 'Mist': 'BR', 'Haze': 'HZ', 'Fog': 'FG', 'Smoke': 'FU',
                          'Blowing Snow': 'BS', 'Light Rain': '-RA', 'Rain': 'RA', 'Heavy Rain': '+RA',
                          'Light Snow': '-SN', 'Snow': 'SN', 'Heavy Snow': '+SN', 'Light Rainshowers': '-SHRA',
                          'Rainshowers': 'SHRA', 'Heavy Rainshowers': '+SHRA', 'blowingSnow': 'BS',
                          'Clouds': 'CLDS'}

        if self._eventPhenomenon in ['Mountain_Obscuration', 'IFR']:
            if self._eventPhenomenon == 'Mountain_Obscuration':
                phenomList = hazardEvent.get("mountainObscurationPhenomenon")
            else:
                phenomList = hazardEvent.get("ifrPhenomenon")

            if phenomList:
                for phenom in phenomList:
                    phenomenonStr = phenomenonStr + phenomenonDict[phenom] + '/'
                return phenomenonStr[:-1]
            else:
                return phenomenonStr
        else:
            return phenomenonStr

    def restrictions(self, hazardEvent):
        if self._eventPhenomenon == 'IFR':
            restrictionType = hazardEvent.get("ifrCigsVis")
            if restrictionType:
                if "ceiling" in restrictionType:
                    ceilingRestriction = hazardEvent.get('ifrCeilingBelow')
                    ceilingRestrictionStr = 'CIG BLW ' + ceilingRestriction
                else:
                    ceilingRestrictionStr = ''
                if "visibility" in restrictionType:
                    visibilityRestriction = hazardEvent.get('ifrVisibilityBelow')
                    visibilityRestrictionStr = 'VIS BLW ' + visibilityRestriction + 'SM'
                else:
                    visibilityRestrictionStr = ''

                if ceilingRestrictionStr:
                    restrictions = ceilingRestrictionStr
                    if visibilityRestrictionStr:
                        restrictions = restrictions + '/' + visibilityRestrictionStr
                else:
                    if visibilityRestrictionStr:
                        restrictions = visibilityRestrictionStr
            else:
                restrictions = ''

            return restrictions
        else:
            return ''

    def verticalExtent(self, hazardEvent):
        if self._eventPhenomenon in ['Turbulence', 'Icing']:
            if self._eventPhenomenon == 'Turbulence':
                if self.severityStr == 'moderateLowTurbulence':
                    turbulenceExtent = hazardEvent.get("moderateLowTurbulenceVerticalExtent")
                    if "lowLevelTurbulenceBetween" in turbulenceExtent:
                        verticalExtent = 'Between'
                        verticalExtentBottom = hazardEvent.get("turbulenceBetweenFLBottom")
                        verticalExtentTop = hazardEvent.get("turbulenceBelowFLTop")
                    elif "lowLevelTurbulenceBelow" in turbulenceExtent:
                        verticalExtent = 'Below'
                        verticalExtentBottom = None
                        verticalExtentTop = hazardEvent.get("turbulenceBelowFL")
                elif self.severityStr == 'moderateHighTurbulence':
                    turbulenceExtent = hazardEvent.get("moderateHighTurbulenceVerticalExtent")
                    verticalExtent = 'Between'
                    verticalExtentBottom = hazardEvent.get("highLevelTurbulenceBetweenFLBottom")
                    verticalExtentTop = hazardEvent.get("highLevelTurbulenceBelowFLTop")
            elif self._eventPhenomenon == 'Icing':
                icingExtent = hazardEvent.get("icingComboBox")
                if "between" in icingExtent:
                    verticalExtent = 'Between'
                    verticalExtentBottom = hazardEvent.get("icingBottom")
                    verticalExtentTop = hazardEvent.get("icingTop")
                elif "below" in icingExtent:
                    verticalExtent = 'Below'
                    verticalExtentBottom = None
                    verticalExtentTop = hazardEvent.get("icingTop")
                else: #above
                    verticalExtent = 'Above'
                    verticalExtentBottom = hazardEvent.get("icingBottom")
                    verticalExtentTop = None

            if verticalExtentBottom:
                if verticalExtentBottom != 'SFC' and int(verticalExtentBottom[2:]) < 180:
                    verticalExtentBottom = verticalExtentBottom[2:]
            if verticalExtentTop:
                if verticalExtentTop != 'SFC' and int(verticalExtentTop[2:]) < 180:
                    verticalExtentTop = verticalExtentTop[2:]

            if verticalExtent == 'Between':
                verticalExtent = verticalExtentBottom+"-"+verticalExtentTop
            elif verticalExtent == 'Below':
                verticalExtent = 'BLW ' + verticalExtentTop
            elif verticalExtent == 'Above':
                verticalExtent = 'ABV ' + verticalExtentBottom
            else:
                return None

            return verticalExtent
        else:
            return None

    def freezingLevel(self, hazardEvent):
        if self._eventPhenomenon == 'Multiple_Freezing_Levels':
            freezingLevelTop = hazardEvent.get('multipleFreezingLevelsTop')
            freezingLevelBottom = hazardEvent.get('multipleFreezingLevelsBottom')
            freezingLevel = 'MULT FRZLVL ' + freezingLevelTop + '-' + freezingLevelBottom + ' '
            return freezingLevel
        else:
            return None

    def boundingStatement(self, hazardEvent):
        return hazardEvent.getHazardAttributes().get('boundingStatement')

    def outlookBoundingStatement(self, hazardEvent):
        return hazardEvent.get('outlookBoundingStatement')

    def currentBoundingStatement(self, hazardEvent):
        return hazardEvent.get('currentBoundingStatement')

    def issuanceType(self, hazardEvent):
        issuanceType = hazardEvent.get('llwsType')
        self.issuanceTypeStr = issuanceType
        return issuanceType

    def geometry(self, hazardEvent):
        for g in hazardEvent.getFlattenedGeometry().geoms:
            geometry = shapely.geometry.base.dump_coords(g)
        return geometry

    def _narrativeForecastInformation(self, segmentDict, productSegmentGroup, productSegment):
        default = '''
|*
WSNT01 KKCI 261700
SIGA0A
KZWY SIGMET ALFA 5 VALID 261700/262100 KKCI-
KZWY NEW YORK OCEANIC FIR OBSC TS FCST WI N4727 W10040 - N4413
W10134 - N440 W9739 - N4727 W10040. TOP FL300. STNR. NC.
*|
         '''
        productDict['narrativeForecastInformation'] = self._section.hazardEvent.get('narrativeForecastInformation', default)

    def executeFrom(self, dataList, eventSet, prevDataList=None):
        if prevDataList is not None:
            dataList = self.correctProduct(dataList, eventSet, prevDataList, False)
        else:
            self.updateExpireTimes(dataList)
        return dataList
