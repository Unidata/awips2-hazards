/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazardtypefirst;

import gov.noaa.gsd.viz.hazards.hazardtypefirst.HazardTypeFirstPresenter.Command;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

/**
 * Description: Interface containing the methods that a hazard-type-first view
 * must implement.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 21, 2015    3626    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardTypeFirstView {

    /**
     * Get the category state changer. The identifier is ignored.
     * 
     * @return Category state changer.
     */
    public IChoiceStateChanger<Object, String, String, String> getCategoryChanger();

    /**
     * Get the type state changer. The identifier is ignored.
     * 
     * @return Type state changer.
     */
    public IChoiceStateChanger<Object, String, String, String> getTypeChanger();

    /**
     * Get the dialog command invoker.
     * 
     * @param Command
     *            invoker.
     */
    public ICommandInvoker<Command> getCommandInvoker();
}
