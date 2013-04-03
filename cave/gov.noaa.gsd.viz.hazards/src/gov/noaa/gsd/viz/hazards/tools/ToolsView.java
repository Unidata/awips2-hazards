/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction.ToolActionEnum;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Settings view, an implementation of ISettingsView that provides an SWT-based
 * view.
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
 * @version 1.0
 */
public class ToolsView implements
        IToolsView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ToolsView.class);

    // Private Classes

    /**
     * Tools pulldown menu action.
     */
    private class ToolsPulldownAction extends PulldownAction {

        // Private Variables

        /**
         * Flag indicating whether or not the menu should be repopulated with
         * tool names.
         */
        private boolean toolsChanged = true;

        /**
         * Listener for menu item invocations.
         */
        private final SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                fireAction(ToolAction.ToolActionEnum.RUN_TOOL,
                        (String) event.widget.getData());
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public ToolsPulldownAction() {
            super("");
            setImageDescriptor(getImageDescriptorForFile("tools.png"));
            setToolTipText("Tools");
        }

        // Public Methods

        /**
         * Receive notification that the tools have changed.
         */
        public void toolsChanged() {
            toolsChanged = true;
        }

        // Protected Methods

        /**
         * Get the menu for the specified parent, possibly reusing the specified
         * menu if provided.
         * 
         * @param parent
         *            Parent control.
         * @param menu
         *            Menu that was created previously, if any; this may be
         *            reused, or disposed of completely.
         * @return Menu.
         */
        @Override
        public Menu doGetMenu(Control parent, Menu menu) {

            // If the tools have changed, recreate the menu's contents.
            if (toolsChanged) {

                // If the menu has not yet been created, do so now;
                // otherwise, delete its contents.
                if (menu == null) {
                    menu = new Menu(parent);
                } else {
                    for (MenuItem item : menu.getItems()) {
                        item.dispose();
                    }
                }

                // Iterate through the tools, if any, creating an item
                // for each.
                for (int j = 0; j < toolNames.size(); j++) {
                    MenuItem item = new MenuItem(menu, SWT.PUSH);
                    item.setText(toolNames.get(j));
                    item.setData(toolIdentifiersForNames.get(toolNames.get(j)));
                    item.addSelectionListener(listener);
                }

                // Reset the tools changed flag.
                toolsChanged = false;
            }

            // Return the menu.
            return menu;
        }
    }

    // Private Variables

    /**
     * Presenter.
     */
    private ToolsPresenter presenter = null;

    /**
     * Array of tool names.
     */
    private final List<String> toolNames = new ArrayList<String>();

    /**
     * Map of tool names to their associated identifiers.
     */
    private final Map<String, String> toolIdentifiersForNames = new HashMap<String, String>();;

    /**
     * Tools pulldown action.
     */
    private ToolsPulldownAction toolsPulldownAction = null;

    /**
     * Tool dialog.
     */
    private ToolDialog toolDialog = null;

    /**
     * Dialog dispose listener.
     */
    private final DisposeListener dialogDisposeListener = new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent e) {
            toolDialog = null;
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public ToolsView() {

        // No action.
    }

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param jsonTools
     *            JSON string providing the tools.
     */
    @Override
    public final void initialize(ToolsPresenter presenter, String jsonTools) {
        this.presenter = presenter;
        setTools(jsonTools);
    }

    /**
     * Prepare for disposal.
     */
    @Override
    public final void dispose() {
        if (toolDialog != null) {
            toolDialog.close();
            toolDialog = null;
        }
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type</code> to (re)populate the main UI
     * with the specified <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist from a previous call
     * with the same <code>type</code>.
     * 
     * @param mainUI
     *            Main user interface to which to contribute.
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return True if items were contributed, otherwise false.
     */
    @Override
    public final boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            // Create the action.
            toolsPulldownAction = new ToolsPulldownAction();

            // Add the action to the toolbar.
            mainUI.getToolBarManager().add(toolsPulldownAction);
            return true;
        }
        return false;
    }

    /**
     * Show a tool subview that is used to gather parameter values for a tool
     * that is to be executed.
     * 
     * @param toolName
     *            Name of the tool for which parameters are to be gathered.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    @Override
    public final void showToolParameterGatherer(String toolName,
            String jsonParams) {
        toolDialog = new ToolDialog(presenter, PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), toolName, jsonParams);
        toolDialog.open();
        toolDialog.getShell().addDisposeListener(dialogDisposeListener);
    }

    /**
     * Set the tools to those specified.
     * 
     * @param jsonTools
     *            JSON string holding a list of dictionaries providing the
     *            tools.
     */
    @Override
    public final void setTools(String jsonTools) {

        // Get the dictionary list from the JSON.
        DictList tools = DictList.getInstance(jsonTools);
        if ((tools == null) || (tools.size() < 1)) {
            return;
        }

        // Get the names and identifiers of the tools.
        toolNames.clear();
        toolIdentifiersForNames.clear();
        for (int j = 0; j < tools.size(); j++) {
            Dict tool = tools.getDynamicallyTypedValue(j);
            String name = tool.getDynamicallyTypedValue("displayName");
            toolNames.add(name);
            String identifier = tool.getDynamicallyTypedValue("toolName");
            toolIdentifiersForNames.put(name, identifier);
        }

        // Notify the pulldown that the tools have changed.
        if (toolsPulldownAction != null) {
            toolsPulldownAction.toolsChanged();
        }
    }

    // Private Methods

    /**
     * Fire an action event to its listener.
     * 
     * @param action
     *            Action.
     * @param detail
     *            Detail.
     */
    private void fireAction(ToolActionEnum action, String detail) {
        presenter.fireAction(new ToolAction(action, detail));
    }
}
