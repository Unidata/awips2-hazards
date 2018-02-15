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

import HazardServicesPythonOverriderInterface
import HazardServicesPythonOverrider
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
from ProductPartHandler import pyProductPartToJavaProductPart, javaProductPartToPyProductPart
JUtil.registerPythonToJava(pyProductPartToJavaProductPart)
JUtil.registerJavaToPython(javaProductPartToPyProductPart)

from collections import OrderedDict
from java.util import ArrayList
from com.raytheon.uf.common.hazards.productgen import GeneratedProduct, GeneratedProductList
import traceback, sys, os, string
import logging, UFStatusHandler

from com.raytheon.uf.common.dataplugin.events import EventSet

from EventSet import EventSet as PythonEventSet
from KeyInfo import KeyInfo
from PathManager import PathManager

class ProductInterface(HazardServicesPythonOverriderInterface.HazardServicesPythonOverriderInterface):

    def __init__(self, scriptPath, localizationPath, site):
        super(ProductInterface, self).__init__(scriptPath, localizationPath, site)
        self.pathMgr = PathManager()
        # TODO - every file that exists in localization and is class based needs
        # to be imported via PythonOverrider. Not just the 2 below directories.

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
            kwargs['eventSet'] = eventSet
        else:
            updateList = False
            genProdList = GeneratedProductList()
            eventSet = PythonEventSet(kwargs.get('eventSet'))

        productParts = kwargs.get('productParts', None)
        # make sure keyInfoList is a python object
        if hasattr(productParts, 'java_name'):
            productParts = JUtil.javaObjToPyVal(productParts)
            kwargs['productParts'] = productParts

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
            self.formatProduct(generatedProduct, formats)

        return generatedProductList

    def formatProduct(self, generatedProduct, formats):
        # Retrieve the product's data to pass to the formatter
        productData = JUtil.javaObjToPyVal(generatedProduct.getData())
        # Retrieve the product's editableEntries if available
        editableEntries = JUtil.javaObjToPyVal(generatedProduct.getEditableEntries())

        if isinstance(productData, dict): 
            errors = []
            # Dictionary containing the formatted products
            productDict = OrderedDict()
            for format in formats:
                try:
                    locPath = 'HazardServices/python/events/productgen/formats/'
                    scriptName = locPath + format + '.py'
                    if sys.modules.has_key(format):
                        self.clearModuleAttributes(format)
                    formatModule = HazardServicesPythonOverrider.importModule(scriptName, localizedSite=self.site)
                    instance = formatModule.Format()
                    product, editableEntries = instance.execute(productData, editableEntries)
                    productDict[format] = product
                    generatedProduct.setEditableEntries(JUtil.pyValToJavaObj(editableEntries))

                except Exception as err:
                    errMsg = 'ERROR:  Failed to execute ' + format + '. ' + str(err)
                    productDict[format] = [errMsg]
                    errors.append(errMsg)
                    exc_type, exc_value, exc_traceback = sys.exc_info()
                    print traceback.format_exc(limit=20)
                    os.sys.__stdout__.flush()

            # TODO Use JUtil.pyValToJavaObj() when JUtil.pyDictToJavaMap() is fully resolved
            generatedProduct.setEntries(JUtil.pyDictToJavaMap(productDict))
            if errors:
                generatedProduct.setErrors('\n'.join(errors))

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
        javaEventSet.addAttribute('runMode', eventSet.getAttribute('runMode'))

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

    def importFormatters(self):
        locPath = 'HazardServices/python/events/productgen/formats/'
        lf = self.pathMgr.getLocalizationFile(locPath, loctype='COMMON_STATIC', loclevel='BASE');
        basePath = lf.getPath()
        # Import all the files in this directory
        self.importFilesFromDir(basePath, locPath)

    def importTextUtility(self, reloadModules=True):
        locPath = 'HazardServices/python/textUtilities/'
        self.importDirectory(locPath, reloadModules)
        if reloadModules:
            self.importFormatters()

    def importEventUtility(self, reloadModules=True):
        locPath = 'HazardServices/python/events/utilities/'
        self.importDirectory(locPath, reloadModules)
        if reloadModules:
            self.importFormatters()
