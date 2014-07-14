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
# Helps with manipulating discrete grids since they come over as bytes
# and are not extremely straightforward value-wise
#
#
#     SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    05/28/13                      mnash       Initial Creation.
#
#
#

def getValue(value, keys):
    """
    @summary: This method returns the value of the key at a certain grid cell value
    @param value: the value of the grid cell
    @param keys: the discrete key values
    """
    return keys[value]

def convertKeysToList(keys):
    """
    @summary: Discrete grids retrieved through the DAF send the keys
    back as an attribute, a string value.  This splits that out and 
    sends them back as a list.
    @param keys: The string value of keys, separated by commas
    """
    return keys.split(',')

def getValuesToSearch(keys):
    """
    @summary: Takes the keys and returns which of those values to search (if they are combined for instance)
    @param keys: The value of the keys as a String
    @return: a dictionary of keys to search
    """
    if isinstance(keys, list) is False :
        keys = convertKeysToList(keys)
    toSearch = dict()
    for val in xrange(len(keys)) :
        keyval = keys[val]
        # since multiple valued keys are split by ^ we want to add
        # those values to the lists as well
        strsplit = keyval.split("^")
        if len(strsplit) == 1 :
            _addToSearchDict(keyval, val, toSearch)
        else :
            for splitval in strsplit :
                _addToSearchDict(splitval, val, toSearch)
    return toSearch

def _addToSearchDict(keyval, val, toSearch):
    if keyval in toSearch :
        toSearch[keyval].append(val)
    else :
       toSearch[keyval] = list()
       toSearch[keyval].append(val)

def convertArray(grid, keys):
    strings = list()
    for x in xrange(grid):
        strings.append(keys[x])
    return strings
