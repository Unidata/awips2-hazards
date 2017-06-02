/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.common.utilities.Sort;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.TimeRangeType;
import gov.noaa.gsd.viz.hazards.console.ConsoleView.ITemporallyAware;
import gov.noaa.gsd.viz.hazards.console.ITemporalDisplay.SelectedTimeMode;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;
import gov.noaa.gsd.viz.widgets.WidgetUtilities;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.core.icon.IconUtil;

/**
 * Description: Body of the console view part.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 01, 2016   15556    Chris.Golden Initial creation.
 * May 05, 2017   10001    Chris.Golden Added detection of attaching/detaching of
 *                                      view part and responding by recreating
 *                                      the column menus in the tree, since these
 *                                      were throwing exceptions when displayed
 *                                      after attachment or detachment.
 * Jun 30, 2017   19223    Chris.Golden Added ability to change the text and
 *                                      enabled state of a row menu's menu item
 *                                      after it is displayed.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class ConsoleBody implements IConsoleTree {

    // Private Static Constants

    /**
     * Width in pixels of the margin used in the form layout.
     */
    private static final int FORM_MARGIN_WIDTH = 2;

    /**
     * Height in pixels of the margin used in the form layout.
     */
    private static final int FORM_MARGIN_HEIGHT = 1;

    /**
     * PNG file name suffix.
     */
    private static final String PNG_FILE_NAME_SUFFIX = ".png";

    /**
     * The default height of the button panel.
     */
    private static final int BUTTON_PANEL_HEIGHT = 40;

    // Private Variables

    /**
     * Console tree user, used to respond to console tree changes.
     */
    private final ConsoleTree.IConsoleTreeUser consoleTreeUser = new ConsoleTree.IConsoleTreeUser() {

        @Override
        public void timeLineRulerSizeChanged(int xStart, int xEnd) {
            if ((comboBoxPanel.isDisposed() == false)
                    && comboBoxPanel.isVisible()) {
                FormData buttonPanelFormData = new FormData();
                buttonPanelFormData.left = new FormAttachment(0, xStart
                        + FORM_MARGIN_WIDTH);
                buttonPanelFormData.right = new FormAttachment(100, -1 * xEnd);
                buttonPanelFormData.top = new FormAttachment(comboBoxPanel, 0,
                        SWT.TOP);
                buttonPanelFormData.bottom = new FormAttachment(comboBoxPanel,
                        0, SWT.BOTTOM);
                buttonsPanel.setLayoutData(buttonPanelFormData);
                bodyPanel.layout(true);
            }
        }

        @Override
        public void timeLineRulerVisibleRangeChanged(long lowerVisibleValue,
                long upperVisibleValue, long zoomedOutRange, long zoomedInRange) {
            updateRulerButtonsState(lowerVisibleValue, upperVisibleValue,
                    zoomedOutRange, zoomedInRange);
        }

        @Override
        public void selectedTimeModeChanged(SelectedTimeMode mode) {
            if ((selectedTimeModeCombo != null)
                    && (selectedTimeModeCombo.isDisposed() == false)) {
                selectedTimeModeCombo.setText(mode.getName());
            }
            if (selectedTimeModeAction != null) {
                selectedTimeModeAction.setSelectedChoice(mode.getName());
            }
        }

        @Override
        @Deprecated
        public List<IContributionItem> getContextMenuItems(String identifier,
                Date persistedTimestamp) {
            return viewPart.getContextMenuItems(identifier, persistedTimestamp);
        }
    };

    /**
     * The body parent panel.
     */
    private Composite bodyPanel;

    /**
     * Console tree.
     */
    private final ConsoleTree consoleTree = new ConsoleTree(consoleTreeUser,
            FORM_MARGIN_WIDTH, FORM_MARGIN_HEIGHT);

    /**
     * Layout data for the console tree.
     */
    private final FormData consoleTreeLayoutData = new FormData();

    /**
     * Separator label between the tree and the controls below it, if any.
     */
    private Label separator;

    /**
     * Panel which contains the time control buttons.
     */
    private Composite buttonsPanel;

    /**
     * The panel which contains the selected time mode combo button.
     */
    private Composite comboBoxPanel;

    /**
     * Map of button identifiers to the associated navigation buttons.
     */
    private final Map<String, Button> buttonsForIdentifiers = new HashMap<>();

    /**
     * Map of button identifiers to the associated toolbar navigation actions.
     */
    private final Map<String, Action> actionsForButtonIdentifiers = new HashMap<>();

    /**
     * Selected time mode combo box.
     */
    private Combo selectedTimeModeCombo = null;

    /**
     * Selected time mode action, built for the toolbar and passed to this
     * object if appropriate.
     */
    private ComboAction selectedTimeModeAction = null;

    /**
     * Set of basic resources created for use in this window, to be disposed of
     * when this window is disposed of.
     */
    private final Set<Resource> resources = new HashSet<>(
            ConsoleView.BUTTON_IDENTIFIERS.size(), 1.0f);

    /**
     * View part that is using this as its body.
     * 
     * @deprecated Should no longer be needed once the
     *             {@link ConsoleViewPart#getContextMenuItems(String, Date)} is
     *             not being used.
     */
    @Deprecated
    private final ConsoleViewPart viewPart;

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     */
    ConsoleBody(ConsoleViewPart viewPart) {
        this.viewPart = viewPart;
        consoleTreeLayoutData.left = new FormAttachment(0, 0);
        consoleTreeLayoutData.top = new FormAttachment(0, 0);
        consoleTreeLayoutData.bottom = new FormAttachment(100, -1
                * BUTTON_PANEL_HEIGHT);
        consoleTreeLayoutData.right = new FormAttachment(100, 0);
    }

    // Public Methods

    @Override
    public ICommandInvoker<Sort> getSortInvoker() {
        return (consoleTree != null ? consoleTree.getSortInvoker() : null);
    }

    @Override
    public IStateChanger<TimeRangeType, Range<Long>> getTimeRangeChanger() {
        return (consoleTree != null ? consoleTree.getTimeRangeChanger() : null);
    }

    @Override
    public IStateChanger<String, ConsoleColumns> getColumnsChanger() {
        return (consoleTree != null ? consoleTree.getColumnsChanger() : null);
    }

    @Override
    public IStateChanger<String, Object> getColumnFiltersChanger() {
        return (consoleTree != null ? consoleTree.getColumnFiltersChanger()
                : null);
    }

    @Override
    public IListStateChanger<String, TabularEntity> getTreeContentsChanger() {
        return (consoleTree != null ? consoleTree.getTreeContentsChanger()
                : null);
    }

    @Override
    public void setCurrentTime(Date currentTime) {
        consoleTree.setCurrentTime(currentTime);
    }

    @Override
    public void setSorts(ImmutableList<Sort> sorts) {
        consoleTree.setSorts(sorts);
    }

    @Override
    public void setTimeResolution(TimeResolution timeResolution,
            Date currentTime) {
        consoleTree.setTimeResolution(timeResolution, currentTime);
    }

    @Override
    public void setActiveCountdownTimers(
            ImmutableMap<String, CountdownTimer> countdownTimersForEventIdentifiers) {
        consoleTree
                .setActiveCountdownTimers(countdownTimersForEventIdentifiers);
    }

    @Override
    public void handleContributionItemUpdate(IContributionItem item,
            String text, boolean enabled) {
        consoleTree.handleContributionItemUpdate(item, text, enabled);
    }

    // Package-Private Methods

    /**
     * Initialize the display.
     * 
     * @param selectedTime
     *            Selected time.
     * @param currentTime
     *            Current time.
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as an epoch
     *            time range in milliseconds.
     * @param timeResolution
     *            Time resolution.
     * @param filterMegawidgets
     *            Maps acting as megawidget specifiers to be used for filter
     *            megawidgets.
     * @param showControlsInToolBar
     *            Flag indicating whether the controls (navigation buttons,
     *            etc.) are to be shown in the toolbar. If false, they are
     *            provided at the bottom of this composite instead.
     */
    void initialize(Date selectedTime, Date currentTime, long visibleTimeRange,
            TimeResolution timeResolution,
            ImmutableList<Map<String, Object>> filterMegawidgets,
            boolean showControlsInToolBar) {

        /*
         * Initialize the console tree.
         */
        consoleTree.initialize(filterMegawidgets, currentTime, selectedTime,
                visibleTimeRange, timeResolution);

        /*
         * If the controls are to be shown in the toolbar, hide the ones in the
         * composite.
         */
        if (showControlsInToolBar) {

            /*
             * Make the two panels and the separator at the bottom invisible.
             */
            if (separator != null) {
                separator.dispose();
                comboBoxPanel.dispose();
                buttonsPanel.dispose();
            }

            /*
             * Tell the tree to fill the space that was taken up by the bottom
             * row of controls, and lay out the temporal display again.
             */
            consoleTreeLayoutData.bottom = new FormAttachment(100, 0);
            if (bodyPanel != null) {
                bodyPanel.layout(true);
            }
        }
    }

    /**
     * Set the map of toolbar widget identifiers to their actions and the
     * selected time mode action. These are constructed elsewhere and provided
     * to this object if appropriate.
     * 
     * @param map
     *            Map of toolbar widget identifiers to their actions.
     * @param selectedTimeModeAction
     *            Selected time mode action.
     */
    void setToolBarActions(final Map<String, Action> map,
            ComboAction selectedTimeModeAction) {
        actionsForButtonIdentifiers.clear();
        actionsForButtonIdentifiers.putAll(map);
        ITemporalDisplay temporalDisplay = consoleTree.getTemporalDisplay();
        for (Action action : actionsForButtonIdentifiers.values()) {
            ((ITemporallyAware) action).setTemporalDisplay(temporalDisplay);
        }
        this.selectedTimeModeAction = selectedTimeModeAction;
        ((ConsoleView.ITemporallyAware) this.selectedTimeModeAction)
                .setTemporalDisplay(temporalDisplay);
        this.selectedTimeModeAction.setSelectedChoice(temporalDisplay
                .getSelectedTimeMode().getName());
    }

    /**
     * Create the area of the dialog window.
     * 
     * @param parent
     *            Parent of the area to be created.
     * @return Dialog area that was created.
     */
    Composite createDisplayComposite(Composite parent) {

        /*
         * Create the composite holding all the widgets.
         */
        bodyPanel = new Composite(parent, SWT.NONE);
        bodyPanel.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                for (Resource resource : resources) {
                    resource.dispose();
                }
            }
        });
        bodyPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /*
         * Use a form layout for the new composite.
         */
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = FORM_MARGIN_WIDTH;
        formLayout.marginHeight = FORM_MARGIN_HEIGHT;
        bodyPanel.setLayout(formLayout);

        /*
         * Create the tree and button panel components.
         */
        Control tree = consoleTree.createWidgets(bodyPanel,
                consoleTreeLayoutData);
        createComboBox(bodyPanel, tree);
        createButtonsPanel(bodyPanel);

        /*
         * Ensure the buttons start off with the right state.
         */
        updateRulerButtonsState(consoleTree.getMinimumVisibleTime(),
                consoleTree.getMaximumVisibleTime(),
                consoleTree.getZoomedOutRange(), consoleTree.getZoomedInRange());

        /*
         * Return the overall composite holding the components.
         */
        return bodyPanel;
    }

    /**
     * Respond to the view part in which the tree is embedded being attached to
     * the main window or detached from it.
     */
    void viewPartAttachedOrDetached() {
        consoleTree.viewPartAttachedOrDetached();
    }

    /**
     * Set the focus to the tree.
     */
    void setFocus() {
        consoleTree.setFocus();
    }

    // Private Methods

    /**
     * Create a command button.
     * 
     * @param panel
     *            Panel in which to place the button.
     * @param identifier
     *            Identifier of the button.
     * @param image
     *            Image to be used for the button.
     * @param description
     *            Description for use in a tooltip for the button.
     * @param listener
     *            Listener for selection events for the button.
     */
    private void createCommandButton(Composite panel, String identifier,
            Image image, String description, SelectionListener listener) {
        Button button = new Button(panel, SWT.PUSH);
        button.setImage(image);
        button.setToolTipText(description);
        button.setData(identifier);
        button.addSelectionListener(listener);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        button.setLayoutData(gridData);
        buttonsForIdentifiers.put(identifier, button);
    }

    /**
     * Create the selected time mode combo box.
     * 
     * @param parent
     *            Composite to contain the combo box.
     * @param above
     *            Control above the combo box to be created, needed for layout
     *            purposes.
     */
    private void createComboBox(Composite parent, Control above) {
        comboBoxPanel = new Composite(parent, SWT.NONE);
        GridLayout headerLayout = new GridLayout(1, false);
        headerLayout.horizontalSpacing = headerLayout.verticalSpacing = 0;
        headerLayout.marginWidth = 10;
        headerLayout.marginHeight = 0;
        headerLayout.marginTop = 3;
        comboBoxPanel.setLayout(headerLayout);

        /*
         * Create the selected time mode combo box.
         */
        selectedTimeModeCombo = new Combo(comboBoxPanel, SWT.READ_ONLY);
        selectedTimeModeCombo.removeAll();
        selectedTimeModeCombo
                .setToolTipText(ConsoleView.SELECTED_TIME_MODE_TEXT);
        for (SelectedTimeMode choice : SelectedTimeMode.values()) {
            selectedTimeModeCombo.add(choice.getName());
        }
        selectedTimeModeCombo.setText(consoleTree.getTemporalDisplay()
                .getSelectedTimeMode().getName());
        selectedTimeModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                consoleTree.getTemporalDisplay().setSelectedTimeMode(
                        SelectedTimeMode.valueOf(selectedTimeModeCombo
                                .getText()));
            }
        });

        /*
         * Create a visual separator between the combo box and anything else
         * next to it, and the control above.
         */
        separator = new Label(parent, SWT.SEPARATOR | SWT.SHADOW_OUT
                | SWT.HORIZONTAL);
        FormData separatorFormData = new FormData();
        separatorFormData.left = new FormAttachment(0, 0);
        separatorFormData.top = new FormAttachment(above, 0);
        separatorFormData.width = 10000;
        separatorFormData.height = 5;
        separator.setLayoutData(separatorFormData);

        /*
         * Lay out the combo box.
         */
        FormData comboTimeFormData = new FormData();
        comboTimeFormData.left = new FormAttachment(0, 0);
        comboTimeFormData.top = new FormAttachment(separator, 0);
        comboBoxPanel.setLayoutData(comboTimeFormData);
    }

    /**
     * Create the panel of buttons for controlling the time ruler.
     * 
     * @param parent
     *            Composite which will contain the button panel.
     */
    private void createButtonsPanel(Composite parent) {

        /*
         * Create the button panel.
         */
        buttonsPanel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(7, false);
        layout.horizontalSpacing = layout.verticalSpacing = 0;
        layout.marginWidth = 10;
        layout.marginHeight = 0;
        layout.marginTop = 3;
        buttonsPanel.setLayout(layout);
        SelectionListener buttonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.widget.getData().equals(ConsoleView.BUTTON_ZOOM_OUT)) {
                    consoleTree.getTemporalDisplay().zoomTimeOut();
                } else if (e.widget.getData().equals(
                        ConsoleView.BUTTON_PAGE_BACKWARD)) {
                    consoleTree.getTemporalDisplay().pageTimeBack();
                } else if (e.widget.getData().equals(
                        ConsoleView.BUTTON_PAN_BACKWARD)) {
                    consoleTree.getTemporalDisplay().panTimeBack();
                } else if (e.widget.getData().equals(
                        ConsoleView.BUTTON_CURRENT_TIME)) {
                    consoleTree.getTemporalDisplay().showCurrentTime();
                } else if (e.widget.getData().equals(
                        ConsoleView.BUTTON_PAN_FORWARD)) {
                    consoleTree.getTemporalDisplay().panTimeForward();
                } else if (e.widget.getData().equals(
                        ConsoleView.BUTTON_PAGE_FORWARD)) {
                    consoleTree.getTemporalDisplay().pageTimeForward();
                } else if (e.widget.getData()
                        .equals(ConsoleView.BUTTON_ZOOM_IN)) {
                    consoleTree.getTemporalDisplay().zoomTimeIn();
                }
            }
        };
        for (int j = 0; j < ConsoleView.BUTTON_IDENTIFIERS.size(); j++) {
            Image image = IconUtil.getImage(HazardServicesActivator
                    .getDefault().getBundle(),
                    ConsoleView.BUTTON_IDENTIFIERS.get(j)
                            + PNG_FILE_NAME_SUFFIX, Display.getCurrent());
            resources.add(image);
            createCommandButton(buttonsPanel,
                    ConsoleView.BUTTON_IDENTIFIERS.get(j), image,
                    ConsoleView.BUTTON_DESCRIPTIONS.get(j), buttonListener);
        }
        FormData buttonFormData = new FormData();
        buttonFormData.left = new FormAttachment(comboBoxPanel, 20);
        buttonFormData.top = new FormAttachment(comboBoxPanel, 0, SWT.TOP);
        buttonFormData.bottom = new FormAttachment(comboBoxPanel, 0, SWT.BOTTOM);
        buttonsPanel.setLayoutData(buttonFormData);
    }

    /**
     * Update the ruler buttons' states in response to a temporal change.
     * 
     * @param lowerVisibleValue
     *            Lower boundary (inclusive) of the new visible value range, as
     *            an epoch time in milliseconds.
     * @param upperVisibleValue
     *            Upper boundary (inclusive) of the new visible value range, as
     *            an epoch time in milliseconds.
     * @param zoomedOutRange
     *            Theoretical range that would be used if the time line ruler
     *            was zoomed out by one step, in milliseconds.
     * @param zoomedInRange
     *            Theoretical range that would be used if the time line ruler
     *            was zoomed in by one step, in milliseconds.
     */
    private void updateRulerButtonsState(long lowerVisibleValue,
            long upperVisibleValue, long zoomedOutRange, long zoomedInRange) {

        /*
         * Update the buttons along the bottom of the view if they exist.
         */
        if (comboBoxPanel.isDisposed() == false) {
            buttonsForIdentifiers.get(ConsoleView.BUTTON_ZOOM_OUT).setEnabled(
                    zoomedOutRange <= WidgetUtilities
                            .getTimeLineRulerMaximumVisibleTimeRange());
            buttonsForIdentifiers.get(ConsoleView.BUTTON_ZOOM_IN).setEnabled(
                    zoomedInRange >= WidgetUtilities
                            .getTimeLineRulerMinimumVisibleTimeRange());
            buttonsForIdentifiers.get(ConsoleView.BUTTON_PAGE_BACKWARD)
                    .setEnabled(lowerVisibleValue > HazardConstants.MIN_TIME);
            buttonsForIdentifiers.get(ConsoleView.BUTTON_PAN_BACKWARD)
                    .setEnabled(lowerVisibleValue > HazardConstants.MIN_TIME);
            buttonsForIdentifiers.get(ConsoleView.BUTTON_PAN_FORWARD)
                    .setEnabled(upperVisibleValue < HazardConstants.MAX_TIME);
            buttonsForIdentifiers.get(ConsoleView.BUTTON_PAGE_FORWARD)
                    .setEnabled(upperVisibleValue < HazardConstants.MAX_TIME);
        }

        /*
         * Update the toolbar buttons if they exist.
         */
        if (actionsForButtonIdentifiers.get(ConsoleView.BUTTON_ZOOM_OUT) != null) {
            actionsForButtonIdentifiers.get(ConsoleView.BUTTON_ZOOM_OUT)
                    .setEnabled(
                            zoomedOutRange <= WidgetUtilities
                                    .getTimeLineRulerMaximumVisibleTimeRange());
            actionsForButtonIdentifiers.get(ConsoleView.BUTTON_ZOOM_IN)
                    .setEnabled(
                            zoomedInRange >= WidgetUtilities
                                    .getTimeLineRulerMinimumVisibleTimeRange());
            actionsForButtonIdentifiers.get(ConsoleView.BUTTON_PAGE_BACKWARD)
                    .setEnabled(lowerVisibleValue > HazardConstants.MIN_TIME);
            actionsForButtonIdentifiers.get(ConsoleView.BUTTON_PAN_BACKWARD)
                    .setEnabled(lowerVisibleValue > HazardConstants.MIN_TIME);
            actionsForButtonIdentifiers.get(ConsoleView.BUTTON_PAN_FORWARD)
                    .setEnabled(upperVisibleValue < HazardConstants.MAX_TIME);
            actionsForButtonIdentifiers.get(ConsoleView.BUTTON_PAGE_FORWARD)
                    .setEnabled(upperVisibleValue < HazardConstants.MAX_TIME);
        }
    }
}
