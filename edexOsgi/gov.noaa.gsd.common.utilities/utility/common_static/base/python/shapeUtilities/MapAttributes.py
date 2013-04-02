"""
Description: Describes the characteristics of
 maps, their names, and products and sites for which they
 apply.   This is used in conjunction with the MapInfo class.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""

def cwazones(atts):
    if atts.has_key('zone') and len(atts['zone']) == 3 and \
      atts.has_key('state') and len(atts['state']) == 2:
        return atts['state'] + "Z" + atts['zone']
    else:
        return ""   #bad attributes
#    if atts.has_key('ZONE') and len(atts['ZONE']) == 3 and \
#      atts.has_key('STATE') and len(atts['STATE']) == 2:
#        return atts['STATE'] + "Z" + atts['ZONE']
#    else:
#        return ""   #bad attributes

def fips(atts):
    #make sure FIPS attribute exists and of proper length
    #make sure STATE attribute exists and of proper length
    if atts.has_key('fips') and len(atts['fips']) == 5 and \
      atts.has_key('state') and len(atts['state']) == 2:
        fips = atts['fips'][-3:]   #last 3 digits from FIPS code
        s = atts['state'] + "C" + fips
        return s
    else:
        return ""
    
def cwaFilter(atts, cwa):
    """
    function that filters by site
    """
    if atts['CWA'] == cwa:
        return 1
    else:
        return 0

def countyCWAFilter(atts, cwa):
    """
    functions that filters by site
    """
    cwaString = atts['CWA']
    if cwaString[0:3] == cwa or cwaString[3:6] == cwa:
        return 1
    else:
        return 0

#-----------------------------------------------------------------
# mapBaseType - for a product, defines the map type to be used
# key = product category, key = (product category, site4id)
#-----------------------------------------------------------------
mapBaseType = {}
mapBaseType['WSW'] = "publicZones"
mapBaseType['TOR'] = "counties"
mapBaseType[('TOR','PAFC')] = "publicZones"
mapBaseType['FFA'] = "publicZones"
mapBaseType['FLW'] = "publicZones"

#-----------------------------------------------------------------
# mapBaselineType - for a product, defines the map type to be used
#-----------------------------------------------------------------
mapBasename = {}
mapBasename['publicZones'] = "z_14fe06"
mapBasename['counties'] = "c_16mr06"

#-----------------------------------------------------------------------
# ugcFromAttributes - provides func used to convert attrs into ugc codes
#-----------------------------------------------------------------------
ugcFromAttributes = {}
ugcFromAttributes["publicZones"] = cwazones
ugcFromAttributes["counties"] = fips

#-----------------------------------------------------------------------
# filterBySite - provides func used to determine if record is in site
#-----------------------------------------------------------------------
filterBySiteFunc = {}
filterBySiteFunc["publicZones"] = cwaFilter
filterBySiteFunc["counties"] = countyCWAFilter
