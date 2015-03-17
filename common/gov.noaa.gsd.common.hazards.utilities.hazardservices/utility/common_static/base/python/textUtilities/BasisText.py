'''
    Description: Maps hazard metadata to the Basis bullet of products
    Focal points can change the bullet text by locally changing the behavior of the 
    methods or including the main getBulletText method.  You will notice references
    to variables such as #damOrLeveeName#.  These denote metadata parameters included
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
    @author Daniel.S.Schaffer@noaa.gov
'''
###############################################################################
import os

class BasisText(object):
    def buildSourceBulletTexts(self):
        sourceBulletTexts = {}
        sourceBulletTexts["dopplerSource"] = "Doppler radar indicated "
        sourceBulletTexts["dopplerGaugesSource"] = "Doppler radar and automated gauges indicated "
        sourceBulletTexts["trainedSpottersSource"] = "Trained weather spotters reported "
        sourceBulletTexts["publicSource"] = "The public reported "
        sourceBulletTexts["localLawEnforcementSource"] = "Local law enforcement reported "
        sourceBulletTexts["emergencyManagementSource"] = "Emergency management reported "
        sourceBulletTexts["satelliteSource"] = "Satellite estimates indicate "
        sourceBulletTexts["satelliteGaugesSource"] = "Satellite estimates and rain gauge data indicate "
        sourceBulletTexts["gaugesSource"] = "Gauge reports "
        sourceBulletTexts["countySource"] = "County dispatch reported "
        sourceBulletTexts["corpsOfEngineersSource"] = "Corps of Engineers reported "
        sourceBulletTexts["damOperatorSource"] = "Dam operators reported "
        sourceBulletTexts["bureauOfReclamationSource"] = "Bureau of Reclamation reported "
        sourceBulletTexts["civilAirPatrolSource"] = "Civil Air Patrol reported "
        sourceBulletTexts["alaskaVolcanoSource"] = "Alaska Volcano Observatory reported "
        sourceBulletTexts["cascadesVolcanoSource"] = "Cascades Volcano Observatory reported "
        return sourceBulletTexts
    
    ###############################################################################
    def get_FF_W_NonConvectiveBulletText(self, identifier, sourceBulletText):
        methodName = "self.get_" + identifier.get("hydrologicCause") + "_bulletText"
        exec "result = " + methodName + "(sourceBulletText)"
        return result
    
    def get_dam_bulletText(self, sourceBulletText):
        return sourceBulletText + "the #damOrLeveeName# failed causing flash flooding downstream on the #riverName#."
    
    def get_levee_bulletText(self, sourceBulletText):
        return sourceBulletText + "a levee on the #riverName# at #floodLocation# failed causing flash flooding of immediately surrounding areas."
    
    def get_floodgate_bulletText(self, sourceBulletText):
        return sourceBulletText + "the floodgates on the #damOrLeveeName# were opened causing flash flooding downstream on the #riverName#."
    
    def get_glacier_bulletText(self, sourceBulletText):
        return sourceBulletText + "a glacier at #floodLocation# has melted...releasing large quantities of impounded water and causing flash flooding #downstreamLocation#."
    
    def get_icejam_bulletText(self, sourceBulletText):
        return sourceBulletText + "an ice jam on the #riverName# at #upstreamLocation# broke causing flash flooding downstream."
    
    def get_snowMelt_bulletText(self, sourceBulletText):
        return sourceBulletText + "rain falling on existing snowpack was generating flash flooding from excessive runoff."
    
    def get_volcano_bulletText(self, sourceBulletText):
        return sourceBulletText + "activity of the #volcanoName# was causing rapid snowmelt on its slopes and generating flash flooding."
    
    def get_volcanoLahar_bulletText(self, sourceBulletText):
        return sourceBulletText + "activity of the #volcanoName# was causing rapid melting of snow and ice on the mountain. This will result in a torrent of mud...ash...rock and hot water to flow down the mountain through #downstreamLocation# and generate flash flooding."
    
    def get_siteImminent_bulletText(self, sourceBulletText):
        return sourceBulletText + "the imminent failure of #damOrLeveeName#. "
    
    def get_siteFailed_bulletText(self, sourceBulletText):
            return sourceBulletText + "the failure of #damOrLeveeName#. "
    
    ###############################################################################
    def get_FA_W_BulletText(self, identifier, sourceBulletText):
        if identifier.get("eventType") == "genericFlooding":
           methodName = "self.get_FA_W_genericFlooding_bulletText"
           exec "result = " + methodName + "(sourceBulletText, identifier['genericFloodReasoning'])"
        else:
            methodName = "self.get_FA_W_" + identifier.get("source") + "_" + identifier.get("eventType") + "_bulletText"
            exec "result = " + methodName + "(sourceBulletText)"
        return result
    
    def get_FA_W_dopplerSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms producing heavy rain which will cause flooding."
    
    def get_FA_W_dopplerSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain that will cause flooding."
    
    def get_FA_W_dopplerSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain that will cause flooding."
    
    def get_FA_W_dopplerGaugesSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms with heavy rain over the area. That rain will cause flooding."
    
    def get_FA_W_dopplerGaugesSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "that heavy rain was falling over the area. That heavy rain will cause flooding."
    
    def get_FA_W_dopplerGaugesSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "that heavy rain was falling over the area. That heavy rain will cause flooding."
    
    def get_FA_W_trainedSpottersSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation# due to thunderstorms that will cause flooding."
    
    def get_FA_W_trainedSpottersSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation# that will cause flooding."
    
    def get_FA_W_trainedSpottersSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "flooding in #floodLocation#."
    
    def get_FA_W_publicSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms with heavy rain in #floodLocation#. The heavy rain will cause flooding."
    
    def get_FA_W_publicSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation#. That heavy rain will cause flooding."
    
    def get_FA_W_publicSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "flooding in #floodLocation#."
    
    def get_FA_W_localLawEnforcementSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms with heavy rain over #floodLocation# that will cause flooding."
    
    def get_FA_W_localLawEnforcementSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation# that will cause flooding."
    
    def get_FA_W_localLawEnforcementSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation# that will cause flooding."
    
    def get_FA_W_emergencyManagementSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms with heavy rain in #floodLocation#. The heavy rain will cause flooding."
    
    def get_FA_W_emergencyManagementSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation#. The heavy rain will cause flooding."
    
    def get_FA_W_emergencyManagementSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "flooding in #floodLocation#."
    
    def get_FA_W_satelliteSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms with heavy rain in #floodLocation#. That heavy rain will cause flooding."
    
    def get_FA_W_satelliteSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation#. That heavy rain will cause flooding."
    
    def get_FA_W_satelliteSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain in #floodLocation#. That heavy rain will cause flooding."
    
    def get_FA_W_satelliteGaugesSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms with heavy rain that will cause flooding in the warning area."
    
    def get_FA_W_satelliteGaugesSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain that will cause flooding in the warning area."
    
    def get_FA_W_satelliteGaugesSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain that will cause flooding in the warning area."
    
    def get_FA_W_gaugesSource_thunderEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "thunderstorms with heavy rain that will cause flooding in the warning area."
    
    def get_FA_W_gaugesSource_rainEvent_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain that will cause flooding in the warning area."
    
    def get_FA_W_gaugesSource_flooding_bulletText(self, sourceBulletText):
        return sourceBulletText + "heavy rain that will cause flooding in the warning area."
    
    def get_FA_W_genericFlooding_bulletText(self, sourceBulletText, reasoning):
        return sourceBulletText + reasoning
    
    ###############################################################################
    def get_FA_Y_BulletText(self, identifier, sourceBulletText):
    
        immediateCauseBulletChoices = {
            "ER":"heavy rain ",
            "SM":"snow melt ",
            "RS":"heavy rain and snow melt ",
            "IJ":"an ice jam ",
            "IC":"an ice jam and heavy rain ",
            "DR":"a dam floodgate release " 
        }
    
        advisoryTypeChoices = {
            "generalMinorFlooding":"minor ",
            "smallStreams":"small stream ",
            "urbanAreasSmallStreams":"urban and small stream ",
            "arroyoSmallStreams":"arroyo and small stream ",
            "hydrologic":"minor "
        }
    
        source = identifier.get("source")
        if source == "dopplerGaugesSource":
            source = "dopplerSource"
        elif source != "satelliteSource":
            source = "inLocation"
        immediateCauseBulletText = immediateCauseBulletChoices.get(identifier.get("immediateCause"))
        advisoryTypeBulletText = advisoryTypeChoices.get(identifier.get("advisoryType"))
        methodName = "self.get_FA_Y_" + source + "_" + identifier.get("eventType") + "_bulletText"
        exec "result = " + methodName + "(sourceBulletText, immediateCauseBulletText,advisoryTypeBulletText)"
        return result
    ############################################################################
    def get_FA_Y_dopplerSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return sourceBulletText + immediateCauseBulletText + "due to thunderstorms.  This will cause " + advisoryTypeBulletText + "flooding in the advisory area."
    
    def get_FA_Y_dopplerSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return sourceBulletText + immediateCauseBulletText + "that will cause " + advisoryTypeBulletText + "flooding in the advisory area."
    
    def get_FA_Y_dopplerSource_minorFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return self.get_FA_Y_dopplerSource_rainEvent_bulletText(sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText)
    
        # TBD.  Waiting for Evan/Phil to provide help on rapidRiverRises and poorDrainage
    
    ############################################################################
    def get_FA_Y_inLocation_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return sourceBulletText + immediateCauseBulletText + "in #floodLocation# due to thunderstorms.  This will cause " + advisoryTypeBulletText + "flooding."
    
    def get_FA_Y_inLocation_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return sourceBulletText + immediateCauseBulletText + "in #floodLocation# that will cause " + advisoryTypeBulletText + "flooding."
    
    def get_FA_Y_inLocation_minorFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return self.get_FA_Y_inLocation_rainEvent_bulletText(sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText)
    
    ############################################################################
    def get_FA_Y_satelliteSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return sourceBulletText + immediateCauseBulletText + "from thunderstorms over #floodLocation# that will cause " + advisoryTypeBulletText + "flooding."
    
    def get_FA_Y_satelliteSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText, advisoryTypeBulletText):
        return sourceBulletText + immediateCauseBulletText + "in #floodLocation# that will cause " + advisoryTypeBulletText + "flooding."
    
    def get_FA_Y_satelliteSource_minorFlooding_bulletText(self, sourceBulletText, immediateCause, advisoryTypeBulletText):
        return self.get_FA_Y_satelliteSource_rainEvent_bulletText(sourceBulletText, immediateCause, advisoryTypeBulletText)
    
    ###############################################################################
    def get_FF_W_ConvectiveBulletText(self, identifier, sourceBulletText):
    
        immediateCauseBulletChoices = {
            "ER":"",
            "RS":" Rapid snow melt is also occurring and will add to the flooding."
        }
    
        immediateCauseBulletText = immediateCauseBulletChoices.get(identifier.get("immediateCause"))
        methodName = "self.get_FF_W_" + identifier.get("source") + "_" + identifier.get("eventType") + "_bulletText"
        exec "result = " + methodName + "(sourceBulletText, immediateCauseBulletText)"
        return result
    
    def get_FF_W_dopplerSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "thunderstorms producing heavy rain across the warned area. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_dopplerSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain across the warned area. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_dopplerSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain across the warned area. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_dopplerGaugesSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "thunderstorms producing heavy rain over the area. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_dopplerGaugesSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "that heavy rain was falling over the area. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_dopplerGaugesSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain was falling over the area. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_trainedSpottersSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "thunderstorms producing heavy rain in #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_trainedSpottersSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain in #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_trainedSpottersSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "flash flooding in #floodLocation#. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_publicSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "thunderstorms producing heavy rain in #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_publicSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain in #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_publicSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "flash flooding in #floodLocation#. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_localLawEnforcementSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "thunderstorms producing heavy rain over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_localLawEnforcementSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_localLawEnforcementSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "flash flooding is occurring in #floodLocation#. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_emergencyManagementSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain due to thunderstorms over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_emergencyManagementSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain in #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_emergencyManagementSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "flash flooding in #floodLocation#. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_satelliteSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain due to thunderstorms over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_satelliteSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_satelliteSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain over #floodLocation#. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_satelliteGaugesSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain due to thunderstorms over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_satelliteGaugesSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_satelliteGaugesSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain over #floodLocation#. Flash flooding is already occurring." + immediateCauseBulletText
    
    def get_FF_W_gaugesSource_thunderEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain due to thunderstorms over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_gaugesSource_rainEvent_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain over #floodLocation#. Flash flooding is expected to begin shortly." + immediateCauseBulletText
    
    def get_FF_W_gaugesSource_flashFlooding_bulletText(self, sourceBulletText, immediateCauseBulletText):
        return sourceBulletText + "heavy rain over #floodLocation#. Flash flooding is already occurring." + immediateCauseBulletText

    def get_FF_W_BurnScarBulletText(self, identifier):
        debrisFlow = identifier.get("debrisFlow")
        result = ""
        if debrisFlow == "debrisFlowBurnScar":
            result += "\n\nExcessive rainfall over the burn scar will result in debris flow moving through the #debrisBurnScarDrainage#. The debris flow can consist of rock...mud...vegetation and other loose materials."
        elif debrisFlow == "debrisFlowMudSlide":
            result += "\n\nExcessive rainfall over the warning area will cause mud slides near steep terrain. The mud slide can consist of rock...mud...vegetation and other loose materials."
        return result
###############################################################################
    def rainSoFar(self, identifier):
        result = ""
        rainAmt = identifier.get("rainAmt");
        if rainAmt == "rainKnown":
            rainSoFarLowerBound = identifier.get("rainSoFarLowerBound")
            rainSoFarUpperBound = identifier.get("rainSoFarUpperBound")
            rainLower = "{:2.1f}".format(rainSoFarLowerBound)
            rainUpper = "{:2.1f}".format(rainSoFarUpperBound)
            rainText = " inches of rain have fallen. "
            if rainLower == 0.0 or rainLower == rainUpper:
                result = "Up to " + rainUpper + rainText
            else:
                result = " Between " + rainLower + " and " + rainUpper + rainText
        return result
            
###############################################################################
    def getBulletText(self, hazardType, identifier):
        sourceBulletTexts = self.buildSourceBulletTexts()
        sourceBulletText = sourceBulletTexts.get(identifier.get("source"))
        if hazardType == "FF.W.NonConvective":
            result = self.get_FF_W_NonConvectiveBulletText(identifier, sourceBulletText)
    
        elif hazardType == "FF.W.Convective":
            result = self.get_FF_W_ConvectiveBulletText(identifier, sourceBulletText)
    
        elif hazardType == "FF.W.BurnScar":
            # First part is same as Convective
            result = self.get_FF_W_ConvectiveBulletText(identifier, sourceBulletText)
            
            result += self.get_FF_W_BurnScarBulletText(identifier)
    
        elif hazardType == "FA.W":
            result = self.get_FA_W_BulletText(identifier, sourceBulletText)
    
        elif hazardType == "FA.Y":
            result = self.get_FA_Y_BulletText(identifier, sourceBulletText)
    
        else:
            result = "Unexpected hazardType " + hazardType
        result += self.rainSoFar(identifier)
        if hazardType != 'FA.Y':
            result = "..." + result
        return result
    
    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

