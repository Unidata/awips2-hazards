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
# A very simple recommender that demonstrates how recommenders should be used.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/22/13                      mnash       Initial Creation.
#    
# 
#

import datetime
import EventFactory
import GeometryFactory
import RecommenderTemplate
import numpy

from ufpy.dataaccess import DataAccessLayer

class Recommender(RecommenderTemplate.Recommender):
    
    def getScriptMetadata(self):
        metadata = {}
        metadata["author"] = "Jonathan Sanchez"
        metadata["description"] = "A simple recommender to show how recommenders should be used."
        metadata["version"] = "1.0"
        metadata["productsgenerated"] = ["TO.W", "TO.A", "SV.W", "SV.A"]
        return metadata
    
    def defineDialog(self):
        print "Dialog info is not necessary for this recommender."
        return
    
    def defineSpatialInfo(self):
        print "Spatial info is not necessary for this recommender."
        return
    
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        req = DataAccessLayer.newGridRequest()
        req.setDatatype("radar")
        req.setParameters("Z")
        req.addIdentifier("icao", "kdmx")
        times = DataAccessLayer.getAvailableTimes(req)
        data = DataAccessLayer.getGridData(req, times)
        latlons = DataAccessLayer.getLatLonCoords(req)
        
        max = numpy.max(latlons[1][1:]-latlons[1][:-1])
        high = 38
    
        rawdata = data[0].getRawData('dBZ')
        finalVals = list()
        import time
        start = time.time()
        for i in range(len(rawdata)):
            for j in range(len(rawdata[i])):
                if rawdata[i][j] > high:
                    finalVals.append([latlons[1][i][j], latlons[0][i][j]])
        print "time to loop over data :", time.time() - start
        event = EventFactory.createEvent()
        event.setSite("koax")
        event.setHazardState("PENDING")
        event.setPhenomenon("FZ")
        event.setSignificance("WARNING")
        d = datetime.datetime.now()
        event.setIssueTime(d)
        event.setEndTime(d)
        event.setStartTime(d)
        event.setHazardMode("OPERATIONAL")
        start = time.time()
        geom = GeometryFactory.createMultiPoint(finalVals)
        print "time to create multi point", time.time() - start
        start = time.time()
        geom = geom.buffer(max * 2.5)
        print "time to buffer point", time.time() - start
        event.setGeometry(geom)
        return event
        
