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
# Handler for ProductPart objects to Java and back.
#  

from com.raytheon.uf.common.hazards.productgen import ProductPart
from com.raytheon.uf.common.python import PyJavaUtil
import JUtil

from ProductPart import ProductPart as PythonProductPart

def pyProductPartToJavaProductPart(val):
    if isinstance(val, PyhonProductPart) == False:
            return False, val
    return True, val.toJavaObj()

def javaProductPartToPyProductPart(obj, customConverter=None):
    if _isJavaConvertible(obj) == False:
        return False, obj
    name = obj.getName()
    displayable = obj.isDisplayable()
    label = obj.getLabel()
    eventIDsInLabel = obj.isEventIDsInLabel()
    editable = obj.isEditable()
    required = obj.isRequired()
    numLines = obj.getNumLines()
    segmentDivider = obj.isSegmentDivider()
    formatMethod = obj.getFormatMethod()
    subParts = JUtil.javaObjToPyVal(obj.getSubParts())
    keyInfo = JUtil.javaObjToPyVal(obj.getKeyInfo())
    generatedText = obj.getGeneratedText()
    previousText = obj.getPreviousText()
    currentText = obj.getCurrentText()
    usePreviousText = obj.isUsePreviousText()
    ProductPart = PythonProductPart(name, displayable, label, eventIDsInLabel, editable, required,
                                    numLines, segmentDivider, formatMethod, subParts, keyInfo, 
                                    generatedText, previousText, currentText, usePreviousText)
    return True, ProductPart

def _isJavaConvertible(obj):
    return PyJavaUtil.isSubclass(obj, ProductPart)
