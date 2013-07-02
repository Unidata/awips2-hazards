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
#    
# 
#
import RollbackMasterInterface
import JUtil, importlib
from collections import OrderedDict
from java.util import ArrayList
from com.raytheon.uf.common.hazards.productgen import GeneratedProduct
import traceback, sys, os
import logging, UFStatusHandler

class ProductInterface(RollbackMasterInterface.RollbackMasterInterface):
    
    def __init__(self, scriptPath):
        super(ProductInterface, self).__init__(scriptPath)
        self.importModules()
        self.logger = logging.getLogger("ProductInterface")
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            "com.raytheon.uf.common.hazards.productgen", "ProductInterface", level=logging.INFO))
        self.logger.setLevel(logging.INFO)         
    
    def execute(self, moduleName, className, hazardEventSet, formats):
        # TODO Convert hazardEventSet to a python hazardEventSet
        kwargs = { 'hazardEventSet' : hazardEventSet }
        dataList = self.runMethod(moduleName, className, 'execute', **kwargs)
        if not isinstance(dataList, list):
            raise Exception('Expecting a list from ' + moduleName + '.execute()')
        formats = JUtil.javaStringListToPylist(formats) 
        
        return self.format(dataList, formats)
    
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
        val = self.runMethod(moduleName, className, 'getScriptMetadata', **kwargs)
        return JUtil.pyValToJavaObj(val)
    
    def format(self, dataList, formats):
        """
        Returns the list of dictionaries of the data in different formats.
        @param dataList: list of dictionaries
        @param formats: list of formats
        @return: Returns a list of GeneratedProducts.
        """
        generatedProductList = ArrayList()
        for data in dataList:
            if isinstance(data, OrderedDict):
                wmoHeader = data.get('wmoHeader') 
                if wmoHeader:
                    productID = wmoHeader.get('productID')
                  
                generatedProduct = GeneratedProduct(productID)
                products = {}
                for format in formats:
                    module = __import__(format)
                    instance = getattr(module, 'Format')()
                    result = instance.execute(data)
                    if type(result) is list:
                        product = result
                    else:
                        product = [result] 
                    products[format] = product
                    try:
                        module = importlib.import_module(format) 
                        instance = getattr(module, 'Format')()
                        result = instance.execute(data)
                        if type(result) is list:
                            product = result
                        else:
                            product = [result] 
                        products[format] = product
                    except Exception, e:
                        products[format] = 'Failed to execute ' + format + '. Make sure it exists.'
                        #self.logger.exception("An Exception Occurred" + traceback.format_exc(limit=20))
                        exc_type, exc_value, exc_traceback = sys.exc_info()
                        traceback.print_tb(exc_traceback, limit=20)
                        os.sys.__stdout__.flush()
                
                jmap = JUtil.pyDictToJavaMap(products)
                generatedProduct.setEntries(jmap)
            else:
                generatedProduct = GeneratedProduct(None)
                generatedProduct.setErrors('Can not format data. Not a python dictionary')
            generatedProductList.add(generatedProduct)
          
        return generatedProductList