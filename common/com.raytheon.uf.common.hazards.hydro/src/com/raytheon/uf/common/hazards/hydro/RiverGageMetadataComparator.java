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
package com.raytheon.uf.common.hazards.hydro;

import java.util.Comparator;

/**
 * Comparator to sort by river gages by the ordinal value.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 7, 2017   28946     mpduff      Initial creation
 *
 * </pre>
 *
 * @author mpduff
 */

public class RiverGageMetadataComparator
        implements Comparator<RiverGageMetadata> {

    @Override
    public int compare(RiverGageMetadata o1, RiverGageMetadata o2) {
        return o1.getOrdinal() < o2.getOrdinal() ? -1
                : o1.getOrdinal() == o2.getOrdinal() ? 0 : 1;
    }
}
