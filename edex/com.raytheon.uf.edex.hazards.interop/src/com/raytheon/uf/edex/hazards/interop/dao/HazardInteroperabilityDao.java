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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.raytheon.uf.common.activetable.ActiveTableKey;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;
import com.raytheon.uf.edex.database.dao.SessionManagedDao;
import com.raytheon.uf.edex.hazards.interop.InteroperabilityUtil;

/**
 * Data Access object for interacting with interoperability objects
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
public class HazardInteroperabilityDao extends
        SessionManagedDao<Integer, HazardInteroperabilityRecord> {

    @Override
    protected Class<HazardInteroperabilityRecord> getEntityClass() {
        return HazardInteroperabilityRecord.class;
    }

    @Override
    public void create(HazardInteroperabilityRecord record) {
        record.setCreationDate(new Date());
        super.create(record);
    }

    /**
     * Gets all interoperability records with the given site ID
     * 
     * @param siteID
     *            The site ID
     * @return All Interoperability records with the given site ID
     */
    public List<HazardInteroperabilityRecord> getBySiteID(String siteID) {
        return this.executeCriteriaQuery(InteroperabilityUtil.getCriteriaQuery(
                getEntityClass(), "siteID", siteID));
    }

    /**
     * Gets all interoparability records with the given hazard event ID
     * 
     * @param id
     *            The hazard event ID
     * @return All interoperability records with the given hazard event ID
     */
    public List<HazardInteroperabilityRecord> getByHazardEventID(int id) {
        return this.executeCriteriaQuery(InteroperabilityUtil.getCriteriaQuery(
                getEntityClass(), "hazardEventID", id));
    }

    /**
     * Gets all interoperability records with the given active table keys
     * 
     * @param activeTableIds
     *            Collection of active table keys
     * @return
     */
    public List<HazardInteroperabilityRecord> getByActiveTableID(
            Collection<ActiveTableKey> activeTableIds) {
        List<HazardInteroperabilityRecord> interopRecords = executeCriteriaQuery(InteroperabilityUtil
                .getCriteriaQuery(getEntityClass(), "activeTableEventID",
                        activeTableIds));
        return interopRecords;
    }

    /**
     * Gets all interoperability records of the given type
     * 
     * @param type
     *            The type of interoperability record
     * @return All interoperability records of the given type
     */
    public List<HazardInteroperabilityRecord> getByInteroperabilityType(
            INTEROPERABILITY_TYPE type) {
        return this.executeCriteriaQuery(InteroperabilityUtil.getCriteriaQuery(
                getEntityClass(), "interoperabilityType", type));
    }

}
