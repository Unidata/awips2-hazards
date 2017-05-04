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
# Globally import and sets up instances of the recommenders.
#   
#
#    
#    SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/22/13                      mnash          Initial Creation.
#    12/05/13        2527          bkowal         Remove unused EventConverter import. Register
#                                                 Hazard Event conversion capabilities with JUtil.
#    01/20/14        2766          bkowal         Updated to use the Python Overrider 
#    10/13/14        3790          Robert.Blum    Reverted to use the RollbackMasterInterface.
#    01/29/15        3626          Chris.Golden   Added EventSet to arguments for getting dialog info.
#    02/12/15        5071          Robert.Blum    Changed to inherit from the PythonOverriderInterface once
#                                                 again. This allows the incremental overrides and also editing
#                                                 without closing Cave.
#    02/12/16        14923         Robert.Blum    Picking up overrides of EventUtilities directory
#    06/23/16       19537          Chris.Golden   Changed to use visual features for spatial info.
#

import os, string

import PythonOverriderInterface
import PythonOverrider
import JUtil
from GeometryHandler import shapelyToJTS, jtsToShapely
JUtil.registerPythonToJava(shapelyToJTS)
JUtil.registerJavaToPython(jtsToShapely)
from HazardEventHandler import pyHazardEventToJavaHazardEvent, javaHazardEventToPyHazardEvent
JUtil.registerPythonToJava(pyHazardEventToJavaHazardEvent)
JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)
from PathManager import PathManager

from EventSet import EventSet

class RecommenderInterface(PythonOverriderInterface.PythonOverriderInterface):
    
    def __init__(self, scriptPath, localizationPath):
        super(RecommenderInterface, self).__init__(scriptPath, localizationPath)
        self.pathMgr = PathManager()
        # Import the eventUtilities dir using PythonOverrider
        self.importEventUtility(reloadModules=False)
        # Import all the generator modules using PythonOverrider.
        self.importModules()
        
    def getScriptMetadata(self, moduleName, className, **kwargs):
        val = self.runMethod(moduleName, className, "defineScriptMetadata", **kwargs)
        if val is not None :
            val = JUtil.pyValToJavaObj(val)
        return val
    
    def execute(self, moduleName, className, **kwargs):
        javaDialogInput = kwargs['dialogInputMap']
        if javaDialogInput is not None :
            kwargs['dialogInputMap'] = JUtil.javaObjToPyVal(javaDialogInput)
        javaSpatialInput = kwargs['visualFeatures']
        if javaSpatialInput is not None :
            kwargs['visualFeatures'] = JUtil.javaObjToPyVal(javaSpatialInput)

        kwargs['eventSet'] = EventSet(kwargs['eventSet'])
        
        val = self.runMethod(moduleName, className, "execute", **kwargs)
        if val is not None :
            val = JUtil.pyValToJavaObj(val)
        return val
    
    def getDialogInfo(self, moduleName, className, **kwargs):
        if kwargs.get('eventSet') is not None:
            kwargs['eventSet'] = EventSet(kwargs['eventSet'])
        else:
            kwargs['eventSet'] = None
        val = self.runMethod(moduleName, className, "defineDialog", **kwargs)
        if val is not None :
            val = JUtil.pyValToJavaObj(val)
        return val
    
    def getSpatialInfo(self, moduleName, className, **kwargs):
        val = self.runMethod(moduleName, className, "defineSpatialInfo", **kwargs)
        if val is not None :
            val = JUtil.pyValToJavaObj(val)
        return val

    def importEventUtility(self, reloadModules=True):
        locPath = 'HazardServices/python/events/utilities/'
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)
        # Import all the Recommenders so that the
        # overridden EventUtility modules are picked up.
        if reloadModules:
            self.reloadModules()

    def importFilesFromDir(self, basePath, locPath):
        # Import all the modules in the basePath directory using PythonOverrider.
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
                            tmpModule = PythonOverrider.importModule(locPath + filename)
