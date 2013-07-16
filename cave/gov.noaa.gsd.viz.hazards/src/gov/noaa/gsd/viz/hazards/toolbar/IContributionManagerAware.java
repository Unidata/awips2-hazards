/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import org.eclipse.jface.action.IContributionManager;

/**
 * Description: Interface that must be implemented by any actions that need to
 * know what contribution manager is managing them.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2013     585    Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IContributionManagerAware {

    // Public Methods

    /**
     * Set the contribution manager.
     * 
     * @param contributionManager
     *            New contribution manager.
     */
    public void setContributionManager(IContributionManager contributionManager);
}