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
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

/**
 * Tools view, an implementation of IToolsView that provides an SWT-based view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jun 13, 2013   1282     Chris.Golden      Fixed bug causing tool menu to
 *                                           remain unchanged when an empty
 *                                           tool list was provided, and made
 *                                           tool menu button disabled in such
 *                                           cases.
 * Jul 15, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * Dec 05, 2014   4124     Chris.Golden      Corrected header comment.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolsView implements
        IToolsView<Action, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ToolsView.class);

    /**
     * Name of the file holding the image for the tools toolbar button icon.
     */
    private static final String TOOLS_TOOLBAR_IMAGE_FILE_NAME = "tools.png";

    /**
     * Tools toolbar button tooltip text.
     */
    private static final String TOOLS_TOOLBAR_BUTTON_TOOLTIP_TEXT = "Tools";

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
                presenter.publish(new ToolAction(
                        ToolAction.ToolActionEnum.RUN_TOOL,
                        (String) event.widget.getData()));
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public ToolsPulldownAction() {
            super("");
            setImageDescriptor(getImageDescriptorForFile(TOOLS_TOOLBAR_IMAGE_FILE_NAME));
            setToolTipText(TOOLS_TOOLBAR_BUTTON_TOOLTIP_TEXT);
            toolsChanged();
        }

        // Public Methods

        /**
         * Receive notification that the tools have changed.
         */
        public void toolsChanged() {
            toolsChanged = true;
            setEnabled(toolIdentifiersForNames.isEmpty() == false);
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

            /*
             * If the tools have changed, recreate the menu's contents.
             */
            if (toolsChanged) {

                /*
                 * If the menu has not yet been created, do so now; otherwise,
                 * delete its contents.
                 */
                if (menu == null) {
                    menu = new Menu(parent);
                } else {
                    for (MenuItem item : menu.getItems()) {
                        item.dispose();
                    }
                }

                /*
                 * Iterate through the tools, if any, creating an item for each.
                 */
                for (Entry<String, String> entry : toolIdentifiersForNames
                        .entrySet()) {
                    MenuItem item = new MenuItem(menu, SWT.PUSH);
                    item.setText(entry.getKey());
                    item.setData(entry.getValue());
                    item.addSelectionListener(listener);
                }

                /*
                 * Reset the tools changed flag.
                 */
                toolsChanged = false;
            }

            /*
             * Return the menu.
             */
            return menu;
        }
    }

    // Private Variables

    /**
     * Presenter.
     */
    private ToolsPresenter presenter = null;

    /**
     * Map of tool names to their associated identifiers.
     */
    private final Map<String, String> toolIdentifiersForNames = new LinkedHashMap<>();

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

        /*
         * No action.
         */
    }

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param tools
     * 
     */
    @Override
    public final void initialize(ToolsPresenter presenter, List<Tool> tools) {
        this.presenter = presenter;
        setTools(tools);
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
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return List of contributions; this may be empty if none are to be made.
     */
    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {
            toolsPulldownAction = new ToolsPulldownAction();
            return Lists.newArrayList(toolsPulldownAction);
        }
        return Collections.emptyList();
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
        if (toolDialog != null) {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            MessageDialog.openInformation(shell, "Hazard Services",
                    "Another tool dialog is already showing. In order to "
                            + "start a tool, first allow the other tool "
                            + "to complete its execution, or dismiss its "
                            + "dialog.");
            toolDialog.open();
            return;
        }
        toolDialog = new ToolDialog(presenter, PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), toolName, jsonParams);
        toolDialog.open();
        toolDialog.getShell().addDisposeListener(dialogDisposeListener);
    }

    /**
     * Set the tools to those specified.
     * 
     * @param tools
     */
    @Override
    public final void setTools(List<Tool> tools) {

        /*
         * Get the names and identifiers of the tools.
         */
        toolIdentifiersForNames.clear();
        for (Tool tool : tools) {
            toolIdentifiersForNames.put(tool.getDisplayName(),
                    tool.getToolName());
        }

        /*
         * Notify the pulldown that the tools have changed.
         */
        if (toolsPulldownAction != null) {
            toolsPulldownAction.toolsChanged();
        }
    }

    // Private Methods

}
