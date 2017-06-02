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

'''

 @since: May 2016
 @author: Raytheon Hazard Services Team

 Description: Utility methods for converting stage and discharge values for
 a river point.
 
 History:
 Date         Ticket#    Engineer    Description
 ------------ ---------- ----------- --------------------------
 May 02, 2016 15584      Kevin.Bisanz Initial Creation: Python version of StageDischargeUtils.java
 May 06, 2016 15584      Kevin.Bisanz StageDischargeUtils.java moved to
                                      gov.noaa.gsd.common.hazards.utilities.hazardservices
'''

from gov.noaa.gsd.common.hazards.utilities.hazardservices import StageDischargeUtils as JStageDischargeUtils

class StageDischargeUtils(object):

    def __init__(self):
        self.jobj = JStageDischargeUtils()

    def stage2discharge(self, lid, stage):
        '''
        Convert a stage value to a discharge value.

        @param lid: The location ID (river station identifier)
        @param stage: Stage value to convert
        @return: A discharge value based on the stage parameter
        '''
        discharge = self.jobj.stage2discharge(lid, stage)

        return discharge

    def discharge2stage(self, lid, discharge):
        '''
        Convert a discharge value to a stage value.

        @param lid: The location ID (river station identifier)
        @param discharge: Discharge value to convert
        @return: A stage value based on the discharge parameter
        '''
        stage = self.jobj.discharge2stage(lid, discharge)

        return stage