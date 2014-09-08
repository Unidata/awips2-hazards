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

import gov.noaa.gsd.viz.mvp.widgets.IWidget;

import org.eclipse.swt.widgets.Widget;

/**
 * Description: Simple widget delegate helper with a direct connection to the
 * principal widget for which delegation is being performed. Using this with an
 * instance of {@link WidgetDelegate} allows thread-safe access to
 * {@link IWidget} instances that represent {@link Widget SWT widgets} or
 * objects composed of SWT widgets. The generic parameter <code>I</code>
 * provides the type of widget identifier to be used, and <code>W</code> is the
 * type of principal this delegate is to represent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 10, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BasicWidgetDelegateHelper<I, W extends IWidget<I>> implements
        IWidgetDelegateHelper<I, W> {

    // Private Variables

    /**
     * Principal represented by this delegate.
     */
    private final W principal;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            The principal represented by this delegate.
     */
    public BasicWidgetDelegateHelper(W principal) {
        this.principal = principal;
    }

    // Public Methods

    @Override
    public final W getPrincipal() {
        return principal;
    }

    @Override
    public final void scheduleTask(final PrincipalRunnableTask<I, W> task) {

        /*
         * No action; the view should always exist when this method is called.
         */
    }

    @Override
    public void scheduleTaskForEachViewCreation(PrincipalRunnableTask<I, W> task) {

        /*
         * No action; the view should always exist when this method is called.
         */
    }
}
