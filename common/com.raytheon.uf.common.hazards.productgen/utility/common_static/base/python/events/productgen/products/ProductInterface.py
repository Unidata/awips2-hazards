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
#    10/24/14        4934          mpduff         Additional metadata actions.
#    01/15/15        5109          bphillip       Separated generated and formatter execution
#    02/12/15        5071          Robert.Blum    Changed to inherit from the PythonOverriderInterface once
#                                                 again along with other changes to allows the incremental 
#                                                 overrides and also editing without closing Cave.
#    02/26/15        6599          Robert.Blum    Picking up overrides of TextUtilities directory.
#    03/11/15        6885          bphillip       Made product entries an ordered dict to ensure consistent ordering
#
import PythonOverriderInterface
import PythonOverrider
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
import traceback, sys, os, string
import logging, UFStatusHandler

from com.raytheon.uf.common.dataplugin.events import EventSet

from EventSet import EventSet as PythonEventSet
from KeyInfo import KeyInfo
from PathManager import PathManager

class ProductInterface(PythonOverriderInterface.PythonOverriderInterface):

    def __init__(self, scriptPath, localizationPath):
        super(ProductInterface, self).__init__(scriptPath, localizationPath)
        self.pathMgr = PathManager()
        # Import the textUtilities dir using PythonOverrider
        self.importTextUtility()
        # Import all the generator modules using PythonOverrider.
        self.importModules()
        # Import all the formatter modules using PythonOverrider.
        self.importFormatters()
        self.logger = logging.getLogger("ProductInterface")
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            "com.raytheon.uf.common.hazards.productgen", "ProductInterface", level=logging.INFO))
        self.logger.setLevel(logging.INFO)

    def executeGenerator(self, moduleName, className, **kwargs):
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

        genProdList = self.executeGeneratorFrom(moduleName, className, **kwargs)
        
        javaEventSet = EventSet()
        javaEventSet.addAll(JUtil.pyValToJavaObj(events))
        genProdList.setEventSet(javaEventSet)
        return genProdList

    def executeGeneratorFrom(self, moduleName, className, **kwargs):
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
          
        genProdList.setProductInfo(moduleName)
        genProdList.addAll(self.createProductsFromDictionary(dataList))
        return genProdList
    
    def executeFormatter(self, moduleName, className, **kwargs):
        
        generatedProductList = kwargs['generatedProductList']
        formats = JUtil.javaObjToPyVal( kwargs['formats'])
        eventSet =  PythonEventSet(generatedProductList.getEventSet())
        productInfo = generatedProductList.getProductInfo()
        
        # Loop over each product that has been generated and format them
        for i in range(generatedProductList.size()):
            self.formatProduct(generatedProductList.get(i),formats, eventSet, productInfo)

        return generatedProductList
      
    def formatProduct(self, generatedProduct, formats, eventSet, productInfo):
        # Retrieve the product's data to pass to the formatter
        productData = JUtil.javaObjToPyVal(generatedProduct.getData())
        
        data = self.keyInfoDictToPythonDict(productData)
        
        if isinstance(data, OrderedDict): 
            # Dictionary containing the formatted products
            productDict = OrderedDict()
            editables = {}
            for format in formats:
                try:
                    locPath = 'python/events/productgen/formats/'
                    scriptName = locPath + format + '.py'
                    if sys.modules.has_key(format):
                        self.clearModuleAttributes(format)
                    formatModule = PythonOverrider.importModule(scriptName)
                    instance = formatModule.Format()
                    result = instance.execute(data)
                    productDict[format] = result[0]
                    editables[format] = result[1]

                except Exception, e:
                    productDict[format] = ['Failed to execute ' + format + '. Make sure it exists. Check log for errors. ']
                    exc_type, exc_value, exc_traceback = sys.exc_info()
                    print traceback.format_exc(limit=20)
                    os.sys.__stdout__.flush()

            # TODO Use JUtil.pyValToJavaObj() when JUtil.pyDictToJavaMap() is fully resolved
            generatedProduct.setEntries(JUtil.pyDictToJavaMap(productDict))
            generatedProduct.setEditableEntries(JUtil.pyDictToJavaMap(editables))

    def createProductsFromDictionary(self, dataList):
        generatedProductList = ArrayList()
        
        if dataList is not None:
            for data in dataList:
                if isinstance(data, OrderedDict):
                    if 'productID' in data:
                        productID = data.get('productID')
                    else:
                        wmoHeader = data.get('wmoHeader') 
                        if wmoHeader:
                            productID = wmoHeader.get('productID')
                  
                    generatedProduct = GeneratedProduct(productID)
                    generatedProduct.setData(JUtil.pyValToJavaObj(self.pyDictToKeyInfoDict(data)));
                else:
                    generatedProduct = GeneratedProduct(None)
                generatedProductList.add(generatedProduct)

        return generatedProductList
    
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

    def importFormatters(self):
        locPath = 'python/events/productgen/formats/'
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)

    def importTextUtility(self):
        locPath = 'python/textUtilities/'
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)
        # Import all the generators/formatters so that the
        # overridden TextUtility modules are picked up.
        self.reloadModules()
        self.importFormatters()

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
