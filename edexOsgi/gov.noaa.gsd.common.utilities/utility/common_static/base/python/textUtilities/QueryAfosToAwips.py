"""
Description: Provides an interface to the contents of the
afos_to_awips table in the fxatext database.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
April 29, 2013            James Ramer      Initial creation
May 6, 2013               Bryon Lawrence   Removed test for 
                                           less than 9 character
                                           product id. Sometimes
                                           these can be 8.
@author James.E.Ramer@noaa.gov
@version 1.0
"""
from LocalizationInterface import *

A2AData = None
SiteData = None

class QueryAfosToAwips() :

    def initA2AData(self) :
        global A2AData
        global SiteData
        A2AData = {}
        SiteData = {}

        # To do this as user awips, we have to submit this to the uEngine.
        myLI = LocalizationInterface("")
        psqlcmd = "psql -a -c #select * from afos_to_awips ;# fxatext "
        result = myLI.submitCommand(psqlcmd, "postgres")
        result = result.split("\n")
        print "AfosToAwips result from table", result
        for oneline in result :
            parts = oneline.split("|")
            if len(parts) < 3 :
                continue
            cccnnnxxx = parts[0].strip()
            wmosite = parts[1].strip()
            wmoprod = parts[2].strip()
            if len(wmosite) < 4 or len(wmoprod) < 5 :
                continue
            if wmosite[0]!="K" and wmosite[0]!="T" and wmosite[0]!="P" :
                continue
            ccc = cccnnnxxx[:3]
            xxx = cccnnnxxx[6:]
            nnn = cccnnnxxx[3:6]
            nnnEntry = A2AData.get(nnn)
            if nnnEntry == None :
                nnnEntry = {}
                A2AData[nnn] = nnnEntry
            nnnEntry[xxx] = { "wmoID" : wmoprod, "pil" : cccnnnxxx[3:], \
                              "awipsWANPil" : wmosite+cccnnnxxx[3:],
                              "textdbPil" : cccnnnxxx }
            if not xxx in SiteData:
                if nnn=="SVR" or nnn=="HYD" or xxx=="GUM" :
                    SiteData[xxx] = { "CCC" : ccc, "WMO" : wmosite }

        return

    def getWMOsite(self, site) :
        global SiteData
        if SiteData == None :
            self.initA2AData()
        siteEntry = SiteData.get(site, {})
        return siteEntry.get("WMO", "K"+site)

    def getCCC(self, site) :
        global SiteData
        if SiteData == None :
            self.initA2AData()
        siteEntry = SiteData.get(site, {})
        return siteEntry.get("CCC", "")

    def getNNNXXXinfo(self, nnn, site) :
        global A2AData
        if A2AData == None :
            self.initA2AData()
        return A2AData.get(nnn, {}).get(site, {})
