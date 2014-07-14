"""This module is responsible for determining which ETN assignment behavior 
is utilized by Hazard Services' VTECEngine.py.

    @author David Gillingham

"""

#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/30/14        2462          dgilling       Initial Creation.    
#
#


import time

USE_NEW_STYLE_VTEC = True
try:
    from com.raytheon.uf.common.hazards.productgen.vtec import ProductGenVtecUtil
except ImportError:
    USE_NEW_STYLE_VTEC = False


class ProductGenEtnProvider(object):
    
    def __init__(self, siteId, currentTime, tpcKeys, tpcBaseETN):
        self.__siteID4 = str(siteId)
        self.__time = currentTime
        self.__tpcKeys = tpcKeys
        self.__tpcBaseETN = tpcBaseETN
        self.__currentYear = time.gmtime(self.__time).tm_year

    def getLastETN(self, phen, sig, vtecRecords):
        """Returns the maximum etn used for the given phen/sig for the 
        current year, given the set of vtec records (not proposed records).

        Keyword Arguments:
        phen -- vtec phen code, e.g., TO
        sig -- vtec significance code, e.g., W

        Returns the max etn used.  If not yet used, 0 is returned.
        
        """
        etn = 0
        if USE_NEW_STYLE_VTEC:
            etn = self.__newStyleETN(phen, sig)
        else:
            etn = self.__legacyETN(phen, sig, vtecRecords)
        return etn
    
    def __newStyleETN(self, phen, sig):
        etn_base = 0
        phensig = (phen, sig)
        
        # For tropical cyclone products, ETN will be provided by the TCV
        # issued by the relevant national center
        # UNLESS, your site is PGUM, then you issue all ETNs locally
        if phensig not in self.__tpcKeys or self.__siteID4 == 'PGUM':
            etn_base = ProductGenVtecUtil.getLastEtn(self.__siteID4, '.'.join(phensig))

        return etn_base

    def __legacyETN(self, phen, sig, vtecRecords):
        etn_base = 0 
        
        #check active table for highest etn for this year
        for active in vtecRecords:
            # find only records with
            # 1. same phen and sig
            # 2. in the present year
            # and not from the national center
            activeyear = time.gmtime(active['issueTime']).tm_year
            phensig = (active['phen'],active['sig'])
            if active['phen'] == phen and active['sig'] == sig and \
              activeyear == self.__currentYear:
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
                if active['etn'] > etn_base and phensig not in self.__tpcKeys:
                    etn_base = active['etn']
                elif active['etn'] > etn_base and phensig in self.__tpcKeys:
                    if self.__siteID4 == 'PGUM':
                        # GUM uses their own ETNs regardless of hazard
                        etn_base = active['etn']
                    elif active['etn'] <= self.__tpcBaseETN:  # is WFO etn
                        etn_base = active['etn']

        return etn_base
        