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

/**
 * Description: Interface describing methods that must be implemented by classes
 * that are to serve as helpers for {@link QualifiedWidgetDelegate} instances.
 * The generic parameter <code>Q</code> provides the type of widget qualifier to
 * be used, <code>I</code> provides the type of widget identifier to be used,
 * and <code>W</code> is the type of principal this delegate is to represent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IQualifiedWidgetDelegateHelper<Q, I, W extends IQualifiedWidget<Q, I>> {

    // Public Methods

    /**
     * Get the principal widget for which this delegate is acting as a
     * representative.
     * 
     * @return Principal widget, or <code>null</code> if the widget is not
     *         available at this time.
     */
    public W getPrincipal();

    /**
     * Schedule the specified task to run later if necessary, since it could not
     * be run now due to the principal being missing.
     * 
     * @param task
     *            Task to be scheduled.
     */
    public void scheduleTask(QualifiedPrincipalRunnableTask<Q, I, W> task);

    /**
     * Schedule the specified task to be run each time a view is created.
     * <p>
     * Note that if a view has not been created when this method is invoked, and
     * {@link #scheduleTask(QualifiedPrincipalRunnableTask)} has also been
     * invoked while the view was not created holding the same task, that task
     * will be run twice when the view is first created, and then only once upon
     * subsequent creations. Therefore, if a given task is to be executed only
     * once per creation, only this method should be invoked; if it is to be
     * executed just once for the first creation, only
     * <code>scheduleTask()</code> should be invoked.
     * </p>
     * 
     * @param task
     *            Task to be scheduled.
     */
    public void scheduleTaskForEachViewCreation(
            QualifiedPrincipalRunnableTask<Q, I, W> task);
}
