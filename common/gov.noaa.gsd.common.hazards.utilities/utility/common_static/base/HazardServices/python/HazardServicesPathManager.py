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
 Extension of PathManager.py that allows a site to be specified. 
 This allows for site level localization files to be retrieved for other sites
 that you are not currently localized to. This is needed for Service
 Backup.
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 Mar 29, 2016    8837    Robert.Blum Initial Creation

''' 
import os, os.path
import PathManager
import JUtil
from jep import jarray

from LocalizationFile import LocalizationFile
from LockingFile import File

from com.raytheon.uf.common.localization import LocalizationContext as JavaLocalizationContext
LocalizationLevel = JavaLocalizationContext.LocalizationLevel

class HazardServicesPathManager(PathManager.PathManager):

    def __init__(self):
        super(HazardServicesPathManager, self).__init__()

    def getTieredLocalizationFileForSite(self, loctype, name, site=None):
        '''
        @param loctype: The localization type to look in
        @param name: The name of the file
        @param site: The site to look retrieve files for 
        @return: a dictionary of string to localization file
        @summary: Returns the localization levels available for the file given
        '''
        jtype = self._convertType(loctype)
        contexts = self.jpathManager.getLocalSearchHierarchy(jtype)

        map = {}
        for context in contexts:
            if context.getLocalizationLevel() == LocalizationLevel.SITE:
                # Change the site of the context if valid
                if site != None and site != "":
                    context.setContextName(site)
            lf = self.jpathManager.getLocalizationFile(context, name)
            if lf.exists():
                map[lf.getContext().getLocalizationLevel()] =  lf

        vals = {}
        for key in map:
            nextValue = map.get(key)
            # the key of the entry set is a localization level, the value is a localization file
            vals[key.name()] = LocalizationFile(nextValue)
        return vals