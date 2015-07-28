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

/**
 * Base Exception for IHFS Data Access.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer            Description
 * ------------ ---------- -----------         --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class IhfsDatabaseException extends Exception {

    private static final long serialVersionUID = 7340224123735498915L;

    /**
     * Default constructor
     */
    public IhfsDatabaseException() {
        super();
    }

    /**
     * Constructor taking a message
     * 
     * @param message
     *            Exception message
     */
    public IhfsDatabaseException(String message) {
        super(message);
    }

    /**
     * Constructor taking a message and a throwable cause
     * 
     * @param message
     *            Exception message
     * @param cause
     *            Exception cause
     */
    public IhfsDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor taking a throwable cause
     * 
     * @param cause
     *            Exception cause
     */
    public IhfsDatabaseException(Throwable cause) {
        super(cause);
    }
}
