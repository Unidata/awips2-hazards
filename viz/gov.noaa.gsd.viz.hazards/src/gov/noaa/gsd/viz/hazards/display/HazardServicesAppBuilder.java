/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)`
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_METADATA_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_SERVICES_LOCALIZATION_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_TYPES_LOCALIZATION_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_BRIDGE_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_CONFIG_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_DATA_ACCESS_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_DATA_STORAGE_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_EVENTS_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_GENERAL_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_GEO_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_GFE_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_LOG_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_RECOMMENDERS_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_TIME_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_TRACK_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_UTILITIES_DIR;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.alerts.AlertVizPresenter;
import gov.noaa.gsd.viz.hazards.alerts.AlertsConfigPresenter;
import gov.noaa.gsd.viz.hazards.alerts.AlertsConfigView;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.ConsoleView;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardServicesCloseAction;
import gov.noaa.gsd.viz.hazards.display.test.AutomatedTests;
import gov.noaa.gsd.viz.hazards.display.test.product_generators.ProductGenerationTests;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.VizConstants;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.globals.IGlobalChangedListener;
import com.raytheon.uf.viz.core.globals.VizGlobalsManager;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.SessionManagerFactory;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;
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
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * Jan 27, 2014 2155       Chris.Golden        Fixed bug that caused occasional exceptions
 *                                             when loading a bundle with Hazard Services in
 *                                             it when H.S. was already running; the time
 *                                             change that would occur would cause the old
 *                                             H.S. to try to react when it was already
 *                                             closing, leading to null pointer exceptions.
 * Feb 7, 2014  2890       bkowal              Product Generation JSON refactor.
 * Apr 09, 2014 2925       Chris.Golden        Changed to support class-based metadata.
 * Jun 18, 2014 3519       jsanchez            Allowed allowed the message dialog buttons to be configurable.
 * Jun 24, 2014 4009       Chris.Golden        Changed to pass new Python include path to
 *                                             Python side effects applier when initializing
 *                                             the latter, in order to allow side effects
 *                                             scripts to have access to AWIPS2/H.S. Python
 *                                             modules.
 * Jul 03, 2014 4084       Chris.Golden        Added shut down of event bus when shutting
 *                                             down Hazard Services.
 * Aug 18, 2014 4243       Chris.Golden        Changed Python side effects applier include
 *                                             path to work with recommender scripts.
 * Sep 09, 2014 4042       Chris.Golden        Moved product staging info generation to
 *                                             the product staging presenter.
 * Oct 03, 2014 4918       Robert.Blum         Added the metadata path to the Python include path.
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
     * Hard-coded canned case time. For use in Operation Mode for now.
     */
    public final static String CANNED_TIME = "1297137600000"; // 4Z

    // Private Static Constants

    /**
     * Scheduler to be used to make {@link Runnable} instances get executed on
     * the main thread. For now, the main thread is the UI thread; when this is
     * changed, this will be rendered obsolete, as at that point there will need
     * to be a blocking queue of <code>Runnable</code>s available to allow the
     * new worker thread to be fed jobs. At that point, this should be replaced
     * with an object that enqueues the <code>Runnable</code>s.
     */
    @Deprecated
    private static final IRunnableAsynchronousScheduler RUNNABLE_ASYNC_SCHEDULER = new IRunnableAsynchronousScheduler() {

        @Override
        public void schedule(Runnable runnable) {

            /*
             * Since the UI thread is currently the thread being used for nearly
             * everything, just run any asynchronous tasks there.
             */
            VizApp.runAsync(runnable);
        }
    };

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
     * Run product generation tests command string.
     */
    private static final String PRODUCT_GENERATION_TEST_COMMAND_MENU_TEXT = "Run Product Generation Tests";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesAppBuilder.class);

    // Private Variables

    /**
     * Event bus.
     */
    private final BoundedReceptionEventBus<Object> eventBus = new BoundedReceptionEventBus<>(
            BusConfiguration.Default(0), RUNNABLE_ASYNC_SCHEDULER);

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

    private ISessionManager<ObservedHazardEvent> sessionManager;

    private AlertVizPresenter alertVizPresenter;

    private IQuestionAnswerer questionAnswerer;

    /**
     * The warner to use to convey warnings to the user.
     */
    private IWarner warner;

    private IContinueCanceller continueCanceller;

    private IMainUiContributor<Action, RCPMainUserInterfaceElement> appBuilderMenubarContributor = null;

    private ProductGenerationTests productGenerationTests;

    private AutomatedTests automatedTests;

    public boolean getUserAnswerToQuestion(String question) {
        return questionAnswerer.getUserAnswerToQuestion(question);
    }

    /**
     * Warn the user. This delegates to the warner either created by the app
     * builder or injected by the client.
     * 
     * @param title
     *            The title of the warning
     * @param warning
     *            The warning message to convey to the user
     * @return
     */
    public void warnUser(String title, String warning) {
        warner.warnUser(title, warning);
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

        /*
         * Add an error handler so that any uncaught exceptions within message
         * handlers are output using the status handler as an error.
         */
        eventBus.addErrorHandler(new IPublicationErrorHandler() {

            @Override
            public void handleError(PublicationError error) {

                /*
                 * Get the cause of the error, and if there's a nested cause,
                 * use that instead. This is because the event bus wraps the
                 * cause in an InvocationTargetException, which doesn't help in
                 * identifying the problem; the exception developers would be
                 * interested in is what caused the problem in the first place.
                 */
                Throwable cause = error.getCause();
                if ((cause != null) && (cause.getCause() != null)) {
                    cause = cause.getCause();
                }
                statusHandler.error(
                        error.getListener().getClass() + "."
                                + error.getHandler().getName() + "(): "
                                + error.getMessage(), cause);
            }
        });

        ((ToolLayerResourceData) toolLayer.getResourceData())
                .setAppBuilder(this);

        PlatformUI.getWorkbench().addWorkbenchListener(this);

        // Initialize the Python side effects applier.
        IPathManager pathManager = PathManagerFactory.getPathManager();
        LocalizationContext localizationContext = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        String pythonPath = pathManager.getFile(localizationContext,
                PYTHON_LOCALIZATION_DIR).getPath();
        String localizationUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_UTILITIES_DIR);
        String vtecUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR);
        String logUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_LOG_UTILITIES_DIR);
        String eventsPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_EVENTS_DIR);
        String eventsUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_EVENTS_DIR, PYTHON_UTILITIES_DIR);
        String recommendersPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_EVENTS_DIR,
                PYTHON_LOCALIZATION_RECOMMENDERS_DIR);
        String recommendersConfigPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_EVENTS_DIR,
                PYTHON_LOCALIZATION_RECOMMENDERS_DIR,
                PYTHON_LOCALIZATION_CONFIG_DIR);
        String geoUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_GEO_UTILITIES_DIR);
        String shapeUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR);
        String textUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR);
        String dataStoragePath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_DATA_STORAGE_DIR);
        String bridgePath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_BRIDGE_DIR);
        String gfePath = FileUtil.join(pythonPath, PYTHON_LOCALIZATION_GFE_DIR);
        String timePath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_TIME_DIR);
        String generalUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_GENERAL_UTILITIES_DIR);
        String trackUtilitiesPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_TRACK_UTILITIES_DIR);
        String dataAccessPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_DATA_ACCESS_DIR);
        String hazardServicesPath = pathManager.getFile(localizationContext,
                HAZARD_SERVICES_LOCALIZATION_DIR).getPath();
        String hazardTypesPath = FileUtil.join(hazardServicesPath,
                HAZARD_TYPES_LOCALIZATION_DIR);
        String hazardMetaDataPath = FileUtil.join(hazardServicesPath,
                HAZARD_METADATA_DIR);
        PythonSideEffectsApplier.initialize(PyUtil.buildJepIncludePath(
                pythonPath, localizationUtilitiesPath, logUtilitiesPath,
                vtecUtilitiesPath, geoUtilitiesPath, shapeUtilitiesPath,
                textUtilitiesPath, dataStoragePath, eventsPath,
                eventsUtilitiesPath, bridgePath, hazardServicesPath,
                hazardTypesPath, hazardMetaDataPath, gfePath, timePath,
                generalUtilitiesPath, trackUtilitiesPath, dataAccessPath,
                recommendersPath, recommendersConfigPath), getClass()
                .getClassLoader());

        /*
         * For testing and demos, force DRT for operational mode start HS
         * according to CAVE clock for practice mode. Needed to do this, because
         * the user can interact with the CAVE status line clock only in
         * practice mode.
         */
        currentTime = SimulatedTime.getSystemTime().getTime();
        this.sessionManager = SessionManagerFactory.getSessionManager(this,
                eventBus);
        messageHandler = new HazardServicesMessageHandler(this, currentTime);

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

            @Override
            public boolean getUserAnswerToQuestion(String question,
                    String[] buttonLabels) {
                final int ISSUE_CODE = 0;
                MessageDialog dialog = new MessageDialog(null,
                        "Hazard Services", null, question,
                        MessageDialog.QUESTION, buttonLabels, ISSUE_CODE)

                {
                    @Override
                    protected void buttonPressed(int buttonId) {
                        setReturnCode(buttonId);
                        close();
                    }
                };

                int buttonId = dialog.open();
                return buttonId == ISSUE_CODE;
            }

        };

        this.warner = new IWarner() {

            @Override
            public void warnUser(String title, String warning) {

                MessageDialog.openWarning(null, title, warning);
            }

        };

        this.continueCanceller = new IContinueCanceller() {

            @Override
            public boolean getUserAnswerToQuestion(String title, String question) {
                String[] buttons = new String[] {
                        HazardConstants.CANCEL_BUTTON,
                        HazardConstants.CONTINUE_BUTTON };

                Shell shell = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell();

                MessageDialog dialog = new MessageDialog(shell, title, null,
                        question, MessageDialog.ERROR, buttons, 0);

                int response = dialog.open();
                return response == 1 ? true : false;
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
                                automatedTests = new AutomatedTests();
                                automatedTests
                                        .init(HazardServicesAppBuilder.this);
                                eventBus.publish(new ConsoleAction(
                                        ConsoleAction.ActionType.RUN_AUTOMATED_TESTS));
                            }
                        };

                        Action productGenerationTestAction = new BasicAction(
                                PRODUCT_GENERATION_TEST_COMMAND_MENU_TEXT,
                                null, Action.AS_PUSH_BUTTON, null) {
                            @Override
                            public void run() {
                                productGenerationTests = new ProductGenerationTests();
                                productGenerationTests
                                        .init(HazardServicesAppBuilder.this);
                                eventBus.publish(new ConsoleAction(
                                        ConsoleAction.ActionType.RUN_PRODUCT_GENERATION_TESTS));
                            }
                        };
                        return Lists.newArrayList(autoTestAction,
                                productGenerationTestAction);
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
            consolePresenter = new ConsolePresenter(sessionManager, eventBus);
            presenters.add(consolePresenter);
        }
        consolePresenter.setView(consoleView);
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
                    eventBus);
            presenters.add(alertsConfigPresenter);
            alertsConfigPresenter.setView(alertsConfigView);
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
            alertVizPresenter = new AlertVizPresenter(sessionManager, eventBus);
            alertVizPresenter.setView(alertVizView);
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
                    eventBus);
            presenters.add(hazardDetailPresenter);
        }
        hazardDetailPresenter.setView(hazardDetailView);
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
            settingsPresenter = new SettingsPresenter(sessionManager, eventBus);
            presenters.add(settingsPresenter);
            settingsPresenter.setView(settingsView);
        }
    }

    /**
     * Create the tools view and presenter.
     */
    private void createToolsDisplay() {
        if (toolsPresenter == null) {
            ToolsView toolsView = new ToolsView();
            toolsPresenter = new ToolsPresenter(sessionManager, eventBus);
            presenters.add(toolsPresenter);
            toolsPresenter.setView(toolsView);

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
            spatialPresenter = new SpatialPresenter(sessionManager, eventBus);
            presenters.add(spatialPresenter);
        } else {
            spatialPresenter.getView().dispose();
        }
        spatialPresenter.setView(spatialView);
    }

    /**
     * Create the product staging view and presenter.
     */
    private void createProductStagingDisplay() {
        ProductStagingView productStagingView = new ProductStagingView();
        if (productStagingPresenter == null) {
            productStagingPresenter = new ProductStagingPresenter(
                    sessionManager, eventBus);
            presenters.add(productStagingPresenter);
        } else {
            productStagingPresenter.getView().dispose();
        }
        productStagingPresenter.setView(productStagingView);
    }

    /**
     * Create the product editor view and presenter.
     */
    private void createProductEditorDisplay() {
        ProductEditorView productEditorView = new ProductEditorView();
        if (productEditorPresenter == null) {
            productEditorPresenter = new ProductEditorPresenter(sessionManager,
                    eventBus);
            presenters.add(productEditorPresenter);
        } else {
            productEditorPresenter.getView().dispose();
        }
        productEditorPresenter.setView(productEditorView);
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
        hazardDetailPresenter.showHazardDetail();
    }

    /**
     * Get the event bus.
     * 
     * @return Event bus.
     */
    public BoundedReceptionEventBus<Object> getEventBus() {
        return eventBus;
    }

    /**
     * 
     * @return the current settings
     */
    public Settings getCurrentSettings() {
        return (sessionManager.getConfigurationManager().getSettings());

    }

    /**
     * Set the current setting.
     * 
     * @param settings
     */
    public void setCurrentSettings(Settings settings) {
        messageHandler.changeCurrentSettings(settings);
    }

    /**
     * Notify all presenters of one or more model changes that occurred.
     * 
     * @param changed
     *            Set of model elements that have changed.
     */
    public void notifyModelChanged(EnumSet<HazardConstants.Element> changed,
            IOriginator originator) {
        if (disposing) {
            return;
        }
        for (HazardServicesPresenter<?> presenter : presenters) {
            if (shouldCall(originator, presenter)) {
                presenter.modelChanged(changed);
            }
        }
    }

    public void notifyModelChanged(EnumSet<HazardConstants.Element> changed) {
        notifyModelChanged(changed, Originator.OTHER);
    }

    /**
     * This is a temporary kludge that is needed until the event propagation is
     * switched over to going directly from the model to presenters, instead of
     * having this class act as an intermediary. At that time, presenters will
     * be free to either respond to or ignore notifications that are a result of
     * their own actions. For the moment, assume that any actions taken by the
     * console are ignored by the console, and the same for the HID.
     */
    @Deprecated
    private boolean shouldCall(IOriginator originator,
            HazardServicesPresenter<?> presenter) {
        if (((originator == UIOriginator.HAZARD_INFORMATION_DIALOG) && presenter
                .getClass().equals(HazardDetailPresenter.class))
                || ((originator == UIOriginator.CONSOLE) && presenter
                        .getClass().equals(ConsolePresenter.class))) {
            return false;
        }
        return true;
    }

    /**
     * Show the product staging dialog.
     * 
     * @param issueFlag
     *            Whether or not this is being called as a result of an issue
     *            action.
     * @param allProductGeneratorInformationForSelectedHazards
     */
    public void showProductStagingView(
            boolean issueFlag,
            Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards) {
        productStagingPresenter.showProductStagingDetail(issueFlag,
                allProductGeneratorInformationForSelectedHazards);
    }

    public void showProductEditorView(
            List<GeneratedProductList> generatedProductsList) {
        this.productEditorPresenter
                .showProductEditorDetail(generatedProductsList);
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
        hazardDetailPresenter.showHazardDetail();
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
        eventBus.publishAsync(new HazardServicesCloseAction());
        sessionManager.shutdown();
        eventBus.shutdown();

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

    public ISessionManager<ObservedHazardEvent> getSessionManager() {
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
     * Returns the continue/canceller.
     */
    @Override
    public IContinueCanceller getContinueCanceller() {
        return continueCanceller;
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
