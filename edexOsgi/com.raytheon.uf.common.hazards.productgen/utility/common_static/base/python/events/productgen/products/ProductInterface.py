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
# Globally import and sets up instances of the products.
#   
#
#    
#    SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    02/20/13                      jsanchez        Initial Creation.
#    08/20/13        1360          blawrenc       Added code to store an event
#                                                 set in the generated product
#    12/05/13        2527          bkowal         Remove unused EventConverter import. Register
#                                                 Hazard Event conversion capabilities with JUtil.
#    
# 
#
import RollbackMasterInterface
import JUtil, importlib

from GeometryHandler import shapelyToJTS, jtsToShapely
JUtil.registerPythonToJava(shapelyToJTS)
JUtil.registerJavaToPython(jtsToShapely)
from HazardEventHandler import pyHazardEventToJavaHazardEvent, javaHazardEventToPyHazardEvent
JUtil.registerPythonToJava(pyHazardEventToJavaHazardEvent)
JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)
from collections import OrderedDict
from java.util import ArrayList
from com.raytheon.uf.common.hazards.productgen import GeneratedProduct
import traceback, sys, os
import logging, UFStatusHandler

from com.raytheon.uf.common.dataplugin.events import EventSet

from EventSet import EventSet as PythonEventSet

class ProductInterface(RollbackMasterInterface.RollbackMasterInterface):
    
    def __init__(self, scriptPath):
        super(ProductInterface, self).__init__(scriptPath)
        self.importModules()
        self.logger = logging.getLogger("ProductInterface")
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            "com.raytheon.uf.common.hazards.productgen", "ProductInterface", level=logging.INFO))
        self.logger.setLevel(logging.INFO)         
    
    def execute(self, moduleName, className, **kwargs):
        
        # TODO
        # Add dialogInputMap in here as well???
        formats = kwargs.pop('formats')
        kwargs['eventSet'] = PythonEventSet(kwargs['eventSet'])
             
        dataList, hazardEvents = self.runMethod(moduleName, className, 'execute', **kwargs)
        if not isinstance(dataList, list):
            raise Exception('Expecting a list from ' + moduleName + '.execute()')
        formats = JUtil.javaStringListToPylist(formats) 
        
        return self.format(dataList, formats, hazardEvents)
    
    def getDialogInfo(self, moduleName, className, **kwargs):
        """
        @return: Returns a map of string to string of the dialog info.
        """
        val = self.runMethod(moduleName, className, 'defineDialog', **kwargs)
        return JUtil.pyValToJavaObj(val)
    
    def getScriptMetadata(self, moduleName, className, **kwargs):
        """
        @return: Returns a map of string to string of the metadata.
        """
        val = self.runMethod(moduleName, className, 'defineScriptMetadata', **kwargs)
        return JUtil.pyValToJavaObj(val)
    
    def format(self, dataList, formats, hazardEvents):
        """
        Returns the list of dictionaries of the data in different formats.
        @param dataList: list of dictionaries
        @param formats: list of formats
        @param eventDicts: list of hazard event dictionaries
        @return: Returns a Hazard Event Set with a list of GeneratedProducts.
        """
        generatedProductList = ArrayList()
        
        javaHazardEvents = JUtil.pyValToJavaObj(hazardEvents)
        eventSet = EventSet()
        eventSet.addAll(javaHazardEvents)

        for data in dataList:
            if isinstance(data, OrderedDict):
                wmoHeader = data.get('wmoHeader') 
                if wmoHeader:
                    productID = wmoHeader.get('productID')
                  
                generatedProduct = GeneratedProduct(productID)
                productDict = {}
                for format in formats:
                    try:
                        module = importlib.import_module(format) 
                        instance = getattr(module, 'Format')()
                        result = instance.execute(data)
                        if type(result) is list:
                            product = result
                        else:
                            product = [result] 
                        productDict[format] = product
                    except Exception, e:
                        productDict[format] = 'Failed to execute ' + format + '. Make sure it exists.'
                        #self.logger.exception("An Exception Occurred" + traceback.format_exc(limit=20))
                        exc_type, exc_value, exc_traceback = sys.exc_info()
                        print traceback.format_exc(limit=20)
                        #traceback.print_tb(exc_traceback, limit=20)
                        os.sys.__stdout__.flush()
                
                jmap = JUtil.pyDictToJavaMap(productDict)
                generatedProduct.setEntries(jmap)
            else:
                generatedProduct = GeneratedProduct(None)
                generatedProduct.setErrors('Can not format data. Not a python dictionary')
                            
            generatedProduct.setEventSet(eventSet)    
            generatedProductList.add(generatedProduct)

        return generatedProductList
