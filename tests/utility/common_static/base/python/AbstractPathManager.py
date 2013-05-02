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
# Class with abstract methods for use with localization files.  
# One implementation of this will be a pure python implementation 
# for use outside of the AWIPS II architecture, and one will follow 
# the AWIPS II architecture.
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

import abc

class AbstractPathManager(object):
    
    __metaclass__ = abc.ABCMeta
    
    def __init__(self):
        return
    
    @abc.abstractmethod
    def getStaticFile(self, name, mode):
        pass
    
    @abc.abstractmethod
    def getStaticFilePath(self, name):
        pass
    
    @abc.abstractmethod
    def getStaticLocalizationFile(self, name):
        pass
    
    @abc.abstractmethod
    def getFilePath(self, context, name):
        pass
    
    @abc.abstractmethod
    def getFile(self, context, name, mode):
        '''
        @param context: the localization context for which to get the file
        @param name: the name and path of the file
        @param mode: the python mode for which to return the file handle for ('r','w','r+w')
        @return: the file handle for which the calling method can manipulate.
        @summary: This method returns the file handle based on the context, the name, and the
        mode that is passed in.  The calling method MUST clean up the file handle by calling close.
        '''
        pass
    
    @abc.abstractmethod
    def getLocalizationFile(self, context, name):
        pass
    
    @abc.abstractmethod
    def getTieredLocalizationFile(self, type, name):
        pass
    
    @abc.abstractmethod
    def listFiles(self, context, name, extensions, recursive, filesOnly):
        pass
    
    @abc.abstractmethod
    def listStaticFiles(self, name, extensions, recursive, filesOnly):
        pass
    
    @abc.abstractmethod
    def getContext(self, type, level):
        '''
        @param type: The localization type, must be one of the pre-defined ones above
        @param level: The localization level, must be one of the pre-defined ones above
        @return: the localization context
        '''
        pass
        
    @abc.abstractmethod
    def getContextForSite(self, type, siteId):
        '''
        @param type: The localization type, must be one of the pre-defined ones above
        @param siteId: The site id for which to search contexts
        @return: the localization context for the site given with the type given
        '''
        pass
    
    @abc.abstractmethod
    def getLocalSearchHierarchy(self, type):
        pass
    
    @abc.abstractmethod
    def getContextList(self, level):
        pass
    
    @abc.abstractmethod
    def getAvailableLevels(self):
        pass
    
    @abc.abstractmethod
    def getCombinedFile(self, name):
        pass
    
