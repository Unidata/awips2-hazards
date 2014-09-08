/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import gov.noaa.gsd.viz.mvp.widgets.IQualifiedWidget;

import java.util.concurrent.Callable;

/**
 * Description: Base class for tasks that are to be run in the main UI thread
 * using a qualified principal and that are to return a value. As with the
 * superclass, the principal is set after construction using
 * {@link #setPrincipal(IQualifiedWidget)}, but must be set before the task is
 * executed. Subclass implementations of {@link #call()} must access the
 * principal via {@link #getPrincipal()} method.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class QualifiedPrincipalCallableTask<Q, I, W extends IQualifiedWidget<Q, I>, R>
        extends QualifiedPrincipalTask<Q, I, W> implements Callable<R> {
}
