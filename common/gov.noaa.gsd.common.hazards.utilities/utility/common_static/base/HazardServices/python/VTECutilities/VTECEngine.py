"""This is the object that takes a proposed set of events, a list of the 
currently active events, and calculates the proper VTEC to determine the 
segments, VTEC codes, HVTEC codes, and vtecRecords.

    @author Mark Mathewson / Tracy.L.Hansen@noaa.gov
    @version 1.0

"""

#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    12/??/13        2368       Tracy L. Hansen   Changing from eventDics to 
#                                                 hazardEvents.
#    01/16/14        2462          dgilling       Rewrite to use GetNextEtnRequest.    
#    Feb 14, 2013    2161        Chris.Golden     Added use of UFN_TIME_VALUE_SECS constant
#    Aug  6, 2014    2826        jsanchez         Added boolean flags for issuing and operational mode
#                                                 instead of hardcoded value.
#    12/09/14        2826          dgilling       Revert previous changes.
#
    

import cPickle, os, types, string, copy
import sys, gzip, time
import collections
from VTECTableUtil import VTECTableUtil
from Pil import Pil
import Logger as LogStream
import ProductGenEtnProvider
import VTECConstants


# Define several named tuples for cleaner code
VTECDefinitions = collections.namedtuple('VTECDefinitions',
  'hazards upgradeDef downgradeDef')

EndedEvents = collections.namedtuple('EndedEvents', 'phen sig eventID areas')

ZCSort = collections.namedtuple('ZCSort', 'vtecRecords zoneCombo')


class VTECEngine(VTECTableUtil):

    def __init__(self, productCategory, siteID4, hazardEvents,
      rawVtecRecords, vtecDefinitions, allowedHazards,
      vtecMode, issueTime=None, limitGeoZones=None, issueFlag=True, operationalMode=True):

        '''Constructor for VTEC Engine
        Once instantiated, it will run the calculations.  The output may
        be accessed through different functions.
        
        NOTE :  Times are in seconds.

        Keyword Arguments:
        productCategory -- identifier for the product, which must match a 
          key in the ProductGeneratorTable.
        siteID4 -- identifier for the site, such as KBOU
        hazardEvents -- list of hazard events.
          All times within the records are in units of milliseconds since epoch
          of Jan 1 1970 at 0000Z.  This is changed on input to seconds.
        rawVtecRecords -- current active vtec records from the database.
        vtecDefinitions -- named tuple consisting of the HazardTypes dictionary,
          upgrade dictionary, and downgrade dictionary.
        allowedHazards - priority ordered list of (key, category) of the hazard
          types that are allowed in this product.
        vtecMode -- 'O' for operational product, 'T' for test product,
          'E' for experimental product, 'X' for Experimental VTEC in an
          operational product.
        issueTime -- time the engine is run, a.k.a. issue time.  Units of
          seconds since epoch (Jan 1 1970 00:00Z)
        limitGeoZones -- A list of zones used to limit the vtec logic.  This is
          only used in places where there are multiple products for the same
          hazard types issued at an office.  Example: PAFG (Fairbanks, AK).
        '''

        VTECTableUtil.__init__(self)
        # save data
        self._productCategory = productCategory
        self._pil = productCategory
        self._siteID4 = siteID4
        self._spcSiteID4 = 'KWNS'
        self._vtecDef = vtecDefinitions
        self._vtecMode = vtecMode
        self._etnCache = {}
        self._hazardEvents = hazardEvents
        self._issueFlag = issueFlag
        self._operationalMode = operationalMode
        
        # determine appropriate VTEC lifecycle, based on event dicts,
        # and the hazards table.  All hazard records provided must equate
        # to the same segmentation technique.
        combinableSegments = self._determineSegmentationBehavior(hazardEvents)

        # determine the type of data (geoType).  All hazard records provided
        # must equate to using the same geoType ('area', 'line', or 'point')
        self._geoType = self._determineGeoType(hazardEvents)

        # filter the allowedHazards down to just those appropriate, based on
        # the phen.sig combinableSegments.  This will let us eliminate those
        # hazards that don't apply for the segment behavior defined.
        self._allowedHazards = \
              [ah for ah in allowedHazards if vtecDefinitions.hazards[ah[0]]['combinableSegments'] == combinableSegments]
        try:
            self._allowedHazards = \
              [ah for ah in allowedHazards if vtecDefinitions.hazards[ah[0]]['combinableSegments'] == combinableSegments]
        except KeyError:
            raise Exception('Phen/Sig in AllowedHazards but not HazardTypes'
              ': {k}'.format(k=ah[0]))

        # list of marine products, required for special VTEC sort order
        self._marineProds = ['MWW']

        # ordering for segments vtec significance and action codes
        self._segmentVTECOrderList = [
          # Place holder for local hazards
          'LocalHazard',            
          'F.ROU', 'F.CON', 'F.EXT', 'F.EXA', 'F.EXB', 'F.NEW',
          'F.UPG', 'S.ROU', 'S.CON', 'S.EXT', 'S.EXA', 'S.EXB',
          'S.NEW', 'S.UPG', 'A.ROU', 'A.CON', 'A.EXT', 'A.EXA',
          'A.EXB', 'A.NEW', 'A.UPG', 'Y.ROU', 'Y.CON', 'Y.EXT',
          'Y.EXA', 'Y.EXB', 'Y.NEW', 'Y.UPG', 'W.ROU', 'W.CON',
          'W.EXT', 'W.EXA', 'W.EXB', 'W.NEW', 'W.UPG', 'F.EXP',
          'F.CAN', 'S.EXP', 'S.CAN', 'A.EXP', 'A.CAN', 'Y.EXP',
          'Y.CAN', 'W.EXP', 'W.CAN']

        # list of phen/sig from national centers and 'until further notice'
        self._tpcKeys = [('HU','A'), ('HU','S'), ('HU','W'), ('TR','A'), 
          ('TR','W')]
        self._tpcBaseETN = 1001
        self._ncKeys = [('TO','A'), ('SV','A'), ('HU','A'), ('HU','W'),
          ('TR','A'), ('TR','W'), ('HU', 'S')]
        self._ufnKeys = [('HU','A'), ('HU','S'), ('HU','W'), ('TR','A'),
          ('TR','W'), ('TY','A'), ('TY','W')]

        self._marineZonesPrefix = ['AM', 'GM', 'PZ', 'PK', 'PH', 'PM', 'AN',
          'PS', 'SL']   #list of zone name prefix that are marine zones

        # determine creation time, allows for non-current execution
        # creation time in seconds since epoch
        if issueTime is not None:
            self._time = issueTime
        else:
            self._time = time.time() #now time in seconds
        self._time = (int(self._time) / 60) * 60  #truncated to minute

        # sample, and merge vtec codes
        proposed, zonesP, limitEventIDs, endedEventIDs = \
          self._getProposedTable(hazardEvents, limitGeoZones,
          combinableSegments)

# TODO: determine if use of this code is necessary for interoperability
#         from com.raytheon.uf.common.dataplugin.events.hazards.requests import GetHazardsConflictDictRequest
#         from com.raytheon.uf.common.serialization.comm import RequestRouter 
#         request = GetHazardsConflictDictRequest()
#         response = RequestRouter.route(request)        
#         self._hazardsConflictDict = JUtil.javaObjToPyVal(response)
             
        self._allGEOVtecRecords, self._activeVtecRecords, zonesA = \
          self._getCurrentTable(rawVtecRecords, limitGeoZones, limitEventIDs)

        self._analyzedTable = self._calcAnalyzedTable(proposed,
          self._activeVtecRecords, endedEventIDs, combinableSegments)

        #print 'analyzed', self._analyzedTable
        # calculate the set of all zones in the proposed and vtec tables
        zoneList = zonesP | zonesA

        #print 'zoneList', zoneList
        # determine the segments (unsorted). Different algorithms based on
        # whether the hazards/segments are combinable.
        hazardCombinations = self._combineZones(
          self.consolidatedAnalyzedTable(), combinableSegments, zoneList)
        
        #print 'Combinations', hazardCombinations

        # calculate the segments, which does the sorting
        self._segments = self._sortSegments(hazardCombinations,
          combinableSegments)

        #print 'Segments', self._segments

        # calc the VTEC strings and vtecRecord Dictionaries. Important
        # to call calcVtecRecords before calcVTECString. 
        self._vtecRecordDict = {}
        for seg in self._segments:
            self._vtecRecordDict[seg] = self._calcVtecRecords(seg,
              combinableSegments)

        self._vtecStrings = {}
        for seg in self._segments:
            self._calcPil(seg)
            self._vtecStrings[seg] = self._calcVTECString(seg)
                    
#-----------------------------------------------------------------
# VTEC Engine Accessors
#-----------------------------------------------------------------


    def activeVtecRecords(self):
        '''Returns the filtered list of vtec records as a list of dicts'''
        return self.consolidateByID(self._activeVtecRecords)

    def analyzedTable(self):
        '''Returns the analyzed table as a list of dictionaries'''
        return self._analyzedTable

    def consolidatedAnalyzedTable(self):
        '''Returns the consolidated version of the analyzed table. Records that
        are identical other than geoArea are combined.  The 'id' field is 
        a list of zones.'''
        return self.consolidateByID(self._analyzedTable)

    def getSegments(self):
        '''Returns the list of segments in product-order.  Each segment is
        represented by a two-tuple.  The first tuple contains a set of zones
        or points representing the geographical designator for the segment; the
        second tuple contains a set of eventIDs that is within the segment.

        Note that for combinable segments, you will always have a unique set
        of zones (e.g., they won't repeat), but the set of eventIDs may be
        repeated among various segments.  For non-combinable segments, you may
        not have a unique set of zones/points, but the set of eventIDs will
        be unique and will not be repeated.
        '''
        return self._segments

    def getVTECString(self, segmentArea):
        '''Returns a list of strings containing the ordered p-vtec and h-vtec
        strings for the given segment area (point). The segment area must be
        one of the values returned from getSegments().
        '''
        return self._vtecStrings.get(segmentArea)

    def getVtecRecords(self, segmentArea):
        '''Returns a list of dictionaries that represent the vtecRecords in the
        provided segment area.  The segment area must be
        one of the values returned from getSegments().
        '''
        return self._vtecRecordDict.get(segmentArea)

#-----------------------------------------------------------------
# VTEC Engine Implementation Routines
#-----------------------------------------------------------------

    def _getExpirationLimits(self, hazardType):
        '''Returns the expiration time limits for the given hazard type

        Keyword Arguments:
        hazardType -- hazard key as found in the HazardTypes dictionary,
          such as 'TO.W'.

        Returns (fromExpirationMinutes, toExpirationMinutes).
        '''
        ExpTime = collections.namedtuple('ExpTime', 'beforeMinutes afterMinutes')
        before, after =  self._vtecDef.hazards[hazardType]['expirationTime']
        return ExpTime(before, after)

    def _allowAreaChange(self, hazardType):
        '''Returns a bool indicating whether EXAs are allowed for the
        hazard type

        Keyword Arguments:
        hazardType -- hazard key as found in the HazardTypes dictionary,
          such as 'TO.W'.

        Returns True or False.
        '''
        return self._vtecDef.hazards[hazardType]['allowAreaChange']

    def _allowTimeChange(self, hazardType):
        '''Returns a bool indicating whether EXTs are allowed for the
        hazard type

        Keyword Arguments:
        hazardType -- hazard key as found in the HazardTypes dictionary,
          such as 'TO.W'.

        Returns True or False
        '''
        return self._vtecDef.hazards[hazardType]['allowTimeChange']

    def _determineSegmentationBehavior(self, hazardEvents):
        '''Determine appropriate VTEC segmentation behavior, based on
        hazardEvents and the hazards table.  Ensures that all records in the
        hazardEvents use the same segmentation behavior or an exception is
        thrown.

        Keyword Arguments:
        hazardEvents -- list of hazard event dictionaries. 

        Returns True if the segments/hazards are combinable.
        '''
        try:
            hazardTypes = set(e.getHazardType() for e in hazardEvents)
        except KeyError:
            raise Exception("Hazard Type field missing in hazardEvent")

        try:
            combinableSegments = set(self._vtecDef.hazards[ht]['combinableSegments']
          for ht in hazardTypes)
        except KeyError:
            raise Exception("Hazard type '{k}' not in HazardTypes".format(
              k=ht))

        # If we can't get any information, assume segments are combinable
        if combinableSegments == set():
            combinableSegments = [True]

        # Ensure that we only have 1 combinable segments states
        if len(combinableSegments) != 1:
            s = 'Multiple combinableSegment states detected {s}'.format(
              s=combinableSegments)
            raise Exception(s)
        else:
            # take the 1st one, which is the only one
            combinableSegments = list(combinableSegments)[0]
        return combinableSegments

    def _determineGeoType(self, hazardEvents):
        '''Determines the geoType for the VTECEngine run.

        Keyword Arguments:
        hazardEvents - list of event hazard dictionaries.

        Returns the geoType for this engine run.  Throws exception if not
        determined.
        '''

        try:
            geoTypes = set(e.get('geoType') for e in hazardEvents)
        except KeyError:
            raise Exception("'geoType' missing from hazardEvent record")

        # extract the 1st one out, otherwise choose a default.
        # Verify that multiple geoTypes are not present.
        if len(geoTypes) == 1:
            return list(geoTypes)[0]
        elif len(geoTypes) == 0:
            return 'area'  # if no hazardEvents, then assume 'area'
        else:
            raise Exception('Multiple geoTypes detected {}'.format(geoTypes))


    def _calcVtecRecords(self, segment, combinableSegments):
        '''Calculates the vtec records for the segment.

        Keyword Arguments:
        segment -- Tuple containing the set of geographical ids, and the
          set of eventIDs, representing one segment.
        combinableSegments -- True if hazards can be combined into segments,
          False otherwise.

        Returns a list of dictionaries that represent the vtecRecords
        in the 'segment'.
        '''

        if combinableSegments:
            vtecRecords = [a for a in self.consolidatedAnalyzedTable() if 
              a['id'] & segment[0]]   # vtecRecords that overlap segment

            # some of the vtecRecords may have zones that include other
            # zones and segments.  We want to remove them here.
            vtecRecords1 = copy.deepcopy(vtecRecords)
            for h in vtecRecords1:
                h['id'] = segment[0] & h['id']
            return vtecRecords1

        else:
            # non-combinable segments
            vtecRecords = [a for a in self.consolidatedAnalyzedTable() if 
              a['id'] == segment[0] and a['eventID'] & segment[1]]
            return vtecRecords
        
    def _calcPil(self, segment):
        '''
        Sets the pil for each vtec record
        '''
        vtecRecords = self.getVtecRecords(segment)
        for vtecRecord in vtecRecords:
            hazardEvent = None
            for hazardEvent in self._hazardEvents:
                if hazardEvent.getEventID() == vtecRecord.get('eventID'):
                    break
            vtecRecord['pil'] = Pil(self._productCategory, vtecRecord, hazardEvent).getPil()
        
    def _calcVTECString(self, segment):
        '''Calculates the P-VTEC and H-VTEC strings for the segment.

        Keyword Arguments:
        segment -- Tuple containing the set of geographical ids, and the
          set of eventIDs, representing one segment.

        Returns a string containing the p-vtec and v-vtec strings for the
        given forecast segment.
        '''

        # get the list of vtecRecords for this segment
        vtecRecords = self.getVtecRecords(segment)   #could sort in here

        # sort the list of vtecRecords depending on the type of product
        if self._pil in self._marineProds:   # it's a marine product
            vtecRecords.sort(self._marineVtecRecordsSort)
        else:   # non-marine product
            vtecRecords.sort(self._vtecRecordsSort)

        # vtecRecords need upgrade records to be paired up
        vtecRecords = self._pairUpgradeRecords(vtecRecords)
            
        # get VTEC strings and VTEC records
        vtecStrings = []
        for i, v in enumerate(vtecRecords):
            vtecStrings.append(v['vtecstr'])

            # is there an v-vtec string?
            if v['hvtecstr']:

                # H-VTEC always included on non-CAN actions.
                if v['act'] != 'CAN':
                    vtecStrings.append(v['hvtecstr'])

                # H-VTEC not included on CAN for FA.A, FF.A exchange with paired
                # records: CAN/NEW, CAN/EXA, CAN/EXB
                elif i + 1 < len(vtecRecords) - 1:   # is there a next record?
                    v1 = vtecRecords[i + 1]
                    if v1['act'] not in ['NEW', 'EXA', 'EXB'] or \
                      not ((v1['key'] == 'FA.A' and v['key'] == 'FF.A') or \
                      (v1['key'] == 'FF.A' and v['key'] == 'FA.A')) :
                        vtecStrings.append(v['hvtecstr'])

                # no next record, so always output v-vtec
                else:
                    vtecStrings.append(v['hvtecstr'])

        return vtecStrings

    def _sortSegments(self, hazardCombinations, combinableSegments):
        ''' Calculates and sorts the segments into the proper order.

        Keyword Arguments:

        hazardCombinations -- list of segments (tuples of sets of zones and
          sets of eventIDs)
        combinableSegments -- True if hazards can be combined and placed in
          segments together.

        Returns the sorted segments.
        ''' 

        allVtecRecords = self.consolidatedAnalyzedTable()

        # reformat to prepare for sorting
        zcsort = []
        for zoneCombo in hazardCombinations:
            # get all vtecRecords for this zoneCombo
            if combinableSegments:
                vtecRecords = [a for a in allVtecRecords \
                  if a['id'] & zoneCombo[0]]

                # change the hazard record so it only has ids for this group
                vtecRecords = copy.deepcopy(vtecRecords)
                for v in vtecRecords:
                    v['id'] &= zoneCombo[0]

            # non-combinable segments
            else:
                vtecRecords = [a for a in allVtecRecords if a['id'] == zoneCombo[0]
                  and a['eventID'] & zoneCombo[1]] 

            vtecRecords.sort(self._vtecRecordsGroupSort)   # normal sorting order

            zc = ZCSort(vtecRecords, zoneCombo)
            zcsort.append(zc)
        zcsort.sort(self._comboSort)

        # extract out the results
        return [a.zoneCombo for a in zcsort]

    def _comboSort(self, a, b):
        ''' Comparison routine for two vtec records.

        Keyword Arguments:
        a -- combo 1 to compare
        b -- combo 2 to compare

        Returns -1, 0, +1 based on the comparisons.
        '''

        aRecords = a.vtecRecords
        bRecords = b.vtecRecords
        commonNum = min(len(aRecords), len(bRecords))
        for idx in range(commonNum):
            hs = self._vtecRecordsGroupSort(aRecords[idx], bRecords[idx])
            if hs != 0:
                return hs   # not equal

        # first common ones are equal, if we got here, then we send either
        # 1 or -1 depending which list is longer
        if len(aRecords) > len(bRecords):
            return -1
        elif len(aRecords) < len(bRecords):
            return 1

        # lists are equal length, and apparently test equal. Check the
        # zone combo itself.
        azc, aids = a.zoneCombo   # (set of zones, set of eventids)
        bzc, bids = b.zoneCombo
        azcs = list(azc)
        bzcs = list(bzc)
        azcs.sort()
        bzcs.sort()
        if azcs < bzcs:
            return -1
        elif azcs > bzcs:
            return 1

        # zone combos are equal, so check the list of eventIDs
        aidss = list(aids)
        bidss = list(bids)
        aidss.sort()
        bidss.sort()
        if aidss < bidss:
            return -1
        elif aidss > bidss:
            return 1
        return 0

    def _vtecRecordsGroupSort(self, a, b):
        ''' Comparison routine for two hazardEvent records.

        Keyword Arguments:
        a -- object A for compare
        b -- object B for compare

        Returns -1, 0, or 1 depending upon the sort criteria for a and b
        '''
    
        # check action code
        actionCodeOrder = ['CAN', 'EXP', 'UPG', 'NEW', 'EXB', 'EXA',
                           'EXT', 'CON', 'ROU']
        try:
            aIndex = actionCodeOrder.index(a['act'])
            bIndex = actionCodeOrder.index(b['act'])
        except ValueError:
            raise Exception('Invalid action code in vtecRecord in '
              '_vtecRecordsGroupSort:\n{a}\n{b}'.format(a=self.printEntry(a),
              b=self.printEntry(b)))

        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check sig
        sigOrder = ['W', 'Y', 'A', 'O', 'S', 'F']
        try:
            aIndex = sigOrder.index(a['sig'])
            bIndex = sigOrder.index(b['sig'])
        except ValueError:
            raise Exception('Invalid sig code in vtecRecord in '
              '_vtecRecordsGroupSort:\n{a}\n{b}'.format(a=self.printEntry(a),
              b=self.printEntry(b)))

        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check startTime
        if a['startTime'] > b['startTime']:
            return 1
        elif a['startTime'] < b['startTime']:
            return -1

        try:
            aIndex = self.getHazardImportance(a['key'])
            bIndex = self.getHazardImportance(b['key'])
            if aIndex > bIndex:
                return 1
            elif aIndex < bIndex:
                return -1
        except ValueError:
            if a['key'] > b['key']:
                return 1
            elif a['key'] < b['key']:
                return -1

        # check etn
        if a['etn'] < b['etn']:
            return 1
        elif a['etn'] > b['etn']:
            return -1

        # check seg
        if a['seg'] > b['seg']:
            return 1
        elif a['seg'] < b['seg']:
            return -1

        return 0

    def _vtecRecordsSort(self, a, b):
        '''Compares two vtecRecord records.

        Keyword Arguments:
        a -- object A for compare
        b -- object B for compare

        Returns 1, 0, or -1 depending upon
        whether the first vtecRecord is considered higher, equal, or lower
        priority when compared to the second as defined in the VTEC
        directive.
        '''

        # check action code
        actionCodeOrder = ['CAN', 'EXP', 'UPG', 'NEW', 'EXB', 'EXA',
                           'EXT', 'CON', 'ROU']
        try:
            aIndex = actionCodeOrder.index(a['act'])
            bIndex = actionCodeOrder.index(b['act'])
        except ValueError:
            raise Exception('Invalid action code in vtecRecord in '
              '_vtecRecordsSort:\n{a}\n{b}'.format(a=self.printEntry(a),
              b=self.printEntry(b)))
        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check sig
        sigOrder = ['W', 'Y', 'A', 'O', 'S', 'F']
        try:
            aIndex = sigOrder.index(a['sig'])
            bIndex = sigOrder.index(b['sig'])
        except ValueError:
            raise Exception('Invalid sig code in vtecRecord in '
              '_vtecRecordsSort:\n{a}\n{b}'.format(a=self.printEntry(a),
              b=self.printEntry(b)))
        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check startTime
        if a['startTime'] > b['startTime']:
            return 1
        elif a['startTime'] < b['startTime']:
            return -1

        # phen
        if a['phen'] > b['phen']:
            return 1
        elif a['phen'] < b['phen']:
            return -1

        # subtype
        if a['subtype'] > b['subtype']:
            return 1
        elif a['subtype'] < b['subtype']:
            return -1

        LogStream.logProblem('VtecRecords are identical in _vtecRecordsSort',
          self.printVtecRecords([a, b]))
        return 0
    
    def _marineVtecRecordsSort(self, a, b):
        '''Compares two vtecRecord records.

        Keyword Arguments:
        a -- object A for compare
        b -- object B for compare

        Returns 1, 0, or -1 depending upon
        whether the first vtecRecord is considered higher, equal, or lower
        priority when compared to the second as defined in the VTEC
        directive.  This sorting is based on the marine directive which
        is difference from that of the non-marine products.
        '''

        # check startTime
        if a['startTime'] > b['startTime']:
            return 1
        elif a['startTime'] < b['startTime']:
            return -1

        # check action code
        actionCodeOrder = ['CAN', 'EXP', 'UPG', 'NEW', 'EXB', 'EXA',
                           'EXT', 'CON', 'ROU']
        try:
            aIndex = actionCodeOrder.index(a['act'])
            bIndex = actionCodeOrder.index(b['act'])
        except ValueError:
            raise Exception('Invalid action code in vtecRecord in '
              '_vtecRecordsSort:\n{a}\n{b}'.format(a=self.printEntry(a),
              b=self.printEntry(b)))

        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check sig
        sigOrder = ['W', 'Y', 'A', 'S', 'F']
        try:
            aIndex = sigOrder.index(a['sig'])
            bIndex = sigOrder.index(b['sig'])
        except ValueError:
            raise Exception('Invalid sig code in vtecRecord in '
              '_vtecRecordsSort:\n{a}\n{b}'.format(a=self.printEntry(a),
              b=self.printEntry(b)))
        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check phen
        if a['phen'] > b['phen']:
            return 1
        elif a['phen'] < b['phen']:
            return -1

        # check etn
        if a['etn'] < b['etn']:
            return 1
        elif a['etn'] > b['etn']:
            return -1

        # subtype
        if a['subtype'] > b['subtype']:
            return 1
        elif a['subtype'] < b['subtype']:
            return -1

        LogStream.logProblem(\
          'Marine VtecRecords are identical in _marineVtecRecordsSort',
            self.printVtecRecords([a, b]))
        return 0

    def _pairUpgradeRecords(self, vtecRecords):
        '''Moves items in the list around such that the upgrades and
        downgrades are sequential (UPG, NEW, (CAN, NEW), etc.

        Keyword Arguments:
        vtecRecords -- list of dictionary records denoting the vtecRecords. These
          are not consolidated by ID.
        '''

        compare = ['etn', 'key']

        # get the list of upgraded or downgraded records
        upDownList = []
        for v in vtecRecords:
            if v.has_key('upgradeFrom') or v.has_key('downgradeFrom'):
                upDownList.append(v)

        # temporarily remove these guys from the vtecRecords
        for upDown in upDownList:
            vtecRecords.remove(upDown)

        # Hunt down their counterparts and add the record in the correct slot
        for upDown in upDownList:
            # get the fields from the up/downgradeFrom record
            oldRec = {}   
            if upDown.has_key('upgradeFrom'):
                oldRec = upDown['upgradeFrom']
            elif upDown.has_key('downgradeFrom'):
                oldRec = upDown['downgradeFrom']

            # find its match
            foundMatch = 0  # set a flag
            for v in vtecRecords:
                if self.vtecRecordCompare(oldRec, v, compare):
                    # found a match
                    vtecRecords.insert(vtecRecords.index(v) + 1, upDown)
                    foundMatch = 1
                    break  # done with this pass through vtecRecords

            if foundMatch == 0:
                LogStream.logProblem('Match not found for upgrade/downgrade.')

        return vtecRecords
        
    #-----------------------------------------------------------------
    # The following set of functions are utility functions.
    #-----------------------------------------------------------------

    def _prepETNCache(self, proposedRecord):
        '''Prepares the etn cache.  Adds new entries to the etn cache,
        but doesn't figure out the etn values at this point.  Organizes the
        information by phen.sig, then maintains a list of start, end, etns,
        eventIDs, and ids'''

        phensig = (proposedRecord['phen'], proposedRecord['sig'])
        id = proposedRecord['id']
        if self._etnCache.has_key(phensig):
            for start, end, etn, eventID, ids in self._etnCache[phensig]:
                if proposedRecord['startTime'] == start and \
                  proposedRecord['endTime'] == end and \
                  proposedRecord['eventID'] == eventID:
                    ids.append(id) # add the id
                    return #already in the cache
            times = self._etnCache[phensig]
            times.append((proposedRecord['startTime'], proposedRecord['endTime'], 0,
              proposedRecord['eventID'], [id]))

        else:
            self._etnCache[phensig] = [(proposedRecord['startTime'], 
              proposedRecord['endTime'], 0, proposedRecord['eventID'], [id])]
    
    def _assignNewETNs(self, vtecRecords, ignoreEventIDs):
        '''Assigns new etns to the etn cache. This is done after all requests
        for new etns have been made.

        Keyword Arguments:
        vtecRecords -- list of non-consolidated by id vtec records
          (dictionaries).
        ignoreEventIDs -- True, if the assignment of ETNs is to ignore
          the eventID.  Ignoring eventID can result in two ids getting the
          same ETN for a phen/sig.
          This also affects the assignment of ETNs with different eventIDs.

        No return value.
        '''
        
        # go through each new phen,sig
        for phen, sig in self._etnCache.keys():

            #determine the first new ETN to use if we need a new one
            etn_base = self._highestETNVtecRecords(phen, sig, 
              self._allGEOVtecRecords) 
            etn_base = etn_base + 1   #the next one in sequence

            #sort the etn cache by (start, end, etn, ...)
            self._etnCache[(phen, sig)].sort()  #sort the start,end,etns
            # keep track of the ids that have been given each etn
            coverage = {}

            #process sequentially each (phen, sig). Entries in cache
            #are list of startT (0), endT (1), etn# (2), eventID (3), [id] (4).
            times = self._etnCache[(phen, sig)]
            for x in xrange(len(times)):   
                s1, e1, etn1, eventID1, ids = times[x]
                #if no etn, then use a new one
                if etn1 == 0:   #etn == 0 indicates etn not yet assigned
                    etn1 = etn_base
                    etn_base = etn_base + 1
                    times[x] = (s1, e1, etn1, eventID1, ids)
                    coverage[etn1] = ids[:]

                # the ids for which a record with etn1 already exists
                assigned = coverage[etn1]

                #search for all adjacent or overlapping, give it the same etn
                for y in xrange(x+1, len(times)):
                    s2, e2, etn2, eventID2, ids2 = times[y]
                    if etn2 == 0 and \
                      (self._isAdjacent((s1, e1), (s2, e2)) or\
                      self._overlaps((s1, e1), (s2, e2))) and \
                      (eventID1 == eventID2 or ignoreEventIDs):

                        # check for potential ETN duplication
                        for id2 in ids2:
                            if id2 in assigned:
                                # cannot assign etn1 to this group since etn1
                                # is already assigned to a record for the zone
                                break
                        else:
                            # ok to assign etn1 to this group
                            etn2 = etn1  #reuse the etn
                            times[y] = (s2, e2, etn2, eventID2, ids2)

                            # add the ids to assigned list
                            assigned.extend(ids2)

    def _highestETNVtecRecords(self, phen, sig, vtecRecords):
        '''Returns the maximum etn used for the given phen/sig for the 
        current year, given the set of vtec records (not proposed records).

        Keyword Arguments:
        phen -- vtec phen code, e.g., TO
        sig -- vtec significance code, e.g., W

        Returns the max etn used.  If not yet used, 0 is returned.
        '''
        etn_provider = ProductGenEtnProvider.ProductGenEtnProvider(
                self._siteID4, self._time, self._tpcKeys, self._tpcBaseETN)
        highest_etn = etn_provider.getLastETN(phen, sig, vtecRecords, self._issueFlag, self._operationalMode)
        LogStream.logDebug('HIGHEST ETN for ', phen, sig, highest_etn)
        return highest_etn

    def _getNewETN(self, pRecord):
        '''Returns the ETN to be assigned to the vtec record, using the
        etn cache.

        Keyword Arguments:
        pRecord -- analyzed vtec record, needing an etn

        Returns the etn to be assigned.
        '''
        key = (pRecord['phen'], pRecord['sig'])
        if self._etnCache.has_key(key):
            times = self._etnCache[key]
            for startT, endT, etn, eventID, ids in times:
                if pRecord['startTime'] == startT and pRecord['endTime'] == endT and \
                  pRecord['eventID'] == eventID:
                    return etn
        raise Exception('ETN/Cache Issue. Could not find etn for {vr}.\n'
          'ETNcache is: {cache}'.format(vr=self.printVtecRecords(pRecord),
          cache=self._etnCache))



    #-----------------------------------------------------------------
    # The following set of functions are used to recombining
    # records from the raw analyzed table to keep the geographic
    # groups together.
    #-----------------------------------------------------------------

    def _organizeByZone(self, vtecRecords):
        '''Returns a dictionary that is keyed on zonename, and contains a list
        of all vtecRecords for that zone.

        Keyword Arguments:
        vtecRecords -- list of non-consolidated vtec records

        Returns dictionary with key being the id, and the values being
        a list of vtec records for that id.
        '''
        
        vtecRecordsByZone = {}
        for v in vtecRecords:
            vtecRecordsByZone.setdefault(v['id'], []).append(v)

        return vtecRecordsByZone

    def _organizeByKey(self, vtecRecords):
        '''Returns a dictionary that is based on key, and contains a list
        of all vtecRecords for each key value.

        Keyword Arguments:
        vtecRecords -- list of consolidated or non-consolidated vtec
          records.

        Returns dictionary with key being the phen/sig/subtype, and the
        values being a list of vtec records with that phen/sig/subtype.
        '''

        vtecRecs = {}
        for h in vtecRecords:
            vtecRecs.setdefault(h['key'], []).append(h)

        return vtecRecs

    def _separateMarineCountyZone(self, rec):
        '''Separates out the ugcs by marine, county, and zone designators.

        Keyword Arguments:
        rec -- consolidated vtec record.

        Returns list of ugc sets for the vtecRecord keeping the marine,
        county, and zone ugcs separated.  Each entry in the returned list
        will have at least one ugc in its set.
        '''
        mz = set(v for v in rec['id'] if v[2] == 'Z' and self._isMarineZone(v))
        pz = set(v for v in rec['id'] if v[2] == 'Z' and not self._isMarineZone(v))
        pc = set(v for v in rec['id'] if v[2] != 'Z')
        return [z for z in [mz, pz, pc] if z]

    def _combineZones(self, vtecRecords, combinableSegments, zoneList):
        '''Determines how to combine all of the vtecRecords based on the
        combinableSegments flag and the list of zones.

        Keyword Arguments:
        vtecRecords -- list of consolidated vtecRecords (dictionary) to
          determine how to combine.
        combinableSegments -- True if hazards can be combined into segments,
          otherwise False if only one hazard can be in a segment.
        zoneList -- A set of geographical identifiers that appear in
          the analyzed table and vtec records.

        Returns unsorted list of combinations represented as a list of
        tuples.  The tuples have a set of geographic identifiers and a
        set of eventIDs.
        '''

        if not zoneList:
            return frozenset([])   # no zone combo possible

        if not combinableSegments:
            outCombo = []   # start with an empty list
            for rec in vtecRecords:
                for ids in self._separateMarineCountyZone(rec):
                    if (ids, rec['eventID']) not in outCombo:
                        outCombo.append((ids, rec['eventID']))
        else:
            # start with a complete list
            outCombo = [zoneList]
            for rec in vtecRecords:
                for ids in self._separateMarineCountyZone(rec):
                    for i, c in enumerate(outCombo):
                        if c == ids:
                            break
                        common = c & ids
                        notCommon = c ^ common
                        if common and notCommon:
                            outCombo[i] = notCommon
                            outCombo.append(common) 

            # handle the event ids
            ev = []
            for c in outCombo:
                s = set()
                for a in vtecRecords:
                    if a['id'] & c:
                        s = s | a['eventID']
                ev.append(s)
            outCombo = zip(outCombo, ev)

        # delete empty segments, convert sets to frozensets so they are hashable
        out = []
        for zones, eids in outCombo:
            for rec in vtecRecords:
                if rec['eventID'] & eids and rec['id'] & zones:
                    out.append((frozenset(zones), frozenset(eids)))
                    break
        return out

    def _getProposedTable(self, hazardEvents, limitGeoZones, combinableSegments):
        '''Calculates and returns the proposed vtec tables.

        Keyword Arguments:
        hazardEvents -- list of objects representing the proposed events.
        limitGeoZones -- None for no limits, otherwise a list of geographical
          descriptors which limit the hazards to be processed to just those
          zones.
        combinableSegments -- True if hazards can be combined into segments,
          otherwise False if only one hazard can be in a segment.

        Returns a list of vtec record dictionaries representing the 
          proposed events. Returns a list of zones used for the calculations.
          Returns a list of eventIDs that the calculations should be
          limited to. Returns a list of events that have ended as EventIDs.
        '''
        
        # Convert events to VTEC Records
        LogStream.logDebug('HazardEvents: ', hazardEvents)
        atable = self._convertEventsToVTECrecords(hazardEvents)
        
        LogStream.logDebug('Proposed Table length: ', len(atable), atable)
        LogStream.logDebug('Sampled Proposed Table:\n', 
          self.printVtecRecords(atable, combine=True))
                
        # determine whether we want to limit the eventIDs for processing based
        # on the combinableSegments
        if combinableSegments:
            limitEventIDs = None
        else:
            limitEventIDs = set(a['eventID'] for a in atable)

        # Combine time entries
        atable = self._timeCombine(atable)
        LogStream.logDebug('Time Combine Proposed Table length: ', len(atable))
        LogStream.logDebug('Proposed Table after Time Combining:\n', 
          self.printVtecRecords(atable, combine=True))

        # remove vtecRecords that don't apply to the product
        atable = self.filterAllowedHazards(atable)
        LogStream.logDebug('Proposed Table, after filterAllowedHazards:\n',
          self.printVtecRecords(atable, combine=True))

        # remove vtecRecords in conflict (applies to GHG longFused only)
        if combinableSegments:
            atable = self.filterLowerPriority(atable)
            LogStream.logDebug('Proposed Table, after filterLowerPriority:\n',
              self.printVtecRecords(atable, combine=True))

        # remove vtecRecords that aren't in the specified geography
        atable = self.filterGeoZones(atable, limitGeoZones)
        LogStream.logDebug('Proposed Table, after filterGeoZones:\n',
          self.printVtecRecords(atable, combine=True))
        
        # determine zones in use (set of all zones in the proposed table)
        zones = set(a['id'] for a in atable)
                
        # make a list of those events that have been marked as 'ending'
       ## or has an ending time now or earlier than now
        endedEventIDs = set(a['eventID'] for a in atable
          if a.get('status') == 'ENDING')

        # strip out all ended events from proposed
        atable = [a for a in atable if a.get('status') != 'ENDING']

        # clear the h-vtec and p-vtec strings
        for a in atable:
            a['vtecstr'] = ''
            a['hvtecstr'] = ''

        LogStream.logVerbose('Proposed Table:\n', self.printVtecRecords(atable,
          combine=True))
        LogStream.logVerbose('Zones=', zones)
        LogStream.logVerbose('limitEventIDs=', limitEventIDs)
        LogStream.logVerbose('endedEventIDs=', set(endedEventIDs))

        return atable, zones, limitEventIDs, endedEventIDs

    def _getCurrentTable(self, rawVtecRecords, limitGeoZones, 
      limitEventIDs=None):
        '''Calculates and returns the current vtecRecords.

        Keyword Arguments:
        rawVtecRecords -- list of dictionaries representing the current vtec records.
          (Current vtec records denote those records in the vtec database and may
          include some older records for etn calculations.)
        limitGeoZones -- None for no limits, otherwise a list of geographical
          descriptors which limit the vtecRecords to be processed to just those
          zones.
        limitEventIDs -- None to not limit the records to just certain
          EventIDs.  Otherwise a list of eventIDs that should be included
          in the calculations, if no list, then no calculations.

        Returns a filtered list of vtec record dictionaries representing the 
          current table events. Returns a filtered list of vtec record
          dictionaries representing the current table events limited to just
          those geographical zones of interest.  Returns a list of zones used
          for the calculations.
        '''
        
        # Get the active table and do some filtering
        LogStream.logDebug('Raw Active Table length: ', len(rawVtecRecords))
        LogStream.logDebug('Raw Active Table:\n', 
          self.printVtecRecords(rawVtecRecords, combine=True))

        # Perform site filtering on the active table.  We keep
        # our site and SPC.
        siteFilter = [self._siteID4, self._spcSiteID4]
        allGEOVtecRecords = [a for a in rawVtecRecords if a['officeid'] in siteFilter]
        LogStream.logDebug('Active Table: after site filter:\n',
          self.printVtecRecords(allGEOVtecRecords, combine=True))

        # further filtering on the active table
        actTable = self.filterTestMode(allGEOVtecRecords,
          testMode=self._vtecMode == 'T')
        LogStream.logDebug('Active Table: after filter test mode:\n',
          self.printVtecRecords(actTable, combine=True))
        actTable = self.filterAllowedHazards(actTable)
        LogStream.logDebug('Active Table: after filterAllowedHazards:\n',
          self.printVtecRecords(actTable, combine=True))
        actTable = self.filterGeoZones(actTable, limitGeoZones)
        LogStream.logDebug('Active Table: after filterGeoZones:\n',
          self.printVtecRecords(actTable, combine=True))
        actTable = self.filterEventIDs(actTable, limitEventIDs)
        LogStream.logDebug('Active Table: after filterEventIDs:\n',
          self.printVtecRecords(actTable, combine=True))


        # determine zones in use
        zones = set(a['id'] for a in actTable)

        # delete upgrade/downgrade information in current table
        actTable = self._removeFieldFromTable(actTable, 'upgradeFrom')
        actTable = self._removeFieldFromTable(actTable, 'downgradeFrom')

        # eliminate the h-vtec and p-vtec strings
        for a in actTable:
            a['vtecstr'] = ''
            a['hvtecstr'] = ''
               
        LogStream.logDebug('Filtered Active Table length: ', len(actTable))
        LogStream.logDebug('Filtered Active Table:\n', 
          self.printVtecRecords(actTable, combine=True))

        return allGEOVtecRecords, actTable, zones

    #--------------------------------------------------------------
    # The following methods sample HazardEvents, obtain the active
    # table,  and create the analyzed table (including injecting 
    # the vtec strings into the table.  
    #--------------------------------------------------------------

    def _convertEventsToVTECrecords(self, hazardEvents):
        '''Create proposed table from hazardEvents.

        Keyword Arguments:
        @hazardEvents -- list of hazardEvent dictionaries.

        Returns list of vtec records representing the hazard events that are
        proposed.
        '''

        rval = []
        for hazardEvent in hazardEvents:

            if hazardEvent.get('siteID4') != self._siteID4:
                continue   #not for this site

            # hazard type
            key = hazardEvent.getHazardType()    #form XX.Y where XX is phen
            keyParts = key.split('.')
            phen = keyParts[0]
            sig = keyParts[1]   #form XX.Y where Y is sig
            try:
                subtype = keyParts[2]
            except IndexError:
                subtype = ''

            geoType = hazardEvent.get('geoType')
            if geoType in ['area', 'line']:
                areas = hazardEvent.get('ugcs')
            elif geoType == 'point':
                areas = [hazardEvent.get('pointID')]
            else:
                raise Exception("Unknown geoType 'type'".format(geoType))

            eventID = hazardEvent.getEventID()

            # capture any h-vtec from the HazardEvents
            hvtec = {}
            for item in ['floodSeverity', 'immediateCause', 'floodRecord',
              'riseAbove', 'crest', 'fallBelow', 'pointID']:
                hvtec[item] = hazardEvent.get(item) 
            # Convert from ms to seconds
            for item in ['riseAbove', 'crest', 'fallBelow']:
                if hvtec.get(item):
                    hvtec[item] = hvtec[item] / 1000
            if set(hvtec.values()) == set([None]):
                hvtec = None   # no vtec entries defined.

            #create the proposed dictionary for this HazardEvent.
            for areaID in areas:
                d = {}
                d['eventID'] = eventID
                d['id'] = areaID
                d['officeid'] = self._siteID4
                d['key'] = key   #such as TO.W or FF.W.Convective

                if hazardEvent.getStatus():
                    d['status'] = hazardEvent.getStatus()

                d['hvtec'] = hvtec

                d['startTime'] = float(time.mktime(hazardEvent.getStartTime().timetuple()))

                d['endTime'] = float(time.mktime(hazardEvent.getEndTime().timetuple()))

                d['act'] = '???'   #Determined after merges
                d['etn'] = hazardEvent.get('forceEtn', '???')
                d['seg'] = hazardEvent.get('forceSeg', 0)
                d['phen'] = phen    #form XX.Y where XX is phen
                d['sig'] = sig   #form XX.Y where Y is sig
                d['phensig'] = phen+'.'+sig
                d['subtype'] = subtype
                d['hdln'] = self._vtecDef.hazards[key]['headline']
                d['ufn'] = hazardEvent.get('ufn', 0)
                if geoType == 'point':
                    d['pointID'] = hazardEvent.get('pointID')

                rval.append(d)
                
        # handle UFN events - convert ending time to max
        for proposed in rval:
            phensig = (proposed['phen'], proposed['sig'])
            # these keys always get the event from now until forever
            if phensig in self._ufnKeys:
                proposed['startTime'] = self._time #now
                proposed['endTime'] = VTECConstants.UFN_TIME_VALUE_SECS
                proposed['ufn'] = 1  #until further notice

            # these events are forced to be until further notice. Leave
            # starting time as specified in the hazardEvent.
            elif proposed['ufn'] == 1:
                proposed['endTime'] = VTECConstants.UFN_TIME_VALUE_SECS
                proposed['ufn'] = 1  #until further notice

        return rval


    def _timeReduce(self, atable, index):
        '''Recursive utility function to combine records with adjacent times
        that are similar in other ways.

        Keyword Arguments:
        atable -- list of dictionaries represent events (vtecRecord format)
        index -- index indicator for combining.
        '''
 
        if index >= len(atable) - 1:
            return
        if atable[index]['endTime'] == atable[index + 1]['startTime']:
            atable[index]['endTime'] = atable[index + 1]['endTime']
            del atable[index + 1]
            self._timeReduce(atable, index)

    def _stripOld(self, atable):
        '''Remove any entries that are in the past from atable.

        Keyword Arguments:
        atable -- list of records in vtecRecord form.

        Returns list of records in vtecRecord form, filtering out records
        that contains records where the event ending time is past.
        '''
        return [a for a in atable if a['endTime'] > self._time]

    def _truncateCurrentTime(self, atable):
        '''Truncates vtec record entries to correctly determine the 
        starting and ending time, to ensure that the starting time is
        after the ending time.

        Keyword Arguments:
        atable -- list of records in vtecRecord form.

        Returns the modified table in vtecRecord form with old entries
        removed.
        '''
        now = int(self._time)
        for a in atable:
            if a['startTime'] < now:
                if now <= a['endTime']:
                    a['startTime'] =  now
                else:
                    a['startTime'] = a['endTime']
        return atable
    
    def _consolidateSortFunc(self, x, y):
        '''Sort Function for consolidating time values.

        Keyword Arguments:
        x -- sort value
        y -- sort value

        Returns -1, 0, or 1 depending upon the values.
        '''

        sortBy = ['officeid', 'id', 'phen', 'sig', 'seg', 'etn', 'act',
          'eventID', 'startTime']

        for skey in sortBy:
            if x[skey] < y[skey]:
                return -1
            elif x[skey] > y[skey]:
                return 1
        return 0  # all equal
            
    def _consolidateTime(self, atable):
        '''Consolidate time values and combine entries that are overlapping
        or adjacent.

        Keyword Arguments:
        atable -- list of vtecRecord dictionaries to consolidate by time.

        Returns the consolidated (by time) vtec records.
        '''

        atable.sort(self._consolidateSortFunc)

        # now attempt to combine entries that are overlapping/adjacent
        compare = ['officeid', 'id', 'phen', 'sig', 'seg', 'etn', 'act', 'eventID']
        if atable:
            for i, rec in enumerate(atable):
                if i + 1 < len(atable) and \
                  self.vtecRecordCompare(rec, atable[i + 1], compare):
                    tr1 = (rec['startTime'], rec['endTime'])
                    tr2 = (atable[i + 1]['startTime'], atable[i + 1]['endTime'])
                    if self._isAdjacent(tr1, tr2) or self._overlaps(tr1, tr2):
                        rec['endTime'] = atable[i + 1]['endTime']  # update time
                        del atable[i + 1]
        return atable

    def _timeCombine(self, atable):
        '''Modify the vtec records based on time information to filter
        them out of the equation.

        Keyword Arguments:
        atable -- list of vtecRecord dictionaries to consolidate by time.

        Returns the consolidated (by time) vtec records.
        '''
        atable = self._consolidateTime(atable)
        atable = self._stripOld(atable)
        atable = self._truncateCurrentTime(atable) 
        return atable

    def _copyFields(self, record, fields):
        '''Copies the specified fields from the record and returns a
        dictionary containing those fields.

        Keyword Arguments:
        record -- single vtecRecord dictionary.
        fields -- list of dictionary fields to extract

        Returns the dictionary containing those fields extracted.
        '''
        d = {}
        for f in fields:
            if record.has_key(f):
                d[f] = record[f]
        return d

    def _removeFieldFromTable(self, records, field):
        '''Removes the specified fields from the records.

        Keyword Arguments:
        records -- list of vtecRecord dictionaries.
        field -- field to remove from the vtecRecords.

        Returns the updated records.
        '''
        for rec in records:
            try:
                del rec[field]
            except KeyError:
                pass
        return records

    def getAllowedHazardList(self):
        '''Returns just a simple list of hazards in the form of the key (
        which is phen.sig.subtype or phen.sig if no subtype is present).
        '''
        return [h[0] for h in self._allowedHazards]

    def getHazardCategory(self, hazard):
        '''Returns the hazard category for the given hazard key.

        Keyword Arguments:
        hazard -- hazard key, in form of phen.sig, or phen.sig.subtype

        Returns the hazard category for the hazard.
        '''
        allowedHazardList = self._allowedHazards
        for h in allowedHazardList:
            if h[0] == hazard:
                return h[1]

        raise Exception('Hazard Category not found for {h}'.format(h=hazard))

    
    def getHazardImportance(self, hazard):
        '''Determines the priority of a Hazard (lower count = higher priority).

        Keyword Arguments:
        hazard -- hazard key, in form of phen.sig, or phen.sig.subtype

        Returns the hazard category for the hazard.
        '''
        phensigs = self.getAllowedHazardList()
        try:
            return phensigs.index(hazard) + 1
        except ValueError:
            raise Exception('Hazard Importance not found for {h}'.format(
              h=hazard))


    def fixRecordConflict(self, index1, index2, vtecRecords):
        '''This method uses the allowedHazards list to determine which
        vtecRecords entry has the most important priority and removes
        the entry or piece thereof in place.

        Keyword Arguments:
        index1 -- index into the vtecRecords to check for conflict.
        index2 -- index into the vtecRecords to check for conflict.
        vtecRecords -- list of dictionaries representing the analyzed 
          table in the vtecRecords format.

        Returns True if something was modified. Modifies the vtecRecords.
        '''
       
        allowedHazardList = self.getAllowedHazardList()
        phen1 = vtecRecords[index1]['phen']
        phen2 = vtecRecords[index2]['phen']
        sig1 = vtecRecords[index1]['sig']
        sig2 = vtecRecords[index2]['sig']
        act1 =  vtecRecords[index1]['act']
        act2 =  vtecRecords[index2]['act']
        haz1 = phen1 + '.' + sig1
        haz2 = phen2 + '.' + sig2
        ignoreList = ['CAN', 'EXP', 'UPG']

        if act1 in ignoreList or act2 in ignoreList:
            return False
       
        if self.getHazardCategory(haz1) != self.getHazardCategory(haz2):
            return False
                                                                                
        if self.getHazardImportance(haz1) < self.getHazardImportance(haz2):
            lowIndex = index2
            highIndex = index1
        else:
            lowIndex = index1
            highIndex = index2
        
        #
        # Added to prevent a current lower TO.A from overiding a higher SV.A
        #
        
        if vtecRecords[lowIndex]['phen'] == 'SV' and \
           vtecRecords[lowIndex]['sig'] == 'A' and \
           vtecRecords[highIndex]['phen'] == 'TO' and \
           vtecRecords[highIndex]['sig'] == 'A':
               if (int(vtecRecords[lowIndex]['etn']) > int(vtecRecords[highIndex]['etn']) and
                  (int(vtecRecords[highIndex]['etn']) - int(vtecRecords[lowIndex]['etn'])) > 50):
                   lowIndexTemp = lowIndex
                   lowIndex = highIndex
                   highIndex = lowIndexTemp
                           
        lowStart = vtecRecords[lowIndex]['startTime']
        lowEnd = vtecRecords[lowIndex]['endTime']
        highStart = vtecRecords[highIndex]['startTime']
        highEnd = vtecRecords[highIndex]['endTime']
                                                                                
        # first check to see if high pri completely covers low pri
        if highStart <= lowStart and highEnd >= lowEnd:  # remove low priority
            del vtecRecords[lowIndex]
                                                                                
        # next check to see if high pri lies within low pri
        elif lowStart <= highStart and lowEnd >= highEnd:  # high pri in middle
            if lowStart < highStart:
                h = copy.deepcopy(vtecRecords[lowIndex])
                # trim the early piece
                vtecRecords[lowIndex]['endTime'] = highStart
                if lowEnd > highEnd:
                    # make a new end piece
                    h['startTime'] = highEnd
                    vtecRecords.append(h)
            elif lowStart == highStart:
                vtecRecords[lowIndex]['startTime'] = highEnd
                                                                                
        elif highEnd >= lowStart:
            vtecRecords[lowIndex]['startTime'] = highEnd  # change low start
                                                                                
        elif highStart <= lowEnd:
            vtecRecords[lowIndex]['endTime'] = highStart  # change low end

        return True
    

    def filterAllowedHazards(self, vtecRecords):
        '''Removes all entries of the specified vtecRecords that are not
        in the allowedHazards list.

        Keyword Arguments:
        vtecRecords -- list of dictionaries in the vtec record format.

        Returns the modified vtecRecords.
        '''

        allowedHazardList = self.getAllowedHazardList()
        return [v for v in vtecRecords if v['key'] in allowedHazardList]

    def filterEventIDs(self, vtecRecords, limitEventIDs):
        '''Removes all entries of the specified vtecRecords whose EventIDs
        are not in the list of limitEventIDs. If limitEventIDs is None, then
        no filtering is performed.

        Keyword Arguments:
        vtecRecords -- list of dictionaries in the vtec record format.
        limitEventIDs -- None for no filtering, otherwise a list of eventIDs
          that are to be retained.

        Returns the modified vtecRecords.
        '''
        
        if limitEventIDs:
            return [v for v in vtecRecords if v['eventID'] in limitEventIDs]
        else:
            return vtecRecords

    def filterGeoZones(self, vtecRecords, limitGeoZones):
        '''Removes all entries of the specified vtecRecords whose geo id
        are not in the list of limitGeoZones. If limitGeoZones is None, then
        no filtering is performed. 

        Keyword Arguments:
        vtecRecords -- list of dictionaries in the vtec record format. 
            Must not be in consolidated by id format.
        limitGeoZones -- None for no filtering, otherwise a list of geo
          identifiers that are to be retained.

        Returns the modified vtecRecords.
        '''

        if limitGeoZones:
            table = [h for h in vtecRecords if h['id'] in limitGeoZones]
        else:
            return vtecRecords

    # This method filters out 'T' vtec records, or keeps just 'T' vtec records
    def filterTestMode(self, vtecRecords, testMode):
        '''Removes all entries of the specified vtecRecords whose vtec
        mode represent test or not, depending upon the testMode flag.

        Keyword Arguments:
        vtecRecords -- list of dictionaries in the vtec record format.
        testMode -- True to only keep test mode records, False to keep all
          records except for test mode records.

        Returns the modified vtecRecords.
        '''
# TODO: Re-instate when interoperability has stabilized        
# In practice mode, WarnGen uses the productClass 'T'
#         phensig = self._hazardEvents[0].getPhenomenon() + '.' + self._hazardEvents[0].getSignificance()
#         if self._operationalMode == False and phensig not in self._hazardsConflictDict:
#             testMode = True
            
        if testMode:
            return [a for a in vtecRecords if a['vtecstr'][0:3] == '/T.']
        else:
            return [a for a in vtecRecords if a['vtecstr'][0:3] != '/T.']
                                        
    def filterByZone(self, zone, vtecRecords):
        '''This method searches all entries of the specified vtecRecords for
        entries matching the specified zone.  Then for each entry it finds
        it looks for a conflicting entry in time.  If it finds one, it calls
        fixRecordConflict, which fixes the table and then calls itself again
        recursively with the fixed table.

        Keyword Arguments:
        zone -- geographic identifier to match.
        vtecRecords -- input/output list of dictionaries representing hazards
          in the non-consolidated by id vtec record format.

        '''

        for i in range(len(vtecRecords)):
            if vtecRecords[i]['id'] == zone:
                for j in range(i + 1, len(vtecRecords)):
                    if vtecRecords[j]['id'] == zone and i != j:
                        if self._vtecRecordsOverlap(vtecRecords[i], vtecRecords[j]):
                            if self.fixRecordConflict(i, j, vtecRecords):
                                self.filterByZone(zone, vtecRecords)
                                return


    def filterLowerPriority(self, vtecRecords):
        '''Main method that drives the code to filter vtecRecords that conflict
        in time. Only one hazard of the same phenomenon is allowed per zone
        per time.  This method processes the table, removing any time
        conflicts, so the one-hazard-per-zone-time rule is adhered to.

        Keyword Arguments:
        vtecRecords -- input/output list of dictionaries in the non-consolidated by 
            id vtec record format.

        Returns the modified vtecRecords.
        '''

        # get a raw list of unique edit areas
        zoneList = set(a['id'] for a in vtecRecords)
        for zone in zoneList:
            # Remove lower priority hazards of the same type
            self.filterByZone(zone, vtecRecords)

        return vtecRecords


    #-------------------------------------------------------------
    # The following functions handle the merging of the        
    # proposed and active tables. P-VTEC and H-VTECstrings are calculated 
    # in these routines.
    #-------------------------------------------------------------

    def _handleHYS(self, pTable):
        '''Handles the special case of HY.S by forcing the etn to 0
        and the action code to ROU.

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.

        Returns the modified proposed table.
        '''
        for p in pTable:
            if p['key'] == 'HY.S':
                p['act'] = 'ROU'
                p['etn'] = 0
        return pTable
             
    def _convertEXPtoCON(self, aTable):
        '''Converts active table EXP codes that are still in effect to CON
        codes.  This simplifies the logic of VTEC comparisons.

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.

        Returns the modified proposed table.
        '''
        for a in aTable:
            if a['act'] == 'EXP' and a['endTime'] > self._time:
                a['act'] = 'CON'
        return aTable

    def _checkForMergedRecords(self, proposedTable, activeVtecRecords):
        '''Checks and corrects for vtecRecords that have merged together.

        Keyword Arguments:
        proposedTable -- proposed set of vtecRecords, non-consolidated by ID.
        activeVtecRecords -- active set of vtecRecords, in non-consolidated format.

        Returns the modified proposed table and the modified activeVtecRecords table.
        '''

        # Checks for vtecRecords that have merged together.  This could result
        # in dropped VTEC entries so we need to EXT one and CAN the other.
        # We remove entries from the active table (memory copy) and generate
        # additional CAN vtecRecords.
        compare = ['id','phen','sig']

        createdCANEntries = []

        for proposed in proposedTable:
            matches = []
 
            #record match and time overlaps for real vtecRecords
            for active in activeVtecRecords:
                if self.vtecRecordCompare(proposed, active, compare) and \
                  active['act'] not in ['CAN','UPG','EXP'] and \
                  active['endTime'] > self._time and \
                  proposed['startTime'] <= active['endTime'] and \
                  proposed['endTime'] >= active['startTime']:
                    matches.append(active)

            #if multiple records match, we have a merged record
            #we need to find the highest etn for the record matches
            if len(matches) > 1:
                highestETN = 0
                for m in matches:
                    highestETN = max(highestETN, m['etn'])

                # find all other entries (non highest etn) and generate
                # new CAN records, then remove the entries from activeTable
                for m in matches:
                    if m['etn'] != highestETN:
                        canEntry = copy.deepcopy(m)
                        canEntry['act'] = 'CAN'
                        createdCANEntries.append(canEntry)
                        del activeVtecRecords[activeVtecRecords.index(m)]

        #return the modified set of records
        return (proposedTable + createdCANEntries, activeVtecRecords)


    def _checkForCONEXT(self, proposedTable, activeVtecRecords):
        '''Checks for vtecRecords that are CON or EXT.

        Keyword Arguments:
        proposedTable -- proposed set of vtecRecords,
          non-consolidated by ID.
        activeVtecRecords -- active set of vtecRecords, in non-consolidated format.

        Returns the modified proposed table.
        '''

        # A record is considered continued if two records have the same
        # id, phen, sig, and if the end times match.  A record
        # is considered to be extended in time if the record overlaps
        # in time.
        compare = ['eventID', 'id', 'key', 'officeid']  #considered equal

        for proposed in proposedTable:
            if proposed['act'] == 'CAN':
                continue   #only occurs with merged vtecRecords

            if proposed['endTime'] <= self._time:
                continue   #occurs with vtecRecords that are ending right now

            for active in activeVtecRecords:
                if self.vtecRecordCompare(proposed, active, compare) and \
                  active['act'] not in ['CAN', 'UPG', 'EXP']:

                    #convective watch (special case, also compare etn)
                    if proposed['phen'] in ['SV', 'TO'] and \
                      proposed['sig'] == 'A' and \
                      proposed['etn'] != active['etn']:
                        continue  #allows CAN/NEW for new convect watches

                    # times exactly match
                    if proposed['startTime'] == active['startTime'] and \
                      proposed['endTime'] == active['endTime']:
                        proposed['act'] = 'CON'
                        proposed['etn'] = active['etn']
                        proposed['issueTime'] = active['issueTime']
                    
                    # start times both before current time, end
                    # times the same, CON state
                    elif self._time >= proposed['startTime'] and \
                      self._time >= active['startTime'] and \
                      proposed['endTime'] == active['endTime']:
                        proposed['act'] = 'CON'
                        proposed['etn'] = active['etn']
                        proposed['issueTime'] = active['issueTime']

                    # special case of vtecRecord ended already, don't
                    # assign 'EXT' even with overlap
                    elif self._time >= active['endTime']:
                        pass   #force of a new vtecRecord since it ended

                    # start and/or end times overlap, 'EXT' case
                    # except when user changed the start time
                    # of a vtecRecord has gone into effect.  'EXT' has
                    # to be allowed.

                    elif self._vtecRecordsOverlap(proposed, active):
                        if not self._allowTimeChange(proposed['key']):
                            raise Exception('Illegal to adjust time for '
                              ' hazard {k}. \nProposed Record=\n{p}'
                              '\nActive=\n{a}'.format(
                              k=proposed['key'], p=self.printEntry(proposed),
                              a=self.printEntry(active)))

                        if active['startTime'] <= self._time:
                            if proposed['startTime'] <= self._time or \
                                   active.has_key('conexted'):
                                proposed['act'] = 'EXT'
                        else:
                            proposed['act'] = 'EXT'       

                        if proposed['act'] == 'EXT':
                            active['conexted'] = 1
                            proposed['etn'] = active['etn']
                            proposed['issueTime'] = active['issueTime']
                            
                            #save original time so we can later determine
                            #whether it is EXTENDED or SHORTENED
                            proposed['previousStart'] = active['startTime']
                            proposed['previousEnd'] = active['endTime']

        self._removeFieldFromTable(activeVtecRecords, 'conexted')

        return proposedTable

    def _checkForCANEXPUPG(self, pTable, activeVtecRecords, endedEventIDs=[]):
        '''Checks for vtecRecords that are CAN, EXP, UPG (ended).

        Keyword Arguments:
        pTable -- proposed set of vtecRecords, non-consolidated by ID.
        activeVtecRecords -- active set of vtecRecords, in non-consolidated format.
        endedEventIDs -- list of eventIDs that have status='ended'.  These 
          records have already been removed from the pTable.

        Returns the modified proposed table.
        '''

        compare1 = ['id', 'phen', 'sig']
        newEntries = []
        
        for active in activeVtecRecords:
            if active['officeid'] != self._siteID4:
                continue   #for a different site

            if active['act'] in ['CAN', 'UPG', 'EXP']:
                continue   #skip these records, vtecRecord already over

            cancel_needed = 1

            # if endedEventIDs match the active records, then we don't have
            # to do comparisons for matches.
            if active['eventID'] not in endedEventIDs:
            
                # determine if cancel is needed, cancel (CAN, EXP, UPG).
                # Cancel not needed if we have an entry in proposed that
                # is already in active and the times overlap, and the active
                # ending time is still in the future
                for proposed in pTable:
                    if self.vtecRecordCompare(active, proposed, compare1):
                        if self._vtecRecordsOverlap(proposed, active) and \
                          self._time < active['endTime']:

                            # active vtecRecord is in effect and proposed vtecRecord is
                            # in future

                            # cancel active vtecRecord
                            if active['startTime'] <= self._time and \
                                   proposed['startTime'] > self._time:
                                break

                            #convective watch, also check etn
                            if proposed['phen'] in ['SV', 'TO'] and \
                              proposed['sig'] == 'A':
                                if proposed['etn'] == active['etn']:
                                    cancel_needed = 0
                                    break
                            else:
                                cancel_needed = 0
                                break

            # CAN's have three special forms. CAN when a product is no longer
            # in the proposed table, EXP when the product is no longer 
            # in the proposed table, and the end was within 30 min of now,
            # and UPG when the phen is the same, but 
            # sig is upgraded, and the VTEC is still in effect.
            #
            if cancel_needed == 1:

                # Case One - UPG
                # Area matches, phen matches, and we are going from an 
                # advisory to a watch, a watch to a warning, or an
                # advisory to a warning.

                for proposed in pTable:
                    #find matches in area, do phen later
                    if self.vtecRecordCompare(active, proposed, ['id']):

                        #find overlaps in time
                        if self._vtecRecordsOverlap(proposed, active):

                            if self._isUpgrade(proposed, active):
                                active['act'] = 'UPG'
                                active['seg'] = 0
                                if active not in newEntries:
                                    newEntries.append(active)
                                cancel_needed = 0
      
                # Case Two - EXP
                # If it wasn't an UPG, then check for EXP. EXP if entry
                # not in the proposed table, and current time is after
                # the EXP time.

                if cancel_needed == 1:
                    timeFromEnd = self._time - active['endTime']   # +after
                    if timeFromEnd >= 0:
                        active['act'] = 'EXP'
                        active['seg'] = 0
                        if active not in newEntries:
                            newEntries.append(active)
                        cancel_needed = 0

                # Final Case - CAN
                # Only Allow 'CAN' entries if the vtecRecord is still ongoing, 
                # otherwise ignore the entry.
                if cancel_needed == 1:
                    if self._time < active['endTime']:
                        active['act'] = 'CAN'
                        active['seg'] = 0
                        if active not in newEntries:
                            newEntries.append(active)
                        cancel_needed = 0

        # return the composite table of the modified proposed and new entries
        return pTable + newEntries

    def _checkForEXAEXB(self, pTable, activeVtecRecords):
        '''Checks for vtecRecords that are EXA and EXB.

        Keyword Arguments:
        pTable -- proposed set of vtecRecords, non-consolidated by ID.
        activeVtecRecords -- active set of vtecRecords, in non-consolidated format.

        Returns the modified proposed table.
        '''

        compare1 = ['id', 'phen', 'sig', 'etn', 'officeid']
        compare2 = ['phen', 'sig']

        for proposed in pTable:

            # do we allow EXA, EXB?
            if not self._allowAreaChange(proposed['key']):
                continue

            # first check to see if we have already assigned an action.
            # This is a special case for SPC watches that now appear in the
            # proposed table, but haven't been issued yet.  In this case,
            # we skip processing this record. Other vtecRecords may have already
            # been assigned actions by this point.
            if proposed['act'] != '???':
                continue

            # Assume first that this is EXA or EXB
            exaexb_flag = 1

            #if we find a match, and it overlaps in time, 
            #then it isn't an EXA, EXB
            for active in activeVtecRecords:
                if self.vtecRecordCompare(proposed, active, compare1):
                    #if proposed['startTime'] <= active['endTime'] and 
                    #  proposed['endTime'] >= active['startTime'] and 
                    if self._vtecRecordsOverlap(proposed, active) and \
                      active['act'] not in ['CAN','EXP','UPG']:
                        exaexb_flag = 0
                        
            # no match was found, thus this is either a EXA, or EXB,
            # match records with phen and sig the same
            if exaexb_flag == 1:
                #first check for EXA, must check ALL records before
                #deciding it isn't an EXA
                for active in activeVtecRecords:
                    if self.vtecRecordCompare(proposed, active, compare2):
                        if active['act'] not in ['CAN', 'UPG', 'EXP']:

                            #if times are identical, then we extended in area 
                            if proposed['startTime'] == active['startTime'] and \
                              proposed['endTime'] == active['endTime']:
                                if proposed['etn'] == '???' or \
                                  proposed['etn'] == active['etn']:
                                    proposed['exaexb'] = 'EXA'
                                    proposed['active'] = active
                                    proposed['issueTime'] = active['issueTime']
                                    break

                            #if start times are both in the past or
                            #current, but end times equal, then it is
                            #an EXA
                            elif proposed['startTime'] <= self._time and \
                              active['startTime'] <= self._time and \
                              proposed['endTime'] == active['endTime']:
                                if proposed['etn'] == '???' or \
                                  proposed['etn'] == active['etn']:
                                    proposed['exaexb'] = 'EXA'
                                    proposed['active'] = active
                                    proposed['issueTime'] = active['issueTime']
                                    break

                if proposed.has_key('exaexb'):
                    continue

                #if it isn't an EXA, now we check the records again, but
                #check for overlapping or adjacent times, that do
                #not occur in the past in the active table, but ensure
                #that there is a vtecRecords in the proposed that overlaps
                #with time. Results in EXB
                if proposed['act'] == '???':
                    for active in activeVtecRecords:
                        if self.vtecRecordCompare(proposed, active, compare2):
                            if active['act'] not in ['CAN', 'UPG', 'EXP']:
                                #if self._vtecRecordsOverlap(proposed, active) and
                                if proposed['startTime'] <= active['endTime'] and \
                                  proposed['endTime'] >= active['startTime'] and \
                                  active['endTime'] > self._time:
                                    if proposed['etn'] == '???' or \
                                      proposed['etn'] == active['etn']:
                                        #ensure record overlaps with proposed vtecRecords
                                        for p1 in pTable:
                                            if p1 == proposed:
                                                continue  #skip itself
                                            if self.vtecRecordCompare(p1, proposed,
                                              compare2) and self._vtecRecordsOverlap(p1, proposed):
                                                proposed['exaexb'] = 'EXB'
                                                proposed['active'] = active
                                                proposed['issueTime'] = active['issueTime']
                                                break
                                        break

        # Now set the marked records to EXA/EXB unless
        # there is already a CAN/EXP/UPG record with the same ETN
        # for the same phen/sig in the same zone

        # Organize vtecRecords by zone
        zoneDict = self._organizeByZone(pTable)
        for zone, vtecRecords in zoneDict.iteritems():
            # then organize by hazard key
            vtecRecords = self._organizeByKey(vtecRecords)
            for key, recs in vtecRecords.iteritems():
                for proposed in recs:

                    if proposed.has_key('exaexb'):
                        act = proposed.pop('exaexb')
                        active = proposed.pop('active')
                        # checking if the etn is used
                        for p in recs:
                            if p['etn'] == active['etn'] and \
                              p['act'] != '???':
                                break
                        else:
                            proposed['act'] = act
                            proposed['etn'] = active['etn']

                            if act == 'EXB':
                                #save original time so we can later 
                                #determine whether it is EXTENDED 
                                #or SHORTENED
                                proposed['previousStart'] = active['startTime']
                                proposed['previousEnd'] = active['endTime']

        # Check to ensure we didn't attempt to extend in area a non-extendable
        self._checkInappropriateEXA(pTable)

        return pTable


    def _checkForNEW(self, pTable, activeVtecRecords, ignoreEventIDs):
        '''Assigns NEW to remaining records and calculates the correct
        ETN number.

        Keyword Arguments:
        pTable -- proposed set of vtecRecords, non-consolidated by ID.
        activeVtecRecords -- active set of vtecRecords, in non-consolidated format.
        ignoreEventIDs -- True, if the assignment of ETNs is to ignore
          the eventID.  Ignoring eventID can result in two ids getting the
          same ETN for a phen/sig.
          This also affects the assignment of ETNs with different eventIDs.

        Returns the modified proposed table.
        '''

        compare = ['id', 'key', 'officeid', 'eventID']

        #check for any remaining records that have an undefined action
        #these records must be 'NEW'.  Need to allocate a new etn, except
        #in two cases: one is already identified in the proposed table,
        #existing record in active table (phen,sig,id) regardless of pil.
        #
        #Already identified are basic TO.A, SV.A using aux data fields,

        allowedActions = ['NEW','CON','EXT','EXA','EXB']

        for proposed in pTable:
            if proposed['act'] == '???':
                if proposed['etn'] == '???':
                    #check in active table for a match (from other product),
                    #with vtecRecords that still are occurring
                    etn = 0
                    for act in activeVtecRecords:
                        if self._vtecRecordsOverlap(proposed, act) and \
                          act['act'] in allowedActions and \
                          self.vtecRecordCompare(proposed, act, compare) and \
                          act['endTime'] > self._time:
                            etn = act['etn']
                            break

                    #not found in active nor proposed, prep for new one
                    if etn == 0:
                        self._prepETNCache(proposed)
                    else:
                        proposed['etn'] = etn   #match found in active table

                proposed['act'] = 'NEW'

                # adjust starting time of new vtecRecords to prevent them from
                # starting in the past and for ending vtecRecords
                if proposed['startTime'] < self._time:
                    if self._time <= proposed['endTime']:
                        proposed['startTime'] = self._time
                    else:
                        proposed['startTime'] = proposed['endTime']

        # determine any new ETNs
        self._assignNewETNs(activeVtecRecords, ignoreEventIDs)
        LogStream.logDebug('New ETN cache: ', self._etnCache)

        # process again for records that are now marked NEW, but no etn
        for proposed in pTable:
            if proposed['act'] == 'NEW' and proposed['etn'] == '???':
                proposed['etn'] = self._getNewETN(proposed)

        return pTable

    def _checkInappropriateEXA(self, pTable):
        '''Adds for inappropriate extensions.

        Keyword Arguments:
        pTable -- proposed set of vtecRecords, non-consolidated by ID.

        Throws exception if not proper extended in area.
        '''
        
        # find records that are similar, except for the actions
        compare = ['key', 'officeid', 'startTime', 'endTime', 'eventID']
        for p1 in pTable:
            for p2 in pTable:
                if self.vtecRecordCompare(p1, p2, compare):
                    if not self._allowAreaChange(p2['key']):
                        if ((p1['act'] == '???' and p2['act'] != '???') or
                          (p1['act'] != '???' and p2['act'] == '???')):
                            raise Exception('Illegal to adjust area for hazard'
                              ' {k}.\nProposed Records\n{p1}\n{p2}'.format(
                              k=p1['key'], p1=self.printEntry(p1),
                              p2=self.printEntry(p2)))

    def _addEXPCodes(self, pTable):
        '''Adds in EXP codes (instead of CON) for vtecRecords ready to expire.

        Keyword Arguments:
        pTable -- proposed set of vtecRecords, non-consolidated by ID.

        Returns the modified proposed table.
        '''

        compare = ['id', 'key', 'officeid', 'eventID']
        
        #looks for vtecRecords that have 'CON', but are within 'expTimeLimit' 
        #minutes of vtecRecord ending time and converts those vtecRecords to EXP. 
        for each_hazard in pTable:
            if each_hazard['act'] == 'CON':
                timeFromEnd = self._time - each_hazard['endTime']   # +after
                expFromEnd = self._getExpirationLimits(
                  each_hazard['key']).beforeMinutes
                if timeFromEnd >= expFromEnd*60 and timeFromEnd <= 0:
                    each_hazard['act'] = 'EXP'   #convert to expired

        return pTable
        
    def _removeEXPWithOngoingCodes(self, pTable):
        '''Remove EXP codes when another vtecRecord with same phen/sig is now
        ongoing for this issuance year.

        Keyword Arguments:
        pTable -- proposed set of vtecRecords, non-consolidated by ID.

        Returns the modified proposed table.
        '''
        compare = ['phen','sig','etn','id']
        tmp = []
        for p in pTable:
            removeIt = False
            #vtecRecords with EXP, and after ending time
            if p['act'] == 'EXP' and self._time >= p['endTime']:
                pIssueT = p.get('issueTime', self._time)
                pIssueYear = time.gmtime(pIssueT)[0]
                for p1 in pTable:
                    #active vtecRecord with same phen/sig/etn
                    p1IssueT = p1.get('issueTime', self._time)
                    p1IssueYear = time.gmtime(p1IssueT)[0]
                    if p1['act'] in ['CON','EXA','EXB','EXT'] and \
                      self.vtecRecordCompare(p, p1, compare) and \
                      p1IssueYear == pIssueYear:
                        removeIt = True
                        break
            if not removeIt:
                tmp.append(p)
        return tmp

    def _adjustStartTimesBasedOnNow(self, pTable):
        '''Adjusts the starting/ending times of the vtecRecords to ensure they
        don't start in the past, and that the starting time is never past
        the ending time of the vtecRecord.

        Keyword Arguments:
        pTable -- list of dictionaries in the vtecRecord format.

        Returns the modified vtecRecord table.
        '''

        for p in pTable:
            # adjust time of NEW vtecRecords to ensure they don't start
            # earlier than now
            if p['startTime'] < self._time:
                if self._time <= p['endTime']:
                    p['startTime'] = self._time
                else:
                    p['startTime'] = p['endTime']
        return pTable
            
    def _addVTECStrings(self, pTable):
        '''Add VTEC strings to all vtecRecords.

        Keyword Arguments:
        pTable -- list of dictionaries in the vtecRecord format.

        Returns the modified vtecRecord table.
        '''
        for p in pTable:

            # get the VTEC Mode
            if self._vtecMode is None:
                p['vtecstr'] = ''
                continue

            # use 00000000 or explicit times for the start time?  Use all
            # zeros for ongoing vtecRecords and for HY.S events.
            if (p['act'] == 'NEW' or \
              (p['act'] == 'EXT' and p['previousStart'] > self._time) or \
              (p['act'] == 'EXB' and p['previousStart'] > self._time) or \
              (p['startTime'] > self._time)) and p['key'] != 'HY.S':
                startStr = self._vtecTimeStr(p['startTime'])
            else:
                startStr = self._vtecTimeStr(None)

            # use 00000000 if event is 'Until Further notice'
            if p.get('ufn', 0) or p['key'] == 'HY.S':
                endStr = self._vtecTimeStr(None)
            else:
                endStr = self._vtecTimeStr(p['endTime'])

            # format the beastly string
            vfmt = '/{vm}.{act}.{site}.{phen}.{sig}.{etn:04d}.{start}-{end}/'
            p['vtecstr'] = vfmt.format(vm=self._vtecMode, site=p['officeid'],
              phen=p['phen'], sig=p['sig'], etn=p['etn'], start=startStr,
              end=endStr, act=p['act'])


    # generate H-VTEC strings for hazards
    def _addHVTECStrings(self, pTable):
        '''Add HVTEC strings to all appropriate vtecRecords.

        Keyword Arguments:
        pTable -- list of dictionaries in the vtecRecord format.

        Returns the modified vtecRecord table.
        '''
        for p in pTable:
            hvtec = p.get('hvtec')   # must have hvtec dictionary defined
            if hvtec:

                # determine the time strings
                startStr = self._vtecTimeStr(hvtec.get('riseAbove'))
                crestStr = self._vtecTimeStr(hvtec.get('crest'))
                endStr = self._vtecTimeStr(hvtec.get('fallBelow'))

                # these are always defined in the dict, but might be None.
                # So for None, we want default.
                sev = hvtec.get('floodSeverity')
                ic = hvtec.get('immediateCause')
                fr = hvtec.get('floodRecord')
                if not ic:
                    ic = 'UU'
                if not fr:
                    fr = 'OO'
                if sev is None:
                    if self._geoType == 'point':
                        sev = 'N'
                    elif self._geoType == 'area':
                        sev = 0
                idStr = hvtec.get('pointID') if self._geoType == 'point' else '00000'
                
                hfmt = '/{id}.{sev}.{ic}.{beg}.{crest}.{end}.{fr}/'
                p['hvtecstr'] = hfmt.format(id=idStr, sev=sev, ic=ic, fr=fr,
                  beg=startStr, crest=crestStr, end=endStr)

            else:
                p['hvtecstr'] = None


    # Add in headlines if missing in the table, note that headlines
    # are not added for situations of merged vtecRecords, i.e., an vtecRecord
    # that has a CAN and a ongoing with same phen/sig and overlapping time.
    # Leaving 'hdln' blank indicates no headline and no mention in hazard
    # products.
    def _addHeadlinesIfMissing(self, pTable):
        '''Add headlines to vtec records if missing.

        Keyword Arguments:
        pTable -- list of dictionaries in vtecRecord format.

        Returns the modified vtecRecord table.
        '''

       
        compare = ['id','phen','sig']
        ongoingAct = ['EXT','EXB','CON','NEW','EXA']
        for v in pTable:

#TODO: Not clear if this will ever run, since apparently all entries in the
#pTable have a 'hdln' key.  
            if v.has_key('hdln'):
                continue
            phensig = v['phen'] + '.' + v['sig']

            #ongoing (merged) and CAN situation?
            mergedFound = False
            for v1 in pTable:
                if self.vtecRecordCompare(v, v1, compare) and \
                  v['act'] == 'CAN' and v1['act'] in ongoingAct and \
                  v1['endTime'] > self._time and \
                  v['startTime'] <= v1['endTime'] and \
                  v['endTime'] >= v1['startTime']:
                      mergedFound = True
                      v['hdln'] = ''

            if mergedFound:
                v['hdln'] = ''
            else:
                v['hdln'] = self._vtecDef.hazards[phensig]['hdln']

    def _vtecTimeStr(self, t):
        '''Returns a formatted vtec/htec time string.

        Keyword Arguments:
        t -- time value in seconds since epoch. If None, then return
          the 'ongoing' or 'until further notice' string.

        Returns the formatted string.
        ''' 
        if t and not self.untilFurtherNotice(t):
            return time.strftime('%y%m%dT%H%MZ', time.gmtime(t))
        else:
            return '000000T0000Z'

    def _isUpgrade(self, proposedRec, activeRec):
        '''Determines if we have an upgrade hazard situation.

        Keyword Arguments:
        proposedRec -- hazard record in the vtecRecord dictionary format.
        activeRec -- active record in the vtecRecord dictionary format.

        Returns True if proposedRec is an upgrade to activeRec.
        '''

        if proposedRec['act'] not in ['CON', 'EXT']:
            values = self._vtecDef.upgradeDef.get(proposedRec['key'], [])
            return activeRec['key'] in values
        else:
            return False   #not an upgrade


    def _isDowngrade(self, proposedRec, activeRec):
        '''Determines if we have an downgrade hazard situation.

        Keyword Arguments:
        proposedRec -- hazard record in the vtecRecord dictionary format.
        activeRec -- active record in the vtecRecord dictionary format.

        Returns True if proposedRec is an downgrade from activeRec.
        '''

        values = self._vtecDef.downgradeDef.get(proposedRec['key'], [])
        if activeRec['key'] in values:
            return True
        else:
            return False

    def _checkForMultipleSegsInSameID(self, pTable):
        '''Checks for records with the same phen/sig for the same geographical
        area (id). Eliminates the records with the lower segment number with
        same times.  Combines records with multiple segment numbers with 
        different times. Result is only to have 1 record per ID for phen/sig.

        Keyword Arguments:
        pTable -- list of dictionaries in the vtecRecord format.

        Returns the modified vtecRecord table.
        '''

        #step 1: reorganize the proposed table by zone, then by phen/sig.
        #dict of zones, then dict of phensigs, value is list of records.
        #Also create dictionary of originally max segment numbers for phen/sig.
        orgHaz = {}
        orgMaxSeg = {}  #key:phensig, value: max seg number
        for p in pTable:
            phensig = (p['phen'], p['sig'])
            id = p['id']
            if orgHaz.has_key(id):
                psOrgHaz = orgHaz[id]
                if psOrgHaz.has_key(phensig):
                    records = psOrgHaz[phensig]
                    records.append(p)
                    orgHaz[id][phensig] = records
                else:
                    orgHaz[id][phensig] = [p]
            else:
                orgHaz[id] = {phensig: [p]}

            # tally the original max segment number per phen/sig
            if orgMaxSeg.has_key(phensig):
               orgMaxSeg[phensig] = max(p['seg'], orgMaxSeg[phensig])
            else:
               orgMaxSeg[phensig] = p['seg']
 

        #step 2: Check for multiple records for phensig and zone.
        #Mark records that can be combined (adjacent/overlap).
        for zone in orgHaz.keys():
            for phensig in orgHaz[zone].keys():
                records = orgHaz[zone][phensig]
                # if only 1 record, we have nothing to do
                if len(records) == 1:
                    continue
                records.sort(self._hazardSortSTET)

                #find adjacent/overlapping, mark them as record number in
                #the dict entry 'rn', track overall tr in trDict (key is 'rn')
                trDict = {}
                for x in xrange(len(records)):
                    xtr = (records[x]['startTime'], records[x]['endTime'])

                    #search for adjacent/overlapping
                    for y in xrange(x+1, len(records)):
                        ytr = (records[y]['startTime'], records[y]['endTime'])
                        rny = records[y].get('rn', None)
                        if rny is None and (self._isAdjacent(xtr, ytr) or \
                          self._overlaps(xtr, ytr)):
                            rnx = records[x].get('rn', x)
                            records[y]['rn'] = rnx  #overlaps/adjacent,reuse rn
                            records[x]['rn'] = rnx  #assign to orig to match
                            if trDict.has_key(rnx):
                                trDict[rnx] = self._combineTR(ytr,trDict[rnx])
                            else:
                                trDict[rnx] = self._combineTR(xtr,ytr)

                maxSN = self._maxSegNumber(orgHaz, phensig) #max seg num

                #now assign new segment numbers, reassign starting/ending
                #times for the adjacent/overlaps, delete the temp markers
                for x in xrange(len(records)):
                    rnx = records[x].get('rn', None)
                    if rnx is not None:
                        records[x]['seg'] = maxSN + rnx + 1
                        records[x]['startTime'] = trDict[rnx][0]
                        records[x]['endTime'] = trDict[rnx][1]
                        records[x]['phensig'] = records[x]['phen'] + '.' + \
                          records[x]['sig'] + ':' + `records[x]['seg']`
                        del records[x]['rn']

                #now eliminate records duplicate records
                newrecs = []
                for rec in records:
                    if rec not in newrecs:
                        newrecs.append(rec)
                orgHaz[zone][phensig] = newrecs

        #step 3: Expand back out to list
        updatedList = []
        for zone in orgHaz.keys():
            for phensig in orgHaz[zone].keys():
                records = orgHaz[zone][phensig]
                for r in records:
                    updatedList.append(r)

        #step 4: Combine new segments if possible. We can tell we have
        #generated new segments based on the orgMaxSeg dictionary. We assign
        #them the same segments.
        compare = ['startTime','endTime','phen','sig']
        for x in xrange(len(updatedList)):
            p = updatedList[x]
            phensig = (p['phen'], p['sig'])
            if orgMaxSeg.has_key(phensig):
                orgMax = orgMaxSeg[phensig]
                if p['seg'] > orgMax:         #must be generated segment numb

                    #find matching records and assign all the same seg#
                    #and key
                    for y in xrange(x+1, len(updatedList)):
                        p1 = updatedList[y]
                        if self.vtecRecordCompare(p, p1, compare) and \
                          p1['seg'] > orgMax:
                            p1['seg'] = p['seg']
                            p1['phensig'] = p1['phen'] + '.' + p1['sig'] + \
                              ':' + `p1['seg']`
                              
        #step 5: Eliminate duplicate entries
        finalList = []
        for p in updatedList:
            if p not in finalList:
                finalList.append(p)

        return finalList

    # sort function: hazard records by starting time, then ending time
    def _hazardSortSTET(self, r1, r2):
        '''Sort function by start and then ending time.

        Keyword Arguments:
        r1 -- hazard record in the vtecRecords format.
        r2 -- hazard record in the vtecRecords format.

        Returns -1, 0, or 1 depending upon the starting and ending time values.
        '''
        if r1['startTime'] < r2['startTime']:
            return -1
        elif r1['startTime'] > r2['startTime']:
            return 1
        else:
            if r1['endTime'] < r2['endTime']:
                return -1
            elif r1['endTime'] > r2['endTime']:
                return 1
            else:
                return 0

    def _maxSegNumber(self, orgHaz, phensig):
        '''Calculates the maximum segment number.

        Keyword Arguments:
        orgHaz -- original hazard records
        phensig -- phen/sig hazard

        Returns max segment number for zone, phen/sig directory
          (support routine).
        '''
        maxSegNumber = 0
        for zone in orgHaz.keys():
            if orgHaz[zone].has_key(phensig):
                entries = orgHaz[zone][phensig]
                for e in entries:
                    maxSegNumber = max(maxSegNumber, e['seg'])
        return maxSegNumber

    def _checkValidETNcw(self, pTable):
        '''check for valid etns for all national center products.'''
        for p in pTable:
            if (p['phen'],p['sig']) in self._ncKeys and p['officeid'] != 'PGUM':
                try:
                    a = int(p['etn'])
                except:
                    raise Exception, '\n\n' + errorLine + '\n' +\
                      'ABORTING: Found National Hazard ' + \
                      'with no ETN in grids. \n' + self.printActiveTable(p) + \
                      ' Fix your grids by adding watch/storm number.' + \
                      '\nFor tropical hazards, an override to MakeHazard' +\
                      '\n is likely to blame.\n' + errorLine

    # check for valid ETN/Actions in the analyzed table. Cannot have
    # a split ETN where one part of ongoing/NEW, and the other part
    # is being dropped (e.g., CAN, UPG). pTable is the analyzed active table.
    def _checkValidETNsActions(self, pTable):
        '''Check for valid ETN/Actions in the analyzed table. Cannot have
        a split ETN where one part of ongoing/NEW, and the other part
        is being dropped (e.g., CAN, UPG). pTable is the analyzed active table.

        Keyword Arguments:
        pTable -- list of hazard records in the vtecRecords format.

        '''
        byZones = self._organizeByZone(pTable)  
        compare = ['etn','phen','sig']
        errorLine = '**************************************************\n'
        currentYear = time.gmtime(self._time)[0]
        for key in byZones:
            for v in byZones[key]:
                if (v['phen'], v['sig']) not in self._ncKeys:
                    continue   #only interested in checking national keys
                if v['act'] in ['EXP','UPG','CAN']:
                    hissueTime = v.get('issueTime', 0)
                    hissueYear = time.gmtime(hissueTime)[0] #issueYear
                    for v1 in byZones[key]:
                        if self.vtecRecordCompare(v, v1, compare) and \
                          v1['act'] in ['NEW','CON','EXA','EXT','EXB'] and \
                          currentYear == hissueYear:
                            raise Exception, '\n\n' + errorLine + '\n' +\
                             'ABORTING: Found VTEC Error'\
                             ' with same ETN, same hazard, conflicting '\
                             'actions.\n' + self.printVtecRecords(v) + \
                             self.printVtecRecords(v1) + '\n' + \
                             'Fix, if convective watch, by coordinating '\
                             'with SPC. Otherwise serious software error.\n'\
                             'Cannot have new hazard with same ETN as one '\
                             'that is no longer in effect (EXP, UPG, CAN).'\
                             '\n' + errorLine

    def _removeOverdueEXPs(self, pTable):
        '''Remove EXP actions that are xxmin past the end of event
        The records were kept for conflict resolution for national events.

        Keyword Arguments:
        pTable -- list of hazard records in the vtecRecords format.

        Returns the filtered list of hazard records.
        '''
        newTable = []
        for p in pTable:
            timeLimit = self._getExpirationLimits(p['key']).afterMinutes
            #if p['act'] != 'EXP' or self._time - p['endTime'] <= timeLimit*60*1000:
            if p['act'] != 'EXP' or self._time - p['endTime'] <= timeLimit*60:
                newTable.append(p)
        return newTable

    def _checkETNdups(self, pTable):
        '''Ensure we don't have two vtecs with same action and etns. Switch
        second one to NEW.

        Keyword Arguments:
        pTable -- list of hazard records in the vtecRecords format.

        Returns the modified list of hazard records.
        '''
        keyetnmax = {}
        compare = ['etn', 'phen', 'sig', 'id']
        compare2 = ['phen', 'sig']
        for p in pTable:
            #look for all vtecRecords to get max etn for each phen/sig
            vteckey = p['phen'] + p['sig']
            if not keyetnmax.has_key(vteckey):
                etn_max = 0
                for e in pTable:
                    if self.vtecRecordCompare(p, e, compare2) and \
                       e['etn'] > etn_max:
                        etn_max = e['etn']
                keyetnmax[vteckey]= etn_max

        assigned = {}
        for p in pTable:
            #only look for EXT, EXA, EXB vtecRecords
            if p['act'] in ['NEW', 'EXP', 'UPG', 'CAN', 'CON']:
                continue
            vteckey = p['phen'] + p['sig']

            for p1 in pTable:
                #check for matching id,etn,phen,sig,act combinations, these
                #are the ones that need to be reassigned. 
                if self.vtecRecordCompare(p, p1, compare) and \
                  p['startTime'] > p1['endTime']:
                    #found a newer record that needs to be reassigned
                    #see if we have already reassigned one that overlaps in time
                    #  phensig startend etn   doublenested dictionary
                    akey = p['phen'] + p['sig']
                    tr = (p['startTime'], p['endTime'])
                    trs = assigned.get(akey, {})
                    etna = None
                    for tre in trs.keys():
                        if self._overlaps(tr, tre):
                            etna = trs[tre]  #get previously reassigned
                            #update dictionary if time overlapped
                            trComb = self._combineTR(tr, tre)
                            if tr != trComb:
                                del trs[tre]
                                trs[trComb] = etna
                                assigned[akey] = trs
                            break

                    if etna is not None:
                        p['act'] = 'NEW'
                        p['etn'] = etna

                    else:
                        #take the newest record and assign new and give new ETN
                        p['act'] = 'NEW'
                        p['etn'] = keyetnmax[vteckey] + 1
                        trs[tr] = p['etn']   #new etn assigned
                        assigned[akey] = trs  #put back into dictionary
                        keyetnmax[vteckey]= p['etn']  #updated for new assign

    def __warnETNduplication(self, pTable):
        # Check should only operate on applicable VTEC products.
        if self._pil not in \
                ['CFW', 'FFA', 'MWW', 'NPW', 'RFW', 'WSW']:
            return

        dups = []
        byZones = self._organizeByZone(pTable)  
        for id, hazards in byZones.iteritems():
            visited = []
            for p in hazards:
                year = time.gmtime(p['issueTime'])[0]
                key = p['phen'], p['sig'], p['etn'], year
                if key in visited:
                    estr = '%s.%s:%d,%d' % key
                    if estr not in dups:
                        dups.append(estr)
                else:
                    visited.append(key)

        if len(dups) > 0:
            errorLine = '\n******************************************************\n'
            LogStream.logProblem('Illegal ETN duplication is found for:\n', \
                                 dups, errorLine)

            # Throw exception
            msg = 'The formatted %s product contains a duplicate ETN.\n'\
                  'Please transmit the product and then open a trouble ticket with the NCF.'\
                  % self._pil
            raise Exception(msg)

    def _addUpgradeDowngradeRec(self, proposedTable):
        '''Add upgrade/downgrade information for upgrades and downgrades.

        Keyword Arguments:
        pTable -- list of hazard records in the vtecRecords format.

        Returns the modified list of hazard records.
        '''

        compare = ['id', 'officeid']
        fields = ['etn', 'startTime', 'endTime', 'key', 'phen', 'sig', 'seg', 'act',
          'subtype']

        for rec in proposedTable:
            if rec['act'] == 'NEW':
                for checkR in proposedTable:
                    if checkR['act'] in ['CAN', 'UPG']:
                        if self._vtecRecordsOverlap(checkR, rec) and \
                           self.vtecRecordCompare(checkR, rec, compare):
                            if self._isDowngrade(rec, checkR):
                               rec['downgradeFrom'] = \
                                 self._copyFields(checkR, fields)
                            elif self._isUpgrade(rec, checkR):  
                               rec['upgradeFrom'] = \
                                 self._copyFields(checkR, fields)

        return proposedTable

        
    
    def _calcAnalyzedTable(self, pTable, activeVtecRecords, endedEventIDs,
      combinableSegments):
        '''Main routine to calculate the analyzed table from the proposed
        and active vtecRecords.

        Keyword Arguments:
        pTable -- list of proposed vtecRecords in the vtecRecord format.
        activeVtecRecords -- list of already issued vtecRecords.
        endedEventIDs -- list of eventIDs that are marked 'ended'.
        combinableSegments -- True if hazards can be combined into the
          same segment; otherwise False if the hazards must be maintained as
          separate segments.

        Returns the analyzed list of vtecRecords representing the new
        set of records and hazards.
        '''

        # set issueTime to current time. We use issueTime to indicate the
        # initial issued time for the hazard.  We start with our current time,
        # and as we match records with the active table we copy that time
        # to this field.   This is just a temporary field.
        for rec in pTable:
            rec['issueTime'] = self._time
       
        # special code for HY.S
        pTable = self._handleHYS(pTable)
        LogStream.logDebug('Analyzed Table -- After handleHYS:', 
          self.printVtecRecords(pTable, combine=True))

        # convert active table EXP still in effect to CON
        activeVtecRecords = self._convertEXPtoCON(activeVtecRecords)
        LogStream.logDebug('Analyzed Table -- After convertEXPtoCON:', 
          self.printVtecRecords(pTable, combine=True))

        if (combinableSegments):
            # Drop multiple segments for same phen/sig in same 'id'
            pTable = self._checkForMultipleSegsInSameID(pTable)
            LogStream.logDebug('Analyzed Table -- After checkForMultipleSegsInSameID:', 
              self.printVtecRecords(pTable, combine=True))
       
            # Check for Merged Events
            pTable, activeVtecRecords = self._checkForMergedRecords(pTable,
              activeVtecRecords)
            LogStream.logDebug('Analyzed Table -- After checkForMergedEvents:', 
              self.printVtecRecords(pTable, combine=True))

        # Check for CON and EXT actions
        pTable = self._checkForCONEXT(pTable, activeVtecRecords)
        LogStream.logDebug('Analyzed Table -- After checkForCONEXT:', 
          self.printVtecRecords(pTable, combine=True))

        # Check for CAN, EXP, and UPG
        pTable = self._checkForCANEXPUPG(pTable, activeVtecRecords, endedEventIDs)
        LogStream.logDebug('Analyzed Table -- After checkForCANEXPUPG:', 
          self.printVtecRecords(pTable, combine=True))

# TODO: Reinstate if necessary for interoperability
# Check for EXA/EXB, applicable for GHG products
#         phensig = self._hazardEvents[0].getPhenomenon() + '.' + self._hazardEvents[0].getSignificance()
#         if phensig in self._hazardsConflictDict:

        pTable = self._checkForEXAEXB(pTable, activeVtecRecords)
        LogStream.logDebug('Analyzed Table -- After checkForEXAEXB:', 
            self.printVtecRecords(pTable, combine=True))

        # Assign NEW to remaining records
        pTable = self._checkForNEW(pTable, activeVtecRecords, combinableSegments)
        LogStream.logDebug('Analyzed Table -- After checkForNEW:', 
          self.printVtecRecords(pTable, combine=True))


        # Check for upgrades and downgrades, add records if needed
        if (combinableSegments):
            pTable = self._addUpgradeDowngradeRec(pTable)
            LogStream.logDebug('Analyzed Table -- After addUpgradeDowngradeRec:', 
              self.printVtecRecords(pTable, combine=True))

        # Convert ongoing events about ready to expire (still in the
        # proposed grids) to switch from CON to EXP
        pTable = self._addEXPCodes(pTable)
        LogStream.logDebug('Analyzed Table -- After addEXPCodes:', 
          self.printVtecRecords(pTable, combine=True))

        if (combinableSegments):
            # Eliminate any EXPs if other events (same phen/sig) in effect
            # at present time.
            pTable = self._removeEXPWithOngoingCodes(pTable)
            LogStream.logDebug('Analyzed Table -- After removeEXPWithOngoingCodes:', 
              self.printVtecRecords(pTable, combine=True))

            # Ensure valid ETN/Actions - no EXP/CAN with valid same ETN
            # for national events
            self._checkValidETNsActions(pTable)
            LogStream.logDebug('Analyzed Table -- After checkValidETNsActions:',
              self.printVtecRecords(pTable, combine=True))

        # Remove EXPs that are 'xx' mins past the end of events
        pTable = self._removeOverdueEXPs(pTable)
        LogStream.logDebug('Analyzed Table -- After removeOverdueEXPs:',
          self.printVtecRecords(pTable, combine=True))

        # Ensure that there are not ETN dups in the same segment w/diff
        # action codes
        self._checkETNdups(pTable)
        LogStream.logDebug('Analyzed Table -- After checkETNdups:',
          self.printVtecRecords(pTable, combine=True))

        # Warn user about ETN duplication if any
        self.__warnETNduplication(pTable)           

        # Ensure that starting times are not before now, and that starting
        # times are not after the event ends.
        pTable = self._adjustStartTimesBasedOnNow(pTable)

        # Complete the VTEC Strings
        self._addVTECStrings(pTable)
        LogStream.logDebug('Analyzed Table -- After addVTECStrings:', 
          self.printVtecRecords(pTable, combine=True))
    
        # Complete the H-VTEC Strings
        self._addHVTECStrings(pTable)
        LogStream.logDebug('Analyzed Table -- After addHVTECStrings:', 
          self.printVtecRecords(pTable, combine=True))

        #add in hdln entries if they are missing
        self._addHeadlinesIfMissing(pTable)
        LogStream.logDebug('Analyzed Table -- After addHeadlinesIfMissing:', 
          self.printVtecRecords(pTable, combine=True))        

        # Ensure that all SV.A and TO.A have valid ETNs
        if (combinableSegments):
            self._checkValidETNcw(pTable)

        # Return pTable, which is essentially analyzedTable at this point
        LogStream.logVerbose('Analyzed Records: ', self.printVtecRecords(pTable,
         combine=True))
        return pTable

    # is marine zone?
    def _isMarineZone(self, id):
        if id[0:2] in self._marineZonesPrefix:
            return True;
        else:
            return False;

    def untilFurtherNotice(self, time_sec):
        if time_sec >= VTECConstants.UFN_TIME_VALUE_SECS:
            return True
        else:
            return False

    
    def flush(self):
        ''' Flush the print buffer '''
        os.sys.__stdout__.flush()
