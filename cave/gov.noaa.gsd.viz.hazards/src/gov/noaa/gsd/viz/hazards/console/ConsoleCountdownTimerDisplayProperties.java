/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.viz.hazards.alerts.CountdownTimerDisplayProperties;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimersDisplayManager;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Description: Encapsulation of the properties of a console countdown timer's
 * display.
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
public class ConsoleCountdownTimerDisplayProperties extends
        CountdownTimerDisplayProperties {

    // Private Variables

    /**
     * Font to be used.
     */
    private final Font font;

    /**
     * Primary color to be used.
     */
    private final Color primaryColor;

    /**
     * Secondary color to be used.
     */
    private final Color secondaryColor;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param font
     *            Font to be used.
     * @param primaryColor
     *            First color to be used.
     * @param secondaryColor
     *            Second color to be used.
     */
    public ConsoleCountdownTimerDisplayProperties(Font font,
            Color primaryColor, Color secondaryColor, boolean isBlinking) {
        super(isBlinking);
        this.font = font;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    // Public Methods

    /**
     * Get the font to be used.
     * 
     * @return Font to be used.
     */
    public final Font getFont() {
        return font;
    }

    /**
     * Get the foreground color to be used.
     * 
     * @return Foreground color to be used.
     */
    public final Color getForegroundColor() {
        return (isPrimaryForeground() ? primaryColor : secondaryColor);
    }

    /**
     * Get the background color to be used.
     * 
     * @return Background color to be used.
     */
    public final Color getBackgroundColor() {
        return (isPrimaryForeground() ? secondaryColor : primaryColor);
    }
}
