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

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Represents an action with respect to the current settings.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 09, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * Feb 19, 2014 2915       bkowal      Settings Action standardization
 * Dec 05, 2014 4124       Chris.Golden Changed to work with ISettings.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 */
public class CurrentSettingsAction extends AbstractSettingsAction {

    private final IOriginator originator;

    public CurrentSettingsAction(ISettings settings, IOriginator originator) {
        super(settings);
        this.originator = originator;
    }

    public IOriginator getOriginator() {
        return originator;
    }
}