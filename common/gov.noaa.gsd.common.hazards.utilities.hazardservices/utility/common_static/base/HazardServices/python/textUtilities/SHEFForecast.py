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
    A Python Wrapper of SHEFForecast.java. This allows focal points the ability
    to see the available methods on the Java object. Note that only getter methods 
    have been implemented at this time, since this is strictly used for accessing
    data.
'''

from SHEFBase import SHEFBase
import JUtil

class SHEFForecast(SHEFBase, JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        super(SHEFForecast, self).__init__(wrappedObject)

    def __str__(self):
        string = 'SHEFForecast: ' + self.jobj.toString()
        return string

##########################################################
#
#    Getter methods from SHEFForecast.java
#
#    Note this is a subclass of SHEFBase.py. Reference that
#    class for additional getter methods.
#
##########################################################

    def getProbability(self):
        '''
        Gets the probability of this forecast value.
        '''
        return self.jobj.getProbability()

    def getValidTime(self):
        '''
        Gets the valid time of this forecast value.
        '''
        return self.jobj.getValidTime()

    def getBasisTime(self):
        '''
        Gets the basis time of this forecast value.
        '''
        return self.jobj.getBasisTime()

    def getTime(self):
        '''
        Gets the valid time.
        '''
        return self.jobj.getValidTime()
