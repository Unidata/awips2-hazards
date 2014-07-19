/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

/**
 * Description: Interface describing the methods that must be implemented by
 * megawidgets that hold one or more {@link TimeDeltaComboComponent} objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2014    3512    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ITimeDeltaComboComponentHolder {

    // Public Methods

    /**
     * Receive notification that the value of the specified time delta component
     * has been changed as a result of GUI manipulation.
     * 
     * @param identifier
     *            Identifier of the time delta component for which the value has
     *            changed.
     * @param value
     *            New delta value of the date-time component.
     */
    public void valueChanged(String identifier, long value);
}
