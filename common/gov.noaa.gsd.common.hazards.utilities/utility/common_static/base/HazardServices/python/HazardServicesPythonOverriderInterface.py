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
# Extension of PythonOverriderInterface that overrides the _importModule method.
# This override allows for the use of the HazardServicesPythonOverrider instead of
# PythonOverrider.
#
#    SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    03/31/2016      #8837         Robert.Blum    Initial Creation.
#    Jul 28, 2016    #19222        Robert.Blum    Added importDirectory(...)
#    Mar 06, 2017    #28300        Robert.Blum    Merged with Awips2_baseline module.
#
#
#

import os, sys, string, traceback
import MasterInterface
import HazardServicesPythonOverrider


class HazardServicesPythonOverriderInterface(MasterInterface.MasterInterface):

    def __init__(self, scriptPath, localizationPath=None, site=None):
        super(HazardServicesPythonOverriderInterface, self).__init__()
        self._localizationPath = localizationPath
        self._scriptPath = scriptPath
        self.site = site

    def _importModule(self, moduleName):
        scriptName = moduleName + '.py'
        if self._localizationPath:
            scriptName = os.path.join(self._localizationPath, scriptName)
        try:
            importedModule = HazardServicesPythonOverrider.importModule(scriptName, localizedSite=self.site)
        except Exception, e:
            msg = moduleName + "\n" + traceback.format_exc()
            self.addImportError(msg)
            return

        if not moduleName in self.scripts:
            self.scripts.append(moduleName)

    def importModules(self):
        modulesToImport = []
        
        for s in self._scriptPath.split(os.path.pathsep):
            if os.path.exists(s):
                scriptfiles = os.listdir(s)
        
                for filename in scriptfiles:
                    split = string.split(filename, ".")
                    if len(split) == 2 and len(split[0]) > 0 and split[1] == "py" and not filename.endswith("Interface.py"):
                        if not split[0] in modulesToImport:
                            modulesToImport.append(split[0])        
        
        for moduleName in modulesToImport:
            self._importModule(moduleName)

    def addModule(self, moduleName):
        if not moduleName in self.scripts:
            self.scripts.append(moduleName)
        self.reloadModules()

    def reloadModule(self, moduleName):
        if sys.modules.has_key(moduleName):
            self.reloadModules()
            self.clearModuleAttributes(moduleName)
            self._importModule(moduleName)

    def reloadModules(self):
        for script in self.scripts:
            self.clearModuleAttributes(script)
            # now use PythonOverrider to re-import the module
            self._importModule(script)

    def getStartupErrors(self):
        from java.util import ArrayList
        errorList = ArrayList()
        for err in self.getImportErrors():
            errorList.add(str(err))
        return errorList

    def importDirectory(self, locPath, reloadModules=True):
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)
        # Import all the interface modules so that the
        # overridden directory modules are picked up.
        if reloadModules:
            self.reloadModules()
        
    def importFilesFromDir(self, basePath, locPath):
        # Import all the modules in the basePath directory using HazardServicesPythonOverrider.
        # Need to do this twice since these modules import/subclass each other which could result in
        # in old references being used. Which would cause the override not being picked up.
        for x in range(2):
            for s in basePath.split(os.path.pathsep):
                if os.path.exists(s):
                    scriptfiles = os.listdir(s)
                    for filename in scriptfiles:
                        split = string.split(filename, ".")
                        if len(split) == 2 and len(split[0]) > 0 and split[1] == "py" and not filename.endswith("Interface.py"):
                            if sys.modules.has_key(split[0]):
                                self.clearModuleAttributes(split[0])
                            tmpModule = HazardServicesPythonOverrider.importModule(locPath + filename, localizedSite=self.site)
