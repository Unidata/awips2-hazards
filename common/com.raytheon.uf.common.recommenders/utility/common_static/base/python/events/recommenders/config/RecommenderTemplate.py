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
# The abstract recommender module that all other recommenders will be drawn from.  
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/22/13                      mnash          Initial Creation.
#    01/29/15        3626          Chris.Golden   Added EventSet to arguments for getting dialog info.
#    
# 
#

import abc

class Recommender(object):
    __metaclass__ = abc.ABCMeta
    
    def __init__(self):
        return
    
    @abc.abstractmethod
    def defineScriptMetadata(self):
        '''
        @return: Returns a python dictionary which defines basic information
        about the recommender, such as author, script version, and description
        '''
        return
    
    def defineDialog(self, eventSet):
        '''      
        @summary: Defines a dialog that will be presented to the user prior to 
        the recommender's execute routine.  Will use python maps to define widgets.  
        Each key within the map will defined a specific attribute for the widget.
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @return: Python map which correspond to attributes for widgets.
        '''
        return
    
    def defineSpatialInfo(self):
        '''
        @summary: Determines spatial information needed by the recommender.
        @return: Unknown
        @todo: fix comments, further figure out spatial info
        '''
        return
    
    @abc.abstractmethod
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        '''
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return 
        @param dialogInputMap: A map containing user selections from the dialog created
        by the defineDialog() routine
        @param spatialInputMap: A map containing spatial input as created by the 
        definedSpatialInfo() routine
        @return: List of objects that will be later converted to Java IEvent objects
        '''
        return
