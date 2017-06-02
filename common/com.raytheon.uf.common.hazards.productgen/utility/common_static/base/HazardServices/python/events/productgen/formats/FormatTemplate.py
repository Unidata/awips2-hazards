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
# The abstract formatter module that all other generators will be drawn from.  
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    02/18/13                      jsanchez       Initial Creation.
#    12/11/13        2266          jsanchez       Added abstract method formatFrom.
#    01/19/2015      5109          bphillip       Added _getEditableParts method
#    01/19/2015      7579          Robert.Blum    Removed getEditableParts method
#    03/21/2016     15640          Robert.Blum    Updated method signature of execute.
#
#

import abc

class Formatter(object):
    __metaclass__ = abc.ABCMeta
    
    def __init__(self):
        return
    
    @abc.abstractmethod
    def execute(self, productDict, editableEntries, overrideProductText):
        """
        Subclasses need to override this method.
        @param productDict: dictionary values provided by the product generator
        @param editableEntries: dictionary of productPart to custom text from the Product Editor
        @param overrideProductText: flag used in determining whether or not to use saved text
                                    from the productText table.
        @return: Abstract method does not return anything
        """
        return
