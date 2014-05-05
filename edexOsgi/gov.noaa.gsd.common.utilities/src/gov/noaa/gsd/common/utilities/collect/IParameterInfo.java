/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities.collect;

/**
 * Description: Interface specifying the methods that must be implemented for
 * any class that is to encapsulate information about a parameter. Parameters
 * have, at minimum, a label describing them in human-readable terms, and an
 * identifier key.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 18, 2014  2336      Chris.Golden Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IParameterInfo {

    /**
     * Get the identifier for this key.
     * 
     * @param Identifier
     *            . .
     */
    public String getKey();

    /**
     * Get the label for this key.
     * 
     * @return Label.
     */
    public String getLabel();
}
