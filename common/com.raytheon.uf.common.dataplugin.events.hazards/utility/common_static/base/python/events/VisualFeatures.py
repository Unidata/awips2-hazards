# #
# This software was developed and / or modified by the
# National Oceanic and Atmospheric Administration (NOAA), 
# Earth System Research Laboratory (ESRL), 
# Global Systems Division (GSD), 
# Evaluation & Decision Support Branch (EDS)
# 
# Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
# #

import datetime

#
# List of visual features. This class is simply a subclass of list with no
# additional functionality, but it is needed so that PyJavaUtil can identify
# visual feature lists when converting them into Java objects. (Otherwise,
# PyJavaUtil has no way of knowing whether any given list is a visual features
# list.)
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer        Description
#    ------------    ----------    ------------    --------------------------
#    Mar 01, 2016      15676       Chris.Golden    Initial Creation.
#
class VisualFeatures(list):
    
    def __init__(self, visualFeatures):
        list.__init__(self, visualFeatures)

    # Utility method for converting the specified datetime object into an epoch
    # time in milliseconds, suitable for specifying the bounds of time range
    # tuples in visual feature properties.
    @staticmethod
    def datetimeToEpochTimeMillis(timestamp):
        return int(((timestamp - datetime.datetime(1970,1,1)).total_seconds()) * 1000.0)

