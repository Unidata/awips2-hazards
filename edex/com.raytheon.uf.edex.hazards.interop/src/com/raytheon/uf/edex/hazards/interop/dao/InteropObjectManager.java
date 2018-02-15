/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.hazards.interop.dao;

import com.raytheon.uf.common.activetable.ActiveTableRecord;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;

/**
 * Manager object used for retrieving interoperability related data access
 * objects
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class InteropObjectManager {

    /** Interoperability record data access object */
    private HazardInteroperabilityDao interopDao;

    /** Operational active table dao */
    private OperationalActiveTableRecordDao operationalActiveTableRecordDao;

    /** Practice active table dao */
    private PracticeActiveTableRecordDao practiceActiveTableRecordDao;

    /**
     * Creates a new InteropObjectManager
     */
    protected InteropObjectManager() {

    }

    /**
     * Creates a new HazardEvent
     * 
     * @return New HazardEvent
     */
    public HazardEvent createHazardEvent() {
        return new HazardEvent(false);
    }

    /**
     * Creates an interoperability record
     * 
     * @param practice
     *            The mode, practice or operational
     * @param interoperabilityType
     *            The origin of the record
     * @param hazardEvent
     *            The hazard event associated with the interoperability record
     * @param activeTableRecord
     *            THe active table record associated with the interoperability
     *            record
     * @return The new HazardInteroperability record
     */
    public HazardInteroperabilityRecord createInteroperabilityRecord(
            boolean practice, INTEROPERABILITY_TYPE interoperabilityType,
            IHazardEvent hazardEvent, ActiveTableRecord activeTableRecord) {
        HazardInteroperabilityRecord interopRecord = null;

        // FIXME: active table record is no longer an int
        // interopRecord = new HazardInteroperabilityRecord(
        // activeTableRecord.getXxxid(), hazardEvent.getEventID(),
        // activeTableRecord.getId(), interoperabilityType);
        interopRecord.setPractice(practice);

        return interopRecord;
    }

    public AbstractActiveTableDao getActiveTableDao(boolean practice) {
        return practice ? practiceActiveTableRecordDao
                : operationalActiveTableRecordDao;
    }

    /**
     * @param operationalActiveTableRecordDao
     *            the operationalActiveTableRecordDao to set
     */
    public void setOperationalActiveTableRecordDao(
            OperationalActiveTableRecordDao operationalActiveTableRecordDao) {
        this.operationalActiveTableRecordDao = operationalActiveTableRecordDao;
    }

    /**
     * @param practiceActiveTableRecordDao
     *            the practiceActiveTableRecordDao to set
     */
    public void setPracticeActiveTableRecordDao(
            PracticeActiveTableRecordDao practiceActiveTableRecordDao) {
        this.practiceActiveTableRecordDao = practiceActiveTableRecordDao;
    }

    /**
     * @return the interopDao
     */
    public HazardInteroperabilityDao getInteropDao() {
        return interopDao;
    }

    /**
     * @param interopDao
     *            the interopDao to set
     */
    public void setInteropDao(HazardInteroperabilityDao interopDao) {
        this.interopDao = interopDao;
    }

}
