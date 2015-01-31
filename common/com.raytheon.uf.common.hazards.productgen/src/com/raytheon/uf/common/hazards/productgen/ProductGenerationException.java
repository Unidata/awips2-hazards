/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.hazards.productgen;

/**
 * Description: Exception handling for unexpected product generation errors.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 02, 2015    5952    Dan Schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ProductGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public ProductGenerationException() {
        super();
    }

    /**
     * Constructor taking a message
     * 
     * @param string
     */
    public ProductGenerationException(String string) {
        super(string);
    }

    /**
     * Constructor taking a message and a throwable cause
     * 
     * @param message
     * @param cause
     */
    public ProductGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor taking a throwable cause
     * 
     * @param cause
     */
    public ProductGenerationException(Throwable cause) {
        super(cause);
    }

}
