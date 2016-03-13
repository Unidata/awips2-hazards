/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.Command;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.DisplayableEventIdentifier;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.ui.BasicWidgetDelegateHelper;
import gov.noaa.gsd.viz.hazards.ui.ChoiceStateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.CommandInvokerDelegate;
import gov.noaa.gsd.viz.hazards.ui.QualifiedStateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.StateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.ViewPartDelegateView;
import gov.noaa.gsd.viz.hazards.ui.ViewPartQualifiedWidgetDelegateHelper;
import gov.noaa.gsd.viz.hazards.ui.ViewPartWidgetDelegateHelper;
import gov.noaa.gsd.viz.megawidgets.displaysettings.IDisplaySettings;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IWidget;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPage;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Hazard detail view, providing an an SWT-based view of hazard details.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * May 10, 2013            Chris.Golden      Change to Eclipse view implementation.
 * Jun 25, 2013            Chris.Golden      Changed to allow hazard metadata
 *                                           changes to be pushed to the event bus
 *                                           if so desired by the view part.
 * Jul 12, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * Aug 22, 2013   1921     Bryon.Lawrence    Added accessor method for
 *                                           testing whether or not HID 
 *                                           updates should fire-off
 *                                           messages.
 * Aug 22, 2013   1936     Chris.Golden      Added console countdown timers.
 * Nov 14, 2013   1463     Bryon.Lawrence    Added code to support hazard conflict
 *                                           detection.
 * Feb 19, 2014   2161     Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view
 *                                           during initialization.
 * Apr 11, 2014   2819     Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * Apr 14, 2014   2925     Chris.Golden      Minor changes to support first round of
 *                                           class-based metadata changes, as well as
 *                                           to conform to new event propagation
 *                                           scheme.
 * May 15, 2014   2925     Chris.Golden      Together with changes made in last
 *                                           2925 changeset, essentially rewritten
 *                                           to provide far better separation of
 *                                           concerns between model, view, and
 *                                           presenter; almost exclusively switch
 *                                           to scheme whereby the model is changed
 *                                           directly instead of via messages, and
 *                                           model changes are detected via various
 *                                           event-bus-listener methods (the sole
 *                                           remaining holdouts are the issue,
 *                                           propose, and preview commands, which
 *                                           are still sent via message to the
 *                                           message handler); and preparation for
 *                                           multithreading in the future.
 * Jun 25, 2014   4009     Chris.Golden      Added code to cache extra data held by
 *                                           metadata megawidgets between view
 *                                           instantiations.
 * Jun 30, 2014   3512     Chris.Golden      Changed to work with changes MVP widget
 *                                           framework classes.
 * Jul 03, 2014   3512     Chris.Golden      Added code to allow a duration selector
 *                                           to be displayed instead of an absolute
 *                                           date/time selector for the end time of
 *                                           a hazard event.
 * Aug 15, 2014   4243     Chris.Golden      Added ability to invoke event-modifying
 *                                           scripts via metadata-specified notifier
 *                                           megawidgets.
 * Sep 16, 2014   4753     Chris.Golden      Changed event script running to include
 *                                           mutable properties.
 * Oct 15, 2014   3498     Chris.Golden      Fixed bug where HID disappeared when
 *                                           switching perspectives, and could not
 *                                           be made visible again without bouncing
 *                                           H.S. (and sometimes CAVE).
 * Oct 20, 2014   5047     mpduff            Add Null Check.
 * Oct 20, 2014   4818     Chris.Golden      Changed from tracking of raw metadata
 *                                           scroll origins for each hazard event to
 *                                           more comprehensive megawidget display
 *                                           settings.
 * Feb 03, 2015   2331     Chris.Golden      Added support for limiting the values
 *                                           that an event's start or end time can
 *                                           take on.
 * Mar 06, 2015   3850     Chris.Golden      Added code to make the category and type
 *                                           lists change according to whether the
 *                                           event being shown has a point ID (if not
 *                                           yet issued), or what it can be replaced
 *                                           by (if issued).
 * Apr 09, 2015   7382     Chris.Golden      Added "show start-end time sliders" flag.
 * Apr 15, 2015   3508     Chris.Golden      Added "hazard detail to be wide" flag.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class HazardDetailView extends
        ViewPartDelegateView<HazardDetailViewPart> implements
        IHazardDetailViewDelegate<Action, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Scheduler to be used to make {@link IWidget} handlers get get executed on
     * the main thread. For now, the main thread is the UI thread; when this is
     * changed, this will be rendered obsolete, as at that point there will need
     * to be a blocking queue of {@link Runnable} instances available to allow
     * the new worker thread to be fed jobs. At that point, this should be
     * replaced with an object that enqueues the <code>Runnable</code>s,
     * probably a singleton that may be accessed by the various components in
     * gov.noaa.gsd.viz.hazards and perhaps elsewhere.
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
     * Name of the file holding the image for the hazard detail toolbar button
     * icon.
     */
    private static final String HAZARD_DETAIL_TOOLBAR_IMAGE_FILE_NAME = "hazardInfo.png";

    /**
     * Text describing the hazard detail toolbar button.
     */
    private static final String HAZARD_DETAIL_TEXT = "Hazard Information";

    /**
     * Suffix for the preferences key used to determine whether or not to use
     * the previous size and position of the view part when showing it.
     */
    private static final String USE_PREVIOUS_SIZE_AND_POSITION_KEY_SUFFIX = ".usePreviousHazardDetailViewPartSizeAndPosition";

    // Private Variables

    /**
     * Hazard detail toggle action.
     */
    private Action hazardDetailToggleAction = null;

    /**
     * Minimum visible time to be shown in the time megawidgets.
     */
    private long minVisibleTime = 0L;

    /**
     * Maximum visible time to be shown in the time megawidgets.
     */
    private long maxVisibleTime = 0L;

    /**
     * Current time provider.
     */
    private ICurrentTimeProvider currentTimeProvider;

    /**
     * Flag indicating whether or not the start-end time UI elements should
     * include a sliders-equipped scale bar.
     */
    private boolean showStartEndTimeScale;

    /**
     * Flag indicating whether or not the view is to built for wide viewing.
     */
    private boolean buildForWideViewing;

    /**
     * Flag indicating whether or not to include the Issue button.
     */
    private boolean includeIssueButton;

    /**
     * Map pairing event identifiers with any extra data they may have used in
     * previous view instantiations, allowing such data to persist between
     * different views.
     */
    private Map<String, Map<String, Map<String, Object>>> extraDataForEventIdentifiers;

    /**
     * View part listener.
     */
    private final IPartListener2 partListener = new IPartListener2() {

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {

            /*
             * No action.
             */
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {

            /*
             * No action.
             */
        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setChecked(false);
                }
                if (detailViewVisibilityChangeHandler != null) {
                    detailViewVisibilityChangeHandler.stateChanged(null, false);
                }
                viewPartShowing = false;
                hideViewPart(true);
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {

            /*
             * No action.
             */
        }

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {

            /*
             * No action.
             */
        }

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setChecked(false);
                }
                if (detailViewVisibilityChangeHandler != null) {
                    detailViewVisibilityChangeHandler.stateChanged(null, false);
                }
                viewPartShowing = false;
            }
        }

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setChecked(true);
                }
                if (detailViewVisibilityChangeHandler != null) {
                    detailViewVisibilityChangeHandler.stateChanged(null, true);
                }
                viewPartShowing = true;
            }
        }

        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {

            /*
             * No action.
             */
        }
    };

    /**
     * Map of hazard event identifiers to their metadata megawidgets' display
     * settings; the latter are forwarded to this object by the principal
     * whenever the latter is about to delete a megawidget manager, so that the
     * same display settings for that event may be restored if that event is
     * shown again.
     */
    private final Map<String, Map<String, IDisplaySettings>> megawidgetDisplaySettingsForEventIds = new HashMap<>();

    /**
     * Metadata megawidgets' display settings change handler.
     */
    private final IStateChangeHandler<String, Map<String, IDisplaySettings>> megawidgetDisplaySettingsChangeHandler = new IStateChangeHandler<String, Map<String, IDisplaySettings>>() {

        @Override
        public void stateChanged(String identifier,
                Map<String, IDisplaySettings> value) {
            megawidgetDisplaySettingsForEventIds.put(identifier, value);
        }

        @Override
        public void statesChanged(
                Map<String, Map<String, IDisplaySettings>> valuesForIdentifiers) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Detail view visibility state change handler.
     */
    private IStateChangeHandler<String, Boolean> detailViewVisibilityChangeHandler;

    /**
     * Detail view visibility state changer.
     */
    private final IStateChanger<String, Boolean> detailViewVisibilityChanger = new IStateChanger<String, Boolean>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            if (hazardDetailToggleAction != null) {
                hazardDetailToggleAction.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of hazard detail view toggle");
        }

        @Override
        public Boolean getState(String identifier) {
            if (hazardDetailToggleAction != null) {
                return hazardDetailToggleAction.isChecked();
            }
            return false;
        }

        @Override
        public void setState(String identifier, Boolean value) {
            if (hazardDetailToggleAction != null) {
                hazardDetailToggleAction.setChecked(Boolean.TRUE.equals(value));
            }
            setDetailViewVisibility(value, false);
        }

        @Override
        public void setStates(Map<String, Boolean> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for hazard detail view toggle");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, Boolean> handler) {
            detailViewVisibilityChangeHandler = handler;
        }
    };

    /**
     * Detail view visibility state changer delegate.
     */
    private final IStateChanger<String, Boolean> detailViewVisibilityChangerDelegate = new StateChangerDelegate<>(
            new BasicWidgetDelegateHelper<>(detailViewVisibilityChanger),
            RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Visible time range state changer delegate.
     */
    private final IStateChanger<String, TimeRange> visibleTimeRangeChanger = new StateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IStateChanger<String, TimeRange>>() {

                        @Override
                        public IStateChanger<String, TimeRange> call()
                                throws Exception {
                            return getViewPart().getVisibleTimeRangeChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Visible event state changer delegate.
     */
    private final IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> visibleEventChanger = new ChoiceStateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IChoiceStateChanger<String, String, String, DisplayableEventIdentifier>>() {

                        @Override
                        public IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> call()
                                throws Exception {
                            return getViewPart().getVisibleEventChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Category state changer delegate.
     */
    private final IChoiceStateChanger<String, String, String, String> categoryChanger = new ChoiceStateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IChoiceStateChanger<String, String, String, String>>() {

                        @Override
                        public IChoiceStateChanger<String, String, String, String> call()
                                throws Exception {
                            return getViewPart().getCategoryChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Type state changer delegate.
     */
    private final IChoiceStateChanger<String, String, String, String> typeChanger = new ChoiceStateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IChoiceStateChanger<String, String, String, String>>() {

                        @Override
                        public IChoiceStateChanger<String, String, String, String> call()
                                throws Exception {
                            return getViewPart().getTypeChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Time range state changer delegate.
     */
    private final IStateChanger<String, TimeRange> timeRangeChanger = new StateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IStateChanger<String, TimeRange>>() {

                        @Override
                        public IStateChanger<String, TimeRange> call()
                                throws Exception {
                            return getViewPart().getTimeRangeChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Time range boundaries state changer delegate.
     */
    private final IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>> timeRangeBoundariesChanger = new QualifiedStateChangerDelegate<>(
            new ViewPartQualifiedWidgetDelegateHelper<>(
                    new Callable<IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>>>() {

                        @Override
                        public IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>> call()
                                throws Exception {
                            return getViewPart()
                                    .getTimeRangeBoundariesChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Duration state changer delegate.
     */
    private final IChoiceStateChanger<String, String, String, String> durationChanger = new ChoiceStateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IChoiceStateChanger<String, String, String, String>>() {

                        @Override
                        public IChoiceStateChanger<String, String, String, String> call()
                                throws Exception {
                            return getViewPart().getDurationChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Metadata state changer delegate.
     */
    private final IMetadataStateChanger metadataChanger = new MetadataStateChangerDelegate(
            new ViewPartQualifiedWidgetDelegateHelper<>(
                    new Callable<IMetadataStateChanger>() {

                        @Override
                        public IMetadataStateChanger call() throws Exception {
                            return getViewPart().getMetadataChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Notifier invoker delegate.
     */
    private final ICommandInvoker<EventScriptInfo> notifierInvoker = new CommandInvokerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<ICommandInvoker<EventScriptInfo>>() {

                        @Override
                        public ICommandInvoker<EventScriptInfo> call()
                                throws Exception {
                            return getViewPart().getNotifierInvoker();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Button invoker delegate.
     */
    private final ICommandInvoker<Command> buttonInvoker = new CommandInvokerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<ICommandInvoker<Command>>() {

                        @Override
                        public ICommandInvoker<Command> call() throws Exception {
                            return getViewPart().getButtonInvoker();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Flag indicating whether or not the view part is showing (the alternative
     * is that it is minimized).
     */
    private boolean viewPartShowing = true;

    /**
     * Flag indicating whether or not to use the previous size and position for
     * the view part from the moment it is created.
     */
    private boolean usePreviousSizeAndPosition;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not the view is being instantiated
     *            as a result of a bundle load.
     */
    public HazardDetailView(final boolean loadedFromBundle) {
        super(HazardDetailViewPart.ID, HazardDetailViewPart.class);

        /*
         * Determine whether the view part uses its previous size and position
         * by seeing whether the flag indicating this should happen exists. If
         * it does not, create the flag in the preferences so that future
         * invocations of the hazard detail view use previous size and position.
         */
        WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
        String usePreviousSizeAndPositionKey = page.getPerspective().getId()
                + USE_PREVIOUS_SIZE_AND_POSITION_KEY_SUFFIX;
        IPreferenceStore preferenceStore = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        usePreviousSizeAndPosition = preferenceStore
                .contains(usePreviousSizeAndPositionKey);
        if (usePreviousSizeAndPosition == false) {
            preferenceStore.setValue(usePreviousSizeAndPositionKey, true);
        }

        /*
         * Show the view part.
         */
        showViewPart();

        /*
         * Execute further manipulation of the view part immediately, or delay
         * such execution until the view part is created if it has not yet been
         * created yet.
         */
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                /*
                 * If previous size and position is not to be used, detach the
                 * view, as this is the default starting state. It would be nice
                 * if this were possible via the plugin.xml perspective
                 * extension entry for this view, but apparently it can only be
                 * started off detached programmatically. If the view is not
                 * being detached, then if it is being instantiated as a result
                 * of a bundle load, minimize it.
                 */
                WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
                if ((usePreviousSizeAndPosition == false) && (page != null)) {
                    page.detachView(page
                            .findViewReference(HazardDetailViewPart.ID));
                } else if (loadedFromBundle && isViewPartDocked()) {
                    setViewPartVisible(false);
                    viewPartShowing = false;
                }

                /*
                 * Set the use previous size and position flag to true so that
                 * any future showings of the view part by this view come up
                 * with previous dimensions.
                 */
                usePreviousSizeAndPosition = true;

                /*
                 * Register the part listener for view part events so that the
                 * closing of the hazard detail view part may be responded to.
                 */
                setPartListener(partListener);
            }
        });
    }

    // Public Methods

    @Override
    public final void initialize(
            long minVisibleTime,
            long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider,
            boolean showStartEndTimeScale,
            boolean buildForWideViewing,
            boolean includeIssueButton,
            Map<String, Map<String, Map<String, Object>>> extraDataForEventIdentifiers) {
        this.minVisibleTime = minVisibleTime;
        this.maxVisibleTime = maxVisibleTime;
        if (minVisibleTime == maxVisibleTime) {
            this.maxVisibleTime = this.minVisibleTime
                    + TimeUnit.DAYS.toMillis(1);
        }
        this.currentTimeProvider = currentTimeProvider;
        this.showStartEndTimeScale = showStartEndTimeScale;
        this.buildForWideViewing = buildForWideViewing;
        this.extraDataForEventIdentifiers = extraDataForEventIdentifiers;
        this.includeIssueButton = includeIssueButton;

        /*
         * Execute manipulation of the view part immediately, or delay such
         * execution until the view part is created if it has not yet been
         * created yet.
         */
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                initializeViewPart();
            }
        });
    }

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            /*
             * Create the actions.
             */
            boolean showing = isViewPartVisible();
            hazardDetailToggleAction = new BasicAction("",
                    HAZARD_DETAIL_TOOLBAR_IMAGE_FILE_NAME, Action.AS_CHECK_BOX,
                    HAZARD_DETAIL_TEXT) {
                @Override
                public void run() {
                    boolean showing = isViewPartVisible();
                    if (isChecked() && (showing == false)) {
                        setDetailViewVisibility(true, true);
                        if (detailViewVisibilityChangeHandler != null) {
                            detailViewVisibilityChangeHandler.stateChanged(
                                    null, true);
                        }
                    } else if ((isChecked() == false) && showing) {
                        setDetailViewVisibility(false, true);
                        if (detailViewVisibilityChangeHandler != null) {
                            detailViewVisibilityChangeHandler.stateChanged(
                                    null, false);
                        }
                    }
                }
            };
            hazardDetailToggleAction.setChecked(showing);
            return Lists.newArrayList(hazardDetailToggleAction);
        }
        return Collections.emptyList();
    }

    @Override
    public IStateChanger<String, Boolean> getDetailViewVisibilityChanger() {
        return detailViewVisibilityChangerDelegate;
    }

    @Override
    public IStateChanger<String, TimeRange> getVisibleTimeRangeChanger() {
        return visibleTimeRangeChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> getVisibleEventChanger() {
        return visibleEventChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getCategoryChanger() {
        return categoryChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getTypeChanger() {
        return typeChanger;
    }

    @Override
    public IStateChanger<String, TimeRange> getTimeRangeChanger() {
        return timeRangeChanger;
    }

    @Override
    public IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>> getTimeRangeBoundariesChanger() {
        return timeRangeBoundariesChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getDurationChanger() {
        return durationChanger;
    }

    @Override
    public IMetadataStateChanger getMetadataChanger() {
        return metadataChanger;
    }

    @Override
    public ICommandInvoker<EventScriptInfo> getNotifierInvoker() {
        return notifierInvoker;
    }

    @Override
    public ICommandInvoker<Command> getButtonInvoker() {
        return buttonInvoker;
    }

    // Protected Methods

    /**
     * Respond to an attempt to execute some action via
     * {@link #executeOnCreatedViewPart(Runnable)} upon a view part when the
     * view part is not in existence and no attempt has been made to create it.
     * Since the view part may have been closed between the scheduling of an
     * action for execution and said execution occurring, and this is not a
     * problem, nothing needs to be done.
     * 
     * @param job
     *            Action for which execution was attempted.
     */
    @Override
    protected void actionExecutionAttemptedUponNonexistentViewPart(Runnable job) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Initialize the view part.
     */
    private void initializeViewPart() {

        /*
         * Do the basic initialization.
         */
        getViewPart().initialize(minVisibleTime, maxVisibleTime,
                currentTimeProvider, showStartEndTimeScale,
                buildForWideViewing, includeIssueButton,
                extraDataForEventIdentifiers);

        /*
         * Register the megawidget display settings change handler with the view
         * part, so that notifications of megawidget display settings changes
         * for event identifiers are sent to this object and they can be
         * recorded to be available for a new view part if the old one is
         * closed. Then send the accumulated megawidget display settings to the
         * new view part to initialize it.
         */
        getViewPart().getMegawidgetDisplaySettingsChanger()
                .setStateChangeHandler(megawidgetDisplaySettingsChangeHandler);
        getViewPart().getMegawidgetDisplaySettingsChanger().setStates(
                megawidgetDisplaySettingsForEventIds);
    }

    /**
     * Set the detail view visibility as specified.
     * 
     * @param visible
     *            Flag indicating whether or not the detail view should be
     *            visible.
     * @param force
     *            Flag indicating whether, if the view is to be hidden, it
     *            should be forced to be hidden regardless of whether it is
     *            docked or not, or instead should only be hidden if undocked.
     */
    private void setDetailViewVisibility(boolean visible, boolean force) {

        /*
         * If the view part should be made visible, or it should be made
         * invisible but it is not forced to be so and it is docked, then make
         * it visible. Otherwise, hide it.
         */
        if (visible || ((force == false) && isViewPartDocked())) {

            /*
             * If the view part does not exist, show it. If it is already
             * showing and was to be hidden, but because it is docked it will
             * not be, notify the visibility change handler of this.
             */
            final boolean needsInitializing = (getViewPart() == null);
            if (needsInitializing) {
                viewPartShowing = true;
                showViewPart();
            } else if ((visible == false) && viewPartShowing) {
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setChecked(true);
                }
                if (detailViewVisibilityChangeHandler != null) {
                    detailViewVisibilityChangeHandler.stateChanged(null, true);
                }
            }

            /*
             * Execute further manipulation of the view part immediately, or
             * delay such execution until the view part is created if it has not
             * yet been created yet.
             */
            executeOnCreatedViewPart(new Runnable() {
                @Override
                public void run() {

                    /*
                     * Initialize the view part if necessary.
                     */
                    if (needsInitializing) {
                        initializeViewPart();
                    }

                    /*
                     * Ensure that the view part is visible.
                     */
                    if (isViewPartVisible() == false) {
                        setViewPartVisible(true);
                    }
                }
            });
        } else {

            /*
             * If the view part is not showing, it may have been meant to be
             * showing, but was unable to because its instantiation was delayed
             * by another view part with the same identifier already existing.
             * If this is the case, clear the jobs queue for the view part, as
             * it should now no longer be brought up once the old view part
             * disappears. Otherwise, if the view part is showing and docked,
             * minimize it; otherwise, hide it.
             */
            if (getViewPart() == null) {
                hideViewPart(true);
            } else if (isViewPartDocked() == false) {
                hideViewPart(false);
            } else {
                setViewPartVisible(false);
            }
        }
    }

    /**
     * Determine whether or not the view part is currently visible.
     * 
     * @return True if the view part is currently visible, false otherwise.
     */
    private boolean isViewPartVisible() {
        return ((getViewPart() != null) && viewPartShowing);
    }

    /**
     * Determine whether or not the view part is docked, or if invisible, was
     * docked when last visible.
     * 
     * @return True if the view part is docked, or if invisible, was docked the
     *         last time it was visible.
     */
    private boolean isViewPartDocked() {
        return (getViewPart() == null ? false : getViewPart().isDocked());
    }
}
