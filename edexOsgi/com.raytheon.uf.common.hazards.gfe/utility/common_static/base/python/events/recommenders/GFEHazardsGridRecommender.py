# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
#
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
#
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
#
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
# #


#
# A recommender that takes a GFE hazards grid and outputs IHazardEvent objects
#
#
#     SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    05/28/13                      mnash       Initial Creation.
#
#
#

import datetime

import EventFactory
import RecommenderTemplate
import JUtil
import GridHelper
import numpy
import ContourUtil
import datetime

from ufpy.dataaccess import DataAccessLayer

class Recommender(RecommenderTemplate.Recommender):

    def defineScriptMetadata(self):
        metadata = {}
        metadata["author"] = "Raytheon"
        metadata["description"] = "Creates hazards out of the GFE Hazards Grid."
        metadata["version"] = "1.0"
        metadata["productsgenerated"] = ['GFE Hazards']
        return metadata

    def defineDialog(self):
        # None, not necessary for this recommender
        return

    def defineSpatialInfo(self):
        # None, not necessary for this recommender
        return

    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        # TODO maybe recommenders shouldn't ever set the mode... why would they?
        # TODO modification should be made here based on items passed in
        # site = eventSet['site']
        site = "OAX"
        mode = "OPERATIONAL"
        hazardState = "PROPOSED"
        
        # setup the data request
        req = DataAccessLayer.newDataRequest()
        req.setDatatype("gfe")
        req.setParameters("Hazards")
        req.addIdentifier("modelName", "Fcst")
        req.addIdentifier("siteId", site)
        
        times = DataAccessLayer.getAvailableTimes(req)
        # TODO we should only request a certain set of times
        # from the eventSet maybe?
        # for now this will do ALL grids, but the logic for the rest still says the same
        grids = DataAccessLayer.getGridData(req, times)
        for data in grids :
            keys = GridHelper.convertKeysToList(data.getAttribute("keys"))
            values = GridHelper.getValuesToSearch(keys)

            # TODO, will become an EventSet
            events = list()

            # have the grid, now I need to contour that stuff
            # for each phensig, possibly creating multiple hazard events
            rawdata = data.getRawData()
            rawdata = numpy.array(rawdata)
            lons, lats = data.getLatLonCoords()
            for val in values.keys() :
                phensig = val.split(".")
                if len(phensig) != 2 :
                    continue

                mp = ContourUtil.contourMultipleValues(lons, lats, rawdata, values[val])

                # take the contoured values and create events
                event = EventFactory.createEvent()
                event.setSiteID(site)
                event.setHazardState(hazardState)
                event.setPhenomenon(phensig[0].upper())
                event.setSignificance(phensig[1].upper())
                vPeriod = data.getDataTime().getValidPeriod()
                startTime = datetime.datetime.fromtimestamp(vPeriod.startTime().unixTime())
                endTime = datetime.datetime.fromtimestamp(vPeriod.endTime().unixTime())
                # we don't set an issue time as this is only taking the grids
                # and putting them to "PROPOSED", not "ISSUED"
                event.setEndTime(endTime)
                event.setStartTime(startTime)
                event.setHazardMode(mode)
                event.setGeometry(mp)
                
                # TODO, once this is an EventSet
                # events.add(event)
                events.append(event)
            return events
        # return no events, will this happen?  I'm pretty sure this will not happen for this recommender.
        return None

