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
#    06/23/16       19537          Chris.Golden   Changed to use visual features for spatial info.
#

import os

import PythonOverriderInterface
import JUtil
from GeometryHandler import shapelyToJTS, jtsToShapely
JUtil.registerPythonToJava(shapelyToJTS)
JUtil.registerJavaToPython(jtsToShapely)
from HazardEventHandler import pyHazardEventToJavaHazardEvent, javaHazardEventToPyHazardEvent
JUtil.registerPythonToJava(pyHazardEventToJavaHazardEvent)
JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)

from EventSet import EventSet

class RecommenderInterface(PythonOverriderInterface.PythonOverriderInterface):
    
    def __init__(self, scriptPath, localizationPath):
        super(RecommenderInterface, self).__init__(scriptPath, localizationPath)
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
