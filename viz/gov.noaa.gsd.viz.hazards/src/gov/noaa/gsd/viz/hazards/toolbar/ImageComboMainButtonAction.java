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
 * of an {@link ActionChooserAction} instance.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 6, 2018    33428    Chris.Golden  Initial creation.
 * </pre>
 *
 * @author golden
 */
public class ImageComboMainButtonAction<P extends BasicAction> extends
        ActionMenuActionHelper<ImageComboChooserAction<P>, ImageComboMainButtonAction<P>, P> {

    // Private Variables

    /**
     * Image combo chooser action associated with this action.
     */
    private ImageComboChooserAction<P> imageComboChooserAction;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Action that is currently the one being wrapped by this action.
     */
    public ImageComboMainButtonAction(P principal) {
        super(principal);
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
        if (imageComboChooserAction == null) {
            return;
        }
        ToolItem item = (ToolItem) event.widget;
        Menu menu = imageComboChooserAction.getMenu(item.getParent());
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

    // Package-Private Methods

    /**
     * Set the associated image combo chooser action to that specified.
     * 
     * @param imageComboChooserAction
     *            Image combo chooser action.
     */
    void setImageComboChooserAction(
            ImageComboChooserAction<P> imageComboChooserAction) {
        this.imageComboChooserAction = imageComboChooserAction;
    }
}
