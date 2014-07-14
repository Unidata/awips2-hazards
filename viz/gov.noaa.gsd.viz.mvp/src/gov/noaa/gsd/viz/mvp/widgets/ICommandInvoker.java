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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ICommandInvoker<I> extends IWidget<I> {

    // Public Methods

    /**
     * Set the command invocation handler for the specified invoker. The
     * specified handler will be notified when a command is invoked.
     * 
     * @param identifier
     *            Identifier of the invoker to have its handler set. This may be
     *            <code>null</code> if this object only handles one type of
     *            invocation.
     * @param handler
     *            Handler to be used.
     */
    public void setCommandInvocationHandler(I identifier,
            ICommandInvocationHandler<I> handler);
}
