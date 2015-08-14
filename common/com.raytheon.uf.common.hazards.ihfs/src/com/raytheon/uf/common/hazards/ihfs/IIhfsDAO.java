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

import java.util.List;

import com.raytheon.uf.common.hazards.ihfs.data.AbstractTableData;

/**
 * Data access Interface for the ihfs database.
 * 
 * This class performs queries and creates data objects as a result of the
 * queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer            Description
 * ------------ ---------- -----------         --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Add Aggregate query functions
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public interface IIhfsDAO {

    /**
     * Execute an IHFS (Hydro) Query
     * 
     * @param simpleIhfsQuery
     *            Object query to execute.
     * @return List of AbstractTableData sub classes containing data returned by
     *         the query
     */
    public List<? extends AbstractTableData> queryIhfsData(
            SimpleIhfsQuery simpleIhfsQuery) throws IhfsDatabaseException;

    /**
     * Execute an Aggregate Function IHFS (Hydro) Query
     * 
     * @param simpleAggIhfsQuery
     *            Aggregate Function Object query to execute.
     * @return Aggregate Function result value
     */
    public Object queryAggregateIhfsData(SimpleAggIhfsQuery simpleAggIhfsQuery)
            throws IhfsDatabaseException;
}
