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

#
# Python should use this interface to get to the localization files.
#   
#
#    
#    SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    03/12/13                      mnash        Initial Creation.
#    
# 
#

import JUtil
from datetime import datetime

from java.io import File

class LocalizationFile(JUtil.JavaWrapperClass):
    
    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
    
    def save(self):
        return self.jobj.save()
        
    def delete(self):
        return self.jobj.delete()
    
    def exists(self):
        return self.jobj.exists()
    
    def getName(self):
        return self.jobj.getName()
    
    def getFilePath(self):
        return self.jobj.getFile().getAbsolutePath()
    
    def getFile(self, mode):
        return self.getFile(mode, True)
    
    def getFile(self, mode, retrieveFile):
        return open(self.jobj.getFile(true).getAbsolutePath(), mode)

    def getTimeStamp(self):
        return datetime.fromtimestamp(self.jobj.getTimeStamp().getTime())
    
    def getContext(self):
        pass
    
    def isAvailableOnServer(self):
        return self.jobj.isAvailableOnServer()
    
    def isDirectory(self):
        return self.jobj.isDirectory()
    
    def isProtected(self):
        return self.jobj.isProtected()
    
    def getProtectedLevel(self):
        pass
    
    def __eq__(self, other):
        return self.jobj.equals(other.jobj)
    
    