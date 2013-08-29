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

import JUtil

#
# An object representation of Product Text.  Only getters, as if the client wants
# to set they need to use ProductTextUtil.py.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    08/28/2013                     mnash       Initial Creation.
#    
# 
#

class ProductText(JUtil.JavaWrapperClass):
    
    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
    
    def getKey(self):
        return self.jobj.getKey()
    
    def getProductCategory(self):
        return self.jobj.getProductCategory()
    
    def getProductID(self):
        return self.jobj.getProductID()
    
    def getSegment(self):
        return self.jobj.getSegment()
        
    def getEventID(self):
        return self.jobj.getEventID()
        
    def getValue(self):
        return JUtil.javaObjToPyVal(self.jobj.getValue())
        
