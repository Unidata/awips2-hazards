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
# See the AWIPS II Master Rights File ('Master Rights File.pdf') for
# further licensing information.   
# #

#    Formats a dictionary 'data' and generates watch, warning, advisory legacy text. The dictionary values
#    will be extracted and managed appropriately based on their keys. 
#
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    12/11/13        2266          jsanchez       Initial Creation.
#    02/26/13        2702          jsanchez       Supported product level editable fields
#
from collections import OrderedDict

class Editable():
    def __init__(self, data):
        self.result = []
        self.map = self.map = OrderedDict()
        self.newSegmentKeys = []
        self.nonSegmentMap = OrderedDict()
        self.nonSegmentList = []
        self.counter = 0;
        self.first = None
        self.inSegmentList = False
        self.validKeys = self._getEditableKeys(data)
        
    def add(self, key, value):
        # verify if it was a valid editable key
        keyname = str(key)
        if str(key) + ':editable' in self.validKeys:
            keyname = str(key) + ':editable'
            # check if a new segment is started
            if keyname in self.nonSegmentList:
                self.nonSegmentMap[keyname] = value
            else:
                if self.counter < len(self.newSegmentKeys) and keyname == self.newSegmentKeys[self.counter]:
                    if self.counter > 0:
                        self.result.append(self.map)
                    self.map = OrderedDict()
                    self.counter = self.counter + 1
                self.map[keyname] = value
     
    def _getEditableKeys(self, data):
        '''
        Identifies the editable keys in data by looking if the key has ':editable'
        '''
        editableKeys = []
        for key in data:
            value = data[key]
            valtype = type(value)
            if ':editable' in key:
                editableKeys.append(key)
                # tracks when a new segment begins
                if self.inSegmentList and self.first is None:
                    self.first = key
                    self.newSegmentKeys.append(key)
                elif self.inSegmentList == False and key not in self.nonSegmentList:
                    self.nonSegmentList.append(key)
                
            if issubclass(valtype, dict):
                editableList = self._getEditableKeys(value)
                if editableList:
                    editableKeys.extend(editableList)
            elif valtype is list:
                if key == 'segment':
                    self.inSegmentList = True
                for i in range(len(value)):
                    item = value[i]        
                    if issubclass(type(item), dict):
                        editableList = self._getEditableKeys(item)
                        if editableList:
                            editableKeys.extend(editableList)
                    if key == 'segment':
                        self.first = None
                if key == 'segment':
                    self.inSegmentList = False
        return editableKeys
    
    def _getResult(self):
        # clean up
        self.result.append(self.map)
        if len(self.result) < len(self.newSegmentKeys):
            for i in range(len(self.newSegmentKeys) - len(self.result)):
                self.result.append(None)
        self.result.insert(0, self.nonSegmentMap)
        return self.result