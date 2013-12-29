/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)`
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.alerts.AlertVizPresenter;
import gov.noaa.gsd.viz.hazards.alerts.AlertsConfigPresenter;
import gov.noaa.gsd.viz.hazards.alerts.AlertsConfigView;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.ConsoleView;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardServicesCloseAction;
import gov.noaa.gsd.viz.hazards.display.test.AutomatedTests;
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
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.tools.ToolsPresenter;
import gov.noaa.gsd.viz.hazards.tools.ToolsView;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;
import gov.noaa.gsd.viz.mvp.IView;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.VizConstants;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.globals.IGlobalChangedListener;
import com.raytheon.uf.viz.core.globals.VizGlobalsManager;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.SessionManagerFactory;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
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
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 22, 2013  787       bryon.lawrence      Removed perspective-specific references
 * Aug 22, 2013   1936     Chris.Golden        Added support for console countdown timers.
 * Aug 29, 2013 1921       bryon.lawrence      Modified loadGeometryOverlayForSelectedEvent to
 *                                             not take a JSON list of event ids.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 14, 2013 1463       bryon.lawrence      Added a method for opening a dialog
 *                                             to warn the user. This is injectable
 *                                             for testing.
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * 
 * </pre>
 * 
 * @author The Hazard Services Team
 * @version 1.0
 */
public class HazardServicesAppBuilder implements IPerspectiveListener4,
        IGlobalChangedListener, IWorkbenchListener, IMessenger {

    // Public Static Constants

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
     * Preferences key used to determine whether or not to start off the views
     * as hidden when loaded from a bundle.
     */
    private static final String START_VIEWS_HIDDEN_WHEN_LOADED_FROM_BUNDLE = "startViewsHiddenWhenLoadedFromBundle";

    /**
     * Run automated tests command string.
     */
    private static final String AUTO_TEST_COMMAND_MENU_TEXT = "Run Automated Tests";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesAppBuilder.class);

    // Private Variables

    /**
     * Event bus.
     */
    private final EventBus eventBus = new EventBus();

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
    private AlertsConfigPresenter alertsConfigPresenter = null;

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
     * Timer used for interval updates.
     */
    private HazardServicesTimer timer = null;

    /**
     * Flag indicating whether or not the app builder is undergoing disposal.
     */
    private boolean disposing = false;

    /**
     * Current time
     */
    private Date currentTime;

    /**
     * Viz resource associated with this builder.
     */
    private ToolLayer toolLayer;

    private ISessionManager sessionManager;

    private AlertVizPresenter alertVizPresenter;

    private IQuestionAnswerer questionAnswerer;

    /**
     * The warner to use to convey warnings to the user.
     */
    private IWarner warner;

    private IMainUiContributor<Action, RCPMainUserInterfaceElement> appBuilderMenubarContributor = null;

    public boolean getUserAnswerToQuestion(String question) {
        return questionAnswerer.getUserAnswerToQuestion(question);
    }

    /**
     * Warn the user. This delegates to the warner either created by the app
     * builder or injected by the client.
     * 
     * @param warning
     *            The warning message to convey to the user
     * @return
     */
    public void warnUser(String warning) {
        warner.warnUser(warning);
    }

    // Private Constructors

    /**
     * Consruct a standard instance.
     * 
     * @param toolLayer
     *            Viz resource associated with this app builder, if a new one is
     *            to be created.
     * @throws VizException
     *             If an exception occurs while attempting to build the Hazard
     *             Services application.
     */
    public HazardServicesAppBuilder(ToolLayer toolLayer) {
        initialize(toolLayer);

    }

    // Methods

    /**
     * Initializes an instance of the HazardServicesAppBuilder
     * 
     * @param toolLayer
     *            Tool layer to be used with this builder.
     * @throws VizException
     *             If an error occurs while attempting to initialize.
     */
    private void initialize(ToolLayer toolLayer) {
        this.toolLayer = toolLayer;

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
        currentTime = SimulatedTime.getSystemTime().getTime();
        this.sessionManager = SessionManagerFactory.getSessionManager(this);

        messageHandler = new HazardServicesMessageHandler(this, currentTime, "");

        new HazardServicesMessageListener(messageHandler, eventBus);

        /**
         * Get a true/false, or OK/cancel, or yes/no answer from the user.
         * 
         * @param question
         *            Question to be asked.
         * @return True if the answer was true/OK/yes, false otherwise.
         */
        this.questionAnswerer = new IQuestionAnswerer() {

            @Override
            public boolean getUserAnswerToQuestion(String question) {
                return MessageDialog.openQuestion(null, "Hazard Services",
                        question);
            }

        };

        this.warner = new IWarner() {

            @Override
            public void warnUser(String warning) {

                MessageDialog.openWarning(null, "Hazard Services", warning);
            }

        };
    }

    /**
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not Hazard Services is being
     *            instantiated as a result of a bundle load.
     * @return
     */
    public void buildGUIs(boolean loadedFromBundle) {

        // Initialize the automated tests if appropriate.
        String autoTestsEnabled = System
                .getenv("HAZARD_SERVICES_AUTO_TESTS_ENABLED");
        if (autoTestsEnabled != null) {
            AutomatedTests tests = new AutomatedTests();
            tests.run(this);

            // Build a menubar contributor that adds the
            // automated test menu item.
            appBuilderMenubarContributor = new IMainUiContributor<Action, RCPMainUserInterfaceElement>() {
                @Override
                public List<? extends Action> contributeToMainUI(
                        RCPMainUserInterfaceElement type) {
                    if (type == RCPMainUserInterfaceElement.MENUBAR) {
                        Action autoTestAction = new BasicAction(
                                AUTO_TEST_COMMAND_MENU_TEXT, null,
                                Action.AS_PUSH_BUTTON, null) {
                            @Override
                            public void run() {
                                eventBus.post(new ConsoleAction(
                                        HazardConstants.RUN_AUTOMATED_TESTS));
                            }
                        };
                        return Lists.newArrayList(autoTestAction);
                    }
                    return Collections.emptyList();
                }
            };
        }

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

        createAlertsConfigDisplay();

        createAlertVizPresenter();

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
        List<IMainUiContributor<Action, RCPMainUserInterfaceElement>> contributors = Lists
                .newArrayList();
        contributors.add(consoleView);
        if (appBuilderMenubarContributor != null) {
            contributors.add(appBuilderMenubarContributor);
        }
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
        contributors.add((AlertsConfigView) alertsConfigPresenter.getView());
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
        ConsoleView consoleView = new ConsoleView(loadedFromBundle);
        if (consolePresenter == null) {
            consolePresenter = new ConsolePresenter(sessionManager,
                    consoleView, eventBus);
            presenters.add(consolePresenter);
        } else {
            consolePresenter.setView(consoleView);
        }
        consolePresenter.initialize(consoleView);
    }

    /**
     * Dispose of the console view.
     */
    private void destroyConsoleView() {
        consolePresenter.getView().dispose();
    }

    /**
     * Dispose of the hazard detail view.
     */
    private void destroyHazardDetailView() {
        hazardDetailPresenter.getView().dispose();
    }

    /**
     * Create the alerts config view and presenter.
     */
    private void createAlertsConfigDisplay() {
        if (alertsConfigPresenter == null) {

            AlertsConfigView alertsConfigView = new AlertsConfigView();
            alertsConfigPresenter = new AlertsConfigPresenter(sessionManager,
                    alertsConfigView, eventBus);
            alertsConfigPresenter.initialize(alertsConfigView);
            presenters.add(alertsConfigPresenter);
        }
    }

    @SuppressWarnings("rawtypes")
    private void createAlertVizPresenter() {
        if (alertVizPresenter == null) {
            IView<?, ?> alertVizView = new IView() {

                @Override
                public void dispose() {
                    /*
                     * Nothing to do here.
                     */
                }

                @Override
                public List<?> contributeToMainUI(Enum type) {
                    /*
                     * No contributions here.
                     */
                    return Lists.newArrayList();
                }

            };
            alertVizPresenter = new AlertVizPresenter(sessionManager,
                    alertVizView, eventBus);
            alertVizPresenter.initialize(alertVizView);
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
        HazardDetailView hazardDetailView = new HazardDetailView(
                loadedFromBundle);
        if (hazardDetailPresenter == null) {
            hazardDetailPresenter = new HazardDetailPresenter(sessionManager,
                    hazardDetailView, eventBus);
            presenters.add(hazardDetailPresenter);
        } else {

            hazardDetailPresenter.setView(hazardDetailView);
        }
        hazardDetailPresenter.initialize(hazardDetailView);
    }

    /**
     * Create the settings view and presenter.
     * 
     * @param
     * @return
     */
    private void createSettingsDisplay() {
        if (settingsPresenter == null) {

            SettingsView settingsView = new SettingsView();
            settingsPresenter = new SettingsPresenter(sessionManager,
                    settingsView, eventBus);
            settingsPresenter.initialize(settingsView);
            presenters.add(settingsPresenter);
        }
    }

    /**
     * Create the tools view and presenter.
     */
    private void createToolsDisplay() {
        if (toolsPresenter == null) {
            ToolsView toolsView = new ToolsView();
            toolsPresenter = new ToolsPresenter(sessionManager, toolsView,
                    eventBus);
            toolsPresenter.initialize(toolsView);
            presenters.add(toolsPresenter);
        }
    }

    /**
     * Create the spatial display. If it already exists, recreate it.
     * <p>
     * We'll have to see if we ever want to recreate it.
     */
    private void createSpatialDisplay(ToolLayer toolLayer) {
        SpatialView spatialView = new SpatialView(toolLayer);
        if (spatialPresenter == null) {
            spatialPresenter = new SpatialPresenter(sessionManager,
                    spatialView, eventBus);
            presenters.add(spatialPresenter);
        } else {
            spatialPresenter.getView().dispose();
            spatialPresenter.setView(spatialView);
        }
        spatialPresenter.initialize(spatialView);
    }

    /**
     * Create the product staging view and presenter.
     */
    private void createProductStagingDisplay() {
        ProductStagingView productStagingView = new ProductStagingView();
        if (productStagingPresenter == null) {
            productStagingPresenter = new ProductStagingPresenter(
                    sessionManager, productStagingView, eventBus);
            presenters.add(productStagingPresenter);
        } else {
            productStagingPresenter.getView().dispose();

            productStagingPresenter.setView(productStagingView);
        }
        productStagingPresenter.initialize(productStagingView);
    }

    /**
     * Create the product editor view and presenter.
     */
    private void createProductEditorDisplay() {
        ProductEditorView productEditorView = new ProductEditorView();
        if (productEditorPresenter == null) {
            productEditorPresenter = new ProductEditorPresenter(sessionManager,
                    productEditorView, eventBus);
            presenters.add(productEditorPresenter);
        } else {
            productEditorPresenter.getView().dispose();

            productEditorPresenter.setView(productEditorView);
        }
        productEditorPresenter.initialize(productEditorView);
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
     * Get the current setting.
     * 
     * @return JSON string containing the mapping of key-value pairs defining
     *         the setting.
     */
    public String getSetting() {
        Settings settings = sessionManager.getConfigurationManager()
                .getSettings();
        JSONConverter jsonConverter = new JSONConverter();
        return jsonConverter.toJson(settings);

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
    public void notifyModelChanged(EnumSet<HazardConstants.Element> changed) {
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
            ProductStagingInfo productStagingInfo) {
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
        hazardDetailPresenter.hideHazardDetail();
    }

    /**
     * Show a tool subview that is used to gather parameter values for a tool
     * that is to be executed.
     * 
     * @param toolName
     *            Name of the tool for which parameters are to be gathered.
     * @param dialogInput
     *            the parameters for this subview. Within the set of all fields
     *            that are defined by these parameters, all the fields
     *            (megawidget specifiers) must have unique identifiers.
     */
    public void showToolParameterGatherer(String toolName,
            Map<String, Serializable> dialogInput) {
        Dict dict = new Dict();
        for (String parameter : dialogInput.keySet()) {
            dict.put(parameter, dialogInput.get(parameter));
        }
        toolsPresenter.showToolParameterGatherer(toolName, dict.toJSONString());
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
        destroyConsoleView();
        destroyHazardDetailView();
    }

    @Override
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        statusHandler
                .debug("HazardServicesAppBuilder.preShutdown(): Workbench = "
                        + workbench + " may shut down, forced = " + forced);
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
     * current selected events. If multiple geometry overlays need to be loaded
     * this currently only loads the first overlay.
     * 
     */
    public void loadGeometryOverlayForSelectedEvent() {
        spatialPresenter.getView().loadGeometryOverlayForSelectedEvent();
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
        sessionManager.shutdown();

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
                presenter.dispose();
            }
        }

        consolePresenter.getView().dispose();
        consolePresenter.dispose();
        presenters.clear();

        /*
         * Attempt to remove the workbench listener.
         */
        try {
            PlatformUI.getWorkbench().removeWorkbenchListener(this);
        } catch (Exception e) {
            statusHandler.error(
                    "Error removing hazard services workbench listener", e);
        }

        // Prepare the Python side effects applier for shutdown.
        PythonSideEffectsApplier.prepareForShutDown();
    }

    public ISessionManager getSessionManager() {
        return sessionManager;
    }

    void setToolsPresenter(ToolsPresenter toolsPresenter) {
        this.toolsPresenter = toolsPresenter;
    }

    public ConsolePresenter getConsolePresenter() {
        return consolePresenter;
    }

    public ProductStagingPresenter getProductStagingPresenter() {
        return productStagingPresenter;
    }

    public ToolsPresenter getToolsPresenter() {
        return toolsPresenter;
    }

    public SpatialPresenter getSpatialPresenter() {
        return spatialPresenter;
    }

    public ToolLayer getToolLayer() {
        return toolLayer;
    }

    public HazardDetailPresenter getHazardDetailPresenter() {
        return hazardDetailPresenter;
    }

    public ProductEditorPresenter getProductEditorPresenter() {
        return productEditorPresenter;
    }

    public void setQuestionAnswerer(IQuestionAnswerer questionAnswerer) {
        this.questionAnswerer = questionAnswerer;
    }

    @Override
    public IQuestionAnswerer getQuestionAnswerer() {
        return questionAnswerer;
    }

    /**
     * Returns the warner.
     * 
     * @param
     * @return The warner.
     */
    @Override
    public IWarner getWarner() {
        return warner;
    }

    /**
     * Sets the warner.
     * 
     * @param warner
     *            The warner
     * @return
     */
    public void setWarner(IWarner warner) {
        this.warner = warner;
    }
}
