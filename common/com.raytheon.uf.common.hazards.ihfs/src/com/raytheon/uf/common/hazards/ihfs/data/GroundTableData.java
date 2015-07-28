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
package com.raytheon.uf.common.hazards.ihfs.data;

import com.raytheon.uf.common.hazards.ihfs.IhfsDatabaseException;
import com.raytheon.uf.common.hazards.ihfs.table.GroundQueryTable;

/**
 * This class is used to contain all column data for an ihfs.GROUND table Row.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class GroundTableData extends ObservedTableData {

    private static final long serialVersionUID = 9485002901271036L;

    public GroundTableData() {
        super(GroundQueryTable.getInstance());
    }

    public GroundTableData(Object[] tableData) throws IhfsDatabaseException {
        super(GroundQueryTable.getInstance(), tableData);
    }

    public String getId() {
        return (this.lid);
    }
}
