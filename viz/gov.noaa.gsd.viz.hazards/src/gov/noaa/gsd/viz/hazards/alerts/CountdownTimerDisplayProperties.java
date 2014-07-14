/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;


/**
 * Description: Base class of an encapsulation of the properties of a countdown
 * timer's display.
 * <p>
 * <strong>NOTE</strong>: Objects of this type are returned by the
 * {@link CountdownTimersDisplayManager#getDisplayPropertiesForEvent(String)}
 * method. They are to be used immediately for drawing operations, and are not
 * to be cached, as their states may be changed by the countdown timers display
 * manager as time ticks forward.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 22, 2013    1936    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class CountdownTimerDisplayProperties {

    // Private Variables

    /**
     * Flag indicating whether or not the timer is blinking.
     */
    private final boolean isBlinking;

    /**
     * Flag indicating whether or not the primary color is currently the
     * foreground color.
     */
    private boolean isPrimaryForeground;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param isBlinking
     *            Flag indicating whether or not the display is blinking.
     */
    protected CountdownTimerDisplayProperties(boolean isBlinking) {
        this.isBlinking = isBlinking;
    }

    // Public Methods

    /**
     * Find out whether or not blinking is enabled.
     * 
     * @return Flag indicating whether or not blinking is enabled.
     */
    public final boolean isBlinking() {
        return isBlinking;
    }

    // Protected Methods

    /**
     * Determine whether the primary color is to be used as the foreground
     * color.
     * 
     * @return Flag indicating whether or not the primary color is to be used as
     *         the foreground color.
     */
    protected final boolean isPrimaryForeground() {
        return isPrimaryForeground;
    }

    // Package Methods

    /**
     * Set the flag indicating whether the primary color is to be used as the
     * foreground color.
     * 
     * @param isPrimaryForeground
     *            Flag indicating whether or not the primary color is to be used
     *            as the foreground color.
     */
    void setPrimaryForeground(boolean isPrimaryForeground) {
        this.isPrimaryForeground = isPrimaryForeground;
    }
}
