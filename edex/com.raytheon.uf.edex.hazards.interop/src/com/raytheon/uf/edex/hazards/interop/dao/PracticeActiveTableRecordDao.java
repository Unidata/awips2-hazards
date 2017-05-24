package com.raytheon.uf.edex.hazards.interop.dao;

import com.raytheon.uf.common.activetable.ActiveTableRecord;
import com.raytheon.uf.common.activetable.PracticeActiveTableRecord;

public class PracticeActiveTableRecordDao extends
        AbstractActiveTableDao<PracticeActiveTableRecord> {

    @Override
    protected Class<ActiveTableRecord> getEntityClass() {
        return ActiveTableRecord.class;
    }
}
