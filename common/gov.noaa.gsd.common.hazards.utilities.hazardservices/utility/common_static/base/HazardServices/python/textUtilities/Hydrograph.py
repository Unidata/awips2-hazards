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
    A Python Wrapper of Hydrograph.java. This allows focal points the ability
    to see the available methods on the Java object. Note that only getter methods 
    have been implemented at this time, since this is strictly used for accessing
    data.
'''


import JUtil
from SHEFHandler import pySHEFToJavaSHEF, javaSHEFToPySHEF
JUtil.registerPythonToJava(pySHEFToJavaSHEF)
JUtil.registerJavaToPython(javaSHEFToPySHEF)

class Hydrograph(JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        self.jobj = wrappedObject

    def __eq__(self, other):
        return self.jobj.equals(other.jobj)

    def __ne__(self, other):
        return not self.__eq__(other)

    def __str__(self):
        string = 'Hydrograph: ' + self.jobj.toString()
        return string

    def toJavaObj(self):
        return self.jobj

##########################################################
#
#    Getter methods from Hydrograph.java
#
##########################################################

    def getLid(self):
        '''
        Gets the Forecast Point Identifier.
        '''
        return self.jobj.getLid()

    def getPhysicalElement(self):
        '''
        Gets the physical element..
        '''
        return self.jobj.getPhysicalElement()

    def getTypeSource(self):
        '''
        Gets the type source.
        '''
        return self.jobj.getTypeSource()

    def getShefHydroDataList(self):
        '''
        Gets the list of all queried SHEF Time Series objects.
        '''
        shefList = self.jobj.getShefHydroDataList()
        return JUtil.javaObjToPyVal(shefList)

    def getShefHydroByValue(self, isMax):
        return JUtil.javaObjToPyVal(self.jobj.getShefHydroByValue(isMax))

    def getMaxShefHydroData(self):
        '''
        Get SHEF Hydro Data object with the Maximum value from all of the
        Hydrograph SHEF objects.
         '''
        return self.getShefHydroByValue(True)

    def getMaxShefHydroDataValue(self):
        '''
        Get the Maximum value from all of the Hydrograph SHEF objects.
        '''
        return self.jobj.getMaxShefHydroDataValue()

    def getShefHydroDataByTime(self, isEarliest):
        return JUtil.javaObjToPyVal(self.jobj.getShefHydroDataByTime(isEarliest))

    def getEarliestShefHydroData(self):
        return JUtil.javaObjToPyVal(self.getShefHydroDataByTime(True))

    def getLatestShefHydroData(self):
        return JUtil.javaObjToPyVal(self.getShefHydroDataByTime(False))

    def getEarliestShefHydroDataTime(self):
        return self.jobj.getEarliestShefHydroDataTime()

    def getLatestShefHydroDataTime(self):
        return self.jobj.getLatestShefHydroDataTime()

