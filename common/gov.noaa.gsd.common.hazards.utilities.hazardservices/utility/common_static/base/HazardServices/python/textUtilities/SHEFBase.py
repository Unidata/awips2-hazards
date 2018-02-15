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
    A Python Wrapper of SHEFBase.java. This allows focal points the ability
    to see the available methods on the Java object. Note that only getter methods 
    have been implemented at this time, since this is strictly used for accessing
    data.
'''


import JUtil

class SHEFBase(JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        self.jobj = wrappedObject

    def __eq__(self, other):
        return self.jobj.equals(other.jobj)

    def __ne__(self, other):
        return not self.__eq__(other)

    def __str__(self):
        string = 'SHEFBase: ' + self.jobj.toString()
        return string

    def toJavaObj(self):
        return self.jobj

##########################################################
#
#    Getter methods from SHEFBase.java
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

    def getDuration(self):
        '''
        Gets the duration.
        '''
        return self.jobj.getDuration()

    def getTypeSource(self):
        '''
        Gets the type source.
        '''
        return self.jobj.getTypeSource()

    def getExtremum(self):
        '''
        Gets the extremum.
        '''
        return self.jobj.getExtremum()

    def getShefQualCode(self):
        '''
        Gets the shefQualCode.
        '''
        return self.jobj.getShefQualCode()

    def getQualityCode(self):
        '''
        Gets the qualityCode..
        '''
        return self.jobj.getQualityCode()

    def getValue(self):
        '''
        Gets the value.
        '''
        return self.jobj.getValue()

    def getRevision(self):
        '''
        Gets the revision.
        '''
        return self.jobj.getRevision()

    def getProductId(self):
        '''
        Gets the Product Identifier.
        '''
        return self.jobj.getProductId()

    def getProductTime(self):
        '''
        Gets the Product Time.
        '''
        return self.jobj.getProductTime()

    def getPostingTime(self):
        '''
        Gets the Posting Time.
        '''
        return self.jobj.getPostingTime()
