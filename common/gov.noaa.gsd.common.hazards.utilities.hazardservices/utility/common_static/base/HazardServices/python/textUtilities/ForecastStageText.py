'''
    Description: Creates the forecastStageBullet text. 
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 2015       4375    Tracy Hansen      Initial creation
    Feb 2015       6599    Robert.Blum       Changed to new style class
    Mar 2015       6963    Robert.Blum       Stage values have precision of 2 and 
                                             times have minutes formatted to '00'.
    Apr 2015       7271    Chris.Golden      Changed to use MISSING_VALUE
                                             constant.
    Apr 2015       7579    Robert.Blum       Updated variable names.
    May 19, 2015   6562    Chris.Cody        Implemented "work-around" code. This needs to be changed under a different issue.
    May 26, 2015   7634    Chris.Cody        Correct improper forcast bullet string generation
    Jun 25, 2015   ????    Chris.Cody        Correct for missing first and second Rise Fall Time error
    Jun 25, 2015    8313   Benjamin.Phillippe Fixed issued event loading when time is changed
    Jul 23, 2015    9643   Robert.Blum       All rounding is now done in one common place so that it can 
                                             be easily overridden by sites.
    Sep 09, 2015    10263  Robert Blum       No Forecast bullet if there is no forecast stage.
    Feb 23, 2016    11901  Robert.Blum       Replacing date/time strings with day of week and time of day wording.
    Mar 02, 2016    11898  Robert.Blum       Complete rework to correctly convert RiverPro's templates.
    May 05, 2016    15584  Kevin.Bisanz      Convert flow based values to stage in replaceVariables(..).
    May 24, 2016    15584  Kevin.Bisanz      Do not convert flow based values
                                             to stage in replaceVariables(..);
                                             instead use values which have been
                                             based on primary PE.
    Jun 23, 2016    16045  Kevin.Bisanz      Forecast text of "flood stage" or
                                             "flood flow" based on primary PE
                                             instead of hard coded.
    Jul 28, 2016    20385  Ben.Phillippe     When no forecast present, changed wording to "A forecast is not
                                             available at this time"
    Aug 16, 2016    15017  Robert.Blum       Updates to variable names.
    Oct 26, 2016    22580  Mark.Fegan        Match Forecast Text on replaced products to new product.

    @author Tracy.L.Hansen@noaa.gov
'''
import collections, os, types
from RiverForecastUtils import RiverForecastUtils
from datetime import datetime
from HazardConstants import MISSING_VALUE
from TextProductCommon import  TextProductCommon

MISSING_VALUE_STRING = str(MISSING_VALUE)

class ForecastStageText(object):

    def __init__(self):
        self._riverForecastUtils = RiverForecastUtils()

    def getForecastStageText(self, hazardDict, timeZones, issueTime, action, pil):
        self.timeZones = timeZones
        self.issueTime = issueTime
        self.action = action
        self.pil = pil
        forecast_description = self.getForecastDescription(hazardDict)
        return forecast_description

    def getVariables(self, hazardDict):
        '''
            Pulls all the needed variables out of the hazardDict. It also converts date/time 
            variables to the needed time of day phrasing and handles rounding of stage values.
        '''
        self.tpc = TextProductCommon()
        self.observedStage = hazardDict.get('observedStage', MISSING_VALUE)
        self.floodStage = hazardDict.get('floodStage', MISSING_VALUE) 
        self.stageTrend = hazardDict.get('stageTrend', MISSING_VALUE)
        self.phen = hazardDict.get('phen')
        self.sig = hazardDict.get('sig')
        self.primaryPE = hazardDict.get('primaryPE')
        self.pointID = hazardDict.get('pointID')
        self.replacedBy = hazardDict.get('replacedBy')


        # ForecastCrestStage
        self.forecastCrestStage = hazardDict.get('forecastCrestStage', MISSING_VALUE)
        if self.forecastCrestStage and self.forecastCrestStage != MISSING_VALUE:
            self.forecastCrestStage_str = self.tpc.roundFloat(self.forecastCrestStage, returnString=True)
        else:
            self.forecastCrestStage_str = None

        self.maxObsStg24 = hazardDict.get('max24HourObservedStage', MISSING_VALUE)
        if self.maxObsStg24 and self.maxObsStg24 != MISSING_VALUE:
            self.maxObsStg24_str = self.tpc.roundFloat(self.maxObsStg24, returnString=True)
        else:
            self.maxObsStg24_str = None

        # Max
        self.maxForecastStage = hazardDict.get('maxForecastStage', MISSING_VALUE)
        if self.maxForecastStage and self.maxForecastStage != MISSING_VALUE:
            self.maxForecastStage_str = self.tpc.roundFloat(self.maxForecastStage, returnString=True)
        else:
            self.maxForecastStage_str = None

        self.fcstRiseFSTime = hazardDict.get('forecastRiseAboveFloodStageTime_ms', MISSING_VALUE) 
        if self.isValidTime(self.fcstRiseFSTime):
            self.fcstRiseFSTime_str = self.tpc.getRiverProTimePhrase(self.issueTime,
                                                                                        self.fcstRiseFSTime, self.timeZones[0])
        else:
            self.fcstRiseFSTime_str = None

        self.fcstFallFSTime = hazardDict.get('forecastFallBelowFloodStageTime_ms', MISSING_VALUE)
        if self.isValidTime(self.fcstFallFSTime):
            self.fcstFallFSTime_str = self.tpc.getRiverProTimePhrase(self.issueTime,
                                                                                        self.fcstFallFSTime, self.timeZones[0])
        else:
            self.fcstFallFSTime_str = None

        self.forecastCrestTime = hazardDict.get('forecastCrestTime', MISSING_VALUE)
        if self.isValidTime(self.forecastCrestTime):
            self.forecastCrestTime_str = self.tpc.getRiverProTimePhrase(self.issueTime, self.forecastCrestTime, self.timeZones[0])
        else:
            self.forecastCrestTime_str = None

        self.maxForecastTime_str = hazardDict.get('maxForecastTime_str', None)
        if self.maxForecastTime_str is None:
            maxForecastTime = hazardDict.get('maxForecastTime', MISSING_VALUE)
            if maxForecastTime:
                self.maxForecastTime_str = self.tpc.getRiverProTimePhrase(self.issueTime, maxForecastTime, self.timeZones[0])

        self.stageFlowUnits = hazardDict.get('stageFlowUnits', MISSING_VALUE_STRING)

        self.specValue =  hazardDict.get('specValue', MISSING_VALUE_STRING) 
        self.specTime = self.convertTime(hazardDict.get('specTime', MISSING_VALUE)) 
        if self.specValue and self.specValue != MISSING_VALUE_STRING:
            self.specValue_str = self.tpc.roundFloat(self.specValue, returnString=True)
        else:
            self.specValue_str = None

        self.HT0FFXNext = hazardDict.get('HT0FFXNext', MISSING_VALUE_STRING)
        self.HT0FFXNextTime = self.convertTime(hazardDict.get('HT0FFXNextTime'))
        if self.HT0FFXNext and self.HT0FFXNext != MISSING_VALUE_STRING:
            self.HT0FFXNext = self.tpc.roundFloat(self.HT0FFXNext, returnString=True)
            self.HT0FFXNext_float = float(self.HT0FFXNext)
        else:
            self.HT0FFXNext = None
            self.HT0FFXNext_float = MISSING_VALUE

        self.HP0FFXNext = hazardDict.get('HP0FFXNext', MISSING_VALUE_STRING)
        self.HP0FFXNextTime = self.convertTime(hazardDict.get('HP0FFXNextTime'))
        if self.HP0FFXNext and self.HP0FFXNext != MISSING_VALUE_STRING:
            self.HP0FFXNext_float = float(self.HP0FFXNext)
            self.HP0FFXNext = self.tpc.roundFloat(self.HP0FFXNext, returnString=True)
        else:
            self.HP0FFXNext = None
            self.HP0FFXNext_float = MISSING_VALUE

        self.HG0FFXNext =  hazardDict.get('HG0FFXNext', MISSING_VALUE_STRING)
        self.HG0FFXNextTime = self.convertTime(hazardDict.get('HG0FFXNextTime'))
        if self.HG0FFXNext and self.HG0FFXNext != MISSING_VALUE_STRING:
            self.HG0FFXNext = self.tpc.roundFloat(self.HG0FFXNext, returnString=True)
            self.HGOFFXNEXT_float = float(self.HG0FFXNext)
        else:
            self.HG0FFXNext = None
            self.HGOFFXNEXT_float = MISSING_VALUE

        self.QR0FFXNext = hazardDict.get('QR0FFXNext', MISSING_VALUE_STRING)
        self.QR0FFXNextTime = self.convertTime(hazardDict.get('QR0FFXNextTime'))
        if self.QR0FFXNext and self.QR0FFXNext != MISSING_VALUE_STRING:
            self.QR0FFXNext = self.tpc.roundFloat(self.QR0FFXNext, returnString=True)
            self.QR0FFXNext_float = float(self.QR0FFXNext)
        else:
            self.QR0FFXNext = None
            self.QR0FFXNext_float = MISSING_VALUE

        self.obsRiseFSTime = hazardDict.get('obsRiseAboveFSTime', MISSING_VALUE)
        if self.isValidTime(self.obsRiseFSTime):
            self.obsRiseFSTime_str = self.tpc.getRiverProTimePhrase(self.issueTime, self.obsRiseFSTime, self.timeZones[0])
        else:
            self.obsRiseFSTime_str = None

        self.obsFallFSTime = hazardDict.get('obsFallBelowFSTime', MISSING_VALUE)
        if self.isValidTime(self.obsFallFSTime):
            self.obsFallFSTime_str = self.tpc.getRiverProTimePhrase(self.issueTime, self.obsFallFSTime, self.timeZones[0])
        else:
            self.obsFallFSTime_str = None

        self.floodFlow = hazardDict.get('floodFlow', MISSING_VALUE)

    def getForecastDescription(self, hazardDict):
        '''
        Determines which template method to call.
        
        @param hazardDict -- a dictionary with the riverforecast point data 
        '''
        self.getVariables(hazardDict)

        crestOnly = False
        if self.fcstFallFSTime_str == None and self.fcstRiseFSTime_str == None:
            crestOnly = True

        # determine if this product is being replaced
        replaced = True
        if not self.replacedBy:
            replaced = False

         # Determine which template method to use
        bulletText = ''
        if self.maxForecastStage == None or self.maxForecastStage == MISSING_VALUE:
            if self.pil == "FLW":
                bulletText = self.VTEC_FLW_NO_FCST()
            else:
                bulletText = self.VTEC_FLS_NO_FCST()

        elif self.observedStage == None or self.observedStage == MISSING_VALUE:
            if self.pil == "FLW":
                bulletText = self.VTEC_FLW_NO_OBS()
            else:
                bulletText = self.VTEC_FLS_NO_OBS()

        elif crestOnly:
            if self.pil == "FLW":
                bulletText = self.VTEC_FLW_CRESTONLY()
            else:
                bulletText = self.VTEC_FLS_CRESTONLY()

        elif self._riverForecastUtils.isPrimaryPeFlow(self.primaryPE) and \
        self.floodFlow != None and self.floodFlow != MISSING_VALUE:
            if self.QR0FFXNext and self.QR0FFXNextTime:
                if self.pil == "FLW":
                    bulletText = self.VTEC_FLW_FLOW()
                else:
                    bulletText = self.VTEC_FLS_FLOW()
            else:
                if self.pil == "FLW":
                    bulletText = self.VTEC_FLW_NONFFX_FLOW()
                else:
                    bulletText = self.VTEC_FLS_NONFFX_FLOW()

        elif self.sig == "Y" and not replaced:
           #bulletText = self.VTEC_FLOOD_ADVISORY()
            bulletText = self.VTEC_FLD_ADVISORY()

        elif self.sig == "A" and not replaced:
            bulletText = self.VTEC_FLOOD_WATCH()

        elif self.HP0FFXNext and self.HP0FFXNextTime:
            if self.pil == "FLW":
                bulletText = self.VTEC_FLW_HP()
            else:
                bulletText = self.VTEC_FLS_HP()

        elif self.HT0FFXNext and self.HT0FFXNextTime:
            if self.pil == "FLW":
                bulletText = self.VTEC_FLW_HT()
            else:
                bulletText = self.VTEC_FLS_HT()

        elif self.HG0FFXNext and self.HG0FFXNextTime:
            if self.pil == "FLW":
                bulletText = self.VTEC_FLW()
            else:
                bulletText = self.VTEC_FLS()
        else:
            if self.pil == "FLW":
                bulletText = self.VTEC_FLW_NONFFX()
            else:
                bulletText = self.VTEC_FLS_NONFFX()

        if bulletText:
            bulletText = self.replaceVariables(bulletText)
        return bulletText

    def replaceVariables(self, bulletText):
        '''
            Replaces framed variables in the bullet text with the actual values if they exist.
        '''
        stageFlowName = 'flood ' + self._riverForecastUtils.getStageFlowName(self.primaryPE)
        bulletText = bulletText.replace('|* StgFlowName *|', stageFlowName)

        if self.stageFlowUnits:
            bulletText = bulletText.replace('|* StgFlowUnits *|', self.stageFlowUnits)
        if self.maxObsStg24_str:
            bulletText = bulletText.replace('|* MaxObsStg24 *|', self.maxObsStg24_str)
        if self.maxForecastStage_str:
            bulletText = bulletText.replace('|* MaxFcstStg *|', self.maxForecastStage_str)
        if self.maxForecastTime_str:
            bulletText = bulletText.replace('|* MaxFcstTime *|', self.maxForecastTime_str)
        if self.fcstRiseFSTime_str:
            bulletText = bulletText.replace('|* FcstRiseFSTime *|', self.fcstRiseFSTime_str)
        if self.fcstFallFSTime_str:
            bulletText = bulletText.replace('|* FcstFallFSTime *|', self.fcstFallFSTime_str)
        if self.forecastCrestStage_str:
            bulletText = bulletText.replace('|* FcstCrestStg *|', self.forecastCrestStage_str)
        if self.forecastCrestTime_str:
            bulletText = bulletText.replace('|* FcstCrestTime *|', self.forecastCrestTime_str)
        if self.specValue_str:
            bulletText = bulletText.replace('|* SpecFcstStg *|', self.specValue_str)
        if self.specTime:
            bulletText = bulletText.replace('|* SpecFcstStgTime *|', self.specTime)

        if self.HT0FFXNext:
            bulletText = bulletText.replace('|* HT,0,FF,X,NEXT *|', self.HT0FFXNext)
        if self.HT0FFXNextTime:
            bulletText = bulletText.replace('|* HT,0,FF,X,NEXT,TIME *|', self.HT0FFXNextTime)
        if self.HG0FFXNext:
            bulletText = bulletText.replace('|* HG,0,FF,X,NEXT *|', self.HG0FFXNext)
        if self.HG0FFXNextTime:
            bulletText = bulletText.replace('|* HG,0,FF,X,NEXT,TIME *|', self.HG0FFXNextTime)
        if self.HP0FFXNext:
            bulletText = bulletText.replace('|* HP,0,FF,X,NEXT *|', self.HP0FFXNext)
        if self.HP0FFXNextTime:
            bulletText = bulletText.replace('|* HP,0,FF,X,NEXT,TIME *|', self.HP0FFXNextTime)
        if self.QR0FFXNext:
            bulletText = bulletText.replace('|* QR,0,FF,X,NEXT *|', self.QR0FFXNext)
        if self.QR0FFXNextTime:
            bulletText = bulletText.replace('|* QR,0,FF,X,NEXT,TIME *|', self.QR0FFXNextTime)

        bulletText = ' '.join(bulletText.split())
        return bulletText

    def isValidTime(self, time):
        if time and time != MISSING_VALUE:
            return True
        return False

    def convertTime(self, time):
        if time != MISSING_VALUE_STRING:
            # Convert to datetime then to millis
            date = datetime.strptime(time, '%Y-%m-%d %H:%M:%S.%f')
            epoch = datetime.utcfromtimestamp(0)
            delta = date - epoch
            result = delta.total_seconds() * 1000.0
            result = self.tpc.getRiverProTimePhrase(self.issueTime, result, self.timeZones[0])
            return result
        return None

    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()

###########################################################################################################
#
#    The below methods are direct conversions from RiverPro's roundup.tpl.BOX file.
#
###########################################################################################################

    def VTEC_FLW(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        elif self.observedStage < self.floodStage and self.HGOFFXNEXT_float > self.floodStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HG,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        elif self.observedStage < self.floodStage and self.maxForecastStage > \
        self.floodStage and self.HGOFFXNEXT_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        elif self.observedStage < self.floodStage and self.HGOFFXNEXT_float > \
        self.floodStage and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by |* HG,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        elif self.observedStage >= self.floodStage and self.maxForecastStage > \
        self.observedStage and self.HGOFFXNEXT_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        elif self.observedStage >= self.floodStage and self.HGOFFXNEXT_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HG,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        elif self.observedStage >= self.floodStage and self.HGOFFXNEXT_float > self.observedStage and \
        self.fcstFallFSTime != MISSING_VALUE and self.forecastCrestStage > self.observedStage:
            bulletstr = '''The river will continue rising to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HG,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        elif self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage and \
        self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        elif self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage and \
        self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        elif self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage and \
        self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        elif self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        elif self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr =  '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        elif self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        elif self.observedStage < self.floodStage and \
        self.HGOFFXNEXT_float > self.floodStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HG,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        elif self.observedStage < self.floodStage and self.maxForecastStage > \
        self.floodStage and self.HGOFFXNEXT_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        elif self.observedStage < self.floodStage and self.HGOFFXNEXT_float > \
        self.floodStage and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by |* HG,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        elif self.observedStage >= self.floodStage and self.maxForecastStage > \
        self.observedStage and self.HGOFFXNEXT_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        elif self.observedStage >= self.floodStage and self.HGOFFXNEXT_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HG,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        elif self.observedStage >= self.floodStage and self.HGOFFXNEXT_float > self.observedStage and \
        self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HG,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HG,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        elif self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage and \
        self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        elif self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage and \
        self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        elif  self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage and \
        self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        elif self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        elif self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast below flood stage/cancellation or expiration issued
        #
        elif (self.action == "CAN" or self.action == "EXP") and self.observedStage < self.floodStage and \
        self.maxForecastStage < self.floodStage and self.obsFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which never rose above flood stage and which has already crested
        #
        elif self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage <= self.maxObsStg24:
            bulletstr = '''The river crested below |* StgFlowName *| at |* MaxObsStg24 *| |* StgFlowUnits *|. The river will
            fall to |* SpecFcstStg *| |* StgFlowUnits *| by <SpecFcstStageTime>.'''

        #
        # Cancellation for a location which will not rise above flood stage but which is still rising
        #
        elif self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage > self.maxObsStg24:
            bulletstr = '''The river will crest below |* StgFlowName *| at |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLOOD_ADVISORY(self):
        bulletstr = ''
        #
        # FORECAST INFORMATION
        #
        if True:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLD_ADVISORY(self):
        bulletstr = ''
        #
        # Updated 04/21/09 by Mark Armstrong HSD
        #
        # This Flood Advisory template contains more information than its rather
        # simple predecessor. Slightly more complex forecast conditions have
        # been added. The largest change is to note cancellations based on
        # the need to issue a Flood Warning.
        #

        #
        # FORECAST INFORMATION
        #
        if self.observedStage == MISSING_VALUE and self.fcstRiseFSTime == MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Rising limb (but not to flood stage) for NEW, EXT, CON;
        #
        if self.action != "CAN" and self.action != "EXP" and self.maxForecastStage != MISSING_VALUE \
        and self.observedStage != MISSING_VALUE and self.maxForecastStage >= self.observedStage \
        and self.fcstRiseFSTime == MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.'''

        #
        # Falling limb (but not to flood stage) for NEW, EXT, CON;
        #
        if self.action != "CAN" and self.action != "EXP" and self.maxForecastStage != MISSING_VALUE \
        and self.observedStage != MISSING_VALUE and self.maxForecastStage < self.observedStage \
        and self.fcstRiseFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall.'''

        #
        # Rising above flood stage; a CAN is issued for the FL.Y, but an FL.W is going to be issued
        #
        if (self.action == "CAN" or self.action == "EXP") and self.fcstRiseFSTime != MISSING_VALUE:
            bulletstr = '''The river is forecast to go above |* StgFlowName *| by |* FcstRiseFSTime *|.'''

        #
        # Cancellation with falling limb forecast
        #
        if (self.action == "CAN" or self.action == "EXP" ) and self.maxForecastStage != MISSING_VALUE \
        and self.maxObsStg24 != MISSING_VALUE and self.maxForecastStage <= self.maxObsStg24:
            bulletstr = '''The river crested below |* StgFlowName *| at |* MaxObsStg24 *| |* StgFlowUnits *|. The river will
            continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        # Cancellation for a location which will not rise above flood stage but which is still rising
        #
        if (self.action == "CAN" or self.action == "EXP") and self.maxForecastStage > self.maxObsStg24 \
        and self.fcstRiseFSTime == MISSING_VALUE:
            bulletstr = '''The river will crest below |* StgFlowName *| at |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_NONFFX(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # New condition added 3/23/2010 to close gap in conditions
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage != self.forecastCrestStage \
        and self.maxForecastStage > self.observedStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodStage and self.forecastCrestStage > self.floodStage \
        and self.maxForecastStage == self.forecastCrestStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage and \
        self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage and \
        self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage == self.forecastCrestStage \
        and self.forecastCrestStage > self.observedStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. The river will fall below |* StgFlowName *| |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_NONFFX(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodStage and self.forecastCrestStage > self.floodStage \
        and self.maxForecastStage == self.forecastCrestStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage and \
        self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage == self.forecastCrestStage \
        and self.forecastCrestStage > self.observedStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. The river will fall below |* StgFlowName *| |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # New condition added 3/23/2010 to close gap in conditions
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage != self.forecastCrestStage \
        and self.maxForecastStage > self.observedStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *|.'''

        #
        # New Condition to allow for the steady trend, but non-MISSING_VALUE Fall Time
        # 3/26/2010
        #
        if self.observedStage >= self.floodStage and self.observedStage > self.maxForecastStage \
        and self.fcstFallFSTime != MISSING_VALUE and self.stageTrend == "steady":
            bulletstr = '''The river will gradually fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast below flood stage/cancellation or expiration issued
        #
        if (self.action == "CAN" or self.action == "EXP") and self.observedStage < self.floodStage \
        and self.maxForecastStage < self.floodStage and self.obsFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which never rose above flood stage and which has already crested
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxObsStg24 >= self.maxForecastStage:
            bulletstr = '''The river crested below |* StgFlowName *| at |* MaxObsStg24 *| |* StgFlowUnits *|. The river will
            continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which will not rise above flood stage but which is still rising
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage > self.maxObsStg24:
            bulletstr = '''The river will crest below |* StgFlowName *| with a value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_CRESTONLY(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed at or above flood stage/forecast higher than observed
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage:
            bulletstr = '''Rise to |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed at or above flood stage/forecast lower than observed
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage < self.observedStage:
            bulletstr = '''Fall to |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed at or above flood stage/forecast equal to observed
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage == self.observedStage:
            bulletstr = '''To remain near |* MaxFcstStg *| |* StgFlowUnits *| through |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_CRESTONLY(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed at or above flood stage/forecast higher than observed
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage:
            bulletstr = '''Rise to |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed at or above flood stage/forecast lower than observed
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage < self.observedStage:
            bulletstr = '''Fall to |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed at or above flood stage/forecast equal to observed
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage == self.observedStage:
            bulletstr = '''To remain near |* MaxFcstStg *| |* StgFlowUnits *| through |* MaxFcstTime *|.'''

        #
        # Observed below flood stage and max forecast below flood stage but greater than obs stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.maxForecastStage > self.observedStage:
            bulletstr = '''Remain below |* StgFlowName *| and rise to near |* MaxFcstStg *| |* StgFlowUnits *|
            |* MaxFcstTime *|.'''

        #
        # Observed below flood stage and max forecast below flood stage and below obs stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage and self.maxForecastStage <= self.observedStage:
            bulletstr = '''Remain below |* StgFlowName *| near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_NO_FCST(self):
        bulletstr = ''
        #
        # Forecast information
        #
        if self.maxForecastStage == MISSING_VALUE:
            bulletstr = '''A forecast is not available at this time. 
            This warning will remain in effect until the river falls below |* StgFlowName *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_NO_FCST(self):
        bulletstr = ''
        #
        # Forecast data is MISSING_VALUE
        #
        if self.maxForecastStage == MISSING_VALUE:
            bulletstr = '''A forecast is not available at this time.
            This warning will remain in effect until the river falls below |* StgFlowName *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_NO_OBS(self):
        bulletstr = ''
        #
        # Forecast information
        #
        if self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_NO_OBS(self):
        bulletstr = ''
        #
        # FORECAST INFORMATION
        #
        if self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE
        #
        if self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLOOD_WATCH(self):
        bulletstr = ''
        #
        # FORECAST INFORMATION
        #
        if self.action == "NEW" or self.action == "CON" or self.action == "EXT" and \
        self.fcstRiseFSTime != MISSING_VALUE:
            bulletstr = '''|* StgFlowName *| may be reached by |* FcstRiseFSTime *|.'''

        #
        if self.action == "CAN" or self.action == "EXP":
            bulletstr = '''The river is forecast to reach a maximum stage of |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_HP(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HP0FFXNext_float > self.floodStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HP,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage \
        and self.HP0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HP0FFXNext_float > self.floodStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by |* HP,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.HP0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HP0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HP,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HP0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE and self.forecastCrestStage > self.observedStage:
            bulletstr = '''The river will continue rising to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HP,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_HP(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HP0FFXNext_float > self.floodStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HP,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage \
        and self.HP0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HP0FFXNext_float > self.floodStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by |* HP,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.HP0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HP0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HP,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HP0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HP,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HP,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast below flood stage/cancellation or expiration issued
        #
        if ( self.action == "CAN" or self.action == "EXP" ) and self.observedStage < self.floodStage  \
        and self.maxForecastStage < self.floodStage and self.obsFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which never rose above flood stage and which has already crested
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage <= self.maxObsStg24:
            bulletstr = '''The river crested below |* StgFlowName *| at |* MaxObsStg24 *| |* StgFlowUnits *|. The river will
            continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which will not rise above flood stage, but which is still rising
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage > self.maxObsStg24:
            bulletstr = '''The river will crest below |* StgFlowName *| at <MaxObsStg> |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_HT(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HT0FFXNext_float > self.floodStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HT,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage \
        and self.HT0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HT0FFXNext_float > self.floodStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by |* HT,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.HT0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HT0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HT,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HT0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE and self.forecastCrestStage > self.observedStage:
            bulletstr = '''The river will continue rising to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HT,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_HT(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodStage and self.maxForecastStage == self.floodStage:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HT0FFXNext_float > self.floodStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HT,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodStage and self.maxForecastStage > self.floodStage \
        and self.HT0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodStage and self.HT0FFXNext_float > self.floodStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by |* HT,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage > self.observedStage \
        and self.HT0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HT0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HT,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.HT0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* HT,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* HT,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodStage and self.maxForecastStage <= self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # Observed below flood stage/forecast below flood stage/cancellation or expiration issued
        #
        if ( self.action == "CAN" or self.action == "EXP") and self.observedStage < self.floodStage \
        and self.maxForecastStage < self.floodStage and self.obsFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Cancellation for a location which never rose above flood stage and which has already crested
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage <= self.maxObsStg24:
            bulletstr = '''The river crested below |* StgFlowName *| at |* MaxObsStg24 *| |* StgFlowUnits *|. The river will
            continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which will not rise above flood stage, but which is still rising
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage > self.maxObsStg24:
            bulletstr = '''The river will crest below |* StgFlowName *| at <MaxObsStg> |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_NONFFX_FLOW(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage == self.floodFlow:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.forecastCrestStage > self.floodFlow \
        and self.maxForecastStage == self.forecastCrestStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage > self.floodFlow \
        and self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage > self.floodFlow \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage > self.observedStage \
        and self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage == self.forecastCrestStage \
        and self.forecastCrestStage > self.observedStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. The river will fall below |* StgFlowName *| |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_NONFFX_FLOW(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage == self.floodFlow:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.forecastCrestStage > self.floodFlow \
        and self.maxForecastStage == self.forecastCrestStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage > self.floodFlow \
        and self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage > self.floodFlow \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage > self.observedStage \
        and self.forecastCrestStage == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage == self.forecastCrestStage \
        and self.forecastCrestStage > self.observedStage and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* FcstCrestStg *| |* StgFlowUnits *| by
            |* FcstCrestTime *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. The river will fall below |* StgFlowName *| |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of self.maxForecastStage |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast below flood stage/cancellation or expiration issued
        #
        if (self.action == "CAN" or self.action == "EXP" ) and self.observedStage < self.floodFlow \
        and self.maxForecastStage < self.floodFlow and self.obsFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which never rose above flood stage and has already crested
        #
        if self.action == "CAN" and self.observedStage < self.floodFlow and self.maxForecastStage < self.floodFlow \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxObsStg24 >= self.maxForecastStage:
            bulletstr = '''The river crested below |* StgFlowName *| at |* MaxObsStg24 *| |* StgFlowUnits *|. The river will
            continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which will not rise above flood stage, but which is still rising
        #
        if self.action == "CAN" and self.observedStage < self.floodFlow and self.maxForecastStage < self.floodFlow \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage > self.maxObsStg24:
            bulletstr = '''The river will crest below |* StgFlowName *| with a value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLW_FLOW(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage == self.floodFlow:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.QR0FFXNext_float > self.floodFlow \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* QR,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage > self.floodFlow \
        and self.QR0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.QR0FFXNext_float > self.floodFlow \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by |* QR,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage > self.observedStage \
        and self.QR0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.QR0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* QR,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.QR0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE and self.forecastCrestStage > self.observedStage:
            bulletstr = '''The river will continue rising to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* QR,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *| 
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr

    def VTEC_FLS_FLOW(self):
        bulletstr = ''
        #
        # Observed below flood stage/forecast to rise just to flood stage
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage == self.floodFlow:
            bulletstr = '''The river is expected to rise to near |* StgFlowName *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/not falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.QR0FFXNext_float > self.floodFlow and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* QR,0,FF,X,NEXT,TIME *|.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has no crest
        #
        if self.observedStage < self.floodFlow and self.maxForecastStage > self.floodFlow \
        and self.QR0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* MaxFcstStg *| |* StgFlowUnits *| by |* MaxFcstTime *|.
            Additional rises are possible thereafter.'''

        #
        # Observed below flood stage/forecast above flood stage/forecast time
        # series has a crest/falling below flood stage
        #
        if self.observedStage < self.floodFlow and self.QR0FFXNext_float > self.floodFlow \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''Rise above |* StgFlowName *| by |* FcstRiseFSTime *|
            and continue to rise to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by |* QR,0,FF,X,NEXT,TIME *|.
            The river will fall below |* StgFlowName *| by |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continues above flood stage/no
        # crest in forecast time series
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage > self.observedStage \
        and self.QR0FFXNext_float == MISSING_VALUE and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* MaxFcstStg *| |* StgFlowUnits *| by
            |* MaxFcstTime *|. Additional rises may be possible thereafter.'''

        #
        # Observed above flood stage/forecast crests but stays above flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.QR0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* QR,0,FF,X,NEXT,TIME *| then begin falling.'''

        #
        # Observed above flood stage/forecast crests and falls below flood
        # x stage
        #
        if self.observedStage >= self.floodFlow and self.QR0FFXNext_float > self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue rising to near |* QR,0,FF,X,NEXT *| |* StgFlowUnits *| by
            |* QR,0,FF,X,NEXT,TIME *|. The river will fall below |* StgFlowName *|
            |* FcstFallFSTime *|.'''

        #
        # Observed above flood stage/forecast continue fall/not below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "falling" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will continue to fall to a stage of |* SpecFcstStg *| |* StgFlowUnits *| by
            |* SpecFcstStgTime *|.'''

        #
        # Observed above flood stage/forecast is steady/not fall below flood stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.stageTrend == "steady" and self.fcstFallFSTime == MISSING_VALUE:
            bulletstr = '''The river will remain near |* MaxFcstStg *| |* StgFlowUnits *|.'''

        #
        # Observed above flood stage/forecast continues fall to below flood
        # stage
        #
        if self.observedStage >= self.floodFlow and self.maxForecastStage <= self.observedStage \
        and self.fcstFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to below |* StgFlowName *| by
            |* FcstFallFSTime *|.'''

        #
        # FORECAST INFORMATION FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage >= self.floodStage:
            bulletstr = '''The river is forecast to have a maximum value of |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST BELOW FLOOD STAGE FOR NO OBS SITUATION
        # change made 3/17/2009 Mark Armstrong HSD
        #
        if self.observedStage == MISSING_VALUE and self.maxForecastStage < self.floodStage:
            bulletstr = '''The river is forecast below |* StgFlowName *| with a maximum value of |* MaxFcstStg *|
            |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # Observed below flood stage/forecast below flood stage/cancellation or expiration issued
        #
        if (self.action == "CAN" or self.action == "EXP") and self.observedStage < self.floodFlow \
        and self.maxForecastStage < self.floodFlow and self.obsFallFSTime != MISSING_VALUE:
            bulletstr = '''The river will continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which never rose above flood stage and which has already crested
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage <= self.maxObsStg24:
            bulletstr = '''The river crested below |* StgFlowName *| at |* MaxObsStg24 *| |* StgFlowUnits *|. The river will
            continue to fall to |* SpecFcstStg *| |* StgFlowUnits *| by |* SpecFcstStgTime *|.'''

        #
        # Cancellation for a location which will not rise above flood stage, but which is still rising
        #
        if self.action == "CAN" and self.observedStage < self.floodStage and self.maxForecastStage < self.floodStage \
        and self.obsRiseFSTime == MISSING_VALUE and self.obsFallFSTime == MISSING_VALUE and self.maxForecastStage > self.maxObsStg24:
            bulletstr = '''The river will crest below |* StgFlowName *| at <MaxObsStg> |* StgFlowUnits *| |* MaxFcstTime *|.'''

        #
        # FORECAST INFORMATION FOR NON-FLOOD LOCATIONS
        #
        if self.action == "ROU" and self.maxForecastStage != MISSING_VALUE:
            bulletstr = '''The river will rise to near |* MaxFcstStg *| |* StgFlowUnits *| |* MaxFcstTime *|.'''
        return bulletstr
