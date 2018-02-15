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
#    02/12/16       14923          Robert.Blum    Picking up overrides of EventUtilities directory
#    03/31/16        8837          Robert.Blum    Changes for Service Backup and importing the correct site files.
#    06/23/16       19537          Chris.Golden   Changed to use visual features for spatial info.
#    06/28/16       19222          Robert.Blum    Recommenders now care about the textUtilities directory.
#

import os, string

import HazardServicesPythonOverriderInterface
import HazardServicesPythonOverrider
import JUtil
from GeometryHandler import shapelyToJTS, jtsToShapely
JUtil.registerPythonToJava(shapelyToJTS)
JUtil.registerJavaToPython(jtsToShapely)
from HazardEventHandler import pyHazardEventToJavaHazardEvent, javaHazardEventToPyHazardEvent
JUtil.registerPythonToJava(pyHazardEventToJavaHazardEvent)
JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)
from PathManager import PathManager
from com.raytheon.uf.common.recommenders.executors import MutablePropertiesAndVisualFeatures

from EventSet import EventSet

class RecommenderInterface(HazardServicesPythonOverriderInterface.HazardServicesPythonOverriderInterface):

    def __init__(self, scriptPath, localizationPath, site):
        super(RecommenderInterface, self).__init__(scriptPath, localizationPath, site)
        self.pathMgr = PathManager()
        # Import the textUtilities dir using PythonOverrider
        self.importTextUtility(reloadModules=False)
        # Import the eventUtilities dir using PythonOverrider
        self.importEventUtility(reloadModules=False)
        # Import all the generator modules using PythonOverrider.
        self.importModules()
        
        # This variable will be used to cache the Pythonic version of the
        # event set supplied by the last call to getDialogInfo(), or the
        # last call to getSpatialInfo() that included a non-None event set.
        # It is then used as the event set between then and the next
        # invocation of execute() for calls to the methods getSpatialInfo(),
        # handleDialogParameterChange(), and isSpatialInfoComplete().
        #
        # This is done because event sets can have complex attributes that
        # take a while to convert to Python values from the original Java
        # objects (e.g. the geometry associated with CWAs if the scope is
        # national), and since the event set should not change during
        # parameter gathering, reusing it is OK.
        self.parameterGatheringEventSet = None
        
    def getScriptMetadata(self, moduleName, className, **kwargs):
        val = self.runMethod(moduleName, className, "defineScriptMetadata", **kwargs)
        if val is not None :
            val = JUtil.pyValToJavaObj(val)
        return val
    
    def execute(self, moduleName, className, **kwargs):

        # Reset the parameter gathering event set, since parameter gathering
        # is complete. The cached event set, if any, cannot be used for the
        # execution, since that requires using the newly supplied input event
        # set, which may be different from the cached one, since events may
        # have been added, removed, etc. in the interval between the start
        # of parameter gathering and recommender execution. 
        self.parameterGatheringEventSet = None
        
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

        # Remember the event set in case it is needed for further processing
        # during parameter gathering.
        self.parameterGatheringEventSet = kwargs['eventSet']
        
        val = self.runMethod(moduleName, className, "defineDialog", **kwargs)
        if val is not None :
            val = JUtil.pyValToJavaObj(val)
        return val
    
    def handleDialogParameterChange(self, moduleName, className, **kwargs):

        kwargs['eventSet'] = self.parameterGatheringEventSet
        kwargs['triggeringDialogIdentifiers'] = JUtil.javaObjToPyVal(kwargs['triggeringDialogIdentifiers'])
        mutableProperties = kwargs['mutableDialogProperties']
        if mutableProperties is not None:
            kwargs['mutableDialogProperties'] = JUtil.javaObjToPyVal(mutableProperties)
        kwargs['triggeringVisualFeatureIdentifiers'] = JUtil.javaObjToPyVal(kwargs['triggeringVisualFeatureIdentifiers'])
        visualFeatures = kwargs['visualFeatures']
        if visualFeatures is not None:
            kwargs['visualFeatures'] = JUtil.javaObjToPyVal(visualFeatures)

        val = self.runMethod(moduleName, className, "handleDialogParameterChange", **kwargs)
        
        if val is None:
            return MutablePropertiesAndVisualFeatures()
        mutableProperties = None if val[0] is None else JUtil.pyValToJavaObj(val[0])
        visualFeatures = None if val[1] is None else JUtil.pyValToJavaObj(val[1])
        if mutableProperties is None and visualFeatures is None:
            return MutablePropertiesAndVisualFeatures()
        else:
            return MutablePropertiesAndVisualFeatures(mutableProperties, visualFeatures)
    
    def getSpatialInfo(self, moduleName, className, **kwargs):

        # If an event set was supplied, remember it in case it is needed for
        # further processing during parameter gathering; otherwise, assume that
        # one was supplied by a previous call to this method, and use that.
        if kwargs['eventSet'] is not None:
            kwargs['eventSet'] = EventSet(kwargs['eventSet'])
            self.parameterGatheringEventSet = kwargs['eventSet']
        else:
            kwargs['eventSet'] = self.parameterGatheringEventSet

        javaSpatialInput = kwargs['visualFeatures']
        if javaSpatialInput is not None :
            kwargs['visualFeatures'] = JUtil.javaObjToPyVal(javaSpatialInput)
        val = self.runMethod(moduleName, className, "defineSpatialInfo", **kwargs)
        if val is not None :
            val = JUtil.pyValToJavaObj(val)
        return val
    
    def isSpatialInfoComplete(self, moduleName, className, **kwargs):
        kwargs['eventSet'] = self.parameterGatheringEventSet
        kwargs['visualFeatures'] = JUtil.javaObjToPyVal(kwargs['visualFeatures'])
        return self.runMethod(moduleName, className, "isSpatialInfoComplete", **kwargs)

    def importEventUtility(self, reloadModules=True):
        locPath = 'HazardServices/python/events/utilities/'
        self.importDirectory(locPath, reloadModules)

    def importTextUtility(self, reloadModules=True):
        locPath = 'HazardServices/python/textUtilities/'
        self.importDirectory(locPath, reloadModules)
