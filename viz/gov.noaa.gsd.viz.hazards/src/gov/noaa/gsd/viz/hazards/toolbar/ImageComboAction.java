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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Wrapper for an action that is the most-recently-chosen one from the drop-down
 * of an {@link ImageComboHelperAction} instance.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 06, 2018   33428    Chris.Golden  Initial creation.
 * Mar 22, 2018   15561    Chris.Golden  Refactored to make the drop-down
 *                                       provider be considered a helper,
 *                                       rather than the main-button-providing
 *                                       class, and to allow the helper and
 *                                       main button provider to communicate
 *                                       amongst themselves without needing
 *                                       help from code using them.
 * </pre>
 *
 * @author golden
 */
public class ImageComboAction<P extends BasicAction> extends
        ActionAndMenuAction<ImageComboAction<P>, ImageComboHelperAction<P>, P> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Action that is currently the one being wrapped by this action.
     * @param helper
     *            Helper for this action.
     */
    public ImageComboAction(P principal, ImageComboHelperAction<P> helper) {
        super(principal, helper);
    }

    // Public Methods

    /*
     * Run the action resulting from the specified event. This would not need
     * overriding were it not for the fact that without it, the user can only
     * click on the down-arrow on the menu button displayed for this action.
     * This implementation ensures that the user can click anywhere on the
     * button to drop down the menu.
     * 
     * @param event Event that triggered this invocation.
     */
    @Override
    public final void runWithEvent(Event event) {
        ImageComboHelperAction<P> helper = getHelper();
        if (helper == null) {
            return;
        }
        ToolItem item = (ToolItem) event.widget;
        Menu menu = helper.getMenu(item.getParent());
        if (menu != null) {
            Point point = item.getParent().toDisplay(
                    new Point(item.getBounds().x, item.getBounds().height));
            menu.setLocation(point.x, point.y);
            menu.setVisible(true);
        }
    }

    @Override
    public void run() {

        /*
         * No action.
         */
    }
}
