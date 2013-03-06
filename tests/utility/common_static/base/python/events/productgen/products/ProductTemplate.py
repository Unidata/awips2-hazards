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

import abc
import collections

class Product(object):
    __metaclass__ = abc.ABCMeta
    data = collections.OrderedDict()
    
    def __init__(self):
        return
    
    @abc.abstractmethod
    def defineDialog(self):
        """      
        @summary: Defines a dialog that will be presented to the user prior to 
        the recommender's execute routine.  Will use python maps to define widgets.  
        Each key within the map will defined a specific attribute for the widget.
        @return: Python map which correspond to attributes for widgets.
        """ 
        return
    
    @abc.abstractmethod
    def execute(self, hazardEventSet):
        """ 
        @param hazardEventSet: 
        @return: Dictionary
        """ 
        return