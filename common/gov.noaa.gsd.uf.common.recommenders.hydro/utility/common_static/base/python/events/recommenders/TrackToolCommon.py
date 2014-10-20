"""
Some common code for storm track recommenders.

@since: Oct 2014
@author: JRamer
"""
import RecommenderTemplate
from GeneralConstants import *

class TrackToolCommon(RecommenderTemplate.Recommender):

    def minuteOf(self, itime) :
        if itime == None :
            return 0
        if itime < VERIFY_MILLISECONDS :
            return itime - itime%SECONDS_PER_MINUTE
        return itime - itime%MILLIS_PER_MINUTE

    def sameMinute(self, itime1, itime2) :
        if itime1 == None or itime2 == None :
            return False
        dt = itime2-itime1
        if itime1 < VERIFY_MILLISECONDS :
            return dt>-SECONDS_PER_MINUTE and dt<SECONDS_PER_MINUTE
        return dt>-MILLIS_PER_MINUTE and dt<MILLIS_PER_MINUTE

