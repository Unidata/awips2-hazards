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
#    04/07/14                      jsanchez        Initial Creation.
#    04/21/14        2336          Chris.Golden    Added capitalization of labels.
#    04/23/14        3519          jsanchez        Added required fields.
import JUtil
from com.raytheon.uf.common.hazards.productgen import KeyInfo as JavaKeyInfo

class KeyInfo(JUtil.JavaWrapperClass):
    
    def __init__(self, name, productCategory=None, productID=None, eventIDs=[], segment=None, editable=False, displayable=False, label=None, required=False, index=0):
        self.name = name
        self.productCategory = productCategory
        self.productID = productID
        if isinstance(eventIDs, list):
            self.eventIDs = tuple(eventIDs)
        else:
            self.eventIDs = tuple([eventIDs])
        self.segment = segment
        self.editable = editable
        self.displayable = displayable     
        if label == None:
            self.label = self.name.title()
        else:
            self.label = label.title()
            
        self.required = required
        # This should be refactored after the ParametersEditorFactory
        # can receive a KeyInfo class.
        if required:
            self.label = label + '*'          
        self.index = index
    
    def getName(self):
        return self.name
    
    def getProductCategory(self):
        return self.productCategory
    
    def getProductID(self):
        return self.productID
            
    def getEventIDs(self):
        return list(iter(self.eventIDs))
    
    def getSegment(self):
        return self.segment
    
    def isEditable(self):
        return self.editable
    
    def isDisplayable(self):
        return self.displayable
    
    def getLabel(self):
        return self.label
    
    def getIndex(self):
        return self.index

    def __hash__(self):
        return hash((self.name, self.productCategory, self.productID, self.eventIDs, self.segment))
    
    def __eq__(self, other):
        return (self.name, self.productCategory, self.productID, self.getEventIDs(), self.segment, self.index) == (other.getName(), other.getProductCategory(), other.getProductID(), other.getEventIDs(), other.getSegment(), other.getIndex())
    
    def __str__(self):
        string = 'Name: ' + self.name + \
            '\nSegment: ' + str(self.segment) + \
            '\nEvent IDs: ' + str(self.getEventIDs()) + \
            '\nEditable: ' + str(self.editable) + \
            '\nDisplayable: ' + str(self.displayable) + \
            '\nLabel: ' + self.label
        return string
            
    def toJavaObj(self):
        keyInfo = JavaKeyInfo()
        keyInfo.setName(self.name)
        keyInfo.setProductCategory(self.productCategory)
        keyInfo.setProductID(self.productID)
        keyInfo.setEventIDs(JUtil.pyValToJavaObj(self.getEventIDs()))
        keyInfo.setSegment(self.segment)
        keyInfo.setEditable(self.editable)
        keyInfo.setDisplayable(self.displayable)
        keyInfo.setLabel(self.label)
        keyInfo.setRequired(self.required)
        keyInfo.setIndex(self.index)
        
        return keyInfo
    
    @staticmethod
    def getElements(name, dict):
        elements = []
        for key in dict:
            if isinstance(key, str) and key == name:
                elements.append(key)
            elif isinstance(key, KeyInfo) and key.getName() == name:
                elements.append(key)    
            
        return elements
    
    def __repr__(self):
        return self.name
    

    