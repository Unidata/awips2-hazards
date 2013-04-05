#------------------------------------------------------------------
#  VTEC Engine
#------------------------------------------------------------------
# This is the object that takes a proposed set of events, a list of
# the currently active events, and calculates the proper VTEC to
# determine the segments, VTEC codes, HVTEC codes, and hazards.
#------------------------------------------------------------------
import cPickle, os, types, string, copy
import sys, gzip, time
import collections
from VTECTableUtil import VTECTableUtil
import logUtilities.Logger as LogStream

# Define several named tuples for cleaner code
VTECDefinitions = collections.namedtuple('VTECDefinitions',
  'hazards upgradeDef downgradeDef')

EndedEvents = collections.namedtuple('EndedEvents', 'phen sig eventID areas')

ZCSort = collections.namedtuple('ZCSort', 'hazards zoneCombo')


class VTECEngine(VTECTableUtil):

    def __init__(self, productCategory, siteID4, eventDicts,
      vtecRecords, vtecDefinitions, allowedHazards,
      vtecMode, creationTime=None, limitGeoZones=None):

        '''Constructor for VTEC Engine
        Once instantiated, it will run the calculations.  The output may
        be accessed through different functions.

        Keyword Arguments:
        productCategory -- identifier for the product, which must match a 
          key in the ProductGeneratorTable.
        siteID4 -- identifier for the site, such as KBOU
        eventDicts -- list of event dictionaries containing the hazard records.
          All times within the records are in units of milliseconds since epoch
          of Jan 1 1970 at 0000Z.
        vtecDefinitions -- named tuple consisting of the HazardTypes dictionary,
          upgrade dictionary, and downgrade dictionary.
        allowedHazards - priority ordered list of (key, category) of the hazard
          types that are allowed in this product.
        vtecMode -- 'O' for operational product, 'T' for test product,
          'E' for experimental product, 'X' for Experimental VTEC in an
          operational product.
        creationTime -- time the engine is run, a.k.a. issue time.  Units of
          milliseconds since epoch (Jan 1 1970 00:00Z)
        limitGeoZones -- A list of zones used to limit the vtec logic.  This is
          only used in places where there are multiple products for the same
          hazard types issued at an office.  Example: PAFG (Fairbanks, AK).
        '''

        VTECTableUtil.__init__(self)
        # save data
        self._pil = productCategory
        self._siteID4 = siteID4
        self._spcSiteID4 = "KWNS"
        self._vtecDef = vtecDefinitions
        self._vtecMode = vtecMode
        self._etnCache = {}

        # determine appropriate VTEC lifecycle, based on event dicts,
        # and the hazards table.  All hazard records provided must equate
        # to the same segmentation technique.
        combinableSegments = self._determineSegmentationBehavior(eventDicts)

        # determine the type of data (geoType).  All hazard records provided
        # must equate to using the same geoType ('area' or 'point')
        self._geoType = self._determineGeoType(eventDicts)

        # filter the allowedHazards down to just those appropriate, based on
        # the phen.sig combinableSegments.  This will let us eliminate those
        # hazards that don't apply for the segment behavior defined.
        self._allowedHazards = \
              [ah for ah in allowedHazards if vtecDefinitions.hazards[ah[0]]['combinableSegments'] == combinableSegments]
        try:
            self._allowedHazards = \
              [ah for ah in allowedHazards if vtecDefinitions.hazards[ah[0]]['combinableSegments'] == combinableSegments]
        except KeyError:
            raise Exception("Phen/Sig in AllowedHazards but not HazardTypes"
              ": {k}".format(k=ah[0]))

        # list of marine products, required for special VTEC sort order
        self._marineProds = ["MWW"]

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

        # list of phen/sig from national centers and "until further notice"
        self._tpcKeys = [('HU','A'), ('HU','S'), ('HU','W'), ('TR','A'), 
          ('TR','W')]
        self._tpcBaseETN = 1000
        self._ncKeys = [('TO','A'), ('SV','A'), ('HU','A'), ('HU','W'),
          ('TR','A'), ('TR','W')]
        self._ufnKeys = [('HU','A'), ('HU','S'), ('HU','W'), ('TR','A'),
          ('TR','W'), ('TY','A'), ('TY','W')]

        self._marineZonesPrefix = ["AM", "GM", "PZ", "PK", "PH", "PM", "AN",
          "PS", "SL"]   #list of zone name prefix that are marine zones

        # determine creation time, allows for non-current execution
        # creation time in milliseconds since epoch
        if creationTime is not None:
            self._time = creationTime
        else:
            self._time = time.time() * 1000 #now time
        self._time = (int(self._time) / 60000) * 60000  #truncated to minute

        # sample, and merge vtec codes
        proposed, zonesP, limitEventIDs, endedEventIDs = \
          self._getProposedTable(eventDicts, limitGeoZones,
          combinableSegments)

        self._allGEOVtecRecords, self._vtecRecords, zonesA = \
          self._getCurrentTable(vtecRecords, limitGeoZones, limitEventIDs)

        self._analyzedTable = self._calcAnalyzedTable(proposed,
          self._vtecRecords, endedEventIDs, combinableSegments)

        #print "analyzed", self._analyzedTable
        # calculate the set of all zones in the proposed and vtec tables
        zoneList = list(zonesP | zonesA)

        #print "zoneList", zoneList
        # determine the segments (unsorted). Different algorithms based on
        # whether the hazards/segments are combinable.
        hazardCombinations = self._combineZones(
          self.consolidatedAnalyzedTable(), combinableSegments, zoneList)
        
        #print "Combinations", hazardCombinations

        # calculate the segments, which does the sorting
        self._segments = self._sortSegments(hazardCombinations,
          combinableSegments)

        #print "Segments", self._segments

        # calc the VTEC strings and hazardList dictionaries. Important
        # to call calcHazardList before calcVTECString. 
        self._hazardList = {}
        for seg in self._segments:
            self._hazardList[seg] = self._calcHazardList(seg,
              combinableSegments)

        self._vtecStrings = {}
        for seg in self._segments:
            self._vtecStrings[seg] = self._calcVTECString(seg)


#-----------------------------------------------------------------
# VTEC Engine Accessors
#-----------------------------------------------------------------


    def vtecRecords(self):
        '''Returns the filtered list of vtec records as a list of dicts'''
        return self.consolidateByID(self._vtecRecords)

    def analyzedTable(self):
        '''Returns the analyzed table as a list of dictionaries'''
        return self._analyzedTable

    def consolidatedAnalyzedTable(self):
        '''Returns the consolidated version of the analyzed table. Records that
        are identical other than geoArea are combined.  The 'geoId' field is 
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

    def getHazardList(self, segmentArea):
        '''Returns a list of dictionaries that represent the hazards in the
        provided segment area.  The segment area must be
        one of the values returned from getSegments().
        '''
        return self._hazardList.get(segmentArea)

#-----------------------------------------------------------------
# VTEC Engine Implementation Routines
#-----------------------------------------------------------------

    def _getExpirationLimits(self, hazardType):
        '''Returns the expiration time limits for the given hazard type

        Keyword Arguments:
        hazardType -- hazard key as found in the HazardTypes dictionary,
          such as "TO.W".

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
          such as "TO.W".

        Returns True or False.
        '''
        return self._vtecDef.hazards[hazardType]['allowAreaChange']

    def _allowTimeChange(self, hazardType):
        '''Returns a bool indicating whether EXTs are allowed for the
        hazard type

        Keyword Arguments:
        hazardType -- hazard key as found in the HazardTypes dictionary,
          such as "TO.W".

        Returns True or False
        '''
        return self._vtecDef.hazards[hazardType]['allowTimeChange']

    def _determineSegmentationBehavior(self, eventDicts):
        '''Determine appropriate VTEC segmentation behavior, based on
        event dicts and the hazards table.  Ensures that all records in the
        eventDicts use the same segmentation behavior or an exception is
        thrown.

        Keyword Arguments:
        eventDicts -- list of hazard event dictionaries. 

        Returns True if the segments/hazards are combinable.
        '''
        try:
            hazardTypes = set([e['type'] for e in eventDicts])
        except KeyError:
            raise Exception("'type' field missing in eventDict")

        try:
            combinableSegments = set([self._vtecDef.hazards[ht]['combinableSegments']
          for ht in hazardTypes])
        except KeyError:
            raise Exception("Hazard type '{k}' not in HazardTypes".format(
              k=ht))

        # If we can't get any information, assume segments are combinable
        if combinableSegments == set():
            combinableSegments = [True]

        # Ensure that we only have 1 combinable segments states
        if len(combinableSegments) != 1:
            s = "Multiple combinableSegment states detected {s}".format(
              s=combinableSegments)
            raise Exception(s)
        else:
            # take the 1st one, which is the only one
            combinableSegments = list(combinableSegments)[0]
        return combinableSegments

    def _determineGeoType(self, eventDicts):
        '''Determines the geoType for the VTECEngine run.

        Keyword Arguments:
        eventDicts - list of event hazard dictionaries.

        Returns the geoType for this engine run.  Throws exception if not
        determined.
        '''

        try:
            geoTypes = set([e['geoType'] for e in eventDicts])
        except KeyError:
            raise Exception("'geoType' missing from eventDict record")

        # extract the 1st one out, otherwise choose a default.
        # Verify that multiple geoTypes are not present.
        if len(geoTypes) == 1:
            return list(geoTypes)[0]
        elif len(geoTypes) == 0:
            return 'area'  # if no eventDicts, then assume 'area'
        else:
            raise Exception("Multiple geoTypes detected {}".format(geoTypes))


    def _calcHazardList(self, segment, combinableSegments):
        '''Calculates the hazard list for the segment.

        Keyword Arguments:
        segment -- Tuple containing the set of geographical ids, and the
          set of eventIDs, representing one segment.
        combinableSegments -- True if hazards can be combined into segments,
          False otherwise.

        Returns a list of dictionaries that represent the vtecRecords
        containing hazards in the "segment".
        '''

        if combinableSegments:
            hazards = [a for a in self.consolidatedAnalyzedTable() if set(
              a['geoId']) & set(segment[0])]   # hazards that overlap segment

            # some of the hazard records may have zones that include other
            # zones and segments.  We want to remove them here.
            hazards1 = copy.deepcopy(hazards)
            for h in hazards1:
                h['geoId'] = list(set(segment[0]) & set(h['geoId']))
            return hazards1

        else:
            # non-combinable segments
            hazards = [a for a in self.consolidatedAnalyzedTable() if set(
              a['geoId']) == set(segment[0]) and a['eventID'] in segment[1]]
            return hazards

    def _calcVTECString(self, segment):
        '''Calculates the P-VTEC and H-VTEC strings for the segment.

        Keyword Arguments:
        segment -- Tuple containing the set of geographical ids, and the
          set of eventIDs, representing one segment.

        Returns a string containing the p-vtec and h-vtec strings for the
        given forecast segment.
        '''

        # get the list of hazards for this segment
        hazards = self.getHazardList(segment)   #could sort in here

        # sort the list of hazards depending on the type of product
        if self._pil in self._marineProds:   # it's a marine product
            hazards.sort(self._marineHazardsSort)
        else:   # non-marine product
            hazards.sort(self._hazardsSort)

        # hazards need upgrade records to be paired up
        hazards = self._pairUpgradeRecords(hazards)
            
        # get VTEC strings and VTEC records
        vtecStrings = []
        for i, h in enumerate(hazards):
            vtecStrings.append(h['vtecstr'])

            # is there an h-vtec string?
            if h['hvtecstr']:

                # H-VTEC always included on non-CAN actions.
                if h['act'] != 'CAN':
                    vtecStrings.append(h['hvtecstr'])

                # H-VTEC not included on CAN for FA.A, FF.A exchange with paired
                # records: CAN/NEW, CAN/EXA, CAN/EXB
                elif i + 1 < len(hazards) - 1:   # is there a next record?
                    h1 = hazards[i + 1]
                    if h1['act'] not in ['NEW', 'EXA', 'EXB'] or \
                      not ((h1['key'] == 'FA.A' and h['key'] == 'FF.A') or \
                      (h1['key'] == 'FF.A' and h['key'] == 'FA.A')) :
                        vtecStrings.append(h['hvtecstr'])

                # no next record, so always output h-vtec
                else:
                    vtecStrings.append(h['hvtecstr'])

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

        allHazards = self.consolidatedAnalyzedTable()

        # reformat to prepare for sorting
        zcsort = []
        for zoneCombo in hazardCombinations:
            # get all hazards for this zoneCombo
            if combinableSegments:
                hazards = [a for a in allHazards \
                  if set(a['geoId']) & zoneCombo[0]]


                # change the hazard record so it only has geoIds for this group
                hazards = copy.deepcopy(hazards)
                for h in hazards:
                    h['geoId'] = list(set(h['geoId']) & zoneCombo[0])

            # non-combinable segments
            else:
                hazards = [a for a in allHazards if set(a['geoId']) == zoneCombo[0]
                  and a['eventID'] in zoneCombo[1]] 

            hazards.sort(self._hazardsGroupSort)   # normal sorting order

            zc = ZCSort(hazards, zoneCombo)
            zcsort.append(zc)
        zcsort.sort(self._comboSort)

        # extract out the results
        return [a.zoneCombo for a in zcsort]

    def _comboSort(self, a, b):
        ''' Comparison routine for two eventDict records.

        Keyword Arguments:
        a -- combo 1 to compare
        b -- combo 2 to compare

        Returns -1, 0, +1 based on the comparisons.
        '''

        ahaz = a.hazards
        bhaz = b.hazards
        commonNum = min(len(ahaz), len(bhaz))
        for idx in range(commonNum):
            hs = self._hazardsGroupSort(ahaz[idx], bhaz[idx])
            if hs != 0:
                return hs   # not equal

        # first common ones are equal, if we got here, then we send either
        # 1 or -1 depending which list is longer
        if len(ahaz) > len(bhaz):
            return -1
        elif len(ahaz) < len(bhaz):
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

    def _hazardsGroupSort(self, a, b):
        ''' Comparison routine for two eventDict records.

        Keyword Arguments:
        a -- object A for compare
        b -- object B for compare

        Returns -1, 0, or 1 depending upon the sort criteria for a and b
        '''
    
        # check action code
        actionCodeOrder = ["CAN", "EXP", "UPG", "NEW", "EXB", "EXA",
                           "EXT", "CON", "ROU"]
        try:
            aIndex = actionCodeOrder.index(a['act'])
            bIndex = actionCodeOrder.index(b['act'])
        except ValueError:
            raise Exception("Invalid action code in hazard in "
              "_hazardsGroupSort:\n{a}\n{b}".format(a=self.printEntry(a),
              b=self.printEntry(b)))

        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check sig
        sigOrder = ["W", "Y", "A", "S", "F"]
        try:
            aIndex = sigOrder.index(a['sig'])
            bIndex = sigOrder.index(b['sig'])
        except ValueError:
            raise Exception("Invalid sig code in hazard in "
              "_hazardsGroupSort:\n{a}\n{b}".format(a=self.printEntry(a),
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

    def _hazardsSort(self, a, b):
        '''Compares two hazard records.

        Keyword Arguments:
        a -- object A for compare
        b -- object B for compare

        Returns 1, 0, or -1 depending upon
        whether the first hazard is considered higher, equal, or lower
        priority when compared to the second as defined in the VTEC
        directive.
        '''

        # check action code
        actionCodeOrder = ["CAN", "EXP", "UPG", "NEW", "EXB", "EXA",
                           "EXT", "CON", "ROU"]
        try:
            aIndex = actionCodeOrder.index(a['act'])
            bIndex = actionCodeOrder.index(b['act'])
        except ValueError:
            raise Exception("Invalid action code in hazard in "
              "_hazardsSort:\n{a}\n{b}".format(a=self.printEntry(a),
              b=self.printEntry(b)))
        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check sig
        sigOrder = ["W", "Y", "A", "S", "F"]
        try:
            aIndex = sigOrder.index(a['sig'])
            bIndex = sigOrder.index(b['sig'])
        except ValueError:
            raise Exception("Invalid sig code in hazard in "
              "_hazardsSort:\n{a}\n{b}".format(a=self.printEntry(a),
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

        LogStream.logProblem("Hazards are identical in _hazardsSort",
          self.printVtecRecords([a, b]))
        return 0
    
    def _marineHazardsSort(self, a, b):
        '''Compares two hazard records.

        Keyword Arguments:
        a -- object A for compare
        b -- object B for compare

        Returns 1, 0, or -1 depending upon
        whether the first hazard is considered higher, equal, or lower
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
        actionCodeOrder = ["CAN", "EXP", "UPG", "NEW", "EXB", "EXA",
                           "EXT", "CON", "ROU"]
        try:
            aIndex = actionCodeOrder.index(a['act'])
            bIndex = actionCodeOrder.index(b['act'])
        except ValueError:
            raise Exception("Invalid action code in hazard in "
              "_hazardsSort:\n{a}\n{b}".format(a=self.printEntry(a),
              b=self.printEntry(b)))

        if aIndex > bIndex:
            return 1
        elif aIndex < bIndex:
            return -1

        # check sig
        sigOrder = ["W", "Y", "A", "S", "F"]
        try:
            aIndex = sigOrder.index(a['sig'])
            bIndex = sigOrder.index(b['sig'])
        except ValueError:
            raise Exception("Invalid sig code in hazard in "
              "_hazardsSort:\n{a}\n{b}".format(a=self.printEntry(a),
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
          "Marine Hazards are identical in _marineHazardsSort",
            self.printVtecRecords([a, b]))
        return 0

    def _pairUpgradeRecords(self, hazardsList):
        '''Moves items in the list around such that the upgrades and
        downgrades are sequential (UPG, NEW, (CAN, NEW), etc.

        Keyword Arguments:
        hazardsList -- list of dictionary records denoting the hazards. These
          are not consolidated by ID.
        '''

        compare = ['etn', 'key']

        # get the list of upgraded or downgraded records
        upDownList = []
        for h in hazardsList:
            if h.has_key('upgradeFrom') or h.has_key('downgradeFrom'):
                upDownList.append(h)

        # temporarily remove these guys from the hazardsList
        for upDown in upDownList:
            hazardsList.remove(upDown)

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
            for h in hazardsList:
                if self.hazardCompare(oldRec, h, compare):
                    # found a match
                    hazardsList.insert(hazardsList.index(h) + 1, upDown)
                    foundMatch = 1
                    break  # done with this pass through hazardsList

            if foundMatch == 0:
                LogStream.logProblem("Match not found for upgrade/downgrade.")

        return hazardsList
        
    #-----------------------------------------------------------------
    # The following set of functions are utility functions.
    #-----------------------------------------------------------------

    def _prepETNCache(self, proposedRecord):
        '''Prepares the etn cache.  Adds new entries to the etn cache,
        but doesn't figure out the etn values at this point.  Organizes the
        information by phen.sig, then maintains a list of start, end, etns,
        eventIDs, and geoIds'''

        phensig = (proposedRecord['phen'], proposedRecord['sig'])
        id = proposedRecord['geoId']
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
    
    def _assignNewETNs(self, vtecRecords):
        '''Assigns new etns to the etn cache. This is done after all requests
        for new etns have been made.

        Keyword Arguments:
        vtecRecords -- list of non-consolidated by id vtec records
          (dictionaries).

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
                      eventID1 == eventID2:

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
        #check active table for highest etn for this year
        presentyear = self.gmtime_fromMS(self._time)[0]
        etn_base = 0 
        
        for active in vtecRecords:
            # find only records with
            # 1. same phen and sig
            # 2. in the present year
            # and not from the national center
            activeyear = self.gmtime_fromMS(active['issueTime'])[0]
            phensig = (active['phen'],active['sig'])
            if active['phen'] == phen and active['sig'] == sig and \
              activeyear == presentyear:
                # find the max ETN...
                # 1. highest ETN period for non-national products (ncKey)
                # or
                # 2. highest ETN < 1000 for the national products (ncKey)
                #
                # Local WFOs do not assign these numbers, so they should have
                # numbers < 1000
                # Because at this time, TO and SV phen use numbers starting
                # at 0001. We will use the ufnKey instead which does not
                # include TO and SV.
                if active['etn'] > etn_base and phensig not in self._tpcKeys:
                    etn_base = active['etn']
                elif active['etn'] > etn_base and phensig in self._tpcKeys:
                    if self._siteID4 == 'PGUM':
                        # GUM uses their own ETNs regardless of hazard
                        etn_base = active['etn']
                    elif active['etn'] <= self._tpcBaseETN:  # is WFO etn
                        etn_base = active['etn']
                        
        LogStream.logDebug("HIGHEST ETN for ", phen, sig, etn_base)
        return etn_base

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
        raise Exception("ETN/Cache Issue. Could not find etn for {vr}.\n"
          "ETNcache is: {cache}".format(vr=self.printVtecRecords(pRecord),
          cache=self._etnCache))



    #-----------------------------------------------------------------
    # The following set of functions are used to recombining
    # records from the raw analyzed table to keep the geographic
    # groups together.
    #-----------------------------------------------------------------

    def _organizeByZone(self, hazardList):
        '''Returns a dictionary that is keyed on zonename, and contains a list
        of all hazards for that zone.

        Keyword Arguments:
        hazardList -- list of non-consolidated hazard records

        Returns dictionary with key being the geoId, and the values being
        a list of hazard records for that geoId.
        '''
        
        hazardsByZone = {}
        for h in hazardList:
            hazardsByZone.setdefault(h['geoId'], []).append(h)

        return hazardsByZone

    def _organizeByKey(self, hazardList):
        '''Returns a dictionary that is based on key, and contains a list
        of all hazards for each key value.

        Keyword Arguments:
        hazardList -- list of consolidated or non-consolidated hazard
          records.

        Returns dictionary with key being the phen/sig/subtype, and the
        values being a list of hazard records with that phen/sig/subtype.
        '''

        hazards = {}
        for h in hazardList:
            hazards.setdefault(h['key'], []).append(h)

        return hazards

    def _combineZones(self, hazards, combinableSegments, zoneList):
        '''Determines how to combine all of the hazards based on the
        combinableSegments flag and the list of zones.

        Keyword Arguments:
        hazards -- list of hazard records (dictionary) to determine how
          to combine.
        combinableSegments -- True if hazards can be combined into segments,
          otherwise False if only one hazard can be in a segment.
        zoneList -- A list of geographical identifiers that appear in
          the analyzed table and vtec records.

        Returns unsorted list of combinations represented as a list of
        tuples.  The tuples have a set of geographic identifiers and a
        set of eventIDs.
        '''

        if not zoneList:
            return frozenset([])   # no zone combo possible

        if not combinableSegments:
            outCombo = []   # start with an empty list
            for h in hazards:
                # ensure Z and C ids are kept separated
                idSets = [set([v for v in h['geoId'] if v[2] == 'Z']),
                  set([v for v in h['geoId'] if v[2] not in 'Z'])]
                for ids in idSets:
                    if ids and (ids, set([h['eventID']])) not in outCombo:
                        outCombo.append((ids, set([h['eventID']])))
        else:
            # start with a complete list
            outCombo = [set(zoneList)]
            for h in hazards:
                # ensure Z and C ids are kept separated
                idSets = [set([v for v in h['geoId'] if v[2] == 'Z']),
                  set([v for v in h['geoId'] if v[2] != 'Z'])]
                for ids in idSets:
                    if ids:
                        for i, c in enumerate(outCombo):
                            if c == ids:
                                break
                            common = c & ids
                            notCommon = c ^ common
                            if common and notCommon:
                                outCombo[i] = notCommon
                                outCombo.append(common) 

            # handle the event ids
            ev = [set([a['eventID'] for a in hazards if set(a['geoId']) & c])
              for c in outCombo]
            outCombo = zip(outCombo, ev)

        # delete any empty segments
        outCombo = [o for o in outCombo if len(o[1])]

        # convert the sets into frozensets so they are hashable
        out = [(frozenset(zones), frozenset(eids)) for zones, eids in outCombo]
        return out

    def _getProposedTable(self, eventDicts, limitGeoZones, combinableSegments):
        '''Calculates and returns the proposed vtec tables.

        Keyword Arguments:
        eventDicts -- list of dictionaries representing the proposed events.
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
        LogStream.logDebug("EventDicts: ", eventDicts)
        atable = self._convertEventsToVTECrecords(eventDicts)
        LogStream.logDebug("Proposed Table length: ", len(atable), atable)
        LogStream.logDebug("Sampled Proposed Table:\n", 
          self.printVtecRecords(atable, combine=True))
                
        # determine whether we want to limit the eventIDs for processing based
        # on the combinableSegments
        if combinableSegments:
            limitEventIDs = None
        else:
            limitEventIDs = set([a['eventID'] for a in atable])

        # Combine time entries
        atable = self._timeCombine(atable)
        LogStream.logDebug("Time Combine Proposed Table length: ", len(atable))
        LogStream.logDebug("Proposed Table after Time Combining:\n", 
          self.printVtecRecords(atable, combine=True))

        # remove hazards that don't apply to the product
        atable = self.filterAllowedHazards(atable)
        LogStream.logDebug("Proposed Table, after filterAllowedHazards:\n",
          self.printVtecRecords(atable, combine=True))

        # remove hazards in conflict (applies to GHG longFused only)
        if combinableSegments:
            atable = self.filterLowerPriorityHazards(atable)
            LogStream.logDebug("Proposed Table, after filterLowerPriorityHazards:\n",
              self.printVtecRecords(atable, combine=True))

        # remove hazards that aren't in the specified geography
        atable = self.filterGeoZones(atable, limitGeoZones)
        LogStream.logDebug("Proposed Table, after filterGeoZones:\n",
          self.printVtecRecords(atable, combine=True))
        
        # determine zones in use (set of all zones in the proposed table)
        zones = set([a['geoId'] for a in atable])
                
        # make a list of those events that have been marked as "ended"
       ## or has an ending time now or earlier than now
        endedEventIDs = set([a['eventID'] for a in atable
          if a.get('state') == 'ended'])

        # strip out all ended events from proposed
        atable = [a for a in atable if a.get('state') != 'ended']

        # clear the h-vtec and p-vtec strings
        for a in atable:
            a['vtecstr'] = ''
            a['hvtecstr'] = ''

        LogStream.logVerbose("Proposed Table:\n", self.printVtecRecords(atable,
          combine=True))
        LogStream.logVerbose("Zones=", zones)
        LogStream.logVerbose("limitEventIDs=", limitEventIDs)
        LogStream.logVerbose("endedEventIDs=", endedEventIDs)

        return atable, zones, limitEventIDs, endedEventIDs

    def _getCurrentTable(self, rawVtecRecords, limitGeoZones, 
      limitEventIDs=None):
        '''Calculates and returns the current hazard vtec tables.

        Keyword Arguments:
        rawVtecRecords -- list of dictionaries representing the current events.
          (Current events denote those records in the vtec database and may
          include some older records for etn calculations.)
        limitGeoZones -- None for no limits, otherwise a list of geographical
          descriptors which limit the hazards to be processed to just those
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
        LogStream.logDebug("Raw Active Table length: ", len(rawVtecRecords))
        LogStream.logDebug("Raw Active Table:\n", 
          self.printVtecRecords(rawVtecRecords, combine=True))

        # Perform site filtering on the active table.  We keep
        # our site and SPC.
        siteFilter = [self._siteID4, self._spcSiteID4]
        allGEOVtecRecords = [a for a in rawVtecRecords if a['officeid'] in siteFilter]
        LogStream.logDebug("Active Table: after site filter:\n",
          self.printVtecRecords(rawVtecRecords, combine=True))

        # further filtering on the active table
        actTable = self.filterTestMode(allGEOVtecRecords,
          testMode=self._vtecMode == "T")
        LogStream.logDebug("Active Table: after filter test mode:\n",
          self.printVtecRecords(rawVtecRecords, combine=True))
        actTable = self.filterAllowedHazards(actTable)
        LogStream.logDebug("Active Table: after filterAllowedHazards:\n",
          self.printVtecRecords(rawVtecRecords, combine=True))
        actTable = self.filterGeoZones(actTable, limitGeoZones)
        LogStream.logDebug("Active Table: after filterGeoZones:\n",
          self.printVtecRecords(rawVtecRecords, combine=True))
        actTable = self.filterEventIDs(actTable, limitEventIDs)
        LogStream.logDebug("Active Table: after filterEventIDs:\n",
          self.printVtecRecords(rawVtecRecords, combine=True))

        # determine zones in use
        zones = set([a['geoId'] for a in actTable])

        # delete upgrade/downgrade information in current table
        actTable = self._removeFieldFromTable(actTable, 'upgradeFrom')
        actTable = self._removeFieldFromTable(actTable, 'downgradeFrom')

        # eliminate the h-vtec and p-vtec strings
        for a in actTable:
            a['vtecstr'] = ''
            a['hvtecstr'] = ''
               
        LogStream.logDebug("Filtered Active Table length: ", len(actTable))
        LogStream.logDebug("Filtered Active Table:\n", 
          self.printVtecRecords(actTable, combine=True))

        return allGEOVtecRecords, actTable, zones

    #--------------------------------------------------------------
    # The following methods sample EventDicts, obtain the active
    # table,  and create the analyzed table (including injecting 
    # the vtec strings into the table.  
    #--------------------------------------------------------------

    def _convertEventsToVTECrecords(self, eventDicts):
        '''Create proposed table from eventDicts.

        Keyword Arguments:
        @eventDicts -- list of eventDict dictionaries.

        Returns list of vtec records representing the hazards that are
        proposed.
        '''

        rval = []
        for phd in eventDicts:

            if phd.get('siteID') != self._siteID4:
                continue   #not for this site

            # hazard type
            key = phd['type']    #form XX.Y where XX is phen
            keyParts = key.split('.')
            phen = keyParts[0]
            sig = keyParts[1]   #form XX.Y where Y is sig
            try:
                subtype = keyParts[2]
            except IndexError:
                subtype = ''

            geoType = phd.get('geoType')
            if geoType == 'area':
                areas = phd['ugcs']
            elif geoType == 'point':
                areas = [phd['pointID']]
            else:
                raise Exception("Unknown geoType 'type'".format(geoType))

            eventID = phd['eventID']

            # capture any h-vtec from the EventDicts
            hvtec = {}
            for p in ['floodSeverity', 'immediateCause', 'floodRecord',
              'riseAbove', 'crest', 'fallBelow', 'pointID']:
                hvtec[p] = phd.get(p) 
            if set(hvtec.values()) == set([None]):
                hvtec = None   # no vtec entries defined.

            #create the proposed dictionary for this EventDict.
            for areaID in areas:
                d = {}
                d['eventID'] = eventID
                d['geoId'] = areaID
                d['officeid'] = self._siteID4
                d['key'] = key   #such as TO.W

                if phd.get('state'):
                    d['state'] = phd['state']

                d['hvtec'] = hvtec

                d['startTime'] = float(phd['startTime'])

                d['endTime'] = float(phd['endTime'])

                d['act'] = "???"   #Determined after merges
                d['etn'] = phd.get('forceEtn', "???")
                d['seg'] = phd.get('forceSeg', 0)
                d['phen'] = phen    #form XX.Y where XX is phen
                d['sig'] = sig   #form XX.Y where Y is sig
                d['subtype'] = subtype
                d['hdln'] = self._vtecDef.hazards[key]['headline']
                d['ufn'] = phd.get('ufn', 0)
                if geoType == 'point':
                    d['pointID'] = phd.get('pointID')

                rval.append(d)
                
        # handle UFN events - convert ending time to max
        for proposed in rval:
            phensig = (proposed['phen'], proposed['sig'])
            # these keys always get the event from now until forever
            if phensig in self._ufnKeys:
                proposed['startTime'] = self._time #now
                proposed['endTime'] = float(2**31-1)*1000  #forever
                proposed['ufn'] = 1  #until further notice

            # these events are forced to be until further notice. Leave
            # starting time as specified in the eventDict.
            elif proposed['ufn'] == 1:
                proposed['endTime'] = float(2**31-1)*1000  #forever
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

        sortBy = ['officeid', 'geoId', 'phen', 'sig', 'seg', 'etn', 'act',
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
        compare = ['officeid', 'geoId', 'phen', 'sig', 'seg', 'etn', 'act', 'eventID']
        if atable:
            for i, rec in enumerate(atable):
                if i + 1 < len(atable) and \
                  self.hazardCompare(rec, atable[i + 1], compare):
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

        raise Exception("Hazard Category not found for {h}".format(h=hazard))

    
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
            raise Exception("Hazard Importance not found for {h}".format(
              h=hazard))


    def fixHazardConflict(self, index1, index2, hazardTable):
        '''This method uses the allowedHazards list to determine which
        hazardTable entry has the most important priority and removes
        the entry or piece thereof in place.

        Keyword Arguments:
        index1 -- index into the hazardTable to check for conflict.
        index2 -- index into the hazardTable to check for conflict.
        hazardTable -- list of dictionaries representing the analyzed hazard
          table in the vtecRecords format.

        Returns True if something was modified. Modifies the hazardTable.
        '''
       
        allowedHazardList = self.getAllowedHazardList()
        phen1 = hazardTable[index1]['phen']
        phen2 = hazardTable[index2]['phen']
        sig1 = hazardTable[index1]['sig']
        sig2 = hazardTable[index2]['sig']
        act1 =  hazardTable[index1]['act']
        act2 =  hazardTable[index2]['act']
        haz1 = phen1 + "." + sig1
        haz2 = phen2 + "." + sig2
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
        
        if hazardTable[lowIndex]['phen'] == 'SV' and \
           hazardTable[lowIndex]['sig'] == 'A' and \
           hazardTable[highIndex]['phen'] == 'TO' and \
           hazardTable[highIndex]['sig'] == 'A':
               if (int(hazardTable[lowIndex]['etn']) > int(hazardTable[highIndex]['etn']) and
                  (int(hazardTable[highIndex]['etn']) - int(hazardTable[lowIndex]['etn'])) > 50):
                   lowIndexTemp = lowIndex
                   lowIndex = highIndex
                   highIndex = lowIndexTemp
                           
        lowStart = hazardTable[lowIndex]['startTime']
        lowEnd = hazardTable[lowIndex]['endTime']
        highStart = hazardTable[highIndex]['startTime']
        highEnd = hazardTable[highIndex]['endTime']
                                                                                
        # first check to see if high pri completely covers low pri
        if highStart <= lowStart and highEnd >= lowEnd:  # remove low priority
            del hazardTable[lowIndex]
                                                                                
        # next check to see if high pri lies within low pri
        elif lowStart <= highStart and lowEnd >= highEnd:  # high pri in middle
            if lowStart < highStart:
                h = copy.deepcopy(hazardTable[lowIndex])
                # trim the early piece
                hazardTable[lowIndex]['endTime'] = highStart
                if lowEnd > highEnd:
                    # make a new end piece
                    h['startTime'] = highEnd
                    hazardTable.append(h)
            elif lowStart == highStart:
                hazardTable[lowIndex]['startTime'] = highEnd
                                                                                
        elif highEnd >= lowStart:
            hazardTable[lowIndex]['startTime'] = highEnd  # change low start
                                                                                
        elif highStart <= lowEnd:
            hazardTable[lowIndex]['endTime'] = highStart  # change low end

        return True
    

    def filterAllowedHazards(self, hazardTable):
        '''Removes all entries of the specified hazardTable that are not
        in the allowedHazards list.

        Keyword Arguments:
        hazardTable -- list of dictionaries represent hazards in the vtec
          record format.

        Returns the modified hazardTable.
        '''

        allowedHazardList = self.getAllowedHazardList()
        return [h for h in hazardTable if h['key'] in allowedHazardList]

    def filterEventIDs(self, hazardTable, limitEventIDs):
        '''Removes all entries of the specified hazardTable whose EventIDs
        are not in the list of limitEventIDs. If limitEventIDs is None, then
        no filtering is performed.

        Keyword Arguments:
        hazardTable -- list of dictionaries representing hazards in the
          vtec record format.
        limitEventIDs -- None for no filtering, otherwise a list of eventIDs
          that are to be retained.

        Returns the modified hazardTable.
        '''
        
        if limitEventIDs:
            return [h for h in hazardTable if h['eventID'] in limitEventIDs]
        else:
            return hazardTable

    def filterGeoZones(self, hazardTable, limitGeoZones):
        '''Removes all entries of the specified hazardTable whose geo id
        are not in the list of limitGeoZones. If limitGeoZones is None, then
        no filtering is performed. 

        Keyword Arguments:
        hazardTable -- list of dictionaries representing hazards in the
          vtec record format. Must not be in consolidated by id format.
        limitGeoZones -- None for no filtering, otherwise a list of geo
          identifiers that are to be retained.

        Returns the modified hazardTable.
        '''

        if limitGeoZones:
            table = [h for h in hazardTable if h['geoId'] in limitGeoZones]
        else:
            return hazardTable

    # This method filters out 'T' vtec records, or keeps just 'T' vtec records
    def filterTestMode(self, hazardTable, testMode):
        '''Removes all entries of the specified hazardTable whose vtec
        mode represent test or not, depending upon the testMode flag.

        Keyword Arguments:
        hazardTable -- list of dictionaries representing hazards in the
          vtec record format.
        testMode -- True to only keep test mode records, False to keep all
          records except for test mode records.

        Returns the modified hazardTable.
        '''
        if testMode:
            return [a for a in hazardTable if a['vtecstr'][0:3] == '/T.']
        else:
            return [a for a in hazardTable if a['vtecstr'][0:3] != '/T.']
                                        
    def filterZoneHazards(self, zone, hazardTable):
        '''This method searches all entries of the specified hazardTable for
        entries matching the specified zone.  Then for each entry it finds
        it looks for a conflicting entry in time.  If it finds one, it calls
        fixHazardsConflict, which fixes the table and then calls itself again
        recursively with the fixed table.

        Keyword Arguments:
        zone -- geographic identifier to match.
        hazardTable -- input/output list of dictionaries representing hazards
          in the non-consolidated by id vtec record format.

        '''

        for i in range(len(hazardTable)):
            if hazardTable[i]['geoId'] == zone:
                for j in range(i + 1, len(hazardTable)):
                    if hazardTable[j]['geoId'] == zone and i != j:
                        if self._hazardsOverlap(hazardTable[i], hazardTable[j]):
                            if self.fixHazardConflict(i, j, hazardTable):
                                self.filterZoneHazards(zone, hazardTable)
                                return


    def filterLowerPriorityHazards(self, hazardTable):
        '''Main method that drives the code to filter hazards that conflict
        in time. Only one hazard of the same phenomenon is allowed per zone
        per time.  This method processes the table, removing any time
        conflicts, so the one-hazard-per-zone-time rule is adhered to.

        Keyword Arguments:
        hazardTable -- input/output list of dictionaries representing hazards
          in the non-consolidated by id vtec record format.

        Returns the modified hazardTable.
        '''

        # get a raw list of unique edit areas
        zoneList = set([a['geoId'] for a in hazardTable])
        for zone in zoneList:
            # Remove lower priority hazards of the same type
            self.filterZoneHazards(zone, hazardTable)

        return hazardTable


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
            if p['key'] == "HY.S":
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

    def _checkForMergedEvents(self, proposedTable, vtecRecords):
        '''Checks and corrects for events that have merged together.

        Keyword Arguments:
        proposedTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.
        vtecRecords -- active set of vtecRecords, in non-consolidated format.

        Returns the modified proposed table and the modified vtecRecords table.
        '''

        # Checks for events that have merged together.  This could result
        # in dropped VTEC entries so we need to EXT one and CAN the other.
        # We remove entries from the active table (memory copy) and generate
        # additional CAN events.
        compare = ['geoId','phen','sig']

        createdCANEntries = []

        for proposed in proposedTable:
            matches = []
 
            #record match and time overlaps for real events
            for active in vtecRecords:
                if self.hazardCompare(proposed, active, compare) and \
                  active['act'] not in ['CAN','UPG','EXP'] and \
                  active['endTime'] > self._time and \
                  proposed['startTime'] <= active['endTime'] and \
                  proposed['endTime'] >= active['startTime']:
                    matches.append(active)

            #if multiple records match, we have a merged event
            #we need to find the highest etn for the event matches
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
                        del vtecRecords[vtecRecords.index(m)]

        #return the modified set of records
        return (proposedTable + createdCANEntries, vtecRecords)


    def _checkForCONEXT(self, proposedTable, vtecRecords):
        '''Checks for events that are CON or EXT.

        Keyword Arguments:
        proposedTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.
        vtecRecords -- active set of vtecRecords, in non-consolidated format.

        Returns the modified proposed table.
        '''

        # An event is considered continued if two hazards have the same
        # id, phen, sig, and if the end times match.  An event
        # is considered to be extended in time if the event overlaps
        # in time.
        compare = ['eventID', 'geoId', 'key', 'officeid']  #considered equal

        for proposed in proposedTable:

            if proposed['act'] == 'CAN':
                continue   #only occurs with merged events

            if proposed['endTime'] <= self._time:
                continue   #occurs with events that are ending right now

            for active in vtecRecords:
                if self.hazardCompare(proposed, active, compare) and \
                  active['act'] not in ['CAN', 'UPG', 'EXP'] and \
                  not self._separateETNtrack(proposed, active):

                    #convective watch (special case, also compare etn)
                    if proposed['phen'] in ['SV', 'TO'] and \
                      proposed['sig'] == "A" and \
                      proposed['etn'] != active['etn']:
                        continue  #allows CAN/NEW for new convect watches

                    # times exactly match
                    if proposed['startTime'] == active['startTime'] and \
                      proposed['endTime'] == active['endTime']:
                        proposed['act'] = 'CON'
                        proposed['etn'] = active['etn']
                    
                    # start times both before current time, end
                    # times the same, CON state
                    elif self._time >= proposed['startTime'] and \
                      self._time >= active['startTime'] and \
                      proposed['endTime'] == active['endTime']:
                        proposed['act'] = 'CON'
                        proposed['etn'] = active['etn']

                    # special case of event ended already, don't
                    # assign "EXT" even with overlap
                    elif self._time >= active['endTime']:
                        pass   #force of a new event since it ended

                    # start and/or end times overlap, "EXT" case
                    # except when user changed the start time
                    # of an event has gone into effect.  "EXT" has
                    # to be allowed.

                    elif self._hazardsOverlap(proposed, active):
                        if not self._allowTimeChange(proposed['key']):
                            raise Exception("Illegal to adjust time for "
                              " hazard {k}. \nProposed Record=\n{p}"
                              "\nActive=\n{a}".format(
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
                            
                            #save original time so we can later determine
                            #whether it is EXTENDED or SHORTENED
                            proposed['previousStart'] = active['startTime']
                            proposed['previousEnd'] = active['endTime']

        self._removeFieldFromTable(vtecRecords, 'conexted')

        return proposedTable

    def _checkForCANEXPUPG(self, pTable, vtecRecords, endedEventIDs=[]):
        '''Checks for events that are CAN, EXP, UPG (ended).

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.
        vtecRecords -- active set of vtecRecords, in non-consolidated format.
        endedEventIDs -- list of eventIDs that have state='ended'.  These 
          records have already been removed from the pTable.

        Returns the modified proposed table.
        '''

        compare1 = ['geoId', 'phen', 'sig']
        newEntries = []
        
        for active in vtecRecords:
            if active['officeid'] != self._siteID4:
                continue   #for a different site

            if active['act'] in ['CAN', 'UPG', 'EXP']:
                continue   #skip these records, event already over

            cancel_needed = 1

            # if endedEventIDs match the active records, then we don't have
            # to do comparisons for matches.
            if active['eventID'] not in endedEventIDs:
            
                # determine if cancel is needed, cancel (CAN, EXP, UPG).
                # Cancel not needed if we have an entry in proposed that
                # is already in active and the times overlap, and the active
                # ending time is still in the future
                for proposed in pTable:
                    if self.hazardCompare(active, proposed, compare1):
                        if self._hazardsOverlap(proposed, active) and \
                          self._time < active['endTime']:

                            # active event is in effect and proposed event is
                            # in future

                            # cancel active event
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
                    if self.hazardCompare(active, proposed, ['geoId']):

                        #find overlaps in time
                        if self._hazardsOverlap(proposed, active):

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
                # Only Allow "CAN" entries if the event is still ongoing, 
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

    def _checkForEXAEXB(self, pTable, vtecRecords):
        '''Checks for events that are EXA and EXB.

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.
        vtecRecords -- active set of vtecRecords, in non-consolidated format.

        Returns the modified proposed table.
        '''

        compare1 = ['geoId', 'phen', 'sig', 'etn', 'officeid']
        compare2 = ['phen', 'sig']

        for proposed in pTable:

            # do we allow EXA, EXB?
            if not self._allowAreaChange(proposed['key']):
                continue

            # first check to see if we have already assigned an action.
            # This is a special case for SPC watches that now appear in the
            # proposed table, but haven't been issued yet.  In this case,
            # we skip processing this record. Other events may have already
            # been assigned actions by this point.
            if proposed['act'] != "???":
                continue

            # Assume first that this is EXA or EXB
            exaexb_flag = 1

            #if we find a match, and it overlaps in time, 
            #then it isn't an EXA, EXB
            for active in vtecRecords:
                if self.hazardCompare(proposed, active, compare1):
                    #if proposed['startTime'] <= active['endTime'] and 
                    #  proposed['endTime'] >= active['startTime'] and 
                    if self._hazardsOverlap(proposed, active) and \
                      active['act'] not in ['CAN','EXP','UPG']:
                        exaexb_flag = 0
            
            # no match was found, thus this is either a EXA, or EXB,
            # match records with phen and sig the same
            if exaexb_flag == 1:
                #first check for EXA, must check ALL records before
                #deciding it isn't an EXA
                for active in vtecRecords:
                    if self.hazardCompare(proposed, active, compare2) and \
                      not self._separateETNtrack(proposed, active):
                        if active['act'] not in ['CAN', 'UPG', 'EXP']:

                            #if times are identical, then we extended in area 
                            if proposed['startTime'] == active['startTime'] and \
                              proposed['endTime'] == active['endTime']:
                                if proposed['etn'] == "???" or \
                                  proposed['etn'] == active['etn']:
                                    proposed['exaexb'] = 'EXA'
                                    proposed['active'] = active
                                    break

                            #if start times are both in the past or
                            #current, but end times equal, then it is
                            #an EXA
                            elif proposed['startTime'] <= self._time and \
                              active['startTime'] <= self._time and \
                              proposed['endTime'] == active['endTime']:
                                if proposed['etn'] == "???" or \
                                  proposed['etn'] == active['etn']:
                                    proposed['exaexb'] = 'EXA'
                                    proposed['active'] = active
                                    break

                if proposed.has_key('exaexb'):
                    continue

                #if it isn't an EXA, now we check the records again, but
                #check for overlapping or adjacent times, that do
                #not occur in the past in the active table, but ensure
                #that there is an event in the proposed that overlaps
                #with time. Results in EXB
                if proposed['act'] == "???":
                    for active in vtecRecords:
                        if self.hazardCompare(proposed, active, compare2) and \
                          not self._separateETNtrack(proposed, active):
                            if active['act'] not in ['CAN', 'UPG', 'EXP']:
                                #if self._hazardsOverlap(proposed, active) and
                                if proposed['startTime'] <= active['endTime'] and \
                                  proposed['endTime'] >= active['startTime'] and \
                                  active['endTime'] > self._time:
                                    if proposed['etn'] == "???" or \
                                      proposed['etn'] == active['etn']:
                                        #ensure record overlaps with proposed
                                        #event
                                        for p1 in pTable:
                                            if p1 == proposed:
                                                continue  #skip itself
                                            if self.hazardCompare(p1, proposed,
                                              compare2) and self._hazardsOverlap(p1, proposed):
                                                proposed['exaexb'] = 'EXB'
                                                proposed['active'] = active
                                                break
                                        break

        # Now set the marked records to EXA/EXB unless
        # there is already a CAN/EXP/UPG record with the same ETN
        # for the same phen/sig in the same zone

        # Organize hazards by zone
        eventDict = self._organizeByZone(pTable)
        for zone, hazards in eventDict.iteritems():
            # then organize by hazard key
            hazards = self._organizeByKey(hazards)
            for key, hzds in hazards.iteritems():
                for proposed in hzds:

                    if proposed.has_key('exaexb'):
                        act = proposed.pop('exaexb')
                        active = proposed.pop('active')
                        # checking if the etn is used
                        for p in hzds:
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


    def _checkForNEW(self, pTable, vtecRecords):
        '''Assigns NEW to remaining records and calculates the correct
        ETN number.

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.
        vtecRecords -- active set of vtecRecords, in non-consolidated format.

        Returns the modified proposed table.
        '''

        compare = ['geoId', 'key', 'officeid', 'eventID']

        #check for any remaining records that have an undefined action
        #these records must be "NEW".  Need to allocate a new etn, except
        #in two cases: one is already identified in the proposed table,
        #existing record in active table (phen,sig,id) regardless of pil.
        #
        #Already identified are basic TO.A, SV.A using aux data fields,

        allowedActions = ['NEW','CON','EXT','EXA','EXB']

        for proposed in pTable:
            if proposed['act'] == '???':
                if proposed['etn'] == "???":
                    #check in active table for a match (from other product),
                    #with events that still are occurring
                    etn = 0
                    for act in vtecRecords:
                        if self._hazardsOverlap(proposed, act) and \
                          act['act'] in allowedActions and \
                          self.hazardCompare(proposed, act, compare) and \
                          act['endTime'] > self._time:
                            etn = act['etn']
                            break

                    #not found in active nor proposed, prep for new one
                    if etn == 0:
                        self._prepETNCache(proposed)
                    else:
                        proposed['etn'] = etn   #match found in active table

                proposed['act'] = "NEW"

                # adjust starting time of new events to prevent them from
                # starting in the past and for ending events
                if proposed['startTime'] < self._time:
                    if self._time <= proposed['endTime']:
                        proposed['startTime'] = self._time
                    else:
                        proposed['startTime'] = proposed['endTime']

        # determine any new ETNs
        self._assignNewETNs(vtecRecords)
        LogStream.logDebug("New ETN cache: ", self._etnCache)

        # process again for records that are now marked NEW, but no etn
        for proposed in pTable:
            if proposed['act'] == 'NEW' and proposed['etn'] == "???":
                proposed['etn'] = self._getNewETN(proposed)

        return pTable

    def _checkInappropriateEXA(self, pTable):
        '''Adds for inappropriate extensions.

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.

        Throws exception if not proper extended in area.
        '''
        
        # find records that are similar, except for the actions
        compare = ['key', 'officeid', 'startTime', 'endTime', 'eventID']
        for p1 in pTable:
            for p2 in pTable:
                if self.hazardCompare(p1, p2, compare):
                    if not self._allowAreaChange(p2['key']):
                        if ((p1['act'] == '???' and p2['act'] != '???') or
                          (p1['act'] != '???' and p2['act'] == '???')):
                            raise Exception("Illegal to adjust area for hazard"
                              " {k}.\nProposed Records\n{p1}\n{p2}".format(
                              k=p1['key'], p1=self.printEntry(p1),
                              p2=self.printEntry(p2)))

    def _addEXPCodes(self, pTable):
        '''Adds in EXP codes (instead of CON) for events ready to expire.

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.

        Returns the modified proposed table.
        '''

        compare = ['geoId', 'key', 'officeid', 'eventID']
        
        #looks for events that have "CON", but are within 'expTimeLimit' 
        #minutes of event ending time and converts those events to EXP. 
        for each_hazard in pTable:
            if each_hazard['act'] == 'CON':
                timeFromEnd = self._time - each_hazard['endTime']   # +after
                expFromEnd = self._getExpirationLimits(
                  each_hazard['key']).beforeMinutes
                if timeFromEnd >= expFromEnd*60*1000 and timeFromEnd <= 0:
                    each_hazard['act'] = 'EXP'   #convert to expired

        return pTable
        
    def _removeEXPWithOngoingCodes(self, pTable):
        '''Remove EXP codes when another event with same phen/sig is now
        ongoing for this issuance year.

        Keyword Arguments:
        pTable -- proposed set of events in the vtecRecord format,
          non-consolidated by ID.

        Returns the modified proposed table.
        '''
        compare = ['phen','sig','etn','geoId']
        tmp = []
        for h in pTable:
            removeIt = False
            #events with EXP, and after ending time
            if h['act'] == 'EXP' and self._time >= h['endTime']:
                hIssueT = h.get('issueTime', self._time)
                hIssueYear = self.gmtime_fromMS(hIssueT)[0]
                for h1 in pTable:
                    #active event with same phen/sig/etn
                    h1IssueT = h1.get('issueTime', self._time)
                    h1IssueYear = self.gmtime_fromMS(h1IssueT)[0]
                    if h1['act'] in ['CON','EXA','EXB','EXT'] and \
                      self.hazardCompare(h, h1, compare) and \
                      h1IssueYear == hIssueYear:
                        removeIt = True
                        break
            if not removeIt:
                tmp.append(h)
        return tmp

    def _adjustStartTimesBasedOnNow(self, pTable):
        '''Adjusts the starting/ending times of the events to ensure they
        don't start in the past, and that the starting time is never past
        the ending time of the event.

        Keyword Arguments:
        pTable -- list of dictionaries representing events in the vtecRecord
          format.

        Returns the modified event table.
        '''

        for h in pTable:
            # adjust time of NEW events to ensure they don't start
            # earlier than now
            if h['startTime'] < self._time:
                if self._time <= h['endTime']:
                    h['startTime'] = self._time
                else:
                    h['startTime'] = h['endTime']
        return pTable
            
    def _addVTECStrings(self, pTable):
        '''Add VTEC strings to all events.

        Keyword Arguments:
        pTable -- list of dictionaries representing events in the vtecRecord
          format.

        Returns the modified event table.
        '''
        for h in pTable:

            # get the VTEC Mode
            if self._vtecMode is None:
                h['vtecstr'] = ""
                continue

            # use 00000000 or explicit times for the start time?  Use all
            # zeros for ongoing events and for HY.S events.
            if (h['act'] is 'NEW' or \
              (h['act'] == 'EXT' and h['previousStart'] > self._time) or \
              (h['act'] == 'EXB' and h['previousStart'] > self._time) or \
              (h['startTime'] > self._time)) and h['key'] != 'HY.S':
                startStr = self._vtecTimeStr(h['startTime'])
            else:
                startStr = self._vtecTimeStr(None)

            # use 00000000 if event is "Until Further notice"
            if h.get('ufn', 0) or h['key'] == 'HY.S':
                endStr = self._vtecTimeStr(None)
            else:
                endStr = self._vtecTimeStr(h['endTime'])

            # format the beastly string
            vfmt = '/{vm}.{act}.{site}.{phen}.{sig}.{etn:04d}.{start}-{end}/'
            h['vtecstr'] = vfmt.format(vm=self._vtecMode, site=h['officeid'],
              phen=h['phen'], sig=h['sig'], etn=h['etn'], start=startStr,
              end=endStr, act=h['act'])


    # generate H-VTEC strings for hazards
    def _addHVTECStrings(self, pTable):
        '''Add HVTEC strings to all appropriate events.

        Keyword Arguments:
        pTable -- list of dictionaries representing events in the vtecRecord
          format.

        Returns the modified event table.
        '''
        for h in pTable:
            hvtec = h.get('hvtec')   # must have hvtec dictionary defined
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
                idStr = hvtec.get('pointID') if self._geoType == 'point' else "00000"
                
                hfmt = '/{id}.{sev}.{ic}.{beg}.{crest}.{end}.{fr}/'
                h['hvtecstr'] = hfmt.format(id=idStr, sev=sev, ic=ic, fr=fr,
                  beg=startStr, crest=crestStr, end=endStr)

            else:
                h['hvtecstr'] = None


    # Add in headlines if missing in the table, note that headlines
    # are not added for situations of merged events, i.e., an event
    # that has a CAN and a ongoing with same phen/sig and overlapping time.
    # Leaving 'hdln' blank indicates no headline and no mention in hazard
    # products.
    def _addHeadlinesIfMissing(self, pTable):
        '''Add headlines to vtec records if missing.

        Keyword Arguments:
        pTable -- list of dictionaries representing events in the vtecRecord
          format.

        Returns the modified event table.
        '''

       
        compare = ['geoId','phen','sig']
        ongoingAct = ['EXT','EXB','CON','NEW','EXA']
        for h in pTable:

#TODO: Not clear if this will ever run, since apparently all entries in the
#pTable have a 'hdln' key.  
            if h.has_key('hdln'):
                continue
            phensig = h['phen'] + '.' + h['sig']

            #ongoing (merged) and CAN situation?
            mergedFound = False
            for h1 in pTable:
                if self.hazardCompare(h, h1, compare) and \
                  h['act'] == 'CAN' and h1['act'] in ongoingAct and \
                  h1['endTime'] > self._time and \
                  h['startTime'] <= h1['endTime'] and \
                  h['endTime'] >= h1['startTime']:
                      mergedFound = True
                      h['hdln'] = ""

            if mergedFound:
                h['hdln'] = ""
            else:
                h['hdln'] = self._vtecDef.hazards[phensig]['hdln']

    def _vtecTimeStr(self, t):
        '''Returns a formatted vtec/htec time string.

        Keyword Arguments:
        t -- time value in milliseconds since epoch. If None, then return
          the 'ongoing' or 'until further notice' string.

        Returns the formatted string.
        ''' 
        if t:
            return time.strftime("%y%m%dT%H%MZ", self.gmtime_fromMS(t))
        else:
            return "000000T0000Z"

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
        pTable -- list of dictionaries representing events in the vtecRecord
          format.

        Returns the modified event table.
        '''

        #step 1: reorganize the proposed table by zone, then by phen/sig.
        #dict of zones, then dict of phensigs, value is list of records.
        #Also create dictionary of originally max segment numbers for phen/sig.
        orgHaz = {}
        orgMaxSeg = {}  #key:phensig, value: max seg number
        for p in pTable:
            phensig = (p['phen'], p['sig'])
            id = p['geoId']
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
                        if self.hazardCompare(p, p1, compare) and \
                          p1['seg'] > orgMax:
                            p1['seg'] = p['seg']

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
            if (p['phen'],p['sig']) in self._ncKeys:
                try:
                    a = int(p['etn'])
                except:
                    raise Exception("ABORTING: Found National Hazard "
                      "with no ETN in grids. \n" + self.printVtecRecords(p) + \
                      " Fix your grids by adding watch/storm number." + \
                      "\nor running PlotSPCWatches or HazardRecovery" +\
                      "\n to correct situation.\n")

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
        currentYear = self.gmtime_fromMS(self._time)[0]
        for key in byZones:
            for h in byZones[key]:
                if (h['phen'], h['sig']) not in self._ncKeys:
                    continue   #only interested in checking national keys
                if h['act'] in ['EXP','UPG','CAN']:
                    hissueTime = h.get('issueTime', 0)
                    hissueYear = self.gmtime_fromMS(hissueTime)[0] #issueYear
                    for h1 in byZones[key]:
                        if self.hazardCompare(h, h1, compare) and \
                          h1['act'] in ['NEW','CON','EXA','EXT','EXB'] and \
                          currentYear == hissueYear:
                            raise Exception, "\n\n" + errorLine + "\n" +\
                             "ABORTING: Found VTEC Error"\
                             " with same ETN, same hazard, conflicting "\
                             "actions.\n" + self.printVtecRecords(h) + \
                             self.printVtecRecords(h1) + "\n" + \
                             "Fix, if convective watch, by coordinating "\
                             "with SPC. Otherwise serious software error.\n"\
                             "Cannot have new hazard with same ETN as one "\
                             "that is no longer in effect (EXP, UPG, CAN)."\
                             "\n" + errorLine

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
            if p['act'] != 'EXP' or self._time - p['endTime'] <= timeLimit*60*1000:
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
        compare = ['etn', 'phen', 'sig', 'geoId']
        compare2 = ['phen', 'sig']
        for p in pTable:
            #look for all events to get max etn for each phen/sig
            vteckey = p['phen'] + p['sig']
            if not keyetnmax.has_key(vteckey):
                etn_max = 0
                for e in pTable:
                    if self.hazardCompare(p, e, compare2) and \
                       e['etn'] > etn_max:
                        etn_max = e['etn']
                keyetnmax[vteckey]= etn_max

        assigned = {}
        for p in pTable:
            #only look for EXT, EXA, EXB events
            if p['act'] in ['NEW', 'EXP', 'UPG', 'CAN', 'CON']:
                continue
            vteckey = p['phen'] + p['sig']

            for p1 in pTable:
                #check for matching id,etn,phen,sig,act combinations, these
                #are the ones that need to be reassigned. 
                if self.hazardCompare(p, p1, compare) and \
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

    def _addUpgradeDowngradeRec(self, proposedTable):
        '''Add upgrade/downgrade information for upgrades and downgrades.

        Keyword Arguments:
        pTable -- list of hazard records in the vtecRecords format.

        Returns the modified list of hazard records.
        '''

        compare = ['geoId', 'officeid']
        fields = ['etn', 'startTime', 'endTime', 'key', 'phen', 'sig', 'seg', 'act',
          'subtype']

        for rec in proposedTable:
            if rec['act'] in ['NEW', 'EXA', 'EXB', 'EXT']:
                for checkR in proposedTable:
                    if checkR['act'] in ['CAN', 'UPG']:
                        if self._hazardsOverlap(checkR, rec) and \
                           self.hazardCompare(checkR, rec, compare):
                            if self._isDowngrade(rec, checkR):
                               rec['downgradeFrom'] = \
                                 self._copyFields(checkR, fields)
                            elif self._isUpgrade(rec, checkR):  
                               rec['upgradeFrom'] = \
                                 self._copyFields(checkR, fields)

        return proposedTable

        
    
    def _calcAnalyzedTable(self, pTable, vtecRecords, endedEventIDs,
      combinableSegments):
        '''Main routine to calculate the analyzed table from the proposed
        and active vtecRecords.

        Keyword Arguments:
        pTable -- list of proposed hazards in the vtecRecord format.
        vtecRecords -- list of already issued hazards in the vtecRecord format.
        endedEventIDs -- list of eventIDs that are marked 'ended'.
        combinableSegments -- True if hazards can be combined into the
          same segment; otherwise False if the hazards must be maintained as
          separate segments.

        Returns the analyzed list of hazard records representing the new
        set of vtec records and hazards.
        '''

        
        # special code for HY.S
        pTable = self._handleHYS(pTable)
        LogStream.logDebug("Analyzed Table -- After handleHYS:", 
          self.printVtecRecords(pTable, combine=True))

        # convert active table EXP still in effect to CON
        vtecRecords = self._convertEXPtoCON(vtecRecords)
        LogStream.logDebug("Analyzed Table -- After convertEXPtoCON:", 
          self.printVtecRecords(pTable, combine=True))

        if (combinableSegments):
            # Drop multiple segments for same phen/sig in same "geoId"
            pTable = self._checkForMultipleSegsInSameID(pTable)
            LogStream.logDebug("Analyzed Table -- After checkForMultipleSegsInSameID:", 
              self.printVtecRecords(pTable, combine=True))
       
            # Check for Merged Events
            pTable, vtecRecords = self._checkForMergedEvents(pTable,
              vtecRecords)
            LogStream.logDebug("Analyzed Table -- After checkForMergedEvents:", 
              self.printVtecRecords(pTable, combine=True))

        # Check for CON and EXT actions
        pTable = self._checkForCONEXT(pTable, vtecRecords)
        LogStream.logDebug("Analyzed Table -- After checkForCONEXT:", 
          self.printVtecRecords(pTable, combine=True))

        # Check for CAN, EXP, and UPG
        pTable = self._checkForCANEXPUPG(pTable, vtecRecords, endedEventIDs)
        LogStream.logDebug("Analyzed Table -- After checkForCANEXPUPG:", 
          self.printVtecRecords(pTable, combine=True))

        # Check for EXA/EXB
        pTable = self._checkForEXAEXB(pTable, vtecRecords)
        LogStream.logDebug("Analyzed Table -- After checkForEXAEXB:", 
          self.printVtecRecords(pTable, combine=True))

        # Assign NEW to remaining records
        pTable = self._checkForNEW(pTable, vtecRecords)
        LogStream.logDebug("Analyzed Table -- After checkForNEW:", 
          self.printVtecRecords(pTable, combine=True))


        # Check for upgrades and downgrades, add records if needed
        if (combinableSegments):
            pTable = self._addUpgradeDowngradeRec(pTable)
            LogStream.logDebug("Analyzed Table -- After addUpgradeDowngradeRec:", 
              self.printVtecRecords(pTable, combine=True))

        # Convert ongoing events about ready to expire (still in the
        # proposed grids) to switch from CON to EXP
        pTable = self._addEXPCodes(pTable)
        LogStream.logDebug("Analyzed Table -- After addEXPCodes:", 
          self.printVtecRecords(pTable, combine=True))

        if (combinableSegments):
            # Eliminate any EXPs if other events (same phen/sig) in effect
            # at present time.
            pTable = self._removeEXPWithOngoingCodes(pTable)
            LogStream.logDebug("Analyzed Table -- After removeEXPWithOngoingCodes:", 
              self.printVtecRecords(pTable, combine=True))

            # Ensure valid ETN/Actions - no EXP/CAN with valid same ETN
            # for national events
            self._checkValidETNsActions(pTable)
            LogStream.logDebug("Analyzed Table -- After checkValidETNsActions:",
              self.printVtecRecords(pTable, combine=True))

        # Remove EXPs that are 'xx' mins past the end of events
        pTable = self._removeOverdueEXPs(pTable)
        LogStream.logDebug("Analyzed Table -- After removeOverdueEXPs:",
          self.printVtecRecords(pTable, combine=True))

        # Ensure that there are not ETN dups in the same segment w/diff
        # action codes
        self._checkETNdups(pTable)
        LogStream.logDebug("Analyzed Table -- After checkETNdups:",
          self.printVtecRecords(pTable, combine=True))

        # Ensure that starting times are not before now, and that starting
        # times are not after the event ends.
        pTable = self._adjustStartTimesBasedOnNow(pTable)

        # Complete the VTEC Strings
        self._addVTECStrings(pTable)
        LogStream.logDebug("Analyzed Table -- After addVTECStrings:", 
          self.printVtecRecords(pTable, combine=True))
    
        # Complete the H-VTEC Strings
        self._addHVTECStrings(pTable)
        LogStream.logDebug("Analyzed Table -- After addHVTECStrings:", 
          self.printVtecRecords(pTable, combine=True))

        #add in hdln entries if they are missing
        self._addHeadlinesIfMissing(pTable)
        LogStream.logDebug("Analyzed Table -- After addHeadlinesIfMissing:", 
          self.printVtecRecords(pTable, combine=True))        

        # Ensure that all SV.A and TO.A have valid ETNs
        if (combinableSegments):
            self._checkValidETNcw(pTable)

        # set issueTime to current time
        for rec in pTable:
            rec['issueTime'] = self._time
        
        # Return pTable, which is essentially analyzedTable at this point
        LogStream.logVerbose("Analyzed Records: ", self.printVtecRecords(pTable,
         combine=True))
        return pTable

    # is marine zone?
    def _isMarineZone(self, id):
        if id[0:2] in self._marineZonesPrefix:
            return True;
        else:
            return False;

    def _separateETNtrack(self, rec1, rec2):
        '''Determine whether the two hazard records must follow a separate
        set of ETNs and actions, even though the phens and sigs may be
        the same.

        Keyword Arguments:
        rec1 -- hazard record in the vtecRecord format -- non-consolidated.
        rec2 -- hazard record in the vtecRecord format -- non-consolidated.

        Returns True if the two records should follow separate action and etn
        tracks.
        '''
        # marine zones and non-marine zones for tpc phen/sigs follow their own
        # sequence of ETNs and actions. This routine determines if separate
        # ETNs/actions should occur between id1 and id2. Returns true if 
        # separate ETN tracks are required - basically if id1 and id2 are one
        # marine and the other not, and the phen/sigs are identical and are tpc
        # phen/sigs. Also returns true if phen/sigs are not identical.
        # returns false.  Only considers phen/sig/id.

        ps1 = (rec1['phen'], rec1['sig'])
        ps2 = (rec2['phen'], rec2['sig'])
        # same phen/sig
        if ps1 == ps2:
            # tropical?
            if ps1 in self._tpcKeys:
                # one a marine zone, the other not?, that requires sepa track
                return (self._isMarineZone(rec1['geoId']) != \
                  self._isMarineZone(rec2['geoId']))
            else:
                return False   #same phen/sig, not tpc, so. non separate track
        else:
            return True;
    
    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()
