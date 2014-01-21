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
#    01/27/14                      bkowal         Initial Creation.
#    
# 
#

import datetime

import EventFactory
import RecommenderTemplate
import HazardEvent
import GeometryFactory

class Recommender(RecommenderTemplate.Recommender):
    
    def defineScriptMetadata(self):
        metadata = {}
        metadata["author"] = "Bryan Kowal"
        metadata["version"] = "1.0"
        return metadata
    
    def defineDialog(self):
        dialogInfo = {}
        dialogInfo["test"] = "TEST BASE"
        return dialogInfo
    
    def defineSpatialInfo(self):
        spatialInfo = {}
        spatialInfo["test"] = "TEST BASE"
        return spatialInfo
    
    def execute(self, eventSet, dialogInputMap, spatialInputMap):
        event = EventFactory.createEvent()
        event.setSiteID("koax")
        event.setHazardState("PENDING")
        event.setPhenomenon("FZ")
        event.setSignificance("WARNING")
        d = datetime.datetime.now()
        event.setIssueTime(d)
        event.setEndTime(d)
        event.setStartTime(d)
        event.setHazardMode("O")
        geom = GeometryFactory.createPoint((122, 42))
        event.setGeometry(geom)
        return event
