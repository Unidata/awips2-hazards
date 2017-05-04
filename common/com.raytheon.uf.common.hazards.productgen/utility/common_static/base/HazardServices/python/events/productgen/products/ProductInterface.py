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
#    11/05/13        2266          jsanchez        Added an intermediate step to just run formatters.
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
#    03/30/15        6929          Robert.Blum    Added a eventSet to each GeneratedProduct.
#    04/16/15        7579          Robert.Blum    Changes for amended Product Editor.
#    05/07/15        6979          Robert.Blum    Changes Product Corrections
#    05/13/15        8161          mduff          Changes for Jep upgrade.
#    02/12/16        14923         Robert.Blum    Picking up overrides of EventUtilities directory
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
from com.raytheon.uf.common.hazards.productgen import GeneratedProduct, GeneratedProductList, EditableEntryMap
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
        self.importTextUtility(reloadModules=False)
        # Import the eventUtilities dir using PythonOverrider
        self.importEventUtility(reloadModules=False)
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
        javaEventSet = kwargs['eventSet']
        # execute method in generators is expecting a python eventSet
        kwargs['eventSet'] = PythonEventSet(kwargs['eventSet'])
        dataList, events = self.runMethod(moduleName, className, 'execute', **kwargs)
        if not isinstance(dataList, list):
            raise Exception('Expecting a list from ' + moduleName + '.execute()')

        kwargs['dataList'] = dataList
        kwargs['formats'] = formats
        # executeGeneratorFrom method is expecting a javaEventSet
        kwargs['eventSet'] = javaEventSet
        # dialogInputMap no longer needed for executeFrom method
        kwargs.pop('dialogInputMap')

        genProdList = self.executeGeneratorFrom(moduleName, className, **kwargs)
        
        javaEventSet = EventSet()
        javaEventSet.addAll(JUtil.pyValToJavaObj(events))
        genProdList.setEventSet(javaEventSet)
        return genProdList

    def executeGeneratorFrom(self, moduleName, className, **kwargs):
        eventSet = []
        if 'generatedProductList' in kwargs:
            updateList = True
            genProdList = kwargs.pop('generatedProductList')
            eventSet = PythonEventSet(genProdList.getEventSet())
            dataList = []
            for i in range(genProdList.size()):
                # make sure dataList is a python object
                dataList.append(JUtil.javaObjToPyVal(genProdList.get(i).getData()))
            kwargs['dataList'] = dataList
        else:
            updateList = False
            genProdList = GeneratedProductList()
            eventSet = PythonEventSet(kwargs.pop('eventSet'))

        # executeFrom does not need formats
        formats = kwargs.pop('formats')

        # call executeFrom from product generator
        dataList = self.runMethod(moduleName, className, 'executeFrom', **kwargs)

        genProdList.setProductInfo(moduleName)
        self.createProductsFromDictionary(dataList, eventSet, genProdList, updateList)
        return genProdList

    def executeGeneratorUpdate(self, moduleName, className, **kwargs):
        dataList = kwargs['dataList']
        # make sure dataList is a python object
        if hasattr(dataList, 'java_name'):
            dataList = JUtil.javaObjToPyVal(dataList)
            kwargs['dataList'] = dataList

        formats = kwargs.pop('formats')
        javaEventSet = kwargs['eventSet']
        kwargs['eventSet'] = PythonEventSet(javaEventSet)

        # call updateDataList from product generator
        dataList, events = self.runMethod(moduleName, className, 'updateDataList', **kwargs)

        if not isinstance(dataList, list):
            raise Exception('Expecting a list from ' + moduleName + '.execute()')

        kwargs['dataList'] = dataList
        kwargs['formats'] = formats
        # executeGeneratorFrom method is expected a javaEventSet
        kwargs['eventSet'] = javaEventSet
        genProdList = self.executeGeneratorFrom(moduleName, className, **kwargs)

        javaEventSet = EventSet()
        javaEventSet.addAll(JUtil.pyValToJavaObj(events))
        genProdList.setEventSet(javaEventSet)
        return genProdList

    def executeFormatter(self, moduleName, className, **kwargs):
        generatedProductList = kwargs['generatedProductList']
        formats = JUtil.javaObjToPyVal( kwargs['formats'])

        # Loop over each product that has been generated and format them
        for i in range(generatedProductList.size()):
            generatedProduct = generatedProductList.get(i)
            self.formatProduct(generatedProduct,formats)

        return generatedProductList

    def formatProduct(self, generatedProduct, formats):
        # Retrieve the product's data to pass to the formatter
        productData = JUtil.javaObjToPyVal(generatedProduct.getData())
        # Retrieve the product's editableEntries if available
        editableEntriesList = generatedProduct.getEditableEntries()

        data = self.keyInfoDictToPythonDict(productData)

        if isinstance(data, dict): 
            # Dictionary containing the formatted products
            productDict = OrderedDict()
            for format in formats:
                try:
                    locPath = 'HazardServices/python/events/productgen/formats/'
                    scriptName = locPath + format + '.py'
                    if sys.modules.has_key(format):
                        self.clearModuleAttributes(format)
                    formatModule = PythonOverrider.importModule(scriptName)
                    instance = formatModule.Format()
                    editableEntries = None
                    if editableEntriesList:
                        for i in range(editableEntriesList.size()):
                            if format == editableEntriesList.get(i).getFormat():
                                editableEntries = JUtil.javaObjToPyVal(editableEntriesList.get(i).getEditableEntries())
                    product, editableEntries = instance.execute(data, editableEntries)
                    productDict[format] = product
                    editableEntryMap = EditableEntryMap(format, JUtil.pyValToJavaObj(self.pyDictToKeyInfoDict(editableEntries)))
                    generatedProduct.addEditableEntry(editableEntryMap)

                except Exception, e:
                    productDict[format] = ['Failed to execute ' + format + '. Make sure it exists. Check log for errors. ']
                    exc_type, exc_value, exc_traceback = sys.exc_info()
                    print traceback.format_exc(limit=20)
                    os.sys.__stdout__.flush()

            # TODO Use JUtil.pyValToJavaObj() when JUtil.pyDictToJavaMap() is fully resolved
#             print "Product Interface", productDict
#             os.sys.__stdout__.flush()
            
            generatedProduct.setEntries(JUtil.pyDictToJavaMap(productDict))

    def createProductsFromDictionary(self, dataList, eventSet, genProdList, updateList):
        if dataList is not None:
            if updateList:
                for i in range(len(dataList)):
                    data = dataList[i]
                    if isinstance(data, dict):
                        generatedProduct = genProdList.get(i)
                        generatedProduct.setData(JUtil.pyValToJavaObj(data));
            else:
                for data in dataList:
                    if isinstance(data, dict):
                        productID = data.get('productID')
                        generatedProduct = GeneratedProduct(productID)
                        generatedProduct.setData(JUtil.pyValToJavaObj(data));
                        javaEventSet = self.getEventSetForProduct(data, eventSet)
                        generatedProduct.setEventSet(javaEventSet)
                        genProdList.add(generatedProduct)

    def getEventSetForProduct(self, data, eventSet):
        """
        @return: Returns a sub set of the eventSet passed to the generator that pertains
        to this one product. This sub set will be used as a PK to save the product to the 
        productData table during  dissemination.
        """
        # Get all the eventIDs from the product
        eventIDs = set()
        for segment in data.get('segments', []):
            for vtecRecord in segment.get('vtecRecords', []):
                for eventID in vtecRecord.get('eventID', []):
                    eventIDs.add(eventID)

        # Create a sub set of the eventSet based on the product eventIDs
        productEventSet = set()
        for eventID in eventIDs:
            for event in eventSet:
                if event.getEventID() == eventID:
                    productEventSet.add(event)
                    break
        javaEventSet = EventSet()
        javaEventSet.addAll(JUtil.pyValToJavaObj(productEventSet))
        return javaEventSet

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
        locPath = 'HazardServices/python/events/productgen/formats/'
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)

    def importTextUtility(self, reloadModules=True):
        locPath = 'HazardServices/python/textUtilities/'
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)
        # Import all the generators/formatters so that the
        # overridden TextUtility modules are picked up.
        if reloadModules:
            self.reloadModules()
            self.importFormatters()

    def importEventUtility(self, reloadModules=True):
        locPath = 'HazardServices/python/events/utilities/'
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)
        # Import all the generators/formatters so that the
        # overridden EventUtility modules are picked up.
        if reloadModules:
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
