# Utility classes for the VTEC records 

import time
import copy


class VTECTableUtil(object):

    def __init__(self):
        '''Constructor for VTECTableUtil'''
        pass

#------------------------------------------------------------------
# Consolidate Utilities
#------------------------------------------------------------------

    #given the table, will consolidate like records and return a table
    #with identical vtecRecords, but with multiple id entries
    def consolidateByID(self, ptable):
        '''Consolidates the vtec records by id.

        Keyword Arguments:
        pTable -- list of dictionary records in the vtecRecord format,
          non-consolidated.

        Returns consolidated vtec records, with the 'id' field now a list.
        '''

        compare = ['etn','vtecstr','ufn','areaPoints','valuePoints','hdln',
          'key','previousStart','previousEnd','purgeTime','issueTime',
          'downgradeFrom','upgradeFrom','startTime','endTime','act','phen',
          'sig','officeid','seg','state','eventID', 'hvtec', 'subtype']

        ctable = []
        for a in ptable:
            found = False
            for c in ctable:
                if self.vtecRecordCompare(a, c, compare):
                    found = True
                    if isinstance(a['id'], list):
                        zones = a['id']
                    else:
                        zones = [a['id']]
                 
                    allzones = c['id']
                    for z in zones:
                        allzones.append(z)
                    c['id'] = allzones

                    break

            if not found:
                newc = copy.deepcopy(a)
                if not isinstance(newc['id'], list):
                    newc['id'] = [newc['id']]
                ctable.append(newc)

        return ctable

#------------------------------------------------------------------
# Printing Utilities
#------------------------------------------------------------------

    def printVtecRecords(self, table, combine=False):
        '''Pretty-prints the given vtecRecord or vtecRecords.

        Keyword Arguments:
        table - individual vtecRecord entry or list of vtecRecord entries.
        combine - If True, will return records with identical hazards from
          different ids.  If False, will not combine the records for output.

        Returns a string representing the table.
        '''
        
        if table is None:
            return "Table is None"

        #dictionary, single record
        elif isinstance(table, dict):
            ptable = [table]

        #list of dictionaries
        else:
            ptable = table
            if len(table) == 0:
                return "No records"

        s = '\n'   #Return value

        #combine mode, attempt to combine records
        if combine:
            ptable = self.consolidateByID(ptable)

        # print each entry
        for p in ptable:
            s += self.printEntry(p)

        return s


    def printEntry(self, h):
        '''Pretty-prints a single vtecRecord dictionary.

        Keyword Arguments:
        h -- single vtecRecord

        Returns a string representing the vtecRecord.
        '''

        #formatting for previousStart/End (only on some records)
        if h.has_key('previousStart') and h.has_key('previousEnd'):
            fmt = "PvStart: {start}\nPvEnd:   {end}\n"
            prevtecstr = fmt.format(start=self.printTime(h['previousStart']),
              end=self.printTime(h['previousEnd']))
        else:
            prevtecstr = ''

        #formatting for upgrades/downgrades special records
        if h.has_key('downgradeFrom'):
            related = h['downgradeFrom']
            duType = "DowngradeFrom: "
        elif h.has_key('upgradeFrom'):
            related = h['upgradeFrom']
            duType = "UpgradeFrom: "
        else:
            related = None
        if related:
            fmt = ("{title}: Action: {act}  Phen: {phen}  Sig: {sig} "
              "SubT: {subT}  Etn: {etn}\n"
              "  Start: {start}\n"
              "  End:   {end}\n")
            try:
               etnS = "{etn:04d}".format(etn=related.get('etn'))
            except ValueError:
               etnS = "{etn:<4}".format(etn=related.get('etn'))
            relatedText = fmt.format(title=duType, act=related.get('act'),
              phen=related.get('phen'), sig=related.get('sig'),
              subT=related.get('subtype'), etn=etnS,
              start=self.printTime(h.get('startTime')),
              end=self.printTime(h.get('endTime')))
 
        else:
            relatedText = ""

        # Hvtec
        hvtec = h.get('hvtec')
        if hvtec:
            hvtecStr = (
              'H-Vtec: PointID: {pointid}  Severity: {sev}  ImmCause: {ic}'
              '  FloodRec: {fr}\n'
              'H-Vtec: Flood Begin: {beg}\n'
              'H-Vtec: Flood Crest: {crest}\n'
              'H-Vtec: Flood End:   {end}\n'
              'H-Vtec: H-VTEC Str:  {hvtecstr:<54}\n'.format(
              sev=hvtec.get('floodSeverity'), ic=hvtec.get('immediateCause'),
              fr=hvtec.get('floodRecord'),
              hvtecstr=h.get('hvtecstr',''), pointid=hvtec.get('pointID'),
              beg=self.printTime(hvtec.get('riseAbove')),
              crest=self.printTime(hvtec.get('crest')),
              end=self.printTime(hvtec.get('fallBelow'))))
        else:
            hvtecStr = ''

        fmt = ("Vtec: {vtecstr}\n"
          "Hdln: {hdln}\n"
          "Start:   {startTime}  Action: {act}  Office: {officeid}\n"
          "End:     {endTime}  UFN: {ufn}\n"
          "Issue:   {issueTime}  Key: {key}\n{prevtecstr}"
          "Phen: {phen}  Sig: {sig}  SubT: {subtype}  Seg: {seg}  Etn: {etn}  Pil: {pil}"
          "  EventID: {eventID} RecState: {recState}\n"
          "ids: {zones}\n{relatedText}{hvtecStr}\n")

        try:
           etnS = "{etn:04d}".format(etn=h.get('etn'))
        except ValueError:
           etnS = "{etn:<4}".format(etn=h.get('etn'))
              
        t = fmt.format(vtecstr=h.get('vtecstr'), hdln=h.get('hdln'),
          startTime=self.printTime(h.get('startTime')), act=h.get('act'),
          eventID=h.get('eventID'), endTime=self.printTime(h.get('endTime')),
          ufn=h.get('ufn', 0), recState=h.get('state'),
          issueTime=self.printTime(h.get('issueTime')), key=h.get('key'),
          phen=h.get('phen'), sig=h.get('sig'), subtype=h.get('subtype'),
          officeid=h.get('officeid'), etn=etnS, seg=h.get('seg'), pil=h.get('pil'),
          zones=h.get('id'), relatedText=relatedText, hvtecStr=hvtecStr,
          prevtecstr=prevtecstr)

        return t


    def vtecRecordCompare(self, rec1, rec2, fields):
        '''Comparison routine for two vtecRecord entries.

        Keyword Arguments:
        rec1 -- first record for comparison in form of vtecRecord dictionary.
        rec2 -- 2nd record for comparison in form of vtecRecord dictionary.
        fields -- list of fields to check.

        Returns True if the two records are equal for the fields provided.
        '''
        for f in fields:
            if rec1.get(f) != rec2.get(f):
                return False
        return True

#---------------------
# time routines
#---------------------

    def gmtime_fromMS(self, time_ms):
        '''Provides for the gmtime() with input of time in milliseconds

        Keyword Arguments:
        time_ms -- time in milliseconds since epoch.

        Returns gmtime structure for the time input.
        '''

        return time.gmtime(time_ms / 1000)

    def printTime(self, time_sec):
        '''Prints out the time in ascii for string formatting operations.

        Keyword Arguments:
        time_ms -- time in milliseconds since epoch or None

        Returns a string representing the time, in both ascii format and
        as a number.
        '''
        fmt = "{asciiTime:<25} {num:13d}"

        if time_sec:
            time_ms = time_sec * 1000
            return fmt.format(num=int(time_ms),
              asciiTime=time.asctime(self.gmtime_fromMS(time_ms)))
        else:
            return fmt.format(num=0, asciiTime="None")

    def _containsT(self, tr, t):
        '''Returns True if the time range (tr) contains time (t).'''
        return (t >= tr[0] and t < tr[1])

    def _overlaps(self, tr1, tr2):
        '''Returns True if tr1 overlaps tr2 (adjacent is not an overlap)'''
        return self._containsT(tr2, tr1[0]) or self._containsT(tr1, tr2[0])

    def _vtecRecordsOverlap(self, v1, v2):
        '''Returns True if two records times overlap.'''
        tr1 = (v1['startTime'], v1['endTime'])
        tr2 = (v2['startTime'], v2['endTime'])
        return self._containsT(tr2, tr1[0]) or self._containsT(tr1, tr2[0])

    def _isAdjacent(self, tr1, tr2):
        '''Returns True if two time range are adjacent to each other.'''
        return tr1[0] == tr2[1] or tr1[1] == tr2[0]

    def _combineTR(self, tr1, tr2):
        '''Combines two time ranges and returns the 'span' of the times'''
        return (min(tr1[0], tr2[0]), max(tr1[1], tr2[1]))

