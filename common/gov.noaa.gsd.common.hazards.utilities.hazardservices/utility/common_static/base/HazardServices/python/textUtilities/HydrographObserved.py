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
    A Python Wrapper of HydrographObserved.java. This allows focal points the ability
    to see the available methods on the Java object. Note that only getter methods 
    have been implemented at this time, since this is strictly used for accessing
    data.
'''

from Hydrograph import Hydrograph 
import JUtil

class HydrographObserved(Hydrograph, JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        super(HydrographObserved, self).__init__(wrappedObject)

    def __str__(self):
        string = 'HydrographObserved: ' + self.jobj.toString()
        return string

##########################################################
#
#    Getter methods from HydrographObserved.java
#
#    Note this is a subclass of Hydrograph.py. Reference that
#    class for additional getter methods.
#
##########################################################

    def getObsBeginTime(self):
        '''
        Get Current Start of Observed Time.
        '''
        return self.jobj.getObsBeginTime()

    def getObsEndTime(self):
        '''
        Get Current End of Observed Time.
        '''
        return self.jobj.getObsEndTime()
