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
    A Python Wrapper of SHEFPrecip.java. This allows focal points the ability
    to see the available methods on the Java object. Note that only getter methods 
    have been implemented at this time, since this is strictly used for accessing
    data.
'''

from SHEFBase import SHEFBase
import JUtil

class SHEFPrecip(SHEFBase, JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        super(SHEFPrecip, self).__init__(wrappedObject)

    def __str__(self):
        string = 'SHEFPrecip: ' + self.jobj.toString()
        return string

##########################################################
#
#    Getter methods from SHEFPrecip.java
#
#    Note this is a subclass of SHEFBase.py. Reference that
#    class for additional getter methods.
#
##########################################################

    def getObsTime(self):
        '''
        Gets the obs time.
        '''
        return self.jobj.getObsTime()

    def getTime(self):
        '''
        Gets the obs time.
        '''
        return self.jobj.getObsTime()

