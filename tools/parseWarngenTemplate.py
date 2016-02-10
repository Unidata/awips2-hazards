import re, sys, os
from collections import OrderedDict
import json
import argparse

###############################################################
#  parseWarngenTemplate.py parses warngen .vm templates and
#  expects files in the standard warngen .vm format.  This
#  script is designed specifically to handle the standard
#  burnScarInfo.vm and damInfo.vm files.  Run script without
#  arguments or parseWarngenTemplate.py -h to see usage
#
#  The standard damInfo.vm is similar to:
#
#if(${list.contains(${bullets}, "BigRockDam")})
    #set($riverName = "PHIL RIVER")
    #set($damName = "BIG ROCK DAM")
    #set($cityInfo = "EVAN...LOCATED ABOUT 3 MILES")
#end
#if(${list.contains(${bullets}, "BigRockhighfast")})
    #set($scenario = "IF A COMPLETE FAILURE OF THE DAM OCCURS...THE WATER DEPTH AT EVAN COULD EXCEED 18 FEET IN 16 MINUTES.")
#end
#if(${list.contains(${bullets}, "BigRockhighnormal")})
    #set($scenario = "IF A COMPLETE FAILURE OF THE DAM OCCURS...THE WATER DEPTH AT EVAN COULD EXCEED 23 FEET IN 31 MINUTES.")
#end
#if(${list.contains(${bullets}, "BigRockmediumfast")})
    #set($scenario = "IF A COMPLETE FAILURE OF THE DAM OCCURS...THE WATER DEPTH AT EVAN COULD EXCEED 14 FEET IN 19 MINUTES.")
#end
#if(${list.contains(${bullets}, "BigRockmediumnormal")})
    #set($scenario = "IF A COMPLETE FAILURE OF THE DAM OCCURS...THE WATER DEPTH AT EVAN COULD EXCEED 17 FEET IN 32 MINUTES.")
#end
#if(${list.contains(${bullets}, "BigRockruleofthumb")})
    #set($ruleofthumb = "FLOOD WAVE ESTIMATE BASED ON THE DAM IN IDAHO: FLOOD INITIALLY HALF OF ORIGINAL HEIGHT BEHIND DAM AND 3-4 MPH; 5 MILES IN 1/2 HOURS; 10 MILES IN 1 HOUR; AND 20 MILES IN 9 HOURS.")
#end
#if(${list.contains(${bullets}, "BranchedOakDam")})
    #set($riverName = "KELLS RIVER")
    #set($damName = "BRANCHED OAK DAM")
    #set($cityInfo = "DANGELO...LOCATED ABOUT 6 MILES")
#end
#
# The standard burnScarInfo.vm is similar to
#
##if(${list.contains($bullets, "FourMileBurnArea")})
    #set($burnScarName = "FOUR MILE BURN AREA")
    #set($burnScarEnd = " OVER THE FOUR MILE BURN AREA")
    #set($emergencyHeadline = "AREAS IN AND AROUND THE ${burnScarName}")
#end
#if(${list.contains($bullets, "fourmilelowimpact")})
  #set($ctaSelected = "YES")
  #set($burnScar = "THIS IS A LIFE THREATENING SITUATION.  HEAVY RAINFALL WILL CAUSE EXTENSIVE AND SEVERE FLASH FLOODING OF CREEKS...STREAMS...AND DITCHES IN THE FOURMILE BURN AREA.")
  #set($burnDrainage = "SOME DRAINAGE BASINS IMPACTED INCLUDE FOURMILE CREEK...GOLD RUN...AND FOURMILE CANYON CREEK.")
  #set($burnCTA = "THIS IS A LIFE THREATENING SITUATION.  HEAVY RAINFALL WILL CAUSE EXTENSIVE AND SEVERE FLASH FLOODING OF CREEKS...STREAMS...AND DITCHES IN THE FOURMILE BURN AREA.  SOME DRAINAGE BASINS IMPACTED INCLUDE FOURMILE CREEK...GOLD RUN...AND FOURMILE CANYON CREEK.  SEVERE DEBRIS FLOWS CAN ALSO BE ANTICIPATED ACROSS ROADS.  ROADS AND DRIVEWAYS MAY BE WASHED AWAY IN PLACES.  IF YOU ENCOUNTER FLOOD WATERS...CLIMB TO SAFETY.")
#end
#if(${list.contains($bullets, "fourmilehighimpact")})
  #set($ctaSelected = "YES")
  #set($burnScar = "THIS IS A LIFE THREATENING SITUATION FOR PEOPLE ALONG BOULDER CREEK IN THE CITY OF BOULDER...IN THE FOURMILE BURN AREA...AND IN BOULDER CANYON.  HEAVY RAINFALL WILL CAUSE EXTENSIVE AND SEVERE FLASH FLOODING OF CREEKS AND STREAMS FROM THE FOURMILE BURN AREA DOWNSTREAM THROUGH THE CITY OF BOULDER.")
  #set($burnDrainage = "SOME DRAINAGE BASINS IMPACTED INCLUDE BOULDER CREEK...FOURMILE CREEK...GOLD RUN...FOURMILE CANYON CREEK...AND WONDERLAND CREEK.")
  #set($burnCTA = "THIS IS A LIFE THREATENING SITUATION FOR PEOPLE ALONG BOULDER CREEK IN THE CITY OF BOULDER...IN THE FOURMILE BURN AREA...AND IN BOULDER CANYON.  HEAVY RAINFALL WILL CAUSE EXTENSIVE AND SEVERE FLASH FLOODING OF CREEKS AND STREAMS FROM THE FOURMILE BURN AREA DOWNSTREAM THROUGH THE CITY OF BOULDER.  SOME DRAINAGE BASINS IMPACTED INCLUDE BOULDER CREEK...FOURMILE CREEK...GOLD RUN...FOURMILE CANYON CREEK...AND WONDERLAND CREEK.  SEVERE DEBRIS FLOWS CAN ALSO BE ANTICIPATED ACROSS ROADS.  ROADWAYS AND BRIDGES MAY BE WASHED AWAY IN PLACES. IF YOU ENCOUNTER FLOOD WATERS...CLIMB TO SAFETY.")
#end
#
#  25 Aug 2015 :: kevin.manross@noaa.gov (GSD)
###############################################################


parser = argparse.ArgumentParser(description='Convert Warngen templates [damInfo.vm, burnScarInfo.vm] into python dictionaries', usage='%(prog)s [--metaType (damInfo, burnScar)] SITE /path/to/warngen/file.vm')
parser.add_argument('--metaType', default='damInfo', help='Choose whether parsing burnScar or damInfo vm file. (Default is \'damInfo\')', choices=['damInfo', 'burnScar'])
parser.add_argument('localizedSite', metavar='SITE',  help='Enter localized site (I.e. BOU)')
parser.add_argument('warngenVMFile', metavar='/path/to/warngen/file.vm',  help='Enter full path to warngen .vm file')
args = parser.parse_args()

filename = args.warngenVMFile
localSite = args.localizedSite
metaType = args.metaType

OUTPUTDIR = '/awips2/edex/data/utility/common_static/site/'+localSite+'/python/textUtilities/'
if not os.path.exists(OUTPUTDIR):
    os.makedirs(OUTPUTDIR)

if args.metaType == 'damInfo':
    metaType = 'damInundationMetadata'
    outputFile = 'DamMetaData.py'
else:
    metaType = 'burnScarAreaMetadata'
    outputFile = 'BurnScarMetaData.py'

FULLOUTPUTPATH = os.path.join(OUTPUTDIR, outputFile)

try:
    fh = open(filename, 'r')
except:
    print '\n\tUnable to open file "' + filename + '". Exiting...\n'
    sys.exit()
lines = fh.readlines()
fh.close()

### Have to make a few assumptions, such as: this will be run
### for damInfo.vm or burnScarInfo.vm and is therefore the first 
### labels they encounter will have a suffix of 'Dam' or 
### 'BurnArea', respectively.
### This method checks to see if the string is a potential
### "header string" which is the start of a new set of metadata
### for that particular dam or burn area. User can specify whether
### they want the whole string returned or just the unique prefix
def checkHeader(str, returnPrefixOnly=False):
    headers = ['Dam', 'BurnArea']
    for h in headers:
        if h in str:
            returnHeader = str
            if returnPrefixOnly:
                returnHeader = str.replace(h, '')
            return returnHeader

    return None

### We can get some "empty" key, value pairs, so 
### delete them.
### for node in dict
###     if len(node) == 0, then del node
def cleanUp(d):
  for k, v in d.iteritems():
    if isinstance(v, OrderedDict):
      if len(v):
        cleanUp(v)
      else:
        del d[k]


quotePattern = r'"(.*)"'
dollarPattern = r'\$\w+'
equalsPattern = r'='
bigDict = OrderedDict()

prevLine = ''
header = None
prevSubkey = None

for line in lines:
    equalsSearch = re.search(equalsPattern, line)
    quoteSearch = re.search(quotePattern, line)
    dollarSearch = re.search(dollarPattern, line)
    prevQuoteSearch = re.search(quotePattern, prevLine)

    if equalsSearch is None and quoteSearch is not None:
        quoteMatch = quoteSearch.group().replace('"', '').strip()
        header = checkHeader(quoteMatch)

        if header is not None:
            bigDict[header] = OrderedDict()
        else:
            header = prevHeader

        if len(bigDict):
            subkey = quoteMatch
            if subkey != header:
                bigDict[header][subkey] = OrderedDict()

            

    if equalsSearch is not None and dollarSearch is not None and quoteSearch is not None:
        key = dollarSearch.group()[1:]
        value = quoteSearch.group().replace('"', '').strip()

        ### Check if any "dynamic variables" are present and replace
        ### I.e., replace ${burnScarName} with the value of 'burnScarName'
        dynVar =  re.findall('\$\{(\w+)\}', value)
        for v in dynVar:
            rep = bigDict[header].get(v)
            if rep is not None:
                value = value.replace('${'+v+'}', rep)



        ### Special for DamInfo case. Place all scenarios into subdict
        if 'cenario' in line and prevQuoteSearch is not None:

            ### Strip specific dam name prefix off of RoT 
            fullScenKey = prevQuoteSearch.group().replace('"', '').strip()
            prefix = checkHeader(header, True)
            scenKey = fullScenKey.replace(prefix, '')

            if 'scenarios' in bigDict[header]:
                bigDict[header]['scenarios'].update({scenKey:value})
            else:
                bigDict[header]['scenarios'] = OrderedDict()
                bigDict[header]['scenarios'][scenKey] = value

        ### Special for DamInfo case. RoT is in pattern that 
        ### would make it a subdict, but "want it up a level" as sibling
        ### to $riverName, $damName, $cityInfo
        elif 'ruleofthumb' in line and prevQuoteSearch is not None:
            rotKey = prevQuoteSearch.group().replace('"', '').strip()
            bigDict[header][key] = value


        ### Handles other sub-dicts, including burnScarInfo
        elif len(bigDict[header].keys()):

            latestSubkey = bigDict[header].keys()[-1]

            if isinstance(bigDict[header][latestSubkey], OrderedDict):
                bigDict[header][latestSubkey].update({key : value})
            else:
                bigDict[header].update({key : value})

        ### Catch anything else
        else:
            bigDict[header].update({key : value})



    if re.search('End of example', line):
        break

    prevHeader = header
    prevLine = line


cleanUp(bigDict)
outputString = '\n\n' + metaType + ' = ' + json.dumps(bigDict, indent=4)



try:
    ### Append new dictionary to end of existing file.  
    ### Inform user to examine output file for accuracy when done.
    outputFH = open(FULLOUTPUTPATH, 'a')
    outputFH.write(outputString)
    outputFH.close()
except:
    print '\n\tCould not open ' + FULLOUTPUTPATH + ' for writing.  Exiting...\n'
    sys.exit()


print '''\n\nYou have successfully updated
\n%s\n
by APPENDING a new dictionary to the end of the file.
Please examine
\n%s\n
and confirm your updates.
This may require merging the new dictionaries in the file
with existing dictionaries\n\n''' %(FULLOUTPUTPATH,FULLOUTPUTPATH)


