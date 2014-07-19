/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.mvp.widgets;

/**
 * Interface describing the methods required in any sort of HMI component that,
 * when invoked, executes a command by notifying its
 * {@link ICommandInvocationHandler} of the invocation. The generic parameter
 * <code>I</code> provides the type of widget identifier to be used.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 08, 2014    2925    Chris.Golden Changed to inherit from new IWidget.
 * Jun 30, 2014    3512    Chris.Golden Simplified by removing the identifier
 *                                      association with a handler when
 *                                      registering it; only one handler is
 *                                      needed, not one per identifier. This
 *                                      also maintains symmetry with the
 *                                      similar change in IStateChanger.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ICommandInvoker<I> extends IWidget<I> {

    // Public Methods

    /**
     * Set the command invocation handler for this invoker. The specified
     * handler will be notified when a command is invoked.
     * 
     * @param handler
     *            Handler to be used.
     */
    public void setCommandInvocationHandler(ICommandInvocationHandler<I> handler);
}
