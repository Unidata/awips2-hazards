'''
    Description: Maps hazard metadata to the Basis bullet of products
    Focal points can change the bullet text by locally changing the behavior of the 
    methods or including the main getBulletText method.  You will notice references
    to variables such as |* damOrLeveeName *|.  These denote metadata parameters included
    as attributes in HazardEvents by CommonMetaData and its subclasses.  The product 
    generation component is responsible for pulling the user chosen values of these variables
    from the attributes and substituting them into the text strings.
    
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Nov 17, 2014       4763     Daniel.S.Schaffer      Initial creation
    Feb 26, 2015       6599     Robert.Blum            Changed to new style class
    Mar 17, 2015       6958     Robert.Blum            Minor formatting change for FA.Y hazards since
                                                       the basisBullet does not include start time.
    Jun 05, 2015       8530     Robert.Blum            Changes to conform to WarnGen outputs
    Aug 19, 2015       10224    Robert.Blum            Adjusted rainSoFar to handle more user error cases.
    Oct 01, 2015       11739    Robert.Blum            Removed decimal place from rain so far if it is not needed.
    Nov 09, 2015       13111    Robert.Blum            Fix for rain so far when less than 1 inch.
    Dec 08, 2015       12479    Robert.Blum            Adding start time back to basis bullet for advisories.
    Dec 18, 2015       14036    Robert.Blum            Changed ellipses in basis bullet to comma.
    Dec 21, 2015       14042    Robert.Blum            Fixed grammer issue with rain so far.
    Mar 15, 2016       11892    Robert.Blum            Complete rework to add optional flooding flag.
    Aug 25, 2016       21458    Robert.Blum            Replacing hashTags with correct framing.
    Oct 13, 2016       22495    Sara.Stewart           Removed extraneous Dam & River words
    Oct 13, 2016       22510    Robert.Blum            Fixed siteImminent and siteFailed cases for FF.W.NonConvective.
    Feb 07, 2017       7137     JRamer                 Minimal hooks to BurnScarMetaData.py
    @author Daniel.S.Schaffer@noaa.gov
'''
import os
from TextProductCommon import TextProductCommon
import traceback
import sys

class BasisText(object):

    SOURCE_BULLET_TEXT = {
                         "dopplerSource" : "Doppler radar indicated ",
                         "dopplerGaugesSource" : "Doppler radar and automated rain gauges indicated ",
                         "trainedSpottersSource" : "trained weather spotters reported ",
                         "publicSource" : "the public reported ",
                         "localLawEnforcementSource" : "local law enforcement reported ",
                         "emergencyManagementSource" : "emergency management reported ",
                         "satelliteSource" : "satellite estimates indicate ",
                         "satelliteGaugesSource" : "satellite estimates and rain gauge data indicate ",
                         "gaugesSource" : "gauge reports indicated ",
                         "countySource" : "county dispatch reported ",
                         "corpsOfEngineersSource" : "Corps of Engineers reported ",
                         "damOperatorSource" : "dam operators reported ",
                         "bureauOfReclamationSource" : "Bureau of Reclamation reported ",
                         "civilAirPatrolSource" : "the Civil Air Patrol reported ",
                         "alaskaVolcanoSource" : "the Alaska Volcano Observatory reported ",
                         "cascadesVolcanoSource" : "the Cascades Volcano Observatory reported ",
                         }

    def getBulletText(self, hazardType, hazardDict, vtecRecord):
        self.getVariables(hazardType, hazardDict, vtecRecord)
        if hazardType == "FF.W.NonConvective":
            result = self.get_FF_W_NonConvectiveBulletText()
        elif hazardType == "FF.W.Convective":
            result = self.get_FF_W_ConvectiveBulletText()
        elif hazardType == "FF.W.BurnScar":
            result = self.get_FF_W_ConvectiveBulletText(burnScarTemplate=True)
            result += self.get_FF_W_BurnScarBulletText()
        elif hazardType == "FA.W":
            result = self.get_FA_W_BulletText()
        elif hazardType == "FA.Y":
            result = self.get_FA_Y_BulletText()
        else:
            result = "|* INSERT BASIS STATEMENT *|"
        return self.tpc.substituteParameters(hazardDict, result)

    def getVariables(self, hazardType, hazardDict, vtecRecord):
        '''
            Pulls out all the required data from the hazardDict for easy access throughout this module.
        '''
        self.hazardDict = hazardDict
        self.tpc = TextProductCommon()
        self.type = hazardDict.get("eventType")
        self.source = hazardDict.get("source")
        self.immediateCause = hazardDict.get("immediateCause")
        self.action = vtecRecord.get("act")
        if self.action == "COR":
            self.action == vtecRecord.get("prevAct")

        # Determine if flooding is occurring
        if hazardType == "FA.W":
            self.flood = hazardDict.get("flood")
            self.floodText = "flooding"
        elif hazardType == "FA.Y":
            self.flood = hazardDict.get("minorFlood")
            self.floodText = "minor flooding"
        else:
            self.flood = hazardDict.get("flashFlood")
            self.floodText = "flash flooding"

        # Will only be set for FA.W hazardTypes.
        self.genericReason = hazardDict.get('genericFloodReasoning', None)

        # Will only be set for FA.Y hazardTypes.
        self.advisoryType = hazardDict.get("advisoryType", None)
        self.optionalSpecificType = hazardDict.get("optionalSpecificType", None)

        # Will only be set for FF.W.NonConvective hazardTypes.
        self.hydrologicClause = hazardDict.get("hydrologicCause", None)

        # Will only be set for FF.W.BurnScar hazardTypes.
        self.debrisFlow = hazardDict.get("debrisFlow", None)

        # This flag is used in places where WarnGen's template file and Warngen's Followup templates differ.
        self.followUp = True
        if self.action in ["NEW", "EXT"]:
            self.followUp = False

        # Rain So Far Text
        self.rainSoFar = self.getRainSoFar(hazardDict)

        # Last thing, pick up our burnScarMetaData if we can.
        burnScarName = hazardDict.get('burnScarName')
        if burnScarName == None :
            # This happens if FFW but not burn scar FFW
            return
        if hasattr(self, 'burnScarMetaData') :
            if burnScarName == self.burnScarMetaData.get("burnScarName") :
                return
        from MapsDatabaseAccessor import MapsDatabaseAccessor
        mapsAccessor = MapsDatabaseAccessor()
        self.burnScarMetaData = mapsAccessor.getBurnScarMetadata(burnScarName)
        if self.burnScarMetaData == None :
            errmsg = "\nWARNING: " + \
                     "Burn scar in mapdata.burnscararea SQL table with no" + \
                     " associated metadata in BurnScarMetaData.py\n\n"
            sys.stderr.write(errmsg)
            self.burnScarMetaData = {}
        # Cache choice of scenario that was made in HID
        self.scenario = hazardDict.get('scenario')


    def isExpected(self, floodLocation=None):
        '''
            Mimics the isExpected variable from the WarnGen templates.
            States whether flooding is already or expected to occur.
        '''
        text = ""
        if self.flood:
            text = self.floodText.capitalize() + " is already occurring"
        else:
            text = self.floodText.capitalize() + " is expected to begin shortly"

        if floodLocation:
            text += floodLocation + "."
        else:
            text += "."

        return text

    def getSourceText(self):
        return self.SOURCE_BULLET_TEXT.get(self.source, "|* SOURCE *|")

###############################################################################

    def get_FF_W_NonConvectiveBulletText(self):
        reportType = ''
        if self.hydrologicClause == "levee":
            reportType = "a levee on the |* riverName *| at |* floodLocation *| failed causing flash flooding of immediately surrounding areas"
        elif self.hydrologicClause == "floodgate":
            reportType = "the floodgates on the |* damOrLeveeName *| were opened causing flash flooding downstream on the |* riverName *|"
        elif self.hydrologicClause == "glacier":
            reportType = "a glacier-dammed lake at |* floodLocation *| is rapidly releasing large quantities of impounded water resulting in flash flooding |* downstreamLocation *|"
        elif self.hydrologicClause == "icejam":
            reportType = "an ice jam on the |* riverName *| at |* floodLocation *| broke causing flash flooding downstream"
        elif self.hydrologicClause == "rain":
            reportType = "rain falling on existing snowpack was generating flash flooding from excessive runoff"
        elif self.hydrologicClause == "snowMelt":
            reportType = "extremely rapid snowmelt was occurring and generating flash flooding"
        elif self.hydrologicClause == "volcano":
            reportType = "activity of the |* volcanoName *| volcano was causing rapid snowmelt on its slopes and generating flash flooding"
        elif self.hydrologicClause == "volcanoLahar":
            reportType = "activity of the |* volcanoName *| volcano was causing rapid melting of snow and ice on the mountain. This will result in a torrent of mud...ash...rock and hot water to flow down the mountain through |*downstreamLocation *| and generate flash flooding"
        elif self.hydrologicClause == "dam":
            reportType = "the |* damOrLeveeName *| failed causing flash flooding downstream on the |* riverName *|"
        elif self.hydrologicClause == "siteImminent":
            reportType = "the imminent failure of |* damOrLeveeName *|"
        elif self.hydrologicClause == "siteFailed":
            reportType = "the failure of |* damOrLeveeName *| causing flash flooding downstream on the |* riverName *|"
        else:
            reportType = "excessive rain causing flash flooding was occurring over the warned area"

        return self.getSourceText() + reportType + "."

###############################################################################

    def get_FF_W_ConvectiveBulletText(self, burnScarTemplate=False):
        snowMelt = ""
        if self.immediateCause == "RS":
            snowMelt = " Rapid snowmelt is also occurring and will add to the flooding."

        report = ""
        if self.type == "thunderEvent":
            report = "thunderstorms producing heavy rain"
            if self.source == "satelliteGaugesSource" and self.followUp:
                report = "that thunderstorms were producing heavy rain"
        else: # Heavy Rain
            report = "heavy rain"
            if self.source in ["dopplerGaugesSource", "satelliteGaugesSource", "gaugesSource"]:
                report = "heavy rain falling"
                if self.followUp:
                    report = "that heavy rain was falling"

        # Use the floodLocation if given, otherwise use default text
        linkingPreposition = " in "
        if burnScarTemplate:
            # Use burnScarName instead of floodLocation
            floodLocation = self.hazardDict.get("burnScarName", None)
            if floodLocation:
                floodLocation += " Burn Scar"
                linkingPreposition = " over the "
        else:
            floodLocation = self.hazardDict.get("floodLocation", None)
        if floodLocation:
            report += linkingPreposition + floodLocation + "."
        else:
            report += " across the warned area."

        return self.getSourceText() + report + self.rainSoFar + " " + self.isExpected() + snowMelt

###############################################################################

    def get_FF_W_BurnScarBulletText(self):
        result = ""
        if self.debrisFlow == "debrisFlowBurnScar":
            drainage = "|* debrisBurnScarDrainage *|."
            try :
                burnDrainage = \
                   self.burnScarMetaData["scenarios"][self.scenario]["burnDrainage"]
                i = burnDrainage.lower().find(" include ")
                if i>0 :
                    drainage = burnDrainage[i+9:]
            except :
                errmsg = "\nWARNING: " + \
                         "Burn scar in mapdata.burnscararea SQL table with no" + \
                         " associated metadata in BurnScarMetaData.py\n\n"
                sys.stderr.write(errmsg)
                pass
            result += "\n\nExcessive rainfall over the burn scar will result in debris flow moving through the "
            # Test data sets we have received so far say 'drainage' will always have a period at the end.
            result += drainage
            result += " The debris flow can consist of rock...mud...vegetation and other loose materials."
        elif self.debrisFlow == "debrisFlowMudSlide":
            result += "\n\nExcessive rainfall over the warning area will cause mud slides near steep terrain. The mud slide can consist of rock...mud...vegetation and other loose materials."
        return result

###############################################################################

    def get_FA_Y_BulletText(self):
        # Use the floodLocation if given, otherwise use default text
        floodLocation = self.hazardDict.get("floodLocation", None)
        if floodLocation:
            floodLocation = " in " + floodLocation
        else:
            floodLocation = " in the advisory area"

        cause = "heavy rain"
        if self.immediateCause == "SM":
            cause = "snowmelt"
        elif self.immediateCause == "RS":
            cause = "heavy rain and snowmelt"
            if self.followUp:
                cause = "rain and snowmelt"
        elif self.immediateCause == "IJ":
            cause = "an ice jam"
        elif self.immediateCause == "IC":
            cause = "an ice jam and heavy rain"
        elif self.immediateCause == "DR":
            cause = "a dam floodgate release"

        causeText = "will cause "
        if self.flood:
            causeText = "is causing "

        advType = ""
        if self.advisoryType == "smallStreams":
            advType = " This " + causeText + "small stream flooding."
        elif self.advisoryType == "urbanAreasSmallStreams":
            advType = " This " + causeText + "urban and small stream flooding."
        elif self.advisoryType == "arroyoSmallStreams":
            advType = " This " + causeText + "arroyo and small stream flooding."

        whenPhrase = "will cause"
        if self.flood:
            whenPhrase = "have already caused"

        report2 = advType
        if self.optionalSpecificType == "rapidRiverRises":
            report2 += " Rapid river rises " + whenPhrase + " " + self.floodText + floodLocation + "."
        elif self.optionalSpecificType ==  "poorDrainage":
            report2 += " Overflowing poor drainage areas " + whenPhrase + " " + self.floodText + floodLocation + "."

        report = self.getSourceText()
        if self.immediateCause == "DR":
            report = "Flooding will result" + floodLocation + " from an upstream dam floodgate release."

        elif self.source in ["dopplerSource", "dopplerGaugesSource"] and self.immediateCause == "SM" and self.followUp == False:
            report = "Rapid snowmelt " + causeText + self.floodText + "." + report2
        elif self.source in ["dopplerSource", "dopplerGaugesSource"] and self.immediateCause == "RS" and self.followUp == False:
            report = "Rain and snowmelt " + causeText + self.floodText + "." + report2
        elif self.source in ["dopplerSource", "dopplerGaugesSource", "gaugesSource"]:
            report += cause
            if self.type == "thunderEvent":
                report += " due to thunderstorms"
            report += "."
            if report2:
                report += report2
            else:
                report += " " + self.isExpected(floodLocation)

        elif self.source in ["trainedSpottersSource", "localLawEnforcementSource", "emergencyManagementSource", "publicSource"]:
            report += cause + floodLocation
            if self.type == "thunderEvent":
                report += " due to thunderstorms"
            report += "."
            if report2:
                report += report2
            else: 
                report += " " + self.isExpected()

        elif self.source == "satelliteSource":
            report += cause
            if self.type == "thunderEvent":
                report += " from thunderstorms"
            report += floodLocation + "."
            if report2:
                report += report2
            else: 
                report += " " + self.isExpected()

        return report + self.rainSoFar

###############################################################################

    def get_FA_W_BulletText(self):
        # Use the floodLocation if given, otherwise use default text
        floodLocation = self.hazardDict.get("floodLocation", None)
        if floodLocation:
            floodLocation = " " + floodLocation
        else:
            floodLocation = " the warning area"

        report = self.getSourceText()
        if self.followUp:
            floodReason = ""
            if self.immediateCause == "SM":
                floodReason = " Rapid snowmelt is occurring and will continue to cause flooding."
            elif self.immediateCause == "RS":
                floodReason = " Rapid snowmelt is also occurring and will add to the flooding."
            elif self.immediateCause == "IJ":
                floodReason = " An ice jam is occurring and will continue to cause flooding."
            elif self.immediateCause == "IC":
                floodReason = " Flooding due to an ice jam and heavy rain will continue."
            elif self.immediateCause == "DM":
                floodReason = " Flooding due to a levee failure will continue."
            elif self.immediateCause == "DR":
                floodReason = " Flooding due to a dam floodgate release will continue."
            elif self.immediateCause == "GO":
                floodReason = " Flooding due to a glacier-dammed lake outburst will continue."

            if self.type == "thunderEvent":
                report += "slow moving thunderstorms with very heavy rainfall across the warned area."
            elif self.type == "rainEvent":
                report += "an area of very heavy rainfall across the warned area."
            elif self.genericReason:
                report += self.genericReason + "."
            report += self.rainSoFar
            report += floodReason
        else:
            if self.genericReason:
                report += self.genericReason + ". " + self.isExpected(" in" + floodLocation)
            elif self.type == "thunderEvent":
                if self.source in ["dopplerSource", "dopplerGaugesSource"]:
                    report += "thunderstorms with heavy rain. " + self.isExpected(" in" + floodLocation)
                else:
                    report += "thunderstorms with heavy rain over" + floodLocation + ". " + self.isExpected()
            else: # rainEvent
                if self.source in ["dopplerSource", "dopplerGaugesSource"]:
                    report += "that heavy rain was falling over the area. " + self.isExpected(" in" + floodLocation)
                else:
                    report += "heavy rain in" + floodLocation + ". " + self.isExpected()
            report += self.rainSoFar
        return report

###############################################################################

    def getRainSoFar(self, hazardDict):
        result = ""
        rainAmt = hazardDict.get("rainAmt");
        if rainAmt == "rainKnown":
            rainSoFarLowerBound = hazardDict.get("rainSoFarLowerBound")
            rainSoFarUpperBound = hazardDict.get("rainSoFarUpperBound")
            rainLower = "{:2.1f}".format(rainSoFarLowerBound)
            rainUpper = "{:2.1f}".format(rainSoFarUpperBound)
            # Remove trailing zeros and decimal place if not needed
            rainLower = rainLower.rstrip('0').rstrip('.') if '.' in rainLower else rainLower
            rainUpper = rainUpper.rstrip('0').rstrip('.') if '.' in rainUpper else rainUpper
            rainText = " of rain have fallen."
            inchText = " inches"
            if rainSoFarUpperBound == 1.0:
                inchText = " inch"
                rainText = " of rain has fallen."
            if rainSoFarLowerBound == 0.0 and rainSoFarUpperBound == 0.0:
                return result
            elif rainSoFarLowerBound == 0.0 or rainSoFarLowerBound == rainSoFarUpperBound:
                result = " Up to " + rainUpper + inchText + rainText
            elif rainSoFarUpperBound == 0.0:
                result = " Up to " + rainLower + inchText + rainText
            else:
                result = " Between " + rainLower + " and " + rainUpper + inchText + rainText
        return result

###############################################################################

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

