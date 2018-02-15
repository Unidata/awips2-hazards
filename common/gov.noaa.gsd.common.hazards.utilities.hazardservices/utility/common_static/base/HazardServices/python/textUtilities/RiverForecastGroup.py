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


'''
    A Python Wrapper of RiverForecastGroup.java. This allows focal points the ability
    to see the available methods on the Java object. Note that only getter methods 
    have been implemented at this time, since this is strictly used for accessing
    data.
'''


import JUtil
from RiverForecastPointHandler import pyRiverForecastPointToJavaRiverForecastPoint, javaRiverForecastPointToPyRiverForecastPoint
JUtil.registerPythonToJava(pyRiverForecastPointToJavaRiverForecastPoint)
JUtil.registerJavaToPython(javaRiverForecastPointToPyRiverForecastPoint)

class RiverForecastGroup(JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        self.jobj = wrappedObject

    def __eq__(self, other):
        return self.jobj.equals(other.jobj)

    def __ne__(self, other):
        return not self.__eq__(other)

    def __str__(self):
        string = 'RiverForecastGroup: ' + self.jobj.toString()
        return string

    def toJavaObj(self):
        return self.jobj

##########################################################
#
#    Getter methods from RiverForecastGroup.java
#
##########################################################

    def getGroupId(self):
        '''
        Gets the group id of this river group.
        '''
        return self.jobj.getGroupId()

    def getGroupName(self):
        '''
        Gets the group_name of this river group.
        '''
        return self.jobj.getGroupName()

    def getOrdinal(self):
        '''
        Gets the ordinal of this river group.
        '''
        return self.jobj.getOrdinal()

    def isRecommendAllPointsInGroup(self):
        '''
        Returns true - all river points are included in the recommended event,
                false - only river points above flood should be included
        '''
        return self.jobj.isRecommendAllPointsInGroup()

    def getNumberOfForecastPoints(self):
        '''
        Gets the number of River Forecast Points in this group.
        '''
        return self.jobj.getNumberOfForecastPoints()

    def getForecastPointList(self):
        '''
        Gets the list of river forecast points in this group.
        '''
        return JUtil.javaObjToPyVal(self.jobj.getForecastPointList())

    def isPointIdInGroup(self, pointID):
        '''
        Determine if the provided point ID is part of this RiverForecastGroup.
        '''
        return self.jobj.isPointIdInGroup(pointID)

    def isIncludedInRecommendation(self):
        '''
        Gets whether or not the river group should be included in the
        recommendation.
        '''
        return self.jobj.isIncludedInRecommendation()

    def getMaxCurrentObservedTime(self):
        '''
        Gets the maximum observed flood time.
        '''
        return self.jobj.getMaxCurrentObservedTime()

    def getMaxOMFCategory(self):
        '''
        Gets the max observed forecast category of all the river points in this
        river group.
        '''
        return self.jobj.getMaxOMFCategory()

    def getMaxOMFTime(self):
        '''
        Gets time of the maximum observed forecast data of all the river
        points in this river group.
        '''
        return self.jobj.getMaxOMFTime()

    def getMaxCurrentObservedCategory(self):
        '''
        Gets the Flood Category (rank) of the maximum observed data of all the
        river points in this river group.
        '''
        return self.jobj.getMaxCurrentObservedCategory()

    def getMaxForecastCategory(self):
        '''
        Gets the Flood Category (rank) of the maximum forecast data of all the
        river points in this river group.
        '''
        return self.jobj.getMaxForecastCategory()

    def getMaxForecastTime(self):
        '''
        Gets the maximum forecast flood time.
        '''
        return self.jobj.getMaxForecastTime()
