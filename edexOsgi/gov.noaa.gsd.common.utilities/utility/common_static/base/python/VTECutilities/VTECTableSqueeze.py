import os, sys, time, copy
import Logger as LogStream
import VTECTableUtil as VTECTableUtil


# This class takes a VTEC active table and eliminates unnecessary 
# records.  Records purged consist of old SPC watches, old Tropical
# events, last year's records, and extraneous records not needed for
# new event ETN determination.

#NOTE: all times are expected to be in milliseconds since epoch, this
#includes times in the records, as well as references to current time.

class VTECTableSqueeze(VTECTableUtil.VTECTableUtil):

    #constructor
    def __init__(self, currentTime):        
        self.__ctime = currentTime
        self.__thisYear = time.gmtime(self.__ctime/1000)[0]
        VTECTableUtil.VTECTableUtil.__init__(self)

    #squeezes the active table by eliminating entries that are no longer
    #needed for VTEC calculations.
    def squeeze(self, table):

        LogStream.logDebug("VTECTableSqueeze Original:\n",
          self.printVtecRecords(table, combine=True))

        # modify old UFN events (in case fcstrs didn't CAN them)
        table, modTable = self.__modifyOldUFNEvents(table)


        st = "VTECTableSqueeze MOD UFN Table:\n"
        for old, new in modTable:
            t = [old, new]
            st += "{rec}\n-----\n".format(
              rec=self.printVtecRecords([old, new]), combine=True)
        LogStream.logDebug(st)

        # remove the national center and short fused events 
        shortWFO, shortNC, purgeT = \
          self.__removeOldNationalAndShortFusedEvents(table)
        LogStream.logDebug("VTECTableSqueeze SHORT WFO TABLE:\n",
          self.printVtecRecords(shortWFO, combine=True))
        LogStream.logDebug("VTECTableSqueeze SHORT NATL CENTER TABLE:\n",
          self.printVtecRecords(shortNC,combine=True))
        LogStream.logDebug("VTECTableSqueeze INITIAL PURGE TABLE:\n",
          self.printVtecRecords(purgeT,combine=True))

        # separate out the shortWFO into dictionary structure
        dict = self.__separateTable(shortWFO)

        # purge old entries with LowerETNs that aren't in effect
        shorterT, purgeT2 = self.__purgeOldEntriesWithLowerETNs(dict)
        LogStream.logDebug("VTECTableSqueeze TRIMMED WFO TABLE:\n",
          self.printVtecRecords(shorterT,combine=True))
        LogStream.logDebug("VTECTableSqueeze ADDITIONAL PURGE TABLE:\n",
          self.printVtecRecords(purgeT2,combine=True))

        #add in any shortNC entries to final table
        for r in shortNC:
            shorterT.append(r)

        #add in the purged entries from before
        for r in purgeT2:
            purgeT.append(r)

        LogStream.logDebug("VTECTableSqueeze FINAL TABLE:\n",
          self.printVtecRecords(shorterT,combine=True))

        return shorterT, purgeT


    # separates table into a series of nested dictionaries and returns the
    # dictionary.  Dictionary returned is organized by:
    # officeid, phensig, issuanceYear, etn, id, and contains a sequence of records
    def __separateTable(self, table):
        d = {}
        for rec in table:
            officeid = rec['officeid']
            phensig = (rec['phen'], rec['sig'])
            issuance = time.gmtime(rec['issueTime']/1000)[0]
            etn = rec['etn']
            id = rec['geoId']
        
            # officeid
            if not d.has_key(officeid):
                d[officeid] = {}
        
            #phensig
            if not d[officeid].has_key(phensig):
                d[officeid][phensig] = {}
        
            #issuance year
            if not d[officeid][phensig].has_key(issuance):
                d[officeid][phensig][issuance] = {}
        
            #etn
            if not d[officeid][phensig][issuance].has_key(etn):
                d[officeid][phensig][issuance][etn] = {}
        
            #ids
            if not d[officeid][phensig][issuance][etn].has_key(id):
                d[officeid][phensig][issuance][etn][id] = []
        
            prevRecords = d[officeid][phensig][issuance][etn][id]
            prevRecords.append(rec)
    
        return d
            
    #figure out any "obsolete records" from the old table.  Obsolete
    #if there is a newer record available, regardless of action.
    #Skip over ROU codes.  Returns tuple of shortened table WFO, 
    #shortened table NC,  and purged table.
    #entries.
    def __removeOldNationalAndShortFusedEvents(self, table):
        compare = ['geoId', 'phen', 'sig', 'officeid']
        convWatch=[('SV','A'), ('TO','A')]
        #tropicalPhen=['TR','TY','HU'] Removed to disable 24hour purging for OB 8.2
        tropicalPhen=[]
        #shortFused=[('FA','W'), ('FF','W'), ('FL','W'), ('MA','W'),
        #  ('SV','W'), ('TO','W'), ('TS','W')]
        shortFused = []   #no longer purge shortFused events
    
        purgeTable = []
        shortTableWFO = []
        shortTableNC = []
        for oldR in table:

            #toss any old convective watch entries that are beyond their
            #times plus an hour.  We don't need to keep around these 
            #entries for ETN determination.
            if (oldR['phen'], oldR['sig']) in convWatch and \
              self.__ctime > oldR['endTime'] + (1*3600*1000): 
                purgeTable.append(oldR)
                continue
        
            #toss any old tropical entries that are beyond their
            #times.  Since the ending time is UFN, we have to use the
            #current time and issuance time for comparison:
            #We don't need to keep around these entries for ETN determination
            #TPC should issue these every 6 hours, so we err on the
            #safe side by saying 24 hr
            if oldR['phen'] in tropicalPhen and \
              self.__ctime > oldR['issueTime'] + (24*3600*1000):
                purgeTable.append(oldR)
                continue
        
            #toss any short-fused warning entries that are older than 1 hour
            if (oldR['phen'], oldR['sig']) in shortFused and \
              self.__ctime > oldR['endTime'] + (1*3600*1000):
                purgeTable.append(oldR)
                continue

            #toss any events that ended last year
            if self.__thisYear > time.gmtime(oldR['endTime']/1000)[0]:
                purgeTable.append(oldR)
                continue
            
            #keep this record 
#            if oldR['officeid'] in ['KWNS', 'KNHC']:
            if oldR['officeid'] in ['KWNS']:
                shortTableNC.append(oldR)
            else:
                shortTableWFO.append(oldR)

        return shortTableWFO, shortTableNC, purgeTable

    # Modify UntilFurtherNotice events that are very old to be "CAN"
    # Returns list of events that were modified, and the updated table.
    def __modifyOldUFNEvents(self, table):
        modTable = []
        for x in xrange(len(table)):
            entry = table[x]

            ufn = entry.get('ufn', 0)

            #UFN events only, old active events (2 weeks old)
            if ufn and self.__ctime > entry['issueTime'] + (14*86400*1000) and \
              entry['act'] not in ['CAN','EXP','UPG']:

                oldR = copy.deepcopy(entry)
                entry['act'] = "CAN"  #force to CANcel event
                modTable.append((oldR, entry))  #save old and mod events

        return table, modTable

    # Only keep the lowest ids from each phen/sig.  The "d" is a separated
    # out VTEC active table. All of the records in "d" end in the current
    # year (or future year).
    # d is [officeid][phensig][issueYear][etn][id] dictionary of records
    def __purgeOldEntriesWithLowerETNs(self, d):
        saveRec = []
        purgeRec = []
        tropicalPhen=['TR','TY','HU']
        for o in d.keys():
            phensig = d[o].keys()
            for ps in phensig:
                issueYears = d[o][ps].keys()
                for iy in issueYears:
                    etns = d[o][ps][iy].keys()

                    #get two maxetn for tropicalPhen (one for site created and the other
                    #for NHC created. The etn will be more than 100 for NHC hazards), 
                    #and the minimum id for this hazard
                    maxetn1 = max(etns)
                    maxetn2 = 0
                    if maxetn1 >= 1000: 
                       for my_etn in etns:
                          if my_etn > maxetn2 and my_etn < 1000:
                             maxetn2 = my_etn
                    else:
                       maxetn2 = maxetn1
                             
                    ids1 = d[o][ps][iy][maxetn1]
                    minid1 = min(ids1)

                    ids2 = ids1
                    minid2 = minid1

                    if maxetn2 > 0 and maxetn2 != maxetn1:
                       ids2 = d[o][ps][iy][maxetn2]
                       minid2 = min(ids2)

                    #determine what to keep and what to toss 
                    for etn in d[o][ps][iy].keys():
                        for id in d[o][ps][iy][etn].keys():
                            for rec in d[o][ps][iy][etn][id]:

                                ufn  = rec.get('ufn',0)
                                hourOld = self.__ctime > rec['endTime'] + (1*3600*1000)
                                twoWeeksOld = self.__ctime > rec['issueTime'] + (14*86400*1000)
                                cancelled = rec['act'] in ['CAN','UPG','EXP']
                                # keep records that are:
                                # 1. are UFN, not cancelled, and not older then two weeks.
                                # 2. not UFN, and not ended in the last hour
                                # 3. cancelled, from this year, are minid, and are maxetn
                                if ufn and not cancelled and not twoWeeksOld: # 1
                                    saveRec.append(rec)
                                elif not ufn and not hourOld: # 2
                                    saveRec.append(rec)
                                elif iy == self.__thisYear and \
                                  rec['geoId'] == minid1 and \
                                  rec['etn'] == maxetn1:
                                    if rec['officeid'] in ['KNHC'] and twoWeeksOld:
                                       LogStream.logDebug("******** WILL PURGE *******", rec['vtecstr'])
                                    else:
                                       saveRec.append(rec)
                                elif iy == self.__thisYear and \
                                  maxetn1 != maxetn2 and \
                                  rec['phen'] in tropicalPhen and \
                                  rec['geoId'] == minid2 and \
                                  rec['etn'] == maxetn2: # 3
                                    saveRec.append(rec)
                                # otherwise, remove them
                                else:
                                    LogStream.logDebug("******** WILL PURGE *******", rec['vtecstr'])
                                    purgeRec.append(rec)
        return saveRec, purgeRec
    
    #prints the dictionary organized by officeid, phensig, issueYear, etn, id
    def __printTable(self, d):
        officeid = d.keys()
        officeid.sort()
        for o in officeid:
            LogStream.logDebug("----------------------")
            LogStream.logDebug("OFFICE: ", o)
            phensig = d[o].keys()
            phensig.sort()
        
            for ps in phensig:
                LogStream.logDebug("    phensig: ", ps)

                issuances = d[o][ps].keys()
                issuances.sort()

                for iy in issuances:
                    LogStream.logDebug("      issueyear: ", iy)
                    etns = d[o][ps][iy].keys()
                    etns.sort()
            
                    for etn in etns:
                        #
                        LogStream.logDebug("        etn: ", etn)
                        ids = d[o][ps][iy][etn].keys()
                        for id in ids:
                            LogStream.logDebug("            id: ", id)
                            # REFACTOR rename self.printActiveTable to self.printVtecRecords
                            self.printVtecRecords(d[o][ps][iy][etn][id],combine=True)

    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()


    
    
