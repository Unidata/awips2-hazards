'''
    Description: A unique section of a product that can be configured in various ways and edited.

    @author: Raytheon Hazard Services Team
'''    
import types, collections
from com.raytheon.uf.common.hazards.productgen import ProductPart as JavaProductPart
import JUtil

class ProductPart(JUtil.JavaWrapperClass):

    def __init__(self, name, displayable=False, label=None, eventIDsInLabel=True, editable=False, required=True, 
                 numLines=0, segmentDivider=False, formatMethod=None, subParts=None, keyInfo=None,
                 generatedText=None, previousText=None, currentText=None, usePreviousText=False):
        self.name = name
        self.displayable = displayable
        self.label = label
        self.eventIDsInLabel= eventIDsInLabel
        self.editable = editable
        self.required = required
        self.numLines = numLines
        self.segmentDivider = segmentDivider
        self.formatMethod = formatMethod
        self.subParts = subParts
        self.keyInfo = keyInfo
        self.generatedText = generatedText
        self.previousText = previousText
        self.currentText = currentText
        self.usePreviousText = usePreviousText

    def __hash__(self):
        return hash((self.name, self.label, self.formatMethod, self.subParts, self.keyInfo,
                     self.generatedText, self.previousText, self.currentText))

    def __eq__(self, other):
        equals = (self.name, self.label, self.formatMethod, self.subParts, self.keyInfo) == \
                 (other.getName(), other.getLabel(), other.getFormatMethod(), other.getSubParts(), other.getKeyInfo())
        return equals

    def __str__(self):
        string = 'Name: ' + self.name + \
            '\nLabel: ' + str(self.label) + \
            '\nFormatMethod: ' + str(self.formatMethod) + \
            '\nSubparts: ' + str(self.subParts) + \
            '\nKeyInfo: ' + str(self.keyInfo) + \
            '\nGenerated Text: ' + str(self.generatedText) + \
            '\nPrevious Text: ' + str(self.previousText) + \
            '\nCurrent Text: ' + str(self.currentText)
        return string

    def toJavaObj(self):
        productPart = JavaProductPart()
        productPart.setName(self.name)
        productPart.setDisplayable(self.displayable)
        productPart.setLabel(self.label)
        productPart.setEventIDsInLabel(self.eventIDsInLabel)
        productPart.setEditable(self.editable)
        productPart.setRequired(self.required)
        productPart.setNumLines(self.numLines)
        productPart.setSegmentDivider(self.segmentDivider)
        productPart.setSubParts(JUtil.pyValToJavaObj(self.subParts))
        productPart.setFormatMethod(self.formatMethod)
        productPart.setKeyInfo(JUtil.pyValToJavaObj(self.keyInfo))
        productPart.setGeneratedText(self.generatedText)
        productPart.setPreviousText(self.previousText)
        productPart.setCurrentText(self.currentText)
        productPart.setUsePreviousText(self.usePreviousText)
        return productPart

    def getName(self):
        return self.name

    def getLabel(self):
        return self.label

    def isEventIDsInLable(self):
        return self.eventIDsInLabel

    def isDisplayable(self):
        return self.displayable

    def isEditable(self):
        return self.editable

    def isRequired(self):
        return self.required

    def getNumLines(self):
        return self.numLines

    def isSegmentDivider(self):
        return self.segmentDivider

    def getFormatMethod(self):
        return self.formatMethod

    def getSubParts(self):
        return self.subParts

    def getKeyInfo(self):
        return self.keyInfo

    def setKeyInfo(self, keyInfo):
        self.keyInfo = keyInfo

    def setGeneratedText(self, generatedText):
        self.generatedText = generatedText

    def getGeneratedText(self):
        return self.generatedText

    def setPreviousText(self, previousText):
        self.previousText = previousText

    def getPreviousText(self):
        return self.previousText

    def setCurrentText(self, currentText):
        self.currentText = currentText

    def getCurrentText(self):
        return self.currentText

    def isUsePreviousText(self):
        return self.usePreviousText

    def setUsePreviousText(self, usePreviousText):
        self.usePreviousText = usePreviousText

    def getProductText(self):
        # Always default to the current text
        if self.currentText is not None:
            return self.currentText
        if self.usePreviousText:
            return self.previousText
        return self.generatedText
