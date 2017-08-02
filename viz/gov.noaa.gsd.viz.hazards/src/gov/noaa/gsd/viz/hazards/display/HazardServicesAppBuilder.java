/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)`
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.configuration.ServerConfigLookupProxy;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.viz.core.AbstractTimeMatcher;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.VizConstants;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.IDescriptor.IFrameChangedListener;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.globals.IGlobalChangedListener;
import com.raytheon.uf.viz.core.globals.VizGlobalsManager;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.maps.MapManager;
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
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.uf.viz.productgen.dialog.ProductViewer;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

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
import gov.noaa.gsd.viz.hazards.console.IConsoleHandler;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailView;
import gov.noaa.gsd.viz.hazards.hazarddetail.IHazardDetailHandler;
import gov.noaa.gsd.viz.hazards.hazardtypefirst.HazardTypeFirstPresenter;
import gov.noaa.gsd.viz.hazards.hazardtypefirst.HazardTypeFirstView;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.product.ProductPresenter;
import gov.noaa.gsd.viz.hazards.product.ProductView;
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
import gov.noaa.gsd.viz.hazards.tools.ToolsPresenter;
import gov.noaa.gsd.viz.hazards.tools.ToolsView;
import gov.noaa.gsd.viz.hazards.ui.HazardServicesPerspectiveListener;
import gov.noaa.gsd.viz.hazards.ui.QuestionDialog;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;
import gov.noaa.gsd.viz.mvp.IView;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;

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
 * Nov 23, 2015 13017      Chris.Golden        Added ability to specify a many-line message below the
 *                                             main message for a question being asked of the user.
 * Feb 24, 2016 13929      Robert.Blum         Remove first part of staging dialog.
 * Feb 25, 2016 14740      kbisanz             Check frameMap's values against defaults
 * Mar 03, 2016  7452      Robert.Blum         Added configurable maps that are loaded on startup.
 * Mar 03, 2016  7452      Robert.Blum         Fixing error due to incomplete python path.
 * Mar 14, 2016 12145      mduff               Handle error thrown by event manager.
 * Mar 15, 2016 15676      Chris.Golden        Updated to use new method names.
 * Apr 01, 2016 16225      Chris.Golden        Added ability to cancel tasks that are scheduled to run
 *                                             at regular intervals.
 * May 02, 2016 16373      mduff               Added the product view/presenter.
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
 * Aug 24, 2016 21424      Kevin.Bisanz        Updated wording/formatting of hazard conflict dialog.
 * Oct 05, 2016 22300      Kevin.Bisanz        Add call to HazardServicesPerspectiveListener.
 *                                             initializeListener(..).
 * Oct 05, 2016 22870      Chris.Golden        Added support for event-driven tools triggered
 *                                             by frame changes. This involves tracking which frame
 *                                             changes are caused by H.S. asking for them, and which
 *                                             originate with D2D (either because the user manipulated
 *                                             D2D's frame index with the D2D toolbar buttons, etc., or
 *                                             looping is on, or D2D loaded more frames and auto-
 *                                             changed the index). Also fixed the choosing of a D2D
 *                                             frame in response to a selected time change so that the
 *                                             entire valid time range is considered when picking the
 *                                             frame closest to (or containing) the selected time, not
 *                                             just the end of said range. Also added refresh of the
 *                                             spatial display when the frame is changed (without this,
 *                                             the display does not always show the new frame).
 * Oct 10, 2016 22300      Kevin.Bisanz        Added shouldOpenHsInPerspective(..) to fix HS being open
 *                                             in Localization. Also moved code in perspectiveActivated
 *                                             into a Runnable to fix the "PartRenderingEngine limbo"
 *                                             issue.
 * Oct 11, 2016 21873      Chris.Golden        Fixed bug that caused null pointer exceptions in some
 *                                             cases when switching perspectives. Also changed the
 *                                             choosing of a D2D frame in response to a selected time
 *                                             change; feedback from users indicated that it should not
 *                                             ever be a frame with a reference time later than the
 *                                             selected time; it has to be at the selected time or,
 *                                             failing that, the most recent frame before that.
 * Oct 12, 2016 21424      Kevin.Bisanz        When issuing, only display conflicts involving
 *                                             selected events.
 * Nov 15, 2016 26331      Chris.Golden        Fixed bug that manifested when importing a previously
 *                                             exported CAVE perspective display; a null pointer
 *                                             exception would occur because the spatial display's
 *                                             descriptor parameter would not yet be non-null. The
 *                                             fix was to have it asynchronously attempt to register
 *                                             again later.
 * Dec 02, 2016 26624      bkowal              Initialize a {@link ServerConfigLookupProxy} instance.
 * Feb 01, 2017 15556      Chris.Golden        Minor changes to support console refactor, including
 *                                             implementation of the new console handler and
 *                                             hazard detail handler interfaces. Also moved methods
 *                                             from HazardServicesMessageHandler here as appropriate,
 *                                             and removed anything not being used.
 * Feb 13, 2017 28892      Chris.Golden        Removed unneeded code.
 * Apr 27, 2017 11853      Chris.Golden        Changed name of product editor closing method.
 * Jun 22, 2017 15561      Chris.Golden        Keep track of which descriptor was last used for a
 *                                             perspective due to the current design. Also added flag
 *                                             to force recreation of spatial displayables when a new
 *                                             perspective is activated.
 * Jun 26, 2017 19207      Chris.Golden        Changes to view products for specific events. Also
 *                                             added warnings/TODOs concerning use of the provided
 *                                             IMessenger interfaces' methods that return something
 *                                             from outside the UI thread. Also added code to ensure
 *                                             that IMessenger interface implementations of methods
 *                                             that do not return anything are executed using the UI
 *                                             thread.
 * Aug 15, 2017 22757      Chris.Golden        Added ability for recommenders to specify either a
 *                                             message to display, or a dialog to display, with their
 *                                             results (that is, within the returned event set).
 * </pre>
 * 
 * @author The Hazard Services Team
 * @version 1.0
 */
public class HazardServicesAppBuilder
        implements IPerspectiveListener4, IGlobalChangedListener,
        IWorkbenchListener, IFrameChangedListener, IConsoleHandler,
        IHazardDetailHandler, ISpatialDisplayHandler, IMessenger {

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
            return (timeMatchBasisTimes == null ? null
                    : Collections.unmodifiableList(timeMatchBasisTimes));
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
                    setUpDataUpdateDetectorsForResource(
                            resourcePair.getResource());
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
                    removeDataUpdateDetectorsForResource(
                            resourcePair.getResource());
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
            AbstractVizResource<?, ?> timeMatchBasisResource = (timeMatcher instanceof D2DTimeMatcher
                    ? ((D2DTimeMatcher) timeMatcher).getTimeMatchBasis()
                    : null);
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
            if (Utils.equal(timeMatchBasisTimes,
                    newTimeMatchBasisTimes) == false) {
                timeMatchBasisTimes = newTimeMatchBasisTimes;
                RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                    @Override
                    public void run() {
                        if (disposing == false) {
                            sessionManager.getConfigurationManager()
                                    .triggerDataLayerChangeDrivenTool();
                        }
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
     * Product presenter.
     */
    private ProductPresenter productPresenter = null;

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
     * Viz resource associated with this builder.
     */
    private SpatialDisplay spatialDisplay;

    private ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    private AlertVizPresenter alertVizPresenter;

    /**
     * Question answerer for the user.
     * <p>
     * TODO: This should not be used by the session manager at all, as when a
     * worker thread is used for session manager activity, that thread would
     * have to block while the UI thread got the answer from the user.
     * </p>
     */
    private IQuestionAnswerer questionAnswerer;

    /**
     * The warner to use to convey warnings to the user.
     */
    private IWarner warner;

    /**
     * Continue-cancel questioner for the user.
     * <p>
     * TODO: This should not be used by the session manager at all, as when a
     * worker thread is used for session manager activity, that thread would
     * have to block while the UI thread got the answer from the user.
     * </p>
     */
    private IContinueCanceller continueCanceller;

    private final IMainUiContributor<Action, RCPMainUserInterfaceElement> appBuilderMenubarContributor = null;

    /**
     * Rise-crest-fall editor for the user.
     * <p>
     * TODO: This should not be used by the session manager at all, as when a
     * worker thread is used for session manager activity, that thread would
     * have to block while the UI thread got the answer from the user.
     * </p>
     */
    private IRiseCrestFallEditor graphicalEditor;

    private IProductViewerChooser productViewerChooser;

    private final Map<IPerspectiveDescriptor, IDescriptor> perspectiveDescriptorMap = new HashMap<>();

    private GraphicalEditor editor;

    private ProductViewerSelectionDlg productSelectionDialog;

    private IToolParameterGatherer toolParameterGatherer;

    /**
     * Epoch time in milliseconds of the current frame.
     */
    private long currentFrameTime = Long.MIN_VALUE;

    /**
     * Queue of frame times that go with the frame index changes requested by
     * Hazard Services, but not yet acknowledged by CAVE.
     */
    private final Queue<Long> unacknowledgedFrameTimeChanges = new LinkedList<>();

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
     * Initialize the instance.
     * 
     * @param spatialDisplay
     *            Tool layer to be used with this builder.
     * @throws VizException
     *             If an error occurs while attempting to initialize.
     */
    private void initialize(SpatialDisplay spatialDisplay) {
        this.spatialDisplay = spatialDisplay;

        ServerConfigLookupProxy
                .initInstance(new VizServerConfigLookupWrapper());

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
                statusHandler.error(error.getListener().getClass() + "."
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
        String pythonPath = pathManager
                .getFile(localizationContext,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_DIR)
                .getPath();
        String hazardServicesPythonPath = pathManager
                .getFile(localizationContext,
                        HazardsConfigurationConstants.HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR)
                .getPath();
        String localizationUtilitiesPath = FileUtil.join(
                hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_UTILITIES_DIR);
        String vtecUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR);
        String logUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_LOG_UTILITIES_DIR);
        String eventsPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR);
        String eventsUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                HazardsConfigurationConstants.PYTHON_UTILITIES_DIR);
        String recommendersPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_RECOMMENDERS_DIR);
        String generatorsPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTGEN_DIR);
        String generatorsProductsPath = FileUtil.join(generatorsPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTS_DIR);
        String generatorsFormatsPath = FileUtil.join(generatorsPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_FORMATS_DIR);
        String generatorsGeoSpatialPath = FileUtil.join(generatorsPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_GEOSPATIAL_DIR);
        String recommendersConfigPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_RECOMMENDERS_DIR,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_CONFIG_DIR);
        String geoUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_GEO_UTILITIES_DIR);
        String shapeUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR);
        String textUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR);
        String dataStoragePath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_DATA_STORAGE_DIR);
        String bridgePath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_BRIDGE_DIR);
        String generalUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_GENERAL_UTILITIES_DIR);
        String trackUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_TRACK_UTILITIES_DIR);

        String hazardServicesPath = pathManager
                .getFile(localizationContext,
                        HazardsConfigurationConstants.HAZARD_SERVICES_DIR)
                .getPath();
        String hazardTypesPath = FileUtil.join(hazardServicesPath,
                HazardsConfigurationConstants.HAZARD_TYPES_LOCALIZATION_DIR);
        String hazardMetaDataPath = FileUtil.join(hazardServicesPath,
                HazardsConfigurationConstants.HAZARD_METADATA_DIR);

        String gfePath = FileUtil.join(pythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_GFE_DIR);
        String timePath = FileUtil.join(pythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_TIME_DIR);
        String dataAccessPath = FileUtil.join(pythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_DATA_ACCESS_DIR);

        String gfeBasePath = pathManager
                .getFile(localizationContext,
                        HazardsConfigurationConstants.GFE_LOCALIZATION_DIR)
                .getPath();
        String gfePythonPath = FileUtil.join(gfeBasePath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_DIR);

        PythonSideEffectsApplier.initialize(PyUtil.buildJepIncludePath(
                pythonPath, hazardServicesPythonPath, localizationUtilitiesPath,
                logUtilitiesPath, vtecUtilitiesPath, geoUtilitiesPath,
                shapeUtilitiesPath, textUtilitiesPath, dataStoragePath,
                eventsPath, eventsUtilitiesPath, bridgePath, hazardServicesPath,
                hazardTypesPath, hazardMetaDataPath, gfePath, timePath,
                generalUtilitiesPath, trackUtilitiesPath, dataAccessPath,
                recommendersPath, recommendersConfigPath, generatorsPath,
                generatorsProductsPath, generatorsFormatsPath,
                generatorsGeoSpatialPath, gfePythonPath),
                getClass().getClassLoader());

        /*
         * For testing and demos, force DRT for operational mode start HS
         * according to CAVE clock for practice mode. Needed to do this, because
         * the user can interact with the CAVE status line clock only in
         * practice mode.
         */
        this.sessionManager = SessionManagerFactory.getSessionManager(this,
                spatialContextProvider, displayResourceContextProvider,
                frameContextProvider, eventBus);
        messageHandler = new HazardServicesMessageHandler(this,
                ((SpatialDisplayResourceData) spatialDisplay.getResourceData())
                        .getSettings());

        /*
         * TODO: Ensure that any call to these methods, if from a non-UI thread,
         * logs an error and returns false.
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
                return getUserAnswerToQuestion(question, null, buttonLabels);
            }

            @Override
            public boolean getUserAnswerToQuestion(String baseQuestion,
                    String potentiallyLongMessage, String[] buttonLabels) {
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
                QuestionDialog dialog = new QuestionDialog(
                        getBestParentForModalDialog(), "Hazard Services",
                        baseQuestion, potentiallyLongMessage, okTitle,
                        cancelTitle);

                /*
                 * Assume blocking dialog.
                 */
                Object result = dialog.open();
                return Boolean.TRUE.equals(result);
            }
        };

        this.warner = new IWarner() {

            @Override
            public void warnUser(final String title, final String warning) {

                if (Display.getDefault().getThread() == Thread
                        .currentThread()) {
                    MessageDialog.openWarning(null, title, warning);
                } else {
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            warnUser(title, warning);
                        }
                    });
                }
            }

        };

        /*
         * TODO: Ensure that any call to these methods, if from a non-UI thread,
         * logs an error and returns false.
         */
        this.continueCanceller = new IContinueCanceller() {

            @Override
            public boolean getUserAnswerToQuestion(String title,
                    String question) {
                String[] buttons = new String[] { HazardConstants.CANCEL_BUTTON,
                        HazardConstants.CONTINUE_BUTTON };
                MessageDialog dialog = new MessageDialog(
                        getBestParentForModalDialog(), title, null, question,
                        MessageDialog.ERROR, buttons, 0);

                int response = dialog.open();
                return response == 1 ? true : false;
            }
        };

        /*
         * TODO: Ensure that any call to these methods, if from a non-UI thread,
         * logs an error and returns null. Or refactor to avoid having to return
         * a hazard event (why does it need to do that?) and change it to ensure
         * it is run on the UI thread, just like the productViewerChooser below.
         */
        this.graphicalEditor = new IRiseCrestFallEditor() {

            @Override
            public IHazardEvent getRiseCrestFallEditor(IHazardEvent event,
                    IEventApplier applier) {
                if (editor == null || editor.isDisposed()) {
                    editor = new GraphicalEditor(getBestParentForModalDialog(),
                            event, applier);
                } else {
                    editor.bringToTop();
                }
                IHazardEvent evt = (IHazardEvent) editor.open();
                return evt;
            }
        };

        this.productViewerChooser = new IProductViewerChooser() {

            @Override
            public void getProductViewerChooser(
                    final List<ProductData> productData) {
                if (Display.getDefault().getThread() == Thread
                        .currentThread()) {
                    Shell shell = VizWorkbenchManager.getInstance()
                            .getCurrentWindow().getShell();
                    if (productSelectionDialog == null
                            || productSelectionDialog.isDisposed()) {
                        productSelectionDialog = new ProductViewerSelectionDlg(
                                shell, productPresenter, productData);
                        productSelectionDialog.open();
                    } else {
                        productSelectionDialog.bringToTop();
                    }
                } else {
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            getProductViewerChooser(productData);
                        }
                    });
                }
            }
        };

        this.toolParameterGatherer = new IToolParameterGatherer() {

            @Override
            public void getToolParameters(final String tool,
                    final ToolType type,
                    final RecommenderExecutionContext context,
                    final Map<String, Serializable> dialogInput) {
                if (Display.getDefault().getThread() == Thread
                        .currentThread()) {
                    Dict dict = new Dict();
                    for (String parameter : dialogInput.keySet()) {
                        dict.put(parameter, dialogInput.get(parameter));
                    }
                    toolsPresenter.showToolParameterGatherer(tool, type,
                            context, dict.toJSONString());
                } else {
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            getToolParameters(tool, type, context, dialogInput);
                        }
                    });
                }
            }

            @Override
            public void getToolSpatialInput(String tool, ToolType type,
                    RecommenderExecutionContext context,
                    VisualFeaturesList visualFeatures) {
                spatialPresenter.setToolVisualFeatures(type, tool, context,
                        visualFeatures);
            }

            @Override
            public void showToolResults(final String tool, final ToolType type,
                    RecommenderExecutionContext context,
                    final Map<String, Serializable> dialogResults) {
                if (Display.getDefault().getThread() == Thread
                        .currentThread()) {
                    Dict dict = new Dict();
                    for (String parameter : dialogResults.keySet()) {
                        dict.put(parameter, dialogResults.get(parameter));
                    }
                    toolsPresenter.showToolResults(tool, type, context,
                            dict.toJSONString());
                } else {
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            showToolResults(tool, type, context, dialogResults);
                        }
                    });
                }
            }
        };

        loadDefaultMaps();
    }

    /**
     * Load the map bundles that are to be loaded by default upon startup.
     */
    private void loadDefaultMaps() {
        IDisplayPaneContainer currentEditor = EditorUtil
                .getActiveVizContainer();
        MapManager mapMgr = MapManager
                .getInstance((IMapDescriptor) currentEditor
                        .getActiveDisplayPane().getDescriptor());

        StartUpConfig config = sessionManager.getConfigurationManager()
                .getStartUpConfig();
        if (config != null) {
            for (String map : config.getDisplayMaps()) {
                mapMgr.loadMapByBundleName(map.trim());
            }
        }
    }

    /**
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not Hazard Services is being
     *            instantiated as a result of a bundle load.
     * @return
     */
    public void buildGUIs(boolean loadedFromBundle) {

        /*
         * Create the Spatial Display layer in the active CAVE editor. This is
         * what hazards will be drawn on.
         */
        createSpatialDisplay(spatialDisplay);

        // Determine whether or not views are to be hidden at first if this
        // app builder is being created as the result of a bundle load.
        IPreferenceStore preferenceStore = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        loadedFromBundle = (loadedFromBundle && ((preferenceStore
                .contains(START_VIEWS_HIDDEN_WHEN_LOADED_FROM_BUNDLE) == false)
                || preferenceStore.getBoolean(
                        START_VIEWS_HIDDEN_WHEN_LOADED_FROM_BUNDLE)));

        // Open the console.
        createConsole(loadedFromBundle, false);

        // Create the settings view.
        createSettingsDisplay();

        // Create the tools view.
        createToolsDisplay();

        // Create the product view
        createProductDisplay();

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

        // Add the HazardServicesAppBuilder as a listener for frame changes.
        VizGlobalsManager.addListener(VizConstants.FRAMES_ID, this);
        VizGlobalsManager.addListener(VizConstants.LOOPING_ID, this);

        redoTimeMatching();

        addFrameChangedListener();

        addResourceListeners();

        perspectiveDescriptorMap.put(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().getPerspective(),
                spatialDisplay.getDescriptor());
    }

    @Override
    public void frameChanged(IDescriptor descriptor, DataTime oldTime,
            DataTime newTime) {

        if (spatialDisplay == null) {
            return;
        }

        /*
         * If a new time has been provided, record it and process it. Also
         * trigger a frame change tool execution if the frame time change was
         * not originally asked for by Hazard Services.
         */
        FramesInfo framesInfo = frameContextProvider.getFramesInfo();
        if ((framesInfo != null) && (newTime != null)) {
            long newRefTime = newTime.getRefTime().getTime();
            boolean changeDidNotOriginateHere = true;
            if ((unacknowledgedFrameTimeChanges.isEmpty() == false)
                    && (unacknowledgedFrameTimeChanges.peek() == newRefTime)) {
                unacknowledgedFrameTimeChanges.remove();
                changeDidNotOriginateHere = false;
            }
            if (newRefTime != currentFrameTime) {
                handleFrameChange();
                currentFrameTime = newRefTime;
                if (changeDidNotOriginateHere) {
                    RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                        @Override
                        public void run() {
                            sessionManager.getConfigurationManager()
                                    .triggerFrameChangeDrivenTool();
                        }
                    });
                }
            }
        }
    }

    /**
     * Update the model with CAVE frame information.
     */
    private void handleFrameChange() {

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
                    if ((frameCount > 0)
                            && (frameIndex != HazardConstants.NO_FRAMES_INDEX)) {
                        final long selectedTime = framesInfo
                                .getFrameTimes()[frameIndex].getValidTime()
                                        .getTimeInMillis();
                        RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {
                            @Override
                            public void run() {
                                if (disposing == false) {
                                    ISessionTimeManager timeManager = sessionManager
                                            .getTimeManager();
                                    long delta = timeManager
                                            .getUpperSelectedTimeInMillis()
                                            - timeManager
                                                    .getLowerSelectedTimeInMillis();
                                    SelectedTime timeRange = new SelectedTime(
                                            selectedTime, selectedTime + delta);
                                    timeManager.setSelectedTime(timeRange,
                                            Originator.CAVE);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void showUserConflictingHazardsWarning(
            Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> areasForConflictingEventsForEvents) {
        launchConflictingHazardsDialog(areasForConflictingEventsForEvents,
                false);
    }

    @Override
    public void consoleDisposed() {
        dispose();
    }

    @Override
    public void spatialDisplayDisposed() {
        dispose();
    }

    /**
     * Get the shell to be used as a parent for a message or other modal dialog
     * that is to be displayed.
     * 
     * @return Shell to be used as the parent of the modal dialog.
     */
    private Shell getBestParentForModalDialog() {

        /*
         * Get the main shell, and get its child shells, if any.
         */
        Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        List<Shell> children = getChildShells(parent);

        /*
         * Iterate recursively down through the descendant shells of the main
         * shell, at each level choosing as the next shells for which to examine
         * descendants any application-modal shells, or if none of those are
         * found, any primary-modal shells, or if none of those are found, any
         * modeless shells. Once no more descendants are found, return the last
         * level's last shell as the one to be used as a parent.
         */
        while (children.isEmpty() == false) {

            /*
             * Categorize the child shells according to their modality, and
             * choose as the next level's parents the one(s) that are most
             * modal.
             */
            List<Shell> modeless = new ArrayList<>(children.size());
            List<Shell> primaryModal = new ArrayList<>(children.size());
            List<Shell> applicationModal = new ArrayList<>(children.size());
            for (Shell child : children) {
                if ((child.getStyle()
                        & (SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL)) != 0) {
                    applicationModal.add(child);
                } else if ((child.getStyle() & SWT.PRIMARY_MODAL) != 0) {
                    primaryModal.add(child);
                } else {
                    modeless.add(child);
                }
            }
            List<Shell> parents = (applicationModal.isEmpty() == false
                    ? applicationModal
                    : (primaryModal.isEmpty() == false ? primaryModal
                            : modeless));

            /*
             * Remember the last parent of the ones chosen above as the one to
             * be used if no children are found of any of these parents.
             */
            parent = parents.get(parents.size() - 1);

            /*
             * Get the children of this level's parents as chosen above.
             */
            children.clear();
            for (Shell aParent : parents) {
                children.addAll(getChildShells(aParent));
            }
        }
        return parent;
    }

    /**
     * Get the child shells of the specified shell.
     * 
     * @param parent
     *            Shell for which to get any child shows.
     * @return List of child shells; may be empty.
     */
    private List<Shell> getChildShells(Shell parent) {
        Shell[] shellsArray = parent.getShells();
        if ((shellsArray == null) || (shellsArray.length == 0)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(shellsArray);
    }

    /**
     * Ask the user a question and get an answer.
     * 
     * @param question
     *            Question to be asked.
     * @return Answer provided by the user.
     */
    private boolean getUserAnswerToQuestion(String question) {
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
    private void warnUser(String title, String warning) {
        warner.warnUser(title, warning);
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
        contributors.add((ProductView) productPresenter.getView());
        contributors.add((HazardDetailView) hazardDetailPresenter.getView());
        contributors
                .add((HazardTypeFirstView) hazardTypeFirstPresenter.getView());
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
     * @param perspectiveChanged
     *            Flag indicating whether or not this display is being
     *            instantiated as a result of a perspective change.
     */
    private void createConsole(boolean loadedFromBundle,
            boolean perspectiveChanged) {
        ConsoleView consoleView = new ConsoleView(loadedFromBundle);
        if (consolePresenter == null) {
            consolePresenter = new ConsolePresenter(sessionManager, this,
                    eventBus);
            presenters.add(consolePresenter);
        }
        consolePresenter.setView(consoleView);

        /*
         * If the perspective has changed, prompt the presenter to reload the
         * settings that were already loaded, as otherwise its view will not
         * have any columns displaying in its table, the title text of the view
         * will not be set, etc.
         */
        if (perspectiveChanged) {
            consolePresenter.settingsModified(
                    new SettingsLoaded(sessionManager.getConfigurationManager(),
                            Originator.OTHER));
        }
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
                    this, eventBus);
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
     * Create the product view and presenter.
     */
    private void createProductDisplay() {
        if (productPresenter == null) {
            ProductView prodView = new ProductView();
            productPresenter = new ProductPresenter(sessionManager, eventBus);
            presenters.add(productPresenter);
            productPresenter.setView(prodView);
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
     * Register a listener for for frame changes with the spatial display's
     * descriptor, if the latter has a descriptor at this point; otherwise,
     * schedule another attempt to register for frame changes to occur later.
     * The descriptor may not be available until later when loading as part of
     * an imported CAVE display, for example.
     */
    private void addFrameChangedListener() {
        MapDescriptor descriptor = spatialDisplay.getDescriptor();
        if (descriptor != null) {
            descriptor.addFrameChangedListener(this);
            handleFrameChange();
        } else {
            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    addFrameChangedListener();
                }
            });
        }
    }

    /**
     * Register resource-related listeners, if an editor is available at this
     * point; otherwise, schedule another attempt to register the listeners to
     * occur later. The editor may not be available until later when loading as
     * part of an imported CAVE display, for example.
     */
    private void addResourceListeners() {

        /*
         * Set up listeners for notifications concerning the addition or removal
         * of resources, as well as a listener for changes to specific
         * resources.
         */
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (editor != null) {
            for (IDisplayPane displayPane : editor.getDisplayPanes()) {
                if (displayPane != null) {
                    ResourceList resourceList = displayPane.getDescriptor()
                            .getResourceList();
                    resourceList.addPostAddListener(addListener);
                    resourceList.addPostRemoveListener(removeListener);
                    for (ResourcePair resourcePair : resourceList) {
                        setUpDataUpdateDetectorsForResource(
                                resourcePair.getResource());
                    }
                }
            }

            /*
             * Trigger the data layer update notification, in case there is a
             * Time Match Basis product already loaded.
             */
            resourceChangeListener.resourceChanged(ChangeType.DATA_UPDATE,
                    null);
        } else {
            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    addResourceListeners();
                }
            });
        }
    }

    /**
     * Remove resource-related listeners.
     */
    private void removeResourceListeners() {

        /*
         * Remove any listeners for specific resource data updates.
         */
        for (AbstractVizResource<?, ?> resource : monitoredVizResources) {
            resource.getResourceData()
                    .removeChangeListener(resourceChangeListener);
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
        IDescriptor descriptor = (editor != null
                ? editor.getActiveDisplayPane().getDescriptor() : null);
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
                removeDataUpdateDetectorsForResource(
                        resourcePair.getResource());
            }
        }
    }

    /**
     * Shut down this instance of hazard services, disposing of all allocated
     * resources.
     */
    private void dispose() {
        closeHazardServices(null);
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
         * If the originator was CAVE, then do not try to change the frame index
         * in CAVE, as that would just be telling CAVE something it already
         * knew.
         */
        if (change.getOriginator() == Originator.CAVE) {
            return;
        }

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
         * If there are data times, find the frame with the reference time that
         * is closest to the selected time, but not after the selected time.
         */
        if (availableDataTimes != null) {

            /*
             * Iterate through the frames, looking for the last frame that has a
             * reference time before or at the selected time.
             */
            int frameIndex = 0;
            for (DataTime time : availableDataTimes) {
                if (selectedTimeMillis < time.getRefTime().getTime()) {
                    if (frameIndex > 0) {
                        frameIndex--;
                    }
                    break;
                }
                frameIndex++;
            }

            /*
             * Ensure the resulting frame index is not out of bounds.
             */
            if (frameIndex == frameCount) {
                frameIndex--;
            }

            /*
             * If the chosen frame index is no different from what is already
             * the current one, and if either the frame count is greater than 1,
             * or the current frame's reference time is the same as the selected
             * time, there is no need to change CAVE's frames info, so do
             * nothing more.
             */
            if ((framesInfo.getFrameIndex() == frameIndex)
                    && ((frameCount > 1) || (availableDataTimes[0].getRefTime()
                            .getTime() == selectedTimeMillis))) {
                return;
            }

            /*
             * If there is only one frame time, use it; otherwise, use the one
             * that was chosen above. Remember the requested time so that
             * notifications from CAVE of this new frame time can be
             * differentiated from frame changes not caused by Hazard Services.
             */
            FramesInfo newFramesInfo;
            if (availableDataTimes.length == 1) {
                unacknowledgedFrameTimeChanges.add(selectedTimeMillis);
                DataTime newDataTime = new DataTime(
                        new Date(selectedTimeMillis));
                newFramesInfo = new FramesInfo(new DataTime[] { newDataTime },
                        0);
                currentFrameTime = selectedTimeMillis;
            } else {
                newFramesInfo = new FramesInfo(frameIndex);
                currentFrameTime = availableDataTimes[frameIndex].getRefTime()
                        .getTime();
                unacknowledgedFrameTimeChanges.add(currentFrameTime);
            }
            spatialDisplay.getDescriptor().setFramesInfo(newFramesInfo);

            /*
             * Refresh the spatial display; without this, the display's viz
             * resources' visual representations are not always updated.
             */
            spatialDisplay.issueRefresh();
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
                            .getResourceData())
                                    .setSettings(change.getSettings());
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
        if (((originator == UIOriginator.HAZARD_INFORMATION_DIALOG)
                && presenter.getClass().equals(HazardDetailPresenter.class))
                || ((originator == UIOriginator.CONSOLE) && presenter.getClass()
                        .equals(ConsolePresenter.class))) {
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
     */
    public void showProductStagingView(boolean issueFlag) {
        productStagingPresenter.showProductStagingDetail(issueFlag);
    }

    public void showProductEditorView(
            List<GeneratedProductList> generatedProductsList) {
        this.productEditorPresenter.showProductEditorDetail(
                generatedProductsList,
                this.sessionManager.getConfigurationManager().getSiteID());
    }

    /**
     * Opens the Product Viewer to allow a view only way of reviewing issued
     * products.
     */
    public void showProductViewer(
            List<GeneratedProductList> generatedProductsList) {
        final ProductViewer productViewer = new ProductViewer(
                VizWorkbenchManager.getInstance().getCurrentWindow().getShell(),
                generatedProductsList,
                this.sessionManager.getConfigurationManager().getHazardTypes());
        VizApp.runSync(new Runnable() {
            @Override
            public void run() {
                productViewer.open();
            }
        });
    }

    @Override
    public boolean shouldContinueIfThereAreHazardConflicts() {

        boolean userResponse = true;

        ISessionEventManager<ObservedHazardEvent> sessionEventManager = sessionManager
                .getEventManager();
        ISessionSelectionManager<ObservedHazardEvent> sessionSelectionManager = sessionManager
                .getSelectionManager();

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictMap = null;
        try {
            List<ObservedHazardEvent> selectedEvents = sessionSelectionManager
                    .getSelectedEvents();
            conflictMap = sessionEventManager.getAllConflictingEvents();

            /*
             * Keep only conflicts which are selected events. Modifying the
             * map's key set reflects the changes in the map itself.
             */
            conflictMap.keySet().retainAll(selectedEvents);
        } catch (HazardEventServiceException e) {
            statusHandler.error("Could not get map of all conflicting events; "
                    + "assuming should not continue.", e);
            return false;
        }

        if (!conflictMap.isEmpty()) {
            userResponse = launchConflictingHazardsDialog(conflictMap, true);
        }

        return userResponse;
    }

    @Override
    public void closeProductEditor() {
        productEditorPresenter.closeProductEditor();
    }

    @Override
    public void perspectiveActivated(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        statusHandler.debug("HazardServicesAppBuilder.perspectiveActivated(): "
                + "perspective activated: page = " + page + ", perspective = "
                + perspective.getDescription());

        /*
         * Close Hazard Services if there is no active editor in the new
         * perspective.
         */
        AbstractEditor activeEditor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        if (activeEditor == null
                || shouldOpenHsInPerspective(perspective) == false) {
            closeHazardServices("Hazard Services cannot run in the "
                    + perspective.getLabel()
                    + " perspective, and must therefore shut down.");
            return;
        }

        /*
         * The majority of this method is in a Runnable to work around the
         * "PartRenderingEngine limbo" issue discovered in Eclipse 4.
         *
         * The issue is that sometimes old views from the old perspective become
         * visible.
         *
         * When the HID is detached in the D2D perspective, switching to Hydro
         * and back will cause Eclipse to enter a limbo state about 50% of the
         * time. It seems to be the case that the order that listeners are fired
         * in is not well defined and sometimes this perspectiveActivated is
         * called before the internal Eclipse code has finished.
         * Showing/creating views in this state causes issues. It seems that
         * Eclipse is parenting views to the limbo shell and then making those
         * views visible.
         *
         * Running this in a Runnable delays the Hazard Services code and allows
         * the Eclipse code to finish (because Eclipse won't run anything from
         * the run queue until it has finished firing all of its listeners).
         *
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=473278
         */
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {

                /*
                 * Removed when the page is closed, inside the listener.
                 */
                HazardServicesPerspectiveListener.initializeListener(page);

                /*
                 * Recreate the console and the hazard detail views.
                 */
                createConsole(false, true);
                createHazardDetailDisplay(false);

                /*
                 * Ensure that the selected time is synchronized with the
                 * current frame.
                 */
                VizApp.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        handleFrameChange();
                    }
                });

                /*
                 * Ensure this object no longer listens for frame changes with
                 * the old spatial display's descriptor.
                 */
                spatialDisplay.getDescriptor().removeFrameChangedListener(
                        HazardServicesAppBuilder.this);

                /*
                 * Get the tool layer data from the old tool layer, and delete
                 * the latter.
                 * 
                 * Note: The current implementation creates and destroys the
                 * resource every time the user switches from one perspective to
                 * another. This occurs even if the original perspective remains
                 * open. As a result, resources are constantly created and
                 * destroyed as the user switches from perspective to
                 * perspective. This would not normally be a requirement for a
                 * simple abstract viz resource. However, in this implementation
                 * everything has been so tightly coupled together. And this is
                 * despite all of the component-to-component messaging (rather
                 * than direction interaction) that occurs.
                 */
                spatialDisplay.perspectiveChanging();
                SpatialDisplayResourceData spatialDisplayResourceData = (SpatialDisplayResourceData) spatialDisplay
                        .getResourceData();
                spatialDisplay.dispose();
                unacknowledgedFrameTimeChanges.clear();

                /*
                 * Create a new tool layer for the new perspective.
                 */
                try {
                    IDescriptor descriptor = perspectiveDescriptorMap
                            .get(perspective);
                    if (descriptor == null) {
                        AbstractEditor abstractEditor = EditorUtil
                                .getActiveEditorAs(AbstractEditor.class);
                        if (abstractEditor != null) {
                            IDisplayPane displayPane = abstractEditor
                                    .getActiveDisplayPane();
                            if (displayPane != null) {
                                descriptor = displayPane.getDescriptor();
                            }
                        }
                    }
                    spatialDisplay = spatialDisplayResourceData
                            .construct(new LoadProperties(), descriptor);
                } catch (VizException e1) {
                    statusHandler.error("Error creating spatial display", e1);
                }

                /*
                 * Create a new spatial view for the new tool layer.
                 */
                addSpatialDisplayResourceToPerspective();

                /*
                 * Make this object listen for frame changes with the new
                 * spatial display's descriptor.
                 */
                addFrameChangedListener();

                /*
                 * Rebuild the console menubar.
                 */
                buildMenuBar();

                /*
                 * Rebuild the console toolbar.
                 */
                buildToolBar();

                /*
                 * Update the spatial display.
                 */
                spatialPresenter.updateAllDisplayables(true);

                if (perspectiveDescriptorMap
                        .containsKey(perspective) == false) {
                    perspectiveDescriptorMap.put(perspective,
                            spatialDisplay.getDescriptor());
                }
            }
        });
    }

    /**
     * @param perspective
     * @return True if Hazard Services should be shown in the provided
     *         perspective, false otherwise.
     */
    private boolean shouldOpenHsInPerspective(
            IPerspectiveDescriptor perspective) {
        String perspectiveId = perspective.getId();
        boolean retval = true;
        /*
         * Don't open HS in the Localization perspective. This value is from
         * com.raytheon.uf.viz.localization.perspective.LocalizationPerspective.
         * ID. The constant isn't reference to avoid having Hazard Services
         * require Localization in the manifest.
         */
        if (perspectiveId
                .equals("com.raytheon.uf.viz.ui.LocalizationPerspective")) {
            /*
             * After https://cm1.oma.us.ray.com/redmine/issues/5929 is fixed,
             * the following code should work again: AbstractEditor activeEditor
             * = EditorUtil .getActiveEditorAs(AbstractEditor.class); if
             * (activeEditor == null) return; }
             */
            retval = false;
        }

        return retval;
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
            IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef,
            String changeId) {

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
        perspectiveDescriptorMap.remove(perspective);
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
     * Launches a dialog displaying conflicting hazards. It is up to the user as
     * to whether or not to fix them.
     * 
     * @param conflictingHazardMap
     *            A map of hazards and hazards which conflict with them.
     * @param requiresConfirmation
     *            Indicates whether or not this dialog should require user
     *            confirmation (Yes or No).
     * @return The return value from the dialog based on the user's selection.
     */
    private boolean launchConflictingHazardsDialog(
            final Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictingHazardMap,
            final Boolean requiresConfirmation) {

        boolean userSelection = true;

        if (!conflictingHazardMap.isEmpty()) {
            StringBuffer message = new StringBuffer(
                    "The following hazard conflicts exist: ");

            if (requiresConfirmation) {
                message.append("Continue?\n");
            } else {
                message.append("\n");
            }

            for (IHazardEvent hazardEvent : conflictingHazardMap.keySet()) {

                String phenSig = HazardEventUtilities
                        .getHazardType(hazardEvent);
                message.append(hazardEvent.getEventID());
                message.append("(");
                message.append(phenSig);
                message.append(") conflicts with: ");

                Map<IHazardEvent, Collection<String>> conflictingHazards = conflictingHazardMap
                        .get(hazardEvent);

                HazardTypeEntry hazardTypeEntry = sessionManager
                        .getConfigurationManager().getHazardTypes()
                        .get(phenSig);
                Set<String> ugcTypes = hazardTypeEntry.getUgcTypes(); // E.g.
                                                                      // county

                for (IHazardEvent conflictingHazard : conflictingHazards
                        .keySet()) {
                    String conflictingPhenSig = HazardEventUtilities
                            .getHazardType(conflictingHazard);
                    message.append("\n\t");
                    message.append(conflictingHazard.getEventID());
                    message.append("(");
                    message.append(conflictingPhenSig);
                    message.append(") ");

                    Collection<String> conflictingAreas = conflictingHazards
                            .get(conflictingHazard);

                    if (!conflictingAreas.isEmpty()) {
                        message.append("\n\t\tUGCs(");
                        message.append(Joiner.on(", ").join(ugcTypes));
                        message.append("):");

                        for (String area : conflictingAreas) {
                            message.append(" ");
                            message.append(area);
                        }
                    }

                }

                message.append("\n");
            }

            if (requiresConfirmation) {
                userSelection = getUserAnswerToQuestion(message.toString());

            } else {
                warnUser("Conflicting Hazards", message.toString());
            }
        }

        return userSelection;
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

    @Override
    public IQuestionAnswerer getQuestionAnswerer() {
        return questionAnswerer;
    }

    @Override
    public IWarner getWarner() {
        return warner;
    }

    @Override
    public IContinueCanceller getContinueCanceller() {
        return continueCanceller;
    }

    @Override
    public IToolParameterGatherer getToolParameterGatherer() {
        return toolParameterGatherer;
    }

    @Override
    public IRiseCrestFallEditor getRiseCrestFallEditor(IHazardEvent event) {
        return this.graphicalEditor;
    }

    @Override
    public IProductViewerChooser getProductViewerChooser() {
        return this.productViewerChooser;
    }
}
