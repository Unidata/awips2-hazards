#!/usr/local/python/bin/python

# This program decodes a product's UGC and VTEC strings, and updates
# the "active" table for VTEC.

# modified for compatibility with ihis, separated out ingester from 
# decoder.

# To work normally in the AWIPS environment, must have siteConfig.py, 
# VTECPartners.  Failure to have these results in no filtering of decoded
# products, i.e., all sites are decoded, no text captures, no remapping
# of pils (TOR->SVS), no notification of WCL, SPC, TPC bulletins to GFE.

# REFACTOR : 2012/07/18
#    rename activeTable to vtecRecords
#
#    Feb 14, 2013    2161     Chris.Golden        Added use of UFN_TIME_VALUE_SECS constant
#                                                 instead of hardcoded value.
#
import sys, os, time, re, string, getopt, cPickle, os.path
import copy, fcntl, glob, stat, tempfile, VTECTableSqueeze
import VTECTableUtil as VTECTableUtil
import VTECConstants

import VTECIngester
try:
    import LogStream
    import AFPS
except:
    import Logger as LogStream #for ihis

ACCURATE_CITIES_PILS = ['CFW', 'FFA', 'NPW', 'RFW', 'WSW']


class VTECDecoder(VTECTableUtil.VTECTableUtil):

    def __init__(self):
        #to ensure time calls are based on Zulu
        os.environ['TZ'] = "GMT0"

        #decode the command line
        self._decodeCommandLine()
        
        #base time for decoder
        self._time = time.time()   #present time
        os.umask(0)   #ensure proper permissions

        # get the SPC and TPC site id from the configuration file
        self._spcSite = self._getConfig("VTECPartners", "VTEC_SPC_SITE", "KWNS")
        self._tpcSite = self._getConfig("VTECPartners", "VTEC_TPC_SITE", "KNHC")

        # get the mapped pils from VTECPartners
        defaultMappedPils =  {
          ('TO','W','SVS'): 'TOR', ('SV','W','SVS'): 'SVR', 
          ('FF','W','FFS'): 'FFW', ('FL','W','FLS'): 'FLW', 
          ('MA','W','MWS'): 'SMW', ('EW','W','SVS'): 'EWW', 
          ('FA','W','FLS'): 'FLW'
          }
        self._mappedPils = self._getConfig("VTECPartners", "VTEC_MAPPED_PILS", 
          defaultMappedPils)

        # get our site, if we don't have a siteConfig, then we don't know
        # our site so we don't do filtering (and we can't send notifications
        # for WCL, SPC, TPC bulletin arrivals)
        try:
            import siteConfig
            siteid = siteConfig.GFESUITE_SITEID
            self._ourSite = self._get4ID(siteid)
            self._filterSites = self._getFilterSites()
        except:
            self._filterSites = None #no site info, so don't filter
            self._ourSite = None

        #set up active table
        # REFACTOR rename self._activeTableFilename to self._vtecRecordsFilename
        VTECTableUtil.VTECTableUtil.__init__(self, self._vtecRecordsFilename)
        self._ingester = VTECIngester.VTECIngester(self._vtecRecordsFilename)

        #get product
        self._lines = self._getProduct()
        if len(self._lines) < 2:
            raise Exception, "Empty Product -- ABORTING"

        #Delete incoming product if requested.
        if self._deleteAfterProcessing:
            os.remove(self._incomingFilename)

        #set up vtec regular expressions
        self._vtecRE = r'/[OTEX]\.([A-Z]{3})\.([A-Z]{4})\.([A-Z]{2})\.' + \
          r'([WAYSOFN])\.([0-9]{4})\.([0-9]{6})T([0-9]{4})Z\-' + \
          r'([0-9]{6})T([0-9]{4})Z/'
        self._hVtecRE = r'/[0-9A-Z]{5}\.[0-3NU]\.[A-Z]{2}\.' + \
          r'[0-9]{6}T[0-9]{4}Z\.' + \
          r'[0-9]{6}T[0-9]{4}Z\.[0-9]{6}T[0-9]{4}Z\.[A-Z][A-Z]/'
        self._badVtecRE = r'^\s?/.*/\s?$'
        self._endSegmentRE = r'^\$\$'
        self._dlineRE = r"^1?[0-9]{3} [AP]M [A-Z][A-Z]?[DS]T.*[A-Z]{3} " + \
          r"[123]?[0-9] 2[0-9]{3}.*$"

        #maximum future time (used for until further notice)
        self._maxFutureTime = VTECConstants.UFN_TIME_VALUE_SECS

    def decode(self):
        #get pil and date-time group
        self._productPil, self._issueTime, linePos,\
          self._completeProductPil  = self._getPilAndDTG()

        # If this is a WCL - don't go any further. Run WCL procedure and exit.
        if self._productPil[0:3] == "WCL":
            self._createWCLNotification(self._completeProductPil, self._lines)
            return
        
        # Determine if this is a segmented product
        segmented = self._determineSegmented(linePos)
 
        # Get overview text
        if segmented == 1:
            self._overviewText, linePos = self._getOverviewText(linePos)
        else:
            self._overviewText = ''
        LogStream.logDebug("OverviewText: ", self._overviewText)

        #find all UGCs, VTEC strings, and segment text
        ugcVTECList = self._getUGCAndVTECStrings(linePos)

        #convert UGC strings into UGC list
        ugcVTECSegText = []
        segCount = 1
        for ugcString, vtecStrings, segText, cities in ugcVTECList:
            purgeTime = self._dtgFromDDHHMM(ugcString[-7:-1])
            ugcList = self._expandUGC(ugcString)
            vtecList = self._expandVTEC(ugcList, vtecStrings, segCount,
              segText, cities, purgeTime)
            segCount = segCount + 1
            for r in vtecList:
                #create the records, do site filtering
                if self._filterSites is None or r['officeid'] in self._filterSites:
                    ugcVTECSegText.append(r)
                else:
                    LogStream.logDebug("Site filtered out: ", r)
        if len(ugcVTECSegText) == 0:
            LogStream.logVerbose("No VTEC Found in product")
            return

        # now merge in records to existing active table
        d = self._ingester.ingestVTEC(ugcVTECSegText, self._time, 
          self._issueTime)

        # print out info about change
        # REFACTOR rename self.printActiveTable to self.printVtecRecords
        #          change message in logVerbose from Active Table to VTEC Records 
        if d is not None:
            LogStream.logVerbose("Updated VTEC Records: purged\n",
               self.printVtecRecords(d['purgedRecords'], combine=1))
            LogStream.logVerbose("Updated VTEC Records: replaced\n",
               self.printVtecRecords(d['replacedRecords'],  combine=1))
            LogStream.logVerbose("Updated VTEC Records: decoded\n",
               self.printVtecRecords(d['decodedRecords'], combine=1))

            #send alert to gfe if needed
            if self._notifyGFE:
                msg = self._createSPCNotification(self._productPil,
                  ugcVTECSegText, self._lines)
                self._sendGFENotification(msg)
                msg = self._createTPCNotification(self._productPil,
                  ugcVTECSegText, self._lines)
                self._sendGFENotification(msg)
    
            #did active table change?
            if d['dbChanged']:

                # notify ifpServer of changes
                self._notifyIFPServerOfChanges(d['changeSummary'])

                # make backups
                # REFACTOR NEED CONFIRM
                #          rename self.saveOldActiveTable to self.saveOldVtecRecords
                #          rename d['oldActiveTable'] to d['oldVtecRecords'] 
                if self._makeBackups:
                    self.saveOldVtecRecords(d['oldVtecRecords'])
                    pTime = self._getConfig("VTECPartners",
                      "VTEC_BACKUP_TABLE_PURGE_TIME", 168*4)
                    self.purgeOldSavedTables(pTime)

        return


    def _usage(self):
        #Prints out usage information if started without sufficient command
        #line arguments.
        # REFACTOR rename activeTableName to vtecRecordsName in usage
        s =  """
usage: VTECDecoder -f productfilename -d -a vtecRecordsName
-f productfilename:  location of the file to be decoded
-d:                  delete input file after processing flag
-a vtecRecordsName:  location of the active table to hold decoded info
-g:                  Notify GFE when configured items arrive
-w: wmofficeid            WMofficeid for product, (optional, for info only)
-n:                  Do not make backups (optional)
-z: drtInfo          Run in DRT mode
"""
        print s
        LogStream.logProblem(s)

    def _decodeCommandLine(self):
        #Routine to decode the command line.
        self._deleteAfterProcessing = 0
        self._incomingFilename = None
        # REFACTOR rename self._activeTableFilename to self._vtecRecordsFilename
        # self._activeTableFilename = None
        self._vtecRecordsFilename = None
        self._notifyGFE = 0
        self._makeBackups = 1  #make backups after each "update"
        self._wmofficeid = None

        if len(sys.argv) < 2:
            self._usage()
            sys.exit(1)

        LogStream.logVerbose("Command line: ", sys.argv[1:])

        try:
            optionlist, arglist = getopt.getopt(sys.argv[1:], 'f:da:gw:nz:')
        except getopt.error, val:
            LogStream.logProblem(val)
            self._usage()
            sys.exit(1)

        for each_option in optionlist:
            if each_option[0] == '-f':
                self._incomingFilename = each_option[1]
            elif each_option[0] == '-w':
                self._wmofficeid = each_option[1]
            elif each_option[0] == '-a':
                # REFACTOR rename self._activeTableFilename to self._vtecRecordsFilename
                self._vtecRecordsFilename = each_option[1]
            elif each_option[0] == '-d':
                self._deleteAfterProcessing = 1
            elif each_option[0] == '-n':
                self._makeBackups = 0
            elif each_option[0] == '-g':
                self._notifyGFE = 1
            elif each_option[0] == '-z':
                import DTFCommonLibrary.VTECutilities.offsetTime as offsetTime
                offsetTime.setDrtOffset(each_option[1])

        if self._incomingFilename is None:
            LogStream.logProblem("Invalid command line specified", sys.argv[1:])
            self._usage()
            sys.exit(1)
            
        # REFACTOR rename self._activeTableFilename to self._vtecRecordsFilename
        if self._vtecRecordsFilename is None:
            # REFACTOR change Active Table to VTEC Records in log message
            LogStream.logProblem("WARNING: VTEC Records Not Defined")

        LogStream.logVerbose("WMofficeid: ", self._wmofficeid)

    def _getTextCaptureCategories(self):
        #gets the list of product categories that need their text captured.
        #if the list is empty, then all products are captured into the
        #active table and None is returned.
        cats = self._getConfig("VTECPartners", "VTEC_CAPTURE_TEXT_CATEGORIES", 
          [])
        if len(cats) == 0:
            return None
        LogStream.logDebug("Text Capture Categories: ", cats)
        return cats

    def _getFilterSites(self):
        #gets the list of filter sites, which is the list specified, plus
        #SPC plus our own site.  Returns None for no-filtering.
        if self._ourSite is None:
            return None
        sites = self._getConfig("VTECPartners", "VTEC_DECODER_SITES", None)
        if sites is None:
            return None
        sites.append(self._spcSite)
        sites.append(self._tpcSite)
        sites.append(self._ourSite)
        LogStream.logVerbose("Filter Sites: ", sites)
        return sites

    def _getProduct(self):
        #Opens, reads the product. Splits into lines, strips whitespace,
        fd = open(self._incomingFilename, 'r')
        if fd == -1:
            s = "Unable to open incoming file: " + self._incomingFilename
            LogStream.logProblem(s)
            raise Exception, s
        buf = fd.read()
        fd.close()


        #eliminate junk characters and change some
        if self._wmofficeid is not None:
            index = string.find(buf, self._wmofficeid)
            if index != -1:
                buf = buf[index:]   #remove garbage before start of real prod
        buf = re.sub(r",", r"...", buf)   #eliminate commas, replace with ...
        buf = re.sub(r"\.\.\. r", "...", buf)  #'... ' becomes '...'
        buf = re.sub(r"\r", r"", buf)   #eliminate carriage returns

        LogStream.logVerbose("Product:\n", buf)

        #process the file into lines, eliminate trailing whitespace
        lines = string.split(buf, '\n')
        for n in xrange(len(lines)):
            lines[n] = string.rstrip(lines[n])

        return lines

    def _determineSegmented(self, startLine):
        #
        # Determine if this is a segmented product or not
        #
        count = startLine
        dlineFlag = 0
        while count < 12 and count < len(self._lines):
            if re.search(r'^[A-Z][A-Z][CZ][0-9][0-9][0-9].*',
               self._lines[count]):
                if dlineFlag == 0:
                    return 0
            
            if re.search(self._dlineRE, self._lines[count]):
                dlineFlag = 1
            
            count += 1

        return 1
             
                 
        
    def _getOverviewText(self, startLine):
        #searches through the product from the startLine to the date-time 
        #group, then until the first UGC line.  Extracts out the text for 
        #the overview text (which is after the MND header.  Returns the 
        #overviewText.
        count = startLine
        ugcLine = None

        #search for the MND header date line
        while 1:
            dline_search = re.search(self._dlineRE, self._lines[count])
            count = count + 1
            if dline_search:
                break
            if count >= len(self._lines)-1:
                raise Exception, "No MND date line to start overview text"
        startOverviewLine = count  #next line after MND date line

        #search for the 1st UGC line 
        ugcRE = r'^[A-Z][A-Z][CZ][0-9][0-9][0-9].*'
        while 1:
            ugc_search = re.search(ugcRE, self._lines[count])
            if ugc_search:
                stopOverviewLine = count - 1
                break
            count = count + 1
            if count >= len(self._lines)-1:
                raise Exception, "No UGC line to end overview text"

        #now eliminate any blank lines between the start/stop overview line
        while startOverviewLine <= stopOverviewLine:
            if len(string.strip(self._lines[startOverviewLine])) != 0:
                break
            startOverviewLine = startOverviewLine + 1
        while startOverviewLine <= stopOverviewLine:
            if len(string.strip(self._lines[stopOverviewLine])) != 0:
                break
            stopOverviewLine = stopOverviewLine - 1

        LogStream.logDebug("start/stop overview: ", startOverviewLine, 
          stopOverviewLine)

        #put together the text
        if startOverviewLine <= stopOverviewLine:
            overviewLines = self._lines[startOverviewLine:stopOverviewLine+1]
            overviewText = string.join(overviewLines, '\n')
            return (overviewText, stopOverviewLine)
        else:
            return ("", startLine)

 
    def _getPilAndDTG(self):
        #searches through the product (lines) and extracts out the product
        #pil and date-time group. Returns (pil, issueTime, lineEnd, fullpil).
        # The line end is how far the processing got for the PIL line.
        count = 0
        while 1:
            dtg_search = re.search(r' ([0123][0-9][012][0-9][0-5][0-9])',
              self._lines[count])
            pil_search = re.search(r'^([A-Z]{3})(\w{3}|\w{2}|\w{1})',
              self._lines[count+1])

            if dtg_search and pil_search:
                LogStream.logVerbose("Dtg=", dtg_search.group(0))
                LogStream.logVerbose("Pil=", pil_search.group(0))
                return (self._lines[count+1][0:3],
                  self._dtgFromDDHHMM(dtg_search.group(1)), count+2,
                    pil_search.group(0))
            count = count + 1
            if count >= len(self._lines)-1:
                LogStream.logProblem("Did not find either the product DTG" +\
                  " or the pil: ", string.join(self._lines, sep='\n'),
                  LogStream.exc())
                raise Exception, "Product DTG or Pil missing"

    def _dtgFromDDHHMM(self, dtgString):
        #utility function taking a ddhhmm string
        #group1=day, group2=hour, group3=minute
        #returns a time object
        wmo_day = int(dtgString[0:2])
        wmo_hour = int(dtgString[2:4])
        wmo_min = int(dtgString[4:6])

        gmtuple = time.gmtime(self._time)
        wmo_year = gmtuple[0]  #based on current time
        wmo_month = gmtuple[1] #based on current time
        current_day = gmtuple[2]
        if current_day - wmo_day > 15:
            # next  month
            wmo_month = wmo_month + 1
            if wmo_month > 12:
                wmo_month = 1
                wmo_year = wmo_year + 1
        elif current_day - wmo_day < -15:
            # previous month
            wmo_month = wmo_month -1
            if wmo_month < 1:
                wmo_month = 12
                wmo_year = wmo_year - 1

        s = `wmo_year` + "%02i" % wmo_month + "%02i" % wmo_day + \
          "%02i" % wmo_hour + "%02i" % wmo_min + "UTC"
        timeTuple = time.strptime(s, "%Y%m%d%H%M%Z")
        wmoTime = time.mktime(timeTuple)   #TZ is GMT0, so this mktime works

        LogStream.logVerbose("DTG=",dtgString, "IssueTime=",
          time.asctime(time.gmtime(wmoTime)))

        return wmoTime


    def _getUGCAndVTECStrings(self, lineStart):
        #goes through the product, extracts UGC and VTEC strings and the
        #segment text, returns a list of (UGC keys, VTEC strings, segText).
        #Segment number is determined by order in the list.
        ugcList = []
        count = lineStart   #start on line following PIL
        while 1:
            #look for the first UGC line
            if re.search(r'^[A-Z][A-Z][CZ][0-9][0-9][0-9].*',
              self._lines[count]):
                LogStream.logDebug("First line of UGC found on line: ", count,
                  '[' + self._lines[count] + ']')

                #find the line with the terminating ugc (dtg), might be
                #the same one. Terminating line has -mmddhh
                #combine all of the UGC lines that are split across
                nxt = 0  #number of lines from the first UGC line
                ugc = "" #final UGC codes
                while count+nxt < len(self._lines):
                    if not re.search(r'.*[0-9][0-9][0-9][0-9][0-9][0-9]-',
                      self._lines[count+nxt]):
                        nxt = nxt + 1
                    else:
                        LogStream.logDebug("Last line of UGC found on line: ",
                          count+nxt, '[' + self._lines[count+nxt] + ']')
                        ugc = string.join(self._lines[count:count+nxt+1],
                          sep="")
                        break
                if len(ugc) == 0:
                    s = "Did not find end of UGC line which started on " +\
                      " line " + `count`
                    LogStream.logProblem(s)
                    raise Exception, "Aborting due to bad UGC lines"


                #find the VTEC codes following the ugc line(s)
                nxt = nxt + 1  #go the 1st line after ugc
                vtectext = []
                while count+nxt < len(self._lines):
                    if re.search(self._vtecRE, self._lines[count+nxt]):
                        vtectext.append(self._lines[count+nxt])
                        LogStream.logDebug("VTEC found on line: ",
                          count+nxt, self._lines[count+nxt])
                    elif (re.search(self._badVtecRE, self._lines[count+nxt]) \
                      and not re.search(self._hVtecRE, self._lines[count+nxt])):
                        LogStream.logProblem("Bad VTEC line detected on line#",
                          count+nxt, '[' + self._lines[count+nxt] + ']',
                          'UGC=', ugc)
                        raise Exception,"Aborting due to bad VTEC line"
                    else:
                        break    #no more VTEC lines for this ugc
                    nxt = nxt + 1   #go to next line


                # for capturing the city names
                cityFirst = count+nxt
                cityLast = cityFirst - 1

                #capture the text from dtg to the $$ at the beginning of
                #the line.  Just in case there isn't a $$, we also look
                #for a new VTEC or UGC line, or the end of file.
                textFirst = count+nxt
                dtgFound = 0
                segmentText = ""
                while count+nxt < len(self._lines):

                    # Date-TimeGroup
                    if dtgFound == 0 and re.search(self._dlineRE, 
                      self._lines[count+nxt]):
                        cityLast = count+nxt-1
                        textFirst = count+nxt+2  #first text line
                        dtgFound = 1

                    # found the $$ line
                    elif re.search(self._endSegmentRE, self._lines[count+nxt]):
                        segmentText = self._prepSegmentText(\
                          self._lines[textFirst:count+nxt])
                        break

                    # found a UGC line, terminate the segment
                    elif re.search(r'^[A-Z][A-Z][CZ][0-9][0-9][0-9].*',
                      self._lines[count+nxt]):
                        segmentText = self._prepSegmentText(\
                          self._lines[textFirst:count+nxt])
                        nxt = nxt - 1  #back up one line to redo UGC outer loop
                        break

                    # end of file, terminate the segment
                    elif count+nxt+1 == len(self._lines):
                        segmentText = self._prepSegmentText(\
                          self._lines[textFirst:count+nxt+1])
                        break

                    nxt = nxt + 1   #next line

                # capture cities
                cityText = ''
                for i in range(cityFirst, cityLast+1):
                    line = self._lines[i].rstrip()

                    if line.startswith("INCLUDING THE"):
                        cityText = line
                    elif cityText != '':
                        cityText += line
                        
                cities = []
                if cityText != '':
                    cities = cityText.split('...')[1:]

                #add the ugc and vtec text to the list
                ugcList.append((ugc, vtectext, segmentText, cities))

                count = count + nxt

            count = count + 1
            if count >= len(self._lines):
                break
        for e in ugcList:
            LogStream.logVerbose("UGC/VTEC found: ", e[0], e[1])
        return ugcList

    def _expandUGC(self, ugcString):
        #expand a UGC string into its individual UGC codes, returns the list.
        ugc_list = []    #returned list of ugc codes
        ugc_line = ugcString[0:-8]   #eliminate dtg at end of ugc string
        working_ugc_list = ugc_line.split('-')  #individual parts
        state = ''
        code_type = ''

        for ugc in working_ugc_list:
            try:
                # Case One (i.e., WIZ023)...
                if len(ugc) == 6:
                    state, code_type, decUGCs = self._ugcCaseOne(ugc)
                    for d in decUGCs:
                        ugc_list.append(d)

                # Case Two (i.e., 023)...
                elif len(ugc) == 3:
                    decUGCs = self._ugcCaseTwo(ugc, state, code_type)
                    for d in decUGCs:
                        ugc_list.append(d)

                # Case Three (ie. 044>067)
                elif len(ugc) == 7:
                    decUGCs = self._ugcCaseThree(ugc, state, code_type)
                    for d in decUGCs:
                        ugc_list.append(d)

                # Case Four (ie. WIZ044>067)
                elif len(ugc) == 10:
                    state, code_type, decUGCs = self._ugcCaseFour(ugc)
                    for d in decUGCs:
                        ugc_list.append(d)

                # Problem - malformed UGC
                else:
                    raise Exception, "Malformed UGC Found"

            except:
                LogStream.logProblem("Failure to decode UGC [" + ugc + \
                  "] in [" + ugc_line + "]", `self._lines`, LogStream.exc())
                raise Exception, "Failure to decode UGC"

        return ugc_list

    def _ugcCaseOne(self, ugc):
        #Decodes the WIZ023 case. Returns state, code type, and list of
        #decoded UGC codes.  Returns None on failure.
        subtype_search = re.search(r'[A-Z][A-Z][CZ][0-9][0-9][0-9]', ugc)
        if not subtype_search:
            return None
        state = ugc[0:2]
        code_type = ugc[2]
        code = ugc[3:6]
        decodedUgcs = [(state + code_type + code)]
        return (state, code_type, decodedUgcs)



    def _ugcCaseTwo(self, ugc, state, code_type):
        #Decodes the 034 case. Current state and code_type are provided in
        #order to generate ugc code. Returns list of decoded UGC codes.
        #Returns None on failure.
        subtype_search = re.search(r'[0-9][0-9][0-9]', ugc)
        if not subtype_search:
            return None
        decodedUgcs = [(state + code_type + ugc)]
        return decodedUgcs

    def _ugcCaseThree(self, ugc, state, code_type):
        #Decodes the 044>067 case. Current state and code_type are provided
        #in order to generate ugc code.  Returns list of decoded UGC codes.
        #Returns None on failure.
        subtype_search = re.search(r'[0-9][0-9][0-9]>[0-9][0-9][0-9]', ugc)
        if not subtype_search:
            return None

        start_code = int(ugc[0:3])
        end_code = int(ugc[4:7])
        ugcList = []
        for code in xrange(start_code, end_code + 1):
            codeString = "%03i" % code
            ugcList.append(state + code_type + codeString)
        return ugcList


    def _ugcCaseFour(self, ugc):
        #Decodes the WIZ023>056 case. Returns state, code type, and list of
        #decoded UGC codes.  Returns None on failure.
        searchString = r'[A-Z][A-Z][CZ][0-9][0-9][0-9]>[0-9][0-9][0-9]'
        subtype_search = re.search(searchString, ugc)
        if not subtype_search:
            return None

        state = ugc[0:2]
        code_type = ugc[2]
        ugcList = self._ugcCaseThree(ugc[3:10], state, code_type)
        return (state, code_type, ugcList)

    def _calcTime(self, yymmdd, hhmm, allZeroValue):
        #Returns tuple of time, and allZeroFlag. Time is based on the 
        #two time strings.  If all zeros, then return allZeroValue
        if yymmdd == "000000" and hhmm == "0000":
            return (allZeroValue, 1)
        else:
            timeString = yymmdd + hhmm
            timeTuple = time.strptime(timeString, "%y%m%d%H%M")
            return (time.mktime(timeTuple), 0)   #TZ is GMT0, mktime works

    def _expandVTEC(self, ugcs, vtecStrings, segment, segmentText, cities,
                    purgeTime):
        #Routine takes a list of vtec strings and expands them out into
        #the format of the active table.
        #Returns the records.
        records = []
        for vtecS in vtecStrings:
            search = re.search(self._vtecRE, vtecS)

            #construct the active table entries, without the geography
            template = {}
            template['vtecstr'] = search.group(0)
            template['etn'] = int(search.group(5))
            template['sig'] = search.group(4)
            template['phen'] = search.group(3)
            template['text'] = segmentText
            template['overviewText'] = self._overviewText
            template['key'] = template['phen'] + '.' + template['sig']
            template['act'] = search.group(1)
            template['seg'] = segment
            template['startTime'], zeros = self._calcTime(search.group(6),
              search.group(7), self._issueTime)
            template['endTime'], ufn = self._calcTime(search.group(8), 
              search.group(9), self._maxFutureTime)
            if ufn:
                template['ufn'] = 1
            template['officeid'] = search.group(2)
            template['purgeTime'] = purgeTime
            template['issueTime'] = self._issueTime
            template['state'] = "Decoded"
            if self._productPil[:3] in ACCURATE_CITIES_PILS:
                template['cities'] = cities

            #remap pil if in mappedPils table to relate events that are
            #issued in one product, and updated in another product
            template['pil'] = self._remapPil(template['phen'], 
              template['sig'], self._productPil)

            #expand the template out by the ugcs
            for geo in ugcs:
                dict = copy.deepcopy(template)
                dict['id'] = geo
                records.append(dict)

        return records

    def _remapPil(self, phen, sig, pil):
        # remaps the product pil for certain phen/sig/pils.  The VTECDecoder
        # needs to relate hazards through all states from the same pil. Some
        # short-fused hazards issue in one pil and followup/cancel in
        # another pil.
        key = (phen, sig, pil)
        rPil = self._mappedPils.get(key,  pil)
        if rPil != pil:
            LogStream.logEvent("Remapped Pil", key, "->", rPil)
        return rPil
  

    def _prepSegmentText(self, lines):
        # eliminate leading and trailing blank lines from the set of
        # lines, the joins the lines and returns one long string.

        # eliminate leading blank lines
        while len(lines) and len(lines[0]) == 0:
            del lines[0]
        # eliminate trailing blank lines
        while len(lines) and len(lines[-1]) == 0:
            del lines[-1]
        # assemble into long string
        return string.join(lines, '\n')

    def _notifyIFPServerOfChanges(self, changes):
        try:
            if self._notifyGFE:
                LogStream.logDebug("Notifying ifpServer of table change")
                import siteConfig
                host = siteConfig.GFESUITE_SERVER
                port = int(siteConfig.GFESUITE_PORT)
                import PyNet
                c = PyNet.IFPClient((host, port))
                # ???????????????????????????????????????????????????????????????
                # REFACTOR rename self._activeTableFilename to self._vtecRecordsFilename
                c.vtecActiveTableChanged(\
                  os.path.basename(self._vtecRecordsFilename),
                  int(self._time), "VTECDecoder", changes)
                del c                      
        except:
            LogStream.logProblem("Caught Exception: ", LogStream.exc())


    def _createSPCNotification(self, productPil, decodedVTEC, productText):
        #create the appropriate SPC notification, returns None if not
        #needed.
        if productPil[0:3] != "WOU":
            LogStream.logDebug("SPC notification:  not WOU product")
            return None

        #find the first record from KWNS, SV.A, TO.A in this product
        #action code must be "NEW"
        matchRecord = None
        for e in decodedVTEC:
            if e['officeid'] == self._spcSite and \
               e['sig'] == "A" and e['phen'] in ['TO', 'SV'] and \
               e['act'] == "NEW":
                matchRecord = e
                break
        if matchRecord is None:
            LogStream.logDebug("SPC notification:  " + \
              "no SV.A, TO.A vtec lines, or not NEW action code")
            return None

        #decode the ATTN line, which tells us which WFOs are affected
        wfos = self._attnWFOs(productText)
        if self._ourSite not in wfos:
            LogStream.logDebug("SPC notification:  my site not in ATTN list")
            return None   #not my WFO

        #create the message
        if matchRecord['phen'] == 'TO':
            txt = "Tornado Watch"
        elif matchRecord['phen'] == 'SV':
            txt = "Severe Thunderstorm Watch"

        testText = ""
        if matchRecord['vtecstr'][1] == "T":
            testText = " This is a TEST watch. Please restart the GFE " + \
              "in TEST mode before issuing WCN. "
        
        msg = "Alert: " + txt + " " + `matchRecord['etn']` + " has arrived. " + \
              "Check for 'red' locks (owned by others) on your Hazard grid and resolve them. " +\
              "If hazards are separated into temporary grids, please run MergeHazards. " +\
              "Next...save Hazards grid.  Finally, select PlotSPCWatches from the Hazards menu."
        msg = msg + testText
        return msg

    def _createTPCNotification(self, productPil, decodedVTEC, productText):
        #create the appropriate TPC notification, returns None if not
        #needed.
        if productPil[0:3] != "TCV":
            LogStream.logDebug("TPC notification:  not TCV product")
            return None

        #get all VTEC records, assemble unique list of phen/sig and storm#
        phensigStormAct = []   #(phensig, storm#, actionCode)
        for e in decodedVTEC:
            if e['officeid'] == self._tpcSite:
                phensig = e['phen'] + "." + e['sig']
                storm = e['etn']
                act = e['act']
                found = 0
                for p, s, a in phensigStormAct:
                    if phensig == p and storm == s:
                        if act not in a:
                            a.append(act)
                        found = 1
                        break
                     
                if found == 0:
                    phensigStormAct.append((phensig, storm, [act]))


        if len(phensigStormAct) == 0:
            LogStream.logDebug("TPC notification:  " + \
              "no HU/TR vtec lines, or not NEW action code")
            return None

        #decode the ATTN line, which tells us which WFOs are affected
        wfos = self._attnWFOs(productText)
        if self._ourSsite not in wfos:
            LogStream.logDebug("TPC notification:  my site not in ATTN list")
            return None   #not my WFO

        # text mapping
        phensigMap = {'HU.A': "Hurricane Watch", 'HU.S': "Hurricane Local Statement", 'HU.W': "Hurricane Warning",
          'TR.A': "Tropical Storm Watch", 'TR.W': "Tropical Storm Warning"}
        actMap = {'CON': "Continued", 'CAN': "Cancelled", 'NEW': "New"}

        #create the message
        msg = "Alert: " + productPil + " has arrived from TPC. " + \
          "Check for 'red' locks (owned by others) on your Hazard grid and resolve them. " +\
          "If hazards are separated into temporary grids, please run MergeHazards. " +\
          "Next...save Hazards grid. Finally, select PlotTPCEvents from Hazards menu."
        for phensig, storm, act in phensigStormAct:
            t1 = phensigMap.get(phensig, phensig)
            acts = ""
            for a in act:
                a1 = actMap.get(a, a)
                if len(acts):
                    acts = acts + ", " + a1
                else:
                    acts = a1
            msg = msg + t1 + ": #" + `storm` + " (" + acts + "). "

          
        return msg


    def _sendGFENotification(self, msg):
        #sends the gfe notification if msg is not None
        if msg is None:
            return
        LogStream.logEvent("Sending GFE Notification: ", msg)
        os.system("sendGfeMessage -s -c GFE -m '" + msg + "'")

    def _attnWFOs(self, lines):
        #decode the ATTN line, which tells us which WFOs are affected
        #only used for WCL and WOU products
        wfoline = None
        for lineNum in xrange(len(lines)):
            if re.search(r"^ATTN...WFO...", lines[lineNum]):
                wfoline = string.join(lines[lineNum:],'')
                wfoline = string.strip(wfoline)
                wfoline = wfoline[13:]  #eliminate ATTN...WFO...
                break

        if wfoline is not None:
            wfos = string.split(wfoline, '...')
            wfosR = []
            for wfo in wfos:
                wfo = string.strip(wfo)
                if len(wfo) == 3:
                    wfosR.append(wfo)
            return wfosR
        else:
            return None


    def _createWCLNotification(self, completeProductPil, lines):
        #decode the ATTN line, which tells us which WFOs are affected
        wfos = self._attnWFOs(lines)
        if self._ourSite in wfos:
            doNotify = 1
        else:
            doNotify = 0
            LogStream.logDebug("WCL notification:  site not in ATTN list")

        #
        # Decode the WCL
        #

        workingWCLList = lines

        #
        # Throw out every line which is not a UGC line
        #

        ugcComposite = ""
        for eachGroup in workingWCLList:
            ugcCheck = re.search(r'[0-9][0-9][0-9]\-', eachGroup)
            if ugcCheck:
                ugcComposite = ugcComposite + eachGroup
        ugcList = string.split(ugcComposite, '-')

        #
        # Process the list of UGC lines into a list of UGCs in full form
        # matching edit area names
        #

        finalUGCList = []
        state = ''
        for eachUGC in ugcList:
            newgroup = re.search(r'^(([A-Z][A-Z][A-Z])([0-9][0-9][0-9]))$', 
              eachUGC)
            followgroup = re.search(r'^([0-9][0-9][0-9])$', eachUGC)
            if newgroup:
                state = newgroup.group(2)
                finalUGCList.append(newgroup.group(1))
            elif followgroup:
                finalUGCList.append(state + followgroup.group(1))

        #
        # Get the expiration time of the product
        #

        expTime = 0 
        for each_line in workingWCLList:
            expireSearch = re.search(r'([0-9][0-9][0-9][0-9][0-9][0-9])\-', 
              each_line)
            if expireSearch:
                expTime = self._dtgFromDDHHMM(expireSearch.group(1))
                break

        #
        # Get the watch type
        #

        watchType = 'SV.A'
        for each_line in workingWCLList:
            if re.search(r'\.TORNADO WATCH', each_line):
                watchType = 'TO.A'

        #
        # Get the WCL 'letter'
        #
        wclVersion = completeProductPil

        #
        # Create a dummy Procedure for export
        #
        
        wclObj = 'watchType =' + `watchType` + '\nfinalUGCList =' + \
          `finalUGCList` +  '\nexpTime = ' + `expTime` + \
          '\nissueTime = ' + `self._issueTime` + '\n'
        LogStream.logEvent("WCLData: ", wclObj)

        #
        # Write dummy procedure to temp file, use the tempfile facility
        #
        fd, tempFilename = tempfile.mkstemp()
        os.write(fd, wclObj)
        os.close(fd)

        LogStream.logEvent("Placing WCL Procedure Utility in ifpServer ")
        os.system("ifpServerText -s -n " + wclVersion + " -u 'SITE' " + \
          "-c Utility -f " + tempFilename)
        os.remove(tempFilename)

        #
        # Notify GFE
        #

        if self._notifyGFE and doNotify:
            msg = "Alert: " + wclVersion + " has arrived. Please select ViewWCL and use "\
                  + wclVersion + ". (Hazards menu)"
            self._sendGFENotification(msg)
        else:
            LogStream.logEvent("Notification of WCL skipped")
        return

    #convert 3 letter to 4 letter site ids
    def _get4ID(self, id):
        if id in ['SJU']:
            return "TJSJ"
        elif id in ['AFG', 'AJK', 'HFO', 'GUM']:
            return "P" + id
        elif id in ['AER', 'ALU']:
            return "PAFC"
        else:
            return "K" + id

    # time contains, if time range (tr) contains time (t), return 1
    def __containsT(self, tr, t):
        return (t >= tr[0] and t < tr[1])

    # time overlaps, if tr1 overlaps tr2 (adjacent is not an overlap)
    def __overlaps(self, tr1, tr2):
        if self.__containsT(tr2, tr1[0]) or self.__containsT(tr1, tr2[0]):
            return 1
        return 0

    # obtains attributes from configuration files.  All parameters are
    # strings.  Module is the name of the module.  Item is the item within
    # the module.  Default is the value assigned in case the module cannot
    # be imported or the item doesn't exist.
    def _getConfig(self, module, item, default):
        try:
            ret = None
            exec("import " + module)
            exec("ret " + module + "." + item)
            return ret
        except:
            return default


def main():
    LogStream.ttyLogOn()
    try:
        LogStream.logEvent("VTECDecoder Starting")
        try:
            LogStream.logEvent(AFPS.DBSubsystem_getBuildDate(),
              AFPS.DBSubsystem_getBuiltBy(), AFPS.DBSubsystem_getBuiltOn(),
              AFPS.DBSubsystem_getBuildVersion())
        except:
            pass    #for ihis, if no AFPS available
        decoder = VTECDecoder()
        decoder.decode()
        decoder = None
        LogStream.logEvent("VTECDecoder Finished")
    except:
        LogStream.logProblem("Caught Exception: ", LogStream.exc())
        sys.exit(1)

if __name__ == "__main__":
    main()
    sys.exit(0)
