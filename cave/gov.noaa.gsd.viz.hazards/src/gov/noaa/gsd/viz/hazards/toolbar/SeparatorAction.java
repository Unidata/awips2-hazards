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

import org.eclipse.jface.action.Action;

/**
 * Description: SeparatorAction, a slight kludge used to allow a UI contribution made
 * to an <code>IView</code> to be a separator instead of only an <code>Action
 * </code> that may be invoked.
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
 * @see gov.noaa.gsd.viz.mvp.IView
 */
public class SeparatorAction extends Action {

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public SeparatorAction() {
    }
}
