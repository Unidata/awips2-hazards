/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

/**
 * Description: Interface describing the methods that must be implemented to
 * generate identifiers for a spatial entity. The generic parameter
 * <code>I</code> provides the type of identifier to be generated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 15, 2016   15676    Chris.Golden Initial creation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IIdentifierGenerator<I> {

    // Public Methods

    /**
     * Generate an identifier for a spatial entity given the specified base.
     * 
     * @param base
     *            Base from which to construct an identifier.
     * @return Identifier.
     */
    public I generate(String base);
}
