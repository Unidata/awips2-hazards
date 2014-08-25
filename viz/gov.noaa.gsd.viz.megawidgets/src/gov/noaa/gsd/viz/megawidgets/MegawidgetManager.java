/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.Lists;

/**
 * <p>
 * Megawidget manager class, used to instantiate megawidgets based upon
 * specifiers and to manage their state changes.
 * </p>
 * <p>
 * The manager is instantiated using parameters that include a list of maps,
 * each defining a megawidget specifier (or optionally, a
 * {@link MegawidgetSpecifierManager}; a map providing the state that the
 * megawidgets will allow the user to view and/or manipulate; and time boundary
 * values for any time scale megawidgets that will be created. The state map's
 * elements are known as <i>state elements</i>, as distinguished from
 * <i>megawidget states</i>. The two are often identical, but see below for
 * cases where conversion must be performed.
 * </p>
 * <p>
 * Each of the megawidget state identifiers is of the form <code>
 * (idElementN&gt;)*idElement</code>, that is, an identifier string <code>
 * idElement</code>, preceded by zero or more strings, each consisting of a
 * different <code>idElementN</code> followed by a greater-than (&gt;)
 * character. Each of these optional preceding <code>idElementN</code> provides
 * the key associated with a map value, thus allowing the state map to have
 * nested maps that hold state elements associated with megawidgets, or
 * themselves holding nested maps, etc. As an example, a state identifier for a
 * megawidget that is <code>geoParams&gt;lat</code> indicates that the state map
 * should associate the string <code>geoParams</code> with a nested map, and
 * that the latter should associate <code>lat</code> with a value.
 * </p>
 * <p>
 * Subclasses that require that their megawidgets use modified (converted) forms
 * of state elements for their states must override the methods
 * {@link #convertStateElementToMegawidgetState(String, Object)} and
 * {@link #convertMegawidgetStateToStateElement(String, Object)}. For example,
 * if a state element is an integer, but the megawidget that is to allow viewing
 * and/or manipulation of the state element takes a string as its megawidget
 * state, these methods would be overridden to perform these conversions in both
 * directions.
 * </p>
 * <p>
 * When constructed, the manager may be provided a side effects applier if
 * desired. The latter has a method invoked to apply any side effects it deems
 * appropriate when the manager first initializes the megawidgets; whenever a
 * notifier megawidget is invoked; and whenever a stateful megawidget has its
 * state changed either programmatically or via the user GUI. Said method is
 * passed all the current values of all managed megawidgets' mutable properties,
 * and is allowed to change these properties as it sees fit. If no side effects
 * applier is supplied, the manager assumes no side effects are desired when
 * megawidgets are invoked.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 01, 2013            Chris.Golden      Initial creation.
 * May 06, 2013    1277    Chris.Golden      Added support for mutable properties,
 *                                           and side effect application execution.
 * Sep 25, 2013    2168    Chris.Golden      Switched to using IParent instead of
 *                                           IContainer, and made compatible with
 *                                           new version of MegawidgetSpecifier and
 *                                           MegawidgetSpecifierFactory.
 * Nov 19, 2013    2336    Chris.Golden      Added to setMutableProperties() Java-
 *                                           doc a note clarifying what it does,
 *                                           and added method to retrieve the
 *                                           parent SWT widget of the megawidgets.
 * Dec 16, 2013    2545    Chris.Golden      Added current time provider for
 *                                           megawidget use.
 * Feb 14, 2014    2161    Chris.Golden      Changed to use the side effects applier
 *                                           if the state of one or more megawidgets
 *                                           is changed programmatically, not just
 *                                           via the GUI.
 * Mar 07, 2014    2925    Chris.Golden      Changed to use the new megawidget
 *                                           specifier manager to do construction
 *                                           and validation of the specifiers.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * May 12, 2014    2925    Chris.Golden      Added modifyState() method and
 *                                           getSpecifierManager() method, and changed
 *                                           to take current time provider from the
 *                                           megawidget specifier manager.
 * Jun 17, 2014    3982    Chris.Golden      Changed to allow simpler specification
 *                                           of "values" mutable property for single-
 *                                           state megawidgets.
 * Jun 23, 2014    4010    Chris.Golden      Changed to allow listeners to be passed
 *                                           in at creation time instead of forcing
 *                                           clients to subclass this (i.e. it is not
 *                                           abstract anymore). Also added ability for
 *                                           manager to notify clients that its
 *                                           composite has changed layout when a
 *                                           size-changing megawidget causes a resize.
 * Jun 25, 2014    4009    Chris.Golden      Added convenience methods allowing the
 *                                           getting and setting of extra data for
 *                                           all managed megawidgets. Also changed
 *                                           side effects application to cause state
 *                                           change notifications to be sent out if
 *                                           the side effects included state changes,
 *                                           and changed notifications of whatever
 *                                           prompted the side effects to be applied
 *                                           so that they go out before the application
 *                                           of the side effects.
 * Jun 30, 2014    3512     Chris.Golden     Changed to use a listener to send out
 *                                           notifications of megawidget events instead
 *                                           of being an abstract class that forces the
 *                                           creation of subclasses to receive such
 *                                           notifications.
 * Aug 19, 2014    4098     Chris.Golden     Changed to provide a method for accessing
 *                                           SWT wrapper megawidgets.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 * @see MegawidgetSpecifier
 * @see IStateful
 * @see IStatefulSpecifier
 * @see ISideEffectsApplier
 * @see IMegawidgetManagerListener
 */
public class MegawidgetManager {

    // Private Variables

    /**
     * Megawidget specifier manager, from which the specifiers of the
     * megawidgets being managed were constructed.
     */
    private MegawidgetSpecifierManager specifierManager;

    /**
     * Listener.
     */
    private IMegawidgetManagerListener managerListener;

    /**
     * Parent widget of the megawidgets being managed.
     */
    private Widget parent;

    /**
     * Map of megawidget specifier identifiers to their values.
     */
    private Map<String, Object> state;

    /**
     * Map pairing megawidget identifiers with their megawidgets, with entries
     * for all megawidgets that are being managed.
     */
    private final Map<String, IMegawidget> megawidgetsForIdentifiers = new HashMap<>();

    /**
     * Map pairing megawidget identifiers with their corresponding stateful
     * megawidgets.
     */
    private final Map<String, IStateful> statefulMegawidgetsForIdentifiers = new HashMap<>();

    /**
     * Map pairing state identifiers with their corresponding megawidget
     * identifiers.
     */
    private final Map<String, String> megawidgetIdentifiersForStateIdentifiers = new HashMap<>();

    /**
     * Set of all time scale megawidgets.
     */
    private final Set<TimeScaleMegawidget> timeScaleMegawidgets = new HashSet<>();

    /**
     * Map pairing megawidget identifiers with their corresponding SWT wrapper
     * megawidgets.
     */
    private final Map<String, SwtWrapperMegawidget> swtWrappersForIdentifiers = new HashMap<>();

    /**
     * Side effects applier, or <code>null</code> if there are no side effects
     * to be applied.
     */
    private ISideEffectsApplier sideEffectsApplier;

    /**
     * Flag indicating whether or not the managed megawidgets are currently
     * enabled.
     */
    private boolean enabled = true;

    /**
     * Flag indicating whether or not the mutable properties of any megawidget
     * may have been changed programmatically since the last non-programmatic
     * change.
     */
    private boolean propertyProgrammaticallyChanged = true;

    /**
     * Notification listener.
     */
    private final INotificationListener notificationListener = new INotificationListener() {

        @Override
        public void megawidgetInvoked(INotifier megawidget) {

            /*
             * Send out notification of the invocation.
             */
            if (managerListener != null) {
                managerListener.commandInvoked(MegawidgetManager.this,
                        megawidget.getSpecifier().getIdentifier());
            }

            /*
             * If a side effects applier is available and the invoked megawidget
             * is not stateful, apply side effects. Stateful megawidgets do not
             * get side effects applied here because the latter has already been
             * done when they experienced the state change that preceded this
             * notification.
             */
            if ((sideEffectsApplier != null)
                    && ((megawidget instanceof IStateful) == false)) {
                applySideEffects(Lists.newArrayList(megawidget.getSpecifier()
                        .getIdentifier()), false);
            }
        }
    };

    /**
     * State change listener.
     */
    private final IStateChangeListener stateChangeListener = new IStateChangeListener() {

        @Override
        public void megawidgetStateChanged(IStateful megawidget,
                String identifier, Object state) {

            /*
             * Do any preprocessing of the state required by a subclass.
             */
            state = convertMegawidgetStateToStateElement(identifier, state);

            /*
             * Remember the new state.
             */
            commitStateElementChange(identifier, state);

            /*
             * Send out notification of the state change.
             */
            if (managerListener != null) {
                managerListener.stateElementChanged(MegawidgetManager.this,
                        identifier, state);
            }

            /*
             * If a side effects applier is available, apply side effects.
             */
            if (sideEffectsApplier != null) {
                applySideEffects(Lists.newArrayList(megawidget.getSpecifier()
                        .getIdentifier()), true);
            }
        }

        @Override
        public void megawidgetStatesChanged(IStateful megawidget,
                Map<String, Object> statesForIdentifiers) {

            /*
             * Process the changed states within this manager.
             */
            for (Map.Entry<String, Object> entry : statesForIdentifiers
                    .entrySet()) {

                /*
                 * Do any preprocessing of the state required by a subclass.
                 */
                entry.setValue(convertMegawidgetStateToStateElement(
                        entry.getKey(), entry.getValue()));

                /*
                 * Remember the new state.
                 */
                commitStateElementChange(entry.getKey(), entry.getValue());
            }

            /*
             * Send out notification of the state changes.
             */
            if (managerListener != null) {
                managerListener.stateElementsChanged(MegawidgetManager.this,
                        statesForIdentifiers);
            }

            /*
             * If a side effects applier is available, apply side effects before
             * sending along the notification of the state change.
             */
            if (sideEffectsApplier != null) {
                applySideEffects(Lists.newArrayList(megawidget.getSpecifier()
                        .getIdentifier()), true);
            }
        }
    };

    /**
     * Resize listener.
     */
    private final IResizeListener resizeListener = new IResizeListener() {

        @Override
        public void sizeChanged(IResizer megawidget) {
            if (managerListener != null) {
                managerListener.sizeChanged(MegawidgetManager.this, megawidget
                        .getSpecifier().getIdentifier());
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance for managing megawidgets that exist within
     * a menu widget with no side effecs applier.
     * 
     * @param parent
     *            Parent menu in which the megawidgets are to be created.
     * @param specifiers
     *            List of maps, each of the latter holding the parameters of a
     *            megawidget specifier. Each megawidget specifier must have an
     *            identifier that is unique within this list.
     * @param state
     *            State to be viewed and/or modified via the megawidgets that
     *            are constructed. Each megawidget specifier defined by <code>
     *            specifiers</code> should have an entry in this map, mapping
     *            the specifier's identifier to the value that the megawidget
     *            will take on (with conversions between megawidget state and
     *            state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @param managerListener
     *            Listener to be notified of events that occur within this
     *            manager.
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    public MegawidgetManager(Menu parent,
            List<? extends Map<String, Object>> specifiers,
            Map<String, Object> state,
            IMegawidgetManagerListener managerListener)
            throws MegawidgetException {
        this(parent, specifiers, state, managerListener, null);
    }

    /**
     * Construct a standard instance for managing megawidgets that exist within
     * a menu widget.
     * 
     * @param parent
     *            Parent menu in which the megawidgets are to be created.
     * @param specifiers
     *            List of maps, each of the latter holding the parameters of a
     *            megawidget specifier. Each megawidget specifier must have an
     *            identifier that is unique within this list.
     * @param state
     *            State to be viewed and/or modified via the megawidgets that
     *            are constructed. Each megawidget specifier defined by <code>
     *            specifiers</code> should have an entry in this map, mapping
     *            the specifier's identifier to the value that the megawidget
     *            will take on (with conversions between megawidget state and
     *            state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @param managerListener
     *            Listener to be notified of events that occur within this
     *            manager.
     * @param sideEffectsApplier
     *            Side effects applier to be used, or <code>null</code> if no
     *            side effects application is to be done by the manager.
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    public MegawidgetManager(Menu parent,
            List<? extends Map<String, Object>> specifiers,
            Map<String, Object> state,
            IMegawidgetManagerListener managerListener,
            ISideEffectsApplier sideEffectsApplier) throws MegawidgetException {
        this(parent, new MegawidgetSpecifierManager(specifiers,
                IMenuSpecifier.class, null, sideEffectsApplier), state,
                managerListener);
    }

    /**
     * Construct a standard instance for managing megawidgets that exist within
     * a menu widget.
     * 
     * @param parent
     *            Parent menu in which the megawidgets are to be created.
     * @param specifierManager
     *            Megawidget specifier manager holding the specifiers that are
     *            to govern the creation of the megawidgets.
     * @param state
     *            State to be viewed and/or modified via the megawidgets that
     *            are constructed. Each megawidget specifier defined by <code>
     *            specifiers</code> should have an entry in this map, mapping
     *            the specifier's identifier to the value that the megawidget
     *            will take on (with conversions between megawidget state and
     *            state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @param managerListener
     *            Listener to be notified of events that occur within this
     *            manager.
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    public MegawidgetManager(Menu parent,
            MegawidgetSpecifierManager specifierManager,
            Map<String, Object> state,
            IMegawidgetManagerListener managerListener)
            throws MegawidgetException {
        construct(parent, IMenu.class, specifierManager, state,
                managerListener, 0L, 0L);
    }

    /**
     * Construct a standard instance for managing megawidgets that exist within
     * a composite widget with no side effects applier.
     * 
     * @param parent
     *            Parent composite in which the megawidgets are to be created.
     * @param specifiers
     *            List of maps, each of the latter holding the parameters of a
     *            megawidget specifier. Each megawidget specifier must have an
     *            identifier that is unique within this list.
     * @param state
     *            State to be viewed and/or modified via the megawidgets that
     *            are constructed. Each megawidget specifier defined by <code>
     *            specifiers</code> should have an entry in this map, mapping
     *            the specifier's identifier to the value that the megawidget
     *            will take on (with conversions between megawidget state and
     *            state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @param managerListener
     *            Listener to be notified of events that occur within this
     *            manager.
     * @param minVisibleTime
     *            Minimum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param maxVisibleTime
     *            Maximum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param currentTimeProvider
     *            Current time provider for any time megawidgets specified
     *            within <code>specifiers</code>. If <code>null</code>, a
     *            default current time provider is used. If no time megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    public MegawidgetManager(Composite parent,
            List<? extends Map<String, Object>> specifiers,
            Map<String, Object> state,
            IMegawidgetManagerListener managerListener, long minVisibleTime,
            long maxVisibleTime, ICurrentTimeProvider currentTimeProvider)
            throws MegawidgetException {
        this(parent, specifiers, state, managerListener, minVisibleTime,
                maxVisibleTime, currentTimeProvider, null);
    }

    /**
     * Construct a standard instance for managing megawidgets that exist within
     * a composite widget.
     * 
     * @param parent
     *            Parent composite in which the megawidgets are to be created.
     * @param specifiers
     *            List of maps, each of the latter holding the parameters of a
     *            megawidget specifier. Each megawidget specifier must have an
     *            identifier that is unique within this list.
     * @param state
     *            State to be viewed and/or modified via the megawidgets that
     *            are constructed. Each megawidget specifier defined by <code>
     *            specifiers</code> should have an entry in this map, mapping
     *            the specifier's identifier to the value that the megawidget
     *            will take on (with conversions between megawidget state and
     *            state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @param managerListener
     *            Listener to be notified of events that occur within this
     *            manager.
     * @param minVisibleTime
     *            Minimum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param maxVisibleTime
     *            Maximum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param currentTimeProvider
     *            Current time provider for any time megawidgets specified
     *            within <code>specifiers</code>. If <code>null</code>, a
     *            default current time provider is used. If no time megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param sideEffectsApplier
     *            Side effects applier to be used, or <code>null</code> if no
     *            side effects application is to be done by the manager.
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    public MegawidgetManager(Composite parent,
            List<? extends Map<String, Object>> specifiers,
            Map<String, Object> state,
            IMegawidgetManagerListener managerListener, long minVisibleTime,
            long maxVisibleTime, ICurrentTimeProvider currentTimeProvider,
            ISideEffectsApplier sideEffectsApplier) throws MegawidgetException {
        this(parent, new MegawidgetSpecifierManager(specifiers,
                IControlSpecifier.class, currentTimeProvider,
                sideEffectsApplier), state, managerListener, minVisibleTime,
                maxVisibleTime);
    }

    /**
     * Construct a standard instance for managing megawidgets that exist within
     * a composite widget.
     * 
     * @param parent
     *            Parent composite in which the megawidgets are to be created.
     * @param specifierManager
     *            Megawidget specifier manager holding the specifiers that are
     *            to govern the creation of the megawidgets.
     * @param state
     *            State to be viewed and/or modified via the megawidgets that
     *            are constructed. Each megawidget specifier within <code>
     *            specifierManager</code> should have an entry in this map,
     *            mapping the specifier's identifier to the value that the
     *            megawidget will take on (with conversions between megawidget
     *            state and state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @param managerListener
     *            Listener to be notified of events that occur within this
     *            manager.
     * @param minVisibleTime
     *            Minimum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param maxVisibleTime
     *            Maximum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    public MegawidgetManager(Composite parent,
            MegawidgetSpecifierManager specifierManager,
            Map<String, Object> state,
            IMegawidgetManagerListener managerListener, long minVisibleTime,
            long maxVisibleTime) throws MegawidgetException {

        /*
         * Ensure that the parent has the properly configured layout manager.
         */
        Layout layout = parent.getLayout();
        if ((layout instanceof GridLayout) == false) {
            parent.setLayout(new GridLayout(1, false));
        } else if (((GridLayout) layout).numColumns != 1) {
            ((GridLayout) layout).numColumns = 1;
        }

        /*
         * Do the heavy lifting for construction.
         */
        Set<IControl> baseMegawidgets = construct(parent, IControl.class,
                specifierManager, state, managerListener, minVisibleTime,
                maxVisibleTime);

        /*
         * Align the base megawidgets' component elements to one another
         * visually.
         */
        ControlComponentHelper.alignMegawidgetsElements(baseMegawidgets);
        parent.layout();
    }

    // Public Methods

    /**
     * Get the specifier manager holding the specifiers that were used to
     * construct the megawidgets being managed.
     * 
     * @return Specifier manager.
     */
    public MegawidgetSpecifierManager getSpecifierManager() {
        return specifierManager;
    }

    /**
     * Get the parent widget of the megawidgets being managed.
     * 
     * @return Parent widget.
     */
    @SuppressWarnings("unchecked")
    public <P extends Widget> P getParent() {
        return (P) parent;
    }

    /**
     * Determine whether or not the manager and its megawidgets are currently
     * enabled not.
     * 
     * @return True if the manager and its megawidgets are currently enabled,
     *         false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the manager and its megawidgets.
     * 
     * @param enabled
     *            Flag indicating whether the manager and its megawidgets are to
     *            be enabled or disabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        for (IMegawidget megawidget : megawidgetsForIdentifiers.values()) {
            megawidget.setEnabled(enabled);
        }
        propertyProgrammaticallyChanged = true;
    }

    /**
     * Get the mutable properties of all megawidgets tracked by the manager.
     * 
     * @return Map of the identifiers of all megawidgets being managed to
     *         submaps holding the megawidgets' mutable properties. Each of the
     *         latter maps the mutable property names to their current values.
     */
    public final Map<String, Map<String, Object>> getMutableProperties() {
        Map<String, Map<String, Object>> mutableProperties = new HashMap<>();
        for (String identifier : megawidgetsForIdentifiers.keySet()) {
            mutableProperties.put(identifier,
                    megawidgetsForIdentifiers.get(identifier)
                            .getMutableProperties());
        }
        return mutableProperties;
    }

    /**
     * Set the specified mutable properties of the specified megawidgets to the
     * given values.
     * <p>
     * Note that {@link #convertStateElementToMegawidgetState(String, Object)}
     * is not used by this method to preprocess any state values that are being
     * changed within the mutable properties map. It is assumed that state
     * values being changed via a call to this method do not require conversion.
     * 
     * @param mutableProperties
     *            Map of the identifiers of all megawidgets for which mutable
     *            properties are to be changed to submaps holding the new
     *            mutable properties. Each of the latter maps the names of any
     *            mutable properties being changed to their new values.
     * @throws MegawidgetPropertyException
     *             If a problem occurs while attempting to set the mutable
     *             properties.
     */
    public final void setMutableProperties(
            Map<String, Map<String, Object>> mutableProperties)
            throws MegawidgetPropertyException {

        /*
         * Set the mutable properties, finding out what states changed in the
         * process of doing so.
         */
        Map<String, Object> valuesForChangedStates = doSetMutableProperties(mutableProperties);

        /*
         * If there is a side effects applier, and at least one state changed,
         * compile a set of the identifiers of all the megawidgets that
         * experienced a state change, and apply side effects.
         */
        if ((sideEffectsApplier != null) && (valuesForChangedStates != null)
                && (valuesForChangedStates.isEmpty() == false)) {
            Set<String> megawidgetsWithStateChanged = new HashSet<>(
                    valuesForChangedStates.size());
            for (String identifier : valuesForChangedStates.keySet()) {
                megawidgetsWithStateChanged
                        .add(megawidgetIdentifiersForStateIdentifiers
                                .get(identifier));
            }
            applySideEffects(megawidgetsWithStateChanged, true);
        }
    }

    /**
     * Get the current state tracked by the manager. The current state is
     * provided as a map that associates megawidget specifier identifier names
     * with the corresponding state elements.
     * 
     * @return Current state.
     */
    public final Map<String, Object> getState() {
        return state;
    }

    /**
     * Get the value for the state element with specified identifier.
     * 
     * @param identifier
     *            Identifier of the state element for which the value is to be
     *            retrieved.
     * @return State value, or <code>null</code> if no such value is found.
     * @throws MegawidgetStateException
     *             If a non-map object is found when a nested map is expected
     *             while fetching a state element that is nested.
     */
    public final Object getStateElement(String identifier)
            throws MegawidgetStateException {

        /*
         * Split up the identifier by greater-thans (>) it contains. It is
         * assumed that each string preceding a greater-than is a map, nested
         * within another map if it also has a greater-than before it, and only
         * the last string following the last greater-than is the name of the
         * actual key to the value. This allows the values map to include values
         * nested within other maps within it.
         */
        String[] keys = identifier.split(">");

        /*
         * Iterate through the keys for the nested maps, treating the value for
         * each as a map except for the last one, which is the value for the
         * megawidget's state.
         */
        Map<?, ?> map = state;
        for (int j = 0; j < keys.length; j++) {
            if (map == null) {
                return null;
            }
            Object value = map.get(keys[j]);
            if (j == keys.length - 1) {
                return value;
            } else {

                /*
                 * Get ready for the next iteration through the loop by using
                 * this value as the nested map to look in for the next step.
                 */
                if (value instanceof Map) {
                    map = (Map<?, ?>) value;
                } else {
                    if (value != null) {
                        throw new MegawidgetStateException(identifier, null,
                                null, "unexpected non-map object "
                                        + "found when looking for nested "
                                        + "state element map");
                    }
                    break;
                }
            }
        }
        return null;
    }

    /**
     * Set the current state held by the megawidgets to equal the values given
     * by the specified map associating megawidget identifiers with their new
     * values.
     * 
     * @param newState
     *            New state to be viewed and/or modified via the managed
     *            megawidgets. Each megawidget specifier that was defined by
     *            <code>specifiers</code> in the {@link #MegawidgetManager
     *            constructor} of the manager should have one or more entries in
     *            this map, associating the specifier's state identifier(s) with
     *            the value(s) that the megawidget will take on (with
     *            conversions between megawidget state and state element being
     *            performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @throws MegawidgetStateException
     *             If at least one megawidget determines that the new state is
     *             invalid.
     */
    public final void setState(Map<String, Object> newState)
            throws MegawidgetStateException {
        state = newState;
        for (IStateful megawidget : statefulMegawidgetsForIdentifiers.values()) {
            setState(megawidget, false);
        }
        propertyProgrammaticallyChanged = true;
        if (sideEffectsApplier != null) {
            applySideEffects(statefulMegawidgetsForIdentifiers.keySet(), true);
        }
    }

    /**
     * Modify the current state held by the megawidgets to include the values
     * given by the specified map associating megawidget identifiers with their
     * new values. Unlike {@link #setState(Map)}, any state identifiers not
     * found as keys in the specified map retain their previous values; only
     * those that are found in this map are changed to use the new values.
     * 
     * @param changedState
     *            Changed state to be viewed and/or modified via the managed
     *            megawidgets. Any megawidget specifier that was defined by
     *            <code>specifiers</code> in the {@link #MegawidgetManager
     *            constructor} of the manager may have one or more entries in
     *            this map, associating the specifier's identifier(s) with the
     *            value(s) that the megawidget will take on (with conversions
     *            between megawidget state and state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @throws MegawidgetStateException
     *             If at least one megawidget determines that the new state is
     *             invalid.
     */
    public final void modifyState(Map<String, ?> changedState)
            throws MegawidgetStateException {
        state.putAll(changedState);
        Set<IStateful> megawidgetsWithStateChange = new HashSet<>();
        for (String identifier : changedState.keySet()) {
            IStateful megawidget = statefulMegawidgetsForIdentifiers
                    .get(identifier);
            if (megawidget != null) {
                megawidgetsWithStateChange.add(megawidget);
            }
        }
        if (megawidgetsWithStateChange.size() > 0) {
            for (IStateful megawidget : megawidgetsWithStateChange) {
                setState(megawidget, false);
            }
            propertyProgrammaticallyChanged = true;
            if (sideEffectsApplier != null) {
                applySideEffects(changedState.keySet(), true);
            }
        }
    }

    /**
     * Set the minimum and maximum times currently visible for any time scale
     * megawidgets.
     * 
     * @param minVisibleTime
     *            Minimum visible time for time scale megawidgets.
     * @param maxVisibleTime
     *            Maximum visible time for time scale megawidgets.
     */
    public final void setVisibleTimeRange(long minVisibleTime,
            long maxVisibleTime) {
        for (TimeScaleMegawidget megawidget : timeScaleMegawidgets) {
            megawidget.setVisibleTimeRange(minVisibleTime, maxVisibleTime);
        }
        propertyProgrammaticallyChanged = true;
    }

    /**
     * Get the extra data associated with all managed megawidgets.
     * 
     * @return Map of any megawidget identifiers that have extra data to their
     *         extra data maps.
     */
    public final Map<String, Map<String, Object>> getExtraData() {
        Map<String, Map<String, Object>> extraDataMap = new HashMap<>();
        for (String identifier : megawidgetsForIdentifiers.keySet()) {
            Map<String, Object> extraData = megawidgetsForIdentifiers.get(
                    identifier).getExtraData();
            if (extraData.isEmpty() == false) {
                extraDataMap.put(identifier, extraData);
            }
        }
        return extraDataMap;
    }

    /**
     * Set the extra data for any managed megawidgets for which an entry is
     * found in the specified map.
     * 
     * @param extraDataMap
     *            Map pairing megawidget identifiers with their new extra data.
     *            Any megawidgets that are being managed that do not have an
     *            entry in this map have their extra data left untouched.
     */
    public final void setExtraData(Map<String, Map<String, Object>> extraDataMap) {
        for (String identifier : extraDataMap.keySet()) {
            if (megawidgetsForIdentifiers.containsKey(identifier) == false) {
                continue;
            }
            megawidgetsForIdentifiers.get(identifier).setExtraData(
                    extraDataMap.get(identifier));
        }
    }

    /**
     * Get the SWT wrapper megawidget associated with the specified identifier.
     * 
     * @param identifier
     *            Megawidget identifier for which to fetch the SWT wrapper.
     * @return SWT wrapper megawidget associated with the identifier, or
     *         <code>null</code> if there is no such megawidget.
     */
    public final SwtWrapperMegawidget getSwtWrapper(String identifier) {
        return swtWrappersForIdentifiers.get(identifier);
    }

    // Protected Methods

    /**
     * Convert the specified value for the specified state identifier to a form
     * suitable for the corresponding megawidget's state. This implementation
     * does no conversion, merely returning the original value. Subclasses may
     * override this method if, for example, a state element is an integer but
     * the megawidget used to view and change the value requires a string. If
     * this is overridden, the method
     * {@link #convertMegawidgetStateToStateElement(String, Object)} should be
     * overridden as well to perform the inverse of the conversion done here.
     * 
     * @param identifier
     *            State identifier.
     * @param value
     *            State element value to be converted to a form suitable for use
     *            as the corresponding megawidget's state.
     * @return Converted value.
     */
    protected Object convertStateElementToMegawidgetState(String identifier,
            Object value) {
        return value;
    }

    /**
     * Convert the specified megawidget state to a form suitable for use as a
     * state element for the specified identifier. This implementation does no
     * conversion, merely returning the original value. Subclasses may override
     * this method if, for example, a state element is an integer but the
     * megawidget used to view and change the value requires a string. If this
     * is overridden, the method
     * {@link #convertStateElementToMegawidgetState(String, Object)} should be
     * overridden as well to perform the inverse of the conversion done here.
     * 
     * @param identifier
     *            State identifier.
     * @param value
     *            Megawidget state value value to be converted to a form
     *            suitable for use as the corresponding state elemeent.
     * @return Converted value.
     */
    protected Object convertMegawidgetStateToStateElement(String identifier,
            Object value) {
        return value;
    }

    // Private Methods

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent widget in which the megawidgets are to be created.
     * @param superClass
     *            Class that must be the superclass of the created megawidgets.
     *            This allows megawidgets of only a certain subclass of
     *            {@link IMegawidget} to be required.
     * @param specifierManager
     *            Megawidget specifier manager from which to fetch the
     *            specifiers that are to govern megawidget construction.
     * @param state
     *            State to be viewed and/or modified via the megawidgets that
     *            are constructed. Each megawidget specifier defined by <code>
     *            specifiers</code> should have an entry in this map, mapping
     *            the specifier's identifier to the value that the megawidget
     *            will take on (with conversions between megawidget state and
     *            state element being performed by
     *            {@link #convertStateElementToMegawidgetState(String, Object)}
     *            and
     *            {@link #convertMegawidgetStateToStateElement(String, Object)}
     *            ).
     * @param managerListener
     *            Listener to be notified of events that occur within this
     *            manager.
     * @param minVisibleTime
     *            Minimum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param maxVisibleTime
     *            Maximum visible time for any time scale megawidgets specified
     *            within <code>specifiers</code>. If no time scale megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @return Set of the top-level megawidgets created during this object's
     *         construction.
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    private <P extends Widget, M extends IMegawidget> Set<M> construct(
            P parent, Class<M> superClass,
            MegawidgetSpecifierManager specifierManager,
            Map<String, Object> state,
            IMegawidgetManagerListener managerListener, long minVisibleTime,
            long maxVisibleTime) throws MegawidgetException {

        /*
         * Remember the specifier manager, parent and state values, and the side
         * effects applier, if any.
         */
        this.specifierManager = specifierManager;
        this.parent = parent;
        this.state = state;
        this.sideEffectsApplier = specifierManager.getSideEffectsApplier();
        this.managerListener = managerListener;

        /*
         * Fill in the megawidget creation parameters map, used to provide
         * parameters to megawidgets created via megawidget specifiers at the
         * megawidgets' creation time.
         */
        Map<String, Object> megawidgetCreationParams = new HashMap<>();
        megawidgetCreationParams.put(INotifier.NOTIFICATION_LISTENER,
                notificationListener);
        megawidgetCreationParams.put(IStateful.STATE_CHANGE_LISTENER,
                stateChangeListener);
        megawidgetCreationParams.put(IResizer.RESIZE_LISTENER, resizeListener);
        megawidgetCreationParams.put(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                minVisibleTime);
        megawidgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                maxVisibleTime);
        megawidgetCreationParams.put(
                TimeMegawidgetSpecifier.CURRENT_TIME_PROVIDER,
                specifierManager.getCurrentTimeProvider());

        /*
         * Iterate through the megawidget specifiers, instantiating each one in
         * turn, and then instantiating the corresponding megawidget.
         */
        Set<M> baseMegawidgets = new HashSet<>();
        for (ISpecifier specifier : specifierManager.getSpecifiers()) {

            /*
             * Create the megawidget based on the specifications, and set its
             * starting state if it has state, also recording it if stateful.
             */
            M megawidget = specifier.createMegawidget(parent, superClass,
                    megawidgetCreationParams);
            baseMegawidgets.add(megawidget);
            rememberMegawidgets(megawidget);
            setState(megawidget, true);
        }

        /*
         * Apply side effects to the megawidgets to finish initializing them.
         */
        if (sideEffectsApplier != null) {
            applySideEffects(null, true);
        }

        /*
         * Return the resulting base megawidgets.
         */
        return baseMegawidgets;
    }

    /**
     * Add the specified megawidget, and any descendants it has, to the set of
     * megawidgets being managed.
     * 
     * @param megawidget
     *            Megawidget to be remembered, along with any descendants.
     */
    @SuppressWarnings("unchecked")
    private void rememberMegawidgets(IMegawidget megawidget) {
        megawidgetsForIdentifiers.put(
                megawidget.getSpecifier().getIdentifier(), megawidget);
        if (megawidget instanceof SwtWrapperMegawidget) {
            swtWrappersForIdentifiers.put(megawidget.getSpecifier()
                    .getIdentifier(), (SwtWrapperMegawidget) megawidget);
        }
        if (megawidget instanceof IParent) {
            for (IMegawidget childMegawidget : ((IParent<? extends IMegawidget>) megawidget)
                    .getChildren()) {
                rememberMegawidgets(childMegawidget);
            }
        }
    }

    /**
     * Set the specified mutable properties of the specified megawidgets to the
     * given values.
     * <p>
     * Note that {@link #convertStateElementToMegawidgetState(String, Object)}
     * is not used by this method to preprocess any state values that are being
     * changed within the mutable properties map. It is assumed that state
     * values being changed via a call to this method do not require conversion.
     * 
     * @param mutableProperties
     *            Map of the identifiers of all megawidgets for which mutable
     *            properties are to be changed to submaps holding the new
     *            mutable properties. Each of the latter maps the names of any
     *            mutable properties being changed to their new values.
     * @return Map of state identifiers to their new values for those states
     *         that were changed as a result of this call.
     * @throws MegawidgetPropertyException
     *             If a problem occurs while attempting to set the mutable
     *             properties.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> doSetMutableProperties(
            Map<String, Map<String, Object>> mutableProperties)
            throws MegawidgetPropertyException {

        /*
         * Iterate through the mutable properties map, setting the properties
         * for each megawidget in turn, keeping track of which states, if any,
         * are being changed part of their mutable property changes.
         */
        Map<String, Object> valuesForChangedStates = new HashMap<>(
                mutableProperties.size());
        for (String identifier : mutableProperties.keySet()) {

            /*
             * Ensure that the megawidget exists.
             */
            IMegawidget megawidget = megawidgetsForIdentifiers.get(identifier);
            if (megawidget == null) {
                throw new MegawidgetPropertyException(identifier, null, null,
                        null, "no megawidget for identifier \"" + identifier
                                + "\"");
            }

            /*
             * Set the megawidget's mutable properties.
             */
            Map<String, Object> megawidgetMutableProperties = mutableProperties
                    .get(identifier);
            megawidget.setMutableProperties(megawidgetMutableProperties);

            /*
             * Ensure that the state, if changed, is kept track of within this
             * instance's state variable.
             */
            if (megawidgetMutableProperties
                    .containsKey(StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES)) {

                /*
                 * Handle the values whether they are a single value, or a map
                 * of values. The former is only possible if this is a
                 * single-state megawidget.
                 */
                Object values = megawidgetMutableProperties
                        .get(StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES);
                Map<String, Object> map = null;
                if (values instanceof Map) {
                    try {
                        map = (HashMap<String, Object>) values;
                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "Should not occur; state "
                                        + "values should have already been checked by "
                                        + "megawidget.setMutableProperties()");
                    }
                } else {
                    map = new HashMap<>();
                    map.put(identifier, values);
                }

                /*
                 * Iterate through the state identifiers, setting each value as
                 * the state for the corresponding identifier. Note that the
                 * state is fetched from the megawidget, instead of taken from
                 * the mutable properties map, just to ensure that any type
                 * conversions have already been performed. For example, if the
                 * side effects applier used JSON to store values, a long
                 * integer may have been erroneously converted to a double.
                 */
                IStateful statefulMegawidget = (IStateful) megawidget;
                for (String stateIdentifier : map.keySet()) {
                    Object value = null;
                    try {
                        value = convertMegawidgetStateToStateElement(
                                stateIdentifier,
                                statefulMegawidget.getState(stateIdentifier));
                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "Should not occur; state "
                                        + "identifiers should already have been checked "
                                        + "by megawidget.setMutableProperties()");
                    }
                    commitStateElementChange(stateIdentifier, value);
                    valuesForChangedStates.put(stateIdentifier, value);
                }
            }

            /*
             * Remember the fact that properties were programmatically changed.
             */
            propertyProgrammaticallyChanged = true;
        }

        /*
         * Return the map of states that were changed as a result of this call.
         */
        return valuesForChangedStates;
    }

    /**
     * Commit the specified value to the specified key in the state map.
     * 
     * @param identifier
     *            Identifier of the parameter to be set.
     * @param value
     *            New value of the parameter.
     */
    @SuppressWarnings("unchecked")
    private void commitStateElementChange(String identifier, Object value) {

        /*
         * Split up the identifier by greater-thans (>) it contains. It is
         * assumed that each string preceding a greater-than is a map, nested
         * within another map if it also has a greater-than before it, and only
         * the last string following the last greater-than is the name of the
         * actual key to the value. This allows the values map to include values
         * nested within other maps within it.
         */
        String[] keys = identifier.split(">");

        /*
         * Iterate through the keys for the nested maps, treating the value for
         * each as a map except for the last one, which is the one which is to
         * be set to the new value.
         */
        Map<String, Object> map = state;
        for (int j = 0; j < keys.length; j++) {
            if (map == null) {
                return;
            } else if (j == keys.length - 1) {

                /*
                 * Set the map value to match the new value.
                 */
                map.put(keys[j], value);
            } else {

                /*
                 * Get ready for the next iteration through the loop by using
                 * this value as the nested map to look in for the next step. If
                 * the value does not exist or is not itself a map, create it as
                 * a map and add it.
                 */
                Object mapObj = map.get(keys[j]);
                if ((mapObj != null) && (mapObj instanceof Map)) {
                    map = (Map<String, Object>) mapObj;
                } else {
                    Map<String, Object> newMap = new HashMap<>();
                    map.put(keys[j], newMap);
                    map = newMap;
                }
            }
        }
    }

    /**
     * Set the starting state of the specified megawidget and any of its
     * descendants (children, grandchildren, etc.) as appropriate, and record
     * any stateful megawidgets.
     * 
     * @param megawidget
     *            Megawidget for which state is to be set.
     * @param isStart
     *            Flag indicating whether or not this is the first time this
     *            megawidget has had its value set.
     * @throws MegawidgetStateException
     *             If the state for the specified megawidget is invalid.
     */
    @SuppressWarnings("unchecked")
    private void setState(IMegawidget megawidget, boolean isStart)
            throws MegawidgetStateException {

        /*
         * Determine the specifier for this megawidget.
         */
        MegawidgetSpecifier specifier = megawidget.getSpecifier();

        /*
         * If the megawidget is stateful and state has been specified for it,
         * assign it that state.
         */
        if (megawidget instanceof IStateful) {

            /*
             * For each state identifier associated with this megawidget, assign
             * the megawidget the proper state associated with that identifier.
             * If the megawidget is of the explicit-commit type, set the states
             * in an uncommitted fashion and then commit them all at once.
             */
            boolean toBeCommitted = false;
            List<String> identifiers = ((IStatefulSpecifier) specifier)
                    .getStateIdentifiers();
            for (String identifier : identifiers) {

                /*
                 * Remember that this megawidget is associated with this state
                 * identifier, and that this megawidget's identifier is
                 * associated with all of its state identifiers, if this is the
                 * first time this megawidget has had its state set.
                 * Additionally, if it is a time scale megawidget, put it in the
                 * set of such megawidgets as well, so that it may be notified
                 * of visible time range changes.
                 */
                if (isStart) {
                    statefulMegawidgetsForIdentifiers.put(identifier,
                            (IStateful) megawidget);
                    for (String stateIdentifier : ((IStatefulSpecifier) ((IStateful) megawidget)
                            .getSpecifier()).getStateIdentifiers()) {
                        megawidgetIdentifiersForStateIdentifiers.put(
                                stateIdentifier, identifier);
                    }
                    if (megawidget instanceof TimeScaleMegawidget) {
                        timeScaleMegawidgets
                                .add((TimeScaleMegawidget) megawidget);
                    }
                }

                Object value = getStateElement(identifier);
                if (value != null) {

                    /*
                     * Do any conversion of this state element to prepare it to
                     * be used as the state of a megawidget.
                     */
                    value = convertStateElementToMegawidgetState(identifier,
                            value);

                    /*
                     * Set the megawidget state to match the value.
                     */
                    if (megawidget instanceof IExplicitCommitStateful) {
                        ((IExplicitCommitStateful) megawidget)
                                .setUncommittedState(identifier, value);
                        toBeCommitted = true;
                    } else {
                        ((IStateful) megawidget).setState(identifier, value);
                    }
                }
            }

            /*
             * If the megawidget has state changes that are to be committed,
             * commit them now.
             */
            if (toBeCommitted) {
                ((IExplicitCommitStateful) megawidget).commitStateChanges();
            }
        }

        /*
         * If the megawidget is a container, iterate through its children,
         * recursively calling this method for each one.
         */
        if (megawidget instanceof IParent) {
            for (IMegawidget childMegawidget : ((IParent<? extends IMegawidget>) megawidget)
                    .getChildren()) {
                setState(childMegawidget, isStart);
            }
        }

        /*
         * Disable the megawidget if necessary.
         */
        if (enabled == false) {
            megawidget.setEnabled(false);
        }
    }

    /**
     * Apply side effects resulting from the specified megawidgets experiencing
     * a state change or invocation.
     * 
     * @param identifiers
     *            Collection of identifiers of the megawidgets that underwent a
     *            state change or were invoked, or <code>null</code> if this
     *            invocation is the result of initialization.
     * @param stateChangeOccurred
     *            Flag indicating whether or not a state change occurrence
     *            triggered this invocation.
     */
    private void applySideEffects(Collection<String> identifiers,
            boolean stateChangeOccurred) {

        /*
         * Apply the side effects, getting a map of changed mutable properties
         * back.
         */
        Map<String, Map<String, Object>> changedProperties = sideEffectsApplier
                .applySideEffects(
                        identifiers,
                        getMutableProperties(),
                        (stateChangeOccurred || propertyProgrammaticallyChanged));

        /*
         * If at least some properties have changed, set them as the new mutable
         * properties. Then send out notifications for any that were state
         * changes.
         */
        if (changedProperties != null) {
            Map<String, Object> valuesForChangedStates = null;
            try {
                valuesForChangedStates = doSetMutableProperties(changedProperties);
            } catch (MegawidgetPropertyException e) {
                if (managerListener != null) {
                    managerListener
                            .sideEffectMutablePropertyChangeErrorOccurred(this,
                                    e);
                }
            }
            if ((valuesForChangedStates != null) && (managerListener != null)) {
                managerListener.stateElementsChanged(this,
                        valuesForChangedStates);
            }
        }
        propertyProgrammaticallyChanged = false;
    }
}