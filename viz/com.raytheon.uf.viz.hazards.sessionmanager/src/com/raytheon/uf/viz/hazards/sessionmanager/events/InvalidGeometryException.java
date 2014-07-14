/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

/**
 * Checked exception that is raised when an attempt to create or modify a
 * geometry results in an invalid geometry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 8, 2014             blawrenc    Initial creation
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */

public class InvalidGeometryException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an InvalidGeometryException with no detail message
     */
    public InvalidGeometryException() {
        super();
    }

    /**
     * Constructs an InvalidGeomtryException with the specified detail message.
     * 
     * @param message
     *            The detail message
     */
    public InvalidGeometryException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidGeometryException with the specified detail
     * message and cause.
     * 
     * @param message
     *            The detail message
     * @param cause
     *            The cause of the exception
     */
    public InvalidGeometryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new InvalidGeometryException with the specified cause and a
     * detail message of (cause == null ? null : cause.toString()) which
     * typically contains the class and detail message of cause
     * 
     * @param cause
     *            The cause of the exception.
     */
    public InvalidGeometryException(Throwable cause) {
        super(cause);
    }

}
