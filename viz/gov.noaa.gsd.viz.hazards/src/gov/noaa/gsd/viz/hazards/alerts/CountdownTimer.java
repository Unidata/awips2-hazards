/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;

import java.util.Date;

import com.raytheon.uf.common.colormap.Color;

/**
 * Description: Countdown timer for an active alert.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 13, 2017   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class CountdownTimer {

    // Private Variables

    /**
     * Expiration time.
     */
    private final Date expireTime;

    /**
     * Color of the countdown timer.
     */
    private final Color color;

    /**
     * Flag indicating whether or not the countdown timer is bold.
     */
    private final boolean bold;

    /**
     * Flag indicating whether or not the countdown timer is italic.
     */
    private final boolean italic;

    /**
     * Flag indicating whether or not the countdown timer is blinking.
     */
    private final boolean blinking;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param expireTime
     *            Expiration time.
     * @param color
     *            Color of the countdown timer.
     * @param bold
     *            Flag indicating whether or not the countdown timer is bold.
     * @param italic
     *            Flag indicating whether or not the countdown timer is italic.
     * @param blinking
     *            Flag indicating whether or not the countdown timer is
     *            blinking.
     */
    public CountdownTimer(Date expireTime, Color color, boolean bold,
            boolean italic, boolean blinking) {
        this.expireTime = expireTime;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.blinking = blinking;
    }

    // Public Methods

    /**
     * Get the expiration time.
     * 
     * @return Expiration time.
     */
    public Date getExpireTime() {
        return expireTime;
    }

    /**
     * Get the color of the countdown timer.
     * 
     * @return Color of the countdown timer.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Determine whether or not the countdown timer is bold.
     * 
     * @return <code>true</code> if the countdown timer is bold,
     *         <code>false</code> otherwise.
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Determine whether or not the countdown timer is italic.
     * 
     * @return <code>true</code> if the countdown timer is italic,
     *         <code>false</code> otherwise.
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Determine whether or not the countdown timer is blinking.
     * 
     * @return <code>true</code> if the countdown timer is blinking,
     *         <code>false</code> otherwise.
     */
    public boolean isBlinking() {
        return blinking;
    }
}
