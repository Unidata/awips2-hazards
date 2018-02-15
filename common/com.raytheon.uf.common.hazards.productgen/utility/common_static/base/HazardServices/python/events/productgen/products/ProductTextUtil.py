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

import JUtil
from ProductText import ProductText

from java.util import ArrayList
from java.lang import Integer
from com.raytheon.uf.common.hazards.productgen.editable import ProductTextUtil


#
# Methods to access Product Text from Python.
#

def createProductText(key, productCategory, mode, segment, eventIDs, officeID, value):
    '''
    Stores an entry in the database.  The value must be able to be converted by JUtil
    to a Java Serializable object.
    '''
    val = JUtil.pyValToJavaObj(value)
    eventIDs = JUtil.pyValToJavaObj(eventIDs)
    ProductTextUtil.createProductText(key, productCategory, mode, segment, eventIDs, officeID, val)

def updateProductText(key, productCategory, mode, segment, eventIDs, officeID, value):
    '''
    Updates an entry in the database.  The value must be able to be converted by JUtil
    to a Java Serializable object.
    '''
    val = JUtil.pyValToJavaObj(value)
    eventIDs = JUtil.pyValToJavaObj(eventIDs)
    ProductTextUtil.updateProductText(key, productCategory, mode, segment, eventIDs, officeID, val)
    
def deleteProductText(key, productCategory, mode, segment, eventIDs, officeID):
    '''
    Deletes an entry from the database.
    '''
    eventIDs = JUtil.pyValToJavaObj(eventIDs)
    ProductTextUtil.deleteProductText(key, productCategory, mode, segment, eventIDs, officeID)

def createOrUpdateProductText(key, productCategory, mode, segment, eventIDs, officeID, value):
    '''
    Saves or updates to the database.
    '''
    eventIDs = JUtil.pyValToJavaObj(eventIDs)
    ProductTextUtil.createOrUpdateProductText(key, productCategory, mode, segment, eventIDs, officeID, value)

def retrieveProductText(key, productCategory, mode, segment, eventIDs, officeID=None):
    '''
    Returns a list of ProductText objects.  Retrieves from the database based on the keys passed in.
    If the user wants to match anything in that column, they should pass in None for that column.
    '''
    eventIDs = JUtil.pyValToJavaObj(eventIDs)
    productTextList = ProductTextUtil.retrieveProductText(key, productCategory, mode, segment, eventIDs, officeID)
    
    if productTextList is None:
        return []
    size = productTextList.size()
    pythonlist = list()
    for i in range(size):
        pythonlist.append(ProductText(productTextList.get(i)))
    
    return pythonlist

