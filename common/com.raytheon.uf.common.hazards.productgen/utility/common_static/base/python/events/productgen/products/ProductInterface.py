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
# Basic information about the keys set in the dictionary.
#   
#
#    
#    SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    02/20/13                      jsanchez        Initial Creation.
#    08/20/13        1360          blawrenc       Added code to store an event
#    11/05/13        2266          jsanchez        Added an intermediate step to just run formatters.                                             set in the generated product
#    12/05/13        2527          bkowal         Remove unused EventConverter import. Register
#                                                 Hazard Event conversion capabilities with JUtil.
#    01/20/14        2766          bkowal         Updated to use the Python Overrider
#    10/10/14        3790          Robert.Blum    Reverted to use the RollbackMasterInterface.
#
import RollbackMasterInterface
import JUtil, importlib

from GeometryHandler import shapelyToJTS, jtsToShapely
JUtil.registerPythonToJava(shapelyToJTS)
JUtil.registerJavaToPython(jtsToShapely)
from HazardEventHandler import pyHazardEventToJavaHazardEvent, javaHazardEventToPyHazardEvent
JUtil.registerPythonToJava(pyHazardEventToJavaHazardEvent)
JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)
from KeyInfoHandler import pyKeyInfoToJavaKeyInfo, javaKeyInfoToPyKeyInfo
JUtil.registerPythonToJava(pyKeyInfoToJavaKeyInfo)
JUtil.registerJavaToPython(javaKeyInfoToPyKeyInfo)
from collections import OrderedDict
from java.util import ArrayList
from com.raytheon.uf.common.hazards.productgen import GeneratedProduct, GeneratedProductList
import traceback, sys, os
import logging, UFStatusHandler

from com.raytheon.uf.common.dataplugin.events import EventSet

from EventSet import EventSet as PythonEventSet
from KeyInfo import KeyInfo

class ProductInterface(RollbackMasterInterface.RollbackMasterInterface):
    
    def __init__(self, scriptPath, localizationPath):
        super(ProductInterface, self).__init__(scriptPath)
        self.importModules()
        self.logger = logging.getLogger("ProductInterface")
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            "com.raytheon.uf.common.hazards.productgen", "ProductInterface", level=logging.INFO))
        self.logger.setLevel(logging.INFO)         
    
    def execute(self, moduleName, className, **kwargs):        
        javaDialogInput = kwargs['dialogInputMap']
        if javaDialogInput is not None :
            kwargs['dialogInputMap'] = JUtil.javaObjToPyVal(javaDialogInput)
        
        formats = kwargs.pop('formats')
        kwargs['eventSet'] = PythonEventSet(kwargs['eventSet'])
        dataList, events = self.runMethod(moduleName, className, 'execute', **kwargs)
        if not isinstance(dataList, list):
            raise Exception('Expecting a list from ' + moduleName + '.execute()')

        kwargs['dataList'] = dataList
        kwargs['formats'] = formats
        # eventSet and dialogInputMap no longer needed for executeFrom method
        kwargs.pop('eventSet')
        kwargs.pop('dialogInputMap')
        
        genProdList = self.executeFrom(moduleName, className, **kwargs)
        
        javaEventSet = EventSet()
        javaEventSet.addAll(JUtil.pyValToJavaObj(events))
        genProdList.setEventSet(javaEventSet)
        
        return genProdList       
    
    def executeFrom(self, moduleName, className, **kwargs):
        genProdList = GeneratedProductList()
        
        dataList = kwargs['dataList']

        # make sure dataList is a python object
        if hasattr(dataList, 'jclassname'):
            dataList = JUtil.javaObjToPyVal(dataList)
            dList = []
            for data in dataList:
                dList.append(self.keyInfoDictToPythonDict(data))
            dataList = dList
            kwargs['dataList'] = dataList            
        
        if 'prevDataList' in kwargs:
            prevDataList = kwargs['prevDataList']
            if prevDataList is not None:
                prevDataList = JUtil.javaObjToPyVal(prevDataList)
                pList = []
                for data in prevDataList:
                    pList.append(self.keyInfoDictToPythonDict(data))
                prevDataList = pList
                kwargs['prevDataList'] = prevDataList           

        # executeFrom does not need formats
        formats = kwargs.pop('formats')

        # call executeFrom from product generator
        dataList = self.runMethod(moduleName, className, 'executeFrom', **kwargs)

        formats = JUtil.javaObjToPyVal(formats)   
          
        genProdList.setProductInfo(moduleName)
        genProdList.addAll(self.format(dataList, formats))
        
        return genProdList
    
    def getDialogInfo(self, moduleName, className, **kwargs):
        """
        @return: Returns a map of string to string of the dialog info.
        """
        kwargs['eventSet'] = PythonEventSet(kwargs['eventSet'])
        val = self.runMethod(moduleName, className, 'defineDialog', **kwargs)
        if val is not None:
            val = JUtil.pyValToJavaObj(val)
        return val
    
    def getScriptMetadata(self, moduleName, className, **kwargs):
        """
        @return: Returns a map of string to string of the metadata.
        """
        val = self.runMethod(moduleName, className, 'defineScriptMetadata', **kwargs)
        return JUtil.pyValToJavaObj(val)
    
    def format(self, dataList, formats):
        """
        Returns the list of dictionaries of the data in different formats.
        @param dataList: list of dictionaries
        @param formats: list of formats
        @param eventDicts: list of hazard event dictionaries
        @return: Returns a Hazard Event Set with a list of GeneratedProducts.
        """
        generatedProductList = ArrayList()
        
        for data in dataList:
            if isinstance(data, OrderedDict):
                wmoHeader = data.get('wmoHeader') 
                if wmoHeader:
                    productID = wmoHeader.get('productID')
                  
                generatedProduct = GeneratedProduct(productID)
                productDict = {}
                editables = {}
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
                        productDict[format] = ['Failed to execute ' + format + '. Make sure it exists. Check console for errors. ']
                        #self.logger.exception("An Exception Occurred" + traceback.format_exc(limit=20))
                        exc_type, exc_value, exc_traceback = sys.exc_info()
                        print traceback.format_exc(limit=20)
                        #traceback.print_tb(exc_traceback, limit=20)
                        os.sys.__stdout__.flush()
                # TODO Use JUtil.pyValToJavaObj() when JUtil.pyDictToJavaMap() is fully resolved
                generatedProduct.setEntries(JUtil.pyDictToJavaMap(productDict))          
                #generatedProduct.setEditableEntries(JUtil.pyDictToJavaMap(editables))
                generatedProduct.setData(JUtil.pyValToJavaObj(self.pyDictToKeyInfoDict(data)));
            else:
                generatedProduct = GeneratedProduct(None)
                generatedProduct.setErrors('Can not format data. Not a python dictionary ')
            generatedProductList.add(generatedProduct)

        return generatedProductList
    
    def pyDictToKeyInfoDict(self, data):
        if isinstance(data, OrderedDict):
            convertedDict = OrderedDict()
        else:
            convertedDict = {}
            
        for key in data:
            value = data[key]
            if isinstance(key, str):
                keyInfo = KeyInfo(key)
            elif isinstance(key, KeyInfo):
                keyInfo = key
            
            if isinstance(value, dict):
                convertedDict[keyInfo] = self.pyDictToKeyInfoDict(value)
            elif isinstance(value, list):
                convertedDict[keyInfo] = self.convertPyList(value)
            else:
                convertedDict[keyInfo] = value
            
        return convertedDict
    
    def convertPyList(self, data):
        convertedList = []
        for item in data:
            if isinstance(item, dict):
                convertedItem = self.pyDictToKeyInfoDict(item)
            elif isinstance(item, list):
                convertedItem = self.convertPyList(item)
            else:
                convertedItem = item

            convertedList.append(convertedItem)
    
        return convertedList
    
    def keyInfoDictToPythonDict(self, data):
        convertedDict = OrderedDict()
        for key in data:      
            value = data[key]
            
            if isinstance(value, dict):
                value = self.keyInfoDictToPythonDict(value)
            elif isinstance(value, list):
                value = self.keyInfoListToPythonList(value)
                          
            if isinstance(key, KeyInfo) and key.isEditable() == False:
                key = key.getName()
            
            convertedDict[key] = value
            
        return convertedDict
    
    def keyInfoListToPythonList(self, data):
        convertedList = []
        for item in data:
            if isinstance(item, dict):
                convertedItem = self.keyInfoDictToPythonDict(item)
            elif isinstance(item, list):
                convertedItem = self.keyInfoListToPythonList(item)
            else:
                convertedItem = item
               
            convertedList.append(convertedItem)
    
        return convertedList
