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
package com.raytheon.uf.edex.hazards.interop.gfe;

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.time.TimeRange;

/**
 * A POJO that identifies which GFERecords need just be ingested and what
 * discrete grid slices need to be merged.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 30, 2013 2277       jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class SeparatedRecords {

    /**
     * List of new GFERecords that need to be ingested in the gfe table but do
     * not need a merge
     */
    public List<GFERecord> newRecords;

    /**
     * Map of a list of discrete grid slices that need to be merged to create a
     * GFERecord.
     */
    public Map<TimeRange, List<DiscreteGridSlice>> slicesToMerge;

    /**
     * List of time ranges of GFE Records to remove.
     */
    public List<TimeRange> timeRangesToRemove;

}
