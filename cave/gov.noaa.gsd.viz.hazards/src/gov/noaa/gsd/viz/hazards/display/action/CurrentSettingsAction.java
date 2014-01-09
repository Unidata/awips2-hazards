/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.action;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Represents an action with respect to the current settings.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 09, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 */
public class CurrentSettingsAction {

    private final Settings settings;

    public CurrentSettingsAction(Settings settings) {
        this.settings = settings;
    }

    public Settings getSettings() {
        return settings;
    }

}