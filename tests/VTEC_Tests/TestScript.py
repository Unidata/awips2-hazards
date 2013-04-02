# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# Generalized method for returning a TestScript to TextProductTest
#
# Author:
# ----------------------------------------------------------------------------

def generalTestScript(scripts, defaults={}):
    # "scripts" is a list test scripts each in dictionary form
    # Each script has the fields described in TextProductTest
    #
    # In addition, it may have the following fields to be processed
    # by this method to further set up the TestScript for TextProductTest
    #  "defaults" is an optional dictionary of default entries that apply
    #    to all scripts IF they do not have their own entries.  For example,
    #
    #    defaults = {"decodeVTEC": 1"}
    #
    #    Then any scripts that do not have a decodeVTEC entry will set it to 1
    #    (instead of the normal default of 0).
    #
    tScript = []
    for script in scripts:
        newScript =  {
            "name": script["name"],
            "productType": getValue(script, "productType", defaults, None),
            "commentary": getValue(script, "commentary", defaults, None), 
            "checkMethod": getValue(script, "checkMethod", defaults, None),
            "checkStrings": getValue(script, "checkStrings", defaults, None),
            "createGrids": getValue(script, "createGrids", defaults, []),
            "gridsStartTime": getValue(script, "gridsStartTime", defaults, None),
            "geoType": getValue(script, "geoType", defaults, 'area'),
            "drtTime": getValue(script, "drtTime", defaults, None),
            "decodeVTEC": getValue(script, "decodeVTEC", defaults, 0),
            "clearHazardsTable": getValue(script, "clearHazardsTable", defaults, 0),
            "vtecMode": getValue(script, "vtecMode", defaults, "O"),
            }
        tScript.append(newScript)
    return tScript

def getValue(script, key, defaults, default):
    return script.get(key, defaults.get(key, default))

