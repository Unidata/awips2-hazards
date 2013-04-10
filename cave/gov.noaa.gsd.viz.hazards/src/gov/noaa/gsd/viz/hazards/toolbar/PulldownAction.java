/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Abstract class from which may be derived classes encapsulating toolbar
 * buttons that, when invoked, display pulldown menus.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 */
public abstract class PulldownAction extends BasicAction implements
        IMenuCreator {

    // Private Variables

    /**
     * Menu that drops down.
     */
    private Menu menu = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param text
     *            Text for the action.
     */
    public PulldownAction(String text) {
        super(text, null, Action.AS_DROP_DOWN_MENU, null);
        setMenuCreator(this);
    }

    // Public Methods

    /**
     * Get the menu for the specified parent.
     * 
     * @param parent
     *            Parent control.
     * @return Menu.
     */
    @Override
    public final Menu getMenu(Control parent) {
        menu = doGetMenu(parent, menu);
        return menu;
    }

    /**
     * Get the menu for the specified parent.
     * 
     * @param parent
     *            Parent menu.
     * @return Menu.
     */
    @Override
    public final Menu getMenu(Menu parent) {
        throw new UnsupportedOperationException();
    }

    /**
     * Run the action.
     */
    @Override
    public final void run() {

        // No action.
    }

    /**
     * Run the action resulting from the specified event. This would not need
     * overriding were it not for the fact that without it, the user can only
     * click on the down-arrow on the menu button displayed for this action.
     * This implementation ensures that the user can click anywhere on the
     * button to drop down the menu.
     * 
     * @param event
     *            Event that triggered this invocation.
     */
    @Override
    public final void runWithEvent(Event event) {
        ToolItem item = (ToolItem) event.widget;
        Menu menu = getMenu(item.getParent());
        if (menu != null) {
            Point point = item.getParent().toDisplay(
                    new Point(item.getBounds().x, item.getBounds().height));
            menu.setLocation(point.x, point.y);
            menu.setVisible(true);
        }
    }

    /**
     * Dispose of the action.
     */
    @Override
    public void dispose() {
        if (menu != null) {
            menu.dispose();
        }
    }

    // Protected Methods

    /**
     * Get the menu for the specified parent, possibly reusing the specified
     * menu if provided.
     * 
     * @param parent
     *            Parent control.
     * @param menu
     *            Menu that was created previously, if any; this may be reused,
     *            or disposed of completely.
     * @return Menu.
     */
    protected abstract Menu doGetMenu(Control parent, Menu menu);
}
