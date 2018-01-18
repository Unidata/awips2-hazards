/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import org.eclipse.jface.action.IAction;

/**
 * Interface that acts as a marker for {@link IAction} implementations that are
 * to only show a pull-down arrow button on a toolbar, not the main button or
 * other widget that is generally to the left of said arrow button.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 10, 2018   33428    Chris.Golden  Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public interface IPulldownArrowButtonOnly extends IAction {
}
