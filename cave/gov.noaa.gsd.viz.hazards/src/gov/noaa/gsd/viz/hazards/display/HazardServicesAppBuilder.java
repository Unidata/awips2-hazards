/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)`
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.alerts.AlertsPresenter;
import gov.noaa.gsd.viz.hazards.alerts.AlertsView;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.ConsoleView;
import gov.noaa.gsd.viz.hazards.display.action.HazardServicesCloseAction;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailView;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.producteditor.ProductEditorPresenter;
import gov.noaa.gsd.viz.hazards.producteditor.ProductEditorView;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingView;
import gov.noaa.gsd.viz.hazards.setting.SettingsPresenter;
import gov.noaa.gsd.viz.hazards.setting.SettingsView;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayer;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayerResourceData;
import gov.noaa.gsd.viz.hazards.timer.HazardServicesTimer;
import gov.noaa.gsd.viz.hazards.tools.ToolsPresenter;
import gov.noaa.gsd.viz.hazards.tools.ToolsView;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.VizConstants;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.globals.IGlobalChangedListener;
import com.raytheon.uf.viz.core.globals.VizGlobalsManager;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;

/**
 * Description: Builder of the Hazard Services application and its components
 * The app builder directly communicates with each view's presenter and provides
 * methods for the creation, update and destruction of these views. It is a
 * singleton.
 * <p>
 * The app builder listens to messages from CAVE which affect the Hazard
 * Services application as a whole (e.g. frame changes, perspective changes,
 * etc). In contrast, the Hazard Services Message Listener listens specifically
 * for messages from the presenters.
 * <p>
 * The app builder also keeps track of the state of the Hazard Services
 * application as a whole.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2013            bryon.lawrence      Initial creation
 * May 10, 2013            Chris.Golden        Change HID to Eclipse view implementation.
 * Jun 20, 2013   1277     Chris.Golden        Added code to support the specification
 *                                             of a Python side effects applier anywhere
 *                                             in Hazard Services.
 * Jul 09, 2013    585     Chris.Golden        Changed to support loading from bundle,
 *                                             including making this class no longer a
 *                                             singleton, and ensuring that any previously
 *                                             class-scoped variables are now member data.
 * </pre>
 * 
 * @author The Hazard Services Team
 * @version 1.0
 */
public class HazardServicesAppBuilder implements IPerspectiveListener4,
        IGlobalChangedListener, IWorkbenchListener {

    // Public Static Constants

    /**
     * Preferences key used to determine whether or not to start off the views
     * as hidden when loaded from a bundle.
     */
    private static final String START_VIEWS_HIDDEN_WHEN_LOADED_FROM_BUNDLE = "startViewsHiddenWhenLoadedFromBundle";

    /**
     * Timer interval in milliseconds.
     */
    public static final long TIMER_UPDATE_MS = TimeUnit.SECONDS.toMillis(10);

    /**
     * Temporal display originator.
     */
    public static final String TEMPORAL_ORIGINATOR = "Temporal";

    /**
     * Hazard info dialog originator.
     */
    public static final String HAZARD_INFO_ORIGINATOR = "HID";

    /**
     * Setting dialog originator.
     */
    public static final String SETTING_DIALOG_ORIGINATOR = "SettingDialog";

    /**
     * Spatial display originator.
     */
    public static final String SPATIAL_ORIGINATOR = "Spatial";

    /**
     * CAVE originator.
     */
    public static final String CAVE_ORIGINATOR = "Cave";

    /**
     * Hard-coded canned case time. For use in Operation Mode for now.
     */
    public final static String CANNED_TIME = "1297137600000"; // 4Z

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesAppBuilder.class);

    // Private Variables

    /**
     * Event bus.
     */
    private EventBus eventBus;

    /**
     * Message handler for all incoming messages.
     */
    private HazardServicesMessageHandler messageHandler;

    /**
     * Console presenter.
     */
    private ConsolePresenter consolePresenter = null;

    /**
     * Settings presenter.
     */
    private SettingsPresenter settingsPresenter = null;

    /**
     * Tools presenter.
     */
    private ToolsPresenter toolsPresenter = null;

    /**
     * Hazard detail presenter.
     */
    private HazardDetailPresenter hazardDetailPresenter = null;

    /**
     * Alerts presenter.
     */
    private AlertsPresenter alertsPresenter = null;

    /**
     * Spatial presenter.
     */

    private SpatialPresenter spatialPresenter = null;

    /**
     * Product staging presenter.
     */
    private ProductStagingPresenter productStagingPresenter = null;

    /**
     * Product editor presenter.
     */
    private ProductEditorPresenter productEditorPresenter = null;

    /**
     * List of all presenters.
     */
    private final List<HazardServicesPresenter<?>> presenters = Lists
            .newArrayList();

    /**
     * Descriptor of the current CAVE perspective.
     */
    private IPerspectiveDescriptor currentPerspectiveDescriptor = null;

    /**
     * The setting with which Hazard Services is started.
     */
    private String initialSetting;

    /**
     * Timer used for interval updates.
     */
    private HazardServicesTimer timer = null;

    /**
     * Flag indicating whether or not the app builder is undergoing disposal.
     */
    private boolean disposing = false;

    /**
     * Flag indicating whether or not the app builder is undergoing forced
     * shutdown.
     */
    private boolean forcedShutdown = false;

    /**
     * Current time, represented as epoch time in milliseconds given as a
     * string.
     */
    private String currentTime;

    /**
     * Viz resource associated with this builder.
     */
    private ToolLayer toolLayer;

    // Public Static Methods

    /**
     * Get a true/false, or OK/cancel, or yes/no answer from the user.
     * 
     * @param question
     *            Question to be asked.
     * @return True if the answer was true/OK/yes, false otherwise.
     */
    public static boolean getUserAnswerToQuestion(String question) {
        return MessageDialog.openQuestion(null, "Hazard Services", question);
    }

    /**
     * Warn the user of a potential problem.
     * 
     * @param problem
     *            Question to be asked.
     */
    public static void warnUser(String problem) {
        MessageDialog.openWarning(null, "Hazard Services", problem);
    }

    // Private Constructors

    /**
     * Consruct a standard instance.
     * 
     * @param toolLayer
     *            Viz resource associated with this app builder, if a new one is
     *            to be created.
     * @param loadedFromBundle
     *            Flag indicating whether or not Hazard Services is being
     *            instantiated as a result of a bundle load.
     * @throws VizException
     *             If an exception occurs while attempting to build the Hazard
     *             Services application.
     */
    public HazardServicesAppBuilder(ToolLayer toolLayer,
            boolean loadedFromBundle) throws VizException {
        initialize(toolLayer, loadedFromBundle);
    }

    // Methods

    /**
     * Initializes an instance of the HazardServicesAppBuilder
     * 
     * @param toolLayer
     *            Tool layer to be used with this builder.
     * @param loadedFromBundle
     *            Flag indicating whether or not Hazard Services is being
     *            instantiated as a result of a bundle load.
     * @throws VizException
     *             If an error occurs while attempting to initialize.
     */
    private void initialize(ToolLayer toolLayer, boolean loadedFromBundle)
            throws VizException {
        currentTime = null;
        this.toolLayer = toolLayer;
        this.eventBus = new EventBus();

        ((ToolLayerResourceData) toolLayer.getResourceData())
                .setAppBuilder(this);

        PlatformUI.getWorkbench().addWorkbenchListener(this);

        // Initialize the Python side effects applier.
        PythonSideEffectsApplier.initialize();

        /*
         * For testing and demos, force DRT for operational mode start HS
         * according to CAVE clock for practice mode. Needed to do this, because
         * the user can interact with the CAVE status line clock only in
         * practice mode.
         */
        currentTime = Long.toString(SimulatedTime.getSystemTime().getTime()
                .getTime());

        currentPerspectiveDescriptor = VizWorkbenchManager.getInstance()
                .getActiveEditor().getSite().getPage().getPerspective();

        initialSetting = "";

        /*
         * We will eventually make Hazard Services perspective agnostic. For
         * example, the settings definitions will contain mappings to
         * perspectives.
         */
        if (currentPerspectiveDescriptor.getId().contains("D2D")) {
            initialSetting = "TOR";
        } else if (currentPerspectiveDescriptor.getId().contains("GFE")) {
            initialSetting = "WSW";
        } else if (currentPerspectiveDescriptor.getId().contains("Hydro")) {
            initialSetting = "Flood";
        } else if (currentPerspectiveDescriptor.getId().contains("MPE")) {
            initialSetting = "Flood";
        }
        messageHandler = new HazardServicesMessageHandler(this, currentTime,
                initialSetting, "", "{}");
        new HazardServicesMessageListener(messageHandler, eventBus);

        /*
         * Create the Spatial Display layer in the active CAVE editor. This is
         * what hazards will be drawn on.
         */

        createSpatialDisplay(toolLayer);

        // Determine whether or not views are to be hidden at first if this
        // app builder is being created as the result of a bundle load.
        IPreferenceStore preferenceStore = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        loadedFromBundle = (loadedFromBundle && ((preferenceStore
                .contains(START_VIEWS_HIDDEN_WHEN_LOADED_FROM_BUNDLE) == false) || preferenceStore
                .getBoolean(START_VIEWS_HIDDEN_WHEN_LOADED_FROM_BUNDLE)));

        // Open the console.
        createConsole(loadedFromBundle);

        // Create the settings view.
        createSettingsDisplay();

        // Create the tools view.
        createToolsDisplay();

        // Create the hazard detail view.
        createHazardDetailDisplay(loadedFromBundle);

        // Create the alerts view.
        createAlertsDisplay();

        // Create the product staging presenter and view.
        createProductStagingDisplay();

        // Create the product editor presenter and view.
        createProductEditorDisplay();

        // Allow all views to contribute to the console menubar.
        buildMenuBar();

        // Allow all views to contribute to the console toolbar.
        buildToolBar();

        spatialPresenter.updateCaveSelectedTime();

        // Add THIS HazardServicesAppBuilder as a perspective listener.
        // when loading a drawing layer
        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .addPerspectiveListener(this);

        // Set the default cursor to an arrow
        spatialPresenter.getView().setCursor(
                SpatialViewCursorTypes.ARROW_CURSOR);

        /*
         * Set the default mouse listener... This is the select mouse handler.
         */

        spatialPresenter.getView().setMouseHandler(
                HazardServicesMouseHandlers.SINGLE_SELECTION, new String[] {});

        // Tell the spatial display to use the setting's zoom parameters.
        // spatialPresenter.useSettingZoomParameters(initialSetting);

        // Set the time line duration.
        messageHandler.updateConsoleVisibleTimeDelta();

        // Add the HazardServicesAppBuilder as a listener for frame changes.
        VizGlobalsManager.addListener(VizConstants.FRAMES_ID, this);
        VizGlobalsManager.addListener(VizConstants.LOOPING_ID, this);

        spatialPresenter.getView().redoTimeMatching();

        // Send the current frame information to the session manager.
        messageHandler.sendFrameInformationToSessionManager();

        // Start the current time update timer.
        timer = new HazardServicesTimer(TIMER_UPDATE_MS, true, eventBus);
        timer.start();

    }

    /**
     * Build the menu bar.
     */
    private void buildMenuBar() {
        ConsoleView consoleView = (ConsoleView) consolePresenter.getView();
        List<IView<Action, RCPMainUserInterfaceElement>> contributors = Lists
                .newArrayList();
        contributors.add(consoleView);
        consoleView.acceptContributionsToMainUI(contributors,
                RCPMainUserInterfaceElement.MENUBAR);
    }

    /**
     * Build the toolbar.
     */
    private void buildToolBar() {
        ConsoleView consoleView = (ConsoleView) consolePresenter.getView();
        List<IView<Action, RCPMainUserInterfaceElement>> contributors = Lists
                .newArrayList();
        contributors.add((SettingsView) settingsPresenter.getView());
        contributors.add((ToolsView) toolsPresenter.getView());
        contributors.add((HazardDetailView) hazardDetailPresenter.getView());
        contributors.add((AlertsView) alertsPresenter.getView());
        contributors.add((SpatialView) spatialPresenter.getView());
        contributors.add(consoleView);
        consoleView.acceptContributionsToMainUI(contributors,
                RCPMainUserInterfaceElement.TOOLBAR);
    }

    /**
     * Create (or if already created, recreate) the console.
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not this display is being
     *            instantiated as a result of a bundle load.
     */
    private void createConsole(boolean loadedFromBundle) {
        if (consolePresenter == null) {
            consolePresenter = new ConsolePresenter(
                    messageHandler.getModelProxy(), new ConsoleView(
                            loadedFromBundle), eventBus);
            presenters.add(consolePresenter);
        } else {
            consolePresenter.setView(new ConsoleView(loadedFromBundle));
        }
    }

    /**
     * Dispose of the console view.
     */
    private void destroyConsole() {
        consolePresenter.getView().dispose();
    }

    /**
     * Dispose of the hazard detail view.
     */
    private void destroyHazardDetailDisplay() {
        hazardDetailPresenter.getView().dispose();
    }

    /**
     * Create the alerts view and presenter.
     */
    private void createAlertsDisplay() {
        if (alertsPresenter == null) {

            alertsPresenter = new AlertsPresenter(
                    messageHandler.getModelProxy(), new AlertsView(), eventBus);
            presenters.add(alertsPresenter);
        }
    }

    /**
     * Create the hazard detail view and presenter.
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not this display is being
     *            instantiated as a result of a bundle load.
     */
    private void createHazardDetailDisplay(boolean loadedFromBundle) {
        if (hazardDetailPresenter == null) {
            hazardDetailPresenter = new HazardDetailPresenter(
                    messageHandler.getModelProxy(), new HazardDetailView(
                            loadedFromBundle), eventBus);
            presenters.add(hazardDetailPresenter);
        } else {
            hazardDetailPresenter
                    .setView(new HazardDetailView(loadedFromBundle));
        }
    }

    /**
     * Create the settings view and presenter.
     * 
     * @param
     * @return
     */
    private void createSettingsDisplay() {
        if (settingsPresenter == null) {

            settingsPresenter = new SettingsPresenter(
                    messageHandler.getModelProxy(), new SettingsView(),
                    eventBus);
            presenters.add(settingsPresenter);
        }
    }

    /**
     * Create the tools view and presenter.
     */
    private void createToolsDisplay() {
        if (toolsPresenter == null) {
            toolsPresenter = new ToolsPresenter(messageHandler.getModelProxy(),
                    new ToolsView(), eventBus);
            presenters.add(toolsPresenter);
        }
    }

    /**
     * Create the spatial display. If it already exists, recreate it.
     * <p>
     * We'll have to see if we ever want to recreate it.
     */
    private void createSpatialDisplay(ToolLayer toolLayer) {
        if (spatialPresenter == null) {
            spatialPresenter = new SpatialPresenter(
                    messageHandler.getModelProxy(), new SpatialView(toolLayer),
                    eventBus);
            presenters.add(spatialPresenter);
        } else {
            spatialPresenter.getView().dispose();
            spatialPresenter.setView(new SpatialView(toolLayer));
        }
    }

    /**
     * Create the product staging view and presenter.
     */
    private void createProductStagingDisplay() {
        if (productStagingPresenter == null) {
            productStagingPresenter = new ProductStagingPresenter(
                    messageHandler.getModelProxy(), new ProductStagingView(),
                    eventBus);
            presenters.add(productStagingPresenter);
        } else {
            productStagingPresenter.getView().dispose();
            productStagingPresenter.setView(new ProductStagingView());
        }
    }

    /**
     * Create the product editor view and presenter.
     */
    private void createProductEditorDisplay() {
        if (productEditorPresenter == null) {
            productEditorPresenter = new ProductEditorPresenter(
                    messageHandler.getModelProxy(), new ProductEditorView(),
                    eventBus);
            presenters.add(productEditorPresenter);
        } else {
            productEditorPresenter.getView().dispose();
            productEditorPresenter.setView(new ProductEditorView());
        }
    }

    /**
     * Shut down this instance of hazard services, disposing of all allocated
     * resources.
     */
    public void dispose() {
        closeHazardServices(null);
    }

    /**
     * Ensure that the views are visible.
     */
    public void ensureViewsVisible() {
        consolePresenter.getView().ensureVisible();
        hazardDetailPresenter.showHazardDetail(false);
    }

    /**
     * Get the event bus.
     * 
     * @return Event bus.
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Get context menu entries.
     * 
     * @return JSON string containing the list of context menu entries.
     */
    public String getContextMenuEntries() {
        return messageHandler.getModelProxy().getContextMenuEntries();
    }

    /**
     * Get the current setting.
     * 
     * @return JSON string containing the mapping of key-value pairs defining
     *         the setting.
     */
    public String getSetting() {
        return messageHandler.getModelProxy().getDynamicSettings();
    }

    /**
     * Set the current setting.
     * 
     * @param setting
     *            JSON string containing the mapping of key-value pairs defining
     *            the setting.
     */
    public void setSetting(String setting) {
        messageHandler.dynamicSettingChanged(setting);
    }

    /**
     * Notify all presenters of one or more model changes that occurred.
     * 
     * @param changed
     *            Set of model elements that have changed.
     */
    public void notifyModelChanged(EnumSet<IHazardServicesModel.Element> changed) {
        for (HazardServicesPresenter<?> presenter : presenters) {
            presenter.modelChanged(changed);
        }
    }

    /**
     * Show the product staging dialog.
     * 
     * @param issueFlag
     *            Whether or not this is being called as a result of an issue
     *            action. productList List of products to stage.
     * @param productStagingInfo
     *            Information to be passed to the product staging dialog in the
     *            form of a dictionary.
     */
    public void showProductStagingView(boolean issueFlag,
            Dict productStagingInfo) {
        productStagingPresenter.showProductStagingDetail(issueFlag,
                productStagingInfo);
    }

    /**
     * Display the product editor dialog with the specified product info.
     * 
     * @param productInfo
     *            JSON string containing the product information.
     */
    public void showProductEditorView(String productInfo) {
        productEditorPresenter.showProductEditorDetail(productInfo);
    }

    /**
     * Close the product editor dialog.
     */
    public void closeProductEditorView() {
        productEditorPresenter.getView().closeProductEditorDialog();
    }

    /**
     * Show the hazard detail subview.
     */
    public void showHazardDetail() {
        hazardDetailPresenter.showHazardDetail(false);
    }

    /**
     * Hide the hazard detail subview.
     */
    public void hideHazardDetail() {
        // hazardDetailPresenter.hideHazardDetail(false);
        hazardDetailPresenter.hideHazardDetail();
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
    public void showToolParameterGatherer(String toolName, String jsonParams) {
        toolsPresenter.showToolParameterGatherer(toolName, jsonParams);
    }

    @Override
    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        statusHandler.debug("HazardServicesAppBuilder.perspectiveActivated(): "
                + "perspective activated: page = " + page + ", perspective = "
                + perspective.getDescription());

        // Close Hazard Services if there is no active editor in the new pers-
        // pective.
        if (VizWorkbenchManager.getInstance().getActiveEditor() == null) {
            closeHazardServices("Hazard Services cannot run in the "
                    + perspective.getLabel()
                    + " perspective, and must therefore shut down.");
            return;
        }

        // Recreate the console and the hazard detail views.
        createConsole(false);
        createHazardDetailDisplay(false);

        /*
         * Retrieve and store the descriptor of the newly activated perspective.
         */
        currentPerspectiveDescriptor = perspective;

        /*
         * Check perspective type and reconfigure Hazard Services and its
         * components accordingly.
         */
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                messageHandler.sendFrameInformationToSessionManager();
            }
        });

        // Get the tool layer data from the old tool layer, and delete the
        // latter.
        toolLayer.perspectiveChanging();
        ToolLayerResourceData toolLayerResourceData = (ToolLayerResourceData) toolLayer
                .getResourceData();
        toolLayer.dispose();

        // Create a new tool layer for the new perspective.
        try {
            toolLayer = toolLayerResourceData.construct(new LoadProperties(),
                    ((AbstractEditor) VizWorkbenchManager.getInstance()
                            .getActiveEditor()).getActiveDisplayPane()
                            .getDescriptor());
        } catch (VizException e1) {
            statusHandler.error("Error creating spatial display", e1);
        }

        // Create a new spatial view for the new tool layer.
        addSpatialDisplayResourceToPerspective();
        spatialPresenter.getView().addGeometryDisplayResourceToPerspective();

        // Rebuild the console menubar.
        buildMenuBar();

        // Rebuild the console toolbar.
        buildToolBar();

        // Update the spatial display.
        spatialPresenter.updateSpatialDisplay();
    }

    @Override
    public void perspectiveChanged(IWorkbenchPage page,
            IPerspectiveDescriptor perspective, String changeId) {
        statusHandler
                .debug("HazardServicesAppBuilder.perspectiveChanged(): Perspective changed: "
                        + perspective.getDescription());
    }

    @Override
    public void perspectiveChanged(IWorkbenchPage page,
            IPerspectiveDescriptor perspective,
            IWorkbenchPartReference partRef, String changeId) {

        // No action.
    }

    @Override
    public void perspectiveOpened(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {

        // No action.
    }

    @Override
    public void perspectiveClosed(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {

        // No action.
    }

    @Override
    public void perspectiveDeactivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {

        // No action.
    }

    @Override
    public void perspectiveSavedAs(IWorkbenchPage page,
            IPerspectiveDescriptor oldPerspective,
            IPerspectiveDescriptor newPerspective) {

        // No action.
    }

    @Override
    public void perspectivePreDeactivate(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        destroyConsole();
        destroyHazardDetailDisplay();
    }

    @Override
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        statusHandler
                .debug("HazardServicesAppBuilder.preShutdown(): Workbench = "
                        + workbench + " may shut down, forced = " + forced);

        // Remember whether or not this is a forced shutdown.
        if (forced) {
            forcedShutdown = true;
        }
        return true;
    }

    @Override
    public void postShutdown(IWorkbench workbench) {
        statusHandler
                .debug("HazardServicesAppBuilder.postShutdown(): Workbench = "
                        + workbench + " shutting down.");
        PlatformUI.getWorkbench().removeWorkbenchListener(this);
    }

    @Override
    public void updateValue(IWorkbenchWindow changedWindow, Object value) {
        messageHandler.sendFrameInformationToSessionManager();
    }

    /**
     * Recenter and rezoom the spatial view on the selected hazard.
     */
    public void recenterRezoomDisplay() {
        spatialPresenter.getView().recenterRezoomDisplay();
    }

    /**
     * Return the descriptor of the current perspective.
     * 
     * @return Descriptor of the current perspective.
     */
    public IPerspectiveDescriptor getCurrentPerspectiveDescriptor() {
        return currentPerspectiveDescriptor;
    }

    /**
     * Get the initial setting.
     * 
     * @return Initial setting.
     */
    public String getInitialSetting() {
        return initialSetting;
    }

    /**
     * Set the initial setting to be used.
     * 
     * @param initialSetting
     *            The default setting to use.
     */
    public void setInitialSetting(String initialSetting) {
        this.initialSetting = initialSetting;
    }

    /**
     * Get the timer used for interval updates.
     * 
     * @return Timer.
     */
    public HazardServicesTimer getTimer() {
        return timer;
    }

    /**
     * Request a mouse handler to load in the current editor.
     * 
     * @param mouseHandler
     *            The identifier of the mouseHandler to load
     * @param args
     *            Optional arguments which can be used by the loaded mouse
     *            handler.
     */
    public void requestMouseHandler(HazardServicesMouseHandlers mouseHandler,
            String... args) {
        spatialPresenter.getView().setMouseHandler(mouseHandler, args);
    }

    /**
     * Modify a shape according to the specified action.
     * 
     * @param drawingAction
     *            Action to be executed to modify the shape.
     */
    public void modifyShape(HazardServicesDrawingAction drawingAction) {
        spatialPresenter.getView().modifyShape(drawingAction, this,
                messageHandler);
    }

    /**
     * Sets the cursor to the specified type.
     * 
     * @param cursorType
     *            Specified cursor type.
     */
    public void setCursor(SpatialViewCursorTypes cursorType) {
        spatialPresenter.getView().setCursor(cursorType);
    }

    /**
     * Checks to determine if a geometry overlay needs to be loaded for the
     * specified selected events. If multiple geometry overlays need to be
     * loaded this currently only loads the first overlay.
     * 
     * @param eventIDs
     *            Identifiers of the selected events.
     */
    public void loadGeometryOverlayForSelectedEvent(String eventIDs) {
        spatialPresenter.getView()
                .loadGeometryOverlayForSelectedEvent(eventIDs);
    }

    /**
     * Add the spatial display to the current perspective.
     */
    private void addSpatialDisplayResourceToPerspective() {
        createSpatialDisplay(toolLayer);

        // Set the default mouse listener; this is the select mouse handler.
        spatialPresenter.getView().setMouseHandler(
                HazardServicesMouseHandlers.SINGLE_SELECTION, new String[] {});
    }

    /**
     * Close Hazard Services.
     * 
     * @param message
     *            Message to be displayed for the user; this should either be
     *            <code>null</code>, if no message is required, or else a
     *            sentence ending with a period (.).
     */
    private void closeHazardServices(String message) {
        if (disposing) {
            return;
        }
        disposing = true;

        ((ToolLayerResourceData) toolLayer.getResourceData())
                .setAppBuilder(null);

        /*
         * Notify any objects interested in the shutting down of Hazard
         * Services.
         */
        eventBus.post(new HazardServicesCloseAction());

        boolean showMessageBox = ((message != null) || (forcedShutdown == false));
        boolean saveSessionData = (forcedShutdown || (showMessageBox && MessageDialog
                .openQuestion(null, "Hazard Services", (message == null ? ""
                        : message + " ")
                        + "Do you want to save session data before closing?")));

        if (saveSessionData) {
            // save session data.
        }

        VizGlobalsManager.removeListener(VizConstants.FRAMES_ID, this);
        VizGlobalsManager.removeListener(VizConstants.LOOPING_ID, this);

        // Stop the update timer.
        timer.stopTimer();

        // Remove the HazardServicesAppBuilder from the list of
        // PerspectiveListeners.
        // Exceptions are caught in case CAVE is closing, since the
        // workbench or window may not be around at this point.
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .removePerspectiveListener(this);
        } catch (Exception e) {
            statusHandler.error("Error removing Perspective listener.", e);
        }

        /*
         * Remove the current mouse listener from the editor...
         */
        spatialPresenter.getView().unregisterCurrentMouseHandler();

        for (HazardServicesPresenter<?> presenter : presenters) {
            if ((presenter instanceof ConsolePresenter) == false) {
                presenter.getView().dispose();
            }
        }
        consolePresenter.getView().dispose();
        consolePresenter = null;

        /*
         * Attempt to remove the workbench listener.
         */
        try {
            PlatformUI.getWorkbench().removeWorkbenchListener(this);
        } catch (Exception e) {
            statusHandler.error(
                    "Error removing hazard services workbench listener", e);
        }

        // Exit from DRT.
        SimulatedTime.getSystemTime().setRealTime();
        SimulatedTime.getSystemTime().setFrozen(false);

        statusHandler.debug(messageHandler.getBenchmarkingStats());

        // Close the Jep connection.
        messageHandler.prepareForShutdown();

        // Prepare the Python side effects applier for shutdown.
        PythonSideEffectsApplier.prepareForShutDown();
    }
}
