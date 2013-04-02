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

/**
 * Megawidget manager class, used to instantiate megawidgets based upon
 * specifiers and to manage their state changes.
 * <p>
 * The manager is instantiated using parameters that include a list of maps,
 * each defining a megawidget specifier; a maps providing the state that the
 * megawidgets will allow the user to view and/or manipulate; and time boundary
 * values for any time scale megawidgets that will be created. The state map's
 * elements are known as <i>state elements</i>, as distinguished from
 * <code>megawidget states</code>. The two are often identical, but see below
 * for cases where conversion must be performed.
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
 * <p>
 * Subclasses that require that their megawidgets use modified (converted) forms
 * of state elements for their states must override the methods <code>
 * convertStateElementToMegawidgetState()</code> and <code>
 * convertMegawidgetStateToStateElement()</code>. For example, if a state
 * element is an integer, but the megawidget that is to allow viewing and/or
 * manipulation of the state element takes a string as its megawidget state,
 * these methods would be overridden to perform these conversions in both
 * directions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 2013                Chris.Golden      Initial creation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 * @see MegawidgetSpecifier
 * @see IStateful
 * @see IStatefulSpecifier
 */
public abstract class MegawidgetManager {

    // Private Variables

    /**
     * Map of megawidget specifier identifiers from <code>specifiers
     * </code> to their values.
     */
    private Map<String, Object> state;

    /**
     * Set of all megawidgets being managed.
     */
    private final Set<IMegawidget> megawidgets = new HashSet<IMegawidget>();

    /**
     * Map pairing megawidget identifiers with their corresponding stateful
     * megawidgets.
     */
    private final Map<String, IStateful> statefulMegawidgetsForIdentifiers = new HashMap<String, IStateful>();

    /**
     * Set of all time scale megawidgets.
     */
    private final Set<TimeScaleMegawidget> timeScaleMegawidgets = new HashSet<TimeScaleMegawidget>();

    /**
     * Flag indicating whether or not the managed megawidgets are currently
     * enabled.
     */
    private boolean enabled = true;

    /**
     * Notification listener.
     */
    private final INotificationListener notificationListener = new INotificationListener() {
        @Override
        public void megawidgetInvoked(INotifier megawidget, String extraCallback) {
            commandInvoked(megawidget.getSpecifier().getIdentifier(),
                    extraCallback);
        }
    };

    /**
     * State change listener.
     */
    private final IStateChangeListener stateChangeListener = new IStateChangeListener() {
        @Override
        public void megawidgetStateChanged(IStateful megawidget,
                String identifier, Object state) {

            // Do any preprocessing of the state required by a subclass.
            state = convertMegawidgetStateToStateElement(identifier, state);

            // Remember the new state.
            commitStateElementChange(identifier, state);

            // Allow the subclass to respond as appropriate to the state change.
            stateElementChanged(identifier, state);
        }
    };

    // Public Constructors

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
     *            state element being performed by <code>
     *            convertStateElementToMegawidgetState()</code> and <code>
     *            convertMegawidgetStateToStateElement()</code>).
     * @throws MegawidgetException
     *             If one of the megawidget specifiers is invalid, or if an
     *             error occurs while creating or initializing one of the
     *             megawidgets.
     */
    public MegawidgetManager(Menu parent,
            List<? extends Map<String, Object>> specifiers,
            Map<String, Object> state) throws MegawidgetException {
        construct(parent, specifiers, state, 0L, 0L, 0L, 0L);
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
     *            state element being performed by <code>
     *            convertStateElementToMegawidgetState()</code> and <code>
     *            convertMegawidgetStateToStateElement()</code>).
     * @param minTime
     *            Minimum time that may be used by any time scale megawidgets
     *            specified within <code>specifiers</code>. If no time scale
     *            megawidgets are included in <code>specifiers</code>, this is
     *            ignored.
     * @param maxTime
     *            Maximum time that may be used by any time scale megawidgets
     *            specified within <code>specifiers</code>. If no time scale
     *            megawidgets are included in <code>specifiers</code>, this is
     *            ignored.
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
            List<? extends Map<String, Object>> specifiers,
            Map<String, Object> state, long minTime, long maxTime,
            long minVisibleTime, long maxVisibleTime)
            throws MegawidgetException {

        // Ensure that the parent has the properly configured layout manager.
        Layout layout = parent.getLayout();
        if ((layout instanceof GridLayout) == false) {
            parent.setLayout(new GridLayout(1, false));
        } else if (((GridLayout) layout).numColumns != 1) {
            ((GridLayout) layout).numColumns = 1;
        }

        // Do the heavy lifting for construction.
        Set<Megawidget> baseMegawidgets = construct(parent, specifiers, state,
                minTime, maxTime, minVisibleTime, maxVisibleTime);

        // Align the base megawidgets' component elements to one another
        // visually.
        Megawidget.alignMegawidgetsElements(baseMegawidgets);
    }

    // Public Methods

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
        for (IMegawidget megawidget : megawidgets) {
            megawidget.setEnabled(enabled);
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

        // Split up the identifier by greater-thans (>) it contains. It
        // is assumed that each string preceding a greater-than is a
        // map, nested within another map if it also has a greater-than
        // before it, and only the last string following the last
        // greater-than is the name of the actual key to the value.
        // This allows the values map to include values nested within
        // other maps within it.
        String[] keys = identifier.split(">");

        // Iterate through the keys for the nested maps, treating the
        // value for each as a map except for the last one, which is
        // the value for the megawidget's state.
        Map<?, ?> map = state;
        for (int j = 0; j < keys.length; j++) {
            Object value = map.get(keys[j]);
            if (j == keys.length - 1) {
                return value;
            } else {

                // Get ready for the next iteration through the loop by
                // using this value as the nested map to look in for
                // the next step.
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
     *            <code>specifiers</code> in the constructor of the manager
     *            should have an entry in this map, associating the specifier's
     *            identifier with the value that the megawidget will take on
     *            (with conversions between megawidget state and state element
     *            being performed by <code>
     *            convertStateElementToMegawidgetState()</code> and
     *            <code>convertMegawidgetStateToStateElement()</code>).
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
    }

    // Protected Methods

    /**
     * Respond to a command being issued as a result of a megawidget being
     * invoked.
     * 
     * @param identifier
     *            Identifier of the command.
     * @param extraCallback
     *            Optional extra information associated with this invocation.
     */
    protected abstract void commandInvoked(String identifier,
            String extraCallback);

    /**
     * Respond to a state element having been changed as a result of a
     * megawidget's state changing.
     * 
     * @param identifier
     *            Identifier of the state element.
     * @param state
     *            New value of the state element.
     */
    protected abstract void stateElementChanged(String identifier, Object state);

    /**
     * Convert the specified value for the specified state identifier to a form
     * suitable for the corresponding megawidget's state. This implementation
     * does no conversion, merely returning the original value. Subclasses may
     * override this method if, for example, a state element is an integer but
     * the megawidget used to view and change the value requires a string. If
     * this is overridden, the method <code>
     * convertMegawidgetStateToStateElement()</code> should be overridden as
     * well to perform the inverse of the conversion done here.
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
     * is overridden, the method <code>
     * convertStateElementToMegawidgetState()</code> should be overridden as
     * well to perform the inverse of the conversion done here.
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
     *            state element being performed by <code>
     *            convertStateElementToMegawidgetState()</code> and <code>
     *            convertMegawidgetStateToStateElement()</code>).
     * @param minTime
     *            Minimum time that may be used by any time scale megawidgets
     *            specified within <code>specifiers</code>. If no time scale
     *            megawidgets are included in <code>specifiers</code>, this is
     *            ignored.
     * @param maxTime
     *            Maximum time that may be used by any time scale megawidgets
     *            specified within <code>specifiers</code>. If no time scale
     *            megawidgets are included in <code>specifiers</code>, this is
     *            ignored.
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
    private <P extends Widget> Set<Megawidget> construct(P parent,
            List<? extends Map<String, Object>> specifiers,
            Map<String, Object> state, long minTime, long maxTime,
            long minVisibleTime, long maxVisibleTime)
            throws MegawidgetException {

        // Remember the state values.
        this.state = state;

        // Fill in the megawidget creation parameters map, used to provide para-
        // meters to megawidgets created via megawidget specifiers at the mega-
        // widgets' creation time.
        Map<String, Object> megawidgetCreationParams = new HashMap<String, Object>();
        megawidgetCreationParams.put(INotifier.NOTIFICATION_LISTENER,
                notificationListener);
        megawidgetCreationParams.put(IStateful.STATE_CHANGE_LISTENER,
                stateChangeListener);
        megawidgetCreationParams.put(TimeScaleSpecifier.MINIMUM_TIME, minTime);
        megawidgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_TIME, maxTime);
        megawidgetCreationParams.put(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                minVisibleTime);
        megawidgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                maxVisibleTime);

        // Iterate through the megawidget specifiers, instantiating each one
        // in turn, and then instantiating the corresponding megawidget.
        MegawidgetSpecifierFactory factory = new MegawidgetSpecifierFactory();
        Set<String> identifiers = new HashSet<String>();
        Set<Megawidget> baseMegawidgets = new HashSet<Megawidget>();
        for (Map<String, Object> specifierMap : specifiers) {

            // Create the megawidget specified as instructed.
            MegawidgetSpecifier specifier = factory
                    .createMegawidgetSpecifier(specifierMap);

            // Ensure that any created megawidget specifiers do not have du-
            // plicate identifiers of megawidgets created earlier.
            ensureMegawidgetIdentifiersAreUnique(specifier, identifiers);

            // Create the megawidget based on the specifications, and set its
            // starting state if it has state, also recording it if stateful.
            Megawidget megawidget = specifier.createMegawidget(parent,
                    megawidgetCreationParams);
            baseMegawidgets.add(megawidget);
            rememberMegawidgets(megawidget);
            setState(megawidget, true);
        }
        return baseMegawidgets;
    }

    /**
     * Add the specified megawidget, and any descendants it has, to the set of
     * megawidgets being managed.
     * 
     * @param megawidget
     *            Megawidget to be remembered, along with any descendants.
     */
    private void rememberMegawidgets(IMegawidget widget) {
        megawidgets.add(widget);
        if (widget instanceof IContainer) {
            for (Megawidget childWidget : ((IContainer) widget).getChildren()) {
                rememberMegawidgets(childWidget);
            }
        }
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

        // Split up the identifier by greater-thans (>) it contains. It is
        // assumed that each string preceding a greater-than is a map,
        // nested within another map if it also has a greater-than before
        // it, and only the last string following the last greater-than is
        // the name of the actual key to the value. This allows the values
        // map to include values nested within other maps within it.
        String[] keys = identifier.split(">");

        // Iterate through the keys for the nested maps, treating the
        // value for each as a map except for the last one, which is the
        // one which is to be set to the new value.
        Map<String, Object> map = state;
        for (int j = 0; j < keys.length; j++) {
            if (j == keys.length - 1) {

                // Set the map value to match the new value.
                map.put(keys[j], value);
            } else {

                // Get ready for the next iteration through the loop by us-
                // ing this value as the nested map to look in for the next
                // step. If the value does not exist or is not itself a
                // map, create it as a map and add it.
                Object mapObj = map.get(keys[j]);
                if ((mapObj != null) && (mapObj instanceof Map)) {
                    map = (Map<String, Object>) mapObj;
                } else {
                    Map<String, Object> newMap = new HashMap<String, Object>();
                    map.put(keys[j], newMap);
                    map = newMap;
                }
            }
        }
    }

    /**
     * Ensure that the provided megawidget specifier and any child specifiers it
     * has have identifiers that are unique with respect to one another and are
     * not found in the provided set of identifiers, and if they are indeed
     * unique, add them to the identifiers set.
     * 
     * @param specifier
     *            Megawidget specifier to be checked.
     * @param identifiers
     *            Set of megawidget specifier identifiers collected so far.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    private boolean ensureMegawidgetIdentifiersAreUnique(
            MegawidgetSpecifier specifier, Set<String> identifiers)
            throws MegawidgetSpecificationException {

        // Ensure that this specifier's identifier is unique.
        if (identifiers.contains(specifier.getIdentifier())) {
            throw new MegawidgetSpecificationException(
                    specifier.getIdentifier(), specifier.getType(), null, null,
                    "duplicate " + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER
                            + " value");
        }

        // Add the identifier of this specifier to the set.
        identifiers.add(specifier.getIdentifier());

        // If this specifier is a container, recursively check all its
        // children's identifiers.
        if (specifier instanceof IContainerSpecifier) {
            for (MegawidgetSpecifier childSpecifier : ((IContainerSpecifier) specifier)
                    .getChildMegawidgetSpecifiers()) {
                if (ensureMegawidgetIdentifiersAreUnique(childSpecifier,
                        identifiers) == false) {
                    return false;
                }
            }
        }
        return true;
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
    private void setState(IMegawidget megawidget, boolean isStart)
            throws MegawidgetStateException {

        // Determine the specifier for this megawidget.
        MegawidgetSpecifier specifier = megawidget.getSpecifier();

        // If the megawidget is stateful and state has been specified for
        // it, assign it that state.
        if (megawidget instanceof IStateful) {

            // For each state identifier associated with this megawidget,
            // assign the megawidget the proper state associated with that
            // identifier. If the megawidget is of the explicit-commit
            // type, set the states in an uncommitted fashion and then
            // commit them all at once.
            boolean toBeCommitted = false;
            List<String> identifiers = ((IStatefulSpecifier) specifier)
                    .getStateIdentifiers();
            for (String identifier : identifiers) {

                // Remember that this megawidget is associated with this
                // state identifier, if this is the first time this mega-
                // widget has had its state set. Additionally, if it is a
                // time scale megawidget, put it in the set of such mega-
                // widgets as well, so that it may be notified of visible
                // time range changes.
                if (isStart) {
                    statefulMegawidgetsForIdentifiers.put(identifier,
                            (IStateful) megawidget);
                    if (megawidget instanceof TimeScaleMegawidget) {
                        timeScaleMegawidgets
                                .add((TimeScaleMegawidget) megawidget);
                    }
                }

                Object value = getStateElement(identifier);
                if (value != null) {

                    // Do any conversion of this state element to prepare
                    // it to be used as the state of a megawidget.
                    value = convertStateElementToMegawidgetState(identifier,
                            value);

                    // Set the megawidget state to match the value.
                    if (megawidget instanceof IExplicitCommitStateful) {
                        ((IExplicitCommitStateful) megawidget)
                                .setUncommittedState(identifier, value);
                        toBeCommitted = true;
                    } else {
                        ((IStateful) megawidget).setState(identifier, value);
                    }
                }
            }

            // If the megawidget has state changes that are to be committed,
            // commit them now.
            if (toBeCommitted) {
                ((IExplicitCommitStateful) megawidget).commitStateChanges();
            }
        }

        // If the megawidget is a container, iterate through its children,
        // recursively calling this method for each one.
        if (megawidget instanceof IContainer) {
            for (Megawidget childWidget : ((IContainer) megawidget)
                    .getChildren()) {
                setState(childWidget, isStart);
            }
        }

        // Disable the megawidget if necessary.
        if (enabled == false) {
            megawidget.setEnabled(false);
        }
    }
}