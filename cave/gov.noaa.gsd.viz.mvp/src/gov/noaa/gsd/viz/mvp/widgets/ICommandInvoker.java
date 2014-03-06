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
 * {@link ICommandInvocationHandler} of the invocation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ICommandInvoker {

    // Public Methods

    /**
     * Set the command invocation handler. The specified handler will be
     * notified when a command is invoked.
     * 
     * @param handler
     *            Handler to be used.
     */
    public void setCommandInvocationHandler(ICommandInvocationHandler handler);
}
