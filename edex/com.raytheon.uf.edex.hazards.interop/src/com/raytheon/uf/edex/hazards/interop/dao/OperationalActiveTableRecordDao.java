package com.raytheon.uf.edex.hazards.interop.dao;

import com.raytheon.uf.common.activetable.ActiveTableRecord;
import com.raytheon.uf.common.activetable.OperationalActiveTableRecord;

public class OperationalActiveTableRecordDao extends
        AbstractActiveTableDao<OperationalActiveTableRecord> {

    @Override
    protected Class<ActiveTableRecord> getEntityClass() {
        return ActiveTableRecord.class;
    }

}
