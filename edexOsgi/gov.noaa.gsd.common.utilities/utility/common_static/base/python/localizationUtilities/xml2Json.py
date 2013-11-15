import xml.etree.ElementTree as ET

import json
import os
import sys
import collections

initDone = False

# This is a set of keys that are always stripped out of the structure that
# returned via the simplify method.
stripKeys = set( [ "listParser", "outerDelimeter", "innerDelimeter",
                   "innerKeyIndex", "modifierAttribute", "listMember" ] )

# modifyName is a list of attributes for which their values are automatically
# adopted as the namespace identifier for an element that is treated as a
# dictionary entry.  modifyTrail is a list of possible suffixes for these
# attribute names.
modifyName = set( [ "xsi:type", "key" ] )
modifyTrail = set( [ "Id", "Name" ] )

#byModifier = { "include" : "file",
#               "pointSource" : "variable" }
byModifier = { }

# keeps track of items that, because there are multiple occurences in the
# same name space, are thereafter treated as lists.  It is possible for
# an item to self identify itself as a list member, and one can add list
# members through xml2Json.xml.
listMember = set( [] )
#listParser = { "mainWarngenProducts" : ( ',' , '/' , 1) ,
#               "otherWarngenProducts" : ( ',' , '/' , 1) }
listParser = { }
overrideStates = { "byKeys" :  "_override_by_keys_",
                   "byKey" :  "_override_by_key_",
                   "byContent" :  "_override_by_content_",
                   "unique" :  "_override_unique_",
                   "additive" :  "_override_additive_",
                   "prepend" :  "_override_prepend_",
                   "append" :  "_override_append_" }
overrideActions = { "remove" : "_override_remove_",
                    "removeOne" : "_override_remove_",
                    "lockOne" : "_override_lock_one_",
                    "removeList" : "_override_remove_list_",
                    "removeStop" : "_override_remove_stop_" ,
                    "removeBefore" : "_override_remove_before_" ,
                    "removeAfter" : "_override_remove_after_" ,
                    "insertBefore" : "_override_insert_before_" ,
                    "insertAfter" : "_override_insert_after_" ,
                    "removeRange" : "_override_remove_range_" ,
                    "removeBetween" : "_override_remove_between_" }

#
# The purpose of this class is to take a string that is either a path to an
# xml file or is directly XML data and convert it into objects that are
# serializable into JSON. JSON is completely unambiguous as to when items
# belong to lists and when they belong to dictionaries.  With XML it can be
# tricky to recognize when it is best to treat a piece of XML data as a
# dictionary entry and when it is best to treat it as a list member.  The
# companion xml file xml2Json.xml is used to give this parser hints for
# how to do this when needed.  This basic function is performed for the
# client using the convert() method.
#
# The object creation is done such that it is possible to use the resulting
# data structure to recreate an XML file that is nearly identical to the
# input XML file.  This results in some extraneous items being added that
# are of limited use in interpreting the resulting data structure as
# configuration data.  Giving this structure to the simplify() method 
# returns a data structure where these extraneous structures have been removed
# and consolidated, such that the data is more easily interpreted as
# configuration data.  
#
# Structures from the convert() method are also designed to be interoperable
# with the jsonCombine class.  If one wants to use jsonCombine with these
# structures, one should use jsonCombine first and then call the simplify()
# method (if desired) as a final step.
#
# A structure from the convert() method can be passed to the unconvert()
# method, and this will result in writing XML to stdout.  If a structure
# from the simplify() method is given to the unconvert() method, this
# will still produce properly formatted XML, but the order of tags may
# be different, and sometimes simple tag content will resolve as attributes.
#
# As these comments are being written (Feb 27 2013), it is expected
# that this class will be subject to significant future refactoring.
# It is minimally functional now, but the inline comments will be
# made more extensive when this refactoring is complete.
#

class xml2Json :

    def __init__(self) :
        global initDone
        global modifyName
        global modifyTrail
        global listMember
        global byModifier
        global listParser
        self.outputBuffer = None
        self.lineLength = 0
        self.orderStart = 1
        self.nextStart = 1
        self.xmlnsValue = ""
        if initDone :
            return
        initDone = True
        path = os.environ.get('XML2JSON')
        if path == None :
            path = "xml2Json.xml"
        try :
            tree = ET.parse(path)
        except :
            return
        topnode = tree.getroot()
        for onenode in topnode :
            aaa = onenode.attrib
            if not "tag" in aaa :
                continue
            lukey = aaa["tag"]
            if len(lukey)==0 :
                continue
            if "parent" in aaa :
                lukey = aaa["parent"]+"___"+lukey
            if onenode.tag=="listMember" :
                listMember.add(lukey)
                continue
            if onenode.tag=="byModifier" :
                if "modifierAttribute" in aaa :
                    byModifier[lukey] = aaa["modifierAttribute"]
                continue
            if onenode.tag=="listParser" :
                outerDel = aaa.get("outerDelimeter", ' ')
                innerDel = aaa.get("innerDelimeter", '')
                keyIdx = int(aaa.get("innerKeyIndex", -1))
                listParser[lukey] = (outerDel, innerDel, keyIdx)

    def newStack(self) :
        self.orderStart = 1
        self.nextStart = 1
        self.xmlnsValue = ""

    def implOne(self, elemRep) :
        if elemRep == None :
            return (None, False)
        elif "___" in elemRep:
            return (elemRep["___"], True)
        else :
            return (elemRep, False)

    # nodemode; -1=very top, 0=normal element, 1=list part, 2=list element
    # returns tuple of (tag, nodemode, parsed_data) 
    def parseOne(self, elem, parent, nodemode, order) :
        global modifyName
        global modifyTrail
        global listMember
        global byModifier
        global listParser

        if elem==None:
            if nodemode<0 :
                return {}
            return ("_", nodemode, None)
        if nodemode<0 :
            self.xmlnsValue = ""

        nnn = len(elem)
        aaa = elem.attrib
        lll = len(aaa)
        if order>0 and nodemode==0:
            aaa["__order__"] = order
            lll = len(aaa)

        # Handle possibility of parsing pure text into list    
        mytag = elem.tag
        textrep = ""
        if elem.text==None :
            ttt = 0
        else :
            textrep = elem.text.strip()
            ttt = len(textrep)
            oneParser = None
            if aaa.get("listParser","false")=="true" :
                outerDel = aaa.get("outerDelimeter", ' ')
                innerDel = aaa.get("innerDelimeter", '')
                keyIdx = int(aaa.get("innerKeyIndex", -1))
                oneParser = ( outerDel, innerDel, keyIdx)
                listParser[parent+"___"+elem.tag] = oneParser
            if oneParser == None :
                oneParser = listParser.get(parent+"___"+elem.tag)
            if oneParser == None :
                oneParser = listParser.get(elem.tag)
            if oneParser!=None and ttt>0 and nnn==0 :
                outerDel = oneParser[0]
                innerDel = oneParser[1]
                keyIdx = oneParser[2]
                outer = textrep.split(outerDel)
                if innerDel=='' :
                    textrep = outer
                elif keyIdx<0 :
                    textrep = []
                    for inner in outer :
                        indat = inner.split(innerDel)
                        if len(indat)==1 :
                            textrep.append(indat[0])
                        elif len(indat)>1 :
                            textrep.append(indat)
                else :
                    textrep = []
                    for inner in outer :
                        indat = inner.split(innerDel)
                        if len(indat)<=keyIdx :
                            continue
                        keydat = indat[keyIdx]
                        if len(keydat)==0 :
                            continue
                        del indat[keyIdx]
                        if len(indat)==0 :
                            textrep.append( {keydat : None} )
                        elif len(indat)==1 :
                            textrep.append( {keydat : indat[0]} )
                        else :
                            textrep.append( {keydat : indat} )
                ttt = len(textrep)
                if order>0 and nodemode==1:
                    aaa["__order__"] = order
                    lll = len(aaa)
                nodemode = 2
        if nnn+lll+ttt==0 :
            if nodemode<0 :
                return { mytag : None }
            return ( mytag, nodemode, None)

        # Grab contents of override attribute, check for key modifier
        overrideContents = []
        if "override" in aaa :
            overrideContents = aaa["override"].split()
            del aaa["override"]
        modAtt = aaa.get("modifierAttribute","")
        if modAtt in aaa:
            byModifier[parent+"___"+elem.tag] = aaa[modAtt]
        else :
            modAtt = byModifier.get(parent+"___"+elem.tag,"")
            if not modAtt in aaa:
                modAtt = byModifier.get(elem.tag,"")
            if not modAtt in aaa:
                modAtt = ""
        if len(modAtt)>0 :
            mytag = mytag+"___"+aaa[modAtt]
            if nodemode==1:
                if order>0 :
                    aaa["__order__"] = order
                nodemode = 0

        # Process our attribute dictionary, which includes unraveling xsi
        outdict = collections.OrderedDict({})
        if lll>0:
            for onekey in aaa.keys():
                if onekey=="listMember" and aaa[onekey]=="true" :
                    listMember.add(parent+"___"+elem.tag)
                    if nodemode==0 :
                        nodemode = 1
                    outdict[onekey] = aaa[onekey]
                    continue
                if onekey[0]!='{' :
                    outdict[onekey] = aaa[onekey]
                    continue
                i = onekey.find('}')
                if i<0 :
                    outdict[onekey] = aaa[onekey]
                    continue
                xmlnsVal = onekey[1:i]
                newkey = 'xsi:'+onekey[i+1:]
                outdict[newkey] = aaa[onekey]
                if len(self.xmlnsValue)>0 :
                    continue
                self.xmlnsValue = xmlnsVal
                outdict['xmlns:xsi'] = xmlnsVal
            lll = len(outdict)

        xTag = ""
        nc = len(mytag)
        for onekey in outdict.keys():
            if onekey in modifyName :
                xTag = outdict[onekey]
            elif mytag==onekey[:nc] and onekey[nc:] in modifyTrail :
                xTag = outdict[onekey]

        # Process contents of override attribute
        overridedata = []
        if len(overrideContents)==0 :
            pass
        elif nodemode==2 : # Pure text represented as list
            for oneentry in overrideContents :
                if oneentry == "replace" :
                    outdict["_override_replace_"] = True
                elif oneentry == "lock" :
                    outdict["_override_lock_"] = True
                elif oneentry == "remove" :
                    if nodemode<0 :
                        return { mytag : "_override_remove_" }
                    return ( mytag, nodemode, "_override_remove_")
                elif oneentry in overrideStates :
                    overridedata.append(overrideStates[oneentry])
                elif not oneentry in overrideActions and len(overridedata)>0:
                    if overridedata[-1]=="_override_by_key_" :
                        overridedata[-1] = "_override_by_key_"+oneentry+"_"
            for oneentry in overrideContents :
                if oneentry=="removeList" :
                    textrep.append("_override_remove_stop_")
                if oneentry in overrideActions :
                    overridedata.append(overrideActions[oneentry])
            if len(overridedata)>0 :
                overridedata.extend(textrep)
                textrep = overridedata
            overridedata = []
        elif nodemode==1 : # Element that is part of a list
            for oneentry in overrideContents :
                if oneentry == "replace" :
                    outdict["_override_replace_"] = True
                elif oneentry == "lock" :
                    outdict["_override_lock_"] = True
                elif oneentry == "removeAll" :
                    mytag = "___"+mytag+"___"
                    return ( mytag, nodemode, "_override_remove_")
                elif oneentry in overrideStates :
                    overridedata.append(overrideStates[oneentry])
                elif not oneentry in overrideActions and len(overridedata)>0:
                    if overridedata[-1]=="_override_by_key_" :
                        overridedata[-1] = "_override_by_key_"+oneentry+"_"
            for oneentry in overrideContents :
                if oneentry in overrideActions :
                    overridedata.append(overrideActions[oneentry])
        else : # Element that is represented as dict entry
            for oneentry in overrideContents :
                if oneentry == "replace" :
                    outdict["_override_replace_"] = True
                elif oneentry == "lock" :
                    outdict["_override_lock_"] = True
                elif oneentry == "remove" :
                    if nodemode<0 :
                        return { mytag : "_override_remove_" }
                    return ( mytag, nodemode, "_override_remove_")
            lll = len(outdict)
        if nnn+lll+ttt==0 :
            if nodemode<0 :
                return { mytag : None }
            return ( mytag, nodemode, None)

        if nnn+ttt==0 :
            if nodemode<0 :
                return { mytag : outdict}
            if nodemode!= 1 :
                return (mytag, nodemode, outdict)
            if len(xTag)==0 :
                xTag = mytag
            mytag = "___"+mytag+"___"
            overridedata.append( { xTag : outdict } )
            return ( mytag, nodemode, overridedata )
        if nnn==0 :
            outdict["___"] = textrep
            if nodemode<0 :
                return { mytag : outdict}
            if nodemode!= 1 :
                return (mytag, nodemode, outdict)
            if len(xTag)==0 :
                xTag = mytag
            mytag = "___"+mytag+"___"
            overridedata.append( { xTag : outdict } )
            return ( mytag, nodemode, overridedata )

        listcheck = {}
        for child in elem:
            ctag = child.tag
            if ctag in listMember or elem.tag+"___"+child.tag in listMember:
                listcheck[ctag] = 1
                continue
            val = listcheck.get(ctag)
            if val==None :
                listcheck[ctag] = 0
            elif val==0 :
                listcheck[ctag] = 1

        if nnn>1 :
            ooo = self.orderStart
        else :
            ooo = -1
        repdict = collections.OrderedDict({})
        for child in elem:
            (ctag, cmode, cdata) = \
                self.parseOne(child, elem.tag, listcheck[child.tag], ooo)
            ooo = ooo+1
            if cmode == 0 :
                repdict[ ctag ] = cdata
                continue
            if cmode == 1 :
                if not ctag in outdict :
                    outdict[ctag] = cdata
                elif isinstance(outdict[ctag], list) :
                    outdict[ctag].extend( cdata )
                if not ctag in listMember :
                    listMember.add(elem.tag+"___"+child.tag)
                continue
            (impl, deep) = self.implOne(cdata)
            if not ctag in repdict :
                repdict[ ctag ] = cdata
                continue
            (impl0, deep0) = self.implOne(repdict[ ctag ])
            if deep0 :
                repdict[ ctag ]["___"].extend(impl)
            elif not deep :
                repdict[ ctag ].extend(impl)
            else :
                impl0.extend(impl)
                cdata["___"] = impl0
                repdict[ ctag ] = cdata

        if len(repdict) > 0 :
            outdict["___"] = repdict
        if nodemode<0 :
            return {mytag : outdict}
        if nodemode!= 1 :
            return (mytag, nodemode, outdict)
        if len(xTag)==0 :
            xTag = mytag
        mytag = "___"+mytag+"___"
        overridedata.append( { xTag : outdict } )
        return ( mytag, nodemode, overridedata )

    # input is either the path to an XML file or a string containing XML data.
    # returns a data structure that represents the contents of the XML and
    # is serializable into JSON.
    def convert(self, input) :

        oneroot = None
        if isinstance(input, str) :
            try :
                tree = ET.parse(input)
                oneroot = tree.getroot()
            except :
                try:
                    oneroot = ET.fromstring(input)
                except:
                    return None
        else :
            return None

        self.orderStart = self.nextStart
        self.nextStart = 10000+self.orderStart
        return self.parseOne(oneroot, "", -1, self.orderStart)

    # Add text to output buffer.
    def bText(self, str) :
        self.outputBuffer = self.outputBuffer+str
        self.lineLength = self.lineLength + len(str)

    # Add text plus endline to output buffer.
    def bLine(self, str) :
        self.outputBuffer = self.outputBuffer+str+"\n"
        self.lineLength = 0

    # Add text plus tag close and endline to output buffer.
    def bEnd(self, str) :
        self.outputBuffer = self.outputBuffer+str+">\n"
        self.lineLength = 0

    # Takes data that is meant to be pure text in xml and replaces certain
    # characters with metasequences.
    def prepText(self, onestr) :
        trns = [ ( '&', '&amp;' ), 
                 ( '"', '&quot;' ),
                 ( '<', '&lt;' ),
                 ( '>', '&gt;' ) ]
        onestr = str(onestr)
        for onetrn in trns :
            i = 0
            while True :
                j = onestr.find(onetrn[0], i)
                if j<0 :
                    break
                onestr = onestr[:j]+onetrn[1]+onestr[j+1:]
                i = j+1
        return onestr

    def outputList(self, onelist, gparent, parent, tag, indstr) :
        global listParser

        autoList = False
        if parent[:3]=="___" :
            parent = parent[3:len(parent)-3]
            autoList = True
        oneParser = None
        if oneParser == None :
            oneParser = listParser.get(gparent+"___"+parent)
        if oneParser == None :
            oneParser = listParser.get(parent)
        keyIdx = -1
        if oneParser != None :
            keyIdx = oneParser[2]

        categorize = 0
        for oneItem in onelist :
            if isinstance(oneItem, dict) :
                if len(oneItem)==1 and keyIdx>=0 :
                    categorize = categorize | 1
                else :
                    categorize = categorize | 2
            elif isinstance(oneItem, list) :
                if oneParser!=None :
                    categorize = categorize | 4
                else :
                    categorize = categorize | 8
            else :
                if isinstance(oneItem, str) :
                    if oneItem[:10] == "_override_" :
                        continue
                if oneParser!=None :
                    categorize = categorize | 16
                else :
                    categorize = categorize | 32

        #plain text as list of maps.
        textRep = ""
        if categorize==1 :
            categorize = 0
            outerDel = oneParser[0]
            for oneItem in onelist :
                if not isinstance(oneItem, dict) :
                    continue
                data = oneItem.values()[0]
                if isinstance(data, list) :
                    subItems = data[:keyIdx]
                    subItems.append(oneItem.keys()[0])
                    subItems.extend(data[keyIdx:])
                elif data==None :
                    subItems = [oneItem.keys()[0]]
                elif keyIdx==0 :
                    subItems = [oneItem.keys()[0], data]
                else :
                    subItems = [data, oneItem.keys()[0]]
                if len(textRep)>0 :
                    textRep = textRep+outerDel
                innerDel = ""
                for onesi in subItems :
                    textRep = textRep+innerDel+self.prepText(onesi)
                    innerDel = oneParser[1]
        
        #plain text as list of lists.
        if categorize==4 :
            categorize = 0
            outerDel = oneParser[0]
            for subItems in onelist :
                if not isinstance(subItems, list) :
                    continue
                if len(textRep)>0 :
                    textRep = textRep+outerDel
                innerDel = ""
                for onesi in subItems :
                    textRep = textRep+innerDel+self.prepText(onesi)
                    innerDel = oneParser[1]
        
        #plain text as simple list.
        if categorize==16 :
            categorize = 0
            outerDel = oneParser[0]
            for oneItem in onelist :
                if isinstance(oneItem, str) :
                    if oneItem[:10] == "_override_" :
                        continue
                if len(textRep)>0 :
                    textRep = textRep+outerDel
                textRep = textRep+self.prepText(oneItem)

        #output plain text representation if created.
        if len(textRep)>0 :
            if len(tag)>0 :
                self.bEnd(indstr+"<"+tag+">"+textRep+"</"+tag)
            else :
                self.bText(textRep)
            return

        if categorize==0 :
            if len(tag)>0 :
                self.bEnd(indstr+"<"+tag+"/")
            return

        for oneItem in onelist :
            if isinstance(oneItem, str) :
                if oneItem[:10] == "_override_" :
                    continue
            if isinstance(oneItem, list) :
                self.outputList(oneItem, gparent, parent, parent, indstr)
            elif isinstance(oneItem, dict) :
                if len(oneItem)!=1 :
                    self.outputNode(oneItem, gparent, parent, indstr)
                    continue
                innerRep = oneItem.values()[0]
                if not isinstance(innerRep, dict) :
                    self.outputNode(oneItem, gparent, parent, indstr)
                    continue
                if "___" in innerRep or autoList :
                    self.outputNode(innerRep, gparent, parent, indstr)
                else :
                    self.outputNode(oneItem, gparent, parent, indstr)
            else :
                textRep = self.prepText(oneItem)
                self.bEnd(indstr+"<"+tag+">"+textRep+"</"+tag)

        return

    # Takes a data structure produced by the convert() method and reformats the
    # corresponding XML.  Not meant to be called by outside clients.
    def outputNode(self, onenode, gparent="", parent="", indstr="") :
        if not isinstance(onenode, dict) :
            return
        if len(parent)==0:
            self.bEnd('<?xml version="1.0" encoding="UTF-8" standalone="yes"?')
        else :
            indstr = indstr+'   '
            i = parent.find("___")
            if i>2:
                parent = parent[:i]
            self.bText(indstr+"<"+parent)
        nind = len(indstr)

        #classify the various member keys
        listkeys = []
        contkeys = []
        topkeys = []
        textRep = ""
        contRep = None
        for onekey in onenode:
            if onekey[:10] == "_override_" or onekey=="__order__" :
                continue
            keyval = onenode[onekey]
            if isinstance(keyval, str) :
                if keyval[:10] == "_override_" :
                    continue
            if onekey=="___" :
                if isinstance(keyval, dict) :
                    contRep = keyval
                    contkeys.extend(keyval.keys())
                elif isinstance(keyval, list) :
                    listkeys.append(onekey)
                else :
                    textRep = self.prepText(keyval)
                continue
            if onekey[:3]=="___" and isinstance(keyval, list) :
                listkeys.append(onekey)
                continue
            if isinstance(keyval, dict) or isinstance(keyval, list) :
                topkeys.append(onekey)
                continue
            if keyval=="deleteAttribute" or len(parent)==0 :
                continue
            attText = onekey+'="'+self.prepText(keyval)+'"'
            if self.lineLength>nind and \
               len(attText)+self.lineLength+nind>80 :
                self.bLine('')
                self.bText(indstr+"    "+attText)
            else :
                self.bText(' '+attText)

        #case of no content with structure
        nn = len(listkeys)+len(contkeys)+len(topkeys)
        if nn==0 :
            if len(textRep)==0 :
                if len(parent)>0 :
                    self.bEnd('/')
            elif len(parent)==0 :            
                self.bText(textRep)
            else :
                self.bEnd('>'+textRep+"</"+parent)
            return

        # we expect a list parsed from text
        if nn==1 and len(listkeys)==1 and "___" in listkeys :
            nn = -1

        if len(parent)>0 :
            if nn>=0 :
                self.bEnd("")
            else :
                self.bText(">")

        #case of mixed simplified content
        for onekey in topkeys :
            nodeval = onenode[onekey]
            if isinstance(nodeval, dict) :
                self.outputNode(nodeval, parent, onekey, indstr)
            else :
                self.outputList(nodeval, parent, onekey, onekey, indstr)

        #case of mapped content
        mm = len(contkeys)-1
        i = 0
        while i<mm :
            j = i+1
            while j<=mm :
                ii = contRep[contkeys[i]].get("__order__",0)
                jj = contRep[contkeys[j]].get("__order__",mm)
                if ii > jj :
                    cpy = contkeys[i]
                    contkeys[i] = contkeys[j]
                    contkeys[j] = cpy
                j = j+1
            i = i+1
        for onekey in contkeys :
            self.outputNode(contRep[onekey], parent, onekey, indstr)

        #case of list content
        for onekey in listkeys :
            if onekey=="___" :
                self.outputList(onenode[onekey], gparent, parent, "", indstr)
            else :
                self.outputList(onenode[onekey], parent, onekey, onekey, indstr)

        if len(parent)==0 :
            return
        if nn>=0 :
            self.bText(indstr)
        self.bEnd("</"+parent)
        return

    # Meant to be called by outside clients to take data structures from
    # the convert method and output them as xml.
    # if dest>0 will output to stdout.
    def unconvert(self, fromConvert) :
        self.outputBuffer = ""
        self.lineLength = 0
        self.outputNode(fromConvert)
        return self.outputBuffer

    def revertList(self, onelist, elem, gparent, parent, tag) :
        global listParser

        autoList = False
        if parent[:3]=="___" :
            parent = parent[3:len(parent)-3]
            autoList = True
        oneParser = None
        if oneParser == None :
            oneParser = listParser.get(gparent+"___"+parent)
        if oneParser == None :
            oneParser = listParser.get(parent)
        keyIdx = -1
        if oneParser != None :
            keyIdx = oneParser[2]

        categorize = 0
        for oneItem in onelist :
            if isinstance(oneItem, dict) :
                if len(oneItem)==1 and keyIdx>=0 :
                    categorize = categorize | 1
                else :
                    categorize = categorize | 2
            elif isinstance(oneItem, list) :
                if oneParser!=None :
                    categorize = categorize | 4
                else :
                    categorize = categorize | 8
            else :
                if isinstance(oneItem, str) :
                    if oneItem[:10] == "_override_" :
                        continue
                if oneParser!=None :
                    categorize = categorize | 16
                else :
                    categorize = categorize | 32

        #plain text as list of maps.
        textRep = ""
        if categorize==1 :
            categorize = 0
            outerDel = oneParser[0]
            for oneItem in onelist :
                if not isinstance(oneItem, dict) :
                    continue
                data = oneItem.values()[0]
                if isinstance(data, list) :
                    subItems = data[:keyIdx]
                    subItems.append(oneItem.keys()[0])
                    subItems.extend(data[keyIdx:])
                elif data==None :
                    subItems = [oneItem.keys()[0]]
                elif keyIdx==0 :
                    subItems = [oneItem.keys()[0], data]
                else :
                    subItems = [data, oneItem.keys()[0]]
                if len(textRep)>0 :
                    textRep = textRep+outerDel
                innerDel = ""
                for onesi in subItems :
                    textRep = textRep+innerDel+self.prepText(onesi)
                    innerDel = oneParser[1]
        
        #plain text as list of lists.
        if categorize==4 :
            categorize = 0
            outerDel = oneParser[0]
            for subItems in onelist :
                if not isinstance(subItems, list) :
                    continue
                if len(textRep)>0 :
                    textRep = textRep+outerDel
                innerDel = ""
                for onesi in subItems :
                    textRep = textRep+innerDel+self.prepText(onesi)
                    innerDel = oneParser[1]
        
        #plain text as simple list.
        if categorize==16 :
            categorize = 0
            outerDel = oneParser[0]
            for oneItem in onelist :
                if isinstance(oneItem, str) :
                    if oneItem[:10] == "_override_" :
                        continue
                if len(textRep)>0 :
                    textRep = textRep+outerDel
                textRep = textRep+self.prepText(oneItem)

        #output plain text representation if created.
        if len(textRep)>0 :
            if len(tag)>0 :
                esub = ET.SubElement(elem, tag)
                esub.text = textRep
            else :
                elem.text = textRep
            return

        if categorize==0 :
            if len(tag)>0 :
                ET.SubElement(elem, tag)
            return

        for oneItem in onelist :
            if isinstance(oneItem, str) :
                if oneItem[:10] == "_override_" :
                    continue
            if isinstance(oneItem, list) :
                self.revertList(oneItem, elem, gparent, parent, parent)
            elif isinstance(oneItem, dict) :
                if len(oneItem)!=1 :
                    self.revertNode(oneItem, ET.SubElement(elem, parent), \
                                    gparent, parent)
                    continue
                innerRep = oneItem.values()[0]
                if not isinstance(innerRep, dict) :
                    self.revertNode(oneItem, ET.SubElement(elem, parent), \
                                    gparent, parent)
                    continue
                if "___" in innerRep or autoList :
                    self.revertNode(innerRep, ET.SubElement(elem, parent), \
                                    gparent, parent)
                else :
                    self.revertNode(oneItem, ET.SubElement(elem, parent), \
                                    gparent, parent)
            else :
                esub = ET.SubElement(elem, parent)
                esub.text = textRep

        return

    def revertNode(self, onenode, elem=None, gparent="", parent="") :
        if not isinstance(onenode, dict) :
            return
        if elem==None :
            if len(onenode)!=1 :
                return None
            parentnow = onenode.keys()[0]
            nodenow = onenode.values()[0]
            elemnow = ET.Element(parentnow)
            return ET.ElementTree( \
              self.revertNode(onenode.values()[0], elemnow, "", parentnow) )
        i = parent.find("___")
        if i>2:
            parent = parent[:i]

        #classify the various member keys
        listkeys = []
        contkeys = []
        topkeys = []
        textRep = ""
        contRep = None
        for onekey in onenode:
            if onekey[:10] == "_override_" or onekey=="__order__" :
                continue
            keyval = onenode[onekey]
            if isinstance(keyval, str) :
                if keyval[:10] == "_override_" :
                    continue
            if onekey=="___" :
                if isinstance(keyval, dict) :
                    contRep = keyval
                    contkeys.extend(keyval.keys())
                elif isinstance(keyval, list) :
                    listkeys.append(onekey)
                else :
                    textRep = self.prepText(keyval)
                continue
            if onekey[:3]=="___" and isinstance(keyval, list) :
                listkeys.append(onekey)
                continue
            if isinstance(keyval, dict) or isinstance(keyval, list) :
                topkeys.append(onekey)
                continue
            if keyval=="deleteAttribute" or len(parent)==0 :
                continue
            elem.attrib[onekey] = keyval

        #case of no content with structure
        nn = len(listkeys)+len(contkeys)+len(topkeys)
        if nn==0 :
            if len(textRep)==0 :
                return elem
            elem.text = textRep
            return elem

        # we expect a list parsed from text
        if nn==1 and len(listkeys)==1 and "___" in listkeys :
            nn = -1

        #case of mixed simplified content
        for onekey in topkeys :
            nodeval = onenode[onekey]
            if isinstance(nodeval, dict) :
                self.revertNode(nodeval, ET.SubElement(elem, onekey), \
                                parent, onekey)
            else :
                self.revertList(nodeval, elem, parent, onekey, onekey)

        #case of mapped content
        mm = len(contkeys)-1
        i = 0
        while i<mm :
            j = i+1
            while j<=mm :
                ii = contRep[contkeys[i]].get("__order__",0)
                jj = contRep[contkeys[j]].get("__order__",mm)
                if ii > jj :
                    cpy = contkeys[i]
                    contkeys[i] = contkeys[j]
                    contkeys[j] = cpy
                j = j+1
            i = i+1
        for onekey in contkeys :
            self.revertNode(contRep[onekey], ET.SubElement(elem,onekey), \
                            parent, onekey)

        #case of list content
        for onekey in listkeys :
            if onekey=="___" :
                self.revertList(onenode[onekey], elem, gparent, parent, "")
            else :
                self.revertList(onenode[onekey], parent, onekey, onekey)

        return elem

    # This method takes output from convert() method and removes some of the
    # items only meant to direct the recreation of XML, making it more
    # easily useable as configuration data.  Outside clients should not
    # supply the ctag argument.
    def simplify(self, onenode, ctag="") :
        global stripKeys
        if isinstance(onenode, list) :
            i = 0
            nn = len(onenode)
            while i<nn :
                if not isinstance(onenode[i], str) :
                    onenode[i] = self.simplify(onenode[i], ctag)
                elif onenode[i][:10] == "_override_" :
                    del onenode[i]
                    continue
                i = i+1
            return onenode
        if not isinstance(onenode, dict) :
            return onenode
        if "__order__" in onenode :
            del onenode["__order__"]
        if len(onenode)==1 and len(ctag)>0:
            nodeval = onenode.values()[0]
            if not isinstance(nodeval, dict) or ctag in onenode :
                return self.simplify(nodeval, "")
            nodetag = onenode.keys()[0]
            for onekey in nodeval.keys() :
                if nodeval[onekey]==nodetag :
                    del nodeval[onekey]
                    break
            return self.simplify(onenode, "")
        anyatt = False
        content = False
        for onekey in onenode.keys() :
            if onekey in stripKeys :
                del onenode[onekey]
                continue
            if onekey[:10] == "_override_" :
                del onenode[onekey]
                continue
            if isinstance(onenode[onekey], str) :
                if onenode[onekey][:10] == "_override_" :
                    del onenode[onekey]
                    continue
            if onekey == "___" :
                content = True
                continue
            if isinstance(onenode[onekey], dict) :
                onenode[onekey] = self.simplify(onenode[onekey], "")
                continue
            nc = len(onekey)
            if nc < 8 or onekey[:3] != "___"  or onekey[nc-3:] != "___" :
                anyatt = True
                continue
            ctag = onekey[3:nc-3]
            if not isinstance(onenode[onekey], list) :
                anyatt = True
                continue
            if not ctag in onenode :
                onenode[ctag] = self.simplify(onenode[onekey], ctag)
                del onenode[onekey]
            else :
                onenode[onekey] = self.simplify(onenode[onekey], ctag)
            
        if content :
            if isinstance(onenode["___"], dict) :
                for conkey in onenode["___"].keys() :
                    if isinstance(onenode["___"][conkey], str) :
                        if onenode["___"][conkey][:10] == "_override_" :
                            del onenode["___"][conkey]
                            continue
                    if not conkey in onenode:
                        onenode[conkey] = \
                           self.simplify(onenode["___"][conkey],"")
                        del onenode["___"][conkey]
                    else :
                        onenode["___"][conkey] = \
                           self.simplify(onenode["___"][conkey],"")
            if len(onenode["___"])==0 :
                del onenode["___"]
            elif len(onenode)==1 :
                onenode = onenode["___"]

        return onenode
