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
#    03/18/13                      mnash        Initial Creation.
#    
# 
#

import os, os.path
import glob
import AbstractPathManager

from com.raytheon.uf.common.localization import PathManagerFactory
from com.raytheon.uf.common.localization import LocalizationContext
from com.raytheon.uf.common.localization import LocalizationContext_LocalizationType as LocalizationType, LocalizationContext_LocalizationLevel as LocalizationLevel
from java.io import File    

# the supported levels are given here
BASE = LocalizationLevel.BASE
CONFIGURED = LocalizationLevel.CONFIGURED
SITE = LocalizationLevel.SITE
WORKSTATION = LocalizationLevel.WORKSTATION
USER = LocalizationLevel.USER

# the supported types are given here
CAVE_STATIC = LocalizationType.CAVE_STATIC
CAVE_CONFIG = LocalizationType.CAVE_CONFIG
COMMON_STATIC = LocalizationType.COMMON_STATIC
EDEX_STATIC = LocalizationType.EDEX_STATIC

class PathManager(AbstractPathManager.AbstractPathManager):
    
    def __init__(self):
        self.pathManager = PathManagerFactory.getPathManager()
        
    def getStaticFile(self, name, mode):
        '''
        @param name: the name and path of the file
        @param mode: the python mode for which to return the file handle for ('r','w','r+w')
        @return: the file handle for which the calling method can manipulate.
        @summary: This method returns the file handle based on the name and the
        mode that is passed in.  The calling method MUST clean up the file handle by calling close.
        '''
        return open(self.getStaticFilePath(name), mode)
    
    def getStaticFilePath(self, name):
        return self.pathManager.getStaticFile(name).getAbsolutePath()
    
    def getStaticLocalizationFile(self, name):
        raise NotImplementedError()
    
    def getFilePath(self, context, name):
        return self.pathManager.getFile(context, name).getAbsolutePath()
    
    def getFile(self, context, name, mode):
        '''
        @param context: the localization context for which to get the file
        @param name: the name and path of the file
        @param mode: the python mode for which to return the file handle for ('r','w','r+w')
        @return: the file handle for which the calling method can manipulate.
        @summary: This method returns the file handle based on the context, the name, and the
        mode that is passed in.  The calling method MUST clean up the file handle by calling close.
        '''
        return open(self.getFilePath(context, name), mode)
    
    def getLocalizationFile(self, context, name):
        raise NotImplementedError()
    
    def getTieredLocalizationFile(self, type, name):
        raise NotImplementedError()
    
    def listFiles(self, context, name, extensions, recursive, filesOnly):
        fileLoc = self.getFilePath(context, name)
        return self._listFiles(fileLoc, extensions, recursive, filesOnly)
    
    def listStaticFiles(self, name, extensions, recursive, filesOnly):
        fileLoc = self.getStaticFilePath(name)
        print fileLoc
        return self._listFiles(fileLoc, extensions, recursive, filesOnly)
    
    def _listFiles(self, path, extensions, recursive, filesOnly):
        listing = list()
        if recursive :
            for root, dirs, files in os.walk(path):
                for f in files :
                    fullpath = os.path.join(root, f)
                    valid = self._checkFile(fullpath, extensions, filesOnly)
                    if valid :
                        listing.append(fullpath)
        else :
            # probably should check if it is a directory first
            for f in os.listdir(path):
                fullpath = os.path.join(path, f)
                valid = self._checkFile(fullpath, extensions, filesOnly)
                if valid :
                    listing.append(f)
        # handle different method signatures            
        return listing
        
    
    def _checkFile(self, fullpath, extensions, filesOnly):
        if extensions is not None and len(extensions) > 0 :
            if os.path.splitext(fullpath)[1] in extensions :
                if filesOnly :
                    if os.path.isfile(fullpath):
                        return True
                else :
                    return True
        else :
            if filesOnly :
                if os.path.isfile(fullpath):
                    return True
            else :
                return True
        return False
    
    def getContext(self, type, level):
        '''
        @param type: The localization type, must be one of the pre-defined ones above
        @param level: The localization level, must be one of the pre-defined ones above
        @return: the localization context
        '''
        return self.pathManager.getContext(type, level)
        
    def getContextForSite(self, type, siteId):
        '''
        @param type: The localization type, must be one of the pre-defined ones above
        @param siteId: The site id for which to search contexts
        @return: the localization context for the site given with the type given
        '''
        return self.pathManager.getContextForSite(type, siteId)
    
    def getLocalSearchHierarchy(self, type):
        raise NotImplementedError()
    
    def getContextList(self, level):
        return self.pathManager.getContextList(level)
    
    def getAvailableLevels(self):
        jLevels = self.pathManager.getAvailableLevels()
        levels = list()
        for level in levels :
            levels.append(level.name())
        return levels
    
    def getCombinedFile(self, name):
        raise NotImplementedError()
    
