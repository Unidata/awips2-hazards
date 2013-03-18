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
import JUtil

from ufpy.dataaccess import DataAccessLayer

class Recommender(RecommenderTemplate.Recommender):
    
    def defineScriptMetadata(self):
        metadata = {}
        metadata["author"] = "Matt Nash"
        metadata["description"] = "A simple recommender to show how to get grid temperatures below certain values."
        metadata["version"] = "1.0"
        metadata["productsgenerated"] = ["FZ.W"]
        return metadata
    
    def defineDialog(self):
        print "Dialog info is not necessary for this recommender."
        return
    
    def defineSpatialInfo(self):
        print "Spatial info is not necessary for this recommender."
        return
    
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        req = DataAccessLayer.newDataRequest()
        req.setDatatype("grid")
        req.setParameters("T")
        req.setLevels("2FHAG")
        req.addIdentifier("info.datasetId", "GFS212")
        times = DataAccessLayer.getAvailableTimes(req)
        data = DataAccessLayer.getGridData(req, times)
        max = 0
        finalVals = None
        if len(data) > 0 :
            finalVals = list()
            latlons = data[0].getLatLonCoords()
            max = numpy.max(latlons[1][1:] - latlons[1][:-1])
            high = -10
    
            rawdata = data[0].getRawData('C')
            for i in range(len(rawdata)):
                for j in range(len(rawdata[i])):
                    if rawdata[i][j] < high:
                        finalVals.append([latlons[1][i][j], latlons[0][i][j]])
                        
        event = EventFactory.createEvent()
        event.setSiteID("koax")
        event.setHazardState("PENDING")
        event.setPhenomenon("FZ")
        event.setSignificance("WARNING")
        d = datetime.datetime.now()
        event.setIssueTime(d)
        event.setEndTime(d)
        event.setStartTime(d)
        event.setHazardMode("OPERATIONAL")
        geom = GeometryFactory.createMultiPoint(finalVals)
        geom = geom.buffer(max)
        event.setGeometry(geom)
        return event
        
