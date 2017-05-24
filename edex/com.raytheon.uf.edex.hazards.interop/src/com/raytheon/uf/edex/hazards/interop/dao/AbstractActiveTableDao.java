package com.raytheon.uf.edex.hazards.interop.dao;

import java.util.Collection;
import java.util.List;

import com.raytheon.uf.common.activetable.ActiveTableKey;
import com.raytheon.uf.common.activetable.ActiveTableRecord;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.edex.database.dao.SessionManagedDao;
import com.raytheon.uf.edex.hazards.interop.InteroperabilityUtil;

public abstract class AbstractActiveTableDao<ENTITY extends ActiveTableRecord>
        extends SessionManagedDao<ActiveTableKey, ActiveTableRecord> {

    public List<ENTITY> getByActiveTableRecordID(Collection<Integer> ids) {
        return executeCriteriaQuery(InteroperabilityUtil.getCriteriaQuery(
                getEntityClass(), "id", ids));
    }

    public ENTITY getByActiveTableRecordID(int id) {
        List<ENTITY> result = executeCriteriaQuery(InteroperabilityUtil
                .getCriteriaQuery(getEntityClass(), "id", id));
        return CollectionUtil.isNullOrEmpty(result) ? null : result.get(0);
    }

    public List<ENTITY> getBySiteID(String siteID) {
        List<ENTITY> result = executeCriteriaQuery(InteroperabilityUtil
                .getCriteriaQuery(getEntityClass(), "", siteID));
        return result;
    }

    public List<ENTITY> getBySiteIDEtnPhenSig(String siteID, String etn,
            String phen, String sig) {
        List<ENTITY> result = executeCriteriaQuery(InteroperabilityUtil
                .getCriteriaQuery(getEntityClass(),"xxxid", siteID, "etn", etn,
                "phen", phen, "sig", sig));
        return result;
    }

}
