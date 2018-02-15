#!/usr/bin/python

import re, sys, os
from collections import OrderedDict
import json
import argparse
import xml.etree.ElementTree as ET

workingElementList = []

def findXMLelements(element) :
    global workingElementList
    if element.attrib.has_key("bulletGroup") and \
       element.attrib.has_key("bulletText") and \
       element.attrib.has_key("bulletName") :
        if element.attrib["bulletGroup"]=="dam" or \
           element.attrib["bulletGroup"]=="scenario" or \
           element.attrib["bulletGroup"]=="burnscenario" :
            workingElementList.append(element)
    for child in element:
        findXMLelements(child)


###############################################################
#  25 Aug 2015 :: kevin.manross@noaa.gov (GSD)
#   4 Oct 2016 :: joseph.s.wakefield@noaa.gov - corrected handling of example/
#                 comments and changed from append to rewrite per user feedback
#  31 Jan 2017 :: jramer (GSD) - now processes companion xml file to get impact
#                 areas to write into shape file; properly parses non-dam specific
#                 metadata; both site and file argument can now normally be just '-'.
###############################################################

parser = argparse.ArgumentParser(description='Convert WarnGen template [damInfo.vm, burnScarInfo.vm] into python dictionary', usage='%(prog)s [--metaType {damInfo,burnScar}] SITE /path/to/warngen/file.vm')
parser.add_argument('--metaType', default='damInfo', help='Choose whether parsing burnScar or damInfo vm file. (Default is \'damInfo\')', choices=['damInfo', 'burnScar'])
parser.add_argument('localizedSite', metavar='SITE', help='Local site ID (e.g. BOU)')
parser.add_argument('warngenVMFile', metavar='/path/to/warngen/file.vm', help='Full path to WarnGen .vm file')
args = parser.parse_args()

filename = args.warngenVMFile
localSite = args.localizedSite

cmdstr = 'parseWarngenTemplate.py --metaType '
if args.metaType == 'burnScar' :
    cmdstr += 'burnScar'
else :
    cmdstr += 'damInfo'
cmdstr += ' ' + filename + ' ' + localSite

# Look up primary site if not specified.
if len(localSite)<=1 :
    try:
        sss = open("/awips2/edex/bin/setup.env", "r")
        lines = sss.read().split("\n")
    except:
        import subprocess
        p = subprocess.Popen('ssh dx3 "cat /awips2/edex/bin/setup.env"', \
                               shell=True, stdout=subprocess.PIPE)
        (sss, stderr) = p.communicate()
        lines = sss.split("\n")
    for line in lines:
        if line.find("AW_SITE_IDENTIFIER")<0 :
            continue
        localSite = line.split('=')[1].replace(' ','').replace('\n','')
        break
    if len(localSite)<=1 :
        print '\n\tUnable to determine primary site, Exiting...\n'
        sys.exit()

OUTPUTDIR = '/awips2/edex/data/utility/common_static/configured/'+localSite+'/HazardServices/python/textUtilities/'
if not os.path.exists(OUTPUTDIR):
    os.makedirs(OUTPUTDIR)

shapeFileName = args.metaType
if args.metaType == 'damInfo':
    metaType = 'DamMetaData'
    outputFile = 'DamMetaData.py'
    xmlName = 'damInfoBullet.xml'
    if len(filename)<=1 :
        filename = "/awips2/edex/data/utility/common_static/site/"+localSite+"/warngen/damInfo.vm"
else:
    metaType = 'BurnScarMetaData'
    outputFile = 'BurnScarMetaData.py'
    xmlName = 'burnScarInfoBullet.xml'
    if len(filename)<=1 :
        filename = "/awips2/edex/data/utility/common_static/site/"+localSite+"/warngen/burnScarInfo.vm"
sf_fields = [('DeletionFlag', 'C', 1, 0), ['Name', 'C', 80, 0]]

FULLOUTPUTPATH = os.path.join(OUTPUTDIR, outputFile)

try:
    fh = open(filename, 'r')
except:
    print '\n\tUnable to open file "' + filename + '". Exiting...\n'
    sys.exit()
lines = fh.readlines()
fh.close()

# Now we need to read both the .vm and its companion xml file; we assume
# both are in same directory.
xmlPath = os.path.dirname(filename)+"/"+xmlName
try :
    tree = ET.parse(xmlPath)
    oneroot = tree.getroot()
except:
    try :
        # Try to open again to verify whether access failure is because
        # the file was badly formed xml.
        fh = open(xmlPath, 'r')
        print '\n\tUnable to parse as xml: "' + xmlPath + '". Exiting...\n'
    except:
        print '\n\tUnable to open file "' + xmlPath + '". Exiting...\n'
    sys.exit()

try:
    import shapefile
    w = shapefile.Writer(shapefile.POLYGON)
except:
    w = None
    print "\nWARNING!!! Was unable to import package required for"
    print "writing out shapefiles.\n"

### Have to make a few assumptions, such as: this will be run for damInfo.vm
### or burnScarInfo.vm and therefore the first labels they encounter will have
### a suffix of 'Dam' or 'BurnArea', respectively.
### This method checks to see if the string is a potential "header string" which
### is the start of a new set of metadata for that particular dam or burn area.
### User can specify whether they want the whole string returned or just the
### unique prefix.
def checkHeader(str, returnPrefixOnly=False):
    headers = ['Dam', 'BurnArea']
    for h in headers:
        if h in str:
            returnHeader = str
            if returnPrefixOnly:
                returnHeader = str.replace(h, '')
            return returnHeader
    return None

### We can get some "empty" key, value pairs, so delete them.
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
inComment = False
commentStart = '#\*'
commentEnd = '\*#'
commentLine = '##'

for line in lines:

### Skip comment lines or comment blocks
    if inComment:
        if re.match(commentEnd, line):
            inComment = False
            continue
        else:
            continue
    elif re.match(commentStart, line):
        inComment = True
        continue
    elif re.match(commentLine, line):
        continue

    equalsSearch = re.search(equalsPattern, line)
    quoteSearch = re.search(quotePattern, line)
    dollarSearch = re.search(dollarPattern, line)
    prevQuoteSearch = re.search(quotePattern, prevLine)

    if equalsSearch is None and quoteSearch is not None:
        quoteMatch = quoteSearch.group().replace('"', '').strip()
        header = checkHeader(quoteMatch)

        if not isinstance(header, str) :
            header = prevHeader
        elif not header in bigDict :
            bigDict[header] = OrderedDict()
        elif line.find("endsWith")>0 :
            header = prevHeader = None

        if len(bigDict) and isinstance(header, str) :
            subkey = quoteMatch
            if subkey != header:
                bigDict[header][subkey] = OrderedDict()

    ### Empty variable value
    if not isinstance(header, str) :
        continue

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

        ### Empty variable value
        elif header == None :
            continue

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

    prevHeader = header
    prevLine = line

cleanUp(bigDict)

# outputString = '\n\n' + metaType + ' = ' + json.dumps(bigDict, indent=4)
# try:
#     ### Write new dictionary to file.  
#     ### Inform user to examine output file for accuracy when done.
#     outputFH = open("/tmp/metaPreProm.py", 'w')
#     outputFH.write(outputString)
#     outputFH.close()
#     print '\nwrote /tmp/metaPreProm.py\n'
# except:
#     print '\n\tCould not open /tmp/metaPreProm.py for writing..\n'

# Were this something that was running in real-time, for sake of efficiency I
# would fix the immediately preceding logic.  However, it is much easier to
# make another pass through this and promote back to the top anything containing
# a "damName".
promotions = {}
for key in bigDict :
    value = bigDict[key]
    if not isinstance(value, dict) :
        continue
    vkeys = value.keys()
    while len(vkeys) > 0 :
        subkey = vkeys[0]
        vkeys = vkeys[1:]
        subval = value[subkey]
        if not isinstance(subval, dict) :
            continue
        if "damName" in subval :
            del value[subkey]
            promotions[subkey] = subval
    bigDict[key] = value
bigDict.update(promotions)

# Were this something that was running in real-time, for sake of efficiency I
# would fix the immediately preceding logic.  However, it is much easier to
# make another pass through this and reconfigure any "scenarios" entries that
# are not dictionaries..
for key in bigDict :
    try :
        scenarios = bigDict[key]["scenarios"]
    except :
        continue
    if not isinstance(scenarios, dict) :
        del bigDict[key]["scenarios"]
        continue
    for scenKey in scenarios :
        scenEntry = scenarios[scenKey]
        if isinstance(scenEntry, dict) :
            continue # scenario entry is already dict, this is fine
        if isinstance(scenEntry, str) :
            dictFromStr = {"productString": scenEntry}
            if scenKey[:4]=="high" :
                dictFromStr["displayString"] = scenKey[:4]+" "+scenKey[4:]
            elif scenKey[:6]=="medium" :
                dictFromStr["displayString"] = scenKey[:6]+" "+scenKey[6:]
            elif scenKey[-4:]=="fast" :
                dictFromStr["displayString"] = scenKey[:-4]+" "+scenKey[-4:]
            elif scenKey[-6:]=="normal" :
                dictFromStr["displayString"] = scenKey[:-6]+" "+scenKey[-6:]
            else :
                dictFromStr["displayString"] = scenKey
            bigDict[key]["scenarios"][scenKey] = dictFromStr
        else :
            del bigDict[key]["scenarios"][scenKey]

# outputString = '\n\n' + metaType + ' = ' + json.dumps(bigDict, indent=4)
# try:
#     ### Write new dictionary to file.  
#     ### Inform user to examine output file for accuracy when done.
#     outputFH = open("/tmp/metaPreXml.py", 'w')
#     outputFH.write(outputString)
#     outputFH.close()
#     print '\nwrote /tmp/metaPreXml.py\n'
# except:
#     print '\n\tCould not open /tmp/metaPreXml.py for writing..\n'

# Now we step through the xml and find those elements with attributes of
# bulletText, bulletName, and possibly coords, and add them to the bigDict.
findXMLelements(oneroot)
damKey = "@@@"
for element in workingElementList :
    if element.attrib["bulletGroup"]=="dam" :
        bulletKey = element.attrib["bulletName"]
        i = bulletKey.find("Dam")
        if i<=0 :
            damKey = "@@@"
        else :
            damKey = bulletKey[:i]
    elif element.attrib["bulletGroup"]=="burnscenario" :
        scenarioId = element.attrib["bulletName"]
        mainBurn = None
        for k in bigDict.keys() :
            if scenarioId in bigDict[k] :
                mainBurn = k
                break
        if mainBurn == None :
            continue
        scenarioTop = bigDict[mainBurn][scenarioId]
        scenarioEntry = {}
        for k in scenarioTop.keys() :
            if k == "burnScar" :
                scenarioEntry["productString"] = scenarioTop[k]
            elif k != "ctaSelected" :
                scenarioEntry[k] = scenarioTop[k]
        dispStr = element.attrib["bulletText"]
        i = dispStr.find('-')
        if i >= 0 :
           n = len(dispStr)
           i += 1
           while i<n and dispStr[i]==' ':
               i += 1
           if n-i>5 :
              dispStr = dispStr[i:]
        scenarioEntry["displayString"] = dispStr
        del bigDict[mainBurn][scenarioId]
        if "scenarios" in bigDict[mainBurn] :
            bigDict[mainBurn]["scenarios"][scenarioId] = scenarioEntry
        else :
            bigDict[mainBurn]["scenarios"] = { scenarioId : scenarioEntry }
        continue
    else :
        if element.attrib["bulletName"].find(damKey)==0 :
            scenarioId = element.attrib["bulletName"][len(damKey):]
            if not "scenarios" in bigDict[bulletKey]:
                continue
            prodStr = bigDict[bulletKey]["scenarios"].get(scenarioId)
            if not isinstance(prodStr,str) :
                continue
            dispStr = element.attrib["bulletText"]
            i = dispStr.find('-')
            if i >= 0 :
               n = len(dispStr)
               i += 1
               while i<n and dispStr[i]==' ':
                   i += 1
               if n-i>5 :
                  dispStr = dispStr[i:]
            bigDict[bulletKey]["scenarios"][scenarioId] = \
              { "displayString": dispStr, "productString": prodStr }
        continue
    if not bigDict.has_key(bulletKey) :
        print "Warning, xml bullet named "+bulletKey+ \
              " could not be matched to .vm"
        damKey = "@@@"
        continue
    bigDict[bulletKey]["dropDownLabel"] = element.attrib["bulletText"]
    if element.attrib.has_key("coords") :
        bigDict[bulletKey]["coords"] = element.attrib["coords"]

# Now attempt to create a shapefile representing any features that have
# coordinates associated with them.
finalDict = OrderedDict()
anyshapes = False
if w :
    w.fields = list(sf_fields)
for featureKey in bigDict.keys() :
    if featureKey=="Dam" :
        genericDict = {}
        for k in bigDict[featureKey].keys() :
            if k == "emergencyHeadline" or k == "ctaSelected" :
                continue
            v = bigDict[featureKey][k]
            if v == "YES" or v == "NO" :
                genericDict[k] = v
                continue
            # Clean out some vm hooks Haz Serv does not need
            v = v.replace("#capitalize(", "").replace(" 'ALL')", "")
            # Potentially start full sentence with upper case. 
            if v[-1:] == '.' :
                v = v[0]+v[1:].lower()
            else :
                v = v.lower()
            # restore camel case of some variable names.
            v = v.replace("name}", "Name}").replace("type2}", "Type2}").replace("info}", "Info}")
            genericDict[k] = v
        finalDict[featureKey] = genericDict
        continue
    feature = bigDict[featureKey]
    if not feature.has_key("dropDownLabel") :
        print "Warning, vm info with key "+featureKey+\
              " was not matched to entry in xml."
    if feature.has_key("burnScarName") :
        nameAtt = feature["burnScarName"]
    elif feature.has_key("damName") :
        nameAtt = feature["damName"]
    else :
        print "Warning, vm info with key "+featureKey+\
              "has no useable name attribute."
        finalDict[featureKey] = feature
        continue
    feature["featureID"] = featureKey
    if not feature.has_key("coords") :
        finalDict[nameAtt] = feature
        continue
    cdata = feature["coords"].split(' ')
    del feature["coords"]
    finalDict[nameAtt] = feature
    if not w :
        continue
    outpts = []
    if cdata[0].find('L')>=0 :
        cdata = cdata[1:]
    while len(cdata)>=2 :
        try :
            outpts.append( ( int(cdata[1])*-0.01 , int(cdata[0])*0.01 ) )
        except :
            sys.stderr.write("Poorly formatted lat/lon data in xml:\n"+\
                             cdata[0]+" "+cdata[1]+"\n")
        cdata = cdata[2:]
    outpts.append(outpts[0])
    if len(outpts)<4 :
        print "Could not make polygon for "+featureKey
        continue
    anyshapes = True
    if shapefile.signed_area(outpts)>0:
        outpts.reverse()
    w.poly(parts=[outpts])
    w.record(nameAtt)

if anyshapes :
    shapeoutdir = '/awips2/edex/data/utility/common_static/configured/'+localSite+'/shapefiles/hazardServices'
    if not os.path.exists(shapeoutdir):
        os.makedirs(shapeoutdir)
    shapeoutname = shapeoutdir+'/'+shapeFileName
    w.save(shapeoutname)
    print "saved "+shapeoutname+".{dbf,shp,shx} as shape file set."

# Now recreate our main dictionary keyed by the 'name' rather than the
#

outputString = '\n\n' + metaType + ' = ' + json.dumps(finalDict, indent=4)

try:
    ### Write new dictionary to file.  
    ### Inform user to examine output file for accuracy when done.
    outputFH = open(FULLOUTPUTPATH, 'w')
    outputFH.write(outputString)
    outputFH.close()
except:
    print '\n\tCould not open ' + FULLOUTPUTPATH + ' for writing. Exiting...\n'
    sys.exit()

print '''\nYou have successfully updated
%s
by writing a new dictionary to the file.

Please examine
%s
and confirm your updates.''' %(FULLOUTPUTPATH,FULLOUTPUTPATH)

#
# Record the exact form of the parseWarngenTemplate.py command used, just in
# case it is not default and we need to reuse same form later
#
cmdFH = open(os.environ.get("HOME")+'/wgn2hazSer_commands.txt',"a")
cmdFH.write(cmdstr+"\n")
cmdFH.close()
