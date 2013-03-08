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
import JUtil
from collections import OrderedDict
from java.util import ArrayList
from com.raytheon.uf.common.hazards.productgen import GeneratedProduct


class ProductInterface(RollbackMasterInterface.RollbackMasterInterface):
    
    def __init__(self, scriptPath):
        super(ProductInterface, self).__init__(scriptPath)
        self.importModules()
    
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
                if 'productID' in data:
                    productID = data['productID']
                else:
                    productID = None
                   
                generatedProduct = GeneratedProduct(productID)
                products = {}
                for format in formats:
                    try:
                        module = __import__(format)
                        instance = getattr(module, 'Format')()
                        result = instance.execute(data)
                        if type(result) is list:
                            product = result
                        else:
                            product = [result] 
                        products[format] = product
                    except Exception, e:
                        products[format] = 'Failed to execute ' + format + '. Make sure it exists.'
                
                jmap = JUtil.pyDictToJavaMap(products)
                generatedProduct.setEntries(jmap)
            else:
                generatedProduct = GeneratedProduct(None)
                generatedProduct.setErrors('Can not format data. Not a python dictionary')
            generatedProductList.add(generatedProduct)
          
        return generatedProductList