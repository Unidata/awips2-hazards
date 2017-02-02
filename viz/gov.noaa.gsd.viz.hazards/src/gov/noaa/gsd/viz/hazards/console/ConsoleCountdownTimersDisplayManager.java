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

import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimersDisplayManager;
import gov.noaa.gsd.viz.hazards.alerts.ICountdownTimersDisplayListener;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Resource;

/**
 * Description: Manager of console countdown timers.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 22, 2013    1936    Chris.Golden Initial creation.
 * Feb 01, 2017   15556    Chris.Golden Removed session manager dependency, and
 *                                      cleaned up in order to make it more
 *                                      view-appropriate within the MVP context.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsoleCountdownTimersDisplayManager
        extends
        CountdownTimersDisplayManager<CountdownTimer, ConsoleCountdownTimerDisplayProperties> {

    // Private Variables

    /**
     * Base font, used for deriving fonts for displaying countdown timers.
     */
    private Font baseFont;

    /**
     * Set of basic resources created for use by this manager, to be disposed of
     * when this manager is disposed of.
     */
    private final Set<Resource> resources = new HashSet<>();

    /**
     * Map of font styles, each an integer holding the flags used to specify the
     * font style for {@link org.eclipse.swt.graphics.Font}, to the
     * corresponding fonts to be used in the count down timer displays.
     */
    private final Map<Integer, Font> fontsForFontStyles = new HashMap<>();

    /**
     * Map of RTS colors to SWT colors used as primary colors for countdown
     * timer display.
     */
    private final Map<com.raytheon.uf.common.colormap.Color, Color> swtColorsForRtsColors = new HashMap<>();

    /**
     * Map of RTS colors to SWT colors that contrast with the RTS colors used as
     * primary colors in the countdown timer display.
     */
    private final Map<com.raytheon.uf.common.colormap.Color, Color> contrastingSwtColorsForRtsColors = new HashMap<>();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param listener
     *            Listener for countdown timer display notifications.
     */
    public ConsoleCountdownTimersDisplayManager(
            ICountdownTimersDisplayListener listener) {
        super(CountdownTimer.class, listener);
    }

    // Public Methods

    /**
     * Dispose of this manager.
     */
    @Override
    public void dispose() {
        super.dispose();

        /*
         * Dispose of any SWT resources created.
         */
        for (Resource resource : resources) {
            resource.dispose();
        }
    }

    /**
     * Set the base font, which is required for deriving fonts for countdown
     * timers' display properties.
     * 
     * @param baseFont
     *            Base font to be used.
     */
    public void setBaseFont(Font baseFont) {
        this.baseFont = baseFont;
    }

    // Protected Methods

    @Override
    protected ConsoleCountdownTimerDisplayProperties generateDisplayPropertiesForCountdownTimer(
            CountdownTimer countdownTimer) {

        /*
         * If the base font has not been provided, return nothing.
         */
        if (baseFont == null) {
            return null;
        }

        /*
         * Get the font, constructing it if it hasn't been created yet.
         */
        int style = (countdownTimer.isBold() ? SWT.BOLD : SWT.NORMAL)
                + (countdownTimer.isItalic() ? SWT.ITALIC : SWT.NORMAL);
        Font font = fontsForFontStyles.get(style);
        if (font == null) {
            FontData fontData = baseFont.getFontData()[0];
            font = new Font(baseFont.getDevice(), fontData.getName(),
                    fontData.getHeight(), style);
            fontsForFontStyles.put(style, font);
            resources.add(font);
        }

        /*
         * Get the colors, constructing them if they haven't been created yet.
         * The second color is meant to contrast with the first.
         */
        com.raytheon.uf.common.colormap.Color rtsColor = countdownTimer
                .getColor();
        Color color1 = swtColorsForRtsColors.get(rtsColor);
        if (color1 == null) {
            color1 = new Color(font.getDevice(),
                    (int) (rtsColor.getRed() * 255f),
                    (int) (rtsColor.getGreen() * 255f),
                    (int) (rtsColor.getBlue() * 255f));
            swtColorsForRtsColors.put(rtsColor, color1);
            resources.add(color1);
        }
        Color color2 = contrastingSwtColorsForRtsColors.get(rtsColor);
        if (color2 == null) {
            com.raytheon.uf.common.colormap.Color contrastingRtsColor = Utilities
                    .getContrastingColor(rtsColor);
            color2 = new Color(font.getDevice(),
                    (int) (contrastingRtsColor.getRed() * 255f),
                    (int) (contrastingRtsColor.getGreen() * 255f),
                    (int) (contrastingRtsColor.getBlue() * 255f));
            contrastingSwtColorsForRtsColors.put(rtsColor, color2);
            resources.add(color2);
        }

        /*
         * Return the result.
         */
        return new ConsoleCountdownTimerDisplayProperties(font, color1, color2,
                countdownTimer.isBlinking());
    }
}
