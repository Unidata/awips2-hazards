#-----------------------------------------------------------------
# VTEC Engine Wrapper
#-----------------------------------------------------------------
#
# Software History:
#  Date         Ticket#    Engineer    Description
#  ------------ ---------- ----------- --------------------------
#  Feb 27, 2014  #2826     dgilling     Updates for refactored VTECTableIO.
#
#

from VTECEngine import VTECEngine
from VTECEngine import VTECDefinitions
from VTECIngester import VTECIngester
import os,sys

# VTEC HazardType upgrade and downgrade table.
import HazardUpgradeDowngrade as UpDown

import json
import VTECTableIO


# NOTE: VERY IMPORTANT.  The VTEC code must be run with the time zone of GMT0
# to ensure all calculations are not done in local time.  This is accomplished
# outside of this routine by setting the environment variable for TZ to GMT0.

# HazardEvent rules:
# 1) All hazardEvents sent to the engine must use the same "combinable segments"
# type for their hazards, as defined by the HazardTypes dictionary.
#
# 2) All hazardEvents sent to the engine must use the same "geoType" for their
# hazards, as defined within the HazardEvents.
#
# 3) All hazardEvents sent to the engine should have the state of 'issued' or
# 'ended'.  If other states are passed then it is very important not to call
# the VTECIngester through the interface (by using mergeResults).
#
# 4) For long-fused, i.e., combinableSegment hazards, all hazards pertaining
# to the product must be sent at the same time to the engine to prevent
# inadvertent cancellation of ongoing hazards.
#
# 5) For short-fused and most hydrological hazards, where the combinableSegment
# is False, either individual or sets of hazards may be sent to the engine.
# The engine will only process those hazards that are presented and will 
# ignore and not calculate/report on other hazards.  This allows individual
# warnings to be generated, such as Tornado Warnings, and the ability to
# cancel/update more than one warning at a time depending upon the number
# of event dictionary entries that are provided.
#
# 6) Events will generate EXP action codes if the event is active and within 
# the expiration time limits based on the hazard type. If the user specifies
# the state of 'ended', then that will result in a cancel of the event
# if it occurs before the ending time of the event.

# 7) If user tries to change the area or time for a hazard which does not allow
# such an operation, then a new HazardEvent / eventID will be generated.  

#------------------------------------------------------------------
#  VTEC Engine Wrapper
#------------------------------------------------------------------
# This is the object that is normally instantiated.  Its main purpose is
# to access the information required to run the VTECEngine and to
# provide all i/o needed.
#------------------------------------------------------------------
class VTECEngineWrapper(object):
    def __init__(self, bridge, productCategory, siteID4, hazardEvents = [],
      vtecMode='O', issueTime=None, limitGeoZones=None, operationalMode=True,
      testHarnessMode=False, vtecProduct=True):
        '''Constructor for VTEC Engine Wrapper
        Once instantiated, it will run the VTEC Engine.  Then the user can
        access the output through different functions.

        Keyword Arguments:
        bridge -- 
        productCategory -- identifier for the product, which must match a 
         key in the ProductGeneratorTable.
        siteID4 -- identifier for the site, such as KBOU
        hazardEvents -- list of hazard events
        vtecMode -- 'O' for operational product, 'T' for test product,
          'E' for experimental product, 'X' for Experimental VTEC in an
          operational product.
        issueTime -- time the engine is run, a.k.a. issue time.  Units of
          milliseconds since epoch (Jan 1 1970 00:00Z)
        limitGeoZones -- A list of zones used to limit the vtec logic.  This is
          only used in places where there are multiple products for the same
          hazard types issued at an office.  Example: PAFG (Fairbanks, AK).
        '''
        
        # If running the test harness, use "test" vtecRecords rather than "live" ones
        if operationalMode:
            self.vtecRecordType = "vtecRecords"
        else:
            self.vtecRecordType = "testVtecRecords"
            
        self.vtecProduct = vtecProduct
            
        # Access to VTEC Table and VTEC records
        self.bridge = bridge
        self._io = VTECTableIO.getInstance(self.vtecRecordType=="vtecRecords", testHarnessMode)
        vtecRecords = self._io.getVtecRecords() if self.vtecProduct else []
        
        if self.bridge is not None:
            
            # Hazard Types
            self.hazardTypes = self.bridge.getHazardTypes() 
            # ProductGeneratorTable
            ProductGeneratorTable = json.loads(self.bridge.getProductGeneratorTable())
        else: # Testing
            from LocalizationInterface import LocalizationInterface
            localizationInterface = LocalizationInterface("")  
            execString = localizationInterface.getLocFile(
                    "hazardServices/hazardTypes/HazardTypes.py", "COMMON_STATIC", "Base")
            exec execString
            self.hazardTypes = HazardTypes            
            ProductGeneratorTable = localizationInterface.getLocFile(
                    "hazardServices/productGeneratorTable/ProductGeneratorTable.py",  
                    "COMMON_STATIC", "Base")
            exec ProductGeneratorTable

        self._issueTime = issueTime

        # Get the list of allowedHazards from the ProductGeneratorTable
        try:
            pgtKey = "{p}_ProductGenerator".format(p=productCategory)
            allowedHazards = ProductGeneratorTable[pgtKey]['allowedHazards']
        except:
            pgtKey = "{p}_Tool".format(p=productCategory)
            allowedHazards = ProductGeneratorTable[pgtKey]['allowedHazards']
            

        # Assemble a tuple containing the hazardTypes, upgrade table,
        # and downgrade table.
        vtecDefinitions = VTECDefinitions(hazards=self.hazardTypes,
          upgradeDef=UpDown.upgradeHazardsDict,
          downgradeDef=UpDown.downgradeHazardsDict)
        
        # instantiate the actual vtec engine
        self._engine = VTECEngine(productCategory, siteID4, hazardEvents,
          vtecRecords, vtecDefinitions, allowedHazards, vtecMode,
          issueTime, limitGeoZones)

    def engine(self):
        '''Returns the VTECEngine object, used for access to information'''
        return self._engine

    def mergeResults(self):
        '''Merges the VTECEngine results with the existing vtec records'''
        # TODO: This method needs to be an atomic transaction
        if not self.vtecProduct: 
            return        
        vtecDicts = self._engine.analyzedTable()

        # Note that we read the vtec records again.  This may or may not be necessary
        # but if there was any actions that occurred between the first read when the
        # VTECEngineWrapper was instantiated, until the time mergeResults() took
        # place, then by reusing the records previously read may result in 
        # missing any changes to the vtec records that has occurred.
        criteria = {"lock": "True"}
        vtecRecords = self._io.getVtecRecords(criteria)
       
        # Instantiate the ingester, to merge the calculated with vtec database
        ingester = VTECIngester()
        ingester.ingestVTEC(vtecDicts, vtecRecords, self._issueTime)
        mergedRecords = ingester.mergedVtecRecords()

        criteria = {"lock": "False"}
        self._io.putVtecRecords(mergedRecords, criteria)

    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()

