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
    @summary: This method returns 
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
    strkeys = keys.split(',')
    finalList = list()
    for key in strkeys :
        finalList.append(str(key))
    return finalList

def getValuesToSearch(keys):
    """
    
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
            if keyval in toSearch :
                toSearch[keyval].append(val)
            else :
                toSearch[keyval] = list()
                toSearch[keyval].append(val)
        else :
            for splitval in strsplit :
                if splitval in toSearch :
                    toSearch[splitval].append(val)
                else :
                    toSearch[splitval] = list()
                    toSearch[splitval].append(val)
    return toSearch

def convertArray(grid, keys):
    strings = list()
    for x in xrange(grid):
        strings.append(keys[x])
    return strings
