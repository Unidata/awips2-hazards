/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;

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
 * Dec 13, 2014   4959     Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Jan 29, 2015   4375     Dan Schaffer      Console initiation of RVS product generation
 * Jan 30, 2015   3626     Chris.Golden      Added ability to pass event type when
 *                                           running a recommender.
 * Feb 15, 2015   2271     Dan Schaffer      Incur recommender/product generator init costs immediately
 * Jun 02, 2015   7138     Robert.Blum       Changed to use new Enums for Product Generators.
 * Nov 10, 2015  12762     Chris.Golden      Added support for use of new recommender manager.
 * Apr 01, 2016  16225     Chris.Golden      Added ability to cancel tasks that are scheduled to run
 *                                           at regular intervals.
 * Jun 08, 2017  16373     Chris.Golden      Corrected spelling of RUN_RECOMMENDER.
 * Aug 15, 2017   22757    Chris.Golden      Added ability for recommenders to specify
 *                                           either a message to display, or a dialog to
 *                                           display, with their results (that is, within
 *                                           the returned event set).
 * Sep 27, 2017   38072    Chris.Golden      Changed to work with new recommender
 *                                           manager.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolsView
        implements IToolsView<Action, RCPMainUserInterfaceElement> {

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

    /**
     * Enable run event driven tools menu item text.
     */
    private static final String TOOLS_MENU_ITEM_RUN_EVENT_DRIVEN_TOOLS_TEXT = "Run Tools at Regular Intervals";

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
                        ToolAction.RecommenderActionEnum.RUN_RECOMMENDER,
                        ((Tool) event.widget.getData()).getToolName(),
                        ((Tool) event.widget.getData()).getToolType()));
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public ToolsPulldownAction() {
            super("");
            setImageDescriptor(
                    getImageDescriptorForFile(TOOLS_TOOLBAR_IMAGE_FILE_NAME));
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
                for (String name : toolIdentifiersForNames.keySet()) {
                    Tool tool = toolIdentifiersForNames.get(name);
                    if (tool.getToolType() == ToolType.RECOMMENDER
                            && tool.isVisible()) {
                        addToolToMenu(menu, name, tool);
                    }
                }
                new MenuItem(menu, SWT.SEPARATOR);
                for (String name : toolIdentifiersForNames.keySet()) {
                    Tool tool = toolIdentifiersForNames.get(name);
                    if (tool.getToolType() == ToolType.HAZARD_PRODUCT_GENERATOR
                            && tool.isVisible()) {
                        addToolToMenu(menu, name, tool);
                    }
                }
                for (String name : toolIdentifiersForNames.keySet()) {
                    Tool tool = toolIdentifiersForNames.get(name);
                    if (tool.getToolType() == ToolType.NON_HAZARD_PRODUCT_GENERATOR
                            && tool.isVisible()) {
                        addToolToMenu(menu, name, tool);
                    }
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

        /**
         * @param
         * @return
         */
        private void addToolToMenu(Menu menu, String text, Tool tool) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(text);
            item.setData(tool);
            item.addSelectionListener(listener);
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
    private final Map<String, Tool> toolIdentifiersForNames = new LinkedHashMap<>();

    /**
     * Tools pulldown action.
     */
    private ToolsPulldownAction toolsPulldownAction = null;

    /**
     * Tool dialog.
     */
    private AbstractToolDialog toolDialog = null;

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

    @Override
    public final void initialize(ToolsPresenter presenter, List<Tool> tools) {
        this.presenter = presenter;
        setTools(tools);
    }

    @Override
    public final void dispose() {
        if (toolDialog != null) {
            toolDialog.close();
            toolDialog = null;
        }
    }

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {
            toolsPulldownAction = new ToolsPulldownAction();
            return Lists.newArrayList(toolsPulldownAction);
        } else {
            Action enableEventDrivenToolsExecutionAction = new BasicAction(
                    TOOLS_MENU_ITEM_RUN_EVENT_DRIVEN_TOOLS_TEXT, null,
                    Action.AS_CHECK_BOX, null) {
                @Override
                public void run() {
                    presenter.publish(new ToolAction(isChecked()));
                }
            };
            enableEventDrivenToolsExecutionAction.setChecked(true);
            return Lists.newArrayList(enableEventDrivenToolsExecutionAction);
        }
    }

    @Override
    public final void showToolParameterGatherer(ToolType type,
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
        toolDialog = new ToolParameterDialog(presenter,
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                type, jsonParams);
        toolDialog.open();
        toolDialog.getShell().addDisposeListener(dialogDisposeListener);
    }

    @Override
    public void showToolResults(ToolType type, String jsonParams) {
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
        toolDialog = new ToolResultDialog(presenter,
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                type, jsonParams);
        toolDialog.open();
        toolDialog.getShell().addDisposeListener(dialogDisposeListener);
    }

    @Override
    public final void setTools(List<Tool> tools) {

        /*
         * Get the names and identifiers of the tools.
         */
        toolIdentifiersForNames.clear();
        for (Tool tool : tools) {
            toolIdentifiersForNames.put(tool.getDisplayName(), tool);
        }

        /*
         * Notify the pulldown that the tools have changed.
         */
        if (toolsPulldownAction != null) {
            toolsPulldownAction.toolsChanged();
        }
    }
}
