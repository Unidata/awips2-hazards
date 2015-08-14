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
 */
package com.raytheon.uf.common.hazards.ihfs;

import com.raytheon.uf.common.hazards.ihfs.table.AbstractQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.AgriculturalQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.ContingencyValueQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.CountiesQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.CrestQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.CurPcQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.CurPpQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.DischargeQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.EvaporationQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FcstDischargeQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FcstHeightQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FcstOtherQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FcstPrecipQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FcstTemperatureQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FishCountQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FloodStmtQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.FpInfoQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.GateDamQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.GroundQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.HeightQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.HsaQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.IceQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.LakeQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.LatestObsValueQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.LocAreaQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.LocationQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.MoistureQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.PowerQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.RawPcQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.RawPpQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.RiverStatQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.RiverStatusQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.RpfFcstGroupQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.SnowQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.StateQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.TemperatureQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.WeatherQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.WfoQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.WindQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.YuniqueQueryTable;
import com.raytheon.uf.common.hazards.ihfs.table.ZoneNumQueryTable;

/**
 * Factory to retrieve the instance of a given Query Table by name.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Add LATESTVALUEOBS Table
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public final class IhfsQueryTableFactory {

    public static AbstractQueryTable getIhfsQueryTable(String queryTableName)
            throws IhfsDatabaseException {
        if ((queryTableName != null) && (queryTableName.isEmpty() == false)) {
            queryTableName = queryTableName.trim().toUpperCase();
            switch (queryTableName) {
            case AgriculturalQueryTable.AgriculturalTableName:
                return (AgriculturalQueryTable.getInstance());
            case ContingencyValueQueryTable.ContingencyValueTableName:
                return (ContingencyValueQueryTable.getInstance());
            case CountiesQueryTable.CountiesTableName:
                return (CountiesQueryTable.getInstance());
            case CrestQueryTable.CrestTableName:
                return (CrestQueryTable.getInstance());
            case CurPcQueryTable.CurPcTableName:
                return (CurPcQueryTable.getInstance());
            case CurPpQueryTable.CurPpTableName:
                return (CurPpQueryTable.getInstance());
            case DischargeQueryTable.DischargeTableName:
                return (DischargeQueryTable.getInstance());
            case EvaporationQueryTable.EvaporationTableName:
                return (EvaporationQueryTable.getInstance());
            case FcstDischargeQueryTable.FcstDischargeTableName:
                return (FcstDischargeQueryTable.getInstance());
            case FcstHeightQueryTable.FcstHeightTableName:
                return (FcstHeightQueryTable.getInstance());
            case FcstOtherQueryTable.FcstOtherTableName:
                return (FcstOtherQueryTable.getInstance());
            case FcstPrecipQueryTable.FcstPrecipTableName:
                return (FcstPrecipQueryTable.getInstance());
            case FcstTemperatureQueryTable.FcstTemperatureTableName:
                return (FcstTemperatureQueryTable.getInstance());
            case FishCountQueryTable.FishcountTableName:
                return (FishCountQueryTable.getInstance());
            case FloodStmtQueryTable.FloodStmtTableName:
                return (FloodStmtQueryTable.getInstance());
            case FpInfoQueryTable.FpInfoTableName:
                return (FpInfoQueryTable.getInstance());
            case GateDamQueryTable.GateDamTableName:
                return (GateDamQueryTable.getInstance());
            case GroundQueryTable.GroundTableName:
                return (GroundQueryTable.getInstance());
            case HeightQueryTable.HeightTableName:
                return (HeightQueryTable.getInstance());
            case HsaQueryTable.HsaTableName:
                return (HsaQueryTable.getInstance());
            case IceQueryTable.IceTableName:
                return (IceQueryTable.getInstance());
            case LakeQueryTable.LakeTableName:
                return (LakeQueryTable.getInstance());
            case LatestObsValueQueryTable.LatestObsValueTableName:
                return (LatestObsValueQueryTable.getInstance());
            case LocAreaQueryTable.LocAreaTableName:
                return (LocAreaQueryTable.getInstance());
            case LocationQueryTable.LocationTableName:
                return (LocationQueryTable.getInstance());
            case MoistureQueryTable.MoistureTableName:
                return (MoistureQueryTable.getInstance());
            case PowerQueryTable.PowerTableName:
                return (PowerQueryTable.getInstance());
            case RawPcQueryTable.RawPcTableName:
                return (RawPcQueryTable.getInstance());
            case RawPpQueryTable.RawPpTableName:
                return (RawPpQueryTable.getInstance());
            case RiverStatQueryTable.RiverStatTableName:
                return (RiverStatQueryTable.getInstance());
            case RiverStatusQueryTable.RiverStatusTableName:
                return (RiverStatusQueryTable.getInstance());
            case RpfFcstGroupQueryTable.RpfFcstGroupTableName:
                return (RpfFcstGroupQueryTable.getInstance());
            case SnowQueryTable.SnowTableName:
                return (SnowQueryTable.getInstance());
            case StateQueryTable.StateTableName:
                return (StateQueryTable.getInstance());
            case TemperatureQueryTable.TemperatureTableName:
                return (TemperatureQueryTable.getInstance());
            case WeatherQueryTable.WeatherTableName:
                return (WeatherQueryTable.getInstance());
            case WfoQueryTable.WfoTableName:
                return (WfoQueryTable.getInstance());
            case WindQueryTable.WindTableName:
                return (WindQueryTable.getInstance());
            case YuniqueQueryTable.YuniqueTableName:
                return (YuniqueQueryTable.getInstance());
            case ZoneNumQueryTable.ZoneNumTableName:
                return (ZoneNumQueryTable.getInstance());
            }
        }
        throw (new IhfsDatabaseException(
                "Invalid or Unsupported IHFS Table Name: " + queryTableName));
    }

}
