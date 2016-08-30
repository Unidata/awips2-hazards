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
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_FORMATS_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_GENERAL_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_GEO_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_GFE_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_LOG_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_PRODUCTGEN_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_PRODUCTS_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_RECOMMENDERS_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_TIME_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_TRACK_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.ENCRYPTION_KEY;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.PASSWORD;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.REGISTRY_LOCATION;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.TRUST_STORE_LOCATION;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.TRUST_STORE_PASSWORD;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.USER_NAME;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.alerts.AlertVizPresenter;
import gov.noaa.gsd.viz.hazards.alerts.AlertsConfigPresenter;
import gov.noaa.gsd.viz.hazards.alerts.AlertsConfigView;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.ConsoleView;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.test.AutomatedTests;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailView;
import gov.noaa.gsd.viz.hazards.hazardtypefirst.HazardTypeFirstPresenter;
import gov.noaa.gsd.viz.hazards.hazardtypefirst.HazardTypeFirstView;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.producteditor.ProductEditorPresenter;
import gov.noaa.gsd.viz.hazards.producteditor.ProductEditorView;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingView;
import gov.noaa.gsd.viz.hazards.risecrestfall.GraphicalEditor;
import gov.noaa.gsd.viz.hazards.setting.SettingsPresenter;
import gov.noaa.gsd.viz.hazards.setting.SettingsView;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialDisplayHandler;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialView;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplayResourceData;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.tools.ToolsPresenter;
import gov.noaa.gsd.viz.hazards.tools.ToolsView;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;
import gov.noaa.gsd.viz.mvp.IView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
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
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.viz.core.AbstractTimeMatcher;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.VizConstants;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.IDescriptor.IFrameChangedListener;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.globals.IGlobalChangedListener;
import com.raytheon.uf.viz.core.globals.VizGlobalsManager;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged.ChangeType;
import com.raytheon.uf.viz.core.rsc.IResourceGroup;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.uf.viz.core.rsc.ResourceList.AddListener;
import com.raytheon.uf.viz.core.rsc.ResourceList.RemoveListener;
import com.raytheon.uf.viz.d2d.core.time.D2DTimeMatcher;
import com.raytheon.uf.viz.hazards.sessionmanager.IDisplayResourceContextProvider;
import com.raytheon.uf.viz.hazards.sessionmanager.IFrameContextProvider;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISpatialContextProvider;
import com.raytheon.uf.viz.hazards.sessionmanager.SessionManagerFactory;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager.StagingRequired;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.uf.viz.productgen.dialog.ProductViewer;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

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
 * Oct 06, 2014 4042       Chris.Golden        Changed to support two-step product staging
 *                                             dialog (first step allows user to select
 *                                             additional events to be included in products,
 *                                             second step allows the inputting of additional
 *                                             product-specific information using megawidgets).
 * Dec 05, 2014 4124       Chris.Golden        Changed to work with newly parameterized config
 *                                             manager, and with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer        Spatial Display cleanup and other bug fixes
 * Jan 21, 2015 3795       rferrel             Use ProductGenConfirmationDlg for getUserAnswer dialog.
 * Jan 21, 2015 3626       Chris.Golden        Added use of new hazard-type-first presenter and
 *                                             view.
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * Jan 29, 2015 3795       Robert.Blum         Fixed NullPointer on Display.getCurrent().getActiveShell().
 * Jan 30, 2015 3626       Chris.Golden        Added ability to pass event type when running a
 *                                             recommender.
 * Feb 03, 2015 3865       Chris.Cody          Shutdown Hazard Services on perspective change when
 *                                             unsupported by current editor
 * Feb 03, 2015 2331       Chris.Golden        Removed obsolete HazardServicesTimer; time tick
 *                                             notifications are now generated by the session
 *                                             time manager.
 * Feb 25, 2015 6600       Dan Schaffer        Fixed bug in spatial display centering
 * Feb 26, 2015 6306       mduff               Pass site id to product editor.
 * Feb 28, 2015 3847       mduff               Added rise/crest/fall editor
 * Apr 10, 2015  6898      Chris.Cody          Refactored async messaging
 * May 14, 2015  7560      mpduff              Added Apply callback
 * Jul 30, 2015 9681       Robert.Blum         Added new method to display the product viewer.
 * Nov 10, 2015 12762      Chris.Golden        Added support for use of new recommender manager.
 * Mar 15, 2016 15676      Chris.Golden        Updated to use new method names.
 * Apr 01, 2016 16225      Chris.Golden        Added ability to cancel tasks that are scheduled to run
 *                                             at regular intervals.
 * Jun 23, 2016 19537      Chris.Golden        Added use of a spatial context provider.
 * Jul 25, 2016 19537      Chris.Golden        Changed to implement spatial display handler (to
 *                                             deal with display-closed events) and frame-change
 *                                             listener (previously handled by the spatial display
 *                                             components). Removed product generation test menu item.
 *                                             Moved loading of registry preferences here, so that if
 *                                             Hazard Services is loaded via bundle load, the prefs
 *                                             will be loaded. Removed inappropriate access of spatial
 *                                             display components.
 * Jul 27, 2016 19924      Chris.Golden        Added code to monitor loaded data layers for Time Match
 *                                             Basis time changes. An earlier version of this code
 *                                             (not monitoring Time Match Basis, but instead particular
 *                                             "classes" of resources) was in the session configuration
 *                                             manager and the spatial view, but those were not
 *                                             appropriate places for it.
 * Aug 15, 2016 18376      Chris.Golden        Added code to make garbage collection of the session
 *                                             manager and so forth more likely.
 * Aug 19, 2016 16259      Chris.Golden        Changed event bus to use only one each of dispatcher and
 *                                             handler threads, so as to avoid messages arriving out
 *                                             of order.
 * </pre>
 * 
 * @author The Hazard Services Team
 * @version 1.0
 */
public class HazardServicesAppBuilder implements IPerspectiveListener4,
        IGlobalChangedListener, IWorkbenchListener, IFrameChangedListener,
        ISpatialDisplayHandler, IMessenger {

    // Public Static Constants

    /**
     * Hard-coded canned case time. For use in Operation Mode for now.
     */
    // public final static String CANNED_TIME = "1297137600000"; // 4Z
    // public final static String CANNED_TIME = "1447266960000"; // 2015-11-11
    // // 1836z
    public final static String CANNED_TIME = "1430067600000"; // 2015-04-26
                                                              // 1700z

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
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesAppBuilder.class);

    // Private Variables

    /**
     * Event bus.
     */
    private final BoundedReceptionEventBus<Object> eventBus = new BoundedReceptionEventBus<>(
            RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Implementation of a temporal frame context provider.
     */
    private final IFrameContextProvider frameContextProvider = new IFrameContextProvider() {

        @Override
        public FramesInfo getFramesInfo() {
            AbstractEditor editor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            return (editor != null ? editor.getActiveDisplayPane()
                    .getDescriptor().getFramesInfo() : null);
        }
    };

    /**
     * Implementation of a spatial context provider.
     */
    private final ISpatialContextProvider spatialContextProvider = new ISpatialContextProvider() {

        @Override
        public Coordinate getLatLonCenterPoint() {
            AbstractEditor editor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            if (editor != null) {
                double[] coordInPixels = editor.getActiveDisplayPane()
                        .getRenderableDisplay().getExtent().getCenter();
                double[] coordInLatLon = editor.getActiveDisplayPane()
                        .getDescriptor().pixelToWorld(coordInPixels);
                return new Coordinate(coordInLatLon[0], coordInLatLon[1],
                        coordInLatLon[2]);
            }
            return null;
        }
    };

    /**
     * Implementation of a display resource context provider.
     */
    private final IDisplayResourceContextProvider displayResourceContextProvider = new IDisplayResourceContextProvider() {

        @Override
        public List<Long> getTimeMatchBasisDataLayerTimes() {
            return (timeMatchBasisTimes == null ? null : Collections
                    .unmodifiableList(timeMatchBasisTimes));
        }
    };

    /**
     * Data times for the Time Match Basis (TMB) product that is currently
     * loaded; if no such product is loaded or the D2D perspective is not being
     * used, the list will be empty.
     */
    private List<Long> timeMatchBasisTimes = null;

    /**
     * Set of viz resources that are having changes listened for via
     * {@link #resourceChangeListener}.
     */
    private final Set<AbstractVizResource<?, ?>> monitoredVizResources = new HashSet<>();

    /**
     * Resource list add listener, for detecting changes in the list of
     * resources currently displayed.
     */
    private final AddListener addListener = new AddListener() {

        @Override
        public void notifyAdd(final ResourcePair resourcePair)
                throws VizException {
            RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                @Override
                public void run() {
                    setUpDataUpdateDetectorsForResource(resourcePair
                            .getResource());
                }
            });
        }
    };

    /**
     * Resource list remove listener, for detecting changes in the list of
     * resources currently displayed.
     */
    private final RemoveListener removeListener = new RemoveListener() {
        @Override
        public void notifyRemove(final ResourcePair resourcePair)
                throws VizException {
            RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                @Override
                public void run() {
                    removeDataUpdateDetectorsForResource(resourcePair
                            .getResource());
                }
            });
        }
    };

    /**
     * Resource change listener, for detecting changes in resource data times.
     */
    private final IResourceDataChanged resourceChangeListener = new IResourceDataChanged() {

        @Override
        public void resourceChanged(ChangeType type, Object object) {

            /*
             * Only respond to data updates and removals.
             */
            if ((type != ChangeType.DATA_UPDATE)
                    && (type != ChangeType.DATA_REMOVE)) {
                return;
            }

            /*
             * Determine what the new Time Match Basis times are. If the D2D
             * time matcher is not available, or if there is no TMB product, or
             * if the TMB product has no times, record this; otherwise, make a
             * list of the times.
             */
            AbstractTimeMatcher timeMatcher = spatialDisplay.getDescriptor()
                    .getTimeMatcher();
            AbstractVizResource<?, ?> timeMatchBasisResource = (timeMatcher instanceof D2DTimeMatcher ? ((D2DTimeMatcher) timeMatcher)
                    .getTimeMatchBasis() : null);
            List<Long> newTimeMatchBasisTimes = null;
            if (timeMatchBasisResource != null) {
                DataTime[] dataTimes = timeMatchBasisResource.getDataTimes();
                newTimeMatchBasisTimes = (dataTimes == null ? null
                        : new ArrayList<Long>(dataTimes.length));
                if (dataTimes != null) {
                    for (DataTime dataTime : dataTimes) {
                        newTimeMatchBasisTimes.add(dataTime.getMatchRef());
                    }
                }
            }

            /*
             * If the new times are not the same as the old ones, record the new
             * times, and trigger any tools that should run in response to a
             * Time Match Basis change.
             */
            if (Utils.equal(timeMatchBasisTimes, newTimeMatchBasisTimes) == false) {
                timeMatchBasisTimes = newTimeMatchBasisTimes;
                RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                    @Override
                    public void run() {
                        sessionManager.getConfigurationManager()
                                .triggerDataLayerChangeDrivenTool();
                    }
                });
            }
        }
    };

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
     * Hazard type first presenter.
     */
    private HazardTypeFirstPresenter hazardTypeFirstPresenter = null;

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
    private SpatialDisplay spatialDisplay;

    private ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    private AlertVizPresenter alertVizPresenter;

    private IQuestionAnswerer questionAnswerer;

    /**
     * The warner to use to convey warnings to the user.
     */
    private IWarner warner;

    private IContinueCanceller continueCanceller;

    private IMainUiContributor<Action, RCPMainUserInterfaceElement> appBuilderMenubarContributor = null;

    private AutomatedTests automatedTests;

    private IRiseCrestFallEditor graphicalEditor;

    private GraphicalEditor editor;

    private IToolParameterGatherer toolParameterGatherer;

    /**
     * Epoch time in milliseconds of the current frame.
     */
    private long currentFrameTime = Long.MIN_VALUE;

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
     * @param spatialDisplay
     *            Viz resource associated with this app builder, if a new one is
     *            to be created.
     * @throws VizException
     *             If an exception occurs while attempting to build the Hazard
     *             Services application.
     */
    public HazardServicesAppBuilder(SpatialDisplay spatialDisplay) {
        initialize(spatialDisplay);

    }

    // Methods

    /**
     * Initializes an instance of the HazardServicesAppBuilder
     * 
     * @param spatialDisplay
     *            Tool layer to be used with this builder.
     * @throws VizException
     *             If an error occurs while attempting to initialize.
     */
    private void initialize(SpatialDisplay spatialDisplay) {
        this.spatialDisplay = spatialDisplay;

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

        ((SpatialDisplayResourceData) spatialDisplay.getResourceData())
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
        String generatorsPath = FileUtil.join(pythonPath,
                PYTHON_LOCALIZATION_EVENTS_DIR,
                PYTHON_LOCALIZATION_PRODUCTGEN_DIR);
        String generatorsProductsPath = FileUtil.join(generatorsPath,
                PYTHON_LOCALIZATION_PRODUCTS_DIR);
        String generatorsFormatsPath = FileUtil.join(generatorsPath,
                PYTHON_LOCALIZATION_FORMATS_DIR);
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
                recommendersPath, recommendersConfigPath, generatorsPath,
                generatorsProductsPath, generatorsFormatsPath), getClass()
                .getClassLoader());

        loadRegistryPreferences();

        /*
         * For testing and demos, force DRT for operational mode start HS
         * according to CAVE clock for practice mode. Needed to do this, because
         * the user can interact with the CAVE status line clock only in
         * practice mode.
         */
        currentTime = SimulatedTime.getSystemTime().getTime();
        this.sessionManager = SessionManagerFactory.getSessionManager(this,
                spatialContextProvider, displayResourceContextProvider,
                frameContextProvider, eventBus);
        messageHandler = new HazardServicesMessageHandler(this,
                ((SpatialDisplayResourceData) spatialDisplay.getResourceData())
                        .getSettings(), currentTime);

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
                String okTitle = IDialogConstants.OK_LABEL;
                String cancelTitle = IDialogConstants.CANCEL_LABEL;
                if (buttonLabels != null) {
                    if (buttonLabels.length >= 1) {
                        okTitle = buttonLabels[0];
                    }
                    if (buttonLabels.length >= 2) {
                        cancelTitle = buttonLabels[1];
                    }
                }
                Shell shell = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell();
                ProductGenConfirmationDlg dialog = new ProductGenConfirmationDlg(
                        shell, "Hazard Services", question, okTitle,
                        cancelTitle);
                // Assume blocking dialog.
                Object result = dialog.open();
                return Boolean.TRUE.equals(result);
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

        this.graphicalEditor = new IRiseCrestFallEditor() {

            @Override
            public IHazardEvent getRiseCrestFallEditor(IHazardEvent event,
                    IEventApplier applier) {
                Shell shell = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell();
                if (editor == null || editor.isDisposed()) {
                    editor = new GraphicalEditor(shell, event, applier);
                } else {
                    editor.bringToTop();
                }
                IHazardEvent evt = (IHazardEvent) editor.open();
                return evt;
            }
        };

        this.toolParameterGatherer = new IToolParameterGatherer() {

            @Override
            public void getToolParameters(String tool, ToolType type,
                    RecommenderExecutionContext context,
                    Map<String, Serializable> dialogInput) {
                Dict dict = new Dict();
                for (String parameter : dialogInput.keySet()) {
                    dict.put(parameter, dialogInput.get(parameter));
                }
                toolsPresenter.showToolParameterGatherer(tool, type, context,
                        dict.toJSONString());
            }

            @Override
            public void getToolSpatialInput(String tool, ToolType type,
                    RecommenderExecutionContext context,
                    VisualFeaturesList visualFeatures) {
                spatialPresenter.setToolVisualFeatures(type, tool, context,
                        visualFeatures);
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
        createSpatialDisplay(spatialDisplay);

        /*
         * Make this object listen for frame changes with the spatial display's
         * descriptor.
         */
        spatialDisplay.getDescriptor().addFrameChangedListener(this);

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

        createHazardTypeFirstDisplay();

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

        // Add THIS HazardServicesAppBuilder as a perspective listener.
        // when loading a drawing layer
        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .addPerspectiveListener(this);

        // Set the time line duration.
        messageHandler.updateConsoleVisibleTimeDelta();

        // Add the HazardServicesAppBuilder as a listener for frame changes.
        VizGlobalsManager.addListener(VizConstants.FRAMES_ID, this);
        VizGlobalsManager.addListener(VizConstants.LOOPING_ID, this);

        redoTimeMatching();

        addResourceListeners();

        // Send the current frame information to the session manager.
        handleFrameChange();
    }

    @Override
    public void frameChanged(IDescriptor descriptor, DataTime oldTime,
            DataTime newTime) {
        if (spatialDisplay == null) {
            return;
        }
        FramesInfo framesInfo = frameContextProvider.getFramesInfo();
        if ((framesInfo != null) && (newTime != null)) {
            long newRefTime = newTime.getRefTime().getTime();
            if (newRefTime != currentFrameTime) {
                handleFrameChange();
                currentFrameTime = newRefTime;
            }
        }
    }

    /**
     * Update the model with CAVE frame information.
     */
    public void handleFrameChange() {

        /*
         * If frame information is available, use it, but ensure that this is
         * done in the UI thread.
         */
        final FramesInfo framesInfo = frameContextProvider.getFramesInfo();
        if (framesInfo != null) {
            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {

                    /*
                     * If there are frames and the frame index is valid, set the
                     * selected time to go with the current frame. This must be
                     * done in the worker thread.
                     */
                    int frameCount = framesInfo.getFrameCount();
                    int frameIndex = framesInfo.getFrameIndex();
                    if ((frameCount > 0) && (frameIndex != -1)) {
                        final long selectedTime = framesInfo.getFrameTimes()[frameIndex]
                                .getValidTime().getTimeInMillis();
                        RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {
                            @Override
                            public void run() {
                                ISessionTimeManager timeManager = sessionManager
                                        .getTimeManager();
                                long delta = timeManager
                                        .getUpperSelectedTimeInMillis()
                                        - timeManager
                                                .getLowerSelectedTimeInMillis();
                                SelectedTime timeRange = new SelectedTime(
                                        selectedTime, selectedTime + delta);
                                timeManager.setSelectedTime(timeRange,
                                        Originator.OTHER);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void spatialDisplayDisposed() {
        dispose();
    }

    /**
     * Initialize the Hazard Services web services interfaces.
     */
    private void loadRegistryPreferences() {
        IPreferenceStore store = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        HazardServicesClient.init(store.getString(REGISTRY_LOCATION),
                store.getString(USER_NAME), store.getString(PASSWORD),
                store.getString(TRUST_STORE_LOCATION),
                store.getString(TRUST_STORE_PASSWORD),
                store.getString(ENCRYPTION_KEY));
    }

    /**
     * Build the menu bar.
     */
    private void buildMenuBar() {
        ConsoleView consoleView = (ConsoleView) consolePresenter.getView();
        List<IMainUiContributor<Action, RCPMainUserInterfaceElement>> contributors = Lists
                .newArrayList();
        contributors.add(consoleView);
        contributors.add((ToolsView) toolsPresenter.getView());
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
        contributors.add((HazardTypeFirstView) hazardTypeFirstPresenter
                .getView());
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
            presenters.add(alertVizPresenter);
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
    private void createSpatialDisplay(SpatialDisplay spatialDisplay) {
        ISpatialView<Action, RCPMainUserInterfaceElement> spatialView = new SpatialView(
                spatialDisplay);
        if (spatialPresenter == null) {
            spatialPresenter = new SpatialPresenter(sessionManager, this,
                    eventBus);
            presenters.add(spatialPresenter);
        } else {
            spatialPresenter.getView().dispose();
        }
        spatialPresenter.setView(spatialView);
    }

    /**
     * Create the hazard type first view and presenter.
     */
    private void createHazardTypeFirstDisplay() {
        HazardTypeFirstView hazardTypeFirstView = new HazardTypeFirstView();
        if (hazardTypeFirstPresenter == null) {
            hazardTypeFirstPresenter = new HazardTypeFirstPresenter(
                    sessionManager, eventBus);
            presenters.add(hazardTypeFirstPresenter);
        } else {
            hazardTypeFirstPresenter.getView().dispose();
        }
        hazardTypeFirstPresenter.setView(hazardTypeFirstView);
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
     * Force time matching to be recalculated.
     */
    private void redoTimeMatching() {

        /*
         * Sometimes when Hazard Services is not fully constructed (when being
         * loaded from a bundle), the editor or descriptor is not available.
         * When this occurs, simply schedule an asynchronous run of this method.
         * Otherwise, simply redo the time matching immediately.
         */
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        IDescriptor descriptor = (editor != null ? editor
                .getActiveDisplayPane().getDescriptor() : null);
        if (descriptor == null) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    redoTimeMatching();
                }
            });
        } else {

            /*
             * Need to account for the possibility that a time matcher does not
             * exist (for example, in GFE).
             */
            AbstractTimeMatcher timeMatcher = descriptor.getTimeMatcher();
            if (timeMatcher != null) {
                try {
                    timeMatcher.redoTimeMatching(descriptor);
                } catch (VizException e) {
                    statusHandler.error(
                            "HazardServicesAppBuilder.redoTimeMatching():", e);
                }
            }
        }
    }

    /**
     * Add resource-related listeners.
     */
    private void addResourceListeners() {

        /*
         * Set up listeners for notifications concerning the addition or removal
         * of resources, as well as a listener for changes to specific
         * resources.
         */
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        for (IDisplayPane displayPane : editor.getDisplayPanes()) {
            if (displayPane != null) {
                ResourceList resourceList = displayPane.getDescriptor()
                        .getResourceList();
                resourceList.addPostAddListener(addListener);
                resourceList.addPostRemoveListener(removeListener);
                for (ResourcePair resourcePair : resourceList) {
                    setUpDataUpdateDetectorsForResource(resourcePair
                            .getResource());
                }
            }
        }

        /*
         * Trigger the data layer update notification, in case there is a Time
         * Match Basis product already loaded.
         */
        resourceChangeListener.resourceChanged(ChangeType.DATA_UPDATE, null);
    }

    /**
     * Remove resource-related listeners.
     */
    private void removeResourceListeners() {

        /*
         * Remove any listeners for specific resource data updates.
         */
        for (AbstractVizResource<?, ?> resource : monitoredVizResources) {
            resource.getResourceData().removeChangeListener(
                    resourceChangeListener);
        }
        monitoredVizResources.clear();

        /*
         * Remove the listeners for resource list changes.
         */
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (editor != null) {
            for (IDisplayPane displayPane : editor.getDisplayPanes()) {
                ResourceList resourceList = displayPane.getDescriptor()
                        .getResourceList();
                resourceList.removePostAddListener(addListener);
                resourceList.removePostRemoveListener(removeListener);
            }
        }
    }

    /**
     * Set up data update detectors for the specified resource and any child
     * resources it has.
     * 
     * @param resource
     *            Resource for which to set up data detectors, for both it and
     *            any children.
     */
    private void setUpDataUpdateDetectorsForResource(
            AbstractVizResource<?, ?> resource) {

        /*
         * Register to listen for the resource's changes, and remember that this
         * resource is being monitored.
         */
        monitoredVizResources.add(resource);
        resource.getResourceData().addChangeListener(resourceChangeListener);

        /*
         * If this resource contains other resources, recursively set up
         * detectors from them.
         */
        if (resource instanceof IResourceGroup) {
            for (ResourcePair resourcePair : ((IResourceGroup) resource)
                    .getResourceList()) {
                setUpDataUpdateDetectorsForResource(resourcePair.getResource());
            }
        }
    }

    /**
     * Remove any data update detectors for the specified resource and any child
     * resources it has if said resources' data updates had detectors monitoring
     * them.
     * 
     * @param resource
     *            Resource for which to remove data detectors, for both it and
     *            its children.
     */
    private void removeDataUpdateDetectorsForResource(
            AbstractVizResource<?, ?> resource) {

        /*
         * If this resource was being monitored, remove the listener and remove
         * the resource from the monitored set.
         */
        resource.getResourceData().removeChangeListener(resourceChangeListener);
        monitoredVizResources.remove(resource);

        /*
         * If this resource contains other resources, recursively remove
         * detectors from them.
         */
        if (resource instanceof IResourceGroup) {
            for (ResourcePair resourcePair : ((IResourceGroup) resource)
                    .getResourceList()) {
                removeDataUpdateDetectorsForResource(resourcePair.getResource());
            }
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
    public ObservedSettings getCurrentSettings() {
        return (sessionManager.getConfigurationManager().getSettings());

    }

    /**
     * Respond to the selected time being modified.
     * <p>
     * TODO: Should be annotated with <code>@Handler(priority = 1)</code> when
     * this class becomes an event bus listener as it is merged with
     * {@link HazardServicesMessageHandler}.
     * </p>
     * 
     * @param change
     *            Change that occurred.
     */
    public void selectedTimeChanged(SelectedTimeChanged change) {
        long selectedTimeMillis = sessionManager.getTimeManager()
                .getSelectedTime().getLowerBound();

        /*
         * Get the available data times from the frames, if any.
         */
        DataTime[] availableDataTimes = null;
        FramesInfo framesInfo = frameContextProvider.getFramesInfo();
        int frameCount = framesInfo.getFrameCount();
        if (frameCount > 0) {
            availableDataTimes = framesInfo.getFrameTimes();
        }

        /*
         * If there are data times, find the closest valid time.
         */
        if (availableDataTimes != null) {

            /*
             * Iterate through the frames, looking for the smallest difference
             * between each frame's time and the selected time.
             */
            int frameIndex = 0;
            long diff;
            long smallestDiff = Long.MAX_VALUE;
            for (DataTime time : availableDataTimes) {

                /*
                 * If there is no difference, use this frame.
                 */
                diff = Math.abs(time.getValidTime().getTimeInMillis()
                        - selectedTimeMillis);
                if (diff == 0) {
                    break;
                }

                /*
                 * If the new difference is greater than the last one, then the
                 * iteration is moving away from the closest difference; in that
                 * case, use the previous frame.
                 */
                if (smallestDiff < diff) {
                    frameIndex--;
                    break;
                }

                /*
                 * Remmember this difference for the next iteration, and
                 * increment the frame index.
                 */
                smallestDiff = diff;
                frameIndex++;
            }

            /*
             * Ensure the resulting frame index is not out of bounds.
             */
            if (frameIndex >= frameCount) {
                frameIndex--;
            }

            /*
             * If there is only one frame time, use it; otherwise, use the one
             * that was chosen above.
             */
            FramesInfo newFramesInfo;
            if (availableDataTimes.length == 1) {
                DataTime newDataTime = new DataTime(
                        new Date(selectedTimeMillis));
                newFramesInfo = new FramesInfo(new DataTime[] { newDataTime },
                        0);
                currentFrameTime = selectedTimeMillis;
            } else {
                newFramesInfo = new FramesInfo(frameIndex);
                currentFrameTime = availableDataTimes[frameIndex].getRefTime()
                        .getTime();
            }
            spatialDisplay.getDescriptor().setFramesInfo(newFramesInfo);
        }
    }

    /**
     * Respond to the settings being modified.
     * <p>
     * TODO: Should be annotated with <code>@Handler(priority = 1)</code> when
     * this class becomes an event bus listener as it is merged with
     * {@link HazardServicesMessageHandler}.
     * </p>
     * 
     * @param change
     *            Change that occurred.
     */
    public void settingsModified(final SettingsModified change) {
        VizApp.runAsync(new Runnable() {

            @Override
            public void run() {
                if (spatialDisplay != null) {
                    ((SpatialDisplayResourceData) spatialDisplay
                            .getResourceData()).setSettings(change
                            .getSettings());
                }
            }
        });
    }

    /**
     * Set the current setting.
     * 
     * @param settings
     * @param originator
     */
    public void setCurrentSettings(ISettings settings, IOriginator originator) {
        messageHandler.changeCurrentSettings(settings, originator);
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
     *            Flag indicating whether or not this is being called as a
     *            result of an issue action; if false, it is a preview.
     * @param stagingRequired
     *            Type of product staging required.
     */
    public void showProductStagingView(boolean issueFlag,
            StagingRequired stagingRequired) {
        productStagingPresenter.showProductStagingDetail(issueFlag,
                stagingRequired);
    }

    public void showProductEditorView(
            List<GeneratedProductList> generatedProductsList) {
        this.productEditorPresenter.showProductEditorDetail(
                generatedProductsList, this.sessionManager
                        .getConfigurationManager().getSiteID());
    }

    /**
     * Opens the Product Viewer to allow a view only way of reviewing issued
     * products.
     */
    public void showProductViewer(
            List<GeneratedProductList> generatedProductsList) {
        final ProductViewer productViewer = new ProductViewer(
                VizWorkbenchManager.getInstance().getCurrentWindow().getShell(),
                generatedProductsList);
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {
                productViewer.open();
            }
        });
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

    @Override
    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        statusHandler.debug("HazardServicesAppBuilder.perspectiveActivated(): "
                + "perspective activated: page = " + page + ", perspective = "
                + perspective.getDescription());

        // Close Hazard Services if there is no active editor in the new pers-
        // pective.
        AbstractEditor activeEditor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (activeEditor == null) {
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
                handleFrameChange();
            }
        });

        /*
         * Ensure this object no longer listens for frame changes with the old
         * spatial display's descriptor.
         */
        spatialDisplay.getDescriptor().removeFrameChangedListener(this);

        // Get the tool layer data from the old tool layer, and delete the
        // latter.
        spatialDisplay.perspectiveChanging();
        SpatialDisplayResourceData spatialDisplayResourceData = (SpatialDisplayResourceData) spatialDisplay
                .getResourceData();
        spatialDisplay.dispose();

        // Create a new tool layer for the new perspective.
        try {
            IDescriptor descriptor = null;
            AbstractEditor abstractEditor = EditorUtil
                    .getActiveEditorAs(AbstractEditor.class);
            if (abstractEditor != null) {
                IDisplayPane displayPane = abstractEditor
                        .getActiveDisplayPane();
                if (displayPane != null) {
                    descriptor = displayPane.getDescriptor();
                }
            }
            spatialDisplay = spatialDisplayResourceData.construct(
                    new LoadProperties(), descriptor);
        } catch (VizException e1) {
            statusHandler.error("Error creating spatial display", e1);
        }

        // Create a new spatial view for the new tool layer.
        addSpatialDisplayResourceToPerspective();

        /*
         * Make this object listen for frame changes with the new spatial
         * display's descriptor.
         */
        spatialDisplay.getDescriptor().addFrameChangedListener(this);

        // Rebuild the console menubar.
        buildMenuBar();

        // Rebuild the console toolbar.
        buildToolBar();

        // Update the spatial display.
        spatialPresenter.updateAllDisplayables();
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
        handleFrameChange();
    }

    /**
     * Add the spatial display to the current perspective.
     */
    private void addSpatialDisplayResourceToPerspective() {
        createSpatialDisplay(spatialDisplay);
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

        messageHandler.dispose();
        messageHandler = null;

        /*
         * Ensure this object no longer listens for frame changes with the
         * spatial display's descriptor.
         */
        spatialDisplay.getDescriptor().removeFrameChangedListener(this);

        ((SpatialDisplayResourceData) spatialDisplay.getResourceData())
                .setAppBuilder(null);

        sessionManager.shutdown();
        sessionManager = null;
        eventBus.shutdown();

        /*
         * Remove resource-related listeners.
         */
        removeResourceListeners();

        VizGlobalsManager.removeListener(VizConstants.FRAMES_ID, this);
        VizGlobalsManager.removeListener(VizConstants.LOOPING_ID, this);

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

    public ISessionManager<ObservedHazardEvent, ObservedSettings> getSessionManager() {
        return sessionManager;
    }

    void setToolsPresenter(ToolsPresenter toolsPresenter) {
        this.toolsPresenter = toolsPresenter;
    }

    public ConsolePresenter getConsolePresenter() {
        return consolePresenter;
    }

    public HazardTypeFirstPresenter getHazardTypeFirstPresenter() {
        return hazardTypeFirstPresenter;
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

    public SpatialDisplay getSpatialDisplay() {
        return spatialDisplay;
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

    @Override
    public IToolParameterGatherer getToolParameterGatherer() {
        return toolParameterGatherer;
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

    @Override
    public IRiseCrestFallEditor getRiseCrestFallEditor(IHazardEvent event) {
        return this.graphicalEditor;
    }
}
