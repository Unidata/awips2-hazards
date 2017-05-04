import json
import subprocess
import sys
import os
import traceback
import collections
from UFStatusLogger import *

#
# The purpose of this class is to take multiple structures that are
# either JSON strings or serializable into JSON, and combine them
# into a single data structure.  The assumption is that the first
# structure represents the base version of some configuration
# data, and the subsequent data structures are overrides for that 
# configuration data.  Where two items reside in the same name space
# in both the base and override structures, the override value will usually
# displace the base value, though pairs of lists and pairs of dictionaries that
# appear at the same namespace will be merged.  Items that have nothing in
# the corresponding name space in the other structure will be usually be
# added to the final result regardless of which structure they are in.
# In the case where there are lists at the same name space in each
# structure, individual list members that are completely identical are
# considered to be in the same name space.  Furthermore, list members that
# are dict objects can be considered to be in the same name space if their
# keys are identical, or optionally if a user specified key has the same
# value.
#
# There are several ways to change the default manner in which the two
# data structures are combined.  Anywhere one encounters a string that
# begins with "_override_", whether that is a key, a value, or a list
# member, this is treated as something designed to be used to affect the
# way the two structures are combined.  When these strings appear in the
# base structure, they are ignored but stripped; in the override structure
# they are interpreted to affect the combination algorithm and then also
# stripped. There are some exceptions to this general behavior for override
# control strings which will be discussed later.
#
# There are only four control strings used within dictionary objects;
# these are _override_replace_, _override_remove_, _override_lock_, and
# _override_multiple_.  _override_lock_ is exceptional in that is
# interpreted in the base and not stripped until the client requests the
# final data structure with the combine() method.  The value of the
# _override_lock_ key can be True, which means the entire base
# dictionary object is prevented from being affected in any way by an
# override object, or it can be a list of specific keys to "lock".  If a
# specific list of keys is provided, the object can be deleted from the
# parent unless one of the entries in the list is True.  An empty list
# acts like a wildcard, locking all keys but still allowing the whole
# object to be deleted from the parent.  An _override_lock_ entry in the
# override will not affect the combination but can be added to the
# combined object to affect later combination operations. An entry with
# a key of _override_replace_ and a value of True will cause all entries
# in the base dictionary object at the same name space to be cleared out
# (if not locked) before adding the entries from the override structure.
# An entry with a value of _override_remove_ will cause the entry with
# that same key at that namespace to be removed, unless that entry is
# locked.  A entry with a key of _override_multiple_ and a value that is
# either a list or dict can potentially be combined with all entries in
# the base that are the same type.
#
# For lists there are many more control strings implemented.  Control strings
# can be interspersed with other kinds of serializable objects.  There are
# three strings that must be immediately at the front of the list to be
# interpreted, _override_replace_, _override_lock_ and _override_lock_parent_.
# _override_lock_ and _override_lock_parent_ are exclusionary, so at most
# two of these can be present, and in this case either can be first or second.
# As with dictionaries, _override_lock_ (and _override_lock_parent_) are
# interpreted in the base and not stripped until the client requests the final
# data structure with the combine() method. If in the base, _override_lock_
# will prevent any part of the base list object from being affected in any way
# by an override list, and will shield the list from being removed from the
# parent. _override_lock_parent_ only shields the list from being removed from
# the parent. If _override_lock_ or_override_lock_parent_ are in the override,
# they will not affect the combination but can be added to the combined object
# to affect later combination operations. If _override_replace_ is present, it
# causes the base list at the same name space to be emptied out before members
# are added from the override structure, much as with dictionaries.
#
# There are several control strings that do not refer to specific members,
# but rather control general aspects of combining lists, and these all
# act as binary switches.  _override_prepend_ causes new list members from
# the override structure to be added to the front of the combined list,
# whereas _override_append_ invokes the default behavior of adding them to
# the end.  _override_additive_ causes all members from an override list
# at the same name space to be added to the combined list regardless, whereas
# _override_unique_ restores the default behavior whereby members get added
# to the combined list only if they are not at the same name space as any
# members of the base list. _override_by_keys_ causes dictionary list members
# to be considered at identical namespace when they have the same keys,
# whereas _override_by_content_ restores the default behavior whereby list
# members must be completely the same to be at the same name space.
# Alternatively, one can optionally use _override_by_key_MYNAMESPACEKEY_, 
# where MYNAMESPACEKEY is not literal but refers to a specific key within
# dictionary list members, where the value of that key determines the
# effective namespace used.
#
# The rest of the available list control strings treat one or more of the
# immediately following override list members as arguments. With the exception
# of _override_lock_one_ ("locks" the next item), they all follow the typical
# paradigm of only being interpretted in the override and then are immediately
# stripped.  Except for _override_lock_one_, _override_insert_before_,
# and _override_insert_after_, they are all used to remove one or more items 
# from the base list at the same name space.
#
# When the argument members cannot be matched up to a member of the base
# list, the operation is ignored and the argument members are also ignored.
# _override_remove_ causes the one item that follows to be removed, whereas
# _override_remove_list_ causes all items that follow up until the next
# control string to be removed; _override_null_ is provided to allow
# this to be terminated and do nothing else. _override_remove_range_ and
# _override_remove_between_ take two arguments, removing everything in
# that range of members, inclusively and exclusively, respectively.
# _override_remove_before_ and _override_remove_after_ take one argument,
# causing everything either before or after the indicated item to be
# removed, exclusively. _override_insert_before_ and _override_insert_after_
# do not remove items, but rather identify the place in the combined list
# where the next insertion action will take place.
#

listArgCount = { "_override_remove_" : 1,
                 "_override_remove_list_" : 999999,
                 "_override_remove_before_" : 1,
                 "_override_remove_after_" : 1,
                 "_override_remove_range_": 2,
                 "_override_remove_between_": 2,
                 "_override_insert_before_": 1,
                 "_override_insert_after_": 1}

class jsonCombine :

    # ctor takes up to two objects that are either JSON strings or serializable
    # into JSON
    def __init__(self, b=None, o=None) :
        self.logger = UFStatusLogger.getInstance()
        self.final = False
        self.base = None
        self.btype = 0
        self.override = None
        self.extraDiag = False
        self.excludeNonJson = True

        if b!=None :
            if isinstance(b, str) or isinstance(b, unicode) :
                try :
                    self.base = json.loads(b, \
                                object_pairs_hook=collections.OrderedDict)
                except :
                    self.base = None
            else :
                self.base = b
            if isinstance(self.base, dict) :
                self.btype = 4
            elif isinstance(self.base, list) or isinstance(self.base, tuple) :
                self.btype = 3
            else :
                self.base = None
                msg = "base object not a dict or list in jsonCombine()"
                self.logger.logMessage(msg, "Error")

        if o==None :
            return

        if isinstance(o, str) or isinstance(o, unicode) :
            try :
                self.override = json.loads(o, \
                                object_pairs_hook=collections.OrderedDict)
            except :
                self.override = None
        else :
            self.override = o

        if isinstance(self.override, dict) :
            otype = 4
        elif isinstance(self.override, list) or \
             isinstance(self.override, tuple) :
            otype = 3
        else :
            self.override = None
            msg = "override object not a dict or list in jsonCombine()"
            self.logger.logMessage(msg, "Error")
            tbData = traceback.format_stack()
            self.logger.logMessage(str(tbData), "Error", "jsonCombine()")
            return

        if self.btype+otype==7 :
            self.override = None
            msg = "override object not compatible with base in jsonCombine()"
            self.logger.logMessage(msg, "Error")
            return

        if self.base==None :
            self.base = self.override
            self.btype = otype
            self.override = None

    # Likely temporary, activate extra diagnostics.
    def setExtraDiag(self, yn) :
        self.extraDiag = yn

    # This is where clients add more data structures to the set of structures
    # to combine.  Returns boolean for success or failure.
    def accumulate(self, a) :
        if self.override==None :
            pass
        elif self.btype == 4 :
            self.base = self.dictionaryCombine(self.base, self.override)
        else :
            self.base = self.listCombine(self.base, self.override)

        if isinstance(a, str) or isinstance(a, unicode) :
            try :
                self.override = json.loads(a, \
                                object_pairs_hook=collections.OrderedDict)
            except :
                self.override = None
        else :
            self.override = a

        if isinstance(self.override, dict) :
            otype = 4
        elif isinstance(self.override, list) or \
             isinstance(self.override, tuple) :
            otype = 3
        else :
            self.override = None
            msg = "override object not a dict or list in " + \
                  "jsonCombine::accumulate()"
            self.logger.logMessage(msg, "Error")
            tbData = traceback.format_stack()
            self.logger.logMessage(str(tbData), "Error", \
                                   "jsonCombine::accumulate()")
            return False

        if self.btype+otype==7 :
            self.override = None
            msg = "override object not compatible with base " + \
                  "jsonCombine::accumulate()"
            self.logger.logMessage(msg, "Error")
            return False

        if self.base==None :
            self.base = self.override
            self.btype = otype
            self.override = None

        return True

    # This method is used to classify objects found in the input data
    # structures: the interpretation of the return value is as follows:
    # -3=non-serializable, -2=control string, -1=lock string,
    #  0=null, 1=simple, 2=string, 3=list, 4=dict
    def classify(self, one) :
        
        try :
            if isinstance(one, str) or isinstance(one, unicode):
                if one[:10]=="_override_" :
                    if one[:15]=="_override_lock_" :
                        return -1
                    return -2
                return 2
            if one==None :
                return 0
            if isinstance(one, dict) :
                return 4
            if isinstance(one, tuple) or isinstance(one, list) :
                return 3
            if isinstance(one, int) or isinstance(one, bool) or \
                 isinstance(one, long) or isinstance(one, float) :
                return 1
        except :
            pass

        return -3

    # Compare list dict objects for equality, ignoring control strings.    
    def listCompare(self, one, two) :
        n = len(one)
        nn = len(two)
        j = 0
        jj = 0
        m = 0
        mm = 0
        while j<n or jj<nn :
            if (mm<=0 or jj>=nn) and j<n :
                v = one[j]
                i = self.classify(v)
                j += 1
                if i==-3 :
                    continue
                if i==-1 or i==-2 :
                    m = listArgCount.get(v, 0)
                    continue
                elif m>0 :
                    m -= 1
                    continue
            if (m<=0 or j>=n) and jj<nn :
                vv = two[jj]
                ii = self.classify(vv)
                jj += 1
                if i==-3 :
                    continue
                if ii==-1 or ii==-2 :
                    mm = listArgCount.get(vv, 0)
                    continue
                elif mm>0 :
                    mm -= 1
                    continue
            if m>0 :
                return mm>0
            if mm>0 :
                return False
            if not self.compare(v, vv) :
                return False
        return (m>0)==(mm>0)

    # Compare two dict objects for equality, ignoring control strings.    
    def dictCompare(self, one, two) :
        keys2 = set( [ ] )
        for kk in two.keys() :
            if self.classify(kk)==2 and self.classify(two[kk])!=-2 :
                if "kk"!="__order__" :
                    keys2.add(kk)
        for kk in one.keys() :
            if self.classify(kk)!=2 or self.classify(one[kk])==-2 :
                continue
            if "kk"=="__order__" :
                continue
            if not kk in keys2 :
                return False
            if not self.compare(one[kk], two[kk]) :
                return False
            keys2.remove(kk)
        return len(keys2)==0

    # Compare two dict objects for identical keys, ignoring control strings.    
    def keysCompare(self, one, two) :
        keys2 = set( [ ] )
        for kk in two.keys() :
            if self.classify(kk)==2 and self.classify(two[kk])!=-2 :
                if "kk"!="__order__" :
                    keys2.add(kk)
        for kk in one.keys() :
            if self.classify(kk)!=2 or self.classify(one[kk])==-2 :
                continue
            if "kk"=="__order__" :
                continue
            if not kk in keys2 :
                return False
            keys2.remove(kk)
        return len(keys2)==0

    # Compare two objects for equality, ignoring control strings.    
    def compare(self, one, two) :
        if one==two :
            return True
        i = self.classify(one)
        ii = self.classify(two)
        if i==-3 :
            return ii==-3
        if ii==-3 :
            return False
        if i<0 and ii<0 :
            return True
        if i!=ii or i<3 :
            return False
        if i==3 :
            return self.listCompare(one, two)
        return self.dictCompare(one, two)

    # Make a copy of a dictionary object, stripping control strings and
    # unserializable objects.
    def dictionaryCopy(self, d) :
        cdict = collections.OrderedDict({ })
        for kk in d.keys() :
            ii = self.classify(kk)
            if ii==-1 :
                if self.final :
                    continue
            elif ii!=2 :
                continue
            vv = d[kk]
            ii = self.classify(vv)
            if ii==-3 :
                if self.excludeNonJson :
                    continue
                try :
                    vv = str(vv)
                    ii = 2
                except :
                    continue
            if ii<0 :
                continue
            if self.extraDiag :
                sys.stderr.write("-> '"+kk+"' "+str(ii)+"\n")
            if ii<3 :
                cdict[kk] = vv
            elif ii==3 :
                cdict[kk] = self.listCopy(vv)
            else :
                cdict[kk] = self.dictionaryCopy(vv)
        return cdict

    # Make a copy of a list object, stripping control strings and
    # unserializable objects.
    def listCopy(self, l) :
        global listArgCount
        clist = [ ]
        m = 0
        for vv in l :
            ii = self.classify(vv)
            if ii==-3 :
                if self.excludeNonJson :
                    continue
                try :
                    vv = str(vv)
                    ii = 2
                except :
                    continue
            if ii==-1 :
                if self.final :
                    continue
            elif ii==-2 :
                m = listArgCount.get(vv, 0)
                continue
            if m>0 :
                m -= 1
                continue
            if ii<3 :
                clist.append(vv)
                if self.extraDiag :
                    sys.stderr.write("=> '"+str(vv)+"' "+str(ii)+"\n")
            elif ii==3 :
                clist.append(self.listCopy(vv))
            else :
                clist.append(self.dictionaryCopy(vv))
        return clist

    # Make a clean copy of an arbitrary object.
    def arbCopy(self, a) :
        ii = self.classify(a)
        if ii==3 :
            return self.listCopy(a)
        elif ii==4 :
            return self.dictionaryCopy(a)
        if ii!=-3 or self.excludeNonJson :
            return a
        try :
            aa = str(a)
            return aa
        except :
            pass
        return a

    # Make a clean copy of an arbitrary object, encoding any non json
    # serializable objects as strings where possible.
    def arbCopyRobust(self, a) :
        self.excludeNonJson = False
        aa = self.arbCopy(a)
        self.excludeNonJson = True
        return aa

    # Make a copy of a list object, copying only locked items
    # Perhaps this copy mechanism needs to be passed down recursively.
    def listCopyLocked(self, l) :
        clist = [ ]
        prevLock = False
        for vv in l :
            ii = self.classify(vv)
            if ii==-1 :
                prevLock = True
                if not self.final :
                    clist.append(vv)
                continue
            elif ii<0 :
                prevLock = False
                continue
            if prevLock :
                prevLock = False
            else :
                continue
            if ii<3 :
                clist.append(vv)
            elif ii==3 :
                clist.append(self.listCopy(vv))
            else :
                clist.append(self.dictionaryCopy(vv))
        return clist

    # Check if an object is internally sheilded from being removed from parent.
    def rigid(self, one) :
        if isinstance(one, dict) :
            lockEntry = one.get("_override_lock_")
            if isinstance(lockEntry, bool) :
                return lockEntry
            if isinstance(lockEntry, list) :
                return True in lockEntry
        elif isinstance(one, list) or isinstance(one, tuple):
            i = len(one)
            if i==0 :
                return True
            if i>1 and one[0]=="_override_replace_" :
                return one[1]=="_override_lock_" or \
                       one[1]=="_override_lock_parent_"
            return one[0]=="_override_lock_" or \
                   one[0]=="_override_lock_parent_"
        return False

    # Here we check whether the value of a key for two dictionaries is the
    # same, always peering down through any "___" keys, this to support
    # converted xml.
    def valcmp(self, one, two, key) :
        for a in [1, 2, 3] :
            if not isinstance(one, dict) or not isinstance(two, dict) :
                return False
            if key in one and key in two :
                v1 = one[key]
                v2 = two[key]
                if self.compare(v1, v2) :
                    return True
                if not isinstance(v1, dict) or not isinstance(v2, dict) :
                    return False
                if not "___" in v1 or not "___" in v2 :
                    return False
                return self.compare(v1["___"],v2["___"])
            if "___" in one and "___" in two :
                kk = "___"
            elif a==1 and len(one)==1 and one.keys()==two.keys() :
                kk = one.keys()[0]
            else :
                return False
            one = one.get(kk)
            two = two.get(kk)
        return False

    # Here we check whether the set of a keys for two dictionaries is the
    # same, always peering down through any "___" keys, this to support
    # converted xml.
    def keyscmp(self, one, two) :
        if not isinstance(one, dict) or not isinstance(two, dict) :
            return False
        if not self.keysCompare(one, two) :
            return False
        if "___" in one :
            one = one.get("___")
            two = two.get("___")
            if not isinstance(one, dict) or not isinstance(two, dict) :
                return True
            return self.keysCompare(one, two)
        if len(one)!=1 :
            return True
        kk = one.keys()[0]
        one = one.get(kk)
        two = two.get(kk)
        if not isinstance(one, dict) or not isinstance(two, dict) :
            return True
        if "___" in one and "___" in two :
            return self.keyscmp(one, two)
        return True

    # Combine two dictionary objects, interpreting control strings as needed.
    def dictionaryCombine(self, b, o, parentLockList=None) :

        if len(o)==0 :
            return self.dictionaryCopy(b)
        if len(b)==0 :
            return self.dictionaryCopy(o)

        # Process lock info in the base
        lockList = b.get("_override_lock_")
        if lockList==None :
            if parentLockList == None :
                lockList = [ ]
            else :
                lockList = parentLockList
        elif isinstance(lockList, bool) :
            if lockList :
                return self.dictionaryCopy(b)
            lockList = [ ]
        elif isinstance(lockList, list) or isinstance(lockList, tuple):
            if len(lockList)==0 :
                return self.dictionaryCopy(b)
        else :
            lockList = [ lockList ]

        # Handle case of non-incrementally replacing everything.
        vv = o.get("_override_replace_")
        if not isinstance(vv, bool) or not vv :
            cdict = self.dictionaryCopy(b)
        elif len(lockList)==0 :
            cdict = collections.OrderedDict({ })
        else :
            cdict = self.dictionaryCopy(b)
            for kk in cdict.keys() :
                if not kk in lockList :
                    del cdict[kk]

        # Grab value that we can combine with multiple entries.
        mmm =  o.get("_override_multiple_")
        iii = self.classify(mmm)
        if (iii<3) :
            iii = 0

        # Process lock info in the override unless this is directly for client
        if not self.final :
            oLL = o.get("_override_lock_")
            if oLL==None :
                pass
            elif isinstance(oLL, bool) :
                if oLL :
                    cdict["_override_lock_"] = True
            elif len(lockList)==0:
                cdict["_override_lock_"] = oLL
            else :
                if not isinstance(oLL, list) and not isinstance(oLL, tuple):
                    oLL = [ oLL ]
                for one in lockList :
                    if not one in oLL :
                        oLL.append(one)
                cdict["_override_lock_"] = oLL

        # Combine the two dictionaries
        for kk in o.keys() :
            ii = self.classify(kk)
            if ii!=2 :
                continue
            if kk in lockList :
                continue
            vv = o[kk]
            ii = self.classify(vv)
            if ii<0 :
                if ii==-2 and vv=="_override_remove_" and kk in cdict :
                    if not self.rigid(cdict[kk]) :
                        del cdict[kk]
                continue
            if ii<3 :
                cdict[kk] = vv
                continue
            if kk in cdict :
                i = self.classify(cdict[kk])
            else :
                i = -999
            if i!=ii:
                if ii==3 :
                    cdict[kk] = self.listCopy(vv)
                else :
                    cdict[kk] = self.dictionaryCopy(vv)
            elif ii==3 :
                cdict[kk] = self.listCombine(cdict[kk], vv)
            elif kk=="___" :
                # Pass lock list from parent down into combined dicts with
                # "___" key, which supports converted xml.
                cdict[kk] = self.dictionaryCombine(cdict[kk], vv, lockList)
            else :
                cdict[kk] = self.dictionaryCombine(cdict[kk], vv)

        if (iii==0) :
            return cdict

        # Processing for value that we can combine with multiple entries.
        for kk in cdict.keys():
            if kk in lockList :
                continue
            ii = self.classify(cdict[kk])
            if ii!=iii :
                continue
            if ii==3 :
                cdict[kk] = self.listCombine(cdict[kk], mmm)
            else :
                cdict[kk] = self.dictionaryCombine(cdict[kk], mmm)

        return cdict


    # Combine two list objects, interpreting control strings as needed.
    def listCombine(self, b, o) :

        if len(o)==0 :
            return self.listCopy(b)
        if len(b)==0 :
            return self.listCopy(o)
        if b[0]=="_override_lock_" :
            return self.listCopy(b)
        if len(b)>1 :
            if b[0]=="_override_replace_" and b[1]=="_override_lock_" :
                return self.listCopy(b)

        # Process the control strings that must be at front of the override
        # list to make any sense, move loop control variable jj past these
        jj = 0
        mm = len(o)
        lock0 = False
        replace0 = False
        while jj<mm and jj<2 :
            if o[jj]=="_override_replace_" and not replace0 :
                replace0 = True
            elif o[jj]=="_override_lock_" and not lock0 :
                lock0 = True
            elif o[jj]=="_override_lock_parent_" and not lock0 :
                pass
            else :
                break
            jj += 1
                
        # Copy our base list to our working output list
        allLocked = False
        if replace0 :
            clist = self.listCopyLocked(b)
            allLocked = True
        else :
            clist = self.listCopy(b)

        # Set default state of our override control flags
        jp = 0
        remove = "no"
        jr0 = -1
        inclusive = True
        additive = False
        append = True
        dictByKeys = False
        dictKey = None
        prevLock = False
        multiple = False

        # Loop by index so we can skip some if we want
        while jj<mm :

            # Classify next object, just ignore if unserializable
            vv = o[jj]
            jj = jj+1
            ii = self.classify(vv)
            if ii==-3 :
                continue

            # Handle our override control strings.
            if ii==-1 :
                prevLock = not self.final and not lock0 and vv.find("_one")
                remove = "no"
                continue
            elif ii==-2 :
                prevLock = False
                if vv=="_override_multiple_" :
                    multiple = True
                    remove = "no"
                    continue
                multiple = False
                if vv=="_override_remove_" :
                    remove = "yes"
                elif vv=="_override_remove_list_" :
                    remove = "list"
                elif vv=="_override_null_" :
                    remove = "no"
                elif vv=="_override_remove_before_" :
                    remove = "before"
                elif vv=="_override_remove_after_" :
                    remove = "after"
                elif vv=="_override_remove_range_" :
                    remove = "range"
                    inclusive = True
                    jr0 = -1
                elif vv=="_override_remove_between_" :
                    remove = "range"
                    inclusive = False
                    jr0 = -1
                elif vv[:17]=="_override_by_key_" :
                    dictByKeys = True
                    dictKey = vv[17:-1]
                elif vv=="_override_by_keys_" :
                    dictByKeys = True
                    dictKey = None
                elif vv=="_override_by_content_" :
                    dictByKeys = False
                    dictKey = None
                elif vv=="_override_unique_" :
                    additive = False
                    remove = "no"
                elif vv=="_override_additive_" :
                    additive = True
                    remove = "no"
                elif vv=="_override_prepend_" :
                    jp = 0
                    append = False
                    remove = "no"
                elif vv=="_override_append_" :
                    append = True
                    remove = "no"
                elif vv=="_override_insert_before_" :
                    jp = 0
                    append = False
                    remove = "beforeno" # borrow remove to trigger matching
                elif vv=="_override_insert_after_" :
                    jp = len(clist)
                    append = False
                    remove = "afterno" # borrow remove to trigger matching
                else :
                    remove = "no"
                continue

            # keep track of whether current object is individually locked.
            prevLockNow = prevLock
            prevLock = False

            # Case where we combine override with multiple base entries.
            if multiple and ii>=3:
                j = 0
                ee = len(clist)
                while j!=ee :
                    if self.classify(clist[j]) != ii :
                        pass
                    elif ii == 3 :
                        clist[j] = self.listCombine(clist[j], vv)
                    elif not dictByKeys :
                        clist[j] = self.dictionaryCombine(clist[j], vv)
                    elif dictKey!=None :
                        if self.valcmp(vv, clist[j], dictKey) :
                            clist[j] = self.dictionaryCombine(clist[j], vv)
                    elif self.keyscmp(vv, clist[j]) :
                        clist[j] = self.dictionaryCombine(clist[j], vv)
                    j = j + 1
                multiple = False
                continue
            multiple = False

            # Simple cases of empty base list or unconditional add
            if len(clist)==0 :
                if remove=="range" and jr0<0 :
                    jj = jj+1
                if remove=="no" :
                     if prevLockNow :
                         clist.append("_override_lock_one_")
                     clist.append(self.arbCopy(vv))
                     jp = len(clist)
                else :
                     remove = "no"
                continue
            if remove=="no" and additive :
                if append :
                    if prevLockNow :
                        clist.append("_override_lock_one_")
                    clist.append(self.arbCopy(vv))
                else :
                    if prevLockNow :
                        clist.insert(jp, "_override_lock_one_")
                        jp = jp + 1
                    clist.insert(jp, self.arbCopy(vv))
                    jp = jp + 1
                continue

            # Rest of cases where we need to see if our override list entry
            # matches something in the base list.
            if remove[:5]=="after" or remove=="range" and jr0>0 :
                j = len(clist)-1
                d = -1
                ee = -1
            else :
                j = 0
                d = 1
                ee = len(clist)
            while j!=ee :
                if ii<4 or not dictByKeys:
                    if self.compare(vv, clist[j]) :
                        break
                elif isinstance(clist[j], dict) :
                    if dictKey!=None :
                        if self.valcmp(vv, clist[j], dictKey) :
                            ii = 9
                            break
                    elif self.keyscmp(vv, clist[j]) :
                        ii = 9
                        break
                j = j + d

            # Case where we add if no match
            if remove=="no" :
                if j!=ee :
                    if allLocked :
                        continue
                    if j>0 and clist[j-1]=="_override_lock_one_":
                        continue
                    if prevLockNow :
                        clist.insert(j,"_override_lock_one_")
                        j=j+1
                        if jp>=j :
                            jp=jp+1
                    if ii==9 :
                        clist[j] = self.dictionaryCombine(clist[j], vv)
                    continue
                if append :
                    if prevLockNow :
                        clist.append("_override_lock_one_")
                    clist.append(self.arbCopy(vv))
                else :
                    if prevLockNow :
                        clist.insert(jp, "_override_lock_one_")
                        jp = jp + 1
                    clist.insert(jp, self.arbCopy(vv))
                    jp = jp + 1
                continue

            # The remaining remove operations fail if no match, catch this
            if j==ee :
                if remove=="range" and jr0<0 :
                    jj = jj+1
                if remove!="list" :
                    remove = "no"
                    jr0 = -1
                continue

            # Determine range of indices for our remove op.
            if remove=="range" :
                if jr0<0 :
                    jr0 = j
                    continue
                if jr0>j :
                    jjj = jr0
                    jr0 = j
                    j = jjj
                if not inclusive :
                    jr0 = jr0+1
                    j = j-1
            elif remove=="after" :
                jr0 = j+1
                j = len(clist)-1
            elif remove=="before" :
                jr0 = 0
                j = j-1
            elif remove=="afterno" :
                jp = j+1
                jr0 = j+1
            elif remove=="beforeno" :
                jp = j
                jr0 = j+1
            else :
                jr0 = j
            if jr0>j :
                jr0 = -1
                remove = "no"
                continue

            # Delete each index in range if not locked
            if not allLocked :
                while j>=jr0 :
                    if j>0 and clist[j-1]=="_override_lock_one_" :
                        j = j-2
                        continue
                    if self.rigid(clist[j]) :
                        j = j-1
                        continue
                    del clist[j]
                    if jp>j and jp>0 :
                        jp = jp - 1
                    j = j - 1

            # Finish up.
            if remove!="list" :
                jr0 = -1
                remove = "no"

        # Add global list lock to front if needed.
        if lock0 and not self.final:
            clist.insert(0, "_override_lock_")

        return clist

    # Perform the primary function of this class, return a combination of all
    # the intput data structures.
    def combine(self) :
        if self.base==None :
            return None
        if self.override!=None :
            if self.btype == 4 :
                self.base = self.dictionaryCombine(self.base, self.override)
            else :
                self.base = self.listCombine(self.base, self.override)
            self.override = None
        self.final = True
        if self.btype == 4 :
            return self.dictionaryCopy(self.base)
        return self.listCopy(self.base)
