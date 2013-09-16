#---------------------------------------------------------------------------
# VTEC Ingester
#---------------------------------------------------------------------------
# This program accepts VTEC analyzed records and merges them into the
# vtec database records.  No I/O is performed in this routine.  The user
# is responsible for writing the output records to disk.
#---------------------------------------------------------------------------

import time
import copy
import Logger as LogStream
from VTECTableSqueeze import VTECTableSqueeze
from VTECTableUtil import VTECTableUtil


class VTECIngester(VTECTableUtil):
    def __init__(self):
        '''Constructor for VTECIngester'''
        self._vtecRecords = None

        VTECTableUtil.__init__(self)

    def mergedVtecRecords(self):
        '''Returns the merged vtec records.'''
        return self._vtecRecords

    def ingestVTEC(self, vtecAnalyzedRecords, vtecRecords, issueTime):
        '''Performs the ingester operation for VTEC codes.

        Keyword Arguments:
        vtecAnalyzedRecords -- List of dictionaries representing calculated
          vtec records to be merged into the vtecRecords.  Non-consolidated
          by id.
        vtecRecords -- List of dictionaries representing already issued vtec
          records from the database. Non-consolidated by id
        issueTime -- current time for processing records, same as issuance time.
          Units are milliseconds since epoch of Jan 1 1970 0000z. Affects
          purging of old records.

        Returns a dictionary containing the following elements, which are
        generally vtecRecords based on action performed:    
           updatedVtecRecords -- vtecRecords merge final results
           replacedRecords -- vtecRecords that were replaced
           decodedRecords -- vtecRecords that were decoded (incoming analyzed)
           otherRecords -- other vtecRecords not affected
           purgedRecords -- vtecRecords that were purged
           changedFlag -- indicates whether any records were changed
        '''

        LogStream.logDebug("Ingest VTEC...............")

        # ensure we have an issueTime in the records, and a 'state'
        for rec in vtecAnalyzedRecords:
            rec['issueTime'] = issueTime
            rec['state'] = 'Decoded'

        #add in the Previous state to the existing vtec records
        for r in vtecRecords:
            r['state'] = "Previous"

        #perform the merging
        updatedTable, purgeRecords, changedFlag = \
          self._updateVtecRecords(vtecRecords, vtecAnalyzedRecords,
          issueTime)

        replaced  = [r for r in updatedTable if r['state'] == "Replaced"]
        decoded = [r for r in updatedTable if r['state'] == "Decoded"]
        other = [r for r in updatedTable if r['state'] not in ['Replaced', 'Decoded']]
        self._vtecRecords = [r for r in updatedTable if r['state'] not in ['Replaced', 'Purged']]

        #strip out the "state" field
        for r in self._vtecRecords:
            del r['state']

        # return the information about changes
        d = {}
        d['updatedVtecRecords'] = self._vtecRecords
        d['replacedRecords'] = replaced
        d['decodedRecords'] = decoded
        d['otherRecords'] = other
        d['purgedRecords'] = purgeRecords
        d['dbChanged'] = changedFlag

        LogStream.logDebug("Updated VTEC Records:\n", self.printVtecRecords(
          self._vtecRecords, combine=True))
        LogStream.logDebug("Replaced VTEC Records:\n", self.printVtecRecords(
          replaced, combine=True))
        LogStream.logDebug("Decoded VTEC Records:\n", self.printVtecRecords(
          decoded, combine=True))
        LogStream.logDebug("Purged VTEC Records:\n", self.printVtecRecords(
          purgeRecords, combine=True))

        return d


    def _updateVtecRecords(self, vtecRecords, newRecords, issueTime):
        '''Merges the previous active table and new records into a new table.

        Keyword Arguments:
        vtecRecords -- list of dictionaries representing existing vtecRecords,
          non-consolidated form.
        newRecords -- list of dictionaries representing new vtecRecords to be
          merged, non-consolidated form.
        issueTime -- time of issuance, in units of milliseconds since epoch of
          Jan 1 1970 at 0000z.

        Returns a tuple of 3 values:
           updated table -- merged results
           purged records -- vtecRecords that were purged
           changeFlag -- True if anything has changed.
        '''

        updatedTable = []
        changedFlag = False


        #delete "obsolete" records from the old table.
        vts = VTECTableSqueeze(issueTime)
        vtecRecords, tossRecords = vts.squeeze(vtecRecords)
        for r in tossRecords:
            r['state'] = "Purged"
        del vts
        if len(tossRecords):
            changedFlag = True

        #expand out any 000 UGC codes, such as FLC000, to indicate all
        #zones. We do this by finding existing records with the 'id'
        #that matches.
        newRecExpanded = []
        compare1 = ['phen', 'sig', 'officeid', 'etn']
        for newR in newRecords:
            if newR['id'][3:6] == "000":
                for oldR in vtecRecords:
                    if self.vtecRecordCompare(oldR, newR, compare1) and \
                      oldR['id'][0:2] == newR['id'][0:2] and \
                      (oldR['act'] not in ['EXP', 'CAN', 'UPG'] or \
                       oldR['act'] == 'EXP' and oldR['endTime'] > issueTime):
                        newE = copy.deepcopy(newR)
                        newE['id'] = oldR['id']
                        newRecExpanded.append(newE)
            else:
                newRecExpanded.append(newR)
        newRecords = newRecExpanded
        
        # match new records with old records, with issue time is different
        # years and event times overlap. Want to reassign ongoing events
        # from last year's issueTime to be 12/31/2359z, rather than the
        # real issuetime (which is this year's).
        cyear = self.gmtime_fromMS(issueTime)[0]  #current year issuance time
        lastYearIssueTime = time.mktime((cyear-1, 12, 31, 23, 59, 
          0, -1, -1, -1))
        compare = ['phen', 'sig', 'officeid', 'etn']
        for newR in newRecords:
            for oldR in vtecRecords:
                if self.vtecRecordCompare(oldR, newR, compare):
                  oldYear = self.gmtime_fromMS(oldR['issueTime'])[0]
                  newYear = self.gmtime_fromMS(newR['issueTime'])[0]
                  if oldYear < newYear and self._vtecRecordsOverlap(oldR, newR):
                      LogStream.logVerbose("Reset issuance time to last year:",
                        "\nNewRec: ", self.printEntry(newR),
                        "OldRec: ", self.printEntry(oldR))
                      newR['issueTime'] = lastYearIssueTime*1000
                      LogStream.logVerbose("Changed To:", self.printEntry(newR))
                  

        # split records out by issuance year for processing
        newRecDict = {}   #key is issuance year
        oldRecDict = {}
        years = []
        for newR in newRecords:
            issueYear = self.gmtime_fromMS(newR['issueTime'])[0]
            records = newRecDict.get(issueYear, [])
            records.append(newR)
            newRecDict[issueYear] = records
            if issueYear not in years:
                years.append(issueYear)

        for oldR in vtecRecords:
            issueYear = self.gmtime_fromMS(oldR['issueTime'])[0]
            records = oldRecDict.get(issueYear, [])
            records.append(oldR)
            oldRecDict[issueYear] = records
            if issueYear not in years:
                years.append(issueYear)

        # process each year
        compare = ['id', 'phen', 'sig', 'officeid']
      
        for year in years:
            newRecords = newRecDict.get(year,[])
            oldRecords = oldRecDict.get(year,[])

            # now process the old and new records
            for oldR in oldRecords:
 
                keepflag = True
                for newR in newRecords:
    
                    if newR['act'] == "ROU":
                        continue
    
                    if self.vtecRecordCompare(oldR, newR, compare):
                        #we don't keep older records with same etns
                        if newR['etn'] == oldR['etn']:
                            keepflag = False   #don't bother keeping this record
                            break
    
                        #higher etns
                        elif newR['etn'] > oldR['etn']:
                            #only keep older etns if end time hasn't passed
                            #or old record is UFN and CAN:
                            ufn = oldR.get('ufn', 0)
                            if issueTime > oldR['endTime'] or \
                              (oldR['act'] == "CAN" and ufn) or \
                              oldR['act'] in ['EXP','UPG','CAN']:
                                keepflag = False
                                break
    
                        #lower etns, ignore (keep processing)
    
                if not keepflag:
                    oldR['state'] = "Replaced"
                    changedFlag = True
                updatedTable.append(oldR)

        #always add in the new records (except for ROU)
        compare = ['id', 'phen', 'sig', 'officeid', 'etn']
        for year in newRecDict.keys():
            newRecords = newRecDict[year]
            for newR in newRecords:
                if newR['act'] != "ROU":

                    #for COR, we need to find the original action, and 
                    #substitute it.
                    if newR['act'] == "COR":
                        for rec in updatedTable:
                            if self.vtecRecordCompare(rec, newR, compare):
                                LogStream.logVerbose(\
                                  "COR record matched with:",
                                  "\nNewRec: ", self.printEntry(newR),
                                  "OldRec: ", self.printEntry(rec),
                                  "\nReassign action to: ", rec['act'])
                                newR['act'] = rec['act']
                                break
                        #due to above code, this should never execute
                        if newR['act'] == "COR":
                            LogStream.logProblem("COR match not found for:\n",
                              self.printEntry(newR), "\nRecord discarded.")
                              
                    if newR['act'] != "COR":
                        updatedTable.append(newR)
                        changedFlag = True

        return updatedTable, tossRecords, changedFlag

