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
# Handler for Hazard Events to Java and back.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    04/07/14        2917          jsanchez         Initial Creation.

from com.raytheon.uf.common.hazards.productgen import KeyInfo
from com.raytheon.uf.common.python import PyJavaUtil
import JUtil

from KeyInfo import KeyInfo as PythonKeyInfo

def pyKeyInfoToJavaKeyInfo(val):
    if isinstance(val, PythonKeyInfo) == False:
            return False, val
    return True, val.toJavaObj()

def javaKeyInfoToPyKeyInfo(obj, customConverter=None):
    if _isJavaConvertible(obj) == False:
        return False, obj
    name = obj.getName()
    productCategory = obj.getProductCategory()
    productID = obj.getProductID()
    eventIDs = JUtil.javaObjToPyVal(obj.getEventIDs())
    segment = obj.getSegment()
    editable = obj.isEditable()
    displayable = obj.isDisplayable()
    label = obj.getLabel()
    required = obj.isRequired()
    index = obj.getIndex()
    keyInfo = PythonKeyInfo(name, productCategory, productID, eventIDs, segment, editable, displayable, label, required, index)
    return True, keyInfo

def _isJavaConvertible(obj):
    return PyJavaUtil.isSubclass(obj, KeyInfo)