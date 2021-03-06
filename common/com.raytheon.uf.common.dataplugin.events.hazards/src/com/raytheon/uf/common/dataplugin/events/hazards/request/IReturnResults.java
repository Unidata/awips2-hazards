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
package com.raytheon.uf.common.dataplugin.events.hazards.request;

import java.util.List;
import java.util.Map;

/**
 * Interface for returning a result for requesting vtec information.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 19, 2014  2826          jsanchez     Initial creation
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * 4/5/2016     16577    Ben.Phillippe Moved out of interoperability plugin 
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public interface IReturnResults {

    public List<Map<String, Object>> getResults();
}
