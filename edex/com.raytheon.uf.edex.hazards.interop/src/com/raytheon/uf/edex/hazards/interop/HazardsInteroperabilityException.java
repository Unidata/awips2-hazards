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
package com.raytheon.uf.edex.hazards.interop;

/**
 * Exception class used to wrap problems with interoperability
 * 
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
public class HazardsInteroperabilityException extends Exception {

    private static final long serialVersionUID = 5563147700544800769L;

    /**
     * Creates a new HazardsInteroperabilityException
     * 
     * @param string
     *            The message
     */
    public HazardsInteroperabilityException(String string) {
        super(string);
    }

    /**
     * Creates a new HazardsInteroperabilityException
     * 
     * @param string
     *            The message
     * @param e
     *            The exception object
     */
    public HazardsInteroperabilityException(String string, Exception e) {
        super(string, e);
    }
}
