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
from com.raytheon.uf.common.hazards.productgen import KeyInfo as JavaKeyInfo

class KeyInfo(JUtil.JavaWrapperClass):

    def __init__(self, name, productCategory=None, mode=None, eventIDs=[], segment=None,
                 index=0):
        self.name = name
        self.productCategory = productCategory
        self.mode = mode
        if isinstance(eventIDs, list):
            self.eventIDs = tuple(eventIDs)
        else:
            self.eventIDs = tuple([eventIDs])
        self.segment = segment
        self.index = index

    def getName(self):
        return self.name
    
    def getProductCategory(self):
        return self.productCategory
    
    def getMode(self):
        return self.mode
            
    def getEventIDs(self):
        return list(iter(self.eventIDs))
    
    def getSegment(self):
        return self.segment
    
    def getIndex(self):
        return self.index

    def __hash__(self):
        return hash((self.name, self.productCategory, self.mode, self.eventIDs, self.segment))
    
    def __eq__(self, other):
        return (self.name, self.productCategory, self.mode, self.getEventIDs(), self.segment, self.index) == (other.getName(), other.getProductCategory(), other.getMode(), other.getEventIDs(), other.getSegment(), other.getIndex())
    
    def __str__(self):
        string = 'Name: ' + self.name + \
            '\nSegment: ' + str(self.segment) + \
            '\nEvent IDs: ' + str(self.getEventIDs()) + \
            '\nEvent IDs: ' + str(self.getEventIDs())
        return string

    def toJavaObj(self):
        keyInfo = JavaKeyInfo()
        keyInfo.setName(self.name)
        keyInfo.setProductCategory(self.productCategory)
        keyInfo.setMode(self.mode)
        keyInfo.setEventIDs(JUtil.pyValToJavaObj(self.getEventIDs()))
        keyInfo.setSegment(self.segment)
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
